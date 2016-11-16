package mcib3d.geom;

import ij.ImagePlus;
import ij.measure.Calibration;
import ij3d.Volume;
import marchingcubes.MCCube;
import mcib3d.image3d.ImageByte;
import mcib3d.image3d.ImageInt;
import mcib3d.image3d.ImageShort;

import java.util.List;

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
        object3D.setResXY(cal.pixelWidth);
        object3D.setResZ(cal.pixelDepth);
        object3D.setUnits(cal.getUnits());
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

    public List computeMeshSurface(Object3D object3D, boolean calibrated) {
        //IJ.showStatus("computing mesh");
        // use miniseg
        ImageInt miniseg = object3D.getLabelImage();
        ImageByte miniseg8 = ((ImageShort) (miniseg)).convertToByte(false);
        ImagePlus objectImage = miniseg8.getImagePlus();
        if (calibrated) {
            objectImage.setCalibration(getCalibration(object3D));
        }
        boolean[] bl = {true, true, true};
        Volume vol = new Volume(objectImage, bl);
        vol.setAverage(true);
        List l = MCCube.getTriangles(vol, 0);
        // needs to invert surface
        l = Object3DSurface.invertNormals(l);
        // translate object with units coordinates
        float tx, ty, tz;
        if (calibrated) {
            tx = (float) (miniseg.offsetX * object3D.getResXY());
            ty = (float) (miniseg.offsetY * object3D.getResXY());
            tz = (float) (miniseg.offsetZ * object3D.getResZ());
        } else {
            tx = (float) (miniseg.offsetX);
            ty = (float) (miniseg.offsetY);
            tz = (float) (miniseg.offsetZ);
        }
        l = Object3DSurface.translateTool(l, tx, ty, tz);

        return l;
    }
}
