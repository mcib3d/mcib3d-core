package tango.plugin.filter.mergeRegions;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import mcib3d.geom.Object3DVoxels;
import mcib3d.geom.Voxel3D;
import mcib3d.image3d.ImageHandler;
import mcib3d.image3d.ImageInt;
import tango.gui.Core;

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

public class Region {
        HashSet<Vox3D> voxels;
        ArrayList<Interface> interfaces;
        int label;
        RegionCollection col;
        public Region(int label, Vox3D vox, RegionCollection col) {
            this.label=label;
            this.voxels=new HashSet<Vox3D>();
            if (vox!=null) voxels.add(vox);
            this.col=col;
        }
        
        public void setVoxelIntensity(ImageHandler intensityMap) {
            for (Vox3D v : voxels) v.value=intensityMap.getPixel(v.xy, v.z);
        }
        
        public void setLabel(int label) {
            col.regions.remove(this.label);
            this.label=label;
            setVoxelLabel(label);
            col.regions.put(label, this);
        }
        
        public void setVoxelLabel(int l) {
            for (Vox3D v : voxels) col.labelMap.setPixel(v.xy, v.z, l);
        }

        public Region fusion(Region region) {
            if (region.label<label) return region.fusion(this);
            if (col.verbose) ij.IJ.log("fusion:"+label+ "+"+region.label);
            region.setVoxelLabel(label);
            this.voxels.addAll(region.voxels);
            //if (this.interactants!=null) interactants.addAll(region.interactants);
            //spots.remove(region.label);
            return region;
        }
        
        public double getArea() {
            ImageCalibrations cal = col.cal;
            ImageInt inputLabels = col.labelMap;
            double area=0;
            for (Vox3D vox: voxels) {
                int x = vox.xy%cal.sizeX;
                int y = vox.xy/cal.sizeX;
                if (x<cal.limX && (inputLabels.getPixelInt(vox.xy+1, vox.z))!=label) {
                    area+=cal.aXZ;
                }
                if (x>0 && (inputLabels.getPixelInt(vox.xy-1, vox.z))!=label) {
                    area+=cal.aXZ;
                }
                if (y<cal.limY && (inputLabels.getPixelInt(vox.xy+cal.sizeX, vox.z))!=label) {
                    area+=cal.aXZ;
                }
                if (y>0 && (inputLabels.getPixelInt(vox.xy-cal.sizeX, vox.z))!=label) {
                    area+=cal.aXZ;
                }
                if (vox.z<cal.limZ && (inputLabels.getPixelInt(vox.xy, vox.z+1))!=label) {
                    area+=cal.aXY;
                }
                if (vox.z>0 && (inputLabels.getPixelInt(vox.xy, vox.z-1))!=label) {
                    area+=cal.aXY;
                }
            }
            return area;
        }
        
        public boolean hasNoInteractant() {
            return interfaces==null || interfaces.isEmpty() || (interfaces.size()==1 && interfaces.get(0).r1.label==0);
        }
        
        public Object3DVoxels toObject3D() {
            ArrayList<Voxel3D> al = new ArrayList<Voxel3D>(voxels.size());
            for (Vox3D v : voxels) al.add(v.toVoxel3D(label, col.cal.sizeX));
            return new Object3DVoxels(al);
        }
        
        @Override 
        public boolean equals(Object o) {
            if (o instanceof Region) {
                return ((Region)o).label==label;
            } else if (o instanceof Integer) return label==(Integer)o;
            else return false;
        }

        @Override
        public int hashCode() {
            int hash = 3;
            hash = 71 * hash + this.label;
            return hash;
        }
        
        @Override 
        public String toString() {
            return "Region:"+label;
        }
    
}
