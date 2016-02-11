/**
 *
**
 * /**
Copyright (C) 2008- 2012 Thomas Boudier and others
 *

 *
This file is part of mcib3d

mcib3d is free software; you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation; either version 3 of the License, or
(at your option) any later version.

This program is distributed cdfIn the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package mcib3d.utils;

import ij.IJ;

/**
 *
 * @author Philippe Andrey
 */
public class CDFTools {

    /**
     *
     * @param x
     * @return
     */
    static public ArrayUtil cdf(ArrayUtil x) {
        // if x.isIncreasing == false... error
        final int n = x.getSize();
        ArrayUtil y = new ArrayUtil(n);

        for (int i = 0; i < n; ++i) {
            y.putValue(i, (double) (i + 1) / n);
        }

        return y;
    }

    /**
     *
     * @param x
     * @param xEvals
     * @return
     */
    static public ArrayUtil cdf(ArrayUtil x, ArrayUtil xEvals) {
        // if x.isIncreasing == false... error
        final int n = x.getSize();
        final int numEvals = xEvals.getSize();
        ArrayUtil y = new ArrayUtil(numEvals);
        int i, p;

        for (i = 0, p = 0; i < n; ++i) {
            while ((p < numEvals) && (xEvals.getValue(p) < x.getValue(i))) {
                y.putValue(p++, (double) (i) / n);
            }
        }

        while (p < numEvals) {
            y.putValue(p++, 1.0);
        }

        return y;
    }

    /**
     *
     * @param x
     * @return
     */
    static public ArrayUtil cdfAverage(ArrayUtil[] x) {
        final int n = x.length;
        int total_size = 0;
        for (int i = 0; i < n; i++) {
            total_size += x[i].getSize();
        }
        ArrayUtil xEvals = new ArrayUtil(total_size);
        total_size = 0;
        for (int i = 0; i < n; ++i) {
            xEvals.insertValues(total_size, x[i]);
            total_size += x[i].getSize();
        }

        return cdfAverage(x, xEvals);
    }

    /**
     *
     * @param x
     * @param xEvals
     * @return
     */
    static public ArrayUtil cdfAverage(ArrayUtil[] x, ArrayUtil xEvals) {
        final int n = x.length;
        final int numEvals = xEvals.getSize();
        ArrayUtil y = new ArrayUtil(numEvals);

        for (int i = 0; i < n; ++i) {
            y.addValueArray(cdf(x[i], xEvals));
        }
        y.divideAll(n);

        return y;
    }

    /**
     *
     * @param x
     * @param xEvals
     * @param pc
     * @return
     */
    static public ArrayUtil cdfPercentage(ArrayUtil[] x, ArrayUtil xEvals, double pc) {
        final int n = x.length;
        final int numEvals = xEvals.getSize();
        ArrayUtil y = new ArrayUtil(numEvals);

        ArrayUtil[] xCDF = new ArrayUtil[n];
        for (int i = 0; i < n; i++) {
            xCDF[i] = cdf(x[i], xEvals);
        }
        ArrayUtil tmp;
        for (int i = 0; i < numEvals; i++) {
            tmp = new ArrayUtil(n);
            for (int j = 0; j < n; j++) {
                tmp.addValue(j, xCDF[j].getValue(i));
            }
            tmp.sort();
            y.addValue(i, tmp.getValue((int) Math.round(pc * (n - 1))));
        }

        return y;
    }
    
    /**
     *
     * @param x
     * @param xEvals
     * @param pc
     * @return ArrayUtil[0] => pc ; ArrayUtil[1] => 1-pc
     */
    static public ArrayUtil[] cdfPercentage2(ArrayUtil[] x, ArrayUtil xEvals, double pc) {
        final int n = x.length;
        final int numEvals = xEvals.getSize();
        ArrayUtil y1 = new ArrayUtil(numEvals);
        ArrayUtil y2 = new ArrayUtil(numEvals);
        ArrayUtil[] xCDF = new ArrayUtil[n];
        for (int i = 0; i < n; i++) {
            xCDF[i] = cdf(x[i], xEvals);
        }
        ArrayUtil tmp;
        for (int i = 0; i < numEvals; i++) {
            tmp = new ArrayUtil(n);
            for (int j = 0; j < n; j++) {
                tmp.addValue(j, xCDF[j].getValue(i));
            }
            tmp.sort();
            y1.addValue(i, tmp.getValue((int) Math.round(pc * (n - 1))));
            y2.addValue(i, tmp.getValue((int) Math.round((1-pc) * (n - 1))));
        }

        return new ArrayUtil[]{y1, y2};
    }

