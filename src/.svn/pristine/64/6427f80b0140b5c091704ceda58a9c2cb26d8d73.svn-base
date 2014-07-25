package mcib3d.geom;

import ij.*;
import ij.measure.*;
import java.util.ArrayList;

import mcib3d.image3d.*;
import mcib3d.Jama.*;
import mcib3d.image3d.legacy.Image3D;

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
 * Creation de forme primitive dans une image
 *
 * @author cedric @created 3 mars 2004
 */
public class ObjectCreator3D {
    //  Le volume qui va contenir les formes

    ImageHandler img;
    // Spacings
    double resXY = 1.0;
    double resZ = 1.0;
    String unit = "pix";

    /**
     * Constructor for the ObjectCreator3D object
     *
     * @param image The image3D to draw inside
     */
    public ObjectCreator3D(ImageHandler image) {
        img = image;
    }

    /**
     *
     * @param stack
     */
    public ObjectCreator3D(ImageStack stack) {
        img = ImageHandler.wrap(stack);
    }

    /**
     * Constructor for the ObjectCreator3D object
     *
     * @param sizex Taille x du volume
     * @param sizey Taille y du volume
     * @param sizez Taille z du volume
     * @param type Type du volume (entier ou réel)
     */
//    public ObjectCreator3D(int sizex, int sizey, int sizez, int type) {
//        if (type == Image3D.FLOAT) {
//            img = new RealImage3D(sizex, sizey, sizez);
//        } else {
//            img = new IntImage3D(sizex, sizey, sizez, type);
//        }
//    }
    /**
     * Constructor for the ObjectCreator3D object
     *
     * @param sizex Taille x du volume
     * @param sizey Taille y du volume
     * @param sizez Taille z du volume
     */
    public ObjectCreator3D(int sizex, int sizey, int sizez) {
        img = new ImageShort("creator", sizex, sizey, sizez);
    }

    /**
     * Sets the resolution attribute of the ObjectCreator3D object
     *
     * @param rxy The new resolution in XY
     * @param rz The new resolution in Z
     * @param u The new resolution unit
     */
    public void setResolution(double rxy, double rz, String u) {
        resXY = rxy;
        resZ = rz;
        unit = u;
    }

    /**
     * Gets the calibration attribute of the Object3D object
     *
     * @return The calibration value
     */
    public Calibration getCalibration() {
        Calibration cal = new Calibration();
        cal.pixelWidth = resXY;
        cal.pixelHeight = resXY;
        cal.pixelDepth = resZ;
        cal.setUnit(unit);

        return cal;
    }

    /**
     * Sets the calibration attribute of the Object3D object
     *
     * @param cal The new calibration value
     */
    public void setCalibration(Calibration cal) {
        if (cal != null) {
            resXY = cal.pixelWidth;
            resZ = cal.pixelDepth;
            unit = cal.getUnits();
        }
    }

    /**
     * Description of the Method
     *
     * @param centerx Description of the Parameter
     * @param centery Description of the Parameter
     * @param centerz Description of the Parameter
     * @param rx Description of the Parameter
     * @param ry Description of the Parameter
     * @param rz Description of the Parameter
     * @param value Description of the Parameter
     * @param gauss Description of the Parameter
     */
    public void createEllipsoidUnit(double centerx, double centery, double centerz, double rx, double ry, double rz, float value, boolean gauss) {
        // scale radius to pixel unit
        rx /= resXY;
        ry /= resXY;
        rz /= resZ;

        // scale radius to pixel unit
        centerx /= resXY;
        centery /= resXY;
        centerz /= resZ;

        createEllipsoid((int) Math.round(centerx), (int) Math.round(centery), (int) Math.round(centerz), rx, ry, rz, value, gauss);

    }

    /**
     * Description of the Method
     *
     * @param V Description of the Parameter
     * @param rx Description of the Parameter
     * @param ry Description of the Parameter
     * @param rz Description of the Parameter
     * @param col Description of the Parameter
     */
    public void createEllipsoid(Vector3D V, double rx, double ry, double rz, float col) {
        createEllipsoid(V.getRoundX(), V.getRoundY(), V.getRoundZ(), rx, ry, rz, col, false);
    }

