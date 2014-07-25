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
import java.awt.Rectangle;
import java.io.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;
import mcib3d.Jama.EigenvalueDecomposition;
import mcib3d.Jama.Matrix;
import mcib3d.image3d.ImageHandler;
import mcib3d.image3d.ImageInt;
import mcib3d.image3d.ImageShort;
import mcib3d.image3d.processing.Flood3D;
import mcib3d.utils.ArrayUtil;
import mcib3d.utils.KDTreeC;

/**
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
 * An Object3D defined as a list of voxels
 *
 * @author thomas & Cedric
 * @created 17 septembre 2003
 * @lastmodified 27 octobre 2003
 */
public class Object3DVoxels extends Object3D {

    // Objexcts as ArrayList of voxels
    ArrayList<Voxel3D> voxels = null;
    // debug
    private boolean showStatus = false;

    /**
     *
     */
    public Object3DVoxels() {
        value = 1;
        resXY = 1.0;
        resZ = 1.0;
        units = "pix";
        voxels = new ArrayList();
    }

    public Object3DVoxels(int val) {
        value = val;
        resXY = 1.0;
        resZ = 1.0;
        units = "pix";
        voxels = new ArrayList();
    }

    /**
     * constructor
     *
     * @param ima Segmented image
     * @param val Pixel value of the object
     */
    public Object3DVoxels(ImageHandler ima, int val) {
        value = val;
        // Calibration
        resXY = 1.0;
        resZ = 1.0;
        units = "pix";
        Calibration cal = ima.getCalibration();
        if (cal != null) {
            if (cal.scaled()) {
                resXY = cal.getX(1.0);
                resZ = cal.getZ(1.0);
                units = cal.getUnits();
            }
        }

        voxels = createArrayList(ima, null);
        init();
    }

    public Object3DVoxels(ImageHandler ima) {
        value = (int) ima.getMax();
        // Calibration
        resXY = 1.0;
        resZ = 1.0;
        units = "pix";
        Calibration cal = ima.getCalibration();
        if (cal != null) {
            if (cal.scaled()) {
                resXY = cal.getX(1.0);
                resZ = cal.getZ(1.0);
                units = cal.getUnits();
            }
        }
        voxels = createArrayList(ima, null);
        init();
    }

    public Object3DVoxels(ImageHandler imaSeg, ImageHandler imaRaw) {
        // normally binarized image 255 or 1
        value = (int) imaSeg.getMax();
        // Calibration
        resXY = 1.0;
        resZ = 1.0;
        units = "pix";
        Calibration cal = imaSeg.getCalibration();
        if (cal != null) {
            if (cal.scaled()) {
                resXY = cal.getX(1.0);
                resZ = cal.getZ(1.0);
                units = cal.getUnits();
            }
        }

        voxels = createArrayList(imaSeg, imaRaw);
        init();
    }

    /**
     * Constructor for the Object3D object
     *
     * @param plus Segmented image
     * @param val Pixel value of the object
     */
    public Object3DVoxels(ImagePlus plus, int val) {
        value = val;
        // Calibration
        resXY = 1.0;
        resZ = 1.0;
        units = "pix";
        Calibration cal = plus.getCalibration();
        if (cal != null) {
            if (cal.scaled()) {
                resXY = cal.getX(1.0);
                resZ = cal.getZ(1.0);
                units = cal.getUnits();
            }
        }
        voxels = createArrayList(ImageInt.wrap(plus), null);
        init();
    }

    /**
     * Constructor for the Object3D object
     *
     * @param plus Segmented image
     * @param val Pixel value of the object
     */
    public Object3DVoxels(ImageStack stack, int val) {
        value = val;
        // No Calibration in ImageStack
        resXY = 1.0;
        resZ = 1.0;
        units = "pix";

        voxels = createArrayList(ImageInt.wrap(stack), null);
        init();
    }

    /**
     *
     * @param al
     */
    public Object3DVoxels(ArrayList<Voxel3D> al) {
        voxels = al;
        init();
        value = (int) voxels.get(0).getValue();

        resXY = 1.0;
        resZ = 1.0;
        units = "pix";
    }

    // ne copie pas les voxels -> todo mettre un boolean si necessaire
    public Object3DVoxels copyObject(int newLabel) {
        Object3DVoxels res = new Object3DVoxels(getVoxels());
        //compute contours if label is different
        if (newLabel != value) {
            this.computeContours();
            res.setContours(contours, areaContactUnit);
            res.setValue(newLabel);
        }

        res.resXY = resXY;
        res.resZ = resZ;
        res.units = units;
        return res;
    }

    public void setVoxels(ArrayList<Voxel3D> al) {
        voxels = al;
        init();
    }

