package tango.dataStructure;

import java.util.HashMap;
import tango.parameter.KeyParameterObjectNumber;

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

public class ObjectQuantifications {
    HashMap<String, Object> measurementObject;
    int nbObjects;

    public ObjectQuantifications() {
    }
    public ObjectQuantifications(int nbObjects) {
        measurementObject = new HashMap<String, Object>();
        this.nbObjects=nbObjects;
    }
    
    public void setQuantificationObjectNumber(KeyParameterObjectNumber key, double[] values) {
        if (values.length==nbObjects) measurementObject.put(key.getKey(), values);
        else ij.IJ.log("Wrong objects number for key"+key.getKey()+ " "+values.length+" instead of:"+nbObjects);
    }
    
    public void setQuantificationObjectNumber(KeyParameterObjectNumber key, int[] values) {
        if (values.length==nbObjects) measurementObject.put(key.getKey(), values);
        else ij.IJ.log("Wrong objects number for key"+key.getKey()+ " "+values.length+" instead of:"+nbObjects);
    }
    
    public HashMap<String, Object> getQuantifObject() {
        return measurementObject;
    }
    
}
