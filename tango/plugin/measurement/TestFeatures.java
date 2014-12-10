package tango.plugin.measurement;

import ij.IJ;
import ij.ImagePlus;
import ij.measure.ResultsTable;
import ij.plugin.filter.PlugInFilter;
import ij.process.ImageProcessor;
import java.util.ArrayList;
import java.util.Iterator;
import mcib3d.geom.Object3D;
import mcib3d.geom.Object3DVoxels;
import mcib3d.geom.Voxel3D;
import mcib3d.image3d.ImageByte;
import mcib3d.image3d.ImageFloat;
import mcib3d.image3d.ImageHandler;
import mcib3d.image3d.ImageInt;
import mcib3d.image3d.distanceMap3d.EDT;
import mcib3d.image3d.processing.FastFilters3D;
import mcib_plugins.analysis.simpleMeasure;
import tango.dataStructure.InputCellImages;
import tango.dataStructure.InputImages;
import tango.dataStructure.ObjectQuantifications;
import tango.dataStructure.SegmentedCellImages;
import tango.gui.Core;
import tango.parameter.*;
import tango.plugin.filter.LaplacianOfGaussian3D;
import tango.plugin.filter.Misc_3DFilters;

/*
 * To change this template, choose Tools | Templates and open the template in
 * the editor.
 */
/**
 *
 **
 * /**
 * Copyright (C) 2008- 2012 Jean Ollion and others
 *
 *
 *
 * This file is part of TANGO
 *
 * TANGO is free software; you can redistribute it and/or modify it under the
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
 * @author Jean
 */
public class TestFeatures implements MeasurementObject {
    
    int nCPUs = 1;
    boolean verbose;
    StructureParameter structureSignal = new StructureParameter("Signal:", "structureSignal", -1, false);
    ChoiceParameter doErodeNuc =  new ChoiceParameter("Perform", "doErodeNuc", new String[]{"No Erosion", "Constant Radius"}, "No Erosion"); //, "Proportion of Volume"
    ConditionalParameter condErodeNuc = new ConditionalParameter("Erode nucleus", doErodeNuc);
    double defRadErodeNuc=4;
    DoubleParameter radErodeNuc = new DoubleParameter("Radius (pix)", "radErodeNuc", defRadErodeNuc, DoubleParameter.nfDEC1);
    SliderDoubleParameter perErodeNuc = new SliderDoubleParameter("Proportion of nuclear volume:", "perErodeNuc", 0d, 1d, 0.3d, 2);
    
    TextParameter prefix = new TextParameter("Global Prefix", "prefix", "");
    
    final static int RAW=0;
    BooleanParameter doRaw = new BooleanParameter("Perform", "doRaw", true);
    ConditionalParameter condRaw = new ConditionalParameter("Raw Image", doRaw);
    TextParameter rawPrefix = new TextParameter("Prefix", "rawPrefix", "raw");
    
    final static int CUSTOM=1;
    BooleanParameter doCustom = new BooleanParameter("Perform", "doCustom", false);
    ConditionalParameter condCustom = new ConditionalParameter("Custom Filter",doCustom);
    PreFilterSequenceParameter prefSeq = new PreFilterSequenceParameter("Pre-Filters", "preFilters");
    TextParameter customPrefix = new TextParameter("Prefix", "customPrefix", "");
    
    final static int GAUSS=2;
    BooleanParameter doGauss = new BooleanParameter("Perform", "doGauss", true);
    ConditionalParameter condGauss = new ConditionalParameter("Gaussian Blur", doGauss);
    IntParameter gaussMinRad = new IntParameter("Min. Radius (pix)", "gaussMinRad", 1);
    IntParameter gaussMaxRad = new IntParameter("Max. Radius (pix)", "gaussMaxRad", 10);
    TextParameter gaussPrefix = new TextParameter("Prefix", "gaussPrefix", "gauss");
    
    final static int DOG=3;
    BooleanParameter doDOG = new BooleanParameter("Perform", "doGauss", true);
    ConditionalParameter condDOG = new ConditionalParameter("Difference of Gaussians",doDOG);
    IntParameter DOGMinRadS = new IntParameter("Min. Radius for Smaller Gaussian (pix)", "DOGminRadS", 0);
    IntParameter DOGMaxRadS = new IntParameter("Max. Radius for Smaller Gaussian (pix)", "DOGmaxRadS", 4);
    IntParameter DOGMinRadL = new IntParameter("Min. Radius for Larger Gaussian (pix)", "DOGminRadL", 1);
    IntParameter DOGMaxRadL = new IntParameter("Max. Radius for Larger Gaussian (pix)", "DOGmaxRadL", 10);
    TextParameter DOGPrefix = new TextParameter("Prefix", "DOGprefix", "DOG");
    
    final static int LOG=4;
    BooleanParameter doLOG = new BooleanParameter("Perform", "doLOG", true);
    ConditionalParameter condLOG = new ConditionalParameter("Laplacian of Gaussian", doLOG);
    IntParameter LOGMinRad = new IntParameter("Min. Radius (pix)", "LOGMinRad", 1);
    IntParameter LOGMaxRad = new IntParameter("Max. Radius (pix)", "LOGMaxRad", 4);
    TextParameter LOGPrefix = new TextParameter("Prefix", "LOGPrefix", "LOG");
    
