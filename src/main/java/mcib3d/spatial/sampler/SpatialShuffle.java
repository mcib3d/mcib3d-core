/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mcib3d.spatial.sampler;

import java.util.ArrayList;
import mcib3d.geom.Object3D;
import mcib3d.geom.Objects3DPopulation;
import mcib3d.image3d.ImageHandler;
import mcib3d.image3d.ImageShort;

/**
 *
 * @author thomasb
 */
public class SpatialShuffle implements SpatialModel {

    private Objects3DPopulation population;

    public SpatialShuffle(Objects3DPopulation pop) {
        population = pop;
    }

    @Override
    public boolean init() {
        return true;
    }

    @Override
    public Objects3DPopulation getSample() {
        ArrayList<Object3D> shuObj = population.shuffle();

        return new Objects3DPopulation(shuObj);
    }

    @Override
    public ImageHandler getSampleImage() {
        Object3D mask = population.getMask();
        ImageHandler tmp = new ImageShort(getName(), mask.getXmax() + 1, mask.getYmax() + 1, mask.getZmax() + 1);
        getSample().draw(tmp);

        return tmp;
    }

    @Override
    public String getName() {
        return "Shuffle";
    }

}
