package tango.spatialStatistics.spatialDescriptors;

import ij.ImagePlus;
import ij.measure.Calibration;
import java.util.HashMap;
import mcib3d.geom.Object3D;
import mcib3d.geom.Point3D;
import mcib3d.image3d.ImageHandler;
import tango.dataStructure.InputCellImages;
import tango.dataStructure.SegmentedCellImages;
import tango.dataStructure.StructureQuantifications;
import tango.parameter.*;
import tango.processing.geodesicDistanceMap.GeodesicMap;
import tango.spatialStatistics.CumulativeCurves;
import tango.spatialStatistics.StochasticProcess.RandomPoint3DGenerator;
import tango.spatialStatistics.StochasticProcess.RandomPoint3DGeneratorUniform;
import tango.spatialStatistics.util.KDTreeC;
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
public class GFunctionGeodesic implements SpatialDescriptor {
    CumulativeCurves curves = new CumulativeCurves();
    StructureParameter structure = new StructureParameter("Observed Objetcts:", "structure1", -1, true);
    SamplerParameter sampler = new SamplerParameter("Sampled Objects:", "sample1", -1);
    Parameter[] defaultParameters=new Parameter[]{structure, sampler, curves.getParameters()};
    Parameter[] parameters;
    GeodesicMap gdm;
    double[] observedDescriptor;
    double[][] sampleDescriptor;
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
    @Override
    public void run(int nbSamples, InputCellImages raw, SegmentedCellImages seg) {
        gdm.init(raw, nCPUs, verbose);
        sampler.initSampler(raw, seg, nCPUs, verbose);
        Object3D[] observedO = structure.getObjects(seg);
        observedDescriptor = eval(observedO);
        sampleDescriptor = new double[nbSamples][];
        for (int i = 0; i < nbSamples; i++) {
            sampleDescriptor[i] = eval(sampler.getSample());
            if (sampleDescriptor[i]==null) {
                sampleDescriptor=null;
                return;
            }
        }
    }
    
    protected double[] eval(Object3D[] objects) {
        if (objects==null) return null;
        double[] res = new double[objects.length];
        if (objects.length<=1) return res; 
        gdm.run(objects, true, false);
        for (int i = 0; i<objects.length; i++) {
            gdm.removeSeedAndRun(i);
            res[i]=gdm.getDistance(objects[i].getCenterAsPoint());
        }
        return res;
    }

    @Override
    public Parameter[] getParameters() {
        if (gdm==null) gdm=new GeodesicMap();
        Parameter[] gdmparam=gdm.getParameters();
        parameters=new Parameter[defaultParameters.length+gdmparam.length];
        System.arraycopy(defaultParameters, 0, parameters, 0, defaultParameters.length);
        System.arraycopy(gdmparam, 0, parameters, defaultParameters.length, gdmparam.length);
        return parameters;
    }

    @Override
    public int[] getStructures() {
        return new int[] {structure.getIndex()};
    }

    @Override
    public String getHelp() {
        return "";
    }

    @Override
    public Parameter[] getKeyParameters() {
        return new Parameter[]{curves.getKeys()};
    }
    
    @Override
    public double[] getObservedDescriptor() {
        return observedDescriptor;
    }

    @Override
    public double[][] getSampleDescriptor() {
        return sampleDescriptor;
    }

    @Override
    public void getCurves(StructureQuantifications quantifs) {
        curves.getCurves(observedDescriptor, sampleDescriptor, quantifs, verbose);
    }
    
}
