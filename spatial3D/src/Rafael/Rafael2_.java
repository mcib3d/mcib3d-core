/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Rafael;

import ij.IJ;
import ij.ImagePlus;
import ij.WindowManager;
import ij.measure.ResultsTable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import mcib3d.geom.Object3D;
import mcib3d.geom.Objects3DPopulation;
import mcib3d.image3d.ImageHandler;
import mcib3d.image3d.ImageInt;
import mcib3d.image3d.ImageShort;
import mcib3d.utils.ArrayUtil;
import mcib_plugins.analysis.spatialAnalysis;

/**
 *
 * @author thomasb
 */
public class Rafael2_ implements ij.plugin.PlugIn {

    private final int UNLABELED = -1;
    private final int ALPHA = 0;
    private final int BETA = 1;
    private final int DELTA = 2;

    private Objects3DPopulation popRegions = null;
    private Objects3DPopulation popNuclei = null;

    ImageHandler[] signals;

    ArrayList<Cell> popCells = null;

    HashMap<Integer, Cell> region2Cell;

    public void run(String arg) 
    {
     
        ImageInt imgWat = ImageInt.wrap(WindowManager.getImage("DAPI-seg-wat.tif"));
        ImageInt imgSeg = ImageInt.wrap(WindowManager.getImage("DAPI-seg.tif"));
        ImageInt imgAlpha = ImageInt.wrap(WindowManager.getImage("alpha-bin.tif"));
        if (imgAlpha.getMax() == 255) {
            imgAlpha.divideByValue(255);// 0-1
        }
        ImageInt imgBeta = ImageInt.wrap(WindowManager.getImage("beta-bin.tif"));
        if (imgBeta.getMax() == 255) {
            imgBeta.divideByValue(255);// 0-1
        }
        ImageInt imgDelta = ImageInt.wrap(WindowManager.getImage("delta-bin.tif"));
        if (imgDelta.getMax() == 255) {
            imgDelta.divideByValue(255);// 0-1
        }
        // signals
        signals = new ImageHandler[]{imgAlpha, imgBeta, imgDelta};

        IJ.log("Initialization ...");
        initCells(imgSeg, imgWat);
        IJ.log("Type ...");
        computeTypeCells(10);
        // random
        randomizeCellType();
        IJ.log("Association1 ...");
        computeAssoCells(imgWat, 1);
        IJ.log("Association2 ...");
        computeAssoCells2();

        // draw 
        drawCellTypes(false).show("TYPE");
        //drawCellNuclei().show("NUC");
        drawCellRegions().show("REG");
        drawCellsContacts(BETA).show("BD");
        drawCellsContacts(ALPHA).show("AD");
        calculStatistic(DELTA);
        computeResults();

    }

    /**
     * ttnhoa
     * dess: function to calcul the distribution of cell.
     * @param typeObserved : ALPHA, BETA, DELTA
     * 
     */
    private void calculStatistic(int typeObserved) {
        ImageHandler draw = new ImageShort("Statistic", signals[0].sizeX, signals[0].sizeY, signals[0].sizeZ);
        
        ImageHandler espaceObserved = new ImageShort("Espace Observed", signals[0].sizeX, signals[0].sizeY, signals[0].sizeZ);
        for (int z = 0; z < espaceObserved.sizeZ; z++) 
        {
                for (int xy = 0; xy < espaceObserved.sizeXY; xy++) 
                {
                      espaceObserved.setPixel(xy, z, 255);
                }
        }
        
        for (Cell C : popCells) {
            int val = 0;
            if (C.type == typeObserved) {
                val = 255;
                C.nucleus.draw(draw, val);
            } 
        }
        int numPoints = 1000;
        int numRandomSamples = 100;
        double distHardCore = 0;
        double env = 0.05;

        ImagePlus imagePlus = draw.duplicate().getImagePlus();
        ImageHandler img = espaceObserved.duplicate();
        ImagePlus imgMask = img.getImagePlus();

        spatialAnalysis spa = new spatialAnalysis(numPoints, numRandomSamples, distHardCore, env);
        spa.processAll(imagePlus, imgMask, true,true);
    }
    
    /**
     * ttnhoa
     * alpha-delta, beta-delta contact
     * @param typeObserved : ALPHA, BETA
     * @return 
     */
    private ImageHandler drawCellsContacts(int typeObserved) {
        ImageHandler draw = new ImageShort("DeltaContact", signals[0].sizeX, signals[0].sizeY, signals[0].sizeZ);

        for (Cell C : popCells) {
            int col = 0;
            if (C.type == DELTA) {
                col = 4;
                C.region.draw(draw, col);
            } else if (C.type == typeObserved) {
                boolean co = C.hasContact1(DELTA);
                if (co) {
                    col = 1;
                } else {
                    co = C.hasContact2(DELTA);
                    if (co) {
                        col = 2;
                    } else {
                        col = 3;
                    }
                }
                C.region.draw(draw, col);
            }
        }

        return draw;
    }

