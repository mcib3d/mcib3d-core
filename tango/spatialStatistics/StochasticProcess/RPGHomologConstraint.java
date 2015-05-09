package tango.spatialStatistics.StochasticProcess;

import mcib3d.geom.Point3D;
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
public class RPGHomologConstraint extends RandomPoint3DGenerator {
    float constraint;
    Point3D firstPoint;
    
    public RPGHomologConstraint(ImageInt mask, int maxSize, float constraint, int nCPUs, boolean verbose) {
        super (mask, maxSize, nCPUs, verbose);
        this.constraint=(float)(constraint/resXY);
    }
    
    @Override
    protected Point3D drawPoint3D() {
        if (pointIndex==1) {
        double theta=randomGenerator.nextFloat()*Math.PI*2;
        double phi=randomGenerator.nextFloat()*Math.PI;
        Point3D p = new Point3D(firstPoint.getX()+Math.cos(theta)*Math.sin(phi)*constraint, firstPoint.getY()+Math.sin(theta)*Math.sin(phi)*constraint, firstPoint.getZ()+Math.cos(phi)*constraint/scale);
        if (mask.maskContains(p.getRoundX(), p.getRoundY(), p.getRoundZ())) return p;
        else return null;
        } else {
            firstPoint = drawPoint3DUniform();
            return firstPoint;
        }
    }
    
    @Override
    public boolean isValid() {
        return nbPoints==2;
    }
    
}
