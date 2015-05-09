package tango.spatialStatistics.SDIEvaluator;

import ij.gui.Plot;
import java.awt.Color;
import java.util.HashMap;
import mcib3d.utils.ArrayUtil;
import mcib3d.utils.CDFTools;
import tango.dataStructure.StructureQuantifications;
import tango.parameter.*;
import tango.plugin.measurement.Measurement;
import tango.plugin.measurement.MeasurementKey;
import tango.plugin.measurement.MeasurementStructure;

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
public class KSEvaluator implements SDIEvaluator {
    KeyParameterStructureNumber sdi_P = new KeyParameterStructureNumber("SDI:", "sdi", "sdi", true);
    KeyParameter[] keys = new KeyParameter[] {sdi_P};
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
    @Override
    public Parameter[] getParameters() {
        return new Parameter[]{};
    }

    @Override
    public KeyParameter[] getKeyParameters() {
        return keys;
    }

    @Override
    public void eval(double[] observedDescriptor, double[][] sampledDescriptor, StructureQuantifications quantifs) {
        if (!sdi_P.isSelected() || observedDescriptor==null || sampledDescriptor==null) return;
        int numEvaluationPoints = observedDescriptor.length;
        ArrayUtil observedDistances = new ArrayUtil(observedDescriptor);
        observedDistances.sort();
        int lim = (sampledDescriptor.length)/2;
        ArrayUtil[] sampleDistancesAVG = new ArrayUtil[lim];
        ArrayUtil xEvals = new ArrayUtil(lim * numEvaluationPoints);
        for (int i = 0; i<lim; i++) {
            sampleDistancesAVG[i]=new ArrayUtil(sampledDescriptor[i]);
            sampleDistancesAVG[i].sort();
            xEvals.insertValues(i * numEvaluationPoints, sampleDistancesAVG[i]);
        }
        xEvals.sort();
        ArrayUtil averageCDF = CDFTools.cdfAverage(sampleDistancesAVG, xEvals);
        
        ArrayUtil[] sampleDistancesSDI = new ArrayUtil[sampledDescriptor.length-lim];
        for (int i = lim; i<sampledDescriptor.length; i++) {
            sampleDistancesSDI[i-lim]=new ArrayUtil(sampledDescriptor[i]);
            sampleDistancesSDI[i-lim].sort();
        }
        
        //sdi
        double sdi = CDFTools.SDI(observedDistances, sampleDistancesSDI, averageCDF, xEvals);
        quantifs.setQuantificationStructureNumber(sdi_P, sdi);
    }

    @Override
    public String getHelp() {
        return "";
    }
    
}
