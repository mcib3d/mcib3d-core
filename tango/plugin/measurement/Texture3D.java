package tango.plugin.measurement;

import ij.gui.Plot;
import java.util.HashMap;
import mcib3d.image3d.ImageHandler;
import tango.dataStructure.InputCellImages;
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
public class Texture3D implements MeasurementStructure {
    boolean verbose;
    int nbCPUs=1;
    StructureParameter structure = new StructureParameter("Structure:", "structure", -1, false);
    PreFilterSequenceParameter filters = new PreFilterSequenceParameter("Filters: ", "filters");
    BooleanParameter filtered = new BooleanParameter("Use filtered image:", "filtered", true);
    BooleanParameter resample = new BooleanParameter("Make isotropic:", "resample", true);
    
    IntParameter radiusMax = new IntParameter("Max. Radius:", "maxRadius", 10);
    IntParameter radiusIncrement = new IntParameter("Radius Increment:", "increment", 1);
    Parameter[] parameters = new Parameter[]{structure, filtered, resample, filters, radiusMax, radiusIncrement};
    KeyParameterStructureArray x = new KeyParameterStructureArray("TextrureRadius:", "radius", "textureRadius", true);
    KeyParameterStructureArray K_asm = new KeyParameterStructureArray("ASM",  "glcm_asm");
    KeyParameterStructureArray K_contrast = new KeyParameterStructureArray("Contrast", "glcm_contrast");
    KeyParameterStructureArray K_corr = new KeyParameterStructureArray("Correlation",  "glcm_correlation");
    KeyParameterStructureArray K_entropy = new KeyParameterStructureArray("Entropy",  "glcm_entropy");
    KeyParameterStructureArray K_idm = new KeyParameterStructureArray("IDM",  "glcm_idm");
    KeyParameterStructureArray K_sum = new KeyParameterStructureArray("Sum",  "glcm_sum");
    KeyParameter[] keys = {x, K_asm, K_contrast, K_corr, K_entropy, K_idm, K_sum};
    
    
    GroupKeyParameter groupKeys = new GroupKeyParameter("", "texture", "", true, keys, false);
    @Override
    public int[] getStructures() {
        return new int[]{structure.getIndex()};
    }

    @Override
    public void getMeasure(InputCellImages rawImages, SegmentedCellImages segmentedImages, StructureQuantifications quantifs) {
        if (structure.getIndex()==-1) {
            ij.IJ.log("Texture 3D measurement: no structure selected!");
            return;
        }
        ImageHandler input = (filtered.isSelected())? rawImages.getFilteredImage(structure.getIndex()):rawImages.getImage(structure.getIndex());
        ImageHandler filteredImage = filters.runPreFilterSequence(structure.getIndex(), input, rawImages, nbCPUs, false);
        
        GLCMTexture3D tex = new GLCMTexture3D(filteredImage, rawImages.getMask(), resample.isSelected(), true);
        int min = 1;
        int max = radiusMax.getIntValue(10);
        int step = Math.max(1, radiusIncrement.getIntValue(1));
        double[] rads = new double[(max-min+1)/step];
        double[] asm = new double[rads.length];
        double[] contrast = new double[rads.length];
        double[] corr = new double[rads.length];
        double[] entropy = new double[rads.length];
        double[] idm = new double[rads.length];
        double[] sum = new double[rads.length];
        
        int curRad = min;
        for (int i =0; i<rads.length; i++) {
            tex.computeMatrix(curRad);
            corr[i] = tex.getCorrelation();
            asm[i] = tex.getASM();
            contrast[i] = tex.getContrast();
            entropy[i] = tex.getEntropy();
            idm[i] = tex.getIDM();
            sum[i] = tex.getSum();
            rads[i]=curRad;
            curRad+=step;
        }
        
        if (Core.debug) {
            Plot p = new Plot ("Texture 3D:", "Radius", "Correlation", rads, corr);
            p.setLimits(min, max, 0, 1);
            p.show();
            tex.intensityResampled.showDuplicate("Intensity resampled");
            tex.maskResampled.showDuplicate("Mask resampled");
            (new Plot ("Texture 3D:", "Radius", "ASM", rads, asm)).show();
            (new Plot ("Texture 3D:", "Radius", "Contrast", rads, contrast)).show();
            (new Plot ("Texture 3D:", "Radius", "Entropy", rads, entropy)).show();
            (new Plot ("Texture 3D:", "Radius", "idm", rads, idm)).show();
            (new Plot ("Texture 3D:", "Radius", "Sum", rads, sum)).show();
        }
        quantifs.setQuantificationStructureArray(K_asm, asm);
        quantifs.setQuantificationStructureArray(K_contrast, contrast);
        quantifs.setQuantificationStructureArray(K_entropy, entropy);
        quantifs.setQuantificationStructureArray(K_idm, idm);
        quantifs.setQuantificationStructureArray(K_sum, sum);
        quantifs.setQuantificationStructureArray(K_corr, corr);
        
        quantifs.setQuantificationStructureArray(x, rads);
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
        return "Anisotropic 3D Texture coefficients";
    }
    
}
