package tango.plugin.filter;

import imagescience.image.Image;
import imagescience.transform.Translate;
import mcib3d.image3d.ImageHandler;
import tango.dataStructure.InputImages;
import tango.parameter.ChoiceParameter;
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
public class Translate3D implements PreFilter {
    private static final String[] schemes = {
		"nearest neighbor",
		"linear",
		"cubic convolution",
		"cubic B-spline",
		"cubic O-MOMS",
		"quintic B-spline"
	};
    protected DoubleParameter x = new DoubleParameter("x-translation (pixels)", "x", 0.0d, DoubleParameter.nfDEC5);
    protected DoubleParameter y = new DoubleParameter("y-translation (pixels)", "y", 0.0d, DoubleParameter.nfDEC5);
    protected DoubleParameter z = new DoubleParameter("z-translation (slices)", "z", 0.0d, DoubleParameter.nfDEC5);
    protected ChoiceParameter interpolation = new ChoiceParameter("Interpolation scheme:", "interpolation", schemes, schemes[1]);
    protected DoubleParameter bcg = new DoubleParameter("background value:", "bcg", 0.0d, DoubleParameter.nfDEC5);
    protected Parameter[] parameters = new Parameter[]{x, y, z, interpolation, bcg};
    boolean verbose;
    int nCPUs=1;
    @Override
    public ImageHandler runPreFilter(int currentStructureIdx, ImageHandler input, InputImages images) {
        final Image img = Image.wrap(input.getImagePlus());
        final Translate translator = new Translate();
        double xs=x.getDoubleValue(0);
        double ys=y.getDoubleValue(0);
        double zs=z.getDoubleValue(0);
        double bg=bcg.getDoubleValue(0);
        translator.background = bg;
        int ischeme = Translate.NEAREST;
        int scheme=interpolation.getSelectedIndex();
        switch (scheme) {
                case 0: ischeme = Translate.NEAREST; break;
                case 1: ischeme = Translate.LINEAR; break;
                case 2: ischeme = Translate.CUBIC; break;
                case 3: ischeme = Translate.BSPLINE3; break;
                case 4: ischeme = Translate.OMOMS3; break;
                case 5: ischeme = Translate.BSPLINE5; break;
        }
        final Image newimg = translator.run(img,xs,ys,zs,ischeme);
        return ImageHandler.wrap(newimg.imageplus());
    }

    @Override
    public Parameter[] getParameters() {
        return parameters;
    }

    @Override
    public String getHelp() {
        return "From TransformJ plugin by Erik Meijering. "
                +" Install imagescience to use this filter "
                + "Please see http://www.imagescience.org/meijering/software/transformj/translate.html";
    }

    @Override
    public void setVerbose(boolean verbose) {
        this.verbose=verbose;
    }

    @Override
    public void setMultithread(int nCPUs) {
        
    }
    
}
