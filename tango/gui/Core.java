package tango.gui;
import tango.gui.util.Displayer;
import mcib3d.utils.exceptionPrinter;
import ij.IJ;
import ij.WindowManager;
import java.awt.Component;
import java.awt.Dimension;
import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintStream;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import tango.analysis.AnalysisCore;
import tango.dataStructure.Experiment;
import tango.gui.util.VerticalTextIcon;
import tango.helper.Helper;
import tango.mongo.MongoConnector;
import tango.parameter.SettingsParameter;
import tango.plugin.PluginFactory;
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
public class Core extends JFrame implements Displayer {
    public static boolean SPATIALSTATS=true;
    public static double VERSION  = 0.84;
    public static boolean TESTING=false;
    public static boolean ANALYSIS = true; //Useless, // si tant que c'est en dev on va garder ça.
    private Component[] panels, processingPanels;
    private JTabbedPane tabs,processingTabs;
    public static MongoConnector mongoConnector;
    private static Experiment experiment;
    private static AnalysisCore analysis;
    public static Helper helper;
    private Connector connector;
    private XPEditor xpEditor;
    private ProcessingSequenceEditorTemplateNucleus nucleusTemplateEditor;
    private ProcessingSequenceEditorTemplateStructure structureTemplateEditor;
    private ProcessingSequenceEditor processingChainEditor;
    private FieldManager fieldManager;
    private CellManager cellManager;
    private int selectedTab, selectedProcessingTab;
    private JScrollPane scroll;
    public static Dimension minSize = new Dimension(1024, 700);
    public static boolean GUIMode=true;
    public static boolean debug=false;
    private static Progressor progressor;
    protected static boolean experimentModifiedFromAnalyzer;
    
    public Core() {
        super("TANGO: Tools for Analysis of Nuclear Genome Organisation - Version: "+VERSION);
        setResizable(true);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        toFront();
        init();
        selectedTab = 0;
        experimentModifiedFromAnalyzer = true;
    }
    
    public static boolean experimentModifiedFromAnalyzer() {
        return experimentModifiedFromAnalyzer;
    }
    
    public static void setExperimentModifiedFromAnalyzer(boolean modified) {
        experimentModifiedFromAnalyzer=modified;
    }
    
    public static synchronized Progressor getProgressor() {
        if (progressor==null) {
            progressor=new Progressor();
            progressor.setVisible(true);
            progressor.toFront();
        }
        return progressor;
    }
    
    public static synchronized void killProgressor() {
        if (progressor!=null) {
            progressor.dispose();
            progressor=null;
        }
    }
    
    public synchronized void toggleIsRunning (boolean isRunning) {
        System.out.println("toggle is runing :"+isRunning);
        if (this.xpEditor!=null) xpEditor.toggleIsRunning(isRunning);
        if (this.connector!=null) connector.toggleIsRunning(isRunning);
        if (cellManager!=null) cellManager.toggleIsRunning(isRunning);
        if (fieldManager!=null) fieldManager.toggleIsRunning(isRunning);
        if (!isRunning) killProgressor();
    }
    
    public static int getMaxCPUs() {
        return Connector.maxThreads.getValue();
    }
    
    public static int getMaxCellProcess() {
        return Connector.maxCellsProcess.getValue();
    }
    
    public static int getMaxCellMeasurement() {
        return Connector.maxCellsMeasure.getValue();
    }
    
    public static double getAvailableMemory() { // in Mb
        return Runtime.getRuntime().freeMemory()/(1024*1024);
    }
    
    public static double getMaxMemory() { // in Mb
        return IJ.maxMemory() / (1024*1024);
    }
    
    public void setRedirectSysOutToFile() {
        // TODO pas encore testée. utile en cas de crash
        String path = IJ.getDirectory("imagej");
        File file  = new File(path+"/sysout.log");
        try {
            file.createNewFile();
            PrintStream printStream = new PrintStream(new FileOutputStream(file));
            System.setOut(printStream);
            System.out.println("Execution time: "+System.currentTimeMillis());
        } catch (Exception e) {
            exceptionPrinter.print(e, "", Core.GUIMode);
        }
    }
    
    public static MongoConnector getMongoConnector(){
        if(mongoConnector==null) mongoConnector = new MongoConnector("localhost");
        return mongoConnector;
    }
    
