package tango.gui;

import tango.gui.parameterPanel.MultiParameterPanel;
import tango.gui.parameterPanel.VirtualStructurePanel;
import tango.gui.parameterPanel.StructurePanel;
import tango.gui.parameterPanel.SamplerPanel;
import tango.gui.parameterPanel.ParameterPanelAbstract;
import tango.gui.parameterPanel.MeasurementPanel;
import tango.gui.parameterPanel.ChannelImagePanel;
import com.mongodb.BasicDBObject;
import ij.IJ;
import ij.Prefs;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.util.HashMap;
import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import mcib3d.utils.exceptionPrinter;
import tango.dataStructure.Experiment;
import tango.gui.util.*;
import tango.gui.util.DuplicateXPOptionPane.DuplicateXP;
import tango.helper.HelpManager;
import tango.helper.Helper;
import tango.helper.ID;
import tango.helper.RetrieveHelp;
import tango.mongo.MongoConnector;
import tango.parameter.*;
import tango.plugin.sampler.SampleRunner;
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
public class XPEditor extends javax.swing.JPanel implements PanelDisplayer {

    public final static DoubleParameter scalexy = new DoubleParameter("Scale XY:", "scaleXY", null, Parameter.nfDEC5);
    public final static DoubleParameter scalez = new DoubleParameter("Scale Z:", "scaleZ", null, Parameter.nfDEC5);
    public final static TextParameter unit = new TextParameter("Unit:", "unit", "µm");
    public final static BooleanParameter useScale=new BooleanParameter("Use Global Scale:", "globalScale", false);
    private final static HashMap<Object, Parameter[]> map = new HashMap<Object, Parameter[]>(){{
        put(true, new Parameter[]{scalexy, scalez, unit}); 
        put(false, new Parameter[0]);
    }};
    private final static ConditionalParameter globalScale = new ConditionalParameter(useScale, map);
    //private final static FileParameter inputFile = new FileParameter("Input Folder", "inputFolder", null);
    private final static ChoiceParameter importFileMethod = new ChoiceParameter("Import File Method:", "importFileMethod", FieldFactory.importMethod, FieldFactory.importMethod[0]);
    public static Parameter[] xpParams = new Parameter[]{globalScale, importFileMethod};
    Core core;
    JPanel currentEditPanel;
    boolean init;
    MultiParameterPanel<StructurePanel> structures;
    MultiParameterPanel<VirtualStructurePanel> virtualStructures;
    MultiParameterPanel<MeasurementPanel> measurements;
    MultiParameterPanel<ChannelImagePanel> channelImages;
    MultiParameterPanel<SamplerPanel> samples;
    JPanel mainSettingsPanel;
    JScrollPane listScroll;
    Dimension listDim;
    Helper ml;
    int selectedTab;
    JButton importImages;
    
