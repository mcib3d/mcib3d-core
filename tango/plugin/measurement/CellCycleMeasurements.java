package tango.plugin.measurement;

import ij.IJ;
import mcib3d.geom.Object3DVoxels;
import mcib3d.image3d.ImageByte;
import mcib3d.image3d.ImageFloat;
import mcib3d.image3d.ImageHandler;
import mcib3d.image3d.ImageInt;
import tango.dataStructure.InputCellImages;
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
public class CellCycleMeasurements implements MeasurementObject {
    
    int nCPUs = 1;
    boolean verbose;
    
    // DNA Staining
    KeyParameterObjectNumber nuc_vol = new KeyParameterObjectNumber("Nucleus Volume (Unit)", "nucleus_volume");
    KeyParameterObjectNumber nuc_int = new KeyParameterObjectNumber("DNA Staining Integrated Intensity", "DNA_staining_intensity");
    
    BooleanParameter doProlif =  new BooleanParameter("Proliferation Marker", "doProlif", false);
    ConditionalParameter condProlif = new ConditionalParameter(doProlif);
    FilteredStructureParameter proliferation = new FilteredStructureParameter("Proliferation marker:", "proliferationMarker");
    
    BooleanParameter doNLProlif =  new BooleanParameter("Compute?","doNLProlif", false);
    ConditionalParameter condNLProlif = new ConditionalParameter("Estimation of Proliferation signal within nucleoli", doNLProlif);
    StructureParameter nlProlif = new StructureParameter("Nucleoli:", "nlProlif", -1, true);
    SliderParameter smoothProlif = new SliderParameter("Smoothing radius for proliferation signal:", "smoothProlif", 0, 10, 1);
    KeyParameterObjectNumber prolif_nl_inside_mean = new KeyParameterObjectNumber("Proliferation: mean intensity inside NL", "prolif_nl_inside_mean");
    KeyParameterObjectNumber prolif_nl_inside_med = new KeyParameterObjectNumber("Proliferation: median intensity inside NL", "prolif_nl_inside_med");
    KeyParameterObjectNumber prolif_nl_inside_sd = new KeyParameterObjectNumber("Proliferation: standard deviation of intensity inside NL", "prolif_nl_inside_sd");
    KeyParameterObjectNumber prolif_nl_outside_mean = new KeyParameterObjectNumber("Proliferation: mean intensity outside NL", "prolif_nl_outside_mean");
    KeyParameterObjectNumber prolif_nl_outside_med = new KeyParameterObjectNumber("Proliferation: median intensity outside NL", "prolif_nl_outside_med");
    KeyParameterObjectNumber prolif_nl_outside_sd = new KeyParameterObjectNumber("Proliferation: standard deviation of intensity outside NL", "prolif_nl_outside_sd");
    KeyParameterObjectNumber prolif_nl_snr = new KeyParameterObjectNumber("Proliferation: SNR", "prolif_nl_snr");
    KeyParameterObjectNumber prolif_nl_ttest_statistic = new KeyParameterObjectNumber("Proliferation: t-test statistic", "prolif_nl_ttest_statistic");
    
    BooleanParameter doErodeNuc =  new BooleanParameter("Erode nucleus", "doErodeNuc", false);
    ConditionalParameter condErodeNuc = new ConditionalParameter(doErodeNuc);
    double defRadErodeNuc=6;
    DoubleParameter radErodeNuc = new DoubleParameter("Radius (pix)", "radErodeNuc", defRadErodeNuc, DoubleParameter.nfDEC1);
    KeyParameterObjectNumber prolif_int = new KeyParameterObjectNumber("Proliferation marker mean intensity", "proliferation_mean_intensity");
    KeyParameterObjectNumber prolif_rac1 = new KeyParameterObjectNumber("Proliferation RadialAutoCorrelation rad:1", "proliferation_rac1");
    KeyParameterObjectNumber prolif_sd = new KeyParameterObjectNumber("Proliferation: standard deviation", "proliferation_sd");
    
    
    BooleanParameter doReplication =  new BooleanParameter("Replication Marker", "doReplication", false);
    ConditionalParameter condReplication = new ConditionalParameter(doReplication);
    FilteredStructureParameter replication = new FilteredStructureParameter("Replication Marker:", "replicationMarker");
    KeyParameterObjectNumber replication_int = new KeyParameterObjectNumber("Replication marker Mean Intensity", "replication_mean_intensity");
    KeyParameterObjectNumber rep_rac2 = new KeyParameterObjectNumber("Replication RadialAutoCorrelation rad:2", "replication_rac2");
    
    BooleanParameter doNLRep =  new BooleanParameter("Compute?","doNL", false);
    ConditionalParameter condNLRep = new ConditionalParameter("Localisation towards nucleoli and periphery", doNLRep);
    StructureParameter nlRep = new StructureParameter("Nucleoli:", "nl", -1, true);
    
