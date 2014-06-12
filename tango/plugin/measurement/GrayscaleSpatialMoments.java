package tango.plugin.measurement;

import ij.ImagePlus;
import ij.WindowManager;
import ij.gui.GenericDialog;
import ij.measure.ResultsTable;
import ij.plugin.filter.PlugInFilter;
import ij.process.ImageProcessor;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import mcib3d.geom.Object3D;
import mcib3d.image3d.ImageByte;
import mcib3d.image3d.ImageHandler;
import mcib3d.image3d.ImageInt;
import mcib_plugins.analysis.simpleMeasure;
import tango.dataStructure.InputCellImages;
import tango.dataStructure.ObjectQuantifications;
import tango.dataStructure.SegmentedCellImages;
import tango.parameter.*;
import tango.plugin.measurement.Measurement;
import tango.plugin.measurement.MeasurementObject;

/*
 * To change this template, choose Tools | Templates and open the template in
 * the editor.
 */
/**
 *
 **
 * /**
 * Copyright (C) 2008- 2012 Jean Ollion
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
 * @author jean ollion
 */
public class GrayscaleSpatialMoments implements MeasurementObject {

    StructureParameter structureObjects = new StructureParameter("Structure objects:", "structure", -1, true);
    StructureParameter structureSignal = new StructureParameter("Structure signal:", "structureSignal", -1, false);
    BooleanParameter outside = new BooleanParameter("Outside Structure", "outside", false);
    PreFilterSequenceParameter preFilters = new PreFilterSequenceParameter("Pre-Filters", "preFilters");
    Parameter[] parameters = new Parameter[]{structureObjects, structureSignal, outside, preFilters};
    KeyParameterObjectNumber m0 = new KeyParameterObjectNumber("Total Signal:", "totalSignal", "totalSignal", true);
    KeyParameterObjectNumber m2 = new KeyParameterObjectNumber("Variance", "variance", "variance", true);
    KeyParameterObjectNumber m2_x = new KeyParameterObjectNumber("Variance (X)", "varianceX", "varianceX", false);
    KeyParameterObjectNumber m2_y = new KeyParameterObjectNumber("Variance (Y)", "varianceY", "varianceY", false);
    KeyParameterObjectNumber m2_z = new KeyParameterObjectNumber("Variance (Z)", "varianceZ", "varianceZ", false);
    KeyParameterObjectNumber m3 = new KeyParameterObjectNumber("Skewness (symmetry)", "skewness", "skewness", true);
    KeyParameterObjectNumber m3_x = new KeyParameterObjectNumber("Skewness (symmetry) (X)", "skewnessX", "skewnessX", false);
    KeyParameterObjectNumber m3_y = new KeyParameterObjectNumber("Skewness (symmetry) (Y)", "skewnessY", "skewnessY", false);
    KeyParameterObjectNumber m3_z = new KeyParameterObjectNumber("Skewness (symmetry) (Z)", "skewnessZ", "skewnessZ", false);
    KeyParameterObjectNumber m4 = new KeyParameterObjectNumber("Kurtosis (flatness)", "kurtosis", "kurtosis", true);
    KeyParameterObjectNumber m4_x = new KeyParameterObjectNumber("Kurtosis (flatness) (X)", "kurtosisX", "kurtosisX", false);
    KeyParameterObjectNumber m4_y = new KeyParameterObjectNumber("Kurtosis (flatness) (Y)", "kurtosisY", "kurtosisY", false);
    KeyParameterObjectNumber m4_z = new KeyParameterObjectNumber("Kurtosis (flatness) (Z)", "kurtosisZ", "kurtosisZ", false);
    KeyParameter[] keys = new KeyParameter[]{m0, m2, m2_x, m2_y, m2_z, m3, m3_x, m3_y, m3_z, m4, m4_x, m4_y, m4_z};
    GroupKeyParameter group = new GroupKeyParameter("", "spatialMoments", "", true, keys, false);
    
    int nCPUs=1;
    boolean verbose=false;
    double[] variance, skewness, kurtosis;
    double sum;
    @Override
    public void setMultithread(int nbCPUs) {
        this.nCPUs=nbCPUs;
    }
    
    @Override
    public void setVerbose(boolean verbose) {
        this.verbose=verbose;
    }

