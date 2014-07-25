package mcib3d.image3d;

import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.io.FileInfo;
import ij.io.FileSaver;
import ij.io.Opener;
import ij.io.TiffEncoder;
import ij.measure.Calibration;
import ij.plugin.ZProjector;
import ij.process.ImageProcessor;
import imagescience.feature.Differentiator;
import imagescience.feature.Edges;
import imagescience.feature.Hessian;
import imagescience.feature.Structure;
import imagescience.image.FloatImage;
import imagescience.image.Image;
import java.awt.image.IndexColorModel;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.TreeMap;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import mcib3d.geom.Object3D;
import mcib3d.geom.Point3D;
import mcib3d.geom.Vector3D;
import mcib3d.geom.Voxel3D;
import mcib3d.image3d.distanceMap3d.EDT;
import mcib3d.image3d.legacy.Image3D;
import mcib3d.image3d.processing.FJDifferentiator3D;
import mcib3d.image3d.processing.FJHessian3D;
import mcib3d.image3d.processing.FJStructure3D;
import mcib3d.image3d.processing.Thresholder;
import mcib3d.utils.ArrayUtil;
import mcib3d.utils.ThreadRunner;
import mcib3d.utils.exceptionPrinter;

/**
 * Copyright (C) 2012 Jean Ollion
 *
 *
 *
 * This file is part of tango
 *
 * tango is free software; you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 3 of the License, or (at your option) any later
 * version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 *
 * @author Jean Ollion
 * @author Thomas Boudier
 */
public abstract class ImageHandler {

    public int sizeX, sizeY, sizeZ, sizeXY, sizeXYZ, offsetX, offsetY, offsetZ;
    protected ImagePlus img;
    protected String title;
    HashMap<ImageHandler, ImageStats> stats = new HashMap<ImageHandler, ImageStats>(2);

    protected ImageHandler(ImagePlus img) {
        this.img = img;
        this.title = img.getShortTitle();
        this.sizeX = img.getWidth();
        this.sizeY = img.getHeight();
        this.sizeZ = img.getNSlices();
        this.sizeXY = this.sizeX * this.sizeY;
        this.sizeXYZ = this.sizeXY * this.sizeZ;
    }

    protected ImageHandler(ImageStack stack) {
        this.img = new ImagePlus("Image", stack);
        this.title = img.getShortTitle();
        this.sizeX = img.getWidth();
        this.sizeY = img.getHeight();
        this.sizeZ = img.getNSlices();
        this.sizeXY = this.sizeX * this.sizeY;
        this.sizeXYZ = this.sizeXY * this.sizeZ;
    }

    protected ImageHandler(String title, int sizeX, int sizeY, int sizeZ) {
        this.title = title;
        this.sizeX = sizeX;
        this.sizeY = sizeY;
        this.sizeZ = sizeZ;
        this.sizeXY = sizeX * sizeY;
        this.sizeXYZ = sizeXY * sizeZ;
    }

    protected ImageHandler(String title, int sizeX, int sizeY, int sizeZ, int offsetX, int offsetY, int offsetZ) {
        this.title = title;
        this.sizeX = sizeX;
        this.sizeY = sizeY;
        this.sizeZ = sizeZ;
        this.sizeXY = sizeX * sizeY;
        this.sizeXYZ = sizeXY * sizeZ;
        this.offsetX = offsetX;
        this.offsetY = offsetY;
        this.offsetZ = offsetZ;
    }

    public abstract double getSizeInMb();

    public ImagePlus getImagePlus() {
        return img;
    }

    public abstract int getType();

    public String getTitle() {
        return title;
    }

    public boolean sameDimentions(ImageHandler other) {
        return (sizeX == other.sizeX && sizeY == other.sizeY && sizeZ == other.sizeZ);
    }

    public boolean sameDimentions(ImagePlus other) {
        return (sizeX == other.getWidth() && sizeY == other.getHeight() && sizeZ == other.getNSlices());
    }

    public boolean contains(int x, int y, int z) {
        return (x >= 0 && x < sizeX && y >= 0 && y < sizeY && z >= 0 && z < sizeZ);
    }

    public boolean contains(double x, double y, double z) {
        return (x >= 0 && x < sizeX && y >= 0 && y < sizeY && z >= 0 && z < sizeZ);
    }

    public boolean contains(Voxel3D V) {
        return (V.x >= 0 && V.x < sizeX && V.y >= 0 && V.y < sizeY && V.z >= 0 && V.z < sizeZ);
    }

    public boolean maskContains(int x, int y, int z) {
        return (contains(x, y, z) && getPixel(x, y, z) != 0);
    }

    public abstract float getPixel(int coord);

    public abstract float getPixel(int x, int y, int z);

    public abstract float getPixel(int xy, int z);

    public abstract float getPixel(Point3D P);

    //public abstract int getPixelInt(Point3D P);
    public abstract float getPixelInterpolated(Point3D P);

    public float getPixel(float x, float y, float z) {
        // FIXME : 0 lorsquon est en dehors? meilleure approx = pixel voisin ? cf methode avec mask
        int xbase = (int) x;
        int ybase = (int) y;
        int zbase = (int) z;
        float xFraction = x - (float) xbase;
        float yFraction = y - (float) ybase;
        float zFraction = z - (float) zbase;
        float xyz = getPixel(xbase, ybase, zbase);
        float x1yz = (xbase + 1 < sizeX) ? getPixel(xbase + 1, ybase, zbase) : 0;
        float x1y1z = ((xbase + 1) < sizeX && (ybase + 1) < sizeY) ? getPixel(xbase + 1, ybase + 1, zbase) : 0;
        float x1y1z1 = ((xbase + 1) < sizeX && (ybase + 1) < sizeY && (zbase + 1) < sizeZ) ? getPixel(xbase + 1, ybase + 1, zbase + 1) : 0;
        float x1yz1 = ((xbase + 1) < sizeX && (zbase + 1) < sizeZ) ? getPixel(xbase + 1, ybase, zbase + 1) : 0;
        float xy1z = (ybase + 1 < sizeY) ? getPixel(xbase, ybase + 1, zbase) : 0;
        float xy1z1 = ((ybase + 1) < sizeY && (zbase + 1) < sizeZ) ? getPixel(xbase, ybase + 1, zbase + 1) : 0;
        float xyz1 = (zbase + 1 < sizeZ) ? getPixel(xbase, ybase, zbase + 1) : 0;
        float upperAvplane = xy1z + xFraction * (x1y1z - xy1z);
        float lowerAvplane = xyz + xFraction * (x1yz - xyz);
        float upperAvplane1 = xy1z1 + xFraction * (x1y1z1 - xy1z1);
        float lowerAvplane1 = xyz1 + xFraction * (x1yz1 - xyz1);
        float plane = lowerAvplane + yFraction * (upperAvplane - lowerAvplane);
        float plane1 = lowerAvplane1 + yFraction * (upperAvplane1 - lowerAvplane1);
        return (plane + zFraction * (plane1 - plane));
    }

