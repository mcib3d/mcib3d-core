package tango.parameter;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import ij.gui.GenericDialog;
import java.awt.Dimension;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.border.BevelBorder;
import tango.gui.util.CollapsiblePanel;
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

// TODO desactiver les chexk box des sous keyparameters et les activer en meme temps que le checkbox principal
public class GroupKeyParameter extends Parameter implements NestedParameter {
    KeyParameter[] parameters;
    KeyParameter prefix;
    boolean lock=false;
    Box mainBox;
    public GroupKeyParameter(String label, String id, String prefix, boolean selected, KeyParameter[] parameters, boolean lock) {
        super(id);
        
        this.lock=lock;
        this.label=new JLabel(label+"prefix:");
        box=new CollapsiblePanel(this.label);
        this.parameters=parameters;
        if (parameters==null) this.parameters=new KeyParameter[0];
        mainBox = Box.createVerticalBox();
        mainBox.setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED));
        this.prefix=new KeyParameterObjectNumber(label+"prefix:", id, prefix, selected);
        this.prefix.addToContainer(mainBox);
        if (lock) {
            setSelected(selected);
            for (KeyParameter k : this.parameters) k.checkbox.setEnabled(false);
            addListener();
        } else this.prefix.checkbox.setEnabled(false);
        for (Parameter p : this.parameters) {
            p.setParent(this);
            p.addToContainer(mainBox);
        }
        box.add(mainBox);
    }
    
    @Override
    public void toggleVisibility(boolean visible) {
        ((CollapsiblePanel)box).toggleVisibility(visible);
    }
    
    public void setKeys(KeyParameter[] parameters) {
        for (Parameter p : this.parameters) p.removeFromContainer(mainBox);
        this.parameters=parameters;
        if (lock) {
            setSelected(prefix.isSelected());
            for (KeyParameter k : parameters) k.checkbox.setEnabled(false);
        }
        for (Parameter p : parameters) p.addToContainer(mainBox);
        mainBox.revalidate();
    }
    
    public boolean isSelected() {
        return prefix.isSelected();
    }
    
    public boolean isLocked() {
        return lock;
    }
    
    private void addListener() {
        this.prefix.checkbox.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent ie) {
                for (KeyParameter p : parameters) p.setSelected(prefix.isSelected());
            }
        });
    }
    
    public void setSelected(boolean selected) {
        prefix.setSelected(selected);
        if (lock) for (KeyParameter p : parameters) p.setSelected(selected);
    }
    
    public KeyParameter[] getKeys() {
        return this.parameters;
    }
    
    private void checkDuplicateIds() {
        ArrayList<String> ids = new ArrayList<String>(this.parameters.length+1);
        ids.add(prefix.id);
        for (int i =0;i<parameters.length;i++) {
            if (ids.contains(parameters[i].id)) {
                parameters[i].id+="_"+i;
            }
            ids.add(parameters[i].id);
        }
    }
    
    @Override
    public void dbPut(DBObject dbo) {
        BasicDBObject subDBO = new BasicDBObject();
        prefix.dbPut(subDBO);
        checkDuplicateIds();
        for (Parameter p : parameters) p.dbPut(subDBO);
        dbo.put(id, subDBO);
    }

    @Override
    public void dbGet(BasicDBObject dbo) {
        BasicDBObject subDBO=(BasicDBObject)dbo.get(id);
        if (subDBO==null) return;
        prefix.dbGet(subDBO);
        checkDuplicateIds();
        String pr = prefix.getKey();
        if (subDBO!=null) {
            for (KeyParameter p : parameters) {
                p.dbGet(subDBO);
                ((KeyParameter)p).addPrefix(pr);
            }
        }
    }

    @Override
    public Parameter duplicate(String newLabel, String newId) {
        return new GroupKeyParameter(newLabel, newId, prefix.getKey(), prefix.isSelected(), KeyParameter.duplicateKeyArray(parameters), lock);
    }
    
    @Override 
    protected void register() {
        box.addMouseListener(this);
        if (parameters!=null) for (Parameter p : this.parameters) p.register(ml);
    }
    
    @Override 
    protected void unRegister() {
        box.removeMouseListener(this);
        if (parameters!=null) for (Parameter p : this.parameters) p.unRegister(ml);
    }

    @Override
    public Parameter[] getParameters() {
        return parameters;
    }

    @Override
    public void addToGenericDialog(GenericDialog gd) {
        ;
    }
  
    @Override
    public void refresh() {
        if (parameters==null) return;
        for (Parameter p : parameters) if (p instanceof Refreshable) ((Refreshable)p).refresh();
        setColor();
    }

    @Override
    public boolean sameContent(Parameter p) {
        return true;
        //throw new UnsupportedOperationException("Not supported yet.");
    }
    
    @Override
    public void setContent(Parameter p) {
        if (p instanceof GroupKeyParameter) {
            Parameter.setContent(getParameters(), ((GroupKeyParameter)p).getParameters());
        }
    }

    
    @Override
    public boolean isValid() {
        if (!this.compulsary) return true;
        if (parameters!=null) {
            for (Parameter p : parameters) if (!p.isValidOrNotCompulsary()) return false;
            return true;
        } else return true;
    }
    
}
