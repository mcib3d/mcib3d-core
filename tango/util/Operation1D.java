
package tango.util;

import ij.gui.Plot;
import ij.measure.CurveFitter;
import tango.gui.Core;

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
public class Operation1D {
    
    public static double[] gaussianSmooth(double[] values, double sigma, int radius) {
        double[] coeffs = new double[radius+1];
        coeffs[0]=1/(sigma*Math.sqrt(2d*Math.PI));
        double sigma2 = 2*sigma*sigma;
        for (int i = 1; i<=radius; i++) {
            double x2 = i*i;
            coeffs[i]=coeffs[0]*Math.exp(-x2/sigma2);
        }
        double[] res = new double[values.length];
        for (int i = 0; i<values.length; i++) {
            res[i]+=values[i]*coeffs[0];
            for (int j = 1; j<=radius; j++) {
                if ((i+j)<values.length) res[i]+=coeffs[j]*values[i+j]; 
                if ((i-j)>=0) res[i]+=coeffs[j]*values[i-j]; 
            }
        }
        return res;
    }
    public static int getMaxIndex(double[] values, int start, int stop) {
        start=Math.max(start, 0);
        stop = Math.min(stop, values.length-1);
        double max = values[start];
        int idx=start;
        for (int i = start+1; i<=stop; i++) if (values[i]>max) {
            max=values[i];
            idx=i;
        } 
        return idx;
    }
    
    public static int getUnderLimitIndex(double[] values, double limit, boolean increasingIndexes, int start, int stop) {
        start=Math.max(start, 0);
        stop = Math.min(stop, values.length-1);
        if (increasingIndexes) {
            int idx = start;
            while (idx<stop && values[idx]<limit) idx++;
            return idx;
        } else {
            int idx = stop;
            while (idx>start && values[idx]<limit) idx--;
            return idx;
        }
    }
    
    public static double[] gaussianFit(double[] x, double[] values, int start, int stop) {
        double[] newVals = new double[stop-start+1];
        System.arraycopy(values, start, newVals, 0, newVals.length);
        if (x==null) {
            x = new double[newVals.length];
            for (int i = 0; i<x.length; i++) x[i]=i+start;
        }
        if (Core.debug) {
            Plot p = new Plot("Histogram", "bins", "count", x, newVals);
            p.show();
        }
        double minVal = newVals[0];
        double maxVal = newVals[0];
        double val;
        for (int i = 0; i <x.length; i++) {
            val = newVals[i];
            if (val > maxVal) {
                maxVal = val;
            }
            if (val < minVal) {
                minVal = val;
            }
        }
        //initial sigma estimation
        int modalIndex = getMaxIndex(newVals, 0, newVals.length-1);
        double[] moments1 = getMoments(x, newVals, 0, newVals.length-1);
        //start = Math.max(start, (int)(modalIndex-3*moments1[1]+0.5));
        //stop = Math.min(stop, (int)(modalIndex+3*moments1[1]+0.5));
        //double[] moments2 = getMoments(x, values, start, stop);
        CurveFitter fit = new CurveFitter(x, newVals);
        double[] params = new double[4];
        params[0] = minVal;
        params[1] = maxVal;
        params[2] = modalIndex; //modal values
        params[3] = moments1[1]; //sigma
        if (Core.debug) ij.IJ.log("gaussian fit init parameters: min" + params[0] + " max:" + params[1] + " peak:" + params[2] + " sigma" + params[3]);
        
        fit.setInitialParameters(params);
        fit.setMaxIterations(10000);
        fit.setRestarts(1000);
        fit.doFit(CurveFitter.GAUSSIAN);
        return fit.getParams();
    }
    
        public static double[] gammaFit(double[] x, double[] values, int start, int stop) {
        if (x==null) {
            x = new double[values.length];
            for (int i = 1; i<values.length; i++) x[i]=i;
        }
        double minVal = values[0];
        double maxVal = values[0];
        double val;
        for (int i = start; i <= stop; i++) {
            val = values[i];
            if (val > maxVal) {
                maxVal = val;
            }
            if (val < minVal) {
                minVal = val;
            }
        }
        //initial sigma estimation
        int modalIndex = getMaxIndex(values, start, stop);
        double[] moments1 = getMoments(x, values, start, stop);
        double[] moments2 = getMoments(x, values, Math.max(start, (int)(modalIndex-3*moments1[1]+0.5)), Math.min(stop, (int)(modalIndex+3*moments1[1]+0.5)));
        // do the fit only between start & stop?
        CurveFitter fit = new CurveFitter(x, values);
        
        double[] params = new double[4];
        params[0] = start;
        params[1] = maxVal;
        params[2] = modalIndex; //modal values
        params[3] = moments2[0];
        if (Core.debug) ij.IJ.log("gaussian fit init parameters: min" + params[0] + " max:" + params[1] + " peak:" + params[2] + " sigma" + params[3]);
        
        fit.setInitialParameters(params);
        fit.setMaxIterations(10000);
        fit.setRestarts(1000);
        fit.doFit(CurveFitter.GAMMA_VARIATE);
        return fit.getParams();
    }
    
    public static double[] getMoments(double[] x, double[] values, int start, int stop) {
        double mean=0;
        double mean2=0;
        double sigma=0;
        double count=0;
        for (int i = start; i<=stop; i++) {
            count+=values[i];
            mean+=values[i]*x[i];
            mean2+=values[i]*x[i]*x[i];
        }
        if (count>0) {
            mean/=(count);
            sigma = Math.sqrt(mean2/count - mean*mean);
        }
        return new double[]{mean, sigma};
    }
}
