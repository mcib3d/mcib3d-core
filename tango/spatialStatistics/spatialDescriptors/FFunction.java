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
public class FFunction implements SpatialDescriptor {
    CumulativeCurves curves = new CumulativeCurves();
    StructureParameter structure = new StructureParameter("Observed Objetcts:", "structure1", -1, true);
    SamplerParameter sampler = new SamplerParameter("Sampled Objects:", "sample1", -1);
    IntParameter nbEvalPoints = new IntParameter("Number of evaluation Points: ", "nbEval", 10000);
    Parameter[] parameters = new Parameter[] {structure, sampler, nbEvalPoints, curves.getParameters()};
    Calibration cal;
    Point3D[] evaluationPoints;
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
        cal = seg.getImage(0).getImagePlus().getCalibration();
        RandomPoint3DGenerator rpg = new RandomPoint3DGeneratorUniform(seg.getImage(0), nbEvalPoints.getIntValue(10000), nCPUs, verbose);
        rpg.drawPoints(1, 1);
        evaluationPoints=rpg.points;
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
        KDTreeC kdTree = new KDTreeC(3, objects.length);
        kdTree.setScaleSq(new double[]{cal.pixelWidth*cal.pixelWidth, cal.pixelWidth*cal.pixelWidth, cal.pixelDepth*cal.pixelDepth});
        for (Object3D o : objects) {
            Point3D p = o.getCenterAsPoint();
            kdTree.add(new double[]{p.getX(), p.getY(), p.getZ()}, p);
        }
        double[] res = new double[evaluationPoints.length];
        for (int i = 0; i<res.length; i++) {
            KDTreeC.Item[] items = kdTree.getNearestNeighbor(new double[]{evaluationPoints[i].getX(), evaluationPoints[i].getY(), evaluationPoints[i].getZ()}, 1);
            res[i]=Math.sqrt(items[0].distance);
        }
        return res;
    }

    @Override
    public Parameter[] getParameters() {
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
