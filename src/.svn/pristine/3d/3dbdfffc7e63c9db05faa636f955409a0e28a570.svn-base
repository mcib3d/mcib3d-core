package mcib3d.image3d.processing;

import java.util.LinkedList;
import java.util.Queue;
import mcib3d.image3d.Coordinate3D;
import mcib3d.image3d.ImageByte;
import mcib3d.image3d.ImageShort;
import mcib3d.utils.exceptionPrinter;
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
 * @author Thomas Boudier
 */
public class FillHoles3D {

    public static void process(ImageByte mask, int foregroundValue, int nbCPUs, boolean verbose) {
        final byte[][] pixels= mask.pixels;
        final int sizeX = mask.sizeX;
        final int sizeZ = mask.sizeZ;
        final int sizeY = mask.sizeY;
        final int sizeXY = mask.sizeXY;
        final byte fgValue = (byte) foregroundValue;
        int mid;
        if (foregroundValue>1) mid= foregroundValue-1;
        else mid = foregroundValue+1;
        final byte midValue = (byte)mid;
        //ImageByte im1 = new ImageByte(pixels, "", sizeX);
        //Z axis
        final mcib3d.utils.ThreadRunner tr = new mcib3d.utils.ThreadRunner(0, sizeY, nbCPUs);
        for (int i = 0; i < tr.threads.length; i++) {
            tr.threads[i] = new Thread(
                    new Runnable() {
                        public void run() {
                            for (int idx = tr.ai.getAndIncrement(); idx < tr.end; idx = tr.ai.getAndIncrement()) {
                                try {
                                    byte mid = midValue;
                                    int offsetY = sizeX * idx;
                                    for (int x = 0; x < sizeX; x++) {
                                        int z = 0;
                                        while (z < sizeZ && pixels[z][x + offsetY] == 0) {
                                            pixels[z][x + offsetY] = mid;
                                            z++;
                                        }
                                        if (z < (sizeZ - 1)) {
                                            z = sizeZ - 1;
                                            while (z >= 0 && pixels[z][x + offsetY] == 0) {
                                                pixels[z][x + offsetY] = mid;
                                                z--;
                                            }
                                        }
                                    }
                                } catch (Exception e) {
                                    exceptionPrinter.print(e, "fillHoles 3D Z:" + idx + "::error::", true);
                                }
                            }
                        }
                    });
        }
        tr.startAndJoin();


        //Y axis
        final mcib3d.utils.ThreadRunner tr2 = new mcib3d.utils.ThreadRunner(0, sizeZ, nbCPUs);
        for (int i = 0; i < tr2.threads.length; i++) {
            tr2.threads[i] = new Thread(
                    new Runnable() {

                        public void run() {
                            for (int idx = tr2.ai.getAndIncrement(); idx < tr2.end; idx = tr2.ai.getAndIncrement()) {
                                try {
                                    byte mid = midValue;
                                    byte fg = fgValue;
                                    for (int y = 0; y < sizeY; y++) {
                                        int offsetY = sizeX * y;
                                        boolean bcg = pixels[idx][offsetY] == 0;
                                        for (int x = 0; x < sizeX; x++) {
                                            byte value = pixels[idx][offsetY + x];
                                            if (value == fg) {
                                                bcg = false;
                                            } else if (value == 0 && bcg) {
                                                pixels[idx][offsetY + x] = mid;
                                            } else if (value == mid) {
                                                bcg = true;
                                            }
                                        }
                                        bcg = pixels[idx][offsetY + sizeX - 1] == 0;
                                        for (int x = sizeX - 1; x >= 0; x--) {
                                            byte value = pixels[idx][offsetY + x];
                                            if (value == fg) {
                                                bcg = false;
                                            } else if (value == 0 && bcg) {
                                                pixels[idx][offsetY + x] = mid;
                                            } else if (value == mid) {
                                                bcg = true;
                                            }
                                        }
                                    }
                                } catch (Exception e) {
                                    exceptionPrinter.print(e, "fillHoles 3D Y:" + idx + "::error::", true);
                                }
                            }
                        }
                    });
        }
        tr2.startAndJoin();

        //X axis
        final mcib3d.utils.ThreadRunner tr3 = new mcib3d.utils.ThreadRunner(0, sizeZ, nbCPUs);
        for (int i = 0; i < tr3.threads.length; i++) {
            tr3.threads[i] = new Thread(
                    new Runnable() {

                        public void run() {
                            for (int idx = tr3.ai.getAndIncrement(); idx < tr3.end; idx = tr3.ai.getAndIncrement()) {
                                try {
                                    byte mid = midValue;
                                    byte fg = fgValue;
                                    for (int x = 0; x < sizeX; x++) {

                                        boolean bcg = pixels[idx][x] == 0;
                                        for (int y = 0; y < sizeY; y++) {
                                            byte value = pixels[idx][y * sizeX + x];
                                            if (value == fg) {
                                                bcg = false;
                                            } else if (value == 0 && bcg) {
                                                pixels[idx][y * sizeX + x] = mid;
                                            } else if (value == mid) {
                                                bcg = true;
                                            }
                                        }
                                        bcg = pixels[idx][(sizeY - 1) * sizeX + x] == 0;
                                        for (int y = sizeY - 1; y >= 0; y--) {
                                            byte value = pixels[idx][y * sizeX + x];
                                            if (value == fg) {
                                                bcg = false;
                                            } else if (value == 0 && bcg) {
                                                pixels[idx][y * sizeX + x] = mid;
                                            } else if (value == mid) {
                                                bcg = true;
                                            }
                                        }
                                    }
                                } catch (Exception e) {
                                    exceptionPrinter.print(e, "fillHoles 3D X:" + idx + "::error::", true);
                                }
                            }
                        }
                    });
        }
        tr3.startAndJoin();
        //im1.showDuplicate("3eme passage");
        //remove artefacts
        Queue<Integer> heap = new LinkedList<Integer>();
        for (int z = 0; z < sizeZ; z++) {
            for (int y = 0; y < sizeY; y++) {
                for (int x = 0; x < sizeX; x++) {
                    if (pixels[z][x + sizeX * y] == 0) {
                        if (checkVoxel(pixels, x, y, z, sizeX, sizeY, sizeZ, midValue)) {
                            heap.add(x + y * sizeX + z * sizeXY);
                        }
                    }
                }
            }
        }
        Coordinate3D c = new Coordinate3D(0, sizeX, sizeY, sizeZ);
        while (!heap.isEmpty()) {
            int coord = heap.remove();
            c.setCoord(coord);
            if (pixels[c.z][c.x + c.y * sizeX] == 0) {
                pixels[c.z][c.x + c.y * sizeX] = midValue;
                if (c.z > 0 && pixels[c.z - 1][c.x + c.y * sizeX] == 0) {
                    heap.add(coord - sizeXY);
                }
                if (c.z < (sizeZ - 1) && pixels[c.z + 1][c.x + c.y * sizeX] == 0) {
                    heap.add(coord + sizeXY);
                }
                if (c.x > 0 && pixels[c.z][c.x - 1 + c.y * sizeX] == 0) {
                    heap.add(coord - 1);
                }
                if (c.x < (sizeX - 1) && pixels[c.z][c.x + 1 + c.y * sizeX] == 0) {
                    heap.add(coord + 1);
                }
                if (c.y > 0 && pixels[c.z][c.x + (c.y - 1) * sizeX] == 0) {
                    heap.add(coord - sizeX);
                }
                if (c.y < (sizeY - 1) && pixels[c.z][c.x + (c.y + 1) * sizeX] == 0) {
                    heap.add(coord + sizeX);
                }
            }
        }
        //im1.showDuplicate("arte");
        //final step
        final mcib3d.utils.ThreadRunner tr4 = new mcib3d.utils.ThreadRunner(0, sizeZ, nbCPUs);
        for (int i = 0; i < tr4.threads.length; i++) {
            tr4.threads[i] = new Thread(
                    new Runnable() {

                        public void run() {
                            for (int idx = tr4.ai.getAndIncrement(); idx < tr4.end; idx = tr4.ai.getAndIncrement()) {
                                try {
                                    byte mid = midValue;
                                    byte fg = fgValue;
                                    for (int xy = 0; xy < sizeX * sizeY; xy++) {
                                        if (pixels[idx][xy] == mid) {
                                            pixels[idx][xy] = 0;
                                        } else if (pixels[idx][xy] == 0) {
                                            pixels[idx][xy] = fg;
                                        }
                                    }
                                } catch (Exception e) {
                                    exceptionPrinter.print(e, "fillHoles 3D final step:" + idx + "::error::", true);
                                }
                            }
                        }
                    });
        }
        tr4.startAndJoin();

    }

