package Cell3D;



import java.util.ArrayList;

import mcib3d.geom.Object3DVoxels;
import mcib3d.geom.ObjectCreator3D;
import mcib3d.image3d.ImageByte;
import mcib3d.image3d.ImageHandler;
import ij.IJ;
import ij.ImagePlus;
import ij.plugin.PlugIn;

public class IntersectionSpheres_ implements PlugIn 
{
	public void run(String arg)
	{
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
        
        //create a big sphere
        ObjectCreator3D bigSphereDraw;
        bigSphereDraw = new ObjectCreator3D(tx, ty, tz);
        // creation de la sphere
        bigSphereDraw.createEllipsoid(cx, cy, cz, rad, rad, rad, val, false);
        Object3DVoxels bigSphere = bigSphereDraw.getObject3DVoxels(val);
        ImagePlus plusShape = new ImagePlus("Cell3D", bigSphereDraw.getStack());
        plusShape.setSlice((int) (cz));
        plusShape.setDisplayRange(0, val);
        plusShape.show();
        RandomCells r = new RandomCells();
        int value = 200;
       /* ArrayList<Object3DVoxels> l = r.intersectSpheres(bigSphere, tx, ty, tz);
        int i = 0;
        for(Object3DVoxels v : l)
        {
        	i++;
        	ImageHandler img=new ImageByte("Cell_"+i,tx,ty,tz);
        	v.draw(img);
        	img.show("sphere_"+i);
        }
        */
        
        /*if(r.createImagesIntersection(bigSphere, tx, ty, tz, value)!=null){
        	ArrayList<Object3DVoxels> listCells = r.createImagesIntersection(bigSphere, tx, ty, tz, value);
        	IJ.log("+++++++++++++++++Results++++++++++++++++++++++++");
        	IJ.log("list cells created: "+listCells.size());
        	int i = 0;
        	for(Object3DVoxels v : listCells)
            {
            	i++;
            	ImageHandler img=new ImageByte("Cell_"+i,tx,ty,tz);
            	v.draw(img);
            	img.show("cell_"+i);
            }
        	listCells.clear();
        }*/
        
        
        r.createImagesIntersection(bigSphere, tx, ty, tz, value);
        
	}
}
