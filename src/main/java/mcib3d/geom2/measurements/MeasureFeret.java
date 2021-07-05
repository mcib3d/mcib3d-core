package mcib3d.geom2.measurements;

import ij.IJ;
import mcib3d.geom.Voxel3D;
import mcib3d.geom2.Object3DInt;
import mcib3d.geom2.VoxelInt;

import java.util.List;

public class MeasureFeret extends MeasureAbstract {
    public static final String FERET_UNIT = "FeretUnit";
    public static final String FERET1_X = "Feret1X(pix)";
    public static final String FERET1_Y = "Feret1Y(pix)";
    public static final String FERET1_Z = "Feret1Z(pix)";
    public static final String FERET2_X = "Feret2X(pix)";
    public static final String FERET2_Y = "Feret2YY(pix)";
    public static final String FERET2_Z = "Feret2Z(pix)";

    private VoxelInt feret1 = null, feret2 = null;
    private double feret = Double.NaN;

    public MeasureFeret(Object3DInt object3DInt) {
        super(object3DInt);
    }

    @Override
    protected String[] getNames() {
        return new String[]{FERET_UNIT, FERET1_X, FERET1_Y, FERET1_Z, FERET2_X, FERET2_Y, FERET2_Z};
    }

    @Override
    protected void computeAll() {
        computeFeret();
        keysValues.put(FERET_UNIT, feret);
        keysValues.put(FERET1_X, (double) feret1.x);
        keysValues.put(FERET1_Y, (double) feret1.y);
        keysValues.put(FERET1_Z, (double) feret1.z);
        keysValues.put(FERET2_X, (double) feret2.x);
        keysValues.put(FERET2_Y, (double) feret2.y);
        keysValues.put(FERET2_Z, (double) feret2.z);
    }

    public Voxel3D getFeret1() {
        if (feret1 == null) computeFeret();
        return new Voxel3D(feret1.x, feret1.y, feret1.z, object3DInt.getValue());
    }

    public Voxel3D getFeret2() {
        if (feret2 == null) computeFeret();
        return new Voxel3D(feret2.x, feret2.y, feret2.z, object3DInt.getValue());
    }

    private void computeFeret() {
        double distmax = 0;
        double dist;
        double rx = object3DInt.getResXY();
        double rz = object3DInt.getResZ();
        VoxelInt p1;
        VoxelInt p2;
        List<VoxelInt> cont = computation3D.getContour();

        int s = cont.size();
        // case object only one voxel
        if (s == 1) {
            feret1 = cont.get(0);
            feret2 = cont.get(0);
            feret = 0;
        }

        VoxelInt[] voxel3DS = new VoxelInt[cont.size()];
        voxel3DS = cont.toArray(voxel3DS);

        for (int i1 = 0; i1 < voxel3DS.length; i1++) {
            IJ.showStatus("Feret " + i1 + "/" + voxel3DS.length);
            p1 = voxel3DS[i1];
            for (int i2 = i1 + 1; i2 < voxel3DS.length; i2++) {
                p2 = voxel3DS[i2];
                dist = p1.distanceSquareScaled(p2, rx, rz);
                if (dist > distmax) {
                    distmax = dist;
                    feret1 = p1;
                    feret2 = p2;
                }
            }
            i1++;
        }
        feret = (float) Math.sqrt(distmax);
    }
}
