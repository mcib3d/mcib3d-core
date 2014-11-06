/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package tango;

import ij.IJ;
import java.io.File;
import java.util.ArrayList;
import tango.util.utils;

/**
 *
 * @author julien
 */
public class addShortcut {
    public void run(String string) {
        final File dir = utils.chooseDir("Select Shortcut directory, for example Desktop", null);
        if(IJ.isWindows()){
            
        }
        if(IJ.isMacOSX()){
            ArrayList<String> commandArgs = new ArrayList<String>() {{
                add("ln");
                add("-s");
                add("/usr/local/bin/ImageJWithTango");
                add(dir.getAbsolutePath()+File.separator+"ImageJWithTango");
            }};
            tango.Installer.execProcess(dir, commandArgs, Boolean.FALSE);
        }
        if(IJ.isLinux()){
            ArrayList<String> commandArgs = new ArrayList<String>() {{
                add("ln");
                add("-s");
                add("~/.imagej/ImageJWithTango.desktop");
                add(dir.getAbsolutePath()+File.separator+"ImageJWithTango");
            }};
            tango.Installer.execProcess(dir, commandArgs, Boolean.FALSE);
        }
    }
}