    /**
     * Description of the Method
     *
     * @param V Description of the Parameter
     * @param rx Description of the Parameter
     * @param ry Description of the Parameter
     * @param rz Description of the Parameter
     * @param col Description of the Parameter
     */
    public void createEllipsoidUnit(Vector3D V, double rx, double ry, double rz, float col) {
        createEllipsoid((int) Math.round(V.getX() / resXY), (int) Math.round(V.getY() / resXY), (int) Math.round(V.getZ() / resZ), rx / resXY, ry / resXY, rz / resZ, col, false);
    }

    /**
     * Creation d'une ellipsoide
     *
     * @param rx Rayon en x (unit)
     * @param ry Rayon en y (unit)
     * @param rz Rayon en z (unit)
     * @param centerx Centre en x
     * @param centery Centre en y
     * @param centerz Centre en z
     * @param value Valeur à remplir
     * @param gauss uniform or ramp values
     */
    public void createEllipsoid(int centerx, int centery, int centerz, double rx, double ry, double rz, float value, boolean gauss) {
        // scale radius to pixel unit
        //rx /= resXY;
        //ry /= resXY;
        //rz /= resZ;

        int startx = (int) Math.round(centerx - rx);
        if (startx < 0) {
            startx = 0;
        }
        int starty = (int) Math.round(centery - ry);
        if (starty < 0) {
            starty = 0;
        }
        int startz = (int) Math.round(centerz - rz);
        if (startz < 0) {
            startz = 0;
        }
        int endx = (int) Math.round(centerx + rx);
        if (endx >= img.sizeX) {
            endx = img.sizeX - 1;
        }
        int endy = (int) Math.round(centery + ry);
        if (endy >= img.sizeY) {
            endy = img.sizeY - 1;
        }
        int endz = (int) Math.round(centerz + rz);
        if (endz >= img.sizeZ) {
            endz = img.sizeZ - 1;
        }
        double rx2 = rx * rx;
        double ry2 = ry * ry;
        double rz2 = rz * rz;
        for (int k = startz; k <= endz; k++) {
            float dz = k - centerz;
            for (int j = starty; j <= endy; j++) {
                float dy = j - centery;
                for (int i = startx; i <= endx; i++) {
                    float dx = i - centerx;
                    double d = (dx * dx) / (rx2) + (dy * dy) / (ry2) + (dz * dz) / (rz2);
                    if (d <= 1.0) {
                        if (gauss) {
                            img.setPixel(i, j, k, (float) (value * (1.0 - d / 2)));
                        } else {
                            img.setPixel(i, j, k, value);
                        }
                    }
                }
            }
        }
    }

    /**
     * Creation d'une ellipsoide (data in pix)
     *
     * @param rx Rayon en x
     * @param ry Rayon en y
     * @param rz Rayon en z
     * @param centerx Centre en x
     * @param centery Centre en y
     * @param centerz Centre en z
     * @param value Valeur à remplir
     * @param gauss uniform or ramp values
     * @param M Description of the Parameter
     */
    public void createEllipsoidAxes(int centerx, int centery, int centerz, double rx, double ry, double rz, float value, Matrix M, boolean gauss) {

        double radius = Math.max(rx, Math.max(ry, rz));
        int startx = (int) (centerx - radius);
        if (startx < 0) {
            startx = 0;
        }
        int starty = (int) (centery - radius);
        if (starty < 0) {
            starty = 0;
        }
        int startz = (int) (centerz - radius);
        if (startz < 0) {
            startz = 0;
        }
        int endx = (int) (centerx + radius);
        if (endx >= img.sizeX) {
            endx = img.sizeX - 1;
        }
        int endy = (int) (centery + radius);
        if (endy >= img.sizeY) {
            endy = img.sizeY - 1;
        }
        int endz = (int) (centerz + radius);
        if (endz >= img.sizeZ) {
            endz = img.sizeZ - 1;
        }

        double ddx;
        double ddy;
        double ddz;
        double dx;
        double dy;
        double dz;

        double rx2 = rx * rx;
        double ry2 = ry * ry;
        double rz2 = rz * rz;

        // inverse
        Matrix MM = M.inverse();

        Matrix V1 = new Matrix(3, 1);
        Matrix V2;

        double d;

        // work in pixel coordinate
        for (int k = startz; k <= endz; k++) {
            IJ.showStatus("Ellipsoid " + k + "/" + endz);
            ddz = (k - centerz);
            for (int j = starty; j <= endy; j++) {
                ddy = (j - centery);
                for (int i = startx; i <= endx; i++) {
                    ddx = (i - centerx);

                    // vector in column
                    V1.set(0, 0, ddx);
                    V1.set(1, 0, ddy);
                    V1.set(2, 0, ddz);

                    // multiplication
                    V2 = MM.times(V1);

                    // result
                    dx = V2.get(0, 0);
                    dy = V2.get(1, 0);
                    dz = V2.get(2, 0);

                    // ellipsoid
                    d = (dx * dx) / (rx2) + (dy * dy) / (ry2) + (dz * dz) / (rz2);
                    if (d <= 1.0) {
                        if (gauss) {
                            img.setPixel(i, j, k, (float) (value * (1.0 - d / 2)));
                        } else {
                            img.setPixel(i, j, k, value);
                        }
                        //System.out.println(ddx + " " + ddy + " " + ddz + " | " + dx + " " + dy + " " + dz);
                    }
                }
            }
        }
    }

