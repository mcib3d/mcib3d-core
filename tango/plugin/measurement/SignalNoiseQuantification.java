package tango.plugin.measurement;

import ij.ImagePlus;
import ij.WindowManager;
import ij.gui.GenericDialog;
import ij.measure.ResultsTable;
import ij.plugin.filter.PlugInFilter;
import ij.process.ImageProcessor;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import mcib3d.geom.Object3D;
import mcib3d.image3d.ImageByte;
import mcib3d.image3d.ImageHandler;
import mcib3d.image3d.ImageInt;
import mcib3d.image3d.ImageStats;
import mcib_plugins.analysis.simpleMeasure;
import tango.dataStructure.InputCellImages;
import tango.dataStructure.ObjectQuantifications;
import tango.dataStructure.SegmentedCellImages;
import tango.parameter.*;
import tango.plugin.measurement.Measurement;
import tango.plugin.measurement.MeasurementObject;

/*
 * To change this template, choose Tools | Templates and open the template in
 * the editor.
 */
/**
 *
 **
 * /**
 * Copyright (C) 2008- 2012 Thomas Boudier and others
 *
 *
 *
 * This file is part of mcib3d
 *
 * mcib3d is free software; you can redistribute it and/or modify it under the
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
 * @author thomas
 */
public class SignalNoiseQuantification implements MeasurementObject {

    StructureParameter channel1 = new StructureParameter("Structure:", "structure", -1, false);
    PreFilterSequenceParameter preFilters = new PreFilterSequenceParameter("Pre-Filters", "preFilters");
    ThresholdParameter thresh = new ThresholdParameter("Threshold", "thld", "AutoThreshold");
    Parameter[] parameters = new Parameter[]{channel1, preFilters, thresh};
    KeyParameterObjectNumber avg_over = new KeyParameterObjectNumber("Average over threshold", "averageOver", "averageOver", true);
    KeyParameterObjectNumber avg_under = new KeyParameterObjectNumber("Average under threshold", "averageUnder", "averageUnder", true);
    KeyParameterObjectNumber sd_over = new KeyParameterObjectNumber("Standard Deviation over threshold", "sdOver", "sdOver", true);
    KeyParameterObjectNumber sd_under = new KeyParameterObjectNumber("Standard Deviation under threshold", "sdUnder", "sdUnder", true);
    KeyParameterObjectNumber snr = new KeyParameterObjectNumber("Signal to noise ratio", "snr", "snr", true);
    KeyParameterObjectNumber thld_k = new KeyParameterObjectNumber("Threshold", "thld", "threshold", true);
    
    KeyParameter[] keys = new KeyParameter[]{thld_k, avg_over, avg_under, sd_over, sd_under, snr};
    GroupKeyParameter group = new GroupKeyParameter("", "signalNoiseQuantif", "", true, keys, false);
    
    Parameter[] returnKeys = new Parameter[]{group};
    int nCPUs=1;
    boolean verbose=false;
    public SignalNoiseQuantification() {
        //quantiles.getSpinner().setFireChangeOnAction();
    }
    
    @Override
    public void setMultithread(int nbCPUs) {
        this.nCPUs=nbCPUs;
    }
    
    @Override
    public void setVerbose(boolean verbose) {
        this.verbose=verbose;
    }

    @Override
    public void getMeasure(InputCellImages raw, SegmentedCellImages seg, ObjectQuantifications quantifications) {
        
        ImageHandler intensityMap = raw.getImage(channel1.getIndex());
        intensityMap=preFilters.runPreFilterSequence(channel1.getIndex(), intensityMap, raw, nCPUs, verbose);
        double thld = thresh.getThreshold(intensityMap, raw, nCPUs, verbose);
        ImageByte maskOver = intensityMap.threshold((float)thld, false, true);
        ImageByte maskUnder = intensityMap.threshold((float)thld, true, false);
        
        ImageStats over = intensityMap.getImageStats(maskOver);
        ImageStats under = intensityMap.getImageStats(maskUnder);
        if (thld_k.isSelected()) {
            quantifications.setQuantificationObjectNumber(thld_k, new double[]{thld});
        }
        if (avg_over.isSelected()) {
            quantifications.setQuantificationObjectNumber(avg_over, new double[]{over.getMean()});
        }
        if (avg_under.isSelected()) {
            quantifications.setQuantificationObjectNumber(avg_under, new double[]{under.getMean()});
        }
        if (sd_over.isSelected()) {
            quantifications.setQuantificationObjectNumber(sd_over, new double[]{over.getStandardDeviation()});
        }
        if (sd_under.isSelected()) {
            quantifications.setQuantificationObjectNumber(sd_under, new double[]{under.getStandardDeviation()});
        }
        if (snr.isSelected()) {
            double snrvalue = -1;
            if (under.getStandardDeviation()>0) snrvalue = (over.getMean()-under.getMean()) / under.getStandardDeviation();
            quantifications.setQuantificationObjectNumber(snr, new double[]{snrvalue});
        }
    }

    @Override
    public Parameter[] getParameters() {
        return parameters;
    }

    @Override
    public Parameter[] getKeys() {
        return returnKeys;
    }

    @Override
    public int getStructure() {
        return 0;
    }

    @Override
    public String getHelp() {
        return "";
    }
}
