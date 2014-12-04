package tango.dataStructure;

import tango.util.ImageUtils;
import mcib3d.utils.ThreadRunner;
import mcib3d.image3d.ImageHandler;
import mcib3d.image3d.ImageInt;
import mcib3d.utils.exceptionPrinter;
import tango.mongo.MongoConnector;
import tango.mongo.MongoUtils;
import java.io.*;
import java.util.*;
import ij.*;
import ij.io.*;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.mongodb.BasicDBList;
import com.mongodb.DBCursor;
import tango.gui.util.FileFilterCell;
import tango.gui.util.FileFilterTIF;
import i5d.ChannelImagePlus;
import i5d.Image5D;
import i5d.cal.ChannelDisplayProperties;
import i5d.gui.ChannelControl;
import java.awt.Color;
import java.awt.Transparency;
import java.awt.color.ColorSpace;
import java.awt.image.ColorModel;
import java.awt.image.ComponentColorModel;
import java.awt.image.DataBuffer;
import java.awt.image.IndexColorModel;
import javax.swing.ImageIcon;
import mcib3d.geom.Object3D;
import mcib3d.image3d.*;
import org.bson.types.ObjectId;
import tango.gui.Core;
import tango.parameter.PreFilterSequenceParameter;
import tango.plugin.segmenter.NucleusSegmenterRunner;
import tango.plugin.filter.PostFilterSequence;
import tango.plugin.filter.PreFilterSequence;
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
public class Field implements ObjectStructure, StructureContainer {

    BasicDBObject properties;
    ArrayList<Cell> cells;
    String name;
    Experiment xp;
    ObjectId id;
    MongoConnector mc;
    ImageIcon thumbnail;
    InputFieldImages inputImages;
    ImageInt segmented;
    Object3D[] objects;
    boolean verbose;
    int nbCPUs=1;
    
    public Field(BasicDBObject dbField, Experiment xp) {
        this.xp = xp;
        this.id = (ObjectId) dbField.get("_id");
        this.name = dbField.getString("name");
        this.mc = xp.getConnector();
        this.thumbnail = mc.getFieldThumbnail(id);
        inputImages = new InputFieldImages(this);
        this.verbose=false;
        this.nbCPUs=Core.getMaxCPUs();
    }
    
    public void setVerbose(boolean verbose) {
        this.verbose=verbose;
    }

    @Override
    public ObjectId getId() {
        return id;
    }

    public void closeInputImages() {
        inputImages.closeAll();
    }
    
    public boolean hasOpenedCellImages() {
        for (Cell c: cells) if(c.hasOpenedImages()) return true;
        return false;
    }

    public void closeOutputImages() {
        this.objects = null;
        if (this.segmented != null) {
            this.segmented.flush();
            this.segmented = null;
        }
        /*
         * if (this.postProcessed!=null) { this.postProcessed.flush();
         * this.postProcessed=null; }
         *
         */
    }

    public int getFileRank(int channelIdx) {
        return xp.getChannelFileIndexes()[channelIdx];
    }

    public Experiment getExperiment() {
        return xp;
    }

    public ImageHandler getStructureInputImage(int structureIdx) {
        if (structureIdx >= 0) {
            ImageHandler im = inputImages.getImage(structureIdx);
            if (im!=null) xp.setCalibration(im);
            return im;
        } else {
            return openInputImage(structureIdx);
        }
    }
    
    public void deleteSlices(int keepStart, int keepStop) {
        for (int i = 0; i<xp.getNBFiles(); i++) {
            ImageHandler file = inputImages.getChannelFile(i);
            if (file!=null) {
                file.trimSlices(keepStart, keepStop);
                //file.showDuplicate("slices deleted");
                this.mc.saveInputImage(id, i, file, false);
            }
        }
        xp.getConnector().saveFieldThumbnail(id, inputImages.getChannelFile(xp.getChannelFileIndexes()[0]), 50, 50);
        if (segmented==null) segmented = (ImageInt) mc.getNucImage(id, 0, MongoConnector.MASKS);
        if (segmented!=null) {
            segmented.trimSlices(keepStart, keepStop);
            saveOutput();
        }
        for (ImageHandler i : inputImages.filteredImages) if (i!=null) i.trimSlices(keepStart, keepStop);
    }

