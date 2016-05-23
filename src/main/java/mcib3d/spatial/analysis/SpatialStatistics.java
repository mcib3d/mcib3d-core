/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mcib3d.spatial.analysis;

import ij.IJ;
import ij.gui.Plot;
import java.awt.Color;
import mcib3d.geom.Objects3DPopulation;
import mcib3d.utils.ArrayUtil;
import mcib3d.utils.CDFTools;
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

    // final sdi value
    private double sdi = Double.NaN;

    private double env = 0.05;
    private Color ColorAVG = Color.red;
    private Color ColorENV = Color.green;
    private Color ColorOBS = Color.blue;
    private Plot plot = null;

    // misc
    private boolean verbose = true;
    private int nbCpus = 1;

    // intermediate values
    private ArrayUtil xEvals;
    private ArrayUtil obsDesc;
    private ArrayUtil obsCD;
    private ArrayUtil averageCD;
    private ArrayUtil samplesEnvLow;
    private ArrayUtil samplesEnvHigh;
    private ArrayUtil xEvalsEnv;

    public SpatialStatistics(SpatialDescriptor descriptor, SpatialModel model, int nbSamples, Objects3DPopulation observed) {
        this.descriptor = descriptor;
        this.model = model;

        this.observed = observed;
        this.nbSamples = nbSamples;
        if (!this.model.init()) {
            IJ.log("Pb with model");
        }
        if (!this.descriptor.init()) {
            IJ.log("Pb with descriptor");
        }
        int nbObj = observed.getNbObjects();
    }

    private void compute() {
        // Observed
        if (verbose) {
            IJ.log("Computing " + descriptor.getName() + " for observed data");
        }
        obsDesc = descriptor.compute(observed);
        int nbDesc = obsDesc.getSize();
        obsDesc.sort();
        obsCD = CDFTools.cdf(obsDesc);
        // Samples for average
        if (verbose) {
            IJ.log("Average : Computing " + descriptor.getName() + " for " + nbSamples + " " + model.getName() + " data");
        }
        //model.getSampleImage().show();
        ArrayUtil[] samplesDesc = getSamples();
        // x values for computation
        xEvals = new ArrayUtil(nbSamples * nbDesc);
        for (int i = 0; i < nbSamples; i++) {
            xEvals.insertValues(i * nbDesc, samplesDesc[i]);
        }
        xEvals.sort();
        // compute average
        averageCD = CDFTools.cdfAverage(samplesDesc, xEvals);

        // New samples for envellope and ranking
        if (verbose) {
            IJ.log("Envellope : Computing " + descriptor.getName() + " for " + nbSamples + " " + model.getName() + " data");
        }
        samplesDesc = null;
        System.gc();
        samplesDesc = getSamples();
        // uniform spaced 
        double max = xEvals.getMaximum();
        int nbBins = 1000;
        xEvalsEnv = new ArrayUtil(nbBins);
        for (int i = 0; i < nbBins; i++) {
            xEvalsEnv.addValue(i, ((double) i) * max / ((double) nbBins));
        }
        samplesEnvLow = CDFTools.cdfPercentage(samplesDesc, xEvalsEnv, env / 2.0);
        samplesEnvHigh = CDFTools.cdfPercentage(samplesDesc, xEvalsEnv, 1.0 - env / 2.0);

        if (verbose) {
            IJ.log("Computing " + descriptor.getName() + " sdi");
        }
        sdi = CDFTools.SDI(obsDesc, samplesDesc, averageCD, xEvals);
    }

    private ArrayUtil[] getSamples() {
        final ArrayUtil[] samplesDesc = new ArrayUtil[nbSamples];

        for (int i = 0; i < nbSamples; i++) {
            if (verbose) {
                IJ.showStatus("Random population " + (i + 1));
            }
            ArrayUtil tmp = descriptor.compute(model.getSample());
            tmp.sort();
            samplesDesc[i] = tmp;

        }

        return samplesDesc;
    }

    /* USE WITH CARE ;) */
//    private ArrayUtil[] getSamplesParallel() {
//        final ArrayUtil[] samplesDesc = new ArrayUtil[nbSamples];
//        final AtomicInteger ai = new AtomicInteger(0);
//        nbCpus = 1;
//        final int n_cpus = nbCpus == 0 ? ThreadUtil.getNbCpus() : nbCpus;
//        Thread[] threads = ThreadUtil.createThreadArray(n_cpus);
//        final int dec = (int) Math.ceil((double) nbSamples / (double) n_cpus);
//        final SpatialDescriptor[] sps = new SpatialDescriptor[n_cpus];
//        final SpatialDescriptor sp = descriptor.copy();
//        for (int i = 0; i < sps.length; i++) {
//            sps[i] = sp.copy();
//            sps[i].init();
//        }
//        final SpatialModel[] sms = new SpatialModel[n_cpus];
//        final SpatialModel sm = model.copy();
//        for (int i = 0; i < sms.length; i++) {
//            sms[i] = sm.copy();
//            sms[i].init();
//        }
//        for (int ithread = 0; ithread < threads.length; ithread++) {
//            threads[ithread] = new Thread() {
//                @Override
//                public void run() {
//                    for (int k = ai.getAndIncrement(); k < n_cpus; k = ai.getAndIncrement()) {
//                        for (int i = dec * k; ((i < (dec * (k + 1))) && (i < nbSamples)); i++) {
//                            if (verbose) {
//                                IJ.log("Random population " + (i + 1) + " by processor " + (k + 1));
//                            }
//                            ArrayUtil tmp = descriptor.compute(model.getSample());
//                            tmp.sort();
//                            samplesDesc[i] = tmp;
//                            if (verbose) {
//                                IJ.log("Random population " + (i + 1) + " by processor " + (k + 1) + " finished");
//                            }
//                        }
//                    }
//                }
//            };
//        }
//        ThreadUtil.startAndJoin(threads);
//
//        return samplesDesc;
//    }
    private void createPlot() {
        if (Double.isNaN(sdi)) {
            compute();
        }
        double plotmaxX = obsDesc.getMaximum();
        double plotmaxY = obsCD.getMaximum();

        // get the limits        
        if (xEvalsEnv.getMaximum() > plotmaxX) {
            plotmaxX = xEvalsEnv.getMaximum();
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
        if (obsCD.getMaximum() > plotmaxY) {
            plotmaxY = obsDesc.getMaximum();
        }
        if (obsDesc.getMaximum() > plotmaxX) {
            plotmaxX = obsDesc.getMaximum();
        }
        // create the plot
        plot = new Plot(descriptor.getName() + "_" + model.getName(), "distance", "cumulated frequency");
        plot.setLimits(0, plotmaxX, 0, plotmaxY);

        // enveloppe
        plot.setColor(ColorENV);
        plot.addPoints(xEvalsEnv.getArray(), samplesEnvLow.getArray(), Plot.LINE);
        plot.setColor(ColorENV);
        plot.addPoints(xEvalsEnv.getArray(), samplesEnvHigh.getArray(), Plot.LINE);

        // average
        plot.setColor(ColorAVG);
        plot.addPoints(xEvals.getArray(), averageCD.getArray(), Plot.LINE);

        // observed
        plot.setColor(ColorOBS);
        plot.addPoints(obsDesc.getArray(), obsCD.getArray(), Plot.LINE);
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

    public void setVerbose(boolean verbose) {
        this.verbose = verbose;
    }

//    public void setNbCpus(int nbCpus) {
//        this.nbCpus = nbCpus;
//    }
    public void setEnvellope(double env) {
        this.env = env;
    }

}
