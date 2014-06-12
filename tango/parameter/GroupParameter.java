package tango.parameter;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import ij.gui.GenericDialog;
import java.awt.Color;
import java.awt.event.ActionListener;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JLabel;
import javax.swing.border.BevelBorder;
import tango.gui.util.CollapsiblePanel;
import tango.gui.util.Displayer;
import tango.gui.util.Refreshable;

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
public class GroupParameter extends Parameter implements NestedParameter{
    Parameter[] parameters;
    Box mainBox;
    
    public GroupParameter(String label, String id) {
        super(id);
        this.label=new JLabel(label);
        box= new CollapsiblePanel(this.label);
        
        
        mainBox = Box.createVerticalBox();
        mainBox.setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED));
        //Box subBox = Box.createHorizontalBox();
        //subBox.add(this.label);
        //mainBox.add(subBox);
        
        box.add(mainBox);
        
    }
    
    public void setParameters(Parameter[] parameters) {
        this.parameters=parameters;
        for (Parameter p : parameters) {
            p.setParent(this);
            p.addToContainer(mainBox);
        }
        setColor();
    }
    
    public GroupParameter(String label, String id, Parameter[] parameters) {
        super(id);
        this.label=new JLabel(label);
        box= new CollapsiblePanel(this.label);
        
        //this.parameters=parameters;
        mainBox = Box.createVerticalBox();
        mainBox.setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED));
        /*for (Parameter p : parameters) {
            p.setParent(this);
            p.addToContainer(mainBox);
        }*/
        box.add(mainBox);
        //setColor();
        setParameters(parameters);
    }
    
    @Override
    public void dbPut(DBObject dbo) {
        BasicDBObject subDBO = new BasicDBObject();
        for (Parameter p : parameters) p.dbPut(subDBO);
        dbo.put(id, subDBO);
    }
    
    @Override
    public void toggleVisibility(boolean visible) {
        ((CollapsiblePanel)box).toggleVisibility(visible);
    }

    @Override
    public void dbGet(BasicDBObject dbo) {
        BasicDBObject subDBO=(BasicDBObject)dbo.get(id);
        if (subDBO!=null) for (Parameter p : parameters) p.dbGet(subDBO);
        setColor();
    }

    @Override
    public Parameter duplicate(String newLabel, String newId) {
        return new GroupParameter(newLabel, newId, Parameter.duplicateArray(parameters));
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
        return (p instanceof GroupParameter) && Parameter.sameValue(((GroupParameter)p).getParameters(), getParameters());
    }
    
    @Override
    public void setContent(Parameter p) {
        if (p instanceof GroupParameter) {
            Parameter.setContent(getParameters(), ((GroupParameter)p).getParameters());
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
    
    @Override 
    public void setTemplate(Parameter template) {
        if (parameters!=null && template instanceof GroupParameter) {
            GroupParameter gt = (GroupParameter)template;
            if (gt.getParameters()!=null && gt.getParameters().length==parameters.length) {
                for (int i = 0; i<parameters.length; i++) {
                    parameters[i].setTemplate(gt.getParameters()[i]);
                }
            }
        }
        else if (parameters!=null && template==null) {
            for (int i = 0; i<parameters.length; i++) {
                parameters[i].setTemplate(null);
            }
        }
        this.template=template;
        setColor();
    }
    
}
