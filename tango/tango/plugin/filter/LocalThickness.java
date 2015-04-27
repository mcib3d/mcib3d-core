package tango.plugin.filter;

import ij.ImagePlus;
import mcib3d.image3d.ImageHandler;
import mcib3d.image3d.distanceMap3d.EDT;
import mcib3d.image3d.distanceMap3d.EDT;
import tango.dataStructure.InputImages;
import tango.parameter.BooleanParameter;
import tango.parameter.Parameter;
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

public class LocalThickness implements PreFilter {
    boolean debug;
    int nbCPUs=1;
    ThresholdParameter threshold = new ThresholdParameter("Binarization Threshold: ", "threshold", null);
    BooleanParameter invert = new BooleanParameter("Outside Object:", "invert", false);
    
    Parameter[] parameters = new Parameter[]{threshold, invert};
    
    public LocalThickness() {
        threshold.setHelp("Pixels above this threshold are considered as objects, the other pixels as background", true);
        invert.setHelp("If Inside Object is selected, computes local thickness from objects in the background, otherwise computes distance from background inside the objects", true);
    }
    
    @Override
    public void setVerbose(boolean debug) {
        this.debug=debug;
    }

    @Override
    public ImageHandler runPreFilter(int currentStructureIdx, ImageHandler input, InputImages images) {
        float thldF;
        Double thld = threshold.getThreshold(input, images, nbCPUs, debug);
        if (thld == null) thldF=0;
        else thldF = thld.floatValue();
        return EDT.localThickness(input, images.getMask(), thldF, invert.isSelected(), nbCPUs);
    }

    @Override
    public Parameter[] getParameters() {
        return parameters;
    }

    @Override
    public String getHelp() {
        return "";
    }

    @Override
    public void setMultithread(int nbCPUs) {
        this.nbCPUs=nbCPUs;
    }
    
}
