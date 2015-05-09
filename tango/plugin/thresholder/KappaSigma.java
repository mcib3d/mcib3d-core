package tango.plugin.thresholder;

import tango.parameter.DoubleParameter;
import tango.parameter.Parameter;
import ij.IJ;
import ij.ImagePlus;
import mcib3d.image3d.ImageHandler;
import tango.dataStructure.InputImages;
import tango.parameter.IntParameter;
import tango.plugin.thresholder.Thresholder;
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

public class KappaSigma implements Thresholder {
    
    DoubleParameter sigmaFactor = new DoubleParameter("Sigma factor: ", "sigma", 2d, Parameter.nfDEC5);
    IntParameter iterations = new IntParameter("Iterations: ", "iterations", 2);
    Parameter[] parameters=new Parameter[] {sigmaFactor, iterations};

    @Override
    public Parameter[] getParameters() {
        return parameters;
    }
    
    boolean debug;
    int nbCPUs=1;
    @Override
    public void setMultithread(int nCPUs) {
        this.nbCPUs=nCPUs;
    }

    @Override
    public void setVerbose(boolean debug) {
        this.debug=debug;
    }

    @Override
    public double runThresholder(ImageHandler input, InputImages images) {
        double lastThreshold = Double.MAX_VALUE;
        double sf = sigmaFactor.getDoubleValue(2);
        double count, mean, mean2, sigma;
        int iter = iterations.getIntValue(2);
        if (iter<=0) iter=1;
        for (int i = 0; i<iter; i++) {
            count=0;
            mean=0;
            mean2=0;
            sigma=0;
            for (int z = 0; z<input.sizeZ; z++) {
                for (int xy = 0; xy<input.sizeXY; xy++) {
                    double val = input.getPixel(xy, z);
                    if (val<lastThreshold) {
                        mean+=val;
                        mean2+=val*val;
                        count++;
                    }
                }
            }
            if (count>0) {
                mean/=count;
                sigma = Math.sqrt(mean2/count - mean*mean);
            }
            double newThreshold = mean + sf * sigma;
            if (debug) {
                ij.IJ.log("Kappa Sigma Thresholder: Iteration:"+ i+" Mean Background Value: "+mean+ " Sigma: "+sigma+ " threshold: "+newThreshold);
            }
            if (newThreshold == lastThreshold) return lastThreshold;
            else lastThreshold = newThreshold;
        }
        return lastThreshold;
    }

    @Override
    public String getHelp() {
        return "Implementation of Kappa Sigma Clipping algorithm by Gaëtan Lehmann, http://www.insight-journal.org/browse/publication/132. Finds the mean and sigma of the background, and use this two properties to select the pixels significantly different of the background. Mean and sigma are first computed on the entire image, and a threshold is computed as mean + f * sigma. This threshold is then used to select the background, and recompute a new threshold with only pixels in the background. This algorithm shouldn’t converge to a value, so the number of iterations must be provided. In general, two iterations are used.";
    }

    
    
}
