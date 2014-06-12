package tango.plugin.measurement.radialAnalysis;

import java.util.HashMap;
import mcib3d.geom.Object3D;
import mcib3d.geom.Object3DVoxels;
import mcib3d.geom.Voxel3D;
import mcib3d.image3d.ImageFloat;
import tango.dataStructure.InputCellImages;
import tango.dataStructure.ObjectQuantifications;
import tango.dataStructure.SegmentedCellImages;
import tango.parameter.*;
import tango.plugin.measurement.MeasurementObject;
import tango.plugin.measurement.distance.Distance;
import tango.plugin.measurement.distance.EuclideanDistance;

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

public class ObjectOrientation implements MeasurementObject {

    StructureParameter structure = new StructureParameter("Structure:", "structure", -1, true);
    StructureParameter referenceStructure = new StructureParameter("Reference structure:", "referenceStructure",-1, true);
    StructureParameter structureMask = new StructureParameter("Distance From structure:", "structureMask", 0, true);
    Parameter[] parameters = new Parameter[]{structure, referenceStructure, structureMask};
    KeyParameterObjectNumber key = new KeyParameterObjectNumber("Relative Volume Fraction:", "relativeVolumeFraction", "relativeVolumeFraction", true);
    KeyParameterObjectNumber nnIdx = new KeyParameterObjectNumber("Nearest reference object idx:", "nearestObjectIdx", "nearestObjectIdx", true);
    
    GroupKeyParameter group = new GroupKeyParameter("", "evfGroup", "", true, new KeyParameter[]{key, nnIdx}, false);
    Parameter[] keys = new Parameter[]{group};
    int nCPUs=1;
    boolean verbose;
    ImageFloat distanceMap;
    public ObjectOrientation() {
        key.setHelp("For each object (o1) contained in \"structure\" (s1), computes the volume fraction of the nearest object (o2) from \"reference structure\" (s2) that is located at a distance inferior or equal to the 3rd structure (s3) than the distance center of o1- to nearest border of s3. ", true);
    }
    
    @Override
    public int getStructure() {
        return structure.getIndex();
    }

    @Override
    public void getMeasure(InputCellImages rawImages, SegmentedCellImages segmentedImages, ObjectQuantifications quantifications) {
        distanceMap = segmentedImages.getDistanceMap(this.structureMask.getIndex(), nCPUs);
        ImageFloat mask=null;
        if (verbose) mask = new ImageFloat("relative spatial distribution mask", distanceMap.sizeX, distanceMap.sizeY, distanceMap.sizeZ);
        Object3DVoxels[] objects1 = segmentedImages.getObjects(structure.getIndex());
        Object3DVoxels[] objects2 = segmentedImages.getObjects(referenceStructure.getIndex());
        Distance distCB = new EuclideanDistance(2, 2);
        if (key.isSelected() || nnIdx.isSelected()) {
            int[] nn = new int[objects1.length];
            distCB.getNearestNeighborDistances(objects1, objects2, nn);
            double[] r = new double[objects1.length];
            for (int i = 0; i<r.length; i++) {
                r[i] = getVolumeFraction(distanceMap.getPixel(objects1[i].getCenterAsPoint()), objects2[nn[i]], i+1, mask);
                if (verbose) {
                    ij.IJ.log("Object:"+(i+1) + " nearest object:"+(nn[i]+1) + "volume fraction:"+r[i]);
                }
            }
            if (verbose) mask.show();
            if (key.isSelected()) quantifications.setQuantificationObjectNumber(key, r);
            if (nnIdx.isSelected()) {
                for (int i = 0; i<nn.length;i++) nn[i]++;
                quantifications.setQuantificationObjectNumber(nnIdx, nn);
            }
        }
        distanceMap=null;
    }
    
    protected double getVolumeFraction(double distance, Object3DVoxels o, int label, ImageFloat mask) {
        double vol = 0;
        for (Voxel3D v : o.getVoxels()) {
            if (distanceMap.getPixel(v)<=distance) {
                vol++;
                if (mask!=null) mask.setPixel(v, label); // verbose mode
            }
        }
        return vol / (double)o.getVolumePixels();
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
        //return "Normalized distance between objects center and selected structure. Normalization : (volume <= distance) / total volume.  Similar to a shell analysis (but continuous)";
    }

    @Override
    public void setVerbose(boolean verbose) {
        this.verbose= verbose;
    }

    @Override
    public void setMultithread(int nCPUs) {
        this.nCPUs=nCPUs;
    }

}
