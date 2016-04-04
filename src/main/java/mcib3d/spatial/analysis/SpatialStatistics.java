/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mcib3d.spatial.analysis;

import ij.IJ;
import ij.gui.Plot;
import java.awt.Color;
import java.util.concurrent.atomic.AtomicInteger;
import mcib3d.geom.Objects3DPopulation;
import mcib3d.utils.ArrayUtil;
import mcib3d.utils.CDFTools;
import mcib3d.utils.ThreadUtil;
import mcib3d.spatial.descriptors.SpatialDescriptor;
import mcib3d.spatial.sampler.SpatialModel;

/**
 *
 * @author thomasb
 */
public class SpatialStatistics {

    // required fields
    private SpatialDescriptor descriptor;
    private SpatialModel model;
    private int nbSamples;
    private Objects3DPopulation observed;
    private int nbObj;

    // final sdi value
    private double sdi = Double.NaN;

    // parameters for plot
    private int nbBins = 1000;
    private double env = 0.05;
    private Color ColorAVG = Color.red;
    private Color ColorENV = Color.green;
    private Color ColorOBS = Color.blue;
    private Plot plot = null;

    // misc
    private boolean verbose = false;
    private int nbCpus = 0;

    // intermediate values
    private ArrayUtil xEvals;
    private ArrayUtil observedDistances;
    private ArrayUtil obsDesc;
    private ArrayUtil averageCD;
    private ArrayUtil samplesEnvLow;
    private ArrayUtil samplesEnvHigh;

    public SpatialStatistics(SpatialDescriptor descriptor, SpatialModel model, int nbSamples, Objects3DPopulation observed) {
        this.descriptor = descriptor;
        this.model = model;
        this.observed = observed;
        this.nbSamples = nbSamples;
        if (!model.init()) {
            IJ.log("Pb with model");
        }
        if (!descriptor.init()) {
            IJ.log("Pb with descriptor");
        }
        nbObj = observed.getNbObjects();
    }

    private void compute() {
        // Observed
        obsDesc = descriptor.compute(observed);
        // Samples for average
        ArrayUtil[] samplesDesc = getSamples();
        // x values for computation
        xEvals = new ArrayUtil(nbSamples * nbObj);
        for (int i = 0; i < nbSamples; i++) {
            xEvals.insertValues(i * nbObj, samplesDesc[i]);
        }
        xEvals.sort();
        // compute average
        averageCD = CDFTools.cdfAverage(samplesDesc, xEvals);

        // New samples for envellope and ranking
        samplesDesc = getSamples();
        samplesEnvLow = CDFTools.cdfPercentage(samplesDesc, xEvals, env / 2.0);
        samplesEnvHigh = CDFTools.cdfPercentage(samplesDesc, xEvals, 1.0 - env / 2.0);

        sdi = CDFTools.SDI(obsDesc, samplesDesc, averageCD, xEvals);
    }

    private ArrayUtil[] getSamples() {
        final ArrayUtil[] samplesDesc = new ArrayUtil[nbSamples];
        final AtomicInteger ai = new AtomicInteger(0);
        final int n_cpus = nbCpus == 0 ? ThreadUtil.getNbCpus() : nbCpus;
        Thread[] threads = ThreadUtil.createThreadArray(n_cpus);
        final int dec = (int) Math.ceil((double) nbSamples / (double) n_cpus);
        for (int ithread = 0; ithread < threads.length; ithread++) {
            threads[ithread] = new Thread() {
                @Override
                public void run() {
                    for (int k = ai.getAndIncrement(); k < n_cpus; k = ai.getAndIncrement()) {
                        for (int i = dec * k; ((i < (dec * (k + 1))) && (i < nbSamples)); i++) {
                            if (verbose) {
                                IJ.showStatus("Random population" + (i + 1) + " by processor " + (k + 1));
                            }
                            samplesDesc[i] = descriptor.compute(model.getSample());
                        }
                    }
                }
            };
        }
        ThreadUtil.startAndJoin(threads);

        return samplesDesc;
    }

    private void createPlot() {
        if (Double.isNaN(sdi)) {
            compute();
        }
        double plotmaxX = observedDistances.getMaximum();
        double plotmaxY = obsDesc.getMaximum();

        // low env      
        double max = xEvals.getMaximum();
        ArrayUtil xEvals0 = new ArrayUtil(nbBins);
        for (int i = 0; i < nbBins; i++) {
            xEvals0.addValue(i, ((double) i) * max / ((double) nbBins));
        }

        // get the limits        
        if (xEvals0.getMaximum() > plotmaxX) {
            plotmaxX = xEvals0.getMaximum();
        }
        if (samplesEnvLow.getMaximum() > plotmaxY) {
            plotmaxY = samplesEnvLow.getMaximum();
        }
        if (samplesEnvHigh.getMaximum() > plotmaxY) {
            plotmaxY = samplesEnvHigh.getMaximum();
        }
        if (xEvals.getMaximum() > plotmaxX) {
            plotmaxX = xEvals.getMaximum();
        }
        if (averageCD.getMaximum() > plotmaxY) {
            plotmaxY = averageCD.getMaximum();
        }
        if (obsDesc.getMaximum() > plotmaxY) {
            plotmaxY = obsDesc.getMaximum();
        }
        if (observedDistances.getMaximum() > plotmaxX) {
            plotmaxX = observedDistances.getMaximum();
        }
        // create the plot
        plot = new Plot(descriptor.getName(), "distance", "cumulated frequency");
        plot.setLimits(0, plotmaxX, 0, plotmaxY);

        // enveloppe
        plot.setColor(ColorENV);
        plot.addPoints(xEvals0.getArray(), samplesEnvLow.getArray(), Plot.LINE);
        plot.setColor(ColorENV);
        plot.addPoints(xEvals0.getArray(), samplesEnvHigh.getArray(), Plot.LINE);

        // average
        plot.setColor(ColorAVG);
        plot.addPoints(xEvals.getArray(), averageCD.getArray(), Plot.LINE);

        // observed
        plot.setColor(ColorOBS);
        plot.addPoints(observedDistances.getArray(), obsDesc.getArray(), Plot.LINE);
    }

    public void setColorsPlot(Color avg, Color env, Color obs) {
        ColorAVG = avg;
        ColorENV = env;
        ColorOBS = obs;

        plot = null; // need to redo plot
    }

    public double getSdi() {
        if (Double.isNaN(sdi)) {
            compute();
        }
        return sdi;
    }

    public Plot getPlot() {
        if (plot == null) {
            createPlot();
        }
        return plot;
    }

}