    /**
     * Creation d'une ellipsoide
     *
     * @param rx Rayon en x (unit)
     * @param ry Rayon en y (unit)
     * @param rz Rayon en z (unit)
     * @param centerx Centre en x
     * @param centery Centre en y
     * @param centerz Centre en z
     * @param value Valeur à remplir
     * @param M Description of the Parameter
     */
    public void createBrickAxes(int centerx, int centery, int centerz, double rx, double ry, double rz, float value, Matrix M) {

        double radius = Math.max(rx, Math.max(ry, rz));
        int startx = (int) (centerx - radius);
        if (startx < 0) {
            startx = 0;
        }
        int starty = (int) (centery - radius);
        if (starty < 0) {
            starty = 0;
        }
        int startz = (int) (centerz - radius);
        if (startz < 0) {
            startz = 0;
        }
        int endx = (int) (centerx + radius);
        if (endx >= img.sizeX) {
            endx = img.sizeX - 1;
        }
        int endy = (int) (centery + radius);
        if (endy >= img.sizeY) {
            endy = img.sizeY - 1;
        }
        int endz = (int) (centerz + radius);
        if (endz >= img.sizeZ) {
            endz = img.sizeZ - 1;
        }

        double ddx;
        double ddy;
        double ddz;
        double dx;
        double dy;
        double dz;

        // inverse
        Matrix MM = M.inverse();

        Matrix V1 = new Matrix(3, 1);
        Matrix V2;

        // work in unit coordinate
        for (int k = startz; k <= endz; k++) {
            IJ.showStatus("Brick " + k + "/" + endz);
            ddz = resZ * (k - centerz);
            for (int j = starty; j <= endy; j++) {
                ddy = resXY * (j - centery);
                for (int i = startx; i <= endx; i++) {
                    ddx = resXY * (i - centerx);

                    // vector in column
                    V1.set(0, 0, ddx);
                    V1.set(1, 0, ddy);
                    V1.set(2, 0, ddz);

                    // multiplication
                    V2 = MM.times(V1);

                    // result
                    dx = V2.get(0, 0);
                    dy = V2.get(1, 0);
                    dz = V2.get(2, 0);

                    // brick
                    if ((Math.abs(dx) <= rx) && (Math.abs(dy) <= ry) && (Math.abs(dz) <= rz)) {
                        img.setPixel(i, j, k, value);
                    }
                    //System.out.println(ddx + " " + ddy + " " + ddz + " | " + dx + " " + dy + " " + dz);

                }
            }
        }
    }

    /**
     *
     * @param centerx
     * @param centery
     * @param centerz
     * @param rx
     * @param ry
     * @param rz
     * @param value
     * @param V
     * @param W
     * @param gauss
     */
    public void createEllipsoidAxesUnit(double centerx, double centery, double centerz, double rx, double ry, double rz, float value, Vector3D V, Vector3D W, boolean gauss) {
        ComputeEllipsoidAxesUnit(centerx, centery, centerz, rx, ry, rz, value, V, W, gauss);
    }

