package tango.parameter;
import javax.swing.*;
import com.mongodb.DBObject;
import com.mongodb.BasicDBObject;
import ij.gui.GenericDialog;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
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
public class BooleanParameter extends Parameter implements ActionnableParameter {
    JCheckBox checkbox;
    Refreshable r;
    public BooleanParameter(String label, String id, boolean value) {
        super(label, id);
        this.checkbox=new JCheckBox("", value);
        checkbox.addItemListener(
            new ItemListener() {
                @Override
                public void itemStateChanged(ItemEvent itemEvent) {
                    setColor();
                }
            }
        );
        box.add(checkbox);
    }
    
    @Override
    public Parameter duplicate(String newLabel, String newId) {
        return new BooleanParameter(newLabel, newId, this.isSelected());
    }
    
    @Override
    public void dbPut(DBObject DBO) {
        DBO.put(id, checkbox.isSelected());
    }
    
    @Override
    public void dbGet(BasicDBObject DBO) {
        //System.out.println("Boolean:"+id);
        //System.out.println("DBO:"+DBO.get(id));
        if (DBO.containsField(id)) setSelected(DBO.getBoolean(id));
    }
    
    @Override
    public Boolean getValue() {
        return checkbox.isSelected();
    }
    
    
    
    public boolean isSelected() {
        return checkbox.isSelected();
    }
    
    @Override
    public BooleanParameter getParameter() {
        return this;
    }
    
    @Override
    public void setRefreshOnAction(Refreshable r_) {
        this.r=r_;
        checkbox.addItemListener(
            new ItemListener() {
                @Override
                public void itemStateChanged(ItemEvent itemEvent) {
                    if (r!=null) r.refresh();
                    //if (displayer!=null) displayer.refreshDisplay();
                }
            }
        );
    }

    @Override
    public void addToGenericDialog(GenericDialog gd) {
         gd.addCheckbox(this.getLabel(), this.isSelected());
    }
    
    public void setSelected(boolean selected) {
        this.checkbox.setSelected(selected);
        setColor();
    }

    @Override
    public boolean sameContent(Parameter p) {
        if (p instanceof BooleanParameter && ((BooleanParameter)p).getValue()==this.getValue()) return true;
        else return false;
    }
    
    @Override
    public void setContent(Parameter p) {
        if (p instanceof BooleanParameter) {
            Boolean b = ((BooleanParameter)p).getValue();
            if (b!=null) this.setSelected(b.booleanValue());
        }
    }

    @Override
    public Color setColor() {
        if (r!=null) return super.setColor();
        else return null;
    }

    @Override
    public void setFireChangeOnAction() {
        checkbox.addItemListener(
            new ItemListener() {
                @Override
                public void itemStateChanged(ItemEvent itemEvent) {
                    fireChange();
                }
            }
        );
    }
}
