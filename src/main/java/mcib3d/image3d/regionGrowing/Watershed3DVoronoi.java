package mcib3d.image3d.regionGrowing;

import mcib3d.image3d.ImageByte;
import mcib3d.image3d.ImageFloat;
import mcib3d.image3d.ImageHandler;
import mcib3d.image3d.ImageInt;
import mcib3d.image3d.distanceMap3d.EDT;
import mcib3d.utils.Logger.AbstractLog;
import mcib3d.utils.Logger.IJLog;

/**
 * Created by thomasb on 17/8/16.
 */
public class Watershed3DVoronoi {
    private ImageInt seeds;
    private float radiusMax = Float.MAX_VALUE;
    private ImageFloat EDTImage = null;
    private ImageInt watershed = null;
    private ImageInt voronoi = null;
    private ImageInt lines = null;
    private boolean labelSeeds = false;
    private AbstractLog log = new IJLog();

    public Watershed3DVoronoi(ImageInt seeds) {
        this.seeds = seeds;
    }

    public Watershed3DVoronoi(ImageInt seeds, float radiusMax) {
        this.seeds = seeds;
        if (!Float.isNaN(radiusMax))
            this.radiusMax = radiusMax;
    }

    public void setSeeds(ImageInt seeds) {
        this.seeds = seeds;
        EDTImage = null;
        watershed = null;
    }

    public void setRadiusMax(float radiusMax) {
        if (!Float.isNaN(radiusMax)) {
            this.radiusMax = radiusMax;
            voronoi = null;
        }
    }

    public void setLabelSeeds(boolean labelSeeds) {
        this.labelSeeds = labelSeeds;
    }

    private void computeEDT(boolean show) {
        log.log("Computing EDT");
        float resXY = (float) seeds.getScaleXY();
        float resZ = (float) seeds.getScaleZ();
        EDTImage = EDT.run(seeds, 0, resXY, resZ, true, 0);
        if (show) EDTImage.show("EDT");
    }

    private void computeWatershed(boolean show) {
        if (EDTImage == null) computeEDT(show);
        log.log("Computing Watershed");
        ImageFloat EDTcopy = EDTImage.duplicate();
        double max = EDTcopy.getMax();
        EDTcopy.invert();
        EDTcopy.addValue((float) max + 1);
        Watershed3D water = new Watershed3D(EDTcopy, seeds, 0, 0);
        water.setLog(log);
        water.setLabelSeeds(labelSeeds);
        watershed = water.getWatershedImage3D();
        lines = water.getDamImage();
    }

    private void computeVoronoi(boolean show) {
        if (watershed == null) computeWatershed(show);
        log.log("Computing Voronoi");
        ImageByte mask = EDTImage.threshold(radiusMax, true, true);
        voronoi = watershed.duplicate();
        voronoi.intersectMask((ImageHandler)mask);
    }

    public ImageInt getVoronoiZones(boolean show) {
        if (voronoi == null) computeVoronoi(show);
        voronoi.setScale(seeds);

        return voronoi;
    }

    public ImageInt getVoronoiLines(boolean show) {
        if (voronoi == null) computeVoronoi(show);
        log.log("Computing voronoi lines");
        ImageByte mask = EDTImage.threshold(radiusMax, true, true);
        voronoi = lines.duplicate();
        voronoi.intersectMask((ImageHandler)mask);
        voronoi.setScale(seeds);

        return voronoi;
    }

    public void setLog(AbstractLog logger) {
        this.log = logger;
    }
}
