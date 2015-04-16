package mcib3d.image3d;

import ij.ImagePlus;
import java.util.TreeMap;
import mcib3d.geom.IntCoord3D;
import mcib3d.geom.Object3D;

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

public class BlankMask extends ImageByte { // TODO extends imageInt
    double scaleXY, scaleZ;
    
    public BlankMask(String title, int sizeX, int sizeY, int sizeZ) {
        super(sizeX, sizeY, sizeZ);
    }
    
    public BlankMask(ImageHandler template) {
        super(template.sizeX, template.sizeY, template.sizeZ);
        this.scaleXY=template.getScaleXY();
        this.scaleZ=template.getScaleZ();
    }
    
    @Override
    public double getScaleXY() {
        if (img==null) return scaleXY;
        else return super.getScaleXY();
    }
    
    @Override
    public double getScaleZ() {
        if (img==null) return scaleZ;
        else return super.getScaleZ();
    }
    
    @Override
    public ImagePlus getImagePlus() {
        if (img!=null && img.getProcessor()!=null) return img;
        ImageByte b =new ImageByte(title, sizeX, sizeY, sizeZ);
        this.img=b.getImagePlus();
        this.pixels=b.pixels;
        this.fill((byte)1);
        return img;
    }
    
    @Override
    public void draw(Object3D o, int value) {
        
    }

    @Override
    public int getPixelInt(int xy, int z) {
        return 1;
    }

    @Override
    public int getPixelInt(int x, int y, int z) {
        return 1;
    }

    @Override
    public int getPixelInt(int coord) {
        return 1;
    }

    @Override
    public TreeMap<Integer, int[]> getBounds(boolean addBorder) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public ImageByte crop3DBinary(String title, int label, int x_min, int x_max, int y_min, int y_max, int z_min, int z_max) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean shiftIndexes(TreeMap<Integer, int[]> bounds) {
        return false;
    }

    @Override
    public float getPixel(int coord) {
        return 1;
    }

    @Override
    public float getPixel(int x, int y, int z) {
        return 1;
    }

    @Override
    public float getPixel(int xy, int z) {
        return 1;
    }

    @Override
    public float getPixel(IntCoord3D vox) {
        return 1;
    }
    
    
    @Override
    public void setPixel(int coord, float value) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void setPixel(int x, int y, int z, float value) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void setPixel(int xy, int z, float value) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Object getArray1D() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void erase() {
        
    }

    @Override
    protected void getMinAndMax(ImageInt mask) {
        
    }

    @Override
    public void setMinAndMax(ImageInt mask) {
        
    }

    @Override
    public int[] getHisto(ImageInt mask) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    protected int[] getHisto(ImageInt mask, int nBins, double min, double max) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public ImageByte threshold(float thld, boolean keepUnderThld, boolean strict) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void thresholdCut(float thld, boolean keepUnderThld, boolean strict) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public ImageByte crop3D(String title, int x_min, int x_max, int y_min, int y_max, int z_min, int z_max) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public ImageByte[] crop3D(TreeMap<Integer, int[]> bounds) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public ImageByte crop3DMask(String title, ImageInt mask, int label, int x_min_, int x_max_, int y_min_, int y_max_, int z_min_, int z_max_) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public ImageHandler resize(int dX, int dY, int dZ) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public ImageByte resample(int newX, int newY, int newZ, int method) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public ImageByte resample(int newZ, int method) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    protected ImageFloat normalize_(ImageInt mask, double saturation) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    @Override
    public boolean equals(Object o) {
        if (o instanceof BlankMask) {
            BlankMask other = (BlankMask)o;
            return other.sizeX==sizeX && other.sizeY==sizeY && other.sizeZ==sizeZ;
        } else return false;
    }
    
    @Override
    public int hashCode() {
        int hash = 7;
        hash = 89 * hash + this.sizeX;
        hash = 89 * hash + this.sizeY;
        hash = 89 * hash + this.sizeZ;
        return hash;
    }
}