    public XPEditor(Core core) {
        this.core = core;
        init = true;
        initComponents();
        editTab.addTab("Channel Images", new JPanel());
        editTab.addTab("Structures", new JPanel());
        editTab.addTab("Virtual Structures", new JPanel());
        editTab.addTab("Measurements", new JPanel());
        editTab.addTab("Samplers", new JPanel());
        editTab.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                selectTab(editTab.getSelectedIndex());
            }
        });
        listScroll = new JScrollPane();
        toggleEnableTabs(false);
        listDim = new Dimension(this.editTab.getPreferredSize().width - 10, this.editTab.getPreferredSize().height - 10);
        mainSettingsPanel = new JPanel(new FlowLayout()); //new GridLayout(0, 1, 0, 0)
        globalScale.addToContainer(mainSettingsPanel);
        unit.allowSpecialCharacter(true);
        
        //scalexy.addToContainer(mainSettingsPanel);
        //scalez.addToContainer(mainSettingsPanel);
        //unit.addToContainer(mainSettingsPanel);
        //inputFile.addToContainer(mainSettingsPanel);
        this.settingsPanel.add(mainSettingsPanel);
        importFileMethod.addToContainer(mainSettingsPanel);
        importImages=new JButton("Import Images");
        mainSettingsPanel.add(importImages);
        importImages.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                importImages();
            }
        });
        
        getFolders();
        String folder = Prefs.get(MongoConnector.getPrefix() + "_" + Core.mongoConnector.getUserName() + "_folder.String", "");
        if (folders.getItemCount() > 0 && utils.contains(folders, folder, true)) {
            folders.setSelectedItem(folder);
            setProject(folder);
        }
        init = false;
    }

    public void registerComponents(HelpManager hm) {
        // projects
        hm.objectIDs.put(this.sfLabel, new ID(RetrieveHelp.editXPPage, "Project"));
        hm.objectIDs.put(this.folders, new ID(RetrieveHelp.editXPPage, "Project"));
        hm.objectIDs.put(this.newFolder, new ID(RetrieveHelp.editXPPage, "New_Project"));
        hm.objectIDs.put(this.removeFolder, new ID(RetrieveHelp.editXPPage, "Delete_Project"));
        
        // experiments
        hm.objectIDs.put(this.eLabel, new ID(RetrieveHelp.editXPPage, "Experiment_2"));
        hm.objectIDs.put(this.experiments, new ID(RetrieveHelp.editXPPage, "Experiment_2"));
        hm.objectIDs.put(this.newExperiment, new ID(RetrieveHelp.editXPPage, "New_Experiment"));
        hm.objectIDs.put(this.deleteExperiment, new ID(RetrieveHelp.editXPPage, "Delete_Experiment"));
        hm.objectIDs.put(this.duplicateExperiment, new ID(RetrieveHelp.editXPPage, "Duplicate"));
        hm.objectIDs.put(this.override, new ID(RetrieveHelp.editXPPage, "Override"));
        hm.objectIDs.put(this.renameExperiment, new ID(RetrieveHelp.editXPPage, "Rename_Experiment"));
        hm.objectIDs.put(scalexy.label(), new ID(RetrieveHelp.editXPPage, "Calibration"));
        hm.objectIDs.put(scalez.label(), new ID(RetrieveHelp.editXPPage, "Calibration"));
        hm.objectIDs.put(unit.label(), new ID(RetrieveHelp.editXPPage, "Calibration"));
        hm.objectIDs.put(save, new ID(RetrieveHelp.editXPPage, "Save"));
        hm.objectIDs.put(importFileMethod.getChoice(), new ID(RetrieveHelp.editXPPage, "Import_File_Method"));
        hm.objectIDs.put(importFileMethod.label(), new ID(RetrieveHelp.editXPPage, "Import_File_Method"));
        hm.objectIDs.put(importImages, new ID(RetrieveHelp.editXPPage, "Import_Images"));
        
        
        registerXPComponents(hm);
        
    }
    
    public void registerXPComponents(HelpManager hm) {
        if (channelImages!=null) hm.objectIDs.put(channelImages.getPanel(), new ID(RetrieveHelp.editXPPage, "Channel_Images"));
        if (structures!=null) hm.objectIDs.put(structures.getPanel(), new ID(RetrieveHelp.editXPPage, "Structures"));
        if (virtualStructures!=null) hm.objectIDs.put(virtualStructures.getPanel(), new ID(RetrieveHelp.editXPPage, "Virtual_Structures"));
        if (measurements!=null) hm.objectIDs.put(measurements.getPanel(), new ID(RetrieveHelp.editXPPage, "Quantitative_Image_Analysis"));
        if (samples!=null) hm.objectIDs.put(samples.getPanel(), new ID(RetrieveHelp.editXPPage, "Samples"));
    }

    public void register(Helper ml) {
        if (this.ml != null && ml != this.ml && ml != null) {
            unRegister(ml);
        }
        this.ml = ml;
        
        register();
    }

    private void register() {
        if (ml == null) {
            return;
        }
        if (structures != null) {
            structures.register(ml);
        }
        if (virtualStructures != null) {
            virtualStructures.register(ml);
        }
        if (measurements != null) {
            measurements.register(ml);
        }
        if (channelImages != null) {
            channelImages.register(ml);
        }
        if (samples != null) {
            samples.register(ml);
        }
    }

    public void unRegister(Helper ml) {
        if (this.ml == ml) {
            this.ml = null;
        }
        if (structures != null) {
            structures.unRegister(ml);
        }
        if (virtualStructures != null) {
            virtualStructures.unRegister(ml);
        }
        if (measurements != null) {
            measurements.unRegister(ml);
        }
        if (channelImages != null) {
            channelImages.unRegister(ml);
        }
        if (samples != null) {
            samples.unRegister(ml);
        }
    }
    
    protected void toggleIsRunning(boolean isRunning) {
        newFolder.setEnabled(!isRunning);
        folders.setEnabled(!isRunning);
        this.experiments.setEnabled(!isRunning);
        toggleEnableButtons(!isRunning, !isRunning);
        
    }
    
    protected void toggleEnableButtons(boolean projectSet, boolean xpSet) {
        if (!projectSet) {
            removeFolder.setEnabled(false);
            newExperiment.setEnabled(false);
            
        }
        if (projectSet) {
            removeFolder.setEnabled(true);
            newExperiment.setEnabled(true);
        }
        if (!xpSet) {
            renameExperiment.setEnabled(false);
            deleteExperiment.setEnabled(false);
            duplicateExperiment.setEnabled(false);
            save.setEnabled(false);
            importImages.setEnabled(false);
            toggleEnableTabs(false);
            this.override.setEnabled(false);
        }
        if (xpSet) {
            deleteExperiment.setEnabled(true);
            renameExperiment.setEnabled(true);
            duplicateExperiment.setEnabled(true);
            this.override.setEnabled(true);
            save.setEnabled(true);
            importImages.setEnabled(true);
            toggleEnableTabs(true);
        }
    }
    
    private void importImages() {
        this.save(true);
        this.core.getFieldManager().importImages();
        
    }

    private void getFolders() {
        folders.removeAllItems();
        folders.addItem("");
        for (String key : Core.mongoConnector.getProjects()) {
            folders.addItem(key);
        }
    }

    private void getXPs() {
        experiments.removeAllItems();
        experiments.addItem("");
        for (String key : Core.mongoConnector.getExperiments()) {
            experiments.addItem(key);
        }
    }

    private void setProject(String name) {
        if (name == null || name.length() == 0) {
            core.toggleEnableTabs(false);
            toggleEnableButtons(false, false);
            return;
        }
        Core.mongoConnector.setProject(name);
        
        getXPs();
        if (experiments.getItemCount() > 0 && init) {
            String defaultXP = (String) Prefs.get(MongoConnector.getPrefix() + "_" + Core.mongoConnector.getUserName() + "_xp.String", "");
            if (defaultXP.length() > 0 && utils.contains(experiments, defaultXP, true)) {
                experiments.setSelectedItem(defaultXP);
                setXP(defaultXP);
            } else toggleEnableButtons(true, false);
        }
        Prefs.set(MongoConnector.getPrefix() + "_" + Core.mongoConnector.getUserName() + "_folder.String", name);
    }

    private void setXP(String name) {
        if (name == null || name.length() == 0) {
            core.toggleEnableTabs(false);
            toggleEnableTabs(false);
            toggleEnableButtons(this.folders.getSelectedIndex()>=0, false);
            return;
        }
        try {
            core.setExperiment(new Experiment(name, Core.mongoConnector));
            for (Parameter p : xpParams) {
                p.dbGet(Core.getExperiment().getData());
            }
            /*measurements = new MultiParameterPanel<MeasurementPanel>(Core.getExperiment().getMeasurementSettings(), 0, 200, listDim, this, MeasurementPanel.class);
            channelImages = new MultiParameterPanel<ChannelImagePanel>(Core.getExperiment().getChannelImages(), 1, 20, listDim, this, ChannelImagePanel.class);
            structures = new MultiParameterPanel<StructurePanel>(Core.getExperiment().getStructures(), 1, 20, listDim, this, StructurePanel.class);
            virtualStructures = new MultiParameterPanel<VirtualStructurePanel>(Core.getExperiment().getVirtualStructures(), 0, 20, listDim, this, VirtualStructurePanel.class);
            samples = new MultiParameterPanel<SamplerPanel>(Core.getExperiment().getSampleChannels(), 0, 100, listDim, this, SamplerPanel.class);
            */
            measurements = new MultiParameterPanel<MeasurementPanel>(core, Core.getExperiment().getMeasurementSettings(), 0, 200, listDim, this, true, MeasurementPanel.class); 
            channelImages = new MultiParameterPanel<ChannelImagePanel>(core, Core.getExperiment().getChannelImages(), 1, 20, listDim, this, false, ChannelImagePanel.class);
            structures = new MultiParameterPanel<StructurePanel>(core, Core.getExperiment().getStructures(), 1, 20, listDim, this, false, StructurePanel.class);
            virtualStructures = new MultiParameterPanel<VirtualStructurePanel>(core, Core.getExperiment().getVirtualStructures(), 0, 20, listDim, this, false, VirtualStructurePanel.class);
            samples = new MultiParameterPanel<SamplerPanel>(core, Core.getExperiment().getSampleChannels(), 0, 100, listDim, this, true, SamplerPanel.class);
            
            register();
            if (ml!=null && ml instanceof Helper) registerXPComponents(((Helper)ml).getHelpManager());
            core.toggleEnableTabs(true);
            toggleEnableTabs(true);
            editTab.setSelectedIndex(0);
            selectTab(0);
            Prefs.set(MongoConnector.getPrefix() + "_" + Core.mongoConnector.getUserName() + "_xp.String", name);
            toggleEnableButtons(true, true);
            IJ.log("xp:" + name + " set");
        } catch (Exception e) {
            toggleEnableButtons(this.folders.getSelectedIndex()>=0, false);
            exceptionPrinter.print(e, "", Core.GUIMode);
        }

    }

    public void toggleEnableTabs(boolean enable) {
        for (int i = 0; i<editTab.getTabCount(); i++) {
            editTab.setEnabledAt(i, enable);
            if (enable && editTab.getSelectedIndex()==i) selectTab(i);
        }
        if (!enable) editTab.setComponentAt(editTab.getSelectedIndex(), new JPanel());
        if (!Core.SPATIALSTATS) editTab.setEnabledAt(4, false);
        if (!Core.TESTING) editTab.setEnabledAt(2, false); //virtual structures
    }
    

    @Override
    public void showPanel(JPanel panel) {
        hidePanel();
        currentEditPanel = panel;
        panel.setMinimumSize(editPanel.getMinimumSize());
        this.editScroll.setViewportView(panel);
        //this.editPanel.add(panel);
        //this.editScrollPane.setViewportView(panel);
        refreshDisplay();
    }

    @Override
    public void hidePanel() {
        this.editScroll.setViewportView(editPanel);
        //if (currentEditPanel!=null) this.editPanel.remove(currentEditPanel);
        currentEditPanel = null;
        refreshDisplay();
    }

    @Override
    public void refreshDisplay() {
        this.editScroll.repaint();
        this.editScroll.revalidate();
        this.editPanel.repaint();
        this.editPanel.revalidate();
        this.settingsPanel.repaint();
        this.settingsPanel.revalidate();
        core.refreshDisplay();
    }

    public void refreshParameters() {
        if (Core.getExperiment() == null) {
            return;
        }
        if (selectedTab==4) {
            ParameterPanelAbstract[] panels = samples.getParameterPanels();
            String[] names = new String[panels.length];
            for (int i = 0; i<names.length; i++) names[i]=((SamplerPanel)panels[i]).getName();
            SamplerParameter.setChannels(names);
        }
        else if (selectedTab==0)  {
            ParameterPanelAbstract[] panels = channelImages.getParameterPanels();
            String[] names = new String[panels.length];
            for (int i = 0; i<names.length; i++) names[i]=((ChannelImagePanel)panels[i]).getName();
            ChannelFileParameter.setChannels(names);
        }
        else if (selectedTab==1 || selectedTab==2) {
            ParameterPanelAbstract[] panels = structures.getParameterPanels();
            String[] names = new String[panels.length];
            for (int i = 0; i<names.length; i++) names[i]=((StructurePanel)panels[i]).getName();
            panels = virtualStructures.getParameterPanels();
            String[] namesV = new String[panels.length];
            for (int i = 0; i<namesV.length; i++) namesV[i]=((VirtualStructurePanel)panels[i]).getName();
            StructureParameter.setStructures(names, namesV);
        }
        
        measurements.refreshParameters();
        channelImages.refreshParameters();
        structures.refreshParameters();
        virtualStructures.refreshParameters();
        samples.refreshParameters();
        register();
    }
    
    public void refreshDispayMPP() {
        if (Core.getExperiment() == null) {
            return;
        }
        measurements.refreshDisplay();
        channelImages.refreshDisplay();
        structures.refreshDisplay();
        virtualStructures.refreshDisplay();
        samples.refreshDisplay();
        //register();
    }
    
    

    private void allOff() {
        if (Core.getExperiment() == null) {
            return;
        }
        measurements.allOff();
        channelImages.allOff();
        structures.allOff();
        virtualStructures.allOff();
        samples.allOff();
    }
    
    public void selectTab(int newTab) {
        editTab.setComponentAt(selectedTab, new JPanel());
        if (Core.getExperiment() == null) {
            return;
        }
        refreshParameters();
        allOff();
        //switch mainPanel
        /*if ((selectedTab == 4)) {
            if (newTab < 4) {
                this.settingsPanel.removeAll();
                this.settingsPanel.add(mainSettingsPanel);
                this.refreshDisplay();
            }
        } else if (newTab >= 4) {
            this.settingsPanel.removeAll();
            this.settingsPanel.add(sampleSettingsPanel);
            this.refreshDisplay();
        }
        * 
        */
        selectedTab=newTab;
        this.hidePanel();
        editTab.setComponentAt(selectedTab, listScroll);
        if (selectedTab==0) {
            listScroll.setViewportView(channelImages.getPanel());
        } else if (selectedTab==1) {
            listScroll.setViewportView(structures.getPanel());
            //editTab.setComponentAt(selectedTab, structures.getPanel());
        } else if (selectedTab==2) {
            listScroll.setViewportView(virtualStructures.getPanel());
            //editTab.setComponentAt(selectedTab, virtualStructures.getPanel());
        } else if (selectedTab==3) {
            listScroll.setViewportView(measurements.getPanel());
            //editTab.setComponentAt(selectedTab, measurements.getPanel());
        } else if (selectedTab==4) {
            listScroll.setViewportView(samples.getPanel());
            //editTab.setComponentAt(selectedTab, samples.getPanel());
        }
    }

    public void save(boolean record) {
        if (Core.getExperiment() == null) {
            return;
        }
        //parameters in mainPanel
        for (Parameter p : xpParams) {
            p.dbPut(Core.getExperiment().getData());
        }
        Core.getExperiment().setChannelImages(this.channelImages.saveAll());
        Core.getExperiment().setStructures(this.structures.saveAll(), true);
        Core.getExperiment().setVirtualStructures(this.virtualStructures.saveAll());
        Core.getExperiment().setMeasurements(this.measurements.saveAll());
        Core.getExperiment().setSamples(this.samples.saveAll());
        if (record) {
            Core.getExperiment().save();
            Core.setExperimentModifiedFromAnalyzer(true);
        }
        this.core.updateSettings();
        IJ.log("xp saved!!");
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        settingsPanel = new javax.swing.JPanel();
        connectPanel = new javax.swing.JPanel();
        sfLabel = new javax.swing.JLabel();
        newFolder = new javax.swing.JButton();
        folders = new javax.swing.JComboBox();
        removeFolder = new javax.swing.JButton();
        eLabel = new javax.swing.JLabel();
        experiments = new javax.swing.JComboBox();
        newExperiment = new javax.swing.JButton();
        renameExperiment = new javax.swing.JButton();
        duplicateExperiment = new javax.swing.JButton();
        deleteExperiment = new javax.swing.JButton();
        save = new javax.swing.JButton();
        override = new javax.swing.JButton();
        editTab = new javax.swing.JTabbedPane();
        editScroll = new javax.swing.JScrollPane();
        editPanel = new javax.swing.JPanel();

        setMaximumSize(new java.awt.Dimension(1024, 600));
        setMinimumSize(new java.awt.Dimension(1024, 600));
        setPreferredSize(new java.awt.Dimension(1024, 600));

        settingsPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("General Options"));
        settingsPanel.setMaximumSize(new java.awt.Dimension(362, 243));
        settingsPanel.setMinimumSize(new java.awt.Dimension(362, 243));
        settingsPanel.setPreferredSize(new java.awt.Dimension(362, 243));
        settingsPanel.setLayout(new java.awt.GridLayout(1, 0));

        connectPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Select Experiment"));
        connectPanel.setMaximumSize(new java.awt.Dimension(362, 321));
        connectPanel.setMinimumSize(new java.awt.Dimension(362, 321));

        sfLabel.setText("Project:");

        newFolder.setText("New");
        newFolder.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                newFolderActionPerformed(evt);
            }
        });

        folders.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "" }));
        folders.setMaximumSize(new java.awt.Dimension(200, 28));
        folders.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                foldersItemStateChanged(evt);
            }
        });

        removeFolder.setText("Delete");
        removeFolder.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                removeFolderActionPerformed(evt);
            }
        });

        eLabel.setText("Experiment:");

        experiments.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "" }));
        experiments.setMaximumSize(new java.awt.Dimension(200, 28));
        experiments.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                experimentsItemStateChanged(evt);
            }
        });

        newExperiment.setText("New");
        newExperiment.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                newExperimentActionPerformed(evt);
            }
        });

        renameExperiment.setText("Rename");
        renameExperiment.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                renameExperimentActionPerformed(evt);
            }
        });

        duplicateExperiment.setText("Duplicate");
        duplicateExperiment.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                duplicateExperimentActionPerformed(evt);
            }
        });

        deleteExperiment.setText("Delete");
        deleteExperiment.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                deleteExperimentActionPerformed(evt);
            }
        });

        save.setText("Save Changes");
        save.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                saveActionPerformed(evt);
            }
        });

        override.setText("Override");
        override.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                overrideActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout connectPanelLayout = new javax.swing.GroupLayout(connectPanel);
        connectPanel.setLayout(connectPanelLayout);
        connectPanelLayout.setHorizontalGroup(
            connectPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(connectPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(connectPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, connectPanelLayout.createSequentialGroup()
                        .addComponent(newFolder, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(removeFolder, javax.swing.GroupLayout.PREFERRED_SIZE, 163, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(folders, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(experiments, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(connectPanelLayout.createSequentialGroup()
                        .addGroup(connectPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(eLabel)
                            .addComponent(sfLabel))
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, connectPanelLayout.createSequentialGroup()
                        .addGroup(connectPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                            .addComponent(duplicateExperiment, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(newExperiment, javax.swing.GroupLayout.DEFAULT_SIZE, 163, Short.MAX_VALUE)
                            .addComponent(override, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGroup(connectPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(save, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(renameExperiment, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 163, Short.MAX_VALUE)
                            .addComponent(deleteExperiment, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 163, Short.MAX_VALUE))))
                .addContainerGap())
        );
        connectPanelLayout.setVerticalGroup(
            connectPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(connectPanelLayout.createSequentialGroup()
                .addGap(0, 0, Short.MAX_VALUE)
                .addComponent(sfLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(folders, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(connectPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(removeFolder)
                    .addComponent(newFolder))
                .addGap(24, 24, 24)
                .addComponent(eLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(experiments, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(connectPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(renameExperiment)
                    .addComponent(newExperiment))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(connectPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(duplicateExperiment)
                    .addComponent(deleteExperiment))
                .addGap(25, 25, 25)
                .addGroup(connectPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(save)
                    .addComponent(override))
                .addContainerGap())
        );

        editTab.setMinimumSize(new java.awt.Dimension(644, 280));
        editTab.setPreferredSize(new java.awt.Dimension(644, 280));

        editPanel.setMinimumSize(new java.awt.Dimension(625, 250));
        editPanel.setPreferredSize(new java.awt.Dimension(625, 250));

        javax.swing.GroupLayout editPanelLayout = new javax.swing.GroupLayout(editPanel);
        editPanel.setLayout(editPanelLayout);
        editPanelLayout.setHorizontalGroup(
            editPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 642, Short.MAX_VALUE)
        );
        editPanelLayout.setVerticalGroup(
            editPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 274, Short.MAX_VALUE)
        );

        editScroll.setViewportView(editPanel);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(connectPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(settingsPanel, javax.swing.GroupLayout.PREFERRED_SIZE, 368, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(editScroll)
                    .addComponent(editTab, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGap(6, 6, 6))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(editTab, javax.swing.GroupLayout.PREFERRED_SIZE, 318, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(editScroll, javax.swing.GroupLayout.DEFAULT_SIZE, 276, Short.MAX_VALUE))
            .addGroup(layout.createSequentialGroup()
                .addComponent(connectPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(settingsPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void newExperimentActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_newExperimentActionPerformed
        String name = JOptionPane.showInputDialog("Experiment Name");
        if (name==null) return;
        if (utils.isValid(name, false) && !utils.contains(experiments, name, false)) {
            Core.mongoConnector.createExperiment(name);
            getXPs();
            experiments.setSelectedItem(name);
            setXP(name);
        } else {
            IJ.error("Invalid Name/Experiment already exists");
        }
    }//GEN-LAST:event_newExperimentActionPerformed

    private void renameExperimentActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_renameExperimentActionPerformed
        if (experiments.getSelectedIndex() >= 0) {
            String old_name = utils.getSelectedString(experiments);
            String name = JOptionPane.showInputDialog("Rename Experiment from:" + old_name + " to:", old_name);
            if (name==null || name.equals(old_name)) return;
            if (utils.isValid(name, false) && !utils.contains(experiments, name, false)) {
                Core.mongoConnector.renameExperiment(old_name, name);
                getXPs();
                experiments.setSelectedItem(name);
                setXP(name);
            } else {
                IJ.error("Invalid Name/Experiment already exists");
            }
        } else {
            IJ.error("Select XP first");
        }
    }//GEN-LAST:event_renameExperimentActionPerformed

    private void removeFolderActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_removeFolderActionPerformed
        Object name = folders.getSelectedItem();
        if (name != null && JOptionPane.showConfirmDialog(this, "Remove Folder:" + name + " and Experiments associated?", "TANGO", JOptionPane.OK_CANCEL_OPTION) == 0) {
            Core.mongoConnector.removeProject((String) name);
            folders.removeItem(name);
            experiments.removeAllItems();
        }
    }//GEN-LAST:event_removeFolderActionPerformed

    private void newFolderActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_newFolderActionPerformed
        String name = JOptionPane.showInputDialog("Project Name (no special chars)");
        if (name==null) return;
        if (!utils.isValid(name, false)) {
            IJ.error("Invalid name (no speical chars allowed)");
            return;
        }
        if (Core.mongoConnector.getUserName().length()+name.length()>50) {
            IJ.error("Name is too long");
            return;
        }
        if (!utils.contains(folders, name, false)) {
            Core.mongoConnector.createProject(name);
            getFolders();
            folders.setSelectedItem(name);
            setProject(name);
        } else {
            IJ.error("Project already exists");
        }
    }//GEN-LAST:event_newFolderActionPerformed

    private void deleteExperimentActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_deleteExperimentActionPerformed
        if (experiments.getSelectedIndex() >= 0) {
            String name = (String) experiments.getSelectedItem();
            if (JOptionPane.showConfirmDialog(this, "Remove Experiment:" + name + " and mesures associated?", "TANGO", JOptionPane.OK_CANCEL_OPTION) == 0) {
                //IJ.log("obj:"+mc.experiment.findOne(new BasicDBObject("outputFileName", outputFileName)).toString());
                Core.mongoConnector.removeExperiment(name);
                experiments.removeItem(name);
            }
        } else {
            IJ.error("Select XP first");
        }
    }//GEN-LAST:event_deleteExperimentActionPerformed

    private void duplicateExperimentActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_duplicateExperimentActionPerformed
        if (experiments.getSelectedIndex() >= 0) {
            //String[] dest = TextChoiceJOptionPane.showInputDialog("Enter Name and Destination Set:", Core.mongoConnector.getProjects());
            DuplicateXP d = DuplicateXPOptionPane.showInputDialog("Duplicate or Override Experiment:", Core.mongoConnector.getProjects(), utils.getSelectedString(experiments));
            if (d == null) {
                return;
            }
            String source = (String) experiments.getSelectedItem();
            if (d.set.equals(folders.getSelectedItem())) {
                if (d.xp != null && d.xp.length() > 0 && !Core.mongoConnector.getExperiments().contains(d.xp)) {
                    Core.mongoConnector.duplicateExperiment(Core.mongoConnector, source, d.xp);
                    getXPs();
                    experiments.setSelectedItem(d.xp);
                    setXP(d.xp);
                } else {
                    IJ.error("name must be different");
                }
            } else {
                MongoConnector mc2 = Core.mongoConnector.duplicate(false);
                mc2.setProject(d.set);
                if (d.xp != null && d.xp.length() > 0 && !mc2.getExperiments().contains(d.xp)) {
                    mc2.duplicateExperiment(Core.mongoConnector, source, d.xp);
                    folders.setSelectedItem(d.set);
                    setProject(d.set);
                    experiments.setSelectedItem(d.xp);
                    setXP(d.xp);
                } else {
                    IJ.error("XP already existing in " + d.set);
                }
                mc2.close();
            }
        }
    }//GEN-LAST:event_duplicateExperimentActionPerformed

    private void saveActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_saveActionPerformed
        if (Core.getExperiment() == null) {
            return;
        }
        save(true);
        //refreshParameters();
        // FIXME save
        //save(); //in case modif on structures / virtualStructure would shift indexes
    }//GEN-LAST:event_saveActionPerformed

    private void foldersItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_foldersItemStateChanged
        if (init) {
            return;
        }
        if (evt.getStateChange() == 1) {
            String name = (String) folders.getSelectedItem();
            setProject(name);
        }
    }//GEN-LAST:event_foldersItemStateChanged

    private void experimentsItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_experimentsItemStateChanged
        if (init) {
            return;
        }
        if (evt.getStateChange() == 1) {
            String name = (String) experiments.getSelectedItem();
            setXP(name);
        }
    }//GEN-LAST:event_experimentsItemStateChanged

    private void overrideActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_overrideActionPerformed
        OverrideXPElement[] xps = OverrideXPOptionPane.showInputDialog(5, "", "");
        if (xps!=null) {
            MongoConnector mc2 = Core.mongoConnector.duplicate(false);  
            for (OverrideXPElement o : xps) {
                mc2.setProject(o.project);
                BasicDBObject xp = mc2.getExperiment(o.xp);
                if (xp != null) {
                    if (o.channelImages) {
                        xp.append("channelFiles", Core.getExperiment().getChannelImages());
                    }
                    if (o.structures) {
                        xp.append("structures", Core.getExperiment().getStructures());
                    }
                    if (o.virtualStructures) {
                        xp.append("virtualStructures", Core.getExperiment().getVirtualStructures());
                    }
                    if (o.measurements) {
                        xp.append("measurements", Core.getExperiment().getMeasurementSettings());
                    }
                    if (o.samplers) {
                        xp.append("sampleChannels", Core.getExperiment().getSampleChannels());
                    }
                    mc2.saveExperiment(xp);
                    IJ.log("Override: "+o);
                }
                    
            }
            mc2.close();
        }
    }//GEN-LAST:event_overrideActionPerformed

    private void testSampleActionPerformed(java.awt.event.ActionEvent evt) {
        SampleRunner.test();
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel connectPanel;
    private javax.swing.JButton deleteExperiment;
    private javax.swing.JButton duplicateExperiment;
    private javax.swing.JLabel eLabel;
    private javax.swing.JPanel editPanel;
    private javax.swing.JScrollPane editScroll;
    private javax.swing.JTabbedPane editTab;
    private javax.swing.JComboBox experiments;
    private javax.swing.JComboBox folders;
    private javax.swing.JButton newExperiment;
    private javax.swing.JButton newFolder;
    private javax.swing.JButton override;
    private javax.swing.JButton removeFolder;
    private javax.swing.JButton renameExperiment;
    private javax.swing.JButton save;
    private javax.swing.JPanel settingsPanel;
    private javax.swing.JLabel sfLabel;
    // End of variables declaration//GEN-END:variables
}
