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
import mcib3d.image3d.distanceMap3d.EDT;
import mcib3d.image3d.processing.FastFilters3D;

/**
 * Created by thomasb on 17/8/16.
 */
public class Watershed3DVoronoi {
    ImageInt seeds = null;
    float radiusMax = Float.MAX_VALUE;
    ImageFloat EDTImage = null;
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
        EDTImage = null;
        watershed = null;
    }

    public void setRadiusMax(float radiusMax) {
        this.radiusMax = radiusMax;
        voronoi = null;
    }

    private void computeEDT(boolean show) {
        IJ.log("Computing EDT");
        Calibration cal = seeds.getCalibration();
        float resXY = 1;
        float resZ = 1;
        if (cal != null) {
            resXY = (float) cal.pixelWidth;
            resZ = (float) cal.pixelDepth;
        }
        EDTImage = EDT.run(seeds, 0, resXY, resZ, true, 0);
        if (show) EDTImage.show("EDT");
    }

    private void computeWatershed(boolean show) {
        if (EDTImage == null) computeEDT(show);
        IJ.log("Computing Watershed");
        ImageFloat EDTcopy = EDTImage.duplicate();
        ImageHandler edt16 = EDTcopy.convertToShort(true);
        edt16.invert();
        Watershed3D water = new Watershed3D(edt16, seeds, 0, 0);
        watershed = water.getWatershedImage3D();
    }

    private void computeVoronoi(boolean show) {
        if (watershed == null) computeWatershed(show);
        IJ.log("Computing Voronoi");
        ImageByte mask = EDTImage.threshold(radiusMax, true, true);
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
