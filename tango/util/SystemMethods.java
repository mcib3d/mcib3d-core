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
import static tango.util.SystemMethods.executeBatchScript;
import static tango.util.SystemMethods.executeRScript;

/**
 *
 * @author julien
 */
public class SystemMethods {
    
    public static void configureR(){
        if(!checkR()) askAboutR();
        else IJ.log("R is Installed! Congratulations!");
    }
    
    public static void configureMongoDB(){
        int n = checkMongoDB();
        if(n==3){
             ij.IJ.log("MongoDB is Installed! Congratulations!");
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
        if(IJ.is64Bit()){
            arch = "64";
        }
        if(IJ.isWindows()) platform = "Win";
        if(IJ.isLinux()) platform = "Linux";
        if(IJ.isMacOSX()) platform = "Mac";
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
    
    public static boolean execProcess(File directory, ArrayList<String> commandArgs) {
        ArrayList<String> processArgs= new ArrayList<String>();
        if(IJ.isWindows()){
            processArgs.add("cmd.exe");
            processArgs.add("/c");
            processArgs.addAll(commandArgs);
        }else{
            processArgs = commandArgs;
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
    
    public static void chmodBatchScript(String scriptName, String ext, File directory){
        File f = new File(directory.getAbsolutePath()+File.separator+scriptName+ext);
        f.setExecutable(true);
    }
    
    public static boolean executeBatchScript(String scriptName, boolean interact, File directory){
        String ext = "";
        String prefix = "";
        if(IJ.isWindows()){
            ext = ".bat";
        }
        if(IJ.isLinux()){
            ext = ".sh";
            prefix = "./";
        }
        if(IJ.isMacOSX()){
            ext = ".command";
            prefix = "./";
        }
        if(directory==null) directory = new File(getBatchPath());
        ArrayList<String> commandArgs = new ArrayList<String>();
        if(IJ.isWindows()){
            commandArgs.add("start");
        }
        if(IJ.isLinux()){
            commandArgs.add("xterm");
            commandArgs.add("-e");
        }
        if(IJ.isMacOSX()){
            commandArgs.add("open");
            commandArgs.add("-a");
            commandArgs.add("Terminal");
            prefix = "./";
        }
        commandArgs.add(prefix+scriptName+ext);
        chmodBatchScript(scriptName, ext, directory);
        return execProcess(directory, commandArgs);
    }
    
    public static boolean executeInteractiveCommandInDirectory(File directory, String command){
        ArrayList<String> commandArgs = new ArrayList<String>();
        commandArgs.add(directory.getAbsolutePath());
        commandArgs.add(command);
        return executeBatchScriptWithParameters("runTerminal" ,commandArgs , null);
    }
    
    public static boolean executeBatchScriptWithParameters(String scriptName, ArrayList<String> scriptArgs, File directory){
        String ext = "";
        String prefix = "";
        if(IJ.isWindows()){
            ext = ".bat";
        }
        if(IJ.isLinux()){
            ext = ".sh";
            prefix = "./";
        }
        if(IJ.isMacOSX()){
            ext = ".command";
            prefix = "./";
        }
        if(directory==null) directory = new File(getBatchPath());
        ArrayList<String> commandArgs = new ArrayList<String>();
        commandArgs.add(prefix+scriptName+ext);
        commandArgs.addAll(scriptArgs);
        chmodBatchScript(scriptName, ext, directory);
        return execProcess(directory, commandArgs);
    }
    
    
    
    public static boolean executeRScript(String script){
        File d = new File(getRPath());
        ArrayList<String> commandArgs = new ArrayList<String>();
        commandArgs.add("Rscript");
        commandArgs.add(script+".r");
        return execProcess(d, commandArgs);
    }
    
    
    
    public static boolean checkR(){
        String path=null;
        if(IJ.isWindows()){
            path = null;
        }
        if(IJ.isLinux()){
            path = "/usr/bin";
        }
        if(IJ.isMacOSX()){
            path = "/usr/local/bin";
        }
        SystemEnvironmentVariable rBin = new SystemEnvironmentVariable("R_BIN", path, true, true);
        String command = "R";
        return rBin.getVersion(command);
        }
    
    public static int checkMongoDB(){
        SystemEnvironmentVariable mongoBinPath = new SystemEnvironmentVariable("mongoBinPath", null, true, true);
        int k = 0;
        String[] bins = {"mongod","mongodump","mongorestore"};
        for(int i=0;i<3;i++){
            if(mongoBinPath.getVersion(bins[i])) k = k+1;;
        }
        return k;
        }
    
    public static boolean checkChocolatey(){
        SystemEnvironmentVariable chocolateyBinPath = new SystemEnvironmentVariable("ChocolateyInstall", null, true, true);
        return chocolateyBinPath.exists();
    }
    
    public static boolean checkHomebrew(){
        SystemEnvironmentVariable homeBrewBinPath = new SystemEnvironmentVariable("homeBrewBinPath", null, true, true);
        return homeBrewBinPath.exists();
    }

    public static void askAboutR() {
        int n = JOptionPane.showConfirmDialog(null, "Do you want to install R?","TANGO INSTALLER", JOptionPane.YES_NO_OPTION);
        if(n==0) {
            boolean a = installR();
            if(a) setREnv(null);
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
        return JOptionPane.showConfirmDialog(null, "Do you want to install Chocolatey?Otherwise you will have to install system dependencies on your own.","TANGO INSTALLER", JOptionPane.YES_NO_OPTION);
    }
    
    public static int askAboutHomebrew(){
        return JOptionPane.showConfirmDialog(null, "Do you want to install Homebrew?Otherwise you will have to install system dependencies on your own.","TANGO INSTALLER", JOptionPane.YES_NO_OPTION);
    }
    
    public static boolean installR() {
        return installSoftware("R", true);
    }
    
    public static boolean installMongoDB() {
        boolean a = installSoftware("MongoDB", true);
        if(a){
            boolean b = executeBatchScript("configureMongoDB" , true, null);
            boolean c = executeBatchScript("installMongoDBAsAService" , true, null);
            return b && c;
        }else{
            return false;
        }
    }
    
    public static boolean installSoftware(String softwareName, boolean interact){ 
        boolean isAdmin = false;
        boolean installManager = false;
        if(IJ.isLinux()){
            isAdmin = true;
            installManager = true;
        }
        if(IJ.isWindows()){
            if(!checkChocolatey()){
                int n = askAboutChocolatey();
                if(n==0){
                    if(installChocolatey()) ij.IJ.showMessage("Please restart Fiji and re-run the install command for "+softwareName);
                    installManager = false;
                }else{
                    ij.IJ.showMessage("Can't install software, package manager Chocolatey is missing");
                    installManager = false;
                }
            }else installManager = true;
        }
        if(IJ.isMacOSX()){
            if(!checkHomebrew()){
                int n = askAboutHomebrew();
                if(n==0){
                    if(installHomebrew()) ij.IJ.showMessage("Please restart Fiji and re-run the install command");
                    installManager = false;
                }else{
                    ij.IJ.showMessage("Can't install software, package manager Homebrew is missing");
                    installManager = false;
                }
            }else installManager = true;
        }
        if(!installManager) return false;
        else return executeBatchScript("install"+softwareName, interact, null);
    }
    
    public static boolean installChocolatey() {
        return executeBatchScript("installChocolatey", true, null);
    }
    
    public static boolean installHomebrew() {
        return executeBatchScript("installHomebrew", true, null);
    }
    
    public static boolean installRTango(){
        return executeRScript("installRTango");
    }
    
    public static boolean setREnv(String rhome) {
        boolean a = true;
        if(rhome==null){
            a = executeBatchScript("setREnv", false, null);
        }
        SystemEnvironmentVariable rHome = new SystemEnvironmentVariable("R_HOME", rhome, true, true);
        String binpath = null;
        File rhd = rHome.getDirectory();
        if(rhd!=null){
            if(IJ.isWindows()) binpath = rhd.getAbsolutePath()+File.separator+"bin"+File.separator+"x64";
            else binpath = rhd.getAbsolutePath()+File.separator+"bin";
            SystemEnvironmentVariable rBin = new SystemEnvironmentVariable("R_BIN", binpath, true, true);
            return a && rHome.exists() && rBin.exists();
        }
        else{
            warnAboutR();
            return false;
        }
    }

    public static boolean setMongoDBEnv(String binpath, String confpath) {
        boolean a = true;
        if(binpath==null || confpath==null){
            a = executeBatchScript("setMongoDBEnv", false, null);
        }
        SystemEnvironmentVariable mongoBinPath = new SystemEnvironmentVariable("mongoBinPath", binpath, true, true);
        SystemEnvironmentVariable mongoConfPath = new SystemEnvironmentVariable("mongoConfPath", confpath, true, true);
        return a && mongoBinPath.exists() && mongoConfPath.exists();
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
    
    public static boolean locateMongoDB() {
        int n = JOptionPane.showConfirmDialog(null, "Have you already installed MongoDB? If you did, you're gonna be asked to locate the MongoDB bin folder and the the MongoDB conf file.","TANGO INSTALLER", JOptionPane.YES_NO_OPTION);
        String bp = "";
        String dp = "";
        if(n==0){
            bp = locateFile("Browse to MongoDB bin folder");
            dp = locateFile("Browse to MongoDB conf file");
        }
        return setMongoDBEnv(bp,dp);
    }

    private static void warnAboutR() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}
