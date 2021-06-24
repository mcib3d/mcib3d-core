package mcib3d.geom2.measurements;

import mcib3d.geom.Voxel3D;
import mcib3d.geom2.Object3DInt;
import mcib3d.geom2.VoxelInt;
import mcib3d.image3d.ImageHandler;

import java.util.List;

public class MeasureDistancesCenter extends MeasureAbstract {
    public final static String DIST_CENTER_MIN_PIX = "DistCenterMinPix";
    public final static String DIST_CENTER_MAX_PIX = "DistCenterMaxPix";
    public final static String DIST_CENTER_AVG_PIX = "DistCenterAvgPix";
    public final static String DIST_CENTER_SD_PIX = "DistCenterSDPix";
    public final static String DIST_CENTER_MIN_UNIT = "DistCenterMinUnit";
    public final static String DIST_CENTER_MAX_UNIT = "DistCenterMaxUnit";
    public final static String DIST_CENTER_AVG_UNIT = "DistCenterAvgUnit";
    public final static String DIST_CENTER_SD_UNIT = "DistCenterSDUnit";

    public MeasureDistancesCenter(Object3DInt object3DInt) {
        super(object3DInt);
    }

    @Override
    protected String[] getNames() {
        return new String[]{DIST_CENTER_MIN_PIX, DIST_CENTER_MAX_PIX, DIST_CENTER_AVG_PIX, DIST_CENTER_SD_PIX,
                DIST_CENTER_MIN_UNIT, DIST_CENTER_MAX_UNIT, DIST_CENTER_AVG_UNIT, DIST_CENTER_SD_UNIT};
    }

    @Override
    protected void computeAll() {
        // check if centre is inside object
        ImageHandler labelImage = computation3D.createLabelImage();

        MeasureCentroid centroid = new MeasureCentroid(object3DInt);
        VoxelInt centreInt = centroid.getCentroidRoundedAsVoxelInt();
        if (labelImage.getPixel(centreInt.x, centreInt.y, centreInt.z) == 0) {
            // pix
            keysValues.put(DIST_CENTER_MIN_PIX, Double.NaN);
            keysValues.put(DIST_CENTER_MAX_PIX, Double.NaN);
            keysValues.put(DIST_CENTER_AVG_PIX, Double.NaN);
            keysValues.put(DIST_CENTER_SD_PIX, Double.NaN);

            // unit
            keysValues.put(DIST_CENTER_MIN_UNIT, Double.NaN);
            keysValues.put(DIST_CENTER_MAX_UNIT, Double.NaN);
            keysValues.put(DIST_CENTER_AVG_UNIT, Double.NaN);
            keysValues.put(DIST_CENTER_SD_UNIT, Double.NaN);

            return;
        }

        List<VoxelInt> contour = computation3D.getContourFromImage(labelImage);

        // compute all distances from center to contour
        Voxel3D centre = centroid.getCentroidAsVoxel();
        // min, max, sum, sum²
        final double[] distancesPix = {Double.POSITIVE_INFINITY, 0, 0, 0};
        final double[] distancesUnit = {Double.POSITIVE_INFINITY, 0, 0, 0};
        double xy = object3DInt.getResXY();
        double xz = object3DInt.getResZ();
        contour.forEach(V -> {
            // pix
            double dist2Pix = V.distanceSquare(centre);
            double distPix = Math.sqrt(dist2Pix);
            distancesPix[0] = Math.min(distancesPix[0], distPix);
            distancesPix[1] = Math.max(distancesPix[1], distPix);
            distancesPix[2] += distPix;
            distancesPix[3] += dist2Pix;
            // unit
            dist2Pix = V.distanceSquareScaled(centre, xy, xz);
            distPix = Math.sqrt(dist2Pix);
            distancesUnit[0] = Math.min(distancesUnit[0], distPix);
            distancesUnit[1] = Math.max(distancesUnit[1], distPix);
            distancesUnit[2] += distPix;
            distancesUnit[3] += dist2Pix;

        });
        double nbContour = contour.size();
        // pix
        keysValues.put(DIST_CENTER_MIN_PIX, distancesPix[0]);
        keysValues.put(DIST_CENTER_MAX_PIX, distancesPix[1]);
        keysValues.put(DIST_CENTER_AVG_PIX, distancesPix[2] / nbContour);
        keysValues.put(DIST_CENTER_SD_PIX, Math.sqrt((distancesPix[3] - ((distancesPix[2] * distancesPix[2]) / nbContour)) / (nbContour - 1)));

        // unit
        keysValues.put(DIST_CENTER_MIN_UNIT, distancesUnit[0]);
        keysValues.put(DIST_CENTER_MAX_UNIT, distancesUnit[1]);
        keysValues.put(DIST_CENTER_AVG_UNIT, distancesUnit[2] / nbContour);
        keysValues.put(DIST_CENTER_SD_UNIT, Math.sqrt((distancesUnit[3] - ((distancesUnit[2] * distancesUnit[2]) / nbContour)) / (nbContour - 1)));
    }
}
