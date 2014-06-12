package tango.plugin.measurement;

import ij.gui.Plot;
import java.util.HashMap;
import mcib3d.geom.Object3D;
import mcib3d.geom.Object3DVoxels;
import mcib3d.image3d.ImageHandler;
import mcib3d.image3d.ImageInt;
import mcib3d.image3d.ImageStats;
import mcib3d.image3d.processing.FastFilters3D;
import tango.dataStructure.InputCellImages;
import tango.dataStructure.SegmentedCellImages;
import tango.dataStructure.StructureQuantifications;
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
public class Granulometry implements MeasurementStructure {

    double[] mean;
    double[] sd;
    int maxRad = 5;
    int minRad = 0;
    int step = 1;
    boolean verbose;
    int nbCPUs = 1;
    StructureParameter structure = new StructureParameter("Signal:", "signal", -1, false);
    IntParameter minRad_P = new IntParameter("Min radius", "minRad", minRad);
    IntParameter maxRad_P = new IntParameter("Max radius", "maxRad", maxRad);
    IntParameter step_P = new IntParameter("Step", "step", step);
    MultiParameter quantiles = new MultiParameter("Quantiles:", "quantilesMP", new Parameter[]{new SliderDoubleParameter("Quantile", "quantile", 0, 1, 0.5d, 4)}, 1, 100, 1);
    Parameter[] parameters = new Parameter[]{structure, minRad_P, maxRad_P, step_P, quantiles};
    KeyParameterStructureArray mean_K = new KeyParameterStructureArray("Mean Intensity:", "meanIntensity", "granulo_mean", true);
    //KeyParameter maxVarRad_K = new KeyParameter("Maximum Variation Radius:", "maxRad", "max_variation_radius", true, MeasurementStructure.Number);
    KeyParameterStructureArray sd_K = new KeyParameterStructureArray("SD Intensity:", "sdIntensity", "granulo_sd", true);
    KeyParameter[] keys = new KeyParameter[]{mean_K, sd_K};

    @Override
    public int[] getStructures() {
        return new int[]{structure.getIndex()};
    }

    @Override
    public void setMultithread(int nbCPUs) {
        this.nbCPUs = nbCPUs;
    }

    @Override
    public void setVerbose(boolean verbose) {
        this.verbose = verbose;
    }

    @Override
    public void getMeasure(InputCellImages rawImages, SegmentedCellImages segmentedImages, StructureQuantifications quantifs) {
        ImageHandler input = rawImages.getImage(structure.getIndex());
        double scale = input.getScaleXY() / input.getScaleZ();
        ImageInt mask = rawImages.getMask();
        maxRad = maxRad_P.getIntValue(maxRad);
        minRad = minRad_P.getIntValue(minRad);
        step = step_P.getIntValue(step);
        int nbRad = (maxRad - minRad) / step;
        mean = new double[nbRad];
        sd = new double[nbRad];
        double[] quants = new double[nbRad];
        // TODO faire les quantiles
        ImageHandler temp = ImageHandler.newBlankImageHandler("temp", input);
        ImageHandler open = ImageHandler.newBlankImageHandler("open", input);
        ImageHandler current = input;
        Object3DVoxels maskObject = mask.getObjects3D()[0];
        double[] qts = new double[quantiles.getNbParameters()];
        int idx = 0;
        for (Parameter p : quantiles.getParameters()) {
            qts[idx++] = ((SliderDoubleParameter) p).getValue();
        }
        for (int i = 0; i < nbRad; i++) {
            int radXY = minRad + i * step;
            int radZ = (int) Math.max(((double) radXY * scale + 0.5), 1);
            //if (radXY>=1) current=input.grayscaleOpen(radXY, radZ, open, temp, false);
            if (radXY >= 1) {
                current = FastFilters3D.filterImage(input, FastFilters3D.OPENGRAY, radXY, radXY, radZ, 0, false);
            }
            if (verbose) {
                current.showDuplicate("Granulometry open#" + (i + 1) + " radXY: " + radXY + " radZ: " + radZ);
            }
            current.resetStats(mask);
            ImageStats stats = current.getImageStats(mask);
            this.mean[i] = stats.getMean();
            this.sd[i] = stats.getStandardDeviation();
            quants[i] = Quantiles.getQuantiles(maskObject, current, qts)[0];
        }
        if (verbose) {
            double[] steps = new double[mean.length];
            for (int i = 0; i < steps.length; i++) {
                steps[i] = minRad + i * step;
            }
            Plot p = new Plot("Granulometry", "radius", "mean value", steps, mean);
            p.show();
            Plot p2 = new Plot("Granulometry", "radius", "quantile value", steps, quants);
            p2.show();
        }
        // analyse de la distribution.... 
        if (mean_K.isSelected()) {
            quantifs.setQuantificationStructureArray(mean_K, mean);
        }
        if (sd_K.isSelected()) {
            quantifs.setQuantificationStructureArray(sd_K, sd);
        }
    }

    @Override
    public KeyParameter[] getKeys() {
        return keys;
    }

    @Override
    public Parameter[] getParameters() {
        return parameters;
    }

    @Override
    public String getHelp() {
        return "";
    }
}
