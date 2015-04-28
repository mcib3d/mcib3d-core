package tango.plugin.filter;

import mcib3d.utils.exceptionPrinter;
import ij.IJ;
import ij.ImagePlus;
import ij.gui.GenericDialog;
import ij.plugin.PlugIn;
import ij.process.ImageProcessor;
import java.util.HashMap;
import java.util.TreeMap;
import mcib3d.image3d.ImageByte;
import mcib3d.image3d.ImageHandler;
import mcib3d.image3d.ImageInt;
import mcib3d.image3d.ImageShort;
import tango.dataStructure.InputImages;
import tango.parameter.*;
import tango.plugin.filter.PostFilter;
import mcib3d.image3d.ImageLabeller;

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
public class Watershed2D implements PostFilter {

    boolean debug;
    int nbCPUs = 1;
    Parameter[] parameters = new Parameter[]{};

    public Watershed2D() {
    }

    @Override
    public Parameter[] getParameters() {
        return parameters;
    }

    @Override
    public void setVerbose(boolean debug) {
        this.debug = debug;
    }

    @Override
    public ImageInt runPostFilter(int currentStructureIdx, ImageInt input, InputImages images) {
        ImagePlus plus = input.getImagePlus();
        plus.getProcessor().threshold(0);
        ImageProcessor ip2 = plus.getProcessor().convertToByte(false);
        ImagePlus plus2 = new ImagePlus("", ip2);
        ij.plugin.filter.EDM wat = new ij.plugin.filter.EDM();
        wat.setup("watershed", plus2);
        wat.run(ip2);
        ImageLabeller lab = new ImageLabeller(debug);
        return lab.getLabels(ImageInt.wrap(plus2), false);
    }

    @Override
    public void setMultithread(int nbCPUs) {
        this.nbCPUs = nbCPUs;
    }

    @Override
    public String getHelp() {
        return "Separate touching objects using Watershed 2D from ImageJ. For 2D images only";
    }
}
