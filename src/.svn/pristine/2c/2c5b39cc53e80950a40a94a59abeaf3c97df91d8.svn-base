/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mcib3d.image3d.IterativeThresholding;

import java.util.ArrayList;
import mcib3d.geom.Object3D;

/**
 *
 **
 * /**
 * Copyright (C) 2008- 2012 Thomas Boudier and others
 *
 *
 *
 * This file is part of mcib3d
 *
 * mcib3d is free software; you can redistribute it and/or modify it under the
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
 * @author thomas
 */
public class Criterion {

    int measure;
    double minValue;
    double maxValue;

    public Criterion(int measure, double minValue, double maxValue) {
        this.measure = measure;
        this.minValue = minValue;
        this.maxValue = maxValue;
    }

    public boolean checkCriterion(Object3D obj) {
        double val = obj.getMeasure(measure);
        return ((val >= minValue) && (val <= maxValue));
    }

    public double distance(Object3D obj) {
        if (checkCriterion(obj)) {
            return 0;
        } else {
            double val = obj.getMeasure(measure);
            if (val < minValue) {
                return minValue - val;
            } else {
                return val - maxValue;
            }
        }
    }
    
    public ArrayList<Double> getArrayListMinMax(){
        ArrayList<Double> list=new ArrayList();
        list.add(new Double(minValue));
        list.add(new Double(maxValue));
        
        return list;
    }
}
