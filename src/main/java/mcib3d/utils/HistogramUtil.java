/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mcib3d.utils;

import ij.gui.Plot;

/**
 *
 **
 * /**
 * Copyright (C) 2008- 2011 Thomas Boudier
 *
 *
 *
 * This file is part of mcib3d
 *
 * mcib3d is free software; you can redistribute it and/or modify it under the
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
 * @author thomas
 */
public class HistogramUtil extends ArrayUtil {

    //ArrayUtil rawData = null;
    double[] xbin = null;
    double[] ynumber = null;
    double[] ycumul = null;

    /**
     *
     * @param val
     */
    public HistogramUtil(double[] val) {
        super(val);
        ynumber = val;
        setIntegerBins();
    }

    private void setIntegerBins() {
        xbin = new double[ynumber.length];
        for (int i = 0; i < xbin.length; i++) {
            xbin[i] = i;
        }
    }

    
    /**
     *
     */
    private void computeCumulHistogram() {
        ycumul = new double[xbin.length];
        ycumul[0] = ynumber[0];
        for (int i = 1; i < xbin.length; i++) {
            ycumul[i] = ycumul[i - 1] + ynumber[i];
        }
    }

    private void computeInverseCumulHistogram() {
        int le = xbin.length;
        ycumul = new double[le];
        ycumul[le - 1] = ynumber[le - 1];
        for (int i = le - 2; i >= 0; i--) {
            ycumul[i] = ycumul[i + 1] + ynumber[i];
        }
    }

    /**
     *
     * @param title
     */
    public void plotHistogram(String title) {
        Plot plot = new Plot(title, "values", "nb", xbin, ynumber);
        plot.show();
    }

    /**
     *
     * @param title
     */
    public void plotCumulHistogram(String title) {
        if (ycumul == null) {
            this.computeCumulHistogram();
        }
        Plot plot = new Plot(title, "values", "nb", xbin, ycumul);
        plot.show();
    }

    /**
     *
     * @return
     */
    public int getNbBins() {
        return xbin.length;
    }

    /**
     *
     * @return
     */
    public double[] getBins() {
        return xbin;
    }

    /**
     *
     * @param bi
     */
    public void setBins(double[] bi) {
        xbin = bi;
    }

    /**
     *
     * @return
     */
    public double[] getYnumbers() {
        return ynumber;
    }

    /**
     *
     * @return
     */
    public double[] getCumulNumbers() {
        if (ycumul == null) {
            computeCumulHistogram();
        }
        return ycumul;
    }

    /**
     *
     * @return
     */
    public double[] getInverseCumulNumbers() {
        if (ycumul == null) {
            computeInverseCumulHistogram();
        }
        return ycumul;
    }

    /**
     *
     * @param ynumber
     */
    public void setYnumber(double[] ynumber) {
        this.ynumber = ynumber;
    }

    /**
     *
     * @param bi
     * @return
     */
    public double getNumber(int bi) {
        return ynumber[bi];
    }

    /**
     *
     * @param bi
     * @return
     */
    public double getCumulNumber(int bi) {
        if (ycumul == null) {
            computeCumulHistogram();
        }
        return ycumul[bi];
    }
}
