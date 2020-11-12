package mcib3d.tracking_dev;

import mcib3d.geom.Object3D;
import mcib3d.geom.Vector3D;
import mcib3d.geom.interactions.InteractionsList;

public class CostPairMitosis
        implements AssociationCost {
    InteractionsList interactions = null;
    private double minColoc = 0.0D;
    private double maxCost = 100.0D;


    public CostPairMitosis() {
    }

    public CostPairMitosis(InteractionsList interactions) {
        this.interactions = interactions;

    }

    public double getMinColoc() {
        return this.minColoc;
    }


    public void setMinColoc(double minColoc) {
        this.minColoc = minColoc;
    }

    public double getMaxCost() {
        return this.maxCost;
    }


    public void setMaxCost(double maxCost) {
        this.maxCost = maxCost;
    }

    public InteractionsList getInteractions() {
        /* 42 */
        return this.interactions;

    }

    public void setInteractions(InteractionsList interactions) {
        this.interactions = interactions;
    }

    public double cost(Object3D object3D1, Object3D object3D2) {
        double cost = -1.0D;
        if (object3D1 == object3D2) return this.maxCost;

        int val1 = object3D1.getValue();
        int val2 = object3D2.getValue();
        if (this.interactions != null && !this.interactions.contains(Math.min(val1, val2), Math.max(val1, val2)))
            return -1.0D;

        Vector3D cen1 = object3D1.getCenterAsVector();
        object3D1.setNewCenter(object3D2);
        double pc1 = object3D1.pcColoc(object3D2);
        double pc2 = object3D2.pcColoc(object3D1);

        object3D1.setNewCenter(cen1);
        if (pc1 > this.minColoc && pc2 > this.minColoc)
            cost = 100.0D - Math.min(pc1, pc2);
        return cost;
    }
}