package tango.gui;

import tango.gui.util.LCRenderer;
import mcib3d.utils.exceptionPrinter;
import tango.dataStructure.Structure;
import tango.dataStructure.Nucleus;
import tango.dataStructure.Cell;
import com.mongodb.BasicDBList;
import java.awt.event.ActionEvent;
import java.awt.event.AdjustmentEvent;
import java.awt.event.MouseWheelEvent;
import javax.swing.event.ListSelectionEvent;
import tango.mongo.MongoConnector;
import mcib3d.image3d.ImageHandler;
import java.util.*;
import java.io.*;
import java.awt.Dimension;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import tango.dataStructure.AbstractStructure;
import tango.dataStructure.Experiment;
import tango.dataStructure.Field;
import tango.dataStructure.Object3DGui;
import mcib3d.utils.ThreadRunner;
import ij.*;
import ij.gui.NewImage;
import ij.gui.PointRoi;
import ij.gui.Roi;
import ij.plugin.filter.ThresholdToSelection;
import ij.process.ByteProcessor;
import ij.process.ImageProcessor;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.AdjustmentListener;
import java.awt.event.MouseWheelListener;
import javax.swing.*;
import javax.swing.event.ListSelectionListener;
import mcib3d.geom.Object3D;
import mcib3d.geom.Voxel3D;
import mcib3d.image3d.*;
import mcib3d.image3d.processing.ManualSpot;
import org.bson.types.ObjectId;
import tango.dataStructure.*;
import tango.gui.util.CellManagerLayout;
import tango.gui.util.ObjectManagerLayout;
import tango.helper.HelpManager;
import tango.parameter.DoubleParameter;
import tango.parameter.Parameter;
import tango.parameter.StructureParameter;
import tango.plugin.measurement.MeasurementKey;
import tango.plugin.measurement.MeasurementObject;
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
public class ObjectManager implements ListSelectionListener, AdjustmentListener, MouseWheelListener {

    protected javax.swing.JList list;
    protected DefaultListModel listModel;
    protected ListSelectionModel listSelectionModel;
    protected JPanel controlPanel, container;
    protected Core core;
    protected boolean populatingObjects, selectingObject;
    protected ObjectStructure[] currentChannels;
    protected int[] currentStructureIdx;
    protected ImagePlus currentImage;
    protected ImageByte roiMask;
    protected HashMap<Integer, Roi> currentROIs;
    protected JToggleButton showObjects, showMeasurements;
    protected Structure msChannel;
    protected JPanel layout;
    protected boolean autoSave;
    protected MeasurementDisplayer measurements;
    protected ObjectId currentNucId;
    protected ManualSpot manualSpot;
    protected DoubleParameter splitDist;
    protected DoubleParameter splitRad;
    protected JToggleButton showSelection;
   
    // ImagePlus roiMask;

    public ObjectManager(Core core, JPanel container) {
        try {
            this.container=container;
            
            this.core = core;
            this.autoSave=true;
            
            initPanels();
        } catch (Exception e) {
            exceptionPrinter.print(e, "", Core.GUIMode);
        }
    }
    
    public void setShowSelection(JToggleButton showSelection) {
        this.showSelection=showSelection;
    }
    
    public void show(boolean refresh) {
        container.add(layout);
        if (refresh) core.refreshDisplay();
    }
    
    public void hide(boolean refresh) {
        container.remove(layout);
        if (showMeasurements.isSelected()) {
            container.remove(measurements);
            showMeasurements.setSelected(false);
        }
        if (refresh) core.refreshDisplay();
    }
    
