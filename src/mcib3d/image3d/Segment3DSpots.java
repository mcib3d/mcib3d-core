/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mcib3d.image3d;

import mcib3d.image3d.regionGrowing.Watershed3D;
import mcib3d.image3d.legacy.IntImage3D;
import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.gui.Plot;
import ij.measure.CurveFitter;
import mcib3d.utils.ArrayUtil;
import java.util.ArrayList;
import java.util.Iterator;
import mcib3d.geom.Object3D;
import mcib3d.geom.Object3DVoxels;
import mcib3d.geom.Voxel3D;
import mcib3d.image3d.distanceMap3d.EDT;
import mcib3d.image3d.processing.FastFilters3D;
import mcib3d.utils.ThreadUtil;

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
 */
/**
 *
 * @author thomas
 */
public class Segment3DSpots {

    ArrayList<Object3D> segmentedObjects = null;
    ImageHandler rawImage; // raw image
    ImageHandler seedsImage; // positions of seeds
    ImageInt watershedImage = null; // watershed from seeds
    ImageHandler labelImage = null; // labelled image with objects
    int seedsThreshold = -1; // global threshold (min value from seeds)
    // methods of segmentation
    public final static int SEG_CLASSICAL = 1;
    public final static int SEG_MAX = 3;
    public final static int SEG_BLOCK = 4;
    int methodSeg = 1;
    // LOCAL THRESHOLD
    public final static int LOCAL_CONSTANT = 1;
    public final static int LOCAL_MEAN = 2;
    public final static int LOCAL_GAUSS = 3;
    int localValue = -1;
    int localMethod = 1; // local threshold (to find spots)
    float radius0, radius1, radius2; // local mean
    private double localWeight = 0.5;
    private int GAUSS_MAXR = 10;
    // normale pc 1.285=90 %
    private double GAUSS_PC = 1.285;
    // VOLUME
    private int volMax = Integer.MAX_VALUE;
    private int volMin = 2;
    // option watershed
    private boolean WATERSHED = false;
    private int noiseWatershed = 0;
    public boolean show = true;
    public boolean multithread = true;
    // special case more than 2^16 labels
    public boolean bigLabel = false;

    /**
     *
     * @param image raw image
     * @param seeds seeds image
     */
    public Segment3DSpots(ImageHandler image, ImageHandler seeds) {
        this.rawImage = image;
        this.seedsImage = seeds;
    }

    /**
     *
     * @return current local threshold
     */
    public int getLocalThreshold() {
        return localValue;
    }

    /**
     *
     * @param localThreshold
     */
    public void setLocalThreshold(int localThreshold) {
        this.localValue = localThreshold;
    }

    /**
     *
     * @return global threshold
     */
    public int getSeedsThreshold() {
        return seedsThreshold;
    }

    /**
     *
     * @param globalThreshold
     */
    public void setSeedsThreshold(int globalThreshold) {
        this.seedsThreshold = globalThreshold;
    }

    /**
     *
     * @return raw image
     */
    public ImageHandler getRawImage() {
        return rawImage;
    }

    /**
     *
     * @param image
     */
    public void setRawImage(ImageHandler image) {
        this.rawImage = image;
    }

    public void setLabelImage(ImageHandler image) {
        this.labelImage = image;
    }

    /**
     *
     * @return seeds image
     */
    public ImageHandler getSeeds() {
        return seedsImage;
    }

    /**
     *
     * @param seeds image
     */
    public void setSeeds(ImageInt seeds) {
        this.seedsImage = seeds;
    }

    /**
     *
     * @return
     */
    public ArrayList<Object3D> getObjects() {
        return segmentedObjects;
    }

    /**
     *
     * @return
     */
    public ImageHandler getLabelImage() {
        return labelImage;
    }

    /**
     *
     * @param method
     */
    public void setMethodSeg(int method) {
        this.methodSeg = method;
    }

    /**
     *
     * @param method
     */
    public void setMethodLocal(int method) {
        this.localMethod = method;
    }

    /**
     *
     * @param wa
     */
    public void setWatershed(boolean wa) {
        WATERSHED = wa;
        if (WATERSHED) {
            this.computeWatershed();
            //watershedImage.show("Watershed Spots");
        }
    }

    /**
     *
     * @param noiseWatershed
     */
    public void setNoiseWatershed(int noiseWatershed) {
        this.noiseWatershed = noiseWatershed;
    }

    /**
     *
     * @param r0
     * @param r1
     * @param r2
     */
    public void setRadiusLocalMean(float r0, float r1, float r2) {
        setRadiusLocalMean(r0, r1, r2, 0.5);
    }

    /**
     *
     * @param r0
     * @param r1
     * @param r2
     * @param we
     */
    public void setRadiusLocalMean(float r0, float r1, float r2, double we) {
        radius0 = r0;
        radius1 = r1;
        radius2 = r2;
        localWeight = we;
    }

    /**
     *
     * @param pc
     */
    public void setGaussPc(double pc) {
        GAUSS_PC = pc;
    }

    /**
     *
     * @param gm
     */
    public void setGaussMaxr(int gm) {
        this.GAUSS_MAXR = gm;
    }

    /**
     *
     * @return
     */
    public int getVolumeMax() {
        return volMax;
    }

    /**
     *
     * @param volMax
     */
    public void setVolumeMax(int volMax) {
        this.volMax = volMax;
    }

    /**
     *
     * @return
     */
    public int getVolumeMin() {
        return volMin;
    }

    /**
     *
     * @param volMin
     */
    public void setVolumeMin(int volMin) {
        this.volMin = volMin;
    }

    private void computeWatershed() {
        Watershed3D wat3D = new Watershed3D(rawImage, seedsImage, noiseWatershed, seedsThreshold);
        watershedImage = wat3D.getWatershedImage3D();
    }

