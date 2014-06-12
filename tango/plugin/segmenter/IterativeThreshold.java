/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package tango.plugin.segmenter;

import mcib3d.image3d.IterativeThresholding.TrackThreshold;
import ij.ImagePlus;
import ij.measure.Calibration;
import mcib3d.image3d.ImageFloat;
import mcib3d.image3d.ImageHandler;
import mcib3d.image3d.ImageInt;
import tango.dataStructure.InputImages;
import tango.parameter.BooleanParameter;
import tango.parameter.DoubleParameter;
import tango.parameter.NumberParameter;
import tango.parameter.Parameter;
import tango.parameter.SliderParameter;

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
public class IterativeThreshold implements NucleusSegmenter, SpotSegmenter {

    boolean verb = false;
    DoubleParameter vminP = new DoubleParameter("Volume Minimum", "ITvmin", 100.0, NumberParameter.nfDEC3);
    DoubleParameter vmaxP = new DoubleParameter("Volume Maximum", "ITvmax", 10000.0, NumberParameter.nfDEC3);
    BooleanParameter useUnit = new BooleanParameter("Use units", "ITunits", true);
    SliderParameter step = new SliderParameter("Step for iteration", "step", 1, 1000, 10);
    BooleanParameter deleteOutsideNuclei = new BooleanParameter("Delete outside nuclei", "deleteOutsideNuclei", true);
    
    private boolean nucMode;

    private ImageInt process(ImageHandler img, InputImages rawImages) {
        double volMin = vminP.getDoubleValue(100);
        double volMax = vmaxP.getDoubleValue(10000);
        int st = step.getValue();
        Calibration cal = img.getCalibration();
        if ((cal != null) && (useUnit.isSelected())) {
            double volUnit = cal.pixelWidth * cal.pixelHeight * cal.pixelDepth;
            volMin /= volUnit;
            volMax /= volUnit;
        }
        TrackThreshold TT = new TrackThreshold((int) volMin, (int) volMax, st, 0, 0);
        TT.setMethodThreshold(TrackThreshold.THRESHOLD_METHOD_STEP); // others methods for histogram are available
        TT.verbose=verb;
        TT.minElong = true; // find roundest object (or max volume if false)
        ImagePlus res = TT.segment(img.getImagePlus(), verb);

        if (deleteOutsideNuclei.isSelected() && !nucMode) {
            ImageInt segHandler = ImageInt.wrap(res);
            segHandler.intersectMask(rawImages.getMask());
            res = segHandler.getImagePlus();
        }
        
        return ImageInt.wrap(res);
    }

    @Override
    public ImageInt runNucleus(int currentStructureIdx, ImageHandler input, InputImages rawImages) {
        nucMode = true;
        return process(input, rawImages);
    }

//    @Override
//    public int[] getTags() {
//        return null;
//    }

    @Override
    public Parameter[] getParameters() {
        vminP.setHelp("The minimum volume of detected objects", true);
        vmaxP.setHelp("The maximum volume of detected objects", true);
        useUnit.setHelp("Check to use calibrated volumes instead of voxels number", true);
        step.setHelp("The step for the iterations, use 1 for 8-bits images and large value for 16-bits images. The larger the step, the faster the algorithm, but the less accurate", true);
        deleteOutsideNuclei.setHelp("Delete objects or parts of them that are outside the nuclei (only for internal structures)", true);
        Parameter[] par = {vminP, vmaxP, useUnit, step, deleteOutsideNuclei};
        
        return par;
    }

    @Override
    public String getHelp() {
        return "Segmentation method to detect spherical objects with volume in given range, by checking increasing thresholds. Can be quite slow for 16-bits images. "
                + "\nSimilar to MSER approches, like HK-Means in ICY.";
    }

    @Override
    public void setVerbose(boolean verbose) {
        verb = verbose;
    }

    @Override
    public void setMultithread(int nCPUs) {
    }

    @Override
    public ImageInt runSpot(int currentStructureIdx, ImageHandler input, InputImages rawImages) {
        nucMode = false;
        return process(input, rawImages);
    }

    @Override
    public ImageFloat getProbabilityMap() {
        return null;
    }
}
