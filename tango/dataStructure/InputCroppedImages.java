package tango.dataStructure;

import ij.IJ;
import ij.ImagePlus;
import mcib3d.image3d.BlankMask;
import mcib3d.image3d.ImageHandler;
import mcib3d.image3d.ImageInt;
import tango.dataStructure.Field;
import tango.parameter.StructureParameter;

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
public class InputCroppedImages extends InputImages {

    protected InputImages input;
    protected int[] bounds;
    int label;
    int volume; // nb of voxel of current label in input image
    boolean filterCroppedImage;
    public InputCroppedImages(InputImages input, ImageInt mask, int label, int[] bounds_, int border, boolean allowNegativeBounds, boolean includeBackgroundInMask) {
        this.input=input;
        if (input!=null) this.container=input.container;
        if (input!=null) this.rawImages = new ImageHandler[input.rawImages.length];
        if (input!=null) filteredImages = new ImageHandler[input.filteredImages.length];
        this.bounds=bounds_;
        this.label=label;
        if (allowNegativeBounds) {
            for (int i = 0; i<3; i++) {
                this.bounds[2*i]-=border;
                this.bounds[2*i+1]+=border;
            }
        } else {
            /*for (int i = 0; i<3; i++) {
                int dLeft = (bounds[2*i]-border);
                boolean left = dLeft>=0;
                int dRight;
                if (i==0) dRight= (mask.sizeX - 1 - (bounds[2*i+1]+border));
                else if (i==1) dRight= (mask.sizeY - 1 - (bounds[2*i+1]+border));
                else dRight= (mask.sizeZ - 1 - (bounds[2*i+1]+border));
                boolean right = dRight>0;
                if (left && right) {
                    bounds[2*i]-=border;
                    bounds[2*i+1]+=border;
                } else if (left && !right) {
                    bounds[2*i]-=border;
                    if ((dLeft-border)>=0) bounds[2*i]-=border;
                } else if (!left && right) {
                    bounds[2*i+1]+=border;
                    if ((dRight-border)>0) bounds[2*i+1]+=border;
                }
            }
            * 
            */
            for (int i = 0; i<3; i++) {
                int limit=mask.sizeX;
                if (i==1) limit=mask.sizeY;
                else if (i==2) limit=mask.sizeZ;
                limit--;
                int dLeft = 0, dRight=0;
                bounds[2*i]-=border;
                if (bounds[2*i]<0) {
                    dLeft = -bounds[2*i];
                    bounds[2*i]=0;
                }
                bounds[2*i+1]+=border;
                if (bounds[2*i+1]>limit) {
                    dLeft = bounds[2*i+1]-limit;
                    bounds[2*i+1]=limit;
                }
                // compensate on the other side to have constant border:
                if (dLeft>0) {
                    bounds[2*i+1]+=dLeft;
                    if (bounds[2*i+1]>limit) bounds[2*i+1]=limit;
                } else if (dRight>0) {
                    bounds[2*i]-=dRight;
                    if (bounds[2*i]<0) bounds[2*i]=0;
                }
                
            }
        }
        this.mask=mask.crop3D(mask.getTitle()+"::cropped::"+label, bounds[0], bounds[1], bounds[2], bounds[3], bounds[4], bounds[5]);
        //ij.IJ.log("mask:"+mask.getTitle()+ " label:"+label+ " boundX:"+bounds[0]+ " initial mask offsetX:"+mask.offsetX+ " cropped mask offsetX:"+this.mask.offsetX);
        int val;
        volume=0;
        int bckLabel = (label<65535)? label+1 : label-1;
        
        for (int z = 0; z<this.mask.sizeZ; z++) {
            for (int xy = 0; xy<this.mask.sizeXY; xy++) {
                val = this.mask.getPixelInt(xy, z);
                if (val == 0) {
                    if (includeBackgroundInMask) this.mask.setPixel(xy, z, bckLabel);
                }
                else if (val!=label) this.mask.setPixel(xy, z, 0);
                else volume++;
            }
        }
        //this.mask.showDuplicate("cropped mask for:"+mask.getTitle());
    }
    
    public ImageHandler crop(ImageHandler input) {
        return input.crop3D(input.getTitle()+"::cropped"+label, bounds[0], bounds[1], bounds[2], bounds[3], bounds[4], bounds[5]);
    }
    
    public ImageInt getMaskNoBackground() {
        ImageInt res = this.mask.duplicate();
        int val;
        for (int z = 0; z<this.mask.sizeZ; z++) {
            for (int xy = 0; xy<this.mask.sizeXY; xy++) {
                val = res.getPixelInt(xy, z);
                if (val != label) {
                    res.setPixel(xy, z, 0);
                }
            }
        }
        return res;
    }
    
    public int getVolume() {
        return volume;
    }
    
    public void setFilterCroppedImage(boolean filterCroppedImage) {
        this.filterCroppedImage=filterCroppedImage;
    }
   
    @Override
    public synchronized ImageHandler getImage(int structureIdx) {
        if (structureIdx < 0) {
            return null;
        }
        return getChannelFile(input.container.getFileRank(structureIdx));
    }

    @Override
    public synchronized ImageHandler getFilteredImage(int structureIdx) {
        if (structureIdx<0) return null;
        if (structureIdx<filteredImages.length) {
            if (filteredImages[structureIdx]==null || !filteredImages[structureIdx].isOpened()) {
                if (filterCroppedImage) {
                    filteredImages[structureIdx]=input.container.preFilterStructure(getImage(structureIdx), structureIdx);
                } else {
                    ImageHandler in = input.getFilteredImage(structureIdx);
                    filteredImages[structureIdx]=in.crop3D(in.getTitle()+"::cropped"+label, bounds[0], bounds[1], bounds[2], bounds[3], bounds[4], bounds[5]);
                }
            }
            return filteredImages[structureIdx];
        }
        return getImage(structureIdx);
        
    }

    @Override
    protected synchronized ImageHandler getChannelFile(int fileIdx) {
        if (rawImages[fileIdx] == null || !rawImages[fileIdx].isOpened()) {
            ImageHandler in = input.getChannelFile(fileIdx);
            rawImages[fileIdx] = in.crop3D(in.getTitle()+"::cropped"+label, bounds[0], bounds[1], bounds[2], bounds[3], bounds[4], bounds[5]);
        }
        return rawImages[fileIdx];
    }
    

    @Override
    public synchronized ImageInt getMask() {
        return mask;
    }
    
}
