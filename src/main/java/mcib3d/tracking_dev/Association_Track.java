package mcib3d.tracking_dev;


import ij.IJ;
import ij.ImagePlus;
import ij.io.FileSaver;
import ij.plugin.PlugIn;
import mcib3d.image3d.ImageHandler;
import mcib3d.image3d.ImageInt;

import java.io.File;

public class Association_Track implements PlugIn {
    @Override
    public void run(String s) {
        String dirSeg = IJ.getDirectory("Select main dir");
        // first image
        ImagePlus plus1 = IJ.openImage(dirSeg + File.separator + "seg" + File.separator + "seg" + IJ.pad(0, 4) + ".zip");
        FileSaver saver = new FileSaver(plus1);
        saver.saveAsZip(dirSeg + File.separator + "track" + File.separator + "track" + IJ.pad(0, 4) + ".zip");
        ImageHandler img1 = ImageInt.wrap(plus1);
        // path1
        plus1 = IJ.openImage(dirSeg + File.separator + "seg" + File.separator + "seg" + IJ.pad(0, 4) + ".zip");
        saver = new FileSaver(plus1);
        saver.saveAsZip(dirSeg + File.separator + "path" + File.separator + "path" + IJ.pad(0, 4) + ".zip");
        plus1 = IJ.openImage(dirSeg + File.separator + "path" + File.separator + "path" + IJ.pad(0, 4) + ".zip");
        ImageHandler path1 = ImageInt.wrap(plus1);

        // loop
        for (int i = 1; i < 1000; i++) {
            IJ.log("");
            IJ.log("Tracking " + i);
            // next image
            ImagePlus plus2 = IJ.openImage(dirSeg + File.separator + "seg" + File.separator + "seg" + IJ.pad(i, 4) + ".zip");
            if (plus2 == null) break;
            ImageInt img2 = ImageInt.wrap(plus2);
            // association
            TrackingAssociation trackingAssociation = new TrackingAssociation(img1, img2);
            trackingAssociation.setPathImage(path1);
            // merge split
            trackingAssociation.setMerge(false);
            // tracking
            ImageHandler tracked = trackingAssociation.getTracked();
            ImageHandler pathed = trackingAssociation.getPathed();
            // save
            saver = new FileSaver(tracked.getImagePlus());
            saver.saveAsZip(dirSeg + File.separator + "track" + File.separator + "track" + IJ.pad(i, 4) + ".zip");
            saver = new FileSaver(pathed.getImagePlus());
            saver.saveAsZip(dirSeg + File.separator + "path" + File.separator + "path" + IJ.pad(i, 4) + ".zip");
            // update
            img1 = tracked;
            path1 = pathed;
        }
        IJ.log("Done");
    }
}
