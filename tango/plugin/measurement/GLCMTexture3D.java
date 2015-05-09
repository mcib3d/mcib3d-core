package tango.plugin.measurement;


/**
 *
 **
 * /**
 * Copyright (C) 2012- 2013 Jean OLLION
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
 * @author Jean Ollion, adapted from GLCM texture by Julio E Cabrera
 */

import ij.IJ;
import java.util.Arrays;
import mcib3d.Jama.Matrix;
import mcib3d.image3d.ImageByte;
import mcib3d.image3d.ImageHandler;
import mcib3d.image3d.ImageInt;
import tango.util.ImageUtils;


public class GLCMTexture3D {
    // GLCM averaged in all directions
    ImageInt maskResampled;
    ImageByte intensityResampled;
    double[][] glcm;
    double meanGrayValue;
    public boolean verbose = false;
    double[][] glcm0;
    int numberOfGrayValues=256;
    float ZFactor;
    public GLCMTexture3D(ImageHandler intensity, ImageInt mask, int numberofGrayValues, int Zresample, float ZFactor) {
        this.ZFactor=ZFactor;
        this.numberOfGrayValues=numberofGrayValues;
        if (!(intensity instanceof ImageByte)) intensityResampled = new ImageByte(intensity, true);
        else intensityResampled = (ImageByte)intensity;
        if (Zresample>0) {
            int newZ = (int)(mask.sizeZ * mask.getScaleZ()/mask.getScaleXY()+0.5);
            maskResampled=mask.resample(newZ, ij.process.ImageProcessor.NEAREST_NEIGHBOR);
            intensityResampled=intensityResampled.resample(newZ, Zresample==1 ? ij.process.ImageProcessor.BILINEAR : ij.process.ImageProcessor.BICUBIC);
        } else {
            maskResampled=mask;
        }
    }
    
