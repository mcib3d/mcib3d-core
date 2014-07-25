package mcib3d.image3d.processing;
import mcib3d.image3d.*;
import mcib3d.image3d.distanceMap3d.EDT;
import mcib3d.utils.ThreadRunner;
import mcib3d.utils.exceptionPrinter;
import mcib3d.utils.ThreadUtil;
/**
 *
 **
 * /**
 * Copyright (C) 2012 Jean Ollion
 *
 *
 *
 * This file is part of tango
 *
 * tango is free software; you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 3 of the License, or (at your option) any later
 * version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 *
 * @author Jean Ollion
 */
public class BinaryMorpho {

    // morphoMath operation
    public static final byte MORPHO_DILATE = 1;
    public static final byte MORPHO_ERODE = 2;
    public static final byte MORPHO_CLOSE = 3;
    public static final byte MORPHO_OPEN = 4;

    public static ImageByte binaryMorpho(ImageInt in, int op, float radius, float radiusZ) {
        return binaryMorpho(in, op, radius, radiusZ, 0);
    }

    public static ImageByte binaryMorpho(ImageInt in, int op, float radius, float radiusZ, int nbCPUs) {
        switch (op) {
            case MORPHO_DILATE:
                return binaryDilate(in, radius, radiusZ, nbCPUs);
            case MORPHO_CLOSE:
                return binaryClose(in, radius, radiusZ, nbCPUs);
            case MORPHO_ERODE:
                return binaryErode(in, radius, radiusZ, nbCPUs);
            case MORPHO_OPEN:
                return binaryOpen(in, radius, radiusZ, nbCPUs);
            default:
                return null;
        }
    }

    public static ImageByte binaryOpen(ImageInt in, float radius, float radiusZ) {
        return binaryOpen(in, radius, radiusZ, 0);
    }

    public static ImageByte binaryOpen(ImageInt in, float radius, float radiusZ, int nbCPUs) {
        try {
            if (nbCPUs == 0) {
                nbCPUs = ThreadUtil.getNbCpus();
            }

            // test rad <=1
            if ((radius <= 1) && (radiusZ <= 1)) {
                return binaryOpenRad1(in, 1, nbCPUs);
            }

            ImageFloat edm = EDT.run(in, 0, 1, radius / radiusZ, false, nbCPUs);
            ImageByte temp = edm.threshold(radius, false, true);
            edm.closeImagePlus();
            edm = EDT.run(temp, 0, 1, radius / radiusZ, true, nbCPUs);
            temp.closeImagePlus();
            temp = edm.threshold(radius, true, false);
            edm.closeImagePlus();
            edm = null;
            System.gc();
            temp.setOffset(in);
            temp.setScale(in);
            return temp;
        } catch (Exception e) {
            exceptionPrinter.print(e, null, true);
        }
        return null;
    }

    public static ImageByte binaryErode(ImageInt in, float radius, float radiusZ) {
        return binaryErode(in, radius, radiusZ, 0);
    }

    public static ImageByte binaryErode(ImageInt in, float radius, float radiusZ, int nbCPUs) {
        try {
            if (nbCPUs == 0) {
                nbCPUs = ThreadUtil.getNbCpus();
            }

            // test rad <=1
            if ((radius <= 1) && (radiusZ <= 1)) {
                return binaryErodeRad1(in, 1, nbCPUs);
            }

            ImageFloat edm = EDT.run(in, 0, 1, radius / radiusZ, false, nbCPUs);
            ImageByte temp = edm.threshold(radius, false, true);
            edm.flush();
            edm = null;
            temp.setOffset(in);
            temp.setScale(in);
            return temp;
        } catch (Exception e) {
            exceptionPrinter.print(e, null, true);
        }
        return null;
    }

    public static ImageByte binaryDilate(ImageInt in, float radius, float radiusZ) {
        return binaryDilate(in, radius, radiusZ, 0);
    }

