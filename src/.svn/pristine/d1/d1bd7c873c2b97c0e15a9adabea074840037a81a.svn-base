/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mcib3d.geom;

import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.gui.Roi;
import ij.measure.Calibration;
import ij.plugin.filter.ThresholdToSelection;
import ij.process.ByteProcessor;
import ij.process.ImageProcessor;
import java.awt.Color;
import java.util.ArrayList;
import java.util.Iterator;
import mcib3d.image3d.ImageHandler;
import mcib3d.image3d.ImageInt;
import mcib3d.utils.ArrayUtil;
import mcib3d.utils.KDTreeC;

/**
 *
 **
 * /**
 * Copyright (C) 2008- 2011 Thomas Boudier
 *
 *
 *
 * This file is part of mcib3d
 *
 * mcib3d is free software; you can redistribute it and/or modify it under the
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
 * @author thomas
 */
public class Object3DLabel extends Object3D {

    //ImageInt labelImage;
    ArrayList<Voxel3D> voxels = null;

    /**
     * constructor
     *
     * @param ima Segmented iamge
     * @param val Pixel value of the object
     */
    public Object3DLabel(ImageInt ima, int val) {
        value = val;
        labelImage = ima;

        init();

        resXY = 1.0;
        resZ = 1.0;
        units = "pix";
    }

    /**
     * Constructor for the Object3D object
     *
     * @param plus Segmented image
     * @param val Pixel value of the object
     */
    public Object3DLabel(ImagePlus plus, int val) {
        value = val;
        //IntImage3D ima = new IntImage3D(plus.getStack());
        ImageInt ima = null;
        if (plus.getBitDepth() > 16) {
            IJ.log("Image type not supported");
        } else {
            ima = (ImageInt) ImageHandler.wrap(plus);
        }
        labelImage = ima;

        init();

        // Calibration
        Calibration cal = plus.getCalibration();
        if (cal != null) {
            if (cal.scaled()) {
                resXY = cal.getX(1.0);
                resZ = cal.getZ(1.0);
                units = cal.getUnits();
            } else {
                resXY = 1.0;
                resZ = 1.0;
                units = "pix";
            }
        }
        //System.out.println("res " + resXY + " " + resZ + " " + units);
    }

