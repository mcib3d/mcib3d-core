/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mcib3d.image3d.regionGrowing;

import java.util.ArrayList;

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
public class AssociationRegion {
    // ordered association between regions (from max to min)

    ArrayList<Integer> association;

    public AssociationRegion() {
        association = new ArrayList();
    }

    public void addRegion(int region) {
        if (association.contains(region)) {
            return;
        }
        if (association.isEmpty()) {
            association.add(region);
        } else {
            int i = 0;
            while ((i < association.size()) && (association.get(i) > region)) {
                i++;
            }
            if (i == association.size()) {
                association.add(region);
            } else {
                association.add(i, region);
            }
        }
    }

    public void addAssociation(AssociationRegion asso) {
        for (int r : asso.getList()) {
            addRegion(r);
        }
    }

    public AssociationRegion getCopy() {
        AssociationRegion copy = new AssociationRegion();
        for (int i : association) {
            copy.association.add(i);
        }
        return copy;
    }

    public boolean contains(int reg) {
        return association.contains(reg);
    }

    public boolean containsOnly(ArrayList<Integer> labels) {
        for (int r : association) {
            if (!labels.contains(r)) {
                return false;
            }
        }
        return true;
    }

    public boolean replaceRegion(int oldR, int newR) {
        if (!contains(oldR)) {
            return false;
        }
        association.remove(association.indexOf(oldR));
        if (!association.contains(newR)) {
            addRegion(newR);
            return false;
        }
        return true;
    }

    public boolean isValid() {
        return ((association != null) && (association.size() > 1));
    }

    public int size() {
        return association.size();
    }

    public ArrayList<Integer> getList() {
        return association;
    }

    public int getFirst() {
        return association.get(0);
    }

    public boolean equals(AssociationRegion other) {
        return association.equals(other.getList());
    }

    @Override
    public String toString() {
        if (association.isEmpty()) {
            return "";
        }
        String S = "" + association.get(0);
        for (int i = 1; i < association.size(); i++) {
            S = S.concat("_" + association.get(i));
        }

        return S;
    }

    public int getMax() {
        // normally first value
        return association.get(0);
    }
}
