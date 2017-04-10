package mcib3d.image3d;

import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.io.FileSaver;
import ij.process.ImageProcessor;

/**
 * Created by thomasb on 3/3/17.
 */
public class ImagePlus_Utils {

    public static boolean saveAsPngSequence(ImageHandler image, String dir, String name, int start, int pad, boolean convert8bits) {
        ImageByte imageByte;
        if (image instanceof ImageShort) {
            imageByte = ((ImageShort) image).convertToByte(0.005);
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

            FileSaver fileSaver = new FileSaver(imagePlus);
            if (!fileSaver.saveAsPng(dir + fileName)) return false;
        }
        return true;
    }


}
