package mcib3d.geom;

import customnode.CustomMesh;
import customnode.CustomTriangleMesh;
import customnode.WavefrontExporter;
import ij.ImagePlus;
import ij3d.Volume;
import marchingcubes.MCCube;
import mcib3d.image3d.ImageByte;
import mcib3d.image3d.ImageInt;
import mcib3d.image3d.ImageShort;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;

import static mcib3d.geom.Object3D_IJUtils.getCalibration;

/**
 * Created by thomasb on 13/2/17.
 */
public class Viewer3D_Utils {

    public static List computeMeshSurface(Object3D object3D, boolean calibrated) {
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

    public static boolean waveFrontExporter(String dir, String fileName, List[] list) {
        // test export wavefront
        int nb = 1;
        HashMap<String, CustomMesh> meshHashMap = new HashMap<String, CustomMesh>(1);
        for (List l : list) {
            CustomMesh customMesh = new CustomTriangleMesh(l);
            meshHashMap.put("Object_" + nb, customMesh);
            nb++;
        }
        try {
            WavefrontExporter.save(meshHashMap, dir + fileName);
            return true;
        } catch (IOException e) {
            return false;
        }
    }
}
