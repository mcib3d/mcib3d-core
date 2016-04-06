/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mcib3d.spatial.sampler;

import mcib3d.geom.Objects3DPopulation;
import mcib3d.image3d.ImageHandler;

/**
 *
 * @author thomasb
 */
public interface SpatialModel {

    public boolean init();

    Objects3DPopulation getSample();

    ImageHandler getSampleImage();

    String getName();
}
