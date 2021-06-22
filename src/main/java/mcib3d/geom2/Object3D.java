package mcib3d.geom2;

import mcib3d.image3d.ImageHandler;
import mcib3d.utils.Logger.AbstractLog;
import mcib3d.utils.Logger.IJLog;

import java.util.LinkedList;
import java.util.List;

public class Object3D {
    private final LinkedList<Object3DPlane> object3DPlanes;
    private BoundingBox boundingBox = null;
    private final float value;
    private double resXY = 1.0;
    private double resZ = 1.0;
    private  double size;

    private final AbstractLog log = new IJLog();

    public Object3D(ImageHandler handler) {
        object3DPlanes = new LinkedList<>();
        // assuming only one object in image >0
        value = handler.getMinAboveValue(0);
        resXY = handler.getScaleXY();
        resZ = handler.getScaleZ();

        size = 0;
        // TODO multithreading
        for (int z = 0; z < handler.sizeZ; z++) {
            Object3DPlane plane = createVoxelsListPlane(handler, null, z);
            if (plane != null) {
                object3DPlanes.add(plane);
                size += plane.size();
            }
        }
        log.log("Total "+object3DPlanes.size()+" planes");
    }

    private void computeBoundingBox(){
        boundingBox = new BoundingBox();
        getObject3DPlanes().stream().
                forEach(plane -> plane.adjustBounding(boundingBox));
    }

    public double size(){
        return size;
    }

    private Object3DPlane createVoxelsListPlane(ImageHandler ima, ImageHandler raw, int z) {
        LinkedList<VoxelInt> voxelsTmp = new LinkedList<>();
        for (int j = 0; j < ima.sizeY; j++) {
            for (int i = 0; i < ima.sizeX; i++) {
                if (ima.getPixel(i, j, z) == value) {
                    // add to voxel list
                    voxelsTmp.add(new VoxelInt(i, j, z, (raw == null) ? value : raw.getPixel(i, j, z)));
                }
            }
        }

        return (voxelsTmp.isEmpty()) ? null : new Object3DPlane(voxelsTmp, z);
    }

    public void drawObject(ImageHandler handler, float val) {
        object3DPlanes.parallelStream()
                .forEach(object3DPlane -> object3DPlane.drawObject(handler, val));
    }

    public List<Object3DPlane> getObject3DPlanes(){
        return object3DPlanes;
    }

    public double getResXY() {
        return resXY;
    }

    public double getResZ() {
        return resZ;
    }

    public float getValue() {
        return value;
    }

    public BoundingBox getBoundingBox() {
        if(boundingBox == null) computeBoundingBox();

        return boundingBox;
    }
}
