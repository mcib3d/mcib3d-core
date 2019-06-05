package mcib3d.geom;

//import ij.IJ;

import ij.ImagePlus;
import ij.ImageStack;
import ij.gui.Roi;
import ij.measure.Calibration;
import ij.plugin.filter.ThresholdToSelection;
import ij.process.ByteProcessor;
import ij.process.ImageProcessor;
import mcib3d.Jama.EigenvalueDecomposition;
import mcib3d.Jama.Matrix;
import mcib3d.image3d.*;
import mcib3d.image3d.processing.FillHoles3D;
import mcib3d.utils.ArrayUtil;
import mcib3d.utils.KDTreeC;

import java.awt.*;
import java.io.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * /**
 * Copyright (C) 2008- 2011 Thomas Boudier
 * <p>
 * <p>
 * <p>
 * This file is part of mcib3d
 * <p>
 * mcib3d is free software; you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 3 of the License, or (at your option) any later
 * version.
 * <p>
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * <p>
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 * <p>
 * An Object3D defined as a list of voxels
 *
 * @author thomas & Cedric
 * @created 17 septembre 2003
 * @lastmodified 27 octobre 2003
 */
public class Object3DVoxels extends Object3D {

    // Objexcts as ArrayList of voxels
    LinkedList<Voxel3D> voxels = null;
    // debug
    private boolean showStatus = false;
    private double correctedSurfaceArea = -1;

    /**
     *
     */
    public Object3DVoxels() {
        value = 1;
        resXY = 1.0;
        resZ = 1.0;
        units = "pix";
        voxels = new LinkedList<>();
    }

    public Object3DVoxels(int val) {
        value =  val;
        resXY = 1.0;
        resZ = 1.0;
        units = "pix";
        voxels = new LinkedList<>();
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
        resXY = ima.getScaleXY();
        resZ = ima.getScaleZ();
        units = ima.getUnit();

        voxels = createArrayList(ima, null);
        init();
    }

    public Object3DVoxels(ImageHandler ima) {
        value = (int)ima.getMax();
        // Calibration
        resXY = ima.getScaleXY();
        resZ = ima.getScaleZ();
        units = ima.getUnit();

        voxels = createArrayList(ima, null);
        init();
    }

    public Object3DVoxels(ImageHandler imaSeg, ImageHandler imaRaw) {
        // normally binarized image 255 or 1
        value = (int) imaSeg.getMax();
        // Calibration
        resXY = imaSeg.getScaleXY();
        resZ = imaSeg.getScaleZ();
        units = imaSeg.getUnit();

        voxels = createArrayList(imaSeg, imaRaw);
        init();
    }

    /**
     * @param plus Segmented image
     * @param val  Pixel value of the object
     * @deprecated use Object3D-IJUtils
     * Constructor for the Object3D object
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
     * @param stack Segmented image
     * @param val   Pixel value of the object
     * @deprecated use Object3D-IJUtils
     * Constructor for the Object3D object
     */
    public Object3DVoxels(ImageStack stack, int val) {
        value =val;
        // No Calibration in ImageStack
        resXY = 1.0;
        resZ = 1.0;
        units = "pix";

        voxels = createArrayList(ImageInt.wrap(stack), null);
        init();
    }

    /**
     * @param al
     */
    public Object3DVoxels(LinkedList<Voxel3D> al) {
        voxels = al;
        init();
        value = (int) voxels.get(0).getValue();

        resXY = 1.0;
        resZ = 1.0;
        units = "pix";
    }

    /**
     * copy
     *
     * @param other
     */
    public Object3DVoxels(Object3DVoxels other) {
        voxels = new LinkedList<>();
        this.addVoxels(other.getVoxels());
        init();
        value = other.getValue();
        //this.labelImage = other.getLabelImage();
        //this.offX = other.offX;
        //this.offY = other.offY;
        //this.offZ = other.offZ;
        this.setCalibration(other.getResXY(), other.getResZ(), other.getUnits());
    }

