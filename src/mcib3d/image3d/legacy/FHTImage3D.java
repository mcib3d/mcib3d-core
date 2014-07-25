package mcib3d.image3d.legacy;

import mcib3d.geom.Voxel3D;
import mcib3d.geom.Vector3D;
import ij.*;
import ij.process.*;
//import java.awt.*;
import mcib3d.utils.*;

/**
Copyright (C) Thomas Boudier

License:
This program is free software; you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation; either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *

/*
 *  Compute the 3D Fast Harley Transform of a 32-bit image IJstack.
 *  Sizes may not be powers of 2, but a slow transform
 *  technique is used if the number of slices is not a power of 2.
 *  The normalization is such that the forward and inverse transforms are
 *  performed by the same method.
 *  Cedric MESSAOUDI
 *  Version 0 21/11/2005 most of the code is taken from Bob Dougherty's FHT3D
 *  Much is the code is from FFT_Filter by Joachim Walter.  Some items are from
 *  the related FHT by Wayne Rasband.  The following notice is from the FHT source code in ImageJ:
 *  This class contains a Java implementation of the Fast Hartley Transform.
 *  It is based on Pascal code in NIH Image contributed by Arlo Reeves
 *  (http://rsb.info.nih.gov/ij/docs/ImageFFT/). The Fast Hartley Transform was
 *  restricted by U.S. Patent No. 4,646,256, but was placed in the public domain
 *  by Stanford University in 1995 and is now freely available.
 */
/**
 *  Description of the Class
 *
 * @author     cedric
 * @created    21 novembre 2005
 */
public class FHTImage3D extends RealImage3D {
    //float[] data;
    //int sizex;
    //int sizey;
    //int sizez;

    boolean frequencyDomain = false;
    boolean centered = false;

    /**
     *  Constructor for the FHTImage3D object the transform is automatically
     *  launched
     *
     * @param stack
     */
    public FHTImage3D(ImageStack stack) {
        this(stack, true);
    }

    /**
     *  Constructor for the FHTImage3D object
     *
     * @param stack
     * @param  doTransform  Description of the Parameter
     */
    public FHTImage3D(ImageStack stack, boolean doTransform) {
        //float[] data = initFromStack(IJstack);
        super(stack);
        if (doTransform) {
            FHT3D();
        } else {
            centered = true;
        }
    }

    /**
     *  Constructor for the FHTImage3D object
     *
     * @param  ip  Description of the Parameter
     */
    public FHTImage3D(ImageProcessor ip) {
        this(ip, true);
    }

    /**
     *  Constructor for the FHTImage3D object
     *
     * @param  ip           Description of the Parameter
     * @param  doTransform  Description of the Parameter
     */
    public FHTImage3D(ImageProcessor ip, boolean doTransform) {
        super(ip.duplicate());
        //float[] data = initFromImage(ip);
        if (doTransform) {
            FHT3D();
        } else {
            centered = true;
        }
    }

    /**
     *  Constructor for the FHTImage3D object
     *
     * @param  img  Description of the Parameter
     */
    public FHTImage3D(Image3D img) {
        this(img, true);
    }

    /**
     *  Constructor for the FHTImage3D object
     *
     * @param  img          Description of the Parameter
     * @param  doTransform  Description of the Parameter
     */
    public FHTImage3D(Image3D img, boolean doTransform) {
        super(img);
        if (doTransform) {
            FHT3D();
        } else {
            centered = true;
        }
    }

    /**
     *  Constructor for the FHTImage3D object
     *
     * @param  sizex        Description of the Parameter
     * @param  sizey        Description of the Parameter
     * @param  sizez        Description of the Parameter
     * @param  doTransform  Description of the Parameter
     */
    public FHTImage3D(int sizex, int sizey, int sizez, boolean doTransform) {
        super(sizex, sizey, sizez);
        if (doTransform) {
            FHT3D();
        } else {
            centered = true;
        }
    }

    /**
     *  Description of the Method
     *
     * @return    Description of the Return Value
     */
    public FHTImage3D duplicate() {
        ImageStack tmp = new ImageStack(sizex, sizey);
        for (int i = 1; i <= sizez; i++) {
            tmp.addSlice(IJstack.getSliceLabel(i), IJstack.getPixels(i));
        }
        FHTImage3D res = new FHTImage3D(tmp);
        res.centered = centered;
        res.frequencyDomain = frequencyDomain;
        return res;
    }

    /**
     *  Gets the IJstack attribute of the FHTImage3D object
     *
     * @return    The IJstack value
     */
    public ImageStack getStack() {
        return IJstack;
    }

    /**
     *  Gets the powerSpectrum attribute of the FHTImage3D object
     *
     * @return    The powerSpectrum value
     */
    public ImageStack getPowerSpectrum() {
        return getPowerSpectrum(false);
    }

    /**
     *  Gets the powerStack attribute of the FHTImage3D object
     *
     * @param  log  Description of the Parameter
     * @return      The powerStack value
     */
    public ImageStack getPowerSpectrum(boolean log) {
        FloatProcessor tmp;
        double n;
        double value;
        ImageStack stack = new ImageStack(sizex, sizey);
        for (int k = 0; k < sizez; k++) {
            tmp = new FloatProcessor(sizex, sizey);
            for (int x = 0; x < sizex; x++) {
                for (int y = 0; y < sizey; y++) {
                    n = getNorm(x, y, k);
                    value = n * n;
                    if (log) {
                        value = Math.log(value);
                    }
                    tmp.putPixelValue(x, y, (float) value);
                }
            }
            stack.addSlice("" + (k + 1), tmp);
        }
        return stack;
    }

    /**
     *  Gets the value attribute of the FHTImage3D object
     *
     * @param  x  x coordinate
     * @param  y  y coordinate
     * @param  z  z coordinate
     * @return    The value
     */
    protected float getValue(int x, int y, int z) {
        return getPixel(x, y, z);
    }

    /**
     *  Gets the real attribute of the FHTImage3D object
     *
     * @param  x  x coordinate
     * @param  y  y coordinate
     * @param  z  z coordinate
     * @return    The real value
     */
    public float getReal(int x, int y, int z) {
        return (0.5f * (getValue(x, y, z) + getValue((sizex - x) % sizex, (sizey - y) % sizey, (sizez - z) % sizez)));
    }

