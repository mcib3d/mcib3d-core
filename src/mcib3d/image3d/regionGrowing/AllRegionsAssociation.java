/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mcib3d.image3d.regionGrowing;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import mcib3d.utils.ArrayUtil;

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
public class AllRegionsAssociation {

    ArrayList<AssociationRegion> list = null;
    ArrayList<Integer> labels = null;
    int[] clusters = null;

    public AllRegionsAssociation() {
        list = new ArrayList<AssociationRegion>();
    }

    public ArrayList<AssociationRegion> getListAssociation() {
        return list;
    }

    public AllRegionsAssociation getSubListAssociation(ArrayList<Integer> subLabels) {
        AllRegionsAssociation sub = new AllRegionsAssociation();
        for (AssociationRegion asso : list) {
            if (asso.containsOnly(subLabels)) {
                sub.addAssoRegion(asso);
            }
        }

        return sub;
    }

    public ArrayList<Integer> getListLabel() {
        if (labels == null) {
            labels = new ArrayList<Integer>();
            for (AssociationRegion asso : list) {
                for (int r : asso.getList()) {
                    if (!labels.contains(r)) {
                        labels.add(r);
                    }
                }
            }
        }
        return labels;
    }

    public int getNbLabels() {
        return getListLabel().size();
    }

    public AllRegionsAssociation getCopy() {
        AllRegionsAssociation copy = new AllRegionsAssociation();
        for (AssociationRegion li : list) {
            copy.list.add(li.getCopy());
        }
        if (labels != null) {
            copy.labels = new ArrayList<Integer>(labels);
        }

        return copy;
    }

    public boolean addAssoRegion(AssociationRegion asso) {
        if (!contains(asso)) {
            list.add(asso);
            labels = null;
            return true;
        } else {
            return false;
        }
    }

    public boolean contains(AssociationRegion asso) {
        for (AssociationRegion a : list) {
            if (a.equals(asso)) {
                return true;
            }
        }
        return false;
    }

    public void replaceRegion(int oldr, int newR) {
        ArrayList<AssociationRegion> toRemove = new ArrayList<AssociationRegion>();
        ArrayList<AssociationRegion> checkDuplicate = new ArrayList<AssociationRegion>();
        for (AssociationRegion A : list) {
            boolean rep = A.replaceRegion(oldr, newR);
            if (!A.isValid()) {
                toRemove.add(A);
            } else if (rep) {
                checkDuplicate.add(A);
            }
        }
        if (!toRemove.isEmpty()) {
            list.removeAll(toRemove);
        }
        for (AssociationRegion A : checkDuplicate) {
            removeDuplicate(A);
        }
        labels = null;
    }

    public void replaceRegion(int[] oldr, int newR) {
        for (int o : oldr) {
            replaceRegion(o, newR);
        }
        labels = null;
    }

    private int indexOf(AssociationRegion A) {
        int i = 0;
        while ((i < list.size()) && (!list.get(i).equals(A))) {
            i++;
        }
        if (i == list.size()) {
            return -1;
        } else {
            return i;
        }
    }

    private int lastIndexOf(AssociationRegion A) {
        int i = list.size() - 1;
        while ((i >= 0) && (!list.get(i).equals(A))) {
            i--;
        }
        return i;
    }

    private void removeDuplicate(AssociationRegion A) {
        int p1 = indexOf(A);
        int p2 = lastIndexOf(A);
        if (p1 == -1) {
            return;
        }
        while (p1 != p2) {
            list.remove(p2);
            p1 = indexOf(A);
            p2 = lastIndexOf(A);
        }
    }

    private int getMaxRegion() {
        if (list.isEmpty()) {
            return 0;
        }
        int max = list.get(0).getMax();
        for (AssociationRegion a : list) {
            if (a.getMax() > max) {
                max = a.getMax();
            }
        }
        return max;
    }

    private void computeClusters() {
        int max = getMaxRegion();
        clusters = new int[max + 1];
        Arrays.fill(clusters, 0);

        int c = 1;
        for (AssociationRegion A : list) {
            int cref;
            ArrayList<Integer> la = A.getList();
            if (clusters[la.get(0)] == 0) {
                cref = c;
                clusters[la.get(0)] = c;
                c++;
            } else {
                cref = clusters[la.get(0)];
            }
            for (int i = 1; i < A.size(); i++) {
                if (clusters[la.get(i)] == 0) {
                    clusters[la.get(i)] = cref;
                } else {
                    int cref2 = clusters[la.get(i)];
                    if (cref < cref2) {
                        for (int j = 0; j < clusters.length; j++) {
                            if (clusters[j] == cref2) {
                                clusters[j] = cref;
                            }
                        }
                    } else {
                        for (int j = 0; j < clusters.length; j++) {
                            if (clusters[j] == cref) {
                                clusters[j] = cref2;
                            }
                        }
                    }
                }
            }
        }
    }

    public int[] getClustersArray() {
        if (clusters == null) {
            computeClusters();
        }

        return clusters;
    }

    public int getNbClusters() {
        if (clusters == null) {
            computeClusters();
        }

        ArrayUtil tab = new ArrayUtil(clusters);
        tab = tab.distinctValues();
        if (tab.getMinimum() == 0) {
            return tab.getSize() - 1;
        } else {
            return tab.getSize();
        }
    }

    public ArrayList<Cluster> getClusters() {
        if (clusters == null) {
            computeClusters();
        }
        ArrayUtil tab = new ArrayUtil(clusters);
        int nbClusters = (int) tab.getMaximum() + 1;
        ArrayList<Cluster> clus = new ArrayList<Cluster>(nbClusters);
        for (int i = 0; i < nbClusters; i++) {
            clus.add(new Cluster());
        }

        for (AssociationRegion asso : list) {
            int c = clusters[asso.getFirst()];
            if (c != 0) {
                clus.get(c).addAssoRegion(asso);
            }
        }
        return clus;
    }

    @Override
    public String toString() {
        if (list.isEmpty()) {
            return "";
        }
        String S = "" + list.get(0);
        for (int i = 1; i < list.size(); i++) {
            S = S.concat("*" + list.get(i));
        }
        return S;
    }
}
