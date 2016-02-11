/*  JACoP: "Just Another Colocalization Plugin..." v1, 13/02/06
 Fabrice P Cordelieres, fabrice.cordelieres at curie.u-psud.fr
 Susanne Bolte, Susanne.bolte@isv.cnrs-gif.fr
 
 Copyright (C) 2006 Susanne Bolte & Fabrice P. Cordelieres
  
 License:
 This program is free software; you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation; either version 3 of the License, or
 (at your option) any later version.

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with this program.  If not, see <http://www.gnu.org/licenses/>.

 *
 *
 */
package mcib3d.image3d.processing;

import ij.*;
import ij.gui.*;
import ij.ImagePlus.*;
import ij.measure.*;
import ij.process.*;

import java.awt.*;

/**
 *
 * @author Fabrice Cordelieres
 */
public class ImageColocalizer {

    int width, height, nbSlices, depth, length, widthCostes, heightCostes, nbsliceCostes, lengthCostes;
    String titleA, titleB;
    int[] A, B;
    int Amin, Amax, Bmin, Bmax;
    double Amean, Bmean;
    Calibration cal, micronCal;
    //Counter3D countA, countB;
    //Values for stats
    boolean doThat;
    double sumA, sumB, sumAB, sumsqrA, Aarraymean, Barraymean;
    boolean verbose;
    /**
     * Creates a new instance of ImageColocalizer
     */
    public ImageColocalizer(ImagePlus ipA, ImagePlus ipB, Calibration cal, boolean verbose) {
        this.verbose=verbose;
        this.width = ipA.getWidth();
        this.height = ipA.getHeight();
        this.nbSlices = ipA.getNSlices();
        this.depth = ipA.getBitDepth();

        if (this.width != ipB.getWidth() || this.height != ipB.getHeight() || this.nbSlices != ipB.getNSlices() || this.depth != ipB.getBitDepth()) {
            IJ.error("ImageColocalizer expects both images to have the same size and depth");
            return;
        }
        this.length = this.width * this.height * this.nbSlices;
        this.A = new int[this.length];
        this.B = new int[this.length];
        this.titleA = ipA.getTitle();
        this.titleB = ipB.getTitle();
        this.cal = cal;
        this.micronCal = (Calibration) cal.clone();

        this.micronCal.pixelDepth /= 1000;
        this.micronCal.pixelHeight /= 1000;
        this.micronCal.pixelWidth /= 1000;
        this.micronCal.setUnit("�m");

        buildArray(ipA, ipB);

        if (verbose) IJ.log("**************************************************\nImage A: " + this.titleA + "\nImage B: " + this.titleB);
    }

    /**
     * Creates a new instance of ImageColocalizer
     */
    public ImageColocalizer(ImagePlus ipA, ImagePlus ipB, boolean verbose) {
        this(ipA, ipB, new Calibration(), verbose);
    }

    private void Pearson() {
        this.doThat = true;
        if (verbose) IJ.log("\nPearson's Coefficient:\nr=" + round(linreg(A, B, 0, 0)[2], 3));
    }

    public double getPearson() {
        return round(linreg(A, B, 0, 0)[2], 3);
    }

    /*
     * public void Pearson(int TA, int TB) { doThat=true; IJ.log("\nPearson's
     * Coefficient using thesholds:\nr="+round(linreg(A,B,TA,TB)[2],3)); }
     */
    public double[] Overlap(int thrA, int thrB) {
        double num = 0;
        double numThr = 0;
        double den1 = 0;
        double den1Thr = 0;
        double den2 = 0;
        double den2Thr = 0;

        for (int i = 0; i < this.length; i++) {
            num += this.A[i] * this.B[i];
            den1 += Math.pow(this.A[i], 2);
            den2 += Math.pow(this.B[i], 2);
            if (this.A[i] > thrA && this.B[i] > thrB) {
                numThr += this.A[i] * this.B[i];
                den1Thr += Math.pow(this.A[i], 2);
                den2Thr += Math.pow(this.B[i], 2);
            }
        }

        double OverlapCoeff = num / (Math.sqrt(den1 * den2));
        if (verbose) IJ.log("\nOverlap Coefficient:\nr=" + round(OverlapCoeff, 3));
        if (verbose) IJ.log("\nr^2=k1xk2:\nk1=" + round(num / den1, 3) + "\nk2=" + round(num / den2, 3));
        double OverlapCoeffThr = numThr / (Math.sqrt(den1Thr * den2Thr));
        if (verbose) IJ.log("\n\nUsing thresholds (thrA=" + thrA + " and thrB=" + thrB + ")");
        if (verbose) IJ.log("\nOverlap Coefficient:\nr=" + round(OverlapCoeffThr, 3));
        if (verbose) IJ.log("\nr^2=k1xk2:\nk1=" + round(numThr / den1Thr, 3) + "\nk2=" + round(numThr / den2Thr, 3));
        
        double[] res={OverlapCoeff,num / den1,num / den2,OverlapCoeffThr,numThr / den1Thr,numThr / den2Thr};
        
        return res;
    }

