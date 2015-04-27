/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package BioStatistics3D;

import java.util.ArrayList;
import java.util.Random;
import mcib3d.geom.Object3D;
import mcib3d.geom.Voxel3D;
import mcib3d.image3d.ImageHandler;

/**
 *
 * @author ttnhoa
 */
public class Utils {
        public static Random RANDOM = new Random(System.nanoTime());
        
        public static final double random(final double pMin, final double pMax) {
	    return pMin + RANDOM.nextDouble() * (pMax - pMin);
        }
        public static final int random(final int pMin, final int pMax) {
	    return pMin + RANDOM.nextInt() * (pMax - pMin);
        }
        public static double distCenterUnit2Objects(Object3D obj1, double obj2_bx, double obj2_by, double obj2_bz) 
        {
            double dist = Math.sqrt((obj1.getCenterX() - obj2_bx) * (obj1.getCenterX() - obj2_bx) * obj1.getResXY() * obj1.getResXY()
                        + (obj1.getCenterY() - obj2_by) * (obj1.getCenterY() - obj2_by) * obj1.getResXY() * obj1.getResXY() 
                        + (obj1.getCenterZ() - obj2_bz) * (obj1.getCenterZ() - obj2_bz) * obj1.getResZ() * obj1.getResZ());
            return dist;
        }
	public static double distCenterUnit2Objects(Object3D obj1, Object3D obj2) 
	{
            double dist = Math.sqrt((obj1.getCenterX() - obj2.getCenterX()) * (obj1.getCenterX() - obj2.getCenterX()) * obj1.getResXY() * obj1.getResXY()
        		+ (obj1.getCenterY() - obj2.getCenterY()) * (obj1.getCenterY() - obj2.getCenterY()) * obj1.getResXY() * obj1.getResXY() 
        		+ (obj1.getCenterZ() - obj2.getCenterZ()) * (obj1.getCenterZ() - obj2.getCenterZ()) * obj1.getResZ() * obj1.getResZ());
            return dist;
        }
        public static ArrayList<Voxel3D> createListVoxels(ImageHandler img0, int val)
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
        
}
