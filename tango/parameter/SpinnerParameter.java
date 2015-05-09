package tango.parameter;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import ij.gui.GenericDialog;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;
import javax.swing.JComboBox;
import javax.swing.JSpinner;
import javax.swing.SpinnerModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import tango.gui.util.Refreshable;
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
public class SpinnerParameter extends Parameter implements ActionnableParameter {

    JSpinner spinner;
    int minValue, maxValue;
    SpinnerNumberModel model;
    Refreshable r;
    public SpinnerParameter(String label, String id, int min, int max, int defaultValue) {
        super(label, id);
        model = new SpinnerNumberModel(defaultValue, min, max, 1);
        this.spinner = new JSpinner(model);
        box.add(spinner);
        minValue = min;
        maxValue = max;
        spinner.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent ce) {
                setColor();
            }
        });
    }

    @Override
    public Parameter duplicate(String newLabel, String newId) {
        return new SpinnerParameter(newLabel, newId, minValue, maxValue, this.getValue());
    }

    @Override
    public void dbPut(DBObject DBO) {
        DBO.put(id, getValue());
    }

    @Override
    public void dbGet(BasicDBObject DBO) {
        if (DBO.containsField(id)) {
            model.setValue(new Integer(DBO.getInt(id)));
        }
        setColor();
    }
    
    @Override
    public void setContent(Parameter p) {
        if (p instanceof SpinnerParameter) {
            model.setValue(((SpinnerParameter)p).getValue());
        }
        setColor();
    }

    @Override
    public Integer getValue() {
        return model.getNumber().intValue();
    }

    public int getMaxValue() {
        return maxValue;
    }

    public int getMinValue() {
        return minValue;
    }

    public void setValues(int min, int max) {
        model.setMinimum(min);
        model.setMaximum(max);
    }

    public JSpinner getSpinner() {
        return this.spinner;
    }
    
    @Override
    public void setRefreshOnAction(Refreshable r_) {
        this.r=r_;
        spinner.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent ce) {
                if (r!=null) r.refresh();
                //if (displayer!=null) displayer.refreshDisplay();
            }
        });
    }

    @Override
    public void addToGenericDialog(GenericDialog gd) {
        gd.addSlider(this.getLabel(), this.getMinValue(), this.getMaxValue(), this.getValue());
    }

    @Override
    public Parameter getParameter() {
        return this;
    }
    
    @Override
    public boolean sameContent(Parameter p) {
        return p instanceof SpinnerParameter && ((SpinnerParameter)p).getValue()==getValue();
    }
    
    @Override
    public Color setColor() {
        if (r!=null) return super.setColor();
        else return null;
    }

    @Override
    public void setFireChangeOnAction() {
        spinner.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent ce) {
                fireChange();
            }
        });
    }

}
