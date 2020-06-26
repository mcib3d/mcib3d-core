package mcib3d.tracking_dev;

import ij.IJ;
import ij.measure.ResultsTable;
import mcib3d.geom.Object3D;
import mcib3d.geom.Object3DVoxels;
import mcib3d.geom.Objects3DPopulation;
import mcib3d.image3d.ImageHandler;

import java.util.ArrayList;
import java.util.HashMap;


public class Association {
    double[][] costs;
    Objects3DPopulation population1;
    Objects3DPopulation population2;
    AssociationCost associationCost;
    double NOASSO = 1000;
    // temp list
    HashMap<String, Double> CostsAll = null;
    HashMap<String, Double> CostsOK;
    HashMap<String, Double> CostsCompute;
    ArrayList<String> Orphan1;
    ArrayList<String> Orphan2;
    boolean verbose = false;


    public Association(Objects3DPopulation population1, Objects3DPopulation population2, AssociationCost associationCost) {
        this.population1 = population1;
        this.population2 = population2;
        this.associationCost = associationCost;
    }

    public static int getValue1FromAsso(String asso) {
        int pos = asso.indexOf("-");
        if (pos < 0) return -1;
        String valS = asso.substring(0, pos);
        int val = Integer.parseInt(valS);

        return val;
    }

    public static int getValue2FromAsso(String asso) {
        int pos = asso.indexOf("-");
        if (pos < 0) return -1;
        String valS = asso.substring(pos + 1);
        int val = Integer.parseInt(valS);

        return val;
    }

    public static int[] getValues(String s) {
        String[] split = s.split("-");
        int val1 = Integer.parseInt(split[0]);
        int val2 = Integer.parseInt(split[1]);

        return new int[]{val1, val2};
    }

    public void drawAssociation(ImageHandler draw) {
        if (CostsAll == null) computeAssociation();

        int max = 0;
        for (String a : CostsOK.keySet()) {
            int[] vals = Association.getValues(a);
            int val1 = vals[0];
            int val2 = vals[1];
            Object3D object3D2 = population2.getObjectByValue(val2);
            //object3D2.setValue(val1);
            object3D2.draw(draw, val1);
            if (val1 > max) max = val1;
        }
        // orphan2
        for (String orphan : Orphan2) {
            int val = Integer.parseInt(orphan);
            max++;
            Object3D object3D2 = population2.getObjectByValue(val);
            //object3D2.setValue(max);
            object3D2.draw(draw, max);
        }
    }

    public void drawAssociationPath(ImageHandler draw, ImageHandler path, ImageHandler track) {
        if (CostsAll == null) computeAssociation();

        // normal association
        for (String a : CostsOK.keySet()) {
            int[] vals = Association.getValues(a);
            int val1 = vals[0];
            int val2 = vals[1];
            Object3D object3D2 = population2.getObjectByValue(val2);
            // get object in pop1 and value in path
            Object3D object3D1 = population1.getObjectByValue(val1);
            int pathValue = (int) path.getPixel(((Object3DVoxels) object3D1).getFirstVoxel());
            object3D2.draw(draw, pathValue);
        }
        // orphan2
        for (String orphan : Orphan2) {
            int val = Integer.parseInt(orphan);
            Object3D object3D2 = population2.getObjectByValue(val);
            int pathValue = (int) track.getPixel(((Object3DVoxels) object3D2).getFirstVoxel());
            object3D2.draw(draw, pathValue);
        }
    }

    public void drawOrphan1(ImageHandler draw) {
        if (CostsAll == null) computeAssociation();

        // orphan1
        for (String orphan : Orphan1) {
            int val = Integer.parseInt(orphan);
            Object3D object3D1 = population1.getObjectByValue(val);
            object3D1.draw(draw);
        }
    }

    public void drawOrphan2(ImageHandler draw) {
        if (CostsAll == null) computeAssociation();

        // orphan2
        for (String orphan : Orphan2) {
            int val = Integer.parseInt(orphan);
            Object3D object3D2 = population2.getObjectByValue(val);
            object3D2.draw(draw);
        }
    }

