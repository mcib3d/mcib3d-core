package tango.plugin.measurement.distance;

import mcib3d.geom.Object3D;
import tango.dataStructure.InputCellImages;
import tango.dataStructure.SegmentedCellImages;
import tango.gui.Core;
import tango.parameter.Parameter;
import tango.plugin.TangoPlugin;
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
public abstract class Distance implements TangoPlugin {
    public static String[] distances = new String[]{"Euclidean Distance"};
    public static String[] distancesTesting = new String[]{"Euclidean Distance", "EdgeContact", "Geodesic Distance", "Intensity Profile 3D"};
    String[] currentDistances;
    int nCPUs=1;
    boolean verbose;
    
    @Override
    public void setMultithread(int nbCPUs) {
        this.nCPUs=nbCPUs;
    }
    
    @Override
    public void setVerbose(boolean verbose) {
        this.verbose=verbose;
    }
    public static Distance getDistance(String name) {
        if (!Core.TESTING) {
            if (name.equals(distances[0])) return new EuclideanDistance();
            else return null;
        } else {
            if (name.equals(distancesTesting[0])) return new EuclideanDistance();
            if (name.equals(distancesTesting[1])) return new EdgeContact();
            else if (name.equals(distancesTesting[2])) return new GeodesicDistance();
            else if (name.equals(distancesTesting[3])) return new IntensityProfile3D();
            else return null;
        }
    }
    public abstract void initialize(InputCellImages in, SegmentedCellImages out);
    public abstract double distance(Object3D p1, Object3D p2);
    public double[] getAllInterDistances(Object3D[] objects) {
        if (objects==null) return null;
        double[] res = new double[objects.length*(objects.length-1)/2];
        int idx=0;
        for (int i = 0; i<objects.length-1; i++) {
            for (int j = i+1; j<objects.length; j++) {
                res[idx++]=distance(objects[i], objects[j]);
            }
        }
        return res;
    }
    public double[] getAllInterDistances(Object3D[] objects1, Object3D[] objects2) {
        //System.out.println("All interDistances regular : o1:"+objects1.length+ " o2:"+objects2.length);
        if (objects1==null || objects2==null) return null;
        double[] res = new double[objects1.length*objects2.length];
        int idx=0;
        for (int i = 0; i<objects1.length; i++) {
            for (int j = 0; j<objects2.length; j++) {
                res[idx++]=distance(objects1[i], objects2[j]);
            }
        }
        return res;
    }
    // convention: si un seul objet: distance minimal = 0
    public double[] getNearestNeighborDistances(Object3D[] objects, int[] indexes) {
        if (objects==null) return null;
        double[] res = new double[objects.length];
        if (res.length<=1) {
            if (indexes!=null) indexes[0]=1;
            return res;
        }
        double[] allDist=getAllInterDistances(objects);
        int offset=-1;
        int N = objects.length-1;
        for (int i = 0; i<objects.length; i++) {
            res[i]=Double.MAX_VALUE;
            //line
            for (int j = i+1; j<=N; j++) {
                if (allDist[offset+j]<res[i]) {
                    res[i]=allDist[offset+j];
                    if (indexes!=null) indexes[i]=j+1;
                }
            }
            offset+=N-i-1;
            //column
            int offset2=i-1;
            for (int j = 1; j<=i; j++) {
                if (allDist[offset2]<res[i]) {
                    res[i]=allDist[offset2];
                    if (indexes!=null) indexes[i]=j+1;
                }
                offset2+=N-j;
            }
            
        }
        return res;
    }
    // TODO KDTree en option
    public double[] getNearestNeighborDistances(Object3D[] objects1, Object3D[] objects2, int[] indexes) {
        if (objects1==null || objects2==null || objects1.length==0 || objects2.length==0) return null;
        if (objects1==objects2) return getNearestNeighborDistances(objects1, indexes);
        double[] res = new double[objects1.length];
        for (int i = 0; i<objects1.length; i++) {
            res[i]=distance(objects1[i], objects2[0]);
            for (int j = 1; j<objects2.length; j++) {
                double temp = distance(objects1[i], objects2[j]);
                if (temp<res[i]) {
                    res[i]=temp;
                    if (indexes!=null) indexes[i]=j;
                }
            }
        }
        return res;
    }
    
    public double getMinimalDistance(Object3D[] objects1, Object3D[] objects2, int[] indexes) {
        if (objects1==null || objects2==null || objects1.length==0 || objects2.length==0) return -1;
        int[] idx = null;
        if (indexes!=null) idx = new int[objects1.length];
        double[] dists = getNearestNeighborDistances(objects1, objects2, idx);
        if (dists==null || dists.length==0) return -Double.MAX_VALUE;
        double d =dists[0];
        if (idx==null) {
            for (int i = 1; i<dists.length; i++) if (dists[i]<d) d=dists[i];
        } else {
            int imin = 0;
            for (int i = 1; i<dists.length; i++) {
                if (dists[i]<d) {
                    d=dists[i];
                    imin=i;
                }
            }
            indexes[0]=imin;
            indexes[1]=idx[imin];
        }
        return d;
    }
    
    public double getMinimalDistance(Object3D[] objects, int[] indexes) {
        if (objects==null || objects.length==0) return -1;
        int[] idx = null;
        if (indexes!=null) idx = new int[objects.length];
        double[] dists = getNearestNeighborDistances(objects, idx);
        if (dists==null || dists.length==0) return -Double.MAX_VALUE;
        double d =dists[0];
        if (idx==null) {
            for (int i = 1; i<dists.length; i++) if (dists[i]<d) d=dists[i];
        } else {
            int imin = 0;
            for (int i = 1; i<dists.length; i++) {
                if (dists[i]<d) {
                    d=dists[i];
                    imin=i;
                }
            }
            indexes[0]=imin;
            indexes[1]=idx[imin];
        }
        return d;
    }
}