    /**
     *
     * @param centerx
     * @param centery
     * @param centerz
     * @param rx
     * @param ry
     * @param rz
     * @param value
     * @param V
     */
    public void createBrickAxesUnit(double centerx, double centery, double centerz, double rx, double ry, double rz, float value, Vector3D V) {
        // scale radius to pixel unit
        rx /= resXY;
        ry /= resXY;
        rz /= resZ;

        // scale radius to pixel unit
        centerx /= resXY;
        centery /= resXY;
        centerz /= resZ;

        this.createBrickAxes((int) Math.round(centerx), (int) Math.round(centery), (int) Math.round(centerz), rx, ry, rz, value, V);
    }

    /**
     * Create ellipsoid oriented, due to calibration and change in axes, radius
     * are still in unit
     *
     * @param centerx center of ellipsoid coordinate x
     * @param centery center of ellipsoid coordinate y
     * @param centerz center of ellipsoid coordinate z
     * @param rx radius of ellipsoid in x direction (main direction)
     * @param ry radius of ellipsoid in y direction
     * @param rz radius of ellipsoid in z direction
     * @param value value of drawing
     * @param V the vector indicating the main direction
     * @param gradient drawing with gradient of values or not
     */
    private void ComputeEllipsoidAxesUnit(double centerx, double centery, double centerz, double rx, double ry, double rz, float value, Vector3D V, Vector3D W, boolean gradient) {
        //IJ.log("ellipsoid " + rx + " " + ry + " " + rz);

        V.normalize();
        W.normalize();
        Vector3D X = V.crossProduct(W);
        X.normalize();

//        Matrix M = new Matrix(3, 3);
//        // V
//        M.set(0, 0, V.getX());
//        M.set(1, 0, V.getY());
//        M.set(2, 0, V.getZ());
//        // W
//        M.set(0, 1, W.getX());
//        M.set(1, 1, W.getY());
//        M.set(2, 1, W.getZ());
//        // X
//        M.set(0, 2, X.getX());
//        M.set(1, 2, X.getY());
//        M.set(2, 2, X.getZ());
        // geomtransform
        GeomTransform3D transform = new GeomTransform3D(new double[][]{
            {rx * V.getX(), ry * W.getX(), rz * X.getX(), centerx},
            {rx * V.getY(), ry * W.getY(), rz * X.getY(), centery},
            {rx * V.getZ(), ry * W.getZ(), rz * X.getZ(), centerz},
            {0, 0, 0, 1}});

        double d;
        Vector3D zero_vector = new Vector3D(0, 0, 0);
        double x, y, z;
        int ix, iy, iz;

        // radii and step
        double rad = Math.max(rx, Math.max(ry, rz));
        double reso = Math.min(resXY, resZ);
        double radpix = rad / reso;
        double step = 0.5 / radpix;
        //IJ.log("rad=" + radpix + " step=" + step);
        // work in pixel coordinate
        for (float k = -1; k <= 1; k += step) {
            IJ.showStatus("Ellipsoid generation " + k);
            for (float j = -1; j <= 1; j += step) {
                for (float i = -1; i <= 1; i += step) {
                    // sphere radius 1 is transformed 
                    d = (i * i) + (j * j) + (k * k);
                    if (d <= 1.0) {
                        Vector3D point = new Vector3D(i, j, k);
                        Vector3D res = transform.getVectorTransformed(point, zero_vector);
                        x = res.getX();
                        y = res.getY();
                        z = res.getZ();
                        ix = (int) Math.round(x / resXY);
                        iy = (int) Math.round(y / resXY);
                        iz = (int) Math.round(z / resZ);
                        if (img.contains(ix, iy, iz)) {
                            if (gradient) {
                                img.setPixel(ix, iy, iz, (float) (value * (1.0 - d / 2)));
                            } else {
                                img.setPixel(ix, iy, iz, value);
                            }
                        }
                        //System.out.println(ddx + " " + ddy + " " + ddz + " | " + dx + " " + dy + " " + dz);
                    }
                }
            }
        }
    }

    /**
     *
     * @param centerx
     * @param centery
     * @param centerz
     * @param rx
     * @param ry
     * @param rz
     * @param value
     * @param V
     */
    public void createBrickAxes(int centerx, int centery, int centerz, double rx, double ry, double rz, float value, Vector3D V) {
        V.normalize();
        Vector3D W = V.getRandomPerpendicularVector();
        W.normalize();
        Vector3D X = V.crossProduct(W);
        X.normalize();

        Matrix M = new Matrix(3, 3);
        // V
        M.set(0, 0, V.getX());
        M.set(1, 0, V.getY());
        M.set(2, 0, V.getZ());
        // W
        M.set(0, 1, W.getX());
        M.set(1, 1, W.getY());
        M.set(2, 1, W.getZ());
        // X
        M.set(0, 2, X.getX());
        M.set(1, 2, X.getY());
        M.set(2, 2, X.getZ());

        createBrickAxes(centerx, centery, centerz, rx, ry, rz, value, M);
    }

    /**
     * Description of the Method
     *
     * @param V Description of the Parameter
     * @param rx Description of the Parameter
     * @param ry Description of the Parameter
     * @param rz Description of the Parameter
     * @param val Description of the Parameter
     */
    public void createBrick(Vector3D V, double rx, double ry, double rz, float val) {
        createBrick(V.getRoundX(), V.getRoundY(), V.getRoundZ(), rx, ry, rz, val);
    }

    /**
     * Description of the Method
     *
     * @param V Description of the Parameter
     * @param rx Description of the Parameter
     * @param ry Description of the Parameter
     * @param rz Description of the Parameter
     * @param val Description of the Parameter
     */
    public void createBrickUnit(Vector3D V, double rx, double ry, double rz, float val) {
        createBrick(V.multiply(1.0 / resXY, 1.0 / resXY, 1.0 / resZ), rx / resXY, ry / resXY, rz / resZ, val);
    }

    /**
     * Description of the Method
     *
     * @param centerx Description of the Parameter
     * @param centery Description of the Parameter
     * @param centerz Description of the Parameter
     * @param rx Description of the Parameter
     * @param ry Description of the Parameter
     * @param rz Description of the Parameter
     * @param value Description of the Parameter
     */
    public void createBrickUnit(double centerx, double centery, double centerz, double rx, double ry, double rz, float value) {
        // scale to pixel
        createBrick((int) Math.round(centerx / resXY), (int) Math.round(centery / resXY), (int) Math.round(centerz / resZ), rx / resXY, ry / resXY, rz / resZ, value);
    }

    /**
     * Creation d'une brique
     *
     * @param rx Rayon en x (unit)
     * @param ry Rayon en y (unit)
     * @param rz Rayon en z (unit)
     * @param centerx Centre en x
     * @param centery Centre en y
     * @param centerz Centre en z
     * @param value Valeur à remplir
     */
    public void createBrick(int centerx, int centery, int centerz, double rx, double ry, double rz, float value) {
        int startx = (int) (centerx - rx);
        if (startx < 0) {
            startx = 0;
        }
        int starty = (int) (centery - ry);
        if (starty < 0) {
            starty = 0;
        }
        int startz = (int) (centerz - rz);
        if (startz < 0) {
            startz = 0;
        }
        int endx = (int) (centerx + rx);
        if (endx >= img.sizeX) {
            endx = img.sizeX - 1;
        }
        int endy = (int) (centery + ry);
        if (endy >= img.sizeY) {
            endy = img.sizeY - 1;
        }
        int endz = (int) (centerz + rz);
        if (endz >= img.sizeZ) {
            endz = img.sizeZ - 1;
        }
        for (int k = startz; k <= endz; k++) {
            for (int j = starty; j <= endy; j++) {
                for (int i = startx; i <= endx; i++) {
                    img.setPixel(i, j, k, value);
                }
            }
        }
    }

    /**
     * Description of the Method
     *
     * @param x Description of the Parameter
     * @param y Description of the Parameter
     * @param z Description of the Parameter
     * @param col Description of the Parameter
     */
    public void createPixel(int x, int y, int z, int col) {
        img.setPixel(x, y, z, col);
    }

    /**
     * Create a 3D line
     *
     * @param x0 x origin
     * @param y0 y origin
     * @param z0 z origin
     * @param x1 x end
     * @param y1 y end
     * @param z1 z end
     * @param val pixel value
     * @param rad radius for the line (as small spheres) (pix)
     */
    public void createLine(int x0, int y0, int z0, int x1, int y1, int z1, float val, int rad) {
        Vector3D V = new Vector3D(x1 - x0, y1 - y0, z1 - z0);
        double len = V.getLength();
        V.normalize();
        double vx = V.getX();
        double vy = V.getY();
        double vz = V.getZ();
        for (int i = 0; i < (int) len; i++) {
            if (rad == 0) {
                img.setPixel((int) (x0 + i * vx), (int) (y0 + i * vy), (int) (z0 + i * vz), val);
            } else {
                createEllipsoid((int) (x0 + i * vx), (int) (y0 + i * vy), (int) (z0 + i * vz), rad, rad, rad, val, false);
            }
        }
    }

