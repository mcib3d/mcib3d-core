package tango.dataStructure;

import tango.plugin.filter.PostFilterSequence;
import tango.plugin.segmenter.SpotSegmenterRunner;
import tango.plugin.segmenter.NucleusSegmenterRunner;
import tango.plugin.measurement.MeasurementSequence;
import tango.plugin.filter.PreFilterSequence;
import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import ij.IJ;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.TreeSet;
import mcib3d.image3d.ImageHandler;
import tango.mongo.MongoConnector;
import org.bson.types.ObjectId;
import tango.gui.Core;
import tango.gui.XPEditor;
import tango.util.Cell3DViewer;
import tango.gui.util.FieldFactory;
import tango.mongo.MongoUtils;
import tango.parameter.*;
import tango.plugin.measurement.MeasurementKey;
import tango.plugin.measurement.MeasurementObject;
import tango.plugin.measurement.MeasurementStructure;
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

public class Experiment {
    private int nbSamples;
    private String folder, name, importFileMethod;
    private BasicDBObject[] structureSettings, processingChain;
    private BasicDBList measurements, structures, virtualStructures, sampleChannels, channelFiles;
    private int[] fileRank;
    private String[] fileKeyword;
    private File directory;
    private ObjectId id;
    private BasicDBObject data;
    private MongoConnector mc;
    public Cell3DViewer c3Dv;
    private HashMap<MeasurementKey, ArrayList<String>> keys;
    
    public Experiment (String name, MongoConnector mc) {
        this.mc=mc;
        this.name=name;
        this.folder=mc.getCurrentProject();
        data = mc.getExperiment(name);
        refresh();
    }
    
    public void refresh() {
        if (data.containsField("inputFolder")) {
            this.directory=new File(data.getString("inputFolder"));
            if (!directory.exists()) directory=null;
        }
        if (data.containsField("importFileMethod")) this.importFileMethod=data.getString("importFileMethod");
        else this.importFileMethod=FieldFactory.importMethod[0];
        this.id=(ObjectId)data.get("_id");
        this.nbSamples=data.getInt("nbSamples", 100);
        
        if (data.containsField("structures")) {
            structures = (BasicDBList)data.get("structures");
        } else {
            structures=new BasicDBList();
            data.append("structures", structures);
        }
        if (data.containsField("channelFiles")) {
            channelFiles = (BasicDBList) data.get("channelFiles");
        } else {
            channelFiles=new BasicDBList();
            data.append("channelFiles", channelFiles);
        }
        if (data.containsField("measurements")) {
            measurements = (BasicDBList) data.get("measurements");
        } else {
            measurements=new BasicDBList();
            data.append("measurements", measurements);
        }
        if (data.containsField("virtualStructures")) {
            virtualStructures = (BasicDBList) data.get("virtualStructures");
        } else {
            virtualStructures=new BasicDBList();
            data.append("virtualStructures", virtualStructures);
        }
        if (data.containsField("sampleChannels")) {
            sampleChannels = (BasicDBList) data.get("sampleChannels");
        } else {
            sampleChannels=new BasicDBList();
            data.append("sampleChannels", sampleChannels);
        }
        getStructureProps();
        keys=null;
    }
    
    private void getStructureProps() {
        structureSettings = new BasicDBObject[structures.size()+virtualStructures.size()];
        for (int i =0; i<structures.size(); i++) structureSettings[i]=(BasicDBObject)structures.get(i);
        for (int i = 0; i<virtualStructures.size(); i++) structureSettings[i+structures.size()] = (BasicDBObject)virtualStructures.get(i);
        processingChain = new BasicDBObject[structures.size()];
        for (int i =0; i<structures.size(); i++) {
            if (structureSettings[i].containsField("processingChain")) processingChain[i] = (BasicDBObject)structureSettings[i].get("processingChain");
            else if (structureSettings[i].containsField("settings")) processingChain[i] = (i==0)? mc.getNucSettings(structureSettings[i].getString("settings")) : mc.getChannelSettings(structureSettings[i].getString("settings")); // for compatibility with older versions
        }
        fileKeyword = new String[channelFiles.size()];
        for (int i = 0; i<channelFiles.size(); i++) {
            BasicDBObject file = (BasicDBObject)channelFiles.get(i);
            fileKeyword[i]=file.getString("keyword");
            if (fileKeyword[i]==null || fileKeyword[i].length()==0) {
                fileKeyword[i]=""+(i+1);
                file.append("keyword", fileKeyword[i]);
            }
        }
        fileRank = new int[structures.size()];
        for (int i = 0; i<structures.size(); i++) fileRank[i]=structureSettings[i].getInt("file", 0);
        ChannelFileParameter.setChannels(fileKeyword);
        StructureParameter.setStructures(this.getStructureNames(false), this.getVirtualStructureNames());
        SamplerParameter.setChannels(this.getSampleChannelNames());
    }
    
