package mcib3d.image3d;

import java.util.HashMap;
import java.util.LinkedList;

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
public class ImageLabeller2D {

    protected HashMap<Integer, Spot2D> listSpots = null;
    // current mask
    protected ImageHandler currentMask = null;
    int[] labels;
    boolean debug = false;
    int minSize = 0;
    int maxsize = Integer.MAX_VALUE;

    public ImageLabeller2D(boolean debug) {
        this.debug = debug;
    }

    public ImageLabeller2D() {
    }

    public ImageLabeller2D(int min, int max) {
        minSize = min;
        maxsize = max;
    }

    public long getMinSize() {
        return minSize;
    }

    public void setMinSize(int minSize) {
        this.minSize = minSize;
    }

    public long getMaxsize() {
        return maxsize;
    }

    public void setMaxsize(int maxsize) {
        this.maxsize = maxsize;
    }


    private void labelSpots2D(ImageHandler mask) {
        currentMask = mask;
        //label objects
        labels = new int[mask.sizeXY];
        int sizeX = mask.sizeX;
        listSpots = new HashMap<>();
        int currentLabel = 1;
        Spot2D currentSpot;
        Vox2D v;
        int nextLabel;
        int xy;
        if (debug) {
            System.out.println("Labelling...");
        }
        for (int y = 0; y < mask.sizeY; y++) {
            for (int x = 0; x < sizeX; x++) {
                xy = x + y * sizeX;
                if (mask.getPixel(xy, 0) != 0) {
                    currentSpot = null;
                    v = new Vox2D(xy);
                    for (int j = -1; j <= 1; j++) {
                        for (int i = -1; i <= 1; i++) {
                            if ((mask.contains(x + i, y + j, 0)) && (i * i + j * j != 0) && ((i < 0) || (j < 0))) {
                                nextLabel = labels[xy + i + j * sizeX];
                                if (nextLabel != 0) {
                                    if (currentSpot == null) {
                                        currentSpot = listSpots.get(nextLabel);
                                        currentSpot.addVox(v);
                                    } else if (nextLabel != currentSpot.label) {
                                        currentSpot = currentSpot.fusion(listSpots.get(nextLabel));
                                        currentSpot.addVox(v);
                                    }
                                }
                            }
                        }
                    }
                    if (currentSpot == null) {
                        listSpots.put(currentLabel, new Spot2D(currentLabel++, v));
                    }
                }
            }
        }
    }


    public ImageInt getLabels(ImageHandler mask) {
        if ((listSpots == null) || (mask != currentMask)) {
            this.labelSpots2D(mask);
        }
        ImageShort res = new ImageShort(mask.getTitle() + "::segmented", mask.sizeX, mask.sizeY, mask.sizeZ);
        res.setScale(mask);
        short label = 1;
        for (Spot2D s : listSpots.values()) {
            LinkedList<Vox2D> a = s.voxels;
            // check size
            if ((a.size() >= minSize) && (a.size() <= maxsize)) {
                for (Vox2D vox : a) {
                    res.pixels[0][vox.xy] = label;
                }
                label++;
            }
        }
        return res;
    }

    public ImageFloat getLabelsFloat(ImageHandler mask) {
        return getLabelsFloat(mask, false);
    }

    public ImageFloat getLabelsFloat(ImageHandler mask, boolean connex6) {
        if ((listSpots == null) || (mask != currentMask)) {
            this.labelSpots2D(mask);
        }
        ImageFloat res = new ImageFloat(mask.getTitle() + "::segmented", mask.sizeX, mask.sizeY, mask.sizeZ);
        int label = 1;
        for (Spot2D s : listSpots.values()) {
            LinkedList<Vox2D> a = s.voxels;
            // check size
            if ((a.size() >= minSize) && (a.size() <= maxsize)) {
                for (Vox2D vox : a) {
                    res.pixels[0][vox.xy] = label;
                }
                label++;
            }
        }
        return res;
    }


    public int getNbObjectsTotal(ImageHandler mask) {
        if ((listSpots == null) || (mask != currentMask)) {
            this.labelSpots2D(mask);
        }

        return listSpots.size();
    }


    public int getNbObjectsInSizeRange(ImageHandler mask) {
        // check if labelling needed
        if ((listSpots == null) || (mask != currentMask)) {
            this.labelSpots2D(mask);
        }
        // count nb objects
        int nbObj = 0;
        int sizeX = mask.sizeX;
        short label = 1;
        for (Spot2D s : listSpots.values()) {
            LinkedList<Vox2D> a = s.voxels;
            // check size
            if ((a.size() >= minSize) && (a.size() <= maxsize)) {
                nbObj++;
            }
        }

        return nbObj;
    }


    protected class Spot2D {
        LinkedList<Vox2D> voxels;
        int label;
        boolean tooBig = false;

        public Spot2D(int label, Vox2D v) {
            this.label = label;
            this.voxels = new LinkedList();
            voxels.add(v);
            v.setLabel(label);
        }

        public void addVox(Vox2D vox) {
            voxels.add(vox);
            vox.setLabel(label);
        }

        public void setLabel(int label) {
            this.label = label;
            for (Vox2D v : voxels) {
                v.setLabel(label);
            }
        }

        public Spot2D fusion(Spot2D other) {
            if (other.label < label) {
                return other.fusion(this);
            }

            listSpots.remove(other.label);
            // FIXME pb if size >= integer max size
            voxels.addAll(other.voxels);

            other.setLabel(label);

            return this;
        }

        public long getSize() {
            return voxels.size();
        }
    }

    protected class Vox2D {

        public int xy;

        public Vox2D(int xy) {
            this.xy = xy;
        }

        public void setLabel(int label) {
            labels[xy] = label;
        }

        @Override
        public boolean equals(Object o) {
            if (o instanceof Vox2D) {
                return xy == ((Vox2D) o).xy;
            }
            return false;
        }

        @Override
        public int hashCode() {
            int hash = 3;
            hash = 47 * hash + this.xy;
            return hash;
        }
    }
}
