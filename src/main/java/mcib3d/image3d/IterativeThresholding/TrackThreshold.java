package mcib3d.image3d.IterativeThresholding;

import ij.IJ;
import ij.ImagePlus;
import mcib3d.geom.Object3DVoxels;
import mcib3d.geom.Point3D;
import mcib3d.geom.Voxel3D;
import mcib3d.image3d.*;
import mcib3d.utils.ArrayUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

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
    public static final byte CRITERIA_METHOD_MAX_COMPACITY = 4;
    public boolean verbose = true;
    // volume range in voxels
    private int volMax = 1000;
    private int volMin = 1;
    private int threshold_method = THRESHOLD_METHOD_STEP;
    private int step = 1;
    private int nbClasses = 100;
    private int startThreshold;
    private int stopThreshold = Integer.MAX_VALUE;
    private int criteria_method = CRITERIA_METHOD_MIN_ELONGATION;
    private int GlobalThreshold;
    private ArrayList<Point3D> point3Ds = null;

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

    public void setStopThreshold(int stopThreshold) {
        this.stopThreshold = stopThreshold;
    }

    public void setMethodThreshold(int meth) {
        threshold_method = meth;
    }

    public void setCriteriaMethod(int criteria_method) {
        this.criteria_method = criteria_method;
    }

    public void setMarkers(ArrayList<Point3D> point3Ds) {
        this.point3Ds = point3Ds;
    }

    private int[] initHistogram(ImageHandler img) {
        int[] histogramThreshold = new int[0];
        if (threshold_method == THRESHOLD_METHOD_STEP) {
            //int min = (int) img.getMin();
            int max = Math.min(stopThreshold, (int) img.getMax());
            ImageInt mask = null;
            int[] histogramImage = img.getHistogram(mask, 65536, 0, 65535);
            ArrayList<Integer> histogramTmp = new ArrayList<Integer>();
            int i = startThreshold;
            while (i < max) {
                while ((histogramImage[i] == 0) && (i < max)) i++;
                histogramTmp.add(i);
                i += step;
            }
            histogramThreshold = new int[histogramTmp.size()];
            for (i = 0; i < histogramTmp.size(); i++) {
                histogramThreshold[i] = histogramTmp.get(i);
            }
        } else if (threshold_method == THRESHOLD_METHOD_KMEANS) {
            // K-means
            BlankMask mas = new BlankMask(img);
            int[] h = img.getHistogram(mas, 65536, 0, 65535);
            if (verbose) {
                IJ.log("Computing k-means");
            }
            histogramThreshold = ArrayUtil.kMeans_Histogram1D(h, nbClasses, startThreshold);
            Arrays.sort(histogramThreshold);
        } else if (threshold_method == THRESHOLD_METHOD_VOLUME)
            histogramThreshold = constantVoxelsHistogram(img, nbClasses, startThreshold);

        return histogramThreshold;
    }

    private ArrayList<ObjectTrack> computeAssociation(ArrayList<ObjectTrack> frame1, ArrayList<ObjectTrack> frame2, ArrayList<ObjectTrack> allFrames, ImageHandler labels2) {
        HashMap<Integer, ObjectTrack> hashObjectsT2 = new HashMap<Integer, ObjectTrack>();
        for (ObjectTrack objectTrack : frame2) {
            hashObjectsT2.put(objectTrack.getObject().getValue(), objectTrack);
        }

        // create association
        ArrayList<ObjectTrack> newListTrack = new ArrayList<ObjectTrack>();

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

    private ArrayList<ObjectTrack> computeFrame(ImageHandler img, ArrayList<Object3DVoxels> objects, ArrayList<Point3D> markers, int threshold, Criterion criterion) {
        ArrayList<ObjectTrack> frame1 = new ArrayList<ObjectTrack>();
        for (Object3DVoxels ob : objects) {
            if ((markers == null) || (ob.insideOne(markers))) {
                ObjectTrack obt = new ObjectTrack();
                obt.setObject(ob);
                obt.setFrame(threshold);
                // add measurements
                obt.computeCriterion(criterion);
                obt.volume = ob.getVolumePixels();
                obt.seed = ob.getFirstVoxel();
                obt.threshold = threshold;
                obt.rawImage = img;
                frame1.add(obt);
            }
        }
        return frame1;
    }

    private ArrayList<ImageHandler> process(ImageHandler img) {
        Criterion criterion;
        if (criteria_method == CRITERIA_METHOD_MIN_ELONGATION)
            criterion = new CriterionElongation();
        else if (criteria_method == CRITERIA_METHOD_MAX_COMPACITY)
            criterion = new CriterionCompactness();
        else
            criterion = new CriterionVolume();

        BestCriterion bestCriterion;
        if ((criteria_method == CRITERIA_METHOD_MAX_VOLUME) || (criteria_method == CRITERIA_METHOD_MAX_COMPACITY)) {
            bestCriterion = new BestCriteriaMax();
        } else if (criteria_method == CRITERIA_METHOD_MIN_ELONGATION) {
            bestCriterion = new BestCriteriaMin();
        } else {
            bestCriterion = new BestCriteriaStable();
        }
        int T0, TMaximum, T1;
        int[] histogramThreshold;
        ImageLabeller labeler = new ImageLabeller(volMin, volMax);
        TMaximum = (int) img.getMax();

        if (verbose) IJ.log("Analysing histogram ...");
        histogramThreshold = initHistogram(img);
        T0 = histogramThreshold[0];

        // first frame
        T1 = T0;
        if (verbose) IJ.log("Computing frame for first threshold " + T1);
        ArrayList<ObjectTrack> frame1 = computeFrame(img, labeler.getObjects(img.thresholdAboveInclusive(T1)), point3Ds, T1, criterion);

        if (verbose) {
            IJ.log("");
            IJ.log("");
        }

        String update = "\\Update:";
        //update = "";
        ArrayList<ObjectTrack> allFrames = new ArrayList<ObjectTrack>();
        allFrames.addAll(frame1);
        // use histogram and unique values to loop over pixel values
        GlobalThreshold = 1;
        while (T1 <= TMaximum) {
            int T2 = computeNextThreshold(T1, TMaximum, img, histogramThreshold);
            if (T2 < 0) break;
            if (verbose) IJ.log(update + "Computing frame for threshold " + T2 + "                   ");

            // Threshold T2
            ImageHandler bin2 = img.thresholdAboveInclusive(T2);
            ImageHandler labels2 = labeler.getLabels(bin2);
            ArrayList<ObjectTrack> frame2 = computeFrame(img, labeler.getObjects(bin2), point3Ds, T2, criterion);


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
            ArrayList<ObjectTrack> toBeRemoved = new ArrayList<ObjectTrack>();
            for (ObjectTrack obt : allFrames) {
                if (obt.getState() == ObjectTrack.STATE_DIE) {
                    ObjectTrack anc = obt.getAncestor();
                    if (anc == null) {
                        anc = obt;
                    }
                    ArrayList<ObjectTrack> list = obt.getLineageTo(anc);

                    ObjectTrack bestObject = computeBestObject(list, bestCriterion);

                    // segment spot
                    Voxel3D seed = anc.seed;
                    int threshold = anc.threshold;
                    ObjectTrack objectSegment = anc;
                    if (bestObject != null) {
                        objectSegment = bestObject;
                        seed = objectSegment.seed;
                        threshold = objectSegment.threshold;
                    }
                    Segment3DSpots SegmentSpot = new Segment3DSpots(objectSegment.rawImage, null);
                    new Object3DVoxels(SegmentSpot.segmentSpotClassical(seed.getRoundX(), seed.getRoundY(), seed.getRoundZ(), threshold, idx)).draw(draw, idx);
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

    private ObjectTrack computeBestObject(ArrayList<ObjectTrack> list, BestCriterion bestCriterion) {
        // test maximal volume //or one with minimal elongation
        // get all values
        ArrayUtil valueCriterion = new ArrayUtil(list.size());
        for (int i = 0; i < valueCriterion.getSize(); i++) {
            valueCriterion.putValue(i, list.get(i).valueCriteria);
        }

        return list.get(bestCriterion.computeBestCriterion(valueCriterion));
    }

    private int computeNextThreshold(int T1, int Tmax, ImageHandler img, int[] histogram) {
        int T2 = T1;
        if (T2 < Tmax) {
            T2 = histogram[GlobalThreshold];
            GlobalThreshold++;
            while ((T2 == 0) && (T2 <= Tmax + 1) && (GlobalThreshold < histogram.length)) {
                T2 = histogram[GlobalThreshold];
                GlobalThreshold++;
            }
            if ((T2 > Tmax + 1) || (GlobalThreshold >= histogram.length)) {
                T2 = -1;
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

        // no results
        if (drawsReconstruct.size() == 0) return null;
        // drawing results
        ImageHandler[] drawsTab = new ImageHandler[drawsReconstruct.size()];
        for (int i = 0; i < drawsTab.length; i++) {
            drawsTab[i] = drawsReconstruct.get(i);
        }

        return ImageHandler.getHyperStack("draw", drawsTab);
    }

    public void setVerbose(boolean verbose) {
        this.verbose = verbose;
    }
}
