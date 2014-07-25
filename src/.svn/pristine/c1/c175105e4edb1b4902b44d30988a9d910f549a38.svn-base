package mcib3d.image3d;

import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.Prefs;
import ij.gui.NewImage;
import ij.process.ByteProcessor;
import ij.process.ImageProcessor;
import ij.process.StackProcessor;
import ij.process.StackStatistics;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.TreeMap;
import mcib3d.geom.*;
import mcib3d.image3d.legacy.IntImage3D;

/**
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
 * @author Thomas Boudier
 */
public class ImageByte extends ImageInt {

    public byte[][] pixels;

    public ImageByte(ImagePlus img) {
        super(img);
        buildPixels();
    }

    public ImageByte(ImageStack stack) {
        super(stack);
        buildPixels();
    }

    private void buildPixels() {
        pixels = new byte[sizeZ][];
        if (img.getImageStack() != null) {
            for (int i = 0; i < sizeZ; i++) {
                pixels[i] = (byte[]) img.getImageStack().getPixels(i + 1);
            }
        } else {
            ImageStack st = new ImageStack(sizeX, sizeY);
            st.addSlice(img.getProcessor());
            pixels[0] = (byte[]) img.getProcessor().getPixels();
            this.img.setStack(null, st);
        }
    }

    public ImageByte(byte[][] pixels, String title, int sizeX) {
        super(title, sizeX, pixels[0].length / sizeX, pixels.length, 0, 0, 0);
        this.pixels = pixels;
        ImageStack st = new ImageStack(sizeX, sizeY, sizeZ);
        for (int z = 0; z < sizeZ; z++) {
            st.setPixels(pixels[z], z + 1);
        }
        img = new ImagePlus(title, st);
    }

    public ImageByte(String title, int sizeX, int sizeY, int sizeZ) {
        super(title, sizeX, sizeY, sizeZ);
        img = NewImage.createByteImage(title, sizeX, sizeY, sizeZ, 1);
        pixels = new byte[sizeZ][];
        for (int i = 0; i < sizeZ; i++) {
            pixels[i] = (byte[]) img.getImageStack().getPixels(i + 1);
        }
    }

    protected ImageByte(int sizeX, int sizeY, int sizeZ) { //blank mask:no pixels creation
        super("blank mask", sizeX, sizeY, sizeZ);
    }

    public ImageByte(ImageHandler im, boolean scaling) {
        super(im.title, im.sizeX, im.sizeY, im.sizeZ, im.offsetX, im.offsetY, im.offsetZ);
        ImageStats s = getImageStats(null);
        if (im instanceof ImageShort) {
            ImageByte b = ((ImageShort) im).convertToByte(scaling);
            pixels = b.pixels;
            img = b.img;
            s.setMinAndMax(0, 255);
        } else if (im instanceof ImageFloat) {
            if (im.img != null) {
                ImageByte b = ((ImageFloat) im).convertToByte(scaling);
                pixels = b.pixels;
                img = b.img;
                s.setMinAndMax(0, 255);
            }
        } else {
            this.img = im.img;
            this.pixels = ((ImageByte) im).pixels;
        }
    }
    

    public static byte[] getArray1DByte(ImagePlus img) {
        byte[] res = new byte[img.getNSlices() * img.getWidth() * img.getHeight()];
        int offZ = 0;
        int sizeXY = img.getWidth() * img.getHeight();
        for (int slice = 0; slice < img.getNSlices(); slice++) {
            System.arraycopy((byte[]) img.getImageStack().getPixels(slice + 1), 0, res, offZ, sizeXY);
            offZ += sizeXY;
        }
        return res;
    }

    public Object getArray1D() {
        byte[] res = new byte[sizeXYZ];
        int offZ = 0;
        for (int slice = 0; slice < img.getNSlices(); slice++) {
            System.arraycopy((byte[]) img.getImageStack().getPixels(slice + 1), 0, res, offZ, sizeXY);
            offZ += sizeXY;
        }
        return res;
    }
    
    public Object getArray1D(int z) {
        byte[] res = new byte[sizeXY];
            System.arraycopy((byte[]) img.getImageStack().getPixels(z + 1), 0, res, 0, sizeXY);
            
        return res;
    }
    
    

