package mcib3d.geom;

import ij.measure.ResultsTable;
import mcib3d.image3d.ImageInt;
import mcib3d.utils.ArrayUtil;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Public class to compute (fast) co-localisation between two images
 */
public class Objects3DPopulationColocalisation {
    // populations
    private Objects3DPopulation population1;
    private Objects3DPopulation population2;
    // values indices
    HashMap<Integer, Integer> indices1 = null;
    HashMap<Integer, Integer> indices2 = null;
    // stored coloc information
    private int[][] colocs = null;

    private boolean needToComputeColoc = false;

    /**
     * Constructor with two populations of objects
     * @param population1
     * @param population2
     */
    public Objects3DPopulationColocalisation(Objects3DPopulation population1, Objects3DPopulation population2) {
        this.population1 = population1;
        this.population2 = population2;
        indices1 = new HashMap<Integer, Integer>();
        indices2 = new HashMap<Integer, Integer>();
        colocs = new int[population1.getNbObjects() + 1][population2.getNbObjects() + 1];
        needToComputeColoc = true;
    }

    /**
     * Compute the colocalisation between two images
     * @param image1 first image of labelled objects
     * @param image2 second image of labelled objects
     */
    private void computeColocalisationImage(ImageInt image1, ImageInt image2) {
        initColocalisation();
        int Xmax = Math.min(image1.sizeX, image2.sizeX);
        int Ymax = Math.min(image1.sizeY, image2.sizeY);
        int Zmax = Math.min(image1.sizeZ, image2.sizeZ);

        for (int z = 0; z < Zmax; z++) {
            for (int x = 0; x < Xmax; x++) {
                for (int y = 0; y < Ymax; y++) {
                    colocs[indices1.get(image1.getPixelInt(x, y, z))][indices2.get(image2.getPixelInt(x, y, z))]++;
                }
            }
        }
        needToComputeColoc = false;
    }

    /**
     * Initialize the colocalisation
     * creates the double array
     */
    private void initColocalisation() {
        // indices 1
        ArrayUtil arrayUtil = population1.getAllIndices();
        indices1.put(0, 0);
        for (int i = 0; i < arrayUtil.size(); i++) {
            indices1.put(arrayUtil.getValueInt(i), i + 1);
        }
        // indices 2
        arrayUtil = population2.getAllIndices();
        indices2.put(0, 0);
        for (int i = 0; i < arrayUtil.size(); i++) {
            indices2.put(arrayUtil.getValueInt(i), i + 1);
        }
        // zeroes the matrix of coloc
        for (int ia = 0; ia < population1.getNbObjects(); ia++) {
            for (int ib = 0; ib < population2.getNbObjects(); ib++) {
                colocs[ia][ib] = 0;
            }
        }
    }

    /**
     * get the results of colocalisatio as an ImageJ  Results Table
     * @param useValueObject use the original value of objects
     *                       else use incremaental values
     * @return the Results Table
     */
    public ResultsTable getResultsTable(boolean useValueObject) {
        if (needToComputeColoc) computeColocalisation();
        ResultsTable rt = new ResultsTable();
        // first raw background 0
        rt.incrementCounter();
        rt.setLabel("BG1", 0);
        rt.setValue("BG2", 0, colocs[0][0]);
        for (int ib = 0; ib < population2.getNbObjects(); ib++) {
            if (!useValueObject) {
                rt.setValue("B" + (ib + 1), 0, colocs[0][indices2.get(population2.getObject(ib).getValue())]);
            } else {
                rt.setValue("B" + population2.getObject(ib).getValue(), 0, colocs[0][indices2.get(population2.getObject(ib).getValue())]);
            }
        }
        for (int ia = 0; ia < population1.getNbObjects(); ia++) {
            rt.incrementCounter();
            if (!useValueObject) {
                rt.setLabel("A" + (ia + 1), ia + 1);
            } else {
                rt.setLabel("A" + population1.getObject(ia).getValue(), ia + 1);
            }
            rt.setValue("BG2", ia + 1, colocs[indices1.get(population2.getObject(ia).getValue())][0]);
            for (int ib = 0; ib < population2.getNbObjects(); ib++) {
                if (!useValueObject) {
                    rt.setValue("B" + ib, (ia + 1), colocs[indices1.get(population1.getObject(ia).getValue())][indices2.get(population2.getObject(ib).getValue())]);
                } else {
                    rt.setValue("B" + population2.getObject(ib).getValue(), ia + 1, colocs[indices1.get(population1.getObject(ia).getValue())][indices2.get(population2.getObject(ib).getValue())]);
                }
            }
        }
        return rt;
    }

