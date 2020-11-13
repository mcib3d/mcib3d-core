package mcib3d.tracking_dev;

import mcib3d.geom.Object3D;
import mcib3d.geom.Objects3DPopulationColocalisation;
import mcib3d.geom.PairColocalisation;

import java.util.ArrayList;

public class CostColocalisation implements AssociationCost {
    Objects3DPopulationColocalisation colocalisation;
    private double distMax = 10;

    public CostColocalisation(Objects3DPopulationColocalisation colocalisation) {
        this.colocalisation = colocalisation;
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
        ArrayList<PairColocalisation> pairColocalisations = colocalisation.getObject1ColocalisationPairs(object3D1);
        for (PairColocalisation pairColocalisation : pairColocalisations) {
            if (pairColocalisation.getObject3D2().getValue() == object3D2.getValue()) {
                int coloc = pairColocalisation.getVolumeColoc();
                if (coloc > 0) cost = (1.0 - ((double) coloc / Math.max(V1, V2)));// was Max(V1,V2)
                else {
                    double dist = object3D1.distBorderPixel(object3D2);
                    if (dist < distMax) cost = dist;
                }
                break;
            }
        }

        return cost;
    }
}
