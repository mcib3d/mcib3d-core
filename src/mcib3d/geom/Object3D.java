package mcib3d.geom;

//import com.mongodb.BasicDBObject;
//import com.mongodb.DBObject;
import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.gui.Roi;
import ij.measure.Calibration;
import ij.process.ByteProcessor;
import ij3d.Volume;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import marchingcubes.MCCube;
import mcib3d.Jama.EigenvalueDecomposition;
import mcib3d.Jama.Matrix;
import mcib3d.image3d.ImageByte;
import mcib3d.image3d.ImageHandler;
import mcib3d.image3d.ImageInt;
import mcib3d.image3d.ImageShort;
import mcib3d.image3d.processing.BinaryMorpho;
import mcib3d.image3d.processing.FastFilters3D;
import mcib3d.utils.ArrayUtil;
import mcib3d.utils.KDTreeC;
import mcib3d.utils.KDTreeC.Item;

/**
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
 * package mcib3d.geom;
 *
 * /**
 * Abstract class for 3D objects, an abstract volume object is defined by some
 * contours, so distance analysis can be computed.
 *
 * @author thomas
 */
public abstract class Object3D {

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
    // center distances stat
    double distcentermin = Double.NaN;
    double distcentermax = Double.NaN;
    double distcentermean = Double.NaN;
    double distcentersigma = Double.NaN;
    /**
     * Integrated density (sum of pixels)
     */
    protected double integratedDensity = Double.NaN;
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
    protected int value;
    /**
     * Contours pixels
     */
    protected ArrayList<Voxel3D> contours = null;
    /**
     * kd-tree for the contour
     */
    protected KDTreeC kdtreeContours;
    /**
     * Touch the borders ?
     */
    protected boolean touchBorders;
    /**
     * Centred Moments order 2
     */
    public double s200 = Double.NaN;
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
     * The image where the object lies
     */
    protected ImageInt miniLabelImage = null;
    /**
     * label image, starts at 0,0
     */
    protected ImageInt labelImage = null;
    // the current image for quantification
    /**
     * current image used for quantification (to compute results once)
     */
    protected ImageHandler currentQuantifImage = null;
    /**
     * the resolution in XY
     */
    protected double resXY = 1;
    /**
     * the resolution in Z
     */
    protected double resZ = 1;
    /**
     * the unit for resolution
     */
    protected String units = "pixels";
    /**
     * use verbose mode (not used ?)
     */
    public boolean verbose = false;
    /**
     * use multithreading mode (not used ?)
     */
    public boolean multiThread = false;
    public static final byte MEASURE_NONE = 0;
    public static final byte MEASURE_VOLUME_PIX = 1;
    public static final byte MEASURE_VOLUME_UNIT = 2;
    public static final byte MEASURE_MAIN_ELONGATION = 3;
    public static final byte MEASURE_COMPACTNESS_VOXELS = 4;
    public static final byte MEASURE_COMPACTNESS_UNITS = 12;
    public static final byte MEASURE_SPHERICITY = 5;
    public static final byte MEASURE_AREA_PIX = 6;
    public static final byte MEASURE_AREA_UNIT = 7;
    public static final byte MEASURE_DC_AVG = 8;
    public static final byte MEASURE_DC_SD = 9;
    // with currentQuantifImage
    public static final byte MEASURE_INTENSITY_AVG = 10;
    public static final byte MEASURE_INTENSITY_SD = 11;