    final static int OPEN=5;
    BooleanParameter doOpen = new BooleanParameter("Perform", "doOpen", true);
    ConditionalParameter condOpen = new ConditionalParameter("Grayscale Opening",doOpen);
    IntParameter openMinRad = new IntParameter("Min. Radius (pix)", "OpenMinRad", 1);
    IntParameter openMaxRad = new IntParameter("Max. Radius (pix)", "OpenMaxRad", 8);
    TextParameter openPrefix = new TextParameter("Prefix", "openPrefix", "open");
    PreFilterParameter openFilter = new PreFilterParameter("De-noising", "openDenoising", "Fast Filters 3D");
    BooleanParameter doOpenDenoising = new BooleanParameter("Perform", "doOpenDenoising", true);
    ConditionalParameter condOpenDenoising = new ConditionalParameter("De-noising", doOpenDenoising);
    
    final static int TH=6;
    BooleanParameter doTH = new BooleanParameter("Perform", "doTH", true);
    ConditionalParameter condTH = new ConditionalParameter("Top-Hat",doTH);
    IntParameter THMinRad = new IntParameter("Min. Radius (pix)", "THMinRad", 1);
    IntParameter THMaxRad = new IntParameter("Max. Radius (pix)", "THMaxRad", 8);
    TextParameter THprefix = new TextParameter("Prefix", "THprefix", "topHat");
    PreFilterParameter THFilter = new PreFilterParameter("De-noising", "THdenoising", "Fast Filters 3D");
    BooleanParameter doTHDenoising = new BooleanParameter("Perform", "doTHDenoising", true);
    ConditionalParameter condTHDenoising = new ConditionalParameter("De-noising", doTHDenoising);
    
    final static int GRAD=7;
    BooleanParameter doGrad = new BooleanParameter("Performe", "doGrad", true);
    ConditionalParameter condGrad = new ConditionalParameter("Gradient Magnitude", doGrad);
    IntParameter gradMinRad = new IntParameter("Min. Radius (pix)", "gradMinRad", 1);
    IntParameter gradMaxRad = new IntParameter("Max. Radius (pix)", "gradMaxRad", 4);
    TextParameter gradPrefix = new TextParameter("Prefix", "gradPrefix", "grad");
    PreFilterParameter gradFilter = new PreFilterParameter("De-noising", "gradDenoising", "Fast Filters 3D");
    BooleanParameter doGradDenoising = new BooleanParameter("Perform", "doGradDenoising", true);
    ConditionalParameter condGradDenoising = new ConditionalParameter("De-noising" , doGradDenoising);
    
    final static int HM=8;
    BooleanParameter doHM = new BooleanParameter("Perform", "doHM", true);
    ConditionalParameter condHM = new ConditionalParameter("Max eigen value of Hessian transform", doHM);
    IntParameter HMMinRad = new IntParameter("Min. Radius (pix)", "HMMinRad", 1);
    IntParameter HMMaxRad = new IntParameter("Max. Radius (pix)", "HMMaxRad", 6);
    TextParameter HMprefix = new TextParameter("Prefix", "HMprefix", "hessian_max");
    
    final static int CURV=9;
    BooleanParameter doCurv = new BooleanParameter("Perform", "doCurv", true);
    ConditionalParameter condCurv = new ConditionalParameter("Curvature", doCurv);
    IntParameter CurvMinRad = new IntParameter("Min. Radius (pix)", "CurvMinRad", 1);
    IntParameter CurvMaxRad = new IntParameter("Max. Radius (pix)", "CurvMaxRad", 6);
    TextParameter Curvprefix = new TextParameter("Prefix", "Curvprefix", "curvature");
    
    final static int STRUCTURE=10;
    BooleanParameter doStruct = new BooleanParameter("Perform", "doStruct", true);
    ConditionalParameter condStruct = new ConditionalParameter("Max eigen value of Inertia transform", doStruct);
    IntParameter structMinRad = new IntParameter("Min. Radius (pix)", "structMinRad", 1);
    IntParameter structMaxRad = new IntParameter("Max. Radius (pix)", "structMaxRad", 4);
    TextParameter structPrefix = new TextParameter("Prefix", "structPrefix", "inertia");
    DoubleParameter structSmooth = new DoubleParameter("Smoothing", "structSmooth", 1d, DoubleParameter.nfDEC1);
    
