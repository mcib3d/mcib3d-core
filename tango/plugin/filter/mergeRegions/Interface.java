package tango.plugin.filter.mergeRegions;

import java.util.HashSet;


/**
 *
 **
 * /**
 * Copyright (C) 2012 Jean Ollion
 *
 *
 *
 * This file is part of tango
 *
 * tango is free software; you can redistribute it and/or modify it under the
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
 * @author Jean Ollion
 */

public class Interface implements java.lang.Comparable<Interface> {
    
    Region r1, r2;
    InterfaceCollection col;
    double strength;
    public Interface(Region r1, Region r2, InterfaceCollection col) {
        this.col=col;
        if (r1.label<=r2.label) {
            this.r1=r1;
            this.r2=r2;
        } else {
            this.r2=r1;
            this.r1=r2;
        }
    }
    
    public RegionPair getKey() {
        return new RegionPair(r1.label, r2.label);
    }

    @Override
    public int compareTo(Interface o) {
        if (strength < o.strength) return -1;
        else if(strength > o.strength) return 1;
        else return 0;
    }
    
    public void computeStrength() {
    }
    
    public Region getOther(Region r) {
        if (r1.equals(r)) return r2;
        else return r1;
    }
    
    public boolean checkFusionCriteria() {
        return true;
    }
    
    public void mergeInterface(Interface other) {
        computeStrength();
    }
    
    public void switchRegion(Region oldRegion, Region newRegion) {
        Region other = this.getOther(oldRegion);
        if (other.label<=newRegion.label) {
            r1=other;
            r2=newRegion;
        } else {
            r1 = newRegion;
            r2=other;
        }
        computeStrength();
    }
    
    public boolean hasNoInteractants() {
        return r1.hasNoInteractant() || r2.hasNoInteractant();
    }
    
    @Override
    public boolean equals(Object o) {
        if (o instanceof Interface) {
            return ((Interface)o).r1.label==r1.label && ((Interface)o).r2.label==r2.label;
        } else if (o instanceof RegionPair) return r1.label==((RegionPair)o).r1 && r2.label==((RegionPair)o).r2;
        else return false;
    }
    
    @Override
    public int hashCode() {
        int hash = 3;
        hash = 97 * hash + this.r1.label;
        hash = 97 * hash + this.r2.label;
        return hash;
    }
    
    @Override
    public String toString() {
        return "Interface: "+r1.label+ " + "+r2.label + " Strength: "+strength;
    }
}
