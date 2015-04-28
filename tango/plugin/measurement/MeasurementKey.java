package tango.plugin.measurement;

import java.util.ArrayList;
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
public class MeasurementKey implements Comparable<MeasurementKey>{
    int[] structures;
    int[] count;
    public int type;
    
    public MeasurementKey(int[] structures, int type) {
        this.structures=structures;
        this.type=type;
    }
    
    public int[] getStructures() {
        return structures;
    }
    
    public int[] getCount() {
        if (count==null) computeCount();
        return count;
    }
    
    public String getStructuresAsString() {
        if (structures==null||structures.length==0) return "";
        String res = structures[0]+"";
        for (int i = 1; i<structures.length; i++)  res+=";"+structures[i];
        return res;
    }
    
    @Override 
    public boolean equals(Object o) {
        if (o instanceof MeasurementKey) {
            MeasurementKey mk = (MeasurementKey)o;
            if (type==mk.type && mk.structures.length==structures.length) {
                for (int i = 0; i<structures.length; i++) {
                    //System.out.println("equals mk:"+structures[i]+ " vs "+ mk.structures[i]);
                    if (structures[i]!=mk.structures[i]) {
                        //System.out.println("equals false");
                        return false;
                    }
                } 
                //System.out.println("equals true");
                return true;
            }
        }
        //System.out.println("equals false");
        return false;
    }
    
    protected void computeCount() {
        int max=structures[0];
        for (int i : structures) if (i>max) max=i;
        count=new int[max+1];
        for (int i : structures) count[i]=1;
    }
    
    public boolean equals2(Object o) { //no order & no duplicate
        if (o instanceof MeasurementKey) {
            MeasurementKey mk = (MeasurementKey)o;
            if (type==mk.type) {
                if (count==null) computeCount();
                if (mk.count==null) mk.computeCount();
                if (count.length==mk.count.length) {
                    for (int i = 0; i<count.length; i++) {
                        if (count[i]>0 && mk.count[i]==0 || count[i]==0 && mk.count[i]>0) return false;
                    } 
                    return true;
                }
                
            }
        }
        return false;
    }
    
    public boolean includeO2O(Object other) { // two first structures of this object are included in other
        if (structures.length==0) return false;
        if (other instanceof MeasurementKey) {
            MeasurementKey mk = (MeasurementKey)other;
            if (type==mk.type) {
                if (mk.count==null) mk.computeCount();
                int s2 = structures.length>1? structures[1] : structures[0];
                if (mk.count.length>this.structures[0] && mk.count.length>=s2) {
                    return mk.count[this.structures[0]]>0 && mk.count[s2]>0;
                }
            }
        }
        return false;
    }
    
    public boolean invertedOrder(int s1, int s2) {
        int idx1=-1;
        int idx2=-1;
        for (int i = 0; i<structures.length; i++) {
            if (s1==structures[i]) idx1=i;
            if (s2==structures[i]) idx2=i;
        }
        if (idx1==-1 || idx2==-1) return false;
        if (idx1<=idx2) return false;
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 79 * hash + Arrays.hashCode(this.structures);
        hash = 79 * hash + this.type;
        return hash;
    }
    
    @Override
    public String toString() {
        return "Structures: "+arrayToString(this.structures)+ " type:"+type;
    }
    
    public static String arrayToString(int[] array) {
        if (array==null || array.length==0) return "[ ]";
        StringBuilder s = new StringBuilder(3*array.length+2);
        s.append("[ ");
        s.append(array[0]);
        for (int i = 1; i<array.length; i++) {
            s.append(", ");
            s.append(array[i]);
        }
        s.append("]");
        return s.toString();
    }
    
    public static String arrayToString(ArrayList<Integer> array) {
        if (array==null || array.isEmpty()) return "[ ]";
        StringBuilder s = new StringBuilder(3*array.size()+2);
        s.append("[ ");
        s.append(array.get(0));
        for (int i = 1; i<array.size(); i++) {
            s.append(", ");
            s.append(array.get(i));
        }
        s.append("]");
        return s.toString();
    }
    
    public static String arrayToString(double[] array) {
        if (array==null || array.length==0) return "[ ]";
        StringBuilder s = new StringBuilder(3*array.length+2);
        s.append("[ ");
        s.append(array[0]);
        for (int i = 1; i<array.length; i++) {
            s.append(", ");
            s.append(array[i]);
        }
        s.append("]");
        return s.toString();
    }
    @Override
    public int compareTo(MeasurementKey o) {
        if (this.type<type) return -1;
        else if (this.type>type) return 1;
        else return compareStructures(this.structures, o.structures);
    }
    
    public static int compareStructures(int[] s1, int[] s2) {
        for (int i = 0; i<Math.min(s1.length, s2.length); i++) {
            if (s1[i]<s2[i]) return -1;
            else if (s1[i]>s2[i]) return 1;
        }
        if (s1.length<s2.length) return -1;
        else if (s1.length>s2.length) return 1;
        else return 0;
    }
}
