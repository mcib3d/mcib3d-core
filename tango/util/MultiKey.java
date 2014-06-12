package tango.util;

import java.util.Arrays;
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
public class MultiKey {
    protected int[] keys;
    public MultiKey(int[] keys) {
        if (keys.length==1) {
            this.keys=new int[2];
            this.keys[0]=keys[0];
            this.keys[1]=keys[0];
        }
        else this.keys=keys; 
    }

    public int getKey(int index) {
        if (index<keys.length) return keys[index];
        else if (keys.length>0) return keys[keys.length-1];
        else return -1;
    }

    public int[] getKeys() {
        return keys;
    }

    @Override
    public boolean equals(Object o) {
        if (o==this) return true;
        if (o instanceof MultiKey) {
            MultiKey mk = (MultiKey)o;
            if (keys.length==mk.keys.length) {
                for (int i = 0; i<keys.length; i++) if (keys[i]!=mk.keys[i]) return false;
                return true;
            }
        }
        return false;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 89 * hash + Arrays.hashCode(this.keys);
        return hash;
    }

    @Override
    public String toString() {
        String s = "";
        for (int i = 0; i<keys.length; i++) {
            s+="key:"+i+ "="+keys[i]+"; ";
        }
        return s;
    }

}