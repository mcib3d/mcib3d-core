package tango.plugin.segmenter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.TreeMap;
import mcib3d.image3d.*;
import tango.dataStructure.InputCellImages;
import tango.dataStructure.InputCroppedImages;
import tango.dataStructure.InputFieldImages;
import tango.dataStructure.InputImages;
import tango.parameter.*;
import tango.plugin.filter.EraseRegions;
import tango.plugin.filter.mergeRegions.MergeRegions;

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
public class NucleusEdgeDetector implements NucleusSegmenter {
    boolean debug;
    int nbCPUs=1;
    ThresholdParameter thresholderGlobal = new ThresholdParameter("Global Threshold:", "globalThld", "AutoThreshold", new Parameter[]{new ChoiceParameter("", "", new String[]{"OTSU"}, "OTSU")});
    ThresholdParameter thresholderLocal = new ThresholdParameter("Local Region Threshold: ", "localThld", "AutoThreshold", new Parameter[]{new ChoiceParameter("", "", new String[]{"OTSU"}, "OTSU")});
    DoubleParameter gradScale = new DoubleParameter("Gradient Scale: ", "gradientScale", 1d, Parameter.nfDEC1);
    IntParameter size = new IntParameter("Minimum Nucleus Size:", "minSize", 10000);
    IntParameter border = new IntParameter("Border:", "border", 5);
    
    Parameter[] parameters= new Parameter[]{thresholderGlobal, size, border, gradScale, thresholderLocal};
    
    public NucleusEdgeDetector() {
        thresholderGlobal.setHelp("Global threshold applied to the image to detect nuclei approximatly", true);
        border.setHelp("Border to add when cropping around nuclei, in case the adjusted nucleus would be bigger than the approximate nucleus", true);
        gradScale.setHelp("Integration Scale for Gradient magnitude computation (used as watershed map)", false);
        thresholderLocal.setHelp("Threshold applied to regions detected after the local watershed procedure", false);
    }
    
    
    @Override
    public ImageInt runNucleus(int currentStructureIdx, ImageHandler input, InputImages rawImages) {
        // step 1 : global threshold
        double thld = thresholderGlobal.getThreshold(input, rawImages, nbCPUs, debug);
        ImageInt labelMap = HysteresisSegmenter.run(input, null, thld, thld, true, debug);
        if (debug) labelMap.showDuplicate("Labelled image");
        // step 2: local adjustment to gradient
        TreeMap<Integer, int[]> bounds = labelMap.getBounds(false);
        ArrayList<Integer> labels = new ArrayList<Integer>(bounds.keySet());
        ArrayList<ImageInt> postFilteredImages = new ArrayList<ImageInt>(labels.size());
        
        int b = border.getIntValue(5);
        int minSize = size.getIntValue(10000);
        double scale = gradScale.getDoubleValue(1);
        if (scale<1) scale=1;
        boolean verboseDone=false;
        for (int label : bounds.keySet()) {
            //if (verboseDone) continue;
            if (debug) System.out.println("NED: label:" +label+ " / "+ bounds.size());
            int[] curBounds = bounds.get(label);
            if ((curBounds[1]-curBounds[0])<=1 || (curBounds[3]-curBounds[2])<=1) continue; // FIXME case of 2D segmentation: allow thin objects...
            if (debug) System.out.println("NED: cropping...");
            InputCroppedImages ici=new InputCroppedImages(rawImages, labelMap, label, curBounds, b, false, true);
            ImageInt croppedMask = ici.getMask();
            if (debug) System.out.println("NED: label:" +label+ " volume:"+ ici.getVolume());
            if (ici.getVolume()<minSize) continue;
            if (debug) System.out.println("NED: watershed transform...");
            WatershedTransform3D ws = new WatershedTransform3D(nbCPUs, debug);
            ws.setDynamics(false, false, 0, false, 0, false, 0);
            ImageHandler in = ici.getFilteredImage(currentStructureIdx);
            
            ImageInt segImage = ws.runWatershed(in, in.getGradient(scale, nbCPUs), croppedMask);
            segImage.setScale(croppedMask);
            segImage.setOffset(croppedMask);
            if (debug) System.out.println("NED: erase regions...");
            EraseRegions er = new EraseRegions();
            er.setMultithread(nbCPUs);
            er.setVerbose(debug&&!verboseDone);
            er.setThresholder(thresholderLocal.getPlugin(nbCPUs, debug&&!verboseDone));
            er.eraseRegionsMean(segImage, in, null, ici, false, false);
            if (debug) System.out.println("NED: merge regions...");

            MergeRegions.mergeAllConnected(segImage, debug&&!verboseDone);
            postFilteredImages.add(segImage);
            //if (debug) verboseDone=true;
        }
        // step 3 : merge nuclei
        if (debug) System.out.println("NED: merging nuclei... nb of nuclei:"+postFilteredImages.size());
        if (labelMap instanceof ImageShort) {
            labelMap.erase();
            ((ImageShort)labelMap).appendMasks(postFilteredImages, 1);
            return (ImageShort)labelMap;
        } else {
            ImageShort res = new ImageShort(input.getTitle(), input.sizeX, input.sizeY, input.sizeZ);
            res.appendMasks(postFilteredImages, 1);
            return res;
        }
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
        return parameters;
    }

    @Override
    public String getHelp() {
        return "Fast algorithm for precise detection of nucleus edges. For noisy images, use pre-processing like 3D median and increase gradient integration scale. It Won't split nuclei in close contact. A global threshold is applied to the image to roughly detect nuclei. Segmentation is adjusted locally: nuclei are cropped with a border (in case the adjusted nucleus should be bigger than the approximate detection) and a watershed 3D is applied on the gradient image to adjust to the edges of the nucleus";
    }

    

}
