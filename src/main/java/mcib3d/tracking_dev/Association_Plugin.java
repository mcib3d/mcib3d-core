package mcib3d.tracking_dev;

import ij.IJ;
import ij.WindowManager;
import ij.plugin.PlugIn;
import mcib3d.geom.*;
import mcib3d.geom.interactions.InteractionsComputeDamLines;
import mcib3d.geom.interactions.InteractionsList;
import mcib3d.image3d.ImageHandler;
import mcib3d.image3d.ImageInt;
import mcib3d.image3d.ImageShort;
import mcib3d.image3d.regionGrowing.Watershed3DVoronoi;
import mcib3d.utils.ArrayUtil;

import java.util.*;

public class Association_Plugin implements PlugIn {
    ImageHandler voronoi;

    @Override
    public void run(String s) {
        // get opened images
        int nbima = WindowManager.getImageCount();
        if (nbima < 2) {
            IJ.error("Needs at least two labelled images");
            return;
        }
        String[] names = new String[nbima + 1];

        for (int i = 0; i < nbima; i++) {
            names[i] = WindowManager.getImage(i + 1).getShortTitle();
        }
        names[nbima] = "None";
        int idxA = 0;
        int idxB = 1;
        int idxC = 2;
        ImageInt img1 = ImageInt.wrap(WindowManager.getImage(idxA + 1));
        ImageInt img2 = ImageInt.wrap(WindowManager.getImage(idxB + 1));
        ImageInt path = null;
        if (nbima > 2) path = ImageInt.wrap(WindowManager.getImage(idxC + 1));

        TrackingAssociation trackingAssociation = new TrackingAssociation(img1, img2);
        if (path != null) trackingAssociation.setPathImage(path);
        trackingAssociation.setMerge(false);
        trackingAssociation.getTracked().show("tracked");
        if (path != null) trackingAssociation.getPathed().show("pathed");
    }

