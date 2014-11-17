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
import mcib_plugins.analysis.simpleMeasure;
import tango.dataStructure.InputCellImages;
import tango.dataStructure.InputImages;
import tango.dataStructure.ObjectQuantifications;
import tango.dataStructure.SegmentedCellImages;
import tango.gui.Core;
import tango.parameter.*;

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
    ChoiceParameter doErodeNuc =  new ChoiceParameter("Erode nucleus", "doErodeNuc", new String[]{"No Erosion", "Constant Radius", "Proportion of Volume"}, "No Erosion");
    ConditionalParameter condErodeNuc = new ConditionalParameter(doErodeNuc);
    double defRadErodeNuc=3;
    DoubleParameter radErodeNuc = new DoubleParameter("Radius (pix)", "radErodeNuc", defRadErodeNuc, DoubleParameter.nfDEC1);
    SliderDoubleParameter perErodeNuc = new SliderDoubleParameter("Proportion of nuclear volume:", "perErodeNuc", 0d, 1d, 0.3d, 2);
    PreFilterSequenceParameter defPrefSeq = new PreFilterSequenceParameter("Pre-Filters", "preFilters");
    MultiParameter preFilters = new MultiParameter("Pre-Filters", "multiprefilters", new PreFilterSequenceParameter[]{defPrefSeq}, 1, 100, 1);
    
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
    
    // Texture parameters
    
    
    GroupKeyParameter defaultKeys = new GroupKeyParameter("Keys for pre-filter:1 ", "keys1", "", true, 
            new KeyParameterObjectNumber[]{
                hist_mean, hist_sd, hist_skewness, hist_kurtosis, hist_sdmu, //histogram moments
                hist_quantile1000, hist_quantile1, hist_quantile10, hist_quantile50 //quantiles
            }, 
            false);
    GroupKeyParameter[] allKeys = new GroupKeyParameter[]{defaultKeys};
    Parameter[] parameters = new Parameter[]{structureSignal, preFilters}; //,condErodeNuc

    public TestFeatures() {
        preFilters.getSpinner().setFireChangeOnAction();
        condErodeNuc.setCondition("Constant Radius", new Parameter[]{radErodeNuc});
        condErodeNuc.setCondition("Proportion of Volume", new Parameter[]{perErodeNuc});
    }

    @Override
    public int getStructure() {
        return 0;
    }

    @Override
    public void getMeasure(InputCellImages raw, SegmentedCellImages seg, ObjectQuantifications quantifications) {
        ImageInt mask = raw.getMask();
        // TODO: erode mask
        Object3DVoxels nuc = seg.getObjects(0)[0]; // regénerer si erode nucleus
        
        ImageHandler rawSignal  = structureSignal.getImage(raw, false);
        ImageHandler[] filteredImages = new ImageHandler[this.preFilters.getNbParameters()];
        Parameter[] preFiltersParam = this.preFilters.getParameters();
        for (int i = 0; i<filteredImages.length; i++) {
            filteredImages[i] = runPreFilterSequence((MultiParameter)preFiltersParam[i], structureSignal.getIndex(), rawSignal, raw, nCPUs, verbose);
        }
        
        // Histogram Moments & quantiles
        for (int i = 0; i<filteredImages.length; i++) {
            double[] moments = computeHistogramMoments(filteredImages[i], nuc);
            for (int m=0;m<4;m++) quantifications.setQuantificationObjectNumber((KeyParameterObjectNumber)allKeys[i].getKeys()[m], new double[]{moments[m]});
            // sd / mean
            if (moments[0]!=0) quantifications.setQuantificationObjectNumber((KeyParameterObjectNumber)allKeys[i].getKeys()[4], new double[]{moments[1]/moments[0]});
            
            //Quantiles
            double[] quantiles = Quantiles.getQuantiles(nuc, rawSignal, new double[]{0.0001, 0.001, 0.1, 0.5});
            for (int q=0;q<4;q++) quantifications.setQuantificationObjectNumber((KeyParameterObjectNumber)allKeys[i].getKeys()[idxHistMoments+q], new double[]{quantiles[q]});
            
            // spatial moments
            double[][] Spamoments = GrayscaleSpatialMoments.computeMoments(filteredImages[i], mask);
            // variance
            quantifications.setQuantificationObjectNumber((KeyParameterObjectNumber)allKeys[i].getKeys()[idxQuantiles], new double[]{Math.sqrt(Spamoments[1][0]+Spamoments[1][1]+Spamoments[1][2])});
            // skewness
            quantifications.setQuantificationObjectNumber((KeyParameterObjectNumber)allKeys[i].getKeys()[idxQuantiles+1], new double[]{Spamoments[2][0]+Spamoments[2][1]+Spamoments[2][2]});
            // kurtosis
            quantifications.setQuantificationObjectNumber((KeyParameterObjectNumber)allKeys[i].getKeys()[idxQuantiles+2], new double[]{Spamoments[3][0]+Spamoments[3][1]+Spamoments[3][2]});
            
            RadialAutoCorrelation rac = new RadialAutoCorrelation(filteredImages[i], mask, true);
            quantifications.setQuantificationObjectNumber(rac1, new double[]{rac.getCorrelation(1)});
            
        }
        
        
            
            
            
            
        
        
    }
    
    private ImageHandler runPreFilterSequence(MultiParameter param ,  int currentStructureIdx, ImageHandler in, InputImages images, int nbCPUs, boolean verbose) {
        ImageHandler current = in;
        for (Parameter[] al : param.getParametersArrayList()) {
            PreFilterParameter pf = (PreFilterParameter)al[0];
            current = pf.preFilter(currentStructureIdx, current, images, nbCPUs, verbose);
            current.setScale(in);
            current.setOffset(in);
        }
        return current;
    }
    

    @Override
    public Parameter[] getParameters() {
        return parameters;
    }

    @Override
    public Parameter[] getKeys() {
        if (preFilters.getNbParameters()!=allKeys.length) {
            GroupKeyParameter[] newKeys = new GroupKeyParameter[preFilters.getNbParameters()];
            if (preFilters.getNbParameters()<allKeys.length) {
                System.arraycopy(allKeys, 0, newKeys, 0, newKeys.length);
            } else {
                System.arraycopy(allKeys, 0, newKeys, 0, allKeys.length);
                for (int i = allKeys.length ; i<newKeys.length; i++ ) newKeys[i]=(GroupKeyParameter)this.defaultKeys.duplicate("Keys for pre-filter:"+(i+1)+" ", "keys"+(i+1));
            }
            allKeys=newKeys;
        }
        return allKeys;
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
