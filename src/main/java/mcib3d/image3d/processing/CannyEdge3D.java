/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mcib3d.image3d.processing;

import ij.IJ;

import static java.lang.Math.sqrt;

import mcib3d.image3d.ImageFloat;
import mcib3d.image3d.ImageHandler;
import mcib3d.utils.ThreadUtil;

/**
 * @author thomasb
 */
public class CannyEdge3D {

    ImageHandler input;
    double alpha;

    ImageFloat[] grads = null;
    ImageFloat edge = null;

    public CannyEdge3D(ImageHandler input, double alpha) {
        this.input = input;
        this.alpha = alpha;
    }

    public ImageHandler getInput() {
        return input;
    }

    public void setInput(ImageHandler input) {
        this.input = input;
        grads = null;
        edge = null;
    }

    public double getAlpha() {
        return alpha;
    }

    public void setAlpha(double alpha) {
        this.alpha = alpha;
        grads = null;
        edge = null;
    }

    private void computeGradX() {
        double[] line;
        double[] res;
        CannyDeriche1D canny;

        // TODO MULTITHREAD
        grads[0] = new ImageFloat("EdgeX", input.sizeX, input.sizeY, input.sizeZ);
        for (int z = 0; z < input.sizeZ; z++) {
            IJ.showStatus("Edge X " + z + "/" + input.sizeZ);
            for (int y = 0; y < input.sizeY; y++) {
                line = input.getLineX(0, y, z, input.sizeX);
                canny = new CannyDeriche1D(line, alpha);
                res = canny.getCannyDeriche();
                grads[0].setLineX(0, y, z, res);
            }
        }
    }

    private void computeGradY() {
        double[] line;
        double[] res;
        CannyDeriche1D canny;

        // TODO MULTITHREAD
        grads[1] = new ImageFloat("EdgeY", input.sizeX, input.sizeY, input.sizeZ);
        for (int z = 0; z < input.sizeZ; z++) {
            IJ.showStatus("Edge Y " + z + "/" + input.sizeZ);
            for (int x = 0; x < input.sizeX; x++) {
                line = input.getLineY(x, 0, z, input.sizeY);
                canny = new CannyDeriche1D(line, alpha);
                res = canny.getCannyDeriche();
                grads[1].setLineY(x, 0, z, res);
            }
        }
    }

    private void computeGradZ() {
        double[] line;
        double[] res;
        CannyDeriche1D canny;

        // TODO MULTITHREAD
        grads[2] = new ImageFloat("EdgeZ", input.sizeX, input.sizeY, input.sizeZ);
        for (int x = 0; x < input.sizeX; x++) {
            IJ.showStatus("Edge Z " + x + "/" + input.sizeX);
            for (int y = 0; y < input.sizeY; y++) {
                line = input.getLineZ(x, y, 0, input.sizeZ);
                canny = new CannyDeriche1D(line, alpha);
                res = canny.getCannyDeriche();
                grads[2].setLineZ(x, y, 0, res);
            }
        }
    }

    private void computeGradient() {
        if (grads == null) {
            grads = new ImageFloat[3];
        }
        if(ThreadUtil.getNbCpus()<4) {
            computeGradX();
            computeGradY();
            computeGradZ();
        }
        else {
           Thread[] threads=ThreadUtil.createThreadArray(3);
           threads[0]=new Thread(){
               public void run(){
                   computeGradX();
               }
           };
            threads[1]=new Thread(){
                public void run(){
                    computeGradY();
                }
            };
            threads[2]=new Thread(){
                public void run(){
                    computeGradZ();
                }
            };
            ThreadUtil.startAndJoin(threads);
        }
    }

    public ImageHandler[] getGradientsXYZ() {
        if (grads == null) {
            computeGradient();
        }
        return grads;
    }

    private void computeEdge() {
        if (grads == null) {
            computeGradient();
        }
        edge = new ImageFloat("Edge", input.sizeX, input.sizeY, input.sizeZ);
        for (int z = 0; z < input.sizeZ; z++) {
            IJ.showStatus("Edge " + z + "/" + input.sizeZ);
            for (int x = 0; x < input.sizeX; x++) {
                for (int y = 0; y < input.sizeY; y++) {
                    float ex = grads[0].getPixel(x, y, z);
                    float ey = grads[1].getPixel(x, y, z);
                    float ez = grads[2].getPixel(x, y, z);
                    float ee = (float) sqrt(ex * ex + ey * ey + ez * ez);
                    edge.setPixel(x, y, z, ee);
                }
            }
        }
    }

    public ImageHandler getEdge() {
        if (edge == null) {
            computeEdge();
        }
        return edge;
    }
}
