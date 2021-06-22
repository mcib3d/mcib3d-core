package mcib3d.geom2;

import mcib3d.geom.Voxel3D;

import java.util.Objects;

public class VoxelInt {
    public int x, y, z;

    public float value;

    public VoxelInt() {
        x = 0;
        y = 0;
        z = 0;
        value = 0;
    }

    public VoxelInt(int x, int y, int z, float value) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.value = value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        VoxelInt voxelInt = (VoxelInt) o;
        return x == voxelInt.x && y == voxelInt.y && z == voxelInt.z;
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, y, z);
    }

    public double distanceSquare(Voxel3D voxel3D) {
        return (x - voxel3D.getX()) * (x - voxel3D.getX()) + (y - voxel3D.getY()) * (y - voxel3D.getY()) + (z - voxel3D.getZ()) * (z - voxel3D.getZ());
    }

    public double distance(Voxel3D voxel3D) {
        return Math.sqrt(distanceSquare(voxel3D));
    }

    public double distanceSquareScaled(Voxel3D voxel3D, double xy, double xz) {
        return (x - voxel3D.getX()) * (x - voxel3D.getX()) * xy * xy + (y - voxel3D.getY()) * (y - voxel3D.getY()) * xy * xy + (z - voxel3D.getZ()) * (z - voxel3D.getZ()) * xz * xz;
    }

    public double distanceSquareScaled(VoxelInt voxel3D, double xy, double xz) {
        return (x - voxel3D.x) * (x - voxel3D.x) * xy * xy + (y - voxel3D.y) * (y - voxel3D.y) * xy * xy + (z - voxel3D.z) * (z - voxel3D.z) * xz * xz;
    }


    public double distanceUnitScaled(Voxel3D voxel3D, double xy, double xz) {
        return Math.sqrt(distanceSquareScaled(voxel3D, xy, xz));
    }

    public double distanceUnitScaled(VoxelInt voxel3D, double xy, double xz) {
        return Math.sqrt(distanceSquareScaled(voxel3D, xy, xz));
    }


    @Override
    public String toString() {
        return "VoxelInt{" +
                "x=" + x +
                ", y=" + y +
                ", z=" + z +
                ", value=" + value +
                '}';
    }
}