    private static boolean checkVoxel(final byte[][] pixels, final int x, final int y, final int z, final int sizeX, final int sizeY, final int sizeZ, byte midValue) {
        if (z > 0 && pixels[z - 1][x + y * sizeX] == midValue) {
            return true;
        }
        if (z < (sizeZ - 1) && pixels[z + 1][x + y * sizeX] == midValue) {
            return true;
        }
        if (x > 0 && pixels[z][x - 1 + y * sizeX] == midValue) {
            return true;
        }
        if (x < (sizeX - 1) && pixels[z][x + 1 + y * sizeX] == midValue) {
            return true;
        }
        if (y > 0 && pixels[z][x + (y - 1) * sizeX] == midValue) {
            return true;
        }
        if (y < (sizeY - 1) && pixels[z][x + (y + 1) * sizeX] == midValue) {
            return true;
        }
        return false;
    }
    
    public static void process(ImageShort mask, int foregroundValue, int nbCPUs, boolean verbose) {
        final short[][] pixels= mask.pixels;
        final int sizeX = mask.sizeX;
        final int sizeZ = mask.sizeZ;
        final int sizeY = mask.sizeY;
        final int sizeXY = mask.sizeXY;
        final short fgValue = (short) foregroundValue;
        int mid;
        if (foregroundValue>1) mid= foregroundValue-1;
        else mid = foregroundValue+1;
        final short midValue = (short)mid;
        //ImageByte im1 = new ImageByte(pixels, "", sizeX);
        //Z axis
        final mcib3d.utils.ThreadRunner tr = new mcib3d.utils.ThreadRunner(0, sizeY, nbCPUs);
        for (int i = 0; i < tr.threads.length; i++) {
            tr.threads[i] = new Thread(
                    new Runnable() {

                        public void run() {
                            for (int idx = tr.ai.getAndIncrement(); idx < tr.end; idx = tr.ai.getAndIncrement()) {
                                try {
                                    short mid = midValue;
                                    int offsetY = sizeX * idx;
                                    for (int x = 0; x < sizeX; x++) {
                                        int z = 0;
                                        while (z < sizeZ && pixels[z][x + offsetY] == 0) {
                                            pixels[z][x + offsetY] = mid;
                                            z++;
                                        }
                                        if (z < (sizeZ - 1)) {
                                            z = sizeZ - 1;
                                            while (z >= 0 && pixels[z][x + offsetY] == 0) {
                                                pixels[z][x + offsetY] = mid;
                                                z--;
                                            }
                                        }
                                    }
                                } catch (Exception e) {
                                    exceptionPrinter.print(e, "fillHoles 3D Z:" + idx + "::error::", true);
                                }
                            }
                        }
                    });
        }
        tr.startAndJoin();


        //Y axis
        final mcib3d.utils.ThreadRunner tr2 = new mcib3d.utils.ThreadRunner(0, sizeZ, nbCPUs);
        for (int i = 0; i < tr2.threads.length; i++) {
            tr2.threads[i] = new Thread(
                    new Runnable() {

                        public void run() {
                            for (int idx = tr2.ai.getAndIncrement(); idx < tr2.end; idx = tr2.ai.getAndIncrement()) {
                                try {
                                    short mid = midValue;
                                    short fg = fgValue;
                                    for (int y = 0; y < sizeY; y++) {
                                        int offsetY = sizeX * y;
                                        boolean bcg = pixels[idx][offsetY] == 0;
                                        for (int x = 0; x < sizeX; x++) {
                                            short value = pixels[idx][offsetY + x];
                                            if (value == fg) {
                                                bcg = false;
                                            } else if (value == 0 && bcg) {
                                                pixels[idx][offsetY + x] = mid;
                                            } else if (value == mid) {
                                                bcg = true;
                                            }
                                        }
                                        bcg = pixels[idx][offsetY + sizeX - 1] == 0;
                                        for (int x = sizeX - 1; x >= 0; x--) {
                                            short value = pixels[idx][offsetY + x];
                                            if (value == fg) {
                                                bcg = false;
                                            } else if (value == 0 && bcg) {
                                                pixels[idx][offsetY + x] = mid;
                                            } else if (value == mid) {
                                                bcg = true;
                                            }
                                        }
                                    }
                                } catch (Exception e) {
                                    exceptionPrinter.print(e, "fillHoles 3D Y:" + idx + "::error::", true);
                                }
                            }
                        }
                    });
        }
        tr2.startAndJoin();

        //X axis
        final mcib3d.utils.ThreadRunner tr3 = new mcib3d.utils.ThreadRunner(0, sizeZ, nbCPUs);
        for (int i = 0; i < tr3.threads.length; i++) {
            tr3.threads[i] = new Thread(
                    new Runnable() {

                        public void run() {
                            for (int idx = tr3.ai.getAndIncrement(); idx < tr3.end; idx = tr3.ai.getAndIncrement()) {
                                try {
                                    short mid = midValue;
                                    short fg = fgValue;
                                    for (int x = 0; x < sizeX; x++) {

                                        boolean bcg = pixels[idx][x] == 0;
                                        for (int y = 0; y < sizeY; y++) {
                                            short value = pixels[idx][y * sizeX + x];
                                            if (value == fg) {
                                                bcg = false;
                                            } else if (value == 0 && bcg) {
                                                pixels[idx][y * sizeX + x] = mid;
                                            } else if (value == mid) {
                                                bcg = true;
                                            }
                                        }
                                        bcg = pixels[idx][(sizeY - 1) * sizeX + x] == 0;
                                        for (int y = sizeY - 1; y >= 0; y--) {
                                            short value = pixels[idx][y * sizeX + x];
                                            if (value == fg) {
                                                bcg = false;
                                            } else if (value == 0 && bcg) {
                                                pixels[idx][y * sizeX + x] = mid;
                                            } else if (value == mid) {
                                                bcg = true;
                                            }
                                        }
                                    }
                                } catch (Exception e) {
                                    exceptionPrinter.print(e, "fillHoles 3D X:" + idx + "::error::", true);
                                }
                            }
                        }
                    });
        }
        tr3.startAndJoin();
        //im1.showDuplicate("3eme passage");
        //remove artefacts
        Queue<Integer> heap = new LinkedList<Integer>();
        for (int z = 0; z < sizeZ; z++) {
            for (int y = 0; y < sizeY; y++) {
                for (int x = 0; x < sizeX; x++) {
                    if (pixels[z][x + sizeX * y] == 0) {
                        if (checkVoxel(pixels, x, y, z, sizeX, sizeY, sizeZ, midValue)) {
                            heap.add(x + y * sizeX + z * sizeXY);
                        }
                    }
                }
            }
        }
        Coordinate3D c = new Coordinate3D(0, sizeX, sizeY, sizeZ);
        while (!heap.isEmpty()) {
            int coord = heap.remove();
            c.setCoord(coord);
            if (pixels[c.z][c.x + c.y * sizeX] == 0) {
                pixels[c.z][c.x + c.y * sizeX] = midValue;
                if (c.z > 0 && pixels[c.z - 1][c.x + c.y * sizeX] == 0) {
                    heap.add(coord - sizeXY);
                }
                if (c.z < (sizeZ - 1) && pixels[c.z + 1][c.x + c.y * sizeX] == 0) {
                    heap.add(coord + sizeXY);
                }
                if (c.x > 0 && pixels[c.z][c.x - 1 + c.y * sizeX] == 0) {
                    heap.add(coord - 1);
                }
                if (c.x < (sizeX - 1) && pixels[c.z][c.x + 1 + c.y * sizeX] == 0) {
                    heap.add(coord + 1);
                }
                if (c.y > 0 && pixels[c.z][c.x + (c.y - 1) * sizeX] == 0) {
                    heap.add(coord - sizeX);
                }
                if (c.y < (sizeY - 1) && pixels[c.z][c.x + (c.y + 1) * sizeX] == 0) {
                    heap.add(coord + sizeX);
                }
            }
        }
        //im1.showDuplicate("arte");
        //final step
        final mcib3d.utils.ThreadRunner tr4 = new mcib3d.utils.ThreadRunner(0, sizeZ, nbCPUs);
        for (int i = 0; i < tr4.threads.length; i++) {
            tr4.threads[i] = new Thread(
                    new Runnable() {

                        public void run() {
                            for (int idx = tr4.ai.getAndIncrement(); idx < tr4.end; idx = tr4.ai.getAndIncrement()) {
                                try {
                                    short mid = midValue;
                                    short fg = fgValue;
                                    for (int xy = 0; xy < sizeX * sizeY; xy++) {
                                        if (pixels[idx][xy] == mid) {
                                            pixels[idx][xy] = 0;
                                        } else if (pixels[idx][xy] == 0) {
                                            pixels[idx][xy] = fg;
                                        }
                                    }
                                } catch (Exception e) {
                                    exceptionPrinter.print(e, "fillHoles 3D final step:" + idx + "::error::", true);
                                }
                            }
                        }
                    });
        }
        tr4.startAndJoin();

    }

    private static boolean checkVoxel(final short[][] pixels, final int x, final int y, final int z, final int sizeX, final int sizeY, final int sizeZ, short midValue) {
        if (z > 0 && pixels[z - 1][x + y * sizeX] == midValue) {
            return true;
        }
        if (z < (sizeZ - 1) && pixels[z + 1][x + y * sizeX] == midValue) {
            return true;
        }
        if (x > 0 && pixels[z][x - 1 + y * sizeX] == midValue) {
            return true;
        }
        if (x < (sizeX - 1) && pixels[z][x + 1 + y * sizeX] == midValue) {
            return true;
        }
        if (y > 0 && pixels[z][x + (y - 1) * sizeX] == midValue) {
            return true;
        }
        if (y < (sizeY - 1) && pixels[z][x + (y + 1) * sizeX] == midValue) {
            return true;
        }
        return false;
    }
}
