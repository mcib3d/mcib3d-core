package tango.plugin.measurement.radialAnalysis;

import ij.gui.Plot;
import java.util.Arrays;
import mcib3d.geom.Object3D;
import mcib3d.geom.Point3D;
import mcib3d.geom.Voxel3D;
import mcib3d.image3d.ImageByte;
import mcib3d.image3d.ImageFloat;
import mcib3d.image3d.ImageHandler;
import mcib3d.image3d.ImageInt;
import mcib3d.image3d.distanceMap3d.EDT;
import tango.spatialStatistics.StochasticProcess.RandomPoint3DGeneratorUniform;

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

public class ShellAnalysisCore {
    Vox3D[] sortedVoxels;
    ImageFloat distanceMap;
    boolean verbose;
    public ShellAnalysisCore(ImageInt nucleusMask, ImageInt segmentedStructure, boolean inside, int nbCPUs, boolean verbose) {
        int count=0;
        this.verbose=verbose;
        distanceMap=EDT.run(segmentedStructure, 0f, (float)nucleusMask.getScaleXY(), (float)nucleusMask.getScaleZ(), !inside, nbCPUs);
        if (verbose) distanceMap.showDuplicate("radial profile shell: distance map");
        for (int z = 0; z<segmentedStructure.sizeZ; z++) {
            for (int xy=0; xy<segmentedStructure.sizeXY; xy++) {
                if (nucleusMask.getPixelInt(xy, z) !=0 && ((inside && segmentedStructure.getPixelInt(xy, z)!=0) || (!inside && segmentedStructure.getPixelInt(xy, z)==0)) ) {
                    count++;
                }
            }
        }
        sortedVoxels  = new Vox3D[count];
        count=0;
        for (int z = 0; z<segmentedStructure.sizeZ; z++) {
            for (int xy = 0; xy<segmentedStructure.sizeXY; xy++) {
                if (nucleusMask.getPixelInt(xy, z) !=0 && ((inside && segmentedStructure.getPixelInt(xy, z)!=0) || (!inside && segmentedStructure.getPixelInt(xy, z)==0)) ) {
                    sortedVoxels[count] = new Vox3D(xy, z, distanceMap.getPixel(xy, z));
                    count++;
                }
                
            }
        }
        Arrays.sort(sortedVoxels);
        //if (verbose) System.out.println("radial profile distances: min:"+sortedVoxels[0].value+ " max:"+sortedVoxels[sortedVoxels.length-1].value);
    }
    
    public double[] getAllX() {
        double[] x = new double[sortedVoxels.length];
        for (int i = 0; i<sortedVoxels.length; i++) x[i]=sortedVoxels[i].value;
        return x;    
    }
    
    /*public double[] getCumulativeProfile(ImageHandler intensity) {
        double[] values = new double[sortedVoxels.length];
        for (int i = 0; i<sortedVoxels.length; i++) {
            values[i]=(i>0?values[i-1]:0) + intensity.getPixelInterpolated(sortedVoxels[sortedVoxels.length-1-i]);
        }
        double sum = values[values.length-1];
        for (int i = 0; i<sortedVoxels.length; i++) values[i]/=sum;
        return values;
    }
    * 
    */
    
    protected float[] getIntensities(ImageHandler intensity) {
        float[] intensities = new float[sortedVoxels.length];
        for (int i = 0; i<sortedVoxels.length; i++) intensities[i]=intensity.getPixel(sortedVoxels[i].xy, sortedVoxels[i].z);
        for (int i = 0; i<(sortedVoxels.length-1); i++) { // gestion des duplicates : mean value
            if (sortedVoxels[i].value==sortedVoxels[i+1].value) {
                int j = i+2;
                float sum = intensities[i]+intensities[i+1];
                while (j<sortedVoxels.length && sortedVoxels[i].value==sortedVoxels[j].value) sum+=intensities[j++];
                sum/=(float)(j-i);
                for (int k = i; k<j; k++) intensities[k]=sum;
            }
        }
        return intensities;
    }
    
    public double getShell(Object3D object, int[] shellIndexes) {
        double distance = distanceMap.getPixelInterpolated(object.getCenterAsPoint());
        for (int i = 0; i<shellIndexes.length;i++) {
            if (distance<=sortedVoxels[shellIndexes[i]-1].value) return (double)(i+1)/(double)(shellIndexes.length);
        }
        return 1;
    }
    
    public double[] getShells(Object3D[] objects, int[] shellIndexes) {
        double[] shells = new double[objects.length];
        for (int i = 0; i<objects.length; i++) {
            shells[i]=getShell(objects[i], shellIndexes);
        }
        return shells;
    }
    
    public ImageInt getShellMap(int[] shellIndexes) {
        ImageByte shellMap = new ImageByte("Shell Map", distanceMap.sizeX, distanceMap.sizeY, distanceMap.sizeZ);
        int currentShell=0;
        for (int i =0;i<sortedVoxels.length; i++) {
            if (i>shellIndexes[currentShell]) currentShell++;
            shellMap.setPixel(sortedVoxels[i].xy, sortedVoxels[i].z, currentShell+1);
        }
        return shellMap;
    }
    
    public double[] getShellRepartitionMask(ImageInt objects, int[] shellIndexes) {
        double[] shells = new double[shellIndexes.length];
        int currentShell=0;
        for (int i =0;i<sortedVoxels.length; i++) {
            if (objects.getPixelInt(sortedVoxels[i].xy, sortedVoxels[i].z)!=0) {
                if (i>shellIndexes[currentShell]) currentShell++;
                shells[currentShell]++;
            }
        }
        //normalize
        double sum = 0;
        for (double d : shells) sum+=d;
        for (int i = 0;i<shells.length;i++) shells[i]/=sum;
        return shells;
    }
    