    protected void initPanels() {
        BasicDBObject usr = Core.mongoConnector.getUser();
        splitDist = new DoubleParameter("Dist", "splitMinDistObj", 5d, DoubleParameter.nfDEC1);
        splitDist.dbGet(usr);
        splitRad = new DoubleParameter("Rad", "splitRadObj", 2d, DoubleParameter.nfDEC1);
        splitRad.dbGet(usr);
        ObjectManagerLayout lay =new ObjectManagerLayout(this);
        showObjects = lay.showROIs;
        showObjects.setSelected(true);
        splitDist.addToContainer(lay.splitDistPanel);
        showMeasurements = lay.viewMeasurements;
        measurements=new MeasurementDisplayer();
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
    
    public void toggleIsRunning(boolean isRunning) {
        ((ObjectManagerLayout)this.layout).toggleIsRunning(isRunning);
        setSortKeys();
    }
    
    public void saveOptions() {
        BasicDBObject user = Core.mongoConnector.getUser();
        splitDist.dbPut(user);
        splitRad.dbPut(user);
        Core.mongoConnector.saveUser(user);
    }
    
    public void registerComponents(HelpManager hm) {
        if (this instanceof ObjectManager) ((ObjectManagerLayout)layout).registerComponents(hm);
    }

    public void setStructures(ObjectId id, Object[] selectedChannels) {
        //System.out.println("Set Structures: cell"+id+ " sel channels length"+selectedChannels.length);
        this.currentNucId=id;
        this.currentChannels = new ObjectStructure[selectedChannels.length];
        currentStructureIdx=new int[selectedChannels.length];
        for (int i = 0; i < selectedChannels.length; i++) {
            currentChannels[i] = (ObjectStructure) selectedChannels[i];
            currentStructureIdx[i]=currentChannels[i].getIdx();
        }
        setSortKeys();
        populateObjects();
        if (showMeasurements.isSelected()) {
            measurements.setStructures(id, currentStructureIdx);
            measurements.setObjects(list.getSelectedValues());
        }
    }
    
    private void setSortKeys() {
        if (currentChannels!=null && currentChannels.length==1) {
            MeasurementKey mkey = new MeasurementKey(new int[]{currentChannels[0].getIdx()}, MeasurementObject.Number);
            System.out.println("Query:"+mkey.toString());
            ((ObjectManagerLayout)layout).setKeys(Core.getExperiment().getKeys().get(mkey));
        } else {
            ((ObjectManagerLayout)layout).unableSortKeys();
        }
    }

    public void populateObjects() {
        try {
            this.listModel.removeAllElements();
            if (currentChannels==null) {
                return;
            }
            this.populatingObjects = true;
            ArrayList<Integer> selection = null;
            if (showSelection!=null && showSelection.isSelected()) selection = new ArrayList<Integer>();
            int currentIdx = 0;
            for (ObjectStructure ass : currentChannels) {
                Object3D[] os = ass.getObjects(); 
                if (os!=null) {
                    Object3DGui[] osg = new Object3DGui[os.length];
                    for (int i = 0;i<os.length;i++) osg[i]= new Object3DGui(os[i], ass);
                    if (layout instanceof ObjectManagerLayout && currentChannels.length==1 && !((ObjectManagerLayout)layout).getSortKey().equals("idx")) this.sort(((ObjectManagerLayout)layout).getSortKey(), osg, ass.getIdx());
                    //System.out.println("populating objects.. nb objects:"+os.length);
                    for (Object3DGui o3D : osg) {
                        this.listModel.addElement(o3D);
                        if (selection!=null && o3D.isInSelection()) selection.add(currentIdx);
                        currentIdx++;
                    }
                    //if (selection!=null) System.out.println("populating objects.. selection size:"+selection.size());
                } //else System.out.println("no objects int channel:"+ass.getChannelName());
            }
            if (selection!=null && !selection.isEmpty()) {
                int[] sel = new int[selection.size()];
                int i = 0;
                for (int idx : selection) sel[i++]=idx;
                list.setSelectedIndices(sel);
            }
        } catch (Exception e) {
            exceptionPrinter.print(e, "", Core.GUIMode);
        }
        this.populatingObjects = false;
    }
    
    public void invertSelection() {
        int[] sel = this.list.getSelectedIndices();
        int[] newSel = new int[listModel.getSize()-sel.length];
        int idx = 0;
        int lastIdx = -1;
        for (int i : sel) {
            for (int i2 = lastIdx+1; i2<i; i2++) newSel[idx++]=i2;
            lastIdx=i;
        }
        for (int i2 = lastIdx+1; i2<listModel.getSize(); i2++) newSel[idx++]=i2;
        list.setSelectedIndices(newSel);
    }

    protected int getChannelRank(ObjectStructure as) {
        int i = 0;
        while (currentChannels[i] != as) {
            i++;
        }
        return i;
    }

    public void manualSegmentation() {
        msChannel = null;
        if (currentChannels != null) {
            if (!(currentChannels[0] instanceof Nucleus)) {
                msChannel = (Structure) currentChannels[0];
            } else if (currentChannels.length > 1) {
                msChannel = (Structure) currentChannels[1];
            }
        }
        if (msChannel instanceof VirtualStructure) {
            return;
        }
        if (msChannel != null) {
            ImageInt seg = msChannel.getSegmented();
            ImageHandler raw = msChannel.getFiltered();
            if (raw==null) {
                if (Core.GUIMode) ij.IJ.log("ERROR: no raw images!");
                return;
            }
            if (seg==null) {
                seg=new ImageShort(msChannel.getChannelName()+"::Segmented", raw.sizeX, raw.sizeY, raw.sizeZ);
                msChannel.setSegmented(seg);
            }
            seg.show();
            raw.show();
            manualSpot = new ManualSpot(raw.getImagePlus(), seg.getImagePlus(), (int) seg.getMax(null) + 1);
            manualSpot.setVisible(true);

            JButton msClose = manualSpot.jButtonClose;
            msClose.addActionListener(new java.awt.event.ActionListener() {
                @Override
                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    if (msChannel != null) {
                        msChannel.saveOutput();
                        msChannel.createObjects();
                        populateObjects();
                    }
                }
            });
        }
    }
    
    
    private void sort(String key, Object3DGui[] objectsGui, int structureIdx) {
        Object3DGui.setAscendingOrger(((ObjectManagerLayout)layout).getAscendingOrder());
        HashMap<Integer, BasicDBObject> objects = Core.getExperiment().getConnector().getObjects(currentNucId, structureIdx);
        boolean notFound=false;
        for (Object3DGui o : objectsGui) {
            BasicDBObject dbo = objects.get(o.getLabel());
            if (dbo!=null) {
                if (dbo.containsField(key)) o.setValue(dbo.getDouble(key));
                else {
                    o.setValue(-1);
                    notFound=true;
                }
            }
        }
        if (notFound) ij.IJ.log("Warning measurement: "+key+ " not found for one or several objects");
        Arrays.sort(objectsGui);
    }