    /**
     *  Gets the norm of the FHTImage3D pixel
     *
     * @param  x  x coordinate
     * @param  y  y coordinate
     * @param  z  z coordinate
     * @return    The power value
     */
    public double getNorm(int x, int y, int z) {
        float r = getReal(x, y, z);
        float i = getImag(x, y, z);
        return Math.sqrt(r * r + i * i);
    }

    /**
     *  Gets the angle of the FHTImage3D pixel
     *
     * @param  x  x coordinate
     * @param  y  y coordinate
     * @param  z  z coordinate
     * @return    The angle value (in radians)
     */
    public double getAngle(int x, int y, int z) {
        float r = getReal(x, y, z);
        float i = getImag(x, y, z);

        return (float) Math.atan(i / r);
    }

    /**
     *  Gets the imag attribute of the FHTImage3D object
     *
     * @param  x  x coordinate
     * @param  y  y coordinate
     * @param  z  z coordinate
     * @return    The imag value
     */
    public float getImag(int x, int y, int z) {
        return (0.5f * (getValue(x, y, z) - getValue((sizex - x) % sizex, (sizey - y) % sizey, (sizez - z) % sizez)));
    }

    /**
     *  Description of the Method
     *
     * @return    Description of the Return Value
     */
    public FHTImage3D doForwardTransform() {
        if (!frequencyDomain) {
            FHT3D();
        }
        return this;
    }

    /**
     *  Description of the Method
     *
     * @return    Description of the Return Value
     */
    public FHTImage3D doInverseTransform() {
        if (frequencyDomain) {
            FHT3D();
        }
        return this;
    }

    /**
     *  Cross Correlation 3D on ImageStack
     *
     * @param  img1  First IJstack
     * @param  img2  Second IJstack
     * @return       Correlation IJstack
     */
    public static ImageStack crossCorrelation(ImageStack img1, ImageStack img2) {
        FHTImage3D tmp = Xcorr(img1, img2);
        if (tmp.centered) {
            return tmp.getStack();
        } else {
            return tmp.swapQuadrants().getStack();
        }
    }

    /**
     *  Cross Correlation 3D on Image3D
     *
     * @param  img1  First Image3D
     * @param  img2  Second Image3D
     * @return       Correlation IJstack
     */
    public static ImageStack crossCorrelation(Image3D img1, Image3D img2) {
        FHTImage3D tmp = Xcorr(img1, img2);
        if (tmp.centered) {
            return tmp.getStack();
        } else {
            return tmp.swapQuadrants().getStack();
        }
    }

    /**
     *  Description of the Method
     *
     * @param  fht1  Description of the Parameter
     * @param  fht2  Description of the Parameter
     * @return       Description of the Return Value
     */
    public static FHTImage3D Xcorr(FHTImage3D fht1, FHTImage3D fht2) {
        //new ImagePlus("fht1", fht1).show();
        //new ImagePlus("fht2", fht2).show();
        FHTImage3D fhtres = mult(fht1, fht2, true);
        //new ImagePlus("fht mult", fht1).show();
        fhtres.FHT3D();
        //new ImagePlus("image correlation", fht1).show();
        return fhtres;
    }

    /**
     *  Description of the Method
     *
     * @param  img1  Description of the Parameter
     * @param  img2  Description of the Parameter
     * @return       Description of the Return Value
     */
    public static FHTImage3D Xcorr(ImageStack img1, ImageStack img2) {
        FHTImage3D fht1 = new FHTImage3D(img1);
        FHTImage3D fht2 = new FHTImage3D(img2);
        return Xcorr(fht1, fht2);
    }

    /**
     *  Description of the Method
     *
     * @param  img1  Description of the Parameter
     * @param  img2  Description of the Parameter
     * @return       Description of the Return Value
     */
    public static FHTImage3D Xcorr(ImageProcessor img1, ImageProcessor img2) {
        FHTImage3D fht1 = new FHTImage3D(img1);
        FHTImage3D fht2 = new FHTImage3D(img2);
        return Xcorr(fht1, fht2);
    }

    /**
     *  Description of the Method
     *
     * @param  img1  Description of the Parameter
     * @param  img2  Description of the Parameter
     * @return       Description of the Return Value
     */
    public static FHTImage3D Xcorr(Image3D img1, Image3D img2) {
        FHTImage3D fht1 = new FHTImage3D(img1);
        FHTImage3D fht2 = new FHTImage3D(img2);
        return Xcorr(fht1, fht2);
    }

    /**
     *  Gets the maxCorrelation attribute of the FHTImage3D class
     *
     * @param  img1  Description of the Parameter
     * @param  img2  Description of the Parameter
     * @return       The maxCorrelation value
     */
    public static Voxel3D getMaxCorrelation(ImageProcessor img1, ImageProcessor img2) {
        return getTranslation(Xcorr(img1, img2));
    }

    /**
     *  Gets the maxCorrelation attribute of the FHTImage3D class
     *
     * @param  img1  Description of the Parameter
     * @param  img2  Description of the Parameter
     * @return       The maxCorrelation value
     */
    public static Voxel3D getMaxCorrelation(ImageStack img1, ImageStack img2) {
        return getTranslation(Xcorr(img1, img2));
    }

    /**
     *  Gets the maxCorrelation attribute of the FHTImage3D class
     *
     * @param  fht1  Description of the Parameter
     * @param  fht2  Description of the Parameter
     * @return       The maxCorrelation value
     */
    public static Voxel3D getMaxCorrelation(FHTImage3D fht1, FHTImage3D fht2) {
        return getTranslation(Xcorr(fht1, fht2));
    }

    /**
     *  Gets the maxCorrelation attribute of the FHTImage3D class
     *
     * @param  img1  Description of the Parameter
     * @param  img2  Description of the Parameter
     * @return       The maxCorrelation value
     */
    public static Voxel3D getMaxCorrelation(Image3D img1, Image3D img2) {
        return getTranslation(Xcorr(img1, img2));
    }

    /**
     *  Gets the maxCorrelation attribute of the FHTImage3D class
     *
     * @param  img1   Description of the Parameter
     * @param  img2   Description of the Parameter
     * @param  RmaxX  Description of the Parameter
     * @param  RmaxY  Description of the Parameter
     * @param  RmaxZ  Description of the Parameter
     * @return        The maxCorrelation value
     */
    public static Voxel3D getMaxCorrelation(Image3D img1, Image3D img2, int RmaxX, int RmaxY, int RmaxZ) {
        return getTranslation(Xcorr(img1, img2), RmaxX, RmaxY, RmaxZ);
    }