    public double[] getShellRepartition(ImageHandler objects, int[] shellIndexes) {
        double[] shells = new double[shellIndexes.length];
        int currentShell=0;
        for (int i =0;i<sortedVoxels.length; i++) {
            float v = objects.getPixel(sortedVoxels[i].xy, sortedVoxels[i].z);
            if (i>shellIndexes[currentShell]) currentShell++;
            shells[currentShell]+=v;
        }
        //normalize
        double sum = 0;
        for (double d : shells) sum+=d;
        for (int i = 0;i<shells.length;i++) shells[i]/=sum;
        return shells;
    }
    
    public int[] getShellIndexes(int nbShells) {
        double shellSize = (double)sortedVoxels.length/(double)nbShells;
        double idx=0;
        int[] indexes = new int[nbShells];
        for (int i = 0; i<nbShells; i++) {
            idx+=shellSize;
            indexes[i]=(int)(idx+0.5);
            if (verbose) System.out.println("shell indexes:"+i+ " start:"+(i==0?0:indexes[i-1])+ " stop:"+(indexes[i]-1)+ " total:"+sortedVoxels.length+ " distance:"+sortedVoxels[indexes[i]-1].value);
        }
        return indexes;
    }
    
    public int[] getShellIndexesNormalized(int nbShells, ImageHandler intensity) {
        float[] norm = getIntensities(intensity);
        double sum = 0;
        for (float f : norm) sum+=f;
        double shellSize = sum/(double)nbShells;
        int idx=0;
        int[] indexes = new int[nbShells];
        double cumSum;
        for (int i = 0; i<nbShells; i++) {
            cumSum=norm[idx];
            while(cumSum<shellSize && idx<(norm.length-1)) cumSum+=norm[++idx];
            indexes[i]=++idx;
            if (verbose) System.out.println("shell indexes:"+i+ " start:"+(i==0?0:indexes[i-1])+ " stop:"+(indexes[i]-1)+ " total:"+sortedVoxels.length);
        }
        return indexes;
    }
    
    public double[] getProfile(ImageHandler intensity, int[] shellIndexes) {
        float[] intensities=getIntensities(intensity);
        double[] y = new double[shellIndexes.length];
        for (int i = 0; i<shellIndexes.length; i++) {
            int start = i==0?0:shellIndexes[i-1];
            for (int ii = start; ii<shellIndexes[i]; ii++) y[i]+=intensities[ii];
            //y[i]/=(lastIdx-curIdx);
        }
        double sum = 0;
        for (double f : y) sum+=f;
        for (int i = 0; i<shellIndexes.length; i++) y[i]/=sum;
        return y;
    }
    
    protected float[] getIntensitiesOverThld(ImageHandler intensity, float thld) {
        float[] intensities = new float[sortedVoxels.length];
        for (int i = 0; i<sortedVoxels.length; i++) intensities[i]=intensity.getPixel(sortedVoxels[i].xy, sortedVoxels[i].z);
        for (int i = 0; i<(sortedVoxels.length-1); i++) { // gestion des duplicates : mean value
            if (sortedVoxels[i].value==sortedVoxels[i+1].value) {
                int j = i;
                float sum = 0;
                int count=0;
                while (j<sortedVoxels.length && sortedVoxels[i].value==sortedVoxels[j].value) {
                    if (intensities[j]>thld) {
                        sum+=intensities[j];
                        count++;
                    }
                    j++;
                }
                if (count>0) sum/=(float)count;
                for (int k = i; k<j; k++) intensities[k]=sum;
            }
        }
        return intensities;
    }
    
    public double[] getProfileOverThld(ImageHandler intensity, int[] shellIndexes, double thld) {
        float[] intensities=getIntensitiesOverThld(intensity, (float)thld);
        double[] y = new double[shellIndexes.length];
        double sum = 0;
        for (float f : intensities) if (f>thld) sum+=f;
        for (int i = 0; i<shellIndexes.length; i++) {
            int start = i==0?0:shellIndexes[i-1];
            for (int ii = start; ii<shellIndexes[i]; ii++) {
                if (intensities[ii]>thld) {
                    y[i]+=intensities[ii];
                }
            }
            if (sum>0) y[i]/=sum;
        }
        return y;
    }
    
    public double[] getBreaks(int[] shellIndexes) {
        double[] x = new double[shellIndexes.length];
        for (int i = 0; i<shellIndexes.length; i++) {
            x[i]=sortedVoxels[shellIndexes[i]-1].value; // FIXME en 0??
            if (verbose) System.out.println("shell breaks:"+i+ " start:"+(i==0?sortedVoxels[0].value:x[i-1])+ " stop:"+x[i]);
        }
        return x;
    }
    
    // light voxel class
    protected class Vox3D implements java.lang.Comparable<Vox3D> {
        public int xy, z;
        public float value;

        public Vox3D(int xy, int z, float value) {
            this.xy=xy;
            this.z=z;
            this.value=value;
        }

        @Override
        public int compareTo(Vox3D v) {
            if (v.xy==xy && v.z==z) return 0;
            else if(value < v.value) return -1;
            else if(value > v.value) return 1;
            else return 0;
        }
        
        @Override 
        public boolean equals(Object o) {
            if (o instanceof Vox3D) {
                return xy==((Vox3D)o).xy && z==((Vox3D)o).z;
            } return false;
        }

        @Override
        public int hashCode() {
            int hash = 3;
            hash = 47 * hash + this.xy;
            hash = 47 * hash + this.z;
            return hash;
        }
        
    }
}
