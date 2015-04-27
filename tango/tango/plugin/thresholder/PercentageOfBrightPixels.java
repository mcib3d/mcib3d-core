package tango.plugin.thresholder;

import tango.parameter.NumberParameter;
import tango.parameter.Parameter;
import ij.IJ;
import ij.ImagePlus;
import mcib3d.image3d.ImageHandler;
import tango.dataStructure.InputImages;
import tango.parameter.DoubleParameter;
import tango.parameter.SliderDoubleParameter;
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
public class PercentageOfBrightPixels implements Thresholder {

    boolean debug;
    int nCPUs = 1;
    SliderDoubleParameter percentage = new SliderDoubleParameter("Percentage of Brigth Pixels: ", "pixPercent", 0, 100, 0.5, 2);
    Parameter[] parameters = new Parameter[]{percentage};

    @Override
    public Parameter[] getParameters() {
        return parameters;
    }

    @Override
    public void setVerbose(boolean debug) {
        this.debug = debug;
    }

    @Override
    public void setMultithread(int nCPUs) {
        this.nCPUs = nCPUs;
    }

    @Override
    public double runThresholder(ImageHandler input, InputImages images) {
        double thld = input.getPercentile(percentage.getValue() / 100.0, images.getMask());
        if (debug) {
            IJ.log("Thresholder: % of bright pixels: " + percentage.getValue() + " Result:" + thld);
        }
        return thld;
    }

    @Override
    public String getHelp() {
        return "The percentage of bright pixels";
    }
}
