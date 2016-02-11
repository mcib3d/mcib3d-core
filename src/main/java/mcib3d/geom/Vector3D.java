package mcib3d.geom;

import ij.IJ;
import java.text.NumberFormat;
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
 * Vecteur 3D
 *
 * @author thomas @created 4 novembre 2004
 */
public class Vector3D extends Point3D {

    /**
     * longueur
     */
    double length = -1;

    /**
     * constructeurs
     */
    public Vector3D() {
        super();
    }

    /**
     * Constructor for the Vector3D object
     *
     * @param x x coordinate of the vector
     * @param y y coordinate of the vector
     * @param z z coordinate of the vector
     */
    public Vector3D(float x, float y, float z) {
        super(x, y, z);
    }

    /**
     * Constructor for the Vector3D object
     *
     * @param x x coordinate of the vector
     * @param y y coordinate of the vector
     * @param z z coordinate of the vector
     */
    public Vector3D(double x, double y, double z) {
        super(x, y, z);
    }

    /**
     * Copy constructor for the Vector3D object
     *
     * @param V the vector to copy
     */
    public Vector3D(Vector3D V) {
        super(V.getX(), V.getY(), V.getZ());
    }

    /**
     *
     * @param P the point3D to copy
     */
    public Vector3D(Point3D P) {
        super(P);
    }

    /**
     * Conctruct a vector from two points
     *
     * @param P1 first point
     * @param P2 second point
     */
    public Vector3D(Point3D P1, Point3D P2) {
        super(P2.getX() - P1.getX(), P2.getY() - P1.getY(), P2.getZ() - P1.getZ());
    }

    /**
     * Conctruct a vector from two points
     *
     * @param P1 first point
     * @param P2 second point
     */
    public Vector3D(Point3f P1, Point3f P2) {
        super(P2.x - P1.x, P2.y - P1.y, P2.z - P1.z);
    }

    public Vector3D(Point3f P) {
        super(P.x, P.y, P.z);
    }

    /**
     * Sets the attributes of the Vector3D object
     *
     * @param x x coordinate of the vector
     * @param y y coordinate of the vector
     * @param z z coordinate of the vector
     */
    public void setCoord(double x, double y, double z) {
        setX(x);
        setY(y);
        setZ(z);
    }

    @Override
    public void setCoordRandom() {
        super.setCoordRandom();
        normalize();
    }

    /**
     * Gets the value attribute of the Pixel3D object
     *
     * @return The value value
     */
    public double getLength() {
        return getLength(1.0, 1.0);
    }

    public double getLengthSquare() {
        return getLengthSquare(1.0, 1.0);
    }

    /**
     * Gets the length attribute of the Vector3D object with units
     *
     * @param resXY resolution in XY
     * @param resZ resolution in Z
     * @return The length value
     */
    public double getLength(double resXY, double resZ) {
        computeLength(resXY, resZ);
        return length;
    }

    public double getLengthSquare(double resXY, double resZ) {
        return computeLengthSquare(resXY, resZ);
    }

    /**
     * Distance between two vectors
     *
     * @param V the other vector
     * @return the length of the difference
     */
    public double distance(Vector3D V) {
        Vector3D diff = add(V, 1, -1);
        return diff.getLength();
    }

    /**
     * Description of the Method
     *
     * @param V Description of the Parameter
     * @return Description of the Return Value
     */
    public double distanceSquare(Vector3D V) {
        Vector3D diff = add(V, 1, -1);
        return (diff.getX() * diff.getX()) + (diff.getY() * diff.getY()) + (diff.getZ() * diff.getZ());
    }

    /**
     * compute the length of the vector with units
     *
     * @param resXY resolution in XY
     * @param resZ resolution in Z
     */
    private void computeLength(double resXY, double resZ) {
        length = Math.sqrt((getX() * getX() * resXY * resXY) + (getY() * getY() * resXY * resXY) + (getZ() * getZ() * resZ * resZ));
    }

    private double computeLengthSquare(double resXY, double resZ) {
        return ((getX() * getX() * resXY * resXY) + (getY() * getY() * resXY * resXY) + (getZ() * getZ() * resZ * resZ));
    }

    /**
     * Add a vector
     *
     * @param v the other vector
     * @return the new vector
     */
    public Vector3D add(Vector3D v) {
        return this.add(v, 1, 1);
    }

    /**
     * Add a vector to current vector
     *
     * @param v the other vector
     * @return the new vector
     */
    public void addMe(Vector3D v) {
        this.x += v.getX();
        this.y += v.getY();
        this.z += v.getZ();
    }

    public void addMe(Vector3D v, double r) {
        this.x += v.getX() * r;
        this.y += v.getY() * r;
        this.z += v.getZ() * r;
    }

    /**
     * add a vector with ration
     *
     * @param v the other vector V2
     * @param f1 the ratio for V1
     * @param f2 the ratio for V2
     * @return V1*f1 + V2*f2
     */
    public Vector3D add(Vector3D v, float f1, float f2) {
        return new Vector3D(f1 * getX() + f2 * v.getX(), f1 * getY() + f2 * v.getY(), f1 * getZ() + f2 * v.getZ());
    }

