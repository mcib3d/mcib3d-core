package mcib3d.image3d.regionGrowing;

import ij.IJ;
import ij.measure.Calibration;
import mcib3d.geom.Object3DVoxels;
import mcib3d.geom.ObjectCreator3D;
import mcib3d.geom.Objects3DPopulation;
import mcib3d.image3d.ImageByte;
import mcib3d.image3d.ImageFloat;
import mcib3d.image3d.ImageHandler;
import mcib3d.image3d.ImageInt;
import mcib3d.image3d.processing.FastFilters3D;

/**
 * Created by thomasb on 17/8/16.
 */
public class Watershed3DVoronoi {
    ImageInt seeds = null;
    float radiusMax = Float.MAX_VALUE;
    ImageFloat EDT = null;
    ImageInt watershed = null;
    ImageInt voronoi = null;

    public Watershed3DVoronoi(ImageInt seeds) {
        this.seeds = seeds;
    }

    public Watershed3DVoronoi(ImageInt seeds, float radiusMax) {
        this.seeds = seeds;
        this.radiusMax = radiusMax;
    }

    public void setSeeds(ImageInt seeds) {
        this.seeds = seeds;
        EDT = null;
        watershed = null;
    }

    public void setRadiusMax(float radiusMax) {
        this.radiusMax = radiusMax;
        voronoi = null;
    }

    private void computeEDT() {
        Calibration cal = seeds.getCalibration();
        float resXY = 1;
        float resZ = 1;
        if (cal != null) {
            resXY = (float) cal.pixelWidth;
            resZ = (float) cal.pixelDepth;
        }
        EDT = mcib3d.image3d.distanceMap3d.EDT.run(seeds, 0, resXY, resZ, true, 0);
    }

    private void computeWatershed(boolean show) {
        if (EDT == null) computeEDT();
        ImageFloat EDTcopy = EDT.duplicate();
        EDTcopy.invert();
        if (show) {
            EDTcopy.show("EDT");
        }
        ImageHandler edt16 = EDT.convertToShort(true);
        edt16.invert();
        IJ.log("Computing watershed");
        Watershed3D water = new Watershed3D(edt16, seeds, 0, 0);
        water.setAnim(false);
        watershed = water.getWatershedImage3D();
    }

    private void computeVoronoi(boolean show) {
        if (watershed == null) computeWatershed(show);
        ImageByte mask = EDT.threshold(radiusMax, true, true);
        voronoi = watershed.duplicate();
        voronoi.intersectMask(mask);
    }

    public ImageInt getVoronoiZones(boolean show) {
        if (voronoi == null) computeVoronoi(show);

        return voronoi;
    }

    public ImageInt getVoronoiLines(boolean show) {
        if (voronoi == null) computeVoronoi(show);
        // lines
        Objects3DPopulation pop = new Objects3DPopulation(voronoi);
        ObjectCreator3D draw = new ObjectCreator3D(seeds.sizeX, seeds.sizeY, seeds.sizeZ);
        for (int o = 0; o < pop.getNbObjects(); o++) {
            Object3DVoxels obj = (Object3DVoxels) pop.getObject(o);
            obj.computeContours();
            if (seeds.sizeZ > 1)
                obj.drawContours(draw, 1);
            else {
                obj.drawContoursXY(draw, 0, 1);
            }
        }
        ImageHandler lines = draw.getImageHandler();
        lines = FastFilters3D.filterImage(lines, FastFilters3D.CLOSEGRAY, 1, 1, 1, 0, false);

        return (ImageInt) lines;
    }
}
