package mcib3d.geom2.measurements;

import mcib3d.geom2.Object3DInt;

public class MeasureVolume extends MeasureAbstract {
    public final static String VOLUME_PIX = "Volume(Pix)";
    public final static String VOLUME_UNIT = "Volume(Unit)";

    public MeasureVolume(Object3DInt object3DInt) {
        super(object3DInt);
    }

    @Override
    protected String[] getNames() {
        return new String[]{VOLUME_PIX, VOLUME_UNIT};
    }

    @Override
    protected void computeAll() {
        computeGeometryVolume();
    }

    public double getVolumePix() {
        if (keysValues.get(VOLUME_PIX) == null) computeGeometryVolume();

        return keysValues.get(VOLUME_PIX);
    }

    public double getVolumeUnit() {
        if (keysValues.get(VOLUME_UNIT) == null) computeGeometryVolume();

        return keysValues.get(VOLUME_UNIT);
    }

    private void computeGeometryVolume() {
        // volumes
        keysValues.put(VOLUME_PIX, object3DInt.size());
        keysValues.put(VOLUME_UNIT, object3DInt.size() * object3DInt.getResXY() * object3DInt.getResXY() * object3DInt.getResZ());
    }
}
