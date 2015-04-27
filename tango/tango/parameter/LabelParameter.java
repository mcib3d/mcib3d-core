package tango.parameter;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import ij.gui.GenericDialog;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionListener;
import javax.swing.JLabel;
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
public class LabelParameter extends Parameter {

    public JLabel text;

    public LabelParameter(String label) {
        super(label, "labelParameter", new Font("Courier", Font.PLAIN+Font.ITALIC,  12));
        this.text=new JLabel("                  ");
        //text.setPreferredSize(new Dimension(100, 20));
        //text.setMinimumSize(new Dimension(80, 20));
        //text.setMaximumSize(new Dimension(160, 20));
        box.add(this.text);
    }

    @Override
    public Parameter duplicate(String newLabel, String newId) {
        return new LabelParameter(newLabel);
    }

    @Override
    public void dbPut(DBObject DBO) {
        
    }

    @Override
    public void dbGet(BasicDBObject DBO) {
        
    }

    public String getText() {
        return text.getText();
    }

    @Override
    public void addToGenericDialog(GenericDialog gd) {
        gd.addMessage(this.getLabel());
    }

    @Override
    public boolean sameContent(Parameter p) {
        return true;
        //throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void setContent(Parameter p) {
        if (p instanceof LabelParameter) {
            this.label.setText(p.getLabel());
        }
    }

}
