package tango.plugin.segmenter;

import ij.IJ;
import ij.process.AutoThresholder;
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
import tango.plugin.filter.Structure;
import tango.plugin.segmenter.SpotSegmenter;
import tango.plugin.thresholder.AutoThreshold;
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
public class WatershedTransform3D {
    //int volumeMin=10;
    //float fusionCoeff=0.5f;
    double gradientScale = 1;
    int volumeDyn=5;
    int volumeDyn2=5;
    double dynamics=0.01;
    
    ImageInt mask;
    ImageHandler input;
    ImageShort segmentedMap;
    ImageHandler watershedMap;
    int limX, limY, limZ, sizeX;
    float aXY, aXZ;
    boolean debug;
    int nCPUs=1;
    //Spot3D[] spots;
    HashMap<Short, Spot3D> spots;
    int sign=1;
    boolean dyn, heightDyn, volDyn, volumeDynConst;
    
    TreeSet<Vox3D> heap;
    double maxDynamics, maxDynamicsWS;

    public WatershedTransform3D(int nCPUs, boolean verbose) {
        this.nCPUs=nCPUs;
        this.debug=verbose;
    }
    
    public void setDynamics(boolean useDynamics, boolean useVolumeDynamics, int volumeDynamics, boolean useIntensityDynamics, double dynamics, boolean volumeConstraint, int volumeLimit) {
        this.dyn=useDynamics;
        this.heightDyn=useIntensityDynamics;
        this.volDyn=useVolumeDynamics;
        this.volumeDyn=volumeDynamics;
        this.volumeDyn2=volumeLimit;
        this.dynamics=dynamics;
        this.volumeDynConst=volumeConstraint;
    }
 
    public ImageInt runWatershed(ImageHandler input, ImageHandler watershedMap, ImageInt mask_) {
        // INIT
        if (mask_==null) this.mask=new BlankMask(input);
        else this.mask=mask_;
        this.input=input;
        if (watershedMap!=null) this.watershedMap=watershedMap;
        sizeX=input.sizeX;
        limX=this.sizeX-1;
        limY=this.input.sizeY-1;
        limZ=this.input.sizeZ-1;
        aXY=(float)(this.input.getScaleXY()*this.input.getScaleXY());
        aXZ=(float)(this.input.getScaleZ()*this.input.getScaleXY());
        // INIT Dynamics
        if (this.heightDyn) computeDynamicLimits();
        // RUN
        getRegionalMinima();
        
        if (debug) {
            segmentedMap.showDuplicate("Regional Minima");
            ij.IJ.log("nb of regional minima: "+spots.size());
        }
        if (this.spots.isEmpty()) return segmentedMap;
        seededWatershed();
        if (debug) {
            segmentedMap.showDuplicate("Segmented map after propagation");
        }
        //if (this.useHessianThld || this.useIntensityThreshold) eraseSpotsQuantile();
        if (!debug) shiftSegmentedMap();
        return segmentedMap;
    }
    
    protected void computeDynamicLimits() {
        //float[] minMax=this.input.getMinAndMaxArray(this.mask);
        //maxDynamics= (minMax[1]-minMax[0]) * dynamics;
        maxDynamics = getImageDynamic(input) * dynamics;
        
        //minMax=this.watershedMap.getMinAndMaxArray(mask);
        //maxDynamicsWS= Math.abs(minMax[1]-minMax[0]) * dynamics;
        maxDynamicsWS = getImageDynamic(watershedMap) * dynamics;
        
        if (debug) {
            IJ.log("Max Dynamics for intensity:"+maxDynamics);
            IJ.log("Max Dynamics for watershed map:"+maxDynamicsWS);
        }
        
    }
    
