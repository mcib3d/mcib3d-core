package mcib3d.image3d.legacy;

import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import java.util.concurrent.atomic.AtomicInteger;
import mcib3d.Jama.EigenvalueDecomposition;
import mcib3d.Jama.Matrix;
import mcib3d.geom.Object3DVoxels;
import mcib3d.geom.Point3D;
import mcib3d.geom.Vector3D;
import mcib3d.geom.Voxel3D;
import mcib3d.image3d.ImageByte;
import mcib3d.image3d.ImageInt;
import mcib3d.image3d.ImageShort;
import mcib3d.image3d.Segment3DImage;
import mcib3d.image3d.processing.FastFilters3D;
import mcib3d.utils.ArrayUtil;
import mcib3d.utils.HistogramUtil;
import mcib3d.utils.ThreadUtil;

/**
 * Copyright (C) Thomas Boudier mcib3d
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
 * 3D images with integer values (8 or 16 bits)
 *
 * @author Thomas BOUDIER & Cedric MESSAOUDI @created 28 avril 2003 last update
 * 6 mai 2003
 */
public class IntImage3D extends Image3D {

    // size of a 2D slice
    private int sizexy;
    //short[][] pixels;
    // virtual
    //private String directory;
    //private String[] files;
    //private int current = -1;
    // private ImageProcessor processor;
    //private boolean virtual = false;
    private boolean debug = false;

    /**
     * constructor (black image)
     *
     * @param sizex size x of 3D image
     * @param sizey size y of 3D image
     * @param sizez size z of 3D image
     */
    public IntImage3D(int sizex, int sizey, int sizez) {
        super(sizex, sizey, sizez, Image3D.SHORT);
        sizexy = sizex * sizey;
        for (int i = 0; i < sizez; i++) {
            IJstack.addSlice("", new short[sizexy]);
        }
    }

    /**
     * Constructor for the IntImage3D object
     *
     * @param sizex size x of 3D image
     * @param sizey size x of 3D image
     * @param sizez size x of 3D image
     * @param ty Data type
     */
    public IntImage3D(int sizex, int sizey, int sizez, int ty) {
        super(sizex, sizey, sizez, ty);
        sizexy = sizex * sizey;
        for (int i = 0; i < sizez; i++) {
            if (type == SHORT) {
                IJstack.addSlice("", new short[sizexy]);
            } else {
                IJstack.addSlice("", new byte[sizexy]);
            }
        }

    }

    /**
     * Constructor for the IntImage3D object, virtual
     *
     * @param sx Size x
     * @param sy Size y
     * @param d Directory with slices
     * @param f Files corresponding to slices
     */
    /*
     * public IntImage3D(int sx, int sy, String d, String f[]) { super(sx, sy,
     * f.length, Image3D.SHORT); if (debug) { IJ.log("virtual stack"); }
     * initVirtual(d, f); }
     */
    /**
     * Image 3D virtual
     *
     * @param d Directory
     * @param f files
     */
    /*
     * private void initVirtual(String d, String f[]) { directory = d; files =
     * f; //virtual = true; current = 0; //ImagePlus plus = new
     * Opener().openImage(directory, files[current]);
     *
     * //processor = plus.getProcessor(); }
     */
    /**
     * constructor from a ImageStack
     *
     * @param stack IJstack
     */
    public IntImage3D(ImageStack stack) {
        super(stack);
        /*
         * if (stack.isVirtual()) { String f[] = new String[stack.getSize()];
         * for (int i = 0; i < f.length; i++) { f[i] = ((VirtualStack)
         * stack).getFileName(i + 1); } initVirtual(((VirtualStack)
         * stack).getDirectory(), f); }
         */
    }

    public IntImage3D(ImagePlus plus) {
        super(plus.getStack());
    }

    /**
     * @param img2copy the original 3D image to copy
     */
    public IntImage3D(IntImage3D img2copy) {
        super(img2copy.sizex, img2copy.sizey, img2copy.sizez, img2copy.type);

        ImageStack stackcopy = img2copy.getStack();
        sizexy = sizex * sizey;
        for (int i = 1; i <= sizez; i++) {
            IJstack.addSlice(stackcopy.getSliceLabel(i), stackcopy.getProcessor(i).duplicate());

        }
    }

    /**
     *
     * @param img2copy
     */
    public IntImage3D(RealImage3D img2copy) {
        super(img2copy.sizex, img2copy.sizey, img2copy.sizez, Image3D.SHORT);

        sizexy = sizex * sizey;
        for (int i = 0; i < sizez; i++) {
            IJstack.addSlice("", new short[sizexy]);
        }

        for (int x = 0; x < this.getSizex(); x++) {
            for (int y = 0; y < this.getSizey(); y++) {
                for (int z = 0; z < this.getSizez(); z++) {
                    this.putPixel(x, y, z, (int) img2copy.getPixel(x, y, z));
                }
            }
        }
    }

    public IntImage3D createImageSameSize3D() {
        IntImage3D out = new IntImage3D(this.getSizex(), this.getSizey(), this.getSizez(), this.getType());

        return out;
    }

    /**
     * Gets the ImageStack associated with the 3D image
     *
     * @return The IJstack
     */
    @Override
    public ImageStack getStack() {
        return IJstack;
    }

    /**
     * 3D median filter
     *
     * @param radx Radius of neighborhood in x
     * @param rady Radius of neighborhood in y
     * @param radz Radius of neighborhood in z
     * @return 3D filtered image
     */
    public IntImage3D medianFilter(float radx, float rady, float radz) {
        ImageStack filt = FastFilters3D.filterIntImageStack(this.getStack(), FastFilters3D.MEDIAN, radx, rady, radz, ThreadUtil.getNbCpus(), debug);

        return new IntImage3D(filt);
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
    public void filterGeneric(IntImage3D out, float radx, float rady, float radz, int zmin, int zmax, int filter) {
        int[] ker = createKernelEllipsoid(radx, rady, radz);
        int nb = 0;
        for (int i = 0; i < ker.length; i++) {
            nb += ker[i];
        }
        if (zmin < 0) {
            zmin = 0;
        }
        if (zmax > this.getSizez()) {
            zmax = this.getSizez();
        }
        int value;
        for (int k = zmin; k < zmax; k++) {
            if (this.showStatus) {
                IJ.showStatus("3D filter : " + k + "/" + zmax);
            }
            for (int j = 0; j < sizey; j++) {
                for (int i = 0; i < sizex; i++) {
                    ArrayUtil tab = this.getNeighborhoodKernel(ker, nb, i, j, k, radx, rady, radz);
                    if (filter == FastFilters3D.MEAN) {
                        out.putPixel(i, j, k, tab.getMean());
                    } else if (filter == FastFilters3D.MEDIAN) {
                        out.putPixel(i, j, k, tab.medianSort());
                    }
                    if (filter == FastFilters3D.MIN) {
                        out.putPixel(i, j, k, tab.getMinimum());
                    }
                    if (filter == FastFilters3D.MAX) {
                        out.putPixel(i, j, k, tab.getMaximum());
                    }
                    if (filter == FastFilters3D.VARIANCE) {
                        out.putPixel(i, j, k, tab.getVariance2());
                    }
                    if (filter == FastFilters3D.MAXLOCAL) {
                        value = this.getPixel(i, j, k);
                        if (tab.isMaximum(value)) {
                            out.putPixel(i, j, k, value);
                        } else {
                            out.putPixel(i, j, k, 0);
                        }
                    }
                }
            }
        }
    }

    /**
     * Variance Filter in 3D
     *
     * @param radx radius in x
     * @param rady radius in y
     * @param radz radius in z
     * @return 3D variance image
     */
    public RealImage3D varianceFilter(float radx, float rady, float radz) {
        RealImage3D varimg = null;
        varimg = new RealImage3D(sizex, sizey, sizez);
        final RealImage3D varimg2 = varimg;

        // create kernel
        final int[] ker = createKernelEllipsoid(radx, rady, radz);
        int nb = 0;
        for (int i = 0; i < ker.length; i++) {
            nb += ker[i];
        }
        final int nb2 = nb;
        final float radx2 = radx;
        final float rady2 = rady;
        final float radz2 = radz;

        // PARALLEL 
        final AtomicInteger ai = new AtomicInteger(0);
        Thread[] threads = ThreadUtil.createThreadArray(0);

        for (int ithread = 0; ithread < threads.length; ithread++) {
            threads[ithread] = new Thread() {

                @Override
                public void run() {
                    // median filter
                    ArrayUtil tab;
                    for (int k = ai.getAndIncrement(); k < sizez; k = ai.getAndIncrement()) {
                        if (showStatus) {
                            IJ.showStatus("3D Variance : " + (int) (100 * k / sizez) + "%");
                        }
                        for (int j = 0; j < sizey; j++) {
                            for (int i = 0; i < sizex; i++) {
                                tab = getNeighborhoodKernel(ker, nb2, i, j, k, radx2, rady2, radz2);
                                varimg2.putPixel(i, j, k, (int) tab.getVariance2());
                            }
                        }
                    }
                }
            };
        }

        ThreadUtil.startAndJoin(threads);

        return varimg2;
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
    public IntImage3D adaptiveFilter(float radx, float rady, float radz, int nbcpus) {
        IntImage3D adaptimg = new IntImage3D(sizex, sizey, sizez, type);
        final IntImage3D adaptimg2 = adaptimg;

        // create kernel
        final int[] ker = createKernelEllipsoid(radx, rady, radz);
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
                    double me = 0;

                    for (int k = ai.getAndIncrement(); k < sizez; k = ai.getAndIncrement()) {
                        if (showStatus) {
                            IJ.showStatus("3D Adaptive : " + (int) (100 * k / sizez) + "%");
                        }
                        for (int j = 0; j < sizey; j++) {
                            for (int i = 0; i < sizex; i++) {
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
                                adaptimg2.putPixel(i, j, k, (int) mes);
                            }
                        }
                    }
                }
            };
        }

        ThreadUtil.startAndJoin(threads);

