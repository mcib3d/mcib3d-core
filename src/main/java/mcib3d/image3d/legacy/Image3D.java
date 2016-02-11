package mcib3d.image3d.legacy;

import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.process.ByteProcessor;
import ij.process.FloatProcessor;
import ij.process.ImageProcessor;
import ij.process.ShortProcessor;
import mcib3d.geom.Point3D;
import mcib3d.geom.Vector3D;
import mcib3d.geom.Voxel3D;
import mcib3d.utils.ArrayUtil;
import mcib3d.utils.HistogramUtil;

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
 * Abstract Image3D class
 *
 * @author cedric + thomas @created 6 mai 2004
 */
public abstract class Image3D {

    /**
     * size x
     */
    protected int sizex;
    /**
     * size y
     */
    protected int sizey;
    /**
     * size z
     */
    protected int sizez;
    /**
     * type of data
     */
    public final int type;
    /**
     * BYTE
     */
    public final static int BYTE = 1;
    /**
     * SHORT
     */
    public final static int SHORT = 2;
    /**
     * FLOAT
     */
    public final static int FLOAT = 3;
    /**
     * where is the maxPixel pixel
     */
    protected Voxel3D maxPixel;
    /**
     * where is the minPixel pixel
     */
    protected Voxel3D minPixel;
    /**
     * std dev of the image
     */
    protected float stDev;
    /**
     * average value of the image
     */
    protected float meanValue;
    /**
     * center x of the image
     */
    protected float centerx;
    /**
     * center y of the image
     */
    protected float centery;
    /**
     * center z of the image
     */
    protected float centerz;
    /**
     * Axis X
     */
    public final static int XAXIS = 1;
    /**
     * Axis Y
     */
    public final static int YAXIS = 2;
    /**
     * Axis Z
     */
    public final static int ZAXIS = 3;
    /**
     * The ImageJ IJstack that stores the pixels data ( a 1D array could be more
     * efficient )
     */
    protected ImageStack IJstack;
    // FILTERING
    /**
     *
     */
    //public final static int FILTER_MEAN = 1;
    /**
     *
     */
    //public final static int FILTER_MEDIAN = 2;
    /**
     *
     */
    //public final static int FILTER_MIN = 3;
    /**
     *
     */
    //public final static int FILTER_MAX = 4;
    /**
     *
     */
    //public final static int FILTER_VAR = 5;
    /**
     *
     */
    //public final static int FILTER_MAXLOCAL = 5;
    // options
    /**
     *
     */
    protected boolean showStatus = true;
    protected boolean multithread = true;

    /**
     *
     */
    /**
     * Constructor for the Image3D object
     *
     * @param sizex Size in x of the image
     * @param sizey Size in y of the image
     * @param sizez Size in z of the image
     * @param type Data type in the image
     */
    public Image3D(int sizex, int sizey, int sizez, int type) {
        IJstack = new ImageStack(sizex, sizey);
        this.sizex = sizex;
        this.sizey = sizey;
        this.sizez = sizez;
        this.type = type;
        maxPixel = null;
        minPixel = null;
        meanValue = Float.NaN;
        stDev = Float.NaN;
        centerx = (float) (sizex - 1) / 2.0F;
        centery = (float) (sizey - 1) / 2.0F;
        centerz = (float) (sizez - 1) / 2.0F;
    }

    /**
     * Constructor for the Image3D object from a ImageJ IJstack
     *
     * @param ijstack imageJ IJstack
     */
    public Image3D(ImageStack ijstack) {
        IJstack = ijstack;
        this.sizex = ijstack.getWidth();
        this.sizey = ijstack.getHeight();
        this.sizez = ijstack.getSize();
        ImageProcessor ip = ijstack.getProcessor(1);
        if (ip instanceof ByteProcessor) {
            this.type = BYTE;
        } else if (ip instanceof ShortProcessor) {
            this.type = SHORT;
        } else if (ip instanceof FloatProcessor) {
            this.type = FLOAT;
        } else {
            IJ.log("Stack not supported");
            this.type = 0;
        }
        maxPixel = null;
        minPixel = null;
        meanValue = Float.NaN;
        stDev = Float.NaN;
        centerx = (float) (sizex - 1) / 2.0F;
        centery = (float) (sizey - 1) / 2.0F;
        centerz = (float) (sizez - 1) / 2.0F;
    }

