package StatisticsTest;

/**
 * Class to random position of vesicles in cell 
 * who satisfy the condition TPPi and DC (at local scale)
 * and one of three conditions of spatial statistics (at global scale)
 * Completely random pattern
 * Aggregated pattern (attraction)
 * Regular pattern
 * @author ttnhoa
 *
 */
import ij.IJ;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Random;
import mcib3d.geom.Object3D;
import mcib3d.geom.Object3DVoxels;
import mcib3d.geom.ObjectCreator3D;
import mcib3d.geom.Voxel3D;
import mcib3d.image3d.ImageByte;
import mcib3d.image3d.ImageHandler;
import mcib3d.image3d.ImageInt;

public class VesicleInCell {
	/**
	 * Declare the variable global 
	 */
	int minR = 1;
	double epsilonEC = 1, epsilonTPPi = 1 ;
	public static Random RANDOM = new Random(System.nanoTime());
	public ArrayList<Double> listRadiusCells;
	//public double radiusFirstCell = 0, radiusSecondCell = 0;
	public ArrayList<Voxel3D> listCenterPos = new ArrayList<Voxel3D>();
	public int tx, ty, tz, cx, cy, cz, val;
	public double radBigSphere;
	public Object3DVoxels bigSphere = new Object3DVoxels();
	public ArrayList<Object3DVoxels> listVesiclesResult = new ArrayList<Object3DVoxels>();
	public int nbVesicles = 20;
	public int nbPropage = 3000;
	public int value_grey = 200;
	public VesicleInCell(int tx, int ty, int tz)
	{
		this.tx = tx; 
		this.ty = ty;
		this.tz = tz;
		//center for the sphere
                this.cx = tx / 2;
                this.cy = ty / 2;
                this.cz = tz / 2;
                // radius of the sphere
                this.radBigSphere = tx / 2;
                this.val = 200;

                //create a big sphere
                ObjectCreator3D bigSphereDraw;
                bigSphereDraw = new ObjectCreator3D(tx, ty, tz);
                // creation de la sphere
                bigSphereDraw.createEllipsoid(cx, cy, cz, radBigSphere, radBigSphere, radBigSphere, val, false);
                this.bigSphere = bigSphereDraw.getObject3DVoxels(val);
	}
	
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
	/**
	 * NTPPi : objectY is inside of objectX
	 * @param obj1
	 * @param obj2_bx
	 * @param obj2_by
	 * @param obj2_bz
	 * @param obj2_radius
	 * @return
	 */
	public boolean no_tangential_proper_parthood_inverse(Object3D obj1, double obj2_bx, double obj2_by, double obj2_bz, double obj2_radius)
	{
		double distance2Spheres = distCenterUnit2Objects(obj1, obj2_bx, obj2_by, obj2_bz);
		return (obj1.getDistCenterMean() >= distance2Spheres + obj2_radius);
	}
	
