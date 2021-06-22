package mcib3d.geom2.measurements;

import mcib3d.geom.Point3D;
import mcib3d.geom.Voxel3D;
import mcib3d.geom2.Object3D;
import mcib3d.geom2.VoxelInt;

public class MeasureCentroid extends MeasureAbstract {
    public final String CX_PIX = "CxPix";
    public final String CY_PIX = "CyPix";
    public final String CZ_PIX = "CzPix";
    public final String CX_UNIT = "CxUnit";
    public final String CY_UNIT = "CyUnit";
    public final String CZ_UNIT = "CzUnit";

    private double cx = Double.NaN, cy = Double.NaN, cz = Double.NaN;

    public MeasureCentroid(Object3D object3D) {
        super(object3D);
    }

    @Override
    public String[] getNames() {
        return new String[]{CX_PIX, CY_PIX, CZ_PIX, CX_UNIT, CY_UNIT, CZ_UNIT};
    }

    @Override
    public void computeAll() {
        computeCentroid();
    }

    private void computeCentroid() {
        // need double precision in sum
        Voxel3D sum = computation3D.getSumCoordinates();
        double vol = object3D.size();

        // pix
        double cx = sum.getX() / vol, cy = sum.getY() / vol, cz = sum.getZ() / vol;
        keysValues.put(CX_PIX, cx);
        keysValues.put(CY_PIX, cy);
        keysValues.put(CZ_PIX, cz);

        // unit
        double xy = object3D.getResXY();
        double xz = object3D.getResZ();
        keysValues.put(CX_UNIT, cx * xy);
        keysValues.put(CY_UNIT, cy * xy);
        keysValues.put(CZ_UNIT, cz * xz);
    }

    public Point3D getCentroidAsPoint() {
        return new Point3D(getValue(CX_PIX), getValue(CY_PIX), getValue(CZ_PIX));
    }

    public Voxel3D getCentroidAsVoxel() {
        Point3D point3D = getCentroidAsPoint();
        return new Voxel3D(point3D.getX(), point3D.getY(), point3D.getZ(), object3D.getValue());
    }

    public VoxelInt getCentroidRoundedAsVoxelInt() {
        Point3D voxel3D = getCentroidAsPoint();
        return new VoxelInt(voxel3D.getRoundX(), voxel3D.getRoundY(), voxel3D.getRoundZ(), object3D.getValue());
    }
}
