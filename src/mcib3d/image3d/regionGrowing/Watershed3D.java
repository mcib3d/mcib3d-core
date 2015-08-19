/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mcib3d.image3d.regionGrowing;

import ij.IJ;
import ij.ImageStack;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.TreeSet;
import mcib3d.geom.ComparatorVoxel;
import mcib3d.geom.Voxel3D;
import mcib3d.geom.Voxel3DComparable;
import mcib3d.image3d.ImageHandler;
import mcib3d.image3d.ImageInt;
import mcib3d.image3d.ImageLabeller;
import mcib3d.image3d.ImageShort;
import mcib3d.utils.ArrayUtil;

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
 * Class for 3D watershed implementation, using seeds
 *
 * @author thomas
 */
public class Watershed3D {

    ImageHandler rawImage; // raw image
    ImageHandler seedsImage; // positions of seeds
    ImageInt watershedImage = null; // watershed from seeds
    ImageInt mask;
    LinkedList<Voxel3DComparable> voxels = null; // voxels to compute watershed    
    final int NO_LABEL = 0; // watershed label
    //final int BORDER = 2; // watershed label
    final int QUEUE = 1;
    // association between neighbor regions
    protected boolean computeAssociation = false;
    ArrayList<String> associations = null;
    AllRegionsAssociation assoRegions = null;
    // list of active labels 
    protected boolean computeUpdatedLabels = false;
    ArrayList<String> updatedLabels = null;
    // get volumes for each labels (jaza)
    protected boolean computeVolumes = false;
    ArrayList<Double> volumeLabels = null;
    private int rawThreshold;
    private final int seedsThreshold;
    private boolean okseeds = false;

    private boolean fastButLessAccurate = false;

    /**
     *
     * @param image raw image
     * @param seeds seeds image
     * @param noi noise level for rw image
     * @param seth noise level for seeds
     */
    public Watershed3D(ImageHandler image, ImageHandler seeds, int noi, int seth) {
        this.rawImage = image;
        this.seedsImage = seeds;
        this.rawThreshold = noi;
        this.seedsThreshold = seth;
    }

    /**
     * Constructor for 3D Watershed
     *
     * @param image raw image
     * @param seeds seeds image
     * @param noi noise level for rw image
     * @param seth noise level for seeds
     */
    public Watershed3D(ImageStack image, ImageStack seeds, int noi, int seth) {
        this.rawImage = ImageHandler.wrap(image);
        this.seedsImage = ImageInt.wrap(seeds);
        this.rawThreshold = noi;
        this.seedsThreshold = seth;
    }

