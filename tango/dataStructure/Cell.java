package tango.dataStructure;
import mcib3d.image3d.ImageHandler;
import mcib3d.image3d.ImageShort;
import mcib3d.image3d.ImageByte;
import tango.mongo.MongoConnector;
import tango.util.Cell3DViewer;
import ij.*;
import ij.plugin.RGBStackMerge;
import java.text.*;
import com.mongodb.BasicDBObject;
import tango.gui.util.Tag;
import mcib3d.utils.exceptionPrinter;
import i5d.Image5D;
import i5d.cal.ChannelDisplayProperties;
import ij.gui.ImageCanvas;
import org.bson.types.ObjectId;
import mcib3d.geom.*;
import mcib3d.image3d.*;
import ij.gui.ImageWindow;
import java.awt.Color;
import java.awt.image.ColorModel;
import java.io.*;
import javax.swing.ImageIcon;
import tango.gui.Core;
import tango.plugin.measurement.MeasurementSequence;
import tango.plugin.sampler.Sampler;
import tango.util.ImageUtils;
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
public class Cell implements StructureContainer, Comparable<Cell> { //ObjectStructure, 
    public final static DecimalFormat df3 = new DecimalFormat("000");
    public final static DecimalFormat df2 = new DecimalFormat("00");
    static int ascendingOrder = 1;
    int idx;
    private AbstractStructure[] channels;
    protected int nbStructures;
    Field field;
    String name;
    double value;
    InputCellImages inputImages;
    SegmentedCellImages segImages;
    ObjectId id;
    
    Tag tag;
    Experiment xp;
    MongoConnector mc;
    ImageIcon[] thumbnails;
    boolean verbose;
    int nbCPUs=1;
    boolean inSelection;
    public static int structureThumbnail;
    
    public Cell(BasicDBObject dbCell, Field f, Experiment xp) {
        this.field=f;
        this.xp=xp;
        this.mc=xp.getConnector();
        this.nbStructures=xp.getNBStructures(false);
        this.channels=new AbstractStructure[xp.getNBStructures(true)];
        this.inputImages=new InputCellImages(this);
        this.segImages = new SegmentedCellImages (this);
        this.idx=dbCell.getInt("idx");
        setName();
        this.tag=new Tag(dbCell.getInt("tag", 0));  
        this.id=(ObjectId)dbCell.get("_id");
        this.thumbnails=new ImageIcon[xp.getNBStructures(false)];
    }
    
    private void setName() {
        this.name= "cell"+Cell.df3.format(idx);
    }
    
    public ImageIcon getThumbnail(int structure) {
        if (structure>=thumbnails.length || structure<0) return null;
        if (thumbnails[structure]==null) thumbnails[structure]=mc.getChannelThumbnail(id, xp.getChannelFileIndex(structure));
        return thumbnails[structure];
    }
    
    public static void setAscendingOrger(boolean ascending) {
        if (ascending) ascendingOrder=1;
        else ascendingOrder=-1;
    }
    
    public void setValue(double value) {
        this.value=value;
        this.setName();
        this.name+=" - value="+Object3DGui.df.format(value);
    }
    
    public void setVerbose(boolean verbose) {
        this.verbose=verbose;
    }
    
    public boolean getVerbose() {
        return verbose;
    }
    
    public void setNbCPUs(int nbCPUs) {
        this.nbCPUs=nbCPUs;
    }
    
    public boolean isInSelection() {
        return inSelection;
    }
    
    public void setInSelection(boolean inSelection) {
        this.inSelection=inSelection;
    }
    
    public int getNbCPUs() {
        return nbCPUs;
    }
    
    public InputCellImages getRawImages() {
        return inputImages;
    }
    
    public SegmentedCellImages getSegmentedImages() {
        return segImages;
    }
    
    public BasicDBObject getDBNucleus(MongoConnector mc) {
        return mc.getNucleus(id);
    }
    
    public void setConnector(MongoConnector mc) {
        this.mc=mc;
    }
    
    public void resetConnector() {
        this.mc=this.xp.getConnector();
    }
    
    public MongoConnector getConnector() {
        return mc;
    }
    
    public ObjectId getId() {
        return id;
    }
    
    //@Override
    public int getIdx() {
        return 0;
    }
    
    //@Override
    public String getChannelName() {
        return this.getName();
    }
    
    public Experiment getExperiment() {
        return xp;
    }
    
    public Field getField() {
        return field;
    }
    
    public String getName() {
        return name;
    }

    public Tag getTag() {
        return tag;
    }

    public Nucleus getNucleus() {
        return (Nucleus)channels[0];
    }
    
    public boolean hasOpenedImages() {
        return this.segImages.hasOpenedImages()||this.inputImages.hasOpenedImages();
    }
    
    public AbstractStructure getStructure(int idx) {
        return channels[idx];
    }
    
    @Override
    public int getFileRank(int channelIdx) {
        return xp.getChannelFileIndexes()[channelIdx];
    }
    
    public int getNbStructures(boolean addVirtual) {
        if (addVirtual) return channels.length;
        else return nbStructures;
    }
    
    public ImageIcon getThumbnail() { 
        return getThumbnail(structureThumbnail);
    }
    
    public void setTag(int tag) {
        this.tag.setTag(tag);
        mc.setNucleusTag(id, tag);
    } 
    
