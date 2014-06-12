package tango.plugin.filter.mergeRegions;

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

public class ImageCalibrations {
    public int sizeX, sizeY, sizeZ, limX, limY, limZ;
    double resXY, resZ, aXY, aXZ;
    public ImageCalibrations(int sizeX, int sizeY, int sizeZ, double resXY, double resZ) {
        init(sizeX, sizeY, sizeZ, resXY, resZ);
    }
    public ImageCalibrations(ImageHandler image) {
        init(image.sizeX, image.sizeY, image.sizeZ, image.getScaleXY(), image.getScaleZ());
    }
    
    private void init(int sizeX, int sizeY, int sizeZ, double resXY, double resZ) {
        this.sizeX=sizeX;
        this.sizeY=sizeY;
        this.sizeZ=sizeZ;
        limX=this.sizeX-1;
        limY=this.sizeY-1;
        limZ=this.sizeZ-1;
        this.resXY=resXY;
        this.resZ=resZ;
        aXY=(float)(resXY*resXY);
        aXZ=(float)(resXY*resZ);
    }
}
