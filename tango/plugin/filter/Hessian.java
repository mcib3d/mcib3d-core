package tango.plugin.filter;

import ij.ImagePlus;
import mcib3d.image3d.ImageFloat;
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
public class Hessian implements PreFilter {

    boolean debug;
    int nbCPUs = 1;
    DoubleParameter scale = new DoubleParameter("Integration Scale (Pix)", "scale", 1d, Parameter.nfDEC3);
    BooleanParameter useScale = new BooleanParameter("Use Image Scale", "useImageScale", true);
    ChoiceParameter choice = new ChoiceParameter("Compute", "compute", new String[]{"max", "mid", "min", "det", "cur"}, "max");
    BooleanParameter invert = new BooleanParameter("Invert values", "invert_hessian", false);
    BooleanParameter clip = new BooleanParameter("Clip negative values", "clip_hessian", false);
    Parameter[] parameters = new Parameter[]{scale, useScale, choice, invert, clip};

    @Override
    public Parameter[] getParameters() {
        return parameters;
    }

    @Override
    public void setVerbose(boolean debug) {
        this.debug = debug;
    }

    @Override
    public ImageHandler runPreFilter(int currentStructureIdx, ImageHandler input, InputImages images) {
        ImageFloat res = null;
        double scaleZ = input.getScaleZ();
        double scaleXY = input.getScaleXY();
        String unit = input.getUnit();
        if (!useScale.isSelected()) input.setScale(scaleXY, scaleXY, unit);
        int cho = choice.getSelectedIndex();
        // eigne values
        if (cho < 3) {
            res = input.getHessian(scale.getFloatValue(1), nbCPUs)[choice.getSelectedIndex()];
        } // determinant
        else if (cho == 3) {
            res = input.getHessianDeterminant(scale.getFloatValue(1), nbCPUs, false);
        } // curvature = det * itensity
        else if (cho == 4) {
            res = input.getHessianDeterminant(scale.getFloatValue(1), nbCPUs, true);
            //res = (ImageFloat) res.multiplyImage(input, 1);
        }
        if (!useScale.isSelected()) {
            res.setScale(scaleXY, scaleZ, unit);
            input.setScale(scaleXY, scaleZ, unit);
        }
        if (invert.isSelected()) {
            res.opposite();
        }
        if (clip.isSelected()) {
            res.thresholdCut(0, false, false);
        }
        return res;
    }

    @Override
    public void setMultithread(int nbCPUs) {
        this.nbCPUs = nbCPUs;
    }

    @Override
    public String getHelp() {
        return "Eigen values of hessian transformation from FeatureJ";
    }
}