        return adaptimg2;
    }

    /**
     * 3D maximum filter
     *
     * @param radx Radius of mean filter in x
     * @param rady Radius of mean filter in y
     * @param radz Radius of mean filter in z
     * @return Filtered 3D image
     */
    public IntImage3D maximumFilter(float radx, float rady, float radz) {
        ImageStack filt = FastFilters3D.filterIntImageStack(this.getStack(), FastFilters3D.MAX, radx, rady, radz, ThreadUtil.getNbCpus(), debug);

        return new IntImage3D(filt);
    }

    /**
     * Gets the pixelMoments attribute of the IntImage3D object
     *
     * @param x x coordinate of pixel
     * @param y y coordinate of pixel
     * @param z z coordinate of pixel
     * @param radXY Description of the Parameter
     * @param radZ Description of the Parameter
     * @return The pixelMoments value
     */
    public double[] getPixelMoments(int x, int y, int z, int radXY, int radZ) {

        double[] tab = new double[9];
        int pix;
        int sxx = 0;
        int sxy = 0;
        int sxz = 0;
        int syy = 0;
        int syz = 0;
        int szz = 0;

        double sum = 0;
        double ic = 0;
        double jc = 0;
        double kc = 0;

        for (int k = -radZ; k <= radZ; k++) {
            for (int j = -radXY; j <= radXY; j++) {
                for (int i = -radXY; i <= radXY; i++) {
                    pix = getPixel(x + i, y + j, z + k);
                    sum += pix;
                    ic += i * pix;
                    jc += j * pix;
                    kc += k * pix;
                }
            }
        }
        if (sum != 0) {
            ic /= sum;
            jc /= sum;
            kc /= sum;
        }

        for (int k = -radZ; k <= radZ; k++) {
            for (int j = -radXY; j <= radXY; j++) {
                for (int i = -radXY; i <= radXY; i++) {
                    pix = getPixel(x + i, y + j, z + k);
                    sxx += (i - ic) * (i - ic) * pix;
                    sxy += (i - ic) * (j - jc) * pix;
                    sxz += (i - ic) * (k - kc) * pix;
                    syy += (j - jc) * (j - jc) * pix;
                    syz += (j - jc) * (k - kc) * pix;
                    szz += (k - kc) * (k - kc) * pix;
                }
            }
        }
        if (sum != 0) {
            tab[0] = sxx / sum;
            tab[1] = sxy / sum;
            tab[2] = sxz / sum;
            tab[3] = sxy / sum;
            tab[4] = syy / sum;
            tab[5] = syz / sum;
            tab[6] = sxz / sum;
            tab[7] = syz / sum;
            tab[8] = szz / sum;
        } else {
            tab[0] = sxx / sum;
            tab[1] = 0;
            tab[2] = 0;
            tab[3] = 0;
            tab[4] = 0;
            tab[5] = 0;
            tab[6] = 0;
            tab[7] = 0;
            tab[8] = 0;
        }

        return tab;
    }

    /**
     * 3D minimum filter
     *
     * @param radx Radius of mean filter in x
     * @param rady Radius of mean filter in y
     * @param radz Radius of mean filter in z
     * @return Filtered 3D image
     */
    public IntImage3D minimumFilter(float radx, float rady, float radz) {
        ImageStack filt = FastFilters3D.filterIntImageStack(this.getStack(), FastFilters3D.MIN, radx, rady, radz, ThreadUtil.getNbCpus(), debug);

        return new IntImage3D(filt);
    }

    /**
     * TopHat Filter to remove local background Enhance spots
     *
     * @param radx rayon du voisinage en x
     * @param rady rayon du voisinage en y
     * @param radz rayon du voisinage en z
     * @return Image Filtrée
     */
    public IntImage3D tophatFilter(int radx, int rady, int radz) {
        IntImage3D minImage = (IntImage3D) minimumFilter(radx, rady, radz);
        IntImage3D maxmin = (IntImage3D) minImage.maximumFilter(radx, rady, radz);
        IntImage3D tophat = (IntImage3D) addImage(maxmin, -1.0f);

        return tophat;
    }

    /**
     * 3D non linear laplacien
     *
     * @param radx Radius of mean filter in x
     * @param rady Radius of mean filter in y
     * @param radz Radius of mean filter in z
     * @return Filtered 3D image
     */
    public IntImage3D nlLaplacien(int radx, int rady, int radz) {
        IntImage3D minImage = (IntImage3D) minimumFilter(radx, rady, radz);
        IntImage3D maxImage = (IntImage3D) maximumFilter(radx, rady, radz);
        IntImage3D res = new IntImage3D(sizex, sizey, sizez, type);

        for (int k = 0; k < sizez; k++) {
            for (int j = 0; j < sizey; j++) {
                for (int i = 0; i < sizex; i++) {
                    res.putPixel(i, j, k,
                            128 + maxImage.getPixel(i, j, k) + minImage.getPixel(i, j, k) - 2 * getPixel(i, j, k));

                }
            }
        }

        return res;
    }

    /**
     * 3D non linear laplacian (version 2)
     *
     * @param radx rayon du voisinage en x
     * @param rady rayon du voisinage en y
     * @param radz rayon du voisinage en z
     * @return Image Filtrée
     */
    public IntImage3D nonLinearLaplacianFilter(int radx, int rady, int radz) {
        IntImage3D minImage = (IntImage3D) minimumFilter(radx, rady, radz);
        IntImage3D maxImage = (IntImage3D) maximumFilter(radx, rady, radz);
        IntImage3D nll = (IntImage3D) minImage.addImage(maxImage, +1.0f);
        nll = (IntImage3D) nll.addImage(this, -2.0f);

        return nll;
    }

    /**
     * 3D dilatation
     *
     * @param radx voisinage en x
     * @param rady voisinage en y
     * @param radz voisinage en z
     * @param black black objects (0)
     * @return dilated image
     */
    public IntImage3D dilatation3D(float radx, float rady, float radz, boolean black) {
        if (black) {
            return this.minimumFilter(radx, rady, radz);
        } else {
            return this.maximumFilter(radx, rady, radz);
        }
    }

    /**
     * 3D dilatation with kernel
     *
     * @param ker The kernel image
     * @param black black objects (0)
     * @return dilated image
     */
    public IntImage3D dilatation3D(Image3D ker, boolean black) {
        if (black) {
            return morpho3Dbin(getMinimum(), getMaximum(), ker);
        } else {
            return morpho3Dbin(getMaximum(), getMinimum(), ker);
        }
    }

    /**
     * Erosion binaire 3D, normalement les objets sont noir sur fond blanc
     *
     * @param voisx voisinage en x
     * @param voisy voisinage en y
     * @param voisz voisinage en z
     * @param normal Vrai si obj noir sur blanc, faux sinon
     * @return image modifiee
     */
    public IntImage3D erosion3D(float voisx, float voisy, float voisz, boolean normal) {
        if (normal) {
            return this.maximumFilter(voisx, voisy, voisz);
        } else {
             return this.minimumFilter(voisx, voisy, voisz);
        }
    }

    /**
     * Erosion binaire 3D avec kernel, normalement les objets sont noir sur fond
     * blanc
     *
     * @param normal Vrai si obj noir sur blanc, faux sinon
     * @param ker Le kernel (>0 inside kernel, 0 outside kernel)
     * @return image modifiee
     */
    public IntImage3D erosion3D(Image3D ker, boolean normal) {
        if (normal) {
            return morpho3Dbin(getMaximum(), getMinimum(), ker);
        } else {
            return morpho3Dbin(getMinimum(), getMaximum(), ker);
        }
    }

    /**
     * Erosion binaire 3D, normalement les objets sont noir sur fond blanc
     *
     * @param voisx voisinage en x
     * @param voisy voisinage en y
     * @param voisz voisinage en z
     * @return image modifiee
     */
    public IntImage3D erosion3D(int voisx, int voisy, int voisz) {
        return erosion3D(voisx, voisy, voisz, true);
    }

    /**
     * Erosion binaire 3D avec kernel, normalement les objets sont noir sur fond
     * blanc
     *
     * @param ker Le kernel (>0 inside kernel, 0 outside kernel)
     * @return image modifiee
     */
    public IntImage3D erosion3D(Image3D ker) {
        return erosion3D(ker, true);
    }

    /**
     * 3D binary morphology - dilatation of objects
     *
     * @param fond Value of background
     * @param obj Value of objects
     * @param rx Radius in x
     * @param ry Radius in x
     * @param rz Radius in x
     * @return Dilated image
     */
    private IntImage3D morpho3Dbin(int obj, int fond, float rx, float ry, float rz) {
        int p;
        IntImage3D morphoimg = new IntImage3D(sizex, sizey, sizez, type);

        // create kernel
        int[] ker = createKernelEllipsoid(rx, ry, rz);
        int nb = 0;
        for (int i = 0; i < ker.length; i++) {
            nb += ker[i];
        }
        ArrayUtil tab;
        for (int k = 0; k < sizez; k++) {
            if (this.showStatus) {
                IJ.showStatus("3D Binary Morphology : " + (int) (100 * k / sizez) + "%");
            }
            for (int j = 0; j < sizey; j++) {
                for (int i = 0; i < sizex; i++) {
                    p = getPixel(i, j, k);
                    if (p == obj) {
                        morphoimg.putPixel(i, j, k, p);
                    } else {
                        tab = this.getNeighborhoodKernel(ker, nb, i, j, k, rx, ry, rz);
                        if (!tab.hasOnlyValue(fond)) {
                            morphoimg.putPixel(i, j, k, obj);
                        } else {
                            morphoimg.putPixel(i, j, k, fond);
                        }
                    }
                }
            }
        }
        return morphoimg;
    }

    /**
     * Morphologie binaire 3D avec kernel
     *
     * @param fond Valeur du fond
     * @param obj Valeur des objets
     * @param ker Noyau (>0 inside kernel, 0 outside kernel)
     * @return Image transformee
     */
    private IntImage3D morpho3Dbin(int obj, int fond, Image3D ker) {
        int p;
        IntImage3D morphoimg = new IntImage3D(sizex, sizey, sizez, type);
        ArrayUtil tab;
        for (int k = 0; k < sizez; k++) {
            for (int j = 0; j < sizey; j++) {
                for (int i = 0; i < sizex; i++) {
                    p = getPixel(i, j, k);
                    if (p == obj) {
                        morphoimg.putPixel(i, j, k, p);
                    } else {
                        tab = this.getNeighborhood(ker, i, j, k);

                        if (!tab.hasOnlyValue(fond)) {
                            morphoimg.putPixel(i, j, k, obj);

                        } else {
                            morphoimg.putPixel(i, j, k, fond);
                        }
                    }
                }
            }
        }
        return morphoimg;
    }

    public IntImage3D hitOrMiss(int obj, int fond, Image3D ker) {
        IntImage3D hitormiss = new IntImage3D(sizex, sizey, sizez, type);
        for (int k = 0; k < sizez; k++) {
            IJ.showStatus("Hit or Miss " + k + "/" + sizez);
            for (int j = 0; j < sizey; j++) {
                for (int i = 0; i < sizex; i++) {
                    if (hitOrMiss(obj, fond, ker, i, j, k)) {
                        hitormiss.setPix(i, j, k, 255);
                    } else {
                        hitormiss.setPix(i, j, k, 0);
                    }
                }
            }
        }

        return hitormiss;
    }

    private boolean hitOrMiss(int obj, int fond, Image3D ker, int x, int y, int z) {
        int rx = (ker.sizex - 1) / 2;
        int ry = (ker.sizex - 1) / 2;
        int rz = (ker.sizex - 1) / 2;
        for (int k = 0; k < ker.sizez; k++) {
            for (int j = 0; j < ker.sizey; j++) {
                for (int i = 0; i < ker.sizex; i++) {
                    if ((ker.getPix(i, j, k) == 1) && (this.getPixel(x - rx + i, y - ry + j, z - rz + k) == fond)) {
                        return false;
                    }
                    if ((ker.getPix(i, j, k) == -1) && (this.getPixel(x - rx + i, y - ry + j, z - rz + k) == obj)) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    /**
     *
     * @param ker
     * @return
     */
    public IntImage3D grayDilatation(final IntImage3D ker) {
        final IntImage3D dilate = new IntImage3D(sizex, sizey, sizez, type);
        final IntImage3D ker2 = new IntImage3D(ker);
        final int vx = ker.getSizex() / 2;
        final int vy = ker.getSizey() / 2;
        final int vz = ker.getSizez() / 2;



        // PARALLEL
        final AtomicInteger ai = new AtomicInteger(0);
        Thread[] threads = ThreadUtil.createThreadArray();

        for (int ithread = 0; ithread < threads.length; ithread++) {
            threads[ithread] = new Thread() {

                @Override
                public void run() {
                    int res = 0;
                    int val = 0;
                    int x0;
                    int x1;
                    int y0;
                    int y1;
                    int z0;
                    int z1;


                    for (int z = ai.getAndIncrement(); z < sizez; z = ai.getAndIncrement()) {
                        if (showStatus) {
                            IJ.showStatus("3D Gray Dilatation : " + (int) (100 * z / sizez) + "%");
                        }
                        z0 = z - vz;
                        z1 = z + vz;
                        for (int y = 0; y < sizey; y++) {
                            y0 = y - vy;
                            y1 = y + vy;
                            for (int x = 0; x < sizex; x++) {
                                res = 0;
                                x0 = x - vx;
                                x1 = x + vx;
                                for (int k = z0; k <= z1; k++) {
                                    for (int j = y0; j <= y1; j++) {
                                        for (int i = x0; i <= x1; i++) {
                                            if (i >= 0 && j >= 0 && k >= 0 && i < sizex && j < sizey && k < sizez) {
                                                val = getPixel(i, j, k) + ker.getPixel(i - x0, j - y0, k - z0);
                                                if (val > res) {
                                                    res = val;
                                                    if (val > 1) {
                                                        //IJ.log("x="+x+" y="+y+" z="+z+" i=" + i + " j=" + j + " k=" + k + " val=" + val + " ker=" + ker.getPixel(i - x0, j - y0, k - z0));
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                                dilate.putPixel(x, y, z, res);
                            }
                        }
                    }
                }
            };
        }

        ThreadUtil.startAndJoin(threads);


        return dilate;
    }

    /**
     *
     * @param ker
     * @return
     */
    public IntImage3D grayErosion(final IntImage3D ker) {
        final IntImage3D erode = new IntImage3D(sizex, sizey, sizez, type);
        final IntImage3D ker2 = new IntImage3D(ker);
        final int vx = ker.getSizex();
        final int vy = ker.getSizey();
        final int vz = ker.getSizez();



        // PARALLEL
        final AtomicInteger ai = new AtomicInteger(0);
        Thread[] threads = ThreadUtil.createThreadArray(0);

        for (int ithread = 0; ithread < threads.length; ithread++) {
            threads[ithread] = new Thread() {

                @Override
                public void run() {
                    ArrayUtil tab;
                    int res = 0;
                    int val = 0;
                    int x0;
                    int x1;
                    int y0;
                    int y1;
                    int z0;
                    int z1;


                    for (int z = ai.getAndIncrement(); z < sizez; z = ai.getAndIncrement()) {
                        if (showStatus) {
                            IJ.showStatus("3D Gray Erosion : " + (int) (100 * z / sizez) + "%");
                        }
                        for (int y = 0; y < sizey; y++) {
                            for (int x = 0; x < sizex; x++) {
                                res = Integer.MAX_VALUE;
                                x0 = x - vx;
                                x1 = x + vx;
                                y0 = y - vy;
                                y1 = y + vy;
                                z0 = z - vz;
                                z1 = z + vz;
                                for (int k = z0; k <= z1; k++) {
                                    for (int j = y0; j <= y1; j++) {
                                        for (int i = x0; i <= x1; i++) {
                                            if (i >= 0 && j >= 0 && k >= 0 && i < sizex && j < sizey && k < sizez) {
                                                val = getPixel(i, j, k) - ker.getPixel(i - x0, j - y0, k - z0);
                                                if (val < res) {
                                                    res = val;
                                                }
                                            }
                                        }
                                    }
                                }
                                erode.putPixel(x, y, z, res);
                            }
                        }
                    }
                }
            };
        }

        ThreadUtil.startAndJoin(threads);


        return erode;
    }

    /**
     * Ouverture binaire 3D, normalement les objets sont noir sur fond blanc
     *
     * @param voisx voisinage en x
     * @param voisy voisinage en y
     * @param voisz voisinage en z
     * @param ite Taille de l'ouverture
     * @param normal Vrai si obj noir sur blanc, faux sinon
     * @return image modifiee
     */
    public IntImage3D opening3D(int voisx, int voisy, int voisz, int ite, boolean normal) {

        IntImage3D morphoimg = new IntImage3D(this);
        for (int i = 0; i < ite; i++) {
            morphoimg = (IntImage3D) morphoimg.erosion3D(voisx, voisy, voisz, normal);

        }
        for (int i = 0; i < ite; i++) {
            morphoimg = (IntImage3D) morphoimg.dilatation3D(voisx, voisy, voisz, normal);

        }

        return morphoimg;
    }

    /**
     * Ouverture binaire 3D avec kernel, normalement les objets sont noir sur
     * fond blanc
     *
     * @param normal Vrai si obj noir sur blanc, faux sinon
     * @param ker Noyau (>0 inside kernel, 0 outside kernel)
     * @param ite Description of the Parameter
     * @return image modifiee
     */
    public IntImage3D opening3D(Image3D ker, int ite, boolean normal) {
        IntImage3D morphoimg = new IntImage3D(this);
        for (int i = 0; i < ite; i++) {
            morphoimg = (IntImage3D) morphoimg.erosion3D(ker, normal);

        }
        for (int i = 0; i < ite; i++) {
            morphoimg = (IntImage3D) morphoimg.dilatation3D(ker, normal);

        }

        return morphoimg;
    }

    /**
     * Ouverture binaire 3D, normalement les objets sont noir sur fond blanc
     *
     * @param voisx voisinage en x
     * @param voisy voisinage en y
     * @param voisz voisinage en z
     * @param ite Taille de l'ouverture
     * @return image modifiee
     */
    public IntImage3D opening3D(int voisx, int voisy, int voisz, int ite) {
        return opening3D(voisx, voisy, voisz, ite, true);
    }

    /**
     * Ouverture binaire 3D avec kernel, normalement les objets sont noir sur
     * fond blanc
     *
     * @param ite Taille de l'ouverture
     * @param ker Noyau (>0 inside kernel, 0 outside kernel)
     * @return image modifiee
     */
    public IntImage3D opening3D(Image3D ker, int ite) {
        return opening3D(ker, ite, true);
    }

    /**
     * Fermeture binaire 3D, normalement les objets sont noir sur fond blanc
     *
     * @param voisx voisinage en x
     * @param voisy voisinage en y
     * @param voisz voisinage en z
     * @param ite Taille de la fermeture
     * @param normal Vrai si obj noir sur blanc, faux sinon
     * @return image modifiee
     */
    public IntImage3D closing3D(float voisx, float voisy, float voisz, int ite, boolean normal) {

        IntImage3D morphoimg = new IntImage3D(this);
        for (int i = 0; i < ite; i++) {
            morphoimg = (IntImage3D) morphoimg.dilatation3D(voisx, voisy, voisz, normal);

        }
        for (int i = 0; i < ite; i++) {
            morphoimg = (IntImage3D) morphoimg.erosion3D(voisx, voisy, voisz, normal);

        }
        return morphoimg;
    }

    /**
     * Fermeture binaire 3D avec kernel, normalement les objets sont noir sur
     * fond blanc
     *
     * @param normal Vrai si obj noir sur blanc, faux sinon
     * @param ker Noyau (>0 inside kernel, 0 outside kernel)
     * @param ite Description of the Parameter
     * @return image modifiee
     */
    public IntImage3D closing3D(Image3D ker, int ite, boolean normal) {
        IntImage3D morphoimg = new IntImage3D(this);
        for (int i = 0; i < ite; i++) {
            morphoimg = (IntImage3D) morphoimg.dilatation3D(ker, normal);

        }
        for (int i = 0; i < ite; i++) {
            morphoimg = (IntImage3D) morphoimg.erosion3D(ker, normal);

        }
        return morphoimg;
    }

    /**
     * Fermeture binaire 3D, normalement les objets sont noir sur fond blanc
     *
     * @param voisx voisinage en x
     * @param voisy voisinage en y
     * @param voisz voisinage en z
     * @param ite Taille de l'ouverture
     * @return image modifiee
     */
    public IntImage3D closing3D(int voisx, int voisy, int voisz, int ite) {
        return closing3D(voisx, voisy, voisz, ite, true);
    }

//    /**
//     * Fermeture binaire 3D avec kernel, normalement les objets sont noir sur
//     * fond blanc
//     *
//     * @param ker Noyau (>0 inside kernel, 0 outside kernel)
//     * @param ite Description of the Parameter
//     * @return image modifiee
//     */
//    public IntImage3D fermeture3D(Image3D ker, int ite) {
//        return closing3D(ker, ite, true);
//    }
    /**
     * recupere la valeur du pixel de coordonnees (x,y,z)
     *
     * @param x coordonnee en x
     * @param y coordonnee en yint
     * @param z coordonnee en z
     * @return valeur du pixel
     */
    public int getPixel(int x, int y, int z) {
        // test
        if (x < 0 || x >= sizex || y < 0 || y >= sizey || z < 0 || z >= sizez) {
            return -1;
        } else {
            return (int) IJstack.getVoxel(x, y, z);
        }

        /*
         * {
         * return (int) getMean(); }
         *
         * if (current != z) { current = z; if (virtual) { ImagePlus plus = new
         * Opener().openImage(directory, files[current]); processor =
         * plus.getProcessor(); } else { processor = IJstack.getProcessor(z +
         * 1); } }
         *
         * if (getType() == SHORT) { return ((short[]) processor.getPixels())[x
         * + y * sizex] & 0xffff; } else { return ((byte[])
         * processor.getPixels())[x + y * sizex] & 0xff; } //return
         * (IJstack.getProcessor(z + 1)).getPixel(x, y);
         *
         */
    }

    /**
     * Gets the pixel attribute of the IntImage3D object
     *
     * @param point Description of the Parameter
     * @param interpolate Description of the Parameter
     * @return The pixel value
     */
    public int getPixel(Point3D point, boolean interpolate) {
        if (interpolate) {
            return (getPixel(point.getX(), point.getY(), point.getZ()));
        } else {
            return (getPixel(point.getRoundX(), point.getRoundY(), point.getRoundZ()));
        }
    }

    /**
     * Gets the pixel attribute of the IntImage3D object
     *
     * @param point Description of the Parameter
     * @param interpolate Description of the Parameter
     * @return The pixel value
     */
    public int getPixel(Voxel3D point, boolean interpolate) {
        return getPixel((Point3D) point, interpolate);
    }

    /**
     * Gets the pixel attribute of the IntImage3D object
     *
     * @param vector Description of the Parameter
     * @param interpolate Description of the Parameter
     * @return The pixel value
     */
    public int getPixel(Vector3D vector, boolean interpolate) {
        if (interpolate) {
            return (getPixel(vector.getX(), vector.getY(), vector.getZ()));
        } else {
            return getPixel(vector.getRoundX(), vector.getRoundY(), vector.getRoundZ());
        }
    }

    /**
     * Gets the pixel attribute of the IntImage3D object with tri-linear
     * interpolation
     *
     * @param x x coordinate (float)
     * @param y y coordinate (float)
     * @param z z coordinate (float)
     * @return The interpoalted pixel value
     */
    public int getPixel(float x, float y, float z) {
        if (x < 0.0F || x >= (float) sizex || y < 0.0f || y >= (float) sizey || z < 0.0F || z >= (float) sizez) {
            return (int) getMean();
        }
        int xbase = (int) x;
        int ybase = (int) y;
        int zbase = (int) z;
        float xFraction = x - (float) xbase;
        float yFraction = y - (float) ybase;
        float zFraction = z - (float) zbase;
        float xyz = getPixel(xbase, ybase, zbase);
        float x1yz = getPixel(xbase + 1, ybase, zbase);
        float x1y1z = getPixel(xbase + 1, ybase + 1, zbase);
        float x1y1z1 = getPixel(xbase + 1, ybase + 1, zbase + 1);
        float x1yz1 = getPixel(xbase + 1, ybase, zbase + 1);
        float xy1z = getPixel(xbase, ybase + 1, zbase);
        float xy1z1 = getPixel(xbase, ybase + 1, zbase + 1);
        float xyz1 = getPixel(xbase, ybase, zbase + 1);
        float upperAvplane = xy1z + xFraction * (x1y1z - xy1z);
        float lowerAvplane = xyz + xFraction * (x1yz - xyz);
        float upperAvplane1 = xy1z1 + xFraction * (x1y1z1 - xy1z1);
        float lowerAvplane1 = xyz1 + xFraction * (x1yz1 - xyz1);
        float plane = lowerAvplane + yFraction * (upperAvplane - lowerAvplane);
        float plane1 = lowerAvplane1 + yFraction * (upperAvplane1 - lowerAvplane1);
        return (int) (plane + zFraction * (plane1 - plane));
    }

    /**
     *
     * @param x
     * @param y
     * @param z
     * @return
     */
    public int getPixel(double x, double y, double z) {
        return getPixel((float) x, (float) y, (float) z);
    }

    /**
     * Gets the pix attribute of the IntImage3D object
     *
     * @param x Description of the Parameter
     * @param y Description of the Parameter
     * @param z Description of the Parameter
     * @return The pix value
     */
    public float getPix(int x, int y, int z) {
        return getPixel(x, y, z);
    }

    /**
     * Gets the pix attribute of the IntImage3D object
     *
     * @param x Description of the Parameter
     * @param y Description of the Parameter
     * @param z Description of the Parameter
     * @return The pix value
     */
    public float getPix(float x, float y, float z) {
        return getPixel(x, y, z);
    }

    /**
     *
     * @param vox
     * @param b
     * @return
     */
    @Override
    public float getPix(Voxel3D vox, boolean b) {
        if (b) {
            return getPixel(vox.getX(), vox.getY(), vox.getZ());
        } else {
            return getPixel(vox.getRoundX(), vox.getRoundY(), vox.getRoundZ());
        }
    }

    /**
     * Sets the pix attribute of the IntImage3D object
     *
     * @param x The new pix value
     * @param y The new pix value
     * @param z The new pix value
     * @param value The new pix value
     */
    public void setPix(int x, int y, int z, double value) {
        putPixel(x, y, z, (int) value);
    }

    /**
     * donne une valeur au pixel de coordonnees (x,y,z)
     *
     * @param x coordonnee en x
     * @param y coordonnee en y
     * @param z coordonnee en z
     * @param value valeur du pixel
     */
    public final void putPixel(int x, int y, int z, int value) {
        // TEST OK
        IJstack.setVoxel(x, y, z, value);

        /*
         * if (x >= 0 && x < sizex && y >= 0 && y < sizey && z >= 0 && z <
         * sizez) { if (virtual) { if (current != z) { FileSaver fs = new
         * FileSaver(new ImagePlus("tmp", processor)); if (debug) {
         * IJ.write("saving " + files[current]); } fs.saveAsTiff(directory +
         * File.separator + files[current]); current = z; ImagePlus plus = new
         * Opener().openImage(directory, files[current]); processor =
         * plus.getProcessor(); } processor.putPixel(x, y, value); } else {
         * //img[x + y * sizex + z * sizexy] = value; if (getType() == SHORT) {
         * short[] pixtmp = (short[]) IJstack.getPixels(z + 1); pixtmp[x + y *
         * sizex] = (short) value; } else { byte[] pixtmp = (byte[])
         * IJstack.getPixels(z + 1); pixtmp[x + y * sizex] = (byte) value; } }
         * maxPixel = null; minPixel = null; meanValue = Float.NaN; sigma =
         * Float.NaN; }
         */
    }

    public final void putPixel(int x, int y, int z, double value) {
        // TEST OK
        IJstack.setVoxel(x, y, z, value);
    }

    /**
     * binarisation de l'image, creation d'une image avec 255 si entre les deux
     * seuils
     *
     * @param lowThreshold seuil bas
     * @param highThreshold seuil haut
     * @return une Image3D contenant l'image binarisee
     */
    public IntImage3D binarisation(int lowThreshold, int highThreshold) {
        IntImage3D bin = new IntImage3D(sizex, sizey, sizez, Image3D.BYTE);

        int pix;
        for (int k = 0; k < sizez; k++) {
            for (int j = 0; j < sizey; j++) {
                for (int i = 0; i < sizex; i++) {
                    pix = this.getPixel(i, j, k);
                    if ((pix < lowThreshold) || (pix > highThreshold)) {
                        bin.putPixel(i, j, k, 0);
                    } else {
                        bin.putPixel(i, j, k, 255);
                    }
                }
            }
        }
        return bin;
    }

    /**
     *
     * @param background
     * @return
     */
    public boolean isBinary(int background) {
        int mi1 = this.getMinAboveValue(background);
        int mi2 = this.getMinAboveValue(mi1);
        if (mi2 == Integer.MAX_VALUE) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * extension de l'histogramme de l'image 8 bits
     */
    public void extendHisto() {
        if ((maxPixel == null) || (minPixel == null)) {
            computeMinMax();
        }

        if (maxPixel.getValue() == 255 && minPixel.getValue() == 0) {
            return;
        }
        double maxmin = (double) (maxPixel.getValue() - minPixel.getValue());
        int MIN = (int) minPixel.getValue();
        for (int k = 0; k < sizez; k++) {
            for (int j = 0; j < sizey; j++) {
                for (int i = 0; i < sizex; i++) {
                    this.putPixel(i, j, k,
                            (int) (255 * (double) (this.getPixel(i, j, k) - MIN) / (maxmin)));

                }
            }
        }
        maxPixel = null;
        minPixel = null;
        meanValue = Float.NaN;
        stDev = Float.NaN;
    }

    /**
     * donne le maximum de l'image3D
     *
     * @return la valeur maximum de l'image3D
     */
    public int getMaximum() {
        if (maxPixel == null) {
            computeMinMax();
        }
        return (int) maxPixel.getValue();
    }

    /**
     * donne le minimum de l'image3D
     *
     * @return la valeur minimum de l'image3D
     */
    public int getMinimum() {
        if (minPixel == null) {
            computeMinMax();
        }
        return (int) minPixel.getValue();
    }

    /**
     * Gets the minAboveValue attribute of the FishImage3D object
     *
     * @param value Description of the Parameter
     * @return The minAboveValue value
     */
    public int getMinAboveValue(int value) {
        int mini = Integer.MAX_VALUE;
        for (int k = 0; k < sizez; k++) {
            for (int j = 0; j < sizey; j++) {
                for (int i = 0; i < sizex; i++) {
                    if ((this.getPixel(i, j, k) > value) && (this.getPixel(i, j, k) < mini)) {
                        mini = this.getPixel(i, j, k);
                    }
                }
            }
        }
        return mini;
    }

    /**
     * Gets the meanValue attribute of the IntImage3D object
     *
     * @param sb Description of the Parameter
     * @param sh Description of the Parameter
     * @return The meanValue value
     */
    public int getMean(int sb, int sh) {
        double total = 0;
        int count = 0;
        int pix;
        for (int k = 0; k < sizez; k++) {
            for (int j = 0; j < sizey; j++) {
                for (int i = 0; i < sizex; i++) {
                    pix = this.getPixel(i, j, k);
                    if ((pix >= sb) && (pix <= sh)) {
                        total += pix;
                        count++;
                    }
                }
            }
        }
        return (int) ((total) / (double) (count));
    }

    /**
     * Gets the count attribute of the IntImage3D object
     *
     * @param sb Description of the Parameter
     * @param sh Description of the Parameter
     * @return The count value
     */
    public int getCount(int sb, int sh) {
        int count = 0;
        int pix;
        for (int k = 0; k < sizez; k++) {
            for (int j = 0; j < sizey; j++) {
                for (int i = 0; i < sizex; i++) {
                    pix = this.getPixel(i, j, k);
                    if ((pix >= sb) && (sh <= sh)) {
                        count++;
                    }
                }
            }
        }

        return count;
    }

    /**
     * Tappering - smooth towards background
     *
     * @param r0x radius x
     * @param r0y radius y
     * @param r0z radius z
     * @return the tappered image
     */
    public IntImage3D tapper(int r0x, int r0y, int r0z) {
        int x;
        int y;
        int z;
        int x0;
        int y0;
        int z0;
        int pix;
        double val;
        double rapport;
        double coeff;
        double r;
        double rm = 0.5;
        int moy = (int) getMean();

        IntImage3D res = new IntImage3D(sizex, sizey, sizez, type);

        for (z = 0; z < sizez; z++) {
            for (x = 0; x < sizex; x++) {
                for (y = 0; y < sizey; y++) {
                    r = ((x - centerx) * (x - centerx)) / (r0x * r0x) + ((y - centery) * (y - centery)) / (r0y * r0y) + ((z - centerz) * (z - centerz)) / (r0z * r0z);
                    if (r >= 1) {
                        res.putPixel(x, y, z, moy);
                    } else {
                        if (r >= rm) {
                            x0 = (int) (centerx + (x - centerx) * (rm / r));
                            y0 = (int) (centery + (y - centery) * (rm / r));
                            z0 = (int) (centerz + (z - centerz) * (rm / r));
                            pix = getPixel(x0, y0, z0);

                            rapport = (1.0 - r) / rm;
                            coeff = Math.cos(1.57 * rapport);
                            val = (1.0 - coeff) * pix + coeff * moy;
                            res.putPixel(x, y, z,
                                    (int) val);

                            if (debug) {
                                System.out.println(" " + x + " " + y + " " + z + " - " + x0 + " " + y0 + " " + z0 + " - " + coeff + " " + pix);
                            }
                        } else {
                            res.putPixel(x, y, z, getPixel(x, y, z));

                        }
                    }

                }
            }
        }
        return res;
    }

    /**
     * Gets the sigma attribute of the IntImage3D object
     *
     * @param sb Description of the Parameter
     * @param sh Description of the Parameter
     * @return The sigma value
     */
    public double getSigma(int sb, int sh) {
        double smean = getMean(sb, sh);
        int total = 0;
        int count = 0;
        int pix;
        for (int k = 0; k < sizez; k++) {
            for (int j = 0; j < sizey; j++) {
                for (int i = 0; i < sizex; i++) {
                    pix = this.getPixel(i, j, k);
                    if ((pix >= sb) && (pix <= sh)) {
                        total += (this.getPixel(i, j, k) - smean) * (this.getPixel(i, j, k) - smean);

                        count++;
                    }
                }
            }
        }
        total /= (count - 1);
        return Math.sqrt(total);
    }

    /**
     * donne une ligne de l'image3D
     *
     * @param x coordonnee en x du pixel de depart
     * @param y coordonnee en y du pixel de depart
     * @param z coordonnee en z du pixel de depart
     * @return la ligne de pixel selon l'axe x
     */
    public ArrayUtil getRow(int x, int y, int z) {
        ArrayUtil tab = new ArrayUtil(sizex);
        for (int i = 0; i < sizex; i++) {
            tab.putValue(i, this.getPixel(i, y, z));
        }
        return tab;
    }

    /**
     * int donne une ligne de l'image3D
     *
     * @param x coordonnee en x du pixel de depart
     * @param y coordonnee en y du pixel de depart
     * @param z coordonnee en z du pixel de depart
     * @return la ligne de pixel selon l'axe y
     */
    public ArrayUtil getColumn(int x, int y, int z) {
        ArrayUtil tab = new ArrayUtil(sizey);
        for (int i = 0; i < sizey; i++) {
            tab.putValue(i, this.getPixel(x, i, z));
        }
        return tab;
    }

    /**
     * donne une ligne en Z dans l'image 3D
     *
     * @param x coordonnee en x du pixel de depart
     * @param y coordonnee en y du pixel de depart
     * @param z coordonnee en z du pixel de depart
     * @return la ligne de pixel selon l'axe z
     */
    public ArrayUtil getProf(int x, int y, int z) {
        ArrayUtil tab = new ArrayUtil(sizez);
        for (int i = 0; i < sizez; i++) {
            tab.putValue(i, this.getPixel(x, y, i));
        }
        return tab;
    }

    /**
     * donne l'histogramme de l'image3D 16 bits
     *
     * @return l'histogramme
     */
    public HistogramUtil getHistogram() {
        int pix;
        double[] tab = new double[this.getMaximum() + 1];
        for (int k = 0; k < sizez; k++) {
            for (int j = 0; j < sizey; j++) {
                for (int i = 0; i < sizex; i++) {
                    pix = this.getPixel(i, j, k);
                    tab[pix]++;
                }
            }
        }
        return new HistogramUtil(tab);
    }

    /**
     * dit si le pixel donne est un maximum local avec un voisinage donne
     *
     * @param x coordonnee en x du pixel
     * @param y coordonnee en y du pixel
     * @param z coordonnee en z du pixel
     * @param voisx voisinage en x
     * @param voisy voisinage en y
     * @param voisz voisinage en z
     * @return vrai si le pixel est maximum local dans le voisinage donne
     */
    public boolean isLocalMaximum(int x, int y, int z, float voisx, float voisy, float voisz) {

        ArrayUtil voisinage = getNeighborhoodSphere(x, y, z, voisx, voisy, voisz);

        if (voisinage.getMaximum() == this.getPixel(x, y, z)) {
            return true;
        }
        return false;
    }

    /**
     * Création d'une image des maxima locaux dans un voisinage donne
     *
     * @param voisx taille voisinage en x
     * @param voisy taille voisinage en y
     * @param voisz taille voisinage en z
     * @param keep
     * @return Image des maxima locaux
     */
    public IntImage3D createLocalMaximaImage(float voisx, float voisy, float voisz, boolean keep) {
        return createLocalMaximaImage(voisx, voisy, voisz, 0, keep);
    }

    /**
     * Création d'une image des maxima locaux dans un voisiange donne
     *
     * @param voisx taille voisinage en x
     * @param voisy taille voisinage en y
     * @param voisz taille voisinage en z
     * @param th threshold image (255)
     * @param keep
     * @return Image des maxima locaux
     */
    public IntImage3D createLocalMaximaImage(float voisx, float voisy, float voisz, int th, boolean keep) {
        // create kernel
        int[] ker = createKernelEllipsoid(voisx, voisy, voisz);
        int nb = 0;
        for (int i = 0; i < ker.length; i++) {
            nb += ker[i];
        }
        int pix;
        int vx = (int) Math.ceil(voisx);
        int vy = (int) Math.ceil(voisy);
        int vz = (int) Math.ceil(voisz);
        IntImage3D maxima = new IntImage3D(sizex, sizey, sizez, type);
        //parcours de l'image3D
        ArrayUtil tab;
        for (int k = vz; k < sizez - vz; k++) {
            if (this.showStatus) {
                IJ.showStatus("3D Max Local : " + (int) (100 * k / sizez) + "%");
            }
            for (int j = vy; j < sizey - vy; j++) {
                for (int i = vx; i < sizex - vx; i++) {
                    pix = getPixel(i, j, k);
                    if (pix > th) {
                        tab = getNeighborhoodKernel(ker, nb, i, j, k, voisx, voisy, voisz);
                        if (tab.getMaximum() == pix) {
                            // IJ.log(""+i+" "+j+" "+k+" "+pix);
                            if (!keep) {
                                maxima.putPixel(i, j, k, 255);
                            } else {
                                maxima.putPixel(i, j, k, pix);
                            }
                        }
                    }
                }
            }
        }
        return maxima;
    }

    /**
     * Adds a feature to the Image attribute of the IntImage3D object
     *
     * @param img The feature to be added to the Image attribute
     * @param s The feature to be added to the Image attribute
     * @return Description of the Return Value
     */
    public Image3D addImage(IntImage3D img, float s) {
        IntImage3D add = new IntImage3D(sizex, sizey, sizez, type);
        for (int k = 0; k < sizez; k++) {
            for (int j = 0; j < sizey; j++) {
                for (int i = 0; i < sizex; i++) {
                    add.putPixel(i, j, k, (int) (getPixel(i, j, k) + s * img.getPixel(i, j, k)));

                }
            }
        }

        return add;
    }

    /**
     * Description of the Method
     *
     * @param s Description of the Parameter
     * @return Description of the Return Value
     */
    public Image3D addValue(int s) {
        IntImage3D add = new IntImage3D(sizex, sizey, sizez, type);
        for (int k = 0; k < sizez; k++) {
            for (int j = 0; j < sizey; j++) {
                for (int i = 0; i < sizex; i++) {
                    add.putPixel(i, j, k, (int) (getPixel(i, j, k) + s));

                }
            }
        }

        return add;
    }

    /**
     *
     * @param ima
     * @return
     */
    public IntImage3D min(IntImage3D ima) {
        IntImage3D mini = new IntImage3D(sizex, sizey, sizez, type);
        for (int k = 0; k < sizez; k++) {
            for (int j = 0; j < sizey; j++) {
                for (int i = 0; i < sizex; i++) {
                    mini.putPixel(i, j, k, Math.min(this.getPixel(i, j, k), ima.getPixel(i, j, k)));
                }
            }
        }

        return mini;
    }

    public IntImage3D max(IntImage3D ima) {
        IntImage3D maxi = new IntImage3D(sizex, sizey, sizez, type);
        for (int k = 0; k < sizez; k++) {
            for (int j = 0; j < sizey; j++) {
                for (int i = 0; i < sizex; i++) {
                    maxi.putPixel(i, j, k, Math.max(this.getPixel(i, j, k), ima.getPixel(i, j, k)));
                }
            }
        }

        return maxi;
    }

    /**
     * Compute the log of the image
     *
     * @return
     */
    public RealImage3D logMath() {
        RealImage3D logImage = new RealImage3D(sizex, sizey, sizez);

        for (int k = 0; k < sizez; k++) {
            for (int j = 0; j < sizey; j++) {
                for (int i = 0; i < sizex; i++) {
                    logImage.putPixel(i, j, k, Math.log(getPixel(i, j, k)));

                }
            }
        }

        return logImage;
    }

    /**
     * Replace a pixel value by another
     *
     * @param val the value to be replaced
     * @param rep the new value
     */
    public void replacePixelsValue(int val, int rep) {
        for (int k = 0; k < sizez; k++) {
            for (int j = 0; j < sizey; j++) {
                for (int i = 0; i < sizex; i++) {
                    if (getPixel(i, j, k) == val) {
                        putPixel(i, j, k, rep);
                    }
                }
            }
        }
    }

    /**
     * Replace a pixel value by another within a specified object
     *
     * @param val the value to be replaced
     * @param rep the new value
     * @param obj the object
     */
    public void replacePixelsValue(int val, int rep, Object3DVoxels obj) {
        int xmin = obj.getXmin();
        int ymin = obj.getYmin();
        int zmin = obj.getZmin();
        int xmax = obj.getXmax();
        int ymax = obj.getYmax();
        int zmax = obj.getZmax();

        for (int k = zmin; k <= zmax; k++) {
            for (int j = ymin; j <= ymax; j++) {
                for (int i = xmin; i <= xmax; i++) {
                    if ((getPixel(i, j, k) == val) && obj.inside(i, j, k)) {
                        putPixel(i, j, k, rep);
                    }
                }
            }
        }
    }

    /**
     * number of pixels of intersection in two images
     *
     * @param autre other image
     * @param val pixel value
     * @param valautre other pixel value
     * @param obj object
     * @param objautre other object
     * @return number of intersecting pixels
     */
//    public int intersectionPixels(IntImage3D autre, int val, int valautre, Object3DVoxels obj, Object3DVoxels objautre) {
//
//        int count = 0;
//        int xmin;
//        int ymin;
//        int zmin;
//        int xmax;
//        int ymax;
//        int zmax;
//        if (obj == null) {
//            xmin = 0;
//            ymin = 0;
//            zmin = 0;
//            xmax = sizex;
//            ymax = sizey;
//            zmax = sizez;
//        } else {
//            xmin = obj.getXmin();
//            ymin = obj.getYmin();
//            zmin = obj.getZmin();
//            xmax = obj.getXmax();
//            ymax = obj.getYmax();
//            zmax = obj.getZmax();
//        }
//        if (objautre != null) {
//            xmin = Math.max(xmin, objautre.getXmin());
//            ymin = Math.max(ymin, objautre.getYmin());
//            zmin = Math.max(zmin, objautre.getZmin());
//            xmax = Math.min(xmax, objautre.getXmax());
//            ymax = Math.min(ymax, objautre.getYmax());
//            zmax = Math.min(zmax, objautre.getZmax());
//        }
//        for (int k = zmin; k <= zmax; k++) {
//            for (int j = ymin; j <= ymax; j++) {
//                for (int i = xmin; i <= xmax; i++) {
//                    if ((getPixel(i, j, k) == val) && (autre.getPixel(i, j, k) == valautre)) {
//                        count++;
//                    }
//                }
//            }
//        }
//        return count;
//    }
    /**
     * Dilatation par rapport a une image de base
     *
     * @param base Image
     * @param voisx taille voisinage en x
     * @param voisy taille voisinage en y
     * @param voisz taille voisinage en z
     * @return Image dilatee
     */
    protected IntImage3D dilatationConditionGris(IntImage3D base, int voisx, int voisy, int voisz) {

        int maxpix;

        IntImage3D res = new IntImage3D(sizex, sizey, sizez);
        res=this.maximumFilter(voisx, voisy, voisz);
        //this.filterGeneric(res, voisx, voisy, voisz, 0, sizez, Image3D.FILTER_MAX);

        for (int z = 0; z < sizez; z++) {
            for (int y = 0; y < sizey; y++) {
                for (int x = 0; x < sizex; x++) {
                    maxpix = res.getPixel(x, y, z);
                    if (maxpix > base.getPixel(x, y, z)) {
                        res.putPixel(x, y, z, base.getPixel(x, y, z));

                    }
                }
            }
        }
        return res;
    }

    protected void flood3D(IntImage3D base, int x0, int y0, int z0, int val) {
        boolean changement = true;
        int sens = 1;
        int xdep = x0;
        int ydep = y0;
        int zdep = z0;
        int xfin = xdep + 1;
        int yfin = ydep + 1;
        int zfin = zdep + 1;

        int b = 100;

        this.putPixel(x0, y0, z0, val);
        int a = base.getPixel(x0, y0, z0);

        while (changement) {
            changement = false;
            for (int k = sens == 1 ? zdep : zfin; ((sens == 1 && k <= zfin) || (sens == -1 && k >= zdep)); k += sens) {
                for (int j = sens == 1 ? ydep : yfin; ((sens == 1 && j <= yfin) || (sens == -1 && j >= ydep)); j += sens) {
                    for (int i = sens == 1 ? xdep : xfin; ((sens == 1 && i <= xfin) || (sens == -1 && i >= xdep)); i += sens) {
                        if (this.getPixel(i, j, k) == val) {
                            for (int n = k - 1; n < k + 2; n++) {
                                for (int m = j - 1; m < j + 2; m++) {
                                    for (int l = i - 1; l < i + 2; l++) {
                                        if ((base.getPixel(l, m, n) == a) && (this.getPixel(l, m, n) != val)) {
                                            changement = true;
                                            this.putPixel(l, m, n, val);
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
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * Description of the Method
     *
     * @param base Description of the Parameter
     * @param abs Description of the Parameter
     * @param ord Description of the Parameter
     * @param val Description of the Parameter
     * @param prof Description of the Parameter
     */
    protected void propagationConditionnelle(IntImage3D base, int abs, int ord, int prof, int val) {

        int x;
        int y;
        int z;
        int i;
        int j;
        int k;
        boolean ok;
        int b;
        int a;

        a = val - 1;
        b = 100;
        this.putPixel(abs, ord, prof, b);
        int c = 0;
        do {
            IJ.showStatus("Binary " + c);
            c++;
            ok = false;
            for (z = 0; z < sizez; z++) {
                for (y = 0; y < sizey; y++) {
                    for (x = 0; x < sizex; x++) {
                        if (getPixel(x, y, z) == b) {
                            for (k = -1; k <= 1; k++) {
                                for (j = -1; j <= 1; j++) {
                                    for (i = -1; i <= 1; i++) {
                                        if ((i != 0) || (j != 0) || (k != 0)) {
                                            if ((base.getPixel(x + i, y + j, z + k) == a) && (getPixel(x + i, y + j, z + k) != b)) {
                                                ok = true;
                                                putPixel(x + i, y + j, z + k, b);
                                            }
                                        }
                                    }
                                }
                            }

                        }
                    }
                }
            }
            for (z = sizez - 1; z >= 0; z--) {
                for (y = sizey - 1; y >= 0; y--) {
                    for (x = sizex - 1; x >= 0; x--) {
                        if (getPixel(x, y, z) == b) {
                            for (k = -1; k <= 1; k++) {
                                for (j = -1; j <= 1; j++) {
                                    for (i = -1; i <= 1; i++) {
                                        if ((i != 0) || (j != 0) || (k != 0)) {
                                            if ((base.getPixel(x + i, y + j, z + k) == a) && (getPixel(x + i, y + j, z + k) != b)) {
                                                ok = true;
                                                putPixel(x + i, y + j, z + k, b);
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

        } while (ok);

        this.replacePixelsValue(b, a);
    }

    /**
     * Propagation of a value to connected pixels
     *
     * @param xx x of the first point
     * @param yy y of the first point
     * @param zz z of the first point
     * @param val value of the connected pixels
     * @param nval value to propagate
     */
    public void propagation27(int xx, int yy, int zz, int val, int nval) {
        debug = true;
        //ImagePlus test = new ImagePlus("propa", this.getStack());
        //int count = 0;
        //test.show();
        int i;
        int j;
        int k;
        int b;
        int a;
        boolean change = true;
        int sens = 1;
        int xdep = xx;
        int ydep = yy;
        int zdep = zz;
        int xfin = xdep + 1;
        int yfin = ydep + 1;
        int zfin = zdep + 1;

        a = val;
        b = 100;

        ImagePlus plus = new ImagePlus("propa", this.getStack());
        if (debug) {
            System.out.println("a=" + a + " b=" + b + " nval=" + nval);
            plus.show();
        }
        while ((b == a) || (b == val) || (b == nval)) {
            b++;
        }
        if (debug) {
            System.out.println("b=" + b);
        }
        this.putPixel(xx, yy, zz, b);

        int c = 0;
        while (change) {
            change = false;
            if (debug) {
                System.out.println("sens=" + sens + " change" + change + " " + xdep + " " + xfin + " " + ydep + " " + yfin + " " + zdep + " " + zfin);
            }
            for (k = sens == 1 ? zdep : zfin; ((sens == 1 && k <= zfin) || (sens == -1 && k >= zdep)); k += sens) {
                for (j = sens == 1 ? ydep : yfin; ((sens == 1 && j <= yfin) || (sens == -1 && j >= ydep)); j += sens) {
                    for (i = sens == 1 ? xdep : xfin; ((sens == 1 && i <= xfin) || (sens == -1 && i >= xdep)); i += sens) {
                        if (this.getPixel(i, j, k) == b) {
                            for (int n = k - 1; n < k + 2; n++) {
                                for (int m = j - 1; m < j + 2; m++) {
                                    for (int l = i - 1; l < i + 2; l++) {
                                        if (this.getPixel(l, m, n) == a) {
                                            if (debug) {
                                                plus.updateAndDraw();
                                            }
                                            this.putPixel(l, m, n, b);
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
                                            change = true;
                                            c++;
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
            sens *= -1;
            //count++;
            //test.setStack("propa" + count, this.getStack());
            //test.updateAndDraw();
        }
        replacePixelsValue(b, nval);
    }

    /**
     * Propagation of a value to connected pixels
     *
     * @param xx x of the first point
     * @param yy y of the first point
     * @param zz z of the first point
     * @param val value of the connected pixels
     * @param nval value to propagate
     */
    public void propagation6(int xx, int yy, int zz, int val, int nval) {
        debug = false;
        int i;
        int j;
        int k;
        int b;
        int a;
        boolean change = true;
        int sens = 1;
        int xdep = xx;
        int ydep = yy;
        int zdep = zz;
        int xfin = xdep + 1;
        int yfin = ydep + 1;
        int zfin = zdep + 1;

        int p1, p2, p3, p4, p5, p6;

        a = val;
        b = 100;

        while ((b == a) || (b == val) || (b == nval)) {
            b++;
        }
        if (debug) {
            System.out.println("b=" + b);
        }
        ImagePlus plus = new ImagePlus("propa", this.getStack());
        if (debug) {
            System.out.println("a=" + a + " b=" + b + " nval=" + nval);
            plus.show();
        }
        this.putPixel(xx, yy, zz, b);

        while (change) {
            change = false;
            if (debug) {
                System.out.println("sens=" + sens + " change" + change + " " + xdep + " " + xfin + " " + ydep + " " + yfin + " " + zdep + " " + zfin);
            }
            for (k = sens == 1 ? zdep : zfin; ((sens == 1 && k <= zfin) || (sens == -1 && k >= zdep)); k += sens) {
                for (j = sens == 1 ? ydep : yfin; ((sens == 1 && j <= yfin) || (sens == -1 && j >= ydep)); j += sens) {
                    for (i = sens == 1 ? xdep : xfin; ((sens == 1 && i <= xfin) || (sens == -1 && i >= xdep)); i += sens) {
                        if (this.getPixel(i, j, k) == b) {
                            p1 = getPixel(i + 1, j, k);
                            p2 = getPixel(i - 1, j, k);
                            p3 = getPixel(i, j + 1, k);
                            p4 = getPixel(i, j - 1, k);
                            p5 = getPixel(i, j, k + 1);
                            p6 = getPixel(i, j, k - 1);
                            if (p1 == a) {
                                change = true;
                                putPixel(i + 1, j, k, b);
                                if (i + 1 > xfin) {
                                    xfin = i + 1;
                                }
                            }
                            if (p2 == a) {
                                change = true;
                                putPixel(i - 1, j, k, b);
                                if (i - 1 < xdep) {
                                    xdep = i - 1;
                                }
                            }
                            if (p3 == a) {
                                change = true;
                                putPixel(i, j + 1, k, b);
                                if (j + 1 > yfin) {
                                    yfin = j + 1;
                                }
                            }
                            if (p4 == a) {
                                change = true;
                                putPixel(i, j - 1, k, b);
                                if (j - 1 < ydep) {
                                    ydep = j - 1;
                                }
                            }
                            if (p5 == a) {
                                change = true;
                                putPixel(i, j, k + 1, b);
                                if (k + 1 > zfin) {
                                    zfin = k + 1;
                                }
                            }
                            if (p6 == a) {
                                change = true;
                                putPixel(i, j, k - 1, b);
                                if (k - 1 < zdep) {
                                    zdep = k - 1;
                                }
                            }
                        }
                    }
                }
            }
            sens *= -1;
        }
        replacePixelsValue(b, nval);
    }

    /**
     * Propagation of a value to connected pixels
     *
     * @param abs x of the first point
     * @param ord y of the first point
     * @param prof z of the first point
     * @param val value of the connected pixels
     * @param nval value to propagate
     * @param base Description of the Parameter
     */
    public void propagation(IntImage3D base, int abs, int ord, int prof, int val, int nval) {
        //ImagePlus test = new ImagePlus("propa", this.getStack());
        //int count = 0;
        //test.show();
        IntImage3D tmp = new IntImage3D(base);
        int i;
        int j;
        int k;
        int b;
        int a;
        boolean changement = true;
        int sens = 1;
        int xdep = abs;
        int ydep = ord;
        int zdep = prof;
        int xfin = xdep + 1;
        int yfin = ydep + 1;
        int zfin = zdep + 1;

        a = val;
        b = 100;
        while ((b == a) || (b == val) || (b == nval)) {
            b++;
        }
        this.putPixel(abs, ord, prof, b);

        while (changement) {
            changement = false;
            for (k = sens == 1 ? zdep : zfin;
                    ((sens == 1 && k <= zfin) || (sens == -1 && k >= zdep)); k += sens) {
                for (j = sens == 1 ? ydep : yfin;
                        ((sens == 1 && j <= yfin) || (sens == -1 && j >= ydep)); j += sens) {
                    for (i = sens == 1 ? xdep : xfin;
                            ((sens == 1 && i <= xfin) || (sens == -1 && i >= xdep)); i += sens) {
                        if (this.getPixel(i, j, k) == b) {
                            for (int n = k - 1; n < k + 2; n++) {
                                for (int m = j - 1; m < j + 2; m++) {
                                    for (int l = i - 1; l < i + 2; l++) {
                                        if (tmp.getPixel(l, m, n) == a) {
                                            tmp.putPixel(l, m, n, 0);
                                            this.putPixel(l, m, n, b);
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
            sens *= -1;
            //count++;
            //test.setStack("propa" + count, this.getStack());
            //test.updateAndDraw();


        }
        replacePixelsValue(b, nval);

        tmp = null;


    }

    /**
     * Watershed algorithm starting with a seed image where seed > 0
     *
     * @return The segmented image
     */
    public IntImage3D watershed() {
        IntImage3D label = new IntImage3D(this);
        IntImage3D tmp;


        int go = 1;


        int i;


        int j;


        int k;


        int rx = 1;


        int ry = 1;


        int rz = 1;
        ArrayUtil tab;
        ArrayUtil tab2;



        int count = 1;


        int pix;
        // label the image with object seeds


        for (k = 0; k
                < sizez; k++) {
            for (i = 0; i < sizex; i++) {
                for (j = 0; j < sizey; j++) {
                    pix = label.getPixel(i, j, k);


                    if ((pix > 0)) {
                        label.putPixel(i, j, k, count);
                        label.propagation27(i, j, k, pix, count);
                        count++;

                    }


                    IJ.showStatus("Watershed 3D Labels : " + count);


                }
            }
        }

        //tmp = new IntImage3D(label);
        //int taille = getSizex() * getSizey() * getSizez();
        //go = taille;

        //ImagePlus test = new ImagePlus("labels", tmp.getStack());
        //test.show();

        // maxPixel depth of the image
        int ite = getMaximum();



        while (ite > 0) {
            for (k = 0; k < sizez; k++) {
                if (this.showStatus) {
                    IJ.showStatus("Watershed 3D Segmentation (" + ite + ") : " + k + " / " + sizez + "   ");


                }
                for (i = 0; i < sizex; i++) {
                    for (j = 0; j < sizey; j++) {
                        // if pix not been labelled
                        if (label.getPixel(i, j, k) == 0) {
                            tab2 = getNeighborhood3x3x3(i, j, k);
                            // if neighbor is value ite


                            if (tab2.hasValue(ite)) {
                                tab = label.getNeighborhood3x3x3(i, j, k);
                                // only distinct values
                                tab = tab.distinctValues();
                                // two values one for zero and one for object


                                if ((tab.getMaximum() > 0) && (tab.getSize() == 2)) {
                                    this.putPixel(i, j, k, ite - 1);
                                    label.putPixel(i, j, k, (int) tab.getMaximum());


                                }
                            }
                        }
                    }
                }
            }
            //label.insert(tmp, 0, 0, 0, false);
            ite--;
            //test.setStack("water", tmp.getStack());
            //test.updateAndDraw();


        }
        return label;


    }

    /**
     * Separate objects from an EDM image
     *
     * @return The segmented image with separated obects
     */
//    public IntImage3D separateWatershed() {
//        int val = this.getMaximum();
//        IntImage3D seg = new IntImage3D(sizex, sizey, sizez);
//        IntImage3D sep = new IntImage3D(sizex, sizey, sizez);
//        IntImage3D neigh;
//        int pix;
//        int minVal;
//        ArrayUtil tab;
//        double val1;
//        double val2;
//        int c = 1;
//        while (val >= 1) {
//            // Find seeds
//            for (int z = 0; z < sizez; z++) {
//                for (int y = 0; y < sizey; y++) {
//                    for (int x = 0; x < sizex; x++) {
//                        pix = getPixel(x, y, z);
//                        if (pix == val) {
//                            tab = seg.getNeighborhood3x3x3(x, y, z);
//                            minVal = (int) tab.getMinimumAbove(0);
//                            if ((minVal == 0) && (isLocalMaximum(x, y, z, 2, 2, 2))) {
//                                seg.putPixel(x, y, z, c);
//                                seg.propagation(this, x, y, z, val, c);
//                                c++;
//                            }
//                        }
//                    }
//                }
//            }
//
//            //new ImagePlus("seg " + val, seg.getStack()).show();
//            // Neighbor image
//            int nb = 2;
//            while (nb <= 2) {
//                nb++;
//                neigh = new IntImage3D(sizex, sizey, sizez);
//                for (int z = 0; z < sizez; z++) {
//                    for (int y = 0; y < sizey; y++) {
//                        for (int x = 0; x < sizex; x++) {
//                            if (this.getPixel(x, y, z) > 0) {
//                                tab = seg.getNeighborhood3x3x3(x, y, z);
//                                if (!tab.hasOnlyValue(0)) {
//                                    val1 = tab.getMinimumAbove(0);
//                                    val2 = tab.getMinimumAbove(val1);
//                                    if (val1 == val2) {
//                                        neigh.putPixel(x, y, z, (int) val1);
//                                    } else {
//                                        sep.putPixel(x, y, z, 255);
//                                    }
//                                }
//                            }
//                        }
//                    }
//                }
//                //new ImagePlus("neigh " + val, neigh.getStack()).show();
//                seg = neigh;
//            }
//            val--;
//        }
//        return seg;
//    }
    /**
     * Description of the Method
     *
     * @param base Description of the Parameter
     * @return Description of the Return Value
     */
    public ImageInt binaryReconstruct(ImageInt base) {
        int i;
        int j;
        int k;
        int val = 255;

        ImageInt res = new ImageByte("",sizex, sizey, sizez);

//        for (k = 0; k < sizez; k++) {
//            for (i = 0; i < sizex; i++) {
//                for (j = 0; j < sizey; j++) {
//                    //IJ.showStatus("Binary reconstruct "+k);
//                    if (base.getPixel(i, j, k) != 0) {
//                        if ((getPixel(i, j, k) != 0) && (res.getPixel(i, j, k) == 0)) {
//                            //IJ.log("flooding "+i+" "+j+" "+k);
//                            res.flood3D(this, i, j, k, val);
//                        }
//                    }
//                }
//            }
//        }
        return res;
    }

    public ImageInt binaryReconstructByObjects(ImageInt base) {
        //System.out.println("max "+this.getMaximum());
        Segment3DImage seg = new Segment3DImage(new ImageShort(this.getStack()), 1, 65535);
        seg.segment();
        ImageInt label = seg.getLabelledObjectsImage3D();
        boolean[] ok = new boolean[(int) label.getMax() + 1];
        for (int i = 0; i < ok.length; i++) {
            ok[i] = false;
        }
        if (label.getMax() > 0) {
            for (int k = 0; k < sizez; k++) {
                for (int j = 0; j < sizey; j++) {
                    for (int i = 0; i < sizex; i++) {
                        if (base.getPixel(i, j, k) != 0) {
                            ok[label.getPixelInt(i, j, k)] = true;
                        }
                    }
                }
            }

            for (int k = 0; k < sizez; k++) {
                for (int j = 0; j < sizey; j++) {
                    for (int i = 0; i < sizex; i++) {
                        int pix = label.getPixelInt(i, j, k);
                        if (pix > 0) {
                            if (ok[pix]) {
                                label.setPixel(i, j, k, 255);
                            } else {
                                label.setPixel(i, j, k, 0);
                            }
                        }
                    }
                }
            }
        }

        return label;

    }

    public IntImage3D grayReconstruction(IntImage3D base) {
        int maxVal = (int) base.getMaximum();

        IntImage3D res = new IntImage3D(sizex, sizey, sizez);

//        // taken from G. Landini GreyscaleReconstruct_ 
//        for (int i = maxVal; i >= 0; i--) {
//            ImageInt copyOrig = ImageInt.wrap(IJstack);
//            ImageInt copyBase = (ImageInt) base.duplicate();
//            
//            copyOrig = copyOrig.thresholdAbove(i);
//            copyBase = copyBase.thresholdAbove(i);
//
//
//            IJ.log("\\Clear");
//            IJ.log("binary reconstruct " + i);
//            copyBase = copyOrig.binaryReconstructByObjects(copyBase);
//
//            copyBase.replacePixelsValue(255, i);
//
//            res = res.max(copyBase);
//        }

        return res;

    }

    /**
     * Méthode du RH maxima
     *
     * @param R Paramètre du rayon R
     * @param H Paramètre de la hauteur H
     * @return Image binarisée résultat
     */
    public IntImage3D RH_Maxima(int R, int H) {
        IntImage3D ptr1;
        IntImage3D ptr2;
        IntImage3D ptr3=null;
        IntImage3D maxImage;
        int p1;
//        if (this.showStatus) {
//            IJ.showStatus("Running Maxima locaux ...");
//        }
//        maxImage = new IntImage3D(this);
//        maxImage = this.createLocalMaximaImage(R, R, R, -1, true);
//        if (debug) {
//            new ImagePlus("max locaux", maxImage.getStack()).show();
//        }
//        ptr1 = new IntImage3D(sizex, sizey, sizez, type);
//        for (int k = R; k < sizez - R; k++) {
//            for (int i = R; i < sizex - R; i++) {
//                for (int j = R; j < sizey - R; j++) {
//                    p1 = this.getPixel(i, j, k);
//
//                    if (maxImage.getPixel(i, j, k) > 0) {
//                        if (p1 > H) {
//                            p1 = p1 - H;
//                        } else {
//                            p1 = 0;
//                        }
//                    } else {
//                        p1 = 0;
//                    }
//                    ptr1.putPixel(i, j, k, p1);
//                }
//            }
//        }
//        if (debug) {
//            IntImage3D maxh = new IntImage3D(ptr1);
//            new ImagePlus("max -H", maxh.getStack()).show();
//        }
//        if (debug) {
//            IJ.log("Running Dilatation conditionelle ...");
//        }
//        ptr2 = ptr1.dilatationConditionGris(this, R, R, R);
//        if (debug) {
//            new ImagePlus("RH : Dilate autour des max locaux",
//                    ptr2.getStack()).show();
//        }
//        ptr3 = ptr2.binarisation(0, 0);
//        for (int k = 0; k < sizez; k++) {
//            for (int i = 0; i < sizey; i++) {
//                for (int j = 0; j < sizey; j++) {
//                    ptr1.putPixel(i, j, k, this.getPixel(i, j, k) - ptr2.getPixel(i, j, k));
//                }
//            }
//        }
//        ptr2 = new IntImage3D(ptr1);
//        ptr2 = ptr2.binarisation(1, 255);
//        ptr1 = new IntImage3D(ptr2);
//        if (debug) {
//            IJ.log("Running Reconstruction ...");
//        }
//        ptr1 = ptr1.binaryReconstruct(ptr3);
//        for (int k = 0; k < sizez; k++) {
//            for (int i = 0; i < sizex; i++) {
//                for (int j = 0; j < sizex; j++) {
//                    ptr3.putPixel(i, j, k,
//                            ptr2.getPixel(i, j, k) - ptr1.getPixel(i, j, k));
//                }
//            }
//        }

        return ptr3;
    }

    /**
     * Number of neighborhood pixels
     *
     * @param obj Value of neighborhood pixels (including itself)
     * @return Image with Number of neighborhood pixels
     */
    public IntImage3D nbNeighbor(int obj) {
        IntImage3D res = new IntImage3D(getSizex(), getSizey(), getSizez(), Image3D.BYTE);
        int nb = 0;
        for (int k = 0; k < sizez; k++) {
            for (int i = 0; i < sizex; i++) {
                for (int j = 0; j < sizex; j++) {
                    nb = 0;
                    if (getPixel(i, j, k) == obj) {
                        for (int kk = -1; kk <= 1; kk++) {
                            for (int ii = -1; ii <= 1; ii++) {
                                for (int jj = -1; jj <= 1; jj++) {
                                    if (getPixel(i + ii, j + jj, k + kk) == obj) {
                                        nb++;
                                    }
                                }
                            }
                        }
                    }
                    res.putPixel(i, j, k, nb);
                }
            }
        }

        return res;
    }

    /**
     * Description of the Method
     *
     * @param z Description of the Parameter
     * @param val Description of the Parameter
     * @return Description of the Return Value
     */
    public int nbConnexite2D(int val, int z) {
        int i;
        int j;
        IntImage3D tmp = new IntImage3D(this);

        int nb = 0;
        for (i = 0; i < sizex; i++) {
            for (j = 0; j < sizey; j++) {
                if (tmp.getPixel(i, j, z) == val) {
                    tmp.propagation2D(i, j, z, val, 1);
                    nb++;
                }
            }
        }
        return nb;
    }

    /**
     * Description of the Method
     *
     * @param abs Description of the Parameter
     * @param ord Description of the Parameter
     * @param prof Description of the Parameter
     * @param val Description of the Parameter
     * @param nval Description of the Parameter
     */
    protected void propagation2D(int abs, int ord, int prof, int val, int nval) {
        int x, y, z, i, j, k, ok, b, a;
        a = val;
        b = 100;
        z = prof;
        k = 0;

        this.putPixel(abs, ord, prof, b);

        do {
            ok = 0;
            for (y = 0; y
                    < sizey; y++) {
                for (x = 0; x < sizex; x++) {
                    if (getPixel(x, y, z) == b) {
                        for (j = -1; j <= 1; j++) {
                            for (i = -1; i <= 1; i++) {
                                if (getPixel(x + i, y + j, z + k) == a) {
                                    ok = 1;
                                    putPixel(x + i, y + j, z + k, b);
                                }
                            }
                        }
                    }
                }
            }

            for (y = sizey - 1; y >= 0; y--) {
                for (x = sizex - 1; x >= 0; x--) {
                    if (getPixel(x, y, z) == b) {
                        for (j = -1; j
                                <= 1; j++) {
                            for (i = -1; i <= 1; i++) {
                                if (getPixel(x + i, y + j, z + k) == a) {
                                    ok = 1;
                                    putPixel(x + i, y + j, z + k, b);
                                }
                            }
                        }
                    }
                }
            }

        } while (ok == 1);

        replacePixelsValue(b, nval);


    }

    /**
     * 3D Distance Map by series of dilatation (not used, use EDT instead)
     *
     * @param inverse true for bright objects
     * @param radx Radius of dilatation in x
     * @param rady Radius of dilatation in x
     * @param radz Radius of dilatation in x
     * @return DistanceMap image, value=nb of dilatation to reach the pixel
     */
//    public IntImage3D distanceMap3D(float radx, float rady, float radz, boolean inverse) {
//
//        IntImage3D dm = new IntImage3D(this);
//        IntImage3D res = new IntImage3D(sizex, sizey, sizez, type);
//        int nbpix = 1;
//        int ite = 1;
//        int fond;
//        int obj;
//        if (!inverse) {
//            fond = getMaximum();
//            obj = getMinimum();
//        } else {
//            fond = getMinimum();
//            obj = getMaximum();
//        } //int taille = getSizex() * getSizey() * getSizez();
//        while (nbpix > 0) {
//            if (this.showStatus) {
//                IJ.showStatus("3D distance map : " + ite);
//            }
//            nbpix = 0;
//            dm = (IntImage3D) dm.morpho3Dbin(obj, fond, radx, rady, radz);
//
//            for (int k = 0; k < sizez; k++) {
//                for (int i = 0; i < sizex; i++) {
//                    for (int j = 0; j < sizey; j++) {
//                        if ((dm.getPixel(i, j, k) == obj) && (res.getPixel(i, j, k) == 0)) {
//                            nbpix++;
//                            res.putPixel(i, j, k, ite);
//                        }
//                    }
//                }
//            }
//            ite++;
//        }
//        // put 0 to original pixels inside objects
//        for (int k = 0; k < sizez; k++) {
//            for (int i = 0; i < sizex; i++) {
//                for (int j = 0; j < sizey; j++) {
//                    if (this.getPixel(i, j, k) == obj) {
//                        res.putPixel(i, j, k, 0);
//                    }
//                }
//            }
//        }
//        return res;
//    }
    /**
     * Average projection
     *
     * @param val Description of the Parameter
     * @param axe Description of the Parameter
     * @param p1 Description of the Parameter
     * @param p2 Description of the Parameter
     * @return Description of the Return Value
     */
    protected IntImage3D projection(int val, int axe, int p1, int p2) {
        //IJ.log("projection "+val);
        int nx = 0;
        int ny = 0;
        int nz = 0;
        int ii = 0;
        int jj = 0;
        int kk = 0;

        if (axe == 1) {
            nx = sizex;
            ny = sizey;
            nz = sizez;
        } else {
            if (axe == 2) {
                nx = sizex;
                ny = sizez;
                nz = sizey;
            } else {
                if (axe == 3) {
                    nx = sizez;
                    ny = sizey;
                    nz = sizex;
                }
            }
        }
        RealImage3D res = new RealImage3D(nx, ny, 1);
        double count = 0;
        double sum = 0;
        int pix = 0;
        boolean bval;
        double maxi = -Double.MAX_VALUE;
        double valpix;

        for (int i = 0; i < nx; i++) {
            for (int j = 0; j < ny; j++) {
                count = 0;
                sum = 0;
                bval = false;


                for (int k = p1; k
                        <= p2; k++) {
                    if (axe == 1) {
                        ii = i;
                        jj = j;
                        kk = k;
                    } else {
                        if (axe == 2) {
                            ii = i;
                            jj = k;
                            kk = j;
                        } else {
                            if (axe == 3) {
                                ii = k;
                                jj = j;
                                kk = i;
                            }
                        }
                    }
                    pix = getPixel(ii, jj, kk);
                    //if(pix==val) IJ.log("pixval "+ii+" "+jj+" "+kk);
                    if (((val != -1) && (pix == val)) || (val == -1)) {
                        // IJ.log("pixval "+count+" "+sum);
                        bval = true;
                        count++;

                        sum += pix;


                    }
                }
                if (bval) {
                    valpix = sum / count;
                    if (valpix > maxi) {
                        maxi = valpix;
                    }
                    res.putPixel(i, j, 0, valpix);

                } else {
                    res.putPixel(i, j, 0, 0);
                }
            }
        }
        //IJ.log(""+maxi);

        // IJ.log(""+maxi);
        // transform to IntImage3D
        double dd = 255.0 / (maxi);
        res.multiplyBy((float) dd);

        return new IntImage3D(res);
    }

    /**
     * Description of the Method
     *
     * @param val Description of the Parameter
     * @return Description of the Return Value
     */
    public IntImage3D projectionZ(int val) {
        return projection(val, 1, 0, sizez);
    }

    /**
     * Description of the Method
     *
     * @param val Description of the Parameter
     * @param p1 Description of the Parameter
     * @param p2 Description of the Parameter
     * @return Description of the Return Value
     */
    public IntImage3D projectionZ(int val, int p1, int p2) {
        return projection(val, 1, p1, p2);
    }

    /**
     * Description of the Method
     *
     * @param val Description of the Parameter
     * @return Description of the Return Value
     */
    public IntImage3D projectionY(int val) {
        return projection(val, 2, 0, sizey);
    }

    /**
     * Description of the Method
     *
     * @param val Description of the Parameter
     * @return Description of the Return Value
     */
    public IntImage3D projectionX(int val) {
        return projection(val, 3, 0, sizex);
    }

    /**
     * Description of the Method
     *
     * @return Description of the Return Value
     */
    public int snr3D() {
        int snr;
        int minpix;
        int maxpix;
        minpix = getMinimum();
        maxpix = getMaximum();
        snr = maxpix - minpix;
        return snr;
    }

    /**
     * Sobel-like filtering in 3D
     *
     * @return The 3D filtered image
     */
    public IntImage3D sobelFilter() {
        IntImage3D res = new IntImage3D(sizex, sizey, sizez);
        ArrayUtil nei;
        double[] edgeX = {-1, 0, 1, -2, 0, 2, -1, 0, 1, -2, 0, 2, -4, 0, 4, -2, 0, 2, -1, 0, 1, -2, 0, 2, -1, 0, 1};
        double[] edgeY = {-1, -2, -1, 0, 0, 0, 1, 2, 1, -2, -4, -2, 0, 0, 0, 2, 4, 2, -1, -2, -1, 0, 0, 0, 1, 2, 1};
        double[] edgeZ = {-1, -2, -1, -2, -4, -2, -1, -2, -1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 2, 1, 2, 4, 2, 1, 2, 1};
        double ex;
        double ey;
        double ez;
        double edge;


        for (int k = 0; k < sizez; k++) {
            if (this.showStatus) {
                IJ.showStatus("3D Sobel : " + (int) (100 * k / sizez) + "%");
            }
            for (int j = 0; j < sizey; j++) {
                for (int i = 0; i < sizex; i++) {
                    nei = getNeighborhood3x3x3(i, j, k);
                    ex = nei.convolve(edgeX, 1.0f);
                    ey = nei.convolve(edgeY, 1.0f);
                    ez = nei.convolve(edgeZ, 1.0f);
                    edge = Math.sqrt(ex * ex + ey * ey + ez * ez);
                    if (edge > 65535) {
                        edge = 65535;
                    }
                    res.putPixel(i, j, k, (int) edge);
                }
            }
        }
        return res;
    }

    /**
     * The gradient of the image in a given direction (sobel)
     *
     * @param axis x,y, or z
     * @return the gradient image
     */
    public IntImage3D gradient(int axis) {
        IntImage3D res = new IntImage3D(sizex, sizey, sizez);
        ArrayUtil nei;
        double[] edgeX = {-1, 0, 1, -2, 0, 2, -1, 0, 1, -2, 0, 2, -4, 0, 4, -2, 0, 2, -1, 0, 1, -2, 0, 2, -1, 0, 1};
        double[] edgeY = {-1, -2, -1, 0, 0, 0, 1, 2, 1, -2, -4, -2, 0, 0, 0, 2, 4, 2, -1, -2, -1, 0, 0, 0, 1, 2, 1};
        double[] edgeZ = {-1, -2, -1, -2, -4, -2, -1, -2, -1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 2, 1, 2, 4, 2, 1, 2, 1};
        double ex;
        double ey;
        double ez;
        double edge;

        for (int k = 0; k < sizez; k++) {
            if (this.showStatus) {
                IJ.showStatus("3D Sobel : " + (int) (100 * k / sizez) + "%");
            }
            for (int j = 0; j < sizey; j++) {
                for (int i = 0; i < sizex; i++) {
                    nei = getNeighborhood3x3x3(i, j, k);
                    ex = nei.convolve(edgeX, 1.0f);
                    ey = nei.convolve(edgeY, 1.0f);
                    ez = nei.convolve(edgeZ, 1.0f);

                    if (axis == XAXIS) {
                        edge = ex;
                    } else if (axis == YAXIS) {
                        edge = ey;
                    } else if (axis == ZAXIS) {
                        edge = ez;
                    } else {
                        edge = 0;
                    }
                    if (edge > 65535) {
                        edge = 65535;
                    }
                    res.putPixel(i, j, k, (int) edge);
                }
            }
        }
        return res;


    }

    /**
     * compute the hessian derivative (to validate)
     *
     * @param value (0=energy 1=coherence)
     * @return the hessian image
     */
    public RealImage3D computeHessianDerivative(int value) {
        RealImage3D res = new RealImage3D(getSizex(), getSizey(), getSizez());
        IntImage3D gx = this.gradient(XAXIS);
        IntImage3D gy = this.gradient(YAXIS);
        IntImage3D gz = this.gradient(ZAXIS);
        IntImage3D gxx = gx.gradient(XAXIS);
        IntImage3D gxy = gx.gradient(YAXIS);
        IntImage3D gxz = gx.gradient(ZAXIS);
        IntImage3D gyy = gy.gradient(YAXIS);
        IntImage3D gyz = gy.gradient(ZAXIS);
        IntImage3D gzz = gz.gradient(ZAXIS);
        Matrix mat = new Matrix(3, 3);
        EigenvalueDecomposition eigen;

        double[] lambda = new double[3];
        double l1, l2, l3, lmin, lmax;
        double C, E;
        int pgxx, pgxy, pgxz, pgyy, pgyz, pgzz;

        for (int z = 0; z < getSizez(); z++) {
            for (int y = 0; y < getSizey(); y++) {
                for (int x = 0; x < getSizex(); x++) {
                    pgxx = gxx.getPixel(x, y, z);
                    pgxy = gxy.getPixel(x, y, z);
                    pgxz = gxz.getPixel(x, y, z);
                    pgyy = gyy.getPixel(x, y, z);
                    pgyz = gyz.getPixel(x, y, z);
                    pgzz = gzz.getPixel(x, y, z);
                    mat.set(0, 0, pgxx);
                    mat.set(0, 1, pgxy);
                    mat.set(0, 2, pgxz);
                    mat.set(1, 0, pgxy);
                    mat.set(1, 1, pgyy);
                    mat.set(1, 2, pgyz);
                    mat.set(2, 0, pgxz);
                    mat.set(2, 1, pgyz);
                    mat.set(2, 2, pgzz);

                    eigen = new EigenvalueDecomposition(mat);
                    lambda = eigen.getRealEigenvalues();
                    l1 = lambda[0];
                    l2 = lambda[1];
                    l3 = lambda[2];
                    // energy L1+L2+L3
                    E = l1 + l2 + l3;
                    // coherence (Lmax-Lmin)/(Lmax+Lmin);
                    lmin = Math.min(l1, Math.min(l2, l3));
                    lmax = Math.max(l1, Math.max(l2, l3));
                    C = (lmax - lmin) / (lmax + lmin);


                    if (value == 0) {
                        res.putPixel(x, y, z, E);


                    } else {
                        res.putPixel(x, y, z, C);


                    }
                }
            }
        }
        return res;


    }

    /**
     * Fill holes for a binary image
     *
     * @param background background level
     * @param object gray level of object
     * @return image remplie
     */
    public IntImage3D fillHoles3D(int background, int object) {
        debug = false;
        IntImage3D res = new IntImage3D(this);
        // remplissage du fond


        int bg = 0;


        while ((bg == background) || (bg == object)) {
            bg++;
        }
        if (debug) {
            System.out.println("bg=" + bg);
        } // on suppose que le (0,0,0) est le fond
        res.propagation6(0, 0, 0, background, bg);


        if (debug) {
            System.out.println("propa done");
        }

        /*
         * for (z = 0; z < sizez; z++) { for (y = 0; y < sizey; y++) { for (x =
         * 0; x < sizex; x++) { if (res.getPixel(x, y, z) == background) {
         * res.putPixel(x, y, z, object); } } } }
         */
        res.replacePixelsValue(background, object);
        res.replacePixelsValue(bg, background);

        return res;

    }
}
