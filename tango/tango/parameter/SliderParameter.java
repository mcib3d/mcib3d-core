package tango.parameter;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import ij.IJ;
import ij.gui.GenericDialog;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Hashtable;
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
public class SliderParameter extends Parameter implements ChangeListener {

    JSlider slider;
    JFormattedTextField currentValue; // was JLabel
    int min, max;
    boolean editing=false;
    public SliderParameter(String label, String id, int min_, int max_, int value) {
        super(label, id);
        this.min = min_;
        this.max = max_;
        slider = new JSlider(min, max, value);
        slider.addChangeListener(this);
        box.add(slider);
        
        currentValue = new JFormattedTextField(NumberFormat.getIntegerInstance());
        currentValue.setValue(max);
        currentValue.setPreferredSize(new Dimension((currentValue.getText().length()+1) * 10, 20));
        //currentValue.setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED));
        box.add(currentValue);
        currentValue.setValue(value );
        setColor();
        
        currentValue.addPropertyChangeListener("value",new PropertyChangeListener(){
            @Override
            public void propertyChange(PropertyChangeEvent e) {
                Number val = (Number)currentValue.getValue();
                if (val!=null){
                    editing=true;
                    int v = val.intValue();
                    if (v<min) {
                        slider.setValue(min);
                        java.awt.EventQueue.invokeLater(new Runnable() {
                            @Override
                            public void run() {
                                currentValue.setValue(min);
                            }
                        });
                    } else if (v>max) {
                        slider.setValue(max);
                        java.awt.EventQueue.invokeLater(new Runnable() {
                            @Override
                            public void run() {
                                currentValue.setValue(max);
                            }
                        });
                    } else slider.setValue(v);
                    if (template != null) setColor();
                    editing=false;
                }
            }
        });
    }

    @Override
    public void dbPut(DBObject DBO) {
        DBO.put(id, slider.getValue());
    }

    @Override
    public void dbGet(BasicDBObject DBO) {
        if (DBO.containsField(id) && DBO.get(id) != null) {
            int value = DBO.getInt(id);
            if (value < this.min) {
                slider.setValue(min);
            } else if (value > this.max) {
                slider.setValue(max);
            } else {
                slider.setValue(value);
            }
        }
        setColor();
    }

    @Override
    public void setContent(Parameter p) {
        if (p instanceof SliderParameter) {
            slider.setValue(((SliderParameter) p).getValue());
        }
        setColor();
    }

    @Override
    public Parameter duplicate(String newLabel, String newId) {
        return new SliderParameter(newLabel, newId, slider.getMinimum(), slider.getMaximum(), slider.getValue());
    }

    public int getValue() {
        return slider.getValue();
    }

    public int getMaxValue() {
        return max;
    }

    public int getMinValue() {
        return min;
    }

    @Override
    public void stateChanged(ChangeEvent ce) {
        if (editing) return;
        if (ce.getSource().equals(slider)) {
             //IJ.log("Slider");
            currentValue.setValue(getValue());
            if (template != null) setColor();
            
        } 
        
    }

    @Override
    public void addToGenericDialog(GenericDialog gd) {
        gd.addSlider(this.getLabel(), this.getMinValue(), this.getMaxValue(), this.getValue());
    }

    @Override
    public boolean sameContent(Parameter p) {
        return p instanceof SliderParameter && this.getValue() == ((SliderParameter) p).getValue();
    }
}