    public static ImagePlus getImagePlus(byte[] pixels, int sizeX, int sizeY, int sizeZ, boolean setMinAndMax) {
        if (pixels == null) {
            return null;
        }
        ImagePlus res = NewImage.createShortImage("", sizeX, sizeY, sizeZ, 1);
        int offZ = 0;
        int sizeXY = sizeX * sizeY;
        for (int z = 0; z < sizeZ; z++) {
            System.arraycopy(pixels, offZ, (byte[]) res.getImageStack().getPixels(z + 1), 0, sizeXY);
            offZ += sizeXY;
        }
        if (setMinAndMax) {
            int max = 0;
            int min = 0;
            for (int i = 0; i < pixels.length; i++) {
                if ((pixels[i] & 0xff) > max) {
                    max = pixels[i] & 0xff;
                }
                if ((pixels[i] & 0xff) < min) {
                    min = pixels[i] & 0xff;
                }
            }
            res.getProcessor().setMinAndMax(min, max);
        }
        return res;
    }
    /*
     * public static ImagePlus convert(ImagePlus img, boolean scaling) { double
     * max=0; double min=0; int sizeX=img.getWidth(); int sizeY=img.getHeight();
     * ImageStack is = img.getImageStack(); for (int z = 0; z<img.getNSlices();
     * z++) { for (int y = 0; y<sizeY; y++) { for (int x=0; x<sizeX; x++) { if
     * (is.getVoxel(x, y, z) >max) max = is.getVoxel(x, y, z); if
     * (is.getVoxel(x, y, z) <min) min = is.getVoxel(x, y, z); } } }
     * img.getProcessor().setMinAndMax(min, max); StackConverter conv = new
     * StackConverter (img); ImageConverter.setDoScaling(scaling);
     * conv.convertToGray8(); return img; }
     *
     */

    public ImageShort convertToShort(boolean scaling) {
        if (scaling) {
            setMinAndMax(null);
        }
        ImageStats s = getImageStats(null);
        int currentSlice = img.getCurrentSlice();
        ImageProcessor ip;
        ImageStack stack2 = new ImageStack(sizeX, sizeY);
        String label;
        ImageStack stack1 = img.getImageStack();
        for (int i = 1; i <= sizeZ; i++) {
            label = stack1.getSliceLabel(i);
            ip = stack1.getProcessor(i);
            if (scaling) {
                ip.setMinAndMax(s.getMin(), s.getMax());
            }
            stack2.addSlice(label, ip.convertToShort(scaling));
        }
        ImagePlus imp2 = new ImagePlus(img.getTitle(), stack2);
        imp2.setCalibration(img.getCalibration()); //update calibration
        imp2.setSlice(currentSlice);
        imp2.getProcessor().setMinAndMax(0, 255);
        return (ImageShort) ImageHandler.wrap(imp2);
    }

    public ImageFloat convertToFloat(boolean scaling) {
        if (scaling) {
            setMinAndMax(null);
        }
        ImageStats s = getImageStats(null);
        int currentSlice = img.getCurrentSlice();
        ImageProcessor ip;
        ImageStack stack2 = new ImageStack(sizeX, sizeY);
        String label;
        ImageStack stack1 = img.getImageStack();
        for (int i = 1; i <= sizeZ; i++) {
            label = stack1.getSliceLabel(i);
            ip = stack1.getProcessor(i);
            if (scaling) {
                ip.setMinAndMax(s.getMin(), s.getMax());
            }
            stack2.addSlice(label, ip.convertToFloat());
        }
        ImagePlus imp2 = new ImagePlus(img.getTitle(), stack2);
        imp2.setCalibration(img.getCalibration()); //update calibration
        imp2.setSlice(currentSlice);
        imp2.getProcessor().setMinAndMax(0, 255);
        return (ImageFloat) ImageHandler.wrap(imp2);
    }

    public static byte[] convert(float[] input, boolean scaling) {
        byte[] res = new byte[input.length];
        if (!scaling) {
            for (int i = 0; i < input.length; i++) {
                res[i] = (byte) (input[i] + 0.5f);
            }
        } else {
            float min = input[0];
            float max = input[0];
            for (float f : input) {
                if (f < min) {
                    min = f;
                }
                if (f > max) {
                    max = f;
                }
            }
            float coeff = 255 / (max - min);
            for (int i = 0; i < input.length; i++) {
                res[i] = (byte) ((input[i] - min) * coeff - 127.5);
            }
        }
        return res;
    }