    public float getPixel(float x, float y, float z, ImageInt mask) {
        // TODO optimiser! pas dappel de maskcontains
        int xbase = (int) x;
        int ybase = (int) y;
        int zbase = (int) z;
        float xFraction = x - (float) xbase;
        float yFraction = y - (float) ybase;
        float zFraction = z - (float) zbase;
        float xyz = mask.maskContains(xbase, ybase, zbase) ? getPixel(xbase, ybase, zbase) : Float.NaN;
        float x1yz = mask.maskContains(xbase + 1, ybase, zbase) ? getPixel(xbase + 1, ybase, zbase) : Float.NaN;
        float x1y1z = mask.maskContains(xbase + 1, ybase + 1, zbase) ? getPixel(xbase + 1, ybase + 1, zbase) : Float.NaN;
        float x1y1z1 = mask.maskContains(xbase + 1, ybase + 1, zbase + 1) ? getPixel(xbase + 1, ybase + 1, zbase + 1) : Float.NaN;
        float x1yz1 = mask.maskContains(xbase + 1, ybase, zbase + 1) ? getPixel(xbase + 1, ybase, zbase + 1) : Float.NaN;
        float xy1z = mask.maskContains(xbase, ybase + 1, zbase) ? getPixel(xbase, ybase + 1, zbase) : Float.NaN;
        float xy1z1 = mask.maskContains(xbase, ybase + 1, zbase + 1) ? getPixel(xbase, ybase + 1, zbase + 1) : Float.NaN;
        float xyz1 = mask.maskContains(xbase, ybase, zbase + 1) ? getPixel(xbase, ybase, zbase + 1) : Float.NaN;
        float upperAvplane = Float.isNaN(xy1z) ? x1y1z : Float.isNaN(x1y1z) ? xy1z : xy1z + xFraction * (x1y1z - xy1z);
        float lowerAvplane = Float.isNaN(xyz) ? x1yz : Float.isNaN(x1yz) ? xyz : xyz + xFraction * (x1yz - xyz);
        float upperAvplane1 = Float.isNaN(xy1z1) ? x1y1z1 : Float.isNaN(x1y1z1) ? xy1z1 : xy1z1 + xFraction * (x1y1z1 - xy1z1);
        float lowerAvplane1 = Float.isNaN(x1yz1) ? xyz1 : Float.isNaN(xyz1) ? x1yz1 : xyz1 + xFraction * (x1yz1 - xyz1);
        float plane = Float.isNaN(lowerAvplane) ? upperAvplane : Float.isNaN(upperAvplane) ? lowerAvplane : lowerAvplane + yFraction * (upperAvplane - lowerAvplane);
        float plane1 = Float.isNaN(lowerAvplane1) ? upperAvplane1 : Float.isNaN(upperAvplane1) ? lowerAvplane1 : lowerAvplane1 + yFraction * (upperAvplane1 - lowerAvplane1);
        return Float.isNaN(plane) ? plane1 : (Float.isNaN(plane1) ? Float.isNaN(xyz) ? getPixel(xbase, ybase, zbase) : xyz : ((plane + zFraction * (plane1 - plane)))); // FIXME pas classe!
    }

    /**
     * Gets the 3D neighborhood of the pixel in 8 connexion --> 27 pixels
     *
     * @param x x coordinate
     * @param y y coordinate
     * @param z z coordinate
     * @return The array with the neighborhood values
     */
    public ArrayUtil getNeighborhood3x3x3(int x, int y, int z) {
        ArrayUtil res = new ArrayUtil(27);
        int idx = 0;
        for (int k = z - 1; k <= z + 1; k++) {
            for (int j = y - 1; j <= y + 1; j++) {
                for (int i = x - 1; i <= x + 1; i++) {
                    if ((i >= 0) && (j >= 0) && (k >= 0) && (i < sizeX) && (j < sizeY) && (k < sizeZ)) {
                        res.putValue(idx, getPixel(i, j, k));
                        idx++;
                    }
                }
            }
        }
        res.setSize(idx);
        return res;
    }

    /**
     * 6-neighborhood in 3D cross of a given voxel
     *
     * @param x x-coordinate of the voxel
     * @param y y-coordinate of the voxel
     * @param z z-coordinate of the voxel
     * @return the ArrayUtil list of neighborhood voxels
     */
    public ArrayUtil getNeighborhoodCross3D(int x, int y, int z) {
        ArrayUtil res = new ArrayUtil(7);
        res.putValue(0, getPixel(x, y, z));
        int idx = 1;
        if ((x + 1) < sizeX) {
            res.putValue(idx, getPixel(x + 1, y, z));
            idx++;
        }
        if ((x - 1) >= 0) {
            res.putValue(idx, getPixel(x - 1, y, z));
            idx++;
        }
        if ((y + 1) < sizeY) {
            res.putValue(idx, getPixel(x, y + 1, z));
            idx++;
        }
        if ((y - 1) >= 0) {
            res.putValue(idx, getPixel(x, y - 1, z));
            idx++;
        }
        if ((z + 1) < sizeZ) {
            res.putValue(idx, getPixel(x, y, z + 1));
            idx++;
        }
        if ((z - 1) >= 0) {
            res.putValue(idx, getPixel(x, y, z - 1));
            idx++;
        }

        res.setSize(idx);
        return res;
    }

    /**
     * 6-neighborhood in 3D cross of a given voxel
     *
     * @param x x-coordinate of the voxel
     * @param y y-coordinate of the voxel
     * @param z z-coordinate of the voxel
     * @param excludeCenter exclude centre pixel from list
     * @return the ArrayUtil list of neighborhood voxels
     */
    public ArrayList<Voxel3D> getNeighborhoodCross3DList(int x, int y, int z, boolean excludeCenter) {
        ArrayList<Voxel3D> res = new ArrayList();
        if (!excludeCenter) {
            res.add(new Voxel3D(x, y, z, getPixel(x, y, z)));
        }
        if ((x + 1) < sizeX) {
            res.add(new Voxel3D(x + 1, y, z, getPixel(x + 1, y, z)));
        }
        if ((x - 1) >= 0) {
            res.add(new Voxel3D(x - 1, y, z, getPixel(x - 1, y, z)));
        }
        if ((y + 1) < sizeY) {
            res.add(new Voxel3D(x, y + 1, z, getPixel(x, y + 1, z)));
        }
        if ((y - 1) >= 0) {
            res.add(new Voxel3D(x, y - 1, z, getPixel(x, y - 1, z)));
        }
        if ((z + 1) < sizeZ) {
            res.add(new Voxel3D(x, y, z + 1, getPixel(x, y, z + 1)));
        }
        if ((z - 1) >= 0) {
            res.add(new Voxel3D(x, y, z - 1, getPixel(x, y, z - 1)));
        }

        return res;
    }

    /**
     * Gets the neighboring of a pixel (default=sphere)
     *
     * @param x Coordinate x of the pixel
     * @param y Coordinate y of the pixel
     * @param z Coordinate z of the pixel
     * @param radx Radius x of the neighboring
     * @param rady Radius y of the neighboring
     * @param radz Radius z of the neighboring
     * @return The neigbor values in a array
     */
    public ArrayUtil getNeighborhood(int x, int y, int z, float radx, float rady, float radz) {
        return getNeighborhoodSphere(x, y, z, radx, rady, radz);
    }

    /**
     * Gets the neighboring of a pixel (sphere)
     *
     * @param x Coordinate x of the pixel
     * @param y Coordinate y of the pixel
     * @param z Coordinate z of the pixel
     * @param radx Radius x of the neighboring
     * @param rady Radius y of the neighboring
     * @param radz Radius z of the neighboring
     * @return The neigbor values in a array
     */
    public ArrayUtil getNeighborhoodSphere(int x, int y, int z, float radx, float rady, float radz) {
        int index = 0;
        double rx2;

        if (radx != 0) {
            rx2 = radx * radx;
        } else {
            rx2 = 1;
        }
        double ry2;
        if (rady != 0) {
            ry2 = rady * rady;
        } else {
            ry2 = 1;
        }
        double rz2;
        if (radz != 0) {
            rz2 = radz * radz;
        } else {
            rz2 = 1;
        }
        double dist;

        int vx = (int) Math.ceil(radx);
        int vy = (int) Math.ceil(rady);
        int vz = (int) Math.ceil(radz);
        double[] pix = new double[(2 * vx + 1) * (2 * vy + 1) * (2 * vz + 1)];

        for (int k = z - vz; k <= z + vz; k++) {
            for (int j = y - vy; j <= y + vy; j++) {
                for (int i = x - vx; i <= x + vx; i++) {
                    if (i >= 0 && j >= 0 && k >= 0 && i < sizeX && j < sizeY && k < sizeZ) {
                        dist = ((x - i) * (x - i)) / rx2 + ((y - j) * (y - j)) / ry2 + ((z - k) * (z - k)) / rz2;
                        if (dist <= 1.0) {
                            //t.putValue(index, );
                            pix[index] = getPixel(i, j, k);
                            index++;
                        }
                    }
                }
            }
        }
        ArrayUtil t = new ArrayUtil(pix);
        t.setSize(index);
        return t;
    }

