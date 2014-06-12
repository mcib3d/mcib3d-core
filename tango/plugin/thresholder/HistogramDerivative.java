package tango.plugin.thresholder;

import tango.parameter.DoubleParameter;
import tango.parameter.Parameter;
import ij.IJ;
import ij.ImagePlus;
import ij.gui.Plot;
import java.util.HashMap;
import mcib3d.image3d.ImageByte;
import mcib3d.image3d.ImageHandler;
import mcib3d.image3d.ImageInt;
import mcib3d.image3d.ImageStats;
import tango.dataStructure.InputImages;
import tango.parameter.*;
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
public class HistogramDerivative implements Thresholder {
    boolean debug, superDebug;
    int nCPUs=1;
    static String[] derivativeOrders = new String[]{"0", "1", "2"};
    ChoiceParameter derivativeOrder = new ChoiceParameter("Derivative order:", "derivativeOrder", derivativeOrders, derivativeOrders[1]); 
    DoubleParameter numberLimit = new DoubleParameter("Pixel Number Limit : ", "numberLimit", 400d, Parameter.nfDEC1);
    SliderDoubleParameter propLimit = new SliderDoubleParameter("Pixel Proportion Limit : ", "proportionLimit", 0d, 0.1d, 0.0001d, 5);
    DoubleParameter sigma = new DoubleParameter("Sigma : ", "sigma", 3d, Parameter.nfDEC1);
    
    static String[] methods = new String[]{"Max Value", "Pixel number limit", "Pixel proportion limit"}; //, "Gaussian Fit", "Gamma Fit"
    ChoiceParameter method = new ChoiceParameter("Operation:", "operation", methods, methods[0]); 
    HashMap<Object, Parameter[]> map = new HashMap<Object, Parameter[]>(){{
        put(methods[0], new Parameter[]{}); 
        put(methods[1], new Parameter[]{numberLimit});
        put(methods[2], new Parameter[]{propLimit});
        //put(methods[3], new Parameter[]{sigma});
        //put(methods[4], new Parameter[]{sigma});
    }};
    ConditionalParameter cond= new ConditionalParameter(method, map);
    
    Parameter[] parameters=new Parameter[] {
        derivativeOrder, cond
    };

    @Override
    public Parameter[] getParameters() {
        return parameters;
    }

    @Override
    public void setVerbose(boolean debug) {
        this.debug=debug;
    }
    
    @Override
    public void setMultithread(int nCPUs) {
        this.nCPUs=nCPUs;
    }

    @Override
    public double runThresholder(ImageHandler in, InputImages images) {
        ImageInt mask = images.getMask();
        int[] histo=in.getHistogram(mask);
        double[] histoCumul=getCumul(histo);
        int start = 1;
        if (derivativeOrder.getSelectedItem().equals(derivativeOrders[1])) start=2;
        else if (derivativeOrder.getSelectedItem().equals(derivativeOrders[2])) start = 5;
        int stop = histoCumul.length-1;
        double bin;
        /*if (this.method.getSelectedItem().equals(methods[3])) {
            double[] fit =tango.util.Operation1D.gaussianFit(null, histoCumul, start, stop);
            if (debug) ij.IJ.log("HistogramDerivative::Gaussian Fit: peak index:"+fit[2]+ " sigma:"+fit[3]);
            bin = fit[2]+sigma.getDoubleValue(3)*fit[3];
        } 
        * 
        */
        /*else if (this.method.getSelectedItem().equals(methods[4])) {
            double[] fit =tango.util.Operation1D.gammaFit(null, histoCumul, start, stop);
            if (debug) ij.IJ.log("HistogramDerivative::Gamma Fit: peak index:"+fit[2]+ " scale:"+fit[3]);
            bin = fit[2]+sigma.getDoubleValue(3)*fit[3];
        }*/ 
        if (this.method.getSelectedItem().equals(methods[1])) {
            double limit = this.numberLimit.getDoubleValue(400);
            bin = tango.util.Operation1D.getUnderLimitIndex(histoCumul, limit, false, start, stop);
        } else if (this.method.getSelectedItem().equals(methods[2])) {
            double prop = this.propLimit.getValue();
            double count = 0;
            for (int i = 0; i<histo.length;i++) count+=histo[i];
            double limit = count*prop;
            if (debug) ij.IJ.log(("Histogram Derivative: proportion:"+prop+ " pixel number limit:"+limit+ " total pixels:"+count));
            bin = tango.util.Operation1D.getUnderLimitIndex(histoCumul, limit, false, start, stop);
        } else {
            bin=tango.util.Operation1D.getMaxIndex(histoCumul, start, stop);
        }
        ImageStats s = in.getImageStats(mask);
        double coeff= s.getHisto256BinSize();
        double min = in.getMin(mask);
        double thld = bin * coeff+min;
        
        if (true && debug) {
            double[] xvals = new double[256];
            for (int i =0; i<256; i++) xvals[i]=i*coeff+min;
            Plot p = (new Plot("histo cumul", "thld", "pixels number", xvals, histoCumul));
            //p4.setLimits(0, 255, 0, 50000);
            p.show();
        }
        if (debug) IJ.log("HistogramDerivative: thld:"+thld);
        return thld;
    }
    
    private double[] getCumul(int[] histo) {
        double[] histoCumul=new double[256];
        histoCumul[255]=histo[255];
        for (int i=254;i>=0;i--) histoCumul[i]=histoCumul[i+1]+histo[i];
        double[] histoCumulSmooth = tango.util.Operation1D.gaussianSmooth(histoCumul, 2, 4);
        if (this.derivativeOrder.getSelectedItem().equals(derivativeOrders[0])) return histoCumulSmooth;
        else {
            double[] der = new double[256];
            for (int i = 1; i<255; i++) der[i]=histoCumulSmooth[i-1]-histoCumulSmooth[i];
            double[] derSmooth = tango.util.Operation1D.gaussianSmooth(der, 2, 4);
            if (this.derivativeOrder.getSelectedItem().equals(derivativeOrders[1])) {
                derSmooth[1]=derSmooth[2];
                derSmooth[0]=derSmooth[2];
                return derSmooth;
            }
            else {
                double[] der2 = new double[256];
                for (int i = 1; i<255; i++) der2[i]=derSmooth[i-1]-derSmooth[i];
                double[] der2Smooth = tango.util.Operation1D.gaussianSmooth(der2, 2, 4);
                der2Smooth[1]=der2Smooth[5];
                der2Smooth[0]=der2Smooth[5];
                der2Smooth[2]=der2Smooth[5];
                der2Smooth[3]=der2Smooth[5];
                der2Smooth[4]=der2Smooth[5];
                return der2Smooth;
                
            }
        }
    }

    @Override
    public String getHelp() {
        return "Cumulative histogram...";
    }
    
}
