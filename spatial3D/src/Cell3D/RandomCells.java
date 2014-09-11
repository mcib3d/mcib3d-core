package Cell3D;

import ij.IJ;
import ij.ImagePlus;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Random;

import mcib3d.geom.MereoObject3D;
import mcib3d.geom.Object3D;
import mcib3d.geom.Object3DVoxels;
import mcib3d.geom.ObjectCreator3D;
import mcib3d.geom.Point3D;
import mcib3d.geom.Voxel3D;
import mcib3d.image3d.ImageByte;
import mcib3d.image3d.ImageHandler;
import mcib3d.image3d.ImageInt;

public class RandomCells {
	int minR = 1;
	double epsilonEC = 1, epsilonTPPi = 1 ;
	public static Random RANDOM = new Random(System.nanoTime());
	//public int xmin=0, xmax=0, ymin=0, ymax=0, zmin=0, zmax=0;  
	public double radiusFirstCell = 0, radiusSecondCell = 0;
	//public ArrayList<Voxel3D> contours = new ArrayList<Voxel3D>();
	
	/**
     * Area in voxels
     */
    public double area = -1;
    /**
     * Area in units
     */
    public double areaUnit = -1;
    public double resXY = 1;
    /**
     * the resolution in Z
     */
    public double resZ = 1;
	
	public static final double random(final double pMin, final double pMax) {
	    return pMin + RANDOM.nextDouble() * (pMax - pMin);
	}
	public double distCenterUnit2Objects(Object3D obj1, double obj2_bx, double obj2_by, double obj2_bz) 
	{
        double dist = Math.sqrt((obj1.getCenterX() - obj2_bx) * (obj1.getCenterX() - obj2_bx) * obj1.getResXY() * obj1.getResXY()
        		+ (obj1.getCenterY() - obj2_by) * (obj1.getCenterY() - obj2_by) * obj1.getResXY() * obj1.getResXY() 
        		+ (obj1.getCenterZ() - obj2_bz) * (obj1.getCenterZ() - obj2_bz) * obj1.getResZ() * obj1.getResZ());
        return dist;
    }
	public double distCenterUnit2Objects(Object3D obj1, Object3D obj2) 
	{
        double dist = Math.sqrt((obj1.getCenterX() - obj2.getCenterX()) * (obj1.getCenterX() - obj2.getCenterX()) * obj1.getResXY() * obj1.getResXY()
        		+ (obj1.getCenterY() - obj2.getCenterY()) * (obj1.getCenterY() - obj2.getCenterY()) * obj1.getResXY() * obj1.getResXY() 
        		+ (obj1.getCenterZ() - obj2.getCenterZ()) * (obj1.getCenterZ() - obj2.getCenterZ()) * obj1.getResZ() * obj1.getResZ());
        return dist;
    }
	//(v) One sphere lies completely outside the other if d > r1 +r2 
	//relation DC
	public boolean disconnection(Object3D obj1, double obj2_bx, double obj2_by, double obj2_bz, double obj2_radius)
	{
		double distance2Spheres = distCenterUnit2Objects(obj1, obj2_bx, obj2_by, obj2_bz);
		return (distance2Spheres > (obj1.getDistCenterMean() + obj2_radius));
	}
	public boolean disconnection(Object3D obj1, Object3D obj2)
	{
		double distance2Spheres = distCenterUnit2Objects(obj1, obj2);
		return (distance2Spheres >= (obj1.getDistCenterMean() + obj2.getDistCenterMean() + epsilonEC));
	}
	//Math.abs(distance - totalR) < epsilonEC
	public boolean external_connection(Object3D obj1, double obj2_bx, double obj2_by, double obj2_bz, double obj2_radius)
	{
		double distance2Spheres = distCenterUnit2Objects(obj1, obj2_bx, obj2_by, obj2_bz);
		return (Math.abs(distance2Spheres - obj1.getDistCenterMean() - obj2_radius) < epsilonEC);
	}
	public boolean external_connection(Object3D obj1, Object3D obj2)
	{
		double distance2Spheres = distCenterUnit2Objects(obj1, obj2);
		return (Math.abs(distance2Spheres - obj1.getDistCenterMean() - obj2.getDistCenterMean()) < epsilonEC);
	}
	
	public boolean tangential_proper_parthood_inverse(Object3D obj1, double obj2_bx, double obj2_by, double obj2_bz, double obj2_radius)
	{
		double distance2Spheres = distCenterUnit2Objects(obj1, obj2_bx, obj2_by, obj2_bz);
		return ((obj1.getDistCenterMean() - distance2Spheres - obj2_radius < epsilonTPPi)
				&& (obj1.getDistCenterMean() >= distance2Spheres + obj2_radius));
	}
	
