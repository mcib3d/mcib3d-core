package tango.gui.util;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCursor;
import ij.IJ;
import tango.dataStructure.Experiment;
import tango.dataStructure.Field;
import tango.gui.util.FileFilterTIF;
import mcib3d.utils.exceptionPrinter;
import ij.ImagePlus;
import ij.measure.Calibration;
import ij.plugin.ChannelSplitter;
import mcib3d.image3d.ImageHandler;
import java.io.File;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryUsage;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import loci.formats.FilePattern;
import loci.formats.FormatException;
import loci.formats.ImageReader;
import loci.plugins.BF;
import loci.plugins.LociImporter;
import loci.plugins.in.ImporterOptions;
import org.bson.types.ObjectId;
import tango.gui.Core;
import tango.gui.FieldManager;
import tango.mongo.MongoConnector;
import tango.util.Progressor;
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
public class FieldFactory {
    public final static DecimalFormat df = new DecimalFormat("000");
    public final static String[] importMethod = new String[] {"Bioformats-LOCI", "Keywords (.tif)", "File order (.tif)"};
    
    public static Field[] getFields(Experiment xp) {
        DBCursor cursor = xp.getConnector().getFields(xp.getId());
        Field[] res = new Field[cursor.size()];
        for (int i = 0; i < res.length; i++) {
            res[i] = new Field((BasicDBObject) cursor.next(), xp);
        }
        cursor.close();
        return res;
    }
    
    public static void createFields(Experiment xp, File[] input) {
        if (xp.getImportFileMethod().equals(importMethod[1])) { //keywords
            for (File f : input) createFieldsKeyword(xp, f);
        } else if (xp.getImportFileMethod().equals(importMethod[2])) {//rank
            for (File f : input) createFieldsFileOrder(xp, f);
        } else if (xp.getImportFileMethod().equals(importMethod[0])){ //LOCI
            createFieldsFromFile(xp, input);
        }
    }

    private static void createFieldsKeyword(Experiment xp, File input) {
        if (!input.isDirectory()) {
            return;
        }
        File[] subinput = input.listFiles(new FileFilterTIF());
        int nucRank = xp.getChannelFileIndex(0);
        String[] keywords = xp.getFileKeywords();
        String nucleusKeyword = keywords[nucRank];
        
        if (subinput.length >= xp.getNBFiles()) {
            for (int f = 0; f < subinput.length; f++) {
                if (subinput[f].getName().indexOf(nucleusKeyword) >= 0) {
                    boolean allFilesLoaded=true;
                    ImageHandler[] files = new ImageHandler[xp.getNBFiles()];
                    try {
                        // noyau
                        IJ.log("Loading nucleus " + subinput[f]);
                        files[nucRank] = ImageHandler.openImage(subinput[f]);
                        // channels
                        for (int k = 0; k < xp.getNBFiles(); k++) {
                            if (k==nucRank) continue;
                            String name = input + File.separator + subinput[f].getName().replace(nucleusKeyword, keywords[k]);
                            IJ.log("Loading channel " + name);
                            File chan = new File(name);
                            if (chan.exists()) files[k] = ImageHandler.openImage(chan);
                            else {
                                allFilesLoaded=false;
                                ij.IJ.log("Error loading field: missing file:"+name);
                                break;
                            }
                        }
                        if (allFilesLoaded) createField(xp, subinput[f].getName().replace(nucleusKeyword, ""), files);
                    } catch (Exception e) {
                        exceptionPrinter.print(e, "", Core.GUIMode);
                    }
                }
            }
        }
        File[] subinputFolder = input.listFiles(new FileFilterFolder());
        for (File f : subinputFolder) {
            createFieldsKeyword(xp, f);
        }
    }

    private static void createFieldsFileOrder(Experiment xp, File input) { //recursif
        if (input.isDirectory()) {
            IJ.log("dir");
            File[] subinput = input.listFiles(new FileFilterTIF());
            Arrays.sort(subinput);
            if (subinput.length >= xp.getNBFiles()) {
                ImageHandler[] files = new ImageHandler[xp.getNBFiles()];
                int count = 0;
                int i = 0;
                while (count < files.length && i < subinput.length) {
                    try {
                        files[count] = ImageHandler.openImage(subinput[i]);
                        i++;
                        count++;
                    } catch (Exception e) {
                        i++;
                        exceptionPrinter.print(e, "", Core.GUIMode);
                    }
                }
                if (count == files.length) {
                    createField(xp, input.getName(), files);
                }
            }
            /*File[] subinputZVI = input.listFiles(new FileFilterZVI());
            for (File f : subinputZVI) {
                createFieldsFileOrder(xp, f);
            }
            * 
            */
            File[] subinputFolder = input.listFiles(new FileFilterFolder());
            for (File f : subinputFolder) {
                createFieldsFileOrder(xp, f);
            }

        } /*else if (input.isFile() && input.getName().matches(".*zvi$")) {
            String n = input.getName();
            String name = n.substring(0, n.length() - 4);
            ZVIReader zr = new ZVIReader();
            ImagePlus[] channels = zr.run(input);
            ImageHandler[] files = new ImageHandler[xp.getNBFiles()];
            if (channels.length >= xp.getNBFiles()) {
                int i = 0;
                while (i < files.length) {
                    String chanName = name + "_Ch" + i;
                    channels[i].setTitle(chanName);
                    files[i] = ImageHandler.wrap(channels[i]);
                    i++;
                }
                createField(xp, name, files);
            }
        }
        * 
        */
    }

