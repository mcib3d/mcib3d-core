package tango.plugin.measurement;

import ij.IJ;
import ij.ImagePlus;
import ij.WindowManager;
import ij.gui.GenericDialog;
import ij.measure.ResultsTable;
import ij.plugin.filter.PlugInFilter;
import ij.process.ImageProcessor;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import mcib3d.geom.Object3D;
import mcib3d.image3d.ImageByte;
import mcib3d.image3d.ImageFloat;
import mcib3d.image3d.ImageHandler;
import mcib3d.image3d.ImageInt;
import mcib3d.image3d.distanceMap3d.EDT;
import mcib_plugins.analysis.simpleMeasure;
import tango.dataStructure.InputCellImages;
import tango.dataStructure.ObjectQuantifications;
import tango.dataStructure.SegmentedCellImages;
import tango.gui.Core;
import tango.parameter.*;
import tango.plugin.measurement.Measurement;
import tango.plugin.measurement.MeasurementObject;
import tango.plugin.measurement.radialAnalysis.NormalizeDistanceMap;

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
public class GrayscaleRadialMoments implements MeasurementObject {
    
    DistanceMapParameter dm = new DistanceMapParameter("Reference Structures", "dm", false, false);
    FilteredStructureParameter signal = new FilteredStructureParameter("Signal", "signal");
    
    Parameter[] parameters = new Parameter[]{dm, signal};
    KeyParameterObjectNumber m0 = new KeyParameterObjectNumber("Total Signal:", "totalSignal", "totalSignal", false);
    KeyParameterObjectNumber m1 = new KeyParameterObjectNumber("Mean", "mean", "mean", true);
    KeyParameterObjectNumber m2 = new KeyParameterObjectNumber("Variance", "variance", "variance", true);
    KeyParameterObjectNumber m3 = new KeyParameterObjectNumber("Skewness (symmetry)", "skewness", "skewness", true);
    KeyParameterObjectNumber m4 = new KeyParameterObjectNumber("Kurtosis (flatness)", "kurtosis", "kurtosis", true);
    KeyParameter[] keys = new KeyParameter[]{m0, m1, m2, m3, m4};
    GroupKeyParameter group = new GroupKeyParameter("", "radialMoments", "radialMoments_", true, keys, false);
    
    int nCPUs=1;
    boolean verbose=false;
    double mean, variance, skewness, kurtosis;
    double sum;
    @Override
    public void setMultithread(int nbCPUs) {
        this.nCPUs=nbCPUs;
    }
    
    @Override
    public void setVerbose(boolean verbose) {
        this.verbose=verbose;
    }

    public void computeMoments(ImageHandler intensityMap, ImageInt mask, ImageFloat distanceMap) {
        
        // get center of mass
        sum=0;
        mean=0;
        double value;
        for (int z =0; z<mask.sizeZ; z++) {
            for (int y=0; y<mask.sizeY; y++) {
                for (int x = 0; x<mask.sizeX; x++) {
                    int xy = x+y*mask.sizeX;
                    if (mask.getPixel(xy, z)!=0) {
                        value = intensityMap.getPixel(xy, z);
                        mean+=value*(distanceMap.getPixel(xy, z));
                        sum+=value;
                    }
                }
            }
        }
        if (sum!=0) mean/=sum;
        
        // get moments
        variance = 0;
        skewness = 0;
        kurtosis = 0;
        double d;
        for (int z =0; z<mask.sizeZ; z++) {
            for (int y=0; y<mask.sizeY; y++) {
                for (int x = 0; x<mask.sizeX; x++) {
                    int xy = x+y*mask.sizeX;
                    if (mask.getPixel(xy, z)!=0) {
                        value = intensityMap.getPixel(xy, z);
                        d = (distanceMap.getPixel(xy, z) - mean);
                        variance+=value * d * d ;
                        skewness+=value * Math.pow(d, 3) ;
                        kurtosis+=value * Math.pow(d, 4);
                    }
                }
            }
        }
        if (sum!=0) {    
            // normalize variance
            variance/=sum;
            // normalize skewness
            // source: Farrell et al, 1994, Water Resources Research, 30(11):3213-3223
            skewness/=( sum * Math.pow(variance, 3.0/2.0));
            // normalize kurtosis
            // source: Farrell et al, 1994, Water Resources Research, 30(11):3213-3223
            kurtosis=kurtosis / ( sum * Math.pow(variance, 2.0)) - 3.0;
            
        }
    }
    
    public double getMean() {
        return mean;
    } 
    
    public double getVariance() {
        return variance;
    } 
    
    public double getKurtosis() {
        return kurtosis;
    }
    
    public double getSkewness() {
        return skewness;
    }
    
    public double getSum() {
        return sum;
    }
    
    /*private int[] getMaskStructures() {
        int[] res = new int[this.referenceStructures.getNbParameters()];
        Parameter[] ps = referenceStructures.getParameters();
        for (int i = 0; i<res.length; i++) {
            res[i] = ((StructureParameter)ps[i]).getIndex();
        }
        Arrays.sort(res);
        return res;
    }*/
    
    
    @Override
    public void getMeasure(InputCellImages raw, SegmentedCellImages seg, ObjectQuantifications quantifications) {
        
        ImageHandler[] mdm = dm.getMaskAndDistanceMap(raw, seg, 0, verbose, nCPUs);
        ImageInt mask = (ImageInt)mdm[0];
        ImageFloat distanceMap = (ImageFloat)mdm[1];
        
        // intensity map
        ImageHandler intensityMap = signal.getImage(raw, verbose, nCPUs);
        
        computeMoments(intensityMap, mask, distanceMap);
        
        if (verbose) {
            IJ.log("GrayscaleRadialMoments. Total signal: "+sum+ " mean: "+mean+ " variance: "+variance+ " skewness: "+skewness+ " kurtosis: "+kurtosis);
        }
        
        if (m0.isSelected()) {
            quantifications.setQuantificationObjectNumber(m0, new double[]{sum});
        } 
        if (m1.isSelected()) {
            quantifications.setQuantificationObjectNumber(m1, new double[]{mean});
        }
        if (m2.isSelected()) {
            quantifications.setQuantificationObjectNumber(m2, new double[]{variance});
        }
        if (m3.isSelected()) {
            quantifications.setQuantificationObjectNumber(m3, new double[]{skewness});
        }
        if (m4.isSelected()) {
            quantifications.setQuantificationObjectNumber(m4, new double[]{kurtosis});
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