	public boolean no_tangential_proper_parthood_inverse(Object3D obj1, double obj2_bx, double obj2_by, double obj2_bz, double obj2_radius)
	{
		double distance2Spheres = distCenterUnit2Objects(obj1, obj2_bx, obj2_by, obj2_bz);
		return (obj1.getDistCenterMean() >= distance2Spheres + obj2_radius);
				
	}
	
	public Object3DVoxels findSphereTPPi(Object3DVoxels bigSphere, int tx, int ty, int tz)
	{
		// center for the sphere
        int cx = tx / 2;
        int cy = ty / 2;
        int cz = tz / 2;
        // radius of the sphere
        double rad = tx / 2;
        // couleur de la sphere
        int val = 255;
        Object3DVoxels newCell = new Object3DVoxels();
        
        int nThrows = 0;
         
        while (nThrows < 100) 
        {
        	nThrows++;
        	//Random center  
        	double ccx = random(cx - Math.random() * (rad - minR), cx + Math.random() * (rad - minR));
			double ccy = random(cy - Math.random() * (rad - minR), cy + Math.random() * (rad - minR));
			double ccz = random(cz - Math.random() * (rad - minR), cz + Math.random() * (rad - minR));
			double r_cell = random(5, rad/2);
            
            if (tangential_proper_parthood_inverse(bigSphere, ccx, ccy, ccz, r_cell))
            {
            	//IJ.log("Test TPPi with sphere:  " +" ccx: "+ccx + " ccy: "+ccy + " ccz: "+ccz + " radius: " + r_cell);
            	ObjectCreator3D cellDraw;
        		cellDraw = new ObjectCreator3D(tx, ty, tz);
    			cellDraw.createEllipsoid((int) ccx, (int) ccy, (int) ccz, r_cell, r_cell, r_cell, 200, false);
                newCell = cellDraw.getObject3DVoxels(200);
                radiusFirstCell = r_cell;
                //IJ.log("radius of first cell: "+r_cell);
                return newCell;
            }
        }
        return null;
        
	}   
	
	public ArrayList<Object3DVoxels> intersectSpheres(Object3DVoxels bigSphere, int tx, int ty, int tz)
	{
		if(findSphereTPPi(bigSphere, tx, ty, tz)!=null)
		{
			ArrayList<Object3DVoxels> listSpheres = new ArrayList<Object3DVoxels>();
			Object3DVoxels firstCell = findSphereTPPi(bigSphere, tx, ty, tz);
			//IJ.log("first cell: center:" + firstCell.getCenterX()+" "+firstCell.getCenterY()+" "+firstCell.getCenterZ()+" radius: "+firstCell.getDistCenterMean());
			listSpheres.add(firstCell);
			radiusSecondCell = random(10, bigSphere.getDistCenterMean()/2);
			double distanceTotal = radiusFirstCell + radiusSecondCell;
			
			ObjectCreator3D sphere2eDraw;
			sphere2eDraw = new ObjectCreator3D(tx, ty, tz);
	        // creation de la sphere who satisfy the condition EC
			sphere2eDraw.createEllipsoid((int) Math.round(firstCell.getCenterX()), (int) Math.round(firstCell.getCenterY()),
					                    (int) Math.round(firstCell.getCenterZ()), distanceTotal, distanceTotal, distanceTotal, 200, false);
	        Object3DVoxels secondSphere = sphere2eDraw.getObject3DVoxels(200);
	       // IJ.log(" Second sphere: " + secondSphere);
	        listSpheres.add(secondSphere);
	        
	        //create a sphere who satisfy the condition TPPi
	        ObjectCreator3D sphere3eDraw;
			sphere3eDraw = new ObjectCreator3D(tx, ty, tz);
			double distance2Spheres = bigSphere.getDistCenterMean() - radiusSecondCell;
	        // creation de la sphere who satisfy the condition EC
			sphere3eDraw.createEllipsoid((int) Math.round(bigSphere.getCenterX()), (int) Math.round(bigSphere.getCenterY()),
					(int) Math.round(bigSphere.getCenterZ()), distance2Spheres, distance2Spheres, distance2Spheres, 200, false);
			
	        Object3DVoxels thirdSphere = sphere3eDraw.getObject3DVoxels(200);
	       // IJ.log(" third sphere: "+thirdSphere);
	        listSpheres.add(thirdSphere);
	        return listSpheres;
		}	
		else{
			return null;
		}
		
	}
	
