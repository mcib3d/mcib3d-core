package tango.spatialStatistics.constraints;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;
import mcib3d.geom.Object3D;
import mcib3d.geom.Point3D;
import mcib3d.image3d.ImageFloat;
import mcib3d.image3d.ImageInt;
import mcib3d.image3d.distanceMap3d.EDT;
import tango.dataStructure.InputCellImages;
import tango.dataStructure.SegmentedCellImages;
import tango.parameter.BooleanParameter;
import tango.parameter.Parameter;
import tango.parameter.StructureParameter;
import tango.spatialStatistics.StochasticProcess.RandomPoint3DGenerator;
import tango.spatialStatistics.util.Distance;
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

public class DistancesToStructure extends Constraint {
    StructureParameter referenceObjects_P = new StructureParameter("Reference Objects for distances:", "objects", 0, true);
    StructureParameter segmentedMap = new StructureParameter("Distance from Objects:", "segMap", 0, true);
    BooleanParameter inside = new BooleanParameter("Draw Inside Objects:", "inside", true);
    BooleanParameter outside = new BooleanParameter("Draw Outside Objects:", "outside", true);
    Parameter[] parameters = new Parameter[] {referenceObjects_P, segmentedMap, inside, outside};
    
    
    protected Distance[] distanceArray;
    protected ImageFloat distanceMap;
    protected float[] constraint;
    protected Random randomGenerator;
    protected ImageInt mask;
    
    public DistancesToStructure() {
    }
    
    @Override
    public ArrayList<Integer> getVoxelIndexes() {
        if (rpg.pointIndex>constraint.length) return null;
        int lowerBound = drawBound((float)(constraint[rpg.pointIndex]-rpg.resXY/2), true);
        int upperBound = drawBound((float)(constraint[rpg.pointIndex]+rpg.resXY/2), false);
        ArrayList<Integer> indexes= new ArrayList<Integer> (upperBound-lowerBound+1);
        for (int i = 0; i<=upperBound-lowerBound; i++) {
            //indexes.add(distanceArray[i+lowerBound].idx);
        }
        return indexes;
    }

    @Override
    public boolean eval(double x, double y, double z, double res) {
        return Math.abs(distanceMap.getPixel((float)x, (float)y, (float)z, mask)-constraint[rpg.pointIndex])<=res;
    }
    
    protected void setDistanceMap(ImageFloat distanceMap) {
        this.distanceMap=distanceMap;
        distanceArray = new Distance[rpg.maskCoordsXY.length];
        //for (int i = 0; i<distanceArray.length; i++) distanceArray[i]=new Distance(distanceMap.pixels[rpg.maskCoordsZ[i]][rpg.maskCoordsXY[i]], i);
        Arrays.sort(distanceArray);
    }
    
    /*private int drawIndex(float distance) {
        int lowerBound = drawBound((float)(distance-rpg.resXY/2), true);
        int upperBound = drawBound((float)(distance+rpg.resXY/2), false);
        int i = lowerBound;
        if (upperBound-lowerBound>0) i+=randomGenerator.nextInt(upperBound-lowerBound+1);
        return distanceArray[i].idx;
    }
    */
    
    private int drawBound(float distance, boolean lower) {
        int i = Arrays.binarySearch(distanceArray, new Distance(distance, 0,0));
        if (i<0) {
            i=-i-1;
            if (i>0 && (i==distanceArray.length || (distanceArray[i].distance-distance>distance-distanceArray[i-1].distance))) i--;
            return i;
        } else {
            // if repeated values:
            if (lower) {
                int lowerBound=i;
                while (i>0 && distanceArray[lowerBound].distance==distanceArray[i].distance) lowerBound--;
                return lowerBound;
            } else {
                int upperBound=i;
                while (upperBound<distanceArray.length && distanceArray[upperBound].distance==distanceArray[i].distance) upperBound++;
                return upperBound;
            }
            
        }
    }

    @Override
    public void reset() {
        
    }

    @Override
    public Parameter[] getParameters() {
        return parameters;
    }

    @Override
    public void initialize(RandomPoint3DGenerator rpg, InputCellImages raw, SegmentedCellImages seg) {
        this.rpg=rpg;
        mask=raw.getMask();
        this.randomGenerator = rpg.randomGenerator;
        Object3D[] referenceObjects = referenceObjects_P.getObjects(seg);
        constraint = new float[referenceObjects.length];
        if (inside.isSelected() && outside.isSelected()) distanceMap= mcib3d.image3d.distanceMap3d.EDT.run_includeInside(seg.getImage(segmentedMap.getIndex()), 0, false,  nCPUs);
        else if (inside.isSelected() && segmentedMap.getIndex()!=0) distanceMap = EDT.run(seg.getImage(segmentedMap.getIndex()), 0, false, nCPUs);
        else distanceMap=seg.getDistanceMap(segmentedMap.getIndex(), nCPUs);
        for (int i = 0; i<referenceObjects.length; i++) {
            Point3D center = referenceObjects[i].getCenterAsPoint();
            constraint[i]=distanceMap.getPixel((float)center.getX(), (float)center.getY(), (float)center.getZ(), mask);
        }
        setDistanceMap(distanceMap);
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