    public void deleteSelectedObjects() {
        populatingObjects = true;
        try {
            boolean[] modif = new boolean[currentChannels.length];
            for (Object o : this.list.getSelectedValues()) {
                listModel.removeElement(o);
                modif[getChannelRank(((Object3DGui) o).getChannel())] = true;
                ((Object3DGui) o).delete(true);
            }
            for (int i = 0; i < currentChannels.length; i++) {
                if (modif[i]) {
                    ImagePlus img = currentChannels[i].getSegmented().getImagePlus();
                    if (img.isVisible()) {
                        img.updateAndDraw();
                    }
                    currentChannels[i].createObjects();
                    if (autoSave) currentChannels[i].saveOutput();
                    
                }
            }
        } catch (Exception e) {
            exceptionPrinter.print(e, "", Core.GUIMode);
        }
        populatingObjects = false;
    }
    
    public void setSelectedObjectsFromDB() {
        this.selectingObject=true;
        this.list.clearSelection();
        int offsetIdx=0;
        ArrayList<Integer> selectedIndices = new ArrayList<Integer>();
        for (ObjectStructure s : this.currentChannels) {
            BasicDBList selectedObjects = Core.mongoConnector.getSelectedObjects(currentNucId, s.getIdx());
            if (selectedObjects!=null && !selectedObjects.isEmpty()) {
                for (Object o : selectedObjects) selectedIndices.add((Integer)o+offsetIdx);
            }
            offsetIdx+=s.getObjects().length;
        }
        if (!selectedIndices.isEmpty()) {
            int[] selectedIdx = new int[selectedIndices.size()];
            for (int i = 0; i<selectedIdx.length; i++) selectedIdx[i]=selectedIndices.get(i);
            this.list.setSelectedIndices(selectedIdx);
        }
        this.selectingObject=false;
    }

    
    protected void registerActiveImage() {
        ImagePlus activeImage = WindowManager.getCurrentImage();
        if (activeImage != null && activeImage.getProcessor() != null) { // && activeImage.getImageStackSize() > 1
            if (currentImage != null && currentImage.getWindow() != null && currentImage != activeImage) {
                //System.out.println("remove listener:"+currentImage.getTitle());
                ImageUtils.removeScrollListener(currentImage, this, this);
                currentImage.killRoi();
                currentImage.updateAndDraw();
                currentImage = null;
            }
            if (currentImage != activeImage) {
                //System.out.println("add listener:"+activeImage.getTitle());
                ImageUtils.addScrollListener(activeImage, this, this);
                this.currentImage = activeImage;
            }
        }
    }

