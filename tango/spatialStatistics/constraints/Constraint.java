package tango.spatialStatistics.constraints;

import java.util.ArrayList;
import tango.dataStructure.InputCellImages;
import tango.dataStructure.SegmentedCellImages;
import tango.plugin.TangoPlugin;
import tango.spatialStatistics.StochasticProcess.RandomPoint3DGenerator;
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
public abstract class Constraint implements TangoPlugin {
    RandomPoint3DGenerator rpg;
    boolean verbose;
    int nCPUs=1;
    public Constraint() {
    }
    @Override
    public void setVerbose(boolean verbose) {
        this.verbose=verbose;
    }
    @Override
    public void setMultithread(int nCPUs) {
        this.nCPUs=nCPUs;
    }
    public abstract void initialize(RandomPoint3DGenerator rpg, InputCellImages raw, SegmentedCellImages seg);
    public abstract ArrayList<Integer> getVoxelIndexes();
    public abstract boolean eval(double x, double y, double z, double res);
    public abstract void reset();
    public abstract boolean isValid();
}
