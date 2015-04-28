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
import tango.parameter.IntParameter;
import tango.parameter.Parameter;
import tango.parameter.SamplerParameter;
import tango.parameter.StructureParameter;
import tango.spatialStatistics.CumulativeCurves;
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
public class H2 implements SpatialDescriptor {
    CumulativeCurves curves = new CumulativeCurves();
    StructureParameter structure1 = new StructureParameter("Observed Objetcts 1:", "structure1", -1, true);
    StructureParameter structure2 = new StructureParameter("Observed Objetcts 2:", "structure2", -1, true);
    SamplerParameter sampler1 = new SamplerParameter("Sampled Objects 1:", "sample1", -1);
    SamplerParameter sampler2 = new SamplerParameter("Sampled Objects 2:", "sample2", -1);
    Parameter[] parameters = new Parameter[] {structure1, structure2, sampler1, sampler2, curves.getParameters()};
    
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
        sampler1.initSampler(raw, seg, nCPUs, verbose);
        sampler2.initSampler(raw, seg, nCPUs, verbose);
        Object3D[] observedO1 = structure1.getObjects(seg);
        Object3D[] observedO2 = structure2.getObjects(seg);
        observedDescriptor = eval(observedO1, observedO2);
        sampleDescriptor = new double[nbSamples][];
        for (int i = 0; i < nbSamples; i++) {
            sampleDescriptor[i] = eval(sampler1.getSample(), sampler2.getSample());
            if (sampleDescriptor[i]==null) {
                sampleDescriptor=null;
                return;
            }
        }
    }
    
    protected double[] eval(Object3D[] objects1, Object3D[] objects2) {
        if (objects1==null || objects2==null) return null;
        double[] res = new double[objects1.length*objects2.length];
        for (int i = 0; i<objects1.length; i++) {
            for (int j = 0; j<objects2.length; j++) {
                res[i*objects2.length+j]=objects1[i].distCenterUnit(objects2[j]);
            }
        }
        return res;
    }

    @Override
    public Parameter[] getParameters() {
        return parameters;
    }

    @Override
    public int[] getStructures() {
        return new int[]{Math.min(structure1.getIndex(), structure2.getIndex()), Math.max(structure1.getIndex(), structure2.getIndex())};
    }
    

    @Override
    public String getHelp() {
        return "Inter-distances center to center between two distinct patterns";
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