    /**
     * Gets the neighboring attribute of the Image3D with a kernel as a array
     *
     * @param ker The kernel array (>0 ok)
     * @param nbval The number of non-zero values
     * @param x Coordinate x of the pixel
     * @param y Coordinate y of the pixel
     * @param z Coordinate z of the pixel
     * @param radx Radius x of the neighboring
     * @param radz Radius y of the neighboring
     * @param rady Radius z of the neighboring
     * @return The values of the nieghbor pixels inside an array
     */
    public ArrayUtil getNeighborhoodKernel(int[] ker, int nbval, int x, int y, int z, float radx, float rady, float radz) {
        ArrayUtil pix = new ArrayUtil(nbval);
        int vx = (int) Math.ceil(radx);
        int vy = (int) Math.ceil(rady);
        int vz = (int) Math.ceil(radz);
        int index = 0;
        int c = 0;
        for (int k = z - vz; k <= z + vz; k++) {
            for (int j = y - vy; j <= y + vy; j++) {
                for (int i = x - vx; i <= x + vx; i++) {
                    if (ker[c] > 0) {
                        if ((i >= 0) && (j >= 0) && (k >= 0) && (i < sizeX) && (j < sizeY) && (k < sizeZ)) {
                            pix.putValue(index, getPixel(i, j, k));
                            index++;
                        }
                    }
                    c++;
                }
            }
        }

        pix.setSize(index);

        return pix;
    }

    public ArrayUtil getNeighborhoodKernel(Object3D obj, int x, int y, int z) {
        int nbval = obj.getVolumePixels();
        ArrayUtil pix = new ArrayUtil(nbval);
        ImageHandler seg = obj.getLabelImage();
        int[] bb = obj.getBoundingBox();
        float radx = (float) (0.5 * (bb[1] - bb[0]));
        float rady = (float) (0.5 * (bb[3] - bb[2]));
        float radz = (float) (0.5 * (bb[5] - bb[4]));
        int vx = (int) Math.ceil(radx);
        int vy = (int) Math.ceil(rady);
        int vz = (int) Math.ceil(radz);
        int index = 0;
        int zmin = z - vz;
        if (zmin < 0) {
            zmin = 0;
        }
        int zmax = z + vz;
        if (zmax >= this.sizeZ) {
            zmax = this.sizeZ - 1;
        }
        int ymin = y - vy;
        if (ymin < 0) {
            ymin = 0;
        }
        int ymax = y + vy;
        if (ymax >= this.sizeY) {
            ymax = this.sizeY - 1;
        }
        int xmin = x - vx;
        if (xmin < 0) {
            xmin = 0;
        }
        int xmax = x + vx;
        if (xmax >= this.sizeX) {
            xmax = this.sizeX - 1;
        }
        int tx = xmin - bb[0];
        int ty = ymin - bb[2];
        int tz = zmin - bb[4];
        for (int k = zmin; k <= zmax; k++) {
            for (int j = ymin; j <= ymax; j++) {
                for (int i = xmin; i <= xmax; i++) {
                    if ((seg.contains(i - tx, j - ty, k - tz)) && (seg.getPixel(i - tx, j - ty, k - tz) > 0)) {
                        pix.putValue(index, this.getPixel(i, j, k));
                        index++;
                    }
                }
            }
        }

        pix.setSize(index);

        return pix;
    }

    public ArrayUtil getNeighborhoodKernelAdd(Object3D obj, ImageHandler img, int x, int y, int z) {
        int nbval = obj.getVolumePixels();
        ArrayUtil pix = new ArrayUtil(nbval);
        ImageHandler seg = obj.getLabelImage();
        int[] bb = obj.getBoundingBox();
        float radx = (float) (0.5 * (bb[1] - bb[0]));
        float rady = (float) (0.5 * (bb[3] - bb[2]));
        float radz = (float) (0.5 * (bb[5] - bb[4]));
        int vx = (int) Math.ceil(radx);
        int vy = (int) Math.ceil(rady);
        int vz = (int) Math.ceil(radz);
        int index = 0;
        int zmin = z - vz;
        if (zmin < 0) {
            zmin = 0;
        }
        int zmax = z + vz;
        if (zmax >= this.sizeZ) {
            zmax = this.sizeZ - 1;
        }
        int ymin = y - vy;
        if (ymin < 0) {
            ymin = 0;
        }
        int ymax = y + vy;
        if (ymax >= this.sizeY) {
            ymax = this.sizeY - 1;
        }
        int xmin = x - vx;
        if (xmin < 0) {
            xmin = 0;
        }
        int xmax = x + vx;
        if (xmax >= this.sizeX) {
            xmax = this.sizeX - 1;
        }
        int tx = xmin - bb[0];
        int ty = ymin - bb[2];
        int tz = zmin - bb[4];
        for (int k = zmin; k <= zmax; k++) {
            for (int j = ymin; j <= ymax; j++) {
                for (int i = xmin; i <= xmax; i++) {
                    if ((seg.contains(i - tx, j - ty, k - tz)) && (seg.getPixel(i - tx, j - ty, k - tz) > 0)) {
                        pix.putValue(index, this.getPixel(i, j, k) + img.getPixel(i - tx, j - ty, k - tz));
                        index++;
                    }
                }
            }
        }

        pix.setSize(index);

        return pix;
    }

    public ArrayUtil getNeighborhoodKernelSubstract(Object3D obj, ImageHandler img, int x, int y, int z) {
        int nbval = obj.getVolumePixels();
        ArrayUtil pix = new ArrayUtil(nbval);
        ImageHandler seg = obj.getLabelImage();
        int[] bb = obj.getBoundingBox();
        float radx = (float) (0.5 * (bb[1] - bb[0]));
        float rady = (float) (0.5 * (bb[3] - bb[2]));
        float radz = (float) (0.5 * (bb[5] - bb[4]));
        int vx = (int) Math.ceil(radx);
        int vy = (int) Math.ceil(rady);
        int vz = (int) Math.ceil(radz);
        int index = 0;
        int zmin = z - vz;
        if (zmin < 0) {
            zmin = 0;
        }
        int zmax = z + vz;
        if (zmax >= this.sizeZ) {
            zmax = this.sizeZ - 1;
        }
        int ymin = y - vy;
        if (ymin < 0) {
            ymin = 0;
        }
        int ymax = y + vy;
        if (ymax >= this.sizeY) {
            ymax = this.sizeY - 1;
        }
        int xmin = x - vx;
        if (xmin < 0) {
            xmin = 0;
        }
        int xmax = x + vx;
        if (xmax >= this.sizeX) {
            xmax = this.sizeX - 1;
        }
        int tx = xmin - bb[0];
        int ty = ymin - bb[2];
        int tz = zmin - bb[4];
        float diff;
        for (int k = zmin; k <= zmax; k++) {
            for (int j = ymin; j <= ymax; j++) {
                for (int i = xmin; i <= xmax; i++) {
                    if ((seg.contains(i - tx, j - ty, k - tz)) && (seg.getPixel(i - tx, j - ty, k - tz) > 0)) {
                        diff = this.getPixel(i, j, k) - img.getPixel(i - tx, j - ty, k - tz);

                        pix.putValue(index, diff);
                        index++;

                    }
                }
            }
        }

        pix.setSize(index);

        return pix;
    }

    /**
     * Get the neighborhood as a layer of pixels
     *
     * @param x Coordinate x of the pixel
     * @param y Coordinate y of the pixel
     * @param z Coordinate z of the pixel
     * @param r0 Minimu radius value
     * @param r1 Maximum radius value
     * @return
     */
    public ArrayUtil getNeighborhoodLayer(int x, int y, int z, float r0, float r1) {
        return this.getNeighborhoodLayer(x, y, z, r0, r1, null);
    }

    /**
     * Get the neighborhood as a layer of pixels
     *
     * @param x Coordinate x of the pixel
     * @param y Coordinate y of the pixel
     * @param z Coordinate z of the pixel
     * @param r0 Minimu radius value
     * @param r1 Maximum radius value
     * @param water
     * @return
     */
    public ArrayUtil getNeighborhoodLayer(int x, int y, int z, float r0, float r1, ImageInt water) {
        int index = 0;
        double r02 = r0 * r0;
        double r12 = r1 * r1;

        double dist;
        double ratio = getScaleZ() / getScaleXY();
        double ratio2 = ratio * ratio;
        int vx = (int) Math.ceil(r1);
        int vy = (int) Math.ceil(r1);
        int vz = (int) (Math.ceil(r1 / ratio));
        double[] pix = new double[(2 * vx + 1) * (2 * vy + 1) * (2 * vz + 1)];
        int wat = 0;
        if (water != null) {
            wat = water.getPixelInt(x, y, z);
        }

        for (int k = z - vz; k <= z + vz; k++) {
            for (int j = y - vy; j <= y + vy; j++) {
                for (int i = x - vx; i <= x + vx; i++) {
                    if (i >= 0 && j >= 0 && k >= 0 && i < sizeX && j < sizeY && k < sizeZ) {
                        if (((water != null) && (water.getPixel(i, j, k) == wat)) || (water == null)) {
                            dist = ((x - i) * (x - i)) + ((y - j) * (y - j)) + ((z - k) * (z - k) * ratio2);
                            if ((dist >= r02) && (dist < r12)) {
                                //t.putValue(index, );
                                pix[index] = getPixel(i, j, k);
                                index++;
                            }
                        }
                    }
                }
            }
        }
        // check if some values are set
        if (index > 0) {
            ArrayUtil t = new ArrayUtil(pix);
            t.setSize(index);
            return t;
        } else {
            return null;
        }
    }

