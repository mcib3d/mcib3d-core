package tango.parameter;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import ij.gui.GenericDialog;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import javax.swing.Box;
import javax.swing.JCheckBox;
import javax.swing.JTextField;
import javax.swing.text.AbstractDocument;
import tango.gui.util.DocumentFilterIllegalCharacters;
import tango.plugin.measurement.MeasurementKey;

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
public abstract class KeyParameter extends Parameter {
    public JTextField key;
    public JCheckBox checkbox;
    String prefix="";
    int type;
    // TODO donnot allow "." in keys...
    public KeyParameter(String label, String id, String defaultValue, boolean selected, int type) {
        super(id);
        box = Box.createHorizontalBox();
        this.key=new JTextField();
        ((AbstractDocument) key.getDocument()).setDocumentFilter(new DocumentFilterIllegalCharacters());
        this.checkbox=new JCheckBox(label, selected);
        box.add(checkbox);
        box.add(Box.createHorizontalStrut(20));
        box.add(Box.createHorizontalGlue());
        box.add(key);
        if (defaultValue!=null && defaultValue.length()>0) key.setText(defaultValue);
        this.type=type;
    }
    
    public KeyParameter(String label, String id, int type) {
        super(id);
        box = Box.createHorizontalBox();
        this.key=new JTextField();
        this.checkbox=new JCheckBox(label, true);
        box.add(checkbox);
        box.add(Box.createHorizontalStrut(20));
        box.add(Box.createHorizontalGlue());
        box.add(key);
        key.setText(id);
        this.type=type;
    }
    
    
    
    
    
    @Override
    public void dbPut(DBObject DBO) {
        BasicDBObject subDBO = new BasicDBObject("name", key.getText()).append("do", checkbox.isSelected()).append("type", type);
        DBO.put(id, subDBO);
    }
    
    @Override
    public void dbGet(BasicDBObject DBO) {
        Object sd = DBO.get(id);
        if (sd!=null) {
            BasicDBObject subDBO = (BasicDBObject)sd;
            key.setText(subDBO.getString("name"));
            checkbox.setSelected(subDBO.getBoolean("do"));
            type=subDBO.getInt("type", 0);
        }
    }
    
    @Override
    public String getLabel() {
        return checkbox.getText();
    }
    
    public String getKey() {
        return prefix+key.getText();
    }
    
    public boolean isSelected() {
        return checkbox.isSelected();
    }
    
    public int getType() {
        return type;
    }
    
    public void setSelected(boolean selected) {
        checkbox.setSelected(selected);
    }
    
    @Override
    public void register() {
        this.checkbox.addMouseListener(this);
    }
    
    @Override
    public void unRegister() {
        this.checkbox.removeMouseListener(this);
    }

    @Override
    public void addToGenericDialog(GenericDialog gd) {
    }

    
    protected static KeyParameter[] duplicateKeyArray(KeyParameter[] parameters) {
        KeyParameter[] res = new KeyParameter[parameters.length];
        for (int i = 0; i < res.length; i++) {
            res[i] = (KeyParameter)parameters[i].duplicate(parameters[i].getLabel(), parameters[i].id);
        }
        return res;
    }
    
    public void addPrefix(String prefix) {
        this.prefix=prefix;
    }
    
    public static boolean isOneKeySelected(Parameter[] keys) {
        for (Parameter p : keys) {
            if (p instanceof KeyParameter) {
                if (((KeyParameter)p).isSelected()) return true;
            } else if (p instanceof GroupKeyParameter) {
                if (((GroupKeyParameter)p).isLocked()) return  ((GroupKeyParameter)p).prefix.isSelected();
                else return isOneKeySelected(((GroupKeyParameter)p).getParameters());
            }
        }
        return false;
    }
    
    public static void addToKeyList(Parameter[] parameters, ArrayList<KeyParameter> list) {
        for (Parameter p : parameters) {
            if (p instanceof NestedParameter) addToKeyList(((NestedParameter)p).getParameters(), list);
            else if (p instanceof KeyParameter) list.add((KeyParameter)p);
        }
    }
    
    public static KeyParameter[] mergeKeyArrays(KeyParameter[][] parameters) {
        int count = 0;
        for (KeyParameter[] p : parameters) if (p!=null) count+=p.length;
        KeyParameter[] res = new KeyParameter[count];
        int offset=0;
        for (Parameter[] p : parameters) {
            if (p==null) continue;
            System.arraycopy(p, 0, res, offset, p.length);
            offset+=p.length;
        }
        return res;
    }

    @Override
    public boolean sameContent(Parameter p) {
        return true;
        //return p instanceof KeyParameter && ((KeyParameter)p).getKey().equals(getKey()) && ((KeyParameter)p).isSelected()==isSelected();
    }
    
    @Override
    public void setContent(Parameter p) {
        if (p instanceof KeyParameter) {
            KeyParameter k = (KeyParameter)p;
            this.setSelected(k.isSelected());
            this.key.setText(k.getKey());
            setColor();
        }
    }

    
    @Override
    public boolean isValid() {
        if (!this.compulsary) return true;
        return this.getKey()!=null && this.getKey().length()>0;
    }
    
    
}
