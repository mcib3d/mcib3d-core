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
import tango.gui.util.Refreshable;
import tango.util.utils;
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
public class ChoiceParameter extends Parameter implements ActionnableParameter {

    JComboBox choice;
    Refreshable r;
    public ChoiceParameter(String label, String id, java.lang.Iterable<String> values, String defaultValue) {
        super(label, id);
        choice = new JComboBox();
        box.add(choice);
        ArrayList<String> al = new ArrayList();
        for (String item : values) {
            this.choice.addItem(item);
            al.add(item);
        }
        if (defaultValue != null) {
            choice.setSelectedItem(defaultValue);
        }
        choice.addItemListener(
            new ItemListener() {
                @Override
                public void itemStateChanged(ItemEvent itemEvent) {
                    if (itemEvent.getStateChange()==ItemEvent.DESELECTED) return;
                    setColor();
                }
            }
        );
        setColor();
    }

    public ChoiceParameter(String label, String id, String[] values, String defaultValue) {
        super(label, id);
        choice = new JComboBox(values);
        box.add(choice);
        if (defaultValue != null) {
            choice.setSelectedItem(defaultValue);
        }
        choice.addItemListener(
            new ItemListener() {
                @Override
                public void itemStateChanged(ItemEvent itemEvent) {
                    if (itemEvent.getStateChange()==ItemEvent.DESELECTED) return;
                    setColor();
                }
            }
        );
        setColor();
    }

    @Override
    public Parameter duplicate(String newLabel, String newId) {
        String[] values = new String[choice.getItemCount()];
        for (int i = 0; i < values.length; i++) {
            values[i] = (String) choice.getItemAt(i);
        }
        Object def = choice.getSelectedItem();
        return new ChoiceParameter(newLabel, newId, values, def != null ? (String) def : null);
    }

    @Override
    public void dbPut(DBObject DBO) {
        if (choice.getSelectedIndex() >= 0) {
            DBO.put(id, choice.getSelectedItem());
        }
    }

    @Override
    public void dbGet(BasicDBObject DBO) {
        if (DBO.containsField(id)) {
            choice.setSelectedItem(DBO.getString(id));
        }
        setColor();
    }

    public String getSelectedItem() {
        return (String) choice.getSelectedItem();
    }

    @Override
    public Object getValue() {
        return choice.getSelectedItem();
    }

    public int getSelectedIndex() {
        return choice.getSelectedIndex();
    }

    public String[] getItems() {
        String[] res = new String[choice.getItemCount()];
        for (int i = 0; i < res.length; i++) {
            res[i] = (String) choice.getItemAt(i);
        }
        return res;
    }

    public JComboBox getChoice() {
        return choice;
    }

    @Override
    public void setRefreshOnAction(Refreshable r_) {
        this.r=r_;
        choice.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent ie) {
                if (ie.getStateChange()==ItemEvent.DESELECTED) return;
                if (r!=null) r.refresh();
            }
        });
    }

    @Override
    public ChoiceParameter getParameter() {
        return this;
    }

    @Override
    public void addToGenericDialog(GenericDialog gd) {
        gd.addChoice(this.getLabel(), this.getItems(), this.getItems()[this.getSelectedIndex()]);
    }

    @Override
    public boolean sameContent(Parameter p) {
        if (p instanceof ChoiceParameter && ((ChoiceParameter)p).getSelectedItem().equals(this.getSelectedItem())) return true;
        else return false;
    }
    
    @Override
    public void setContent(Parameter p) {
        if (p instanceof ChoiceParameter) {
            String s = ((ChoiceParameter)p).getSelectedItem();
            if (utils.contains(choice, s, true)) this.choice.setSelectedItem(s);
        }
    }
    
    @Override
    public boolean isValid() {
        if (!this.compulsary) return true;
        if (this.getSelectedIndex()<0) return false;
        else if (this.getSelectedItem().length()==0) return false;
        else return true;
    }
    
    /*@Override
    public Color setColor() {
        if (r!=null) return super.setColor();
        else return null;
    }
    */

    @Override
    public void setFireChangeOnAction() {
        choice.addItemListener(
            new ItemListener() {
                @Override
                public void itemStateChanged(ItemEvent itemEvent) {
                    if (itemEvent.getStateChange()==ItemEvent.SELECTED) fireChange();
                }
            }
        );
    }

}
