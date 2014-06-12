package tango.mongo;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.Mongo;
import com.mongodb.gridfs.GridFS;
import com.mongodb.gridfs.GridFSDBFile;
import com.mongodb.gridfs.GridFSInputFile;
import ij.IJ;
import ij.ImagePlus;
import ij.io.FileInfo;
import ij.io.Opener;
import ij.io.TiffDecoder;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryUsage;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import mcib3d.image3d.ImageHandler;
import mcib3d.image3d.ImageInt;
import mcib3d.utils.exceptionPrinter;
import org.bson.types.ObjectId;
import tango.gui.Core;
import static tango.mongo.MongoConnector.createImage;

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

public class ImageManager {
    private HashMap<ObjectId, GridFS> gfsField, gfsNucleus;
    private GridFS gfsFieldAll, gfsNucleusAll; // retrocompatibilité
    private GridFS gfsFieldThumbnail, gfsNucleusThumbnail;
    private MongoConnector mongo;
    private DB project;
    
    public ImageManager(MongoConnector mongo, DB project) {
        this.mongo=mongo;
        this.project=project;
        ArrayList<ObjectId> exps = mongo.getExperimentIds();
        this.gfsField=new HashMap<ObjectId, GridFS>(exps.size());
        this.gfsNucleus=new HashMap<ObjectId, GridFS>(exps.size());
        gfsFieldThumbnail=new GridFS(project, "fieldThumbnail");
        gfsNucleusThumbnail=new GridFS(project, "nucleusThumbnail");
        DBCollection fieldsFilesT = project.getCollection("fieldThumbnail.files");
        fieldsFilesT.ensureIndex(new BasicDBObject("field_id", 1));
        DBCollection nucleiFilesT = project.getCollection("nucleusThumbnail.files");
        nucleiFilesT.ensureIndex(new BasicDBObject("nucleus_id", 1).append("fileRank", 1));
        for (ObjectId xp : exps) addExperiment(xp);
    }
    
    private void addExperiment(ObjectId id) {
        if (!gfsField.containsKey(id)) {
            String collectionName="fieldImages_"+id.toStringMongod();
            if (project.collectionExists(collectionName+".files") || !project.collectionExists("field")) {
                gfsField.put(id, new GridFS(project, collectionName));
                DBCollection fieldsFiles = project.getCollection(collectionName+".files");
                fieldsFiles.ensureIndex(new BasicDBObject("field_id", 1).append("fileRank", 1));
            } else if (gfsFieldAll==null) {// retrocompatibilité
                gfsFieldAll = new GridFS(project, "field");
                DBCollection fieldsFiles = project.getCollection("field.files");
                fieldsFiles.ensureIndex(new BasicDBObject("field_id", 1).append("fileRank", 1));
                gfsField.put(id, gfsFieldAll);
            }
        }
        if (!gfsNucleus.containsKey(id)) {
            String collectionName="nucleusImages_"+id.toStringMongod();
            if (project.collectionExists(collectionName+".files") || !project.collectionExists("nucleus")) {
                gfsNucleus.put(id, new GridFS(project, collectionName));
                DBCollection nucleiFiles = project.getCollection(collectionName+".files");
                nucleiFiles.ensureIndex(new BasicDBObject("nucleus_id", 1).append("fileIdx", 1).append("fileType", 1));
            } else if (gfsNucleusAll==null) {// retrocompatibilité
                gfsNucleusAll = new GridFS(project, "nucleus");
                DBCollection nucleiFiles = project.getCollection("nucleus.files");
                nucleiFiles.ensureIndex(new BasicDBObject("nucleus_id", 1).append("fileIdx", 1).append("fileType", 1));
                gfsNucleus.put(id, gfsNucleusAll);
            }
        }
    }
    
    protected ArrayList<String> getNucleusCollections() {
        ArrayList<String> res = new ArrayList<String>();
        if (gfsNucleusAll==null) {
            for (ObjectId oid : this.gfsNucleus.keySet()) {
                String colName = "nucleusImages_"+oid.toStringMongod();
                res.add(colName+".files");
                res.add(colName+".chunks");
            }
        } else {
            res.add("nucleus.files");
            res.add("nucleus.chunks");
        }
        return res;
    }
    
    protected ArrayList<String> getFieldCollections() {
        ArrayList<String> res = new ArrayList<String>();
        if (gfsFieldAll==null) {
            for (ObjectId oid : this.gfsField.keySet()) {
                String colName = "fieldImages_"+oid.toStringMongod();
                res.add(colName+".files");
                res.add(colName+".chunks");
            }
        } else {
            res.add("field.files");
            res.add("field.chunks");
        }
        
        return res;
    }
    
