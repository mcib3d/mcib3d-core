package tango.parameter;

import com.mongodb.DBObject;
import com.mongodb.BasicDBObject;
import ij.WindowManager;
import ij.gui.GenericDialog;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Font;
import tango.gui.PanelDisplayer;
import java.awt.GridLayout;
import java.awt.event.*;
import java.text.*;
import java.util.*;
import javax.swing.*;
import tango.gui.parameterPanel.ParameterPanel;
import tango.gui.parameterPanel.ParameterPanelAbstract;
import tango.gui.parameterPanel.ParameterPanelPlugin;
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
public abstract class Parameter implements MouseListener {
    
    JLabel label;
    String id;
    Container box;
    protected String helpBasic, helpAdvanced;
    protected MouseListener ml;
    protected boolean compulsary=true;
    protected Parameter template=null;
    protected Parameter parent=null;
    public final static NumberFormat nfINIT = NumberFormat.getIntegerInstance();
    public final static NumberFormat nfDEC1 = DecimalFormat.getNumberInstance(Locale.ENGLISH);
    public final static NumberFormat nfDEC2 = DecimalFormat.getNumberInstance(Locale.ENGLISH);
    public final static NumberFormat nfDEC3 = DecimalFormat.getNumberInstance(Locale.ENGLISH);
    public final static NumberFormat nfDEC5 = DecimalFormat.getNumberInstance(Locale.ENGLISH);
    protected ParameterPanelPlugin panel;
    
    public Parameter(String label, String id) {
        initParameter(label, id, null);
    }
    
    public void setParameterPanel(ParameterPanelPlugin panel) {
        this.panel = panel;
    }
    
    protected void fireChange() {
        if (panel!=null) {
            panel.removeParameters();
            panel.displayParameters();
            panel.refresh();
        } else if (parent!=null) {
            parent.fireChange();
        }
    }
    
    public JLabel label() {
        return label;
    }
    
    public Parameter(String label, String id, Font font) {
        initParameter(label, id, font);
    }
    
    private void initParameter(String label, String id, Font font) {
        this.label = new JLabel(label);
        if (font != null) {
            this.label.setFont(font);
        }
        this.id = id;
        box = Box.createHorizontalBox();
        box.add(this.label);
        this.label.setAlignmentX(Component.LEFT_ALIGNMENT);
        box.add(Box.createHorizontalStrut(20));
        box.add(Box.createGlue());
    }
    
    public Parameter(String id) {
        this.id = id;
    }
    
    public static void initNF() {
        nfDEC1.setMinimumFractionDigits(1);
        nfDEC2.setMinimumFractionDigits(2);
        nfDEC3.setMinimumFractionDigits(3);
        nfDEC5.setMinimumFractionDigits(6);
        
        nfDEC5.setMaximumFractionDigits(6);
    }
    
    
    public void addToContainer(Container container) {
        /*
         * if (this instanceof Refreshable) { ((Refreshable) this).refresh(); }
         *
         */
        container.add(box);
    }
    
    public void removeFromContainer(Container panel) {
        panel.remove(box);
    }
    
    public String getId() {
        return id;
    }
    
    public String getLabel() {
        return label.getText();
    }
    
    public abstract void dbPut(DBObject dbo);
    
    public abstract void dbGet(BasicDBObject dbo);
    
