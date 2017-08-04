package mcib3d.image3d;

import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.io.FileSaver;
import ij.process.ImageProcessor;

import java.awt.image.IndexColorModel;

/**
 * Created by thomasb on 3/3/17.
 */
public class ImagePlus_Utils {

    public static boolean saveAsPngSequence(ImageHandler image, String dir, String name, int start, int pad, boolean lut) {
        ImageByte imageByte;
        if (image instanceof ImageShort) {
            if (!lut)
                imageByte = ((ImageShort) image).convertToByte(0.005);
            else
                imageByte = ((ImageShort) image).convertToByte(0);
        } else {
            imageByte = (ImageByte) image;
        }
        ImageStack imageStack = imageByte.getImageStack();
        String fileName;
        ImageProcessor imageProcessor;
        ImagePlus imagePlus;
        for (int s = 0; s < imageStack.getSize(); s++) {
            fileName = name + IJ.pad(s + start, pad) + ".png";
            imageProcessor = imageStack.getProcessor(s + 1);
            imagePlus = new ImagePlus(fileName, imageProcessor);
            if (lut) rgb332(imagePlus);
            FileSaver fileSaver = new FileSaver(imagePlus);
            if (!fileSaver.saveAsPng(dir + fileName)) return false;
        }
        return true;
    }

    private static void rgb332(ImagePlus imagePlus) {
        byte[] reds = new byte[256];
        byte[] greens = new byte[256];
        byte[] blues = new byte[256];
        for (int i = 0; i < 256; i++) {
            reds[i] = (byte) (i & 0xe0);
            greens[i] = (byte) ((i << 3) & 0xe0);
            blues[i] = (byte) ((i << 6) & 0xc0);
        }
        IndexColorModel cm = new IndexColorModel(8, 256, reds, greens, blues);
        ImageProcessor ip = imagePlus.getChannelProcessor();
        ip.setColorModel(cm);
        ip.setMinAndMax(0, 255);
    }


}
