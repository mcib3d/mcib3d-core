package mcib3d.geom;

import java.util.Random;
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
 * Class Point3D Simple class for a point in 3D space
 *
 * @author thomas @created 9 janvier 2009
 */
public class Point3D {

    //
    // Fields
    //
    public double x = 0;
    public double y = 0;
    public double z = 0;
    //
    // Constructors
    //

    /**
     * Constructor for the Point3D object
     */
    public Point3D() {
    }

    /**
     * Constructor for the Point3D object
     *
     * @param a Description of the Parameter
     * @param b Description of the Parameter
     * @param c Description of the Parameter
     */
    public Point3D(double a, double b, double c) {
        x = a;
        y = b;
        z = c;
    }

    /**
     * Constructor for the Point3D object
     *
     * @param P Description of the Parameter
     */
    public Point3D(Point3D P) {
        x = P.getX();
        y = P.getY();
        z = P.getZ();
    }

    public Point3D(Point3f P) {
        x = P.x;
        y = P.y;
        z = P.z;
        // Pb compatibility with mac
//        x = P.getX();
//        y = P.getY();
//        z = P.getZ();
    }

    //
    // Methods
    //
    //
    // Accessor methods
    //
    /**
     * Set the value of x
     *
     * @param newVar the new value of x
     */
    public void setX(double newVar) {
        x = newVar;
    }

    /**
     * Get the value of x
     *
     * @return the value of x
     */
    public double getX() {
        return x;
    }

    /**
     *
     * @return
     */
    public int getRoundX() {
        return (int) Math.round(x);
    }

    /**
     *
     * @return
     */
    public int getRoundY() {
        return (int) Math.round(y);
    }

    /**
     *
     * @return
     */
    public int getRoundZ() {
        return (int) Math.round(z);
    }

    /**
     * Set the value of y
     *
     * @param newVar the new value of y
     */
    public void setY(double newVar) {
        y = newVar;
    }

    /**
     * Get the value of y
     *
     * @return the value of y
     */
    public double getY() {
        return y;
    }

    /**
     * Set the value of z
     *
     * @param newVar the new value of z
     */
    public void setZ(double newVar) {
        z = newVar;
    }

    /**
     * Get the value of z
     *
     * @return the value of z
     */
    public double getZ() {
        return z;
    }

    /**
     * Gets the vector3D attribute of the Point3D object
     *
     * @return The vector3D value
     */
    public Vector3D getVector3D() {
        return new Vector3D(x, y, z);
    }

    /**
     * Gets the position attribute of the Point3D object
     *
     * @return The position value
     */
    public Point3D getPosition() {
        return new Point3D(x, y, z);
    }

    public Point3f getPoint3f() {
        return new Point3f((float) x, (float) y, (float) z);
    }

    //
    // Other methods
    //
    /**
     * Description of the Method
     *
     * @param v Description of the Parameter
     */
    public void translate(Vector3D v) {
        x += v.getX();
        y += v.getY();
        z += v.getZ();
    }
    
    public void translate(double dx, double dy, double dz) {
        x += dx;
        y += dy;
        z += dz;
    }

    public void scale(double sx, double sy, double sz) {
        x *= sx;
        y *= sy;
        z *= sz;
    }

    /**
     * Description of the Method
     *
     * @param other Description of the Parameter
     * @return Description of the Return Value
     */
    public double distance(Point3D other) {
        return Math.sqrt(distanceSquare(other));
    }

    /**
     * Description of the Method
     *
     * @param other Description of the Parameter
     * @param resXY
     * @param resZ
     * @return Description of the Return Value
     */
    public double distance(Point3D other, double resXY, double resZ) {
        return Math.sqrt(distanceSquare(other, resXY, resZ));
    }

    /**
     * Description of the Method
     *
     * @param other Description of the Parameter
     * @return Description of the Return Value
     */
    public double distanceSquare(Point3D other) {
        return ((x - other.getX()) * (x - other.getX()) + (y - other.getY()) * (y - other.getY()) + (z - other.getZ()) * (z - other.getZ()));
    }

    /**
     * Description of the Method
     *
     * @param other Description of the Parameter
     * @param resXY
     * @param resZ
     * @return Description of the Return Value
     */
    public double distanceSquare(Point3D other, double resXY, double resZ) {
        return ((x - other.getX()) * (x - other.getX()) * resXY * resXY + (y - other.getY()) * (y - other.getY()) * resXY * resXY + (z - other.getZ()) * (z - other.getZ()) * resZ * resZ);
    }

    /**
     *
     * @param other
     * @return
     */
    public double distBlock(Point3D other) {
        return (Math.abs(x - other.getX()) + Math.abs(y - other.getY()) + Math.abs(z - other.getZ()));
    }

    /**
     * Description of the Method
     *
     * @return Description of the Return Value
     */
    @Override
    public String toString() {
        return ("(" + x + " " + y + " " + z + ")");
    }

    /**
     *
     * @param wr
     */
    public void xmlWrite(java.io.Writer wr) {
        try {
            wr.write("<Point3D x=\"" + x + "\" y=\"" + y + "\" z=\"" + z + "\" />\n");
            wr.flush();

        } catch (java.io.IOException e) {
            System.out.println("error in Point3D::XmlWrite " + e);
        }

    }

    /**
     * set coordinates with random numbers between -1 and 1
     */
    public void setCoordRandom() {
        Random rand = new Random();
        setX(rand.nextGaussian());
        setY(rand.nextGaussian());
        setZ(rand.nextGaussian());
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof Point3D) {
            Point3D other = (Point3D) o;
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

    public double[] getArray() {
        double[] res = {x, y, z};
        return res;
    }

    public boolean isInsideBoundingBox(int[] boundingBox) { //xmin, xmax, ymin, ymax, zmin, zmax
        return (x >= boundingBox[0] && x <= boundingBox[1] && y >= boundingBox[2] && y <= boundingBox[3] && z >= boundingBox[4] && z <= boundingBox[5]);
    }

    public boolean sameVoxel(Point3D other) { //returns true if 2 points in the same voxel
        if (Math.abs(x - other.x) < 0.5 && Math.abs(y - other.y) < 0.5 && Math.abs(z - other.z) < 0.5) {
            //return this.distanceSquare(other) < 0.25;
            return true;
        } else {
            return false;
        }
    }

    public boolean samePosition(Point3D other, double error) {
        if ((other.x - x < error) && (x - other.x < error) && (other.y - y < error) && (y - other.y < error) && (other.z - z < error) && (z - other.z < error)) {
            return true;
        } else {
            return false;
        }
    }

    public boolean samePosition(Point3D other) {
        return samePosition(other, 0.5);
    }

    public Point3D projectionPlane(double a, double b, double c, double d) {
        double k = -(a * x + b * y + c * z + d) / (a * a + b * b + c * c);

        return new Point3D(x + k * a, y + k * b, z + k * c);
    }
}
