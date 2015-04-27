package tango.gui.parameterPanel;
import ij.*;
import java.util.*;
import javax.swing.*;
import java.awt.*;
import com.mongodb.BasicDBObject;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import tango.gui.util.Refreshable;
import tango.helper.Helper;
import tango.parameter.StructureParameter;
import tango.parameter.Parameter;
import tango.parameter.TextParameter;
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
public abstract class ParameterPanel extends ParameterPanelAbstract implements DocumentListener {
    Parameter[] parameters;
    public ParameterPanel() {
       
    }
    public abstract void getParameters();
    
    public void dbGet() {
        if (parameters!=null && data!=null) for (Parameter p : parameters) p.dbGet(data);
    }
    
    @Override
    public void initPanel() {
        this.subPanel=new JPanel();
        subPanel.setLayout(new BoxLayout(subPanel, BoxLayout.Y_AXIS));
        subPanel.setAlignmentX(JPanel.LEFT_ALIGNMENT);
        getParameters();
        if (parameters!=null) {
            dbGet();
            for (Parameter p : parameters) {
                
                p.addToContainer(subPanel);
            }
        }
        this.label=new JLabel("");
        this.label.addMouseListener(this);
        JPanel subsubPanel = new JPanel();
        subsubPanel.add(label);
        subPanel.add(subsubPanel);
        subPanel.add(new JLabel(""));
        panel=new JPanel();
        panel.add(subPanel);
    }
    
    @Override
    public void refreshParameters() {
        if (parameters!=null) for (Parameter p : parameters) if (p instanceof Refreshable) ((Refreshable)p).refresh();
    }
    
    @Override
    public BasicDBObject save() {
        if (parameters==null) return null;
        this.data = new BasicDBObject();
        for (Parameter p : parameters) p.dbPut(data);
        return data;
    }

    @Override
    public void register(Helper ml) {
        if (currentMl!=null) unRegister(currentMl);
        if (parameters!=null) {
            for (Parameter p : parameters) p.register(ml);
        }
        currentMl=ml;
    }
    
    @Override
    public void unRegister(Helper ml) {
        if (currentMl!=null && currentMl==ml && parameters!=null) {
            for (Parameter p : parameters) p.unRegister(ml);
            currentMl=null;
        }
    }
    
    @Override
    public void refreshDisplay() {
        displayer.refreshDisplay();
    }
    
    protected void fireTextChange() {
        if (mppLabel!=null) {
            String s = getMPPLabel();
            this.mppLabel.setText(s);
        }
    }
    
    
    @Override
    public void insertUpdate(DocumentEvent de) {
        fireTextChange();
    }

    @Override
    public void removeUpdate(DocumentEvent de) {
        fireTextChange();
    }

    @Override
    public void changedUpdate(DocumentEvent de) {
        fireTextChange();
    }
    
    
}
