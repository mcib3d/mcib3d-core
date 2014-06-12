package tango.plugin.measurement;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import tango.dataStructure.Cell;
import tango.dataStructure.Structure;
import ij.IJ;
import ij.ImagePlus;
import java.util.*;
import mcib3d.image3d.ImageHandler;
import mcib3d.geom.Object3D;
import mcib3d.utils.exceptionPrinter;
import tango.mongo.DoubleKey;
import tango.mongo.MongoConnector;
import org.bson.types.ObjectId;
import tango.dataStructure.*;
import tango.gui.Core;
import tango.parameter.GroupKeyParameter;
import tango.parameter.KeyParameter;
import tango.parameter.KeyParameterObjectNumber;
import tango.parameter.Parameter;
import tango.plugin.PluginFactory;
import tango.util.MultiKey;
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
public class MeasurementSequence {
    HashMap<MeasurementKey, ArrayList<String>> keys;
    ArrayList<MeasurementStructure> mS;
    ArrayList<MeasurementObject> mO;
    boolean override;
    
    public MeasurementSequence(Experiment xp, BasicDBList list, boolean override) {
        this.override = override;
        if (list == null) {
            return;
        }
        mS = new ArrayList<MeasurementStructure>(list.size());
        mO = new ArrayList<MeasurementObject>(list.size());
        for (int i = 0; i < list.size(); i++) {
            addMeasurement(list.get(i));
        }
        //ij.IJ.log("list:"+list.size()+ " mC2C:"+mS.size()+ " mO:"+mO.size());
    }
    
    protected void addMeasurement(Object o) {
        if (o != null) {
            BasicDBObject data = (BasicDBObject) o;
            Measurement m = PluginFactory.getMeasurement(data.getString("method"));
            if (m != null) {
                for (Parameter p : m.getParameters()) {
                    //System.out.println("parameter:"+p.getClass()+ " id:"+p.getId()+ " label:"+p.getLabel());
                    p.dbGet(data);
                }
                Parameter[] keys = m.getKeys();
                Object kData=data.get("keys");
                if (keys!=null && kData!=null) {
                    BasicDBObject keysData=(BasicDBObject)kData;
                    for (Parameter p : keys) p.dbGet(keysData);
                }
                if (m instanceof MeasurementObject) {
                    mO.add((MeasurementObject) m);
                } else if (m instanceof MeasurementStructure) {
                    mS.add((MeasurementStructure) m);
                }

            }
        }
    }
    
    // constructor for testing
    public MeasurementSequence(Experiment xp, BasicDBList list, int idx) {
        override=true;
        if (list == null || idx>=list.size()) {
            return;
        }
        mS = new ArrayList<MeasurementStructure>(list.size());
        mO = new ArrayList<MeasurementObject>(list.size());
        addMeasurement(list.get(idx));
    }
    
    private void computeKeys() {
        keys = new HashMap<MeasurementKey, ArrayList<String>>();
        for (MeasurementStructure m : mS) {
            int[] s = m.getStructures();
            ArrayList<KeyParameter> list = new ArrayList<KeyParameter>();
            KeyParameter.addToKeyList(m.getKeys(), list);
            for (KeyParameter k : list) {
                if (k.isSelected()) {
                    MeasurementKey mk = new MeasurementKey(s, k.getType());
                    ArrayList<String> names = keys.get(mk);
                    if (names==null) {
                         names=new ArrayList<String>();
                         keys.put(mk, names);
                    }
                    names.add(k.getKey());
                }
            }
        }
        for (MeasurementObject m : mO) {
            int s = m.getStructure();
            MeasurementKey mk = new MeasurementKey(new int[]{s}, MeasurementObject.Number);
            ArrayList<String> names = keys.get(mk);
            if (names==null) {
                names=new ArrayList<String>();
                keys.put(mk, names);
            }
            ArrayList<KeyParameter> list = new ArrayList<KeyParameter>();
            KeyParameter.addToKeyList(m.getKeys(), list);
            for (KeyParameter k : list) {
                if (k.isSelected()) {
                    //System.out.println("structure:"+s+ " addKey:"+k.getKey());
                    names.add(k.getKey());
                }
            }
        }
    }
    
