package tango.dataStructure;

import java.awt.Point;
import mcib3d.image3d.ImageFloat;
import mcib3d.image3d.ImageHandler;
import mcib3d.image3d.ImageInt;
import mcib3d.image3d.ImageShort;
import tango.gui.Core;

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

public abstract class InputImages extends StructureImages {
    ImageHandler[] rawImages;
    ImageHandler[] filteredImages;
    StructureContainer container;
    ImageInt mask;
    protected abstract ImageHandler getChannelFile(int fileIdx);
    public abstract ImageHandler getFilteredImage(int structureIdx);
    public void setFilteredImage(ImageHandler filtered, int structureIdx) {
        filteredImages[structureIdx]=filtered;
    }
    public void setVerbose(boolean verbose) {
        if (container!=null) container.setVerbose(verbose);
        Core.debug=verbose;
    }
    
    public boolean isOpened(int structureIdx) {
        return rawImages[structureIdx]!=null;
    }
    
    public boolean isVisible(int structureIdx) {
       return (isOpened(structureIdx) && rawImages[structureIdx].isVisible());
    }
    
    public Point getWindowPosition(int structureIdx) {
        if (isVisible(structureIdx)) return rawImages[structureIdx].getImagePlus().getWindow().getLocationOnScreen();
        else return null;
    }
    
    public void setWindowPosition(int structureIdx, Point p) {
        if (p!=null && isOpened(structureIdx)) {
            if (rawImages[structureIdx].getImagePlus()!=null && rawImages[structureIdx].getImagePlus().getWindow()!=null) {
                rawImages[structureIdx].getImagePlus().getWindow().setLocation(p);
            }
        }
    }
    
    @Override
    protected boolean hasOpenedImages() {
        for (int i = 0; i<rawImages.length; i++) if (rawImages[i]!=null) return true;
        for (int i = 0; i<filteredImages.length; i++) if (filteredImages[i]!=null) return true;
        return false;
    }
    
    @Override
    protected void closeAll() {
        for (int i = 0; i<rawImages.length; i++) {
            if (rawImages[i]!=null) {
                rawImages[i].flush();
                rawImages[i]=null;
            }
        }
        for (int i = 0; i<filteredImages.length; i++) {
            if (filteredImages[i]!=null) {
                filteredImages[i].flush();
                filteredImages[i]=null;
            }
        }
    }
    
    @Override
    protected void hideAll() {
        for (int i = 0; i<rawImages.length; i++) {
            if (rawImages[i]!=null) {
                rawImages[i].hide();
            }
        }
        for (int i = 0; i<filteredImages.length; i++) {
            if (filteredImages[i]!=null) {
                filteredImages[i].hide();
            }
        }
    }
    public abstract ImageInt getMask();
    
}
