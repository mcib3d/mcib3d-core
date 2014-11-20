package StatisticsTest;



import ij.IJ;
import ij.ImagePlus;
import ij.process.FloatProcessor;
import ij.process.ImageProcessor;
/*import ij.ImageStack;
import ij.Undo;
import ij.WindowManager;
import ij.gui.EllipseRoi;
import ij.gui.PolygonRoi;
import ij.gui.Roi;
import ij.process.ByteProcessor;
import ij.process.ShortProcessor;
import java.awt.Polygon;
import java.awt.Rectangle;
*/
//import ij.plugin.PlugIn;
//import ij.process.FloatProcessor;
import java.util.ArrayList;
import java.util.Arrays;
//import java.util.Iterator;
import java.util.Random;
import mcib3d.geom.Object3D;
import mcib3d.geom.Object3DVoxels;
import mcib3d.geom.ObjectCreator3D;
//import mcib3d.geom.Point3D;
import mcib3d.geom.Voxel3D;
import mcib3d.image3d.ImageByte;
import mcib3d.image3d.ImageFloat;
import mcib3d.image3d.ImageHandler;
import mcib3d.image3d.ImageInt;
import mcib3d.image3d.distanceMap3d.EDT;
import mcib_plugins.analysis.spatialAnalysis;
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
public class Cluster3D {
        /**
	 * Declare the variable global 
	 */
	int minR = 1;
	double epsilonEC = 2, epsilonTPPi = 1 ;
	public static Random RANDOM = new Random(System.nanoTime());
	public ArrayList<Double> listRadiusCells;
	//public double radiusFirstCell = 0, radiusSecondCell = 0;
	//public ArrayList<Voxel3D> listCenterPos = new ArrayList<Voxel3D>();
	public int tx, ty, tz, cx, cy, cz, val;
	public double radBigSphere;
	public Object3DVoxels bigSphere = new Object3DVoxels();
	//public ArrayList<Object3DVoxels> listAttractVesicles;
        public ArrayList<Object3DVoxels> historyVesicles;
        ImageHandler img_big_sphere;
	public int nbVesicles = 20, nbAttractionVes = 2;
	public int nbPropage = 3000;
	public int value_grey = 200;
        
        ImagePlus imgPlus, imgModify;
        ArrayList<Voxel3D> objects;
        
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

	public Cluster3D(int tx, int ty, int tz)
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
                this.val = 255;

                //create a big sphere
                ObjectCreator3D bigSphereDraw;
                bigSphereDraw = new ObjectCreator3D(tx, ty, tz);
                // creation de la sphere
                bigSphereDraw.createEllipsoid(cx, cy, cz, radBigSphere, radBigSphere, radBigSphere, val, false);
                this.bigSphere = bigSphereDraw.getObject3DVoxels(val);
                img_big_sphere = new ImageByte("Big Sphere",tx,ty,tz);
                this.bigSphere.draw(img_big_sphere);
                img_big_sphere.show("img big sphere");
                
            
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
        
        public ArrayList<Object3DVoxels> randomAttractVesicles(int nbVesicles)
        {
            ArrayList<Object3DVoxels> listAttractVesicles = new ArrayList<Object3DVoxels>();
            int nSuccess = 0, nThrow = 0;
            while(listAttractVesicles.isEmpty())
            {
                nThrow++;
                Object3DVoxels firstVesicle = addFirstVesicle(100);
                if(firstVesicle != null)
                {
                        listAttractVesicles.add(firstVesicle);
                        nSuccess = 1;
                }
            }
            
            while(nSuccess < nbVesicles && nThrow < 100)
            {
                    nThrow++;
                    Object3DVoxels nextVesicle = addAttractVesicle(listAttractVesicles);
                    if(nextVesicle != null)
                    {
                            listAttractVesicles.add(nextVesicle);
                            nSuccess++;
                    }	
            }
            IJ.log("size of list attract vesicles: " + listAttractVesicles.size());
            //showImagesResult(listAttractVesicles, "AttractVesicle");
            return listAttractVesicles;
            
        }   
        public void randomElementsCluster(int nbVesicles)
        {
            ArrayList<Object3DVoxels> listAttractVesicles = randomAttractVesicles(nbVesicles);
            historyVesicles = new ArrayList<>();
            //showImagesResult(listAttractVesicles, "attract vesicle");
            while(!listAttractVesicles.isEmpty())
            {
                int lastCurrentIndex = listAttractVesicles.size();
                Object3DVoxels attractVes = listAttractVesicles.get(lastCurrentIndex - 1);
                listAttractVesicles.remove(lastCurrentIndex - 1);
                IJ.log("remove a element: " + attractVes);
                attractVes.setName("attraction vesicle");
                historyVesicles.add(attractVes);
                createListVesicle(attractVes, historyVesicles);
                IJ.log("create a list of cluster correspond");
                
            } 
            IJ.log("Size of list history: " + historyVesicles.size());
            //calculStatistic();
            showImagesResult(historyVesicles);
            
        
        }        
        