    /**
     * Description of the Method
     *
     * @param V1 Description of the Parameter
     * @param val Description of the Parameter
     * @param rad Description of the Parameter
     * @param V2 Description of the Parameter
     */
    public void createLine(Vector3D V1, Vector3D V2, float val, int rad) {
        createLine(V1.getRoundX(), V1.getRoundY(), V1.getRoundZ(), V2.getRoundX(), V2.getRoundY(), V2.getRoundZ(), val, rad);
    }

    /**
     *
     * @param P1
     * @param P2
     * @param val
     * @param rad
     */
    public void createLine(Point3D P1, Point3D P2, float val, int rad) {
        createLine(P1.getRoundX(), P1.getRoundY(), P1.getRoundZ(), P2.getRoundX(), P2.getRoundY(), P2.getRoundZ(), val, rad);
    }

    /**
     * Description of the Method
     *
     * @param V1 Description of the Parameter
     * @param V2 Description of the Parameter
     * @param val Description of the Parameter
     * @param rad Description of the Parameter
     */
    public void createLineUnit(Vector3D V1, Vector3D V2, float val, int rad) {
        createLine((int) (V1.getX() / resXY), (int) (V1.getY() / resXY), (int) (V1.getZ() / resZ), (int) (V2.getX() / resXY), (int) (V2.getY() / resXY), (int) (V2.getZ() / resZ), val, rad);
    }

    // DF
    /**
     * Creation d'un cylindre
     *
     * @param centerx Centre sur l'axe X
     * @param centery Centre sur l'axe Y
     * @param centerz Centre sur l'axe Z
     * @param radius Rayon du cylindre
     * @param height Epaisseur du cylindre
     * @param axe 1 : X, 2 : Y et 3 : Z
     * @param value Valeur associé au pixel
     *
     */
    public void createCylinder(int centerx, int centery, int centerz, double rx, double ry, double height, float value) {
        int startx = (int) Math.round(centerx - rx);
        if (startx < 0) {
            startx = 0;
        }
        int starty = (int) Math.round(centery - ry);
        if (starty < 0) {
            starty = 0;
        }
        int startz = (int) Math.round(centerz - height / 2);
        if (startz < 0) {
            startz = 0;
        }
        int endx = (int) Math.round(centerx + rx);
        if (endx >= img.sizeX) {
            endx = img.sizeX - 1;
        }
        int endy = (int) Math.round(centery + ry);
        if (endy >= img.sizeY) {
            endy = img.sizeY - 1;
        }
        int endz = (int) Math.round(centerz + height / 2);
        if (endz >= img.sizeZ) {
            endz = img.sizeZ - 1;
        }
        double rx2 = rx * rx;
        double ry2 = ry * ry;
        for (int k = startz; k <= endz; k++) {
            for (int j = starty; j <= endy; j++) {
                float dy = j - centery;
                for (int i = startx; i <= endx; i++) {
                    float dx = i - centerx;
                    double d = (dx * dx) / (rx2) + (dy * dy) / (ry2);
                    if (d <= 1.0) {
                        img.setPixel(i, j, k, value);

                    }
                }
            }
        }
    }