    /**
     * Get the neighborhood as a layer of pixels
     *
     * @param x Coordinate x of the pixel
     * @param y Coordinate y of the pixel
     * @param z Coordinate z of the pixel
     * @param r0 Minimun radius value
     * @param r1 Maximum radius value
     * @param water
     * @return
     */
    public ArrayUtil getNeighborhoodLayerAngle(int x, int y, int z, float r0, float r1, double angRef, Vector3D ref) {
        int index = 0;
        double r02 = r0 * r0;
        double r12 = r1 * r1;

        double dist;
        double ratio = getScaleZ() / getScaleXY();
        double ratio2 = ratio * ratio;
        int vx = (int) Math.ceil(r1);
        int vy = (int) Math.ceil(r1);
        int vz = (int) (Math.ceil(r1 / ratio));
        double[] pix = new double[(2 * vx + 1) * (2 * vy + 1) * (2 * vz + 1)];

        Vector3D cen = new Vector3D(x, y, z);

        for (int k = z - vz; k <= z + vz; k++) {
            for (int j = y - vy; j <= y + vy; j++) {
                for (int i = x - vx; i <= x + vx; i++) {
                    if (i >= 0 && j >= 0 && k >= 0 && i < sizeX && j < sizeY && k < sizeZ) {
                        dist = ((x - i) * (x - i)) + ((y - j) * (y - j)) + ((z - k) * (z - k) * ratio2);
                        if ((dist >= r02) && (dist < r12)) {
                            // check angle
                            double angle = ref.angleDegrees(new Vector3D(i - x, j - y, k - z));
                            if (angle < angRef) {
                                pix[index] = getPixel(i, j, k);
                                index++;
                            }
                        }
                    }
                }
            }
        }
        // check if some values are set
        if (index > 0) {
            ArrayUtil t = new ArrayUtil(pix);
            t.setSize(index);
            return t;
        } else {
            return null;
        }
    }

    public ImageStack getImageStack() {
        return img.getImageStack();
    }

    public abstract void draw(Object3D o, float value);

    public abstract void setPixel(int coord, float value);

    public abstract void setPixel(Point3D point, float value);

    public abstract void setPixel(int x, int y, int z, float value);

    public abstract void setPixel(int xy, int z, float value);

    public abstract Object getArray1D();

    public abstract Object getArray1D(int z);

    public void setTitle(String title) {
        this.title = title;
        if (img != null) {
            img.setTitle(title);
        }
    }

    public Calibration getCalibration() {
        if (img == null) {
            return null;
        } else {
            return img.getCalibration();
        }
    }

    public void setCalibration(Calibration cal) {
        if (img != null) {
            img.setCalibration(cal);
        }
    }

    public void setScale(double scaleXY, double scaleZ, String unit) {
        if (img != null) {
            Calibration cal = img.getCalibration();
            cal.pixelDepth = scaleZ;
            cal.pixelHeight = scaleXY;
            cal.pixelWidth = scaleXY;
            cal.setUnit(unit);
        }
    }

    public void setScale(ImageHandler other) {
        if (img != null) {
            Calibration cal = other.getImagePlus().getCalibration().copy();
            img.setCalibration(cal);
        }
    }

    public void setOffset(ImageHandler other) {
        this.offsetX = other.offsetX;
        this.offsetY = other.offsetY;
        this.offsetZ = other.offsetZ;
    }

    public double getScaleXY() {
        if (img != null) {
            Calibration cal = img.getCalibration();
            return cal.pixelWidth;
        }
        return 1;
    }

    public String getUnit() {
        if (img != null) {
            Calibration cal = img.getCalibration();
            return cal.getUnit();
        }
        return "";
    }

    public double getScaleZ() {
        if (img != null) {
            Calibration cal = img.getCalibration();
            return cal.pixelDepth;
        }
        return 1;
    }

    public static ImageHandler wrap(ImagePlus imp) {
        switch (imp.getBitDepth()) {
            case 8:
                return new ImageByte(imp);
            case 16:
                return new ImageShort(imp);
            case 32:
                return new ImageFloat(imp);
        }
        return null;
    }

    public ImageHandler createSameDimensions() {
        if (this instanceof ImageByte) {
            ImageStack stack = ImageStack.create(sizeX, sizeY, sizeZ, 8);
            return new ImageByte(stack);
        } else if (this instanceof ImageShort) {
            ImageStack stack = ImageStack.create(sizeX, sizeY, sizeZ, 16);
            return new ImageShort(stack);
        } else if (this instanceof ImageFloat) {
            ImageStack stack = ImageStack.create(sizeX, sizeY, sizeZ, 32);
            return new ImageFloat(stack);
        }

        return null;
    }

    public static ImageHandler wrap(ImageStack stack) {
        switch (stack.getBitDepth()) {
            case 8:
                return new ImageByte(stack);
            case 16:
                return new ImageShort(stack);
            case 32:
                return new ImageFloat(stack);
        }
        return null;
    }

    public static ImageHandler openImage(File f) throws Exception {
        Opener op = new Opener();
        op.setSilentMode(true);
        ImagePlus i = op.openImage(f.getAbsolutePath());
        i.setTitle(f.getName());
        i.setTitle(i.getShortTitle());
        switch (i.getBitDepth()) {
            case 8:
                return new ImageByte(i);
            case 16:
                return new ImageShort(i);
            case 32:
                return new ImageFloat(i);
        }
        return null;
    }

    public static ImageHandler newBlankImageHandler(String title, ImageHandler ih) {
        switch (ih.img.getBitDepth()) {
            case 8:
                return new ImageByte(title, ih.sizeX, ih.sizeY, ih.sizeZ);
            case 16:
                return new ImageShort(title, ih.sizeX, ih.sizeY, ih.sizeZ);
            case 32:
                return new ImageFloat(title, ih.sizeX, ih.sizeY, ih.sizeZ);
        }
        return null;
    }

    /**
     * Compute the operation s1*this + s2*other
     *
     * @param image The other image
     * @param s1 The coefficient applied to this
     * @param s2 The coefficient applied to other image
     * @return The resulting float image
     */
    public ImageHandler addImage(ImageHandler image, float s1, float s2) {
        if (!this.sameDimentions(image)) {
            return null;
        }
        // ImageFloat is returned
        ImageFloat res = new ImageFloat("res", this.sizeX, this.sizeY, this.sizeZ);
        for (int i = 0; i < sizeXYZ; i++) {
            res.setPixel(i, s1 * this.getPixel(i) + s2 * image.getPixel(i));
        }

        return res;
    }

    /**
     * Compute the operation coeff*this*other;
     *
     * @param coeff The coefficient applied to this
     * @return The resulting float image
     */
    public ImageHandler multiplyImage(ImageHandler image, float coeff) {
        // ImageFloat is returned
        ImageFloat res = new ImageFloat("multiply", this.sizeX, this.sizeY, this.sizeZ);
        for (int i = 0; i < sizeXYZ; i++) {
            res.setPixel(i, coeff * this.getPixel(i) * image.getPixel(i));
        }

        return res;
    }

    /**
     * Compute the operation this/(other*coeff);
     *
     * @param coeff The coefficient applied to this
     * @return The resulting float image
     */
    public ImageHandler divideImage(ImageHandler image, float coeff) {
        // ImageFloat is returned
        ImageFloat res = new ImageFloat("divide", this.sizeX, this.sizeY, this.sizeZ);
        for (int i = 0; i < sizeXYZ; i++) {
            res.setPixel(i, this.getPixel(i) / (image.getPixel(i) * coeff));
        }

        return res;
    }

