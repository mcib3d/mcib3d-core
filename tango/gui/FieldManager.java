package tango.gui;

import tango.gui.util.FileFilterFolder;
import tango.gui.util.LCRenderer;
import mcib3d.utils.exceptionPrinter;
import tango.dataStructure.Experiment;
import tango.dataStructure.Field;
import tango.dataStructure.Cell;
import java.awt.event.ActionEvent;
import javax.swing.event.ListSelectionEvent;
import tango.mongo.MongoConnector;
import mcib3d.image3d.ImageHandler;
import java.util.*;
import java.io.*;
import java.awt.Dimension;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import tango.gui.util.FieldFactory;
import ij.*;
import ij.gui.GenericDialog;
import java.awt.Choice;
import java.awt.Component;
import java.awt.Container;
import java.awt.GridLayout;
import java.awt.event.ActionListener;
import javax.swing.*;
import javax.swing.event.ListSelectionListener;
import mcib3d.image3d.*;
import org.bson.types.ObjectId;
import tango.dataStructure.*;
import tango.gui.util.*;
import tango.helper.HelpManager;
import tango.helper.ID;
import tango.helper.RetrieveHelp;
import tango.parameter.PreFilterSequenceParameter;
import tango.plugin.filter.PostFilterSequence;
import tango.plugin.filter.PreFilterSequence;
import tango.plugin.sampler.SampleRunner;
import tango.plugin.sampler.Sampler;
import tango.plugin.segmenter.NucleusSegmenterRunner;
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
public class FieldManager implements ListSelectionListener {
    
    private javax.swing.JList list;
    private DefaultListModel listModel;
    private ListSelectionModel listSelectionModel;
    private JPanel mainPanel;
    Experiment xp;
    int[] fileRank;
    private JToggleButton showCells, showSelections, manualSegmentation;
    Core core;
    private CellManager cellManager;
    private NucleusManager nucleusManager;
    private File curDir;
    boolean populatingFields, populatingCells, displaying;
    int[] selectedFields;
    private FieldManagerLayout layout;
    protected Field currentField;
    public FieldManager(Core core)  {
        try {
            this.core= core;
            //Create Cell Manager and Particle Manager
            layout=new FieldManagerLayout(this);
            showCells=layout.viewCells;
            showSelections=layout.viewSelections;
            manualSegmentation=layout.manualSeg;
            mainPanel =new JPanel();
            mainPanel.setMinimumSize(Core.minSize);
            cellManager = new CellManager(core, mainPanel);
            nucleusManager = new NucleusManager(core, mainPanel);
            
            this.listModel = new DefaultListModel();
            this.list=layout.list;
            list.setModel(listModel);
            this.list.setCellRenderer(new LCRenderer());
            this.list.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
            this.list.setLayoutOrientation(JList.VERTICAL);
            listSelectionModel = list.getSelectionModel();
            listSelectionModel.addListSelectionListener(this);
            mainPanel.add(layout);
            
        } catch(Exception e) {
            exceptionPrinter.print(e, "", Core.GUIMode);
        }
    }
    
    public CellManager getCellManager() {
        return cellManager;
    }
    
    public void registerComponents(HelpManager hm) {
        layout.registerComponents(hm);
        nucleusManager.registerComponents(hm);
    }
    
    public void setXP(Experiment xp) {
        if (xp==null) return;
        try {
            this.xp=xp;
            curDir = xp.getDirectory();
            if (curDir==null) curDir=getUsrDir();
            if (this.showCells.isSelected() && list.getSelectedValue()!=null) {
                this.hideCells();
                core.refreshDisplay();
            } else if (this.manualSegmentation.isSelected()) {
                this.hideNuclei();
                core.refreshDisplay();
            }
            populateFields();
            cellManager.setXP(xp);
        } catch (Exception e) {
            exceptionPrinter.print(e, "", Core.GUIMode);
        }
    }
    
    public Experiment getExperiment () {
        return xp;
    }

    public Core getCore() {
        return core;
    }
    
