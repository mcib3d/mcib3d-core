package mcib3d.image3d.legacy;

import mcib3d.geom.GeomTransform3D;
import mcib3d.geom.Voxel3D;
import mcib3d.geom.Vector3D;
import ij.*;
import ij.process.*;
import java.util.concurrent.atomic.AtomicInteger;
import mcib3d.image3d.processing.FastFilters3D;
import mcib3d.utils.*;

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
 * Description of the Class
 *
 * @author cedric @created 6 mai 2004
 */
public class RealImage3D extends Image3D {

    /**
     * Description of the Field
     */
    private boolean debug = false;
    //private float[][] img;
    private int sizexy;

    /**
     * constructeur d'un volume vide (noir)
     *
     * @param sizex taille du volume en x
     * @param sizey taille du volume en y
     * @param sizez taille du volume en z
     */
    public RealImage3D(int sizex, int sizey, int sizez) {
        super(sizex, sizey, sizez, Image3D.FLOAT);
        sizexy = sizex * sizey;
        for (int i = 0; i < sizez; i++) {
            IJstack.addSlice("", new float[sizexy]);
        }
        //img = new float[sizex * sizey][sizez];

    }

    /**
     * Constructor for the RealImage3D object
     *
     * @param sizex Description of the Parameter
     * @param sizey Description of the Parameter
     * @param sizez Description of the Parameter
     * @param data Description of the Parameter
     */
    public RealImage3D(int sizex, int sizey, int sizez, float[][] data) {
        super(sizex, sizey, sizez, Image3D.FLOAT);
        //img = data;
        sizexy = sizex * sizey;
        for (int i = 0; i < sizez; i++) {
            IJstack.addSlice("", data[i]);
        }
    }

    /**
     * constructeur d'un volume a partir d'un IJstack
     *
     * @param pile IJstack en entree
     */
    public RealImage3D(ImageStack pile) {
        super(pile);
        /*
         * super(pile.getProcessor(1).getWidth(),
         * pile.getProcessor(1).getHeight(), pile.getSize(), Image3D.FLOAT);
         * sizexy = sizex * sizey; for (int i = 1; i <= sizez; i++) {
         * ImageProcessor ip = pile.getProcessor(i); ip = ip.convertToFloat();
         * IJstack.addSlice(pile.getSliceLabel(i), ip.getPixels()); }
         * //load(pile); //fastLoad(pile);
         */
    }
    
    public RealImage3D(ImagePlus plus){
        super(plus.getStack());
    }

    /**
     * Constructor for the RealImage3D object
     *
     * @param ip Description of the Parameter
     */
    public RealImage3D(ImageProcessor ip) {
        super(ip.getWidth(), ip.getHeight(), 1, Image3D.FLOAT);
        sizexy = sizex * sizey;
        ip = ip.convertToFloat();
        IJstack.addSlice("", ip);
    }

    /**
     * Constructor copying data from another RealImage3D
     *
     * @param img2copy RealImage3D to copy
     */
    public RealImage3D(Image3D img2copy) {
        super(img2copy.sizex, img2copy.sizey, img2copy.sizez, Image3D.FLOAT);
        //copyImage(img2copy.getData());
        sizexy = sizex * sizey;
        for (int i = 1; i <= sizez; i++) {
            ImageProcessor ip = img2copy.getStack().getProcessor(i);
            ip = ip.convertToFloat();
            IJstack.addSlice(img2copy.getStack().getSliceLabel(i), ip.getPixels());
        }
    }

    /**
     * Gets the image as a float array
     *
     * @param sx The new sizes value
     * @param sy The new sizes value
     * @param sz The new sizes value
     */
    //public float[][] getData() {
    //	return img;
    //}
    /**
     * Sets the image as the given float array
     *
     * @param sx The new sizes value
     * @param sy The new sizes value
     * @param sz The new sizes value
     */
    //public void setImage(float[][] data, int sx, int sy, int sz) {
    //	sizex = sx;
    //	sizey = sy;
    //	sizez = sz;
    //	img = data;
    //	sizexy = sizex * sizey;
    //}
    /**
     * Sets the image as a copy of the given float array
     *
     * @param sx The new sizes value
     * @param sy The new sizes value
     * @param sz The new sizes value
     */
    //public void copyImage(float[][] data) {
    //	img = new float[data.length][data[0].length];
    //	for (int i = 0; i < img.length; i++) {
    //		for (int j = 0; j < img[0].length; j++) {
    //			img[i][j] = data[i][j];
    //		}
    //	}
    //sizexy = sizex * sizey;
    //}
    /**
     * Sets the sizes attribute of the RealImage3D object
     *
     * @param sx The new sizes value
     * @param sy The new sizes value
     * @param sz The new sizes value
     */
    public void setSizes(int sx, int sy, int sz) {
        sizex = sx;
        sizey = sy;
        sizez = sz;
        sizexy = sizex * sizey;
    }

    /**
     * Gets the pixel value at position (x,y,z) as a double
     *
     * @param x coordinate on X axis
     * @param y coordinate on Y axis
     * @param z coordinate on Z axis
     * @return The pixel value
     */
    public float getPix(int x, int y, int z) {
        return getPixel(x, y, z);
    }

    /**
     * Gets the pixel value at position (x,y,z)
     *
     * @param x coordinate on X axis
     * @param y coordinate on Y axis
     * @param z coordinate on Z axis
     * @return The pixel value
     */
    public float getPixel(int x, int y, int z) {
        if (x < 0 || x >= sizex || y < 0 || y >= sizey || z < 0 || z >= sizez) {
            return getMean();
        }
        return ((float[]) IJstack.getPixels(z + 1))[x + y * sizex];
        //return getProcessor(z + 1).getPixelValue(x, y);
    }

