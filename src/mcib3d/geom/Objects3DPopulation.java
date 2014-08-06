/*
 * Class for population of 3D objects
 * especially for statistical analyses
 */
package mcib3d.geom;

/**
 * Copyright (C) Thomas Boudier
 *
 * License: This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by the Free
 * Software Foundation; either version 3 of the License, or (at your option) any
 * later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 *
 */
//import ij.IJ;
import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.measure.Calibration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import mcib3d.image3d.ImageHandler;
import mcib3d.image3d.ImageInt;
import mcib3d.utils.ArrayUtil;
import mcib3d.utils.KDTreeC;
import mcib3d.utils.KDTreeC.Item;

/**
 *
 * @author thomas
 */
public class Objects3DPopulation {

    private ArrayList<Object3D> objects;
    private Object3D mask = null;
    private Calibration calibration = null;
    private KDTreeC kdtree = null;
    // link between values and index
    private HashMap<Integer, Integer> hash;
    //private ImageInt labelImage = null;

    /**
     * Conctructor
     */
    public Objects3DPopulation() {
        objects = new ArrayList();
        hash = new HashMap();
        calibration = new Calibration();
    }

    public Objects3DPopulation(Object3D[] objs) {
        objects = new ArrayList();
        hash = new HashMap();
        calibration = new Calibration();
        this.addObjects(objs);
    }

    public Objects3DPopulation(Object3D[] objs, Calibration cal) {
        objects = new ArrayList();
        hash = new HashMap();
        if (cal != null) {
            calibration = cal;
        } else {
            calibration = new Calibration();
        }
        this.addObjects(objs);
    }

    public Objects3DPopulation(ImagePlus plus) {
        objects = new ArrayList();
        hash = new HashMap();
        addImagePlus(plus);
    }

    public Objects3DPopulation(ImageInt plus) {
        objects = new ArrayList();
        hash = new HashMap();
        Calibration cal = plus.getCalibration();
        if (cal == null) {
            cal = new Calibration();
        }
        addImage(plus, cal);
    }

    public Objects3DPopulation(ImageInt plus, int threshold) {
        objects = new ArrayList();
        hash = new HashMap();
        Calibration cal = plus.getCalibration();
        if (cal == null) {
            cal = new Calibration();
        }
        addImage(plus, threshold, cal);
    }

    /**
     *
     * @return
     */
    public Calibration getCalibration() {
        return calibration;
    }

    /**
     *
     * @param cal
     */
    public void setCalibration(Calibration cal) {
        this.calibration = cal;
        // recalibrate objects
        if ((objects != null) && (objects.size() > 0)) {
            for (Object3D obj : objects) {
                obj.setCalibration(calibration);
            }
        }
    }

    // hardcore distance in unit
    /**
     *
     * @param nb
     * @param hardcore
     */
    public void createRandomPopulation(int nb, double hardcore) {
        ArrayList voxlist;
        Voxel3D v;
        Object3D closest;
        double dist;
        Point3D P;
        double HardCoreDistUnit = hardcore;

        // first point
        P = getRandomPointInMask();
        v = new Voxel3D(P.getX(), P.getY(), P.getZ(), 0);
        voxlist = new ArrayList(1);
        voxlist.add(v);
        Object3DVoxels ob = new Object3DVoxels(voxlist);
        addObject(ob);
        for (int i = 1; i < nb; i++) {
            dist = -1;
            while (dist < HardCoreDistUnit) {
                P = getRandomPointInMask();
                closest = closestCenter(P);
                dist = closest.distPixelCenter(P.getX(), P.getY(), P.getZ());
            }
            v = new Voxel3D(P.getX(), P.getY(), P.getZ(), (float) i);
            voxlist = new ArrayList(1);
            voxlist.add(v);
            ob = new Object3DVoxels(voxlist);
            addObject(ob);

        }
    }

    /**
     *
     * @param nb
     * @param r0
     * @param r1
     */
    public void createRandomPopulationDistAbsMb(int nb, double r0, double r1) {
        ArrayList voxlist;
        Voxel3D v;
        Point3D P;
        for (int i = 0; i < nb; i++) {
            voxlist = new ArrayList(1);
            P = getRandomPointInMaskDistAbsMb(r0, r1);
            v = new Voxel3D(P.getX(), P.getY(), P.getZ(), (float) i);
            voxlist.add(v);
            Object3DVoxels ob = new Object3DVoxels(voxlist);
            addObject(ob);
        }
    }

