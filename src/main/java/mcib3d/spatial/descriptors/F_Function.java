/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mcib3d.spatial.descriptors;

import java.util.Random;
import mcib3d.geom.Object3D;
import mcib3d.geom.Object3DVoxels;
import mcib3d.geom.Objects3DPopulation;
import mcib3d.geom.Point3D;
import mcib3d.utils.ArrayUtil;

/**
 *
 * @author thomasb
 */
public class F_Function implements SpatialDescriptor {
    
    private int nbEvaluationpoints;
    private Point3D[] evaluationPoints;
    private Object3D mask;
    
    public F_Function(int nb, Object3D ma) {
        nbEvaluationpoints = nb;
        mask = ma;
    }
    
    @Override
    public boolean init() {
        Random ra = new Random();
        Object3DVoxels maskV = new Object3DVoxels(mask);
        evaluationPoints = new Point3D[nbEvaluationpoints];
        // TODO, may use double coordinates
        // FIXME, check if already selected and too many points
        for (int i = 0; i < nbEvaluationpoints; ++i) {
            evaluationPoints[i] = maskV.getRandomVoxel(ra);
        }
        
        return true;
    }
    
    @Override
    public ArrayUtil compute(Objects3DPopulation pop) {
        pop.createKDTreeCenters();
        return pop.computeDistances(evaluationPoints);
    }
    
    @Override
    public String getName() {
        return "F";
    }
   
    
}