    public void showRois3D() {
        registerActiveImage();
        if (currentImage==null) return;
        //verifier que l'image active a les memes dimentions
        Object[] os = this.list.getSelectedValues();

        if (os.length == 1) {
            mcib3d.geom.Object3D o = ((Object3DGui) os[0]).getObject3D();
            currentImage.setSlice((o.getZmax() + o.getZmin()) / 2 + 1);
        }
        int nSlices = currentImage.getNSlices();
        
        currentROIs = new HashMap<Integer, Roi>(nSlices);
        //stores the roi mask to save memory..
        if (roiMask == null || !roiMask.sameDimentions(currentImage)) {
            roiMask = new ImageByte("mask", currentImage.getWidth(), currentImage.getHeight(), nSlices);
        } else {
            roiMask.erase();
        }
        ImageStack maskStack = roiMask.getImageStack();
        Object3DGui obj;
        for (Object o : os) {
            obj = (Object3DGui) o;
            obj.getObject3D().draw(maskStack, 255);
        }
        //roiMask.show();

        for (int i = 1; i <= nSlices; i++) {
            ImagePlus im = new ImagePlus("mask", maskStack.getProcessor(i));
            im.getProcessor().setThreshold(1, 255, ImageProcessor.NO_LUT_UPDATE);
            ThresholdToSelection tts = new ThresholdToSelection();
            tts.setup("", im);
            tts.run(im.getProcessor());
            Roi r = im.getRoi();
            if (r!=null) currentROIs.put(i,r);
        }
        updateRoi();
    }
    
    protected void hideRois() {
        if (currentImage==null) return;
        currentImage.killRoi();
        if (currentImage.isVisible()) {
            currentImage.updateAndDraw();
            ImageUtils.removeScrollListener(currentImage, this, this);
        }
        currentImage = null;
    }

    protected HashMap<Integer, ArrayList<Object3DGui>> getSplitSelection() {
        System.out.println("get split selection: currenChannels==null?"+(this.currentChannels==null));
        if (this.currentChannels==null) return new HashMap<Integer, ArrayList<Object3DGui>>(0);
        HashMap<Integer, ArrayList<Object3DGui>> res = new HashMap<Integer, ArrayList<Object3DGui>>(this.currentChannels.length);
        for (ObjectStructure ass : currentChannels) res.put(ass.getIdx(), new ArrayList<Object3DGui>());
        for (Object o : list.getSelectedValues()) {
            Object3DGui o3D = (Object3DGui) (o);
            int idx = o3D.getChannel().getIdx();
            res.get(idx).add(o3D);
        }
        return res;
    }
    
    protected HashMap<Integer, ArrayList<Integer> > getSplitSelectionIndexes() {
        HashMap<Integer, ArrayList<Object3DGui> > splitSelection = getSplitSelection();
        HashMap<Integer, ArrayList<Integer> > res = new HashMap<Integer, ArrayList<Integer>>(splitSelection.size());
        for (Map.Entry<Integer, ArrayList<Object3DGui>> e : splitSelection.entrySet()) {
            ArrayList<Integer> idxs=new ArrayList<Integer> (e.getValue().size());
            for (Object3DGui o : e.getValue()) idxs.add(o.getLabel());
            res.put(e.getKey(), idxs);
        }
        return res;
    }

    public void mergeSelectedObjects() {
        this.populatingObjects = true;
        HashMap<Integer, ArrayList<Object3DGui>> allObjects = getSplitSelection();
        for (int channelIdx : allObjects.keySet()) {
            ArrayList<Object3DGui> objects = allObjects.get(channelIdx);
            if (objects != null && objects.size() >= 2) {
                Collections.sort(objects);
                Object3DGui o1 = objects.get(0);
                for (int i = objects.size() - 1; i > 0; i--) {
                    Object3DGui o2 = objects.get(i);
                    o1.merge(o2);
                    listModel.removeElement(o2);
                    //IJ.log("merge:"+o1.getName()+ "::"+objects.get(i).getName()+ " channel:"+channelIdx);
                }
                o1.getChannel().createObjects();
                if (autoSave) o1.getChannel().saveOutput();
                ImagePlus img = o1.getChannel().getSegmented().getImagePlus();
                if (img.isVisible()) {
                    img.updateAndDraw();
                }
            }
        }
        this.populatingObjects = false;
    }

