/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mcib3d.image3d.regionGrowing;

import ij.IJ;
import ij.ImageStack;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
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
    ArrayList<Voxel3DComparable> voxels = null; // voxels to compute watershed    
    final int NO_LABEL = 0; // watershed label
    final int BORDER = 1; // watershed label
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
    private int seedsThreshold;
    private boolean okseeds = false;

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
    public ImageInt getWatershedImage3D(int threshold) {
        if (watershedImage == null) {
            this.computeWatershed(threshold);
        }
        return watershedImage;
    }

    /**
     * compute the watershed image
     *
     * @return watershed image
     */
    public ImageInt getWatershedImage3D() {
        return getWatershedImage3D(0);
    }

    public void updateWatershedImage3D(ImageInt wat) {
        watershedImage = wat;
    }

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
    public void updateVolumeLabel(int idx, double vol) {
        if (idx < volumeLabels.size()) {
            volumeLabels.set(idx, vol);
        } else {
            volumeLabels.add(vol);
        }
    }

    /**
     * Compute the watershed image as next iteration from previous watershed ,
     * only for voxels with value > threshold
     *
     * @param threshold
     * @return
     */
    public ImageInt continueWatershed3D(int threshold) {
        if (watershedImage == null) {
            this.computeWatershed(threshold);
        } else {
            IJ.log("Continuing watershed");
            continueWatershed(threshold);
        }
        return watershedImage;
    }

    /**
     * compute the watershed image
     *
     * @return watershed image
     */
    public ImageStack getWatershedImageStack(int threshold) {
        return getWatershedImage3D(threshold).getImageStack();
    }

    /**
     * compute the watershed image
     *
     * @return watershed image
     */
    public ImageStack getWatershedImageStack() {
        return getWatershedImage3D().getImageStack();
    }

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

    private void initWatershed() {
        this.createArrayList();
        if (!okseeds) {
            IJ.log("No seeds found !");
            return;
        }
        //IJ.log("Nb voxels = " + voxels.size());
        IJ.showStatus("Sorting ...");
        Collections.shuffle(voxels);
        Collections.sort(voxels);
    }

    private void computeWatershed(int threshold) {
        initWatershed();
        continueWatershed(threshold);
    }

    private void continueWatershed(int threshold) {
        int sx = rawImage.sizeX;
        int sy = rawImage.sizeY;
        int sz = rawImage.sizeZ;
        int MaxIterations = (int) Math.sqrt(sx * sx + sy * sy + sz * sz);
        //IJ.log("ite=" + MaxIterations);
        int ite = 0;
        boolean loop = true;
        int nb0, nb1;
        while ((loop) && (ite < MaxIterations)) {
            IJ.showStatus("Watershed " + ite + " (" + voxels.size() + ")");
            //IJ.log("Watershed " + ite + " (" + voxels.size() + ")");
            ite++;
            nb0 = voxels.size();
            loop = assignWatershed(threshold);
            nb1 = voxels.size();
            // no voxels could be assigned --> stop
            if (nb1 == nb0) {
                loop = false;
            }
            //IJ.log("Nb " + nb0 + " " + nb1 + " " + loop);
        }
    }

    public ImageHandler getNonLabelledVoxels(int threshold, boolean value) {
        ImageHandler res = watershedImage.createSameDimensions();
        for (Voxel3DComparable V : voxels) {
            if (V.getValue() >= threshold) {
                if (value) {
                    res.setPixel(V, (int) V.getValue());
                } else {
                    res.setPixel(V, 255);
                }
            }
        }

        return res;
    }

    private boolean assignWatershed(int threshold) {
        Voxel3DComparable voxel;
        int px, py, pz;
        ArrayUtil neigh;
        int max, max2;
        boolean loop = false;
        ArrayList voxels2 = new ArrayList();
        for (Iterator it = voxels.iterator(); it.hasNext();) {
            voxel = (Voxel3DComparable) it.next();
            if (voxel.getValue() < threshold) {
                voxels2.add(voxel);
                continue;
            }
            px = voxel.getRoundX();
            py = voxel.getRoundY();
            pz = voxel.getRoundZ();
            if (watershedImage.getPixel(px, py, pz) == NO_LABEL) {
                // 6-neighbor
                //neigh = watershedImage.getNeighborhoodCross3D(px, py, pz);
                // 26-neighbor
                neigh = watershedImage.getNeighborhood3x3x3(px, py, pz);
                max = (int) neigh.getMaximum();
                if ((max == NO_LABEL) || (max == BORDER)) {                    
                    voxels2.add(voxel);
                } else { // has a neighbor already labeled // test if two differents labels around
                    max2 = (int) neigh.getMaximumBelow(max);
                    // only one label around
                    if ((max2 == NO_LABEL) || (max2 == BORDER)) {
                        watershedImage.setPixel(px, py, pz, max);
                        loop = true;
                        // compute volumes
                        if (computeVolumes) {
                            //volumeLabels.get(max)[1]++;
                            volumeLabels.set(max, volumeLabels.get(max) + 1);
                        }
                        // get active label
                        if (computeUpdatedLabels) {
                            String la = "" + max;
                            // test if exists already
                            boolean ok = true;
                            for (String S : updatedLabels) {
                                if ((S.compareTo(la)) == 0) {
                                    ok = false;
                                    break;
                                }
                            }
                            if (ok) {
                                updatedLabels.add(la);
                            }
                        }
                        // add the new labels sharing a border
                        if ((max2 == BORDER) && (computeAssociation)) {
                            updateAssociationBorder(px, py, pz, max);
                        }
                        // two or more labels around
                    } else {
                        watershedImage.setPixel(px, py, pz, BORDER);
                        // get association
                        if (computeAssociation) {
                            String asso = max + "_" + max2;
                            AssociationRegion assoR = new AssociationRegion();
                            assoR.addRegion(max);
                            assoR.addRegion(max2);
                            max2 = (int) neigh.getMaximumBelow(max2);
                            while ((max2 != NO_LABEL) && (max2 != BORDER)) {
                                asso = asso.concat("_" + max2);
                                assoR.addRegion(max2);
                                max2 = (int) neigh.getMaximumBelow(max2);
                            }
                            // if next to border update 
                            if ((max2 == BORDER) && (computeAssociation)) {
                                for (String S : asso.split("_")) {
                                    updateAssociationBorder(px, py, pz, Integer.parseInt(S));
                                }
                            }
                            // test if association exists already
                            boolean ok = true;
                            for (String S : associations) {
                                if ((S.compareTo(asso)) == 0) {
                                    ok = false;
                                    break;
                                }
                            }
                            if (ok) {
                                associations.add(asso);
                            }
                            assoRegions.addAssoRegion(assoR);
                        }
                    }
                }
            }
        }
        voxels = voxels2;
        return loop;
    }

    private void createArrayList() {
        voxels = new ArrayList();
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
        //IJ.log("MAX SEEDS " + seedsLabel.getImageStats(null).getMax());
        // since seedsLabel starts at 1 and watershed at 2, replace values
        seedsLabel.replacePixelsValue(BORDER, (int) seedsLabel.getMax() + 1);
        seedsLabel.resetStats(null);
        // compute volumes
        //IJ.log("MAX SEEDS " + seedsLabel.getImageStats(null).getMax());
        if (computeVolumes) {
            for (int i = 0; i <= seedsLabel.getMax(); i++) {
                volumeLabels.add(0.0);
            }
        }

        for (int z = 0; z < sz; z++) {
            IJ.showStatus("Processsing " + z);
            for (int y = 0; y < sy; y++) {
                for (int x = 0; x < sx; x++) {
                    pix = rawImage.getPixel(x, y, z);
                    se = seedsLabel.getPixel(x, y, z);
                    if (pix > rawThreshold) {
                        //IJ.log("seed found " + x + " " + y + " " + z+" : "+se);
                        if (se > 0) {
                            //IJ.log("seed found " + x + " " + y + " " + z);
                            watershedImage.setPixel(x, y, z, se);
                            voxels.add(new Voxel3DComparable(x, y, z, pix, se));
                            okseeds = true;
                        } else {
                            voxels.add(new Voxel3DComparable(x, y, z, pix, NO_LABEL));
                            watershedImage.setPixel(x, y, z, NO_LABEL);
                        }
                    } else {
                        watershedImage.setPixel(x, y, z, NO_LABEL);
                    }
                    // compute volumes (warning if seeds are float values)
                    if (computeVolumes) {
                        //volumeLabels.get((int) se)[1]++;
                        volumeLabels.set((int) se, volumeLabels.get((int) se) + 1);
                    }
                }
            }
        }
    }

    private void updateAssociationBorder(int px, int py, int pz, int label) {
        //int label = watershedImage.getPixelInt(px, py, pz);
        // get neighboring borders
        int sx = Math.max(0, px - 1);
        int sy = Math.max(0, py - 1);
        int sz = Math.max(0, pz - 1);
        int ex = Math.min(watershedImage.sizeX - 1, px + 1);
        int ey = Math.min(watershedImage.sizeY - 1, py + 1);
        int ez = Math.min(watershedImage.sizeZ - 1, pz + 1);

        for (int x = sx; x <= ex; x++) {
            for (int y = sy; y <= ey; y++) {
                for (int z = sz; z <= ez; z++) {
                    if (watershedImage.getPixel(x, y, z) == BORDER) {
                        ArrayUtil neigh = watershedImage.getNeighborhood3x3x3(x, y, z);
                        // update all associations
                        for (int i = 0; i < neigh.getSize(); i++) {
                            int val = (int) neigh.getValue(i);
                            if ((val != BORDER) && (val != NO_LABEL) && (val != label)) {
                                // always max value first
                                String asso;
                                AssociationRegion assoR = new AssociationRegion();
                                assoR.addRegion(label);
                                assoR.addRegion(val);
                                if (label > val) {
                                    asso = label + "_" + val;
                                } else {
                                    asso = val + "_" + label;
                                }
                                // test if association exists already
                                boolean ok = true;
                                for (String S : associations) {
                                    if ((S.compareTo(asso)) == 0) {
                                        ok = false;
                                        break;
                                    }
                                }
                                if (ok) {
                                    associations.add(asso);
                                }
                                assoRegions.addAssoRegion(assoR);
                            }
                        }
                    }
                }
            }
        }
    }
}
