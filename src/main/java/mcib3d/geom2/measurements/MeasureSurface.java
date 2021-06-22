package mcib3d.geom2.measurements;

import mcib3d.geom2.Object3D;
import mcib3d.geom2.VoxelInt;
import mcib3d.image3d.ImageHandler;

import java.util.List;

public class MeasureSurface extends MeasureAbstract {
    public final String SURFACE_PIX = "SurfaceContPix";
    public final String SURFACE_UNIT = "SurfaceContUnit";
    public final String SURFACE_CORRECTED = "SurfaceCorrPix";
    public final String SURFACE_NB_VOXELS = "SurfaceNBbPix";

    public MeasureSurface(Object3D object3D) {
        super(object3D);
    }

    @Override
    public String[] getNames() {
        return new String[]{SURFACE_PIX, SURFACE_UNIT, SURFACE_CORRECTED, SURFACE_NB_VOXELS};
    }

    @Override
    public void computeAll() {
        computeGeometrySurface();
    }

    public double getSurfaceContactPix() {
        if (keysValues.get(SURFACE_PIX) == null) computeGeometrySurface();

        return keysValues.get(SURFACE_PIX);
    }

    public double getSurfaceContactUnit() {
        if (keysValues.get(SURFACE_UNIT) == null) computeGeometrySurface();

        return keysValues.get(SURFACE_UNIT);
    }

    public double getSurfaceCorrectedPix() {
        if (keysValues.get(SURFACE_CORRECTED) == null) computeGeometrySurface();

        return keysValues.get(SURFACE_CORRECTED);
    }

    public double getSurfaceNbVoxelsContours() {
        if (keysValues.get(SURFACE_NB_VOXELS) == null) computeGeometrySurface();

        return keysValues.get(SURFACE_NB_VOXELS);
    }


    private void computeGeometrySurface() {
        // contour
        List<VoxelInt> contour = computation3D.getContour();

        ComputeContours contours = new ComputeContours();
        keysValues.put(SURFACE_PIX, contours.getSurfaceContactVoxels());
        keysValues.put(SURFACE_UNIT, contours.getSurfaceContactUnit());
        keysValues.put(SURFACE_CORRECTED, contours.getSurfaceCorrectedVoxels());
        keysValues.put(SURFACE_NB_VOXELS, contours.getSurfaceNbVoxelsContour());
    }

    class ComputeContours {
        private double surfaceNbVoxelsContour = Double.NaN;
        private double surfaceCorrectedVoxels = Double.NaN;
        private double surfaceContactUnit = Double.NaN;
        private double surfaceContactVoxels = Double.NaN;

        private int class1 = 0, class2 = 0, class3 = 0, class4 = 0, class5 = 0, class6 = 0;

        private ImageHandler segImage;

        private final int sx, sy, sz;

        private final double XZ, XX;

        public ComputeContours() {
            segImage = computation3D.createLabelImage();
            sx = segImage.sizeX;
            sy = segImage.sizeY;
            sz = segImage.sizeZ;

            XZ = object3D.getResXY() * object3D.getResZ();
            XX = object3D.getResXY() * object3D.getResXY();
        }


        public double getSurfaceCorrectedVoxels() {
            if (Double.isNaN(surfaceCorrectedVoxels)) computeContour();
            return surfaceCorrectedVoxels;
        }

        public double getSurfaceContactUnit() {
            if (Double.isNaN(surfaceContactUnit)) computeContour();
            return surfaceContactUnit;
        }

        public double getSurfaceContactVoxels() {
            if (Double.isNaN(surfaceContactVoxels)) computeContour();
            return surfaceContactVoxels;
        }

        public double getSurfaceNbVoxelsContour() {
            return surfaceNbVoxelsContour;
        }

        private void computeContour() {
            surfaceCorrectedVoxels = 0;
            surfaceContactUnit = 0;
            surfaceContactVoxels = 0;
            surfaceNbVoxelsContour = 0;

            computation3D.getContour().forEach(v -> updateVoxel(v.x, v.y, v.z));

            // METHOD LAURENT GOLE FROM Lindblad2005 TO COMPUTE SURFACE
            // Surface area estimation of digitized 3D objects using weighted local configurations
            double w1 = 0.894, w2 = 1.3409, w3 = 1.5879, w4 = 2.0, w5 = 8.0 / 3.0, w6 = 10.0 / 3.0;
            surfaceCorrectedVoxels = (class1 * w1 + class2 * w2 + class3 * w3 + class4 * w4 + class5 * w5 + class6 * w6);
        }


        private void updateVoxel(int i, int j, int k) {
            int pix1, pix2, pix3, pix4, pix5, pix6;
            int face;
            int class3or4;
            boolean cont = false;

            int val = 1;

            face = 0;
            class3or4 = 0;

            pix1 = (i + 1 < sx) ? (int) segImage.getPixel(i + 1, j, k) : 0;
            pix2 = (i - 1 >= 0) ? (int) segImage.getPixel(i - 1, j, k) : 0;
            pix3 = (j + 1 < sy) ? (int) segImage.getPixel(i, j + 1, k) : 0;
            pix4 = (j - 1 >= 0) ? (int) segImage.getPixel(i, j - 1, k) : 0;
            pix5 = (k + 1 < sz) ? (int) segImage.getPixel(i, j, k + 1) : 0;
            pix6 = (k - 1 >= 0) ? (int) segImage.getPixel(i, j, k - 1) : 0;

            if (pix1 != val) {
                cont = true;
                surfaceContactUnit += XZ;
                surfaceContactVoxels++;
                face++;
                if (pix2 != val) {
                    class3or4 = 1;
                }
            }
            if (pix2 != val) {
                cont = true;
                surfaceContactVoxels++;
                surfaceContactUnit += XZ;
                face++;
            }
            if (pix3 != val) {
                cont = true;
                surfaceContactVoxels++;
                surfaceContactUnit += XZ;
                face++;
                if (pix4 != val) {
                    class3or4 = 1;
                }
            }
            if (pix4 != val) {
                cont = true;
                surfaceContactVoxels++;
                surfaceContactUnit += XZ;
                face++;
            }
            if (pix5 != val) {
                cont = true;
                surfaceContactVoxels++;
                surfaceContactUnit += XX;
                face++;
                if (pix6 != val) {
                    class3or4 = 1;
                }
            }
            if (pix6 != val) {
                cont = true;
                surfaceContactVoxels++;
                surfaceContactUnit += XX;
                face++;
            }

            if (cont) {
                surfaceNbVoxelsContour++;
                // METHOD LAURENT GOLE FROM Lindblad2005 TO COMPUTE SURFACE
                // Surface area estimation of digitized 3D objects using weighted local configurations
                if (face == 1) {
                    class1++;
                }
                if (face == 2) {
                    class2++;
                }
                if (face == 3 && class3or4 == 0) {
                    class3++;
                }
                if (face == 3 && class3or4 == 1) {
                    class4++;
                }
                if (face == 4) {
                    class5++;
                }
                if (face == 5) {
                    class6++;
                }
            }
        }
    }
}
