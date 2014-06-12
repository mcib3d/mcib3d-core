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
public class MaximumGradientFitDistanceMap extends SpotLocalThresholder implements PostFilter {
    
    int layerVol=10;
    int layerNb=20;
    double gScale=1;
    double limit=0.75;
    double layerSize=1;
    DoubleParameter gScale_P = new DoubleParameter("Gradient Scale (pix): ", "gScale", gScale, Parameter.nfDEC2);
    SliderDoubleParameter distanceLimit = new SliderDoubleParameter("Erosion limit", "limit", 0, 1, limit, 3);
    DoubleParameter layerSize_P = new DoubleParameter("Layer thickness (pix): ", "layerSize", layerSize, Parameter.nfDEC2);
    
    Parameter[] parameters = new Parameter[] {gScale_P, layerSize_P, distanceLimit};
    ImageFloat gradient;
    ImageInt[] masks;
    public MaximumGradientFitDistanceMap() {
        super();
    }
    
    @Override
    public ImageInt runPostFilter(int currentStructureIdx, ImageInt input, InputImages images) {
        initialize(input, filtered.isSelected()?images.getFilteredImage(currentStructureIdx):images.getImage(currentStructureIdx), images.getMask());
        
        run(false);
        if (debug) gradient.show();
        return segMap;
    }
    
    @Override
    protected void postInitialize() {
        this.limit=distanceLimit.getValue();
        layerSize=layerSize_P.getDoubleValue(layerSize);
        gScale = gScale_P.getDoubleValue(gScale);
        if (gScale<1) gScale=1;
        this.gradient=intensityMap.getGradient((float)gScale, this.nbCPUs);
        masks = segMap.crop3DBinary();
    }
    
    @Override
    public double getLocalThreshold(Object3D s) {
        ImageInt m = masks[s.getValue()-1];
        ImageFloat distanceMap = m.getDistanceMapInsideMask(nbCPUs);
        ArrayList<Voxel3D> voxels = s.getVoxels();
        int offXY=m.offsetX+m.offsetY*m.sizeX;
        for (Voxel3D v : voxels) v.setValue(distanceMap.pixels[v.getRoundZ()-m.offsetZ][v.getXYCoord(m.sizeX)-offXY]);
        Collections.sort(voxels);
        double dMax=voxels.get(0).getValue();
        double inc = this.intensityMap.getScaleXY()*layerSize;
        int nbLayers = (int)(dMax/inc)+1;
        double[] layerBins = new double[nbLayers];
        layerBins[0]=dMax-inc;
        for (int i = 1; i<nbLayers; i++) layerBins[i]=layerBins[i-1]-inc;
        double[] layerGradient=new double[nbLayers];
        double[] layerIntensity=new double[nbLayers];
        int currentLayerIdx=0;
        int count=0;
        for (Voxel3D vox : voxels) {
            if (vox.getValue()<=layerBins[currentLayerIdx]) {
                if (count>0) {
                    layerIntensity[currentLayerIdx]/=count;
                    layerGradient[currentLayerIdx]/=count;
                }
                currentLayerIdx++;
                count=0;
            }
            int xy = vox.getRoundX()+vox.getRoundY()*sizeX;
            layerGradient[currentLayerIdx]+=gradient.pixels[vox.getRoundZ()][xy];
            layerIntensity[currentLayerIdx]+=intensityMap.getPixel(xy, vox.getRoundZ());
            count++;
        }
        //last layer
        if (count>0 && currentLayerIdx<nbLayers) {
            layerIntensity[currentLayerIdx]/=count;
            layerGradient[currentLayerIdx]/=count;
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
        int start = (int)(nbLayers*limit);
        if (debug) ij.IJ.log("Start index: "+start);
        for (int i = start; i<(layerGradient.length-1); i++) {
            if (layerGradient[i]>maxGrad) {
                maxGrad=layerGradient[i];
                thld=layerIntensity[i];
            }
        }
        if (debug) distanceMap.show("spot:"+s.getValue()+ "::distanceMap");
        else distanceMap.flush();
        if (debug) m.show("spot:"+s.getValue()+ "::mask");
        else m.flush();
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

    
    
    
}
