package tango.plugin.filter;

import mcib3d.image3d.ImageHandler;
import mcib3d.image3d.ImageInt;
import tango.dataStructure.InputImages;
import tango.parameter.Parameter;
import tango.parameter.SliderDoubleParameter;

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
public class Normalize implements PreFilter {
    boolean debug;
    int nbCPUs=1;
    SliderDoubleParameter satu = new SliderDoubleParameter("% of saturation", "saturation", 0, 100, 0, 4);
    
    public Normalize() {
        satu.setHelp("Percentage of saturated pixels. 0 for no saturation. a low value can avoid the effect of extreme intensity values", true);
    }
    
    @Override
    public ImageHandler runPreFilter(int currentStructureIdx, ImageHandler input, InputImages images) {
        return input.normalize(images.getMask(), satu.getValue()/100d);
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
        return new Parameter[]{satu};
    }

    @Override
    public String getHelp() {
        return "Normalize with a percentage of saturation";
    }
    
}
