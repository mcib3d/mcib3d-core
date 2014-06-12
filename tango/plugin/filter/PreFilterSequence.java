package tango.plugin.filter;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import tango.dataStructure.Experiment;
import mcib3d.utils.exceptionPrinter;
import ij.IJ;
import ij.ImagePlus;
import mcib3d.image3d.ImageHandler;
import java.util.ArrayList;
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
public class PreFilterSequence {
    // TODO pour alleger la memoire : les prefilters prennent en argument une image tampon vide
    ArrayList<PreFilter> filters;
    
    public PreFilterSequence(BasicDBObject settings, int nbCPUs, boolean verbose) {
        if (settings==null || !settings.containsField("preFilters")) return;
        BasicDBList prefilters = (BasicDBList)settings.get("preFilters");
        filters = new ArrayList<PreFilter>(prefilters.size());
        for (int i = 0; i<prefilters.size(); i++) {
            //IJ.log(i+"");
            Object o = prefilters.get(i);
            if (o!=null) {
                BasicDBObject data = (BasicDBObject) o;
                //IJ.log("preFilter:"+data);
                PreFilter f = PluginFactory.getPreFilter(data.getString("method"));
                if (f!=null) {
                    //IJ.log("preFilter loaded:"+f.getId());
                    Parameter[] parameters=f.getParameters();
                    for (Parameter p : parameters) p.dbGet(data);
                    filters.add(f);
                    f.setMultithread(nbCPUs);
                    f.setVerbose(verbose);
                }
            }
        }
    }
    
    
    public ImageHandler run(int currentStructureIdx, ImageHandler in, InputImages images ) {
        ImageHandler currentImage=in;
        if (isEmpty()) return in;
        while(!filters.isEmpty()) {
            
            currentImage=filters.remove(0).runPreFilter(currentStructureIdx, currentImage, images);
            
            currentImage.setScale(in);
            currentImage.setOffset(in);
        }
        currentImage.setTitle(in.getTitle()+"::preFiltered");
        return currentImage;
    }
    
    public void test(int currentStructureIdx, ImageHandler in, InputImages images, int step, boolean onlyStep) {
        ImageHandler currentImage=in;
        if (isEmpty() || step>filters.size()) return;
        int idx = 0;
        while(!filters.isEmpty()) {
            PreFilter f = filters.remove(0);
            if (step==idx) {
                if (!onlyStep) currentImage.showDuplicate("Image Before selected Step");
                images.setVerbose(true);
                f.setVerbose(true);
            } else {
                if (onlyStep) {
                    idx++;
                    continue;
                }
                images.setVerbose(false);
                f.setVerbose(false);
            }
            
            currentImage=f.runPreFilter(currentStructureIdx, currentImage, images);
            currentImage.setScale(in);
            currentImage.setOffset(in);
            if (step==idx) {
                currentImage.showDuplicate("Image After selected Step");
                images.setVerbose(false);
                return;
            }
            idx++;
        }
    }

    
    public boolean isEmpty() {
        return (filters==null || filters.isEmpty());
    }
}