/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package tango.plugin.filter;

import mcib3d.image3d.ImageFloat;
import mcib3d.image3d.ImageHandler;
import mcib3d.image3d.ImageInt;
import tango.dataStructure.InputImages;
import tango.parameter.BooleanParameter;
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
public class Scale implements PreFilter {
    boolean debug;
    int nbCPUs=1;
    BooleanParameter center = new BooleanParameter("Subtract Mean value", "center", false);
    BooleanParameter standardDeviation = new BooleanParameter("Divide by standard deviation", "sd", false);
    
    public Scale() {
        center.setHelp("If selected, subtracts mean value (within mask) to all pixels", false);
        standardDeviation.setHelp("If selected, divides all pixels by standard deviation (within mask); otherwise divide by root mean square (within mask)", false);
    }
    @Override
    public ImageHandler runPreFilter(int currentStructureIdx, ImageHandler input, InputImages images) {
        ImageFloat res = new ImageFloat(input.getTitle()+"::scaled", input.sizeX, input.sizeY, input.sizeZ);
        double min = center.isSelected()?input.getImageStats(images.getMask()).getMean():0;
        double coeff = standardDeviation.isSelected()?input.getImageStats(images.getMask()).getStandardDeviation():input.getImageStats(images.getMask()).getRootMeanSquare();
        for (int z = 0; z<input.sizeZ; z++) {
            for (int xy=0; xy<input.sizeXY; xy++) {
                res.pixels[z][xy]=(float)(input.getPixel(xy, z)/coeff-min);
            }
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
        return new Parameter[]{center, standardDeviation};
    }

    @Override
    public String getHelp() {
        return "Scales image";
    }
    
}