    private void computeMitosis(ImageHandler img1, ImageHandler img2, Objects3DPopulation population1, Objects3DPopulation population2, Association association) {
        ImageInt orphans1 = new ImageShort("orphans1", img1.sizeX, img1.sizeY, img1.sizeZ);
        ImageInt orphans2 = new ImageShort("orphans2", img1.sizeX, img1.sizeY, img1.sizeZ);
        // orphans 1
        association.drawOrphan1(orphans1);
        // draw the mitosis
        //for (Object3D object3D : object3DMitosis) object3D.draw(orphans1);
        orphans1.show();
        Objects3DPopulation popOrphan1 = new Objects3DPopulation(orphans1);

        // orphans2
        Watershed3DVoronoi watershed3DVoronoi = new Watershed3DVoronoi(img2, 50);
        voronoi = watershed3DVoronoi.getVoronoiZones(false);
        IJ.log("*** ORPHAN 2 ***");
        Objects3DPopulation popOrphan2 = association.getOrphan2Population();
        association.drawOrphan2(orphans2);

        LinkedList<Integer> listOrphans2 = new LinkedList<>();
        for (Object3D object3D : popOrphan2.getObjectsList()) listOrphans2.add(object3D.getValue());
        // check touching
        //ImageHandler touch2 = touching(population2, popOrphan2, img1, img2);
        //touch2.show();
        // orphan2, pop2 and img2 are updated
        //popOrphan2 = new Objects3DPopulation(touch2);

        // interactiong with orphans2
        ArrayList<Object3D> around2 = interactOrphan2(population2, association.getOrphan2Population());
        for (Object3D object3D : around2) IJ.log("Around2 " + object3D.getValue());

        // possible pair 2
        HashMap<String, Double> assoPair2 = pairInteract2(around2, population2);
        for (String S : assoPair2.keySet()) {
            int obj1 = getObject1FromPair(S);
            int obj2 = getObject2FromPair(S);
            if (((listOrphans2.contains(obj1)) || (listOrphans2.contains(obj2))) && (obj1 < obj2))
                IJ.log("Asso orphan2 " + S);
        }

        // touch orphan2
        HashMap<String, Double> assoTouch2 = touchDamLines2(img2);
        for (String S : assoTouch2.keySet()) {
            int obj1 = getObject1FromPair(S);
            int obj2 = getObject2FromPair(S);
            if ((listOrphans2.contains(obj1)) || (listOrphans2.contains(obj2))) IJ.log("Touching orphan2 " + S);
        }

        // Check duplicate from pair and touch
        ArrayList<String> toMerge = new ArrayList<>();
        for (String S : assoPair2.keySet()) {
            if (assoTouch2.containsKey(S)) {
                IJ.log("Touching pair + orphan2 " + S);
                if (!toMerge.contains(S)) toMerge.add(S);
            }
        }
        // merging
        for (String S : toMerge) {
            Object3D object3D1 = population2.getObjectByValue(getObject1FromPair(S));
            Object3D object3D2 = population2.getObjectByValue(getObject2FromPair(S));
            Object3D object3D = mergeObjects(object3D1, object3D2);
            // update img2
            object3D1.draw(img2, 0);
            object3D2.draw(img2, 0);
            object3D.draw(img2);
        }
        img2.updateDisplay();
        population2 = new Objects3DPopulation(img2);
        // voronoi
        watershed3DVoronoi = new Watershed3DVoronoi(img2, 50);
        voronoi = watershed3DVoronoi.getVoronoiZones(false);
        // need to recompute around2
        association = new Association(population1, population2, new CostColocalisation(new Objects3DPopulationColocalisation(population1, population2)));
        // associations
        association.computeAssociation();
        //association.drawAssociation(tracked); /// TODO REDO ASSOCIATION
        popOrphan2 = association.getOrphan2Population();
        association.drawOrphan2(orphans2);
        listOrphans2 = new LinkedList<>();
        for (Object3D object3D : popOrphan2.getObjectsList()) listOrphans2.add(object3D.getValue());
        orphans2.show();
        around2 = interactOrphan2(population2, popOrphan2);
        for (Object3D object3D : around2) IJ.log("Around2 merge " + object3D);
        assoPair2 = pairInteract2(around2, population2);


        // possible interactions pop1
        Objects3DPopulation voronoi2 = new Objects3DPopulation(voronoi);
        ArrayList<Object3D> around1 = new ArrayList<>();
        for (Object3D object3D : around2) {
            int val = object3D.getValue();
            ArrayUtil arrayUtil = voronoi2.getObjectByValue(val).listValues(img1, 0).distinctValues();
            for (int i = 0; i < arrayUtil.size(); i++) {
                if (arrayUtil.getValueInt(i) > 0) {
                    Object3D object3D1 = population1.getObjectByValue(arrayUtil.getValueInt(i));
                    if (!around1.contains(object3D1))
                        around1.add(object3D1);
                }
            }
        }
        for (Object3D object3D : around1) IJ.log("Around1 " + object3D.getValue());

        // test mitosis
        TreeSet<Mitosis> treeSet = new TreeSet<>(new Comparator<Mitosis>() {
            @Override
            public int compare(Mitosis mitosis, Mitosis t1) {
                return -Double.compare(mitosis.getColocMitosis(), t1.getColocMitosis());
            }
        });
        for (String S : assoPair2.keySet()) {
            int obj1 = getObject1FromPair(S);
            int obj2 = getObject2FromPair(S);
            Object3D object3D1 = population2.getObjectByValue(obj1);
            Object3D object3D2 = population2.getObjectByValue(obj2);
            // around 1
            for (Object3D object3D : around1) {
                if ((object3D1 == null) || (object3D2 == null) || (object3D == null))
                    IJ.log("Pb mitosis check " + getObject1FromPair(S) + " " + getObject2FromPair(S));
                if ((listOrphans2.contains(obj1)) || (listOrphans2.contains(obj2))) {
                    Mitosis mitosis = mitosisCheck(object3D1, object3D2, object3D);
                    //if (mitosis != null)
                    //IJ.log("MITO " + mitosis.getDaughter1().getValue() + " " + mitosis.getDaughter2().getValue() + " " + mitosis.getMother().getValue() + " " + mitosis.getColocMitosis());
                    if (mitosis != null) {
                        treeSet.add(mitosis);
                    }
                }
            }
        }
        while (!treeSet.isEmpty()) {
            Mitosis mitosis = treeSet.pollFirst();
            Object3D d1 = mitosis.getDaughter1();
            Object3D d2 = mitosis.getDaughter2();
            Object3D mo = mitosis.getMother();
            double coloc = mitosis.getColocMitosis();
            IJ.log("BEST MITO " + d1.getValue() + " " + d2.getValue() + " " + mo.getValue() + " " + coloc);
            if (coloc > 50) {
                // draw mito in paths

            }
        }
    }

