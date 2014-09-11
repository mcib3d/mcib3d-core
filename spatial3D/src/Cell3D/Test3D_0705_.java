



import ij.IJ;
import ij.ImagePlus;
import ij.plugin.PlugIn;
import mcib3d.geom.MereoObject3D;
import mcib3d.geom.Object3DVoxels;
import mcib3d.geom.ObjectCreator3D;

/**
 * Description of the Class
 *
 * @author thomas
 * @created 19 avril 2005
 */
public class Test3D_0705_ implements PlugIn {

    /**
     * Main processing method for the Shape3D_ object
     *
     * @param arg Description of the Parameter
     */
    @Override
    public void run(String arg) {
        // parameter of image
        int tx = 64;
        int ty = 64;
        int tz = 64;

        // center for the sphere
        int cx = tx / 2;
        int cy = ty / 2;
        int cz = tz / 2;
        // radius of the sphere
        double rad = tx / 2;
        // couleur de la sphere
        int val = 255;

        // classe pour creer des objets 3D dans une image 3D
        ObjectCreator3D bigSphereDraw;
        bigSphereDraw = new ObjectCreator3D(tx, ty, tz);
        // creation de la sphere
        bigSphereDraw.createEllipsoid(cx, cy, cz, rad, rad, rad, val, false);
        Object3DVoxels bigSphere = bigSphereDraw.getObject3DVoxels(val);
        ObjectCreator3D vesDraw;
        vesDraw = new ObjectCreator3D(tx, ty, tz);
        Object3DVoxels ves;
        MereoObject3D mereo;
        // test small sphere
        boolean ok = false;
        while (!ok) {
            double ra = Math.random() * 4 + 4;
            double ccx = Math.random() * (tx - 2 * ra) + ra;
            double ccy = Math.random() * (ty - 2 * ra) + ra;
            double ccz = Math.random() * (tz - 2 * ra) + ra;
            vesDraw.createEllipsoid((int) ccx, (int) ccy, (int) ccz, ra, ra, ra, 200, false);
            ves = vesDraw.getObject3DVoxels(200);
            mereo = new MereoObject3D(bigSphere, ves);
            IJ.log("Obj " + ves + " " + mereo.getRCC8Relationship());
            if (mereo.TangentialProperParthoodInverse()) {
                ok = true;
            } else {
                vesDraw.clear();
            }
        }

        // image pour afficher la sphere dans ImageJ
        ImagePlus plusShape = new ImagePlus("Sphere3D", bigSphereDraw.getStack());
        plusShape.setSlice((int) (cz));
        plusShape.setDisplayRange(0, val);
        plusShape.show();
        ImagePlus plusShape2 = new ImagePlus("Vesicle3D", vesDraw.getStack());
        plusShape2.setSlice((int) (cz));
        plusShape2.setDisplayRange(0, 200);
        plusShape2.show();
    }
}
