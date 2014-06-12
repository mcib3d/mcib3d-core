package tango.parameter;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import ij.gui.GenericDialog;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Locale;
import javax.swing.*;
import javax.swing.border.BevelBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
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
public class SliderDoubleParameter extends Parameter implements ChangeListener {

    JSlider slider;
    JFormattedTextField currentValue;
    double min, max, decimals;
    DecimalFormat df;
    boolean editing=false;
    public SliderDoubleParameter(String label, String id, double minValue, double maxValue, double value, double decimals) {
        super(label, id);
        this.min = minValue;
        this.max = maxValue;
        this.decimals = decimals;
        double coeff = Math.pow(10, decimals);
        int min_ = (int) (min * coeff);
        int max_ = (int) (max * coeff + 0.5);
        slider = new JSlider(min_, max_, max_);
        slider.addChangeListener(this);
        box.add(slider);
        String pattern = "0.0";
        for (int i = 1; i < decimals; i++) {
            pattern = pattern + "0";
        }
        df = new DecimalFormat(pattern);
        df.setDecimalFormatSymbols(DecimalFormatSymbols.getInstance(Locale.ENGLISH));
        currentValue = new JFormattedTextField(df);
        //currentValue.setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED));
        currentValue.setPreferredSize(new Dimension(pattern.length() * 10, 20));
        box.add(currentValue);
        setSliderValue(value);
        setColor();
        currentValue.addPropertyChangeListener("value",new PropertyChangeListener(){
            @Override
            public void propertyChange(PropertyChangeEvent e) {
                Number val = (Number)currentValue.getValue();
                if (val!=null){
                    editing=true;
                    double v = val.doubleValue();
                    if (v<min) {
                        setSliderValue(min);
                        java.awt.EventQueue.invokeLater(new Runnable() {
                            @Override
                            public void run() {
                                currentValue.setValue(min);
                            }
                        });
                    } else if (v>max) {
                        setSliderValue(max);
                        java.awt.EventQueue.invokeLater(new Runnable() {
                            @Override
                            public void run() {
                                currentValue.setValue(max);
                            }
                        });
                    } else setSliderValue(v);
                    if (template != null) setColor();
                    editing=false;
                }
            }
        });

    }

    @Override
    public void dbPut(DBObject DBO) {
        DBO.put(id, getValue());
    }

    @Override
    public void dbGet(BasicDBObject DBO) {
        if (DBO.containsField(id) && DBO.get(id) != null) {
            double value = DBO.getDouble(id);
            if (value<this.min) setSliderValue(min);
            else if (value>this.max) setSliderValue(max);
            else setSliderValue(value);
        }
        setColor();
    }
    
    @Override
    public void setContent(Parameter p) {
        if (p instanceof SliderDoubleParameter) {
            setSliderValue(((SliderDoubleParameter)p).getValue());
        }
        setColor();
    }
    
    protected void setSliderValue(double value) {
        slider.setValue((int) (value * Math.pow(10, decimals) + 0.5));
    }
    
    public void setValue(double value) {
        if (value>max) {
            setSliderValue(max);
            currentValue.setValue(max);
        } else if (value<min) {
            setSliderValue(min);
            currentValue.setValue(min);
        } else {
            setSliderValue(value);
            currentValue.setValue(value);
        }
    }

    @Override
    public Parameter duplicate(String newLabel, String newId) {
        return new SliderDoubleParameter(newLabel, newId, min, max, getValue(), decimals);
    }

    public double getValue() {
        return (slider.getValue() + 0.0) / Math.pow(10, decimals);
    }

    @Override
    public void stateChanged(ChangeEvent ce) {
        if (editing) return;
        if (ce.getSource().equals(slider)) {
            currentValue.setValue(getValue());
            if (template!=null) setColor();
        }
    }

    @Override
    public void addToGenericDialog(GenericDialog gd) {
        Number n = this.getValue();
        float def = n != null ? n.floatValue() : 0;
        gd.addNumericField(this.getLabel(), def, 3);
    }

    @Override
    public boolean sameContent(Parameter p) {
        return p instanceof SliderDoubleParameter && this.getValue()==((SliderDoubleParameter)p).getValue();
    }
 
}
