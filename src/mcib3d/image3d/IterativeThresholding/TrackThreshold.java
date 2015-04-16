package mcib3d.image3d.IterativeThresholding;

import ij.IJ;
import ij.ImagePlus;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import mcib3d.geom.Object3D;
import mcib3d.geom.Object3DVoxels;
import mcib3d.geom.Voxel3D;
import mcib3d.image3d.BlankMask;
import mcib3d.image3d.ImageByte;
import mcib3d.image3d.ImageFloat;
import mcib3d.image3d.ImageHandler;
import mcib3d.image3d.ImageLabeller;
import mcib3d.image3d.ImageShort;
import mcib3d.image3d.Segment3DSpots;
import mcib3d.utils.ArrayUtil;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 **
 * /**
 * Copyright (C) 2008- 2012 Thomas Boudier and others
 *
 *
 *
 * This file is part of mcib3d
 *
 * mcib3d is free software; you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 3 of the License, or (at your option) any later
 * version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 *
 * @author thomas
 */
public class TrackThreshold {

    // volume range in voxels 
    int volMax = 1000;
    int volMin = 1;
    public static final byte THRESHOLD_METHOD_STEP = 1;
    public static final byte THRESHOLD_METHOD_KMEANS = 2;
    public static final byte THRESHOLD_METHOD_VOLUME = 3;
    int threshold_method = THRESHOLD_METHOD_STEP;
    int step = 1;
    int nbClasses = 100;
//    double volMaxUnit = 1;
//    double volMinUnit = 1;
//    double volUnit = 1;
//    String unit = "pix";
//    boolean filter = true;
//    Calibration cal;
    int startThreshold;
    // measure to use
    public static final byte CRITERIA_METHOD_MIN_ELONGATIO = 1;
    public static final byte CRITERIA_METHOD_MAX_VOLUME = 2;
    public static final byte CRITERIA_METHOD_MSER = 3; // min variation of volume
    int criteria_method = CRITERIA_METHOD_MIN_ELONGATIO;
    public boolean verbose = true;

    public TrackThreshold(int vmin, int vmax, int st, int nbCla, int sta) {
        if (vmax >= vmin) {
            volMax = vmax;
            volMin = vmin;
        } else {
            volMax = vmin;
            volMin = vmax;
        }
        step = st;
        nbClasses = nbCla;
        startThreshold = sta;
    }

    public void setMethodThreshold(int meth) {
        threshold_method = meth;
    }

    public void setCriteriaMethod(int criteria_method) {
        this.criteria_method = criteria_method;
    }

