package tango.plugin.measurement.distance;

import ij.gui.Plot;
import java.util.ArrayList;
import mcib3d.geom.Object3D;
import mcib3d.geom.Point3D;
import mcib3d.geom.Vector3D;
import mcib3d.image3d.ImageHandler;
import mcib3d.image3d.ImageInt;
import tango.dataStructure.InputCellImages;
import tango.dataStructure.SegmentedCellImages;
import tango.gui.Core;
import tango.parameter.*;

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
public class IntensityProfile3D extends Distance {
    ImageHandler intensityMap;
    ImageInt mask;
    double alphaRad, globalMean;
    double resXY, resZ;
    boolean verbose;
    int nbCPUs=1;
    StructureParameter intensityMap_P = new StructureParameter("Intensity Map: ", "intensityMap", -1, false);
    BooleanParameter filtered = new BooleanParameter("Use filtered Image: ", "filtered", false);
    SliderDoubleParameter alpha= new SliderDoubleParameter("Alpha (°):", "alpha", 0, 45, 0, 1);
    static String[] stat=new String[] {"Global Mean","Mean( Mean(step) )", "Min( Mean(step) )"};
    ChoiceParameter stat_P = new ChoiceParameter("Compute: ", "stat", stat, stat[0]);
    Parameter[] parameters=new Parameter[]{intensityMap_P, filtered, alpha, stat_P};
    boolean debug=false;
    public IntensityProfile3D() {
        intensityMap_P.setHelp("Channel Image to compute intensity mean value", true);
        alpha.setHelp("Angle of cones that define the volume to compute mean value. This volume is composed of two cones starting from each point Choose 0 for a classic profile. ", true);
    }
    
