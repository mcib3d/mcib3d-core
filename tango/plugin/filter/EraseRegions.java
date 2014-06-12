package tango.plugin.filter;

import ij.ImagePlus;
import ij.ImageStack;
import ij.process.AutoThresholder;
import mcib3d.geom.Object3D;
import mcib3d.geom.Object3DVoxels;
import mcib3d.geom.Voxel3D;
import mcib3d.image3d.*;
import tango.dataStructure.InputImages;
import tango.parameter.*;
import tango.plugin.TangoPlugin;
import tango.plugin.thresholder.AutoThreshold;
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
public class EraseRegions implements PostFilter {
    boolean debug;
    int nbCPUs=1;
    ThresholdParameter thresholder = new ThresholdParameter("Thresholder", "thld", "AutoThreshold", new Parameter[]{new ChoiceParameter("", "", new String[]{"OTSU"}, "OTSU")});
    BooleanParameter useFiltered = new BooleanParameter("Use filtered Image", "useFiltered", true);
    PreFilterSequenceParameter preFilters = new PreFilterSequenceParameter("Pre-Filters:", "filters");
    static String[] methods = new String[]{"Mean", "Quantile"};
    ChoiceParameter choice = new ChoiceParameter("Statistics:", "eraseMethod", methods, methods[0]); 
    
    SliderDoubleParameter quantile = new SliderDoubleParameter("Quantile:", "quantile", 0, 1, 0.5d, 2);
    BooleanParameter drawRegions = new BooleanParameter("Draw regions intensity on Map", "draw", false);
    ConditionalParameter cond = new ConditionalParameter(drawRegions);
    ConditionalParameter condStat = new ConditionalParameter(choice);
    BooleanParameter uniformBackground = new BooleanParameter("Uniform background?", "background", false);
    
    BooleanParameter doMultiply = new BooleanParameter("Multiply by other feature", "multiply", false);
    PreFilterSequenceParameter preFilters2 = new PreFilterSequenceParameter("Pre-Filters:", "filters", 1, "Image Features", null);
    ConditionalParameter condMultiply = new ConditionalParameter(doMultiply);
    
    Parameter[] parameters=new Parameter[]{useFiltered, preFilters, thresholder, condStat, cond, uniformBackground}; //
    
    
    public EraseRegions() {
        cond.setCondition(true, new Parameter[]{condMultiply});
        condStat.setCondition(methods[1], new Parameter[]{quantile});
        condMultiply.setCondition(true, new Parameter[]{preFilters2});
    }
    
    @Override
    public ImageInt runPostFilter(int currentStructureIdx, ImageInt in, InputImages images) {
        // TODO au lieu d'ecrire sur l'image, faire directement l'histogramme
        ImageHandler input = useFiltered.isSelected()?images.getFilteredImage(currentStructureIdx):images.getImage(currentStructureIdx);
        ImageHandler intensityMap = preFilters.runPreFilterSequence(currentStructureIdx, input, images, nbCPUs, debug);
        ImageHandler intensityMap2 = null;
        if (doMultiply.isSelected()) {
            intensityMap2 = preFilters2.runPreFilterSequence(currentStructureIdx, input, images, nbCPUs, debug);
            if (debug) intensityMap2.showDuplicate("other feature");
        }
        if (debug) ij.IJ.log("erase region method:"+choice.getSelectedIndex());
        //double t = AutoThreshold.run(map, images.getMask(), AutoThresholder.Method.Otsu);
        //double t = AutoThreshold.run(histo, AutoThresholder.Method.Otsu);
        eraseRegions(in, intensityMap, intensityMap2, images, uniformBackground.isSelected(), !drawRegions.isSelected());
        return in;
    }
    
    public void setThresholder(TangoPlugin t) {
        this.thresholder.setThresholder(t);
    }
    
    public void setQuantileValue(double quantile) {
        this.quantile.setValue(quantile);
    }
    
