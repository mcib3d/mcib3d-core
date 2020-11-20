package mcib3d.tracking_dev;

import mcib3d.geom.Object3D;
import mcib3d.geom.Objects3DPopulation;
import mcib3d.geom.Objects3DPopulationColocalisation;

import java.util.ArrayList;
import java.util.HashMap;

public class CostColocalisation implements AssociationCost {
    Objects3DPopulationColocalisation colocalisation;
    HashMap<Object3D, Double> objectsMaxRadialDist1; // Max distance center to border for objects population1
    HashMap<Object3D, Double> objectsMaxRadialDist2; // Max distance center to border for objects population2
    private double distMax = 10; // max distance BB (pixel)

    public CostColocalisation(Objects3DPopulation population1, Objects3DPopulation population2, double distMax) {
        this.distMax = distMax;
        this.colocalisation = new Objects3DPopulationColocalisation(population1, population2);
        objectsMaxRadialDist1 = new HashMap<>(population1.getNbObjects());
        for (Object3D object3D : population1.getObjectsList()) {
            objectsMaxRadialDist1.put(object3D, object3D.getDistCenterMaxPixel());
        }
        objectsMaxRadialDist2 = new HashMap<>(population2.getNbObjects());
        for (Object3D object3D : population2.getObjectsList()) {
            objectsMaxRadialDist2.put(object3D, object3D.getDistCenterMaxPixel());
        }
    }

    public double getDistMax() {
        return distMax;
    }

    public void setDistMax(double distMax) {
        this.distMax = distMax;
    }

    @Override
    public double cost(Object3D object3D1, Object3D object3D2) {
        double cost = -1;
        double V1 = object3D1.getVolumePixels();
        double V2 = object3D2.getVolumePixels();
        int coloc = colocalisation.getColocObject(object3D1, object3D2);
        if (coloc > 0) { // colocalised
            cost = (1.0 - ((double) coloc / Math.max(V1, V2)));// was Max(V1,V2)
        } else { // not colocalised
            // check dist center-center first
            double distCC = object3D1.distCenterPixel(object3D2);
            double distCCMax = distMax + objectsMaxRadialDist1.get(object3D1) + objectsMaxRadialDist2.get(object3D2);
            if (distCC < distCCMax) {
                double dist = object3D1.distBorderPixel(object3D2);
                if (dist < distMax) cost = dist;
            }
        }

        return cost;
    }
}