    public ImagePlus segment(ImagePlus input, boolean show) {
        int T0 = 0, Tmax = Integer.MAX_VALUE, T1;
        int[] histoThreshold = new int[0];
        ImageHandler img = ImageHandler.wrap(input);
        ImageLabeller labeler = new ImageLabeller(volMin, volMax);
        if (threshold_method == THRESHOLD_METHOD_STEP) {
            // use start for step method
            T0 = startThreshold;
            Tmax = (int) img.getMax();
        } else if (threshold_method == THRESHOLD_METHOD_KMEANS) {
            // K-means
            BlankMask mas = new BlankMask(img);
            int[] h = img.getHistogram(mas, 65536, 0, 65535);
            if (verbose) {
                IJ.log("Computing k-means");
            }
            histoThreshold = ArrayUtil.kMeans_Histogram1D(h, nbClasses, startThreshold);
            Arrays.sort(histoThreshold);
            if (show) {
                int c = 0;
                for (int i = 0; i < histoThreshold.length; i++) {
                    IJ.log("" + histoThreshold[i]);
                    if (histoThreshold[i] > 0) {
                        c++;
                    }
                }
                if (verbose) {
                    IJ.log("Non zero " + c);
                }
            }
            T0 = histoThreshold[0];
            Tmax = (int) img.getMax();
        } else if (threshold_method == THRESHOLD_METHOD_VOLUME) {
            histoThreshold = constantVoxelsHistogram(img, nbClasses, startThreshold);
        }

        // TEST
        //IJ.log("Thresholds:" + Arrays.toString(kmeansHisto));
//        int[] hist;
//        // optimize histogram for 16 bits
//        if ((img instanceof ImageShort)) {
//            hist = this.constantVoxelsHistogram(img, (int) Math.max(1, 0.75 * volMin));
//        } else {
//            hist = new int[256];
//            for (int i = 0; i < 256; i++) {
//                hist[i] = i;
//            }
//        }
//
//        int T1;
//        int idxT = 0;
//        while (hist[idxT] < T0) {
//            idxT++;
//        }
//        T0 = hist[idxT];
        T1 = T0;
        ImageHandler bin1 = img.thresholdAboveInclusive(T1);

        // test classification (useless)
        //Criterion crit = new Criterion(Object3D.MEASURE_MAIN_ELONGATION, 1, 100);
        //Classification class1 = new Classification();
        //class1.addCriterion(crit);
        ArrayList<Object3DVoxels> obj1 = labeler.getObjects(bin1);
        ArrayList<ObjectTrack> frame1 = new ArrayList();
        for (Object3DVoxels ob : obj1) {
            //if (class1.checkClassification(ob)) {
            ObjectTrack obt = new ObjectTrack();
            // TODO keep only list of unique pixels in next frame, for object association
            obt.setObject(ob);
            obt.setFrame(T1);
            // add measurments
            obt.volume = ob.getVolumePixels();
            obt.sphericity = ob.getCompactness();
            obt.elongation = ob.getMainElongation();
            obt.DCavg = ob.getDistCenterMean();
            obt.DCsd = ob.getDistCenterSigma();
            obt.seed = ob.getFirstVoxel();
            obt.threshold = T1;
            obt.rawImage = img;
            frame1.add(obt);
            //}
        }

        if (show) {
            IJ.log("");
            IJ.log("");
        }

        String update = "\\Update:";
        //update = "";
        ArrayList<ObjectTrack> allFrames = new ArrayList();
        // use histogram and unique values to loop over pixel values
        int c = 1;
        while (T1 <= Tmax) {
            int T2 = T1;
            if (show) {
                IJ.log(update + "Threshold1 " + T1 + " " + obj1.size() + " objects");
            }
            if (T2 < Tmax) {
                if (threshold_method == THRESHOLD_METHOD_STEP) {
                    T2 = T1 + step;
                    while ((!img.hasOneValue(T2)) && (T2 <= Tmax + 1)) {
                        T2++;
                    }
                    if (T2 > Tmax + 1) {
                        break;
                    }
                } else if ((threshold_method == THRESHOLD_METHOD_KMEANS) || (threshold_method == THRESHOLD_METHOD_VOLUME)) {
                    T2 = histoThreshold[c];
                    c++;
                    while ((T2 == 0) && (T2 <= Tmax + 1) && (c < histoThreshold.length)) {
                        T2 = histoThreshold[c];
                        c++;
                    }
                    if ((T2 > Tmax + 1) || (c >= histoThreshold.length)) {
                        break;
                    }
                }
            } else if (T2 == Tmax) {
                // use extra threshold to finalize all objects without any child
                T2 = Tmax + 1;
            }

            // Threshold T2
            ImageHandler bin2 = img.thresholdAboveInclusive(T2);
            ArrayList<Object3DVoxels> obj2 = labeler.getObjects(bin2);
            if (show) {
                IJ.log(update + "Threshold2 " + T2 + " " + obj2.size() + " objects");
            }
            ImageHandler labels2 = labeler.getLabels(bin2);
            //labels2.show("T2 " + T2 + " " + obj2.size() + " " + labeler.getNbObjectsTotal(bin2));

            // Create objects T1
            allFrames.addAll(frame1);

            // Create objects T2
            HashMap<Integer, ObjectTrack> hash = new HashMap();
            ArrayList<ObjectTrack> frame2 = new ArrayList();
            for (Object3DVoxels ob : obj2) {
                // if (class1.checkClassification(ob)) {
                ObjectTrack obt = new ObjectTrack();
                obt.setObject(ob);
                // add measurements
                obt.volume = ob.getVolumePixels();
                obt.sphericity = ob.getCompactness();
                obt.elongation = ob.getMainElongation();
                obt.DCavg = ob.getDistCenterMean();
                obt.DCsd = ob.getDistCenterSigma();
                obt.seed = ob.getFirstVoxel();
                obt.threshold = T2;
                obt.rawImage = img;
                frame2.add(obt);
                hash.put(ob.getValue(), obt);
                //} else {
                //    ob.draw(labels2, 0);
                //}
            }
            // create association
            for (ObjectTrack obt : frame1) {
                ArrayUtil list = obt.getObject().listValues(labels2);
                list = list.distinctValues();
                // 1<->1
                if (list.getSize() == 1) {
                    ObjectTrack child = hash.get((int) list.getValue(0));
                    // check if change in volume
                    if (child != null) {
                        //if (child.volume != obt.volume) {
                        obt.addChild(child);
                        child.setParent(obt);
                        obt.setObject(null);
                        //} else {
                        //    frame2.add(obt);
                        //}
                    } else {
                        obt.setObject(null);
                    }
                }
                // association 1<->n
                if (list.getSize() > 1) {
                    obt.setObject(null);
                    for (int i = 0; i < list.getSize(); i++) {
                        ObjectTrack child = hash.get((int) list.getValue(i));
                        if (child != null) {
                            obt.addChild(child);
                            child.setParent(obt);
                        }
                    }
                }
            }
            // remove Objects from frame1 for memory
//            for (ObjectTrack obt : frame1) {
//                obt.setObject(null);
//            }
            System.gc();
            // T2--> new T1
            T1 = T2;
            obj1 = obj2;
            frame1 = frame2;
        } // thresholding
        if (verbose) {
            IJ.log("Finished thresholding");
        }

        // get results from the tree with different levels of objects
        int level = 1;
        int maxLevel = 10;
        ArrayList<ImageHandler> drawsReconstruct = new ArrayList<ImageHandler>();
        while ((!allFrames.isEmpty()) && (level < maxLevel)) {
            if (show) {
                IJ.log("Nb Total objects level " + level + " : " + allFrames.size());
            }
            ImageHandler draw;
            // 32-bits case 
            if (allFrames.size() < 65535) {
                draw = new ImageShort("DrawNew", img.sizeX, img.sizeY, img.sizeZ);
            } else {
                draw = new ImageFloat("DrawNew", img.sizeX, img.sizeY, img.sizeZ);
            }
            drawsReconstruct.add(draw);
            int idx = 1;
            ArrayList<ObjectTrack> toBeRemoved = new ArrayList();
            Iterator<ObjectTrack> it = allFrames.iterator();
            while (it.hasNext()) {
                ObjectTrack obt = it.next();
                //IJ.log("obj " + obt.getState() + " " + co + " " + obt.threshold);
                if (obt.getState() == ObjectTrack.STATE_DIE) {
                    ObjectTrack anc = obt.getAncestor();
                    if (anc == null) {
                        anc = obt;
                    }
                    ArrayList<ObjectTrack> list = obt.getLineageTo(anc);
                    if (show) {
                        //IJ.log("list " + allFrames.size());
                    }
                    // test maximal volume //or one with minimal elongation                    
                    // get all values 
                    ArrayUtil vols = new ArrayUtil(list.size());
                    for (int i = 0; i < vols.getSize(); i++) {
                        if ((criteria_method == CRITERIA_METHOD_MAX_VOLUME) || (criteria_method == CRITERIA_METHOD_MSER)) {
                            vols.putValue(i, list.get(i).volume);
                        } else if (criteria_method == CRITERIA_METHOD_MIN_ELONGATIO) {
                            vols.putValue(i, list.get(i).elongation);
                        }
                    }
                    int imax = 0;
                    if (criteria_method == CRITERIA_METHOD_MAX_VOLUME) {
                        imax = vols.getMaximumIndex();
                    } else if (criteria_method == CRITERIA_METHOD_MIN_ELONGATIO) {
                        imax = vols.getMinimumIndex();
                    } else if (criteria_method == CRITERIA_METHOD_MSER) {
                        if (vols.getSize() == 1) {
                            imax = 0;
                        } else {
                            ArrayUtil diff = vols.getDifferenceNextAbs();
                            imax = diff.getMinimumIndex();
                        }
                    }
                    // segment spot
                    Voxel3D seed = anc.seed;
                    int tresh = anc.threshold;
                    ObjectTrack obdraw = anc;
                    if (imax >= 0) {
                        obdraw = list.get(imax);
                        seed = obdraw.seed;
                        tresh = obdraw.threshold;
                    }
                    Segment3DSpots segspot = new Segment3DSpots(obdraw.rawImage, null);
                    ArrayList<Voxel3D> vox = segspot.segmentSpotClassical(seed.getRoundX(), seed.getRoundY(), seed.getRoundZ(), tresh, idx);

                    if (show) {
                        //IJ.log("Spot " + seed.getRoundX() + " " + seed.getRoundY() + " " + seed.getRoundZ() + " " + tresh + "  " + idx);
                    }
                    Object3DVoxels obnew = new Object3DVoxels(vox);
                    obnew.draw(draw, idx);
                    // set to remove all objects in list
                    toBeRemoved.addAll(list);
                    idx++;
                }
            }
            level++;
            // really remove all objects set to be removed
            if (toBeRemoved.isEmpty()) {
                break;
            }
            for (ObjectTrack obt : toBeRemoved) {
                ObjectTrack par = obt.getParent();
                if (par != null) {
                    par.removeChild(obt);
                }
            }
            allFrames.removeAll(toBeRemoved);
        } // drawing results

        ImageHandler[] drawsTab = new ImageHandler[drawsReconstruct.size()];
        for (int i = 0; i < drawsTab.length; i++) {
            drawsTab[i] = drawsReconstruct.get(i);
        }
        ImagePlus plusDraw = ImageHandler.getHyperStack("draw", drawsTab);

        allFrames = null;
        System.gc();

        return plusDraw;
    }