    public File getDirectory() {
        return directory;
    }
    
    public void setDirectory(File directory) {
        this.directory=directory;
        this.data.append("inputFolder", directory.getAbsolutePath());
    }
    
    public String getImportFileMethod() {
        return importFileMethod;
    }
    
    public BasicDBObject getData() {
        return data;
    }
    
    public BasicDBList getVirtualStructures() {
        return virtualStructures;
    }
    
    public BasicDBList getStructures() {
        return structures;
    }
    
    public BasicDBList getChannelImages() {
        return channelFiles;
    }
    
    public PreFilterSequenceParameter getChannelFilePreFilterSequence(int idx) {
        if (channelFiles==null && channelFiles.size()<=idx) return null; 
        PreFilterSequenceParameter sequence = new PreFilterSequenceParameter("PreFilters", "preFilters");
        sequence.dbGet((BasicDBObject)channelFiles.get(idx));
        return sequence;
    }
    
    public BasicDBList getSampleChannels() {
        return sampleChannels;
    }
    
    public String[] getSampleChannelNames() {
        if (sampleChannels==null) return null;
        String[] res = new String[sampleChannels.size()];
        for (int i = 0; i < res.length; i++) {
            if (sampleChannels.get(i)==null) res[i]="sample"+(i+1);
            else res[i]= ((BasicDBObject)(sampleChannels.get(i))).getString("name");
        }
        return res;
    }
    
    public String[] getVirtualStructureNames() {
        if (virtualStructures==null) return null;
        String[] res = new String[virtualStructures.size()];
        for (int i = 0; i < res.length; i++) {
            res[i] = ((BasicDBObject)(virtualStructures.get(i))).getString("name");
        }
        return res;
    }
    
    public String[] getVirtualStructureColors() {
        if (virtualStructures==null) return null;
        String[] res = new String[virtualStructures.size()];
        for (int i = 0; i < res.length; i++) {
            res[i] = ((BasicDBObject)(virtualStructures.get(i))).getString("color");
        }
        return res;
    }
    
    public String[] getStructureNames(boolean addVirtual) {
        String[] res = new String[this.getNBStructures(addVirtual)];
        for (int i = 0; i < this.getNBStructures(addVirtual); i++) res[i] = structureSettings[i].getString("name");
        return res;
    }
    
    public String[] getStructureColors(boolean addVirtual) {
        String[] res = new String[this.getNBStructures(addVirtual)];
        for (int i = 0; i < this.getNBStructures(addVirtual); i++) res[i] = structureSettings[i].getString("color");
        return res;
    }
    
    public String[] getFileKeywords() {
        return this.fileKeyword;
    }
    
    public int getChannelFileIndex(int structureIdx) {
        return this.fileRank[structureIdx];
    }
    
    public MongoConnector getConnector() {
        return mc;
    }
    
    public ObjectId getId() {
        return id;
    }
    
    public void setCalibration(ImageHandler image) {
        if (XPEditor.useScale.isSelected()) {
            image.setScale(XPEditor.scalexy.getFloatValue(1), XPEditor.scalez.getFloatValue(1), XPEditor.unit.getText());
        }
    }
    
    public String getName() {
        return name;
    }
    
    public String getUserName() {
        return mc.getUserName();
    }
    
    public String getFolder() {
        return folder;
    }
    
    public int getNBFiles() {
        return this.channelFiles.size();
    }
    
    public int getNBSamples() {
        return this.nbSamples;
    }
    
    public int getNBStructures(boolean addVirtual) {
        if (addVirtual) return this.structures.size()+ virtualStructures.size();
        else return this.structures.size();
    }
    
    public int[] getChannelFileIndexes() {
        return this.fileRank;
    }
    
   public BasicDBObject getChannelSettings(int channel) {
       if (channel>=0 && channel<structureSettings.length) return this.structureSettings[channel];
       else return null;
   }
    
   public BasicDBObject getProcessingChain(int structureIdx) {
        if (structureIdx>=0 && structureIdx<processingChain.length) return processingChain[structureIdx];
        else return null;
    }
   
    public PreFilterSequence getPreFilterSequence(int channel, int nbCPUs, boolean verbose) {
        return new PreFilterSequence(processingChain[channel],nbCPUs, verbose);
    }
    
    public PostFilterSequence getPostFilterSequence(int channel, int nbCPUs, boolean verbose) {
        return new PostFilterSequence(processingChain[channel], nbCPUs, verbose);
    }
    
