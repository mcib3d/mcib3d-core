package tango.parameter;

import mcib3d.utils.exceptionPrinter;
import ij.ImagePlus;
import ij.gui.GenericDialog;
import java.awt.event.ActionListener;
import java.util.Set;
import mcib3d.image3d.ImageHandler;
import mcib3d.image3d.ImageInt;
import tango.dataStructure.InputImages;
import tango.gui.Core;
import tango.plugin.PluginFactory;
import tango.plugin.thresholder.Thresholder;
import tango.plugin.thresholder.ThresholderHistogram;
import tango.spatialStatistics.SDIEvaluator.SDIEvaluator;
import tango.util.utils;
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
public class ThresholdHistogramParameter extends PluginParameter {

    public ThresholdHistogramParameter(String label, String id, String defMethod) {
        super(label, id, defMethod);
        initChoice();
    }
    
    public ThresholdHistogramParameter(String label, String id, String defMethod, Parameter[] defParameters) {
        super(label, id, defMethod);
        initChoice();
        setContent(currentParameters, defParameters);
    }

    @Override
    public Parameter duplicate(String newLabel, String newId) {
        return new ThresholdHistogramParameter(newLabel, newId, utils.getSelectedString(choice));
    }

    @Override
    protected void initChoice() {
        Set<String> list = PluginFactory.getThresholderHistoList();
        if (list == null) {
            return;
        }
        selecting = true;
        choice.addItem(" ");
        for (String s : list) {
            choice.addItem(s);
        }
        if (defMethod != null && defMethod.length() > 0) {
            choice.setSelectedItem(defMethod);
            majPanel();
        }
        selecting = false;
    }

    @Override
    protected void getPlugin(String method) {
        plugin = PluginFactory.getThresholder(method);
    }

    public Double getThreshold(int[] histogram, double binSize, double min) {
        if (plugin == null) {
            return null;
        }
        try {
            ThresholderHistogram t = (ThresholderHistogram) plugin;
            //t.setVerbose(verbose);
            //t.setMultithread(nCPUs);
            return t.getThreshold(histogram, binSize, min);
        } catch (Exception e) {
            exceptionPrinter.print(e, "", Core.GUIMode);
        }
        return null;
    }

    @Override
    public ThresholderHistogram getPlugin(int nCPUs, boolean verbose) {
        if (plugin!=null) return (ThresholderHistogram)super.getPlugin(nCPUs, verbose);
        else return null;
    }
    
    public void setThresholder(ThresholderHistogram t) {
        this.plugin=t;
    }

    @Override
    public void addToGenericDialog(GenericDialog gd) {
    }
}
