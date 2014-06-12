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

import mcib3d.image3d.ImageByte;
import mcib3d.image3d.ImageHandler;
import mcib3d.image3d.ImageInt;
import tango.util.ImageUtils;


public class GLCMTexture3D {

    ImageInt maskResampled;
    ImageByte intensityResampled;
    double[][] glcm;
    public boolean verbose = false;
    double[][] glcm0;
    
    public GLCMTexture3D(ImageHandler intensity, ImageInt mask, boolean resample, boolean normalize) {
        init( intensity,  mask,  resample, normalize);
    }
    
    private void init(ImageHandler intensity, ImageInt mask, boolean resample, boolean normalize) {
        if (!(intensity instanceof ImageByte)) intensityResampled = new ImageByte(intensity, true);
        else intensityResampled = (ImageByte)intensity;
        if (resample) {
            int newZ = (int)(mask.sizeZ * mask.getScaleZ()/mask.getScaleXY()+0.5);
            maskResampled=mask.resample(newZ, ij.process.ImageProcessor.NEAREST_NEIGHBOR);
            intensityResampled=intensityResampled.resample(newZ, ij.process.ImageProcessor.BICUBIC);
        } else {
            maskResampled=mask;
        }
        if (normalize) computeRefMatrix();
        
    }
    
    public void computeMatrix(int radius) {
        int[][] neighbor=ImageUtils.getNeigh(radius, radius, 1, true);
        double count=0;
        int zz, xx, yy, xy2;
        glcm = new double[256][256];
        for (int z = 0; z<maskResampled.sizeZ; z++) {
            for (int y = 0; y<maskResampled.sizeY; y++) {
                for (int x= 0; x<maskResampled.sizeX; x++) {
                    int xy = x+y*maskResampled.sizeX;
                    if (maskResampled.getPixel(xy, z)!=0) {
                        int value = intensityResampled.getPixelInt(xy, z);
                        for (int i = 0; i<neighbor.length; i++) {
                            zz = z + neighbor[2][i];
                            if (zz<maskResampled.sizeZ) {
                                xx= neighbor[0][i]+x;
                                if (xx<maskResampled.sizeX) {
                                    yy= neighbor[1][i]+y;
                                    if (yy<maskResampled.sizeY) {
                                        xy2 = xx+yy*maskResampled.sizeX;
                                        if (maskResampled.getPixel(xy2, zz)!=0) {
                                            int value2 = intensityResampled.getPixelInt(xy2, zz);
                                            glcm[value][value2] += 1;
                                            glcm[value2][value] += 1;
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
        for (int i = 0; i < 256; i++) {
            for (int j = 0; j < 256; j++) {
                glcm[i][j] = (glcm[i][j]) / (count);
            }
        }
    }
    
    private void computeRefMatrix() {
        double count=0;
        glcm0 = new double[256][256];
        for (int z = 0; z<maskResampled.sizeZ; z++) {
            for (int y = 0; y<maskResampled.sizeY; y++) {
                for (int x= 0; x<maskResampled.sizeX; x++) {
                    int xy = x+y*maskResampled.sizeX;
                    if (maskResampled.getPixel(xy, z)!=0) {
                        int value = intensityResampled.getPixelInt(xy, z);
                        glcm0[value][value] += 1;
                        count++;
                    }
                }
            }
        }
        for (int i = 0; i < 256; i++) {
            for (int j = 0; j < 256; j++) {
                glcm0[i][j] = (glcm0[i][j]) / (count);
            }
        }
    }


//=====================================================================================================
// This part calculates the angular second moment; the value is stored in asm

    public double getASM() {
        double asm = 0.0;
        for (int a = 0; a < 256; a++) {
            for (int b = 0; b < 256; b++) {
                asm = asm + (glcm[a][b] * glcm[a][b]);
            }
        }
        if (glcm0!=null) return asm/getASM0();
        return asm;
    }
    
    private double getASM0() {
        double asm = 0.0;
        for (int a = 0; a < 256; a++) {
            for (int b = 0; b < 256; b++) {
                asm = asm + (glcm0[a][b] * glcm0[a][b]);
            }
        }
        return asm;
    }
//=====================================================================================================
// This part calculates the contrast; the value is stored in contrast

    public double getContrast() {
        double contrast = 0.0;
        for (int a = 0; a < 256; a++) {
            for (int b = 0; b < 256; b++) {
                contrast = contrast + (a - b) * (a - b) * (glcm[a][b]);
            }
        }
        return contrast;
    }
//=====================================================================================================
//This part calculates the correlation; the value is stored in correlation
// px []  and py [] are arrays to calculate the correlation
// meanx and meany are variables  to calculate the correlation
//  stdevx and stdevy are variables to calculate the correlation

    public double getCorrelation() {

//First step in the calculations will be to calculate px [] and py []
        double correlation = 0.0;
        double px = 0;
        double py = 0;
        double meanx = 0.0;
        double meany = 0.0;
        double stdevx = 0.0;
        double stdevy = 0.0;

        for (int a = 0; a < 256; a++) {
            for (int b = 0; b < 256; b++) {
                px = px + a * glcm[a][b];
                py = py + b * glcm[a][b];

            }
        }
// Now calculate the standard deviations
        for (int a = 0; a < 256; a++) {
            for (int b = 0; b < 256; b++) {
                stdevx = stdevx + (a - px) * (a - px) * glcm[a][b];
                stdevy = stdevy + (b - py) * (b - py) * glcm[a][b];
            }
        }
// Now finally calculate the correlation parameter

        for (int a = 0; a < 256; a++) {
            for (int b = 0; b < 256; b++) {
                correlation = correlation + ((a - px) * (b - py) * glcm[a][b] / (stdevx * stdevy));
            }
        }

        return correlation;
    }
//===============================================================================================
// This part calculates the inverse difference moment

    public double getIDM() {
        double IDM = 0.0;
        for (int a = 0; a < 256; a++) {
            for (int b = 0; b < 256; b++) {
                IDM = IDM + (glcm[a][b] / (1 + (a - b) * (a - b)));
            }
        }
        if (glcm0!=null) return IDM / getIDM0();
        return IDM;

    }
    
    private double getIDM0() {
        double IDM = 0.0;
        for (int a = 0; a < 256; a++) {
            for (int b = 0; b < 256; b++) {
                IDM = IDM + (glcm0[a][b] / (1 + (a - b) * (a - b)));
            }
        }
        return IDM;

    }
//===============================================================================================
// This part calculates the entropy

    public double getEntropy() {
        double entropy = 0.0;
        for (int a = 0; a < 256; a++) {
            for (int b = 0; b < 256; b++) {
                if (glcm[a][b] == 0) {
                } else {
                    entropy = entropy - (glcm[a][b] * (Math.log(glcm[a][b])));
                }
            }
        }
        if (glcm0!=null) return entropy-getEntropy0();
        return entropy;

    }
    
    private double getEntropy0() {
        double entropy = 0.0;
        for (int a = 0; a < 256; a++) {
            for (int b = 0; b < 256; b++) {
                if (glcm0[a][b] == 0) {
                } else {
                    entropy = entropy - (glcm0[a][b] * (Math.log(glcm0[a][b])));
                }
            }
        }
        return entropy;

    }

    public double getSum() {
        double suma = 0.0;
        for (int a = 0; a < 256; a++) {
            for (int b = 0; b < 256; b++) {
                suma = suma + glcm[a][b];
            }
        }
        return suma;
    }
}