    public double[] MM(int thrA, int thrB) {
        double sumAcoloc = 0;
        double sumAcolocThr = 0;
        double sumA = 0;
        double sumAThr = 0;
        double sumBcoloc = 0;
        double sumBcolocThr = 0;
        double sumB = 0;
        double sumBThr = 0;

        for (int i = 0; i < this.length; i++) {
            if (this.B[i] > 0) {
                sumB += this.B[i];
                if (this.A[i] > 0) {
                    sumAcoloc += this.A[i];
                }
            }
            if (this.B[i] > thrB) {
                sumBThr += this.B[i];
                if (this.A[i] > thrA) {
                    sumAcolocThr += this.A[i];
                }
            }
            if (this.A[i] > 0) {
                sumA += this.A[i];
                if (this.B[i] > 0) {
                    sumBcoloc += this.B[i];
                }
            }
            if (this.A[i] > thrA) {
                sumAThr += this.A[i];
                if (this.B[i] > thrB) {
                    sumBcolocThr += this.B[i];
                }
            }
        }

        double M1 = sumAcoloc / sumA;
        double M1Thr = sumAcolocThr / sumAThr;
        double M2 = sumBcoloc / sumB;
        double M2Thr = sumBcolocThr / sumBThr;
        double[] res = {M1, M2, M1Thr, M2Thr};
        if (verbose) IJ.log("\nManders' Coefficients (original):\nM1=" + round(M1, 3) + " (fraction of A overlapping B)\nM2=" + round(M2, 3) + " (fraction of B overlapping A)");
        if (verbose) IJ.log("\nManders' Coefficients (using threshold value of " + thrA + " for imgA and " + thrB + " for imgB):\nM1=" + round(M1Thr, 3) + " (fraction of A overlapping B)\nM2=" + round(M2Thr, 3) + " (fraction of B overlapping A)");

        return res;
    }

    public void CostesAutoThr() {
        int CostesThrA = this.Amax;
        int CostesThrB = this.Bmax;
        double CostesSumAThr = 0;
        double CostesSumA = 0;
        double CostesSumBThr = 0;
        double CostesSumB = 0;
        double CostesPearson = 1;
        double[] rx = new double[this.Amax - this.Amin + 1];
        double[] ry = new double[this.Amax - this.Amin + 1];
        double rmax = 0;
        double rmin = 1;
        this.doThat = true;
        int count = 0;

        //First Step: define line equation
        this.doThat = true;
        double[] tmp = linreg(this.A, this.B, 0, 0);
        double a = tmp[0];
        double b = tmp[1];
        double CoeffCorr = tmp[2];
        this.doThat = false;

        int LoopMin = (int) Math.max(this.Amin, (this.Bmin - b) / a);
        int LoopMax = (int) Math.min(this.Amax, (this.Bmax - b) / a);


        //Minimize r of points below (thrA,a*thrA+b)
        for (int i = LoopMax; i >= LoopMin; i--) {
            IJ.showStatus("Costes' threshold calculation in progress : " + (int) (100 * (LoopMax - i) / (LoopMax - LoopMin)) + "% done");
            //IJ.showProgress(LoopMax-i, LoopMax-LoopMin);

            if (IJ.escapePressed()) {
                IJ.showStatus("Task canceled by user");
                //IJ.showProgress(2,1);
                return;
            }

            CostesPearson = linregCostes(this.A, this.B, i, (int) (a * i + b))[2];

            rx[count] = i;
            ry[count] = CostesPearson;
            if (((Double) CostesPearson).isNaN()) {
                if (count != LoopMax) {
                    ry[count] = ry[count - 1];
                } else {
                    ry[count] = 1;
                }
            }

            if (CostesPearson <= rmin && i != LoopMax) {
                CostesThrA = i;
                CostesThrB = (int) (a * i + b);
                //i=Amin-1;
            }

            rmax = Math.max(rmax, ry[count]);
            rmin = Math.min(rmin, ry[count]);
            count++;


        }


        for (int i = 0; i < this.length; i++) {
            CostesSumA += this.A[i];
            if (this.A[i] > CostesThrA) {
                CostesSumAThr += this.A[i];
            }
            CostesSumB += this.B[i];
            if (this.B[i] > CostesThrB) {
                CostesSumBThr += this.B[i];
            }
        }
        if (verbose) {
            Plot plot = new Plot("Costes' threshold " + this.titleA + " and " + this.titleB, "ThrA", "Pearson's coefficient below", rx, ry);
            plot.setLimits(LoopMin, LoopMax, rmin, rmax);
            plot.setColor(Color.black);
            plot.draw();

            //Draw the zero line
            double[] xline = {CostesThrA, CostesThrA};
            double[] yline = {rmin, rmax};
            plot.setColor(Color.red);
            plot.addPoints(xline, yline, 2);

            plot.show();
        }
        ImagePlus CostesMask = NewImage.createRGBImage("Costes' mask", this.width, this.height, this.nbSlices, 0);
        CostesMask.getProcessor().setValue(Math.pow(2, this.depth));
        for (int k = 1; k <= this.nbSlices; k++) {
            CostesMask.setSlice(k);
            for (int j = 0; j < this.height; j++) {
                for (int i = 0; i < this.width; i++) {
                    int position = offset(i, j, k);
                    int[] color = new int[3];
                    color[0] = this.A[position];
                    color[1] = this.B[position];
                    color[2] = 0;
                    if (color[0] > CostesThrA && color[1] > CostesThrB) {
                        //CostesMask.getProcessor().setValue(((A[position]-CostesThrA)/(LoopMax-CostesThrA))*Math.pow(2, depthA));
                        //CostesMask.getProcessor().drawPixel(i,j);
                        for (int l = 0; l <= 2; l++) {
                            color[l] = 255;
                        }
                    }
                    CostesMask.getProcessor().putPixel(i, j, color);
                }
            }
        }
        CostesMask.setCalibration(this.cal);
        CostesMask.setSlice(1);
        CostesMask.show();


        IJ.showStatus("");
        // IJ.showProgress(2,1);

        this.doThat = true;
        if (verbose) {
            IJ.log("\nCostes' automatic threshold set to " + CostesThrA + " for imgA & " + CostesThrB + " for imgB");
            IJ.log("Pearson's Coefficient:\nr=" + round(linreg(this.A, this.B, CostesThrA, CostesThrB)[2], 3) + " (" + round(CostesPearson, 3) + " below thresholds)");
            IJ.log("M1=" + round(CostesSumAThr / CostesSumA, 3) + " & M2=" + round(CostesSumBThr / CostesSumB, 3));
        }
    }