    /**
     * Gets the segImage attribute of the Object3D object
     *
     * @return The segImage value
     */
    //public ImageInt getSegImage() {
    //     return labelImage;
    //}
    /**
     * test if the point is inside the segmented image
     *
     * @param x x coordinate of the point
     * @param y y coordinate of the point
     * @param z z coordinate of the point
     * @return true or false
     */
    private boolean insideImage(double x, double y, double z) {
        if ((x >= 0) && (x < labelImage.sizeX) && (y >= 0) && (y < labelImage.sizeY) && (z >= 0) && (z < labelImage.sizeZ)) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * @param other object
     * @return percentage of colocalisation of this object into the other object
     */
//    @Override
//    public double pcColoc(Object3D other) {
//        int count = 0;
//        int xmin0;
//        int ymin0;
//        int zmin0;
//        int xmax0;
//        int ymax0;
//        int zmax0;
//
//        int val = other.getValue();
//        ImageInt otherseg = ((Object3DLabel) other).getLabelImage();
//
//        xmin0 = getXmin();
//        ymin0 = getYmin();
//        zmin0 = getZmin();
//        xmax0 = getXmax();
//        ymax0 = getYmax();
//        zmax0 = getZmax();
//
//        if (other != null) {
//            xmin0 = Math.max(xmin0, other.getXmin());
//            ymin0 = Math.max(ymin0, other.getYmin());
//            zmin0 = Math.max(zmin0, other.getZmin());
//            xmax0 = Math.min(xmax0, other.getXmax());
//            ymax0 = Math.min(ymax0, other.getYmax());
//            zmax0 = Math.min(zmax0, other.getZmax());
//        }
//
//        for (int k = zmin0; k <= zmax0; k++) {
//            for (int j = ymin0; j <= ymax0; j++) {
//                for (int i = xmin0; i <= xmax0; i++) {
//                    if ((labelImage.getPixel(i, j, k) == value) && (otherseg.getPixel(i, j, k) == val)) {
//                        count++;
//                    }
//                }
//            }
//        }
//
//        return 100.0 * (double) count / (double) volume;
//    }
    /**
     *
     * @return array of listed values (with coordinates)
     */
    public ArrayList listVoxels(ImageHandler ima, double threshold) {
        ArrayList<Voxel3D> list = new ArrayList();
        Voxel3D pixel;
        int xmin0;
        int ymin0;
        int zmin0;
        int xmax0;
        int ymax0;
        int zmax0;

        xmin0 = getXmin();
        ymin0 = getYmin();
        zmin0 = getZmin();
        xmax0 = getXmax();
        ymax0 = getYmax();
        zmax0 = getZmax();

        float val;

        for (int k = zmin0; k <= zmax0; k++) {
            for (int j = ymin0; j <= ymax0; j++) {
                for (int i = xmin0; i <= xmax0; i++) {
                    if (labelImage.getPixel(i, j, k) == value) {
                        if (ima != null) {
                            val = ima.getPixel(i, j, k);
                        } else {
                            val = value;
                        }
                        if (val > threshold) {
                            pixel = new Voxel3D(i, j, k, val);
                            list.add(pixel);
                        }
                    }
                }
            }
        }

        return list;
    }

    /**
     * Compute the bounding box of the object and the array list of voxels
     */
    protected void computeBounding() {
        xmin = labelImage.sizeX;
        xmax = 0;
        ymin = labelImage.sizeY;
        ymax = 0;
        zmin = labelImage.sizeZ;
        zmax = 0;
        for (int k = 0; k < labelImage.sizeZ; k++) {
            for (int j = 0; j < labelImage.sizeY; j++) {
                for (int i = 0; i < labelImage.sizeX; i++) {
                    if (labelImage.getPixel(i, j, k) == value) {
                        // add to vxel list
                        //voxels.add(new Voxel3D(i, j, k, value));
                        if (i < xmin) {
                            xmin = i;
                        }
                        if (i > xmax) {
                            xmax = i;
                        }
                        if (j < ymin) {
                            ymin = j;
                        }
                        if (j > ymax) {
                            ymax = j;
                        }
                        if (k < zmin) {
                            zmin = k;
                        }
                        if (k > zmax) {
                            zmax = k;
                        }
                    }
                }
            }
        }
    }

    public void computeContours() {
        boolean cont;
        contours = new ArrayList();
        kdtreeContours = new KDTreeC(3);
        kdtreeContours.setScale3(this.resXY, this.resXY, this.resZ);
        int pix0, pix1, pix2, pix3, pix4, pix5, pix6;
        int sx = labelImage.sizeX, sy = labelImage.sizeY, sz = labelImage.sizeZ;
        double XYZ = resXY * resZ;
        double XX = resXY * resXY;
        areaNbVoxels = 0;
        areaContactUnit = 0;
        areaContactVoxels=0;

        for (int k = zmin; k <= zmax; k++) {
            for (int j = ymin; j <= ymax; j++) {
                for (int i = xmin; i <= xmax; i++) {
                    cont = false;
                    pix1 = pix2 = pix3 = pix4 = pix5 = pix6 = 0;
                    pix0 = labelImage.getPixelInt(i, j, k);
                    if (pix0 == value) {
                        if (i + 1 < sx) {
                            pix1 = labelImage.getPixelInt(i + 1, j, k);
                        }
                        if (i > 0) {
                            pix2 = labelImage.getPixelInt(i - 1, j, k);
                        }
                        if (j + 1 < sy) {
                            pix3 = labelImage.getPixelInt(i, j + 1, k);
                        }
                        if (j > 0) {
                            pix4 = labelImage.getPixelInt(i, j - 1, k);
                        }
                        if (k + 1 < sz) {
                            pix5 = labelImage.getPixelInt(i, j, k + 1);
                        }
                        if (k > 0) {
                            pix6 = labelImage.getPixelInt(i, j, k - 1);
                        }
                        if (pix1 != value) {
                            cont = true;
                            areaContactUnit += XYZ;
                            areaContactVoxels++;
                        }
                        if (pix2 != value) {
                            cont = true;
                            areaContactUnit += XYZ;
                            areaContactVoxels++;
                        }
                        if (pix3 != value) {
                            cont = true;
                            areaContactUnit += XYZ;
                            areaContactVoxels++;
                        }
                        if (pix4 != value) {
                            cont = true;
                            areaContactUnit += XYZ;
                            areaContactVoxels++;
                        }
                        if (pix5 != value) {
                            cont = true;
                            areaContactUnit += XX;
                            areaContactVoxels++;
                        }
                        if (pix6 != value) {
                            cont = true;
                            areaContactUnit += XX;
                            areaContactVoxels++;
                        }
                        if (cont) {
                            areaNbVoxels++;
                            Voxel3D vox = new Voxel3D(i, j, k, value);
                            contours.add(vox);
                            kdtreeContours.add(vox.getArray(), vox);
                        }
                    }
                }
            }
        }
    }

    /**
     *
     * @param ima to find the maximum value
     * @return the voxel with the max value
     */
    public Voxel3D getPixelMax(ImageHandler ima) {
        Voxel3D res = null;
        float pix;
        float max = -Float.MAX_VALUE;

        for (int k = zmin; k <= zmax; k++) {
            for (int j = ymin; j <= ymax; j++) {
                for (int i = xmin; i <= xmax; i++) {
                    if (labelImage.getPixel(i, j, k) == value) {
                        pix = ima.getPixel(i, j, k);
                        if (pix > max) {
                            max = pix;
                            res = new Voxel3D(i, j, k, pix);
                        }
                    }
                }
            }
        }
        return res;
    }

    /**
     * the pixel is contour ?
     *
     * @param x x coordinate
     * @param y y coordinate
     * @param z z coordinate
     * @return true if pixel is contour
     */
    private boolean isContour(int x, int y, int z) {
        if (labelImage.getPixel(x, y, z) != value) {
            return false;
        }
        ArrayUtil vois = labelImage.getNeighborhood3x3x3(x, y, z);
        // different from its own value
        if (!vois.hasOnlyValue(value)) {
            return true;
        }
        return false;
    }

    /**
     * Compute the barycenter and the volume
     */
    protected void computeCenter() {
        bx = 0;
        by = 0;
        bz = 0;
        int sum = 0;

        for (int k = zmin; k <= zmax; k++) {
            for (int j = ymin; j <= ymax; j++) {
                for (int i = xmin; i <= xmax; i++) {
                    if (labelImage.getPixel(i, j, k) == value) {
                        bx += i;
                        by += j;
                        bz += k;
                        sum++;
                    }
                }
            }
        }
        bx /= sum;
        by /= sum;
        bz /= sum;

        volume = sum;
    }

    /**
     * compute mass center with an image
     *
     * @param ima the image
     */
    protected void computeMassCenter(ImageHandler ima) {
        if (ima != null) {
            cx = 0;
            cy = 0;
            cz = 0;
            double sum = 0;
            double sum2 = 0;
            double pix;
            double pmin = Double.MAX_VALUE;
            double pmax = -Double.MAX_VALUE;
            for (int k = zmin; k <= zmax; k++) {
                for (int j = ymin; j <= ymax; j++) {
                    for (int i = xmin; i <= xmax; i++) {
                        if (labelImage.getPixel(i, j, k) == value) {
                            pix = ima.getPixel(i, j, k);
                            cx += i * pix;
                            cy += j * pix;
                            cz += k * pix;
                            sum += pix;
                            sum2 += pix * pix;
                            if (pix > pmax) {
                                pmax = pix;
                            }
                            if (pix < pmin) {
                                pmin = pix;
                            }
                        }
                    }
                }
            }
            cx /= sum;
            cy /= sum;
            cz /= sum;

            integratedDensity = sum;

            pixmin = pmin;
            pixmax = pmax;

            // standard dev
            int vol = getVolumePixels();
            sigma = Math.sqrt((sum2 - ((sum * sum) / vol)) / (vol - 1));
        }
    }

    /**
     * Computation of the dispersion tensor with units value
     */
    public void computeMoments2(boolean normalize) {
        s200 = 0;
        s110 = 0;
        s101 = 0;
        s020 = 0;
        s011 = 0;
        s002 = 0;

        for (int k = zmin; k <= zmax; k++) {
            for (int j = ymin; j <= ymax; j++) {
                for (int i = xmin; i <= xmax; i++) {
                    if (labelImage.getPixel(i, j, k) == value) {
                        s200 += (i - bx) * (i - bx);
                        s020 += (j - by) * (j - by);
                        s002 += (k - bz) * (k - bz);
                        s110 += (i - bx) * (j - by);
                        s101 += (i - bx) * (k - bz);
                        s011 += (j - by) * (k - bz);
                    }
                }
            }
        }
        // resolution
        s200 *= resXY * resXY;
        s020 *= resXY * resXY;
        s002 *= resZ * resZ;
        s110 *= resXY * resXY;
        s101 *= resXY * resZ;
        s011 *= resXY * resZ;
        // normalize by volume
        if (normalize) {
            s200 /= volume;
            s020 /= volume;
            s002 /= volume;
            s110 /= volume;
            s101 /= volume;
            s011 /= volume;
        }

        eigen = null;
    }

    public void computeMoments3() {
        s300 = s030 = s003 = 0;
        s210 = s201 = s120 = s021 = s102 = s012 = s111 = 0;

        for (int k = zmin; k <= zmax; k++) {
            for (int j = ymin; j <= ymax; j++) {
                for (int i = xmin; i <= xmax; i++) {
                    if (labelImage.getPixel(i, j, k) == value) {
                        double xx = (i - bx);
                        double yy = (j - by);
                        double zz = (k - bz);
                        s300 += xx * xx * xx;
                        s030 += yy * yy * yy;
                        s003 += zz * zz * zz;
                        s210 += xx * xx * yy;
                        s201 += xx * xx * zz;
                        s120 += yy * yy * xx;
                        s021 += yy * yy * zz;
                        s102 += zz * zz * xx;
                        s012 += zz * zz * yy;
                        s111 += xx * yy * zz;
                    }
                }
            }
        }
        // resolution
        s300 *= resXY * resXY * resXY;
        s030 *= resXY * resXY * resXY;
        s003 *= resZ * resZ * resZ;
        s210 *= resXY * resXY * resXY;
        s201 *= resXY * resXY * resZ;
        s120 *= resXY * resXY * resXY;
        s021 *= resXY * resXY * resZ;
        s102 *= resZ * resZ * resXY;
        s012 *= resZ * resZ * resXY;
        s111 *= resXY * resXY * resZ;

        // normalize by volume
        s300 /= volume;
        s030 /= volume;
        s003 /= volume;
        s210 /= volume;
        s201 /= volume;
        s120 /= volume;
        s021 /= volume;
        s102 /= volume;
        s012 /= volume;
        s111 /= volume;
    }

    public void computeMoments4() {
        s400 = s040 = s040 = s220 = s202 = s022 = s121 = s112 = s211 = 0;
        s103 = s301 = s130 = s310 = s013 = s031 = 0;

        for (int k = zmin; k <= zmax; k++) {
            for (int j = ymin; j <= ymax; j++) {
                for (int i = xmin; i <= xmax; i++) {
                    if (labelImage.getPixel(i, j, k) == value) {
                        double xx = (i - bx);
                        double yy = (j - by);
                        double zz = (k - bz);
                        s400 += xx * xx * xx * xx;
                        s040 += yy * yy * yy * yy;
                        s004 += zz * zz * zz * zz;
                        s220 += xx * xx * yy * yy;
                        s202 += xx * xx * zz * zz;
                        s022 += yy * yy * zz * zz;
                        s121 += xx * yy * yy * zz;
                        s112 += xx * yy * zz * zz;
                        s211 += xx * xx * yy * zz;
                        s103 += xx * zz * zz * zz;
                        s301 += xx * xx * xx * zz;
                        s130 += xx * yy * yy * yy;
                        s310 += xx * xx * xx * yy;
                        s013 += yy * zz * zz * zz;
                        s031 += yy * yy * yy * zz;
                    }
                }
            }
        }
        // calibration
        s400 *= resXY * resXY * resXY * resXY;
        s040 *= resXY * resXY * resXY * resXY;
        s004 *= resZ * resZ * resZ * resZ;
        s220 *= resXY * resXY * resXY * resXY;
        s202 *= resXY * resXY * resZ * resZ;
        s022 *= resXY * resXY * resZ * resZ;
        s121 *= resXY * resXY * resXY * resZ;
        s112 *= resXY * resXY * resZ * resZ;
        s211 *= resXY * resXY * resXY * resZ;
        s103 *= resXY * resZ * resZ * resZ;
        s301 *= resXY * resXY * resXY * resZ;
        s130 *= resXY * resXY * resXY * resXY;
        s310 *= resXY * resXY * resXY * resXY;
        s013 *= resXY * resZ * resZ * resZ;
        s031 *= resXY * resXY * resXY * resZ;
    }

    /**
     * Computation of the inertia tensor with units value
     */
    public void computeMomentsInertia() {
        s200 = 0;
        s110 = 0;
        s101 = 0;
        s020 = 0;
        s011 = 0;
        s002 = 0;
        for (int k = zmin; k <= zmax; k++) {
            for (int j = ymin; j <= ymax; j++) {
                for (int i = xmin; i <= xmax; i++) {
                    if (labelImage.getPixel(i, j, k) == value) {
                        s200 += resXY * resXY * (j - by) * (j - by) + resZ * resZ * (k - bz) * (k - bz);
                        s020 += resXY * resXY * (i - bx) * (i - bx) + resZ * resZ * (k - bz) * (k - bz);
                        s002 += resXY * resXY * (i - bx) * (i - bx) + resXY * resXY * (j - by) * (j - by);
                        s110 += resXY * resXY * (i - bx) * (j - by);
                        s101 += resXY * resZ * (i - bx) * (k - bz);
                        s011 += resXY * resZ * (j - by) * (k - bz);
                    }
                }
            }
        }
    }

    /**
     * Draw the pixel inside a creator
     *
     * @param obj The objectCreator to draw in
     * @param col The value to draw
     */
    public void draw(ObjectCreator3D obj, int col) {
        for (int z = zmin; z <= zmax; z++) {
            for (int y = ymin; y <= ymax; y++) {
                for (int x = xmin; x <= xmax; x++) {
                    if (labelImage.getPixel(x, y, z) == value) {
                        obj.createPixel(x, y, z, col);
                    }
                }
            }
        }
    }

    /**
     * Draw inside a particular Z
     *
     * @param mask The mask image to draw
     * @param z The Z coordinate
     * @param col The value to draw
     */
    public void draw(ByteProcessor mask, int z, int col) {
        for (int x = xmin; x <= xmax; x++) {
            for (int y = ymin; y <= ymax; y++) {
                if (labelImage.getPixel(x, y, z) == value) {
                    mask.putPixel(x, y, col);
                }
            }
        }
    }

    /**
     *
     * @param mask
     * @param col
     */
    public void draw(ImageHandler mask, int col) {
        for (int z = zmin; z <= zmax; z++) {
            for (int x = xmin; x <= xmax; x++) {
                for (int y = ymin; y <= ymax; y++) {
                    if (labelImage.getPixel(x, y, z) == value) {
                        mask.setPixel(x, y, z, col);
                    }
                }
            }
        }
    }

    /**
     *
     * @param mask
     * @param red
     * @param green
     * @param blue
     */
    public void draw(ImageStack mask, int red, int green, int blue) {
        ImageProcessor tmp;
        Color col = new Color(red, green, blue);
        for (int z = zmin; z <= zmax; z++) {
            tmp = mask.getProcessor(z + 1);
            tmp.setColor(col);
            for (int x = xmin; x <= xmax; x++) {
                for (int y = ymin; y <= ymax; y++) {
                    if (labelImage.getPixel(x, y, z) == value) {
                        tmp.drawPixel(x, y);
                    }
                }
            }
        }
    }

    /**
     *
     * @param mask
     * @param col
     */
    public void draw(ImageStack mask, int col) {
        ImageProcessor tmp;
        for (int z = zmin; z <= zmax; z++) {
            tmp = mask.getProcessor(z + 1);
            for (int x = xmin; x <= xmax; x++) {
                for (int y = ymin; y <= ymax; y++) {
                    if (labelImage.getPixel(x, y, z) == value) {
                        tmp.putPixel(x, y, col);
                    }
                }
            }
        }
    }

    /**
     *
     * @param mask
     * @param other
     * @param red
     * @param green
     * @param blue
     */
    public void drawIntersection(ImageStack mask, Object3DLabel other, int red, int green, int blue) {
        ImageProcessor tmp;
        ImageHandler otherSeg = other.getLabelImage();
        int otherValue = other.getValue();
        Color col = new Color(red, green, blue);
        for (int z = zmin; z <= zmax; z++) {
            tmp = mask.getProcessor(z + 1);
            tmp.setColor(col);
            for (int x = xmin; x <= xmax; x++) {
                for (int y = ymin; y <= ymax; y++) {
                    if ((labelImage.getPixel(x, y, z) == value) && (otherSeg.getPixel(x, y, z) == otherValue)) {
                        tmp.drawPixel(x, y);
                    }
                }
            }
        }
    }

    /**
     *
     * @param mask
     * @param other
     * @param col
     */
    public void drawIntersection(ImageStack mask, Object3DLabel other, int col) {
        ImageProcessor tmp;
        ImageHandler otherSeg = other.getLabelImage();
        int otherValue = other.getValue();
        for (int z = zmin; z <= zmax; z++) {
            tmp = mask.getProcessor(z + 1);
            for (int x = xmin; x <= xmax; x++) {
                for (int y = ymin; y <= ymax; y++) {
                    if ((labelImage.getPixel(x, y, z) == value) && (otherSeg.getPixel(x, y, z) == otherValue)) {
                        tmp.putPixel(x, y, col);
                    }
                }
            }
        }
    }

    /**
     * Constructor for the createRoi object
     *
     * @param z Description of the Parameter
     * @return Description of the Return Value
     */
    public Roi createRoi(int z) {
        // IJ.write("create roi " + z);
        int sx = labelImage.sizeX;
        int sy = labelImage.sizeY;
        ByteProcessor mask = new ByteProcessor(sx, sy);
        // object black on white
        //mask.invert();
        draw(mask, z, 255);
        ImagePlus maskPlus = new ImagePlus("mask " + z, mask);
        //maskPlus.show();
        //IJ.run("Create Selection");
        ThresholdToSelection tts = new ThresholdToSelection();
        tts.setup("", maskPlus);
        tts.run(mask);
        maskPlus.updateAndDraw();
        // IJ.write("sel=" + maskPlus.getRoi());
        //maskPlus.hide();

        return maskPlus.getRoi();
    }

    @Override
    public ArrayList<Voxel3D> getVoxels() {
        if (voxels == null) {
            voxels = listVoxels(null);
        }

        return voxels;
    }

    protected Object3DVoxels buildObject3DVoxels() {
        Object3DVoxels obj = new Object3DVoxels(this.getVoxels());
        obj.setCalibration(this.getCalibration());

        return obj;
    }

    /**
     *
     * @param path
     */
    @Override
    public void writeVoxels(String path) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void translate(double x, double y, double z) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public int getColoc(Object3D other) {
        if (this.disjointBox(other)) {
            return 0;
        }
        int count = 0;
        int xmin0;
        int ymin0;
        int zmin0;
        int xmax0;
        int ymax0;
        int zmax0;

        int val = other.getValue();
        ImageInt otherseg = ((Object3DLabel) other).getLabelImage();

        xmin0 = getXmin();
        ymin0 = getYmin();
        zmin0 = getZmin();
        xmax0 = getXmax();
        ymax0 = getYmax();
        zmax0 = getZmax();

        if (other != null) {
            xmin0 = Math.max(xmin0, other.getXmin());
            ymin0 = Math.max(ymin0, other.getYmin());
            zmin0 = Math.max(zmin0, other.getZmin());
            xmax0 = Math.min(xmax0, other.getXmax());
            ymax0 = Math.min(ymax0, other.getYmax());
            zmax0 = Math.min(zmax0, other.getZmax());
        }

        for (int k = zmin0; k <= zmax0; k++) {
            for (int j = ymin0; j <= ymax0; j++) {
                for (int i = xmin0; i <= xmax0; i++) {
                    if ((labelImage.getPixel(i, j, k) == value) && (otherseg.getPixel(i, j, k) == val)) {
                        count++;
                    }
                }
            }
        }

        return count;
    }

    @Override
    public boolean hasOneVoxelColoc(Object3D obj) {
        if (this.disjointBox(obj)) {
            return false;
        }
        if ((this.getLabelImage() == null) || (obj.getLabelImage() == null)) {
            return false;
        }
        // taken from object3DLabel
        int xmin0;
        int ymin0;
        int zmin0;
        int xmax0;
        int ymax0;
        int zmax0;

        int val = obj.getValue();
        ImageInt otherseg = obj.getLabelImage();

        xmin0 = getXmin();
        ymin0 = getYmin();
        zmin0 = getZmin();
        xmax0 = getXmax();
        ymax0 = getYmax();
        zmax0 = getZmax();

        xmin0 = Math.max(xmin0, obj.getXmin());
        ymin0 = Math.max(ymin0, obj.getYmin());
        zmin0 = Math.max(zmin0, obj.getZmin());
        xmax0 = Math.min(xmax0, obj.getXmax());
        ymax0 = Math.min(ymax0, obj.getYmax());
        zmax0 = Math.min(zmax0, obj.getZmax());

        //IJ.log(""+xmin0+"-"+xmax0+" "+ymin0+"-"+ymax0+" "+zmin0+"-"+zmax0+" "+otherseg);
        //labelImage.show("this");
        //otherseg.show("other");
        int offX0 = labelImage.offsetX;
        int offY0 = labelImage.offsetY;
        int offZ0 = labelImage.offsetZ;
        int offX1 = otherseg.offsetX;
        int offY1 = otherseg.offsetY;
        int offZ1 = otherseg.offsetZ;

        for (int k = zmin0; k <= zmax0; k++) {
            for (int j = ymin0; j <= ymax0; j++) {
                for (int i = xmin0; i <= xmax0; i++) {
                    if ((labelImage.getPixel(i - offX0, j - offY0, k - offZ0) == value) && (otherseg.getPixel(i - offX1, j - offY1, k - offZ1) == val)) {
                        return true;
                    }
                }
            }
        }

        return false;
    }

    @Override
    public ArrayUtil listValues(ImageHandler ima) {
        ArrayUtil list = new ArrayUtil(this.getVolumePixels());
        Voxel3D pixel;
        int xmin0;
        int ymin0;
        int zmin0;
        int xmax0;
        int ymax0;
        int zmax0;

        xmin0 = getXmin();
        ymin0 = getYmin();
        zmin0 = getZmin();
        xmax0 = getXmax();
        ymax0 = getYmax();
        zmax0 = getZmax();

        int idx = 0;
        for (int k = zmin0; k <= zmax0; k++) {
            for (int j = ymin0; j <= ymax0; j++) {
                for (int i = xmin0; i <= xmax0; i++) {
                    if (labelImage.getPixel(i, j, k) == value) {
                        list.putValue(idx, ima.getPixel(i, j, k));
                        idx++;
                    }
                }
            }
        }


        return list;
    }
}