    /**
     * Local mean for segmentation
     *
     * @param xc x coordinate of pixel
     * @param yc y coordinate of pixel
     * @param zc z coordinate of pixel
     * @return the local mean at this position
     */
    public double localMean(int xc, int yc, int zc) {
        double mspot, mback;
        int sx = rawImage.sizeX;
        int sy = rawImage.sizeY;
        int sz = rawImage.sizeZ;

        float rad0 = radius0;
        float rad1 = radius1;
        float rad2 = radius2;

        double weight = localWeight;

        if (!WATERSHED) {
            // no watershed image
            // central value
            ArrayUtil neigh = rawImage.getNeighborhood(xc, yc, zc, rad0, rad0, rad0);
            mspot = neigh.getMean();
            // background value
            neigh = rawImage.getNeighborhoodLayer(xc, yc, zc, rad1, rad2);
            mback = neigh.getMean();
        } else {
            // take into account watershed image
            double rad02 = rad0 * rad0;
            double rad12 = rad1 * rad1;
            double rad22 = rad2 * rad2;
            int zd, zf, yd, yf, xd, xf;
            int nbspot = 0;
            int nbback = 0;
            double sumspot = 0;
            double sumback = 0;
            double dist;
            float pix;
            int water;
            int waterc = watershedImage.getPixelInt(xc, yc, zc);

            // compute bounding
            zd = (int) (zc - rad2);
            if (zd < 0) {
                zd = 0;
            }
            zf = (int) (zc + rad2);
            if (zf >= sz) {
                zf = sz;
            }
            yd = (int) (yc - rad2);
            if (yd < 0) {
                yd = 0;
            }
            yf = (int) (yc + rad2);
            if (yf >= sy) {
                yf = sy;
            }
            xd = (int) (xc - rad2);
            if (xd < 0) {
                xd = 0;
            }
            xf = (int) (xc + rad2);
            if (xf >= sx) {
                xf = sx;
            }

            for (int z = zd; z < zf; z++) {
                for (int y = yd; y < yf; y++) {
                    for (int x = xd; x < xf; x++) {
                        dist = (x - xc) * (x - xc) + (y - yc) * (y - yc) + (z - zc) * (z - zc);
                        if (dist <= rad02) {
                            water = watershedImage.getPixelInt(x, y, z);
                            if (water == waterc) {
                                pix = rawImage.getPixel(x, y, z);
                                sumspot += pix;
                                nbspot++;
                            }
                        } else if ((dist >= rad12) && (dist <= rad22)) {
                            water = watershedImage.getPixelInt(x, y, z);
                            if (water == waterc) {
                                pix = rawImage.getPixel(x, y, z);
                                sumback += pix;
                                nbback++;
                            }
                        }
                    }
                }
            }
            // test nb minimum of pixels in spot and background
            if (nbspot > 1) {
                mspot = sumspot / nbspot;
            } else {
                return -1;
            }
            if (nbback > 1) {
                mback = sumback / nbback;
            } else {
                return -1;
            }
        }

        return (mspot * weight + (1.0 - weight) * mback);
    }

    /**
     *
     * @param x
     * @param y
     * @param z
     * @param plotb
     * @return
     */
    public double[] gaussianFit(int x, int y, int z, boolean plotb) {
        double[] gaussFit;
        double[] params;
        if (WATERSHED) {
            gaussFit = rawImage.radialDistribution(x, y, z, GAUSS_MAXR, watershedImage);
        } else {
            gaussFit = rawImage.radialDistribution(x, y, z, GAUSS_MAXR);
        }
        params = ArrayUtil.fitGaussian(gaussFit, 3, GAUSS_MAXR);
        // plot
        if (plotb) {
            double[] xx = new double[gaussFit.length];
            for (int i = 0; i < xx.length; i++) {
                xx[i] = i;
            }
            Plot plot = new Plot("Rad", "X", "Y", xx, gaussFit);
            plot.show();
        }

        return params;
    }

    private void createLabelImage() {
        if (!bigLabel) {
            labelImage = new ImageShort("Label", rawImage.sizeX, rawImage.sizeY, rawImage.sizeZ);
        } else {
            labelImage = new ImageFloat("Label", rawImage.sizeX, rawImage.sizeY, rawImage.sizeZ);
        }
    }

    /**
     *
     */
    public void segmentAll() {
        segmentedObjects = new ArrayList();
        ArrayList<Voxel3D> obj;
        Voxel3D vox;
        int o = 1;
        int localThreshold = localValue;
        if (labelImage == null) {
            this.createLabelImage();
        }
        // locate seeds
        for (int z = 0; z < seedsImage.sizeZ; z++) {
            if (show) {
                IJ.showStatus("Segmenting slice " + (z + 1));
            }
            for (int y = 0; y < seedsImage.sizeY; y++) {
                for (int x = 0; x < seedsImage.sizeX; x++) {
                    if (seedsImage.getPixel(x, y, z) > seedsThreshold) {
                        // LOCAL THRESHOLD
                        if (localMethod == LOCAL_MEAN) {
                            localThreshold = (int) localMean(x, y, z);
                        } else if (localMethod == LOCAL_GAUSS) {
                            double[] gauss;
                            gauss = this.gaussianFit(x, y, z, false);
                            if (gauss != null) {
                                double thresh = CurveFitter.f(CurveFitter.GAUSSIAN, gauss, GAUSS_PC * gauss[3]);
                                //IJ.log("gauss sigma : " + gauss[3] + " thresh=" + thresh);
                                localThreshold = (int) thresh;
                            }
                        }
                        if (localThreshold > 0) {
                            if (show) {
                                IJ.log("segmenting spot at : " + x + " " + y + " " + " " + z + " lc=" + localThreshold);
                            }
                        }
                        switch (methodSeg) {
                            case SEG_CLASSICAL:
                                obj = this.segmentSpotClassical(x, y, z, localThreshold, o);
                                break;
                            case SEG_MAX:
                                obj = this.segmentSpotMax(x, y, z, localThreshold, o);
                                break;
                            case SEG_BLOCK:
                                obj = this.segmentSpotBlock(x, y, z, localThreshold, o);
                                break;
                            default:
                                obj = this.segmentSpotClassical(x, y, z, localThreshold, o);
                                break;
                        }
                        if ((obj != null) && (obj.size() >= volMin) && (obj.size() <= volMax)) {
                            segmentedObjects.add(new Object3DVoxels(obj));
                            o++;
                        } else if (obj != null) {
                            // erase from label image
                            Iterator it = obj.iterator();
                            while (it.hasNext()) {
                                vox = (Voxel3D) it.next();
                                labelImage.setPixel(vox.getRoundX(), vox.getRoundY(), vox.getRoundZ(), 0);
                            }
                            if (show) {
                                IJ.log("object volume outside range : " + obj.size());
                            }
                        }
                    }
                }
            }
        }
    }

