package tango.parameter;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import java.awt.Choice;
import java.awt.Color;
import java.awt.Container;
import java.awt.GridLayout;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import javax.swing.*;
import javax.swing.border.BevelBorder;
import tango.gui.parameterPanel.ParameterPanelPlugin;
import tango.gui.util.CollapsiblePanel;
import tango.gui.util.Refreshable;
import tango.plugin.TangoPlugin;
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
public abstract class PluginParameter extends Parameter  implements ItemListener, NestedParameter, ActionnableParameter {
    protected JComboBox choice;
    protected String defMethod;
    protected String curMethod;
    protected Parameter[] currentParameters;
    boolean selecting;
    Refreshable r;
    TangoPlugin plugin;
    Box mainBox;
    
    protected BasicDBObject lastConfig;
    
    public PluginParameter(String label, String id, String defMethod){
        super(id);
        init(label, defMethod);
    }
    
    public PluginParameter(String label, String id, String defMethod, Parameter[] defParameters){
        super(id);
        init(label, defMethod);
        setContent(currentParameters, defParameters);
    }
    
    @Override
    public void setParameterPanel(ParameterPanelPlugin panel) {
        this.panel = panel;
    }
    
    protected void init(String label, String defMethod) {
        this.label = new JLabel(label);
        box = new CollapsiblePanel(this.label);
        mainBox=Box.createVerticalBox();
        mainBox.setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED));
        Box subBox = Box.createHorizontalBox();
        subBox.add(this.label);
        subBox.add(Box.createHorizontalStrut(20));
        subBox.add(Box.createHorizontalGlue());
        this.defMethod=defMethod;
        choice=new JComboBox();
        choice.addItemListener(this);
        subBox.add(choice);
        //panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        mainBox.add(subBox);
        box.add(mainBox);
        setColor();
    }
    
    @Override
    public void toggleVisibility(boolean visible) {
        ((CollapsiblePanel)box).toggleVisibility(visible);
    }
    
    
    @Override
    public void dbPut(DBObject DBO) {
        if (plugin==null) {
            return;
        }
        BasicDBObject subDBO= new BasicDBObject("method", choice.getSelectedItem());
        for (Parameter p : getParameters()) p.dbPut(subDBO);
        DBO.put(id, subDBO);
        lastConfig=subDBO;
    }
    
    @Override
    public void dbGet(BasicDBObject DBO) {
        if (DBO.containsField(id)) {
            Object o = DBO.get(id);
            if (!(o instanceof BasicDBObject)) return;
            BasicDBObject subDBO=(BasicDBObject)o;
            lastConfig = subDBO;
            String m = subDBO.getString("method");
            if (m!=null && m.length()>0) {
                if (plugin!=null) for (Parameter p : getParameters()) p.removeFromContainer(mainBox);
                getPlug(m);
                if (plugin!=null) {
                    for (Parameter p : getParameters()) p.dbGet(subDBO);
                    displayParameters();
                }
                selecting=true;
                choice.setSelectedItem(m);
                selecting=false;
            }
        }
        setColor();
    }
    
    protected void majPanel() {
        if (currentParameters!=null) {
            for (Parameter p : currentParameters) p.removeFromContainer(mainBox);
            if (ml!=null) unRegister();
        }
        getPlugin(getMethod());
        displayParameters();
        if (ml!=null) register();
        mainBox.revalidate();
        mainBox.repaint();
        box.repaint();
        setColor();
    }
    
    public String getMethod() {
        return utils.getSelectedString(choice);
    }
    
    @Override
    public void setFireChangeOnAction() {
        choice.addItemListener(
            new ItemListener() {
                @Override
                public void itemStateChanged(ItemEvent itemEvent) {
                    if (itemEvent.getStateChange()==ItemEvent.SELECTED) fireChange();
                }
            }
        );
        Parameter[] ps = getParameters();
        if (ps!=null) for (Parameter p : ps) if (p instanceof ActionnableParameter) ((ActionnableParameter)p).setFireChangeOnAction();
    }
    
    @Override
    public void setRefreshOnAction(Refreshable r_) {
        this.r=r_;
        choice.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent ie) {
                if (ie.getStateChange()==ItemEvent.DESELECTED) return;
                if (r!=null) r.refresh();
            }
        });
    }

    @Override
    public PluginParameter getParameter() {
        return this;
    }
    
    @Override
    public Object getValue() {
        return choice.getSelectedItem();
    }
    
    @Override
    public Parameter[] getParameters() {
        if (plugin!=null) {
            return plugin.getParameters();
        }
        else return null;
    }
    
    protected abstract void initChoice();
    protected abstract void getPlugin(String method);
    protected void displayParameters() {
        if (plugin!=null) {
            currentParameters = getParameters();
            this.setParametersProps();
            for (Parameter p : currentParameters) {
                p.setParent(this);
                p.addToContainer(mainBox);
            }
        }
    }
    protected void getPlug(String method) {
        if (curMethod==null || !curMethod.equals(method)) getPlugin(method);
        if (plugin!=null) {
            curMethod=method;
            this.setHelp(plugin.getHelp(), false);
        } else if (method!=null && method.length()>0 && !method.equals(" ")) {
            ij.IJ.log("Error: Plugin not found:"+method+ " for parameter:"+this.label.getText());
        }
    }
    @Override
    public void itemStateChanged(ItemEvent e) {
        if (e.getStateChange()==ItemEvent.DESELECTED) return;
        if (!selecting && e.getSource() == choice) {
            majPanel();
            //if (displayer!=null) displayer.refreshDisplay();
         }
    }
    
    public TangoPlugin getPlugin(int nCPUs, boolean verbose) {
        if (plugin!=null) {
            plugin.setMultithread(nCPUs);
            plugin.setVerbose(verbose);
        }
        return plugin;
    }
    
    @Override 
    protected void register() {
        label.addMouseListener(this);
        box.addMouseListener(this);
        if (currentParameters!=null) for (Parameter p : this.currentParameters) p.register(ml);
    }
    
    @Override 
    protected void unRegister() {
        label.removeMouseListener(this);
        box.removeMouseListener(this);
        if (currentParameters!=null) for (Parameter p : this.currentParameters) p.unRegister(ml);
    }
    
    @Override
    public void refresh() {
        if (plugin==null || plugin.getParameters()==null) return;
        for (Parameter p : plugin.getParameters()) if (p instanceof Refreshable) ((Refreshable)p).refresh();
        setColor();
    }
    
    @Override
    public boolean sameContent(Parameter p) {
        if (p instanceof PluginParameter) {
            PluginParameter other = (PluginParameter) p;
            return other.getMethod().equals(getMethod()) && Parameter.sameValue(other.getParameters(), getParameters());
        } else return false;
    }
    
    @Override 
    public void setContent(Parameter p) {
        if (p instanceof PluginParameter) {
            PluginParameter other = (PluginParameter) p;
            if (other.getMethod().equals(getMethod())) Parameter.setContent(currentParameters, other.currentParameters);
        }
    }
    
    
    @Override
    public void setTemplate(Parameter template) {
        this.template=template;
        setTemplates(template);
        setColor();
    }
    
    protected void setTemplates(Parameter template) {
        if (this.template instanceof PluginParameter && template.getClass()==getClass()) {
            PluginParameter t = (PluginParameter)template;
            if (t.getMethod().equals(getMethod())) {
                if (t.currentParameters!=null && currentParameters!=null && t.currentParameters.length==currentParameters.length) {
                    for (int i = 0; i<currentParameters.length; i++) {
                        currentParameters[i].setTemplate(t.currentParameters[i]);
                    }
                }
            }
        } else if (template==null && currentParameters!=null) {
            for (int i = 0; i<currentParameters.length; i++) {
                currentParameters[i].setTemplate(null);
            }
        }
    }
    
    protected void setParametersProps() {
        if (this.currentParameters!=null) {
            setTemplates(template); 
            for (Parameter p : currentParameters) p.setParent(this);
        }
    }
    
    @Override
    public boolean isValid() {
        if (!this.compulsary) return true;
        if (this.choice.getSelectedIndex()>0) {
            if (currentParameters!=null) {
                for (Parameter p : currentParameters) if (!p.isValidOrNotCompulsary()) return false;
            }
            return true;
        } else return false;
    }

}