    /**
     *
     * @param showStatus
     */
    public void setShowStatus(boolean showStatus) {
        this.showStatus = showStatus;
    }

    /**
     * Kill the Image3D
     */
    public void kill() {
        IJstack = null;
    }

    /**
     * set all pixels to 0
     */
    public void reset() {
        for (int k = 0; k < sizez; k++) {
            for (int j = 0; j < sizey; j++) {
                for (int i = 0; i < sizex; i++) {
                    setPix(i, j, k, 0);
                }
            }
        }
    }

    /**
     * Reset the image to a given value
     *
     * @param nb
     */
    public void reset(float nb) {
        for (int k = 0; k < sizez; k++) {
            for (int j = 0; j < sizey; j++) {
                for (int i = 0; i < sizex; i++) {
                    setPix(i, j, k, nb);
                }
            }
        }
    }

    /**
     * Get the imageJ IJstack (for displaying)
     *
     * @return The IJstack
     */
    public ImageStack getStack() {
        return IJstack;
    }

    public ImagePlus getPlus() {
        return new ImagePlus("3Dima", IJstack);
    }

    /**
     * Gets the voxel value of the Image3D object
     *
     * @param x x coordinate of the voxel
     * @param y y coordinate of the voxel
     * @param z z coordinate of the voxel
     * @return The voxel value
     */
    public abstract float getPix(int x, int y, int z);

    /**
     * Gets the voxel value of the Image3D object (interpolated value)
     *
     * @param x x coordinate of the voxel
     * @param y y coordinate of the voxel
     * @param z z coordinate of the voxel
     * @return The voxel value
     */
    public abstract float getPix(float x, float y, float z);

    /**
     * Sets the voxel value
     *
     * @param x x coordinate of the voxel
     * @param y y coordinate of the voxel
     * @param z z coordinate of the voxel
     * @param value The new voxel value
     */
    public abstract void setPix(int x, int y, int z, double value);

    public double[] getLinePixelValue(int x0, int y0, int z0, int x1, int y1, int z1, boolean interpolated) {
        int dx = (x1 - x0);
        int dy = (y1 - y0);
        int dz = (z1 - z0);
        int dist = 1 + Math.max(Math.max(Math.abs(dx), Math.abs(dy)), Math.abs(dz));

        double[] res = new double[dist];

        float xStep = dx / (float) dist;
        float yStep = dy / (float) dist;
        float zStep = dz / (float) dist;

        //System.out.println("Steps: " + xStep + ", " + yStep + ", " + zStep);

        float posX, posY, posZ;

        for (int i = 0; i < dist; i++) {
            posX = x0 + (i * xStep);
            posY = y0 + (i * yStep);
            posZ = z0 + (i * zStep);

            if (interpolated) {
                res[i] = this.getPix(posZ, posZ, posZ);
            } else {
                res[i] = this.getPix(Math.round(posX), Math.round(posY), Math.round(posZ));
            }
        }

        return res;
    }

    public double[] getLinePixelValue(Point3D origin, Vector3D dir, double distNeg, double distPos, boolean interpolated) {
        Vector3D dirN = dir.getNormalizedVector();

        int x0 = (int) Math.round(origin.getX() - distNeg * dirN.getX());
        int y0 = (int) Math.round(origin.getY() - distNeg * dirN.getY());
        int z0 = (int) Math.round(origin.getZ() - distNeg * dirN.getZ());

        int x1 = (int) Math.round(origin.getX() + distPos * dirN.getX());
        int y1 = (int) Math.round(origin.getY() + distPos * dirN.getY());
        int z1 = (int) Math.round(origin.getZ() + distPos * dirN.getZ());

        return getLinePixelValue(x0, y0, z0, x1, y1, z1, interpolated);
    }

    /**
     * Gets the sizex attribute of the Image3D object
     *
     * @return The sizex value
     */
    public int getSizex() {
        return sizex;
    }

    /**
     * Gets the sizey attribute of the Image3D object
     *
     * @return The sizey value
     */
    public int getSizey() {
        return sizey;
    }

    /**
     * Gets the sizez attribute of the Image3D object
     *
     * @return The sizez value
     */
    public int getSizez() {
        return sizez;
    }

