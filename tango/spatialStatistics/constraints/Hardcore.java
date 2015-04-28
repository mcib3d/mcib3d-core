package tango.spatialStatistics.constraints;

import ij.ImagePlus;
import java.util.ArrayList;
import java.util.Arrays;
import mcib3d.geom.Object3D;
import mcib3d.geom.Point3D;
import tango.dataStructure.InputCellImages;
import tango.dataStructure.SegmentedCellImages;
import tango.parameter.DoubleParameter;
import tango.parameter.Parameter;
import tango.parameter.StructureParameter;
import tango.spatialStatistics.StochasticProcess.RandomPoint3DGenerator;
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
public class Hardcore extends Constraint {
    
    double[] constraint;
    DoubleParameter distance = new DoubleParameter("Hardcore Radius (unit):", "distance", 0.15d, Parameter.nfDEC5);
    StructureParameter refObjects = new StructureParameter("Use Mean Radius of Objects:", "objects", -1, true);
    Parameter[] parameters = new Parameter[] {distance, refObjects};
    int currentIndex;
    KDTreeC kdTree;
    @Override
    public Parameter[] getParameters() {
        return parameters;
    }

    @Override
    public void initialize(RandomPoint3DGenerator rpg, InputCellImages raw, SegmentedCellImages seg) {
        this.rpg=rpg;
        kdTree = new KDTreeC(3, rpg.nbPoints);
        kdTree.setScaleSq(new double[] {rpg.resXY*rpg.resXY, rpg.resXY*rpg.resXY, rpg.resZ*rpg.resZ});
        if (refObjects.getIndex()>=0) {
            Object3D[] referenceObjects = refObjects.getObjects(seg);
            constraint = new double[referenceObjects.length];
            for (int i = 0; i<constraint.length; i++) {
                constraint[i]=referenceObjects[i].getDistCenterMean();
            }
        } else {
            constraint = new double[rpg.nbPoints];
            Arrays.fill(constraint, distance.getDoubleValue(0.15d));
        }
        currentIndex=0;
    }

    @Override
    public ArrayList<Integer> getVoxelIndexes() {
        return null;
    }

    @Override
    public boolean eval(double x, double y, double z, double res) {
        if (rpg.pointIndex==0) return true;
        else if (rpg.pointIndex>currentIndex) {
            kdTree.add(new double[] {rpg.points[currentIndex].getX(), rpg.points[currentIndex].getY(), rpg.points[currentIndex].getZ()}, currentIndex);
            currentIndex++;
        }
        KDTreeC.Item[] items = kdTree.getNearestNeighbor(new double[]{x, y, z}, 1);
        if (Math.sqrt(items[0].distance)<(constraint[currentIndex]+constraint[(Integer)items[0].obj])) return false;
        return true;
    }

    @Override
    public void reset() {
        kdTree = new KDTreeC(3, rpg.nbPoints);
        kdTree.setScaleSq(new double[] {rpg.resXY*rpg.resXY, rpg.resXY*rpg.resXY, rpg.resZ*rpg.resZ});
        currentIndex=0;
    }

    @Override
    public boolean isValid() {
        return rpg.nbPoints<=constraint.length;
    }

    @Override
    public String getHelp() {
        return "";
    }
    
}
