package mcib3d.geom;

import ij.IJ;
import ij.measure.ResultsTable;
import mcib3d.image3d.ImageInt;

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
    //private HashMap<Integer, Integer> indices1 = null;
    //private HashMap<Integer, Integer> indices2 = null;
    // stored coloc information
    private int[][] colocs;

    private boolean needToComputeColoc = false;

    /**
     * Constructor with two populations of objects
     *
     * @param population1
     * @param population2
     */
    public Objects3DPopulationColocalisation(Objects3DPopulation population1, Objects3DPopulation population2) {
        this.population1 = population1;
        this.population2 = population2;
        colocs = new int[population1.getNbObjects()][population2.getNbObjects()];
        for (int i1 = 0; i1 < population1.getNbObjects(); i1++)
            for (int i2 = 0; i2 < population2.getNbObjects(); i2++)
                colocs[i1][i2] = 0;
        population1.updateNamesAndValues();
        population2.updateNamesAndValues();
        needToComputeColoc = true;
    }

    /**
     * Compute the colocalisation between two images
     *
     * @param image1 first image of labelled objects
     * @param image2 second image of labelled objects
     */
    private void computeColocalisationImage(ImageInt image1, ImageInt image2) {
        int Xmax = Math.min(image1.sizeX, image2.sizeX);
        int Ymax = Math.min(image1.sizeY, image2.sizeY);
        int Zmax = Math.min(image1.sizeZ, image2.sizeZ);

        for (int k = 0; k < Zmax; k++) {
            //IJ.showStatus("Colocalisation " + k);
            for (int x = 0; x < Xmax; x++) {
                for (int y = 0; y < Ymax; y++) {
                    int pix1 = image1.getPixelInt(x, y, k);
                    int pix2 = image2.getPixelInt(x, y, k);
                    if ((pix1 > 0) && (pix2 > 0)) {
                        colocs[population1.getObjectIndex(pix1)][population2.getObjectIndex(pix2)]++;
                    }
                }
            }
        }


        needToComputeColoc = false;
    }


    /**
     * get the results of colocalisation as an ImageJ  Results Table
     *
     * @param useValueObject use the original value of objects
     *                       else use incremental values
     * @return the Results Table
     */
    public ResultsTable getResultsTableAll(boolean useValueObject) {
        if (needToComputeColoc) computeColocalisation();
        IJ.log("Colocalisation completed, building results table");
        ResultsTable rt = ResultsTable.getResultsTable();
        if (rt == null) rt = new ResultsTable();
        rt.reset();
        // temp colum index
        HashMap<String, Integer> colums = new HashMap<>(population2.getNbObjects());
        for (int ia = 0; ia < population1.getNbObjects(); ia++) {
            rt.incrementCounter();
            if (!useValueObject) {
                rt.setLabel("A" + ia, ia);
            } else {
                rt.setLabel("A" + population1.getObject(ia).getValue(), ia);
            }
            for (int ib = 0; ib < population2.getNbObjects(); ib++) {
                if (ia == 0) {
                    if (!useValueObject) {
                        rt.setValue("B" + ib, ia, colocs[ia][ib]);
                        colums.put("B" + ib, rt.getColumnIndex("B" + ib));
                    } else {
                        int v2 = population2.getObject(ib).getValue();
                        rt.setValue("B" + v2, ia, colocs[ia][ib]);
                        colums.put("B" + v2, rt.getColumnIndex("B" + v2));
                    }
                } else {
                    if (!useValueObject) {
                        rt.setValue(colums.get("B" + ib), ia, colocs[ia][ib]);
                    } else {
                        int v2 = population2.getObject(ib).getValue();
                        rt.setValue(colums.get("B" + v2), ia, colocs[ia][ib]);
                    }
                }
            }
        }
        return rt;
    }

    public ResultsTable getResultsTableOnlyColoc(boolean useValueObject) {
        if (needToComputeColoc) computeColocalisation();
        IJ.log("Colocalisation completed, building results table");
        ResultsTable rt = ResultsTable.getResultsTable();
        if (rt == null) rt = new ResultsTable();
        rt.reset();
        for (int ia = 0; ia < population1.getNbObjects(); ia++) {
            Object3D object1 = population1.getObject(ia);
            rt.incrementCounter();
            if (!useValueObject) {
                rt.setLabel("A" + ia, ia);
            } else {
                rt.setLabel("A" + object1.getValue(), ia);
            }
            ArrayList<PairColocalisation> list = getObject1ColocalisationPairs(object1);
            for (int c = 0; c < list.size(); c++) {
                PairColocalisation colocalisation = list.get(c);
                if (colocalisation.getObject3D1() != object1) IJ.log("Pb colocalisation " + object1);
                Object3D object2 = colocalisation.getObject3D2();
                int i2 = population2.getIndexOf(object2);
                rt.setValue("O" + (c + 1), ia, i2);
                rt.setValue("V" + (c + 1), ia, colocalisation.getVolumeColoc());
                rt.setValue("P" + (c + 1), ia, (double) colocalisation.getVolumeColoc() / (double) object1.getVolumePixels());
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
     *
     * @param i1 index of first object in population 1
     * @param i2 index of second object in population 2
     * @return the colocalisation volume (intersection) between the two objects
     */
    public int getColocRaw(int i1, int i2) {
        if (needToComputeColoc) computeColocalisation();
        return colocs[i1][i2];
    }

    /**
     * Return the colocalisation between two objects
     *
     * @param object3D1 The object in population 1
     * @param object3D2 The object in population 2
     * @return the colocalisation volume (intersection) between the two objects
     */
    public int getColocObject(Object3D object3D1, Object3D object3D2) {
        if (needToComputeColoc) computeColocalisation();
        int v1 = object3D1.getValue();
        int v2 = object3D2.getValue();

        return colocs[population1.getObjectIndex(v1)][population2.getObjectIndex(v2)];
    }

    /**
     * Returns all the colocalisations
     *
     * @return List of colocalisation as PairColocalisation
     */
    public ArrayList<PairColocalisation> getAllColocalisationPairs() {
        if (needToComputeColoc) computeColocalisation();
        ArrayList<PairColocalisation> pairColocalisations = new ArrayList<PairColocalisation>();
        for (int i1 = 0; i1 < population1.getNbObjects(); i1++) {
            for (int i2 = 0; i2 < population2.getNbObjects(); i2++) {
                int vol = colocs[i1][i2];
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
     *
     * @param object3D The objet in population 1
     * @return List of colocalisation as PairColocalisation
     */
    public ArrayList<PairColocalisation> getObject1ColocalisationPairs(Object3D object3D) {
        if (needToComputeColoc) computeColocalisation();
        ArrayList<PairColocalisation> pairColocalisations = new ArrayList<PairColocalisation>();
        int i1 = population1.getObjectIndex(object3D.getValue());
        for (int i2 = 0; i2 < population2.getNbObjects(); i2++) {
            if (i1 >= colocs.length) IJ.log(" " + i1 + " " + colocs.length);
            if (i2 >= colocs[0].length) {
                IJ.log(" " + i1 + " " + colocs.length + " " + population1.getNbObjects());
                IJ.log(" " + i2 + " " + colocs[0].length + " " + population2.getNbObjects());
            }

            int vol = colocs[i1][i2];
            if (vol > 0) {
                PairColocalisation pairColocalisation = new PairColocalisation(population1.getObject(i1), population2.getObject(i2), vol);
                pairColocalisations.add(pairColocalisation);
            }
        }
        return pairColocalisations;
    }

    /**
     * Returns all the colocalisations for one specific object in population 2
     *
     * @param object3D The objet in population 2
     * @return List of colocalisation as PairColocalisation
     */
    public ArrayList<PairColocalisation> getObject2ColocalisationPairs(Object3D object3D) {
        if (needToComputeColoc) computeColocalisation();
        ArrayList<PairColocalisation> pairColocalisations = new ArrayList<PairColocalisation>();
        int i2 = population2.getIndexOf(object3D);
        for (int i1 = 0; i1 < population1.getNbObjects(); i1++) {
            int vol = colocs[i1][i2];
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