    /**
     *
     * @return
     */
    public int getNbPixel() {
        return sizex * sizey * sizez;
    }

    /**
     * Gets the data type attribute of the Image3D object
     *
     * @return The type value
     */
    public int getType() {
        return type;
    }

    /**
     * Gets the centerX attribute of the Image3D object
     *
     * @return The centerX value
     */
    public float getCenterX() {
        return centerx;
    }

    /**
     * Gets the centerY attribute of the Image3D object
     *
     * @return The centerY value
     */
    public float getCenterY() {
        return centery;
    }

    /**
     * Gets the centerZ attribute of the Image3D object
     *
     * @return The centerZ value
     */
    public float getCenterZ() {
        return centerz;
    }

    /**
     * Gets the maximum pixel value
     *
     * @return The maximum value
     */
    protected double getFloatMaximum() {
        if (maxPixel == null) {
            computeMinMax();
        }

        return maxPixel.getValue();
    }

    /**
     * Gets the minimum pixel value
     *
     * @return The minimum value
     */
    protected double getFloatMinimum() {
        if (minPixel == null) {
            computeMinMax();
        }

        return minPixel.getValue();
    }

    /**
     * compute the min and max pixels
     */
    protected void computeMinMax() {
        minPixel = new Voxel3D(0, 0, 0, getPix(0, 0, 0));
        maxPixel = new Voxel3D(0, 0, 0, getPix(0, 0, 0));
        for (int k = 0; k < sizez; k++) {
            for (int j = 0; j < sizey; j++) {
                for (int i = 0; i < sizex; i++) {
                    double val = getPix(i, j, k);
                    if (val < minPixel.getValue()) {
                        minPixel.setVoxel(i, j, k, val);
                    } else if (val > maxPixel.getValue()) {
                        maxPixel.setVoxel(i, j, k, val);
                    }
                }
            }
        }
    }

    /**
     * Gets the mean Value of the Image3D
     *
     * @return The meanValue value
     */
    public float getMean() {
        if (!Float.isNaN(meanValue)) {
            return meanValue;
        }
        float total = 0.0F;
        for (int k = 0; k < sizez; k++) {
            for (int j = 0; j < sizey; j++) {
                for (int i = 0; i < sizex; i++) {
                    total += getPix(i, j, k);
                }
            }
        }

        meanValue = total / (float) (sizex * sizey * sizez);
        return meanValue;
    }

    /**
     * Gets the sum of absolute values of the Image3D
     *
     * @return The sumAbs value
     */
    public float getSumAbs() {
        float total = 0.0F;
        for (int k = 0; k < sizez; k++) {
            for (int j = 0; j < sizey; j++) {
                for (int i = 0; i < sizex; i++) {
                    total += getPix(i, j, k) * getPix(i, j, k);
                }
            }
        }
        return total;
    }

    /**
     * Gets the std dev value of the Image3D
     *
     * @return The sigma value
     */
    public float getSigma() {
        if (!Float.isNaN(stDev)) {
            return stDev;
        }
        if (!Float.isNaN(meanValue)) {
            getMean();
        }

        double total = 0.0;
        for (int k = 0; k < sizez; k++) {
            for (int j = 0; j < sizey; j++) {
                for (int i = 0; i < sizex; i++) {
                    double val = (double) (getPix(i, j, k) - meanValue);
                    total += val * val;
                }
            }
        }

        total /= (double) (sizex * sizey * sizez);
        stDev = (float) Math.sqrt(total);
        return stDev;
    }

    /**
     * Gets the maxPixel attribute of the Image3D object
     *
     * @return The maxPixel value
     */
    public Voxel3D getMaxPixel() {
        if (maxPixel == null) {
            computeMinMax();
        }

        return maxPixel;
    }