    private void computeResults()
    {
        ResultsTable rtALPHA = new ResultsTable();
        ResultsTable rtBETA = new ResultsTable();
        ResultsTable rtDELTA = new ResultsTable();
        int rA = -1, rB = -1, rD = -1;

        for (Cell C : popCells) {
            Object3D obj = C.nucleus;
            ResultsTable rt = null;
            int c = 0;
            if (C.type == ALPHA) {
                rt = rtALPHA;
                rA++;
                c = rA;
            } else if (C.type == BETA) {
                rt = rtBETA;
                rB++;
                c = rB;
            } else if (C.type == DELTA) {
                rt = rtDELTA;
                rD++;
                c = rD;
            }
            if (rt != null) {
                rt.incrementCounter();
                rt.setValue("type", c, C.type);
                rt.setValue("val", c, C.id + 1);
                rt.setValue("vol", c, obj.getVolumeUnit());
                rt.setValue("diam", c, obj.getDistCenterMean());
                rt.setValue("elon", c, obj.getMainElongation());
                int[] nei = C.computeNei1Type();
                rt.setValue("Nei1_A", c, nei[0]);
                rt.setValue("Nei1_B", c, nei[1]);
                rt.setValue("Nei1_D", c, nei[2]);
                nei = C.computeNei2Type();
                rt.setValue("Nei2_A", c, nei[0]);
                rt.setValue("Nei2_B", c, nei[1]);
                rt.setValue("Nei2_D", c, nei[2]);

//                I WILL HAVE TO REDO THIS PART
                   // closest D 
//                Object3D cloD = popD.closestCenter(obj, 0);
//                if (cloD == null) {
//                    rt.setValue("cloD", c, -1);
//                    rt.setValue("cloDi", c, -1);
//                } else {
//                    rt.setValue("cloD", c, obj.distCenterUnit(cloD));
//                    rt.setValue("cloDi", c, cloD.getValue());
//                }
            }
        }
        rtALPHA.show("Alpha");
        rtBETA.show("Beta");
        rtDELTA.show("Delta");
    }