    GroupKeyParameter[][] allKeysMatrix = new GroupKeyParameter[11][];
    Parameter[] parameters = new Parameter[]{structureSignal, prefix, condErodeNuc, condRaw, condCustom, condGauss, condDOG, condLOG, condOpen, condTH, condGrad, condHM, condCurv, condStruct}; //,condErodeNuc

    
    // Histogram Moments [0-3]
    KeyParameterObjectNumber hist_mean = new KeyParameterObjectNumber("Histogram Mean Intensity", "hist_mean", "hist_mean_intensity", true);
    KeyParameterObjectNumber hist_sd = new KeyParameterObjectNumber("Histogram Standart Deviation", "histSD", "hist_sd", true);
    KeyParameterObjectNumber hist_sdmu = new KeyParameterObjectNumber("Histogram Standart Deviation/Mean", "histSdMu", "hist_sd_mean", true);
    KeyParameterObjectNumber hist_skewness = new KeyParameterObjectNumber("Histogram Skewness (Order 3 moment)", "hist_skewness", "hist_skewness", true);
    KeyParameterObjectNumber hist_kurtosis = new KeyParameterObjectNumber("Histogram Kurtosis (Order 4 moment)", "hist_kurtosis", "hist_kurtosis", true);
    int idxHistMoments=5;
    // Quantiles => 1/1000 1/100 1/20 1/2
    KeyParameterObjectNumber hist_quantile1000 = new KeyParameterObjectNumber("Histogram Quantile 1/1000", "hist_quantile_0001", "hist_quantile_0001", true);
    KeyParameterObjectNumber hist_quantile1 = new KeyParameterObjectNumber("Histogram Quantile 1%", "hist_quantile_001", "hist_quantile_001", true);
    KeyParameterObjectNumber hist_quantile10 = new KeyParameterObjectNumber("Histogram Quantile 10%", "hist_quantile_01", "hist_quantile_01", true);
    KeyParameterObjectNumber hist_quantile50 = new KeyParameterObjectNumber("Histogram Quantile 50%", "hist_quantile_05", "hist_quantile_05", true);
    int idxQuantiles = 4+idxHistMoments;
    // Spatial Moments
    KeyParameterObjectNumber spa_sd = new KeyParameterObjectNumber("Spatial Moment 2: Standart Deviation", "spa_sd", "spa_sd", true);
    KeyParameterObjectNumber spa_skewness = new KeyParameterObjectNumber("Spatial Moment 3: Skewness", "spa_skewness", "spa_skewness", true);
    KeyParameterObjectNumber spa_kurtosis = new KeyParameterObjectNumber("Spatial Moment 4: Kurtosis", "spa_kurtosis", "spa_kurtosis", true);
    int idxSpaMom = 3+ idxQuantiles;
    // RAC
    KeyParameterObjectNumber rac1 = new KeyParameterObjectNumber("Radial autocorrelation radius 1", "rac1", "rac1", true);
    KeyParameterObjectNumber rac2 = new KeyParameterObjectNumber("Radial autocorrelation radius 2", "rac2", "rac2", true);
    KeyParameterObjectNumber rac3 = new KeyParameterObjectNumber("Radial autocorrelation radius 3", "rac3", "rac3", true);
    KeyParameterObjectNumber rac4 = new KeyParameterObjectNumber("Radial autocorrelation radius 4", "rac4", "rac4", true);
    KeyParameterObjectNumber rac5 = new KeyParameterObjectNumber("Radial autocorrelation radius 5", "rac5", "rac5", true);
    KeyParameterObjectNumber rac6 = new KeyParameterObjectNumber("Radial autocorrelation radius 6", "rac6", "rac6", true);
    KeyParameterObjectNumber rac7 = new KeyParameterObjectNumber("Radial autocorrelation radius 7", "rac7", "rac7", true);
    KeyParameterObjectNumber rac8 = new KeyParameterObjectNumber("Radial autocorrelation radius 8", "rac8", "rac8", true);
    int idxRAC = 8+ idxSpaMom;
    