    public static ImageByte binaryDilate(ImageInt in, float radius, float radiusZ, int nbCPUs) {
        try {
            if (nbCPUs == 0) {
                nbCPUs = ThreadUtil.getNbCpus();
            }

            // test rad <=1
            if ((radius <= 1) && (radiusZ <= 1)) {
                return binaryDilateRad1(in, 1, nbCPUs);
            }

            int reX = (int) (radius + 1);
            int reY = (int) (radius + 1);
            int reZ = (int) (radiusZ + 1);
            ImageInt inResized = (ImageInt) in.resize(reX, reY, reZ);
            ImageFloat edm = EDT.run(inResized, 0, 1, radius / radiusZ, true, nbCPUs);
            //edm.duplicate().show("edm");
            ImageByte temp = edm.threshold(radius, true, false);
            //temp.show("edm dilate");
            edm.flush();
            edm = null;
            temp.setOffset(in);
            temp.offsetX -= reX;
            temp.offsetY -= reY;
            temp.offsetZ -= reZ;
            temp.setScale(in);
            return temp;
        } catch (Exception e) {
            exceptionPrinter.print(e, null, true);
        }
        return null;
    }

    public static ImageByte binaryClose(ImageInt in, float radius, float radiusZ) {
        return binaryClose(in, radius, radiusZ, 0);
    }

    public static ImageByte binaryClose(ImageInt in, float radius, float radiusZ, int nbCPUs) {
        try {
            if (nbCPUs == 0) {
                nbCPUs = ThreadUtil.getNbCpus();
            }
            // test rad <=1
            if ((radius <= 1) && (radiusZ <= 1)) {
                return binaryCloseRad1(in, 1, nbCPUs);
            }
            // FIXME thresholdings > strict or not??
            int rad = (int) radius + 1;
            int radZ = (int) radiusZ + 1;
            int resize = 0;
            ImageInt inResized = (ImageInt) in.resize(rad + resize, rad + resize, radZ + resize);
            ImageFloat edm = EDT.run(inResized, 0, 1, radius / radiusZ, true, nbCPUs);
            inResized.closeImagePlus();
            ImageByte inThresholded = edm.threshold(radius, true, false);
            edm.closeImagePlus();
            edm = EDT.run(inThresholded, 0, 1, radius / radiusZ, false, nbCPUs);
            inThresholded.closeImagePlus();
            ImageFloat edm2 = (ImageFloat) edm.resize(-rad, -rad, -radZ);
            edm.closeImagePlus();
            edm = null;
            System.gc();
            inThresholded = edm2.threshold(radius, false, true);
            edm2.closeImagePlus();
            edm2 = null;
            System.gc();
            inThresholded.offsetX = in.offsetX - resize;
            inThresholded.offsetY = in.offsetY - resize;
            inThresholded.offsetZ = in.offsetZ - resize;
            inThresholded.setScale(in);
            return inThresholded;
        } catch (Exception e) {
            exceptionPrinter.print(e, null, true);
        }
        return null;
    }

    private static ImageByte binaryOpenRad1(final ImageInt in, final float thld, int nbCPUs) {
        if (nbCPUs == 0) {
            nbCPUs = ThreadUtil.getNbCpus();
        }
        final ImageByte min = new ImageByte("min", in.sizeX, in.sizeY, in.sizeZ);
        final ThreadRunner tr = new ThreadRunner(0, in.sizeZ, nbCPUs);
        for (int i = 0; i < tr.threads.length; i++) {
            tr.threads[i] = new Thread(
                    new Runnable() {
                public void run() {
                    for (int z = tr.ai.getAndIncrement(); z < tr.end; z = tr.ai.getAndIncrement()) {
                        for (int y = 0; y < in.sizeY; y++) {
                            for (int x = 0; x < in.sizeX; x++) {
                                if (minRad1(in, thld, x, y, z)) {
                                    min.pixels[z][x + y * in.sizeX] = (byte) 255;
                                }
                            }
                        }
                    }
                }
            });
        }
        tr.startAndJoin();
        final ImageByte open = new ImageByte(in.getTitle() + "::open", in.sizeX, in.sizeY, in.sizeZ);
        final ThreadRunner tr2 = new ThreadRunner(0, in.sizeZ, nbCPUs);
        for (int i = 0; i < tr2.threads.length; i++) {
            tr2.threads[i] = new Thread(
                    new Runnable() {
                public void run() {
                    for (int z = tr2.ai.getAndIncrement(); z < tr2.end; z = tr2.ai.getAndIncrement()) {
                        for (int y = 0; y < in.sizeY; y++) {
                            for (int x = 0; x < in.sizeX; x++) {
                                if (maxRad1(min, 1, x, y, z)) {
                                    open.pixels[z][x + y * in.sizeX] = (byte) 255;
                                }
                            }
                        }
                    }
                }
            });
        }
        tr2.startAndJoin();
        min.closeImagePlus();
        open.setScale(in);
        open.setOffset(in);
        return open;
    }

