package tango.spatialStatistics.StochasticProcess;

import ij.IJ;
import mcib3d.image3d.ImageHandler;
import java.util.Random;
import java.util.TreeMap;
import mcib3d.geom.Object3D;
import mcib3d.geom.Object3DPoint;
import mcib3d.geom.Object3DVoxels;
import mcib3d.geom.Point3D;
import mcib3d.image3d.*;
import tango.dataStructure.AbstractStructure;
import tango.dataStructure.Cell;
import tango.spatialStatistics.util.KDTreeC;
import tango.util.Cell3DViewer;
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
public abstract class RandomPoint3DGenerator {
    
    ImageInt mask;
    public Random randomGenerator;
    public int nbPoints;
    boolean hardcore;
    public KDTreeC kdTree; // null if hardcore not set
    double hardcoreDistance, scale, hardcoreDistanceSq;
    public double resXY, resZ;
    public int[] maskCoordsZ;
    public int[] maskCoordsXY;
    public int pointIndex;
    public Point3D[] points;
    public boolean verbose;
    int nCPUs=1;
    public RandomPoint3DGenerator(ImageInt mask, int maxSize, int nCPUs, boolean verbose) {
        this.randomGenerator=new Random();
        this.mask=mask;
        pointIndex=0;
        this.nbPoints=maxSize;
        this.points=new Point3D[maxSize];
        setMask(mask);
        this.verbose=verbose;
        this.nCPUs=nCPUs;
    }
    
    public void setHardCore(double hardcoreDistance) {
        this.hardcore=true;
        kdTree = new KDTreeC(3, nbPoints);
        kdTree.setScaleSq(new double[] {resXY*resXY, resXY*resXY, resZ*resZ});
        this.hardcoreDistance=hardcoreDistance;
        this.hardcoreDistanceSq=hardcoreDistance*hardcoreDistance;
    }
    
    protected void setMask(ImageInt mask) {
        this.mask=mask;
        this.resXY=mask.getScaleXY();
        this.resZ=mask.getScaleZ();
        this.scale = resZ/resXY;
        int count = 0;
        for (int z = 0; z<mask.sizeZ; z++) {
            for (int xy = 0; xy<mask.sizeXY; xy++) {
                if (mask.getPixel(xy, z)!=0) count++;
            }
        }
        maskCoordsZ=new int[count];
        maskCoordsXY=new int[count]; // FIXME heap space
        count=0;
        for (int z = 0; z<mask.sizeZ; z++) {
            for (int xy = 0; xy<mask.sizeXY; xy++) {
                if (mask.getPixel(xy, z)!=0) {
                    maskCoordsZ[count]=z;
                    maskCoordsXY[count]=xy;
                    count++;
                }
            }
        }
    }
    
    public int[] getCoordsXY() {
        return this.maskCoordsXY;
    }
    
    public int[] getCoordsZ() {
        return this.maskCoordsZ;
    }
    
    public boolean isHardcore() {
        return hardcore;
    }
    
    protected Point3D drawPoint() {
        Point3D point = drawPoint3D();
        if (point==null) return null;
        if (hardcore) {
            if (testHardcore(point)) {
                kdTree.add(new double[] {point.getX(), point.getY(), point.getZ()}, point);
                points[pointIndex]=point;
                pointIndex++;
                return point;
            }
            else return null;
        } else {
            points[pointIndex]=point;
            pointIndex++;
            return point;
        }
    }
    
    protected abstract Point3D drawPoint3D();
    
    protected Point3D drawPoint3DUniform() {
        int key = randomGenerator.nextInt(this.maskCoordsXY.length);
        return new Point3D(this.maskCoordsXY[key]%mask.sizeX+randomGenerator.nextFloat()-0.5, this.maskCoordsXY[key]/mask.sizeX+randomGenerator.nextFloat()-0.5, this.maskCoordsZ[key]+randomGenerator.nextFloat()-0.5);
    }
    
    public abstract boolean isValid();
    
