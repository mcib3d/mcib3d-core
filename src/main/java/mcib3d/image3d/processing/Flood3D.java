package mcib3d.image3d.processing;

import ij.IJ;
import mcib3d.geom.IntCoord3D;
import mcib3d.image3d.ImageByte;
import mcib3d.image3d.ImageHandler;
import mcib3d.image3d.ImageInt;
import mcib3d.image3d.ImageShort;

import java.util.ArrayList;

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
public class Flood3D {
    public static void flood3d6(ImageInt img, int seedX, int seedY, int seedZ, int newVal) {
        IntCoord3D seed = new IntCoord3D(seedX, seedY, seedZ);
        if (img instanceof ImageShort) {
            flood3DShort6((ImageShort) img, seed, (short) newVal);
        } else if (img instanceof ImageByte) {
            flood3DByte6((ImageByte) img, seed, (byte) newVal);
        }
    }

    public static void flood3d26(ImageInt img, int seedX, int seedY, int seedZ, int newVal) {
        IntCoord3D seed = new IntCoord3D(seedX, seedY, seedZ);
        if (img instanceof ImageShort) {
            flood3DShort26((ImageShort) img, seed, (short) newVal);
        } else if (img instanceof ImageByte) {
            flood3DByte26((ImageByte) img, seed, (byte) newVal);
        }
    }

    private static void flood3DShort6(ImageShort img, IntCoord3D seed, short newVal) {
        short[][] pixels = img.pixels;
        int sizeX = img.sizeX;
        int sizeY = img.sizeY;
        int sizeZ = img.sizeZ;
        short oldVal = pixels[seed.z][seed.x + seed.y * sizeX];
        // tmp image to store queue pixels
        short CAND = 1;
        short QUEUE = 2;

        short[][] temp = img.duplicate().pixels;
        for (int z = 0; z < img.sizeZ; z++) {
            for (int xy = 0; xy < img.sizeXY; xy++) {
                if (temp[z][xy] == oldVal) {
                    temp[z][xy] = CAND;
                }
            }
        }
        ArrayList<IntCoord3D> queue = new ArrayList<IntCoord3D>();
        queue.add(seed);
        while (!queue.isEmpty()) {
            IntCoord3D curCoord = queue.remove(0);
            int xy = curCoord.x + curCoord.y * sizeX;
            pixels[curCoord.z][xy] = newVal;
            if (curCoord.x > 0 && temp[curCoord.z][xy - 1] == CAND) {
                queue.add(new IntCoord3D(curCoord.x - 1, curCoord.y, curCoord.z));
                temp[curCoord.z][xy - 1] = QUEUE;
            }
            if (curCoord.x < (sizeX - 1) && temp[curCoord.z][xy + 1] == CAND) {
                queue.add(new IntCoord3D(curCoord.x + 1, curCoord.y, curCoord.z));
                temp[curCoord.z][xy + 1] = QUEUE;
            }
            if (curCoord.y > 0 && temp[curCoord.z][xy - sizeX] == CAND) {
                queue.add(new IntCoord3D(curCoord.x, curCoord.y - 1, curCoord.z));
                temp[curCoord.z][xy - sizeX] = QUEUE;
            }
            if (curCoord.y < (sizeY - 1) && temp[curCoord.z][xy + sizeX] == CAND) {
                queue.add(new IntCoord3D(curCoord.x, curCoord.y + 1, curCoord.z));
                temp[curCoord.z][xy + sizeX] = QUEUE;
            }
            if (curCoord.z > 0 && temp[curCoord.z - 1][xy] == CAND) {
                queue.add(new IntCoord3D(curCoord.x, curCoord.y, curCoord.z - 1));
                temp[curCoord.z - 1][xy] = QUEUE;
            }
            if (curCoord.z < (sizeZ - 1) && temp[curCoord.z + 1][xy] == CAND) {
                queue.add(new IntCoord3D(curCoord.x, curCoord.y, curCoord.z + 1));
                temp[curCoord.z + 1][xy] = QUEUE;
            }
        }
    }

    @Deprecated
    private static void flood3DNoiseShort6(ImageShort img, IntCoord3D seed, short limit, short newVal) {
        short[][] pixels = img.pixels;
        int sizeX = img.sizeX;
        int sizeY = img.sizeY;
        int sizeZ = img.sizeZ;
        int oldVal = pixels[seed.z][seed.x + seed.y * sizeX];
        //int limit=oldVal-noise;
        ArrayList<IntCoord3D> queue = new ArrayList<IntCoord3D>();
        queue.add(seed);
        while (!queue.isEmpty()) {
            IntCoord3D curCoord = queue.remove(0); // FIXME last element?
            IJ.log("processing " + curCoord.x + " " + curCoord.y + " " + curCoord.z + " " + oldVal + " " + limit);
            int xy = curCoord.x + curCoord.y * sizeX;
            if (pixels[curCoord.z][xy] >= limit) {
                pixels[curCoord.z][xy] = newVal;
                if (curCoord.x > 0 && pixels[curCoord.z][xy - 1] >= limit) {
                    queue.add(new IntCoord3D(curCoord.x - 1, curCoord.y, curCoord.z));
                }
                if (curCoord.x < (sizeX - 1) && pixels[curCoord.z][xy + 1] >= limit) {
                    queue.add(new IntCoord3D(curCoord.x + 1, curCoord.y, curCoord.z));
                }
                if (curCoord.y > 0 && pixels[curCoord.z][xy - sizeX] >= limit) {
                    queue.add(new IntCoord3D(curCoord.x, curCoord.y - 1, curCoord.z));
                }
                if (curCoord.y < (sizeY - 1) && pixels[curCoord.z][xy + sizeX] >= limit) {
                    queue.add(new IntCoord3D(curCoord.x, curCoord.y + 1, curCoord.z));
                }
                if (curCoord.z > 0 && pixels[curCoord.z - 1][xy] >= limit) {
                    queue.add(new IntCoord3D(curCoord.x, curCoord.y, curCoord.z - 1));
                }
                if (curCoord.z < (sizeZ - 1) && pixels[curCoord.z + 1][xy] >= limit) {
                    queue.add(new IntCoord3D(curCoord.x, curCoord.y, curCoord.z + 1));
                }
            }
        }
    }

    private static void flood3DByte6(ImageByte img, IntCoord3D seed, byte newVal) {
        byte[][] pixels = img.pixels;
        int sizeX = img.sizeX;
        int sizeY = img.sizeY;
        int sizeZ = img.sizeZ;
        short oldVal = pixels[seed.z][seed.x + seed.y * sizeX];
        // tmp image to store queue pixels
        byte CAND = 1;
        byte QUEUE = 2;

        byte[][] temp = img.duplicate().pixels;
        for (int z = 0; z < img.sizeZ; z++) {
            for (int xy = 0; xy < img.sizeXY; xy++) {
                if (temp[z][xy] == oldVal) {
                    temp[z][xy] = CAND;
                }
            }
        }
        ArrayList<IntCoord3D> queue = new ArrayList<IntCoord3D>();
        queue.add(seed);
        while (!queue.isEmpty()) {
            IntCoord3D curCoord = queue.remove(0);
            int xy = curCoord.x + curCoord.y * sizeX;
            pixels[curCoord.z][xy] = newVal;
            if (curCoord.x > 0 && temp[curCoord.z][xy - 1] == CAND) {
                queue.add(new IntCoord3D(curCoord.x - 1, curCoord.y, curCoord.z));
                temp[curCoord.z][xy - 1] = QUEUE;
            }
            if (curCoord.x < (sizeX - 1) && temp[curCoord.z][xy + 1] == CAND) {
                queue.add(new IntCoord3D(curCoord.x + 1, curCoord.y, curCoord.z));
                temp[curCoord.z][xy + 1] = QUEUE;
            }
            if (curCoord.y > 0 && temp[curCoord.z][xy - sizeX] == CAND) {
                queue.add(new IntCoord3D(curCoord.x, curCoord.y - 1, curCoord.z));
                temp[curCoord.z][xy - sizeX] = QUEUE;
            }
            if (curCoord.y < (sizeY - 1) && temp[curCoord.z][xy + sizeX] == CAND) {
                queue.add(new IntCoord3D(curCoord.x, curCoord.y + 1, curCoord.z));
                temp[curCoord.z][xy + sizeX] = QUEUE;
            }
            if (curCoord.z > 0 && temp[curCoord.z - 1][xy] == CAND) {
                queue.add(new IntCoord3D(curCoord.x, curCoord.y, curCoord.z - 1));
                temp[curCoord.z - 1][xy] = QUEUE;
            }
            if (curCoord.z < (sizeZ - 1) && temp[curCoord.z + 1][xy] == CAND) {
                queue.add(new IntCoord3D(curCoord.x, curCoord.y, curCoord.z + 1));
                temp[curCoord.z + 1][xy] = QUEUE;
            }
        }
    }

