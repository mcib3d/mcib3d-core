package tango.gui;

import com.mongodb.BasicDBObject;
import ij.IJ;
import ij.ImagePlus;
import ij.WindowManager;
import ij.gui.Roi;
import ij.plugin.filter.ThresholdToSelection;
import ij.plugin.frame.RoiManager;
import ij.process.ImageProcessor;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.AdjustmentEvent;
import java.awt.event.MouseWheelEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import mcib3d.geom.Object3D;
import mcib3d.geom.Object3DVoxels;
import mcib3d.geom.Vector3D;
import mcib3d.geom.Voxel3D;
import mcib3d.image3d.ImageHandler;
import mcib3d.image3d.ImageInt;
import mcib3d.utils.exceptionPrinter;
import org.bson.types.ObjectId;
import tango.dataStructure.Field;
import tango.dataStructure.InputCroppedImages;
import tango.dataStructure.Object3DGui;
import tango.dataStructure.ObjectStructure;
import tango.gui.util.LCRenderer;
import tango.gui.util.NucleusManagerLayout;
import tango.gui.util.ObjectManagerLayout;
import tango.helper.HelpManager;
import tango.parameter.DoubleParameter;
import tango.parameter.IntParameter;
import tango.parameter.Parameter;
import tango.plugin.filter.PostFilterSequence;
import tango.plugin.segmenter.NucleusSegmenterRunner;
import tango.util.RoiInterpolator;

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
// TODO : pas  extends ObjectManager
public class NucleusManager extends ObjectManager {
    IntParameter border;
    //DoubleParameter thresholdHigh, thresholdLow;
    RoiManager3D roiManager;
    protected boolean maskChange=false;
    public NucleusManager(Core core, JPanel container) {
        super(core, container);
        this.autoSave=false;
        roiManager = new RoiManager3D(this);
    }
    
    @Override
    public void registerComponents(HelpManager hm) {
        ((NucleusManagerLayout)layout).registerComponents(hm);
        roiManager.registerComponents(hm);
    }
    
    @Override
    public void setStructures(ObjectId id, Object[] field) {
        this.currentChannels = new ObjectStructure[(field.length>0)?1:0];
        if (field.length>0 && field[0]!=null) {
            currentChannels[0] = (Field) field[0];
            roiManager.populateRois(getMask().sizeZ);
        }
        populateObjects();
    }
    
    @Override
    public void toggleIsRunning(boolean isRunning) {
        ((NucleusManagerLayout)this.layout).toggleIsRunning(isRunning);
        this.roiManager.toggleIsRunning(isRunning);
    }
    
    protected ImageInt getMask() {
        return ((Field)this.currentChannels[0]).getSegmented();
    }
    
    protected ImageHandler getInput() {
        //return ((Field)this.currentChannels[0]).getFilteredInputImage(); // filtered image
        return ((Field)this.currentChannels[0]).getStructureInputImage(0);
    }
    
    @Override
    public void show(boolean refresh){
        container.add(layout);
        container.add(roiManager);
    }
    
    @Override
    public void hide(boolean refresh) {
        this.container.remove(layout);
        this.container.remove(roiManager);
        if (refresh) core.refreshDisplay();
    }
    
    @Override
    protected void initPanels() {
        BasicDBObject usr = Core.getMongoConnector().getUser();
        splitDist = new DoubleParameter("Dist", "splitMinDistNuc", 20d, DoubleParameter.nfDEC1);
        splitDist.dbGet(usr);
        splitRad = new DoubleParameter("Rad", "splitRadNuc", 10d, DoubleParameter.nfDEC1);
        splitRad.dbGet(usr);
        border = new IntParameter("Border:", "border", 5);
        border.dbGet(usr);
        //thresholdHigh = new DoubleParameter("High:", "thldHighNuc", 100d, Parameter.nfDEC3);
        //thresholdHigh.dbGet(usr);
        //thresholdLow = new DoubleParameter("Low:", "thldLowNuc", 30d, Parameter.nfDEC3);
        //thresholdLow.dbGet(usr);
        NucleusManagerLayout lay =new NucleusManagerLayout(this);
        showObjects = lay.viewROIs;
        border.addToContainer(lay.borderParam);
        //thresholdHigh.addToContainer(lay.thresholdParam1);
        //thresholdLow.addToContainer(lay.thresholdParam2);
        splitDist.addToContainer(lay.splitParam1);
        splitRad.addToContainer(lay.splitParam2);
        this.listModel = new DefaultListModel();
        this.list = lay.list;
        this.list.setModel(listModel);
        this.list.setCellRenderer(new LCRenderer());
        this.list.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        this.list.setLayoutOrientation(JList.VERTICAL);
        listSelectionModel = list.getSelectionModel();
        listSelectionModel.addListSelectionListener(this);
        
        this.layout=lay;
    }
    