    // Texture parameters
    KeyParameterObjectNumber tex_corr1 = new KeyParameterObjectNumber("Texture: Correlation radius 1", "tex_corr1", "tex_corr1", true);
    KeyParameterObjectNumber tex_asm1 = new KeyParameterObjectNumber("Texture: ASM radius 1", "tex_asm1", "tex_asm1", true);
    KeyParameterObjectNumber tex_contrast1 = new KeyParameterObjectNumber("Texture: Contrast radius 1", "tex_contrast1", "tex_contrast1", true);
    KeyParameterObjectNumber tex_entropy1 = new KeyParameterObjectNumber("Texture: Entropy radius 1", "tex_entropy1", "tex_entropy1", true);
    KeyParameterObjectNumber tex_idm1 = new KeyParameterObjectNumber("Texture: IDM radius 1", "tex_idm1", "tex_idm1", true);
    KeyParameterObjectNumber tex_corr2 = new KeyParameterObjectNumber("Texture: Correlation radius 2", "tex_corr2", "tex_corr2", true);
    KeyParameterObjectNumber tex_asm2 = new KeyParameterObjectNumber("Texture: ASM radius 2", "tex_asm2", "tex_asm2", true);
    KeyParameterObjectNumber tex_contrast2 = new KeyParameterObjectNumber("Texture: Contrast radius 2", "tex_contrast2", "tex_contrast2", true);
    KeyParameterObjectNumber tex_entropy2 = new KeyParameterObjectNumber("Texture: Entropy radius 2", "tex_entropy2", "tex_entropy2", true);
    KeyParameterObjectNumber tex_idm2 = new KeyParameterObjectNumber("Texture: IDM radius 2", "tex_idm2", "tex_idm2", true);
    KeyParameterObjectNumber tex_corr3 = new KeyParameterObjectNumber("Texture: Correlation radius 3", "tex_corr3", "tex_corr3", true);
    KeyParameterObjectNumber tex_asm3 = new KeyParameterObjectNumber("Texture: ASM radius 3", "tex_asm3", "tex_asm3", true);
    KeyParameterObjectNumber tex_contrast3 = new KeyParameterObjectNumber("Texture: Contrast radius 3", "tex_contrast3", "tex_contrast3", true);
    KeyParameterObjectNumber tex_entropy3 = new KeyParameterObjectNumber("Texture: Entropy radius 3", "tex_entropy3", "tex_entropy3", true);
    KeyParameterObjectNumber tex_idm3 = new KeyParameterObjectNumber("Texture: IDM radius 3", "tex_idm3", "tex_idm3", true);
    KeyParameterObjectNumber tex_corr4 = new KeyParameterObjectNumber("Texture: Correlation radius 4", "tex_corr4", "tex_corr4", true);
    KeyParameterObjectNumber tex_asm4 = new KeyParameterObjectNumber("Texture: ASM radius 4", "tex_asm4", "tex_asm4", true);
    KeyParameterObjectNumber tex_contrast4 = new KeyParameterObjectNumber("Texture: Contrast radius 4", "tex_contrast4", "tex_contrast4", true);
    KeyParameterObjectNumber tex_entropy4 = new KeyParameterObjectNumber("Texture: Entropy radius 4", "tex_entropy4", "tex_entropy4", true);
    KeyParameterObjectNumber tex_idm4 = new KeyParameterObjectNumber("Texture: IDM radius 4", "tex_idm4", "tex_idm4", true);
    KeyParameterObjectNumber tex_corr5 = new KeyParameterObjectNumber("Texture: Correlation radius 5", "tex_corr5", "tex_corr5", true);
    KeyParameterObjectNumber tex_asm5 = new KeyParameterObjectNumber("Texture: ASM radius 5", "tex_asm5", "tex_asm5", true);
    KeyParameterObjectNumber tex_contrast5 = new KeyParameterObjectNumber("Texture: Contrast radius 5", "tex_contrast5", "tex_contrast5", true);
    KeyParameterObjectNumber tex_entropy5 = new KeyParameterObjectNumber("Texture: Entropy radius 5", "tex_entropy5", "tex_entropy5", true);
    KeyParameterObjectNumber tex_idm5 = new KeyParameterObjectNumber("Texture: IDM radius 5", "tex_idm5", "tex_idm5", true);
    KeyParameterObjectNumber tex_corr6 = new KeyParameterObjectNumber("Texture: Correlation radius 6", "tex_corr6", "tex_corr6", true);
    KeyParameterObjectNumber tex_asm6 = new KeyParameterObjectNumber("Texture: ASM radius 6", "tex_asm6", "tex_asm6", true);
    KeyParameterObjectNumber tex_contrast6 = new KeyParameterObjectNumber("Texture: Contrast radius 6", "tex_contrast6", "tex_contrast6", true);
    KeyParameterObjectNumber tex_entropy6 = new KeyParameterObjectNumber("Texture: Entropy radius 6", "tex_entropy6", "tex_entropy6", true);
    KeyParameterObjectNumber tex_idm6 = new KeyParameterObjectNumber("Texture: IDM radius 6", "tex_idm6", "tex_idm6", true);
    KeyParameterObjectNumber tex_corr7 = new KeyParameterObjectNumber("Texture: Correlation radius 7", "tex_corr7", "tex_corr7", true);
    KeyParameterObjectNumber tex_asm7 = new KeyParameterObjectNumber("Texture: ASM radius 7", "tex_asm7", "tex_asm7", true);
    KeyParameterObjectNumber tex_contrast7 = new KeyParameterObjectNumber("Texture: Contrast radius 7", "tex_contrast7", "tex_contrast7", true);
    KeyParameterObjectNumber tex_entropy7 = new KeyParameterObjectNumber("Texture: Entropy radius 7", "tex_entropy7", "tex_entropy7", true);
    KeyParameterObjectNumber tex_idm7 = new KeyParameterObjectNumber("Texture: IDM radius 7", "tex_idm7", "tex_idm7", true);
    KeyParameterObjectNumber tex_corr8 = new KeyParameterObjectNumber("Texture: Correlation radius 8", "tex_corr8", "tex_corr8", true);
    KeyParameterObjectNumber tex_asm8 = new KeyParameterObjectNumber("Texture: ASM radius 8", "tex_asm8", "tex_asm8", true);
    KeyParameterObjectNumber tex_contrast8 = new KeyParameterObjectNumber("Texture: Contrast radius 8", "tex_contrast8", "tex_contrast8", true);
    KeyParameterObjectNumber tex_entropy8 = new KeyParameterObjectNumber("Texture: Entropy radius 8", "tex_entropy8", "tex_entropy8", true);
    KeyParameterObjectNumber tex_idm8 = new KeyParameterObjectNumber("Texture: IDM radius 8", "tex_idm8", "tex_idm8", true);
    
    GroupKeyParameter defaultKeys = new GroupKeyParameter("Keys for pre-filter:1 ", "keys1", "", true, 
            new KeyParameterObjectNumber[]{
                hist_mean, hist_sd, hist_skewness, hist_kurtosis, hist_sdmu, //histogram moments
                hist_quantile1000, hist_quantile1, hist_quantile10, hist_quantile50, //quantiles
                spa_sd, spa_skewness, spa_kurtosis, // spatial moments
                rac1, rac2, rac3, rac4, rac5, rac6, rac7, rac8, // RAC
                tex_corr1, tex_asm1, tex_contrast1, tex_entropy1, tex_idm1, // tex 1
                tex_corr2, tex_asm2, tex_contrast2, tex_entropy2, tex_idm2, // tex 2
                tex_corr3, tex_asm3, tex_contrast3, tex_entropy3, tex_idm3, // tex 3
                tex_corr4, tex_asm4, tex_contrast4, tex_entropy4, tex_idm4, // tex 4
                tex_corr5, tex_asm5, tex_contrast5, tex_entropy5, tex_idm5, // tex 5
                tex_corr6, tex_asm6, tex_contrast6, tex_entropy6, tex_idm6, // tex 6
                tex_corr7, tex_asm7, tex_contrast7, tex_entropy7, tex_idm7, // tex 7
                tex_corr8, tex_asm8, tex_contrast8, tex_entropy8, tex_idm8 // tex 8
            }, 
            false);
    

