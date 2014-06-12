/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package tango;

import ij.Macro;
import ij.Prefs;
import ij.gui.GenericDialog;
import ij.plugin.PlugIn;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import tango.dataStructure.Cell;
import tango.dataStructure.Experiment;
import tango.dataStructure.Field;
import tango.gui.CellManager;
import tango.gui.Core;
import tango.gui.FieldManager;
import tango.gui.XPEditor;
import tango.gui.util.CellFactory;
import tango.gui.util.FieldFactory;
import tango.mongo.MongoConnector;
import tango.parameter.Parameter;
import tango.parameter.SettingsParameter;
import tango.plugin.PluginFactory;
//import tango.util.utils;

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


public class Runner implements PlugIn {
    // call java -cp ij.jar ij.ImageJ -batch tango_runner.ijm
    // content of tango_runner.ijm: run("tangoRunner", getArgument())
    public void run(String argss) {
        Core.GUIMode=false;
        System.out.println("Welcome to TANGO v"+Core.VERSION+" shell");
        PluginFactory.findPlugins();
        String host = prompt("Enter hostname:");
        if ("".equals(host)) host = "localhost";
        Core.mongoConnector = new MongoConnector(host);
        boolean connected = Core.mongoConnector.isConnected();
        if (!connected) {
            String start = prompt("Not connected to DB. Start MongoDB? [Y/N]");
            if (start.equals("Y")) {
                MongoConnector.mongoStart();
                connected = Core.mongoConnector.isConnected();
            }
        }
        if (connected) {
            String user = prompt("Enter username:");
            boolean userSet = Core.mongoConnector.setUser(user, false)!=null;
            if (userSet) {
                while(!parseGeneralCommand()){};
            } else System.out.println("unknown user");
        } else System.out.println("Could'nt connect to db ...");
        
        
        
        //String arg = Macro.getOptions();
        
        //String[] args = arg.split(":");
        
        
        /*if (args.length==0) {
            String s = prompt("enter a command");
            System.out.println("The command was:"+s);
            return;
        }
        if (args[0].equals("test")) {
            System.out.println("test:"+args.length);
            String dbPath = (String) Prefs.get(mongoConnector.getPrefix()+"_dbPath.String", "");
            System.out.println("db path: "+dbPath);
        }
        else if (args[0].equals("isMongoOn")) {
            if (args.length==1) System.out.println("give host name please");
            else {
                boolean b = mongoConnector.isMongoOn(args[1]);
                if (b) System.out.println("mongo is ON!");
                else System.out.println("mongo is OFF..");
            }
        }
        else if (args[0].equals("startMongo")) {
            System.out.println("stating mongo db...");
            mongoConnector.mongoStart();
        }
        else if (args[0].equals("stopMongo")) {
            System.out.println("stopping mongo db...");
            mongoConnector.mongoStop();
        }
        else if (args[0].equals("run")) {
            if (args.length>=6) {
                String host = args[1];
                String user = args[2];
                int nbTasks = (args.length-3)/3;
                String[][] tasks = new String[nbTasks][3];
                for (int curTask = 0; curTask<nbTasks; curTask++) {
                    for (int i = 0; i<3; i++) {
                        tasks[curTask][i]=args[3+curTask*3+i];
                    }
                }
                PluginFactory.findPlugins();
                Core.mongoConnector = new mongoConnector(host);
                if (Core.mongoConnector.isConnected()) {
                    boolean userSet = Core.mongoConnector.setUser(user, false)!=null;
                    if (userSet) {
                        for (int task = 0; task<tasks.length; task++) run(tasks[task][0], tasks[task][1], getCommands(tasks[task][2]));
                    } else System.out.println("unknown user");
                } else System.out.println("Could'nt connect to db .. try startMongo command");
            } else {
                System.out.println("run command arguments: \"run hostname username [project experiment XXXX] \" ");
                System.out.println("Commands: X= T or F. segment nuclei / post process / crop / process cells / measure cells / override measurements");
            }
        }*/
    }
    
    private boolean parseGeneralCommand() {
        String c = prompt("Type \"R\" to run action within TANGO \"EXIT\" to exit shell:");
        if (c.equals("V")) {
            view();
            return true;
        } else if (c.equals("R")) {
            run();
           return false;
        } else if (c.equals("EXIT")) {
            return true;
        } else {
            return false;
        }
    }
    
    private void view() {
        
    }
    
    private void run() {
        ArrayList<Command> commands= new ArrayList<Command>();
        while (!addCommands(commands)){}
        System.out.println("The following commands will be performed:");
        for (Command c: commands) {
            System.out.println(c);
        }
        if (!promptBool("Process?")) return;
        for (Command c : commands) run(c);
    }
    