    public static byte[] convert(short[] input, boolean scaling) {
        byte[] res = new byte[input.length];
        if (!scaling) {
            for (int i = 0; i < input.length; i++) {
                res[i] = (byte) (input[i] + 0.5f);
            }
        } else {
            float min = input[0];
            float max = input[0];
            for (float f : input) {
                if (f < min) {
                    min = f;
                }
                if (f > max) {
                    max = f;
                }
            }
            float coeff = 255 / (max - min);
            for (int i = 0; i < input.length; i++) {
                res[i] = (byte) ((input[i] - min) * coeff - 127.5);
            }
        }
        return res;
    }

    @Override
    public void erase() {
        for (int xy = 0; xy < sizeXY; xy++) {
            pixels[0][xy] = 0;
        }
        for (int z = 1; z < sizeZ; z++) {
            System.arraycopy(pixels[0], 0, pixels[z], 0, sizeXY);
        }
    }

    @Override
    public void fill(double value) {
        for (int xy = 0; xy < sizeXY; xy++) {
            pixels[0][xy] = (byte)value;
        }
        for (int z = 1; z < sizeZ; z++) {
            System.arraycopy(pixels[0], 0, pixels[z], 0, sizeXY);
        }
    }
    @Override
    public ImageByte duplicate() {
        ImageByte res = new ImageByte(img.duplicate());
        res.offsetX = offsetX;
        res.offsetY = offsetY;
        res.offsetZ = offsetZ;
        if (title != null) {
            res.title = title;
        }
        return res;
    }

    public void copy(ImageByte destination) {
        for (int z = 0; z < sizeZ; z++) {
            System.arraycopy(pixels[z], 0, destination.pixels[z], 0, sizeXY);
        }
    }
    @Override
    public float getPixel(int xy, int z) {
        return (float) (pixels[z][xy] & 0xff);
    }
    @Override
    public float getPixel(int coord) {
        return (float) (pixels[coord / sizeXY][coord % sizeXY] & 0xff);
    }

    public float getPixel(mcib3d.geom.Voxel3D vox, boolean approx) {
        return (float) (pixels[(int) vox.getZ()][(int) (vox.getX()) + ((int) (vox.getY())) * sizeX] & 0xff);
    }

    @Override
    public float getPixel(int x, int y, int z) {
        return (float) (pixels[z][x + y * sizeX] & 0xff);
    }
    @Override
    public int getPixelInt(int xy, int z) {
        return pixels[z][xy] & 0xff;
    }
    @Override
    public int getPixelInt(int coord) {
        return pixels[coord / sizeXY][coord % sizeXY] & 0xff;
    }

    public int getPixelInt(int x, int y, int z) {
        return pixels[z][x + y * sizeX] & 0xff;
    }

    public float getPixel(IntCoord3D vox) {
        return (float) (pixels[vox.z][vox.x + vox.y * sizeX] & 0xff);
    }

    public int getPixelInt(IntCoord3D vox) {
        return pixels[vox.z][vox.x + vox.y * sizeX] & 0xff;
    }

    @Override
    public void setPixel(int coord, float value) {
        pixels[coord / sizeXY][coord % sizeXY] = (byte) (value);
    }
    
    @Override
    public void setPixel(Point3D point, float value) {
        pixels[(int)point.z][(int)point.x + (int)point.y * sizeX] = (byte) (value);
    }

    @Override
    public void setPixel(int x, int y, int z, float value) {
        pixels[z][x + y * sizeX] = (byte) (value);
    }

    @Override
    public void setPixel(int xy, int z, float value) {
        pixels[z][xy] = (byte) (value + 0.5);
    }

    @Override
    public void setPixel(int x, int y, int z, int value) {
        pixels[z][x + y * sizeX] = (byte) value;
    }

    @Override
    public void setPixel(int xy, int z, int value) {
        pixels[z][xy] = (byte) value;
    }

    @Override
    protected synchronized void getMinAndMax(ImageInt mask) {
        ImageStats s = getImageStats(mask);
        if (s.minAndMaxSet()) return;
        int max = 0;
        int min = 255;
        if (mask == null) {
            for (int z = 0; z < sizeZ; z++) {
                for (int xy = 0; xy < sizeXY; xy++) {
                    if ((pixels[z][xy] & 0xff) > max) {
                        max = pixels[z][xy] & 0xff;
                    }
                    if ((pixels[z][xy] & 0xff) < min) {
                        min = pixels[z][xy] & 0xff;
                    }
                }
            }
        } else {
            for (int z = 0; z < sizeZ; z++) {
                for (int xy = 0; xy < sizeXY; xy++) {
                    if (mask.getPixel(xy, z) != 0) {
                        if ((pixels[z][xy] & 0xff) > max) {
                            max = pixels[z][xy] & 0xff;
                        }
                        if ((pixels[z][xy] & 0xff) < min) {
                            min = pixels[z][xy] & 0xff;
                        }
                    }
                }
            }
        }
        s.setMinAndMax(min, max);
    }

