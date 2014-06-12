package tango.gui.parameterPanel;

import ij.IJ;
import java.awt.Choice;
import java.awt.Color;
import java.awt.Component;
import java.awt.GridBagLayout;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.Set;
import javax.swing.*;
import tango.gui.util.PanelElementAbstract;
import tango.helper.Helper;
import tango.parameter.StructureParameter;
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

public class PanelElementPlugin extends PanelElementAbstract implements ActionListener {
    protected JComboBox method;
    protected PanelElementPlugin template;
    protected boolean templateSet;
    protected JButton test;
    
    public PanelElementPlugin(ParameterPanelPlugin parameterPanel, MultiParameterPanel mpp, boolean enableRemove, boolean enableTest) {
        super(mpp, parameterPanel);
        panel = Box.createHorizontalBox();
        panel.add(Box.createHorizontalStrut(2));
        method=new JComboBox();
        method.addItem("");
        for (String s : parameterPanel.getMethods()) method.addItem(s);
        if (parameterPanel.getMethod()!=null) {
            method.setSelectedItem(parameterPanel.getMethod());
        }
        method.addActionListener(this);
        method.setMinimumSize(method.getPreferredSize());
        method.setMaximumSize(method.getPreferredSize());
        panel.add(method);
        panel.add(Box.createHorizontalStrut(2));
        edit=new JToggleButton("Edit");
        edit.addActionListener(this);
        panel.add(edit);
        panel.add(Box.createHorizontalStrut(2));
        remove= new JButton("Remove");
        panel.add(remove);
        panel.add(Box.createHorizontalStrut(2));
        if (enableRemove) remove.addActionListener(this);
        else remove.setEnabled(false);
        test= new JButton("Test");
        if (enableTest) {
            panel.add(Box.createHorizontalStrut(2));
            panel.add(test);
            panel.add(Box.createHorizontalStrut(2));
            if (enableTest) test.addActionListener(this);
        }
        this.label = new JLabel(parameterPanel.getMPPLabel());
        parameterPanel.setMPPLabel(label);
        panel.add(label);
        panel.add(Box.createHorizontalGlue());
        panel.setMinimumSize(panel.getPreferredSize());
        updateValidity();
    }
    
    public void setTemplate(PanelElementPlugin template) {
        this.template=template;
        this.templateSet=true;
        updateValidity();
    }
    
    public void removeTemplate() {
        this.template=null;
        this.templateSet=false;
        updateValidity();
    }
    
    @Override
    public void updateValidity() {
        Color col = Color.black;
        boolean b = false;
        if (template!=null) b = ((ParameterPanelPlugin)parameterPanel).setTemplate((ParameterPanelPlugin)template.parameterPanel);
        if (!parameterPanel.checkValidity()) col=Color.red;
        else if (templateSet && !b) col=Color.blue;
        edit.setForeground(col);
    }
    
    
    public PanelElementPlugin(ParameterPanelPlugin parameterPanel, MultiParameterPanel mpp) {
        super(mpp, parameterPanel);
        updateValidity();
    }
    
    @Override
    public ParameterPanelPlugin getParameterPanel() {
        return (ParameterPanelPlugin)parameterPanel;
    }
    
    public void setCurrentMethod() {
        ((ParameterPanelPlugin)parameterPanel).setMethod((String)method.getSelectedItem());
        if (ml!=null) parameterPanel.register(ml);
        if (templateSet) setTemplate(template);
    }
    
    
    @Override
    public void actionPerformed(ActionEvent ae) {
        String command = ae.getActionCommand();
        if (command == null) {
            return;
        } else if (command.equals("Remove")) {
            mpp.removeElement(this);
        } else if (command.equals("Edit")) {
            if (edit.isSelected()) mpp.showPanel(this);
            else {
                updateValidity();
                mpp.hidePanel();
            }
        } else if (command.equals("Test")) {
            mpp.test(this);
        } else if (ae.getSource()==method) {
            setCurrentMethod();
            mpp.showPanel(this);
        }

    }
    
    

}
