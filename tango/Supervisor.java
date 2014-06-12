/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package tango;

import ij.IJ;
import ij.plugin.PlugIn;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.SwingUtilities;
import tango.gui.Core;
/**
 *
 * @author julien
 */
public class Supervisor implements PlugIn{
    
    public static String scheme = "http";
    public static String webSite = "biophysique.mnhn.fr";
    public static String page = "tango/downloads";
    public static String downloadSite = "download.tuxfamily.org/tango";
    public static String prefix = "tango";
    public static String mongoBinPath = null;
    
    public void run(String string) {
        Core.GUIMode=false;
        System.out.println("Install RTango");
        createFolder();
        updateFreeJars();
        updateNonFreeJars();
        if(checkR()){
            System.out.println("R already installed");
            downloadRScripts();
            executeRScript("installRTango", false);
        }else{
            IJ.showMessage("R not installed, please install it before running RTango install again");
        }
        if(!this.checkMongoDb()){
            IJ.showMessage("MongoDB is not installed or not running, maybe you should install it");
        }
    }
    
    public void createFolder(){
        File rFolder = new File(getRPath());
        if(!rFolder.isDirectory()) rFolder.mkdir();
        IJ.log(rFolder+" created");
    }
    
    public String getRPath(){
        String folderPath = IJ.getDirectory("plugins")+prefix+"RScripts";
        return folderPath;
    }
    
    public void downloadBatchFiles(){
        String RScriptUrl = "http://"+downloadSite+"/r/RScript.bat";
        String RExeUrl = "http://"+downloadSite+"/r/R.bat";
        String BatToExeConvertorUrl = "http://"+downloadSite+"/r/BatToExeConvertor.exe";
        download(RScriptUrl, getRPath()+File.separator+"RScript.bat", true);
        download(RExeUrl, getRPath()+File.separator+"R.bat", true);
        download(BatToExeConvertorUrl,getRPath()+File.separator+"BatToExeConvertor.bat", true);
    }
    