    @Override
    protected int[] getHisto(ImageInt mask) {
        if (mask == null) {
            mask = new BlankMask(this);
        }
        getMinAndMax(mask);
        ImageStats s = getImageStats(mask);
        int[] histo = new int[256];
        for (int z = 0; z < sizeZ; z++) {
            for (int xy = 0; xy < sizeXY; xy++) {
                if (mask.getPixel(xy, z) != 0) {
                    histo[pixels[z][xy] & 0xff]++;
                }
            }
        }
        s.setHisto256(histo, 1);
        return histo;
    }

    @Override
    protected int[] getHisto(ImageInt mask, int nBins, double min, double max) {
        if (mask == null) {
            mask = new BlankMask(this);
        }
        double scale = (double) nBins / (max - min);
        int[] hist = new int[nBins];
        int idx;
        for (int z = 0; z < sizeZ; z++) {
            for (int xy = 0; xy < sizeXY; xy++) {
                if (mask.getPixel(xy, z) != 0) {
                    idx = (int) (((pixels[z][xy] & 0xFF) - min) * scale);
                    if (idx >= nBins) {
                        hist[hist.length - 1]++;
                    } else {
                        hist[idx]++;
                    }
                }
            }
        }
        return hist;
    }

    @Override
    public IntImage3D getImage3D() {
        return new IntImage3D(img.getImageStack());
    }

    @Override
    public ImageByte threshold(float thld, boolean keepUnderThld, boolean strict) {
        ImageByte res = new ImageByte(this.title + "thld", sizeX, sizeY, sizeZ);
        res.offsetX = offsetX;
        res.offsetY = offsetY;
        res.offsetZ = offsetZ;
        if (!keepUnderThld && !strict) {
            for (int z = 0; z < sizeZ; z++) {
                for (int xy = 0; xy < sizeXY; xy++) {
                    if ((pixels[z][xy] & 0xFF) >= thld) {
                        res.pixels[z][xy] = (byte) 255;
                    }
                }
            }
        } else if (!keepUnderThld && strict) {
            for (int z = 0; z < sizeZ; z++) {
                for (int xy = 0; xy < sizeXY; xy++) {
                    if ((pixels[z][xy] & 0xFF) > thld) {
                        res.pixels[z][xy] = (byte) 255;
                    }
                }
            }
        } else if (keepUnderThld && !strict) {
            for (int z = 0; z < sizeZ; z++) {
                for (int xy = 0; xy < sizeXY; xy++) {
                    if ((pixels[z][xy] & 0xFF) <= thld) {
                        res.pixels[z][xy] = (byte) 255;
                    }
                }
            }
        } else if (keepUnderThld && strict) {
            for (int z = 0; z < sizeZ; z++) {
                for (int xy = 0; xy < sizeXY; xy++) {
                    if ((pixels[z][xy] & 0xFF) < thld) {
                        res.pixels[z][xy] = (byte) 255;
                    }
                }
            }
        }
        return res;
    }

    public void thresholdCut(float thld, boolean keepUnderThld, boolean strict) { //modifies the image
        if (!keepUnderThld && !strict) {
            for (int z = 0; z < sizeZ; z++) {
                for (int xy = 0; xy < sizeXY; xy++) {
                    if ((pixels[z][xy] & 0xFF) < thld) {
                        pixels[z][xy] = 0;
                    }
                }
            }
        } else if (!keepUnderThld && strict) {
            for (int z = 0; z < sizeZ; z++) {
                for (int xy = 0; xy < sizeXY; xy++) {
                    if ((pixels[z][xy] & 0xFF) <= thld) {
                        pixels[z][xy] = 0;
                    }
                }
            }
        } else if (keepUnderThld && !strict) {
            for (int z = 0; z < sizeZ; z++) {
                for (int xy = 0; xy < sizeXY; xy++) {
                    if ((pixels[z][xy] & 0xFF) > thld) {
                        pixels[z][xy] = 0;
                    }
                }
            }
        } else if (keepUnderThld && strict) {
            for (int z = 0; z < sizeZ; z++) {
                for (int xy = 0; xy < sizeXY; xy++) {
                    if ((pixels[z][xy] & 0xFF) >= thld) {
                        pixels[z][xy] = 0;
                    }
                }
            }
        }
    }