    public ImageHandler getFilteredInputImage() {
        return inputImages.getFilteredImage(0);
    }

    public InputFieldImages getInputImages() {
        return inputImages;
    }
    
    @Override
    public ImageInt getMask() {
        ImageHandler ref = inputImages.getImage(0);
        if (ref!=null) return new BlankMask(ref);
        else return null;
    }
    
    @Override
    public ImageHandler openInputImage(int fileIdx) {
        try {
            ImageHandler res = null;
            if (fileIdx >= 0) {
                res = mc.getInputImage(id, fileIdx);
                if (res==null) ij.IJ.log("No input image found for field: "+getName());
            } else if (fileIdx == MongoConnector.MASKS) {
                return openSegmented();
            }
            if (res != null) {
                xp.setCalibration(res);
            }
            return res;
        } catch (Exception e) {
            exceptionPrinter.print(e, "", Core.GUIMode);
        }
        return null;
    }

    public Image5D getImage5D() {
        ImageHandler[] channels = new ImageHandler[this.xp.getNBStructures(false)];
        for (int i = 0; i < this.xp.getNBStructures(false); i++) {
            channels[i] = this.getStructureInputImage(i);
            //System.out.println("get Image5D: structure:"+i+ " channelFile:"+xp.getChannelFileIndexes()[i]+ " file null?"+(channels[i]==null));
        }
        Image5D res = ImageUtils.getImage5D(name, channels);
        for (int i = 0; i < this.xp.getNBStructures(false); i++) {
            Color c = tango.gui.util.Colors.colors.get(xp.getChannelSettings(i).getString("color"));
            if (c != null) {
                ColorModel cm = ChannelDisplayProperties.createModelFromColor(c);
                res.setChannelColorModel(i + 1, cm);
            }
            res.getChannelCalibration(i + 1).setLabel(xp.getChannelSettings(i).getString("name"));
        }
        res.setDisplayMode(ChannelControl.OVERLAY);

        return res;
    }

    public ImageIcon getThumbnail() {
        return this.thumbnail;
    }

    public String getXPName() {
        return this.xp.getName();
    }

    public String getName() {
        return name;
    }

    public ArrayList<Cell> getCells() {
        return cells;
    }
    
    public ImageHandler preFilterChannel(ImageHandler input, int channelIdx) {
        PreFilterSequenceParameter sequence = xp.getChannelFilePreFilterSequence(channelIdx);
        if (sequence!=null) {
            return sequence.runPreFilterSequence(0, input, inputImages, Core.getMaxCPUs(), this.verbose);
        } else return input;
    }

    @Override
    public ImageHandler preFilterStructure(ImageHandler input, int structureIdx) {
        if (structureIdx==0) {
            //filter channel
            ImageHandler preFilter = preFilterChannel(input, xp.getChannelFileIndex(0));
            //filter stucture
            PreFilterSequence pfs = xp.getPreFilterSequence(0, nbCPUs, verbose);
            if (pfs.isEmpty()) {
                 return preFilter;
            } else {
                ImageHandler res = pfs.run(0, preFilter, this.inputImages);
                preFilter.closeImagePlus();
                return res;
            }
        }
        return input;
    }

    public int[] processNucleus() {
        try {
            System.gc();
            ImageHandler in = inputImages.getFilteredImage(0);
            if (this.verbose) in.showDuplicate("pre Filtered image");
            NucleusSegmenterRunner nsr = xp.getNucleusSegmenterRunner(nbCPUs, verbose);
            segmented = nsr.run(0, in, inputImages);
            if (segmented == null) return null;
            IJ.log("segmenting... " + segmented.getTitle());
            segmented.setTitle(in.getTitle() + "_masks");
            segmented.set332RGBLut();
            segmented.setScale(in);
            if (this.verbose) segmented.showDuplicate("Segmented image");
            segmented = postFilterStructure(segmented, 0);
            if (this.verbose) segmented.showDuplicate("Post-Filtered image");
            return null; //return nsr.getTags();
        } catch (Exception e) {
            exceptionPrinter.print(e, "", Core.GUIMode);
        }
        return null;
    }
    