    public void computeMatrix(int radius) {
        int[][] neighbor=ImageUtils.getHalfNeighbourhood(radius, radius * ZFactor, 1);
        double count=0;
        int zz, xx, yy, xy2;
        double factor =  numberOfGrayValues / 256d;
        meanGrayValue=0;
        glcm = new double[numberOfGrayValues][numberOfGrayValues];
        for (int z = 0; z<maskResampled.sizeZ; z++) {
            for (int y = 0; y<maskResampled.sizeY; y++) {
                for (int x= 0; x<maskResampled.sizeX; x++) {
                    int xy = x+y*maskResampled.sizeX;
                    if (maskResampled.getPixel(xy, z)!=0) {
                        int value = intensityResampled.getPixelInt(xy, z);
                        if (numberOfGrayValues!=256) value = (int) (value * factor);
                        meanGrayValue+=value;
                        for (int i = 0; i<neighbor[0].length; i++) {
                            zz = z + neighbor[2][i];
                            if (zz<maskResampled.sizeZ && zz>=0) {
                                xx= neighbor[0][i]+x;
                                if (xx<maskResampled.sizeX && xx>=0) {
                                    yy= neighbor[1][i]+y;
                                    if (yy<maskResampled.sizeY && yy>=0) {
                                        xy2 = xx+yy*maskResampled.sizeX;
                                        if (maskResampled.getPixel(xy2, zz)!=0) {
                                            int value2 = intensityResampled.getPixelInt(xy2, zz);
                                            if (numberOfGrayValues!=256) value2 = (int) (value2 * factor);
                                            glcm[value][value2] ++;
                                            glcm[value2][value] ++;
                                            count+=2;
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        if (count>0) {
            meanGrayValue/=count;
            for (int i = 0; i < numberOfGrayValues; i++) {
                for (int j = 0; j < numberOfGrayValues; j++) {
                    glcm[i][j] = (glcm[i][j]) / (count);
                }
            }
        }
    }
    
    private void computeRefMatrix() {
        double count=0;
        glcm0 = new double[numberOfGrayValues][numberOfGrayValues];
        double factor =  numberOfGrayValues / 256d;
        for (int z = 0; z<maskResampled.sizeZ; z++) {
            for (int xy = 0; xy<maskResampled.sizeXY; xy++) {
                if (maskResampled.getPixel(xy, z)!=0) {
                    int value = intensityResampled.getPixelInt(xy, z);
                    if (numberOfGrayValues!=256) value = (int) (value * factor);
                    glcm0[value][value] ++;
                    count++;
                } 
            }
        }
        for (int i = 0; i < numberOfGrayValues; i++) {
            for (int j = 0; j < numberOfGrayValues; j++) {
                glcm0[i][j] = (glcm0[i][j]) / (count);
            }
        }
    }
    
    public double[] computeTextureParameters(boolean normalize) {
        double[] features = computeTextureParameters(glcm, meanGrayValue);
        if (normalize) {
            if (glcm0==null) computeRefMatrix();
            double[] features0 = computeTextureParameters(glcm0, meanGrayValue);
            /*for (int i = 0; i<features.length; i++) {
                IJ.log("feature: "+i + " value:"+features[i]+ " ref:"+features0[i]);
            }*/
            features[0]/=features0[0];
            features[2]/=features0[2];
            features[3]/=features0[3];
            features[5]/=features0[5];
            features[6]/=features0[6];
            features[7]-=features0[7]; // entropy additive
            features[8]-=features0[8];// entropy additive
            /*IJ.log("after norm");
            for (int i = 0; i<features.length; i++) {
                IJ.log("feature: "+i + " value:"+features[i]);
            }*/
        }
        return features;
    }
    
    public double[] computeTextureParametersRef() {
        return computeTextureParameters(glcm0, meanGrayValue);
    }

    /**
     * Returns the logarithm of the specified value.
     *
     * @param glcm the value glcm
     * @param meanGrayValue the mean gray value
     * @return array{Angular 2nd moment, Contrast, Correlation, variance, Inverse Difference Moment, Sum Average, Sum Variance, Sum Entropy, Entropy, Difference Variance, Difference Entropy, Information Measures of Correlation 1, Information Measures of Correlation 2, Maximum Correlation Coefficient }
     */
    private double[] computeTextureParameters(double[][] glcm, double meanGrayValue) {
        // stats 
        double[] p_x_plus_y = new double[2 * numberOfGrayValues - 1];
        double[] p_x_minus_y = new double[numberOfGrayValues];

        double mu_x = 0;
        /**
         * column mean value
         */
         double mu_y = 0;
        /**
         * row variance
         */
         double var_x = 0;
        /**
         * column variance
         */
         double var_y = 0;
        /**
         * HXY1 statistics
         */
         double hx = 0;
        /**
         * HXY2 statistics
         */
         double hy = 0;
        /**
         * HXY1 statistics
         */
         double hxy1 = 0;
        /**
         * HXY2 statistics
         */
        double hxy2 = 0;
        /**
         * p_x statistics
         */
         double[] p_x = new double[numberOfGrayValues];
        /**
         * p_y statistics
         */
         double[] p_y = new double[numberOfGrayValues];

        // p_x, p_y, p_x+y, p_x-y
        for (int i = 0; i < numberOfGrayValues; i++) {
            for (int j = 0; j < numberOfGrayValues; j++) {
                double p_ij = glcm[i][j];

                p_x[i] += p_ij;
                p_y[j] += p_ij;

                p_x_plus_y[i + j] += p_ij;
                p_x_minus_y[Math.abs(i - j)] += p_ij;
            }
        }

        // mean and variance values
        double[] meanVar;
        meanVar = meanVar(p_x);
        mu_x = meanVar[0];
        var_x = meanVar[1];
        meanVar = meanVar(p_y);
        mu_y = meanVar[0];
        var_y = meanVar[1];

        for (int i = 0; i < numberOfGrayValues; i++) {
            // hx and hy
            hx += p_x[i] * log(p_x[i]);
            hy += p_y[i] * log(p_y[i]);

            // hxy1 and hxy2
            for (int j = 0; j < numberOfGrayValues; j++) {
                double p_ij = glcm[i][j];
                hxy1 += p_ij * log(p_x[i] * p_y[j]);
                hxy2 += p_x[i] * p_y[j] * log(p_x[i] * p_y[j]);
            }
        }
        hx *= -1;
        hy *= -1;
        hxy1 *= -1;
        hxy2 *= -1;
        
        double[] features = new double[13];

        //double[][] Q = new double[numberOfGrayValues][numberOfGrayValues];
        for (int i = 0; i < numberOfGrayValues; i++) {
            double sum_j_p_x_minus_y = 0;
            for (int j = 0; j < numberOfGrayValues; j++) {
                double p_ij = glcm[i][j];

                sum_j_p_x_minus_y += j * p_x_minus_y[j];

                features[0] += p_ij * p_ij;
                features[2] += i * j * p_ij - mu_x * mu_y;
                features[3] += (i - meanGrayValue) * (i - meanGrayValue) * p_ij;
                features[4] += p_ij / (1 + (i - j) * (i - j));
                features[8] += p_ij * log(p_ij);
                /*
                // feature 13
                if (p_ij != 0 && p_x[i] != 0) { // would result in 0
                    for (int k = 0; k < numberOfGrayValues; k++) {
                        if (p_y[k] != 0 && glcm[j][k] != 0) { // would result in NaN
                            Q[i][j] += (p_ij * glcm[j][k]) / (p_x[i] * p_y[k]);
                        }
                    }
                }
                */
            }

            features[1] += i * i * p_x_minus_y[i];
            features[9] += (i - sum_j_p_x_minus_y) * (i - sum_j_p_x_minus_y) * p_x_minus_y[i];
            features[10] += p_x_minus_y[i] * log(p_x_minus_y[i]);
        }
        
        /*
        // feature 13: Max Correlation Coefficient
        double[] realEigenvaluesOfQ = new Matrix(Q).eig().getRealEigenvalues();
        for (int i = 0; i<realEigenvaluesOfQ.length; i++) realEigenvaluesOfQ[i] = Math.abs(realEigenvaluesOfQ[i]);
        Arrays.sort(realEigenvaluesOfQ);
        features[13] = Math.sqrt(realEigenvaluesOfQ[realEigenvaluesOfQ.length - 2]);
        */
        features[2] /= Math.sqrt(var_x * var_y);
        features[8] *= -1;
        features[10] *= -1;
        double maxhxhy = Math.max(hx, hy);
        if (Math.signum(maxhxhy) == 0) {
            features[11] = 0;
        } else {
            features[11] = (features[8] - hxy1) / maxhxhy;
        }
        features[12] = Math.sqrt(1 - Math.exp(-2 * (hxy2 - features[8])));
        
        for (int i = 0; i < 2 * numberOfGrayValues - 1; i++) {
            features[5] += i * p_x_plus_y[i];
            features[7] += p_x_plus_y[i] * log(p_x_plus_y[i]);

            /*double sum_j_p_x_plus_y = 0;
            for (int j = 0; j < 2 * numberOfGrayValues - 1; j++) {
                sum_j_p_x_plus_y += j * p_x_plus_y[j];
            }
            features[6] += (i - sum_j_p_x_plus_y) * (i - sum_j_p_x_plus_y) * p_x_plus_y[i];
            */
        }
        for (int i = 0; i < 2 * numberOfGrayValues - 1; i++) {
            features[6] += (i - features[5]) * (i - features[5]) * p_x_plus_y[i];
        }

        features[7] *= -1;
        return features;
    }
    
        /**
     * Compute mean and variance of the given array
     *
     * @param a input values
     * @return array{mean, variance}
     */
    private double[] meanVar(double[] a) {
        // VAR(X) = E(X^2) - E(X)^2
        double ex = 0, ex2 = 0; // E(X), E(X^2)
        for (int i = 0; i < numberOfGrayValues; i++) {
            ex += a[i];
            ex2 += a[i] * a[i];
        }
        ex /= a.length;
        ex2 /= a.length;
        double var = ex2 - ex * ex;

        return new double[]{ex, var};
    }
    /**
     * Returns the logarithm of the specified value.
     *
     * @param value the value for which the logarithm should be returned
     * @return the logarithm of the specified value
     */
    private double log(double value) {
        if (value<=0) return 0;
        else return Math.log(value);
    }
}