    public synchronized void removeInputImages(ObjectId xpId, ObjectId fieldId, boolean removeThumbnail) {
        BasicDBObject queryField = new BasicDBObject("field_id", (ObjectId)fieldId);
        this.gfsField.get(xpId).remove(queryField);
        if (removeThumbnail) this.gfsFieldThumbnail.remove(queryField);
    }
    
    public synchronized void removeInputImage(ObjectId xpId, ObjectId field_id, int fileRank) {
        BasicDBObject query = new BasicDBObject("field_id", field_id).append("fileRank", fileRank);
        gfsField.get(xpId).remove(query);
    }
    
    public synchronized boolean saveInputImage(ObjectId xpId, ObjectId field_id, int fileRank, ImageHandler img, boolean flushImage) {
        if (img==null) return false;
        
        //IJ.log("file: "+img.getTitle()+" size:"+img.getSizeInMb()+ " available memory:"+Core.getAvailableMemory()+ " please free memory");
        
        double scaleZ = img.getScaleZ();
        String unit = img.getUnit();
        String title =img.getTitle();
        MemoryMXBean memoryBean = ManagementFactory.getMemoryMXBean();
        try {
            byte[] data = img.getBinaryData();
            if (data==null) {
                IJ.log("couldn't save image:"+title);
                return false;
            }
            if (flushImage) img.flush();
            GridFSInputFile gfi = this.gfsField.get(xpId).createFile(data);
            data=null;
            gfi.setFilename(title);
            gfi.put("field_id", field_id);
            gfi.put("fileRank", fileRank);
            gfi.put("pixelDepth", scaleZ);
            gfi.put("unit", unit);
            removeInputImage(xpId, field_id, fileRank);
            gfi.save();
            gfi.getOutputStream().close();
            return true;
        } catch (Exception e) {
            exceptionPrinter.print(e, "Error while saving image: "+title, true);
        } catch (OutOfMemoryError e) {
            int MEGABYTE = (1024*1024);
            MemoryUsage heapUsage = memoryBean.getHeapMemoryUsage();
            long maxMemory = heapUsage.getMax() / MEGABYTE;
            long usedMemory = heapUsage.getUsed() / MEGABYTE;
            IJ.log("Error while saving image:"+title+ " Out of memory. Memory Use :" + usedMemory + "M/" + maxMemory + "M");
        }
        return false;
    }
    
    public synchronized void saveFieldThumbnail(ObjectId field_id, ImageHandler img, int sizeX, int sizeY) {
        GridFSInputFile gfi = this.gfsFieldThumbnail.createFile(img.getThumbNail(sizeX, sizeY));
        BasicDBObject query = new BasicDBObject("field_id", field_id);
        gfsFieldThumbnail.remove(query);
        gfi.put("field_id", field_id);
        gfi.save();
        try {
            gfi.getOutputStream().close();
        } catch (Exception e) {
            exceptionPrinter.print(e, "", Core.GUIMode);
        }
    }
    
    
    public synchronized void removeNucleusImage(ObjectId xpId, ObjectId nucleus_id, int fileIdx, int fileType) {
        BasicDBObject query = new BasicDBObject("nucleus_id", nucleus_id).append("fileIdx", fileIdx).append("fileType", fileType);
        gfsNucleus.get(xpId).remove(query);
    }
    
    public synchronized void saveNucleusImage(ObjectId xpId, ObjectId nucleus_id, int fileIdx, int fileType, ImageHandler img) {
        if (img==null) return;
        removeNucleusImage(xpId, nucleus_id, fileIdx, fileType);
        try {
            GridFSInputFile gfi = this.gfsNucleus.get(xpId).createFile(img.getBinaryData());
            gfi.setFilename(img.getImagePlus().getShortTitle());
            gfi.put("nucleus_id", nucleus_id);
            gfi.put("fileIdx", fileIdx);
            gfi.put("fileType", fileType);
            gfi.put("pixelDepth", img.getScaleZ());
            gfi.put("unit", img.getUnit());
            gfi.save();
            if (gfi!=null) gfi.getOutputStream().close();
        } catch (Exception e) {
            exceptionPrinter.print(e, "Error while saving image:"+img.getTitle(), Core.GUIMode);
        }
    }
    
    public synchronized void saveChannelImageThumbnail(ObjectId nucleus_id, int fileIdx, ImageHandler img, int sizeX, int sizeY, ImageInt mask) {
        GridFSInputFile gfi = this.gfsNucleusThumbnail.createFile(img.getThumbNail(sizeX, sizeY, mask));
        BasicDBObject query = new BasicDBObject("nucleus_id", nucleus_id).append("fileRank", fileIdx);
        gfsNucleusThumbnail.remove(query);
        gfi.put("nucleus_id", nucleus_id);
        gfi.put("fileRank", fileIdx);
        gfi.save();
        try {
            gfi.getOutputStream().close();
        } catch (Exception e) {
            exceptionPrinter.print(e, "", Core.GUIMode);
        }
    }
    