	/**
	 * function to generate a poison distribution 
	 * @param mean : exp lambda
	 * @return a number according to poisson distribution
	 */
	private static int getPoissonRandom(double mean) {
        Random r = new Random();
        double L = Math.exp(-mean);
        int k = 0;
        double p = 1.0;
        do {
            p = p * r.nextDouble();
            k++;
        } while (p > L);
        return k - 1;
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
        			if(img0.getPixel(x, y, z)==value_grey)
        			{
        				Voxel3D v = new Voxel3D(x, y, z, value_grey);
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
    public ArrayList<Integer> computeBounding(ArrayList<Voxel3D> voxels) 
    {
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
    public Object3DVoxels addFirstVesicle(int nbPropage)
    {	
        int nThrows = 0; 
        boolean flag = true;
        while (flag && nThrows < nbPropage) 
        {
        	nThrows++;
        	//Random center  
        	double ccx = random(cx - Math.random() * (radBigSphere - minR), cx + Math.random() * (radBigSphere - minR));
			double ccy = random(cy - Math.random() * (radBigSphere - minR), cy + Math.random() * (radBigSphere - minR));
			double ccz = random(cz - Math.random() * (radBigSphere - minR), cz + Math.random() * (radBigSphere - minR));
			double r_cell = random(5, radBigSphere/2);
            //IJ.log("stupid_"+nThrows);
            if (no_tangential_proper_parthood_inverse(bigSphere, ccx, ccy, ccz, r_cell))
            {
            	//IJ.log("Test TPPi with sphere:  " +" ccx: "+ccx + " ccy: "+ccy + " ccz: "+ccz + " radius: " + r_cell);
            	ObjectCreator3D vesDraw;
        		vesDraw = new ObjectCreator3D(tx, ty, tz);
    			vesDraw.createEllipsoid((int) ccx, (int) ccy, (int) ccz, r_cell, r_cell, r_cell, value_grey, false);
    			Object3DVoxels newVesicle = vesDraw.getObject3DVoxels(value_grey);
    			IJ.log("--------------------------------------------");
        		IJ.log("First Vesicle : " + newVesicle);
        		IJ.log("--------------------------------------------");
                flag = false;
                return newVesicle;   
            }
        }
        return null;
	}  
    
    /**
	 * random radius of a new cell 
	 * traverse the list of cell exist 
	 * found the list of center possible 
	 * random a element in this list 
	 */
	public Object3DVoxels addVesicle()
	{	
		//Random center of new vesicle  
                double ccx = random(cx - Math.random() * (radBigSphere - minR), cx + Math.random() * (radBigSphere - minR));
		double ccy = random(cy - Math.random() * (radBigSphere - minR), cy + Math.random() * (radBigSphere - minR));
		double ccz = random(cz - Math.random() * (radBigSphere - minR), cz + Math.random() * (radBigSphere - minR));
		double r_vesicle = random(5, radBigSphere/2);
        
                if (no_tangential_proper_parthood_inverse(bigSphere, ccx, ccy, ccz, r_vesicle))
                {
                        ObjectCreator3D vesDraw;
                        vesDraw = new ObjectCreator3D(tx, ty, tz);
                                vesDraw.createEllipsoid((int) ccx, (int) ccy, (int) ccz, r_vesicle, r_vesicle, r_vesicle, value_grey, false);
                                Object3DVoxels newVesicle = vesDraw.getObject3DVoxels(value_grey);

                                int count = 0;
                        for(int index=0; index<listVesiclesResult.size(); index++)
                        {
                                Object3DVoxels ci = listVesiclesResult.get(index);
                                if(disconnection(ci, newVesicle) || external_connection(ci, newVesicle))
                                {
                                        count++;
                                }
                        }
                        if(count == listVesiclesResult.size())
                        {	
                                IJ.log("--------------------------------------------");
                        IJ.log("Next Obj " + newVesicle);
                        IJ.log("--------------------------------------------");
                                return newVesicle;
                        }
                        else{
                                return null;
                        }
                }
                else{
                        return null;
                }

		
	}
        /**
	 * random radius of a new cell 
	 * traverse the list of cell exist 
	 * found the list of center possible 
	 * random a element in this list 
	 */
	public ArrayList<Object3DVoxels> createListVesicle()
	{	
		Object3DVoxels firstVesicle = addFirstVesicle(100);
		if(listVesiclesResult.isEmpty())
		{
			if(firstVesicle != null)
			{
				listVesiclesResult.add(firstVesicle);
			}		
		}	
		int nSuccess = 1, nThrow = 0;
		while(nSuccess < nbVesicles && nThrow < nbPropage)
		{
			nThrow++;
			Object3DVoxels nextVesicle = addVesicle();
			if(nextVesicle != null)
			{
				listVesiclesResult.add(nextVesicle);
				nSuccess++;
			}	
			
		}	
		IJ.log("size of list cells: " + listVesiclesResult.size());
		
		
		ImageHandler img_big_sphere = new ImageByte("Big Sphere",tx,ty,tz);
		bigSphere.draw(img_big_sphere);
                img_big_sphere.show("Big Sphere Img");
		int i = 0;
		for(Object3DVoxels v : listVesiclesResult)
                {
                        IJ.log("position of vesicle: " + i + " x: " + v.getCenterX() + " y: " + v.getCenterY() + " z: "+v.getCenterZ());
                        i++;
                        ImageHandler img = new ImageByte("Vesicle_" + i, tx, ty, tz);
                        v.draw(img);
                        img.save("/home/ttnhoa/Pictures/img_cells_2306_ttnhoa/");
                        img.show("vesicle_" + i );
                }
                //listVesiclesResult.clear();
		return listVesiclesResult;
	}
}
