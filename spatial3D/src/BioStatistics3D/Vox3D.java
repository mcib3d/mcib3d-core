/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package BioStatistics3D;

/**
 *
 * @author 
 * class to stock the array of result with position and distance
 */
public class Vox3D implements Comparable<Vox3D> 
{
    float distance;
    double index;
    int xy, z, x, y;
    
    /**
     * constructor create a object
     * @param distance
     * @param xy
     * @param z 
     */
    public Vox3D(float distance, int xy, int z) {
        this.distance = distance;
        this.xy = xy;
        this.z = z;
    }
    /**
     * Create a object Vox3D
     * @param distance
     * @param x
     * @param y
     * @param z 
     */
    public Vox3D(float distance, int x, int y, int z) {
        this.distance = distance;
        this.x = x;
        this.y = y;
        this.z = z;
    }
    /**
     * Create a object Vox3D
     * @param distance
     * @param index
     * @param xy
     * @param z 
     */
    public Vox3D(float distance, double index, int xy, int z) {
        this.distance = distance;
        this.index = index;
        this.xy = xy;
        this.z = z;
    }

    @Override
    public int compareTo(Vox3D v) {
        if (distance > v.distance) {
            return 1;
        } else if (distance < v.distance) {
            return -1;
        } else {
            return 0;
        }
    }
}