    private ArrayList<Voxel3D> createArrayList(ImageHandler ima, ImageHandler raw) {
        ArrayList<Voxel3D> voxelsTmp = new ArrayList();
        xmin = ima.sizeX;
        xmax = 0;
        ymin = ima.sizeY;
        ymax = 0;
        zmin = ima.sizeZ;
        zmax = 0;
        float val;
        for (int k = 0; k < ima.sizeZ; k++) {
            for (int j = 0; j < ima.sizeY; j++) {
                for (int i = 0; i < ima.sizeX; i++) {
                    if (ima.getPixel(i, j, k) == value) {
                        // add to voxel list
                        if (raw == null) {
                            val = value;
                        } else {
                            val = raw.getPixel(i, j, k);
                        }
                        voxelsTmp.add(new Voxel3D(i, j, k, val));
                    }
                }
            }
        }
        return voxelsTmp;
    }

    public void createSphereUnit(float cx, float cy, float cz, float rad) {
        float rxy = (float) this.getCalibration().pixelWidth;
        float rz = (float) this.getCalibration().pixelDepth;
        int ix = (int) Math.round(cx);
        int iy = (int) Math.round(cy);
        int iz = (int) Math.round(cz);
        float raxy = rad / rxy;
        float raz = rad / rz;

        this.createEllipsoidPixel(ix, iy, iz, raxy, raxy, raz);
    }

    public void createEllipsoidPixel(int cx, int cy, int cz, float rx, float ry, float rz) {
        ObjectCreator3D ell = new ObjectCreator3D((int) (2 * rx + 1), (int) (2 * ry + 1), (int) (2 * rz + 1));
        // centers of ellispoid
        int ex = Math.round(rx);
        int ey = Math.round(ry);
        int ez = Math.round(rz);
        ell.createEllipsoid(ex, ey, ez, rx, ry, rz, 255, false);
        int tmpval = this.getValue();
        this.setValue(255);
        voxels = createArrayList((ImageInt) ell.getImageHandler(), null);
        this.setValue(tmpval);
        this.translate(cx - ex, cy - ey, cz - ez);
        init();
    }

    /**
     * copy
     *
     * @param other
     */
    public Object3DVoxels(Object3DVoxels other) {
        voxels = new ArrayList();
        this.addVoxels(other.getVoxels());
        init();
        value = other.getValue();
        this.labelImage = other.getLabelImage();
        this.setCalibration(other.getResXY(), other.getResZ(), other.getUnits());
    }

    public Object3DVoxels(Object3D other) {
        voxels = new ArrayList();
        addVoxels(other.getVoxels());
        init();
        value = other.getValue();
        this.labelImage = other.getLabelImage();
        this.setCalibration(other.getResXY(), other.getResZ(), other.getUnits());
    }

    // intersection 
    public void addVoxelsIntersection(Object3D ob1, Object3D ob2) {
        ArrayList<Voxel3D> al1 = ob1.getVoxels();
        ArrayList<Voxel3D> al2 = ob2.getVoxels();
        double dist = 0.25;// normally int values (0.25=0.5²)
        Voxel3D v1, v2;
        for (Iterator it1 = al1.iterator(); it1.hasNext();) {
            v1 = (Voxel3D) it1.next();
            for (Iterator it2 = al2.iterator(); it2.hasNext();) {
                v2 = (Voxel3D) it2.next();
                if (v1.distanceSquare(v2) < dist) {
                    voxels.add(new Voxel3D(v1));
                    break;
                }
            }
        }
        init();
        contours = null;
    }

    // intersection multiple
    public void addVoxelsIntersection(ArrayList<Object3D> objs) {
        int nbob = objs.size();
        ArrayList<Voxel3D> al1 = objs.get(0).getVoxels();
        ArrayList<Voxel3D> al2 = objs.get(1).getVoxels();
        double dist = 0.25;// normally int values (0.25=0.5²)
        Voxel3D v1, v2;
        for (Iterator it1 = al1.iterator(); it1.hasNext();) {
            v1 = (Voxel3D) it1.next();
            for (Iterator it2 = al2.iterator(); it2.hasNext();) {
                v2 = (Voxel3D) it2.next();
                if (v1.distanceSquare(v2) < dist) {
                    // test if any voxel in all other object
                    boolean ok = true;
                    int i = 2;
                    while ((ok) && (i < nbob)) {
                        ok = false;
                        for (Voxel3D v : (objs.get(i).getVoxels())) {
                            if (v1.distanceSquare(v) < dist) {
                                ok = true;
                                i++;
                                break;
                            }
                        }
                    } // while
                    if (ok) {
                        voxels.add(new Voxel3D(v1));
                    }
                }
            }
        }
        init();
    }

    // union 
    public void addVoxelsUnion(Object3D ob1, Object3D ob2) {
        ArrayList<Voxel3D> al1 = ob1.getVoxels();
        ArrayList<Voxel3D> al2 = ob2.getVoxels();
        Voxel3D v;
        for (Iterator it = al1.iterator(); it.hasNext();) {
            v = (Voxel3D) it.next();
            voxels.add(v);
        }
        for (Iterator it = al2.iterator(); it.hasNext();) {
            v = (Voxel3D) it.next();
            voxels.add(v);
        }
        init();
        contours = null;
    }