    public void divideByValue(float coeff) {
        for (int i = 0; i < sizeXYZ; i++) {
            setPixel(i, this.getPixel(i) / coeff);
        }
    }

    public void addValue(float val) {
        for (int i = 0; i < sizeXYZ; i++) {
            this.setPixel(i, this.getPixel(i) + val);
        }
    }

    public ImageHandler duplicate() {
        return ImageHandler.wrap(img.duplicate());
    }

    public abstract ImageHandler deleteSlices(int zmin, int zmax);

    public abstract void trimSlices(int zmin, int zmax);

    public abstract void erase();

    public abstract void fill(double value);

    public abstract boolean isOpened();

    public boolean isVisible() {
        return img != null && img.isVisible();
    }

    public void updateDisplay() {
        if (isVisible()) {
            img.updateAndRepaintWindow();
        }
    }

    public double[] getMinAndMaxArray(ArrayList<? extends Point3D> mask) {
        double min = Double.MAX_VALUE;
        double max = -min;
        for (Point3D p : mask) {
            double v = getPixel(p);
            if (v > max) {
                max = v;
            }
            if (v < min) {
                min = v;
            }
        }
        return new double[]{min, max};
    }

    protected abstract void getMinAndMax(ImageInt mask);

    public synchronized double getMin(ImageInt mask) {
        return getImageStats(mask).getMin();
    }

    public synchronized double getMax(ImageInt mask) {
        return getImageStats(mask).getMax();
    }

    public double getMin() {
        return getMin(new BlankMask(this));
    }

    public double getMax() {
        return getMax(new BlankMask(this));
    }

    public double getMean(ImageInt mask) {
        return getImageStats(mask).getMean();
    }

    public double getMean() {
        return getImageStats(new BlankMask(this)).getMean();
    }

    public double[] extractLine(int x0, int y0, int z0, int x1, int y1, int z1, boolean interpolate) {
        int dx = (x1 - x0);
        int dy = (y1 - y0);
        int dz = (z1 - z0);
        int dist = Math.abs(dx) + Math.abs(dy) + Math.abs(dz);

        double[] line = new double[dist];

        float xStep = dx / (float) dist;
        float yStep = dy / (float) dist;
        float zStep = dz / (float) dist;

        //System.out.println("Steps: " + xStep + ", " + yStep + ", " + zStep);
        float posX, posY, posZ;

        if (!contains(x1, y1, z1) || !contains(x0, y0, z0)) {
            for (int i = 0; i < dist; i++) {
                posX = x0 + (i * xStep);
                posY = y0 + (i * yStep);
                posZ = z0 + (i * zStep);

                if (!contains(posX, posY, posZ)) {
                    if (i == 0) {
                        line[i] = 0;
                    } else {
                        line[i] = line[i - 1];
                    }
                } else {
                    if (interpolate) {
                        line[i] = this.getPixel(posX, posY, posZ);
                    } else {
                        line[i] = this.getPixel(Math.round(posX), Math.round(posY), Math.round(posZ));
                    }
                }
            }
        } else {
            for (int i = 0; i < dist; i++) {
                posX = (x0 + (i * xStep));
                posY = (y0 + (i * yStep));
                posZ = (z0 + (i * zStep));

                if (interpolate) {
                    line[i] = this.getPixel(posX, posY, posZ);
                } else {
                    line[i] = this.getPixel(Math.round(posX), Math.round(posY), Math.round(posZ));
                }
            }
        }
        return line;
    }

    public void setMinAndMax(ImageInt mask) {
        if (!isOpened()) {
            return;
        }
        getMinAndMax(mask);
        ImageStats s = getImageStats(mask);
        img.getProcessor().setMinAndMax(s.getMin(), s.getMax());
    }

    protected void getMoments(ImageInt mask) {
        if (mask == null) {
            mask = new BlankMask(this);
        }
        ImageStats s = getImageStats(mask);
        if (s.momentsSet()) {
            return;
        }
        double mean2 = 0;
        double mean = 0;
        double count = 0;
        for (int z = 0; z < sizeZ; z++) {
            for (int xy = 0; xy < sizeXY; xy++) {
                if (mask.getPixel(xy, z) != 0) {
                    double value = getPixel(xy, z);
                    mean += value;
                    mean2 += value * value;
                    count++;
                }
            }
        }
        if (count != 0) {
            mean /= count;
            s.setMoments(mean, mean2, Math.sqrt(mean2 / count - mean * mean));
        } else {
            s.setMoments(0, 0, 0);
        }
    }

    public void setMinAndMax(float min, float max) {
        if (img != null) {
            img.getProcessor().setMinAndMax(min, max);
        }
    }

    public int[] getHistogram(ImageInt mask) { //256 bins
        if (mask == null) {
            mask = new BlankMask(this);
        }
        ImageStats s = getImageStats(mask);
        return s.getHisto256();
    }

    public int[] getHistogram() { //256 bins
        return getHistogram(null);
    }

    public int[] getHistogram(ImageInt mask, int nBins, double min, double max) {
        return getHisto(mask, nBins, min, max);
    }

    public int[] getHistogram(ArrayList<? extends Point3D> mask, int nBins, double min, double max) {
        int[] histo = new int[nBins];
        double coeff = nBins / (max - min);
        int idx;
        for (Point3D p : mask) {
            idx = (int) ((getPixel(p) - min) * coeff);
            if (idx >= 255) {
                histo[255]++;
            } else {
                histo[idx]++;
            }
        }
        return histo;
    }

    protected abstract int[] getHisto(ImageInt mask);

    protected abstract int[] getHisto(ImageInt mask, int nBins, double min, double max);

    public double getPercentile(double percent, ImageInt mask) {
        ImageStats s = getImageStats(mask);
        int[] histo = s.getHisto256();
        int count = 0;
        for (int i : histo) {
            count += i;
        }
        double limit = count * percent;
        if (limit >= count) {
            return s.getMin();
        }
        count = histo[255];
        int idx = 255;
        while (count < limit && idx > 0) {
            idx--;
            count += histo[idx];
        }
        double idxInc = (histo[idx] != 0) ? (count - limit) / (histo[idx]) : 0; //lin approx
        //ij.IJ.log("percentile: bin:"+idx+ " inc:"+ idxInc+ " min:"+min+ " max:"+max);
        return (float) (idx + idxInc) * s.getHisto256BinSize() + s.getMin();
    }

    public abstract Image3D getImage3D();

    public void show() {
        this.setMinAndMax(null);
        this.img.show();
    }

    public void show(String title) {
        this.setMinAndMax(null);
        this.img.setTitle(title);
        this.img.show();
    }

    public void showDuplicate(String title) {
        this.setMinAndMax(null);
        ImagePlus ip = this.img.duplicate();
        if (title != null) {
            ip.setTitle(title);
        } else {
            ip.setTitle(this.title);
        }
        ip.setCalibration(img.getCalibration());
        ip.show();
    }

    public void closeImagePlus() {
        if (this.img != null) {
            try {
                //this.img.hide();
                this.img.close();
                //this.img.flush();
            } catch (Exception e) {
                exceptionPrinter.print(e, "", false);
            }
        }
        this.img = null;
    }

    public void flush() {
        if (this.img != null) {
            try {
                this.img.hide();
                this.img.flush();
            } catch (Exception e) {
                exceptionPrinter.print(e, "", false);
            }
        }
        this.img = null;
        this.stats = new HashMap<ImageHandler, ImageStats>();
        this.flushPixels();
    }

    public synchronized ImageStats getImageStats(ImageInt mask) {
        if (mask == null) {
            mask = new BlankMask(this);
        }
        ImageStats res = this.stats.get(mask);
        if (res == null) {
            res = new ImageStats(this, mask);
            stats.put(mask, res);
        }
        return res;
    }

    public synchronized void resetStats(ImageInt mask) {
        if (mask == null) {
            mask = new BlankMask(this);
        }
        stats.remove(mask);
    }

    public void hide() {
        if (img != null) {
            img.hide();
        }
    }

    protected abstract void flushPixels();

    public void save(String directory) {
        FileSaver fs = new FileSaver(img);
        setMinAndMax(null);
        fs.saveAsTiffStack(directory + File.separator + img.getTitle());
    }