    @Override
    public ImageByte crop3D(String title, int x_min_, int x_max_, int y_min_, int y_max_, int z_min_, int z_max_) {
        int x_min = x_min_;
        int z_min = z_min_;
        int y_min = y_min_;
        int x_max = x_max_;
        int y_max = y_max_;
        int z_max = z_max_;
        int sX = x_max - x_min + 1;
        int sY = y_max - y_min + 1;
        int sZ = z_max - z_min + 1;
        ImageByte res = new ImageByte(title, sX, sY, sZ);
        res.offsetX = x_min;
        res.offsetY = y_min;
        res.offsetZ = z_min;
        res.setScale(this);
        int oZ = -z_min;
        int oY_i = 0;
        int oX = 0;
        if (x_min <= -1) {
            x_min = 0;
        }
        if (x_max >= sizeX) {
            x_max = sizeX - 1;
        }
        if (y_min <= -1) {
            oY_i = -sX * y_min;
            y_min = 0;
        }
        if (y_max >= sizeY) {
            y_max = sizeY - 1;
        }
        if (z_min <= -1) {
            z_min = 0;
        }
        if (z_max >= sizeZ) {
            z_max = sizeZ - 1;
        }
        int sXo = x_max - x_min + 1;
        for (int z = z_min; z <= z_max; z++) {
            int offY = y_min * sizeX;
            int oY = oY_i;
            for (int y = y_min; y <= y_max; y++) {
                System.arraycopy(pixels[z], offY + x_min, res.pixels[z + oZ], oY + oX, sXo);
                oY += sX;
                offY += sizeX;
            }
        }
        return res;
    }

    @Override
    public boolean shiftIndexes(TreeMap<Integer, int[]> bounds) {
        boolean change = false;
        int newLabel = 1;
        ArrayList<Integer> keySet = new ArrayList<Integer>(bounds.keySet());
        for (int i = 0; i < keySet.size(); i++) {
            int label = keySet.get(i);
            if (label > newLabel) {
                int[] bds = bounds.get(label);
                for (int z = bds[4]; z <= bds[5]; z++) {
                    for (int y = bds[2]; y <= bds[3]; y++) {
                        for (int x = bds[0]; x <= bds[1]; x++) {
                            int xy = x + y * sizeX;
                            if ((pixels[z][xy] & 0XFF) == label) {
                                pixels[z][xy] = (byte) newLabel;
                            }
                        }
                    }
                }
                change = true;
                bounds.remove(label);
                bounds.put(newLabel, bds);
            }
            newLabel++;
        }
        return change;
    }

    @Override
    public ImageByte[] crop3D(TreeMap<Integer, int[]> bounds) {
        ImageByte[] ihs = new ImageByte[bounds.size()];
        ArrayList<Integer> keys = new ArrayList<Integer>(bounds.keySet());
        for (int idx = 0; idx < ihs.length; idx++) {
            int label = keys.get(idx);
            int[] bds = bounds.get(label);
            ihs[idx] = this.crop3D(title + ":" + label, bds[0], bds[1], bds[2], bds[3], bds[4], bds[5]);
        }
        return ihs;
    }

    @Override
    public ImageByte crop3DMask(String title, ImageInt mask, int label, int x_min_, int x_max_, int y_min_, int y_max_, int z_min_, int z_max_) {
        int x_min = x_min_;
        int z_min = z_min_;
        int y_min = y_min_;
        int x_max = x_max_;
        int y_max = y_max_;
        int z_max = z_max_;
        int sX = x_max - x_min + 1;
        int sY = y_max - y_min + 1;
        int sZ = z_max - z_min + 1;
        ImageByte res = new ImageByte(title, sX, sY, sZ);
        res.offsetX = x_min;
        res.offsetY = y_min;
        res.offsetZ = z_min;
        res.setScale(this);
        int oZ = -z_min;
        int oY_i = 0;
        int oX = -x_min;
        if (x_min <= -1) {
            x_min = 0;
        }
        if (x_max >= sizeX) {
            x_max = sizeX - 1;
        }
        if (y_min <= -1) {
            oY_i = -sX * y_min;
            y_min = 0;
        }
        if (y_max >= sizeY) {
            y_max = sizeY - 1;
        }
        if (z_min <= -1) {
            z_min = 0;
        }
        if (z_max >= sizeZ) {
            z_max = sizeZ - 1;
        }
        if (mask instanceof ImageShort) {
            ImageShort m = (ImageShort) mask;
            for (int z = z_min; z <= z_max; z++) {
                int offY = y_min * sizeX;
                int oY = oY_i;
                for (int y = y_min; y <= y_max; y++) {
                    for (int x = x_min; x <= x_max; x++) {
                        if ((m.pixels[z][offY + x] & 0xffff) == label) {
                            res.pixels[z + oZ][oY + x + oX] = pixels[z][offY + x];
                        }
                    }
                    oY += sX;
                    offY += sizeX;
                }
            }
        } else if (mask instanceof ImageByte) {
            ImageByte m = (ImageByte) mask;
            for (int z = z_min; z <= z_max; z++) {
                int offY = y_min * sizeX;
                int oY = oY_i;
                for (int y = y_min; y <= y_max; y++) {
                    for (int x = x_min; x <= x_max; x++) {
                        if ((m.pixels[z][offY + x] & 0xff) == label) {
                            res.pixels[z + oZ][oY + x + oX] = pixels[z][offY + x];
                        }
                    }
                    oY += sX;
                    offY += sizeX;
                }
            }
        }
        return res;
    }

