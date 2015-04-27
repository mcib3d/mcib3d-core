/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package BioStatistics3D;

import ij.IJ;
import java.util.ArrayList;
import java.util.Arrays;
import mcib3d.geom.Object3DVoxels;
import mcib3d.geom.ObjectCreator3D;
import mcib3d.geom.Voxel3D;
import mcib3d.image3d.ImageByte;
import mcib3d.image3d.ImageFloat;
import mcib3d.image3d.ImageHandler;
import mcib3d.image3d.ImageInt;
import mcib3d.image3d.distanceMap3d.EDT;

/**
 *
 * @author ttnhoa
 */
public class Demo {
    int minR = 1;
	public ArrayList<Double> listRadiusCells;
	public int tx, ty, tz, cx, cy, cz, val;
	public double radBigSphere;
        float distanceMax = 0;
        ImageHandler img_big_sphere;
	public Object3DVoxels bigSphere = new Object3DVoxels();
        public ArrayList<Object3DVoxels> listElementsExisting = new ArrayList<>();
        ArrayList<Object3DVoxels> listCentroid = new ArrayList<>();
        
	//public int value_grey = 200;
        //ImagePlus imgPlus, imgModify;
        
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
        public Demo(int tx, int ty, int tz)
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
              
        public void getDistanceMax()
        {
            boolean inverse = false;
            ImageFloat r = getEDT(img_big_sphere, inverse, 1);
            if (r != null) 
            {   
                ImageFloat r2 = r.duplicate(); 
                Vox3D[] idx = getListVox3D(r2, img_big_sphere.threshold(1, inverse, true));
                distanceMax =  idx[idx.length-1].distance; 
            }
        }
        public ImageFloat getEDT(ImageHandler img, boolean inverse, int threshold)
        {
            ImageFloat r = EDT.run(img, threshold, inverse, Runtime.getRuntime().availableProcessors());
            return r;
        }
        public ImageFloat getTPPiDomain(ImageHandler img, float radiusVes)
        {
            boolean inverse = false;
            int threshold = 1;
            ImageFloat r = getEDT(img, inverse, threshold);
            //r.show("TestEDT inverse = false");
            Vox3D[] idx = getListVox3D(r, img.threshold(threshold, inverse, true));
            ImageFloat imgTPPi;
            imgTPPi = new ImageFloat("tppi", r.sizeX, r.sizeY, r.sizeZ);            
            float eps = (float) 0.5;    

            for (Vox3D idx1 : idx) 
            {
                if(idx1.distance >= radiusVes && idx1.distance < radiusVes + eps)
                {
                    imgTPPi.pixels[idx1.z][idx1.xy] = idx1.distance;
                }

            }
            return imgTPPi;
                  
        }  
        public ImageFloat getDCDomain(float radiusVes, ArrayList<Object3DVoxels> listExistingVesicles, int ratio)
        {
            boolean inverse = true;
            int threshold = 1;
            ImageHandler img1;
            img1 = new ImageByte("vesicle_", tx, ty, tz);
            int i = 0;
            for(Object3DVoxels v : listExistingVesicles)
            {
                    i++;
                    v.draw(img1, val);
            }
            ImageFloat edt = getEDT(img1, inverse, threshold);
            if (edt != null) 
            {   
                ImageFloat r2 = edt.duplicate(); 
                ImageInt mask = img1.threshold(threshold, inverse, true);
                Vox3D[] idx = getListVox3D(r2, mask);
                ImageFloat imgDC;
                imgDC = new ImageFloat("dc", r2.sizeX, r2.sizeY, r2.sizeZ);
                float eps = (float) 0.5;    
                
                for (Vox3D idx1 : idx) 
                {
                    if(idx1.distance > radiusVes * ratio)
                    {
                        imgDC.pixels[idx1.z][idx1.xy] = idx1.distance;
                    }
                }
                return imgDC;
            }
            return null;
            
        }
        public Vox3D[] getListVox3D(ImageFloat distanceMap, ImageInt mask)
        {
            int c = 0;
            Vox3D[] idx = new Vox3D[mask.countMaskVolume()];
            for (int z = 0; z < distanceMap.sizeZ; z++) {
                for (int xy = 0; xy < distanceMap.sizeXY; xy++) {
                    if (mask.getPixelInt(xy, z) != 0 && distanceMap.pixels[z][xy] !=0) 
                    {   
                        idx[c] = new Vox3D(distanceMap.pixels[z][xy], xy, z);
                        c++;
                    }
                }
            }
            Arrays.sort(idx);
            return idx;
        
        }  
        public float randomRadius()
        {
            return (float)Utils.random(2, distanceMax / 10); 
        }
        public void generateListCentroid(int nbCentroid)
        {
            int count = 0;
            
            while(count < nbCentroid)
            {
                if(listCentroid.isEmpty())
                {
                    float radiusNewCentroid = randomRadius();
                    ImageFloat imgTPPi = getTPPiDomain(img_big_sphere, radiusNewCentroid);
                    ImageFloat imgTPPiDemo = imgTPPi.duplicate();
                    //imgTPPiDemo.show("TPPi Domain S1");
                    //imgTPPiDemo.save("home/ttnhoa/Picture/imgRapport");
                    ArrayList<Voxel3D> domainPossible = Utils.createListVoxels(imgTPPi, val);
                    int sizeList = domainPossible.size();
                    IJ.log("size " + sizeList);
                    if(sizeList > 0)
                    {
                        int ranIndex = Utils.RANDOM.nextInt(sizeList-1);
                        Voxel3D centerObj = domainPossible.get(ranIndex);
                        ObjectCreator3D nextObjDraw;
                        nextObjDraw = new ObjectCreator3D(tx, ty, tz);
                        nextObjDraw.createEllipsoid(centerObj.getRoundX(), centerObj.getRoundY(), centerObj.getRoundZ(),
                                                 radiusNewCentroid, radiusNewCentroid, radiusNewCentroid, val, false);
                        Object3DVoxels newCentroid = nextObjDraw.getObject3DVoxels(val);
                        //newCentroid.setName("centroid");
                        listCentroid.add(newCentroid);
                        listElementsExisting.add(newCentroid);
                        count++;
                        IJ.log("Add new centroid " + count + " : "+ centerObj + " radius: " + radiusNewCentroid);
                        domainPossible.clear();
                        showImagesResult(listCentroid);
                    }    
                }
                else
                {
                    float radiusNewCentroid = randomRadius();
                    ImageFloat imgTPPi = getTPPiDomain(img_big_sphere, radiusNewCentroid);
                    ImageFloat imgDC = getDCDomain(radiusNewCentroid, listCentroid, 2);
                    
                    //ImageFloat imgDCDemo = imgDC.duplicate();
                    //imgDCDemo.intersectMask((ImageInt)img_big_sphere);
                    
                    //imgDCDemo.show("DC Domain S");
                    //imgDCDemo.save("home/ttnhoa/Picture/imgRapport");
                    
                    //ImageFloat imgTPPiDemo = imgTPPi.duplicate();
                    //imgTPPiDemo.show("TPPi Domain S");
                    //imgTPPiDemo.save("home/ttnhoa/Picture/imgRapport");
                    
                    imgTPPi.intersectMask(imgDC);
                    //ImageFloat imgIntersectDemo = imgTPPi.duplicate();
                    //imgIntersectDemo.show("intersection Domain ");
                    //imgIntersectDemo.save("home/ttnhoa/Picture/imgRapport");
                    
                    
                    ArrayList<Voxel3D> domainPosible = Utils.createListVoxels(imgTPPi, val);
                    int sizeList = domainPosible.size();
                    if(sizeList > 0)
                    {
                        int ranIndex = Utils.RANDOM.nextInt(sizeList-1);
                        Voxel3D centerObj = domainPosible.get(ranIndex);
                        ObjectCreator3D nextObjDraw;
                        nextObjDraw = new ObjectCreator3D(tx, ty, tz);
                        nextObjDraw.createEllipsoid(centerObj.getRoundX(), centerObj.getRoundY(), centerObj.getRoundZ(),
                                                 radiusNewCentroid, radiusNewCentroid, radiusNewCentroid, val, false);
                        Object3DVoxels newCentroid = nextObjDraw.getObject3DVoxels(val);
                        listCentroid.add(newCentroid);
                        listElementsExisting.add(newCentroid);
                        count++;
                        IJ.log("Add new centroid " + count + ": "+ centerObj + " radius: " +radiusNewCentroid);
                        domainPosible.clear();
                        showImagesResult(listCentroid);
                    }
                }
            }    
            
            
        }
        public ImageFloat getClusterDomain(double rangeCluster, ArrayList<Object3DVoxels> listExistingVesicles)
        {
            boolean inverse = true;
            int threshold = 1;
            ImageHandler img1 = new ImageByte("cluster_", tx, ty, tz);
            int i = 0;
            for(Object3DVoxels v : listExistingVesicles)
            {
                    i++;
                    v.draw(img1, val);
            }
            ImageFloat edtCluster = getEDT(img1, inverse, threshold);
            
            if (edtCluster != null) 
            {   
                ImageFloat r2 = edtCluster.duplicate(); 
                normalizeDistanceMap(r2, img1.threshold(threshold, inverse, true));
                for (int z = 0; z < r2.sizeZ; z++) {
                    for (int xy = 0; xy < r2.sizeXY; xy++) {
                        if (r2.getPixel(xy, z) > rangeCluster) {
                            r2.pixels[z][xy] = 0;
                        }   
                    }
                }
                return r2;
            }
            return null;
        }
        public ImageFloat getClusterDomainRepulsion(double rangeCluster, ArrayList<Object3DVoxels> listExistingVesicles)
        {
            boolean inverse = true;
            int threshold = 1;
            ImageHandler img1 = new ImageByte("cluster_", tx, ty, tz);
            int i = 0;
            for(Object3DVoxels v : listExistingVesicles)
            {
                    i++;
                    v.draw(img1, val);
            }
            ImageFloat edtCluster = getEDT(img1, inverse, threshold);
            
            if (edtCluster != null) 
            {   
                ImageFloat r2 = edtCluster.duplicate(); 
                normalizeDistanceMap(r2, img1.threshold(threshold, inverse, true));
                for (int z = 0; z < r2.sizeZ; z++) {
                    for (int xy = 0; xy < r2.sizeXY; xy++) {
                        if (r2.getPixel(xy, z) < rangeCluster) {
                            r2.pixels[z][xy] = 0;
                        }   
                    }
                }
                return r2;
            }
            return null;
        }
        public ImageFloat getClusterDomain1(double rangeCluster, ArrayList<Object3DVoxels> listExistingVesicles)
        {
            boolean inverse = true;
            int threshold = 1;
            ImageHandler img1 = new ImageByte("cluster_", tx, ty, tz);
            int i = 0;
            for(Object3DVoxels v : listExistingVesicles)
            {
                    i++;
                    v.draw(img1, val);
            }
            ImageFloat edtCluster = getEDT(img1, inverse, threshold);
            
            if (edtCluster != null) 
            {   
                ImageFloat r2 = edtCluster.duplicate(); 
                normalizeDistanceMap(r2, img1.threshold(threshold, inverse, true));
                for (int z = 0; z < r2.sizeZ; z++) {
                    for (int xy = 0; xy < r2.sizeXY; xy++) {
                        if (r2.getPixel(xy, z) > rangeCluster) {
                            r2.pixels[z][xy] = 0;
                        }   
                    }
                }
                return r2;
            }
            return null;
        }
        public ImageFloat getClusterDomain2(double rangeCluster, ArrayList<Object3DVoxels> listExistingVesicles)
        {
            boolean inverse = true;
            int threshold = 1;
            ImageHandler img1 = new ImageByte("cluster_", tx, ty, tz);
            int i = 0;
            for(Object3DVoxels v : listExistingVesicles)
            {
                    i++;
                    v.draw(img1, val);
            }
            ImageFloat edtCluster = getEDT(img1, inverse, threshold);
            
            if (edtCluster != null) 
            {   
                ImageFloat r2 = edtCluster.duplicate(); 
                normalizeDistanceMap(r2, img1.threshold(threshold, inverse, true));
                
                return r2;
            }
            return null;
        }
        public void generateEntitiesPerCluster(int nbEntities)
        {
           double rangeCluster = 0.2; 
           int iteration = 0;
           for(Object3DVoxels ci : listCentroid)
           {
                iteration++;
                ci.setName("C" + iteration);
                int nbSuccess = 0, nbThrow = 0, nbPropage = 10;
                ArrayList<Object3DVoxels> listElementsInClusterCi = new ArrayList<Object3DVoxels>();
                listElementsInClusterCi.add(ci);
                
                while(nbSuccess < nbEntities && nbThrow < nbPropage)
                {
                    nbThrow++;
                    float radiusNewElement = randomRadius();
                    ImageFloat imgTPPi = getTPPiDomain(img_big_sphere, radiusNewElement);
                    ImageFloat imgDC = getDCDomain(radiusNewElement, listElementsExisting, 1);
                    ImageFloat imgCluster = getClusterDomain(rangeCluster, listElementsInClusterCi);
                    
                    ImageFloat imgClusterDemo = imgCluster.duplicate();
                    imgClusterDemo.show("cluster domain");
                    
                    ImageFloat imgDCDemo = imgDC.duplicate();
                    imgDCDemo.intersectMask((ImageInt)img_big_sphere);
                    imgDCDemo.show("DC domain");
                    
                    ImageFloat imgTPPiDemo = imgTPPi.duplicate();
                    imgTPPiDemo.show("TPPi domain");
                    
                    
                    
                    /*ImageFloat imgCluster1 = getClusterDomainRepulsion(0.8, listElementsInClusterCi);
                    ImageFloat imgCluster2 = getClusterDomain1(0.2, listElementsInClusterCi);
                    ImageFloat imgCluster3 = getClusterDomain2(0.1, listElementsInClusterCi);
                    imgCluster1.intersectMask((ImageInt)img_big_sphere);
                    imgCluster2.intersectMask((ImageInt)img_big_sphere);
                    imgCluster3.intersectMask((ImageInt)img_big_sphere);
                    imgCluster1.show("repulsion domain");
                    
                    imgCluster2.show("cluster domain");
                    imgCluster3.show("random distribution");
                    */
                    if(imgTPPi != null && imgDC !=null && imgCluster != null)
                    {
                        imgTPPi.intersectMask(imgDC);   //domain local
                        ImageFloat domainLocal = imgTPPi.duplicate();
                        
                        ImageFloat imgIntersect = imgTPPi.duplicate();
                        imgIntersect.show("domain local");
                    
                        domainLocal.intersectMask(imgCluster);
                        ImageFloat domainPossible = domainLocal.duplicate();
                        domainPossible.show("domain possible");
                        
                        ArrayList<Voxel3D> domainPosible = Utils.createListVoxels(domainLocal, val);
                        int sizeList = domainPosible.size();
                     
                        if(sizeList > 0)
                        {
                            int ranIndex = Utils.RANDOM.nextInt(sizeList-1);
                            Voxel3D centerObj = domainPosible.get(ranIndex);
                            ObjectCreator3D nextObjDraw;
                            nextObjDraw = new ObjectCreator3D(tx, ty, tz);
                            nextObjDraw.createEllipsoid(centerObj.getRoundX(), centerObj.getRoundY(), centerObj.getRoundZ(),
                                                     radiusNewElement, radiusNewElement, radiusNewElement, val, false);
                            Object3DVoxels newElement = nextObjDraw.getObject3DVoxels(val);
                            newElement.setName("C"+iteration);
                            listElementsExisting.add(newElement);
                            listElementsInClusterCi.add(newElement);
                            nbSuccess++;
                            IJ.log("Add new element " + nbSuccess + ": "+ centerObj + radiusNewElement);
                            domainPosible.clear();
                        }
                    }
                    
                }
                listElementsInClusterCi.clear();
            }
            
            
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
        public void showImagesResult(ArrayList<Object3DVoxels> listObj)
        {
                ImageHandler img1 = new ImageByte("list_obj", tx, ty, tz);
		int i = 0;
		for(Object3DVoxels v : listObj)
                {    
                        i++;
                        v.draw(img1, 255);
                }
                img1.show("demo_result");
               
        }     
        
        public void runPattern(int nbCentroid, int nbEntities)
        {
            IJ.log("_____________Start program______________________");
            getDistanceMax();
            IJ.log("distance max: " + distanceMax);
            generateListCentroid(nbCentroid);
            IJ.log("Number of centroid generated: " + listCentroid.size());
            generateEntitiesPerCluster(nbEntities);
            showImagesResult(listElementsExisting);
            IJ.log("_____________End program______________________");
            
        }
}
