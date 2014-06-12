package tango.plugin.segmenter.legacy;

import ij.IJ;
import java.util.*;
import mcib3d.geom.Object3DVoxels;
import mcib3d.geom.Voxel3D;
import mcib3d.image3d.*;
import mcib3d.utils.ThreadRunner;
import mcib3d.utils.exceptionPrinter;
import tango.dataStructure.InputCellImages;
import tango.dataStructure.InputImages;
import tango.gui.Core;
import tango.parameter.*;
import tango.plugin.filter.GaussianFit;
import tango.plugin.segmenter.SpotSegmenter;
import tango.plugin.segmenter.SpotSegmenter;
import tango.util.ImageUtils;

/**
 *
 **
 * /**
 * Copyright (C) 2012 Jean Ollion
 *
 *
 *
 * This file is part of tango
 *
 * tango is free software; you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 3 of the License, or (at your option) any later
 * version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 *
 * @author Jean Ollion
 */
public class SeededWatershed3D_ implements SpotSegmenter  {
    int volumeLimit=10000;
    double thldLow, thldHigh;
    float seedRadXY=3;
    float seedRadZ=2;
    int volumeMin=10;
    //float fusionCoeff=0.5f;
    double hessianScale = 1.5;
    double hessianThld = -1;
    float dynamicLimit, dynamicLimitWS;
    double dynamicCoeff=0.01;
    double fusionQuantile=0.75;
    boolean descendingWatershedMap=false;
    IntParameter volumeMin_P = new IntParameter("Volume Min: ", "volumeMin", volumeMin);
    DoubleParameter seedRadXY_P = new DoubleParameter("Seed radius XY", "seedRadXY", (double)seedRadXY, Parameter.nfDEC1);
    DoubleParameter seedRadZ_P = new DoubleParameter("Seed radius Z", "seedRadZ", (double)seedRadZ, Parameter.nfDEC1);
    DoubleParameter hessianScale_P = new DoubleParameter("Hessian Scale:", "hessianScale", hessianScale, Parameter.nfDEC2);
    ThresholdParameter hessianThld_P = new ThresholdParameter("Hessian Upper limit:", "hessianThld", "Percentage Of Bright Pixels", new Parameter[]{new SliderDoubleParameter("pixPercent", "pixPercent", 0, 1, 99.95d, 4)});
    ThresholdParameter seedThld = new ThresholdParameter("Seed Threshold", "seedThld", null);
    GroupParameter seeds_P = new GroupParameter("Seeds", "seeds", new Parameter[]{seedRadXY_P, seedRadZ_P, hessianScale_P, hessianThld_P, seedThld});
    //SliderDoubleParameter fusionCoeff_P = new SliderDoubleParameter("Fusion coeff:", "fusionCoeff",0, 1, (double)fusionCoeff, 2);
    SliderDoubleParameter fusionIntCoeff_P = new SliderDoubleParameter("Dynamic limit for fusion:", "fusionIntCoeff", 0, 0.2,  (double)dynamicCoeff, 3);
    GroupParameter fusion_P = new GroupParameter("Fusion:", "fusion", new Parameter[]{fusionIntCoeff_P, volumeMin_P});
    ThresholdParameter thldLow_P= new ThresholdParameter("Background Limit:", "thldLow", null);
    BooleanParameter descending = new BooleanParameter("Watershed Propagation: descending intensity values?", "descending", descendingWatershedMap); 
    
    Parameter[] parameters;
    ImageInt mask;
    ImageHandler input;
    ImageShort segmentedMap;
    ImageFloat hessian;
    ImageHandler watershedMap;
    int limX, limY, limZ, sizeX;
    float aXY, aXZ;
    boolean debug;
    int nCPUs=1;
    //Spot3D[] spots;
    HashMap<Short, Spot3D> spots;
    int sign=1;
    ImageFloat probaMap;
    ArrayList<AbstractInteraction> interactions;
    ArrayList<Spot3D> deadSpots;
    TreeSet<Vox3D> heap;
    int[] tags;
    public SeededWatershed3D_() {
        
        hessianScale_P.setHelp("hessian smoothing scale, in pixels. \n if the value is inferior to 1 hessain won't be computed (saves time)", true);
        hessianThld_P.setHelp("hessian limit. \n the hessian is used to get the seeds: the hessian value at a seed position must be inferior to this value.\n if the value is >=0 hessain won't be computed (saves time)", true);
        seedThld.setHelp("a the value @ a seed coordinate must be superior to this threshold", true);
        //fusionIntCoeff_P.setHelp("Proportion of the maximal dynamic of the image. If two spots meet", true);
        //fusionCoeff_P.setHelp("After watershed: all interactions are inspected, in the order of decreasing interaction strength. \n two spots in contact are merged if the following condition is verified: \n mean value @ the interface >= mean intensity value of the spots * fusion coeff) ", false);
        //fusionCoeff_P.setHelp("Increase this parameter to increase fusion of spots", true);
        fusionIntCoeff_P.setHelp("Increase this parameter to increase fusion of spots", true);
    }
    
