/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package BioStatistics3D;

import ij.IJ;
import ij.ImagePlus;
import java.util.ArrayList;
import java.util.Arrays;
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
import mcib_plugins.analysis.spatialAnalysis;

/**
 *
 * @author ttnhoa
 */
public class Repulsion3D {
    /**
	 * Declare the variable global 
	 */
	int minR = 1;
	double epsilonEC = 2, epsilonTPPi = 1 ;
	//public static Random RANDOM = new Random(System.nanoTime());
        Random rand = new Random();
	public ArrayList<Double> listRadiusCells;
	//public double radiusFirstCell = 0, radiusSecondCell = 0;
	//public ArrayList<Voxel3D> listCenterPos = new ArrayList<Voxel3D>();
	public int tx, ty, tz, cx, cy, cz, val;
	public double radBigSphere;
	public Object3DVoxels bigSphere = new Object3DVoxels();
	//public ArrayList<Object3DVoxels> listAttractVesicles;
        public ArrayList<Object3DVoxels> listVesiclesResult = new ArrayList<Object3DVoxels>();
        ImageHandler img_big_sphere;
	public int nbVesicles = 20, nbAttractionVes = 2;
	public int nbPropage = 100;
	public int value_grey = 255;
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

	public Repulsion3D(int tx, int ty, int tz)
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
       
        
        public ImageHandler getCenterPositionsDC(double radiusNextVesicle, Object3DVoxels previousVesicle)
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

            ArrayList<Voxel3D> listVoxelImg00 = Utils.createListVoxels(img00, val);
            ImageHandler img00_contours = img_big_sphere.duplicate();