    KeyParameterObjectNumber rep_loc = new KeyParameterObjectNumber("Replication Localization", "replication_loc");
    
    
    GroupKeyParameter allKeys = new GroupKeyParameter("", "cellCycleKeys", "", true, null, false);
    
    Parameter[] parameters = new Parameter[]{condProlif, condReplication};

    public CellCycleMeasurements() {
        doProlif.setFireChangeOnAction();
        condProlif.setCondition(true, new Parameter[]{proliferation, condErodeNuc, smoothProlif}); //condNLProlif
        doNLProlif.setFireChangeOnAction();
        condNLProlif.setCondition(true, new Parameter[]{nlProlif});
        condErodeNuc.setCondition(true, new Parameter[]{radErodeNuc});
        doReplication.setFireChangeOnAction();
        condReplication.setCondition(true, new Parameter[]{replication, condNLRep});
        doNLRep.setFireChangeOnAction();
        condNLRep.setCondition(true, new Parameter[]{nlRep});
    }

    @Override
    public int getStructure() {
        return 0;
    }

    @Override
    public void getMeasure(InputCellImages raw, SegmentedCellImages seg, ObjectQuantifications quantifications) {
        ImageInt mask = raw.getMask();
        Object3DVoxels nuc = seg.getObjects(0)[0];
        ImageHandler nucImage = raw.getImage(0);
        // DNA staining
        if (nuc_vol.isSelected()) {
            quantifications.setQuantificationObjectNumber(nuc_vol, new double[]{nuc.getVolumeUnit()});
        }
        if (nuc_int.isSelected()) {
            quantifications.setQuantificationObjectNumber(nuc_int, new double[]{nuc.getIntegratedDensity(nucImage)});
        }
        
        if (doReplication.isSelected()) {
            ImageHandler rep = replication.getImage(raw, verbose, nCPUs);
            if (replication_int.isSelected()) {
                quantifications.setQuantificationObjectNumber(replication_int, new double[]{nuc.getPixMeanValue(rep)});
            }           
            RadialAutoCorrelation rac=null;
            if (rep_rac2.isSelected()) {
                rac = new RadialAutoCorrelation(rep, mask, 2);
                if (rep_rac2.isSelected()) quantifications.setQuantificationObjectNumber(rep_rac2, new double[]{rac.getCorrelation(2, 2)});
                if (verbose) rac.intensityResampled.show("RAC replication Image");
            }
            if (this.doNLRep.isSelected() && rep_loc.isSelected()) {// grayscale radial moment
                ImageHandler dm = DistanceMapParameter.getMaskAndDistanceMap(raw,  seg, 0, new int[] {0, nlRep.getIndex()}, true, true, false, null, verbose, nCPUs)[1];
                GrayscaleRadialMoments grm = new GrayscaleRadialMoments();
                grm.computeMoments(rep, mask, (ImageFloat)dm);
                quantifications.setQuantificationObjectNumber(rep_loc, new double[]{grm.getMean()});                    
            }    
            if (verbose) rep.show("Replication Image");
        }
        
        if (doProlif.isSelected()) {
            if (doErodeNuc.isSelected()) {
                double rad = this.radErodeNuc.getDoubleValue(defRadErodeNuc) * mask.getScaleXY();
                ImageByte erodedMask = mask.erode((float)rad, nCPUs);
                if (verbose) erodedMask.show("Eroded Mask: "+this.radErodeNuc.getDoubleValue(defRadErodeNuc));
                Object3DVoxels[] obj = erodedMask.getObjects3D();
                if (obj.length>=1) {
                    nuc=obj[0];
                    mask = erodedMask;
                }
                else {
                    if (Core.GUIMode) IJ.log("Cell cycle measurement error: unable to erode nucleus");
                    else System.out.println("Cell cycle measurement error: unable to erode nucleus");
                    return;
                }
            }
            
            ImageHandler prolif = proliferation.getImage(raw, verbose, nCPUs); //gaussian par défaut?            
            if (this.smoothProlif.getValue()>0) prolif = prolif.gaussianSmooth(smoothProlif.getValue(), smoothProlif.getValue() * raw.getMask().getScaleXY() / raw.getMask().getScaleZ(), nCPUs);
            double prolifMean = nuc.getPixMeanValue(prolif);
            if (prolif_int.isSelected()) quantifications.setQuantificationObjectNumber(prolif_int, new double[]{prolifMean});
            double prolifSd = nuc.getPixStdDevValue(prolif);
            if (prolif_sd.isSelected()) quantifications.setQuantificationObjectNumber(prolif_sd, new double[]{prolifSd});
            
            
            RadialAutoCorrelation rac=null;
            if (prolif_rac1.isSelected()) {
                rac = new RadialAutoCorrelation(prolif, mask, 2);
                if (prolif_rac1.isSelected()) quantifications.setQuantificationObjectNumber(prolif_rac1, new double[]{rac.getCorrelation(1, 1)});
            }
            
            if (this.doNLProlif.isSelected()) {
                ImageByte maskNL = seg.getImage(nlProlif.getIndex()).toMask();
                ImageByte maskOutsideNL = mask.toMask();
                maskOutsideNL.substractMask(maskNL);
                if (verbose) {
                    maskNL.show("Nucleloi mask");
                    maskOutsideNL.show("Outside nucleoli mask");
                }
                Object3DVoxels[] nlsAr=maskNL.getObjects3D();
                Object3DVoxels[] onlsAr=maskOutsideNL.getObjects3D();
                if (nlsAr.length==1 && onlsAr.length==1) {
                    Object3DVoxels nls=nlsAr[0];
                    Object3DVoxels onls=onlsAr[0];
                    
                    // mean
                    double inMean = nls.getPixMeanValue(prolif);
                    quantifications.setQuantificationObjectNumber(prolif_nl_inside_mean, new double[]{inMean});
                    double outMean = onls.getPixMeanValue(prolif);
                    quantifications.setQuantificationObjectNumber(prolif_nl_outside_mean, new double[]{outMean});
                    // sd
                    double inSd = nls.getPixStdDevValue(prolif);
                    quantifications.setQuantificationObjectNumber(prolif_nl_inside_sd, new double[]{inSd});
                    double outSd = onls.getPixStdDevValue(prolif);
                    quantifications.setQuantificationObjectNumber(prolif_nl_outside_sd, new double[]{outSd});
                    //med
                    quantifications.setQuantificationObjectNumber(prolif_nl_inside_med, new double[]{Quantiles.getQuantiles(nls, prolif, new double[]{0.5})[0]});
                    quantifications.setQuantificationObjectNumber(prolif_nl_outside_med, new double[]{Quantiles.getQuantiles(onls, prolif, new double[]{0.5})[0]});
                    
                    if (outSd!=0) quantifications.setQuantificationObjectNumber(prolif_nl_snr, new double[]{(inMean-outMean)/outSd});
                    double outN = onls.getVolumePixels();
                    double inN = nls.getVolumePixels();
                    if (outSd!=0 && inSd!=0 && outN!=0 && inN!=0) quantifications.setQuantificationObjectNumber(prolif_nl_ttest_statistic, new double[]{(inMean-outMean)/Math.sqrt(outSd*outSd/outN + inSd*inSd/inN)});
                
                } else {
                    //erreurs
                    if (nlsAr.length==0) System.out.println("Cell Cycle Measurement error, no nucleoli found in image:"+seg.getImage(nlProlif.getIndex()).getTitle());
                    if (onlsAr.length==0) System.out.println("Cell Cycle Measurement error, no nucleus found in image:"+seg.getImage(0).getTitle());
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
        int nbKeys = 2;
        if (doProlif.isSelected()) nbKeys+=3;
        if (doNLProlif.isSelected()) nbKeys+=8;
        if (doReplication.isSelected()) nbKeys+=2;
        if (doNLRep.isSelected()) nbKeys+=1;
        KeyParameter[] keys = new KeyParameter[nbKeys];
        keys[0]= this.nuc_int;
        keys[1] = this.nuc_vol;
        int count=2;
        if (doProlif.isSelected()) {
            keys[count++] = prolif_int;
            keys[count++] = prolif_sd;
            keys[count++] = prolif_rac1;
        }
        if (doNLProlif.isSelected()) {
            keys[count++] = prolif_nl_inside_mean;
            keys[count++] = prolif_nl_inside_med;
            keys[count++] = prolif_nl_inside_sd;
            keys[count++] = prolif_nl_outside_mean;
            keys[count++] = prolif_nl_outside_med;
            keys[count++] = prolif_nl_outside_sd;
            keys[count++] = prolif_nl_snr;
            keys[count++] = prolif_nl_ttest_statistic;
        }
        if (doReplication.isSelected()) {
            keys[count++] = replication_int;
            keys[count++] = rep_rac2;
        }
        if (doNLRep.isSelected()) {
            keys[count++] = rep_loc;
        }
        
        allKeys.setKeys(keys);
        return new Parameter[]{allKeys};
    }

    @Override
    public String getHelp() {
        return "";
    }


    @Override
    public void setMultithread(int nbCPUs) {
        this.nCPUs = nbCPUs;
    }

    @Override
    public void setVerbose(boolean verbose) {
        this.verbose = verbose;
    }
}
