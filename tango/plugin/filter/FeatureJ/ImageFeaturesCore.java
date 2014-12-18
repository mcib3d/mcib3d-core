/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tango.plugin.filter.FeatureJ;

import ij.ImagePlus;
import ij.measure.Calibration;
import imagescience.feature.Differentiator;
import imagescience.feature.Edges;
import imagescience.image.FloatImage;
import imagescience.image.Image;
import java.util.Vector;
import mcib3d.image3d.ImageFloat;
import mcib3d.image3d.ImageHandler;
import mcib3d.utils.ThreadRunner;

/**
 *
 * @author jollion
 */
public class ImageFeaturesCore {
    
    public static void hysteresis(ImageHandler image, double lowval, double highval, boolean lowConnectivity) {
        final Image imp = Image.wrap(image.getImagePlus());
        final src.mcib_plugins.processing.Thresholder thres = new src.mcib_plugins.processing.Thresholder();
        if (lowConnectivity) {
            thres.hysteresisLowConnectivity(imp, lowval, highval);
        } else {
            thres.hysteresis(imp, lowval, highval);
        }
    }
    
    public static ImageFloat[] getInertia(ImageHandler image, double smoothScale, double integrationScale, int nbCPUs) {
        ImageFloat[] res = new ImageFloat[3];
        ImagePlus[] hess = FJInertia(image.getImagePlus(), smoothScale, integrationScale, nbCPUs);
        for (int i = 0; i < 3; i++) {
            res[i] = new ImageFloat(hess[i]);
            res[i].setTitle(image.getTitle() + ":intertia" + (i + 1));
            res[i].offsetX = image.offsetX;
            res[i].offsetY = image.offsetY;
            res[i].offsetZ = image.offsetZ;
        }
        return res;
    }
    
    public static ImageFloat getHessianDeterminant(ImageHandler image, double scale, int nbCPUs, boolean allNegative) {
        ImageFloat res, res0, res1, res2;
        ImagePlus[] hess = FJHessian(image.getImagePlus(), scale, nbCPUs);

        res = new ImageFloat(hess[0]);
        res = (ImageFloat) res.multiplyImage(new ImageFloat(hess[1]), 1);
        res = (ImageFloat) res.multiplyImage(new ImageFloat(hess[2]), 1);
        if (allNegative) {
            // test if all values are negative
            res0 = new ImageFloat(hess[0]);
            res1 = new ImageFloat(hess[1]);
            res2 = new ImageFloat(hess[2]);
            for (int i = 0; i < res0.sizeXYZ; i++) {
                if ((res0.getPixel(i) >= 0) || (res1.getPixel(i) >= 0) || (res2.getPixel(i) >= 0)) {
                    res.setPixel(i, 0);
                }
            }
        }
        return res;
    }

    private static ImagePlus FJEdges(ImagePlus imp, double scaleval, boolean edge, double lowval, double highval) {
        Image imw = Image.wrap(imp);
        Calibration cal = imp.getCalibration();
        if (cal.scaled()) {
            scaleval *= cal.pixelWidth; //scaleval*=Math.pow(cal.pixelWidth*cal.pixelHeight*cal.pixelDepth, 0.3332);
        }
        Image newimg = new FloatImage(imw);
        Edges edges = new Edges();
        newimg = edges.run(newimg, scaleval, edge);
        if (edge && (lowval > 0) && (highval > 0)) {
            Thresholder thres = new Thresholder();
            thres.hysteresis(newimg, lowval, highval);
        }
        ImagePlus res = newimg.imageplus();
        return res;
    }

    private static ImagePlus[] FJHessian(ImagePlus imp, double scaleval, int nbCPUs) {
        Image image = Image.wrap(imp);
        Calibration cal = imp.getCalibration();
        if (cal.scaled()) {
            scaleval *= cal.pixelWidth; //scaleval*=Math.pow(cal.pixelWidth*cal.pixelHeight*cal.pixelDepth, 0.3332);
        }
        Vector vector = new FJHessian3D().run(new FloatImage(image), scaleval, nbCPUs);
        ImagePlus[] res = new ImagePlus[3];
        res[0] = ((Image) vector.get(0)).imageplus();
        res[1] = ((Image) vector.get(1)).imageplus();
        res[2] = ((Image) vector.get(2)).imageplus();
        return res;
    }