    public TestFeatures() {
        condErodeNuc.setCondition("Constant Radius", new Parameter[]{radErodeNuc});
        condErodeNuc.setCondition("Proportion of Volume", new Parameter[]{perErodeNuc});
        
        condGauss.setCondition(true, new Parameter[]{gaussPrefix, gaussMinRad, gaussMaxRad});
        condDOG.setCondition(true, new Parameter[]{DOGPrefix, DOGMinRadS, DOGMaxRadS, DOGMinRadL, DOGMaxRadL});
        condTH.setCondition(true, new Parameter[]{THprefix, THMinRad, THMaxRad, condTHDenoising});
        condOpen.setCondition(true, new Parameter[]{openPrefix, openMinRad, openMaxRad, condOpenDenoising});
        condCustom.setCondition(true, new Parameter[]{customPrefix, prefSeq});
        condLOG.setCondition(true, new Parameter[]{LOGPrefix, LOGMinRad, LOGMaxRad});
        condHM.setCondition(true, new Parameter[]{HMprefix, HMMinRad, HMMaxRad});
        condCurv.setCondition(true, new Parameter[]{Curvprefix, CurvMinRad, CurvMaxRad});
        condGrad.setCondition(true, new Parameter[]{gradPrefix, gradMinRad, gradMaxRad, condGradDenoising});
        condStruct.setCondition(true, new Parameter[]{structPrefix, structMinRad, structMaxRad, structSmooth});
        condRaw.setCondition(true, new Parameter[]{rawPrefix});
        
        condTHDenoising.setCondition(true, new Parameter[]{THFilter});
        condOpenDenoising.setCondition(true, new Parameter[]{openFilter});
        condGradDenoising.setCondition(true, new Parameter[]{gradFilter});
        
        gaussPrefix.allowSpecialCharacter(false);
        DOGPrefix.allowSpecialCharacter(false);
        openPrefix.allowSpecialCharacter(false);
        THprefix.allowSpecialCharacter(false);
        customPrefix.allowSpecialCharacter(false);
        LOGPrefix.allowSpecialCharacter(false);
        HMprefix.allowSpecialCharacter(false);
        Curvprefix.allowSpecialCharacter(false);
        gradPrefix.allowSpecialCharacter(false);
        structPrefix.allowSpecialCharacter(false);
        rawPrefix.allowSpecialCharacter(false);
        rawPrefix.setCompulsary(false);
    }

    @Override
    public int getStructure() {
        return 0;
    }

