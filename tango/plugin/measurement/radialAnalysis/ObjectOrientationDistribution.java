package tango.plugin.measurement.radialAnalysis;

import java.util.Arrays;
import java.util.HashMap;
import mcib3d.geom.Object3D;
import mcib3d.geom.Object3DVoxels;
import mcib3d.geom.Voxel3D;
import mcib3d.image3d.ImageFloat;
import tango.dataStructure.InputCellImages;
import tango.dataStructure.ObjectQuantifications;
import tango.dataStructure.SegmentedCellImages;
import tango.dataStructure.StructureQuantifications;
import tango.parameter.*;
import tango.plugin.measurement.MeasurementObject;
import tango.plugin.measurement.MeasurementStructure;

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

public class ObjectOrientationDistribution implements MeasurementStructure {

    StructureParameter structure = new StructureParameter("Structure:", "structure", -1, true);
    StructureParameter structureMask = new StructureParameter("Distance from structure:", "structureMask", 0, true);
    //BooleanParameter inside = new BooleanParameter("Inside Structure", "inside", true);
    DoubleParameter bin = new DoubleParameter("Bin:", "bin", 0.1d, NumberParameter.nfDEC2);
    Parameter[] parameters = new Parameter[]{structure, structureMask, bin}; //inside
    KeyParameterStructureArray cumulVolProp = new KeyParameterStructureArray("Cumulative proportion of volume:", "cumulativeVolumeProportion");
    KeyParameterStructureArray distanceFromStructure = new KeyParameterStructureArray("Distance from Structure:", "distanceFromStructure");
    KeyParameterStructureArray objectIdx = new KeyParameterStructureArray("Object Index:", "ObjectIdx");
    GroupKeyParameter group = new GroupKeyParameter("", "evfGroup", "", true, new KeyParameter[]{cumulVolProp, distanceFromStructure, objectIdx}, true);
    Parameter[] keys = new Parameter[]{group};
    int nCPUs=1;
    boolean verbose;
    ImageFloat distanceMap;
    public ObjectOrientationDistribution() {
        cumulVolProp.setHelp("array of cumulative proportion of volume located at a distance from the 2nd structure inferior to distances stored in the \"distanceFromStructure\" array ", true);
        distanceFromStructure.setHelp("array of distances", true);
        objectIdx.setHelp("index of the object from the first structure", true);
    }
    
    @Override
    public void getMeasure(InputCellImages rawImages, SegmentedCellImages segmentedImages, StructureQuantifications quantif) {
        distanceMap = segmentedImages.getDistanceMap(structureMask.getIndex(), nCPUs);
        
        Object3DVoxels[] objects = segmentedImages.getObjects(structure.getIndex());
        double step = bin.getDoubleValue(0.1);
        int totalLength = 0;
        double[][] allDistances = new double[objects.length][];
        int[] offsets = new int[objects.length];
        if (this.group.isSelected()) {
            int count = 0;
            for (Object3DVoxels o:objects) {
                allDistances[count] = getDistances(o);
                int length = (int) (0.5+allDistances[count][allDistances[count].length-1] / step);
                totalLength += length;
                offsets[count]=totalLength;
                count++;        
            }
            double[] cumul = new double[totalLength];
            int[] idx = new int[totalLength];
            double[] dist = new double[totalLength];
            count=0;
            for (Object3DVoxels o : objects) {
                double volume = o.getVolumePixels();
                double[] d = allDistances[count];
                if (d==null || d.length==0) continue;
                double curDist = 0;
                double cumulValue = 0;
                int offsetD=0;
                for (int offset = count==0?0:offsets[count-1];offset<offsets[count];offset++ ) {
                    curDist+=step;
                    while(offsetD < d.length && d[offsetD]<curDist) {
                        cumulValue++;
                        offsetD++;
                    }
                    cumul[offset]=cumulValue/volume;
                    dist[offset]=curDist;
                    idx[offset]=count+1;
                }
                count++;
            }
            quantif.setQuantificationStructureArray(objectIdx, idx);
            quantif.setQuantificationStructureArray(this.distanceFromStructure, dist);
            quantif.setQuantificationStructureArray(this.cumulVolProp, cumul);
        }
    }
    
    protected double[] getDistances(Object3DVoxels o ) {
        double[] dist = new double[o.getVolumePixels()];
        int count = 0;
        for (Voxel3D v : o.getVoxels()) dist[count++] = distanceMap.getPixel(v);
        Arrays.sort(dist);
        return dist;
    }
    

    @Override
    public Parameter[] getKeys() {
        return keys;
    }

    @Override
    public Parameter[] getParameters() {
        return parameters;
    }

    @Override
    public String getHelp() {
        return "";
    }

    @Override
    public void setVerbose(boolean verbose) {
        this.verbose= verbose;
    }

    @Override
    public void setMultithread(int nCPUs) {
        this.nCPUs=nCPUs;
    }

    @Override
    public int[] getStructures() {
        return new int[]{this.structure.getIndex(), this.structureMask.getIndex()};
    }

    

}
