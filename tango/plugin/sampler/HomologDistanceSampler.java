package tango.plugin.sampler;

import tango.spatialStatistics.StochasticProcess.RPGHomologConstraint;
import tango.spatialStatistics.StochasticProcess.RandomPoint3DGenerator;
import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import ij.ImagePlus;
import ij.measure.Calibration;
import mcib3d.geom.Object3D;
import mcib3d.geom.Point3D;
import mcib3d.image3d.ImageFloat;
import mcib3d.image3d.ImageHandler;
import mcib3d.image3d.ImageInt;
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
public class HomologDistanceSampler implements Sampler {
    final static int maxIterationsSample = 1000;
    final static int maxIterationsPoint = 1000;
    protected boolean multithread;
    StructureParameter referenceObjects_P = new StructureParameter("Reference Objects:", "objects", 0, true);
    
    Parameter[] parameters = new Parameter[] {referenceObjects_P};
    RandomPoint3DGenerator rpg;
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
        Object3D[] referenceObjects = referenceObjects_P.getObjects(seg);
        float constraint = (referenceObjects.length>1) ? (float)referenceObjects[0].distCenterUnit(referenceObjects[1]) : 0;
        rpg = new RPGHomologConstraint(seg.getImage(0), 2, constraint, nbCPUs, verbose);
    }

    @Override
    public Object3D[] getSample() {
        rpg.resetPoints();
        return rpg.drawObjects(maxIterationsPoint, maxIterationsSample);
    }
    
    @Override
    public String getHelp() {
        return "Generates 2 points with a constrained distance beteens them";
    }
    
    @Override
    public void displaySample() {
        rpg.showPoints("homolog sampler", getSample()).show();
    }
    
}