    protected Object3DGui newObject() {
        Object3DVoxels o = new Object3DVoxels();
        o.setValue(getNextLabel());
        Object3DGui ogui = new Object3DGui(o, this.currentChannels[0]);
        this.listModel.addElement(ogui);
        return ogui;
    }
    
    
    public void showMask() {
        getMask().show();
    }
    
    public void openImage() {
        getInput().show();
    }
    
    public void revert() {
        ((Field)this.currentChannels[0]).closeOutputImages();
        populateObjects();
        getMask().show();
    }
    
    public void processObjects(boolean process, boolean instersectWithMask, boolean postProcess, boolean test) {
        int borderSize = this.border.getIntValue(5);
        if (test) {
            Object3DGui o3D = (Object3DGui)list.getSelectedValue();
            process(o3D, process, instersectWithMask, postProcess, borderSize, test);
        } else {
            for (Object o : list.getSelectedValues()) {
                Object3DGui o3D = (Object3DGui)o;
                process(o3D, process, instersectWithMask, postProcess, borderSize, test);
            }
        }
        
        if (maskChange) getMask().getImagePlus().updateAndDraw();
        BasicDBObject usr = Core.mongoConnector.getUser();
        this.border.dbPut(usr);
        Core.mongoConnector.saveUser(usr);
    }
    /*
    public void postProcessObjects() {
        for (Object o : list.getSelectedValues()) {
            Object3DGui o3D = (Object3DGui)o;
            postProcess(o3D);
        }
    }
    * 
    */
    
    public void saveMask() {
        if (getMask()!=null && getMask().isOpened()) {
            this.currentChannels[0].saveOutput();
            this.maskChange=false;
        }
    }
    
    protected int getNextLabel() {
        int max=0;
        for (int i = 0; i<listModel.getSize(); i++) {
            Object3DGui o = (Object3DGui) listModel.get(i);
            if (o.getLabel()>max) max=o.getLabel();
        }
        return max+1;
    }
    
    protected Roi[] getCurrentLabelRois() {
        ImageInt maskImage = getMask();
        Object3DGui o = (Object3DGui)list.getSelectedValue();
        int l = o.getLabel();
        ThresholdToSelection tts = new ThresholdToSelection();
        tts.setup("", maskImage.getImagePlus());
        Roi[] rois = new Roi[maskImage.sizeZ];
        for (int z = 1; z<=maskImage.sizeZ; z++) {
            ImageProcessor ip = maskImage.getImagePlus().getStack().getProcessor(z);
            ip.setThreshold(l, l, ImageProcessor.NO_LUT_UPDATE);
            tts.run(ip);
            Roi roi = maskImage.getImagePlus().getRoi();
            if (roi!=null) {
                roi.setPosition(z);
                rois[z-1]=roi;
            }
        }
        return rois;
    }
    
    protected void addRoisToMask() {
        Object[] masks = list.getSelectedValues();
        if (masks.length>1 || masks.length==0) {
            ij.IJ.error("Invalid Selection! Select only 1 object.");
            return;
        }
        Object3DGui o = (Object3DGui) masks[0];
        Roi[] rois = roiManager.getROIs();
        for (Roi roi : rois) addToMask(o, roi, false);
        Object3DVoxels ov = (Object3DVoxels)o.getObject3D();
        ov.setVoxels(ov.getVoxels()); //maj object 3D
        getMask().getImagePlus().updateAndDraw();
        this.maskChange=true;
    }
    
