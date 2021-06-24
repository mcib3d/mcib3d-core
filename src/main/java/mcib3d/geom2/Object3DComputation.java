package mcib3d.geom2;

import mcib3d.geom.Voxel3D;
import mcib3d.image3d.ImageByte;
import mcib3d.image3d.ImageHandler;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.DoubleAdder;
import java.util.stream.Collectors;


public class Object3DComputation {
    private Object3DInt object3DInt;

    public Object3DComputation(Object3DInt object3DInt) {
        this.object3DInt = object3DInt;
    }

    public Object3DInt getObject3D() {
        return object3DInt;
    }

    public void setObject3D(Object3DInt object3DInt) {
        this.object3DInt = object3DInt;
    }

    public Voxel3D getSumCoordinates() {
        return object3DInt.getObject3DPlanes().parallelStream()
                .map(Object3DPlane::getSumCoordinates)
                .reduce(new Voxel3D(), (s, v) -> new Voxel3D(s.x + v.x, s.y + v.y, s.z + v.z, s.value));
    }

    public Double[] computeMoments2(Voxel3D centroid, boolean normalize) {
        final DoubleAdder s200 = new DoubleAdder();
        final DoubleAdder s110 = new DoubleAdder();
        final DoubleAdder s020 = new DoubleAdder();
        final DoubleAdder s011 = new DoubleAdder();
        final DoubleAdder s101 = new DoubleAdder();
        final DoubleAdder s002 = new DoubleAdder();

        object3DInt.getObject3DPlanes().parallelStream()
                .forEach(plane -> {
                    Double[] sums = plane.computeMoments2(centroid, normalize);
                    s200.add(sums[0]);
                    s020.add(sums[1]);
                    s002.add(sums[2]);
                    s110.add(sums[3]);
                    s101.add(sums[4]);
                    s011.add(sums[5]);
                });

        // resolution
        double resXY = object3DInt.getResXY();
        double resZ = object3DInt.getResZ();
        double sum200 = s200.sum() * resXY * resXY;
        double sum020 = s020.sum() * resXY * resXY;
        double sum002 = s020.sum() * resZ * resZ;
        double sum110 = s110.sum() * resXY * resXY;
        double sum101 = s101.sum() * resXY * resZ;
        double sum011 = s011.sum() * resXY * resZ;

        // normalize by volume
        if (normalize) {
            double volume = object3DInt.size();
            sum200 /= volume;
            sum020 /= volume;
            sum002 /= volume;
            sum110 /= volume;
            sum101 /= volume;
            sum011 /= volume;
        }

        return new Double[]{sum200, sum020, sum002, sum110, sum101, sum011};
    }

    // label image limited to the max box of the object
    // (not a mini label image only within the bounding box)
    public ImageHandler createLabelImage() {
        // bounding box
        BoundingBox box = object3DInt.getBoundingBox();
        int xma = box.xmax;
        int yma = box.ymax;
        int zma = box.zmax;

        ImageHandler segImage = new ImageByte("Object", xma + 1, yma + 1, zma + 1);

        object3DInt.drawObject(segImage, 1);

        return segImage;
    }

    public List<VoxelInt> getContour() {
        final ImageHandler labelImage = createLabelImage();
        final List<VoxelInt> voxelInts = new LinkedList<>();
        object3DInt.getObject3DPlanes().forEach(p -> voxelInts.addAll(computeContourPlane(p, labelImage)));

        return voxelInts;
    }

    public List<VoxelInt> getContourFromImage(ImageHandler image) {
        final List<VoxelInt> voxelInts = new LinkedList<>();
        object3DInt.getObject3DPlanes().forEach(p -> voxelInts.addAll(computeContourPlane(p, image)));

        return voxelInts;
    }

    private List<VoxelInt> computeContourPlane(Object3DPlane plane, ImageHandler labelImage) {
        return plane.getVoxels().stream().filter(v -> isContour(v, labelImage)).collect(Collectors.toList());
    }

    private boolean isContour(VoxelInt v, ImageHandler labelImage) {
        int pix1, pix2, pix3, pix4, pix5, pix6;
        int sx = labelImage.sizeX, sy = labelImage.sizeY, sz = labelImage.sizeZ;

        int i = v.x, j = v.y, k = v.z;
        pix1 = (i + 1 < sx) ? (int) labelImage.getPixel(i + 1, j, k) : 0;
        if (pix1 == 0) return true;
        pix2 = (i - 1 >= 0) ? (int) labelImage.getPixel(i - 1, j, k) : 0;
        if (pix2 == 0) return true;
        pix3 = (j + 1 < sy) ? (int) labelImage.getPixel(i, j + 1, k) : 0;
        if (pix3 == 0) return true;
        pix4 = (j - 1 >= 0) ? (int) labelImage.getPixel(i, j - 1, k) : 0;
        if (pix4 == 0) return true;
        pix5 = (k + 1 < sz) ? (int) labelImage.getPixel(i, j, k + 1) : 0;
        if (pix5 == 0) return true;
        pix6 = (k - 1 >= 0) ? (int) labelImage.getPixel(i, j, k - 1) : 0;
        if (pix6 == 0) return true;

        return false;
    }


}
