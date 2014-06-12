package tango.plugin.filter;

import ij.ImagePlus;
import ij.ImageStack;
import ij.process.AutoThresholder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import mcib3d.geom.Object3D;
import mcib3d.geom.Object3DVoxels;
import mcib3d.geom.Voxel3D;
import mcib3d.image3d.*;
import mcib3d.image3d.processing.BinaryMorpho;
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
public class EraseSpots implements PostFilter {
    boolean debug;
    int nbCPUs=1;
    BooleanParameter useFiltered = new BooleanParameter("Use filtered Image", "useFiltered", true);
    PreFilterSequenceParameter preFilters = new PreFilterSequenceParameter("Pre-Filters:", "filters");
    static String[] methods = new String[]{"SNR", "Absolute Intensity"};
    static String[] methodsBackground = new String[]{"whole nucleus", "nucleus minus spots"}; //, "nucleus minus dialted spots"
    static String[] methodsSignal = new String[]{"mean intensity", "max intensity", "quantile"};
    ChoiceParameter criterionChoice = new ChoiceParameter("Criterion:", "criterionChoice", methods, methods[1]);
    ConditionalParameter criterion = new ConditionalParameter(criterionChoice);
    Parameter[] defaultParameters = new Parameter[]{criterion};
    MultiParameter criteria;
    
    public EraseSpots() {
        DoubleParameter minSNR = new DoubleParameter("Minimal SNR:", "minSNR", 2d, Parameter.nfDEC5);
        ChoiceParameter bckChoice = new ChoiceParameter("Background estimation:", "backgroundChoice", methodsBackground, methodsBackground[0]);
        ConditionalParameter dilCond = new ConditionalParameter(bckChoice);
        //dilCond.setCondition(methodsBackground[2], new Parameter[]{new IntParameter("Radius (pixels)", "radius", 1)});
        ChoiceParameter signalChoice = new ChoiceParameter("Intensity estimation:", "signalChoice", methodsSignal, methodsSignal[0]);
        ConditionalParameter signalCond = new ConditionalParameter(signalChoice);
        signalCond.setCondition(methodsSignal[2], new Parameter[]{new SliderDoubleParameter("Quantile", "quantile", 0, 1, 0.9d, 4)});
        criterion.setCondition(methods[0], new Parameter[]{minSNR, signalCond, dilCond});
        criterion.setCondition(methods[1], new Parameter[]{signalCond, new ThresholdParameter("Thresholder", "thld", "Percentage Of Bright Pixels" )}); //"AutoThreshold", new Parameter[]{new ChoiceParameter("", "", new String[]{"OTSU"}, "OTSU")})}
        criteria = new MultiParameter("Criteria", "criteria", defaultParameters, 1, 100, 1);
    }
    
