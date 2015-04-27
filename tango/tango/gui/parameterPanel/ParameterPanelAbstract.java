package tango.gui.parameterPanel;

import com.mongodb.BasicDBObject;
import java.awt.Color;
import java.awt.Component;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import javax.swing.JLabel;
import javax.swing.JPanel;
import tango.gui.PanelDisplayer;
import tango.gui.util.Displayer;
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
public abstract class ParameterPanelAbstract implements MouseListener, Displayer {
    int idx;
    protected JLabel mppLabel;
    protected BasicDBObject data;
    protected PanelDisplayer displayer;
    protected JPanel subPanel, panel;
    protected JLabel label;
    protected Helper currentMl;
    public abstract void initPanel();
    public JPanel getPanel() {
        return panel;
    }
    public void setMPPLabel(JLabel name) {
        this.mppLabel=name;
    }
    public abstract String getMPPLabel();
    public abstract void refreshParameters();
    public abstract String getHelp();
    public void setIdx(int idx) {
        this.idx=idx;
    }
    public void setData(BasicDBObject data) {
         this.data = data;
    }
    public void setDisplayer(PanelDisplayer displayer) {
        this.displayer=displayer;
    }
    public abstract void register(Helper ml);
    public abstract void unRegister(Helper ml);
    public abstract BasicDBObject save();
    @Override
    public void mouseClicked(MouseEvent me) {
        
    }

    @Override
    public void mousePressed(MouseEvent me) {
        
    }

    @Override
    public void mouseReleased(MouseEvent me) {
        
    }

    @Override
    public void mouseEntered(MouseEvent me) {
        if (currentMl!=null) {
            me.setSource(this);
            currentMl.mouseEntered(me);
        }
    }

    @Override
    public void mouseExited(MouseEvent me) {
        
    }
    
    public abstract boolean checkValidity();
}
