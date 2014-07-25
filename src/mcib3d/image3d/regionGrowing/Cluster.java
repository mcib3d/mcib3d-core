/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mcib3d.image3d.regionGrowing;

import java.util.ArrayList;
import org.paukov.combinatorics.Factory;
import org.paukov.combinatorics.Generator;
import org.paukov.combinatorics.ICombinatoricsVector;

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
public class Cluster extends AllRegionsAssociation {

    public ArrayList<Cluster> getSubClusters(int n) {
        ArrayList<Cluster> res = new ArrayList<Cluster>();
        if ((n > getNbLabels()) || (n < 1)) {
            return null;
        }
        // special cases
        if (n == getNbLabels()) {
            Cluster clu = new Cluster();
            for (AssociationRegion asso : this.getListAssociation()) {
                clu.addAssoRegion(asso.getCopy());
            }
            res.add(clu);
        } else if (n == 1) {
            for (int i : getListLabel()) {
                AssociationRegion a = new AssociationRegion();
                a.addRegion(i);
                Cluster clu = new Cluster();
                clu.addAssoRegion(a);
                res.add(clu);
            }
        } else {
            ICombinatoricsVector<Integer> all = Factory.createVector(this.getListLabel());
            Generator<Integer> gen = Factory.createSimpleCombinationGenerator(all, n);
            for (ICombinatoricsVector<Integer> combination : gen) {
                ArrayList<Integer> list2 = new ArrayList(combination.getVector());
                AllRegionsAssociation sub = this.getSubListAssociation(list2);
                //IJ.log("sub " + sub.toString() + " " + list2);
                if (sub.getNbLabels() > 0) {
                    //IJ.log("clusters " + Arrays.toString(sub.getClustersArray()));
                    if (sub.getNbClusters() == 1) {
                        //IJ.log("ok ");
                        // check first non-zero cluster
                        int i = 0;
                        ArrayList<Cluster> subclu = sub.getClusters();
                        while (subclu.get(i).getListAssociation().isEmpty()) {
                            i++;
                        }
                        res.add(subclu.get(i));
                        //IJ.log("ok " + i);
                    }
                }
            }
        }

        return res;
    }
}