    protected double getImageDynamic(ImageHandler image) {
        double thld = AutoThreshold.run(image, mask, AutoThresholder.Method.Otsu);
        double upMean=0;
        double upCount=0;
        double lowMean=0;
        double lowCount=0;
        for (int z = 0; z<mask.sizeZ; z++) {
            for (int xy = 0; xy<mask.sizeXY; xy++) {
                if (mask.getPixel(xy, z)!=0) {
                    double value = image.getPixel(xy, z);
                    if (value>=thld) {
                        upCount++;
                        upMean+=value;
                    } else {
                        lowCount++;
                        lowMean+=value;
                    }
                }
            }
        }
        if (upCount>0) upMean/=upCount;
        if (lowCount>0) lowMean/=lowCount;
        if (debug) {
            IJ.log("Image Dynamics: image"+image.getTitle()+  " upper:"+upMean+ " lower:"+lowMean+ " thld:"+thld);
        }
        return (upMean - lowMean);
    }
    
/*
    protected void eraseSpots() {
        ArrayList<Short> keys = new ArrayList<Short>(spots.keySet());
        for (short key : keys) {
            Spot3D s = spots.get(key);
            if (s.seedIntensity<this.intensityThld || (useHessianThld && hessian.getPixel(s.seed.xy, s.seed.z)>this.hessianThld)) {
                //if (debug) ij.IJ.log("erase spot:"+key);
                s.setLabel((short)0);
                spots.remove(key);
            }
        }
    }
    
    protected void eraseSpotsQuantile() {
        ArrayList<Short> keys = new ArrayList<Short>(spots.keySet());
        for (short key : keys) {
            Spot3D s = spots.get(key);
            if (s.getQuantile(input, 0.5d) <this.intensityThld || (useHessianThld && s.getQuantile(hessian, 0.5d)>this.hessianThld)) {
                //if (debug) ij.IJ.log("erase spot:"+key);
                s.setLabel((short)0);
                spots.remove(key);
            }
        }
    }
    * 
    */
    
    protected void shiftSegmentedMap() {
        short currentLabel=1;
        for (short i : spots.keySet()) {
            for (Vox3D v : spots.get(i).voxels) segmentedMap.pixels[v.z][v.xy]=currentLabel;
            currentLabel++;
        }
    }
    
