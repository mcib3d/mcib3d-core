package tango.plugin.measurement;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import mcib3d.geom.Object3D;
import mcib3d.geom.Object3DVoxels;
import mcib3d.image3d.ImageByte;
import tango.dataStructure.InputCellImages;
import tango.dataStructure.SegmentedCellImages;
import tango.dataStructure.StructureQuantifications;
import tango.parameter.*;
import tango.plugin.measurement.Measurement;
import tango.plugin.measurement.*;
import tango.plugin.measurement.distance.Distance;
import tango.processing.geodesicDistanceMap.GeodesicMap;
import tango.util.utils;

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
public class MediatedContact implements MeasurementStructure {
    int nbCPUs=1;
    boolean verbose=false;
    StructureParameter structure1 = new StructureParameter("Structure 1:", "structure1", -1, true);
    StructureParameter structure2 = new StructureParameter("Structure 2:", "structure2", -1, true);
    StructureParameter structureContact = new StructureParameter("Contact mediated by:", "contactStructure", -1, true);
    BooleanParameter doArcDistance = new BooleanParameter("Compute Arc Distance:", "doArcDistance", true);
    DistanceParameter distance = new  DistanceParameter("Distance used for contact", "distance","Euclidean Distance", 
            new Parameter[]{
                new ConditionalParameter(
                    new ChoiceParameter("type", "type", new String[]{"Border-Border"}, "Border-Border"), 
                    new HashMap<Object, Parameter[]>(){{
                        put("Border-Border", new Parameter[]{new ChoiceParameter("If inclusion:",  "inclusion", new String[]{"zero"}, "zero")});
                    }})
            }
        );
    DoubleParameter threshold = new DoubleParameter("Contact Threshold:", "threshold", 0.1d, Parameter.nfDEC5);
    Parameter[] parameters = new Parameter[] {structure1, structure2, structureContact, distance, threshold, doArcDistance};
    
    KeyParameterStructureArrayO2O mediatingObjectIndex = new KeyParameterStructureArrayO2O("Mediating Object index", "mediatingObject", "mediatingObject", true);
    KeyParameterStructureArrayO2O arcDistance = new KeyParameterStructureArrayO2O("Arc Distance", "arcDistance", "arcDistance", true);

    
    
    public MediatedContact () {
        doArcDistance.setFireChangeOnAction();
    }
    
    @Override
    public int[] getStructures() {
        return new int[]{structure1.getIndex(), structure2.getIndex(), structureContact.getIndex()};
    }


