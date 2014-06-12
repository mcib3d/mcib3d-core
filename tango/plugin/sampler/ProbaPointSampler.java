package tango.plugin.sampler;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import ij.ImagePlus;
import ij.measure.Calibration;
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
public class ProbaPointSampler implements Sampler {
    final static int maxIterationsSample = 1000;
    final static int maxIterationsPoint = 1000;
    IntParameter nbPoints = new IntParameter("Number of Points:", "nbPoints", null);
    StructureParameter nbPointsChannel  = new StructureParameter("Same number of points as :", "nPointsChannel", -1, true);
    DoubleParameter hardcore = new DoubleParameter("Hardcore distance (unit):", "hardcore", 0.15d, Parameter.nfDEC5);
    StructureParameter probaMap = new StructureParameter("Probability Map:", "probaMap", -1, false);
    BooleanParameter invert = new BooleanParameter("Invert Probability Map:", "invert", false);
    DoubleParameter saturation = new DoubleParameter("Proba. Map % saturation:", "saturation", 0d, Parameter.nfDEC5);
    BooleanParameter gradient = new BooleanParameter("Use P.M. Gradient:", "gradient", false);
    DoubleParameter gScale = new DoubleParameter("Gradient Scale (pixel):", "gScale", 1d, Parameter.nfDEC1);
    
    Parameter[] parameters = new Parameter[] {nbPoints, nbPointsChannel, hardcore, probaMap, invert, saturation, gradient, gScale};
    RandomPoint3DGenerator rpg;
    protected boolean multithread;
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
        ImageHandler pm = probaMap.getImage(raw, false);
        if (gradient.isSelected()) {
            pm = pm.getGradient(gScale.getFloatValue(1), nbCPUs);
        }
        rpg = new RandomPoint3DGeneratorProbaMap(seg.getImage(0), nPoints, pm, saturation.getFloatValue(0), nbCPUs, verbose);
        if (hard>0) rpg.setHardCore(hard);
    }

    @Override
    public Object3D[] getSample() {
        rpg.resetPoints();
        return rpg.drawObjects(maxIterationsPoint, maxIterationsSample);
    }
    
    @Override
    public String getHelp() {
        return "Generates random points with a probability that depends on the intensity level";
    }

    @Override
    public void displaySample() {
        rpg.showPoints("proba sampler", getSample()).show();
    }
    
}
