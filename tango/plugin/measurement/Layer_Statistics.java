/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package tango.plugin.measurement;

import ij.IJ;
import ij.ImagePlus;
import ij.WindowManager;
import ij.gui.GenericDialog;
import ij.measure.Calibration;
import ij.measure.ResultsTable;
import ij.plugin.filter.PlugInFilter;
import ij.process.ImageProcessor;
import java.util.ArrayList;
import mcib3d.geom.Object3D;
import mcib3d.geom.Object3DVoxels;
import mcib3d.geom.Objects3DPopulation;
import mcib3d.geom.Objects3DPopulationAnalysis;
import mcib3d.image3d.ImageByte;
import mcib3d.image3d.ImageHandler;
import mcib3d.image3d.ImageInt;
import mcib3d.image3d.ImageShort;
import tango.dataStructure.InputCellImages;
import tango.dataStructure.ObjectQuantifications;
import tango.dataStructure.SegmentedCellImages;
import tango.parameter.*;

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
public class Layer_Statistics implements PlugInFilter, MeasurementObject {

    ImagePlus plus;
    private int mask_idx;
    private int dapi_idx;
    private float radMin;
    private float radMax;
    // TANGO
    // parameters
    StructureParameter channel1 = new StructureParameter("Structure objects:", "structure", -1, true);
    StructureParameter channel2 = new StructureParameter("Structure signal:", "structureSignal", -1, false);
    IntParameter TradMin = new IntParameter("Radius Minimum", "radmin", 0);
    IntParameter TradMax = new IntParameter("Radius Maximum", "radmax", 2);
    BooleanParameter TuseZcalibration = new BooleanParameter("Use Z calibration", "zcalibration", true);
    Parameter[] parameters = new Parameter[]{channel1, channel2, TradMin, TradMax, TuseZcalibration};
    // keys for measure
    KeyParameterObjectNumber k_avg = new KeyParameterObjectNumber("Average", "Layer_average", "Layer_average", true);
    KeyParameterObjectNumber k_sd = new KeyParameterObjectNumber("Standard Deviation", "Layer_standardDeviation", "Layer_standardDeviation", true);
    KeyParameterObjectNumber k_min = new KeyParameterObjectNumber("Minimum", "Layer_minimum", "Layer_minimum", true);
    KeyParameterObjectNumber k_max = new KeyParameterObjectNumber("Maximum", "Layer_maximum", "Layer_maximum", true);
    KeyParameterObjectNumber k_id = new KeyParameterObjectNumber("Integrated Density", "Layer_integratedDensity", "Layer_integratedDensity", true);
    KeyParameter[] keys = new KeyParameter[]{k_avg, k_sd, k_min, k_max, k_id};
    GroupKeyParameter group = new GroupKeyParameter("", "layerMeasureStatistics", "", true, keys, false);
    int nCPUs = 1;
    boolean verbose = false;

    public Layer_Statistics() {
        channel1.setHelp("The structure corresponding to objects", true);
        channel2.setHelp("The channel corresponding to signal to quantify ", true);
        TradMin.setHelp("The Radius corresponding to the interior border of layer , if positive a dilated object, if negative an eroded object. Max radius should always be higher that min radius, if positive (4>2), if negative (-2>-4).", true);
        TradMax.setHelp("The Radius corresponding to exterior border of layer, if positive a dilated object, if negative a eroded object. Max radius should always be higher that min radius, if positive (4>2), if negative (-2>-4).", true);
        TuseZcalibration.setHelp("Use z calibration to compute Z radius, otherwise uses same as XY", verbose);
        k_avg.setHelp("The average intensity value inside layer", true);
        k_sd.setHelp("The standard deviation intensity value inside layer", true);
        k_min.setHelp("The minimal intensity value inside layer", true);
        k_max.setHelp("The maximal intensity value inside layer", true);
        k_id.setHelp("The raw integrated density inside layer, i. e. the summ of all pixel values inside layer", true);
    }

    @Override
    public int setup(String arg, ImagePlus imp) {
        plus = imp;

        return DOES_16 + DOES_8G;
    }

    @Override
    public void run(ImageProcessor ip) {
        if (dialogue()) {
            process();
        }
    }

    private void process() {
        ImageHandler mask = ImageHandler.wrap(WindowManager.getImage(mask_idx + 1));
        ImageHandler signal = ImageHandler.wrap(WindowManager.getImage(dapi_idx + 1));
        Objects3DPopulation pop = new Objects3DPopulation(mask.getImagePlus());
        ResultsTable rt = ResultsTable.getResultsTable();
        if (rt == null) {
            rt = new ResultsTable();
        }
        rt.reset();

        for (int i = 0; i < pop.getNbObjects(); i++) {
            Object3D ob = pop.getObject(i);
            Object3DVoxels obMax;
            if (radMax > 0) {
                obMax = ob.getDilatedObject(radMax, radMax, radMax, true);
            } else {
                obMax = ob.getErodedObject(-radMax, -radMax, -radMax, true);
            }
            Object3DVoxels obMin;
            if (radMin > 0) {
                obMin = ob.getDilatedObject(radMin, radMin, radMin, true);
            } else {
                obMin = ob.getErodedObject(-radMin, -radMin, -radMin, true);
            }
//        ImageInt test=new ImageShort("test",512,512,512);
//        test.draw(obMax, 200);
//        test.draw(obMin, 100);
//        test.draw(ob, 50);
//        test.show();

            obMax.substractObject(obMin);
            rt.setValue("Avg_object", i, ob.getMeanPixValue(signal));
            rt.setValue("IntDen_object", i, ob.getIntegratedDensity(signal));
            rt.setValue("Avg_layer", i, obMax.getMeanPixValue(signal));
            rt.setValue("IntDen_layer", i, obMax.getIntegratedDensity(signal));
        }
    }

