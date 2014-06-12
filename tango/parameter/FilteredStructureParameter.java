package tango.parameter;

import mcib3d.image3d.ImageFloat;
import mcib3d.image3d.ImageHandler;
import mcib3d.image3d.ImageInt;
import tango.dataStructure.InputCellImages;
import tango.dataStructure.ObjectQuantifications;
import tango.dataStructure.SegmentedCellImages;

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

public class FilteredStructureParameter extends GroupParameter {
    StructureParameter structureSignal = new StructureParameter("Signal:", "structureSignal", -1, false);
    BooleanParameter useFiltered = new BooleanParameter("Use Filtered Image", "useFiltered", false);
    PreFilterSequenceParameter preFilters = new PreFilterSequenceParameter("Pre-Filters", "preFilters");
    
    public FilteredStructureParameter(String label, String id) {
        super(label, id);
        this.setParameters(new Parameter[]{structureSignal, useFiltered, preFilters});
    }
    
    public int getIndex() {
        return structureSignal.getIndex();
    }
    
    public ImageHandler getImage(InputCellImages raw, boolean verbose, int nCPUs) {
        ImageHandler intensityMap = (useFiltered.isSelected()) ? raw.getFilteredImage(structureSignal.getIndex()) : raw.getImage(structureSignal.getIndex());
        intensityMap=preFilters.runPreFilterSequence(structureSignal.getIndex(), intensityMap, raw, nCPUs, verbose);
        if (verbose) intensityMap.show("intensityMap");
        return intensityMap;
    }
}
