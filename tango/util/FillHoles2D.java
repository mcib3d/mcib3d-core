package tango.util;
import ij.ImagePlus;
import ij.ImageStack;
import ij.process.*;
import mcib3d.image3d.ImageByte;

public class FillHoles2D {


    // Binary fill by Gabriel Landini, G.Landini at bham.ac.uk
    // 21/May/2008


    public static void fill(ImageByte image, int foreground, int background) {
        if (image.sizeZ==1) {
            fill(image.getImagePlus().getProcessor(), foreground, background);
        } else {
            ImageStack stack = image.getImageStack();
            for (int i = 1; i<=image.sizeZ; i++) { // TODO multithread
                fill(stack.getProcessor(i), foreground, background);
            }
        }
    }
    
    protected static void fill(ImageProcessor ip, int foreground, int background) {
        int width = ip.getWidth();
        int height = ip.getHeight();
        FloodFiller ff = new FloodFiller(ip);
        ip.setColor(127);
        for (int y=0; y<height; y++) {
            if (ip.getPixel(0,y)==background) ff.fill(0, y);
            if (ip.getPixel(width-1,y)==background) ff.fill(width-1, y);
        }
        for (int x=0; x<width; x++){
            if (ip.getPixel(x,0)==background) ff.fill(x, 0);
            if (ip.getPixel(x,height-1)==background) ff.fill(x, height-1);
        }
        //ImagePlus show = new ImagePlus("fill holdes intermediate", ip);
        //show.show();
        byte[] pix = (byte[])ip.getPixels();
        int n = width*height;
        for (int i=0; i<n; i++) {
        if (pix[i]==127)
            pix[i] = (byte)background;
        else
            pix[i] = (byte)foreground;
        }
    }

}
