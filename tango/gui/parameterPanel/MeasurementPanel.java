package tango.gui.parameterPanel;

import com.mongodb.BasicDBObject;
import java.awt.GridLayout;
import java.util.Set;
import javax.swing.JLabel;
import javax.swing.JPanel;
import tango.gui.PanelDisplayer;
import tango.helper.Helper;
import tango.parameter.KeyParameter;
import tango.parameter.Parameter;
import tango.parameter.TextParameter;
import tango.plugin.measurement.Measurement;
import tango.plugin.measurement.MeasurementStructure;
import tango.plugin.measurement.MeasurementObject;
import tango.plugin.PluginFactory;
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
public class MeasurementPanel extends ParameterPanelPlugin  {
    JLabel blank;
    JPanel keyPanel;
    Parameter[] currentKeys;
    public MeasurementPanel() {
        super();
        blank=new JLabel(" ");
        JLabel keyLabel=new JLabel("Output:");
        keyPanel = new JPanel(new GridLayout(1, 2));
        keyPanel.add(keyLabel);
    }

    @Override
    public Set<String> getMethods() {
        return PluginFactory.getMeasurementList();
    }
    
    @Override
    protected void dbGet() {
        for (Parameter p : getParameters()) p.dbGet(data);
        Object kData=data.get("keys");
        Parameter[] keys = ((Measurement)plugin).getKeys();
        if (keys!=null && kData!=null) {
            BasicDBObject keysData=(BasicDBObject)kData;
            for (Parameter p : keys) p.dbGet(keysData);
        }
    }
    
    @Override
    public void removeParameters() {
        if (plugin==null) return;
        for (Parameter p : currentParameters) p.removeFromContainer(subPanel);
        subPanel.remove(blank);
        subPanel.remove(keyPanel);
        for (Parameter p : currentKeys) p.removeFromContainer(subPanel);
    }
    @Override
    public void displayParameters() {
        if (plugin==null) return;
        currentParameters = getParameters();
        for (Parameter p : currentParameters) {
            p.setParameterPanel(this);
            p.addToContainer(subPanel);
        }      
        subPanel.add(blank);
        subPanel.add(keyPanel);
        currentKeys = getKeys();
        for (Parameter p : currentKeys) {
            p.setParameterPanel(this);
            p.addToContainer(subPanel);
        }
        if (mppLabel!=null) mppLabel.setText(getMPPLabel());
    }
    
    public Parameter[] getKeys() {
        if (plugin!=null) return ((Measurement)plugin).getKeys();
        else return null;
    }
    
    @Override
    public void register(Helper ml) {
        super.register(ml);
        if (plugin!=null) for (Parameter p : getKeys()) p.register(ml);
    }
    
    @Override
    public void unRegister(Helper ml) {
        if (currentMl!=null && currentMl==ml && plugin!=null) {
            for (Parameter p : getParameters()) p.unRegister(ml);
            for (Parameter p : getKeys()) p.unRegister(ml);
            currentMl=null;
        }
    }
    
    @Override
    public BasicDBObject save() {
        if (plugin==null) return null;
        this.data = new BasicDBObject("method", curMethod);
        for (Parameter p : getParameters()) p.dbPut(data);
        if (plugin instanceof MeasurementStructure) data.append("structures", ((MeasurementStructure)plugin).getStructures());
        else if (plugin instanceof MeasurementObject) data.append("structures", ((MeasurementObject)plugin).getStructure());
        Parameter[] keys = ((Measurement)plugin).getKeys();
        if (keys!=null) {
            BasicDBObject keysData = new BasicDBObject();
            data.append("keys", keysData);
            for (Parameter p : keys) p.dbPut(keysData);
        }
        return data;
    }

    @Override
    protected void getPlugin(String method) {
        plugin = PluginFactory.getMeasurement(method);
    }

    @Override
    public String getMPPLabel() {
        if (plugin instanceof MeasurementStructure) {
            int[] s = ((MeasurementStructure)plugin).getStructures();
            return "Structures: "+MeasurementKey.arrayToString(s);
        } else if (plugin instanceof MeasurementObject) {
            int s = ((MeasurementObject)plugin).getStructure();
            return "Structure: "+s;
        }
        return null;
    }
}
