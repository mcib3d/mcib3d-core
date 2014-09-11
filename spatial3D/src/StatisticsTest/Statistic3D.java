package StatisticsTest;



import ij.IJ;
//import ij.plugin.PlugIn;
//import ij.process.FloatProcessor;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Random;
import mcib3d.geom.Object3D;
import mcib3d.geom.Object3DVoxels;
import mcib3d.geom.ObjectCreator3D;
import mcib3d.geom.Voxel3D;
import mcib3d.image3d.ImageByte;
import mcib3d.image3d.ImageFloat;
import mcib3d.image3d.ImageHandler;
import mcib3d.image3d.ImageInt;
import mcib3d.image3d.distanceMap3d.EDT;
//import mcib_plugins.EDT_3D;
//import mcib3d.image3d.ImageByte;
//import mcib3d.image3d.ImageHandler;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author nhuhoa
 */
/**
 * Statistic 2D 
 * Steps: 
 * 1. Create a cell (large zone) containing all clusters of vesicles.
 * and stocker this cell in an image 3D with (01, R1)
 * 2. Random a vesicle in this cell. function inside of
 * 3. Random R2, create domain possible EC, DC
 * 4. Define domain possible of global spatial.
 * 5. Intersection of two domain --> results zone. 
 * 6. Random a point in this result zone. 
 * 7. Construct a second vesicle with this center and radius R2. 
 * @author nhuhoa
 */
public class Statistic3D {
        /**
	 * Declare the variable global 
	 */
	int minR = 1;
	double epsilonEC = 1, epsilonTPPi = 1 ;
	public static Random RANDOM = new Random(System.nanoTime());
	public ArrayList<Double> listRadiusCells;
	//public double radiusFirstCell = 0, radiusSecondCell = 0;
	//public ArrayList<Voxel3D> listCenterPos = new ArrayList<Voxel3D>();
	public int tx, ty, tz, cx, cy, cz, val;
	public double radBigSphere;
	public Object3DVoxels bigSphere = new Object3DVoxels();
	
	public int nbVesicles = 20, nbAttractionVes = 2;
	public int nbPropage = 3000;
	public int value_grey = 200;
        
        
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

