package tango.mongo;
import ij.*;
import java.util.*;

import com.mongodb.*;
import com.mongodb.gridfs.GridFS;
import com.mongodb.gridfs.GridFSDBFile;
import com.mongodb.gridfs.GridFSInputFile;
import ij.io.FileInfo;
import ij.io.Opener;
import ij.io.TiffDecoder;
import mcib3d.utils.exceptionPrinter;
import mcib3d.image3d.ImageHandler;
import java.awt.Image;
import java.io.*;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryUsage;
import java.net.UnknownHostException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import mcib3d.image3d.ImageInt;
import org.bson.types.ObjectId;
import tango.dataStructure.Selection;
import tango.gui.Core;
import tango.helper.ID;
import tango.parameter.SettingsParameter;
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
public class MongoConnector {
    public final static String prefix = "tango";
    private MongoClient m;
    public static String defaultHost_DB="localhost";
    private DB project, settings, admin;
    private DBCollection adminUser, adminProject, help;
    private DBCollection channelSettings, nucleusSettings;
    private DBCollection nucleus, experiment, object3D, field, structureMeasurement, selection;
    private GridFS gfsField, gfsNucleus, gfsFieldThumbnail, gfsNucleusThumbnail;
    private String host, username, projectName;
    public final static int R=0;
    public final static int S=2;
    public final static int SP=3;
    public final static int TMB=-1;
    public final static int MASKS=-2;
    public static String[] collections=new String[] {"experiment", "field", "nucleus", "object3d", "selection", "structureMeasurement", "nucleusThumbnail.files", "nucleusThumbnail.chunks", "fieldThumbnail.files", "fieldThumbnail.chunks"};
    public static String[] collectionsSettings=new String[] {"nucleus", "channel"};
    ObjectId userId;
    Thread mongod;
    public MongoConnector(String host_DB) {
        if (host_DB ==null || host_DB.equals("")) host=defaultHost_DB;
        else this.host=host_DB;
        createMongo();
        IJ.showStatus("testing connection with db...");
        if (isConnected()) {
            IJ.showStatus("connection with db ok!");
            try {
                // FIXME for compatibility with older version...
                String adminName = prefix+"_admin";
                String oldAdminName = "ij3DM_admin";
                List<String> dbnames = m.getDatabaseNames();
                if (dbnames.contains(adminName)) admin=m.getDB(adminName);
                else if (dbnames.contains(oldAdminName)) admin=m.getDB(oldAdminName);
                else admin=m.getDB(adminName); //creates the admin database
                adminUser=admin.getCollection("user");
                adminProject=admin.getCollection("dbnames");
                help=admin.getCollection("help");
            } catch (Exception e) {
                exceptionPrinter.print(e, "Connection: ", Core.GUIMode);
            }
        } else {
            IJ.error("Couldn't connect to DB. Try to run mongoDB server first, or check hostname.");
        }
    }
    
    private synchronized void createMongo() {
        if (Core.GUIMode) IJ.showStatus("creating connection with db...");
        try {
            m = new MongoClient(host);
        } catch (UnknownHostException e) {
            exceptionPrinter.print(e, "ukhe:", Core.GUIMode);
        }
    }
    
    public boolean isConnected() {
        if (m==null) return false;
        List<String> l;
        try {
            l=m.getDatabaseNames();
        } catch (Exception e) {
            if (Core.GUIMode) IJ.log("connection failed..");
            return false;
        }
        return true;
    }
    
    public static boolean isMongoOn(String host) {
        MongoClient m;
        m=null;
        try {
            m = new MongoClient(host);
        } catch (UnknownHostException e) {
            exceptionPrinter.print(e, "ukhe:", Core.GUIMode);
            return false;
        }
        if (m==null) return false;
        List<String> l;
        try {
            l=m.getDatabaseNames();
        } catch (MongoException e) {
            if (Core.GUIMode) IJ.log("connection failed..");
            return false;
        }
        return true;
    }
    
    public void close () {    
        this.m.close();
    }
    
    public MongoConnector duplicate(Boolean setProject) {
        MongoConnector dup = new MongoConnector(host);
        dup.setUser(username, true);
        if (setProject) dup.setProject(projectName);
        return dup;
    }
    
    
    
    public String getHost() {
        if (host ==null || host.equals("")) host="localhost";
        return host;
    }
    
    public ArrayList<String> getUsers() {
        DBCursor cur = adminUser.find();
        ArrayList<String> res= new ArrayList(cur.size());
        while (cur.hasNext()) {
            BasicDBObject u = (BasicDBObject)cur.next();
            res.add(u.getString("name"));
        }
        cur.close();
        return res;
    }
    
