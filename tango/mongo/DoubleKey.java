package tango.mongo;
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

public class DoubleKey implements java.lang.Comparable<DoubleKey> {
    public int key1, key2;
    public DoubleKey(int key1, int key2) {
        this.key1=key1;
        this.key2=key2; 
    }
    
    @Override
    public boolean equals(Object o) {
        if (o==this) return true;
        if (o instanceof DoubleKey) {
            DoubleKey dk = (DoubleKey)o;
            return key1==dk.key1 && key2==dk.key2;
        }
        return false;
    }

    @Override
    public int hashCode() {
        /*int hash=1;
        hash=hash*31+key1;
        return hash*31+key2;
         *
         */
        return (key1+";"+key2).hashCode();
    }
    
    public int compareTo(DoubleKey key) {
        if (key1<key.key1) return -1;
        else if (key1==key.key1) {
            if (key2<key.key2) return -1;
            else if (key2>key.key2) return 1;
            else return 0;
        }
        return 1;
    }
    
    @Override
    public String toString() {
        return key1+";"+key2;
    }
}
