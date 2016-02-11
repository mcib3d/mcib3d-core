/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mcib3d.geom;

import java.util.ArrayList;

/**
 *
 * @author jean
 */
public class Object3DPoint extends Object3DVoxels {
    // fast constructor for singletons...
    public Object3DPoint(int value, Point3D point) {
        init(value, point.getX(), point.getY(), point.getZ());
    }
    
    public Object3DPoint(int value, double x, double y, double z){
        init(value, x, y, z);
    }
    
    private void init(int label, double x, double y, double z) {
        this.voxels = new ArrayList(1);
        voxels.add(new Voxel3D(x, y, z, 0));
        this.contours=voxels;
        this.value = label;
        resXY = 1.0;
        resZ = 1.0;
        units = "pix";
        // center of mass
        cx = x;
        cy = y;
        cz = z;
        // barycenter
        bx = cx;
        by = cy;
        bz = cz;
        // moments
        s200 = Double.NaN;
        s110 = Double.NaN;
        s101 = Double.NaN;
        s020 = Double.NaN;
        s011 = Double.NaN;
        s002 = Double.NaN;
        eigen = null;
        // dist center
        distcentermin = Double.NaN;
        distcentermax = Double.NaN;
        distcentermean = Double.NaN;
        distcentersigma = Double.NaN;
        // feret
        feret = Double.NaN;
        feret1 = null;
        feret2 = null;
        integratedDensity = Double.NaN;
        pixmax = Double.NaN;
        pixmin = Double.NaN;
        sigma = Double.NaN;
        volume = 1;
        areaNbVoxels = -1;
        areaContactUnit = -1;
        //bb
        xmin = (int)cx;
        xmax = (int)(cx+0.5);
        ymin = (int)cy;
        ymax = (int)(cy+0.5);
        zmin = (int)cz;
        zmax = (int)(cz+0.5);
        
    }
}
