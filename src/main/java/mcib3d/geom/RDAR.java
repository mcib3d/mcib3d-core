package mcib3d.geom;

import java.util.ArrayList;

/**
 * Created by thomasb on 19/7/16.
 */
public class RDAR {
    private Object3DVoxels volume;
    private Object3DVoxels ellipsoid;
    private ArrayList<Object3DVoxels> partsIn = null;
    private ArrayList<Object3DVoxels> partsOut = null;

    public RDAR(Object3DVoxels volume) {
        this.volume = volume;
        ellipsoid = null;
    }

    private void compute() {
        ellipsoid = this.getEllipsoid();

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

    public int getPartsOutVolumePixels() {
        return getPartsOutVolumePixels(0);
    }

    public int getPartsOutVolumePixels(int minVolume) {
        if (getPartsOut(minVolume) == null) return 0;
        int volume = 0;
        for (Object3DVoxels object3DVoxels : getPartsOut(minVolume)) {
            volume += object3DVoxels.getVolumePixels();
        }

        return volume;
    }


    public double getPartsInVolumeUnit() {
        return getPartsInVolumeUnit(0);
    }

    public double getPartsInVolumeUnit(int minVolume) {
        if (getPartsIn(minVolume) == null) return 0;
        double volume = 0;
        for (Object3DVoxels object3DVoxels : getPartsIn(minVolume)) {
            volume += object3DVoxels.getVolumeUnit();
        }

        return volume;
    }

    public double getPartsOutVolumeUnit() {
        return getPartsOutVolumeUnit(0);
    }

    public double getPartsOutVolumeUnit(int minVolume) {
        if (getPartsOut(minVolume) == null) return 0;
        double volume = 0;
        for (Object3DVoxels object3DVoxels : getPartsOut(minVolume)) {
            volume += object3DVoxels.getVolumeUnit();
        }

        return volume;
    }


    public int getPartsInVolumePixels() {
        return getPartsInVolumePixels(0);
    }

    public int getPartsInVolumePixels(int minVolume) {
        if (getPartsIn(minVolume) == null) return 0;
        int volume = 0;
        for (Object3DVoxels object3DVoxels : getPartsIn(minVolume)) {
            volume += object3DVoxels.getVolumePixels();
        }

        return volume;
    }


    public ArrayList<Object3DVoxels> getPartsIn(int minVolume) {
        if (ellipsoid == null) compute();
        if (partsIn == null) return null;
        ArrayList<Object3DVoxels> result = new ArrayList<Object3DVoxels>();
        for (Object3DVoxels part : partsIn) if (part.getVolumePixels() > minVolume) result.add(part);
        return result;
    }

    public ArrayList<Object3DVoxels> getPartsOut(int minVolume) {
        if (ellipsoid == null) compute();
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
        ellipsoid = null;
    }
}