    public ArrayList<Voxel3D> segmentOneSpot(int x, int y, int z, int o) {
        int localThreshold = localValue;
        ArrayList<Voxel3D> obj;
        // LOCAL THRESHOLD
        if (localMethod == LOCAL_MEAN) {
            localThreshold = (int) localMean(x, y, z);
        } else if (localMethod == LOCAL_GAUSS) {
            double[] gauss;
            gauss = this.gaussianFit(x, y, z, false);
            if (gauss != null) {
                double thresh = CurveFitter.f(CurveFitter.GAUSSIAN, gauss, GAUSS_PC * gauss[3]);
                //IJ.log("gauss sigma : " + gauss[3] + " thresh=" + thresh);
                localThreshold = (int) thresh;
            }
        }
        if (localThreshold > 0) {
            if (show) {
                IJ.log("segmenting spot at : " + x + " " + y + " " + " " + z + " lc=" + localThreshold);
            }
        }
        switch (methodSeg) {
            case SEG_CLASSICAL:
                obj = this.segmentSpotClassical(x, y, z, localThreshold, o);
                break;
            case SEG_MAX:
                obj = this.segmentSpotMax(x, y, z, localThreshold, o);
                break;
            case SEG_BLOCK:
                obj = this.segmentSpotBlock(x, y, z, localThreshold, o);
                break;
            default:
                obj = this.segmentSpotClassical(x, y, z, localThreshold, o);
                break;
        }
        if ((obj != null) && (obj.size() >= volMin) && (obj.size() <= volMax)) {
            return obj;
        }

        return null;
    }

    /**
     *
     * @param xdep
     * @param ydep
     * @param zdep
     * @param lcThreshold
     * @param val
     * @return
     */
    private ArrayList<Voxel3D> segmentSpotTemplate(int xdep, int ydep, int zdep, int lcThreshold, int val) {
        boolean changement = true;
        int xfin = xdep + 1;
        int yfin = ydep + 1;
        int zfin = zdep + 1;
        int sens = 1;
        int value = val;
        if (labelImage == null) {
            this.createLabelImage();
        }
        // pixel already segmented ?
        if (labelImage.getPixel(xdep, ydep, zdep) > 0) {
            return null;
        }
        labelImage.setPixel(xdep, ydep, zdep, value);
        ArrayList<Voxel3D> object = new ArrayList();
        object.add(new Voxel3D(xdep, ydep, zdep, value));
        int volume = 1;
        int i;
        int j;
        int k;
        int l;
        int m;
        int n;

        ImageHandler original = rawImage;

        ArrayList<Voxel3D> neigh;
        Iterator it;
        Voxel3D tmpneigh;

        while (changement) {
            changement = false;
            for (k = sens == 1 ? zdep : zfin; ((sens == 1 && k <= zfin) || (sens == -1 && k >= zdep)); k += sens) {
                for (j = sens == 1 ? ydep : yfin; ((sens == 1 && j <= yfin) || (sens == -1 && j >= ydep)); j += sens) {
                    for (i = sens == 1 ? xdep : xfin; ((sens == 1 && i <= xfin) || (sens == -1 && i >= xdep)); i += sens) {
                        if (labelImage.getPixel(i, j, k) == value) {
                            // create neighbors list
                            neigh = new ArrayList();
                            for (n = k - 1; n < k + 2; n++) {
                                for (m = j - 1; m < j + 2; m++) {
                                    for (l = i - 1; l < i + 2; l++) {
                                        if (labelImage.getPixel(l, m, n) == 0) {
                                            neigh.add(new Voxel3D(l, m, n, original.getPixel(l, m, n)));
                                        }
                                    }
                                }
                            }
                            // analyse list
                            it = neigh.iterator();
                            while (it.hasNext()) {
                                tmpneigh = (Voxel3D) it.next();
                                // CLASSICAL
                                if (tmpneigh.getValue() >= lcThreshold) {
                                    l = tmpneigh.getRoundX();
                                    m = tmpneigh.getRoundY();
                                    n = tmpneigh.getRoundZ();
                                    labelImage.setPixel(l, m, n, value);
                                    object.add(new Voxel3D(l, m, n, value));
                                    volume++;
                                    if (volume > volMax) {
                                        if (show) {
                                            IJ.log("VOL :" + volume);
                                        }
                                        return null;
                                    }
                                    // update min-max
                                    if (l < xdep) {
                                        xdep--;
                                    }
                                    if (l > xfin) {
                                        xfin++;
                                    }
                                    if (m < ydep) {
                                        ydep--;
                                    }
                                    if (m > yfin) {
                                        yfin++;
                                    }
                                    if (n < zdep) {
                                        zdep--;
                                    }
                                    if (n > zfin) {
                                        zfin++;
                                    }
                                    changement = true;
                                }
                            }
                        }
                    }
                }
            }
            sens *= -1;
        }

        return object;
    }

