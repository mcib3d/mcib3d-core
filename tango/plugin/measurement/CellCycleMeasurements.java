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
    BooleanParameter doErodeNuc =  new BooleanParameter("Erode nucleus", "doErodeNuc", false);
    ConditionalParameter condErodeNuc = new ConditionalParameter(doErodeNuc);
    double defRadErodeNuc=6;
    DoubleParameter radErodeNuc = new DoubleParameter("Radius (pix)", "radErodeNuc", defRadErodeNuc, DoubleParameter.nfDEC1);
    KeyParameterObjectNumber prolif_int = new KeyParameterObjectNumber("Proliferation marker mean intensity", "proliferation_mean_intensity");
    KeyParameterObjectNumber rac2 = new KeyParameterObjectNumber("RadialAutoCorrelation rad:2", "proliferation_rac2");
    
    BooleanParameter doReplication =  new BooleanParameter("Replication Marker", "doReplication", false);
    ConditionalParameter condReplication = new ConditionalParameter(doReplication);
    FilteredStructureParameter replication = new FilteredStructureParameter("Replication Marker:", "replicationMarker");
    KeyParameterObjectNumber replication_int = new KeyParameterObjectNumber("Replication marker Mean Intensity", "replication_mean_intensity");
    KeyParameterObjectNumber rep_rac2 = new KeyParameterObjectNumber("Replication RadialAutoCorrelation rad:2", "replication_rac2");
    
    BooleanParameter doNL =  new BooleanParameter("Compute?","doNL", false);
    ConditionalParameter condNL = new ConditionalParameter("Localisation towards nucleoli and periphery", doNL);
    StructureParameter nl = new StructureParameter("Nucleoli:", "nl", -1, true);
    KeyParameterObjectNumber rep_loc = new KeyParameterObjectNumber("Replication Localization", "replication_loc");
    
    
    GroupKeyParameter allKeys = new GroupKeyParameter("", "cellCycleKeys", "", true, null, false);
    
    Parameter[] parameters = new Parameter[]{condProlif, condReplication};

    public CellCycleMeasurements() {
        doProlif.setFireChangeOnAction();
        condProlif.setCondition(true, new Parameter[]{proliferation, condErodeNuc});
        condErodeNuc.setCondition(true, new Parameter[]{radErodeNuc});
        doReplication.setFireChangeOnAction();
        condReplication.setCondition(true, new Parameter[]{replication, condNL});
        doNL.setFireChangeOnAction();
        condNL.setCondition(true, new Parameter[]{nl});
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
                rac = new RadialAutoCorrelation(rep, raw.getMask(), true);
                if (rep_rac2.isSelected()) quantifications.setQuantificationObjectNumber(rep_rac2, new double[]{rac.getCorrelation(2)});
                if (verbose) rac.intensityResampled.show("RAC Image");
                if (verbose) rep.show("prolif Image");
            }
            if (this.doNL.isSelected() && rep_loc.isSelected()) {// grayscale radial moment
                ImageHandler dm = DistanceMapParameter.getMaskAndDistanceMap(raw,  seg, 0, new int[] {0, nl.getIndex()}, true, false, false, null, verbose, nCPUs)[1];
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
            double prolifMean = nuc.getPixMeanValue(prolif);
            if (prolif_int.isSelected()) quantifications.setQuantificationObjectNumber(prolif_int, new double[]{prolifMean});
            
            RadialAutoCorrelation rac=null;
            if (rac2.isSelected()) {
                rac = new RadialAutoCorrelation(prolif, mask, true);
                if (rac2.isSelected()) quantifications.setQuantificationObjectNumber(rac2, new double[]{rac.getCorrelation(2)});
                if (verbose) rac.intensityResampled.show("RAC Image");
                if (verbose) prolif.show("prolif Image");
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
        if (doProlif.isSelected()) nbKeys+=2;
        if (doReplication.isSelected()) nbKeys+=2;
        if (doNL.isSelected()) nbKeys+=1;
        KeyParameter[] keys = new KeyParameter[nbKeys];
        keys[0]= this.nuc_int;
        keys[1] = this.nuc_vol;
        int count=2;
        if (doProlif.isSelected()) {
            keys[count++] = prolif_int;
            keys[count++] = rac2;
        }
        if (doReplication.isSelected()) {
            keys[count++] = replication_int;
            keys[count++] = rep_rac2;
        }
        if (doNL.isSelected()) {
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