    @Override
    public ImageInt runSpot(int currentStructureIdx, ImageHandler input, InputImages images) {
        this.mask=images.getMask();
        System.out.println(((ImageByte)mask).pixels==null);
        this.input=input;
        init(images);
        postInit(images);
        ImageStats s = watershedMap.getImageStats(images.getMask());
        this.dynamicLimitWS=(float)(dynamicCoeff* (s.getMax()-s.getMin()));
        if (debug) IJ.log("Dynamic limit ws map:"+dynamicLimitWS);
        return run();
    }

    
    protected void init(InputImages images) {
        sizeX=input.sizeX;
        limX=this.sizeX-1;
        limY=this.input.sizeY-1;
        limZ=this.input.sizeZ-1;
        aXY=(float)(this.input.getScaleXY()*this.input.getScaleXY());
        aXZ=(float)(this.input.getScaleZ()*this.input.getScaleXY());
        this.seedRadXY=seedRadXY_P.getFloatValue(seedRadXY);
        this.seedRadZ=seedRadZ_P.getFloatValue(seedRadZ);
        //this.fusionCoeff=(float)fusionCoeff_P.getValue();
        this.dynamicCoeff=fusionIntCoeff_P.getValue();
        this.volumeMin=volumeMin_P.getIntValue(volumeMin);
        this.hessianScale=this.hessianScale_P.getDoubleValue(hessianScale);
        
        this.thldLow=this.thldLow_P.getThreshold(input, images, nCPUs, debug);
        this.thldHigh=this.seedThld.getThreshold(input, images, nCPUs, debug);
        if (thldHigh<thldLow) thldHigh=thldLow;
        this.descendingWatershedMap=this.descending.isSelected();
        ImageStats s = input.getImageStats(images.getMask());
        this.dynamicLimit=(float)(dynamicCoeff* (s.getMax()-s.getMin()));
        
        if (debug) {
            IJ.log("thld low:"+thldLow);
            IJ.log("thld high:"+thldHigh);
            IJ.log("Dynamic limit:"+dynamicLimit);
            
        }
        
        
    }
    
    protected void postInit(InputImages images) {
        watershedMap=input;
        if (hessianScale>=1) {
            hessian=input.getHessian((float)hessianScale, nCPUs)[0];
            this.hessianThld=this.hessianThld_P.getThreshold(hessian, images, nCPUs, debug);
            if (debug) hessian.showDuplicate("hessian transfrom");
        }
    }
    
    protected ImageInt run() {
        ArrayList<Vox3D> seeds=getSeeds();
        if (debug) ij.IJ.log("nb seeds:"+seeds.size());
        seededWatershed(seeds);
        if (debug) ij.IJ.log("nb spots after ws:"+spots.size());
        //if (debug) this.segmentedMap.showDuplicate("Before fusion");
        //if (doGaussianFit.isSelected()) localThreshold();
        //watershedInteraction();
        //if (debug) ij.IJ.log("Interactions ok.");
        if (!debug) shiftIndexes();
        return segmentedMap;
    }
    
    protected void shiftIndexes() {
        short currentLabel=1;
        for (short i : spots.keySet()) {
            for (Vox3D v : spots.get(i).voxels) segmentedMap.pixels[v.z][v.xy]=currentLabel;
            currentLabel++;
        }
    }
    