            for(Voxel3D v00 : listVoxelImg00)
            {
                    img00_contours.setPixel(v00, 0);
            }
            return img00_contours;
        }
        public Vox3D[] createListVox3D(ImageFloat img)
        {
            int length = 0, count = 0;
            for(int z=0; z<img.sizeZ; z++)
            {
                for(int x=0; x<img.sizeX; x++)
                {
                    for(int y=0; y<img.sizeY; y++)
                    {
                        if(img.getPixel(x, y, z) > 0)
                        {
                                length++;
                        }
                    }	
                }	
            }
            Vox3D[] voxList = new Vox3D[length];
            for(int z=0; z<img.sizeZ; z++)
            {
                for(int x=0; x<img.sizeX; x++)
                {
                    for(int y=0; y<img.sizeY; y++)
                    {
                        if(img.getPixel(x, y, z) > 0)
                        {
                                voxList[count] = new Vox3D(img.getPixel(x,y,z), x, y, z);
                                count++;
                        }
                    }	
                }	
            }
            /*for (int z = 0; z < img.sizeZ; z++) 
            {
                for (int xy = 0; xy < img.sizeXY; xy++) 
                {
                    if(img.getPixel(xy, z) > 0)
                    {
                        length++;
                    }    
                }
            }*/
            
            /*for (int z = 0; z < img.sizeZ; z++) 
            {
                for (int xy = 0; xy < img.sizeXY; xy++) 
                {
                    if(img.getPixel(xy, z) > 0)
                    {
                        voxList[count] = new Vox3D(img.getPixel(xy, z), xy, z);
                        count++;
                    }    
                }
            }*/
            return voxList;
        }
        /**
         * Euclidian Distance Map
         * @param img
         * @param distance
         * @return 
         */
        public ImageFloat getEDMDomainPosition(ImageHandler img, double distance)
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
                    for (int xy = 0; xy < r2.sizeXY; xy++) 
                    {
                        if (r2.getPixel(xy, z) < distance) 
                        {
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
            spa.process(imagePlus, imgMask, true);
        }
        public Object3DVoxels addFirstVesicle(int nbPropage)
	{	
            int nThrows = 0; 
            boolean flag = true;
            while (flag && nThrows < nbPropage) 
            {
                nThrows++;
                //Random center  
                double ccx = Utils.random(cx - Math.random() * (radBigSphere - minR), cx + Math.random() * (radBigSphere - minR));
                double ccy = Utils.random(cy - Math.random() * (radBigSphere - minR), cy + Math.random() * (radBigSphere - minR));
                double ccz = Utils.random(cz - Math.random() * (radBigSphere - minR), cz + Math.random() * (radBigSphere - minR));
                double r_cell = Utils.random(1, radBigSphere/10);
                //IJ.log("stupid_"+nThrows);
                if (RCC.no_tangential_proper_parthood_inverse(bigSphere, ccx, ccy, ccz, r_cell))
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
        public Object3DVoxels addNextVesicle()
        {
            ImageHandler imgTotal = new ImageByte("vesicle_", tx, ty, tz);
            double distance = 0.75;
            int i = 0;
            for(Object3DVoxels v : listVesiclesResult)
            {
                i++;
                v.draw(imgTotal,255);
            }
            //imgTotal.show("res");
            
            ImageFloat imgEDM;
            imgEDM = getEDMDomainPosition(imgTotal, distance);
            //imgEDM.show("hic");
            
            double radiusNextVesicle = Utils.random(1, bigSphere.getDistCenterMean()/10);
            //ImageHandler imgDomainDC;
            //imgDomainDC = getCenterPositionsDC(radiusNextVesicle, firstVesicle);
            imgEDM.intersectMask((ImageInt)img_big_sphere);            
            Vox3D[] domainPosible = createListVox3D(imgEDM);;
            int sizePosible = domainPosible.length;
            IJ.log("size of list possible: " + sizePosible);
            
            Arrays.sort(domainPosible);
            /*for(int j=sizePosible * 9 /10; j<sizePosible; j++)
            {
                IJ.log("Value: " + domainPosible[j].distance + " ");
            } */   
            if(sizePosible > 0)
            { 
                int nThrow = 0;
                //boolean flag = true;
                while(nThrow < nbPropage)
                {
                    nThrow++;
                    int iMin = (int)8 * sizePosible/10;
                    int iMax = sizePosible-1;
                    int ranIndex = iMin + rand.nextInt(iMax - iMin);
                    IJ.log("ran index: " + ranIndex);
                    Vox3D centerSecondCell = domainPosible[ranIndex];
                    IJ.log("position vox3d: " + centerSecondCell.distance + " " 
                          + " x: " + centerSecondCell.x + " y: "+centerSecondCell.y + " z: "+centerSecondCell.z);
                    if(RCC.no_tangential_proper_parthood_inverse(bigSphere, centerSecondCell.x
                            , centerSecondCell.y, centerSecondCell.z, radiusNextVesicle))
                    {
                        ObjectCreator3D nextVesicleDraw;
                        nextVesicleDraw = new ObjectCreator3D(tx, ty, tz);
                        nextVesicleDraw.createEllipsoid(centerSecondCell.x, centerSecondCell.y, centerSecondCell.z,
                                                 radiusNextVesicle, radiusNextVesicle, radiusNextVesicle, val, false);
                        Object3DVoxels nextVesicle = nextVesicleDraw.getObject3DVoxels(val);

                        int count = 0;
                        for (Object3DVoxels ci : listVesiclesResult) {
                            if(RCC.disconnection(ci, nextVesicle))
                            {
                                count++;
                            }
                        }
                        if(count == listVesiclesResult.size())
                        {
                            IJ.log("--------------------------------------------");
                            IJ.log("Next Vesicle " + nextVesicle);
                            //listVesiclesResult.add(nextVesicle);
                            IJ.log("--------------------------------------------");
                            return nextVesicle;
                        }   
                    }
                }    
                
            }
            return null;
        }       
        public void randomListRepulsionVesicles(int nbVesicles)
        {
            //double distance = 0.2;
            int nbTotal = 0;
            Object3DVoxels firstVesicle = addFirstVesicle(100);
            if(firstVesicle != null)
            {
                    listVesiclesResult.add(firstVesicle);
                    nbTotal++;
            }
            IJ.log("Size of list: " + listVesiclesResult.size());
            
            
            while(nbTotal <= nbVesicles)
            {
                //nbTotal++;
                //addNextVesicle();
                
                Object3DVoxels nextVesicle = addNextVesicle();
                if(nextVesicle!=null){
                    listVesiclesResult.add(nextVesicle);
                    nbTotal++;
                }
            }
            IJ.log("Size of list total: " + listVesiclesResult.size());
            showImagesResult();
                
        }   
       
        
        /*public ImageFloat intersect2Images(ImageHandler img1, ImageHandler img2, double distance)
        {
            ImageFloat imgEDMResult1;
            imgEDMResult1 = getEDMDomainPosition(img1, distance);
            ImageFloat imgEDMResult3 = imgEDMResult1.duplicate();
            imgEDMResult3.show("EDM1");
            
            ImageFloat imgEDMResult2;
            imgEDMResult2 = getEDMDomainPosition(img2, distance);
            imgEDMResult2.show("EDM2");
            for (int z = 0; z < imgEDMResult1.sizeZ; z++) 
            {
                for (int xy = 0; xy < imgEDMResult1.sizeXY; xy++) 
                {
                    if (imgEDMResult2.getPixel(xy, z)==0.0) 
                    {   
                        imgEDMResult1.setPixel(xy, z, (float)0.0);
                    }
                }
            }
            return imgEDMResult1;
        }*/
        public void showImagesResult()
        {
            ImageHandler img1 = new ImageByte("result final", tx, ty, tz);
            int i = 0;
            for(Object3DVoxels v : listVesiclesResult)
            {
                i++;
                v.draw(img1,i);
            }
            img1.show();
            calculStatistic(img1);
        }     
}
