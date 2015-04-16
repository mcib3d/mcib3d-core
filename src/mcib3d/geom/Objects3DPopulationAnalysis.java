/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mcib3d.geom;

import ij.IJ;
import ij.ImagePlus;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import mcib3d.utils.KDTreeC;

/**
 *
 **
 * /**
 * Copyright (C) 2008- 2012 Thomas Boudier and others
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
public class Objects3DPopulationAnalysis extends Objects3DPopulation {

    ArrayList<Object3D> orderedDistancesCenter[] = null;
    ArrayList<Object3D> orderedDistancesBorder[] = null;

    public Objects3DPopulationAnalysis(ImagePlus plus) {
        super(plus);
        for (int i = 0; i < this.getNbObjects(); i++) {
            this.getObject(i).computeContours();
        }
    }
    
    public Objects3DPopulationAnalysis(){
        super();
    }

    private void computeOrderedDistancesCenter() {
        orderedDistancesCenter = new ArrayList[this.getNbObjects()];
        for (int i = 0; i < this.getNbObjects(); i++) {
            orderedDistancesCenter[i] = this.orderedDistancesCenterObjects(this.getObject(i));
        }
    }

    private void computeOrderedDistancesBorder() {
        orderedDistancesBorder = new ArrayList[this.getNbObjects()];
        for (int i = 0; i < this.getNbObjects(); i++) {
            IJ.showStatus("ordered border " + i);
            orderedDistancesBorder[i] = this.orderedDistancesBorderObjects(this.getObject(i));
        }
    }

    public ArrayList<Object3D> orderedDistancesBorderObjects(Object3D ob) {
        ArrayList<Object3D> res = new ArrayList<Object3D>();
        res.add(ob);
        for (int i = 1; i < this.getNbObjects(); i++) {
            Object3D clo = this.kClosestBorder(ob, i);
            if (clo != null) {
                res.add(clo);
            }
        }

        return res;
    }

    public ArrayList<Object3D> orderedDistancesCenterObjects(Object3D ob) {
        ArrayList<Object3D> res = new ArrayList<Object3D>();
        res.add(ob);
        for (int i = 1; i < this.getNbObjects(); i++) {
            Object3D clo = this.kClosestCenter(ob, i, true);
            if (clo != null) {
                res.add(clo);
            }
        }

        return res;
    }

    public boolean separatedCenter(int i1, int i2) {
        if (i1 == i2) {
            return false;
        }
        if (orderedDistancesCenter == null) {
            this.computeOrderedDistancesCenter();
        }
        ArrayList<Object3D> res1 = orderedDistancesCenter[i1];
        ArrayList<Object3D> res2 = orderedDistancesCenter[i2];

        int idx1 = res1.indexOf(this.getObject(i2));
        int idx2 = res2.indexOf(this.getObject(i1));
        if ((idx1 >= 0) && (idx2 >= 0)) {
            List<Object3D> subres1 = res1.subList(1, idx1);
            List<Object3D> subres2 = res2.subList(1, idx2);
            return !Collections.disjoint(subres1, subres2);
        } else {
            return true;
        }
    }

    public boolean separatedBorder(int i1, int i2) {
        if (i1 == i2) {
            return false;
        }
        if (orderedDistancesBorder == null) {
            this.computeOrderedDistancesBorder();
        }
        ArrayList<Object3D> res1 = orderedDistancesBorder[i1];
        ArrayList<Object3D> res2 = orderedDistancesBorder[i2];
        int idx1 = res1.indexOf(this.getObject(i2));
        int idx2 = res2.indexOf(this.getObject(i1));
        if ((idx1 >= 0) && (idx2 >= 0)) {
            List<Object3D> subres1 = res1.subList(1, res1.indexOf(this.getObject(i2)));
            List<Object3D> subres2 = res2.subList(1, res2.indexOf(this.getObject(i1)));
            return !Collections.disjoint(subres1, subres2);
        } else {
            return true;
        }
    }
}
