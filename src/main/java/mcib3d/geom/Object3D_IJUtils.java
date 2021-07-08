package mcib3d.geom;

import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.gui.Roi;
import ij.measure.Calibration;
import ij.plugin.filter.ThresholdToSelection;
import ij.process.ByteProcessor;
import ij.process.ColorProcessor;
import ij.process.ImageProcessor;
import mcib3d.image3d.ImageHandler;
import mcib3d.image3d.ImageInt;

import java.awt.*;
import java.time.Duration;
import java.time.Instant;
import java.util.stream.Stream;

/**
 * static methods to use with ImageJ for the Object3D class
 * Created by thomasb on 16/11/16.
 */
public class Object3D_IJUtils {


    public static Calibration getCalibration(Object3D object3D) {
        Calibration cal = new Calibration();
        cal.pixelWidth = object3D.getResXY();
        cal.pixelHeight = object3D.getResXY();
        cal.pixelDepth = object3D.getResZ();
        cal.setUnit(object3D.getUnits());

        return cal;
    }

    public static void setCalibration(Object3D object3D, Calibration cal) {
        if (cal != null) {
            object3D.setResXY(cal.pixelWidth);
            object3D.setResZ(cal.pixelDepth);
            object3D.setUnits(cal.getUnits());
        }
    }

    public static boolean touchBorders(Object3D object3D, ImagePlus img, boolean Z) {
        int[] bb = object3D.getBoundingBox();
        // 0
        if ((bb[0] <= 0) || (bb[2] <= 0)) {
            return true;
        }
        if (Z && (bb[4] <= 0)) {
            return true;
        }
        // max
        if ((bb[1] >= img.getWidth() - 1) || (bb[3] >= img.getHeight() - 1)) {
            return true;
        }
        return Z && (bb[5] >= img.getNSlices() - 1);
    }

    public static Object3DVoxels createObject3DVoxels(ImagePlus plus, int val) {
        ImageInt imageInt = ImageInt.wrap(plus);
        return new Object3DVoxels(imageInt, val);
    }

    public static Object3DLabel createObject3DLabel(ImagePlus plus, int val) {
        ImageInt imageInt = ImageInt.wrap(plus);
        return new Object3DLabel(imageInt, val);
    }

    public static Object3DLabel createObject3DLabel(ImageStack plus, int val) {
        ImageInt imageInt = ImageInt.wrap(plus);
        return new Object3DLabel(imageInt, val);
    }

    public static Object3DVoxels createObject3DVoxels(ImageStack plus, int val) {
        ImageInt imageInt = ImageInt.wrap(plus);
        return new Object3DVoxels(imageInt, val);
    }

    public static Roi createRoi(Object3D object3D, int z) {
        // IJ.write("create roi " + z);
        int sx = object3D.getXmax() - object3D.getXmin() + 1;
        int sy = object3D.getYmax() - object3D.getYmin() + 1;
        ByteProcessor mask = new ByteProcessor(sx, sy);
        // object black on white
        //mask.invert();
        draw(object3D, mask, z, 255);
        ImagePlus maskPlus = new ImagePlus("mask " + z, mask);
        //maskPlus.show();
        //IJ.run("Create Selection");
        ThresholdToSelection tts = new ThresholdToSelection();
        tts.setup("", maskPlus);
        tts.run(mask);
        maskPlus.updateAndDraw();
        // IJ.write("sel=" + maskPlus.getRoi());
        //maskPlus.hide();
        Roi roi = maskPlus.getRoi();
        Rectangle rect = roi.getBounds();
        rect.x += object3D.getXmin();
        rect.y += object3D.getYmin();

        return roi;
    }

    public static ImagePlus drawRandomColors(Objects3DPopulation objects3DPopulation, int sx, int sy, int sz) {
        ImageStack imageStack = new ImageStack(sx, sy);
        for (int s = 0; s < sz; s++) {
            ColorProcessor colorProcessor = new ColorProcessor(sx, sy);
            imageStack.addSlice(colorProcessor);
        }

        for (int i = 0; i < objects3DPopulation.getNbObjects(); i++) {
            Object3D object3D = objects3DPopulation.getObject(i);
            int r = (int) (255 * Math.random());
            int g = (int) (255 * Math.random());
            int b = (int) (255 * Math.random());
            while ((r < 50) && (g < 50) && (b < 50)) {
                if (r < 50) r = (int) (255 * Math.random());
                if (g < 50) g = (int) (255 * Math.random());
                if (b < 50) b = (int) (255 * Math.random());
            }
            Object3D_IJUtils.draw(object3D, imageStack, r, g, b);
        }

        return new ImagePlus("color", imageStack);
    }