    // union multiple
    public void addVoxelsUnion(ArrayList<Object3D> objs) {
        for (Object3D ob : objs) {
            for (Voxel3D v : ob.getVoxels()) {
                voxels.add(v);
            }
        }
        init();
        contours = null;
    }

    // substraction
    private void substractObjectVoxels(Object3D other) {
        ArrayList<Voxel3D> al2 = other.getVoxels();
        ArrayList<Voxel3D> al11 = new ArrayList();
        //IJ.log("sub vox " + al11.size());
        double dist = 0.25;// normally int values (0.25=0.5²)
        Voxel3D v2;
        IJ.showStatus("Substracting " + other.getVolumePixels() + " voxels");
        //IJ.log("Substracting " + other.getVolumePixels() + " voxels");
        for (Voxel3D vox : voxels) {
            boolean inter = false;
            for (Voxel3D vox2 : al2) {
                if (vox.distanceSquare(vox2) < dist) {
                    inter = true;
                    break;
                }
            }
            if (!inter) {
                al11.add(vox);
            }
        }
        //IJ.log("sub vox " + al11.size());
        voxels = al11;
        init();
        contours = null;
    }

    private void substractObjectImage(Object3D obj) {
        int xmin0 = getXmin();
        int ymin0 = getYmin();
        int zmin0 = getZmin();
        int xmax0 = getXmax();
        int ymax0 = getYmax();
        int zmax0 = getZmax();

        int otherval = obj.getValue();
        ImageInt otherseg = obj.getLabelImage();

        for (int k = zmin0; k <= zmax0; k++) {
            for (int j = ymin0; j <= ymax0; j++) {
                for (int i = xmin0; i <= xmax0; i++) {
                    if ((labelImage.contains(i, j, k)) && (otherseg.contains(i, j, k))) {
                        if ((labelImage.getPixel(i, j, k) == value) && (otherseg.getPixel(i, j, k) == otherval)) {
                            labelImage.setPixel(i, j, k, 0);
                        }
                    }
                }
            }
        }
        // reset voxels 
        voxels = createArrayList(labelImage, null);
        init();
    }

    public void substractObject(Object3D other) {
        final int seui_vol = 20;
        if ((labelImage != null) && (other.getLabelImage() != null) && (getVolumePixels() > seui_vol) && (other.getVolumePixels() > seui_vol)) {
            substractObjectImage(other);
        } else {
            substractObjectVoxels(other);
        }
    }

    //private void addNewVoxels(ArrayList vox) {
    //    this.addNewVoxels(vox);
    //}
    /**
     *
     * @param vox
     */
    public final void addVoxels(ArrayList vox) {
        voxels.addAll(vox);
        init();
        contours = null;
    }

    public void removeVoxels(int threshold) {
        ArrayList<Voxel3D> toRemove = new ArrayList();
        for (Voxel3D V : voxels) {
            if (V.getValue() < threshold) {
                toRemove.add(V);
            }
        }
        if (!toRemove.isEmpty()) {
            voxels.removeAll(toRemove);
            init();
            contours = null;
        }
    }

    public boolean isConnex() {
        ImageShort seg = (ImageShort) this.createSegImageMini(1, 1);
        Voxel3D se = this.getFirstVoxel();
        se.translate(-seg.offsetX, -seg.offsetY, -seg.offsetZ);
        Flood3D.flood3d26(seg, se.getRoundX(), se.getRoundY(), se.getRoundZ(), 2);
        if (seg.hasOneValueInt(1)) {
            return false;
        } else {
            return true;
        }
    }

    /**
     *
     * @param showStatus
     */
    public void setShowStatus(boolean showStatus) {
        this.showStatus = showStatus;
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

        Iterator it = voxels.iterator();
        Voxel3D vox;

        while (it.hasNext()) {
            vox = (Voxel3D) it.next();
            pix = ima.getPixel(vox);
            if (pix > max) {
                max = pix;
                res = new Voxel3D(vox);
            }
        }

        return res;
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

        double i, j, k;
        Voxel3D vox;
        Iterator it = voxels.iterator();
        while (it.hasNext()) {
            vox = (Voxel3D) it.next();
            i = vox.getX();
            j = vox.getY();
            k = vox.getZ();
            s200 += resXY * resXY * (j - by) * (j - by) + resZ * resZ * (k - bz) * (k - bz);
            s020 += resXY * resXY * (i - bx) * (i - bx) + resZ * resZ * (k - bz) * (k - bz);
            s002 += resXY * resXY * (i - bx) * (i - bx) + resXY * resXY * (j - by) * (j - by);
            s110 += resXY * resXY * (i - bx) * (j - by);
            s101 += resXY * resZ * (i - bx) * (k - bz);
            s011 += resXY * resZ * (j - by) * (k - bz);
        }
    }

