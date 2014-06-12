package tango.plugin.segmenter;

import ij.IJ;
import java.util.*;
import mcib3d.geom.Object3DVoxels;
import mcib3d.geom.Voxel3D;
import mcib3d.image3d.*;
import mcib3d.utils.ThreadRunner;
import mcib3d.utils.exceptionPrinter;
import tango.dataStructure.InputCellImages;
import tango.dataStructure.InputImages;
import tango.gui.Core;
import tango.parameter.*;
import tango.plugin.filter.GaussianFit;
import tango.plugin.filter.Structure;
import tango.plugin.segmenter.SpotSegmenter;
import tango.util.ImageUtils;

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
public class SpotDetector3D extends SeededWatershed3D implements SpotSegmenter  {
    
    //seeds:
    ThresholdParameter thldLow_P = new ThresholdParameter("Background Limit:", "thldLow", "AutoThreshold", new Parameter[]{new ChoiceParameter("", "", new String[]{"OTSU"}, "OTSU")});
    
    DoubleParameter hessianScale_P = new DoubleParameter("Hessian Scale:", "hessianScale", 1d, Parameter.nfDEC2);
    ThresholdParameter hessianThld_P = new ThresholdParameter("Hessian Upper limit:", "hessianThld", "Percentage Of Bright Pixels", new Parameter[]{new SliderDoubleParameter("pixPercent", "pixPercent", 0, 100, 95d, 2)});
    ThresholdParameter seedThld = new ThresholdParameter("Seed Threshold:", "seedThld", "AutoThreshold", new Parameter[]{new ChoiceParameter("", "", new String[]{"OTSU"}, "OTSU")});
    //BooleanParameter useHessianAsW
    //PreFilterParameter watershedMap_P2 = new PreFilterParameter("Watershed Map:", "watershedMap", "Image Features", new Parameter[]{new ChoiceParameter("", "", new String[]{"Hessian"}, "Hessian")}); 
    
    GroupParameter seeds_P = new GroupParameter("Seeds", "seeds", new Parameter[]{hessianScale_P, hessianThld_P, seedThld});
    
    public SpotDetector3D() {
        super();
        //thldLow_P.setCompulsary(false);
    }
    
    @Override
    public String getHelp() {
        return"<ul><strong>Spot Detector 3D </strong>"
                + "<br> For detection of spots-like objects"
                + "<li>Uses max hessian eigen value transform as a propagation map. </li>"
                + "<li>Constraints on seeds (applied before propagation): regional minima of the propagation map, constraint on max hessian eigen value (spotiness), constraint on intensity level. </li>"
                + "<li>Runs until the background threshold. </li>"
                + "<li>If adjust spots is selected, segmented spots are adjusted to the maximum of gradient. </li>"
                + "<li>This plugin is under developpement, and is subject to changes in the next versions of TANGO</li></ul>";

    }

    
    @Override
    public ImageInt runSpot(int currentStructureIdx, ImageHandler input, InputImages images) {
        //ImageHandler wsmap = watershedMap_P2.preFilter(0, input, images, nCPUs, debug);
        double hessianScale=Math.max(1, hessianScale_P.getDoubleValue(1));
        ImageFloat wsmap = input.getHessian(hessianScale, nCPUs)[0];
        WatershedTransform3DSeedConstraint wsT = new WatershedTransform3DSeedConstraint(wsmap, nCPUs, debug);
        double thldLow;
        Double tl = thldLow_P.getThreshold(input, images, nCPUs, debug);
        if (tl==null) thldLow = input.getMin(images.getMask());
        else thldLow = tl;
        double thldHigh = seedThld.getThreshold(input, images, nCPUs, debug);
        if (thldHigh<thldLow) thldHigh=thldLow;
        double hessianThld=this.hessianThld_P.getThreshold(wsmap, images, nCPUs, debug);
        if (debug) {
            IJ.log("SpotDetector3D: background limit: "+thldLow);
            IJ.log("SpotDetector3D:  seed intensity thld: "+thldHigh);
            IJ.log("SpotDetector3D: seed hessian thld: "+hessianThld);
        }
        wsT.setThresholds((float)thldHigh, (float)thldLow, true, (float)hessianThld);
        wsTransform=wsT;
        setDynamicsParameters();
        if (debug) wsmap.showDuplicate("Watershed Map");
        ImageInt seg =  wsTransform.runWatershed(input, wsmap, images.getMask());
        return seg;
    }
    

    @Override
    public Parameter[] getParameters() {
        return new Parameter[]{thldLow_P, seeds_P}; //dynCond
    }

}
