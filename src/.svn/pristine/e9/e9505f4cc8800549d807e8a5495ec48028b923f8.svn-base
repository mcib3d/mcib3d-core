package mcib3d.geom;

import ij.ImagePlus;
import ij.ImageStack;
import ij.gui.Roi;
import ij.plugin.filter.ThresholdToSelection;
import ij.process.ByteProcessor;
import ij.process.ImageProcessor;
import java.awt.Color;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Iterator;
import mcib3d.image3d.ImageHandler;

/**
 *
 * @author jean
 */
public class Object3DFuzzy extends Object3DVoxels {

    public Object3DFuzzy(int label, ArrayList<Voxel3D> voxels) {
        this.value = label;
        this.voxels = voxels;
        init();
    }

    @Override
    public ArrayList<Voxel3D> getContours() {
        return this.contours;
    }
    // TODO : methode "propre" de calcul des contours .. (coord subvoxelaires.. )

    /*public double colocPixels(Object3DFuzzy other) {
     double res=0;
     //NEEDS A COMPARATOR FOR VOXEL3D THAT TAKES ONLY THE COORDS IN ACCOUNT
     /*if (other.voxels.size()>voxels.size()) {
     ArrayList<Voxel3D> coloc=new ArrayList<Voxel3D>(voxels);
     coloc.retainAll(other.voxels);
     for (Voxel3D key : coloc) {
     res+=Math.sqrt(key.getValue()*other.voxels.get(other.voxels.indexOf(key)).getValue());
     }
     } else {
     ArrayList<Voxel3D> coloc=new ArrayList<Voxel3D>(other.voxels);
     coloc.retainAll(voxels);
     for (Voxel3D key : coloc) {
     res+=Math.sqrt(key.getValue()*voxels.get(voxels.indexOf(key)).getValue());
     }
     }
          
         
     return res;
     }
     * 
     */
    public void setContours(ArrayList<Voxel3D> contours) {
        this.contours = contours;
    }

    @Override
    protected void computeCenter() {
        bx = 0;
        by = 0;
        bz = 0;
        volume = 0;
        double weight;
        for (Voxel3D vox : voxels) {
            //weight=vox.getValue();
            weight = 1;
            bx += vox.getX() * weight;
            by += vox.getY() * weight;
            bz += vox.getZ() * weight;
            volume += weight;
        }
        if (volume != 0) {
            bx /= volume;
            by /= volume;
            bz /= volume;
        }
    }

    @Override
    protected void computeMassCenter(ImageHandler ima) {
        if (ima != null) {
            cx = 0;
            cy = 0;
            cz = 0;
            double sum = 0;
            double sum2 = 0;
            double pix;
            double pmin = Double.MAX_VALUE;
            double pmax = -Double.MAX_VALUE;
            for (Voxel3D vox : voxels) {
                pix = ima.getPixel(vox) * vox.getValue();
                cx += vox.getX() * pix;
                cy += vox.getY() * pix;
                cz += vox.getZ() * pix;
                sum += pix;
                sum2 += pix * pix;
                if (pix > pmax) {
                    pmax = pix;
                }
                if (pix < pmin) {
                    pmin = pix;
                }
            }
            cx /= sum;
            cy /= sum;
            cz /= sum;

            integratedDensity = sum;

            pixmin = pmin;
            pixmax = pmax;

            // standard dev
            int vol = getVolumePixels();
            sigma = Math.sqrt((sum2 - ((sum * sum) / vol)) / (vol - 1));
        }
    }

    @Override
    protected void computeBounding() {
        xmin = Integer.MAX_VALUE;
        xmax = 0;
        ymin = Integer.MAX_VALUE;
        ymax = 0;
        zmin = Integer.MAX_VALUE;
        zmax = 0;

        for (Voxel3D vox : voxels) {
            if (vox.getX() < xmin) {
                xmin = (int) vox.getX();
            }
            if (vox.getX() > xmax) {
                xmax = (int) vox.getX();
            }
            if (vox.getY() < ymin) {
                ymin = (int) vox.getY();
            }
            if (vox.getY() > ymax) {
                ymax = (int) vox.getY();
            }
            if (vox.getZ() < zmin) {
                zmin = (int) vox.getZ();
            }
            if (vox.getZ() > zmax) {
                zmax = (int) vox.getZ();
            }
        }
    }

    @Override
    public void computeContours() {
        //area
        //areaUnit
    }

    @Override
    public void computeMoments2(boolean normalize) {
        s200 = 0;
        s110 = 0;
        s101 = 0;
        s020 = 0;
        s011 = 0;
        s002 = 0;

        double i, j, k;
        for (Voxel3D vox : voxels) {
            i = vox.getX();
            j = vox.getY();
            k = vox.getZ();
            s200 += (i - bx) * (i - bx);
            s020 += (j - by) * (j - by);
            s002 += (k - bz) * (k - bz);
            s110 += (i - bx) * (j - by);
            s101 += (i - bx) * (k - bz);
            s011 += (j - by) * (k - bz);
        }
        // resolution
        s200 *= resXY * resXY;
        s020 *= resXY * resXY;
        s002 *= resZ * resZ;
        s110 *= resXY * resXY;
        s101 *= resXY * resZ;
        s011 *= resXY * resZ;
        // normalize by volume
        if (normalize) {
            s200 /= volume;
            s020 /= volume;
            s002 /= volume;
            s110 /= volume;
            s101 /= volume;
            s011 /= volume;
        }

        eigen = null;
    }

