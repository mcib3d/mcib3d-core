package mcib3d.tracking_dev;

import mcib3d.geom.Object3D;

public class AssociationPair {
    private final Object3D object3D1;
    private final Object3D object3D2;
    private final double asso;

    public AssociationPair(Object3D object3D1, Object3D object3D2, double asso) {
        this.object3D1 = object3D1;
        this.object3D2 = object3D2;
        this.asso = asso;
    }

    public Object3D getObject3D1() {
        return this.object3D1;
    }

    public Object3D getObject3D2() {
        return this.object3D2;

    }

    public double getAsso() {
        return this.asso;
    }

}