/*     */
package mcib3d.tracking_dev;
/*     */

import ij.IJ;
import mcib3d.geom.Object3D;
import mcib3d.geom.Object3DVoxels;
import mcib3d.geom.Objects3DPopulation;
import mcib3d.geom.Vector3D;
import mcib3d.image3d.ImageHandler;
import mcib3d.utils.ArrayUtil;

/*     */
/*     */
/*     */
/*     */
/*     */
/*     */
/*     */ public class Mitosis {
    /*     */   private final Object3D daughter1;
    private final Object3D daughter2;
    /*  11 */   private Object3D mother = null;
    /*  12 */   private double colocMitosis = -1.0D;

    /*     */
    /*     */
    /*     */
    /*     */
    public Mitosis(Object3D daughter1, Object3D daughter2) {
        /*  17 */
        this.daughter1 = daughter1;
        /*  18 */
        this.daughter2 = daughter2;
        /*     */
    }

    /*     */
    /*     */
    public static Object3D createConvexDaughters(Object3D daughter1, Object3D daughter2) {
        /*  58 */
        Object3DVoxels voxels1 = new Object3DVoxels();
        /*  59 */
        voxels1.addVoxelsUnion(daughter1.getObject3DVoxels(), daughter2.getObject3DVoxels());
        /*     */
        /*  61 */
        return voxels1.getConvexObject();
        /*     */
    }

    /*     */
    /*     */
    public static boolean checkValidMitosis(Object3D daughter1, Object3D daughter2, ImageHandler image) {
        /*  73 */
        Object3D convex = createConvexDaughters(daughter1, daughter2);
        /*  74 */
        ArrayUtil values = convex.listValues(image).distinctValues();
        /*  75 */
        IJ.log("Checking mitosis " + daughter1.getValue() + "-" + daughter2.getValue() + " " + values);
        /*  76 */
        int val1 = daughter1.getValue();
        /*  77 */
        int val2 = daughter2.getValue();
        /*  78 */
        for (int i = 0; i < values.size(); i++) {
            /*  79 */
            int val = values.getValueInt(i);
            /*  80 */
            if (val != val1 && val != val2 && val > 0) return false;
            /*     */
            /*     */
        }
        /*  83 */
        return true;
        /*     */
    }

    /*     */
    /*     */
    public Object3D getDaughter1() {
        /*  26 */
        return this.daughter1;
        /*     */
    }

    /*     */
    /*     */
    public Object3D getDaughter2() {
        /*  30 */
        return this.daughter2;
        /*     */
    }

    /*     */
    /*     */
    public Object3D getMother() {
        /*  34 */
        return this.mother;
        /*     */
    }

    /*     */
    /*     */
    public void setMother(Object3D mother) {
        /*  22 */
        this.mother = mother;
        /*     */
    }

    /*     */
    /*     */
    public double getColocMitosis() {
        /*  38 */
        return this.colocMitosis;
        /*     */
    }

    /*     */
    /*     */
    public void setColocMitosis(double colocMitosis) {
        /*  42 */
        this.colocMitosis = colocMitosis;
        /*     */
    }

    /*     */
    /*     */
    public boolean hasObject(Object3D object3D) {
        /*  46 */
        int val = object3D.getValue();
        /*  47 */
        return (val == this.daughter1.getValue() || val == this.daughter2.getValue() || val == this.mother.getValue());
        /*     */
    }

    /*     */
    /*     */
    private Object3D createConvexDaughters() {
        /*  51 */
        Object3DVoxels voxels1 = new Object3DVoxels();
        /*  52 */
        voxels1.addVoxelsUnion(this.daughter1.getObject3DVoxels(), this.daughter2.getObject3DVoxels());
        /*     */
        /*  54 */
        return voxels1.getConvexObject();
        /*     */
    }

    /*     */
    /*     */
    private Vector3D getCenterDaughters() {
        /*  65 */
        Vector3D cen1 = this.daughter1.getCenterAsVector();
        /*  66 */
        Vector3D cen2 = this.daughter2.getCenterAsVector();
        /*  67 */
        Vector3D middle = cen1.add(cen2).multiply(0.5D);
        /*     */
        /*  69 */
        return middle;
        /*     */
    }

    /*     */
    /*     */
    public void getPotentialMother(ImageHandler image1) {
        /*  87 */
        Objects3DPopulation population = new Objects3DPopulation(image1);
        /*  88 */
        Object3D convex = createConvexDaughters();
        /*  89 */
        ArrayUtil values = convex.listValues(image1).distinctValues();
        /*  90 */
        int bestMother = 0;
        /*  91 */
        double maxColoc = 0.0D;
        /*  92 */
        Vector3D cen1 = this.daughter1.getCenterAsVector();
        /*  93 */
        Vector3D cen2 = this.daughter2.getCenterAsVector();
        /*  94 */
        Vector3D mid = getCenterDaughters();
        /*  95 */
        this.daughter1.setNewCenter(mid);
        /*  96 */
        this.daughter2.setNewCenter(mid);
        /*  97 */
        IJ.log("Middle " + mid);
        /*  98 */
        for (int i = 0; i < values.size(); i++) {
            /*  99 */
            int val = values.getValueInt(i);
            /* 100 */
            if (val != 0) {
                /* 101 */
                Object3D object3D = population.getObjectByValue(val);
                /* 102 */
                double pc1 = object3D.pcColoc(this.daughter1);
                /* 103 */
                double pc2 = object3D.pcColoc(this.daughter2);
                /* 104 */
                if (Math.min(pc1, pc2) > maxColoc) {
                    /* 105 */
                    maxColoc = Math.min(pc1, pc2);
                    /* 106 */
                    bestMother = val;
                    /*     */
                }
                /*     */
            }
            /* 109 */
        }
        this.daughter1.setNewCenter(cen1);
        /* 110 */
        this.daughter2.setNewCenter(cen2);
        /* 111 */
        IJ.log("Values mitosis : " + values.toString() + " Best : " + bestMother + " " + maxColoc);
        /*     */
    }
    /*     */
}


/* Location:              /home/thomas/Prog/MCIB/mcib3d-dev/mcib3d_dev/!/TRACKING/Mitosis.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */