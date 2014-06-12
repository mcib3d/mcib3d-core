package tango.plugin.filter;

import mcib3d.utils.exceptionPrinter;
import ij.IJ;
import ij.ImagePlus;
import ij.gui.GenericDialog;
import ij.plugin.PlugIn;
import java.util.HashMap;
import mcib3d.image3d.ImageByte;
import mcib3d.image3d.ImageHandler;
import mcib3d.image3d.ImageInt;
import mcib3d.image3d.processing.BinaryMorpho;
import tango.dataStructure.InputImages;
import tango.parameter.*;
import tango.plugin.filter.PostFilter;

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
public class BinaryOpen implements PostFilter, PlugIn{
    // TODO utiliser une methode classique si le rayon est petit
    boolean debug;
    int nbCPUs=1;
    DoubleParameter radiusXY = new DoubleParameter("XY-radius: ", "radiusXY", 1d, Parameter.nfDEC1);
    DoubleParameter radiusZ = new DoubleParameter("Z-radius: ", "radiusZ", 1d, Parameter.nfDEC1);
    BooleanParameter useScale=new BooleanParameter("Use Image Scale for Z radius: ", "useScale", true);
    HashMap<Object, Parameter[]> map = new HashMap<Object, Parameter[]>(){{
        put(false, new Parameter[]{radiusZ}); 
        put(true, new Parameter[0]);
    }};
    ConditionalParameter cond = new ConditionalParameter(useScale, map);
    Parameter[] parameters = new Parameter[] {radiusXY, cond};

    public BinaryOpen() {
        radiusXY.setHelp("Radius in XY direction (pixels)", true);
        radiusZ.setHelp("Radius in Z direction (pixels)", true);
        useScale.setHelp("If selected, the radius in Z direction will be computed according to the image anisotropy", true);
        useScale.setHelp("If selected, radiusZ = radiusXY * scaleXY / scaleZ", false);
    }
    
    @Override
    public Parameter[] getParameters() {
        return parameters;
    }

    @Override
    public void setVerbose(boolean debug) {
        this.debug=debug;
    }

    @Override
    public ImageInt runPostFilter(int currentStructureIdx, ImageInt input, InputImages images) {
        try {
            float radXY=Math.max(radiusXY.getFloatValue(1), 1);
            float radZ;
            if (useScale.isSelected()) radZ=Math.max(radXY*(float)(input.getScaleXY()/input.getScaleZ()), 1);
            else radZ=radiusZ.getFloatValue(1);
            if (debug) {
                IJ.log("binaryOpen: radius XY"+radXY+ " radZ:"+radZ);
            }
            
            return BinaryMorpho.binaryOpenMultilabel(input, radXY, radZ, nbCPUs);
        } catch (Exception e) {
            exceptionPrinter.print(e,"", true);
        } return null;
    }

    @Override
    public void setMultithread(int nbCPUs) {
        this.nbCPUs=nbCPUs;
    }
    
    @Override
    public void run(String arg) {
        ImagePlus imp = IJ.getImage();
        if (imp==null || imp.getBitDepth()!=8) {
            IJ.log("8-bit thresholded image");
            return;
        }
        IJ.showStatus("binaryOpen");
        GenericDialog gd = new GenericDialog("BinaryOpen");
        gd.addNumericField("radiusXY:", 5, 1);
        gd.addNumericField("radiusZ:", 3, 1);
        gd.showDialog();
        if (gd.wasOKed()) {
            double radXY=gd.getNextNumber();
            double radZ=gd.getNextNumber();
            this.radiusXY.setValue(radXY);
            this.radiusZ.setValue(radZ);
            ImageHandler res=runPostFilter(0, (ImageInt)ImageHandler.wrap(imp), null);
            res.show(imp.getTitle()+"::open");
        }
    }
    
    @Override
    public String getHelp() {
        return "morphological opening using distance maps, optimized for large radius. \nWorks on binary masks (no border effects)";
    }
    
}