    /**
     * Creation d'un cylindre
     *
     * @param rx Rayon en x (unit)
     * @param ry Rayon en y (unit)
     * @param rz Rayon en z (unit)
     * @param centerx Centre en x
     * @param centery Centre en y
     * @param centerz Centre en z
     * @param value Valeur à remplir
     * @param M Description of the Parameter
     */
    public void createCylinderAxes(int centerx, int centery, int centerz, double rx, double ry, double height, float value, Matrix M) {

        double radius = Math.max(rx, Math.max(ry, height / 2));
        int startx = (int) (centerx - radius);
        if (startx < 0) {
            startx = 0;
        }
        int starty = (int) (centery - radius);
        if (starty < 0) {
            starty = 0;
        }
        int startz = (int) (centerz - radius);
        if (startz < 0) {
            startz = 0;
        }
        int endx = (int) (centerx + radius);
        if (endx >= img.sizeX) {
            endx = img.sizeX - 1;
        }
        int endy = (int) (centery + radius);
        if (endy >= img.sizeY) {
            endy = img.sizeY - 1;
        }
        int endz = (int) (centerz + radius);
        if (endz >= img.sizeZ) {
            endz = img.sizeZ - 1;
        }

        double ddx, ddy, ddz;
        double dx, dy, dz;
        double rx2 = rx * rx;
        double ry2 = ry * ry;
        // inverse
        Matrix MM = M.inverse();

        Matrix V1 = new Matrix(3, 1);
        Matrix V2;

        // work in pixel coordinate
        for (int k = startz; k <= endz; k++) {
            IJ.showStatus("Cylinder " + k + "/" + endz);
            ddz = (k - centerz);
            for (int j = starty; j <= endy; j++) {
                ddy = (j - centery);
                for (int i = startx; i <= endx; i++) {
                    ddx = (i - centerx);

                    // vector in column
                    V1.set(0, 0, ddx);
                    V1.set(1, 0, ddy);
                    V1.set(2, 0, ddz);

                    // multiplication
                    V2 = MM.times(V1);

                    // result
                    dx = V2.get(0, 0);
                    dy = V2.get(1, 0);
                    dz = V2.get(2, 0);

                    // cylinder
                    if (Math.abs(dz) < height / 2) {
                        double d = (dx * dx) / (rx2) + (dy * dy) / (ry2);
                        if (d <= 1.0) {
                            img.setPixel(i, j, k, value);
                        }
                    }
                    //System.out.println(ddx + " " + ddy + " " + ddz + " | " + dx + " " + dy + " " + dz);

                }
            }
        }
    }

    public void createCylinderAxes(int centerx, int centery, int centerz, double rx, double ry, double height, float value, Vector3D V, Vector3D W) {
        V.normalize();
        W.normalize();
        Vector3D X = V.crossProduct(W);
        X.normalize();

        Matrix M = new Matrix(3, 3);
        // V
        M.set(0, 0, V.getX());
        M.set(1, 0, V.getY());
        M.set(2, 0, V.getZ());
        // W
        M.set(0, 1, W.getX());
        M.set(1, 1, W.getY());
        M.set(2, 1, W.getZ());
        // X
        M.set(0, 2, X.getX());
        M.set(1, 2, X.getY());
        M.set(2, 2, X.getZ());

        createCylinderAxes(centerx, centery, centerz, rx, ry, height, value, M);
    }

    /**
     * Création d'un dégradé
     *
     * @param center Coordonées du centre du cube
     * @param rx Demi-largeur du cube
     * @param ry Demi-hauteur du cube
     * @param rz Demi-profondeur du cube
     * @param g Gradient
     */
    public void createGradientCube(Vector3D center, double rx, double ry, double rz, Gradient g) {
        this.createGradientCube(center.getRoundX(), center.getRoundY(), center.getRoundZ(), rx, ry, rz, g);
    }

    /**
     * Création d'un dégradé sur cube
     *
     * @param centerx Centre X du cube
     * @param centery Centre Y du cube
     * @param centerz Centre Z du cube
     * @param rx Demi-largeur du cube
     * @param ry Demi-hauteur du cube
     * @param rz Demi-profondeur du cube
     * @param g Gradient
     */
    public void createGradientCube(int centerx, int centery, int centerz, double rx, double ry, double rz, Gradient g) {
        this.createGradient(centerx, centery, centerz, rx, ry, rz, false, g);
    }

    /**
     * Création d'un gradient sur sphère
     *
     * @param center Coordonées du centre de la sphère
     * @param r Rayon de la sphère
     * @param g Gradient
     */
    public void createGradientSphere(Point3D center, double r, Gradient g) {
        this.createGradientSphere(center.getRoundX(), center.getRoundY(), center.getRoundZ(), r, g);
    }

    /**
     * Création d'un gradient sur ellipsoïde
     *
     * @param centerx Centre X de la sphère
     * @param centery Centre Y de la sphère
     * @param centerz Centre Z de la sphère
     * @param r Rayon de la sphère
     * @param g Gradient
     */
    public void createGradientSphere(int centerx, int centery, int centerz, double r, Gradient g) {
        this.createGradient(centerx, centery, centerz, r, r, r, true, g);
    }