    public ResultsTable getAssociationTable() {
        ResultsTable rt = ResultsTable.getResultsTable();
        if (rt == null) rt = new ResultsTable();

        // normal asso
        int row = rt.getCounter();
        for (String s : CostsOK.keySet()) {
            Object3D object3D1 = getObject3D1fromAsso(s);
            Object3D object3D2 = getObject3D2fromAsso(s);
            rt.setValue("Label1", row, object3D1.getValue());
            rt.setValue("Label2", row, object3D2.getValue());
            rt.setValue("CostAsso", row, CostsOK.get(s));
            row++;
        }
        // orphan1
        row = rt.getCounter();
        for (String s : getOrphan1()) {
            rt.setValue("Label1", row, s);
            rt.setValue("Label2", row, 0);
            rt.setValue("CostAsso", row, 0);
            row++;
        }
        // orphan2
        row = rt.getCounter();
        for (String s : getOrphan2()) {
            rt.setValue("Label1", row, 0);
            rt.setValue("Label2", row, s);
            rt.setValue("CostAsso", row, 0);
            row++;
        }

        return rt;
    }

    public void computeAssociation() {
        CostsAll = new HashMap<>();
        CostsOK = new HashMap<>();
        CostsCompute = new HashMap<>();
        computePairCosts();
        associationOK();
        computeAssociationsCosts();
        // display results
        if (verbose) {
            IJ.log("");
            IJ.log("Final associations");
            for (String s : CostsOK.keySet()) {
                IJ.log(s + " " + CostsOK.get(s));
            }
        }
        // not associated 1
        Orphan1 = new ArrayList();
        for (int i1 = 0; i1 < population1.getNbObjects(); i1++) {
            String match = population1.getObject(i1).getValue() + "-";
            boolean found = false;
            for (String s : CostsOK.keySet()) {
                if (s.startsWith(match)) {
                    found = true;
                    break;
                }
            }
            if (!found) {
                Orphan1.add(match.subSequence(0, match.length() - 1).toString());
                if (verbose) IJ.log("Orphan1 : " + match);
            }
        }
        // not associated 2
        Orphan2 = new ArrayList();
        for (int i2 = 0; i2 < population2.getNbObjects(); i2++) {
            String match = "-" + population2.getObject(i2).getValue();
            boolean found = false;
            for (String s : CostsOK.keySet()) {
                if (s.endsWith(match)) {
                    found = true;
                    break;
                }
            }
            if (!found) {
                if (verbose) IJ.log("Orphan2 : " + match);
                Orphan2.add(match.subSequence(1, match.length()).toString());
            }
        }
    }

    public HashMap<String, Double> getAssociations() {
        if (CostsAll == null) computeAssociation();

        return CostsOK;
    }

    public Object3D getAssociated(Object3D object3D) {
        int val = object3D.getValue();
        for (String asso : getAssociations().keySet()) {
            if (asso.startsWith(val + "-")) {
                return getObject3D2fromAsso(asso);
            }
        }

        return null;
    }

    public Object3D getObject3D1fromAsso(String asso) {
        int val = getValue1FromAsso(asso);
        if (val > 0)
            return population1.getObjectByValue(val);
        else return null;
    }

    public Object3D getObject3D2fromAsso(String asso) {
        int val = getValue2FromAsso(asso);
        if (val > 0)
            return population2.getObjectByValue(val);
        else return null;
    }

    public ArrayList<String> getOrphan1() {
        if (CostsAll == null) computeAssociation();

        return Orphan1;
    }

    public Objects3DPopulation getOrphan1Population() {
        Objects3DPopulation pop = new Objects3DPopulation();

        for (String orphanS : getOrphan1()) {
            int val = Integer.parseInt(orphanS);
            Object3D object3D1 = population1.getObjectByValue(val);
            pop.addObject(object3D1);
        }

        return pop;
    }

    public ArrayList<String> getOrphan2() {
        if (CostsAll == null) computeAssociation();

        return Orphan2;
    }

    public Objects3DPopulation getOrphan2Population() {
        Objects3DPopulation pop = new Objects3DPopulation();

        for (String orphanS : getOrphan2()) {
            int val = Integer.parseInt(orphanS);
            Object3D object3D2 = population2.getObjectByValue(val);
            pop.addObject(object3D2);
        }

        return pop;
    }