    private ArrayList<Integer>[] computeAsso(ImageHandler img, int BorderValue) {
        //ImagePlus imp = WindowManager.getCurrentImage();
        //ImageHandler img = ImageHandler.wrap(imp);
        int max = (int) img.getMax();

        ArrayList<Integer>[] nei = new ArrayList[max + 1];
        for (int i = 0; i < nei.length; i++) {
            nei[i] = new ArrayList();
        }

        for (int x = 0; x < img.sizeX; x++) {
            for (int y = 0; y < img.sizeY; y++) {
                for (int z = 0; z < img.sizeZ; z++) {
                    if (img.getPixel(x, y, z) == BorderValue) {
                        ArrayUtil tab = img.getNeighborhood3x3x3(x, y, z);
                        tab = tab.distinctValues();
                        for (int j = 0; j < tab.getSize(); j++) {
                            int val = (int) tab.getValue(j);
                            if (!tab.hasOnlyValuesInt(nei[val])) {
                                for (int i = 0; i < tab.getSize(); i++) {
                                    if (!nei[val].contains((int) tab.getValue(i))) {
                                        nei[val].add((int) tab.getValue(i));
//                                        if (val == 9) {
//                                            IJ.log(" " + tab);
//                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        return nei;
    }

    private void randomizeCellType(){
        byte[] type = new byte[popCells.size()];
        int count = 0;
        for(int i =0; i<popCells.size(); i++){
            type[count] = popCells.get(i).type;
            count++;
        }
        byte[] type1 = randomizeType(type);
        for(int k=0; k< type1.length; k++){
            popCells.get(k).type = type1[k];
        }
        
    }
    // HI HOA TRY TO REWRITE THIS PART USING CELLS ;-)
    private byte[] randomizeType(byte[] type) {
        ArrayList<Integer> okcells = new ArrayList();
        byte[] rtype = new byte[type.length];
        for (int i = 0; i < type.length; i++) 
        {
            rtype[i] = type[i];
            if (type[i] != UNLABELED) {
                okcells.add(i);
            }
        }
        ArrayList<Integer> okcellsorig = new ArrayList();
        okcellsorig.addAll(okcells);
        
        //shuffle element in this list
        Collections.shuffle(okcells);
        //Utils.shuffleFisherYates(okcells);
        //Utils.shuffleList(okcells);
        //Utils.shuffleNaiveSwapI2Random(okcells);
        //Utils.shuffleNaiveSwapRandom2Random(okcells);
        
        for (int i = 0; i < okcells.size(); i++) {
            rtype[okcells.get(i)] = type[okcellsorig.get(i)];
        }

        return rtype;
    }

    private void initCells(ImageInt nucLabel, ImageInt regionLabel) 
    {
        popNuclei = new Objects3DPopulation(nucLabel);
        popRegions = new Objects3DPopulation(regionLabel, 1); // exclude value 1 used by borders
        popCells = new ArrayList<Cell>(popRegions.getNbObjects());
        region2Cell = new HashMap<Integer, Cell>(popRegions.getNbObjects());

        // get nucleus label for each region
        int c = 0;
        for (Object3D region : popRegions.getObjectsList()) 
        {
            int nuc = (int) region.getPixModeNonZero(nucLabel);
            Cell cell = new Cell();
            cell.region = region;
            cell.nucleus = popNuclei.getObjectByValue(nuc);
            popCells.add(cell);
            cell.id = c++;
            region2Cell.put(region.getValue(), cell);
        }
    }

    private void computeTypeCells(double threshold) 
    {
        for (Cell C : popCells) {
            C.type = -1;
            Object3D region = C.region;
            Object3D nucleus = C.nucleus;
            double maxIDs = 0;
            for (byte i = 0; i < signals.length; i++) {
                double ID = region.getIntegratedDensity(signals[i]);
                //double ID = nucleus.getLayerObject(0, 2).getIntegratedDensity(signals[i]);
                if ((ID > maxIDs) && (ID > threshold)) {
                    maxIDs = ID;
                    C.type = i;
                }
            }
        }
    }

    private void computeAssoCells(ImageHandler img, int borderValue) {

        ArrayList<Integer>[] nei = computeAsso(img, borderValue);

        // index refers to region label
        for (int i = 0; i < nei.length; i++) {
            if (nei[i].isEmpty()) {
                continue;
            }
            Cell C = region2Cell.get(i);
            if (C != null) {
                C.nei1 = new ArrayList<Cell>();
                for (int j : nei[i]) {
                    Cell C1 = region2Cell.get(j);
                    if ((C1 != null) && (C1 != C)) {
                        C.nei1.add(C1);
                    }
                }
            }
        }
    }

    private void computeAssoCells2() {
        for (Cell C : popCells) {
            C.nei2 = new ArrayList<Cell>();
            for (Cell C1 : C.nei1) {
                for (Cell C2 : C1.nei1) {
                    if ((C2 != C) && (!C.nei2.contains(C2))) {
                        C.nei2.add(C2);
                    }
                }
            }
        }
    }

    private ImageHandler drawCellTypes(boolean nuc) {
        ImageHandler draw = new ImageShort("TYPE", signals[0].sizeX, signals[0].sizeY, signals[0].sizeZ);

        for (Cell C : popCells) {
            if (nuc) {
                C.nucleus.draw(draw, C.type + 2);
            } else {
                C.region.draw(draw, C.type + 2);
            }
        }

        return draw;
    }

    private ImageHandler drawCellNuclei() {
        ImageHandler draw = new ImageShort("NUC", signals[0].sizeX, signals[0].sizeY, signals[0].sizeZ);

        for (Cell C : popCells) {
            C.nucleus.draw(draw, C.id + 1);
        }
        return draw;
    }

    private ImageHandler drawCellRegions() {
        ImageHandler draw = new ImageShort("REG", signals[0].sizeX, signals[0].sizeY, signals[0].sizeZ);

        for (Cell C : popCells) {
            C.region.draw(draw, C.id + 1);
        }

        return draw;
    }

    private class Cell {

        int id;
        Object3D nucleus;
        Object3D region;
        byte type;

        ArrayList<Cell> nei1 = null;
        ArrayList<Cell> nei2 = null;

        @Override
        public String toString() {
            return "(" + nucleus.getValue() + ", " + region.getValue() + ", " + type + ")";
        }

        public int[] computeNei1Type() {
            int[] res = new int[3];
            for (Cell C : nei1) {
                if (C.type > 0) {
                    res[C.type]++;
                }
            }

            return res;
        }

        public int[] computeNei2Type() {
            int[] res = new int[3];
            for (Cell C : nei2) {
                if (C.type > 0) {
                    res[C.type]++;
                }
            }

            return res;
        }

        public boolean hasContact1(int type) 
        {   
            for (Cell N : nei1) {
                if (N.type == type) {
                    return true;
                }
            }
            return false;
        }

        public boolean hasContact2(int type) 
        {
            for (Cell N : nei2) {
                if (N.type == type) {
                    return true;
                }
            }
            return false;
        }

    }
}