    public void CCF(int CCFx) {
        double meanA;
        double meanB;
        double nPoints;
        double num;
        double den1;
        double den2;
        double CCF0 = 0;
        double CCFmin = 0;
        int lmin = -CCFx;
        double CCFmax = 0;
        int lmax = -CCFx;

        double[] CCFarray = new double[2 * CCFx + 1];
        double[] x = new double[2 * CCFx + 1];

        int count = 0;

        if (verbose) IJ.log("\nVan Steensel's Cross-correlation Coefficient between " + titleA + " and " + titleB + ":");
        for (int l = -CCFx; l <= CCFx; l++) {
            if (verbose) IJ.showStatus("CCF calculation in progress: " + (count + 1) + "/" + (2 * CCFx + 1));
            //IJ.showProgress(count+1, 2*CCFx+1);

            if (IJ.escapePressed()) {
                IJ.showStatus("Task canceled by user");
                //IJ.showProgress(2,1);
                return;
            }

            meanA = 0;
            meanB = 0;
            nPoints = 0;

            for (int k = 1; k <= this.nbSlices; k++) {
                for (int j = 0; j < this.height; j++) {
                    for (int i = 0; i < this.width; i++) {
                        if (i + l >= 0 && i + l < this.width) {
                            int coord = offset(i, j, k);
                            int coordShift = offset(i + l, j, k);

                            meanA += this.A[coord];
                            meanB += this.B[coordShift];
                            nPoints++;
                        }
                    }
                }
            }

            meanA /= nPoints;
            meanB /= nPoints;

            num = 0;
            den1 = 0;
            den2 = 0;

            for (int k = 1; k <= this.nbSlices; k++) {
                for (int j = 0; j < this.height; j++) {
                    for (int i = 0; i < this.width; i++) {
                        if (i + l >= 0 && i + l < this.width) {
                            int coord = offset(i, j, k);
                            int coordShift = offset(i + l, j, k);

                            num += (this.A[coord] - meanA) * (this.B[coordShift] - meanB);
                            den1 += Math.pow((this.A[coord] - meanA), 2);
                            den2 += Math.pow((this.B[coordShift] - meanB), 2);
                        }
                    }
                }
            }

            double CCF = num / (Math.sqrt(den1 * den2));

            if (l == -CCFx) {
                CCF0 = CCF;
                CCFmin = CCF;
                CCFmax = CCF;
            } else {
                if (CCF < CCFmin) {
                    CCFmin = CCF;
                    lmin = l;
                }
                if (CCF > CCFmax) {
                    CCFmax = CCF;
                    lmax = l;
                }
            }
            x[count] = l;
            CCFarray[count] = CCF;
            count++;
        }
        IJ.log("CCF min.: " + round(CCFmin, 3) + " (obtained for dx=" + lmin + ") CCF max.: " + round(CCFmax, 3) + " (obtained for dx=" + lmax + ")");
        Plot plot = new Plot("Van Steensel's CCF between " + this.titleA + " and " + this.titleB, "dx", "CCF", x, CCFarray);
        plot.setLimits(-CCFx, CCFx, CCFmin - (CCFmax - CCFmin) * 0.05, CCFmax + (CCFmax - CCFmin) * 0.05);
        plot.setColor(Color.white);
        plot.draw();

        //Previous plot is white, just to get values inserted into the plot list, the problem being that the plot is as default a line plot... Following line plots same values as circles.
        plot.setColor(Color.black);
        plot.addPoints(x, CCFarray, Plot.CIRCLE);

        double[] xline = {0, 0};
        double[] yline = {CCFmin - (CCFmax - CCFmin) * 0.05, CCFmax + (CCFmax - CCFmin) * 0.05};
        plot.setColor(Color.red);
        plot.addPoints(xline, yline, 2);

        CurveFitter cf = new CurveFitter(x, CCFarray);
        double[] param = {CCFmin, CCFmax, lmax, (double) CCFx};
        cf.setInitialParameters(param);
        cf.doFit(CurveFitter.GAUSSIAN);
        param = cf.getParams();
        IJ.log("\nResults for fitting CCF on a Gaussian (CCF=a+(b-a)exp(-(xshift-c)^2/(2d^2))):" + cf.getResultString() + "\nFWHM=" + Math.abs(round(2 * Math.sqrt(2 * Math.log(2)) * param[3], 3)) + " pixels");
        for (int i = 0; i < x.length; i++) {
            CCFarray[i] = cf.f(CurveFitter.GAUSSIAN, param, x[i]);
        }
        plot.setColor(Color.BLUE);
        plot.addPoints(x, CCFarray, 2);

        IJ.showStatus("");
        // IJ.showProgress(2,1);

        plot.show();

    }