    private static int[] constantVoxelsHistogram(ImageHandler img, int nbClasses, int startThreshold) {
        int[] res;
        // 8 bits --> classical histogram
        if (img instanceof ImageByte) {
            res = new int[256];
            for (int i = 0; i < 256; i++) {
                res[i] = i;
            }
        }
        IJ.log("Analysing histogram ...");
        short[] pix = (short[]) img.getArray1D();
        Arrays.sort(pix);
        // starts
        int idx = 0;
        if (startThreshold > pix[pix.length - 1]) {
            startThreshold = 0;
        }
        while ((idx < pix.length) && (pix[idx] < startThreshold)) {
            idx++;
        }
        pix = Arrays.copyOfRange(pix, idx, pix.length);
        IJ.log("Finished");
        int nbVox = pix.length / nbClasses;
        res = new int[nbVox];
        Arrays.fill(res, -1);
        int count = 0;
        int val = (int) pix[0];
        res[count] = val;
        int id1 = nbVox;
        val = (int) pix[id1];
        while (id1 < pix.length) {
            while ((id1 < pix.length) && (pix[id1] == val)) {
                id1++;
            }
            if (id1 < pix.length) {
                count++;
                res[count] = pix[id1];
                id1 += nbVox;
                if (id1 < pix.length) {
                    val = pix[id1];
                }
            }
        }
        // DO NOT WRITE -1
        int[] res2 = new int[count + 1];
        System.arraycopy(res, 0, res2, 0, count + 1);

        return res2;
    }
}
