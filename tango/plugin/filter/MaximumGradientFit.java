package tango.plugin.filter;

import ij.gui.Plot;
import ij.measure.CurveFitter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.TreeSet;
import mcib3d.geom.Object3D;
import mcib3d.geom.Voxel3D;
import mcib3d.image3d.*;
import mcib3d.utils.ArrayUtil;
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
public class MaximumGradientFit extends SpotLocalThresholder implements PostFilter {
    
    int layerVol=10;
    int layerNb=20;
    double gScale=1;
    DoubleParameter gScale_P = new DoubleParameter("Gradient Scale (pix): ", "gScale", gScale, Parameter.nfDEC2);
    IntParameter layerVolume = new IntParameter("Min Layer volume: ", "layerVolume", layerVol);
    IntParameter nbLayer = new IntParameter("Max Layer Number: ", "layerNb", layerNb);
    
    Parameter[] parameters = new Parameter[] {gScale_P, layerVolume, nbLayer};
    ImageFloat gradient;
    
    public MaximumGradientFit() {
        super();
    }
    
    @Override
    public ImageInt runPostFilter(int currentStructureIdx, ImageInt input, InputImages images) {
        initialize(input, filtered.isSelected()?images.getFilteredImage(currentStructureIdx):images.getImage(currentStructureIdx), images.getMask());
        
        run(false);
        if (debug) gradient.show(input.getTitle()+ "::gradient");
        return segMap;
    }
    
    @Override
    public double getLocalThreshold(Object3D s) {
        ArrayList<Voxel3D> voxels = s.getVoxels();
        int size = voxels.size();
        for (Voxel3D v : voxels) v.setValue(intensityMap.getPixel(v.getRoundX(), v.getRoundY(), v.getRoundZ()));
        Collections.sort(voxels);
        int layerSize=Math.max(layerVol, (int) ((double)size/(double)layerNb+0.5));
        int nbLayers = size/layerSize;
        double[] layerGradient=new double[nbLayers];
        double[] layerIntensity=new double[nbLayers];
        int count=0;
        int currentIdx=0;
        int currentLayerIdx=0;
        while(currentIdx<size) {
            Voxel3D vox = voxels.get(currentIdx);
            layerGradient[currentLayerIdx]+=gradient.pixels[vox.getRoundZ()][vox.getRoundX()+vox.getRoundY()*sizeX];
            layerIntensity[currentLayerIdx]+=vox.getValue();
            count++;
            currentIdx++;
            if ((currentLayerIdx<(nbLayers-1) && count==layerSize) || (currentLayerIdx==(nbLayers-1) && currentIdx==size)) {
                layerGradient[currentLayerIdx]/=(double)count;
                layerIntensity[currentLayerIdx]/=(double)count;
                count=0;
                currentLayerIdx++;
            }
        }
        //plot
        if (true && debug) {
            double[] xx = new double[layerGradient.length];
            for (int i = 0; i < xx.length; i++) {
                xx[i] = i;
            }
            Plot grad = new Plot("Local Gradient: spot:"+s.getValue(), "Layer", "Mean Gradient", xx, layerGradient);
            grad.show();
            Plot intens = new Plot("Local Intensity: spot:"+s.getValue(), "Layer", "Mean Intensity", xx, layerIntensity);
            intens.show();
        }
        //get Max gradient layer
        double maxGrad=layerGradient[layerIntensity.length-1];
        double thld=layerIntensity[layerIntensity.length-1];
        for (int i = 0; i<(layerGradient.length-1); i++) {
            if (layerGradient[i]>maxGrad) {
                maxGrad=layerGradient[i];
                thld=layerIntensity[i];
            }
        }
        return thld;
    }
    
    @Override 
    protected Parameter[] getOtherParameters() {
        return parameters;
    }

    @Override
    public String getHelp() {
        return "";
    }

    @Override
    protected void postInitialize() {
        this.layerVol=layerVolume.getIntValue(layerVol);
        this.layerNb=nbLayer.getIntValue(layerNb);
        gScale = gScale_P.getDoubleValue(gScale);
        if (gScale<1) gScale=1;
        this.gradient=intensityMap.getGradient((float)gScale, this.nbCPUs);
    }
    
    
}
