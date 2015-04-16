package mcib3d.geom;
import java.util.ArrayList;

public class IntCoord3D {
    public int x, y, z;
    public IntCoord3D() {
        this.x=0;
        this.y=0;
        this.z=0;
    }
    public IntCoord3D(int x, int y, int z) {
        this.x=x;
        this.y=y;
        this.z=z;
    }
    
//    public boolean increment(int sizeX, int sizeY, int sizeZ) {
//        x++;
//        if (x==sizeX) {
//            x=0;
//            y++;
//            if (y==sizeY) {
//                y=0;
//                z++;
//                if (z==sizeZ) return false;
//            }
//        }
//        return true;
//    }
//    
//    public ArrayList<Coord3D> getVois1(int sizeX, int sizeY, int sizeZ) {
//        ArrayList<Coord3D> res = new ArrayList<Coord3D>(6);
//        if (x>0) res.add(new Coord3D(x-1, y, z));
//        if (x<(sizeX-1)) res.add(new Coord3D(x+1, y, z));
//        if (y>0) res.add(new Coord3D(x, y-1, z));
//        if (y<(sizeY-1)) res.add(new Coord3D(x, y+1, z));
//        if (z>0) res.add(new Coord3D(x, y, z-1));
//        if (z<(sizeZ-1)) res.add(new Coord3D(x, y, z+1));
//        return res;
//    }
}