    public Connector getConnector() {
        return connector;
    }
    
    public JScrollPane getScrollPane() {
        return scroll;
    }
    
    public XPEditor getXPEditor() {
        return xpEditor;
    }
    
    public ProcessingSequenceEditorTemplateNucleus getProcessingSequenceEditorNucleus() {
        return nucleusTemplateEditor;
    }
    
    public ProcessingSequenceEditorTemplateStructure getProcessingSequenceEditorStructure() {
        return structureTemplateEditor;
    }
    
    public ProcessingSequenceEditor getProcessingSequenceEditor() {
        return this.processingChainEditor;
    }
    
    public FieldManager getFieldManager() {
        return fieldManager;
    }
    
    public CellManager getCellManager() {
        return cellManager;
    }
    
    public void setExperiment(Experiment xp) {
        System.out.println("Set XP!!");
        Core.experiment=xp;
        Core.experimentModifiedFromAnalyzer=true;
        processingChainEditor=new ProcessingSequenceEditor(this);
        processingPanels[0] = processingChainEditor.getPanel();
        processingTabs.setSelectedIndex(0);
        this.selectProcessingTab(0);
        if (fieldManager!=null) fieldManager.setXP(xp);
    } 
    
    public static void setXP(Experiment xp) {
        Core.experiment=xp;
        Core.experimentModifiedFromAnalyzer=true;
    }
    
    public static Experiment getExperiment() {
        return Core.experiment;
    }

