package mcib3d.geom;
//import gui.utils.exceptionPrinter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.TreeMap;
import mcib3d.image3d.ImageFloat;
import mcib3d.image3d.ImageHandler;
import mcib3d.image3d.ImageInt;
import mcib3d.utils.exceptionPrinter;

public class Object3DFactory {

    ImageFloat SP;
    ImageInt S;
    double scaleXY, scaleZ, XX, XZ;
    int sizeXY, sizeX, sizeY, sizeZ, limX, limY, limZ;
    private TreeMap<Integer, HashMap<Integer, Voxel3D>> objects;
    
    public Object3DFactory(ImageInt S) {
        if (S == null) {
            return;
        }
        this.S = S;
        this.scaleXY = S.getScaleXY();
        this.scaleZ = S.getScaleZ();
        XZ = scaleXY * scaleZ;
        XX = scaleXY * scaleXY;
        this.sizeXY = S.sizeXY;
        this.sizeX = S.sizeX;
        this.sizeY = S.sizeY;
        this.sizeZ = S.sizeZ;
        this.limX = sizeX - 1;
        this.limY = sizeY - 1;
        this.limZ = sizeZ - 1;
    }
    
    public Object3DFuzzy[] getFuzzyObjects(float contoursProba, ImageFloat probaMap) {
        try {
            this.SP = probaMap;
            objects = new TreeMap<Integer, HashMap<Integer, Voxel3D>>();
            for (int z = 0; z < sizeZ; z++) {
                for (int xy = 0; xy < sizeXY; xy++) {
                    if (S.getPixel(xy, z) != 0) {
                        addVoxel(xy % sizeX, xy / sizeX, z, S.getPixelInt(xy, z), SP.pixels[z][xy]);
                    }
                }
            }
            ArrayList<Object3D> res = new ArrayList<Object3D>(objects.size());
            for (int label : objects.keySet()) {                
                Object3DFuzzy o = new Object3DFuzzy(label, new ArrayList<Voxel3D>(objects.get(label).values()));
                //compute contours...
                HashMap<Integer, Voxel3D> curVoxels = objects.get(label);
                boolean change = true;
                while (change) { //erase contours voxel if proba < limit
                    change = false;
                    for (Voxel3D vox : curVoxels.values()) {
                        if (S.getPixelInt(vox.getRoundX(), vox.getRoundY(), vox.getRoundZ()) == label && isContour(vox, label) && SP.getPixel(vox.getRoundX(), vox.getRoundY(), vox.getRoundZ()) < contoursProba) {
                            S.setPixel(vox.getRoundX(), vox.getRoundY(), vox.getRoundZ(), 0);
                            change = true;
                        }
                    }
                }
                //get contours
                ArrayList<Voxel3D> contours = new ArrayList<Voxel3D>();
                for (Voxel3D vox : curVoxels.values()) {
                    if (S.getPixelInt(vox.getRoundX(), vox.getRoundY(), vox.getRoundZ()) == label && isContour(vox, label)) {
                        contours.add(vox);
                    }
                }
                if (contours.isEmpty()) { //add the vox of max proba if no contours
                    Voxel3D maxvox = null;
                    float max = 0;
                    for (Voxel3D vox : curVoxels.values()) {
                        if (SP.getPixel(vox.getRoundX(), vox.getRoundY(), vox.getRoundZ()) > max) {
                            max = SP.getPixel(vox.getRoundX(), vox.getRoundY(), vox.getRoundZ());
                            maxvox = vox;
                        }
                    }
                    contours.add(maxvox);
                }
                contours.trimToSize();

                //restore voxels values
                for (Voxel3D vox : curVoxels.values()) {
                    S.setPixel(vox.getRoundX(), vox.getRoundY(), vox.getRoundZ(), label);
                }
                o.setContours(contours);
                o.setResXY(scaleXY);
                o.setResZ(scaleZ);
                res.add(o);
            }
            Object3DFuzzy[] resArray = new Object3DFuzzy[res.size()];
            resArray = res.toArray(resArray);
            return resArray;
        } catch (Exception e) {
            exceptionPrinter.print(e, "", true);
        }
        return null;
    }
    
    public Object3DVoxels[] getObjects() {
        objects = new TreeMap<Integer, HashMap<Integer, Voxel3D>>();
        for (int z = 0; z < sizeZ; z++) {
            for (int xy = 0; xy < sizeXY; xy++) {
                if (S.getPixelInt(xy, z) != 0) {
                    addVoxel(xy % sizeX, xy / sizeX, z, S.getPixelInt(xy, z));
                }
            }
        }
        ArrayList<Object3DVoxels> res = new ArrayList<Object3DVoxels>(objects.size());
        for (int label : objects.keySet()) {
            Object3DVoxels o = new Object3DVoxels(new ArrayList<Voxel3D>(objects.get(label).values()));
            o.setValue(label);
            HashMap<Integer, Voxel3D> curVoxels = objects.get(label);
            ArrayList<Voxel3D> contours = new ArrayList<Voxel3D>();
            double surf = 0;
            double s;
            for (Voxel3D vox : curVoxels.values()) {
                s = getContourSurface(vox, label);
                if (s > 0) {
                    surf += s;
                    contours.add(vox);
                }
            }
            o.setContours(contours, surf);
            
            o.setResXY(scaleXY);
            o.setResZ(scaleZ);
            o.setLabelImage(S);
            res.add(o);
        }
        Object3DVoxels[] resArray = new Object3DVoxels[res.size()];
        resArray = res.toArray(resArray);
        /*
         ImageHandler contours = ImageHandler.newBlankImageHandler(S.getTitle()+"::contours", S);
         for (Object3D o : res) {
         for (Voxel3D v : o.getContours()) contours.setPixel(v.getXYCoord(sizeX), v.getRoundZ(), o.getValue());
         }
         contours.show();
        
         * 
         */
        
        return resArray;
    }
    