    public void createChannels() {
        for (int i = 0; i<channels.length;i++) {
            String ChanName = this.name+"_Ch"+i;
            if (i==0) this.channels[i]=new Nucleus(ChanName, this);
            else if (i<nbStructures) this.channels[i]=new Structure(ChanName, i, this);
            else this.channels[i]=VirtualStructure.createStructure(ChanName, i, this);
        }
    }

    public Image5D getImage5D(boolean raw) {
        try {
            ImageHandler [] ihC = new ImageHandler[this.getNbStructures(!raw)];
            for (int i = 0; i<this.getNbStructures(!raw); i++) {
                ihC[i]=raw?inputImages.getImage(i) :segImages.getImage(i);
                if (ihC[i]==null || !ihC[i].isOpened()) ihC[i]=ImageHandler.newBlankImageHandler("", ihC[i-1]);
            }
            Image5D res = ImageUtils.getImage5D(name, ihC);
            for (int i = 0; i<this.getNbStructures(!raw); i++) {
                Color c = tango.gui.util.Colors.colors.get(this.xp.getChannelSettings(i).getString("color"));
                ColorModel cm = ChannelDisplayProperties.createModelFromColor(c);
                res.setChannelColorModel(i+1, cm);
                res.getChannelCalibration(i+1).setLabel(this.xp.getChannelSettings(i).getString("name"));
            }
            String t = raw?"::raw":"::segmented";
            res.setTitle(name+t);
            return res;
        } catch (Exception e) {
            exceptionPrinter.print(e, "", Core.GUIMode);
        } return null;
    }

    public void show3D() throws Exception {
        if (xp.c3Dv==null) {
            xp.c3Dv = new Cell3DViewer();
            xp.c3Dv.show();
        }
        xp.c3Dv.addContent(this, true);
            
        
    }
    
    public void mesure(boolean override) {
        try {
            MeasurementSequence seq = xp.getMeasurementSequence(override);
            seq.run(this, mc);
        } catch (Exception e) {
            String n = (field!=null) ? "field: "+field.name+ " ": "";
            n+="cell: "+name;
            exceptionPrinter.print(e, "mesure: "+n, Core.GUIMode);
        }
    }
    
    public void testMeasure(int measurementIdx) {
        this.verbose=true;
        Core.debug=true;
        MeasurementSequence seq = xp.getMeasurementSequenceTest(measurementIdx);
        seq.run(this, mc);
        this.verbose=false;
        Core.debug=false;
    }
    
    public void testSampler(Sampler sampler) {
        this.verbose=true;
        Core.debug=true;
        sampler.initSampler(inputImages, segImages);
        sampler.setVerbose(true);
        sampler.setMultithread(Core.getMaxCPUs());
        sampler.displaySample();
        this.verbose=false;
        Core.debug=false;
    }
    
    
    public void process(boolean[] processChannel) {
        for (int i = 1; i<channels.length; i++) {
            if (processChannel==null || processChannel[i]) {
                if (verbose) System.out.println("cell:"+name+"process Structure:"+i);
                ((Structure)channels[i]).process();
                
            }
        }
        if (!verbose) { 
            for (int i = 1; i<channels.length; i++) {
                if (processChannel==null || processChannel[i]) {
                    channels[i].saveOutput(); //saves all channels. it is done after every channel are process in case of modifications on other channels (by virtual channels)
                }
            }
            System.out.println("saved..");
        } else {
            //reset segmented images..
            for (int i = 0; i<channels.length; i++) {
                if (processChannel==null || processChannel[i]) {
                    segImages.setSegmentedImage(null, i);
                }
            }
        }
        
    }
    
    public void delete() {
        mc.removeNucleus(id);
        if (field!=null) field.removeCell(this);
        close();
    }
    
    public void deleteChannels() {
        for (int i = 1; i<channels.length; i++) {
            mc.deleteStructure(id, i);
        }
    }
    
    @Override
    public ImageInt getMask(){
        return this.segImages.getImage(0);
    }
    
    @Override
    public ImageHandler preFilterStructure(ImageHandler image, int structureIdx) {
        AbstractStructure ass = getStructure(structureIdx);
        if (ass instanceof Structure) return ((Structure)ass).preFilter(image);
        return image;
    }
    
    @Override
    public ImageInt postFilterStructure(ImageInt image, int structureIdx) {
        AbstractStructure ass = getStructure(structureIdx);
        if (ass instanceof Structure) return ((Structure)ass).postFilter(image);
        return image;
    }
    
    @Override
    public ImageHandler openInputImage(int fileIdx) {
        if (fileIdx>=0) {
            ImageHandler im=mc.getNucImage(id, fileIdx, MongoConnector.R);
            xp.setCalibration(im);
            return im;
        }
        return null;
    }


    public void saveOutput() {
        for (AbstractStructure s : channels) {
            s.saveOutput();
        }
    }
    

    public void close() {
        inputImages.closeAll();
        segImages.closeAll();
    }
    
    public void hide() {
        inputImages.hideAll();
        segImages.hideAll();
    }
    
    @Override
    public boolean equals(Object o) {
        if (o instanceof Cell) return this.id.equals(((Cell)o).id);
        else if (o instanceof String) return this.id.toStringMongod().equals(o);
        else return false;
    }

    @Override
    public int hashCode() {
        return id.toStringMongod().hashCode();
    }
    
    @Override
    public int compareTo(Cell t) {
        if (value<t.value) return -ascendingOrder;
        else if (value>t.value) return ascendingOrder;
        else return 0;
    }

}
