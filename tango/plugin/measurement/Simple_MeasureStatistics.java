package tango.plugin.measurement;

import ij.ImagePlus;
import ij.WindowManager;
import ij.gui.GenericDialog;
import ij.measure.ResultsTable;
import ij.plugin.filter.PlugInFilter;
import ij.process.ImageProcessor;
import java.util.ArrayList;
import java.util.Iterator;
import mcib3d.geom.Object3D;
import mcib3d.image3d.ImageHandler;
import mcib3d.image3d.ImageInt;
import mcib_plugins.analysis.simpleMeasure;
import tango.dataStructure.InputCellImages;
import tango.dataStructure.ObjectQuantifications;
import tango.dataStructure.SegmentedCellImages;
import tango.parameter.*;

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
public class Simple_MeasureStatistics implements PlugInFilter, MeasurementObject {

    ImagePlus myPlus;
    int imaSpots;
    int imaSignal;
    // DB
    StructureParameter channel1 = new StructureParameter("Structure objects:", "structure", -1, true);
    StructureParameter channel2 = new StructureParameter("Structure signal:", "structureSignal", -1, false);
    BooleanParameter outside = new BooleanParameter("Outside Structure", "outside", false);
    BooleanParameter preFilter = new BooleanParameter("Use filtered image", "filtered", false);
    PreFilterSequenceParameter preFilters = new PreFilterSequenceParameter("Pre-Filters", "preFilters");
    MultiParameter quantiles = new MultiParameter("Quantiles:", "quantilesMP", new Parameter[]{new SliderDoubleParameter("Quantile", "quantile", 0, 1, 0.5d, 8)}, 0, 100, 0);
    Parameter[] parameters = new Parameter[]{channel1, outside, channel2, preFilter , preFilters, quantiles};
    KeyParameterObjectNumber k_avg = new KeyParameterObjectNumber("Average", "average", "average", true);
    KeyParameterObjectNumber k_sd = new KeyParameterObjectNumber("Standard Deviation", "standardDeviation", "standardDeviation", true);
    KeyParameterObjectNumber k_min = new KeyParameterObjectNumber("Minimum", "minimum", "minimum", true);
    KeyParameterObjectNumber k_max = new KeyParameterObjectNumber("Maximum", "maximum", "maximum", true);
    KeyParameterObjectNumber k_id = new KeyParameterObjectNumber("Integrated Density", "integratedDensity", "integratedDensity", true);
    KeyParameterObjectNumber k_sigma_mu = new KeyParameterObjectNumber("StDev / mean", "sigma_mu", "sigma_mu", true);
    KeyParameter[] keys = new KeyParameter[]{k_avg, k_sd, k_min, k_max, k_id, k_sigma_mu};
    GroupKeyParameter group = new GroupKeyParameter("", "simpleMeasureStatistics", "", true, keys, false);
    KeyParameterObjectNumber[] keysQ = new KeyParameterObjectNumber[0];
    GroupKeyParameter groupQ = new GroupKeyParameter("", "quantilesKeys", "", true, keysQ, false);
    Parameter[] returnKeys = new Parameter[]{group, groupQ};
    String[] keys_s = new String[]{"average", "standardDeviation", "minimum", "maximum", "integratedDensity"};
    int nCPUs = 1;
    boolean verbose = false;

    public Simple_MeasureStatistics() {
        quantiles.getSpinner().setFireChangeOnAction();
        outside.setFireChangeOnAction();
    }

    @Override
    public void setMultithread(int nbCPUs) {
        this.nCPUs = nbCPUs;
    }

    @Override
    public void setVerbose(boolean verbose) {
        this.verbose = verbose;
    }

    @Override
    public int setup(String arg, ImagePlus imp) {
        myPlus = imp;
        return PlugInFilter.DOES_16 + PlugInFilter.DOES_8G + PlugInFilter.STACK_REQUIRED;
    }

    @Override
    public void run(ImageProcessor ip) {
        if (Dialogue()) {
            simpleMeasure mes = new simpleMeasure(WindowManager.getImage(imaSpots));
            ResultsTable rt = ResultsTable.getResultsTable();
            if (rt == null) {
                rt = new ResultsTable();
            }
            ArrayList<double[]> res = mes.getMeasuresStats(WindowManager.getImage(imaSignal));
            int row = rt.getCounter();
            for (Iterator<double[]> it = res.iterator(); it.hasNext();) {
                rt.incrementCounter();
                double[] m = it.next();
                for (int k = 0; k < keys_s.length; k++) {
                    rt.setValue(keys_s[k], row, m[k]);
                }
                row++;
            }
            rt.updateResults();
            rt.show("Results");
        }
    }

    private boolean Dialogue() {
        int nbima = WindowManager.getImageCount();
        String[] names = new String[nbima];
        for (int i = 0; i < nbima; i++) {
            names[i] = WindowManager.getImage(i + 1).getShortTitle();
        }
        imaSpots = 0;
        imaSignal = nbima > 1 ? nbima - 1 : 0;

        GenericDialog dia = new GenericDialog("Statistical measure");
        dia.addChoice("Objects", names, names[imaSpots]);
        dia.addChoice("Signal", names, names[imaSignal]);
        dia.showDialog();
        imaSpots = dia.getNextChoiceIndex() + 1;
        imaSignal = dia.getNextChoiceIndex() + 1;

        return dia.wasOKed();
    }