    public void toggleShowROIs(boolean show) {
        if (show) {
            this.core.getCellManager().toggleShowROIs(false);
            this.showRois3D();
        }
        else {
            hideRois();
            if (this.showObjects.isSelected()) this.showObjects.setSelected(false);
        }
    }
    
    public void selectAll() {
        list.setSelectionInterval(0, list.getModel().getSize() - 1);
    }
    
    public void selectNone() {
        this.list.clearSelection();
    }
    
    public void shift() {
        boolean change = false;
        for (ObjectStructure as : currentChannels) {
            if (as instanceof Structure) {
                boolean c = ((Structure) as).shiftObjectIndexes(true);
                if (c) {
                    change = true;
                    Core.mongoConnector.removeStructureMeasurements(as.getId(), as.getIdx());
                }
            } else if (as instanceof Field) {
                if (((Field)as).shiftObjectIndexes()) {
                    if (autoSave) as.saveOutput();
                    change=true;
                }
            }
        }
        if (change) {
            this.populateObjects();
        }
    }
    
    
    public void toggleShowMeasurements() {
        if (showMeasurements.isSelected()) {
            measurements.setStructures(currentNucId, currentStructureIdx);
            measurements.setObjects(list.getSelectedValues());
            this.container.add(measurements);
        }
        else this.container.remove(measurements);
        core.refreshDisplay();
    }

    @Override
    public void valueChanged(ListSelectionEvent lse) {
        if (lse.getValueIsAdjusting()) {
            return;
        }
        if (populatingObjects || selectingObject || !showObjects.isSelected()) {
            return;
        }
        selectingObject = true;
        if (measurements!=null && showMeasurements.isSelected()) measurements.setObjects(list.getSelectedValues());
        try {
            showRois3D();
        } catch (Exception e) {
            exceptionPrinter.print(e, "", Core.GUIMode);
        }
        selectingObject = false;
    }
    

    @Override
    public void adjustmentValueChanged(AdjustmentEvent ae) {
        updateRoi();
    }

    @Override
    public void mouseWheelMoved(MouseWheelEvent mwe) {
        updateRoi();
    }

    protected void updateRoi() {
        //System.out.println("image:"+currentImage.getTitle()+ " slice:"+currentImage.getSlice());
        Roi r = currentROIs.get(currentImage.getSlice());
        if (r!=null) {
            currentImage.setRoi(r);
        } else {
            currentImage.killRoi();
        }
        currentImage.updateAndDraw();
    }

    public void splitObjects() {
        Object[] os = this.list.getSelectedValues();
        if (os.length==0) return;
        Set<ObjectStructure> channels=new HashSet<ObjectStructure>();
        for (Object o : os) {
            if (split((Object3DGui)o)) channels.add(((Object3DGui)o).getChannel());
        }
        for (ObjectStructure o : channels) o.saveOutput();
        saveOptions();
    }
    
    protected boolean split(Object3DGui og) {
        if (!(this instanceof NucleusManager) && og.getChannel() instanceof Nucleus) {
            if (Core.GUIMode) ij.IJ.log("Cannont split nucleus!");
            return false;
        }
        Object3DGui[] newObjects = og.split(splitRad.getFloatValue(2), splitDist.getFloatValue(5));
        if (newObjects.length==0) {
            if (Core.GUIMode) ij.IJ.log("Object couldn't be split");
            return false;
        }
        Object3D[] objs = og.getChannel().getObjects();
        int nextLabel = objs[objs.length-1].getValue()+1;
        for (Object3DGui o : newObjects) {
            o.changeLabel(nextLabel);
            this.listModel.addElement(o); //TODO le mettre a la fin des objets du channel.. 
            nextLabel++;
        }
        og.getChannel().getSegmented().updateDisplay();
        return true;
    }
}
