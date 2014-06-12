package tango.plugin.measurement.distance;

import java.util.HashMap;
import mcib3d.geom.Object3D;
import tango.dataStructure.InputCellImages;
import tango.dataStructure.SegmentedCellImages;
import tango.parameter.ChoiceParameter;
import tango.parameter.ConditionalParameter;
import tango.parameter.Parameter;
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
public class EuclideanDistance extends Distance {
    public static String[] type = new String[] {"Center-Center", "Border-Border", "Center-Border", "Border-Center"};
    ChoiceParameter type_P = new ChoiceParameter("Distance: ", "type", type, type[0]);
    public static String[] inc = new String[] {"Positive distance", "zero", "Negative distance"};
    ChoiceParameter inside = new ChoiceParameter("If inclusion:",  "inclusion", inc, inc[0]);
    HashMap<Object, Parameter[]> map = new HashMap<Object, Parameter[]>(){{
        put(type[1], new Parameter[]{inside});
        put(type[2], new Parameter[]{inside});
        put(type[3], new Parameter[]{inside});
    }};
    ConditionalParameter action = new ConditionalParameter(type_P, map);
    int dType;
    int incl;
    
    public EuclideanDistance(){}
    
    public EuclideanDistance(int type, int inclusion) {
        dType=type;
        incl=inclusion;
    }
    
    
    @Override
    public void initialize(InputCellImages in, SegmentedCellImages out) {
        dType=type_P.getSelectedIndex();
        incl=inside.getSelectedIndex();
    }
    
    

    @Override
    public double distance(Object3D p1, Object3D p2) {
        if (dType>0) { // involves border
          switch(dType) {
            case 1 : // border-border
                if (incl==0) return p1.distBorderUnit(p2);
                else { // si negative distance if inclusion
                    double dist = p1.distBorderUnit(p2);
                    if (dist==0) return dist;
                    else if (p2.hasOneVoxelColoc(p1)) {
                        if (incl==1) return 0;
                        else return -dist;
                    } else return dist;
                    
                }
            case 2 : return getCenterBorder(p1, p2);
            case 3 : return getCenterBorder(p2, p1);
        }  
        } return p1.distCenterUnit(p2);
    }
    
    private double getCenterBorder(Object3D p1, Object3D p2) {
        if (incl==0) return p1.distCenterBorderUnit(p2);
        else {
            double[] center = p1.getCenterAsArray();
            if (p2.inside(center[0], center[1], center[2])) {
                if (incl==1) return 0;
                else return -p1.distCenterBorderUnit(p2);
            } else return p1.distCenterBorderUnit(p2);
        }
    }

    @Override
    public Parameter[] getParameters() {
        return new Parameter[]{action};
    }

    @Override
    public String getHelp() {
        return "Euclidean Distance between two objects";
    }
    
}