    public synchronized ImageHandler getInputImage(ObjectId xpId, ObjectId field_id, int idx) {
        BasicDBObject query = new BasicDBObject("field_id", field_id).append("fileRank", idx);
        GridFSDBFile f = this.gfsField.get(xpId).findOne(query);
        if (f!=null) return createImage(f);
        return null;
    }
    
    public synchronized ImageHandler getNucImage(ObjectId xpId, ObjectId cellId, int fileIdx, int fileType) {
        BasicDBObject query = new BasicDBObject("nucleus_id", cellId).append("fileIdx", fileIdx).append("fileType", fileType);
        GridFSDBFile f = this.gfsNucleus.get(xpId).findOne(query);
        if (f!=null) return createImage(f);
        return null;
    }
    
    public static ImageHandler createImage(GridFSDBFile file) {
        
        TiffDecoder td = new TiffDecoder(file.getInputStream(), file.getFilename());
        try {
            FileInfo[] info = td.getTiffInfo();
            ImagePlus imp = null;
            //System.out.println("opening file: depth:"+info.length+ " info0:"+info[0].toString());
            if (info.length>1) { // try to open as stack
                Opener o = new Opener();
                o.setSilentMode(true);
                imp = o.openTiffStack(info);
                imp.setTitle(file.getFilename());
                if (file.containsField("pixelDepth")) imp.getCalibration().pixelDepth=(Double)file.get("pixelDepth");
                if (file.containsField("unit")) imp.getCalibration().setUnit((String)file.get("unit"));
                file.getInputStream().close();
                if (imp!=null) return ImageHandler.wrap(imp);
            } else {
                // FIXME not tested!!
                Opener o = new Opener();
                imp = o.openTiff(file.getInputStream(), file.getFilename());
                file.getInputStream().close();
                if (imp!=null) return ImageHandler.wrap(imp);
            }
        } catch(Exception e) {
            exceptionPrinter.print(e, "", Core.GUIMode);
        }
        return null;
    }

    // restore & dump
    
    
    // mise a jour de la base de donnee
    
    public void transferProject() {
        int count = 0;
        for (ObjectId id : mongo.getExperimentIds()) transferXP(id, count++);
        Core.killProgressor();
    }
    
    private void transferXP(ObjectId xpId, int counter) {
        GridFS gfsNuc = new GridFS(project, "fieldImages_"+xpId.toStringMongod());
        GridFS gfsF = new GridFS(project, "nucleusImages_"+xpId.toStringMongod());
        String label = "Update Experiment:"+counter+" ";
        ArrayList<ObjectId> fids = mongo.getFieldIds(xpId);
        Core.getProgressor().setAction(label+" Field Images");
        Core.getProgressor().resetProgress(fids.size());
        for (ObjectId fId : fids) {
            transferFiles("field_id", fId, this.gfsFieldAll, gfsF);
            Core.getProgressor().incrementStep();
        }
        ArrayList<ObjectId> nids =  mongo.getNucleusIds(xpId);
        Core.getProgressor().setAction(label+" Nucleus Images");
        Core.getProgressor().resetProgress(nids.size());
        for (ObjectId nId :nids) {
            transferFiles("nucleus_id", nId, this.gfsFieldAll, gfsF);
            Core.getProgressor().incrementStep();
        }
        this.gfsField.put(xpId, gfsF);
        this.gfsNucleus.put(xpId, gfsNuc);
    }
    
    private void transferFiles(String queryField, ObjectId queryValue, GridFS gfsSource, GridFS gfsDestination) {
        BasicDBObject query = new BasicDBObject(queryField, queryValue);
        List<GridFSDBFile> files = gfsSource.find(query); // FIXME charge tout en mémoire?
        for (GridFSDBFile file : files) {
            GridFSInputFile gfi = gfsDestination.createFile(file.getInputStream(), file.getFilename());
            gfi.put(queryField, queryValue);
            gfi.put("fileIdx", file.get("fileIdx"));
            gfi.put("fileType", file.get("fileType"));
            gfi.put("pixelDepth", file.get("pixelDepth"));
            gfi.put("unit", file.get("unit"));
            gfi.save();
            if (gfi!=null) try {
                gfi.getOutputStream().close();
            } catch (IOException ex) {
                Logger.getLogger(ImageManager.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        gfsSource.remove(query);
    }
    
}