    @Override
    public Parameter[] getParameters() {
        return parameters;
    }
    
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
        if (!filtered.isSelected()) this.intensityMap=in.getImage(intensityMap_P.getIndex());
        else this.intensityMap=in.getFilteredImage(intensityMap_P.getIndex());
        this.mask=in.getMask();
        this.alphaRad=(alpha.getValue()/360d)*2*Math.PI;
        resXY=intensityMap.getScaleXY();
        globalMean=intensityMap.getImageStats(mask).getMean();
        if (Core.debug) ij.IJ.log("IntensityProfile: global mean:"+globalMean);
    }
    
    @Override
    public double distance(Object3D o1, Object3D o2) {
        Point3D p1 = o1.getCenterAsPoint();
        Point3D p2 = o2.getCenterAsPoint();
        double distance = p1.distance(p2, resXY, resZ);
        int n = (int)(distance/resXY+0.5);
        double step = distance/((double)n*resXY);
        int limit=n/2;
        ArrayList<ArrayList<Float>> values = new ArrayList<ArrayList<Float>>();
        if (n<=1) {
            ArrayList<Float> al1 = new ArrayList<Float>(1);
            ArrayList<Float> al2 = new ArrayList<Float>(1);
            values.add(al1);
            values.add(al2);
            al1.add(intensityMap.getPixel((float)p1.getX(), (float)p1.getY(), (float)p1.getZ()));
            al2.add(intensityMap.getPixel((float)p2.getX(), (float)p2.getY(), (float)p2.getZ()));
        } else {
            Vector3D normZ = new Vector3D(p1, p2);
            normZ.normalize();
            Vector3D normX=getOrtho(normZ);
            Vector3D normY=normZ.crossProduct(normX); //pas besoin de normaliser car Z et X ortho et normalises
            Vector3D center=p1.getVector3D();
            //System.out.println("meanIntensity: p1:"+p1+ " p2:"+p2);
            //System.out.println("Z:"+normZ+ " X:"+normX+ " Y:"+normY);
            //System.out.println("distance:"+ distance+ " res:"+ resXY+ " step:"+step+ " n:"+n);
            for (int i = 0; i<=n; i++) {
                double radius = (i<=limit)?alphaRad*i*step:alphaRad*(n-i)*step;
                int rad=(int)(radius+0.5);
                double distSq=radius*radius;
                Vector3D point;
                ArrayList<Float> current = new ArrayList<Float>();
                values.add(current);
                for (int x = -rad; x<=rad; x++) {
                    for (int y=-rad;y<=rad; y++) {
                        if ((x*x+y*y)<=distSq) {
                            point=center.getVector3D();
                            point.addMe(normX, x);
                            point.addMe(normY, y);
                            if (mask.maskContains(point.getRoundX(), point.getRoundY(), point.getRoundZ())) {
                                current.add(intensityMap.getPixel((float)point.getX(), (float)point.getY(), (float)point.getZ()));
                            }
                        }
                    }
                }
                //System.out.println("i:"+i+ " radius:"+radius+ "  center:"+center+ " nbPix:"+values.size());
                center.addMe(normZ, step);
            }
        }
        //debug=true;
        if (Core.debug) {
            double[] y = new double[values.size()];
            double[] x = new double[values.size()];
            int count=0;
            for (ArrayList<Float> alf : values) {
                double m = 0;
                for (Float f : alf) m+=f;
                if (!alf.isEmpty()) m/=alf.size();
                y[count]=m;
                x[count]=count*step;
                count++;
            }
            Plot p = new Plot("3D Intensity profile: ", "dist", "mean intensity", x, y, 1);
            p.show();
        }
        if (this.stat_P.getSelectedIndex()==1) { // mean ( mean / step))
            double mean = 0;
            for (ArrayList<Float> alf : values) {
                double m = 0;
                for (Float f : alf) m+=f;
                if (!alf.isEmpty()) m/=alf.size();
                mean+=m;
            }
            if (!values.isEmpty()) mean/=values.size();
            return mean/globalMean;
        } else if (this.stat_P.getSelectedIndex()==2) { // min mean per step
            double min = Double.MAX_VALUE;
            for (ArrayList<Float> alf : values) {
                double m = 0;
                for (Float f : alf) m+=f;
                if (!alf.isEmpty()) m/=alf.size();
                if (m<min) min = m;
            }
            return min/globalMean;
        } else { // global mean
            double mean=0;
            int count = 0;
            for (ArrayList<Float> alf : values) {
                for (Float f : alf) mean+=f;
                count+=alf.size();
            }
            if (count!=0) mean/=count;
            //System.out.println("mean:"+mean+ " normalized:"+(mean/globalMean));
            return mean/globalMean;
        }
    }
    /*
    protected void addValues(double radius, Point3D center, Vector3D xDir, Vector3D yDir, ArrayList<Float> values) {
        int rad=(int)(radius+0.5);
        double distSq=radius*radius;
        Vector3D point;
        for (int x = -rad; x<=rad; x++) {
            for (int y=-rad;y<=rad; y++) {
                if ((x*x+y*y)<=distSq) {
                    point=center.getVector3D();
                    point.addMe(xDir, x);
                    point.addMe(yDir, y);
                    if (mask.maskContains(point.getRoundX(), point.getRoundY(), point.getRoundZ())) {
                        values.add(intensityMap.getPixel((float)point.getX(), (float)point.getY(), (float)point.getZ()));
                    }
                }
            }
        }
    }
    * 
    */
    
    protected static Vector3D getOrtho(Vector3D n) {
        double[] coords= n.getArray();
        double[] coordsAbs= new double[]{Math.abs(coords[0]), Math.abs(coords[1]), Math.abs(coords[2])};
        double[] coords2 = new double[3];
        int max;
        int mid;
        int min;
        if (coordsAbs[0]>=coordsAbs[1] && coordsAbs[0]>=coordsAbs[2]) {
            max=0;
            if (coordsAbs[1]>=coordsAbs[2]) {
                mid=1;
                min=2;
            } else {
                mid=2;
                min=1;
            }
        } else if (coordsAbs[1]>=coordsAbs[0] && coordsAbs[1]>=coordsAbs[2]) {
            max=1;
            if (coordsAbs[0]>=coordsAbs[2]) {
                mid=0;
                min=2;
            } else {
                mid=2;
                min=0;
            }
        } else {
            max=2;
            if (coordsAbs[0]>=coordsAbs[1]) {
                mid=0;
                min=1;
            } else {
                mid=1;
                min=0;
            }
        }
        coords2[max]=coords[mid];
        coords2[mid]=-coords[max];
        Vector3D res = new Vector3D(coords2[0], coords2[1], coords2[2]);
        res.normalize();
        return res;
    }

    @Override
    public String getHelp() {
        return "Mean Intensity between centers of two objects";
    }
}