    public HashMap<MeasurementKey, ArrayList<String>> getKeys() {
        if (keys==null) computeKeys();
        return keys;
    }

    public void run(Cell cell, MongoConnector mc) {
        for (int i = 1; i < cell.getNbStructures(true); i++) {
            Structure ch = (Structure) cell.getStructure(i);
            boolean change = ch.shiftObjectIndexes(true);
            if (change && !override && !cell.getVerbose()) mc.removeStructureMeasurements(cell.getId(), i);
        }
        if (override && !cell.getVerbose()) {
            for (int c = 0; c < cell.getNbStructures(true); c++) {
                mc.removeStructureMeasurements(cell.getId(), c);
            }
        }
        mesureObjects(cell, mc, cell.getRawImages(), cell.getSegmentedImages());
        mesureStructure(cell, mc, cell.getRawImages(), cell.getSegmentedImages());
    }

    private void mesureObjects(Cell cell, MongoConnector mc, InputCellImages raw, SegmentedCellImages seg) {
        HashMap<Integer, HashMap<Integer, BasicDBObject>> channelObjects = new HashMap<Integer, HashMap<Integer, BasicDBObject>>();
        HashMap<Integer, ObjectQuantifications> mes = new HashMap<Integer, ObjectQuantifications>();
        //getmesurements..
        for (int channel = 0; channel < cell.getNbStructures(true); channel++) {
            channelObjects.put(channel, mc.getObjects(cell.getId(), channel));
        }
        Iterator<MeasurementObject> it = mO.iterator();
        while (it.hasNext()) {
            MeasurementObject m = it.next();
            //System.out.println("Measure:"+m.getClass()+ " verbose"+cell.getVerbose());
            it.remove();
            try {
                int channel = m.getStructure();
                HashMap<Integer, BasicDBObject> objects = channelObjects.get(channel);

                if (!override && objects != null && !objects.isEmpty()) {
                    setIgnoredKeys(objects.values(), m.getKeys());
                }
                m.setVerbose(cell.getVerbose());
                m.setMultithread(cell.getNbCPUs());
                ObjectQuantifications quantifs = mes.get(channel);
                if (quantifs == null) {
                    Object3D[] os = seg.getObjects(channel);
                    if (os!=null) {
                        quantifs = new ObjectQuantifications(os.length);
                        mes.put(channel, quantifs);
                    } 
                }
                if (quantifs!=null) m.getMeasure(raw, seg, quantifs);
                
            } catch (Exception e) {
                exceptionPrinter.print(e, "measure Object cell:"+cell.getName(), Core.GUIMode);
            }
        }
        if (cell.getVerbose()) return;
        //save to db
        for (int structure = 0; structure < cell.getNbStructures(true); structure++) {
            if (structure==0) { // measurement nucleus
                ObjectQuantifications quantifs = mes.get(structure);
                if (quantifs == null) {
                    quantifs = new ObjectQuantifications(1);
                    mes.put(0, quantifs);
                }
                for (int s = 1; s < cell.getNbStructures(true); s++) {
                    int nb = 0;
                    if (seg.getObjects(s)!=null) nb= seg.getObjects(s).length;
                    quantifs.setQuantificationObjectNumber(new KeyParameterObjectNumber("", "", "objectNumber_"+s, true), new double[]{nb});
                    //ij.IJ.log("Structure: "+s+ " object number"+seg.getObjects(s).length);
                }
            }
            // virtual structure object number
            if (structure>=cell.getNbStructures(false)) {
                AbstractStructure ass = cell.getStructure(structure);
                if (ass instanceof VirtualStructureObjectNumber) {
                    if (seg.getObjects(structure)==null) continue;
                    int nb = seg.getObjects(structure).length; // creates the objects if not created
                    ObjectQuantifications quantifs = mes.get(structure);
                    if (quantifs == null) {
                        quantifs = new ObjectQuantifications(nb);
                        mes.put(structure, quantifs);
                    }
                    
                    VirtualStructureObjectNumber vs = (VirtualStructureObjectNumber)ass;
                    quantifs.setQuantificationObjectNumber(new KeyParameterObjectNumber("", "", "objectNumber", true), vs.getObjectNumbers(true));
                    quantifs.setQuantificationObjectNumber(new KeyParameterObjectNumber("", "", "inputObjectIdx", true), vs.getObjectIdx());
                    quantifs.setQuantificationObjectNumber(new KeyParameterObjectNumber("", "", "effectiveValue", true), vs.getEffectiveValues(true));
                    double[] structureIdx = new double[nb];
                    Arrays.fill(structureIdx, (double)vs.getInputStructure());
                    quantifs.setQuantificationObjectNumber(new KeyParameterObjectNumber("", "", "inputStructureIdx", true), structureIdx);
                }
            }
            
            ObjectQuantifications quantifs = mes.get(structure);
            if (quantifs != null && !quantifs.getQuantifObject().isEmpty()) {
                
                HashMap<String, Object> map = quantifs.getQuantifObject();
                Set<String> keys = map.keySet();
                HashMap<Integer, BasicDBObject> objects = channelObjects.get(structure);
                boolean save = false;
                for (String key : keys) {
                    Object ob = map.get(key);
                    if (ob!=null) {
                        save = true;
                        if (ob instanceof double[]) {
                            double[] values = (double[])ob;
                            for (int i = 0; i < values.length; i++) {
                                BasicDBObject o = objects.get(i + 1);
                                if (o != null) {
                                    o.append(key, values[i]);
                                } else {
                                    objects.put(i + 1, new BasicDBObject("nucleus_id", cell.getId()).append("experiment_id", cell.getExperiment().getId()).append("channelIdx", structure).append("idx", i + 1).append(key, values[i]));
                                }
                            }
                        } else if (ob instanceof int[]) {
                            int[] values = (int[])ob;
                            for (int i = 0; i < values.length; i++) {
                                BasicDBObject o = objects.get(i + 1);
                                if (o != null) {
                                    o.append(key, values[i]);
                                } else {
                                    objects.put(i + 1, new BasicDBObject("nucleus_id", cell.getId()).append("experiment_id", cell.getExperiment().getId()).append("channelIdx", structure).append("idx", i + 1).append(key, values[i]));
                                }
                            }
                        }
                    }
                }
                if (save) {
                    for (BasicDBObject o : objects.values()) {
                        //IJ.log("save object:"+o);
                        mc.saveObject3D(o);
                    }
                }
            } else { //channels with no measurements
                //IJ.log("no measure on structure:"+structure);
                HashMap<Integer, BasicDBObject> objects = channelObjects.get(structure);
                Object3D[] ob = seg.getObjects(structure);
                if (ob != null && ob.length>0) {
                    int nbPart = ob.length;
                    //IJ.log("nbParts:"+nbPart);
                    if (nbPart != objects.size() || objects==null) { //create objects...
                        for (int i = 1; i <= nbPart; i++) {
                            if (!objects.containsKey(i)) {
                                BasicDBObject o = new BasicDBObject("nucleus_id", cell.getId()).append("experiment_id", cell.getExperiment().getId()).append("channelIdx", structure).append("idx", i);
                                mc.saveObject3D(o);
                                //IJ.log("save object:"+o);
                            }
                        }
                    }
                }
            }
        }
    }

