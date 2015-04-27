package tango.plugin.measurement;

import ij.ImagePlus;
import ij.measure.ResultsTable;
import ij.plugin.filter.PlugInFilter;
import ij.process.ImageProcessor;
import java.util.ArrayList;
import java.util.Iterator;
import mcib3d.geom.Object3D;
import mcib3d.geom.Object3DVoxels;
import mcib3d.geom.Voxel3D;
import mcib3d.image3d.ImageHandler;
import mcib3d.image3d.ImageInt;
import mcib_plugins.analysis.simpleMeasure;
import tango.dataStructure.InputCellImages;
import tango.dataStructure.ObjectQuantifications;
import tango.dataStructure.SegmentedCellImages;
import tango.parameter.*;

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
public class EdgeContact implements MeasurementObject {

    KeyParameterObjectNumber xy = new KeyParameterObjectNumber("XY Edge Contact (Pix)", "edge_contact_xy_pix");
    KeyParameterObjectNumber z = new KeyParameterObjectNumber("Z Edge Contact (Pix)", "edge_contact_z_pix");
    KeyParameterObjectNumber xyz = new KeyParameterObjectNumber("XYZ Edge Contact (Pix)", "edge_contact_xyz_pix");
    
    Parameter[] keys = new Parameter[]{xy, z, xyz};
    
    public EdgeContact() {
        
    }

    @Override
    public int getStructure() {
        return 0;
    }

    @Override
    public void getMeasure(InputCellImages raw, SegmentedCellImages seg, ObjectQuantifications quantifications) {
        //System.out.println("simple geometrical "+getStructure()+" / "+rawImages.length+" "+segmentedImages.length);
        //System.out.println("simple geometrical "+segmentedImages[getStructure()]+" "+segmentedImages[0]);
        //simpleMeasure sm = new simpleMeasure(structure.getImagePlus(seg, false));
        ImageHandler in = raw.getGlobalNucleusMask();
        int limX = in.sizeX - 1;
        int limY = in.sizeY - 1;
        int limZ = in.sizeZ - 1;
        
        Object3DVoxels o = seg.getObjects(0)[0];
        int count = 0;
        for (Voxel3D v : o.getContours()) {
            if (v.getRoundX() == 0 || v.getRoundX() == limX || v.getRoundY() == 0 || v.getRoundY() == limY) {
                count++;
            }
        }
        int countZ = 0;
        for (Voxel3D v : o.getContours()) {
            if (v.getRoundZ() == 0 || v.getRoundZ() == limZ) {
                countZ++;
            }
        }
        quantifications.setQuantificationObjectNumber(xy, new int[]{count});
        quantifications.setQuantificationObjectNumber(z, new int[]{countZ});
        quantifications.setQuantificationObjectNumber(xyz, new int[]{count+countZ});
            
        
    }

    @Override
    public Parameter[] getParameters() {
        return new Parameter[0];
    }

    @Override
    public Parameter[] getKeys() {
        return keys;
    }

    @Override
    public String getHelp() {
        return "";
    }
    int nCPUs = 1;
    boolean verbose;

    @Override
    public void setMultithread(int nbCPUs) {
        this.nCPUs = nbCPUs;
    }

    @Override
    public void setVerbose(boolean verbose) {
        this.verbose = verbose;
    }
}