    public void testProcess(int step, int subStep) {
        try {
            System.gc();
            if (step==0) {
                ImageHandler in = inputImages.getImage(0);
                in = preFilterChannel(in, xp.getChannelFileIndex(0));
                PreFilterSequence pofs= xp.getPreFilterSequence(0, nbCPUs, verbose);
                pofs.test(0, in, inputImages, subStep, false);
            } else if (step==1) {
                ImageHandler in = inputImages.getFilteredImage(0);
                in.showDuplicate("Before Segmentation");
                NucleusSegmenterRunner nsr = xp.getNucleusSegmenterRunner(nbCPUs, true);
                segmented = nsr.run(0, in, inputImages);
                segmented.set332RGBLut();
                segmented.showDuplicate("After Segmentation");
            } else if (step==2) {
                ImageHandler in = inputImages.getFilteredImage(0);
                NucleusSegmenterRunner nsr = xp.getNucleusSegmenterRunner(nbCPUs, false);
                ImageInt segmented = nsr.run(0, in, inputImages);
                PostFilterSequence pfs = xp.getPostFilterSequence(0, nbCPUs, verbose);
                pfs.test(0, segmented, inputImages, subStep, false);
            }
        } catch (Exception e) {
            exceptionPrinter.print(e, "", Core.GUIMode);
        }
    }

    @Override
    public void saveOutput() {
        mc.saveNucleusImage(id, 0, MongoConnector.MASKS, segmented);
        //mc.saveInputImage(id, mongoConnector.MASKS, mask);
    }

    public ImageInt openSegmented() {
        ImageHandler mask;
        mask = mc.getNucImage(id, 0, MongoConnector.MASKS);
        if (mask == null) {
            ImageHandler in = getStructureInputImage(0);
            if (in == null) {
                ij.IJ.log("No input image found for Field:"+getName());
                return null;
            }
            mask = new ImageShort(getName() + "::segmented", in.sizeX, in.sizeY, in.sizeZ);
        }
        xp.setCalibration(mask);
        segmented = (ImageInt) mask;
        return segmented;
    }

    @Override
    public ImageInt postFilterStructure(ImageInt input, int structureIdx) {
        System.gc();
        PostFilterSequence pfs = xp.getPostFilterSequence(0, nbCPUs, verbose);
        ImageInt res = pfs.run(0, input, inputImages);
        System.gc();
        return res;
    }

    public boolean shiftObjectIndexes() {
        TreeMap<Integer, int[]> bounds = segmented.getBounds(false);
        boolean change = segmented.shiftIndexes(bounds);
        if (change) {
            objects = null;
        }
        return change;
    }
    
    public void cropCells(int[] tags) { 
        deleteCells();
        try {
            ImageInt in=this.getSegmented();
            ImageInt[] masks = in.crop3DBinary();
            ObjectId[] cellIds = new ObjectId[masks.length];
            for (int j = 0; j < masks.length; j++) {
                if (masks[j] != null) {
                    String cellName = "cell" + Cell.df3.format(j + 1);
                    DBObject nucleus = mc.getNucleus(xp.getName(), name, j + 1, null);
                    cellIds[j] = (ObjectId) nucleus.get("_id");
                    masks[j].setTitle(cellName + "_Ch0_S");
                    mc.saveNucleusImage(cellIds[j], 0, MongoConnector.S, masks[j]);
                    //set Tags...
                    if (tags != null && tags.length>j) {
                        mc.setNucleusTag(cellIds[j], tags[j]);
                    }
                }
            }

            for (int i = 0; i < xp.getNBFiles(); i++) {
                ImageHandler ih = inputImages.getChannelFile(i);
                if (ih == null) ij.IJ.log("Error: Field:"+name+ " channel image not found:"+i);
                else {
                    ImageHandler preFilter = preFilterChannel(ih, i);
                    for (int j = 0; j<masks.length; j++) { //MULTITHREAD
                        if (masks[j]!=null) {
                            ImageHandler im =preFilter.crop3D("cell"+Cell.df3.format(j+1)+"_File"+i+".tif", masks[j].offsetX, masks[j].offsetX+masks[j].sizeX-1, masks[j].offsetY, masks[j].offsetY+masks[j].sizeY-1, masks[j].offsetZ, masks[j].offsetZ+masks[j].sizeZ-1);
                            //im.showDuplicate("j"+Cell.df3.format(j+1)+"_File"+i+".tif");
                            mc.saveNucleusImage(cellIds[j], i, MongoConnector.R, im);
                            mc.saveChannelImageThumbnail(cellIds[j], i, im, 25, 25, masks[j]);
                            im.closeImagePlus();
                        }
                    }
                    ih.closeImagePlus();
                    preFilter.closeImagePlus();
                }
            }
            for (int j = 0; j < masks.length; j++) {
                if (masks[j] != null) {
                    masks[j].closeImagePlus();
                }
            }
        } catch (Exception e) {
            exceptionPrinter.print(e, "", Core.GUIMode);
        }
    }