	public Statistic3D(int tx, int ty, int tz)
	{
		this.tx = tx; 
		this.ty = ty;
		this.tz = tz;
		//center for the sphere
                this.cx = tx / 2;
                this.cy = ty / 2;
                this.cz = tz / 2;
	}
        public void createCell()
        {
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
        public boolean no_tangential_proper_parthood_inverse(Object3D obj1, double obj2_bx, double obj2_by, double obj2_bz, double obj2_radius)
	{
		double distance2Spheres = distCenterUnit2Objects(obj1, obj2_bx, obj2_by, obj2_bz);
		return (obj1.getDistCenterMean() >= distance2Spheres + obj2_radius);
	}
        public boolean no_tangential_proper_parthood_inverse(Object3D obj1, Object3D obj2)
	{
		return no_tangential_proper_parthood_inverse(obj1, obj2.getCenterX(), obj2.getCenterY() , obj2.getCenterZ(), obj2.getDistCenterMean());
	}
        public boolean disconnection(Object3D obj1, Object3D obj2)
	{
		double distance2Spheres = distCenterUnit2Objects(obj1, obj2);
		return (distance2Spheres >= (obj1.getDistCenterMean() + obj2.getDistCenterMean() + epsilonEC));
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
                    double r_cell = random(2, radBigSphere/10);
                    //IJ.log("stupid_"+nThrows);
                    if (no_tangential_proper_parthood_inverse(bigSphere, ccx, ccy, ccz, r_cell))
                    {
                        //IJ.log("Test TPPi with sphere:  " +" ccx: "+ccx + " ccy: "+ccy + " ccz: "+ccz + " radius: " + r_cell);
                        ObjectCreator3D vesDraw;
                                vesDraw = new ObjectCreator3D(tx, ty, tz);
                                vesDraw.createEllipsoid((int) ccx, (int) ccy, (int) ccz, r_cell, r_cell, r_cell, value_grey, false);
                                Object3DVoxels newVesicle = vesDraw.getObject3DVoxels(value_grey);
                                /*J.log("--------------------------------------------");
                                IJ.log("First Vesicle : " + newVesicle);
                                IJ.log("--------------------------------------------");
                                */
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
	public void createListVesicle()
	{		
                Object3DVoxels previousVesicle;
                ImageFloat imgClusterRes;
                ArrayList<Object3DVoxels> listVesiclesResult = new ArrayList<>();
                int nSuccess = 0, nThrow = 0;
                while(listVesiclesResult.isEmpty())
                {
                    Object3DVoxels firstVesicle = addFirstVesicle(100);
                    if(firstVesicle != null)
                    {
                            listVesiclesResult.add(firstVesicle);
                            nSuccess = 1;
                    }
                }
                previousVesicle = listVesiclesResult.get(0);
                ImageHandler img = new ImageByte("Vesicle", tx, ty, tz);
                previousVesicle.draw(img);
                imgClusterRes = createClusterDomainPos(img);
                ImageFloat imgRes1 = imgClusterRes.duplicate();
                imgRes1.show("EVF");
                //createClusterDomain();
                
		while(nSuccess < 20 && nThrow < 50)
		{
			nThrow++;
                        
			Object3DVoxels nextVesicle = addVesicle(previousVesicle, imgClusterRes, listVesiclesResult);
			if(nextVesicle != null)
			{
				listVesiclesResult.add(nextVesicle);
				nSuccess++;
			}	
			
		}	
		IJ.log("size of list vesicles: "+listVesiclesResult.size());
                showImagesResult(listVesiclesResult);
                listVesiclesResult.clear();
                
                
        }
        
        /*public void createClusterDomain(Object3DVoxels previousVesicle)
        {
            previousVesicle = listVesiclesResult.get(0);
            ImageHandler img = new ImageByte("Vesicle", tx, ty, tz);
            previousVesicle.draw(img);
            imgClusterRes = createClusterDomainPos(img);
            ImageFloat imgRes1 = imgClusterRes.duplicate();
            imgRes1.show("EVF");
        } */       
        public Object3DVoxels addVesicle(Object3DVoxels previousVesicle, ImageFloat imgClusterRes, ArrayList<Object3DVoxels> listVesiclesResult)
        {
                
                double radiusNextVesicle = random(2, bigSphere.getDistCenterMean()/10);
                ImageHandler imgDomainDC;
                imgDomainDC = getDomainCenterPosDC(radiusNextVesicle, previousVesicle);
                //imgDomainDC.show("Img DC");
                imgClusterRes.intersectMask((ImageInt) imgDomainDC);
                //imgRes.show("result final");
                ArrayList<Voxel3D> domainPosible = createListVoxels(imgClusterRes);
                //IJ.log("size of contours possible: " + domainPosible.size());
                //imgRes.show("copy img");
                int sizeListPos = domainPosible.size();
                if(sizeListPos > 0)
                {
                    int ranIndex = RANDOM.nextInt(sizeListPos);
                    Voxel3D centerSecondCell = domainPosible.get(ranIndex);
                    if(no_tangential_proper_parthood_inverse(bigSphere, centerSecondCell.getX(), centerSecondCell.getY(), centerSecondCell.getZ(), radiusNextVesicle))
                    {
                        ObjectCreator3D nextVesicleDraw;
                        nextVesicleDraw = new ObjectCreator3D(tx, ty, tz);
                        nextVesicleDraw.createEllipsoid(centerSecondCell.getRoundX(), centerSecondCell.getRoundY(), centerSecondCell.getRoundZ(),
                                                 radiusNextVesicle, radiusNextVesicle, radiusNextVesicle, val, false);
                        Object3DVoxels nextVesicle = nextVesicleDraw.getObject3DVoxels(val);
                        
                        int count = 0;
                        for (Object3DVoxels ci : listVesiclesResult) {
                            if(disconnection(ci, nextVesicle))
                            {
                                count++;
                            }
                        }
                        if(count == listVesiclesResult.size())
                        {
                            IJ.log("--------------------------------------------");
                            IJ.log("Next Vesicle " + nextVesicle);
                            IJ.log("--------------------------------------------");
                            domainPosible.clear();
                            return nextVesicle;
                        }    
                        
                        
                    }
                    	

                } 
                return null;
        }        
        /**
        * Compute the contours of the object rad=0.5
        */
        private ArrayList<Voxel3D> computeContours(ImageFloat segImage, int x0, int y0, int z0, int value) 
        {	
           boolean cont;
           ArrayList<Voxel3D> contours = new ArrayList<>();
           float pix0, pix1, pix2, pix3, pix4, pix5, pix6;
           int sx = segImage.sizeX;
           int sy = segImage.sizeY;
           int sz = segImage.sizeZ;
           area = 0;
           areaUnit = 0;
           double XZ = resXY * resZ;
           double XX = resXY * resXY;

           ArrayList<Voxel3D> voxelList;
           voxelList = createListVoxels(segImage);
           //IJ.log("img intersection: "+img0);
           ArrayList<Integer> sizeSphere = computeBounding(voxelList);
           int xmin = 0;
           int xmax = segImage.sizeX;
           int ymin = 0;
           int ymax = sizeSphere.get(3);
           int zmin = sizeSphere.get(4);
           int zmax = sizeSphere.get(5);
           for (int k = zmin - z0; k <= zmax - z0; k++) {
               for (int j = ymin - y0; j <= ymax - y0; j++) {
                   for (int i = xmin - x0; i <= xmax - x0; i++) {
                       cont = false;
                       if (segImage.contains(i, j, k)) {
                           pix0 = segImage.getPixel(i, j, k);
                           if (pix0 > 0) {

                                   area++;
                                   Voxel3D voxC = new Voxel3D(i + x0, j + y0, k + z0, value);
                                   contours.add(voxC);
                               }
                       }
                   }
               }    
           }
           return contours;
        }

	
        
        public ImageFloat createClusterDomainPos(ImageHandler img)
        {
            boolean inverse = true;
            int threshold = 1;
            ImageFloat r = EDT.run(img, threshold, inverse, Runtime.getRuntime().availableProcessors());
            if (r != null) 
            {   
                ImageFloat r2 = r.duplicate();
                normalizeDistanceMap(r2, img.threshold(threshold, inverse, true));
                for (int z = 0; z < r2.sizeZ; z++) {
                    for (int xy = 0; xy < r2.sizeXY; xy++) {
                        if (r2.getPixel(xy, z) > 0.1) {
                            r2.pixels[z][xy] = 0;
                        }
                    }
                }
                //r2.show("EVF");
                return r2;
            }
            return null;
            
        }  
        
        public void normalizeDistanceMap(ImageFloat distanceMap, ImageInt mask) 
        {
            int count = 0;
            Vox3D[] idx = new Vox3D[mask.countMaskVolume()];
            double volume = idx.length;
            for (int z = 0; z < distanceMap.sizeZ; z++) {
                for (int xy = 0; xy < distanceMap.sizeXY; xy++) {
                    if (mask.getPixelInt(xy, z) != 0) {
                        idx[count] = new Vox3D(distanceMap.pixels[z][xy], xy, z);
                        count++;
                    }
                }
            }
            Arrays.sort(idx);
            for (int i = 0; i < idx.length - 1; i++) {
                // gestion des repetitions
                if (idx[i + 1].distance == idx[i].distance) {
                    int j = i + 1;
                    while (j < (idx.length - 1) && idx[i].distance == idx[j].distance) {
                        j++;
                    }
                    double median = (i + j) / 2d;
                    for (int k = i; k <= j; k++) {
                        idx[k].index = median;
                    }
                    i = j;
                } else {
                    idx[i].index = i;
                }
            }
            if (idx[idx.length - 1].index == 0) {
                idx[idx.length - 1].index = idx.length - 1;
            }
            for (Vox3D idx1 : idx) {
                distanceMap.pixels[idx1.z][idx1.xy] = (float) (idx1.index / volume);
            }
        }
        public class Vox3D implements Comparable<Vox3D> 
        {
            float distance;
            double index;
            int xy, z;

            public Vox3D(float distance, int xy, int z) {
                this.distance = distance;
                this.xy = xy;
                this.z = z;
            }

            public Vox3D(float distance, double index, int xy, int z) {
                this.distance = distance;
                this.index = index;
                this.xy = xy;
                this.z = z;
            }

            @Override
            public int compareTo(Vox3D v) {
                if (distance > v.distance) {
                    return 1;
                } else if (distance < v.distance) {
                    return -1;
                } else {
                    return 0;
                }
            }
        }      
        public void showImagesResult(ArrayList<Object3DVoxels> listVesiclesResult)
        {
                ImageHandler img_big_sphere = new ImageByte("Big Sphere",tx,ty,tz);
		bigSphere.draw(img_big_sphere);
                //img_big_sphere.save("/home/ttnhoa/Pictures/img_cells_2306_ttnhoa/");
                img_big_sphere.show("Big Sphere");
                
		int i = 0;
		for(Object3DVoxels v : listVesiclesResult)
                {
                        //IJ.log("position of vesicle: " + i + " x: " + v.getCenterX() + " y: " + v.getCenterY() + " z: "+v.getCenterZ());
                        i++;
                        ImageHandler img = new ImageByte("Vesicle_" + i, tx, ty, tz);
                        v.draw(img);
                        //img.save("/home/ttnhoa/Pictures/img_cells_2306_ttnhoa/");
                        img.show("vesicle_" + i );
                }
        }        
        public ArrayList<Voxel3D> createListVoxels(ImageHandler img0)
	{
		ArrayList<Voxel3D> voxelList = new ArrayList<>();
                for(int z=0; z<img0.sizeZ; z++)
                {
                        for(int x=0; x<img0.sizeX; x++)
                        {
                                for(int y=0; y<img0.sizeY; y++)
                                {
                                        if(img0.getPixel(x, y, z) > 0)
                                        {
                                                Voxel3D v = new Voxel3D(x, y, z, val);
                                                voxelList.add(v);
                                        }
                                }	
                        }	
                }
    	return voxelList;
	}
	/**
     * Compute the bounding box of the object
     * @param voxels
     * @return 
     */
    public ArrayList<Integer> computeBounding(ArrayList<Voxel3D> voxels) 
    {
    	ArrayList<Integer> sizeSphere = new ArrayList<>();
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
        ArrayList<Voxel3D> contours = new ArrayList<>();
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
    
    public ImageHandler getDomainCenterPosDC(double radiusNextVesicle, Object3DVoxels previousVesicle)
    {
		double distanceTotal = previousVesicle.getDistCenterMean() + radiusNextVesicle;
		
		// creation a sphere who satisfy the condition EC
		ObjectCreator3D sphere2eDraw;
		sphere2eDraw = new ObjectCreator3D(tx, ty, tz);
		sphere2eDraw.createEllipsoid((int) Math.round(previousVesicle.getCenterX()), (int) Math.round(previousVesicle.getCenterY()),
				                    (int) Math.round(previousVesicle.getCenterZ()), distanceTotal, distanceTotal, distanceTotal, 200, false);
                Object3DVoxels secondSphere = sphere2eDraw.getObject3DVoxels(val);

                ImageHandler img0 = new ImageByte("Sphere_1", tx, ty, tz);
                secondSphere.draw(img0);

                //compute contour of image 0
                ImageInt img00 = (ImageInt)img0;
                
                ArrayList<Voxel3D> listVoxelImg00 = createListVoxels(img00);
   
                ImageHandler img_big_sphere = new ImageByte("Big Sphere",tx,ty,tz);
		bigSphere.draw(img_big_sphere);
                ImageHandler img00_contours = img_big_sphere.duplicate();
                
                for(Voxel3D v00 : listVoxelImg00)
                {
                        img00_contours.setPixel(v00, 0);
                }
                return img00_contours;
    }
}