    /**
     * Computation of the dispersion tensor with units value
     */
    protected void computeMoments2(boolean normalize) {
        s200 = 0;
        s110 = 0;
        s101 = 0;
        s020 = 0;
        s011 = 0;
        s002 = 0;

        double i, j, k;
        Voxel3D vox;
        Iterator it = voxels.iterator();
        while (it.hasNext()) {
            vox = (Voxel3D) it.next();
            i = vox.getX();
            j = vox.getY();
            k = vox.getZ();
            s200 += (i - bx) * (i - bx);
            s020 += (j - by) * (j - by);
            s002 += (k - bz) * (k - bz);
            s110 += (i - bx) * (j - by);
            s101 += (i - bx) * (k - bz);
            s011 += (j - by) * (k - bz);
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
        double i, j, k;
        Voxel3D vox;
        Iterator it = voxels.iterator();
        while (it.hasNext()) {
            vox = (Voxel3D) it.next();
            i = vox.getX();
            j = vox.getY();
            k = vox.getZ();
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
    }

    public void computeMoments4() {
        s400 = s040 = s040 = s220 = s202 = s022 = s121 = s112 = s211 = 0;
        s103 = s301 = s130 = s310 = s013 = s031 = 0;

        double i, j, k;
        Voxel3D vox;
        Iterator it = voxels.iterator();
        while (it.hasNext()) {
            vox = (Voxel3D) it.next();
            i = vox.getX();
            j = vox.getY();
            k = vox.getZ();
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
     * Compute the barycenter and the volume
     */
    protected void computeCenter() {
        bx = 0;
        by = 0;
        bz = 0;
        Voxel3D vox;
        Iterator it = voxels.iterator();
        while (it.hasNext()) {
            vox = (Voxel3D) it.next();
            bx += vox.getX();
            by += vox.getY();
            bz += vox.getZ();
        }
        int sum = voxels.size();
        bx /= sum;
        by /= sum;
        bz /= sum;

        volume = sum;
    }

    public void setContours(ArrayList<Voxel3D> contours, double areaUnit) {
        this.areaContactUnit = areaUnit;
        this.areaNbVoxels = contours.size();
        this.contours = contours;
        this.kdtreeContours = null; //kdTree computed if needed (getKdtreeContours())
    }

    /**
     * Compute the contours of the object rad=0.5
     */
    private void computeContours(ImageInt segImage, int x0, int y0, int z0) {
        // init kdtree
        kdtreeContours = new KDTreeC(3);
        kdtreeContours.setScale3(this.resXY, this.resXY, this.resZ);
        areaNbVoxels = 0;
        areaContactUnit = 0;
        areaContactVoxels = 0;
        contours = new ArrayList();
        boolean cont;
        double XZ = resXY * resZ;
        double XX = resXY * resXY;
        int pix0, pix1, pix2, pix3, pix4, pix5, pix6;
        int sx = segImage.sizeX;
        int sy = segImage.sizeY;
        int sz = segImage.sizeZ;
        // TODO parcourir seulement les objets de arraylist voxels et non la bounding box!
        // pb ??
//       for (Voxel3D vox : voxels) {
//            int i = vox.getRoundX() - x0;
//            int j = vox.getRoundY() - y0;
//            int k = vox.getRoundZ() - z0;
        for (int k = zmin - z0; k <= zmax - z0; k++) {
            if (showStatus) {
                IJ.showStatus("Contours " + (100 * (k - zmin) / (zmax - zmin + 1)) + " % ");
            }
            for (int j = ymin - y0; j <= ymax - y0; j++) {
                for (int i = xmin - x0; i <= xmax - x0; i++) {
                    cont = false;
                    if (segImage.contains(i, j, k)) {
                        pix0 = segImage.getPixelInt(i, j, k);
                        if (pix0 == value) {
                            if (i + 1 < sx) {
                                pix1 = segImage.getPixelInt(i + 1, j, k);
                            } else {
                                pix1 = 0;
                            }
                            if (i - 1 >= 0) {
                                pix2 = segImage.getPixelInt(i - 1, j, k);
                            } else {
                                pix2 = 0;
                            }
                            if (j + 1 < sy) {
                                pix3 = segImage.getPixelInt(i, j + 1, k);
                            } else {
                                pix3 = 0;
                            }
                            if (j - 1 >= 0) {
                                pix4 = segImage.getPixelInt(i, j - 1, k);
                            } else {
                                pix4 = 0;
                            }
                            if (k + 1 < sz) {
                                pix5 = segImage.getPixelInt(i, j, k + 1);
                            } else {
                                pix5 = 0;
                            }
                            if (k - 1 >= 0) {
                                pix6 = segImage.getPixelInt(i, j, k - 1);
                            } else {
                                pix6 = 0;
                            }
                            if (pix1 != value) {
                                cont = true;
                                areaContactUnit += XZ;
                                areaContactVoxels++;
                            }
                            if (pix2 != value) {
                                cont = true;
                                areaContactUnit += XZ;
                                areaContactVoxels++;
                            }
                            if (pix3 != value) {
                                cont = true;
                                areaContactUnit += XZ;
                                areaContactVoxels++;
                            }
                            if (pix4 != value) {
                                cont = true;
                                areaContactUnit += XZ;
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
                                Voxel3D voxC = new Voxel3D(i + x0, j + y0, k + z0, value);
                                contours.add(voxC);
                                kdtreeContours.add(voxC.getArray(), voxC);
                            }
                        }
                    }
                }
            }
        }

        if (areaNbVoxels == 0) {
            //new ImagePlus("MiniSeg 0", segImage.getImageStack()).show();
            //IJ.log(" " + x0 + " " + y0 + " " + z0 + " " + xmin + " " + ymin + " " + zmin);
        }
    }

    @Override
    public void computeContours() {
        if (labelImage != null) {
            this.computeContours(labelImage, 0, 0, 0);
        } else {
            // segImage = this.createSegImageMini(value, 1);
            ImageInt segImage = this.getLabelImage();
            this.computeContours(segImage, segImage.offsetX, segImage.offsetY, segImage.offsetZ);
        }
    }

    /**
     * Compute the bounding box of the object
     */
    @Override
    protected void computeBounding() {
        xmin = Integer.MAX_VALUE;
        xmax = 0;
        ymin = Integer.MAX_VALUE;
        ymax = 0;
        zmin = Integer.MAX_VALUE;
        zmax = 0;

        Voxel3D vox;
        Iterator it = voxels.iterator();
        while (it.hasNext()) {
            vox = (Voxel3D) it.next();
            if (vox.getX() < xmin) {
                xmin = (int) Math.floor(vox.getX());
            }
            if (vox.getX() > xmax) {
                xmax = (int) Math.ceil(vox.getX());
            }
            if (vox.getY() < ymin) {
                ymin = (int) Math.floor(vox.getY());
            }
            if (vox.getY() > ymax) {
                ymax = (int) Math.ceil(vox.getY());
            }
            if (vox.getZ() < zmin) {
                zmin = (int) Math.floor(vox.getZ());
            }
            if (vox.getZ() > zmax) {
                zmax = (int) Math.ceil(vox.getZ());
            }
        }
    }

    /**
     * Constructor for the computeEigenInertia object
     */
    private void computeEigenInertia() {
        if (eigen == null) {
            Matrix mat = new Matrix(3, 3);
            mat.set(0, 0, s200);
            mat.set(0, 1, -s110);
            mat.set(0, 2, -s101);
            mat.set(1, 0, -s110);
            mat.set(1, 1, s020);
            mat.set(1, 2, -s011);
            mat.set(2, 0, -s101);
            mat.set(2, 1, -s011);
            mat.set(2, 2, s002);

            eigen = new EigenvalueDecomposition(mat);
        }
    }

//    /**
//     * Compute the pourcentage for contact
//     *
//     * @param obj
//     * @return
//     */
//    @Override
//    public double pcColoc(Object3D obj) {
//        double pourc;
//        if (!this.intersectionBox(obj)) {
//            return 0.0;
//        }
//        ArrayList<Voxel3D> al1 = this.getVoxels();
//        ArrayList<Voxel3D> al2 = ((Object3DVoxels) obj).getVoxels();
//        double cpt = 0;
//        double dist = 0.25;// normally int values (0.25=0.5²)
//        Voxel3D v1, v2;
//        for (Iterator it1 = al1.iterator(); it1.hasNext();) {
//            v1 = (Voxel3D) it1.next();
//            IJ.showStatus("Testing coloc " + v1);
//            for (Iterator it2 = al2.iterator(); it2.hasNext();) {
//                v2 = (Voxel3D) it2.next();
//                if (v1.distanceSquare(v2) < dist) {
//                    cpt++;
//                }
//            }
//        }
//        pourc = (cpt / (double) al1.size()) * 100.0;
//
//        return pourc;
//    }
    protected ArrayList<Voxel3D> getVoxelInsideBoundingBox(int[] boundingBox) { //xmin, xmax, ymin, ymax, zmin, zmax
        ArrayList<Voxel3D> res = new ArrayList<Voxel3D>();
        for (Voxel3D v : voxels) {
            if (v.isInsideBoundingBox(boundingBox)) {
                res.add(v);
            }
        }
        return res;
    }

    @Override
    public int getColoc(Object3D obj) {
        // test box
        if (this.disjointBox(obj)) {
            return 0;
        }
        // if labels images for both objects, use them
        if ((this.getLabelImage() != null) || (obj.getLabelImage() != null)) {
            return getColocImage(obj);
        }

        // thresgold on size of objects
        int thres = 1000;
        // if one object has size > threshold use images else use voxels
        if ((this.getVolumePixels() > thres) && (obj.getVolumePixels() > thres)) {
            //IJ.log("Using coloc image");
            return getColocImage(obj);
        } else {
            //IJ.log("Using coloc voxels");
            return getColocVoxels(obj);
        }

    }

    @Override
    public boolean hasOneVoxelColoc(Object3D obj) {
        return hasOneVoxelColocVoxels(obj);
    }

    /*
     @Override
     public boolean hasOneVoxelColoc(Object3D obj) {
     // if no associated label image, use voxels
     if ((this.getLabelImage() == null) || (obj.getLabelImage() == null)) {
     //IJ.log("Using coloc voxels");
     return hasOneVoxelColocVoxels(obj);
     } else {
     // thresgold on size of objects
     int thres = 1000;
     // if one object has size > threshold use images else use voxels
     if ((this.getVolumePixels() > thres) && (obj.getVolumePixels() > thres)) {
     //IJ.log("Using coloc image");
     return hasOneVoxelColocImage(obj);
     } else {
     //IJ.log("Using coloc voxels");
     return hasOneVoxelColocVoxels(obj);
     }
     }
     }
     * 
     */
    private int getColocImage(Object3D obj) {
        if (this.disjointBox(obj)) {
            return 0;
        }
        // if no label images, create temporary one
        if ((this.getLabelImage() == null) || (obj.getLabelImage() == null)) {
            return getColocImageIntersection(obj);
        }
        // taken from object3DLabel
        int count = 0;
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
                        count++;
                    }
                }
            }
        }

        return count;
    }

