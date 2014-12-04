package tango.util;
import ij.IJ;
import ij.Prefs;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Scanner;
import static tango.util.SystemMethods.execProcess;
import static tango.util.SystemMethods.executeBatchScriptWithParameters;
import static tango.util.SystemMethods.executeInteractiveCommandInDirectory;
import static tango.util.SystemMethods.getBatchPath;


/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * @author julien
 */
public class SystemEnvironmentVariable {
    public final static String prefix = "tango";
    public boolean realValue = false;
    public boolean writeToSystem;
    public String key;
    public String value;
    public ArrayList<String> paths;
    public ArrayList<String> bins;
    private boolean isDirectory;
    
    public SystemEnvironmentVariable(String key, String value, boolean check, boolean writeToSystem){
        this.key = key;
        this.value = value;
        this.writeToSystem = writeToSystem;
        if(check){
            boolean a = this.read();
            if(!a) IJ.log(key+" environment variable has no real value.");
            else realValue = true;
        }
    }
    
    public boolean exists(){
        return this.realValue;
    }
    
    public String getValue(){
        return this.value;
    }
    
    public String getPrefsKey(){
        return prefix + "_" + key + ".String";
    }

    public final boolean read() {
        if(isDirectory){
            scanPathsAndBins();
            boolean b = extractRealValue();
            if(!b){
                this.value = null;
                this.realValue = false;
            }else{
                this.realValue = true;
            }
        }else{
            if(value==null) value = readFromPrefs();
            if(writeToSystem){
                if(value==null) value = readFromSystem();
                if(value==null) value = readFromEnv();
            }
            if(value==null) this.realValue = false;
        }
        return this.realValue;
    }
    
    public boolean write(boolean toSystem){
        if(value != null){
            IJ.log("Environment variable "+key+" will be set to value "+value);
            writeToPrefs();
            if(toSystem){
                writeToSystem();
                return writeToEnv();
            }
        }
        return true;
    }
    
    public void addToEnv(ProcessBuilder pb){
        if(value != null){
            pb.environment().put(key, value);
        }
    }

    public String readFromPrefs() {
        return Prefs.get(getPrefsKey(), null);
    }
    
    public void writeToPrefs() {
        Prefs.set(getPrefsKey(), value);
    }

    private void writeToSystem() {
        System.setProperty(key, value);
    }

    private String readFromSystem() {
        return System.getProperty(key);
    }
    
    private String readFromEnv(){
        return System.getenv(key);
    }
    
    private boolean writeToEnv(){
        return setEnvironmentVar(key, value);
    }
    
    public boolean check(String subElementName){
        if(value==null || "".equals(subElementName)) return getVersion(subElementName);
        else{
            String v = null;
            if(subElementName != null && !"".equals(subElementName)) v = v+File.pathSeparator+subElementName;
            else v = value;
            File d = new File(v);
            return d.isDirectory() || d.isFile();
        }
    }
    
    public static boolean setEnvironmentVar(String key, String value){
        String scriptName = "setEnvVarFromKeyAndValue";
        boolean interact = true;
        ArrayList<String> scriptArgs = new ArrayList<String>();
        scriptArgs.add(key);
        scriptArgs.add(value);
        if(IJ.isWindows()) interact = false;
        else scriptArgs.add("--system-wide");
        return executeBatchScriptWithParameters(scriptName, scriptArgs, null);
    }

    public String getCommand(String command) {
        if(!check(command)) return command;
        else{
            if(!IJ.isWindows()) return command;
            else return "./"+command;
        }
    }

    public File getDirectory() {
        if(value!=null && !"".equals(value) && new File(value).exists()) return new File(value);
        else return null;
    }
    
    public boolean executeProcess(String command, ArrayList<String> commandArgs){
        ArrayList<String> allArgs = new ArrayList<String>();
        allArgs.add(getCommand(command));
        if(commandArgs!=null) allArgs.addAll(commandArgs);
        return execProcess(getDirectory(), allArgs);
    }
    
    public boolean executeInteractiveProcess(String command){
        return executeInteractiveCommandInDirectory(getDirectory() ,getCommand(command));
    }
    
    public void scanPathsAndBins(){
        ArrayList<String> allPaths = new ArrayList<String>();
        String v1 = readFromPrefs();
        if(v1!=null) allPaths.add(v1);
        String v2 = readFromSystem();
        if(v2!=null) allPaths.add(v2);
        String v3 = readFromEnv();
        if(v3!=null) allPaths.add(v3);
        try {
            Scanner scanner = new Scanner(new File(getBatchPath()+File.separator+"SystemEnvironmentVariables.csv"));
            scanner.useDelimiter(";");
            while (scanner.hasNext())
            {
                if(scanner.next().equals(key)){
                    String[] usualPaths = scanner.next().split(":");
                    for(String usualPath : usualPaths){
                        allPaths.add(usualPath);
                    }
                    paths = allPaths;
                    for(String bin : scanner.next().split(":")){
                        bins.add(bin);
                    }
                }
            }
            scanner.close();
        } catch (FileNotFoundException ex) {
            paths = null;
            bins = null;
        }
    }
    
    public boolean extractValueFromPathsAndBins(){
        if(paths == null || bins == null) return false;
        else{
            for(String path : paths){
                boolean p = true;
                if(IJ.isWindows()) value = System.getenv("SystemDrive")+File.separator+path;
                else value = path;
                for(String bin : bins){
                    p = p & check(bin);
                }
                if(p){
                    return true;
                }
            }
        }
        return false;
    }
    
    public final boolean extractRealValue(){
        if(value!=null){
            boolean v = true;
            for(String bin : bins){
                v = v & check(bin);
            }
            if(v) return true;
            else{
                return extractValueFromPathsAndBins();
            }
        }else{
            return extractValueFromPathsAndBins();
        }
    }
    
    public boolean getVersion(String command){
        ArrayList<String> commandArgs = new ArrayList<String>();
        commandArgs.add("--version");
        return executeProcess(command, commandArgs);
    }

    private void delete() {
        value = null;
    }
}