    /**
     * Compute the colocalisation between the two populations
     */
    private void computeColocalisation() {
        ImageInt image1 = population1.drawPopulation();
        ImageInt image2 = population2.drawPopulation();

        computeColocalisationImage(image1, image2);
    }

    /**
     * Return the colocalisation between two indices (not necessarily objects values)
     * @param i1 index of first object in population 1
     * @param i2 index of second object in population 2
     * @return the colocalisation volume (intersection) between the two objects
     */
    public int getColocRaw(int i1, int i2) {
        if (needToComputeColoc) computeColocalisation();
        return colocs[i1][i2];
    }

    /**
     * Return thea colocalisation between two objects
     * @param object3D1 The object in population 1
     * @param object3D2 The object in population 2
     * @return the colocalisation volume (intersection) between the two objects
     */
    public int getColocObject(Object3D object3D1, Object3D object3D2) {
        if (needToComputeColoc) computeColocalisation();
        int v1 = object3D1.getValue();
        int v2 = object3D2.getValue();
        int i1 = indices1.get(v1);
        int i2 = indices2.get(v2);

        return colocs[i1][i2];
    }

    /**
     * Returns all the colocalisations
     * @return List of colocalisation as PairColocalisation
     */
    public ArrayList<PairColocalisation> getAllColocalisationPairs() {
        if (needToComputeColoc) computeColocalisation();
        ArrayList<PairColocalisation> pairColocalisations = new ArrayList<PairColocalisation>();
        for (int i1 = 0; i1 < population1.getNbObjects(); i1++) {
            for (int i2 = 0; i2 < population2.getNbObjects(); i2++) {
                int vol = colocs[indices1.get(population1.getObject(i1).getValue())][indices2.get(population2.getObject(i2).getValue())];
                if (vol > 0) {
                    PairColocalisation pairColocalisation = new PairColocalisation(population1.getObject(i1), population2.getObject(i2), vol);
                    pairColocalisations.add(pairColocalisation);
                }
            }
        }
        return pairColocalisations;
    }

    /**
     * Returns all the colocalisations for one specific object in population 1
     * @param object3D The objet in population 1
     * @return  List of colocalisation as PairColocalisation
     */
    public ArrayList<PairColocalisation> getObject1ColocalisationPairs(Object3D object3D) {
        if (needToComputeColoc) computeColocalisation();
        ArrayList<PairColocalisation> pairColocalisations = new ArrayList<PairColocalisation>();
        int i1 = population1.getIndexOf(object3D);
        for (int i2 = 0; i2 < population2.getNbObjects(); i2++) {
            int vol = colocs[indices1.get(population1.getObject(i1).getValue())][indices2.get(population2.getObject(i2).getValue())];
            if (vol > 0) {
                PairColocalisation pairColocalisation = new PairColocalisation(population1.getObject(i1), population2.getObject(i2), vol);
                pairColocalisations.add(pairColocalisation);
            }
        }
        return pairColocalisations;
    }
    /**
     * Returns all the colocalisations for one specific object in population 2
     * @param object3D The objet in population 2
     * @return  List of colocalisation as PairColocalisation
     */
    public ArrayList<PairColocalisation> getObject2ColocalisationPairs(Object3D object3D) {
        if (needToComputeColoc) computeColocalisation();
        ArrayList<PairColocalisation> pairColocalisations = new ArrayList<PairColocalisation>();
        int i2 = population2.getIndexOf(object3D);
        for (int i1 = 0; i1 < population1.getNbObjects(); i1++) {
            int vol = colocs[indices1.get(population1.getObject(i1).getValue())][indices2.get(population2.getObject(i2).getValue())];
            if (vol > 0) {
                PairColocalisation pairColocalisation = new PairColocalisation(population1.getObject(i1), population2.getObject(i2), vol);
                pairColocalisations.add(pairColocalisation);
            }
        }
        return pairColocalisations;
    }

    /**
     * Reset the colocalisation
     * In case some information changed in the populations
     */
    public void resetColocalisation() {
        needToComputeColoc = true;
    }
}
