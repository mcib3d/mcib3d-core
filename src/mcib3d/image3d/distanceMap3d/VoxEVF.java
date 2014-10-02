/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mcib3d.image3d.distanceMap3d;

/**
 *
 * @author thomasb
 */
public class VoxEVF implements Comparable<VoxEVF> {

    float distance;
    double index;
    int xy, z;

    public VoxEVF(float distance, int xy, int z) {
        this.distance = distance;
        this.xy = xy;
        this.z = z;
    }

    public VoxEVF(float distance, double index, int xy, int z) {
        this.distance = distance;
        this.index = index;
        this.xy = xy;
        this.z = z;
    }

    @Override
    public int compareTo(VoxEVF v) {
        if (distance > v.distance) {
            return 1;
        } else if (distance < v.distance) {
            return -1;
        } else {
            return 0;
        }
    }
}
