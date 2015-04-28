package tango.plugin.filter.mergeRegions;

import java.util.*;
import mcib3d.image3d.*;
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

public class InterfaceCollection {
    public static final String[] methods = new String[]{"Absolute Gradient Value", "Relative Gradient Value"};
    int method;
    RegionCollection regions;
    Set<Interface> interfaces;
    ImageHandler intensityMap;
    double strengthLimit;
    boolean verbose;
    
    public InterfaceCollection(RegionCollection regions, boolean verbose) {
        this.regions = regions;
        this.verbose=verbose;
        intensityMap = regions.derivativeMap;
        getInterfaces();
        initializeRegionInterfaces();
        //if (verbose) drawInterfaces();
    }
    
    protected void getInterfaces() {
        HashMap<RegionPair, Interface> interfaceMap = new HashMap<RegionPair, Interface>();
        ImageCalibrations cal = regions.cal;
        ImageInt inputLabels = regions.labelMap;
        int otherLabel;
        for (int z = 0; z<cal.sizeZ; z++) {
            for (int y = 0; y<cal.sizeY; y++) {
                for (int x = 0; x<cal.sizeX; x++) {
                    int label = inputLabels.getPixelInt(x, y, z);
                    if (label==0) continue;
                    Vox3D vox = new Vox3D(x+y*cal.sizeX, z, Float.NaN);
                    // en avant uniquement pour les interactions avec d'autre spots
                    // eventuellement aussi en arriere juste pour interaction avec 0
                    if (x<cal.limX) {
                        otherLabel = inputLabels.getPixelInt(vox.xy+1, vox.z);
                        if (otherLabel!=label) addPair(interfaceMap, label, vox, otherLabel, new Vox3D(vox.xy+1, vox.z, Float.NaN)); // && otherLabel!=0
                    }
                    if (x>0) { // with 0 only
                        otherLabel = inputLabels.getPixelInt(vox.xy-1, vox.z);
                        if (otherLabel==0) addPair(interfaceMap, label, vox, otherLabel, new Vox3D(vox.xy-1, vox.z, Float.NaN));
                    }
                    if (y<cal.limY) {
                        otherLabel = inputLabels.getPixelInt(vox.xy+cal.sizeX, vox.z);
                        if (otherLabel!=label) addPair(interfaceMap, label, vox, otherLabel, new Vox3D(vox.xy+cal.sizeX, vox.z, Float.NaN));
                    }
                    if (y>0) {// with 0 only
                        otherLabel = inputLabels.getPixelInt(vox.xy-cal.sizeX, vox.z);
                        if (otherLabel==0) addPair(interfaceMap, label, vox, otherLabel, new Vox3D(vox.xy-cal.sizeX, vox.z, Float.NaN));
                    }
                    if (vox.z<cal.limZ) {
                        otherLabel = inputLabels.getPixelInt(vox.xy, vox.z+1);
                        if (otherLabel!=label) addPair(interfaceMap, label, vox, otherLabel, new Vox3D(vox.xy, vox.z+1, Float.NaN));
                    }
                    if (vox.z>0) {// with 0 only
                        otherLabel = inputLabels.getPixelInt(vox.xy, vox.z-1);
                        if (otherLabel==0) addPair(interfaceMap, label, vox, otherLabel, new Vox3D(vox.xy, vox.z-1, Float.NaN));
                    }
                }
            }
            
            interfaces = new HashSet<Interface>(interfaceMap.values());
            setVoxelIntensity();
        }
        if (verbose) ij.IJ.log("Interface collection: nb of interfaces:"+interfaces.size());
    }
    
