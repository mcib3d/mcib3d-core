package mcib3d.geom.interactions;

import mcib3d.image3d.ImageHandler;
import mcib3d.image3d.regionGrowing.Watershed3DVoronoi;

public class InteractionsComputeVoronoi implements InteractionsCompute {
    @Override
    public InteractionsList compute(ImageHandler image) {
        // first compute voronoi
        Watershed3DVoronoi watershed3DVoronoi = new Watershed3DVoronoi(image);
        ImageHandler voronoi = watershed3DVoronoi.getVoronoiZones(false);
        // second compute interactions Lines
        return new InteractionsComputeDamLines().compute(voronoi);
    }
}