	public ArrayList<Voxel3D> createListVoxels(ImageHandler img0)
	{
		ArrayList<Voxel3D> voxelList = new ArrayList<Voxel3D>();
    	for(int z=0; z<img0.sizeZ; z++)
    	{
    		for(int x=0; x<img0.sizeX; x++)
    		{
    			for(int y=0; y<img0.sizeY; y++)
        		{
        			if(img0.getPixel(x, y, z)==200)
        			{
        				Voxel3D v = new Voxel3D(x, y, z, 200);
        				voxelList.add(v);
        			}
        		}	
    		}	
    	}
    	return voxelList;
	}
	/**
     * Compute the bounding box of the object
     */
    public ArrayList<Integer> computeBounding(ArrayList<Voxel3D> voxels) {
    	ArrayList<Integer> sizeSphere = new ArrayList<Integer>();
    	int xmin = Integer.MAX_VALUE;
        int xmax = 0;
        int ymin = Integer.MAX_VALUE;
        int ymax = 0;
        int zmin = Integer.MAX_VALUE;
        int zmax = 0;

        Voxel3D vox;
        Iterator<Voxel3D> it = voxels.iterator();
        while (it.hasNext()) {
            vox = (Voxel3D) it.next();
            if (vox.getX() < xmin) {
                xmin = (int) Math.floor(vox.getX());
            }
            if (vox.getX() > xmax) {
                xmax = (int) Math.ceil(vox.getX());
            }
            if (vox.getY() < ymin) {
                ymin = (int) Math.floor(vox.getY());
            }
            if (vox.getY() > ymax) {
                ymax = (int) Math.ceil(vox.getY());
            }
            if (vox.getZ() < zmin) {
                zmin = (int) Math.floor(vox.getZ());
            }
            if (vox.getZ() > zmax) {
                zmax = (int) Math.ceil(vox.getZ());
            }
        }
        
        sizeSphere.add(xmin);
        sizeSphere.add(xmax);
        sizeSphere.add(ymin);
        sizeSphere.add(ymax);
        sizeSphere.add(zmin);
        sizeSphere.add(zmax);
        
        return sizeSphere;
    }
    
    /**
     * Compute the contours of the object rad=0.5
     */
    private ArrayList<Voxel3D> computeContours(ImageInt segImage, int x0, int y0, int z0, int value) 
    {	
        boolean cont;
        ArrayList<Voxel3D> contours = new ArrayList<Voxel3D>();
        int pix0, pix1, pix2, pix3, pix4, pix5, pix6;
        int sx = segImage.sizeX;
        int sy = segImage.sizeY;
        int sz = segImage.sizeZ;
        area = 0;
        areaUnit = 0;
        double XZ = resXY * resZ;
        double XX = resXY * resXY;
        
        ArrayList<Voxel3D> voxelList = createListVoxels(segImage);
    	//IJ.log("img intersection: "+img0);
        ArrayList<Integer> sizeSphere = computeBounding(voxelList);
        int xmin = sizeSphere.get(0);
        int xmax = sizeSphere.get(1);
        int ymin = sizeSphere.get(2);
        int ymax = sizeSphere.get(3);
        int zmin = sizeSphere.get(4);
        int zmax = sizeSphere.get(5);
        for (int k = zmin - z0; k <= zmax - z0; k++) {
            for (int j = ymin - y0; j <= ymax - y0; j++) {
                for (int i = xmin - x0; i <= xmax - x0; i++) {
                    cont = false;
                    if (segImage.contains(i, j, k)) {
                        pix0 = segImage.getPixelInt(i, j, k);
                        if (pix0 == value) {
                            if (i + 1 < sx) {
                                pix1 = segImage.getPixelInt(i + 1, j, k);
                            } else {
                                pix1 = 0;
                            }
                            if (i - 1 >= 0) {
                                pix2 = segImage.getPixelInt(i - 1, j, k);
                            } else {
                                pix2 = 0;
                            }
                            if (j + 1 < sy) {
                                pix3 = segImage.getPixelInt(i, j + 1, k);
                            } else {
                                pix3 = 0;
                            }
                            if (j - 1 >= 0) {
                                pix4 = segImage.getPixelInt(i, j - 1, k);
                            } else {
                                pix4 = 0;
                            }
                            if (k + 1 < sz) {
                                pix5 = segImage.getPixelInt(i, j, k + 1);
                            } else {
                                pix5 = 0;
                            }
                            if (k - 1 >= 0) {
                                pix6 = segImage.getPixelInt(i, j, k - 1);
                            } else {
                                pix6 = 0;
                            }
                            if (pix1 != value) {
                                cont = true;
                                areaUnit += XZ;
                            }
                            if (pix2 != value) {
                                cont = true;
                                areaUnit += XZ;
                            }
                            if (pix3 != value) {
                                cont = true;
                                areaUnit += XZ;
                            }
                            if (pix4 != value) {
                                cont = true;
                                areaUnit += XZ;
                            }
                            if (pix5 != value) {
                                cont = true;
                                areaUnit += XX;
                            }
                            if (pix6 != value) {
                                cont = true;
                                areaUnit += XX;
                            }
                            if (cont) {
                            	area++;
                                Voxel3D voxC = new Voxel3D(i + x0, j + y0, k + z0, value);
                                contours.add(voxC);
                            }
                        }
                    }
                }
            }
        }
        return contours;
    }

