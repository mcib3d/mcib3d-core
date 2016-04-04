/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package spatial.sampler;

import mcib3d.geom.Objects3DPopulation;

/**
 *
 * @author thomasb
 */
public interface SpatialModel {
    public boolean init();
    Objects3DPopulation getSample();    
}