    public synchronized BasicDBObject setUser(String username, boolean create) {
        if (username==null) {
            this.username=null;
            this.userId=null;
            settings=null;
            nucleusSettings=null;
            channelSettings=null;
            return null;
        }
        try {
            this.username=username;
            BasicDBObject user = (BasicDBObject)adminUser.findOne(new BasicDBObject("name", username));
            if (user==null) {
                if (!create) return null;
                String settingsDB = prefix+"_"+username+"_settings";
                List<String> names = m.getDatabaseNames();
                while (names.contains(settingsDB)) settingsDB=settingsDB+"0";
                adminUser.save(new BasicDBObject("name", username).append("settingsDB", settingsDB));
                user = (BasicDBObject)adminUser.findOne(new BasicDBObject("name", username));
            } 
            userId=(ObjectId)user.get("_id");
            settings = m.getDB(user.getString("settingsDB"));
            if (!settings.collectionExists("nucleus")) {settings.createCollection("nucleus", new BasicDBObject()); }//IJ.log("collection nucleus created!");}
            if (!settings.collectionExists("channel")) {settings.createCollection("channel", new BasicDBObject()); }//IJ.log("collection channel created!");}
            nucleusSettings = settings.getCollection("nucleus");
            channelSettings = settings.getCollection("channel");
            return user;
        } catch (Exception e) {
            exceptionPrinter.print(e, "Connection: ", Core.GUIMode);
            return null;
        }
    }
    
    public synchronized BasicDBObject getUser(String username) {
        try {
            return (BasicDBObject)adminUser.findOne(new BasicDBObject("name", username));
        } catch (Exception e) {
            exceptionPrinter.print(e, "GetUser: ", Core.GUIMode);
            return null;
        }
    }
    
    public synchronized BasicDBObject getUser() {
        try {
            return (BasicDBObject)adminUser.findOne(new BasicDBObject("name", username));
        } catch (Exception e) {
            exceptionPrinter.print(e, "GetUser: ", Core.GUIMode);
            return null;
        }
    }
    
    public synchronized void saveUser(BasicDBObject user) {
        try {
            adminUser.save(user);
        } catch (Exception e) {
            exceptionPrinter.print(e, "saveUser: ", Core.GUIMode);
        }
    }
    
    public synchronized void saveImportDir(String dir) {
        BasicDBObject usr = getUser();
        usr.append("importDir", dir);
        saveUser(usr);
    }
    
    public static String getPrefix() {
        return prefix;
    }

    public synchronized static void mongoStart() {
        if (IJ.isMacOSX()) {
            File cellar = new File("/usr/local/Cellar/mongodb");
            String[] commandArgs = {
            "brew",
            "services",
            "start",
            "mongodb"
            };
            tango.Supervisor.execProcess(cellar, commandArgs, false);
        } else if (IJ.isWindows()) {
            File cellar = new File("C:\\mongodb");
            String[] commandArgs = {
            "net",
            "start",
            "mongodb"
            };
            tango.Supervisor.execProcess(cellar, commandArgs, true);
        } else if (IJ.isLinux()){
            File cellar = new File("/usr/bin");
            String[] commandArgs = {
            "service",
            "mongodb",
            "start"
            };
            tango.Supervisor.execProcess(cellar, commandArgs, true);
        } 
    }
    
    public synchronized static void mongoStop() {
        if (IJ.isMacOSX()) {
            File cellar = new File("/usr/local/Cellar/mongodb");
            String[] commandArgs = {
            "brew",
            "services",
            "stop",
            "mongodb"
            };
            tango.Supervisor.execProcess(cellar, commandArgs, false);
        } else if (IJ.isWindows()) {
            File cellar = new File("C:\\mongodb");
            String[] commandArgs = {
            "net",
            "stop",
            "mongodb"
            };
            tango.Supervisor.execProcess(cellar, commandArgs, true);
        } else if (IJ.isLinux()){
            File cellar = new File("/usr/bin");
            String[] commandArgs = {
            "service",
            "mongodb",
            "stop"
            };
            tango.Supervisor.execProcess(cellar, commandArgs, true);
        }
    }

    public void mongoDumpProject(String projectName, String outputPath, boolean inputImages, boolean outputImages) {
        String projectDBName = this.getProjectDBName(projectName);
        for (String col : collections) dumpCollection(projectDBName, col, outputPath);
        if (inputImages || outputImages) {
            ImageManager im = new ImageManager(this, m.getDB(projectDBName));
            if (inputImages) for (String col : im.getFieldCollections()) dumpCollection(projectDBName, col, outputPath);
            if (outputImages) for (String col : im.getNucleusCollections()) dumpCollection(projectDBName, col, outputPath);
        }
    }
    
    private void dumpCollection(String projectDBName, String collectionName, String outputPath) {
        ProcessBuilder pb = new ProcessBuilder("mongodump", "--host", host, "--db", projectDBName, "--collection", collectionName, "-o", outputPath);
        execProcess(pb);
    }
    
    public void mongoDumpSettings(String outputPath) {
        for (String col : collectionsSettings) dumpCollection(this.settings.getName(), col, outputPath);
    }
    
