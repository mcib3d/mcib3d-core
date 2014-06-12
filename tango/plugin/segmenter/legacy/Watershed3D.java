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
import tango.plugin.filter.Structure;
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
public class Watershed3D implements SpotSegmenter  {
    //int volumeMin=10;
    //float fusionCoeff=0.5f;
    double gradientScale = 1;
    int volumeDyn=5;
    int volumeDyn2=5;
    double dynamics=0.01;
    PreFilterParameter watershedMap_P = new PreFilterParameter("Watershed Map:", "watershedMap", "Image Features", new Parameter[]{new ChoiceParameter("", "", new String[]{"Gradient Magnitude"}, "Gradient Magnitude")}); 
    //DoubleParameter gradientScale_P = new DoubleParameter("Graident Scale:", "gradientScale", gradientScale, Parameter.nfDEC2);
    //static String[] dyn = new String[]{"no dynamics", "volume dynamics", "dynamics", "volume dynamics & dynamics", "Dynamics (volume constraint)", "volume dynamics & dynamics (volume constraint)"};
    BooleanParameter useDynamic = new BooleanParameter("Use Dynamics", "useDyn", false);
    BooleanParameter useVolumeDynamic = new BooleanParameter("Volume Dynamic", "useVolDyn", false);
    BooleanParameter useHeightDynamic = new BooleanParameter("Dynamics", "useHeightDyn", false);
    BooleanParameter useVolumeConstraint = new BooleanParameter("Volume constraint:", "useVolumeConstraint", true);
    //ChoiceParameter dynCond = new ChoiceParameter("Regional Minima Dynamics:", "dynamicsCond", dyn, dyn[0]);
    IntParameter volumeDyn_P = new IntParameter("Volume Dynamics: ", "volumeDyn", volumeDyn);
    IntParameter volumeConstraint_P = new IntParameter("Volume constraint: ", "volumeConstraint", volumeDyn2);
    SliderDoubleParameter dynamics_P = new SliderDoubleParameter("Dynamics:", "dynamics", 0, 0.2,  (double)dynamics, 3);
    ConditionalParameter volumDynCond = new ConditionalParameter(useVolumeDynamic);
    ConditionalParameter dynCond = new ConditionalParameter(useDynamic);
    ConditionalParameter heightDynCond = new ConditionalParameter(useHeightDynamic);
    ConditionalParameter volumeConstraintCond = new ConditionalParameter(useVolumeConstraint);
    
    //seeds:
    ThresholdParameter intensityThld_P = new ThresholdParameter("Threshold on Intensity:", "intThld", null);
    BooleanParameter useHessianThld_P = new BooleanParameter("Use Hessian Threshold", "useHessianThld", true);
    ConditionalParameter useHessianCond = new ConditionalParameter(useHessianThld_P);
    DoubleParameter hessianScale = new DoubleParameter("Integration Scale: ", "hessianScale", 1d, DoubleParameter.nfDEC5);
    ThresholdParameter hessianThld_P = new ThresholdParameter("Hessian Threshold:", "hessianThld", "Percentage Of Bright Pixels", new Parameter[]{new DoubleParameter("pixPercent", "pixPercent", 099.5d, Parameter.nfDEC5)});
    
    
    Parameter[] parameters;
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
    ImageFloat hessian;
    TreeSet<Vox3D> heap;
    double maxDynamics, maxDynamicsGrad;
    boolean useHessianThld, useIntensityThreshold;
    double hessianThld;
    double intensityThld;
    public Watershed3D() {
        //gradientScale_P.setHelp("gradient scale, in pixels", true);
        dynCond.setCondition(true, new Parameter[]{volumDynCond, heightDynCond});
        volumDynCond.setCondition(true, new Parameter[]{volumeDyn_P});
        heightDynCond.setCondition(true, new Parameter[]{dynamics_P, volumeConstraintCond});
        volumeConstraintCond.setCondition(true, new Parameter[]{volumeConstraint_P});
        dynamics_P.setHelp("The dynamics of a regional minima is the minimum height (normalized) a pixel has to climb in a walk to reach another regional minima with a lower value", true);
        volumeDyn_P.setHelp("The volume-dynamics is the minimum volume a catchment basin has to raise to reach another regional minima", true);
        volumeConstraint_P.setHelp("Maximum volume of a catchment basin to reach another regional minima. This constraint limits the dynamics constraint", true);
        useHessianCond.setCondition(true, new Parameter[]{hessianScale, hessianThld_P});
        hessianScale.setHelp("Integration scale for Hessian transform", true);
        hessianThld_P.setHelp("Constraint applied after watershed transform: The 0.05-quantile of hessian value of each spot's voxels has to be inferior to this threshold", true);
        intensityThld_P.setHelp("Constraint applied after watershed transform: The 0.9-quantile of intensity value of each spot's voxels has to be superior to this threshold", debug);
        //volumDynCond.toggleVisibility(false);
    }
    
    @Override
    public String getHelp() {
        return "Watershed 3D, using a custom map (gradient magnitude by default). Seeded by regional minima. Use dynamics to merge regional minima (avoiding over-segmentation). Spots are earased after propagation, using constraints on the intensity and hessian value of the extrema. This plugin is under developpement, and is subject to changes in the next versions of TANGO";
    }

    
    @Override
    public ImageInt runSpot(int currentStructureIdx, ImageHandler input, InputImages images) {
        getDynamicsParameters();
        computeThresholds(input, images);
        ImageHandler wsmap = watershedMap_P.preFilter(0, input, images, nCPUs, debug);
        if (debug) wsmap.showDuplicate("Watershed Map");
        return runWatershed(input, wsmap, images.getMask());
    }
    
