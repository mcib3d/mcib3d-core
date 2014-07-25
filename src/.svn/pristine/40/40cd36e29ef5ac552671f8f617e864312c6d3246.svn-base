package mcib3d.image3d;

import ij.IJ;
import mcib3d.image3d.legacy.IntImage3D;
import ij.ImagePlus;
import ij.ImageStack;
import ij.measure.Calibration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicInteger;
import mcib3d.geom.*;
import mcib3d.image3d.distanceMap3d.EDT;
import mcib3d.image3d.processing.FastFilters3D;
import mcib3d.utils.ArrayUtil;
import mcib3d.utils.ThreadRunner;
import mcib3d.utils.ThreadUtil;
import mcib3d.utils.exceptionPrinter;

/**
 *
 **
 * /**
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
public abstract class ImageInt extends ImageHandler {

    public ImageInt(ImagePlus img) {
        super(img);
    }

    public ImageInt(ImageStack img) {
        super(img);
    }

    public ImageInt(String title, int sizeX, int sizeY, int sizeZ) {
        super(title, sizeX, sizeY, sizeZ);
    }

    public ImageInt(ImageHandler im, boolean scaling) {
        super(im.title, im.sizeX, im.sizeY, im.sizeZ, im.offsetX, im.offsetY, im.offsetZ);
    }

    protected ImageInt(String title, int sizeX, int sizeY, int sizeZ, int offsetX, int offsetY, int offsetZ) {
        super(title, sizeX, sizeY, sizeZ, offsetX, offsetY, offsetZ);
    }

    public abstract void setPixel(int x, int y, int z, int value);

    public abstract void setPixel(int xy, int z, int value);

    public abstract void draw(Object3D o, int value);

    public abstract int getPixelInt(int xy, int z);

    public abstract int getPixelInt(int x, int y, int z);

    public abstract int getPixelInt(int coord);

    @Override
    public abstract float getPixel(Point3D P);

    public abstract int getPixelInt(Point3D P);

    @Override
    public abstract float getPixelInterpolated(Point3D P);

    public abstract int getPixelIntInterpolated(Point3D P);

    public TreeMap<Integer, int[]> getBounds(boolean addBorder) { //xmin, xmax, ymin, ymax, zmin, zmax, nbvox
        TreeMap<Integer, int[]> bounds = new TreeMap<Integer, int[]>();
        for (int z = 0; z < sizeZ; z++) {
            for (int y = 0; y < sizeY; y++) {
                for (int x = 0; x < sizeX; x++) {
                    int value = getPixelInt(x + y * sizeX, z);
                    if (value != 0) {
                        int[] bds = bounds.get(value);
                        if (bds != null) {
                            if (x < bds[0]) {
                                bds[0] = x;
                            } else if (x > bds[1]) {
                                bds[1] = x;
                            }
                            if (y < bds[2]) {
                                bds[2] = y;
                            } else if (y > bds[3]) {
                                bds[3] = y;
                            }
                            if (z < bds[4]) {
                                bds[4] = z;
                            } else if (z > bds[5]) {
                                bds[5] = z;
                            }
                            bds[6]++;
                        } else {
                            int[] nb = {x, x, y, y, z, z, 1};
                            bounds.put(value, nb);
                        }
                    }
                }
            }
        }
        if (addBorder) {
            for (int[] bds : bounds.values()) {
                bds[0]--;
                bds[1]++;
                bds[2]--;
                bds[3]++;
                bds[4]--;
                bds[5]++;
            }
        }
        return bounds;
    }

    public ArrayList<Integer> getUniqueValues(int th) {
        boolean[] vals = new boolean[(int) (getMax() + 1)];
        Arrays.fill(vals, false);
        ArrayList<Integer> list = new ArrayList();
        for (int c = 0; c < sizeXYZ; c++) {
            int pix = getPixelInt(c);
            if ((pix > th) && (!vals[pix])) {
                vals[pix] = true;
                list.add(pix);
            }
        }
        return list;
    }

    public ArrayList<Integer> getUniqueValues() {
        return getUniqueValues(-1);
    }

    @Override
    public abstract ImageInt crop3D(String title, int x_min, int x_max, int y_min, int y_max, int z_min, int z_max);

    public abstract ImageByte crop3DBinary(String title, int label, int x_min, int x_max, int y_min, int y_max, int z_min, int z_max);

    public abstract boolean shiftIndexes(TreeMap<Integer, int[]> bounds);

    @Override
    public abstract ImageInt[] crop3D(TreeMap<Integer, int[]> bounds);

    @Override
    public abstract ImageInt crop3DMask(String title, ImageInt mask, int label, int x_min_, int x_max_, int y_min_, int y_max_, int z_min_, int z_max_);

    public ImageByte[] crop3DBinary(TreeMap<Integer, int[]> bounds) {
        ImageByte[] ihs = new ImageByte[bounds.size()];
        ArrayList<Integer> keys = new ArrayList<Integer>(bounds.keySet());
        for (int idx = 0; idx < ihs.length; idx++) {
            int label = keys.get(idx);
            int[] bds = bounds.get(label);
            ihs[idx] = this.crop3DBinary(title + ":" + label, label, bds[0], bds[1], bds[2], bds[3], bds[4], bds[5]);
        }
        return ihs;
    }

    public ImageByte[] crop3DBinary() {
        TreeMap<Integer, int[]> bounds = this.getBounds(false);
        return crop3DBinary(bounds);
    }

    @Override
    public ImageInt cropRadius(int xc, int yc, int zc, int rx, int ry, int rz, boolean mean, boolean sphere) {
        int x0 = Math.max(0, xc - rx);
        int y0 = Math.max(0, yc - ry);
        int z0 = Math.max(0, zc - rz);
        int x1 = Math.min(sizeX, xc + rx);
        int y1 = Math.min(sizeY, yc + ry);
        int z1 = Math.min(sizeZ, zc + rz);
        ImageInt res;
        double r;
        double rx2 = rx * rx;
        double ry2 = ry * ry;
        double rz2 = rz * rz;
        int moy = 0;
        if (mean) {
            ImageStats s = this.getImageStats(null);
            moy = (int) s.getMean();
        }

        if (this instanceof ImageByte) {
            res = new ImageByte("crop" + title, x1 - x0 + 1, y1 - y0 + 1, z1 - z0 + 1);
        } else {
            res = new ImageShort("crop" + title, x1 - x0 + 1, y1 - y0 + 1, z1 - z0 + 1);
        }
        for (int z = zc - rz; z <= z1; z++) {
            for (int x = xc - rx; x <= x1; x++) {
                for (int y = yc - ry; y <= y1; y++) {
                    if (sphere) {
                        r = (x - xc) * (x - xc) / rx2 + (y - yc) * (y - yc) / ry2 + (z - zc) * (z - zc) / rz2;
                        if (r <= 1) {
                            res.setPixel(x - xc + rx, y - yc + ry, z - zc + rz, getPixelInt(x, y, z));
                        } else {
                            res.setPixel(x - xc + rx, y - yc + ry, z - zc + rz, moy);
                        }
                    } else {
                        res.setPixel(x - xc + rx, y - yc + ry, z - zc + rz, getPixelInt(x, y, z));
                    }
                }
            }
        }
        return res;
    }

    public Object3DFuzzy[] getObjects3D(ImageFloat probaMap, float contourProba) {
        try {
            Object3DFactory oc = new Object3DFactory(this);
            return oc.getFuzzyObjects(contourProba, probaMap);
        } catch (Exception e) {
            exceptionPrinter.print(e, "", false);
        }
        return null;
    }

    public Object3DVoxels[] getObjects3D() {
        try {
            Object3DFactory oc = new Object3DFactory(this);
            return oc.getObjects();
        } catch (Exception e) {
            exceptionPrinter.print(e, "", false);
        }
        return null;
    }

    public Objects3DPopulation getObjects3DPopulation() {
        // build new population
        return new Objects3DPopulation(this.getObjects3D(), this.getCalibration());
    }

    public Object3DVoxels getObject3DBackground(ImageInt mask) {
        ArrayList<Voxel3D> al = new ArrayList<Voxel3D>();
        for (int z = 0; z < sizeZ; z++) {
            for (int xy = 0; xy < sizeXY; xy++) {
                if (mask.getPixel(xy, z) != 0 && getPixel(xy, z) == 0) {
                    al.add(new Voxel3D(xy % sizeX, xy / sizeX, z, 0));
                }
            }
        }
        if (al.isEmpty()) {
            return new Object3DVoxels(0);
        } else {
            return new Object3DVoxels(al);
        }
    }

//    @Override
//    public ImageInt grayscaleOpen(final int radXY, final int radZ, ImageHandler res, ImageHandler temp, boolean multithread) {
//        ImageInt min = this.grayscaleFilter(radXY, radZ, FastFilters3D.MIN, temp, multithread);
//        ImageInt open = min.grayscaleFilter(radXY, radZ, FastFilters3D.MAX, res, multithread);
//        return open;
//    }
//
//    @Override
//    public ImageInt grayscaleClose(final int radXY, final int radZ, ImageHandler res, ImageHandler temp, boolean multithread) {
//        ImageInt max = this.grayscaleFilter(radXY, radZ, FastFilters3D.MAX, temp, multithread);
//        ImageInt close = max.grayscaleFilter(radXY, radZ, FastFilters3D.MIN, res, multithread);
//        return close;
//    }
//
//    @Override
//    public ImageInt grayscaleFilter(final int radXY, final int radZ, final int filter, ImageHandler resIH, boolean multithread) {
//        //final IntImage3D in = new IntImage3D(img.getImageStack());
//        final ImageInt in = ImageInt.wrap(img.getImageStack());
//        if (resIH == null) {
//            resIH = (ImageInt) ImageHandler.newBlankImageHandler(title + "::filtered", this);
//        }
//        final ImageInt res = ImageInt.wrap(resIH.img.getImageStack());
//        final ThreadRunner tr = new ThreadRunner(0, sizeZ, multithread ? 0 : 1);
//        for (int i = 0; i < tr.threads.length; i++) {
//            tr.threads[i] = new Thread(
//                    new Runnable() {
//
//                        public void run() {
//                            for (int idx = tr.ai.getAndIncrement(); idx < tr.end; idx = tr.ai.getAndIncrement()) {
//                                in.filterGeneric(res, radXY, radXY, radZ, idx, idx + 1, filter);
//                            }
//                        }
//                    });
//        }
//        tr.startAndJoin();
//        return (ImageInt) resIH;
//    }
    public ImageFloat getDistanceMapInsideMask(int nbCPUs) {
        return EDT.run(this, 0, false, nbCPUs);
    }

    public static ImageInt wrap(ImagePlus imp) {
        switch (imp.getBitDepth()) {
            case 8:
                return new ImageByte(imp);
            case 16:
                return new ImageShort(imp);
            case 32:
                return new ImageShort(ImageHandler.wrap(imp), true);
        }
        return null;
    }

    public static ImageInt wrap(ImageStack stack) {
        switch (stack.getBitDepth()) {
            case 8:
                return new ImageByte(stack);
            case 16:
                return new ImageShort(stack);
            case 32:
                return new ImageShort(ImageHandler.wrap(stack), true);
        }
        return null;
    }

    /**
     * Replace a pixel value by another
     *
     * @param val the value to be replaced
     * @param rep the new value
     */
    public void replacePixelsValue(int val, int rep) {
        for (int k = 0; k < sizeXYZ; k++) {
            if (this.getPixel(k) == val) {
                this.setPixel(k, rep);
            }
        }
        // reset stats
    }

    /**
     * Replace a pixel value by another
     *
     * @param val the value to be replaced
     * @param rep the new value
     */
    public void replacePixelsValue(int val1, int rep1, int val2, int rep2) {
        for (int k = 0; k < sizeXYZ; k++) {
            if (this.getPixel(k) == val1) {
                this.setPixel(k, rep1);
            } else if (this.getPixel(k) == val2) {
                this.setPixel(k, rep2);
            }
        }
    }

