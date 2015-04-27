package tango.gui;

import tango.gui.parameterPanel.MultiParameterPanel;
import tango.gui.parameterPanel.PreFilterPanel;
import tango.gui.parameterPanel.ParameterPanelPlugin;
import tango.gui.parameterPanel.PostFilterPanel;
import mcib3d.utils.exceptionPrinter;
import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import ij.IJ;
import ij.ImagePlus;
import ij.gui.GenericDialog;
import mcib3d.image3d.ImageHandler;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import tango.gui.util.*;
import tango.helper.HelpManager;
import tango.helper.Helper;
import tango.helper.ID;
import tango.helper.RetrieveHelp;
import tango.mongo.MongoConnector;
import tango.parameter.SettingsParameter;
import tango.plugin.PluginFactory;
import tango.plugin.filter.PreFilterSequence;
import tango.util.utils;
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
public abstract class ProcessingSequenceTemplateEditor  implements ActionListener {
    protected JPanel controlPanel;
    protected JComboBox processingSequences;
    protected MultiParameterPanel<PreFilterPanel> preFilterPanel;
    protected MultiParameterPanel<? extends ParameterPanelPlugin> segmenterPanel;
    protected MultiParameterPanel<PostFilterPanel> postFilterPanel;
    protected Core core;
    protected BasicDBObject data;
    protected String currentProcessingSequence;
    protected boolean populatingProcessingSequences;
    protected ProcessingSequenceManagerLayout layout;
    protected JButton newPS, rename ,duplicate, remove, save;
    protected Helper ml;
    public ProcessingSequenceTemplateEditor(Core main) {
        this.core=main;
        this.layout = new ProcessingSequenceManagerLayout(main, getTitle());
        try {
            init();
        } catch (Exception e) {
            exceptionPrinter.print(e, "", Core.GUIMode);
        }
    }
    
    public void registerComponents(HelpManager hm) {
        hm.objectIDs.put(newPS, new ID(RetrieveHelp.editPSPage, "New"));
        hm.objectIDs.put(rename, new ID(RetrieveHelp.editPSPage, "Rename"));
        hm.objectIDs.put(duplicate, new ID(RetrieveHelp.editPSPage, "Duplicate"));
        hm.objectIDs.put(remove, new ID(RetrieveHelp.editPSPage, "Remove"));
        hm.objectIDs.put(save, new ID(RetrieveHelp.editPSPage, "Save"));
        hm.objectIDs.put(layout.preFilterPanel, new ID(RetrieveHelp.editPSPage, "Pre-filtering"));
        hm.objectIDs.put(layout.segmentationPanel, new ID(RetrieveHelp.editPSPage, "Segmentation"));
        hm.objectIDs.put(layout.postFilterPanel, new ID(RetrieveHelp.editPSPage, "Post-filtering"));
    }
    
    public void register(Helper ml) {
        if (this.ml!=null && this.ml!=ml) unRegister(this.ml);
        this.ml=ml;
        register();
    }
    
    protected abstract String getTitle();
    
    public void register() {
        if (preFilterPanel!=null) preFilterPanel.register(ml);
        if (segmenterPanel!=null) segmenterPanel.register(ml);
        if (postFilterPanel!=null) postFilterPanel.register(ml);
    }
    
    public void unRegister(Helper ml) {
        if (this.ml==ml) this.ml=null;
        if (preFilterPanel!=null) preFilterPanel.unRegister(ml);
        if (segmenterPanel!=null) segmenterPanel.unRegister(ml);
        if (postFilterPanel!=null) postFilterPanel.unRegister(ml);
    }
    
    private void init() throws Exception {
        controlPanel = new JPanel();
        controlPanel.setLayout(new BoxLayout(controlPanel, BoxLayout.Y_AXIS));
        controlPanel.setPreferredSize(layout.controlDim);
        processingSequences = new JComboBox();
        this.get();
        utils.addHorizontalScrollBar(processingSequences);
        processingSequences.addActionListener(this);
        processingSequences.setAlignmentX(0);
        processingSequences.setMaximumSize(new Dimension(layout.controlDim.width, processingSequences.getPreferredSize().height));
        controlPanel.add(processingSequences);
        newPS=new JButton("New");
        newPS.addActionListener(this);
        controlPanel.add(newPS);
        newPS.setAlignmentX(0);
        Dimension buttonDim = new Dimension(layout.controlDim.width, newPS.getPreferredSize().height);
        newPS.setMinimumSize(buttonDim);
        newPS.setMaximumSize(buttonDim);
        newPS.setPreferredSize(buttonDim);
        rename=new JButton("Rename");
        rename.addActionListener(this);
        rename.setAlignmentX(0);
        controlPanel.add(rename);
        rename.setMinimumSize(buttonDim);
        rename.setMaximumSize(buttonDim);
        rename.setPreferredSize(buttonDim);
        duplicate=new JButton("Duplicate");
        duplicate.addActionListener(this);
        duplicate.setAlignmentX(0);
        duplicate.setMinimumSize(buttonDim);
        duplicate.setMaximumSize(buttonDim);
        duplicate.setPreferredSize(buttonDim);
        controlPanel.add(duplicate);
        remove=new JButton("Remove");
        remove.addActionListener(this);
        remove.setAlignmentX(0);
        controlPanel.add(remove);
        remove.setMinimumSize(buttonDim);
        remove.setMaximumSize(buttonDim);
        remove.setPreferredSize(buttonDim);
        save=new JButton("Save");
        save.addActionListener(this);
        save.setAlignmentX(0);
        controlPanel.add(save);
        save.setMinimumSize(buttonDim);
        save.setMaximumSize(buttonDim);
        save.setPreferredSize(buttonDim);
        /*test=new JButton("Test");
        test.addActionListener(this);
        test.setAlignmentX(0);
        controlPanel.add(test);
        test.setMinimumSize(buttonDim);
        test.setMaximumSize(buttonDim);
        test.setPreferredSize(buttonDim);
        * 
        */
        layout.controlPanel.add(controlPanel);
    }
    
