package Cell3D;

import ij.IJ;
import ij.ImagePlus;
import ij.gui.GenericDialog;
import ij.plugin.PlugIn;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;
import mcib3d.image3d.ImageFloat;
import mcib3d.image3d.ImageHandler;
import mcib3d.image3d.ImageInt;
import mcib3d.image3d.distanceMap3d.EDT;

/**
 * Euclidean Distance Map Eroded Volume Fraction (originally written by Jean O
 * for TANGO)
 *
 */
public class TestEDT_3D implements PlugIn {

    boolean inverse = false;
    int threshold = 1;

    @Override
    public void run(String arg) {
        ImagePlus imp = IJ.getImage();
        String title = imp.getTitle();

        GenericDialog dia = new GenericDialog("EDT");
        dia.addChoice("Map", new String[]{"EDT", "EVF", "Both"}, "EDT");
        dia.addNumericField("Threshold", threshold, 0);
        dia.addCheckbox("Inverse", inverse);
        dia.showDialog();
        String map = dia.getNextChoice();
        threshold = (int) dia.getNextNumber();
        inverse = dia.getNextBoolean();

        if (dia.wasOKed()) {
            try {
                ImageHandler img = ImageHandler.wrap(imp);
                ImageFloat r = EDT.run(img, threshold, inverse, Runtime.getRuntime().availableProcessors());
                if (r != null) {
                    r.setTitle("EDT_" + title);
                    if (map.compareTo("EVF") != 0) {
                        r.show("EDT");
                    }
                    // EVF 
                    if (map.compareTo("EDT") != 0) {
                        ImageFloat r2 = r.duplicate();
                        normalizeDistanceMap(r2, img.threshold(threshold, inverse, true));
                        r2.show("EVF");
                    }
                }
            } catch (Exception ex) {
                Logger.getLogger(TestEDT_3D.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    public void normalizeDistanceMap(ImageFloat distanceMap, ImageInt mask) {
        int count = 0;
        Vox[] idx = new Vox[mask.countMaskVolume()];
        double volume = idx.length;
        for (int z = 0; z < distanceMap.sizeZ; z++) {
            for (int xy = 0; xy < distanceMap.sizeXY; xy++) {
                if (mask.getPixelInt(xy, z) != 0) {
                    idx[count] = new Vox(distanceMap.pixels[z][xy], xy, z);
                    count++;
                }
            }
        }
        Arrays.sort(idx);
        for (int i = 0; i < idx.length - 1; i++) 
        {
            // gestion des repetitions
            if (idx[i + 1].distance == idx[i].distance) 
            {
                int j = i + 1;
                while (j < (idx.length - 1) && idx[i].distance == idx[j].distance) 
                {
                    j++;
                }
                double median = (i + j) / 2d;
                for (int k = i; k <= j; k++) 
                {
                    idx[k].index = median;
                }
                i = j;
            } else {
                idx[i].index = i;
            }
        }
        if (idx[idx.length - 1].index == 0) {
            idx[idx.length - 1].index = idx.length - 1;
        }
        //for (int i = 0; i < idx.length; i++) {
        	/*if(idx[i].index / volume < 0.5){
        		distanceMap.pixels[idx[i].z][idx[i].xy] = (float) (idx[i].index / volume);
        		IJ.log("probability: " + idx[i].index / volume);
        	}
            */
        	/*if(idx[i].index==0){
        		distanceMap.pixels[idx[i].z][idx[i].xy] = 255;
        		IJ.log("merde");
        	}*/
            //IJ.log("index: " + idx[i].index + " dis: " + idx[i].distance);
            
        //}
    }

    protected class Vox implements Comparable<Vox> {

        float distance;
        double index;
        int xy, z;

        public Vox(float distance, int xy, int z) {
            this.distance = distance;
            this.xy = xy;
            this.z = z;
        }

        public Vox(float distance, double index, int xy, int z) {
            this.distance = distance;
            this.index = index;
            this.xy = xy;
            this.z = z;
        }

        @Override
        public int compareTo(Vox v) {
            if (distance > v.distance) {
                return 1;
            } else if (distance < v.distance) {
                return -1;
            } else {
                return 0;
            }
        }
    }
}
