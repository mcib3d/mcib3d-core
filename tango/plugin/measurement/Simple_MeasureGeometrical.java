package tango.plugin.measurement;

import ij.ImagePlus;
import ij.measure.ResultsTable;
import ij.plugin.filter.PlugInFilter;
import ij.process.ImageProcessor;
import java.util.ArrayList;
import java.util.Iterator;
import mcib3d.geom.Object3D;
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
public class Simple_MeasureGeometrical implements PlugInFilter, MeasurementObject {

    ImagePlus myPlus;
    // DB
    StructureParameter structure = new StructureParameter("Structure:", "structure", -1, true);
    Parameter[] parameters = new Parameter[]{structure};
    KeyParameterObjectNumber k_vol_p = new KeyParameterObjectNumber("Volume (Pix)", "volume_pix");
    KeyParameterObjectNumber k_vol_u = new KeyParameterObjectNumber("Volume (Unit)", "volume_unit");
    KeyParameterObjectNumber k_surf_p = new KeyParameterObjectNumber("Surface (Pix)", "surface_pix");
    KeyParameterObjectNumber k_surf_u = new KeyParameterObjectNumber("Surface (Unit)", "surface_unit");
    //KeyParameterObjectNumber k_surfMesh_u = new KeyParameterObjectNumber("Surface Mesh (Unit)", "surface_mesh","surface_mesh", false);
    //KeyParameterObjectNumber k_surfMeshS_u = new KeyParameterObjectNumber("Surface Mesh smooth (Unit)", "surface_mesh_smooth", "surface_mesh_smooth", false);
    KeyParameterObjectNumber k_cx = new KeyParameterObjectNumber("CX(pix)", "cx");
    KeyParameterObjectNumber k_cy = new KeyParameterObjectNumber("CY(pix)", "cy");
    KeyParameterObjectNumber k_cz = new KeyParameterObjectNumber("CZ(pix)", "cz");
    KeyParameterObjectNumber k_radDist = new KeyParameterObjectNumber("Radial distance", "rad_dist", "rad_dist", false);
    KeyParameterObjectNumber k_comp = new KeyParameterObjectNumber("Compactness", "compacity");
    KeyParameterObjectNumber k_feretmax = new KeyParameterObjectNumber("FeretMax", "feretMax");
    KeyParameterObjectNumber k_elong = new KeyParameterObjectNumber("Elongation", "elongation");
    KeyParameterObjectNumber k_ratioellvol = new KeyParameterObjectNumber("Ratio Ellipsoid / Volume", "ratioEllVolume");
    KeyParameterObjectNumber k_DCavg = new KeyParameterObjectNumber("DCavg", "DCavg");
    KeyParameterObjectNumber k_DCsd = new KeyParameterObjectNumber("DCsd", "DCsd");
    KeyParameterObjectNumber k_ZSize = new KeyParameterObjectNumber("Z-thickness", "ZThickness", "ZThickness", true);
    KeyParameterObjectNumber k_XYFeretMax = new KeyParameterObjectNumber("XY max feret", "XYFeretMax", "XYFeretMax", true);
    KeyParameterObjectNumber k_XYEllMax = new KeyParameterObjectNumber("XY-max axis", "XYAxisMax", "XYAxisMax", true);
    KeyParameterObjectNumber k_XYEllMin = new KeyParameterObjectNumber("XY-min axis", "XYAxisMin", "XYAxisMin", true);
    KeyParameterObjectNumber k_XYDCavg = new KeyParameterObjectNumber("XY-DCavg", "XYDCavg");
    KeyParameterObjectNumber k_XYDCsd = new KeyParameterObjectNumber("XY-DCsd", "XYDCsd");
    GroupKeyParameter XYKeys = new GroupKeyParameter("XY Measurements:", "XYMeasurements", "", true, new KeyParameter[]{k_XYFeretMax, k_XYEllMax, k_XYEllMin, k_XYDCavg, k_XYDCsd}, false);
    Parameter[] keys = new Parameter[]{k_vol_p, k_vol_u, k_surf_p, k_surf_u, k_cx, k_cy, k_cz, k_radDist, k_comp, k_feretmax, k_elong, k_ratioellvol, k_DCavg, k_DCsd, k_ZSize, XYKeys};
    // dialog
    //KeyParameter[] keysBase = new KeyParameter[]{k_vol_p, k_vol_u, k_surf_p, k_surf_u};
    String[] keysBase_s = new String[]{"label", "Volume(pix)", "Volume(unit)", "Surface(pix)", "Surface(unit)"};
    //KeyParameter[] keysCentroid = new KeyParameter[]{k_cx, k_cy, k_cz};
    //KeyParameter[] keysShape = new KeyParameter[]{k_comp, k_feretmax, k_elong, k_ratioellvol, k_DCavg, k_DCsd};

