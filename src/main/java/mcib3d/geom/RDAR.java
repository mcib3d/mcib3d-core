package mcib3d.geom;

import java.util.ArrayList;

/**
 * Created by thomasb on 19/7/16.
 */
public class RDAR {
    private Object3DVoxels volume;
    private Object3DVoxels ellipsoid;
    private int radX, radY, radZ;
    private ArrayList<Object3DVoxels> partsIn = null;
    private ArrayList<Object3DVoxels> partsOut = null;

    public RDAR(Object3DVoxels volume, int radX, int radY, int radZ) {
        this.volume = volume;
        this.radX = radX;
        this.radY = radY;
        this.radZ = radZ;
    }

    private void compute() {
        // compute ellipsoid
        ObjectCreator3D objectCreator3D = new ObjectCreator3D(2 * radX + 2, 2 * radY + 2, 2 * radZ + 2);
        objectCreator3D.createEllipsoid(radX, radY, radZ, radX, radY, radZ, 1, false);
        ellipsoid = new Object3DVoxels(objectCreator3D.getImageHandler(), 1);
        ellipsoid.translate(volume.getCenterX() - radX, volume.getCenterY() - radY, volume.getCenterZ() - radZ);

        // difference volume - ellipsoid
        Object3DVoxels object3DVoxels = new Object3DVoxels(volume);
        object3DVoxels.substractObject(ellipsoid);
        if (object3DVoxels.getVolumePixels() > 0) {
            partsOut = object3DVoxels.getConnexComponents();
        }

        // difference ellipsoid - volume
        object3DVoxels = new Object3DVoxels(ellipsoid);
        object3DVoxels.substractObject(volume);
        if (object3DVoxels.getVolumePixels() > 0) {
            partsIn = object3DVoxels.getConnexComponents();
        }
    }

    public ArrayList<Object3DVoxels> getPartsIn() {
        return getPartsIn(0);
    }

    public ArrayList<Object3DVoxels> getPartsOut() {
        return getPartsOut(0);
    }

    public int getPartsInNumber() {
        if (getPartsIn() == null) return 0;
        else return getPartsIn().size();
    }

    public int getPartsOutNumber() {
        if (getPartsOut() == null) return 0;
        else return getPartsOut().size();
    }


    public ArrayList<Object3DVoxels> getPartsIn(int minVolume) {
        if (partsIn == null) compute();
        if (partsIn == null) return null;
        ArrayList<Object3DVoxels> result = new ArrayList<Object3DVoxels>();
        for (Object3DVoxels part : partsIn) if (part.getVolumePixels() > minVolume) result.add(part);
        return result;
    }

    public ArrayList<Object3DVoxels> getPartsOut(int minVolume) {
        if (partsOut == null) compute();
        if (partsOut == null) return null;
        ArrayList<Object3DVoxels> result = new ArrayList<Object3DVoxels>();
        for (Object3DVoxels part : partsOut) if (part.getVolumePixels() > minVolume) result.add(part);
        return result;
    }

    public int getPartsInNumber(int minVolume) {
        if (getPartsIn(minVolume) == null) return 0;
        else return getPartsIn(minVolume).size();
    }

    public int getPartsOutNumber(int minVolume) {
        if (getPartsOut(minVolume) == null) return 0;
        else return getPartsOut(minVolume).size();
    }

    public Object3DVoxels getEllipsoid() {
        return ellipsoid;
    }

    public void setVolume(Object3DVoxels volume) {
        this.volume = volume;
        partsIn = null;
        partsOut = null;
    }

    public void setRadX(int radX) {
        this.radX = radX;
        partsIn = null;
        partsOut = null;
    }

    public void setRadY(int radY) {
        this.radY = radY;
        partsIn = null;
        partsOut = null;
    }

    public void setRadZ(int radZ) {
        this.radZ = radZ;
        partsIn = null;
        partsOut = null;
    }
}
