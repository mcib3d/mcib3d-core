/*     */
package TRACKING;
/*     */

import mcib3d.geom.*;
import mcib3d.geom.interactions.InteractionsComputeDamLines;
import mcib3d.geom.interactions.InteractionsList;
import mcib3d.image3d.ImageHandler;
import mcib3d.image3d.ImageInt;
import mcib3d.image3d.ImageShort;
import mcib3d.utils.ArrayUtil;

import java.util.*;

/*     */
/*     */
/*     */
/*     */
/*     */
/*     */
/*     */
/*     */
/*     */
/*     */
/*     */
/*     */
/*     */
/*     */
/*     */
/*     */
/*     */ public class MitosisDetector {
    /*  21 */ Objects3DPopulation population1 = null;
    /*  22 */ Objects3DPopulation population2 = null; /*     */
    private ImageHandler img1;
    private ImageHandler img2;
    private Association association;
    /*     */
    /*  24 */   private ImageHandler voronoi = null;
    /*  25 */   private int radVoronoi = 50;
    /*  26 */   private double minColocMitosis = 50.0D;

    /*     */
    /*     */
    public MitosisDetector(ImageHandler img1, ImageHandler img2, Association association) {
        /*  29 */
        this.img1 = img1;
        /*  30 */
        this.img2 = img2;
        /*  31 */
        this.association = association;
        /*     */
    }

    /*     */
    /*     */
    public double getMinColocMitosis() {
        /*  35 */
        return this.minColocMitosis;
        /*     */
    }

    /*     */
    /*     */
    public void setMinColocMitosis(double minColocMitosis) {
        /*  39 */
        this.minColocMitosis = minColocMitosis;
        /*     */
    }

    /*     */
    /*     */
    public List<AssociationPair> getFinalAssociation() {
        /*  43 */
        ArrayList<PairColocalisation> list = new ArrayList<>();
        /*     */
        /*  45 */
        TreeSet<Mitosis> mitoses = detectMitosis();
        /*     */
        /*  47 */
        TreeSet<Integer> mito1 = new TreeSet<>();
        /*  48 */
        TreeSet<Integer> mito2 = new TreeSet<>();
        /*  49 */
        for (Mitosis mitosis : mitoses) {
            /*  50 */
            mito1.add(Integer.valueOf(mitosis.getMother().getValue()));
            /*  51 */
            mito2.add(Integer.valueOf(mitosis.getDaughter1().getValue()));
            /*  52 */
            mito2.add(Integer.valueOf(mitosis.getDaughter2().getValue()));
            /*     */
        }
        /*     */
        /*  55 */
        HashMap<String, Double> map = this.association.getAssociations();
        /*  56 */
        List<AssociationPair> map2 = new ArrayList<>();
        /*     */
        /*  58 */
        for (String s : map.keySet()) {
            /*  59 */
            int n1 = getObject1FromPair(s);
            /*  60 */
            int n2 = getObject2FromPair(s);
            /*  61 */
            boolean ok1 = !mito1.contains(Integer.valueOf(n1));
            /*  62 */
            boolean ok2 = !mito2.contains(Integer.valueOf(n2));
            /*  63 */
            if (ok1 && ok2) {
                /*  64 */
                map2.add(new AssociationPair(this.population1.getObjectByValue(n1), this.population2.getObjectByValue(n2), ((Double) map.get(s)).doubleValue()));
                /*     */
            }
            /*     */
        }
        /*     */
        /*  68 */
        return map2;
        /*     */
    }

    /*     */
    /*     */
    /*     */
    public TreeSet<Mitosis> detectMitosis() {
        /*  73 */
        this.population1 = new Objects3DPopulation(this.img1);
        /*  74 */
        this.population2 = new Objects3DPopulation(this.img2);
        /*     */
        /*  76 */
        Objects3DPopulation popOrphan2 = this.association.getOrphan2Population();
        /*  77 */
        LinkedList<Integer> listOrphans2 = new LinkedList<>();
        /*  78 */
        for (Object3D object3D : popOrphan2.getObjectsList()) listOrphans2.add(Integer.valueOf(object3D.getValue()));
        /*  79 */
        ArrayList<Object3D> around2 = interactOrphan2(this.population2, popOrphan2);
        /*     */
        /*  81 */
        HashMap<String, Double> assoPair2 = pairInteract2(around2, this.population2);
        /*     */
        /*  83 */
        if (this.voronoi == null) computeVoronoi(this.radVoronoi);
        /*  84 */
        Objects3DPopulation voronoi2 = new Objects3DPopulation(this.voronoi);
        /*  85 */
        ArrayList<Object3D> around1 = new ArrayList<>();
        /*  86 */
        for (Object3D object3D : around2) {
            /*  87 */
            int val = object3D.getValue();
            /*  88 */
            ArrayUtil arrayUtil = voronoi2.getObjectByValue(val).listValues(this.img1, 0.0F).distinctValues();
            /*  89 */
            for (int i = 0; i < arrayUtil.size(); i++) {
                /*  90 */
                if (arrayUtil.getValueInt(i) > 0) {
                    /*  91 */
                    Object3D object3D1 = this.population1.getObjectByValue(arrayUtil.getValueInt(i));
                    /*  92 */
                    if (!around1.contains(object3D1)) {
                        /*  93 */
                        around1.add(object3D1);
                        /*     */
                    }
                    /*     */
                }
                /*     */
            }
            /*     */
        }
        /*  98 */
        TreeSet<Mitosis> treeSet = new TreeSet<>((t1, t2) -> -Double.compare(t1.getColocMitosis(), t2.getColocMitosis()));
        /*  99 */
        for (String S : assoPair2.keySet()) {
            /* 100 */
            int obj1 = getObject1FromPair(S);
            /* 101 */
            int obj2 = getObject2FromPair(S);
            /* 102 */
            Object3D object3D1 = this.population2.getObjectByValue(obj1);
            /* 103 */
            Object3D object3D2 = this.population2.getObjectByValue(obj2);
            /*     */
            /* 105 */
            for (Object3D object3D : around1) {
                /* 106 */
                if (object3D1 == null || object3D2 == null || object3D == null)
                    /* 107 */ IJ.log("Pb mitosis check " + getObject1FromPair(S) + " " + getObject2FromPair(S));
                /* 108 */
                if (listOrphans2.contains(Integer.valueOf(obj1)) || listOrphans2.contains(Integer.valueOf(obj2))) {
                    /* 109 */
                    Mitosis mitosis = mitosisCheck(object3D1, object3D2, object3D);
                    /*     */
                    /*     */
                    /* 112 */
                    if (mitosis != null) {
                        /* 113 */
                        treeSet.add(mitosis);
                        /*     */
                    }
                    /*     */
                }
                /*     */
            }
            /*     */
        }
        /*     */
        /* 119 */
        return treeSet;
        /*     */
    }

    /*     */
    /*     */
    /*     */
    private HashMap<String, Double> pairInteract2(ArrayList<Object3D> interactOrphan2, Objects3DPopulation population2) {
        /* 124 */
        if (this.voronoi == null) computeVoronoi(this.radVoronoi);
        /* 125 */
        ImageHandler zones = this.voronoi;
        /*     */
        /* 127 */
        InteractionsList interactions = (new InteractionsComputeDamLines()).compute(zones);
        /* 128 */
        AssociationCost costPair = new CostPairMitosis(interactions);
        /* 129 */
        double maxCost = 0.0D;
        /* 130 */
        for (int i = 0; i < interactOrphan2.size(); i++) {
            /* 131 */
            for (int j = i + 1; j < interactOrphan2.size(); j++) {
                /* 132 */
                Object3D object3D1 = interactOrphan2.get(i);
                /* 133 */
                Object3D object3D2 = interactOrphan2.get(j);
                /* 134 */
                double cost = costPair.cost(object3D1, object3D2);
                /* 135 */
                if (cost > maxCost) maxCost = cost;
                /*     */
                /*     */
            }
            /*     */
        }
        /*     */
        /* 140 */
        Objects3DPopulation pair2 = new Objects3DPopulation();
        /* 141 */
        pair2.addObjects(interactOrphan2);
        /* 142 */
        Association association = new Association(pair2, population2, costPair);
        /* 143 */
        association.computeAssociation();
        /* 144 */
        HashMap<String, Double> assos = association.getAssociations();
        /*     */
        /* 146 */
        return assos;
        /*     */
    }

    /*     */
    /*     */
    private void computeVoronoi(int radius) {
        /* 150 */
        Watershed3DVoronoi watershed3DVoronoi = new Watershed3DVoronoi(this.img2, radius);
        /* 151 */
        this.voronoi = watershed3DVoronoi.getVoronoiZones(false);
        /*     */
    }

    /*     */
    /*     */
    private ArrayList<Object3D> interactOrphan2(Objects3DPopulation population2, Objects3DPopulation orphans2) {
        /* 155 */
        ArrayList<Object3D> interactOrphan2 = new ArrayList<>();
        /*     */
        /* 157 */
        for (Object3D object3D : orphans2.getObjectsList()) interactOrphan2.add(object3D);
        /* 158 */
        if (this.voronoi == null) computeVoronoi(this.radVoronoi);
        /* 159 */
        ImageHandler zones = this.voronoi;
        /*     */
        /* 161 */
        InteractionsList interactions = (new InteractionsComputeDamLines()).compute(zones);
        /* 162 */
        AssociationCost costPair = new CostPairMitosis(interactions);
        /* 163 */
        HashMap<String, PairColocalisation> map = interactions.getMap();
        /* 164 */
        for (String S : map.keySet()) {
            /*     */
            /* 166 */
            PairColocalisation pair = map.get(S);
            /* 167 */
            Object3D object3D1 = pair.getObject3D1();
            /* 168 */
            Object3D object3D2 = pair.getObject3D2();
            /* 169 */
            for (Object3D orphan2 : orphans2.getObjectsList()) {
                /* 170 */
                if (object3D1.getValue() == orphan2.getValue()) {
                    /* 171 */
                    double cost = costPair.cost(object3D2, orphan2);
                    /* 172 */
                    if (cost < 100.0D &&
                            /* 173 */             !interactOrphan2.contains(population2.getObjectByValue(object3D2.getValue())))
                        /*     */ {
                        /* 175 */
                        interactOrphan2.add(population2.getObjectByValue(object3D2.getValue()));
                        /*     */
                    }
                    /*     */
                }
                /*     */
                /* 179 */
                if (object3D2.getValue() == orphan2.getValue()) {
                    /* 180 */
                    double cost = costPair.cost(object3D2, orphan2);
                    /* 181 */
                    if (cost < 100.0D &&
                            /* 182 */             !interactOrphan2.contains(population2.getObjectByValue(object3D1.getValue()))) {
                        /* 183 */
                        interactOrphan2.add(population2.getObjectByValue(object3D1.getValue()));
                        /*     */
                    }
                    /*     */
                }
                /*     */
            }
            /*     */
        }
        /*     */
        /*     */
        /*     */
        /* 191 */
        return interactOrphan2;
        /*     */
    }

    /*     */
    /*     */
    private Mitosis mitosisCheck(Object3D pair21, Object3D pair22, Object3D around1) {
        /* 195 */
        Vector3D vector3D21 = pair21.getCenterAsVector();
        /* 196 */
        Vector3D vector3D22 = pair22.getCenterAsVector();
        /* 197 */
        Vector3D middle2 = vector3D21.add(vector3D22, 0.5D, 0.5D);
        /*     */
        /* 199 */
        pair21.setNewCenter(middle2);
        /* 200 */
        pair22.setNewCenter(middle2);
        /*     */
        /* 202 */
        double pc211 = pair21.pcColoc(around1);
        /* 203 */
        double pc221 = pair22.pcColoc(around1);
        /* 204 */
        double pc2122 = pair21.pcColoc(pair22);
        /* 205 */
        double minPc = Math.min(Math.min(pc211, pc221), pc2122);
        /*     */
        /* 207 */
        pair21.setNewCenter(vector3D21);
        /* 208 */
        pair22.setNewCenter(vector3D22);
        /* 209 */
        if (minPc > 0.0D) {
            /* 210 */
            Mitosis mitosis = new Mitosis(pair21, pair22);
            /* 211 */
            mitosis.setMother(around1);
            /* 212 */
            mitosis.setColocMitosis(minPc);
            /* 213 */
            return mitosis;
            /*     */
        }
        /*     */
        /* 216 */
        return null;
        /*     */
    }

    /*     */
    /*     */
    public ImageHandler detectAndMergeSplit() {
        /* 220 */
        ImageShort imageShort1 = new ImageShort("orphans1", this.img1.sizeX, this.img1.sizeY, this.img1.sizeZ);
        /* 221 */
        ImageShort imageShort2 = new ImageShort("orphans2", this.img1.sizeX, this.img1.sizeY, this.img1.sizeZ);
        /*     */
        /* 223 */
        this.association.drawOrphan1((ImageHandler) imageShort1);
        /*     */
        /*     */
        /* 226 */
        Objects3DPopulation popOrphan2 = this.association.getOrphan2Population();
        /* 227 */
        this.association.drawOrphan2((ImageHandler) imageShort2);
        /*     */
        /* 229 */
        LinkedList<Integer> listOrphans2 = new LinkedList<>();
        /* 230 */
        for (Object3D object3D : popOrphan2.getObjectsList()) listOrphans2.add(Integer.valueOf(object3D.getValue()));
        /*     */
        /*     */
        /* 233 */
        ArrayList<Object3D> around2 = interactOrphan2(this.population2, this.association.getOrphan2Population());
        /*     */
        /*     */
        /*     */
        /* 237 */
        HashMap<String, Double> assoPair2 = pairInteract2(around2, this.population2);
        /* 238 */
        for (String S : assoPair2.keySet()) {
            /* 239 */
            int obj1 = getObject1FromPair(S);
            /* 240 */
            int i = getObject2FromPair(S);
            /*     */
        }
        /*     */
        /*     */
        /*     */
        /*     */
        /* 246 */
        HashMap<String, Double> assoTouch2 = touchDamLines2(this.img2);
        /* 247 */
        for (String S : assoTouch2.keySet()) {
            /* 248 */
            int obj1 = getObject1FromPair(S);
            /* 249 */
            int i = getObject2FromPair(S);
            /*     */
        }
        /*     */
        /*     */
        /*     */
        /* 254 */
        ArrayList<String> toMerge = new ArrayList<>();
        /* 255 */
        for (String S : assoPair2.keySet()) {
            /* 256 */
            if (assoTouch2.containsKey(S))
                /*     */ {
                /* 258 */
                if (!toMerge.contains(S)) toMerge.add(S);
                /*     */
                /*     */
            }
            /*     */
        }
        /* 262 */
        ImageHandler merged = this.img2.duplicate();
        /* 263 */
        for (String S : toMerge) {
            /* 264 */
            Object3D object3D1 = this.population2.getObjectByValue(getObject1FromPair(S));
            /* 265 */
            Object3D object3D2 = this.population2.getObjectByValue(getObject2FromPair(S));
            /* 266 */
            Object3D object3D = mergeObjects(object3D1, object3D2);
            /*     */
            /* 268 */
            object3D1.draw(merged, 0);
            /* 269 */
            object3D2.draw(merged, 0);
            /* 270 */
            object3D.draw(merged);
            /*     */
        }
        /*     */
        /* 273 */
        return merged;
        /*     */
    }

    /*     */
    /*     */
    private HashMap<String, Double> touchDamLines2(ImageHandler img2) {
        /* 277 */
        InteractionsList interactions = (new InteractionsComputeDamLines()).compute(img2);
        /* 278 */
        HashMap<String, PairColocalisation> map = interactions.getMap();
        /* 279 */
        HashMap<String, Double> result = new HashMap<>(map.size());
        /* 280 */
        for (String S : map.keySet()) {
            /* 281 */
            result.put(S, Double.valueOf(((PairColocalisation) map.get(S)).getVolumeColoc()));
            /*     */
        }
        /*     */
        /* 284 */
        return result;
        /*     */
    }

    /*     */
    /*     */
    private Object3D mergeObjects(Object3D object3D1, Object3D object3D2) {
        /* 288 */
        IJ.log("Merging " + object3D1.getValue() + " and " + object3D2.getValue());
        /* 289 */
        if (object3D1 == null || object3D2 == null) return null;
        /* 290 */
        Objects3DPopulation popTemp = new Objects3DPopulation();
        /* 291 */
        popTemp.addObject(object3D1);
        /* 292 */
        popTemp.addObject(object3D2);
        /* 293 */
        ImageInt imageInt = popTemp.drawPopulation();
        /* 294 */
        int nbInter = interactionsDamLines((ImageHandler) imageInt, object3D1.getValue(), object3D2.getValue());
        /* 295 */
        if (nbInter == 0) return null;
        /*     */
        /* 297 */
        imageInt.draw(object3D1, object3D1.getValue());
        /* 298 */
        imageInt.draw(object3D2, object3D1.getValue());
        /*     */
        /*     */
        /* 301 */
        return (Object3D) new Object3DVoxels((ImageHandler) imageInt, object3D1.getValue());
        /*     */
    }

    /*     */
    /*     */
    private int interactionsDamLines(ImageHandler image, int v1, int v2) {
        /* 305 */
        LinkedList<Point3D> list = new LinkedList<>();
        /* 306 */
        for (int z = 0; z < image.sizeZ; z++) {
            /* 307 */
            for (int x = 0; x < image.sizeX; x++) {
                /* 308 */
                for (int y = 0; y < image.sizeY; y++) {
                    /* 309 */
                    if (image.getPixel(x, y, z) == 0.0F) {
                        /* 310 */
                        ArrayUtil util = image.getNeighborhood3x3x3(x, y, z);
                        /* 311 */
                        util = util.distinctValues();
                        /* 312 */
                        int c = 0;
                        /* 313 */
                        for (int i = 0; i < util.size(); i++) {
                            /* 314 */
                            for (int j = i + 1; j < util.size(); j++) {
                                /* 315 */
                                if ((util.getValueInt(i) == v1 && util.getValueInt(j) == v2) || (util.getValueInt(i) == v2 && util.getValueInt(j) == v1)) {
                                    /* 316 */
                                    list.add(new Point3D(x, y, z));
                                    /*     */
                                }
                                /*     */
                            }
                            /*     */
                        }
                        /*     */
                    }
                    /*     */
                }
                /*     */
            }
            /*     */
        }
        /* 324 */
        for (Point3D voxel3D : list) {
            /* 325 */
            image.setPixel(voxel3D, v1);
            /*     */
        }
        /*     */
        /* 328 */
        return list.size();
        /*     */
    }

    /*     */
    /*     */
    private int getObject1FromPair(String S) {
        /* 332 */
        int pos = S.indexOf("-");
        /*     */
        /* 334 */
        return Integer.parseInt(S.substring(0, pos));
        /*     */
    }

    /*     */
    /*     */
    private int getObject2FromPair(String S) {
        /* 338 */
        int pos = S.indexOf("-");
        /*     */
        /* 340 */
        return Integer.parseInt(S.substring(pos + 1));
        /*     */
    }
    /*     */
}


/* Location:              /home/thomas/Prog/MCIB/mcib3d-dev/mcib3d_dev/!/TRACKING/MitosisDetector.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */