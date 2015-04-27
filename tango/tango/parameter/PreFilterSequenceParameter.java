package tango.parameter;

import ij.ImagePlus;
import java.util.ArrayList;
import mcib3d.image3d.ImageHandler;
import mcib3d.utils.exceptionPrinter;
import tango.dataStructure.InputImages;
import tango.gui.Core;

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
public class PreFilterSequenceParameter extends MultiParameter {
    
    public PreFilterSequenceParameter(String label, String id) {
        super(label, id, new Parameter[]{new PreFilterParameter("filter", id, null)}, 0, 100, 0);
    }
    
    public PreFilterSequenceParameter(String label, String id, int defaultNumber, String defaultFilter, Parameter[] defaultParameters) {
        super(label, id, new Parameter[]{new PreFilterParameter("filter", id, defaultFilter, defaultParameters)}, 0, 100, defaultNumber);
    }
    
    public ImageHandler runPreFilterSequence(int currentStructureIdx, ImageHandler in, InputImages images, int nbCPUs, boolean verbose) {
        ImageHandler current = in;
        for (Parameter[] al : getParametersArrayList()) {
            PreFilterParameter pf = (PreFilterParameter)al[0];
            current = pf.preFilter(currentStructureIdx, current, images, nbCPUs, verbose);
            current.setScale(in);
            current.setOffset(in);
        }
        return current;
    }
}
