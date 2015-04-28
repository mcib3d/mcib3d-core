package tango.parameter;

import ij.gui.GenericDialog;
import tango.dataStructure.InputCellImages;
import tango.dataStructure.SegmentedCellImages;
import tango.gui.Core;
import tango.plugin.PluginFactory;
import tango.plugin.measurement.distance.Distance;

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
public class DistanceParameter extends PluginParameter {
    
    public DistanceParameter(String label, String id, String defDistance) {
        super(label, id, defDistance);
        initChoice();
    }
    
    public DistanceParameter(String label, String id, String defDistance, Parameter[] defParameters) {
        super(label, id, defDistance);
        initChoice();
        setContent(currentParameters, defParameters);
    }
    
    @Override
    public Distance getPlugin(int nCPUs, boolean verbose) {
        if (plugin!=null) return (Distance)super.getPlugin(nCPUs, verbose);
        else return null;
    }
    
    public Distance getDistance(InputCellImages in, SegmentedCellImages out) {
        if (plugin!=null) {
            Distance d = (Distance) plugin;
            d.initialize(in, out);
            return d;
        } else return null;
    }
    
    @Override
    protected void initChoice() {
        selecting=true;
        choice.addItem("");
        if (Core.TESTING) for (String s : Distance.distancesTesting) choice.addItem(s);
        else for (String s : Distance.distances) choice.addItem(s);
        if (defMethod!=null && defMethod.length()>0) {
            choice.setSelectedItem(defMethod);
            majPanel();
        }
        selecting=false;
    }

    @Override
    protected void getPlugin(String method) {
        plugin = Distance.getDistance(method);
    }

    @Override
    public Parameter duplicate(String newLabel, String newId) {
        return new DistanceParameter(newLabel, newId, defMethod);
    }

    @Override
    public void addToGenericDialog(GenericDialog gd) {
        ;
    }
    
}