    public JPanel getPanel() {
        return layout;
    }
    
    public void refreshParameters() {
        if (preFilterPanel!=null) this.preFilterPanel.refreshParameters();
        if (postFilterPanel!=null) this.postFilterPanel.refreshParameters();
        if (segmenterPanel!=null) this.segmenterPanel.refreshParameters();
        //refresh processing sequences:
        get();
        this.set(currentProcessingSequence);
    }
    
    protected abstract void get();
    protected abstract void create(String name);
    protected abstract void duplicate(String name, String newName);
    protected abstract void rename(String oldName, String newName);
    protected abstract BasicDBObject get(String name);
    protected abstract void remove(String name);
    protected abstract void createMultiPanels();
    protected abstract void record();
    
    private void set(String name){
        try {
            populatingProcessingSequences=true;
            this.processingSequences.setSelectedItem(name);
            currentProcessingSequence=name;
            data = get(name);
            if (this.ml!=null) unRegister(ml);
            createMultiPanels();
            register(Core.helper);
            layout.hidePanel();
            layout.showListPanels(this.preFilterPanel.getPanel(), this.segmenterPanel.getPanel(), this.postFilterPanel.getPanel());
            populatingProcessingSequences=false;
        } catch (Exception e) {
            exceptionPrinter.print(e, "", Core.GUIMode);
        }
    }
    
    protected void save(boolean record) {
        try {
            if (data==null) data=new BasicDBObject("name", (String)processingSequences.getSelectedItem());
            data.append("preFilters", preFilterPanel.save());
            data.append("segmentation", segmenterPanel.save());
            data.append("postFilters", postFilterPanel.save());
            data.append("tango_version", Core.VERSION);
            if (record) record();
        } catch (Exception e) {
            exceptionPrinter.print(e, null, Core.GUIMode);
        }
    }
    
    protected DBObject getPreFilters() {
        if (data==null) return null;
        Object data = this.data.get("preFilters");
        if (data!=null) return (DBObject) data;
        else return null;
    }
    
    protected DBObject getPostFilters() {
        if (data==null) return null;
        Object data = this.data.get("postFilters");
        if (data!=null) return (DBObject) data;
        else return null;
    }
    
    protected DBObject getSegmentation() {
        if (data==null) return null;
        Object data = this.data.get("segmentation");
        if (data!=null) return (DBObject) data;
        else return null;
    }
    
    protected abstract void test();
    
    @Override
    public void actionPerformed(ActionEvent e) {
        Object source = e.getSource();
        if (e.getSource() == processingSequences && !populatingProcessingSequences) {
            String name= (String)processingSequences.getSelectedItem();
            set(name);
        } else if (source==newPS) {
            String name = JOptionPane.showInputDialog("Processing Sequence Name");
            if (name==null) return;
            if (utils.isValid(name, false) && !utils.contains(processingSequences, name, false)) {
                create(name);
                get();
                set(name);
                core.updateSettings();
            } else {
                IJ.error("Invalid Command/Processing Chain already exists");
            }
        } else if (source==rename) {
            String oldName = utils.getSelectedString(processingSequences);
            if (oldName!=null && oldName.length()>0) {
                String name = JOptionPane.showInputDialog("New Name", oldName);
                if (name==null) return;
                if (utils.isValid(name, false) && !utils.contains(processingSequences, name, false)) {
                    rename(oldName, name);
                    get();
                    set(name);
                    core.updateSettings();
                } else {
                    IJ.error("Invalid Command/Processing Chain already exists");
                }
            } else IJ.error("Select Processing Chain First");
        } else if (source==duplicate) {
            String oldName = utils.getSelectedString(processingSequences);
            if (oldName!=null && oldName.length()>0) {
                String name = JOptionPane.showInputDialog("New Name", oldName);
                if (name==null) return;
                if (!name.equals("") && !utils.contains(processingSequences, name, false)) {
                    duplicate(oldName, name);
                    get();
                    set(name);
                    core.updateSettings();
                } else {
                    IJ.error("Invalid Command/Processing Chain already exists");
                }
            } else IJ.error("Select Processing Sequence First");
        } else if (source==remove){
            String name = utils.getSelectedString(processingSequences);
            if (name!=null && name.length()>0) {
                if (JOptionPane.showConfirmDialog(controlPanel, "Remove selected Processing Sequence?", "tango", JOptionPane.OK_CANCEL_OPTION)==0) {
                    remove(name);
                    layout.hidePanel();
                    get();
                    core.updateSettings();
                }
            } else IJ.error("Select Processing Chain First");
        } else if (source==save) {
            save(true);
        } /*else if (source==test) {
            test();
        }
        * 
        */
    }
}