    /**
     *
     * @param xdep
     * @param ydep
     * @param zdep
     * @param lcThreshold
     * @param val
     * @return
     */
    public ArrayList<Voxel3D> segmentSpotBlock(int xdep, int ydep, int zdep, int lcThreshold, int val) {
        boolean changement = true;
        int xfin = xdep + 1;
        int yfin = ydep + 1;
        int zfin = zdep + 1;
        int sens = 1;
        int value = val;
        if (labelImage == null) {
            this.createLabelImage();
        }
        // pixel already segmented ?
        if (labelImage.getPixel(xdep, ydep, zdep) > 0) {
            return null;
        }
        labelImage.setPixel(xdep, ydep, zdep, value);
        ArrayList<Voxel3D> object = new ArrayList();
        object.add(new Voxel3D(xdep, ydep, zdep, value));
        int volume = 1;
        int i;
        int j;
        int k;
        int l;
        int m;
        int n;
        int sx = rawImage.sizeX;
        int sy = rawImage.sizeY;
        int sz = rawImage.sizeZ;

        ImageHandler original = rawImage;

        ArrayList<Voxel3D> neigh;
        Iterator it;
        Voxel3D tmpneigh;
        float pixelCenter;
        int waterne = 0, water = 0;
        boolean ok;

        //int ite = 0;
        while (changement) {
            //IJ.log("Ite " + ite + " " + xdep + " " + xfin);
            //ite++;
            changement = false;
            for (k = sens == 1 ? zdep : zfin; ((sens == 1 && k <= zfin) || (sens == -1 && k >= zdep)); k += sens) {
                for (j = sens == 1 ? ydep : yfin; ((sens == 1 && j <= yfin) || (sens == -1 && j >= ydep)); j += sens) {
                    for (i = sens == 1 ? xdep : xfin; ((sens == 1 && i <= xfin) || (sens == -1 && i >= xdep)); i += sens) {
                        if (labelImage.contains(i, j, k) && labelImage.getPixel(i, j, k) == value) {
                            pixelCenter = original.getPixel(i, j, k);
                            if (WATERSHED) {
                                water = watershedImage.getPixelInt(i, j, k);
                            }
                            // create neighbors list
                            neigh = new ArrayList();
                            for (n = k - 1; n < k + 2; n++) {
                                for (m = j - 1; m < j + 2; m++) {
                                    for (l = i - 1; l < i + 2; l++) {
                                        if ((l >= 0) && (l < sx) && (m >= 0) && (m < sy) && (n >= 0) && (n < sz)) {
                                            if (WATERSHED) {
                                                waterne = watershedImage.getPixelInt(l, m, n);
                                            }
                                            if ((labelImage.getPixel(l, m, n) == 0) && (original.getPixel(l, m, n) >= lcThreshold) && (waterne == water)) {
                                                neigh.add(new Voxel3D(l, m, n, original.getPixel(l, m, n)));
                                            }
                                        }
                                    } //l
                                } // m
                            } //n

                            // analyse list                           
                            ok = true;
                            // empty
                            if (neigh.isEmpty()) {
                                ok = false;
                            }
                            // test 1 neighbor
                            if (neigh.size() == 1) {
                                ok = false;
                            }
                            // test all neighbors
                            it = neigh.iterator();
                            while (it.hasNext() && ok) {
                                tmpneigh = (Voxel3D) it.next();
                                // BLOCK
                                if (tmpneigh.getValue() > pixelCenter) {
                                    ok = false;
                                    break;
                                }
                            }

                            if (ok) {
                                changement = true;
                                it = neigh.iterator();
                                while (it.hasNext()) {
                                    tmpneigh = (Voxel3D) it.next();
                                    l = tmpneigh.getRoundX();
                                    m = tmpneigh.getRoundY();
                                    n = tmpneigh.getRoundZ();
                                    labelImage.setPixel(l, m, n, value);
                                    object.add(new Voxel3D(l, m, n, value));
                                    volume++;
                                    if (volume > volMax) {
                                        if (show) {
                                            IJ.log("VOL :" + volume);
                                        }
                                        return null;
                                    }
                                    // update min-max
                                    if (l < xdep) {
                                        xdep--;
                                    }
                                    if (l > xfin) {
                                        xfin++;
                                    }
                                    if (m < ydep) {
                                        ydep--;
                                    }
                                    if (m > yfin) {
                                        yfin++;
                                    }
                                    if (n < zdep) {
                                        zdep--;
                                    }
                                    if (n > zfin) {
                                        zfin++;
                                    }
                                }
                            } // BLOCKING
                            // else {
                            //     it = neigh.iterator();
                            //     while ((it != null) && (it.hasNext())) {
                            //         tmpneigh = (Voxel3D) it.next();
                            //         l = (int) tmpneigh.getX();
                            //         m = (int) tmpneigh.getY();
                            //         n = (int) tmpneigh.getZ();
                            // 0 do not change 1 exclude from future seg
                            //labelImage.putPixel(l, m, n, 0);
                            //    }
                            //}
                        } // labelimage==value
                    } //i
                } // j
            }// k
            sens *= -1;
        }//while      

        return object;
    }

