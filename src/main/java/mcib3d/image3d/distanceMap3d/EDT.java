package mcib3d.image3d.distanceMap3d;

import ij.IJ;
import ij.ImagePlus;
import ij.measure.Calibration;

import java.util.ArrayList;
import java.util.Arrays;

import mcib3d.image3d.*;
import mcib3d.utils.ThreadUtil;
import mcib3d.utils.exceptionPrinter;

/**
 * @author thomas !
 */
public class EDT {

    /**
     * @param ip      8-bit or 16-bit image
     * @param thresh
     * @param inverse
     * @param scaleXY
     * @param scaleZ
     * @param nbCPUs
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
                //return ((inverse) ? new EdtByteInv().run((ImageByte) ip, (int) (thresh + 0.5), scaleXY, scaleZ, nbCPUs) : new EdtByte().run((ImageByte) ip, (int) (thresh + 0.5), scaleXY, scaleZ, nbCPUs));
               if(inverse){
                   return new EdtByteInv().run((ImageByte) ip, (int) (thresh + 0.5), scaleXY, scaleZ, nbCPUs);
               }
               else {
                   return new EdtByte().run((ImageByte) ip, (int) (thresh + 0.5), scaleXY, scaleZ, nbCPUs);
               }
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
        //IJ.log("EDT scale " + ip.getScaleXY() + " " + ip.getScaleZ());
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


    public static void normalizeDistanceMap(ImageFloat distanceMap, ImageInt mask) {
        normalizeDistanceMap(distanceMap, mask, true);
    }

    public static void normalizeDistanceMap(ImageFloat distanceMap, ImageInt mask, boolean excludeZeros) {
        normalizeDistanceMap(distanceMap, (ImageHandler) mask, excludeZeros);
    }

    public static void normalizeDistanceMap(ImageFloat distanceMap, ImageHandler mask, boolean excludeZeros) {
        // int count = 0;
        ArrayList<VoxEVF> idxList = new ArrayList<VoxEVF>();
        //VoxEVF[] idx = new VoxEVF[mask.countMaskVolume()];
        //double volume = idx.length;
        double minDist = Double.NEGATIVE_INFINITY;
        if (excludeZeros) {
            minDist = 0;
        }
        for (int z = 0; z < distanceMap.sizeZ; z++) {
            for (int xy = 0; xy < distanceMap.sizeXY; xy++) {
                if ((mask.getPixel(xy, z) > 0) && (distanceMap.getPixel(xy, z) > minDist)) {
                    idxList.add(new VoxEVF(distanceMap.pixels[z][xy], xy, z));
                    //idx[count] = new VoxEVF(distanceMap.pixels[z][xy], xy, z);
                    //count++;
                } else {
                    distanceMap.setPixel(xy, z, 1.0f);
                }
            }
        }
        if (idxList.isEmpty()) {
            return;
        }
        VoxEVF[] idx = new VoxEVF[idxList.size()];
        idx = (VoxEVF[]) idxList.toArray(idx);
        double volume = idx.length;
        Arrays.sort(idx);
        for (int i = 0; i < idx.length - 1; i++) {
            // gestion des repetitions
            if (idx[i + 1].distance == idx[i].distance) {
                int j = i + 1;
                while (j < (idx.length - 1) && idx[i].distance == idx[j].distance) {
                    j++;
                }
                double median = (i + j) / 2d;
                for (int k = i; k <= j; k++) {
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
        for (VoxEVF idx1 : idx) {
            distanceMap.pixels[idx1.z][idx1.xy] = (float) (idx1.index / volume);
        }
    }
}
