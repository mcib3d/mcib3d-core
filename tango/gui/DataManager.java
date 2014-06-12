package tango.gui;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCursor;
import ij.IJ;
import ij.measure.ResultsTable;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.HashMap;
import java.util.Locale;
import java.util.TreeMap;
import java.util.TreeSet;
import mcib3d.utils.exceptionPrinter;
import org.bson.types.ObjectId;
import tango.dataStructure.Experiment;
import tango.mongo.MultiKey2D;
import tango.mongo.MultiKey3D;
import tango.mongo.MultiKey4D;
import tango.mongo.MongoConnector;
import tango.plugin.measurement.MeasurementSequence;
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
public class DataManager {

    MongoConnector mc;
    Core core;
    Experiment xp;
    HashMap<Integer, TreeSet<String>> ojectKeys;
    HashMap<MultiKey, TreeSet<String>> c2cKeys;
    HashMap<Integer, TreeMap<MultiKey3D, String>> objectMes;
    HashMap<MultiKey, TreeMap<MultiKey4D, String>> o2oMes;
    TreeMap<MultiKey2D, int[]> nbObjects;
    String[] channelNames;
    TreeMap<MultiKey2D, Integer> nucTags;
    TreeMap<MultiKey2D, String> nucIds;

    public DataManager(Core core, Experiment xp) {
        this.mc = xp.getConnector();
        this.core = core;
        this.xp = xp;
    }

    public void extractData(File dir) {
        getKeys();
        try {
            extractData();
        } catch (Exception e) {
            exceptionPrinter.print(e, "", Core.GUIMode);
        }
        for (int i = 0; i < channelNames.length; i++) {
            //File f = new File(dir.getAbsolutePath() + File.separator + channelNames[i] + ".csv");
            //writeObjects(f, i, false);// CSV R format
            File f = new File(dir.getAbsolutePath() + File.separator + channelNames[i] + ".xls");
            writeObjects(f, i, true);// XLS IJ FORMAT
            try {
                ResultsTable res = ResultsTable.open(f.getAbsolutePath());
                if (res != null) {
                    res.show("Results_" + channelNames[i]);
                }
            } catch (IOException ex) {
                IJ.log("Pb reading results " + f.getName());
            }
        }
        for (MultiKey dk : c2cKeys.keySet()) {
            if (dk.getKey(0) < 0) {
                continue;
            }
            // csv R format
            //File f = new File(dir.getAbsolutePath() + File.separator + channelNames[dk.getKey(0)] + "_" + channelNames[dk.getKey(1)] + ".csv");
            //writeObjects2Objects(f, dk, false);
            // excel IJ format
            File f = new File(dir.getAbsolutePath() + File.separator + channelNames[dk.getKey(0)] + "_" + channelNames[dk.getKey(1)] + ".xls");
            writeObjects2Objects(f, dk, true);// XLS IJ FORMAT
            try {
                ResultsTable res = ResultsTable.open(f.getAbsolutePath());
                if (res != null) {
                    res.show("Results_" + channelNames[dk.getKey(0)] + "_" + channelNames[dk.getKey(1)]);
                }
            } catch (IOException ex) {
                IJ.log("Pb reading results " + f.getName());
            }
        }
    }

    private void extractData() {
        HashMap<MultiKey, TreeSet<String>> newC2CKeys = new HashMap<MultiKey, TreeSet<String>>();
        TreeSet<String> NucKeysToAdd = new TreeSet<String>();
        DBCursor cur = mc.getXPNuclei(xp.getName());
        cur.sort(new BasicDBObject("field_id", 1).append("idx", 1));
        int nbNuc = cur.count();
        IJ.log("extract data nb nuc:" + nbNuc);
        objectMes = new HashMap<Integer, TreeMap<MultiKey3D, String>>(ojectKeys.size());
        nbObjects = new TreeMap<MultiKey2D, int[]>();
        nucTags = new TreeMap<MultiKey2D, Integer>();
        nucIds = new TreeMap<MultiKey2D, String>();
        for (int i : ojectKeys.keySet()) {
            if (i < 0) {
                continue;
            }
            objectMes.put(i, new TreeMap<MultiKey3D, String>());
        }
        if (!ojectKeys.containsKey(0)) {
            objectMes.put(0, new TreeMap<MultiKey3D, String>());
            ojectKeys.put(0, new TreeSet<String>());
        }
        o2oMes = new HashMap<MultiKey, TreeMap<MultiKey4D, String>>();
        for (MultiKey dk : c2cKeys.keySet()) {
            o2oMes.put(dk, new TreeMap<MultiKey4D, String>());
            newC2CKeys.put(dk, new TreeSet<String>());
        }
        while (cur.hasNext()) {
            BasicDBObject nuc = (BasicDBObject) cur.next();
            if (nuc.getInt("tag", 0)<0) continue; // exclude negative tags
            ObjectId nucId = (ObjectId) nuc.get("_id");
            int nucIdx = nuc.getInt("idx");
            String fieldName = mc.getField((ObjectId) nuc.get("field_id")).getString("name");
            int[] nbPart = new int[channelNames.length];
            //mesure objects
            for (int i = 0; i < channelNames.length; i++) {
                TreeMap<MultiKey3D, String> omes = objectMes.get(i);
                TreeSet<String> keys = ojectKeys.get(i);
                DBCursor cursor = mc.getObjectsCursor(nucId, i);
                cursor.sort(new BasicDBObject("idx", 1));
                nbPart[i] = cursor.count();
                if (keys != null && !keys.isEmpty()) {
                    while (cursor.hasNext()) {
                        BasicDBObject o = (BasicDBObject) cursor.next();
                        //IJ.log("o="+o);
                        //IJ.log("omes="+omes);
                        //IJ.log("f="+fieldName+" "+nucIdx+" "+o.getInt("idx")+" "+keys);
                        for (String k : keys) {
                            //IJ.log("k="+k+" "+o.getString(k));
                            if (o.getString(k) != null) {
                                omes.put(new MultiKey3D(fieldName, nucIdx, o.getInt("idx"), k), o.get(k).toString());
                            }
                        }
                    }
                }
                cursor.close();
            }
            String s = "";
            for (int i : nbPart) {
                s += i + ";";
            }
            IJ.log("nb objects:" + s);
            MultiKey2D k2D = new MultiKey2D(fieldName, nucIdx, "nbParts");
            nbObjects.put(k2D, nbPart);
            nucTags.put(k2D, nuc.getInt("tag", 0));
            nucIds.put(k2D, nuc.getString("_id"));
            //C2C
            TreeMap<MultiKey3D, String> nucMes = objectMes.get(0);
            for (MultiKey dk : c2cKeys.keySet()) {
                if (dk.getKey(0) < 0) {
                    continue;
                }
                int size = (dk.getKey(0) != dk.getKey(1)) ? nbPart[dk.getKey(0)] * nbPart[dk.getKey(1)] : nbPart[dk.getKey(0)] * (nbPart[dk.getKey(0)] - 1) / 2;
                BasicDBObject mes = mc.getMeasurementStructure(nucId, dk.getKeys(), true);
                IJ.log("get mes:" + dk + " mes");
                TreeMap<MultiKey4D, String> o2oMesDk = o2oMes.get(dk);
                TreeSet<String> keys = c2cKeys.get(dk);
                TreeSet<String> newKeys = newC2CKeys.get(dk);

                for (String k : keys) {
                    Object o = mes.get(k);
                    if (o instanceof BasicDBList) {
                        BasicDBList list = ((BasicDBList) o);
                        if (list.size() == size) {
                            int count = 0;
                            if (dk.getKey(0) != dk.getKey(1)) {
                                for (int p1 = 1; p1 <= nbPart[dk.getKey(0)]; p1++) {
                                    for (int p2 = 1; p2 <= nbPart[dk.getKey(1)]; p2++) {
                                        o2oMesDk.put(new MultiKey4D(fieldName, nucIdx, p1, p2, k), list.get(count).toString());
                                        count++;
                                    }
                                }
                            } else {
                                for (int p1 = 1; p1 < nbPart[dk.getKey(0)]; p1++) {
                                    for (int p2 = p1 + 1; p2 <= nbPart[dk.getKey(1)]; p2++) {
                                        o2oMesDk.put(new MultiKey4D(fieldName, nucIdx, p1, p2, k), list.get(count).toString());
                                        count++;

                                    }
                                }
                            }
                            newKeys.add(k);
                        }
                    } else if (o instanceof Number || o instanceof String) {
                        String newKey = channelNames[dk.getKey(0)] + "." + channelNames[dk.getKey(1)] + "." + k;
                        nucMes.put(new MultiKey3D(fieldName, nucIdx, 1, newKey), o.toString());
                        NucKeysToAdd.add(newKey);
                    }
                }
            }
        }
        cur.close();
        this.ojectKeys.get(0).addAll(NucKeysToAdd);
        this.c2cKeys = newC2CKeys;
    }