    /**
     *  Gets the maxCorrelation attribute of the FHTImage3D class
     *
     * @param  img1   Description of the Parameter
     * @param  img2   Description of the Parameter
     * @param  RmaxX  Description of the Parameter
     * @param  RmaxY  Description of the Parameter
     * @param  RmaxZ  Description of the Parameter
     * @return        The maxCorrelation value
     */
    public static Voxel3D getMaxCorrelation(ImageStack img1, ImageStack img2, int RmaxX, int RmaxY, int RmaxZ) {
        return getTranslation(Xcorr(img1, img2), RmaxX, RmaxY, RmaxZ);
    }

    /**
     *  Gets the maxCorrelation attribute of the FHTImage3D class
     *
     * @param  img1   Description of the Parameter
     * @param  img2   Description of the Parameter
     * @param  RmaxX  Description of the Parameter
     * @param  RmaxY  Description of the Parameter
     * @return        The maxCorrelation value
     */
    public static Voxel3D getMaxCorrelation(ImageProcessor img1, ImageProcessor img2, int RmaxX, int RmaxY) {
        return getTranslation(Xcorr(img1, img2), RmaxX, RmaxY, 10);
    }

    /**
     *  Gets the maxCorrelation attribute of the FHTImage3D class
     *
     * @param  fht1   Description of the Parameter
     * @param  fht2   Description of the Parameter
     * @param  RmaxX  Description of the Parameter
     * @param  RmaxY  Description of the Parameter
     * @param  RmaxZ  Description of the Parameter
     * @return        The maxCorrelation value
     */
    public static Voxel3D getMaxCorrelation(FHTImage3D fht1, FHTImage3D fht2, int RmaxX, int RmaxY, int RmaxZ) {
        return getTranslation(Xcorr(fht1, fht2), RmaxX, RmaxY, RmaxZ);
    }

    /**
     *  Gets the translation attribute of the FHTImage3D class
     *
     * @param  xcorr  Description of the Parameter
     * @param  RmaxX  Description of the Parameter
     * @param  RmaxY  Description of the Parameter
     * @param  RmaxZ  Description of the Parameter
     * @return        The translation value
     */
    public static Voxel3D getTranslation(FHTImage3D xcorr, int RmaxX, int RmaxY, int RmaxZ) {
        int sizex = xcorr.sizex;
        int sizey = xcorr.sizey;
        int sizez = xcorr.sizez;
        int centerx = (int) ((sizex) / 2.0F);
        int centery = (int) ((sizey) / 2.0F);
        int centerz = (int) ((sizez) / 2.0F);
        if (xcorr.centered) {
            centerx = 0;
            centery = 0;
            centerz = 0;
        }
        Voxel3D max = new Voxel3D(centerx, centery, centerz, xcorr.getPixel(centerx, centery, centerz));

        for (int k = 0; k < sizez; k++) {
            int dz = (k >= sizez - centerz) ? k - sizez : k;
            for (int j = 0; j < sizey; j++) {
                int dy = (j >= sizey - centery) ? j - sizey : j;
                for (int i = 0; i < sizex; i++) {
                    int dx = (i >= sizex - centerx) ? i - sizex : i;
                    if (Math.abs(dx) <= RmaxX && Math.abs(dy) <= RmaxY && Math.abs(dz) <= RmaxZ) {
                        double val = xcorr.getPixel(i, j, k);
                        if (val > max.getValue()) {
                            max.setVoxel(dx, dy, dz, val);
                        }
                    }
                }
            }
        }
        return max;
    }

    /**
     *  Gets the translation attribute of the FHTImage3D class
     *
     * @param  xcorr  Description of the Parameter
     * @return        The translation value
     */
    public static Voxel3D getTranslation(FHTImage3D xcorr) {
        Voxel3D max = xcorr.getMaxPixel();
        int sizex = xcorr.sizex;
        int sizey = xcorr.sizey;
        int sizez = xcorr.sizez;
        int centerx = (int) ((sizex) / 2.0F);
        int centery = (int) ((sizey) / 2.0F);
        int centerz = (int) ((sizez) / 2.0F);
        if (!xcorr.centered) {
            if (max.getX() >= sizex - centerx) {
                max.setX(max.getX() - sizex);
            }
            if (max.getY() >= sizey - centery) {
                max.setY(max.getY() - sizey);
            }
            if (max.getZ() >= sizez - centerz) {
                max.setZ(max.getZ() - sizez);
            }
        }
        return max;
    }

    /**
     *  Description of the Method
     *
     * @param  fht1       Description of the Parameter
     * @param  fht2       Description of the Parameter
     * @param  conjugate  Description of the Parameter
     * @return            Description of the Return Value
     */
    private static FHTImage3D mult(FHTImage3D fht1, FHTImage3D fht2, boolean conjugate) {
        int sizex = fht1.sizex;
        int sizey = fht1.sizey;
        int sizez = fht1.sizez;
        int sizexy = sizex * sizey;
        int kC;
        int jC;
        int iC;
        float h2r;
        float h2i;
        //float[] data1 = fht1.data;
        //float[] data2 = fht2.data;
        FHTImage3D res = new FHTImage3D(sizex, sizey, sizez, false);
        //ImageStack tmp = new ImageStack(sizex, sizey);
        for (int k = 0; k < sizez; k++) {
            IJ.showStatus("FHT Mult " + (int) (100.0 * k / sizez) + " %");
            float[] result = new float[sizexy];
            kC = (sizez - k) % sizez;
            for (int j = 0; j < sizey; j++) {
                jC = (sizey - j) % sizey;
                for (int i = 0; i < sizex; i++) {
                    iC = (sizex - i) % sizex;
                    h2r = (fht2.getPixel(i, j, k) + fht2.getPixel(iC, jC, kC)) / 2;
                    h2i = (fht2.getPixel(i, j, k) - fht2.getPixel(iC, jC, kC)) / 2;
                    if (conjugate) {
                        //result[i + j * sizex] = (fht1.getPixel(i, j, k) * h2r - fht1.getPixel(iC, jC, kC) * h2i) / 2;
                        res.putPixel(i, j, k, (fht1.getPixel(i, j, k) * h2r - fht1.getPixel(iC, jC, kC) * h2i) / 2);
                    } else {
                        //result[i + j * sizex] = (fht1.getPixel(i, j, k) * h2r + fht1.getPixel(iC, jC, kC) * h2i) / 2;
                        res.putPixel(i, j, k, (fht1.getPixel(i, j, k) * h2r + fht1.getPixel(iC, jC, kC) * h2i) / 2);
                    }
                }
            }
            //tmp.addSlice("", result);
        }
        //FHTImage3D res = new FHTImage3D(tmp);
        res.frequencyDomain = fht1.frequencyDomain;
        res.centered = fht1.centered;
        return res;
    }