    public Object3DVoxels(Object3D other) {
        voxels = new LinkedList<>();
        addVoxels(other.getVoxels());
        init();
        value = other.getValue();
        //this.labelImage = other.getLabelImage();
        //this.offX = other.offX;
        //this.offY = other.offY;
        //this.offZ = other.offZ;
        this.setCalibration(other.getResXY(), other.getResZ(), other.getUnits());
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

    private LinkedList<Voxel3D> createArrayList(ImageHandler ima, ImageHandler raw) {
        LinkedList<Voxel3D> voxelsTmp = new LinkedList<Voxel3D>();
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
        float rxy = (float) this.getResXY();
        float rz = (float) this.getResZ();
        int ix = Math.round(cx);
        int iy = Math.round(cy);
        int iz = Math.round(cz);
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
        voxels = createArrayList(ell.getImageHandler(), null);
        this.setValue(tmpval);
        this.translate(cx - ex, cy - ey, cz - ez);
        init();
    }

    // intersection
    public void addVoxelsIntersection(Object3D ob1, Object3D ob2) {
        LinkedList<Voxel3D> al1 = ob1.getVoxels();
        LinkedList<Voxel3D> al2 = ob2.getVoxels();
        double dist = 0.25;// normally int values (0.25=0.5²)
        Voxel3D v1, v2;
        for (Voxel3D anAl1 : al1) {
            v1 = anAl1;
            for (Voxel3D anAl2 : al2) {
                v2 = anAl2;
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
        LinkedList<Voxel3D> al1 = objs.get(0).getVoxels();
        LinkedList<Voxel3D> al2 = objs.get(1).getVoxels();
        double dist = 0.25;// normally int values (0.25=0.5²)
        Voxel3D v1, v2;
        for (Voxel3D anAl1 : al1) {
            v1 = anAl1;
            for (Voxel3D anAl2 : al2) {
                v2 = anAl2;
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
        LinkedList<Voxel3D> al1 = ob1.getVoxels();
        LinkedList<Voxel3D> al2 = ob2.getVoxels();
        Voxel3D v;
        for (Voxel3D anAl1 : al1) {
            v = anAl1;
            voxels.add(v);
        }
        for (Voxel3D anAl2 : al2) {
            v = anAl2;
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
        LinkedList<Voxel3D> al2 = other.getVoxels();
        LinkedList<Voxel3D> al11 = new LinkedList<Voxel3D>();
        //IJ.log("sub vox " + al11.size());
        double dist = 0.25;// normally int values (0.25=0.5²)
        //IJ.showStatus("Substracting " + other.getVolumePixels() + " voxels");
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
        ImageHandler seg = this.createSegImage(0, 0, 0, getXmax(), getYmax(), getZmax(), getValue());
        obj.draw(seg, 0);
        // reset voxels
        voxels = createArrayList(seg, null);
        init();
    }

    public void substractObject(Object3D other) {
        final int seuil_vol = 100;
        if ((getVolumePixels() > seuil_vol) || (other.getVolumePixels() > seuil_vol)) {
            substractObjectImage(other);
        } else {
            substractObjectVoxels(other);
        }
    }

    /**
     * @param vox
     */
    public final void addVoxels(LinkedList<Voxel3D> vox) {
        voxels.addAll(vox);
        init();
        contours = null;
    }

    //private void addNewVoxels(ArrayList vox) {
    //    this.addNewVoxels(vox);
    //}

    public void removeVoxels(int threshold) {
        ArrayList<Voxel3D> toRemove = new ArrayList<Voxel3D>();
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
        //ImageShort seg = (ImageShort) this.createSegImageMini(1, 1);
        ImageHandler seg = this.getLabelImage();
        // label the seg image
        ImageLabeller labeler = new ImageLabeller();
        return (labeler.getNbObjectsTotal(seg) == 1);
    }

    public ArrayList<Object3DVoxels> getConnexComponents() {
        ImageHandler seg = this.getLabelImage();
        ImageLabeller labeler = new ImageLabeller();
        ArrayList<Object3DVoxels> objs = labeler.getObjects(seg);
        for (Object3DVoxels O : objs) {
            O.translate(seg.offsetX, seg.offsetY, seg.offsetZ);
        }

        return objs;
    }

    public Object3DVoxels getInterior3DFill() {
        //ImageHandler seg = createSegImageMini(255, 1);
        ImageHandler seg = this.getLabelImage();
        ImageHandler fill = seg.duplicate();
        FillHoles3D.process(fill, value, 0, false);
        ImageFloat res = fill.subtractImage(seg);
        Object3DVoxels inte = new Object3DVoxels(res);
        inte.translate(seg.offsetX, seg.offsetY, seg.offsetZ);
        return inte;
    }

    /**
     * @param showStatus
     */
    public void setShowStatus(boolean showStatus) {
        this.showStatus = showStatus;
    }

    /**
     * @param ima to find the maximum value
     * @return the voxel with the max value
     */
    @Override
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
        for (Voxel3D voxel : voxels) {
            vox = voxel;
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
    @Override
    protected void computeMoments2(boolean normalize) {
        s200 = 0;
        s110 = 0;
        s101 = 0;
        s020 = 0;
        s011 = 0;
        s002 = 0;

        double i, j, k;
        Voxel3D vox;
        for (Voxel3D voxel : voxels) {
            vox = voxel;
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

    @Override
    public void computeMoments3() {
        s300 = s030 = s003 = 0;
        s210 = s201 = s120 = s021 = s102 = s012 = s111 = 0;
        double i, j, k;
        Voxel3D vox;
        for (Voxel3D voxel : voxels) {
            vox = voxel;
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

    @Override
    public void computeMoments4() {
        s004 = s400 = s040 = s220 = s202 = s022 = s121 = s112 = s211 = 0;
        s103 = s301 = s130 = s310 = s013 = s031 = 0;

        double i, j, k;
        Voxel3D vox;
        for (Voxel3D voxel : voxels) {
            vox = voxel;
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
    @Override
    protected void computeCenter() {
        bx = 0;
        by = 0;
        bz = 0;
        for (Voxel3D vox : voxels) {
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

    public void setContours(LinkedList<Voxel3D> contours, double areaUnit) {
        this.areaContactUnit = areaUnit;
        this.areaNbVoxels = contours.size();
        this.contours = contours;
        this.kdtreeContours = null; //kdTree computed if needed (getKdtreeContours())
    }

    /**
     * Compute the contours of the object rad=0.5
     */
    private void computeContours(ImageHandler segImage, int x0, int y0, int z0) {
        //IJ.log("computing contours for "+this+"  "+x0+" "+y0+" "+z0+" "+xmin+" "+ymin+" "+zmin);
        //segImage.show(""+this);
        // init kdtree
        kdtreeContours = new KDTreeC(3);
        kdtreeContours.setScale3(this.resXY, this.resXY, this.resZ);
        areaNbVoxels = 0;
        areaContactUnit = 0;
        areaContactVoxels = 0;
        contours = new LinkedList<>();
        boolean cont;
        double XZ = resXY * resZ;
        double XX = resXY * resXY;
        int pix0, pix1, pix2, pix3, pix4, pix5, pix6;
        int sx = segImage.sizeX;
        int sy = segImage.sizeY;
        int sz = segImage.sizeZ;

        int face;
        int class3or4;
        int class1 = 0, class2 = 0, class3 = 0, class4 = 0, class5 = 0, class6 = 0;

        int val = value;
        // special case value=0
        if (val == 0) {
            val = (int) segImage.getMinAboveValue(0);
        }

        // TODO parcourir seulement les objets de arraylist voxels et non la bounding box!
        // pb ??
//       for (Voxel3D vox : voxels) {
//            int i = vox.getRoundX() - x0;
//            int j = vox.getRoundY() - y0;
//            int k = vox.getRoundZ() - z0;
        for (int k = zmin - z0; k <= zmax - z0; k++) {
            if (showStatus) {
                // IJ.showStatus("Contours " + (100 * (k - zmin) / (zmax - zmin + 1)) + " % ");
            }
            for (int j = ymin - y0; j <= ymax - y0; j++) {
                for (int i = xmin - x0; i <= xmax - x0; i++) {
                    cont = false;
                    if (segImage.contains(i, j, k)) {
                        pix0 = (int)segImage.getPixel(i, j, k);
                        if (pix0 == val) {
                            face = 0;
                            class3or4 = 0;
                            if (i + 1 < sx) {
                                pix1 = (int)segImage.getPixel(i + 1, j, k);
                            } else {
                                pix1 = 0;
                            }
                            if (i - 1 >= 0) {
                                pix2 = (int)segImage.getPixel(i - 1, j, k);
                            } else {
                                pix2 = 0;
                            }
                            if (j + 1 < sy) {
                                pix3 = (int)segImage.getPixel(i, j + 1, k);
                            } else {
                                pix3 = 0;
                            }
                            if (j - 1 >= 0) {
                                pix4 = (int)segImage.getPixel(i, j - 1, k);
                            } else {
                                pix4 = 0;
                            }
                            if (k + 1 < sz) {
                                pix5 = (int)segImage.getPixel(i, j, k + 1);
                            } else {
                                pix5 = 0;
                            }
                            if (k - 1 >= 0) {
                                pix6 = (int)segImage.getPixel(i, j, k - 1);
                            } else {
                                pix6 = 0;
                            }
                            if (pix1 != val) {
                                cont = true;
                                areaContactUnit += XZ;
                                areaContactVoxels++;
                                face++;
                                if (pix2 != val) {
                                    class3or4 = 1;
                                }
                            }
                            if (pix2 != val) {
                                cont = true;
                                areaContactUnit += XZ;
                                areaContactVoxels++;
                                face++;
                            }
                            if (pix3 != val) {
                                cont = true;
                                areaContactUnit += XZ;
                                areaContactVoxels++;
                                face++;
                                if (pix4 != val) {
                                    class3or4 = 1;
                                }
                            }
                            if (pix4 != val) {
                                cont = true;
                                areaContactUnit += XZ;
                                areaContactVoxels++;
                                face++;
                            }
                            if (pix5 != val) {
                                cont = true;
                                areaContactUnit += XX;
                                areaContactVoxels++;
                                face++;
                                if (pix6 != val) {
                                    class3or4 = 1;
                                }
                            }
                            if (pix6 != val) {
                                cont = true;
                                areaContactUnit += XX;
                                areaContactVoxels++;
                                face++;
                            }
                            if (cont) {
                                areaNbVoxels++;
                                Voxel3D voxC = new Voxel3D(i + x0, j + y0, k + z0, val);
                                contours.add(voxC);
                                kdtreeContours.add(voxC.getArray(), voxC);
                                // METHOD LAURENT GOLE FROM Lindblad2005 TO COMPUTE SURFACE
                                // Surface area estimation of digitized 3D objects using weighted local configurations
                                if (face == 1) {
                                    class1++;
                                }
                                if (face == 2) {
                                    class2++;
                                }

                                if (face == 3 && class3or4 == 0) {
                                    class3++;
                                }

                                if (face == 3 && class3or4 == 1) {
                                    class4++;
                                }

                                if (face == 4) {
                                    class5++;
                                }

                                if (face == 5) {
                                    class6++;
                                }
                            }
                        }
                    }
                }
            }
        }
        //IJ.log("contours "+contours.size());
        // METHOD LAURENT GOLE FROM Lindblad2005 TO COMPUTE SURFACE
        // Surface area estimation of digitized 3D objects using weighted local configurations
        double w1 = 0.894, w2 = 1.3409, w3 = 1.5879, w4 = 2.0, w5 = 8.0 / 3.0, w6 = 10.0 / 3.0;
        correctedSurfaceArea = (class1 * w1 + class2 * w2 + class3 * w3 + class4 * w4 + class5 * w5 + class6 * w6);

        //if (areaNbVoxels == 0) {
        //new ImagePlus("MiniSeg 0", segImage.getImageStack()).show();
        //IJ.log(" " + x0 + " " + y0 + " " + z0 + " " + xmin + " " + ymin + " " + zmin);
        //}
    }

    /**
     * Compute the contours of the object rad=0.5
     */

    @Override
    public void computeContours() {
        ImageHandler label = this.getLabelImage();
        this.computeContours(label, label.offsetX, label.offsetY, label.offsetZ);
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
        for (Voxel3D voxel : voxels) {
            vox = voxel;
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


    public boolean hasOneVoxelValueRange(ImageHandler img, int t0, int t1) {
       for (Voxel3D vox : voxels) {
                float pix = img.getPixel(vox);
                if ((pix >= t0) && (pix <= t1)) return true;
            }

        return false;
    }

    protected LinkedList<Voxel3D> getVoxelInsideBoundingBox(int[] boundingBox) { //xmin, xmax, ymin, ymax, zmin, zmax
        LinkedList<Voxel3D> res = new LinkedList<Voxel3D>();
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
            //IJ.log("coloc disjoint box");
            //IJ.log("" + xmin + "-" + xmax + "  " + ymin + "-" + ymax + "  " + zmin + "-" + zmax);
            // IJ.log("" + obj.xmin + "-" + obj.xmax + "  " + obj.ymin + "-" + obj.ymax + "  " + obj.zmin + "-" + obj.zmax);
            return 0;
        }
        // if labels images for both objects, use them
        if ((labelImage != null) && (obj.getLabelImage() != null)) {
            //IJ.log("using coloc image with labels");
            //this.getLabelImage().duplicate().show("this coloc");
            //obj.getLabelImage().duplicate().show("other coloc");
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

        //IJ.log("coloc image " + this.getValue() + " " + obj.getValue());
//        getLabelImage().show("this label");
//        obj.getLabelImage().duplicate().show("obj label");
        // if no label images, create temporary one
        if ((this.getLabelImage() == null) || (obj.getLabelImage() == null)) {
            // IJ.log("coloc intersection image ");
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
        ImageHandler otherseg = obj.getLabelImage();
        ImageHandler label = this.getLabelImage();

//        int offX0 = labelImage.offsetX;
//        int offY0 = labelImage.offsetY;
//        int offZ0 = labelImage.offsetZ;
//        int offX1 = otherseg.offsetX;
//        int offY1 = otherseg.offsetY;
//        int offZ1 = otherseg.offsetZ;
        int offX0 = label.offsetX;
        int offY0 = label.offsetY;
        int offZ0 = label.offsetZ;
        int offX1 = otherseg.offsetX;
        int offY1 = otherseg.offsetY;
        int offZ1 = otherseg.offsetZ;

        //IJ.log("" + offX0 + " " + offY0 + " " + offZ0 + " " + offX1 + " " + offY1 + " " + offZ1);
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

        //IJ.log("" + xmin0 + "-" + xmax0 + " " + ymin0 + "-" + ymax0 + " " + zmin0 + "-" + zmax0);
        //labelImage.show("this_"+this);
        //otherseg.show("other_"+obj);
        for (int k = zmin0; k <= zmax0; k++) {
            for (int j = ymin0; j <= ymax0; j++) {
                for (int i = xmin0; i <= xmax0; i++) {
                    //if ((labelImage.getPixel(i, j, k) == value) && (otherseg.getPixel(i, j, k) == val)) {
                    int x = i - offX0;
                    int y = j - offY0;
                    int z = k - offZ0;
                    int xx = i - offX1;
                    int yy = j - offY1;
                    int zz = k - offZ1;
                    if ((label.contains(x, y, z)) && (otherseg.contains(xx, yy, zz))) {
                        if ((label.getPixel(x, y, z) == value) && (otherseg.getPixel(xx, yy, zz) == val)) {
                            count++;
                        }
                    }
                }
            }
        }

        return count;
    }

    private int getColocImageIntersection(Object3D other) {
        ImageHandler inter = this.createIntersectionImage(other, 1, 2);
        int count = 0;
        for (int z = 0; z < inter.sizeZ; z++)
            for (int i = 0; i < inter.sizeXY; i++) {
                if (inter.getPixel(i, z) == 3) {
                    count++;
                }
            }
        return count;
    }

    public int getColocVoxels(Object3D obj) {
        if (this.disjointBox(obj)) {
            return 0;
        }
        int[] intersec = this.getIntersectionBox(obj);
        LinkedList<Voxel3D> al1 = this.getVoxelInsideBoundingBox(intersec);
        if (!(obj instanceof Object3DVoxels)) {
            obj = obj.getObject3DVoxels();
        }
        LinkedList<Voxel3D> al2 = ((Object3DVoxels) obj).getVoxelInsideBoundingBox(intersec);

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

    //    private boolean hasOneVoxelColocImage(Object3D obj) {
//        if (this.disjointBox(obj)) {
//            return false;
//        }
//        if ((this.getLabelImage() == null) || (obj.getLabelImage() == null)) {
//            return false;
//        }
//        // taken from object3DLabel
//        int xmin0;
//        int ymin0;
//        int zmin0;
//        int xmax0;
//        int ymax0;
//        int zmax0;
//
//        int val = obj.getValue();
//        ImageInt otherseg = obj.getLabelImage();
//
//        xmin0 = getXmin();
//        ymin0 = getYmin();
//        zmin0 = getZmin();
//        xmax0 = getXmax();
//        ymax0 = getYmax();
//        zmax0 = getZmax();
//
//        xmin0 = Math.max(xmin0, obj.getXmin());
//        ymin0 = Math.max(ymin0, obj.getYmin());
//        zmin0 = Math.max(zmin0, obj.getZmin());
//        xmax0 = Math.min(xmax0, obj.getXmax());
//        ymax0 = Math.min(ymax0, obj.getYmax());
//        zmax0 = Math.min(zmax0, obj.getZmax());
//
//        //IJ.log(""+xmin0+"-"+xmax0+" "+ymin0+"-"+ymax0+" "+zmin0+"-"+zmax0+" "+otherseg);
//        //labelImage.show("this");
//        //otherseg.show("other");
//        int offX0 = labelImage.offsetX;
//        int offY0 = labelImage.offsetY;
//        int offZ0 = labelImage.offsetZ;
//        int offX1 = otherseg.offsetX;
//        int offY1 = otherseg.offsetY;
//        int offZ1 = otherseg.offsetZ;
//
//        for (int k = zmin0; k <= zmax0; k++) {
//            for (int j = ymin0; j <= ymax0; j++) {
//                for (int i = xmin0; i <= xmax0; i++) {
//                    if ((labelImage.getPixel(i - offX0, j - offY0, k - offZ0) == value) && (otherseg.getPixel(i - offX1, j - offY1, k - offZ1) == val)) {
//                        return true;
//                    }
//                }
//            }
//        }
//
//        return false;
//    }
    private boolean hasOneVoxelColocVoxels(Object3D obj) {
        if (this.disjointBox(obj)) {
            return false;
        }
        int[] intersec = this.getIntersectionBox(obj);
        LinkedList<Voxel3D> al1 = this.getVoxelInsideBoundingBox(intersec);
        if (!(obj instanceof Object3DVoxels)) {
            obj = obj.getObject3DVoxels();
        }
        LinkedList<Voxel3D> al2 = ((Object3DVoxels) obj).getVoxelInsideBoundingBox(intersec);
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
                if (!obInter.overlapBox(ob3)) {
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
        for (Voxel3D voxel : voxels) {
            vox = voxel;
            int vx = vox.getRoundX();
            int vy = vox.getRoundY();
            int vz = vox.getRoundZ();
            if (obj.img.contains(vx, vy, vz)) {
                obj.createPixel(vx, vy, vz, col);
            }
        }
    }

    /**
     * @param z Description of the Parameter
     * @return Description of the Return Value
     * @deprecated use Object3D-IJUtils
     * Constructor for the createRoi object
     */
    @Override
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
     * @param mask The mask image to draw
     * @param z    The Z coordinate
     * @param col  The value to draw
     * @return
     * @deprecated use Object3D-IJUtils
     * Draw inside a particular Z
     */
    @Override
    public boolean draw(ByteProcessor mask, int z, int col) {
        boolean ok = false;
        Voxel3D vox;
        for (Voxel3D voxel : voxels) {
            vox = voxel;
            if (Math.abs(z - vox.getZ()) < 0.5) {
                mask.putPixel(vox.getRoundX(), vox.getRoundY(), col);
                ok = true;
            }
        }
        return ok;
    }

    /**
     * @param ima
     * @param col
     */
    public void drawContours(ObjectCreator3D ima, int col) {
        Voxel3D p2;
        for (Voxel3D contour : contours) {
            p2 = contour;
            ima.createPixel(p2.getRoundX(), p2.getRoundY(), p2.getRoundZ(), col);
        }
    }

    public void drawContoursXY(ObjectCreator3D ima, int z, int col) {
        Voxel3D p2;
        ImageHandler seg = getMaxLabelImage(1);
        for (Voxel3D contour : contours) {
            p2 = contour;
            if (Math.abs(p2.getZ() - z) < 0.5) {
                ArrayUtil arrayUtil = seg.getNeighborhoodXY3x3(p2.getRoundX(), p2.getRoundY(), p2.getRoundZ());
                if (arrayUtil.hasValue(0))
                    ima.createPixel(p2.getRoundX(), p2.getRoundY(), p2.getRoundZ(), col);
            }
        }
    }

    /**
     * @deprecated use Object3D-IJUtils
     */
    public void draw(ImageStack mask, int col) {
        Voxel3D vox;
        for (Voxel3D voxel : voxels) {
            vox = voxel;

            mask.setVoxel(vox.getRoundX(), vox.getRoundY(), vox.getRoundZ(), col);
        }
    }


    /**
     * @deprecated use Object3D-IJUtils
     */
    @Override
    public void draw(ImageStack mask, int r, int g, int b) {
        Voxel3D vox;
        ImageProcessor tmp;
        Color col = new Color(r, g, b);
        for (Voxel3D voxel : voxels) {
            vox = voxel;
            tmp = mask.getProcessor((int) (vox.getZ() + 1));
            tmp.setColor(col);
            tmp.drawPixel(vox.getRoundX(), vox.getRoundY());
        }
    }

    @Override
    public LinkedList<Voxel3D> getVoxels() {
        return voxels;
    }

    public void setVoxels(LinkedList<Voxel3D> al) {
        voxels = al;
        init();
    }

    public Voxel3D getFirstVoxel() {
        return voxels.get(0);
    }

    /**
     * Get a random voxel in the object
     *
     * @param ra Random Object
     * @return next random voxel in the object
     */
    public Voxel3D getRandomVoxel(Random ra) {
        if (isEmpty()) {
            return null;
        }
        return voxels.get(ra.nextInt(getVolumePixels()));
    }

    /**
     * @param ra
     * @return
     * @link Object3DVoxels.getRandomVoxel()
     */
    @Deprecated
    public Voxel3D getRandomvoxel(Random ra) {
        if (isEmpty()) {
            return null;
        }
        return voxels.get(ra.nextInt(getVolumePixels()));
    }

    // From Bribiesca 2008 Pattern Recognition
    // An easy measure of compactness for 2D and 3D shapes
    public double getDiscreteCompactness() {
        if (areaContactVoxels == -1) computeContours();
        double n = getVolumePixels();
        double tmp = Math.pow(n, 2.0 / 3.0);

        return ((n - areaContactVoxels / 6.0) / (n - tmp));
    }

    // METHOD LAURENT GOLE FROM Lindblad2005 TO COMPUTE SURFACE
    // Surface area estimation of digitized 3D objects using weighted local configurations
    public double getCompactnessCorrected() {
        double V = getVolumePixels();
        double S = correctedSurfaceArea;

        return (Math.PI * 36.0 * V * V) / (S * S * S);
    }

    // METHOD LAURENT GOLE FROM Lindblad2005 TO COMPUTE SURFACE
    // Surface area estimation of digitized 3D objects using weighted local configurations
    public double getSphericityCorrected() {
        return Math.pow(getCompactnessCorrected(), 1.0 / 3.0);
    }

    public double getAreaPixelsCorrected() {
        if (correctedSurfaceArea == -1) {
            computeContours();
        }
        return correctedSurfaceArea;
    }

    /**
     * @param path
     */
    @Override
    public void saveObject(String path) {
        Voxel3D pixel;
        BufferedWriter bf;
        int c = 0;

        try {
            bf = new BufferedWriter(new FileWriter(path + name + ".3droi"));
            saveInfo(bf);
            for (Voxel3D voxel : voxels) {
                c++;
                pixel = new Voxel3D(voxel);
                bf.write(c + "\t" + pixel.getX() + "\t" + pixel.getY() + "\t" + pixel.getZ() + "\t" + pixel.getValue() + "\n");
            }
            bf.close();
        } catch (IOException ex) {
            Logger.getLogger(Object3DVoxels.class.getName()).log(Level.SEVERE, null, ex);
        }
    }


    /**
     * @param path
     * @param name
     */
    public void loadObject(String path, String name) {
        BufferedReader bf;
        String data;
        String[] coord;
        double dx, dy, dz;
        int v;
        this.setName(name);
        voxels = new LinkedList<Voxel3D>();
        try {
            bf = new BufferedReader(new FileReader(path + name));
            data = loadInfo(bf);
            while (data != null) {
                coord = data.split("\t");
                dx = Double.parseDouble(coord[1]);
                dy = Double.parseDouble(coord[2]);
                dz = Double.parseDouble(coord[3]);
                v = (int) Double.parseDouble(coord[4]);
                voxels.add(new Voxel3D(dx, dy, dz, v));
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
        // reinit seg image if any
        miniLabelImage = null;
        labelImage = null;
        // reinit kdtreecontours
        kdtreeContours = null;
    }

    @Override
    public void translate(Vector3D trans) {
        translate(trans.getX(), trans.getY(), trans.getZ());
    }

    public void rotate(Vector3D Axis, double angle) {
        GeomTransform3D trans = new GeomTransform3D();
        trans.setRotation(Axis, angle);

        for (Voxel3D v : voxels) {
            Vector3D tv = trans.getVectorTransformed(v.getVector3D(), this.getCenterAsVector());
            v.setVoxel(tv.getX(), tv.getY(), tv.getZ(), v.getValue());
        }
        init();
        // reinit seg image if any
        miniLabelImage = null;
        labelImage = null;
        // reinit kdtreecontours
        kdtreeContours = null;
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
            double pmin = Double.POSITIVE_INFINITY;
            double pmax = Double.NEGATIVE_INFINITY;

            double i, j, k;
            int nb = 0;
            for (Voxel3D vox: voxels) {
                i = vox.getX();
                j = vox.getY();
                k = vox.getZ();
                if (ima.contains(vox)) {
                    pix = ima.getPixel(vox);
                    if (!Double.isNaN(pix)) {
                        nb++;
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
            cx /= sum;
            cy /= sum;
            cz /= sum;

            integratedDensity = sum;
            meanDensity = integratedDensity / (double) nb;

            pixmin = pmin;
            pixmax = pmax;

            if (pmin == Double.POSITIVE_INFINITY) {
                pixmin = Double.NaN;
            }
            if (pmax == Double.NEGATIVE_INFINITY) {
                pixmax = Double.NaN;
            }

            // standard dev
            int vol = getVolumePixels();
            sigma = Math.sqrt((sum2 - ((sum * sum) / vol)) / (vol - 1));
        }
    }

    @Override
    protected void computeMassCenter(ImageHandler ima, ImageHandler mask) {
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
            for (Voxel3D vox : voxels) {
                if (ima.contains(vox) && mask.contains(vox) && (mask.getPixel(vox) > 0)) {
                    i = vox.getX();
                    j = vox.getY();
                    k = vox.getZ();

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
    public LinkedList<Voxel3D> listVoxels(ImageHandler ima, double threshold) {
        return listVoxels(ima, threshold, Double.POSITIVE_INFINITY);
    }

    @Override
    public LinkedList<Voxel3D> listVoxels(ImageHandler ima, double thresholdLow, double thresholdHigh) {
        LinkedList<Voxel3D> list = new LinkedList<Voxel3D>();
        Voxel3D voxel, newVoxel;

        Iterator<Voxel3D> it = voxels.iterator();
        float pixvalue;

        while (it.hasNext()) {
            voxel = it.next();
            if (ima.contains(voxel.getX(), voxel.getY(), voxel.getZ())) {
                pixvalue = ima.getPixel(voxel);
                if ((pixvalue > thresholdLow) && (pixvalue < thresholdHigh)) {
                    newVoxel = new Voxel3D(voxel);
                    newVoxel.setValue(pixvalue);
                    list.add(newVoxel);
                }
            }
        }

        return list;
    }

    // list voxels when object translated to new position for center
    public ArrayUtil listVoxels(ImageHandler ima, int newCenterX, int newCenterY, int newCenterZ) {
        ArrayUtil list = new ArrayUtil(this.getVolumePixels());
        Voxel3D voxel;

        Iterator<Voxel3D> it = voxels.iterator();
        float pixvalue;
        double tx = newCenterX - bx;
        double ty = newCenterY - by;
        double tz = newCenterZ - bz;
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
        for (Voxel3D voxel : voxels) {
            vox = voxel;
            int x = vox.getRoundX();
            int y = vox.getRoundY();
            int z = vox.getRoundZ();
            if (mask.contains(x, y, z)) {
                mask.setPixel(x, y, z, col);
            }
        }
    }

    @Override
    public void draw(ImageHandler mask, float val) {
        Voxel3D vox;
        for (Voxel3D voxel : voxels) {
            vox = voxel;
            int x = vox.getRoundX();
            int y = vox.getRoundY();
            int z = vox.getRoundZ();
            if (mask.contains(x, y, z)) {
                mask.setPixel(x, y, z, val);
            }
        }
    }


    @Override
    public void draw(ImageHandler mask, int col, int tx, int ty, int tz) {
        Voxel3D vox;
        for (Voxel3D voxel : voxels) {
            vox = voxel;
            int x = vox.getRoundX() + tx;
            int y = vox.getRoundY() + ty;
            int z = vox.getRoundZ() + tz;
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

    @Override
    public ArrayUtil listValues(ImageHandler ima, float thresh) {
        //ArrayUtil list = new ArrayUtil(this.getVolumePixels());

        ArrayList<Double> list = new ArrayList<>();

        for (Voxel3D voxel : voxels) {
            if (ima.contains(voxel.getRoundX(), voxel.getRoundY(), voxel.getRoundZ())) {
                float pixel = ima.getPixel(voxel.getRoundX(), voxel.getRoundY(), voxel.getRoundZ());
                if (pixel > thresh) {
                    list.add((double) pixel);
                }
            }
        }

        return new ArrayUtil(list);
    }

    public float[] getValueArray(ImageHandler im) {
        float[] res = new float[getVolumePixels()];
        int i = 0;
        if (im instanceof ImageFloat) {
            ImageFloat imf = (ImageFloat) im;
            for (Voxel3D v : getVoxels()) {
                res[i++] = imf.pixels[v.getRoundZ()][v.getXYCoord(imf.sizeX)];
            }
        } else if (im instanceof ImageShort) {
            ImageShort imf = (ImageShort) im;
            for (Voxel3D v : getVoxels()) {
                res[i++] = imf.pixels[v.getRoundZ()][v.getXYCoord(imf.sizeX)] & 0xffff;
            }
        } else if (im instanceof ImageByte) {
            ImageByte imf = (ImageByte) im;
            for (Voxel3D v : getVoxels()) {
                res[i++] = imf.pixels[v.getRoundZ()][v.getXYCoord(imf.sizeX)] & 0xff;
            }
        } else {
            for (Voxel3D v : getVoxels()) {
                res[i++] = im.getPixel(v);
            }
        }
        return res;
    }

    public Object3DVoxels dilate(float dilateSize, ImageInt mask, int nbCPUs) {
        // use getdilatedObject
        Object3DVoxels dilated = this.getDilatedObject(dilateSize, dilateSize, dilateSize);
        ImageHandler seg = dilated.getLabelImage();

//        ImageInt oldMiniLabelImage = this.miniLabelImage;
//        ImageInt seg = this.createSegImageMini(1, 0);
//        float dilateSizeZ = (float) (dilateSize * this.resXY / this.resZ);
//        ImageInt dil = BinaryMorpho.binaryDilate(seg, dilateSize, dilateSizeZ, true, type);
//        //dil.show("Dilate");
        ImageHandler label = this.getLabelImage();
        LinkedList<Voxel3D> vox = new LinkedList<Voxel3D>();
        int xx, yy, zz;
        for (int z = 0; z < seg.sizeZ; z++) {
            for (int y = 0; y < seg.sizeY; y++) {
                for (int x = 0; x < seg.sizeX; x++) {
                    if (seg.getPixel(x, y, z) != 0) {
                        xx = x + label.offsetX;
                        yy = y + label.offsetY;
                        zz = z + label.offsetZ;
                        if (mask == null || (mask.contains(xx, yy, zz) && (mask.getPixel(xx, yy, zz) == 0 || mask.getPixel(xx, yy, zz) == this.value))) {
                            vox.add(new Voxel3D(xx, yy, zz, value));
                        }
                    }
                }
            }
        }
//        this.miniLabelImage = oldMiniLabelImage;
        Object3DVoxels res = new Object3DVoxels(vox);
        res.setResXY(resXY);
        res.setResZ(resZ);
        res.setUnits(units);
        //res.setLabelImage(dil);
        return res;
    }

    public double getPixMedianValue(ImageHandler img) {
        ArrayUtil tab = this.listValues(img);
        return tab.median();
    }


}
