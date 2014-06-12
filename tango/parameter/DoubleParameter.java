package tango.parameter;

import java.awt.*;
import java.util.*;
import java.io.*;
import javax.swing.*;
import java.text.*;
import com.mongodb.DBObject;
import com.mongodb.BasicDBObject;
import ij.gui.GenericDialog;
import mcib3d.utils.exceptionPrinter;
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
public class DoubleParameter extends NumberParameter {

    public DoubleParameter(String label, String id, Double value, NumberFormat nf) {
        super(label, id, nf);
        if (value != null) {
            number.setValue(value);
        }
        setColor();
    }

    @Override
    public Parameter duplicate(String newLabel, String newId) {
        Double value = null;
        if (number.getValue() != null) {
            // Pb conversions Long (?)
            try {
                value = Double.parseDouble(""+number.getValue());
            } catch (NumberFormatException e) {
                exceptionPrinter.print(e, "duplicate double. parameter:"+newLabel+ " id:"+newId, Core.GUIMode);
            }
        }
        return new DoubleParameter(newLabel, newId, value, nf);
    }

    @Override
    public void dbPut(DBObject DBO) {
        if (getValue() != null) {
            DBO.put(id, getValue());
        }
    }

    @Override
    public void dbGet(BasicDBObject DBO) {
        if (DBO.containsField(id) && DBO.get(id)!=null) {
            number.setValue(DBO.getDouble(id));
        }
        setColor();
    }
    
    public double getDoubleValue(double defaultValue) {
        Number val = getValue();
        if (val!=null) return val.doubleValue();
        else return defaultValue;
    }
    
    public float getFloatValue(float defaultValue) {
        Number val = getValue();
        if (val!=null) return val.floatValue();
        else return defaultValue;
    }
    
    @Override
    public boolean sameContent(Parameter p) {
        if (p instanceof DoubleParameter) {
            if ((((DoubleParameter)p).getValue()!=null && ((DoubleParameter)p).getValue().equals(getValue())) || (((DoubleParameter)p).getValue()==null && getValue()==null)) return true;
        }
        return false;
    }
    
    @Override
    public void setContent(Parameter p) {
        if (p instanceof DoubleParameter) {
            Number n = ((DoubleParameter)p).getValue();
            if (n!=null) this.setValue(n);
        }
    }
    
}