    private void displayProjects() {
        ArrayList<String> projects  = Core.mongoConnector.getProjects();
        System.out.println("All those projects have been found:");
        //String s = "";
        //for (String p : projects) s=s+p+" / ";
        //System.out.println(s);
        for (String p : projects) System.out.println(p);
    }
    
    private void displayXPs(String project){
        ArrayList<String> xps  = Core.mongoConnector.getExperiments(project);
        System.out.println("All those experiments have been found in the project:"+project);
        //String s = "";
        //for (String p : xps) s=s+p+" / ";
        //System.out.println(s);
        for (String p : xps) System.out.println(p);
    }
    
    private boolean addCommands(ArrayList<Command> commands) {
        displayProjects();
        String folder = prompt("Type project(s) name (separated by a space char):");
        if (folder.equals("EXIT")) return true;
        String[] folders = folder.split(" ");
        if (folders.length==1) {
            if (!projectExists(folder)) {
                System.out.println("Project doesn't exist");
                return false;
            }
            displayXPs(folder);
        }
        String xp = prompt("Type experiment(s) name (separated by a space char):");
        if (xp.equals("EXIT")) return true;
        String[] xps = xp.split(" ");
        if (folders.length>1 && xps.length!=folders.length) {
            System.out.println("There must be as many XPs as projects when entering multiple projects");
            return false;
        }
        boolean crop=false, procN=false, procS=false, meas=false, ovMeas=false;
        String procNS = prompt("Run: \"Process Nuclei\" [Y/N]:");
        if (procNS.equals("Y")) procN=true;
        String cropS = prompt("Run: \"Crop\" [Y/N]:");
        if (cropS.equals("Y")) crop=true;
        String procSS = prompt("Run: \"Process Structures\" [Y/N]:");
        if (procSS.equals("Y")) procS=true;
        String measS = prompt("Run \"Measurements\" [Y/N]:");
        if (measS.equals("Y")) meas=true;
        if (meas) {
            String ovMeasS = prompt("Override Measurements? [Y/N]:");
            if (ovMeasS.equals("Y")) ovMeas=true;
        }
        if (folders.length==xps.length) {
            for (int i = 0; i<xps.length; i++) {
                commands.add(new Command(folders[i], xps[i], procN, crop, procS, meas, ovMeas));
            }
        } else {
            for (int i = 0; i<xps.length; i++) {
                commands.add(new Command(folder, xps[i], procN, crop, procS, meas, ovMeas));
            }
        }
        String other = prompt("Enter other experiments? [Y/N]:");
        if (other.equals("Y")) return false;
        else return true;
    }
    
    private boolean projectExists(String folder) {
        ArrayList<String> projects = Core.mongoConnector.getProjects();
        if (projects.contains(folder)) return true;
        else return false;
    }
    
    public static void run(Command c) {
        System.out.println("Run Command: "+c);
        SettingsParameter.setSettings();
        if (Core.mongoConnector.setProject(c.project)) {
            Core.setXP(new Experiment(c.experiment, Core.mongoConnector));
            if (Core.getExperiment()!=null) {
                for (Parameter p : XPEditor.xpParams) p.dbGet(Core.getExperiment().getData());
                runCommand(c);
            } else System.out.println("unknown experiment");
        } else System.out.println("unknown folder");
    }
    
    public static void runCommand(Command c) {
        Experiment xp = Core.getExperiment();
        String xpName= xp.getName();
        Field[] fields = FieldFactory.getFields(xp);
        // print nb fields ..
        if (c.processNuc || c.crop) {
            FieldManager.processAndCropFields(fields, c.processNuc, c.crop);
        }
        if (c.processStructures || c.measurements) {
            Cell[] cells = CellFactory.getCells(fields);
            System.out.println("Experiment: "+xpName+" nb cells: "+cells.length);
            if (c.processStructures) {
                System.out.println("Experiment: "+xpName+" Process structures...");
                CellManager.processCells(cells, null);
            }
            if (c.measurements) {
                System.out.println("Experiment: "+xpName+" Measurements...");
                CellManager.measureCells(cells, c.overrideMeasurements);
            }
        }
    }
    
    private String prompt(String promptInstruction) {
        if (promptInstruction!=null && promptInstruction.length()>0) System.out.println(promptInstruction);
        System.out.print(">");
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        try {
           return(br.readLine());
        } catch (IOException ioe) {
           System.out.println("IO error trying to read command!");
        }
        return "";
    }
    
    private boolean promptBool(String instruction) {
        String p = prompt(instruction+" [Y/N]:");
        if ("Y".equals(p)) return true;
        else return false;
    }
}
