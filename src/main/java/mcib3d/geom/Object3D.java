package mcib3d.geom;

import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.gui.PolygonRoi;
import ij.gui.Roi;
import ij.measure.Calibration;
import ij.process.ByteProcessor;
import ij3d.Volume;
import marchingcubes.MCCube;
import mcib3d.Jama.EigenvalueDecomposition;
import mcib3d.Jama.Matrix;
import mcib3d.image3d.*;
import mcib3d.image3d.distanceMap3d.EDT;
import mcib3d.image3d.processing.BinaryMorpho;
import mcib3d.image3d.processing.FastFilters3D;
import mcib3d.utils.ArrayUtil;
import mcib3d.utils.KDTreeC;
import mcib3d.utils.KDTreeC.Item;
import mcib3d.utils.Logger.AbstractLog;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.util.*;

/**
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
 * package mcib3d.geom;
 * <p>
 * /**
 * Abstract class for 3D objects, compute various measurements, and also distance analysis.
 *
 * @author thomas
 */
public abstract class Object3D implements Comparable<Object3D> {
    public static final byte MEASURE_NONE = 0;
    public static final byte MEASURE_VOLUME_PIX = 1;
    public static final byte MEASURE_VOLUME_UNIT = 2;
    public static final byte MEASURE_MAIN_ELONGATION = 3;
    public static final byte MEASURE_COMPACTNESS_VOXELS = 4;
    public static final byte MEASURE_COMPACTNESS_UNITS = 5;
    //public static final byte MEASURE_SPHERICITY = 5;
    public static final byte MEASURE_AREA_PIX = 6;
    public static final byte MEASURE_AREA_UNIT = 7;
    public static final byte MEASURE_DC_AVG = 8;
    public static final byte MEASURE_DC_SD = 9;
    // with currentQuantifImage
    public static final byte MEASURE_INTENSITY_AVG = 10;
    public static final byte MEASURE_INTENSITY_SD = 11;
    public static final byte MEASURE_INTENSITY_MIN = 12;
    public static final byte MEASURE_INTENSITY_MAX = 13;
    public static final byte MEASURE_INTENSITY_MEDIAN = 14;

    /**
     * Centred Moments order 2
     */
    public double s200 = Double.NaN;
    /**
     * use verbose mode (not used ?)
     */
    public boolean verbose = false;
    /**
     * use multithreading mode (not used ?)
     */
    public boolean multiThread = false;
    // TEST comparable
    public double compare = 0;
    /**
     * name the object (not used yet)
     */
    protected String name = "";
    /**
     * category of the object (not used yet)
     */
    protected int type = 0;
    /**
     * comment on the object (not used yet)
     */
    protected String comment = "";
    /**
     * Bounding box
     */
    protected int xmin, ymin, zmin, xmax, ymax, zmax;
    /**
     * IsoBarycenter x
     */
    protected double bx = Double.NaN;
    /**
     * IsoBarycenter x
     */
    protected double by = Double.NaN;
    /**
     * IsoBarycenter y
     */
    protected double bz = Double.NaN;
    /**
     * Center of mass x
     */
    protected double cx = Double.NaN;
    /**
     * Center of mass y
     */
    protected double cy = Double.NaN;
    /**
     * Center of mass z
     */
    protected double cz = Double.NaN;
    /**
     * Area in voxels
     */
    protected double areaNbVoxels = -1;
    /**
     * Touch the borders ?
     */
    //protected boolean touchBorders;
    /**
     * Area in units
     */
    protected double areaContactUnit = -1;
    protected double areaContactVoxels = -1;
    /**
     * volume in voxels
     */
    protected int volume = -1;
    /**
     * feret diameter
     */
    protected double feret = Double.NaN;
    /**
     * Pixel 1 for feret diameter
     */
    protected Voxel3D feret1 = null;
    /**
     * Pixel 2 for feret diameter
     */
    protected Voxel3D feret2 = null;
    /**
     * Integrated density (sum of pixels)
     */
    protected double integratedDensity = Double.NaN;
    protected double meanDensity = Double.NaN;
    /**
     * standard deviation
     */
    protected double sigma = Double.NaN;
    /**
     * Min pix value in object
     */
    protected double pixmin = Double.NaN;
    /**
     * Max pix value in object
     */
    protected double pixmax = Double.NaN;
    /**
     * Value (grey-level)
     */
    protected int value = 0;
    /**
     * Contours pixels
     */
    protected LinkedList<Voxel3D> contours = null;
    /**
     * kd-tree for the contour
     */
    protected KDTreeC kdtreeContours = null;
    protected double s110, s101, s020, s011, s002;
    /**
     * Centred Moments order 3
     */
    protected double s300 = Double.NaN;
    protected double s210, s201, s030, s120, s021, s003, s102, s012, s111;
    /**
     * Centred Moments order 4
     */
    protected double s400 = Double.NaN;
    protected double s040, s004, s220, s202, s022, s121, s112, s211;
    protected double s103, s301, s130, s310, s013, s031;
    /**
     * Matrix of decomposition
     */
    protected EigenvalueDecomposition eigen = null;
    /**
     * The image where the object lies with offset usually xmin, ymin and zmin
     */
    protected ImageInt miniLabelImage = null;
    /**
     * label image, starts at 0,0,0
     */
    protected ImageHandler labelImage = null;
    /**
     * current image used for quantification (to compute results once)
     */
    protected ImageHandler currentQuantifImage = null;
    /**
     * the resolution in XY
     */
    protected double resXY = 1;
    //protected int offX = 0, offY = 0, offZ = 0; // offset of object into label image
    /**
     * the resolution in Z
     */
    protected double resZ = 1;
    /**
     * the unit for resolution
     */
    protected String units = "pixels";
    // log
    AbstractLog logger;
    boolean logging = true;

    // center distances stat unit
    double distCenterMinUnit = Double.NaN;
    double distCenterMaxUnit = Double.NaN;
    double distCenterMeanUnit = Double.NaN;
    double distCenterSigmaUnit = Double.NaN;
    // center distances stat pixel
    double distCenterMinPixel = Double.NaN;
    double distCenterMaxPixel = Double.NaN;
    double distCenterMeanPixel = Double.NaN;
    double distCenterSigmaPixel = Double.NaN;


    public static int getNbMoments3D() {
        return 5;
    }

    /**
     * @param A
     * @param B
     * @return
     */
    public static double pcColocSum(Object3D A, Object3D B) {
        double vA = A.getVolumePixels();
        double vB = B.getVolumePixels();
        double vC = A.getColoc(B);

        return 100.0 * vC / (vA + vB);
    }

    /**
     * Gets the calibration in XY of the Object3D
     *
     * @return The resXY value
     */
    public double getResXY() {
        return resXY;
    }

    /**
     * Sets the calibration in XY of the Object3D
     *
     * @param rxy The new calibration in XY
     */
    public void setResXY(double rxy) {
        resXY = rxy;
    }

    /**
     * Gets the calibration in Z of the Object3D
     *
     * @return The resZ value
     */
    public double getResZ() {
        return resZ;
    }

    /**
     * Sets the calibration in Z of the Object3D
     *
     * @param rz he new calibration in Z
     */
    public void setResZ(double rz) {
        resZ = rz;
    }

    /**
     * Gets the unit attribute of the Object3D object
     *
     * @return The units
     */
    public String getUnits() {
        return units;
    }

    /**
     * Sets the units attribute for the calibration
     *
     * @param u The new units value
     */
    public void setUnits(String u) {
        units = u;
    }

    /**
     * @return The calibration
     * @deprecated use Object3D-IJUtils
     * Gets the calibration of the Object3D (ImageJ)
     */
    public Calibration getCalibration() {
        Calibration cal = new Calibration();
        cal.pixelWidth = resXY;
        cal.pixelHeight = resXY;
        cal.pixelDepth = resZ;
        cal.setUnit(units);

        return cal;
    }

    /**
     * @param cal The new calibration
     * @deprecated use Object3D-IJUtils
     * Sets the calibration of the Object3D (ImageJ)
     */
    public void setCalibration(Calibration cal) {
        resXY = cal.pixelWidth;
        resZ = cal.pixelDepth;
        units = cal.getUnits();
    }

    /**
     * Sets the calibration of the Object3D
     *
     * @param rxy The new calibration in XY
     * @param rz  The new calibration in Z
     * @param u   The new calibration units
     */
    public final void setCalibration(double rxy, double rz, String u) {
        resXY = rxy;
        resZ = rz;
        units = u;
    }

    /**
     * The name of the object
     *
     * @return name
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the name
     *
     * @param name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * The type
     *
     * @return type
     */
    public int getType() {
        return type;
    }