    /*
    protected void localThreshold() {
        gauss.initialize(segmentedMap, input, mask);
        gauss.setMultithread(nCPUs);
        gauss.setVerbose(debug);
        gauss.run(false);
        //update spots... (localthreshold can shift spots)
        HashMap<Short, Spot3D> newSpots = new HashMap<Short, Spot3D>(spots.size());
        for (Spot3D s : spots.values()) {
            ArrayList<Vox3D> al = new ArrayList<Vox3D>(s.voxels);
            for (Vox3D v : al) if (segmentedMap.pixels[v.z][v.xy]==0) s.voxels.remove(v);
            s.setLabel(segmentedMap.pixels[s.seed.z][s.seed.xy]);
            if (s.label>0) newSpots.put(s.label, s);
        }
        spots=newSpots;
    } 
    
    * 
    */

    
    
    
    
    // FIXME regional extrema cf watershed3D
    // seed = over thldHigh, under thldHessian if hessain not null && verify isSeed
    protected ArrayList<Vox3D> getSeeds() {
        final HashMap<Integer, ArrayList<Vox3D>> seedThread = new HashMap<Integer, ArrayList<Vox3D>>();
        final int[][] neigh = ImageUtils.getNeigh(seedRadXY, seedRadZ);
        final ThreadRunner tr = new ThreadRunner(0, input.sizeZ, nCPUs);
        for (int i = 0; i<tr.threads.length; i++) {
            final ArrayList<Vox3D> s = new ArrayList<Vox3D>();
            seedThread.put(i, s);
            tr.threads[i] = new Thread(
                new Runnable() {
                    public void run() {
                        for (int z = tr.ai.getAndIncrement(); z<tr.end; z = tr.ai.getAndIncrement()) {
                            for (int y=0; y<input.sizeY; y++) {
                                for (int x = 0; x<sizeX; x++) {
                                    int xy=x+y*sizeX;
                                    if (mask==null || mask.getPixel(xy, z)!=0) {
                                        float ws = watershedMap.getPixel(xy, z);
                                        if (input.getPixel(xy, z)>=thldHigh && (hessian==null || hessian.pixels[z][xy]<=hessianThld) && isSeed(neigh, x, y, z, ws)) s.add(new Vox3D(xy, z, ws*sign));
                                    }
                                }
                            }
                        }
                    }
                }
            );
        }
        tr.startAndJoin();
        ArrayList<Vox3D> seeds = new ArrayList<Vox3D>();
        for (ArrayList<Vox3D> al : seedThread.values()) seeds.addAll(al);
        Collections.sort(seeds);
        if (debug) IJ.log("number of seeds:"+seeds.size());
        if (debug) {
            ImageShort seedsMap = new ImageShort("seeds", sizeX, input.sizeY, input.sizeZ);
            for (int i = 0; i<seeds.size(); i++) {
                Vox3D v = seeds.get(i);
                seedsMap.pixels[v.z][v.xy]=(short)(i+1);
            }
            seedsMap.show();
        }
        return seeds;
    }
    
    protected boolean isSeed(int[][] neigh, int x, int y, int z, float max) {
        int zz, yy, xx;
        for (int i = 0; i<neigh[0].length; i++) {
            zz = z+neigh[2][i];
            if (zz>=0 && zz<input.sizeZ) {
                yy = y+neigh[1][i];
                if (yy>=0 && yy<input.sizeY) {
                    xx = x+neigh[0][i];
                    if (xx>=0 && xx<sizeX) {
                        int xxyy=xx+yy*sizeX;
                        if ((mask==null || mask.getPixel(xxyy, zz)!=0) && this.watershedMap.getPixel(xxyy, zz)>max) return false;
                    }
                }
            }
        }
        return true;
    }

    
    protected void seededWatershed(ArrayList<Vox3D> sortedSeeds) {
        segmentedMap = new ImageShort("segMap", sizeX, input.sizeY, input.sizeZ);
        spots=new HashMap<Short, Spot3D>(sortedSeeds.size());
        heap = new TreeSet<Vox3D>();
        for (int i = 0; i<sortedSeeds.size(); i++) {
            Vox3D vox = sortedSeeds.get(i);
            vox.setLabel(i+1);
            heap.add(vox);
            spots.put((short)(i+1), new Spot3D((short)(i+1), vox));
        }
        while (!heap.isEmpty()) {
            Vox3D v = heap.pollFirst();
            int x = v.xy%sizeX;
            int y = v.xy/sizeX;
            float currentIntensity = input.getPixel(v.xy, v.z);
            Spot3D currentSpot = spots.get(segmentedMap.pixels[v.z][v.xy]);
            if (x<limX && (mask==null || mask.getPixel(v.xy+1, v.z)!=0)) {
                currentSpot=propagate(currentSpot,currentIntensity, v.value,  new Vox3D(v.xy+1, v.z, 0));
            }
            if (x>0 && (mask==null || mask.getPixel(v.xy-1, v.z)!=0)){
                currentSpot=propagate(currentSpot,currentIntensity, v.value,  new Vox3D(v.xy-1, v.z, 0));
            }
            if (y<limY && (mask==null || mask.getPixel(v.xy+sizeX, v.z)!=0)){
                currentSpot=propagate(currentSpot,currentIntensity, v.value,  new Vox3D(v.xy+sizeX, v.z, 0));
            }
            if (y>0 && (mask==null || mask.getPixel(v.xy-sizeX, v.z)!=0)){
                currentSpot=propagate(currentSpot,currentIntensity, v.value,  new Vox3D(v.xy-sizeX, v.z, 0));
            }
            if (v.z<limZ && (mask==null || mask.getPixel(v.xy, v.z+1)!=0)){
                currentSpot=propagate(currentSpot,currentIntensity, v.value,  new Vox3D(v.xy, v.z+1, 0));
            }
            if (v.z>0  && (mask==null || mask.getPixel(v.xy, v.z-1)!=0)){
                propagate(currentSpot,currentIntensity, v.value, new Vox3D(v.xy, v.z-1, 0));
            }
            //if (spots[currentLabel].voxels.size()>volumeLimit) break;
        }
    }
    