    private void computePairCosts() {
        for (int i1 = 0; i1 < population1.getNbObjects(); i1++) {
            for (int i2 = 0; i2 < population2.getNbObjects(); i2++) {
                double cost = associationCost.cost(population1.getObject(i1), population2.getObject(i2));
                int val1 = population1.getObject(i1).getValue();
                int val2 = population2.getObject(i2).getValue();
                if (cost >= 0) {
                    CostsAll.put(val1 + "-" + val2, cost);
                    //IJ.log("All " + val1 + "-" + val2);
                }
            }
        }
    }

    private void associationOK() {
        for (String s : CostsAll.keySet()) {
            String[] split = s.split("-");
            int val1 = Integer.parseInt(split[0]);
            int val2 = Integer.parseInt(split[1]);
            // Nb asso for val1
            if (nbAsso1(val1) == 1) {
                if (nbAsso2(val2) == 1) {
                    CostsOK.put(s, CostsAll.get(s));
                    if (verbose) IJ.log("Asso OK " + s + " " + CostsOK.get(s));
                } else {
                    CostsCompute.put(s, CostsAll.get(s));
                    if (verbose) IJ.log("Asso Compute " + s + " " + CostsCompute.get(s));
                }
            }
            if (nbAsso1(val1) > 1) {
                CostsCompute.put(s, CostsAll.get(s));
                if (verbose) IJ.log("Asso Compute " + s + " " + CostsCompute.get(s));
            }
        }
    }

    private int nbAsso1(int val1) {
        int c = 0;
        for (String s : CostsAll.keySet()) {
            if (s.startsWith(val1 + "-")) c++;
        }

        return c;
    }

    private int nbAsso2(int val2) {
        int c = 0;
        for (String s : CostsAll.keySet()) {
            if (s.endsWith("-" + val2)) c++;
        }

        return c;
    }

    private void computeAssociationsCosts() {
        // find number of objects involved
        HashMap<Integer, Integer> valuesIndices1 = new HashMap<>();
        HashMap<Integer, Integer> valuesIndices2 = new HashMap<>();
        // read pairs
        int c1 = 0;
        int c2 = 0;
        for (String s : CostsCompute.keySet()) {
            String[] split = s.split("-");
            int val1 = Integer.parseInt(split[0]);
            int val2 = Integer.parseInt(split[1]);
            if (!valuesIndices1.containsKey(val1)) {
                valuesIndices1.put(val1, c1);
                c1++;
            }
            if (!valuesIndices2.containsKey(val2)) {
                valuesIndices2.put(val2, c2);
                c2++;
            }
        }
        int nb1 = valuesIndices1.size();
        int nb2 = valuesIndices2.size();
        if (nb1 * nb2 == 0) {
            IJ.log("No association to compute");
            return;
        }

        costs = new double[nb1][nb2];

        // init all values to impossible
        for (int i1 = 0; i1 < nb1; i1++) {
            for (int i2 = 0; i2 < nb2; i2++) {
                costs[i1][i2] = NOASSO;
            }
        }
        // read pairs
        for (String s : CostsCompute.keySet()) {
            String[] split = s.split("-");
            int val1 = Integer.parseInt(split[0]);
            int val2 = Integer.parseInt(split[1]);
            double cost = associationCost.cost(population1.getObjectByValue(val1), population2.getObjectByValue(val2));
            costs[valuesIndices1.get(val1)][valuesIndices2.get(val2)] = cost;
        }
        // do the association
        HungarianAlgorithm algorithm = new HungarianAlgorithm(costs);
        algorithm.execute();
        // get associated
        int[] associated = algorithm.getAssociations();
        for (String s : CostsAll.keySet()) {
            String[] split = s.split("-");
            int val1 = Integer.parseInt(split[0]);
            int val2 = Integer.parseInt(split[1]);
            for (int i = 0; i < associated.length; i++) {
                if ((valuesIndices1.containsKey(val1)) && (valuesIndices2.containsKey(val2))) {
                    int i1 = valuesIndices1.get(val1);
                    int i2 = valuesIndices2.get(val2);
                    double cost = costs[i1][i2];
                    if (cost != NOASSO) {
                        if ((i1 == i) && (i2 == associated[i]))
                            CostsOK.put(val1 + "-" + val2, cost);
                    }
                }
            }
        }
    }
}
