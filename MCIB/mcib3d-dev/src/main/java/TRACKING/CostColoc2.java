/*    */
package TRACKING;
/*    */
/*    */

import mcib3d.geom.Object3D;

/*    */
/*    */
/*    */
/*    */
/*    */ public class CostColoc2
        /*    */ implements AssociationCost
        /*    */ {
    /*    */
    public double cost(Object3D object3D1, Object3D object3D2) {
        /* 12 */
        double cost = -1.0D;
        /* 13 */
        double V1 = object3D1.getVolumePixels();
        /* 14 */
        double V2 = object3D2.getVolumePixels();
        /* 15 */
        int coloc = object3D1.getColoc(object3D2);
        /* 16 */
        if (coloc > 0) cost = 1.0D - coloc / Math.max(V1, V2);
        /*    */
        /* 18 */
        return 0.0D;
        /*    */
    }
    /*    */
}


/* Location:              /home/thomas/Prog/MCIB/mcib3d-dev/mcib3d_dev/!/TRACKING/CostColoc2.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */