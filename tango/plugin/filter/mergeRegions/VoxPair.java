package tango.plugin.filter.mergeRegions;

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

public class VoxPair { 
    Vox3D v1;
    Vox3D v2;
    public VoxPair(Vox3D v1, Vox3D v2) {
        if (v1.z<v2.z) {
            this.v1=v1;
            this.v2=v2;
        } else if (v1.z>v2.z) {
            this.v2=v1;
            this.v1=v2;
        } else {
            if (v1.xy<=v2.xy) {
                this.v1=v1;
                this.v2=v2;
            } else {
                this.v2=v1;
                this.v1=v2;
            }
        }
    }
    
    public double getSurf(ImageCalibrations cal) {
        if (v1.z!=v2.z) return cal.aXZ;
        else return cal.aXY;
    }
    
    @Override
    public boolean equals(Object o) {
        if (o instanceof VoxPair) {
            return ((VoxPair)o).v1==v1 && ((VoxPair)o).v2==v2;
        }
        return false;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 59 * hash + (this.v1 != null ? this.v1.hashCode() : 0);
        hash = 59 * hash + (this.v2 != null ? this.v2.hashCode() : 0);
        return hash;
    }
}