    private void addToMask(Object3DGui o, Roi roi, boolean update) {
        if (roi==null) return;
        int l = o.getLabel();
        if (l==0) return;
        ImageInt maskImage = getMask();
        Roi roi2 = (Roi)roi.clone();
        maskImage.getImagePlus().setSlice(roi.getPosition());
        maskImage.getImagePlus().setRoi(roi2);

        ArrayList<Voxel3D> vox = o.getObject3D().getVoxels();
        ImageProcessor p = maskImage.getImagePlus().getProcessor();
        p.setColor(l);
        p.fill(roi2);
        //p.setRoi(roi2);
        Rectangle r = roi2.getBounds();
        
        //IJ.log("bounds:"+r);
        int z = maskImage.getImagePlus().getSlice()-1;
        for (int y = r.y; y<r.y+r.height; y++) {
            for (int x = r.x; x<r.x+r.width; x++) {
                int xy = x+y*maskImage.sizeX;
                //IJ.log("vox: x:"+x+" y:"+y+" z:"+z+ " value:"+maskImage.pixels[z][xy]);
                if (maskImage.getPixel(xy, z)==l) {
                    vox.add(new Voxel3D(x, y, z, l)); // doublons possibles..mais pas genants 
                }
            }
        }
        if (update) maskImage.getImagePlus().updateAndDraw();
        maskChange=true;
    }
    
    
    private void removeFromMask() {
        Object[] masks = list.getSelectedValues();
        if (masks.length>1 || masks.length==0) {
            ij.IJ.error("Invalid Selection! Select only 1 object.");
            return;
        }
        ImageInt maskImage = getMask();
        Roi roi = maskImage.getImagePlus().getRoi();
        Object3DGui o = (Object3DGui)masks[0];
        int l = o.getLabel();
        if (l==0) return;
        ArrayList<Voxel3D> vox = o.getObject3D().getVoxels();
        if (vox==null) return;
        Rectangle r = roi.getBounds();
        //get Voxels to Redraw
        ArrayList<Voxel3D> voxelToReDraw = new ArrayList<Voxel3D>();
        int z = maskImage.getImagePlus().getSlice()-1;
        for (int y = r.y; y<r.y+r.height; y++) {
            for (int x = r.x; x<r.x+r.width; x++) {
                int curLabel = maskImage.getPixelInt(x+y*maskImage.sizeX, z);
                if (curLabel>0 && curLabel!=l) {
                    voxelToReDraw.add(new Voxel3D(x, y, z, curLabel));
                }
            }
        }
        ImageProcessor p = maskImage.getImagePlus().getProcessor();
        p.setColor(0);
        p.fill(roi);
        for (int i = 0; i<vox.size(); i++) {
            Voxel3D v = vox.get(i);
            if (maskImage.getPixelInt(v.getRoundX(), v.getRoundY(), v.getRoundZ())==0) {
                vox.remove(i);
                i--;
            }
        }
        for (Voxel3D v : voxelToReDraw) maskImage.setPixel(v.getRoundX(),v.getRoundY(), v.getRoundZ(), (int)v.getValue());
        maskImage.getImagePlus().updateAndDraw();
        maskChange=true;
    }
    
