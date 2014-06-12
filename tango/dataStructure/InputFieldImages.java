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
public class InputFieldImages extends InputImages {

    protected InputFieldImages(Field f) {
        this.container = f;
        this.rawImages = new ImageHandler[f.getExperiment().getNBFiles()];
        filteredImages = new ImageHandler[f.getExperiment().getNBStructures(false)];
    }

    //minimal constructor for testing.. field == null
    public InputFieldImages(ImageHandler nucleusRawImage) {
        this.rawImages = new ImageHandler[1];
        rawImages[0] = nucleusRawImage;
        this.filteredImages = new ImageHandler[1];
        mask = new BlankMask(nucleusRawImage);
    }

    @Override
    public synchronized ImageHandler getImage(int structureIdx) {
        if (structureIdx < 0) {
            return null;
        }
        if (container == null) {
            if (structureIdx == 0) {
                return rawImages[0];
            } else {
                return null;
            }
        }
        return getChannelFile(container.getFileRank(structureIdx));
    }

    @Override
    public synchronized ImageHandler getFilteredImage(int structureIdx) {
        if (structureIdx == 0) {
            if (filteredImages[0] == null) {
                if (container!=null) filteredImages[0]=container.preFilterStructure(getImage(0), 0);
                else return getImage(structureIdx); // testing
            }
            return filteredImages[0];
        } else {
            return getImage(structureIdx);
        }
    }

    @Override
    protected synchronized ImageHandler getChannelFile(int fileIdx) {
        if (rawImages[fileIdx] == null || !rawImages[fileIdx].isOpened()) {
            rawImages[fileIdx] = container.openInputImage(fileIdx);
        }
        return rawImages[fileIdx];
    }

    @Override
    public synchronized ImageInt getMask() {
        if (mask==null) mask = container.getMask();
        return mask;
    }
}