    public Point3D[] drawPoints(int maxIterationsPoint, int maxIterationsSample) {
        if (!isValid()) return null;
        int iteration=0;
        while (this.pointIndex<nbPoints) {
            int iter = 1;
            Point3D p = drawPoint();
            while(p==null && iter<maxIterationsPoint) {
                p=drawPoint();
                iter++;
            }
            if (p==null && iter==maxIterationsPoint) {
                iteration++;
                if (iteration>maxIterationsSample) return null;
                //System.out.println("reset points: iter: "+iteration);
                resetPoints();
            }
        }
        return points;
    }
    
    public Object3DPoint[] drawObjects(int maxIterationsPoint, int maxIterationsSample) {
        Object3DPoint[] objects = new Object3DPoint[nbPoints];
        Point3D[] pts = drawPoints(maxIterationsPoint, maxIterationsSample);
        if (pts==null) return null;
        for (int i = 0; i<nbPoints; i++) {
            if (pts[i]==null) return null;
            objects[i]=new Object3DPoint(i+1, pts[i]);
            objects[i].setResXY(resXY);
            objects[i].setResZ(resZ);
        }
        return objects;
    }
    
    public ImageHandler showPoints(String title, Point3D[] points) {
        ImageShort im = new ImageShort(title, mask.sizeX, mask.sizeY, mask.sizeZ);
        im.setScale(mask);
        if (points == null ) return im;
        for (int i = 0;i<points.length; i++) im.setPixel(points[i].getRoundX(), points[i].getRoundY(), points[i].getRoundZ(), i+1);
        im.setMinAndMax(0, points.length);
        return im;
    }
    
    public ImageHandler showPoints(String title, Object3D[] points) {
        ImageShort im = new ImageShort(title, mask.sizeX, mask.sizeY, mask.sizeZ);
        im.setScale(mask);
        if (points == null ) return im;
        for (int i = 0;i<points.length; i++) {
            Point3D center = points[i].getCenterAsPoint();
            im.setPixel(center.getRoundX(), center.getRoundY(), center.getRoundZ(), i+1);
        }
        im.setMinAndMax(0, points.length);
        return im;
    }
    
    public void showPoint3D(AbstractStructure s, float radiusXY, float radiusZ) {
        Cell cell = s.getCell();
        if (points == null ) return;
        ImageByte im = new ImageByte("random", mask.sizeX, mask.sizeY, mask.sizeZ);
        im.setScale(mask);
        Object3DVoxels o = new Object3DVoxels();
        o.createEllipsoidPixel(0, 0, 0, radiusXY, radiusXY, radiusZ);
        for (int i = 0;i<points.length; i++) {
            if (points[i]!=null) {
                //im.setPixel(points[i].getRoundX(), points[i].getRoundY(), points[i].getRoundZ(), 255);
                o.translate(points[i].getX(), points[i].getY(), points[i].getZ());
                o.draw(im.getImageStack(), 255);
                o.translate(-points[i].getX(), -points[i].getY(), -points[i].getZ());
            }
        }
        
        
        cell.getExperiment().c3Dv.resetAndAddNucleus(cell);
        cell.getExperiment().c3Dv.addContent(im, s.getChannelName()+"::sampled", Cell3DViewer.colors3f.get(s.getColorName()), s.getIJ3DViwerParameter());
    }
    
    public void resetPoints() {
        if (hardcore) {
            this.kdTree=new KDTreeC(3, this.nbPoints);
            kdTree.setScaleSq(new double[] {resXY*resXY, resXY*resXY, resZ*resZ});
        }
        pointIndex=0;
        points=new Point3D[nbPoints];
    }
    
    protected boolean testHardcore(Point3D point) { 
        if (point==null) return false;
        //KDTreeC.Item[] items = kdTree.getRange(new double[]{point.getX()-hardcoreDistance, point.getY()-hardcoreDistance, point.getZ()-hardcoreDistanceZ}, new double[]{point.getX()+hardcoreDistance, point.getY()+hardcoreDistance, point.getZ()+hardcoreDistanceZ});
        KDTreeC.Item[] items = kdTree.getNearestNeighbor(new double[]{point.getX(), point.getY(), point.getZ()}, 1);
        if (items[0]==null) return true;
        else {
            if (items[0].distance<hardcoreDistanceSq) return false;
        }
        return true;
    }
}
