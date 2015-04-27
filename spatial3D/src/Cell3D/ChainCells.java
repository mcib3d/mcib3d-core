package Cell3D;

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


public class ChainCells {
	
	/**
	 * Declare the variable global 
	 */
	int minR = 1;
	double epsilonEC = 1, epsilonTPPi = 1 ;
	public static Random RANDOM = new Random(System.nanoTime());
	public ArrayList<Double> listRadiusCells;
	//public double radiusFirstCell = 0, radiusSecondCell = 0;
	public ArrayList<Voxel3D> rangeSearch = new ArrayList<Voxel3D>();
	public int tx, ty, tz, cx, cy, cz, val;
	public double radBigSphere;
	public Object3DVoxels bigSphere = new Object3DVoxels();
	public ArrayList<Object3DVoxels> listCellsResult = new ArrayList<Object3DVoxels>();
	//public int nbPropage = 100;
	public ChainCells(int tx, int ty, int tz)
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
        bigSphere = bigSphereDraw.getObject3DVoxels(val);
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
	
	public boolean no_tangential_proper_parthood_inverse(Object3D obj1, double obj2_bx, double obj2_by, double obj2_bz, double obj2_radius)
	{
		double distance2Spheres = distCenterUnit2Objects(obj1, obj2_bx, obj2_by, obj2_bz);
		return (obj1.getDistCenterMean() >= distance2Spheres + obj2_radius);
				
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
    

	public void addFirstCell(int nbPropage)
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
            
            if (tangential_proper_parthood_inverse(bigSphere, ccx, ccy, ccz, r_cell))
            {
            	//IJ.log("Test TPPi with sphere:  " +" ccx: "+ccx + " ccy: "+ccy + " ccz: "+ccz + " radius: " + r_cell);
            	ObjectCreator3D cellDraw;
        		cellDraw = new ObjectCreator3D(tx, ty, tz);
    			cellDraw.createEllipsoid((int) ccx, (int) ccy, (int) ccz, r_cell, r_cell, r_cell, 200, false);
    			Object3DVoxels newCell = cellDraw.getObject3DVoxels(200);
                listRadiusCells.add(r_cell);
                listCellsResult.add(newCell);
                //IJ.log("radius of first cell: "+r_cell);
                flag = false;
            }
        }
        if(flag){
        	nbPropage += 100;
        	addFirstCell(nbPropage);
        }
	}   
	public Object3DVoxels addCell2List(Object3DVoxels previousCell)
	{	
			//ArrayList<Object3DVoxels> listSpheres = new ArrayList<Object3DVoxels>();
			double radiusNextCell = random(10, bigSphere.getDistCenterMean()/2);
			//listRadiusCells.add(radiusNextCell);
			double distanceTotal = previousCell.getDistCenterMean() + radiusNextCell;
			
			// creation de la sphere who satisfy the condition EC
			ObjectCreator3D sphere2eDraw;
			sphere2eDraw = new ObjectCreator3D(tx, ty, tz);
			sphere2eDraw.createEllipsoid((int) Math.round(previousCell.getCenterX()), (int) Math.round(previousCell.getCenterY()),
					                    (int) Math.round(previousCell.getCenterZ()), distanceTotal, distanceTotal, distanceTotal, 200, false);
	        Object3DVoxels secondSphere = sphere2eDraw.getObject3DVoxels(val);
	        //listSpheres.add(secondSphere);
	        
	        //create a sphere who satisfy the condition TPPi
	        ObjectCreator3D sphere3eDraw;
			sphere3eDraw = new ObjectCreator3D(tx, ty, tz);
			double distance2Spheres = bigSphere.getDistCenterMean() - radiusNextCell;
	        // creation de la sphere who satisfy the condition EC
			sphere3eDraw.createEllipsoid((int) Math.round(bigSphere.getCenterX()), (int) Math.round(bigSphere.getCenterY()),
					(int) Math.round(bigSphere.getCenterZ()), distance2Spheres, distance2Spheres, distance2Spheres, val, false);
	        Object3DVoxels thirdSphere = sphere3eDraw.getObject3DVoxels(val);
	        //listSpheres.add(thirdSphere);
	        
	        ImageHandler img0 = new ImageByte("Sphere_1", tx, ty, tz);
	        secondSphere.draw(img0);
        	//img0.show("coucou_0");
        	ImageHandler img0_v2 = new ImageByte("Sphere_1", tx, ty, tz);
        	secondSphere.draw(img0_v2);
        	ImageInt img00 = (ImageInt)img0_v2;
        	ArrayList<Voxel3D> contours_img00 = computeContours(img00, img00.offsetX, img00.offsetY, img00.offsetZ, val);
        	ImageHandler img00_contours = new ImageByte("Contours_1", tx, ty, tz);
        	ImageHandler img00_contours_v2 = new ImageByte("Contours_1", tx, ty, tz);
        	for(Voxel3D v00 : contours_img00){
        		img00_contours.setPixel(v00, val);
        		img00_contours_v2.setPixel(v00, val);
        	}
        	img00_contours.show("result of img00_contours");
        	
        	ImageHandler img1 = new ImageByte("Sphere_2", tx, ty, tz);
        	thirdSphere.draw(img1);
        	img1.show("coucou_1");
        	ImageInt img01 = (ImageInt)img1;
        	ArrayList<Voxel3D> contours_img01 = computeContours(img01, img01.offsetX, img01.offsetY, img01.offsetZ, val);
        	ImageHandler img01_contours = new ImageByte("Contours_1", tx, ty, tz);
        	for(Voxel3D v01 : contours_img01){
        		img01_contours.setPixel(v01, val);
        	}
        	img01_contours.show("result of img01_contours");
        	
        	img00_contours_v2.intersectMask((ImageInt)img01_contours);
        	img00_contours_v2.show("the last result ^^");
        	ImageInt img_result = (ImageInt)img00_contours_v2;
        	ArrayList<Voxel3D> contours_img_result = computeContours(img_result, img_result.offsetX, img_result.offsetY, img_result.offsetZ, val);
        	rangeSearch.addAll(contours_img_result);
        	int ranIndex = RANDOM.nextInt(rangeSearch.size());
        	IJ.log("center of second cell: " + contours_img_result.get(ranIndex));
        	Voxel3D centerSecondCell = contours_img_result.get(ranIndex);
        	contours_img_result.clear();
        	ObjectCreator3D nextCellDraw;
        	nextCellDraw = new ObjectCreator3D(tx, ty, tz);
        	nextCellDraw.createEllipsoid(centerSecondCell.getRoundX(), centerSecondCell.getRoundY(), centerSecondCell.getRoundZ(),
        			                     radiusNextCell, radiusNextCell, radiusNextCell, val, false);
        	Object3DVoxels nextCell = nextCellDraw.getObject3DVoxels(val);
        	return nextCell;
	}
	/**
	 * exist a list of elements cells 
	 * add a cell to list 
	 * a new cell will EC with the last element in the list --> condition sphere 
	 * a range = union of range_cell1 + range_cell2 + range_cell3 + ...+ range_last_cell. 
	 * random a cell in this range. 
	 * verify condition of this cell.
	 * add this cell at the last of list cell
	 * throw exception
	 */
	/*public void addCell2List(){
		if(listCellsResult==null){
			addFirstCell(100);
		}
		else{
			int sizeOfListCells = listCellsResult.size();
			Object3DVoxels lastElementCell = listCellsResult.get(sizeOfListCells - 1);
			ArrayList<Object3DVoxels> listSpheres = intersectSpheres(lastElementCell);
		}
		
	}*/
}
