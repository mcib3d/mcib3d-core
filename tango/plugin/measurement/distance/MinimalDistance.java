package tango.plugin.measurement.distance;

import tango.plugin.measurement.*;
import mcib3d.geom.Object3DFactory;
import mcib3d.geom.Object3DFuzzy;
import ij.ImagePlus;
import mcib3d.image3d.ImageFloat;
import mcib3d.image3d.ImageHandler;
import mcib3d.image3d.ImageInt;
import java.util.ArrayList;
import java.util.HashMap;
import mcib3d.geom.Object3D;
import mcib3d.geom.Object3DVoxels;
import tango.dataStructure.InputCellImages;
import tango.dataStructure.ObjectQuantifications;
import tango.dataStructure.SegmentedCellImages;
import tango.parameter.*;
import tango.plugin.measurement.Measurement;
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
public class MinimalDistance implements MeasurementObject {
    boolean verbose;
    int nbCPUs=1;
    StructureParameter structure1 = new StructureParameter("Structure:", "structure1", -1, true);
    StructureParameter structure2 = new StructureParameter("Minimal Distance from Structure:", "structure2", -1, true);
    DistanceParameter distance = new DistanceParameter("Distance:", "distance", Distance.distances[0]);
    Parameter[] parameters = new Parameter[] {structure1, structure2, distance};
    KeyParameterObjectNumber dist = new KeyParameterObjectNumber("Distance", "dist", "dist", true);
    KeyParameterObjectNumber idx = new KeyParameterObjectNumber("Structure 2 Object Index", "s2idx", "idx", true);
    KeyParameter[] keys = new KeyParameter[]{dist, idx};
    GroupKeyParameter groupkey = new GroupKeyParameter("", "groupMinDist", "", true, keys, false);
    
    @Override
    public int getStructure() {
        return structure1.getIndex();
    }


    @Override
    public void setMultithread(int nbCPUs) {
        this.nbCPUs=nbCPUs;
    }
    
    @Override
    public void setVerbose(boolean verbose) {
        this.verbose=verbose;
    }
    
    @Override
    public void getMeasure(InputCellImages raw, SegmentedCellImages seg, ObjectQuantifications quantifs) {
        Distance d = distance.getDistance(raw, seg);
        //System.out.println("generic distance:"+d.getClass());
        int[] labels = null;
        double[] labelsO = null;
        if (idx.isSelected()) {
            labels = new int[structure1.getObjects(seg).length];
            labelsO = new double[labels.length];
        }
        double[] minDist;
        if (structure1.getIndex()==structure2.getIndex()) minDist=d.getNearestNeighborDistances(structure1.getObjects(seg), labels);
        else minDist = d.getNearestNeighborDistances(structure1.getObjects(seg), structure2.getObjects(seg), labels);
        if (labels!=null && minDist!=null) for (int i = 0; i<minDist.length; i++) labelsO[i]=labels[i]+1;
        if (minDist!=null) quantifs.setQuantificationObjectNumber(dist, minDist);
        if (labels!=null) quantifs.setQuantificationObjectNumber(idx, labelsO);
    }
    

    @Override
    public Parameter[] getParameters() {
        return parameters;
    }


    @Override
    public Parameter[] getKeys() {
        return new Parameter[]{groupkey};
    }

    @Override
    public String getHelp() {
        return "Computes minimal distance between each segmented objects of the first selected Structure against all semgented objects of the second structure. Result is an array of length n1";
    }

    
}