    private void mesureStructure(Cell cell, MongoConnector mc, InputCellImages raw, SegmentedCellImages seg) {
        if (mS==null || mS.isEmpty()) return;
        HashMap<MultiKey, StructureQuantifications> mesC2C = new HashMap<MultiKey, StructureQuantifications>();
        HashMap<MultiKey, BasicDBObject> C2C = new HashMap<MultiKey, BasicDBObject>();
        Iterator<MeasurementStructure> it = mS.iterator();
        while (it.hasNext()) {
            MeasurementStructure m = it.next();
            it.remove();
            try {
                m.setVerbose(cell.getVerbose());
                m.setMultithread(cell.getNbCPUs());
                MultiKey key = new MultiKey(m.getStructures());
                BasicDBObject dbo = C2C.get(key);
                if (dbo == null) {
                    dbo = mc.getMeasurementStructure(cell.getId(), key.getKeys(), true);
                    C2C.put(key, dbo);
                }
                if (!override) setIgnoredKeys(dbo, m.getKeys());
                StructureQuantifications quantifs = mesC2C.get(key);
                if (quantifs == null) {
                    if (key.getKeys().length==1 || (key.getKeys().length>1 && key.getKeys()[0]==key.getKeys()[1]) ) { // same structure
                        Object3D[] os = seg.getObjects(key.getKeys()[0]);
                        quantifs= new StructureQuantifications(os.length);
                    } else if (key.getKeys().length>1) {
                        Object3D[] os1 = seg.getObjects(key.getKeys()[0]);
                        Object3D[] os2 = seg.getObjects(key.getKeys()[1]);
                        quantifs= new StructureQuantifications(os1.length, os2.length);
                    }
                    if (quantifs!=null) mesC2C.put(key, quantifs);
                }
                if (quantifs!=null) m.getMeasure(raw, seg, quantifs);
                
            } catch (Exception e) {
                exceptionPrinter.print(e, "measure C2C cell:"+cell.getName(), Core.GUIMode);
            }
        }
        if (cell.getVerbose()) return;
        Set<MultiKey> channels = mesC2C.keySet();
        for (MultiKey key : channels) {
            BasicDBObject dbo = C2C.get(key);
            boolean save = false;
            StructureQuantifications q = mesC2C.get(key);
            if (q != null) {
                HashMap<String, Object> mapC2C = q.getQuantifStructure();
                save = true;
                for (String k : mapC2C.keySet()) {
                    dbo.put(k, mapC2C.get(k));
                }
            }
            if (save) {
                mc.saveStructureMeasurement(dbo);
            }
        }
    }

