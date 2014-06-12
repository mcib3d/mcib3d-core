
package tango.gui.util;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import ij.IJ;
import java.util.ArrayList;
import mcib3d.utils.exceptionPrinter;
import tango.dataStructure.Cell;
import tango.dataStructure.Experiment;
import tango.dataStructure.Field;
import tango.gui.Core;
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
public class CellFactory {
    public static Cell[] getCells(Experiment xp) {
        DBCursor cur = xp.getConnector().getXPNuclei(xp.getName());
        Cell[] res = new Cell[cur.count()];
        for (int i = 0; i<res.length; i++) {
            res[i]=new Cell((BasicDBObject)cur.next(), null, xp);
            res[i].createChannels();
        }
        cur.close();
        return res;
    }
    
    public static Cell[] getCells(Field[] fields) {
        int count = 0;
        for (Field f : fields) {
            f.createCells();
            count+=f.getCells().size();
        }
        Cell[] res = new Cell[count];
        count=0;
        for (Field f : fields) {
            for (Cell c : f.getCells()) {
                res[count]=c;
                count++;
            }
        }
        return res;
    }
    
    /*public static Cell[] getCells(Experiment xp) {
        ArrayList<Cell> temp = new ArrayList<Cell>();
        Field[] fields = FieldFactory.getFields(xp);
        for (Field f : fields) {
            if (f.getCells()==null) f.createCells();
            temp.addAll(f.getCells());
        }
        Cell[] res = new Cell[temp.size()];
        res = temp.toArray(res);
        return res;
    }
    * 
    */
    
    public static Cell getOneCell(Experiment xp) {
        try {
            DBObject cell = xp.getConnector().getOneNucleus(xp.getId());
            if (cell!=null) {
                Cell c = new Cell((BasicDBObject) cell, null, xp);
                
                c.createChannels();
                return c;
            }
        } catch (Exception e) {
            exceptionPrinter.print(e, "", Core.GUIMode);
        }
        return null;
    }
}
