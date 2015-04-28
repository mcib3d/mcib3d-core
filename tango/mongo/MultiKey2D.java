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
public class MultiKey2D implements java.lang.Comparable<MultiKey2D> {
    public String fieldName, mesureName;
    public int nucIdx;
    
    public MultiKey2D(String fieldName, int nucIdx, String mesure) {
        this.fieldName=fieldName;
        this.nucIdx=nucIdx;
        this.mesureName=mesure;
    }

    public boolean sameLine(String fieldName, int nucIdx) {
        return  nucIdx==this.nucIdx && fieldName.equals(this.fieldName);
    }
    
    public boolean sameLine(MultiKey2D mk) {
        return nucIdx==mk.nucIdx && fieldName.equals(mk.fieldName);
    }
    
    @Override
    public String toString() {
        return fieldName+ ";"+nucIdx+ ";";
    }
    
    @Override
    public boolean equals(Object o) {
        if (o==this) return true;
        if (o instanceof MultiKey2D) {
            MultiKey2D mk = (MultiKey2D)o;
            return nucIdx==mk.nucIdx && fieldName.equals(mk.fieldName) && mesureName.equals(this.mesureName);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return (fieldName+";"+nucIdx+";"+mesureName).hashCode();
    }
    
    public int compareTo(MultiKey2D mk) {
        int c = fieldName.compareTo(mk.fieldName);
        if (c==0) {
            if (nucIdx<mk.nucIdx) return -1;
            else if (nucIdx>mk.nucIdx) return 1;
            else return mesureName.compareTo(mk.mesureName);
        } else return c;
    }

}