    /**
     * Gets the trilinear interpolated pixel value at position (x,y,z) as a
     * double
     *
     * @param x coordinate on X axis
     * @param y coordinate on Y axis
     * @param z coordinate on Z axis
     * @return The pixel value
     */
    public float getPix(float x, float y, float z) {
        return getPixel(x, y, z);
    }

    /**
     * Gets the trilinear interpolated pixel value at position (x,y,z)
     *
     * @param x coordinate on X axis
     * @param y coordinate on Y axis
     * @param z coordinate on Z axis
     * @return The pixel value
     */
    public float getPixel(float x, float y, float z) {
        if (x < 0.0F || x >= (float) sizex || y < 0.0f || y >= (float) sizey || z < 0.0F || z >= (float) sizez) {
            return getMean();
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
        float xy1z = getPixel(xbase, ybase + 1, zbase);
        float upperAvplane = xy1z + xFraction * (x1y1z - xy1z);
        float lowerAvplane = xyz + xFraction * (x1yz - xyz);
        float plane = lowerAvplane + yFraction * (upperAvplane - lowerAvplane);
        if (sizez == 1) {
            return plane;
        }

        float x1y1z1 = getPixel(xbase + 1, ybase + 1, zbase + 1);
        float x1yz1 = getPixel(xbase + 1, ybase, zbase + 1);
        float xy1z1 = getPixel(xbase, ybase + 1, zbase + 1);
        float xyz1 = getPixel(xbase, ybase, zbase + 1);
        float upperAvplane1 = xy1z1 + xFraction * (x1y1z1 - xy1z1);
        float lowerAvplane1 = xyz1 + xFraction * (x1yz1 - xyz1);
        float plane1 = lowerAvplane1 + yFraction * (upperAvplane1 - lowerAvplane1);
        return plane + zFraction * (plane1 - plane);
    }

    /**
     *
     * @param x
     * @param y
     * @param z
     * @return
     */
    public float getPixel(double x, double y, double z) {
        if (x < 0.0F || x >= (double) sizex || y < 0.0f || y >= (double) sizey || z < 0.0F || z >= (double) sizez) {
            return getMean();
        }
        int xbase = (int) x;
        int ybase = (int) y;
        int zbase = (int) z;
        float xFraction = (float) x - xbase;
        float yFraction = (float) y - ybase;
        float zFraction = (float) z - zbase;
        float xyz = getPixel(xbase, ybase, zbase);
        float x1yz = getPixel(xbase + 1, ybase, zbase);
        float x1y1z = getPixel(xbase + 1, ybase + 1, zbase);
        float xy1z = getPixel(xbase, ybase + 1, zbase);
        float upperAvplane = xy1z + xFraction * (x1y1z - xy1z);
        float lowerAvplane = xyz + xFraction * (x1yz - xyz);
        float plane = lowerAvplane + yFraction * (upperAvplane - lowerAvplane);
        if (sizez == 1) {
            return plane;
        }

        float x1y1z1 = getPixel(xbase + 1, ybase + 1, zbase + 1);
        float x1yz1 = getPixel(xbase + 1, ybase, zbase + 1);
        float xy1z1 = getPixel(xbase, ybase + 1, zbase + 1);
        float xyz1 = getPixel(xbase, ybase, zbase + 1);
        float upperAvplane1 = xy1z1 + xFraction * (x1y1z1 - xy1z1);
        float lowerAvplane1 = xyz1 + xFraction * (x1yz1 - xyz1);
        float plane1 = lowerAvplane1 + yFraction * (upperAvplane1 - lowerAvplane1);
        return plane + zFraction * (plane1 - plane);
    }

    /**
     * Sets the pixel value at position (x,y,z) as a double
     *
     * @param x coordinate on X axis
     * @param y coordinate on Y axis
     * @param z coordinate on Z axis
     * @param value The pixel value
     */
    public void setPix(int x, int y, int z, double value) {
        putPixel(x, y, z, (float) value);
    }

    /**
     * Sets the pixel value at position (x,y,z)
     *
     * @param x coordinate on X axis
     * @param y coordinate on Y axis
     * @param z coordinate on Z axis
     * @param value The pixel value
     */
    public void putPixel(int x, int y, int z, float value) {
        if (x >= 0 && x < sizex && y >= 0 && y < sizey && z >= 0 && z < sizez) {
            //img[x + y * sizex][z] = value;
            ((float[]) IJstack.getPixels(z + 1))[x + y * sizex] = value;
            //java.lang.Object tmp = getPixels(z + 1);
            //if (!(tmp instanceof float[])) {
            //	IJ.write(tmp.toString());
            //}
            //getProcessor(z + 1).putPixelValue(x, y, value);
            maxPixel = null;
            minPixel = null;
            meanValue = Float.NaN;
            stDev = Float.NaN;
        }
    }

    /**
     * Description of the Method
     *
     * @param x Description of the Parameter
     * @param y Description of the Parameter
     * @param z Description of the Parameter
     * @param value Description of the Parameter
     */
    public void putPixel(int x, int y, int z, double value) {
        putPixel(x, y, z, (float) value);
    }

    /**
     * Gets the IJstack attribute of the RealImage3D object
     *
     * @return The IJstack value
     */
    public ImageStack getStack() {
        return IJstack;
    }

    /**
     * donne le maximum de l'image3D
     *
     * @return la valeur maximum de l'image3D
     */
    public double getMaximum() {
        return this.getFloatMaximum();
    }

    /**
     * donne le minimum de l'image3D
     *
     * @return la valeur minimum de l'image3D
     */
    public double getMinimum() {
        return this.getFloatMinimum();
    }

    /**
     * Gets the pow2Img attribute of the RealImage3D object
     *
     * @return The pow2Img value
     */
    public RealImage3D getPow2Img() {
        return getPow2Img(sizex, sizey, sizez);
    }

    /**
     * Gets the pow2Img attribute of the RealImage3D object
     *
     * @param sizexmax Description of the Parameter
     * @param sizeymax Description of the Parameter
     * @param sizezmax Description of the Parameter
     * @return The pow2Img value
     */
    public RealImage3D getPow2Img(int sizexmax, int sizeymax, int sizezmax) {
        if (sizexmax > sizex) {
            sizexmax = sizex;
        }
        if (sizeymax > sizey) {
            sizeymax = sizey;
        }
        if (sizezmax > sizez) {
            sizezmax = sizez;
        }

        int x;
        int y;
        int z;

        for (x = 2; x < sizexmax; x *= 2) {
            ;
        }
        for (y = 2; y < sizeymax; y *= 2) {
            ;
        }
        for (z = 2; z < sizezmax; z *= 2) {
            ;
        }

        if (x > sizexmax) {
            x /= 2;
        }
        if (y > sizeymax) {
            y /= 2;
        }
        if (z > sizezmax) {
            z /= 2;
        }

        return this.centerCrop(x, y, z);
    }

    /**
     * Création d'une image des maxima locaux dans un voisiange donne
     *
     * @param voisx taille voisiange en x
     * @param voisy taille voisiange en y
     * @param voisz taille voisiange en z
     * @param keep
     * @return Image des maxima locaux
     */
    public RealImage3D createLocalMaximaImage(float voisx, float voisy, float voisz, boolean keep) {
        RealImage3D maxima = new RealImage3D(sizex, sizey, sizez);
        //parcours de l'image3D
        int vx = (int) Math.ceil(voisx);
        int vy = (int) Math.ceil(voisy);
        int vz = (int) Math.ceil(voisz);
        for (int k = vz; k < sizez - vz; k++) {
            IJ.showStatus("3D Max Local : " + (int) (100 * k / sizez) + "%");
            for (int j = vy; j < sizey - vy; j++) {
                for (int i = vx; i < sizex - vx; i++) {
                    if (isLocalMaximum(i, j, k, voisx, voisy, voisz)) {
                        if (!keep) {
                            maxima.putPixel(i, j, k, 255.0);
                        } else {
                            maxima.putPixel(i, j, k, this.getPixel(i, j, k));
                        }
                    }
                }
            }
        }
        return maxima;
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
     * binarisation de l'image
     *
     * @param seuilb Description of the Parameter
     * @param seuilh Description of the Parameter
     * @return une Image3D contenant l'image binarisee
     */
    public IntImage3D binarisation(int seuilb, int seuilh) {
        IntImage3D bin = new IntImage3D(sizex, sizey, sizez);
        float pix;
        for (int k = 0; k < sizez; k++) {
            for (int j = 0; j < sizey; j++) {
                for (int i = 0; i < sizex; i++) {
                    pix = this.getPixel(i, j, k);
                    if ((pix < seuilb) || (pix > seuilh)) {
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
     * donne l'histogramme de l'image3D 32 bits en decoupage de 65 536 bins //
     * attention pas de lien direct entre case et valeur pixel
     *
     * @return l'histogramme
     */
    public HistogramUtil getHistogram() {
        double pix;
        double maxval = getFloatMaximum();
        double minval = getFloatMinimum();
        double dx = (maxval - minval) / 65535;
        int idx;
        double[] tab = new double[65536];
        for (int k = 0; k < sizez; k++) {
            for (int j = 0; j < sizey; j++) {
                for (int i = 0; i < sizex; i++) {
                    pix = this.getPixel(i, j, k);
                    idx = (int) ((pix - minval) / dx);
                    tab[idx]++;
                }
            }
        }
        return new HistogramUtil(tab);
    }

    /**
     * Adds a feature to the Image attribute of the RealImage3D object
     *
     * @param img The feature to be added to the Image attribute
     * @param s The feature to be added to the Image attribute
     * @param t The feature to be added to the Image attribute
     * @return Description of the Return Value
     */
    public Image3D addImage(Image3D img, float s, float t) {
        RealImage3D add = new RealImage3D(sizex, sizey, sizez);
        for (int k = 0; k < sizez; k++) {
            for (int j = 0; j < sizey; j++) {
                for (int i = 0; i < sizex; i++) {
                    add.putPixel(i, j, k, (s * getPixel(i, j, k) + t * (float) img.getPix(i, j, k)));
                }
            }
        }
        return add;
    }

    /**
     * 3D filter using threads
     *
     * @param out
     * @param radx Radius of mean filter in x
     * @param rady Radius of mean filter in y
     * @param radz Radius of mean filter in z
     * @param zmin
     * @param filter
     * @param zmax
     */
    public void filterGeneric(RealImage3D out, float radx, float rady, float radz, int zmin, int zmax, int filter) {
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
        double value;
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
     * Adaptive Filter in 3D (expermimental) Take 7 neighborhood, compute mean
     * and std dev Assign mean of neighborhood with lowest std dev
     *
     * @param radx
     * @param radz
     * @param rady
     * @return 3D filtered image
     */
    public RealImage3D adaptiveFilter(float radx, float rady, float radz, int nbcpus) {
        RealImage3D adaptimg = new RealImage3D(sizex, sizey, sizez);
        final RealImage3D adaptimg2 = adaptimg;

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
                                mins = Double.MAX_VALUE;
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
     * Adds a feature to the Image attribute of the RealImage3D object
     *
     * @param img The feature to be added to the Image attribute
     * @param t The feature to be added to the Image attribute
     * @return Description of the Return Value
     */
    public Image3D addImage(RealImage3D img, float t) {
        return addImage(img, 1.0f, t);
    }

    /**
     * adds a RealImage3D to this one
     *
     * @param img RealImage3d to add
     * @return this image3d to which an other image3d has been added
     */
    public RealImage3D add(RealImage3D img) {
        for (int k = 0; k < sizez; k++) {
            for (int j = 0; j < sizey; j++) {
                for (int i = 0; i < sizex; i++) {
                    this.putPixel(i, j, k, this.getPixel(i, j, k) + img.getPixel(i, j, k));
                }
            }
        }
        return this;
    }

    /**
     * adds a transformed RealImage3D to this one
     *
     * @param img RealImage3d to add
     * @param transform 3D transform to apply to the image given [tx ty tz rx ry
     * rz]
     * @return this image3d to which an other image3d has been added
     */
    public RealImage3D add(RealImage3D img, ArrayUtil transform) {
        GeomTransform3D transf = new GeomTransform3D();
        transf.setTranslation(transform.getValue(0), transform.getValue(1), transform.getValue(2));
        transf.setRotationDegrees(transform.getValue(3), transform.getValue(4), transform.getValue(5));
        return add(img, transf);
    }

    /**
     * adds a transformed RealImage3D to this one
     *
     * @param img RealImage3d to add
     * @param transf 3D transform to apply to the image given
     * @return this image3d to which an other image3d has been added
     */
    public RealImage3D add(RealImage3D img, GeomTransform3D transf) {
        transf.invert();
        for (int k = 0; k < sizez; k++) {
            for (int j = 0; j < sizey; j++) {
                for (int i = 0; i < sizex; i++) {
                    this.putPixel(i, j, k, this.getPixel(i, j, k) + img.getPixelTransformedI(i, j, k, transf));
                }
            }
        }
        return this;
    }

    /**
     * divides all pixels by a value
     *
     * @param div value by which dividing all pixels
     * @return the resulting image (image definitively modified)
     */
    public RealImage3D divideBy(float div) {
        for (int k = 0; k < sizez; k++) {
            for (int j = 0; j < sizey; j++) {
                for (int i = 0; i < sizex; i++) {
                    this.putPixel(i, j, k, this.getPixel(i, j, k) / div);
                }
            }
        }
        return this;
    }

    /**
     * multiplies all pixels by a given value
     *
     * @param mult value by which multiplying all pixels
     * @return the resulting image (image definitively modified)
     */
    public RealImage3D multiplyBy(float mult) {
        for (int k = 0; k < sizez; k++) {
            for (int j = 0; j < sizey; j++) {
                for (int i = 0; i < sizex; i++) {
                    this.putPixel(i, j, k, this.getPixel(i, j, k) * mult);
                }
            }
        }
        return this;
    }

    /**
     *
     * @param mult
     * @return
     */
    public RealImage3D multiplyBy(Image3D mult) {
        for (int k = 0; k < sizez; k++) {
            for (int j = 0; j < sizey; j++) {
                for (int i = 0; i < sizex; i++) {
                    this.putPixel(i, j, k, this.getPixel(i, j, k) * mult.getPix(i, j, k));
                }
            }
        }
        return this;
    }

    /**
     * adds a given value to all pixels
     *
     * @param value value to add to all pixels
     * @return the resulting image (image definitively modified)
     */
    public RealImage3D addValue(float value) {
        for (int k = 0; k < sizez; k++) {
            for (int j = 0; j < sizey; j++) {
                for (int i = 0; i < sizex; i++) {
                    this.putPixel(i, j, k, this.getPixel(i, j, k) + value);
                }
            }
        }
        return this;
    }

    /**
     * applique une transformation 3D a une image (inversion de la matrice
     * realisee pour trouver le pixel (i,j,k) dans l'image originale)
     *
     * @param trans Description of the Parameter
     * @return Description of the Return Value
     */
    public RealImage3D applyTransform(GeomTransform3D trans) {
        RealImage3D result = new RealImage3D(this.sizex, this.sizey, this.sizez);
        trans.invert();
        for (int k = 0; k < sizez; k++) {
            for (int j = 0; j < sizey; j++) {
                for (int i = 0; i < sizex; i++) {
                    result.putPixel(i, j, k, getPixelTransformedI(i, j, k, trans));
                }
            }
        }

        return result;
    }

    /**
     * Description of the Method
     *
     * @param trans Description of the Parameter
     * @return Description of the Return Value
     */
    public RealImage3D autoApplyTransform(GeomTransform3D trans) {
        RealImage3D result = this.applyTransform(trans);
        for (int k = 0; k < sizez; k++) {
            for (int j = 0; j < sizey; j++) {
                for (int i = 0; i < sizex; i++) {
                    this.putPixel(i, j, k, result.getPixel(i, j, k));
                }
            }
        }
        return this;
    }

    /**
     * Gets the pixel corresponding to the pixel given if the transform is
     * applied (inversion of matrice)
     *
     * @param pixel Description of the Parameter
     * @param trans Description of the Parameter
     * @return The transformPixel value
     */
    public float getTransformPixel(Voxel3D pixel, GeomTransform3D trans) {
        trans.invert();
        return this.getPixelTransformedI(pixel, trans);
    }

    /**
     * Gets the pixel corresponding to the direct application of the transform
     * given
     *
     * @param pixel coordinates of the original pixel
     * @param inverttrans 3D transform inverted
     * @return The pixel value via transformation
     */
    public float getPixelTransformedI(Voxel3D pixel, GeomTransform3D inverttrans) {
        return this.getPixelTransformedI(pixel.getX(), pixel.getY(), pixel.getZ(), inverttrans);
    }

    /**
     * Gets the pixel corresponding to the direct application of the transform
     * given
     *
     * @param X coordinate of original pixel on X Axis
     * @param Y coordinate of original pixel on Y Axis
     * @param Z coordinate of original pixel on Z Axis
     * @param inverttrans 3D transform inverted
     * @return The pixel value via transformation
     */
    public float getPixelTransformedI(double X, double Y, double Z, GeomTransform3D inverttrans) {
        if (inverttrans.isIdentity()) {
            return this.getPixel((float) X, (float) Y, (float) Z);
        }
        double x = ((double) X) - centerx;
        double y = ((double) Y) - centery;
        double z = ((double) Z) - centerz;

        double xx = inverttrans.getValue(0, 0) * x + inverttrans.getValue(0, 1) * y + inverttrans.getValue(0, 2) * z + inverttrans.getValue(0, 3);
        double yy = inverttrans.getValue(1, 0) * x + inverttrans.getValue(1, 1) * y + inverttrans.getValue(1, 2) * z + inverttrans.getValue(1, 3);
        xx += centerx;
        yy += centery;
        if (sizez == 1) {
            return this.getPixel(xx, yy, 0);
        }

        double zz = inverttrans.getValue(2, 0) * x + inverttrans.getValue(2, 1) * y + inverttrans.getValue(2, 2) * z + inverttrans.getValue(2, 3);
        zz += centerz;

        float pixel = this.getPixel(xx, yy, zz);

        return pixel;
    }

    /**
     * Description of the Method
     *
     * @param newsizex Description of the Parameter
     * @param newsizey Description of the Parameter
     * @param newsizez Description of the Parameter
     * @return Description of the Return Value
     */
    public RealImage3D centerCrop(int newsizex, int newsizey, int newsizez) {
        RealImage3D result = new RealImage3D(newsizex, newsizey, newsizez);
        int xmin = (sizex - newsizex) / 2;
        int ymin = (sizey - newsizey) / 2;
        int zmin = (sizez - newsizez) / 2;

        for (int k = 0; k < newsizez; k++) {
            for (int j = 0; j < newsizey; j++) {
                for (int i = 0; i < newsizex; i++) {
                    result.putPixel(i, j, k, this.getPixel(i + xmin, j + ymin, k + zmin));
                }
            }
        }

        return result;
    }

    /**
     * Description of the Method
     *
     * @param newcenterx Description of the Parameter
     * @param newcentery Description of the Parameter
     * @param newcenterz Description of the Parameter
     * @param newsizex Description of the Parameter
     * @param newsizey Description of the Parameter
     * @param newsizez Description of the Parameter
     * @return Description of the Return Value
     */
    @Override
    public RealImage3D crop(int newcenterx, int newcentery, int newcenterz, int newsizex, int newsizey, int newsizez) {
        RealImage3D result = new RealImage3D(newsizex, newsizey, newsizez);
        int xmin = newcenterx - ((newsizex - 1) / 2);
        int ymin = newcentery - ((newsizey - 1) / 2);
        int zmin = newcenterz - ((newsizez - 1) / 2);

        for (int k = 0; k < newsizez; k++) {
            for (int j = 0; j < newsizey; j++) {
                for (int i = 0; i < newsizex; i++) {
                    result.putPixel(i, j, k, this.getPixel(i + xmin, j + ymin, k + zmin));
                }
            }
        }

        return result;
    }

    /**
     * Description of the Method
     *
     * @return Description of the Return Value
     */
    public RealImage3D resetImage() {
        for (int k = 0; k < sizez; k++) {
            for (int j = 0; j < sizey; j++) {
                for (int i = 0; i < sizex; i++) {
                    this.putPixel(i, j, k, 0);
                }
            }
        }
        return this;
    }

    /**
     * filtre le volume grace a un filtre median 3D
     *
     * @param voisx rayon du voisinage en x
     * @param voisy rayon du voisinage en y
     * @param voisz rayon du voisinage en z
     * @return Image3D contenant le volume filtre par un filtre median
     */
    public Image3D medianFilter(int voisx, int voisy, int voisz) {
        RealImage3D medianeimg = new RealImage3D(sizex, sizey, sizez);
        for (int k = 0; k < sizez; k++) {
            for (int j = 0; j < sizey; j++) {
                for (int i = 0; i < sizex; i++) {
                    ArrayUtil tab = this.getNeighborhoodSphere(i, j, k, voisx, voisy, voisz);
                    medianeimg.putPixel(i, j, k, (int) tab.median());
                }
            }
        }

        return medianeimg;
    }

    /**
     * filtre le volume grace a un filtre maximum 3D
     *
     * @param voisx rayon du voisinage en x
     * @param voisy rayon du voisinage en y
     * @param voisz rayon du voisinage en z
     * @return Image3D contenant le volume filtre par un filtre median
     */
    public RealImage3D maximumFilter(int voisx, int voisy, int voisz) {
        RealImage3D maximg = new RealImage3D(sizex, sizey, sizez);
        for (int k = 0; k < sizez; k++) {
            for (int j = 0; j < sizey; j++) {
                for (int i = 0; i < sizex; i++) {
                    ArrayUtil tab = this.getNeighborhoodSphere(i, j, k, voisx, voisy, voisz);
                    maximg.putPixel(i, j, k, (int) tab.getMaximum());
                }
            }
        }

        return maximg;
    }

    /**
     * filtre le volume grace a un filtre minimum 3D
     *
     * @param voisx rayon du voisinage en x
     * @param voisy rayon du voisinage en y
     * @param voisz rayon du voisinage en z
     * @return Image3D contenant le volume filtre par un filtre median
     */
    public Image3D minimumFilter(int voisx, int voisy, int voisz) {
        RealImage3D minimg = new RealImage3D(sizex, sizey, sizez);
        for (int k = 0; k < sizez; k++) {
            for (int j = 0; j < sizey; j++) {
                for (int i = 0; i < sizex; i++) {
                    ArrayUtil tab = this.getNeighborhoodSphere(i, j, k, voisx, voisy, voisz);
                    minimg.putPixel(i, j, k, (int) tab.getMinimum());
                }
            }
        }

        return minimg;
    }

    /**
     * filtre le volume grace a un filtre top hat
     *
     * @param voisx rayon du voisinage en x
     * @param voisy rayon du voisinage en y
     * @param voisz rayon du voisinage en z
     * @return Image3D contenant le volume filtre par un filtre top hat
     */
    public Image3D tophatFilter(int voisx, int voisy, int voisz) {
        RealImage3D min = (RealImage3D) minimumFilter(voisx, voisy, voisz);
        RealImage3D maxmin = (RealImage3D) min.maximumFilter(voisx, voisy, voisz);
        RealImage3D tophat = (RealImage3D) addImage(maxmin, -1.0f);

        return tophat;
    }

    /**
     * filtre le volume grace a un filtre laplacien non lineaire
     *
     * @param voisx Description of the Parameter
     * @param voisy Description of the Parameter
     * @param voisz Description of the Parameter
     * @return Description of the Return Value
     */
    public Image3D nonLinearLaplacianFilter(int voisx, int voisy, int voisz) {
        RealImage3D min = (RealImage3D) minimumFilter(voisx, voisy, voisz);
        RealImage3D max = (RealImage3D) maximumFilter(voisx, voisy, voisz);
        RealImage3D nll = (RealImage3D) min.addImage(max, +1.0f);
        nll = (RealImage3D) nll.addImage(this, -2.0f);

        return nll;
    }

    /**
     * base pour la morphologie mathématique binaire (dilatation)<br> Les objets
     * sont segmentes et ont donc une couleur différente du fond
     *
     * @param fond niveau de gris du fond
     * @param voisx voisinage en x
     * @param voisy voisinage en y
     * @param voisz voisinage en z
     * @return image modifiée
     */
    public Image3D dilatation3D(int fond, int voisx, int voisy, int voisz) {
        float p;
        RealImage3D morphoimg = new RealImage3D(sizex, sizey, sizez);
        for (int k = 0; k < sizez; k++) {
            for (int j = 0; j < sizey; j++) {
                for (int i = 0; i < sizex; i++) {
                    p = getPixel(i, j, k);
                    if (p != fond) {
                        morphoimg.putPixel(i, j, k, p);
                    } else {
                        ArrayUtil tab = this.getNeighborhoodSphere(i, j, k, voisx, voisy, voisz);
                        if (!tab.hasOnlyValue(fond)) {
                            morphoimg.putPixel(i, j, k, (int) tab.getMaximum());
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
     * Description of the Method
     *
     * @param fond Description of the Parameter
     * @param voisx Description of the Parameter
     * @param voisy Description of the Parameter
     * @param voisz Description of the Parameter
     * @return Description of the Return Value
     */
    public Image3D erosion3D(int fond, int voisx, int voisy, int voisz) {
        float p;
        RealImage3D morphoimg = new RealImage3D(sizex, sizey, sizez);
        for (int k = 0; k < sizez; k++) {
            for (int j = 0; j < sizey; j++) {
                for (int i = 0; i < sizex; i++) {
                    p = getPixel(i, j, k);
                    if (p == fond) {
                        morphoimg.putPixel(i, j, k, fond);
                    } else {
                        ArrayUtil tab = this.getNeighborhoodSphere(i, j, k, voisx, voisy, voisz);
                        if (tab.hasValue(fond)) {
                            morphoimg.putPixel(i, j, k, fond);
                        } else {
                            morphoimg.putPixel(i, j, k, p);
                        }

                    }
                }
            }
        }

        return morphoimg;
    }

    /**
     * Description of the Method
     *
     * @param voisx Description of the Parameter
     * @param voisy Description of the Parameter
     * @param voisz Description of the Parameter
     * @param ite Description of the Parameter
     * @param fond Description of the Parameter
     * @return Description of the Return Value
     */
    public Image3D ouverture3D(int fond, int voisx, int voisy, int voisz, int ite) {
        RealImage3D morphoimg = new RealImage3D(this);
        for (int i = 0; i < ite; i++) {
            morphoimg = (RealImage3D) morphoimg.erosion3D(fond, voisx, voisy, voisz);
        }

        for (int i = 0; i < ite; i++) {
            morphoimg = (RealImage3D) morphoimg.dilatation3D(fond, voisx, voisy, voisz);
        }

        return morphoimg;
    }

    /**
     * Description of the Method
     *
     * @param voisx Description of the Parameter
     * @param voisy Description of the Parameter
     * @param voisz Description of the Parameter
     * @param ite Description of the Parameter
     * @param fond Description of the Parameter
     * @return Description of the Return Value
     */
    public Image3D fermeture3D(int fond, int voisx, int voisy, int voisz, int ite) {
        RealImage3D morphoimg = new RealImage3D(this);
        for (int i = 0; i < ite; i++) {
            morphoimg = (RealImage3D) morphoimg.dilatation3D(fond, voisx, voisy, voisz);
        }

        for (int i = 0; i < ite; i++) {
            morphoimg = (RealImage3D) morphoimg.erosion3D(fond, voisx, voisy, voisz);
        }

        return morphoimg;
    }

    /**
     * Description of the Method
     *
     * @param axis Description of the Parameter
     * @return Description of the Return Value
     */
    public RealImage3D project(int axis) {
        RealImage3D result = null;
        if (axis == ZAXIS) {
            result = new RealImage3D(sizex, sizey, 1);
            double total = 0.0;
            for (int j = 0; j < sizey; j++) {
                for (int i = 0; i < sizex; i++) {
                    total = 0.0;
                    for (int k = 0; k < sizez; k++) {
                        total += this.getPixel(i, j, k);
                    }

                    result.putPixel(i, j, 0, (float) (total / (double) sizez));
                }
            }
        } else if (axis == YAXIS) {
            result = new RealImage3D(sizex, sizez, 1);
            double total = 0;
            for (int j = 0; j < sizez; j++) {
                for (int i = 0; i < sizex; i++) {
                    total = 0.0;
                    for (int k = 0; k < sizey; k++) {
                        total += this.getPixel(i, k, j);
                    }
                    result.putPixel(i, j, 0, (float) (total / (double) sizez));
                }
            }
        } else if (axis == XAXIS) {
            result = new RealImage3D(sizey, sizez, 1);
            double total = 0;
            for (int j = 0; j < sizez; j++) {
                for (int i = 0; i < sizey; i++) {
                    total = 0.0;
                    for (int k = 0; k < sizex; k++) {
                        total += this.getPixel(k, i, j);
                    }

                    result.putPixel(i, j, 0, (float) (total / (double) sizez));
                }
            }
        }
        return result;
    }

    /**
     * Description of the Method
     *
     * @param V Description of the Parameter
     * @param C Description of the Parameter
     * @param moy Description of the Parameter
     * @return Description of the Return Value
     */
    public RealImage3D project(Vector3D V, Vector3D C, boolean moy) {
        int s = Math.max(sizex, Math.max(sizey, sizez));
        RealImage3D I = new RealImage3D(s, s, 1);

        Vector3D X = new Vector3D(0, 0, 1);
        Vector3D Y = new Vector3D(0, 1, 0);

        Vector3D XX = Y.crossProduct(V);
        Vector3D YY = XX.crossProduct(V);

        XX.normalize();
        YY.normalize();

        float xx = (float) XX.getX();
        float xy = (float) XX.getY();
        float xz = (float) XX.getZ();

        float yx = (float) YY.getX();
        float yy = (float) YY.getY();
        float yz = (float) YY.getZ();

        int sx = I.getSizex();
        int sy = I.getSizey();

        float cx = (float) C.getX();
        float cy = (float) C.getY();
        float cz = (float) C.getZ();

        float vx = (float) V.getX();
        float vy = (float) V.getY();
        float vz = (float) V.getZ();

        int vsx = this.getSizex();
        int vsy = this.getSizey();
        int vsz = this.getSizez();

        float ii;
        float jj;
        // increment de boucle sur l'image
        float k;
        int nb;
        float pix;
        float pixI;
        float vvx;
        float vvy;
        float vvz;

        boolean d1;
        boolean d2;

        for (int i = 0; i < sx; i++) {
            for (int j = 0; j < sy; j++) {
                ii = i - sx / 2;
                jj = -j + sy / 2;
                k = 0;
                nb = 0;
                pix = 0;
                d1 = true;
                d2 = true;
                while (d1 || d2) {
                    vvx = (cx + ii * xx + jj * yx + k * vx);
                    vvy = (cy + ii * xy + jj * yy + k * vy);
                    vvz = (cz + ii * xz + jj * yz + k * vz);
                    if (d1) {
                        if ((vvx < 0) || (vvx >= vsx) || (vvy < 0) || (vvy >= vsy) || (vvz < 0) || (vvz >= vsz)) {
                            d1 = false;
                        } else {
                            pix += this.getPixel(vvx, vvy, vvz);
                            nb++;
                        }
                    }

                    vvx = (cx + ii * xx + jj * yx - k * vx);
                    vvy = (cy + ii * xy + jj * yy - k * vy);
                    vvz = (cz + ii * xz + jj * yz - k * vz);
                    if (d2) {
                        if ((vvx < 0) || (vvx >= vsx) || (vvy < 0) || (vvy >= vsy) || (vvz < 0) || (vvz >= vsz)) {
                            d2 = false;
                        } else {
                            pix += this.getPixel(vvx, vvy, vvz);
                            nb++;
                        }
                    }
                    k += 0.5;
                }
                if (moy) {
                    if (nb > 0) {
                        I.putPixel(i, j, 0, pix / nb);
                    } else {
                        I.putPixel(i, j, 0, 0);
                    }
                } else {
                    I.putPixel(i, j, 0, pix);
                }
            }
        }
        return I;
    }

    /**
     * Description of the Method
     *
     * @param V Description of the Parameter
     * @param C Description of the Parameter
     * @param moy Description of the Parameter
     * @return Description of the Return Value
     */
    public FloatProcessor projection(Vector3D V, Vector3D C, boolean moy) {
        int s = Math.max(sizex, Math.max(sizey, sizez));
        FloatProcessor I = new FloatProcessor(s, s);
        int sx = I.getWidth();
        int sy = I.getHeight();

        Vector3D X = new Vector3D(0, 0, 1);
        Vector3D Y = new Vector3D(0, 1, 0);

        Vector3D XX = Y.crossProduct(V);
        Vector3D YY = XX.crossProduct(V);

        XX.normalize();
        YY.normalize();

        float xx = (float) XX.getX();
        float xy = (float) XX.getY();
        float xz = (float) XX.getZ();

        float yx = (float) YY.getX();
        float yy = (float) YY.getY();
        float yz = (float) YY.getZ();

        float cx = (float) C.getX();
        float cy = (float) C.getY();
        float cz = (float) C.getZ();

        float vx = (float) V.getX();
        float vy = (float) V.getY();
        float vz = (float) V.getZ();

        int vsx = this.getSizex();
        int vsy = this.getSizey();
        int vsz = this.getSizez();

        float ii;
        float jj;
        // increment de boucle sur l'image
        float k;
        int nb;
        float pix;
        float pixI;
        float vvx;
        float vvy;
        float vvz;

        boolean d1;
        boolean d2;

        for (int i = 0; i < sx; i++) {
            for (int j = 0; j < sy; j++) {
                ii = i - sx / 2;
                jj = -j + sy / 2;
                k = 0;
                nb = 0;
                pix = 0;
                d1 = true;
                d2 = true;
                while (d1 || d2) {
                    vvx = (cx + ii * xx + jj * yx + k * vx);
                    vvy = (cy + ii * xy + jj * yy + k * vy);
                    vvz = (cz + ii * xz + jj * yz + k * vz);
                    if (d1) {
                        if ((vvx < 0) || (vvx >= vsx) || (vvy < 0) || (vvy >= vsy) || (vvz < 0) || (vvz >= vsz)) {
                            d1 = false;
                        } else {
                            pix += this.getPixel(vvx, vvy, vvz);
                            nb++;
                        }
                    }

                    vvx = (cx + ii * xx + jj * yx - k * vx);
                    vvy = (cy + ii * xy + jj * yy - k * vy);
                    vvz = (cz + ii * xz + jj * yz - k * vz);
                    if (d2) {
                        if ((vvx < 0) || (vvx >= vsx) || (vvy < 0) || (vvy >= vsy) || (vvz < 0) || (vvz >= vsz)) {
                            d2 = false;
                        } else {
                            pix += this.getPixel(vvx, vvy, vvz);
                            nb++;
                        }
                    }
                    k += 0.5;
                }
                if (moy) {
                    if (nb > 0) {
                        I.putPixelValue(i, j, pix / nb);
                    } else {
                        I.putPixelValue(i, j, 0);
                    }
                } else {
                    I.putPixelValue(i, j, pix);
                }
            }
        }
        return I;
    }

    /**
     * Projection by interpolation
     *
     * @param W Direction of the projection
     * @param moy Mean projection or sum
     * @return Projected image
     */
    public FloatProcessor projectionInterpolated(Vector3D W, boolean moy) {
        FloatProcessor res[] = new FloatProcessor[2];
        res[0] = new FloatProcessor(sizex, sizey);
        res[1] = new FloatProcessor(sizex, sizey);
        FloatProcessor resfinal = new FloatProcessor(sizex, sizey);

        Vector3D Y = new Vector3D(0, 1, 0);

        Vector3D U = Y.crossProduct(W);
        Vector3D V = U.crossProduct(W);
        U.normalize();
        V.normalize();
        double ux = U.getX();
        double uy = U.getY();
        double uz = U.getZ();
        double vx = V.getX();
        double vy = V.getY();
        double vz = V.getZ();

        double pix;
        double alpha;
        double beta;

        double lambda;
        double px;
        double py;
        double pz;
        double ox = sizex / 2;
        double oy = sizey / 2;
        double oz = sizez / 2;
        double wx = W.getX();
        double wy = W.getY();
        double wz = W.getZ();
        double ww = wx * wx + wy * wy + wz * wz;
        double defaultValue = 0;
        int x0;
        int y0;
        double dx;
        double dy;
        double pix01;
        double pix02;
        double pix03;
        double pix04;
        double pix11;
        double pix12;
        double pix13;
        double pix14;
        for (int x = 0; x < sizex; x++) {
            for (int y = 0; y < sizey; y++) {
                for (int z = 0; z < sizez; z++) {
                    // resolution directe
                    lambda = (wx * (ox - x) + wy * (oy - y) + wz * (oz - z)) / ww;
                    px = x + lambda * wx;
                    py = y + lambda * wy;
                    pz = z + lambda * wz;
                    alpha = (px - ox) * ux + (py - oy) * uy + (pz - oz) * uz;
                    beta = (px - ox) * vx + (py - oy) * vy + (pz - oz) * vz;
                    double xx = ox + (alpha);
                    double yy = oy - (beta);
                    // interpolation inverse
                    x0 = (int) (xx);
                    y0 = (int) (yy);
                    dx = xx - x0;
                    dy = yy - y0;
                    pix = this.getPixel(x, y, z);
                    // recuperer les valeurs
                    pix01 = res[0].getPixelValue(x0, y0);
                    pix02 = res[0].getPixelValue(x0 + 1, y0);
                    pix03 = res[0].getPixelValue(x0, y0 + 1);
                    pix04 = res[0].getPixelValue(x0 + 1, y0 + 1);
                    pix11 = res[1].getPixelValue(x0, y0);
                    pix12 = res[1].getPixelValue(x0 + 1, y0);
                    pix13 = res[1].getPixelValue(x0, y0 + 1);
                    pix14 = res[1].getPixelValue(x0 + 1, y0 + 1);
                    // coeff interpolation image 0
                    res[0].putPixelValue(x0, y0, pix01 + (1 - dy) * (1 - dx));
                    res[0].putPixelValue(x0 + 1, y0, pix02 + (1 - dy) * dx);
                    res[0].putPixelValue(x0, y0 + 1, pix03 + dy * (1 - dx));
                    res[0].putPixelValue(x0 + 1, y0 + 1, pix04 + dy * dx);
                    // pix * coeff image 1
                    res[1].putPixelValue(x0, y0, pix11 + (1 - dy) * (1 - dx) * pix);
                    res[1].putPixelValue(x0 + 1, y0, pix12 + (1 - dy) * dx * pix);
                    res[1].putPixelValue(x0, y0 + 1, pix13 + dy * (1 - dx) * pix);
                    res[1].putPixelValue(x0 + 1, y0 + 1, pix14 + dy * dx * pix);
                }
            }
        }
        double res0;

        for (int x = 0; x < sizex; x++) {
            for (int y = 0; y < sizey; y++) {
                res0 = res[0].getPixelValue(x, y);
                if (res0 == 0) {
                    resfinal.putPixelValue(x, y, 0);
                } else {
                    if (moy) {
                        resfinal.putPixelValue(x, y, (double) (res[1].getPixelValue(x, y) / res0));
                    } else {
                        resfinal.putPixelValue(x, y, (double) (res[1].getPixelValue(x, y)));
                    }
                }
            }
        }
        return resfinal;
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
}