    public void saveThumbNail(int sizeX, int sizeY, String directory) {
        ZProjector proj = new ZProjector(img);
        proj.setMethod(ZProjector.MAX_METHOD);
        proj.doProjection();
        ImagePlus im = proj.getProjection();
        ImageProcessor ip = im.getProcessor().resize(sizeX, sizeY, true);
        ip.convertToByte(true);
        ip.setMinAndMax(0, 255);
        im = new ImagePlus(img.getShortTitle() + "_tmb", ip);
        FileSaver fs = new FileSaver(im);
        FileSaver.setJpegQuality(75);
        fs.saveAsJpeg(directory + File.separator + im.getTitle() + ".jpg");
    }

    public byte[] getThumbNail(int sizeX, int sizeY) {
        return getThumbNail(sizeX, sizeY, null);
    }

    public byte[] getThumbNail(int sizeX, int sizeY, ImageInt mask) {
        //projection
        ImagePlus im;
        this.setMinAndMax(mask);
        if (sizeZ > 1) {
            ZProjector proj = new ZProjector(img);
            proj.setMethod(ZProjector.MAX_METHOD);
            proj.doProjection();
            im = proj.getProjection();
        } else {
            im = img;
        }
        ImageProcessor ip = im.getProcessor().resize(sizeX, sizeY, true);
        ip = ip.convertToByte(true);
        ip.setMinAndMax(ip.getMin(), ip.getMax());
        im = new ImagePlus(img.getShortTitle() + "_tmb", ip);

        //jpeg encoding
        ByteArrayOutputStream out = new ByteArrayOutputStream();
//        JPEGImageEncoder encoder = JPEGCodec.createJPEGEncoder(out);
//        JPEGEncodeParam param = encoder.getDefaultJPEGEncodeParam(im.getBufferedImage());
//        param.setQuality(0.5f, false);

        // test png
        try {
            ImageIO.write(im.getBufferedImage(), "png", out);
            byte[] res = out.toByteArray();
            out.close();
            return res;
        } catch (IOException ex) {
            Logger.getLogger(ImageHandler.class.getName()).log(Level.SEVERE, null, ex);
        }

//        try {
//            encoder.encode(im.getBufferedImage());
//            byte[] res = out.toByteArray();
//            out.close();
//            return res;
//        } catch (Exception e) {
//            exceptionPrinter.print(e, "", false);
//        }
        return null;
    }

    public byte[] getBinaryData() throws Exception, OutOfMemoryError {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        FileInfo fi = img.getFileInfo();
        TiffEncoder te = new TiffEncoder(fi);

        try {
            //ij.IJ.log("get binary data: memory:"+ (Runtime.getRuntime().freeMemory()/(1024*1024)+ " size:"+this.getSizeInMb());
            te.write(out);
            byte[] res = out.toByteArray();
            out.close();
            return res;
        } catch (Exception e) {
            exceptionPrinter.print(e, "", true);
        }
        return null;
    }

    public boolean touchBorders(int x, int y, int z) {
        return (x == 0 || y == 0 || z == 0 || x == (sizeX - 1) || y == (sizeY - 1) || (z == sizeZ - 1));
    }

    public abstract ImageByte threshold(float thld, boolean keepUnderThld, boolean strict);

    public ImageByte thresholdAboveInclusive(float thld) {
        return this.threshold(thld, false, false);
    }

    public ImageByte thresholdAboveExclusive(float thld) {
        return this.threshold(thld, false, true);
    }

    public abstract void thresholdCut(float thld, boolean keepUnderThld, boolean strict);
    //public abstract void resize(int X, int Y, int Z, boolean increaseSize);

    /**
     * Extract a 3D image from a specified location as the center of crop
     *
     * @param x0 the x coordinate of the center of the extraction
     * @param y0 the y coordinate of the center of the extraction
     * @param z0 the z coordinate of the center of the extraction
     * @param rx radius X of new image
     * @param ry radius Y of new image
     * @param rz radius Z of new image
     * @param sphere extract inside a sphere
     * @return the cropped image
     */
    public abstract ImageHandler cropRadius(int x0, int y0, int z0, int rx, int ry, int rz, boolean mean, boolean sphere);

    public abstract ImageHandler crop3D(String title, int x_min, int x_max, int y_min, int y_max, int z_min, int z_max);

    public abstract ImageHandler[] crop3D(TreeMap<Integer, int[]> bounds);

    public abstract ImageHandler crop3DMask(String title, ImageInt mask, int label, int x_min_, int x_max_, int y_min_, int y_max_, int z_min_, int z_max_);

    public static ImageShort merge3DBinary(ImageInt[] images, int sizeX, int sizeY, int sizeZ) { // 1 label per image
        ImageShort out = new ImageShort("merge", sizeX, sizeY, sizeZ);
        if (images == null || images.length == 0 || images[0] == null) {
            return out;
        }
        for (int idx = 0; idx < images.length; idx++) {
            short label = (short) (idx + 1);
            for (int z = 0; z < images[idx].sizeZ; z++) {
                for (int y = 0; y < images[idx].sizeY; y++) {
                    for (int x = 0; x < images[idx].sizeX; x++) {
                        if (images[idx].getPixel(x, y, z) != 0) {
                            int xx = x + images[idx].offsetX;
                            int yy = y + images[idx].offsetY;
                            int zz = z + images[idx].offsetZ;
                            if (zz >= 0 && zz < sizeZ && xx >= 0 && xx < sizeX && yy >= 0 && yy < sizeY) {
                                out.pixels[zz][xx + yy * sizeX] = label;
                            }
                        }
                    }
                }
            }
        }
        return out;
    }

    public static ImageShort merge3D(ImageInt[] images, int sizeX, int sizeY, int sizeZ) {
        // multiple labels per image, but no gap between numbers
        ImageShort out = new ImageShort("merge", sizeX, sizeY, sizeZ);
        if (images == null || images.length == 0 || images[0] == null) {
            return out;
        }
        int offset = 1;
        for (int idx = 0; idx < images.length; idx++) {
            int min = (short) images[idx].getMinAboveValue(0);
            int max = (short) images[idx].getMax();
            for (int z = 0; z < images[idx].sizeZ; z++) {
                for (int y = 0; y < images[idx].sizeY; y++) {
                    for (int x = 0; x < images[idx].sizeX; x++) {
                        if (images[idx].getPixel(x, y, z) != 0) {
                            int val = images[idx].getPixelInt(x, y, z);
                            int xx = x + images[idx].offsetX;
                            int yy = y + images[idx].offsetY;
                            int zz = z + images[idx].offsetZ;
                            if (zz >= 0 && zz < sizeZ && xx >= 0 && xx < sizeX && yy >= 0 && yy < sizeY) {
                                out.pixels[zz][xx + yy * sizeX] = (short) (val - min + offset);
                            }
                        }
                    }
                }
            }
            offset += max - min + 1;
        }
        return out;
    }

    /**
     * Insert a 3D image to a specified location
     *
     * @param vol the 3D image to be inserted
     * @param x0 the x coordinate of the insertion
     * @param y0 the y coordinate of the insertion
     * @param z0 the z coordinate of the insertion
     * @param average average with original image or not
     */
    public void insert(ImageHandler vol, int x0, int y0, int z0, boolean average) {
        int xx0 = Math.max(x0, 0);
        int yy0 = Math.max(y0, 0);
        int zz0 = Math.max(z0, 0);
        int x1 = Math.min(x0 + vol.sizeX, sizeX);
        int y1 = Math.min(y0 + vol.sizeY, sizeY);
        int z1 = Math.min(z0 + vol.sizeZ, sizeZ);
        float pix;
        float pixo;
        for (int z = zz0; z < z1; z++) {
            for (int x = xx0; x < x1; x++) {
                for (int y = yy0; y < y1; y++) {
                    pix = vol.getPixel(x - x0, y - y0, z - z0);
                    if (average) {
                        pixo = getPixel(x - x0, y - y0, z - z0);
                        setPixel(x, y, z, 0.5f * (pixo + pix));
                    } else {
                        setPixel(x, y, z, pix);
                    }
                }
            }
        }
    }

    public abstract ImageHandler resize(int dX, int dY, int dZ);

    public abstract ImageHandler resample(int newX, int newY, int newZ, int method);