        /**
	 * random radius of a new cell 
	 * traverse the list of cell exist 
	 * found the list of center possible 
	 * random a element in this list 
         * @param attractVesicle
	 */
	public void createListVesicle(Object3DVoxels attractVesicle, ArrayList<Object3DVoxels> listVesiclesResult)
	{		
                
                
                //ArrayList<Object3DVoxels> listVesiclesResult = new ArrayList<>();
                int nSuccess = 0, nThrow = 0;
                /*while(listVesiclesResult.isEmpty())
                {
                    Object3DVoxels firstVesicle = addFirstVesicle(100);
                    if(firstVesicle != null)
                    {
                            listVesiclesResult.add(firstVesicle);
                            nSuccess = 1;
                    }
                }*/
                //attractVesicle = listVesiclesResult.get(0);
                
                //ImageFloat imgRes1 = imgClusterRes.duplicate();
                //imgRes1.show("EVF");
                //createClusterDomain();
                ImageFloat imgClusterRes;
                ImageHandler img = new ImageByte("Vesicle", tx, ty, tz);
                attractVesicle.draw(img);
                imgClusterRes = getClusterDomainPosition(img);
		while(nSuccess <= 10 && nThrow < 50)
		{
			nThrow++;
			Object3DVoxels nextVesicle = addVesicle(imgClusterRes, attractVesicle, listVesiclesResult);
			if(nextVesicle != null)
			{
                                nextVesicle.setName("normal");
				listVesiclesResult.add(nextVesicle);
				nSuccess++;
			}	
			
		}	
		IJ.log("size of list vesicles: " + listVesiclesResult.size());
                //showImagesResult(listVesiclesResult, "vesicle");
                //listVesiclesResult.clear();
                //return listVesiclesResult;
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
        
        public Object3DVoxels addAttractVesicle(ArrayList<Object3DVoxels> listAttractVesicles)
        {
                int size_current = listAttractVesicles.size();
                Object3DVoxels previousVesicle = listAttractVesicles.get(size_current - 1);
                double radiusNextVesicle = random(2, bigSphere.getDistCenterMean()/10);
                ImageHandler imgDomainDC;
                imgDomainDC = getDomainCenterPosDC(radiusNextVesicle, previousVesicle);
                //imgDomainDC.show("Img DC");
                ImageHandler clone_sphere = img_big_sphere.duplicate();
                clone_sphere.intersectMask((ImageInt) imgDomainDC);
                //imgRes.show("result final");
                ArrayList<Voxel3D> domainPosible = createListVoxels(clone_sphere);
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
                        for (Object3DVoxels ci : listAttractVesicles) {
                            if(disconnection(ci, nextVesicle))
                            {
                                count++;
                            }
                        }
                        if(count == listAttractVesicles.size())
                        {
                            IJ.log("--------------------------------------------");
                            IJ.log("Next Attract Vesicle " + nextVesicle);
                            IJ.log("--------------------------------------------");
                            domainPosible.clear();
                            return nextVesicle;
                        }   
                    }
                }
                return null;
        
        }        
        public Object3DVoxels addVesicle(ImageFloat imgClusterRes, Object3DVoxels attractVesicle, ArrayList<Object3DVoxels> listVesiclesResult)
        {       
                
                double radiusNextVesicle = random(2, bigSphere.getDistCenterMean()/10);
                ImageHandler imgDomainDC;
                imgDomainDC = getDomainCenterPosDC(radiusNextVesicle, attractVesicle);
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
        
        public ImageFloat getClusterDomainPosition(ImageHandler img)
        {
            boolean inverse = true;
            int threshold = 1;
            ImageFloat r = EDT.run(img, threshold, inverse, Runtime.getRuntime().availableProcessors());
            if (r != null) 
            {   
                ImageFloat r2 = r.duplicate(); 
                //ImageFloat r3 = r.duplicate(); 
                normalizeDistanceMap(r2, img.threshold(threshold, inverse, true));
                for (int z = 0; z < r2.sizeZ; z++) {
                    for (int xy = 0; xy < r2.sizeXY; xy++) {
                        if (r2.getPixel(xy, z) > 0.08) {
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
            /*for(int k = 0; k < idx.length/2; k++)
            {
                distanceMap.pixels[idx[k].z][idx[k].xy] = (float) (idx[k].index / volume);
            }    
            for(int k1 = idx.length/2; k1 < idx.length - 1; k1++)
            {
                distanceMap.pixels[idx[k1].z][idx[k1].xy] = 0;
            } */   
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
                //ImageHandler img_big_sphere = new ImageByte("Big Sphere",tx,ty,tz);
		//bigSphere.draw(img_big_sphere);
                //img_big_sphere.save("/home/ttnhoa/Pictures/img_cells_2306_ttnhoa/");
                //img_big_sphere.show("Big Sphere");
                ImageHandler img1 = new ImageByte("vesicle_", tx, ty, tz);
                
                
                //bigSphere.draw(img1);
		int i = 0;
		for(Object3DVoxels v : listVesiclesResult)
                {
                    //IJ.log("position of vesicle: " + i + " x: " + v.getCenterX() + " y: " + v.getCenterY() + " z: "+v.getCenterZ());
                    if(v.getName()=="normal")
                    {    
                        i++;
                        v.draw(img1,i);
                        //ImageHandler img2 = new ImageByte("vesicle_", tx, ty, tz);
                        //v.draw(img2);
                        //img.save("/home/ttnhoa/Pictures/img_cells_2306_ttnhoa/");
                        //img2.show("vesicle_" + i );
                    }
                    else
                    {
                        i++;
                        v.draw(img1, i);
                        /*ImageHandler img = new ImageByte("vesicle_", tx, ty, tz);
                        v.draw(img);
                        img.show("vesicle_attract_" + i);*/
                        /*ImageHandler img11 = img.duplicate();
                        ImageFloat imgClusterRes = getClusterDomainPosition(img11);
                        //img.save("/home/ttnhoa/Pictures/img_cells_2306_ttnhoa/");
                        imgClusterRes.show("EVF_result_" + i);
                        */
                    
                    }
                }
                img1.show("cluster1_result");
                
                calculStatistic(img1);
                //img2.show("cluster2_result");
        }     
        public void calculStatistic(ImageHandler imgResult)
        {
            int numPoints = 1000;
            int numRandomSamples = 100;
            double distHardCore = 0;
            double env = 0.05;
            
            ImagePlus imagePlus = imgResult.duplicate().getImagePlus();
            ImageHandler img = img_big_sphere.duplicate();
            ImagePlus imgMask = img.getImagePlus();
            
            spatialAnalysis spa = new spatialAnalysis(numPoints, numRandomSamples, distHardCore, env);
            spa.processAll(imagePlus, imgMask, true,true);
            
                    
        }      
      
        /*public ImageByte createMask(ImageHandler img0) {
            ImageByte img1 = new ImageByte("Mask",img_big_sphere.sizeX, img_big_sphere.sizeY,img_big_sphere.sizeZ);;
            for(int z=0; z<img0.sizeZ; z++)
            {
                    for(int x=0; x<img0.sizeX; x++)
                    {
                            for(int y=0; y<img0.sizeY; y++)
                            {
                                    if(img0.getPixel(x, y, z) > 0)
                                    {
                                        img1.setPixel(x, y, z, 255);
                                    }
                            }	
                    }	
            }
            IJ.log("Mask created");
            return img1;
        }*/
    
    
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
    public ImageHandler getDomainCenterPosDC(double radiusNextVesicle, Object3DVoxels previousVesicle)
    {
                double distanceTotal = previousVesicle.getDistCenterMean() + radiusNextVesicle * 2;

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


                ImageHandler img00_contours = img_big_sphere.duplicate();

                for(Voxel3D v00 : listVoxelImg00)
                {
                        img00_contours.setPixel(v00, 0);
                }
                return img00_contours;
    }
}
