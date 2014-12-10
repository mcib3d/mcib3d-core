package tango.plugin.measurement;

import ij.IJ;
import ij.gui.Plot;
import java.util.HashMap;
import mcib3d.image3d.ImageByte;
import mcib3d.image3d.ImageHandler;
import tango.dataStructure.InputCellImages;
import tango.dataStructure.ObjectQuantifications;
import tango.dataStructure.SegmentedCellImages;
import tango.dataStructure.StructureQuantifications;
import tango.gui.Core;
import tango.parameter.*;

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
public class Texture3D implements MeasurementObject {
    boolean verbose;
    int nbCPUs=1;
    StructureParameter structure = new StructureParameter("Signal:", "structure", -1, false);
    ChoiceParameter undersample = new ChoiceParameter("Number of gray values (undersampling):", "numberofGrayValues", new String[]{"256", "128", "64", "32"}, "256");
    PreFilterSequenceParameter filters = new PreFilterSequenceParameter("Filters: ", "filters");
    BooleanParameter filtered = new BooleanParameter("Use filtered image:", "filtered", false);
    ChoiceParameter resample = new ChoiceParameter("Z-Anisotropy correction method", "resampleChoice", new String[]{"None", "Use image scale for Z radius", "Make isotropic (bilinear interpolation)", "Make isotropic (bicubic interpolation)"}, "Make isotropic (bilinear interpolation)" );
    BooleanParameter filterAfterResample = new BooleanParameter("Process pre-filters after resampling:", "filtersAfterResample", false);
    BooleanParameter normalize = new BooleanParameter("Normalize by 0-displacement values:", "normalize", true);
    IntParameter displacement = new IntParameter("Displacement:", "radius", 1);
    Parameter[] parameters = new Parameter[]{structure, filtered, resample, filters, filterAfterResample, displacement, undersample, normalize};
    
    KeyParameterObjectNumber K_asm = new KeyParameterObjectNumber("Angular 2nd moment",  "asm");
    KeyParameterObjectNumber K_contrast = new KeyParameterObjectNumber("Contrast",  "contrast");
    KeyParameterObjectNumber K_corr = new KeyParameterObjectNumber("Correlation",  "correlation");
    KeyParameterObjectNumber K_var = new KeyParameterObjectNumber("Variance",  "variance");
    KeyParameterObjectNumber K_idm = new KeyParameterObjectNumber("Inverse Difference Moment",  "idm");
    KeyParameterObjectNumber K_sa = new KeyParameterObjectNumber("Sum Average",  "sumAvg");
    KeyParameterObjectNumber K_sv = new KeyParameterObjectNumber("Sum Variance",  "sumVar");
    KeyParameterObjectNumber K_sumEnt = new KeyParameterObjectNumber("Sum Entropy",  "sumEntropy");
    KeyParameterObjectNumber K_ent = new KeyParameterObjectNumber("Entropy",  "entropy");
    KeyParameterObjectNumber K_dv = new KeyParameterObjectNumber("Difference Variance",  "diffVar");
    KeyParameterObjectNumber K_de = new KeyParameterObjectNumber("Difference Entropy",  "de");
    KeyParameterObjectNumber K_imc1 = new KeyParameterObjectNumber("Information Measures of Correlation 1",  "imc1");
    KeyParameterObjectNumber K_imc2 = new KeyParameterObjectNumber("Information Measures of Correlation 2",  "imc2");
    //KeyParameterObjectNumber K_mcc = new KeyParameterObjectNumber("Maximum Correlation Coefficient",  "mcc");
    
    KeyParameter[] keys = {K_asm, K_contrast, K_corr, K_var, K_idm, K_sa, K_sv, K_sumEnt, K_ent, K_dv, K_de, K_imc1, K_imc2};
    
    
    GroupKeyParameter groupKeys = new GroupKeyParameter("", "texture", "", true, keys, false);
    
    @Override
    public int getStructure() {
        return 0;
    }