    /*
    private void threshold(Object3DGui o) {
        ImageHandler rawImage = getInput();
        ImageInt maskImage = getMask();
        double thld = this.thresholdLow.getFloatValue(0);
        ArrayList<Voxel3D> vox = o.getObject3D().getVoxels();
        if (vox==null) return;
        ArrayList<Voxel3D> newVox = new ArrayList<Voxel3D>(vox.size());
        for (Voxel3D  v : vox) {
            if (rawImage.getPixel(v.getRoundX(), v.getRoundY(), v.getRoundZ()) >=thld) newVox.add(v);
            else maskImage.setPixel(v.getRoundX(),v.getRoundY(), v.getRoundZ(), 0);
        }
        ((Object3DVoxels)o.getObject3D()).setVoxels(newVox);
        maskImage.getImagePlus().updateAndDraw();
        maskChange=true;
    }
    
    private void hysteresisThreshold(Object3DGui o) {
        ImageHandler rawImage = getInput();
        ImageInt maskImage = getMask();
        double thldH = this.thresholdHigh.getFloatValue(0);
        double thldL = this.thresholdLow.getFloatValue(0);
        ArrayList<Voxel3D> vox = o.getObject3D().getVoxels();
        int l = o.getLabel();
        //IJ.log("thld current: l:"+l+" thld:"+thldH+" thld Low:"+thldL);
        if (vox==null) return;
        ArrayList<Voxel3D> newVox = new ArrayList<Voxel3D>(vox.size());
        int zMax=0;
        int zMin=maskImage.sizeZ;
        int xMax=0;
        int xMin=maskImage.sizeX;
        int yMax=0;
        int yMin=maskImage.sizeY;
        //get bounding box
        for (Voxel3D  v : vox) {
            int c = v.getRoundX();
            if (c>xMax) xMax=c;
            if (c<xMin) xMin=c;
            c=v.getRoundY();
            if (c>yMax) yMax=c;
            if (c<yMin) yMin=c;
            c=v.getRoundZ();
            if (c>zMax) zMax=c;
            if (c<zMin) zMin=c;
        }
        //get Cropped Image:
        ImageHandler crop = rawImage.crop3DMask("crop", maskImage, l, xMin, xMax, yMin, yMax, zMin, zMax);
        //crop.showDuplicate("postProcessAndCrop");
        //hysteresis thld
        crop.hysteresis(thldL, thldH, true);
        //crop.showDuplicate("postProcessAndCrop thld");
        //repaint global mask image
        for (int z = zMin; z<=zMax && z<rawImage.sizeZ; z++) {
            int zz=z-zMin;
            for (int y = yMin; y<=yMax && y<rawImage.sizeY; y++) {
                int yy=y-yMin;
                for (int x = xMin; x<=xMax && x<rawImage.sizeX; x++) {
                    int xx=x-xMin;
                    if (maskImage.getPixelInt(x, y, z) ==l) {
                        if (crop.getPixel(xx, yy, zz)!=0) newVox.add(new Voxel3D(x, y, z, l)); //rawImage.getPixel(x, y, z)
                        else maskImage.setPixel(x, y, z, 0);
                    } 
                }
            }
        }
        crop.closeImagePlus();
        ((Object3DVoxels)o.getObject3D()).setVoxels(newVox);
        maskImage.getImagePlus().updateAndDraw();
        maskChange=true;
    }
    * 
    */
    /*
    private void postProcess(Object3DGui o) {
        ImageInt maskImage = getMask();
        ArrayList<Voxel3D> vox = o.getObject3D().getVoxels();
        int l = o.getLabel();
        if (vox==null) return;
        //IJ.log("nb of vox:"+vox.size());
        ArrayList<Voxel3D> newVox = new ArrayList<Voxel3D>(vox.size());
        int zMax=0;
        int zMin=maskImage.sizeZ;
        int xMax=0;
        int xMin=maskImage.sizeX;
        int yMax=0;
        int yMin=maskImage.sizeY;
        //get bounding box
        for (Voxel3D  v : vox) {
            int c = v.getRoundX();
            if (c>xMax) xMax=c;
            if (c<xMin) xMin=c;
            c=v.getRoundY();
            if (c>yMax) yMax=c;
            if (c<yMin) yMin=c;
            c=v.getRoundZ();
            if (c>zMax) zMax=c;
            if (c<zMin) zMin=c;
        }
        //get Cropped Image:
        ImageInt crop = maskImage.crop3DBinary("crop", l, xMin, xMax, yMin, yMax, zMin, zMax);
        PostFilterSequence pfs = Core.getExperiment().getPostFilterSequence(0, Core.getMaxCPUs(), false);
        crop = pfs.run(0, crop, ((Field)this.currentChannels[0]).getInputImages());
        //repaint global mask image
        for (int z = zMin; z<=zMax && z<maskImage.sizeZ; z++) {
            int zz=z-zMin;
            for (int y = yMin; y<=yMax && y<maskImage.sizeY; y++) {
                int yy=y-yMin;
                for (int x = xMin; x<=xMax && x<maskImage.sizeX; x++) {
                    int xx=x-xMin;
                    int oldValue = maskImage.getPixelInt(x, y, z);
                    if (crop.getPixel(xx, yy, zz)!=0) {
                        if (oldValue==0 ) {
                            newVox.add(new Voxel3D(x, y, z, l));
                            maskImage.setPixel(x, y, z, l);
                        } else if (oldValue==l) newVox.add(new Voxel3D(x, y, z, l));
                    } else if (oldValue==l) maskImage.setPixel(x, y, z, 0); 
                }
            }
        }
        crop.closeImagePlus();
        ((Object3DVoxels)o.getObject3D()).setVoxels(newVox);
        maskImage.getImagePlus().updateAndDraw();
        maskChange=true;
    }
    * 
    */
    