    private int getColocImageIntersection(Object3D other) {
        ImageInt inter = this.createIntersectionImage(other, 1, 2);
        int count = 0;
        for (int i = 0; i < inter.sizeXYZ; i++) {
            if (inter.getPixelInt(i) == 3) {
                count++;
            }
        }
        return count;
    }

    private int getColocVoxels(Object3D obj) {
        if (this.disjointBox(obj)) {
            return 0;
        }
        int[] intersec = this.getIntersectionBox(obj);
        ArrayList<Voxel3D> al1 = this.getVoxelInsideBoundingBox(intersec);
        if (!(obj instanceof Object3DVoxels)) {
            obj = obj.getObject3DVoxels();
        }
        ArrayList<Voxel3D> al2 = ((Object3DVoxels) obj).getVoxelInsideBoundingBox(intersec);

        int cpt = 0;
        for (Voxel3D v1 : al1) {
            for (Voxel3D v2 : al2) {
                if (v1.sameVoxel(v2)) {
                    cpt++;
                }
            }
        }
        //System.out.println("Coloc:"+value +" voxelBB:"+al2.size()+ " coloc:"+cpt);
        return cpt;
    }

    private boolean hasOneVoxelColocImage(Object3D obj) {
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

    private boolean hasOneVoxelColocVoxels(Object3D obj) {
        if (this.disjointBox(obj)) {
            return false;
        }
        int[] intersec = this.getIntersectionBox(obj);
        ArrayList<Voxel3D> al1 = this.getVoxelInsideBoundingBox(intersec);
        if (!(obj instanceof Object3DVoxels)) {
            obj = obj.getObject3DVoxels();
        }
        ArrayList<Voxel3D> al2 = ((Object3DVoxels) obj).getVoxelInsideBoundingBox(intersec);
        for (Voxel3D v1 : al1) {
            for (Voxel3D v2 : al2) {
                if (v1.sameVoxel(v2)) {
                    return true;
                }
            }
        }
        //System.out.println("Coloc:"+value +" voxelBB:"+al2.size()+ " coloc:"+cpt);
        return false;
    }

//    public double pcColocVoxels(Object3D obj) {
//        double pourc;
//        if (!this.intersectionBox(obj)) {
//            return 0.0;
//        }
//        ArrayList<Voxel3D> al1 = this.getVoxels();
//        ArrayList<Voxel3D> al2 = ((Object3DVoxels) obj).getVoxels();
//        double cpt = 0;
//        double dist = 0.25;// normally int values (0.25=0.5²)
//        Voxel3D v1, v2;
//        for (Iterator it1 = al1.iterator(); it1.hasNext();) {
//            v1 = (Voxel3D) it1.next();
//            for (Iterator it2 = al2.iterator(); it2.hasNext();) {
//                v2 = (Voxel3D) it2.next();
//                if (v1.distanceSquare(v2) < dist) {
//                    cpt++;
//                }
//            }
//        }
//        pourc = (100.0 * 2.0 * cpt) / ((double) (al1.size() + al2.size()));
//
//        return pourc;
//    }
    public double pcColoc2(ArrayList<Object3DVoxels> objs) {
        double pourc;
        int nb = objs.size();
        int cpt = 0, sumVol = 1;
        Object3DVoxels ob1 = this;
        Object3DVoxels ob2 = objs.get(0);
        if (ob1.pcColoc(ob2) > 0) {
            Object3DVoxels obInter = new Object3DVoxels();
            obInter.addVoxelsIntersection(ob1, ob2);
            cpt = obInter.getVolumePixels();
            sumVol = ob1.getVolumePixels() + ob2.getVolumePixels();
            int i = 1;
            boolean ok = true;
            while ((ok) && (i < nb)) {
                ok = false;
                Object3DVoxels ob3 = objs.get(i);
                if (!obInter.intersectionBox(ob3)) {
                    cpt = 0;
                } else {
                    if (obInter.pcColoc(ob3) > 0) {
                        ok = true;
                        sumVol += ob3.getVolumePixels();
                        Object3DVoxels ob4 = new Object3DVoxels();
                        ob4.addVoxelsIntersection(obInter, ob3);
                        obInter = ob4;
                        cpt = obInter.getVolumePixels();
                        i++;
                    } else {
                        cpt = 0;
                    }
                }
            }
        }
        pourc = (100.0 * (nb * cpt)) / ((double) (sumVol));

        return pourc;
    }

    /**
     * Draw the pixel inside a creator
     *
     * @param obj The objectCreator to draw in
     * @param col The value to draw
     */
    public void draw(ObjectCreator3D obj, int col) {
        Voxel3D vox;
        Iterator it = voxels.iterator();
        while (it.hasNext()) {
            vox = (Voxel3D) it.next();
            int vx = vox.getRoundX();
            int vy = vox.getRoundY();
            int vz = vox.getRoundZ();
            if (obj.img.contains(vx, vy, vz)) {
                obj.createPixel(vx, vy, vz, col);
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
        int sx = this.getXmax() - this.getXmin() + 1;
        int sy = this.getYmax() - this.getYmin() + 1;
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
        Roi roi = maskPlus.getRoi();
        Rectangle rect = roi.getBounds();
        rect.x += this.getXmin();
        rect.y += this.getYmin();

        return roi;
    }

    /**
     * Draw inside a particular Z
     *
     * @param mask The mask image to draw
     * @param z The Z coordinate
     * @param col The value to draw
     */
    public void draw(ByteProcessor mask, int z, int col) {
        Voxel3D vox;
        Iterator it = voxels.iterator();
        while (it.hasNext()) {
            vox = (Voxel3D) it.next();
            if (Math.abs(z - vox.getZ()) < 0.5) {
                mask.putPixel(vox.getRoundX(), vox.getRoundY(), col);
            }
        }
    }

    /**
     *
     * @param ima
     * @param col
     */
    public void drawContours(ObjectCreator3D ima, int col) {
        int s = contours.size();
        Voxel3D p2;
        for (int j = 0; j < s; j++) {
            p2 = (Voxel3D) contours.get(j);
            ima.createPixel(p2.getRoundX(), p2.getRoundY(), p2.getRoundZ(), col);
        }
    }

    public void draw(ImageStack mask, int col) {
        Voxel3D vox;
        Iterator it = voxels.iterator();
        while (it.hasNext()) {
            vox = (Voxel3D) it.next();

            mask.setVoxel(vox.getRoundX(), vox.getRoundY(), vox.getRoundZ(), col);
        }
    }

    public void draw(ImageStack mask, int r, int g, int b) {
        Voxel3D vox;
        ImageProcessor tmp;
        Color col = new Color(r, g, b);
        Iterator it = voxels.iterator();
        while (it.hasNext()) {
            vox = (Voxel3D) it.next();
            tmp = mask.getProcessor((int) (vox.getZ() + 1));
            tmp.setColor(col);
            tmp.drawPixel(vox.getRoundX(), vox.getRoundY());
        }
    }

    @Override
    public ArrayList<Voxel3D> getVoxels() {
        return voxels;
    }

    public Voxel3D getFirstVoxel() {
        return voxels.get(0);
    }

    // From Bribiesca 2008 Pattern Recognition
    public double getDiscreteCompactness() {
        double n = getVolumePixels();
        double tmp = Math.pow(n, 2.0 / 3.0);

        return ((n - areaContactVoxels / 6.0) / (n - tmp));
    }

    /**
     *
     * @param path
     */
    @Override
    public void writeVoxels(String path) {
        Voxel3D pixel;
        BufferedWriter bf;
        int c = 0;

        try {
            bf = new BufferedWriter(new FileWriter(path + name + ".3droi"));
            Iterator it = voxels.iterator();
            // calibration
            bf.write("cal=\t" + resXY + "\t" + resZ + "\t" + "\t" + units + "\n");
            while (it.hasNext()) {
                c++;
                pixel = new Voxel3D((Voxel3D) it.next());
                bf.write(c + "\t" + pixel.getX() + "\t" + pixel.getY() + "\t" + pixel.getZ() + "\t" + pixel.getValue() + "\n");
            }
            bf.close();
        } catch (IOException ex) {
            Logger.getLogger(Object3DVoxels.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     *
     * @param path
     * @param name
     */
    public void loadVoxels(String path, String name) {
        BufferedReader bf;
        String data;
        String[] coord;
        double dx, dy, dz;
        int v;
        this.setName(name);
        voxels = new ArrayList();
        try {
            bf = new BufferedReader(new FileReader(path + name));
            data = bf.readLine();
            while (data != null) {
                // calibration
                if (data.startsWith("cal=")) {
                    coord = data.split("\t");
                    resXY = Double.parseDouble(coord[1]);
                    resZ = Double.parseDouble(coord[2]);
                    units = coord[3];
                } else {
                    coord = data.split("\t");
                    dx = Double.parseDouble(coord[1]);
                    dy = Double.parseDouble(coord[2]);
                    dz = Double.parseDouble(coord[3]);
                    v = (int) Double.parseDouble(coord[4]);
                    voxels.add(new Voxel3D(dx, dy, dz, v));
                }
                data = bf.readLine();
            }
            bf.close();
            init();
        } catch (IOException ex) {
            Logger.getLogger(Object3DVoxels.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public void translate(double x, double y, double z) {
        for (Voxel3D v : voxels) {
            v.translate(x, y, z);
        }
        init();
    }

    @Override
    public void translate(Vector3D trans) {
        for (Voxel3D v : voxels) {
            v.translate(trans);
        }
        init();
    }

    public void rotate(Vector3D Axis, double angle) {
        GeomTransform3D trans = new GeomTransform3D();
        trans.setRotation(Axis, angle);

        for (Voxel3D v : voxels) {
            Vector3D tv = trans.getVectorTransformed(v.getVector3D(), this.getCenterAsVector());
            v.setVoxel(tv.getX(), tv.getY(), tv.getZ(), v.getValue());
        }
    }

    @Override
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

            double i, j, k;
            Voxel3D vox;
            Iterator it = voxels.iterator();
            while (it.hasNext()) {
                vox = (Voxel3D) it.next();
                i = vox.getX();
                j = vox.getY();
                k = vox.getZ();
                if (ima.contains(i, j, k)) {
                    pix = ima.getPixel(vox);
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

    @Override
    public ArrayList<Voxel3D> listVoxels(ImageHandler ima, double thresh) {
        ArrayList<Voxel3D> list = new ArrayList();
        Voxel3D voxel, newVoxel;

        Iterator<Voxel3D> it = voxels.iterator();
        float pixvalue;

        while (it.hasNext()) {
            voxel = it.next();
            if (ima.contains(voxel.getX(), voxel.getY(), voxel.getZ())) {
                pixvalue = ima.getPixel(voxel);
                if (pixvalue > thresh) {
                    newVoxel = new Voxel3D(voxel);
                    newVoxel.setValue(pixvalue);
                    list.add(newVoxel);
                }
            }
        }

        return list;
    }

    // list voxels when object translated to new position for center
    public ArrayUtil listVoxels(ImageHandler ima, int nx, int ny, int nz) {
        ArrayUtil list = new ArrayUtil(this.getVolumePixels());
        Voxel3D voxel;

        Iterator<Voxel3D> it = voxels.iterator();
        float pixvalue;
        double tx = nx - bx;
        double ty = ny - by;
        double tz = nz - bz;
        int idx = 0;
        while (it.hasNext()) {
            voxel = it.next();
            int px = (int) Math.round(tx + voxel.getX());
            int py = (int) Math.round(ty + voxel.getY());
            int pz = (int) Math.round(tz + voxel.getZ());
            if (ima.contains(px, py, pz)) {
                pixvalue = ima.getPixel(px, py, pz);
                list.putValue(idx, pixvalue);
                idx++;
            }

        }
        list.setSize(idx);
        return list;
    }

    @Override
    public void draw(ImageHandler mask, int col) {
        Voxel3D vox;
        Iterator it = voxels.iterator();
        while (it.hasNext()) {
            vox = (Voxel3D) it.next();
            int x = vox.getRoundX();
            int y = vox.getRoundY();
            int z = vox.getRoundZ();
            if (mask.contains(x, y, z)) {
                mask.setPixel(x, y, z, col);
            }
        }
    }

    @Override
    public ArrayUtil listValues(ImageHandler ima) {
        ArrayUtil list = new ArrayUtil(this.getVolumePixels());
        Voxel3D voxel;

        Iterator<Voxel3D> it = voxels.iterator();
        int idx = 0;
        while (it.hasNext()) {
            voxel = it.next();
            if (ima.contains(voxel.getX(), voxel.getY(), voxel.getZ())) {
                list.putValue(idx, ima.getPixel(voxel));
                idx++;
            }
        }
        list.setSize(idx);

        return list;
    }
}
