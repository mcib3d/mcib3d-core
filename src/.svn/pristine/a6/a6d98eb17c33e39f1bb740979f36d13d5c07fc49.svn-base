package mcib3d.image3d;

import mcib3d.image3d.legacy.RealImage3D;
import ij.*;
import ij.process.*;
import ij.gui.*;
import java.util.TreeMap;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicInteger;
import mcib3d.geom.*;
import mcib3d.image3d.legacy.IntImage3D;
import mcib3d.image3d.processing.FastFilters3D;
import mcib3d.utils.ArrayUtil;
import mcib3d.utils.ThreadRunner;
import mcib3d.utils.ThreadUtil;

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
 * @author Thomas Boudier
 */
public class ImageFloat extends ImageHandler {

    public float[][] pixels;

    public ImageFloat(ImagePlus img) {
        super(img);
        buildPixels();
    }

    public ImageFloat(ImageStack img) {
        super(img);
        buildPixels();
    }

    private void buildPixels() {
        pixels = new float[sizeZ][];
        if (img.getImageStack() != null) { //img.getImageStackSize() > 1
            for (int i = 0; i < sizeZ; i++) {
                pixels[i] = (float[]) img.getImageStack().getPixels(i + 1);
            }
        } else {
            ImageStack st = new ImageStack(sizeX, sizeY);
            st.addSlice(img.getProcessor());
            pixels[0] = (float[]) img.getProcessor().getPixels();
            this.img.setStack(null, st);
        }
    }

    public ImageFloat(float[][] pixels, String title, int sizeX) {
        super(title, sizeX, pixels[0].length / sizeX, pixels.length, 0, 0, 0);
        this.pixels = pixels;
        ImageStack st = new ImageStack(sizeX, sizeY, sizeZ);
        for (int z = 0; z < sizeZ; z++) {
            st.setPixels(pixels[z], z + 1);
        }
        img = new ImagePlus(title, st);
    }

    public ImageFloat(String title, int sizeX, int sizeY, int sizeZ) {
        super(title, sizeX, sizeY, sizeZ);
        img = NewImage.createFloatImage(title, sizeX, sizeY, sizeZ, 1);
        pixels = new float[sizeZ][];
        for (int i = 0; i < sizeZ; i++) {
            pixels[i] = (float[]) img.getImageStack().getPixels(i + 1);
        }
    }

    public ImageFloat(ImageHandler im) {
        super(im.title, im.sizeX, im.sizeY, im.sizeZ, im.offsetX, im.offsetY, im.offsetZ);
        ImageStats s = getImageStats(null);
        if (im instanceof ImageShort) {
            if (im.img != null) {
                ImageFloat f = ((ImageShort) im).convertToFloat(false);
                pixels = f.pixels;
                img = f.img;
                s.setMinAndMax(img.getProcessor().getMin(), img.getProcessor().getMax());
            }
        } else if (im instanceof ImageByte) {
            if (im.img != null) {
                ImageFloat f = ((ImageByte) im).convertToFloat(false);
                pixels = f.pixels;
                img = f.img;
                s.setMinAndMax(img.getProcessor().getMin(), img.getProcessor().getMax());
            }
        } else {
            this.img = im.img;
            this.pixels = ((ImageFloat) im).pixels;
        }
    }
    
    public static ImageFloat newBlankImageFloat(String title, ImageHandler ih) {
        ImageFloat res =  new ImageFloat(title, ih.sizeX, ih.sizeY, ih.sizeZ);
        res.setScale(ih);
        res.setOffset(ih);
        return res;
    }

    public ImageFloat(float[][] matrix) {
        super("matrix", matrix[0].length, matrix.length, 1);
        img = NewImage.createFloatImage(title, sizeX, sizeY, sizeZ, 1);
        pixels = new float[1][];
        pixels[0] = (float[]) img.getImageStack().getPixels(1);
        int offset = 0;
        for (float[] f : matrix) {
            System.arraycopy(f, 0, pixels[0], offset, f.length);
            offset += sizeX;
        }
    }

    public static float[] getArray1DFloat(ImagePlus img) {
        float[] res = new float[img.getNSlices() * img.getWidth() * img.getHeight()];
        int offZ = 0;
        int sizeXY = img.getWidth() * img.getHeight();
        for (int slice = 0; slice < img.getNSlices(); slice++) {
            System.arraycopy((float[]) img.getImageStack().getPixels(slice + 1), 0, res, offZ, sizeXY);
            offZ += sizeXY;
        }
        return res;
    }

    public Object getArray1D() {
        float[] res = new float[sizeXYZ];
        int offZ = 0;
        for (int slice = 0; slice < img.getNSlices(); slice++) {
            System.arraycopy((float[]) img.getImageStack().getPixels(slice + 1), 0, res, offZ, sizeXY);
            offZ += sizeXY;
        }
        return res;
    }

    public Object getArray1D(int z) {
        float[] res = new float[sizeXY];
        System.arraycopy((float[]) img.getImageStack().getPixels(z + 1), 0, res, 0, sizeXY);
        return res;
    }

    public static ImagePlus getImagePlus(float[] pixels, int sizeX, int sizeY, int sizeZ, boolean setMinAndMax) {
        if (pixels == null) {
            return null;
        }
        ImagePlus res = NewImage.createFloatImage("", sizeX, sizeY, sizeZ, 1);
        int offZ = 0;
        int sizeXY = sizeX * sizeY;
        for (int z = 0; z < sizeZ; z++) {
            System.arraycopy(pixels, offZ, (float[]) res.getImageStack().getPixels(z + 1), 0, sizeXY);
            offZ += sizeXY;
        }
        if (setMinAndMax) {
            float max = 0;
            float min = 0;
            for (int i = 0; i < pixels.length; i++) {
                if ((pixels[i]) > max) {
                    max = pixels[i];
                }
                if ((pixels[i]) < min) {
                    min = pixels[i];
                }
            }
            res.getProcessor().setMinAndMax(min, max);
        }
        return res;
    }

    public static ImagePlus getImagePlus(int[] pixels, int sizeX, int sizeY, int sizeZ, boolean setMinAndMax) {
        if (pixels == null) {
            return null;
        }
        ImagePlus res = NewImage.createFloatImage("", sizeX, sizeY, sizeZ, 1);
        int offZ = 0;
        int sizeXY = sizeX * sizeY;
        for (int z = 0; z < sizeZ; z++) {
            float[] output = (float[]) res.getImageStack().getPixels(z + 1);
            for (int i = 0; i < sizeXY; i++) {
                output[i] = (float) pixels[offZ + i];
            }
            offZ += sizeXY;
        }
        if (setMinAndMax) {
            float max = 0;
            float min = 0;
            for (int i = 0; i < pixels.length; i++) {
                if ((pixels[i]) > max) {
                    max = pixels[i];
                }
                if ((pixels[i]) < min) {
                    min = pixels[i];
                }
            }
            res.getProcessor().setMinAndMax(min, max);
        }
        return res;
    }

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
        //imp2.getProcessor().setMinAndMax(0, 255);
        return (ImageShort) ImageHandler.wrap(imp2);
    }

    public ImageByte convertToByte(boolean scaling) {
        getMinAndMax(null);
        ImageStats s = getImageStats(null);
        ImageByte res = new ImageByte(title, sizeX, sizeY, sizeZ);
        if (scaling) {
            double coeff = 255d / (s.getMax() - s.getMin());
            System.out.println("convert to byte: min:" + s.getMin() + " max:" + s.getMax() + "coeff:" + coeff);
            double min = s.getMin();
            for (int z = 0; z < sizeZ; z++) {
                for (int xy = 0; xy < sizeXY; xy++) {
                    int value = (int) (((pixels[z][xy] - min) * coeff) + 0.5);
                    if (value < 0 || value > 255) {
                        System.out.println(pixels[z][xy] + " to " + value);
                    }
                    res.pixels[z][xy] = (byte) (((pixels[z][xy] - min) * coeff) + 0.5);
                }
            }
            res.setMinAndMax(0, 255);
        } else {
            for (int z = 0; z < sizeZ; z++) {
                for (int xy = 0; xy < sizeXY; xy++) {
                    res.pixels[z][xy] = (byte) (pixels[z][xy]);
                }
            }
        }
        res.setScale(this);
        res.setOffset(this);
        return res;
    }

    public static float[] convert(short[] input) {
        float[] res = new float[input.length];
        for (int i = 0; i < input.length; i++) {
            res[i] = (float) (input[i] + 0.5f);
        }
        return res;
    }

    public static float[] convert(byte[] input) {
        float[] res = new float[input.length];
        for (int i = 0; i < input.length; i++) {
            res[i] = (float) (input[i]);
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
            pixels[0][xy] = (float) value;
        }
        for (int z = 1; z < sizeZ; z++) {
            System.arraycopy(pixels[0], 0, pixels[z], 0, sizeXY);
        }
    }

    public ImageFloat duplicate() {
        ImageFloat res = new ImageFloat(img.duplicate());
        res.offsetX = offsetX;
        res.offsetY = offsetY;
        res.offsetZ = offsetZ;
        if (title != null) {
            res.title = title;
        }
        return res;
    }

    public void copy(ImageFloat destination) {
        for (int z = 0; z < sizeZ; z++) {
            System.arraycopy(pixels[z], 0, destination.pixels[z], 0, sizeXY);
        }
    }

    public float getPixel(int coord) {
        return pixels[coord / sizeXY][coord % sizeXY];
    }

    public float getPixel(IntCoord3D vox) {
        return pixels[vox.z][vox.x + vox.y * sizeX];
    }

    public float getPixel(Coordinate3D coord) {
        return pixels[coord.z][coord.x + coord.y * sizeX];
    }

    public float getPixel(int x, int y, int z) {
        return pixels[z][x + y * sizeX];
    }

    public float getPixel(int xy, int z) {
        return pixels[z][xy];
    }

    @Override
    public void setPixel(int coord, float value) {
        pixels[coord / sizeXY][coord % sizeXY] = value;
    }
    
    @Override
    public void setPixel(Point3D point, float value) {
        pixels[(int)point.z][(int)point.x + (int)point.y * sizeX] = value;
    }

    @Override
    public void setPixel(int x, int y, int z, float value) {
        pixels[z][x + y * sizeX] = value;
    }

    @Override
    public void setPixel(int xy, int z, float value) {
        pixels[z][xy] = value;
    }

    @Override
    protected synchronized void getMinAndMax(ImageInt mask) {
        ImageStats s = getImageStats(mask);
        if (s.minAndMaxSet()) {
            return;
        }
        double min = Double.MAX_VALUE, max = -Double.MAX_VALUE;
        if (mask == null) {
            for (int z = 0; z < sizeZ; z++) {
                for (int xy = 0; xy < sizeXY; xy++) {
                    if ((pixels[z][xy]) > max) {
                        max = pixels[z][xy];
                    }
                    if ((pixels[z][xy]) < min) {
                        min = pixels[z][xy];
                    }
                }
            }
        } else {
            for (int z = 0; z < sizeZ; z++) {
                for (int xy = 0; xy < sizeXY; xy++) {
                    if (mask.getPixel(xy, z) != 0) {
                        if ((pixels[z][xy]) > max) {
                            max = pixels[z][xy];
                        }
                        if ((pixels[z][xy]) < min) {
                            min = pixels[z][xy];
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
        double coeff = 256f / (s.getMax() - s.getMin());
        double min = s.getMin();
        int idx;
        int[] histo = new int[256];
        for (int z = 0; z < sizeZ; z++) {
            for (int xy = 0; xy < sizeXY; xy++) {
                if (mask.getPixel(xy, z) != 0) {
                    idx = (int) ((pixels[z][xy] - min) * coeff);
                    if (idx >= 256) {
                        histo[255]++;
                    } else {
                        histo[idx]++;
                    }
                }
            }
        }
        s.setHisto256(histo, 1d / coeff);
        return histo;
    }

    @Override
    protected int[] getHisto(ImageInt mask, int nBins, double min, double max) {
        if (mask == null) {
            mask = new BlankMask(this);
        }
        double coeff = (double) nBins / (max - min);
        int[] hist = new int[nBins];
        int idx;
        for (int z = 0; z < sizeZ; z++) {
            for (int xy = 0; xy < sizeXY; xy++) {
                if (mask.getPixel(xy, z) != 0) {
                    idx = (int) ((pixels[z][xy] - min) * coeff);
                    if (idx >= nBins) {
                        hist[nBins - 1]++;
                    } else {
                        hist[idx]++;
                    }
                }
            }
        }
        return hist;
    }

    @Override
    public void draw(Object3D o, float value) {
        Object3DVoxels ov;
        if (!(o instanceof Object3DVoxels)) {
            ov = o.getObject3DVoxels();
        } else {
            ov = (Object3DVoxels) o;
        }
        for (Voxel3D v : ov.getVoxels()) {
            pixels[v.getRoundZ()][v.getRoundX() + v.getRoundY() * sizeX] = value;
        }
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
                    if ((pixels[z][xy]) >= thld) {
                        res.pixels[z][xy] = (byte) 255;
                    }
                }
            }
        } else if (!keepUnderThld && strict) {
            for (int z = 0; z < sizeZ; z++) {
                for (int xy = 0; xy < sizeXY; xy++) {
                    if ((pixels[z][xy]) > thld) {
                        res.pixels[z][xy] = (byte) 255;
                    }
                }
            }
        } else if (keepUnderThld && !strict) {
            for (int z = 0; z < sizeZ; z++) {
                for (int xy = 0; xy < sizeXY; xy++) {
                    if ((pixels[z][xy]) <= thld) {
                        res.pixels[z][xy] = (byte) 255;
                    }
                }
            }
        } else if (keepUnderThld && strict) {
            for (int z = 0; z < sizeZ; z++) {
                for (int xy = 0; xy < sizeXY; xy++) {
                    if ((pixels[z][xy]) < thld) {
                        res.pixels[z][xy] = (byte) 255;
                    }
                }
            }
        }
        return res;
    }

    @Override
    public void thresholdCut(float thld, boolean keepUnderThld, boolean strict) { //modifies the image
        if (!keepUnderThld && !strict) {
            for (int z = 0; z < sizeZ; z++) {
                for (int xy = 0; xy < sizeXY; xy++) {
                    if ((pixels[z][xy]) < thld) {
                        pixels[z][xy] = 0;
                    }
                }
            }
        } else if (!keepUnderThld && strict) {
            for (int z = 0; z < sizeZ; z++) {
                for (int xy = 0; xy < sizeXY; xy++) {
                    if ((pixels[z][xy]) <= thld) {
                        pixels[z][xy] = 0;
                    }
                }
            }
        } else if (keepUnderThld && !strict) {
            for (int z = 0; z < sizeZ; z++) {
                for (int xy = 0; xy < sizeXY; xy++) {
                    if ((pixels[z][xy]) > thld) {
                        pixels[z][xy] = 0;
                    }
                }
            }
        } else if (keepUnderThld && strict) {
            for (int z = 0; z < sizeZ; z++) {
                for (int xy = 0; xy < sizeXY; xy++) {
                    if ((pixels[z][xy]) >= thld) {
                        pixels[z][xy] = 0;
                    }
                }
            }
        }
    }

    @Override
    public ImageFloat crop3D(String title, int x_min_, int x_max_, int y_min_, int y_max_, int z_min_, int z_max_) {
        int x_min = x_min_;
        int z_min = z_min_;
        int y_min = y_min_;
        int x_max = x_max_;
        int y_max = y_max_;
        int z_max = z_max_;
        int sX = x_max - x_min + 1;
        int sY = y_max - y_min + 1;
        int sZ = z_max - z_min + 1;
        ImageFloat res = new ImageFloat(title, sX, sY, sZ);
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
    public ImageFloat[] crop3D(TreeMap<Integer, int[]> bounds) {
        ImageFloat[] ihs = new ImageFloat[bounds.size()];
        ArrayList<Integer> keys = new ArrayList<Integer>(bounds.keySet());
        for (int idx = 0; idx < ihs.length; idx++) {
            int label = keys.get(idx);
            int[] bds = bounds.get(label);
            ihs[idx] = this.crop3D(title + ":" + label, bds[0], bds[1], bds[2], bds[3], bds[4], bds[5]);
        }
        return ihs;
    }

    @Override
    public ImageFloat crop3DMask(String title, ImageInt mask, int label, int x_min_, int x_max_, int y_min_, int y_max_, int z_min_, int z_max_) {
        int x_min = x_min_;
        int z_min = z_min_;
        int y_min = y_min_;
        int x_max = x_max_;
        int y_max = y_max_;
        int z_max = z_max_;
        int sX = x_max - x_min + 1;
        int sY = y_max - y_min + 1;
        int sZ = z_max - z_min + 1;
        ImageFloat res = new ImageFloat(title, sX, sY, sZ);
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
                res.addSlice("", new FloatProcessor(newX, newY), 0);
                res.addSlice("", new FloatProcessor(newX, newY));
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
        return new ImageFloat(new ImagePlus(title + "::resized", res));
    }

    @Override
    public ImageHandler resample(int newX, int newY, int newZ, int method) {
        if (method == -1) {
            method = ij.process.ImageProcessor.BICUBIC;
        }
        if ((newX == sizeX && newY == sizeY && newZ == sizeZ) || (newX == 0 && newY == 0 && newZ == 0)) {
            return new ImageFloat(img.duplicate());
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
        return new ImageFloat(ip);
    }

    @Override
    public ImageHandler resample(int newZ, int method) {
        if (method == -1) {
            method = ij.process.ImageProcessor.BICUBIC;
        }
        ij.plugin.Resizer r = new ij.plugin.Resizer();
        return new ImageFloat(r.zScale(img, newZ, method));
    }

//    @Override
//    public ImageFloat grayscaleOpen(final int radXY, final int radZ, ImageHandler res, ImageHandler temp, boolean multithread) {
//        ImageFloat min = this.grayscaleFilter(radXY, radZ, FastFilters3D.MIN, temp, multithread);
//        ImageFloat open = min.grayscaleFilter(radXY, radZ, FastFilters3D.MAX, res, multithread);
//        return open;
//    }
//
//    @Override
//    public ImageFloat grayscaleClose(final int radXY, final int radZ, ImageHandler res, ImageHandler temp, boolean multithread) {
//        ImageFloat max = this.grayscaleFilter(radXY, radZ, FastFilters3D.MAX, temp, multithread);
//        ImageFloat close = max.grayscaleFilter(radXY, radZ, FastFilters3D.MIN, res, multithread);
//        max.closeImagePlus();
//        return close;
//    }
//
//    @Override
//    public ImageFloat grayscaleFilter(final int radXY, final int radZ, final int filter, ImageHandler resIH, boolean multithread) {
//        final RealImage3D in = new RealImage3D(img.getImageStack());
//        if (resIH == null) {
//            resIH = (ImageFloat) ImageHandler.newBlankImageHandler(title + "::filtered", this);
//        }
//        final RealImage3D res = new RealImage3D(resIH.img.getImageStack());
//        final ThreadRunner tr = new ThreadRunner(0, sizeZ, multithread ? 0 : 1);
//        for (int i = 0; i < tr.threads.length; i++) {
//            tr.threads[i] = new Thread(
//                    new Runnable() {
//                        public void run() {
//                            for (int idx = tr.ai.getAndIncrement(); idx < tr.end; idx = tr.ai.getAndIncrement()) {
//                                in.filterGeneric(res, radXY, radXY, radZ, idx, idx + 1, filter);
//                            }
//                        }
//                    });
//        }
//        tr.startAndJoin();
//        return (ImageFloat) resIH;
//    }
    @Override
    protected ImageFloat normalize_(ImageInt mask, double saturation) {
        getMinAndMax(mask);
        ImageStats s = getImageStats(mask);
        double max_ = s.getMax();
        if (saturation > 0 && saturation < 1) {
            max_ = this.getPercentile(saturation, mask);
        }
        if (max_ <= s.getMin()) {
            max_ = s.getMin();
        }
        double scale = 1 / (max_ - s.getMin());
        double offset = -s.getMin() * scale;
        ImageFloat res = new ImageFloat(title + "::normalized", sizeX, sizeY, sizeZ);
        if (saturation > 0 && saturation < 1) {
            for (int z = 0; z < sizeZ; z++) {
                for (int xy = 0; xy < sizeXY; xy++) {
                    res.pixels[z][xy] = (float) ((pixels[z][xy] >= max_) ? 1 : pixels[z][xy] * scale + offset);
                }
            }
        } else {
            for (int z = 0; z < sizeZ; z++) {
                for (int xy = 0; xy < sizeXY; xy++) {
                    res.pixels[z][xy] = (float) (pixels[z][xy] * scale + offset);
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
        for (int z = 0; z < sizeZ; z++) {
            for (int xy = 0; xy < sizeXY; xy++) {
                if (pixels[z][xy] >= max) {
                    res.pixels[z][xy] = 1;
                } else if (pixels[z][xy] <= min) {
                    res.pixels[z][xy] = 0;
                } else {
                    res.pixels[z][xy] = (float) (pixels[z][xy] * scale + offset);
                }
            }
        }
        return res;
    }

    @Override
    public void intersectMask(ImageInt mask) {
        for (int z = 0; z < sizeZ; z++) {
            for (int xy = 0; xy < sizeXY; xy++) {
                if (mask.getPixel(xy, z) == 0) {
                    pixels[z][xy] = 0;
                }
            }
        }
    }

    @Override
    public void invert(ImageInt mask) {
        getMinAndMax(mask);
        for (int z = 0; z < sizeZ; z++) {
            for (int xy = 0; xy < sizeXY; xy++) {
                pixels[z][xy] = -pixels[z][xy];
            }
        }
    }

    public void opposite() {
        for (int z = 0; z < sizeZ; z++) {
            for (int xy = 0; xy < sizeXY; xy++) {
                pixels[z][xy] = -pixels[z][xy];
            }
        }
    }

    
    
    public void subtract(ImageFloat other) {
        /*if (!this.sameDimentions(other)) {
            return;
        }
        */
        for (int z = 0; z < sizeZ; z++) {
            for (int xy = 0; xy < sizeXY; xy++) {
                pixels[z][xy] -= other.pixels[z][xy];
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
        return pixels[P.getRoundZ()][P.getRoundX() + P.getRoundY() * sizeX];
    }

    @Override
    public float getPixelInterpolated(Point3D P) {
        return getPixel((float) P.x, (float) P.y, (float) P.z);
    }

 

    @Override
    public ImageHandler cropRadius(int xc, int yc, int zc, int rx, int ry, int rz, boolean mean, boolean sphere) {
        int x0 = Math.max(0, xc - rx);
        int y0 = Math.max(0, yc - ry);
        int z0 = Math.max(0, zc - rz);
        int x1 = Math.min(sizeX, xc + rx);
        int y1 = Math.min(sizeY, yc + ry);
        int z1 = Math.min(sizeZ, zc + rz);
        ImageFloat res;
        double r;
        double rx2 = rx * rx;
        double ry2 = ry * ry;
        double rz2 = rz * rz;
        float moy = 0;
        if (mean) {
            ImageStats s = this.getImageStats(null);
            moy = (int) s.getMean();
        }

        res = new ImageFloat("crop_" + title, x1 - x0 + 1, y1 - y0 + 1, z1 - z0 + 1);

        for (int z = zc - rz; z <= z1; z++) {
            for (int x = xc - rx; x <= x1; x++) {
                for (int y = yc - ry; y <= y1; y++) {
                    if (sphere) {
                        r = (x - xc) * (x - xc) / rx2 + (y - yc) * (y - yc) / ry2 + (z - zc) * (z - zc) / rz2;
                        if (r <= 1) {
                            res.setPixel(x - xc + rx, y - yc + ry, z - zc + rz, getPixel(x, y, z));
                        } else {
                            res.setPixel(x - xc + rx, y - yc + ry, z - zc + rz, moy);
                        }
                    } else {
                        res.setPixel(x - xc + rx, y - yc + ry, z - zc + rz, getPixel(x, y, z));
                    }
                }
            }
        }
        return res;
    }

    /**
     * 3D filter using threads
     *
     * @param out
     * @param radx Radius of mean filter in x
     * @param rady Radius of mean filter in y
     * @param radz Radius of mean filter in z
     * @param zmin
     * @param zmax
     * @param filter
     */
    public void filterGeneric(ImageFloat out, float radx, float rady, float radz, int zmin, int zmax, int filter) {
        int[] ker = FastFilters3D.createKernelEllipsoid(radx, rady, radz);
        int nb = 0;
        for (int i = 0; i < ker.length; i++) {
            nb += ker[i];
        }
        if (zmin < 0) {
            zmin = 0;
        }
        if (zmax > this.sizeZ) {
            zmax = this.sizeZ;
        }
        float value;
        for (int k = zmin; k < zmax; k++) {
            IJ.showStatus("3D filter : " + k + "/" + zmax);
            for (int j = 0; j < sizeY; j++) {
                for (int i = 0; i < sizeX; i++) {
                    ArrayUtil tab = this.getNeighborhoodKernel(ker, nb, i, j, k, radx, rady, radz);
                    if (filter == FastFilters3D.MEAN) {
                        out.setPixel(i, j, k, (float) tab.getMean());
                    } else if (filter == FastFilters3D.MEDIAN) {
                        out.setPixel(i, j, k, (float) tab.medianSort());
                    }
                    if (filter == FastFilters3D.MIN) {
                        out.setPixel(i, j, k, (float) tab.getMinimum());
                    }
                    if (filter == FastFilters3D.MAX) {
                        out.setPixel(i, j, k, (float) tab.getMaximum());
                    }
                    if (filter == FastFilters3D.VARIANCE) {
                        out.setPixel(i, j, k, (float) tab.getVariance2());
                    }
                    if (filter == FastFilters3D.MAXLOCAL) {
                        value = this.getPixel(i, j, k);
                        if (tab.isMaximum(value)) {
                            out.setPixel(i, j, k, value);
                        } else {
                            out.setPixel(i, j, k, 0);
                        }
                    }
                }
            }
        }
    }

    public void filterGeneric(ImageFloat out, Object3DVoxels obj, int zmin, int zmax, int filter) {
        if (zmin < 0) {
            zmin = 0;
        }
        if (zmax > this.sizeZ) {
            zmax = this.sizeZ;
        }
        float value;
        int[] ker = FastFilters3D.createKernelFromObject(obj);
        int nb = FastFilters3D.getNbFromKernel(ker);
        float[] rad = FastFilters3D.getRadiiFromObject(obj);

        ArrayUtil tab;
        for (int k = zmin; k < zmax; k++) {
            IJ.showStatus("3D filter : " + (k + 1) + "/" + zmax);
            for (int j = 0; j < sizeY; j++) {
                for (int i = 0; i < sizeX; i++) {
                    //tab = obj.listVoxels(this, i, j, k);
                    tab = this.getNeighborhoodKernel(ker, nb, i, j, k, rad[0], rad[1], rad[2]);
                    if (filter == FastFilters3D.MEAN) {
                        out.setPixel(i, j, k, (float) (tab.getMean() + 0.5));
                    } else if (filter == FastFilters3D.MEDIAN) {
                        out.setPixel(i, j, k, (float) tab.medianSort());
                    }
                    if (filter == FastFilters3D.MIN) {
                        out.setPixel(i, j, k, (float) tab.getMinimum());
                    }
                    if (filter == FastFilters3D.MAX) {
                        out.setPixel(i, j, k, (float) tab.getMaximum());
                    }
                    if (filter == FastFilters3D.VARIANCE) {
                        out.setPixel(i, j, k, (float) (tab.getVariance2() + 0.5));
                    }
                    if (filter == FastFilters3D.MAXLOCAL) {
                        value = this.getPixel(i, j, k);
                        if (tab.isMaximum(value)) {
                            out.setPixel(i, j, k, value);
                        } else {
                            out.setPixel(i, j, k, 0);
                        }
                    }
                }
            }
        }
    }

    /**
     * Sobel-like filtering in 3D
     *
     * @return The 3D filtered image
     */
    public ImageFloat sobelFilter() {
        ImageFloat res = (ImageFloat) this.createSameDimensions();
        ArrayUtil nei;
        double[] edgeX = {-1, 0, 1, -2, 0, 2, -1, 0, 1, -2, 0, 2, -4, 0, 4, -2, 0, 2, -1, 0, 1, -2, 0, 2, -1, 0, 1};
        double[] edgeY = {-1, -2, -1, 0, 0, 0, 1, 2, 1, -2, -4, -2, 0, 0, 0, 2, 4, 2, -1, -2, -1, 0, 0, 0, 1, 2, 1};
        double[] edgeZ = {-1, -2, -1, -2, -4, -2, -1, -2, -1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 2, 1, 2, 4, 2, 1, 2, 1};
        double ex;
        double ey;
        double ez;
        double edge;


        for (int k = 0; k < sizeZ; k++) {
            //if (this.showStatus) {
            //    IJ.showStatus("3D Sobel : " + (int) (100 * k / sizez) + "%");
            //}
            for (int j = 0; j < sizeY; j++) {
                for (int i = 0; i < sizeX; i++) {
                    nei = getNeighborhood3x3x3(i, j, k);
                    ex = nei.convolve(edgeX, 1.0f);
                    ey = nei.convolve(edgeY, 1.0f);
                    ez = nei.convolve(edgeZ, 1.0f);
                    edge = Math.sqrt(ex * ex + ey * ey + ez * ez);
                    res.setPixel(i, j, k, (float) edge);
                }
            }
        }
        return res;
    }

    /**
     * Adaptive Filter in 3D (expermimental) Take 7 neighborhood, compute mean
     * and std dev Assign mean of neighborhood with lowest std dev
     *
     * @param radx
     * @param radz
     * @param rady
     * @return 3D filtered image
     */
    public ImageFloat adaptiveFilter(float radx, float rady, float radz, int nbcpus) {
        ImageFloat adaptimg = (ImageFloat) this.createSameDimensions();
        final ImageFloat adaptimg2 = adaptimg;

        // create kernel
        final int[] ker = FastFilters3D.createKernelEllipsoid(radx, rady, radz);
        int nb = 0;
        for (int i = 0; i < ker.length; i++) {
            nb += ker[i];
        }
        final int nb2 = nb;
        final float radX2 = radx;
        final float radY2 = rady;
        final float radZ2 = radz;


        // PARALLEL 
        final AtomicInteger ai = new AtomicInteger(0);
        Thread[] threads = ThreadUtil.createThreadArray(nbcpus);

        for (int ithread = 0; ithread < threads.length; ithread++) {
            threads[ithread] = new Thread() {
                @Override
                public void run() {

                    ArrayUtil[] tab = new ArrayUtil[7];
                    // displacement to take neighborhoods
                    int dep = 1;
                    double mes;
                    double mins;
                    double si;
                    double me = 0;

                    for (int k = ai.getAndIncrement(); k < sizeZ; k = ai.getAndIncrement()) {
                        //if (showStatus) {
                        //    IJ.showStatus("3D Adaptive : " + (int) (100 * k / sizez) + "%");
                        // }
                        for (int j = 0; j < sizeY; j++) {
                            for (int i = 0; i < sizeX; i++) {
                                tab[0] = getNeighborhoodKernel(ker, nb2, i, j, k, radX2, radY2, radZ2);
                                tab[1] = getNeighborhoodKernel(ker, nb2, (int) (i + dep), j, k, radX2, radY2, radZ2);
                                tab[2] = getNeighborhoodKernel(ker, nb2, (int) (i - dep), j, k, radX2, radY2, radZ2);
                                tab[3] = getNeighborhoodKernel(ker, nb2, i, (int) (j + dep), k, radX2, radY2, radZ2);
                                tab[4] = getNeighborhoodKernel(ker, nb2, i, (int) (j - dep), k, radX2, radY2, radZ2);
                                tab[5] = getNeighborhoodKernel(ker, nb2, i, j, (int) (k + dep), radX2, radY2, radZ2);
                                tab[6] = getNeighborhoodKernel(ker, nb2, i, j, (int) (k - dep), radX2, radY2, radZ2);
                                mes = 0;
                                mins = Float.MAX_VALUE;
                                for (int c = 0; c < 7; c++) {
                                    //me = tab[c].median();
                                    me = tab[c].getMean();
                                    si = tab[c].getStdDev();
                                    if (si < mins) {
                                        mins = si;
                                        mes = me;
                                    }
                                }
                                adaptimg2.setPixel(i, j, k, (int) mes);
                            }
                        }
                    }
                }
            };
        }

        ThreadUtil.startAndJoin(threads);

        return adaptimg2;
    }

    @Override
    public ImageHandler deleteSlices(int zmin, int zmax) {
        int z0 = Math.min(zmin, zmax);
        int z1 = Math.max(zmin, zmax);
        int diff = z1 - z0 + 1;
        int newSz = sizeZ - diff;

        ImageFloat res = new ImageFloat("deleted slices", sizeX, sizeY, newSz);
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
        int newSize = z1 - z0 + 1;
        float[][] newPixels = new float[newSize][];
        for (int i = 0; i < newSize; i++) {
            newPixels[i] = pixels[i + z0 - 1];
        }
        if (this.img != null) {
            ImageStack stack = img.getImageStack();
            for (int i = 1; i < z0; i++) {
                stack.deleteSlice(1);
            }
            for (int i = z1 + 1; i <= sizeZ; i++) {
                stack.deleteLastSlice();
            }
        }
        this.sizeZ = newSize;
        this.sizeXYZ = sizeXY * sizeZ;
        this.offsetZ += z0 - 1;
        this.stats = new HashMap<ImageHandler, ImageStats>(2);
    }

    @Override
    public double getSizeInMb() {
        return (double) (4 * sizeX * sizeY * sizeZ) / (1024 * 1024);
    }
    
    @Override
    public int getType() {
        return ImagePlus.GRAY32;
    }
}