    private static void createField(Experiment xp, String name, ImagePlus hyperstack) {
        ImagePlus[] channels = ChannelSplitter.split(hyperstack);
        createField(xp, name, channels);
    }
    
    private static void createField(Experiment xp, String name, ImagePlus[] channels){
        ImageHandler[] ims= new ImageHandler[channels.length];
        for (int i = 0;i<ims.length; i++) ims[i]=ImageHandler.wrap(channels[i]);
        createField(xp, name, ims);
    }
    
    private static void createField(Experiment xp, String name, ImageHandler[] inputFiles) {
        if (inputFiles.length!=xp.getNBFiles()) {
            ij.IJ.log("wrong number of channels for file:"+name+ ".\nfound:"+inputFiles.length+ " requiered:"+xp.getNBFiles());
            return;
        }
        if (xp.getChannelFileIndexes()==null || xp.getChannelFileIndexes().length==0 || xp.getChannelFileIndexes()[0]<0) {
            ij.IJ.log("Error no channel file associated to nucleus structure");
            return;
        }
        boolean fieldExists = xp.getConnector().fieldExists(xp.getName(), name);
        BasicDBObject field = xp.getConnector().getField(xp.getName(), name);
        ObjectId field_id = (ObjectId) field.get("_id");
        inputFiles[xp.getChannelFileIndexes()[0]].setGraysLut();
        xp.getConnector().saveFieldThumbnail(field_id, inputFiles[xp.getChannelFileIndexes()[0]], 50, 50);
        boolean savedImages = true;
        for (int i = 0; i < inputFiles.length; i++) {
            inputFiles[i].setGraysLut();
            savedImages=xp.getConnector().saveInputImage(field_id, i, inputFiles[i], true); //removes image if existing
            inputFiles[i]=null;
            if (!savedImages) break;
        }
        if (!savedImages){
            if (!fieldExists) xp.getConnector().removeField(field_id);
        }
    }

    public static Field createVirtualField(Experiment xp, String name) {
        BasicDBObject field = xp.getConnector().getField(xp.getName(), name);
        return new Field(field, xp);
    }
    
    public static void createFieldsFromFile(final Experiment xp, final File[] files) {
        final ArrayList<File> alf = getFilesRecursive(files);
        Progressor p = Core.getProgressor();
        p.setAction("Importing Files");
        p.resetProgress(alf.size());
        for (File f : alf) {
            createFieldFromFile(xp, f);
            p.incrementStep();
        }
    }
    
    private static void createFieldFromFile(Experiment xp, File file) {
        try {
            ij.IJ.log("opening file:"+file.getAbsolutePath());
            System.gc();
            double maxMem = Core.getMaxMemory();
            double size = file.length() / (1024*1024);
            if (maxMem<2*size) {
                IJ.log("Warning: Maximum memory ("+maxMem+") has to be superior to the file size ("+size+") (We advise 2 times the file size). Please refer to ImageJ manual to increase memory");
            }
            /*
            long ds = MongoConnector.getDiskspaceDBPath();
            String dbPath = MongoConnector.getDBPath();
            if (ds>=0 && ds<=size*1.1) IJ.log("Warning: low disk space: Maximum available space: "+ds+"Mb in directory"+ dbPath +", File size: "+size+"Mb");
            */
            ImporterOptions options = new ImporterOptions();
            options.setId(file.getAbsolutePath());
            options.setOpenAllSeries(true);
            options.setQuiet(true);
            options.setGroupFiles(false);
            options.setSplitChannels(false);
            options.setSplitFocalPlanes(false);
            options.setSplitTimepoints(false);
            ImagePlus[] imps=null;
            MemoryMXBean memoryBean = ManagementFactory.getMemoryMXBean();
            try {
                    imps= BF.openImagePlus(options);

            } catch (FormatException exc) {
                IJ.log("Error while opening image: "+file.getAbsolutePath()+ "::" + exc.getMessage());
            } catch (Exception e) {
                exceptionPrinter.print(e, "Error while opening file: "+file.getAbsolutePath(), true);
            } catch (OutOfMemoryError e) {
                int MEGABYTE = (1024*1024);
                MemoryUsage heapUsage = memoryBean.getHeapMemoryUsage();
                long maxMemory = heapUsage.getMax() / MEGABYTE;
                long usedMemory = heapUsage.getUsed() / MEGABYTE;
                IJ.log("Error while opening file:"+file.getAbsolutePath()+ ":: Out of memory. Memory Use :" + usedMemory + "M/" + maxMemory + "M");
            }
            if (imps!=null) for (int i =0; i<imps.length; i++) createField(xp, imps[i].getTitle(), imps[i]); //name+"_"+df.format(i)
        }
        
        catch (IOException exc) {
            IJ.log("Sorry, an error occurred: " + exc.getMessage());
        } catch (Exception e) {
            exceptionPrinter.print(e, "Error2 while opening file: "+file.getAbsolutePath(), true);
        } 
    }
    
    protected static ArrayList<File> getFilesRecursive(File[] input) {
        ArrayList<File> files = new ArrayList<File>();
        for (File f : input) {
            if (f.isDirectory()) addFiles(files, f);
            else files.add(f);
        }
        return files;
    }
    
    protected static void addFiles(ArrayList<File> files, File folder) {
        if (folder.isDirectory()) {
            File[] subFiles=folder.listFiles();
            for (File f : subFiles) {
                if (f.isDirectory()) addFiles(files, f);
                else files.add(f);
            }
        }
    }
    
}
