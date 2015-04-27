package tango.spatialStatistics.constraints;

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
public class ConstraintFactory {
    public static String[] constraintNames = new String[]{"", "Distance to Homolog", "Distances to Structure", "Hardcore", "Hardcore to structure"};
    
    public static Constraint getConstraint(String name) {
        Constraint c=null;
        if (name.equals(constraintNames[1])) c= new HomologDistance();
        else if (name.equals(constraintNames[2])) c= new DistancesToStructure();
        else if (name.equals(constraintNames[3])) c= new Hardcore();
        else if (name.equals(constraintNames[4])) c= new HardcoreToStructure();
        return c;        
    }
}
