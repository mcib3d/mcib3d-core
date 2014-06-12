package tango.parameter;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import ij.gui.GenericDialog;
import java.awt.Dimension;
import java.awt.event.ActionListener;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
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
public class TextParameter extends Parameter{
    public JTextField text;
    boolean allowSpecial=false;
    public TextParameter(String label, String id, String defaultValue) {
        super(label, id);
        this.text=new JTextField();
        text.setPreferredSize(new Dimension(100, 20));
        text.setMinimumSize(new Dimension(80, 20));
        text.setMaximumSize(new Dimension(160, 20));
        box.add(this.text);
        //addToPanel(text);
        if (defaultValue!=null && defaultValue.length()>0 && utils.isValid(defaultValue, true)) text.setText(defaultValue);
        addColorListener();
    }
    
    public void allowSpecialCharacter(boolean allow) {
        this.allowSpecial=allow;
    }
    
    protected void addColorListener() {
        text.getDocument().addDocumentListener(
            new DocumentListener() {
                @Override
                public void changedUpdate(DocumentEvent documentEvent) {
                    setColor();
                }

                @Override
                public void insertUpdate(DocumentEvent e) {
                    setColor();
                }

                @Override
                public void removeUpdate(DocumentEvent e) {
                    setColor();
                }
            }
        );
    }
    
    @Override
    public Parameter duplicate(String newLabel, String newId) {
        return new TextParameter(newLabel, newId, text.getText());
    }
    
    @Override
    public void dbPut(DBObject DBO) {
        if (getText()!=null) DBO.put(id, text.getText());
    }
    
    @Override
    public void dbGet(BasicDBObject DBO) {
        if (DBO.containsField(id)) {
            text.setText(DBO.getString(id));
        }
        setColor();
    }
    
    public String getText() {
        return text.getText();
    }

    @Override
    public void addToGenericDialog(GenericDialog gd) {
        gd.addStringField(this.getLabel(), this.getText());
    }
    
    @Override
    public boolean isValid() {
        if (!this.compulsary) return true;
        return utils.isValid(getText(), allowSpecial);
    }

    @Override
    public boolean sameContent(Parameter p) {
        return (p instanceof TextParameter) && ((TextParameter)p).getText().equals(getText());
    }
    
    @Override
    public void setContent(Parameter p) {
        if (p instanceof TextParameter) {
            text.setText(((TextParameter)p).getText());
        }
        setColor();
    }

}
