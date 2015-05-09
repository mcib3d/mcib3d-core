package tango.parameter;

import java.awt.*;
import java.util.*;
import java.io.*;
import javax.swing.*;
import java.text.*;
import com.mongodb.DBObject;
import com.mongodb.BasicDBObject;
import ij.gui.GenericDialog;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import mcib3d.utils.exceptionPrinter;
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
public abstract class NumberParameter extends Parameter {

    protected JFormattedTextField number;
    protected NumberFormat nf;

    public NumberParameter(String label, String id, Double value, NumberFormat nf) {
        super(label, id);
        this.number = new JFormattedTextField(nf);
        number.setPreferredSize(new Dimension(100, 20));
        number.setMinimumSize(new Dimension(80, 20));
        number.setMaximumSize(new Dimension(120, 20));
        this.box.add(this.number);
        this.nf = nf;
        if (value != null) {
            number.setValue(value);
        }
        setColor();
        addColorListener();
    }

    protected NumberParameter(String label, String id, NumberFormat nf) {
        super(label, id);
        this.number = new JFormattedTextField(nf);
        number.setPreferredSize(new Dimension(100, 20));
        number.setMinimumSize(new Dimension(80, 20));
        number.setMaximumSize(new Dimension(120, 20));
        this.box.add(this.number);
        this.nf = nf;
        addColorListener();
    }
    
    protected void addColorListener() {
        number.getDocument().addDocumentListener(
            new DocumentListener() {
                @Override
                public void changedUpdate(DocumentEvent documentEvent) {
                    setColor();
                }

                @Override
                public void insertUpdate(DocumentEvent e) {
                    setColor();
                }

                @Override
                public void removeUpdate(DocumentEvent e) {
                    setColor();
                }
            }
        );
    }

    @Override
    public void dbPut(DBObject DBO) {
        if (getValue() != null) {
            DBO.put(id, getValue());
        }
    }

    @Override
    public void dbGet(BasicDBObject DBO) {
        if (DBO.containsField(id)) {
            setValue(DBO.get(id));
        }
        setColor();
    }

    public void setValue(Object value) {
        number.setValue(value);
        setColor();
    }

    public Number getValue() {
        try {
            number.commitEdit();
        } catch (Exception e) {
            //exceptionPrinter.print(e, "");
            return null;
        }
        Object val = number.getValue();
        if (val != null) {
            return (Number) val;
        } else {
            return null;
        }
    }

    /*
     * public float getFloatValue() {
     *
     * Object value = getValue(); if (value instanceof Long) { return ((Long)
     * value).floatValue(); } else if (value instanceof Double) { return
     * ((Double) number.getValue()).floatValue(); } else if (value instanceof
     * Integer) { return ((Integer) number.getValue()).floatValue(); } else {
     * return (Float) number.getValue(); } }
     *
     * public int getIntValue() { Object value = getValue(); if (value
     * instanceof Long) { return ((Long) value).intValue(); } else if (value
     * instanceof Double) { return ((Double) number.getValue()).intValue(); }
     * else if (value instanceof Float) { return ((Float)
     * number.getValue()).intValue(); } else { return (Integer)
     * number.getValue(); } }
     *
     */
    public int getNbDecimal() {
        return nf.getMinimumFractionDigits();
    }

    @Override
    public void addToGenericDialog(GenericDialog gd) {
        Number n = this.getValue();
        float def = n != null ? n.floatValue() : 0;
        gd.addNumericField(this.getLabel(), def, this.getNbDecimal());
    }
    
    @Override
    public boolean isValid() {
        if (!this.compulsary) return true;
        return getValue()!=null;
    }
    
    
}
