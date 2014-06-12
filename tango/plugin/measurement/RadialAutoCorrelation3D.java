package tango.plugin.measurement;

import ij.gui.Plot;
import java.util.HashMap;
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
public class RadialAutoCorrelation3D implements MeasurementObject {
    boolean verbose;
    int nbCPUs=1;
    StructureParameter structure = new StructureParameter("Structure:", "structure", -1, false);
    PreFilterSequenceParameter filters = new PreFilterSequenceParameter("Filters: ", "filters");
    BooleanParameter filtered = new BooleanParameter("Use filtered image:", "filtered", true);
    BooleanParameter resample = new BooleanParameter("Make isotropic:", "resample", true);
    MultiParameter radii = new MultiParameter("Radius:", "radiusMP", new Parameter[]{new SliderParameter("Radius", "radius", 1, 20, 1)}, 0, 20, 0);
    Parameter[] parameters = new Parameter[]{structure, filtered, resample, filters, radii};
    
    KeyParameterObjectNumber[] keys = new KeyParameterObjectNumber[0];
    GroupKeyParameter group = new GroupKeyParameter("", "autocorrelationKeys", "", true, keys, false);
    Parameter[] returnKeys = new Parameter[]{group};
    public RadialAutoCorrelation3D() {
        radii.getSpinner().setFireChangeOnAction();
    }
    
    @Override
    public int getStructure() {
        return 0;
    }

    @Override
    public void getMeasure(InputCellImages rawImages, SegmentedCellImages segmentedImages, ObjectQuantifications quantifs) {
        if (structure.getIndex()==-1) {
            ij.IJ.log("Autocorrelation measurement: no structure selected!");
            return;
        }
        ImageHandler input = (filtered.isSelected())? rawImages.getFilteredImage(structure.getIndex()):rawImages.getImage(structure.getIndex());
        ImageHandler filteredImage = filters.runPreFilterSequence(structure.getIndex(), input, rawImages, nbCPUs, false);
        
        RadialAutoCorrelation rac = new RadialAutoCorrelation(filteredImage, rawImages.getMask(), resample.isSelected());
        Parameter[] radParam = radii.getParameters();
        double[] rads = new double[radParam.length];
        double[] corrs = new double[radParam.length];
        for (int i =0; i<radParam.length; i++) {
            int curRad = ((SliderParameter)radParam[i]).getValue();
            corrs[i] = rac.getCorrelation(curRad);
            rads[i]=curRad;
            quantifs.setQuantificationObjectNumber(keys[i], new double[]{corrs[i]});
        }
        
        if (Core.debug) {
            Plot p = new Plot ("Radial Autocorrelation:", "Radius", "Correlation", rads, corrs);
            double xmin = Double.MAX_VALUE;
            double xmax = 0;
            double ymin=0;
            double ymax=1;
            for (int i = 0; i<corrs.length; i++) {
                if (corrs[i]<ymin) ymin=corrs[i];
                if (corrs[i]>ymax) ymax=corrs[i];
                if (rads[i]<xmin) xmin=rads[i];
                if (rads[i]>xmax) xmax=rads[i];
            }
            p.setLimits(xmin, xmax, ymin, ymax);
            p.show();
            rac.intensityResampled.showDuplicate("Intensity resampled");
            rac.maskResampled.showDuplicate("Mask resampled");
        }
    }

    @Override
    public Parameter[] getKeys() {
        if (radii.getNbParameters() != keys.length) {
            KeyParameterObjectNumber[] newKeys = new KeyParameterObjectNumber[radii.getNbParameters()];
            if (newKeys.length > 0) {
                if (radii.getNbParameters() < keys.length) {
                    if (keys.length > 0) {
                        System.arraycopy(keys, 0, newKeys, 0, newKeys.length);
                    }
                } else {
                    if (keys.length > 0) {
                        System.arraycopy(keys, 0, newKeys, 0, keys.length);
                    }
                    for (int i = keys.length; i < newKeys.length; i++) {
                        
                        newKeys[i] = new KeyParameterObjectNumber("AutoCorrelation Radius " + (i + 1) + ":", "rac" + (i + 1), "rac" + (i + 1), true);
                    }
                }
            }
            keys = newKeys;
            this.group.setKeys(keys);
        }
        return returnKeys;
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
