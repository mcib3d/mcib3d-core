package tango.plugin.segmenter;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import mcib3d.utils.exceptionPrinter;
import ij.ImagePlus;
import mcib3d.image3d.ImageFloat;
import mcib3d.image3d.ImageHandler;
import mcib3d.image3d.ImageInt;
import java.util.ArrayList;
import tango.dataStructure.InputCellImages;
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
public class SpotSegmenterRunner {
    SpotSegmenter segmenter;
    public SpotSegmenterRunner(BasicDBObject type, int nbCPUs, boolean verbose) {
        if (type==null) return;
        Object o = type.get("segmentation");
        if (o!=null) {
            BasicDBObject data = (BasicDBObject) o;
            segmenter = PluginFactory.getSpotSegmenter(data.getString("method"));
            if (segmenter!=null) {
                Parameter[] parameters=segmenter.getParameters();
                for (Parameter p : parameters) p.dbGet(data);
                segmenter.setMultithread(nbCPUs);
                segmenter.setVerbose(verbose);
            }
        }
    }
    
    
    public ImageInt run(int currentStructureIdx, ImageHandler in, InputCellImages images) {
        try {
            ImageInt res = segmenter.runSpot(currentStructureIdx, in, images);
            if (res!=null) {
                res.setScale(in);
                res.setOffset(in);
                res.setTitle(in.getTitle()+"::segmented");
                return res;
            }
        }catch (Exception e) {
            exceptionPrinter.print(e, "", Core.GUIMode);
        }
        return null;
    }
    
    public ImageFloat getProbabilityMap() {
        try {
            return segmenter.getProbabilityMap();
        }catch (Exception e) {
            exceptionPrinter.print(e, "", Core.GUIMode);
        } return null;
    }
    
    public boolean isEmpty() {
        return (segmenter==null);
    }
}