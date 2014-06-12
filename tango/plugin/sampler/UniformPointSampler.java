package tango.plugin.sampler;

import mcib3d.geom.Object3D;
import mcib3d.geom.Point3D;
import tango.spatialStatistics.StochasticProcess.RandomPoint3DGenerator;
import tango.spatialStatistics.StochasticProcess.RandomPoint3DGeneratorProbaMap;
import tango.spatialStatistics.StochasticProcess.RandomPoint3DGeneratorUniform;
import mcib3d.image3d.ImageHandler;
import tango.dataStructure.InputCellImages;
import tango.dataStructure.SegmentedCellImages;
import tango.dataStructure.Structure;
import tango.parameter.*;
import tango.plugin.sampler.Sampler;
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
public class UniformPointSampler implements Sampler {
    final static int maxIterationsSample = 1000;
    final static int maxIterationsPoint = 1000;
    BooleanParameter constantNumberofPoints= new BooleanParameter("Constant Number of Points", "constantPointNb", true);
    ConditionalParameter cond = new ConditionalParameter(constantNumberofPoints);
    IntParameter nbPoints = new IntParameter("Number of Points:", "nbPoints", null);
    StructureParameter nbPointsChannel  = new StructureParameter("Same number of points as :", "nPointsChannel", -1, true);
    DoubleParameter hardcore = new DoubleParameter("Hardcore distance (unit):", "hardcore", 0d, Parameter.nfDEC5);
    Parameter[] parameters = new Parameter[] {cond, hardcore};
    
    RandomPoint3DGenerator rpg;
    protected boolean multithread;
    
    public UniformPointSampler() {
        cond.setCondition(true, new Parameter[]{nbPoints});
        cond.setCondition(false, new Parameter[]{nbPointsChannel});
    }
    
    @Override
    public Parameter[] getParameters() {
       return parameters;
    }

    boolean verbose;
    int nbCPUs=1;
    @Override
    public void setMultithread(int nbCPUs) {
        this.nbCPUs=nbCPUs;
    }
    
    @Override
    public void setVerbose(boolean verbose) {
        this.verbose=verbose;
    }
    
    @Override
    public void initSampler(InputCellImages raw, SegmentedCellImages seg) {
        int nPoints;
        int nbPointChannel = nbPointsChannel.getIndex();
        if (nbPointChannel>0) nPoints=nbPointsChannel.getObjects(seg).length;
        else nPoints=nbPoints.getIntValue(0);
        float hard = hardcore.getFloatValue(0);
        rpg = new RandomPoint3DGeneratorUniform(seg.getImage(0), nPoints, nbCPUs, verbose);
        if (hard>0) rpg.setHardCore(hard);
    }

    @Override
    public Object3D[] getSample() {
        rpg.resetPoints();
        return rpg.drawObjects(maxIterationsPoint, maxIterationsSample);
    }
    
    @Override
    public String getHelp() {
        return "Generates random points unfiormly within the nucleus space";
    }

    @Override
    public void displaySample() {
        rpg.showPoints("uniform sampler", getSample()).show();
    }
    
}
