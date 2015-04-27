package tango.plugin.measurement;

import ij.ImagePlus;
import ij.WindowManager;
import ij.gui.GenericDialog;
import ij.measure.ResultsTable;
import ij.plugin.filter.PlugInFilter;
import ij.process.ImageProcessor;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import mcib3d.geom.Object3D;
import mcib3d.geom.Voxel3D;
import mcib3d.image3d.ImageByte;
import mcib3d.image3d.ImageHandler;
import mcib_plugins.analysis.simpleMeasure;
import tango.dataStructure.InputCellImages;
import tango.dataStructure.ObjectQuantifications;
import tango.dataStructure.SegmentedCellImages;
import tango.gui.parameterPanel.MultiParameterPanel;
import tango.parameter.*;
import tango.plugin.measurement.Measurement;
import tango.plugin.measurement.MeasurementObject;

/**
 *
 **
 * /**
 * Copyright (C) 2012 Jean Ollion
 *
 *
 *
 * This file is part of TANGO
 *
 * TANGO is free software; you can redistribute it and/or modify it under the
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
public class Quantiles implements MeasurementObject {

    StructureParameter structureObject = new StructureParameter("Structure objects:", "structure", 0, true);
    StructureParameter structureSignal = new StructureParameter("Structure signal:", "structureSignal", -1, false);
    MultiParameter quantiles = new MultiParameter("Quantiles:", "quantilesMP", new Parameter[]{new SliderDoubleParameter("Quantile", "quantile", 0, 1, 0.5d, 4)}, 1, 100, 1);
    PreFilterSequenceParameter preFilters = new PreFilterSequenceParameter("Pre-Filters", "preFilters");
    Parameter[] parameters = new Parameter[]{structureObject, structureSignal, preFilters, quantiles};
    KeyParameterObjectNumber[] keys = new KeyParameterObjectNumber[0];
    GroupKeyParameter group = new GroupKeyParameter("", "quantilesKeys", "", true, keys, false);
    Parameter[] returnKeys = new Parameter[]{group};
    int nCPUs=1;
    boolean verbose;
    
    public Quantiles ( ) {
        quantiles.getSpinner().setFireChangeOnAction();
        
    }
    
    @Override
    public void setMultithread(int nbCPUs) {
        this.nCPUs=nbCPUs;
    }
    
    @Override
    public void setVerbose(boolean verbose) {
        this.verbose=verbose;
    }

    @Override
    public void getMeasure(InputCellImages raw, SegmentedCellImages seg, ObjectQuantifications quantifications) {
        ImageHandler signal = raw.getImage(structureSignal.getIndex());
        signal=preFilters.runPreFilterSequence(structureSignal.getIndex(), signal, raw, nCPUs, verbose);
        Object3D[] objects = seg.getObjects(structureObject.getIndex());
        int nbQuantiles = quantiles.getNbParameters();
        double[] qts = new double[nbQuantiles];
        int idx = 0;
        for (Parameter p : quantiles.getParameters()) qts[idx++] = ((SliderDoubleParameter)p).getValue();
        double[][] mes = new double[objects.length][nbQuantiles];
        for (int i = 0; i<objects.length; i++) mes[i] = getQuantiles(objects[i], signal, qts);
        for (int q = 0; q<nbQuantiles; q++) {
            if (this.keys[q].isSelected()) {
                double[] m = new double[objects.length];
                for (int i = 0; i<objects.length; i++) m[i]=mes[i][q];
                quantifications.setQuantificationObjectNumber(keys[q], m);
            }
        }
    }
    
    public static double[] getQuantiles(Object3D mask, ImageHandler signal, double[] quantiles) {
        double[] mes = new double[quantiles.length];
        ArrayList<Voxel3D> vox = mask.getVoxels();
        if (vox.size()>1000) { // using histo (approx)
            double[] minMax = signal.getMinAndMaxArray(vox);
            int[] histo = signal.getHistogram(vox, 256, minMax[0], minMax[1]);
            double binSize = (minMax[1] - minMax[0]) / 256d;
            int vol = vox.size();
            for (int quantIdx = 0; quantIdx < quantiles.length; quantIdx++) {
                int index = (int) (quantiles[quantIdx] * vol+0.5);
                int histIdx=-1;
                int count = 0;
                //System.out.println("quantile:"+quantIdx+ " quantile:"+quantile + " index"+index);
                if (index<=0) mes[quantIdx]=minMax[0];
                else if (index>=vol) mes[quantIdx]= minMax[1];
                else {
                    while (count<index) count+=histo[++histIdx];
                    if (count==index) mes[quantIdx]= ((histIdx+1)*binSize+minMax[0]);
                    else { //linear approx between 2 bins
                        double val1 = histIdx*binSize+minMax[0];
                        double val2 = val1+binSize;
                        double p = (double)(count-index) / histo[histIdx];
                        mes[quantIdx] =  ((1-p) * val2 + (p) * val1);
                        //System.out.println("approx: val1;"+val1+ " val2:"+val2 + " res"+mes[quantIdx]+ " count:"+count+ " histo:"+histo[histIdx]+ " histIdx"+histIdx);
                    }
                }
            }
        } else { // sorting values
            float[] vals = mask.getArrayValues(signal);
            Arrays.sort(vals);
            for (int quantIdx = 0; quantIdx<quantiles.length; quantIdx++) {
                double index = quantiles[quantIdx] * vals.length;
                if (index<=0) mes[quantIdx]=(double)vals[0];
                else if (index>=(vals.length-1)) mes[quantIdx]= (double)  vals[vals.length-1];
                else {
                double d = index-(int)index;
                    if (d==0) mes[quantIdx]= (double) vals[(int)index];
                    else {
                        double val1 = vals[(int)index];
                        double val2 = vals[(int)(index+1)];
                        mes[quantIdx] = (d*val2+(1-d)*val1);
                    }
                }
            }
        }
        return mes;
    }

    @Override
    public Parameter[] getParameters() {
        return parameters;
    }

    @Override
    public Parameter[] getKeys() {
        if (quantiles.getNbParameters()!=keys.length) {
            KeyParameterObjectNumber[] newKeys = new KeyParameterObjectNumber[quantiles.getNbParameters()];
            if (quantiles.getNbParameters()<keys.length) {
                System.arraycopy(keys, 0, newKeys, 0, newKeys.length);
            } else {
                System.arraycopy(keys, 0, newKeys, 0, keys.length);
                for (int i = keys.length ; i<newKeys.length; i++ ) newKeys[i]=new KeyParameterObjectNumber("Quantile "+(i+1)+ ":", "quantile"+(i+1), "quantile"+(i+1), true);
            }
            keys=newKeys;
            this.group.setKeys(keys);
        }
        return returnKeys;
    }

    @Override
    public int getStructure() {
        return structureObject.getIndex();
    }

    @Override
    public String getHelp() {
        return "";
    }
}
