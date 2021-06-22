package mcib3d.geom2;

import mcib3d.image3d.ImageHandler;

public class BoundingBox {
    public int xmin, ymin, zmin, xmax, ymax, zmax;

    public BoundingBox() {
        xmin = Integer.MAX_VALUE;
        xmax = 0;
        ymin = Integer.MAX_VALUE;
        ymax = 0;
        zmin = Integer.MAX_VALUE;
        zmax = 0;
    }

    public BoundingBox(ImageHandler handler) {
        xmin = 0;
        xmax = handler.sizeX;
        ymin = 0;
        ymax = handler.sizeY;
        zmin = 0;
        zmax = handler.sizeZ;
    }

    public void setBounding(int x0, int x1, int y0, int y1, int z0, int z1) {
        xmin = x0;
        xmax = x1;
        ymin = y0;
        ymax = y1;
        zmin = z0;
        zmax = z1;
    }

    public void adjustBounding(int x0, int y0,  int z0) {
        xmin = Math.min(xmin, x0);
        xmax = Math.max(xmax, x0);
        ymin = Math.min(ymin, y0);
        ymax = Math.max(ymax, y0);
        zmin = Math.min(zmin, z0);
        zmax = Math.max(zmax, z0);
    }

    @Override
    public String toString() {
        return "BoundingBox{" +
                "xmin=" + xmin +
                ", ymin=" + ymin +
                ", zmin=" + zmin +
                ", xmax=" + xmax +
                ", ymax=" + ymax +
                ", zmax=" + zmax +
                '}';
    }
}