    private File getUsrDir() {
        BasicDBObject usr = Core.mongoConnector.getUser();
        if (usr.containsField("importDir")) {
            File f = new File(usr.getString("importDir"));
            if (f.exists()) return f;
        }
        return null;
    }
    
    public void importImages() {
        final JFileChooser fc = new JFileChooser("Select Fields (folders or .zvi files)");
        fc.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
        fc.setMultiSelectionEnabled(true);
        if (curDir!=null) fc.setCurrentDirectory(curDir);
        else fc.setCurrentDirectory(getUsrDir());
        int returnval = fc.showOpenDialog(layout);
        if (returnval == JFileChooser.APPROVE_OPTION) {
            final File[] files = fc.getSelectedFiles();
            curDir=files[0].getParentFile();
            xp.setDirectory(curDir);
            Core.mongoConnector.saveImportDir(curDir.getAbsolutePath());
            core.toggleIsRunning(true);
            Thread t = new Thread(new Runnable() {
                @Override
                public void run() {
                    FieldFactory.createFields(xp, files);
                    Thread t2 = new Thread(new Runnable() {
                    @Override
                        public void run() {
                            core.toggleIsRunning(false);
                            populateFields();
                        }
                    }
                    );
                    SwingUtilities.invokeLater(t2);
                }
            }
            );
            t.start();
            
        }
    }
    
    private void createVirtualField(String name) {
        Field f = FieldFactory.createVirtualField(xp, name);
        this.listModel.addElement(f);
    }
    
    private void createCell(Field f) {
        try{
            int[] ids = ij.WindowManager.getIDList();
            String[] names = new String[ids.length+1];
            names[0]="*NONE*";
            for (int i = 0; i<ids.length; i++) {
                names[i+1]=ij.WindowManager.getImage(ids[i]).getTitle();
            }
            String [] channels = xp.getStructureNames(false);
            GenericDialog gd = new GenericDialog("Create Cell:", core);
            gd.addStringField("Cell name:", null);
            for (int i = 0; i<this.xp.getNBStructures(false);i++) {
                gd.addChoice(channels[i]+" Raw Image: ", names, names[0]);
                gd.addChoice(channels[i]+" Segmented Image: ", names, names[0]);
                gd.addChoice(channels[i]+" ProbaMap Image: ", names, names[0]);
            }
            gd.showDialog();
            Vector choices = gd.getChoices();
            ImageHandler[] raw=new ImageHandler[xp.getNBStructures(false)];
            ImageHandler[] seg=new ImageHandler[xp.getNBStructures(false)];
            ImageHandler[] proba=new ImageHandler[xp.getNBStructures(false)];
            if (gd.wasOKed()) {
                for (int i = 0; i<this.xp.getNBStructures(false);i++) {
                    int rawIdx = ((Choice)choices.get(i*3)).getSelectedIndex()-1;
                    if (rawIdx>=0) raw[i]=ImageHandler.wrap(ij.WindowManager.getImage(ids[rawIdx]));
                    int segIdx = ((Choice)choices.get(i*3+1)).getSelectedIndex()-1;
                    if (segIdx>=0) seg[i]=ImageHandler.wrap(ij.WindowManager.getImage(ids[segIdx]));
                    int probaIdx = ((Choice)choices.get(i*3+2)).getSelectedIndex()-1;
                    if (probaIdx>=0) proba[i]=ImageHandler.wrap(ij.WindowManager.getImage(ids[probaIdx]));
                }
                Cell c = f.createCellFromFiles(gd.getNextString(), raw, seg, proba);
                this.cellManager.addCell(c);
            }
        } catch (Exception e) {
            exceptionPrinter.print(e, "", Core.GUIMode);
        }
    }
    
    public void populateFields() {
        try {
            populatingFields=true;
            this.listModel.removeAllElements();
            Field[] fields = FieldFactory.getFields(xp);
            for (Field f : fields) listModel.addElement(f);
            populatingFields=false;
        } catch (Exception e) {exceptionPrinter.print(e, "", Core.GUIMode);}
    }
    