    public Simple_MeasureGeometrical() {
        k_vol_p.setHelp("Volume of the object, estimated as the number of voxels of the object", true);
        k_vol_u.setHelp("Volume of the object in calibrated unit", true);
        k_surf_p.setHelp("The surface of the object", true);
        k_surf_p.setHelp("The surface of the object as the number of border voxel", false);
        k_surf_u.setHelp("The surface of the object in calibrated unit", true);
        k_surf_u.setHelp("The surface of the object as the number of border voxel using calibrated unit, voxel ma have different surfaces depending if they are on XY border or Z border ", false);
        k_cx.setHelp("The X coordinate of the center of the object", true);
        k_cy.setHelp("The Y coordinate of the center of the object", true);
        k_cz.setHelp("The Z coordinate of the center of the object", true);
        k_radDist.setHelp("The position of the structure along the radius coming from center of nucleus and passing trough center of object (between 0 and 1)", true);
        k_comp.setHelp("The compactness (equivalent to 2D circularity in ImageJ)", true);
        k_comp.setHelp("The compactness as the ratio between volume square and surface power 3 (with a normalization factor so as to have 1 for a sphere)", false);
        k_feretmax.setHelp("The Feret diameter, as the longest distance between two points of the object", true);
        k_elong.setHelp("The object id fitted by an ellipsoid, then the elongation is the ratio between the longest axis and the second longest axis of that ellipsooid.", true);
        k_ratioellvol.setHelp("Ratio between the fitted 3D ellipsoid and the actual volume, values close to 1 describe an ellpitic shape.", true);
        k_DCavg.setHelp("The average of all distances between the center of the object and its borders", true);
        k_DCsd.setHelp("The standard deviation of all distances between the center of the object and its border", true);
        k_ZSize.setHelp("The difference between the highest Z and lowest Z of the object.", true);
        k_XYFeretMax.setHelp("2D feret measurements on all slices of the object", true);
        k_XYEllMax.setHelp("2D major axis of 2D fitted ellipses on all slices of the object", true);
        k_XYEllMin.setHelp("2D minor axis of 2D fitted ellipses on all slices of the object", true);
    }

    @Override
    public int setup(String arg, ImagePlus imp) {
        myPlus = imp;
        return PlugInFilter.DOES_16 + PlugInFilter.DOES_8G + PlugInFilter.STACK_REQUIRED;
    }

    @Override
    public void run(ImageProcessor ip) {
        simpleMeasure mes = new simpleMeasure(myPlus);
        ResultsTable rt = ResultsTable.getResultsTable();
        if (rt == null) {
            rt = new ResultsTable();
        }
        ArrayList<double[]> res = mes.getMeasuresBase();
        int row = rt.getCounter();
        for (Iterator<double[]> it = res.iterator(); it.hasNext();) {
            rt.incrementCounter();
            double[] m = it.next();
            for (int k = 0; k < keysBase_s.length; k++) {
                rt.setValue(keysBase_s[k], row, m[k]);
            }
            row++;
        }
        rt.updateResults();
        rt.show("Results");


    }

    @Override
    public int getStructure() {
        return structure.getIndex();
    }