    public void CytoFluo() {

        //Plot only accepts double array: convert A & B to Adb & Bdb    
        double[] Adb = int2double(this.A);
        double[] Bdb = int2double(this.B);

        Plot plot = new Plot("Cytofluorogram between " + this.titleA + " and " + this.titleB, this.titleA, this.titleB, Adb, Bdb);
        double limHigh = Math.max(this.Amax, this.Bmax);
        double limLow = Math.min(this.Amin, this.Bmin);
        plot.setLimits(this.Amin, this.Amax, this.Bmin, this.Bmax);
        plot.setColor(Color.white);

        this.doThat = true;
        double[] tmp = linreg(this.A, this.B, 0, 0);
        double a = tmp[0];
        double b = tmp[1];
        double CoeffCorr = tmp[2];
        plot.draw();
        plot.setColor(Color.black);
        plot.addPoints(Adb, Bdb, 6);

        double[] xline = {limLow, limHigh};
        double[] yline = {a * limLow + b, a * limHigh + b};
        plot.setColor(Color.red);
        plot.addPoints(xline, yline, 2);

        //cyto.show();
        plot.show();
        IJ.log("\nCytofluorogram's parameters:\na: " + round(a, 3) + "\nb: " + round(b, 3) + "\nCorrelation coefficient: " + round(CoeffCorr, 3));

    }

    private double ICA() {
        double[] Anorm = new double[this.length];
        double[] Bnorm = new double[this.length];
        double AnormMean = 0;
        double BnormMean = 0;
        double prodMin = 0;
        double prodMax = 0;
        double lim = 0;
        double[] x = new double[this.length];
        double ICQ = 0;

        //Intensities are normalized to range from 0 to 1
        for (int i = 0; i < this.length; i++) {
            Anorm[i] = (double) (this.A[i] - this.Amin) / this.Amax;
            Bnorm[i] = (double) (this.B[i] - this.Bmin) / this.Bmax;

            AnormMean += Anorm[i];
            BnormMean += Bnorm[i];
        }
        AnormMean = AnormMean / this.length;
        BnormMean = BnormMean / this.length;

        for (int i = 0; i < this.length; i++) {
            x[i] = (Anorm[i] - AnormMean) * (Bnorm[i] - BnormMean);
            if (x[i] > prodMax) {
                prodMax = x[i];
            }
            if (x[i] < prodMin) {
                prodMin = x[i];
            }
            if (x[i] > 0) {
                ICQ++;
            }
        }

        if (Math.abs(prodMin) > Math.abs(prodMax)) {
            lim = Math.abs(prodMin);
        } else {
            lim = Math.abs(prodMax);
        }

        ICQ = ICQ / this.length - 0.5;
        
        
        if (verbose) {
            Plot plotA = new Plot("ICA A (" + this.titleA + ")", "(Ai-a)(Bi-b)", this.titleA, new double[]{0, 0}, new double[]{0, 0});
            plotA.setColor(Color.white);
            plotA.setLimits(-lim, lim, 0, 1);
            plotA.draw();
            plotA.setColor(Color.black);
            plotA.addPoints(x, Anorm, Plot.DOT);
            plotA.draw();

            plotA.setColor(Color.red);
            plotA.drawLine(0, 0, 0, 1);
            plotA.show();


            /*
            * double[] xline={0,0}; double[] yline={0,1};
            * plotA.setColor(Color.red); plotA.addPoints(xline, yline, Plot.LINE);
            *
            * plotA.show();
            */

            Plot plotB = new Plot("ICA B (" + this.titleB + ")", "(Ai-a)(Bi-b)", titleB, new double[]{0, 0}, new double[]{0, 0});
            plotB.setColor(Color.white);
            plotB.setLimits(-lim, lim, 0, 1);
            plotB.draw();
            plotB.setColor(Color.black);
            plotB.addPoints(x, Bnorm, Plot.DOT);


            plotB.setColor(Color.red);
            plotB.drawLine(0, 0, 0, 1);
            //plotB.addPoints(xline, yline, Plot.LINE);

            plotB.show();

            IJ.log("\nLi's Intensity correlation coefficient:\nICQ: " + ICQ);
        }
        return ICQ;

    }

