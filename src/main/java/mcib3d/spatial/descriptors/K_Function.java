/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mcib3d.spatial.descriptors;

import mcib3d.geom.Object3D;
import mcib3d.geom.Objects3DPopulation;
import mcib3d.utils.ArrayUtil;

/**
 * @author thomasb
 */
public class K_Function implements SpatialDescriptor {
    private double step = 1.0;
    private double max = 1000;
    private double vol;

    public K_Function(double step, double max, Object3D mask) {
        this.step = step;
        this.max = max;
        this.vol = mask.getVolumeUnit();
    }

    @Override
    public boolean init() {
        return true;
    }

    @Override
    public ArrayUtil compute(Objects3DPopulation pop) {
        ArrayUtil allCenter = pop.distancesAllCenter();
        int nb = pop.getNbObjects();
        // normal histogram
        int[] kutil = new int[((int) Math.ceil(max / step))];
        for (int i = 0; i < kutil.length; i++) kutil[i] = 0;
        for (int i = 0; i < allCenter.size(); i++) {
            double dist = allCenter.getValue(i);
            int pos = (int) Math.floor(dist / step);
            kutil[pos]++;
        }
        // cumulated histogram
        int[] kutil2 = new int[((int) Math.ceil(max / step))];
        kutil2[0] = kutil[0];
        for (int i = 1; i < kutil.length; i++) kutil2[i] = kutil[i] + kutil2[i - 1];

        double coeff = vol / (Math.PI * nb * (nb - 1));
        ArrayUtil kutil3 = new ArrayUtil(kutil.length);
        for (int i = 0; i < kutil.length; i++) {
            kutil3.putValue(i, Math.sqrt(kutil2[i] * coeff));
        }

        return kutil3;
    }

    @Override
    public String getName() {
        return "K";
    }


}
