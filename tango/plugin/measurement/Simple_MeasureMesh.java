package tango.plugin.measurement;

import ij.ImagePlus;
import java.util.ArrayList;
import java.util.HashMap;
import mcib3d.geom.Object3D;
import mcib3d.geom.Object3DSurface;
import mcib3d.image3d.ImageInt;
import mcib_plugins.analysis.simpleMeasure;
import tango.dataStructure.InputCellImages;
import tango.dataStructure.ObjectQuantifications;
import tango.dataStructure.SegmentedCellImages;
import tango.parameter.KeyParameter;
import tango.parameter.KeyParameterObjectNumber;
import tango.parameter.Parameter;
import tango.parameter.StructureParameter;

/*
 * To change this template, choose Tools | Templates and open the template in
 * the editor.
 */
/**
 *
 **
 * /**
 * Copyright (C) 2008- 2012 Thomas Boudier and others
 *
 *
 *
 * This file is part of mcib3d
 *
 * mcib3d is free software; you can redistribute it and/or modify it under the
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
 * @author thomas
 */
public class Simple_MeasureMesh implements MeasurementObject {

    ImagePlus myPlus;
    // DB
    StructureParameter structure = new StructureParameter("Structure:", "structure", -1, true);
    Parameter[] parameters = new Parameter[]{structure};
    KeyParameterObjectNumber k_surf_u = new KeyParameterObjectNumber("Surface Mesh (Unit)", "surface_mesh");
    KeyParameterObjectNumber k_surfs_u = new KeyParameterObjectNumber("Surface Mesh smooth (Unit)", "surface_mesh_smooth");
    KeyParameter[] keys = new KeyParameter[]{k_surf_u, k_surfs_u};
    int nCPUs=1;
    boolean verbose;
    @Override
    public void setMultithread(int nbCPUs) {
        this.nCPUs=nbCPUs;
    }
    
    @Override
    public void setVerbose(boolean verbose) {
        this.verbose=verbose;
    }

    @Override
    public int getStructure() {
        return structure.getIndex();
    }

    @Override
    public void getMeasure(InputCellImages raw, SegmentedCellImages seg, ObjectQuantifications quantifs) {
        Object3D[] objects = seg.getObjects(getStructure());
        Object3DSurface[] objectSurf = new Object3DSurface[objects.length];
        ImageInt mask = raw.getMask();
        for (int i = 0; i<objects.length; i++) {
            objectSurf[i] = new Object3DSurface(objects[i].computeMeshSurface(true), objects[i].getValue());
            objectSurf[i].setCalibration(mask.getScaleXY(), mask.getScaleZ(), mask.getUnit());
            objectSurf[i].setSmoothingFactor(0.1f);
        }
        if (k_surf_u.isSelected()) {
            double[] values = new double[objectSurf.length];
            for (int i = 0; i<objectSurf.length; i++) values[i]=objectSurf[i].getSurfaceMesh();
            quantifs.setQuantificationObjectNumber(k_surf_u, values);
        } 
        if (k_surfs_u.isSelected()) {
            double[] values = new double[objectSurf.length];
            for (int i = 0; i<objectSurf.length; i++) values[i]=objectSurf[i].getSmoothSurfaceArea();
            quantifs.setQuantificationObjectNumber(k_surfs_u, values);
        } 
        
    }

    @Override
    public Parameter[] getParameters() {
        return parameters;
    }

    @Override
    public KeyParameter[] getKeys() {
        return keys;
    }

    @Override
    public String getHelp() {
        return "";
    }
}
