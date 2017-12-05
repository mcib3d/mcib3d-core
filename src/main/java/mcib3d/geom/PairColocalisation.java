package mcib3d.geom;

public class PairColocalisation {
    private Object3D object3D1;
    private Object3D object3D2;
    private int volumeColoc;

    public PairColocalisation(Object3D object3D1, Object3D object3D2, int volumeColoc) {
        this.object3D1 = object3D1;
        this.object3D2 = object3D2;
        this.volumeColoc = volumeColoc;
    }

    public Object3D getObject3D1() {
        return object3D1;
    }

    public Object3D getObject3D2() {
        return object3D2;
    }

    public int getVolumeColoc() {
        return volumeColoc;
    }
}