    protected Spot3D propagate(Spot3D currentSpot, float currentIntensity, float currentWS, Vox3D nextVox) {
        short label = segmentedMap.pixels[nextVox.z][nextVox.xy];
        if (label!=0) {
            if (label!=currentSpot.label) {
                Spot3D s2 = spots.get(label);
                if (checkFusionCriteriaWS(currentSpot, s2, currentIntensity, currentWS)) return currentSpot.fusion(s2);
                else heap.remove(nextVox);
            }
        } else if (input.getPixel(nextVox.xy, nextVox.z)>=thldLow && (!descendingWatershedMap || (watershedMap.getPixel(nextVox.xy, nextVox.z)*sign<=currentWS)) ) {
            nextVox.value=watershedMap.getPixel(nextVox.xy, nextVox.z)*sign;
            currentSpot.addVox(nextVox);
            heap.add(nextVox);
        }
        return currentSpot;
    }

    protected void addInteraction(Spot3D s1, Spot3D s2) {
        Interaction i = new Interaction(s1, s2, 0);
        if (!interactions.contains(i)) interactions.add(i);
    }

    protected void removeInteraction(Spot3D s1, Spot3D s2) {
        interactions.remove(new Interaction(s1, s2, 0));
    }

    /*
    protected void watershedInteraction() {
        interactions = new ArrayList<AbstractInteraction>();
        deadSpots=new ArrayList<Spot3D>();
        try {
        for (Spot3D s : spots.values()) {
            s.addInteractants();
            this.computeSpotInteractionCoeff(s);
        }
        } catch (Exception e) {
            exceptionPrinter.print(e, null, Core.GUIMode);
        }
        if (debug) System.out.println("nb interactions after ws:"+interactions.size());
        for (AbstractInteraction i : interactions) i.computeInteraction();
        Collections.sort(interactions);
        int idx = 0;
        while (idx<interactions.size()) {
            if (interactions.get(idx).checkFusionCriteria()) {
                if (interactions.remove(idx).fusion()) idx=0; //fusion > sort > RAZ
            } else if (interactions.get(idx).getInteractants().isEmpty()) interactions.remove(idx);
            else idx++;
        }
    }
    * 
    */
    
    //fusion criteria during watershed
    public boolean checkFusionCriteriaWS(Spot3D s1, Spot3D s2, float intensity, float ws) {
        if (s1.voxels.size()<volumeMin || s2.voxels.size()<volumeMin) return true;
        if (dynamicCoeff==1) return false;
        //if ((Math.min(s1.seedIntensity, s2.seedIntensity) - intensity) <  this.dynamicLimit) return true;
        return (Math.min(Math.abs(s1.seed.value - ws), Math.abs(s2.seed.value - ws))<dynamicLimitWS ) && (Math.min(Math.abs(s1.seedIntensity - intensity), Math.abs(s2.seedIntensity - intensity)) <  dynamicLimit);
        //else return false;
    }
    