    private void populateCells() {
        try {
            populatingCells=true;
            cellManager.populateCells();
            populatingCells=false;
        } catch (Exception e) {exceptionPrinter.print(e, "", Core.GUIMode);}
    }
    
    
    
    private void populateNuclei() {
        try {
            populatingCells=true;
            if (list.getSelectedIndex()==-1) nucleusManager.setStructures(null, new ObjectStructure[]{});
            else nucleusManager.setStructures(null, new ObjectStructure[]{(Field)list.getSelectedValue()});
        } catch (Exception e) {exceptionPrinter.print(e, "", Core.GUIMode);}
        populatingCells=false;
    }
    
    
    private void deleteSelectedFields() {
        populatingFields=true;
        try {
            for (Object o : this.list.getSelectedValues()) {
                listModel.removeElement(o);
                ((Field)o).delete();
            }
        } catch (Exception e){
            exceptionPrinter.print(e, "", Core.GUIMode);
        }
        populatingFields=false;
    }
    
    private void deleteSelectedFieldsFiles() {
        try {
            for (Object o : this.list.getSelectedValues()) {
                ((Field)o).deleteFiles();
            }
        } catch (Exception e){
            exceptionPrinter.print(e, "", Core.GUIMode);
        }
    }

    public Component getPanel() {
        return mainPanel;
    }


    public void showField(int idx, int chanidx) {
        try {
            if (chanidx>=0 && idx>=0) {
                Field f = (Field)listModel.get(idx);
                f.getStructureInputImage(chanidx).show();
            }
        } catch (Exception e) {
            exceptionPrinter.print(e, "show field", Core.GUIMode);
        }
    }

    private void mesureSelectedFields(final boolean override) {
        final Cell[] cellList = getSelectedCellsArray();
        CellManager.measureCells(cellList, override);
    }
    
    public ArrayList<Cell> getSelectedCells() {
        Object[] fields = list.getSelectedValues();
        ArrayList<Cell> cells = new ArrayList<Cell>();
        if (fields==null) return cells;
        for (Object o : fields) {
            Field f = (Field) o;
            if (f.getCells() == null) f.createCells();
            for (Cell c : f.getCells()) {
                if (c!=null && (c.getTag().getTag()>=0)) {
                    c.createChannels();
                }
                cells.add(c);
            }
        }
        return cells;
    }
    
    private Cell[] getSelectedCellsArray() {
        ArrayList<Cell> cellList = getSelectedCells();
        for (int i = 0; i<cellList.size(); i++) {
            if (cellList.get(i).getTag().getTag()<0) {
                cellList.remove(i);
                i--;
            }
        }
        Cell[] cells = new Cell[cellList.size()];
        cells = cellList.toArray(cells);
        return cells;
    }
    
    private void processSelectedFields() {
        final Cell[] cellList = getSelectedCellsArray();
        CellManager.processCells(cellList, null);
    }
    
    

    
    public void test(){
        /*if (list.getSelectedIndex()==1) {
            IJ.log("gc");
            System.gc();
        } else if (list.getSelectedIndex()==2) {
            IJ.log("fin");
            System.runFinalization();
        }
        
        * 
        */
        if (this.list.getSelectedIndex()<0) {
            ij.IJ.error("Select a Field First!");
            return;
        }
        Field field = (Field)this.list.getSelectedValue();
        field.setVerbose(true);
        Core.debug=true;
        try {
            IJ.log("Test segment field:"+field.getName());
            System.out.println("Nuclei segmentation test: "+field.getName());
            field.processNucleus();
        }
        catch (Exception e) {
            exceptionPrinter.print(e, "run error :: ", Core.GUIMode);
        }
        Core.debug=false;
        field.setVerbose(false);
    }
    
