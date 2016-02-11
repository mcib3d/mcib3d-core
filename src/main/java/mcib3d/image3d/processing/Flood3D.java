package mcib3d.image3d.processing;

import java.util.ArrayList;
import mcib3d.geom.IntCoord3D;
import mcib3d.image3d.ImageByte;
import mcib3d.image3d.ImageInt;
import mcib3d.image3d.ImageShort;
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
        ArrayList<IntCoord3D> queue = new ArrayList<IntCoord3D>();
        queue.add(seed);
        while (!queue.isEmpty()) {
            IntCoord3D curCoord = queue.remove(0); // FIXME last element?
            int xy = curCoord.x + curCoord.y * sizeX;
            if (pixels[curCoord.z][xy] == oldVal) {
                pixels[curCoord.z][xy] = newVal;
                if (curCoord.x > 0 && pixels[curCoord.z][xy - 1] == oldVal) {
                    queue.add(new IntCoord3D(curCoord.x - 1, curCoord.y, curCoord.z));
                }
                if (curCoord.x < (sizeX - 1) && pixels[curCoord.z][xy + 1] == oldVal) {
                    queue.add(new IntCoord3D(curCoord.x + 1, curCoord.y, curCoord.z));
                }
                if (curCoord.y > 0 && pixels[curCoord.z][xy - sizeX] == oldVal) {
                    queue.add(new IntCoord3D(curCoord.x, curCoord.y - 1, curCoord.z));
                }
                if (curCoord.y < (sizeY - 1) && pixels[curCoord.z][xy + sizeX] == oldVal) {
                    queue.add(new IntCoord3D(curCoord.x, curCoord.y + 1, curCoord.z));
                }
                if (curCoord.z > 0 && pixels[curCoord.z - 1][xy] == oldVal) {
                    queue.add(new IntCoord3D(curCoord.x, curCoord.y, curCoord.z - 1));
                }
                if (curCoord.z < (sizeZ - 1) && pixels[curCoord.z + 1][xy] == oldVal) {
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
        ArrayList<IntCoord3D> queue = new ArrayList<IntCoord3D>();
        queue.add(seed);
        while (!queue.isEmpty()) {
            IntCoord3D curCoord = queue.remove(0); // FIXME last element?
            int xy = curCoord.x + curCoord.y * sizeX;
            if (pixels[curCoord.z][xy] == oldVal) {
                pixels[curCoord.z][xy] = newVal;
                if (curCoord.x > 0 && pixels[curCoord.z][xy - 1] == oldVal) {
                    queue.add(new IntCoord3D(curCoord.x - 1, curCoord.y, curCoord.z));
                }
                if (curCoord.x < (sizeX - 1) && pixels[curCoord.z][xy + 1] == oldVal) {
                    queue.add(new IntCoord3D(curCoord.x + 1, curCoord.y, curCoord.z));
                }
                if (curCoord.y > 0 && pixels[curCoord.z][xy - sizeX] == oldVal) {
                    queue.add(new IntCoord3D(curCoord.x, curCoord.y - 1, curCoord.z));
                }
                if (curCoord.y < (sizeY - 1) && pixels[curCoord.z][xy + sizeX] == oldVal) {
                    queue.add(new IntCoord3D(curCoord.x, curCoord.y + 1, curCoord.z));
                }
                if (curCoord.z > 0 && pixels[curCoord.z - 1][xy] == oldVal) {
                    queue.add(new IntCoord3D(curCoord.x, curCoord.y, curCoord.z - 1));
                }
                if (curCoord.z < (sizeZ - 1) && pixels[curCoord.z + 1][xy] == oldVal) {
                    queue.add(new IntCoord3D(curCoord.x, curCoord.y, curCoord.z + 1));
                }
            }
        }
    }

    private static void flood3DShort26(ImageShort img, IntCoord3D seed, short newVal) {
        short[][] pixels = img.pixels;
        int sizeX = img.sizeX;
        int sizeY = img.sizeY;
        int sizeZ = img.sizeZ;
        short oldVal = pixels[seed.z][seed.x + seed.y * sizeX];
        ArrayList<IntCoord3D> queue = new ArrayList<IntCoord3D>();
        queue.add(seed);
        while (!queue.isEmpty()) {
            IntCoord3D curCoord = queue.remove(0); // FIXME last element?
            int xy = curCoord.x + curCoord.y * sizeX;
            if (pixels[curCoord.z][xy] == oldVal) {
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
                                        if (pixels[curZ][curX + curY * sizeX] == oldVal) {
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

    private static void flood3DByte26(ImageByte img, IntCoord3D seed, byte newVal) {
        byte[][] pixels = img.pixels;
        int sizeX = img.sizeX;
        int sizeY = img.sizeY;
        int sizeZ = img.sizeZ;
        short oldVal = pixels[seed.z][seed.x + seed.y * sizeX];
        ArrayList<IntCoord3D> queue = new ArrayList<IntCoord3D>();
        queue.add(seed);
        while (!queue.isEmpty()) {
            IntCoord3D curCoord = queue.remove(0); // FIXME last element?
            int xy = curCoord.x + curCoord.y * sizeX;
            if (pixels[curCoord.z][xy] == oldVal) {
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
                                        if (pixels[curZ][curX + curY * sizeX] == oldVal) {
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
}