    public final void createCells() {
        try {
            DBCursor cur = mc.getFieldNuclei(xp.getName(), name);
            int nbCells = cur.size();
            this.cells = new ArrayList<Cell>(nbCells);
            //IJ.log("creating cells.. field:"+name+ " nb cell found:"+nbCells);
            for (int i = 0; i < nbCells; i++) {
                Cell c = new Cell((BasicDBObject) cur.next(), this, xp);
                cells.add(c);
                c.createChannels();
                //IJ.log("creating cells.. cell:"+i);
            }
            cur.close();
        } catch (Exception e) {
            exceptionPrinter.print(e, "", Core.GUIMode);
        }
    }

    public Cell createCellFromFiles(String cellName, ImageHandler[] rawFiles, ImageHandler[] segFiles, ImageHandler[] probaMaps) {
        if (cells == null) {
            createCells();
        }
        int cellIdx = this.cells.size();
        String cName = "cell" + Cell.df3.format(cellIdx + 1);
        if (cellName != null) {
            cName += "_" + cellName;
        }
        DBObject nucleus = mc.getNucleus(xp.getName(), name, cellIdx + 1, cName);
        ObjectId id = (ObjectId) nucleus.get("_id");
        if (segFiles != null) {
            for (int i = 0; i < segFiles.length; i++) {
                if (segFiles[i] != null) {
                    mc.saveNucleusImage(id, i, MongoConnector.S, segFiles[i]);
                }
            }
        }
        int[] fileRank = xp.getChannelFileIndexes();
        if (rawFiles != null) {
            for (int i = 0; i < rawFiles.length; i++) {
                if (rawFiles[i] != null) {
                    mc.saveNucleusImage(id, fileRank[i], MongoConnector.R, rawFiles[i]);
                    mc.saveChannelImageThumbnail(id, fileRank[i], rawFiles[i], 25, 25, null);
                }
            }
        }
        if (probaMaps != null) {
            for (int i = 0; i < probaMaps.length; i++) {
                if (probaMaps[i] != null) {
                    mc.saveNucleusImage(id, i, MongoConnector.SP, probaMaps[i]);
                }
            }
        }
        Cell c = new Cell((BasicDBObject) nucleus, this, xp);
        cells.add(c);
        c.createChannels();
        return c;
    }

    public void delete() {
        mc.removeField(id);
        closeCells();
    }

    public void deleteFiles() {
        mc.removeInputImages(id, false);
    }

    public void removeCell(Cell c) {
        cells.remove(c);
    }

    public void deleteCells() {
        if (cells == null) {
            createCells();
        }
        if (cells != null) {
            while (cells.size() > 0) {
                cells.get(cells.size() - 1).delete();
            }
            cells = null;
        }

    }

    public void saveCellsOutput() throws Exception {
        for (int i = 0; i < cells.size(); i++) {
            if (cells.get(i) != null) {
                cells.get(i).saveOutput();
            }
        }
    }

    public void closeCells() {
        if (cells == null) {
            return;
        }
        for (int i = 0; i < cells.size(); i++) {
            if (cells.get(i) != null) {
                cells.get(i).close();
            }
        }
    }

    public void hide() {
        this.inputImages.hideAll();
    }

    @Override
    public Object3D[] getObjects() {
        if (this.objects == null) createObjects();
        return objects;
    }
    
    @Override
    public void createObjects() {
        objects = getSegmented().getObjects3D();
    }

    @Override
    public ImageInt getSegmented() {
        if (this.segmented == null || !segmented.isOpened()) {
            openSegmented();
        }
        return segmented;
    }

    @Override
    public MongoConnector getConnector() {
        return this.mc;
    }

    @Override
    public String getChannelName() {
        return this.getName();
    }

    @Override
    public int getIdx() {
        return 0;
    }
    
    @Override
    public String toString() {
        return this.name;
    }
}
