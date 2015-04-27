package tango.spatialStatistics.StochasticProcess;

import ij.IJ;
import mcib3d.image3d.ImageFloat;
import mcib3d.image3d.ImageHandler;
import java.util.*;
import java.util.Map.Entry;
import mcib3d.geom.Point3D;
import mcib3d.image3d.ImageInt;
import tango.gui.Core;
import tango.spatialStatistics.util.KDTreeC;

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


// faire classe abstraite, une extension uniforme + rapide, et cette extension avec proba map. 
// ajouter "hardCore process" > avec un rayon. conserve la liste des points si dans ce rayon il y a des points déjà tirés on recommence le tirage
// stocker les points dans une image et parcourir le rayon??

public class RandomPoint3DGeneratorProbaMap extends RandomPoint3DGenerator {
    double[] cumulProba;
    ImageFloat probaImage;
    
    public RandomPoint3DGeneratorProbaMap(ImageInt mask, int maxSize, ImageHandler probaImage, float saturation, int nbCPUs, boolean verbose) {
        super(mask, maxSize, nbCPUs, verbose);
        setProbaMap(probaImage, saturation);
    }
    
    
    
    protected void setProbaMap(ImageHandler probaImage, float saturation) {
        if (!probaImage.sameDimentions(mask)) {
            if (Core.GUIMode) IJ.log("proba map must be of same dimentions as mask..");
            return;
        }
        this.probaImage=probaImage.normalize(mask, saturation);
        //this.probaImage.showDuplicate("probaImage");
        cumulProba=new double[maskCoordsZ.length];
        cumulProba[0]=this.probaImage.pixels[this.maskCoordsZ[0]][this.maskCoordsXY[0]];
        for (int i = 1; i<cumulProba.length; i++) cumulProba[i]=cumulProba[i-1]+this.probaImage.pixels[this.maskCoordsZ[i]][this.maskCoordsXY[i]];
        double sum = cumulProba[cumulProba.length-1];
        for (int i =0; i<cumulProba.length; i++) cumulProba[i]/=sum;
    }
    
    @Override
    public Point3D drawPoint3D() {
        double key = randomGenerator.nextDouble();
        int idx = Arrays.binarySearch(cumulProba, key);
        Point3D point;
        if (idx<0) {
            idx=-idx-1;
            if (idx>0 && (idx==cumulProba.length || (cumulProba[idx]-key>key-cumulProba[idx-1]))) idx--;
            point=new Point3D(maskCoordsXY[idx]%mask.sizeX, maskCoordsXY[idx]/mask.sizeX, maskCoordsZ[idx]);
            point=toFloat(point);
        } else point=new Point3D(maskCoordsXY[idx]%mask.sizeX, maskCoordsXY[idx]/mask.sizeX, maskCoordsZ[idx] );
        return point;
    }
    
    
    
    protected Point3D toFloat(Point3D point) { //int coords..floatise en fonction des voisins
        int x = (int) point.getX();
        int y = (int) point.getY();
        int z = (int) point.getZ();
        Point3D res = new Point3D(0, 0, 0);
        if (x>=1 && mask.getPixel(x-1, y, z)>0) {
            if (x+1<mask.sizeX && mask.getPixel(x+1, y, z)>0) {
                res.setX(x+interpolation(probaImage.getPixel(x-1, y, z), probaImage.getPixel(x, y, z), probaImage.getPixel(x+1, y, z)));
            } else res.setX(x+interpolationPrev(probaImage.getPixel(x-1, y, z), probaImage.getPixel(x, y, z)));
        } else if (x+1<mask.sizeX && mask.getPixel(x+1, y, z)>0) res.setX(x+interpolationNext(probaImage.getPixel(x, y, z), probaImage.getPixel(x+1, y, z)));
        if (y>=1 && mask.getPixel(x, y-1, z)>0) {
            if (y+1<mask.sizeY && mask.getPixel(x, y+1, z)>0) {
                res.setY(y+interpolation(probaImage.getPixel(x, y-1, z), probaImage.getPixel(x, y, z), probaImage.getPixel(x, y+1, z)));
            } else res.setY(y+interpolationPrev(probaImage.getPixel(x, y-1, z), probaImage.getPixel(x, y, z)));
        } else if (y+1<mask.sizeY && mask.getPixel(x, y+1, z)>0) res.setY(y+interpolationNext(probaImage.getPixel(x, y, z), probaImage.getPixel(x, y+1, z)));
        if (z>=1 && mask.getPixel(x, y, z-1)>0) {
            if (z+1<mask.sizeZ && mask.getPixel(x, y, z+1)>0) {
                res.setZ(z+interpolation(probaImage.getPixel(x, y, z-1), probaImage.getPixel(x, y, z), probaImage.getPixel(x, y, z+1)));
            } else res.setZ(z+interpolationPrev(probaImage.getPixel(x, y, z-1), probaImage.getPixel(x, y, z)));
        } else if (z+1<mask.sizeZ && mask.getPixel(x, y, z+1)>0) res.setZ(z+interpolationNext(probaImage.getPixel(x, y, z), probaImage.getPixel(x, y, z+1)));
        return res;
    }
    
