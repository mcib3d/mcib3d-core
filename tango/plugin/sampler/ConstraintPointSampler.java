package tango.plugin.sampler;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import ij.ImagePlus;
import ij.measure.Calibration;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;
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
import tango.spatialStatistics.StochasticProcess.RPGConstraint;
import tango.spatialStatistics.constraints.Constraint;
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
public class ConstraintPointSampler implements Sampler {
    final static int maxIterationsSample = 1000;
    final static int maxIterationsPoint = 1;
    IntParameter nbPoints = new IntParameter("Number of Points:", "nbPoints", null);
    StructureParameter nbPointsChannel  = new StructureParameter("Same number of points as :", "nPointsChannel", -1, true);
    DoubleParameter resLimit = new DoubleParameter("Resolution Limit (unit):", "resLimit", 0.01d, Parameter.nfDEC5);
    MultiParameter constraints_P = new MultiParameter("Constraints", "constraints", new Parameter[]{new ConstraintParameter("", "constraint", null)}, 1, 10, 2);
    Parameter[] parameters = new Parameter[] {nbPoints, nbPointsChannel, resLimit, constraints_P};
    RandomPoint3DGenerator rpg;
    public ConstraintPointSampler() {
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
    public Parameter[] getParameters() {
       return parameters;
    }
   

    @Override
    public void initSampler(InputCellImages raw, SegmentedCellImages seg) {
        int nPoints;
        int nbPointChannel = nbPointsChannel.getIndex();
        if (nbPointChannel>0) nPoints=nbPointsChannel.getObjects(seg).length;
        else nPoints=nbPoints.getIntValue(0);
        Constraint[] constraints = new Constraint[constraints_P.getNbParameters()];
        ArrayList<Parameter[]> cs = constraints_P.getParametersArrayList();
        for (int i= 0; i<constraints.length; i++) constraints[i]=((ConstraintParameter)cs.get(i)[0]).getPlugin(nbCPUs, verbose);
        rpg = new RPGConstraint(seg.getImage(0), nPoints, constraints, resLimit.getDoubleValue(0.01), raw, seg, nbCPUs, verbose);
    }

    @Override
    public Object3D[] getSample() {
        rpg.resetPoints();
        return rpg.drawObjects(maxIterationsPoint, maxIterationsSample);
    }
    
    @Override
    public String getHelp() {
        return "Generates random points with multiple constraints";
    }
    
    @Override
    public void displaySample() {
        rpg.showPoints("constraint sampler", getSample()).show();
    }
    
}
