package mcib3d.image3d;
import mcib3d.image3d.comparator.ComparatorInvByte;
import mcib3d.image3d.comparator.ComparatorInvFloat;
import mcib3d.image3d.comparator.ComparatorInvShort;
import mcib3d.image3d.comparator.ComparatorByte;
import mcib3d.image3d.comparator.ComparatorFloat;
import mcib3d.image3d.comparator.ComparatorShort;
import java.util.*;

public class MaskIterator{
    ImageHandler mask;
    public Integer[] coords;
    private Coordinate3D c3D;
    public int limit;
    
    public MaskIterator(ImageHandler mask) {
        if (mask instanceof ImageByte) {
            ImageByte im = (ImageByte) mask;
            int count=0;
            for (int z =0; z<mask.sizeZ; z++) {
                for (int xy = 0; xy<mask.sizeXY; xy++) {
                    if (im.pixels[z][xy]!=0) count++;
                }
            }
            coords=new Integer[count];
            count=0;
            for (int z =0; z<mask.sizeZ; z++) {
                for (int xy = 0; xy<mask.sizeXY; xy++) {
                    if (im.pixels[z][xy]!=0) {
                        coords[count]=z*mask.sizeXY+xy;
                        count++;
                    }
                }
            }
        } else if (mask instanceof ImageShort) {
            ImageShort im = (ImageShort) mask;
            int count=0;
            for (int z =0; z<mask.sizeZ; z++) {
                for (int xy = 0; xy<mask.sizeXY; xy++) {
                    if (im.pixels[z][xy]!=0) count++;
                }
            }
            coords=new Integer[count];
            count=0;
            for (int z =0; z<mask.sizeZ; z++) {
                for (int xy = 0; xy<mask.sizeXY; xy++) {
                    if (im.pixels[z][xy]!=0) {
                        coords[count]=z*mask.sizeXY+xy;
                        count++;
                    }
                }
            }
        } else if (mask instanceof ImageFloat) {
            ImageFloat im = (ImageFloat) mask;
            int count=0;
            for (int z =0; z<mask.sizeZ; z++) {
                for (int xy = 0; xy<mask.sizeXY; xy++) {
                    if (im.pixels[z][xy]!=0) count++;
                }
            }
            coords=new Integer[count];
            count=0;
            for (int z =0; z<mask.sizeZ; z++) {
                for (int xy = 0; xy<mask.sizeXY; xy++) {
                    if (im.pixels[z][xy]!=0) {
                        coords[count]=z*mask.sizeXY+xy;
                        count++;
                    }
                }
            }
        }
        c3D=new Coordinate3D(0, mask.sizeX, mask.sizeY, mask.sizeZ);
    }

    public MaskIterator(ImageHandler mask, float thld) {
        if (mask instanceof ImageByte) {
            ImageByte im = (ImageByte) mask;
            int count=0;
            for (int z =0; z<mask.sizeZ; z++) {
                for (int xy = 0; xy<mask.sizeXY; xy++) {
                    if ((im.pixels[z][xy]&0xff)>=thld) count++;
                }
            }
            coords=new Integer[count];
            count=0;
            for (int z =0; z<mask.sizeZ; z++) {
                for (int xy = 0; xy<mask.sizeXY; xy++) {
                    if ((im.pixels[z][xy]&0xff)>=thld) {
                        coords[count]=z*mask.sizeXY+xy;
                        count++;
                    }
                }
            }
        } else if (mask instanceof ImageShort) {
            ImageShort im = (ImageShort) mask;
            int count=0;
            for (int z =0; z<mask.sizeZ; z++) {
                for (int xy = 0; xy<mask.sizeXY; xy++) {
                    if ((im.pixels[z][xy]&0xffff)>=thld) count++;
                }
            }
            coords=new Integer[count];
            count=0;
            for (int z =0; z<mask.sizeZ; z++) {
                for (int xy = 0; xy<mask.sizeXY; xy++) {
                    if ((im.pixels[z][xy]&0xffff)>=thld) {
                        coords[count]=z*mask.sizeXY+xy;
                        count++;
                    }
                }
            }
        } else if (mask instanceof ImageFloat) {
            ImageFloat im = (ImageFloat) mask;
            int count=0;
            for (int z =0; z<mask.sizeZ; z++) {
                for (int xy = 0; xy<mask.sizeXY; xy++) {
                    if ((im.pixels[z][xy])>=thld) count++;
                }
            }
            coords=new Integer[count];
            count=0;
            for (int z =0; z<mask.sizeZ; z++) {
                for (int xy = 0; xy<mask.sizeXY; xy++) {
                    if ((im.pixels[z][xy])>=thld) {
                        coords[count]=z*mask.sizeXY+xy;
                        count++;
                    }
                }
            }
        }
        c3D=new Coordinate3D(0, mask.sizeX, mask.sizeY, mask.sizeZ);
    }

    public MaskIterator(int sizeX, int sizeY, int sizeZ) {
        coords=new Integer[sizeX*sizeY*sizeZ];
        for (int i=0; i<coords.length; i++) coords[i]=i;
        c3D=new Coordinate3D(0, sizeX, sizeY, sizeZ);
    }

    public void sort(boolean inv, ImageHandler image) {
        if (image instanceof ImageByte) {
            if (inv) Arrays.sort(coords, new ComparatorInvByte(((ImageByte)image)));
            else Arrays.sort(coords, new ComparatorByte(((ImageByte)image)));
        }
        else if (image instanceof ImageShort) {
            if (inv) Arrays.sort(coords, new ComparatorInvShort(((ImageShort)image)));
            else Arrays.sort(coords, new ComparatorShort(((ImageShort)image)));
        } else if (image instanceof ImageFloat) {
            if (inv) Arrays.sort(coords, new ComparatorInvFloat(((ImageFloat)image)));
            else Arrays.sort(coords, new ComparatorFloat(((ImageFloat)image)));
        }
    }

    public boolean isIn(int xx, int yy, int zz){
        if (xx>=0 && xx<mask.sizeX && yy>=0 && yy<mask.sizeY && zz>=0 && zz<mask.sizeZ) {
            return mask.getPixel(xx, yy, zz)!=0;
        } else return false;
    }

    public ArrayList<Integer> getVois1(int coord) {
        c3D.setCoord(coord);
        if (mask!=null) return c3D.getVois1(mask);
        else return c3D.getVois1();
    }
    public ArrayList<Integer> getVois15(int coord) {
        c3D.setCoord(coord);
        if (mask!=null) return c3D.getVois15(mask);
        else return c3D.getVois15();
    }
    public void setVois(float radius, float radiusZ) {
        c3D.setVois(radius, radiusZ);
    }
    public ArrayList<Integer> getVois(int coord) {
        c3D.setCoord(coord);
        if (mask!=null) {
            return c3D.getVois(mask);
        }
        else return c3D.getVois();
    }
}
