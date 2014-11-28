package tango.plugin.filter;

import mcib3d.image3d.ImageFloat;
import mcib3d.image3d.ImageHandler;
import mcib3d.image3d.ImageInt;
import tango.dataStructure.InputImages;
import tango.parameter.BooleanParameter;
import tango.parameter.ConditionalParameter;
import tango.parameter.DoubleParameter;
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
public class DifferenceOfGaussians implements PreFilter {
    boolean debug;
    int nbCPUs=1;
    double voisx1=1;
    double voisz1=voisx1/2;
    double voisx2=10;
    double voisz2=voisx2/2;
    DoubleParameter voisXY_1_P = new DoubleParameter("Smaller Gaussian scale XY (pix): ", "voisXY1", (double) voisx1, Parameter.nfDEC1);
    DoubleParameter voisZ_1_P = new DoubleParameter("Smaller Gaussian sacle Z (pix): ", "voisZ1", (double) voisz1, Parameter.nfDEC1);
    BooleanParameter useScale_1 = new BooleanParameter("Use Image Scale for Z radius: ", "useScale1", true);
    ConditionalParameter cond_1 = new ConditionalParameter("Z-radius", useScale_1);
    DoubleParameter voisXY_2_P = new DoubleParameter("Larger Gaussian scale XY (pix): ", "voisXY2", (double) voisx2, Parameter.nfDEC1);
    DoubleParameter voisZ_2_P = new DoubleParameter("Larger Gaussi scale Z (pix): ", "voisZ2", (double) voisz2, Parameter.nfDEC1);
    BooleanParameter useScale_2 = new BooleanParameter("Use Image Scale for Z radius: ", "useScale2", true);
    ConditionalParameter cond_2 = new ConditionalParameter("Z-radius", useScale_2);
    Parameter[] parameters;
    public DifferenceOfGaussians() {
        voisXY_1_P.setHelp("The radius in <em>X</em> and <em>Y</em> direction for Smaller Gaussian Kernel, in pixels", true);
        voisZ_1_P.setHelp("The radius in <em>Z</em> direction for Smaller Gaussian Kernel, in pixels", true);
        voisXY_2_P.setHelp("The radius in <em>X</em> and <em>Y</em> direction for Larger Gaussian Kernel, in pixels", true);
        voisZ_2_P.setHelp("The radius in <em>Z</em> direction for Larger Gaussian Kernel, in pixels", true);
        cond_1.setCondition(false, new Parameter[]{voisZ_1_P});
        cond_2.setCondition(false, new Parameter[]{voisZ_2_P});
        parameters=new Parameter[]{voisXY_1_P, cond_1, voisXY_2_P, cond_2};
    }
    
    @Override
    public ImageHandler runPreFilter(int currentStructureIdx, ImageHandler input, InputImages images) {
        voisx1=voisXY_1_P.getDoubleValue(voisx1);
        if (useScale_1.isSelected()) voisz1=voisx1 * images.getMask().getScaleXY() / images.getMask().getScaleZ();
        else voisz1=voisZ_1_P.getDoubleValue(voisz1);
        voisx2=voisXY_2_P.getDoubleValue(voisx2);
        if (useScale_2.isSelected()) voisz2=voisx2 * images.getMask().getScaleXY() / images.getMask().getScaleZ();
        else voisz1=voisZ_2_P.getDoubleValue(voisz2);
        ImageFloat gaussSmall = input.gaussianSmooth(voisx1, voisz1, nbCPUs);
        ImageFloat gaussLarge = input.gaussianSmooth(voisx2, voisz2, nbCPUs);
        if (debug) {
            gaussSmall.show("Subtract Gaussian:: SmoothXY:"+voisx1+" SmooothZ:"+voisz1);
            gaussLarge.show("Subtract Gaussian:: SmoothXY:"+voisx2+" SmooothZ:"+voisz2);
        }
        return gaussSmall.substractImage(gaussLarge);
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
        return "BandPass Filter by soustraction of two gaussian blurred images.";
    }
    
}
