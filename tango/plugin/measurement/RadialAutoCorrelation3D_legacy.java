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
public class RadialAutoCorrelation3D_legacy implements MeasurementStructure {
    boolean verbose;
    int nbCPUs=1;
    StructureParameter structure = new StructureParameter("Structure:", "structure", -1, false);
    PreFilterSequenceParameter filters = new PreFilterSequenceParameter("Filters: ", "filters");
    BooleanParameter filtered = new BooleanParameter("Use filtered image:", "filtered", true);
    BooleanParameter resample = new BooleanParameter("Make isotropic:", "resample", true);
    
    IntParameter radiusMax = new IntParameter("Max. Radius:", "maxRadius", 10);
    IntParameter radiusIncrement = new IntParameter("Radius Increment:", "increment", 1);
    Parameter[] parameters = new Parameter[]{structure, filtered, resample, filters, radiusMax, radiusIncrement};
    KeyParameterStructureArray x = new KeyParameterStructureArray("Autocorrelation Radius:", "autocorrelationRadius", "autocorrelationRadius", true);
    KeyParameterStructureArray y = new KeyParameterStructureArray("Autocorrelation:", "autocorrelation", "autocorrelation", true);
    GroupKeyParameter keys = new GroupKeyParameter("", "autocorrelation", "", true, new KeyParameter[]{x, y}, false);
    @Override
    public int[] getStructures() {
        return new int[]{structure.getIndex()};
    }

    @Override
    public void getMeasure(InputCellImages rawImages, SegmentedCellImages segmentedImages, StructureQuantifications quantifs) {
        if (structure.getIndex()==-1) {
            ij.IJ.log("Autocorrelation measurement: no structure selected!");
            return;
        }
        ImageHandler input = (filtered.isSelected())? rawImages.getFilteredImage(structure.getIndex()):rawImages.getImage(structure.getIndex());
        ImageHandler filteredImage = filters.runPreFilterSequence(structure.getIndex(), input, rawImages, nbCPUs, false);
        
        RadialAutoCorrelation rac = new RadialAutoCorrelation(filteredImage, rawImages.getMask(), resample.isSelected());
        int min = 1;
        int max = radiusMax.getIntValue(10);
        int step = Math.max(1, radiusIncrement.getIntValue(1));
        double[] rads = new double[(max-min+1)/step];
        double[] corrs = new double[rads.length];
        int curRad = min;
        for (int i =0; i<rads.length; i++) {
            corrs[i] = rac.getCorrelation(curRad);
            rads[i]=curRad;
            curRad+=step;
        }
        
        if (Core.debug) {
            Plot p = new Plot ("Radial Autocorrelation:", "Radius", "Correlation", rads, corrs);
            p.setLimits(min, max, 0, 1);
            p.show();
            rac.intensityResampled.showDuplicate("Intensity resampled");
            rac.maskResampled.showDuplicate("Mask resampled");
        }
        quantifs.setQuantificationStructureArray(y, corrs);
        quantifs.setQuantificationStructureArray(x, rads);
    }

    @Override
    public Parameter[] getKeys() {
        return new Parameter[]{keys};
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
        return "3D Signal Autocorrelation coefficient, normalized by mean and variance of signal. See http://en.wikipedia.org/wiki/Autocorrelation";
    }
    
}
