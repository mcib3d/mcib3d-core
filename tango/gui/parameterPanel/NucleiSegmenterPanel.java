package tango.gui.parameterPanel;

import com.mongodb.BasicDBObject;
import java.awt.Choice;
import java.awt.event.ItemEvent;
import java.util.Set;
import tango.plugin.segmenter.NucleusSegmenter;
import tango.plugin.PluginFactory;
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
public class NucleiSegmenterPanel extends ParameterPanelPlugin {
    
    public NucleiSegmenterPanel() {
        super();
    }

    @Override
    public Set<String> getMethods() {
        return PluginFactory.getNucleiSegmenterList();
    }

    @Override
    protected void getPlugin(String method) {
        plugin = PluginFactory.getNucleiSegmenter(method);
    }

    @Override
    public String getMPPLabel() {
        return "";
    }
    
    @Override
    public void itemStateChanged(ItemEvent ie) {
    }
    
    @Override
    protected void registerChannelParameters() {}
}
