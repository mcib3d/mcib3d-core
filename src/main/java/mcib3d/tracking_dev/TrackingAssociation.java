/*     */
package mcib3d.tracking_dev;
/*     */
/*     */

import ij.IJ;
import mcib3d.geom.Object3D;
import mcib3d.geom.Objects3DPopulation;
import mcib3d.geom.Objects3DPopulationColocalisation;
import mcib3d.image3d.ImageHandler;

import java.util.LinkedList;
import java.util.List;
import java.util.TreeSet;

/*     */
/*     */
/*     */
/*     */
/*     */
/*     */
/*     */
/*     */
/*     */
/*     */
/*     */
/*     */ public class TrackingAssociation
        /*     */ {
    /*     */
    /*     */ List<AssociationPair> finalAssociations;
    /*     */ List<Object3D> finalOrphan1;
    /*     */ List<Object3D> finalOrphan2;
    /*     */ List<Mitosis> finalMitosis;
    /*     */   private ImageHandler img1;
    /*     */   private ImageHandler img2;
    /*  19 */   private ImageHandler path = null;
    /*  20 */   private ImageHandler tracked = null;
    /*  21 */   private ImageHandler pathed = null;
    /*     */
    /*     */   private boolean merge = false;

    /*     */
    /*     */
    public TrackingAssociation(ImageHandler img1, ImageHandler img2) {
        /*  31 */
        this.img1 = img1;
        /*  32 */
        this.img2 = img2;
        /*     */
    }

    /*     */
    /*     */
    public void setPathImage(ImageHandler path) {
        /*  36 */
        this.path = path;
        /*     */
    }

    /*     */
    /*     */
    public ImageHandler getTracked() {
        /*  40 */
        if (this.tracked == null) computeTracking();
        /*     */
        /*  42 */
        return this.tracked;
        /*     */
    }

    /*     */
    /*     */
    public ImageHandler getPathed() {
        /*  46 */
        if (this.path == null) return null;
        /*  47 */
        if (this.pathed == null) computeTracking();
        /*     */
        /*  49 */
        return this.pathed;
        /*     */
    }

    /*     */
    /*     */
    /*     */
    public void setImage1(ImageHandler img1) {
        /*  54 */
        this.img1 = img1;
        /*  55 */
        this.tracked = null;
        /*     */
    }

    /*     */
    /*     */
    public void setImage2(ImageHandler img2) {
        /*  59 */
        this.img2 = img2;
        /*  60 */
        this.tracked = null;
        /*     */
    }

    /*     */
    /*     */
    /*     */
    public void setMerge(boolean merge) {
        /*  65 */
        this.merge = merge;
        /*  66 */
        this.tracked = null;
        /*     */
    }

    /*     */
    /*     */
    /*     */
    private void computeTracking() {
        /*  71 */
        this.tracked = this.img1.createSameDimensions();
        /*  72 */
        if (this.path != null) this.pathed = this.img1.createSameDimensions();
        /*     */
        /*  74 */
        Objects3DPopulation population1 = new Objects3DPopulation(this.img1);
        /*  75 */
        Objects3DPopulation population2 = new Objects3DPopulation(this.img2);
        /*     */
        /*  77 */
        Association association = new Association(population1, population2, new CostColocalisation(new Objects3DPopulationColocalisation(population1, population2)));
        /*  78 */
        association.verbose = true;
        /*     */
        /*  80 */
        association.computeAssociation();
        /*     */
        /*     */
        /*  83 */
        MitosisDetector mitosisDetector = new MitosisDetector(this.img1, this.img2, association);
        /*     */
        /*     */
        /*  86 */
        if (this.merge) {
            /*  87 */
            this.img2 = mitosisDetector.detectAndMergeSplit();
            /*     */
            /*  89 */
            population2 = new Objects3DPopulation(this.img2);
            /*  90 */
            association = new Association(population1, population2, new CostColocalisation(new Objects3DPopulationColocalisation(population1, population2)));
            /*  91 */
            association.computeAssociation();
            /*  92 */
            mitosisDetector = new MitosisDetector(this.img1, this.img2, association);
            /*     */
        }
        /*     */
        /*  95 */
        association.drawAssociation(this.tracked);
        /*     */
        /*  97 */
        if (this.path != null) {
            /*  98 */
            association.drawAssociationPath(this.pathed, this.path, this.tracked);
            /*     */
        }
        /*     */
        /*     */
        /*     */
        /*     */
        /*     */
        /* 105 */
        TreeSet<Mitosis> treeSet = mitosisDetector.detectMitosis();
        /* 106 */
        List<Object3D> mito = new LinkedList<>();
        /* 107 */
        for (Mitosis mitosis : treeSet) {
            /* 108 */
            Object3D d1 = mitosis.getDaughter1();
            /* 109 */
            Object3D d2 = mitosis.getDaughter2();
            /* 110 */
            Object3D mo = mitosis.getMother();
            /* 111 */
            double coloc = mitosis.getColocMitosis();
            /* 112 */
            int valPath = (int) mo.getPixMeanValue(this.path);
            /* 113 */
            if (!mito.contains(d1) && !mito.contains(d2) &&
                    /* 114 */         coloc > mitosisDetector.getMinColocMitosis()) {
                /* 115 */
                IJ.log("MITOSIS : " + d1.getValue() + " " + d2.getValue() + " " + mo.getValue() + " " + coloc + " " + valPath);
                /* 116 */
                mito.add(d1);
                /* 117 */
                mito.add(d2);
                /*     */
                /* 119 */
                if (this.path != null) {
                    /* 120 */
                    d1.draw(this.pathed, valPath);
                    /* 121 */
                    d2.draw(this.pathed, valPath);
                    /*     */
                }
                /*     */
            }
            /*     */
        }
        /*     */
    }
    /*     */
}


/* Location:              /home/thomas/Prog/MCIB/mcib3d-dev/mcib3d_dev/!/TRACKING/TrackingAssociation.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */