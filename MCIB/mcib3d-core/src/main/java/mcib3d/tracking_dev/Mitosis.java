package mcib3d.tracking_dev;

import ij.IJ;
import mcib3d.geom.Object3D;
import mcib3d.geom.Object3DVoxels;
import mcib3d.geom.Objects3DPopulation;
import mcib3d.geom.Vector3D;
import mcib3d.image3d.ImageHandler;
import mcib3d.utils.ArrayUtil;

public class Mitosis {
    private final Object3D daughter1;
    private final Object3D daughter2;
    private Object3D mother = null;
    private double colocMitosis = -1.0D;

    public Mitosis(Object3D daughter1, Object3D daughter2) {
        this.daughter1 = daughter1;
        this.daughter2 = daughter2;
    }

    public static Object3D createConvexDaughters(Object3D daughter1, Object3D daughter2) {
        Object3DVoxels voxels1 = new Object3DVoxels();
        voxels1.addVoxelsUnion(daughter1.getObject3DVoxels(), daughter2.getObject3DVoxels());

        return voxels1.getConvexObject();
    }


    public static boolean checkValidMitosis(Object3D daughter1, Object3D daughter2, ImageHandler image) {
        Object3D convex = createConvexDaughters(daughter1, daughter2);
        ArrayUtil values = convex.listValues(image).distinctValues();
        IJ.log("Checking mitosis " + daughter1.getValue() + "-" + daughter2.getValue() + " " + values);
        int val1 = daughter1.getValue();
        int val2 = daughter2.getValue();
        for (int i = 0; i < values.size(); i++) {
            int val = values.getValueInt(i);
            if (val != val1 && val != val2 && val > 0) return false;
        }

        return true;
    }

    public Object3D getDaughter1() {
        return this.daughter1;
    }

    public Object3D getDaughter2() {
        return this.daughter2;
    }

    public Object3D getMother() {
        return this.mother;
    }

    public void setMother(Object3D mother) {
        this.mother = mother;
    }

    public double getColocMitosis() {
        return this.colocMitosis;
    }

    public void setColocMitosis(double colocMitosis) {
        this.colocMitosis = colocMitosis;
    }

    public boolean hasObject(Object3D object3D) {
        int val = object3D.getValue();
        return (val == this.daughter1.getValue() || val == this.daughter2.getValue() || val == this.mother.getValue());
    }

    private Object3D createConvexDaughters() {
        Object3DVoxels voxels1 = new Object3DVoxels();
        voxels1.addVoxelsUnion(this.daughter1.getObject3DVoxels(), this.daughter2.getObject3DVoxels());
        return voxels1.getConvexObject();
    }

    private Vector3D getCenterDaughters() {
        Vector3D cen1 = this.daughter1.getCenterAsVector();
        Vector3D cen2 = this.daughter2.getCenterAsVector();
        Vector3D middle = cen1.add(cen2).multiply(0.5D);

        return middle;
    }

    public void getPotentialMother(ImageHandler image1) {
        Objects3DPopulation population = new Objects3DPopulation(image1);
        Object3D convex = createConvexDaughters();
        ArrayUtil values = convex.listValues(image1).distinctValues();
        int bestMother = 0;
        double maxColoc = 0.0D;
        Vector3D cen1 = this.daughter1.getCenterAsVector();
        Vector3D cen2 = this.daughter2.getCenterAsVector();
        Vector3D mid = getCenterDaughters();
        this.daughter1.setNewCenter(mid);
        this.daughter2.setNewCenter(mid);
        IJ.log("Middle " + mid);
        for (int i = 0; i < values.size(); i++) {
            int val = values.getValueInt(i);
            if (val != 0) {
                Object3D object3D = population.getObjectByValue(val);
                double pc1 = object3D.pcColoc(this.daughter1);
                double pc2 = object3D.pcColoc(this.daughter2);
                if (Math.min(pc1, pc2) > maxColoc) {
                    maxColoc = Math.min(pc1, pc2);
                    bestMother = val;
                }
            }
        }
        this.daughter1.setNewCenter(cen1);
        this.daughter2.setNewCenter(cen2);
        IJ.log("Values mitosis : " + values.toString() + " Best : " + bestMother + " " + maxColoc);
    }
}