    private static void flood3DShort26(ImageShort img, IntCoord3D seed, short newVal) {
        short[][] pixels = img.pixels;
        int sizeX = img.sizeX;
        int sizeY = img.sizeY;
        int sizeZ = img.sizeZ;
        short oldVal = pixels[seed.z][seed.x + seed.y * sizeX];
        // tmp image to store queue pixels
        short CAND = 1;
        short QUEUE = 2;

        short[][] temp = img.duplicate().pixels;
        for (int z = 0; z < img.sizeZ; z++) {
            for (int xy = 0; xy < img.sizeXY; xy++) {
                if (temp[z][xy] == oldVal) {
                    temp[z][xy] = CAND;
                }
            }
        }
        ArrayList<IntCoord3D> queue = new ArrayList<IntCoord3D>();
        queue.add(seed);
        while (!queue.isEmpty()) {
            IntCoord3D curCoord = queue.remove(0);
            int xy = curCoord.x + curCoord.y * sizeX;
            pixels[curCoord.z][xy] = newVal;
            int curZ, curY, curX;
            for (int zz = -1; zz < 2; zz++) {
                curZ = curCoord.z + zz;
                if (curZ > 0 && curZ < (sizeZ - 1)) {
                    for (int yy = -1; yy < 2; yy++) {
                        curY = curCoord.y + yy;
                        if (curY > 0 && curY < (sizeY - 1)) {
                            for (int xx = -1; xx < 2; xx++) {
                                curX = curCoord.x + xx;
                                if (curX > 0 && curX < (sizeX - 1) && (xx != 0 || yy != 0 || zz != 0)) {
                                    if (temp[curZ][curX + curY * sizeX] == CAND) {
                                        queue.add(new IntCoord3D(curX, curY, curZ));
                                        temp[curZ][curX + curY * sizeX] = QUEUE;
                                    }
                                }
                            }
                        }
                    }
                }
            }

        }
    }

    public static void flood3DNoise(ImageHandler img, IntCoord3D seed, int limit, int newVal) {
        if (img instanceof ImageByte)
            flood3DNoise26Byte((ImageByte) img, seed, limit, newVal);
        else if (img instanceof ImageShort)
            flood3DNoise26Short((ImageShort) img, seed, limit, newVal);
    }

