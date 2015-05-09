package tango.plugin.filter;

import imageware.Builder;
import imageware.ImageWare;
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
public class LaplacianOfGaussian3D implements PreFilter {
    boolean debug;
    int nbCPUs=1;
    double voisx=2;
    double voisz=voisx/2;
    DoubleParameter voisXY_P = new DoubleParameter("VoisXY (pix): ", "voisXY", (double) voisx, Parameter.nfDEC1);
    DoubleParameter voisZ_P = new DoubleParameter("VoisZ (pix): ", "voisZ", (double) voisz, Parameter.nfDEC1);
    BooleanParameter useScale = new BooleanParameter("Use Image Scale for Z radius: ", "useScale", true);
    ConditionalParameter cond = new ConditionalParameter(useScale);
    Parameter[] parameters;
    public LaplacianOfGaussian3D() {
        voisXY_P.setHelp("The radius in <em>X</em> and <em>Y</em> direction", true);
        voisZ_P.setHelp("The radius in <em>Z</em> direction", true);
        cond.setCondition(false, new Parameter[]{voisZ_P});
        parameters=new Parameter[]{voisXY_P, cond};
    }
    
    @Override
    public ImageHandler runPreFilter(int currentStructureIdx, ImageHandler input, InputImages images) {
        voisx=voisXY_P.getDoubleValue(voisx);
        if (useScale.isSelected()) voisz=voisx * images.getMask().getScaleXY() / images.getMask().getScaleZ();
        else voisz=voisZ_P.getDoubleValue(voisz);
        ImageHandler res = LOG(input, voisx, voisz);
        res.setTitle(input.getTitle() + "::LOG3D");
        return res;
    }

    public static ImageHandler LOG(ImageHandler imp, double radX, double radZ) {
        ImageWare in = Builder.create(imp.getImagePlus(), 3);
            mcib_plugins.processing.LoG3D localLoG3D = new mcib_plugins.processing.LoG3D(false);
            ImageWare res;
            if (imp.sizeZ > 1) {
                res = localLoG3D.doLoG(in, radX, radX, radZ);
            } else {
                res = localLoG3D.doLoG(in, radX, radX);
            }
            res.invert();
        return ImageHandler.wrap(res.buildImageStack());
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
        return "<ul><li><strong>LoG</strong>, laplacian of Gaussian, <br>taken from BIG http://bigwww.epfl.ch/sage/soft/LoG3D/ <br>. <br>When using this plugin, please cite : <br><br>D. Sage, F.R. Neumann, F. Hediger, S.M. Gasser, M. Unser, \"Automatic Tracking of Individual Fluorescence Particles: Application to the Study of Chromosome Dynamics,\" IEEE Transactions on Image Processing, vol. 14, no. 9, pp. 1372-1383, September 2005.<br> </li></ul>";
    }
    
}