    public void testProcess(int step, int subStep){
        InputFieldImages in;
        ImageHandler input;
        ImagePlus ip = ij.IJ.getImage();
        if (ip!=null) {
            input = ImageHandler.wrap(ip);
            in = new InputFieldImages(input);
            if (step==2) {
                IJ.log("Caution, running post-process that requiers access to raw images migth lead to an error. To avoid the error, run the whole process at a time on a selected Field in the Field list");
            }
            testProcess(step, subStep, input, in);
        } else {//if (this.list.getSelectedIndex()<0) {
                ij.IJ.error("Open an image first");
                return;
             /*else {
                Field field = (Field)this.list.getSelectedValue();
                PreFilterSequenceParameter sequence = xp.getChannelFilePreFilterSequence(xp.getChannelFileIndex(0));
                if (sequence!=null) {
                    input = sequence.runPreFilterSequence(0, input, inputImages, Core.getMaxCPUs(), this.verbose);
                }
                in = preFilterChannel(in, );
                input=field.getInputImages().getImage(0).duplicate();
                input.show();
            }
            * 
            */
        }
        
        
        /*try {
            IJ.log("Test segment field:"+field.getName() + " step:"+step+ " subStep"+subStep);
            System.out.println("Nuclei segmentation test: "+field.getName());
            field.testProcess(step, subStep);
        }
        catch (Exception e) {
            exceptionPrinter.print(e, "run error :: ", Core.GUIMode);
        }
        field.setVerbose(false);
        Core.debug=false;
        * 
        */
    }
    
    private void testProcess(int step, int subStep, ImageHandler image, InputFieldImages in) {
        try {
            if (step==0) {
                PreFilterSequence pofs= xp.getPreFilterSequence(0, Core.getMaxCPUs(), true);
                pofs.test(0, image, in, subStep, true);
            } else if (step==1) {
                NucleusSegmenterRunner nsr = xp.getNucleusSegmenterRunner(Core.getMaxCPUs(), true);
                ImageHandler segmented = nsr.run(0, image, in);
                segmented.set332RGBLut();
                segmented.showDuplicate("After Segmentation");
            } else if (step==2) {
                if (!(image instanceof ImageInt)) {
                    IJ.error("8-bit or 16-bit image requiered for post-processing");
                    return;
                }
                ImageInt segmented = (ImageInt)image;
                PostFilterSequence pfs = xp.getPostFilterSequence(0, Core.getMaxCPUs(), true);
                pfs.test(0, segmented, in, subStep, true);
            }
        } catch (Exception e) {
            exceptionPrinter.print(e, "", Core.GUIMode);
        }
    }

    
    private void hideCells() {
        System.out.println("hiding cells ..");
        displaying=true;
        cellManager.hide();
        this.showCells.setSelected(false);
        displaying=false;
    }
    
    private void hideNuclei() {
        System.out.println("hiding nucs..");
        if (this.manualSegmentation.isSelected() && this.nucleusManager.maskChange) {
            if (JOptionPane.showConfirmDialog(this.layout, "Save changes on Nucleus Mask?", "TANGO", JOptionPane.OK_CANCEL_OPTION) == 0) nucleusManager.saveMask();
        }
        displaying=true;
        this.manualSegmentation.setSelected(false);
        this.nucleusManager.hide(true);
        displaying=false;
    }
    
    private void refreshDisplay() {
        this.mainPanel.repaint();
        this.mainPanel.revalidate();
        core.refreshDisplay();
    }
    
    public void selectAll() {
        list.setSelectionInterval(0, list.getModel().getSize() -1);
    }
    
    public void selectNone() {
        this.list.clearSelection();
    }
    
