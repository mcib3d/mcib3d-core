/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mcib3d.geom.deformation3d;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;

/**
 *
 * @author lautaro
 */
public class IO {
    
    public IO(){}
    
    public File[] getFiles(String dir) {
        
        File folder = new File(dir);
        File[] listOfFiles = folder.listFiles();
        Collections.sort(Arrays.asList(listOfFiles));

        return listOfFiles;
    }
    
}