    public static void mergeAll(RegionCollection regions) {
        ImageCalibrations cal = regions.cal;
        ImageInt inputLabels = regions.labelMap;
        int otherLabel;
        for (int z = 0; z<cal.sizeZ; z++) {
            for (int y = 0; y<cal.sizeY; y++) {
                for (int x = 0; x<cal.sizeX; x++) {
                    int label = inputLabels.getPixelInt(x, y, z);
                    if (label==0) continue;
                    Region currentRegion = regions.get(label);
                    Vox3D vox = new Vox3D(x+y*cal.sizeX, z, Float.NaN);
                    if (x<cal.limX) {
                        otherLabel = inputLabels.getPixelInt(vox.xy+1, vox.z);
                        if (otherLabel!=label && otherLabel!=0) {
                            Region otherRegion = regions.get(otherLabel);
                            regions.fusion(currentRegion, otherRegion);
                            if (label>otherLabel) {
                                currentRegion=otherRegion;
                                label=otherLabel;
                            }
                        }
                    }
                    if (y<cal.limY) {
                        otherLabel = inputLabels.getPixelInt(vox.xy+cal.sizeX, vox.z);
                        if (otherLabel!=label && otherLabel!=0) {
                            if (otherLabel!=label && otherLabel!=0) {
                               Region otherRegion = regions.get(otherLabel);
                               regions.fusion(currentRegion, otherRegion);
                               if (label>otherLabel) {
                                    currentRegion=otherRegion;
                                    label=otherLabel;
                                }
                            }
                        }
                    }
                    if (vox.z<cal.limZ) {
                        otherLabel = inputLabels.getPixelInt(vox.xy, vox.z+1);
                        if (otherLabel!=label && otherLabel!=0) {
                            if (otherLabel!=label && otherLabel!=0) {
                                Region otherRegion = regions.get(otherLabel);
                                regions.fusion(currentRegion, otherRegion);
                            }
                        }
                    }
                }
            }
        }
    }
    
    protected void initializeRegionInterfaces() {
        for (Region r : regions.regions.values()) r.interfaces=new ArrayList<Interface>(5);
        for (Interface i : interfaces) {
            i.r1.interfaces.add(i);
            i.r2.interfaces.add(i);
        }
        if (verbose) {
            for (Region r : regions.regions.values()) {
                String in = "Region:"+r.label+" Interfaces: ";
                for (int i = 0; i<r.interfaces.size(); i++) in+=r.interfaces.get(i).getOther(r).label+", ";
                System.out.println(in);
            }
        }
    }
    
    public ImageStats getInterfaceHistogram() {
        int size=0;
        for (Interface i : interfaces) {
            InterfaceVoxSet ivs = (InterfaceVoxSet)i;
            size+=ivs.r1Voxels.size();
            size+=ivs.r2Voxels.size();
        }
        ImageFloat f = new ImageFloat("Interfaces", size, 1, 1);
        int offset = 0;
        for (Interface i : interfaces) {
            InterfaceVoxSet ivs = (InterfaceVoxSet)i;
            for (Vox3D v : ivs.r1Voxels) f.pixels[0][offset++]=v.value;
            for (Vox3D v : ivs.r2Voxels) f.pixels[0][offset++]=v.value;
        }
        f.getHistogram();
        return f.getImageStats(null);
    }
    
    /*protected void addPair(int label1, Vox3D vox1, int label2, Vox3D vox2) {
        RegionPair pair = new RegionPair(label1, label2);
        int idx = interfaces.indexOf(pair);
        if (idx<0) {
            interfaces.add(new Interface(regions.get(pair.r1), regions.get(pair.r2)));
        }
    }
    * 
    */
    
    protected void addPair(HashMap<RegionPair, Interface> interfaces, int label1, Vox3D vox1, int label2, Vox3D vox2) {
        RegionPair pair = new RegionPair(label1, label2);
        Interface inter = interfaces.get(pair);
        if (inter==null) {
            inter = new InterfaceVoxSet(regions.get(pair.r1), regions.get(pair.r2), this);
            interfaces.put(pair, inter);
        }
        ((InterfaceVoxSet)inter).addPair(vox1, vox2);
    }
    
    protected void setVoxelIntensity() {
        if (intensityMap==null) return;
        for (Interface i : interfaces) {
            InterfaceVoxSet ivs = (InterfaceVoxSet)i;
            for (Vox3D v : ivs.r1Voxels) v.value=intensityMap.getPixel(v.xy, v.z);
            for (Vox3D v : ivs.r2Voxels) v.value=intensityMap.getPixel(v.xy, v.z);
        }
    }
    