    @Override
    public void getMeasure(InputCellImages raw, SegmentedCellImages seg, StructureQuantifications quantifs) {
        if (structure1.getIndex()==structureContact.getIndex() || structureContact.getIndex()==structure2.getIndex()) {
            ij.IJ.log("Mediated contact Warning: contact structure can't be the same as structure 1 or 2");
            return;
        }
        Distance d = distance.getDistance(raw, seg);
        Object3D[] o3 = seg.getObjects(structureContact.getIndex());
        Object3D[] o1 = seg.getObjects(structure1.getIndex());
        Object3D[] o2 = seg.getObjects(structure2.getIndex());
        GeodesicMap map=null;
        ImageByte mask=null;
        if (this.doArcDistance.isSelected()) {
            mask = new ImageByte("mask", raw.getMask().sizeX, raw.getMask().sizeY, raw.getMask().sizeZ);
            mask.setScale(raw.getMask());
            map = new GeodesicMap(mask, null, this.nbCPUs, this.verbose);
        }
        double[] medObjIdx;
        double[] adist=null;
        boolean showedOne=!this.verbose;
        if (structure1.getIndex()==structure2.getIndex()) {
            medObjIdx = new double[o1.length * (o1.length -1 )/ 2];
            if (mask!=null) {
                adist = new double[o1.length * (o1.length -1 )/ 2];
                Arrays.fill(adist, -1);
            }
            HashMap<Integer, ArrayList<Integer>> contacts = getContacts(d, o3, o1);
            int idx1, idx2;
            int lastIdx = -1;
            for (Map.Entry<Integer, ArrayList<Integer>> e : contacts.entrySet()) {
                ArrayList<Integer> list = e.getValue();
                if (list.size()>1) {
                    Collections.sort(list);
                    int key = e.getKey();
                    if (lastIdx>=0) o3[lastIdx].draw(mask, 0);
                    o3[key].draw(mask, 1);
                    lastIdx = key;
                    for (int i1 = 0; i1<list.size()-1; i1++) {
                        idx1 = list.get(i1);
                        if (mask!=null) {
                            map.run(new Object3D[]{o1[idx1]}, false, true);
                            if (!showedOne) {
                                showedOne=true;
                                map.getDistanceMap().showDuplicate("arc distance map. mediating object:"+(key+1)+ " from object:"+(idx1+1));
                            }
                        }
                        for (int i2 = i1+1; i2<list.size(); i2++) {
                            idx2 = list.get(i2);
                            int idx = utils.getIdxSameStructure(idx1, idx2, o1.length);
                            medObjIdx[idx] = key+1;
                            if (mask!=null) adist[idx] = map.getMinDistance(o1[idx2], true);
                        }
                    }
                }
            }
            
        } else {
            medObjIdx = new double[o1.length * o2.length];
            if (mask!=null) {
                adist = new double[o1.length * o2.length];
                Arrays.fill(adist, -1);
            }
            
            HashMap<Integer, ArrayList<Integer>> contacts31 = getContacts(d, o3, o1);
            HashMap<Integer, ArrayList<Integer>> contacts32 = getContacts(d, o3, o2);
            contacts31.keySet().retainAll(contacts32.keySet());
            int lastIdx = -1;
            for (Map.Entry<Integer, ArrayList<Integer>> e : contacts31.entrySet()) {
                ArrayList<Integer> list1 = e.getValue();
                int key = e.getKey();
                if (lastIdx>=0) o3[lastIdx].draw(mask, 0);
                o3[key].draw(mask, 1);
                lastIdx = key;
                ArrayList<Integer> list2 = contacts32.get(key);
                    if (o1.length<=o2.length) {
                        for (int idx1 : list1) {
                            if (mask!=null) {
                                map.run(new Object3D[]{o1[idx1]}, false, true);
                                if (!showedOne) {
                                    showedOne=true;
                                    map.getDistanceMap().showDuplicate("arc distance map. mediating object:"+(key+1)+ " from object:"+(idx1+1) +" of structure:"+structure1.getStructureName());
                                }
                            }
                            for (int idx2 :list2) {
                                int idx = utils.getIdx(idx1, idx2, o2.length);
                                medObjIdx[idx] = key;
                                if (mask!=null) adist[idx] = map.getMinDistance(o2[idx2], true);
                            }
                        }
                    } else {
                        for (int idx2 : list2) {
                            if (mask!=null) {
                                map.run(new Object3D[]{o2[idx2]}, false, true);
                                if (!showedOne) {
                                    showedOne=true;
                                    map.getDistanceMap().showDuplicate("arc distance map. mediating object:"+(key+1)+ " from object:"+(idx2+1) +" of structure:"+structure2.getStructureName());
                                }
                            }
                            for (int idx1 :list1) {
                                int idx = utils.getIdx(idx1, idx2, o2.length);
                                medObjIdx[idx] = key+1;
                                if (mask!=null) adist[idx] = map.getMinDistance(o1[idx1], true);
                            }
                        }
                    }
            }
        }
        if (mediatingObjectIndex.isSelected()) quantifs.setQuantificationStructureArrayO2O(this.mediatingObjectIndex, medObjIdx);
        if (mask!=null && this.arcDistance.isSelected()) quantifs.setQuantificationStructureArrayO2O(arcDistance, adist);
    }
    
    private HashMap<Integer, ArrayList<Integer>> getContacts(Distance d, Object3D[] o1, Object3D[] o2) {
        double[] distances31 = d.getAllInterDistances(o1, o2);
        double thld = threshold.getDoubleValue(0.1);
        HashMap<Integer, ArrayList<Integer>> contact31 = new HashMap<Integer, ArrayList<Integer>>();
        for (int i = 0; i<o1.length; i++) {
            for (int j = 0;j<o2.length; j++) {
                int idx = o2.length * i + j;
                if (distances31[idx]<=thld) {
                    ArrayList<Integer> list;
                    if (!contact31.containsKey(i)) {
                        list = new ArrayList<Integer>(2);
                        contact31.put(i, list);
                    } else {
                        list = contact31.get(i);
                    }
                    list.add(j);
                }
            }
        }
        return contact31;
    }
    

    @Override
    public Parameter[] getParameters() {
        return parameters;
    }
    
    @Override
    public void setMultithread(int nbCPUs) {
        this.nbCPUs=nbCPUs;
    }
    
    @Override
    public void setVerbose(boolean verbose) {
        this.verbose=verbose;
    }


    @Override
    public KeyParameter[] getKeys() {
        if (this.doArcDistance.isSelected()) {
            return new KeyParameterStructureArrayO2O[]{mediatingObjectIndex, arcDistance};
        } else {
            return new KeyParameterStructureArrayO2O[]{mediatingObjectIndex};
        }
    }

    @Override
    public String getHelp() {
        return "Determines if each object from structure 1 is in contact with structure 2 mediated by structure 3. Computes the arc distance between two object in mediated-contact, or -1 if not";
    }

    

    
}