    public double getICQ() {
        return ICA();
    }

    public void CostesRand(int xyBlock, int zBlock, int nbRand, double binWidth, int fillMeth, boolean xyRand, boolean zRand, boolean showRand) {
        int[] ACostes, BCostes, BRandCostes;

        if (fillMeth == 0) {
            this.widthCostes = ((int) (this.width / xyBlock)) * xyBlock;
            this.heightCostes = ((int) (this.height / xyBlock)) * xyBlock;
        } else {
            this.widthCostes = (((int) (this.width / xyBlock)) + 1) * xyBlock;
            this.heightCostes = (((int) (this.height / xyBlock)) + 1) * xyBlock;
        }

        if (zRand) {
            if (fillMeth == 0) {
                this.nbsliceCostes = ((int) (this.nbSlices / zBlock)) * zBlock;
            } else {
                this.nbsliceCostes = (((int) (this.nbSlices / zBlock)) + 1) * zBlock;
            }
            if (this.nbSlices == 1) {
                nbsliceCostes = 1;
            }

        } else {
            this.nbsliceCostes = this.nbSlices;
        }

        this.lengthCostes = this.widthCostes * this.heightCostes * this.nbsliceCostes;
        ACostes = new int[this.lengthCostes];
        BCostes = new int[this.lengthCostes];
        BRandCostes = new int[this.lengthCostes];

        int index = 0;
        for (int k = 1; k <= this.nbsliceCostes; k++) {
            for (int j = 0; j < this.heightCostes; j++) {
                for (int i = 0; i < this.widthCostes; i++) {
                    int offset = offset(i, j, k);
                    ACostes[index] = A[offset];
                    BCostes[index] = B[offset];
                    index++;
                }
            }
        }



        double direction;
        int shift;
        int newposition;
        if (xyRand || this.nbsliceCostes == 1) {
            //If slices independent 2D there is no need to take into account the z thickness and ranndomization along z axis should not be done
            zBlock = 1;
            zRand = false;
        }
        this.doThat = true;
        double r2test = linreg(ACostes, BCostes, 0, 0)[2];
        this.doThat = false;
        double[] arrayR = new double[nbRand];
        double mean = 0;
        double SD = 0;
        double Pval = 0;
        double[] arrayDistribR = new double[(int) (2 / binWidth + 1)];
        double[] x = new double[arrayDistribR.length];


        for (int f = 0; f < nbRand; f++) {

            //Randomization by shifting along x axis
            for (int e = 1; e <= this.nbsliceCostes - zBlock + 1; e += zBlock) {
                for (int d = 0; d < this.heightCostes - xyBlock + 1; d += xyBlock) {

                    //Randomization of the shift's direction
                    direction = 1;
                    if (Math.random() < 0.5) {
                        direction = -1;
                    }
                    //Randomization of the shift: should be a multiple of the xy block size
                    shift = ((int) (direction * Math.random() * this.widthCostes / xyBlock)) * xyBlock;

                    for (int a = 0; a < this.widthCostes; a++) {
                        for (int b = d; b < d + xyBlock; b++) {
                            for (int c = e; c < e + zBlock; c++) {
                                newposition = a + shift;
                                if (newposition >= this.widthCostes) {
                                    newposition -= this.widthCostes;
                                }
                                if (newposition < 0) {
                                    newposition += this.widthCostes;
                                }
                                BRandCostes[offsetCostes(newposition, b, c)] = BCostes[offsetCostes(a, b, c)];
                            }
                        }
                    }
                }
            }
            for (int i = 0; i < BCostes.length; i++) {
                BCostes[i] = BRandCostes[i];
            }

            //Randomization by shifting along y axis
            for (int e = 1; e <= this.nbsliceCostes - zBlock + 1; e += zBlock) {
                for (int d = 0; d < this.widthCostes - xyBlock + 1; d += xyBlock) {

                    //Randomization of the shift's direction
                    direction = 1;
                    if (Math.random() < 0.5) {
                        direction = -1;
                    }
                    //Randomization of the shift: should be a multiple of the xy block size
                    shift = ((int) (direction * Math.random() * this.heightCostes / xyBlock)) * xyBlock;

                    for (int a = 0; a < this.heightCostes; a++) {
                        for (int b = d; b < d + xyBlock; b++) {
                            for (int c = e; c < e + zBlock; c++) {
                                newposition = a + shift;
                                if (newposition >= this.heightCostes) {
                                    newposition -= this.heightCostes;
                                }
                                if (newposition < 0) {
                                    newposition += this.heightCostes;
                                }
                                BRandCostes[offsetCostes(b, newposition, c)] = BCostes[offsetCostes(b, a, c)];
                            }
                        }
                    }
                }
            }
            for (int i = 0; i < BCostes.length; i++) {
                BCostes[i] = BRandCostes[i];
            }

            if (zRand) {
                //Randomization by shifting along z axis
                for (int e = 0; e < this.heightCostes - xyBlock + 1; e += xyBlock) {
                    for (int d = 0; d < this.widthCostes - xyBlock + 1; d += xyBlock) {

                        //Randomization of the shift's direction
                        direction = 1;
                        if (Math.random() < 0.5) {
                            direction = -1;
                        }
                        //Randomization of the shift: should be a multiple of the z block size
                        shift = ((int) (direction * Math.random() * this.nbsliceCostes / zBlock)) * zBlock;

                        for (int a = 1; a <= this.nbsliceCostes; a++) {
                            for (int b = d; b < d + xyBlock; b++) {
                                for (int c = e; c < e + xyBlock; c++) {
                                    newposition = a + shift;
                                    if (newposition > this.nbsliceCostes) {
                                        newposition -= this.nbsliceCostes;
                                    }
                                    if (newposition < 1) {
                                        newposition += this.nbsliceCostes;
                                    }
                                    BRandCostes[offsetCostes(b, c, newposition)] = BCostes[offsetCostes(b, c, a)];
                                }
                            }
                        }
                    }
                }
                for (int i = 0; i < BCostes.length; i++) {
                    BCostes[i] = BRandCostes[i];
                }
            }
            arrayR[f] = linreg(ACostes, BCostes, 0, 0)[2];
            //if (arrayR[f]<r2test) Pval++;
            mean += arrayR[f];
            arrayDistribR[(int) ((arrayR[f] + 1) / binWidth)]++;
            x[(int) ((arrayR[f] + 1) / binWidth)] += arrayR[f];
            IJ.showStatus("Costes' randomization loop n�" + f + "/" + nbRand);
        }

        //Draw the last randomized image, if requiered
        if (showRand) {
            ImagePlus Rand = NewImage.createImage("Randomized images of " + this.titleB, this.widthCostes, this.heightCostes, this.nbsliceCostes, this.depth, 1);

            index = 0;
            for (int k = 1; k <= this.nbsliceCostes; k++) {
                Rand.setSlice(k);
                for (int j = 0; j < this.heightCostes; j++) {
                    for (int i = 0; i < this.widthCostes; i++) {
                        Rand.getProcessor().putPixel(i, j, BRandCostes[index]);
                        index++;
                    }
                }
            }
            Rand.setCalibration(this.cal);
            Rand.setSlice(1);
            Rand.show();
            IJ.setMinAndMax(this.Bmin, this.Bmax);
        }

        //Plots the r probability distribution
        double minx = -1;
        double maxx = 1;
        double maxy = 0;
        for (int i = 0; i < arrayDistribR.length; i++) {
            x[i] = arrayDistribR[i] == 0 ? i * binWidth - 1 + binWidth / 2 : x[i] / arrayDistribR[i];
        }
        for (int i = 0; i < arrayDistribR.length; i++) {
            arrayDistribR[i] /= nbRand;
        }

        for (int i = 0; i < arrayDistribR.length; i++) {
            //x[i]=i*binWidth-1+binWidth/2;
            if (minx == -1 && arrayDistribR[i] != 0) {
                minx = x[i];
            }
            if (maxy < arrayDistribR[i]) {
                maxy = arrayDistribR[i];
            }
        }
        minx = Math.min(minx, r2test);

        int i = arrayDistribR.length - 1;
        while (arrayDistribR[i] == 0) {
            maxx = x[i];
            i--;
        }

        maxx = Math.max(maxx, r2test);

        //Remove from arraDistribR all values equals to zero.
        int newLength = 0;
        for (i = 0; i < arrayDistribR.length; i++) {
            if (arrayDistribR[i] != 0) {
                newLength++;
            }
        }
        double[] xNew = new double[newLength], arrayNew = new double[newLength];
        newLength = 0;
        for (i = 0; i < arrayDistribR.length; i++) {
            if (arrayDistribR[i] != 0) {
                xNew[newLength] = x[i];
                arrayNew[newLength++] = arrayDistribR[i];
            }
        }
        x = xNew;
        arrayDistribR = arrayNew;


        Plot plot = new Plot("Costes' method (" + this.titleA + " & " + this.titleB + ")", "r", "Probability density of r", x, arrayDistribR);
        plot.setLimits(minx - 10 * binWidth, maxx + 10 * binWidth, 0, maxy * 1.05);
        plot.setColor(Color.white);
        plot.draw();

        //Previous plot is white, just to get values inserted into the plot list, the problem being that the plot is as default a line plot... Following line plots same values as circles.
        plot.setColor(Color.black);
        plot.addPoints(x, arrayDistribR, Plot.CIRCLE);

        //Draw the r line
        double[] xline = {r2test, r2test};
        double[] yline = {0, maxy * 1.05};
        plot.setColor(Color.red);
        plot.addPoints(xline, yline, 2);


        //Retrieves the mean, SD and P-value of the r distribution
        for (i = 1; i < nbRand; i++) {
            SD += Math.pow(arrayR[i] - mean, 2);
        }
        mean /= nbRand;
        SD = Math.sqrt(SD / (nbRand - 1));
        //Pval/=nbRand;


        IJ.log("\nCostes' randomization based colocalization:\nParameters: Nb of randomization rounds: " + nbRand + ", Resolution (bin width): " + binWidth);


        CurveFitter cf = new CurveFitter(x, arrayDistribR);
        double[] param = {0, maxy, mean, SD};
        cf.setInitialParameters(param);
        cf.doFit(CurveFitter.GAUSSIAN);
        param = cf.getParams();
        mean = param[2];
        SD = param[3];

        //Algorithm 26.2.17 from Abromowitz and Stegun, Handbook of Mathematical Functions for approximation of the cumulative density function (max. error=7.5e^-8). 
        double[] b = {0.319381530, -0.356563782, 1.781477937, -1.821255978, 1.330274429};
        double p = 0.2316419;
        double z = (1 / Math.sqrt(2 * Math.PI)) * Math.exp(-Math.pow((r2test - mean) / SD, 2) / 2);
        double t = 1 / (1 + p * Math.abs((r2test - mean) / SD));

        if (r2test >= 0) {
            Pval = 1 - z * t * (t * (t * (t * (t * b[4] + b[3]) + b[2]) + b[1]) + b[0]);
        } else {
            Pval = z * t * (t * (t * (t * (t * b[4] + b[3]) + b[2]) + b[1]) + b[0]);
        }

        IJ.log("r (original)=" + round(r2test, 3) + "\nr (randomized)=" + round(mean, 3) + "�" + round(SD, 3) + " (calculated from the fitted data)\nP-value=" + round(Pval * 100, 2) + "% (calculated from the fitted data)");

        IJ.log("\nResults for fitting the probability density function on a Gaussian (Probability=a+(b-a)exp(-(R-c)^2/(2d^2))):" + cf.getResultString() + "\nFWHM=" + Math.abs(round(2 * Math.sqrt(2 * Math.log(2)) * param[3], 3)));
        for (i = 0; i < x.length; i++) {
            arrayDistribR[i] = cf.f(CurveFitter.GAUSSIAN, param, x[i]);
        }
        plot.setColor(Color.BLUE);
        plot.addPoints(x, arrayDistribR, 2);
        plot.show();

    }