    private static ImageByte binaryErodeRad1(final ImageInt in, final float thld, int nbCPUs) {
        if (nbCPUs == 0) {
            nbCPUs = ThreadUtil.getNbCpus();
        }
        final ImageByte min = new ImageByte("min", in.sizeX, in.sizeY, in.sizeZ);
        final ThreadRunner tr = new ThreadRunner(0, in.sizeZ, nbCPUs);
        for (int i = 0; i < tr.threads.length; i++) {
            tr.threads[i] = new Thread(
                    new Runnable() {
                public void run() {
                    for (int z = tr.ai.getAndIncrement(); z < tr.end; z = tr.ai.getAndIncrement()) {
                        for (int y = 0; y < in.sizeY; y++) {
                            for (int x = 0; x < in.sizeX; x++) {
                                if (minRad1(in, thld, x, y, z)) {
                                    min.pixels[z][x + y * in.sizeX] = (byte) 255;
                                }
                            }
                        }
                    }
                }
            });
        }
        tr.startAndJoin();
        min.setScale(in);
        min.setOffset(in);
        return min;
    }

    private static ImageByte binaryDilateRad1(final ImageInt in, final float thld, int nbCPUs) {
        if (nbCPUs == 0) {
            nbCPUs = ThreadUtil.getNbCpus();
        }
        final ImageByte max = new ImageByte("max", in.sizeX, in.sizeY, in.sizeZ);
        final ThreadRunner tr = new ThreadRunner(0, in.sizeZ, nbCPUs);
        for (int i = 0; i < tr.threads.length; i++) {
            tr.threads[i] = new Thread(
                    new Runnable() {
                public void run() {
                    for (int z = tr.ai.getAndIncrement(); z < tr.end; z = tr.ai.getAndIncrement()) {
                        for (int y = 0; y < in.sizeY; y++) {
                            for (int x = 0; x < in.sizeX; x++) {
                                if (maxRad1(in, thld, x, y, z)) {
                                    max.pixels[z][x + y * in.sizeX] = (byte) 255;
                                }
                            }
                        }
                    }
                }
            });
        }
        tr.startAndJoin();

        max.setScale(in);
        max.setOffset(in);

        return max;
    }

