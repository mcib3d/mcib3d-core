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

public class InterfaceVoxSet extends Interface {
    HashSet<Vox3D> r1Voxels;
    HashSet<Vox3D> r2Voxels;
    
    public InterfaceVoxSet(Region r1, Region r2, InterfaceCollection col) {
        super(r1, r2, col);
        r1Voxels = new HashSet<Vox3D>();
        r2Voxels = new HashSet<Vox3D>();
    }
    
    @Override 
    public void mergeInterface(Interface other) {
        if (this.r1.label==other.r1.label || this.r2.label==other.r2.label) {
            r1Voxels.addAll(((InterfaceVoxSet)other).r1Voxels);
            r2Voxels.addAll(((InterfaceVoxSet)other).r2Voxels);
        } else {
            r1Voxels.addAll(((InterfaceVoxSet)other).r2Voxels);
            r2Voxels.addAll(((InterfaceVoxSet)other).r1Voxels);
        }
        computeStrength();
    }
    
    @Override
    public void computeStrength() {
        strength=0;
        if (col.method ==0) {
            strength=getSum();
            double size =(double)(r1Voxels.size()+r2Voxels.size()); 
            if (size>0) strength/=size;
        } else if (col.method==1) {
            double sum=0;
            double sum2 = 0;
            double size2=0;
            for (Interface i : r2.interfaces) {
                double sumtemp = ((InterfaceVoxSet)i).getSum();
                sum2+=sumtemp;
                size2+=((InterfaceVoxSet)i).getSize();
                if (i.equals(this)) {
                    sum = sumtemp / (double)((InterfaceVoxSet)i).getSize();
                }
            }
            if (size2>0) sum2/=size2;
            if (r1.label!=0) {
                double sum1 = 0;
                double size1=0;
                for (Interface i : r1.interfaces) {
                    sum1+=((InterfaceVoxSet)i).getSum();
                    size1+=((InterfaceVoxSet)i).getSize();
                }
                if (size1>0) sum1/=size1;
                if (sum1>=sum2 && sum1!=0) {
                    strength = sum/sum1;
                    return;
                }
            }
            if (sum2!=0) strength = sum/sum2;
        }
    }
    
    protected double getSum() {
        double sum = 0;
        for (Vox3D v : r1Voxels) sum+=v.value;
        for (Vox3D v : r2Voxels) sum+=v.value;
        return sum;
    }
    
    protected int getSize() {
        return r1Voxels.size()+r2Voxels.size();
    }
    
    @Override
    public boolean checkFusionCriteria() {
        return strength<col.strengthLimit;
    }
    
    public void addPair(Vox3D v1, Vox3D v2) {
        r1Voxels.add(v1);
        r2Voxels.add(v2);
    }
    
}
