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
public class Classification {

    ArrayList<Criterion> criterions;

    public Classification() {
        criterions = new ArrayList<Criterion>();
    }

    public void addCriterion(Criterion crit) {
        criterions.add(crit);
    }

    public boolean checkClassification(Object3D obj) {
        for (Criterion crit : criterions) {
            if (!crit.checkCriterion(obj)) {
                return false;
            }
        }
        return true;
    }

    public double distance(Object3D obj) {
        double dist = 0;
        for (Criterion crit : criterions) {
            dist += crit.distance(obj);
        }
        return dist;
    }
    
    public ArrayList<Double> getArrayList(){
        ArrayList<Double> list=new ArrayList();
        for(int i=0;i<criterions.size();i++){
            list.addAll(criterions.get(i).getArrayListMinMax());
        }
        return list;
    }
}