    public void createKDTreeCenters() {
        kdtree = new KDTreeC(3, this.getNbObjects());
        for (int i = 0; i < this.getNbObjects(); i++) {
            kdtree.add(this.getObject(i).getCenterAsArray(), this.getObject(i));
        }
        double[] tmp = {calibration.pixelWidth, calibration.pixelHeight, calibration.pixelDepth};
        kdtree.setScale(tmp);
    }

    /**
     *
     * @param ima
     * @param col
     */
    public void draw(ImageStack ima, int col) {
        Object3D ob;
        Iterator it = objects.iterator();
        while (it.hasNext()) {
            ob = (Object3D) it.next();
            ob.draw(ima, col);
        }
    }

    public void draw(ImageStack ima) {
        Object3D ob;
        Iterator it = objects.iterator();
        while (it.hasNext()) {
            ob = (Object3D) it.next();
            ob.draw(ima, ob.getValue());
        }
    }

    public void draw(ImageHandler ima) {
        Object3D ob;
        Iterator it = objects.iterator();
        while (it.hasNext()) {
            ob = (Object3D) it.next();
            ob.draw(ima, ob.getValue());
        }
    }

    /**
     * Add an object to the population
     *
     * @param obj the 3D object to add
     */
    public void addObject(Object3D obj) {
        obj.setCalibration(calibration);
        objects.add(obj);
        hash.put(obj.getValue(), objects.size() - 1);
        // update kdtree if available // FIXME UPDATE kdtree
        if (kdtree != null) {
            createKDTreeCenters();
        }
        //IJ.log("adding ob " + obj.getValue() + " " + obj.getCenterAsPoint());
    }

    public final void addObjects(Object3D[] objs) {
        for (int i = 0; i < objs.length; i++) {
            //objs[i].setCalibration(calibration);
            addObject(objs[i]);
        }
        // update kdtree if available // FIXME UPDATE kdtree
        if (kdtree != null) {
            createKDTreeCenters();
        }
    }

    public void addObjects(ArrayList<Object3D> list) {
        for (int i = 0; i < list.size(); i++) {
            //objs[i].setCalibration(calibration);
            addObject(list.get(i));
        }
        // update kdtree if available // FIXME UPDATE kdtree
        if (kdtree != null) {
            createKDTreeCenters();
        }
    }

    public void removeObject(int i) {
        hash.remove(objects.get(i));
        objects.remove(i);
    }

    public void addPoints(Point3D[] points) {
        int inc = objects.size();
        for (int i = 0; i < points.length; i++) {
            Point3D P = points[i];
            Voxel3D v = new Voxel3D(P.getX(), P.getY(), P.getZ(), (float) i + inc);
            ArrayList voxlist = new ArrayList(1);
            voxlist.add(v);
            Object3DVoxels ob = new Object3DVoxels(voxlist);
            ob.setCalibration(calibration);
            ob.setValue(i + 1);
            addObject(ob);
        }
        // update kdtree if available // FIXME UPDATE kdtree
        if (kdtree != null) {
            createKDTreeCenters();
        }
    }

    /**
     *
     * @param seg
     */
//    public void addImage(IntImage3D seg, Calibration cali) {
//        int min = (int) seg.getMinAboveValue(0);
//        int max = (int) seg.getMaximum();
//        //IJ.log("min-max " + min + " " + max);
//        // iterate in image  and constructs objects
//        calibration = cali;
//        ArrayList<Voxel3D>[] objectstmp = new ArrayList[max - min + 1];
//        for (int i = 0; i < max - min + 1; i++) {
//            objectstmp[i] = new ArrayList<Voxel3D>();
//        }
//        int pix;
//        for (int k = 0; k < seg.getSizez(); k++) {
//            for (int j = 0; j < seg.getSizey(); j++) {
//                for (int i = 0; i < seg.getSizex(); i++) {
//                    pix = seg.getPixel(i, j, k);
//                    if (pix > 0) {
//                        objectstmp[pix - min].add(new Voxel3D(i, j, k, pix));
//                    }
//                }
//            }
//        }
//        // ARRAYLIST
//        for (int i = 0; i < max - min + 1; i++) {
//            if (!objectstmp[i].isEmpty()) {
//                Object3DVoxels ob = new Object3DVoxels(objectstmp[i]);
//                //ob.setCalibration(calibration);
//                addObject(ob);
//                //IJ.log("adding ob " + ob.getValue() + " " + ob.getCenterAsPoint());
//            }
//        }
//    }
    /**
     *
     * @param seg
     */
    public void addImage(ImageInt seg, int threshold, Calibration cali) {
        int min = (int) seg.getMinAboveValue(0);
        int max = (int) seg.getMax();
        if (max == 0) {
            IJ.log("No objects found");
            return;
        }
        //IJ.log("min-max " + min + " " + max);
        // iterate in image  and constructs objects
        calibration = cali;
        ArrayList<Voxel3D>[] objectstmp = new ArrayList[max - min + 1];
        for (int i = 0; i < max - min + 1; i++) {
            objectstmp[i] = new ArrayList<Voxel3D>();
        }
        int pix;
        int sz = seg.sizeZ;
        int sy = seg.sizeY;
        int sx = seg.sizeX;

        for (int k = 0; k < sz; k++) {
            for (int j = 0; j < sy; j++) {
                for (int i = 0; i < sx; i++) {
                    pix = seg.getPixelInt(i, j, k);
                    if (pix > threshold) {
                        objectstmp[pix - min].add(new Voxel3D(i, j, k, pix));
                    }
                }
            }
        }
        // ARRAYLIST   
        for (int i = 0; i < max - min + 1; i++) {
            if (!objectstmp[i].isEmpty()) {
                Object3DVoxels ob = new Object3DVoxels(objectstmp[i]);
                ob.setLabelImage(seg);
                addObject(ob);
                //IJ.log("adding ob " + ob.getValue() + " " + ob.getCenterAsPoint());
            }
        }
    }

    public void addImage(ImageInt seg, Calibration cali) {
        addImage(seg, 0, cali);
    }

    /**
     *
     * @param plus
     */
    public void addImage(ImagePlus plus) {
        Calibration calplus = plus.getCalibration();
        if (calplus == null) {
            calplus = new Calibration();
            calplus.pixelWidth = 1;
            calplus.pixelHeight = 1;
            calplus.pixelDepth = 1;
            calplus.setUnit("pix");
        }
        this.setCalibration(calplus);
        ImageInt seg = ImageInt.wrap(plus);
        addImage(seg, calplus);
    }

    /**
     *
     * @return
     */
    public Object3D getMask() {
        return mask;
    }

    /**
     *
     * @param mask
     */
    public void setMask(Object3D mask) {
        this.mask = mask;
    }

    /**
     *
     * @param i
     * @return
     */
    public Object3D getObject(int i) {
        return objects.get(i);
    }

    public Object3D getObjectByValue(int val) {
        return objects.get(hash.get(val));
    }

    public int getIndexFromValue(int val) {
        return hash.get(val);
    }

    public int getIndexOf(Object3D ob) {
        return objects.indexOf(ob);
    }

    public Object3D[] getObjectsArray() {
        return (Object3D[]) objects.toArray();
    }

    public ArrayList<Object3D> getObjectsList() {
        return objects;
    }

    /**
     *
     * @return
     */
    public int getNbObjects() {
        return objects.size();
    }

    public int[] getMaxSizeAllObjects() {
        int maxX = 0;
        int maxY = 0;
        int maxZ = 0;
        for (Object3D ob : objects) {
            if (ob.xmax > maxX) {
                maxX = ob.xmax;
            }
            if (ob.ymax > maxY) {
                maxY = ob.ymax;
            }
            if (ob.zmax > maxZ) {
                maxZ = ob.zmax;
            }
        }
        return new int[]{maxX, maxY, maxZ};
    }

    /**
     *
     * @return
     */
    public Point3D getRandomPointInMask() {
        int xmin = mask.getXmin();
        int xmax = mask.getXmax();
        int ymin = mask.getYmin();
        int ymax = mask.getYmax();
        int zmin = mask.getZmin();
        int zmax = mask.getZmax();

        // FIXME NO CALIBRATION   !!!
        double x = (Math.random() * (xmax - xmin) + xmin);
        double y = (Math.random() * (ymax - ymin) + ymin);
        double z = (Math.random() * (zmax - zmin) + zmin);

        while (!mask.inside(x, y, z)) {
            x = Math.random() * (xmax - xmin) + xmin;
            y = Math.random() * (ymax - ymin) + ymin;
            z = Math.random() * (zmax - zmin) + zmin;
        }

        return new Point3D(x, y, z);
    }

