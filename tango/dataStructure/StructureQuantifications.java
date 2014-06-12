package tango.dataStructure;

import java.util.HashMap;
import tango.parameter.KeyParameterObjectNumber;
import tango.parameter.KeyParameterStructureArray;
import tango.parameter.KeyParameterStructureArrayO2O;
import tango.parameter.KeyParameterStructureNumber;

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

public class StructureQuantifications {
    HashMap<String, Object> measurementStructure;
    int O2Onumber;
    
    public StructureQuantifications(int nbObjects) { // same structure
        measurementStructure = new HashMap<String, Object>();
        O2Onumber = nbObjects * (nbObjects-1) / 2;
    }
    
    public StructureQuantifications(int nbObjectsS1, int nbObjectsS2) { // 2 different structures
        measurementStructure = new HashMap<String, Object>();
        O2Onumber = nbObjectsS1 * nbObjectsS2;
    }
    
    public void setQuantificationStructureArrayO2O(KeyParameterStructureArrayO2O key, double[] values) {
        if (values.length==O2Onumber) measurementStructure.put(key.getKey(), values);
        else ij.IJ.log("Wrong values number for key: "+key.getKey()+ " "+values.length+" instead of:"+O2Onumber);
    }
    
    public void setQuantificationStructureArray(KeyParameterStructureArray key, double[] values) {
        measurementStructure.put(key.getKey(), values);
    }
    
    public void setQuantificationStructureArray(KeyParameterStructureArray key, int[] values) {
        measurementStructure.put(key.getKey(), values);
    }
    
    public void setQuantificationStructureNumber(KeyParameterStructureNumber key, double value) {
        measurementStructure.put(key.getKey(), value);
    }
    
    public HashMap<String, Object> getQuantifStructure() {
        return measurementStructure;
    }
}
