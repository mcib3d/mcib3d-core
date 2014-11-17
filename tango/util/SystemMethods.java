/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tango.util;

import ij.IJ;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

/**
 *
 * @author julien
 */
public class SystemMethods {
    
    public static void configureR(){
        if(!checkR()){
            if(ij.IJ.isWindows()){
                if(checkEnvironmentVar("ProgramFiles","R")){
                    setREnv(null);
                }else{
                    askAboutR();
                }
            }else{
                askAboutR();
            }
        }else{
            ij.IJ.log("R is Installed and running! Congratulations!");
        }
    }
    
    public static void configureMongoDB(){
        int n = checkMongoDB();
        if(n==3){
             ij.IJ.log("MongoDB is Installed and running! Congratulations!");
        }else{
            askAboutMongoDB();
        }
    }
    
    public static String getRPath(){
        String folderPath = IJ.getDirectory("startup")+File.separator+"lib"+File.separator+"R";
        return folderPath;
    }
    
    public static String getBatchPath(){
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
    
    public static String getImageJPath(){
        try {
            File pluginDir = new File(IJ.getDirectory("plugins"));
            return pluginDir.getParentFile().getCanonicalPath();
        } catch (IOException ex) {
            return null;
        }
    }
    
    public static boolean execProcess(File directory, ArrayList<String> commandArgs, Boolean isAdmin) {
        ArrayList<String> processArgs= new ArrayList<String>();
        if(IJ.isWindows()){
            if(isAdmin){
                processArgs.add("cmd.exe");
                processArgs.add("/c");
                processArgs.add("start");
                processArgs.add("runas");
                processArgs.add("/user:"+System.getenv("COMPUTERNAME")+"\\"+System.getProperty("user.name"));
                processArgs.add("\"");
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
        final ResultObject r = new ResultObject(false);
        Thread t = new Thread(new SystemTask(r, pb));
        t.start();
        try {
            t.join();
        } catch (InterruptedException ex) {
            Logger.getLogger(SystemMethods.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        }
        return r.getResult();
    }
    
    public static boolean executeBatchScript(String scriptName, boolean isAdmin){
        String ext = "";
        if(ij.IJ.isWindows()) ext = ".bat";
        if(ij.IJ.isLinux()) ext = ".sh";
        if(ij.IJ.isMacOSX()) ext = ".command";
        File d = new File(getBatchPath());
        ArrayList<String> commandArgs = new ArrayList<String>();
        commandArgs.add(scriptName+ext);
        return execProcess(d, commandArgs, isAdmin);
    }
    
    public static boolean executeBatchScriptWithParameters(String scriptName, ArrayList<String> scriptArgs, boolean isAdmin){
        String ext = "";
        if(ij.IJ.isWindows()) ext = ".bat";
        if(ij.IJ.isLinux()) ext = ".sh";
        if(ij.IJ.isMacOSX()) ext = ".command";
        File d = new File(getBatchPath());
        ArrayList<String> commandArgs = new ArrayList<String>();
        commandArgs.add(scriptName+ext);
        commandArgs.addAll(scriptArgs);
        return execProcess(d, commandArgs, isAdmin);
    }
    
    public static boolean executeRScript(String script, Boolean isAdmin){
        File d = new File(getRPath());
        ArrayList<String> commandArgs = new ArrayList<String>();
        commandArgs.add("Rscript");
        commandArgs.add(script+".r");
        return execProcess(d, commandArgs, isAdmin);
    }
    
    public static boolean checkR(){
        File d = new File(getRPath());
        ArrayList<String> commandArgs = new ArrayList<String>();
        commandArgs.add("R");
        commandArgs.add("--version");
        return execProcess(d, commandArgs,false);
        }
    
    public static int checkMongoDB(){
        File d = new File(getBatchPath());
        int k = 0;
        String[] bins = {"mongod","mongodump","mongorestore"};
        for(int i=0;i<3;i++){
            ArrayList<String> commandArgs = new ArrayList<String>();
            commandArgs.add(bins[i]);
            commandArgs.add("--version");
            if(execProcess(d, commandArgs,false)) k = k+1;
        }
        return k;
        }
    
    public static boolean checkChocolatey(){
        File d = new File(getBatchPath());
        ArrayList<String> commandArgs = new ArrayList<String>();
        commandArgs.add("choco");
        commandArgs.add("--version");
        return execProcess(d, commandArgs,false);
    }
    
    public static boolean checkHomebrew(){
        File d = new File(getBatchPath());
        ArrayList<String> commandArgs = new ArrayList<String>();
        commandArgs.add("brew");
        commandArgs.add("--version");
        return execProcess(d, commandArgs,false);
    }
    
    public static boolean checkEnvironmentVar(String key, String subFolderName){
            String v = System.getenv(key);
            if(v==null) return false;
            if(subFolderName != null && subFolderName != "") v = v+File.pathSeparator+subFolderName;
                File d = new File(v);
                return d.isDirectory();
            }
    
    public static boolean setEnvironmentVar(String key, String value){
        String scriptName = "setEnvVarFromKeyAndValue";
        ArrayList<String> scriptArgs = new ArrayList<String>();
        scriptArgs.add(key);
        scriptArgs.add(value);
        return executeBatchScriptWithParameters(scriptName, scriptArgs, true);
    }
    
    public static boolean addDirectoryToPath(String directoryName){
        String sep = "";
        if(ij.IJ.isWindows()) sep = ";";
        if(ij.IJ.isLinux()) sep = ":";
        if(ij.IJ.isMacOSX()) sep = ":";
        String oldPath = System.getenv("PATH");
        String newPath = oldPath+sep+directoryName;
        return setEnvironmentVar("PATH", newPath);
    }

    public static void askAboutR() {
        int n = JOptionPane.showConfirmDialog(null, "Do you want to install R?","TANGO INSTALLER", JOptionPane.YES_NO_OPTION);
        if(n==0) {
            installR();
            setREnv(null);
        }
        else locateR();
    }

    public static void askAboutMongoDB() {
        int n = JOptionPane.showConfirmDialog(null, "Do you want to install MongoDB?","TANGO INSTALLER", JOptionPane.YES_NO_OPTION);
        if(n==0){
            installMongoDB();
            setMongoDBEnv(null, null);
        }
        else locateMongoDB();
    }
    
    public static int askAboutChocolatey(){
        return JOptionPane.showConfirmDialog(null, "Do you want to install Chocolatey?","TANGO INSTALLER", JOptionPane.YES_NO_OPTION);
    }
    
    public static int askAboutHomebrew(){
        return JOptionPane.showConfirmDialog(null, "Do you want to install Homebrew?","TANGO INSTALLER", JOptionPane.YES_NO_OPTION);
    }
    
    public static boolean installR() {
        return installSoftware("R");
    }
    
    public static boolean setREnv(String path) {
        boolean r;
        if(path==null){
            r = executeBatchScript("setREnv", false);
        }else{
            boolean a = setEnvironmentVar("R_HOME", path);
            String binpath = "";
            if(ij.IJ.isWindows()) binpath = path+File.separator+"bin"+File.separator+"x64";
            if(ij.IJ.isLinux()) binpath = path+File.separator+"bin";
            if(ij.IJ.isMacOSX()) binpath = path+File.separator+"bin";
            boolean b = addDirectoryToPath(binpath);
            r = (a && b);
        }
        return r;
    }

    public static boolean setMongoDBEnv(String binpath, String dbpath) {
        boolean r;
        if(binpath==null || dbpath==null){
            r = executeBatchScript("setMongoDBEnv", false);
        }else{
            boolean a = setEnvironmentVar("MONGODB_BIN", binpath);
            boolean b = setEnvironmentVar("MONGODB_DATA", dbpath);
            boolean c = addDirectoryToPath(binpath);
            r = (a && b && c);
        }
        return r;
    }

    public static boolean installRTango(){
        return executeRScript("installRTango", false);
    }

    public static String locateFile(String title) {
        final JFileChooser fc = new JFileChooser(title);
        fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        int returnval = fc.showOpenDialog(null);
        String p = null;
        if (returnval == JFileChooser.APPROVE_OPTION) {
            p = fc.getSelectedFile().getAbsolutePath();
        }
        return p;
    }
    
    public static boolean locateR(){
        int n = JOptionPane.showConfirmDialog(null, "Have you already installed R? If you did, you're gonna be asked to locate the R folder.","TANGO INSTALLER", JOptionPane.YES_NO_OPTION);
        String p = "";
        if(n==0) p = locateFile("Browse to R folder");
        return setREnv(p);
    }

    public static boolean installMongoDB() {
        if(ij.IJ.isWindows()){
            boolean a = installSoftware("MongoDB");
            if(a){
                boolean b = executeBatchScript("configureMongoDB" ,false);
                boolean c = executeBatchScript("installMongoDBAsAService" ,false);
                return b && c;
            }else{
                return false;
            }
        }else{
                return installSoftware("MongoDB");
        }
    }
    
    public static boolean installSoftware(String softwareName){ 
        boolean isAdmin = false;
        boolean installSoftware = false;
        if(ij.IJ.isLinux()){
            isAdmin = true;
            installSoftware = true;
        }
        if(ij.IJ.isWindows()){
            if(!checkChocolatey()){
                int n = askAboutChocolatey();
                if(n==0){
                    if(installChocolatey()) ij.IJ.showMessage("Please restart Fiji and re-run the install command for "+softwareName);
                    installSoftware = false;
                }else{
                    ij.IJ.showMessage("Can't install software, package manager Chocolatey is missing");
                    installSoftware = false;
                }
            }else installSoftware = true;
        }
        if(ij.IJ.isMacOSX()){
            if(!checkHomebrew()){
                int n = askAboutHomebrew();
                if(n==0){
                    if(installHomebrew()) ij.IJ.showMessage("Please restart Fiji and re-run the install command");
                    installSoftware = false;
                }else{
                    ij.IJ.showMessage("Can't install software, package manager Homebrew is missing");
                    installSoftware = false;
                }
            }else installSoftware = true;
        }
        if(!installSoftware) return false;
        else return executeBatchScript("install"+softwareName, isAdmin);
    }
    
    public static boolean installChocolatey() {
        return executeBatchScript("installChocolatey", false);
    }
    
    public static boolean installHomebrew() {
        return executeBatchScript("installHomebrew", false);
    }

    public static boolean locateMongoDB() {
        int n = JOptionPane.showConfirmDialog(null, "Have you already installed R? If you did, you're gonna be asked to locate the R folder.","TANGO INSTALLER", JOptionPane.YES_NO_OPTION);
        String bp = "";
        String dp = "";
        if(n==0){
            bp = locateFile("Browse to MongoDB bin folder");
            dp = locateFile("Browse to MongoDB data folder");
        }
        return setMongoDBEnv(bp,dp);
    }
}