    private Object3D mergeObjects(Object3D object3D1, Object3D object3D2) {
        if ((object3D1 == null) || (object3D2 == null)) return null;
        Objects3DPopulation popTemp = new Objects3DPopulation();
        popTemp.addObject(object3D1);
        popTemp.addObject(object3D2);
        ImageHandler imgTemp = popTemp.drawPopulation();
        int nbInter = interactionsDamLines(imgTemp, object3D1.getValue(), object3D2.getValue());
        if (nbInter == 0) return null; // cannot merge not interacting objects
        // draw object2 as value of object1
        imgTemp.draw(object3D1, object3D1.getValue());
        imgTemp.draw(object3D2, object3D1.getValue());
        // return the object

        return new Object3DVoxels(imgTemp, object3D1.getValue());
    }

    private int interactionsDamLines(ImageHandler image, int v1, int v2) {
        LinkedList<Point3D> list = new LinkedList<>();
        for (int z = 0; z < image.sizeZ; z++) {
            for (int x = 0; x < image.sizeX; x++) {
                for (int y = 0; y < image.sizeY; y++) {
                    if (image.getPixel(x, y, z) == 0) {
                        ArrayUtil util = image.getNeighborhood3x3x3(x, y, z);
                        util = util.distinctValues();
                        int c = 0;
                        for (int i = 0; i < util.size(); i++) {
                            for (int j = i + 1; j < util.size(); j++) {
                                if (((util.getValueInt(i) == v1) && (util.getValueInt(j) == v2)) || ((util.getValueInt(i) == v2) && (util.getValueInt(j) == v1))) {
                                    list.add(new Point3D(x, y, z));
                                }
                            }
                        }
                    }
                }
            }
        }
        for (Point3D voxel3D : list) {
            image.setPixel(voxel3D, v1);
        }

        return list.size();
    }

    private ImageHandler touching(Objects3DPopulation population2, Objects3DPopulation orphans2, ImageHandler img1, ImageHandler img2) {
        //LinkedList<Object3D> touch = new LinkedList<>();
        InteractionsList interactions = new InteractionsComputeDamLines().compute(img2);
        HashMap<String, PairColocalisation> map = interactions.getMap();
        int minTouch = Integer.MAX_VALUE;
        for (String S : map.keySet()) {
            int vol = map.get(S).getVolumeColoc();
            minTouch = Math.min(minTouch, vol);
            //touch.add(population2.getObjectByValue(getObject1FromPair(S)));
            //touch.add(population2.getObjectByValue(getObject2FromPair(S)));
        }
        IJ.log("Min Touch " + minTouch);

        ImageInt touchImg2 = new ImageShort("touch2", img1.sizeX, img1.sizeY, img1.sizeZ);
        for (Object3D object3D : orphans2.getObjectsList()) {
            object3D.draw(touchImg2);
        }
        //touchImg2.show();
        Objects3DPopulation popTouch = new Objects3DPopulation(touchImg2);

        /////////// ASSO for touching
        CostTouching costTouching = new CostTouching(interactions);
        costTouching.setMaxCost(1.0 / (double) minTouch);
        Association association = new Association(popTouch, popTouch, costTouching);
        association.computeAssociation();
        HashMap<String, Double> assos = association.getAssociations();
        for (String asso : assos.keySet()) {
            Object3D object3D1 = association.getObject3D1fromAsso(asso);
            Object3D object3D2 = association.getObject3D2fromAsso(asso);
            int val1 = object3D1.getValue();
            int val2 = object3D2.getValue();
            if (val1 != val2) {
                if ((orphans2.getObjectByValue(val1) != null) || (orphans2.getObjectByValue(val2) != null)) {
                    IJ.log("Asso touch : " + object3D1.getValue() + " - " + object3D2.getValue());
                    // merge objects
                    Object3D merged = mergeObjects(object3D1, object3D2);
                    merged.draw(touchImg2);
                    // update orphan2
                    orphans2.removeObject(object3D1);
                    orphans2.removeObject(object3D2);
                    orphans2.addObject(merged);
                    // update pop2
                    population2.removeObject(object3D1);
                    population2.removeObject(object3D2);
                    population2.addObject(merged);
                    // update img2
                    merged.draw(img2);
                }
            }
        }

        return touchImg2;
    }

    private int getObject1FromPair(String S) {
        int pos = S.indexOf("-");

        return Integer.parseInt(S.substring(0, pos));
    }

    private int getObject2FromPair(String S) {
        int pos = S.indexOf("-");

        return Integer.parseInt(S.substring(pos + 1));
    }

