package tango.parameter;

import java.util.Set;
import tango.dataStructure.InputCellImages;
import tango.dataStructure.SegmentedCellImages;
import tango.dataStructure.StructureQuantifications;
import static tango.parameter.Parameter.setContent;
import tango.plugin.PluginFactory;
import tango.plugin.measurement.Measurement;
import tango.plugin.measurement.MeasurementObject2Object;
import tango.plugin.measurement.MeasurementStructure;

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

public class MeasurementO2OParameter extends MeasurementParameter {
    public MeasurementO2OParameter(String label, String id, String defMethod, Parameter[] defParameters) {
        super(label, id, defMethod, defParameters);
    }
    
    @Override     
    protected Set<String> getMeasurementList() {
        return PluginFactory.getMeasurementO2OList();
    }

    
    @Override
    public MeasurementObject2Object getMeasurement() {
        if (plugin!=null) return (MeasurementObject2Object)plugin;
        else return null;
    }
    
    @Override
    protected void getPlugin(String method) {
        plugin = PluginFactory.getMesurement_O2O(method);
    }
    
    public StructureQuantifications getMeasureStructure(InputCellImages rawImages, SegmentedCellImages segmentedImages) {
        if (plugin!=null) {
            if (plugin instanceof MeasurementStructure) {
                MeasurementStructure ms = (MeasurementStructure)plugin;
                int[] s = ms.getStructures();
                if (s!=null && s.length>0) {
                    StructureQuantifications res;
                    if (s.length==1 || s[0]==s[1]) {
                        res= new StructureQuantifications(segmentedImages.getObjects(s[0]).length);
                    } else {
                        res= new StructureQuantifications(segmentedImages.getObjects(s[0]).length, segmentedImages.getObjects(s[1]).length);
                    }
                    ms.getMeasure(rawImages, segmentedImages, res);
                    return res;
                }
            }
        }
        return null;
    }
    
}
