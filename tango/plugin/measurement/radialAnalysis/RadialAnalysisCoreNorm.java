package tango.plugin.measurement.radialAnalysis;

import java.util.Arrays;
import mcib3d.image3d.BlankMask;
import mcib3d.image3d.ImageFloat;
import mcib3d.image3d.ImageHandler;
import mcib3d.image3d.ImageInt;
import tango.dataStructure.InputCellImages;
import tango.dataStructure.SegmentedCellImages;

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

public class RadialAnalysisCoreNorm extends RadialAnalysisCore {
    Vox[] indicies;
    ImageHandler nucImage;
    
    public RadialAnalysisCoreNorm(ImageHandler nucleusIntensity, ImageInt mask, ImageInt segmentedStructure, boolean inside, int nbCPUs, boolean verbose) {
        super(nucleusIntensity, mask, segmentedStructure, inside, nbCPUs, verbose);
    }
    
    public RadialAnalysisCoreNorm(InputCellImages inputImages, SegmentedCellImages images, int structureIdx, int nbCPUs, boolean verbose) {
        super(inputImages, images, structureIdx, nbCPUs, verbose);
    }
    
    
    
    @Override
    protected void initIndicies(ImageHandler nuc, ImageInt maskImage, ImageInt segmentedStructure, boolean inside) {
        int volume =getVolume(maskImage,  segmentedStructure, inside);
        this.indicies=new Vox[volume];
        int count=0;
        if (inside) {
            for (int z = 0; z<distanceMap.sizeZ; z++) {
                for (int xy=0; xy<distanceMap.sizeXY; xy++) {
                    if (segmentedStructure.getPixelInt(xy, z) !=0) {
                        indicies[count]=new Vox(distanceMap.getPixel(xy, z), nuc.getPixel(xy, z));
                        count++;
                    }
                }
            }
        } else {
            for (int z = 0; z<distanceMap.sizeZ; z++) {
                for (int xy=0; xy<distanceMap.sizeXY; xy++) {
                    if (maskImage.getPixelInt(xy, z)!=0 && segmentedStructure.getPixelInt(xy, z)==0) {
                        indicies[count]=new Vox(distanceMap.getPixel(xy, z), nuc.getPixel(xy, z));
                        count++;
                    }
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
                i=j+1;
            }
        }
    }
    
    @Override
    protected double getShell(float  distance) {
        if (distance==0 || indicies.length==0) return 0;
        int idx = Arrays.binarySearch(indicies, new Vox(distance, 0));
        if (idx<0) return indicies[-idx+1].index;
        else return indicies[idx].index;
    }
    
    
    
    
    protected class Vox implements Comparable<Vox>{
        float distance;
        double index;
        public Vox(float distance, double value) {
            this.distance=distance;
            this.index=value;
        }
        @Override
        public int compareTo(Vox v) {
            if (distance > v.distance) return 1;
            else if (distance<v.distance) return -1;
            else return 0;
        }
    }
    
    
    
}