    private Point3D getRandomPointInMaskDistAbsMb(double r0, double r1) {
        int xmin = mask.getXmin();
        int xmax = mask.getXmax();
        int ymin = mask.getYmin();
        int ymax = mask.getYmax();
        int zmin = mask.getZmin();
        int zmax = mask.getZmax();

        double x = Math.random() * (xmax - xmin) + xmin;
        double y = Math.random() * (ymax - ymin) + ymin;
        double z = Math.random() * (zmax - zmin) + zmin;

        double dist = Double.MAX_VALUE;

        while ((!mask.inside(x, y, z)) || (dist < r0) || (dist > r1)) {
            x = Math.random() * (xmax - xmin) + xmin;
            y = Math.random() * (ymax - ymin) + ymin;
            z = Math.random() * (zmax - zmin) + zmin;
            dist = mask.distPixelBorderUnit(x, y, z);
        }
        //IJ.log("dist=" + dist);

        return new Point3D(x, y, z);
    }

    /**
     *
     * @param evaluationPoints
     * @return
     */
    public ArrayUtil computeDistances(Point3D[] evaluationPoints) {
        final int numPoints = evaluationPoints.length;
        ArrayUtil array;
        Point3D P;
        Object3D cl;
        array = new ArrayUtil(numPoints);
        for (int i = 0; i < numPoints; i++) {
            P = evaluationPoints[i];
            cl = this.closestCenter(P);
            array.putValue(i, cl.distPixelCenter(P.getX(), P.getY(), P.getZ()));
        }

        return array;
    }

    /**
     * All distances 2x2 from center to center
     *
     * @return an array[][] of distances
     */
    public double[][] distancesAllPairsCenter() {
        int s = objects.size();
        double res[][] = new double[s][s];
        Object3D obj1, obj2;
        double dist;

        for (int i = 0; i < s; i++) {
            obj1 = (Object3D) objects.get(i);
            res[i][i] = 0;
            for (int j = i + 1; j < s; j++) {
                obj2 = (Object3D) objects.get(j);
                dist = obj1.distCenterUnit(obj2);
                res[i][j] = dist;
                res[j][i] = dist;
            }
        }
        return res;
    }

    /**
     * All distances 2x2 from center to center
     *
     * @param pop other population of objects
     * @return an array[][] of distances
     */
    public double[][] distancesAllPairsCenter(Objects3DPopulation pop) {
        int s = objects.size();
        int ss = pop.getNbObjects();
        double res[][] = new double[s][s];
        Object3D obj1, obj2;
        double dist;

        for (int i = 0; i < s; i++) {
            obj1 = (Object3D) objects.get(i);
            res[i][i] = 0;
            for (int j = 0; j < ss; j++) {
                obj2 = pop.getObject(j);
                dist = obj1.distCenterUnit(obj2);
                res[i][j] = dist;
                res[j][i] = dist;
            }
        }
        return res;
    }

    /**
     * All distances 2x2 from center to center
     *
     * @param pop other population of objects
     * @return an array[][] of distances
     */
    public double[][] distancesAllPairsBorder(Objects3DPopulation pop) {
        int s = objects.size();
        int ss = pop.getNbObjects();
        double res[][] = new double[s][s];
        Object3D obj1, obj2;
        double dist;

        for (int i = 0; i < s; i++) {
            obj1 = (Object3D) objects.get(i);
            res[i][i] = 0;
            for (int j = 0; j < ss; j++) {
                obj2 = pop.getObject(j);
                dist = obj1.distBorderUnit(obj2);
                res[i][j] = dist;
                res[j][i] = dist;
            }
        }
        return res;
    }

    /**
     *
     * @return
     */
    public ArrayUtil distancesAllCenter() {
        double[] distances = new double[getNbObjects() * (getNbObjects() - 1) / 2];
        int nb = this.getNbObjects();
        Object3D ob1, ob2;
        int count = 0;
        for (int i = 0; i < nb; i++) {
            ob1 = objects.get(i);
            for (int j = i + 1; j < nb; j++) {
                ob2 = objects.get(j);
                distances[count] = ob1.distCenterUnit(ob2);
                count++;
            }
        }
        return new ArrayUtil(distances);
    }

