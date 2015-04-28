/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package tango.plugin.thresholder;

import tango.plugin.TangoPlugin;

/**
 *
 * @author jean
 */
public interface ThresholderHistogram extends TangoPlugin {
    public double getThreshold(int[] histogram, double binSize, double min);
}
