package tango.plugin.measurement.distance;

import java.util.HashMap;
import mcib3d.geom.Object3D;
import tango.dataStructure.InputCellImages;
import tango.dataStructure.SegmentedCellImages;
import tango.gui.Core;
import tango.parameter.BooleanParameter;
import tango.parameter.ChoiceParameter;
import tango.parameter.ConditionalParameter;
import tango.parameter.Parameter;
import tango.processing.geodesicDistanceMap.GeodesicMap;
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
public class GeodesicDistance extends Distance {
    boolean verbose;
    int nbCPUs=1;
    GeodesicMap map = new GeodesicMap();
    public static String[] type = new String[] {"Center-Center", "Border-Border", "Center-Border", "Border-Center"};
    ChoiceParameter type_P = new ChoiceParameter("Distance: ", "type", type, type[0]);
    /*BooleanParameter center = new BooleanParameter("Center of Objects", "center", true);
    BooleanParameter contour = new BooleanParameter("Use only Contour of Objects", "center", false);
    HashMap<Object, Parameter[]> m = new HashMap<Object, Parameter[]>(){{
        put(false, new Parameter[]{contour}); 
    }};
    ConditionalParameter cond = new ConditionalParameter(center, m);
    * 
    */
    boolean centerObject1, contourObject1, centerObject2, contourObject2;
    
    @Override
    public void setMultithread(int nbCPUs) {
        this.nbCPUs=nbCPUs;
    }
    
    @Override
    public void setVerbose(boolean verbose) {
        this.verbose=verbose;
    }
    
    @Override
    public void initialize(InputCellImages in, SegmentedCellImages out) {
        map.init(in, nbCPUs, verbose);
        if (type_P.getSelectedIndex()==0) {
            centerObject1 = true;
            centerObject2 = true;
            contourObject1 = false;
            contourObject2 = false;
        } else if (type_P.getSelectedIndex()==1) {
            centerObject1 = false;
            centerObject2 = false;
            contourObject1 = true;
            contourObject2 = true;
        } else if (type_P.getSelectedIndex()==2) {
            centerObject1 = true;
            centerObject2 = false;
            contourObject1 = false;
            contourObject2 = true;
        } else if (type_P.getSelectedIndex()==3) {
            centerObject1 = false;
            centerObject2 = true;
            contourObject1 = true;
            contourObject2 = false;
        }
        //centerObject=center.isSelected();
        //contourObject=contour.isSelected();
    }

    @Override
    public double distance(Object3D p1, Object3D p2) {
        map.run(new Object3D[]{p1}, centerObject1, contourObject1);
        return getDistance2(p2);
    }
    
    private double getDistance2(Object3D o2) {
        if (!centerObject2) return map.getMinDistance(o2, contourObject2);
        else return map.getDistance(o2.getCenterAsPoint());
    }
    
    private double getDistance1(Object3D o1) {
        if (!centerObject1) return map.getMinDistance(o1, contourObject1);
        else return map.getDistance(o1.getCenterAsPoint());
    }
    
    private int getLabel(Object3D o1) {
        return map.getLabel(o1.getCenterAsPoint());
    }

    @Override
    public Parameter[] getParameters() {
        Parameter[] mapP = map.getParameters();
        Parameter[] p = new Parameter[mapP.length+1];
        System.arraycopy(mapP, 0, p, 0, mapP.length);
        p[mapP.length]=type_P;
        return p;
    }
    
    @Override
    public double[] getAllInterDistances(Object3D[] objects) {
        if (objects==null) return null;
        double[] res = new double[objects.length*(objects.length-1)];
        if (objects.length<=1) return res; 
        int idx=0;
        for (int i = 0; i<(objects.length-1); i++) {
            map.run(new Object3D[]{objects[i]}, centerObject1, contourObject1);
            for (int j = i+1; j<objects.length;j++) {
                res[idx++]=getDistance2(objects[j]);
            }
        }
        if (Core.debug) {
            map.getIntensityMap().showDuplicate("Intensity map for Geodesic distance map");
            map.getDistanceMap().showDuplicate("Geodesic distance map");
        }
        return res;
    }
    
    @Override
    public double[] getAllInterDistances(Object3D[] objects1, Object3D[] objects2) {
        //System.out.println("All interDistances geodesic : o1:"+objects1.length+ " o2:"+objects2.length);
        if (objects1==null || objects2==null) return null;
        double[] res = new double[objects1.length*objects2.length];
        if (objects1.length<=objects2.length) {
            int idx=0;
            for (int i = 0; i<objects1.length; i++) {
                map.run(new Object3D[]{objects1[i]}, centerObject1, contourObject1);
                for (int j = 0; j<objects2.length;j++) {
                    res[idx++]=getDistance2(objects2[j]);
                }
            }
        } else {
            for (int i = 0; i<objects2.length; i++) {
                map.run(new Object3D[]{objects2[i]}, centerObject2, contourObject2);
                for (int j = 0; j<objects1.length; j++) {
                    res[j*objects2.length+i]=getDistance1(objects1[j]);
                }
            }
        }
        if (Core.debug) {
            map.getIntensityMap().showDuplicate("Intensity map for Geodesic distance map");
            map.getDistanceMap().showDuplicate("Geodesic distance map");
        }
        return res;
    }
    
    
    @Override
    public double[] getNearestNeighborDistances(Object3D[] objects, int[] indexes) {
        if (objects==null) return null;
        double[] res = new double[objects.length];
        if (objects.length<=1) {
            if (indexes!=null) indexes[0]=1;
            return res;
        } 
        map.run(objects, centerObject1, contourObject1);
        for (int i = 0; i<objects.length; i++) {
            map.removeSeedAndRun(i);
            res[i]=getDistance2(objects[i]);
            if (indexes!=null) indexes[i] = getLabel(objects[i]);
        }
        if (Core.debug) {
            map.getIntensityMap().showDuplicate("Intensity map for Geodesic distance map");
            map.getDistanceMap().showDuplicate("Geodesic distance map");
        }
        return res;
    }
    
    @Override
    public double[] getNearestNeighborDistances(Object3D[] objects1, Object3D[] objects2, int[] indexes) {
        if (objects1==null || objects2==null) return null;
        if (objects1==objects2) return getNearestNeighborDistances(objects1, indexes);
        map.run(objects2, centerObject1, contourObject1);
        double[] res = new double[objects1.length];
        for (int i = 0; i<objects1.length; i++) {
            res[i]=getDistance2(objects1[i]);
            if (indexes!=null) indexes[i] = getLabel(objects1[i]);
        }
        if (Core.debug) map.getDistanceMap().showDuplicate("Geodesic distance map");
        return res;
    }

    @Override
    public String getHelp() {
        return "Geodesic Distance between centers of two objects";
    }
    
}
