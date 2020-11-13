package mcib3d.image3d.processing;

import mcib3d.image3d.*;
import mcib3d.image3d.distanceMap3d.EDT;
import mcib3d.utils.ThreadRunner;
import mcib3d.utils.ThreadUtil;
import mcib3d.utils.exceptionPrinter;

/**
 * *
 * /**
 * Copyright (C) 2012 Jean Ollion
 * <p>
 * <p>
 * <p>
 * This file is part of tango
 * <p>
 * tango is free software; you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 3 of the License, or (at your option) any later
 * version.
 * <p>
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * <p>
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

    public static ImageByte binaryMorpho(ImageHandler in, int op, float radius, float radiusZ, int nbCPUs) {
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

    public static ImageByte binaryOpen(ImageHandler in, float radius, float radiusZ) {
        return binaryOpen(in, radius, radiusZ, 0);
    }

    public static ImageByte binaryOpen(ImageHandler in, float radius, float radiusZ, int nbCPUs) {
        try {
            if (nbCPUs == 0) {
                nbCPUs = ThreadUtil.getNbCpus();
            }

            // test rad <=1
            /*if ((radius <= 1) && (radiusZ <= 1)) {
             return binaryOpenRad1(in, 1, nbCPUs);
             }*/
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

    public static ImageByte binaryErode(ImageHandler in, float radius, float radiusZ, int nbCPUs) {
        try {
            if (nbCPUs == 0) {
                nbCPUs = ThreadUtil.getNbCpus();
            }

            // test rad <=1
            /*if ((radius <= 1) && (radiusZ <= 1)) {
             return binaryErodeRad1(in, 1, nbCPUs);
             }*/
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

    public static ImageByte binaryDilate2D(ImageByte in, float radius, boolean enlarge) {
        ImageInt resize = in;
        // resize
        int reX = (int) (radius + 1);
        int reY = (int) (radius + 1);
        int reZ = 0;
        if (enlarge) resize = (ImageInt) in.enlarge(reX, reY, reZ);
        ImageByte temp = (ImageByte) FastFilters3D.filterIntImage(resize, FastFilters3D.MAX, radius, radius, 0, 1, false);
        temp.setScale(in);
        if (enlarge)
            temp.setOffset(in.offsetX - reX, in.offsetY - reY, in.offsetZ - reZ);
        else
            temp.setOffset(in);

        return temp;
    }

    public static ImageByte binaryErode2D(ImageByte in, float radius) {
        ImageByte temp = (ImageByte) FastFilters3D.filterIntImage(in, FastFilters3D.MIN, radius, radius, 0, 1, false);
        temp.setOffset(in);
        temp.setScale(in);

        return temp;
    }


    public static ImageByte binaryDilate(ImageInt in, float radius, float radiusZ) {
        return binaryDilate(in, radius, radiusZ, 0);
    }

    // if no resize of the image, object at the border may be truncated
    public static ImageByte binaryDilate(ImageHandler in, float radius, float radiusZ, int nbCPUs, boolean enlarge) {
        try {
            if (nbCPUs == 0) {
                nbCPUs = ThreadUtil.getNbCpus();
            }

            ImageHandler resize = in;

            // resize
            int reX = (int) (radius + 1);
            int reY = (int) (radius + 1);
            int reZ = (int) (radiusZ + 1);
            if (enlarge) resize = in.enlarge(reX, reY, reZ);

            ImageFloat edm = EDT.run(resize, 0, 1, radius / radiusZ, true, nbCPUs);
            //edm.duplicate().show("edm");
            ImageByte temp = edm.threshold(radius, true, false);
            //temp.show("thres");
            edm.flush();
            edm = null;
            if (enlarge)
                temp.setOffset(in.offsetX - reX, in.offsetY - reY, in.offsetZ - reZ);
            else
                temp.setOffset(in);
            temp.setScale(in);

            return temp;
        } catch (Exception e) {
            exceptionPrinter.print(e, null, true);
        }
        return null;
    }


    public static ImageByte binaryDilate(ImageHandler in, float radius, float radiusZ, int nbCPUs) {
        // use generic version of binaryDilate
        return binaryDilate(in, radius, radiusZ, nbCPUs, true);

        /*
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
            temp.offsetX = in.offsetX - reX;
            temp.offsetY = in.offsetY - reY;
            temp.offsetZ = in.offsetZ - reZ;
            temp.setScale(in);
            // no more resize, should use crop

            return temp;
        } catch (Exception e) {
            exceptionPrinter.print(e, null, true);
        }
        return null;
        */
    }

    public static ImageByte binaryClose(ImageInt in, float radius, float radiusZ) {
        return binaryClose(in, radius, radiusZ, 0);
    }

    private static ImageByte binaryClose2D(ImageByte in, float radius) {
        ImageByte dilated = binaryDilate2D(in, radius, true);
        ImageByte close = binaryErode2D(dilated, radius);
        // crop image
        int ox = in.offsetX - dilated.offsetX;
        int oy = in.offsetY - dilated.offsetY;
        int oz = in.offsetZ - dilated.offsetZ;
        return close.crop3D("binaryClose", ox, ox + in.sizeX - 1, oy, oy + in.sizeY - 1, oz, oz + in.sizeZ - 1);
    }

    public static ImageByte binaryClose(ImageHandler in, float radius, float radiusZ, int nbCPUs) {
        // use binary dilate
        ImageByte dilated = binaryDilate(in, radius, radiusZ, nbCPUs, true);
        ImageByte close = binaryErode(dilated, radius, radiusZ, nbCPUs);
        // crop image
        int ox = in.offsetX - dilated.offsetX;
        int oy = in.offsetY - dilated.offsetY;
        int oz = in.offsetZ - dilated.offsetZ;
        return close.crop3D("binaryClose", ox, ox + in.sizeX - 1, oy, oy + in.sizeY - 1, oz, oz + in.sizeZ - 1);

        /*
        try {
            if (nbCPUs == 0) {
                nbCPUs = ThreadUtil.getNbCpus();
            }
            // test rad <=1
            /*if ((radius <= 1) && (radiusZ <= 1)) {
             return binaryCloseRad1(in, 1, nbCPUs);
             }*/
        /*
            // FIXME thresholdings > strict
            int rad = (int) (radius + 1);
            int radZ = (int) (radiusZ + 1);
            int resize = 0; // FIXME useful ??
            ImageInt inResized = (ImageInt) in.resize(rad + resize, rad + resize, radZ + resize);
            //inResized.show("resize");
            ImageFloat edm = EDT.run(inResized, 0, 1, radius / radiusZ, true, nbCPUs);
            inResized.closeImagePlus();
            ImageByte inThresholded = edm.threshold(radius, true, false);
            edm.closeImagePlus();
            edm = EDT.run(inThresholded, 0, 1, radius / radiusZ, false, nbCPUs);
            //edm.show("edm");
            inThresholded.closeImagePlus();
            //ImageFloat edm2 = (ImageFloat) edm.resize(-rad+resize, -rad+resize, -radZ+resize);
            //edm.closeImagePlus();
            //edm = null;
            //System.gc();
            inThresholded = edm.threshold(radius, false, false);
            // inThresholded.show("bin");
            edm.closeImagePlus();
            edm = null;
            System.gc();
            inThresholded.offsetX = inResized.offsetX;
            inThresholded.offsetY = inResized.offsetY;
            inThresholded.offsetZ = inResized.offsetZ;
            inThresholded.setScale(in);
            return inThresholded;
        } catch (Exception e) {
            exceptionPrinter.print(e, null, true);
        }
        return null;
        */
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
                        @Override
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
                        @Override
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
                        @Override
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

    private static ImageByte binaryDilateRad1(final ImageInt in_, final float thld, int nbCPUs) {
        if (nbCPUs == 0) {
            nbCPUs = ThreadUtil.getNbCpus();
        }
        final ImageInt in = (ImageInt) in_.resize(1, 1, 1);
        final ImageByte max = new ImageByte("max", in.sizeX, in.sizeY, in.sizeZ);
        final ThreadRunner tr = new ThreadRunner(0, in.sizeZ, nbCPUs);
        for (int i = 0; i < tr.threads.length; i++) {
            tr.threads[i] = new Thread(
                    new Runnable() {
                        @Override
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

    private static ImageByte binaryDilateRad1diag(final ImageInt in_, final float thld, int nbCPUs) {
        if (nbCPUs == 0) {
            nbCPUs = ThreadUtil.getNbCpus();
        }
        final ImageInt in = (ImageInt) in_.resize(1, 1, 1);
        final ImageByte max = new ImageByte("max", in.sizeX, in.sizeY, in.sizeZ);
        final ThreadRunner tr = new ThreadRunner(0, in.sizeZ, nbCPUs);
        for (int i = 0; i < tr.threads.length; i++) {
            tr.threads[i] = new Thread(
                    new Runnable() {
                        @Override
                        public void run() {
                            for (int z = tr.ai.getAndIncrement(); z < tr.end; z = tr.ai.getAndIncrement()) {
                                for (int y = 0; y < in.sizeY; y++) {
                                    for (int x = 0; x < in.sizeX; x++) {
                                        if (maxRad15(in, thld, x, y, z)) {
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

    private static ImageByte binaryCloseRad1(final ImageInt in_, final float thld, int nbCPUs) {
        if (nbCPUs == 0) {
            nbCPUs = ThreadUtil.getNbCpus();
        }
        final ImageInt in = (ImageInt) in_.resize(1, 1, 1); // TODO: faire sans resize avec un simple décalage des indices
        final ImageByte max = new ImageByte("max", in.sizeX, in.sizeY, in.sizeZ);
        final ThreadRunner tr = new ThreadRunner(0, max.sizeZ, nbCPUs);
        for (int i = 0; i < tr.threads.length; i++) {
            tr.threads[i] = new Thread(
                    new Runnable() {
                        @Override
                        public void run() {
                            for (int z = tr.ai.getAndIncrement(); z < tr.end; z = tr.ai.getAndIncrement()) {
                                for (int y = 0; y < max.sizeY; y++) {
                                    for (int x = 0; x < max.sizeX; x++) {
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

    private static ImageByte binaryCloseRad1diag(final ImageInt in_, final float thld, int nbCPUs) {
        if (nbCPUs == 0) {
            nbCPUs = ThreadUtil.getNbCpus();
        }
        final ImageInt in = (ImageInt) in_.resize(1, 1, 1); // TODO: faire sans resize avec un simple décalage des indices
        final ImageByte max = new ImageByte("max", in.sizeX, in.sizeY, in.sizeZ);
        final ThreadRunner tr = new ThreadRunner(0, max.sizeZ, nbCPUs);
        for (int i = 0; i < tr.threads.length; i++) {
            tr.threads[i] = new Thread(
                    new Runnable() {
                        @Override
                        public void run() {
                            for (int z = tr.ai.getAndIncrement(); z < tr.end; z = tr.ai.getAndIncrement()) {
                                for (int y = 0; y < max.sizeY; y++) {
                                    for (int x = 0; x < max.sizeX; x++) {
                                        if (maxRad15(in, thld, x, y, z)) {
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
                        @Override
                        public void run() {
                            for (int z = tr2.ai.getAndIncrement(); z < tr2.end; z = tr2.ai.getAndIncrement()) {
                                for (int y = 0; y < in.sizeY; y++) {
                                    for (int x = 0; x < in.sizeX; x++) {
                                        if (minRad15(max, 1, x, y, z)) {
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

    //returns true if the value is over thld && no pixel around is under thld && dont touch borders (outside border -> value = 0)
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
            return !(in.getPixel(x, y, z + 1) < thld);
        } else {
            return false;
        }
    }

    private static boolean minRad15(ImageInt in, float thld, int x, int y, int z) {
        if (in.getPixel(x, y, z) >= thld) {
            if (in.touchBorders(x, y, z)) {
                return false;
            }
            for (int zz = z - 1; zz <= z + 1; zz++) {
                for (int yy = y - 1; yy <= y + 1; yy++) {
                    for (int xx = x - 1; xx <= x + 1; xx++) {
                        if (in.getPixel(xx, yy, zz) < thld) {
                            return false;
                        }
                    }
                }
            }
            return true;
        } else {
            return false;
        }
    }

    private static boolean maxRad15(ImageInt in, float thld, int x, int y, int z) {
        if (in.getPixel(x, y, z) < thld) {
            for (int zz = z - 1; zz <= z + 1; zz++) {
                for (int yy = y - 1; yy <= y + 1; yy++) {
                    for (int xx = x - 1; xx <= x + 1; xx++) {
                        if (in.contains(xx, yy, zz) && in.getPixel(xx, yy, zz) >= thld) {
                            return true;
                        }
                    }
                }
            }
            return false;
        } else {
            return true;
        }
    }

    private static boolean maxRad1(ImageInt in, float thld, int x, int y, int z) {
        if (in.getPixel(x, y, z) < thld) {
            if (x > 0 && in.getPixel(x - 1, y, z) >= thld) {
                return true;
            }
            if ((x + 1) < in.sizeX && in.getPixel(x + 1, y, z) >= thld) {
                return true;
            }
            if (y > 0 && in.getPixel(x, y - 1, z) >= thld) {
                return true;
            }
            if ((y + 1) < in.sizeY && in.getPixel(x, y + 1, z) >= thld) {
                return true;
            }
            if (z > 0 && in.getPixel(x, y, z - 1) >= thld) {
                return true;
            }
            return (z + 1) < in.sizeZ && in.getPixel(x, y, z + 1) >= thld;
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
                /*if (radius <= 1 && radiusZ <= 1) {
                 ihs[idx] = BinaryMorpho.binaryOpenRad1(ihs[idx], 1, nbCPUs);
                 } else {
                 ihs[idx] = binaryOpen(ihs[idx], radius, radiusZ, nbCPUs);
                 }*/
                ihs[idx] = binaryOpen(ihs[idx], radius, radiusZ, nbCPUs);
            }
            ImageInt temp = ImageShort.merge3DBinary(ihs, in.sizeX, in.sizeY, in.sizeZ);
            temp.setScale(in);
            temp.setOffset(in);
            return temp;
        }
        return in;
    }

    public static ImageInt binaryCloseMultilabel(ImageInt in, float radiusXY, float radiusZ) {
        return binaryCloseMultilabel(in, radiusXY, radiusZ, 0);
    }

    public static ImageInt binaryCloseMultilabel(ImageInt in, float radiusXY, float radiusZ, int nbCPUs) {
        // FIXME should get same label as input
        ImageByte[] ihs = in.crop3DBinary();
        if (ihs != null) {
            //ij.IJ.log("BinaryClose multilabel nb :"+ihs.length);
            for (int idx = 0; idx < ihs.length; idx++) {
                if (radiusXY < 1 && radiusZ < 1) {
                    ihs[idx] = binaryCloseRad1(ihs[idx], 1, nbCPUs);
                } else if (radiusXY < 2 && radiusZ < 2) {
                    ihs[idx] = binaryCloseRad1diag(ihs[idx], 1, nbCPUs);
                } else if (radiusZ == 0) {
                    ihs[idx] = binaryClose2D(ihs[idx], radiusXY);
                } else {
                    ihs[idx] = binaryClose(ihs[idx], radiusXY, radiusZ, nbCPUs);
                }
            }
            ImageInt temp = ImageShort.merge3DBinary(ihs, in.sizeX, in.sizeY, in.sizeZ);
            temp.setScale(in);
            temp.setOffset(in);

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
                if (radiusXY < 1 && radiusZ < 1) {
                    ihs[idx] = binaryDilateRad1(ihs[idx], 1, nbCPUs);
                } else if (radiusXY < 2 && radiusZ < 2) {
                    ihs[idx] = binaryDilateRad1diag(ihs[idx], 1, nbCPUs);
                } else {
                    ihs[idx] = binaryDilate(ihs[idx], radiusXY, radiusZ, nbCPUs);
                }
            }
            ImageInt temp = ImageShort.merge3DBinary(ihs, in.sizeX, in.sizeY, in.sizeZ);
            temp.setScale(in);
            temp.setOffset(in);
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
                    ihs[idx] = BinaryMorpho.binaryCloseRad1(ihs[idx], 1, nbCPUs);
                } else {
                    ihs[idx] = binaryClose(ihs[idx], radiusXY[idx], radiusZ[idx], nbCPUs);
                }
                ihs[idx] = binaryClose(ihs[idx], radiusXY[idx], radiusZ[idx], nbCPUs);
            }
            ImageInt temp = ImageShort.merge3DBinary(ihs, in.sizeX, in.sizeY, in.sizeZ);
            temp.setScale(in);
            temp.setOffset(in);
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
                ihs[idx] = binaryDilate(ihs[idx], radiusXY[idx], radiusZ[idx], nbCPUs);
            }
            ImageInt temp = ImageShort.merge3DBinary(ihs, in.sizeX, in.sizeY, in.sizeZ);
            temp.setScale(in);
            temp.setOffset(in);
            return temp;
        }
        return in;
    }
}
