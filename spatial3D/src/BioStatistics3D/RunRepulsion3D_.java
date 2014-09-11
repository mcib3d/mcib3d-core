package BioStatistics3D;

import ij.gui.GenericDialog;
import ij.plugin.PlugIn;

/**
 *
 * @author ttnhoa
 */
public class RunRepulsion3D_ implements PlugIn 
{    
    int tx = 128, ty = 128, tz = 128;
    int nbVesicles = 2;
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
            Repulsion3D s = new Repulsion3D(tx, ty, tz);
            s.createCell();
            //s.randomVesicles();
            s.randomListRepulsionVesicles(nbVesicles);
            
        }
        
        private boolean Dialogue() {
            GenericDialog gd = new GenericDialog("Generate many vesicles of repulsion");
            gd.addNumericField("Number of vesicles", nbVesicles, 0);
            gd.addNumericField("tx", tx, 0);
            gd.addNumericField("ty", ty, 0);
            gd.addNumericField("tz", tz, 0);
            gd.showDialog();
            nbVesicles = (int) gd.getNextNumber();
            tx = (int) gd.getNextNumber();
            ty = (int) gd.getNextNumber();
            tz = (int) gd.getNextNumber();
            return (!gd.wasCanceled());
    }
}        