    /**
     *  Description of the Method
     *
     * @return    Description of the Return Value
     */
    public FHTImage3D center() {
        if (!centered) {
            swapQuadrants();
        }
        return this;
    }

    /**
     *  Sets the center attribute of the FHTImage3D object
     *
     * @param  ce  The new center value
     */
    public void setCenter(boolean ce) {
        centered = ce;
    }

    /**
     *  Description of the Method
     *
     * @return    Description of the Return Value
     */
    public FHTImage3D decenter() {
        if (centered) {
            swapQuadrants();
        }
        return this;
    }

    /**
     *  Description of the Method
     *
     * @return    Description of the Return Value
     */
    public FHTImage3D swapQuadrants() {
        //int sizexy = sizex * sizey;
        int centerx = sizex / 2;
        int centery = sizey / 2;
        int centerz = sizez / 2;
        for (int k = 0; k < sizez; k++) {
            int nk = k + centerz;
            if (nk >= sizez) {
                nk -= sizez;
            }
            for (int j = 0; j < sizey; j++) {
                int nj = j + centery;
                if (nj >= sizey) {
                    nj -= sizey;
                }
                for (int i = 0; i < centerx; i++) {
                    int ni = i + centerx;
                    if (ni >= sizex) {
                        ni -= sizex;
                    }
                    float tmp = getPixel(i, j, k);
                    putPixel(i, j, k, getPixel(ni, nj, nk));
                    putPixel(ni, nj, nk, tmp);
                }
            }
        }
        centered = !centered;
        return this;
    }

    /**
     *  Description of the Method
     *
     * @param  w  Description of the Parameter
     * @return    Description of the Return Value
     */
    boolean powerOf2Size(int w) {
        int i = 2;
        while (i < w) {
            i *= 2;
        }
        return i == w;
    }

    /**
     *  Description of the Method
     */
    public void FHT3D() {
        if (!frequencyDomain && centered) {
            centered = false;
        }
        float[] u = new float[sizex];
        boolean pow2x = powerOf2Size(sizex);
        boolean pow2y = powerOf2Size(sizey);
        boolean pow2z = powerOf2Size(sizez);
        float[] s = new float[1];
        float[] c = new float[1];
        float[] cas = new float[1];
        float[] work = new float[1];

        if (pow2x) {
            s = new float[sizex / 4];
            c = new float[sizex / 4];
            makeSinCosTables(sizex, s, c);
        } else {
            cas = hartleyCoefs(sizex);
            work = new float[sizex];
        }
        for (int k = 0; k < sizez; k++) {
            IJ.showStatus("FHT step 1/4 " + (int) (100.0 * k / (double) sizez) + "%");
            for (int j = 0; j < sizey; j++) {
                //int jj = j * sizex + kk;
                for (int i = 0; i < sizex; i++) {
                    u[i] = getPixel(i, j, k);
                }
                if (pow2x) {
                    dfht3(u, 0, sizex, s, c);
                } else {
                    slowHT(u, cas, sizex, work);
                }
                for (int i = 0; i < sizex; i++) {
                    putPixel(i, j, k, u[i]);
                }
            }
        }

        u = new float[sizey];
        if (pow2y) {
            s = new float[sizey / 4];
            c = new float[sizey / 4];
            makeSinCosTables(sizey, s, c);
        } else {
            cas = hartleyCoefs(sizey);
            work = new float[sizey];
        }
        for (int k = 0; k < sizez; k++) {
            IJ.showStatus("FHT step 2/4 " + (int) (100.0 * k / (double) sizez) + "%");
            for (int i = 0; i < sizex; i++) {
                for (int j = 0; j < sizey; j++) {
                    u[j] = getPixel(i, j, k);
                }
                if (pow2y) {
                    dfht3(u, 0, sizey, s, c);
                } else {
                    slowHT(u, cas, sizey, work);
                }
                for (int j = 0; j < sizey; j++) {
                    putPixel(i, j, k, u[j]);
                }
            }
        }

        u = new float[sizez];
        if (pow2z) {
            s = new float[sizez / 4];
            c = new float[sizez / 4];
            makeSinCosTables(sizez, s, c);
        } else {
            cas = hartleyCoefs(sizez);
            work = new float[sizez];
        }
        //if(IJ.getNumber("0 for fast, 1 for slow",0)==0){

        for (int j = 0; j < sizey; j++) {
            IJ.showStatus("FHT step 3/4 " + (int) (100.0 * j / (double) sizey) + "%");
            int jj = j * sizex;
            for (int i = 0; i < sizex; i++) {
                for (int k = 0; k < sizez; k++) {
                    u[k] = getPixel(i, j, k);
                }
                if (pow2z) {
                    dfht3(u, 0, sizez, s, c);
                } else {
                    slowHT(u, cas, sizez, work);
                }
                for (int k = 0; k < sizez; k++) {
                    putPixel(i, j, k, u[k]);
                }
            }
        }

        //Convert to actual Hartley transform
        float A;
        float B;
        float C;
        float D;
        float E;
        float F;
        float G;
        float H;
        int k1C;
        int k2C;
        int k3C;
        for (int k3 = 0; k3 <= sizez / 2; k3++) {
            IJ.showStatus("FHT step 4/4 " + (int) (100.0 * k3 / (double) sizez) + "%");
            k3C = (sizez - k3) % sizez;
            for (int k2 = 0; k2 <= sizey / 2; k2++) {
                k2C = (sizey - k2) % sizey;
                for (int k1 = 0; k1 <= sizex / 2; k1++) {
                    k1C = (sizex - k1) % sizex;
                    A = getPixel(k1, k2C, k3);
                    B = getPixel(k1C, k2, k3);
                    C = getPixel(k1, k2, k3C);
                    D = getPixel(k1C, k2C, k3C);
                    E = getPixel(k1, k2C, k3C);
                    F = getPixel(k1C, k2, k3C);
                    G = getPixel(k1, k2, k3);
                    H = getPixel(k1C, k2C, k3);
                    /*
                     *  A = data[k3][k1 + w * k2C];
                     *  B = data[k3][k1C + w * k2];
                     *  C = data[k3C][k1 + w * k2];
                     *  D = data[k3C][k1C + w * k2C];
                     *  E = data[k3C][k1 + w * k2C];
                     *  F = data[k3C][k1C + w * k2];
                     *  G = data[k3][k1 + w * k2];
                     *  H = data[k3][k1C + w * k2C];
                     */
                    putPixel(k1, k2, k3, (A + B + C - D) / 2);
                    putPixel(k1, k2, k3C, (E + F + G - H) / 2);
                    putPixel(k1, k2C, k3, (G + H + E - F) / 2);
                    putPixel(k1, k2C, k3C, (C + D + A - B) / 2);
                    putPixel(k1C, k2, k3, (H + G + F - E) / 2);
                    putPixel(k1C, k2, k3C, (D + C + B - A) / 2);
                    putPixel(k1C, k2C, k3, (B + A + D - C) / 2);
                    putPixel(k1C, k2C, k3C, (F + E + H - G) / 2);
                    /*
                     *  data[k3][k1 + w * k2] = (A + B + C - D) / 2;
                     *  data[k3C][k1 + w * k2] = (E + F + G - H) / 2;
                     *  data[k3][k1 + w * k2C] = (G + H + E - F) / 2;
                     *  data[k3C][k1 + w * k2C] = (C + D + A - B) / 2;
                     *  data[k3][k1C + w * k2] = (H + G + F - E) / 2;
                     *  data[k3C][k1C + w * k2] = (D + C + B - A) / 2;
                     *  data[k3][k1C + w * k2C] = (B + A + D - C) / 2;
                     *  data[k3C][k1C + w * k2C] = (F + E + H - G) / 2;
                     */
                }
            }
        }

        //normalize
        float norm = (float) Math.sqrt(sizez * sizey * sizex);
        divideBy(norm);

        frequencyDomain = !frequencyDomain;
    }

    /**
     *  Description of the Method
     *
     * @param  maxPixel  Description of the Parameter
     * @return      Description of the Return Value
     */
    float[] hartleyCoefs(int max) {
        float[] cas = new float[max * max];
        int ind = 0;
        for (int n = 0; n < max; n++) {
            for (int k = 0; k < max; k++) {
                double arg = (2 * Math.PI * k * n) / max;
                cas[ind++] = (float) (Math.cos(arg) + Math.sin(arg));
            }
        }
        return cas;
    }

    /**
     *  Description of the Method
     *
     * @param  u     Description of the Parameter
     * @param  cas   Description of the Parameter
     * @param  maxPixel   Description of the Parameter
     * @param  work  Description of the Parameter
     */
    void slowHT(float[] u, float[] cas, int max, float[] work) {
        int ind = 0;
        for (int k = 0; k < max; k++) {
            float sum = 0;
            for (int n = 0; n < max; n++) {
                sum += u[n] * cas[ind++];
            }
            work[k] = sum;
        }
        for (int k = 0; k < max; k++) {
            u[k] = work[k];
        }
    }

    /**
     *  Description of the Method
     *
     * @param  maxN  Description of the Parameter
     * @param  s     Description of the Parameter
     * @param  c     Description of the Parameter
     */
    void makeSinCosTables(int maxN, float[] s, float[] c) {
        int n = maxN / 4;
        double theta = 0.0;
        double dTheta = 2.0 * Math.PI / maxN;
        for (int i = 0; i < n; i++) {
            c[i] = (float) Math.cos(theta);
            s[i] = (float) Math.sin(theta);
            theta += dTheta;
        }
    }


    /*
     *  An optimized real FHT
     */
    /**
     *  Description of the Method
     *
     * @param  x     Description of the Parameter
     * @param  base  Description of the Parameter
     * @param  maxN  Description of the Parameter
     * @param  s     Description of the Parameter
     * @param  c     Description of the Parameter
     */
    void dfht3(float[] x, int base, int maxN, float[] s, float[] c) {
        int i;
        int stage;
        int gpNum;
        int gpIndex;
        int gpSize;
        int numGps;
        int Nlog2;
        int bfNum;
        int numBfs;
        int Ad0;
        int Ad1;
        int Ad2;
        int Ad3;
        int Ad4;
        int CSAd;
        float rt1;
        float rt2;
        float rt3;
        float rt4;

        Nlog2 = log2(maxN);
        BitRevRArr(x, base, Nlog2, maxN);
        //bitReverse the input array
        gpSize = 2;
        //first & second stages - do radix 4 butterflies once thru
        numGps = maxN / 4;
        for (gpNum = 0; gpNum < numGps; gpNum++) {
            Ad1 = gpNum * 4;
            Ad2 = Ad1 + 1;
            Ad3 = Ad1 + gpSize;
            Ad4 = Ad2 + gpSize;
            rt1 = x[base + Ad1] + x[base + Ad2];
            // a + b
            rt2 = x[base + Ad1] - x[base + Ad2];
            // a - b
            rt3 = x[base + Ad3] + x[base + Ad4];
            // c + d
            rt4 = x[base + Ad3] - x[base + Ad4];
            // c - d
            x[base + Ad1] = rt1 + rt3;
            // a + b + (c + d)
            x[base + Ad2] = rt2 + rt4;
            // a - b + (c - d)
            x[base + Ad3] = rt1 - rt3;
            // a + b - (c + d)
            x[base + Ad4] = rt2 - rt4;
            // a - b - (c - d)
        }
        if (Nlog2 > 2) {
            // third + stages computed here
            gpSize = 4;
            numBfs = 2;
            numGps = numGps / 2;
            //IJ.write("FFT: dfht3 "+Nlog2+" "+numGps+" "+numBfs);
            for (stage = 2; stage < Nlog2; stage++) {
                for (gpNum = 0; gpNum < numGps; gpNum++) {
                    Ad0 = gpNum * gpSize * 2;
                    Ad1 = Ad0;
                    // 1st butterfly is different from others - no mults needed
                    Ad2 = Ad1 + gpSize;
                    Ad3 = Ad1 + gpSize / 2;
                    Ad4 = Ad3 + gpSize;
                    rt1 = x[base + Ad1];
                    x[base + Ad1] = x[base + Ad1] + x[base + Ad2];
                    x[base + Ad2] = rt1 - x[base + Ad2];
                    rt1 = x[base + Ad3];
                    x[base + Ad3] = x[base + Ad3] + x[base + Ad4];
                    x[base + Ad4] = rt1 - x[base + Ad4];
                    for (bfNum = 1; bfNum < numBfs; bfNum++) {
                        // subsequent BF's dealt with together
                        Ad1 = bfNum + Ad0;
                        Ad2 = Ad1 + gpSize;
                        Ad3 = gpSize - bfNum + Ad0;
                        Ad4 = Ad3 + gpSize;

                        CSAd = bfNum * numGps;
                        rt1 = x[base + Ad2] * c[CSAd] + x[base + Ad4] * s[CSAd];
                        rt2 = x[base + Ad4] * c[CSAd] - x[base + Ad2] * s[CSAd];

                        x[base + Ad2] = x[base + Ad1] - rt1;
                        x[base + Ad1] = x[base + Ad1] + rt1;
                        x[base + Ad4] = x[base + Ad3] + rt2;
                        x[base + Ad3] = x[base + Ad3] - rt2;

                    }
                    /*
                     *  end bfNum loop
                     */
                }
                /*
                 *  end gpNum loop
                 */
                gpSize *= 2;
                numBfs *= 2;
                numGps = numGps / 2;
            }
            /*
             *  end for all stages
             */
        }
        /*
         *  end if Nlog2 > 2
         */
    }

