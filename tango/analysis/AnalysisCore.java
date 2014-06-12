package tango.analysis;

import com.mongodb.BasicDBList;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeModel;
import mcib3d.utils.exceptionPrinter;
import org.omancode.r.RFace;
import org.omancode.r.RFaceException;
import org.omancode.r.types.RDataFrame;
import org.omancode.r.types.UnsupportedTypeException;
import org.omancode.r.ui.RObjectTreeBuilder;
import org.rosuda.REngine.REXP;
import org.rosuda.REngine.REXPString;
import org.rosuda.REngine.RList;
import tango.dataStructure.Experiment;
import tango.dataStructure.Selection;
import tango.gui.Core;
import tango.mongo.MongoConnector;
import tango.plugin.measurement.MeasurementKey;
import tango.plugin.measurement.MeasurementObject;
import tango.plugin.measurement.MeasurementStructure;
import tango.util.utils;
/**
 *
 **
 * /**
 * Copyright (C) 2012 Julien Cochennec
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
 * @author Julien Cochennec
 */

public class AnalysisCore {

    AnalysisGUI layout;
    public Core core;
    public RFace rInterface;
    public AnalysisCore(Core newcore) {
        try {
            core=newcore;
            layout = new AnalysisGUI(this);
            System.out.println(System.getProperty("R_HOME"));
            try {
                this.rInterface = RFace.getInstance(layout.getConsole());
                try {
                    rInterface.loadRSupportFunctions();
                } catch (IOException ex) {
                    Logger.getLogger(AnalysisCore.class.getName()).log(Level.SEVERE, null, ex);
                }
            } catch (RFaceException e) {
                        System.err.println(e.getMessage());
            }
        } catch (SecurityException ex) {
            Logger.getLogger(AnalysisCore.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public void refreshRObjects(){
        layout.refreshRObjectTree();
    }     
            
    public REXP executeRCommand(String cmd, boolean silent) {
        try {
            rInterface.printlnToConsole(cmd);
        } catch (RFaceException e1) {
            exceptionPrinter.print(e1, cmd, true);
        }
        REXP e;
        try {
            e = rInterface.parseEvalTry(cmd, silent);
            layout.getConsole().printPrompt();
            return e;
        } catch (RFaceException ex) {
            Logger.getLogger(AnalysisCore.class.getName()).log(Level.SEVERE, null, ex);
            layout.getConsole().printPrompt();
            return null;
        }
        
    }
    
    public REXP [] executeRCommands(String [] cmds, boolean silent) {
        int n = cmds.length;
        REXP [] es = new REXP[n];
        for(int i=0; i<n; i++){
            REXP e = executeRCommand(cmds[i], silent);
            es[i] = e;
        }
        return es;
    }
    
    protected void initRSession() {
        Experiment experiment = getExperiment();
        String [] rCommands;
        if (experiment==null) {
             rCommands= new String[]{
            "require(rtango)",
            "session=tangoSession$new()",
            "session$initFields()"
         };
        } else {  
        rCommands = new String[]{
            "require(rtango)",
            "session=tangoSession$new()",
            "session$initFields()",
            "xp=session$addExperiment(projectName='tango_"+experiment.getUserName()+'_'+experiment.getFolder()+"',host='"+experiment.getConnector().getHost()+"',experimentName='"+experiment.getName()+"')"
         };
        }
        executeRCommands(rCommands, false);
        initPlots();
    }
    
    private void initPlots() { // code a mettre dans le package?
        // code pour initialiser les parametres de ggplot (pour faire zoli)
        String rCommand = "session$initPlots()";
        executeRCommand(rCommand, false);
    }
    
    public void setXP() {
        if(Core.experimentModifiedFromAnalyzer()){
            initRSession();
            layout.setXP();
            Experiment experiment = getExperiment();
            if (experiment==null) return;
            String rCommand = "xp=session$addExperiment(projectName='tango_"+experiment.getUserName()+'_'+experiment.getFolder()+"', host='"+experiment.getConnector().getHost()+"', experimentName='"+experiment.getName()+"')";
            REXP e = executeRCommand(rCommand, false);
            Core.setExperimentModifiedFromAnalyzer(false);
        }
    }
    
    public String[] getStructureNames(){
        return getExperiment().getStructureNames(true);
    }
    
    public void refresh(){
        setXP();
    }
    
    public AnalysisGUI getPanel() {
        return layout;
    }
    
    public JTree getRObjectTree(){
        JTree tree;
        try {
            tree = new RObjectTreeBuilder(this.rInterface).getTree();
        } catch (RFaceException ex) {
            Logger.getLogger(AnalysisCore.class.getName()).log(Level.SEVERE, null, ex);
            tree = new JTree();
        }
        return tree;
    }
    
    public TreeModel getRObjectTreeModel(){
        return getRObjectTree().getModel();
    }
    
    public void initGD(){
        String[] cmds = new String[]{
            ".setenv <- if (exists('Sys.setenv')) Sys.setenv else Sys.putenv",
            ".setenv('JAVAGD_CLASS_NAME'='tango/rGraphicDevice/rTangoJavaGD')",
            "library(JavaGD)"
        };
        REXP[] es = executeRCommands(cmds, false);
    }
    
    public ArrayList<String> getDataFrameNames(){
        ArrayList<String> names = new ArrayList<String>();
        try {
            RList rlist = rInterface.parseEvalTryAsRList(".getObjects(include='data.frame')");
            String[] namesArray = ((REXPString) rlist.get("names")).asStrings();
            for (int i = 0; i < namesArray.length; i++) {
		names.add(namesArray[i]);
            }
        } catch (RFaceException ex) {
            Logger.getLogger(AnalysisCore.class.getName()).log(Level.SEVERE, null, ex);
        }
        return names;
    }
    
    public boolean existDataFrameName(String name){
        return getDataFrameNames().contains(name);
    }
    
    public RDataFrame getDataFrameByName(String name) throws RFaceException, UnsupportedTypeException{
        return new RDataFrame("df", executeRCommand(name,true));
    }
    
    public Map<String,String> getDataFrameColumnNames(String name) throws RFaceException, UnsupportedTypeException{
        RDataFrame dataframe = getDataFrameByName(name);
        return dataframe.getConcreteMap();
    }
    
    public ArrayList<String> getDataFrameNumericColumnNames(String name) throws RFaceException, UnsupportedTypeException{
        ArrayList<String> names = new ArrayList<String>();
        RDataFrame dataframe = getDataFrameByName(name);
        String[] n = dataframe.getColumnNames();
        Class[] t = dataframe.getColumnTypes();
        for (int i = 0; i < n.length; i++) {
            String a = t[i].toString();
            if("class java.lang.Integer".equals(a)||"class java.lang.Double".equals(a)) names.add(n[i]);
        }
        System.out.println(names);
        return names;
    }
    
    public ArrayList<String> getDataFrameStringColumnNames(String name) throws RFaceException, UnsupportedTypeException{
        ArrayList<String> names = new ArrayList<String>();
        RDataFrame dataframe = getDataFrameByName(name);
        String[] n = dataframe.getColumnNames();
        Class[] t = dataframe.getColumnTypes();
        for (int i = 0; i < n.length; i++) {
            String a = t[i].toString();
            if("class java.lang.String".equals(a)) names.add(n[i]);
        }
        System.out.print(names);
        return names;
    }
    
    public ArrayList<String> getDataFrameAllColumnNames(String name) throws RFaceException, UnsupportedTypeException{
        ArrayList<String> names = new ArrayList<String>();
        RDataFrame dataframe = getDataFrameByName(name);
        String[] namesArray = dataframe.getColumnNames();
        for (int i = 0; i < namesArray.length; i++) {
            names.add(namesArray[i]);
        }
        return names;
    }
    
    static ArrayList<Integer> getMeasurementTypes(Map<MeasurementKey,ArrayList<String>> keys) {
        ArrayList<Integer> measurementTypes = new ArrayList<Integer>();
        for (Map.Entry<MeasurementKey,ArrayList<String>> m : keys.entrySet()) {
            MeasurementKey mkey = m.getKey();
            Integer i = new Integer(mkey.type);
            measurementTypes.add(i);
        }
        //Make each element unique with a set function
        Set uniqueMeasurementTypes = new HashSet(measurementTypes);
        measurementTypes = new ArrayList<Integer>(uniqueMeasurementTypes);
        return measurementTypes;
    }
    
    static ArrayList<int []> getTypeStructures(Integer type,Map<MeasurementKey,ArrayList<String>> keys){
        ArrayList<int []> structures = new ArrayList<int[]>();
        for (Map.Entry<MeasurementKey,ArrayList<String>> m : keys.entrySet()) {
            MeasurementKey mkey = m.getKey();
            int[] s = mkey.getStructures();
            if(mkey.type == type) structures.add(s);
        }
        return structures;
    }
    
    static ArrayList<String> getStructureKeys(Integer type,int [] structures,Map<MeasurementKey,ArrayList<String>> keys) {
        for (Map.Entry<MeasurementKey,ArrayList<String>> m : keys.entrySet()) {
            MeasurementKey mkey = m.getKey();
            if(mkey.type == type && mkey.getStructures()== structures) return m.getValue();
        }
        return null;
    }
    
    public TreeModel getMeasurementTree (Map<MeasurementKey,ArrayList<String>> keys) {
        DefaultMutableTreeNode root = new DefaultMutableTreeNode(getDescription());
        for(Integer mt : getMeasurementTypes(keys)){
            DefaultMutableTreeNode typeNode = new DefaultMutableTreeNode(getTypeLabel(mt));
            for(int [] ts: getTypeStructures(mt,keys)){
                DefaultMutableTreeNode structureNode = new DefaultMutableTreeNode(getStructureLabel(ts));
                for(String sk : getStructureKeys(mt,ts,keys)){
                    DefaultMutableTreeNode keyNode = new DefaultMutableTreeNode(sk.toString());
                    structureNode.add(keyNode);
                }
                typeNode.add(structureNode);
            }
            root.add(typeNode);
        }
        TreeModel tm = new DefaultTreeModel(root);
        return tm;
    }
    
    public TreeModel getAllMeasurementTree (){
        return getMeasurementTree(Core.getExperiment().getKeys());
    }
    
    static public Experiment getExperiment() {
        return Core.getExperiment();
    }
    
    static BasicDBList getMeasurements() {
        return Core.getExperiment().getMeasurementSettings();
    }
    
    static HashMap getKeys() {
        return Core.getExperiment().getKeys();
    }
    
    static String getDescription() {
        if(Core.getExperiment()==null) return "None";
        else return Core.getExperiment().getFolder()+" > "+Core.getExperiment().getName();
    }
    
    protected MongoConnector getConnector() {
        return Core.mongoConnector;
    }
    
    public String getTypeLabel(Integer i){
        if(i==MeasurementObject.Number) return "Object Number";
        if(i==MeasurementStructure.Number) return "Structure Number";
        if(i==MeasurementStructure.ArrayO2O) return "Object to Object Array";
        if(i==MeasurementStructure.ArrayMisc) return "Misc Array";
        return null;
    }
    
    public int getTypeIndex(String l){
        if("Object Number".equals(l)) return -1;
        if("Structure Number".equals(l)) return 0;
        if("Object to Object Array".equals(l)) return 1;
        if("Misc Array".equals(l)) return 2;
        return -2;
    }
    
    public String getStructureLabel(int [] i){
        //String [] colors = getExperiment().getStructureColors(true);
        String [] names = getExperiment().getStructureNames(true);
        int n = i.length;
        String [] labels = new String [n];
        for(int j=0;j<n;j++){
            if (i[j]>=0 & i[j]<names.length) labels[j]=names[i[j]];
            else labels[j]="noNRame";
        }
        return "["+utils.join(labels,",")+"]";
    }
    
    

    public int [] getStructureIndexes(String l) {
        String[] names = getExperiment().getStructureNames(true);
        String [] s = l.substring(1,l.length()-1).split(",");
        int n = s.length;
        int [] indexes = new int[n];
        for(int j=0;j<n;j++){
            String name = s[j];
            for(int i=0;i<names.length;i++){
                if(name.equals(names[i])) indexes[j]=i;
            }
        }
        return indexes;
    }
    
    public String getFilterCommand(String[] selectionNames, String[] tagNames, Map<String,String> objectNumberStrings){
        String selections;
        if(selectionNames.length==0) selections = "NULL";
        else{
            selections = "c(";
            for (int j=0;j<selectionNames.length;j++) {
                if(j<selectionNames.length-1) selections += "'"+selectionNames[j]+"',";
                else selections += "'"+selectionNames[j]+"')";
            }
        }
        String tags;
        if(tagNames.length==0) tags = "NULL";
        else{
            tags = "c(";
            for (int j=0;j<tagNames.length;j++) {
                if(j<tagNames.length-1) tags += tagNames[j]+",";
                else tags += tagNames[j]+")";
            }
        }
        ArrayList<String> objectNumberRDescriptions = new ArrayList<String>();
        for (Map.Entry<String, String> entry : objectNumberStrings.entrySet()){
            if(!"".equals(entry.getValue())) objectNumberRDescriptions.add("'" + entry.getKey() + "'=" + entry.getValue());
        }
        String objectnumbersperstructure = "list("+utils.join(objectNumberRDescriptions.toArray(new String[0]),",")+")";
        return "filter = xp$getFilterFromSelectionsTagsAndObjectNumbers("+selections+","+tags+","+objectnumbersperstructure+")";
    }
    
    public String getConstantColumnCommand(String dataframeName,String constantName,String constantValue){
        return dataframeName+"$"+constantName+"='"+constantValue+"'";
    }
    
    public String getExtractionCommand(String varname,int type,int [] structures,ArrayList<String> keys){
        String fct;
        String st;
        String k=",keys=c('"+utils.join(keys.toArray(new String[0]),"','")+"')";
        String a=")";
        String [] sstructures = new String [structures.length];
        for(int m = 0;m<structures.length;m++){
            sstructures[m] = new Integer(structures[m]).toString();
        }
        ArrayList<String> rCommands = new ArrayList();
        if(type==MeasurementObject.Number){
            fct="=xp$extractObjectData(";
            st="structure=c("+utils.join(sstructures,",")+")";
            a=",addIdx=TRUE,filter=filter)";
        }
        else{
            fct="=xp$extractStructureData(";
            st="structures=c("+utils.join(sstructures,",")+")";
            if(type==MeasurementStructure.ArrayO2O){
                a=",addIdx=TRUE,filter=filter)";
            } else if(type==MeasurementStructure.ArrayMisc){
                a=",addIdx=FALSE,filter=filter)";
            } else if(type==MeasurementStructure.Number){
                a=",addIdx=FALSE,filter=filter)";
            }
        }
        return varname+fct+st+k+a;
    }

    void extract(String dfprefix,Map<String,Map<String,ArrayList<String>>> selectedkeysMap, String[] selectionNames, String[] tagNames, Map<String,String> objectNumberStrings) throws RFaceException, UnsupportedTypeException {
        int i = 0;
        if(!selectedkeysMap.isEmpty()){
            for (Map.Entry<String, Map<String, ArrayList<String>>> entry : selectedkeysMap.entrySet()) {
                int type = getTypeIndex(entry.getKey());
                for(Map.Entry<String, ArrayList<String>> subentry: entry.getValue().entrySet()){
                    i++;
                    int[] structures = getStructureIndexes(subentry.getKey());
                    ArrayList<String> keys = subentry.getValue();
                    String varname = dfprefix;
                    String rFilterCommand = getFilterCommand(selectionNames, tagNames, objectNumberStrings);
                    String rExtractionCommand = getExtractionCommand(varname,type,structures,keys);
                    REXP e1 = executeRCommand(rFilterCommand, false);
                    REXP e2 = executeRCommand(rExtractionCommand, false);
                }
            }
            layout.refreshDataFrames();
        }
    }
    
    void addConstantColumn(String dataFrameName,String constantName, String constantValue) throws RFaceException, UnsupportedTypeException{
        String rConstantColumnCommand = getConstantColumnCommand(dataFrameName, constantName, constantValue);
        REXP e = executeRCommand(rConstantColumnCommand, false);
        layout.refreshDataFrames();
    }

    void ihist(String dataFrameName,String columnName) throws RFaceException {
        rInterface.assign("h", "ihist("+dataFrameName+"$"+columnName+")");
    }
    
    void ibar(String dataFrameName,String columnName) throws RFaceException {
        executeRCommand("ibar("+dataFrameName+"$"+columnName+")", false);
    }

    void iscatter(String dataFrameName,String xColumnName,String yColumnName) throws RFaceException {
        executeRCommand("iplot("+dataFrameName+"$"+xColumnName+","+dataFrameName+"$"+yColumnName+")", false);
    }

    void ibox(String dataFrameName,String columnName, String groupBy) throws RFaceException {
        ArrayList<String> l = new ArrayList();
        executeRCommand("ibox("+dataFrameName+"$"+columnName+",'"+groupBy+"')", false);
    }
    
    void retrieveSelection(String dataFrameName,String selectionName,boolean saveDataFrame,boolean saveSelection) throws RFaceException, UnsupportedTypeException{
        String rSelectionCommand = "xp$extractSelection("+dataFrameName+"[iset.selected(),]";
        if(saveSelection){
            if(selectionName=="") rSelectionCommand += ")$save()";
            else rSelectionCommand += ",selectionName='"+selectionName+"')$save()";
        }else{
            rSelectionCommand += ")";
        }
        executeRCommand(rSelectionCommand, false);
        if(saveDataFrame){
            String rDataframeCommand = dataFrameName+"_"+selectionName+"="+dataFrameName+"[iset.selected(),]";
            executeRCommand(rDataframeCommand, false);
            layout.refreshDataFrames();
        }
        //core.getCellManager().updateSelections();
        layout.selectionsRefresh();
    }

    void merge(String dataFrame1Name, String dataFrame2Name, String byColumn, String prefix) {
        executeRCommand(prefix+"."+dataFrame1Name+"."+dataFrame2Name+"= merge("+dataFrame1Name+","+dataFrame2Name+",by='"+byColumn+"')", false);
    }

    public ArrayList<String> getSelections() {
        ArrayList<String> selectionNames = new ArrayList<String>();
        Experiment xp = Core.getExperiment();
        ArrayList<Selection> sels = Core.mongoConnector.getSelections(xp.getId());
        for (Selection s : sels ) selectionNames.add(s.getName());
        return selectionNames;
    }

    void concatenate(String dataFrame1Name, String dataFrame2Name) throws RFaceException, UnsupportedTypeException {
        executeRCommand("concatenate."+dataFrame1Name+"."+dataFrame2Name+"= rbind.fill("+dataFrame1Name+","+dataFrame2Name+")", false);
        layout.refreshDataFrames();
    }
    
    void plotECDF(String dataFrameName, String varName, String color, String linetype, String facet) {
        String rJavaGDCommand = "JavaGD(name="+dataFrameName+" > "+varName+")";
        String rEcdfCommand = "xp$plotECDF("+dataFrameName+",measure='"+varName+"'";
        if (!"".equals(color)) rEcdfCommand+=", color='"+color+"'";
        if (!"".equals(linetype)) rEcdfCommand+=", linetype='"+linetype+"'";
        if (!"".equals(facet)) rEcdfCommand+=", facet.wrap='"+facet+"'";
        rEcdfCommand+=")";
        String [] rCommands = {
            rJavaGDCommand,
            rEcdfCommand
        };
        executeRCommands(rCommands, false);
    }

    void annotate(String dataFrameName, String columnName, String defaultAnnotation, String[] selectionNames) {
        String selections;
        if(selectionNames.length==0) selections = "NULL";
        else{
            selections = "c(";
            for (int j=0;j<selectionNames.length;j++) {
                if(j<selectionNames.length-1) selections += "'"+selectionNames[j]+"',";
                else selections += "'"+selectionNames[j]+"')";
            }
        }
        executeRCommand(dataFrameName + "<-xp$appendSelections("+dataFrameName+","+selections+", column.name='"+columnName+"', default.name='"+defaultAnnotation+"')", false);
    }
}