    public void setThresholds(boolean useIntensityThreshold, double intensityThreshold, boolean useHessianThreshold , double hessianThreshold) {
        this.useHessianThld=useHessianThreshold;
        this.useIntensityThreshold=useIntensityThreshold;
        this.intensityThld=intensityThreshold;
        this.hessianThld=hessianThreshold;
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
    
    protected void getDynamicsParameters() {
        this.volumeDyn=volumeDyn_P.getIntValue(volumeDyn);
        this.volumeDyn2=volumeConstraint_P.getIntValue(volumeDyn2);
        this.dynamics=dynamics_P.getValue();
        this.dyn=useDynamic.getValue();
        this.heightDyn=this.useHeightDynamic.getValue();
        this.volDyn=this.useVolumeDynamic.getValue();
        this.volumeDynConst=this.useVolumeDynamic.getValue();
    }
    
    protected void computeThresholds(ImageHandler input , InputImages images) {
        this.useIntensityThreshold=useHessianThld_P.isSelected();
        if (this.useIntensityThreshold) {
            hessian = input.getHessian(hessianScale.getDoubleValue(1d), nCPUs)[0];
            this.hessianThld=hessianThld_P.getThreshold(hessian, images, nCPUs, debug);
            if (debug) hessian.showDuplicate("hessian transform");
        }
        if (intensityThld_P.getPlugin(nCPUs, debug)!=null) {
            this.intensityThld=this.intensityThld_P.getThreshold(input, images, nCPUs, debug);
            useIntensityThreshold=true;
        }
    }
    
    
    public ImageInt runWatershed(ImageHandler input, ImageHandler watershedMap, ImageInt mask_) {
        // INIT
        if (mask_==null) this.mask=new BlankMask(input);
        else this.mask=mask_;
        this.input=input;
        this.watershedMap=watershedMap;
        sizeX=input.sizeX;
        limX=this.sizeX-1;
        limY=this.input.sizeY-1;
        limZ=this.input.sizeZ-1;
        aXY=(float)(this.input.getScaleXY()*this.input.getScaleXY());
        aXZ=(float)(this.input.getScaleZ()*this.input.getScaleXY());
        // INIT Dynamics
        if (this.heightDyn) {
            ImageStats s = input.getImageStats(mask);
            maxDynamics= (s.getMax()-s.getMin()) * dynamics;
            s = input.getImageStats(mask);
            maxDynamicsGrad= (s.getMax()-s.getMin()) * dynamics;
            if (debug) {
                IJ.log("Max Dynamics for intensity:"+maxDynamics);
                IJ.log("Max Dynamics for gradient:"+maxDynamicsGrad);
            }
        }
        // RUN
        getRegionalMinima();
        if (debug) {
            segmentedMap.showDuplicate("Regional Minima");
            ij.IJ.log("nb of regional minima: "+spots.size());
        }
        seededWatershed();
        if (debug) {
            segmentedMap.showDuplicate("Segmented map after propagation");
        }
        if (this.useHessianThld || this.useIntensityThreshold) eraseSpotsQuantile();
        if (!debug) shiftSegmentedMap();
        return segmentedMap;
    }
    

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
    
    protected void shiftSegmentedMap() {
        short currentLabel=1;
        for (short i : spots.keySet()) {
            for (Vox3D v : spots.get(i).voxels) segmentedMap.pixels[v.z][v.xy]=currentLabel;
            currentLabel++;
        }
    }
    
    protected void getRegionalMinima() {
        segmentedMap = new ImageShort("segMap", sizeX, input.sizeY, input.sizeZ);
        short minValue=(short)1;
        //search for local extrema
        for (int z = 0; z<input.sizeZ; z++) {
            for (int y=0; y<input.sizeY; y++) {
                for (int x = 0; x<sizeX; x++) {
                    int xy=x+y*sizeX;
                    if (mask==null || mask.getPixel(xy, z)!=0) {
                        if (isLocalMin(x, y, z, watershedMap.getPixel(xy, z))) segmentedMap.pixels[z][xy]=minValue;
                    }
                }
            }
        }
        //if (debug) segmentedMap.showDuplicate("local minima");
        //merge connex local extrema
        this.spots=new HashMap<Short, Spot3D>();
        short currentLabel=1;
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
                                                //System.out.println("currentLabel:"+currentLabel+ " spot created:"+spotCreated+" neigh:"+neigh);
                                                if (neigh!=0) {
                                                    if (neigh==minValue) {
                                                        if (currentSpot==null) {
                                                            currentLabel++;
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
                            currentLabel++;
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
    
    protected Spot3D propagate(Spot3D currentSpot, Vox3D currentVoxel, Vox3D nextVox) {
        short label = segmentedMap.pixels[nextVox.z][nextVox.xy];
        if (label!=0) {
            if (label!=currentSpot.label) {
                Spot3D s2 = spots.get(label);
                if (checkDynamicsCriteria(currentSpot, s2, currentVoxel)) return currentSpot.fusion(s2);
                else heap.remove(nextVox); // FIXME ??et dans les autres directions?
            }
        } else {
            nextVox.value=watershedMap.getPixel(nextVox.xy, nextVox.z); //*sign.
            currentSpot.addVox(nextVox);
            heap.add(nextVox);
        }
        return currentSpot;
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
        return (Math.min(Math.abs(s1.seed.value - gradient), Math.abs(s2.seed.value - gradient))<maxDynamicsGrad ) && (Math.min(Math.abs(s1.seedIntensity - intensity), Math.abs(s2.seedIntensity - intensity)) <  maxDynamics);
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
        return null;
    }

    @Override
    public Parameter[] getParameters() {
        return new Parameter[]{this.watershedMap_P, dynCond, intensityThld_P, useHessianCond}; //
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
