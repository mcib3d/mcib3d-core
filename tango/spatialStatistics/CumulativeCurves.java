package tango.spatialStatistics;

import ij.gui.Plot;
import java.awt.Color;
import java.util.HashMap;
import mcib3d.utils.ArrayUtil;
import mcib3d.utils.CDFTools;
import tango.dataStructure.StructureQuantifications;
import tango.parameter.*;
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
public class CumulativeCurves {
    int nBins=1000;
    DoubleParameter enveloppe = new DoubleParameter("Envelope %", "envelppe", 0.05d, Parameter.nfDEC5);
    IntParameter nBins_P = new IntParameter("Number of Points for Plots", "nBins", nBins);
    GroupParameter parameters = new GroupParameter("Cumulative Curves:", "cumulativeCurveParams", new Parameter[]{enveloppe, nBins_P});
    
    KeyParameterStructureArray observed = new KeyParameterStructureArray("Observed CDF:", "observed", "observed", false);
    KeyParameterStructureArray acdf = new KeyParameterStructureArray("Average CDF:", "acdf", "acdf", false);
    KeyParameterStructureArray lower = new KeyParameterStructureArray("Lower CDF:", "lower", "lower", false);
    KeyParameterStructureArray upper = new KeyParameterStructureArray("Upper CDF:", "upper", "upper", false);
    KeyParameterStructureArray x_P = new KeyParameterStructureArray("X axis:", "x", "x", false);
    GroupKeyParameter keys = new GroupKeyParameter("Cumulative Curves:", "cumulativeCurveKeys", "", false, new KeyParameter[]{observed, acdf, lower, upper, x_P}, true);
    
    public Parameter getParameters() {
        return parameters;
    }
    
    public Parameter getKeys() {
        return keys;
    }
    
    public void getCurves(double[] observedDescriptor, double[][] sampledDescriptor, StructureQuantifications quantifs, boolean verbose) {
        if (observedDescriptor==null || observedDescriptor.length==0) return;
        ArrayUtil observedDistances = new ArrayUtil(observedDescriptor);
        observedDistances.sort();
        double max = observedDescriptor[observedDescriptor.length-1];
        int lim = (sampledDescriptor.length)/2;
        ArrayUtil[] sampleDistancesAVG = new ArrayUtil[lim];
        for (int i = 0; i<lim; i++) {
            
            sampleDistancesAVG[i]=new ArrayUtil(sampledDescriptor[i]);
            sampleDistancesAVG[i].sort();
            double mt= sampledDescriptor[i][sampledDescriptor[i].length-1];
            if (mt>max) max=mt;
        }
        ArrayUtil[] sampleDistancesENV = new ArrayUtil[sampledDescriptor.length-lim];
        for (int i = lim; i<sampledDescriptor.length; i++) {
            sampleDistancesENV[i-lim]=new ArrayUtil(sampledDescriptor[i]);
            sampleDistancesENV[i-lim].sort();
        }
        nBins = nBins_P.getIntValue(nBins);
        ArrayUtil xEvalsBins = new ArrayUtil(nBins);
        double[] x = xEvalsBins.getArray();
        double coeff = max / ((double) nBins);
        for (int i = 0; i < nBins; i++) x[i]= ((double) i) * coeff;
        quantifs.setQuantificationStructureArray(x_P, x);
        ArrayUtil[] env = CDFTools.cdfPercentage2(sampleDistancesENV, xEvalsBins, enveloppe.getDoubleValue(0.05d) / 2.0);
        double[] acdfArray = CDFTools.cdfAverage(sampleDistancesAVG, xEvalsBins).getArray();
        double[] observedArray = CDFTools.cdf(observedDistances, xEvalsBins).getArray();
        quantifs.setQuantificationStructureArray(lower, env[0].getArray());
        quantifs.setQuantificationStructureArray(upper, env[1].getArray());
        quantifs.setQuantificationStructureArray(acdf, acdfArray);
        quantifs.setQuantificationStructureArray(observed, observedArray);
        
        if (verbose) {
            Plot plotF = new Plot("Spatial Analysis", "distance", "Spatial Descriptor", x, observedArray);
            plotF.setLimits(x[0], x[x.length-1], 0, 1);
            //plotF.setLimits(max, , 0, 1);
            //env
            plotF.setColor(Color.green);
            plotF.addPoints(x, env[0].getArray(), Plot.LINE);
            plotF.setColor(Color.green);
            plotF.addPoints(x, env[1].getArray(), Plot.LINE);
            // average
            plotF.setColor(Color.red);
            plotF.addPoints(x, acdfArray, Plot.LINE);
            // observed
            plotF.setColor(Color.blue);
            plotF.addPoints(x, observedArray, Plot.LINE);
            plotF.show();
        }
    }
    
    public String[] getCurveKeys() {
        return new String[]{x_P.getKey(), lower.getKey(), upper.getKey(), acdf.getKey(), observed.getKey()};
    }
}