    private void getKeys() {
        try {
            channelNames = xp.getStructureNames(true);
            ojectKeys = xp.getObjectKeys();
            c2cKeys = xp.getC2CKeys();
        } catch (Exception e) {
            exceptionPrinter.print(e, "", Core.GUIMode);
        }
    }

    private void writeObjects(File output, int channel, boolean IJformat) {
        NumberFormat nfDEC3 = DecimalFormat.getNumberInstance(Locale.ENGLISH);
        nfDEC3.setMinimumFractionDigits(3);
        nfDEC3.setMaximumFractionDigits(3);
        try {
            FileWriter fstream = new FileWriter(output);
            BufferedWriter out = new BufferedWriter(fstream);
            TreeMap<MultiKey3D, String> mes = objectMes.get(channel);
            TreeSet<String> keys = ojectKeys.get(channel);
            if (mes == null || mes.isEmpty()) {
                return;
            }
            String delimiter = IJformat ? "\t" : ";";
            String headers;
            if (!IJformat) {
                headers = "nucId" + delimiter + "field" + delimiter + "nuc.idx" + delimiter + "idx" + delimiter;
            } else {
                headers = " " + delimiter + "Label" + delimiter + "nuc.idx" + delimiter + "idx" + delimiter;
            }
            if (channel == 0) {
                headers += "tag" + delimiter;
                for (int i = 0; i < channelNames.length; i++) {
                    headers += "nbObjects." + channelNames[i] + delimiter;
                }
            }
            for (String k : keys) {
                headers += k + delimiter;
            }
            // remove last delimiter
            headers = headers.substring(0, headers.length() - delimiter.length());
            out.write(headers);
            out.newLine();
            int c = 1;
            for (MultiKey2D key : nbObjects.keySet()) {
                int[] nbO = nbObjects.get(key);
                for (int i = 1; i <= nbO[channel]; i++) {
                    String line;
                    if (!IJformat) {
                        line = nucIds.get(key) + delimiter + key.fieldName + delimiter + key.nucIdx + delimiter + i + delimiter;
                    } else {
                        line = c + delimiter + key.fieldName + delimiter + key.nucIdx + delimiter + i + delimiter;
                        c++;
                    }
                    if (channel == 0) {
                        line += nucTags.get(key) + delimiter;
                        for (int n : nbO) {
                            line += n + delimiter;
                        }
                    }
                    for (String k : keys) {
                        String m = mes.get(new MultiKey3D(key.fieldName, key.nucIdx, i, k));
                        line += m == null ? "NA" + delimiter : m + delimiter;
                    }
                    // remove last delimiter
                    line = line.substring(0, line.length() - delimiter.length());
                    out.write(line);
                    out.newLine();
                }
            }
            out.close();
        } catch (Exception e) {
            exceptionPrinter.print(e, "", Core.GUIMode);
        }
    }