    /**
     * Description of the Method
     *
     * @param v Description of the Parameter
     * @param f1 Description of the Parameter
     * @param f2 Description of the Parameter
     * @return Description of the Return Value
     */
    public Vector3D add(Vector3D v, double f1, double f2) {
        return new Vector3D(f1 * getX() + f2 * v.getX(), f1 * getY() + f2 * v.getY(), f1 * getZ() + f2 * v.getZ());
    }

    /**
     * multiply a vector by a scalar
     *
     * @param f the scalar
     * @return the new vector multiplied
     */
    public Vector3D multiply(double f) {
        return new Vector3D(f * getX(), f * getY(), f * getZ());
    }

    /**
     * multiply current vector by a scalar
     *
     * @param f the scalar
     * @return the new vector multiplied
     */
    public void multiplyMe(double f) {
        this.x *= f;
        this.y *= f;
        this.z *= f;
    }

    /**
     * mumultiply the coordinates
     *
     * @param fx multiplication factor for x
     * @param fy multiplication factor for y
     * @param fz multiplication factor for z
     * @return the new vector
     */
    public Vector3D multiply(double fx, double fy, double fz) {
        return new Vector3D(fx * getX(), fy * getY(), fz * getZ());
    }

    /**
     * mumultiply the coordinates
     *
     * @param fx multiplication factor for x
     * @param fy multiplication factor for y
     * @param fz multiplication factor for z
     * @return the new vector
     */
    public void multiplyMe(double fx, double fy, double fz) {
        x *= fx;
        y *= fy;
        z *= fz;
    }

    /**
     * angle between two vectors in radians between 0 and PI
     *
     * @param v the other vector
     * @return the angle
     */
    public double angle(Vector3D v) {
        double le = getLength() * v.getLength();
        double alpha = 0;
        if (le > 0) {
            double sca = dotProduct(v);
            sca /= le;
            if (sca < 0) {
                sca *= -1;
            }
            alpha = Math.acos(sca);
        }

        return alpha;
    }

    public double anglePlane(double a, double b, double c, double d) {        
        Vector3D proj = new Vector3D(projectionPlane(a, b, c, d));
        if (proj.getLength() > 0) {
            return angle(proj);
        } else {
            return Math.PI / 2;
        }
    }

    public double anglePlaneDegrees(double a, double b, double c, double d) {
        return Math.toDegrees(anglePlane(a, b, c, d));
    }

    /**
     * angle between two vectors in degrees between 0 and 180
     *
     * @param v the other vector
     * @return the angle
     */
    public double angleDegrees(Vector3D v) {
        return Math.toDegrees(angle(v));
    }

    /**
     * scalar product between two vectors
     *
     * @param V the other vector
     * @return this.Y
     */
    public double dotProduct(Vector3D V) {
        return (getX() * V.getX() + getY() * V.getY() + getZ() * V.getZ());
    }

    /**
     * Compute the scalar product, using calibration
     *
     * @param V the other vector
     * @param resXY the XY calibration
     * @param resZ the Z calibration
     * @return
     */
    public double dotProduct(Vector3D V, double resXY, double resZ) {
        return ((getX() * V.getX()) * resXY * resXY + (getY() * V.getY()) * resXY * resXY + (getZ() * V.getZ()) * resZ * resZ);
    }

    /**
     *
     * @return a random perpendicular vector
     */
    public Vector3D getRandomPerpendicularVector() {
        Vector3D res = new Vector3D();
        double a = getX();
        double b = getY();
        double c = getZ();
        double x = 0, y = 0, z = 0;

        if (c != 0) {
            x = 2 * Math.random() - 1;
            y = 2 * Math.random() - 1;
            z = (-a * x - b * y) / c;
        } // in the x-y plane
        else if (b != 0) {
            x = 1;
            y = -a / b;
            z = 0;
            // only x != 0
        } else {
            x = 0;
            y = 1;
            z = 0;
        }

        res.setX(x);
        res.setY(y);
        res.setZ(z);

        res.normalize();

        //System.out.println("orig="+this+" perp="+res+" cc="+this.scalarProduct(res));
        return res;
    }

    /**
     * the cross product between two vectors
     *
     * @param V the other vector
     * @return the result vector
     */
    public Vector3D crossProduct(Vector3D V) {
        Vector3D res = new Vector3D();
        res.setCoord(getY() * V.getZ() - getZ() * V.getY(),
                getZ() * V.getX() - getX() * V.getZ(),
                getX() * V.getY() - getY() * V.getX());

        return res;
    }

    /**
     * Computes the colinearity between two vectors that is to say the absolute
     * value of cross product between normalized vectors
     *
     * @param other
     * @return
     */
    public double colinear(Vector3D other) {
        Vector3D V1 = new Vector3D(this);
        V1.normalize();
        Vector3D V2 = new Vector3D(other);
        V2.normalize();
        double res = V1.dotProduct(V2);

        return res;
    }

    /**
     * normalization of the vector, put length to 1.0
     */
    public void normalize() {
        computeLength(1.0, 1.0);
        if (length > 0) {
            setX(getX() / length);
            setY(getY() / length);
            setZ(getZ() / length);
            length = 1.0;
        } else {
            length = 0;
        }

    }