    private void init() {
        IJ.showStatus("TANGO: updating...");
        PluginFactory.findPlugins();
        IJ.showStatus("TANGO: initializing GUI...");
        WindowManager.addWindow(this);
        //frame.setLayout(new FlowLayout(FlowLayout.LEFT, 5, 5));
        //setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
        IJ.showStatus("TANGO: initializing panels...");
        initPanel();
        IJ.showStatus("TANGO: adding tabs ...");
        scroll = new JScrollPane(tabs, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        Dimension scrollSize = new Dimension(minSize.width+15, minSize.height+15);
        scroll.setPreferredSize(scrollSize); 
        add(scroll, java.awt.BorderLayout.CENTER);
        setPreferredSize(scrollSize);
        pack();
        disableTabs();
        ij.gui.GUI.center(this);
        setVisible(true);
        toFront();
        IJ.showStatus("TANGO: initialized!");
        IJ.showStatus("");
    }

    private void initPanel()  {
        panels = new Component[6];
        if (IJ.isMacOSX() || IJ.isMacintosh()) {
            processingTabs = new JTabbedPane(JTabbedPane.TOP, JTabbedPane.WRAP_TAB_LAYOUT);
            processingTabs.addTab("Current Structures", new JPanel());
            processingTabs.addTab("Nucleus Templates", new JPanel());
            processingTabs.addTab("Structure Template", new JPanel());
        } else {
            processingTabs = VerticalTextIcon.createTabbedPane(JTabbedPane.LEFT);
            VerticalTextIcon.addTab(processingTabs, "Current Structures", new JPanel());
            VerticalTextIcon.addTab(processingTabs, "Nucleus Templates", new JPanel());
            VerticalTextIcon.addTab(processingTabs, "Structure Template", new JPanel());
        }
        processingPanels = new Component[3];
        connector = new Connector(this);
        panels[0] = connector;
        panels[2]=processingTabs;
        tabs = new JTabbedPane(JTabbedPane.TOP, JTabbedPane.WRAP_TAB_LAYOUT);
        tabs.addTab("Connect", panels[0]);
        tabs.addTab("Edit Experiment", new JPanel());
        tabs.addTab("Edit Processing Chains", new JPanel());
        tabs.addTab("Data", new JPanel());
        if(ANALYSIS){
               analysis = new AnalysisCore(this);
               panels[4]=analysis.getPanel();
               tabs.addTab("Analysis", new JPanel());
        }
        //this.dimensions=new Dimension[panels.length];
        //for (int i = 0; i<dimensions.length;i++) dimensions[i]=new Dimension(minSize.width+15, minSize.height+15);
        //dimension=new Dimension(minSize.width+15, minSize.height+15);
        tabs.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                //dimensions[selectedTab]=new Dimension(getSize().width, getSize().height);
                //dimensions[selectedTab]=new Dimension(getSize().width, getSize().height);
                tabs.setComponentAt(selectedTab, new JPanel());
                if (selectedTab==0) { //connector
                    connector.saveOptions();
                }
                
                selectedTab = tabs.getSelectedIndex();
                if (selectedTab == 1) { //xpEditor
                    
                } else if (selectedTab == 3) { //data
                    try {
                        Core.experiment.refresh();
                    } catch (Exception ex) {
                        exceptionPrinter.print(ex, "", Core.GUIMode);
                    }
                } else if (selectedTab == 4) { //analysis
                    try {
                        Core.experiment.refresh();
                        System.out.println("Analysis On");
                        analysis.setXP();
                    } catch (Exception ex) {
                        exceptionPrinter.print(ex, "", Core.GUIMode);
                    }
                }
                tabs.setComponentAt(selectedTab, panels[selectedTab]);
                //setPreferredSize(dimensions[selectedTab]);
                refreshDisplay();
                
            }
        });
        processingTabs.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                selectProcessingTab(processingTabs.getSelectedIndex());
            }
        });
    }
    
    protected void selectProcessingTab(int newTab) {
        //if (selectedTab==2) dimensions[2]=new Dimension(getSize().width, getSize().height);
        processingTabs.setComponentAt(selectedProcessingTab, new JPanel());
        
        selectedProcessingTab = newTab;
        
        processingTabs.setComponentAt(selectedProcessingTab, processingPanels[selectedProcessingTab]);
        //setPreferredSize(dimensions[2]);
        refreshDisplay();
    }

    public void connect() {
        try {
            SettingsParameter.setSettings();
            toggleEnableTabs(false);
            nucleusTemplateEditor = new ProcessingSequenceEditorTemplateNucleus(this);
            processingPanels[1] = nucleusTemplateEditor.getPanel();
            structureTemplateEditor = new ProcessingSequenceEditorTemplateStructure(this);
            processingPanels[2] = structureTemplateEditor.getPanel();
            processingTabs.setComponentAt(1, processingPanels[1]);
            selectedProcessingTab=1;
            processingTabs.setSelectedIndex(1);
            xpEditor = new XPEditor(this);
            panels[1] = xpEditor;
            fieldManager = new FieldManager(this);
            fieldManager.setXP(experiment);
            panels[3] = fieldManager.getPanel();
            cellManager=fieldManager.getCellManager();
            if (helper!=null) {
                helper.registerComponents();
                helper.register();
            }
        } catch (Exception e) {
            exceptionPrinter.print(e, null, Core.GUIMode);
        }
    }

    public void toggleEnableTabs(boolean xpSet) {
        if (tabs==null) return;
        int dec=xpSet?0:1;
        for (int i = 1; i < tabs.getTabCount()-dec; i++) {
            tabs.setEnabledAt(i, true);
        }
        if (!xpSet) {
            tabs.setEnabledAt(3, false);
            processingTabs.setEnabledAt(0, false);
        } else {
            processingTabs.setEnabledAt(0, true);
        }
    }

    public void disableTabs() {
       if (tabs==null) return;
       for (int i = 1; i < tabs.getTabCount(); i++) {
            tabs.setEnabledAt(i, false);
       }
       processingTabs.setEnabledAt(0, false);
    }
    
    public void updateSettings() {
        SettingsParameter.setSettings();
        if (this.xpEditor!=null) xpEditor.refreshParameters();
        if (this.nucleusTemplateEditor!=null) nucleusTemplateEditor.refreshParameters();
        if (this.structureTemplateEditor!=null) structureTemplateEditor.refreshParameters();
        if (this.processingChainEditor!=null) processingChainEditor.refreshParameters();
        if (this.cellManager!=null && Core.getExperiment()!=null) cellManager.updateXP();
    }
    
    
    public void close() {
        if (mongoConnector!=null) mongoConnector.close();
        if (helper!=null) Core.helper.close();
        if (this.progressor!=null) {
            progressor.dispose();
            progressor=null;
        }
        Main_.core=null;
    }
    
    @Override
    public void dispose() {
        super.dispose();
        close();
    }
    
    @Override
    public void refreshDisplay() {
        setPreferredSize(getSize());
        pack();
    }
}
