package tango.plugin.filter;

import ij.gui.Plot;
import ij.measure.CurveFitter;
import mcib3d.geom.Object3D;
import mcib3d.image3d.ImageHandler;
import mcib3d.image3d.ImageInt;
import mcib3d.utils.ArrayUtil;
import tango.dataStructure.InputImages;
import tango.parameter.BooleanParameter;
import tango.parameter.IntParameter;
import tango.parameter.Parameter;
import tango.parameter.SliderDoubleParameter;

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
public class GaussianFit extends SpotLocalThresholder {
    
    int GAUSS_MAXR;
    double GAUSS_PC;
    IntParameter maxRadius = new IntParameter("Max Radius for Gaussian Fit", "maxRadius", 10);
    SliderDoubleParameter sigma = new SliderDoubleParameter("Gaussian Fit Sigma:", "sigma", 1, 3, 1.7, 3);
    BooleanParameter useScale = new BooleanParameter("Use Image Scale", "useImageScale", false);
    
    Parameter[] parameters = new Parameter[] {sigma, maxRadius, useScale}; //
    
    
    public GaussianFit() {
        super();
    }
    
    @Override
    public ImageInt runPostFilter(int currentStructureIdx, ImageInt input, InputImages images) {
        ImageHandler intensity = filtered.isSelected()?images.getFilteredImage(currentStructureIdx):images.getImage(currentStructureIdx);
        double scaleZ = input.getScaleZ();
        double scaleXY = input.getScaleXY();
        String unit = input.getUnit();
        if (!useScale.isSelected()) intensity.setScale(scaleXY, scaleXY, unit);
        initialize(input, intensity, images.getMask());
        run(false);
        if (!useScale.isSelected()) intensity.setScale(scaleXY, scaleZ, unit);
        return segMap;
    }
    
    @Override
    public void postInitialize() {
        this.GAUSS_MAXR=maxRadius.getIntValue(10);
        this.GAUSS_PC=sigma.getValue();
    }
     
    @Override
    public double getLocalThreshold(Object3D s) {
        if (debug) System.out.println("getting local thld : spot:"+s.getValue()+ " sigma:"+GAUSS_PC);
        if (s.getVoxels().isEmpty()) return 0;
        double[] gaussFit;
        double[] params;
        Vox3D seed = getMax(s);
        gaussFit = intensityMap.radialDistribution(seed.xy%segMap.sizeX, seed.xy/segMap.sizeX, seed.z, GAUSS_MAXR, segMap);
        //correction
        boolean nan=false;
        for (int i = 0; i<GAUSS_MAXR; i++) {
            if (!nan && Double.isNaN(gaussFit[GAUSS_MAXR-i])) nan=true;
            if (nan) {
                gaussFit[GAUSS_MAXR-i]=Double.NaN;
                gaussFit[GAUSS_MAXR+i]=Double.NaN;
            } else if (gaussFit[GAUSS_MAXR-i]<gaussFit[GAUSS_MAXR-i-1]) {
                nan=true;
                gaussFit[GAUSS_MAXR-i-1]=Double.NaN;
                gaussFit[GAUSS_MAXR+i+1]=Double.NaN;
            }
        }
        if (nan) {
            gaussFit[gaussFit.length-1]=Double.NaN;
            gaussFit[0]=Double.NaN;
        }
        
        params = ArrayUtil.fitGaussian(gaussFit, 3, GAUSS_MAXR);
        // plot
        if (false &&  debug) {
            double[] xx = new double[gaussFit.length];
            for (int i = 0; i < xx.length; i++) {
                xx[i] = i;
            }
            Plot plot = new Plot("Rad:"+s.getValue(), "X", "Y", xx, gaussFit);
            plot.show();
        }
        double thld = CurveFitter.f(CurveFitter.GAUSSIAN, params, GAUSS_PC * params[3]);
        if (debug) ij.IJ.log("local thld : spot:"+s.getValue()+ " thld:"+thld);
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