    private static ImageByte binaryCloseRad1(final ImageInt in, final float thld, int nbCPUs) {
        if (nbCPUs == 0) {
            nbCPUs = ThreadUtil.getNbCpus();
        }
        final ImageByte max = new ImageByte("max", in.sizeX, in.sizeY, in.sizeZ);
        final ThreadRunner tr = new ThreadRunner(0, in.sizeZ, nbCPUs);
        for (int i = 0; i < tr.threads.length; i++) {
            tr.threads[i] = new Thread(
                    new Runnable() {
                public void run() {
                    for (int z = tr.ai.getAndIncrement(); z < tr.end; z = tr.ai.getAndIncrement()) {
                        for (int y = 0; y < in.sizeY; y++) {
                            for (int x = 0; x < in.sizeX; x++) {
                                if (maxRad1(in, thld, x, y, z)) {
                                    max.pixels[z][x + y * in.sizeX] = (byte) 255;
                                }
                            }
                        }
                    }
                }
            });
        }
        tr.startAndJoin();
        final ThreadRunner tr2 = new ThreadRunner(0, in.sizeZ, nbCPUs);
        final ImageByte close = new ImageByte(in.getTitle() + "::close", in.sizeX, in.sizeY, in.sizeZ);
        for (int i = 0; i < tr2.threads.length; i++) {
            tr2.threads[i] = new Thread(
                    new Runnable() {
                public void run() {
                    for (int z = tr2.ai.getAndIncrement(); z < tr2.end; z = tr2.ai.getAndIncrement()) {
                        for (int y = 0; y < in.sizeY; y++) {
                            for (int x = 0; x < in.sizeX; x++) {
                                if (minRad1(max, 1, x, y, z)) {
                                    close.pixels[z][x + y * in.sizeX] = (byte) 255;
                                }
                            }
                        }
                    }
                }
            });
        }
        tr2.startAndJoin();
        max.closeImagePlus();
        close.setOffset(in);
        close.setScale(in);
        return close;
    }

    //returns true if the value is over thld && no pixel around is under thld && dont touch borders
    private static boolean minRad1(ImageInt in, float thld, int x, int y, int z) {
        if (in.getPixel(x, y, z) >= thld) {
            if (in.touchBorders(x, y, z)) {
                return false;
            }
            if (in.getPixel(x - 1, y, z) < thld) {
                return false;
            }
            if (in.getPixel(x + 1, y, z) < thld) {
                return false;
            }
            if (in.getPixel(x, y - 1, z) < thld) {
                return false;
            }
            if (in.getPixel(x, y + 1, z) < thld) {
                return false;
            }
            if (in.getPixel(x, y, z - 1) < thld) {
                return false;
            }
            if (in.getPixel(x, y, z + 1) < thld) {
                return false;
            }
            return true;
        } else {
            return false;
        }
    }

    //returns true if the value is over thld or at least 1 pixel around is over thld
    private static boolean maxRad1(ImageInt in, float thld, int x, int y, int z) {
        if (in.getPixel(x, y, z) < thld) {
            if (x > 0 && in.getPixel(x - 1, y, z) >= thld) {
                return true;
            }
            if (x < (in.sizeX - 1) && in.getPixel(x + 1, y, z) >= thld) {
                return true;
            }
            if (y > 0 && in.getPixel(x, y - 1, z) >= thld) {
                return true;
            }
            if (y < (in.sizeY - 1) && in.getPixel(x, y + 1, z) >= thld) {
                return true;
            }
            if (z > 0 && in.getPixel(x, y, z - 1) >= thld) {
                return true;
            }
            if (z < (in.sizeZ - 1) && in.getPixel(x, y, z + 1) >= thld) {
                return true;
            }
            return false;
        } else {
            return true;
        }
    }

    public static ImageInt binaryOpenMultilabel(ImageInt in, float radius, float radiusZ) {
        return binaryOpenMultilabel(in, radius, radiusZ, 0);

    }

    public static ImageInt binaryOpenMultilabel(ImageInt in, float radius, float radiusZ, int nbCPUs) {
        ImageByte[] ihs = in.crop3DBinary();
        if (ihs != null) {
            for (int idx = 0; idx < ihs.length; idx++) {
                if (radius <= 1 && radiusZ <= 1) {
                    ihs[idx] = BinaryMorpho.binaryOpenRad1(ihs[idx], 1, nbCPUs);
                } else {
                    ihs[idx] = binaryOpen(ihs[idx], radius, radiusZ, nbCPUs);
                }
            }
            ImageInt temp = ImageShort.merge3DBinary(ihs, in.sizeX, in.sizeY, in.sizeZ);
            temp.setScale(in);
            return temp;
        }
        return in;
    }

    public static ImageInt binaryCloseMultilabel(ImageInt in, float radiusXY, float radiusZ) {
        return binaryCloseMultilabel(in, radiusXY, radiusZ, 0);
    }