    protected void getRegionalMinima() {
        segmentedMap = new ImageShort("segMap", sizeX, input.sizeY, input.sizeZ);
        //search for local extrema
        for (int z = 0; z<input.sizeZ; z++) {
            for (int y=0; y<input.sizeY; y++) {
                for (int x = 0; x<sizeX; x++) {
                    int xy=x+y*sizeX;
                    if (mask.getPixel(xy, z)!=0) {
                        if (isLocalMin(x, y, z, watershedMap.getPixel(xy, z))) segmentedMap.pixels[z][xy]=Short.MIN_VALUE;
                    }
                }
            }
        }
        //segmentedMap.showDuplicate("local minima");
        //watershedMap.showDuplicate("watershed Map");
        //this.input.showDuplicate("watershed Map");
        //merge connex local extrema
        this.spots=new HashMap<Short, Spot3D>();
        short currentLabel=Short.MIN_VALUE;
        boolean spotCreated=false;
        for (int z = 0; z<input.sizeZ; z++) {
            for (int y=0; y<input.sizeY; y++) {
                for (int x = 0; x<sizeX; x++) {
                    int xy=x+y*sizeX;
                    if (segmentedMap.pixels[z][xy]!=0) {
                        Spot3D currentSpot=null;
                        spotCreated=false;
                        for (int zz = z-1; zz<=z+1; zz++) {
                            if (zz>=0 && zz<input.sizeZ) {
                                for (int yy = y-1; yy<=y+1; yy++) {
                                    if (yy>=0 && yy<input.sizeY) {
                                        for (int xx = x-1; xx<=x+1; xx++) {
                                            if ((xx!=x || yy!=y || zz!=z) && (xx>=0 && xx<sizeX)) {
                                                int xxyy=xx+yy*sizeX;
                                                short neigh = segmentedMap.pixels[zz][xxyy];
                                                //ij.IJ.log("currentLabel:"+currentLabel+ " spot created:"+spotCreated+" neigh:"+neigh);
                                                if (neigh!=0) {
                                                    if (neigh==Short.MIN_VALUE) {
                                                        if (currentSpot==null) {
                                                            if (currentLabel==Short.MAX_VALUE) {
                                                                System.out.println("Watershed transform error: too many regions image:"+input.getTitle());
                                                                ij.IJ.log("Watershed transform error: too many regions image:"+input.getTitle());
                                                                spots=new HashMap<Short, Spot3D>();
                                                                segmentedMap.erase();
                                                                return;
                                                            }
                                                            currentLabel++;
                                                            if (currentLabel==0) currentLabel++;
                                                            currentSpot = new Spot3D(currentLabel, new Vox3D(xy, z));
                                                            spots.put(currentLabel, currentSpot);
                                                            spotCreated=true;
                                                        } else {
                                                            currentSpot.addVox(new Vox3D(xxyy, zz));
                                                        }
                                                    } else {
                                                        if (currentSpot==null) {
                                                            currentSpot = this.spots.get(neigh);
                                                            currentSpot.addVox(new Vox3D(xy, z));
                                                        } else if (currentSpot.label!=neigh) {
                                                            currentSpot=this.spots.get(neigh).fusion(currentSpot);
                                                            if (spotCreated) {
                                                                spotCreated=false;
                                                                currentLabel--;
                                                                if (currentLabel==0) currentLabel++;
                                                            }
                                                        }
                                                    }
                                                }  
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        if (currentSpot==null) {
                            if (currentLabel==Short.MAX_VALUE) {
                                System.out.println("Watershed transform error: too many regions image:"+input.getTitle());                              
                                ij.IJ.log("Watershed transform error: too many regions image:"+input.getTitle());
                                spots=new HashMap<Short, Spot3D>();
                                segmentedMap.erase();
                                return;
                            }
                            currentLabel++;
                            if (currentLabel==0) currentLabel++;
                            spots.put(currentLabel, new Spot3D(currentLabel, new Vox3D(xy, z)));
                        }
                    }
                }
            }
        }
    }
    
    protected boolean isLocalMin(int x, int y, int z, float ws) {
        for (int zz = z-1; zz<=z+1; zz++) {
            if (zz>=0 && zz<input.sizeZ) {
                for (int yy = y-1; yy<=y+1; yy++) {
                    if (yy>=0 && yy<input.sizeY) {
                        for (int xx = x-1; xx<=x+1; xx++) {
                            if ((xx!=x || yy!=y || zz!=z) && xx>=0 && xx<sizeX) {
                                int xxyy=xx+yy*sizeX;
                                if (mask==null || mask.getPixel(xxyy, zz)!=0) {
                                    if (watershedMap.getPixel(xxyy, zz)<ws) return false;
                                }  
                            }
                        }
                    }
                }
            }
        }
        return true;
    }

    
    protected void seededWatershed() {
        heap = new TreeSet<Vox3D>();
        for (Spot3D s : spots.values()) {
            for (Vox3D v : s.voxels) heap.add(v);
        }
        while (!heap.isEmpty()) {
            Vox3D v = heap.pollFirst();
            int x = v.xy%sizeX;
            int y = v.xy/sizeX;
            Spot3D currentSpot = spots.get(segmentedMap.pixels[v.z][v.xy]);
            if (x<limX && (mask==null || mask.getPixel(v.xy+1, v.z)!=0)) {
                currentSpot=propagate(currentSpot, v,  new Vox3D(v.xy+1, v.z, 0));
            }
            if (x>0 && (mask==null || mask.getPixel(v.xy-1, v.z)!=0)){
                currentSpot=propagate(currentSpot,v,  new Vox3D(v.xy-1, v.z, 0));
            }
            if (y<limY && (mask==null || mask.getPixel(v.xy+sizeX, v.z)!=0)){
                currentSpot=propagate(currentSpot,v,  new Vox3D(v.xy+sizeX, v.z, 0));
            }
            if (y>0 && (mask==null || mask.getPixel(v.xy-sizeX, v.z)!=0)){
                currentSpot=propagate(currentSpot,v,  new Vox3D(v.xy-sizeX, v.z, 0));
            }
            if (v.z<limZ && (mask==null || mask.getPixel(v.xy, v.z+1)!=0)){
                currentSpot=propagate(currentSpot,v,  new Vox3D(v.xy, v.z+1, 0));
            }
            if (v.z>0  && (mask==null || mask.getPixel(v.xy, v.z-1)!=0)){
                propagate(currentSpot,v, new Vox3D(v.xy, v.z-1, 0));
            }
        }
    }
    
    protected Spot3D propagate(Spot3D currentSpot, Vox3D currentVoxel, Vox3D nextVox) { /// nextVox.value = 0 at this step
        short label = segmentedMap.pixels[nextVox.z][nextVox.xy];
        if (label!=0) {
            if (label!=currentSpot.label) {
                Spot3D s2 = spots.get(label);
                if (checkDynamicsCriteria(currentSpot, s2, currentVoxel)) return currentSpot.fusion(s2);
                else heap.remove(nextVox); // FIXME ??et dans les autres directions?
            }
        } else if (continuePropagation(currentVoxel, nextVox)) {
            nextVox.value=watershedMap.getPixel(nextVox.xy, nextVox.z); //*sign.
            currentSpot.addVox(nextVox);
            heap.add(nextVox);
        }
        return currentSpot;
    }

    protected boolean continuePropagation(Vox3D currentVox, Vox3D nextVox) {
        return true;
    }
    
    //fusion criteria during watershed
    public boolean checkDynamicsCriteria(Spot3D s1, Spot3D s2, Vox3D currentVoxel) {
        if (dyn) {
            if (volDyn && !heightDyn)  {
                return s1.voxels.size()<=this.volumeDyn || s2.voxels.size()<=this.volumeDyn;
            }
            else if (volDyn && heightDyn && !this.volumeDynConst)  {
                float intensity = input.getPixel(currentVoxel.xy, currentVoxel.z);
                return  (s1.voxels.size()<=this.volumeDyn || s2.voxels.size()<=this.volumeDyn) || checkDynamics(s1, s2, intensity, currentVoxel.value);
            }
            else if (volDyn && heightDyn && volumeDynConst) {
                float intensity = input.getPixel(currentVoxel.xy, currentVoxel.z);
                return  (s1.voxels.size()<=this.volumeDyn || s2.voxels.size()<=this.volumeDyn) || ( (  s1.voxels.size()<=this.volumeDyn2 && checkDynamics(s1, s2, intensity, currentVoxel.value)) || ( s2.voxels.size()<=this.volumeDyn2 && checkDynamics(s1, s2, intensity, currentVoxel.value)) );
            }
            else if (!volDyn && heightDyn && !volumeDynConst) {
                float intensity = input.getPixel(currentVoxel.xy, currentVoxel.z);
                return checkDynamics(s1, s2, intensity, currentVoxel.value);
            }
            else if (!volDyn && heightDyn && volumeDynConst) {
                float intensity = input.getPixel(currentVoxel.xy, currentVoxel.z);
                return  ( (   s1.voxels.size()<=this.volumeDyn2 && checkDynamics(s1, s2, intensity, currentVoxel.value)) || (  s2.voxels.size()<=this.volumeDyn2 && checkDynamics(s1, s2, intensity, currentVoxel.value)) );
            }
        } else return false;
        return false;
    }
    
    protected boolean checkDynamics(Spot3D s1, Spot3D s2, float intensity, float gradient) {
        return (Math.min(Math.abs(s1.seed.value - gradient), Math.abs(s2.seed.value - gradient))<maxDynamicsWS ) && (Math.min(Math.abs(s1.seedIntensity - intensity), Math.abs(s2.seedIntensity - intensity)) <  maxDynamics);
    }
    
    protected class Vox3D implements java.lang.Comparable<Vox3D> {
        public int xy, z;
        public float value;
        public Vox3D(int xy, int z) {
            this.xy=xy;
            this.z=z;
            this.value=watershedMap.getPixel(xy, z); //*sign
        }
        
        public Vox3D(int xy, int z, float value) {
            this.xy=xy;
            this.z=z;
            this.value=value;
        }


        public void setLabel(short label) {
            segmentedMap.pixels[z][xy]=label;
        }

        @Override
        public int compareTo(Vox3D v) {
            if (v.xy==xy && v.z==z) return 0;
            else if(value > v.value) return 1;
            else if(value < v.value) return -1;
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
        float seedIntensity;
        public Spot3D(short label, Vox3D vox) {
            this.label=label;
            this.voxels=new HashSet<Vox3D>();
            voxels.add(vox);
            seed=vox;
            seedIntensity=input.getPixel(seed.xy, seed.z);
            vox.setLabel(label);
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
            //seed: 
            if (seedIntensity<spot.seedIntensity) {
                seed=spot.seed;
                seedIntensity=spot.seedIntensity;
            }
            
            return this;
        }
        
        public void addVox(Vox3D vox) {
            voxels.add(vox);
            vox.setLabel(label);
        }
        
        public Object3DVoxels toObject3D() {
            ArrayList<Voxel3D> al = new ArrayList<Voxel3D>(voxels.size());
            for (Vox3D v : voxels) al.add(v.toVoxel3D(label));
            return new Object3DVoxels(al);
        }
        
        public double getQuantile(ImageHandler ima, double quantile) {
            float[] pix = new float[voxels.size()];
            int i = 0;
            for (Vox3D v : voxels) {
                pix[i++] = ima.getPixel(v.xy, v.z);
            }
            Arrays.sort(pix);
            double idx = pix.length*quantile;
            int idxInt = (int) idx;
            if (idxInt<=0) return pix[0];
            else if (idxInt>=(pix.length-1)) return pix[pix.length-1];
            else {
                double d = idx - idxInt;
                if (debug) System.out.println("spot:"+label+" get Quantile:"+ima.getTitle()+ " quantile:"+ quantile+ " value:"+(pix[idxInt] * (1-d) + pix[idxInt+1] * d));
                return pix[idxInt] * (1-d) + pix[idxInt+1] * d ; 
            }
        }
    
    }
}
