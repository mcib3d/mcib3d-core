package mcib3d.geom2.measurements;

import mcib3d.geom2.Object3D;

public class MeasureVolume extends MeasureAbstract {
    public final String VOLUME_PIX = "VolumePix";
    public final String VOLUME_UNIT = "VolumeUnit";

    public MeasureVolume(Object3D object3D) {
        super(object3D);
    }

    @Override
    public String[] getNames() {
        return new String[]{VOLUME_PIX, VOLUME_UNIT};
    }

    @Override
    public void computeAll() {
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
        keysValues.put(VOLUME_PIX, object3D.size());
        keysValues.put(VOLUME_UNIT, object3D.size() * object3D.getResXY() * object3D.getResXY() * object3D.getResZ());
    }
}
