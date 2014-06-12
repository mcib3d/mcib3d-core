package tango.plugin.segmenter.legacy;

import tango.plugin.segmenter.legacy.SeededWatershed3D_;
import ij.gui.Plot;
import ij.measure.CurveFitter;
import java.util.ArrayList;
import java.util.Collections;
import mcib3d.utils.ArrayUtil;
import tango.dataStructure.InputCellImages;
import tango.dataStructure.InputImages;
import tango.parameter.SliderDoubleParameter;

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
public class SpotSeededWatershed3D extends SeededWatershed3D_ {
    
    public SpotSeededWatershed3D() {
        super();
    }
    
    @Override
    public String getHelp() {
        return "Seeded Watershed 3D using max hessian eigen value transform as a propagation map. Constraints on seeds (applied before propagation): local extrema, constraint on max hessian eigen value (spotiness), constraint on intensity level. Runs until the background threshold. This plugin is under developpement, and is subject to changes in the next versions of TANGO";
    }
    
    @Override
    protected void postInit(InputImages images) {
        hessianScale=Math.max(1, hessianScale);
        hessian=input.getHessian((float)hessianScale, nCPUs)[0];
        this.hessianThld=this.hessianThld_P.getThreshold(hessian, images, nCPUs, debug);
        if (debug) hessian.showDuplicate("watershed map");
        sign=-1;
        this.watershedMap=this.hessian;
    }
    
     @Override
     protected boolean isSeed(int[][] neigh, int x, int y, int z, float max) {
        int zz, yy, xx;
        for (int i = 0; i<neigh[0].length; i++) {
            zz = z+neigh[2][i];
            if (zz>0 && zz<input.sizeZ) {
                yy = y+neigh[1][i];
                if (yy>0 && yy<input.sizeY) {
                    xx = x+neigh[0][i];
                    if (xx>0 && xx<sizeX) {
                        int xxyy=xx+yy*sizeX;
                        if ((mask==null || mask.getPixel(xxyy, zz)!=0) && this.watershedMap.getPixel(xxyy, zz)<max) return false;
                    }
                }
            }
        }
        return true;
    }
    
    @Override
    protected void addInteraction(Spot3D s1, Spot3D s2) {
        Interaction i = new Interaction(s1, s2, 0);
        if (!interactions.contains(i)) interactions.add(i);
    }
      
    protected class Interaction extends SeededWatershed3D_.AbstractInteraction {
        
        public Interaction(Spot3D s1, Spot3D s2, float v) {
            super(s1, s2, v);
        }
        /*@Override
        protected void computeInteraction() {
            ArrayList<Float> inter = new ArrayList<Float>();
            if (s1==null || s2==null) {
                v=Float.NEGATIVE_INFINITY;
                return;
            } else v=0;
            Spot3D min = (s1.voxels.size()<s2.voxels.size())?s1:s2;
            int otherLabel = (min==s1)?s2.label:s1.label;
            for (Vox3D vox: min.voxels) {
                int x = vox.xy%sizeX;
                int y = vox.xy/sizeX;
                if (x<limX && (segMap[vox.z][vox.xy+1])==otherLabel) {
                    inter.add((watershedMap.getPixel(vox.xy+1, vox.z)*sign+vox.value)/2);
                }
                if (x>0 && (segMap[vox.z][vox.xy-1])==otherLabel) {
                    inter.add((watershedMap.getPixel(vox.xy-1, vox.z)*sign+vox.value)/2);
                }
                if (y<limY && (segMap[vox.z][vox.xy+sizeX])==otherLabel) {
                    inter.add((watershedMap.getPixel(vox.xy+sizeX, vox.z)*sign+vox.value)/2);
                }
                if (y>0 && (segMap[vox.z][vox.xy-sizeX])==otherLabel) {
                    inter.add((watershedMap.getPixel(vox.xy-sizeX, vox.z)*sign+vox.value)/2);
                }
                if (vox.z<limZ && (segMap[vox.z+1][vox.xy])==otherLabel) {
                    inter.add((watershedMap.getPixel(vox.xy, vox.z+1)*sign+vox.value)/2);
                }
                if (vox.z>0 && (segMap[vox.z-1][vox.xy-1])==otherLabel) {
                    inter.add((watershedMap.getPixel(vox.xy, vox.z-1)*sign+vox.value)/2);
                }
                
            }
            if (inter.isEmpty()) v=Float.NEGATIVE_INFINITY;
            else {
                Collections.sort(inter);
                v=inter.get(inter.size()/2);
            }
        }
        
        @Override
        public boolean checkFusionCriteria() {
            if (s1.voxels.isEmpty() || s2.voxels.isEmpty()) return true;

            double mean = 0;
            ArrayList<Vox3D> t1=new ArrayList<Vox3D> (s1.voxels);
            Collections.sort(t1);
            Vox3D vox = t1.get(t1.size()/2);
            mean+=watershedMap.getPixel(vox.xy, vox.z)*sign;
            t1=new ArrayList<Vox3D> (s2.voxels);
            Collections.sort(t1);
            vox = t1.get(t1.size()/2);
            mean+=watershedMap.getPixel(vox.xy, vox.z)*sign;
            mean/=2;
            if (debug) System.out.println("fusion_P: "+s1.label+"+"+s2.label+" mean intensity:"+mean+ " value:"+v +" test:"+((v>0 && mean>0)?(mean*(1-fusionCoeff)+"<"+v):v*(1-fusionCoeff)+">"+mean));
            if (v==mean && v==0) return true;
            if (v<0 && mean>0) return false;
            if (v>0 && mean<0) return true;
            
            if (v>0 && mean>0) return mean*(1-fusionCoeff)<v;
            else return v*(1-fusionCoeff)>mean;
        }
        * 
        */
        
    }
}
