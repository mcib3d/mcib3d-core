package tango.gui.util;

import java.awt.Color;
import tango.gui.parameterPanel.MultiParameterPanel;
import tango.gui.parameterPanel.ParameterPanelAbstract;
import java.awt.Container;
import javax.swing.*;
import tango.helper.Helper;

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
public abstract class PanelElementAbstract implements Displayer {
    protected MultiParameterPanel mpp;
    protected ParameterPanelAbstract parameterPanel;
    protected SpringLayout layout;
    protected Helper ml;
    protected int idx;
    protected Box panel;
    protected JButton remove;
    protected JLabel label;
    protected JToggleButton edit;
    
    public PanelElementAbstract(MultiParameterPanel mpp, ParameterPanelAbstract pp) {
        this.mpp=mpp;
        this.parameterPanel=pp;
        
    }
    @Override
    public void refreshDisplay() {
        parameterPanel.refreshDisplay();
    }
    
    public ParameterPanelAbstract getParameterPanel() {
        return parameterPanel;
    }
    public Container getPanel() {
        return panel;
    }
    public void on() {
        edit.setSelected(true);
    }
    
    public void off() {
        if (edit.isSelected()) updateValidity();
        edit.setSelected(false);
    }
    
    public void updateValidity() {
        if (parameterPanel.checkValidity()) edit.setForeground(Color.black);
        else edit.setForeground(Color.red);
    }
    
    public void setIdx(int idx) {
        this.idx=idx;
        this.parameterPanel.setIdx(idx);
    }
    public void register(Helper ml) {
        if (parameterPanel!=null && ml!=this.ml) parameterPanel.register(ml);
        this.ml=ml;
    }
    public void unRegister(Helper ml) {
        this.ml=null;
        if (parameterPanel!=null) parameterPanel.unRegister(ml);
    }
}