    private void addVoxel(int x, int y, int z, int label, float p) {
        Voxel3D vox = new Voxel3D(x, y, z, p);        
        if (objects.containsKey(label)) {
            objects.get(label).put(z * sizeXY + y * sizeX + x, vox);
        } else {
            int coord = z * sizeXY + y * sizeX + x;
            HashMap<Integer, Voxel3D> hp = new HashMap<Integer, Voxel3D>();
            hp.put(coord, vox);
            objects.put(label, hp);
        }
    }
    
    private void addVoxel(int x, int y, int z, int label) {
        Voxel3D vox = new Voxel3D(x, y, z, label);        
        if (objects.containsKey(label)) {
            objects.get(label).put(z * sizeXY + y * sizeX + x, vox);
        } else {
            HashMap<Integer, Voxel3D> hp = new HashMap<Integer, Voxel3D>();
            hp.put(z * sizeXY + y * sizeX + x, vox);
            objects.put(label, hp);
        }
    }
    
    private double getContourSurface(Voxel3D vox, int label) {
        double surf = 0;
        int x = vox.getRoundX();
        int y = vox.getRoundY();
        int z = vox.getRoundZ();
        if (x < limX) {            
            if (S.getPixelInt(x + 1, y, z) != label) {
                surf += XZ;
            }
        } else {
            surf += XZ;
        }
        if (x > 0) {            
            if (S.getPixelInt(x - 1, y, z) != label) {
                surf += XZ;
            }
        } else {
            surf += XZ;
        }
        if (y < limY) {            
            if (S.getPixelInt(x, y + 1, z) != label) {
                surf += XZ;
            }
        } else {
            surf += XZ;
        }
        if (y > 0) {            
            if (S.getPixelInt(x, y - 1, z) != label) {
                surf += XZ;
            }
        } else {
            surf += XZ;
        }
        if (z < limZ) {            
            if (S.getPixelInt(x, y, z + 1) != label) {
                surf += XX;
            }
        } else if (limZ > 0) {
            surf += XX; // 2D case: image with sizeZ=1
        }
        if (z > 0) {            
            if (S.getPixelInt(x, y, z - 1) != label) {
                surf += XX;
            }
        } else if (limZ > 0) {
            surf += XX; // 2D case: image with sizeZ=1
        }
        return surf;
    }
    
    private boolean isContour(Voxel3D vox, int label) {
        int x = vox.getRoundX();
        int y = vox.getRoundY();
        int z = vox.getRoundZ();
        if (x < limX) {            
            if (S.getPixelInt(vox.getRoundX() + 1, vox.getRoundY(), vox.getRoundZ()) != label) {
                return true;
            }
        } else {
            return true;
        }
        if (x > 0) {            
            if (S.getPixelInt(vox.getRoundX() - 1, vox.getRoundY(), vox.getRoundZ()) != label) {
                return true;
            }
        } else {
            return true;
        }
        if (y < limY) {            
            if (S.getPixelInt(vox.getRoundX(), vox.getRoundY() + 1, vox.getRoundZ()) != label) {
                return true;
            }
        } else {
            return true;
        }
        if (y > 0) {            
            if (S.getPixelInt(vox.getRoundX(), vox.getRoundY() - 1, vox.getRoundZ()) != label) {
                return true;
            }
        } else {
            return true;
        }
        if (z < limZ) {            
            if (S.getPixelInt(vox.getRoundX(), vox.getRoundY(), vox.getRoundZ() + 1) != label) {
                return true;
            }
        } else if (limZ > 0) {
            return true; // 2D case: image with sizeZ=1
        }
        if (z > 0) {            
            if (S.getPixelInt(vox.getRoundX(), vox.getRoundY(), vox.getRoundZ() - 1) != label) {
                return true;
            }
        } else if (limZ > 0) {
            return true; // 2D case: image with sizeZ=1
        }
        return false;
    }
    
    public class ObjectValueComparator implements Comparator<Object3D> {

        @Override
        public int compare(Object3D t, Object3D t1) {
            if (t.getValue() < t1.getValue()) {
                return -1;
            } else if (t.getValue() > t1.getValue()) {
                return 1;
            } else {
                return 0;
            }
        }
    }
}
