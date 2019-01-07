package mcib3d.geom;

public class Point3DInt {
    public int x, y, z;

    public Point3DInt(int x, int y, int z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getZ() {
        return z;
    }

    public void translate(int tx, int ty, int tz) {
        x += tx;
        y += ty;
        z += tz;
    }

    public boolean isInsideBoundingBox(int[] boundingBox) { //xmin, xmax, ymin, ymax, zmin, zmax
        return (x >= boundingBox[0] && x <= boundingBox[1] && y >= boundingBox[2] && y <= boundingBox[3] && z >= boundingBox[4] && z <= boundingBox[5]);
    }

    public boolean sameVoxel(Point3DInt other) {
        return (this.x == other.x) && (this.y == other.y) && (this.z == other.z);
    }
}
