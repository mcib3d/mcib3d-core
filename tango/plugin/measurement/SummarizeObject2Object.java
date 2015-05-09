package tango.plugin.measurement;

import java.util.HashMap;
import tango.dataStructure.InputCellImages;
import tango.dataStructure.ObjectQuantifications;
import tango.dataStructure.SegmentedCellImages;
import tango.dataStructure.StructureQuantifications;
import tango.parameter.KeyParameter;
import tango.parameter.KeyParameterObject;
import tango.parameter.KeyParameterStructureArrayO2O;
import tango.parameter.MeasurementO2OParameter;
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

public class SummarizeObject2Object implements MeasurementObject{
    
    int nCPUs=1;
    boolean verbose;
    
    MeasurementO2OParameter meas = new MeasurementO2OParameter("Measurement", "measurement", "Generic Distance", null);
    MeasurementObject2Object currentMeas;
    KeyParameterObject[] keys;
    
    public SummarizeObject2Object() {
        meas.setFireChangeOnAction();
    }
    
    protected MeasurementObject2Object getMeasurement() {
        MeasurementObject2Object m = meas.getMeasurement();
        if (m!=null && !m.getClass().equals(currentMeas)) currentMeas=m;
        return currentMeas;
    }
    
    protected boolean measurementHasChanged() {
        MeasurementObject2Object m = meas.getMeasurement();
        return m!=null && !m.getClass().equals(currentMeas);
    }
    
    @Override
    public int getStructure() {
        MeasurementObject2Object m = getMeasurement();
        if (m!=null) {
            return m.getStructures()[0];
        } else return -1;
    }

    @Override
    public void getMeasure(InputCellImages rawImages, SegmentedCellImages segmentedImages, ObjectQuantifications quantifications) {
        MeasurementObject2Object m  = getMeasurement();
        if (m!=null) {
            int[] structs = m.getStructures();
            StructureQuantifications s;
            if (structs.length==1 || structs[0]==structs[1]) s= new StructureQuantifications(segmentedImages.getObjects(structs[0]).length);
            else s = new StructureQuantifications(segmentedImages.getObjects(structs[0]).length, segmentedImages.getObjects(structs[1]).length);
            m.getMeasure(rawImages, segmentedImages, s);
            HashMap<String, Object> res = s.getQuantifStructure();
            int count = 0;
            for (KeyParameterStructureArrayO2O k020 : m.getKeys()) {
                Object o = res.get(k020.getKey());
                if (o instanceof double[]) {
                    double[] resD = (double[])o;
                    // TODO summarize along first structure
                }
            }
        }
    }

    @Override
    public KeyParameter[] getKeys() {
        if (measurementHasChanged()) {
            MeasurementObject2Object m  = getMeasurement();
            if (m==null) return new KeyParameterObject[0];
            KeyParameterStructureArrayO2O[] k020s = m.getKeys();
            keys = new KeyParameterObject[k020s.length];
            int count = 0;
            for (KeyParameterStructureArrayO2O k : k020s) {
                keys[++count] = k.getKeyParameterObjectNumber();
            }
            return keys;
        }  else return new KeyParameterObject[0];
    }

    @Override
    public Parameter[] getParameters() {
        return new Parameter[]{meas};
    }

    @Override
    public String getHelp() {
        return "Summarizes a measurement along the first structure";
    }

    @Override
    public void setVerbose(boolean verbose) {
        this.verbose=verbose;
    }

    @Override
    public void setMultithread(int nCPUs) {
        this.nCPUs=nCPUs;
    }

}
