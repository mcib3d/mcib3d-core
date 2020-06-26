/*    */
package TRACKING;
/*    */
/*    */

import mcib3d.geom.Object3D;

/*    */
/*    */ public class AssociationPair {
    /*    */   private Object3D object3D1;
    /*    */   private Object3D object3D2;
    /*    */   private double asso;

    /*    */
    /*    */
    public AssociationPair(Object3D object3D1, Object3D object3D2, double asso) {
        /* 11 */
        this.object3D1 = object3D1;
        /* 12 */
        this.object3D2 = object3D2;
        /* 13 */
        this.asso = asso;
        /*    */
    }

    /*    */
    /*    */
    public Object3D getObject3D1() {
        /* 17 */
        return this.object3D1;
        /*    */
    }

    /*    */
    /*    */
    public Object3D getObject3D2() {
        /* 21 */
        return this.object3D2;
        /*    */
    }

    /*    */
    /*    */
    public double getAsso() {
        /* 25 */
        return this.asso;
        /*    */
    }
    /*    */
}


/* Location:              /home/thomas/Prog/MCIB/mcib3d-dev/mcib3d_dev/!/TRACKING/AssociationPair.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */