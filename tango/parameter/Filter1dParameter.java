package tango.parameter;

import ij.gui.GenericDialog;
import tango.plugin.PluginFactory;
import tango.spatialStatistics.SDIEvaluator.filter1d.Filter1d;
import tango.spatialStatistics.StochasticProcess.RPGConstraint;
import tango.spatialStatistics.constraints.Constraint;
import tango.spatialStatistics.constraints.ConstraintFactory;

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
public class Filter1dParameter extends PluginParameter {
    
    public Filter1dParameter(String label, String id, String defConstraint) {
        super(label, id, defConstraint);
        initChoice();
    }
    
    @Override
    public Filter1d getPlugin(int nCPUs, boolean verbose) {
        if (plugin!=null) return (Filter1d)super.getPlugin(nCPUs, verbose);
        else return null;
    }
    
    @Override
    protected void initChoice() {
        selecting=true;
        choice.addItem("");
        for (String s : Filter1d.filters) choice.addItem(s);
        if (defMethod!=null && defMethod.length()>0) {
            choice.setSelectedItem(defMethod);
            majPanel();
        }
        selecting=false;
    }

    @Override
    protected void getPlugin(String method) {
        plugin = Filter1d.getFilter(method);
    }

    @Override
    public Parameter duplicate(String newLabel, String newId) {
        return new Filter1dParameter(newLabel, newId, defMethod);
    }

    @Override
    public void addToGenericDialog(GenericDialog gd) {
        ;
    }
    
}
