package mcib3d.geom;

import ij.IJ;
import ij.measure.ResultsTable;
import mcib3d.image3d.ImageHandler;
import mcib3d.utils.ArrayUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;

public class Objects3DPopulationInteractions {
    public static final int methodLINE = 1;
    public static final int methodDILATE = 2;
    public static final int methodTOUCH = 3;
    // radii for dilation
    private float radxy = 1;
    private float radz = 1;
    // process
    private int method = methodLINE;
    private boolean needToComputeInteractions;
    private ImageHandler image;
    private Objects3DPopulation population;
    private HashMap<String, PairColocalisation> interactions;

    public Objects3DPopulationInteractions(ImageHandler image) {
        this.image = image;
    }

    public Objects3DPopulationInteractions(int method, ImageHandler image) {
        this.method = method;
        this.image = image;
    }

    public void setRadxyForDilateMethod(float radxy) {
        this.radxy = radxy;
    }

    public void setRadzForDilateMethod(float radz) {
        this.radz = radz;
    }

    public void setMethod(int method) {
        this.method = method;
    }

    private void interactionsDilate(float rx, float ry, float rz) {
        interactions = new HashMap<>();
        for (Object3D object3D : population.getObjectsList()) {
            int value = object3D.getValue();
            Object3D dilated = object3D.getDilatedObject(rx, ry, rz);
            LinkedList<Voxel3D> contours = dilated.getContours();
            ArrayUtil arrayUtil = new ArrayUtil(contours.size());
            int c = 0;
            for (Voxel3D voxel3D : contours) {
                if (image.contains(voxel3D)) {
                    arrayUtil.putValue(c, image.getPixel(voxel3D));
                    c++;
                }
            }
            arrayUtil.setSize(c);
            ArrayUtil distinctValues = arrayUtil.distinctValues();
            for (int i = 0; i < distinctValues.size(); i++) {
                int other = distinctValues.getValueInt(i);
                if ((other == 0) || (other == value)) continue;
                String key = value + "-" + other;
                if (other < value) key = other + "-" + value;
                if (!interactions.containsKey(key)) {
                    PairColocalisation pairColocalisation = new PairColocalisation(object3D, population.getObjectByValue(other));
                    interactions.put(key, pairColocalisation);
                }
                interactions.get(key).incrementVolumeColoc(arrayUtil.countValue(other));
            }
        }
        needToComputeInteractions = false;
    }

    private void interactionsDamLines() {
        interactions = new HashMap<>();
        for (int z = 0; z < image.sizeZ; z++) {
            for (int x = 0; x < image.sizeX; x++) {
                for (int y = 0; y < image.sizeY; y++) {
                    if (image.getPixel(x, y, z) == 0) {
                        ArrayUtil util = image.getNeighborhood3x3x3(x, y, z);
                        util = util.distinctValues();
                        int c = 0;
                        for (int i = 0; i < util.size(); i++) {
                            for (int j = i + 1; j < util.size(); j++) {
                                if ((util.getValueInt(i) > 0) && (util.getValueInt(j) > 0)) {
                                    String key = util.getValueInt(i) + "-" + util.getValueInt(j);
                                    if (!interactions.containsKey(key)) {
                                        PairColocalisation pairColocalisation = new PairColocalisation(population.getObjectByValue(util.getValueInt(i)), population.getObjectByValue(util.getValueInt(j)));
                                        interactions.put(key, pairColocalisation);
                                    }
                                    interactions.get(key).incrementVolumeColoc();
                                    c++;
                                }
                            }
                        }
                        if (c > 1) IJ.log("Multiple point " + c + " : " + x + " " + y + " " + z);
                    }
                }
            }
        }

        needToComputeInteractions = false;
    }

    private void interactionsContours() {
        interactions = new HashMap<>();
        for (Object3D object3D : population.getObjectsList()) {
            LinkedList<Voxel3D> list = object3D.getContours();
            for (Voxel3D voxel3D : list) {
                ArrayUtil util = image.getNeighborhood3x3x3(voxel3D.getRoundX(), voxel3D.getRoundY(), voxel3D.getRoundZ());
                util = util.distinctValues();
                int c = 0;
                for (int i = 0; i < util.size(); i++) {
                    for (int j = i + 1; j < util.size(); j++) {
                        if ((util.getValueInt(i) > 0) && (util.getValueInt(j) > 0)) {
                            if ((util.getValueInt(i) == object3D.getValue()) || (util.getValueInt(j) == object3D.getValue())) {
                                String key = util.getValueInt(i) + "-" + util.getValueInt(j);
                                if (!interactions.containsKey(key)) {
                                    PairColocalisation pairColocalisation = new PairColocalisation(population.getObjectByValue(util.getValueInt(i)), population.getObjectByValue(util.getValueInt(j)));
                                    interactions.put(key, pairColocalisation);
                                }
                                interactions.get(key).incrementVolumeColoc();
                                c++;
                            }
                        }
                    }
                }
                if (c > 1)
                    IJ.log("Multiple point " + c + " : " + voxel3D.getRoundX() + " " + voxel3D.getRoundY() + " " + voxel3D.getRoundZ());
            }
        }

        needToComputeInteractions = false;
    }

    public ResultsTable getResultsTableInteractions(boolean useValueObject) {
        if (needToComputeInteractions) computeInteractions();
        IJ.log("Interactions completed, building results table");
        ResultsTable rt = ResultsTable.getResultsTable();
        if (rt == null) rt = new ResultsTable();
        rt.reset();
        for (int ia = 0; ia < population.getNbObjects(); ia++) {
            Object3D object1 = population.getObject(ia);
            rt.incrementCounter();
            if (!useValueObject) {
                rt.setLabel("A" + ia, ia);
            } else {
                rt.setLabel("A" + object1.getValue(), ia);
            }
            ArrayList<PairColocalisation> list = getObject1ColocalisationPairs(object1);
            if (list.size() == 0) {
                if (!useValueObject)
                    rt.setValue("O1", ia, 0);
                else
                    rt.setValue("O1", ia, 0);
                rt.setValue("V1", ia, 0);
            }
            for (int c = 0; c < list.size(); c++) {
                PairColocalisation colocalisation = list.get(c);
                if (colocalisation.getObject3D1() != object1) IJ.log("Pb colocalisation " + object1);
                Object3D object2 = colocalisation.getObject3D2();
                int i2 = population.getIndexOf(object2);
                if (!useValueObject)
                    rt.setValue("O" + (c + 1), ia, i2);
                else
                    rt.setValue("O" + (c + 1), ia, object2.getValue());
                rt.setValue("V" + (c + 1), ia, colocalisation.getVolumeColoc());
            }
        }
        return rt;
    }

    private void computeInteractions() {
        switch (method) {
            case methodDILATE:
                interactionsDilate(radxy, radxy, radz);
                break;
            case methodTOUCH:
                interactionsContours();
                break;
            case methodLINE:
            default:
                interactionsDamLines();
                break;
        }
    }

    private ArrayList<PairColocalisation> getObject1ColocalisationPairs(Object3D object3D) {
        ArrayList<PairColocalisation> pairColocalisations = new ArrayList<PairColocalisation>();
        int i1 = object3D.getValue();
        for (String key : interactions.keySet()) {
            if (key.startsWith("" + i1 + "-"))
                pairColocalisations.add(new PairColocalisation(interactions.get(key).getObject3D1(), interactions.get(key).getObject3D2(), interactions.get(key).getVolumeColoc()));
        }

        return pairColocalisations;
    }
}
