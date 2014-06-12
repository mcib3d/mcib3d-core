package tango.util;

import java.util.HashMap;
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
public class IJ3dViewerParameters {
    BooleanParameter display;
    IntParameter resamplingFactor;
    SliderDoubleParameter transparancy;
    // type : volume, surface.. 
    BooleanParameter shade;
    SliderParameter smooth;
    ConditionalParameter ij3d;
    Parameter[] parameters;
    public IJ3dViewerParameters(boolean nucleus) {
        display = new BooleanParameter("3D-Display", "display", true);
        resamplingFactor = new IntParameter("Resampling factor", "resampling", nucleus?6:1);
        transparancy = new SliderDoubleParameter("Transparency", "transparency", 0, 1, nucleus?0.85:0.3, 2);
        shade = new BooleanParameter("Shade Surface", "shade", !nucleus);
        smooth = new SliderParameter("3D-smooth: iterations:", "smooth", 0, 20, nucleus?6:2);
        parameters = new Parameter[]{resamplingFactor, transparancy, shade, smooth};
        HashMap<Object, Parameter[]> map = new HashMap<Object, Parameter[]>(){{
            put(false, new Parameter[]{}); 
            put(true, parameters);
        }};
        ij3d = new ConditionalParameter(display, map);
        ij3d.toggleVisibility(false);
    }
    
    public Parameter getParameter() {
        return ij3d;
    }
    
    public int getResamplingFactor() {
        return resamplingFactor.getIntValue(1);
    }
    
    public boolean getShade() {
        return this.shade.isSelected();
    }
    
    public double getTransparancy() {
        return transparancy.getValue();
    }
    
    public int getSmooth() {
        return this.smooth.getValue();
    }
    
    public boolean getDisplay() {
        return display.isSelected();
    }
}
