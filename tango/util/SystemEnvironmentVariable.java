package tango.util;
import ij.IJ;
import ij.Prefs;
import java.io.File;
import java.util.ArrayList;
import static tango.util.SystemMethods.executeBatchScriptWithParameters;


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
    public String key;
    public String value;
    
    public SystemEnvironmentVariable(String key, String value){
        this.key = key;
        if(value == null){
            this.read();
        }else{
            this.value = value;
        }
        if(value == null) IJ.log("No "+key+" Environment Variable has been found");
        else IJ.log("Environment Variable "+key+" has value "+value);
    }
    
    public String getPrefsKey(){
        return prefix + "_" + key + ".String";
    }

    public boolean read() {
        String v = readFromPrefs();
        if(v == null){
            v = readFromSystem();
            if(v == null){
                v = readFromEnv();
                if(v == null){
                    this.value = null;
                    return false;
                }
            }else{
                this.value = v;
                return true;
            }
        }else{
            this.value = v;
            return true;
        }
        return false;
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
            String v = null;
            if(value==null) return false;
            else{
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

    String getPath(String command) {
        return value+File.separator+command;
    }

    File getDirectory() {
        if(value!=null) return new File(value);
        else return null;
        
    }
}
