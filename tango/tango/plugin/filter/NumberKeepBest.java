/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package tango.plugin.filter;

import ij.IJ;
import mcib3d.geom.Object3D;
import mcib3d.image3d.ImageHandler;
import mcib3d.image3d.ImageInt;
import mcib3d.utils.ArrayUtil;
import tango.dataStructure.InputImages;
import tango.parameter.ChoiceParameter;
import tango.parameter.IntParameter;
import tango.parameter.Parameter;
import tango.parameter.StructureParameter;

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
public class NumberKeepBest implements PostFilter {

    boolean debug;
    int nbCPUs = 1;
    IntParameter nbParam = new IntParameter("Fixed number of objects", "nbObj", 92);
    ChoiceParameter criteria = new ChoiceParameter("Criteria ", "criteria", new String[]{"Volume", "Mean Intensity", "Integrated Density"}, "Volume");
    StructureParameter signal = new StructureParameter("Signal", "signal", 1, false);
    //ConditionalParameter useMaxCond = new ConditionalParameter("Use a constraint on maximum number of objects", criteria);
    Parameter[] parameters = new Parameter[]{nbParam, criteria, signal};

    public NumberKeepBest() {
        nbParam.setHelp("The number of objects to keep. If actual number lower, does nothing.", true);
        criteria.setHelp("The criteria to select best objects. ", debug);
        signal.setHelp("The structure used to compute mean intensity or integrated density.", true);
    }

    @Override
    public ImageInt runPostFilter(int currentStructureIdx, ImageInt in, InputImages images) {
        Object3D[] objects = in.getObjects3D();
        int nbObj = objects.length;
        int nb = nbParam.getIntValue(0);
        if (debug) {
            IJ.log("Nb obj=" + nbObj + " keep=" + nb);
        }
        if (nb > nbObj) {
            return in;
        }
        ImageHandler sig = images.getImage(signal.getIndex());

        ArrayUtil tab = new ArrayUtil(nbObj);
        String crit = (String) criteria.getValue();
        for (int i = 0; i < nbObj; i++) {
            Object3D ob = objects[i];
            if (crit.equals("Volume")) {
                tab.putValue(i, ob.getVolumePixels());
            } else if (crit.equals("Mean Intensity")) {
                tab.putValue(i, ob.getPixMeanValue(sig));
            } else if (crit.equals("Integrated Density")) {
                tab.putValue(i, ob.getIntegratedDensity(sig));
            }
        }
        int[] idx = tab.sortIndexShellMeitzner();

        for (int i = 0; i < nbObj - nb; i++) {
            if (debug) {
                IJ.log("Deleting object " + idx[i]);
            }
            objects[idx[i]].draw(in, 0);
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
        return parameters;
    }

    @Override
    public String getHelp() {
        return "Keep only specified number of objects, based on criteria.";
    }
}
