package BioStatistics3D;

import ij.gui.GenericDialog;
import ij.plugin.PlugIn;
/**
 *
 * @author nhuhoa
 */
public class RunDemo_ implements PlugIn 
{    
    int tx = 128, ty = 128, tz = 128;
    int nbClusters = 2;
    int nbElementPerCluster = 4;
    /**
     *
     * @param arg
     */
    @Override
    public void run(String arg) 
    {
        if (!Dialogue()) 
        {
            return;
        }
        Demo c = new Demo(tx, ty, tz);
        c.createCell();
        //c.generateListCentroid(nbClusters);
        c.runPattern(nbClusters, nbElementPerCluster);
        
    }

    private boolean Dialogue() 
    {
        GenericDialog gd = new GenericDialog("Generate many clusters");
        gd.addNumericField("Number of clusters", nbClusters, 0);  //nbCentroid
        gd.addNumericField("Number of elements per cluster", nbElementPerCluster, 0);
        gd.addNumericField("tx", tx, 0);
        gd.addNumericField("ty", ty, 0);
        gd.addNumericField("tz", tz, 0);
        gd.showDialog();
        nbClusters = (int) gd.getNextNumber();
        nbElementPerCluster = (int) gd.getNextNumber();
        tx = (int) gd.getNextNumber();
        ty = (int) gd.getNextNumber();
        tz = (int) gd.getNextNumber();
        return (!gd.wasCanceled());
    }
}
