package tango.plugin.filter;

import mcib3d.image3d.ImageHandler;
import tango.dataStructure.InputImages;
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
public class TestThresholdMethod implements PreFilter {
    boolean debug;
    int nbCPUs=1;
    ThresholdParameter thld = new ThresholdParameter("Threshold Method:", "threshold", null);
    Parameter[] parameters= new Parameter[]{thld};
    @Override
    public ImageHandler runPreFilter(int currentStructureIdx, ImageHandler input, InputImages images) {
        double thldValue = thld.getThreshold(input, images, nbCPUs, debug);
        if (debug) ij.IJ.log("Thld method:"+thld.getMethod()+ " value:"+thldValue);
        return input;
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
        return "To test a thresholding method";
    }
    
}