//    /**
//     * Replace a pixel value by another
//     *
//     * @param val the value to be replaced
//     * @param rep the new value
//     */
//    public void replacePixelsValue(int val1, int rep1, int val2, int rep2, int val3, int rep3) {
//        for (int k = 0; k < sizeXYZ; k++) {
//            if (this.getPixel(k) == val1) {
//                this.setPixel(k, rep1);
//            } else if (this.getPixel(k) == val2) {
//                this.setPixel(k, rep2);
//            } else if (this.getPixel(k) == val3) {
//                this.setPixel(k, rep3);
//            }
//        }
//    }

    /**
     * Replace pixel values by others
     *
     * @param val the values to be replaced
     * @param rep the new values
     */
    public void replacePixelsValue(int[] values, int[] replace) {
        for (int k = 0; k < sizeXYZ; k++) {
            int pix = getPixelInt(k);
            for (int i = 0; i < values.length; i++) {
                if (pix == values[i]) {
                    setPixel(k, replace[i]);
                    break;
                }
            }
        }
    }
    
    /**
     * Replace a pixel values by another
     *
     * @param val the values to be replaced
     * @param rep the new value
     */
    public void replacePixelsValue(int[] values, int replace) {
        for (int k = 0; k < sizeXYZ; k++) {
            int pix = getPixelInt(k);
            for (int i = 0; i < values.length; i++) {
                if (pix == values[i]) {
                    setPixel(k, replace);
                    break;
                }
            }
        }
    }
    

    /**
     *
     * @param background
     * @return
     */
    public boolean isBinary(int background) {
        float mi1 = this.getMinAboveValue(background);
        float mi2 = this.getMinAboveValue(mi1);
        if (mi2 == Float.MAX_VALUE) {
            return true;
        } else {
            return false;
        }
    }

    @Override
    public abstract ImageInt resample(int newX, int newY, int newZ, int method);

    @Override
    public abstract ImageInt resample(int newZ, int method);
   
    public abstract ImageByte toMask();
    
    public ImageByte toCenterMask() {
        ImageByte res = new ImageByte("mask", this.sizeX, this.sizeY, this.sizeZ);
        res.setScale(this);
        res.setOffset(this);
        Object3DVoxels[] os = this.getObjects3D();
        for (Object3DVoxels o:os) res.setPixel(o.getCenterAsPoint(), 255);
        return res;
    }
    
    public abstract int countMaskVolume();

    public ImageInt addImage(ImageInt other) {
        ImageInt res;
        if (!this.sameDimentions(other)) {
            return null;
        }
        // both images are byte then return byte
        if ((this instanceof ImageByte) && (other instanceof ImageByte)) {
            res = (ImageInt) this.createSameDimensions();
        } else {
            res = new ImageShort("add", sizeX, sizeY, sizeZ);
        }

        for (int i = 0; i < sizeXYZ; i++) {
            res.setPixel(i, this.getPixelInt(i) + other.getPixelInt(i));
        }

        return res;
    }
    
    public ImageInt diffAbsImage(ImageHandler other) {
        ImageInt res;
        if (!this.sameDimentions(other)) {
            return null;
        }
        // both images are byte then return byte
        if ((this instanceof ImageByte) && (other instanceof ImageByte)) {
            res = (ImageInt) this.createSameDimensions();
        } else {
            res = new ImageShort("diff", sizeX, sizeY, sizeZ);
        }

        for (int i = 0; i < sizeXYZ; i++) {
            res.setPixel(i, Math.abs(this.getPixel(i) - other.getPixel(i)));
        }

        return res;
    }
    

    @Override
    public abstract ImageInt duplicate();

    public ImageInt substractImage(ImageInt other) {
        ImageInt res;
        if (!this.sameDimentions(other)) {
            return null;
        }
        // both images are byte then return byte
        if ((this instanceof ImageByte) && (other instanceof ImageByte)) {
            res = (ImageInt) this.createSameDimensions();
        } else {
            res = new ImageShort("add", sizeX, sizeY, sizeZ);
        }

        for (int i = 0; i < sizeXYZ; i++) {
            res.setPixel(i, this.getPixelInt(i) - other.getPixelInt(i));
        }

        return res;
    }

    /**
     *
     * @param mask reference mask
     * @return negative of this image: 255 if inside reference mask and outside
     * this image, 0 otherwise.
     */
    public ImageInt invertMask(ImageInt mask) {
        ImageByte res = new ImageByte("mask", mask.sizeX, mask.sizeY, mask.sizeZ);
        byte value = (byte) 255;
        for (int z = 0; z < res.sizeZ; z++) {
            for (int xy = 0; xy < res.sizeXY; xy++) {
                if (mask.getPixelInt(xy, z) != 0 && getPixelInt(xy, z) == 0) {
                    res.setPixel(xy, z, value);
                }
            }
        }
        return res;
    }

    public boolean hasOneValueInt(int f) {
        for (int i = 0; i < sizeXYZ; i++) {
            if (getPixelInt(i) == f) {
                return true;
            }
        }
        return false;
    }

    public Voxel3D firstVoxelValueInt(int f) {
        for (int k = 0; k < sizeZ; k++) {
            for (int j = 0; j < sizeY; j++) {
                for (int i = 0; i < sizeX; i++) {
                    if (getPixelInt(i, j, k) == f) {
                        return new Voxel3D(i, j, k, f);
                    }
                }
            }
        }
        return null;
    }

    /**
     * 3D filter using threads
     *
     * @param out
     * @param radx Radius of mean filter in x
     * @param rady Radius of mean filter in y
     * @param radz Radius of mean filter in z
     * @param zmin
     * @param zmax
     * @param filter
     */
    public void filterGeneric(ImageInt out, float radx, float rady, float radz, int zmin, int zmax, int filter) {
        int[] ker = FastFilters3D.createKernelEllipsoid(radx, rady, radz);
        int nb = FastFilters3D.getNbFromKernel(ker);
        if (zmin < 0) {
            zmin = 0;
        }
        if (zmax > this.sizeZ) {
            zmax = this.sizeZ;
        }
        int value;
        ArrayUtil tab;
        for (int k = zmin; k < zmax; k++) {
            IJ.showStatus("3D filter : " + (k + 1) + "/" + zmax);
            for (int j = 0; j < sizeY; j++) {
                for (int i = 0; i < sizeX; i++) {
                    tab = this.getNeighborhoodKernel(ker, nb, i, j, k, radx, rady, radz);
                    if (filter == FastFilters3D.MEAN) {
                        out.setPixel(i, j, k, (int) (tab.getMean() + 0.5));
                    } else if (filter == FastFilters3D.MEDIAN) {
                        out.setPixel(i, j, k, (int) tab.medianSort());
                    }
                    if (filter == FastFilters3D.MIN) {
                        out.setPixel(i, j, k, (int) tab.getMinimum());
                    }
                    if (filter == FastFilters3D.MAX) {
                        out.setPixel(i, j, k, (int) tab.getMaximum());
                    }
                    if (filter == FastFilters3D.VARIANCE) {
                        out.setPixel(i, j, k, (int) (tab.getVariance2() + 0.5));
                    }
                    if (filter == FastFilters3D.MAXLOCAL) {
                        value = this.getPixelInt(i, j, k);
                        if (tab.isMaximum(value)) {
                            out.setPixel(i, j, k, value);
                        } else {
                            out.setPixel(i, j, k, 0);
                        }
                    }
                }
            }
        }
    }

    public void filterGeneric(ImageInt out, Object3DVoxels obj, int zmin, int zmax, int filter) {
        if (zmin < 0) {
            zmin = 0;
        }
        if (zmax > this.sizeZ) {
            zmax = this.sizeZ;
        }
        int value;
        // convert the object to neighborhood
        int[] ker = FastFilters3D.createKernelFromObject(obj);
        int nb = FastFilters3D.getNbFromKernel(ker);
        float[] rad = FastFilters3D.getRadiiFromObject(obj);
        ArrayUtil tab;
        for (int k = zmin; k < zmax; k++) {
            IJ.showStatus("3D filter : " + (k + 1) + "/" + zmax);
            for (int j = 0; j < sizeY; j++) {
                for (int i = 0; i < sizeX; i++) {
                    tab = this.getNeighborhoodKernel(ker, nb, i, j, k, rad[0], rad[1], rad[2]);
                    //tab = ker.listVoxels(this, i, j, k);
                    if (filter == FastFilters3D.MEAN) {
                        out.setPixel(i, j, k, (int) (tab.getMean() + 0.5));
                    } else if (filter == FastFilters3D.MEDIAN) {
                        out.setPixel(i, j, k, (int) tab.medianSort());
                    }
                    if (filter == FastFilters3D.MIN) {
                        out.setPixel(i, j, k, (int) tab.getMinimum());
                    }
                    if (filter == FastFilters3D.MAX) {
                        out.setPixel(i, j, k, (int) tab.getMaximum());
                    }
                    if (filter == FastFilters3D.VARIANCE) {
                        out.setPixel(i, j, k, (int) (tab.getVariance2() + 0.5));
                    }
                    if (filter == FastFilters3D.MAXLOCAL) {
                        value = this.getPixelInt(i, j, k);
                        if (tab.isMaximum(value)) {
                            out.setPixel(i, j, k, value);
                        } else {
                            out.setPixel(i, j, k, 0);
                        }
                    }
                }
            }
        }
    }

    /**
     * Sobel-like filtering in 3D
     *
     * @return The 3D filtered image
     */
    public ImageInt sobelFilter() {
        ImageInt res = (ImageInt) this.createSameDimensions();
        ArrayUtil nei;
        double[] edgeX = {-1, 0, 1, -2, 0, 2, -1, 0, 1, -2, 0, 2, -4, 0, 4, -2, 0, 2, -1, 0, 1, -2, 0, 2, -1, 0, 1};
        double[] edgeY = {-1, -2, -1, 0, 0, 0, 1, 2, 1, -2, -4, -2, 0, 0, 0, 2, 4, 2, -1, -2, -1, 0, 0, 0, 1, 2, 1};
        double[] edgeZ = {-1, -2, -1, -2, -4, -2, -1, -2, -1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 2, 1, 2, 4, 2, 1, 2, 1};
        double ex;
        double ey;
        double ez;
        double edge;


        for (int k = 0; k < sizeZ; k++) {
            //if (this.showStatus) {
            //    IJ.showStatus("3D Sobel : " + (int) (100 * k / sizez) + "%");
            //}
            for (int j = 0; j < sizeY; j++) {
                for (int i = 0; i < sizeX; i++) {
                    nei = getNeighborhood3x3x3(i, j, k);
                    ex = nei.convolve(edgeX, 1.0f);
                    ey = nei.convolve(edgeY, 1.0f);
                    ez = nei.convolve(edgeZ, 1.0f);
                    edge = Math.sqrt(ex * ex + ey * ey + ez * ez);
                    if ((edge > 65535) && (getType() == ImagePlus.GRAY16)) {
                        edge = 65535;
                    }
                    if ((edge > 255) && (getType() == ImagePlus.GRAY8)) {
                        edge = 255;
                    }
                    res.setPixel(i, j, k, (int) edge);
                }
            }
        }
        return res;
    }

    /**
     * Adaptive Filter in 3D (expermimental) Take 7 neighborhood, compute mean
     * and std dev Assign mean of neighborhood with lowest std dev
     *
     * @param radx
     * @param radz
     * @param rady
     * @return 3D filtered image
     */
    public ImageInt adaptiveFilter(float radx, float rady, float radz, int nbcpus) {
        ImageInt adaptimg = (ImageInt) this.createSameDimensions();
        final ImageInt adaptimg2 = adaptimg;

        // create kernel
        final int[] ker = FastFilters3D.createKernelEllipsoid(radx, rady, radz);
        int nb = 0;
        for (int i = 0; i < ker.length; i++) {
            nb += ker[i];
        }
        final int nb2 = nb;
        final float radX2 = radx;
        final float radY2 = rady;
        final float radZ2 = radz;


        // PARALLEL (Thomas Boudier)
        final AtomicInteger ai = new AtomicInteger(0);
        Thread[] threads = ThreadUtil.createThreadArray(nbcpus);

        for (int ithread = 0; ithread < threads.length; ithread++) {
            threads[ithread] = new Thread() {
                @Override
                public void run() {

                    ArrayUtil[] tab = new ArrayUtil[7];
                    // displacement to take neighborhoods
                    int dep = 1;
                    double mes;
                    double mins;
                    double si;
                    double me;

                    for (int k = ai.getAndIncrement(); k < sizeZ; k = ai.getAndIncrement()) {
                        //if (showStatus) {
                        //    IJ.showStatus("3D Adaptive : " + (int) (100 * k / sizez) + "%");
                        // }
                        for (int j = 0; j < sizeY; j++) {
                            for (int i = 0; i < sizeX; i++) {
                                tab[0] = getNeighborhoodKernel(ker, nb2, i, j, k, radX2, radY2, radZ2);
                                tab[1] = getNeighborhoodKernel(ker, nb2, (int) (i + dep), j, k, radX2, radY2, radZ2);
                                tab[2] = getNeighborhoodKernel(ker, nb2, (int) (i - dep), j, k, radX2, radY2, radZ2);
                                tab[3] = getNeighborhoodKernel(ker, nb2, i, (int) (j + dep), k, radX2, radY2, radZ2);
                                tab[4] = getNeighborhoodKernel(ker, nb2, i, (int) (j - dep), k, radX2, radY2, radZ2);
                                tab[5] = getNeighborhoodKernel(ker, nb2, i, j, (int) (k + dep), radX2, radY2, radZ2);
                                tab[6] = getNeighborhoodKernel(ker, nb2, i, j, (int) (k - dep), radX2, radY2, radZ2);
                                mes = 0;
                                mins = Float.MAX_VALUE;
                                for (int c = 0; c < 7; c++) {
                                    //me = tab[c].median();
                                    me = tab[c].getMean();
                                    si = tab[c].getStdDev();
                                    if (si < mins) {
                                        mins = si;
                                        mes = me;
                                    }
                                }
                                adaptimg2.setPixel(i, j, k, (int) mes);
                            }
                        }
                    }
                }
            };
        }

        ThreadUtil.startAndJoin(threads);

        return adaptimg2;
    }
}
