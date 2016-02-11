package mcib3d.geom;

import mcib3d.utils.*;

/*
 *
 * mcib3d Thomas Boudier Copyright (C) 2010 Jean Ollion
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
 */
/**
 *
 * @author thomas boudier
 */
public class GeomTransform3D {

    /**
     * geometric transform matrix
     */
    double[][] matrix;
    /**
     * size of the square matrix
     */
    int size = 4;
    boolean identity;

    /**
     * Constructor for the Transform3D object create an identity transform
     */
    public GeomTransform3D() {
        double mat[][] = {{1, 0, 0, 0}, {0, 1, 0, 0}, {0, 0, 1, 0}, {0, 0, 0, 1}};
        matrix = mat;
        identity = true;
    }

    /**
     * Constructor for the Transform3D object create an identity transform
     *
     * @param transfmatrix matrix in double array
     */
    public GeomTransform3D(double[][] transfmatrix) {

        matrix = transfmatrix;
        testIdentity();
    }

    /**
     * Constructor for the GeomTransform3D object
     *
     * @param transform matrix in 1D array of 6 values translation then rotation
     */
    public GeomTransform3D(ArrayUtil transform) {
        double mat[][] = {{1, 0, 0, 0}, {0, 1, 0, 0}, {0, 0, 1, 0}, {0, 0, 0, 1}};
        matrix = mat;
        setTranslation(transform.getValue(0), transform.getValue(1), transform.getValue(2));
        setRotationDegrees(transform.getValue(3), transform.getValue(4), transform.getValue(5));
        testIdentity();
    }

    /**
     * Constructor for the GeomTranform3D object
     *
     * @param tx x translation
     * @param ty y translation
     * @param tz z translation
     * @param rx x rotation
     * @param ry y rotation
     * @param rz z rotation
     */
    public GeomTransform3D(double tx, double ty, double tz, double rx, double ry, double rz) {
        double mat[][] = {{1, 0, 0, 0}, {0, 1, 0, 0}, {0, 0, 1, 0}, {0, 0, 0, 1}};
        matrix = mat;
        setTranslation(tx, ty, tz);
        setRotationDegrees(rx, ry, rz);
        testIdentity();
    }

    /**
     * Reset the matrix to identity
     *
     * @return Description of the Return Value
     */
    public void reset() {
        double mat[][] = {{1, 0, 0, 0}, {0, 1, 0, 0}, {0, 0, 1, 0}, {0, 0, 0, 1}};
        matrix = mat;

        identity = true;
    }

    /**
     * Sets a new transformation
     *
     * @param tx x translation
     * @param ty y translation
     * @param tz z translation
     * @param rx x rotation
     * @param ry y rotation
     * @param rz z rotation
     */
    public void setTransform(double tx, double ty, double tz, double rx, double ry, double rz) {
        setTranslation(tx, ty, tz);
        setRotationDegrees(rx, ry, rz);
        testIdentity();
    }

    /**
     * Sets the translation attribute of the Transform
     *
     * @param x The new translation value on x axis
     * @param y The new translation value on y axis
     * @param z The new translation value on z axis
     */
    public void setTranslation(double x, double y, double z) {
        double trans[][] = {{1, 0, 0, x}, {0, 1, 0, y}, {0, 0, 1, z}, {0, 0, 0, 1}};
        multBy(trans);
        testIdentity();
    }

    /**
     * Scale factor
     *
     * @param sx scale in X
     * @param sy scale in Y
     * @param sz scale in Z
     */
    public void setScale(double sx, double sy, double sz) {
        double scale[][] = {{sx, 0, 0, 0}, {0, sy, 0, 0}, {0, 0, sz, 0}, {0, 0, 0, 1}};
        multBy(scale);

        testIdentity();
    }

    /**
     * Sets the rotation on the X Axis for the Transform
     *
     * @param angleRadian The angle of rotation in radian
     */
    public void setRotationXAxis(double angleRadian) {
        double cos = Math.cos(angleRadian);
        double sin = Math.sin(angleRadian);
        double rot[][] = {{1, 0, 0, 0}, {0, cos, -sin, 0}, {0, sin, cos, 0}, {0, 0, 0, 1}};
        multBy(rot);
        testIdentity();
    }