    public String getImageJPath(){
        try {
            File pluginDir = new File(IJ.getDirectory("plugins"));
            return pluginDir.getParentFile().getCanonicalPath();
        } catch (IOException ex) {
            Logger.getLogger(Supervisor.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
    }
    
    public void downloadLauncherTemplate(){
        String imageJPath = getImageJPath();
        if(IJ.isWindows()){
            String tangoBatUrl = "http://"+downloadSite+"/launchers/tango.bat";
            download(tangoBatUrl, imageJPath+File.separator+"tango.bat", true);
            String tangoCfgUrl = "http://"+downloadSite+"/launchers/ImageJ.cfg";
            download(tangoCfgUrl, imageJPath+File.separator+"ImageJ.cfg", true);
        }
        if(IJ.isMacOSX()){
            String tangoAppUrl = "http://"+downloadSite+"/launchers/tango_app.zip";
            download(tangoAppUrl, imageJPath+File.separator+"tango_app.zip", true);
            String[] commandArgs = {
              "unzip",
              imageJPath+File.separator+"tango_app.zip"
            };
            this.execProcess(new File(getImageJPath()), commandArgs, false);
        }
        if(IJ.isLinux()){
            String tangoShUrl = "http://"+downloadSite+"/r/tango.sh";
            download(tangoShUrl, imageJPath+File.separator+"tango.sh", true);
        }
    }
    
    public void buildFileFromTemplate(File f, String [] fields, String[] values){
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader(f));
            String line = "", oldtext = "", newtext = "";
            try {
                while((line = reader.readLine()) != null)
                {
                    oldtext += line + "\r\n";
                }
            } catch (IOException ex) {
                Logger.getLogger(Supervisor.class.getName()).log(Level.SEVERE, null, ex);
            }
            for(int i=0;i<fields.length;i++){
                newtext = oldtext.replaceAll("$"+fields[i], values[i]);
            }
            try {
                FileWriter writer = new FileWriter(f);
                writer.write(newtext);
                writer.close();
            } catch (IOException ex) {
                Logger.getLogger(Supervisor.class.getName()).log(Level.SEVERE, null, ex);
            }
        } catch (FileNotFoundException ex) {
            Logger.getLogger(Supervisor.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                reader.close();
            } catch (IOException ex) {
                Logger.getLogger(Supervisor.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    
    public void downloadRScripts(){
        String [] rscriptnames = {
            "installRTango.r"
        };
        for(int i=0;i<rscriptnames.length;i++){
            String localPath = getRPath()+File.separator+rscriptnames[i];
            File f = new File(localPath);
            if(!f.exists()) download("http://"+downloadSite+"/r/"+rscriptnames[i], localPath, true);
        }
    }
    
    public void downloadBatchMacro(){
        download("http://"+downloadSite+"/macros/tango_runner.ijm", ij.IJ.getDirectory("macros"), true);
    }
    
    public static void execProcess(File directory, String[] commandArgs, Boolean isAdmin) {
        String[] processArgs=null;
        if(IJ.isWindows()){
            processArgs = new String[commandArgs.length+2];
            if(isAdmin) processArgs[0] = "Cmd.exe";
            else processArgs[0] = "cmd.exe";
            processArgs[1] = "/c";
            for(int i=0;i<commandArgs.length;i++){
                processArgs[i+2] = commandArgs[i];
            }
        }
        if(IJ.isMacOSX()){
            if(isAdmin){
                processArgs = new String[commandArgs.length+4];
                String sudoCommand = "`sudo";
                for(int i=0;i<commandArgs.length;i++){
                    sudoCommand = sudoCommand + " " + commandArgs[i];
                }
                sudoCommand = sudoCommand + "`";
                processArgs[0]="open";
                processArgs[1]="-a";
                processArgs[2]="Terminal";
                processArgs[3]=sudoCommand;
            }else{
                processArgs = commandArgs;
            }
        }
        if(IJ.isLinux()){
            if(isAdmin){
                processArgs = new String[commandArgs.length+3];
                processArgs[0]="sudo";
                processArgs[1]="xterm";
                processArgs[2]="-e";
                for(int i=0;i<commandArgs.length;i++){
                    processArgs[i+3] = commandArgs[i];
                }
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
                        Logger.getLogger(Supervisor.class.getName()).log(Level.SEVERE, null, ex);
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
                    Logger.getLogger(Supervisor.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        });
        SwingUtilities.invokeLater(t);
    }
    
    public void executeRScript(String script, Boolean isAdmin){
        String RScriptPath;
        if(IJ.isWindows()) RScriptPath = "RScript.bat";
        else RScriptPath = "Rscript";
        File d = new File(getRPath());
        String[] commandArgs = {
            RScriptPath,
            script+".r"
        };
        execProcess(d, commandArgs, isAdmin);
    }
    
    public String getInstallPath(){
        String folderPath = IJ.getDirectory("plugins")+prefix+"Install";
        return folderPath;
    }
    
    public void installHomeBrew(){
        File d = new File(getInstallPath());
        String[] commandArgs = {
            "installHomeBrew.sh"
        };
        execProcess(d, commandArgs, false);
    }
    
    public void installChocolatey(){
        File d = new File(getInstallPath());
        String[] commandArgs = {
            "installChocolatey.bat"
        };
        execProcess(d, commandArgs, false);
    }
    
    public Boolean checkR(){
        String RExePath;
        if(IJ.isWindows()){
            IJ.log("Tango for Windows is downloading R batch files");
            downloadBatchFiles();
            RExePath = getRPath() + File.separator + "R.bat";
        }else{
            RExePath = "R";
        }
        try {
            Runtime runtime = Runtime.getRuntime();
            String[] args = { RExePath, "--version" };
            for(int i=0; i<args.length;i++) IJ.log(args[i]);
            final Process process = runtime.exec(args);
            return true;
        } catch (IOException ex) {
            Logger.getLogger(Supervisor.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        }
    }
    
    public void installR(){
        if(IJ.isWindows()){
            if(checkChocolatey()){
                String[] commandArgs = {
                  "cinst",
                  "R-latest"
                };
                execProcess(new File(getImageJPath()), commandArgs, false);
            }
        }
        if(IJ.isMacOSX()){
            if(checkHomeBrew()){
                String[] commandArgs = {
                  "brew",
                  "install",
                  "science/r"
                };
                execProcess(new File(getImageJPath()), commandArgs, false);
            }
        }
        if(IJ.isLinux()){
            IJ.showMessage("You'd better install R on your own...");
        }
    }
    
    public Boolean checkMongoDb(){
        try {
            Runtime runtime = Runtime.getRuntime();
            String[] args = { "mongo", "--version" };
            final Process process = runtime.exec(args);
            if(IJ.isWindows()){
                
            }
            return true;
        } catch (IOException ex) {
            Logger.getLogger(Supervisor.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        }
    }
    
    public void installMongoDb(){
        
    }
    
    public void downloadRPlugin(String address, String localFileName) {
        String localPath = getRPath() + File.separator +localFileName;
        IJ.log("downloading plugin: "+address+ " to local dir: "+localPath);
        download(address, localPath, true);
    }
    
    public static void download(String address, String localPath, boolean executePermission){
        OutputStream out = null;
        URLConnection conn = null;
        InputStream in = null;

        try {
            URL url = new URL(address);
            out = new BufferedOutputStream(new FileOutputStream(localPath));
            File file = new File(localPath);
            if (!file.exists()) {
                file.createNewFile();
            }
            if(executePermission){
                file.setExecutable(true); 
            }
            conn = url.openConnection();
            conn.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows; U; Windows NT 6.1; en-GB;     rv:1.9.2.13) Gecko/20101203 Firefox/3.6.13 (.NET CLR 3.5.30729)");
            in = conn.getInputStream();
            byte[] buffer = new byte[1024];

            int numRead;
            long numWritten = 0;

            while ((numRead = in.read(buffer)) != -1) {
                out.write(buffer, 0, numRead);
                numWritten += numRead;
            }
            System.out.println(localPath + "\t" + numWritten);
        } 
        catch (Exception exception) { 
            exception.printStackTrace();
        } 
        finally {
            try {
                if (in != null) {
                    in.close();
                }
                if (out != null) {
                    out.close();
                }
            } 
            catch (IOException ioe) {
            }
        }
    }
    
    public void downloadJarPlugin(String address, String localFileName) {
        String localPath = getJarsPath() + File.separator + localFileName;
        IJ.log("downloading plugin: "+address+ " to local dir: "+localPath);
        download(address, localPath, true);
    }
    
    public String getJarsPath(){
        String folderPath = IJ.getDirectory("plugins")+prefix+"Jars";
        return folderPath;
    }
    
    public void updateNonFreeJars(){
        boolean restart = false;
        String[] dependencieNames = {
            "imagescience"
        };
        String[] classNames = {
            "imagescience.image.Image"
        };
        String[] dependencieUrls = {
            "http://www.imagescience.org/meijering/software/download/imagescience.jar"
        };
        for(int i=0;i<dependencieNames.length;i++){
            if(!loadJarClass(classNames[i])){
                restart = true;
                IJ.showMessage("You have to download" + dependencieNames[i] +". It will be downloaded and you will have to restart ImageJ.");
                downloadJarPlugin(dependencieUrls[i], dependencieNames[i]+".jar");
            }
        }
    }
    
    public void updateFreeJars(){
        boolean restart = false;
        String[] dependencieNames = {
            "bsh",
            "casperdatasets",
            "combinatoricslib",
            "commons-beanutils",
            "commons-io",
            "commons-lang",
            "commons-logging",
            "droplet_finder",
            "fastutil",
            "readmytables",
            "omcutil",
            "quickhull3d",
            "mongo",
            "romanface",
            "Jama",
            "JRI",
            "JRIEngine",
            "REngine",
            "Image_5D",
            "swing-layout",
            "vecmath",
            "j3dutils",
            "j3dcore",
            "loci_tools",
            "jericho-html",
            "imageware",
            "mcib3d-core",
            "mcib3d_plugins",
            "JavaGD"
        };
        String[] classNames = {
            "bsh.Console",
            "net.casper.data.model.CBuilder",
            "org.paukov.combinatorics.Generator",
            "org.apache.commons.beanutils.BeanMap",
            "org.apache.commons.io.FileUtils",
            "org.apache.commons.lang.ArrayUtils",
            "org.apache.commons.logging.LogFactory",
            "droplet_finder.DF_Watershed3D",
            "it.unimi.dsi.fastutil.ints.IntComparator",
            "org.omancode.rmt.cellreader.CellReader",
            "org.omancode.math.NamedNumber",
            "quickhull3d.QuickHull3D",
            "com.mongodb.DB",
            "org.omancode.r.RFace",
            "mcib3d.Jama.util.Maths",
            "org.rosuda.JRI.REXP",
            "org.rosuda.REngine.REXP",
            "org.rosuda.REngine.JRI.JRIEngine",
            "i5d.Image5D",
            "org.jdesktop.layout.GroupLayout",
            "javax.vecmath.VecMathUtil",
            "javax.media.j3d.Canvas3D",
            "com.sun.j3d.exp.swing.JCanvas3D",
            "loci.formats.ImageTools",
            "net.htmlparser.jericho.HTMLElements",
            "imageware.Display",
            "mcib3d.geom.Object3D",
            "mcib_plugins.Fast_filters3D",
            "org.rosuda.javaGD.JavaGD"
        };
        for(int i=0;i<dependencieNames.length;i++){
            if(!loadJarClass(classNames[i])){
                restart = true;
                downloadJarPlugin("http://"+downloadSite+"/jars/"+dependencieNames[i]+".jar", dependencieNames[i]+".jar");
            }
        }
    }
    
    public Boolean loadJarClass(String className){
        ClassLoader loader = ij.IJ.getClassLoader();
        try {
            loader.loadClass(className);
            return true;
        } catch (Exception e) {
            ij.IJ.log(className+" couldn't be found");
            return false;
        }
    }

    private boolean checkHomeBrew() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    private boolean checkChocolatey() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}
