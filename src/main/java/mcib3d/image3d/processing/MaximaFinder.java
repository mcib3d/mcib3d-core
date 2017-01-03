/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mcib3d.image3d.processing;

import ij.IJ;
import mcib3d.geom.IntCoord3D;
import mcib3d.geom.Voxel3D;
import mcib3d.geom.Voxel3DComparable;
import mcib3d.image3d.ImageHandler;
import mcib3d.image3d.ImageShort;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;

/**
 * @author thomasb
 */
public class MaximaFinder {

    /**
     * The raw image to analyze
     */
    protected ImageHandler img;

    /**
     * The image with the final peaks
     */
    protected ImageHandler imagePeaks = null;

    /**
     * The noise tolerance around each peak
     */
    protected float noiseTolerance = 0;

    /**
     * The list of peaks
     */
    protected ArrayList<Voxel3D> maxima;

    /**
     * The radius XY to find local maxima
     */
    protected float radXY = 1.5f;
    /**
     * The radius Z to find local maxima
     */
    protected float radZ = 1.5f;

    /**
     * The number of cpus to use, 0 = all
     */
    protected int nbCpus = 0;

    /**
     * Do we display information ?
     */
    protected boolean verbose = true;

    /**
     * Constructor with default values for radii
     *
     * @param ima            The raw image
     * @param noiseTolerance The noise tolerance
     */
    public MaximaFinder(ImageHandler ima, float noiseTolerance) {
        img = ima.duplicate();
        this.noiseTolerance = noiseTolerance;
    }

    /**
     * Constructor
     *
     * @param img            The raw image
     * @param radXY          The radius XY to find local maxima
     * @param radZ           The radius Z to find local maxima
     * @param noiseTolerance The noise tolerance
     */
    public MaximaFinder(ImageHandler img, float radXY, float radZ, float noiseTolerance) {
        this.img = img;
        this.noiseTolerance = noiseTolerance;
        this.radXY = radXY;
        this.radZ = radZ;
    }

    /**
     * Constructor with default values
     *
     * @param ima The raw image
     */
    public MaximaFinder(ImageHandler ima) {
        img = ima.duplicate();
        noiseTolerance = 0;
    }

    private void computePeaks() {
        imagePeaks = new ImageShort("peaks", img.sizeX, img.sizeY, img.sizeZ);
        if (verbose) {
            IJ.log("Finding all peaks");
        }
        ArrayList<Voxel3DComparable> maximaTmp = FastFilters3D.getListMaxima(img, radXY, radXY, radZ, nbCpus, false);
        Collections.sort(maximaTmp);
        for (Voxel3DComparable V : maximaTmp) {
            imagePeaks.setPixel(V, (float) V.getValue());
        }
        if (verbose) {
            IJ.log(maximaTmp.size() + " peaks found");
        }
        if (verbose) {
            IJ.log("Removing peaks below noise");
        }
        maxima = new ArrayList<Voxel3D>();

        int c = 1;
        int nb = maximaTmp.size();
        Date start = new Date();
        Date temp;
        for (Voxel3DComparable V : maximaTmp) {
            if (img.getPixel(V) > 0) {
                if (V.getValue() > noiseTolerance) {
                    maxima.add(V);
                    if (verbose) {
                        temp = new Date();
                        if ((temp.getTime() - start.getTime()) > 100) {
                            IJ.showStatus("Processing peak " + c + "/" + nb + " " + V);
                            start = new Date();
                        }
                        c++;
                    }
                    Flood3D.flood3DNoise26(img, new IntCoord3D(V.getRoundX(), V.getRoundY(), V.getRoundZ()), (int) (Math.max(1, V.getValue() - noiseTolerance)), 0);
                }
            }
        }

        if (verbose) {
            IJ.log(maxima.size() + " peaks found");
        }
        if (verbose) {
            IJ.log("Creating final peaks");
        }
        imagePeaks.fill(0);
        for (Voxel3D V : maxima) {
            imagePeaks.setPixel(V, (float) V.getValue());
        }
        if (verbose) {
            IJ.log("MaximaFinder3D finished.");
        }
    }

    /**
     * Do the computation and returns the result
     *
     * @return The image with peaks
     */
    public ImageHandler getImagePeaks() {
        if (imagePeaks == null) {
            computePeaks();
        }
        return imagePeaks;
    }

    public ArrayList<Voxel3D> getListPeaks() {
        if (imagePeaks == null) {
            computePeaks();
        }

        return maxima;
    }

    /**
     * A new image to process
     *
     * @param img The image
     */
    public void setImage(ImageHandler img) {
        this.img = img;
        imagePeaks = null;
    }

    /**
     * The noise tolerance
     *
     * @param noiseTolerance The noise tolerance
     */
    public void setNoiseTolerance(float noiseTolerance) {
        this.noiseTolerance = noiseTolerance;
        imagePeaks = null;
    }

    /**
     * The radii to compute local maxima
     *
     * @param rxy The radius XY to find local maxima
     * @param rz  The radius Z to find local maxima
     */
    public void setRadii(float rxy, float rz) {
        radXY = rxy;
        radZ = rz;
        imagePeaks = null;
    }

    /**
     * Number of Cpus
     *
     * @param nbCpus Number of cpus
     */
    public void setNbCpus(int nbCpus) {
        this.nbCpus = nbCpus;
    }

    /**
     * Display information
     *
     * @param show Display information
     */
    public void setVerbose(boolean show) {
        this.verbose = show;
    }

    @Deprecated
    public void setShow(boolean show) {
        this.verbose = show;
    }


}