    /**
     *
     * @return
     */
    public ArrayUtil distancesAllClosestCenter() {
        int nb = this.getNbObjects();
        ArrayUtil tab = new ArrayUtil(nb);
        Object3D cl;
        for (int i = 0; i < nb; i++) {
            cl = closestCenter(this.getObject(i), true);
            if (cl != null) {
                double d = cl.distCenterUnit(this.getObject(i));
                tab.putValue(i, d);
            }
        }
        return tab;
    }

    public ArrayList<double[]> getMeasuresGeometrical() {
        // geometrical mesure volume (pix and unit) and surface (pix and unit)
        ArrayList<double[]> al = new ArrayList<double[]>();
        for (Object3D ob : objects) {
            double[] mes = {ob.getValue(), ob.getVolumePixels(), ob.getVolumeUnit(), ob.getAreaPixels(), ob.getAreaUnit()};
            al.add(mes);
        }

        return al;
    }

    public ArrayList<double[]> getMeasuresStats(ImageHandler raw) {
        // geometrical mesure volume (pix and unit) and surface (pix and unit)
        ArrayList<double[]> al = new ArrayList<double[]>();
        for (Object3D ob : objects) {
            double[] mes = {ob.getMeanPixValue(raw), ob.getStDevPixValue(raw), ob.getPixMinValue(raw), ob.getPixMaxValue(raw), ob.getIntegratedDensity(raw)};
            al.add(mes);
        }

        return al;
    }

    public ArrayList<double[]> getMeasuresStats(ImageStack raw) {
        return getMeasuresStats(ImageHandler.wrap(raw));
    }

    /**
     * All distances 2x2 from border to border
     *
     * @return an array[][] of distances
     */
    public double[][] distancesAllPairsBorder() {
        int s = objects.size();
        double res[][] = new double[s][s];
        Object3D obj1, obj2;
        double dist;

        for (int i = 0; i < s; i++) {
            obj1 = (Object3D) objects.get(i);
            res[i][i] = 0;
            for (int j = i + 1; j < s; j++) {
                obj2 = (Object3D) objects.get(j);
                dist = obj1.distBorderUnit(obj2);
                res[i][j] = dist;
                res[j][i] = dist;
            }
        }
        return res;
    }

    /**
     * Histogram of all distances center to center
     *
     * @param step to create bins from min to max
     * @return double[][] with 0 values and 1 counts
     */
    public double[][] histogramDistancesCenter(double step) {
        int s = objects.size();
        double[][] dists = this.distancesAllPairsCenter();

        return histogramDistances(dists, step);
    }

    /**
     * Histogram of all distances border to border
     *
     * @param step to create bins from min to max
     * @return double[][] with 0 vlaues and 1 counts
     */
    public double[][] histogramDistancesBorder(double step) {
        int s = objects.size();
        double[][] dists = this.distancesAllPairsBorder();

        return histogramDistances(dists, step);
    }

    // function to compute histogram of distances
    private double[][] histogramDistances(double distances[][], double step) {
        int s = objects.size();
        double dmin = distances[0][1];
        double dmax = dmin;
        double d;

        for (int i = 0; i < s; i++) {
            for (int j = i + 1; j < s; j++) {
                d = distances[i][j];
                if (d > dmax) {
                    dmax = d;
                }
                if (d < dmin) {
                    dmin = d;
                }
            }
        }

        int nbins = (int) Math.ceil((dmax - dmin) / step);
        if (nbins < 1) {
            nbins = 1;
        }
        int idx;
        double[][] res = new double[2][nbins];
        for (int i = 0; i < nbins; i++) {
            res[0][i] = dmin + i * step;
            res[1][i] = 0;
        }

        for (int i = 0; i < s; i++) {
            for (int j = i + 1; j < s; j++) {
                d = distances[i][j];
                idx = (int) Math.floor((d - dmin) / step);
                res[1][idx]++;
            }
        }
        return res;
    }

    /**
     *
     * @param x
     * @param y
     * @param z
     * @param dist
     * @return
     */
    public Object3D closestCenter(double x, double y, double z, double dist) {
        Object3D res = null;
        double dmin = Double.MAX_VALUE;
        Object3D tmp;
        double d;

        for (Iterator e = objects.iterator(); e.hasNext();) {
            tmp = (Object3D) e.next();
            d = tmp.distPixelCenter(x, y, z);
            if ((d < dmin) && (d > dist)) {
                dmin = d;
                res = tmp;
            }
        }

        return res;
    }

