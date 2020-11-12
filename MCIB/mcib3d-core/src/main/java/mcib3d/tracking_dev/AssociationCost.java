package mcib3d.tracking_dev;

import mcib3d.geom.Object3D;

public interface AssociationCost {
     double cost(Object3D object3D1, Object3D object3D2);
}
