/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package BioStatistics3D;

import mcib3d.geom.Object3D;

/**
 *
 * @author ttnhoa
 */
public class RCC {
        public static double epsilonEC = 2, epsilonTPPi = 1 ;
        public static boolean no_tangential_proper_parthood_inverse(Object3D obj1, double obj2_bx, double obj2_by, double obj2_bz, double obj2_radius)
	{
		double distance2Spheres = Utils.distCenterUnit2Objects(obj1, obj2_bx, obj2_by, obj2_bz);
		return (obj1.getDistCenterMean() >= distance2Spheres + obj2_radius);
	}
        public static boolean no_tangential_proper_parthood_inverse(Object3D obj1, Object3D obj2)
	{
		return no_tangential_proper_parthood_inverse(obj1, obj2.getCenterX(), obj2.getCenterY() , obj2.getCenterZ(), obj2.getDistCenterMean());
	}
        public static boolean disconnection2(Object3D obj1, Object3D obj2)
	{
		double distance2Spheres = Utils.distCenterUnit2Objects(obj1, obj2);
		return (distance2Spheres >= (obj1.getDistCenterMean() + obj2.getDistCenterMean() + 3 * epsilonEC));
	}
        public static boolean disconnection(Object3D obj1, Object3D obj2)
	{
		double distance2Spheres = Utils.distCenterUnit2Objects(obj1, obj2);
		return (distance2Spheres >= (obj1.getDistCenterMean() + obj2.getDistCenterMean() + epsilonEC));
	}
        public static boolean tangential_proper_parthood_inverse(Object3D obj1, Object3D obj2)
	{
		return tangential_proper_parthood_inverse(obj1, obj2.getCenterX(), obj2.getCenterY() , obj2.getCenterZ(), obj2.getDistCenterMean());
	}
        public static boolean tangential_proper_parthood_inverse(Object3D obj1, double obj2_bx, double obj2_by, double obj2_bz, double obj2_radius)
	{
		double distance2Spheres = Utils.distCenterUnit2Objects(obj1, obj2_bx, obj2_by, obj2_bz);
		return ((obj1.getDistCenterMean() - distance2Spheres - obj2_radius < epsilonTPPi)
				&& (obj1.getDistCenterMean() >= distance2Spheres + obj2_radius));
	}
}
