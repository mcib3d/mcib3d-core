/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mcib3d.spatial.sampler;

import java.util.Random;
import mcib3d.geom.Object3D;
import mcib3d.geom.Object3DVoxels;
import mcib3d.geom.Objects3DPopulation;
import mcib3d.geom.Point3D;
import mcib3d.geom.Voxel3D;
import mcib3d.image3d.ImageHandler;
import mcib3d.image3d.ImageInt;
import mcib3d.image3d.ImageShort;

/**
 *
 * @author thomasb
 */
public class SpatialRandom implements SpatialModel {

    private int nbObjects;
    private Object3D mask;
    private Object3DVoxels maskVox;
    private ImageInt maskimg;

    public SpatialRandom(int nbObjects, Object3D mask) {
        this.nbObjects = nbObjects;
        this.mask = mask;
    }

    @Override
    public boolean init() {
        maskVox = mask.getObject3DVoxels();
        maskimg = mask.getMaxLabelImage(1);

        return true;
    }

    @Override
    public Objects3DPopulation getSample() {
        Point3D[] points = new Point3D[nbObjects];
        Objects3DPopulation pop = new Objects3DPopulation();
        pop.setMask(mask);

        ImageInt maskimgTmp = maskimg.duplicate();
        Random ra = new Random();

        for (int i = 0; i < nbObjects; i++) {
            Voxel3D vox = maskVox.getRandomVoxel(ra);
            while (maskimgTmp.getPixel(vox) == 0) {
                vox = maskVox.getRandomVoxel(ra);
            }
            points[i] = vox;
            maskimgTmp.setPixel(vox, 0);
        }
        pop.addPoints(points);
        pop.setCalibration(mask.getResXY(), mask.getResZ(), mask.getUnits());

        return pop;
    }

    @Override
    public ImageHandler getSampleImage() {
        ImageHandler tmp = new ImageShort(getName(), mask.getXmax() + 1, mask.getYmax() + 1, mask.getZmax() + 1);
        getSample().draw(tmp);

        return tmp;
    }

    @Override
    public String getName() {
        return "Random Sampling";
    }

    public Object3D getMask() {
        return mask;
    }

}
