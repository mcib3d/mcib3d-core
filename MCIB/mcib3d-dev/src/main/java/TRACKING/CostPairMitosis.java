/*    */
package TRACKING;
/*    */
/*    */

import mcib3d.geom.Object3D;
import mcib3d.geom.Vector3D;
import mcib3d.geom.interactions.InteractionsList;

/*    */
/*    */
/*    */
/*    */
/*    */
/*    */
/*    */
/*    */ public class CostPairMitosis
        /*    */ implements AssociationCost
        /*    */ {
    /* 16 */ InteractionsList interactions = null;
    /* 14 */   private double minColoc = 0.0D;
    /* 15 */   private double maxCost = 100.0D;

    /*    */
    /*    */
    /*    */
    public CostPairMitosis() {
    }

    /*    */
    /*    */
    public CostPairMitosis(InteractionsList interactions) {
        /* 22 */
        this.interactions = interactions;
        /*    */
    }

    /*    */
    /*    */
    public double getMinColoc() {
        /* 26 */
        return this.minColoc;
        /*    */
    }

    /*    */
    /*    */
    public void setMinColoc(double minColoc) {
        /* 30 */
        this.minColoc = minColoc;
        /*    */
    }

    /*    */
    /*    */
    public double getMaxCost() {
        /* 34 */
        return this.maxCost;
        /*    */
    }

    /*    */
    /*    */
    public void setMaxCost(double maxCost) {
        /* 38 */
        this.maxCost = maxCost;
        /*    */
    }

    /*    */
    /*    */
    public InteractionsList getInteractions() {
        /* 42 */
        return this.interactions;
        /*    */
    }

    /*    */
    /*    */
    public void setInteractions(InteractionsList interactions) {
        /* 46 */
        this.interactions = interactions;
        /*    */
    }

    /*    */
    /*    */
    /*    */
    public double cost(Object3D object3D1, Object3D object3D2) {
        /* 51 */
        double cost = -1.0D;
        /* 52 */
        if (object3D1 == object3D2) return this.maxCost;
        /*    */
        /* 54 */
        int val1 = object3D1.getValue();
        /* 55 */
        int val2 = object3D2.getValue();
        /* 56 */
        if (this.interactions != null &&
                /* 57 */       !this.interactions.contains(Math.min(val1, val2), Math.max(val1, val2))) return -1.0D;
        /*    */
        /*    */
        /*    */
        /*    */
        /* 62 */
        Vector3D cen1 = object3D1.getCenterAsVector();
        /* 63 */
        object3D1.setNewCenter(object3D2);
        /* 64 */
        double pc1 = object3D1.pcColoc(object3D2);
        /* 65 */
        double pc2 = object3D2.pcColoc(object3D1);
        /*    */
        /*    */
        /* 68 */
        object3D1.setNewCenter(cen1);
        /* 69 */
        if (pc1 > this.minColoc && pc2 > this.minColoc)
            /* 70 */ cost = 100.0D - Math.min(pc1, pc2);
        /* 71 */
        return cost;
        /*    */
    }
    /*    */
}


/* Location:              /home/thomas/Prog/MCIB/mcib3d-dev/mcib3d_dev/!/TRACKING/CostPairMitosis.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */