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
public class EdgeContact extends Distance {
    public static String[] type = new String[] {"% of 1st structure surface", "voxel number"};
    ChoiceParameter type_P = new ChoiceParameter("Value: ", "type", type, type[0]);
    public static String[] contact = new String[] {"colocalization", "side contact", "XY diagonal contact", "Z diagonal contact"};
    ChoiceParameter contact_P = new ChoiceParameter("Contact type: ", "contact", contact, contact[3]);
    
    int dType, contactType;
    
    public EdgeContact(){}
    
    public EdgeContact(int type, int contactType_) {
        dType=type;
        contactType = contactType_;
    }
    
    
    @Override
    public void initialize(InputCellImages in, SegmentedCellImages out) {
        dType=type_P.getSelectedIndex();
    }
    
    

    @Override
    public double distance(Object3D p1, Object3D p2) {
        double c =  -p1.edgeContact(p2, contactType);
        if (dType==0) return (double)c / p1.getAreaPixels();
        else return c;
    }
    

    @Override
    public Parameter[] getParameters() {
        return new Parameter[]{type_P, contact_P};
    }

    @Override
    public String getHelp() {
        return "Edge contact between two objects";
    }
    
}