    /**
     * Sets the rotation on the Y Axis for the Transform
     *
     * @param angleRadian The angle of rotation in radian
     */
    public void setRotationYAxis(double angleRadian) {
        double cos = Math.cos(angleRadian);
        double sin = Math.sin(angleRadian);
        double rot[][] = {{cos, 0, -sin, 0}, {0, 1, 0, 0}, {sin, 0, cos, 0}, {0, 0, 0, 1}};
        multBy(rot);
        testIdentity();
    }

    /**
     * Sets the rotationZAxis attribute of the Transform3D object
     *
     * @param angleRadian The angle of rotation in radian
     */
    public void setRotationZAxis(double angleRadian) {
        double cos = Math.cos(angleRadian);
        double sin = Math.sin(angleRadian);
        double rot[][] = {{cos, -sin, 0, 0}, {sin, cos, 0, 0}, {0, 0, 1, 0}, {0, 0, 0, 1}};
        multBy(rot);
        testIdentity();
    }

    /**
     * Sets the rotation attribute of the Transform3D object
     *
     * @param angleRadianXAxis The angle of rotation in radian on the X axis
     * @param angleRadianYAxis The angle of rotation in radian on the Y axis
     * @param angleRadianZAxis The angle of rotation in radian on the Z axis
     */
    public void setRotation(double angleRadianXAxis, double angleRadianYAxis, double angleRadianZAxis) {
        setRotationXAxis(angleRadianXAxis);
        setRotationYAxis(angleRadianYAxis);
        setRotationZAxis(angleRadianZAxis);
    }

    /**
     *Sets the rotation around an axis with an angle
     * 
     * @param axis the axis as a vector
     * @param angle the angle in radian
     */
    public void setRotation(Vector3D axis, double angle) {
        axis.normalize();
        double c = Math.cos(angle);
        double s = Math.sin(angle);
        double t = 1 - c;

        axis.normalize();
        double x = axis.getX();
        double y = axis.getY();
        double z = axis.getZ();

        double rot[][] = {{t * x * x + c, t * x * y - s * z, t * x * z + s * y, 0},
            {t * x * y + s * z, t * y * y + c, t * y * z - s * x, 0},
            {t * x * z - s * y, t * y * z + s * x, t * z * z + c, 0},
            {0, 0, 0, 1}};
        multBy(rot);
        testIdentity();
    }

    /**
     * Sets the rotation attribute of the Transform3D object
     *
     * @param angleDegreesXAxis The new x rotationDegrees value in degrees
     * @param angleDegreesYAxis The new y rotationDegrees value in degrees
     * @param angleDegreesZAxis The new z rotationDegrees value in degrees
     */
    public void setRotationDegrees(double angleDegreesXAxis, double angleDegreesYAxis, double angleDegreesZAxis) {
        setRotationXAxis((double) Math.toRadians(angleDegreesXAxis));
        setRotationYAxis((double) Math.toRadians(angleDegreesYAxis));
        setRotationZAxis((double) Math.toRadians(angleDegreesZAxis));
    }

    /**
     * Adds a feature to the Transform attribute of the GeomTransform3D object
     *
     * @param transfo The feature to be added to the Transform attribute
     */
    public void addTransform(GeomTransform3D transfo) {
        multBy(transfo.matrix);
    }

    /**
     * Adds a feature to the Transform attribute of the GeomTransform3D object
     *
     * @param transfo The feature to be added to the Transform attribute
     * @return Description of the Return Value
     */
    public void addTransform(ArrayUtil transfo) {
        multBy(new GeomTransform3D(transfo).matrix);
    }

    /**
     * multiply the matrix of geometric transform by an other transform matrix
     *
     * @param other the transform matrix 4x4 by which multiply
     */
    private void multBy(double[][] other) {
        double[][] result = new double[size][size];
        for (int j = 0; j < size; j++) {
            for (int i = 0; i < size; i++) {
                for (int k = 0; k < size; k++) {
                    result[i][j] += this.matrix[i][k] * other[k][j];
                }
            }
        }

        this.matrix = result;
    }

