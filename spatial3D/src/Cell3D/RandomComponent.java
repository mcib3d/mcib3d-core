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


public class RandomComponent {
	
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
	public boolean disconnection(Object3D obj1, double obj2_bx, double obj2_by, double obj2_bz, double obj2_radius)
	{
		double distance2Spheres = distCenterUnit2Objects(obj1, obj2_bx, obj2_by, obj2_bz);
		return (distance2Spheres > (obj1.getDistCenterMean() + obj2_radius));
	}
	public boolean disconnection(Object3D obj1, Object3D obj2)
	{
		double distance2Spheres = distCenterUnit2Objects(obj1, obj2);
		return (distance2Spheres > (obj1.getDistCenterMean() + obj2.getDistCenterMean()));
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
	public ArrayList<Object3DVoxels> randomVesicleInNucleusV2(double minRadiusV, double maxRadiusV, int nbVesicles, int tx, int ty, int tz)
	{
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
        
        
        Object3DVoxels ves;
        //MereoObject3D mereo;
        // test small sphere
        //boolean ok = false;
        int nThrows = 0, nbSuccess = 0;
        ArrayList<Object3DVoxels> vesList = new ArrayList<Object3DVoxels>();
        IJ.log("Random vesicle in cell");
        
        while (nThrows < 100 && nbSuccess < nbVesicles) 
        {
        	nThrows++;
        	//Random center vesicle 
        	double ccx = random(cx - Math.random() * (rad - minR), cx + Math.random() * (rad - minR));
			double ccy = random(cy - Math.random() * (rad - minR), cy + Math.random() * (rad - minR));
			double ccz = random(cz - Math.random() * (rad - minR), cz + Math.random() * (rad - minR));
			double r_vesicle = Math.random() * (maxRadiusV - minRadiusV) + minRadiusV;
			
			//vesDraw = new ObjectCreator3D(tx, ty, tz);
			//vesDraw.createEllipsoid((int) ccx, (int) ccy, (int) ccz, r_vesicle, r_vesicle, r_vesicle, 200, false);
            //ves = vesDraw.getObject3DVoxels(200);
            //mereo = new MereoObject3D(bigSphere, ves);
            //IJ.log("Obj " + ves + " " + mereo.getRCC8Relationship());
            if (tangential_proper_parthood_inverse(bigSphere, ccx, ccy, ccz, r_vesicle)) 
            {
            	nbSuccess++;
            	ObjectCreator3D vesDraw;
            	vesDraw = new ObjectCreator3D(tx, ty, tz);
    			vesDraw.createEllipsoid((int) ccx, (int) ccy, (int) ccz, r_vesicle, r_vesicle, r_vesicle, 200, false);
                ves = vesDraw.getObject3DVoxels(200);
            	IJ.log("Obj " + ves + " TPPi " +" ccx: "+ccx + " ccy: "+ccy + " ccz: "+ccz + " radius: " + r_vesicle);
                vesList.add(ves);
            } 
        }       
        return vesList;
	}
	public ArrayList<Object3DVoxels> randomVesicleInNucleus(double minRadiusV, double maxRadiusV, int nbVesicles, int tx, int ty, int tz)
	{
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
        
        
        Object3DVoxels ves;
        MereoObject3D mereo;
        int nThrows = 0, nbSuccess = 0;
        ArrayList<Object3DVoxels> vesList = new ArrayList<Object3DVoxels>();
        
        while (nThrows < 50 && nbSuccess < nbVesicles) 
        {
        	nThrows++;
        	//Random center vesicle 
        	double ccx = random(cx - Math.random() * (rad - minR), cx + Math.random() * (rad - minR));
			double ccy = random(cy - Math.random() * (rad - minR), cy + Math.random() * (rad - minR));
			double ccz = random(cz - Math.random() * (rad - minR), cz + Math.random() * (rad - minR));
			double r_vesicle = Math.random() * (maxRadiusV - minRadiusV) + minRadiusV;
			ObjectCreator3D vesDraw;
			vesDraw = new ObjectCreator3D(tx, ty, tz);
			vesDraw.createEllipsoid((int) ccx, (int) ccy, (int) ccz, r_vesicle, r_vesicle, r_vesicle, 200, false);
            ves = vesDraw.getObject3DVoxels(200);
            mereo = new MereoObject3D(bigSphere, ves);
            //IJ.log("Obj " + ves + " " + mereo.getRCC8Relationship());
            if (mereo.TangentialProperParthoodInverse() || mereo.NonTangentialProperParthoodInverse()) 
            {
            	nbSuccess++;
            	IJ.log("Obj " + ves + " " + mereo.getRCC8Relationship() +" ccx: "+ccx + " ccy: "+ccy + " ccz: "+ccz + " radius: " + r_vesicle);
                vesList.add(ves);
            } else {
                vesDraw.clear();
            }
        }       
        return vesList;
	}
	
	public ArrayList<Object3DVoxels> randomCells(double minRadiusCell, double maxRadiusCell, int nbCells, int tx, int ty, int tz)
	{
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
        
        Object3DVoxels cell;
        Object3DVoxels previousCell = new Object3DVoxels();
        
        MereoObject3D mereo, mereo1;
        // test small sphere
        //boolean ok = false;
        int nThrows = 0, nbSuccess = 0;
        while (nThrows < 10000 && nbSuccess < nbCells) 
        {
        	nThrows++;
        	//Random center vesicle 
        	double ccx = random(cx - Math.random() * (rad - minR), cx + Math.random() * (rad - minR));
			double ccy = random(cy - Math.random() * (rad - minR), cy + Math.random() * (rad - minR));
			double ccz = random(cz - Math.random() * (rad - minR), cz + Math.random() * (rad - minR));
			double r_cell = Math.random() * (maxRadiusCell - minRadiusCell) + minRadiusCell;
			ObjectCreator3D cellDraw;
	        cellDraw = new ObjectCreator3D(tx, ty, tz);
			cellDraw.createEllipsoid((int) ccx, (int) ccy, (int) ccz, r_cell, r_cell, r_cell, 200, false);
            cell = cellDraw.getObject3DVoxels(200);
            //cell.distCenterUnit(cell);
            //cell.getDistCenterMean();
            mereo = new MereoObject3D(bigSphere, cell);
            //IJ.log("Obj " + ves + " " + mereo.getRCC8Relationship());
            if (mereo.TangentialProperParthoodInverse())
            {
            	//cellList.add(cell);
            	IJ.log("Test : " + cell + " " + mereo.getRCC8Relationship() +" ccx: "+ccx + " ccy: "+ccy + " ccz: "+ccz + " radius: " + r_cell);
            	if(cellList.isEmpty()){
            		cellList.add(cell);
            		previousCell = cell;
            		nbSuccess++;
            		IJ.log("--------------------------------------------");
            		IJ.log("First Obj " + cell + " " + mereo.getRCC8Relationship() +" ccx: "+ccx + " ccy: "+ccy + " ccz: "+ccz + " radius: " + r_cell);
            	}
            	else{
            		mereo1 = new MereoObject3D(cell, previousCell);
            		if(mereo1.Disconnection()){
            			nbSuccess++;
            			cellList.add(cell);
            			IJ.log("--------------------------------------------");
                    	IJ.log("DC Obj " + cell + " " + mereo.getRCC8Relationship() +" ccx: "+ccx + " ccy: "+ccy + " ccz: "+ccz + " radius: " + r_cell);
                        
            		}
            		
            	}
            	            	
            	
            } else {
                cellDraw.clear();
            }
        }  
        IJ.log("-----------------Finish---------------------------");
        return cellList;
	}     
	
	public ArrayList<Object3DVoxels> randomCellsV2(double minRadiusCell, double maxRadiusCell, int nbCells, int tx, int ty, int tz)
	{
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
        
        Object3DVoxels newCell;
        Object3DVoxels previousCell = new Object3DVoxels();
        
        int nThrows = 0, nbSuccess = 0;
        while (nThrows < 100000) 
        {
        	nThrows++;
        	//Random center  
        	double ccx = random(cx - Math.random() * (rad - minR), cx + Math.random() * (rad - minR));
			double ccy = random(cy - Math.random() * (rad - minR), cy + Math.random() * (rad - minR));
			double ccz = random(cz - Math.random() * (rad - minR), cz + Math.random() * (rad - minR));
			//double r_cell = Math.random() * (maxRadiusCell - minRadiusCell) + minRadiusCell;
			double r_cell = random(minRadiusCell, maxRadiusCell);
            //cell.distCenterUnit(cell);
            //cell.getDistCenterMean();
            if (tangential_proper_parthood_inverse(bigSphere, ccx, ccy, ccz, r_cell))
            {
            	//cellList.add(cell);
            	IJ.log("Test TPPi with sphere:  " +" ccx: "+ccx + " ccy: "+ccy + " ccz: "+ccz + " radius: " + r_cell);
            	ObjectCreator3D cellDraw;
        		cellDraw = new ObjectCreator3D(tx, ty, tz);
    			cellDraw.createEllipsoid((int) ccx, (int) ccy, (int) ccz, r_cell, r_cell, r_cell, 200, false);
                newCell = cellDraw.getObject3DVoxels(200);
            	if(cellList.isEmpty()){
            		cellList.add(newCell);
            		previousCell = newCell;
            		nbSuccess++;
            		IJ.log("--------------------------------------------");
            		IJ.log("First Obj: " + newCell +" ccx: "+ccx + " ccy: "+ccy + " ccz: "+ccz + " radius: " + r_cell);
            		IJ.log("--------------------------------------------");
            	}
            	else{
            		if(external_connection(newCell, previousCell))
            		{
            			int count = 0;
            			for(Object3DVoxels c: cellList)
            			{
            				if(disconnection(c, newCell) || external_connection(c, newCell))
            				{
            					count++;
            				}	
            			}	
            			if(count == cellList.size())
            			{
            				nbSuccess++;
                			cellList.add(newCell);
                			previousCell = newCell;
                			IJ.log("--------------------------------------------");
                        	IJ.log("EC Obj " + newCell + " ccx: " + ccx + " ccy: " + ccy + " ccz: " + ccz + " radius: " + r_cell);
                        	IJ.log("--------------------------------------------");
            			}	
            		}
            		
            	}
            }  
        }   
        IJ.log("-----------------Finish---------------------------");
        IJ.log("number of success: "+nbSuccess);
        return cellList;
	}    
	
	public ArrayList<Object3DVoxels> randomCellsV3(double minRadiusCell, double maxRadiusCell, int nbCells, int tx, int ty, int tz)
	{
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
        Object3DVoxels newCell;
        Object3DVoxels previousCell = new Object3DVoxels();
        
        int nThrows = 0, nbSuccess = 0;
        double volume = 0; 
        while (nThrows < 100000 && nbSuccess < nbCells) 
        {
        	nThrows++;
        	//Random center  
        	double ccx = random(cx - Math.random() * (rad - minR), cx + Math.random() * (rad - minR));
			double ccy = random(cy - Math.random() * (rad - minR), cy + Math.random() * (rad - minR));
			double ccz = random(cz - Math.random() * (rad - minR), cz + Math.random() * (rad - minR));
			//double r_cell = Math.random() * (maxRadiusCell - minRadiusCell) + minRadiusCell;
			double r_cell = random(minRadiusCell, maxRadiusCell);
            //cell.distCenterUnit(cell);
            //cell.getDistCenterMean();
            if (tangential_proper_parthood_inverse(bigSphere, ccx, ccy, ccz, r_cell))
            {
            	//cellList.add(cell);
            	//IJ.log("Test TPPi with sphere:  " +" ccx: "+ccx + " ccy: "+ccy + " ccz: "+ccz + " radius: " + r_cell);
            	ObjectCreator3D cellDraw;
        		cellDraw = new ObjectCreator3D(tx, ty, tz);
    			cellDraw.createEllipsoid((int) ccx, (int) ccy, (int) ccz, r_cell, r_cell, r_cell, 200, false);
                newCell = cellDraw.getObject3DVoxels(200);
            	if(cellList.isEmpty()){
            		cellList.add(newCell);
            		previousCell = newCell;
            		nbSuccess++;
            		volume += r_cell * r_cell * r_cell;
            		IJ.log("--------------------------------------------");
            		IJ.log("First Obj: " + newCell +" ccx: "+ccx + " ccy: "+ccy + " ccz: "+ccz + " radius: " + r_cell);
            		IJ.log("--------------------------------------------");
            	}
            	
            	else{
            		if(external_connection(newCell, previousCell))
            		{
            			int count = 0;
            			for(Object3DVoxels c: cellList)
            			{
            				if(disconnection(c, newCell) || external_connection(c, newCell))
            				{
            					count++;
            				}	
            			}	
            			if(count == cellList.size())
            			{
            				nbSuccess++;
                			cellList.add(newCell);
                			previousCell = newCell;
                			volume += r_cell * r_cell * r_cell;
                			IJ.log("--------------------------------------------");
                        	IJ.log("Next Obj " + newCell + " ccx: " + ccx + " ccy: " + ccy + " ccz: " + ccz + " radius: " + r_cell);
                        	IJ.log("--------------------------------------------");
            			}	
            			
                        
            		}
            		
            	}
            }  
        }    
        volume = volume * (4/3) * Math.PI ;
        IJ.log("-----------------Finish---------------------------");
        IJ.log("number of success: "+nbSuccess +" volume of total: " + volume);
        IJ.log("Volume of big sphere: "+ (4/3) * Math.PI * rad * rad * rad);
        return cellList;
	}    
	
	

}
