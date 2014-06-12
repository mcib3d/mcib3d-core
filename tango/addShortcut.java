/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package tango;

import ij.IJ;
import java.io.File;
import tango.util.utils;

/**
 *
 * @author julien
 */
public class addShortcut {
    public void run(String string) {
        File dir = utils.chooseDir("Select Shortcut directory, for example Desktop", null);
        if(IJ.isWindows()){
            
        }
        if(IJ.isMacOSX()){
            String[] commandArgs = {
                "ln",
                "-s",
                "/usr/local/bin/ImageJWithTango",
                dir.getAbsolutePath()+File.separator+"ImageJWithTango"
            };
            tango.Supervisor.execProcess(dir, commandArgs, Boolean.FALSE);
        }
        if(IJ.isLinux()){
            String[] commandArgs = {
                "ln",
                "-s",
                "~/.imagej/ImageJWithTango.desktop",
                dir.getAbsolutePath()+File.separator+"ImageJWithTango"
            };
            tango.Supervisor.execProcess(dir, commandArgs, Boolean.FALSE);
        }
    }
}
