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
    ImageHandler peaks;
    float noiseTolerance;
    ArrayList<Voxel3DComparable> maxima;

    public MaximaFinder(ImageHandler ima, float noiseTolerance) {
        img = ima;
        this.noiseTolerance = noiseTolerance;
    }

    public MaximaFinder(ImageHandler ima) {
        img = ima;
        noiseTolerance = 0;
    }

    public void computeMaximaList() {
        maxima = new ArrayList<Voxel3DComparable>();

        if (img.getType() == ImagePlus.GRAY16) {
            maxima = ((ImageInt) img).getListMaxima(1.5f, 1.5f, 1, 0, img.sizeZ);
        }
        Collections.sort(maxima);
    }

    public void drawPeaks() {
        peaks = new ImageShort("peaks", img.sizeX, img.sizeY, img.sizeZ);
        IJ.log("Finding all peaks");
        for (Voxel3DComparable V : maxima) {
            peaks.setPixel(V, (float) V.getValue());
        }
        IJ.log("Removing peaks above noise " + maxima.size());
        ArrayList<Voxel3DComparable> toKeep = new ArrayList<Voxel3DComparable>();
        for (Voxel3DComparable V : maxima) {
            if (img.getPixel(V) > 0) {
                if (V.getValue() > noiseTolerance) {
                    toKeep.add(V);
                    IJ.showStatus("processing " + V + " " + (V.getValue() - noiseTolerance));
                    Flood3D.flood3DNoiseShort26((ImageShort) img, new IntCoord3D(V.getRoundX(), V.getRoundY(), V.getRoundZ()), (short) (Math.max(1, V.getValue() - noiseTolerance)), (short) 0);
                } else {
                    //toRemove.add(V);
                }
            } else {
                //toRemove.add(V);
            }
        }
        IJ.log("Creating final peaks ");
        //maxima.removeAll(toRemove);
        IJ.log("Final peaks " + toKeep.size());
        peaks.fill(0);
        for (Voxel3DComparable V : toKeep) {
            peaks.setPixel(V, (float) V.getValue());
        }
    }

    public ImageHandler getPeaks() {
        return peaks;
    }

}
