package tango.plugin.sampler;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import ij.ImagePlus;
import ij.measure.Calibration;
import mcib3d.geom.Object3D;
import mcib3d.geom.Point3D;
import tango.spatialStatistics.StochasticProcess.RandomPoint3DGenerator;
import tango.spatialStatistics.StochasticProcess.RandomPoint3DGeneratorDistanceMap;
import tango.spatialStatistics.StochasticProcess.RandomPoint3DGeneratorProbaMap;
import tango.spatialStatistics.StochasticProcess.RandomPoint3DGeneratorUniform;
import mcib3d.image3d.ImageFloat;
import mcib3d.image3d.ImageHandler;
import mcib3d.image3d.ImageInt;
import mcib3d.image3d.distanceMap3d.EDT;
import tango.dataStructure.InputCellImages;
import tango.dataStructure.SegmentedCellImages;
import tango.dataStructure.Structure;
import tango.parameter.*;
import tango.plugin.sampler.Sampler;
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
public class ConstraintDistanceSampler implements Sampler {
    
    final static int maxIterationsSample = 1000;
    final static int maxIterationsPoint = 1000;
    DoubleParameter hardcore = new DoubleParameter("Hardcore distance from other points (unit):", "hardcore",0d, Parameter.nfDEC5);
    StructureParameter referenceObjects_P = new StructureParameter("Reference Objects for distances:", "objects", 0, true);
    DistanceMapParameter dm = new DistanceMapParameter("Distance from structure", "dm", "");
    Parameter[] parameters = new Parameter[] {referenceObjects_P, dm, hardcore};
    float[] constraint;
    RandomPoint3DGenerator rpg;
    public ConstraintDistanceSampler() {
        hardcore.setHelp("Radius for Hardcore constraint. 0 for no hardcore", true);
        hardcore.setHelp("Hardcore!", false);
    }
    boolean verbose;
    int nbCPUs=1;
    @Override
    public void setMultithread(int nbCPUs) {
        this.nbCPUs=nbCPUs;
    }
    
    @Override
    public void setVerbose(boolean verbose) {
        this.verbose=verbose;
    }
    
    @Override
    public Parameter[] getParameters() {
       return parameters;
    }
    
    @Override 
    public void initSampler(InputCellImages raw, SegmentedCellImages seg) {
        ImageInt mask = raw.getMask();
        Object3D[] referenceObjects = referenceObjects_P.getObjects(seg);
        
        float meanRad=0;
        if (dm.isErodeNucleus() && dm.getErodeDistance()<=0) {
            for (int i =0;i<referenceObjects.length; i++) meanRad += (float)referenceObjects[i].getDistCenterMean();
            if (referenceObjects.length>0) meanRad/=referenceObjects.length;
        }
        constraint = new float[referenceObjects.length];
        ImageHandler[] mdm = dm.getMaskAndDistanceMap(raw, seg,meanRad, verbose, nbCPUs);
        ImageFloat distanceMap = (ImageFloat)mdm[1];
        for (int i = 0; i<referenceObjects.length; i++) {
            Point3D center = referenceObjects[i].getCenterAsPoint();
            constraint[i]=distanceMap.getPixel((float)center.getX(), (float)center.getY(), (float)center.getZ(), mask);
            if (verbose) ij.IJ.log("Point:"+i+ " distance from reference structure:"+constraint[i]);
        }
        rpg = new RandomPoint3DGeneratorDistanceMap((ImageInt)mdm[0], referenceObjects.length, distanceMap, constraint, nbCPUs, verbose);
        float hard = hardcore.getFloatValue(0);
        if (hard>0) rpg.setHardCore(hard);
    }
    
    @Override
    public Object3D[] getSample() {
        rpg.resetPoints();
        return rpg.drawObjects(maxIterationsPoint, maxIterationsSample);
    }

    @Override
    public String getHelp() {
        return "Generates random points with constrained distances to another structure";
    }
    
    @Override
    public void displaySample() {
        rpg.showPoints("constraint distance sampler", getSample()).show();
    }
    
}
