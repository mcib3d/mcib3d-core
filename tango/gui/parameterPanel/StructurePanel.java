package tango.gui.parameterPanel;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JLabel;
import tango.gui.Core;
import tango.parameter.*;
import tango.util.IJ3dViewerParameters;
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
public class StructurePanel extends ParameterPanel {
    
    TextParameter name;
    ChannelFileParameter file;
    ChoiceParameter color;
    IJ3dViewerParameters ij3d;
    public StructurePanel() {
        super();
        
    }
    
    @Override
    public void getParameters() {
        name = new TextParameter((idx == 0) ? "Nucleus Name:" : "Structure Name:", "name", (idx == 0) ? "nucleus" : "");
        name.text.getDocument().addDocumentListener(this);
        name.setHelp("The name of your structure", true);
        file = new ChannelFileParameter("Channel File:", "file", idx);
        file.setHelp("The file associated to this structure", true);
        //settings = new SettingsParameter("Processing:", "settings", idx == 0);
        //settings.setHelp("The processing sequence to segment the structure", true);
        color = new ChoiceParameter("Color: ", "color", tango.gui.util.Colors.colorNames, tango.gui.util.Colors.colorNames[this.idx + 1]);
        color.setHelp("The color to display the structure in overlay views", true);
        color.getChoice().addActionListener(
            new ActionListener(){
                @Override
                public void actionPerformed(ActionEvent e) {
                    setColor();
                }
            }
        );
        ij3d = new IJ3dViewerParameters(idx==0);
        parameters = new Parameter[]{name, file, color, ij3d.getParameter()};
    }
    
    @Override 
    public void setMPPLabel(JLabel label){
        super.setMPPLabel(label);
        setColor();
    }
    
    protected void setColor() {
        if (color.getSelectedIndex()>0) {
            if (mppLabel!=null) mppLabel.setForeground(tango.gui.util.Colors.colors.get(color.getSelectedItem()));
        }
    }
    
    @Override
    public String getHelp() {
        return "Define the different structures of the experiment";
    }

    @Override
    public String getMPPLabel() {
        return " "+ idx+": "+name.getText();
    }
    
    
    public String getName() {
        return name.getText();
    }

    @Override
    public boolean checkValidity() {
        return name.isValidOrNotCompulsary()&&file.isValidOrNotCompulsary()&&color.isValidOrNotCompulsary();

    }
    
    
}