    @Override
    public void getMeasure(InputCellImages raw, SegmentedCellImages seg, ObjectQuantifications quantifications) {
        Object3D[] os;
        if (outside.isSelected() && channel1.getIndex()!=0) {
            ImageInt structure = seg.getImage(channel1.getIndex());
            ImageInt mask = structure.invertMask(raw.getMask());
            os = mask.getObjects3D();
        } else {
            os = seg.getObjects(channel1.getIndex());
        }
        ImageHandler intensityMap = preFilter.isSelected() ? raw.getFilteredImage(channel2.getIndex()) : raw.getImage(channel2.getIndex());
        intensityMap = preFilters.runPreFilterSequence(channel2.getIndex(), intensityMap, raw, nCPUs, verbose);

        if (k_avg.isSelected()) {
            double[] values = new double[os.length];
            for (int i = 0; i < os.length; i++) {
                values[i] = os[i].getMeanPixValue(intensityMap);
            }
            quantifications.setQuantificationObjectNumber(k_avg, values);
        }
        if (k_sd.isSelected()) {
            double[] values = new double[os.length];
            for (int i = 0; i < os.length; i++) {
                values[i] = os[i].getStDevPixValue(intensityMap);
            }
            quantifications.setQuantificationObjectNumber(k_sd, values);
        }
        if (k_min.isSelected()) {
            double[] values = new double[os.length];
            for (int i = 0; i < os.length; i++) {
                values[i] = os[i].getPixMinValue(intensityMap);
            }
            quantifications.setQuantificationObjectNumber(k_min, values);
        }
        if (k_max.isSelected()) {
            double[] values = new double[os.length];
            for (int i = 0; i < os.length; i++) {
                values[i] = os[i].getPixMaxValue(intensityMap);
            }
            quantifications.setQuantificationObjectNumber(k_max, values);
        }
        if (k_id.isSelected()) {
            double[] values = new double[os.length];
            for (int i = 0; i < os.length; i++) {
                values[i] = os[i].getIntegratedDensity(intensityMap);
            }
            quantifications.setQuantificationObjectNumber(k_id, values);
        }
        
        if (k_sigma_mu.isSelected()) {
            double[] values = new double[os.length];
            for (int i = 0; i < os.length; i++) {
                values[i] = os[i].getStDevPixValue(intensityMap);
                double mu = os[i].getMeanPixValue(intensityMap);
                if (mu!=0) values[i]/=mu;
                else values[i]=Double.NaN;
            }
            quantifications.setQuantificationObjectNumber(k_sigma_mu, values);
        }

        int nbQuantiles = quantiles.getNbParameters();
        double[] qts = new double[nbQuantiles];
        int idx = 0;
        for (Parameter p : quantiles.getParameters()) {
            qts[idx++] = ((SliderDoubleParameter) p).getValue();
        }
        double[][] mes = new double[os.length][nbQuantiles];
        for (int i = 0; i < os.length; i++) {
            mes[i] = Quantiles.getQuantiles(os[i], intensityMap, qts);
        }
        for (int q = 0; q < nbQuantiles; q++) {
            if (this.keysQ[q].isSelected()) {
                double[] m = new double[os.length];
                for (int i = 0; i < os.length; i++) {
                    m[i] = mes[i][q];
                }
                quantifications.setQuantificationObjectNumber(keysQ[q], m);
            }
        }
    }

    @Override
    public Parameter[] getParameters() {
        return parameters;
    }

    @Override
    public Parameter[] getKeys() {
        if (quantiles.getNbParameters() != keysQ.length) {
            KeyParameterObjectNumber[] newKeys = new KeyParameterObjectNumber[quantiles.getNbParameters()];
            if (newKeys.length > 0) {
                if (quantiles.getNbParameters() < keysQ.length) {
                    if (keysQ.length > 0) {
                        System.arraycopy(keysQ, 0, newKeys, 0, newKeys.length);
                    }
                } else {
                    if (keysQ.length > 0) {
                        System.arraycopy(keysQ, 0, newKeys, 0, keysQ.length);
                    }
                    for (int i = keysQ.length; i < newKeys.length; i++) {
                        newKeys[i] = new KeyParameterObjectNumber("Quantile " + (i + 1) + ":", "quantile" + (i + 1), "quantile" + (i + 1), true);
                    }
                }
            }
            keysQ = newKeys;
            this.groupQ.setKeys(keysQ);
        }
        return returnKeys;
    }

    @Override
    public int getStructure() {
        if (outside.isSelected()) {
            return 0;
        } else {
            return channel1.getIndex();
        }
    }

    @Override
    public String getHelp() {
        return "Intensity Quantification inside or outside objects";
    }
}
