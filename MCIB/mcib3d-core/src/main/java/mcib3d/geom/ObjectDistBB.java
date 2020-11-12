package mcib3d.geom;

public class ObjectDistBB {
    Object3D object3D;
    double distBB;

    public ObjectDistBB(Object3D object3D, double distBB) {
        this.object3D = object3D;
        this.distBB = distBB;
    }

    public Object3D getObject3D() {
        return object3D;
    }

    public double getDistBB() {
        return distBB;
    }
}
