package tango.plugin.filter;

import ij.IJ;
import mcib3d.image3d.ImageHandler;
import tango.dataStructure.InputImages;
import tango.parameter.BooleanParameter;
import tango.parameter.IntParameter;
import tango.parameter.Parameter;

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
public class DeleteSlices implements PreFilter {

    BooleanParameter beginLast = new BooleanParameter("Last frame as reference", "lastBegin", false);
    IntParameter beginSlice = new IntParameter("Begin slice", "beginSlice", 1);
    //BooleanParameter endLast = new BooleanParameter("Last frame as reference", "lastEnd", false);
    IntParameter endSlice = new IntParameter("End slice", "endSlice", 2);
    Parameter[] parameters = {beginLast, beginSlice, endSlice};
    //MultiParameter multiP = new MultiParameter("Repetition", "repet", pars, 1, 10, 1);
    //protected Parameter[] parameters = new Parameter[]{multiP};
    boolean verbose;
    int nCPUs = 1;

    @Override
    public ImageHandler runPreFilter(int currentStructureIdx, ImageHandler input, InputImages images) {
        ImageHandler img;
        int sizeZ = input.sizeZ;
        int z0 = beginSlice.getIntValue(1) - 1;
        int z1 = endSlice.getIntValue(1) - 1;
        if (beginLast.isSelected()) {
            z0 = sizeZ - 1 - beginSlice.getIntValue(1);
            z1 = sizeZ - 1 - endSlice.getIntValue(1);
        }
        if (verbose) {
            IJ.log("Delete Slices : " + z0 + "-" + z1);
        }
        img = input.deleteSlices(z0, z1);

        return img;
    }

    @Override
    public Parameter[] getParameters() {
        beginLast.setHelp("If checked consider the last frame as reference, 0 meaning last frame, 1 meaning one before last frame", true);
        beginSlice.setHelp("The first slice to delete", true);
        //endLast.setHelp("If checked consider the last frame as reference,0 meaning last frame", verbose);
        endSlice.setHelp("The last slice to delete", true);
        return parameters;
    }

    @Override
    public String getHelp() {
        return "Delete slices in a stack (slices start at 1).";
    }

    @Override
    public void setVerbose(boolean verbose) {
        this.verbose = verbose;
    }

    @Override
    public void setMultithread(int nCPUs) {
    }
}
