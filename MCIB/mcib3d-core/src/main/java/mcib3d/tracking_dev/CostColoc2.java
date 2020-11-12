package mcib3d.tracking_dev;

import mcib3d.geom.Object3D;

public class CostColoc2
        implements AssociationCost {

    public double cost(Object3D object3D1, Object3D object3D2) {
        double cost = -1.0D;
        double V1 = object3D1.getVolumePixels();
        double V2 = object3D2.getVolumePixels();
        int coloc = object3D1.getColoc(object3D2);
        if (coloc > 0) cost = 1.0D - coloc / Math.max(V1, V2);

        return cost;
    }
}