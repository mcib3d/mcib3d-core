package tango.gui.parameterPanel;
import ij.*;
import java.util.*;
import javax.swing.*;
import java.awt.*;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.mongodb.WriteConcern;
import com.mongodb.DBCollection;
import com.mongodb.DB;
import com.mongodb.WriteResult;
import java.awt.event.*;
import tango.gui.Core;
import tango.gui.PanelDisplayer;
import tango.gui.util.Refreshable;
import tango.helper.Helper;
import tango.parameter.NestedParameter;
import tango.parameter.StructureParameter;
import tango.parameter.Parameter;
import tango.plugin.PluginFactory;
import tango.plugin.TangoPlugin;
import tango.plugin.measurement.Measurement;
import tango.plugin.measurement.MeasurementObject;
import tango.plugin.measurement.MeasurementStructure;
import tango.plugin.sampler.Sampler;
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
public abstract class ParameterPanelPlugin extends ParameterPanelAbstract implements ItemListener  {
    protected String curMethod;
    protected TangoPlugin plugin;
    protected String help;
    protected Parameter[] currentParameters;
    ArrayList<JComboBox> structureParameters;
    
    public ParameterPanelPlugin() {
       
    }
    
    public boolean setTemplate(ParameterPanelPlugin template) {
        if (template==null) { //remove template
            if (currentParameters!=null) {
                for (int i = 0; i<currentParameters.length; i++) {
                    currentParameters[i].setTemplate(null);
                }
            }
            return false;
        } else {
            if (template.curMethod==null && curMethod!=null) return false;
            else if (template.curMethod!=null && curMethod==null) return false;
            else if (template.curMethod==null && curMethod==null) return true;
            else {
                if (!template.curMethod.equals(curMethod)) return false;
                if (currentParameters==null && template.currentParameters!=null) return false;
                else if (currentParameters!=null && template.currentParameters==null) return false;
                if (currentParameters==null && template.currentParameters==null) return true;
                if (currentParameters.length!=template.currentParameters.length) return false;
                for (int i = 0; i<currentParameters.length; i++) {
                    currentParameters[i].setTemplate(template.currentParameters[i]);
                }
                return true;
            }
        }
    }
    
    
    @Override
    public String getHelp() {
        return help;
    }
    
    @Override
    public void refreshParameters() {
        if (plugin!=null) {
            for (Parameter p : plugin.getParameters()) {
                if (p instanceof Refreshable) {
                    ((Refreshable)p).refresh();
                }
            }
        }
    }
    
    @Override
    public void refreshDisplay() {
        majPanel(curMethod);
        displayer.refreshDisplay();

    }
    
    public void refresh() {
        subPanel.revalidate();
        subPanel.repaint();
    }

    
    public String getMethod() {
        return curMethod;
    }
    
    public abstract Set<String> getMethods();
    
    protected abstract void getPlugin(String method);
    private void getPlug(String method) {
        if (curMethod==null || !curMethod.equals(method)) {
            method=PluginFactory.getCorrespondance(method);
            getPlugin(method);
        }
        if (plugin!=null) {
            curMethod=method;
            help=plugin.getHelp();
            this.label.setText(method);
        }
    }
    
    @Override
    public void initPanel() {
        String method=null;
        if (data!=null) {
            if (data.containsField("method")) method = data.getString("method");
        }
        this.subPanel=new JPanel();
        subPanel.setLayout(new BoxLayout(subPanel, BoxLayout.Y_AXIS));
        this.label=new JLabel(" ");
        this.label.addMouseListener(this);
        JPanel subsubPanel = new JPanel();
        subsubPanel.add(label);
        subPanel.add(subsubPanel);
        subPanel.add(new JLabel(""));
        panel=new JPanel();
        panel.add(subPanel);
        majPanel(method);
    }
    
    public void setMethod(String method) {
        majPanel(method);
    }
    
    protected void majPanel(String method) {
        //System.out.println("maj panel old:"+curMethod+" new:"+method);
        if (plugin!=null) {
            removeParameters();
        }
        String meth = curMethod;
        getPlug(method);
        if (data!=null && curMethod!=null && !curMethod.equals(meth)) {
            String dataMethod = data.getString("method");
            if (dataMethod!=null && dataMethod.length()>0 && curMethod.equals(dataMethod)) {
                if (plugin!=null) {
                    dbGet();
                }
            }
        }
        displayParameters();
        subPanel.revalidate();
        subPanel.repaint();
        if (mppLabel!=null) {
            registerChannelParameters();
            //System.out.println("maj panel setText null:"+(getMPPLabel()));
            mppLabel.setText(getMPPLabel());
        }
        
    }
    
    protected void dbGet() {
        for (Parameter p : getParameters()) p.dbGet(data);
    }
    
    public void removeParameters() {
        if (plugin==null) return;
        if (currentParameters!=null) for (Parameter p : currentParameters) p.removeFromContainer(subPanel);
    }
    
    public void displayParameters() {
        if (plugin==null) return;
        currentParameters=getParameters();
        for (Parameter p : currentParameters) {
            p.setParameterPanel(this);
            p.addToContainer(subPanel);
        }
    }
    
    protected void registerChannelParameters() {
        if (structureParameters!=null) {
            for (JComboBox jc : structureParameters) {
                jc.removeItemListener(this);
            }
        }
        if (plugin==null) {
            structureParameters= null;
            return;
        }
        this.structureParameters=  new ArrayList<JComboBox>(3);
        for (Parameter p : getParameters()) {
            if (p instanceof StructureParameter) {
                structureParameters.add(((StructureParameter)p).getChoice());
            } else if (p instanceof NestedParameter) {
                if (((NestedParameter)p).getParameters()==null) continue;
                for (Parameter pp:((NestedParameter)p).getParameters()) {
                    if (pp instanceof StructureParameter) {
                        structureParameters.add(((StructureParameter)pp).getChoice());
                    }
                }
            }
        }
        for (JComboBox jc : structureParameters) {
            jc.addItemListener(this);
        }
    }
    

    
    
    @Override
    public BasicDBObject save() {
        if (plugin==null) {
            return data;
        }
        this.data = new BasicDBObject("method", curMethod);
        for (Parameter p : getParameters()) p.dbPut(data);
        return data;
    }
    
    @Override
    public void register(Helper ml) {
        if (currentMl!=null) unRegister(currentMl);
        if (plugin!=null) {
            for (Parameter p : getParameters()) p.register(ml);
        }
        currentMl=ml;
    }
    
    @Override
    public void unRegister(Helper ml) {
        if (currentMl!=null && currentMl==ml && plugin!=null) {
            for (Parameter p : getParameters()) p.unRegister(ml);
            currentMl=null;
        }
    }
    
    protected Parameter[] getParameters() {
        return plugin.getParameters();
    }
    
    @Override
    public void itemStateChanged(ItemEvent ie) {
        if ((mppLabel!=null && ie.getSource() instanceof JComboBox)) {
            this.mppLabel.setText(getMPPLabel());
        }
    }

    @Override
    public boolean checkValidity() {
        if (plugin==null) return false;
        else {
            if (currentParameters!=null) for (Parameter p : currentParameters) if (!p.isValidOrNotCompulsary()) return false;
            return true;
        }
    }
}
