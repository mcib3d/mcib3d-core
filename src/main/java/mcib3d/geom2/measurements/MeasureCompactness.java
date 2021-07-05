package mcib3d.geom2.measurements;


import mcib3d.geom2.Object3DInt;

public class MeasureCompactness extends MeasureAbstract {
    public final static String COMP_UNIT = "Compactness(Pix)";
    public final static String COMP_PIX = "Compactness(Unit)";
    public final static String COMP_CORRECTED = "CompactCorr(Pix)";
    public final static String COMP_DISCRETE = "CompactDiscrete(Pix)";
    public final static String SPHER_UNIT = "Sphericity(Pix)";
    public final static String SPHER_PIX = "Sphericity(Unit)";
    public final static String SPHER_CORRECTED = "SpherCorr(Pix)";
    public final static String SPHER_DISCRETE = "SpherDiscrete(Pix)";

    public MeasureCompactness(Object3DInt object3DInt) {
        super(object3DInt);
    }

    @Override
    protected String[] getNames() {
        return new String[]{COMP_UNIT, COMP_PIX, COMP_CORRECTED, COMP_DISCRETE, SPHER_PIX, SPHER_UNIT, SPHER_CORRECTED, SPHER_DISCRETE};
    }

    @Override
    protected void computeAll() {
        MeasureVolume volume = new MeasureVolume(object3DInt);
        MeasureSurface surface = new MeasureSurface(object3DInt);

        double s3 = Math.pow(surface.getSurfaceContactPix(), 3);
        double v2 = Math.pow(volume.getVolumePix(), 2);
        double c = (v2 * 36.0 * Math.PI) / s3;
        keysValues.put(COMP_PIX, c);
        keysValues.put(SPHER_PIX, Math.pow(c, 1.0 / 3.0));

        s3 = Math.pow(surface.getSurfaceCorrectedPix(), 3);
        c = (v2 * 36.0 * Math.PI) / s3;
        keysValues.put(COMP_CORRECTED,c);
        keysValues.put(SPHER_CORRECTED, Math.pow(c, 1.0 / 3.0));

        // From Bribiesca 2008 Pattern Recognition
        // An easy measure of compactness for 2D and 3D shapes
        double v = volume.getVolumePix();
        double tmp = Math.pow(v, 2.0 / 3.0);
        c = ((v - surface.getSurfaceContactPix() / 6.0) / (v - tmp));
        keysValues.put(COMP_DISCRETE, c);
        keysValues.put(SPHER_DISCRETE, Math.pow(c, 1.0 / 3.0));

        s3 = Math.pow(surface.getSurfaceContactUnit(), 3);
        v2 = Math.pow(volume.getVolumeUnit(), 2);
        c = (v2 * 36.0 * Math.PI) / s3;
        keysValues.put(COMP_UNIT, c);
        keysValues.put(SPHER_UNIT, Math.pow(c, 1.0 / 3.0));
    }
}
