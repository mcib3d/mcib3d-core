package tango.parameter;

import mcib3d.utils.exceptionPrinter;
import ij.ImagePlus;
import ij.gui.GenericDialog;
import java.awt.event.ActionListener;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;
import mcib3d.image3d.ImageHandler;
import mcib3d.image3d.ImageInt;
import mcib3d.image3d.ImageStats;
import tango.dataStructure.InputImages;
import tango.gui.Core;
import tango.plugin.PluginFactory;
import tango.plugin.TangoPlugin;
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
public class ThresholdParameter extends PluginParameter {

    public ThresholdParameter(String label, String id, String defMethod) {
        super(label, id, defMethod);
        initChoice();
    }
    
    public ThresholdParameter(String label, String id, String defMethod, Parameter[] defParameters) {
        super(label, id, defMethod);
        initChoice();
        setContent(currentParameters, defParameters);
    }

    @Override
    public Parameter duplicate(String newLabel, String newId) {
        return new ThresholdParameter(newLabel, newId, utils.getSelectedString(choice));
    }

    @Override
    protected void initChoice() {
        Set<String> list1 = PluginFactory.getThresholdersList();
        Set<String> list2 = PluginFactory.getThresholderHistoList();
        Set<String> list = new TreeSet<String>();
        list.addAll(list1);
        list.addAll(list2);
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
        if (plugin==null) plugin = PluginFactory.getThresholderHisto(method);
    }

    public Double getThreshold(ImageHandler in, InputImages images, int nCPUs, boolean verbose) {
        if (plugin == null) {
            return null;
        }
        try {
            if (Thresholder.class.isAssignableFrom(plugin.getClass())) {
                Thresholder t = (Thresholder) plugin;
                t.setVerbose(verbose);
                t.setMultithread(nCPUs);
                return t.runThresholder(in, images);
            } else if (ThresholderHistogram.class.isAssignableFrom(plugin.getClass())) {
                ThresholderHistogram t = (ThresholderHistogram) plugin;
                t.setVerbose(verbose);
                t.setMultithread(nCPUs);
                ImageStats stats = in.getImageStats(images.getMask());
                return t.getThreshold(stats.getHisto256(), stats.getHisto256BinSize(), stats.getMin());
            }
        } catch (Exception e) {
            exceptionPrinter.print(e, "", Core.GUIMode);
        }
        return null;
    }

    /*@Override
    public Thresholder getPlugin(int nCPUs, boolean verbose) {
        if (plugin!=null) return (Thresholder)super.getPlugin(nCPUs, verbose);
        else return null;
    }
    * 
    */
    
    public void setThresholder(TangoPlugin t) {
        if (t==null) plugin = null;
        else if (Thresholder.class.isAssignableFrom(t.getClass()) || ThresholderHistogram.class.isAssignableFrom(t.getClass())) this.plugin=t;
    }

    @Override
    public void addToGenericDialog(GenericDialog gd) {
    }
}
