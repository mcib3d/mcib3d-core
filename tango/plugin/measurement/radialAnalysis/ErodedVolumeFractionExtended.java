package tango.plugin.measurement.radialAnalysis;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import mcib3d.geom.Object3D;
import mcib3d.geom.Object3DVoxels;
import mcib3d.geom.Voxel3D;
import mcib3d.image3d.ImageFloat;
import mcib3d.image3d.ImageHandler;
import tango.dataStructure.InputCellImages;
import tango.dataStructure.ObjectQuantifications;
import tango.dataStructure.SegmentedCellImages;
import tango.parameter.*;
import tango.plugin.measurement.MeasurementObject;

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

public class ErodedVolumeFractionExtended implements MeasurementObject {

    StructureParameter structure = new StructureParameter("Structure:", "structure", -1, true);
    DistanceMapParameter dm = new DistanceMapParameter("ReferenceStructure", "refStructure", true, true);
    
    Parameter[] parameters = new Parameter[]{structure, dm}; 
    KeyParameterObjectNumber key = new KeyParameterObjectNumber("Eroded Volume Fraction", "evf", "evf", true);
    KeyParameterObjectNumber keyMin = new KeyParameterObjectNumber("Minimal Eroded Volume Fraction", "evfMin", "evfMin", false);
    KeyParameterObjectNumber keyMax = new KeyParameterObjectNumber("Maximal Eroded Volume Fraction", "evfMax", "evfMax", false);
    KeyParameterObjectNumber keyMean = new KeyParameterObjectNumber("Mean Eroded Volume Fraction", "evfMean", "evfMean", false);
    KeyParameterObjectNumber keyMedian = new KeyParameterObjectNumber("Median Eroded Volume Fraction", "evfMedian", "evfMedian", false);
    GroupKeyParameter group = new GroupKeyParameter("", "evfGroup", "", true, new KeyParameter[]{key, keyMin, keyMax, keyMean, keyMedian}, false);
    Parameter[] keys = new Parameter[]{group};
    int nCPUs=1;
    boolean verbose;
    
    public ErodedVolumeFractionExtended() {
        key.setHelp("Eroded volume fraction at the center of the object", true);
        keyMin.setHelp("Minimal Eroded volume fraction within the object", true);
        keyMax.setHelp("Maximal Eroded volume fraction within the object", true);
        keyMean.setHelp("Mean Eroded volume fraction within the object", true);
        keyMean.setHelp("Median Eroded volume fraction within the object", true);
    }
    
    @Override
    public int getStructure() {
        return structure.getIndex();
    }
    
    private float[] getDistanceDistribution(ImageFloat dm, Object3DVoxels o) {
        float[] res = new float[o.getVolumePixels()];
        ArrayList<Voxel3D> al = o.getVoxels();
        int i=0;
        for (Voxel3D v : al) res[i++] = dm.pixels[v.getRoundZ()][v.getXYCoord(dm.sizeX)];
        Arrays.sort(res);
        return res;
    }

    @Override
    public void getMeasure(InputCellImages rawImages, SegmentedCellImages segmentedImages, ObjectQuantifications quantifications) {
        ImageFloat distanceMap = null;
        if (!dm.isErodeNucleus()) distanceMap = (ImageFloat)dm.getMaskAndDistanceMap(rawImages, segmentedImages, 0, verbose, nCPUs)[1];
        
        Object3DVoxels[] objects = segmentedImages.getObjects(structure.getIndex());
        double[] r=null, min=null, max=null, mean=null, med=null;
        if (key.isSelected()) r = new double[objects.length];
        if (keyMin.isSelected() || keyMax.isSelected() || keyMean.isSelected() || keyMedian.isSelected()) {
            min = new double[objects.length];
            max = new double[objects.length];
            mean = new double[objects.length];
            med = new double[objects.length];
        }
        for (int i = 0; i<objects.length; i++) {
            
            if (dm.isErodeNucleus()) {
                float dist = (float)objects[i].getDistCenterMean();
                distanceMap = (ImageFloat)dm.getMaskAndDistanceMap(rawImages, segmentedImages, dist, verbose, nCPUs)[1];
            }
            
            if (key.isSelected()) r[i]=distanceMap.getPixelInterpolated(objects[i].getCenterAsPoint());
            
            if (keyMin.isSelected() || keyMax.isSelected() || keyMean.isSelected() || keyMedian.isSelected()) {
                float[] evf = getDistanceDistribution(distanceMap, objects[i]);
                if (evf.length==0) {
                    min[i]=-1;
                    max[i]=-1;
                    mean[i]=-1;
                    med[i]=-1;
                } else {
                    min[i]=evf[0];
                    max[i]=evf[evf.length-1];
                    if ((evf.length-1)%2==0) med[i]=evf[((evf.length-1)/2)];
                    else med[i] = (evf[(int)((double)(evf.length-1)/2d)] + evf[(int)((double)(evf.length-1)/2d)+1])/2d;
                    double m = 0; 
                    for (float f : evf) m+=f;
                    if (evf.length>0) m/=evf.length;
                    mean[i]=m;
                }
            }
        }
        
        if (key.isSelected()) quantifications.setQuantificationObjectNumber(key, r);
        if (keyMean.isSelected()) quantifications.setQuantificationObjectNumber(keyMean, mean);
        if (keyMin.isSelected()) quantifications.setQuantificationObjectNumber(keyMin, min);
        if (keyMax.isSelected()) quantifications.setQuantificationObjectNumber(keyMax, max);
        if (keyMedian.isSelected()) quantifications.setQuantificationObjectNumber(keyMedian, med);
        
    }

    @Override
    public Parameter[] getKeys() {
        return keys;
    }

    @Override
    public Parameter[] getParameters() {
        return parameters;
    }

    @Override
    public String getHelp() {
        return "Eroded Volume Fraction, similar to a shell analysis but continuous, as described in Ballester, M., Kress, C., Hue-Beauvais, C., Kiêu, K., Lehmann, G., Adenot, P., & Devinoy, E. (2008). The nuclear localization of WAP and CSN genes is modified by lactogenic hormones in HC11 cells. Journal of cellular biochemistry, 105(1), 262–70. doi:10.1002/jcb.21823. The EVF of a point within the nucleus is defined as the fraction of nuclear volume lying between a considered point and the nuclear membrane. The EVF rises from 0 at the nuclear periphery to 1 at the nuclear center. The EVF of points uniformly distributed within a nucleus is uniformly distributed between 0 and 1. This property holds for nuclei of any size and shape. It should be noted that the EVF changes more rapidly near the nuclear periphery than in the nuclear center. For instance, in a spherical nucleus with a radius of 5 mm, a point with an EVF equal to 0.5 lies only about 1 mm from the nuclear membrane. Standard erosion analyses [Parada et al., 2004a] were based on a discretized version of the EVF. EVFs were computed based on a Euclidean distance transform. This implementation also allows to compute EVF from other segmented structure. It also provides a normalization with  nuclear intensity instead of volume.";
        
        //return "Normalized distance between objects center and selected structure. Normalization : (volume <= distance) / total volume.  Similar to a shell analysis (but continuous)";
    }

    @Override
    public void setVerbose(boolean verbose) {
        this.verbose= verbose;
    }

    @Override
    public void setMultithread(int nCPUs) {
        this.nCPUs=nCPUs;
    }

}
