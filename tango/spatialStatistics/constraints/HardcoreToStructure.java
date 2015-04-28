package tango.spatialStatistics.constraints;

import ij.ImagePlus;
import java.util.ArrayList;
import java.util.Arrays;
import mcib3d.geom.Object3D;
import mcib3d.geom.Point3D;
import mcib3d.image3d.ImageFloat;
import mcib3d.image3d.ImageHandler;
import mcib3d.image3d.ImageInt;
import mcib3d.image3d.distanceMap3d.EDT;
import mcib3d.image3d.distanceMap3d.EDT;
import tango.dataStructure.AbstractStructure;
import tango.dataStructure.InputCellImages;
import tango.dataStructure.SegmentedCellImages;
import tango.gui.Core;
import tango.parameter.BooleanParameter;
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
public class HardcoreToStructure extends Constraint {
    
    double[] constraint;
    DoubleParameter distance = new DoubleParameter("Hardcore Radius (unit):", "distance", 0.15d, Parameter.nfDEC5);
    StructureParameter refObjects = new StructureParameter("Use Mean Radius of Objects:", "objects", -1, true);
    StructureParameter structure = new StructureParameter("Distance to Structure:", "structure", 0, true);
    BooleanParameter inside = new BooleanParameter("Distance Inside Structure", "inside", true);
    Parameter[] parameters = new Parameter[] {distance, refObjects, structure, inside};
    int currentIndex;
    ImageFloat distanceMap;
    ImageInt mask;
    
    @Override
    public Parameter[] getParameters() {
        return parameters;
    }

    @Override
    public void initialize(RandomPoint3DGenerator rpg, InputCellImages raw, SegmentedCellImages seg) {
        mask=raw.getMask();
        this.rpg=rpg;
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
        
        if (inside.isSelected()&&structure.getIndex()!=0) distanceMap=EDT.run(structure.getImage(seg, false), 0, !inside.isSelected(), nCPUs);
        else distanceMap = seg.getDistanceMap(structure.getIndex(), nCPUs);
    }

    @Override
    public ArrayList<Integer> getVoxelIndexes() {
        return null;
    }

    @Override
    public boolean eval(double x, double y, double z, double res) {
        double dist = distanceMap.getPixel((float)x, (float)y, (float)z, mask);
        return  dist>constraint[currentIndex];
    }

    @Override
    public void reset() {
        currentIndex=0;
    }

    @Override
    public boolean isValid() {
        return rpg.nbPoints<=constraint.length;
    }


    @Override
    public String getHelp() {
        return "constraint on the minimal distance between drawn point and observed Structure";
    }
    
}
