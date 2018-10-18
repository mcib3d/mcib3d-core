/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mcib3d.spatial.descriptors;

import mcib3d.geom.Objects3DPopulation;
import mcib3d.utils.ArrayUtil;

/**
 * @author thomasb
 */
public class K_Function implements SpatialDescriptor {
    private double step = 1.0;
    private double max = 1000;
    private double bin;

    public K_Function(double step, double max) {
        this.step = step;
        this.max = max;
        this.bin = max / step;
    }

    @Override
    public boolean init() {
        return true;
    }

    @Override
    public ArrayUtil compute(Objects3DPopulation pop) {
        ArrayUtil util = pop.distancesAllCenter();
        // normal histogram
        int[] kutil = new int[((int) Math.ceil(max / bin))];
        for (int i = 0; i < util.size(); i++) kutil[i] = 0;
        for (int i = 0; i < util.size(); i++) {
            double dist = util.getValue(i);
            int pos = (int) Math.floor(dist / bin);
            kutil[pos]++;
        }
        // cumulated histogram
        ArrayUtil kutil2 = new ArrayUtil(kutil.length);
        kutil2.putValue(0, kutil[0]);
        for (int i = 1; i < util.size(); i++) {
            kutil2.putValue(i, kutil2.getValue(i - 1) + kutil[i]);
        }

        return kutil2;

    }

    @Override
    public String getName() {
        return "K";
    }


}