    /**
     * compute the watershed image, only for voxels with value > threshold
     *
     * @param threshold
     * @return watershed image
     */
//    public ImageInt getWatershedImage3D(int threshold) {
//        if (watershedImage == null) {
//            this.computeWatershed(threshold);
//        }
//        return watershedImage;
//    }
    /**
     * compute the watershed image
     *
     * @return watershed image
     */
//    public ImageInt getWatershedImage3D() {
//        return getWatershedImage3D(0);
//    }
//    public void updateWatershedImage3D(ImageInt wat) {
//        watershedImage = wat;
//    }
//    public void setFastButLessAccurate(boolean fastButLessAccurate) {
//        this.fastButLessAccurate = fastButLessAccurate;
//    }
//    public void computeAssociations(boolean ca) {
//        computeAssociation = ca;
//        if (!ca) {
//            associations = null;
//        }
//        if ((ca) && (associations == null)) {
//            associations = new ArrayList<String>();
//        }
//    }
//    
//    public void computeUpdatedLabels(boolean ca) {
//        computeUpdatedLabels = ca;
//        if (!ca) {
//            updatedLabels = null;
//        }
//        if ((ca) && (updatedLabels == null)) {
//            updatedLabels = new ArrayList<String>();
//        }
//    }
//    
//    public void computeVolumes(boolean b) {
//        computeVolumes = b;
//        if (!b) {
//            volumeLabels = null;
//        }
//        if ((b) && (volumeLabels == null)) {
//            volumeLabels = new ArrayList<double[]>();
//        }
//    }
//    
//    public ArrayList<String> getAssociationsAsString() {
//        return associations;
//    }
//    
//    public ArrayList<String> getUpdatedLabelsAsString() {
//        return updatedLabels;
//    }
//    
//    public ArrayList<int[]> getAssociationsAsIntegers() {
//        if ((associations == null) || (associations.isEmpty())) {
//            return null;
//        }
//        ArrayList<int[]> res = new ArrayList<int[]>(associations.size());
//        for (String S : associations) {
//            String[] sp = S.split("_");
//            int[] tab = new int[sp.length];
//            for (int i = 0; i < sp.length; i++) {
//                tab[i] = Integer.parseInt(sp[i]);
//            }
//            res.add(tab);
//        }
//        return res;
//    }
//    
//    public ArrayList<double[]> getVolumeLabels() {
//        return volumeLabels;
//    }
//    public void updateVolumeLabel(int idx, double vol) {
//        if (idx < volumeLabels.size()) {
//            volumeLabels.set(idx, vol);
//        } else {
//            volumeLabels.add(vol);
//        }
//    }
//
//    /**
//     * Compute the watershed image as next iteration from previous watershed ,
//     * only for voxels with value > threshold
//     *
//     * @param threshold
//     * @return
//     */
//    public ImageInt continueWatershed3D(int threshold) {
//        if (watershedImage == null) {
//            this.computeWatershed(threshold);
//        } else {
//            IJ.log("Continuing watershed");
//            continueWatershed(threshold);
//        }
//        return watershedImage;
//    }
    /**
     * compute the watershed image
     *
     * @return watershed image
     */
//    public ImageStack getWatershedImageStack(int threshold) {
//        return getWatershedImage3D(threshold).getImageStack();
//    }
    /**
     * compute the watershed image
     *
     * @return watershed image
     */
//    public ImageStack getWatershedImageStack() {
//        return getWatershedImage3D().getImageStack();
//    }
    /**
     * Get the raw image used
     *
     * @return raw image
     */
    public ImageHandler getRawImage() {
        return rawImage;
    }

    /**
     * Sets the raw image
     *
     * @param image
     */
    public void setRawImage(ImageHandler image) {
        this.rawImage = image;
    }

    /**
     * Get the seeds image used
     *
     * @return seeds image
     */
    public ImageHandler getSeeds() {
        return seedsImage;
    }