    public abstract ImageHandler resample(int newZ, int method);

//    public abstract ImageHandler grayscaleFilter(int sizeX, int sizeY, int filter, ImageHandler res, boolean multithread);
//
//    public abstract ImageHandler grayscaleOpen(int radXY, int radZ, ImageHandler res, ImageHandler temp, boolean multithread);
//
//    public abstract ImageHandler grayscaleClose(int radXY, int radZ, ImageHandler res, ImageHandler temp, boolean multithread);
    protected abstract ImageFloat normalize_(ImageInt mask, double saturation);

    public abstract ImageFloat normalize(double min, double max);

    public ImageFloat normalize(ImageInt mask, double saturation) {
        return normalize_(mask, saturation);
    }

    public abstract void invert(ImageInt mask); //maximum within mask // FIXME mask is not used

    public void invert() {
        invert(null);
    }

    //from FeatureJ
    public ImageFloat gaussianSmooth(double scaleXY, double scaleZ, int nbCPUs) {
        Image res;
        double old_scaleXY = this.getScaleXY();
        double old_scaleZ = this.getScaleZ();
        this.setScale(1, scaleXY / scaleZ, this.getUnit());
        if (nbCPUs > 0) {
            FJDifferentiator3D differentiator = new FJDifferentiator3D();
            res = differentiator.run(Image.wrap(img), scaleXY, 0, 0, 0, nbCPUs);
        } else {
            Differentiator differentiator = new Differentiator();
            res = differentiator.run(Image.wrap(img), scaleXY, 0, 0, 0);
        }
        this.setScale(old_scaleXY, old_scaleZ, this.getUnit());
        ImageFloat ihres = (ImageFloat) ImageHandler.wrap(res.imageplus());
        ihres.setScale(old_scaleXY, old_scaleZ, this.getUnit());
        ihres.setOffset(this);
        return ihres;
    }

    public ImageFloat substractImage(ImageHandler other) {
        ImageFloat res;
        if (!this.sameDimentions(other)) {
            return null;
        }
        // both images are byte then return byte
        res = ImageFloat.newBlankImageFloat(title + "-" + other.title, this);
        for (int z = 0; z < sizeZ; z++) {
            for (int xy = 0; xy < sizeXY; xy++) {
                res.pixels[z][xy] = this.getPixel(xy, z) - other.getPixel(xy, z);
            }
        }

        return res;
    }

    public ImageFloat getGradient(double scale, int nbCPUs) {
        return getGradient(scale, true, nbCPUs)[3];
    }

    public ImageFloat getEdges(double scale, float min_thld, float max_thld) { //retourne gradient en 0 et edges en 1
        ImagePlus im_edge = FJEdges(img, scale, true, min_thld, max_thld);
        ImageFloat res = new ImageFloat(im_edge);
        res.setTitle(title + ":gradient");
        res.offsetX = offsetX;
        res.offsetY = offsetY;
        res.offsetZ = offsetZ;
        return res;
    }

    public ImageFloat[] getHessian(double scale, int nbCPUs) {
        ImageFloat[] res = new ImageFloat[3];
        ImagePlus[] hess = FJHessian(img, scale, nbCPUs);
        for (int i = 0; i < 3; i++) {
            res[i] = new ImageFloat(hess[i]);
            res[i].setTitle(title + ":hessian" + (i + 1));
            res[i].offsetX = offsetX;
            res[i].offsetY = offsetY;
            res[i].offsetZ = offsetZ;
        }
        return res;
    }

    public ImageFloat getHessianDeterminant(double scale, int nbCPUs, boolean allNegative) {
        ImageFloat res, res0, res1, res2;
        ImagePlus[] hess = FJHessian(img, scale, nbCPUs);

        res = new ImageFloat(hess[0]);
        res = (ImageFloat) res.multiplyImage(new ImageFloat(hess[1]), 1);
        res = (ImageFloat) res.multiplyImage(new ImageFloat(hess[2]), 1);
        if (allNegative) {
            // test if all values are negative
            res0 = new ImageFloat(hess[0]);
            res1 = new ImageFloat(hess[1]);
            res2 = new ImageFloat(hess[2]);
            for (int i = 0; i < res0.sizeXYZ; i++) {
                if ((res0.getPixel(i) >= 0) || (res1.getPixel(i) >= 0) || (res2.getPixel(i) >= 0)) {
                    res.setPixel(i, 0);
                }
            }
        }
        return res;
    }

    public ImageFloat[] getInertia(double smoothScale, double integrationScale, int nbCPUs) {
        ImageFloat[] res = new ImageFloat[3];
        ImagePlus[] hess = FJInertia(img, smoothScale, integrationScale, nbCPUs);
        for (int i = 0; i < 3; i++) {
            res[i] = new ImageFloat(hess[i]);
            res[i].setTitle(title + ":intertia" + (i + 1));
            res[i].offsetX = offsetX;
            res[i].offsetY = offsetY;
            res[i].offsetZ = offsetZ;
        }
        return res;
    }

    public ImageFloat[] getGradient(double scale, boolean computeMagnitude, int nbCPUs) {
        final ImageFloat[] res = new ImageFloat[computeMagnitude ? 4 : 3];
        Image imw = Image.wrap(img);
        final Image Ix, Iy, Iz;
        scale *= (double) getScaleXY(); // FIXME scaleZ?
        if (nbCPUs > 1) {
            final FJDifferentiator3D differentiator = new FJDifferentiator3D();
            Ix = differentiator.run(imw.duplicate(), scale, 1, 0, 0, nbCPUs);
            Iy = differentiator.run(imw.duplicate(), scale, 0, 1, 0, nbCPUs);
            Iz = differentiator.run(imw.duplicate(), scale, 0, 0, 1, nbCPUs);
        } else {
            final Differentiator differentiator = new Differentiator();
            Ix = differentiator.run(imw.duplicate(), scale, 1, 0, 0);
            Iy = differentiator.run(imw.duplicate(), scale, 0, 1, 0);
            Iz = differentiator.run(imw.duplicate(), scale, 0, 0, 1);
        }
        res[0] = new ImageFloat(Ix.imageplus());
        res[1] = new ImageFloat(Iy.imageplus());
        res[2] = new ImageFloat(Iz.imageplus());
        if (computeMagnitude) {
            res[3] = new ImageFloat(title + "_gradientMagnitude", sizeX, sizeY, sizeZ);
            final ThreadRunner tr = new ThreadRunner(0, sizeZ, nbCPUs);
            for (int i = 0; i < tr.threads.length; i++) {
                tr.threads[i] = new Thread(
                        new Runnable() {
                            public void run() {
                                for (int z = tr.ai.getAndIncrement(); z < tr.end; z = tr.ai.getAndIncrement()) {
                                    for (int xy = 0; xy < sizeXY; xy++) {
                                        res[3].pixels[z][xy] = (float) Math.sqrt(res[0].pixels[z][xy] * res[0].pixels[z][xy] + res[1].pixels[z][xy] * res[1].pixels[z][xy] + res[2].pixels[z][xy] * res[2].pixels[z][xy]);
                                    }
                                }
                            }
                        });
            }
            tr.startAndJoin();
        }
        return res;
    }

    private ImagePlus FJEdges(ImagePlus imp, double scaleval, boolean edge, double lowval, double highval) {
        Image imw = Image.wrap(imp);
        Calibration cal = imp.getCalibration();
        if (cal.scaled()) {
            scaleval *= cal.pixelWidth; //scaleval*=Math.pow(cal.pixelWidth*cal.pixelHeight*cal.pixelDepth, 0.3332);
        }
        Image newimg = new FloatImage(imw);
        Edges edges = new Edges();
        newimg = edges.run(newimg, scaleval, edge);
        if (edge && (lowval > 0) && (highval > 0)) {
            Thresholder thres = new Thresholder();
            thres.hysteresis(newimg, lowval, highval);
        }
        ImagePlus res = newimg.imageplus();
        return res;
    }

    private ImagePlus[] FJHessian(ImagePlus imp, double scaleval, int nbCPUs) {
        Image image = Image.wrap(imp);
        Calibration cal = imp.getCalibration();
        if (cal.scaled()) {
            scaleval *= cal.pixelWidth; //scaleval*=Math.pow(cal.pixelWidth*cal.pixelHeight*cal.pixelDepth, 0.3332);
        }
        Vector vector;
        if (nbCPUs > 1) {
            FJHessian3D hessian = new FJHessian3D();
            vector = hessian.run(new FloatImage(image), scaleval, nbCPUs);
        } else {
            Hessian hessian = new Hessian();
            vector = hessian.run(new FloatImage(image), scaleval, false);
        }
        ImagePlus[] res = new ImagePlus[3];
        res[0] = ((Image) vector.get(0)).imageplus();
        res[1] = ((Image) vector.get(1)).imageplus();
        res[2] = ((Image) vector.get(2)).imageplus();
        return res;
    }

