package tango.plugin.measurement.radialAnalysis;

import ij.gui.Plot;
import java.util.HashMap;
import mcib3d.geom.Object3D;
import mcib3d.image3d.ImageFloat;
import mcib3d.image3d.ImageHandler;
import tango.dataStructure.InputCellImages;
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

public class ShellProfile implements MeasurementStructure {
    BooleanParameter useFiltered = new BooleanParameter("Use filtered Image", "filtered", true);
    BooleanParameter normalize = new BooleanParameter("Normalize with nucleus Signal", "normalize", false);
    StructureParameter structure = new StructureParameter("Structure Signal:", "structure", -1, false);
    StructureParameter structureMask = new StructureParameter("Distance From structure:", "structureMask", 0, true);
    //BooleanParameter inside = new BooleanParameter("Inside Structure", "inside", true);
    IntParameter nbShells = new IntParameter("Number of Shells", "nbShells", 5);
    DoubleParameter hessScale = new DoubleParameter("Hessian Integration Scale", "hessScale", 1d, DoubleParameter.nfDEC5);
    BooleanParameter hessBool = new BooleanParameter("Compute Spotiness Profile", "doSpotiness", true);
    ConditionalParameter hessCond = new ConditionalParameter(hessBool);
    Parameter[] parameters = new Parameter[]{structure, structureMask, useFiltered, normalize, nbShells, hessCond}; 
    KeyParameterStructureArray keyX = new KeyParameterStructureArray("Shell Profile breaks", "shellProfileBreaks", "shellProfileBreaks", true);
    KeyParameterStructureArray key = new KeyParameterStructureArray("Shell Profile", "shellProfile", "shellProfile", true);
    KeyParameterStructureArray keyHess = new KeyParameterStructureArray("Spotiness Shell Profile", "spotinessShellProfile", "spotinessShellProfile", true);
    GroupKeyParameter groupKey= new GroupKeyParameter("", "keys", "", true, new KeyParameter[]{keyX, key}, false); //keyHess
    int nCPUs=1;
    boolean verbose;
    
    public ShellProfile() {
        this.hessCond.setCondition(true, new Parameter[]{hessScale});
    }
    
    @Override
    public int[] getStructures() {
        return new int[]{structure.getIndex(), structureMask.getIndex()};
    }

    @Override
    public void getMeasure(InputCellImages rawImages, SegmentedCellImages segmentedImages, StructureQuantifications quantifs) {
        if (!keyX.isSelected() && !key.isSelected() && !keyHess.isSelected()) return;
        ShellAnalysisCore shell = new ShellAnalysisCore(rawImages.getMask(), segmentedImages.getImage(structureMask.getIndex()), structureMask.getIndex()==0, nCPUs, verbose);
        int nbShell = nbShells.getIntValue(5);
        int[] indexes = (normalize.isSelected()) ? shell.getShellIndexesNormalized(nbShell, rawImages.getImage(0)) : shell.getShellIndexes(nbShell);
        double[] x=null;
        if (keyX.isSelected() || verbose) {
            x = shell.getBreaks(indexes);
            quantifs.setQuantificationStructureArray(keyX, x);
        }
        ImageHandler intensity = (useFiltered.isSelected())? rawImages.getFilteredImage(structure.getIndex()):rawImages.getImage(structure.getIndex());
        if (key.isSelected()) {
            double[] y = shell.getProfile(intensity, indexes);
            quantifs.setQuantificationStructureArray(key, y);
            if (verbose) {
                Plot p  = new Plot("radial profile", "distance from structure", "proportion of signal", x, y);
                p.setLimits(x[0], x[x.length-1], 0, 1);
                p.show();
                intensity.show();
            }
        }
        /*if (keyHess.isSelected() && hessBool.isSelected()) {
            ImageFloat hess = intensity.normalize(rawImages.getMask(), 0).getHessian(hessScale.getDoubleValue(1d), nCPUs)[0];
            hess.opposite();
            double[] y2 = shell.getProfileOverThld(hess, indexes, Spotiness.spotinessThreshold);
            quantifs.setQuantificationStructureArray(keyHess, y2);
            if (verbose) {
                Plot p2  = new Plot("radial spotiness profile", "distance from structure", "proportion of spotiness", x, y2);
                p2.setLimits(x[0], x[x.length-1], 0, 1);
                p2.show();
                hess.show();
            }
        }
        * 
        */
    }

    @Override
    public Parameter[] getKeys() {
        return new Parameter[]{groupKey};
    }

    @Override
    public Parameter[] getParameters() {
        return parameters;
    }

    @Override
    public String getHelp() {
        return "Normalized intensity profile (cumulative sum). X Normalization : (volume <= distance) / total volume.  Y Normalization: sum = 1";
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