    @Override
    public void getMeasure(InputCellImages raw, SegmentedCellImages seg, ObjectQuantifications quantifications) {
        ImageInt mask;
        Object3DVoxels nuc;
        
        if (doErodeNuc.getSelectedIndex()==1) { // constant radius
            float erodeRad = radErodeNuc.getFloatValue(3) * (float)raw.getMask().getScaleXY();
            mask = raw.getMask().erode(erodeRad, nCPUs);
            Object3DVoxels[]  os = mask.getObjects3D();
            if (verbose) {
                mask.showDuplicate("Eroded Mask: rad::"+erodeRad);
                IJ.log("nb objects:"+os.length);
            }
            if (os.length==1) nuc=os[0];
            else {
                if (Core.GUIMode) IJ.log("TestFeature error: nucleus erode: nb objects after erosion:"+os.length);
                return;
            }
        } //else if (doErodeNuc.getSelectedIndex()==2) {
            
        //} 
        else {
            mask = raw.getMask();
            nuc = seg.getObjects(0)[0];
        }   
        
        
        
        
        double Zfactor = mask.getScaleXY()/mask.getScaleZ();
        ImageHandler rawSignal  = structureSignal.getImage(raw, false);
        ImageHandler[] filteredImages;
        if (doRaw.isSelected()) {
            filteredImages = new ImageHandler[1];
            filteredImages[0]=rawSignal;
            performMeasures(allKeysMatrix[RAW], filteredImages, quantifications, mask, nuc);
        }
        if (this.doCustom.isSelected() && isOneKeySelected(CUSTOM, 0)) {
            filteredImages = new ImageHandler[1];
            filteredImages[0] = this.prefSeq.runPreFilterSequence(structureSignal.getIndex(), rawSignal, raw, nCPUs, verbose);
            performMeasures(allKeysMatrix[CUSTOM], filteredImages, quantifications, mask, nuc);
        }
        if (this.doGauss.isSelected()) {
            filteredImages = new ImageHandler[allKeysMatrix[GAUSS].length];
            int idx = 0;
            for (int i = this.gaussMinRad.getIntValue(1); i<=this.gaussMaxRad.getIntValue(10); i++) {
                if (isOneKeySelected(GAUSS, idx)) filteredImages[idx] = rawSignal.gaussianSmooth(i, i*Zfactor, nCPUs);
                idx++;
            }
            performMeasures(allKeysMatrix[GAUSS], filteredImages, quantifications, mask, nuc);
        }
        if (this.doDOG.isSelected()) {
            filteredImages = new ImageHandler[allKeysMatrix[DOG].length];
            int idx = 0;
            for (int s = this.DOGMinRadS.getIntValue(1); s<=this.DOGMaxRadS.getIntValue(2); s++) {
                for (int l = Math.max(s+1, this.DOGMinRadL.getIntValue(2)); l<=this.DOGMaxRadL.getIntValue(10); l++) {
                    if (isOneKeySelected(DOG, idx)) {
                        ImageFloat gaussSmall = rawSignal.gaussianSmooth(s, s*Zfactor, nCPUs);
                        ImageFloat gaussLarge = rawSignal.gaussianSmooth(l, l*Zfactor, nCPUs);
                        filteredImages[idx] = gaussSmall.substractImage(gaussLarge);
                    }
                    idx++;
                }
            }
            performMeasures(allKeysMatrix[DOG], filteredImages, quantifications, mask, nuc);
        }
        if (this.doLOG.isSelected()) {
            filteredImages = new ImageHandler[allKeysMatrix[LOG].length];
            int idx = 0;
            for (int i = this.LOGMinRad.getIntValue(1); i<=this.LOGMaxRad.getIntValue(4); i++) {
                if (isOneKeySelected(LOG, idx)) filteredImages[idx] =LaplacianOfGaussian3D.LOG(rawSignal, i, i*Zfactor);
                idx++;
            }
            performMeasures(allKeysMatrix[LOG], filteredImages, quantifications, mask, nuc);
        }
        if (this.doOpen.isSelected()) {
            filteredImages = new ImageHandler[allKeysMatrix[OPEN].length];
            ImageHandler filtered;
            if (this.doOpenDenoising.isSelected()) filtered = this.openFilter.preFilter(structureSignal.getIndex(), rawSignal, raw, nCPUs, verbose);
            else filtered = rawSignal;
            int idx = 0;
            for (int i = this.openMinRad.getIntValue(1); i<=this.openMaxRad.getIntValue(10); i++) {
                if (isOneKeySelected(OPEN, idx)) filteredImages[idx] = FastFilters3D.filterImage(filtered, FastFilters3D.OPENGRAY, (float)i, (float)i, (float)(i*Zfactor), nCPUs, false);
                idx++;
            }
            performMeasures(allKeysMatrix[OPEN], filteredImages, quantifications, mask, nuc);
        }
        if (this.doTH.isSelected()) {
            filteredImages = new ImageHandler[allKeysMatrix[TH].length];
            ImageHandler filtered;
            if (this.doTHDenoising.isSelected()) filtered = this.THFilter.preFilter(structureSignal.getIndex(), rawSignal, raw, nCPUs, verbose);
            else filtered = rawSignal;
            int idx = 0;
            for (int i = this.THMinRad.getIntValue(1); i<=this.THMaxRad.getIntValue(10); i++) {
                if (isOneKeySelected(TH, idx)) filteredImages[idx] = FastFilters3D.filterImage(filtered, FastFilters3D.TOPHAT, (float)i, (float)i, (float)(i*Zfactor), nCPUs, false);
                idx++;
            }
            performMeasures(allKeysMatrix[TH], filteredImages, quantifications, mask, nuc);
        }
        if (this.doGrad.isSelected()) {
            filteredImages = new ImageHandler[allKeysMatrix[GRAD].length];
            ImageHandler filtered;
            if (this.doGradDenoising.isSelected()) filtered = this.gradFilter.preFilter(structureSignal.getIndex(), rawSignal, raw, nCPUs, verbose);
            else filtered = rawSignal;
            int idx = 0;
            for (int i = this.gradMinRad.getIntValue(1); i<=this.gradMaxRad.getIntValue(4); i++) {
                if (isOneKeySelected(GRAD, idx)) filteredImages[idx] = filtered.getGradient(i, nCPUs);
                idx++;
            }
            performMeasures(allKeysMatrix[GRAD], filteredImages, quantifications, mask, nuc);
        }
        if (this.doHM.isSelected()) {
            filteredImages = new ImageHandler[allKeysMatrix[HM].length];
            ImageHandler filtered = rawSignal;
            int idx = 0;
            for (int i = this.HMMinRad.getIntValue(1); i<=this.HMMaxRad.getIntValue(4); i++) {
                if (isOneKeySelected(HM, idx)) {
                    filteredImages[idx] = filtered.getHessian(i, nCPUs)[0];
                    ((ImageFloat)filteredImages[idx]).opposite();
                }
                idx++;
            }
            performMeasures(allKeysMatrix[HM], filteredImages, quantifications, mask, nuc);
        }
        if (this.doCurv.isSelected()) {
            filteredImages = new ImageHandler[allKeysMatrix[CURV].length];
            ImageHandler filtered = rawSignal;
            int idx = 0;
            for (int i = this.CurvMinRad.getIntValue(1); i<=this.CurvMaxRad.getIntValue(4); i++) {
                if (isOneKeySelected(CURV, idx)) {
                    ImageFloat[] hess = filtered.getHessian(i, nCPUs);
                    ImageFloat res = ImageFloat.newBlankImageFloat("Curvature", filtered);
                    int sizeZ = hess[0].sizeZ;
                    int sizeXY = hess[0].sizeXY;
                    for (int z= 0; z<sizeZ; z++) {
                        for (int xy= 0; xy<sizeXY; xy++) {
                            res.pixels[z][xy]=(float) ( Math.pow( Math.abs( filtered.getPixel(xy, z) * hess[0].pixels[z][xy] * hess[1].pixels[z][xy] * hess[2].pixels[z][xy] ) , 0.25d ) );
                        }
                    }
                    filteredImages[idx] = res;
                }
                idx++;
            }
            performMeasures(allKeysMatrix[CURV], filteredImages, quantifications, mask, nuc);
        }
        if (this.doStruct.isSelected()) {
            filteredImages = new ImageHandler[allKeysMatrix[STRUCTURE].length];
            int idx = 0;
            for (int i = this.structMinRad.getIntValue(1); i<=this.structMaxRad.getIntValue(4); i++) {
                if (isOneKeySelected(STRUCTURE, idx)) filteredImages[idx] = rawSignal.getInertia(structSmooth.getDoubleValue(1), i, nCPUs)[0];
                idx++;
            }
            performMeasures(allKeysMatrix[STRUCTURE], filteredImages, quantifications, mask, nuc);
        }
    }
    
