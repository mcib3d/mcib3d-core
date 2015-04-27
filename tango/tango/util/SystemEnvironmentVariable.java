package tango.util;
import ij.IJ;
import ij.Prefs;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Scanner;
import static tango.util.SystemMethods.execProcess;
import static tango.util.SystemMethods.setInteractiveEnvironmentVar;
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
    public boolean real = false;
    public boolean writeToSystem;
    public String key;
    public String value;
    public ArrayList<String> paths = new ArrayList<String>();
    public ArrayList<String> bins = new ArrayList<String>();
    public boolean isBinDirectory;
    
    public SystemEnvironmentVariable(String key, String value, boolean check, boolean writeToSystem, boolean isBinDirectory){
        this.key = key;
        this.value = value;
        this.writeToSystem = writeToSystem;
        this.isBinDirectory = isBinDirectory;
        this.read(check);
    }
    
    public boolean exists(){
        return this.real;
    }
    
    public String getValue(){
        return this.value;
    }
    
    public void setValue(String value) {
        this.value=value;
    }
    
    public String getPrefsKey(){
        return prefix + "_" + key + ".String";
    }
    
    public void parseCsvLine(String line){
        String[] lineElements = line.split(";");
        String k = lineElements[0];
        String p = lineElements[1];
        String b = lineElements[2];
        if(k.equals(key)){
            String[] usualPaths = p.split(":");
            for(String usualPath : usualPaths){
                IJ.log(usualPath+" has been added to possible locations for "+key);
                paths.add(usualPath);
            }
            String[] usualBins = b.split(":");
            for(String bin : usualBins){
                IJ.log(bin+" has been added to sub elements to check for "+key);
                bins.add(bin);
            }
        }
    }
    
    public void scanPathsAndBins(){
        IJ.log("Parsing CSV file "+getBatchPath()+File.separator+"SystemEnvironmentVariables.csv");
        try {
            Scanner scanner = new Scanner(new File(getBatchPath()+File.separator+"SystemEnvironmentVariables.csv"));
            scanner.useDelimiter(System.getProperty("line.separator"));
            while (scanner.hasNext())
            {
                String line = scanner.next();
                parseCsvLine(line);
            }
            scanner.close();
        } catch (FileNotFoundException ex) {
            paths = null;
            bins = null;
        }
    }
    
    public void checkSubElements(String path){
        boolean p = true;
        String firstValue = value;
        boolean firstRealValue = real;
        if(path!=null){
            if(IJ.isWindows()){
                if(value!="") value = System.getenv("SystemDrive")+File.separator+path;
            }
            else value = path;
        }
        if(value==null) real=false;
        else{
            IJ.log("Moving to "+value+" Directory:");
            for(String bin : bins){
                boolean a = check(bin);
                if(a) IJ.log(value+" Directory has an element "+bin);
                //else break;
                p = p & a;
            }
            if(p){
                IJ.log(value+" Directory has all elements");
                real = true;
            }else{
                IJ.log(value+" Directory does not have all elements");
                value = firstValue;
                real = firstRealValue;
            }
        }
    }
    
    public void extractValueFromPathsAndBins(){
        scanPathsAndBins();
        if(paths == null || bins == null) real=false;
        else{
            checkSubElements(null); //On vérifie l'ancienne valeur
            if(!real) checkSubElements("");//On vérifie si le path fonctionne
            if(!real){
                for(String path : paths){
                    checkSubElements(path);
                    if(real) break;
                }
            }
        }
    }

    public final void read(boolean check) {
        if(value==null) value = readFromPrefs();
        if(writeToSystem){
            if(value==null) value = readFromSystem();
            if(value==null) value = readFromEnv();
        }
        if(check){
            if(isBinDirectory){
                IJ.log("Checking sub elements of Directory "+key);
                if (value!=null) checkSubElements(null);
                if (!real) extractValueFromPathsAndBins();
                if(!real){
                    this.value = null;
                }
            }else{
                if(value==null) real = false;
                else real = true;
            }
            if (real) write();
        } else{
            if(value==null) real = false;
            else real = true;
        }
        if(!real) IJ.log(key+" environment variable is not consistent.");
    }
    
    public boolean write(){
        if(value != null){
            writeToPrefs();
            if(writeToSystem){
                IJ.log("Environment variable "+key+" will be set to value "+value);
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
        return setInteractiveEnvironmentVar(key, value);
    }
    
    public boolean check(String subElementName){
        IJ.log("Checking if "+value+" path has an element "+subElementName); // commentaire de jeannot si subElementName==null ça bug pas?
        if(value==null) return false;
        if("".equals(value)) return getVersion(subElementName); // commentaire de jeannot: ne pas executer si subElementName == null ou =="" 
        else{
            String v = null;
            if(subElementName != null && !"".equals(subElementName)) v = value+File.separator+subElementName;
            else v = value;
            File d = new File(v);
            IJ.log("Checking if "+d.getAbsolutePath()+" exists");
            return d.isDirectory() || d.isFile() ; // commentaire de jeannot: d.exists() ? 
        }
    }

    public String getCommand(String command) {
        if(value!=null && !"".equals(value)) {
           if(IJ.isWindows()) return command;
           else return "./"+command;
        }else return command;
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
    
    public boolean getVersion(String command){
        if(value=="") IJ.log("Path is empty");
        IJ.log("Retrieving "+command+" version");
        ArrayList<String> commandArgs = new ArrayList<String>();
        commandArgs.add("--version");
        return executeProcess(command, commandArgs);
    }

    private void delete() {
        value = null;
    }
}
