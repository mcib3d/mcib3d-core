package tango.spatialStatistics.spatialDescriptors;

import ij.ImagePlus;
import ij.measure.Calibration;
import java.util.HashMap;
import mcib3d.geom.Object3D;
import mcib3d.geom.Point3D;
import mcib3d.image3d.ImageFloat;
import mcib3d.image3d.ImageHandler;
import mcib3d.image3d.ImageInt;
import mcib3d.image3d.distanceMap3d.EDT;
import tango.dataStructure.InputCellImages;
import tango.dataStructure.SegmentedCellImages;
import tango.dataStructure.StructureQuantifications;
import tango.parameter.*;
import tango.processing.geodesicDistanceMap.GeodesicMap;
import tango.spatialStatistics.CumulativeCurves;
import tango.spatialStatistics.StochasticProcess.RandomPoint3DGenerator;
import tango.spatialStatistics.StochasticProcess.RandomPoint3DGeneratorUniform;
//import tango.spatialStatistics.util.KDTreeC;
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
public class G2ToObservedStructure implements SpatialDescriptor {
    CumulativeCurves curves = new CumulativeCurves();
    StructureParameter structure = new StructureParameter("Observed Objects:", "structure1", -1, true);
    SamplerParameter sampler = new SamplerParameter("Sampled Objects:", "sample1", -1);
    StructureParameter structure2 = new StructureParameter("Distance To Structure:", "structure2", -1, true);
    BooleanParameter geodesic = new BooleanParameter("Geodesic Distance: ", "geodesic", false);
    BooleanParameter negative = new BooleanParameter("Negative value when inside Structure: ", "negative", false);
    GeodesicMap gdm=new GeodesicMap();
    HashMap<Object, Parameter[]> map = new HashMap<Object, Parameter[]>(){{
        put(false, new Parameter[]{negative}); 
        put(true, gdm.getParameters());
    }};
    ConditionalParameter action = new ConditionalParameter(geodesic, map);
    Parameter[] defaultParameters = new Parameter[] {structure, sampler, structure2, action, curves.getParameters()};
    ImageFloat distanceMap;
    ImageInt mask;
    double[] observedDescriptor;
    double[][] sampleDescriptor;
    int nCPUs=1;
    boolean verbose;
    
    public G2ToObservedStructure() {
        geodesic.setFireChangeOnAction();
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
    public void run(int nbSamples, InputCellImages raw, SegmentedCellImages seg) {
        sampler.initSampler(raw, seg, nCPUs, verbose);
        mask=raw.getMask();
        Object3D[] observedO = structure.getObjects(seg);
        // TODO proposer le choix inside/oustide (et si les 2 negative inside)
        if (!geodesic.isSelected()) {
            if (negative.isSelected()) distanceMap = EDT.run_includeInside(structure2.getImage(seg, false), 0, !negative.isSelected(), nCPUs);
            else distanceMap = seg.getDistanceMap(structure2.getIndex(), nCPUs);
        }
        else {
            gdm.init(raw, nCPUs, verbose);
            gdm.run(structure2.getObjects(seg), false, true);
            // TODO: negative inside?? parcours de la map a posteriori
            distanceMap=gdm.getDistanceMap();
        }
        //distanceMap.showDuplicate("distanceMap");
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
        double[] res = new double[objects.length];
        for (int i = 0; i<objects.length; i++) {
            Point3D p = objects[i].getCenterAsPoint();
            res[i]=distanceMap.getPixel((float)p.getX(), (float)p.getY(), (float)p.getZ(), mask);
        }
        return res;
    }

    @Override
    public Parameter[] getParameters() {
        if (geodesic.isSelected()) {
            if (gdm==null) gdm=new GeodesicMap();
            Parameter[] gdmparam=gdm.getParameters();
            Parameter[] parameters=new Parameter[defaultParameters.length+gdmparam.length];
            System.arraycopy(defaultParameters, 0, parameters, 0, defaultParameters.length);
            System.arraycopy(gdmparam, 0, parameters, defaultParameters.length, gdmparam.length);
            return parameters;
        } else {
            Parameter[] parameters=new Parameter[defaultParameters.length+1];
            System.arraycopy(defaultParameters, 0, parameters, 0, defaultParameters.length);
            parameters[defaultParameters.length]=negative;
            return parameters;
        }
    }

    @Override
    public int[] getStructures() {
        return new int[] {structure.getIndex(), structure2.getIndex()};
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