    /**
     * Sets the calibration in XY of the Object3D
     *
     * @param rxy The new calibration in XY
     */
    public void setResXY(double rxy) {
        resXY = rxy;
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
     * Sets the units attribute for the calibration
     *
     * @param u The new units value
     */
    public void setUnits(String u) {
        units = u;
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
     * Gets the calibration in Z of the Object3D
     *
     * @return The resZ value
     */
    public double getResZ() {
        return resZ;
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
     * Gets the calibration of the Object3D (ImageJ)
     *
     * @return The calibration
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
     * Sets the calibration of the Object3D (ImageJ)
     *
     * @param cal The new calibration
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
     * @param rz The new calibration in Z
     * @param u The new calibration units
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
    public ImageInt getLabelImage() {
        if (labelImage == null) {
            labelImage = this.createSegImage();
        }
        return labelImage;
    }

    /**
     * Sets the label image of the object (should start at 0,0
     *
     * @param labelImage
     */
    public void setLabelImage(ImageInt labelImage) {
        this.labelImage = labelImage;
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
                return getMeanPixValue(currentQuantifImage);
            } else {
                return 0;
            }
        } else if (Measure == MEASURE_INTENSITY_SD) {
            if (currentQuantifImage != null) {
                return getStDevPixValue(currentQuantifImage);
            } else {
                return 0;
            }
        } else {
            return Double.NaN;
        }
    }

    private void computeVolume() {
        volume = getVoxels().size();
    }

    /**
     * Gets the volumeof the Object3D in units
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
     * Compute the mass center of the object within an image
     *
     * @param ima
     */
    protected abstract void computeMassCenter(ImageHandler ima);

    /**
     * Gets the list of values inside an image as an array
     *
     * @param the image
     * @return the array of pixel values
     */
    public float[] getArrayValues(ImageHandler ima) {
        ArrayList<Voxel3D> vox = getVoxels();
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
     * @param the image
     * @param the quantile (0-1)
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
        double index = quantile * vals.length;
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
     * Compute the moments of the object (for ellipsoid orientation)
     */
    protected abstract void computeMoments2(boolean normalize); // order 2

    protected abstract void computeMoments3(); // order 3    

    protected abstract void computeMoments4(); // order 3    

    public double[] getMomentsRaw2() {
        if (Double.isNaN(s200)) {
            computeMoments2(false);
        }
        return new double[]{s200, s020, s002, s110, s101, s011};
    }

    public double[] getMomentsRaw3() {
        if (Double.isNaN(s300)) {
            computeMoments3();
        }
        return new double[]{s300, s030, s003, s210, s201, s120, s021, s102, s012, s111};
    }

    public double[] getMomentsRaw4() {
        if (Double.isNaN(s400)) {
            computeMoments4();
        }
        return new double[]{s400, s040, s004, s220, s202, s022, s121, s112, s211, s103, s301, s130, s310, s013, s031};
    }

    // refer to Xu and Li 2008. Geometric moments invariants. Pattern recognition. doi:10.1016/j.patcog.2007.05.001
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
        // FIXME power in interger ??
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
     * @param the image
     * @return the list of voxels
     */
    public ArrayList<Voxel3D> listVoxels(ImageHandler ima) {
        return listVoxels(ima, Double.NEGATIVE_INFINITY);
    }

    public abstract ArrayUtil listValues(ImageHandler ima);

    /**
     * List voxels in the image with values > threshold
     *
     * @param the image
     * @param the threshold
     * @return the list of voxels with values > threshold
     */
    public abstract ArrayList<Voxel3D> listVoxels(ImageHandler ima, double thresh);

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
        } else {
            return null;
        }
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
            return new Object3DSurface(((Object3DVoxels) (this)).computeMeshSurface(true));
        } else if (this instanceof Object3DLabel) {
            return new Object3DSurface(((Object3DLabel) (this)).computeMeshSurface(true));
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
     * drawing inside an objectcreator
     *
     * @param obj the object creator
     * @param col the color(grey level)
     */
    public abstract void draw(ObjectCreator3D obj, int col);

    /**
     * drawing inside a 2D byteprocessor
     *
     * @param mask the byte processor
     * @param z the z slice
     * @param col the color(grey level)
     */
    public abstract void draw(ByteProcessor mask, int z, int col);

    /**
     * drawing inside an imagestack
     *
     * @param mask the image
     * @param col the color(grey level)
     */
    public abstract void draw(ImageStack mask, int col);

    /**
     * drawing inside an ImageHandler
     *
     * @param mask the image
     * @param col the color(grey level)
     */
    public abstract void draw(ImageHandler mask, int col);

    /**
     * Drawing inside an image, with default value = object value
     *
     * @param the image
     */
    public void draw(ImageHandler mask) {
        draw(mask, this.getValue());
    }

    /**
     * Drawing links between two objects
     *
     * @param image
     * @param object
     * @param color
     */
    public void drawLink(ImageHandler mask, Object3D other, int col) {
        ObjectCreator3D create = new ObjectCreator3D(mask);
        create.createLine(this.getCenterAsPoint(), other.getCenterAsPoint(), col, 1);
    }

    /**
     * drawing inside an imagestack, in rgb color
     *
     * @param mask the imagestack
     * @param r red value
     * @param g greeen value
     * @param b blue value
     */
    public abstract void draw(ImageStack mask, int r, int g, int b);

    /**
     * create a roi for a slice
     *
     * @param z the z slice
     * @return the contour roi in slice z
     */
    public abstract Roi createRoi(int z);

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
        distcentermin = Double.NaN;
        distcentermax = Double.NaN;
        distcentermean = Double.NaN;
        distcentersigma = Double.NaN;
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
    public ArrayList<Voxel3D> getContours() {
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
     *
     * @return
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
     *
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
     *
     * @param x
     * @param y
     * @param z
     */
    public void setNewCenter(double x, double y, double z) {
        translate(x - this.getCenterX(), y - this.getCenterY(), z - this.getCenterZ());
    }

    /**
     *
     * @param x
     * @param y
     * @param z
     */
    public abstract void translate(double x, double y, double z);

    /**
     *
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
     *
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

        ArrayList<Voxel3D> cont = getContours();
        ArrayList<Voxel3D> orientedCont = new ArrayList();
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
        Vector3D res = new Vector3D(evect.get(0, order), evect.get(1, order), evect.get(2, order));

        return res;
    }

    /**
     *
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
        contours = new ArrayList();
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
        if (Double.isNaN(distcentermin)) {
            this.computeDistCenter();
        }

        return distcentermin;
    }

    /**
     * the maximum distance between center and contours
     *
     * @return the max distance(in real unit)
     */
    public double getDistCenterMax() {
        if (Double.isNaN(distcentermax)) {
            this.computeDistCenter();
        }

        return distcentermax;
    }

    /**
     * the average distance between center and contours
     *
     * @return the mean distance(in real unit)
     */
    public double getDistCenterMean() {
        if (Double.isNaN(distcentermean)) {
            this.computeDistCenter();
        }

        return distcentermean;
    }

    /**
     * the sigma value for distances between center and contours
     *
     * @return the SD distance
     */
    public double getDistCenterSigma() {
        if (Double.isNaN(distcentersigma)) {
            this.computeDistCenter();
        }

        return distcentersigma;
    }

    /**
     *
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
            Voxel3D p2;
            ArrayList cont = this.getContours();

            int s = getContours().size();
            double sd = s;
            //Voxel3D tmpmin = null;

            for (int j = 0; j < s; j++) {
                p2 = (Voxel3D) cont.get(j);
                dist2 = rx2 * ((ccx - p2.getX()) * (ccx - p2.getX()) + ((ccy - p2.getY()) * (ccy - p2.getY()))) + rz2 * (ccz - p2.getZ()) * (ccz - p2.getZ());
                distsum += Math.sqrt(dist2);
                distsum2 += dist2;
                if (dist2 > distmax) {
                    distmax = dist2;
                }
                if (dist2 < distmin) {
                    distmin = dist2;
                    //tmpmin = p2;
                    //System.out.println("pix=" + tmpmin + " dist=" + distmin);
                }

            }

            distcentermax = Math.sqrt(distmax);
            distcentermin = Math.sqrt(distmin);
            distcentermean = distsum / sd;
            distcentersigma = Math.sqrt((distsum2 - ((distsum * distsum) / sd)) / (sd - 1));
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
        ArrayList cont = this.getContours();

        int s = getContours().size();
        for (int i = 0; i < s; i++) {
            p1 = (Voxel3D) cont.get(i);
            for (int j = i + 1; j < s; j++) {
                p2 = (Voxel3D) cont.get(j);
                //IJ.log("i=" + i + " j=" + j + " p1=" + p1 + " +p2=" + p2 + " " + distmax + " " + dist);
                dist = rx2 * ((p1.getX() - p2.getX()) * (p1.getX() - p2.getX()) + ((p1.getY() - p2.getY()) * (p1.getY() - p2.getY()))) + rz2 * (p1.getZ() - p2.getZ()) * (p1.getZ() - p2.getZ());
                if (dist > distmax) {
                    distmax = dist;
                    feret1 = p1;
                    feret2 = p2;
                }
            }
        }
        feret = (float) Math.sqrt(distmax);
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
     * @param other the other object
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
        double dist = Math.sqrt((bx - autre.bx) * (bx - autre.bx) * resXY * resXY + (by - autre.by) * (by - autre.by) * resXY * resXY + (bz - autre.bz) * (bz - autre.bz) * resZ * resZ);
        return dist;
    }

    /**
     * 2D distance from center to center (in real distance)
     *
     * @param autre the other Object3D
     * @return distance
     */
    public double distCenter2DUnit(Object3D autre) {
        double dist = Math.sqrt((bx - autre.bx) * (bx - autre.bx) * resXY * resXY + (by - autre.by) * (by - autre.by) * resXY * resXY);
        return dist;
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
     *
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
     * The distance between center and a point
     *
     * @param x x-coordinate ofthe point
     * @param y y-coordinate ofthe point
     * @param z z-coordinate ofthe point
     * @return the distance (in voxels)
     */
    public double distPixelCenter(double x, double y, double z) {
        return Math.sqrt((bx - x) * (bx - x) * resXY * resXY + (by - y) * (by - y) * resXY * resXY + (bz - z) * (bz - z) * resZ * resZ);
    }

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
     * Gets the touchBorders attribute of the Object3D Object3D
     *
     * @return The touchBorders value
     */
    public boolean getTouchBorders() {
        return touchBorders;
    }

    /**
     *
     * @param vox
     * @return
     */
    public boolean isContour(Voxel3D vox) {
        if (contours == null) {
            this.computeContours();
        }
        Iterator<Voxel3D> it = contours.iterator();
        while (it.hasNext()) {
            Voxel3D v = it.next();
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
        if ((x >= xmin) && (x <= xmax) && (y >= ymin) && (y <= ymax) && (z >= zmin) && (z <= zmax)) {
            return true;
        } else {
            return false;
        }
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
        if ((x >= xmin) && (x <= xmax) && (y >= ymin) && (y <= ymax) && (z >= zmin) && (z <= zmax)) {
            return true;
        } else {
            return false;
        }
    }

    /**
     *
     * @param x
     * @param y
     * @param z
     * @param rx
     * @param ry
     * @param rz
     * @return
     */
    public boolean insideBounding(double x, double y, double z, int rx, int ry, int rz) {
        if ((x >= xmin - rx) && (x <= xmax + rx) && (y >= ymin - ry) && (y <= ymax + ry) && (z >= zmin - rz) && (z <= zmax + rz)) {
            return true;
        } else {
            return false;
        }
    }

    public boolean inside(Point3D P) {
        return inside(P.getX(), P.getY(), P.getZ());
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
        if (this.labelImage != null) {
            int testx = (int) Math.round(x);
            int testy = (int) Math.round(y);
            int testz = (int) Math.round(z);
            if ((testx < 0) || (testy < 0) || (testz < 0) || (testx >= labelImage.sizeX) || (testy >= labelImage.sizeY) || (testz >= labelImage.sizeZ)) {
                return false;
            }
            return (labelImage.getPixel(testx, testy, testz) == val);
        }
        if (miniLabelImage == null) {
            miniLabelImage = this.createSegImageMini(val, 1);
        } else {
            // ERROR SOMETIMES IN TANGO (DO NOT UNDERSTAND WHY, SYNCHRONIZED ?)
            if (miniLabelImage.sizeZ < (getZmax() - getZmin())) {
                System.out.println("Pb size seg image ");
                miniLabelImage = this.createSegImageMini(val, 1);
            }
        }
        //miniLabelImage = this.createSegImageMini(val, 1);
        // debug
        int xmi = miniLabelImage.offsetX;
        int ymi = miniLabelImage.offsetY;
        int zmi = miniLabelImage.offsetZ;
        //IJ.log(" " + xmi + " " + ymi + " " + zmi + " " + getXmax() + " " + getYmax() + " " + getZmax());
        //IJ.log("" + miniLabelImage.sizeX + " " + miniLabelImage.sizeY + " " + miniLabelImage.sizeZ);
        //miniLabelImage.show("minseg " + x + " " + y + " " + z);
        int testx = (int) Math.round(x - xmi);
        int testy = (int) Math.round(y - ymi);
        int testz = (int) Math.round(z - zmi);
        if ((testx < 0) || (testy < 0) || (testz < 0) || (testx >= miniLabelImage.sizeX) || (testy >= miniLabelImage.sizeY) || (testz >= miniLabelImage.sizeZ)) {
            return false;
        }
        if (miniLabelImage.getPixel(testx, testy, testz) == val) {
            return true;
        }
        return false;
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
        if (insideBounding(oxmin, oymin, ozmin) && insideBounding(oxmin, oymax, ozmin) && insideBounding(oxmax, oymin, ozmin) && insideBounding(oxmax, oymax, ozmin) && insideBounding(oxmin, oymin, ozmax) && insideBounding(oxmin, oymax, ozmax) && insideBounding(oxmax, oymin, ozmax) && insideBounding(oxmax, oymax, ozmax)) {
            return true;
        } else {
            return false;
        }
    }

    /**
     *
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

    private boolean includesOneVertexBox(Object3D autre) {
        int oxmin = autre.getXmin();
        int oxmax = autre.getXmax();
        int oymin = autre.getYmin();
        int oymax = autre.getYmax();
        int ozmin = autre.getZmin();
        int ozmax = autre.getZmax();

        if (insideBounding(oxmin, oymin, ozmin) || insideBounding(oxmin, oymax, ozmin) || insideBounding(oxmax, oymin, ozmin) || insideBounding(oxmax, oymax, ozmin) || insideBounding(oxmin, oymin, ozmax) || insideBounding(oxmin, oymax, ozmax) || insideBounding(oxmax, oymin, ozmax) || insideBounding(oxmax, oymax, ozmax)) {
            return true;
        } else {
            return false;
        }
    }

    /**
     *
     * @param other
     * @return
     */
    public boolean intersectionBox(Object3D other) {
        return (this.includesOneVertexBox(other) || (other.includesOneVertexBox(this)));
    }

    /**
     *
     * @param other
     * @return
     */
    public boolean disjointBox(Object3D other) {
        return !intersectionBox(other);
    }

    /**
     * Create an intersection image around two objects
     *
     * @param other the Other object
     * @param val1 the value for this object
     * @param val2 the value for other object
     * @param border the border around box
     * @return the image with 3 values (val1 for this, val2 for other, val1+val2
     * for intersection)
     */
    public ImageInt createIntersectionImage(Object3D other, int val1, int val2, int border) {
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

        ImageInt imgThis = this.createSegImage(xmi, ymi, zmi, xma, yma, zma, val1);
        ImageInt imgOther = other.createSegImage(xmi, ymi, zmi, xma, yma, zma, val2);

        //imgThis.show();
        //imgOther.show();
        ImageInt addImage = imgThis.addImage(imgOther);
        addImage.offsetX = xmi;
        addImage.offsetY = ymi;
        addImage.offsetZ = zmi;
        imgThis = null;
        imgOther = null;
        System.gc();

        return addImage;
    }

    /**
     * Create an intersection image around two objects (with no extra borders)
     *
     * @param other the Other object
     * @param val1 the value for this object
     * @param val2 the value for other object
     * @return the image with 3 values (val1 for this, val2 for other, val1+val2
     * for intersection)
     */
    public ImageInt createIntersectionImage(Object3D other, int val1, int val2) {
        return createIntersectionImage(other, val1, val2, 0);

    }

    /**
     * Get the intersection voxels as a 3D object
     *
     * @param other The other object
     * @return The objecdt with intersection voxels (or null if objects are
     * disjoint)
     */
    public Object3DVoxels getIntersectionObject(Object3D other) {
        if (disjointBox(other)) {
            return null;
        }
        ImageInt inter = this.createIntersectionImage(other, 1, 2);
        //inter.show();
        Object3DVoxels obj = new Object3DVoxels(inter, 3);
        obj.setValue(this.getValue());
        obj.translate(inter.offsetX, inter.offsetY, inter.offsetZ);

        return obj;
    }

    /**
     * Get the contact surfaces between two objects, outside voxels < dist and
     * number of border voxels of this object included in the other
     *
     * @param other the other object
     * @param dist_max distance max (in pixel) between two contour points of the
     * two objects
     * @return int array with : nb ofcontours points below distance max to
     * contours points in other object AND nb of voxel of this object inside the
     * other one
     */
    public int[] surfaceContact(Object3D other, double dist_max) {
        // check distance border-border
        // if BB > dist_max no contact surface
        double distbb = this.distBorderPixel(other);
        if (distbb > dist_max) {
            return new int[]{0, 0};
        }
        int surfPos;
        int surfNeg = 0;
        Voxel3D p0;
        Voxel3D p1;
        double dist;
        int s = getContours().size();
        ArrayList othercontours = other.getContours();
        int t = othercontours.size();
        double dmax2 = dist_max * dist_max;
        double[][] distres = new double[s][t];
        double dmin;
        int j0;

        for (int i = 0; i < s; i++) {
            for (int j = 0; j < t; j++) {
                distres[i][j] = -1;
            }
        }

        // FIXME use kd-tree for optimisation
        for (int i = 0; i < s; i++) {
            p0 = (Voxel3D) contours.get(i);
            j0 = -1;
            // if voxel inside other object does not count it
            if (other.inside(p0)) {
                surfNeg++;
                continue;
            }
            dmin = dmax2;
            for (int j = 0; j < t; j++) {
                p1 = (Voxel3D) othercontours.get(j);
                dist = p0.distanceSquare(p1);
                if (dist <= dmin) {
                    dmin = dist;
                    j0 = j;
                }
            }
            if (j0 != -1) {
                distres[i][j0] = dmin;
            }
        }
        // count nb pix from 2 --> 1 having min
        surfPos = 0;
        for (int j = 0; j < t; j++) {
            j0 = 0;
            while ((j0 < s) && (distres[j0][j]) == -1) {
                j0++;
            }
            if (j0 < s) {
                surfPos++;
            }
        }
        return new int[]{surfPos, surfNeg};
    }

    /**
     * @param other object
     * @param distSquareMax object maximum square distance between two voxels (0
     * for coloc, 1 for side contact, >1 for diagonal contact
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
     * @param obj the other object
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
//        double distmin = Double.MAX_VALUE;
//        Voxel3D p0 = null, p1 = null;
//        for (Voxel3D othervox : other.getContours()) {
//            double[] pos = othervox.getArray();
//            //IJ.log("border " + kdtreeContours);
//            Item item = getKdtreeContours().getNearestNeighbor(pos, 1)[0];
//            //IJ.log("pixelborder " + item);
//            if (item.distanceSq < distmin) {
//                p0 = othervox;
//                p1 = (Voxel3D) item.obj;
//                distmin = item.distanceSq;
//            }
//        }
        Voxel3D[] voxs = this.VoxelsBorderBorder(other);

        return new Vector3D(voxs[0], voxs[1]);
    }

    public Voxel3D[] VoxelsBorderBorder(Object3D other) {
        double distmin = Double.MAX_VALUE;
        Voxel3D otherBorder = null, thisBorder = null;
        for (Voxel3D othervox : other.getContours()) {
            double[] pos = othervox.getArray();
            //IJ.log("border " + kdtreeContours);
            Item item = getKdtreeContours().getNearestNeighbor(pos, 1)[0];
            //IJ.log("pixelborder " + item);
            if (item.distanceSq < distmin) {
                otherBorder = othervox;
                thisBorder = (Voxel3D) item.obj;
                distmin = item.distanceSq;
            }
        }

        return new Voxel3D[]{thisBorder, otherBorder};
    }

    /**
     * The vector between V0 and V1 : V0 is the point on border of object from
     * point0 along direction V1 is the point on border of other object from
     * point1 along opposite direction
     *
     *
     * @param point0 the first point on this object
     * @param other the other object
     * @param point1 the second point on other object
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

        Vector3D V = other.vectorPixelBorder(this.getCenterAsVector());

        return V;

        /*
         * double distmin = Double.MAX_VALUE; double dist; Voxel3D p1 = new
         * Voxel3D(); Voxel3D ailleur = null; ArrayList autrecontour =
         * other.getContours(); int s = autrecontour.size(); int c = 0; Iterator
         * it = autrecontour.iterator(); while (it.hasNext()) { ailleur =
         * (Voxel3D) it.next(); dist = (bx - ailleur.getX()) * (bx -
         * ailleur.getX()) * resXY * resXY + (by - ailleur.getY()) * (by -
         * ailleur.getY()) * resXY * resXY + (bz - ailleur.getZ()) * (bz -
         * ailleur.getZ()) * resZ * resZ; if (dist < distmin) { distmin = dist;
         * p1 = ailleur; } c++; } IJ.showStatus("Computing distance 100%");
         * return new Vector3D(-bx + p1.getX(), -by + p1.getY(), -bz +
         * p1.getZ());
         *
         */
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
        Voxel3D vox = (Voxel3D) item.obj;

        return vox;
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
//        // USE KDTREE
//        double[] pos = {x, y, z};
//        Item item = getKdtreeContours().getNearestNeighbor(pos, 1)[0];
//        //System.out.println("object:"+this.getValue()+" contour: "+this.contours.size()+ " item null?"+(item==null));
        Voxel3D vox = this.getPixelBorder(x, y, z);

        return new Vector3D(vox.getX() - x, vox.getY() - y, vox.getZ() - z);

//        double distmin = Double.MAX_VALUE;
//        double dist;
//        double rx2 = resXY * resXY;
//        double rz2 = resZ * resZ;
//        Voxel3D here;
//        Voxel3D stock = new Voxel3D();
//        ArrayList cont = this.getContours();
//        Iterator it = cont.iterator();
//        while (it.hasNext()) {
//            here = (Voxel3D) it.next();
//            dist = rx2 * ((x - here.getX()) * (x - here.getX()) + (y - here.getY()) * (y - here.getY())) + (z - here.getZ()) * (z - here.getZ()) * rz2;
//            if (dist < distmin) {
//                distmin = dist;
//                stock = here;
//            }
//        }
//        return new Vector3D(stock.getX() - x, stock.getY() - y, stock.getZ() - z);
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
        ArrayList cont = this.getContours();
        Iterator it = cont.iterator();
        while (it.hasNext()) {
            edge = (Voxel3D) it.next();
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
     * @param V the vector (to describe the point)
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
     * @param x the x-coordinate of the point
     * @param y the y-coordinate of the point
     * @param z the z-coordinate of the point
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
        double angledeg = Math.toDegrees(Math.acos(cosangle));
        return angledeg;
    }

    /**
     * Gets the meanPixValue attribute of the Object3D object
     *
     * @param ima
     * @return The meanPixValue value
     */
    public double getMeanPixValue(ImageHandler ima) {
        if (volume > 0) {
            return getIntegratedDensity(ima) / getVolumePixels();
        } else {
            return Double.NaN;
        }
    }
    
    public double getModePixValue(ImageHandler ima) {
        if (volume > 0) {
            return listValues(ima).getMode();
        } else {
            return Double.NaN;
        }
    }
    
    public double getModePixValueNonZero(ImageHandler ima) {
        if (volume > 0) {
            return listValues(ima).getModeNonZero();
        } else {
            return Double.NaN;
        }
    }
    
    

    /**
     *
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
     * Gets the SD of pixels in object
     *
     * @param ima the 3D image
     * @return The sigma value
     */
    public double getStDevPixValue(ImageHandler ima) {
        if ((currentQuantifImage == null) || (currentQuantifImage != ima)) {
            computeMassCenter(ima);
            currentQuantifImage = ima;
        }
        return sigma;
    }

    /**
     * create a small labeleld image containing the object, limited to the
     * bounding box
     *
     * @param val the value to draw inside image
     * @param borderSize
     * @return the labelled image
     */
    public ImageInt createSegImageMini(int val, int borderSize) {
        return createSegImageMini(val, borderSize, borderSize, borderSize);
    }

    /**
     *
     * @param val
     * @param borderSizeX
     * @param borderSizeY
     * @param borderSizeZ
     * @return
     */
    public ImageInt createSegImageMini(int val, int borderSizeX, int borderSizeY, int borderSizeZ) {
        int xm = this.getXmin() - borderSizeX;
        if (xm < -borderSizeX) {
            xm = -borderSizeX;
        }
        int ym = this.getYmin() - borderSizeY;
        if (ym < -borderSizeY) {
            ym = -borderSizeY;
        }
        int zm = this.getZmin() - borderSizeZ;
        if (zm < -borderSizeZ) {
            zm = -borderSizeZ;
        }

        int w = this.getXmax() - xm + 1 + borderSizeX;
        int h = this.getYmax() - ym + 1 + borderSizeY;
        int d = this.getZmax() - zm + 1 + borderSizeZ;
        miniLabelImage = new ImageShort("Object_" + val, w, h, d);
        Voxel3D vox;
        double xx;
        double yy;
        double zz;
        Iterator it = getVoxels().iterator();
        while (it.hasNext()) {
            vox = (Voxel3D) it.next();
            xx = vox.getX() - xm;
            yy = vox.getY() - ym;
            zz = vox.getZ() - zm;
            // TODO suface vertices may have coordinates < 0 if touching edges 
            if (miniLabelImage.contains(xx, yy, zz)) {
                miniLabelImage.setPixel((int) Math.round(xx), (int) Math.round(yy), (int) Math.round(zz), val);
            }
        }

        //miniSegImage.getImagePlus().show();
        // set the offsets
        miniLabelImage.offsetX = xm;
        miniLabelImage.offsetY = ym;
        miniLabelImage.offsetZ = zm;
        miniLabelImage.setScale((float) this.getResXY(), (float) this.getResZ(), this.getUnits());
        //miniLabelImage.show("obj:"+this);
        return miniLabelImage;
    }

    /**
     *
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
        Iterator it = getVoxels().iterator();
        while (it.hasNext()) {
            vox = (Voxel3D) it.next();
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
     *
     * @return
     */
    public ImageInt createSegImage() {
        return createSegImage(0, 0, 0, xmax, ymax, zmax, value);
    }

    /**
     *
     * @param xmi
     * @param ymi
     * @param zmi
     * @param xma
     * @param yma
     * @param zma
     * @param val
     * @return
     */
    public ImageInt createSegImage(int xmi, int ymi, int zmi, int xma, int yma, int zma, int val) {
        ImageInt SegImage = new ImageShort("Object", xma - xmi + 1, yma - ymi + 1, zma - zmi + 1);
        Voxel3D vox;
        Iterator it = getVoxels().iterator();
        while (it.hasNext()) {
            vox = (Voxel3D) it.next();
            if (SegImage.contains(vox.getRoundX() - xmi, vox.getRoundY() - ymi, vox.getRoundZ() - zmi)) {
                SegImage.setPixel(vox.getRoundX() - xmi, vox.getRoundY() - ymi, vox.getRoundZ() - zmi, val);
            }
        }
        //new ImagePlus("MiniSeg", seg.getStack()).show();
        return SegImage;
    }

    /**
     * Get the list of voxels of the object
     *
     * @return the list of voxels
     */
    public abstract ArrayList<Voxel3D> getVoxels();

    /**
     *
     * @param path
     */
    public abstract void writeVoxels(String path);

    // code copied from ImageJ 3D Viewer MCTriangulator
    /**
     *
     * @param calibrated
     * @return
     */
    public List computeMeshSurface(boolean calibrated) {
        IJ.showStatus("computing mesh");
        // use miniseg
        ImageInt miniseg = createSegImageMini(1, 1);
        //IJ.log("Miniseg " + miniseg.getImagePlus().getBitDepth());
        ImageByte miniseg8 = ((ImageShort) (miniseg)).convertToByte(false);
        //IJ.log("Miniseg " + miniseg8.getImagePlus().getBitDepth());
        ImagePlus objectImage = miniseg8.getImagePlus();
        if (calibrated) {
            objectImage.setCalibration(getCalibration());
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

    /**
     *
     * @param obj
     * @return
     */
    public boolean includes(Object3D obj) {
        if (!this.includesBox(obj)) {
            return false;
        }

        // use coloc
        int co = this.getColoc(obj);
        if (co == obj.getVolumePixels()) {
            return true;
        } else {
            return false;
        }

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
        if (kdtreeContours == null) {
            if (contours != null && !contours.isEmpty()) {
                kdtreeContours = new KDTreeC(3);
                kdtreeContours.setScale3(this.resXY, this.resXY, this.resZ);
                for (Voxel3D v : contours) {
                    kdtreeContours.add(v.getArray(), v);
                }
            } else {
                this.computeContours();
            }
        }
        return kdtreeContours;
    }

    /**
     *
     * @param obj
     * @return
     */
    public abstract boolean hasOneVoxelColoc(Object3D obj);

    /**
     *
     * @param obj
     * @return
     */
    public abstract int getColoc(Object3D obj);

    /**
     *
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

    private Object3DVoxels getMorphoObject(int op, float radX, float radY, float radZ, boolean createLabelImage) {
        // special case radii = 0
        if ((radX == 0) && (radY == 0) && (radZ == 0)) {
            return new Object3DVoxels(this);
        }
        // draw object on new image
        //int bo = (int) Math.max(radX, radY), radZ);
        ImageInt segImage;
        segImage = this.createSegImageMini(value, (int) Math.ceil(radX), (int) Math.ceil(radY), (int) Math.ceil(radZ));
        //segImage.show("segimage 1");
        ImageInt segImage2 = null;
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
            segImage2 = FastFilters3D.filterIntImage(segImage, filter, radX, radY, radZ, 0, true);
            segImage2.setOffset(segImage);
        } // else use binaryMorpho class (based on edm)
        else {
            segImage2 = BinaryMorpho.binaryMorpho(segImage, op, radX, radZ);
            if (segImage2 != null) {
                segImage2.replacePixelsValue(255, value);
            }
        }
        if (segImage2 == null) {
            return null;
        }
        //segImage2.show("segimage 2");
        Object3DVoxels objMorpho = new Object3DVoxels(segImage2, value);
        objMorpho.translate(segImage2.offsetX, segImage2.offsetY, segImage2.offsetZ);
        if (createLabelImage) {
            ImageInt labelDilated = objMorpho.createSegImage();
            objMorpho.setLabelImage(labelDilated);
        }
        return objMorpho;
    }

    // see landini's page for details
    // equals to its closing
    /**
     *
     * @param radX
     * @param radY
     * @param radZ
     * @return
     */
    public boolean b_closed(float radX, float radY, float radZ) {
        Object3D closed = this.getClosedObject(radX, radY, radZ, false);
        MereoObject3D mereo = new MereoObject3D(this, closed);
        return mereo.Equality();
    }

    // see landini's page for details
    // equals to its opening
    /**
     *
     * @param radX
     * @param radY
     * @param radZ
     * @return
     */
    public boolean b_open(float radX, float radY, float radZ) {
        Object3D open = this.getOpenedObject(radX, radY, radZ, false);
        MereoObject3D mereo = new MereoObject3D(this, open);
        return mereo.Equality();
    }

    // see landini's page for details
    // equals to its opening and closing
    /**
     *
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
     *
     * @param radX
     * @param radY
     * @param radZ
     * @param createLabelImage
     * @return
     */
    public Object3DVoxels getDilatedObject(float radX, float radY, float radZ, boolean createLabelImage) {
        return getMorphoObject(BinaryMorpho.MORPHO_DILATE, radX, radY, radZ, createLabelImage);
    }

    /**
     *
     * @param radX
     * @param radY
     * @param radZ
     * @param createLabelImage
     * @return
     */
    public Object3DVoxels getErodedObject(float radX, float radY, float radZ, boolean createLabelImage) {
        return getMorphoObject(BinaryMorpho.MORPHO_ERODE, radX, radY, radZ, createLabelImage);
    }

    /**
     *
     * @param radX
     * @param radY
     * @param radZ
     * @param createLabelImage
     * @return
     */
    public Object3DVoxels getClosedObject(float radX, float radY, float radZ, boolean createLabelImage) {
        return getMorphoObject(BinaryMorpho.MORPHO_CLOSE, radX, radY, radZ, createLabelImage);
    }

    /**
     *
     * @param radX
     * @param radY
     * @param radZ
     * @param createLabelImage
     * @return
     */
    public Object3DVoxels getOpenedObject(float radX, float radY, float radZ, boolean createLabelImage) {
        return getMorphoObject(BinaryMorpho.MORPHO_OPEN, radX, radY, radZ, createLabelImage);
    }

    /**
     *
     * @return
     */
    public Object3DSurface getConvexSurface() {
        IJ.showStatus("Computing convex surface");
        // compute surface if necessary
        Object3DSurface surf;
        if (this instanceof Object3DSurface) {
            surf = (Object3DSurface) this;
        } else {
            Object3DVoxels obj = this.getObject3DVoxels();
            // claibration not important for computing hull
            surf = new Object3DSurface(obj.computeMeshSurface(true));
        }
        // compute convex hull
        Object3DSurface conv = new Object3DSurface(surf.computeConvexHull3D());
        conv.setCalibration(this.getCalibration());
        conv.setValue(this.getValue());

        return conv;
    }

    /**
     *
     * @param multi multiprocessor
     * @return
     */
    public Object3DVoxels getConvexObject(boolean multi) {
        IJ.showStatus("Computing convex object");
        Object3DSurface conv = this.getConvexSurface();
        conv.multiThread = multi;
        ArrayList<Voxel3D> vox = conv.getVoxels();
        Object3DVoxels obj = new Object3DVoxels(vox);
        obj.setCalibration(this.getCalibration());
        obj.computeContours();

        return obj;
    }

    /**
     *
     * @return
     */
    public Object3DVoxels getConvexObject() {
        return getConvexObject(true);
    }
}
