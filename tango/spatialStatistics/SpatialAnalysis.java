package tango.spatialStatistics;

import tango.spatialStatistics.SDIEvaluator.SDIEvaluator;
import tango.spatialStatistics.spatialDescriptors.SpatialDescriptor;
import ij.ImagePlus;
import java.util.ArrayList;
import java.util.HashMap;
import mcib3d.geom.Object3D;
import tango.dataStructure.InputCellImages;
import tango.dataStructure.SegmentedCellImages;
import tango.dataStructure.StructureQuantifications;
import tango.parameter.*;
import tango.plugin.measurement.MeasurementStructure;
import tango.plugin.PluginFactory;
import tango.spatialStatistics.SDIEvaluator.SDIEvaluatorFactory;
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
public class SpatialAnalysis implements MeasurementStructure {
    int ns=500;
    SpatialDescriptorParameter descriptors = new SpatialDescriptorParameter("Spatial Descriptor:", "spatialDescriptor", null);
    SDIEvaluatorParameter evaluatorParam = new SDIEvaluatorParameter("SDI Evaluator:", "sdiEvaluator", SDIEvaluatorFactory.evaluators[0]);
    MultiParameter evaluators = new MultiParameter("SDI Evaluators", "sdiEvaluators", new Parameter[]{evaluatorParam}, 0, 100, 1);
    IntParameter nbSamples = new IntParameter("Number of Samples", "nbSamples", ns);
    String currentDescriptor, currentEvaluator;
    Parameter[] parameters= new Parameter[] {descriptors, nbSamples, evaluators};
    GroupKeyParameter keys = new GroupKeyParameter("Evaluators:", "evaluators", "", true, null, false);
    int nCPUs=1;
    boolean verbose;
    
    public SpatialAnalysis() {
        evaluators.getSpinner().setFireChangeOnAction();
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
    public int[] getStructures() {
        SpatialDescriptor sd = descriptors.getPlugin(nCPUs, verbose);
        if (sd!=null) return sd.getStructures();
        else return new int[0];
    }

    @Override
    public void getMeasure(InputCellImages raw, SegmentedCellImages seg, StructureQuantifications quantifs) {
        Parameter[] evals = evaluators.getParameters();
        SpatialDescriptor sd = descriptors.getPlugin(nCPUs, verbose);
        if (sd!=null && runMeasurement(evals)) {
            sd.run(nbSamples.getIntValue(ns), raw, seg);
            double[][] sampledDesc = sd.getSampleDescriptor();
            double[] observedDesc = sd.getObservedDescriptor();
            if (sampledDesc!=null && observedDesc!=null) {
                sd.getCurves(quantifs);
                for (Parameter p : evals) {
                    SDIEvaluator evaluator = ((SDIEvaluatorParameter)p).getPlugin(nCPUs, verbose);
                    if (evaluator!=null) {
                        evaluator.eval(observedDesc, sampledDesc, quantifs);
                    }
                }
            }
        }
    }

    @Override
    public Parameter[] getParameters() {
        return parameters;
    }

    @Override
    public Parameter[] getKeys() {
        KeyParameter[][] matrix = new KeyParameter[evaluators.getNbParameters()][];
        ArrayList<Parameter[]> ev = evaluators.getParametersArrayList();
        for (int i = 0; i<matrix.length; i++) {
            SDIEvaluator se = ((SDIEvaluatorParameter)ev.get(i)[0]).getPlugin(nCPUs, verbose);
            if (se!=null) matrix[i]=se.getKeyParameters();
        } 
        keys.setKeys(KeyParameter.mergeKeyArrays(matrix));
        if (descriptors.getPlugin(nCPUs, verbose)!=null) return Parameter.mergeArrays(new Parameter[][]{descriptors.getPlugin(nCPUs, verbose).getKeyParameters(), new Parameter[]{keys}});
        else return new Parameter[]{keys};
    }
    
    private boolean runMeasurement(Parameter[] evals) {
        if (KeyParameter.isOneKeySelected(this.descriptors.getPlugin(nCPUs, verbose).getKeyParameters())) return true;
        for (Parameter p : evals) {
            SDIEvaluator evaluator = ((SDIEvaluatorParameter)p).getPlugin(nCPUs, verbose);
            if (evaluator!=null) {
                for (KeyParameter k : evaluator.getKeyParameters()) {
                    if (k.isSelected()) return true;
                }
            }
        }
        return false;
    }


    @Override
    public String getHelp() {
        return "spatial analysis help";
    }
    
}