    public void mongoRestoreProject(String dumpProjectPath, String projectName) {
        if (!this.projectNameisValid(projectName)) {
            IJ.error("invalid project name");
            return;
        }
        File dir = new File(dumpProjectPath);
        if (!dir.isDirectory()) dir=dir.getParentFile();
        File[] bsonFiles = dir.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File directory, String fileName) {
                return fileName.endsWith(".bson");
            }
        });
        if (bsonFiles!=null&& bsonFiles.length>0) {
            createProject(projectName);
            String newDB = getProjectDBName(projectName);
            ProcessBuilder pb;
            for (File f : bsonFiles) {
                String colName = f.getName().replaceFirst("[.][^.]+$", "");
                pb= new ProcessBuilder("mongorestore", "--host", host, "--db", newDB, "--collection", colName, f.getAbsolutePath());
                execProcess(pb);
            }
            this.setProject(projectName);
        } else {
            if (Core.GUIMode) ij.IJ.log("Folder:"+dir.getAbsolutePath()+ " does not contains .bson files");
        }
        
    }
    
    public void mongoRestoreSettings(String dumpProjectPath) {
        ProcessBuilder pb;
        File dir = new File(dumpProjectPath);
        if (!dir.isDirectory()) dir=dir.getParentFile();
        File nuc = new File(dir.getAbsolutePath()+File.separator+"nucleus.bson");
        if (nuc.exists()) {
            pb = new ProcessBuilder("mongorestore", "--host", host, "--db", this.settings.getName(), "--collection", "nucleus", "--drop",  nuc.getAbsolutePath());
            execProcess(pb);
        } else if (Core.GUIMode) ij.IJ.log("nucleus.bson file not found in folder:"+dir.getAbsolutePath());
        
        
        File chan = new File(dir.getAbsolutePath()+File.separator+"channel.bson");
        if (chan.exists()) {
            pb = new ProcessBuilder("mongorestore", "--host", host, "--db", this.settings.getName(), "--collection", "channel", "--drop",  chan.getAbsolutePath());
            execProcess(pb);
        } else if (Core.GUIMode) ij.IJ.log("channel.bson file not found in folder:"+dir.getAbsolutePath());
    }
    
    /*public synchronized static boolean exec(String[] command) {
        if(Core.GUIMode) System.out.println("executing command:"+command);
        try {  
            Process p = Runtime.getRuntime().exec(command); 
            BufferedReader in = new BufferedReader(new InputStreamReader(p.getInputStream()));  
            String line = null;  
            while ((line = in.readLine()) != null) {  
                if(Core.GUIMode)  IJ.log(line);  
                else System.out.println(line);  
            }  
            BufferedReader error = new BufferedReader(new InputStreamReader(p.getErrorStream()));
            boolean er=true;
            while ((line = error.readLine()) != null) {
                if(Core.GUIMode) IJ.log(line);
                else System.out.println(line);
                er=false;
            }
            return er;
        } catch (IOException e) {  
            exceptionPrinter.print(e, "exec command:"+command, Core.GUIMode);
            return false;
        }  
    }*/
    
    private static boolean execProcess(ProcessBuilder pb) {
        try {
            System.out.println("exec: "+pb.toString());
            System.out.println("exec2: "+pb.command());
            IJ.log("Executing command : "+pb.command());
            Process p = pb.start();
            BufferedReader in = new BufferedReader(new InputStreamReader(p.getInputStream()));  
            String line = null;  
            while ((line = in.readLine()) != null) {  
                if(Core.GUIMode)  IJ.log(line);  
                else System.out.println(line);  
            }  
            BufferedReader error = new BufferedReader(new InputStreamReader(p.getErrorStream()));
            boolean er=true;
            while ((line = error.readLine()) != null) {
                if(Core.GUIMode) IJ.log(line);
                else System.out.println(line);
                er=false;
            }
            IJ.log("Finished command : "+pb.command());
            return er;
            
        } catch (IOException ex) {
            Logger.getLogger(MongoConnector.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        }
        
    }
    
    public String getUserName() {
        return username;
    }
    
    public void removeUser() {
        for (String p : getProjects()) removeProject(p);
        adminProject.remove(new BasicDBObject("user_id", this.userId));
        adminUser.remove(new BasicDBObject("_id", this.userId));
        m.dropDatabase(settings.getName());
        
    }

    public ArrayList<String> getProjects() { //db names: prefixe + _ + username + _ + xp element
        DBCursor cur = adminProject.find(new BasicDBObject("user_id", this.userId));
        ArrayList<String> res= new ArrayList<String>(cur.size());
        while (cur.hasNext()) {
            BasicDBObject f = (BasicDBObject)cur.next();
            res.add(f.getString("name"));
        }
        cur.close();
        return res;
    }
    
    public synchronized BasicDBObject getField(ObjectId _id) {
        return (BasicDBObject) field.findOne(new BasicDBObject("_id", _id));
    }
    
    public String getCurrentProject() {
        return projectName;
    }

    private String getProjectDBName(String name) {
        BasicDBObject projectDB = (BasicDBObject)adminProject.findOne(new BasicDBObject("user_id", userId).append("name", name));
        if (projectDB==null) return null;
        else return projectDB.getString("dbname");
    }
    
    public boolean setProject(String name) {
        String dbname=getProjectDBName(name);
        if (dbname==null) return false;
        project=m.getDB(dbname);
        experiment=project.getCollection("experiment");
        structureMeasurement=project.getCollection("structureMeasurement");
        nucleus=project.getCollection("nucleus");
        object3D=project.getCollection("object3d");
        field=project.getCollection("field");
        selection=project.getCollection("selection");
        selection.setObjectClass(Selection.class);
        gfsField=new GridFS(project, "field");
        gfsNucleus=new GridFS(project, "nucleus");
        gfsFieldThumbnail=new GridFS(project, "fieldThumbnail");
        gfsNucleusThumbnail=new GridFS(project, "nucleusThumbnail");
        if (Core.GUIMode) IJ.log("project: "+ name+ " ("+project.getName()+") set!");
        this.projectName=name;
        return true;
    }

    public boolean projectNameisValid(String name) {
        return adminProject.findOne(new BasicDBObject("user_id", userId).append("name", name))==null;
    }
    
    public synchronized void createProject (String name) {
        if (!projectNameisValid(name)) {
            IJ.error("Project name not valid");
            return;
        }
        String dbname = prefix+"_"+username+"_"+name;
        List<String> dbnames = m.getDatabaseNames();
        while (dbnames.contains(dbname)) dbname+="0";
        adminProject.save(new BasicDBObject("user_id",userId).append("name", name).append("dbname", dbname));
        setProject(name);
        if (Core.GUIMode) IJ.log("project:"+name+ " created!");
    }
    
    public synchronized void removeProject(String name) {
        BasicDBObject query = new BasicDBObject("user_id", userId).append("name", name);
        DBObject f = adminProject.findOne(query);
        if (f==null) return;
        m.dropDatabase(((BasicDBObject)f).getString("dbname"));
        adminProject.remove(query);
        if (Core.GUIMode) IJ.log("project:" + name+ " removed!");
    }
    
    
    public void createExperiment (String name) {
        experiment.save(new BasicDBObject("name", name));
    }

    public synchronized void removeExperiment(String name) {
        BasicDBObject xp = this.getExperiment(name);
        BasicDBObject queryXP = new BasicDBObject("experiment_id", (ObjectId)xp.get("_id"));
        DBCursor cursor = field.find(queryXP);
        if (Core.GUIMode) ij.IJ.log("Deleting XP:"+name+ "...");
        if (Core.GUIMode) ij.IJ.log("Number of fields"+cursor.count());
        while(cursor.hasNext()) {
            DBObject obj = cursor.next();
            removeField((ObjectId)obj.get("_id"));
        }
        cursor.close();
        selection.remove(queryXP);
        experiment.remove(new BasicDBObject("name", name));
        if (Core.GUIMode)  IJ.log("xp removed:"+name);
    }
    
    public synchronized void removeNucleus(ObjectId nuc_id) {
        BasicDBObject queryNuc = new BasicDBObject("nucleus_id", nuc_id);
        object3D.remove(queryNuc);
        structureMeasurement.remove(queryNuc);
        gfsNucleus.remove(queryNuc);
        gfsNucleusThumbnail.remove(queryNuc);
        nucleus.remove(new BasicDBObject("_id", nuc_id));
    }
    
    public synchronized void deleteStructure(ObjectId nuc_id, int channelIdx) {
        removeStructureMeasurements(nuc_id, channelIdx);
        gfsNucleus.remove(new BasicDBObject("nucleus_id", nuc_id).append("fileIdx", channelIdx).append("fileType", MongoConnector.S));
        gfsNucleus.remove(new BasicDBObject("nucleus_id", nuc_id).append("fileIdx", channelIdx).append("fileType", MongoConnector.SP));
    }
    
    public synchronized void removeField(ObjectId fieldId) {
        BasicDBObject queryField = new BasicDBObject("field_id", fieldId);
        DBCursor cur = nucleus.find(queryField);
        while (cur.hasNext()) {
            DBObject nuc = cur.next();
            removeNucleus((ObjectId)nuc.get("_id"));
        }
        cur.close();
        this.field.remove(new BasicDBObject("_id", fieldId));
        removeNucleusImage(fieldId, 0, MASKS);
        removeInputImages(fieldId, true);
    }

    public synchronized ArrayList<String> getExperiments() {
        ArrayList<String> res = new ArrayList<String>();
        DBCursor cur = experiment.find();
        while (cur.hasNext()) {
            res.add(cur.next().get("name").toString());
        }
        cur.close();
        return res;
    }
    
    public synchronized ArrayList<String> getExperiments(String project) {
        ArrayList<String> res = new ArrayList<String>();
        String fol = getProjectDBName(project);
        if (fol==null) return res;
        DB f=m.getDB(fol);
        DBCollection xp=f.getCollection("experiment");
        
        DBCursor cur = xp.find();
        while (cur.hasNext()) {
            res.add(cur.next().get("name").toString());
        }
        cur.close();
        
        return res;
    }
    
    public synchronized void duplicateExperiment(MongoConnector sourceMongo, String source, String destination) {
        DBObject xp = sourceMongo.getExperiment(source);
        xp.removeField("_id");
        xp.put("name", destination);
        experiment.insert(xp);
    }

    public synchronized BasicDBObject getExperiment(String name) {
        if (name==null || name.length()==0) {
            IJ.error("invalid XP name");
            return null;
        }
        DBObject xp = (experiment.findOne(new BasicDBObject("name", name)));
        if (xp!=null) return (BasicDBObject)xp;
        else {
            this.createExperiment(name);
            return (BasicDBObject)(experiment.findOne(new BasicDBObject("name", name)));
        }
    }
    
    public synchronized void saveExperiment(BasicDBObject xp) {
        String name = xp.getString("name");
        experiment.update(new BasicDBObject("name", name), xp, true, false);
    }
    
    public synchronized ArrayList<String> getNucSettings() {
        DBCursor cur = nucleusSettings.find().sort(new BasicDBObject("name", 1));
        ArrayList<String> res = new ArrayList<String>(cur.count());
        while (cur.hasNext()) {
            DBObject o = cur.next();
            if (o.containsField("name")) res.add(o.get("name").toString());
        }
        cur.close();
        return res;
    }
    
    public synchronized void duplicateNucSettings(String name, String newName) {
        DBObject type = (nucleusSettings.findOne(new BasicDBObject("name", name)));
        if (type==null) {
            ij.IJ.error("duplicate error: settings not found");
            return;
        }
        type.removeField("_id");
        type.put("name", newName);
        nucleusSettings.insert(type);
    }
    
    public synchronized void duplicateChannelSettings(String name, String newName) {
        DBObject type = (channelSettings.findOne(new BasicDBObject("name", name)));
        if (type==null) {
            ij.IJ.error("duplicate error: settings not found");
            return;
        }
        type.removeField("_id");
        type.put("name", newName);
        channelSettings.insert(type);
    }
    
    public synchronized BasicDBObject getNucSettings(String name) {
        DBObject query = new BasicDBObject("name", name);
        DBObject res = nucleusSettings.findOne(query);
        if (res!=null) return (BasicDBObject)res;
        else return null;
    }
    
    public synchronized BasicDBObject getChannelSettings(String name) {
        DBObject query = new BasicDBObject("name", name);
        DBObject res = channelSettings.findOne(query);
        if (res!=null) return (BasicDBObject)res;
        else return null;
    }
    
    

    public synchronized void createNucSettings(String name) {
        nucleusSettings.insert(new BasicDBObject("name", name));
    }

    public synchronized void renameNucSettings(String old_name, String new_name) {
        nucleusSettings.update(new BasicDBObject("name", old_name), new BasicDBObject("$set", new BasicDBObject("name", new_name)));
    }

    public synchronized void renameChannelSettings(String old_name, String new_name) {
        channelSettings.update(new BasicDBObject("name", old_name), new BasicDBObject("$set", new BasicDBObject("name", new_name)));
    }
    
    public synchronized void saveNucSettings(BasicDBObject type) {
        String name = type.getString("name");
        this.nucleusSettings.update(new BasicDBObject("name", name), type, true, false);
    }
    
    public synchronized void saveStructureProcessingChain(BasicDBObject type) {
        String name = type.getString("name");
        this.channelSettings.update(new BasicDBObject("name", name), type, true, false);
    }

    public synchronized void removeNucSettings(String name) {
        nucleusSettings.remove(new BasicDBObject("name", name));
        SettingsParameter.setSettings();
    }

    public synchronized void removeChannelSettings(String name) {
        channelSettings.remove(new BasicDBObject("name", name));
        SettingsParameter.setSettings();
    }

    public synchronized void renameExperiment(String old_name, String new_name) {
         experiment.update(new BasicDBObject("name", old_name), new BasicDBObject("$set", new BasicDBObject("name", new_name)));
    }

    public synchronized ArrayList<String> getChannelSettings() {
        DBCursor cur = channelSettings.find().sort(new BasicDBObject("name", 1));
        ArrayList<String> res = new ArrayList<String>(cur.count()+1);
        res.add("NO SEGMENTATION");
        while (cur.hasNext()) {
            res.add(cur.next().get("name").toString());
        }
        cur.close();
        return res;
    }
    
    public synchronized void createStructureProcessingChain(String name) {
        channelSettings.insert(new BasicDBObject("name", name));
    }

    public synchronized BasicDBObject getNucleus(String xpName, String fieldName, int nucIdx, String cellName) {
        BasicDBObject field = getField(xpName, fieldName);
        BasicDBObject query = new BasicDBObject("field_id", field.get("_id")).append("idx", nucIdx);
        DBObject nuc = nucleus.findOne(query);
        if (nuc==null) { //save the nucleus to add an _id field
            nuc = new BasicDBObject("field_id", field.get("_id")).append("idx", nucIdx).append("experiment_id", field.get("experiment_id"));
            if (cellName!=null) ((BasicDBObject)nuc).append("name", cellName);
            nucleus.save(nuc);
            return (BasicDBObject)nucleus.findOne(query);
        }
        else return (BasicDBObject)nuc;
    }
    
    public synchronized ArrayList<Selection> getSelections(ObjectId xpId) {
        if (selection==null) return null;
        BasicDBObject query = new BasicDBObject("experiment_id", xpId);
        DBCursor cursor = selection.find(query);
        cursor.sort(new BasicDBObject("name", 1));
        ArrayList<Selection> res = new ArrayList<Selection> (cursor.size());
        while (cursor.hasNext()) {
            Selection s = (Selection) cursor.next();
            s.init();
            res.add(s);
        }
        cursor.close();
        return res;
    }
    
    public void saveSelection(Selection s) {
        selection.save(s);
    }
    
    public void removeSelection(Selection s) {
        selection.remove(s);
    }
    
    public synchronized BasicDBObject getNucleus(ObjectId nucId) {
        return (BasicDBObject)nucleus.findOne(new BasicDBObject("_id", nucId));
    }
    
    public synchronized DBCursor getFieldNuclei(String xpName, String fieldName) {
        BasicDBObject field = getField(xpName, fieldName);
        DBObject query = new BasicDBObject("field_id", field.get("_id"));
        return nucleus.find(query);
    }
    
    public synchronized DBObject getOneNucleus(ObjectId experiment_id) {
        return nucleus.findOne(new BasicDBObject("experiment_id", experiment_id));
    }
    
    public synchronized DBCursor getXPNuclei(String xpName) {
        BasicDBObject xp = getExperiment(xpName);
        DBObject query = new BasicDBObject("experiment_id", xp.get("_id"));
        return nucleus.find(query);
    }
    
    public synchronized void setNucleusTag(ObjectId nucId, int tag) {
        nucleus.update(new BasicDBObject("_id", nucId), new BasicDBObject("$set", new BasicDBObject("tag", tag)));
    }
    
    public synchronized BasicDBObject getField(String xpName, String fieldName) {
        BasicDBObject xp = getExperiment(xpName);
        DBObject query = new BasicDBObject("experiment_id", xp.get("_id")).append("name", fieldName);
        DBObject field = this.field.findOne(query);
        if (field==null) { //save to add an _id field
            this.field.save(query);
            return (BasicDBObject)this.field.findOne(query);
        }
        else return (BasicDBObject)field;
    }
    
    public synchronized boolean fieldExists(String xpName, String fieldName) {
        BasicDBObject xp = getExperiment(xpName);
        DBObject query = new BasicDBObject("experiment_id", xp.get("_id")).append("name", fieldName);
        DBObject field = this.field.findOne(query);
        return (field!=null);
    }
    
    public synchronized DBCursor getFields(ObjectId xp_id) {
        DBObject query = new BasicDBObject("experiment_id", xp_id);
        return this.field.find(query);
    }
    
    public synchronized void removeObject(ObjectId nucleusId, int channelIdx, int idx) {
        BasicDBObject query = new BasicDBObject("nucleus_id", nucleusId).append("channelIdx", channelIdx).append("idx", idx);
        object3D.remove(query);
    }
    
    public synchronized void removeStructureMeasurements(ObjectId nucleusId, int structureIdx) {
        removeObjectMeasurements(nucleusId, structureIdx);
        structureMeasurement.remove(new BasicDBObject("nucleus_id", nucleusId).append("structures", new BasicDBObject("$all", new int[]{structureIdx})));
    }
    
    public synchronized void removeObjectMeasurements(ObjectId nucleusId, int structureIdx) {
        BasicDBObject query = new BasicDBObject("nucleus_id", nucleusId).append("channelIdx", structureIdx);
        object3D.remove(query);
    }

    public ArrayList<ObjectId> getExperimentIds() {
        BasicDBObject query = new BasicDBObject();
        return listIds(query, experiment);
    }
    
    public ArrayList<ObjectId> getNucleusIds(ObjectId xpId) {
        BasicDBObject query = new BasicDBObject("experiment_id", xpId);
        return listIds(query, nucleus);
    }
    
     public ArrayList<ObjectId> getFieldIds(ObjectId xpId) {
        BasicDBObject query = new BasicDBObject("experiment_id", xpId);
        return listIds(query, field);
    }
    
     private ArrayList<ObjectId> listIds(BasicDBObject query, DBCollection col) {
         DBCursor cursor = col.find(query);
        ArrayList<ObjectId> res = new ArrayList<ObjectId>(cursor.count());
        while (cursor.hasNext()) {
            BasicDBObject o = (BasicDBObject) cursor.next();
            res.add(o.getObjectId("_id"));
        }
        return res;
     }
     
    public synchronized DBCursor getObjectsCursor(ObjectId nucleusId, int channelIdx) {
        BasicDBObject query = new BasicDBObject("nucleus_id", nucleusId).append("channelIdx", channelIdx);
        return object3D.find(query);
    }
    
    public synchronized BasicDBObject getObject(ObjectId nucleusId, int channelIdx, int idx) {
        BasicDBObject query = new BasicDBObject("nucleus_id", nucleusId).append("channelIdx", channelIdx).append("idx", idx);
        DBObject res = object3D.findOne(query);
        if (res==null) return query;
        else return (BasicDBObject)res;
    }
    
    public synchronized BasicDBObject getObject(ObjectId nucleusId, int channelIdx, int idx, ArrayList<String> fields) {
        BasicDBObject query = new BasicDBObject("nucleus_id", nucleusId).append("channelIdx", channelIdx).append("idx", idx);
        DBObject res;
        if (fields==null || fields.isEmpty()) res = object3D.findOne(query);
        else {
            BasicDBObject f = new BasicDBObject();
            for (String s : fields) f.append(s, 1);
            res = object3D.findOne(query, f);
        }
        if (res==null) return query;
        else return (BasicDBObject)res;
    }
    
    public synchronized HashMap<ObjectId, BasicDBObject> getNucleiObjects(ObjectId experimentId) {
        BasicDBObject query = new BasicDBObject("experiment_id", experimentId).append("channelIdx", 0);
        DBCursor cursor = object3D.find(query);
        HashMap<ObjectId, BasicDBObject> res = new HashMap<ObjectId, BasicDBObject> (cursor.size());
        while (cursor.hasNext()) {
            BasicDBObject nuc = (BasicDBObject) cursor.next();
            res.put(nuc.getObjectId("nucleus_id"), nuc);
        }
        cursor.close();
        return res;
    }
    
    public synchronized HashMap<Integer, BasicDBObject> getObjects(ObjectId nucleusId, int channelIdx) {
        BasicDBObject query = new BasicDBObject("nucleus_id", nucleusId).append("channelIdx", channelIdx);
        DBCursor cursor = object3D.find(query);
        HashMap<Integer, BasicDBObject> res = new HashMap<Integer, BasicDBObject> (cursor.size());
        while (cursor.hasNext()) {
            BasicDBObject nuc = (BasicDBObject) cursor.next();
            res.put(nuc.getInt("idx"), nuc);
        }
        cursor.close();
        return res;
    }
    
    public synchronized BasicDBObject[] getObjectsArray(ObjectId nucleusId, int channelIdx, ArrayList<String> fields) {
        BasicDBObject query = new BasicDBObject("nucleus_id", nucleusId).append("channelIdx", channelIdx);
        DBCursor cursor;
        if (fields==null || fields.isEmpty()) cursor = object3D.find(query);
        else {
            BasicDBObject f = new BasicDBObject();
            for (String s : fields) f.append(s, 1);
            cursor = object3D.find(query, f);
        }
        cursor.sort(new BasicDBObject("idx", 1));
        BasicDBObject[] res = new BasicDBObject[cursor.size()];
        int count=0;
        while (cursor.hasNext()) res[count] = (BasicDBObject) cursor.next();
        cursor.close();
        return res;
    }
    
    public synchronized int getObjectCount(ObjectId nucleusId, int channelIdx) {
        BasicDBObject query = new BasicDBObject("nucleus_id", nucleusId).append("channelIdx", channelIdx);
        DBCursor cursor = object3D.find(query).sort(new BasicDBObject("idx", 1));
        int nb = cursor.size();
        cursor.close();
        return nb;
    }
    
    public synchronized BasicDBObject getMeasurementStructure(ObjectId nucId, int[] structures, boolean exactMatch) {
        BasicDBObject query=new BasicDBObject("nucleus_id", nucId);
        if (exactMatch) query.append("structures", structures);
        else query.append("structures", new BasicDBObject("$all", structures));
        DBObject res = structureMeasurement.findOne(query);
        if (res==null) return query;
        else return (BasicDBObject)res;
    }
    
    public synchronized BasicDBObject[] getMeasurementStructure(ObjectId nucId, int[] structures, ArrayList<String> fields, boolean exactMatch) {
        BasicDBObject query=new BasicDBObject("nucleus_id", nucId);
        if (exactMatch) {
            query.append("structures", structures);
            DBObject res;
            if (fields==null || fields.isEmpty()) res = structureMeasurement.findOne(query);
            else {
                BasicDBObject f = new BasicDBObject();
                if (!fields.contains("structures")) fields.add("structures");
                for (String s : fields) f.append(s, 1);
                res = structureMeasurement.findOne(query, f);
            }
            if (res==null) return new BasicDBObject[]{query};
            else return new BasicDBObject[]{(BasicDBObject)res};
        }
        else {
            query.append("structures", new BasicDBObject("$all", structures));
            DBCursor cur;
            if (fields==null || fields.isEmpty()) cur = structureMeasurement.find(query);
            else {
                BasicDBObject f = new BasicDBObject();
                if (!fields.contains("structures")) fields.add("structures");
                for (String s : fields) f.append(s, 1);
                cur = structureMeasurement.find(query, f);
            }
            BasicDBObject[] res= new BasicDBObject[cur.count()];
            int idx=0;
            while(cur.hasNext()){
                res[idx]=(BasicDBObject)cur.next();
                idx++;
            }
            return res;
        }
    }
    
    public synchronized void saveObject3D(BasicDBObject object) {
        //BasicDBObject query = new BasicDBObject("nucleus_id", object3D.get("nucleus_id")).append("channelIdx", object3D.get("channelIdx")).append("idx", object3D.get("idx"));
        this.object3D.save(object);
    }
    
    public synchronized void saveStructureMeasurement(BasicDBObject sm) {
        structureMeasurement.save(sm);
    }
    
    public synchronized void saveNucleus(BasicDBObject nuc) {
        nucleus.save(nuc);
    }

    
    
    
    public synchronized ImageIcon getFieldThumbnail(ObjectId field_id) {
        BasicDBObject query = new BasicDBObject("field_id", field_id);
        GridFSDBFile f = gfsFieldThumbnail.findOne(query);
        if (f!=null) {
            try {
                Image im=ImageIO.read(f.getInputStream());
                if (im!=null) return new ImageIcon(im);
            } catch (Exception e) {
                exceptionPrinter.print(e, "", Core.GUIMode);
            }
        }
        return null;
    }
    
    public synchronized ImageIcon getChannelThumbnail(ObjectId nucleus_id, int fileIdx) {
        BasicDBObject query = new BasicDBObject("nucleus_id", nucleus_id).append("fileRank", fileIdx);
        GridFSDBFile f = gfsNucleusThumbnail.findOne(query);
        if (f!=null) {
            InputStream is = f.getInputStream();
            try {
                Image im=ImageIO.read(is);
                if (im!=null) return new ImageIcon(im);
            } catch (Exception e) {
                exceptionPrinter.print(e, "", Core.GUIMode);
            }
        }
        return null;
    }
    
    public synchronized void removeInputImages(ObjectId fieldId, boolean removeThumbnail) {
        BasicDBObject queryField = new BasicDBObject("field_id", (ObjectId)fieldId);
        this.gfsField.remove(queryField);
        if (removeThumbnail) this.gfsFieldThumbnail.remove(queryField);
    }
    
    public synchronized void removeInputImage(ObjectId field_id, int fileRank) {
        BasicDBObject query = new BasicDBObject("field_id", field_id).append("fileRank", fileRank);
        gfsField.remove(query);
    }
    
    public synchronized boolean saveInputImage(ObjectId field_id, int fileRank, ImageHandler img, boolean flushImage) {
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
            GridFSInputFile gfi = this.gfsField.createFile(data);
            data=null;
            gfi.setFilename(title);
            gfi.put("field_id", field_id);
            gfi.put("fileRank", fileRank);
            gfi.put("pixelDepth", scaleZ);
            gfi.put("unit", unit);
            removeInputImage(field_id, fileRank);
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
    
    public synchronized void removeNucleusImage(ObjectId nucleus_id, int fileIdx, int fileType) {
        BasicDBObject query = new BasicDBObject("nucleus_id", nucleus_id).append("fileIdx", fileIdx).append("fileType", fileType);
        gfsNucleus.remove(query);
    }
    
    public synchronized void saveNucleusImage(ObjectId nucleus_id, int fileIdx, int fileType, ImageHandler img) {
        if (img==null) return;
        removeNucleusImage(nucleus_id, fileIdx, fileType);
        try {
            GridFSInputFile gfi = this.gfsNucleus.createFile(img.getBinaryData());
            gfi.setFilename(img.getImagePlus().getShortTitle());
            gfi.put("nucleus_id", nucleus_id);
            gfi.put("fileIdx", fileIdx);
            gfi.put("fileType", fileType);
            gfi.put("pixelDepth", img.getScaleZ());
            gfi.put("unit", img.getUnit());
            gfi.put("offsetX", img.offsetX);
            gfi.put("offsetY", img.offsetY);
            gfi.put("offsetZ", img.offsetZ);
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
    
    public synchronized ImageHandler getInputImage(ObjectId field_id, int idx) {
        BasicDBObject query = new BasicDBObject("field_id", field_id).append("fileRank", idx);
        GridFSDBFile f = this.gfsField.findOne(query);
        if (f!=null) return createImage(f);
        return null;
    }
    
    public synchronized ImageHandler getNucImage(ObjectId cellId, int fileIdx, int fileType) {
        BasicDBObject query = new BasicDBObject("nucleus_id", cellId).append("fileIdx", fileIdx).append("fileType", fileType);
        GridFSDBFile f = this.gfsNucleus.findOne(query);
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
                if (imp!=null) {
                    ImageHandler ih= ImageHandler.wrap(imp);
                    if (file.containsField("offsetX")) ih.offsetX=(Integer)file.get("offsetX");
                    if (file.containsField("offsetY")) ih.offsetY=(Integer)file.get("offsetY");
                    if (file.containsField("offsetZ")) ih.offsetZ=(Integer)file.get("offsetZ");
                    return ih;
                }
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
    
    /////////////////////////////////HELP////////////////////
    public synchronized void setHelp(ID id, String help) {
        BasicDBObject query = new BasicDBObject("container", id.container).append("element", id.element);
        this.help.remove(query);
        this.help.save(query.append("help", help));
    }
    public String getHelp(ID id) {
        DBObject dbo = help.findOne(new BasicDBObject("container", id.container).append("element", id.element));
        try {
            if (dbo!=null) return (String)dbo.get("help");
        } catch (Exception e) {
            
        }
        return null;
    }
    
    ////////////////////SELECTIONS//////////////////////////
    public synchronized BasicDBList getSelectedCells(ObjectId xpId) {
        DBObject dbo = selection.findOne(new BasicDBObject("experiment_id", xpId));
        if (dbo!=null) {
            return ((BasicDBList)dbo.get("selection"));
        } else return null;
    }
    
    public synchronized BasicDBList getSelectedObjects(ObjectId nucId, int structureIdx) {
        DBObject dbo = selection.findOne(new BasicDBObject("nucleus_id", nucId).append("structure_idx", structureIdx));
        if (dbo!=null) {
            return ((BasicDBList)dbo.get("selection"));
        } else return null;
    }
    
    
    public void testDuplicateIds(MongoConnector other) {
        DBCursor cur = this.object3D.find(new BasicDBObject(), new BasicDBObject());
        DBCursor cur2 = other.object3D.find(new BasicDBObject(), new BasicDBObject());
        System.out.println("items:"+cur.count()+ " items2:"+cur2.count());
        int count = 0;
        while (cur.hasNext()) {
            DBObject o = cur.next();
            if (other.object3D.findOne(new BasicDBObject("_id", o.get("_id")))!=null) count++;
        }
        System.out.println("Duplicate:"+count);
    }
    
    public CommandResult getCmdLines(){
        DB db = this.m.getDB("admin");
        DBObject cmd = new BasicDBObject();
        cmd.put("getCmdLineOpts", 1);
        return db.command(cmd);
    }
}
