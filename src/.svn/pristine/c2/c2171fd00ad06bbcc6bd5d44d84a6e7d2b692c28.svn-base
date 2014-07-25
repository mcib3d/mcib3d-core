/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mcib3d.geom;

import ij.IJ;
import java.util.ArrayList;
import java.util.Iterator;
import javax.swing.border.EmptyBorder;

/**
 *
 **
 * /**
 * Copyright (C) 2008- 2011 Thomas Boudier
 *
 *
 *
 * This file is part of mcib3d
 *
 * mcib3d is free software; you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 3 of the License, or (at your option) any later
 * version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 *
 * @author thomas
 */
public class MereoObject3D {
    // reference object

    private Object3D A;
    private Object3D Adilated = null;
    // relationship object
    private Object3D B;
    private Object3D Bdilated = null;
    // intersection object
    private Object3DVoxels inter = null;
    // radius for neighboring, based on dilatations
    private float RadX, RadY, RadZ;
    // border to border distance between objects (inside border)
    public double distBB = Double.NaN;
    private boolean disjoint = true;
    // CONSTANTS
    private final static double SQRT3 = Math.sqrt(3);
    private final static double SMALL = 0.001;
    private boolean canonicRadii = false;
    static public final String DC = "DC";
    static public final String EC = "EC";
    static public final String PO = "PO";
    static public final String EQ = "EQ";
    static public final String TPP = "TPP";
    static public final String NTPP = "NTPP";
    static public final String TPPi = "TPPi";
    static public final String NTPPi = "NTPPi";
    static public final String EMPTY = "EMPTY";
    static public final String UNKNOWN = "UNKNOWN";

    /**
     *
     * @param A
     * @param B
     */
    public MereoObject3D(Object3D A, Object3D B) {
        this.A = A;
        this.B = B;
        RadX = 1;
        RadY = 1;
        RadZ = 1;
        checkCanonicRadii();
        buildInterObject(false);
    }

    public MereoObject3D(Object3D A, Object3D B, float radX, float radY, float radZ) {
        this.A = A;
        this.B = B;
        RadX = radX;
        RadY = radY;
        RadZ = radZ;
        checkCanonicRadii();
        buildInterObject(false);
    }

    public MereoObject3D(Object3D A, Object3D B, float radXY, float radZ) {
        this.A = A;
        this.B = B;
        RadX = radXY;
        RadY = radXY;
        RadZ = radZ;
        checkCanonicRadii();
        buildInterObject(false);
    }

    public MereoObject3D(Object3D A, Object3D B, float rad) {
        this.A = A;
        this.B = B;
        RadX = rad;
        RadY = rad;
        RadZ = rad;
        checkCanonicRadii();
        buildInterObject(false);
    }

    public Object3D getA() {
        return A;
    }

    public void setA(Object3D A) {
        this.A = A;
        Adilated = null;
        buildInterObject(false);
    }

    public Object3D getB() {
        return B;
    }

    public void setB(Object3D B) {
        this.B = B;
        Bdilated = null;
        buildInterObject(false);
    }

    public float getRadX() {
        return RadX;
    }

    public void setRadX(float RadX) {
        this.RadX = RadX;
        Adilated = null;
        Bdilated = null;
        checkCanonicRadii();
    }

    public float getRadY() {
        return RadY;
    }

    public void setRadY(float RadY) {
        this.RadY = RadY;
        Adilated = null;
        Bdilated = null;
        checkCanonicRadii();
    }

    public float getRadZ() {
        return RadZ;
    }

    public void setRadZ(float RadZ) {
        this.RadZ = RadZ;
        Adilated = null;
        Bdilated = null;
        checkCanonicRadii();
    }

    private void checkCanonicRadii() {
        if ((RadX == 1) && (RadY == 1) && (RadZ == 1)) {
            canonicRadii = true;
        }
    }

    private void buildInterObject(boolean contourtest) {
        //IJ.log("Building intersection object");

        inter = A.getIntersectionObject(B);

        if ((contourtest) && (inter != null)) {
            Iterator<Voxel3D> it = inter.getVoxels().iterator();
            while (it.hasNext()) {
                Voxel3D vox = it.next();
                boolean conta = A.isContour(vox);
                boolean contb = B.isContour(vox);
                int cc = 0;
                if (conta) {
                    cc = 1;
                }
                if (contb) {
                    cc += 2;
                }
                vox.setValue(cc);
            }
        }

        if (inter != null) {
            disjoint = (inter.getVolumePixels() == 0);
        }
        if ((!A.isEmpty()) && (!B.isEmpty())) {
            distBB = A.distBorderUnit(B);
        } else {
            distBB = Double.NaN;
        }
    }

    /////////////////////////////////////////////////
    // O(A,B)
    private boolean Overlap() {
        return !disjoint;
    }

