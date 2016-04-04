/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package spatial.sampler;

import mcib3d.geom.Object3D;
import mcib3d.geom.Objects3DPopulation;

/**
 *
 * @author thomasb
 */
public class SpatialRandom implements SpatialModel {

    private int nbObjects;
    private double distHardCore;// in pixels
    private Object3D mask;

    public SpatialRandom(int nbObjects, double distHardCore, Object3D mask) {
        this.nbObjects = nbObjects;
        this.distHardCore = distHardCore;
        this.mask = mask;
    }

    @Override
    public boolean init() {
        return true;
    }

    @Override
    public Objects3DPopulation getSample() {
        Objects3DPopulation pop = new Objects3DPopulation();
        pop.setMask(mask);
        pop.createRandomPopulation(nbObjects, distHardCore);

        return pop;
    }

}
