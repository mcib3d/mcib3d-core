package tango.plugin.segmenter;

import ij.gui.Plot;
import java.util.ArrayList;
import java.util.Collections;
import mcib3d.geom.Object3DVoxels;

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

public class DivideObjects {
    double[] numbers;
    Object3DVoxels[] objects;
    double[] values;
    
    public DivideObjects(Object3DVoxels[] objects, double[] values) {
        this.objects=objects;
        this.values=values;
    }
    
    public double[] getNumbers() {
        return numbers;
    }
    
    public double[] getEffectiveValues() {
        double[] effectiveValues = new double[values.length];
        for (int i = 0; i<values.length; i++) effectiveValues[i]= values[i]/numbers[i];
        return effectiveValues;
    }
    
    public void plot(String title) {
        if (numbers==null || numbers.length!=values.length) return;
        double[] nb = new double[values.length];
        double maxNb =1;
        for (int i = 0; i<objects.length;i++) {
            nb[i]=numbers[i];
            if (nb[i]>maxNb) maxNb=nb[i];
        }
        double minV = values[0], maxV = values[0];
        for (double v:getEffectiveValues()) {
            if (v<minV) minV=v;
            if (v>maxV) maxV=v;
        }
        Plot p = new Plot(title, "number of object", "effective value");
        p.setLimits(0, maxNb+1, 0, maxV*1.05);
        p.addPoints(nb, getEffectiveValues(), Plot.CROSS);
        
        p.show();
    }
    
    public void divideObjects(int nb) {
        numbers = new double[objects.length];
        if (objects.length==0) return;
        ArrayList<ObjectDivide> list = new ArrayList<ObjectDivide>(objects.length);
        for (int i = 0; i<objects.length;i++) {
            ObjectDivide o = new ObjectDivide(objects[i], values[i]);
            list.add(o);
        }
        int count = objects.length;
        while (count<nb) {
            ObjectDivide max = Collections.max(list);
            max.incrementValue();
            count++;
        }
        for (int i = 0; i<objects.length; i++) {
            numbers[i]=list.get(i).div;
            //ij.IJ.log("Object :"+i+ " nb:" +numbers[i]+ " effective value:"+list.get(i).effectiveValue);
        }
    }
    
    public class ObjectDivide implements Comparable<ObjectDivide> {
        Object3DVoxels object;
        double value, effectiveValue;
        int div;
        public ObjectDivide(Object3DVoxels o , double value) {
            this.object=o;
            this.value=value;
            effectiveValue=value;
            this.div=1;
        }
        
        
        public void incrementValue() {
            div++;
            effectiveValue=value/(double)div;
        }
        
        public double getValue() {
            return effectiveValue;
        }
        
        @Override
        public int compareTo(ObjectDivide o) {
            return Double.compare(getValue(), o.getValue());
        }
    }
}
