package mcib3d.geom2;

import mcib3d.geom.Voxel3D;
import mcib3d.image3d.ImageHandler;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.DoubleAdder;

public class Object3DPlane {
    private LinkedList<VoxelInt> voxels;
    int zPlane;

    public Object3DPlane(int z) {
        zPlane = z;
        voxels = new LinkedList<>();
    }

    public Object3DPlane(List<VoxelInt> voxel3DS, int z) {
        zPlane = z;
        voxels = new LinkedList<>();
        addVoxels(voxel3DS);
    }

    public void addVoxels(List<VoxelInt> voxel3DS) {
        voxels.addAll(voxel3DS);
    }

    public boolean isEmpty() {
        return voxels.isEmpty();
    }

    public void drawObject(ImageHandler handler, float val) {
        voxels.stream().forEach(voxel -> handler.setPixel(voxel.x, voxel.y, voxel.z, val));
    }

    public int size() {
        return voxels.size();
    }

    public Voxel3D getSumCoordinates() {
        return voxels.stream()
                .map(v -> new Voxel3D(v.x, v.y, v.z, v.value))
                .reduce(new Voxel3D(), (s, v) -> new Voxel3D(s.x + v.x, s.y + v.y, s.z + v.z, s.value));
    }

    public Double[] computeMoments2(Voxel3D centroid, boolean normalize) {
        final DoubleAdder s200 = new DoubleAdder();
        final DoubleAdder s110 = new DoubleAdder();
        final DoubleAdder s020 = new DoubleAdder();
        final DoubleAdder s011 = new DoubleAdder();
        final DoubleAdder s101 = new DoubleAdder();
        final DoubleAdder s002 = new DoubleAdder();

        final double bx = centroid.getX(), by = centroid.getY(), bz = centroid.getZ();

        voxels.stream()
                .forEach(
                        V -> {
                            double i = V.x, j = V.y, k = V.z;
                            s200.add((i - bx) * (i - bx));
                            s020.add((j - by) * (j - by));
                            s002.add((k - bz) * (k - bz));
                            s110.add((i - bx) * (j - by));
                            s101.add((i - bx) * (k - bz));
                            s011.add((j - by) * (k - bz));
                        }
                );

        return new Double[]{s200.sum(), s020.sum(), s002.sum(), s110.sum(), s101.sum(), s011.sum()};
    }

    public void adjustBounding(BoundingBox box) {
        voxels.stream()
                .forEach(v -> box.adjustBounding(v.x, v.y, v.z));
    }

    public List<VoxelInt> getVoxels() {
        return voxels;
    }
}
