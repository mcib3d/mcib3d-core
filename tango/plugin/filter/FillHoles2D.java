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
public class FillHoles2D implements PostFilter {
    boolean debug;
    int nbCPUs=1;
    
    Parameter[] parameters = new Parameter[] {};
    public FillHoles2D() {
        
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
                    tango.util.FillHoles2D.fill(masks[i], 255, 0);
                }
                return ImageHandler.merge3DBinary(masks, input.sizeX, input.sizeY, input.sizeZ);
            } else if (bounds.size()==1){
                ImageByte ib = new ImageByte(input, true);
                tango.util.FillHoles2D.fill(ib, 255, 0);
                return ib;
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
        return "2D fill holes from ImageJ. Algorithm by Gabriel Landini";
    }
    
}