    private Mitosis mitosisCheck(Object3D pair21, Object3D pair22, Object3D around1) {
        Vector3D vector3D21 = pair21.getCenterAsVector();
        Vector3D vector3D22 = pair22.getCenterAsVector();
        Vector3D middle2 = vector3D21.add(vector3D22, .5, .5);
        // move all objects 2 to this center
        pair21.setNewCenter(middle2);
        pair22.setNewCenter(middle2);
        // around1 should be somewhere around middle
        double pc211 = pair21.pcColoc(around1);
        double pc221 = pair22.pcColoc(around1);
        double pc2122 = pair21.pcColoc(pair22);
        double minPc = Math.min(Math.min(pc211, pc221), pc2122);
        if (minPc > 0) {
            Mitosis mitosis = new Mitosis(pair21, pair22);
            mitosis.setMother(around1);
            mitosis.setColocMitosis(minPc);
            return mitosis;
        }
        // reset centers
        pair21.setNewCenter(vector3D21);
        pair22.setNewCenter(vector3D22);

        return null;
    }


    private ArrayList<Object3D> interactOrphan2(Objects3DPopulation population2, Objects3DPopulation orphans2) {
        ArrayList<Object3D> interactOrphan2 = new ArrayList<>();
        // add orphan2 first
        for (Object3D object3D : orphans2.getObjectsList()) interactOrphan2.add(object3D);
        ImageHandler zones = voronoi;
        //zones.show();
        InteractionsList interactions = new InteractionsComputeDamLines().compute(zones);
        AssociationCost costPair = new CostPairMitosis(interactions);
        HashMap<String, PairColocalisation> map = interactions.getMap();
        for (String S : map.keySet()) {
            //IJ.log("Testing " + S);
            PairColocalisation pair = map.get(S);
            Object3D object3D1 = pair.getObject3D1();
            Object3D object3D2 = pair.getObject3D2();
            for (Object3D orphan2 : orphans2.getObjectsList()) {
                if ((object3D1.getValue() == orphan2.getValue())) {
                    double cost = costPair.cost(object3D2, orphan2);
                    if (cost < 100) {
                        if (!interactOrphan2.contains(population2.getObjectByValue(object3D2.getValue()))) {
                            IJ.log("Asso Pair2 : " + orphan2.getValue() + "-" + object3D2.getValue() + " " + cost);
                            interactOrphan2.add(population2.getObjectByValue(object3D2.getValue()));
                        }
                    }
                }
                if ((object3D2.getValue() == orphan2.getValue())) {
                    double cost = costPair.cost(object3D2, orphan2);
                    if (cost < 100) {
                        if (!interactOrphan2.contains(population2.getObjectByValue(object3D1.getValue()))) {
                            interactOrphan2.add(population2.getObjectByValue(object3D1.getValue()));
                            IJ.log("Asso Pair2 : " + orphan2.getValue() + "-" + object3D1.getValue() + " " + cost);
                        }
                    }
                }
            }
        }

        return interactOrphan2;
    }

    private HashMap<String, Double> touchDamLines2(ImageHandler img2) {
        InteractionsList interactions = new InteractionsComputeDamLines().compute(img2);
        HashMap<String, PairColocalisation> map = interactions.getMap();
        HashMap<String, Double> result = new HashMap<>(map.size());
        for (String S : map.keySet()) {
            result.put(S, (double) map.get(S).getVolumeColoc());
        }

        return result;
    }

    private HashMap<String, Double> pairInteract2(ArrayList<Object3D> interactOrphan2, Objects3DPopulation population2) {
        // association with all from interactOrphan2
        ImageHandler zones = voronoi;
        //zones.show();
        InteractionsList interactions = new InteractionsComputeDamLines().compute(zones);
        AssociationCost costPair = new CostPairMitosis(interactions);
        double maxCost = 0;
        for (int i = 0; i < interactOrphan2.size(); i++) {
            for (int j = i + 1; j < interactOrphan2.size(); j++) {
                Object3D object3D1 = interactOrphan2.get(i);
                Object3D object3D2 = interactOrphan2.get(j);
                double cost = costPair.cost(object3D1, object3D2);
                if (cost > maxCost) maxCost = cost;
            }
        }
        IJ.log("Max Cost " + maxCost);
        // association
        Objects3DPopulation pair2 = new Objects3DPopulation();
        pair2.addObjects(interactOrphan2);
        Association association = new Association(pair2, population2, costPair);
        association.computeAssociation();
        HashMap<String, Double> assos = association.getAssociations();

        return assos;
    }

}
