package mcib3d.geom;

import ij.IJ;
import ij.measure.ResultsTable;
import mcib3d.image3d.ImageHandler;
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
    private HashMap<String, PairColocalisation> colocs;

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
        // init colocs
        colocs = new HashMap<>();
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
    private void computeColocalisationImage(ImageHandler image1, ImageHandler image2) {
        int Xmax = Math.min(image1.sizeX, image2.sizeX);
        int Ymax = Math.min(image1.sizeY, image2.sizeY);
        int Zmax = Math.min(image1.sizeZ, image2.sizeZ);

        for (int k = 0; k < Zmax; k++) {
            //IJ.showStatus("Colocalisation " + k);
            for (int x = 0; x < Xmax; x++) {
                for (int y = 0; y < Ymax; y++) {
                    int pix1 = (int) image1.getPixel(x, y, k);
                    int pix2 = (int) image2.getPixel(x, y, k);
                    if ((pix1 > 0) && (pix2 > 0)) {
                        incrementColoc(pix1, pix2);
                    }
                }
            }
        }


        needToComputeColoc = false;
    }

    private void incrementColoc(int val1, int val2) {
        String key = "" + val1 + "-" + val2;
        if (!colocs.containsKey(key))
            colocs.put(key, new PairColocalisation(population1.getObjectByValue(val1), population2.getObjectByValue(val2)));
        colocs.get(key).incrementVolumeColoc();
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
                int coloc;
                String key = population1.getObject(ia).getValue() + "-" + population2.getObject(ib).getValue();
                if (colocs.containsKey(key))
                    coloc = colocs.get(key).getVolumeColoc();
                else coloc = 0;
                if (ia == 0) {
                    if (!useValueObject) {
                        rt.setValue("B" + ib, ia, coloc);
                        colums.put("B" + ib, rt.getColumnIndex("B" + ib));
                    } else {
                        int v2 = population2.getObject(ib).getValue();
                        rt.setValue("B" + v2, ia, coloc);
                        colums.put("B" + v2, rt.getColumnIndex("B" + v2));
                    }
                } else {
                    if (!useValueObject) {
                        rt.setValue(colums.get("B" + ib), ia, coloc);
                    } else {
                        int v2 = population2.getObject(ib).getValue();
                        rt.setValue(colums.get("B" + v2), ia, coloc);
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
                if (!useValueObject)
                    rt.setValue("O" + (c + 1), ia, i2);
                else
                    rt.setValue("O" + (c + 1), ia, object2.getValue());
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
            ImageHandler image1 = population1.drawPopulation();
            ImageHandler image2 = population2.drawPopulation();

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
        int coloc;
        String key = population1.getObject(i1).getValue() + "-" + population2.getObject(i2).getValue();
        if (colocs.containsKey(key))
            coloc = colocs.get(key).getVolumeColoc();
        else coloc = 0;
        return coloc;
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
        int coloc;
        String key = v1 + "-" + v2;
        if (colocs.containsKey(key))
            coloc = colocs.get(key).getVolumeColoc();
        else coloc = 0;

        return coloc;
    }

    /**
     * Return the colocalisation between two objects
     *
     * @param v1 The object value in population 1
     * @param v2 The object value in population 2
     * @return the colocalisation volume (intersection) between the two objects
     */
    public int getColocObject(int v1, int v2) {
        if (needToComputeColoc) computeColocalisation();
        int coloc = 0;
        String key = v1 + "-" + v2;
        if (colocs.containsKey(key))
            coloc = colocs.get(key).getVolumeColoc();

        return coloc;
    }



    /**
     * Returns all the colocalisations
     *
     * @return List of colocalisation as PairColocalisation
     */
    public ArrayList<PairColocalisation> getAllColocalisationPairs() {
        if (needToComputeColoc) computeColocalisation();
        ArrayList<PairColocalisation> pairColocalisations = new ArrayList<PairColocalisation>();
        for (String key : colocs.keySet()) {
            pairColocalisations.add(new PairColocalisation(colocs.get(key).getObject3D1(), colocs.get(key).getObject3D2(), colocs.get(key).getVolumeColoc()));
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
        int i1 = object3D.getValue();
        for (String key : colocs.keySet()) {
            if (key.startsWith("" + i1 + "-"))
                pairColocalisations.add(new PairColocalisation(colocs.get(key).getObject3D1(), colocs.get(key).getObject3D2(), colocs.get(key).getVolumeColoc()));
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
        int i2 = object3D.getValue();
        for (String key : colocs.keySet()) {
            if (key.endsWith("-" + i2))
                pairColocalisations.add(new PairColocalisation(colocs.get(key).getObject3D1(), colocs.get(key).getObject3D2(), colocs.get(key).getVolumeColoc()));
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
