/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package tango;

import ij.plugin.PlugIn;
import static tango.util.SystemMethods.configureMongoDB;
/**
 *
 * @author julien
 */
public class ConfigureMongoDB implements PlugIn {
    
    @Override
    public void run(String arg){
        configureMongoDB();
    }
}