    public void run(final boolean processNuclei, final boolean crop, final boolean process, final boolean measure, final boolean override) {
        if (list.getSelectedIndex()<0) return;
        String s = "";
        if (processNuclei) s+=" segmentation masks";
        if (crop || process) s+=" segmented images and measurements";
        else if (measure && override) s+=" measurements?";
        if (s.length()>0) s="Run will override:"+s;
        else s="Run?";
        if (JOptionPane.showConfirmDialog(layout, s, "tango", JOptionPane.OK_CANCEL_OPTION)==0) {    
            
            Thread t = new Thread(new Runnable() {
                @Override
                public void run() {
                    processAndCropFields(processNuclei, crop);
                    if (process) processSelectedFields();
                    if (measure) mesureSelectedFields(override);
                    Thread t2 = new Thread(new Runnable() {
                    @Override
                        public void run() {
                            core.toggleIsRunning(false); 
                            populateCells();
                            refreshDisplay();
                        }
                    }
                    );
                    SwingUtilities.invokeLater(t2);
                }
            }
            );
            core.toggleIsRunning(true);
            t.start();
        }
    }
    
    protected Field[] getSelectedFields() {
        Object[] fields = this.list.getSelectedValues();
        Field[] f = new Field[fields.length];
        for (int i = 0; i<f.length; i++) f[i]=(Field)fields[i];
        return f;
    }
    
    protected void processAndCropFields(boolean processNuclei, boolean crop){
        Field[] f = getSelectedFields();
        processAndCropFields(f, processNuclei, crop);
    }
    
    
    public static void processAndCropFields(Field[] fields, boolean processNuclei, boolean crop) {
        try {
            System.out.println("Nb of fields: "+fields.length);
            int[][] tags = new int[fields.length][];
            if (processNuclei) {
                if (Core.GUIMode) Core.getProgressor().resetProgress(fields.length);
                for (int i  = 0; i<fields.length; i++) {
                    if (Core.GUIMode) Core.getProgressor().setAction("Processing field");
                    if (Core.GUIMode) IJ.log("segment field:"+fields[i]);
                    IJ.showStatus("Nuclei segmentation: "+(i+1)+"/"+fields.length);
                    System.out.println("Nuclei segmentation: "+(i+1)+"/"+fields.length);
                    Field field = (Field)fields[i];
                    field.setVerbose(false);
                    field.hide();
                    tags[i]=field.processNucleus();
                    field.saveOutput();
                    if (crop) {
                        if (Core.GUIMode) Core.getProgressor().setAction("Cropping field");
                        if (Core.GUIMode) IJ.log ("crop field:"+fields[i]);
                        if (Core.GUIMode) IJ.showStatus("Nuclei cropping: "+(i+1)+"/"+fields.length);
                        System.out.println("Nuclei cropping: "+(i+1)+"/"+fields.length);
                        field.cropCells(tags[i]);
                    }
                    field.closeInputImages();
                    field.closeOutputImages();
                    if (Core.GUIMode) Core.getProgressor().incrementStep();
                }
            } else if (crop) {
                if (Core.GUIMode) Core.getProgressor().setAction("Cropping field");
                if (Core.GUIMode) Core.getProgressor().resetProgress(fields.length);
                for (int i  = 0; i<fields.length; i++) {
                    Field field = (Field)fields[i];
                    field.setVerbose(false);
                    field.hide();
                    if (Core.GUIMode) IJ.log ("crop field:"+fields[i]);
                    System.out.println("Nuclei cropping: "+(i+1)+"/"+fields.length);
                    field.cropCells(tags[i]);
                    field.closeInputImages();
                    field.closeOutputImages();
                    if (Core.GUIMode) Core.getProgressor().incrementStep();
                }
            }
        }
        catch (Exception e) {
            exceptionPrinter.print(e, "run error :: ", Core.GUIMode);
        }
    }
    
    public void viewOverlay() {
        try {
                ((Field)listModel.get(list.getSelectedIndex())).getImage5D().show();
            } catch(Exception e) {
                exceptionPrinter.print(e, "", Core.GUIMode);
            }
    }
    
    public void viewInputImages() {
        try {
                for (int i = 0; i<xp.getNBStructures(false); i++) {
                    Field f = (Field)list.getSelectedValue();
                    f.getStructureInputImage(i).show(f.getName()+"_"+xp.getChannelSettings(i).getString("name"));
                }
            } catch(Exception e) {
                exceptionPrinter.print(e, "", Core.GUIMode);
            }
    }
    
