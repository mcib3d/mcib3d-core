package mcib3d.image3d;

import java.util.ArrayList;
import java.util.Comparator;
import mcib3d.geom.IntCoord3D;
import mcib3d.geom.Voxel3D;

public class Coordinate3D {

    public int sizeX, sizeY, sizeZ, sizeXY, sizeXYZ;
    public int coord, x, y, z;
    public int[][] vois; //res[0] = array of x's
    public float[] distances;

    public Coordinate3D(int coord, int sizeX, int sizeY, int sizeZ) {
        this.coord = coord;
        this.sizeX = sizeX;
        this.sizeY = sizeY;
        this.sizeXY = sizeX * sizeY;
        this.sizeZ = sizeZ;
        this.sizeXYZ = sizeXY * sizeZ;
        this.computeXYZ();
    }

    public void setCoord(int c) {
        this.coord = c;
        this.computeXYZ();
    }

    public void setCoord(int c, int x, int y, int z) {
        this.coord = c;
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public void setCoord(int x, int y, int z) {
        this.coord = x + z * sizeXY + y * sizeX;
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public boolean hasNext() {
        coord++;
        x++;
        if (x == sizeX) {
            x = 0;
            y++;
            if (y == sizeY) {
                y = 0;
                z++;
                if (z == sizeZ) {
                    return false;
                }
            }
        }
        return true;
    }

    public boolean isIn() {
        return x >= 0 && x < sizeX && y >= 0 && y < sizeY && z >= 0 && z < sizeZ;
    }

    public boolean isIn(int xx, int yy, int zz) {
        return xx >= 0 && xx < sizeX && yy >= 0 && yy < sizeY && zz >= 0 && zz < sizeZ;
    }

    public String print() {
        return " x:" + x + " y:" + y + " z:" + z;
    }

    public ArrayList<Integer> getVois1() {
        ArrayList<Integer> res = new ArrayList<Integer>(6);
        if (x > 0) {
            res.add(coord - 1);
        }
        if (x < (sizeX - 1)) {
            res.add(coord + 1);
        }
        if (y > 0) {
            res.add(coord - sizeX);
        }
        if (y < (sizeY - 1)) {
            res.add(coord + sizeX);
        }
        if (z > 0) {
            res.add(coord - sizeXY);
        }
        if (z < (sizeZ - 1)) {
            res.add(coord + sizeXY);
        }
        return res;
    }

    public ArrayList<Voxel3D> getVois1Vox() {
        ArrayList<Voxel3D> res = new ArrayList<Voxel3D>(6);
        if (x > 0) {
            res.add(new Voxel3D(x - 1, y, z, 0));
        }
        if (x < (sizeX - 1)) {
            res.add(new Voxel3D(x + 1, y, z, 0));
        }
        if (y > 0) {
            res.add(new Voxel3D(x, y - 1, z, 0));
        }
        if (y < (sizeY - 1)) {
            res.add(new Voxel3D(x, y + 1, z, 0));
        }
        if (z > 0) {
            res.add(new Voxel3D(x, y, z - 1, 0));
        }
        if (z < (sizeZ - 1)) {
            res.add(new Voxel3D(x, y, z + 1, 0));
        }
        return res;
    }

    public ArrayList<IntCoord3D> getVois1C3D() {
        ArrayList<IntCoord3D> res = new ArrayList<IntCoord3D>(6);
        if (x > 0) {
            res.add(new IntCoord3D(x - 1, y, z));
        }
        if (x < (sizeX - 1)) {
            res.add(new IntCoord3D(x + 1, y, z));
        }
        if (y > 0) {
            res.add(new IntCoord3D(x, y - 1, z));
        }
        if (y < (sizeY - 1)) {
            res.add(new IntCoord3D(x, y + 1, z));
        }
        if (z > 0) {
            res.add(new IntCoord3D(x, y, z - 1));
        }
        if (z < (sizeZ - 1)) {
            res.add(new IntCoord3D(x, y, z + 1));
        }
        return res;
    }

    public ArrayList<Integer> getVois1(ImageHandler mask) {
        if (mask != null) {
            ArrayList<Integer> res = new ArrayList<Integer>(6);
            if (x > 0 && mask.getPixel(x - 1 + y * sizeX, z) != 0) {
                res.add(coord - 1);
            }
            if (x < (sizeX - 1) && mask.getPixel(x + 1 + y * sizeX, z) != 0) {
                res.add(coord + 1);
            }
            if (y > 0 && mask.getPixel(x + (y - 1) * sizeX, z) != 0) {
                res.add(coord - sizeX);
            }
            if (y < (sizeY - 1) && mask.getPixel(x + (y + 1) * sizeX, z) != 0) {
                res.add(coord + sizeX);
            }
            if (z > 0 && mask.getPixel(x + y * sizeX, z - 1) != 0) {
                res.add(coord - sizeXY);
            }
            if (z < (sizeZ - 1) && mask.getPixel(x + y * sizeX, z + 1) != 0) {
                res.add(coord + sizeXY);
            }
            return res;
        } else {
            return getVois1();
        }
    }

    public ArrayList<Integer> getVois15(ImageHandler mask) {
        if (mask != null) {
            ArrayList<Integer> res = new ArrayList<Integer>();
            for (int zz = -1; zz <= 1; zz++) {
                if ((zz + this.z < sizeZ) && (this.z >= -zz)) {
                    for (int yy = -1; yy <= 1; yy++) {
                        if ((yy + this.y < sizeY) && (this.y >= -yy)) {
                            for (int xx = -1; xx <= 1; xx++) {
                                if ((xx + this.x < sizeX) && (this.x >= -xx)) {
                                    if (!((xx == 0) && (yy == 0) && (zz == 0))) {	//exclusion du point
                                        if (mask.getPixel(xx + x + (yy + y) * sizeX, z + zz) != 0) {
                                            res.add(this.coord + xx + yy * sizeX + zz * sizeXY);
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
            return res;
        } else {
            return getVois15();
        }
    }

    public ArrayList<Integer> getVois15() {
        ArrayList<Integer> res = new ArrayList<Integer>();
        for (int zz = -1; zz <= 1; zz++) {
            if ((zz + this.z < sizeZ) && (this.z >= -zz)) {
                for (int yy = -1; yy <= 1; yy++) {
                    if ((yy + this.y < sizeY) && (this.y >= -yy)) {
                        for (int xx = -1; xx <= 1; xx++) {
                            if ((xx + this.x < sizeX) && (this.x >= -xx)) {
                                if (!((xx == 0) && (yy == 0) && (zz == 0))) {	//exclusion du point
                                    res.add(this.coord + xx + yy * sizeX + zz * sizeXY);
                                }
                            }
                        }
                    }
                }
            }
        }
        return res;
    }

    public void setVois(float radius, float radiusZ) {
        float r = (float) radius / radiusZ;
        int rad = (int) (radius + 0.5f);
        int radZ = (int) (radiusZ + 0.5f);
        int[][] temp = new int[3][(2 * rad + 1) * (2 * rad + 1) * (2 * radZ + 1)];
        float[] tempDist = new float[temp[0].length];
        int count = 0;
        float rad2 = radius * radius;
        for (int zz = -radZ; zz <= radZ; zz++) {
            for (int yy = -rad; yy <= rad; yy++) {
                for (int xx = -rad; xx <= rad; xx++) {
                    float d2 = zz * r * zz * r + yy * yy + xx * xx;
                    if (d2 <= rad2 && !((xx == 0) && (yy == 0) && (zz == 0))) {	//exclusion du point
                        temp[0][count] = xx;
                        temp[1][count] = yy;
                        temp[2][count] = zz;
                        tempDist[count] = (float) Math.sqrt(d2);
                        count++;
                    }
                }
            }
        }

        distances = new float[count];
        System.arraycopy(tempDist, 0, distances, 0, count);

        /*
         * Integer[] order = new Integer[distances.length]; for (int i = 0; i <
         * order.length; i++) order[i]=i; Arrays.sort(order, new
         * ComparatorDistances()); Arrays.sort(distances); for (int i = 0;
         * i<count; i++) { vois[0][i]=temp[0][order[i]];
         * vois[1][i]=temp[1][order[i]]; vois[2][i]=temp[2][order[i]]; }
         *
         */
        vois = new int[3][count];
        System.arraycopy(temp[0], 0, vois[0], 0, count);
        System.arraycopy(temp[1], 0, vois[1], 0, count);
        System.arraycopy(temp[2], 0, vois[2], 0, count);





        /*
         * for (int i = 0; i<distances.length; i++) { ij.IJ.log("x:"+vois[0][i]+
         * " y:"+vois[1][i]+" z:"+vois[2][i]+" distance:"+distances[i]); }
         *
         */


    }

    public void setVois1(float scaleZ) {
        vois = new int[3][6];
        distances = new float[6];
        vois[0][0] = 1;
        vois[1][0] = 0;
        vois[2][0] = 0;
        distances[0] = 1;
        vois[0][1] = 0;
        vois[1][1] = 1;
        vois[2][1] = 0;
        distances[1] = 1;
        vois[0][2] = -1;
        vois[1][2] = 0;
        vois[2][2] = 0;
        distances[2] = 1;
        vois[0][3] = 0;
        vois[1][3] = -1;
        vois[2][3] = 0;
        distances[3] = 1;
        vois[0][4] = 0;
        vois[1][4] = 0;
        vois[2][4] = 1;
        distances[4] = scaleZ;
        vois[0][5] = 0;
        vois[1][5] = 0;
        vois[2][5] = -1;
        distances[5] = scaleZ;
    }

    public ArrayList<Integer> getVois(ImageHandler mask) {
        if (mask != null) {
            ArrayList<Integer> res = new ArrayList<Integer>();
            for (int i = 0; i < vois[0].length; i++) {
                int xx = vois[0][i] + x;
                int yy = vois[1][i] + y;
                int zz = vois[2][i] + z;
                if (isIn(xx, yy, zz)) {
                    if (mask.getPixel(xx + yy * sizeX, zz) != 0) {
                        res.add(xx + yy * sizeX + zz * sizeXY);
                    }
                }
            }
            return res;
        } else {
            return getVois();
        }
    }

    public ArrayList<Integer> getVois() {
        ArrayList<Integer> res = new ArrayList<Integer>();
        for (int i = 0; i < vois[0].length; i++) {
            int xx = vois[0][i] + x;
            int yy = vois[1][i] + y;
            int zz = vois[2][i] + z;
            if (isIn(xx, yy, zz)) {
                res.add(xx + yy * sizeX + zz * sizeXY);
            }
        }
        return res;
    }

    private void computeXYZ() {
        this.z = coord / sizeXY;
        int xy = coord % sizeXY;
        this.y = xy / sizeX;
        this.x = xy % sizeX;
    }

    private class ComparatorDistances implements Comparator<Integer> {

        public int compare(Integer i1, Integer i2) {
            if (distances[i1] > distances[i2]) {
                return 1;
            } else if (distances[i1] < distances[i2]) {
                return -1;
            } else {
                return 0;
            }
        }
    }
}