    /**
     *
     * @param x1
     * @param y1
     * @param x2
     * @param y2
     * @return
     */
    static public double[] cdfDifferences(ArrayUtil x1, ArrayUtil y1, ArrayUtil x2, ArrayUtil y2) {
        // check tri croissant de x1 et x2
        final int n = x1.getSize();
        final int m = x2.getSize();
        int i = 0, j = 0, iprev, jprev;
        double diff;
        double[] maxDiff;
        double[] maxDiffAbove = new double[2];
        double[] maxDiffBelow = new double[2];
        double v1 = 0, v2 = 0;

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

            diff = v1 - v2;

            if (diff > 0.0 && diff > maxDiffAbove[1]) {
                maxDiffAbove[0] = 0.5 * (x1.getValue(iprev) + x2.getValue(jprev));
                maxDiffAbove[1] = diff;
            }

            if (diff < 0.0 && diff < maxDiffBelow[1]) {
                maxDiffBelow[0] = 0.5 * (x1.getValue(iprev) + x2.getValue(jprev));
                maxDiffBelow[1] = diff;
            }
        }

        maxDiff = maxDiffAbove[1] >= Math.abs(maxDiffBelow[1]) ? maxDiffAbove : maxDiffBelow;

        return maxDiff;
    }

    /**
     *
     * @param x
     * @param samples
     * @param averageCDF
     * @param xEvals
     * @return
     */
    static public int rank(ArrayUtil x, ArrayUtil[] samples, ArrayUtil averageCDF, ArrayUtil xEvals) {
        final int numSamples = samples.length;
        ArrayUtil maxDifferences = new ArrayUtil(1 + numSamples);
        double[] maxDiffBuffer;
        double xMaxDiff;

        maxDiffBuffer = cdfDifferences(x, cdf(x), xEvals, averageCDF);
        xMaxDiff = maxDiffBuffer[1];
        maxDifferences.putValue(0, xMaxDiff);
        //IJ.log("Observed max diff " + maxDiffBuffer[1] + " " + maxDiffBuffer[0]);

        for (int i = 0; i < numSamples; ++i) {
            maxDiffBuffer = cdfDifferences(samples[i], cdf(samples[i]), xEvals, averageCDF);
            maxDifferences.putValue(i + 1, maxDiffBuffer[1]);
        }

        /*
         * maxDifferences.sort(); IJ.log( "Max diffs = " + maxDifferences );
         *
         * int r = 0; while ( maxDifferences.getValue(r) != xMaxDiff ) { r++; }
         * return r;
         */
        //System.cdfOut.println("array maxdiff=" + maxDifferences);
        maxDifferences.sort();
        //System.cdfOut.println("array maxdiff=" + maxDifferences);
        return maxDifferences.indexOf(xMaxDiff);
    }

    /**
     *
     * @param x
     * @param samples
     * @param averageCDF
     * @param xEvals
     * @return
     */
    static public double SDI(ArrayUtil x, ArrayUtil[] samples, ArrayUtil averageCDF, ArrayUtil xEvals) {
        final int numSamples = samples.length;
        final int rank = rank(x, samples, averageCDF, xEvals);
        //IJ.log("Rank = " + rank);
        return 1.0 - (double) rank / (double) numSamples;
    }
    
    public static ArrayUtil resampleCdf(ArrayUtil cdf, ArrayUtil xEvals, ArrayUtil newXEvals) {
        double[] cdfIn = cdf.getArray();
        double[] x=xEvals.getArray();
        double[] newX=newXEvals.getArray();
        double[] cdfOut = new double[newX.length];
        int start=0;
        int p=0;
        while(x[0]>newX[start]) start++;
        cdfOut[start]=cdfIn[0];
        int lim=x.length-1;
        for (int i = start+1; i < newX.length; i++) {
            // FIXME à verifier...
            while (p<lim && x[p+1]<=newX[i]) p++;
            cdfOut[i]=cdfIn[p];
        }
        return new ArrayUtil(cdfOut);
    }
}
