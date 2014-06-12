/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package tango.plugin.filter;

import ij.IJ;
import ij.ImagePlus;
import java.util.TreeMap;
import mcib3d.image3d.ImageInt;
import mcib_plugins.segmentation.Segment3D;
import tango.dataStructure.InputImages;
import tango.parameter.BooleanParameter;
import tango.parameter.Parameter;
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
public class AdjustLabels implements PostFilter {

    boolean debug;
    int nbCPUs = 1;
    BooleanParameter PLabel = new BooleanParameter("Re-label image", "relabel", false);
    Parameter[] parameters = new Parameter[]{PLabel};

    public AdjustLabels() {        
    }

    @Override
    public ImageInt runPostFilter(int currentStructureIdx, ImageInt in, InputImages images) {
        if (PLabel.getValue()) {
            ImageLabeller lab = new ImageLabeller(debug);
            if (debug) {
                IJ.log("Re-Labelling");
            }
            in = lab.getLabels(in, true);           
        }
        TreeMap<Integer, int[]> bounds = in.getBounds(false);
        boolean b = in.shiftIndexes(bounds);
        if (debug) {
            if (b) {
                IJ.log("Shift indexes successfull");
            } else {
                IJ.log("No shift indexes were necessary");
            }
        }
        return in;
    }

    @Override
    public void setVerbose(boolean debug) {
        this.debug = debug;
    }

    @Override
    public void setMultithread(int nbCPUs) {
        this.nbCPUs = nbCPUs;
    }

    @Override
    public Parameter[] getParameters() {
        PLabel.setHelp("Segment and re-label the image, only useful if a process has separated objects (like watershed or manual drawing)", true);
        return parameters;
    }

    @Override
    public String getHelp() {
        return "Renumber labels to match indexes";
    }
}