    public void deleteFields() {
        if (JOptionPane.showConfirmDialog(layout, "Remove selected Fields From DB and Disk?", "ij3DM", JOptionPane.OK_CANCEL_OPTION)==0) {
            this.deleteSelectedFields();
        }
    }
    
    public void deleteInputImages() {
        if (JOptionPane.showConfirmDialog(layout, "Remove selected Fields' input files From DB?", "ij3DM", JOptionPane.OK_CANCEL_OPTION)==0) {
                this.deleteSelectedFieldsFiles();
            }
    }
    
    public void deleteSlices() {
        Object o = this.list.getSelectedValue();
        if (o==null) {
            IJ.error("Select a Field First");
            return;
        }
        Field f = (Field)o;
        ImageHandler ih = f.getInputImages().getImage(0);
        if (ih == null) {
            IJ.error("No input image found");
            return;
        }
        int[] slices = DeleteSlicesOptionPane.showInputDialog(ih.sizeZ);
        if (slices!=null) {
            IJ.log("start:"+slices[0]+ " stop:"+slices[1]);
            f.deleteSlices(slices[0], slices[1]);
        } else IJ.log("cancel");
    }
    
    public void extractData() {
        final JFileChooser fc = new JFileChooser("Select Folder for Output Files");
        fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        if (curDir!=null) fc.setCurrentDirectory(curDir);
        int returnval = fc.showOpenDialog(layout);
        if (returnval == JFileChooser.APPROVE_OPTION) {
            DataManager dm = new DataManager(this.core, this.getExperiment());
            dm.extractData(fc.getSelectedFile());
        }
    }
    
    public void toggleShowCells() {
        if (showCells.isSelected()) {
            if (manualSegmentation.isSelected()) hideNuclei();
            System.out.println("showCells");
            mainPanel.add(cellManager.getPanel());
            this.populateCells();
            refreshDisplay();
        } else {
            hideCells();
            refreshDisplay();
        }
    }
    
    public void toggleShowSelections() {
        if (showSelections.isSelected()) {
            if (manualSegmentation.isSelected()) hideNuclei();
            System.out.println("showSelections");
            mainPanel.add(cellManager.selectionManager);
            cellManager.selectionManager.update();
            refreshDisplay();
        } else {
            cellManager.selectionManager.clearSelection();
            mainPanel.remove(cellManager.selectionManager);
            refreshDisplay();
        }
    }
    
    public void toggleShowManualSeg() {
        if (manualSegmentation.isSelected()) {
            if (showCells.isSelected()) hideCells();
            if (showSelections.isSelected()) {
                showSelections.setSelected(false);
                mainPanel.remove(cellManager.selectionManager);
            }
            System.out.println("showing nucs..");
            nucleusManager.show(false);
            this.populateNuclei();
            refreshDisplay();
        } else {
            hideNuclei();
            refreshDisplay();
        }
    }


    @Override
    public void valueChanged(ListSelectionEvent lse) {
        if (lse.getValueIsAdjusting()) return;
        if (populatingFields) return;
        if (lse.getSource().equals(this.listSelectionModel) && !populatingCells) {
            if (this.manualSegmentation.isSelected() && this.nucleusManager.maskChange) {
                if (JOptionPane.showConfirmDialog(this.layout, "Save changes on Nucleus Mask?", "TANGO", JOptionPane.OK_CANCEL_OPTION) == 0) nucleusManager.saveMask();
            } else this.nucleusManager.maskChange=false;
            //free memory..
            if (currentField!=null) {
                currentField.closeInputImages();
                currentField.closeOutputImages();
            } 
            Object o = list.getSelectedValue();
            if (o!=null) currentField = (Field)o;
            if (this.showCells.isSelected()) this.populateCells();
            else if (this.manualSegmentation.isSelected()) this.populateNuclei();
        }
    }
    public void toggleIsRunning(boolean isRunning) {
        layout.toggleIsRunning(isRunning);
        if (this.nucleusManager!=null) nucleusManager.toggleIsRunning(isRunning);
    }
}
 