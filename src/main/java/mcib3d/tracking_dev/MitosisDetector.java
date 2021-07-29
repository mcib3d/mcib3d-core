package mcib3d.tracking_dev;

import ij.IJ;
import mcib3d.geom.*;
import mcib3d.geom.interactions.InteractionsComputeDamLines;
import mcib3d.geom.interactions.InteractionsList;
import mcib3d.image3d.ImageHandler;
import mcib3d.image3d.ImageInt;
import mcib3d.image3d.ImageShort;
import mcib3d.image3d.regionGrowing.Watershed3DVoronoi;
import mcib3d.utils.ArrayUtil;

import java.util.*;

 public class MitosisDetector {
     private final ImageHandler img1;
     private final ImageHandler img2;
     private final Association association;
     private final int radVoronoi = 50;
     Objects3DPopulation population1 = null;
     Objects3DPopulation population2 = null;

     private ImageHandler voronoi = null;
     private double minColocMitosis = 50.0D;

     public MitosisDetector(ImageHandler img1, ImageHandler img2, Association association) {
         this.img1 = img1;
         this.img2 = img2;
         this.association = association;
     }

     public double getMinColocMitosis() {
         return this.minColocMitosis;
     }

    public void setMinColocMitosis(double minColocMitosis) {
        this.minColocMitosis = minColocMitosis;
    }

     public List<AssociationPair> getFinalAssociation() {
        ArrayList<PairColocalisation> list = new ArrayList<>();
        TreeSet<Mitosis> mitoses = detectMitosis();

         TreeSet<Integer> mito1 = new TreeSet<>();
        TreeSet<Integer> mito2 = new TreeSet<>();
        for (Mitosis mitosis : mitoses) {
            mito1.add(Integer.valueOf(mitosis.getMother().getValue()));
            mito2.add(Integer.valueOf(mitosis.getDaughter1().getValue()));
            mito2.add(Integer.valueOf(mitosis.getDaughter2().getValue()));
        }

         HashMap<String, Double> map = this.association.getAssociations();
        List<AssociationPair> map2 = new ArrayList<>();

         for (String s : map.keySet()) {
            int n1 = getObject1FromPair(s);
            int n2 = getObject2FromPair(s);
            boolean ok1 = !mito1.contains(Integer.valueOf(n1));
            boolean ok2 = !mito2.contains(Integer.valueOf(n2));
            if (ok1 && ok2) {
                map2.add(new AssociationPair(this.population1.getObjectByValue(n1), this.population2.getObjectByValue(n2), map.get(s).doubleValue()));
            }
        }

         return map2;
    }

    public TreeSet<Mitosis> detectMitosis() {
        this.population1 = new Objects3DPopulation(this.img1);
        this.population2 = new Objects3DPopulation(this.img2);

        Objects3DPopulation popOrphan2 = this.association.getOrphan2Population();
        LinkedList<Integer> listOrphans2 = new LinkedList<>();
        for (Object3D object3D : popOrphan2.getObjectsList()) listOrphans2.add(Integer.valueOf(object3D.getValue()));
        ArrayList<Object3D> around2 = interactOrphan2(this.population2, popOrphan2);

        HashMap<String, Double> assoPair2 = pairInteract2(around2, this.population2);

        if (this.voronoi == null) computeVoronoi(this.radVoronoi);
        Objects3DPopulation voronoi2 = new Objects3DPopulation(this.voronoi);
        ArrayList<Object3D> around1 = new ArrayList<>();
        for (Object3D object3D : around2) {
            int val = object3D.getValue();
            ArrayUtil arrayUtil = voronoi2.getObjectByValue(val).listValues(this.img1, 0.0F).distinctValues();
            for (int i = 0; i < arrayUtil.size(); i++) {
                if (arrayUtil.getValueInt(i) > 0) {
                    Object3D object3D1 = this.population1.getObjectByValue(arrayUtil.getValueInt(i));
                    if (!around1.contains(object3D1)) {
                        around1.add(object3D1);
                    }
                }

            }

        }
        TreeSet<Mitosis> treeSet = new TreeSet<>((t1, t2) -> -Double.compare(t1.getColocMitosis(), t2.getColocMitosis()));
        for (String S : assoPair2.keySet()) {
            int obj1 = getObject1FromPair(S);
            int obj2 = getObject2FromPair(S);
            Object3D object3D1 = this.population2.getObjectByValue(obj1);
            Object3D object3D2 = this.population2.getObjectByValue(obj2);

            for (Object3D object3D : around1) {
                if (object3D1 == null || object3D2 == null || object3D == null)
                    IJ.log("Pb mitosis check " + getObject1FromPair(S) + " " + getObject2FromPair(S));
                if (listOrphans2.contains(Integer.valueOf(obj1)) || listOrphans2.contains(Integer.valueOf(obj2))) {
                    Mitosis mitosis = mitosisCheck(object3D1, object3D2, object3D);
                    if (mitosis != null) {
                        treeSet.add(mitosis);
                    }
                }
            }
        }

        return treeSet;
    }

    private HashMap<String, Double> pairInteract2(ArrayList<Object3D> interactOrphan2, Objects3DPopulation population2) {
        if (this.voronoi == null) computeVoronoi(this.radVoronoi);
        ImageHandler zones = this.voronoi;

        InteractionsList interactions = (new InteractionsComputeDamLines()).compute(zones);
        AssociationCost costPair = new CostPairMitosis(interactions);
        double maxCost = 0.0D;
        for (int i = 0; i < interactOrphan2.size(); i++) {
            for (int j = i + 1; j < interactOrphan2.size(); j++) {
                Object3D object3D1 = interactOrphan2.get(i);
                Object3D object3D2 = interactOrphan2.get(j);
                double cost = costPair.cost(object3D1, object3D2);
                if (cost > maxCost) maxCost = cost;
            }
        }

        Objects3DPopulation pair2 = new Objects3DPopulation();
        pair2.addObjects(interactOrphan2);
        Association association = new Association(pair2, population2, costPair);
        association.computeAssociation();
        HashMap<String, Double> assos = association.getAssociations();

        return assos;
    }

     private void computeVoronoi(int radius) {
        Watershed3DVoronoi watershed3DVoronoi = new Watershed3DVoronoi(this.img2, radius);
        this.voronoi = watershed3DVoronoi.getVoronoiZones(false);
    }

    private ArrayList<Object3D> interactOrphan2(Objects3DPopulation population2, Objects3DPopulation orphans2) {
        ArrayList<Object3D> interactOrphan2 = new ArrayList<>();

        for (Object3D object3D : orphans2.getObjectsList()) interactOrphan2.add(object3D);
        if (this.voronoi == null) computeVoronoi(this.radVoronoi);
        ImageHandler zones = this.voronoi;

        InteractionsList interactions = (new InteractionsComputeDamLines()).compute(zones);
        AssociationCost costPair = new CostPairMitosis(interactions);
        HashMap<String, PairColocalisation> map = interactions.getMap();
        for (String S : map.keySet()) {
            PairColocalisation pair = map.get(S);
            Object3D object3D1 = pair.getObject3D1();
            Object3D object3D2 = pair.getObject3D2();
            for (Object3D orphan2 : orphans2.getObjectsList()) {
                if (object3D1.getValue() == orphan2.getValue()) {
                    double cost = costPair.cost(object3D2, orphan2);
                    if (cost < 100.0D &&
                            !interactOrphan2.contains(population2.getObjectByValue(object3D2.getValue()))) {
                        interactOrphan2.add(population2.getObjectByValue(object3D2.getValue()));
                    }
                }

                if (object3D2.getValue() == orphan2.getValue()) {
                    double cost = costPair.cost(object3D2, orphan2);
                    if (cost < 100.0D && !interactOrphan2.contains(population2.getObjectByValue(object3D1.getValue()))) {
                        interactOrphan2.add(population2.getObjectByValue(object3D1.getValue()));
                    }
                }
            }
        }

        return interactOrphan2;
    }

    private Mitosis mitosisCheck(Object3D pair21, Object3D pair22, Object3D around1) {
        Vector3D vector3D21 = pair21.getCenterAsVector();
        Vector3D vector3D22 = pair22.getCenterAsVector();
        Vector3D middle2 = vector3D21.add(vector3D22, 0.5D, 0.5D);

        pair21.setNewCenter(middle2);
        pair22.setNewCenter(middle2);

        double pc211 = pair21.pcColoc(around1);
        double pc221 = pair22.pcColoc(around1);
        double pc2122 = pair21.pcColoc(pair22);
        double minPc = Math.min(Math.min(pc211, pc221), pc2122);

        pair21.setNewCenter(vector3D21);
        pair22.setNewCenter(vector3D22);
        if (minPc > 0.0D) {
            Mitosis mitosis = new Mitosis(pair21, pair22);
            mitosis.setMother(around1);
            mitosis.setColocMitosis(minPc);
            return mitosis;
        }

        return null;
    }

    public ImageHandler detectAndMergeSplit() {
        ImageShort imageShort1 = new ImageShort("orphans1", this.img1.sizeX, this.img1.sizeY, this.img1.sizeZ);
        ImageShort imageShort2 = new ImageShort("orphans2", this.img1.sizeX, this.img1.sizeY, this.img1.sizeZ);

        this.association.drawOrphan1(imageShort1);

        Objects3DPopulation popOrphan2 = this.association.getOrphan2Population();
        this.association.drawOrphan2(imageShort2);

        LinkedList<Integer> listOrphans2 = new LinkedList<>();
        for (Object3D object3D : popOrphan2.getObjectsList()) listOrphans2.add(Integer.valueOf(object3D.getValue()));

        ArrayList<Object3D> around2 = interactOrphan2(this.population2, this.association.getOrphan2Population());

        HashMap<String, Double> assoPair2 = pairInteract2(around2, this.population2);
        for (String S : assoPair2.keySet()) {
            int obj1 = getObject1FromPair(S);
            int i = getObject2FromPair(S);
            // ??
        }

        HashMap<String, Double> assoTouch2 = touchDamLines2(this.img2);
        for (String S : assoTouch2.keySet()) {
            int obj1 = getObject1FromPair(S);
            int i = getObject2FromPair(S);
            // ??
        }

        ArrayList<String> toMerge = new ArrayList<>();
        for (String S : assoPair2.keySet()) {
            if (assoTouch2.containsKey(S)) {
                if (!toMerge.contains(S)) toMerge.add(S);
            }
        }
        ImageHandler merged = this.img2.duplicate();
        for (String S : toMerge) {
            Object3D object3D1 = this.population2.getObjectByValue(getObject1FromPair(S));
            Object3D object3D2 = this.population2.getObjectByValue(getObject2FromPair(S));
            Object3D object3D = fuseObjects(object3D1, object3D2);

            object3D1.draw(merged, 0);
            object3D2.draw(merged, 0);
            object3D.draw(merged);
        }

        return merged;
    }

     private HashMap<String, Double> touchDamLines2(ImageHandler img2) {
        InteractionsList interactions = (new InteractionsComputeDamLines()).compute(img2);
        HashMap<String, PairColocalisation> map = interactions.getMap();
        HashMap<String, Double> result = new HashMap<>(map.size());
        for (String S : map.keySet()) {
            result.put(S, Double.valueOf(map.get(S).getVolumeColoc()));
        }

         return result;
    }

     private Object3D fuseObjects(Object3D object3D1, Object3D object3D2) {
         IJ.log("Merging " + object3D1.getValue() + " and " + object3D2.getValue());
         if (object3D1 == null || object3D2 == null) return null;
         Objects3DPopulation popTemp = new Objects3DPopulation();
         popTemp.addObject(object3D1);
         popTemp.addObject(object3D2);
         ImageHandler imageInt = popTemp.drawPopulation();
         int nbInter = interactionsDamLines(imageInt, object3D1.getValue(), object3D2.getValue());
         if (nbInter == 0) return null;

         imageInt.draw(object3D1, object3D1.getValue());
        imageInt.draw(object3D2, object3D1.getValue());

         return new Object3DVoxels(imageInt, object3D1.getValue());
    }

    private int interactionsDamLines(ImageHandler image, int v1, int v2) {
        LinkedList<Point3D> list = new LinkedList<>();
        for (int z = 0; z < image.sizeZ; z++) {
            for (int x = 0; x < image.sizeX; x++) {
                for (int y = 0; y < image.sizeY; y++) {
                    if (image.getPixel(x, y, z) == 0.0F) {
                        ArrayUtil util = image.getNeighborhood3x3x3(x, y, z);
                        util = util.distinctValues();
                        int c = 0;
                        for (int i = 0; i < util.size(); i++) {
                            for (int j = i + 1; j < util.size(); j++) {
                                if ((util.getValueInt(i) == v1 && util.getValueInt(j) == v2) || (util.getValueInt(i) == v2 && util.getValueInt(j) == v1)) {
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

    private int getObject1FromPair(String S) {
        int pos = S.indexOf("-");

        return Integer.parseInt(S.substring(0, pos));
    }

    private int getObject2FromPair(String S) {
        int pos = S.indexOf("-");

        return Integer.parseInt(S.substring(pos + 1));
    }
}