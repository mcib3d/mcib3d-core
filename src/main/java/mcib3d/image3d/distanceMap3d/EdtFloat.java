package mcib3d.image3d.distanceMap3d;

import ij.*;
import ij.process.*;
import mcib3d.image3d.ImageFloat;

/* Bob Dougherty 8/8/2006
 Saito-Toriwaki algorithm for Euclidian Distance Transformation.
 Direct application of Algorithm 1.
 Version S1A: lower memory usage.
 Version S1A.1 A fixed indexing bug for 666-bin data set
 Version S1A.2 Aug. 9, 2006.  Changed noResult value.
 Version S1B Aug. 9, 2006.  Faster.
 Version S1B.1 Sept. 6, 2006.  Changed comments.
 Version S1C Oct. 1, 2006.  Option for inverse case.
 Fixed inverse behavior in y and z directions.
 Version D July 30, 2007.  Multithread processing for step 2.

 This version assumes the input stack is already in memory, 8-bit, and
 outputs to a new 32-bit stack.  Versions that are more stingy with memory
 may be forthcoming.

 License:
 Copyright (c) 2006, OptiNav, Inc.
 All rights reserved.

 Redistribution and use in source and binary forms, with or without
 modification, are permitted provided that the following conditions
 are met:

 Redistributions of source code must retain the above copyright
 notice, this list of conditions and the following disclaimer.
 Redistributions in binary form must reproduce the above copyright
 notice, this list of conditions and the following disclaimer in the
 documentation and/or other materials provided with the distribution.
 Neither the name of OptiNav, Inc. nor the names of its contributors
 may be used to endorse or promote products derived from this software
 without specific prior written permission.

 THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 A PARTICULAR PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

 */

/**
 * @author thomas
 */
public class EdtFloat {

    /**
     * @param imp
     * @param thresh
     * @param scaleXY
     * @param scaleZ
     * @return
     * @throws Exception
     */
    public ImageFloat run(ImageFloat imp, float thresh, float scaleXY, float scaleZ, int nbCPUs) throws Exception {
        int w = imp.sizeX;
        int h = imp.sizeY;
        int d = imp.sizeZ;
        float scale = scaleZ / scaleXY;
        float[][] data = imp.pixels;
        //Create 32 bit floating point stack for output, s.  Will also use it for g in Transormation 1.
        ImageStack sStack = new ImageStack(w, h);
        float[][] s = new float[d][];
        for (int k = 0; k < d; k++) {
            ImageProcessor ipk = new FloatProcessor(w, h);
            sStack.addSlice(null, ipk);
            s[k] = (float[]) ipk.getPixels();
        }
        float[] sk;
        //Transformation 1.  Use s to store g.
        Step1Thread[] s1t = new Step1Thread[nbCPUs];
        for (int thread = 0; thread < nbCPUs; thread++) {
            s1t[thread] = new Step1Thread(thread, nbCPUs, w, h, d, thresh, s, data, scale);
            s1t[thread].start();
        }
        try {
            for (int thread = 0; thread < nbCPUs; thread++) {
                s1t[thread].join();
            }
        } catch (InterruptedException ie) {
            IJ.error("A thread was interrupted in step 1 .");
        }
        //Transformation 2.  g (in s) -> h (in s)
        Step2Thread[] s2t = new Step2Thread[nbCPUs];
        for (int thread = 0; thread < nbCPUs; thread++) {
            s2t[thread] = new Step2Thread(thread, nbCPUs, w, h, d, s);
            s2t[thread].start();
        }
        try {
            for (int thread = 0; thread < nbCPUs; thread++) {
                s2t[thread].join();
            }
        } catch (InterruptedException ie) {
            IJ.error("A thread was interrupted in step 2 .");
        }
        //Transformation 3. h (in s) -> s
        if (imp.sizeZ > 1) {
            Step3Thread[] s3t = new Step3Thread[nbCPUs];
            for (int thread = 0; thread < nbCPUs; thread++) {
                s3t[thread] = new Step3Thread(thread, nbCPUs, w, h, d, s, data, thresh, scale);
                s3t[thread].start();
            }
            try {
                for (int thread = 0; thread < nbCPUs; thread++) {
                    s3t[thread].join();
                }
            } catch (InterruptedException ie) {
                IJ.error("A thread was interrupted in step 3 .");
            }
        }
        //Find the largest distance for scaling
        //Also fill in the background values.
        float distMax = 0;
        int wh = w * h;
        float dist;
        for (int k = 0; k < d; k++) {
            sk = s[k];
            for (int ind = 0; ind < wh; ind++) {
                if ((data[k][ind] <= thresh)) {
                    sk[ind] = 0;
                } else {
                    dist = (float) Math.sqrt(sk[ind]) * scaleXY;
                    sk[ind] = dist;
                    distMax = (dist > distMax) ? dist : distMax;
                }
            }
        }

        ImageFloat res = (ImageFloat) ImageFloat.wrap(sStack);
        res.setScale(imp);
        res.setOffset(imp);
        res.setMinAndMax(0, distMax);
        return res;
    }

