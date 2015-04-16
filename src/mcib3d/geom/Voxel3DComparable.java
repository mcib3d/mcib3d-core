/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mcib3d.geom;

/**
Copyright (C) Thomas Boudier

License:
This program is free software; you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation; either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *

/**
 *
 * @author thomas
 */
public class Voxel3DComparable extends Voxel3D implements Comparable<Voxel3D> {

    double label;

    /**
     * 
     * @return
     */
    public double getLabel() {
        return label;
    }

    /**
     * 
     * @param label
     */
    public void setLabel(double label) {
        this.label = label;
    }

    /**
     * 
     * @param x
     * @param y
     * @param z
     * @param val
     * @param label
     */
    public Voxel3DComparable(int x, int y, int z, double val, double label) {
        super(x, y, z, val);
        this.label = label;
    }

    // order (inverted for bright voxels)
    /**
     * 
     * @param t
     * @return
     */
    public int compareTo(Voxel3DComparable t) {
        double v1 = this.getValue();
        double v2 = t.getValue();

        if (v1 == v2) {
            return 0;
        } else if (v1 > v2) {
            return -1;
        } else {
            return 1;
        }


    }
}
