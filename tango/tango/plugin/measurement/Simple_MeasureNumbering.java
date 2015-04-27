package tango.plugin.measurement;

import mcib3d.geom.Object3D;
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
public class Simple_MeasureNumbering implements MeasurementObject {
    // DB
    StructureParameter channel1 = new StructureParameter("Structure reference :", "structRef", -1, true);
    StructureParameter channel2 = new StructureParameter("Structure objects:", "structObj", -1, false);
    SliderParameter SliderPcIn = new SliderParameter("% inside", "pcIn", 50, 100, 100);
    SliderParameter SliderPcOut = new SliderParameter("% outside", "pcOut", 50, 100, 100);
    Parameter[] parameters = {channel1, channel2, SliderPcIn, SliderPcOut};
    KeyParameterObjectNumber k_all = new KeyParameterObjectNumber("All objects", "allObjects", "allObjects", true);
    KeyParameterObjectNumber k_in = new KeyParameterObjectNumber("Objects inside", "insideObjects", "insideObjects", false);
    KeyParameterObjectNumber k_out = new KeyParameterObjectNumber("Objects outside", "outsideObjects", "outsideObjects", false);
    KeyParameter[] keys = new KeyParameter[]{k_all, k_in, k_out};
    int nCPUs = 1;
    boolean verbose = false;

    public Simple_MeasureNumbering() {
        channel1.setHelp("The reference structure where to count objects (usually the nucleus)", true);
        channel2.setHelp("The structure to count objects", true);
        SliderPcIn.setHelp("Percentage of coloc for objects considered inside (100 % means totally inside reference structure", verbose);
        SliderPcOut.setHelp("Percentage of coloc for objects considered outside (100 % means totally outside reference structure", verbose);
        k_all.setHelp("Count all objects", true);
        k_in.setHelp("Count only objects that are inside the reference structure", true);
        k_out.setHelp("Count only objects that are outside the reference structure", true);
    }

    @Override
    public void setMultithread(int nbCPUs) {
        this.nCPUs = nbCPUs;
    }

    @Override
    public void setVerbose(boolean verbose) {
        this.verbose = verbose;
    }

    @Override
    public void getMeasure(InputCellImages raw, SegmentedCellImages seg, ObjectQuantifications quantifications) {
        Object3D[] refs = seg.getObjects(channel1.getIndex());
        Object3D[] objs = seg.getObjects(channel2.getIndex());
        int nb_ref = refs.length;
        int nb_objs = objs.length;
        if (k_all.isSelected()) {
            double[] values = new double[nb_ref];
            for (int i = 0; i < nb_ref; i++) {
                values[i] = nb_objs;
            }
            quantifications.setQuantificationObjectNumber(k_all, values);
        }
        // process inside and outside; for optimization separate them ?
        if ((k_in.isSelected()) || (k_out.isSelected())) {
            int pcIn = SliderPcIn.getValue();
            int pcOut = SliderPcOut.getValue();
            double[] valuesIn = new double[nb_ref];
            double[] valuesOut = new double[nb_ref];
            for (int ir = 0; ir < nb_ref; ir++) {
                Object3D obRef = refs[ir];
                int countIn = 0;
                int countOut = 0;
                for (int io = 0; io < nb_objs; io++) {
                    double coloc=objs[io].pcColoc(obRef);
                    if (coloc >= pcIn) {
                        countIn++;
                    }
                    if (coloc <= 100 - pcOut) {
                        countOut++;
                    }
                }
                valuesIn[ir] = countIn;
                valuesOut[ir] = countOut;
            }
            if (k_in.isSelected()) {
                quantifications.setQuantificationObjectNumber(k_in, valuesIn);
            }
            if (k_out.isSelected()) {
                quantifications.setQuantificationObjectNumber(k_out, valuesOut);
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
    public int getStructure() {
        return channel1.getIndex();
    }

    @Override
    public String getHelp() {
        return "Numbering of objects inside or outside reference";
    }
}
