package tango.plugin.segmenter;

import java.util.ArrayList;
import java.util.Map.Entry;
import java.util.TreeMap;
import mcib3d.image3d.ImageFloat;
import mcib3d.image3d.ImageHandler;
import mcib3d.image3d.ImageInt;
import mcib3d.image3d.ImageShort;
import tango.dataStructure.InputCroppedImages;

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

public class WatershedEdgeDetection extends WatershedTransform3D {
    float seedIntensityThld;
    float seedHessianThld;
    boolean useSeedHessianThld;
    float backgroundLimit;
    int label;
    short labelSpot;
    public WatershedEdgeDetection(int nCPUs, boolean verbose) {
        super(nCPUs, verbose);
    }
    
    @Override
    protected boolean isLocalMin(int x, int y, int z, float ws) {
        int xy = x+y*this.sizeX;
        if (this.mask.getPixel(xy, z)==label) return false;
        return super.isLocalMin(x, y, z, ws);
    }
    
    @Override
    protected void getRegionalMinima() {
        // gets regional minima outside spot within mask
        super.getRegionalMinima();
        float max = -Float.MAX_VALUE;
        int zMin=0, xyMin=0;
        for (int z = 0; z<mask.sizeZ;z++) {
            for (int xy = 0; xy<mask.sizeXY; xy++) {
                if (mask.getPixelInt(xy, z)==label) {
                    if (this.input.getPixel(xy, z)>max) {
                       zMin=z;
                       xyMin=xy;
                       max=input.getPixel(xy, z);
                    }
                }
            }
        }
        
        labelSpot  = 1;
        for (short key : spots.keySet()) if (key>labelSpot) labelSpot=key;
        labelSpot++;
        spots.put(labelSpot, new Spot3D(labelSpot, new Vox3D(xyMin, zMin)));
    }
    
    // mask contains on label
    public ImageInt runWatershed(ImageHandler input, ImageInt mask_, int label) {
        ImageHandler wsMap = input.getGradient(1, nCPUs);
        if (debug) wsMap.showDuplicate("Gradient Map");
        this.label=label;
        runWatershed(input, wsMap, mask_);
        
        //erase all spots but spot contained in label
        for (Spot3D s : spots.values()) {
            if (s.label!=labelSpot) s.setLabel((short)0);
        }
        if (debug) segmentedMap.showDuplicate("Segmented Map after erase");
        return segmentedMap;
    }

    public static ImageInt edgeDetection(ImageInt input, ImageHandler intensityMap, int nCPUS, boolean verbose) {
        TreeMap<Integer, int[]> bounds = input.getBounds(false);
        ArrayList<Integer> labels = new ArrayList<Integer>(bounds.keySet());
        ArrayList<ImageInt> postFilteredImages = new ArrayList<ImageInt>(labels.size());
        
        int b = 4;
        for (int label : bounds.keySet()) {
            InputCroppedImages ici=new InputCroppedImages(null, input, label, bounds.get(label), b, false, true);
            ImageInt croppedMask = ici.getMask();
            if (verbose) croppedMask.showDuplicate("cropped mask:: label:"+label);
            if (croppedMask.sizeX<=1 || croppedMask.sizeY<=1) continue; // FIXME case of 2D segmentation: allow thin objects...
            WatershedEdgeDetection wsas = new WatershedEdgeDetection(nCPUS, verbose);
            ImageInt segImage =  wsas.runWatershed(ici.crop(intensityMap), ici.getMask(), label);
            segImage.setScale(croppedMask);
            segImage.setOffset(croppedMask);
            postFilteredImages.add(segImage);
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
    
}
