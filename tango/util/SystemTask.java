/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tango.util;

import ij.IJ;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;

/**
 *
 * @author julien
 */
public class SystemTask implements Runnable{
    public ResultObject r;
    public ProcessBuilder pb;
    SystemTask(ResultObject r, ProcessBuilder pb){
        this.r = r;
        this.pb = pb;
    }
    @Override
    public void run() {
        final Process p;
                try {
                    logInputDescription();
                    p = pb.start();
                    try {
                        p.waitFor();
                    } catch (InterruptedException ex) {
                        //IJ.log(ex.toString());
                        r.addError(ex.toString());
                        r.setResult(false);
                    }
                    OutputStream out = p.getOutputStream();
                    out.close();
                    BufferedReader in = new BufferedReader(new InputStreamReader(p.getInputStream()));
                    String line = null;
                    while ((line = in.readLine()) != null) {
                        //IJ.log(line);
                        r.addOutput(line);
                    }
                    line = null;
                    BufferedReader error = new BufferedReader(new InputStreamReader(p.getErrorStream()));
                    while ((line = error.readLine()) != null) {
                        //IJ.log(line);
                        r.addOutput(line);
                    }
                    try {
                        int v = p.exitValue();
                        if(v==0) r.setResult(true);
                        else r.setResult(false);
                    }
                    catch(IllegalThreadStateException e) {
                        r.setResult(false);
                        //IJ.log(ex.toString());
                        r.addError(e.toString());
                    }
                } catch (IOException ex) {
                    //IJ.log(ex.toString());
                    r.addError(ex.toString());
                    r.setResult(false);
                }
                r.logResult();
                r.logOutput();
                r.logError();
        }

    private void logInputDescription() {
        IJ.log("Executing command : "+pb.command());
        IJ.log("In working directory : "+pb.directory().getAbsolutePath());
        if(pb.directory().isDirectory()) IJ.log("Directory exists");
    }
}
