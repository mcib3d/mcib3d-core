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
public class Histogram implements MeasurementStructure {
    boolean verbose;
    int nbCPUs=1;
    StructureParameter structure = new StructureParameter("Structure:", "structure", -1, false);
    PreFilterSequenceParameter filters = new PreFilterSequenceParameter("Filters: ", "filters");
    BooleanParameter filtered = new BooleanParameter("Use filtered image:", "filtered", false);
    IntParameter bins = new IntParameter("Nb bins:", "nBins", 255);
    Parameter[] parameters = new Parameter[]{structure, filtered, filters, bins};
    KeyParameterStructureArray x = new KeyParameterStructureArray("X:", "x", "x", true);
    KeyParameterStructureArray y = new KeyParameterStructureArray("Y:", "y", "y", true);
    GroupKeyParameter keys = new GroupKeyParameter("Histogram:", "histo", "histo", true, new KeyParameter[]{x, y}, false);
    @Override
    public int[] getStructures() {
        return new int[]{structure.getIndex()};
    }

    @Override
    public void getMeasure(InputCellImages rawImages, SegmentedCellImages segmentedImages, StructureQuantifications quantifs) {
        if (structure.getIndex()==-1) {
            ij.IJ.log("Histogram measurement: no structure selected!");
            return;
        }
        ImageHandler input = (filtered.isSelected())? rawImages.getFilteredImage(structure.getIndex()):rawImages.getImage(structure.getIndex());
        ImageHandler filteredImage = filters.runPreFilterSequence(structure.getIndex(), input, rawImages, nbCPUs, false);
        double min = filteredImage.getMin(rawImages.getMask());
        double max = filteredImage.getMax(rawImages.getMask());
        int[] histo = filteredImage.getHistogram(rawImages.getMask(), bins.getIntValue(255), min, max);
        double binSize =  (max - min) / bins.getIntValue(255);
        double[] xa = new double[histo.length];
        for (int i = 0; i<xa.length; i++) {
            xa[i]=min + i * binSize;
        }
        if (Core.debug) {
            double[] y2 = new double[xa.length];
            for (int i = 0; i<xa.length; i++) {
                y2[i]=histo[i];
            } 
            Plot p = new Plot ("HistoGram:", "value", "count", xa, y2);
            p.show();
            filteredImage.showDuplicate("Histogram input");
        } 
        
        quantifs.setQuantificationStructureArray(y, histo);
        quantifs.setQuantificationStructureArray(x, xa);
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
        return "Stores Histogram of the structure's channel images, after applying filters, within the mask";
    }
    
}