    @Override
    public void getMeasure(InputCellImages raw, SegmentedCellImages seg, ObjectQuantifications quantifications) {
        //System.out.println("simple geometrical "+getStructure()+" / "+rawImages.length+" "+segmentedImages.length);
        //System.out.println("simple geometrical "+segmentedImages[getStructure()]+" "+segmentedImages[0]);
        //simpleMeasure sm = new simpleMeasure(structure.getImagePlus(seg, false));
        Object3D[] os = seg.getObjects(structure.getIndex());

        // base
        if (k_vol_p.isSelected()) {
            double[] values = new double[os.length];
            for (int i = 0; i < os.length; i++) {
                values[i] = os[i].getVolumePixels();
            }
            quantifications.setQuantificationObjectNumber(k_vol_p, values);

        }
        if (k_vol_u.isSelected()) {
            double[] values = new double[os.length];
            for (int i = 0; i < os.length; i++) {
                values[i] = os[i].getVolumeUnit();
            }
            quantifications.setQuantificationObjectNumber(k_vol_u, values);
        }
        if (k_surf_p.isSelected()) {
            double[] values = new double[os.length];
            for (int i = 0; i < os.length; i++) {
                values[i] = os[i].getAreaPixels();
            }
            quantifications.setQuantificationObjectNumber(k_surf_p, values);
        }
        if (k_surf_u.isSelected()) {
            double[] values = new double[os.length];
            for (int i = 0; i < os.length; i++) {
                values[i] = os[i].getAreaUnit();
            }
            quantifications.setQuantificationObjectNumber(k_surf_u, values);
        }
        // centroid
        if (k_cx.isSelected()) {
            double[] values = new double[os.length];
            for (int i = 0; i < os.length; i++) {
                values[i] = os[i].getCenterX();
            }
            quantifications.setQuantificationObjectNumber(k_cx, values);
        }
        if (k_cy.isSelected()) {
            double[] values = new double[os.length];
            for (int i = 0; i < os.length; i++) {
                values[i] = os[i].getCenterY();
            }
            quantifications.setQuantificationObjectNumber(k_cy, values);
        }
        if (k_cz.isSelected()) {
            double[] values = new double[os.length];
            for (int i = 0; i < os.length; i++) {
                values[i] = os[i].getCenterZ();
            }
            quantifications.setQuantificationObjectNumber(k_cz, values);
        }
        // radial distance
        Object3D nucleus = seg.getObjects(0)[0];
        if (k_radDist.isSelected()) {
            double[] values = new double[os.length];
            for (int i = 0; i < os.length; i++) {
                double rad = nucleus.radiusCenter(os[i]);
                if (rad != 0) {
                    values[i] = nucleus.distCenterUnit(os[i]) / rad;                
                } else {
                    values[i] = 0;
                }
            }
            quantifications.setQuantificationObjectNumber(k_radDist, values);
        }
        // shape
        if (k_comp.isSelected()) {
            double[] values = new double[os.length];
            for (int i = 0; i < os.length; i++) {
                values[i] = os[i].getCompactness();
            }
            quantifications.setQuantificationObjectNumber(k_comp, values);
        }
        if (k_feretmax.isSelected()) {
            double[] values = new double[os.length];
            for (int i = 0; i < os.length; i++) {
                values[i] = os[i].getFeret();
            }
            quantifications.setQuantificationObjectNumber(k_feretmax, values);
        }
        if (k_elong.isSelected()) {
            double[] values = new double[os.length];
            for (int i = 0; i < os.length; i++) {
                values[i] = os[i].getMainElongation();
            }
            quantifications.setQuantificationObjectNumber(k_elong, values);
        }
        if (k_ratioellvol.isSelected()) {
            double[] values = new double[os.length];
            for (int i = 0; i < os.length; i++) {
                values[i] = os[i].getRatioEllipsoid();
            }
            quantifications.setQuantificationObjectNumber(k_ratioellvol, values);
        }
        if (k_DCavg.isSelected()) {
            double[] values = new double[os.length];
            for (int i = 0; i < os.length; i++) {
                values[i] = os[i].getDistCenterMean();
            }
            quantifications.setQuantificationObjectNumber(k_DCavg, values);
        }
        if (k_DCsd.isSelected()) {
            double[] values = new double[os.length];
            for (int i = 0; i < os.length; i++) {
                values[i] = os[i].getDistCenterSigma();
            }
            quantifications.setQuantificationObjectNumber(k_DCsd, values);
        }
        //Z-thickness
        if (k_ZSize.isSelected()) {
            double[] values = new double[os.length];
            for (int i = 0; i < os.length; i++) {
                values[i] = (os[i].getZmax() - os[i].getZmin()) * os[i].getResZ();
            }
            quantifications.setQuantificationObjectNumber(k_ZSize, values);
        }
        // XY measurements 
        if (XYKeys.isSelected()) {
            if (os == null) {
                os = seg.getObjects(structure.getIndex());
            }
            Object3D[] os2D = new Object3D[os.length];
            for (int i = 0; i < os.length; i++) {
                ImageInt proj = os[i].createSegImageMini2D(os[i].getValue(), 1);
                os2D[i] = proj.getObjects3D()[0];
            }
            if (k_XYEllMax.isSelected() || k_XYEllMin.isSelected()) {
                double[] valuesMax = new double[os.length];
                double[] valuesMin = new double[os.length];
                for (int i = 0; i < os.length; i++) {
                    double r1 = os2D[i].getRadiusMoments(2);
                    double r2 = r1 / os2D[i].getMainElongation();
                    valuesMax[i] = r1;
                    valuesMin[i] = r2;
                }
                quantifications.setQuantificationObjectNumber(k_XYEllMax, valuesMax);
                quantifications.setQuantificationObjectNumber(k_XYEllMin, valuesMin);
            }
            if (k_XYFeretMax.isSelected()) {
                double[] values = new double[os.length];
                for (int i = 0; i < os.length; i++) {
                    values[i] = os2D[i].getFeret();
                }
                quantifications.setQuantificationObjectNumber(k_XYFeretMax, values);
            }
            if (k_XYDCavg.isSelected()) {
                double[] values = new double[os.length];
                for (int i = 0; i < os.length; i++) {
                    values[i] = os2D[i].getDistCenterMean();
                }
                quantifications.setQuantificationObjectNumber(k_XYDCavg, values);
            }
            if (k_XYDCsd.isSelected()) {
                double[] values = new double[os.length];
                for (int i = 0; i < os.length; i++) {
                    values[i] = os2D[i].getDistCenterSigma();
                }
                quantifications.setQuantificationObjectNumber(k_XYDCsd, values);
            }


        }
    }

    @Override
    public Parameter[] getParameters() {
        return parameters;
    }

    @Override
    public Parameter[] getKeys() {
        return keys;
    }

    @Override
    public String getHelp() {
        return "Classical 3D geometrical measurements and shape descriptors.";
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