    //----------------------------------------------------------------------------------------------------------------------------------------------
    private void buildArray(ImagePlus imgA, ImagePlus imgB) {
        int index = 0;
        this.Amin = (int) Math.pow(2, this.depth);
        this.Amax = 0;
        this.Amean = 0;
        this.Bmin = this.Amin;
        this.Bmax = 0;
        this.Bmean = 0;

        for (int z = 1; z <= this.nbSlices; z++) {
            imgA.setSlice(z);
            imgB.setSlice(z);

            ImageStatistics stA = imgA.getStatistics();
            ImageStatistics stB = imgB.getStatistics();

            this.Amin = Math.min(this.Amin, (int) stA.min);
            this.Bmin = Math.min(this.Bmin, (int) stB.min);
            this.Amax = Math.max(this.Amax, (int) stA.max);
            this.Bmax = Math.max(this.Bmax, (int) stB.max);

            this.Amean += stA.pixelCount * stA.mean;
            this.Bmean += stB.pixelCount * stB.mean;

            for (int y = 0; y < this.height; y++) {
                for (int x = 0; x < this.width; x++) {
                    this.A[index] = imgA.getProcessor().getPixel(x, y);
                    this.B[index] = imgB.getProcessor().getPixel(x, y);
                    index++;
                }
            }

            this.Amean /= this.length;
            this.Bmean /= this.length;
        }
    }

