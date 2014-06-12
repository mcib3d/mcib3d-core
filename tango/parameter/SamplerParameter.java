package tango.parameter;

import java.awt.*;
import java.util.*;
import java.io.*;
import javax.swing.*;
import java.text.*;
import com.mongodb.DBObject;
import com.mongodb.BasicDBObject;
import ij.ImagePlus;
import mcib3d.geom.Object3D;
import mcib3d.geom.Object3DPoint;
import mcib3d.geom.Point3D;
import tango.dataStructure.InputCellImages;
import tango.dataStructure.SegmentedCellImages;
import tango.gui.Core;
import tango.plugin.PluginFactory;
import tango.plugin.sampler.Sampler;
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
public class SamplerParameter extends StructureParameter {
    protected static String[] sampleChannels;
    protected Sampler sampler;
    public SamplerParameter(String label, String id, int defaultValue) {
        super (label, id, defaultValue);
    }
    
    @Override
    protected void addItems(int idx) {
        choice.addItem("");
        if (sampleChannels!=null) {
            for (String s : sampleChannels) choice.addItem(s);
            if (idx>=-1 && idx<=sampleChannels.length) choice.setSelectedIndex(idx+1);
        }
    }
    
    @Override
    public void refresh() {
        int idx = getIndex();
        choice.removeAllItems();
        addItems(idx);
    }
    
    @Override
    public Parameter duplicate(String newLabel, String newId) {
        return new SamplerParameter(newLabel, newId, choice.getSelectedIndex());
    }
    
    
    public static void setChannels(String[] channels) {
        SamplerParameter.sampleChannels=channels;
    }
    
    public void initSampler(InputCellImages raw, SegmentedCellImages seg, int nbCPUs, boolean verbose) {
        int sampleIdx = this.getIndex();
        if (sampleIdx>=0) {
            BasicDBObject settings = (BasicDBObject)Core.getExperiment().getSampleChannels().get(sampleIdx);
            sampler = PluginFactory.getSampler(settings.getString("method"));
            if (sampler != null) {
                for (Parameter p : sampler.getParameters()) p.dbGet(settings);
                sampler.initSampler(raw, seg);
                sampler.setMultithread(nbCPUs);
                sampler.setVerbose(verbose);
            }
        }
    }
    
    public Object3D[] getSample() {
        if (sampler!=null) {
            return sampler.getSample();
        } else return null;
    }
    
}
