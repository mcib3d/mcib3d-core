package mcib3d.geom2.measurements;

import ij.IJ;
import mcib3d.geom.Voxel3D;
import mcib3d.geom2.Object3D;
import mcib3d.geom2.VoxelInt;

import java.util.List;

public class MeasureFeret extends MeasureAbstract {
    public static final String FERET_UNIT = "FeretUnit";

    private VoxelInt feret1 = null, feret2 = null;
    private double feret = Double.NaN;

    public MeasureFeret(Object3D object3D) {
        super(object3D);
    }

    @Override
    public String[] getNames() {
        return new String[]{FERET_UNIT};
    }

    @Override
    protected void computeAll() {
        computeFeret();
        keysValues.put(FERET_UNIT, feret);
    }

    public Voxel3D getFeret1() {
        if (feret1 == null) computeFeret();
        return new Voxel3D(feret1.x, feret1.y, feret1.z, object3D.getValue());
    }

    public Voxel3D getFeret2() {
        if (feret2 == null) computeFeret();
        return new Voxel3D(feret2.x, feret2.y, feret2.z, object3D.getValue());
    }

    private void computeFeret() {
        double distmax = 0;
        double dist;
        double rx = object3D.getResXY();
        double rz = object3D.getResZ();
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
