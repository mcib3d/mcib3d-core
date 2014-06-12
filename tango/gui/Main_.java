package tango.gui;

import ij.IJ;
import ij.plugin.PlugIn;
import tango.plugin.PluginFactory;

/**
 *
 **
 * /**
 * Copyright (C) 2012 Jean Ollion
 *
 *
 *
 * This file is part of tango
 *
 * tango is free software; you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 3 of the License, or (at your option) any later
 * version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 *
 * @author Jean Ollion
 */
public class Main_ implements PlugIn {

    //private static Main_ instance;
    protected static Core core;

    public Main_() {
        
    }

    @Override
    public void run(String arg) {
        String req = "1.47m";
        boolean version = IJ.versionLessThan(req);
        if (version) {
            IJ.log("currentImageJ version: "+IJ.getVersion()+" requiered: "+ req +". please update ImageJ");
            return;
        }
        IJ.showStatus("TANGO.. checking installation...");
        IJ.log("TANGO VERSION:"+Core.VERSION);
        IJ.log("TANGO.. checking installation...");
        boolean osarch = checkSystem();
        if (!osarch) {
            //ij.IJ.error("ImageJ is running with java 32-bits. Please see log and installation instructions on our website: http://tango.tuxfamily.org/");
            ij.IJ.log("ImageJ is running with java 32-bits. TANGO can work with java 32 bit, but memory will be limited to 1499Mb, which might lead to errors when importing large images. However, the Operating System has to be a 64-bit one, due to a requierement of the database system (mongoDB). Please see installation instructions on our website: http://tango.tuxfamily.org/");
        }
        IJ.log("Checking plugins");
        boolean install = PluginFactory.installComplete();
        if (!install) {
            ij.IJ.error("Install incomplete. Please see log for details, and installation instructions on our website: http://tango.tuxfamily.org/");
        }
        double maxMem = IJ.maxMemory()/(1024*1024);
        String errorMem = "Maximum memory is:" +maxMem+ " Mb. This value should be increased in order to be able to import and process large images. It should larger than twice the size of images. Please see imageJ documentation in order to increase memory";
        if (maxMem<1499) IJ.log(errorMem);
        IJ.showStatus("TANGO.. initializing...");
        IJ.log("TANGO.. initializing...");
        
        if (core != null) {
            IJ.log("TANGO is already opened");
            IJ.showStatus("TANGO is already opened");
            return;
        }
        
        //instance = this;
        
         
        
        
        
        core = new Core();
    }
    
    

    protected boolean checkSystem() {
        String os="?";
        if (IJ.isMacOSX()) os="MacOSX";
        if (IJ.isWindows()) os = "Windows";
        if (IJ.isVista()) os = "Vista";
        if (IJ.isLinux()) os="Linux :)";
        ij.IJ.log("OS: "+os);
        String osarch = System.getProperty("os.arch");
        ij.IJ.log(("os architecture: " + osarch));
        return (osarch.indexOf("64") >= 0);
    }
}
