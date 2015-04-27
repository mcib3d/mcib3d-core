package tango.spatialStatistics.SDIEvaluator.filter1d;

import tango.plugin.TangoPlugin;
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
public abstract class Filter1d implements TangoPlugin {
    public static String[] filters = new String[] {"uniform", "low-pass", "second order low-pass"};
    int nCPUs=1;
    boolean verbose;
    @Override
    public void setMultithread(int nbCPUs) {
        this.nCPUs=nbCPUs;
    }
    
    @Override
    public void setVerbose(boolean verbose) {
        this.verbose=verbose;
    }
    public abstract double getValue(double x);
    
    public abstract void initFilter();
    
    public static Filter1d getFilter(String name) {
        if (name.equals(filters[0])) return new Uniform();
        else if (name.equals(filters[1])) return new LowPass();
        else if (name.equals(filters[2])) return new LowPass2();
        else return new Uniform();
    }
}