    public Vector3D getNormalizedVector() {
        Vector3D tmp = new Vector3D(this);
        tmp.normalize();

        return tmp;
    }

    public Vector3D getComposante(Vector3D base) {
        Vector3D cv = new Vector3D(this);
        Vector3D ba = base.getNormalizedVector();
        double co = cv.dotProduct(base);

        return base.multiply(co);
    }

    /**
     * Distance traversée dans un cube (for projection computation)
     *
     * @param rx coordonnée x du point de passage
     * @param ry coordonnée x du point de passage
     * @param rz coordonnée x du point de passage
     * @param x Description of the Parameter
     * @param y Description of the Parameter
     * @param z Description of the Parameter
     * @return distance
     */
    public double intersection_unit_cube(double x, double y, double z, double rx, double ry, double rz) {

        double t1 = 0;
        double t2 = 0;
        double t = 0;
        int found_t = 0;
        double prec = 0.0000001;
        Vector3D r = new Vector3D(rx - x, ry - y, rz - z);

        // Intersect with x=0.5 and x=-0.5
        if (getX() != 0) {
            t = (0.5 - r.getX()) / getX();
            if ((Math.abs(r.getX() + t * getX()) - prec <= 0.5) && (Math.abs(r.getY() + t * getY()) - prec <= 0.5) && (Math.abs(r.getZ() + t * getZ()) - prec <= 0.5)) {
                if (found_t == 0) {
                    found_t++;
                    t1 = t;
                } else if (found_t == 1) {
                    found_t++;
                    t2 = t;
                }
            }
            t = (-0.5 - r.getX()) / getX();
            if ((Math.abs(r.getX() + t * getX()) - prec <= 0.5) && (Math.abs(r.getY() + t * getY()) - prec <= 0.5) && (Math.abs(r.getZ() + t * getZ()) - prec <= 0.5)) {
                if (found_t == 0) {
                    found_t++;
                    t1 = t;
                } else if (found_t == 1) {
                    found_t++;
                    t2 = t;
                }
            }
        }

        // Intersect with y=0.5 and y=-0.5
        if ((getY() != 0) && (found_t != 2)) {
            t = (0.5 - r.getY()) / getY();
            if ((Math.abs(r.getX() + t * getX()) - prec <= 0.5) && (Math.abs(r.getY() + t * getY()) - prec <= 0.5) && (Math.abs(r.getZ() + t * getZ()) - prec <= 0.5)) {
                if (found_t == 0) {
                    found_t++;
                    t1 = t;
                } else if (found_t == 1) {
                    found_t++;
                    t2 = t;
                }
            }
            t = (-0.5 - r.getY()) / getY();
            if ((Math.abs(r.getX() + t * getX()) - prec <= 0.5) && (Math.abs(r.getY() + t * getY()) - prec <= 0.5) && (Math.abs(r.getZ() + t * getZ()) - prec <= 0.5)) {
                if (found_t == 0) {
                    found_t++;
                    t1 = t;
                } else if (found_t == 1) {
                    found_t++;
                    t2 = t;
                }
            }
        }

        // Intersect with z=0.5 and z=-0.5
        if ((getZ() != 0) && (found_t != 2)) {
            t = (0.5 - r.getZ()) / getZ();
            if ((Math.abs(r.getX() + t * getX()) - prec <= 0.5) && (Math.abs(r.getY() + t * getY()) - prec <= 0.5) && (Math.abs(r.getZ() + t * getZ()) - prec <= 0.5)) {
                if (found_t == 0) {
                    found_t++;
                    t1 = t;
                } else if (found_t == 1) {
                    found_t++;
                    t2 = t;
                }
            }
            t = (-0.5 - r.getZ()) / getZ();
            if ((Math.abs(r.getX() + t * getX()) - prec <= 0.5) && (Math.abs(r.getY() + t * getY()) - prec <= 0.5) && (Math.abs(r.getZ() + t * getZ()) - prec <= 0.5)) {
                if (found_t == 0) {
                    found_t++;
                    t1 = t;
                } else if (found_t == 1) {
                    found_t++;
                    t2 = t;
                }
            }
        }
        //System.out.println("ict : " + t1 + " " + t2);
        if (found_t == 2) {
            return Math.abs(t1 - t2);
        } else {
            return 0;
        }
    }

    /**
     * display vector information
     *
     * @return text
     */
    @Override
    public String toString() {
        NumberFormat nf = NumberFormat.getInstance();
        nf.setMaximumFractionDigits(3);

        String res = "(" + nf.format(getX()) + ", " + nf.format(getY()) + ", " + nf.format(getZ()) + ")";
        return res;
    }

    /**
     *
     * @param wr
     */
    @Override
    public void xmlWrite(java.io.Writer wr) {
        try {
            wr.write("<Vector3D x=\"" + getX() + "\" y=\""
                    + getY() + "\" z=\""
                    + getZ() + "\" />");
            wr.flush();

        } catch (java.io.IOException e) {
            System.out.println("error in Vector3D::xmlWrite " + e);
        }

    }
}