    /**
     * Sets the type
     *
     * @param type
     */
    public void setType(int type) {
        this.type = type;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    /**
     * Gets the label image of the object (should start at 0,0
     *
     * @return the image with the labelled object
     */
    public ImageHandler getLabelImage() {
        if (labelImage == null) {
            labelImage = this.createSegImage();
        }
        return labelImage;
    }

    /**
     * Sets the label image of the object (should start at 0,0,0)
     *
     * @param labelImage
     */
    public void setLabelImage(ImageInt labelImage) {
        this.labelImage = labelImage;
        // offset is associated to label image
        /*
        if (labelImage != null) {
            offX = labelImage.offsetX;
            offY = labelImage.offsetY;
            offZ = labelImage.offsetZ;
        } else {
            offX = 0;
            offY = 0;
            offZ = 0;
        }*/
    }

    /**
     * Get the label image starting at 0,0,0
     *
     * @param val the value to draw the binary mask
     * @return The binary mask for the object
     */
    public ImageHandler getMaxLabelImage(int val) {
        return createMaxSegImage(val);
    }

    /**
     * volume of the Object3D (in pixel)
     *
     * @return the volume
     */
    public int getVolumePixels() {
        if (volume == -1) {
            computeVolume();
        }
        return volume;
    }

    /**
     * Utility to perform measurement
     *
     * @param Measure The index of the measurement
     * @return the measurement
     */
    public double getMeasure(int Measure) {
        if (Measure == MEASURE_VOLUME_PIX) {
            return getVolumePixels();
        } else if (Measure == MEASURE_VOLUME_UNIT) {
            return getVolumeUnit();
        } else if (Measure == MEASURE_AREA_PIX) {
            return getAreaPixels();
        } else if (Measure == MEASURE_AREA_UNIT) {
            return getAreaUnit();
        } else if (Measure == MEASURE_MAIN_ELONGATION) {
            return getMainElongation();
        } else if (Measure == MEASURE_COMPACTNESS_VOXELS) {
            return getCompactness(false);
        } else if (Measure == MEASURE_COMPACTNESS_UNITS) {
            return getCompactness(true);
        } else if (Measure == MEASURE_DC_AVG) {
            return getDistCenterMean();
        } else if (Measure == MEASURE_DC_SD) {
            return getDistCenterSigma();
        } else if (Measure == MEASURE_INTENSITY_AVG) {
            if (currentQuantifImage != null) {
                return getPixMeanValue(currentQuantifImage);
            } else {
                return 0;
            }
        } else if (Measure == MEASURE_INTENSITY_SD) {
            if (currentQuantifImage != null) {
                return getPixStdDevValue(currentQuantifImage);
            } else {
                return 0;
            }
        } else if (Measure == MEASURE_INTENSITY_MAX) {
            if (currentQuantifImage != null) {
                return getPixMaxValue(currentQuantifImage);
            } else {
                return 0;
            }
        } else if (Measure == MEASURE_INTENSITY_MEDIAN) {
            if (currentQuantifImage != null) {
                return getPixMedianValue(currentQuantifImage);
            } else {
                return 0;
            }
        } else if (Measure == MEASURE_INTENSITY_MIN) {
            if (currentQuantifImage != null) {
                return getPixMinValue(currentQuantifImage);
            } else {
                return 0;
            }
        } else {
            return Double.NaN;
        }
    }

    /**
     * Compute the volume, will compute voxels in case of surface object
     */
    private void computeVolume() {
        volume = getVoxels().size();
    }

    /**
     * Gets the volume of the Object3D in units
     *
     * @return The calibrated volume
     */
    public double getVolumeUnit() {
        return getVolumePixels() * resXY * resXY * resZ;
    }

    /**
     * Compute the barycenter of the object
     */
    protected abstract void computeCenter();

    /**
     * Compute the mass center of the object using signal from an image
     *
     * @param ima the image with the signal intensity
     */
    protected abstract void computeMassCenter(ImageHandler ima);

    /**
     * Compute the mass center of the object using signal from an image and within a mask
     *
     * @param ima  the image with the signal intensity
     * @param mask the mask to restrain the computation
     */
    protected abstract void computeMassCenter(ImageHandler ima, ImageHandler mask);

    /**
     * Gets the list of values inside an image as an array
     *
     * @param ima image
     * @return the array of pixel values
     */
    public float[] getArrayValues(ImageHandler ima) {
        List<Voxel3D> vox = getVoxels();
        float[] res = new float[vox.size()];
        int i = 0;
        int x, y, z;
        for (Voxel3D v : vox) {
            x = v.getRoundX();
            y = v.getRoundY();
            z = v.getRoundZ();
            if (ima.contains(x, y, z)) {
                res[i++] = ima.getPixel(x, y, z);
            }
        }
        return res;
    }

    /**
     * Gets the quantile value of the object in the image
     *
     * @param ima image
     * @return the quantile value
     */
    public double getQuantilePixValue(ImageHandler ima, double quantile) {
        if (quantile == 1) {
            return getPixMaxValue(ima);
        } else if (quantile == 0) {
            return getPixMinValue(ima);
        }
        float[] vals = getArrayValues(ima);
        if (vals.length == 0) {
            return Double.NaN;
        }
        Arrays.sort(vals);
        double index = quantile * (vals.length - 1);
        if (index <= 0) {
            return vals[0];
        } else if (index >= (vals.length - 1)) {
            return vals[vals.length - 1];
        }
        double d = index - (int) index;
        if (d == 0) {
            return vals[(int) index];
        }
        double val1 = vals[(int) index];
        double val2 = vals[(int) (index + 1)];
        return d * val2 + (1 - d) * val1;
    }

    /**
     * Compute the bounding box of the object
     */
    protected abstract void computeBounding();

    /**
     * Compute the contour voxels of the object
     */
    // FIXME check contours connexity and test borders of image (mereo)
    public abstract void computeContours();

    /**
     * Compute the moments of the object (for ellipsoid orientation), order 2
     *
     * @param normalize normalize by volume or not
     */
    protected abstract void computeMoments2(boolean normalize); // order 2

    /**
     * Compute the moments of the object, order 3
     */
    protected abstract void computeMoments3();

    /**
     * Compute the moments of the object, order 4
     */
    protected abstract void computeMoments4(); // order 3

    /**
     * Returns the moments order 2
     *
     * @return the moments (6)
     */
    public double[] getMomentsRaw2() {
        if (Double.isNaN(s200)) {
            computeMoments2(false);
        }
        return new double[]{s200, s020, s002, s110, s101, s011};
    }

    /**
     * Returns the moments order 3
     *
     * @return the moments (10)
     */
    public double[] getMomentsRaw3() {
        if (Double.isNaN(s300)) {
            computeMoments3();
        }
        return new double[]{s300, s030, s003, s210, s201, s120, s021, s102, s012, s111};
    }

    /**
     * Returns the moments order 4
     *
     * @return the moments (15)
     */
    public double[] getMomentsRaw4() {
        if (Double.isNaN(s400)) {
            computeMoments4();
        }
        return new double[]{s400, s040, s004, s220, s202, s022, s121, s112, s211, s103, s301, s130, s310, s013, s031};
    }

    /**
     * Compute geometric invariants, based on orders 4 moments
     * refer to Xu and Li 2008. Geometric moments invariants. Pattern recognition. doi:10.1016/j.patcog.2007.05.001
     *
     * @return the invariants (6)
     */
    public double[] getGeometricInvariants() {
        if (Double.isNaN(s200)) {
            computeMoments2(false);
        }
        if (Double.isNaN(s300)) {
            computeMoments3();
        }
        if (Double.isNaN(s400)) {
            computeMoments4();
        }
        double[] inv = new double[6];
        double v = getVolumeUnit();
        // FIXME power in integer ??
        inv[0] = (s400 + s040 + s004 + 2 * s220 + 2 * s202 + 2 * s022) / (Math.pow(v, 7.0 / 3.0));
        inv[1] = (s400 * s040 + s400 * s004 + s004 * s040 + 3 * s220 * s220 + 3 * s202 * s202 + 3 * s022 * s022
                - 4 * s103 * s301 - 4 * s130 * s310 - 4 * s013 * s031
                + 2 * s022 * s202 + 2 * s022 * s220 + 2 * s220 * s202
                + 2 * s022 * s400 + 2 * s004 * s220 + 2 * s040 * s202
                - 4 * s103 * s121 - 4 * s130 * s112 - 4 * s013 * s211
                - 4 * s121 * s301 - 4 * s112 * s310 - 4 * s211 * s031
                + 4 * s211 * s211 + 4 * s112 * s112 + 4 * s121 * s121) / (Math.pow(v, 14.0 / 3.0));
        inv[2] = (s400 * s400 + s040 * s040 + s004 * s004
                + 4 * s130 * s130 + 4 * s103 * s103 + 4 * s013 * s013 + 4 * s031 * s031 + 4 * s310 * s310 + 4 * s301 * s301
                + 6 * s220 * s220 + 6 * s202 * s202 + 6 * s022 * s022
                + 12 * s112 * s112 + 12 * s121 * s121 + 12 * s211 * s211) / (Math.pow(v, 14.0 / 3.0));
        inv[3] = (s300 * s300 + s030 * s030 + s003 * s003 + 3 * (s210 * s210 + s201 * s201 + s120 * s120 + s021 * s021 + s102 * s102 + s012 * s012) + 6 * s111) / (Math.pow(v, 4));
        inv[4] = (s300 * s300 + s030 * s030 + s003 * s003 + s210 * s210 + s201 * s201 + s120 * s120 + s021 * s021 + s102 * s102 + s012 * s012
                + 2 * (s300 * s120 + s300 * s102 + s030 * s210 + s030 * s012 + s003 * s201 + s003 * s021 + s120 * s102 + s021 * s201 + s012 * s210)) / (Math.pow(v, 4));
        inv[5] = (s200 * (s400 + s220 + s202) + s020 * (s220 + s040 + s022) + s002 * (s202 + s022 + s004)
                + 2 * s110 * (s310 + s130 + s112) + 2 * s101 * (s301 * s121 + s103) + 2 * s011 * (s211 + s031 + s013)) / (Math.pow(v, 4));
        return inv;
    }

    /**
     * Compute homogeneous invariants, based on orders 4 moments
     *
     * @return the invariants (5)
     */
    public double[] getHomogeneousInvariants() {
        if (Double.isNaN(s200)) {
            computeMoments2(false);
        }
        if (Double.isNaN(s300)) {
            computeMoments3();
        }
        if (Double.isNaN(s400)) {
            computeMoments4();
        }
        double[] inv = new double[5];
        double v = getVolumeUnit();

        // order 2
        inv[0] = (s200 + s020 + s002) / (v * v);
        inv[1] = (s200 * s200 + s020 * s020 + s002 * s002 + 2 * s101 * s101 + 2 * s110 * s110 + 2 * s011 * s011) / (v * v * v * v);
        inv[2] = (s200 * s200 * s200
                + 3 * s200 * s110 * s110 + 3 * s200 * s101 * s101 + 3 * s110 * s110 * s020 + 3 * s101 * s101 * s020
                + s020 * s020 * s020
                + 3 * s020 * s011 * s011 + 3 * s011 * s011 * s002
                + s002 * s002 * s002
                + 6 * s110 * s101 * s011) / Math.pow(v, 6);
        // order 3
        inv[3] = (s300 * s300 + s030 * s030 + s003 * s003 + 3 * s210 * s201 + 3 * s201 * s201 + 3 * s120 * s120 + 3 * s102 * s102 + 3 * s021 * s021 + 3 * s012 * s012 + 6 * s111 * s111) / Math.pow(v, 5);
        inv[4] = (s300 * s300 + 2 * s300 * s120 + 2 * s300 * s102 + 2 * s210 * s030 + 2 * s201 * s003
                + s030 * s030 + 2 * s030 * s012 + 2 * s021 * s003 + s003 * s003 + s210 * s210
                + 2 * s210 * s012 + s201 * s201 + 2 * s201 * s021
                + s120 * s120 + 2 * s120 + s102 + s102 * s102 + s021 * s021 + s012 + s012) / Math.pow(v, 5);
        return inv;
    }

    /**
     * This code is taken from the BIOCAT platform:
     * http://faculty.cs.niu.edu/~zhou/tool/biocat/ Reference for BIOCAT: J.
     * Zhou, S. Lamichhane, G. Sterne, B. Ye, H. C. Peng, "BIOCAT: a Pattern
     * Recognition Platform for Customizable Biological Image Classification and
     * Annotation", BMC Bioinformatics , 2013, 14:291 * This class calculates a
     * set of five 3D image moments that are invariant to size, position and
     * orientation. Reference: F. A. Sadjadi and E. L. Hall, Three-Dimensional
     * Moment Invariants, IEEE Transactions on Pattern Analysis and Machine
     * Intelligence, vol. PAMI-2, no. 2, pp. 127-136, March 1980.
     *
     * @return 3D moments (5)
     */
    public double[] getMoments3D() {
        computeMoments2(false);
//        if (Double.isNaN(s300)) {
//            computeMoments3();
//        }
//        if (Double.isNaN(s400)) {
//            computeMoments4();
//        }
        // normalize
        double v = getVolumeUnit();
        double v53 = Math.pow(v, 5 / 3); // keep in integer ?
        s200 /= v53;
        s020 /= v53;
        s002 /= v53;
        s011 /= v53;
        s101 /= v53;
        s110 /= v53;

        double J1 = (s200 + s020 + s002);
        double J2 = (s020 * s002 - s011 * s011 + s200 * s002 - s101 * s101 + s200 * s020 - s110 * s110);
        double J3 = (s200 * s020 * s002 + 2 * s110 * s101 * s011 - s002 * s110 * s110 - s020 * s101 * s101 - s200 * s011 * s011);
        double I1 = (J1 * J1) / J2;
        double I2 = J3 / (J1 * J1 * J1);

        return new double[]{J1, J2, J3, I1, I2};
    }

    /**
     * compute the voxel with maximum value in an image
     *
     * @param ima the image
     * @return the max voxel
     */
    public abstract Voxel3D getPixelMax(ImageHandler ima);

    /**
     * gets the list of all pixels within an image as an ArrayList
     *
     * @param ima the image with signal
     * @return the list of Voxels
     */
    public List<Voxel3D> listVoxels(ImageHandler ima) {
        return listVoxels(ima, Double.NEGATIVE_INFINITY);
    }

    /**
     * Outputs the list of Voxels values using intensity image
     *
     * @param ima the intensity image
     * @return a array of values
     */
    public abstract ArrayUtil listValues(ImageHandler ima);

    /**
     * Outputs the list of Voxels values using intensity image above a fixed threshold
     *
     * @param ima the intensity image
     * @return a array of values
     */
    public abstract ArrayUtil listValues(ImageHandler ima, float thresh);

    /**
     * List Voxels in the image with values > threshold
     *
     * @param ima    The image with values
     * @param thresh the threshold
     * @return the list of Voxels with values > threshold
     */
    public abstract List<Voxel3D> listVoxels(ImageHandler ima, double thresh);

    /**
     * List Voxels in the image with values > threshold0 and < threshold1
     *
     * @param ima     The image with values
     * @param thresh0 the min threshold
     * @param thres1  the max threshold
     * @return the list of Voxels with values > threshold
     */
    public abstract List<Voxel3D> listVoxels(ImageHandler ima, double thresh0, double thres1);

    /**
     * List Voxels in the image with with distances in specific range from a reference point
     *
     * @param P0          the reference point
     * @param dist0       the min distance
     * @param dist1       the max distance
     * @param contourOnly lsit only voxels from the contour of the object
     * @return
     */
    public List<Voxel3D> listVoxelsByDistance(Point3D P0, double dist0, double dist1, boolean contourOnly) {
        List<Voxel3D> res = new LinkedList<Voxel3D>();
        List<Voxel3D> list;
        if (contourOnly) {
            if (contours == null) {
                computeContours();
            }
            list = contours;
        } else {
            list = getVoxels();
        }
        for (Voxel3D Vox : list) {
            double dist = Vox.distance(P0, resXY, resZ);
            if ((dist > dist0) && (dist < dist1)) {
                Voxel3D V = new Voxel3D(Vox);
                V.setValue(dist);
                res.add(V);
            }
        }

        return res;
    }

    /**
     * Convert the object to voxels (if necessary)
     *
     * @return the object as a voxels object
     */
    public Object3DVoxels getObject3DVoxels() {
        if (this instanceof Object3DVoxels) {
            return (Object3DVoxels) this;
        } else if (this instanceof Object3DSurface) {
            return ((Object3DSurface) this).buildObject3DVoxels();
        } else if (this instanceof Object3DLabel) {
            return ((Object3DLabel) this).buildObject3DVoxels();
        } else return null;
    }

    /**
     * Convert the object to surface (if necessary)
     *
     * @return the object as a voxels object
     */
    public Object3DSurface getObject3DSurface() {
        if (this instanceof Object3DSurface) {
            return (Object3DSurface) this;
        } else if (this instanceof Object3DVoxels) {
            return new Object3DSurface(this.computeMeshSurface(true));
        } else if (this instanceof Object3DLabel) {
            return new Object3DSurface(this.computeMeshSurface(true));
        } else {
            return null;
        }
    }

    /**
     * Percentage of colocalisation between two objects relative to this object
     *
     * @param obj the other object
     * @return the percentage of colocalisation (0-100)
     */
    public double pcColoc(Object3D obj) {
        if (this.disjointBox(obj)) {
            return 0;
        }
        int cpt = getColoc(obj);
        if (cpt == 0) {
            return 0d;
        }
        return (100.0 * cpt) / ((double) (this.getVolumePixels()));

//        ImageInt inter = this.createIntersectionImage(obj, 1, 2);
//        int[] hist = inter.getHistogram(new BlankMask(inter));
//        return 100.0 * ((double) hist[3]) / ((double) (hist[1] + hist[3]));
        // FIXME incoherent avec les methodes overridés
    }

    /**
     * drawing inside an objectCreator
     *
     * @param obj the object creator
     * @param col the color(grey level)
     */
    public abstract void draw(ObjectCreator3D obj, int col);

    /**
     * @param mask the byte processor
     * @param z    the z slice
     * @param col  the color(grey level)
     * @return
     * @deprecated use Object3D-IJUtils
     * drawing inside a 2D byteprocessor
     */
    public abstract boolean draw(ByteProcessor mask, int z, int col);

    /**
     * @param mask the image
     * @param col  the color(grey level)
     * @deprecated use Object3D-IJUtils
     * drawing inside an imagestack
     */
    public abstract void draw(ImageStack mask, int col);

    /**
     * drawing inside an ImageHandler
     *
     * @param mask the image
     * @param col  the color(grey level)
     */
    public abstract void draw(ImageHandler mask, int col);

    /**
     * drawing inside an ImageHandler
     *
     * @param mask the image
     * @param val  the real value
     */
    public abstract void draw(ImageHandler mask, float val);


    public void drawAt(ImageHandler mask, int col, Point3D center) {
        double cx = getCenterX();
        double cy = getCenterY();
        double cz = getCenterZ();

        translate(center.getX() - cx, center.getY() - cy, center.getZ() - cz);
        draw(mask, col);
        translate(-center.getX() + cx, -center.getY() + cy, -center.getZ() + cz);
    }

    /**
     * @param mask
     * @param col
     * @param tx
     * @param ty
     * @param tz
     */
    public abstract void draw(ImageHandler mask, int col, int tx, int ty, int tz);

    /**
     * Drawing inside an image, with default value = object value
     *
     * @param mask image
     */
    public void draw(ImageHandler mask) {
        draw(mask, this.getValue());
    }

    /**
     * Drawing links between two objects
     *
     * @param mask
     * @param other
     * @param col
     */
    public void drawLink(ImageHandler mask, Object3D other, int col) {
        ObjectCreator3D create = new ObjectCreator3D(mask);
        create.createLine(this.getCenterAsPoint(), other.getCenterAsPoint(), col, 1);
    }

    /**
     * @param mask the imagestack
     * @param r    red value
     * @param g    greeen value
     * @param b    blue value
     * @deprecated use Object3D-IJUtils
     * drawing inside an imagestack, in rgb color
     */
    public abstract void draw(ImageStack mask, int r, int g, int b);

    /**
     * @param z the z slice
     * @return the contour roi in slice z
     * @deprecated use Object3D-IJUtils
     * create a roi for a slice
     */
    public abstract Roi createRoi(int z);

    public PolygonRoi getConvexPolygonRoi(int z) {
        LinkedList<Voxel3D> contours3D = this.getContours();
        int[] x = new int[contours3D.size()];
        int[] y = new int[contours3D.size()];
        int nbPoint = -1;
        for (Voxel3D contour : contours3D) {
            if (Math.abs(z - contour.z) < 0.5) {
                nbPoint++;
                x[nbPoint] = contour.getRoundX();
                y[nbPoint] = contour.getRoundY();
            }
        }
        int[] xx = new int[nbPoint + 1];
        int[] yy = new int[nbPoint + 1];
        System.arraycopy(x, 0, xx, 0, nbPoint + 1);
        System.arraycopy(y, 0, yy, 0, nbPoint + 1);
        PolygonRoi pRoi = new PolygonRoi(xx, yy, nbPoint, Roi.POLYGON);
        // if not convex, the contours points are not ordered
        return (new PolygonRoi(pRoi.getConvexHull(), Roi.POLYGON));
    }

    /**
     * Init default values and compute contours and center
     */
    public void init() {
        // center of mass
        cx = Double.NaN;
        cy = Double.NaN;
        cz = Double.NaN;
        // barycenter
        bx = Double.NaN;
        by = Double.NaN;
        bz = Double.NaN;
        // moments
        s200 = Double.NaN;
        s110 = Double.NaN;
        s101 = Double.NaN;
        s020 = Double.NaN;
        s011 = Double.NaN;
        s002 = Double.NaN;
        eigen = null;
        // dist center
        distCenterMinUnit = Double.NaN;
        distCenterMaxUnit = Double.NaN;
        distCenterMeanUnit = Double.NaN;
        distCenterSigmaUnit = Double.NaN;
        // feret
        feret = Double.NaN;
        feret1 = null;
        feret2 = null;
        integratedDensity = Double.NaN;
        pixmax = Double.NaN;
        pixmin = Double.NaN;
        sigma = Double.NaN;
        volume = -1;
        areaNbVoxels = -1;
        areaContactUnit = -1;
        miniLabelImage = null;
        this.computeBounding();
        this.computeCenter();
    }

    /**
     * return the list of contour voxels
     *
     * @return the list of contour voxels
     */
    public LinkedList<Voxel3D> getContours() {
        if (contours == null) {
            this.computeContours();
        }
        return contours;
    }

    /**
     * Value of the Object3D
     *
     * @return value
     */
    public int getValue() {
        return value;
    }

    /**
     * Sets the value attribute of the Object3D object
     *
     * @param v The new value value
     */
    public void setValue(int v) {
        value = v;
    }

    /**
     * Gets the xmin attribute of the Object3D
     *
     * @return The xmin value
     */
    public int getXmin() {
        //if (xmin == -1) {
        //   computeBounding();
        // }
        return xmin;
    }

    /**
     * Gets the ymin attribute of the Object3D
     *
     * @return The ymin value
     */
    public int getYmin() {
        // if (ymin == -1) {
        //     computeBounding();
        // }
        return ymin;
    }

    /**
     * Gets the zmin attribute of the Object3D
     *
     * @return The zmin value
     */
    public int getZmin() {
        // if (zmin == -1) {
        //     computeBounding();
        // }
        return zmin;
    }

    /**
     * Gets the xmax attribute of the Object3D
     *
     * @return The xmax value
     */
    public int getXmax() {
        ///  if (xmax == -1) {
        //      computeBounding();
        //  }
        return xmax;
    }

    /**
     * Gets the ymax attribute of the Object3D
     *
     * @return The ymax value
     */
    public int getYmax() {
        //  if (ymax == -1) {
        //      computeBounding();
        //  }
        return ymax;
    }

    /**
     * Gets the zmax attribute of the Object3D
     *
     * @return The zmax value
     */
    public int getZmax() {
        //  if (zmax == -1) {
        //      computeBounding();
        //  }
        return zmax;
    }

    /**
     * Get the bounding box of the object
     * (Xmin, Xmax, Ymin, Ymax, Zmin, Zmax)
     *
     * @return the coordinates of the bounding box
     */
    public int[] getBoundingBox() {
        return new int[]{xmin, xmax, ymin, ymax, zmin, zmax};
    }

    /**
     * x coordinate of the center
     *
     * @return x center value
     */
    public double getCenterX() {
        Vector3D ce = getCenterAsVector();
        return ce.getX();
    }

    /**
     * y coordinate of the center
     *
     * @return y center value
     */
    public double getCenterY() {
        Vector3D ce = getCenterAsVector();
        return ce.getY();
    }

    /**
     * z coordinate of the center
     *
     * @return z center value
     */
    public double getCenterZ() {
        Vector3D ce = getCenterAsVector();
        return ce.getZ();
    }

    /**
     * @return
     */
    public boolean isEmpty() {
        return (getVolumePixels() == 0);
    }

    /**
     * integrated density of the Object3D in an image
     *
     * @param ima the image
     * @return the sum of pixels values
     */
    public double getIntegratedDensity(ImageHandler ima) {
        if ((currentQuantifImage == null) || (currentQuantifImage != ima)) {
            computeMassCenter(ima);
            currentQuantifImage = ima;
        }
        return integratedDensity;
    }

    public double getIntegratedDensity(ImageHandler ima, ImageHandler mask) {
        computeMassCenter(ima, mask);
        currentQuantifImage = null;

        return integratedDensity;
    }

    /**
     * x coordinate of the center of mass in an image
     *
     * @param ima the image
     * @return x coordinate of mass center
     */
    public double getMassCenterX(ImageHandler ima) {
        if ((currentQuantifImage == null) || (currentQuantifImage != ima)) {
            computeMassCenter(ima);
            currentQuantifImage = ima;
        }
        return cx;
    }

    /**
     * @param x
     * @param y
     * @param z
     */
    public void setNewCenter(double x, double y, double z) {
        translate(x - this.getCenterX(), y - this.getCenterY(), z - this.getCenterZ());
    }

    public void setNewCenter(Object3D obj) {
        translate(obj.getCenterX() - this.getCenterX(), obj.getCenterY() - this.getCenterY(), obj.getCenterZ() - this.getCenterZ());
    }

    public void setNewCenter(Vector3D newCenter) {
        translate(newCenter.getX() - this.getCenterX(), newCenter.getY() - this.getCenterY(), newCenter.getZ() - this.getCenterZ());
    }


    /**
     * @param x
     * @param y
     * @param z
     */
    public abstract void translate(double x, double y, double z);

    /**
     * @param V
     */
    public void translate(Vector3D V) {
        translate(V.getX(), V.getY(), V.getZ());
    }

    /**
     * u coordinate of the center of mass in an image
     *
     * @param ima the image
     * @return y coordinate of mass center
     */
    public double getMassCenterY(ImageHandler ima) {
        if ((currentQuantifImage == null) || (currentQuantifImage != ima)) {
            computeMassCenter(ima);
            currentQuantifImage = ima;
        }
        return cy;
    }

    /**
     * z coordinate of the center of mass in an image
     *
     * @param ima the image
     * @return z coordinate of mass center
     */
    public double getMassCenterZ(ImageHandler ima) {
        if ((currentQuantifImage == null) || (currentQuantifImage != ima)) {
            computeMassCenter(ima);
            currentQuantifImage = ima;
        }
        return cz;
    }

    /**
     * Gets the center attribute of the Object3D object
     *
     * @return The center value
     */
    public Vector3D getCenterAsVector() {
        if (Double.isNaN(bx)) {
            computeCenter();
        }
        return new Vector3D(bx, by, bz);
    }

    /**
     * Gets the center attribute of the Object3D object
     *
     * @return The center value
     */
    public Vector3D getCenterAsVectorUnit() {
        if (Double.isNaN(bx)) {
            computeCenter();
        }
        return new Vector3D(bx * resXY, by * resXY, bz * resZ);
    }

    /**
     * Gets the center attribute of the Object3D object
     *
     * @return The center value
     */
    public Point3D getCenterAsPoint() {
        if (Double.isNaN(bx)) {
            computeCenter();
        }
        return new Point3D(bx, by, bz);
    }

    /**
     * @return
     */
    public double[] getCenterAsArray() {
        if (Double.isNaN(bx)) {
            computeCenter();
        }
        return this.getCenterAsPoint().getArray();
    }

    /**
     * the area of the Object3D (in pixels)
     *
     * @return the area value
     */
    public double getAreaPixels() {
        if (areaNbVoxels == -1) {
            computeContours();
        }
        // Use contact surface instead
        return areaContactVoxels;
    }

    /**
     * Gets the areaUnit attribute of the Object3D object
     *
     * @return The areaUnit value
     */
    public double getAreaUnit() {
        if (areaContactUnit == -1) {
            computeContours();
        }
        return areaContactUnit;
    }

    /**
     * Gets the compactness attribute of the Object3D (unit) related to
     * sphericity
     *
     * @param useUnit use calibration about area and volumes
     * @return The compactness value
     */
    public double getCompactness(boolean useUnit) {
        double v2, s3;
        if (useUnit) {
            s3 = Math.pow(getAreaUnit(), 3);
            v2 = Math.pow(getVolumeUnit(), 2);
            return (v2 * 36.0 * Math.PI) / s3;
        } else {
            // use non calibrated measures instead (area unit may be surevaluated in unit)
            s3 = Math.pow(getAreaPixels(), 3);
            v2 = Math.pow(getVolumePixels(), 2);
        }
        return (v2 * 36.0 * Math.PI) / s3;
    }

    public double getCompactness() {
        return getCompactness(false);
    }

    public double getSphericity() {
        return getSphericity(false);
    }

    /**
     * Gets the sphericity attribute of the Object3D (unit) related to
     * compactness
     *
     * @return The compactness value
     */
    public double getSphericity(boolean useUnit) {
        return Math.pow(getCompactness(useUnit), 0.333333);
    }

    /**
     * Gets the ration between volume and volume of the bounding box (in units)
     *
     * @return The ratioBox value
     */
    public double getRatioBox() {
        double vol = this.getVolumePixels();
        double volbox = this.getVolumeBoundingBoxPixel();

        return vol / volbox;
    }

    /**
     * Gets the ration between volume and volume of the ellipsoid (in units)
     *
     * @return The ratio value
     */
    public double getRatioEllipsoid() {
        double vol = this.getVolumeUnit();
        double volell = this.getVolumeEllipseUnit();

        return vol / volell;
    }

    /**
     * the volume of the bounding box
     *
     * @return the volume
     */
    public double getVolumeBoundingBoxPixel() {
        //if (zmin == -1) {
        //   computeBounding();
        //}
        return (zmax - zmin + 1) * (xmax - xmin + 1) * (ymax - ymin + 1);
    }

    /**
     * the volume of the oriented bounding box FIXME computing of oriented box
     *
     * @return the volume
     */
    public double getVolumeBoundingBoxOrientedPixel() {
        ArrayList<Voxel3D> oriContours = getBoundingOriented();
        double minx = Double.POSITIVE_INFINITY;
        double maxx = Double.NEGATIVE_INFINITY;
        double miny = Double.POSITIVE_INFINITY;
        double maxy = Double.NEGATIVE_INFINITY;
        double minz = Double.POSITIVE_INFINITY;
        double maxz = Double.NEGATIVE_INFINITY;

        for (Voxel3D vox : oriContours) {
            double x = vox.getX();
            double y = vox.getY();
            double z = vox.getZ();
            if (x > maxx) {
                maxx = x;
            }
            if (x < minx) {
                minx = x;
            }
            if (y > maxy) {
                maxy = y;
            }
            if (y < miny) {
                miny = y;
            }
            if (z > maxz) {
                maxz = z;
            }
            if (z < minz) {
                minz = z;
            }
        }

        double[] bbo = {minx, maxx, miny, maxy, minz, maxz};
        return (bbo[1] - bbo[0] + 1) * (bbo[3] - bbo[2] + 1) * (bbo[5] - bbo[4] + 1);
    }

    public ArrayList<Voxel3D> getBoundingOriented() {
        Vector3D V0 = getVectorAxis(2);
        V0.normalize();
        Vector3D V1 = getVectorAxis(1);
        V1.normalize();
        Vector3D V2 = getVectorAxis(0);
        V2.normalize();
        double v0x = V0.getX();
        double v0y = V0.getY();
        double v0z = V0.getZ();
        double v1x = V1.getX();
        double v1y = V1.getY();
        double v1z = V1.getZ();
        double v2x = V2.getX();
        double v2y = V2.getY();
        double v2z = V2.getZ();

        double[] cen = getCenterAsArray();
        double ccx = cen[0];
        double ccy = cen[1];
        double ccz = cen[2];

        LinkedList<Voxel3D> cont = getContours();
        ArrayList<Voxel3D> orientedCont = new ArrayList<Voxel3D>();
        for (Voxel3D vox : cont) {
            double nx = v0x * (vox.getX() - ccx) + v1x * (vox.getY() - ccy) + v2x * (vox.getZ() - ccz) + ccx;
            double ny = v0y * (vox.getX() - ccx) + v1y * (vox.getY() - ccy) + v2y * (vox.getZ() - ccz) + ccy;
            double nz = v0z * (vox.getX() - ccx) + v1z * (vox.getY() - ccy) + v2z * (vox.getZ() - ccz) + ccz;
            orientedCont.add(new Voxel3D(nx, ny, nz, getValue()));
        }

        return orientedCont;
    }

    /**
     * The volume of the approximated ellipsoid in unit the main radius is DCMax
     *
     * @return the volume of the ellipsoid
     */
    public double getVolumeEllipseUnit() {
        // major radius is DCmax, could be sqrt(main eigen value)
        double r1 = this.getRadiusMoments(2);
        double r2 = r1 / getMainElongation();
        double r3 = r2 / getMedianElongation();

        // 4/3PIabc
        return (4.18879 * r1 * r2 * r3);
    }

    /**
     * Constructor for the computeEigen object
     */
    protected void computeEigen() {
        if (eigen == null) {
            Matrix mat = new Matrix(3, 3);
            if (Double.isNaN(s200)) {
                computeMoments2(true);
            }
            mat.set(0, 0, s200);
            mat.set(0, 1, s110);
            mat.set(0, 2, s101);
            mat.set(1, 0, s110);
            mat.set(1, 1, s020);
            mat.set(1, 2, s011);
            mat.set(2, 0, s101);
            mat.set(2, 1, s011);
            mat.set(2, 2, s002);

            eigen = new EigenvalueDecomposition(mat);
        }
    }

    /**
     * Gets the main axes vectors as a matrix
     *
     * @return The matrix, e1 is main axis
     */
    public Matrix getMatrixAxes() {
        computeEigen();

        // swap e1 and e3 because e3 main axis
        Matrix M = (eigen.getV()).copy();
        double tmp;
        // swap e1 and e3
        tmp = M.get(0, 2);
        M.set(0, 2, M.get(0, 0));
        M.set(0, 0, tmp);
        tmp = M.get(1, 2);
        M.set(1, 2, M.get(1, 0));
        M.set(1, 0, tmp);
        tmp = M.get(2, 2);
        M.set(2, 2, M.get(2, 0));
        M.set(2, 0, tmp);

        return M;
    }

    /**
     * Gets the valueAxis attribute of the Object3D object (unit)
     *
     * @param order the order of the value
     * @return The valueAxis value
     */
    public double getValueAxis(int order) {
        computeEigen();

        double[] evalues = eigen.getRealEigenvalues();

        return evalues[order];
    }

    /**
     * Gets the mainAxis attribute of the Object3D object (unit)
     *
     * @return The mainAxis value
     */
    public Vector3D getMainAxis() {
        return getVectorAxis(2);
    }

    /**
     * Gets the Axis attribute of the Object3D object (unit)
     *
     * @param order the order of the axis
     * @return The mainAxis value
     */
    public Vector3D getVectorAxis(int order) {
        computeEigen();
        Matrix evect = eigen.getV();

        return new Vector3D(evect.get(0, order), evect.get(1, order), evect.get(2, order));
    }

    /**
     * @param order
     * @return
     */
    public double getRadiusMoments(int order) {
        return Math.sqrt(5.0 * getValueAxis(order));
    }

    /**
     * Gets the main elongation of the object
     *
     * @return The main elongation
     */
    public double getMainElongation() {
        // ratio between high and median value
        if (getValueAxis(1) != 0) {
            return Math.sqrt((getValueAxis(2) / getValueAxis(1)));
        } else {
            return Double.NaN;
        }
    }

    /**
     * Gets the flatness of the object
     *
     * @return The median elongation (flatness)
     */
    public double getMedianElongation() {
        // ratio between median and low value
        if (getValueAxis(0) != 0) {
            return Math.sqrt((getValueAxis(1) / getValueAxis(0)));
        } else {
            return Double.NaN;
        }
    }

    /**
     * The contours is given in an image (see OC3D)
     *
     * @param ima the image with labelled contours
     */
    public void computeContours(ImageHandler ima) {
        areaNbVoxels = 0;
        contours = new LinkedList<Voxel3D>();
        for (int k = zmin; k <= zmax; k++) {
            for (int j = ymin; j <= ymax; j++) {
                for (int i = xmin; i <= xmax; i++) {
                    if (ima.getPixel(i, j, k) == value) {
                        areaNbVoxels++;
                        Voxel3D temp = new Voxel3D(i, j, k, value);
                        contours.add(temp);
                    }
                }
            }
        }
    }

    /**
     * the minimum distance between center and contours
     *
     * @return the min distance(in real unit)
     */
    public double getDistCenterMin() {
        if (Double.isNaN(distCenterMinUnit)) {
            this.computeDistCenter();
        }

        return distCenterMinUnit;
    }

    /**
     * the maximum distance between center and contours
     *
     * @return the max distance(in real unit)
     */
    public double getDistCenterMax() {
        if (Double.isNaN(distCenterMaxUnit)) {
            this.computeDistCenter();
        }

        return distCenterMaxUnit;
    }

    /**
     * the average distance between center and contours
     *
     * @return the mean distance(in real unit)
     */
    public double getDistCenterMean() {
        if (Double.isNaN(distCenterMeanUnit)) {
            this.computeDistCenter();
        }

        return distCenterMeanUnit;
    }

    /**
     * the sigma value for distances between center and contours
     *
     * @return the SD distance
     */
    public double getDistCenterSigma() {
        if (Double.isNaN(distCenterSigmaUnit)) {
            this.computeDistCenter();
        }

        return distCenterSigmaUnit;
    }

    /**
     * the minimum distance between center and contours
     *
     * @return the min distance(in pixel)
     */
    public double getDistCenterMinPixel() {
        if (Double.isNaN(distCenterMinPixel)) {
            this.computeDistCenter();
        }

        return distCenterMinPixel;
    }

    /**
     * the maximum distance between center and contours
     *
     * @return the max distance(in pixel)
     */
    public double getDistCenterMaxPixel() {
        if (Double.isNaN(distCenterMaxPixel)) {
            this.computeDistCenter();
        }

        return distCenterMaxPixel;
    }

    /**
     * the average distance between center and contours
     *
     * @return the mean distance(in real unit)
     */
    public double getDistCenterMeanPixel() {
        if (Double.isNaN(distCenterMeanPixel)) {
            this.computeDistCenter();
        }

        return distCenterMeanPixel;
    }

    /**
     * the sigma value for distances between center and contours
     *
     * @return the SD distance
     */
    public double getDistCenterSigmaPixel() {
        if (Double.isNaN(distCenterSigmaPixel)) {
            this.computeDistCenter();
        }

        return distCenterSigmaPixel;
    }

    /**
     * @return
     */
    public boolean centerInside() {
        Point3D P = this.getCenterAsPoint();
        return inside(P.getX(), P.getY(), P.getZ());
    }

    /**
     * compute the distances between center and contour
     */
    private void computeDistCenter() {
        if (centerInside()) {
            // pixel
            double dist2Pix;
            double distmaxPix = 0;
            double distminPix = Double.POSITIVE_INFINITY;
            double distsumPix = 0;
            double distsum2Pix = 0;
            // unit
            double dist2;
            double distmax = 0;
            double distmin = Double.POSITIVE_INFINITY;
            double distsum = 0;
            double distsum2 = 0;
            double rx2 = resXY * resXY;
            double rz2 = resZ * resZ;
            Vector3D vc = getCenterAsVector();
            Voxel3D center = new Voxel3D(vc.getX(), vc.getY(), vc.getZ(), 0);
            double ccx = center.getX();
            double ccy = center.getY();
            double ccz = center.getZ();

            LinkedList<Voxel3D> cont = this.getContours();

            int s = getContours().size();
            //Voxel3D tmpmin = null;

            for (Voxel3D p2:cont) {
                // pixel
                dist2Pix = ((ccx - p2.getX()) * (ccx - p2.getX()))+ ((ccy - p2.getY()) * (ccy - p2.getY())) +((ccz - p2.getZ()) * (ccz - p2.getZ()));
                distsumPix += Math.sqrt(dist2Pix);
                distsum2Pix += dist2Pix;
                if (dist2Pix > distmaxPix) {
                    distmaxPix = dist2Pix;
                }
                if (dist2Pix < distminPix) {
                    distminPix = dist2Pix;
                }
                // unit
                dist2 = rx2 * ((ccx - p2.getX()) * (ccx - p2.getX()) + ((ccy - p2.getY()) * (ccy - p2.getY()))) + rz2 * (ccz - p2.getZ()) * (ccz - p2.getZ());
                distsum += Math.sqrt(dist2);
                distsum2 += dist2;
                if (dist2 > distmax) {
                    distmax = dist2;
                }
                if (dist2 < distmin) {
                    distmin = dist2;
                }

            }
            // unit
            distCenterMaxUnit = Math.sqrt(distmax);
            distCenterMinUnit = Math.sqrt(distmin);
            distCenterMeanUnit = distsum / (double) s;
            distCenterSigmaUnit = Math.sqrt((distsum2 - ((distsum * distsum) / (double) s)) / ((double) s - 1));
            // pixel
            distCenterMaxPixel = Math.sqrt(distmaxPix);
            distCenterMinPixel = Math.sqrt(distminPix);
            distCenterMeanPixel = distsumPix / (double) s;
            distCenterSigmaPixel = Math.sqrt((distsum2Pix - ((distsumPix * distsumPix) / (double) s)) / ((double) s - 1));
        }
    }

    /**
     * Gets the feret attribute of the Object3D (unit)
     */
    private void computeFeret() {
        double distmax = 0;
        double dist;
        double rx2 = resXY * resXY;
        double rz2 = resZ * resZ;
        Voxel3D p1;
        Voxel3D p2;
        LinkedList<Voxel3D> cont = this.getContours();

        int s = cont.size();
        // case object only one voxel
        if (s == 1) {
            feret1 = cont.get(0);
            feret2 = cont.get(0);
            feret = 0;
        }

        Voxel3D[] voxel3DS = new Voxel3D[cont.size()];
        voxel3DS = cont.toArray(voxel3DS);

        for (int i1 = 0; i1 < voxel3DS.length; i1++) {
            IJ.showStatus("Feret " + i1 + "/" + voxel3DS.length);
            p1 = voxel3DS[i1];
            for (int i2 = i1 + 1; i2 < voxel3DS.length; i2++) {
                p2 = voxel3DS[i2];
                dist = rx2 * ((p1.getX() - p2.getX()) * (p1.getX() - p2.getX()) + ((p1.getY() - p2.getY()) * (p1.getY() - p2.getY()))) + rz2 * (p1.getZ() - p2.getZ()) * (p1.getZ() - p2.getZ());
                if (dist > distmax) {
                    distmax = dist;
                    feret1 = p1;
                    feret2 = p2;
                }
            }
            i1++;
        }
        feret = (float) Math.sqrt(distmax);

        voxel3DS = null;
    }

    /**
     * Gets the feret diameter of the object (unit)
     *
     * @return The feret value
     */
    public double getFeret() {
        if (Double.isNaN(feret)) {
            computeFeret();
        }

        return feret;
    }

    /**
     * Gets the first feret voxel of the object (unit)
     *
     * @return The feret voxel
     */
    public Voxel3D getFeretVoxel1() {
        if (feret1 == null) {
            computeFeret();
        }
        return feret1;
    }

    /*
     * public DBObject toDB(int rank) { DBObject output = new
     * BasicDBObject("rank", rank);
     *
     * output.put("centerX", this.getCenterX()); output.put("centerY",
     * this.getCenterY()); output.put("centerZ", this.getCenterZ()); return
     * output; }
     *
     */

    /**
     * Gets the second feret voxel of the object (unit)
     *
     * @return The feret voxel
     */
    public Voxel3D getFeretVoxel2() {
        if (feret2 == null) {
            computeFeret();
        }
        return feret2;
    }

    /**
     * Display information
     *
     * @return text
     */
    @Override
    public String toString() {
        return ("Object3D : " + value + " (" + (int) (bx + 0.5) + "," + (int) (by + 0.5) + "," + (int) (bz + 0.5) + ")");
    }

    /**
     * the minimum distance between two objets (in real distance) computed on
     * the contours pixels
     *
     * @param other the other Object3D
     * @return the minimum distance
     */
    public double distBorderUnit(Object3D other) {
        return vectorBorderBorder(other).getLength(resXY, resZ);
    }

    public double distHausdorffUnit(Object3D other) {
        double dist = 0;
        for (Voxel3D voxel3D : getContours()) {
            double d = other.distPixelBorderUnit(voxel3D.getX(), voxel3D.getY(), voxel3D.getZ());
            if (d > dist) {
                dist = d;
            }
        }

        return dist;
    }

    /**
     * the minimum distance between two objets (in real distance) computed on
     * the contours pixels
     *
     * @param other the other Object3D
     * @return the minimum distance
     */
    public double distBorderPixel(Object3D other) {
        return vectorBorderBorder(other).getLength(1, 1);
    }

    /**
     * The border to border distance along a direction (EXPERIMENTAL)
     *
     * @param point0
     * @param other    the other object
     * @param opposite
     * @param point1
     * @return the border to border distance
     */
    public double distBorderUnit(Point3D point0, Object3D other, Point3D point1, boolean opposite) {
        Vector3D V = this.vectorBorderBorder(point0, other, point1, opposite);
        if (V != null) {
            return V.getLength(resXY, resZ);
        } else {
            return Double.NaN;
        }
    }

    /**
     * distance from center to center (in real distance)
     *
     * @param autre the other Object3D (has the same resolution !)
     * @return distance
     */
    public double distCenterUnit(Object3D autre) {
        return Math.sqrt((bx - autre.bx) * (bx - autre.bx) * resXY * resXY + (by - autre.by) * (by - autre.by) * resXY * resXY + (bz - autre.bz) * (bz - autre.bz) * resZ * resZ);
    }

    /**
     * distance from center to center (in pixel)
     *
     * @param other the other Object3D
     * @return distance
     */
    public double distCenterPixel(Object3D other) {
        return Math.sqrt((bx - other.bx) * (bx - other.bx) + (by - other.by) * (by - other.by)  + (bz - other.bz) * (bz - other.bz));
    }



    /**
     * 2D distance from center to center (in real distance)
     *
     * @param autre the other Object3D
     * @return distance
     */
    public double distCenter2DUnit(Object3D autre) {
        return Math.sqrt((bx - autre.bx) * (bx - autre.bx) * resXY * resXY + (by - autre.by) * (by - autre.by) * resXY * resXY);
    }

    /**
     * Distance center of this object to the border of the other
     *
     * @param autre Other object
     * @return Min distance between center and border ot other object
     */
    public double distCenterBorderUnit(Object3D autre) {
        return vectorCenterBorder(autre).getLength(resXY, resZ);
    }

    /**
     * Distance from a point to the border of the object
     *
     * @param x x coordinate of the point
     * @param y y coordinate of the point
     * @param z z coordinate of the point
     * @return The shortest distance
     */

    /**
     * @param P
     * @return
     */
    public double distPixelBorder(Point3D P) {
        return vectorPixelBorder(P.getX(), P.getY(), P.getZ()).getLength(resXY, resZ);
    }

    /**
     * Distance from a point to the border of the object
     *
     * @param x x coordinate of the point
     * @param y y coordinate of the point
     * @param z z coordinate of the point
     * @return The shortest distance
     */
    public double distPixelBorderUnit(double x, double y, double z) {
        return vectorPixelBorder(x, y, z).getLength(resXY, resZ);
    }

    /**
     * Distance from a point along a direction to the border of the object
     *
     * @param x x coordinate of the point in pixel
     * @param y y coordinate of the point in pixel
     * @param z z coordinate of the point in pixel
     * @param V the direction vector
     * @return The shortest distance in unit
     */
    public double distPixelBorderUnit(double x, double y, double z, Vector3D V) {
        //IJ.log("dist pix border " + x + " " + y + " " + z + " " + V);
        Vector3D res = vectorPixelBorder(x, y, z, V);
        if (res != null) {
            return res.getLength(resXY, resZ);
        } else {
            return Double.NaN;
        }
    }

    /**
     * The distance between center and a point with coordinates
     *
     * @param x x-coordinate of the point
     * @param y y-coordinate of the point
     * @param z z-coordinate of the point
     * @return the distance (in unit)
     */
    public double distPixelCenter(double x, double y, double z) {
        return Math.sqrt((bx - x) * (bx - x) * resXY * resXY + (by - y) * (by - y) * resXY * resXY + (bz - z) * (bz - z) * resZ * resZ);
    }

    /**
     * The distance between center and a Point3D
     *
     * @param P the point
     * @return the distance (in unit)
     */
    public double distPixelCenter(Point3D P) {
        return Math.sqrt((bx - P.x) * (bx - P.x) * resXY * resXY + (by - P.y) * (by - P.y) * resXY * resXY + (bz - P.z) * (bz - P.z) * resZ * resZ);
    }

    /**
     * Gets the touchBorders attribute of the Object3D Object3D
     *
     * @return The touchBorders value
     */
//    public boolean getTouchBorders() {
//        return touchBorders;
//    }

    /**
     * Gets the centerUnit attribute of the Object3D object
     *
     * @return The centerUnit value
     */
    public Vector3D getCenterUnit() {
        Vector3D ce = getCenterAsVector();
        return new Vector3D(ce.getX() * resXY, ce.getY() * resXY, ce.getZ() * resZ);
    }

    /**
     * @param vox
     * @return
     */
    public boolean isContour(Voxel3D vox) {
        if (contours == null) {
            this.computeContours();
        }
        for (Voxel3D v : contours) {
            if (v.distBlock(vox) < 0.001) {
                return true;
            }
        }
        return false;
    }

    /**
     * test if the point is inside the bounding box of the object
     *
     * @param x x coordinate of the point
     * @param y y coordinate of the point
     * @param z z coordinate of the point
     * @return true or false
     */
    public boolean insideBounding(double x, double y, double z) {
        return (x >= xmin) && (x <= xmax) && (y >= ymin) && (y <= ymax) && (z >= zmin) && (z <= zmax);
    }

    /**
     * test if the point is inside the bounding box of the object
     *
     * @param x x coordinate of the point
     * @param y y coordinate of the point
     * @param z z coordinate of the point
     * @return true or false
     */
    public boolean insideBounding(float x, float y, float z) {
        return (x >= xmin) && (x <= xmax) && (y >= ymin) && (y <= ymax) && (z >= zmin) && (z <= zmax);
    }

    /**
     * @param x
     * @param y
     * @param z
     * @param rx
     * @param ry
     * @param rz
     * @return
     */
    public boolean insideBounding(double x, double y, double z, int rx, int ry, int rz) {
        return (x >= xmin - rx) && (x <= xmax + rx) && (y >= ymin - ry) && (y <= ymax + ry) && (z >= zmin - rz) && (z <= zmax + rz);
    }

    public boolean inside(Point3D P) {
        return inside(P.getX(), P.getY(), P.getZ());
    }

    public boolean insideOne(ArrayList<Point3D> markers) {
        for (Point3D marker : markers) {
            if (inside(marker)) return true;
        }

        return false;
    }

    public boolean insideAll(ArrayList<Point3D> markers) {
        for (Point3D marker : markers) {
            if (!inside(marker)) return false;
        }

        return true;
    }

    /**
     * test if the point is inside the object
     *
     * @param x x-coordinate of the point
     * @param y y-coordinate of the point
     * @param z z-coordinate of the point
     * @return true is point inside object
     */
    public synchronized boolean inside(double x, double y, double z) {
        //IJ.log("testing inside " + x + " " + y + " " + z);
        if (!insideBounding(x, y, z, 1, 1, 1)) {
            //IJ.log(" outside bounding");
            return false;
        }
        int val = value;
        if (val == 0) {
            val = 1;
        }
        ImageHandler label = this.getLabelImage();
        int testX = (int) Math.round(x) - label.offsetX;
        int testY = (int) Math.round(y) - label.offsetY;
        int testZ = (int) Math.round(z) - label.offsetZ;
        if ((testX < 0) || (testY < 0) || (testZ < 0) || (testX >= label.sizeX) || (testY >= label.sizeY) || (testZ >= label.sizeZ)) {
            return false;
        }
        return (label.getPixel(testX, testY, testZ) == val);

//        if (miniLabelImage == null) {
//            miniLabelImage = this.createSegImageMini(val, 1);
//        } else {
//            // ERROR SOMETIMES IN TANGO (DO NOT UNDERSTAND WHY, SYNCHRONIZED ?)
//            if (miniLabelImage.sizeZ < (getZmax() - getZmin())) {
//                System.out.println("Pb size seg image ");
//                miniLabelImage = this.createSegImageMini(val, 1);
//            }
//        }
//        //miniLabelImage = this.createSegImageMini(val, 1);
//        // debug
//        int xmi = miniLabelImage.offsetX;
//        int ymi = miniLabelImage.offsetY;
//        int zmi = miniLabelImage.offsetZ;
//        //IJ.log(" " + xmi + " " + ymi + " " + zmi + " " + getXmax() + " " + getYmax() + " " + getZmax());
//        //IJ.log("" + miniLabelImage.sizeX + " " + miniLabelImage.sizeY + " " + miniLabelImage.sizeZ);
//        //miniLabelImage.show("minseg " + x + " " + y + " " + z);
//        int testx = (int) Math.round(x - xmi);
//        int testy = (int) Math.round(y - ymi);
//        int testz = (int) Math.round(z - zmi);
//        if ((testx < 0) || (testy < 0) || (testz < 0) || (testx >= miniLabelImage.sizeX) || (testy >= miniLabelImage.sizeY) || (testz >= miniLabelImage.sizeZ)) {
//            return false;
//        }
//        if (miniLabelImage.getPixel(testx, testy, testz) == val) {
//            return true;
//        }
    }

    /**
     * Test if the bouding boxes intersect
     *
     * @param autre the othe object
     * @return true or false
     */
    public boolean includesBox(Object3D autre) {
        int oxmin = autre.getXmin();
        int oxmax = autre.getXmax();
        int oymin = autre.getYmin();
        int oymax = autre.getYmax();
        int ozmin = autre.getZmin();
        int ozmax = autre.getZmax();
        return insideBounding(oxmin, oymin, ozmin) && insideBounding(oxmin, oymax, ozmin) && insideBounding(oxmax, oymin, ozmin) && insideBounding(oxmax, oymax, ozmin) && insideBounding(oxmin, oymin, ozmax) && insideBounding(oxmin, oymax, ozmax) && insideBounding(oxmax, oymin, ozmax) && insideBounding(oxmax, oymax, ozmax);
    }

    /**
     * @param other
     * @return
     */
    protected int[] getIntersectionBox(Object3D other) {
        int[] res = new int[6]; //xmin, xmax, ymin, ymax, zmin, zmax
        res[0] = Math.max(getXmin(), other.getXmin());
        res[1] = Math.min(getXmax(), other.getXmax());
        res[2] = Math.max(getYmin(), other.getYmin());
        res[3] = Math.min(getYmax(), other.getYmax());
        res[4] = Math.max(getZmin(), other.getZmin());
        res[5] = Math.min(getZmax(), other.getZmax());
        return res;
    }

    private boolean computeOverlapBox(Object3D autre) {
        int oxmin = autre.getXmin();
        int oxmax = autre.getXmax();
        int oymin = autre.getYmin();
        int oymax = autre.getYmax();
        int ozmin = autre.getZmin();
        int ozmax = autre.getZmax();

        boolean intersectX = ((xmax >= oxmin) && (oxmax >= xmin));
        boolean intersectY = ((ymax >= oymin) && (oymax >= ymin));
        boolean intersectZ = ((zmax >= ozmin) && (ozmax >= zmin));

        return (intersectX && intersectY && intersectZ);
    }

    /**
     * @param other
     * @return
     */
    public boolean overlapBox(Object3D other) {
        return this.computeOverlapBox(other);
    }

    /**
     * @param other
     * @return
     */
    public boolean disjointBox(Object3D other) {
        return !overlapBox(other);
    }

    /**
     * Create an intersection image around two objects
     *
     * @param other  the Other object
     * @param val1   the value for this object
     * @param val2   the value for other object
     * @param border the border around box
     * @return the image with 3 values (val1 for this, val2 for other, val1+val2
     * for intersection)
     */
    public ImageHandler createIntersectionImage(Object3D other, int val1, int val2, int border) {
        // keep label image
        ImageHandler label = this.getLabelImage();
        ImageHandler label2 = other.getLabelImage();
        // bounding box
        int xmi = Math.min(this.xmin, other.xmin) - border;
        if (xmi < 0) {
            xmi = 0;
        }
        int ymi = Math.min(this.ymin, other.ymin) - border;
        if (ymi < 0) {
            ymi = 0;
        }
        int zmi = Math.min(this.zmin, other.zmin) - border;
        if (zmi < 0) {
            zmi = 0;
        }
        int xma = Math.max(this.xmax, other.xmax) + border;
        int yma = Math.max(this.ymax, other.ymax) + border;
        int zma = Math.max(this.zmax, other.zmax) + border;

        // this will update label image and change offset
        ImageHandler imgThis = this.createSegImage(xmi, ymi, zmi, xma, yma, zma, val1);
        ImageHandler imgOther = other.createSegImage(xmi, ymi, zmi, xma, yma, zma, val2);

        //imgThis.show();
        //imgOther.show();
        ImageHandler addImage = imgThis.addImage(imgOther,1,1);
        addImage.offsetX = xmi;
        addImage.offsetY = ymi;
        addImage.offsetZ = zmi;
        imgThis = null;
        imgOther = null;
        System.gc();

        // put old label back
        labelImage = label;
        other.labelImage = label2;

        return addImage;
    }

    /**
     * Create an intersection image around two objects (with no extra borders)
     *
     * @param other the Other object
     * @param val1  the value for this object
     * @param val2  the value for other object
     * @return the image with 3 values (val1 for this, val2 for other, val1+val2
     * for intersection)
     */
    public ImageHandler createIntersectionImage(Object3D other, int val1, int val2) {
        return createIntersectionImage(other, val1, val2, 0);

    }

    /**
     * Get the intersection voxels as a 3D object
     *
     * @param other The other object
     * @return The object with intersection voxels (or null if objects are
     * disjoint)
     */
    public Object3DVoxels getIntersectionObject(Object3D other) {
        if (disjointBox(other)) {
            return null;
        }
        ImageHandler inter = this.createIntersectionImage(other, 1, 2);
        Object3DVoxels obj = new Object3DVoxels(inter, 3);
        obj.setValue(this.getValue());
        obj.translate(inter.offsetX, inter.offsetY, inter.offsetZ);
        // clean
        inter = null;

        return obj;
    }

    /**
     * Get the contact surfaces between two objects, outside voxels < dist and
     * number of border voxels of this object included in the objectB
     *
     * @param other    the objectB object
     * @param dist_max distance max (in pixel) between two contour points of the
     *                 two objects
     * @return int array with : nb ofcontours points below distance max to
     * contours points in objectB object AND nb of voxel of this object inside the
     * objectB one
     */
    public int[] surfaceContact(Object3D other, double dist_max) {
        // check distance border-border, if BB > dist_max no contact surface
        double distbb = this.distBorderPixel(other);
        if (distbb > dist_max) {
            return new int[]{0, 0};
        }
        // find intersection between two dilated objects
        float radius = (float) dist_max;
        Object3D dilA = this.getDilatedObject(radius, radius, radius);
        Object3D dilB = other.getDilatedObject(radius, radius, radius);
        Object3D inter = dilA.getIntersectionObject(dilB);
        // new objectA = this intersects inter
        // new objectB = other intersects inter
        Object3D objectA = inter.getIntersectionObject(this);
        Object3D objectB = inter.getIntersectionObject(other);
        if ((objectA == null) || (objectB == null)) {
            IJ.log("No intersection");
            return new int[]{0, 0};
        }
        int surfPos;
        int surfNeg = 0;
        Voxel3D contourA;
        Voxel3D contourB;
        double dist;
        int sizeA = objectA.getContours().size();
        LinkedList<Voxel3D> contoursA = objectA.getContours();
        LinkedList<Voxel3D> contoursB = objectB.getContours();
        int sizeB = contoursB.size();
        double dmax2 = dist_max * dist_max;
        double[][] distres = new double[sizeA][sizeB];
        double dmin;
        int closestB;

        for (int iA = 0; iA < sizeA; iA++) {
            for (int iB = 0; iB < sizeB; iB++) {
                distres[iA][iB] = -1;
            }
        }

        // FIXME use kd-tree for optimisation
        for (int iA = 0; iA < sizeA; iA++) {
            contourA = contoursA.get(iA);
            closestB = -1;
            // if voxel inside objectB object, count it for negative contact
            if (objectB.inside(contourA)) {
                surfNeg++;
                continue;
            }
            dmin = dmax2;
            for (int j = 0; j < sizeB; j++) {
                contourB = contoursB.get(j);
                dist = contourA.distanceSquare(contourB);
                if (dist <= dmin) {
                    dmin = dist;
                    closestB = j;
                }
            }
            if (closestB != -1) {
                distres[iA][closestB] = dmin;
            }
        }
        // count nb pix from 2 --> 1 having min
        surfPos = 0;
        for (int j = 0; j < sizeB; j++) {
            closestB = 0;
            while ((closestB < sizeA) && (distres[closestB][j]) == -1) {
                closestB++;
            }
            if (closestB < sizeA) {
                surfPos++;
            }
        }
        return new int[]{surfPos, surfNeg};
    }

    /**
     * @param other         object
     * @param distSquareMax object maximum square distance between two voxels (0
     *                      for coloc, 1 for side contact, >1 for diagonal contact
     * @return nb ofcontours voxels of first object at a distance inferior to
     * dist_square_max in pixels
     */
    public int edgeContact(Object3D other, int distSquareMax) {
        HashSet<Voxel3D> voxSet = new HashSet<Voxel3D>();
        for (Voxel3D v1 : this.getContours()) {
            for (Voxel3D v2 : other.getContours()) {
                if (v1.distanceSquare(v2) <= distSquareMax) {
                    voxSet.add(v1);
                }
            }
        }
        return voxSet.size();
    }

    public boolean edgeImage(ImageHandler img, boolean edgeXY, boolean edgeZ) {
        if (edgeXY) {
            if ((xmin <= 0) || (ymin <= 0) || (xmax >= img.sizeX - 1) || (ymax >= img.sizeY - 1)) {
                return true;
            }
        }
        if (edgeZ) {
            return (zmin <= 0) || (zmax >= img.sizeZ - 1);
        }

        return false;
    }

    /**
     * the radius of the object towards another object
     *
     * @param obj the other object
     * @return the radius (in real units)
     */
    public double radiusCenter(Object3D obj) {
        return radiusCenter(obj, false);
    }

    /**
     * the radius of the object towards another object, in forward or opposite
     * direction
     *
     * @param obj      the other object
     * @param opposite opposite or forward direction
     * @return the radius (in real units)
     */
    public double radiusCenter(Object3D obj, boolean opposite) {
        Vector3D VV;
        if (opposite) {
            VV = new Vector3D(obj.getCenterAsPoint(), this.getCenterAsPoint());
        } else {
            VV = new Vector3D(this.getCenterAsPoint(), obj.getCenterAsPoint());
        }
        if (VV.getLength() == 0) {
            return 0;
        }
        return distPixelBorderUnit(getCenterX(), getCenterY(), getCenterZ(), VV);
    }

    /**
     * the radius along a direction
     *
     * @param V the direction
     * @return the radius (in real units)
     */
    public double radiusCenter(Vector3D V) {
        Vector3D VV = getCenterAsVector().add(V, -1, 1);
        return distPixelBorderUnit(getCenterX(), getCenterY(), getCenterZ(), VV);
    }

    /**
     * the radius towards a point
     *
     * @param x the x-coordinate of the point
     * @param y the y-coordinate of the point
     * @param z the z-coordinate of the point
     * @return the radius (in real units)
     */
    public double radiusPixel(double x, double y, double z) {
        Vector3D V = getCenterAsVector().add(new Vector3D(x, y, z), -1, 1);
        return distPixelBorderUnit(getCenterX(), getCenterY(), getCenterZ(), V);
    }

    /**
     * Gets the areaUnit attribute of the Object3D object
     *
     * @param other
     * @return The areaUnit value
     */
    public Vector3D vectorBorderBorder(Object3D other) {
        Voxel3D[] voxs = this.VoxelsBorderBorder(other);

        return new Vector3D(voxs[0], voxs[1]);
    }

    /**
     * Return the two voxels defining the border-border distance
     *
     * @param other The other object
     * @return The two voxels(from this object, from other object)
     */
    public Voxel3D[] VoxelsBorderBorder(Object3D other) {
        double distanceMinimum = Double.MAX_VALUE;
        Voxel3D otherBorder = null, thisBorder = null;
        KDTreeC tree = this.getKdtreeContours();

        for (Voxel3D otherVoxel : other.getContours()) {
            double[] pos = otherVoxel.getArray();
            Item item = tree.getNearestNeighbor(pos, 1)[0];
            if (item.distanceSq < distanceMinimum) {
                otherBorder = otherVoxel;
                thisBorder = (Voxel3D) item.obj;
                distanceMinimum = item.distanceSq;
            }
        }

        return new Voxel3D[]{thisBorder, otherBorder};
    }

    /**
     * The vector between V0 and V1 : V0 is the point on border of object from
     * point0 along direction V1 is the point on border of other object from
     * point1 along opposite direction
     *
     * @param point0   the first point on this object
     * @param other    the other object
     * @param point1   the second point on other object
     * @param opposite
     * @return The difference vector between the two border points
     */
    public Vector3D vectorBorderBorder(Point3D point0, Object3D other, Point3D point1, boolean opposite) {
        Vector3D dir = new Vector3D(point0, point1);
        Vector3D tmp = this.vectorPixelBorder(point0.getX(), point0.getY(), point0.getZ(), dir);
        Vector3D V0;
        if (tmp != null) {
            V0 = point0.getVector3D().add(tmp);
        } else {
            return null;
        }
        if (opposite) {
            dir = dir.multiply(-1);
        }
        tmp = other.vectorPixelBorder(point1.getX(), point1.getY(), point1.getZ(), dir);
        Vector3D V1;
        if (tmp != null) {
            V1 = point1.getVector3D().add(tmp);
        } else {
            return null;
        }

        return new Vector3D(V0, V1);
    }

    /**
     * The vector between the center of this object and the border of the other
     * object
     *
     * @param other the othe object
     * @return The difference vector between the two closest points
     */
    public Vector3D vectorCenterBorder(Object3D other) {

        return other.vectorPixelBorder(this.getCenterAsVector());
    }

    /**
     * Vector between a voxel defined by a vector and the closest border
     *
     * @param V The 3D point defined as a vector
     * @return the vector
     */
    public Vector3D vectorPixelBorder(Vector3D V) {
        return vectorPixelBorder(V.getX(), V.getY(), V.getZ());
    }

    public Voxel3D getPixelBorder(double x, double y, double z) {
        // USE KDTREE
        double[] pos = {x, y, z};
        Item item = getKdtreeContours().getNearestNeighbor(pos, 1)[0];
        //System.out.println("object:"+this.getValue()+" contour: "+this.contours.size()+ " item null?"+(item==null));

        return (Voxel3D) item.obj;
    }

    /**
     * Vector between a pixel and the closest border
     *
     * @param x x coordinate of the pixel
     * @param y y coordinate of the pixel
     * @param z z coordinate of the pixel
     * @return the vector
     */
    public Vector3D vectorPixelBorder(double x, double y, double z) {
        Voxel3D vox = this.getPixelBorder(x, y, z);

        return new Vector3D(vox.getX() - x, vox.getY() - y, vox.getZ() - z);
    }

    /**
     * Vector from a point along a direction to the border of the object The
     * distance is the distance to the point where there is a change in the
     * object appartenance (inside / outside)
     *
     * @param x x coordinate of the point
     * @param y y coordinate of the point
     * @param z z coordinate of the point
     * @param V the direction vector
     * @return The shortest distance Vector
     */
    public Vector3D vectorPixelBorder(double x, double y, double z, Vector3D V) {
        Vector3D dir = new Vector3D(V);
        dir.normalize();
        float vx = (float) dir.getX();
        float vy = (float) dir.getY();
        float vz = (float) dir.getZ();
        float px;
        float py;
        float pz;
        px = (float) x;
        py = (float) y;
        pz = (float) z;
        float step = 0.5f;
        boolean in0 = this.inside((int) Math.round(x), (int) Math.round(y), (int) Math.round(z));
//        if (!in0) {
//            System.out.println("point " + x + " " + y + " " + z + " not inside object");
//        }
        boolean in = in0;
        while ((in == in0) && (insideBounding(px, py, pz, 1, 1, 1))) {
            px += step * vx;
            py += step * vy;
            pz += step * vz;
            in = this.inside(px, py, pz);
        }
        //IJ.log("in0 " + in0 + " in " + in + " px=" + px + " py=" + py + " pz=" + pz);
        if (in != in0) {
            return new Vector3D(px - 0.5 * step * vx - x, py - 0.5 * step * vy - y, pz - 0.5 * step * vz - z);
        } else {
            return null;
        }
    }

    /**
     * Vector from a point along a direction to the border of the object
     *
     * @param x x coordinate of the point
     * @param y y coordinate of the point
     * @param z z coordinate of the point
     * @param V the direction vector
     * @return The shortest distance Vector
     */
    public Point3D pointPixelBorder(double x, double y, double z, Vector3D V) {
        double dist;
        double distmin = Double.MAX_VALUE;
        Vector3D dir = new Vector3D(V);
        dir.multiply(resXY, resXY, resZ);
        dir.normalize();

        Voxel3D edge;
        Voxel3D stock = new Voxel3D();
        Vector3D here = new Vector3D(x, y, z);
        Vector3D rad;
        LinkedList<Voxel3D> cont = this.getContours();
        for (Voxel3D aCont : cont) {
            edge = aCont;
            rad = new Vector3D(here, edge);
            rad.multiply(resXY, resXY, resZ);
            dist = (1.0 - dir.colinear(rad));
            //System.out.println(" " + edge + " " + dir + " " + rad + " " + dist);
            if (dist < distmin) {
                distmin = dist;
                stock = edge;
            }
        }
        return new Point3D(stock);
    }

    /**
     * vector between a point and the border the vector is given is real units
     *
     * @param V the vector (to describe the point)
     * @return vector between the point and the border
     */
    public Vector3D vectorPixelUnitBorder(Vector3D V) {
        return vectorPixelUnitBorder(V.getX(), V.getY(), V.getZ());
    }

    /**
     * vector between a point and the border along a direction the point is
     * given is real units
     *
     * @param V   the vector (to describe the point)
     * @param dir the direction
     * @return vector between the point and the border
     */
    public Vector3D vectorPixelUnitBorder(Vector3D V, Vector3D dir) {
        return vectorPixelUnitBorder(V.getX(), V.getY(), V.getZ(), dir);
    }

    /**
     * vector between a pixel and the border the point is given is real units
     *
     * @param x the x-coordinate of the point
     * @param y the y-coordinate of the point
     * @param z the z-coordinate of the point
     * @return vector between the point and the border
     */
    public Vector3D vectorPixelUnitBorder(double x, double y, double z) {
        return vectorPixelBorder(x / resXY, y / resXY, z / resZ);
    }

    /**
     * vector between a pixel and the border, along a direction the vector is
     * given is real units
     *
     * @param x   the x-coordinate of the point
     * @param y   the y-coordinate of the point
     * @param z   the z-coordinate of the point
     * @param dir the direction
     * @return vector between the point and the border
     */
    public Vector3D vectorPixelUnitBorder(double x, double y, double z, Vector3D dir) {
        Vector3D dir2 = dir.multiply(1.0 / resXY, 1.0 / resXY, 1.0 / resZ);
        return vectorPixelBorder(x / resXY, y / resXY, z / resZ, dir2);
    }

    /**
     * Angle between (a, this, b) with real coordinates
     *
     * @param a Object3D a
     * @param b Object3D b
     * @return Angle in degrees
     */
    public double angle(Object3D a, Object3D b) {
        double OI = this.distCenterUnit(a);
        double OF = this.distCenterUnit(b);
        double IF = a.distCenterUnit(b);
        double cosangle = (IF * IF - OF * OF - OI * OI) / (-2.0 * OF * OI);
        return Math.toDegrees(Math.acos(cosangle));
    }

    /**
     * Gets the meanPixValue attribute of the Object3D object
     *
     * @param ima
     * @return The meanPixValue value
     */
    public double getPixMeanValue(ImageHandler ima) {
        if (volume > 0) {
            if ((currentQuantifImage == null) || (currentQuantifImage != ima)) {
                computeMassCenter(ima);
                currentQuantifImage = ima;
            }
            return meanDensity;
        } else {
            return Double.NaN;
        }
    }

    public double getPixMeanValueContour(ImageHandler ima) {
        if (volume > 0) {
            LinkedList<Voxel3D> contours = getContours();
            double sum = 0;
            for (Voxel3D voxel3D : contours) {
                sum += ima.getPixel(voxel3D);
            }
            return sum / contours.size();
        }
        return 0;

    }


    public double getPixMedianValue(ImageHandler ima) {
        if (volume > 0) {
            if ((currentQuantifImage == null) || (currentQuantifImage != ima)) {
                computeMassCenter(ima);
                currentQuantifImage = ima;
            }
            return listValues(ima).median();
        } else {
            return Double.NaN;
        }
    }

    public double getPixModeValue(ImageHandler ima) {
        if (volume > 0) {
            return listValues(ima).getMode();
        } else {
            return Double.NaN;
        }
    }

    public double getPixModeNonZero(ImageHandler ima) {
        if (volume > 0) {
            return listValues(ima).getModeNonZero();
        } else {
            return Double.NaN;
        }
    }

    /**
     * @param massCenter
     * @param objectMap
     * @param ima
     * @param radXY
     * @param radZ
     * @return
     */
    public double getMeanPixValueAroundBarycenter(boolean massCenter, ImageInt objectMap, ImageHandler ima, double radXY, double radZ) {
        int x, y, z;
        if (massCenter) {
            this.computeMassCenter(ima);
            x = (int) (cx + 0.5);
            y = (int) (cy + 0.5);
            z = (int) (cz + 0.5);
        } else {
            this.computeCenter();
            x = (int) (bx + 0.5);
            y = (int) (by + 0.5);
            z = (int) (bz + 0.5);
        }
        // TODO with any spherical kernel and with double barycenter
        double res = 0;
        int count = 0;
        for (int zz = z - 1; zz <= z + 1; zz++) {
            if (zz >= 0 && zz < ima.sizeZ) {
                for (int yy = y - 1; yy <= y + 1; yy++) {
                    if (yy >= 0 && yy < ima.sizeY) {
                        for (int xx = x - 1; xx <= x + 1; xx++) {
                            if (xx >= 0 && xx < ima.sizeX) {
                                if (objectMap.getPixelInt(xx, yy, zz) == this.getValue()) {
                                    res += ima.getPixel(xx, yy, zz);
                                    count++;
                                }
                            }
                        }
                    }
                }
            }
        }
        if (count > 0) {
            return res / (double) count;
        } else {
            return 0;
        }
    }

    /**
     * Gets the pixMaxValue attribute of the Object3D object
     *
     * @param ima
     * @return The pixMaxValue value
     */
    public double getPixMaxValue(ImageHandler ima) {
        if ((currentQuantifImage == null) || (currentQuantifImage != ima)) {
            computeMassCenter(ima);
            currentQuantifImage = ima;
        }
        return pixmax;
    }


    public double getPixCenterValue(ImageHandler ima) {
        if (ima.contains(getCenterX(), getCenterY(), getCenterZ())) {
            return ima.getPixel(getCenterAsPoint());
        } else {
            return Double.NaN;
        }
    }

    /**
     * Gets the pixMinValue attribute of the Object3D object
     *
     * @param ima
     * @return The pixMinValue value
     */
    public double getPixMinValue(ImageHandler ima) {
        if ((currentQuantifImage == null) || (currentQuantifImage != ima)) {
            computeMassCenter(ima);
            currentQuantifImage = ima;
        }
        return pixmin;
    }

    /**
     * Will count the number of different objects within this object     *
     *
     * @param ima A labelled image
     * @return the number of objects and the volume occupied by the objects
     */
    public int[] getNumbering(ImageHandler ima) {
        int vol = 0;
        int min = (int) this.getPixMinValue(ima);
        int max = (int) this.getPixMaxValue(ima);
        BitSet bitSet = new BitSet(max - min + 1);

        for (Voxel3D voxel3D : getVoxels()) {
            int x = voxel3D.getRoundX();
            int y = voxel3D.getRoundY();
            int z = voxel3D.getRoundZ();
            int pix = (int) ima.getPixel(x, y, z);
            if (pix > 0) {
                vol++;
                bitSet.set(pix - min);
            }
        }
        return new int[]{bitSet.cardinality(), vol};
    }

    public void resetQuantifImage() {
        currentQuantifImage = null;
    }

    /**
     * create a small labeleld image containing the object, limited to the
     * bounding box
     *
     * @param val the value to draw inside image
     * @param borderSize
     * @return the labelled image
     */
//    public ImageInt createSegImageMini(int val, int borderSize) {
//        return createSegImageMini(val, borderSize, borderSize, borderSize);
//    }
    /**
     *
     * @param val
     * @param borderSizeX
     * @param borderSizeY
     * @param borderSizeZ
     * @return
     */
//    public ImageInt createSegImageMini(int val, int borderSizeX, int borderSizeY, int borderSizeZ) {
//        int xm = this.getXmin() - borderSizeX;
//        if (xm < -borderSizeX) {
//            xm = -borderSizeX;
//        }
//        int ym = this.getYmin() - borderSizeY;
//        if (ym < -borderSizeY) {
//            ym = -borderSizeY;
//        }
//        int zm = this.getZmin() - borderSizeZ;
//        if (zm < -borderSizeZ) {
//            zm = -borderSizeZ;
//        }
//
//        int w = this.getXmax() - xm + 1 + borderSizeX;
//        int h = this.getYmax() - ym + 1 + borderSizeY;
//        int d = this.getZmax() - zm + 1 + borderSizeZ;
//        miniLabelImage = new ImageShort("Object_" + val, w, h, d);
//        Voxel3D vox;
//        int xx;
//        int yy;
//        int zz;
//        Iterator it = getVoxels().iterator();
//        while (it.hasNext()) {
//            vox = (Voxel3D) it.next();
//            xx = (int) Math.round(vox.getX() - xm);
//            yy = (int) Math.round(vox.getY() - ym);
//            zz = (int) Math.round(vox.getZ() - zm);
//            // TODO suface vertices may have coordinates < 0 if touching edges 
//            if (miniLabelImage.contains(xx, yy, zz)) {
//                miniLabelImage.setPixel(xx, yy, zz, val);
//            }
//        }
//
//        //miniSegImage.getImagePlus().show();
//        // set the offsets
//        // offset of minilable image within label image
////        miniLabelImage.offsetX = xm;
////        miniLabelImage.offsetY = ym;
////        miniLabelImage.offsetZ = zm;
//        // offset of object within label image
//        this.offX = xm;
//        this.offY = ym;
//        this.offZ = zm;
//        miniLabelImage.setScale((float) this.getResXY(), (float) this.getResZ(), this.getUnits());
//        //miniLabelImage.show("obj:"+this);
//        return miniLabelImage;
//    }

    /**
     * Gets the SD of pixels in object
     *
     * @param ima the 3D image
     * @return The sigma value
     */
    public double getPixStdDevValue(ImageHandler ima) {
        if ((currentQuantifImage == null) || (currentQuantifImage != ima)) {
            computeMassCenter(ima);
            currentQuantifImage = ima;
        }
        return sigma;
    }

    /**
     * @param val
     * @param borderSize
     * @return
     */
    public ImageInt createSegImageMini2D(int val, int borderSize) {
        int xm = this.getXmin() - borderSize;
        if (xm < -borderSize) {
            xm = -borderSize;
        }
        int ym = this.getYmin() - borderSize;
        if (ym < -borderSize) {
            ym = -borderSize;
        }
        int w = this.getXmax() - xm + 1 + borderSize;
        int h = this.getYmax() - ym + 1 + borderSize;
        miniLabelImage = new ImageShort("Object_" + val, w, h, 1);
        Voxel3D vox;
        double xx;
        double yy;
        for (Voxel3D o : getVoxels()) {
            vox = o;
            xx = vox.getX() - xm;
            yy = vox.getY() - ym;
            // TODO suface vertices may have coordinates < 0 if touching edges
            if (!miniLabelImage.contains(xx, yy, 0)) {
                System.out.println("outside miniseg " + xx + " " + yy);
            } else {
                miniLabelImage.setPixel((int) Math.round(xx), (int) Math.round(yy), 0, val);
            }
        }
        //miniSegImage.getImagePlus().show();
        // set the offsets
        miniLabelImage.offsetX = xm;
        miniLabelImage.offsetY = ym;
        miniLabelImage.setScale((float) this.getResXY(), (float) this.getResXY(), this.getUnits());
        return miniLabelImage;
    }

    /**
     * @return
     */
    private ImageHandler createSegImage() {
        // case value =0
        if (value != 0) {
            return createSegImage(xmin, ymin, zmin, xmax, ymax, zmax, value);
        } else {
            return createSegImage(xmin, ymin, zmin, xmax, ymax, zmax, 1);
        }
    }

    private ImageHandler createMaxSegImage(int val) {
        return createSegImage(0, 0, 0, xmax, ymax, zmax, val);
    }

    /**
     * @param xmi
     * @param ymi
     * @param zmi
     * @param xma
     * @param yma
     * @param zma
     * @param val
     * @return
     */
    public ImageHandler createSegImage(int xmi, int ymi, int zmi, int xma, int yma, int zma, int val) {
        ImageHandler segImage;
        if (val > 65535)
            segImage = new ImageFloat("Object", xma - xmi + 1, yma - ymi + 1, zma - zmi + 1);
        else segImage = new ImageShort("Object", xma - xmi + 1, yma - ymi + 1, zma - zmi + 1);
        // FIXME offset useful --> yes, see intersection image for mereo, and also for dilated object
        int offX = xmi;
        int offY = ymi;
        int offZ = zmi;
        Voxel3D vox;
        for (Voxel3D o : getVoxels()) {
            vox = o;
            if (segImage.contains(vox.getRoundX() - offX, vox.getRoundY() - offY, vox.getRoundZ() - offZ)) {
                segImage.setPixel(vox.getRoundX() - offX, vox.getRoundY() - offY, vox.getRoundZ() - offZ, val);
            }
        }
        // IJ.log("seg image " + offX + " " + offY + " " + offZ);
        segImage.setOffset(xmi, ymi, zmi);
        segImage.setScale(resXY, resZ, units);

        return segImage;
    }

    public ImageHandler createSegImage(int bx, int by, int bz) {
        return createSegImage(xmin - bx, ymin - by, zmin - bz, xmax + bx, ymax + by, zmax + bz, value);
    }

    /**
     * Get the list of voxels of the object
     *
     * @return the list of voxels
     */
    public abstract List<Voxel3D> getVoxels();

    /**
     * @param path
     */
    public abstract void saveObject(String path);

    protected void saveInfo(BufferedWriter bf) throws IOException {
        // calibration
        bf.write("cal=\t" + resXY + "\t" + resZ + "\t" + units + "\n");
        // comments
        if (!comment.isEmpty()) {
            bf.write("comment=\t" + comment + "\n");
        }
        // type
        if (type > 0) {
            bf.write("type=\t" + type + "\n");
        }
        // value
        bf.write("value=\t" + value + "\n");
    }

    // code copied from ImageJ 3D Viewer MCTriangulator

    protected String loadInfo(BufferedReader bf) throws IOException {
        String data = bf.readLine();
        String[] coord;
        while (data != null) {
            // calibration
            if (data.startsWith("cal=")) {
                coord = data.split("\t");
                resXY = Double.parseDouble(coord[1]);
                resZ = Double.parseDouble(coord[2]);
                units = coord[3];
            } else if (data.startsWith("comment=")) {
                coord = data.split("\t");
                comment = coord[1];
            } else if (data.startsWith("type=")) {
                coord = data.split("\t");
                type = Integer.parseInt(coord[1]);
            } else if (data.startsWith("value=")) {
                coord = data.split("\t");
                value = Integer.parseInt(coord[1]);
                if (value == 0) {
                    value = 1;
                }
            } else {
                break;
            }
            data = bf.readLine();
        }
        return data;
    }

    /**
     * @param calibrated
     * @return
     * @deprecated use Object3D-IJUtils
     */
    public List computeMeshSurface(boolean calibrated) {
        //IJ.showStatus("computing mesh");
        // use miniseg
        ImageHandler miniseg = this.getLabelImage();
        ImageByte miniseg8 = ((ImageShort) (miniseg)).convertToByte(false);
        ImagePlus objectImage = miniseg8.getImagePlus();
        if (calibrated) {
            objectImage.setCalibration(Object3D_IJUtils.getCalibration(this));
        }
        boolean[] bl = {true, true, true};
        Volume vol = new Volume(objectImage, bl);
        vol.setAverage(true);
        List l = MCCube.getTriangles(vol, 0);
        // needs to invert surface
        l = Object3DSurface.invertNormals(l);
        // translate object with units coordinates
        float tx, ty, tz;
        if (calibrated) {
            tx = (float) (miniseg.offsetX * resXY);
            ty = (float) (miniseg.offsetY * resXY);
            tz = (float) (miniseg.offsetZ * resZ);
        } else {
            tx = (float) (miniseg.offsetX);
            ty = (float) (miniseg.offsetY);
            tz = (float) (miniseg.offsetZ);
        }
        l = Object3DSurface.translateTool(l, tx, ty, tz);

        return l;
    }

    public boolean includesMarkersNone(ImageInt imageMarkers) {
        for (Voxel3D voxel3D : getVoxels()) {
            if (imageMarkers.contains(voxel3D)) {
                if (imageMarkers.getPixel(voxel3D) > 0) return false;
            }
        }
        return true;
    }

    public boolean includesMarkersOneOnly(ImageInt imageMarkers) {
        int label = -1;
        for (Voxel3D voxel3D : getVoxels()) {
            if (imageMarkers.contains(voxel3D)) {
                int pixel = imageMarkers.getPixelInt(voxel3D);
                if (pixel > 0) {
                    if (label == -1) label = pixel;
                    if ((label > 0) && (pixel != label)) return false;
                }
            }
        }
        return (label > 0);
    }

    public boolean includesMarkersOneMore(ImageInt imageMarkers) {
        for (Voxel3D voxel3D : getVoxels()) {
            if (imageMarkers.contains(voxel3D)) {
                int pixel = imageMarkers.getPixelInt(voxel3D);
                if (pixel > 0) {
                    return true;
                }
            }
        }
        return false;
    }

    public boolean includedInZonesNone(ImageInt imageZones) {
        for (Voxel3D voxel3D : getVoxels()) {
            if (imageZones.contains(voxel3D)) {
                if (imageZones.getPixel(voxel3D) > 0) return false;
            }
        }
        return true;
    }

    public boolean includedInZonesOneOnly(ImageInt imageZones) {
        int label = -1;
        for (Voxel3D voxel3D : getVoxels()) {
            if (imageZones.contains(voxel3D)) {
                int pixel = imageZones.getPixelInt(voxel3D);
                if (pixel == 0) return false;
                if (label == -1) label = pixel;
                else if (pixel != label) return false;
            }
        }
        return true;
    }


    public boolean includedInZonesOneMore(ImageInt imageZones) {
        int label = -1;
        for (Voxel3D voxel3D : getVoxels()) {
            if (imageZones.contains(voxel3D)) {
                int pixel = imageZones.getPixelInt(voxel3D);
                if (pixel == 0) return false;
            }
        }
        return true;
    }


    /**
     * @param obj
     * @return
     */
    public boolean includes(Object3D obj) {
        if (!this.includesBox(obj)) {
            return false;
        }

        // use coloc
        int co = this.getColoc(obj);
        //IJ.log("includes " + co + " " + obj.getVolumePixels());
        return co == obj.getVolumePixels();

//        ImageInt inter = this.createIntersectionImage(obj, 1, 2);
//        int[] hist = inter.getHistogram(new BlankMask(inter), 256, 0, 255);
//
//        return (hist[2] == 0);
//        ArrayList<Voxel3D> al1 = autre.getVoxels();
//        Voxel3D v1;
//        for (Iterator it1 = al1.iterator(); it1.hasNext();) {
//            v1 = (Voxel3D) it1.next();
//            IJ.showStatus("Testing includes " + v1);
//            if (!this.inside(v1.getX(), v1.getY(), v1.getZ())) {
//                return false;
//            }
//        }
    }

    private KDTreeC getKdtreeContours() {
        if (contours == null) {
            computeContours();
        }
        if (kdtreeContours == null) {
            if ((contours != null) && (!contours.isEmpty())) {
                kdtreeContours = new KDTreeC(3);
                kdtreeContours.setScale3(this.resXY, this.resXY, this.resZ);
                for (Voxel3D v : contours) {
                    kdtreeContours.add(v.getArray(), v);
                }
            }
        }
        return kdtreeContours;
    }

    /**
     * @param obj
     * @return
     */
    public abstract boolean hasOneVoxelColoc(Object3D obj);

    public abstract boolean hasOneVoxelValueRange(ImageHandler img, int t0, int t1);

    /**
     * @param obj
     * @return
     */
    public abstract int getColoc(Object3D obj);

    public boolean touchBorders(ImageHandler img, boolean Z) {
        int[] bb = getBoundingBox();
        // 0
        if ((bb[0] <= 0) || (bb[2] <= 0)) {
            return true;
        }
        if (Z && (bb[4] <= 0)) {
            return true;
        }
        // max
        if ((bb[1] >= img.sizeX - 1) || (bb[3] >= img.sizeY - 1)) {
            return true;
        }
        return Z && (bb[5] >= img.sizeZ - 1);
    }

    /**
     * @param img
     * @param Z
     * @return
     * @deprecated use Object3D-IJUtils
     */
    public boolean touchBorders(ImagePlus img, boolean Z) {
        int[] bb = getBoundingBox();
        // 0
        if ((bb[0] <= 0) || (bb[2] <= 0)) {
            return true;
        }
        if (Z && (bb[4] <= 0)) {
            return true;
        }
        // max
        if ((bb[1] >= img.getWidth() - 1) || (bb[3] >= img.getHeight() - 1)) {
            return true;
        }
        return Z && (bb[5] >= img.getNSlices() - 1);
    }

    private Object3DVoxels getMorphologicalObject(int op, float radX, float radY, float radZ) {
        // special case radii = 0
        if ((radX == 0) && (radY == 0) && (radZ == 0)) {
            return new Object3DVoxels(this);
        }

        // resize if DILATE or CLOSE
        if ((op == BinaryMorpho.MORPHO_DILATE) || (op == BinaryMorpho.MORPHO_CLOSE)) {
            labelImage = createSegImage((int) (radX + 1), (int) (radY + 1), (int) (radZ + 1));
        }

        ImageHandler segImage2;
        /// use fastFilter if rx != ry
        if ((radY != radX) || (radZ == 0)) {
            int filter = 0;
            if (op == BinaryMorpho.MORPHO_DILATE) {
                filter = FastFilters3D.MAX;
            } else if (op == BinaryMorpho.MORPHO_ERODE) {
                filter = FastFilters3D.MIN;
            } else if (op == BinaryMorpho.MORPHO_CLOSE) {
                filter = FastFilters3D.CLOSEGRAY;
            } else if (op == BinaryMorpho.MORPHO_OPEN) {
                filter = FastFilters3D.OPENGRAY;
            }
            segImage2 = FastFilters3D.filterImage(getLabelImage(), filter, radX, radY, radZ, 0, true);
            segImage2.setOffset(labelImage);
        } // else use binaryMorpho class (based on edm)
        else {
            if (op == BinaryMorpho.MORPHO_DILATE) // use enlarge image
                segImage2 = BinaryMorpho.binaryDilate(getLabelImage(), radX, radZ, 0, true);
            else
                segImage2 = BinaryMorpho.binaryMorpho(getLabelImage(), op, radX, radZ, 0);
        }
        if (segImage2 == null) {
            return null;
        }
        Object3DVoxels objMorpho = new Object3DVoxels(segImage2);
        objMorpho.translate(segImage2.offsetX, segImage2.offsetY, segImage2.offsetZ);
        objMorpho.setCalibration(resXY, resZ, units);

        return objMorpho;
    }

    // see landini's page for details
    // equals to its closing

    /**
     * @param radX
     * @param radY
     * @param radZ
     * @return
     */
    public boolean b_closed(float radX, float radY, float radZ) {
        Object3D closed = this.getClosedObject(radX, radY, radZ);
        MereoObject3D mereo = new MereoObject3D(this, closed);
        return mereo.Equality();
    }

    // see landini's page for details
    // equals to its opening

    /**
     * @param radX
     * @param radY
     * @param radZ
     * @return
     */
    public boolean b_open(float radX, float radY, float radZ) {
        Object3D open = this.getOpenedObject(radX, radY, radZ);
        MereoObject3D mereo = new MereoObject3D(this, open);
        return mereo.Equality();
    }

    // see landini's page for details
    // equals to its opening and closing

    /**
     * @param radX
     * @param radY
     * @param radZ
     * @return
     */
    public boolean regular(float radX, float radY, float radZ) {
        if (!b_open(radX, radY, radZ)) {
            return false;
        } else {
            return b_closed(radX, radY, radZ);
        }
    }

    /**
     * @param radX
     * @param radY
     * @param radZ
     * @return
     */
    public Object3DVoxels getDilatedObject(float radX, float radY, float radZ) {
        return getMorphologicalObject(BinaryMorpho.MORPHO_DILATE, radX, radY, radZ);
    }

    /**
     * @param radX
     * @param radY
     * @param radZ
     * @return
     */
    public Object3DVoxels getErodedObject(float radX, float radY, float radZ) {
        return getMorphologicalObject(BinaryMorpho.MORPHO_ERODE, radX, radY, radZ);
    }

    /**
     * @param radX
     * @param radY
     * @param radZ
     * @return
     */
    public Object3DVoxels getClosedObject(float radX, float radY, float radZ) {
        return getMorphologicalObject(BinaryMorpho.MORPHO_CLOSE, radX, radY, radZ);
    }

    /**
     * @param radX
     * @param radY
     * @param radZ
     * @return
     */
    public Object3DVoxels getOpenedObject(float radX, float radY, float radZ) {
        return getMorphologicalObject(BinaryMorpho.MORPHO_OPEN, radX, radY, radZ);
    }

    public Object3DVoxels getLayerObject(float r1, float r2) {
        Object3DVoxels obMax;
        if (r2 > 0) {
            obMax = getDilatedObject(r2, r2, r2);
        } else {
            obMax = getErodedObject(-r2, -r2, -r2);
        }
        Object3DVoxels obMin;
        if (r1 > 0) {
            obMin = getDilatedObject(r1, r1, r1);
        } else {
            obMin = getErodedObject(-r1, -r1, -r1);
        }

        // if large object use image
        obMax.substractObject(obMin);

        return obMax;
    }

    public Object3DVoxels getLayerEVFObject(ImageInt mask, float ratio) {
        ImageHandler dup = mask.createSameDimensions();
        this.draw(dup, 255);
        ImageFloat edt = EDT.run(dup, 128, (float) resXY, (float) resZ, true, 0);
        EDT.normalizeDistanceMap(edt, mask, true);
        ImageByte th = edt.thresholdRangeInclusive(0, ratio);
        Object3DVoxels res = new Object3DVoxels(th, 255);
        res.setCalibration(resXY, resZ, units);

        return res;
    }

    public Object3DVoxels getLayerEVFObject(Object3D mask, float ratio) {
        // mask should include this
        if (!mask.includesBox(this)) {
            return null;
        }
        //ImageInt img = mask.createSegImageMini(255, 1);
        ImageHandler img = mask.getLabelImage();
        ImageHandler dup = img.createSameDimensions();
        this.draw(dup, 255, -img.offsetX, -img.offsetY, -img.offsetZ);
        ImageFloat edt = EDT.run(dup, 128, (float) resXY, (float) resZ, true, 0);
        EDT.normalizeDistanceMap(edt, img, true);
        ImageByte th = edt.thresholdRangeInclusive(0, ratio);
        Object3DVoxels res = new Object3DVoxels(th);
        res.setCalibration(resXY, resZ, units);
        res.translate(img.offsetX, img.offsetY, img.offsetZ);

        return res;
    }

    public Object3DVoxels getEllipsoid() {
        Vector3D V = this.getVectorAxis(2);
        Vector3D W = this.getVectorAxis(1);
        double rad1 = this.getRadiusMoments(2);
        double rad2 = Double.NaN;
        if (!Double.isNaN(this.getMainElongation())) {
            rad2 = rad1 / this.getMainElongation();
        }
        double rad3 = Double.NaN;
        if (!Double.isNaN(this.getMedianElongation())) {
            rad3 = rad2 / this.getMedianElongation();
        }
        int radius = (int) (rad1 / resXY) + 1;
        ObjectCreator3D ellipsoid = new ObjectCreator3D(2 * radius, 2 * radius, 2 * radius);
        ellipsoid.setResolution(this.getResXY(), this.getResZ(), this.getUnits());
        ellipsoid.createEllipsoidAxesUnit(radius * resXY, radius * resXY, radius * resZ, rad1, rad2, rad3, 1, V, W, false);
        Object3DVoxels ell = new Object3DVoxels(ellipsoid.getImageHandler(), 1);
        ell.translate(this.getCenterX() - radius, this.getCenterY() - radius, this.getCenterZ() - radius);

        return ell;
    }

    /**
     * @return
     */

    public Object3DSurface getConvexSurface() {
        //IJ.showStatus("Computing convex surface");
        // compute surface if necessary
        Object3DSurface surf;
        if (this instanceof Object3DSurface) {
            surf = (Object3DSurface) this;
        } else {
            Object3DVoxels obj = this.getObject3DVoxels();
            // calibration not important for computing hull
            surf = new Object3DSurface(obj.computeMeshSurface(true));
        }
        // compute convex hull
        Object3DSurface conv = new Object3DSurface(surf.computeConvexHull3D());
        conv.setCalibration(this.getCalibration());
        conv.setValue(this.getValue());

        return conv;
    }


    /**
     * @param multi multiprocessor
     * @return
     */

    public Object3DVoxels getConvexObject(boolean multi) {
        //IJ.showStatus("Computing convex object");
        Object3DSurface conv = this.getConvexSurface();
        conv.multiThread = multi;
        LinkedList<Voxel3D> vox = conv.getVoxels();
        Object3DVoxels obj = new Object3DVoxels(vox);
        obj.setCalibration(this.getCalibration());
        obj.computeContours();

        return obj;
    }


    /**
     * @return
     */

    public Object3DVoxels getConvexObject() {
        return getConvexObject(true);
    }


    @Override
    public int compareTo(Object3D o) {
        if (this.compare < o.compare) {
            return -1;
        } else if (this.compare > o.compare) {
            return 1;
        } else {
            return 0;
        }
    }
}
