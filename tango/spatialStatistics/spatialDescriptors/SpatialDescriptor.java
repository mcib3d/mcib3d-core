package tango.spatialStatistics.spatialDescriptors;

import ij.ImagePlus;
import java.util.HashMap;
import mcib3d.geom.Object3D;
import tango.dataStructure.InputCellImages;
import tango.dataStructure.SegmentedCellImages;
import tango.dataStructure.StructureQuantifications;
import tango.parameter.Parameter;
import tango.plugin.TangoPlugin;
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
public interface SpatialDescriptor extends TangoPlugin {
    // attention: handle the case when a sample is null
    // returns null if at least one sample is null
    public void run(int nbSamples, InputCellImages raw, SegmentedCellImages seg);
    public double[] getObservedDescriptor();
    public double[][] getSampleDescriptor();
    public void getCurves(StructureQuantifications quantifs);
    public int[] getStructures();
    public Parameter[] getKeyParameters();
}