    private static void flood3DNoise26Byte(ImageByte img, IntCoord3D seed, int limit, int newVal) {
        // temp image
        byte CAND = 1;
        byte QUEUE = 2;
        byte[][] temp = img.duplicate().pixels;
        for (int z = 0; z < img.sizeZ; z++) {
            for (int xy = 0; xy < img.sizeXY; xy++) {
                if (img.getPixel(xy, z) >= limit) {
                    temp[z][xy] = CAND;
                }
            }
        }
        int sizeX = img.sizeX;
        int sizeY = img.sizeY;
        int sizeZ = img.sizeZ;
        ArrayList<IntCoord3D> queue = new ArrayList<IntCoord3D>();
        queue.add(seed);
        while (!queue.isEmpty()) {
            IntCoord3D curCoord = queue.remove(0);
            img.setPixel(curCoord.x, curCoord.y, curCoord.z, newVal);
            int curZ, curY, curX;
            for (int zz = -1; zz < 2; zz++) {
                curZ = curCoord.z + zz;
                if ((curZ >= 0) && (curZ <= (sizeZ - 1))) {
                    for (int yy = -1; yy < 2; yy++) {
                        curY = curCoord.y + yy;
                        if ((curY >= 0) && (curY <= (sizeY - 1))) {
                            for (int xx = -1; xx < 2; xx++) {
                                curX = curCoord.x + xx;
                                if ((curX >= 0) && (curX <= (sizeX - 1))) {
                                    if (temp[curZ][curX + curY * sizeX] == CAND) {
                                        queue.add(new IntCoord3D(curX, curY, curZ));
                                        temp[curZ][curX + curY * sizeX] = QUEUE;
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    public static void flood3DNoise26Short(ImageShort img, IntCoord3D seed, int limit, int newVal) {
        // temp image
        short CAND = 1;
        short QUEUE = 2;
        short[][] temp = img.duplicate().pixels;
        for (int z = 0; z < img.sizeZ; z++) {
            for (int xy = 0; xy < img.sizeXY; xy++) {
                if (img.getPixel(xy, z) >= limit) {
                    temp[z][xy] = CAND;
                }
            }
        }
        int sizeX = img.sizeX;
        int sizeY = img.sizeY;
        int sizeZ = img.sizeZ;
        ArrayList<IntCoord3D> queue = new ArrayList<IntCoord3D>();
        queue.add(seed);
        while (!queue.isEmpty()) {
            IntCoord3D curCoord = queue.remove(0);
            img.setPixel(curCoord.x, curCoord.y, curCoord.z, newVal);
            int curZ, curY, curX;
            for (int zz = -1; zz < 2; zz++) {
                curZ = curCoord.z + zz;
                if ((curZ >= 0) && (curZ <= (sizeZ - 1))) {
                    for (int yy = -1; yy < 2; yy++) {
                        curY = curCoord.y + yy;
                        if ((curY >= 0) && (curY <= (sizeY - 1))) {
                            for (int xx = -1; xx < 2; xx++) {
                                curX = curCoord.x + xx;
                                if ((curX >= 0) && (curX <= (sizeX - 1))) {
                                    if (temp[curZ][curX + curY * sizeX] == CAND) {
                                        queue.add(new IntCoord3D(curX, curY, curZ));
                                        temp[curZ][curX + curY * sizeX] = QUEUE;
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    public static void connect3D(ImageInt img, int lowVal, int highVal) {
        connect3D(img, lowVal, highVal, false);
    }

    public static void connect3D(ImageHandler img, int lowVal, int highVal, boolean log) {
        // find temporary value
        float tmpVal = 1;
        float c = img.getMinAboveValue(0);
        while (c == tmpVal) {
            tmpVal++;
            c = img.getMinAboveValue(c);
        }
        // loop over image to find high value pixels
        for (int z = 0; z < img.sizeZ; z++) {
            for (int x = 0; x < img.sizeX; x++) {
                for (int y = 0; y < img.sizeY; y++) {
                    if (img.getPixel(x, y, z) == highVal) {
                        // byte
                        if (img instanceof ImageByte) {
                            connect3DByte26((ImageByte) img, new IntCoord3D(x, y, z), (byte) lowVal, (byte) highVal, (byte) tmpVal, log);
                        }
                        // short
                        if (img instanceof ImageShort) {
                            connect3DShort26((ImageShort) img, new IntCoord3D(x, y, z), (short) lowVal, (short) highVal, (short) tmpVal, log);
                        }
                    }
                }
            }
        }
        // multi-threaded version
        //img = FastOperation3D.operationImage(img, FastOperation3D.INVERT, 255, 0);
        //IJ.log("" + tmpVal + " " + highVal + " " + lowVal);
        //ImageHandler replaced = FastOperation3D.operationImage(img, FastOperation3D.REPLACE, tmpVal, highVal);
        img.replacePixelsValue((int) tmpVal, highVal, lowVal, 0);
    }


    /**
     * Connect in 3D pixel with low values to pixel with high values
     * Pixels with low values not connected to pixels with high values will remain with low values
     *
     * @param img     The image to process, 8-bit image
     * @param seed    one seed to start the connection
     * @param highVal the high value
     * @param lowVal  the low value
     */
    public static void connect2DByteXY(ImageByte img, int zcoord, IntCoord3D seed, byte lowVal, byte highVal, byte tmpVal) {
        byte CAND = 1;
        byte QUEUE = 2;
        // tmp image to store queue pixels
        byte[][] temp = img.duplicate().pixels;
        for (int z = 0; z < img.sizeZ; z++) {
            for (int xy = 0; xy < img.sizeXY; xy++) {
                if ((temp[z][xy] == lowVal) || (temp[z][xy] == highVal)) {
                    temp[z][xy] = CAND;
                }
            }
        }
        long step = 500;
        byte[][] pixels = img.pixels;
        int sizeX = img.sizeX;
        int sizeY = img.sizeY;
        int sizeZ = img.sizeZ;
        ArrayList<IntCoord3D> queue = new ArrayList<IntCoord3D>();
        if (pixels[seed.z][seed.x + seed.y * sizeX] != highVal) {
            IJ.log("Seed not right value ");
            return;
        }
        // if seed not in this Z return
        if (seed.z != zcoord) return;
        // do flooding
        queue.add(seed);
        Long t0 = System.currentTimeMillis();
        while (!queue.isEmpty()) {
            if (System.currentTimeMillis() - t0 > step) {
                IJ.log("\\Update:Voxels to process : " + queue.size());
                t0 = System.currentTimeMillis();
            }
            IntCoord3D curCoord = queue.remove(0);
            int xy = curCoord.x + curCoord.y * sizeX;
            pixels[curCoord.z][xy] = tmpVal;
            int curZ, curY, curX;
            curZ = curCoord.z;
            for (int yy = -1; yy < 2; yy++) {
                curY = curCoord.y + yy;
                if (curY >= 0 && curY <= (sizeY - 1)) {
                    for (int xx = -1; xx < 2; xx++) {
                        curX = curCoord.x + xx;
                        if (curX >= 0 && curX <= (sizeX - 1)) {
                            if (temp[curZ][curY * sizeX + curX] == CAND) {
                                temp[curZ][curY * sizeX + curX] = QUEUE;
                                queue.add(new IntCoord3D(curX, curY, curZ));
                            }
                        }
                    }
                }
            }


        }
        temp = null;
    }

    /**
     * Connect in 3D pixel with low values to pixel with high values
     * Pixels with low values not connected to pixels with high values will remain with low values
     *
     * @param img     The image to process, 8-bit image
     * @param seed    one seed to start the connection
     * @param highVal the high value
     * @param lowVal  the low value
     */
    private static void connect3DByte26OK(ImageByte img, IntCoord3D seed, byte lowVal, byte highVal, byte tmpVal, boolean log) {
        byte CAND = 1;
        byte QUEUE = 2;
        // tmp image to store queue pixels
        byte[][] temp = img.duplicate().pixels;
        for (int z = 0; z < img.sizeZ; z++) {
            for (int xy = 0; xy < img.sizeXY; xy++) {
                if ((temp[z][xy] == lowVal) || (temp[z][xy] == highVal)) {
                    temp[z][xy] = CAND;
                }
            }
        }
        long step = 50;
        byte[][] pixels = img.pixels;
        int sizeX = img.sizeX;
        int sizeY = img.sizeY;
        int sizeZ = img.sizeZ;
        ArrayList<IntCoord3D> queue = new ArrayList<IntCoord3D>();
        if (pixels[seed.z][seed.x + seed.y * sizeX] != highVal) {
            IJ.log("Seed not right value ");
            return;
        }
        queue.add(seed);
        Long t0 = System.currentTimeMillis();
        // test
        log = false;
        IJ.log("flooding started for " + seed.x + " " + seed.y + " " + seed.z);
        // test
        while (!queue.isEmpty()) {
            if (log && (System.currentTimeMillis() - t0 > step)) {
                IJ.log("\\Update:Voxels to process : " + queue.size());
                t0 = System.currentTimeMillis();
            }
            IntCoord3D curCoord = queue.remove(0);
            int xy = curCoord.x + curCoord.y * sizeX;
            pixels[curCoord.z][xy] = tmpVal;
            int curZ, curY, curX;
            for (int zz = -1; zz < 2; zz++) {
                curZ = curCoord.z + zz;
                if (curZ >= 0 && curZ <= (sizeZ - 1)) {
                    for (int yy = -1; yy < 2; yy++) {
                        curY = curCoord.y + yy;
                        if (curY >= 0 && curY <= (sizeY - 1)) {
                            for (int xx = -1; xx < 2; xx++) {
                                curX = curCoord.x + xx;
                                if (curX >= 0 && curX <= (sizeX - 1)) {
                                    if (temp[curZ][curY * sizeX + curX] == CAND) {
                                        temp[curZ][curY * sizeX + curX] = QUEUE;
                                        queue.add(new IntCoord3D(curX, curY, curZ));
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        temp = null;
    }

    /**
     * Connect in 3D pixel with low values to pixel with high values
     * Pixels with low values not connected to pixels with high values will remain with low values
     *
     * @param img     The image to process, 8-bit image
     * @param seed    one seed to start the connection
     * @param highVal the high value
     * @param lowVal  the low value
     */
    private static void connect3DByte26(ImageByte img, IntCoord3D seed, byte lowVal, byte highVal, byte tmpVal, boolean log) {
        byte CAND = 1;
        byte QUEUE = 2;
        // tmp image to store queue pixels
        byte[][] temp = img.duplicate().pixels;
        for (int z = 0; z < img.sizeZ; z++) {
            for (int xy = 0; xy < img.sizeXY; xy++) {
                if ((temp[z][xy] == lowVal) || (temp[z][xy] == highVal)) {
                    temp[z][xy] = CAND;
                }
            }
        }
        long step = 50;
        byte[][] pixels = img.pixels;
        int sizeX = img.sizeX;
        int sizeY = img.sizeY;
        int sizeZ = img.sizeZ;
        ArrayList<IntCoord3D> queue = new ArrayList<IntCoord3D>();
        if (pixels[seed.z][seed.x + seed.y * sizeX] != highVal) {
            IJ.log("Seed not right value ");
            return;
        }
        queue.add(seed);
        Long t0 = System.currentTimeMillis();
        // test
        log = false;
        IJ.showStatus("flooding started for " + seed.x + " " + seed.y + " " + seed.z);
        // test
        while (!queue.isEmpty()) {
            //if (log && (System.currentTimeMillis() - t0 > step)) {
            //    IJ.log("\\Update:Voxels to process : " + queue.size());
            //    t0 = System.currentTimeMillis();
            //}
            IntCoord3D curCoord = queue.remove(0);
            int xy = curCoord.x + curCoord.y * sizeX;
            pixels[curCoord.z][xy] = tmpVal;

            // x++
            int xx = curCoord.x + 1;
            while ((xx < img.sizeX) && (temp[curCoord.z][curCoord.y * sizeX + xx] == CAND)) {
                temp[curCoord.z][curCoord.y * sizeX + xx] = QUEUE;
                queue.add(new IntCoord3D(xx, curCoord.y, curCoord.z));
                xx++;
            }
            // x--
            xx = curCoord.x - 1;
            while ((xx >= 0) && (temp[curCoord.z][curCoord.y * sizeX + xx] == CAND)) {
                temp[curCoord.z][curCoord.y * sizeX + xx] = QUEUE;
                queue.add(new IntCoord3D(xx, curCoord.y, curCoord.z));
                xx--;
            }
            // y++
            int yy = curCoord.y + 1;
            while ((yy < img.sizeY) && (temp[curCoord.z][yy * sizeX + curCoord.x] == CAND)) {
                temp[curCoord.z][yy * sizeX + curCoord.x] = QUEUE;
                queue.add(new IntCoord3D(curCoord.x, yy, curCoord.z));
                yy++;
            }
            // y--
            yy = curCoord.y - 1;
            while ((yy >= 0) && (temp[curCoord.z][yy * sizeX + curCoord.x] == CAND)) {
                temp[curCoord.z][yy * sizeX + curCoord.x] = QUEUE;
                queue.add(new IntCoord3D(curCoord.x, yy, curCoord.z));
                yy--;
            }
            // z++
            int zz = curCoord.z + 1;
            while ((zz < img.sizeZ) && (temp[zz][curCoord.y * sizeX + curCoord.x] == CAND)) {
                temp[zz][curCoord.y * sizeX + curCoord.x] = QUEUE;
                queue.add(new IntCoord3D(curCoord.x, curCoord.y, zz));
                zz++;
            }
            // z--
            zz = curCoord.z - 1;
            while ((zz >= 0) && (temp[zz][curCoord.y * sizeX + curCoord.x] == CAND)) {
                temp[zz][curCoord.y * sizeX + curCoord.x] = QUEUE;
                queue.add(new IntCoord3D(curCoord.x, curCoord.y, zz));
                zz--;
            }
        }
        temp = null;
    }

    /**
     * Connect in 3D pixel with low values to pixel with high values
     * Pixels with low values not connected to pixels with high values will remain with low values
     *
     * @param img     The image to process, 8-bit image
     * @param seed    one seed to start the connection
     * @param highVal the high value
     * @param lowVal  the low value
     */
    private static void connect3DShort26(ImageShort img, IntCoord3D seed, short lowVal, short highVal, short tmpVal, boolean log) {
        byte CAND = 1;
        byte QUEUE = 2;
        // tmp image to store queue pixels
        short[][] temp = img.duplicate().pixels;
        for (int z = 0; z < img.sizeZ; z++) {
            for (int xy = 0; xy < img.sizeXY; xy++) {
                if ((temp[z][xy] == lowVal) || (temp[z][xy] == highVal)) {
                    temp[z][xy] = CAND;
                }
            }
        }
        long step = 50;
        short[][] pixels = img.pixels;
        int sizeX = img.sizeX;
        int sizeY = img.sizeY;
        int sizeZ = img.sizeZ;
        ArrayList<IntCoord3D> queue = new ArrayList<IntCoord3D>();
        if (pixels[seed.z][seed.x + seed.y * sizeX] != highVal) {
            IJ.log("Seed not right value ");
            return;
        }
        queue.add(seed);
        Long t0 = System.currentTimeMillis();
        // test
        log = false;
        IJ.showStatus("flooding started for " + seed.x + " " + seed.y + " " + seed.z);
        // test
        while (!queue.isEmpty()) {
            //if (log && (System.currentTimeMillis() - t0 > step)) {
            //    IJ.log("\\Update:Voxels to process : " + queue.size());
            //    t0 = System.currentTimeMillis();
            //}
            IntCoord3D curCoord = queue.remove(0);
            int xy = curCoord.x + curCoord.y * sizeX;
            pixels[curCoord.z][xy] = tmpVal;

            // x++
            int xx = curCoord.x + 1;
            while ((xx < img.sizeX) && (temp[curCoord.z][curCoord.y * sizeX + xx] == CAND)) {
                temp[curCoord.z][curCoord.y * sizeX + xx] = QUEUE;
                queue.add(new IntCoord3D(xx, curCoord.y, curCoord.z));
                xx++;
            }
            // x--
            xx = curCoord.x - 1;
            while ((xx >= 0) && (temp[curCoord.z][curCoord.y * sizeX + xx] == CAND)) {
                temp[curCoord.z][curCoord.y * sizeX + xx] = QUEUE;
                queue.add(new IntCoord3D(xx, curCoord.y, curCoord.z));
                xx--;
            }
            // y++
            int yy = curCoord.y + 1;
            while ((yy < img.sizeY) && (temp[curCoord.z][yy * sizeX + curCoord.x] == CAND)) {
                temp[curCoord.z][yy * sizeX + curCoord.x] = QUEUE;
                queue.add(new IntCoord3D(curCoord.x, yy, curCoord.z));
                yy++;
            }
            // y--
            yy = curCoord.y - 1;
            while ((yy >= 0) && (temp[curCoord.z][yy * sizeX + curCoord.x] == CAND)) {
                temp[curCoord.z][yy * sizeX + curCoord.x] = QUEUE;
                queue.add(new IntCoord3D(curCoord.x, yy, curCoord.z));
                yy--;
            }
            // z++
            int zz = curCoord.z + 1;
            while ((zz < img.sizeZ) && (temp[zz][curCoord.y * sizeX + curCoord.x] == CAND)) {
                temp[zz][curCoord.y * sizeX + curCoord.x] = QUEUE;
                queue.add(new IntCoord3D(curCoord.x, curCoord.y, zz));
                zz++;
            }
            // z--
            zz = curCoord.z - 1;
            while ((zz >= 0) && (temp[zz][curCoord.y * sizeX + curCoord.x] == CAND)) {
                temp[zz][curCoord.y * sizeX + curCoord.x] = QUEUE;
                queue.add(new IntCoord3D(curCoord.x, curCoord.y, zz));
                zz--;
            }
        }
        temp = null;
    }


    /**
     * Connect in 3D pixel with low values to pixel with high values
     * Pixels with low values not connected to pixels with high values will remain with low values
     *
     * @param img     The image to process, 8-bit image
     * @param seed    one seed to start the connection
     * @param highVal the high value
     * @param lowVal  the low value
     */
    private static void connect3DShort26OK(ImageShort img, IntCoord3D seed, short lowVal, short highVal, short tmpVal, boolean log) {
        short CAND = 1;
        short QUEUE = 2;
        // tmp image to store queue pixels
        short[][] temp = img.duplicate().pixels;
        for (int z = 0; z < img.sizeZ; z++) {
            for (int xy = 0; xy < img.sizeXY; xy++) {
                if ((temp[z][xy] == lowVal) || (temp[z][xy] == highVal)) {
                    temp[z][xy] = CAND;
                }
            }
        }
        short[][] pixels = img.pixels;
        int sizeX = img.sizeX;
        int sizeY = img.sizeY;
        int sizeZ = img.sizeZ;
        ArrayList<IntCoord3D> queue = new ArrayList<IntCoord3D>();
        if (pixels[seed.z][seed.x + seed.y * sizeX] != highVal) {
            IJ.log("Seed not right value ");
            return;
        }
        queue.add(seed);
        long step = 500;
        Long t0 = System.currentTimeMillis();
        while (!queue.isEmpty()) {
            if (log && (System.currentTimeMillis() - t0 > step)) {
                IJ.log("\\Update:Voxels to process : " + queue.size());
                t0 = System.currentTimeMillis();
            }
            IntCoord3D curCoord = queue.remove(0);
            int xy = curCoord.x + curCoord.y * sizeX;
            pixels[curCoord.z][xy] = tmpVal;
            int curZ, curY, curX;
            for (int zz = -1; zz < 2; zz++) {
                curZ = curCoord.z + zz;
                if (curZ >= 0 && curZ <= (sizeZ - 1)) {
                    for (int yy = -1; yy < 2; yy++) {
                        curY = curCoord.y + yy;
                        if (curY >= 0 && curY <= (sizeY - 1)) {
                            for (int xx = -1; xx < 2; xx++) {
                                curX = curCoord.x + xx;
                                if (curX >= 0 && curX <= (sizeX - 1)) {
                                    if (temp[curZ][curY * sizeX + curX] == CAND) {
                                        temp[curZ][curY * sizeX + curX] = QUEUE;
                                        queue.add(new IntCoord3D(curX, curY, curZ));
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private static void flood3DByte26(ImageByte img, IntCoord3D seed, byte newVal) {
        byte[][] pixels = img.pixels;
        int sizeX = img.sizeX;
        int sizeY = img.sizeY;
        int sizeZ = img.sizeZ;
        short oldVal = pixels[seed.z][seed.x + seed.y * sizeX];
        // tmp image to store queue pixels
        byte CAND = 1;
        byte QUEUE = 2;

        byte[][] temp = img.duplicate().pixels;
        for (int z = 0; z < img.sizeZ; z++) {
            for (int xy = 0; xy < img.sizeXY; xy++) {
                if (temp[z][xy] == oldVal) {
                    temp[z][xy] = CAND;
                }
            }
        }

        ArrayList<IntCoord3D> queue = new ArrayList<IntCoord3D>();
        queue.add(seed);
        while (!queue.isEmpty()) {
            IntCoord3D curCoord = queue.remove(0);
            int xy = curCoord.x + curCoord.y * sizeX;
            pixels[curCoord.z][xy] = newVal;
            int curZ, curY, curX;
            for (int zz = -1; zz < 2; zz++) {
                curZ = curCoord.z + zz;
                if (curZ > 0 && curZ < (sizeZ - 1)) {
                    for (int yy = -1; yy < 2; yy++) {
                        curY = curCoord.y + yy;
                        if (curY > 0 && curY < (sizeY - 1)) {
                            for (int xx = -1; xx < 2; xx++) {
                                curX = curCoord.x + xx;
                                if (curX > 0 && curX < (sizeX - 1) && (xx != 0 || yy != 0 || zz != 0)) {
                                    if (temp[curZ][curX + curY * sizeX] == CAND) {
                                        temp[curZ][curX + curY * sizeX] = QUEUE;
                                        queue.add(new IntCoord3D(curX, curY, curZ));
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