    // P(A,B) A is inside B
    private boolean Parthood() {
        if (disjoint) {
            return false;
        }
        boolean Anotnull = (A.getVolumePixels() > 0);
        boolean AinB = B.includes(A);

        return Anotnull && AinB;
    }

    // Pi(A,B)=P(B,A) B is inside A
    private boolean ParthoodInverse() {
        if (disjoint) {
            return false;
        }
        boolean Bnotnull = (B.getVolumePixels() > 0);
        boolean BinA = A.includes(B);

        return Bnotnull && BinA;
    }

    //////////////////// RCC5 ////////////////////////
    // EQ(A,B)
    public boolean Equality() {
        if (disjoint) {
            return false;
        }
        return Parthood() && ParthoodInverse();
    }

    // PP(A,B)
    public boolean ProperParthood() {
        if (disjoint) {
            return false;
        }
        return Parthood() && !Equality();
    }

    // PPi(A,B):=PP(B,A)
    public boolean ProperParthoodInverse() {
        if (disjoint) {
            return false;
        }
        return ParthoodInverse() && !Equality();
    }

    // DR(A,B)
    public boolean Discrete() {
        return disjoint;
    }

    // PO(A,B)
    public boolean PartialOverlap() {
        if (disjoint) {
            return false;
        }
        return Overlap() && !Parthood() && !ParthoodInverse();
    }
    //////////////////// RCC5 ////////////////////////

    //////////////////// RCC8 ////////////////////////
    ///////////////// generic public version //
    //////////////////////////////////////////////////
    // DC(A,B) : DR(A,B) && distBB>sqrt3
    public boolean Disconnection() {
        if (canonicRadii) {
            return Disconnection1();
        } else {
            return DisconnectionRad();
        }
    }

    // EC(A,B) : DR(A,B) && distBB<=sqrt3
    public boolean ExternalConnection() {
        if (canonicRadii) {
            return ExternalConnection1();
        } else {
            return ExternalConnectionRad();
        }
    }

    // TPP(A,B) : PP(A,B) && distBB==0
    public boolean TangentialProperParthood() {
        if (canonicRadii) {
            return TangentialProperParthood1();
        } else {
            return TangentialProperParthoodRad();
        }
    }

    // NTPP(A,B) : PP(A,B) && distBB>0
    public boolean NonTangentialProperParthood() {
        if (canonicRadii) {
            return NonTangentialProperParthood1();
        } else {
            return NonTangentialProperParthoodRad();
        }
    }

    // TPPi(A,B) : PPi(A,B) && distBB==0
    public boolean TangentialProperParthoodInverse() {
        if (canonicRadii) {
            return TangentialProperParthoodInverse1();
        } else {
            return TangentialProperParthoodInverseRad();
        }
    }

    // NTPPi(A,B) : PPi(A,B) && distBB>0
    public boolean NonTangentialProperParthoodInverse() {
        if (canonicRadii) {
            return NonTangentialProperParthoodInverse1();
        } else {
            return NonTangentialProperParthoodInverseRad();
        }
    }

    public String getRCC8Relationship() {
        if (A.isEmpty() || B.isEmpty()) {
            // IJ.log("EMPTY "+A+" "+B+" "+A.getVolumePixels()+" "+B.getVolumePixels()+" "+A.isEmpty()+" "+B.isEmpty());
            return EMPTY;
        } else if (Disconnection()) {
            return DC;
        } else if (ExternalConnection()) {
            return EC;
        } else if (PartialOverlap()) {
            return PO;
        } else if (Equality()) {
            return EQ;
        } else if (TangentialProperParthood()) {
            return TPP;
        } else if (NonTangentialProperParthood()) {
            return NTPP;
        } else if (TangentialProperParthoodInverse()) {
            return TPPi;
        } else if (NonTangentialProperParthoodInverse()) {
            return NTPPi;
        } else {
            return UNKNOWN;
        }
    }
    //////////////////// RCC8 ////////////////////////

    //////////////////// RCC8 ////////////////////////
    ///////////////// dist Border-Border version ////
    //////////////////////////////////////////////////
    // DC(A,B) : DR(A,B) && distBB>sqrt3
    private boolean Disconnection1() {
        return Discrete() && (distBB > SQRT3);
    }

    // EC(A,B) : DR(A,B) && distBB<=sqrt3
    private boolean ExternalConnection1() {
        return Discrete() && (distBB <= SQRT3);
    }

    // TPP(A,B) : PP(A,B) && distBB==0
    private boolean TangentialProperParthood1() {
        return ProperParthood() && (distBB <= SMALL);
    }

    // NTPP(A,B) : PP(A,B) && distBB>0
    private boolean NonTangentialProperParthood1() {
        return ProperParthood() && (distBB > SMALL);
    }