    public Object3D closestBorder(double x, double y, double z, double dist) {
        Object3D res = null;
        double dmin = Double.MAX_VALUE;
        for (Object3D tmp : objects) {
            double d = tmp.distPixelBorderUnit(x, y, z);
            if ((d < dmin) && (d > dist)) {
                dmin = d;
                res = tmp;
            }
        }

        return res;
    }

    /**
     *
     * @param x
     * @param y
     * @param z
     * @param dist
     * @return
     */
    public Object3D closestCenter(Object3D ob, ArrayList<Object3D> exclude) {
        Object3D res = null;
        double dmin = Double.MAX_VALUE;
        Object3D tmp;
        double d;
        for (Iterator e = objects.iterator(); e.hasNext();) {
            tmp = (Object3D) e.next();
            if (!exclude.contains(tmp)) {
                d = ob.distCenterUnit(tmp);
                if (d < dmin) {
                    dmin = d;
                    res = tmp;
                }
            }
        }

        return res;
    }

    public Object3D closestBorder(Object3D ob, ArrayList<Object3D> exclude) {
        Object3D res = null;
        double dmin = Double.MAX_VALUE;
        Object3D tmp;
        double d;
        for (Iterator e = objects.iterator(); e.hasNext();) {
            tmp = (Object3D) e.next();
            if (!exclude.contains(tmp)) {
                d = ob.distBorderUnit(tmp);
                if (d < dmin) {
                    dmin = d;
                    res = tmp;
                }
            }
        }

        return res;
    }

    /**
     *
     * @param x
     * @param y
     * @param z
     * @return
     */
    public Object3D closestCenter(double x, double y, double z) {
        if (kdtree == null) {
            this.createKDTreeCenters();
        }
        double[] pos = {x, y, z};

        Item clo = (kdtree.getNearestNeighbor(pos, 1))[0];
        return (Object3D) clo.obj;
    }

    /**
     *
     * @param P
     * @return
     */
    public Object3D closestCenter(Point3D P) {
        return closestCenter(P.getX(), P.getY(), P.getZ());
    }

    /**
     * Get the closest object in the population from given object with distance
     * greater than given distance
     *
     * @param O
     * @param dist
     * @return
     */
    public Object3D closestCenter(Object3D O, double dist) {
        Point3D P = O.getCenterAsPoint();
        return closestCenter(P.getX(), P.getY(), P.getZ(), dist);
    }

    /**
     * Get the closest object in the population from given object with distance
     * greater than given distance, distance border to border between objects
     *
     * @param O
     * @param dist
     * @return
     */
    public Object3D closestBorder(Object3D O, double dist) {
        Iterator<Object3D> it;
        double distmin = Double.MAX_VALUE;
        Object3D res = null;
        for (it = objects.iterator(); it.hasNext();) {
            Object3D tmp = it.next();
            double d = O.distBorderUnit(tmp);
            if (d > dist) {
                if (d < distmin) {
                    distmin = d;
                    res = tmp;
                }
            }
        }
        return res;
    }

    /**
     * Get the closest object in the population from given object Center to
     * center distance
     *
     * @param obj
     * @param dist
     * @return
     */
    public Object3D closestCenter(Object3D obj, boolean excludeInputObject) {
        return kClosestCenter(obj, 1, excludeInputObject);
    }

    /**
     * Get the closest object in the population from given object Border to
     * border distance
     *
     * @param O
     * @param dist
     * @return
     */
    public Object3D closestBorder(Object3D O) {
        return closestBorder(O, 0);
    }

    public Object3D kClosestCenter(Object3D ob, int k, boolean excludeInputObject) {
//        if (k == 1) {
//            return this.closestCenter(ob);
//        }
//        int kk = 1;
//        ArrayList<Object3D> exclude = new ArrayList();
//        exclude.add(ob);
//        Object3D clo = this.closestCenter(ob, exclude);
//        exclude.add(clo);
//        while ((clo != null) && (kk < k)) {
//            clo = this.closestCenter(ob, exclude);
//            if (clo != null) {
//                exclude.add(clo);
//            }
//            kk++;
//        }
//        return clo;

        if (kdtree == null) {
            this.createKDTreeCenters();
        }
        // closest should be itself
        if (excludeInputObject) {
            k++;
        }
        Item clo = (kdtree.getNearestNeighbor(ob.getCenterAsArray(), k))[k - 1];
        return (Object3D) clo.obj;
    }

