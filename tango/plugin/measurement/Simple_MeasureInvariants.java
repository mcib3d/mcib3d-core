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
public class Simple_MeasureInvariants implements MeasurementObject {

    ImagePlus myPlus;
    // DB
    StructureParameter structure = new StructureParameter("Structure:", "structure", -1, true);
    Parameter[] parameters = new Parameter[]{structure};
    KeyParameterObjectNumber k_GI1 = new KeyParameterObjectNumber("I1", "gi1");
    KeyParameterObjectNumber k_GI2 = new KeyParameterObjectNumber("I2", "gi2");
    KeyParameterObjectNumber k_GI3 = new KeyParameterObjectNumber("I3", "gi3");
    KeyParameterObjectNumber k_GI4 = new KeyParameterObjectNumber("I4", "gi4");
    KeyParameterObjectNumber k_GI5 = new KeyParameterObjectNumber("I5", "gi5");
    KeyParameterObjectNumber k_GI6 = new KeyParameterObjectNumber("I6", "gi6");
    Parameter[] keys = new Parameter[]{k_GI1, k_GI2, k_GI3, k_GI4, k_GI5, k_GI6};

    public Simple_MeasureInvariants() {
        String help = "Higher order geometric invariants (based on Xu and Li Pattern Recognition 2008).";
        k_GI1.setHelp(help + " First Moment", true);
        k_GI2.setHelp(help + " Second Moment", true);
        k_GI3.setHelp(help + " Third Moment", true);
        k_GI4.setHelp(help + " Fourth Moment", true);
        k_GI5.setHelp(help + " Fifth Moment", true);
        k_GI6.setHelp(help + " Sixth Moment", true);
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
        double[] gi;
        int nb = os[0].getGeometricInvariants().length; // should be 6
        double[][] values = new double[nb][os.length];

        for (int o = 0; o < os.length; o++) {
            gi = os[o].getGeometricInvariants();
            for (int i = 0; i < nb; i++) {
                values[i][o] = gi[i];
            }
        }

        if (k_GI1.isSelected()) {
            quantifications.setQuantificationObjectNumber(k_GI1, values[0]);
        }
        if (k_GI2.isSelected()) {
            quantifications.setQuantificationObjectNumber(k_GI2, values[1]);
        }
        if (k_GI3.isSelected()) {
            quantifications.setQuantificationObjectNumber(k_GI3, values[2]);
        }
        if (k_GI4.isSelected()) {
            quantifications.setQuantificationObjectNumber(k_GI4, values[3]);
        }
        if (k_GI5.isSelected()) {
            quantifications.setQuantificationObjectNumber(k_GI5, values[4]);
        }
        if (k_GI6.isSelected()) {
            quantifications.setQuantificationObjectNumber(k_GI6, values[5]);
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