    /**
     *  Description of the Method
     *
     * @param  x  Description of the Parameter
     * @return    Description of the Return Value
     */
    int log2(int x) {
        int count = 15;
        while (!btst(x, count)) {
            count--;
        }
        return count;
    }

    /**
     *  Description of the Method
     *
     * @param  x    Description of the Parameter
     * @param  bit  Description of the Parameter
     * @return      Description of the Return Value
     */
    private boolean btst(int x, int bit) {
        //int mask = 1;
        return ((x & (1 << bit)) != 0);
    }

    /**
     *  Description of the Method
     *
     * @param  x       Description of the Parameter
     * @param  base    Description of the Parameter
     * @param  bitlen  Description of the Parameter
     * @param  maxN    Description of the Parameter
     */
    void BitRevRArr(float[] x, int base, int bitlen, int maxN) {
        int l;
        float[] tempArr = new float[maxN];
        for (int i = 0; i < maxN; i++) {
            l = BitRevX(i, bitlen);
            //i=1, l=32767, bitlen=15
            tempArr[i] = x[base + l];
        }
        for (int i = 0; i < maxN; i++) {
            x[base + i] = tempArr[i];
        }
    }

    /**
     *  Description of the Method
     *
     * @param  x       Description of the Parameter
     * @param  bitlen  Description of the Parameter
     * @return         Description of the Return Value
     */
    private int BitRevX(int x, int bitlen) {
        int temp = 0;
        for (int i = 0; i <= bitlen; i++) {
            if ((x & (1 << i)) != 0) {
                temp |= (1 << (bitlen - i - 1));
            }
        }
        return temp & 0x0000ffff;
    }

    /**
     *  Description of the Method
     *
     * @param  x    Description of the Parameter
     * @param  bit  Description of the Parameter
     * @return      Description of the Return Value
     */
    private int bset(int x, int bit) {
        x |= (1 << bit);
        return x;
    }

    /**
     *  Description of the Method
     *
     * @return    Description of the Return Value
     */
    public FHTImage3D RFilter() {
        return RFilter(1, false);
    }

    /**
     *  Filtre R pour tomographie sur FFT centree
     *
     * @param  coeff   Description of the Parameter
     * @param  centre  Description of the Parameter
     * @return         Description of the Return Value
     */
    public FHTImage3D RFilter(float coeff, boolean centre) {
        // FFT centree
        //IJ.write("RFilter dans FFTImage3D");
        float dist;
        int cx = sizex / 2;
        int cy = sizey / 2;
        int cz = sizez / 2;
        int xx;
        int yy;
        int zz;
        //int X;
        //int Y;
        //int Z;
        int sizexy = sizex * sizey;

        for (int z = 0; z < sizez; z++) {
            //Z = z * sizexy;
            if (centre) {
                zz = (z - cz);
            } else {
                zz = (z < cz) ? z : z - sizez;
            }
            zz *= zz;
            for (int y = 0; y < sizey; y++) {
                //Y = y * sizex + Z;
                if (centre) {
                    yy = y - cy;
                } else {
                    yy = (y < cy) ? y : y - sizey;
                }
                yy *= yy;
                for (int x = 0; x < sizex; x++) {
                    if (centre) {
                        xx = x - cx;
                    } else {
                        xx = (x < cx) ? x : x - sizex;
                    }
                    xx *= xx;
                    dist = (float) Math.sqrt(xx + yy + zz);
                    putPixel(x, y, z, getPixel(x, y, z) * coeff * dist);
                    //this.data[x + Y] *= coeff * dist;
                    //this.data[x][y][z] = this.makeComplexNumber(coeff * dist * getPixelReal(x, y, z), coeff * dist * getPixelImag(x, y, z));
                }
            }
        }
        return this;
    }