    protected void eraseRegions(ImageInt input, ImageHandler intensityMap, ImageHandler intensityMap2, InputImages images, boolean uniformBackground, boolean useOriginalMap) {
        Object3D[] objects   = input.getObjects3D();
        double[] values = getObjectValues(objects, intensityMap, intensityMap2);
        ImageHandler map;
        if (intensityMap2!=null) useOriginalMap=false;
        if (useOriginalMap || !uniformBackground) map = intensityMap.duplicate();
        else map = new ImageFloat("intensity filter map", input.sizeX, input.sizeY, input.sizeZ);
        if (uniformBackground) {
            Object3DVoxels bck = input.getObject3DBackground(images.getMask());
            double bckValue;
            if (choice.getSelectedIndex()==0) {
                bckValue = bck.getMeanPixValue(intensityMap);
                if (intensityMap2!=null) bckValue *= bck.getMeanPixValue(intensityMap2);
            } else {
                bckValue = bck.getQuantilePixValue(intensityMap, quantile.getValue());
                if (intensityMap2!=null) bckValue *= bck.getQuantilePixValue(intensityMap2, quantile.getValue());
            }
            map.draw(bck, (float)bckValue);
        }
        if (!useOriginalMap) {
            for (int i = 0; i<objects.length; i++) {
                map.draw(objects[i], (float)values[i]);
            }
        }
        double threshold = thresholder.getThreshold(map, images, nbCPUs, debug);
        eraseRegions(input, threshold, values, objects);
        if (debug) {
            ij.IJ.log("global thld: "+threshold);
            map.showDuplicate("intensityFilter map");
            intensityMap.showDuplicate("input intensities map");
        }
    }
    
    protected void eraseRegions(ImageInt input, double threshold, double[] values, Object3D[] objects) {
        int currentIndex = 1;
        for (int i = 0; i<objects.length; i++) {
            if (values[i]<threshold) {
                if (debug) ij.IJ.log("erase object:"+objects[i].getValue()+ " local mean:"+values[i]+" global thld:"+threshold);
                input.draw(objects[i], 0);
                objects[i].setValue(0);
            } else {
                if (debug) ij.IJ.log("keep object:"+objects[i].getValue()+ " local mean:"+values[i]+" global thld:"+threshold);
                objects[i].setValue(currentIndex);
                if (!debug) input.draw(objects[i], currentIndex);
                currentIndex++;
            }
        }
    }
    
    protected double[] getObjectValues(Object3D[] objects, ImageHandler intensityMap, ImageHandler intensityMap2) {
        double q = quantile.getValue();
        double[] res = new double[objects.length];
        int c = choice.getSelectedIndex();
        
        for (int i = 0; i<objects.length; i++) {
            if (c==0) {
                res[i]=objects[i].getQuantilePixValue(intensityMap, q);
                if (intensityMap2!=null) res[i]*=objects[i].getQuantilePixValue(intensityMap2, q);
            }
            else if (c==1) {
                res[i]=objects[i]. getMeanPixValue(intensityMap);
                if (intensityMap2!=null) res[i]*=objects[i].getMeanPixValue(intensityMap2);
            }
        }
        return res;
    }
    
    public void eraseRegionsQuantile(double quantile, ImageInt input, ImageHandler intensityMap, ImageHandler intensityMap2, InputImages images, boolean uniformBackground, boolean useOriginalMap) {
        this.setQuantileValue(quantile);
        this.choice.getChoice().setSelectedIndex(0);
        eraseRegions(input, intensityMap, intensityMap2, images, uniformBackground, useOriginalMap);
    }
    
    public void eraseRegionsMean(ImageInt input, ImageHandler intensityMap, ImageHandler intensityMap2, InputImages images, boolean uniformBackground, boolean useOriginalMap) {
        this.choice.getChoice().setSelectedIndex(0);
        eraseRegions(input, intensityMap, intensityMap2, images, uniformBackground, useOriginalMap);
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
        return "Erase Objects according to their mean intensity. Implemented from a procedure designed by Philippe Andrey";
    }
    
}