    /**
     * Création d'un gradient quelqu'il soit (sphérique ou non)
     *
     * @param centerx Centre du gradient
     * @param centery Centre du gradient
     * @param centerz Centre du gradient
     * @param rx Rayon ou demi-largeur
     * @param ry Demi-hauteur
     * @param rz Demi-profondeur
     * @param sphere Si true : il s'agit d'une sphère, sinon il s'agit d'un
     * rectangle
     * @param g Gradient
     */
    private void createGradient(int centerx, int centery, int centerz, double rx, double ry, double rz, boolean sphere, Gradient g) {
        int left = (int) (centerx - rx);
        int top = (int) (centery - ry);
        int front = (int) (centerz - rz);

        int right = (int) (centerx + rx - 1);
        int bottom = (int) (centery + ry - 1);
        int back = (int) (centerz + rz - 1);

        int drawingLeft = (int) Math.max(left, 0);
        int drawingTop = (int) Math.max(top, 0);
        int drawingFront = (int) Math.max(front, 0);

        int drawingRight = (int) Math.min(right, img.sizeX);
        int drawingBottom = (int) Math.min(bottom, img.sizeY);
        int drawingBack = (int) Math.min(back, img.sizeZ);

        double lpr2 = (double) (left + right) / 2;
        double tpb2 = (double) (top + bottom) / 2;
        double fpb2 = (double) (front + back) / 2;
        double r2 = rx * rx;

        double rml = (double) (right - left);
        double bmt = (double) (bottom - top);
        double bmf = (double) (back - front);

        for (int x = drawingLeft; x < drawingRight; x++) {
            for (int y = drawingTop; y < drawingBottom; y++) {
                for (int z = drawingFront; z < drawingBack; z++) {
                    double l2 = (lpr2 - x) * (lpr2 - x) + (tpb2 - y) * (tpb2 - y) + (fpb2 - z) * (fpb2 - z);

                    if (!sphere || l2 < r2) {
                        double pcentX = (double) (x - left) / rml;
                        double pcentY = (double) (y - top) / bmt;
                        double pcentZ = (double) (z - front) / bmf;

                        float imgPx = (int) Math.round(img.getPixel(x, y, z));

                        int pixel;
                        if (sphere) {
                            pixel = g.getRadialPixelAt(pcentX, pcentY, pcentZ);
                        } else {
                            pixel = g.getLinearPixelAt(pcentX, pcentY, pcentZ);
                        }

                        pixel = (int) Math.round(imgPx + g.getLastAlpha() * (pixel - imgPx));

                        this.createPixel(x, y, z, pixel);
                    }
                }
            }
        }

        g.reset();
    }

    /**
     * Returns an ImageStack
     *
     * @return The IJstack
     */
    public ImageStack getStack() {
        ImageStack stack = img.getImageStack();
        // should calibrate the IJstack
        return stack;
    }

    public ImagePlus getPlus() {
        ImagePlus plus = new ImagePlus("ObjectCreator3D", img.getImageStack());
        Calibration cal = new Calibration();
        cal.pixelWidth = resXY;
        cal.pixelHeight = resXY;
        cal.pixelDepth = resZ;
        cal.setUnit(unit);
        plus.setCalibration(cal);

        return plus;
    }

    public void clear() {
        img.fill(0);
    }

    /**
     * Gets the image3D attribute of the ObjectCreator3D object
     *
     * @return The image3D value
     */
    public ImageHandler getImageHandler() {
        return img;
    }

    public Object3DVoxels getObject3DVoxels(int val) {
        return new Object3DVoxels(img, val);
    }

    public Image3D getImage3D() {
        return img.getImage3D();
    }

    /**
     *
     */
    public void reset() {
        img.erase();
    }

    public void drawVoxels(ArrayList<Voxel3D> voxels) {
        for (Voxel3D vox : voxels) {
            int x = vox.getRoundX();
            int y = vox.getRoundY();
            int z = vox.getRoundZ();
            if (img.contains(x, y, z)) {
                img.setPixel(x, y, z, (int) vox.getValue());
            }
        }
    }

    public void drawObject(Object3D obj) {
        obj.draw(img);
    }

    /**
     *
     * @param nb
     */
    public void reset(int nb) {
        img.fill(nb);
    }
}
