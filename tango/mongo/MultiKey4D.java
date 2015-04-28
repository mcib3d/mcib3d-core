package tango.mongo;
import java.util.TreeMap;
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
public class MultiKey4D implements java.lang.Comparable<MultiKey4D> {
    String fieldName, mesureName;
    int nucIdx, p1, p2;
    
    public MultiKey4D(String fieldName, int nucIdx, int p1, int p2, String mesure) {
        this.fieldName=fieldName;
        this.nucIdx=nucIdx;
        this.mesureName=mesure;
        this.p1=p1;
        this.p2=p2;
    }

    public boolean sameLine(String fieldName, int nucIdx, int p1, int p2) {
        return p1==this.p1 && p2==this.p2 && nucIdx==this.nucIdx && fieldName.equals(this.fieldName);
    }
    
    public boolean sameLine(MultiKey4D mk) {
        return p1==mk.p1 && p2==mk.p2 && nucIdx==mk.nucIdx && fieldName.equals(mk.fieldName);
    }
    
    @Override
    public String toString() {
        return fieldName+ ";"+nucIdx+ ";" + p1+ ";" + p2+ ";";
    }
    
    @Override
    public boolean equals(Object o) {
        if (o==this) return true;
        if (o instanceof MultiKey4D) {
            MultiKey4D mk = (MultiKey4D)o;
            return p1==mk.p1 && p2==mk.p2 && nucIdx==mk.nucIdx && fieldName.equals(mk.fieldName) && mesureName.equals(this.mesureName);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return (fieldName+";"+nucIdx+";"+p1+";"+p2+";"+mesureName).hashCode();
    }
    
    public int compareTo(MultiKey4D mk) {
        int c = fieldName.compareTo(mk.fieldName);
        if (c==0) {
            if (nucIdx<mk.nucIdx) return -1;
            else if (nucIdx>mk.nucIdx) return 1;
            else if (p1<mk.p1) return -1;
            else if (p1>mk.p1) return 1;
            else if (p2<mk.p2) return -1;
            else if (p2>mk.p2) return 1;
            else return mesureName.compareTo(mk.mesureName);
        } else return c;
    }

}
