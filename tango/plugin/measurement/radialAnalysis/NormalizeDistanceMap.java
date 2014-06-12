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
    public void normalizeDistanceMap(ImageFloat distanceMap, ImageInt mask) {
        int count = 0;
        Vox[] idx = new Vox[mask.countMaskVolume()];
        double volume = idx.length;
        for (int z = 0; z<distanceMap.sizeZ; z++) {
            for (int xy=0; xy<distanceMap.sizeXY; xy++) {
                if (mask.getPixelInt(xy, z) !=0) {
                    idx[count]=new Vox(distanceMap.pixels[z][xy], xy, z);
                    count++;
                }
            }
        }
        Arrays.sort(idx);
        for (int i = 0;i<idx.length-1;i++) {
            // gestion des repetitions
            if (idx[i+1].distance==idx[i].distance) {
                int j = i+1;
                while (j<(idx.length-1) && idx[i].distance==idx[j].distance) j++;
                double median = (i+j)/2d;
                for (int k = i; k<=j;k++) idx[k].index=median;
                i=j;
            } else {
                idx[i].index=i;
            }
        }
        if (idx[idx.length-1].index==0) idx[idx.length-1].index = idx.length-1;
        for (int i=0;i<idx.length;i++) {
            distanceMap.pixels[idx[i].z][idx[i].xy]=(float)(idx[i].index/volume);
        }
    }
    
    public void normalizeDistanceMap(ImageFloat distanceMap, ImageInt mask, ImageHandler normalizationMap) {
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
                i=j; //j+1?
            }
        }
        // apply to distance map
        for (int i=0;i<indicies.length;i++) {
            distanceMap.pixels[indicies[i].z][indicies[i].xy]=(float)(indicies[i].index);
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