    @Override
    public ImageByte crop3DBinary(String title, int label, int x_min_, int x_max_, int y_min_, int y_max_, int z_min_, int z_max_) {
        int x_min = x_min_;
        int z_min = z_min_;
        int y_min = y_min_;
        int x_max = x_max_;
        int y_max = y_max_;
        int z_max = z_max_;
        int sX = x_max - x_min + 1;
        int sY = y_max - y_min + 1;
        int sZ = z_max - z_min + 1;
        ImageByte res = new ImageByte(title, sX, sY, sZ);
        res.offsetX = x_min;
        res.offsetY = y_min;
        res.offsetZ = z_min;
        res.setScale(this);
        int oZ = -z_min;
        int oY_i = 0;
        int oX = -x_min;
        if (x_min <= -1) {
            x_min = 0;
        }
        if (x_max >= sizeX) {
            x_max = sizeX - 1;
        }
        if (y_min <= -1) {
            oY_i = -sX * y_min;
            y_min = 0;
        }
        if (y_max >= sizeY) {
            y_max = sizeY - 1;
        }
        if (z_min <= -1) {
            z_min = 0;
        }
        if (z_max >= sizeZ) {
            z_max = sizeZ - 1;
        }
        for (int z = z_min; z <= z_max; z++) {
            int offY = y_min * sizeX;
            int oY = oY_i;
            for (int y = y_min; y <= y_max; y++) {
                for (int x = x_min; x <= x_max; x++) {
                    if ((pixels[z][offY + x] & 0xff) == label) {
                        res.pixels[z + oZ][oY + x + oX] = (byte) 255;
                    }
                }
                oY += sX;
                offY += sizeX;
            }
        }
        return res;
    }

    /*
     * public static ImageByte merge3DBinary(ImageByte[] images, int sizeX, int
     * sizeY, int sizeZ) { ImageByte out = new ImageByte("merge", sizeX, sizeY,
     * sizeZ); if (images==null || images.length==0 || images[0]==null) return
     * out; // si offset negatif, les pixels doivent etre = 0 sinon erreur for
     * (int idx = 0; idx < images.length; idx++) { byte label = (byte)(idx+1);
     * for (int z = 0; z<images[idx].sizeZ; z++) { for (int y = 0;
     * y<images[idx].sizeY; y++) { for (int x = 0; x<images[idx].sizeX; x++) {
     * if (images[idx].pixels[z][x+y*images[idx].sizeX]!=0) {
     * out.pixels[z+images[idx].offsetZ][x+images[idx].offsetX+
     * (y+images[idx].offsetY)*sizeX]=label; } } } } } return out; }
     *
     */
    

    @Override
    public ImageHandler resize(int dX, int dY, int dZ) {
        int newX = Math.max(1, sizeX + 2 * dX);
        int newY = Math.max(1, sizeY + 2 * dY);
        boolean bck = Prefs.get("resizer.zero", true);
        Prefs.set("resizer.zero", true);
        ij.plugin.CanvasResizer cr = new ij.plugin.CanvasResizer();
        ImageStack res = cr.expandStack(img.getStack(), newX, newY, dX, dY);
        if (!bck) {
            //Prefs.set("resizer.zero", false);
        }
        if (dZ > 0) {
            for (int i = 0; i < dZ; i++) {
                res.addSlice("", new ByteProcessor(newX, newY), 0);
                res.addSlice("", new ByteProcessor(newX, newY));
            }
        } else {
            for (int i = 0; i < -dZ; i++) {
                if (res.getSize() <= 2) {
                    break;
                }
                res.deleteLastSlice();
                res.deleteSlice(1);

            }
        }
        return new ImageByte(new ImagePlus(title + "::resized", res));
    }

