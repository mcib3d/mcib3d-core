package tango.plugin.thresholder;

import java.util.HashMap;
import mcib3d.image3d.ImageHandler;
import mcib3d.image3d.ImageInt;
import mcib3d.image3d.ImageStats;
import tango.dataStructure.InputFieldImages;
import tango.dataStructure.InputImages;
import tango.parameter.*;
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
public class ThresholdAdjustment implements Thresholder {
    ThresholdParameter threshold = new ThresholdParameter("Threshold method:", "thld", null);
    SliderDoubleParameter per = new SliderDoubleParameter("Proportion of pixels:", "per", -1, 1, 0.5d, 4);
    DoubleParameter sigmaCoeff = new DoubleParameter("Sigma coefficient:", "sigmaCoeff", 1d, Parameter.nfDEC3);
    static String[] methods = new String[]{"Proportion of brigth pixels", "Mean + n * Sigma", "Threshold + n * Sigma"};
    ChoiceParameter choice = new ChoiceParameter("Compute Method:", "computeMethod", methods, methods[2]); 
    static String[] sigmaMethods = new String[]{"Over Threshold", "Under Threshold", "Whole Image"};
    ChoiceParameter choiceSigma = new ChoiceParameter("Compute Sigma:", "computeSigma", sigmaMethods, sigmaMethods[0]); 
    static String[] sigmaMethods2 = new String[]{"Over Threshold", "Under Threshold"};
    ChoiceParameter choiceSigma2 = new ChoiceParameter("Compute Sigma and Mean:", "computeSigma", sigmaMethods2, sigmaMethods2[0]); 
    
    HashMap<Object, Parameter[]> map = new HashMap<Object, Parameter[]>(){{
        put(methods[0], new Parameter[]{per}); 
        put(methods[1], new Parameter[]{choiceSigma2, sigmaCoeff});
        put(methods[2], new Parameter[]{choiceSigma, sigmaCoeff});
    }};
    ConditionalParameter cond= new ConditionalParameter(choice, map);
    