    /**
     * set the seeds iamge used
     *
     * @param seeds image
     */
    public void setSeeds(ImageInt seeds) {
        this.seedsImage = seeds;
    }

//    private void initWatershed() {
//        this.createArrayList();
//        if (!okseeds) {
//            IJ.log("No seeds found !");
//            return;
//        }
//        IJ.showStatus("Sorting watershed ...");
//        if (fastButLessAccurate) {
//            Collections.shuffle(voxels);
//        }
//        Collections.sort(voxels);
//    }
//    private void computeWatershed(int threshold) {
//        initWatershed();
//        //continueWatershed(threshold);
//        testWatershed(threshold, true);
//    }
//    private void continueWatershed(int threshold) {
//        int sx = rawImage.sizeX;
//        int sy = rawImage.sizeY;
//        int sz = rawImage.sizeZ;
//        int MaxIterations = (int) Math.sqrt(sx * sx + sy * sy + sz * sz);
//        //IJ.log("ite=" + MaxIterations);
//        int ite = 0;
//        boolean loop = true;
//        int nb0, nb1;
//        float Nb = voxels.size();
//        while ((loop) && (ite < MaxIterations)) {
//            IJ.showStatus("Watershed " + (ite + 1) + " (" + (int) (100 - (100.0f * voxels.size() / Nb)) + "%)");
//            //IJ.log("Watershed " + ite + " (" + voxels.size() + ")");
//            ite++;
//            nb0 = voxels.size();
//            if (fastButLessAccurate) {
//                loop = assignWatershedFastLessAccurate(threshold);
//            } else {
//                loop = assignWatershedAccurate(threshold);
//            }
//            nb1 = voxels.size();
//            // no voxels could be assigned --> stop
//            if (nb1 == nb0) {
//                loop = false;
//            }
//            //IJ.log("Nb " + nb0 + " " + nb1 + " " + loop);
//        }
//    }
//    public void testWatershed(int threshold, boolean anim) {
//        initWatershed();
//
//        //mask = watershedImage.thresholdAboveExclusive(0);
//        //mask = FastFilters3D.filterIntImage(mask, FastFilters3D.MAX, 1, 1, 1, 0, false);
////
////        int sx = rawImage.sizeX;
////        int sy = rawImage.sizeY;
////        int sz = rawImage.sizeZ;
//        //int MaxIterations = (int) Math.sqrt(sx * sx + sy * sy + sz * sz);
//        //IJ.log("ite=" + MaxIterations);
//        //int ite = 0;
//        boolean loop = true;
//        //int nb0, nb1;
//        //float Nb = voxels.size();
//        int level = (int) (voxels.get(0).getValue());
//        if (anim) {
//            watershedImage.show();
//        }
////        for (int i = 0; i < 12; i++) {
////            IJ.log(" " + (int) (voxels.get(i).getValue()));
////        }
//
//        String update = "\\Update:";
//        int ii = 0;
//        int pc = 0;
//        long t0 = System.currentTimeMillis();
//        while ((loop) && (level > threshold)) {
//
//            //nb0 = voxels.size();
//            assignWatershedAccurate2(level, ii);
//            // nb1 = voxels.size();
//            // no voxels could be assigned --> stop
////            if (nb1 == nb0) {
////                loop = false;
////            }
//
//            if (System.currentTimeMillis() - t0 > 1000) {
//                int pc2 = (int) (100.0 * ii / voxels.size());
//                IJ.log(update + "Watershed level " + (level) + " " + ii + "/" + voxels.size() + " (" + pc2 + "%)");
//                pc = pc2;
//                t0 = System.currentTimeMillis();
//                if (anim) {
//                    watershedImage.updateDisplay();
//                }
//            }
//
//            // IJ.log("Watershed " + level + " " + ii + " " + loop);
//            //ii = 0;
//            int val = (int) (voxels.get(ii).getValue());
//            while (((val == -1)) && (ii < voxels.size() - 1)) {
//                ii++;
//                val = (int) (voxels.get(ii).getValue());
//            }
//            while (((val >= level) || (val == -1)) && (ii < voxels.size() - 1)) {
//                ii++;
//                val = (int) (voxels.get(ii).getValue());
//            }
//            level = val;
//
//            if (ii == voxels.size() - 1) {
//                loop = false;
//            }
//
//            //level--;
//        }
//    }
    public ImageInt getClassicWatershed(boolean anim) {
        long step = 100;
        // gt voxels list
        createNeigList();
        long t0 = System.currentTimeMillis();
        if (anim) {
            watershedImage.show();
        }
        if (rawImage.getMin() > rawThreshold) {
            IJ.log("Setting minimum for raw image to " + rawImage.getMin());
            rawThreshold = (int) rawImage.getMin();
        }
        boolean loop = true;
        // test tree set
        ComparatorVoxel comp = new ComparatorVoxel();
        TreeSet<Voxel3DComparable> tree = new TreeSet(comp);
        TreeSet<Voxel3DComparable> tree2 = new TreeSet(comp);
        int idx = 1;
        for (Voxel3DComparable V : voxels) {
            V.setMax(idx++, 0);
            tree.add(V);
        }
        boolean newt = true;

        while (newt) {
            newt = false;
            while ((!tree.isEmpty()) && (loop)) {
                //IJ.wait((int) step);
                Voxel3DComparable V = tree.pollFirst();
                ArrayList<Voxel3D> Nei = watershedImage.getNeighborhood3x3x3ListNoCenter(V.getRoundX(), V.getRoundY(), V.getRoundZ());

                // check all labels around
                ArrayUtil tab = new ArrayUtil(Nei.size());
                int id = 0;
                double label = -1;
                for (Voxel3D N : Nei) {
                    if (N.getValue() > QUEUE) {
                        tab.addValue(id++, N.getValue());
                        // first label
                        if (label == -1) {
                            label = N.getValue();
                        } // next ones
                        else {
                            if (N.getValue() != label) {
                                label = -2;
                            }
                            break;
                        }
                    }
                }
                boolean assign = false;

                if (label > 0) {
                    watershedImage.setPixel(V, tab.getValueInt(0));
                    assign = true;
                }

                // all non labelled around are put into priority queue   
                if (assign) {
                    for (Voxel3D N : Nei) {
                        int rawN = (int) rawImage.getPixel(N);

                        // neighbor voxel not in queue yet
                        if ((N.getValue() == 0) && (rawN > rawThreshold)) {
                            watershedImage.setPixel(N, QUEUE);// was queue instead of W0
                            Voxel3DComparable Vnew = new Voxel3DComparable(N.getRoundX(), N.getRoundY(), N.getRoundZ(), rawN, V.getLabel());
                            Vnew.setMax(idx++, 0);
                            tree.add(Vnew);
                        }
                    }
                }

                if (System.currentTimeMillis() - t0 > step) {
                    IJ.log("\\Update:Voxels to process : " + Math.abs(tree.size()));
                    if (anim) {
                        watershedImage.updateDisplay();
                    }
                    t0 = System.currentTimeMillis();
                }
            }
        }
        IJ.log("\\Update:Voxels to process : " + Math.abs(tree.size()));
        // delete voxels with color QUEUE
        watershedImage.replacePixelsValue(QUEUE, 0);
        IJ.log("Watershed completed.");

        return watershedImage;
    }