    public static ImageInt binaryCloseMultilabel(ImageInt in, float radiusXY, float radiusZ, int nbCPUs) {
        ImageByte[] ihs = in.crop3DBinary();
        if (ihs != null) {
            //ij.IJ.log("BinaryClose multilabel nb :"+ihs.length);
            for (int idx = 0; idx < ihs.length; idx++) {
                if (radiusXY <= 1 && radiusZ <= 1) {
                    ihs[idx] = BinaryMorpho.binaryCloseRad1(ihs[idx], 1, nbCPUs);
                } else {
                    ihs[idx] = (ImageByte) binaryClose(ihs[idx], radiusXY, radiusZ, nbCPUs);
                }
            }
            ImageInt temp = ImageShort.merge3DBinary(ihs, in.sizeX, in.sizeY, in.sizeZ);
            temp.setScale(in);
            return temp;
        }
        return in;
    }

    public static ImageInt binaryDilateMultilabel(ImageInt in, float radiusXY, float radiusZ) {
        return binaryDilateMultilabel(in, radiusXY, radiusZ, 0);
    }

    public static ImageInt binaryDilateMultilabel(ImageInt in, float radiusXY, float radiusZ, int nbCPUs) {
        //IJ.log("Binary multi dilate");
        ImageByte[] ihs = in.crop3DBinary();
        if (ihs != null) {
            //ij.IJ.log("BinaryClose multilabel nb :"+ihs.length);
            //ihs[0].show("crop binary 0");
            int end = ihs.length;
            for (int idx = 0; idx < end; idx++) {
                ihs[idx] = (ImageByte) binaryDilate(ihs[idx], radiusXY, radiusZ, nbCPUs);
            }
            //ihs[0].show("crop binary 0 dilated");
            ImageInt temp = ImageShort.merge3DBinary(ihs, in.sizeX, in.sizeY, in.sizeZ);
            temp.setScale(in);
            return temp;
        }
        return in;
    }

    public static ImageInt binaryCloseMultilabel(ImageInt in, float[] radiusXY, float[] radiusZ, int nbCPUs) {
        ImageByte[] ihs = in.crop3DBinary();
        if ((radiusXY.length != ihs.length) || (radiusZ.length != ihs.length)) {
            return null;
        }
        if (ihs != null) {
            //ij.IJ.log("BinaryClose multilabel nb :"+ihs.length);
            for (int idx = 0; idx < ihs.length; idx++) {
                if (radiusXY[idx] <= 1 && radiusZ[idx] <= 1) {
                    // FIXME pb if dilate, only performs closing
                    ihs[idx] = BinaryMorpho.binaryCloseRad1(ihs[idx], 1, nbCPUs);
                } else {
                    ihs[idx] = (ImageByte) binaryClose(ihs[idx], radiusXY[idx], radiusZ[idx], nbCPUs);
                }
            }
            ImageInt temp = ImageShort.merge3DBinary(ihs, in.sizeX, in.sizeY, in.sizeZ);
            temp.setScale(in);
            return temp;
        }
        return in;
    }

    public static ImageInt binaryDilateMultilabel(ImageInt in, float[] radiusXY, float[] radiusZ, int nbCPUs) {
        ImageByte[] ihs = in.crop3DBinary();
        if ((radiusXY.length != ihs.length) || (radiusZ.length != ihs.length)) {
            return null;
        }
        if (ihs != null) {
            //ij.IJ.log("BinaryClose multilabel nb :"+ihs.length);
            for (int idx = 0; idx < ihs.length; idx++) {
                ihs[idx] = (ImageByte) binaryDilate(ihs[idx], radiusXY[idx], radiusZ[idx], nbCPUs);
            }
            ImageInt temp = ImageShort.merge3DBinary(ihs, in.sizeX, in.sizeY, in.sizeZ);
            temp.setScale(in);
            return temp;
        }
        return in;
    }
}