    Parameter[] parameters = new Parameter[]{threshold, cond};
    boolean debug;
    int nCPUs=1;
    public ThresholdAdjustment() {
        sigmaCoeff.setHelp("threshold is computed as follow: thld (computed with the selected method) or mean (depending on selected method) + n * sigma", true);
        choiceSigma.setHelp("if \"Over Threshold\" is selected only pixels above threshold are considered to compute sigma. if \"Under Threshold\" is selected only pixels below threshold are considered to compute sigma. if \"Whole\" is selected every pixels of the image are considered to compute sigma", false);
        per.setHelp("Proportion of bright pixels over the threshold computed with the selected threshold method: e.g: 0.99 corresponds to the level of the 1% brightest pixels among pixels with a value over the threshold. Negative value: idem for dark pixels: e.g. -0.01 corresponds to the 99% brigthest pixel among pixels with a value under the threshold", true);
        choiceSigma2.setHelp("if \"Over Threshold\" is selected only pixels above threshold are considered to compute sigma and mean value. if \"Under Threshold\" is selected only pixels below threshold are considered to compute sigma and mean value.", false);
        
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
    public double runThresholder(ImageHandler input, InputImages images) {
        ImageInt mask = images.getMask();
        double thld = threshold.getThreshold(input, images, nCPUs, debug);
        
        //if (debug) ij.IJ.log("Percentage over thld: histo binSize:"+bin+ " min value:"+min+ " nBins"+histo.length);
        //if (debug) ij.IJ.log("Percentage over thld: nb pix over thld:"+count + " ( approx = "+approx+" )");
        
        if (choice.getSelectedItem().equals(methods[0])) { // percentage of bright pixels
            //if (thld<0) thld=0;
            if (debug) ij.IJ.log("Threshold over thld: thld:"+thld);
            ImageStats s = input.getImageStats(mask);
            int[] histo = input.getHistogram(mask); //(images instanceof InputFieldImages)?null:
            double bin= s.getHisto256BinSize();
            double min = input.getMin(mask);
            int thldIdx = (int) ((thld-min)/bin); 
            double approx = (thld-(thldIdx*bin+min))*histo[thldIdx];//linear approx within thldIdx bin
            double count = approx;
            for (int i = histo.length-1; i>thldIdx; i--) count+=histo[i];
            double p = per.getValue();
            if (p>=0) {
                double limit = count * (1-p);
                count=0;
                int limitIdx = histo.length-1;
                while (count<limit) {
                    count+=histo[limitIdx];
                    limitIdx--;
                }
                double l = (double)limitIdx + (count-limit)/histo[limitIdx];
                if (debug) ij.IJ.log("Percentage over thld: value: "+(l*bin+min)+ " ( histo index:"+l+ " approx = "+((count-limit)/histo[limitIdx])+" )");
                return l*bin+min;
            } else {
                int count2=0;
                for (int i = 0;i<=thldIdx;i++) count2+=histo[i];
                double limit=count2 * (1+p);
                count=0;
                int limitIdx = 0;
                while (count<limit) {
                    count+=histo[limitIdx];
                    limitIdx++;
                }
                double l = (double)limitIdx + (count-limit)/histo[limitIdx];
                if (debug) ij.IJ.log("Percentage over thld: value: "+(l*bin+min)+ " ( histo index:"+l+ " approx = "+((count-limit)/histo[limitIdx])+" )");
                return l*bin+min;
            }
        } else {
            if (choice.getSelectedItem().equals(methods[2])) { // thld + n * sigma
                boolean under = this.choiceSigma.getSelectedIndex()==1;
                boolean whole = this.choiceSigma.getSelectedIndex()==2;
                boolean over = this.choiceSigma.getSelectedIndex()==0;
                double count = 0; 
                double x = 0;
                double x2 = 0;
                for (int z = 0; z<mask.sizeZ; z++) {
                    for (int xy = 0; xy<mask.sizeXY; xy++) {
                        if (mask.getPixel(xy, z)!=0) {
                            double value = input.getPixel(xy, z);
                            if (whole || (over && value>=thld) || (under && value<thld)) {
                                count++;
                                x+=value;
                                x2+=value*value;
                            }
                        }
                    }
                }
                if (count==0) return thld;
                double mean = x / count;
                double sigma = Math.sqrt(x2 / count - mean * mean);
                if (debug) ij.IJ.log("Threshold over Thld: value: "+(thld+sigmaCoeff.getDoubleValue(1) * sigma)+ " sigma:"+sigma+ " thld:"+thld);
                return thld+sigmaCoeff.getDoubleValue(1) * sigma;
            } else if (choice.getSelectedItem().equals(methods[1])) {
                boolean over = this.choiceSigma2.getSelectedIndex()==0;
                double count = 0; 
                double x = 0;
                double x2 = 0;
                for (int z = 0; z<mask.sizeZ; z++) {
                    for (int xy = 0; xy<mask.sizeXY; xy++) {
                        if (mask.getPixel(xy, z)!=0) {
                            double value = input.getPixel(xy, z);
                            if ((over && value>=thld) || (!over && value<thld) ) {
                                count++;
                                x+=value;
                                x2+=value*value;
                            }
                        }
                    }
                }
                if (count==0) return thld;
                double mean = x / count;
                double sigma = Math.sqrt(x2 / count - mean * mean);
                if (debug) ij.IJ.log("Mean over Thld: value: "+(mean+sigmaCoeff.getDoubleValue(0) * sigma)+ " mean:"+mean+ " sigma:"+sigma);
                return mean + sigmaCoeff.getDoubleValue(0) * sigma;
                
            } else return thld;
        } 
    }
    
    @Override
    public Parameter[] getParameters() {
        return parameters;
    }

    @Override
    public String getHelp() {
        return "This thresholder runs a thresholding method and returns a value computed using the first threshold";
    }
}
