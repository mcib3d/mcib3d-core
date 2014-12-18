/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package tango.gui;

import ij.IJ;
import ij.plugin.PlugIn;
import static tango.util.SystemMethods.configureMongoDB;
/**
 *
 * @author jean
 */
public class PlotMeanZProfile implements PlugIn {
    
    @Override
    public void run(String arg){
        if (Main_.core==null) {
            IJ.log("Please Run tango and select cells first");
            return;
        }
        Main_.core.getCellManager().plotMeanZProfiles();
    }
}