    /**
     * Gets the value of the matrix of the Transform3D object at position
     * [row][column]
     *
     * @param row row number of the matrix
     * @param column column number of the matrix
     * @return The value in the matrix
     */
    public double getValue(int row, int column) {
        return matrix[row][column];
    }

    /**
     * Gets the matrix attribute of the GeomTransform3D object
     *
     * @return The matrix value
     */
    public double[][] getMatrix() {
        return matrix;
    }

    /**
     * Description of the Method
     *
     * @return Description of the Return Value
     */
    public void invert() {
        double[][] inv = new double[4][4];
        invert4x4(inv, matrix);
        matrix=inv;
    }

    /**
     * A identity test
     */
    private void testIdentity() {
        double mat[][] = {{1, 0, 0, 0}, {0, 1, 0, 0}, {0, 0, 1, 0}, {0, 0, 0, 1}};
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                if (matrix[i][j] != mat[i][j]) {
                    identity = false;
                    return;
                }
            }
        }

        identity = true;
    }

    /**
     * Gets the identity attribute of the GeomTransform3D object
     *
     * @return The identity value
     */
    public boolean isIdentity() {
        return identity;
    }

    /**
     * Apply the transform to a point described by a vector
     *
     * @param P Original point
     * @param C Center for rotation
     * @return The transformed point by the transform, described by a vector
     */
    public Vector3D getVectorTransformed(Vector3D P, Vector3D C) {
        Vector3D res;
        double rx;
        double ry;
        double rz;
        double px = P.getX() - C.getX();
        double py = P.getY() - C.getY();
        double pz = P.getZ() - C.getZ();

        rx = matrix[0][0] * px + matrix[0][1] * py + matrix[0][2] * pz + matrix[0][3];
        ry = matrix[1][0] * px + matrix[1][1] * py + matrix[1][2] * pz + matrix[1][3];
        rz = matrix[2][0] * px + matrix[2][1] * py + matrix[2][2] * pz + matrix[2][3];

        res = new Vector3D(rx + C.getX(), ry + C.getY(), rz + C.getZ());

        return res;
    }

    /**
     * taken from volumeJ Invert 4x4 matrix m into inverse mi. USes Gauss-Jordan
     * inversion. See numerical recipes in C, second version.
     *
     * @param mi a double[4][4] that will be overwritten with the inverse of
     * @param m a double[4][4] that contains the matrix.
     */
    public static void invert4x4(double[][] mi, double[][] m) {
        // calculate inverse components of first column (m[0][0], m[1][0], m[2][0], m[3][0])
        double[] t = new double[20];
        double[] x = new double[4];

        t[0] = m[0][0];
        t[1] = m[0][1];
        t[2] = m[0][2];
        t[3] = m[0][3];
        t[4] = 1.0f;

        t[5] = m[1][0];
        t[6] = m[1][1];
        t[7] = m[1][2];
        t[8] = m[1][3];
        t[9] = 0.0f;

        t[10] = m[2][0];
        t[11] = m[2][1];
        t[12] = m[2][2];
        t[13] = m[2][3];
        t[14] = 0.0f;

        t[15] = m[3][0];
        t[16] = m[3][1];
        t[17] = m[3][2];
        t[18] = m[3][3];
        t[19] = 0.0f;

        if (GaussElim(t, x) <= 0) {
            return;
        }
        //throw new MDAMatrixException("inverse failure");

        mi[0][0] = x[0];
        mi[1][0] = x[1];
        mi[2][0] = x[2];
        mi[3][0] = x[3];

        t[0] = m[0][0];
        t[1] = m[0][1];
        t[2] = m[0][2];
        t[3] = m[0][3];
        t[4] = 0.0f;

        t[5] = m[1][0];
        t[6] = m[1][1];
        t[7] = m[1][2];
        t[8] = m[1][3];
        t[9] = 1.0f;

        t[10] = m[2][0];
        t[11] = m[2][1];
        t[12] = m[2][2];
        t[13] = m[2][3];
        t[14] = 0.0f;

        t[15] = m[3][0];
        t[16] = m[3][1];
        t[17] = m[3][2];
        t[18] = m[3][3];
        t[19] = 0.0f;

        if (GaussElim(t, x) <= 0) {
            return;
        }
        //throw new MDAMatrixException("inverse failure");

        mi[0][1] = x[0];
        mi[1][1] = x[1];
        mi[2][1] = x[2];
        mi[3][1] = x[3];

        t[0] = m[0][0];
        t[1] = m[0][1];
        t[2] = m[0][2];
        t[3] = m[0][3];
        t[4] = 0.0f;

        t[5] = m[1][0];
        t[6] = m[1][1];
        t[7] = m[1][2];
        t[8] = m[1][3];
        t[9] = 0.0f;

        t[10] = m[2][0];
        t[11] = m[2][1];
        t[12] = m[2][2];
        t[13] = m[2][3];
        t[14] = 1.0f;

        t[15] = m[3][0];
        t[16] = m[3][1];
        t[17] = m[3][2];
        t[18] = m[3][3];
        t[19] = 0.0f;

        if (GaussElim(t, x) <= 0) {
            return;
        }
        //throw new MDAMatrixException("inverse failure");

        mi[0][2] = x[0];
        mi[1][2] = x[1];
        mi[2][2] = x[2];
        mi[3][2] = x[3];

        t[0] = m[0][0];
        t[1] = m[0][1];
        t[2] = m[0][2];
        t[3] = m[0][3];
        t[4] = 0.0f;

        t[5] = m[1][0];
        t[6] = m[1][1];
        t[7] = m[1][2];
        t[8] = m[1][3];
        t[9] = 0.0f;

        t[10] = m[2][0];
        t[11] = m[2][1];
        t[12] = m[2][2];
        t[13] = m[2][3];
        t[14] = 0.0f;

        t[15] = m[3][0];
        t[16] = m[3][1];
        t[17] = m[3][2];
        t[18] = m[3][3];
        t[19] = 1.0f;

        if (GaussElim(t, x) <= 0) {
            return;
        }
        //throw new MDAMatrixException("inverse failure");

        mi[0][3] = x[0];
        mi[1][3] = x[1];
        mi[2][3] = x[2];
        mi[3][3] = x[3];
    }

    /**
     * Taken from VolumeJ Gaussian elimination method with backward substitution
     * to solve a system of linear equations. E1: a11 x1 + a12 x2 + ... a1n xn =
     * a1,n+1 E2: a21 x1 + a22 x2 + ... a2n xn = a2,n+1 . . . En: an1 x1 + an2
     * x2 + ... ann xn = an,n+1
     *
     * @param a Description of the Parameter
     * @param x Description of the Parameter
     * @return Description of the Return Value
     */
    private static int GaussElim(double[] a, double[] x) {
        int n = x.length;
        /*
         * number of unknowns and equations in system
         */
        for (int i = 0; i < n; i++) {
            x[i] = 0.0f;
        }
        int w = n + 1;
        for (int i = 0; i < (n - 1); i++) {
            int p;
            for (p = i; p < n; p++) {
                if (a[(p * w) + i] != 0.0) {
                    break;
                }
            }
            if (p == n) {
                return (0);
            }
            if (p != i) {
                for (int k = 0; k < w; k++) {
                    double temp = a[(i * w) + k];
                    a[(i * w) + k] = a[(p * w) + k];
                    a[(p * w) + k] = temp;
                }
            }

            for (int j = (i + 1); j < n; j++) {
                double m = a[(j * w) + i] / a[(i * w) + i];

                for (int k = 0; k <= n; k++) {
                    a[(j * w) + k] = a[(j * w) + k] - (m * a[(i * w) + k]);
                }
            }
        }
        if (a[((n - 1) * w) + (n - 1)] == 0.0) {
            return (0);
        }
        x[n - 1] = a[((n - 1) * w) + n] / a[((n - 1) * w) + (n - 1)];
        for (int i = (n - 2); i >= 0; i--) {
            double s = 0.0;
            for (int j = (i + 1); j < n; j++) {
                s = s + (a[(i * w) + j] * x[j]);
            }
            x[i] = (a[(i * w) + n] - s) / a[(i * w) + i];
        }
        return (1);
    }
    // GaussElim
}
