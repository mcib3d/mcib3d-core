package mcib3d.geom2.measurements;

import mcib3d.geom2.Object3DInt;
import mcib3d.geom2.Object3DPlane;
import mcib3d.image3d.ImageHandler;

public class MeasureIntensity extends MeasureAbstract {
    public static final String INTENSITY_AVG = "IntensityAvg";
    public static final String INTENSITY_MIN = "IntensityMin";
    public static final String INTENSITY_MAX = "IntensityMax";
    public static final String INTENSITY_SD = "IntensityStdDev";
    public static final String INTENSITY_SUM = "IntensitySum";

    private ImageHandler intensityImage = null;

    public MeasureIntensity(Object3DInt object3DInt) {
        super(object3DInt);
    }

    public void setIntensityImage(ImageHandler image) {
        this.intensityImage = image;
        computeAll();
    }

    @Override
    protected String[] getNames() {
        return new String[]{INTENSITY_MIN, INTENSITY_MAX, INTENSITY_AVG, INTENSITY_SD, INTENSITY_SUM};
    }

    @Override
    protected void computeAll() {
        if (intensityImage == null) return;

        Double[] values = computeIntensityValues(intensityImage);
        keysValues.put(INTENSITY_MIN, values[0]);
        keysValues.put(INTENSITY_MAX, values[1]);
        keysValues.put(INTENSITY_AVG, values[2]);
        keysValues.put(INTENSITY_SD, values[3]);
        keysValues.put(INTENSITY_SUM, values[4]);
    }

    private Double[] computeIntensityValues(ImageHandler ima) {
        if (ima == null) return new Double[]{Double.NaN, Double.NaN, Double.NaN, Double.NaN, Double.NaN};

        double sum = 0, sum2 = 0, min = Double.POSITIVE_INFINITY, max = Double.NEGATIVE_INFINITY, nb = 0;
        Double[] values = new Double[]{sum, sum2, min, max, nb};

        object3DInt.getObject3DPlanes().stream().forEach(plane -> updateIntensityValues(plane, values));
        sum = values[0];
        sum2 = values[1];
        min = values[2];
        max = values[3];
        nb = values[4];

        double integratedDensity = sum;
        double meanDensity = integratedDensity / nb;
        if (min == Double.POSITIVE_INFINITY) min = Double.NaN;
        if (max == Double.NEGATIVE_INFINITY) max = Double.NaN;
        // standard dev
        double sigma = Math.sqrt((sum2 - ((sum * sum) / nb)) / (nb - 1));

        return new Double[]{min, max, meanDensity, sigma, integratedDensity};
    }

    private void updateIntensityValues(Object3DPlane plane, Double[] values) {
        plane.getVoxels().stream()
                .forEach(V -> {
                    float pix = intensityImage.getPixel(V.x, V.y, V.z);
                    if (!Float.isNaN(pix)) {
                        values[0] += pix;
                        values[1] += pix * pix;
                        values[2] = Math.min(values[2], pix);
                        values[3] = Math.max(values[3], pix);
                        values[4]++;
                    }
                });
    }
}
