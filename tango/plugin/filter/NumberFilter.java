/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package tango.plugin.filter;

import mcib3d.geom.Object3D;
import mcib3d.image3d.ImageInt;
import tango.dataStructure.InputImages;
import tango.parameter.BooleanParameter;
import tango.parameter.ConditionalParameter;
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
public class NumberFilter implements PostFilter {

    boolean debug;
    int nbCPUs = 1;
    IntParameter nbMin = new IntParameter("Minimal number of objects", "nbmin", 1);
    IntParameter nbMax = new IntParameter("Maximal number of objects", "nbmax", 1000);
    BooleanParameter useMax = new BooleanParameter("Specify maxmimum number", "usemax", false);
    ConditionalParameter useMaxCond = new ConditionalParameter(useMax);
    Parameter[] parameters = new Parameter[]{nbMin, useMaxCond};

    public NumberFilter() {
        nbMin.setHelp("Speicify the minimum number of objects, if less then all objects are deleted", true);
        nbMax.setHelp("Speicify the maximum number of objects, if greater then all objects are deleted", true);
        useMax.setHelp("Specify a maximum number of objects, else no maximum limit is set", true);
        useMaxCond.setCondition(true, new Parameter[]{nbMax});
    }

    @Override
    public ImageInt runPostFilter(int currentStructureIdx, ImageInt in, InputImages images) {
        Object3D[] objects = in.getObjects3D();
        int nb = objects.length;
        int min = nbMin.getIntValue(1);
        int max = Integer.MAX_VALUE;
        if (useMax.isSelected()) {
            max = nbMax.getIntValue(1000);
        }
        if ((nb < min) || (nb > max)) {
            // erase all
            in.erase();
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
        return "Test object nulbering, if not in a specified range, delete all objects";
    }
}