    @Override
    public void getMeasure(InputCellImages rawImages, SegmentedCellImages segmentedImages, ObjectQuantifications quantifs) {
        if (structure.getIndex()==-1) {
            if (Core.GUIMode) ij.IJ.log("Texture 3D measurement: no structure selected!");
            else System.out.println("Texture 3D measurement: no structure selected!");
            return;
        }
        ImageHandler filteredImage = (filtered.isSelected())? rawImages.getFilteredImage(structure.getIndex()):rawImages.getImage(structure.getIndex());
        
        if (!filterAfterResample.isSelected()) filteredImage = filters.runPreFilterSequence(structure.getIndex(), filteredImage, rawImages, nbCPUs, false);
        if (verbose) filteredImage.showDuplicate("filtered image");
        int resampleMeth = 0;
        if (resample.getSelectedIndex()==2) resampleMeth=1;
        else if (resample.getSelectedIndex()==3) resampleMeth=2;
        float ZFactor = 1;
        if (resample.getSelectedIndex()==1) ZFactor = (float) (rawImages.getMask().getScaleXY() / rawImages.getMask().getScaleZ());
        GLCMTexture3D tex = new GLCMTexture3D(filteredImage, rawImages.getMask(), Integer.parseInt(undersample.getSelectedItem()), resampleMeth, ZFactor);
        if (verbose) tex.intensityResampled.showDuplicate("after resample");
        if (filterAfterResample.isSelected()) {
            ImageHandler f = filters.runPreFilterSequence(structure.getIndex(), tex.intensityResampled, rawImages, nbCPUs, false); // potentially unstable because doesn't have the same Z-size
            if (!(f instanceof ImageByte)) tex.intensityResampled = new ImageByte(f, true);
            else tex.intensityResampled = (ImageByte)f;
        }
        tex.computeMatrix(displacement.getIntValue(1));
        
        double[] features = tex.computeTextureParameters(normalize.isSelected());
        
        if (K_asm.isSelected()) quantifs.setQuantificationObjectNumber(K_asm, new double[]{features[0]});
        if (K_contrast.isSelected()) quantifs.setQuantificationObjectNumber(K_contrast, new double[]{features[1]});
        if (K_corr.isSelected()) quantifs.setQuantificationObjectNumber(K_corr, new double[]{features[2]});
        if (K_var.isSelected()) quantifs.setQuantificationObjectNumber(K_var, new double[]{features[3]});
        if (K_idm.isSelected()) quantifs.setQuantificationObjectNumber(K_idm, new double[]{features[4]});
        if (K_sa.isSelected()) quantifs.setQuantificationObjectNumber(K_sa, new double[]{features[5]});
        if (K_sv.isSelected()) quantifs.setQuantificationObjectNumber(K_sv, new double[]{features[6]});
        if (K_sumEnt.isSelected()) quantifs.setQuantificationObjectNumber(K_sumEnt, new double[]{features[7]});
        if (K_ent.isSelected()) quantifs.setQuantificationObjectNumber(K_ent, new double[]{features[8]});
        if (K_dv.isSelected()) quantifs.setQuantificationObjectNumber(K_dv, new double[]{features[9]});
        if (K_de.isSelected()) quantifs.setQuantificationObjectNumber(K_de, new double[]{features[10]});
        if (K_imc1.isSelected()) quantifs.setQuantificationObjectNumber(K_imc1, new double[]{features[11]});
        if (K_imc2.isSelected()) quantifs.setQuantificationObjectNumber(K_imc2, new double[]{features[12]});
        //if (K_mcc.isSelected()) quantifs.setQuantificationObjectNumber(K_mcc, new double[]{features[13]});

    }

    @Override
    public Parameter[] getKeys() {
        return new Parameter[]{groupKeys};
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
    public String getHelp() {
        return "Anisotropic 3D Texture coefficients (averaged in all directions). Adapted from JFeatureLib by Franz Gray: https://JFeatureLib.googlecode.com ";
    }
    
}