    /**
     * Generates the ImagePlus base on the input array and title.
     *
     * @param array containing the pixels intensities (integer array).
     * @param title to attribute to the ImagePlus (string).
     */
    private ImagePlus buildImg(int[] array, String title) {
        int index = 0;
        double min = array[0];
        double max = array[0];
        ImagePlus img = NewImage.createImage(title, this.width, this.height, this.nbSlices, this.depth, 1);

        for (int z = 1; z <= this.nbSlices; z++) {
            IJ.showStatus("Creating the image...");
            img.setSlice(z);
            for (int y = 0; y < this.height; y++) {
                for (int x = 0; x < this.width; x++) {
                    int currVal = array[index];
                    min = Math.min(min, currVal);
                    max = Math.max(max, currVal);
                    img.getProcessor().putPixel(x, y, currVal);
                    index++;
                }
            }
        }
        IJ.showStatus("");
        img.setCalibration(this.micronCal);
        img.getProcessor().setMinAndMax(min, max);
        return img;
    }

    public double[] linreg(int[] Aarray, int[] Barray, int TA, int TB) {
        double num = 0;
        double den1 = 0;
        double den2 = 0;
        double[] coeff = new double[6];
        int count = 0;

        if (doThat) {
            sumA = 0;
            sumB = 0;
            sumAB = 0;
            sumsqrA = 0;
            Aarraymean = 0;
            Barraymean = 0;
            for (int m = 0; m < Aarray.length; m++) {
                if (Aarray[m] >= TA && Barray[m] >= TB) {
                    sumA += Aarray[m];
                    sumB += Barray[m];
                    sumAB += Aarray[m] * Barray[m];
                    sumsqrA += Math.pow(Aarray[m], 2);
                    count++;
                }
            }

            Aarraymean = sumA / count;
            Barraymean = sumB / count;
        }

        for (int m = 0; m < Aarray.length; m++) {
            if (Aarray[m] >= TA && Barray[m] >= TB) {
                num += (Aarray[m] - Aarraymean) * (Barray[m] - Barraymean);
                den1 += Math.pow((Aarray[m] - Aarraymean), 2);
                den2 += Math.pow((Barray[m] - Barraymean), 2);
            }
        }

        //0:a, 1:b, 2:corr coeff, 3: num, 4: den1, 5: den2
        coeff[0] = (count * sumAB - sumA * sumB) / (count * sumsqrA - Math.pow(sumA, 2));
        coeff[1] = (sumsqrA * sumB - sumA * sumAB) / (count * sumsqrA - Math.pow(sumA, 2));
        coeff[2] = num / (Math.sqrt(den1 * den2));
        coeff[3] = num;
        coeff[4] = den1;
        coeff[5] = den2;
        return coeff;
    }