    public void computeMoments(ImageHandler intensityMap, ImageInt mask) {
        double scaleXY=mask.getScaleXY();
        double scaleZ=mask.getScaleZ();
        // get center of mass
        sum=0;
        double cx=0, cy=0, cz=0, value;
        for (int z =0; z<mask.sizeZ; z++) {
            for (int y=0; y<mask.sizeY; y++) {
                for (int x = 0; x<mask.sizeX; x++) {
                    int xy = x+y*mask.sizeX;
                    if (mask.getPixel(xy, z)!=0) {
                        value = intensityMap.getPixel(xy, z);
                        cx+=value*(x+0.5);
                        cy+=value*(y+0.5);
                        cz+=value*(z+0.5);
                        sum+=value;
                    }
                }
            }
        }
        if (sum!=0) {
            cx/=sum;
            cy/=sum;
            cz/=sum;
        }
        // get moments
        variance = new double[3];
        skewness = new double[3];
        kurtosis = new double[3];
        double dx, dy, dz;
        for (int z =0; z<mask.sizeZ; z++) {
            for (int y=0; y<mask.sizeY; y++) {
                for (int x = 0; x<mask.sizeX; x++) {
                    int xy = x+y*mask.sizeX;
                    if (mask.getPixel(xy, z)!=0) {
                        value = intensityMap.getPixel(xy, z);
                        dx = (x+0.5 - cx) * scaleXY;
                        dy = (y+0.5 - cy) * scaleXY;
                        dz = (z+0.5 - cz) * scaleZ;
                        //x
                        variance[0]+=value * dx * dx ;
                        skewness[0]+=value * Math.pow(dx, 3) ;
                        kurtosis[0]+=value * Math.pow(dx, 4);
                        //y
                        variance[1]+=value * dy * dy ;
                        skewness[1]+=value * Math.pow(dy, 3) ;
                        kurtosis[1]+=value * Math.pow(dy, 4) ;
                        //z
                        variance[2]+=value * dz * dz ;
                        skewness[2]+=value * Math.pow(dz, 3) ;
                        kurtosis[2]+=value * Math.pow(dz, 4) ;
                    }
                }
            }
        }
        if (sum!=0) {    
            // normalize variance
            for (int i = 0; i<3; i++) {
                variance[i]/=sum;
            }
            // normalize skewness
            // source: Farrell et al, 1994, Water Resources Research, 30(11):3213-3223
            for (int i = 0; i<3; i++) {
                skewness[i]/=( sum * Math.pow(variance[i], 3.0/2.0));
            }
            // normalize kurtosis
            // source: Farrell et al, 1994, Water Resources Research, 30(11):3213-3223
            for (int i = 0; i<3; i++) {
                kurtosis[i]=kurtosis[i] / ( sum * Math.pow(variance[i], 2.0)) - 3.0;
            }
        }
    }
    
    public double[] getVariance() {
        return variance;
    } 
    
    public double[] getKurtosis() {
        return kurtosis;
    }
    
    public double[] getSkewness() {
        return skewness;
    }
    
    public double getSum() {
        return sum;
    }
    
    @Override
    public void getMeasure(InputCellImages raw, SegmentedCellImages seg, ObjectQuantifications quantifications) {
        ImageInt mask;
        if (outside.isSelected() && structureObjects.getIndex()>0) {
            ImageInt structure = seg.getImage(structureObjects.getIndex());
            mask = structure.invertMask(raw.getMask());
        } else mask = seg.getImage(structureObjects.getIndex());
        ImageHandler intensityMap = raw.getImage(structureSignal.getIndex());
        intensityMap=preFilters.runPreFilterSequence(structureSignal.getIndex(), intensityMap, raw, nCPUs, verbose);
        computeMoments(intensityMap, mask);
        
        if (m0.isSelected()) {
            quantifications.setQuantificationObjectNumber(m0, new double[]{sum});
        } 
        if (m2.isSelected()) {
            quantifications.setQuantificationObjectNumber(m2, new double[]{variance[0]+variance[1]+variance[2]});
        }
        if (m2_x.isSelected()) {
            quantifications.setQuantificationObjectNumber(m2_x, new double[]{variance[0]});
        }
        if (m2_y.isSelected()) {
            quantifications.setQuantificationObjectNumber(m2_y, new double[]{variance[1]});
        }
        if (m2_z.isSelected()) {
            quantifications.setQuantificationObjectNumber(m2_z, new double[]{variance[2]});
        }
        if (m3.isSelected()) {
            quantifications.setQuantificationObjectNumber(m3, new double[]{skewness[0]+skewness[1]+skewness[2]});
        }
        if (m3_x.isSelected()) {
            quantifications.setQuantificationObjectNumber(m3_x, new double[]{skewness[0]});
        }
        if (m3_y.isSelected()) {
            quantifications.setQuantificationObjectNumber(m3_y, new double[]{skewness[1]});
        }
        if (m3_z.isSelected()) {
            quantifications.setQuantificationObjectNumber(m3_z, new double[]{skewness[2]});
        }
        if (m4.isSelected()) {
            quantifications.setQuantificationObjectNumber(m4, new double[]{kurtosis[0]+kurtosis[1]+kurtosis[2]});
        }
        if (m4_x.isSelected()) {
            quantifications.setQuantificationObjectNumber(m4_x, new double[]{kurtosis[0]});
        }
        if (m4_y.isSelected()) {
            quantifications.setQuantificationObjectNumber(m4_y, new double[]{kurtosis[1]});
        }
        if (m4_z.isSelected()) {
            quantifications.setQuantificationObjectNumber(m4_z, new double[]{kurtosis[2]});
        }
    }

    @Override
    public Parameter[] getParameters() {
        return parameters;
    }

    @Override
    public Parameter[] getKeys() {
        return new Parameter[] {group};
    }

    @Override
    public int getStructure() {
        return 0;
    }

    @Override
    public String getHelp() {
        return "Intensity Quantification inside or outside objects";
    }
}
