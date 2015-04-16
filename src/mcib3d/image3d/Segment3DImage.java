package mcib3d.image3d;

import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import java.util.Arrays;

/**
 * Copyright (C) F. Cordelières from 3D Object Counter
 * http://imagejdocu.tudor.lu/doku.php?id=plugin:analysis:3d_object_counter:start
 *
 * License: This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by the Free
 * Software Foundation; either version 3 of the License, or (at your option) any
 * later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 */
/**
 * Description of the Class
 *
 * @author fabrice cordelieres @created 6 mars 2009
 */
public class Segment3DImage {

    float lowThreshold;
    float highThreshold;
    // FABRICE
    //Object imgArray;
    ImageHandler imgCopy;
    int[] objID, IDcount, surfList;
    boolean[] IDisAtEdge;
    boolean[] isSurf;
    int width = 1, height = 1, nbSlices = 1, nbVoxels = 1, depth = 8;
    int minSize, maxSize, nbObj = 0, nbSurfPix = 0;
    boolean sizeFilter = true, exclude = false;
    //float thr = 0;

    /**
     * Constructor for the Segment3DImage object
     *
     * @param img The image to segment
     * @param lowthr Low threshold
     * @param highthr High Threshold
     */
    public Segment3DImage(ImageHandler img, float lowthr, float highthr) {
        init(img, lowthr, highthr);
    }

    private void init(ImageHandler img, float lowthr, float highthr) {
        this.imgCopy=img.duplicate();
        this.width = img.sizeX;
        this.height = img.sizeY;
        this.nbSlices = img.sizeZ;
        this.nbVoxels = this.width * this.height * this.nbSlices;
        //this.thr = lowthr;
        this.minSize = 1;
        this.maxSize = nbVoxels;
        this.sizeFilter = true;
        this.exclude = false;

        // TB
        if (lowthr <= highthr) {
            this.lowThreshold = lowthr;
            this.highThreshold = highthr;
        } else {
            this.lowThreshold = highthr;
            this.highThreshold = lowthr;
        }

        if (depth != 8 && depth != 16) {
            throw new IllegalArgumentException("Counter3D class expects 8- or 16-bits images only");
        }

        this.nbObj = this.nbVoxels;

        //imgArray=img.getArray1D();

        this.imgArrayModifier();

    }

    /**
     *
     * @param plus
     * @param lo
     * @param hi
     */
    public Segment3DImage(ImagePlus plus, float lo, float hi) {
        ImageHandler img = ImageHandler.wrap(plus);
        init(img, lo, hi);
    }

    /**
     *
     * @return
     */
    public int getMaxSizeObject() {
        return maxSize;
    }

    /**
     *
     * @param maxSize
     */
    public void setMaxSizeObject(int maxSize) {
        this.maxSize = maxSize;
    }

    /**
     *
     * @return
     */
    public int getMinSizeObject() {
        return minSize;
    }

    /**
     *
     * @param minSize
     */
    public void setMinSizeObject(int minSize) {
        this.minSize = minSize;
    }

    public int getNbObj() {
        return nbObj;
    }

