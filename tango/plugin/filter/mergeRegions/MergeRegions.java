package tango.plugin.filter.mergeRegions;

import tango.plugin.filter.*;
import ij.ImagePlus;
import ij.ImageStack;
import ij.process.AutoThresholder;
import mcib3d.geom.Object3D;
import mcib3d.geom.Object3DVoxels;
import mcib3d.geom.Voxel3D;
import mcib3d.image3d.*;
import tango.dataStructure.InputImages;
import tango.gui.Core;
import tango.parameter.*;
import tango.plugin.thresholder.AutoThreshold;
import tango.plugin.thresholder.Thresholder;

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
public class MergeRegions implements PostFilter {
    boolean debug;
    int nbCPUs=1;
    static String[] methods = new String[]{"Merge All Connected", "Merge All"};
    //static String[] methodsTesting = new String[]{"Merge All Connected", "Merge All", "Merge Sort"};
    ChoiceParameter choice = new ChoiceParameter("Method:", "mergeMethod", methods, methods[0]); 
    //ChoiceParameter choice = new ChoiceParameter("Method:", "mergeMethod", Core.TESTING?methodsTesting:methods, methods[0]); 
    ConditionalParameter cond = new ConditionalParameter(choice);
    Parameter[] parameters=new Parameter[]{cond};
    
    SliderDoubleParameter mergeCoeff = new SliderDoubleParameter("Merge Coefficient", "mergeCoeff", 0, 1, 0.75d, 3);
    PreFilterParameter derivativeMap = new PreFilterParameter("Derivative Map:", "derivativeMap", "Image Features", new Parameter[]{new ChoiceParameter("", "", new String[]{"Gradient Magnitude"}, "Gradient Magnitude")}); 
    ThresholdHistogramParameter derivativeThreshold = new ThresholdHistogramParameter("Derivative Limit:", "derivativeLimit", "AutoThreshold", new Parameter[]{new ChoiceParameter("", "", new String[]{"OTSU"}, "OTSU")});
    
    
    public MergeRegions() {
        //if (Core.TESTING) cond.setCondition(methodsTesting[2], new Parameter[]{derivativeMap, mergeCoeff});
    }
    
    @Override
    public ImageInt runPostFilter(int currentStructureIdx, ImageInt in, InputImages images) {
        
        if (choice.getSelectedIndex()==0) mergeAllConnected(in, debug);
        else if (choice.getSelectedIndex()==1) mergeAll(in);
        else if (choice.getSelectedIndex()==2) {
            ImageHandler derivative = derivativeMap.preFilter(currentStructureIdx, images.getFilteredImage(currentStructureIdx), images, nbCPUs, debug);
            if (debug) derivative.showDuplicate("Merge sort: derivative map");
            //double thld = derivativeThreshold.getThreshold(derivative, images, nbCPUs, debug);
            //if (debug) ij.IJ.log("Merge sort: Derivative limit:"+thld);
            //mergeSort(in, derivative, thld, debug);
            RegionCollection col = new RegionCollection(in, null,  derivative, debug);
            col.initInterfaces();
            //ImageStats stats = col.interfaces.getInterfaceHistogram();
            /*double derivativeLimit = derivativeThreshold.getThreshold(stats.getHisto256(), stats.getHisto256BinSize(), stats.getMin());
            if (debug) {
                ij.IJ.log("Merge sort: Derivative limit:"+derivativeLimit);
                stats.getImage().show("borders");
            }
            * 
            */
            col.mergeSort(1, mergeCoeff.getValue());
        }
        return in;
    }
    
    public static void mergeAllConnected(ImageInt input, boolean debug) {
        RegionCollection col = new RegionCollection(input, null,  null, debug);
        col.mergeAll();
        col.shiftIndicies(false);
    }
    
    public static void mergeAll(ImageInt input) {
        for (int z = 0; z<input.sizeZ; z++) {
            for (int xy = 0; xy<input.sizeXY; xy++) {
                if (input.getPixelInt(xy, z)!=0) input.setPixel(xy, z, 1);
            }
        }
    }
    
    public static void mergeSort(ImageInt input, ImageHandler derivative, double derivativeLimit, boolean debug) {
        RegionCollection col = new RegionCollection(input, null,  derivative, debug);
        col.mergeSort(1, derivativeLimit);
    }

    @Override
    public void setVerbose(boolean debug) {
        this.debug=debug;
    }

    @Override
    public void setMultithread(int nbCPUs) {
        this.nbCPUs=nbCPUs;
    }

    @Override
    public Parameter[] getParameters() {
        return parameters;
    }

    @Override
    public String getHelp() {
        return "Erase Objects according to their mean intensity. Implemented from a procedure designed by Philippe Andrey";
    }
    
}
