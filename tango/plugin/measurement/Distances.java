package tango.plugin.measurement;

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
import tango.plugin.measurement.*;

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
public class Distances implements MeasurementStructure {
    int nbCPUs=1;
    boolean verbose=false;
    StructureParameter structure1 = new StructureParameter("Structure 1:", "structure1", -1, true);
    StructureParameter structure2 = new StructureParameter("Structure 2:", "structure2", -1, true);
    
    Parameter[] parameters = new Parameter[] {structure1, structure2};
    
    KeyParameterStructureArrayO2O distCC = new KeyParameterStructureArrayO2O("Distance Center-Center", "distCC", "distCC", true);
    KeyParameterStructureArrayO2O distCB = new KeyParameterStructureArrayO2O("Distance Center-Border", "distCB", "distCB", true);
    KeyParameterStructureArrayO2O distBC = new KeyParameterStructureArrayO2O("Distance Border-Center", "distBC", "distBC", true);
    KeyParameterStructureArrayO2O distBB = new KeyParameterStructureArrayO2O("Distance Border-Border", "distBB", "distBB", true);
    KeyParameterStructureArrayO2O[] keys = new KeyParameterStructureArrayO2O[]{distCC, distCB, distBC, distBB};
    
    @Override
    public int[] getStructures() {
        return new int[]{structure1.getIndex(), structure2.getIndex()};
    }


    @Override
    public void getMeasure(InputCellImages raw, SegmentedCellImages seg, StructureQuantifications quantifs) {
        if (structure1.getIndex()==structure2.getIndex()) getMes(structure1.getObjects(seg), quantifs);
        else getMes(structure1.getObjects(seg), structure2.getObjects(seg), quantifs);
    }
    
    private void getMes(Object3D[] objects1, Object3D[] objects2, StructureQuantifications quantifs) {
        if (objects1==null || objects2==null) return;
        int size1=objects1.length;
        int size2=objects2.length;
        if (distCC.isSelected()) {
            double[] mes = new double[size1*size2]; 
            int offset=0;
            for (int i = 0 ; i<objects1.length; i++) {
                Object3D o = objects1[i];
                for (int j =0; j<size2; j++) {
                    mes[j+offset]=o.distCenterUnit(objects2[j]);
                }
                offset+=size2;
            }
            quantifs.setQuantificationStructureArrayO2O(distCC, mes);
        }
        if (distCB.isSelected()) {
            double[] mes = new double[size1*size2]; 
            int offset=0;
            for (int i = 0 ; i<objects1.length; i++) {
                Object3D o = objects1[i];
                for (int j =0; j<objects2.length; j++) {
                    mes[j+offset]=o.distCenterBorderUnit(objects2[j]);
                }
                offset+=size2;
            }
            quantifs.setQuantificationStructureArrayO2O(distCB, mes);
        }
        if (distBC.isSelected()) {
            double[] mes = new double[size1*size2];
            int offset=0;
            for (int i = 0 ; i<objects1.length; i++) {
                Object3D o = objects1[i];
                for (int j =0; j<objects2.length; j++) {
                    mes[j+offset]=objects2[j].distCenterBorderUnit(o);
                }
                offset+=size2;
            }
            quantifs.setQuantificationStructureArrayO2O(distBC, mes);
        }
        if (distBB.isSelected()) {
            double[] mes = new double[size1*size2];
            int offset=0;
            for (int i = 0 ; i<objects1.length; i++) {
                Object3D o = objects1[i];
                for (int j =0; j<objects2.length; j++) {
                    mes[j+offset]=o.distBorderUnit(objects2[j]);
                }
                offset+=size2;
            }
            quantifs.setQuantificationStructureArrayO2O(distBB, mes);
        }
    }
    
    private void getMes(Object3D[] objects, StructureQuantifications quantifs) {
        if (objects==null) return;
        int size=(objects.length*(objects.length-1))/2;
        if (distCC.isSelected()) {
            double[] mes = new double[size]; 
            int count=0;
            for (int i = 0 ; i<(objects.length-1); i++) {
                Object3D o = objects[i];
                for (int j =i+1; j<objects.length; j++) {
                    mes[count]=o.distCenterUnit(objects[j]);
                    count++;
                }
            }
            quantifs.setQuantificationStructureArrayO2O(distCC, mes);
        }
        if (distCB.isSelected()) {
            double[] mes = new double[size]; 
            int count=0;
            for (int i = 0 ; i<(objects.length-1); i++) {
                Object3D o = objects[i];
                for (int j =i+1; j<objects.length; j++) {
                    mes[count]=o.distCenterBorderUnit(objects[j]);
                    count++;
                }
            }
            quantifs.setQuantificationStructureArrayO2O(distCB, mes);
        }
        if (distBC.isSelected()) {
            double[] mes = new double[size]; 
            int count=0;
            for (int i = 0 ; i<(objects.length-1); i++) {
                Object3D o = objects[i];
                for (int j =i+1; j<objects.length; j++) {
                    mes[count]=objects[j].distCenterBorderUnit(o);
                    count++;
                }
            }
            quantifs.setQuantificationStructureArrayO2O(distBC, mes);
        }
        if (distBB.isSelected()) {
            double[] mes = new double[size]; 
            int count=0;
            for (int i = 0 ; i<(objects.length-1); i++) {
                Object3D o = objects[i];
                for (int j =i+1; j<objects.length; j++) {
                    mes[count]=objects[j].distBorderUnit(o);
                    count++;
                }
            }
            quantifs.setQuantificationStructureArrayO2O(distBB, mes);
        }
    }

    @Override
    public Parameter[] getParameters() {
        return parameters;
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
    public KeyParameter[] getKeys() {
        return keys;
    }

    @Override
    public String getHelp() {
        return "computes Euclidean distance between segmented objects of the two selected structures";
    }

    

    
}
