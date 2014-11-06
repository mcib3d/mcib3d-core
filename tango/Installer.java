/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package tango;

import ij.IJ;
import ij.plugin.PlugIn;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.SwingUtilities;
import tango.gui.Core;
/**
 *
 * @author julien
 */
public class Installer implements PlugIn {
    
    public void run(String string) {
        boolean install = false;
        Core.GUIMode=false;
        if(!checkR()){
            install = true;
        }
        if(!this.checkMongoDb()){
            install = true;
        }
        if(install) {
            installDependencies();
    }
        installRTango();
    }
    
    public String getRPath(){
        String folderPath = IJ.getDirectory("startup")+File.separator+"lib"+File.separator+"R";
        return folderPath;
    }
    
    public String getBatchPath(){
        String platform = "";
        String arch = "32";
        if(ij.IJ.is64Bit()){
            arch = "64";
    }
        if(ij.IJ.isWindows()) platform = "Win";
        if(ij.IJ.isLinux()) platform = "Linux";
        if(ij.IJ.isMacOSX()) platform = "MacOSX";
        return IJ.getDirectory("startup")+File.separator+"lib"+File.separator+platform+arch;
    }
    
    public String getImageJPath(){
        try {
            File pluginDir = new File(IJ.getDirectory("plugins"));
            return pluginDir.getParentFile().getCanonicalPath();
        } catch (IOException ex) {
            Logger.getLogger(Installer.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
    }
    public static void execProcess(File directory, String[] commandArgs, Boolean isAdmin) {
        execProcess(directory, new ArrayList<String>(Arrays.asList(commandArgs)), isAdmin);
    }
    public static void execProcess(File directory, ArrayList<String> commandArgs, Boolean isAdmin) {
        ArrayList<String> processArgs= new ArrayList<String>();
        if(IJ.isWindows()){
            if(isAdmin){
                processArgs.add("runas");
                processArgs.add("/profile");
                processArgs.add("/user:Administrator");
                processArgs.add("\"");
                processArgs.add("cmd.exe");
                processArgs.add("/c");
                processArgs.addAll(commandArgs);
                processArgs.add("\"");
        }
            else{
                processArgs.add("cmd.exe");
                processArgs.add("/c");
                processArgs.addAll(commandArgs);
        }
        }
        if(IJ.isMacOSX()){
            if(isAdmin){
                processArgs.add("open");
                processArgs.add("-a");
                processArgs.add("Terminal");
                processArgs.add("`sudo ");
                processArgs.addAll(commandArgs);
                processArgs.add("`");
                }
            else processArgs = commandArgs;
            }
        if(IJ.isLinux()){
            if(isAdmin){
                processArgs.add("sudo");
                processArgs.add("xterm");
                processArgs.add("-e");
                processArgs.addAll(commandArgs);
            }else{
                processArgs = commandArgs;
            }
        }
        final ProcessBuilder pb = new ProcessBuilder(processArgs);
        pb.directory(directory);
        if(directory.isDirectory()) IJ.log("Directory exists");
        IJ.log("Executing command : "+pb.command());
        IJ.log("In working directory : "+pb.directory().getAbsolutePath());
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    final Process p = pb.start();
                    try {
                        p.waitFor();
                    } catch (InterruptedException ex) {
                        Logger.getLogger(Installer.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    OutputStream out = p.getOutputStream();
                    out.close();
                    BufferedReader in = new BufferedReader(new InputStreamReader(p.getInputStream()));
                    String line = null;
                    while ((line = in.readLine()) != null) {
                        IJ.log(line);
                    }
                    line = null;
                    BufferedReader error = new BufferedReader(new InputStreamReader(p.getErrorStream()));
                    while ((line = error.readLine()) != null) {
                        IJ.log(line);
                    }
                } catch (IOException ex) {
                    Logger.getLogger(Installer.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        });
        SwingUtilities.invokeLater(t);
    }
    
    public void executeRScript(String script, Boolean isAdmin){
        File d = new File(getRPath());
        ArrayList<String> commandArgs = new ArrayList<String>();
                commandArgs.add("Rscript");
                commandArgs.add(script+".r");
        execProcess(d, commandArgs, isAdmin);
    }
    
    public Boolean checkR(){
        File d = new File(getRPath());
        ArrayList<String> commandArgs = new ArrayList<String>();
        commandArgs.add("R");
        commandArgs.add("--version");
        execProcess(d, commandArgs,false);
            return true;
        }
    
    public Boolean checkMongoDb(){
        File d = new File(getBatchPath());
        ArrayList<String> commandArgs = new ArrayList<String>();
        commandArgs.add("mongo");
        commandArgs.add("--version");
        execProcess(d, commandArgs,false);
            return true;
        }
    
    public void installDependencies(){
        String ext = "";
        if(ij.IJ.isWindows()) ext = ".bat";
        if(ij.IJ.isLinux()) ext = ".sh";
        if(ij.IJ.isMacOSX()) ext = ".command";
        File d = new File(getBatchPath());
        ArrayList<String> commandArgs = new ArrayList<String>();
        commandArgs.add("installTangoDependencies"+ext);
        execProcess(d, commandArgs, true);
    }
    
    public void installRTango(){
        this.executeRScript("installRTango", false);
    }
            }
