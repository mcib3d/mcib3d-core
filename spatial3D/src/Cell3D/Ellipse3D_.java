package Cell3D;



import mcib3d.geom.MereoObject3D;
import mcib3d.geom.Object3DVoxels;
import mcib3d.geom.ObjectCreator3D;
import ij.IJ;
import ij.ImagePlus;
import ij.plugin.PlugIn;

public class Ellipse3D_ implements PlugIn {
	@Override
    public void run(String arg) {
        // parameter of image
        int tx = 100;
        int ty = 120;
        int tz = 100;
        
        // center for the sphere and ellipse
        int cx = tx / 2;
        int cy = ty / 2 ;
        int cz = tz / 2;
        
        // rayon of the ellipse
        double rx = tx / 4;
        double ry = ty / 3;
        double rz = tz / 4;
        // couleur de la sphere
        int val = 255;

        // classe pour creer des objets 3D dans une image 3D
        ObjectCreator3D ellipseDraw;
        ellipseDraw = new ObjectCreator3D(tx, ty, tz);
        // creation de la sphere
        ellipseDraw.createEllipsoid(cx, cy, cz, rx, ry, rz, val, false);
        IJ.log("  ++++++++ create ellipse ++++++  ");
        
        Object3DVoxels ellipse = ellipseDraw.getObject3DVoxels(val);
        IJ.log("Obj " + ellipse + " " +" ccx: " + cx + " ccy: "+ cy + " ccz: " + cz + " radius: " + rx + "  " + ry + "  " + rz );
        // image pour afficher la sphere dans ImageJ
        ImagePlus plusShape = new ImagePlus("Ellipse3D_1", ellipseDraw.getStack());
        plusShape.setSlice((int) (cz));
        plusShape.setDisplayRange(0, val);
        plusShape.show();
        
        double distDCMax = ellipse.getDistCenterMax();
        ObjectCreator3D bigSphereDraw;
        bigSphereDraw = new ObjectCreator3D(tx, ty, tz);
        // creation de la sphere
        bigSphereDraw.createEllipsoid(cx, cy, cz, distDCMax, distDCMax, distDCMax, val, false);
        Object3DVoxels bigSphere = bigSphereDraw.getObject3DVoxels(val);
        IJ.log("  ++++++++ create big sphere outside of ellipse +++++++++++");
        IJ.log("Obj " + bigSphere + " " +" ccx: " + cx + " ccy: "+ cy + " ccz: " + cz + " radius: " + distDCMax + "  " + distDCMax + "  " + distDCMax );
        ImagePlus plusShape2 = new ImagePlus("Sphere3D_1", bigSphereDraw.getStack());
        plusShape2.setSlice((int) (cz));
        plusShape2.setDisplayRange(0, val);
        plusShape2.show();
   
        
        
     // center for the sphere and ellipse
        int cx2 = tx / 3;
        int cy2 = ty / 3 ;
        int cz2 = tz / 3;
        
        // rayon of the ellipse
        double rx2 = tx / 3;
        double ry2 = ty / 5;
        double rz2 = tz / 4;
        // couleur de la sphere
        //int val = 255;

        // classe pour creer des objets 3D dans une image 3D
        ObjectCreator3D ellipseDraw2;
        ellipseDraw2 = new ObjectCreator3D(tx, ty, tz);
        // creation de la sphere
        ellipseDraw2.createEllipsoid(cx2, cy2, cz2, rx2, ry2, rz2, val, false);
        IJ.log("  ++++++++ create ellipse 2 ++++++  ");
        
        Object3DVoxels ellipse2 = ellipseDraw2.getObject3DVoxels(val);
        IJ.log("Obj " + ellipse2 + " " +" ccx: " + cx2 + " ccy: "+ cy2 + " ccz: " + cz2 + " radius: " + rx2 + "  " + ry2 + "  " + rz2 );
        // image pour afficher la sphere dans ImageJ
        ImagePlus plusShape3 = new ImagePlus("Ellipse3D_2", ellipseDraw2.getStack());
        plusShape3.setSlice((int) (cz2));
        plusShape3.setDisplayRange(0, val);
        plusShape3.show();
        
        double distDCMax2 = ellipse2.getDistCenterMax();
        ObjectCreator3D bigSphereDraw2;
        bigSphereDraw2 = new ObjectCreator3D(tx, ty, tz);
        // creation de la sphere
        bigSphereDraw2.createEllipsoid(cx2, cy2, cz2, distDCMax2, distDCMax2, distDCMax2, val, false);
        Object3DVoxels bigSphere2 = bigSphereDraw2.getObject3DVoxels(val);
        IJ.log("  ++++++++ create big sphere outside of ellipse +++++++++++");
        IJ.log("Obj " + bigSphere2 + " " +" ccx: " + cx2 + " ccy: "+ cy2 + " ccz: " + cz2 + " radius: " + distDCMax2 + "  " + distDCMax2 + "  " + distDCMax2 );
        ImagePlus plusShape4 = new ImagePlus("Sphere3D_2", bigSphereDraw2.getStack());
        plusShape4.setSlice((int) (cz2));
        plusShape4.setDisplayRange(0, val);
        plusShape4.show();
        //RandomComponent r = new RandomComponent();
        MereoObject3D mereo;
        mereo = new MereoObject3D(ellipse, ellipse2);
        IJ.log("Ellipse 1: " + ellipse + " && ellipse 2: " + ellipse2 + " relationship is: " + mereo.getRCC8Relationship());
        
	}    
}