    protected void computeSpotInteractionCoeff(Spot3D s) {
        double mean = 0;
        ArrayList<Vox3D> t1=new ArrayList<Vox3D> (s.voxels);
        Collections.sort(t1);
        double count = 0;
        for (int i =0; i<t1.size()*fusionQuantile; i++) {
            Vox3D vox = t1.get(i);
            mean+=input.getPixel(vox.xy, vox.z);
            count++;
        }
        if (count>0) mean/=count;
        double area= 0;
        for (Vox3D vox: s.voxels) {
            int x = vox.xy%sizeX;
            int y = vox.xy/sizeX;
            if (x<limX && (segmentedMap.pixels[vox.z][vox.xy+1])!=s.label) {
                area+=aXZ;
            }
            if (x>0 && (segmentedMap.pixels[vox.z][vox.xy-1])!=s.label) {
                area+=aXZ;
            }
            if (y<limY && (segmentedMap.pixels[vox.z][vox.xy+sizeX])!=s.label) {
                area+=aXZ;
            }
            if (y>0 && (segmentedMap.pixels[vox.z][vox.xy-sizeX])!=s.label) {
                area+=aXZ;
            }
            if (vox.z<limZ && (segmentedMap.pixels[vox.z+1][vox.xy])!=s.label) {
                area+=aXY;
            }
            if (vox.z>0 && (segmentedMap.pixels[vox.z-1][vox.xy])!=s.label) {
                area+=aXY;
            }
        }
        s.interactionCoeff=area*mean;
    }
    

    @Override
    public void setVerbose(boolean debug) {
        this.debug=debug;
    }

    @Override
    public void setMultithread(int nCPUs) {
        this.nCPUs=nCPUs;
    }

    @Override
    public ImageFloat getProbabilityMap() {
        return probaMap;
    }

    @Override
    public Parameter[] getParameters() {
        return new Parameter[]{thldLow_P, seeds_P, descending, fusion_P}; //
    }

    @Override
    public String getHelp() {
        return "Seeded Watershed 3D. This plugin is under developpement, use at your own risk!";
    }

    protected class Vox3D implements java.lang.Comparable<Vox3D> {
        public int xy, z;
        public float value;

        public Vox3D(int xy, int z, float value) {
            this.xy=xy;
            this.z=z;
            this.value=value;
        }


        public void setLabel(int label) {
            segmentedMap.pixels[z][xy]=(short)label;
        }

        @Override
        public int compareTo(Vox3D v) {
            if (v.xy==xy && v.z==z) return 0;
            else if(value < v.value) return 1;
            else if(value > v.value) return -1;
            //FIXME > l'inverse?
            else if (segmentedMap!=null && (segmentedMap.pixels[z][xy])<(segmentedMap.pixels[v.z][v.xy])) return -1;
            else return 1;
        }
        
        @Override 
        public boolean equals(Object o) {
            if (o instanceof Vox3D) {
                return xy==((Vox3D)o).xy && z==((Vox3D)o).z;
            } return false;
        }

        @Override
        public int hashCode() {
            int hash = 3;
            hash = 47 * hash + this.xy;
            hash = 47 * hash + this.z;
            return hash;
        }

        @Override
        public String toString() {
            return "xy:"+xy+ " z:"+z+ " value:"+value+ " label:"+(segmentedMap.pixels[z][xy]);
        }
        
        public Voxel3D toVoxel3D(double value) {
            return new Voxel3D(xy%sizeX, xy/sizeX, z, value);
        }
    }

    
    
    protected class Spot3D {
        public HashSet<Vox3D> voxels;
        short label;
        Vox3D seed;
        //float fusionThld;
        float seedIntensity;
        double interactionCoeff;
        public Spot3D(short label, Vox3D vox) {
            this.label=label;
            this.voxels=new HashSet<Vox3D>();
            voxels.add(vox);
            seed=vox;
            seedIntensity=input.getPixel(seed.xy, seed.z);
            //fusionThld=(float)(dynamicCoeff*input.getPixel(seed.xy, seed.z));
        }
        
        public void setLabel(short label) {
            this.label=label;
            for (Vox3D v : voxels) v.setLabel(label);
        }

