package mcib3d.geom2.measurements;

import mcib3d.Jama.EigenvalueDecomposition;
import mcib3d.Jama.Matrix;
import mcib3d.geom.Vector3D;
import mcib3d.geom2.Object3DInt;


public class MeasureEllipsoid extends MeasureAbstract {
    public final static String ELL_VOL_UNIT = "EllVol(Unit)";
    public final static String ELL_SPARENESS = "EllSpareness";
    public final static String ELL_MAJOR_RADIUS_UNIT = "EllMajRad(Unit)";
    public final static String ELL_ELONGATION = "EllElon";
    public final static String ELL_FLATNESS = "EllFlatness";

    private double radius1, radius2, radius3;
    private Vector3D Axis1, Axis2, Axis3;

    public MeasureEllipsoid(Object3DInt object3DInt) {
        super(object3DInt);
    }

    @Override
    protected String[] getNames() {
        return new String[]{ELL_VOL_UNIT, ELL_SPARENESS, ELL_MAJOR_RADIUS_UNIT, ELL_ELONGATION, ELL_FLATNESS};
    }

    public Double[] getRadii() {
        return new Double[]{radius1, radius2, radius3};
    }

    public Vector3D getAxis1() {
        if (Axis1 == null) computeAll();
        return Axis1;
    }

    public Vector3D getAxis2() {
        if (Axis2 == null) computeAll();
        return Axis2;
    }

    public Vector3D getAxis3() {
        if (Axis3 == null) computeAll();
        return Axis3;
    }

    @Override
    protected void computeAll() {
        Matrix mat = new Matrix(3, 3);
        MeasureCentroid centroid = new MeasureCentroid(object3DInt);
        Double[] sums = computation3D.computeMoments2(centroid.getCentroidAsVoxel(), true);

        mat.set(0, 0, sums[0]); // xx
        mat.set(0, 1, sums[3]); // xy
        mat.set(0, 2, sums[4]); // xz
        mat.set(1, 0, sums[3]); // xy
        mat.set(1, 1, sums[1]); // yy
        mat.set(1, 2, sums[5]); // yz
        mat.set(2, 0, sums[4]); // xz
        mat.set(2, 1, sums[5]); // yz
        mat.set(2, 2, sums[2]); // zz

        EigenvalueDecomposition eigen = new EigenvalueDecomposition(mat);
        double[] eigenValues = eigen.getRealEigenvalues();

        double R1 = Math.sqrt(5.0 * eigenValues[2]);
        double R2 = Math.sqrt(5.0 * eigenValues[1]);
        double R3 = Math.sqrt(5.0 * eigenValues[0]);
        double volEll = (4.0 / 3.0) * Math.PI * R1 * R2 * R3;
        MeasureVolume volume = new MeasureVolume(object3DInt);

        keysValues.put(ELL_VOL_UNIT, volEll);
        keysValues.put(ELL_SPARENESS, volume.getValueMeasurement(MeasureVolume.VOLUME_UNIT) / volEll);
        keysValues.put(ELL_MAJOR_RADIUS_UNIT, R1);
        keysValues.put(ELL_ELONGATION, R1 / R2);
        keysValues.put(ELL_FLATNESS, R2 / R3);

        radius1 = R1;
        radius2 = R2;
        radius3 = R3;

        // vectors
        Matrix eVectors = eigen.getV();
        Axis1 = new Vector3D(eVectors.get(0, 2), eVectors.get(1, 2), eVectors.get(2, 2));
        Axis2 = new Vector3D(eVectors.get(0, 1), eVectors.get(1, 1), eVectors.get(2, 1));
        Axis3 = new Vector3D(eVectors.get(0, 0), eVectors.get(1, 0), eVectors.get(2, 0));
    }
}