    protected void process(Object3DGui o, boolean process, boolean intersectWithMask, boolean postProcess, int border, boolean test) {
        ImageInt maskImage = getMask();
        int[] bounds = o.getObject3D().getBoundingBox();
        //get Cropped Image:
        if (test) ((Field)this.currentChannels[0]).setVerbose(true);
        InputCroppedImages ici=new InputCroppedImages(((Field)this.currentChannels[0]).getInputImages(), maskImage, o.getLabel(), bounds, border, false, true);
        ici.setFilterCroppedImage(true);
        ImageInt mask = ici.getMaskNoBackground();
        ImageInt seg=mask;
        if (test) seg.showDuplicate("cropped mask");
        if (process) {
            ImageHandler input = ici.getFilteredImage(0);
            if (test) input.showDuplicate("pre-filtered image");
            NucleusSegmenterRunner nsr = Core.getExperiment().getNucleusSegmenterRunner(Core.getMaxCPUs(), test);
            seg = nsr.run(0, input, ici);
            if (intersectWithMask) seg.intersectMask(mask);
            if (test) seg.showDuplicate("segmented image");
        }
        if (postProcess) {
            PostFilterSequence pfs = Core.getExperiment().getPostFilterSequence(0, Core.getMaxCPUs(), test);
            seg = pfs.run(0, seg, ici);
            seg.intersectMask(mask);
            if (test) seg.showDuplicate("post-filtered image");
        }
        if (test) ((Field)this.currentChannels[0]).setVerbose(false);
        //repaint global mask image
        
        if (!test) {
            if (seg!=ici.getMask()) seg.setOffset(ici.getMask());
            repaintObject(seg, o);
        }
        seg.closeImagePlus();
    }
    /*
    protected void repaintObject(ImageInt image, Object3DGui o) {
        ImageInt maskImage = getMask();
        ArrayList<Voxel3D> newVox = new ArrayList<Voxel3D>(o.getObject3D().getVolumePixels());
        int label = o.getLabel();
        Object3DVoxels[] newObjects = image.getObjects3D();
        
        for (int z =image.offsetZ; z<(image.offsetZ+image.sizeZ) && z<maskImage.sizeZ; z++) {
            int zz=z-image.offsetZ;
            for (int y = image.offsetY; y<(image.offsetY+image.sizeY) && y<maskImage.sizeY; y++) {
                int yy=y-image.offsetY;
                for (int x = image.offsetX; x<(image.offsetX+image.sizeX) && x<maskImage.sizeX; x++) {
                    int xx=x-image.offsetX;
                    int oldValue = maskImage.getPixelInt(x, y, z);
                    if (image.getPixel(xx, yy, zz)!=0) {
                        if (oldValue==0 ) {
                            newVox.add(new Voxel3D(x, y, z, label));
                            maskImage.setPixel(x, y, z, label);
                        } else if (oldValue==label) newVox.add(new Voxel3D(x, y, z, label));
                    } else if (oldValue==label) maskImage.setPixel(x, y, z, 0); 
                }
            }
        }
        ((Object3DVoxels)o.getObject3D()).setVoxels(newVox);
        maskChange=true;
    }
    * 
    */
    
    protected void repaintObject(ImageInt image, Object3DGui o) {
        Object3DVoxels[] newObjects = image.getObjects3D();
        for (int i = 0; i<newObjects.length; i++) {
            newObjects[i].translate(image.offsetX, image.offsetY, image.offsetZ);
            Object3DGui oGui = i==0?o:newObject();
            oGui.setObject3D(newObjects[i]);
        }
        if (newObjects.length>0) maskChange=true;
    }
    
}