    private boolean isOneKeySelected(int measureIdx, int idx) {
        for (KeyParameter k : allKeysMatrix[measureIdx][idx].getKeys()) {
            if (k.isSelected()) return true;
        }
        return false;
    }
    
    private ArrayList<String> getPrefixEnd(int measureIdx) {
        ArrayList<String> res = new ArrayList<String>();
        switch(measureIdx) {
            case RAW: 
                if (doRaw.isSelected()) res.add(rawPrefix.getText()+ ( (rawPrefix.getText().length()>0) ? "_"  : "") );
                break;
            case CUSTOM: if (doCustom.isSelected()) res.add(customPrefix.getText()+ ( (customPrefix.getText().length()>0) ? "_"  : ""));
                    break;
            case GAUSS: 
                if (doGauss.isSelected()) for (int i = this.gaussMinRad.getIntValue(1); i<=this.gaussMaxRad.getIntValue(10); i++) res.add(gaussPrefix.getText()+i+"_");
                break;
            case DOG:
                if (doDOG.isSelected()) {
                    for (int s = this.DOGMinRadS.getIntValue(1); s<=this.DOGMaxRadS.getIntValue(2); s++) {
                        for (int l = Math.max(s+1, this.DOGMinRadL.getIntValue(2)); l<=this.DOGMaxRadL.getIntValue(10); l++) {
                            res.add(DOGPrefix.getText()+s+"_"+l+"_");
                        }
                    }
                }
                break;
            case LOG: 
                if (doLOG.isSelected()) for (int i = this.LOGMinRad.getIntValue(1); i<=this.LOGMaxRad.getIntValue(10); i++) res.add(LOGPrefix.getText()+i+"_");
                break;
            case OPEN: 
                if (doOpen.isSelected()) for (int i = this.openMinRad.getIntValue(1); i<=this.openMaxRad.getIntValue(10); i++) res.add(openPrefix.getText()+i+"_");
                break;
            case TH: 
                if (doTH.isSelected()) for (int i = this.THMinRad.getIntValue(1); i<=this.THMaxRad.getIntValue(10); i++) res.add(THprefix.getText()+i+"_");
                break;
            case HM:
                if (doHM.isSelected()) for (int i = this.HMMinRad.getIntValue(1); i<=this.HMMaxRad.getIntValue(4); i++) res.add(HMprefix.getText()+i+"_");
                break;
            case CURV:
                if (doCurv.isSelected()) for (int i = this.CurvMinRad.getIntValue(1); i<=this.CurvMaxRad.getIntValue(4); i++) res.add(Curvprefix.getText()+i+"_");
                break;
            case GRAD:
                if (doGrad.isSelected()) for (int i = this.gradMinRad.getIntValue(1); i<=this.gradMaxRad.getIntValue(4); i++) res.add(gradPrefix.getText()+i+"_");
                break;
            case STRUCTURE:
                if (doStruct.isSelected()) for (int i = this.structMinRad.getIntValue(1); i<=this.structMaxRad.getIntValue(4); i++) res.add(structPrefix.getText()+i+"_");
                break;
        }
        return res;
    }
    
