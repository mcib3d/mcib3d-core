package Cell3D;


import java.util.ArrayList;
import java.util.Random;


import ij.IJ;
import ij.ImagePlus;
import ij.plugin.PlugIn;
import mcib3d.geom.MereoObject3D;
import mcib3d.geom.Object3DVoxels;
import mcib3d.geom.ObjectCreator3D;
import mcib3d.image3d.ImageByte;
import mcib3d.image3d.ImageHandler;
import mcib3d.image3d.ImageShort;

/**
 * Description of the Class
 *
 * @author thomas
 * @created 19 avril 2005
 */
public class VesicleInCell_ implements PlugIn {

    /**
     * Main processing method for the Shape3D_ object
     *
     * @param arg Description of the Parameter
     */
	double minRadiusVesicle = 1;
    double maxRadiusVesicle = 10;
    int nbVesicles = 5; 
    double minRadiusCell = 1, maxRadiusCell = 10;
    int nbCells = 30; 
    ArrayList<Object3DVoxels> vesList = new ArrayList<Object3DVoxels>();
    ArrayList<Object3DVoxels> cellList = new ArrayList<Object3DVoxels>();
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
        ArrayList<Object3DVoxels> cellList = new ArrayList<Object3DVoxels>();
        ObjectCreator3D bigSphereDraw;
        bigSphereDraw = new ObjectCreator3D(tx, ty, tz);
        // creation de la sphere
        bigSphereDraw.createEllipsoid(cx, cy, cz, rad, rad, rad, val, false);
        Object3DVoxels bigSphere = bigSphereDraw.getObject3DVoxels(val);
        
        RandomComponent r = new RandomComponent();
        //vesList = r.randomVesicleInNucleusV2(minRadiusVesicle, maxRadiusVesicle, nbVesicles, tx, ty, tz);
        //vesList = r.randomVesicleInNucleus(minRadiusVesicle, maxRadiusVesicle, nbVesicle, tx, ty, tz);
        //IJ.log("test array"+vesList.size());
        
        cellList = r.randomCellsV3(minRadiusCell, maxRadiusCell, nbCells, tx, ty, tz);
        
        //IJ.log("size of cell list: "+cellList.size());
        // image pour afficher la sphere dans ImageJ
        ImagePlus plusShape = new ImagePlus("Cell3D", bigSphereDraw.getStack());
        plusShape.setSlice((int) (cz));
        plusShape.setDisplayRange(0, val);
        plusShape.show();
        
        
        /**
         * ImagePlus plusShape2 = new ImagePlus("Vesicle3D", v.getStack());
            plusShape2.setSlice((int) (tz/2));
            plusShape2.setDisplayRange(0, 200);
            plusShape2.show();
         */
        /*int i=0;
        for(Object3DVoxels v : vesList)
        {
        	i++;
        	ImageHandler img=new ImageByte("Cell_"+i,tx,ty,tz);
        	v.draw(img);
        	img.show("coucou_"+i);
        	
        	
        }
        IJ.log("Finish random vesicle");
        */
        int i=0;
        for(Object3DVoxels v : cellList)
        {
        	i++;
        	ImageHandler img=new ImageByte("Cell_"+i,tx,ty,tz);
        	v.draw(img);
        	img.show("coucou_"+i);
        	//img.save("/home/ttnhoa/Pictures/imgs_cells/");
        	//img.saveThumbNail(64, 64, "/home/ttnhoa/Pictures/imgs_cells/");
        	
        	
        }
    }
}
