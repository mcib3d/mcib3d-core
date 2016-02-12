/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mcib3d.image3d.processing;

import ij.IJ;
import static java.lang.Math.pow;
import mcib3d.geom.Point3D;
import mcib3d.geom.Vector3D;
import mcib3d.image3d.ImageFloat;
import mcib3d.image3d.ImageHandler;

/**
 * From the article Gertych et al., Computers in Biology and Medicine 2015
 * 
 * @author thomasb
 */
public class SymmetryFilter {

    ImageHandler[] edges = null;
    int radius = 0;
    double normalize = 10;
    double scaling = 2;
    ImageHandler bin1 = null, bin2 = null;
    boolean improved = false;

    ImageHandler sym = null, sym2 = null;

    public SymmetryFilter(ImageHandler[] edges, int radius, boolean improved) {
        this.edges = edges;
        this.radius = radius;
        this.improved = improved;
    }

    private void computeBins() {
        ImageHandler img = edges[0];
        bin1 = new ImageFloat("bin1", img.sizeX, img.sizeY, img.sizeZ);
        bin2 = new ImageFloat("bin2", img.sizeX, img.sizeY, img.sizeZ);
        for (int z = 0; z < img.sizeZ; z++) {
            IJ.showStatus("Symmetry " + z + "/" + img.sizeZ);
            for (int x = 0; x < img.sizeX; x++) {
                for (int y = 0; y < img.sizeY; y++) {
                    double ex = edges[0].getPixel(x, y, z);
                    double ey = edges[1].getPixel(x, y, z);
                    double ez = edges[2].getPixel(x, y, z);
                    double ee = Math.sqrt(ex * ex + ey * ey + ez * ez);
                    // bin
                    Vector3D grad = new Vector3D(ex, ey, ez);
                    grad.normalize();
                    if (grad.getLength() == 0) {
                        continue;
                    }
                    Point3D pos = new Vector3D(x, y, z);
                    for (int d = 0; d < radius; d++) {
                        pos.translate(grad);
                        if ((d > 0) && (img.contains(pos.getRoundX(), pos.getRoundY(), pos.getRoundZ()))) {
                            bin1.setPixelIncrement(pos, 1);
                            if (improved) {
                                bin2.setPixelIncrement(pos, (float) (d * ee));
                            } else {
                                bin2.setPixelIncrement(pos, (float) (ee));
                            }
                        }
                    }
                }
            }
        }
    }

    private void computeSymmetry() {
        if (bin1 == null) {
            computeBins();
        }
        ImageHandler bin22 = bin2.duplicate();
        ImageHandler bin11 = bin1.duplicate();
        bin22.multiplyByValue((float) scaling);
        bin11.multiplyByValue((float) pow(normalize, scaling));
        sym = bin11.addImage(bin22, 1, 1);
        sym.setTitle("Symmetry_" + radius);
        sym2 = sym.duplicate();
        ij.plugin.GaussianBlur3D.blur(sym2.getImagePlus(), 2, 2, 2);
        sym2.setTitle("Symmetry_" + radius + "_smoothed");
    }

    public ImageHandler[] getIntermediates() {
        if (bin1 == null) {
            computeBins();
        }

        return new ImageHandler[]{bin1, bin2};
    }

    public ImageHandler getSymmetry(boolean smoothed) {
        if (sym == null) {
            computeSymmetry();
        }
        if (smoothed) {
            return sym2;
        } else {
            return sym;
        }
    }

    public ImageHandler[] getEdges() {
        return edges;
    }

    public void setEdges(ImageHandler[] edges) {
        this.edges = edges;
        bin1 = null;
        sym = null;
    }

    public int getRadius() {
        return radius;
    }

    public void setRadius(int radius) {
        this.radius = radius;
        bin1 = null;
        sym = null;
    }

    public void setNormalize(double normalize) {
        this.normalize = normalize;
        sym = null;
    }

    public void setScaling(double scaling) {
        this.scaling = scaling;
        sym = null;
    }

    public void setImproved(boolean improved) {
        this.improved = improved;
        bin1 = null;
        sym = null;
    }

}