    private void performMeasures(GroupKeyParameter[] allKeys, ImageHandler[] filteredImages, ObjectQuantifications quantifications, ImageInt mask, Object3D nuc) {    
        for (int i = 0; i<filteredImages.length; i++) {
            if (filteredImages[i]!=null) {
                // Histogram Moments & quantiles
                double[] moments = computeHistogramMoments(filteredImages[i], nuc);
                for (int m=0;m<4;m++) quantifications.setQuantificationObjectNumber((KeyParameterObjectNumber)allKeys[i].getKeys()[m], new double[]{moments[m]});
                // sd/mean
                if (moments[0]!=0) quantifications.setQuantificationObjectNumber((KeyParameterObjectNumber)allKeys[i].getKeys()[4], new double[]{moments[1]/moments[0]});

                //Quantiles
                double[] quantiles = Quantiles.getQuantiles(nuc, filteredImages[i], new double[]{0.0001, 0.001, 0.1, 0.5});
                for (int q=0;q<4;q++) {
                    quantifications.setQuantificationObjectNumber((KeyParameterObjectNumber)allKeys[i].getKeys()[idxHistMoments+q], new double[]{quantiles[q]});
                }

                // spatial moments
                double[][] Spamoments = GrayscaleSpatialMoments.computeMoments(filteredImages[i], mask);
                // variance
                double var=Spamoments[1][0]+Spamoments[1][1]+Spamoments[1][2];
                if (var<0) var = -Math.sqrt(-var);
                else var=Math.sqrt(var);
                quantifications.setQuantificationObjectNumber((KeyParameterObjectNumber)allKeys[i].getKeys()[idxQuantiles], new double[]{var});
                // skewness
                quantifications.setQuantificationObjectNumber((KeyParameterObjectNumber)allKeys[i].getKeys()[idxQuantiles+1], new double[]{Spamoments[2][0]+Spamoments[2][1]+Spamoments[2][2]});
                // kurtosis
                quantifications.setQuantificationObjectNumber((KeyParameterObjectNumber)allKeys[i].getKeys()[idxQuantiles+2], new double[]{Spamoments[3][0]+Spamoments[3][1]+Spamoments[3][2]});

                // RAC
                RadialAutoCorrelation rac = new RadialAutoCorrelation(filteredImages[i], mask, 2);
                for (int r=0;r<(idxRAC-idxSpaMom);r++) {
                    quantifications.setQuantificationObjectNumber((KeyParameterObjectNumber)allKeys[i].getKeys()[idxSpaMom+r], new double[]{rac.getCorrelation(r+1, r+1)});
                    //if (this.verbose) ij.IJ.log("rac :"+(r+1)+ allKeys[i].getKeys()[idxSpaMom+r].getLabel());
                }

                // texture
                GLCMTexture3D tex = new GLCMTexture3D(rac.intensityResampled, rac.maskResampled, 256, 0, 1);
                for (int r=0; r<8; r++) {
                    tex.computeMatrix(r+1);
                    int idx = idxRAC+r*5;
                    /*quantifications.setQuantificationObjectNumber((KeyParameterObjectNumber)allKeys[i].getKeys()[idx], new double[]{tex.getCorrelation()});
                    quantifications.setQuantificationObjectNumber((KeyParameterObjectNumber)allKeys[i].getKeys()[idx+1], new double[]{tex.getASM()});
                    quantifications.setQuantificationObjectNumber((KeyParameterObjectNumber)allKeys[i].getKeys()[idx+2], new double[]{tex.getContrast()});
                    quantifications.setQuantificationObjectNumber((KeyParameterObjectNumber)allKeys[i].getKeys()[idx+3], new double[]{tex.getEntropy()});
                    quantifications.setQuantificationObjectNumber((KeyParameterObjectNumber)allKeys[i].getKeys()[idx+4], new double[]{tex.getIDM()});
                    */
                    //if (this.verbose) ij.IJ.log("tex sum:"+ allKeys[i].getKeys()[idx+5].getLabel());
                }
            }
        }
        
        
            
            
            
            
        
        
    }
    
    
    @Override
    public Parameter[] getParameters() {
        return parameters;
    }

    @Override
    public Parameter[] getKeys() {
        int count = 0;
        for (int i = 0; i<allKeysMatrix.length;i++) {
            allKeysMatrix[i] = getKeys(allKeysMatrix[i], i);
            count += allKeysMatrix[i].length;
        }
        GroupKeyParameter[] res = new GroupKeyParameter[count];
        int idx = 0;
        for (int i = 0; i<allKeysMatrix.length;i++) {
            for (int j = 0; j<allKeysMatrix[i].length; j++) res[idx++]=allKeysMatrix[i][j];
        }
        return res;
    }
    
    private GroupKeyParameter[] getKeys(GroupKeyParameter[] keys, int measureIdx) {
        ArrayList<String> prefixEnd = getPrefixEnd(measureIdx);
        GroupKeyParameter[] newKeys=new GroupKeyParameter[prefixEnd.size()];
        if (keys==null) {
            for (int i = 0 ; i<newKeys.length; i++ ) {
                String p = prefixEnd.get(i);
                newKeys[i]=(GroupKeyParameter)this.defaultKeys.duplicate("Keys for :"+p+ " ", "keys"+p);
                newKeys[i].getPrefix().setText(this.prefix.getText()+p);
            }
        } else {
            for (int i = 0; i<newKeys.length; i++) {
                String p = prefixEnd.get(i);
                if (i<keys.length && keys[i].getPrefix().getKey().equals(this.prefix.getText()+p))  newKeys[i]=keys[i];
                else {
                    newKeys[i]=(GroupKeyParameter)this.defaultKeys.duplicate("Keys for :"+p +" ", "keys"+p);
                    newKeys[i].getPrefix().setText(this.prefix.getText()+p);
                }
            }
        }
        return newKeys;
    }

    
    
    @Override
    public String getHelp() {
        return "Measure multiple features on multiple pre-filtered version of a signal";
    }


    @Override
    public void setMultithread(int nbCPUs) {
        this.nCPUs = nbCPUs;
    }

    @Override
    public void setVerbose(boolean verbose) {
        this.verbose = verbose;
    }
    
    public static double[]computeHistogramMoments(ImageHandler intensityMap, Object3D mask) { // returns mean, sd, skewness, kurtosis
        double mean=0, sd=0, skewness=0, kurtosis=0;
        for (Voxel3D v : mask.getVoxels()) mean+=intensityMap.getPixel(v);
        int nbPix = mask.getVolumePixels();
        mean/=nbPix;
        for (Voxel3D v : mask.getVoxels()) {
            double dv=intensityMap.getPixel(v)-mean;
            sd+=dv*dv;
            skewness+=Math.pow(dv, 3);
            kurtosis+=Math.pow(dv, 4);
        }
        // normaliza variance
        sd= Math.sqrt(sd/nbPix);
        skewness = skewness / (nbPix * Math.pow(sd, 3));
        kurtosis = kurtosis / (nbPix * Math.pow(sd, 4)) - 3;
        return new double[]{mean, sd, skewness, kurtosis};
    }
    
}