    private ImagePlus[] FJInertia(ImagePlus imp, double smoothScale, double integrationScale, int nbCPUs) {
        Image image = Image.wrap(imp);
        Calibration cal = imp.getCalibration();
        double sscale = smoothScale;
        double iscale = integrationScale;
        if (cal.scaled()) {
            sscale *= cal.pixelWidth;
            iscale *= cal.pixelWidth;
        }
        Vector vector;
        if (nbCPUs > 1) {
            vector = (new FJStructure3D()).run(image, sscale, iscale, nbCPUs);
        } else {
            vector = (new Structure()).run(image, sscale, iscale);
        }

        ImagePlus[] res = new ImagePlus[3];
        res[0] = ((Image) vector.get(0)).imageplus();
        res[1] = ((Image) vector.get(1)).imageplus();
        res[2] = ((Image) vector.get(2)).imageplus();
        return res;
    }

    public static ImagePlus getHyperStack(String title, ImageHandler[] images) { //assumes images have same size
        homogenizeBitDepth(images);
        ImageStack stack = new ImageStack(images[0].sizeX, images[0].sizeY, images[0].sizeZ * images.length);
        int count = 1;
        for (ImageHandler ih : images) {
            if (ih != null) {
                ih.setMinAndMax(null);
            }
        }
        for (int slice = 1; slice <= images[0].sizeZ; slice++) {
            for (int channel = 0; channel < images.length; channel++) {
                //System.out.println("slice:"+slice+" channel:"+channel+ " bit depth:"+images[channel].img.getBitDepth());

                stack.setPixels(images[channel].img.getStack().getPixels(slice), count);
                count++;
            }
        }

        ImagePlus res = new ImagePlus();
        res.setStack(stack, images.length, images[0].sizeZ, 1);
        res.setTitle(title);
        res.setOpenAsHyperStack(true);
        return res;
    }

    public static void homogenizeBitDepth(ImageHandler[] images) {
        boolean shortIm = false;
        boolean floatIm = false;
        for (ImageHandler im : images) {
            if (im instanceof ImageShort) {
                shortIm = true;
            } else if (im instanceof ImageFloat) {
                floatIm = true;
            }
        }
        if (floatIm) {
            for (int i = 0; i < images.length; i++) {
                if (images[i] instanceof ImageByte) {
                    images[i] = ((ImageByte) images[i]).convertToFloat(false);
                }
                if (images[i] instanceof ImageShort) {
                    images[i] = ((ImageShort) images[i]).convertToFloat(false);
                }
            }
        } else if (shortIm) {
            for (int i = 0; i < images.length; i++) {
                if (images[i] instanceof ImageByte) {
                    images[i] = ((ImageByte) images[i]).convertToShort(false);
                }
            }
        }
    }

    public static void convertToByte(ImageHandler[] images) {
        for (int i = 0; i < images.length; i++) {
            if ((images[i] instanceof ImageShort)) {
                images[i] = ((ImageShort) images[i]).convertToByte(true);
            }
            if ((images[i] instanceof ImageFloat)) {
                images[i] = ((ImageFloat) images[i]).convertToByte(true);
            }
        }
    }

    public static void convertToByte(ImageHandler image) {
        if ((image instanceof ImageShort)) {
            image = ((ImageShort) image).convertToByte(true);
        }
        if ((image instanceof ImageFloat)) {
            image = ((ImageFloat) image).convertToByte(true);
        }
    }

    public void hysteresis(double lowval, double highval, boolean lowConnectivity) {
        final Image imp = Image.wrap(img);
        final Thresholder thres = new Thresholder();
        if (lowConnectivity) {
            thres.hysteresisLowConnectivity(imp, lowval, highval);
        } else {
            thres.hysteresis(imp, lowval, highval);
        }
    }

    public abstract void intersectMask(ImageInt mask);

    public ImageFloat getDistanceMap(float thld, float scaleXY, float scaleZ, boolean invert, int nbCPUs) {
        return EDT.run(this, thld, scaleXY, scaleZ, invert, nbCPUs);
    }

    public void set332RGBLut() {
        IndexColorModel cm = get332RGB();
        img.getChannelProcessor().setColorModel(cm);
        img.getStack().setColorModel(cm);
        img.updateAndRepaintWindow();
    }

    public void setGraysLut() {
        IndexColorModel cm = getGrays();
        img.getChannelProcessor().setColorModel(cm);
        img.getStack().setColorModel(cm);
        img.updateAndRepaintWindow();
    }

    private IndexColorModel get332RGB() {
        FileInfo fi332RGB = new FileInfo();
        fi332RGB.reds = new byte[256];
        fi332RGB.greens = new byte[256];
        fi332RGB.blues = new byte[256];
        fi332RGB.lutSize = 256;
        for (int i = 0; i < 256; i++) {
            fi332RGB.reds[i] = (byte) (i & 0xe0);
            fi332RGB.greens[i] = (byte) ((i << 3) & 0xe0);
            fi332RGB.blues[i] = (byte) ((i << 6) & 0xc0);
        }
        return new IndexColorModel(8, 256, fi332RGB.reds, fi332RGB.greens, fi332RGB.blues);
    }

    private IndexColorModel getGrays() {
        FileInfo fiGrays = new FileInfo();
        fiGrays.reds = new byte[256];
        fiGrays.greens = new byte[256];
        fiGrays.blues = new byte[256];
        fiGrays.lutSize = 256;
        for (int i = 0; i < 256; i++) {
            fiGrays.reds[i] = (byte) i;
            fiGrays.greens[i] = (byte) i;
            fiGrays.blues[i] = (byte) i;
        }
        return new IndexColorModel(8, 256, fiGrays.reds, fiGrays.greens, fiGrays.blues);
    }

    /**
     * Gets the minAboveValue attribute of the FishImage3D object
     *
     * @param value Description of the Parameter
     * @return The minAboveValue value
     */
    public float getMinAboveValue(float value) {
        float mini = Float.MAX_VALUE;
        float pix;
        for (int p = 0; p < this.sizeXYZ; p++) {
            pix = this.getPixel(p);
            if ((pix > value) && (pix < mini)) {
                mini = pix;
            }
        }
        return mini;
    }

    /**
     * Radial distribution of pixels mean values in layers
     *
     * @param x0 Coordinate x of the pixel
     * @param y0 Coordinate y of the pixel
     * @param z0 Coordinate z of the pixel
     * @param maxR maximu radius
     * @param water
     * @return arry with mean radial values
     */
    public double[] radialDistribution(int x0, int y0, int z0, int maxR, ImageInt water) {
        //int maxR = 10;
        double[] radPlot = new double[2 * maxR + 1];
        ArrayUtil raddist;

        int c = 0;
        int r;

        // compute radial means
        for (int i = -maxR; i <= 0; i++) {
            r = -i;
            raddist = getNeighborhoodLayer(x0, y0, z0, r, r + 1, water);
            if (raddist != null) {
                radPlot[c] = raddist.getMean();
            } else {
                radPlot[c] = Double.NaN;
            }
            c++;
        }
        for (int i = 1; i <= maxR; i++) {
            r = i;
            raddist = getNeighborhoodLayer(x0, y0, z0, r, r + 1, water);
            if (raddist != null) {
                radPlot[c] = raddist.getMean();
            } else {
                radPlot[c] = Double.NaN;
            }
            c++;
        }

        return radPlot;
    }

    /**
     * Radial distribution of pixels mean values in layers
     *
     * @param x0 Coordinate x of the pixel
     * @param y0 Coordinate y of the pixel
     * @param z0 Coordinate z of the pixel
     * @param maxR maximu radius
     * @return arry with mean radial values
     */
    public double[] radialDistribution(int x0, int y0, int z0, int maxR) {
        return this.radialDistribution(x0, y0, z0, maxR, null);
    }

    public boolean hasOneValue(float f) {
        for (int i = 0; i < sizeXYZ; i++) {
            if (getPixel(i) == f) {
                return true;
            }
        }
        return false;
    }
}
