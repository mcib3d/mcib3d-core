package mcib3d.geom.interactions;

import mcib3d.geom.Object3D;
import mcib3d.geom.Objects3DPopulation;
import mcib3d.geom.Voxel3D;
import mcib3d.image3d.ImageHandler;
import mcib3d.utils.ArrayUtil;

import java.util.LinkedList;

public class InteractionsComputeDilate implements InteractionsCompute {
    float rx, ry, rz;

    public InteractionsComputeDilate(float rx, float ry, float rz) {
        this.rx = rx;
        this.ry = ry;
        this.rz = rz;
    }

    @Override
    public InteractionsList compute(ImageHandler image) {
        // get population from image
        Objects3DPopulation population = new Objects3DPopulation(image);
        InteractionsList interactions = new InteractionsList();
        for (Object3D object3D : population.getObjectsList()) {
            int value = object3D.getValue();
            Object3D dilated = object3D.getDilatedObject(rx, ry, rz);
            LinkedList<Voxel3D> contours = dilated.getContours();
            ArrayUtil arrayUtil = new ArrayUtil(contours.size());
            int c = 0;
            for (Voxel3D voxel3D : contours) {
                if (image.contains(voxel3D)) {
                    arrayUtil.putValue(c, image.getPixel(voxel3D));
                    c++;
                }
            }
            arrayUtil.setSize(c);
            ArrayUtil distinctValues = arrayUtil.distinctValues();
            for (int i = 0; i < distinctValues.size(); i++) {
                int other = distinctValues.getValueInt(i);
                if ((other == 0) || (other == value)) continue;
                //String key = value + "-" + other;
                //if (other < value) key = other + "-" + value;
                if (!interactions.contains(value, other)) {
                    interactions.addInteraction(object3D, population.getObjectByValue(other), arrayUtil.countValue(other));
                }
                //map.get(key).incrementVolumeColoc(arrayUtil.countValue(other)); // next version
                //for (int v = 0; v < arrayUtil.countValue(other); v++) interactions.get(key).incrementVolumeColoc();
            }
        }

        return interactions;
    }
}
