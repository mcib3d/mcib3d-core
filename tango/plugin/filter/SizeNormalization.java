package tango.plugin.filter;

import ij.ImagePlus;
import ij.gui.Plot;
import java.util.Arrays;
import mcib3d.image3d.ImageFloat;
import mcib3d.image3d.ImageHandler;
import mcib3d.image3d.ImageInt;
import tango.dataStructure.InputImages;
import tango.parameter.*;
import tango.plugin.filter.PreFilter;
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
public class SizeNormalization implements PreFilter {
    boolean debug;
    int nbCPUs=1;
    SliderDoubleParameter saturation = new SliderDoubleParameter("Saturation ", "saturation", 0, 0.01, 0.001, 5);
    SliderDoubleParameter strength = new SliderDoubleParameter("Strength", "strength", 0, 1, 0.5, 3);
    IntParameter nBins_P = new IntParameter("Number of Classes:", "nBins", 256);
    Parameter[] parameters= new Parameter[] {saturation, nBins_P, strength};
    double[] inputBins, outputBins, outputCoeffs;
    int nBins;
    
    public SizeNormalization() {
        saturation.setHelp("proportion of bright pixels that will define the maximium value of the histogram.", true);
        
    }
   
    @Override
    public Parameter[] getParameters() {
        return parameters;
    }

    @Override
    public void setVerbose(boolean debug) {
        this.debug=debug;
    }

    @Override
    public ImageHandler runPreFilter(int currentStructureIdx, ImageHandler in, InputImages images) {
        ImageInt mask = images.getMask();
        nBins=this.nBins_P.getIntValue(255);
        double min = in.getMin(null);
        double satu = this.saturation.getValue();
        double max = (satu<=0 || satu>=1) ? in.getMax(mask) : in.getPercentile(satu, mask);
        int[] histo = in.getHistogram(mask, nBins, min, max);

        inputBins = new double[histo.length];
        double binSize= (max-min)/(nBins+0.0d);
        inputBins[0]=min;
        for (int i = 1; i<histo.length;i++) inputBins[i]=inputBins[i-1]+binSize;
        outputBins=new double[histo.length];
        double limit = max*strength.getValue()+min*(1-strength.getValue());
        double cst = Math.max(histo[nBins], 1);
        int idx=nBins-1;
        while (idx>0 && inputBins[idx]>limit) cst+=histo[idx--];
        //System.out.println("cst:"+cst);
        //double cst = Math.max(in.sizeXYZ*perVol.getDoubleValue(0.1)/100, 1);
        
        
        outputBins[0]=1/(histo[0]+cst);
        double sumBins=outputBins[0];
        for (int i = 1; i<histo.length; i++) {
            outputBins[i]=1/(cst+histo[i]);
            sumBins+=outputBins[i];
            outputBins[i]+=outputBins[i-1];
        }
        for (int i = 0; i<histo.length; i++) outputBins[i]/=sumBins;
        if (debug) {
            Plot p = (new Plot("HistoTransfomation", " thld ", " bins ", inputBins, outputBins));
            p.show();
        }
        //interpolation within classes
        outputCoeffs=new double[histo.length];
        for (int i = 1; i<histo.length-1; i++) outputCoeffs[i]=(float) ((outputBins[i]-outputBins[i-1])/(inputBins[i+1]-inputBins[i]));
        outputCoeffs[nBins]= (float) ((outputBins[nBins]-outputBins[nBins-1])/(in.getMax(mask) -inputBins[nBins]));
        outputCoeffs[0]=(float) (outputBins[0]/(inputBins[1]-inputBins[0]));
        System.out.println("o b:"+outputBins[nBins]+ " i b:"+inputBins[nBins]+ " o c:"+outputCoeffs[nBins]);
        ImageFloat res = new ImageFloat(in.getTitle(), in.sizeX, in.sizeY, in.sizeZ);
        for (int z = 0; z<in.sizeZ; z++) { // TODO multithread en z...
            for (int xy=0; xy<in.sizeXY; xy++) {
                float value = in.getPixel(xy, z);
                int classIdx = (value>=max) ? nBins : (int)(((value-min)/binSize));
                if (classIdx==0) res.pixels[z][xy]=(float)((value-inputBins[0])*outputCoeffs[0]);
                else res.pixels[z][xy]=(float)(outputBins[classIdx-1] + (value-inputBins[classIdx])*outputCoeffs[classIdx]);
                if (Float.isNaN(res.pixels[z][xy])) res.pixels[z][xy]=1; // FIXME problem when saturation == 0 && pixel == max -> decalage??
            }
        }
        return res;
    }

    @Override
    public void setMultithread(int nbCPUs) {
        this.nbCPUs=nbCPUs;
    }
    @Override
    public String getHelp() {
        return "";
    }
    
}
