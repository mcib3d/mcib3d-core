package tango.plugin.thresholder;

import tango.parameter.DoubleParameter;
import tango.parameter.Parameter;
import ij.IJ;
import ij.ImagePlus;
import mcib3d.image3d.ImageHandler;
import tango.dataStructure.InputImages;
import tango.plugin.thresholder.Thresholder;
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

public class Value implements Thresholder {
    
    DoubleParameter value = new DoubleParameter("Value: ", "value", 1d, Parameter.nfDEC5);
    Parameter[] parameters=new Parameter[] {
        value
    };

    @Override
    public Parameter[] getParameters() {
        return parameters;
    }
    
    boolean debug;
    int nbCPUs=1;
    @Override
    public void setMultithread(int nCPUs) {
        this.nbCPUs=nCPUs;
    }

    @Override
    public void setVerbose(boolean debug) {
        this.debug=debug;
    }

    @Override
    public double runThresholder(ImageHandler input, InputImages images) {
        if (debug) IJ.log("Thresholder: value: "+value.getFloatValue(1));
        
        return value.getFloatValue(1);
    }

    @Override
    public String getHelp() {
        return "value";
    }

    
    
}
