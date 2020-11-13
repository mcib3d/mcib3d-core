/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mcib3d.geom;

import java.util.Comparator;

/**
 *
 * @author thomasb
 */
public class ComparatorVoxel implements Comparator<Voxel3DComparable> {

    @Override
    public int compare(Voxel3DComparable V0, Voxel3DComparable V1) {
        double v0 = V0.getValue();
        double v1 = V1.getValue();

        if (v0 == v1) {
            if (V0.max > V1.max) {
                return 1;
            } else {
                return +1;
            }
        } else if (v0 > v1) {
            return -1;
        } else {
            return 1;
        }
    }

}