    public ArrayList<Object3D> getObjectsWithinDistanceCenter(Object3D ob, double dist) {
        ArrayList<Object3D> list = new ArrayList();
        // first method tet all distances
        // FIXME use kdtree
        for (int i = 0; i < objects.size(); i++) {
            double tmp = ob.distCenterUnit(objects.get(i));
            if (tmp <= dist) {
                list.add(objects.get(i));
            }
        }

        return list;
    }

    public ArrayList<Object3D> getObjectsWithinDistanceBorder(Object3D ob, double dist) {
        ArrayList<Object3D> list = new ArrayList();
        for (int i = 0; i < objects.size(); i++) {
            double tmp = ob.distBorderUnit(objects.get(i));
            if (tmp <= dist) {
                list.add(objects.get(i));
            }
        }

        return list;
    }

    public Object3D kClosestBorder(Object3D ob, int k) {
        ArrayList<Object3D> exclude = new ArrayList();
        exclude.add(ob);
        if (k == 1) {
            return this.closestBorder(ob, exclude);
        }
        int kk = 1;
        Object3D clo = this.closestBorder(ob, exclude);
        exclude.add(clo);
        while ((clo != null) && (kk < k)) {
            clo = this.closestBorder(ob, exclude);
            if (clo != null) {
                exclude.add(clo);
            }
            kk++;
        }
        return clo;
    }

    /**
     * Get the second closest object in the population from given object with
     * distance greater than given distance
     *
     * @param O
     * @param dist
     * @return
     */
    public Object3D secondClosestCenter(Object3D O, double dist) {
        Point3D P = O.getCenterAsPoint();
        Object3D OO = closestCenter(P.getX(), P.getY(), P.getZ(), dist);
        return closestCenter(P.getX(), P.getY(), P.getZ(), O.distCenterUnit(OO));
    }

    /**
     * Get the second closest object in the population from given object with
     * distance greater than given distance
     *
     * @param O
     * @param dist
     * @return
     */
    public Object3D secondClosestCenter(Object3D O, boolean ExcludeInputObject) {
        return kClosestCenter(O, 2, ExcludeInputObject);
    }

    /**
     *
     * @param x
     * @param y
     * @param z
     * @return
     */
    public Object3D closestBorder(double x, double y, double z) {
        Object3D res = null;
        double dmin = Double.MAX_VALUE;
        Object3D tmp;
        double d;

        for (Iterator e = objects.iterator(); e.hasNext();) {
            tmp = (Object3D) e.next();
            d = tmp.distPixelBorderUnit(x, y, z);
            if (d < dmin) {
                dmin = d;
                res = tmp;
            }
        }

        return res;
    }

    public boolean shuffle(double angle, Vector3D axis) {
        int si = this.getNbObjects();
        ArrayUtil idx = new ArrayUtil(si);
        for (int i = 0; i < si; i++) {
            idx.addValue(i, i);
        }
        idx.randomize();
        double dx = mask.getXmax() - mask.getXmin();
        double dy = mask.getYmax() - mask.getYmin();
        double dz = mask.getZmax() - mask.getZmin();
        int c = 0;
        // test rotate
        for (int i = 0; i < si; i++) {
            boolean ok = false;
            Object3D obj = this.getObject((int) idx.getValue(i));
            c = 0;
            while ((!ok) && (c < 1000)) {
                ok = true;
                c++;
                double x = Math.random() * dx + mask.getXmin();
                double y = Math.random() * dy + mask.getYmin();
                double z = Math.random() * dz + mask.getZmin();
                obj.setNewCenter(x, y, z + 1);
                if (!mask.includes(obj)) {
                    ok = false;
                }
                if (ok) {
                    for (Object3D o : objects) {
                        if (!o.equals(obj)) {
                            double pc = obj.pcColoc(o);
                            if (pc > 0) {
                                ok = false;
                                break;
                            }
                        }
                    }
                }
            }

            if (c == 1000) {
                break;
            } else if ((angle != 0) && (axis != null)) {
                ((Object3DVoxels) obj).rotate(axis, angle);
            }
        }

        if (c == 1000) {
            return false;
        } else {
            return true;
        }
    }

