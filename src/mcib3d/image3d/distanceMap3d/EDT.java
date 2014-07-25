package mcib3d.image3d.distanceMap3d;

import ij.ImagePlus;
import ij.measure.Calibration;
import mcib3d.image3d.*;
import mcib3d.utils.ThreadUtil;
import mcib3d.utils.exceptionPrinter;

/**
 *
 * @author thomas !
 */
public class EDT {

    /**
     *
     * @param ip 8-bit or 16-bit image
     * @param thresh
     * @param inverse
     * @param scaleXY
     * @param scaleZ
     * @return
     * @throws Exception
     */
    public static ImageFloat run(ImageHandler ip, float thresh, float scaleXY, float scaleZ, boolean inverse, int nbCPUs) {
        if (nbCPUs <= 0) {
            nbCPUs = ThreadUtil.getNbCpus();
        }
        try {
            if (ip instanceof ImageShort) {
                return (inverse) ? new EdtShortInv().run((ImageShort) ip, (int) (thresh + 0.5), scaleXY, scaleZ, nbCPUs) : new EdtShort().run((ImageShort) ip, (int) (thresh + 0.5), scaleXY, scaleZ, nbCPUs);
            } else if (ip instanceof ImageByte) {
                return ((inverse) ? new EdtByteInv().run((ImageByte) ip, (int) (thresh + 0.5), scaleXY, scaleZ, nbCPUs) : new EdtByte().run((ImageByte) ip, (int) (thresh + 0.5), scaleXY, scaleZ, nbCPUs));
            } else if (ip instanceof ImageFloat) {
                return (inverse) ? new EdtFloatInv().run((ImageFloat) ip, thresh, scaleXY, scaleZ, nbCPUs) : new EdtFloat().run((ImageFloat) ip, thresh, scaleXY, scaleZ, nbCPUs);
            }
        } catch (Exception e) {
            exceptionPrinter.print(e, "distance Map ", false);
            return null;
        }
        return null;
    }

    public static ImageFloat run(ImageHandler ip, float thresh, boolean inverse, int nbCPUs) {
        return run(ip, thresh, (float) ip.getScaleXY(), (float) ip.getScaleZ(), inverse, nbCPUs);
    }

    public static ImageFloat run_includeInside(ImageHandler ip, int thresh, float scaleXY, float scaleZ, boolean absolute, int nbCPUs) { //negative inside objects, positive outide
        ImageFloat ihdm1 = run(ip, thresh, scaleXY, scaleZ, true, nbCPUs);
        ImageFloat ihdm2 = run(ip, thresh, scaleXY, scaleZ, false, nbCPUs);
        if (!absolute) {
            for (int z = 0; z < ihdm1.sizeZ; z++) {
                for (int xy = 0; xy < ihdm1.sizeXY; xy++) {
                    if (ihdm2.pixels[z][xy] != 0) {
                        ihdm1.pixels[z][xy] = -ihdm2.pixels[z][xy];
                    }
                }
            }
        } else {
            for (int z = 0; z < ihdm1.sizeZ; z++) {
                for (int xy = 0; xy < ihdm1.sizeXY; xy++) {
                    if (ihdm2.pixels[z][xy] != 0) {
                        ihdm1.pixels[z][xy] = ihdm2.pixels[z][xy];
                    }
                }
            }
        }
        return ihdm1;
    }

    public static ImageFloat run_includeInside(ImageHandler ip, int thresh, boolean absolute, int nbCPUs) { //negative inside objects, positive outide
        return run_includeInside(ip, thresh, (float) ip.getScaleXY(), (float) ip.getScaleZ(), absolute, nbCPUs);
    }

    public static ImageFloat localThickness(ImageHandler in, ImageInt mask, float thld, float radiusXY, float radiusZ, boolean inside, int nbCPUs) {
        ImageFloat edm = EDT.run(in, thld, radiusXY, radiusZ, inside, nbCPUs);
        if (mask != null) {
            edm.intersectMask(mask);
        }
        DistanceRidge dr = new DistanceRidge();
        ImagePlus distRidge = dr.run(edm.getImagePlus(), radiusXY, radiusZ, nbCPUs);
        if (mask != null) {
            ImageHandler drIm = ImageHandler.wrap(distRidge);
            drIm.intersectMask(mask);
        }
        edm.closeImagePlus();
        LocalThickness lt = new LocalThickness();
        lt.run(distRidge, radiusXY, radiusZ, nbCPUs);
        CleanUpLocalThickness cult = new CleanUpLocalThickness();
        ImagePlus localThickness = cult.run(distRidge);
        distRidge.flush();
        return (ImageFloat) ImageFloat.wrap(localThickness);
    }

    public static ImageFloat localThickness(ImageHandler in, ImageInt mask, float thld, boolean inside, int nbCPUs) {
        return localThickness(in, mask, thld, (float) in.getScaleXY(), (float) in.getScaleZ(), inside, nbCPUs);
    }

    /**
     *
     * @param objects : 16-bit obeject label image
     * @param scaleXY
     * @param scaleZ
     * @return
     */
    public static ImageFloat runEdtLabel(ImageInt objects, float scaleXY, float scaleZ, int nbCPUs) {
        try {
            ImageShort is;
            if (!(objects instanceof ImageShort)) {
                is = new ImageShort(objects, false);
            } else {
                is = (ImageShort) objects;
            }
            return (new EdtShortLabel()).run(is, scaleXY, scaleZ, nbCPUs);
        } catch (Exception e) {
        }
        return null;
    }
}