    public abstract Parameter duplicate(String newLabel, String newId);
    
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o instanceof String) {
            String s = (String) o;
            return s.equals(this.id);
        } else if (o instanceof Parameter) {
            return ((Parameter) o).id.equals(this.id);
        }
        return false;
    }
    
    @Override
    public int hashCode() {
        return id.hashCode();
    }

    public static GenericDialog buildDialog(String name, Parameter[] pars) {
        GenericDialog gd = new GenericDialog(name);
        for (Parameter p : pars) {
            p.addToGenericDialog(gd);
        }
        
        return gd;
    }
    
    public abstract void addToGenericDialog(GenericDialog gd);
    
    public void setTemplate(Parameter template) {
        this.template=template;
        setColor();
    }
    
    public void setCompulsary(boolean compulsary) {
        this.compulsary=compulsary;
    }
    
    public boolean isValid() {
        return true; //default
    }
    
    public boolean isValidOrNotCompulsary() {
        return !compulsary || isValid();
    }
    
    protected Color setColor() {
        Color col = Color.BLACK;
        if (!isValidOrNotCompulsary()) col=Color.red;
        else if (template!=null && !this.sameContent(template)) col=Color.blue;
        this.label.setForeground(col);
        if (parent!=null) parent.setColor();
        return col;
    }
    
    public abstract boolean sameContent(Parameter p);
    
    public abstract void setContent(Parameter p);
    
    protected void setParent(Parameter parent) {
        this.parent = parent;
    }
    
    public void register(MouseListener ml) {
        if (this.ml != null && this.ml == ml) {
            return;
        }
        this.ml = ml;
        register();
    }
    
    protected void register() {
        label.addMouseListener(this);
    }
    
    public void unRegister(MouseListener ml) {
        if (this.ml != ml) {
            return;
        }
        unRegister();
        this.ml = null;
    }
    
    protected void unRegister() {
        label.removeMouseListener(this);
    }
    
    public void setHelp(String help, boolean basic) {
        if (basic) {
            this.helpBasic = help;
        } else {
            this.helpAdvanced = help;
        }
    }
    
    public String getHelp(boolean basic) {
        String help = this.getLabel() + " \n";
        if (basic) {
            if (this.helpBasic != null) {
                //ij.IJ.log(help + helpBasic);
                return help + helpBasic;
            } else if (this.helpAdvanced != null) {
                return help + helpAdvanced;
            }
        } else {
            if (this.helpAdvanced != null) {
                return help + helpAdvanced;
            } else if (helpBasic != null) {
                return help + helpBasic;
            }
        }
        return help;
    }
    
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
        if (ml != null) {
            me.setSource(this);
            ml.mouseEntered(me);
        }
    }
    
    @Override
    public void mouseExited(MouseEvent me) {
    }
    
    public static Parameter[] duplicateArray(Parameter[] parameters) {
        Parameter[] res = new Parameter[parameters.length];
        for (int i = 0; i < res.length; i++) {
            res[i] = parameters[i].duplicate(parameters[i].getLabel(), parameters[i].id);
        }
        return res;
    }
    
    public static Parameter[] mergeArrays(Parameter[][] parameters) {
        int count = 0;
        for (Parameter[] p : parameters) if (p!=null) count+=p.length;
        Parameter[] res = new Parameter[count];
        int offset=0;
        for (Parameter[] p : parameters) {
            if (p==null) continue;
            System.arraycopy(p, 0, res, offset, p.length);
            offset+=p.length;
        }
        return res;
    }
    
    public static void addToList(Parameter[] parameters, ArrayList<Parameter> list) {
        for (Parameter p : parameters) {
            if (p instanceof NestedParameter) addToList(((NestedParameter)p).getParameters(), list);
            else list.add(p);
        }
    }
    
    protected static boolean sameValue(Parameter[] array1, Parameter[] array2) {
        if (array1==null && array2==null) return true;
        else if (array1!=null && array2!=null && array1.length==array2.length) {
            for (int i = 0; i<array1.length; i++) {
                if (!array1[i].sameContent(array2[i])) return false;
            }
            return true;
        } else return false;
    }
    
    protected static void setContent(Parameter[] recieve, Parameter[] give) {
        if (give==null || recieve==null) return;
        HashMap<String, Parameter> recieveMap = getHashMap(recieve);
        for (Parameter p : give) {
            Parameter p2 = recieveMap.get(p.getId());
            if (p2!=null) p2.setContent(p);
        }
        /*
        if (give!=null && recieve!=null && recieve.length==give.length) {
            for (int i = 0; i<recieve.length; i++ ) {
                recieve[i].setContent(give[i]);
            }
        }
        * 
        */
    }
    
    protected static HashMap<String, Parameter> getHashMap(Parameter[] parameters) {
        if (parameters==null) return new HashMap<String, Parameter>(0);
        HashMap<String, Parameter> ids = new HashMap<String, Parameter>(parameters.length);
        for (Parameter p : parameters) if (p!=null) ids.put(p.getId(), p);
        return ids;
    }
    
}
