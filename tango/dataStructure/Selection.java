package tango.dataStructure;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import java.util.*;
import org.bson.types.ObjectId;
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

public class Selection extends BasicDBObject {
    BasicDBObject nuclei;
    public Selection(String name, ObjectId xp) {
        super();
        this.append("name", name);
        this.append("experiment_id", xp);
        this.append("_id", ObjectId.get());
        nuclei = new BasicDBObject();
        this.append("nuclei", nuclei);
        // remove duplicate??
    }
    
    public Selection() {
        super();
        init();
    }
    
    public void init() {
        if (!this.containsField("nuclei")) {
            nuclei = new BasicDBObject();
            this.append("nuclei", nuclei);
        } else {
            nuclei = (BasicDBObject)this.get("nuclei");
        }
    }
    
    public String getName() {
        return this.getString("name");
    }
    
    protected BasicDBObject getNucleus (ObjectId id, boolean create) {
        if (!nuclei.containsField(id.toStringMongod())) {
            if (create) {
                BasicDBObject currentNuc = new BasicDBObject();
                nuclei.append(id.toStringMongod(), currentNuc);
                return currentNuc;
            } else return null;
        } else {
            return (BasicDBObject)nuclei.get(id.toStringMongod());
        }
    }
    
    public int[] getSelectedStructures(ObjectId id) {
        BasicDBObject nuc = getNucleus(id, false);
        if (nuc==null) return new int[0];
        Set<String> keys = nuc.keySet();
        ArrayList<Integer> si = new ArrayList<Integer>(keys.size());
        for (String key : keys) if (key.length()>0) si.add(Integer.parseInt(key));
        int[] selectedIndexes = new int[si.size()];
        for (int i = 0; i<si.size(); i++) selectedIndexes[i]=si.get(i);
        return selectedIndexes;
    }
    
    public ArrayList<Integer> getSelectedObjects(ObjectId id, int structureIdx) {
        BasicDBObject nuc = getNucleus(id, false);
        if (nuc==null) return new ArrayList<Integer>(0);
        else {
            Object o = nuc.get(structureIdx+"");
            if (o instanceof Integer) {
                ArrayList<Integer> listIdx = new ArrayList<Integer>(1);
                listIdx.add((Integer)o);
                return listIdx;
            } else if (o instanceof BasicDBList) {
                BasicDBList list = (BasicDBList)o;
                ArrayList<Integer> listIdx = new ArrayList<Integer>(list.size());
                for (Object i : list) {
                    if (i instanceof Number) listIdx.add(((Number)i).intValue());
                }
                return listIdx;
            } else if (o instanceof ArrayList) return (ArrayList<Integer>)o;
            else return new ArrayList<Integer>(0);
        }
    }
    
    protected void appendToNucleus(ObjectId id, int structure, ArrayList<Integer> selectedObjects) {
        BasicDBObject nucleus = getNucleus(id, true);
        System.out.println("appending structure:"+structure+ " to nucleus:"+id.toStringMongod());
        if (!nucleus.containsField(structure+"")) {
            nucleus.append(structure+"", selectedObjects);
        } else {
            Object o = nucleus.get(structure+"");
            Collection c = (Collection)o;
            selectedObjects.removeAll(c);
            c.addAll(selectedObjects);
        }
        //setNucleus(id, nucleus);
    }
    
    public void appendCells(Cell[] cells) {
        for (Cell c: cells) {
            String id = c.id.toStringMongod();
            if (!nuclei.containsField(id)) nuclei.append(id, new BasicDBObject());
        }
        save();
        //System.out.println("nuclei object: "+nuclei);
        //System.out.println("Selection:"+this);
    }
    
    public void removeCells(Cell[] cells) {
        for (Cell c: cells) {
            String id = c.id.toStringMongod();
            nuclei.removeField(id);
        }
        save();
        //System.out.println("nuclei object: "+nuclei);
        //System.out.println("Selection:"+this);
    }
    
    public void appendToNucleus(ObjectId id, HashMap<Integer, ArrayList<Integer>> selectedObjects) {
        System.out.println("append to nucleus single cells:"+selectedObjects.size());
        if (selectedObjects.isEmpty()) {
            String idm = id.toStringMongod();
            if (!nuclei.containsField(idm)) nuclei.append(idm, new BasicDBObject());
        } else {
            for (Map.Entry<Integer, ArrayList<Integer>> e : selectedObjects.entrySet()) {
                appendToNucleus(id, e.getKey(), e.getValue());
            }
        }
        save();
    }
    
    protected void removeFromNucleus(ObjectId id, int structure, ArrayList<Integer> selectedObjects) {
        BasicDBObject nucleus = getNucleus(id, false);
        if (nucleus==null) return;
        if (nucleus.containsField(structure+"")) {
            Collection list =  (Collection)nucleus.get(structure+"");
            list.removeAll(selectedObjects);
            if (list.isEmpty()) nucleus.remove(structure+"");
        }        
    }
    
    public void removeFromNucleus(ObjectId id, HashMap<Integer, ArrayList<Integer>> selectedObjects) {
        if (selectedObjects.isEmpty()) {
            removeNucleus(id);
            return;
        }
        for (Map.Entry<Integer, ArrayList<Integer>> e : selectedObjects.entrySet()) {
            removeFromNucleus(id, e.getKey(), e.getValue());
        }
        save();
    }
    
    public void removeNucleus(ObjectId id) {
        nuclei.removeField(id.toStringMongod());
        save();
    }
    
    public Set<String> getNuclei() {
        return nuclei.keySet();
    }
    
    public void save() {
        this.append("nuclei", nuclei);
        Core.mongoConnector.saveSelection(this);
    }
    
    public void removeFromDB() {
        Core.mongoConnector.removeSelection(this);
    }
    
    @Override
    public boolean equals(Object o) {
        if (o instanceof Selection) {
            return ((Selection)o).getName().equals(this.getName());
        } else if (o instanceof String) {
            return ((String)o).equals(this.getName());
        } else return false;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 31 * hash + (this.getName() != null ? this.getName().hashCode() : 0);
        return hash;
    }
}