    private static ImagePlus[] FJInertia(ImagePlus imp, double smoothScale, double integrationScale, int nbCPUs) {
        Image image = Image.wrap(imp);
        Calibration cal = imp.getCalibration();
        double sscale = smoothScale;
        double iscale = integrationScale;
        if (cal.scaled()) {
            sscale *= cal.pixelWidth;
            iscale *= cal.pixelWidth;
        }
        Vector vector = (new FJStructure3D()).run(image, sscale, iscale, nbCPUs);


        ImagePlus[] res = new ImagePlus[3];
        res[0] = ((Image) vector.get(0)).imageplus();
        res[1] = ((Image) vector.get(1)).imageplus();
        res[2] = ((Image) vector.get(2)).imageplus();
        return res;
    }
    
    public static ImageFloat[] getGradient(ImageHandler image, double scale, boolean computeMagnitude, int nbCPUs) {
        final ImageFloat[] res = new ImageFloat[computeMagnitude ? 4 : 3];
        Image imw = Image.wrap(image.getImagePlus());
        final Image Ix, Iy, Iz;
        scale *= (double) image.getScaleXY(); // FIXME scaleZ?
        if (nbCPUs > 1) {
            final FJDifferentiator3D differentiator = new FJDifferentiator3D();
            Ix = differentiator.run(imw.duplicate(), scale, 1, 0, 0, nbCPUs);
            Iy = differentiator.run(imw.duplicate(), scale, 0, 1, 0, nbCPUs);
            Iz = differentiator.run(imw.duplicate(), scale, 0, 0, 1, nbCPUs);
        } else {
            final Differentiator differentiator = new Differentiator();
            Ix = differentiator.run(imw.duplicate(), scale, 1, 0, 0);
            Iy = differentiator.run(imw.duplicate(), scale, 0, 1, 0);
            Iz = differentiator.run(imw.duplicate(), scale, 0, 0, 1);
        }
        res[0] = new ImageFloat(Ix.imageplus());
        res[1] = new ImageFloat(Iy.imageplus());
        res[2] = new ImageFloat(Iz.imageplus());
        if (computeMagnitude) {
            res[3] = new ImageFloat(image.getTitle() + "_gradientMagnitude", image.sizeX, image.sizeY, image.sizeZ);
            final ThreadRunner tr = new ThreadRunner(0, image.sizeZ, nbCPUs);
            final int sizeXY  = image.sizeXY;
            for (int i = 0; i < tr.threads.length; i++) {
                tr.threads[i] = new Thread(
                        new Runnable() {
                            public void run() {
                                for (int z = tr.ai.getAndIncrement(); z < tr.end; z = tr.ai.getAndIncrement()) {
                                    for (int xy = 0; xy < sizeXY; xy++) {
                                        res[3].pixels[z][xy] = (float) Math.sqrt(res[0].pixels[z][xy] * res[0].pixels[z][xy] + res[1].pixels[z][xy] * res[1].pixels[z][xy] + res[2].pixels[z][xy] * res[2].pixels[z][xy]);
                                    }
                                }
                            }
                        });
            }
            tr.startAndJoin();
        }
        return res;
    }
    
    public static ImageFloat getGradient(ImageHandler image, double scale, int nbCPUs) {
        return getGradient(image, scale, true, nbCPUs)[3];
    }

    public static ImageFloat[] getHessian(ImageHandler image, double scale, int nbCPUs) {
        ImageFloat[] res = new ImageFloat[3];
        ImagePlus[] hess = FJHessian(image.getImagePlus(), scale, nbCPUs);
        for (int i = 0; i < 3; i++) {
            res[i] = new ImageFloat(hess[i]);
            res[i].setTitle(image.getTitle() + ":hessian" + (i + 1));
            res[i].offsetX = image.offsetX;
            res[i].offsetY = image.offsetY;
            res[i].offsetZ = image.offsetZ;
        }
        return res;
    }
    
    //from FeatureJ
    public static ImageFloat gaussianSmooth(ImageHandler image, double scaleXY, double scaleZ, int nbCPUs) {
        Image res;
        double old_scaleXY = image.getScaleXY();
        double old_scaleZ = image.getScaleZ();
        image.setScale(1, scaleXY / scaleZ, image.getUnit());
        FJDifferentiator3D differentiator = new FJDifferentiator3D();
        res = differentiator.run(Image.wrap(image.getImagePlus()), scaleXY, 0, 0, 0, nbCPUs);
        image.setScale(old_scaleXY, old_scaleZ, image.getUnit());
        ImageFloat ihres = (ImageFloat) ImageHandler.wrap(res.imageplus());
        ihres.setScale(old_scaleXY, old_scaleZ, image.getUnit());
        ihres.setOffset(image);
        return ihres;
    }
    
}
