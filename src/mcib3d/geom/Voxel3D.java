package mcib3d.geom;

import javax.vecmath.Point3f;

/**
 * Copyright (C) Thomas Boudier
 *
 * License: This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by the Free
 * Software Foundation; either version 3 of the License, or (at your option) any
 * later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 *
 *
 * /**
 * Description of the Class
 *
 * @author cedric @created 12 novembre 2003
 */
public class Voxel3D extends Point3D implements Comparable<Voxel3D> {

    /**
     * Description of the Field
     */
    double value;

    /**
     * constructeurs
     */
    public Voxel3D() {
        super();
        value = Float.NaN;
    }

    /**
     * constructeurs
     *
     * @param x Description of the Parameter
     * @param y Description of the Parameter
     * @param z Description of the Parameter
     * @param val
     */
    public Voxel3D(int x, int y, int z, float val) {
        super(x, y, z);
        value = val;
    }

    /**
     * Constructor for the Voxel3D object
     *
     * @param x Description of the Parameter
     * @param y Description of the Parameter
     * @param z Description of the Parameter
     * @param val
     */
    public Voxel3D(int x, int y, int z, double val) {
        super(x, y, z);
        value = val;
    }

    /**
     *
     * @param x
     * @param y
     * @param z
     * @param val
     */
    public Voxel3D(double x, double y, double z, double val) {

        super(x, y, z);
        value = val;
    }

    /**
     *
     * @param vox
     */
    public Voxel3D(Voxel3D vox) {
        super(vox.getX(), vox.getY(), vox.getZ());
        this.value = vox.getValue();
    }

    /**
     *
     * @param P
     * @param val
     */
    public Voxel3D(Point3f P, double val) {
        super(P.x, P.y, P.z);
        this.value = val;
    }

    public Voxel3D(Point3D P, double val) {
        super(P);
        this.value = val;
    }
    

    /**
     * Sets the pixel attribute of the Voxel3D object
     *
     * @param x The new pixel value
     * @param y The new pixel value
     * @param z The new pixel value
     * @param value The new pixel value
     */
    public void setVoxel(int x, int y, int z, float value) {
        setX(x);
        setY(y);
        setZ(z);
        this.value = value;
    }

    /**
     * Sets the pixel attribute of the Voxel3D object
     *
     * @param x The new pixel value
     * @param y The new pixel value
     * @param z The new pixel value
     * @param value The new pixel value
     */
    public void setVoxel(int x, int y, int z, double value) {
        setX(x);
        setY(y);
        setZ(z);
        this.value = (float) value;
    }

    public void setVoxel(double x, double y, double z, double value) {
        setX(x);
        setY(y);
        setZ(z);
        this.value = (float) value;
    }

    /**
     * Gets the value attribute of the Voxel3D object
     *
     * @return The value value
     */
    public double getValue() {
        return value;
    }

    @Override
    public String toString() {
        return ("(" + getX() + " , " + getY() + " , " + getZ() + ")");
    }

    /**
     *
     * @param val
     */
    public void setValue(double val) {
        value = val;
    }

    public int getXYCoord(int sizeX) {
        return (int) (x + 0.5) + ((int) (y + 0.5)) * sizeX;
    }
    

    @Override
    public boolean equals(Object o) {
        if (o instanceof Voxel3D) {
            Voxel3D other = (Voxel3D) o;
            return other.x == x && other.y == y && other.z == z;
        }
        return false;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 31 * hash + (int) (Double.doubleToLongBits(this.x) ^ (Double.doubleToLongBits(this.x) >>> 32));
        hash = 31 * hash + (int) (Double.doubleToLongBits(this.y) ^ (Double.doubleToLongBits(this.y) >>> 32));
        hash = 31 * hash + (int) (Double.doubleToLongBits(this.z) ^ (Double.doubleToLongBits(this.z) >>> 32));
        return hash;
    }

    @Override
    public int compareTo(Voxel3D v) {
        //consitancy with equals method
        if (v.x == x && v.y == y && v.z == z) {
            return 0;
        } // decreasing intensities
        else if (value < v.value) {
            return 1;
        } else if (value > v.value) {
            return -1;
        } else {
            return 0;
        }
    }
}
