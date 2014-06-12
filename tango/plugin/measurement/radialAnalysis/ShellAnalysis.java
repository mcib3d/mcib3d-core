package tango.plugin.measurement.radialAnalysis;

import ij.gui.Plot;
import java.util.HashMap;
import mcib3d.geom.Object3D;
import mcib3d.image3d.ImageByte;
import tango.dataStructure.InputCellImages;
import tango.dataStructure.ObjectQuantifications;
import tango.dataStructure.SegmentedCellImages;
import tango.dataStructure.StructureQuantifications;
import tango.parameter.*;
import tango.plugin.measurement.MeasurementObject;
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

public class ShellAnalysis implements MeasurementStructure {

    StructureParameter structure = new StructureParameter("Structure:", "structure", -1, true);
    BooleanParameter segmented = new BooleanParameter("Use segmented objects", "segmented", true);
    StructureParameter structureMask = new StructureParameter("Distance From structure:", "structureMask", 0, true);
    //IntParameter nbShells = new IntParameter("Number of Shells", "nbShells", 5);
    SpinnerParameter nbShells = new SpinnerParameter("Number of Shells", "nbShells", 2, 100, 5);
    BooleanParameter normalize = new BooleanParameter("Normalize with nucleus Signal", "normalize", false);
    Parameter[] parameters = new Parameter[]{structure, segmented, structureMask, nbShells, normalize};
    GroupKeyParameter group = new GroupKeyParameter("Shells", "shell", "shell_", true, new KeyParameterObjectNumber[0], true);
    //KeyParameterObjectNumber key = new KeyParameterObjectNumber("Shell", "shell", "shell", true);
    KeyParameterStructureNumber[] keys = new KeyParameterStructureNumber[0];
    Parameter[] allKeys = new Parameter[]{group};
    int nCPUs=1;
    boolean verbose;
    
    public ShellAnalysis() {
        nbShells.setFireChangeOnAction();
    }
    
    @Override
    public int[] getStructures() {
        return new int[] {structure.getIndex()};
    }

    @Override
    public void getMeasure(InputCellImages rawImages, SegmentedCellImages segmentedImages, StructureQuantifications quantifications) {
        if (!group.isSelected()) return;
        ShellAnalysisCore shell = new ShellAnalysisCore(rawImages.getMask(), segmentedImages.getImage(structureMask.getIndex()), structureMask.getIndex()==0, nCPUs, false);
        int nbShell = nbShells.getValue();
        int[] indexes = (normalize.isSelected()) ? shell.getShellIndexesNormalized(nbShell, rawImages.getImage(0)) : shell.getShellIndexes(nbShell);
        double[] shells;
        if (segmented.isSelected()) shells = shell.getShellRepartitionMask(segmentedImages.getImage(structure.getIndex()), indexes);
        else shells = shell.getShellRepartition(rawImages.getImage(structure.getIndex()), indexes);
        if (verbose) ij.IJ.log("Shell Analysis: ");
        for (int i = 0; i<shells.length; i++) {
            quantifications.setQuantificationStructureNumber(keys[i], shells[i]);
            if (verbose) ij.IJ.log(keys[i].getKey()+":"+shells[i]);
        }
        if (verbose) {
            shell.getShellMap(indexes).show();
            double[] shellIdx = new double[shells.length];
            for (int i = 0; i<shells.length; i++) shellIdx[i]=i+1;
            Plot p = (new Plot("Shell Analysis", "Shell Index", "% of fluorescence signal", shellIdx, shells));
            p.setLimits(1 , shellIdx.length, 0, 1);
            p.show();
        }
        
    }

    @Override
    public Parameter[] getKeys() {
        if (nbShells.getValue()!=keys.length) {
            KeyParameterStructureNumber[] newKeys = new KeyParameterStructureNumber[nbShells.getValue()];
            if (newKeys.length>0) {
                if (nbShells.getValue()<keys.length) {
                    if (keys.length>0) System.arraycopy(keys, 0, newKeys, 0, newKeys.length);
                } else {
                    if (keys.length>0) System.arraycopy(keys, 0, newKeys, 0, keys.length);
                    for (int i = keys.length ; i<newKeys.length; i++ ) {
                        newKeys[i]=new KeyParameterStructureNumber("Shell "+(i+1)+ ":", "shell"+(i+1), ""+(i+1), true);
                    }
                }
            }
            keys=newKeys;
            this.group.setKeys(keys);
        }
        return allKeys;
    }

    @Override
    public Parameter[] getParameters() {
        return parameters;
    }

    @Override
    public String getHelp() {
        return "Shell analysis. Shells of equal volume (or equal nucleus signal integrated density is Normalized is checked). % of segmented voxel in each shell if \"Use segmented objects\" is selected, % of fluorescence otherwise";
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