    @Override
    public Voxel3D getPixelMax(ImageHandler ima) {
        Voxel3D res = null;
        float pix;
        float max = -Float.MAX_VALUE;

        for (Voxel3D vox : voxels) {
            pix = ima.getPixel(vox);
            if (pix > max) {
                max = pix;
                res = new Voxel3D(vox);
            }
        }

        return res;
    }

    public Voxel3D getPixelMin(ImageHandler ima) {
        Voxel3D res = null;
        float pix;
        float min = Float.MAX_VALUE;

        for (Voxel3D vox : voxels) {
            pix = ima.getPixel(vox);
            if (pix < min) {
                min = pix;
                res = new Voxel3D(vox);
            }
        }

        return res;
    }

    @Override
    public ArrayList listVoxels(ImageHandler ima) {
        ArrayList vector = new ArrayList();
        Voxel3D pixel;

        Iterator it = voxels.iterator();

        while (it.hasNext()) {
            pixel = new Voxel3D((Voxel3D) it.next());
            pixel.setValue(ima.getPixel(pixel));
            vector.add(pixel);
        }

        return vector;
    }

    /*public double pcColoc(Object3D obj) {
     //test volume...?
     return colocPixels((Object3DFuzzy)obj)/volume*100;
     }
     * 
     */
    @Override
    public void drawContours(ObjectCreator3D ima, int col) {
        int s = contours.size();
        Voxel3D p2;
        for (int j = 0; j < s; j++) {
            p2 = (Voxel3D) contours.get(j);
            ima.createPixel((int) p2.getX(), (int) p2.getY(), (int) p2.getZ(), col);
        }
    }

    @Override
    public void draw(ImageStack mask, int col) {
        Voxel3D vox;
        Iterator it = voxels.iterator();
        while (it.hasNext()) {
            vox = (Voxel3D) it.next();
            mask.setVoxel((int) vox.getX(), (int) vox.getY(), (int) vox.getZ(), col);
        }
    }

    @Override
    public void draw(ImageStack mask, int r, int g, int b) {
        Voxel3D vox;
        ImageProcessor tmp;
        Color col = new Color(r, g, b);
        Iterator it = voxels.iterator();
        while (it.hasNext()) {
            vox = (Voxel3D) it.next();
            tmp = mask.getProcessor((int) (vox.getZ() + 1));
            tmp.setColor(col);
            tmp.drawPixel((int) vox.getX(), (int) vox.getY());
        }
    }

    @Override
    public void draw(ByteProcessor mask, int z, int col) {
        Voxel3D vox;
        Iterator it = voxels.iterator();
        while (it.hasNext()) {
            vox = (Voxel3D) it.next();
            if (Math.abs(z - vox.getZ()) < 0.5) {
                mask.putPixel((int) vox.getX(), (int) vox.getY(), col);
            }
        }
    }

    @Override
    public void draw(ObjectCreator3D obj, int col) {
        Voxel3D vox;
        Iterator it = voxels.iterator();
        while (it.hasNext()) {
            vox = (Voxel3D) it.next();
            obj.createPixel((int) vox.getX(), (int) vox.getY(), (int) vox.getZ(), col);
        }
    }

    @Override
    public Roi createRoi(int z) {
        // IJ.write("create roi " + z);
        int sx = this.getXmax() - this.getXmin() + 1;
        int sy = this.getYmax() - this.getYmin() + 1;
        ByteProcessor mask = new ByteProcessor(sx, sy);
        // object black on white
        //mask.invert();
        draw(mask, z, 255);
        ImagePlus maskPlus = new ImagePlus("mask " + z, mask);
        //maskPlus.show();
        //IJ.run("Create Selection");
        ThresholdToSelection tts = new ThresholdToSelection();
        tts.setup("", maskPlus);
        tts.run(mask);
        maskPlus.updateAndDraw();
        // IJ.write("sel=" + maskPlus.getRoi());
        //maskPlus.hide();
        Roi roi = maskPlus.getRoi();
        Rectangle rect = roi.getBounds();
        rect.x += this.getXmin();
        rect.y += this.getYmin();

        return roi;
    }

    @Override
    public ArrayList<Voxel3D> getVoxels() {
        return voxels;
    }

    @Override
    public void writeVoxels(String path) {
        Voxel3D pixel;
        java.io.BufferedWriter bf;
        int c = 0;

        try {
            bf = new java.io.BufferedWriter(new java.io.FileWriter(path + value + ".3droi")); //name??
            Iterator it = voxels.iterator();
            while (it.hasNext()) {
                c++;
                pixel = new Voxel3D((Voxel3D) it.next());
                bf.write(c + "\t" + pixel.getX() + "\t" + pixel.getY() + "\t" + pixel.getZ() + "\n");
            }
            bf.close();
        } catch (java.io.IOException ex) {
            java.util.logging.Logger.getLogger(Object3DVoxels.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
    }
}
