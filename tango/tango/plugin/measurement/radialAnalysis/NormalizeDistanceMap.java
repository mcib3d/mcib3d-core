package tango.plugin.measurement.radialAnalysis;

import java.util.Arrays;
import mcib3d.image3d.ImageFloat;
import mcib3d.image3d.ImageHandler;
import mcib3d.image3d.ImageInt;

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

public class NormalizeDistanceMap {
    public void normalizeDistanceMap(ImageFloat distanceMap, ImageInt mask, ImageInt outerMask, boolean interpolationErodedArea) {
        int count = 0;
        Vox[] indicies = new Vox[mask.countMaskVolume()];
        double volume = indicies.length;
        for (int z = 0; z<distanceMap.sizeZ; z++) {
            for (int xy=0; xy<distanceMap.sizeXY; xy++) {
                if (mask.getPixelInt(xy, z) !=0) {
                    indicies[count]=new Vox(distanceMap.pixels[z][xy], xy, z);
                    count++;
                }
            }
        }
        Arrays.sort(indicies);
        for (int i = 0;i<indicies.length-1;i++) {
            // gestion des repetitions : valeur médiane
            if (indicies[i+1].distance==indicies[i].distance) {
                int j = i+1;
                while (j<(indicies.length-1) && indicies[i].distance==indicies[j].distance) j++;
                double median = (i+j)/2d;
                for (int k = i; k<=j;k++) indicies[k].index=median;
                i=j;
            } else {
                indicies[i].index=i;
            }
        }
        if (indicies.length>=1 && indicies[indicies.length-1].index==0) indicies[indicies.length-1].index = indicies.length-1;
        for (int i = 0; i<indicies.length; i++)indicies[i].index/=volume;
        for (Vox v : indicies) {
            distanceMap.pixels[v.z][v.xy] = (float) (v.index);
        }
        if (outerMask!=null) correctDistanceMap(outerMask, mask, distanceMap, indicies, interpolationErodedArea); // correction en cas d'erosion du masque du noyau
    }
    
    public void normalizeDistanceMap(ImageFloat distanceMap, ImageInt mask, ImageHandler normalizationMap, ImageInt outerMask, boolean interpolationErodedArea) {
        int count = 0;
        Vox[] indicies = new Vox[mask.countMaskVolume()];
        for (int z = 0; z<distanceMap.sizeZ; z++) {
            for (int xy=0; xy<distanceMap.sizeXY; xy++) {
                if (mask.getPixelInt(xy, z) !=0) {
                    indicies[count]=new Vox(distanceMap.pixels[z][xy], normalizationMap.getPixel(xy, z), xy, z);
                    count++;
                }
            }
        }
        Arrays.sort(indicies);
        for (int i = 1; i<count; i++) indicies[i].index+=indicies[i-1].index;
        double cumSum = indicies[count-1].index;
        for (int i = 0; i<count; i++)indicies[i].index/=cumSum;
        //gestion des duplicates: median value
        for (int i = 0; i<(count-1); i++) {
            if (indicies[i].distance==indicies[i+1].distance) {
                int j = i+1;
                while (j<(count-1) && indicies[j].distance==indicies[i].distance) j++;
                double median;
                if ((i+j)%2==0) median=indicies[((i+j)/2)].index;
                else median = (indicies[(int)((double)(i+j)/2d)].index + indicies[(int)((double)(i+j)/2d)+1].index)/2d;
                for (int k = i; k<=j; k++) indicies[k].index=median;
                i=j; 
            }
        }
        // apply to distance map
        for (Vox v : indicies) {
            distanceMap.pixels[v.z][v.xy] = (float) (v.index);
        }
        if (outerMask!=null) correctDistanceMap(outerMask, mask, distanceMap, indicies, interpolationErodedArea);
    }
    
    private void correctDistanceMap(ImageInt outerMask, ImageInt innerMask, ImageFloat distanceMap, Vox[] indicies, boolean interpolationErodedArea) {
        if (interpolationErodedArea) {
            for (int z = 0; z<distanceMap.sizeZ; z++) {
                for (int xy=0; xy<distanceMap.sizeXY; xy++) {
                    if (outerMask.getPixelInt(xy, z) !=0 && innerMask.getPixelInt(xy, z)==0 ) {
                        Vox v = new Vox(distanceMap.getPixel(xy, z), xy, z);
                        int i=Arrays.binarySearch(indicies, v);
                        if (i>=0) distanceMap.setPixel(xy, z, (float)indicies[i].index);
                        else {
                            int ins = -i-1;
                            if (ins==indicies.length) distanceMap.setPixel(xy, z, 1);
                            else if (ins==0) distanceMap.setPixel(xy, z, 0);
                            else distanceMap.setPixel(xy, z, (float)(indicies[ins].index+indicies[ins-1].index)/2);
                        }
                    }
                }
            }
        } else {
            for (int z = 0; z<distanceMap.sizeZ; z++) {
                for (int xy=0; xy<distanceMap.sizeXY; xy++) {
                    if (outerMask.getPixelInt(xy, z) !=0 && innerMask.getPixelInt(xy, z)==0 ) {
                        distanceMap.setPixel(xy, z, 0);
                    }
                }
            }
        }
    }
    
    
    protected class Vox implements Comparable<Vox>{
        float distance;
        double index;
        int xy, z;
        public Vox(float distance, int xy, int z) {
            this.distance=distance;
            this.xy=xy;
            this.z=z;
        }
        public Vox(float distance, double index, int xy, int z) {
            this.distance=distance;
            this.index=index;
            this.xy=xy;
            this.z=z;
        }
        @Override
        public int compareTo(Vox v) {
            if (distance > v.distance) return 1;
            else if (distance<v.distance) return -1;
            else return 0;
        }
    }
    
    protected class Vox2 implements Comparable<Vox>{
        float distance;
        double index;
        int xy, z;
        public Vox2(float distance, int xy, int z) {
            this.distance=distance;
            this.xy=xy;
            this.z=z;
        }
        @Override
        public int compareTo(Vox v) {
            if (distance > v.distance) return 1;
            else if (distance<v.distance) return -1;
            else return 0;
        }
    }
}
