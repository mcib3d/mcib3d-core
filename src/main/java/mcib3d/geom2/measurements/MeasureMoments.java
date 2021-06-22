package mcib3d.geom2.measurements;

import mcib3d.geom2.Object3D;


public class MeasureMoments extends MeasureAbstract {
    public static final String MOMENT_1 = "Moment1";
    public static final String MOMENT_2 = "Moment2";
    public static final String MOMENT_3 = "Moment3";
    public static final String MOMENT_4 = "Moment4";
    public static final String MOMENT_5 = "Moment5";

    public MeasureMoments(Object3D object3D) {
        super(object3D);
    }

    @Override
    public String[] getNames() {
        return new String[]{MOMENT_1, MOMENT_2, MOMENT_3, MOMENT_4, MOMENT_5};
    }

    @Override
    protected void computeAll() {
        Double[] moments = getMoments3D();
        keysValues.put(MOMENT_1, moments[0]);
        keysValues.put(MOMENT_2, moments[1]);
        keysValues.put(MOMENT_3, moments[2]);
        keysValues.put(MOMENT_4, moments[3]);
        keysValues.put(MOMENT_5, moments[4]);
    }

    /**
     * This code is taken from the BIOCAT platform:
     * http://faculty.cs.niu.edu/~zhou/tool/biocat/ Reference for BIOCAT: J.
     * Zhou, S. Lamichhane, G. Sterne, B. Ye, H. C. Peng, "BIOCAT: a Pattern
     * Recognition Platform for Customizable Biological Image Classification and
     * Annotation", BMC Bioinformatics , 2013, 14:291 * This class calculates a
     * set of five 3D image moments that are invariant to size, position and
     * orientation. Reference: F. A. Sadjadi and E. L. Hall, Three-Dimensional
     * Moment Invariants, IEEE Transactions on Pattern Analysis and Machine
     * Intelligence, vol. PAMI-2, no. 2, pp. 127-136, March 1980.
     *
     * @return 3D moments (5)
     */
    public Double[] getMoments3D() {
        MeasureCentroid centroid = new MeasureCentroid(object3D);
        Double[] moments = computation3D.computeMoments2(centroid.getCentroidAsVoxel(), false);

        // normalize
        MeasureVolume volume = new MeasureVolume(object3D);
        double v = volume.getVolumeUnit();
        double v53 = Math.pow(v, 5.0 / 3.0); // keep in integer ?
        double s200 = moments[0] / v53;
        double s020 = moments[1] / v53;
        double s002 = moments[2] / v53;
        double s110 = moments[3] / v53;
        double s101 = moments[4] / v53;
        double s011 = moments[5] / v53;

        double J1 = (s200 + s020 + s002);
        double J2 = (s020 * s002 - s011 * s011 + s200 * s002 - s101 * s101 + s200 * s020 - s110 * s110);
        double J3 = (s200 * s020 * s002 + 2 * s110 * s101 * s011 - s002 * s110 * s110 - s020 * s101 * s101 - s200 * s011 * s011);
        double I1 = (J1 * J1) / J2;
        double I2 = J3 / (J1 * J1 * J1);

        return new Double[]{J1, J2, J3, I1, I2};
    }
}
