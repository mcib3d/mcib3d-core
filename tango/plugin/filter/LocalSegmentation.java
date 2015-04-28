package tango.plugin.filter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.TreeMap;
import mcib3d.image3d.ImageHandler;
import mcib3d.image3d.ImageInt;
import mcib3d.image3d.ImageShort;
import tango.dataStructure.InputCroppedImages;
import tango.dataStructure.InputImages;
import tango.parameter.*;

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
public class LocalSegmentation implements PostFilter {
    
    boolean debug;
    int nbCPUs=1;
    IntParameter border = new IntParameter("Border:", "border", 5);
    SegmenterParameter segmenter = new SegmenterParameter("Segmentation Method", "segmenter", "Watershed3D");
    PostFilterSequenceParameter postFilters = new PostFilterSequenceParameter("Post-Filters", "postFilters");
    Parameter[] parameters = new Parameter[] {border, segmenter, postFilters};
    
    
    public LocalSegmentation() {
         
    }
    
    @Override
    public ImageInt runPostFilter(int currentStructureIdx, ImageInt input, InputImages images) {
        TreeMap<Integer, int[]> bounds = input.getBounds(false);
        ArrayList<Integer> labels = new ArrayList<Integer>(bounds.keySet());
        ArrayList<ImageInt> postFilteredImages = new ArrayList<ImageInt>(labels.size());
        
        int b = border.getIntValue(5);
        for (int label : bounds.keySet()) {
            InputCroppedImages ici=new InputCroppedImages(images, input, label, bounds.get(label), b, false, true);
            ImageInt croppedMask = ici.getMask();
            if (croppedMask.sizeX<=1 || croppedMask.sizeY<=1) continue; // FIXME case of 2D segmentation: allow thin objects...
            ImageInt segImage = segmenter.runSegmenter(currentStructureIdx, ici.getFilteredImage(currentStructureIdx), ici, nbCPUs, debug);
            segImage.setScale(croppedMask);
            segImage.setOffset(croppedMask);
            postFilteredImages.add(postFilters.runPostFilterSequence(currentStructureIdx, segImage, ici, nbCPUs, debug));
        }
        if (input instanceof ImageShort) {
            input.erase();
            ((ImageShort)input).appendMasks(postFilteredImages, 1);
            return input;
        } else {
            ImageShort res = new ImageShort(input.getTitle(), input.sizeX, input.sizeY, input.sizeZ);
            res.appendMasks(postFilteredImages, 1);
            return res;
        }
    }

    @Override
    public String getHelp() {
        return "Runs the selected segmenter and post-filters locally around each semgented object. A border can be set if objects might become larger. When an object is processed, other objects are excluded from mask";
    }

    @Override
    public void setVerbose(boolean debug) {
        this.debug=debug;
    }

    @Override
    public void setMultithread(int nbCPUs) {
        this.nbCPUs=nbCPUs;
    }

    @Override
    public Parameter[] getParameters() {
        return parameters;
    }
    
   
    
}
