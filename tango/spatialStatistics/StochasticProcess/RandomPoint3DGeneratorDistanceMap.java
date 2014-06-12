/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package tango.spatialStatistics.StochasticProcess;

import java.util.Arrays;
import java.util.Comparator;
import java.util.Random;
import mcib3d.geom.Point3D;
import tango.spatialStatistics.util.Distance;
import mcib3d.image3d.ImageFloat;
import mcib3d.image3d.ImageHandler;
import mcib3d.image3d.ImageInt;
import mcib3d.image3d.distanceMap3d.EDT;
import tango.spatialStatistics.util.CellPopulation;
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
public class RandomPoint3DGeneratorDistanceMap extends RandomPoint3DGenerator {
    //private Distance[] distanceArray;
    ImageFloat distanceMap;
    private float[] distanceConstraint;
    CellPopulation cp;
    public RandomPoint3DGeneratorDistanceMap(ImageInt mask, int maxSize, ImageFloat distanceMap, float[] distances, int nbCPUs, boolean verbose) {
        super(mask, maxSize, nbCPUs, verbose);
        distanceConstraint=distances;
        setSegmentedMap(distanceMap);
    }
    
    protected void setSegmentedMap(ImageFloat distanceMap) {
        this.distanceMap=distanceMap;
        cp = new CellPopulation(10, mask, distanceMap, resXY, randomGenerator);
        pointIndex=0;
    }
    
    @Override
    public Point3D drawPoint3D() {
        float distance = distanceConstraint[pointIndex];
        Point3D point=cp.drawPoint(distance);
        return toFloat(point, distance);
    }
    
    private Point3D toFloat(Point3D point, float distance) { // ajuste au plus proche de la distance
        // choix des voisins:nt
        //x
        int x = (int) point.getX();
        int y = (int) point.getY();
        int xy=x+y*distanceMap.sizeX;
        int z = (int) point.getZ();
        int sX=0;
        int sY=0;
        int sZ=0;
        float curDist = distanceMap.pixels[z][xy];
        if (x>0) {
            if ((distance>curDist && distanceMap.pixels[z][xy-1]>curDist) || (distance<curDist && distanceMap.pixels[z][xy-1]<curDist)) sX=-1;
        }
        if (x<distanceMap.sizeX-1) {
            if ((distance>curDist && distanceMap.pixels[z][xy+1]>curDist) || (distance<curDist && distanceMap.pixels[z][xy+1]<curDist)) {
                if (sX==-1 && randomGenerator.nextBoolean() || sX==0) sX=1; 
            }
        }
        if (y>0) {
            if ((distance>curDist && distanceMap.pixels[z][xy-distanceMap.sizeX]>curDist) || (distance<curDist && distanceMap.pixels[z][xy-distanceMap.sizeX]<curDist)) sY=-1;
        }
        if (y<distanceMap.sizeY-1) {
            if ((distance>curDist && distanceMap.pixels[z][xy+distanceMap.sizeX]>curDist) || (distance<curDist && distanceMap.pixels[z][xy+distanceMap.sizeX]<curDist)) {
                if (sY==-1 && randomGenerator.nextBoolean() || sY==0) sY=1; 
            }
        }
        if (z>0) {
            if ((distance>curDist && distanceMap.pixels[z-1][xy]>curDist) || (distance<curDist && distanceMap.pixels[z-1][xy]<curDist)) sZ=-1;
        }
        if (z<distanceMap.sizeZ-1) {
            if ((distance>curDist && distanceMap.pixels[z+1][xy]>curDist) || (distance<curDist && distanceMap.pixels[z+1][xy]<curDist)) {
                if (sZ==-1 && randomGenerator.nextBoolean() || sZ==0) sZ=1; 
            }
        }
        float dX, dY, dZ;
        if (sX!=0) dX=Math.min((distance-curDist)/(distanceMap.pixels[z][xy+sX]-curDist), 1);
        else dX=0;
        if (sY!=0) dY=Math.min((distance-curDist)/(distanceMap.pixels[z][xy+sY*distanceMap.sizeX]-curDist), 1);
        else dY=0;
        if (sZ!=0) dZ=Math.min((distance-curDist)/(distanceMap.pixels[z+sZ][xy]-curDist), 1);
        else dZ=0;
        double theta=randomGenerator.nextFloat()*Math.PI/2;
        double phi=randomGenerator.nextFloat()*Math.PI/2;
        
        return new Point3D(x+((sX!=0)?Math.cos(theta)*Math.sin(phi) *dX*sX:0), y+((sY!=0)?Math.sin(theta)*Math.sin(phi)*dY*sY:0), z+((sZ!=0)?Math.cos(phi)*dZ*sZ:0));
        //ij.IJ.log("distance:"+distance+ " curDist:"+curDist + " new dist: "+distanceMap.getPixel((float)p.getX(),(float)p.getY(), (float)p.getZ()));
    }
    

    @Override
    public boolean isValid() {
        return nbPoints<=this.distanceConstraint.length;
    }
    
}