    // TPPi(A,B) : PPi(A,B) && distBB==0
    private boolean TangentialProperParthoodInverse1() {
        return ProperParthoodInverse() && (distBB <= SMALL);
    }

    // NTPPi(A,B) : PPi(A,B) && distBB>0
    private boolean NonTangentialProperParthoodInverse1() {
        return ProperParthoodInverse() && (distBB > SMALL);
    }
    //////////////////// RCC8 ////////////////////////

    //////////////////// RCC8 ////////////////////////
    //////// Dilated object version //////////////////
    //////////////////////////////////////////////////
    private Object3D getDilatedA() {
        if (Adilated == null) {
            Adilated = A.getDilatedObject(RadX, RadY, RadZ, false);
        }
        return Adilated;
    }

    private Object3D getDilatedB() {
        if (Bdilated == null) {
            Bdilated = B.getDilatedObject(RadX, RadY, RadZ, false);
        }
        return Bdilated;
    }

    // DC(A,B) : DR(A,B) && !O(Adilated, B)
    private boolean DisconnectionRad() {
        if (!Discrete()) {
            return false;
        } else {
            MereoObject3D mereo = new MereoObject3D(getDilatedA(), B);
            return !mereo.Overlap();
        }
    }

    // EC(A,B) : DR(A,B) && O(Adilated, B)
    private boolean ExternalConnectionRad() {
        if (!Discrete()) {
            return false;
        } else {
            MereoObject3D mereo = new MereoObject3D(getDilatedA(), B);
            return mereo.Overlap();
        }
    }

    // TPP(A,B) : PP(A,B) && !PP(Adilated,B)
    private boolean TangentialProperParthoodRad() {
        if (!ProperParthood()) {
            return false;
        } else {
            MereoObject3D mereo = new MereoObject3D(getDilatedA(), B);
            return !mereo.ProperParthood();
        }
    }

    // NTPP(A,B) : PP(A,B) && PP(Adilated,B)
    private boolean NonTangentialProperParthoodRad() {
        if (!ProperParthood()) {
            return false;
        } else {
            MereoObject3D mereo = new MereoObject3D(getDilatedA(), B);
            return mereo.ProperParthood();
        }
    }

    // TPPi(A,B) : PPi(A,B) && !PPi(A,Bdilated)
    private boolean TangentialProperParthoodInverseRad() {
        if (!ProperParthoodInverse()) {
            return false;
        } else {
            MereoObject3D mereo = new MereoObject3D(A, getDilatedB());
            return !mereo.ProperParthoodInverse();
        }
    }

    // TPPi(A,B) : PPi(A,B) && PPi(A,Bdilated)
    private boolean NonTangentialProperParthoodInverseRad() {
        if (!ProperParthoodInverse()) {
            return false;
        } else {
            MereoObject3D mereo = new MereoObject3D(A, getDilatedB());
            return mereo.ProperParthoodInverse();
        }
    }
    //////////////////// RCC8 ////////////////////////

    // DR(A,B) et PPi(Aconvex,B)
    public boolean surrounds() {
        if (!Discrete()) {
            return false;
        } else {
            // compute convex object
            Object3DVoxels obj = A.getConvexObject();
            MereoObject3D mereo = new MereoObject3D(obj, B);
            return (mereo.ProperParthoodInverse());
        }
    }

    /**
     *
     * @return
     */
    public Object3DVoxels getInterObject() {
        return inter;
    }

    /**
     *
     * @return
     */
    public double pcColocA() {
        if (Discrete()) {
            return 0;
        }
        return 100.0 * ((double) inter.getVolumePixels() / (double) A.getVolumePixels());
    }

    /**
     *
     * @return
     */
    public double pcColocB() {
        if (Discrete()) {
            return 0;
        }
        return 100.0 * ((double) inter.getVolumePixels() / (double) B.getVolumePixels());
    }

    /**
     *
     * @param ba
     * @param bb
     * @return
     */
    public int nbVoxContourOverlap(boolean ba, boolean bb) {
        if (Discrete()) {
            return 0;
        }
        int c = 0;
        int val1 = 0;
        int val2 = 0;
        // one object
        if (ba) {
            val1 = 1;
        }
        if (bb) {
            val1 = 2;
        }
        if (ba && bb) {
            val1 = 3;
        }
        // two objects
        if (ba || bb) {
            val2 = 3;
        }
        ArrayList<Voxel3D> list = inter.getVoxels();
        Iterator<Voxel3D> it = list.iterator();
        while (it.hasNext()) {
            Voxel3D vox = it.next();
            if ((vox.getValue() == val1) || (vox.getValue() == val2)) {
                c++;
            }
        }
        return c;
    }
}
