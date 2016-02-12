package mcib3d.image3d;

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

public class ImageStats {
    ImageHandler image;
    ImageInt mask;
    private int[] histo256;
    private double max=Double.NaN, min=Double.NaN; 
    private double mean=Double.NaN, meanSquare=Double.NaN, sd=Double.NaN, histo256BinSize=Double.NaN;
    public ImageStats(ImageHandler image, ImageInt mask) {
        this.image=image;
        this.mask=mask;
    }
    
    public void resetMinAndMax() {
        image.getMinAndMax(mask);
    }
    
    public double getMin() {
        if (Double.isNaN(min)) image.getMinAndMax(mask);
        return min;
    }
    
    public double getMax() {
        if (Double.isNaN(max)) image.getMinAndMax(mask);
        return max;
    }
    
    public int[] getHisto256() {
        if (histo256==null) return image.getHisto(mask);
        else return histo256;
    }
    
    protected void setHisto256(int[] histo, double binSize) {
        this.histo256=histo;
        this.histo256BinSize=binSize;
    }
    
    protected boolean minAndMaxSet() {
        return (!Double.isNaN(min))&&(!Double.isNaN(max));
    }
    
    
    public double getHisto256BinSize() {
        if (Double.isNaN(this.histo256BinSize)) {
            if (Double.isNaN(max)) image.getMinAndMax(mask);
            histo256BinSize=image instanceof ImageByte?1d:(max-min)/256d;
        }
        return histo256BinSize;
    }
    
    public double getMean() {
        if (Double.isNaN(mean)) image.getMoments(mask);
        return mean;
    }
    
    
    protected void setMoments(double mean, double meanSquare, double sd) {
        this.mean=mean;
        this.sd=sd;
        this.meanSquare=meanSquare;
    }
    
    protected boolean momentsSet() {
        return (!Double.isNaN(mean) && !Double.isNaN(sd) && !Double.isNaN(meanSquare));
    }
    
    protected void setMinAndMax(double min, double max) {
        this.min=min;
        this.max=max;
    }
    
    public double getStandardDeviation() {
        if (Double.isNaN(sd)) image.getMoments(mask);
        return sd;
    }
    
    public double getRootMeanSquare() {
        if (Double.isNaN(this.meanSquare)) image.getMoments(mask);
        return Math.sqrt(meanSquare);
    }
    
    public ImageHandler getImage() {
        return image;
    }
    
    public ImageInt getMask() {
        return mask;
    }
    
}