    @Override
    public ImageByte resample(int newX, int newY, int newZ, int method) {
        if (method ==-1 ) method = ij.process.ImageProcessor.BICUBIC;
        if ((newX == sizeX && newY == sizeY && newZ == sizeZ) || (newX == 0 && newY == 0 && newZ == 0)) {
            return new ImageByte(img.duplicate());
        }
        ImagePlus ip;

        if (newX != 0 && newY != 0 && newX != sizeX && newY != sizeY) {
            StackProcessor sp = new StackProcessor(img.getImageStack(), img.getProcessor());
            ip = new ImagePlus(title + "::resampled", sp.resize(newX, newY, true));
        } else {
            ip = img;
        }
        if (newZ != 0 && newZ != sizeZ) {
            ij.plugin.Resizer r = new ij.plugin.Resizer();
            ip = r.zScale(ip, newZ, method);
        }
        return new ImageByte(ip);
    }

    @Override
    public ImageByte resample(int newZ, int method) {
        if (method ==-1 ) method = ij.process.ImageProcessor.BICUBIC;
        ij.plugin.Resizer r = new ij.plugin.Resizer();
        return new ImageByte(r.zScale(img, newZ, method));
    }

    @Override
    protected ImageFloat normalize_(ImageInt mask, double saturation) {
        getMinAndMax(mask);
        ImageStats s = getImageStats(mask);
        double max_ = s.getMax();
        if (saturation > 0 && saturation < 1) {
            max_ = this.getPercentile(saturation, mask);
        }
        if (max_ <= s.getMin()) {
            max_ = s.getMin() + 1;
        }
        double scale = 1 / (max_ - s.getMin());
        double offset = - s.getMin() * scale;
        ImageFloat res = new ImageFloat(title + "::normalized", sizeX, sizeY, sizeZ);
        if (saturation > 0 && saturation < 1) {
            for (int z = 0; z < sizeZ; z++) {
                for (int xy = 0; xy < sizeXY; xy++) {
                    res.pixels[z][xy] = (float) (((pixels[z][xy] & 0XFF) >= max_) ? 1 : (pixels[z][xy] & 0XFF) * scale + offset);
                }
            }
        } else {
            for (int z = 0; z < sizeZ; z++) {
                for (int xy = 0; xy < sizeXY; xy++) {
                    res.pixels[z][xy] = (float) ((pixels[z][xy] & 0XFF) * scale + offset);
                }
            }
        }
        return res;
    }

    @Override
    public ImageFloat normalize(double min, double max) {
        double scale = 1 / (max - min);
        double offset = -min * scale;
        ImageFloat res = new ImageFloat(title + "::normalized", sizeX, sizeY, sizeZ);
        double pix;
        for (int z = 0; z < sizeZ; z++) {
            for (int xy = 0; xy < sizeXY; xy++) {
                pix = pixels[z][xy] & 0xff;
                if (pix >= max) {
                    res.pixels[z][xy] = 1;
                } else if (pix <= min) {
                    res.pixels[z][xy] = 0;
                } else {
                    res.pixels[z][xy] = (float) (pix * scale + offset);
                }
            }
        }
        return res;
    }

    @Override
    public void invert(ImageInt mask) {
        for (int z = 0; z < sizeZ; z++) {
            for (int xy = 0; xy < sizeXY; xy++) {
                pixels[z][xy] = (byte) (255 - pixels[z][xy]);
            }
        }
    }

    @Override
    public void draw(Object3D o, float value) {
        draw(o, (int) (value + 0.5));
    }

    @Override
    public void draw(Object3D o, int value) {
        Object3DVoxels ov;
        if (!(o instanceof Object3DVoxels)) {
            ov = o.getObject3DVoxels();
        } else {
            ov = (Object3DVoxels) o;
        }
        if (value > 255) {
            value = 255;
        }
        if (value < 0) {
            value = 0;
        }
        byte val = (byte) value;
        for (Voxel3D v : ov.getVoxels()) {
            if (contains(v.getX(), v.getY(), v.getZ())) {
                pixels[v.getRoundZ()][v.getRoundX() + v.getRoundY() * sizeX] = val;
            }
        }
    }

