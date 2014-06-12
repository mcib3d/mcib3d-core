package tango.plugin.segmenter;

import mcib3d.image3d.ImageLabeller;
import java.util.ArrayList;
import java.util.HashMap;
import mcib3d.image3d.*;
import tango.dataStructure.InputCellImages;
import tango.dataStructure.InputFieldImages;
import tango.dataStructure.InputImages;
import tango.parameter.BooleanParameter;
import tango.parameter.Parameter;
import tango.parameter.StructureParameter;
import tango.parameter.ThresholdParameter;

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
public class HysteresisSegmenter implements NucleusSegmenter, SpotSegmenter {
    boolean debug;
    int nbCPUs=1;
    ThresholdParameter thresholdHigh = new ThresholdParameter("Threshold High:", "thldHigh", null);
    ThresholdParameter thresholdLow = new ThresholdParameter("Threshold Low:", "thldLow", null);
    BooleanParameter lowConnectivity = new BooleanParameter("Use 6 connectivity (vs 26)", "connex6", true);
    Parameter[] parameters= new Parameter[]{thresholdLow, thresholdHigh, lowConnectivity};
    
    
    public HysteresisSegmenter() {
        thresholdHigh.setCompulsary(false);
    }
    
    
    @Override
    public ImageInt runNucleus(int currentStructureIdx, ImageHandler input, InputImages rawImages) {
        return run(input, rawImages);
    }
    
    @Override
    public ImageInt runSpot(int currentStructureIdx, ImageHandler input, InputImages rawImages) {
        return run(input, rawImages);
    }
    
    protected ImageInt run(ImageHandler input, InputImages rawImages) {
        if (debug) System.out.println("Getting thld Low..");
        double low = thresholdLow.getThreshold(input, rawImages, nbCPUs, debug);
        if (debug) System.out.println("thld Low:"+low);
        if (debug) System.out.println("Getting thld High..");
        Double high = thresholdHigh.getThreshold(input, rawImages, nbCPUs, debug);
        if (debug) System.out.println("thld High:"+high);
        return run(input, rawImages.getMask(), low, (high!=null)?high.doubleValue():low, lowConnectivity.isSelected(), debug);
    }
    
    public static ImageInt run(ImageHandler input, ImageInt mask, double thldLow, double thldHigh, boolean lowConnectivity, boolean verbose) {
        ImageHandler maskres;
        if (thldHigh<=thldLow) {
            maskres = input.threshold((float)thldLow, false, false);
        } else {
            maskres = input.duplicate();
            maskres.hysteresis(thldLow, thldHigh, lowConnectivity);
            // FIXME : si pixels > thldHigh hors du mask -> incoherence...
        }
        if (mask!=null && !(mask instanceof BlankMask)) maskres.intersectMask(mask);
        ImageLabeller lab = new ImageLabeller(verbose);
        return lab.getLabels(maskres, lowConnectivity);
    }
    
    

//    @Override
//    public int[] getTags() {
//        return null;
//    }

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
        thresholdLow.setHelp("The low threshold, pixels under this value will be considered as background", true);
        thresholdHigh.setHelp("The high threshlod, pixels above this value will be considered as object", true);
        return parameters;
    }

    @Override
    public String getHelp() {
        return "Hysteresis Segmentation. This technique actually define 3 classes using two thresholds.<br> Pixels below <b>low threshold</b> are considered as background, pixels above <b>high threshold</b> are considered as objects and pixels between low and high thresholds are considered as intermediate and will be linked to background or objects depending on their neighbourhood. <br>All intermediate pixels connected to at least one pixel above high threshold are transformed into object pixels, otherwise they are transformed to background pixels.";
    }

    

    @Override
    public ImageFloat getProbabilityMap() {
        return null;
    }
    
    
    
}
