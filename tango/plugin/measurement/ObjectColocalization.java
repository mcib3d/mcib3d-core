package tango.plugin.measurement;

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
import tango.dataStructure.SegmentedCellImages;
import tango.dataStructure.StructureQuantifications;
import tango.parameter.StructureParameter;
import tango.parameter.KeyParameter;
import tango.parameter.KeyParameterStructureArrayO2O;
import tango.parameter.Parameter;
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
public class ObjectColocalization implements MeasurementObject2Object {
    boolean verbose;
    int nbCPUs=1;
    StructureParameter structure1 = new StructureParameter("Structure 1:", "structure1", -1, true);
    StructureParameter structure2 = new StructureParameter("Structure 2:", "structure2", -1, true);
    
    Parameter[] parameters = new Parameter[] {structure1, structure2};
    
    KeyParameterStructureArrayO2O overlap = new KeyParameterStructureArrayO2O("Overlap: (unit)", "overlap", "overlap", true);
    KeyParameterStructureArrayO2O pc1 = new KeyParameterStructureArrayO2O("%Overlap: (/structure1)", "pcoverlap1", "pcOverlap1", true);
    KeyParameterStructureArrayO2O pc2 = new KeyParameterStructureArrayO2O("%Overlap: (/structure2)", "pcoverlap2", "pcOverlap2", true);
    KeyParameterStructureArrayO2O[] keys = new KeyParameterStructureArrayO2O[]{overlap, pc1, pc2};
    
    @Override
    public int[] getStructures() {
        return new int[]{structure1.getIndex(), structure2.getIndex()};
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
    public void getMeasure(InputCellImages raw, SegmentedCellImages seg, StructureQuantifications quantifs) {
        if (!overlap.isSelected() && !pc1.isSelected() && !pc2.isSelected()) return;
        if (structure1.getIndex()==structure2.getIndex()) getMes(structure1.getObjects(seg), quantifs);
        else getMes(structure1.getObjects(seg), structure2.getObjects(seg), quantifs);
    }
    
    private void getMes(Object3D[] objects1, Object3D[] objects2, StructureQuantifications quantifs) {
        int size1=objects1.length;
        int size2=objects2.length;
        
        if (size1==0 || size2==0) return;
        double volumeUnit = objects1[0].getResXY()*objects1[0].getResXY()*objects1[0].getResZ();
        double[] mes=new double[size1*size2];
        double[] mespc1 = null;
        double[] mespc2=null;
        if (pc1.isSelected()) mespc1 = new double[size1*size2]; 
        if (pc2.isSelected()) mespc2= new double[size1*size2];
        int offset=0;
        for (int i = 0 ; i<objects1.length; i++) {
            Object3D o = objects1[i];
            for (int j =0; j<size2; j++) {
                mes[j+offset] = o.getColoc(objects2[j]) * volumeUnit;
                //System.out.println("coloc:"+i+ "+"+j+ " "+mes[j+offset]);
                if (mespc1!=null) mespc1[j+offset]=mes[j+offset] / (o.getVolumeUnit());
                if (mespc2!=null) mespc2[j+offset]=mes[j+offset]/ (objects2[j].getVolumeUnit());
            }
            offset+=size2;
        }
        if (overlap.isSelected()) {
            quantifs.setQuantificationStructureArrayO2O(overlap, mes);
        }
        if (pc1.isSelected()) {
            quantifs.setQuantificationStructureArrayO2O(pc1, mespc1);
        }
        if (pc2.isSelected()) {
            quantifs.setQuantificationStructureArrayO2O(pc2, mespc2);
        }
    }
    
    private void getMes(Object3D[] objects, StructureQuantifications quantifs) {
        int size=(objects.length*(objects.length-1))/2;
        if (objects.length<=1) return;
        double volumeUnit = objects[0].getResXY()*objects[0].getResXY()*objects[0].getResZ();
        double[] mes=new double[size];
        double[] mespc1 = null;
        double[] mespc2=null;
        if (pc1.isSelected()) mespc1 = new double[size]; 
        if (pc2.isSelected()) mespc2= new double[size];
        int count=0;
        for (int i = 0 ; i<(objects.length-1); i++) {
            Object3D o = objects[i];
            for (int j =i+1; j<objects.length; j++) {
                mes[count]=o.getColoc(objects[j]) * volumeUnit;
                if (mespc1!=null) mespc1[count]=mes[count] / (double)o.getVolumeUnit();
                if (mespc2!=null) mespc2[count]=mes[count] / (double)objects[j].getVolumeUnit();
                count++;
            }
        }
        if (overlap.isSelected()) {
            quantifs.setQuantificationStructureArrayO2O(overlap, mes);
        }
        if (pc1.isSelected()) {
            quantifs.setQuantificationStructureArrayO2O(pc1, mespc1);
        }
        if (pc2.isSelected()) {
            quantifs.setQuantificationStructureArrayO2O(pc2, mespc2);
        }
    }

    @Override
    public Parameter[] getParameters() {
        return parameters;
    }


    @Override
    public KeyParameterStructureArrayO2O[] getKeys() {
        return keys;
    }

    @Override
    public String getHelp() {
        return "Computes overlap between segmented objects of the two selected structures";
    }

    
}
