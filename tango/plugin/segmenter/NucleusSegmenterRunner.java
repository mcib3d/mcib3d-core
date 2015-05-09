package tango.plugin.segmenter;

import com.mongodb.BasicDBObject;
import ij.ImagePlus;
import mcib3d.image3d.ImageHandler;
import mcib3d.image3d.ImageInt;
import tango.dataStructure.InputFieldImages;
import tango.dataStructure.InputImages;
import tango.gui.Core;
import tango.parameter.Parameter;
import tango.plugin.PluginFactory;
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
public class NucleusSegmenterRunner {
    NucleusSegmenter segmenter;
    
    public NucleusSegmenterRunner(BasicDBObject settings, int nbCPUs, boolean verbose) {
        if (settings==null) return;
        Object o = settings.get("segmentation");
        if (o!=null) {
            BasicDBObject data = (BasicDBObject) o;
            
            segmenter = PluginFactory.getNucleiSegmenter(data.getString("method"));
            if (segmenter!=null) {
                Parameter[] parameters=segmenter.getParameters();
                for (Parameter p : parameters) p.dbGet(data);
                segmenter.setMultithread(nbCPUs);
                segmenter.setVerbose(verbose);
            }
        }
    }
    
    
    public ImageInt run(int currentStructureIdx, ImageHandler in, InputImages rawImages) {
        if (segmenter==null) {
            if (Core.GUIMode) ij.IJ.log("no nucleus segmentation found");
            return null;
        }
        ImageInt mask = segmenter.runNucleus(currentStructureIdx, in, rawImages);
        mask.setTitle(in.getTitle()+"::segmented");
        mask.setScale(in);
        mask.setOffset(in);
        return mask;
    }
    
//    public int[] getTags() {
//        return segmenter.getTags();
//    }
    
    
    public boolean isEmpty() {
        return (segmenter==null);
    }
}