package tango.plugin.segmenter;

import ij.IJ;
import ij.ImagePlus;
import mcib3d.image3d.ImageFloat;
import mcib3d.image3d.ImageHandler;
import mcib3d.image3d.ImageInt;
import mcib3d.image3d.ImageLabeller;
import tango.dataStructure.InputImages;
import tango.parameter.BooleanParameter;
import tango.parameter.ConditionalParameter;
import tango.parameter.IntParameter;
import tango.parameter.Parameter;
import tango.parameter.ThresholdParameter;

/*
 * To change this template, choose Tools | Templates and open the template in
 * the editor.
 */
/**
 *
 **
 * /**
 * Copyright (C) 2008- 2012 Thomas Boudier and others
 *
 *
 *
 * This file is part of mcib3d
 *
 * mcib3d is free software; you can redistribute it and/or modify it under the
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
 * @author thomas
 */
public class Segment3D_ implements NucleusSegmenter, SpotSegmenter {

    ImagePlus myPlus;
    // DB
    boolean debug = false;
    int nbCPUs = 1;
    ThresholdParameter lowThreshold = new ThresholdParameter("Low Threshold:", "lowThreshold", "Value");
    //ThresholdParameter highThreshold = new ThresholdParameter("High Threshold:", "highThreshold", "Value");
    //BooleanParameter useMaxThresholdBox = new BooleanParameter("Specify a Max threshold", "useMaxThreshold", true);
    //ConditionalParameter useMaxThreshold = new ConditionalParameter(useMaxThresholdBox);
    IntParameter minSize = new IntParameter("Minimum size:", "minSize", 0);
    BooleanParameter useMaxsizeBox = new BooleanParameter("Specify a Max size", "useMaxSize", false);
    ConditionalParameter useMaxSize = new ConditionalParameter(useMaxsizeBox);
    IntParameter maxSize = new IntParameter("Maximum size:", "maxSize", Integer.MAX_VALUE);
    BooleanParameter deleteOutsideNuclei = new BooleanParameter("Delete outside nuclei", "deleteOutsideNuclei", true);
    Parameter[] par = {lowThreshold, minSize, useMaxSize, deleteOutsideNuclei};
    private boolean nucMode;

    @Override
    public Parameter[] getParameters() {
        //useMaxThreshold.setCondition(true, new Parameter[]{highThreshold});
        //useMaxThresholdBox.setHelp("For bright object use the maximum pixel value, else use a threshold to define the high threshold", true);
        //highThreshold.setCompulsary(false);
        useMaxSize.setCondition(true, new Parameter[]{maxSize});
        lowThreshold.setHelp("The lower threshold inclusive, the high threshold is the maximum value in the image", true);
        //highThreshold.setHelp("The upper threshold, only if you do not want to use the highest pixel value", true);
        minSize.setHelp("The minimum size for object", true);
        useMaxSize.setHelp("Use the maximum possible size for objects, or specify a maximal size", true);
        maxSize.setHelp("The maximum size for object, only if you do not want to use the default maximum size", true);
        deleteOutsideNuclei.setHelp("Delete objects or parts of them that are outside the nuclei", true);
        return par;
    }

    @Override
    public void setVerbose(boolean debug) {
        this.debug = debug;
    }

    // nucleiSegmenter
    @Override
    public ImageInt runNucleus(int currentStructureIdx, ImageHandler input, InputImages images) {
        nucMode = true;
        return segmentation(input, images);
    }

//    @Override
//    public int[] getTags() {
//        return null;
//    }

    @Override
    public void setMultithread(int nbCPUs) {
        this.nbCPUs = nbCPUs;
    }

    // spotSegmenter
    @Override
    public ImageInt runSpot(int currentStructureIdx, ImageHandler input, InputImages images) {
        nucMode = false;
        return segmentation(input, images);
    }

    private ImageInt segmentation(ImageHandler input, InputImages images) {
        //IJ.log("Simple Segmenter");
        Double D = lowThreshold.getThreshold(input, images, nbCPUs, debug);
        float low = D.floatValue();
        if (debug) {
            IJ.log("Threshold low " + low);
        }
        //float high;
//        if (!useMaxThresholdBox.isSelected()) {
//            high = -1;
//            if (debug) {
//                IJ.log("Threshold high not specified " + high);
//            }
//        } else {
//            high = (highThreshold.getThreshold(input, images, nbCPUs, debug)).floatValue();
//            if (debug) {
//                IJ.log("Threshold high specified  " + high);
//            }
//        }

        int min = minSize.getIntValue(0);
        int max = Integer.MAX_VALUE;
        if (useMaxsizeBox.isSelected()) {
            max = maxSize.getIntValue(Integer.MAX_VALUE);
        }
//        Segment3D seg3d = new Segment3D();       
//        ImagePlus segPlus = seg3d.segmentation3D(input.getImagePlus(), low, high, min, max, false);       
//        // delete outside nuclei (only in not nucleus mode)
//        ImageInt segHandler = ImageInt.wrap(segPlus);

        // use ImageLabeller
        ImageLabeller labeller = new ImageLabeller(min, max);
        ImageInt segHandler = labeller.getLabels(input.thresholdAboveInclusive(low), false);
        if (deleteOutsideNuclei.isSelected()&& !nucMode) {
            segHandler.intersectMask(images.getMask());
        }

        return segHandler;
    }

    @Override
    public ImageFloat getProbabilityMap() {
        return null;
    }

    @Override
    public String getHelp() {
        return "Simple 3D thresholding and labelling, using code from 3D Objects Counter";
    }
}
