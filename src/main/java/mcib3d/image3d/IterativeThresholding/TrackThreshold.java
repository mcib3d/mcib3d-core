package mcib3d.image3d.IterativeThresholding;

import ij.ImagePlus;
import mcib3d.geom.*;
import mcib3d.image3d.*;
import mcib3d.utils.ArrayUtil;
import mcib3d.utils.Chrono;
import mcib3d.utils.Logger.AbstractLog;
import mcib3d.utils.Logger.IJLog;

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
    public static final byte CRITERIA_METHOD_MAX_COMPACTNESS = 4;
    public static final byte CRITERIA_METHOD_MAX_EDGES = 5;
    public static final byte CRITERIA_METHOD_MAX_CLASSIFICATION = 6; // based on GulMohammed 2014 BMC bioinformatics
    @Deprecated
    public boolean verbose = true;
    @Deprecated
    public boolean status = true;
    AbstractLog log = new IJLog();
    // volume range in voxels
    private int volMax = 1000;
    private int volMin = 1;
    private int contrastMin = 100;
    private int threshold_method = THRESHOLD_METHOD_STEP;
    private int step = 1;
    private int nbClasses = 100;
    private int startThreshold;
    private int stopThreshold = Integer.MAX_VALUE;
    private int criteria_method = CRITERIA_METHOD_MIN_ELONGATION;
    private int GlobalThreshold;
    // markers
    private ArrayList<Point3D> markers = null;// TO REMOVE
    private ImageInt imageMarkers = null;
    private ImageInt imageZones = null;
    private Objects3DPopulation populationZones = null;

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

    public TrackThreshold(int vmin, int vmax, int contrast, int st, int nbCla, int sta) {
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
        contrastMin = contrast;
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
        //IJ.log("Analysing histogram ...");
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
        //IJ.log("Finished");
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

    public void setImageMarkers(ImageInt imageMarkers) {
        this.imageMarkers = imageMarkers;
    }

    public void setImageZones(ImageInt imageZones) {
        this.imageZones = imageZones;
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
        this.markers = point3Ds;
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
            if (log != null) {
                log.log("Computing k-means");
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
            // if (checkMarkers(ob)) {
            if (checkMarkersTest(ob)) {
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
        BestCriterion bestCriterion;

        // test weka for Jaza dataset
        //criteria_method=CRITERIA_METHOD_MAX_CLASSIFICATION;


        switch (criteria_method) {
            case CRITERIA_METHOD_MIN_ELONGATION:
                criterion = new CriterionElongation();
                bestCriterion = new BestCriteriaMin();
                break;
            case CRITERIA_METHOD_MAX_COMPACTNESS:
                criterion = new CriterionCompactness();
                bestCriterion = new BestCriteriaMax();
                break;
            case CRITERIA_METHOD_MAX_VOLUME:
                criterion = new CriterionVolume();
                bestCriterion = new BestCriteriaMax();
                break;
            case CRITERIA_METHOD_MSER:
                criterion = new CriterionVolume();
                bestCriterion = new BestCriteriaStable();
                break;
            case CRITERIA_METHOD_MAX_EDGES:
                criterion = new CriterionEdge(img, 0.5);
                bestCriterion = new BestCriteriaMax();
                break;
            case CRITERIA_METHOD_MAX_CLASSIFICATION:
                criterion = new CriterionClassification("/home/thomasb/App/ImageJ/testWEKA.arff"); // test
                bestCriterion = new BestCriteriaMax();
                break;
            default: // MSER
                criterion = new CriterionVolume();
                bestCriterion = new BestCriteriaStable();
                break;
        }

        int T0, TMaximum, T1;
        int[] histogramThreshold;
        ImageLabeller labeler = new ImageLabeller(volMin, volMax);
        TMaximum = (int) img.getMax();

        if (log != null) log.log("Analysing histogram ...");
        histogramThreshold = initHistogram(img);
        T0 = histogramThreshold[0];

        // first frame
        T1 = T0;
        if (log != null) log.log("Computing frame for first threshold " + T1);
        ArrayList<ObjectTrack> frame1 = computeFrame(img, labeler.getObjects(img.thresholdAboveInclusive(T1)), markers, T1, criterion);

        if (log != null) log.log("Starting iterative thresholding ... ");

        ArrayList<ObjectTrack> allFrames = new ArrayList<ObjectTrack>();
        allFrames.addAll(frame1);
        // use histogram and unique values to loop over pixel values
        GlobalThreshold = 1;
        //
        Chrono chrono = new Chrono(TMaximum);
        chrono.start();
        String S = null;
        if (log instanceof IJLog) {
            ((IJLog) (log)).setUpdate(true);
        }
        while (T1 <= TMaximum) {
            int T2 = computeNextThreshold(T1, TMaximum, histogramThreshold);
            if (T2 < 0) break;

            if (log != null) {
                S = chrono.getFullInfo(T2 - T1);
                //IJ.log("\\Update: "+S);
                if (S != null) log.log(S);
            }
            //if (status) IJ.showStatus("Computing frame for threshold " + T2 + "                   ");

            // Threshold T2
            ImageHandler bin2 = img.thresholdAboveInclusive(T2);
            ImageHandler labels2 = labeler.getLabels(bin2);
            ArrayList<ObjectTrack> frame2 = computeFrame(img, labeler.getObjects(bin2), markers, T2, criterion);


            System.gc();
            // T2--> new T1
            T1 = T2;
            frame1 = computeAssociation(frame1, frame2, allFrames, labels2);

        } // thresholding
        if (log instanceof IJLog) {
            ((IJLog) (log)).setUpdate(false);
        }
        if (log != null) {
            log.log("Iterative Thresholding finished");
        }

        return computeResults(allFrames, img, bestCriterion);
    }

    private ArrayList<ImageHandler> computeResults(ArrayList<ObjectTrack> allFrames, ImageHandler img, BestCriterion bestCriterion) {
        // get results from the tree with different levels of objects
        int level = 1;
        int maxLevel = 10;
        ArrayList<ImageHandler> drawsReconstruct = new ArrayList<ImageHandler>();

        // delete low contrast

        while (deleteLowContrastTracks(allFrames, contrastMin)) ;

        while ((!allFrames.isEmpty()) && (level < maxLevel)) {
            if (log != null) {
                log.log("Nb total objects level " + level + " : " + allFrames.size());
            }
            ImageHandler drawIdx, drawContrast;
            // 32-bits case
            if (allFrames.size() < 65535) {
                drawIdx = new ImageShort("Objects", img.sizeX, img.sizeY, img.sizeZ);
                //drawContrast = new ImageShort("Contrast", img.sizeX, img.sizeY, img.sizeZ);
            } else {
                drawIdx = new ImageFloat("Objects", img.sizeX, img.sizeY, img.sizeZ);
                //drawContrast = new ImageFloat("Contrast", img.sizeX, img.sizeY, img.sizeZ);
            }
            drawsReconstruct.add(drawIdx);
            //drawsReconstruct.add(drawContrast);
            int idx = 1;
            ArrayList<ObjectTrack> toBeRemoved = new ArrayList<ObjectTrack>();
            for (ObjectTrack obt : allFrames) {
                if (obt.getState() == ObjectTrack.STATE_DIE) {
                    ObjectTrack anc = obt.getAncestor();
                    if (anc == null) {
                        anc = obt;
                    }
                    ArrayList<ObjectTrack> list = obt.getLineageTo(anc);
                    // compute contrast, max threshold - min threshold
                    int contrast = 0;
                    if (anc != null)
                        contrast = obt.getFrame() - anc.getFrame();

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
                    Object3D object3D = new Object3DVoxels(SegmentSpot.segmentSpotClassical(seed.getRoundX(), seed.getRoundY(), seed.getRoundZ(), threshold, idx));
                    object3D.draw(drawIdx, idx);
                    //object3D.draw(drawContrast, contrast);
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

    private int computeNextThreshold(int T1, int Tmax, int[] histogram) {
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

    private boolean deleteLowContrastTracks(ArrayList<ObjectTrack> allFrames, int contrastMin) {
        boolean change = false;
        if (contrastMin <= 0) return change;
        ArrayList<ObjectTrack> toBeRemoved = new ArrayList<ObjectTrack>();
        for (ObjectTrack objectTrack : allFrames) {
            if (objectTrack.getState() == ObjectTrack.STATE_DIE) {
                ObjectTrack anc = objectTrack.getAncestor();
                if (anc == null) {
                    anc = objectTrack;
                }
                // compute contrast, max threshold - min threshold
                int contrast = 0;
                if (anc != null)
                    contrast = objectTrack.getFrame() - anc.getFrame();
                if (contrast < contrastMin) {
                    toBeRemoved.addAll(objectTrack.getLineageTo(anc));
                    change = true;
                }
            }
        }
        allFrames.removeAll(toBeRemoved);

        return change;
    }

    public ImageHandler segment(ImageHandler input, boolean verbose) {
        setVerbose(verbose);
        ArrayList<ImageHandler> res = process(input);
        if (!res.isEmpty()) {
            return res.get(0);
        } else {
            return null;
        }
    }

    public ArrayList<ImageHandler> segmentAll(ImageHandler input, boolean verbose) {
        setVerbose(verbose);
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

    @Deprecated
    public void setVerbose(boolean verbose) {
        this.verbose = verbose;
    }

    public void setLog(AbstractLog abstractLog) {
        log = abstractLog;
    }

    private boolean checkMarkers(Object3D object3D) {
        // TO MODIFY
        if ((markers == null) || (object3D.insideOne(markers))) return true;

        return false;
    }

    // EXPERIMENTAL
    private boolean checkMarkersTest(Object3D object3D) {
        boolean mark = ((imageMarkers == null) || (object3D.includesMarkersOneOnly(imageMarkers)));
        boolean zone = ((imageZones == null) || (object3D.includedInZonesOneOnly(imageZones)));
        //zone=false;
        //if (zone) return mark;
        //double coloc = checkZoneColoc(object3D);
        //zone = (coloc > 60);
        //if(coloc<100) IJ.log("Zone Coloc " + coloc);

        return mark && zone;
    }

    private double checkZoneColoc(Object3D object3D) {
        if (populationZones == null) {
            populationZones = new Objects3DPopulation(imageZones);
        }
        ArrayUtil arrayUtil = object3D.listValues(imageZones);
        arrayUtil = arrayUtil.distinctValues();
        double maxColoc = 0;
        for (double z : arrayUtil.getArrayList()) {
            if (z == 0) continue;
            Object3D zone = populationZones.getObjectByValue((int) (z));
            double coloc = object3D.pcColoc(zone);
            if (coloc > maxColoc) maxColoc = coloc;
        }
        return maxColoc;
    }


}
