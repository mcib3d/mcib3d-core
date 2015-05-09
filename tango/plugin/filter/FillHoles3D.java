package tango.plugin.filter;

import mcib3d.utils.exceptionPrinter;
import ij.IJ;
import ij.ImagePlus;
import ij.gui.GenericDialog;
import ij.plugin.PlugIn;
import java.util.HashMap;
import java.util.TreeMap;
import mcib3d.image3d.ImageByte;
import mcib3d.image3d.ImageHandler;
import mcib3d.image3d.ImageInt;
import mcib3d.image3d.ImageShort;
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
public class FillHoles3D implements PostFilter {
    boolean debug;
    int nbCPUs=1;
    
    Parameter[] parameters = new Parameter[] {};
    public FillHoles3D() {
        
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
            TreeMap<Integer, int[]> bounds = input.getBounds(true);
            if (bounds.size()>1) {
                ImageByte[] masks = input.crop3DBinary(bounds);
                for (int i = 0; i<masks.length; i++) {
                    mcib3d.image3d.processing.FillHoles3D.process(masks[i], 255, nbCPUs, debug);
                }
                return ImageHandler.merge3DBinary(masks, input.sizeX, input.sizeY, input.sizeZ);
            } else {
                int label = bounds.firstKey();
                if (input instanceof ImageShort) {
                    mcib3d.image3d.processing.FillHoles3D.process((ImageShort)input, label, nbCPUs, debug);
                } else if (input instanceof ImageByte) {
                    
                    mcib3d.image3d.processing.FillHoles3D.process((ImageByte)input, label, nbCPUs, debug);
                }
            }
            
            
        } catch (Exception e) {
            exceptionPrinter.print(e,"", true);
        } return input;
    }

    @Override
    public void setMultithread(int nbCPUs) {
        this.nbCPUs=nbCPUs;
    }


    @Override
    public String getHelp() {
        return "3D fill holes";
    }
    
}
