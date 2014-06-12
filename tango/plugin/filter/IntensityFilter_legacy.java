package tango.plugin.filter;

import ij.ImagePlus;
import ij.ImageStack;
import ij.process.AutoThresholder;
import mcib3d.geom.Object3D;
import mcib3d.geom.Voxel3D;
import mcib3d.image3d.*;
import tango.dataStructure.InputImages;
import tango.parameter.*;
import tango.plugin.thresholder.AutoThreshold;

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
public class IntensityFilter_legacy implements PostFilter {
    boolean debug;
    int nbCPUs=1;
    //DoubleParameter t = new DoubleParameter("Mean intensity threshold:", "t", null, Parameter.nfDEC5);
    ThresholdParameter thresholder = new ThresholdParameter("Thresholder", "thld", "AutoThreshold", new Parameter[]{new ChoiceParameter("", "", new String[]{"OTSU"}, "OTSU")});
    BooleanParameter center = new BooleanParameter("Use value at center", "useCenter", true);
    BooleanParameter useIntensity = new BooleanParameter("Use Intensity", "useIntensity", true);
    BooleanParameter useHess = new BooleanParameter("Use Hessian", "useHessian", false);
    ConditionalParameter hessCond = new ConditionalParameter(useHess);
    SliderDoubleParameter satu = new SliderDoubleParameter("Hessian Saturation", "saturation", 0, 1, 0.1d, 3);
    DoubleParameter hessScale = new DoubleParameter("Hessian Scale", "hessScale", 1d, DoubleParameter.nfDEC1);
    BooleanParameter useConstantThld = new BooleanParameter("Use Constant Threshold", "useThld", false);
    SliderDoubleParameter thld = new SliderDoubleParameter("Threshold", "thld", 0, 1, 0.5d, 2);
    ConditionalParameter thldCond = new ConditionalParameter(useConstantThld);
    Parameter[] parameters=new Parameter[]{center, useIntensity, hessCond, thldCond};
    
    
    public IntensityFilter_legacy() {
        hessCond.setCondition(true, new Parameter[]{hessScale, satu});
        thldCond.setCondition(true, new Parameter[]{thld});
        thldCond.setCondition(false, new Parameter[]{thresholder});
    }
    
    @Override
    public ImageInt runPostFilter(int currentStructureIdx, ImageInt in, InputImages images) {
        // TODO au lieu d'ecrire sur l'image, faire directement l'histogramme
        if (!useIntensity.isSelected() && !useHess.isSelected()) useIntensity.setSelected(true);
        Object3D[] objects = in.getObjects3D();
        ImageHandler input = images.getFilteredImage(currentStructureIdx);
        ImageFloat map = null;
        if (debug || !useConstantThld.isSelected()) map = new ImageFloat("intensity filter map", input.sizeX, input.sizeY, input.sizeZ);
        
        ImageFloat hess=null;
        if (useHess.isSelected()) {
            hess=input.getHessian(hessScale.getFloatValue(1), nbCPUs)[2];
            //double min = hess.getMin(images.getMask());
            //if (min<0) hess=hess.normalize(min, 0);
            hess=hess.normalize(images.getMask(), satu.getValue());
            hess.invert(images.getMask());
            if (debug) hess.showDuplicate("hessian");
        }
        // TODO eventuellement utiliser un quantile pour la hessienne?
        // TODO essayer avec l'histogramme (256 bins)
        //ImageByte ib;
        //ib=new ImageByte(input, true);
        double[] meanValues = new double[objects.length];
        double max = input.getMax(images.getMask());
        float x, y, z;
        //int[] histo = new int[256];
        for (int i = 0; i<objects.length; i++) {
            x=(float)objects[i].getMassCenterX(input);
            y=(float)objects[i].getMassCenterY(input);
            z=(float)objects[i].getMassCenterZ(input);
            double coeff;
            if (hess!=null) {
                //coeff = objects[i].getQuantilePixValue(hess,q);
                coeff=(center.isSelected())? hess.getPixel(x, y, z, images.getMask()) : objects[i].getMeanPixValue(hess);
                
                //coeff = objects[i].getMeanPixValueAroundBarycenter(false, in, hess, 3, 2);
                //coeff = objects[i].getMeanPixValue(hess);
            } else coeff = 1;
            //ij.IJ.log(objects[i].getValue()+" mean:"+mean+ " quantile:"+quantileValue);
            //meanValues[i] = objects[i].getMeanPixValue(input) * coeff;
            if (useIntensity.isSelected()) meanValues[i]=(center.isSelected()?input.getPixel(x, y, z, images.getMask()):objects[i].getMeanPixValue(input)) / max * coeff;
            else meanValues[i]=coeff;
            //meanValues[i]=coeff;
            //meanValues[i] = objects[i].getMeanPixValueAroundBarycenter(true, in, input, 3, 2) * coeff;
            //histo[meanValues[i]]++;
            if (debug || !useConstantThld.isSelected()) map.draw(objects[i], (float)meanValues[i]);
        }
        double t = useConstantThld.isSelected()?thld.getValue():thresholder.getThreshold(map, images, nbCPUs, debug);
        //double t = AutoThreshold.run(map, images.getMask(), AutoThresholder.Method.Otsu);
        //double t = AutoThreshold.run(histo, AutoThresholder.Method.Otsu);
        if (debug) {
            ij.IJ.log("global thld: "+t);
            map.showDuplicate("intensityFilter map");
        }
        int currentIndex = 1;
        for (int i = 0; i<objects.length; i++) {
            if (meanValues[i]<t) {
                if (debug) ij.IJ.log("erase object:"+objects[i].getValue()+ " local mean:"+meanValues[i]+" global thld:"+t);
                in.draw(objects[i], 0);
                objects[i].setValue(0);
            } else {
                objects[i].setValue(currentIndex);
                if (!debug) in.draw(objects[i], currentIndex);
                currentIndex++;
                if (debug) ij.IJ.log("keep object:"+objects[i].getValue()+ " local mean:"+meanValues[i]+" global thld:"+t);
            }
        }
        return in;
    }

    @Override
    public void setVerbose(boolean debug) {
        this.debug=debug;
    }

    @Override
    public void setMultithread(int nbCPUs) {
        this.nbCPUs=nbCPUs;
    }

    @Override
    public Parameter[] getParameters() {
        return parameters;
    }

    @Override
    public String getHelp() {
        return "Erase Objects according to their mean intensity";
    }
    
}