    /**
     *  Band Pass Filter
     *
     * @param  rayonmincut   Description of the Parameter
     * @param  rayonminkeep  Description of the Parameter
     * @param  rayonmaxkeep  Description of the Parameter
     * @param  rayonmaxcut   Description of the Parameter
     */
    public void bandPassFilter(float rayonmincut , float rayonminkeep, float rayonmaxkeep, float rayonmaxcut) {
        int cenx = sizex / 2;
        int ceny = sizey / 2;
        int cenz = sizez / 2;
        // float auxmin = (float) Math.PI / (rayonminkeep - rayonmincut);
        // float auxmax = (float) Math.PI / (rayonmaxcut - rayonmaxkeep);
        float auxmin = (float) (0.5 * Math.PI) / (rayonminkeep - rayonmincut);
        float auxmax = (float) (0.5 * Math.PI) / (rayonmaxcut - rayonmaxkeep);

        IJ.log("BandPass3D radii= "+rayonmincut+" "+rayonminkeep+" "+rayonmaxkeep+" "+rayonmaxcut);

        for (int k = 0; k < sizez; k++) {
            int ck = k - cenz;
            if (!centered) {
                ck = (k > cenz) ? k - sizez : k;
            }
            //kk = k * sizexy;
            for (int j = 0; j < sizey; j++) {
                int cj = j - ceny;
                if (!centered) {
                    cj = (j > ceny) ? j - sizey : j;
                }
                //jj = j * sizex + kk;
                for (int i = 0; i < sizex; i++) {
                    int ci = i - cenx;
                    if (!centered) {
                        ci = (i > cenx) ? i - sizex : i;
                    }
                    double dist = Math.sqrt(ci * ci + cj * cj + ck * ck);
                    if ((dist >= rayonmincut) && (dist < rayonminkeep)) {
                        // float tmp = (float) (1 + Math.cos((dist - rayonminkeep) * auxmin)) * .5f;
                        float tmp = (float) Math.cos((rayonminkeep - dist) * auxmin);
                        putPixel(i, j, k, getPixel(i, j, k) * tmp);
                    }
                    if ((dist > rayonmaxkeep) && (dist <= rayonmaxcut)) {
                        // float tmp = (float) (1 + Math.cos((dist - rayonmaxkeep) * auxmax)) * .5f;
                        float tmp = (float) Math.cos((rayonmaxcut - dist) * auxmax);
                        //tmp = 0;
                        putPixel(i, j, k, getPixel(i, j, k) * tmp);
                    }
                    if ((dist < rayonmincut) || (dist > rayonmaxcut)) {
                        if ((ci != 0) || (cj != 0) || (ck != 0)) {
                            putPixel(i, j, k, 0);
                        }
                    }
                }
            }
        }
        // return this;
    }

    /**
     *  Description of the Method
     *
     * @param  x  Description of the Parameter
     * @param  y  Description of the Parameter
     * @param  z  Description of the Parameter
     */
    public void zeroValue(int x, int y, int z) {
        //data[sizex * sizey * z + sizex * y + x] = 0;
        putPixel(x, y, z, 0);
        int xx = (sizex - x) % sizex;
        int yy = (sizey - y) % sizey;
        int zz = (sizez - z) % sizez;
        //data[sizex * sizey * zz + sizex * yy + xx] = 0;
        putPixel(xx, yy, zz, 0);
    }