    public double[] linregCostes(int[] Aarray, int[] Barray, int TA, int TB) {
        double num = 0;
        double den1 = 0;
        double den2 = 0;
        double[] coeff = new double[3];
        int count = 0;

        sumA = 0;
        sumB = 0;
        sumAB = 0;
        sumsqrA = 0;
        Aarraymean = 0;
        Barraymean = 0;

        for (int m = 0; m < Aarray.length; m++) {
            if (Aarray[m] < TA && Barray[m] < TB) {
                sumA += Aarray[m];
                sumB += Barray[m];
                sumAB += Aarray[m] * Barray[m];
                sumsqrA += Math.pow(Aarray[m], 2);
                count++;
            }
        }

        Aarraymean = sumA / count;
        Barraymean = sumB / count;


        for (int m = 0; m < Aarray.length; m++) {
            if (Aarray[m] < TA && Barray[m] < TB) {
                num += (Aarray[m] - Aarraymean) * (Barray[m] - Barraymean);
                den1 += Math.pow((Aarray[m] - Aarraymean), 2);
                den2 += Math.pow((Barray[m] - Barraymean), 2);
            }
        }

        coeff[0] = (count * sumAB - sumA * sumB) / (count * sumsqrA - Math.pow(sumA, 2));
        coeff[1] = (sumsqrA * sumB - sumA * sumAB) / (count * sumsqrA - Math.pow(sumA, 2));
        coeff[2] = num / (Math.sqrt(den1 * den2));
        return coeff;
    }

    private double[] int2double(int[] input) {
        double[] output = new double[input.length];
        for (int i = 0; i < input.length; i++) {
            output[i] = input[i];
        }
        return output;
    }

    /**
     * Returns the index where to find the informations corresponding to pixel
     * (x, y, z).
     *
     * @param x coordinate of the pixel.
     * @param y coordinate of the pixel.
     * @param z coordinate of the pixel.
     * @return the index where to find the informations corresponding to pixel
     * (x, y, z).
     */
    private int offset(int x, int y, int z) {
        if (x + y * this.width + (z - 1) * this.width * this.height >= this.width * this.height * this.nbSlices) {
            return this.width * this.height * this.nbSlices - 1;
        } else {
            if (x + y * this.width + (z - 1) * this.width * this.height < 0) {
                return 0;
            } else {
                return x + y * this.width + (z - 1) * this.width * this.height;
            }
        }
    }

    public int offsetCostes(int m, int n, int o) {
        if (m + n * this.widthCostes + (o - 1) * this.widthCostes * this.heightCostes >= this.widthCostes * this.heightCostes * this.nbsliceCostes) {
            return this.widthCostes * this.heightCostes * this.nbsliceCostes - 1;
        } else {
            if (m + n * this.widthCostes + (o - 1) * this.widthCostes * this.heightCostes < 0) {
                return 0;
            } else {
                return m + n * this.widthCostes + (o - 1) * this.widthCostes * this.heightCostes;
            }
        }
    }

    public double round(double y, int z) {
        //Special tip to round numbers to 10^-2
        y *= Math.pow(10, z);
        y = (int) y;
        y /= Math.pow(10, z);
        return y;
    }
}