    private void createNeigList() {
        voxels = new LinkedList();
        int sx = rawImage.sizeX;
        int sy = rawImage.sizeY;
        int sz = rawImage.sizeZ;

        // watershedImage
        this.watershedImage = new ImageShort("watershed", sx, sy, sz);
        okseeds = false;

        float pix;
        float se;

        // compute the labelled image (in case seeds are clustered)
        ImageLabeller labeller = new ImageLabeller();
        ImageInt seedsLabel = labeller.getLabels(seedsImage.thresholdAboveExclusive(seedsThreshold));
        // since seedsLabel starts at 1 and watershed at 2, replace values
        int max = (int) seedsLabel.getMax();
        seedsLabel.replacePixelsValue(QUEUE, max + 1);

        seedsLabel.resetStats(null);

        for (int z = 0; z < sz; z++) {
            IJ.showStatus("Processing watershed " + (z + 1));
            for (int y = 0; y < sy; y++) {
                for (int x = 0; x < sx; x++) {
                    pix = rawImage.getPixel(x, y, z);
                    se = seedsLabel.getPixel(x, y, z);
                    if (pix > rawThreshold) {
                        if (se > 0) {
                            watershedImage.setPixel(x, y, z, se);
                            okseeds = true;
                            for (Voxel3D N : watershedImage.getNeighborhood3x3x3ListNoCenter(x, y, z)) {
                                int vx = (int) N.getX();
                                int vy = (int) N.getY();
                                int vz = (int) N.getZ();
                                int raw = (int) rawImage.getPixel(vx, vy, vz);
                                if ((raw > rawThreshold) && (seedsLabel.getPixel(vx, vy, vz) == 0) && (watershedImage.getPixel(vx, vy, vz) != QUEUE)) {
                                    voxels.add(new Voxel3DComparable(vx, vy, vz, raw, se));
                                    watershedImage.setPixel(vx, vy, vz, QUEUE);
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
