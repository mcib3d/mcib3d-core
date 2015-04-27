package tango.plugin.segmenter;

import mcib3d.image3d.ImageFloat;
import mcib3d.image3d.ImageHandler;
import mcib3d.image3d.ImageInt;

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

public class WatershedTransform3DSeedConstraint extends WatershedTransform3D {
    float seedIntensityThld;
    float seedHessianThld;
    boolean useSeedHessianThld;
    float backgroundLimit;
    ImageHandler wsMap;
    public WatershedTransform3DSeedConstraint(ImageHandler wsMap, int nCPUs, boolean verbose) {
        super(nCPUs, verbose);
        this.wsMap = wsMap;
    }
    
    
    public void setThresholds(float seedIntensityThld, float backgroundLimit, boolean useSeedHessianThld, float seedHessianThld) {
        this.seedIntensityThld=seedIntensityThld;
        this.seedHessianThld=seedHessianThld;
        this.useSeedHessianThld=useSeedHessianThld;
        this.backgroundLimit=backgroundLimit;
    }
    
    @Override
    protected boolean isLocalMin(int x, int y, int z, float ws) {
        int xy = x+y*this.sizeX;
        if (this.input.getPixel(xy, z)<seedIntensityThld || (useSeedHessianThld && wsMap.getPixel(xy, z)>seedHessianThld)) return false;
        return super.isLocalMin(x, y, z, ws);
    }
    
    @Override
    protected boolean continuePropagation(Vox3D currentVox, Vox3D nextVox) {
        return (input.getPixel(nextVox.xy, nextVox.z)>=backgroundLimit);
    }
    
    public ImageInt runWatershed(ImageHandler input, ImageInt mask_) {
        return runWatershed(input, wsMap, mask_);
    }

}
