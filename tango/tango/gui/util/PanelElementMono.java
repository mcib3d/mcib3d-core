package tango.gui.util;

import tango.gui.parameterPanel.MultiParameterPanel;
import tango.gui.parameterPanel.ParameterPanel;
import ij.IJ;
import java.awt.Choice;
import java.awt.Component;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;
import java.util.Set;
import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import tango.parameter.StructureParameter;
import tango.parameter.TextParameter;

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
public class PanelElementMono extends PanelElementAbstract implements ActionListener {
    
    public PanelElementMono(ParameterPanel parameterPanel, MultiParameterPanel mpp, boolean enableRemove, int idx) {
        super(mpp, parameterPanel);
        this.idx=idx;
        panel=Box.createHorizontalBox();
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
        this.label = new JLabel(parameterPanel.getMPPLabel());
        parameterPanel.setMPPLabel(label);
        panel.add(label);
        panel.add(Box.createHorizontalGlue());
        panel.setMinimumSize(panel.getPreferredSize());
        updateValidity();
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
        } 

    }

}
