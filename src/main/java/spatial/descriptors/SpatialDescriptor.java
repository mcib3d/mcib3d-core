/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package spatial.descriptors;

import mcib3d.geom.Objects3DPopulation;
import mcib3d.utils.ArrayUtil;

/**
 *
 * @author thomasb
 */
public interface SpatialDescriptor {

    public boolean init();

    public ArrayUtil compute(Objects3DPopulation pop);

    public String getName();

}
