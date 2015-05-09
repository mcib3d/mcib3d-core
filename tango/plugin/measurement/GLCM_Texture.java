package tango.plugin.measurement;


import ij.ImagePlus;
import ij.gui.GenericDialog;
import ij.measure.ResultsTable;
import ij.plugin.filter.PlugInFilter;
import ij.process.ImageProcessor;
import java.util.HashMap;
import mcib_plugins.analysis.GLCMTexture;
import tango.dataStructure.InputCellImages;
import tango.dataStructure.SegmentedCellImages;
import tango.dataStructure.StructureQuantifications;
import tango.parameter.*;
import tango.plugin.measurement.MeasurementStructure;

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
public class GLCM_Texture implements  MeasurementStructure {

    ImagePlus myPlus;
    // DB
    String[] dirs = {"X", "Y", "Z"};
    int dir = 0;
    int step = 1;
    StructureParameter structure = new StructureParameter("Structure:", "structure", -1, true);
    ChoiceParameter dirchoice = new ChoiceParameter("Direction:", "direction", dirs, dirs[dir]);
    IntParameter stepPar = new IntParameter("Step:", "step", step);
    //BooleanParameter centroid = new BooleanParameter("Centroid ?", "centroid", true);
    //BooleanParameter shape = new BooleanParameter("Shape descriptors ?", "shape", true);
    Parameter[] parameters = new Parameter[]{structure, dirchoice, stepPar};
    
    KeyParameterStructureNumber K_asm = new KeyParameterStructureNumber("ASM",  "glcm_asm");
    KeyParameterStructureNumber K_contrast = new KeyParameterStructureNumber("Contrast", "glcm_contrast");
    KeyParameterStructureNumber K_corr = new KeyParameterStructureNumber("Correlation",  "glcm_correlation");
    KeyParameterStructureNumber K_entropy = new KeyParameterStructureNumber("Entropy",  "glcm_entropy");
    KeyParameterStructureNumber K_idm = new KeyParameterStructureNumber("IDM",  "glcm_idm");
    KeyParameterStructureNumber K_sum = new KeyParameterStructureNumber("Sum",  "glcm_sum");
    KeyParameter[] keys = {K_asm, K_contrast, K_corr, K_entropy, K_idm, K_sum};
    GroupKeyParameter group = new GroupKeyParameter("", "GLCM", "", true, keys, false);
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
    // 
    
    @Override
    public int[] getStructures() {
        return new int[] {structure.getIndex()};
    }

   

    @Override
    public void getMeasure(InputCellImages raw, SegmentedCellImages seg, StructureQuantifications quantifs) {
        GLCMTexture glcm = new GLCMTexture(structure.getImagePlus(raw, false), dirchoice.getSelectedIndex(), stepPar.getIntValue(1));
        
        if (K_asm.isSelected()) quantifs.setQuantificationStructureNumber(K_asm, glcm.getASM());
        if (K_contrast.isSelected()) quantifs.setQuantificationStructureNumber(K_contrast, glcm.getContrast());
        if (K_corr.isSelected()) quantifs.setQuantificationStructureNumber(K_corr, glcm.getCorrelation());
        if (K_entropy.isSelected()) quantifs.setQuantificationStructureNumber(K_entropy, glcm.getEntropy());
        if (K_idm.isSelected()) quantifs.setQuantificationStructureNumber(K_idm, glcm.getIDM());
        if (K_sum.isSelected()) quantifs.setQuantificationStructureNumber(K_sum, glcm.getSum());

    }

    @Override
    public Parameter[] getParameters() {
        return parameters;
    }
    
    @Override
    public Parameter[] getKeys() {
        return new Parameter[]{group};
    }


    @Override
    public String getHelp() {
        return "GLCM TExture";
    }
}
