package tango.plugin.filter;

import ij.gui.Plot;
import ij.measure.CurveFitter;
import ij.process.AutoThresholder;
import mcib3d.geom.Object3D;
import mcib3d.geom.Point3D;
import mcib3d.image3d.ImageFloat;
import mcib3d.image3d.ImageInt;
import mcib3d.utils.ArrayUtil;
import tango.dataStructure.InputImages;
import tango.parameter.IntParameter;
import tango.parameter.Parameter;
import tango.parameter.SliderDoubleParameter;
import tango.plugin.thresholder.AutoThreshold;
import tango.util.ImageUtils;

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
public class LocalThresholdAdjustment extends SpotLocalThresholder {
    
    IntParameter radius = new IntParameter("Radius for histogram computation", "maxRadius", 10);
    int[][] neigh;
    Parameter[] parameters = new Parameter[] {radius};
    
    
    public LocalThresholdAdjustment() {
        super();
    }
    
    @Override
    public ImageInt runPostFilter(int currentStructureIdx, ImageInt input, InputImages images) {
        initialize(input, filtered.isSelected()?images.getFilteredImage(currentStructureIdx):images.getImage(currentStructureIdx), images.getMask());
        run(false);
        return segMap;
    }
    
    @Override
    public void postInitialize() {
        double radXY = radius.getIntValue(10);
        double radZ = radXY * intensityMap.getScaleXY()/intensityMap.getScaleZ();
        neigh = ImageUtils.getNeigh((float)radXY, (float)radZ);
    }
     
    @Override
    public double getLocalThreshold(Object3D s) {
        if (debug) System.out.println("getting local thld : spot:"+s.getValue());
        if (s.getVoxels().isEmpty()) return Double.NaN;
        Point3D center = s.getCenterAsPoint();
        float[] pix = getNeigborhoodPixels(center.getRoundX(), center.getRoundY(), center.getRoundZ(), s.getValue());
        float[][] matrix = new float[1][];
        matrix[0]=pix;
        ImageFloat localIm = new ImageFloat(matrix, "title", pix.length);
        return AutoThreshold.run(localIm, null, AutoThresholder.Method.Otsu);
    }
    
    
    protected float[] getNeigborhoodPixels(int x, int y, int z, int label) {
        int zz, yy, xx;
        float[] temp = new float[neigh[0].length];
        int count = 0;
        for (int i = 0; i<neigh[0].length; i++) {
            zz = z+neigh[2][i];
            if (zz>=0 && zz<intensityMap.sizeZ) {
                yy = y+neigh[1][i];
                if (yy>=0 && yy<intensityMap.sizeY) {
                    xx = x+neigh[0][i];
                    if (xx>=0 && xx<sizeX) {
                        int xxyy=xx+yy*sizeX;
                        int l = segMap.getPixelInt(xxyy, zz);
                        if (mask.getPixel(xxyy, zz)!=0 && l==0 || l==label) {
                            temp[count++]=intensityMap.getPixel(xxyy, zz);
                        }
                    }
                }
            }
        }
        float[] res = new float[count];
        System.arraycopy(temp, 0, res, 0, count);
        return res;
    }
    
    @Override 
    protected Parameter[] getOtherParameters() {
        return parameters;
    }

    @Override
    public String getHelp() {
        return "";
    }
    
   
    
}
