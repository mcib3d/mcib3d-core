package tango.plugin.filter;

import ij.ImagePlus;
import mcib3d.image3d.ImageHandler;
import tango.dataStructure.InputImages;
import tango.parameter.BooleanParameter;
import tango.parameter.ChoiceParameter;
import tango.parameter.DoubleParameter;
import tango.parameter.Parameter;
import tango.plugin.filter.PreFilter;
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
public class GradientMagnitude implements PreFilter {
    boolean debug;
    int nbCPUs=1;
    DoubleParameter iscale = new DoubleParameter("Integration Scale (Pix)", "iscale", 1d, Parameter.nfDEC3);
    BooleanParameter useScale = new BooleanParameter("Use Image Scale", "useImageScale", true);
    Parameter[] parameters = new Parameter[] {iscale, useScale};
    @Override
    public Parameter[] getParameters() {
        return parameters;
    }

    @Override
    public void setVerbose(boolean debug) {
        this.debug=debug;
    }

    @Override
    public ImageHandler runPreFilter(int currentStructureIdx, ImageHandler input,  InputImages images ) {
        double scaleZ = input.getScaleZ();
        double scaleXY = input.getScaleXY();
        String unit = input.getUnit();
        if (!useScale.isSelected()) input.setScale(scaleXY, scaleXY, unit);
        ImageHandler res =  input.getGradient(Math.max(iscale.getFloatValue(1), 1), nbCPUs);
        if (!useScale.isSelected()) {
            res.setScale(scaleXY, scaleZ, unit);
            input.setScale(scaleXY, scaleZ, unit);
        }
        return res;
    }

    @Override
    public void setMultithread(int nbCPUs) {
        this.nbCPUs=nbCPUs;
    }
    
    @Override
    public String getHelp() {
        return "Compute gradient Magnitude. from featureJ";
    }
    
}