    /**
     * Segment an object from a seed
     *
     * @param lcThreshold Threshold to connect pixels
     * @param val value of the object
     * @param xdep x coordinate of the seed
     * @param ydep y coordinate of the seed
     * @param zdep z coordinate of the seed
     * @return true if object cold be segmented
     */
    public ArrayList<Voxel3D> segmentSpotClassical(int xdep, int ydep, int zdep, int lcThreshold, int val) {
        boolean changement = true;
        int xfin = xdep + 1;
        int yfin = ydep + 1;
        int zfin = zdep + 1;
        int sens = 1;
        int value = val;
        if (labelImage == null) {
            this.createLabelImage();
        }
        // pixel already segmented ?
        if (labelImage.getPixel(xdep, ydep, zdep) > 0) {
            return null;
        }
        labelImage.setPixel(xdep, ydep, zdep, value);
        ArrayList<Voxel3D> object = new ArrayList();
        object.add(new Voxel3D(xdep, ydep, zdep, value));
        int volume = 1;
        int i;
        int j;
        int k;
        int l;
        int m;
        int n;

        ImageHandler original = rawImage;
        int waterCenter = 0;
        int water = 0;

        while (changement) {
            changement = false;
            for (k = sens == 1 ? zdep : zfin; ((sens == 1 && k <= zfin) || (sens == -1 && k >= zdep)); k += sens) {
                for (j = sens == 1 ? ydep : yfin; ((sens == 1 && j <= yfin) || (sens == -1 && j >= ydep)); j += sens) {
                    for (i = sens == 1 ? xdep : xfin; ((sens == 1 && i <= xfin) || (sens == -1 && i >= xdep)); i += sens) {
                        if (labelImage.contains(i, j, k) && labelImage.getPixel(i, j, k) == value) {
                            if (WATERSHED) {
                                waterCenter = watershedImage.getPixelInt(i, j, k);
                            }
                            for (n = k - 1; n < k + 2; n++) {
                                for (m = j - 1; m < j + 2; m++) {
                                    for (l = i - 1; l < i + 2; l++) {
                                        if (labelImage.contains(l, m, n)) {
                                            if (WATERSHED) {
                                                water = watershedImage.getPixelInt(l, m, n);
                                            }
                                            if ((labelImage.getPixel(l, m, n) == 0) && (original.getPixel(l, m, n) >= lcThreshold) && (water == waterCenter)) {
                                                labelImage.setPixel(l, m, n, value);
                                                // original.putPixel(l, m, n, 0);
                                                // add voxel to object
                                                object.add(new Voxel3D(l, m, n, value));
                                                volume++;
                                                if (volume > volMax) {
                                                    if (show) {
                                                        IJ.log("VOL TOO BIG STOP SEG :" + volume);
                                                    }
                                                    return null;
                                                }
                                                // update min-max
                                                if (l < xdep) {
                                                    xdep--;
                                                }
                                                if (l > xfin) {
                                                    xfin++;
                                                }
                                                if (m < ydep) {
                                                    ydep--;
                                                }
                                                if (m > yfin) {
                                                    yfin++;
                                                }
                                                if (n < zdep) {
                                                    zdep--;
                                                }
                                                if (n > zfin) {
                                                    zfin++;
                                                }
                                                changement = true;
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
            sens *= -1;
        }

        return object;
    }

    /**
     * Segment an object from a seed, keep only max local
     *
     * @param lcThreshold Threshold to connect pixels
     * @param val value of the object
     * @param xdep x coordinate of the seed
     * @param ydep y coordinate of the seed
     * @param zdep z coordinate of the seed
     * @return true if object cold be segmented
     */
    public ArrayList<Voxel3D> segmentSpotMax(int xdep, int ydep, int zdep, int lcThreshold, int val) {
        boolean changement = true;
        int xfin = xdep + 1;
        int yfin = ydep + 1;
        int zfin = zdep + 1;

        int sens = 1;

        int value = val;

        if (labelImage == null) {
            this.createLabelImage();
        }
        // pixel already segmented ?
        if (labelImage.getPixel(xdep, ydep, zdep) > 0) {
            return null;
        }
        labelImage.setPixel(xdep, ydep, zdep, value);
        //volume++;
        ArrayList<Voxel3D> object = new ArrayList();
        object.add(new Voxel3D(xdep, ydep, zdep, value));

        int i;
        int j;
        int k;
        int l;
        int m;
        int n;

        ImageHandler original = rawImage;
        float pixelCenter;
        int waterCenter = 0;
        int water = 0;

        while (changement) {
            changement = false;
            for (k = sens == 1 ? zdep : zfin; ((sens == 1 && k <= zfin) || (sens == -1 && k >= zdep)); k += sens) {
                for (j = sens == 1 ? ydep : yfin; ((sens == 1 && j <= yfin) || (sens == -1 && j >= ydep)); j += sens) {
                    for (i = sens == 1 ? xdep : xfin; ((sens == 1 && i <= xfin) || (sens == -1 && i >= xdep)); i += sens) {
                        if (labelImage.contains(i, j, k) && labelImage.getPixel(i, j, k) == value) {
                            if (WATERSHED) {
                                waterCenter = watershedImage.getPixelInt(i, j, k);
                            }
                            pixelCenter = original.getPixel(i, j, k);
                            for (n = k - 1; n < k + 2; n++) {
                                for (m = j - 1; m < j + 2; m++) {
                                    for (l = i - 1; l < i + 2; l++) {
                                        if (labelImage.contains(l, m, n)) {
                                            if (WATERSHED) {
                                                water = watershedImage.getPixelInt(l, m, n);
                                            }
                                            if ((labelImage.getPixel(l, m, n) == 0) && (original.getPixel(l, m, n) >= lcThreshold) && (original.getPixel(l, m, n) <= pixelCenter) && (water == waterCenter)) {
                                                labelImage.setPixel(l, m, n, value);
                                                //original.putPixel(l, m, n, 0);
                                                // add voxel to object
                                                object.add(new Voxel3D(l, m, n, value));
                                                // update min-max
                                                if (l < xdep) {
                                                    xdep--;
                                                }
                                                if (l > xfin) {
                                                    xfin++;
                                                }
                                                if (m < ydep) {
                                                    ydep--;
                                                }
                                                if (m > yfin) {
                                                    yfin++;
                                                }
                                                if (n < zdep) {
                                                    zdep--;
                                                }
                                                if (n > zfin) {
                                                    zfin++;
                                                }
                                                changement = true;
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
            sens *= -1;
        }

        return object;
    }
    // FIXME a revoir, notamment dist ??

    public static Object3DVoxels[] splitSpotWatershed(Object3D obj, float rad, float dist) {
        ImageInt seg = obj.createSegImage(0, 0, 0, obj.getXmax() + 1, obj.getYmax() + 1, obj.getZmax() + 1, 255);
        //seg.show();
        ImagePlus segplus = seg.getImagePlus();
        segplus.setCalibration(obj.getCalibration());
        // return
        Object3DVoxels res[] = null;
        try {
            int cpus = ThreadUtil.getNbCpus();
            // FIXME variable multithread
            ImageFloat edt3d = EDT.run(seg, 1f, false, cpus);
            //edt3d.showDuplicate("edt");

            ImageStack localMax = FastFilters3D.filterFloatImageStack(edt3d.getImageStack(), FastFilters3D.MAXLOCAL, rad, rad, rad, cpus, false);
            ImageFloat maxlocal3d = new ImageFloat(localMax);
            //new ImagePlus("max", maxlocal3d.getStack()).show();
            ArrayList<Voxel3D> locals = obj.listVoxels(maxlocal3d, 0);

            int nb = locals.size();
            // IJ.log("nb=" + nb);
            if (nb < 2) {
                return null;
            }
            // chercher deux max locaux les plus eloignes
            int i1 = 0;
            int i2 = 0;
            double dmax = 0.0;
            double d;
            for (int i = 0; i < nb; i++) {
                for (int j = i + 1; j < nb; j++) {
                    d = locals.get(i).distance(locals.get(j));
                    if (d > dmax) {
                        dmax = d;
                        i1 = i;
                        i2 = j;
                    }
                }
            }

            // Ranger les max locaux en deux classes et calculer barycentre des deux classes
            double cx1 = 0;
            double cy1 = 0;
            double cz1 = 0;
            double cx2 = 0;
            double cy2 = 0;
            double cz2 = 0;
            double d1;
            double d2;

            Voxel3D PP1 = new Voxel3D(locals.get(i1).getX(), locals.get(i1).getY(), locals.get(i1).getZ(), 1);
            Voxel3D PP2 = new Voxel3D(locals.get(i2).getX(), locals.get(i2).getY(), locals.get(i2).getZ(), 2);
            Voxel3D P1 = new Voxel3D(cx1, cy1, cz1, 0);
            Voxel3D P2 = new Voxel3D(cx2, cy2, cz2, 0);
            int nb1, nb2;

            int nbite = 0;
            while ((nb > 2) && ((P1.distance(PP1) > 1) || (P2.distance(PP2) > 1)) && (nbite < 100)) {
                nbite++;
                cx1 = 0;
                cy1 = 0;
                cx2 = 0;
                cy2 = 0;
                cz1 = 0;
                cz2 = 0;
                nb1 = 0;
                nb2 = 0;
                P1.setX(PP1.getX());
                P1.setY(PP1.getY());
                P1.setZ(PP1.getZ());
                P2.setX(PP2.getX());
                P2.setY(PP2.getY());
                P2.setZ(PP2.getZ());
                for (int i = 0; i < nb; i++) {
                    d1 = P1.distance(locals.get(i));
                    d2 = P2.distance(locals.get(i));
                    if (d1 < d2) {
                        cx1 += locals.get(i).getX();
                        cy1 += locals.get(i).getY();
                        cz1 += locals.get(i).getZ();
                        nb1++;
                    } else {
                        cx2 += locals.get(i).getX();
                        cy2 += locals.get(i).getY();
                        cz2 += locals.get(i).getZ();
                        nb2++;
                    }
                }
                cx1 /= nb1;
                cy1 /= nb1;
                cx2 /= nb2;
                cy2 /= nb2;
                cz1 /= nb1;
                cz2 /= nb2;

                PP1.setX(cx1);
                PP1.setY(cy1);
                PP1.setZ(cz1);
                PP2.setX(cx2);
                PP2.setY(cy2);
                PP2.setZ(cz2);
            }
            // check minimal distances
            double distPP = PP1.distance(PP2);
            IJ.log("Centers found PP1=" + PP1 + " PP2=" + PP2 + " distance " + distPP);
            if (distPP < dist) {
                return null;
            }
            ImageInt seeds = new ImageShort("seeds", seg.sizeX, seg.sizeY, seg.sizeZ);
            seeds.setPixel(PP1.getRoundX(), PP1.getRoundY(), PP1.getRoundZ(), 255);
            seeds.setPixel(PP2.getRoundX(), PP2.getRoundY(), PP2.getRoundZ(), 255);
            //seeds.show();
            Watershed3D wat = new Watershed3D(edt3d, seeds, 0, 0);
            ImageInt wat2 = wat.getWatershedImage3D();
            //wat2.show();
            // in watershed label starts at 2
            Object3DVoxels ob1 = new Object3DVoxels(wat2, 2);
            Object3DVoxels ob2 = new Object3DVoxels(wat2, 3);
            // translate objects if needed by miniseg
            //ob1.translate(seg.offsetX, seg.offsetY, seg.offsetZ);
            //new ImagePlus("wat", wat2.getStack()).show();
            res = new Object3DVoxels[2];
            res[0] = ob1;
            res[1] = ob2;
        } catch (Exception e) {
            IJ.log("Exception EDT " + e);
        }

        return res;
    }

    /**
     *
     * @param ori
     * @param val
     * @param f
     * @param new1
     * @param new2
     * @param dist
     * @param rad
     * @param dim
     * @return
     */
    public static ArrayList<Voxel3D>[] splitSpotProjection(IntImage3D ori, int val, Object3D f, int new1, int new2, double dist, float rad, int dim) {
        // only for voxels object
        if (!(f instanceof Object3DVoxels)) {
            return null;
        }

        boolean debug = false;
        int p1 = f.getZmin();
        int p2 = f.getZmax();
        IntImage3D proj;
        if (dim == 2) {
            proj = ori.projectionZ(-1, p1, p2);
        } else {
            proj = ori;
        }
        //proj = (IntImage3D) proj.medianFilter(1, 1, 0);
        float radz = (dim == 2) ? 0 : rad;
        IntImage3D maxi = (IntImage3D) proj.createLocalMaximaImage(rad, rad, radz, false);
        //IntImage3D seg = f.getSegImage();
        if (debug) {
            System.out.println("Separe2D " + val);
        }
        //IntImage3D seg2D = seg.projectionZ(val, p1, p2);
        double cx1;
        double cy1;
        double cz1;
        double nb1;
        double cx2;
        double cy2;
        double cz2;
        double nb2;
        double dist1;
        double dist2;
        int nb;

        if (debug) {
            System.out.println("separe spots 2D : " + val);
        }

        // compter nb de max locaux
        /*
         * for (int j = 2; j < maxi.getSizey() - 2; j++) { for (int i = 2; i <
         * maxi.getSizex() - 2; i++) { if ((maxi.getPixel(i, j, 0) > 0) &&
         * (seg2D.getPixel(i, j, 0) > 0)) { maxi.putPixel(i, j, 0, 255); nb++; }
         * else { } } }
         *
         */
        // with ArrayList
        ArrayList<Voxel3D> list = ((Object3DVoxels) f).getVoxels();
        ArrayList<Voxel3D> maxlo = new ArrayList<Voxel3D>();

        Iterator it = list.iterator();
        int xx, yy, zz;
        Voxel3D vox;
        Voxel3D vox1;
        while (it.hasNext()) {
            vox = (Voxel3D) it.next();
            xx = vox.getRoundX();
            yy = vox.getRoundY();
            zz = vox.getRoundZ();
            if (dim == 2) {
                zz = 0;
            }
            if (maxi.getPix(xx, yy, zz) > 0) {
                //maxi.putPixel(xx, yy, 0, 255);
                vox1 = new Voxel3D(vox);
                vox1.setZ(0);
                maxlo.add(vox1);
                //nb++;
                //} else {
                //maxi.putPixel(xx, yy, 0, 0);
                //}
            }
        }
        nb = maxlo.size();
        IJ.log("max loco " + nb);
        if (debug) {
            new ImagePlus("max loco-" + val, maxi.getStack()).show();
            //new ImagePlus("seg2D-" + val, seg2D.getStack()).show();
            //new ImagePlus("seg-" + val, seg.getStack()).show();
            new ImagePlus("proj-" + val, proj.getStack()).show();
        } // si 1 seul pixel on retourne false
        if (maxlo.size() < 2) {
            return null;
        }
        /*
         * Voxel3D tablo[] = new Voxel3D[nb]; nb = 0; // ranger max locaux dans
         * le tablo for (int j = 0; j < maxi.getSizey(); j++) { for (int i = 0;
         * i < maxi.getSizex(); i++) { if ((maxi.getPixel(i, j, 0) > 0)) {
         * tablo[nb] = new Voxel3D(i, j, 0, 0); nb++; } } } /*
         */

        // chercher deux max locaux les plus eloignes
        int i1 = 0;
        int i2 = 0;
        double dmax = 0.0;
        double d;
        for (int i = 0; i < nb; i++) {
            for (int j = i + 1; j < nb; j++) {
                d = maxlo.get(i).distance(maxlo.get(j));
                if (d > dmax) {
                    dmax = d;
                    i1 = i;
                    i2 = j;
                }
            }
        }

        // Ranger les max locaux en deux classes et calculer barycentre des deux classes
        cx1 = 0;
        cy1 = 0;
        cz1 = 0;
        cx2 = 0;
        cy2 = 0;
        cz2 = 0;
        double d1;
        double d2;
        /*
         * if (debug) { System.out.println("max locaux eloignes:");
         * System.out.println("centre1: " + tablo[i1].getX() + " " +
         * tablo[i1].getY() + " " + tablo[i1].getZ());
         * System.out.println("centre2: " + tablo[i2].getX() + " " +
         * tablo[i2].getY() + " " + tablo[i2].getZ());
         * System.out.println("distance : " + tablo[i1].distance(tablo[i2])); }
         *
         */
        Voxel3D PP1 = new Voxel3D(maxlo.get(i1).getX(), maxlo.get(i1).getY(), maxlo.get(i1).getZ(), 0.0);
        Voxel3D PP2 = new Voxel3D(maxlo.get(i2).getX(), maxlo.get(i2).getY(), maxlo.get(i2).getZ(), 0.0);
        Voxel3D P1 = new Voxel3D(cx1, cy1, cz1, 0);
        Voxel3D P2 = new Voxel3D(cx2, cy2, cz2, 0);

        int nbite = 0;
        while (((P1.distance(PP1) > 1) || (P2.distance(PP2) > 1)) && (nbite < 100)) {
            nbite++;
            cx1 = 0;
            cy1 = 0;
            cx2 = 0;
            cy2 = 0;
            cz1 = 0;
            cz2 = 0;
            nb1 = 0;
            nb2 = 0;
            P1.setX(PP1.getX());
            P1.setY(PP1.getY());
            P1.setZ(PP1.getZ());
            P2.setX(PP2.getX());
            P2.setY(PP2.getY());
            P2.setZ(PP2.getZ());
            for (int i = 0; i < nb; i++) {
                d1 = P1.distance(maxlo.get(i));
                d2 = P2.distance(maxlo.get(i));
                if (d1 < d2) {
                    cx1 += maxlo.get(i).getX();
                    cy1 += maxlo.get(i).getY();
                    cz1 += maxlo.get(i).getZ();
                    nb1++;
                } else {
                    cx2 += maxlo.get(i).getX();
                    cy2 += maxlo.get(i).getY();
                    cz2 += maxlo.get(i).getZ();
                    nb2++;
                }
            }
            cx1 /= nb1;
            cy1 /= nb1;
            cx2 /= nb2;
            cy2 /= nb2;
            cz1 /= nb1;
            cz2 /= nb2;

            PP1.setX(cx1);
            PP1.setY(cy1);
            PP2.setX(cx2);
            PP2.setY(cy2);
            PP1.setZ(cz1);
            PP2.setZ(cz2);
        }

        if (debug) {
            System.out.println("max locaux centres:");
            System.out.println("centre1: " + cx1 + " " + cy1 + " " + cz1);
            System.out.println("centre2: " + cx2 + " " + cy2 + " " + cz2);
            System.out.println("distance: " + PP1.distance(PP2));
        }
        // remonter centres en 3D
        // prendre z milieu
        if (dim == 2) {
            double zmin1 = f.getZmax();
            double zmax1 = f.getZmin();
            double zmin2 = f.getZmax();
            double zmax2 = f.getZmin();
            double di, z;
            // With ArrayList
            it = list.iterator();
            while (it.hasNext()) {
                vox = (Voxel3D) it.next();
                z = vox.getZ();
                // PP1
                PP1.setZ(z);
                di = PP1.distanceSquare(vox);
                if (di < 1) {
                    if (z < zmin1) {
                        zmin1 = z;
                    }
                    if (z > zmax1) {
                        zmax1 = z;
                    }
                }
                // PP2
                PP2.setZ(z);
                di = PP2.distanceSquare(vox);
                if (di < 1) {
                    if (z < zmin2) {
                        zmin2 = z;
                    }
                    if (z > zmax2) {
                        zmax2 = z;
                    }
                }

            }
            cz1 = 0.5 * (zmin1 + zmax1);
            cz2 = 0.5 * (zmin2 + zmax2);

            PP1.setZ(cz1);
            PP2.setZ(cz2);
        }

        /*
         * int sizez = f.getSegImage().getSizez(); while ((seg.getPixel((int)
         * cx1, (int) cy1, k) != val) && (k < sizez)) { k++; } if (k >= sizez) {
         * k1 = f.getZmin(); k2 = f.getZmax(); } else { k1 = k; while
         * ((seg.getPixel((int) cx1, (int) cy1, k) == val) && (k < sizez)) {
         * k++; } if (k >= sizez) { k2 = f.getZmax(); } else { k2 = k; } }
         *
         */
        //cz1 = (k1 + k2) / 2.0;
            /*
         * // z pour spot 2 k = f.getZmin(); while ((seg.getPixel((int) cx2,
         * (int) cy2, k) != val) && (k < sizez)) { k++; } if (k >= sizez) { k1 =
         * f.getZmin(); k2 = f.getZmax(); } else { k1 = k; while
         * ((seg.getPixel((int) cx2, (int) cy2, k) == val) && (k < sizez)) {
         * k++; } if (k >= sizez) { k2 = f.getZmax(); } else { k2 = k; } } cz2 =
         * (k1 + k2) / 2.0;
         *
         */

        if (debug) {
            System.out.println("max locaux centres en Z:");
            System.out.println("centre1: " + cx1 + " " + cy1 + " " + cz1);
            System.out.println("centre2: " + cx2 + " " + cy2 + " " + cz2);
            System.out.println("distance: " + PP1.distance(PP2));
        }

        // si distance trop petite, on retourne false
        if (PP1.distance(PP2) < dist) {
            return null;
        }
        // hybrid watershed test
        double db1 = f.distPixelBorder(PP1);
        double db2 = f.distPixelBorder(PP2);
        //System.out.println("db " + db1 + " " + db2);
        double db = db1 - db2;
        boolean oneIsLarger = true;
        // object 2 larger than object 1
        if (db2 > db1) {
            db = db2 - db1;
            oneIsLarger = false;
        }

        ////////////////////////////////////////////////////////
        ///// separate the two objects   ///////////////////////
        ////////////////////////////////////////////////////////
        //ArrayUtil tab;
        //IntImage3D seg1 = new IntImage3D(sizex, sizey, sizez);
        //IntImage3D seg2 = new IntImage3D(sizex, sizey, sizez);
        ArrayList<Voxel3D> ob1 = new ArrayList<Voxel3D>();
        ArrayList<Voxel3D> ob2 = new ArrayList<Voxel3D>();
        //IntImage3D tmp;

        // with ArrayList
        it = list.iterator();
        while (it.hasNext()) {
            vox = (Voxel3D) it.next();
            dist1 = PP1.distance(vox);
            dist2 = PP2.distance(vox);
            if (oneIsLarger) {
                dist1 -= db;
            } else {
                dist2 -= db;
            }
            if (dist1 < dist2) {
                vox1 = new Voxel3D(vox);
                vox1.setValue(new1);
                ob1.add(new Voxel3D(vox1));
            } else {
                vox1 = new Voxel3D(vox);
                vox1.setValue(new2);
                ob2.add(new Voxel3D(vox1));
            }
        }
        /*
         * for (k = 0; k < seg.getSizez(); k++) { for (int j = 0; j <
         * seg.getSizey(); j++) { for (int i = 0; i < seg.getSizex(); i++) { if
         * (seg.getPixel(i, j, k) == val) { dist1 = (i - cx1) * (i - cx1) + (j -
         * cy1) * (j - cy1) + (k - cz1) * (k - cz1); dist2 = (i - cx2) * (i -
         * cx2) + (j - cy2) * (j - cy2) + (k - cz2) * (k - cz2); if
         * (oneIsLarger) { dist1 -= db; } else { dist2 -= db; }
         *
         * }
         * }
         * }
         * }
         */

        ArrayList[] tab = new ArrayList[2];
        tab[0] = ob1;
        tab[1] = ob2;

        return tab;
    }

    public Voxel3D getLocalMaximum(int x, int y, int z, float radx, float rady, float radz) {
        Voxel3D v = null;

        int sizex = rawImage.sizeX;
        int sizey = rawImage.sizeY;
        int sizez = rawImage.sizeZ;

        double vmax = Double.NEGATIVE_INFINITY;
        double pix;

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

        for (int k = z - vz; k <= z + vz; k++) {
            for (int j = y - vy; j <= y + vy; j++) {
                for (int i = x - vx; i <= x + vx; i++) {
                    if (i >= 0 && j >= 0 && k >= 0 && i < sizex && j < sizey && k < sizez) {
                        dist = ((x - i) * (x - i)) / rx2 + ((y - j) * (y - j)) / ry2 + ((z - k) * (z - k)) / rz2;
                        if (dist <= 1.0) {
                            pix = rawImage.getPixel(i, j, k);
                            if (pix > vmax) {
                                v = new Voxel3D(i, j, k, pix);
                                vmax = pix;
                            }
                        }
                    }
                }
            }
        }

        return v;

    }
}
