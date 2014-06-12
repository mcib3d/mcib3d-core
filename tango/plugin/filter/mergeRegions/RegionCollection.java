package tango.plugin.filter.mergeRegions;

import java.util.HashMap;
import java.util.TreeMap;
import mcib3d.image3d.ImageHandler;
import mcib3d.image3d.ImageInt;
import tango.gui.Core;

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

public class RegionCollection {
    HashMap<Integer, Region> regions;
    ImageInt labelMap;
    ImageCalibrations cal;
    ImageHandler inputGray, derivativeMap;
    InterfaceCollection interfaces;
    boolean setInterfaces;
    boolean verbose;
    public RegionCollection(ImageInt labelMap, ImageHandler intensityMap, ImageHandler derivativeMap, boolean verbose) {
        this.verbose=verbose;
        this.labelMap=labelMap;
        this.inputGray=intensityMap;
        this.derivativeMap = derivativeMap;
        cal = new ImageCalibrations(labelMap);
        if (inputGray==null) this.setInterfaces=false;
        getRegions();
        if (inputGray!=null) setIntensity();
    }
    
    public void shiftIndicies(boolean updateRegionMap) {
        TreeMap<Integer, Region> sortedReg = new TreeMap<Integer, Region>(regions);
        HashMap<Integer, Region> newRegions = null;
        if (updateRegionMap) newRegions = new HashMap<Integer, Region> (regions.size());
        int curIdx=1;
        for (Region r : sortedReg.values()) {
            if (r.label==0) {
                if (updateRegionMap) newRegions.put(0, r);
                continue;
            }
            r.setVoxelLabel(curIdx);
            r.label=curIdx;
            if (updateRegionMap) newRegions.put(curIdx, r);
            curIdx++;
        }
        if (updateRegionMap) regions=newRegions;
    }
    
    public void initInterfaces() {
        interfaces = new InterfaceCollection(this, verbose);
    }
    
    public void mergeAll() {
        InterfaceCollection.mergeAll(this);
    }
    
    public void mergeSort(int method, double derivativeLimit) {
        if (interfaces==null) initInterfaces();
        interfaces.mergeSort(method, derivativeLimit);
    }
    
    public Region get(int label) {
        return regions.get(label);
    }
    
    protected void getRegions() {
        regions=new HashMap<Integer, Region>();
        regions.put(0, new Region(0, null, this));
        for (int z = 0; z<labelMap.sizeZ; z++) {
            for (int xy = 0; xy<labelMap.sizeXY; xy++) {
                int label = labelMap.getPixelInt(xy, z);
                if (label!=0) {
                    Region r = regions.get(label);
                    if (r==null) regions.put(label, new Region(label, new Vox3D(xy, z, Float.NaN), this));
                    else r.voxels.add(new Vox3D(xy, z, Float.NaN));
                }
            }
        }
        if (verbose) ij.IJ.log("Region collection: nb of spots:"+regions.size());
    }
    
    protected void setIntensity() {
        for (Region r : regions.values()) r.setVoxelIntensity(inputGray);
    }
    
    public void fusion(Region r1, Region r2) {
        regions.remove(r1.fusion(r2).label);
    }
}
