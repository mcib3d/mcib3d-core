package mcib3d.geom2.measurements;

import mcib3d.geom.Point3D;
import mcib3d.geom.Voxel3D;
import mcib3d.geom2.Object3DInt;
import mcib3d.geom2.VoxelInt;

public class MeasureCentroid extends MeasureAbstract {
    public final static String CX_PIX = "CX(pix)";
    public final static String CY_PIX = "CY(pix)";
    public final static String CZ_PIX = "CZ(pix)";
    public final static String CX_UNIT = "CX(unit)";
    public final static String CY_UNIT = "CY(unit)";
    public final static String CZ_UNIT = "CZ(unit)";

    private double cx = Double.NaN, cy = Double.NaN, cz = Double.NaN;

    public MeasureCentroid(Object3DInt object3DInt) {
        super(object3DInt);
    }

    @Override
    protected String[] getNames() {
        return new String[]{CX_PIX, CY_PIX, CZ_PIX, CX_UNIT, CY_UNIT, CZ_UNIT};
    }

    @Override
    protected void computeAll() {
        computeCentroid();
    }

    private void computeCentroid() {
        // need double precision in sum
        Voxel3D sum = computation3D.getSumCoordinates();
        double vol = object3DInt.size();

        // pix
        double cx = sum.getX() / vol, cy = sum.getY() / vol, cz = sum.getZ() / vol;
        keysValues.put(CX_PIX, cx);
        keysValues.put(CY_PIX, cy);
        keysValues.put(CZ_PIX, cz);

        // unit
        double xy = object3DInt.getResXY();
        double xz = object3DInt.getResZ();
        keysValues.put(CX_UNIT, cx * xy);
        keysValues.put(CY_UNIT, cy * xy);
        keysValues.put(CZ_UNIT, cz * xz);
    }

    public Point3D getCentroidAsPoint() {
        return new Point3D(getValueMeasurement(CX_PIX), getValueMeasurement(CY_PIX), getValueMeasurement(CZ_PIX));
    }

    public Voxel3D getCentroidAsVoxel() {
        Point3D point3D = getCentroidAsPoint();
        return new Voxel3D(point3D.getX(), point3D.getY(), point3D.getZ(), object3DInt.getValue());
    }

    public VoxelInt getCentroidRoundedAsVoxelInt() {
        Point3D voxel3D = getCentroidAsPoint();
        return new VoxelInt(voxel3D.getRoundX(), voxel3D.getRoundY(), voxel3D.getRoundZ(), object3DInt.getValue());
    }
}