        public Spot3D fusion(Spot3D spot) {
            if (spot.label<label) return spot.fusion(this);
            if (debug) System.out.println("fusion:"+label+ "+"+spot.label);
            for (Vox3D v : spot.voxels) v.setLabel(label);
            this.voxels.addAll(spot.voxels);
            spots.remove(spot.label);
            if (spot.seedIntensity>seedIntensity) {
                seed=spot.seed;
                seedIntensity=spot.seedIntensity;
            }
            return this;
        }
        
        public void addVox(Vox3D vox) {
            voxels.add(vox);
            vox.setLabel(label);
        }
        
        protected void addInteractants() {
            short otherLabel;
            int  x, y;
            for (Vox3D vox: voxels) {
                x = vox.xy%sizeX;
                y = vox.xy/sizeX;
                if (x<limX) {
                    otherLabel = segmentedMap.pixels[vox.z][vox.xy+1];
                    if (otherLabel!=label && otherLabel>0) addInteraction(this, spots.get(otherLabel));
                }
                if (x>0) {
                    otherLabel = segmentedMap.pixels[vox.z][vox.xy-1];
                    if (otherLabel!=label && otherLabel>0) addInteraction(this, spots.get(otherLabel));
                }
                if (y<limY) {
                    otherLabel = segmentedMap.pixels[vox.z][vox.xy+sizeX];
                    if (otherLabel!=label && otherLabel>0) addInteraction(this, spots.get(otherLabel));
                }
                if (y>0) {
                    otherLabel = segmentedMap.pixels[vox.z][vox.xy-sizeX];
                    if (otherLabel!=label && otherLabel>0) addInteraction(this, spots.get(otherLabel));
                }
                if (vox.z<limZ) {
                    otherLabel = segmentedMap.pixels[vox.z+1][vox.xy];
                    if (otherLabel!=label && otherLabel>0) addInteraction(this, spots.get(otherLabel));
                }
                if (vox.z>0) {
                    otherLabel = segmentedMap.pixels[vox.z-1][vox.xy];
                    if (otherLabel!=label && otherLabel>0) addInteraction(this, spots.get(otherLabel));
                }
            }
        }
        
        //FloodFill3D from seed within spot with a given threshold
        public void localThreshold(float thld) {
            TreeSet<Vox3D> heap = new TreeSet<Vox3D>();
            heap.add(seed);
            HashSet<Vox3D> newVox = new HashSet<Vox3D>(voxels.size());
            newVox.add(seed);
            short negLabel = Short.MAX_VALUE;
            for (Vox3D v : voxels) segmentedMap.pixels[v.z][v.xy]=negLabel;
            seed.setLabel(label);
            int limX=sizeX-1;
            int limY=input.sizeY-1;
            int limZ=input.sizeZ-1;
            while (!heap.isEmpty()) {
                Vox3D v = heap.pollFirst();
                int x = v.xy%sizeX;
                int y = v.xy/sizeX;
                if (x<limX && segmentedMap.pixels[v.z][v.xy+1]==negLabel && input.getPixel(v.xy+1, v.z)>=thld) {
                    segmentedMap.pixels[v.z][v.xy+1]=label;
                    Vox3D vox = new Vox3D(v.xy+1, v.z, watershedMap.getPixel(v.xy+1, v.z)*sign);
                    newVox.add(vox);
                    heap.add(vox);
                }
                if (x>0 && segmentedMap.pixels[v.z][v.xy-1]==negLabel && input.getPixel(v.xy-1, v.z)>=thld) {
                    segmentedMap.pixels[v.z][v.xy-1]=label;
                    Vox3D vox = new Vox3D(v.xy-1, v.z, watershedMap.getPixel(v.xy-1, v.z)*sign);
                    newVox.add(vox);
                    heap.add(vox);
                }
                if (y<limY && segmentedMap.pixels[v.z][v.xy+sizeX]==negLabel && input.getPixel(v.xy+sizeX, v.z)>=thld) {
                    segmentedMap.pixels[v.z][v.xy+sizeX]=label;
                    Vox3D vox = new Vox3D(v.xy+sizeX, v.z, watershedMap.getPixel(v.xy+sizeX, v.z)*sign);
                    newVox.add(vox);
                    heap.add(vox);
                }
                if (y>0 && segmentedMap.pixels[v.z][v.xy-sizeX]==negLabel && input.getPixel(v.xy-sizeX, v.z)>=thld) {
                    segmentedMap.pixels[v.z][v.xy-sizeX]=label;
                    Vox3D vox = new Vox3D(v.xy-sizeX, v.z, watershedMap.getPixel(v.xy-sizeX, v.z)*sign);
                    newVox.add(vox);
                    heap.add(vox);
                }
                if (v.z<limZ && segmentedMap.pixels[v.z+1][v.xy]==negLabel && input.getPixel(v.xy, v.z+1)>=thld) {
                    segmentedMap.pixels[v.z+1][v.xy]=label;
                    Vox3D vox = new Vox3D(v.xy, v.z+1, watershedMap.getPixel(v.xy, v.z+1)*sign);
                    newVox.add(vox);
                    heap.add(vox);
                }
                if (v.z>0 && segmentedMap.pixels[v.z-1][v.xy]==negLabel && input.getPixel(v.xy, v.z-1)>=thld) {
                    segmentedMap.pixels[v.z-1][v.xy]=label;
                    Vox3D vox = new Vox3D(v.xy, v.z-1, watershedMap.getPixel(v.xy, v.z-1)*sign);
                    newVox.add(vox);
                    heap.add(vox);
                }
            }
            for (Vox3D v : voxels) if (segmentedMap.pixels[v.z][v.xy]==negLabel) segmentedMap.pixels[v.z][v.xy]=0;
            voxels=newVox;
        }
        
