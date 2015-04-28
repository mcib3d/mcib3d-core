package tango.gui.parameterPanel;

import java.awt.event.ItemEvent;
import tango.plugin.sampler.Sampler;
import java.util.Set;
import javax.swing.JComboBox;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import tango.parameter.BooleanParameter;
import tango.parameter.Parameter;
import tango.parameter.TextParameter;
import tango.plugin.*;
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
public class SamplerPanel extends ParameterPanelPlugin implements DocumentListener {
    TextParameter name =new TextParameter("Sample Name: ", "name", "sample");
    public SamplerPanel() {
        super();
        name.text.getDocument().addDocumentListener(this);
    }
    
    public Sampler getSampler() {
        if (plugin!=null) return (Sampler)plugin;
        else return null;
    }

    @Override
    protected void getPlugin(String method) {
        plugin = PluginFactory.getSampler(method);
    }
    
    @Override
    protected Parameter[] getParameters() {
        Parameter[] params = plugin.getParameters();
        Parameter[] parameters=new Parameter[params.length+1];
        parameters[0]=name;
        System.arraycopy(params, 0, parameters, 1, params.length);
        return parameters;
    }

    @Override
    public Set<String> getMethods() {
        return PluginFactory.getSamplerList();
    }
    
    @Override
    public String getMPPLabel() {
        return name.getText();
    }
    
    public String getName() {
        return name.getText();
    }

    @Override
    public void insertUpdate(DocumentEvent de) {
        if (mppLabel!=null) this.mppLabel.setText(getMPPLabel());
    }

    @Override
    public void removeUpdate(DocumentEvent de) {
        if (mppLabel!=null) this.mppLabel.setText(getMPPLabel());
    }

    @Override
    public void changedUpdate(DocumentEvent de) {
        if (mppLabel!=null) this.mppLabel.setText(getMPPLabel());
    }
    
    @Override
    public void itemStateChanged(ItemEvent ie) {
    }
    
    @Override
    protected void registerChannelParameters() {}
}
