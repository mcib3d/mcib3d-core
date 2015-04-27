package tango.spatialStatistics.constraints;

import ij.ImagePlus;
import java.util.ArrayList;
import mcib3d.geom.Object3D;
import mcib3d.geom.Point3D;
import tango.dataStructure.InputCellImages;
import tango.dataStructure.SegmentedCellImages;
import tango.parameter.Parameter;
import tango.parameter.StructureParameter;
import tango.spatialStatistics.StochasticProcess.RandomPoint3DGenerator;
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
public class HomologDistance extends Constraint {
    StructureParameter referenceObjects_P = new StructureParameter("Reference Objects:", "objects", 0, true);
    Parameter[] parameters = new Parameter[] {referenceObjects_P};
    
    double distanceSq;
    double refX, refY, refZ;
    double resXYSq, resZSq;
    public HomologDistance() {
        //super();
    }

    @Override
    public ArrayList<Integer> getVoxelIndexes() {
        return null;
    }

    @Override
    public boolean eval(double x, double y, double z, double res) {
        if (rpg.pointIndex==0) return true;
        else {
            if (Double.isNaN(refX)) {
                refX=rpg.points[0].getX();
                refY= rpg.points[0].getY();
                refZ=rpg.points[0].getZ();
            }
            return Math.abs((x-refX)*(x-refX)*resXYSq+ (y-refY)*(y-refY)*resXYSq + (z-refZ)*(z-refZ)*resZSq-distanceSq)<=res;
        }
    }


    @Override
    public void reset() {
        refX=Double.NaN;
    }

    @Override
    public Parameter[] getParameters() {
        return parameters;
    }

    @Override
    public void initialize(RandomPoint3DGenerator rpg, InputCellImages raw, SegmentedCellImages seg) {
        this.rpg=rpg;
        this.resXYSq=rpg.resXY*rpg.resXY;
        this.resZSq=rpg.resZ*rpg.resZ;
        this.refX=Double.NaN;
        Object3D[] referenceObjects = referenceObjects_P.getObjects(seg);
        distanceSq = (referenceObjects.length>1) ? referenceObjects[0].distCenterUnit(referenceObjects[1]) : 0;
        distanceSq=distanceSq*distanceSq;
    }

    @Override
    public boolean isValid() {
        return rpg.nbPoints==2;
    }

    @Override
    public String getHelp() {
        return "";
    }
    
    
}
