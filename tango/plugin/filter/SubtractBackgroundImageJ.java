package tango.plugin.filter;

import ij.plugin.filter.BackgroundSubtracter;
import ij.process.ImageProcessor;
import mcib3d.image3d.ImageFloat;
import mcib3d.image3d.ImageHandler;
import mcib3d.image3d.ImageInt;
import tango.dataStructure.InputImages;
import tango.parameter.BooleanParameter;
import tango.parameter.ChoiceParameter;
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
public class SubtractBackgroundImageJ implements PreFilter {
    boolean debug;
    int nbCPUs=1;
    double voisx=50;
    DoubleParameter voisXY_P = new DoubleParameter("Radius (pix): ", "voisXY", (double) voisx, Parameter.nfDEC1);
    ChoiceParameter method = new ChoiceParameter("Method", "subMethod", new String[]{"Rolling Ball", "Sliding Paraboloïd"}, "Rolling Ball");
    BooleanParameter doPreSmooth = new BooleanParameter("Perform Pre-smooth", "preSmooth", true);
    Parameter[] parameters;
    public SubtractBackgroundImageJ() {
        voisXY_P.setHelp("Radius of the rolling ball creating the background (actually paraboloid of rotation with the same curvature)", true);
        doPreSmooth.setHelp("Whether the image should be smoothened (3x3 mean) before creating the background. With smoothing, the background will not necessarily be below the image data.", true);
        //corner.setHelp("Whether the algorithm should try to detect corner particles to avoid subtracting them as a background", true);
        
        parameters=new Parameter[]{voisXY_P, method, doPreSmooth};
    }
    
    @Override
    public ImageHandler runPreFilter(int currentStructureIdx, ImageHandler input, InputImages images) {
        //ij.IJ.log("run subtractBackground:");
        voisx=voisXY_P.getDoubleValue(voisx);
        ImageHandler res = input; //.duplicate();
        BackgroundSubtracter bs = new BackgroundSubtracter();
        for (int z = 0; z<res.sizeZ; z++) {
            //ij.IJ.log("slice:"+z);
            ImageProcessor ip = res.getImageStack().getProcessor(z+1);
            bs.rollingBallBackground(ip, voisx, false, false, false, method.getSelectedIndex()==1, true);
        }
        return res;
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
        return "ImageJ's 2D Background subtraction plugin./** Implements ImageJ's Subtract Background command. Based on the concept of the\n" +
"rolling ball algorithm described in Stanley Sternberg's article, \"Biomedical Image\n" +
"Processing\", IEEE Computer, January 1983.\n" +
"\n" +
"Imagine that the 2D grayscale image has a third (height) dimension by the image\n" +
"value at every point in the image, creating a surface. A ball of given radius is\n" +
"rolled over the bottom side of this surface; the hull of the volume reachable by\n" +
"the ball is the background.\n" +
"\n" +
"With \"Sliding Parabvoloid\", the rolling ball is replaced by a sliding paraboloid\n" +
"of rotation with the same curvature at its apex as a ball of a given radius.\n" +
"A paraboloid has the advantage that suitable paraboloids can be found for any image\n" +
"values, even if the pixel values are much larger than a typical object size (in pixels).\n" +
"The paraboloid of rotation is approximated as parabolae in 4 directions: x, y and\n" +
"the two 45-degree directions. Lines of the image in these directions are processed\n" +
"by sliding a parabola against them. Obtaining the hull needs the parabola for a\n" +
"given direction to be applied multiple times (after doing the other directions);\n" +
"in this respect the current code is a compromise between accuracy and speed.\n" +
"\n" +
"For noise rejection, with the sliding paraboloid algorithm, a 3x3 maximum of the\n" +
"background is applied. With both, rolling ball and sliding paraboloid,\n" +
"the image used for calculating the background is slightly smoothened (3x3 average).\n" +
"This can result in negative values after background subtraction. This preprocessing\n" +
"can be disabled.\n" +
"\n" +
"In the sliding paraboloid algorithm, additional code has been added to avoid\n" +
"subtracting corner objects as a background (note that a paraboloid or ball would\n" +
"always touch the 4 corner pixels and thus make them background pixels).\n" +
"This code assumes that corner particles reach less than 1/4 of the image size\n" +
"into the image.\n" +
"\n" +
"Rolling ball code based on the NIH Image Pascal version by Michael Castle and Janice \n" +
"Keller of the University of Michigan Mental Health Research Institute.\n" +
"Sliding Paraboloid by Michael Schmid, 2007.";
    }
    
}