    /**
     * Generates the connexity analysis.
     */
    public void segment() {
        //First ID attribution
        int currID = 0;
        int currPos = 0;
        int minID = 0;
        int surfPix;
        int neigbNb;
        int pos;
        int currPixID;
        int neigbX;
        int neigbY;
        int neigbZ;

        long start = System.currentTimeMillis();
        /*
         * Finding the structures: The minID tag is initialized with the current
         * value of tag (currID).If thresholded, the neighborhood of the current
         * pixel is collected. For each of those 13 pixels, the value is
         * retrieved and tested against minID: only the minimum of the two is
         * kept. As anterior pixels have already been tagged, only two
         * possibilities may exists: 1-The minimum is currID: we start a new
         * structure and currID should be incremented 2-The minimum is not
         * currID: we continue an already existing structure Each time a new
         * pixel is tagged, a counter of pixels in the current tag is
         * incremented.
         */
        this.objID = new int[this.nbVoxels];

        for (int z = 1; z <= this.nbSlices; z++) {
            for (int y = 0; y < this.height; y++) {
                for (int x = 0; x < this.width; x++) {
                    if (minID == currID) {
                        currID++;
                    }
                    if (imgCopy.getPixel(currPos) != 0) {
                        minID = currID;
                        minID = minAntTag(minID, x, y, z);
                        this.objID[currPos] = minID;
                    }
                    currPos++;
                }
            }
            //IJ.showStatus("Finding structures "+z*100/this.nbSlices+"%");
            //IJ.showStatus("Step 1/3: Finding structures");
            //IJ.showProgress(z, this.nbSlices);
        }
        //IJ.showStatus("");

        this.IDcount = new int[currID];
        for (int i = 0; i < this.nbVoxels; i++) {
            this.IDcount[this.objID[i]]++;
        }

        this.IDisAtEdge = new boolean[currID];
        Arrays.fill(this.IDisAtEdge, false);
        /*
         * Connecting structures: The first tagging of structure may have led to
         * shearing apart pieces of a same structure This part will connect them
         * back by attributing the minimal retrieved tag among the 13
         * neighboring pixels located prior to the current pixel + the centre
         * pixel and will replace all the values of those pixels by the minimum
         * value.
         */
        this.isSurf = new boolean[this.nbVoxels];
        currPos = 0;

        for (int z = 1; z <= this.nbSlices; z++) {
            for (int y = 0; y < this.height; y++) {
                for (int x = 0; x < this.width; x++) {
                    if (imgCopy.getPixel(currPos) != 0) {
                        minID = this.objID[currPos];
                        surfPix = 0;
                        neigbNb = 0;
                        //Find the minimum tag in the neighbours pixels
                        for (neigbZ = z - 1; neigbZ <= z + 1; neigbZ++) {
                            for (neigbY = y - 1; neigbY <= y + 1; neigbY++) {
                                for (neigbX = x - 1; neigbX <= x + 1; neigbX++) {
                                    //Following line is important otherwise objects might be linked from one side of the stack to the other !!!
                                    if (neigbX >= 0 && neigbX < this.width && neigbY >= 0 && neigbY < this.height && neigbZ >= 1 && neigbZ <= this.nbSlices) {
                                        pos = offset(neigbX, neigbY, neigbZ);
                                        if (imgCopy.getPixel(pos) != 0) {
                                            if ((this.nbSlices > 1 && ((neigbX == x && neigbY == y && neigbZ == z - 1) || (neigbX == x && neigbY == y && neigbZ == z + 1))) || (neigbX == x && neigbY == y - 1 && neigbZ == z) || (neigbX == x && neigbY == y + 1 && neigbZ == z) || (neigbX == x - 1 && neigbY == y && neigbZ == z) || (neigbX == x + 1 && neigbY == y && neigbZ == z)) {
                                                surfPix++;
                                            }
                                            minID = Math.min(minID, this.objID[pos]);
                                        }
                                        neigbNb++;
                                    }
                                }
                            }
                        }
                        if ((surfPix != 6 && this.nbSlices > 1) || (surfPix != 4 && this.nbSlices == 1)) {
                            this.isSurf[currPos] = true;
                            this.nbSurfPix++;
                        } else {
                            this.isSurf[currPos] = false;
                        }
                        //Replacing tag by the minimum tag found
                        for (neigbZ = z - 1; neigbZ <= z + 1; neigbZ++) {
                            for (neigbY = y - 1; neigbY <= y + 1; neigbY++) {
                                for (neigbX = x - 1; neigbX <= x + 1; neigbX++) {
                                    //Following line is important otherwise objects might be linked from one side of the stack to the other !!!
                                    if (neigbX >= 0 && neigbX < this.width && neigbY >= 0 && neigbY < this.height && neigbZ >= 1 && neigbZ <= this.nbSlices) {
                                        pos = offset(neigbX, neigbY, neigbZ);
                                        if (imgCopy.getPixel(pos) != 0) {
                                            currPixID = this.objID[pos];
                                            if (currPixID > minID) {
                                                replaceID(currPixID, minID);
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        //Check if the current particle is touching an edge
                        if (x == 0 || y == 0 || x == this.width - 1 || y == this.height - 1 || (this.nbSlices != 1 && (z == 1 || z == this.nbSlices))) {
                            this.IDisAtEdge[minID] = true;
                        }
                    }
                    currPos++;
                }
            }
            //IJ.showStatus("Step 2/3: Connecting structures");
            //IJ.showProgress(z, this.nbSlices);
        }
        //IJ.showStatus("");

        int newCurrID = 0;

        //Renumbering of all the found objects and update of their respective number of pixels while filtering based on the number of pixels
        for (int i = 1; i < this.IDcount.length; i++) {
            if ((this.IDcount[i] != 0 && this.IDcount[i] >= this.minSize && this.IDcount[i] <= this.maxSize) && (!this.exclude || !(this.exclude && this.IDisAtEdge[i]))) {
                newCurrID++;
                int nbPix = this.IDcount[i];
                replaceID(i, newCurrID);
                this.IDcount[newCurrID] = nbPix;
            } else {
                replaceID(i, 0);
            }
            //IJ.showStatus("Step 3/3: Renumbering structures");
            //IJ.showProgress(i, currID);
        }
        //IJ.showStatus("3D segmentation : " + (System.currentTimeMillis() - start) / 1000.0 + " ms");

        //if (this.redirect) {
        //	this.prepareImgArrayForRedirect();
        //	}
        //if (this.showMaskedImg) {
        //this.buildImg(this.imgArray, null, "Masked image", false, false, false, 0, 0).show();

        // compute surf

        //}

        this.nbObj = newCurrID;

        //this.getObjects();
    }

    /**
     *
     * @return
     */
    public ImageInt getLabelledObjectsImage3D() {
        return buildImg(this.objID);
    }

    /**
     *
     * @return
     */
    public ImageStack getLabelledObjectsStack() {
        return getLabelledObjectsImage3D().getImageStack();
    }

    /**
     *
     * @return
     */
    public ImageInt getSurfaceObjectsImage3D() {
        this.surfList = new int[this.nbVoxels];
        for (int i = 0; i < this.nbVoxels; i++) {
            this.surfList[i] = this.isSurf[i] ? this.objID[i] : 0;
        }
        return buildImg(this.surfList);
    }

    /**
     *
     * @return
     */
    public ImageStack getSurfaceObjectsStack() {
        return getSurfaceObjectsImage3D().getImageStack();
    }

    /**
     * Returns the minimum anterior tag among the 13 previous pixels (4 pixels
     * in 2D).
     *
     * @param x coordinate x of the current pixel.
     * @param y coordinate y of the current pixel.
     * @param z coordinate z of the current pixel.
     * @param initialValue Description of the Parameter
     * @return the minimum found anterior tag as an integer.
     */
    private int minAntTag(int initialValue, int x, int y, int z) {
        int min = initialValue;
        int currPos;

        for (int neigbY = y - 1; neigbY <= y + 1; neigbY++) {
            for (int neigbX = x - 1; neigbX <= x + 1; neigbX++) {
                //Following line is important otherwise objects might be linked from one side of the stack to the other !!!
                if (neigbX >= 0 && neigbX < this.width && neigbY >= 0 && neigbY < this.height && z - 1 >= 1 && z - 1 <= this.nbSlices) {
                    currPos = offset(neigbX, neigbY, z - 1);
                    if (imgCopy.getPixel(currPos) != 0) {
                        min = Math.min(min, this.objID[currPos]);
                    }
                }
            }
        }

        for (int neigbX = x - 1; neigbX <= x + 1; neigbX++) {
            //Following line is important otherwise objects might be linked from one side of the stack to the other !!!
            if (neigbX >= 0 && neigbX < this.width && y - 1 >= 0 && y - 1 < this.height && z >= 1 && z <= this.nbSlices) {
                currPos = offset(neigbX, y - 1, z);
                if (imgCopy.getPixel(currPos)!= 0) {
                    min = Math.min(min, this.objID[currPos]);
                }
            }
        }

        //Following line is important otherwise objects might be linked from one side of the stack to the other !!!
        if (x - 1 >= 0 && x - 1 < this.width && y >= 0 && y < this.height && z >= 1 && z <= this.nbSlices) {
            currPos = offset(x - 1, y, z);
            if (imgCopy.getPixel(currPos) != 0 && x >= 1 && y >= 0 && z >= 1) {
                min = Math.min(min, this.objID[currPos]);
            }
        }

        return min;
    }

    /**
     * Returns the index where to find the informations corresponding to pixel
     * (x, y, z).
     *
     * @param m coordinate x of the current pixel
     * @param n coordinate y of the current pixel
     * @param o coordinate z of the current pixel
     * @return the index where to find the informations corresponding to pixel
     * (x, y, z).
     */
    private int offset(int m, int n, int o) {
        if (m + n * this.width + (o - 1) * this.width * this.height >= this.width * this.height * this.nbSlices) {
            return this.width * this.height * this.nbSlices - 1;
        } else {
            if (m + n * this.width + (o - 1) * this.width * this.height < 0) {
                return 0;
            } else {
                return m + n * this.width + (o - 1) * this.width * this.height;
            }
        }
    }

    /**
     * Replaces one object ID by another within the objID array.
     *
     * @param oldVal old value
     * @param newVal new value
     */
    private void replaceID(int oldVal, int newVal) {
        if (oldVal != newVal) {
            int nbFoundPix = 0;
            for (int i = 0; i < this.objID.length; i++) {
                if (this.objID[i] == oldVal) {
                    this.objID[i] = newVal;
                    nbFoundPix++;
                }
                if (nbFoundPix == this.IDcount[oldVal]) {
                    i = this.objID.length;
                }
            }
            this.IDcount[oldVal] = 0;
            this.IDcount[newVal] += nbFoundPix;
        }
    }

    /**
     * Set to zero pixels below the threshold in the "imgArray" arrays.
     *
     * @param img The image
     */
    private void imgArrayModifier() {
        int index = 0;
        float val;
        for (int z = 0; z < nbSlices; z++) {
            for (int y = 0; y < this.height; y++) {
                for (int x = 0; x < this.width; x++) {
                    val = imgCopy.getPixel(x, y, z);
                    if ((val < lowThreshold) || (val > highThreshold)) {
                        imgCopy.setPixel(index, 0);
                        nbObj--;
                    } else {
                        imgCopy.setPixel(index, val);
                    }
                    index++;
                }
            }
        }

        if (nbObj <= 0) {
            IJ.log("No object found");
        }
    }

    /**
     * Generates the ImagePlus based on Counter3D object width, height and
     * number of slices, the input array and title.
     *
     * @param imgArray containing the pixels intensities (integer array).
     * @param cenArray containing the coordinates of pixels where the labels
     * should be put (integer array).
     * @param title to attribute to the ImagePlus (string).
     * @param drawDots should be true if dots should be drawn instead of a
     * single pixel for each coordinate of imgArray (boolean).
     * @param drawNb should be true if numbers have to be drawn at each
     * coordinate stored in cenArray (boolean).
     * @param whiteNb should be true if numbers have to appear white (boolean).
     * @param dotSize size of the dots to be drawn (integer).
     * @param fontSize font size of the numbers to be shown (integer).
     * @return Description of the Return Value
     */
    private ImageInt buildImg(int[] IDobj) {
        int index = 0;

        ImageInt ima = new ImageShort("Objects",this.width, this.height, this.nbSlices);
        //IJ.showStatus("Creating the image...");
        for (int z = 0; z < this.nbSlices; z++) {
            for (int y = 0; y < this.height; y++) {
                for (int x = 0; x < this.width; x++) {
                    int currVal = IDobj[index];
                    if (currVal != 0) {
                        ima.setPixel(x, y, z, currVal);
                    }
                    index++;
                }
            }
        }
        //IJ.showStatus("");

        /*
         * index = 0; if (drawNb && cenArray != null) { for (int z = 1; z <=
         * this.nbSlices; z++) { IJ.showStatus("Numbering objects...");
         * img.setSlice(z); ImageProcessor ip = img.getProcessor();
         * ip.setValue(Math.pow(2, imgDepth)); ip.setFont(new Font("Arial",
         * Font.PLAIN, fontSize)); for (int y = 0; y < this.height; y++) { for
         * (int x = 0; x < this.width; x++) { int currVal = cenArray[index]; if
         * (currVal != 0) { if (!whiteNb) { ip.setValue(currVal); }
         * ip.drawString("" + currVal, x, y); } index++; } } } }
         * IJ.showStatus("");
         *
         */

        return ima;
    }
}
