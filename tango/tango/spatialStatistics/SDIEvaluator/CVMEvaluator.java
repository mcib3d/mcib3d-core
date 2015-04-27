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
import tango.spatialStatistics.SDIEvaluator.filter1d.Filter1d;

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
public class CVMEvaluator implements SDIEvaluator {
    Filter1dParameter filter = new Filter1dParameter("Filter:", "filter", null);
    Parameter[] parameters = new Parameter[]{filter};
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
        return parameters;
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
        int rank = rank(observedDistances, sampleDistancesSDI, averageCDF, xEvals, filter.getPlugin(nCPUs, verbose));
        double sdi = 1.0 - (double) rank / (double) sampleDistancesSDI.length;
        quantifs.setQuantificationStructureNumber(sdi_P, sdi);
    }

    @Override
    public String getHelp() {
        return "";
    }
    
    
    static public int rank(ArrayUtil x, ArrayUtil[] samples, ArrayUtil averageCDF, ArrayUtil xEvals, Filter1d f) {
        final int numSamples = samples.length;
        ArrayUtil differences = new ArrayUtil(1 + numSamples);
        double obsDiff, sampleDiff;

        obsDiff = cdfDifferences(x, CDFTools.cdf(x), xEvals, averageCDF, f);
        differences.putValue(0, obsDiff);
        //IJ.log("Observed max diff " + maxDiffBuffer[1] + " " + maxDiffBuffer[0]);

        for (int i = 0; i < numSamples; ++i) {
            sampleDiff = cdfDifferences(samples[i], CDFTools.cdf(samples[i]), xEvals, averageCDF, f);
            differences.putValue(i + 1, sampleDiff);
        }

        /*
         * maxDifferences.sort(); IJ.log( "Max diffs = " + maxDifferences );
         *
         * int r = 0; while ( maxDifferences.getValue(r) != xMaxDiff ) { r++; }
         * return r;
         */
        //System.cdfOut.println("array maxdiff=" + maxDifferences);
        differences.sort();
        //System.cdfOut.println("array maxdiff=" + maxDifferences);
        return differences.indexOf(obsDiff);
    }
    
    static public double cdfDifferences(ArrayUtil x1, ArrayUtil y1, ArrayUtil x2, ArrayUtil y2, Filter1d f) {
        // check tri croissant de x1 et x2
        final int n = x1.getSize();
        final int m = x2.getSize();
        int i = 0, j = 0, iprev, jprev;
        double diff=0;
        double v1 = 0, v2 = 0, curX, prevX;

        while (i < n && j < m) {
            iprev = i;
            jprev = j;

            if (x1.getValue(i) < x2.getValue(j)) {
                v1 = y1.getValue(i++);
            } else if (x2.getValue(j) < x1.getValue(i)) {
                v2 = y2.getValue(j++);
            } else // les deux positions sont identiques
            {
                v1 = y1.getValue(i++);
                v2 = y2.getValue(j++);
            }
            curX=0.5 * (x1.getValue(i) + x2.getValue(j));
            prevX=0.5 * (x1.getValue(iprev) + x2.getValue(jprev));
            
            diff += (v1 - v2) * (curX-prevX) * f.getValue(0.5 * (curX+prevX));
        }
        return diff;
    }
    
}