    @Override
    public ImageInt runPostFilter(int currentStructureIdx, ImageInt in, InputImages images) {
        ImageHandler input = useFiltered.isSelected()?images.getFilteredImage(currentStructureIdx):images.getImage(currentStructureIdx);
        ImageHandler intensityMap = preFilters.runPreFilterSequence(currentStructureIdx, input, images, nbCPUs, debug);
        if (debug) intensityMap.showDuplicate("erase spot input image");
        ArrayList<Parameter[]> alCrit = criteria.getParametersArrayList();
        Object3DVoxels[] objectsArray = in.getObjects3D();
        ArrayList<Object3DVoxels> objects = new ArrayList<Object3DVoxels>(Arrays.asList(objectsArray));
        for (Parameter[] p : alCrit) {
            ConditionalParameter crit =  (ConditionalParameter)p[0];
            int idx = ((ChoiceParameter)crit.getActionnableParameter()).getSelectedIndex();
            if (idx==0) { //SNR
                DoubleParameter snr = (DoubleParameter)crit.getParameters()[0];
                ConditionalParameter signalCond = (ConditionalParameter)crit.getParameters()[1];
                int sig = ((ChoiceParameter)signalCond.getActionnableParameter()).getSelectedIndex();
                double quant = -1;
                if (sig==1) quant=1;
                else if (sig==2) {
                    quant = ((SliderDoubleParameter)signalCond.getParameters()[0]).getValue();
                }
                ConditionalParameter dilCond = (ConditionalParameter)crit.getParameters()[2];
                int bck = ((ChoiceParameter)dilCond.getActionnableParameter()).getSelectedIndex();
                int dilate = 0;
                if (bck==0) dilate=-1;
                else if (bck==2) {
                    dilate = ((IntParameter)dilCond.getParameters()[0]).getIntValue(1);
                }
                eraseObjectsSNR(objects, in, dilate, images.getMask(), intensityMap, quant, snr.getDoubleValue(2));
            } else if (idx==1) { //intensity
                ConditionalParameter signalCond = (ConditionalParameter)crit.getParameters()[0];
                int sig = ((ChoiceParameter)signalCond.getActionnableParameter()).getSelectedIndex();
                double quant = -1;
                if (sig==1) quant=1;
                else if (sig==2) quant = ((SliderDoubleParameter)signalCond.getParameters()[0]).getValue();
                
                ThresholdParameter thld = (ThresholdParameter)crit.getParameters()[1];
                double thldValue = thld.getThreshold(intensityMap, images, nbCPUs, debug);
                eraseObjectsIntensity(objects, in, intensityMap, quant, thldValue);
            }
        }
        return in;
    }
    
    
    public void eraseObjectsSNR(ArrayList<Object3DVoxels> objects, ImageInt in, int dilate, ImageInt mask, ImageHandler intensity, double quantile, double thld) {
        if (objects.isEmpty()) return;
        // get noise
        Object3DVoxels bcg;
        if (dilate>0) {
            ImageByte dil = BinaryMorpho.binaryDilate(in, dilate, Math.max((int)(dilate * in.getScaleXY()/in.getScaleZ()+0.5), 1), nbCPUs);
            bcg  = dil.getObject3DBackground(mask);
            if (debug) dil.show("Dilated Image - radius:"+dilate);
            if (bcg.getVolumePixels()==0) {
                ij.IJ.log("eraseSpots error: no background after dilate");
                bcg = in.getObject3DBackground(mask);
            }
        } else if (dilate==0) bcg = in.getObject3DBackground(mask);
        else bcg=mask.getObjects3D()[0];
        if (bcg.getVolumePixels()==0) {
            ij.IJ.log("eraseSpots error: no background");
            bcg=mask.getObjects3D()[0];
        }
        double sigma = bcg.getStDevPixValue(intensity);
        double mean = bcg.getMeanPixValue(intensity);
        Iterator<Object3DVoxels> it = objects.iterator();
        while(it.hasNext()) {
            Object3DVoxels o = it.next();
            double I;
            if (quantile<0) I= o.getMeanPixValue(intensity);
            else I=o.getQuantilePixValue(intensity, quantile);
            double snrValue = (I-mean) / sigma;
            if (debug) ij.IJ.log("EraseSpots::SNR::spot:"+o.getValue()+ " snr:"+snrValue+ " thld:"+thld+ (snrValue<thld?"erased":""));
            if (snrValue<thld) {
                o.draw(in, 0);
                it.remove();
            }
        }
    }
    
    public void eraseObjectsIntensity(ArrayList<Object3DVoxels> objects, ImageInt in, ImageHandler intensity, double quantile, double thld) {
        if (objects.isEmpty()) return;
        Iterator<Object3DVoxels> it = objects.iterator();
        while(it.hasNext()) {
            Object3DVoxels o = it.next();
            double I;
            if (quantile<0) I= o.getMeanPixValue(intensity);
            else I=o.getQuantilePixValue(intensity, quantile);
            if (debug) ij.IJ.log("EraseSpots::Intensity::spot:"+o.getValue()+ " intensity:"+I+ " thld:"+thld+ (I<thld?"erased":""));
            if (I<thld) {
                o.draw(in, 0);
                it.remove();
            }
        }
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
        return new Parameter[]{useFiltered, preFilters, criteria};
    }

    @Override
    public String getHelp() {
        return "Erase Objects according to their signal-to-noise ratio or their mean intensity.";
    }
    
}