    protected void drawInterfaces() {
        ImageShort im = new ImageShort("Iterfaces", regions.cal.sizeX, regions.cal.sizeY, regions.cal.sizeZ);
        for (Interface i : interfaces) {
            for (Vox3D v : ((InterfaceVoxSet)i).r1Voxels) {
                im.setPixel(v.xy, v.z, i.r2.label);
            }
            for (Vox3D v : ((InterfaceVoxSet)i).r2Voxels) {
                im.setPixel(v.xy, v.z, i.r1.label);
            }
        }
        im.show();
    }
    
    protected void drawInterfacesStrength() {
        ImageFloat im = new ImageFloat("Iterface Strength", regions.cal.sizeX, regions.cal.sizeY, regions.cal.sizeZ);
        for (Interface i : interfaces) {
            if (i.r1.label==0) continue;
            for (Vox3D v : ((InterfaceVoxSet)i).r1Voxels) {
                im.setPixel(v.xy, v.z, (float)i.strength);
            }
            for (Vox3D v : ((InterfaceVoxSet)i).r2Voxels) {
                im.setPixel(v.xy, v.z, (float)i.strength);
            }
        }
        im.show();
    }
    
    public boolean fusion(Interface i, boolean remove) {
        if (remove) interfaces.remove(i);
        if (i.r1.interfaces!=null) i.r1.interfaces.remove(i);
        boolean change = false;
        if (i.r2.interfaces!=null) {
            for (Interface otherInterface : i.r2.interfaces) { // appends interfaces of deleted region to new region
                if (!otherInterface.equals(i)) {
                    change=true;
                    interfaces.remove(otherInterface);
                    Region otherRegion = otherInterface.getOther(i.r2);
                    int idx = i.r1.interfaces.indexOf(new RegionPair(i.r1, otherRegion));
                    if (idx>=0) {
                        Interface existingInterface = i.r1.interfaces.get(idx);
                        interfaces.remove(existingInterface);
                        existingInterface.mergeInterface(otherInterface);
                        interfaces.add(existingInterface);
                    } else {
                        otherInterface.switchRegion(i.r2, i.r1);
                        i.r1.interfaces.add(otherInterface);
                        interfaces.add(otherInterface);
                    }
                }
            }
        }
        regions.fusion(i.r1, i.r2);
        return change;
    }
    
    protected void mergeSort(int method, double strengthLimit) {
        this.method=method;
        this.strengthLimit=strengthLimit;
        if (verbose) ij.IJ.log("Merge Regions: nb interactions:"+interfaces.size());
        
        for (Interface i : interfaces) i.computeStrength();
        if (verbose) drawInterfacesStrength();
        interfaces = new TreeSet<Interface>(interfaces);
        Iterator<Interface> it = interfaces.iterator(); // descending??
        while (it.hasNext()) {
            Interface i = it.next();
            if (i.r1.label==0) {
                it.remove();
                continue;
            }
            //if (verbose) System.out.println("Interface:"+i);
            if (i.checkFusionCriteria()) {
                it.remove();
                if (fusion(i, false)) it=interfaces.iterator();
            } else if (i.hasNoInteractants()) it.remove();
        }
    }
    
    /*
    protected void mergeSort() {
        //compute strength
        if (Core.debug) ij.IJ.log("Merge Regions: nb interactions:"+interfaces.size());
        for (Interface i : interfaces) i.computeStrength();
        Collections.sort(interfaces);
        int idx = 0;
        while (idx<interfaces.size()) {
            if (interfaces.get(idx).checkFusionCriteria()) {
                if (fusion(interfaces.remove(idx), false)) idx=0; //fusion > sort > RAZ
            } else if (interfaces.get(idx).hasNoInteractants()) interfaces.remove(idx);
            else idx++;
        }
    }
    * 
    */
}
