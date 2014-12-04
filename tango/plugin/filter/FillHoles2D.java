package tango.plugin.filter;

import mcib3d.utils.exceptionPrinter;
import java.util.TreeMap;
import mcib3d.image3d.ImageByte;
import mcib3d.image3d.ImageHandler;
import mcib3d.image3d.ImageInt;
import tango.dataStructure.InputImages;
import tango.parameter.*;

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
    int nbCPUs = 1;

    BooleanParameter useInterior = new BooleanParameter("Use interior as object", "useInterior", false);
    Parameter[] parameters = new Parameter[]{useInterior};

    public FillHoles2D() {
        useInterior.setHelp("If checked, the segmented object will only be the filled interior, useful for membrane labelling.", true);
    }

    @Override
    public Parameter[] getParameters() {
        return parameters;
    }

    @Override
    public void setVerbose(boolean debug) {
        this.debug = debug;
    }

    @Override
    public ImageInt runPostFilter(int currentStructureIdx, ImageInt input, InputImages images) {
        try {
            ImageInt fill = input;
            TreeMap<Integer, int[]> bounds = input.getBounds(true);
            if (bounds.size() > 1) {
                ImageByte[] masks = input.crop3DBinary(bounds);
                for (ImageByte mask : masks) {
                    tango.util.FillHoles2D.fill(mask, 255, 0);
                }
                fill = ImageHandler.merge3DBinary(masks, input.sizeX, input.sizeY, input.sizeZ);
            } else if (bounds.size() == 1) {
                ImageByte ib = new ImageByte(input, true);
                tango.util.FillHoles2D.fill(ib, 255, 0);
                fill = ib;
            }
            //  interior = filled - original
            if (useInterior.isSelected()) {
                fill = fill.substractImage(input);
            }
            return fill;
        } catch (Exception e) {
            exceptionPrinter.print(e, "", true);
        }
        return input;
    }

    @Override
    public void setMultithread(int nbCPUs) {
        this.nbCPUs = nbCPUs;
    }

    @Override
    public String getHelp() {
        return "2D fill holes from ImageJ. Algorithm by Gabriel Landini";
    }

}
