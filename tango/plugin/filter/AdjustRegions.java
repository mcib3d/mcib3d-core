package tango.plugin.filter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.TreeMap;
import mcib3d.image3d.ImageHandler;
import mcib3d.image3d.ImageInt;
import mcib3d.image3d.ImageShort;
import tango.dataStructure.InputCroppedImages;
import tango.dataStructure.InputImages;
import tango.parameter.ChoiceParameter;
import tango.parameter.ConditionalParameter;
import tango.parameter.Parameter;
import tango.plugin.segmenter.WatershedEdgeDetection;

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
public class AdjustRegions implements PostFilter {
    
    boolean debug;
    int nbCPUs=1;
    
    static String[] methods=new String[] {"Gaussian Fit", "Edge Detector"}; //, "Local Threshold"
    ChoiceParameter choice = new ChoiceParameter("Choose Adjustement Method:", "thresholderMethod", methods, methods[0]); 
    SpotLocalThresholder gaussianThresholder=new GaussianFit(); //, new LocalThresholdAdjustment() , new MaximumGradientFit(), new MaximumGradientFitDistanceMap()
    ConditionalParameter cond= new ConditionalParameter(choice);
    Parameter[] parameters = new Parameter[] {cond};
        
    public AdjustRegions() {
         cond.setCondition(methods[0], gaussianThresholder.getParameters());
    }
    
    @Override
    public ImageInt runPostFilter(int currentStructureIdx, ImageInt input, InputImages images) {
        int idx = choice.getSelectedIndex();
        if (idx==0) {
            gaussianThresholder.setVerbose(debug);
            gaussianThresholder.setMultithread(nbCPUs);
            return gaussianThresholder.runPostFilter(currentStructureIdx, input, images);
        } else if (idx==1) {
            ImageHandler intensity = images.getFilteredImage(currentStructureIdx);
            return WatershedEdgeDetection.edgeDetection(input, intensity, nbCPUs, debug);
        } else return input;
    }
    
 

    @Override
    public String getHelp() {
        if (choice.getSelectedIndex()==0) return gaussianThresholder.getHelp();
        else if (choice.getSelectedIndex()==1) return "Edge Detector";
        else return "Adjust Regions, choose a method";
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
    
   
    
}
