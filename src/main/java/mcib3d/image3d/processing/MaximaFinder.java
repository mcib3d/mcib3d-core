/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mcib3d.image3d.processing;

import ij.IJ;
import java.util.ArrayList;
import java.util.Collections;
import mcib3d.geom.IntCoord3D;
import mcib3d.geom.Voxel3DComparable;
import mcib3d.image3d.ImageHandler;
import mcib3d.image3d.ImageShort;

/**
 *
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
    protected ImageHandler peaks = null;

    /**
     * The noise tolerance around each peak
     */
    protected float noiseTolerance = 0;

    /**
     * The list of peaks
     */
    protected ArrayList<Voxel3DComparable> maxima;

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
    protected boolean show = true;

    /**
     * Constructor with default values for radii
     *
     * @param ima The raw image
     * @param noiseTolerance The noise tolerance
     */
    public MaximaFinder(ImageHandler ima, float noiseTolerance) {
        img = ima.duplicate();
        this.noiseTolerance = noiseTolerance;
    }

    /**
     * Constructor
     *
     * @param img The raw image
     * @param radXY The radius XY to find local maxima
     * @param radZ The radius Z to find local maxima
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
     * @param ima The raw image
     */
    public MaximaFinder(ImageHandler ima) {
        img = ima.duplicate();
        noiseTolerance = 0;
    }

    private void computePeaks() {
        peaks = new ImageShort("peaks", img.sizeX, img.sizeY, img.sizeZ);
        if (show) {
            IJ.log("Finding all peaks");
        }
        maxima = FastFilters3D.getListMaxima(img, radXY, radXY, radZ, nbCpus, false);
        Collections.sort(maxima);
        for (Voxel3DComparable V : maxima) {
            peaks.setPixel(V, (float) V.getValue());
        }
        if (show) {
            IJ.log(maxima.size() + " peaks found");
        }
        if (show) {
            IJ.log("Removing peaks below noise");
        }
        ArrayList<Voxel3DComparable> toKeep = new ArrayList<Voxel3DComparable>();
        for (Voxel3DComparable V : maxima) {
            if (img.getPixel(V) > 0) {
                if (V.getValue() > noiseTolerance) {
                    toKeep.add(V);
                    if (show) {
                        IJ.showStatus("Processing peak" + V);
                    }
                    Flood3D.flood3DNoise26(img, new IntCoord3D(V.getRoundX(), V.getRoundY(), V.getRoundZ()), (int) (Math.max(1, V.getValue() - noiseTolerance)), 0);
                }
            }
        }
        if (show) {
            IJ.log(toKeep.size() + " peaks found");
        }
        if (show) {
            IJ.log("Creating final peaks");
        }
        peaks.fill(0);
        for (Voxel3DComparable V : toKeep) {
            peaks.setPixel(V, (float) V.getValue());
        }
        if (show) {
            IJ.log("Maximafinder3D finished.");
        }
    }

    /**
     * Do the computation and returns the result
     * @return The image with peaks 
     */
    public ImageHandler getPeaks() {
        if (peaks == null) {
            computePeaks();
        }
        return peaks;
    }

    /**
     * A new image to process
     * @param img The image
     */
    public void setImage(ImageHandler img) {
        this.img = img;
        peaks = null;
    }

    /**
     * The noise tolerance 
     * @param noiseTolerance The noise tolerance
     */
    public void setNoiseTolerance(float noiseTolerance) {
        this.noiseTolerance = noiseTolerance;
        peaks = null;
    }

    /**
     * The radii to compute local maxima
     * @param rxy The radius XY to find local maxima
     * @param rz The radius Z to find local maxima
     */
    public void setRadii(float rxy, float rz) {
        radXY = rxy;
        radZ = rz;
        peaks = null;
    }

    /**
     * Number of Cpus
     * @param nbCpus Number of cpus
     */
    public void setNbCpus(int nbCpus) {
        this.nbCpus = nbCpus;
    }

    /**
     * Display information
     * @param show Display information
     */
    public void setShow(boolean show) {
        this.show = show;
    }
}
