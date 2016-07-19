package mcib3d.image3d.IterativeThresholding;

import mcib3d.geom.Object3D;
import mcib3d.image3d.ImageHandler;
import mcib3d.image3d.processing.CannyEdge3D;

/**
 * Created by thomasb on 20/4/16.
 */
public class CriterionEdge implements Criterion {
    ImageHandler edges;

    public CriterionEdge(ImageHandler imageHandler, double alpha) {
        // compute edge image
        CannyEdge3D cannyEdge3D = new CannyEdge3D(imageHandler, alpha);
        edges = cannyEdge3D.getEdge();

    }

    @Override
    public double computeCriterion(Object3D object3D) {
        return object3D.getPixMeanValueContour(edges);
    }
}
