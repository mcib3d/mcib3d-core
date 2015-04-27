package tango.plugin.measurement.radialAnalysis;

import java.util.HashMap;
import mcib3d.geom.Object3D;
import tango.dataStructure.InputCellImages;
import tango.dataStructure.ObjectQuantifications;
import tango.dataStructure.SegmentedCellImages;
import tango.parameter.*;
import tango.plugin.measurement.MeasurementObject;

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

public class ShellAnalysis_legacy implements MeasurementObject {

    StructureParameter structure = new StructureParameter("Structure:", "structure", -1, true);
    StructureParameter structureMask = new StructureParameter("Distance From structure:", "structureMask", 0, true);
    IntParameter nbShells = new IntParameter("Number of Shells", "nbShells", 5);
    BooleanParameter normalize = new BooleanParameter("Normalize with nucleus Signal", "normalize", false);
    Parameter[] parameters = new Parameter[]{structure, structureMask, nbShells, normalize};
    KeyParameterObjectNumber key = new KeyParameterObjectNumber("Shell", "shell", "shell", true);
    KeyParameter[] keys = new KeyParameter[]{key};
    int nCPUs=1;
    boolean verbose;
    @Override
    public int getStructure() {
        return structure.getIndex();
    }

    @Override
    public void getMeasure(InputCellImages rawImages, SegmentedCellImages segmentedImages, ObjectQuantifications quantifications) {
        if (!key.isSelected()) return;
        ShellAnalysisCore shell = new ShellAnalysisCore(rawImages.getMask(), segmentedImages.getImage(structureMask.getIndex()), structureMask.getIndex()==0, nCPUs, verbose);
        Object3D[] objects = segmentedImages.getObjects(structure.getIndex());
        int nbShell = nbShells.getIntValue(5);
        int[] indexes = (normalize.isSelected()) ? shell.getShellIndexesNormalized(nbShell, rawImages.getImage(0)) : shell.getShellIndexes(nbShell);
        double[] shells = shell.getShells(objects, indexes);
        quantifications.setQuantificationObjectNumber(key, shells);
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
        return "Shell analysis. Shells of equal volume (or equal integrated density is Normalized is checked)";
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