    int[] k_Means(int k) {
        int s = objects.size();
        int[] res = new int[s];
        for (int i = 0; i < s; i++) {
            res[i] = 0;
        }
        Point3D[] ck = new Point3D[k];
        double cx[] = new double[k];
        double cy[] = new double[k];
        double cz[] = new double[k];
        int[] nb = new int[k];

        //initialisation
        double xmin, xmax, ymin, ymax, zmin, zmax;
        xmin = ymin = zmin = Double.MAX_VALUE;
        xmax = ymax = zmax = -Double.MAX_VALUE;

        Object3D tmp;
        double d, dmin;

        for (Iterator e = objects.iterator(); e.hasNext();) {
            tmp = (Object3D) e.next();
            if (tmp.getCenterX() < xmin) {
                xmin = tmp.getCenterX();
            }
            if (tmp.getCenterY() < ymin) {
                ymin = tmp.getCenterY();
            }
            if (tmp.getCenterZ() < zmin) {
                zmin = tmp.getCenterZ();
            }
            if (tmp.getCenterX() > xmax) {
                xmax = tmp.getCenterX();
            }
            if (tmp.getCenterY() > ymax) {
                ymax = tmp.getCenterY();
            }
            if (tmp.getCenterZ() > zmax) {
                zmax = tmp.getCenterZ();
            }
        }
        double dx = (xmax - xmin) / (k - 1.0);
        double dy = (ymax - ymin) / (k - 1.0);
        double dz = (zmax - zmin) / (k - 1.0);

        for (int i = 0; i < k; i++) {
            ck[i] = new Point3D(xmin + dx * i, ymin + i * dy, zmin + i * dz);
        }

        // loop
        boolean loop = true;
        int idx;

        while (loop) {
            loop = false;
            for (int i = 0; i < s; i++) {
                tmp = (Object3D) objects.get(i);
                idx = 0;
                // for each object find the closest center
                dmin = tmp.distPixelCenter(ck[0].getX(), ck[0].getY(), ck[0].getZ());
                for (int j = 1; j < k; j++) {
                    d = tmp.distPixelCenter(ck[j].getX(), ck[j].getY(), ck[j].getZ());
                    if (d < dmin) {
                        dmin = d;
                        idx = j;
                    }
                }
                // any change in class
                if (idx != res[i]) {
                    loop = true;
                    res[i] = idx;
                }
            }
            // compute new centers
            for (int j = 0; j < k; j++) {
                cx[k] = 0;
                cy[k] = 0;
                cz[k] = 0;
                nb[k] = 0;
            }
            for (int i = 0; i < s; i++) {
                tmp = (Object3D) objects.get(i);
                idx = res[i];
                cx[idx] += tmp.getCenterX();
                cy[idx] += tmp.getCenterY();
                cz[idx] += tmp.getCenterZ();
                nb[idx]++;
            }
            for (int j = 0; j < k; j++) {
                ck[j] = new Point3D(cx[j] / (double) nb[j], cy[j] / (double) nb[j], cz[j] / (double) nb[j]);
            }
        }

        return res;

    }

    public ArrayList<double[]> getMeasureCentroid() {
        // geometrical mesure volume (pix and unit) and surface (pix and unit)
        ArrayList<double[]> al = new ArrayList<double[]>();
        for (Object3D ob : objects) {
            double[] mes = {ob.getCenterX(), ob.getCenterY(), ob.getCenterZ()};
            al.add(mes);
        }

        return al;
    }

    public ArrayList<double[]> getMeasuresShape() {
        ArrayList<double[]> al = new ArrayList<double[]>();
        for (Object3D ob : objects) {
            double[] mes = {ob.getValue(), ob.getCompactness(true), ob.getSphericity(true), ob.getMainElongation(), ob.getMedianElongation(), ob.getRatioEllipsoid()};
            al.add(mes);
        }

        return al;
    }

    public ArrayList<double[]> getMeasuresMesh() {
        // geometrical mesure volume (pix and unit) and surface (pix and unit)
        ArrayList<double[]> al = new ArrayList<double[]>();
        for (Object3D ob : objects) {
            Object3DSurface surf = new Object3DSurface(ob.computeMeshSurface(true), ob.getValue());
            surf.setCalibration(calibration);
            surf.setSmoothingFactor(0.1f);

            double[] mes = {surf.getValue(), surf.getSurfaceMesh(), surf.getSmoothSurfaceArea()};
            al.add(mes);
        }

        return al;
    }

    private void addImagePlus(ImagePlus plus) {
        addImage(plus);
    }
//    public ImageInt getLabelImage() {
//        return labelImage;
//    }
//
//    public void setLabelImage(ImageInt labelImage) {
//        this.labelImage = labelImage;
//    }

}
