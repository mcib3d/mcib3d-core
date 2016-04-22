package mcib3d.image3d.IterativeThresholding;

import ij.IJ;
import ij.ImagePlus;
import mcib3d.geom.Object3DVoxels;
import mcib3d.geom.Voxel3D;
import mcib3d.image3d.*;
import mcib3d.utils.ArrayUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 * *
 * /**
 * Copyright (C) 2008- 2012 Thomas Boudier and others
 * <p>
 * <p>
 * <p>
 * This file is part of mcib3d
 * <p>
 * mcib3d is free software; you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 3 of the License, or (at your option) any later
 * version.
 * <p>
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * <p>
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 *
 * @author thomas
 */
public class TrackThreshold {

    public static final byte THRESHOLD_METHOD_STEP = 1;
    public static final byte THRESHOLD_METHOD_KMEANS = 2;
    public static final byte THRESHOLD_METHOD_VOLUME = 3;
    // measure to use
    public static final byte CRITERIA_METHOD_MIN_ELONGATION = 1;
    public static final byte CRITERIA_METHOD_MAX_VOLUME = 2;
    public static final byte CRITERIA_METHOD_MSER = 3; // min variation of volume
    public boolean verbose = true;
    // volume range in voxels
    int volMax = 1000;
    int volMin = 1;
    int threshold_method = THRESHOLD_METHOD_STEP;
    int step = 1;
    int nbClasses = 100;
    int startThreshold;
    int criteria_method = CRITERIA_METHOD_MIN_ELONGATION;
    private int GlobalThreshold;

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

