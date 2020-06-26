/*    */
package mcib3d.tracking_dev;
/*    */
/*    */

import mcib3d.geom.Object3D;
import mcib3d.geom.PairColocalisation;
import mcib3d.geom.interactions.InteractionsList;

/*    */
/*    */
/*    */
/*    */ public class CostTouching implements AssociationCost {
    /*    */ double maxCost;
    /*    */ InteractionsList interactionsList;
    /* 10 */ int minTouch = 10;

    /*    */
    /*    */
    public CostTouching(InteractionsList interactionsList) {
        /* 13 */
        this.interactionsList = interactionsList;
        /*    */
    }

    /*    */
    /*    */
    public double getMaxCost() {
        /* 17 */
        return this.maxCost;
        /*    */
    }

    /*    */
    /*    */
    public void setMaxCost(double maxCost) {
        /* 21 */
        this.maxCost = maxCost;
        /*    */
    }

    /*    */
    /*    */
    /*    */
    public double cost(Object3D object3D1, Object3D object3D2) {
        /* 26 */
        int val1 = object3D1.getValue();
        /* 27 */
        int val2 = object3D2.getValue();
        /* 28 */
        if (val1 == val2) return this.maxCost;
        /* 29 */
        PairColocalisation pair = this.interactionsList.getPair(val1, val2);
        /* 30 */
        if (pair == null) return -1.0D;
        /* 31 */
        int vol = pair.getVolumeColoc();
        /* 32 */
        if (vol == 0) return -1.0D;
        /* 33 */
        if (vol < this.minTouch) return -1.0D;
        /* 34 */
        return 1.0D / pair.getVolumeColoc();
        /*    */
    }
    /*    */
}


/* Location:              /home/thomas/Prog/MCIB/mcib3d-dev/mcib3d_dev/!/TRACKING/CostTouching.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */