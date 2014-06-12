package tango.spatialStatistics.util;

import java.util.Arrays;
import java.util.Random;
import mcib3d.geom.Point3D;
import mcib3d.image3d.ImageFloat;
import mcib3d.image3d.ImageInt;
import tango.spatialStatistics.util.Distance;

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

public class SubCell {
    int xMin, xMax, yMin, yMax, zMin, zMax;
    double dmin, dmax, resXY;
    CellPopulation cp;
    public SubCell(int xMin, int xMax, int yMin, int yMax, int zMin, int zMax, CellPopulation cp) {
        this.xMax=xMax;
        this.xMin=xMin;
        this.yMax=yMax;
        this.yMin=yMin;
        this.zMax=zMax;
        this.zMin=zMin;
        dmin=Double.MAX_VALUE;
        dmax=-Double.MAX_VALUE;
        this.cp=cp;
        for (int z=zMin; z<zMax; z++) {
            for (int y=yMin; y<yMax; y++) {
                for (int x=xMin; x<xMax; x++) {
                    int xy = x+y*cp.mask.sizeX;
                    if (cp.mask.getPixelInt(xy, z)!=0) {
                        float dist = cp.dm.getPixel(xy, z); 
                        if (dmin>dist) dmin=dist;
                        if (dmax<dist) dmax=dist;
                    }
                }
            }
        }
        
        
    }
    
    public boolean isValidCandidate(double distance) {
        return distance>=dmin&&distance<=dmax;
    }
    public Point3D draw(float distance, Distance[] distanceArray) {
        // fill distance array
        int idx=0;
        for (int z=zMin; z<zMax; z++) {
            for (int y=yMin; y<yMax; y++) {
                for (int x=xMin; x<xMax; x++) {
                    int xy = x+y*cp.mask.sizeX;
                    if (cp.mask.getPixelInt(xy, z)!=0) {
                        distanceArray[idx++]=new Distance(cp.dm.getPixel(xy, z), xy, z); 
                    }
                }
            }
        }
        // fill with dumb distances
        for (int i = idx; i<distanceArray.length; i++) {
            distanceArray[i]=new Distance(-1, 0, 0);
        }
        // sort
        Arrays.sort(distanceArray);
        
        // draw
        int lowerBound = drawBound((float)(distance-resXY/2), true, distanceArray);
        int upperBound = drawBound((float)(distance+resXY/2), false, distanceArray);
        int i = lowerBound;
        if (upperBound-lowerBound>0) i+=cp.randomGenerator.nextInt(upperBound-lowerBound+1);
        int x = distanceArray[i].xy%cp.mask.sizeX;
        return new Point3D(x, distanceArray[i].xy/cp.mask.sizeX, distanceArray[i].z);
        
    }
    
    
    private int drawBound(float distance, boolean lower, Distance[] distanceArray) {
        int idx = Arrays.binarySearch(distanceArray, new Distance(distance, 0, 0));
        if (idx<0) {
            idx=-idx-1;
            if (idx>0 && (idx==distanceArray.length || (distanceArray[idx].distance-distance>distance-distanceArray[idx-1].distance))) idx--;
            return idx;
        } else {
            // if repeated values:
            if (lower) {
                int lowerBound=idx;
                while (idx>0 && distanceArray[lowerBound].distance==distanceArray[idx].distance) lowerBound--;
                return lowerBound;
            } else {
                int upperBound=idx;
                while (upperBound<distanceArray.length && distanceArray[upperBound].distance==distanceArray[idx].distance) upperBound++;
                return upperBound;
            }
        }
    }
    @Override
    public String toString() {
        return "SubCell: xMin:"+xMin+" xMax:"+xMax+" yMin:"+yMin+" yMax:"+yMax+" zMin:"+zMin+" zMax:"+zMax+" dmin"+dmin+"dmax:"+dmax;
    }
}