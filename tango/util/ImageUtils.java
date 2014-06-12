package tango.util;

import i5d.Image5D;
import i5d.cal.ChannelDisplayProperties;
import i5d.gui.ChannelControl;
import ij.ImagePlus;
import ij.gui.ImageCanvas;
import java.awt.*;
import java.awt.event.AdjustmentListener;
import java.awt.event.KeyListener;
import java.awt.event.MouseWheelListener;
import java.awt.image.ColorModel;
import mcib3d.image3d.ImageHandler;
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
public class ImageUtils {
    
    public static void zoom(ImagePlus image, double magnitude) {
        ImageCanvas ic = image.getCanvas();
        if (ic==null) return;
        ic.zoom100Percent();
        if (magnitude>1) {
            for (int i = 0; i<(int)(magnitude+0.5); i++) {
                ic.zoomIn(image.getWidth()/2, image.getHeight()/2);
            }
        } else if (magnitude>0 && magnitude<1) {
            for (int i =0; i<(int)(1/magnitude+0.5); i++) {
                ic.zoomOut(image.getWidth()/2, image.getHeight()/2);
            }
        }
    }
    
    public static Image5D getImage5D(String title, ImageHandler[] images) {
        ImagePlus hyperstack = ImageHandler.getHyperStack(title, images);
        Image5D res = new Image5D(hyperstack.getShortTitle(), hyperstack.getStack(), images.length, images[0].sizeZ, 1);
        for (int i = 0; i<images.length; i++) {
            res.setChannelMinMax(i+1, images[i].getMin(null), images[i].getMax(null));
            //res.setChannelCalibration(i+1, new ChannelCalibration(img.getCalibration()));
            
            res.setDefaultChannelNames();
        }
        
        for (int i = 0; i<images.length; i++) {
            Color c = tango.gui.util.Colors.colors.get(tango.gui.util.Colors.colorNames[i+1]);
            ColorModel cm = ChannelDisplayProperties.createModelFromColor(c);
            res.setChannelColorModel(i+1, cm);
        }
        res.setDisplayMode(ChannelControl.OVERLAY);
        return res;
    }
    
    public static void addScrollListener(ImagePlus img, AdjustmentListener al, MouseWheelListener ml) {
        //from Fiji code
        // TODO Find author...
        for (Component c : img.getWindow().getComponents()) {
            if (c instanceof Scrollbar) ((Scrollbar)c).addAdjustmentListener(al);
            else if (c instanceof Container) {
                for (Component c2 : ((Container)c).getComponents()) {
                    if (c2 instanceof Scrollbar) ((Scrollbar)c2).addAdjustmentListener(al);
                }
            }
        }
        img.getWindow().addMouseWheelListener(ml);
    }
    
    public static void removeScrollListener(ImagePlus img, AdjustmentListener al, MouseWheelListener ml) {
        //from Fiji code
        // TODO Find author...
        for (Component c : img.getWindow().getComponents()) {
            if (c instanceof Scrollbar) ((Scrollbar)c).removeAdjustmentListener(al);
            else if (c instanceof Container) {
                for (Component c2 : ((Container)c).getComponents()) {
                    if (c2 instanceof Scrollbar) ((Scrollbar)c2).removeAdjustmentListener(al);
                }
            }
        }
        img.getWindow().removeMouseWheelListener(ml);
    }
    
    public static void overrideKeyListeners(ImagePlus img, KeyListener kl) {
        Canvas rc = img.getWindow().getCanvas();
        KeyListener[] kls = rc.getKeyListeners();
        for (KeyListener kll : kls) rc.removeKeyListener(kll);
        rc.addKeyListener(kl);
    }
    
    public static int[][] getNeigh(float radius, float radiusZ) {
        float r = (float) radius / radiusZ;
        int rad = (int) (radius + 0.5f);
        int radZ = (int) (radiusZ + 0.5f);
        int[][] temp = new int[3][(2 * rad + 1) * (2 * rad + 1) * (2 * radZ + 1)];
        //float[] tempDist = new float[temp[0].length];
        int count = 0;
        float rad2 = radius * radius;
        for (int zz = -radZ; zz <= radZ; zz++) {
            for (int yy = -rad; yy <= rad; yy++) {
                for (int xx = -rad; xx <= rad; xx++) {
                    float d2 = zz * r * zz * r + yy * yy + xx * xx;
                    if (d2 <= rad2 && !((xx == 0) && (yy == 0) && (zz == 0))) {	//exclusion du point
                        temp[0][count] = xx;
                        temp[1][count] = yy;
                        temp[2][count] = zz;
                        //tempDist[count] = (float) Math.sqrt(d2);
                        count++;
                    }
                }
            }
        }

        //distances = new float[count];
        //System.arraycopy(tempDist, 0, distances, 0, count);

        /*
         * Integer[] order = new Integer[distances.length]; for (int i = 0; i <
         * order.length; i++) order[i]=i; Arrays.sort(order, new
         * ComparatorDistances()); Arrays.sort(distances); for (int i = 0;
         * i<count; i++) { vois[0][i]=temp[0][order[i]];
         * vois[1][i]=temp[1][order[i]]; vois[2][i]=temp[2][order[i]]; }
         *
         */
        int [][] res = new int[3][count];
        System.arraycopy(temp[0], 0, res[0], 0, count);
        System.arraycopy(temp[1], 0, res[1], 0, count);
        System.arraycopy(temp[2], 0, res[2], 0, count);
        return res;
    }
    
    public static int[][] getNeigh(float radius, float radiusZ, float thickness, boolean onlyPositive) {
        float r = (float) radius / radiusZ;
        int rad = (int) (radius + 0.5f);
        int radZ = (int) (radiusZ + 0.5f);
        int[][] temp = new int[3][(2 * rad + 1) * (2 * rad + 1) * (2 * radZ + 1)];
        //float[] tempDist = new float[temp[0].length];
        int count = 0;
        float rad2 = radius * radius;
        float radMin = (float)Math.pow(radius-thickness, 2);
        int startZ = onlyPositive?0:-radZ;
        int startXY = onlyPositive?0:-rad;
        for (int zz = startZ; zz <= radZ; zz++) {
            for (int yy = startXY; yy <= rad; yy++) {
                for (int xx = startXY; xx <= rad; xx++) {
                    float d2 = zz * r * zz * r + yy * yy + xx * xx;
                    if (d2 <= rad2 && d2>radMin && !((xx == 0) && (yy == 0) && (zz == 0))) {	//exclusion du point
                        temp[0][count] = xx;
                        temp[1][count] = yy;
                        temp[2][count] = zz;
                        //tempDist[count] = (float) Math.sqrt(d2);
                        count++;
                    }
                }
            }
        }

        //distances = new float[count];
        //System.arraycopy(tempDist, 0, distances, 0, count);

        /*
         * Integer[] order = new Integer[distances.length]; for (int i = 0; i <
         * order.length; i++) order[i]=i; Arrays.sort(order, new
         * ComparatorDistances()); Arrays.sort(distances); for (int i = 0;
         * i<count; i++) { vois[0][i]=temp[0][order[i]];
         * vois[1][i]=temp[1][order[i]]; vois[2][i]=temp[2][order[i]]; }
         *
         */
        int [][] res = new int[3][count];
        System.arraycopy(temp[0], 0, res[0], 0, count);
        System.arraycopy(temp[1], 0, res[1], 0, count);
        System.arraycopy(temp[2], 0, res[2], 0, count);
        //ij.IJ.log("Neigbor: Radius:"+radius+ " radiusZ"+radiusZ+ " count:"+count);
        return res;
    }
}
