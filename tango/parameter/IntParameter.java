package tango.parameter;

import java.awt.*;
import java.util.*;
import java.io.*;
import javax.swing.*;
import java.text.*;
import com.mongodb.DBObject;
import com.mongodb.BasicDBObject;
import ij.IJ;
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
public class IntParameter extends NumberParameter {
    public final static NumberFormat nfINT = NumberFormat.getIntegerInstance();

    public IntParameter(String label, String id, Integer value) {
        super(label, id, nfINT);
        if (value != null) {
            number.setValue(value);
        }
        setColor();
    }

    @Override
    public Parameter duplicate(String newLabel, String newId) {
        Integer value = null;
        //Class C=number.getValue().getClass();
        //IJ.log("formatter "+number.getFormatter()+" "+C.getSimpleName()+" "+number.getValue());
        if (number.getValue() != null) {
            // Pb conversions Long (?)
            try {
                value = Integer.parseInt(""+number.getValue());
            } catch (NumberFormatException e) {
                exceptionPrinter.print(e, "duplicate interger. parameter:"+newLabel+ " id:"+newId, Core.GUIMode);
            }
        }
        return new IntParameter(newLabel, newId, value);
    }

    @Override
    public void dbPut(DBObject DBO) {
        if (getValue() != null) {
            DBO.put(id, getIntValue(0));
        }
    }

    @Override
    public void dbGet(BasicDBObject DBO) {
        if (DBO.containsField(id) && DBO.get(id)!=null) {
            number.setValue(DBO.getInt(id));
        }
        setColor();
    }

    public int getIntValue(int defaultValue) {
        Number value = getValue();
        if (value!=null) return value.intValue();
        else return defaultValue;
    }
    public long getLongValue(long defaultValue) {
        Number value = getValue();
        if (value!=null) return value.longValue();
        else return defaultValue;
    }
    public short getShortValue(short defaultValue) {
        Number value = getValue();
        if (value!=null) return value.shortValue();
        else return defaultValue;
    }
    
    @Override
    public boolean sameContent(Parameter p) {
        if (p instanceof IntParameter) {
            if ((((IntParameter)p).getValue()!=null && ((IntParameter)p).getValue().equals(getValue())) || (((IntParameter)p).getValue()==null && getValue()==null)) return true;
        }
        return false;
    }
    
    @Override
    public void setContent(Parameter p) {
        if (p instanceof IntParameter) {
            Number n = ((IntParameter)p).getValue();
            if (n!=null) this.setValue(n);
        }
    }
}