    /**
     *  Fill 3D Reconstruction using central section theorem
     *
     * @param  proj  Array of FFT of the projections
     * @param  W     Orientations of the images
     */
    public void fill3D(FHTImage3D[] proj, Vector3D[] W) {
        if (!frequencyDomain) {
            return;
        }

        Vector3D Y = new Vector3D(0, 1, 0);
        int sizexy = sizex * sizey;
        double ox = sizex / 2;
        double oy = sizey / 2;
        double oz = sizez / 2;
        double x0;
        double y0;
        double z0;
        double xc0;
        double yc0;
        double zc0;
        int xx0;
        int yy0;
        int nbproj = proj.length;
        double a11[] = new double[nbproj];
        double a12[] = new double[nbproj];
        double a21[] = new double[nbproj];
        double a22[] = new double[nbproj];
        double a31[] = new double[nbproj];
        double a32[] = new double[nbproj];
        double at11[] = new double[nbproj];
        double at12[] = new double[nbproj];
        double at13[] = new double[nbproj];
        double at21[] = new double[nbproj];
        double at22[] = new double[nbproj];
        double at23[] = new double[nbproj];

        Vector3D U;

        Vector3D V;

        double RP0;
        double XX0;
        double XX1;
        double XX2;
        double RP1;
        for (int p = 0; p
                < nbproj; p++) {
            U = Y.crossProduct(W[p]);
            V =
                    W[p].crossProduct(U);
            U.normalize();
            V.normalize();
            double ux = U.getX();
            double uy = U.getY();
            double uz = U.getZ();
            double vx = V.getX();
            double vy = V.getY();
            double vz = V.getZ();
            double wx = W[p].getX();
            double wy = W[p].getY();
            double wz = W[p].getZ();

            // A = new Matrix(3, 3);
            // A.set(0, 0, ux);
            // A.set(0, 1, vx);
            // A.set(0, 2, wx);
            // A.set(1, 0, uy);
            // A.set(1, 1, vy);
            // A.set(1, 2, wy);
            // A.set(2, 0, uz);
            // A.set(2, 1, vz);
            // A.set(2, 2, wz);
            a11[p] = ux;
            a12[p] = vx;
            a21[p] = uy;
            a22[p] = vy;
            a31[p] = uz;
            a32[p] = vz;
            // AT = A.inverse();
            // at11[p] = AT.get(0, 0);
            // at12[p] = AT.get(0, 1);
            // at13[p] = AT.get(0, 2);
            // at21[p] = AT.get(1, 0);
            // at22[p] = AT.get(1, 1);
            // at23[p] = AT.get(1, 2);

            at11[p] = ux;
            at12[p] = uy;
            at13[p] = uz;
            at21[p] = vx;
            at22[p] = vy;
            at23[p] = vz;

        }

        double dist;
        double distmin;
        int projmin;
        Chrono time = new Chrono(sizex);
        time.start();
        for (int x = 0; x
                < sizex; x++) {
            for (int y = 0; y
                    < sizey; y++) {
                for (int z = 0; z
                        < sizez; z++) {
                    x0 = x - ox;
                    y0 =
                            y - oy;
                    z0 =
                            z - oz;
                    distmin =
                            1.0;
                    projmin =
                            -1;

                    for (int p = 0; p
                            < nbproj; p++) {
                        RP0 = at11[p] * x0 + at12[p] * y0 + at13[p] * z0;
                        RP1 =
                                at21[p] * x0 + at22[p] * y0 + at23[p] * z0;
                        xx0 =
                                (int) Math.round(RP0);
                        yy0 =
                                (int) Math.round(RP1);
                        XX0 =
                                a11[p] * xx0 + a12[p] * yy0;
                        XX1 =
                                a21[p] * xx0 + a22[p] * yy0;
                        XX2 =
                                a31[p] * xx0 + a32[p] * yy0;
                        xc0 =
                                XX0 + ox;
                        yc0 =
                                XX1 + oy;
                        zc0 =
                                XX2 + oz;
                        dist =
                                (x - xc0) * (x - xc0) + (y - yc0) * (y - yc0) + (z - zc0) * (z - zc0);
                        if (dist <= distmin) {
                            distmin = dist;
                            projmin =
                                    p;
                        }

                    }
                    //index = 0;
                    if (projmin != -1) {
                        for (int k = -1; k
                                <= 1; k++) {
                            for (int l = -1; l
                                    <= 1; l++) {
                                RP0 = at11[projmin] * x0 + at12[projmin] * y0 + at13[projmin] * z0;
                                RP1 =
                                        at21[projmin] * x0 + at22[projmin] * y0 + at23[projmin] * z0;
                                xx0 =
                                        (int) Math.round(RP0 + k);
                                yy0 =
                                        (int) Math.round(RP1 + l);
                                XX0 =
                                        a11[projmin] * xx0 + a12[projmin] * yy0;
                                XX1 =
                                        a21[projmin] * xx0 + a22[projmin] * yy0;
                                XX2 =
                                        a31[projmin] * xx0 + a32[projmin] * yy0;
                                xc0 =
                                        XX0 + ox;
                                yc0 =
                                        XX1 + oy;
                                zc0 =
                                        XX2 + oz;
                                //ll[index] = W[projmin].intersection_unit_cube(x, y, z, xc0, yc0, zc0);
                                //real = proj[projmin].getPixelReal((int) (xx0 + ox), (int) (yy0 + oy), 0);
                                //imag = proj[projmin].getPixelImag((int) (xx0 + ox), (int) (yy0 + oy), 0);
                                //this.putPixel(x, y, z, getPixelReal(x, y, z) + (float) real, getPixelImag(x, y, z) + (float) imag);
                                xx0 +=
                                        ox;
                                yy0 +=
                                        oy;
                                // if (xx0 < 0) {
                                // xx0 = 0;
                                // }
                                // if (xx0 >= sizex) {
                                // xx0 = sizex - 1;
                                // }
                                // if (yy0 < 0) {
                                // yy0 = 0;
                                // }
                                // if (yy0 >= sizey) {
                                // yy0 = sizey - 1;
                                // }
                                if (xx0 > 0 && xx0 < sizex && yy0 > 0 && yy0 < sizey) {
                                    int fhtcoord = (int) (xx0 + yy0 * sizex);
                                    putPixel(x, y, z, getPixel(x, y, z) + proj[projmin].getPixel(xx0, yy0, 0));
                                }
//index++;

                            }
                        }
                    }
                }
            }
            time.stop();
            System.out.print("\r                                                                 \r" + (100 * (x + 1) / sizex) + "% \t" + time.delayString() + "\t (" + time.remainString(x + 1) + ")           ");

        }
        /*
         *  /////////////////////////////////////////////////////
         *  int nbproj = proj.length;
         *  Matrix[] M = new Matrix[nbproj];
         *  Matrix MM;
         *  Vector3D U;
         *  Vector3D V;
         *  /Vector3D X = new Vector3D(0, 0, 1);
         *  Vector3D Y = new Vector3D(0, 1, 0);
         *  Matrix B;
         *  Matrix res;
         *  double[] b = new double[6];
         *  double dist;
         *  int projmin;
         *  double amin;
         *  double bmin;
         *  double distmin;
         *  float real;
         *  float imag;
         *  float coeff;
         *  double alpha;
         *  double beta;
         *  double ux[] = new double[nbproj];
         *  double uy[] = new double[nbproj];
         *  double uz[] = new double[nbproj];
         *  double vx[] = new double[nbproj];
         *  double vy[] = new double[nbproj];
         *  double vz[] = new double[nbproj];
         *  double ox = 0;
         *  double oy = 0;
         *  double oz = 0;
         *  double wx[] = new double[nbproj];
         *  double wy[] = new double[nbproj];
         *  double wz[] = new double[nbproj];
         *  double ww[] = new double[nbproj];
         *  for (int p = 0; p < nbproj; p++) {
         *  U = Y.crossProduct(W[p]);
         *  V = U.crossProduct(W[p]);
         *  U.normalize();
         *  V.normalize();
         *  ux[p] = U.getX();
         *  uy[p] = U.getY();
         *  uz[p] = U.getZ();
         *  vx[p] = V.getX();
         *  vy[p] = V.getY();
         *  vz[p] = V.getZ();
         *  ox = getSizex() / 2;
         *  oy = getSizey() / 2;
         *  oz = getSizez() / 2;
         *  wx[p] = W[p].getX();
         *  wy[p] = W[p].getY();
         *  wz[p] = W[p].getZ();
         *  ww[p] = wx[p] * wx[p] + wy[p] * wy[p] + wz[p] * wz[p];
         *  }
         *  int sx = getSizex();
         *  int sy = getSizey();
         *  int sz = getSizez();
         *  double px;
         *  double py;
         *  double pz;
         *  double lambda;
         *  for (int x = 0; x < sx; x++) {
         *  for (int y = 0; y < sy; y++) {
         *  for (int z = 0; z < sz; z++) {
         *  / parcours des projections
         *  distmin = 1.0;
         *  projmin = -1;
         *  coeff = 0;
         *  amin = 0;
         *  bmin = 0;
         *  real = 0;
         *  imag = 0;
         *  double xxmin = 0;
         *  double yymin = 0;
         *  for (int p = 0; p < nbproj; p++) {
         *  / resolution directe
         *  lambda = (wx[p] * (ox - x) + wy[p] * (oy - y) + wz[p] * (oz - z)) / ww[p];
         *  px = x + lambda * wx[p];
         *  py = y + lambda * wy[p];
         *  pz = z + lambda * wz[p];
         *  alpha = (px - ox) * ux[p] + (py - oy) * uy[p] + (pz - oz) * uz[p];
         *  beta = (px - ox) * vx[p] + (py - oy) * vy[p] + (pz - oz) * vz[p];
         *  double xx = (alpha + sx / 2);
         *  double yy = (sy / 2 - beta);
         *  dist = Math.abs(lambda);
         *  if (dist < distmin) {
         *  distmin = dist;
         *  projmin = p;
         *  xxmin = xx;
         *  yymin = yy;
         *  }
         *  }
         *  if (projmin != -1) {
         *  real = proj[projmin].getInterpolatedPixelReal((float) xxmin, (float) yymin, 0);
         *  imag = proj[projmin].getInterpolatedPixelImag((float) xxmin, (float) yymin, 0);
         *  this.putPixel(x, y, z, real, imag);
         *  }
         *  }
         *  }
         *  }
         */
    }
}
