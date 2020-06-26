package mcib3d.tracking_dev;

import mcib3d.geom.Object3D;
import mcib3d.geom.PairColocalisation;
import mcib3d.geom.interactions.InteractionsList;

public class CostTouching implements AssociationCost {
    double maxCost;
    InteractionsList interactionsList;
    int minTouch = 10;


    public CostTouching(InteractionsList interactionsList) {
        this.interactionsList = interactionsList;
    }

    public double getMaxCost() {
        return this.maxCost;
    }


    public void setMaxCost(double maxCost) {
        this.maxCost = maxCost;
    }

    public double cost(Object3D object3D1, Object3D object3D2) {
        int val1 = object3D1.getValue();
        int val2 = object3D2.getValue();
        if (val1 == val2) return this.maxCost;
        PairColocalisation pair = this.interactionsList.getPair(val1, val2);
        if (pair == null) return -1.0D;
        int vol = pair.getVolumeColoc();
        if (vol == 0) return -1.0D;
        if (vol < this.minTouch) return -1.0D;

        return 1.0D / pair.getVolumeColoc();
    }
}