    public static boolean draw(Object3D object3D, ByteProcessor mask, int z, int col) {
        boolean ok = false;

        for (Voxel3D vox : object3D.getVoxels()) {
            if (Math.abs(z - vox.getZ()) < 0.5) {
                mask.putPixel(vox.getRoundX(), vox.getRoundY(), col);
                ok = true;
            }
        }
        return ok;
    }

    /**
     *
     */
    public static void draw(Object3D object3D, ImageStack mask, int col) {
        // TEST STREAM
        Stream<Voxel3D> stream = object3D.getVoxels().parallelStream();
        stream.forEach(vox -> {
            mask.setVoxel(vox.getRoundX(), vox.getRoundY(), vox.getRoundZ(), col);
        });
    }

    /**
     *
     */
    public static void draw(Object3D object3D, ImageStack mask, int r, int g, int b) {
        ImageProcessor tmp;
        Color col = new Color(r, g, b);
        for (Voxel3D vox : object3D.getVoxels()) {
            tmp = mask.getProcessor((int) (vox.getZ() + 1));
            tmp.setColor(col);
            tmp.drawPixel(vox.getRoundX(), vox.getRoundY());
        }
    }

    public static void drawLabel(Object3D object3D, ImageStack mask, int col) {
        int z = (int) object3D.getCenterZ();
        ImageProcessor tmp = mask.getProcessor((int) (z + 1));
        tmp.setColor(col);
        Font font = new Font(Font.DIALOG, Font.PLAIN, 10);
        String name = object3D.getName();
        tmp.drawString(object3D.getName(), (int) (object3D.getCenterX() - name.length() * font.getSize() / 4), (int) (object3D.getYmax() + font.getSize()));

    }

    public static void drawLabel(Object3D object3D, ImageStack mask, int nbz, int r, int g, int b) {
        int cz = (int) object3D.getCenterZ();
        int zmin = Math.max(0, cz - nbz);
        int zmax = Math.min(mask.getSize() - 1, cz + nbz);
        for (int z = zmin; z <= zmax; z++) {
            ImageProcessor tmp = mask.getProcessor((int) (z + 1));
            tmp.setColor(new Color(r, g, b));
            Font font = new Font(Font.DIALOG, Font.PLAIN, 10);
            String name = object3D.getName();
            tmp.drawString(object3D.getName(), (int) (object3D.getCenterX() - name.length() * font.getSize() / 4), (int) (object3D.getYmax() + font.getSize()));
        }
    }


    public static void drawIntersectionLabel(Object3DLabel object3DLabel, Object3DLabel other, ImageStack mask, int red, int green, int blue) {
        ImageProcessor tmp;
        ImageHandler otherSeg = other.getLabelImage();
        int otherValue = other.getValue();
        Color col = new Color(red, green, blue);
        int zmin = object3DLabel.getZmin();
        int zmax = object3DLabel.getZmax();
        int ymin = object3DLabel.getYmin();
        int ymax = object3DLabel.getYmax();
        int xmin = object3DLabel.getXmin();
        int xmax = object3DLabel.getXmax();
        ImageHandler labelImage = object3DLabel.getLabelImage();
        int value = object3DLabel.getValue();
        for (int z = zmin; z <= zmax; z++) {
            tmp = mask.getProcessor(z + 1);
            tmp.setColor(col);
            for (int x = xmin; x <= xmax; x++) {
                for (int y = ymin; y <= ymax; y++) {
                    if ((labelImage.getPixel(x, y, z) == value) && (otherSeg.getPixel(x, y, z) == otherValue)) {
                        tmp.drawPixel(x, y);
                    }
                }
            }
        }
    }

    public static void drawIntersectionLabel(Object3DLabel object3DLabel, Object3DLabel other, ImageStack mask, int col) {
        ImageProcessor tmp;
        ImageHandler otherSeg = other.getLabelImage();
        int otherValue = other.getValue();
        int zmin = object3DLabel.getZmin();
        int zmax = object3DLabel.getZmax();
        int ymin = object3DLabel.getYmin();
        int ymax = object3DLabel.getYmax();
        int xmin = object3DLabel.getXmin();
        int xmax = object3DLabel.getXmax();
        ImageHandler labelImage = object3DLabel.getLabelImage();
        int value = object3DLabel.getValue();
        for (int z = zmin; z <= zmax; z++) {
            tmp = mask.getProcessor(z + 1);
            for (int x = xmin; x <= xmax; x++) {
                for (int y = ymin; y <= ymax; y++) {
                    if ((labelImage.getPixel(x, y, z) == value) && (otherSeg.getPixel(x, y, z) == otherValue)) {
                        tmp.putPixel(x, y, col);
                    }
                }
            }
        }
    }


}