    private double interpolation(float prev, float cur, float next) { // res >=-0.5 et <=0.5
        if (prev==cur && cur==next && cur==0) return 0;
        double y1, y2;
        if (cur==prev) y1=cur/2f;
        else y1=cur/2f-(cur-prev)/8f;
        if (cur==next) y2=y1+cur;
        else y2=cur/2f+(next-cur)/8f+y1;
        double y = this.randomGenerator.nextDouble()*y2;
        if (y<y1) {
            if (cur>prev) {
                double b=cur/Math.sqrt(2*(cur-prev));
                return (Math.sqrt(y-y1+b*b)-b)/Math.sqrt((cur-prev)/2f);
            } else if (cur<prev) {
                double b=cur/Math.sqrt(2*(prev-cur));
                return (-Math.sqrt(y1-y+b*b)+b)/Math.sqrt((prev-cur)/2f);
            } else return y/cur-0.5f;
        } else if (y>y1) {
            if (next>cur) {
                double b=cur/Math.sqrt(2*(next-cur));
                return (Math.sqrt(y-y1+b*b)-b)/Math.sqrt((next-cur)/2f);
            } else if (next<cur) {
                double b=cur/Math.sqrt(2*(cur-next));
                return (-Math.sqrt(y1-y+b*b)+b)/Math.sqrt((cur-next)/2f);
            } else return y/cur-0.5f;
        } else return 0;
    }
    
    private double interpolationPrev(float prev, float cur) { // res >=-0.5 et <=0.5
        if (cur==0 && prev==cur ) return 0;
        double y1;
        if (cur==prev) y1=cur/2f;
        else y1=cur/2f-(cur-prev)/8f;
        double y = this.randomGenerator.nextDouble()*y1;
        if (cur>prev) {
            double b=cur/Math.sqrt(2*(cur-prev));
            return (Math.sqrt(y-y1+b*b)-b)/Math.sqrt((cur-prev)/2f);
        } else if (cur<prev) {
            double b=cur/Math.sqrt(2*(prev-cur));
            return (-Math.sqrt(y1-y+b*b)+b)/Math.sqrt((prev-cur)/2f);
        } else return y/cur-0.5f;
    }
    
    private double interpolationNext(float cur, float next) { // res >=-0.5 et <=0.5
        if (cur==next && cur==0) return 0;
        double y2;
        if (cur==next) y2=cur;
        else y2=cur/2f+(next-cur)/8f;
        double y = this.randomGenerator.nextDouble()*y2;
        if (next>cur) {
            double b=cur/Math.sqrt(2*(next-cur));
            return (Math.sqrt(y+b*b)-b)/Math.sqrt((next-cur)/2f);
        } else if (next<cur) {
            double b=cur/Math.sqrt(2*(cur-next));
            return (-Math.sqrt(-y+b*b)+b)/Math.sqrt((cur-next)/2f);
        } else return y/cur-0.5f;
    }
    
    @Override
    public boolean isValid() {
        return true;
    }
}