        public Object3DVoxels toObject3D() {
            ArrayList<Voxel3D> al = new ArrayList<Voxel3D>(voxels.size());
            for (Vox3D v : voxels) al.add(v.toVoxel3D(label));
            return new Object3DVoxels(al);
        }
    
    }
    
    protected abstract class AbstractInteraction implements java.lang.Comparable<AbstractInteraction> {
        public Spot3D s1, s2;
        public double v;
        
        public AbstractInteraction(Spot3D s1, Spot3D s2, double v) {
            if (s1.label<s2.label) {
                this.s1=s1;
                this.s2=s2;
            } else {
                this.s1=s2;
                this.s2=s1;
            }
            this.v=v;
        }
        
        
        public boolean fusion() {
            Spot3D fus = s1.fusion(s2);
            computeSpotInteractionCoeff(fus);
            Spot3D other = (fus==s1)?s2:s1;
            deadSpots.add(other);
            removeInteraction(fus, other);
            ArrayList<AbstractInteraction> inters = getInteractants();
            if (inters.isEmpty()) return false;
            for (AbstractInteraction i : inters) {
                if (i.s1==other || i.s2==other) {
                    if (interactions.contains(new Interaction(i.getOther(other), fus, 0))) interactions.remove(i);
                    else {
                        i.switchSpot(other, fus);
                        i.computeInteraction();
                    }
                } else i.computeInteraction();
            }
            Collections.sort(interactions);
            return true;
        }
        
        protected Spot3D getOther(Spot3D s) {
            if (s==s1) return s2;
            else return s1;
        }
        
        protected boolean switchSpot(Spot3D oldSpot, Spot3D newSpot) {
            if (s1==oldSpot && s2!=newSpot) {
                s1=newSpot;
                return true;
            } else if (s2==oldSpot && s1!=newSpot) {
                s2=newSpot;
                return true;
            }
            return false;
        }
        
        protected void removeSpot(Spot3D spot) {
            for (int i = 0; i<interactions.size(); i++) {
                AbstractInteraction inter = interactions.get(i);
                if (inter.s1==spot || inter.s2==spot) {
                    interactions.remove(i);
                    i--;
                }
            }
        }
        
        protected ArrayList<AbstractInteraction> getInteractants() {
            ArrayList<AbstractInteraction> res = new ArrayList<AbstractInteraction>();
            for (AbstractInteraction i : interactions) {
                if (( (i.s1==s1 || i.s1==s2) && i.s2!=null) || ((i.s2==s1 || i.s2==s2) && i.s1!=null)) res.add(i); 
            }
            return res;
        }
        
