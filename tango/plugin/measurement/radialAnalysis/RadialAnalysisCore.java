package tango.plugin.measurement.radialAnalysis;

import java.util.Arrays;
import mcib3d.geom.Object3DVoxels;
import mcib3d.geom.Point3D;
import mcib3d.geom.Voxel3D;
import mcib3d.image3d.*;
import mcib3d.image3d.distanceMap3d.EDT;
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

public class RadialAnalysisCore {
    ImageFloat distanceMap;
    ImageInt mask;
    float[] sortedDistances;
    boolean verbose;
    int nbCPUs;
    
    public RadialAnalysisCore(ImageHandler nucleusIntensity, ImageInt mask, ImageInt segmentedStructure, boolean inside, int nbCPUs, boolean verbose) {
        this.verbose=verbose;
        this.nbCPUs=nbCPUs;
        this.mask=new BlankMask(mask);
        initDistanceMap(segmentedStructure, inside);
        initIndicies(null, mask, segmentedStructure, inside);
    }
    
    public RadialAnalysisCore(InputCellImages inputImages, SegmentedCellImages images, int structureIdx, int nbCPUs, boolean verbose) {
        this.verbose=verbose;
        this.nbCPUs=nbCPUs;
        this.mask=new BlankMask(images.getImage(0));
        this.distanceMap = images.getDistanceMap(structureIdx, nbCPUs);
        if (verbose) distanceMap.showDuplicate("distance map");
        initIndicies(null, images.getImage(0), images.getImage(structureIdx), structureIdx==0);
    }
    
    
    protected void initDistanceMap(ImageInt segmentedStructure, boolean inside) {
        distanceMap=EDT.run(segmentedStructure, 0f, (float)mask.getScaleXY(), (float)mask.getScaleZ(), !inside, nbCPUs);
        if (verbose) distanceMap.showDuplicate("radial analysis:distance map");
        
    }
    
    protected int getVolume(ImageInt maskImage, ImageInt segmentedStructure, boolean inside) {
        int count=0;
        if (inside) {
            for (int z = 0; z<segmentedStructure.sizeZ; z++) {
                for (int xy=0; xy<segmentedStructure.sizeXY; xy++) {
                    if (segmentedStructure.getPixelInt(xy, z) !=0) count++;
                }
            }
        } else { //compute nucleusMask - segmentedStructure
            for (int z = 0; z<segmentedStructure.sizeZ; z++) {
                for (int xy=0; xy<segmentedStructure.sizeXY; xy++) {
                    if (maskImage.getPixelInt(xy, z)!=0 && segmentedStructure.getPixelInt(xy, z)==0) {
                        count++;
                    }
                }
            }
        }
        return count;
    }
    

    
    protected void initIndicies(ImageHandler nucelucIntensity, ImageInt maskImage, ImageInt segmentedStructure, boolean inside) {
        int volume = getVolume(maskImage, segmentedStructure, inside);
        this.sortedDistances=new float[volume];
        int count=0;
        if (inside) {
            for (int z = 0; z<distanceMap.sizeZ; z++) {
                for (int xy=0; xy<distanceMap.sizeXY; xy++) {
                    if (segmentedStructure.getPixelInt(xy, z) !=0) {
                        sortedDistances[count]=distanceMap.getPixel(xy, z);
                        count++;
                    }
                }
            }
        } else {
            for (int z = 0; z<distanceMap.sizeZ; z++) {
                for (int xy=0; xy<distanceMap.sizeXY; xy++) {
                    if (maskImage.getPixelInt(xy, z)!=0 && segmentedStructure.getPixelInt(xy, z)==0) {
                        sortedDistances[count]=distanceMap.getPixel(xy, z);
                        count++;
                    }
                }
            }
        }
        Arrays.sort(sortedDistances);
    }
    

    
    protected double getShell(float  distance) {
        if (distance==0 || sortedDistances.length==0) return 0;
        int idx = Arrays.binarySearch(sortedDistances, distance);
        if (idx<0) return ((double)(-idx+1)/(double)sortedDistances.length);
        else {
           //recherche des bornes a gauche et à droite
           // on prend le milieu
           int left = idx;
           while (left>0 && sortedDistances[left]==distance) left--;
           int right = idx;
           while (right<(sortedDistances.length-1) && sortedDistances[right]==distance) right++;
           return ((double)(right+left)/2d)/(double)sortedDistances.length;
        }
    }
    
    public double getShell(double x, double y, double z) { // returns proportion of volume
        float distance = distanceMap.getPixel((float)x, (float)y, (float)z, mask);
        return getShell(distance);
    }
    
    public double getShell(int x, int y, int z) { // returns proportion of volume
        float distance = distanceMap.getPixel(x, y, z, mask);
        return getShell(distance);
    }
    
    public double getShell(Point3D p) {
        float distance = distanceMap.getPixelInterpolated(p);
        return getShell(distance);
    }
    
    public double getMinShell(Object3DVoxels o) {
        float dmin = Float.MAX_VALUE;
        for (Voxel3D v : o.getContours()) {
            float dist = distanceMap.getPixel(v.getRoundX(), v.getRoundY(), v.getRoundZ());
            if (dist<dmin) dmin=dist;
        }
        return getShell(dmin);
    }
    
    public double getMaxShell(Object3DVoxels o) {
        float dmax = -Float.MAX_VALUE;
        for (Voxel3D v : o.getContours()) {
            float dist = distanceMap.getPixel(v.getRoundX(), v.getRoundY(), v.getRoundZ());
            if (dist>dmax) dmax=dist;
        }
        return getShell(dmax);
    }
    
    public double getMeanShell(Object3DVoxels o) {
        double d = 0;
        for (Voxel3D v : o.getVoxels()) {
            d += distanceMap.getPixel(v.getRoundX(), v.getRoundY(), v.getRoundZ());
        }
        if (o.getVolumePixels()>0) d/=(double)o.getVolumePixels();
        return getShell((float)d);
    }
}