    //Modified from ImageJ code by Wayne Rasband

    String stripExtension(String name) {
        if (name != null) {
            int dotIndex = name.lastIndexOf(".");
            if (dotIndex >= 0) {
                name = name.substring(0, dotIndex);
            }
        }
        return name;
    }

    class Step1Thread extends Thread {

        int thread, nThreads, w, h, d;
        float thresh;
        float[][] s;
        float[][] data;
        float scaleZ;

        public Step1Thread(int thread, int nThreads, int w, int h, int d, float thresh, float[][] s, float[][] data, float scaleZ) {
            this.thread = thread;
            this.nThreads = nThreads;
            this.w = w;
            this.h = h;
            this.d = d;
            this.thresh = thresh;
            this.data = data;
            this.s = s;
            this.scaleZ = scaleZ * scaleZ;
        }

        public void run() {
            float[] sk;
            float[] dk;
            int n = w;
            if (h > n) {
                n = h;
            }
            if (d > n) {
                n = d;
            }
            //int noResult = 3*(n+1)*(n+1);
            boolean[] background = new boolean[n];
            boolean nonempty;
            float test, min;
            for (int k = thread; k < d; k += nThreads) {
                sk = s[k];
                dk = data[k];
                for (int j = 0; j < h; j++) {
                    for (int i = 0; i < w; i++) {
                        background[i] = (dk[i + w * j] <= thresh);
                    }
                    for (int i = 0; i < w; i++) {
                        // wrong, not to compare with distance to image borders, otherwise borders will be considered as joints
                        // compare only to x where f(xjk) = 0 (background[x])
                        // min = Math.min(i + 1, w - i); // distance minimale = distance au bord le plus proche + 1
                        // min *= min;
                        // compute initial value for min
                        // TODO: if there is no background pixel on this row, there is no initial min value
                        for (int x = 0; x<w; x++){
                            if (background[x]) {
                                min = i - x;
                                min *= min;
                                break; // take distance to first background pixel as initial value
                            }
                        }
                        for (int x = i; x < w; x++) {
                            if (background[x]) {
                                test = i - x;
                                test *= test;
                                if (test < min) {
                                    min = test;
                                }
                                break; // break here if background[x] true is acceptable. all background pixels in this row must be considered. the closest to be considered
                            }
                        }
                        for (int x = i - 1; x >= 0; x--) {
                            if (background[x]) {
                                test = i - x;
                                test *= test;
                                if (test < min) {
                                    min = test;
                                }
                                break;
                            }
                        }
                        sk[i + w * j] = min;
                    }
                }
            }
        }//run
    }//Step1Thread

    class Step2Thread extends Thread {

        int thread, nThreads, w, h, d;
        float[][] s;

        public Step2Thread(int thread, int nThreads, int w, int h, int d, float[][] s) {
            this.thread = thread;
            this.nThreads = nThreads;
            this.w = w;
            this.h = h;
            this.d = d;
            this.s = s;
        }

        public void run() {
            float[] sk;
            int n = w;
            if (h > n) {
                n = h;
            }
            if (d > n) {
                n = d;
            }
            //int noResult = 3*(n+1)*(n+1);
            float[] tempInt = new float[n];
            float[] tempS = new float[n];
            boolean nonempty;
            float test, min;
            int delta;
            for (int k = thread; k < d; k += nThreads) {
                sk = s[k];
                for (int i = 0; i < w; i++) {
                    nonempty = false;
                    for (int j = 0; j < h; j++) {
                        tempS[j] = sk[i + w * j];
                        if (tempS[j] > 0) {
                            nonempty = true;
                        }
                    }
                    if (nonempty) {
                        for (int j = 0; j < h; j++) {
                            // TODO: wrong, not to compare with distance to image borders, otherwise borders will be considered as joints
                            // min = Math.min(j + 1, h - j);
                            // min *= min;
                            // delta = j;
                            min = tempS[0] + j *j ; // initial value for min at first pixel in this column
                            for (int y = 0; y < h; y++) {
                                test = tempS[y] + (y - j) * (y - j); // not delta * delta-- as befored
                                if (test < min) {
                                    min = test;
                                }
                            }
                            tempInt[j] = min;
                        }
                        for (int j = 0; j < h; j++) {
                            sk[i + w * j] = tempInt[j];
                        }
                    }
                }
            }
        }//run
    }//Step2Thread

    class Step3Thread extends Thread {

        int thread, nThreads, w, h, d;
        float thresh;
        float[][] s;
        float[][] data;
        float scaleZ;

        public Step3Thread(int thread, int nThreads, int w, int h, int d, float[][] s, float[][] data, float thresh, float scaleZ) {
            this.thresh = thresh;
            this.thread = thread;
            this.nThreads = nThreads;
            this.w = w;
            this.h = h;
            this.d = d;
            this.s = s;
            this.data = data;
            this.scaleZ = scaleZ * scaleZ;
        }

        public void run() {
            int zStart, zStop, zBegin, zEnd;
            float[] sk;
            int n = w;
            if (h > n) {
                n = h;
            }
            if (d > n) {
                n = d;
            }
            //int noResult = 3*(n+1)*(n+1);
            float[] tempInt = new float[n];
            float[] tempS = new float[n];
            boolean nonempty;
            float test, min;
            int delta;
            for (int j = thread; j < h; j += nThreads) {
                for (int i = 0; i < w; i++) {
                    nonempty = false;
                    for (int k = 0; k < d; k++) {
                        tempS[k] = s[k][i + w * j];
                        if (tempS[k] > 0) {
                            nonempty = true;
                        }
                    }
                    if (nonempty) {
                        zStart = 0;
                        while ((zStart < (d - 1)) && (tempS[zStart] == 0)) {
                            zStart++;
                        }
                        if (zStart > 0) {
                            zStart--;
                        }
                        zStop = d - 1;
                        while ((zStop > 0) && (tempS[zStop] == 0)) {
                            zStop--;
                        }
                        if (zStop < (d - 1)) {
                            zStop++;
                        }

                        for (int k = 0; k < d; k++) {
                            //Limit to the non-background to save time,
                            if ((data[k][i + w * j] > thresh)) {
                                // wrong, not to compare with distance to image borders, otherwise borders will be considered as joints
                                // min = Math.min(k + 1, d - k);// bug fixed
                                // min *= min * scaleZ;
                                zBegin = zStart;
                                zEnd = zStop;
                                if (zBegin > k) {
                                    zBegin = k;
                                }
                                if (zEnd < k) {
                                    zEnd = k;
                                }
                                min = tempS[zBegin] + (k - zBegin) * (k - zBegin) * scaleZ; // set initial value for min with first z index

                                for (int z = zBegin; z <= zEnd; z++) {
                                    test = tempS[z] + (k - z) * (k - z) * scaleZ;
                                    if (test < min) {
                                        min = test;
                                    }
                                    //min = (test < min) ? test : min;
                                }
                                tempInt[k] = min;
                            }
                        }
                        for (int k = 0; k < d; k++) {
                            s[k][i + w * j] = tempInt[k];
                        }
                    }
                }
            }
        }//run
    }//Step2Thread
}