    private static int[] constantVoxelsHistogram(ImageHandler img, int nbClasses, int startThreshold) {
        int[] res;
        // 8 bits --> classical histogram
        if (img instanceof ImageByte) {
            res = new int[256];
            for (int i = 0; i < 256; i++) {
                res[i] = i;
            }
            return res;
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

    public void setMethodThreshold(int meth) {
        threshold_method = meth;
    }

    public void setCriteriaMethod(int criteria_method) {
        this.criteria_method = criteria_method;
    }

    private int[] initHistogram(ImageHandler img) {
        int[] histoThreshold = new int[0];
        if (threshold_method == THRESHOLD_METHOD_STEP) {
            int min = (int) img.getMin();
            int max = (int) img.getMax();
            ImageInt mask = null;
            int[] histogramImage = img.getHistogram(mask, max - min + 1, min, max);
            ArrayList<Integer> histogramTmp = new ArrayList<Integer>();
            for (int i = 0; i < histogramImage.length; i++) {
                if (histogramImage[i] > 0) histogramTmp.add(i);
            }
            histoThreshold = new int[histogramTmp.size()];
            for (int i = 0; i < histogramTmp.size(); i++) {
                histoThreshold[i] = histogramTmp.get(i);
            }
        } else if (threshold_method == THRESHOLD_METHOD_KMEANS) {
            // K-means
            BlankMask mas = new BlankMask(img);
            int[] h = img.getHistogram(mas, 65536, 0, 65535);
            if (verbose) {
                IJ.log("Computing k-means");
            }
            histoThreshold = ArrayUtil.kMeans_Histogram1D(h, nbClasses, startThreshold);
            Arrays.sort(histoThreshold);
        } else if (threshold_method == THRESHOLD_METHOD_VOLUME)
            histoThreshold = constantVoxelsHistogram(img, nbClasses, startThreshold);

        return histoThreshold;
    }

    private ArrayList<ObjectTrack> computeAssociation(ArrayList<ObjectTrack> frame1, ArrayList<ObjectTrack> frame2, ArrayList<ObjectTrack> allFrames, ImageHandler labels2) {
        HashMap<Integer, ObjectTrack> hashObjectsT2 = new HashMap();
        for (ObjectTrack objectTrack : frame2) {
            hashObjectsT2.put(objectTrack.getObject().getValue(), objectTrack);
        }

        // create association
        ArrayList<ObjectTrack> newListTrack = new ArrayList();

        for (ObjectTrack obt : frame1) {
            ArrayUtil list = obt.getObject().listValues(labels2, 0);
            list = list.distinctValues();
            // association 1<->1
            if (list.getSize() == 1) {
                ObjectTrack child = hashObjectsT2.get((int) list.getValue(0));
                if ((child != null) && (obt.volume > child.volume)) {
                    obt.addChild(child);
                    child.setParent(obt);
                    allFrames.add(child);
                    newListTrack.add(child);
                    frame2.remove(child);
                    obt.setObject(null);
                }
                if ((child != null) && (obt.volume == child.volume)) {
                    newListTrack.add(obt);
                    frame2.remove(child);
                }
            }
            // association 1<->n
            else if (list.getSize() > 1) {
                for (int i = 0; i < list.getSize(); i++) {
                    ObjectTrack child = hashObjectsT2.get((int) list.getValue(i));
                    if ((child != null)) {
                        obt.addChild(child);
                        child.setParent(obt);
                        newListTrack.add(child);
                        allFrames.add(child);
                        frame2.remove(child);
                        obt.setObject(null);
                    }
                }
            }

        }
        // add all new objects from T2
        newListTrack.addAll(frame2);

        return newListTrack;
    }

    private ArrayList<ObjectTrack> computeFrame(ImageHandler img, ArrayList<Object3DVoxels> objects, int threshold, Criterion criterion) {
        ArrayList<ObjectTrack> frame1 = new ArrayList<ObjectTrack>();
        for (Object3DVoxels ob : objects) {
            ObjectTrack obt = new ObjectTrack();
            obt.setObject(ob);
            obt.setFrame(threshold);
            // add measurements
            obt.computeCriterion(criterion);
            obt.volume = ob.getVolumePixels();
            obt.sphericity = ob.getCompactness();
            obt.elongation = ob.getMainElongation();
            obt.DCavg = ob.getDistCenterMean();
            obt.DCsd = ob.getDistCenterSigma();
            obt.seed = ob.getFirstVoxel();
            obt.threshold = threshold;
            obt.rawImage = img;
            frame1.add(obt);
        }
        return frame1;
    }

    private ArrayList<ImageHandler> process(ImageHandler img) {
        Criterion criterion;
        if ((criteria_method == CRITERIA_METHOD_MAX_VOLUME) || (criteria_method == CRITERIA_METHOD_MSER))
            criterion = new CriterionVolume();
        else
            criterion = new CriterionElongation();
        BestCriterion bestCriterion;
        if (criteria_method == CRITERIA_METHOD_MAX_VOLUME) bestCriterion = new BestCriteriaMax();
        else bestCriterion = new BestCriteriaMin();
        int T0, Tmaximum, T1;
        int[] histogramThreshold;
        ImageLabeller labeler = new ImageLabeller(volMin, volMax);
        Tmaximum = (int) img.getMax();

        if (verbose) IJ.log("Analysing histogram ...");
        histogramThreshold = initHistogram(img);
        T0 = histogramThreshold[0];

        // first frame
        T1 = T0;
        if (verbose) IJ.log("Computing frame for first threshold " + T1);
        ArrayList<ObjectTrack> frame1 = computeFrame(img, labeler.getObjects(img.thresholdAboveInclusive(T1)), T1, criterion);

        if (verbose) {
            IJ.log("");
            IJ.log("");
        }

        String update = "\\Update:";
        //update = "";
        ArrayList<ObjectTrack> allFrames = new ArrayList();
        allFrames.addAll(frame1);
        // use histogram and unique values to loop over pixel values
        GlobalThreshold = 1;
        while (T1 <= Tmaximum) {
            int T2 = computeNextThreshold(T1, Tmaximum, img, histogramThreshold);
            if (T2 < 0) break;
            if (verbose) IJ.log(update + "Computing frame for threshold " + T2 + "                   ");

            // Threshold T2
            ImageHandler bin2 = img.thresholdAboveInclusive(T2);
            ImageHandler labels2 = labeler.getLabels(bin2);
            ArrayList<ObjectTrack> frame2 = computeFrame(img, labeler.getObjects(bin2), T2, criterion);


            System.gc();
            // T2--> new T1
            T1 = T2;
            frame1 = computeAssociation(frame1, frame2, allFrames, labels2);
        } // thresholding
        if (verbose) {
            IJ.log("Iterative Thresholding finished");
        }

        return computeResults(allFrames, img, bestCriterion);
    }

    private ArrayList<ImageHandler> computeResults(ArrayList<ObjectTrack> allFrames, ImageHandler img, BestCriterion bestCriterion) {
        // get results from the tree with different levels of objects
        int level = 1;
        int maxLevel = 10;
        ArrayList<ImageHandler> drawsReconstruct = new ArrayList<ImageHandler>();
        while ((!allFrames.isEmpty()) && (level < maxLevel)) {
            if (verbose) {
                IJ.log("Nb total objects level " + level + " : " + allFrames.size());
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
                if (obt.getState() == ObjectTrack.STATE_DIE) {
                    ObjectTrack anc = obt.getAncestor();
                    if (anc == null) {
                        anc = obt;
                    }
                    ArrayList<ObjectTrack> list = obt.getLineageTo(anc);

                    // test maximal volume //or one with minimal elongation
                    // get all values
                    ArrayUtil vols = new ArrayUtil(list.size());
                    for (int i = 0; i < vols.getSize(); i++) {
                        if ((criteria_method == CRITERIA_METHOD_MAX_VOLUME) || (criteria_method == CRITERIA_METHOD_MSER)) {
                            vols.putValue(i, list.get(i).volume);
                        } else if (criteria_method == CRITERIA_METHOD_MIN_ELONGATION) {
                            vols.putValue(i, list.get(i).elongation);
                        }
                    }
                    int imax = 0;
                    if (criteria_method == CRITERIA_METHOD_MAX_VOLUME) {
                        imax = vols.getMaximumIndex();
                        imax = bestCriterion.computeBestCriterion(vols);
                    } else if (criteria_method == CRITERIA_METHOD_MIN_ELONGATION) {
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
                    int threshold = anc.threshold;
                    ObjectTrack obdraw = anc;
                    if (imax >= 0) {
                        obdraw = list.get(imax);
                        seed = obdraw.seed;
                        threshold = obdraw.threshold;
                    }
                    Segment3DSpots segspot = new Segment3DSpots(obdraw.rawImage, null);
                    ArrayList<Voxel3D> vox = segspot.segmentSpotClassical(seed.getRoundX(), seed.getRoundY(), seed.getRoundZ(), threshold, idx);
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
        }

        allFrames = null;
        System.gc();

        return drawsReconstruct;
    }

    private int computeNextThreshold(int T1, int Tmax, ImageHandler img, int[] histogram) {
        int T2 = T1;
        if (T2 < Tmax) {
            if (threshold_method == THRESHOLD_METHOD_STEP) {
                T2 = T1 + step;
                while ((!img.hasOneValue(T2)) && (T2 <= Tmax + 1)) {
                    T2++;
                }
                if (T2 > Tmax + 1) T2 = -1;
            } else if ((threshold_method == THRESHOLD_METHOD_KMEANS) || (threshold_method == THRESHOLD_METHOD_VOLUME)) {
                T2 = histogram[GlobalThreshold];
                GlobalThreshold++;
                while ((T2 == 0) && (T2 <= Tmax + 1) && (GlobalThreshold < histogram.length)) {
                    T2 = histogram[GlobalThreshold];
                    GlobalThreshold++;
                }
                if ((T2 > Tmax + 1) || (GlobalThreshold >= histogram.length)) {
                    T2 = -1;
                }
            }
        } else if (T2 == Tmax) {
            // use extra threshold to finalize all objects without any child
            T2 = Tmax + 1;
        }

        return T2;
    }

    public ImageHandler segment(ImageHandler input, boolean show) {
        setVerbose(show);
        ArrayList<ImageHandler> res = process(input);
        if (!res.isEmpty()) {
            return res.get(0);
        } else {
            return null;
        }
    }

    public ArrayList<ImageHandler> segmentAll(ImageHandler input, boolean show) {
        setVerbose(show);
        return process(input);
    }

    public ImagePlus segment(ImagePlus input, boolean show) {
        setVerbose(show);
        ArrayList<ImageHandler> drawsReconstruct = process(ImageHandler.wrap(input));

        // drawing results
        ImageHandler[] drawsTab = new ImageHandler[drawsReconstruct.size()];
        for (int i = 0; i < drawsTab.length; i++) {
            drawsTab[i] = drawsReconstruct.get(i);
        }
        ImagePlus plusDraw = ImageHandler.getHyperStack("draw", drawsTab);

        return plusDraw;
    }

    public void setVerbose(boolean verbose) {
        this.verbose = verbose;
    }
}
