package tango.dataStructure;

import ij.ImagePlus;
import mcib3d.image3d.ImageHandler;

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

public abstract class StructureImages {
    
    public abstract ImageHandler getImage(int StructureIdx);

    public ImagePlus getImagePlus(int StructureIdx) {
        ImageHandler im = this.getImage(StructureIdx);
        if (im != null) {
            return im.getImagePlus();
        } else {
            return null;
        }
    }

    protected abstract boolean hasOpenedImages();
    
    protected abstract void closeAll();

    protected abstract void hideAll();
}
