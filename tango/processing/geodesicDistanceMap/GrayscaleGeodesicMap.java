package tango.processing.geodesicDistanceMap;

import java.util.Arrays;
import java.util.TreeSet;
import mcib3d.geom.Object3D;
import mcib3d.image3d.ImageHandler;
import mcib3d.image3d.ImageShort;
import tango.dataStructure.InputImages;
import tango.parameter.Parameter;

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

public class GrayscaleGeodesicMap extends GeodesicMap {
    int cursor=0;
    short curLabel=1;
    IntensityVoxel[] sortedIntensities;
    ImageHandler im;
    public Parameter[] getParametersPreFilter() {
        return new Parameter[] {normalize_P, invert_P};
    }
    
    public void setIntensity(ImageHandler intensityMap) {
        im=intensityMap;
    }
    
    @Override
    protected ImageHandler getIntensity(InputImages raw) {
        if (im!=null) return im;
        else return super.getIntensity(raw);
    }
    
    public void runGrayscale() {
        run(null, true, true);
    }
    
    @Override 
    public void run(Object3D[] seeds, boolean centerOfObjects, boolean contourOfObjects) {
        initDistanceMap();
        if (labelMap!=null) labelMap.erase();
        else labelMap=new ImageShort("gdm_labels", sizeX, sizeY, sizeZ);
        firstRun=true;
        heap=new TreeSet<Voxel>();
        //getDistanceMap().showDuplicate("distanceMap init");
        
        run();
        if (verbose) getLabelMap().showDuplicate("labelMap");
    }
    
    @Override
    protected void computeIntensityMap(InputImages raw) {
        super.computeIntensityMap(raw);
        int count = 0;
        for (int z = 0; z<mask.sizeZ; z++) {
            for (int xy=0;xy<mask.sizeXY;xy++) {
                if (mask.getPixel(xy, z)!=0) count++;
            }
        }
        sortedIntensities=new IntensityVoxel[count];
        count=0;
        for (int z = 0; z<mask.sizeZ; z++) {
            for (int xy=0;xy<mask.sizeXY;xy++) {
                if (mask.getPixel(xy, z)!=0) {
                    sortedIntensities[count]=new IntensityVoxel(xy, z, intensityMap.pixels[z][xy]);
                    count++;
                }
            }
        }
        Arrays.sort(sortedIntensities);
    }
    @Override
    protected void run() {
        // FIXME TOUT REVOIR
        float lastDistance = 0;
        float nextIntensity=sortedIntensities[0].value;
        while(cursor<sortedIntensities.length) {
            if (sortedIntensities[cursor].getLabel()==0) {
                Voxel v=new Voxel(sortedIntensities[cursor].xy, sortedIntensities[cursor].z, lastDistance, curLabel++);
                addVoxel(v);
                propagate(v);
            }
            cursor++;
            if (cursor<sortedIntensities.length) {
                while (cursor<(sortedIntensities.length-1) && sortedIntensities[cursor].getLabel()!=0) cursor++;
                nextIntensity=sortedIntensities[cursor].value;
            }
            
            if (!heap.isEmpty()) {
                while (true) {
                    Voxel v = heap.first();
                    if (v.getIntensity()>nextIntensity) {
                        lastDistance=v.value;
                        break;
                    } else {
                        heap.pollFirst();
                        propagate(v);
                        if (heap.isEmpty()) {
                            lastDistance=v.value;
                            break;
                        }
                    }
                }
            }
        }
    }
    
    protected void addVoxel(Voxel v) {
        heap.add(v);
        labelMap.pixels[v.z][v.xy]=v.label;
        distanceMap.pixels[v.z][v.xy]=v.value;
    }
    
    protected class IntensityVoxel implements java.lang.Comparable<IntensityVoxel> {
        public int xy, z;
        public float value;

        public IntensityVoxel(int xy, int z, float value) {
            this.xy=xy;
            this.z=z;
            this.value=value;
        }
        
        @Override
        public int compareTo(IntensityVoxel v) {
            if(value > v.value) return 1;
            else if(value < v.value) return -1;
            else if (xy==v.xy && z==v.z) return 0;
            else if (z<v.z) return -1;
            else if (z==v.z && xy<v.xy) return -1;
            else return 1;
        }
        
        @Override
        public int hashCode() {
            int hash = 5;
            hash = 41 * hash + this.xy;
            hash = 41 * hash + this.z;
            return hash;
        }
        @Override
        public boolean equals(Object o) {
            if (o instanceof IntensityVoxel) {
                return xy==((IntensityVoxel)o).xy && z==((IntensityVoxel)o).z;
            } else if (o instanceof Voxel) {
                return (xy==((Voxel)o).xy && z==((Voxel)o).z); 
            } else return false;
        }
        
        public short getLabel() {
            return labelMap.pixels[z][xy];
        }
    }
}