    private void writeObjects2Objects(File output, MultiKey dk, boolean IJformat) {
        //IJ.log("write o2o:" + dk);
        String delimiter = IJformat ? "\t" : ";";
        try {
            TreeMap<MultiKey4D, String> mes = o2oMes.get(dk);
            TreeSet<String> keys = c2cKeys.get(dk);
            if (mes == null || mes.isEmpty()) {
                return;
            }
            FileWriter fstream = new FileWriter(output);
            BufferedWriter out = new BufferedWriter(fstream);
            String headers;
            if (!IJformat) {
                headers = "nucId" + delimiter + "field" + delimiter + "nuc.idx" + delimiter + "idx1" + delimiter + "idx2" + delimiter;
            } else {
                headers = " " + delimiter + "Label" + delimiter + "nuc.idx" + delimiter + "idx1" + delimiter + "idx2" + delimiter;
            }
            for (String k : keys) {
                headers += k + delimiter;
            }
            // remove last delimiter
            headers = headers.substring(0, headers.length() - delimiter.length());
            out.write(headers);
            out.newLine();
            int c = 1;
            for (MultiKey2D key : nbObjects.keySet()) {
                int nb1 = nbObjects.get(key)[dk.getKey(0)];
                int nb2 = nbObjects.get(key)[dk.getKey(1)];
                if (dk.getKey(0) != dk.getKey(1)) {
                    for (int i = 1; i <= nb1; i++) {
                        for (int j = 1; j <= nb2; j++) {
                            String line;
                            if (!IJformat) {
                                line = nucIds.get(key) + delimiter + key.fieldName + delimiter + key.nucIdx + delimiter + i + delimiter + j + delimiter;
                            } else {
                                line = c + delimiter + key.fieldName + delimiter + key.nucIdx + delimiter + i + delimiter + j + delimiter;
                                c++;
                            }
                            for (String k : keys) {
                                String m = mes.get(new MultiKey4D(key.fieldName, key.nucIdx, i, j, k));
                                line += m == null ? "NA" + delimiter : m + delimiter;
                            }
                            // remove last delimiter
                            line = line.substring(0, line.length() - delimiter.length());
                            out.write(line);
                            out.newLine();
                        }
                    }
                } else {
                    for (int i = 1; i < nb1; i++) {
                        for (int j = i + 1; j <= nb1; j++) {
                            String line;
                            if (!IJformat) {
                                line = nucIds.get(key) + delimiter + key.fieldName + delimiter + key.nucIdx + delimiter + i + delimiter + j + delimiter;
                            } else {
                                line = c + delimiter + key.fieldName + delimiter + key.nucIdx + delimiter + i + delimiter + j + delimiter;
                                c++;
                            }
                            for (String k : keys) {
                                String m = mes.get(new MultiKey4D(key.fieldName, key.nucIdx, i, j, k));
                                line += m == null ? "NA" + delimiter : m + delimiter;
                            }
                            // remove last delimiter
                            line = line.substring(0, line.length() - delimiter.length());
                            out.write(line);
                            out.newLine();
                        }
                    }
                }
            }
            out.close();
        } catch (Exception e) {
            exceptionPrinter.print(e, "", Core.GUIMode);
        }
    }

    private void writeC2CMisc(File output, MultiKey dk, String key, String delimiter) {
        try {
            DBCursor cur = mc.getXPNuclei(xp.getName());
            cur.sort(new BasicDBObject("field_id", 1).append("idx", 1));
            int nbNuc = cur.count();
            FileWriter fstream = new FileWriter(output);
            BufferedWriter out = new BufferedWriter(fstream);
            String headers = "nucId" + delimiter + "field" + delimiter + "nuc.idx" + delimiter + "idx" + delimiter + key;
            out.write(headers);
            out.newLine();
            while (cur.hasNext()) {
                BasicDBObject nuc = (BasicDBObject) cur.next();
                ObjectId nucId = (ObjectId) nuc.get("_id");
                int nucIdx = nuc.getInt("idx");
                String fieldName = mc.getField((ObjectId) nuc.get("field_id")).getString("name");
                String line = nucId + delimiter + fieldName + delimiter + nucIdx + delimiter;
                //C2C
                BasicDBObject mes = mc.getMeasurementStructure(nucId, dk.getKeys(), true);
                Object o = mes.get(key);
                if (o instanceof BasicDBList) {
                    BasicDBList list = ((BasicDBList) o);
                    for (int i = 0; i < list.size(); i++) {
                        out.write(line + i + delimiter + list.get(i));
                        out.newLine();
                    }
                } else if (o instanceof Number || o instanceof String) {
                    out.write(line + "1" + delimiter + o.toString());
                    out.newLine();
                }
            }
            out.close();
            cur.close();
        } catch (Exception e) {
            exceptionPrinter.print(e, "extract key: " + key, Core.GUIMode);
        }
    }
}