    /**
     * Cross correlation between this image and another one at a specified
     * location .
     *
     * @param other the other image
     * @param x0 the x-coordinate in this image
     * @param y0 the y-coordinate in this image
     * @param z0 the z-coordinate in this image
     * @param x1 the x-coordinate in other image
     * @param y1 the x-coordinate in other image
     * @param z1 the x-coordinate in other image
     * @param rx the radius x to compute correlation
     * @param ry the radius y to compute correlation
     * @param rz the radius z to compute correlation
     * @return the normalized (between -1 and 1) cross-correlation
     */
    public double crossCorrelationNormalized(Image3D other, int x0, int y0, int z0, int x1, int y1, int z1, int rx, int ry, int rz) {
        double cc;

        double meA = 0;
        double meB = 0;

        double sAA = 0;
        double sBB = 0;
        double sAB = 0;
        double dist2;

        double rx2 = rx * rx;
        double ry2 = ry * ry;
        double rz2 = rz * rz;

        double pixA, pixB;

        double c = 0;

        for (int zz = -rz; zz <= rz; zz++) {
            for (int xx = -rx; xx <= rx; xx++) {
                for (int yy = -ry; yy <= ry; yy++) {
                    dist2 = xx * xx / rx2 + yy * yy / ry2 + zz * zz / rz2;
                    if (dist2 < 1) {
                        meA += this.getPix(xx + x0, yy + y0, zz + z0);
                        meB += other.getPix(xx + x1, yy + y1, zz + z1);
                        c++;
                    }
                }
            }
        }
        meA /= c;
        meB /= c;


        for (int zz = -rz; zz <= rz; zz++) {
            for (int xx = -rx; xx <= rx; xx++) {
                for (int yy = -ry; yy <= ry; yy++) {
                    dist2 = xx * xx / rx2 + yy * yy / ry2 + zz * zz / rz2;
                    if (dist2 < 1) {
                        pixA = this.getPix(xx + x0, yy + y0, zz + z0) - meA;
                        pixB = other.getPix(xx + x1, yy + y1, zz + z1) - meB;
                        sAA += pixA * pixA;
                        sBB += pixB * pixB;
                        sAB += pixA * pixB;
                    }
                }
            }
        }
        cc = sAB / (Math.sqrt(sAA * sBB));

        return cc;
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
                    if (i >= 0 && j >= 0 && k >= 0 && i < sizex && j < sizey && k < sizez) {
                        dist = ((x - i) * (x - i)) / rx2 + ((y - j) * (y - j)) / ry2 + ((z - k) * (z - k)) / rz2;
                        if (dist <= 1.0) {
                            //t.putValue(index, );
                            pix[index] = getPix(i, j, k);
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
     * Gets the neighboring of a pixel (sphere)
     *
     * @param x Coordinate x of the pixel
     * @param y Coordinate y of the pixel
     * @param z Coordinate z of the pixel
     * @param radx Radius x of the neighboring
     * @param rady Radius y of the neighboring
     * @param radz Radius z of the neighboring
     * @param edm
     * @return The neigbor values in a array
     */
    public ArrayUtil getNeighborhoodSphere(int x, int y, int z, float radx, float rady, float radz, RealImage3D edm) {
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

        double edmvalue, edmtmp;

        int vx = (int) Math.ceil(radx);
        int vy = (int) Math.ceil(rady);
        int vz = (int) Math.ceil(radz);
        double[] pix = new double[(2 * vx + 1) * (2 * vy + 1) * (2 * vz + 1)];
        edmvalue = edm.getPixel(x, y, z);
        for (int k = z - vz; k <= z + vz; k++) {
            for (int j = y - vy; j <= y + vy; j++) {
                for (int i = x - vx; i <= x + vx; i++) {
                    if (i >= 0 && j >= 0 && k >= 0 && i < sizex && j < sizey && k < sizez) {
                        dist = ((x - i) * (x - i)) / rx2 + ((y - j) * (y - j)) / ry2 + ((z - k) * (z - k)) / rz2;
                        edmtmp = edm.getPixel(i, j, k);
                        if ((dist <= 1.0) && (edmtmp > edmvalue)) {
                            //t.putValue(index, );
                            pix[index] = getPix(i, j, k);
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
                    if ((i >= 0) && (j >= 0) && (k >= 0) && (i < sizex) && (j < sizey) && (k < sizez)) {
                        res.putValue(idx, getPix(i, j, k));
                    }
                    idx++;
                }
            }
        }
        res.setSize(idx);
        return res;
    }

    /**
     *
     * @param x
     * @param y
     * @param z
     * @return
     */
    public ArrayUtil getNeighborhoodCross3D(int x, int y, int z) {
        ArrayUtil res = new ArrayUtil(7);
        res.putValue(0, getPix(x, y, z));
        int idx = 1;
        if ((x + 1) < sizex) {
            res.putValue(idx, getPix(x + 1, y, z));
            idx++;
        }
        if ((x - 1) >= 0) {
            res.putValue(idx, getPix(x - 1, y, z));
            idx++;
        }
        if ((y + 1) < sizey) {
            res.putValue(idx, getPix(x, y + 1, z));
            idx++;
        }
        if ((y - 1) >= 0) {
            res.putValue(idx, getPix(x, y - 1, z));
            idx++;
        }
        if ((z + 1) < sizez) {
            res.putValue(idx, getPix(x, y, z + 1));
            idx++;
        }
        if ((z - 1) >= 0) {
            res.putValue(idx, getPix(x, y, z - 1));
            idx++;
        }


        res.setSize(idx);
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
    public ArrayUtil getNeighborhoodLayer(int x, int y, int z, float r0, float r1, IntImage3D water) {
        int index = 0;
        double r02 = r0 * r0;
        double r12 = r1 * r1;

        double dist;

        int vx = (int) Math.ceil(r1);
        int vy = (int) Math.ceil(r1);
        int vz = 0;
        double[] pix = new double[(2 * vx + 1) * (2 * vy + 1) * (2 * vz + 1)];
        int wat = 0;
        if (water != null) {
            wat = water.getPixel(x, y, z);
        }

        for (int k = z - vz; k <= z + vz; k++) {
            for (int j = y - vy; j <= y + vy; j++) {
                for (int i = x - vx; i <= x + vx; i++) {
                    if (i >= 0 && j >= 0 && k >= 0 && i < sizex && j < sizey && k < sizez) {
                        if (((water != null) && (water.getPixel(i, j, k) == wat)) || (water == null)) {
                            dist = ((x - i) * (x - i)) + ((y - j) * (y - j)) + ((z - k) * (z - k));
                            if ((dist >= r02) && (dist < r12)) {
                                //t.putValue(index, );
                                pix[index] = getPix(i, j, k);
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
     * @param r0 Minimu radius value
     * @param r1 Maximum radius value
     * @return
     */
    public ArrayUtil getNeighborhoodLayer(int x, int y, int z, float r0, float r1) {
        return this.getNeighborhoodLayer(x, y, z, r0, r1, null);
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
    public double[] radialDistribution(int x0, int y0, int z0, int maxR, IntImage3D water) {
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

    /**
     * Gets the neighboring of a pixel, with a kernel as an image
     *
     * @param x Coordinate x of the pixel
     * @param y Coordinate y of the pixel
     * @param z Coordinate z of the pixel
     * @param ker The kernel (0 = outside kernel, else inside)
     * @return The neigbor values in a array
     */
    public ArrayUtil getNeighborhood(Image3D ker, int x, int y, int z) {
        int voisx = ker.getSizex();
        int voisy = ker.getSizey();
        int voisz = ker.getSizez();
        int cx = (int) ker.getCenterX();
        int cy = (int) ker.getCenterY();
        int cz = (int) ker.getCenterZ();

        ArrayUtil t = new ArrayUtil((voisx * 2 + 1) * (voisy * 2 + 1) * (voisz * 2 + 1));
        int index = 0;
        for (int k = z - cz; k < z + voisz - cz; k++) {
            for (int j = y - cy; j < y + voisy - cy; j++) {
                for (int i = x - cx; i < x + voisx - cx; i++) {
                    if (i >= 0 && j >= 0 && k >= 0 && i < sizex && j < sizey && k < sizez) {
                        if (ker.getPix(cx + i - x, cy + j - y, cz + k - z) > 0) {
                            t.putValue(index, (float) getPix(i, j, k));
                            index++;
                        }
                    }
                }
            }
        }

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
                        if ((i >= 0) && (j >= 0) && (k >= 0) && (i < sizex) && (j < sizey) && (k < sizez)) {
                            pix.putValue(index, (float) getPix(i, j, k));
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

    /**
     *
     * @param ker
     * @param x
     * @param y
     * @param z
     * @return
     */
    public ArrayUtil getNeighborhoodGrayKernelAdd(IntImage3D ker, int x, int y, int z) {
        ArrayUtil pix = new ArrayUtil(ker.getNbPixel());
        int vx = ker.getSizex();
        int vy = ker.getSizey();
        int vz = ker.getSizez();
        int index = 0;
        int x0 = x - vx;
        int x1 = x + vx;
        int y0 = y - vy;
        int y1 = y + vy;
        int z0 = z - vz;
        int z1 = z + vz;
        for (int k = z0; k <= z1; k++) {
            for (int j = y0; j <= y1; j++) {
                for (int i = x0; i <= x1; i++) {
                    if (i >= 0 && j >= 0 && k >= 0 && i < sizex && j < sizey && k < sizez) {
                        pix.putValue(index, (float) getPix(i, j, k) + (float) ker.getPixel(i - x0, j - y0, k - z0));
                        index++;
                    }
                }
            }
        }
        pix.setSize(index);

        return pix;
    }

    /**
     *
     * @param ker
     * @param x
     * @param y
     * @param z
     * @return
     */
    public ArrayUtil getNeighborhoodGrayKernelMinus(IntImage3D ker, int x, int y, int z) {
        ArrayUtil pix = new ArrayUtil(ker.getNbPixel());
        int vx = ker.getSizex();
        int vy = ker.getSizey();
        int vz = ker.getSizez();
        int index = 0;
        int x0 = x - vx;
        int x1 = x + vx;
        int y0 = y - vy;
        int y1 = y + vy;
        int z0 = z - vz;
        int z1 = z + vz;
        float val;
        for (int k = z0; k <= z1; k++) {
            for (int j = y0; j <= y1; j++) {
                for (int i = x0; i <= x1; i++) {
                    if (i >= 0 && j >= 0 && k >= 0 && i < sizex && j < sizey && k < sizez) {
                        val = (float) getPix(i, j, k) - (float) ker.getPixel(i - x0, j - y0, k - z0);
                        if (val < 0) {
                            val = 0;
                        }
                        pix.putValue(index, val);

                        index++;
                    }
                }
            }
        }
        pix.setSize(index);

        return pix;
    }

    /**
     * Create a kernel neighorhood as an ellipsoid
     *
     * @param radx Radius x of the ellipsoid
     * @param rady Radius x of the ellipsoid
     * @param radz Radius x of the ellipsoid
     * @return The kernel as an array
     */
    public static int[] createKernelEllipsoid(float radx, float rady, float radz) {
        int vx = (int) Math.ceil(radx);
        int vy = (int) Math.ceil(rady);
        int vz = (int) Math.ceil(radz);
        int[] ker = new int[(2 * vx + 1) * (2 * vy + 1) * (2 * vz + 1)];
        double dist;

        double rx2 = radx * radx;
        double ry2 = rady * rady;
        double rz2 = radz * radz;


        if (rx2 != 0) {
            rx2 = 1.0 / rx2;
        } else {
            rx2 = 0;
        }
        if (ry2 != 0) {
            ry2 = 1.0 / ry2;
        } else {
            ry2 = 0;
        }
        if (rz2 != 0) {
            rz2 = 1.0 / rz2;
        } else {
            rz2 = 0;
        }

        int idx = 0;
        for (int k = -vz; k <= vz; k++) {
            for (int j = -vy; j <= vy; j++) {
                for (int i = -vx; i <= vx; i++) {
                    dist = ((double) (i * i)) * rx2 + ((double) (j * j)) * ry2 + ((double) (k * k)) * rz2;
                    if (dist <= 1.0) {
                        ker[idx] = 1;
                    } else {
                        ker[idx] = 0;
                    }
                    idx++;
                }
            }
        }

        return ker;
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
    public void insert(Image3D vol, int x0, int y0, int z0, boolean average) {
        int xx0 = Math.max(x0, 0);
        int yy0 = Math.max(y0, 0);
        int zz0 = Math.max(z0, 0);
        int x1 = Math.min(x0 + vol.getSizex(), getSizex());
        int y1 = Math.min(y0 + vol.getSizey(), getSizey());
        int z1 = Math.min(z0 + vol.getSizez(), getSizez());
        double pix;
        double pixo;
        for (int z = zz0; z < z1; z++) {
            for (int x = xx0; x < x1; x++) {
                for (int y = yy0; y < y1; y++) {
                    pix = vol.getPix(x - x0, y - y0, z - z0);
                    if (average) {
                        pixo = getPix(x - x0, y - y0, z - z0);
                        setPix(x, y, z, 0.5 * (pixo + pix));
                    } else {
                        setPix(x, y, z, pix);
                    }
                }
            }
        }
    }

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
    public Image3D extract(int x0, int y0, int z0, int rx, int ry, int rz, boolean sphere) {
        int x1 = x0 + rx;
        int y1 = y0 + ry;
        int z1 = z0 + rz;
        Image3D res;
        double r;
        double rx2 = rx * rx;
        double ry2 = ry * ry;
        double rz2 = rz * rz;
        double moy = getMean();

        if ((type == BYTE) || (type == SHORT)) {
            res = new IntImage3D(2 * rx, 2 * ry, 2 * rz);
        } else {
            res = new RealImage3D(2 * rx, 2 * ry, 2 * rz);
        }
        for (int z = z0 - rz; z <= z1; z++) {
            for (int x = x0 - rx; x <= x1; x++) {
                for (int y = y0 - ry; y <= y1; y++) {
                    if (sphere) {
                        r = (x - x0) * (x - x0) / rx2 + (y - y0) * (y - y0) / ry2 + (z - z0) * (z - z0) / rz2;
                        if (r <= 1) {
                            res.setPix(x - x0 + rx, y - y0 + ry, z - z0 + rz, getPix(x, y, z));
                        } else {
                            res.setPix(x - x0 + rx, y - y0 + ry, z - z0 + rz, moy);
                        }
                    } else {
                        res.setPix(x - x0 + rx, y - y0 + ry, z - z0 + rz, getPix(x, y, z));
                    }
                }
            }
        }
        return res;
    }

    /**
     * Cropping of the image
     *
     * @param newzerox Start of the crop in x
     * @param newzeroy Start of the crop in x
     * @param newzeroz Start of the crop in z
     * @param newsizex New size in x
     * @param newsizey New size in y
     * @param newsizez New size in z
     * @return The cropped image
     */
    public Image3D crop(int newzerox, int newzeroy, int newzeroz, int newsizex, int newsizey, int newsizez) {
        Image3D res;

        if ((type == BYTE) || (type == SHORT)) {
            res = new IntImage3D(newsizex, newsizey, newsizez);
        } else {
            res = new RealImage3D(newsizex, newsizey, newsizez);
        }
        int xmin = newzerox;
        int ymin = newzeroy;
        int zmin = newzeroz;

        for (int k = 0; k < newsizez; k++) {
            for (int j = 0; j < newsizey; j++) {
                for (int i = 0; i < newsizex; i++) {
                    res.setPix(i, j, k, this.getPix(i + xmin, j + ymin, k + zmin));
                }
            }
        }

        return res;
    }

    /**
     * Split a image into sub images
     *
     * @param axis The axis to split along
     * @param nb The number of splitted parts
     * @param overlap The overlap (in pixels)
     * @return An array of splitted images
     */
    public Image3D[] split(int axis, int nb, int overlap) {
        Image3D[] res = new Image3D[nb];

        int ss;
        int off;
        if (axis == ZAXIS) {
            ss = sizez / nb;
            off = sizez - ss * nb;
            //System.out.println("off=" + off);
            if (nb == 1) {
                res[0] = crop(0, 0, 0, sizex, sizey, sizez);
            } else {
                res[0] = crop(0, 0, 0, sizex, sizey, ss + overlap);
                for (int c = 1; c < nb - 1; c++) {
                    res[c] = crop(0, 0, ss * c - overlap, sizex, sizey, ss + 2 * overlap);
                }
                res[nb - 1] = crop(0, 0, ss * (nb - 1) - overlap, sizex, sizey, ss + overlap + off);
            }
        } else if (axis == YAXIS) {
            ss = sizey / nb;
            res[0] = crop(0, 0, 0, sizex, ss + overlap, sizez);
            for (int c = 1; c < nb - 1; c++) {
                res[c] = crop(0, ss * c - overlap, 0, sizex, ss + 2 * overlap, sizez);
            }
            res[nb - 1] = crop(0, ss * (nb - 1) - overlap, 0, sizex, ss + overlap, sizez);
        } else if (axis == XAXIS) {
            ss = sizex / nb;
            res[0] = crop(0, 0, 0, sizex, ss + overlap, sizez);
            for (int c = 1; c < nb - 1; c++) {
                res[c] = crop(0, ss * c - overlap, 0, sizex, ss + 2 * overlap, sizez);
            }
            res[nb - 1] = crop(0, ss * (nb - 1) - overlap, 0, sizex, ss + overlap, sizez);
        }

        return res;
    }

    /**
     * Join splitted images
     *
     * @param array Array of splitted images
     * @param axis Axis of split
     * @param overlap Nb of pixels overlapping
     * @return The joined image
     */
    static Image3D join(Image3D[] array, int axis, int overlap) {
        int nb = array.length;
        int sizeT;
        int sizeO;
        Image3D res = null;
        int type = array[0].getType();
        int sx = array[0].getSizex();
        int sy = array[0].getSizey();
        int sz = array[0].getSizez();
        int n;
        int a;
        int b;
        int nn;

        if (axis == ZAXIS) {
            sizeT = (sz - overlap) * (nb - 1) + (array[nb - 1].getSizez() - overlap);
            if ((type == BYTE) || (type == SHORT)) {
                if (nb == 1) {
                    res = new IntImage3D((IntImage3D) array[0]);
                } else {
                    res = new IntImage3D(sx, sy, sizeT);
                }
            } else {
                if (nb == 1) {
                    res = new RealImage3D(array[0]);
                } else {
                    res = new RealImage3D(sx, sy, sizeT);
                }
            }
            sizeO = sz - overlap;

            if (nb > 1) {
                for (int z = 0; z < sizeT; z++) {
                    n = z / (sz - overlap);
                    if (n >= nb) {
                        n = nb - 1;
                    }
                    if (n == 0) {
                        nn = z;
                    } else {
                        nn = z - n * (sz - overlap) + overlap;
                    }
                    //System.out.println("z=" + z + " n=" + n);
                    for (a = 0; a < sx; a++) {
                        for (b = 0; b < sy; b++) {
                            res.setPix(a, b, z, array[n].getPix(a, b, nn));
                        }
                    }
                }
            }
        }

        return res;
    }

    /**
     * Create a image with local maxima
     *
     * @param radx Radius in x
     * @param rady Radius in y
     * @param radz Radius in z
     * @param keep Keep original values of pixels
     * @return image with local maxima
     */
    public abstract Image3D createLocalMaximaImage(float radx, float rady, float radz, boolean keep);

    /**
     * Thresholding, create a binarised image
     *
     * @param threshLow Low threshold
     * @param threshHigh high threshold
     * @return The thresholded image
     */
    public abstract IntImage3D binarisation(int threshLow, int threshHigh);

    /**
     * compute the histogram of the image
     *
     * @return the histogram as an array
     */
    public abstract HistogramUtil getHistogram();

    /**
     *
     * @param vox the voxel
     * @param b interpolated if true, rounded if false
     * @return the pixel value
     */
    public abstract float getPix(Voxel3D vox, boolean b);

    /*
     * public void putImageInVolume(Image3D I, Vector3D V, Vector3D C) {
     * Vector3D X = new Vector3D(0, 0, 1); Vector3D Y = new Vector3D(0, 1, 0);
     * Vector3D XX = Y.crossProduct(V); Vector3D YY = XX.crossProduct(V);
     * XX.normalize(); YY.normalize(); System.out.println("XX=" + XX.getX() + "
     * " + XX.getY() + " " + XX.getZ()); System.out.println("YY=" + YY.getX() +
     * " " + YY.getY() + " " + YY.getZ()); float xx = XX.getX(); float xy =
     * XX.getY(); float xz = XX.getZ(); float yx = YY.getX(); float yy =
     * YY.getY(); float yz = YY.getZ(); int sx = I.getSizex(); int sy =
     * I.getSizey(); float cx = C.getX(); float cy = C.getY(); float cz =
     * C.getZ(); float ii; float jj; for (float i = 0; i < sx - 1.9; i += 0.1) {
     * for (float j = 0; j < sy - 1.9; j += 0.1) { ii = i - sx / 2; jj = -j + sy
     * / 2; this.setPix((int) (cx + ii * xx + jj * yx), (int) (cy + ii * xy + jj
     * * yy), (int) (cz + ii * xz + jj * yz), I.getPix(i, j, 0)); } } }
     */
}
