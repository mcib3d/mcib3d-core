package tango.spatialStatistics.StochasticProcess;

import ij.IJ;
import mcib3d.image3d.ImageHandler;
import java.util.*;
import java.util.Map.Entry;
import mcib3d.geom.Point3D;
import mcib3d.image3d.ImageInt;
import tango.spatialStatistics.util.KDTreeC;

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
public class RandomPoint3DGeneratorUniform extends RandomPoint3DGenerator {
    
    public RandomPoint3DGeneratorUniform(ImageInt mask, int maxSize, int nbCPUs, boolean verbose) {
        super(mask, maxSize, nbCPUs, verbose);
    }
    
    @Override
    protected Point3D drawPoint3D() {
        return drawPoint3DUniform();
    }
    @Override
    public boolean isValid() {
        return true;
    }
    
}
