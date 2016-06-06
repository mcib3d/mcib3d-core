package mcib3d.image3d.IterativeThresholding;

import mcib3d.geom.Object3D;

/**
 * Created by thomasb on 20/4/16.
 */
public class CriterionCompactness implements Criterion {
    @Override
    public double computeCriterion(Object3D object3D) {
        return object3D.getCompactness();
    }
}
