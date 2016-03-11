/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mcib3d.image3d.processing;

import ij.IJ;
import ij.ImagePlus;
import java.util.ArrayList;
import java.util.Collections;
import mcib3d.geom.IntCoord3D;
import mcib3d.geom.Voxel3DComparable;
import mcib3d.image3d.ImageHandler;
import mcib3d.image3d.ImageInt;
import mcib3d.image3d.ImageShort;

/**
 *
 * @author thomasb
 */
public class MaximaFinder {

    ImageHandler img;
    ImageHandler peaks = null;
    float noiseTolerance;
    ArrayList<Voxel3DComparable> maxima;
    float radXY, radZ;

    public MaximaFinder(ImageHandler ima, float noiseTolerance) {
        img = ima.duplicate();
        this.noiseTolerance = noiseTolerance;
        radXY = 1.5f;
        radZ = 1.5f;
    }

    public MaximaFinder(ImageHandler ima) {
        img = ima.duplicate();
        noiseTolerance = 0;
    }

    private void computeMaximaList() {
        maxima = new ArrayList<Voxel3DComparable>();

        if (img.getType() == ImagePlus.GRAY16) {
            maxima = ((ImageInt) img).getListMaxima(1.5f, 1.5f, 1, 0, img.sizeZ);
        }
        Collections.sort(maxima);
    }

    private void computePeaks() {
        peaks = new ImageShort("peaks", img.sizeX, img.sizeY, img.sizeZ);
        IJ.log("Finding all peaks");
        computeMaximaList();
        for (Voxel3DComparable V : maxima) {
            peaks.setPixel(V, (float) V.getValue());
        }
        IJ.log(maxima.size() + " peaks found");
        IJ.log("Removing peaks below noise");
        ArrayList<Voxel3DComparable> toKeep = new ArrayList<Voxel3DComparable>();
        for (Voxel3DComparable V : maxima) {
            if (img.getPixel(V) > 0) {
                if (V.getValue() > noiseTolerance) {
                    toKeep.add(V);
                    IJ.showStatus("Processing peak" + V);
                    Flood3D.flood3DNoiseShort26((ImageShort) img, new IntCoord3D(V.getRoundX(), V.getRoundY(), V.getRoundZ()), (int) (Math.max(1, V.getValue() - noiseTolerance)), 0);
                }
            }
        }
        IJ.log(toKeep.size() + " peaks found");
        IJ.log("Creating final peaks");
        peaks.fill(0);
        for (Voxel3DComparable V : toKeep) {
            peaks.setPixel(V, (float) V.getValue());
        }
    }

    public ImageHandler getPeaks() {
        if (peaks == null) {
            computePeaks();
        }
        return peaks;
    }

    public void setImage(ImageHandler img) {
        this.img = img;
        peaks = null;
    }

    public void setNoiseTolerance(float noiseTolerance) {
        this.noiseTolerance = noiseTolerance;
        peaks = null;
    }

    public void setRadii(float rxy, float rz) {
        radXY = rxy;
        radZ = rz;
        peaks = null;
    }
}
