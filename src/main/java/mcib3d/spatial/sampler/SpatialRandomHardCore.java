/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mcib3d.spatial.sampler;

import java.util.Random;
import mcib3d.geom.Object3D;
import mcib3d.geom.Object3DVoxels;
import mcib3d.geom.ObjectCreator3D;
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
public class SpatialRandomHardCore implements SpatialModel {

    private int nbObjects;
    private double distHardCore;// in pixels
    private Object3D mask;
    // init values
    private Object3DVoxels maskVox;
    private ImageInt maskimg;

    public SpatialRandomHardCore(int nbObjects, double distHardCore, Object3D mask) {
        this.nbObjects = nbObjects;
        this.distHardCore = distHardCore;
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
        Random ra = new Random();
        ImageInt maskImgTmp = maskimg.duplicate();
        ObjectCreator3D create = new ObjectCreator3D(maskImgTmp);
        for (int i = 0; i < nbObjects; i++) {
            Voxel3D vox = maskVox.getRandomvoxel(ra);
            while (maskImgTmp.getPixel(vox) == 0) {
                vox = maskVox.getRandomvoxel(ra);
            }
            points[i] = vox;
            create.createSphere(vox.getRoundX(), vox.getRoundY(), vox.getRoundZ(), distHardCore, 0, false);
        }
        pop.addPoints(points);

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
        return "Random Sampling with Hard Core distance";
    }

    public Object3D getMask() {
        return mask;
    }

}