    @Override
    protected void flushPixels() {
        if (pixels != null) {
            for (int i = 0; i < pixels.length; i++) {
                pixels[i] = null;
            }
            pixels = null;
        }
    }

    @Override
    public boolean isOpened() {
        return !(pixels == null || img == null || img.getProcessor() == null);
    }

    @Override
    public float getPixel(Point3D P) {
        return (float) (pixels[P.getRoundZ()][P.getRoundX() + P.getRoundY() * sizeX] & 0xff);
    }

    @Override
    public int getPixelInt(Point3D P) {
        return pixels[P.getRoundZ()][P.getRoundX() + P.getRoundY() * sizeX] & 0xff;
    }

    @Override
    public float getPixelInterpolated(Point3D P) {
        return getPixel((float) P.getX(), (float) P.getY(), (float) P.getZ());
    }

    @Override
    public int getPixelIntInterpolated(Point3D P) {
        return (int) getPixel((float) P.getX(), (float) P.getY(), (float) P.getZ());
    }

   

    @Override
    public ImageHandler deleteSlices(int zmin, int zmax) {
        int z0 = Math.min(zmin, zmax);
        int z1 = Math.max(zmin, zmax);
        int diff = z1 - z0 + 1;
        int newSz = sizeZ - diff;
        //IJ.log("min-max "+z0+" "+z1);

        ImageByte res = new ImageByte("deleted slices", sizeX, sizeY, newSz);
        // copy before zmin
        for (int z = 0; z < z0; z++) {
            System.arraycopy(pixels[z], 0, res.pixels[z], 0, sizeXY);
        }
        // copy after zmax
        for (int z = z1 + 1; z < sizeZ; z++) {
            System.arraycopy(pixels[z], 0, res.pixels[z - diff], 0, sizeXY);
        }

        return res;
    }
    
    @Override
    public void trimSlices(int zmin, int zmax) {
        int z0 = Math.max(1, Math.min(zmin, zmax));
        int z1 = Math.min(sizeZ, Math.max(zmin, zmax));
        int newSize = z1-z0+1;
        byte[][] newPixels = new byte[newSize][];
        for (int i = 0; i<newSize; i++) {
            newPixels[i]=pixels[i+z0-1];
        }
        if (this.img!=null) {
            ImageStack stack = img.getImageStack();
            for (int i = 1; i<z0; i++) stack.deleteSlice(1);
            for (int i = z1+1; i<=sizeZ; i++) stack.deleteLastSlice();
        }
        this.sizeZ=newSize;
        this.sizeXYZ=sizeXY*sizeZ;
        this.offsetZ+=z0-1;
        this.stats = new HashMap<ImageHandler, ImageStats>(2);
    }
    
    // mask operation
    @Override
    public void intersectMask(ImageInt mask) {
        if (mask==null) return;
        for (int z = 0; z<sizeZ; z++) {
            for (int xy = 0; xy<sizeXY; xy++) {
                if (mask.getPixel(xy, z)==0) pixels[z][xy]=0;
            }
        }
    }
    
    public void substractMask(ImageInt mask) {
        if (mask==null) return;
        for (int z = 0; z<sizeZ; z++) {
            for (int xy = 0; xy<sizeXY; xy++) {
                if (mask.getPixel(xy, z)!=0) pixels[z][xy]=0;
            }
        }
    }
    
    public void addMask(ImageInt mask) {
        if (mask==null) return;
        for (int z = 0; z<sizeZ; z++) {
            for (int xy = 0; xy<sizeXY; xy++) {
                if (mask.getPixel(xy, z)!=0) pixels[z][xy]=(byte)255;
            }
        }
    }

    @Override
    public double getSizeInMb() {
        return (double)(sizeX*sizeY*sizeZ) / (1024*1024);
    }

    @Override
    public int getType() {
        return ImagePlus.GRAY8;
    }
    
    @Override 
    public ImageByte toMask() {
        ImageByte res = new ImageByte("mask", this.sizeX, this.sizeY, this.sizeZ);
        res.setScale(this);
        res.setOffset(this);
        for (int z = 0; z<sizeZ; z++) {
            for (int xy = 0; xy<sizeXY; xy++) {
                if (pixels[z][xy]!=0) res.pixels[z][xy]=(byte)255;
            }
        }
        return res;
    }
    
    @Override 
    public int countMaskVolume() {
        int count = 0;
        for (int z = 0; z<sizeZ; z++) {
            for (int xy = 0; xy<sizeXY; xy++) {
                if (pixels[z][xy]!=0) count++;
            }
        }
        return count;
    }
}
