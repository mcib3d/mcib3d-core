package tango.dataStructure;

import mcib3d.image3d.ImageHandler;
import mcib3d.image3d.ImageInt;
import tango.dataStructure.Cell;

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

public class InputCellImages extends InputImages {
    public InputCellImages(Cell cell) {
        this.container=cell;
        rawImages= new ImageHandler[cell.xp.getNBFiles()];
        filteredImages= new ImageHandler[cell.getNbStructures(false)];
    }
    
    
    //simple constructor for tests .. will throws exception if other images than mask needed..
    public InputCellImages(ImageInt mask) {
        this.mask=mask;
    }
    
    
    @Override
    public synchronized ImageHandler getImage(int structureIdx) {
        if (structureIdx<0) return null;
        return getChannelFile(container.getFileRank(structureIdx));
    }
    
    @Override
    public synchronized ImageHandler getFilteredImage(int structureIdx) {
        if (structureIdx<0) return null;
        if (structureIdx>=filteredImages.length || structureIdx==0) return getImage(structureIdx);
        if (filteredImages[structureIdx]==null || !filteredImages[structureIdx].isOpened()) {
            filteredImages[structureIdx]=container.preFilterStructure(getImage(structureIdx), structureIdx); //calls setFilterImage
        }
        return filteredImages[structureIdx];
    }
    
    @Override
    public synchronized ImageInt getMask() {
        if (mask==null || !mask.isOpened()) mask = container.getMask();
        return mask;
    }
    
    public synchronized ImageInt getGlobalNucleusMask() {
        return ((Cell)container).getField().getSegmented();
    }
    
    public synchronized Field getAssociatedField() {
        return ((Cell)container).getField();
    }
    
    

    @Override
    protected synchronized ImageHandler getChannelFile(int idx) {
        if (rawImages[idx]==null || !rawImages[idx].isOpened()) rawImages[idx]=container.openInputImage(idx);
        return rawImages[idx];
    }
    
}