        protected void computeInteraction() {
            ArrayList<InteractionValue> inter = new ArrayList<InteractionValue>();
            if (s1==null || s2==null) {
                v=Float.NEGATIVE_INFINITY;
                return;
            } else v=0;
            Spot3D min = (s1.voxels.size()<s2.voxels.size())?s1:s2;
            int otherLabel = (min==s1)?s2.label:s1.label;
            for (Vox3D vox: min.voxels) {
                int x = vox.xy%sizeX;
                int y = vox.xy/sizeX;
                if (x<limX && (segmentedMap.pixels[vox.z][vox.xy+1])==otherLabel) {
                    inter.add(new InteractionValue(input.getPixel(vox.xy+1, vox.z)+vox.value, aXZ));
                }
                if (x>0 && (segmentedMap.pixels[vox.z][vox.xy-1])==otherLabel) {
                    inter.add(new InteractionValue(input.getPixel(vox.xy-1, vox.z)+vox.value, aXZ));
                }
                if (y<limY && (segmentedMap.pixels[vox.z][vox.xy+sizeX])==otherLabel) {
                    inter.add(new InteractionValue(input.getPixel(vox.xy+sizeX, vox.z)+vox.value, aXZ));
                }
                if (y>0 && (segmentedMap.pixels[vox.z][vox.xy-sizeX])==otherLabel) {
                    inter.add(new InteractionValue(input.getPixel(vox.xy-sizeX, vox.z)+vox.value, aXZ));
                }
                if (vox.z<limZ && (segmentedMap.pixels[vox.z+1][vox.xy])==otherLabel) {
                    inter.add(new InteractionValue(input.getPixel(vox.xy, vox.z+1)+vox.value, aXY));
                }
                if (vox.z>0 && (segmentedMap.pixels[vox.z-1][vox.xy])==otherLabel) {
                    inter.add(new InteractionValue(input.getPixel(vox.xy, vox.z-1)+vox.value, aXY));
                }
                
            }
            if (inter.isEmpty()) v=Float.NEGATIVE_INFINITY;
            else {
                Collections.sort(inter);
                double count=0;
                for (int i =0; i<inter.size()*fusionQuantile; i++) {
                    v+=inter.get(i).value;
                    count+=inter.get(i).area;
                }
                v/=(count*2);
                count=0;
                for (InteractionValue iv : inter) count+=iv.area;
                v*=count;
                
                v = 2*Math.sqrt(2*v / (s1.interactionCoeff+s2.interactionCoeff));
            }
        }
        
        

        @Override
        public int compareTo(AbstractInteraction t) { //revese order of value / spot
            if (t.v<v) return -1;
            else if (t.v>v) return 1;
            else {
                if (s1!=null && t.s1!=null) {
                    if(s1.label > t.s1.label) return -1;
                    else if (s1.label<t.s1.label) return 1;
                } else {
                    if (s2.label==t.s2.label || t.s2==null || s2==null) return 0;
                    else if (s2.label>t.s2.label) return -1;
                    else return 1;
                }
            } 
            return 0;
        }
        
        @Override 
        public boolean equals(Object o ) {
            if (o instanceof AbstractInteraction) {
                AbstractInteraction i = (AbstractInteraction)o;
                return (s1==i.s1 && s2==i.s2);
            } return false;
        }

        @Override
        public int hashCode() {
            int hash = 3;
            hash = 53 * hash + (this.s1 != null ? this.s1.hashCode() : 0);
            hash = 53 * hash + (this.s2 != null ? this.s2.hashCode() : 0);
            return hash;
        }
            
        @Override
        public String toString() {
            return "spot1:"+s1.label+" spots2:"+s2.label+ " interaction:"+v;
        }
    }
    
    private class Interaction extends AbstractInteraction {
        public Interaction(Spot3D s1, Spot3D s2, float v) {
            super(s1, s2, v);
        }
    }
    
    private class DoubleKey {
        int k1, k2;
        public DoubleKey(int k1, int k2) {
            if (k1<=k2) {
                this.k1=k1;
                this.k2=k2;
            } else {
                this.k1=k2;
                this.k2=k1;
            }
        }
        @Override
        public boolean equals(Object obj) {
            if( ! (obj instanceof DoubleKey))
            return false;
            DoubleKey p = (DoubleKey)obj;
            return k1==p.k1 && k2==p.k2;
        }
    }
    
    protected class InteractionValue implements Comparable<InteractionValue> {
        double value, area;
        public InteractionValue(double value, double area) {
            this.value=value*area;
            this.area=area;
        }

        @Override
        public int compareTo(InteractionValue t) {
            if (value<t.value) return 1;
            else if (value>t.value) return -1;
            else return 0;
        }
    }
    
}