    private boolean dialogue() {
        int nbima = WindowManager.getImageCount();
        if (nbima < 2) {
            IJ.error("Needs two images");
            return false;
        }
        String[] namesA = new String[nbima];
        String[] namesB = new String[nbima];
        for (int i = 0; i < nbima; i++) {
            namesA[i] = WindowManager.getImage(i + 1).getShortTitle();
            namesB[i] = WindowManager.getImage(i + 1).getShortTitle();
        }

        GenericDialog dia = new GenericDialog("Pair Test");
        dia.addChoice("Image_Mask", namesA, namesA[0]);
        dia.addChoice("Image_DAPI", namesB, namesB[1]);
        dia.addNumericField("rad_min", 2, 1);
        dia.addNumericField("rad_max", 4, 1);
        dia.showDialog();
        mask_idx = dia.getNextChoiceIndex();
        dapi_idx = dia.getNextChoiceIndex();
        radMin = (float) dia.getNextNumber();
        radMax = (float) dia.getNextNumber();

        return dia.wasOKed();
    }

    // TANGO
    @Override
    public int getStructure() {
        return channel1.getIndex();
    }

    @Override
    public void getMeasure(InputCellImages rawImages, SegmentedCellImages segmentedImages, ObjectQuantifications quantifications) {
        Object3D[] os = segmentedImages.getObjects(channel1.getIndex());
        int nb = os.length;
        ImageHandler intensityMap = rawImages.getImage(channel2.getIndex());
        radMax = TradMax.getIntValue(1);
        radMin = TradMin.getIntValue(0);
        float radMaxZ = radMax;
        float radMinZ = radMin;
        if (TuseZcalibration.isSelected()) {
            float ratio = (float) (intensityMap.getScaleZ() / intensityMap.getScaleXY());
            radMaxZ = radMax / ratio;
            radMinZ = radMinZ / ratio;
        }

        double[] avg = null;
        if (k_avg.isSelected()) {
            avg = new double[nb];
        }
        double[] sd = null;
        if (k_sd.isSelected()) {
            sd = new double[nb];
        }
        double[] min = null;
        if (k_min.isSelected()) {
            min = new double[nb];
        }
        double[] max = null;
        if (k_max.isSelected()) {
            max = new double[nb];
        }
        double[] id = null;
        if (k_id.isSelected()) {
            id = new double[nb];
        }
        // create mask for visu in case of verbose
        ImageHandler mask = intensityMap.createSameDimensions();
        for (int i = 0; i < nb; i++) {
            Object3D ob = os[i];
            Object3DVoxels obMax;
            // compute max (dilated) object 
            if (radMax > 0) {
                obMax = ob.getDilatedObject(radMax, radMax, radMaxZ, true);
            } else {
                obMax = ob.getErodedObject(-radMax, -radMax, -radMaxZ, true);
            }
            // compute min (dilated) object 
            Object3DVoxels obMin;
            if (radMin > 0) {
                obMin = ob.getDilatedObject(radMin, radMin, radMinZ, true);
            } else {
                obMin = ob.getErodedObject(-radMin, -radMin, -radMinZ, true);
            }
            // compute difference of objects to get layer
            obMax.substractObject(obMin);
            if (verbose) {
                obMax.draw(mask, ob.getValue());
            }
            //IJ.log("Ob layer " + obMax.getVolumePixels());
            if (k_avg.isSelected()) {
                avg[i] = obMax.getMeanPixValue(intensityMap);
            }
            if (k_sd.isSelected()) {
                sd[i] = obMax.getStDevPixValue(intensityMap);
            }

            if (k_max.isSelected()) {
                max[i] = obMax.getPixMaxValue(intensityMap);
            }
            if (k_id.isSelected()) {
                id[i] = obMax.getIntegratedDensity(intensityMap);
            }
        }
        if (k_avg.isSelected()) {
            quantifications.setQuantificationObjectNumber(k_avg, avg);
        }
        if (k_sd.isSelected()) {
            quantifications.setQuantificationObjectNumber(k_sd, sd);
        }
        if (k_min.isSelected()) {
            quantifications.setQuantificationObjectNumber(k_min, min);
        }
        if (k_max.isSelected()) {
            quantifications.setQuantificationObjectNumber(k_max, max);
        }
        if (k_id.isSelected()) {
            quantifications.setQuantificationObjectNumber(k_id, id);
        }
        // show mask
        if (verbose) {
            mask.show("Mask_layer");
        }
    }

    @Override
    public Parameter[] getKeys() {
        return new Parameter[]{group};
    }

    @Override
    public Parameter[] getParameters() {
        return parameters;
    }

    @Override
    public String getHelp() {
        return ("Intensity measure in layers defined by the radius min and max.");
    }

    @Override
    public void setVerbose(boolean verbose) {
        this.verbose = verbose;
    }

    @Override
    public void setMultithread(int nCPUs) {
        this.nCPUs = nCPUs;
    }
}