	public void createImagesIntersection(Object3DVoxels bigSphere, int tx, int ty, int tz, int value)
	{
		if(intersectSpheres(bigSphere, tx, ty, tz) != null)
		{
			ArrayList<Object3DVoxels> listSpheres = intersectSpheres(bigSphere, tx, ty, tz);
			//int i=0;
			Object3DVoxels cell1 = listSpheres.get(0);
			Object3DVoxels s0 = listSpheres.get(1);
			Object3DVoxels s1 = listSpheres.get(2);
			IJ.log("************************************************************");
			IJ.log("Test: First sphere: center: "+ s0.getCenterX()+" "+s0.getCenterY()+" "+s0.getCenterZ() +" radius: "+s0.getDistCenterMean());
			IJ.log("Test: Cell 1: center: "+ cell1.getCenterX()+" "+cell1.getCenterY()+" "+cell1.getCenterZ() +" radius: "+cell1.getDistCenterMean());
			IJ.log("************************************************************");
			
			
			ImageHandler img0 = new ImageByte("Sphere_1", tx, ty, tz);
        	s0.draw(img0);
        	img0.show("coucou_0");
        	ImageHandler img0_v2 = new ImageByte("Sphere_1", tx, ty, tz);
        	s0.draw(img0_v2);
        	ImageInt img00 = (ImageInt)img0_v2;
        	ArrayList<Voxel3D> contours_img00 = computeContours(img00, img00.offsetX, img00.offsetY, img00.offsetZ, value);
        	ImageHandler img00_contours = new ImageByte("Contours_1", tx, ty, tz);
        	ImageHandler img00_contours_v2 = new ImageByte("Contours_1", tx, ty, tz);
        	for(Voxel3D v00 : contours_img00){
        		img00_contours.setPixel(v00, value);
        		img00_contours_v2.setPixel(v00, value);
        	}
        	img00_contours.show("result of img00_contours");
        	
        	ImageHandler img1 = new ImageByte("Sphere_2", tx, ty, tz);
        	s1.draw(img1);
        	img1.show("coucou_1");
        	ImageInt img01 = (ImageInt)img1;
        	ArrayList<Voxel3D> contours_img01 = computeContours(img01, img01.offsetX, img01.offsetY, img01.offsetZ, value);
        	ImageHandler img01_contours = new ImageByte("Contours_1", tx, ty, tz);
        	for(Voxel3D v01 : contours_img01){
        		img01_contours.setPixel(v01, value);
        	}
        	img01_contours.show("result of img01_contours");
        	
        	img00_contours_v2.intersectMask((ImageInt)img01_contours);
        	img00_contours_v2.show("the last result ^^");
        	ImageInt img_result = (ImageInt)img00_contours_v2;
        	ArrayList<Voxel3D> contours_img_result = computeContours(img_result, img_result.offsetX, img_result.offsetY, img_result.offsetZ, value);
        	
        	int ranIndex = RANDOM.nextInt(contours_img_result.size());
        	IJ.log("center of second cell: " + contours_img_result.get(ranIndex));
        	Voxel3D centerSecondCell = contours_img_result.get(ranIndex);
        	contours_img_result.clear();
        	ObjectCreator3D secondCellDraw;
        	secondCellDraw = new ObjectCreator3D(tx, ty, tz);
        	secondCellDraw.createEllipsoid(centerSecondCell.getRoundX(), centerSecondCell.getRoundY(), centerSecondCell.getRoundZ(),
        									radiusSecondCell, radiusSecondCell, radiusSecondCell, value, false);
        	Object3DVoxels secondCell = secondCellDraw.getObject3DVoxels(value);
        	
        	ArrayList<Object3DVoxels> listCells = new ArrayList<Object3DVoxels>();
        	listCells.add(listSpheres.get(0));
        	listCells.add(secondCell);
        	int i=0;
        	for(Object3DVoxels v : listCells)
            {
            	i++;
            	ImageHandler img=new ImageByte("Cell_"+i,tx,ty,tz);
            	v.draw(img);
            	img.show("cell_"+i);
            }
        	listCells.clear();
        	
        
		}
		else{
			IJ.log("Not satisfy, try again");
			
		}
	}
}