    public boolean isEmpty() {
        return (mO == null || mO.isEmpty()) && (mS == null || mS.isEmpty());
    }

    private void setIgnoredKeys(BasicDBObject o, Parameter[] keys) {
        for (Parameter p : keys) {
            if (p instanceof KeyParameter) setIgnoredKey(o, (KeyParameter)p);
            else if (p instanceof GroupKeyParameter) {
                if (((GroupKeyParameter)p).isLocked()) {
                    boolean doAll=false;
                    for (KeyParameter key : ((GroupKeyParameter)p).getKeys()) {
                        setIgnoredKey(o, key);
                        if (key.isSelected()) {
                            doAll=true;
                            break;
                        }
                    }
                    ((GroupKeyParameter)p).setSelected(doAll);
                } else for (KeyParameter key : ((GroupKeyParameter)p).getKeys()) setIgnoredKey(o, key);
            }
        }
    }
    
    private void setIgnoredKey(BasicDBObject o, KeyParameter key) {
        if (key.isSelected()) {
            if (o.containsField(key.getKey())) {
                key.setSelected(false);
                //IJ.log("measurement sequence set ignored keys objects: "+keys[i].getKey()+ " "+false);
            }
        }
    }
    
    private void setIgnoredKeys(Collection<BasicDBObject> os, Parameter[] keys) {
        for (Parameter p : keys) {
            if (p instanceof KeyParameter) setIgnoredKey(os, (KeyParameter)p);
            else if (p instanceof GroupKeyParameter) {
                if (((GroupKeyParameter)p).isLocked()) {
                    boolean doAll=false;
                    for (KeyParameter key : ((GroupKeyParameter)p).getKeys()) {
                        setIgnoredKey(os, key);
                        if (key.isSelected()) {
                            doAll=true;
                            break;
                        }
                    }
                    ((GroupKeyParameter)p).setSelected(doAll);
                } else for (KeyParameter key : ((GroupKeyParameter)p).getKeys()) setIgnoredKey(os, key);
            }
        }
    }    
    
    private void setIgnoredKey(Collection<BasicDBObject> os, KeyParameter key) {
        if (key.isSelected()) {
            boolean doMeas = false;
            String k = key.getKey();
            for (BasicDBObject o : os) {
                if (!o.containsField(k)) {
                    doMeas=true;
                    break;
                }
            }
            key.setSelected(doMeas);
        }
    }
    
    
}