    public SpotSegmenterRunner getSpotSegmenterRunner(int channel, int nbCPUs, boolean verbose) {
        if (channel>=1) return new SpotSegmenterRunner(processingChain[channel], nbCPUs, verbose);
        else return new SpotSegmenterRunner(null, nbCPUs, verbose);
    }
    
    public NucleusSegmenterRunner getNucleusSegmenterRunner(int nbCPUs, boolean verbose) {
        return new NucleusSegmenterRunner(processingChain[0], nbCPUs, verbose);
    }
    
    public MeasurementSequence getMeasurementSequence(boolean override) {
        return new MeasurementSequence(this, measurements, override);
    }
    
    public MeasurementSequence getMeasurementSequenceTest(int measurementIdx) {
        return new MeasurementSequence(this, measurements, measurementIdx);
    }
    
    public BasicDBList getMeasurementSettings() {
        return measurements;
    }
    
    public void save() {
        data.append("keys", getDBKeys());
        data.append("tango_version", Core.VERSION);
        mc.saveExperiment(data);
        refresh();
    }
    
    public void setMeasurements (BasicDBList measurements) {
        this.measurements=measurements;
        this.data.append("measurements", measurements);
        keys=null;
    }
    
    public void setSamples (BasicDBList samples) {
        this.sampleChannels=samples;
        this.data.append("sampleChannels", samples);
    }
    
    public void setStructures(BasicDBList structures, boolean keepProcessingChains) {
        if (keepProcessingChains) {
            for (int i = 0; i<structures.size(); i++) {
                if (i<this.structures.size()) {
                    BasicDBObject p = this.getProcessingChain(i);
                    ((BasicDBObject)structures.get(i)).append("processingChain", p);
                }
            }
        }
        this.data.append("structures", structures);
        this.structures=structures;
    }
    
    public void setProcessingChain(int structureIdx, BasicDBObject processingChain) {
        this.processingChain[structureIdx]=processingChain;
        this.structureSettings[structureIdx].append("processingChain", processingChain);
    }
    
    public void setVirtualStructures(BasicDBList structures) {
        this.virtualStructures=structures;
        this.data.append("virtualStructures", structures);
    }
    
    public void setChannelImages(BasicDBList channelFiles) {
        this.channelFiles=channelFiles;
        this.data.append("channelFiles", channelFiles);
    }
    
    private void computeKeys() {
        MeasurementSequence ms = this.getMeasurementSequence(true);
        keys = ms.getKeys();
    }
    
    public HashMap<MeasurementKey, ArrayList<String>> getKeys() {
        if (keys==null) computeKeys();
        return keys;
    }
    
    private BasicDBObject getDBKeys() {
        getKeys();
        BasicDBObject res = new BasicDBObject();
        BasicDBObject o2o = new BasicDBObject();
        BasicDBObject misc = new BasicDBObject();
        BasicDBObject o = new BasicDBObject();
        res.append("objectToObject", o2o);
        res.append("object", o);
        res.append("misc", misc);
        for (Entry<MeasurementKey, ArrayList<String>> e : keys.entrySet()) {
            if (e.getKey().type==MeasurementObject.Number) o.append(e.getKey().getStructuresAsString(), e.getValue());
            else if (e.getKey().type==MeasurementStructure.ArrayO2O) o2o.append(e.getKey().getStructuresAsString(), e.getValue());
            else misc.append(e.getKey().getStructuresAsString(), e.getValue());
        }
        return res;
    }
    
    public HashMap<Integer, TreeSet<String>> getObjectKeys() {
        if (keys==null) computeKeys();
        HashMap<Integer, TreeSet<String>> res = new HashMap<Integer, TreeSet<String>>();
        for (MeasurementKey mk : keys.keySet()) {
            if (mk.type==MeasurementObject.Number) {
                res.put(mk.getStructures()[0], new TreeSet(keys.get(mk)));
            }
        }
        return res;
    }

    public HashMap<MultiKey, TreeSet<String>> getC2CKeys() {
        if (keys==null) computeKeys();
        HashMap<MultiKey, TreeSet<String>> res = new HashMap<MultiKey, TreeSet<String>>();
        for (MeasurementKey mk : keys.keySet()) {
            if (mk.type==MeasurementStructure.ArrayO2O) {
                res.put(new MultiKey(mk.getStructures()), new TreeSet(keys.get(mk)));
            }
        }
        
        return res;
    }
    
    public String getStructureArrayName(int[] structures) {
        String[] names = this.getStructureNames(true);
        String res = "[";
        for (int i = 0; i<structures.length;i++) {
            res+=names[structures[i]];
            if (i<structures.length-1) res+=", ";
        }
        return res+"]";
    }
}
