package tango.mongo;
import com.mongodb.*;

import ij.*;
import java.util.*;
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
public class MongoUtils {

    public static int[] getIntArray(DBObject dbo, String key) {
        //IJ.log("mongoutils:"+dbo.get(key));
        if (!dbo.containsField(key)) return null;
        Object o = dbo.get(key);
        if (o instanceof BasicDBList) {
            BasicDBList list = ((BasicDBList)dbo.get(key));
            int[] res = new int[list.size()];
            for (int i =0; i<list.size(); i++) {
                res[i]=(Integer)(list.get(""+i));
            }
            return res;
        } else if (o instanceof int[]) return (int[])o;
        else return new int[]{(Integer)o};
    }
    
    public static double[] getDoubleArray(DBObject dbo, String key) {
        if (!dbo.containsField(key)) return null;
        BasicDBList list = ((BasicDBList)dbo.get(key));
        double[] res = new double[list.size()];
        for (int i =0; i<list.size(); i++) {
            res[i]=(Double)(list.get(""+i));
        }
        return res;
    }

    public static float[] getFloatArray(DBCursor cur, String key) {
        List<DBObject> list = cur.toArray();
        float[] res = new float[list.size()];
        boolean field=false;
        for (int i =0; i<list.size(); i++) {
            BasicDBObject o = ((BasicDBObject)list.get(i));
            if (o.containsField(key)) {
                field=true;
                res[i]=(float)o.getDouble(key);
            }
        }
        return field?res:null;
    }

    public static float[] getFloatArray(BasicDBList list, String key) {
        float[] res = new float[list.size()];
        boolean field=false;
        for (int i =0; i<list.size(); i++) {
            BasicDBObject o = ((BasicDBObject)list.get(i));
            if (o.containsField(key)) {
                field=true;
                res[i]=(float)o.getDouble(key);
            }
        }
        return field?res:null;
    }
}
