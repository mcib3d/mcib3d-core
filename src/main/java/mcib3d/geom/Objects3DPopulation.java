/*
 * Class for population of 3D objects
 * especially for statistical analyses
 */
package mcib3d.geom;

/**
 * Copyright (C) Thomas Boudier
 * <p>
 * License: This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by the Free
 * Software Foundation; either version 3 of the License, or (at your option) any
 * later version.
 * <p>
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * <p>
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 */

import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.measure.Calibration;
import mcib3d.image3d.ImageHandler;
import mcib3d.image3d.ImageInt;
import mcib3d.image3d.ImageShort;
import mcib3d.utils.ArrayUtil;
import mcib3d.utils.KDTreeC;
import mcib3d.utils.KDTreeC.Item;
import mcib3d.utils.Logger.AbstractLog;

import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Random;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

/**
 * @author thomas
 */
public class Objects3DPopulation {

    private final ArrayList<Object3D> objects;
    //private ImageInt labelImage = null;
    AbstractLog log = null;
    private Object3D mask = null; // usually a object3dlabel
    // all objects should have same calibration !
    @Deprecated
    private Calibration calibration = null; // deprecated
    private double scaleXY = 1, scaleZ = 1;
    private String unit = "pix";
    private KDTreeC kdtree = null;
    // link between values and index
    private HashMap<Integer, Integer> hashValue = null;
    private HashMap<String, Integer> hashName = null;

    /**
     * Conctructor
     */
    public Objects3DPopulation() {
        objects = new ArrayList<Object3D>();
    }

    public Objects3DPopulation(Object3D[] objs) {
        objects = new ArrayList<Object3D>();
        this.addObjects(objs);
    }

    public Objects3DPopulation(ArrayList<Object3D> objs) {
        objects = new ArrayList<Object3D>();
        this.addObjects(objs);
    }

    @Deprecated
    public Objects3DPopulation(Object3D[] objs, Calibration cal) {
        objects = new ArrayList<Object3D>();
        if (cal != null) {
            calibration = cal;
        } else {
            calibration = new Calibration();
        }
        this.addObjects(objs);
    }

    @Deprecated
    public Objects3DPopulation(ImagePlus plus) {
        objects = new ArrayList<Object3D>();
        addImagePlus(plus);
    }

    public Objects3DPopulation(ImageInt plus) {
        objects = new ArrayList<Object3D>();
        addImage(plus, 0);
    }

    public Objects3DPopulation(ImageInt plus, int threshold) {
        objects = new ArrayList<Object3D>();
        addImage(plus, threshold);
    }

    public AbstractLog getLog() {
        return log;
    }

    public void setLog(AbstractLog log) {
        this.log = log;
    }

    public double getScaleXY() {
        return scaleXY;
    }

    public void setScaleXY(double scaleXY) {
        this.scaleXY = scaleXY;
    }

    public double getScaleZ() {
        return scaleZ;
    }

    public void setScaleZ(double scaleZ) {
        this.scaleZ = scaleZ;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    /**
     * @return
     */
    @Deprecated
    public Calibration getCalibration() {
        return calibration;
    }

    /**
     * @param cal
     */
    @Deprecated
    public void setCalibration(Calibration cal) {
        this.calibration = cal;
        this.scaleXY = cal.pixelWidth;
        this.scaleZ = cal.pixelDepth;
        this.unit = cal.getUnits();
        // recalibrate objects
        if ((objects != null) && (objects.size() > 0)) {
            for (Object3D obj : objects) {
                Object3D_IJUtils.setCalibration(obj, calibration);
            }
        }
    }

    public void setCalibration(double sxy, double sz, String u) {
        setScale(sxy, sz, u);
    }

    public void setScale(double sxy, double sz, String u) {
        scaleXY = sxy;
        scaleZ = sz;
        unit = u;
        if ((objects != null) && (objects.size() > 0)) {
            for (Object3D obj : objects) {
                obj.setResXY(sxy);
                obj.setResZ(sz);
                obj.setUnits(u);
            }
        }
    }




    // hardcore distance in unit

    /**
     * @param nb
     * @param hardcore
     */
    public boolean createRandomPopulation(int nb, double hardcore) {
        ArrayList<Voxel3D> voxlist;
        Voxel3D v;
        Object3D closest;
        double dist;
        Point3D P;

        // first point
        Object3DVoxels maskVox = mask.getObject3DVoxels();
        Random ra = new Random();
        P = maskVox.getRandomvoxel(ra);
        v = new Voxel3D(P.getX(), P.getY(), P.getZ(), 1);
        voxlist = new ArrayList<Voxel3D>(1);
        voxlist.add(v);
        Object3DVoxels ob = new Object3DVoxels(voxlist);
        ob.setCalibration(scaleXY, scaleZ, unit);
        //Object3D_IJUtils.setCalibration(ob, calibration);
        addObject(ob);
        for (int i = 1; i < nb; i++) {
            P = maskVox.getRandomvoxel(ra);
            closest = closestCenter(P);
            dist = closest.distPixelCenter(P.getX(), P.getY(), P.getZ());
            // TODO should have exit conditions
            int count = 0;
            int maxCount = 1000;
            while ((dist <= hardcore) && (count < maxCount)) {
                P = getRandomPointInMask();
                if (P != null) {
                    closest = closestCenter(P);
                    dist = closest.distPixelCenter(P.getX(), P.getY(), P.getZ());
                }
                count++;
                //IJ.showStatus("***");
            }
            if (count == maxCount) {
                IJ.log("Cannot generate random spots");
                if (hardcore > 0) IJ.log("Maybe hard core distance " + hardcore + "(pixels) is too large.");
                return false;
            }
            v = new Voxel3D(P.getX(), P.getY(), P.getZ(), (float) (i + 1));
            voxlist = new ArrayList<Voxel3D>(1);
            voxlist.add(v);
            ob = new Object3DVoxels(voxlist);
            //Object3D_IJUtils.setCalibration(ob, calibration);
            ob.setCalibration(scaleXY, scaleZ, unit);
            addObject(ob);
        }

        return true;
    }

    /**
     * @param nb
     * @param r0
     * @param r1
     */
    public void createRandomPopulationDistAbsMb(int nb, double r0, double r1) {
        ArrayList<Voxel3D> voxlist;
        Voxel3D v;
        Point3D P;
        for (int i = 0; i < nb; i++) {
            voxlist = new ArrayList<Voxel3D>(1);
            P = getRandomPointInMaskDistAbsMb(r0, r1);
            v = new Voxel3D(P.getX(), P.getY(), P.getZ(), (float) i);
            voxlist.add(v);
            Object3DVoxels ob = new Object3DVoxels(voxlist);
            addObject(ob);
        }
    }

    public void createKDTreeCenters() {
        kdtree = new KDTreeC(3, 64);
        double[] tmp = {scaleXY, scaleXY, scaleZ};
        kdtree.setScale(tmp);
        for (int i = 0; i < this.getNbObjects(); i++) {
            kdtree.add(this.getObject(i).getCenterAsArray(), this.getObject(i));
        }
    }

    /**
     * @param ima
     * @param col
     */
    public void draw(ImageStack ima, int col) {
        Object3D ob;
        for (Object3D object : objects) {
            ob = object;
            Object3D_IJUtils.draw(ob, ima, col);
            //ob.draw(ima, col);
        }
    }

    public void draw(ImageHandler ima, int col) {
        Object3D ob;
        for (Object3D object : objects) {
            ob = object;
            ob.draw(ima, col);
        }
    }

    public void draw(ImageStack ima) {
        Object3D ob;
        for (Object3D object : objects) {
            ob = object;
            ob.draw(ima, ob.getValue());
        }
    }

    public void draw(ImageHandler ima) {
        Object3D ob;
        for (Object3D object : objects) {
            ob = object;
            ob.draw(ima, ob.getValue());
        }
    }

    /**
     * Add an object to the population
     *
     * @param obj the 3D object to add
     */
    public void addObject(Object3D obj) {
        //Object3D_IJUtils.setCalibration(obj, calibration);
        // first object ? will set calibration
        if (getNbObjects() == 0) {
            scaleXY = obj.resXY;
            scaleZ = obj.resZ;
            unit = obj.getUnits();
        }
        // check if calibration consistent
        else {
            if ((scaleXY != obj.resXY) || (scaleZ != obj.resZ)) {
                if (log != null)
                    log.log("Calibration not consistent between population and object : (" + scaleXY + "," + scaleZ + ") (" + obj.resXY + "," + obj.resZ + ")");
                obj.setCalibration(scaleXY, scaleZ, unit);
            }
        }
        objects.add(obj);
        // update kdtree if available
        kdtree = null;
        // need to update hash tables
        hashValue = null;
        hashName = null;
    }

    public final void addObjects(Object3D[] objs) {
        for (Object3D obj : objs) {
            addObject(obj);
        }
        // update kdtree if available
        kdtree = null;
        // need to update hash tables
        hashValue = null;
        hashName = null;
    }

    public void addObjects(ArrayList<Object3D> list) {
        for (Object3D list1 : list) {
            //objs[i].setCalibration(calibration);
            addObject(list1);
        }
        // update kdtree if available
        kdtree = null;
        // need to update hash tables
        hashValue = null;
        hashName = null;
    }

    public void removeObjectsTouchingBorders(ImageHandler img, boolean Z) {
        ArrayList<Object3D> toRemove = new ArrayList<Object3D>();
        for (Object3D obj : objects) {
            if (obj.touchBorders(img, Z)) {
                toRemove.add(obj);
            }
        }
        for (Object3D obj : toRemove) {
            removeObject(obj);
            //IJ.log("removing touching " + obj);
        }
        // update kdtree if available
        kdtree = null;
        // need to update hash tables
        hashValue = null;
        hashName = null;
    }

    public void removeObjectsTouchingBorders(ImagePlus img, boolean Z) {
        ArrayList<Object3D> toRemove = new ArrayList<Object3D>();
        for (Object3D obj : objects) {
            if (obj.touchBorders(img, Z)) {
                toRemove.add(obj);
            }
        }
        for (Object3D obj : toRemove) {
            removeObject(obj);
            //IJ.log("removing touching " + obj);
        }
        // update kdtree if available
        kdtree = null;
        // need to update hash tables
        hashValue = null;
        hashName = null;
    }

    public void removeObject(int i) {
        // update kdtree if available
        kdtree = null;
        // need to update hash tables
        hashValue = null;
        hashName = null;
        objects.remove(i);
    }

    public void removeObject(Object3D obj) {
        // update kdtree if available
        kdtree = null;
        // need to update hash tables
        hashValue = null;
        hashName = null;
        if (!objects.remove(obj)) {
            if (log != null) log.log("Pb removing " + obj);
        }
    }

    @Deprecated
    private void buildHash() {
        // need to update hash tables
        hashName = new HashMap<String, Integer>(getNbObjects());
        hashValue = new HashMap<Integer, Integer>(getNbObjects());

        for (int i = 0; i < getNbObjects(); i++) {
            Object3D O = getObject(i);
            hashName.put(O.getName(), i);
            hashValue.put(O.getValue(), i);
        }
    }

    public void updateNamesAndValues() {
        hashName = new HashMap<String, Integer>(getNbObjects());
        hashValue = new HashMap<Integer, Integer>(getNbObjects());

        for (int i = 0; i < getNbObjects(); i++) {
            Object3D O = getObject(i);
            hashName.put(O.getName(), i);
            hashValue.put(O.getValue(), i);
        }
    }

    public void addPoints(Point3D[] points) {
        int inc = objects.size();
        for (int i = 0; i < points.length; i++) {
            Point3D P = points[i];
            Voxel3D v = new Voxel3D(P.getX(), P.getY(), P.getZ(), (float) i + inc);
            ArrayList<Voxel3D> voxlist = new ArrayList<Voxel3D>(1);
            voxlist.add(v);
            Object3DVoxels ob = new Object3DVoxels(voxlist);
            //Object3D_IJUtils.setCalibration(ob, calibration);
            ob.setCalibration(scaleXY, scaleZ, unit);
            ob.setName("Point-" + i);
            ob.setValue(i + 1);
            addObject(ob);
        }
        // update kdtree if available
        kdtree = null;
        // need to update hash tables
        hashValue = null;
        hashName = null;

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
     * @param seg
     * @param threshold
     * @param cali
     */
    @Deprecated
    public void addImage(ImageInt seg, int threshold, Calibration cali) {
        seg.resetStats(null);
        int min = (int) seg.getMinAboveValue(threshold);
        int max = (int) seg.getMax();
        if (max == 0) {
            if (log != null) log.log("No objects found");
            return;
        }
        //IJ.log("mm "+min+" "+max);
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
        int c = 1;
        for (int i = 0; i < max - min + 1; i++) {
            if (!objectstmp[i].isEmpty()) {
                Object3DVoxels ob = new Object3DVoxels(objectstmp[i]);
                //ob.setLabelImage(null);// the image can be closed anytime
                Object3D_IJUtils.setCalibration(ob, calibration);
                ob.setName("Obj" + c);
                addObject(ob);
                c++;
            }
        }
        // update kdtree if available
        kdtree = null;
        // need to update hash tables
        hashValue = null;
        hashName = null;
    }

    public void addImage(ImageInt seg, int threshold) {
        seg.resetStats(null);
        int min = (int) seg.getMinAboveValue(threshold);
        int max = (int) seg.getMax();
        if (max == 0) {
            if (log != null) log.log("No objects found");
            return;
        }
        //IJ.log("mm "+min+" "+max);
        // iterate in image  and constructs objects
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
        int c = 1;
        for (int i = 0; i < max - min + 1; i++) {
            if (!objectstmp[i].isEmpty()) {
                Object3DVoxels ob = new Object3DVoxels(objectstmp[i]);
                //ob.setLabelImage(null);// the image can be closed anytime
                //Object3D_IJUtils.setCalibration(ob, calibration);
                ob.setCalibration(seg.getScaleXY(), seg.getScaleZ(), seg.getUnit());
                ob.setName("Obj-" + c + "-" + ob.getValue());
                addObject(ob);
                c++;
            }
        }
        // update kdtree if available
        kdtree = null;
        // need to update hash tables
        hashValue = null;
        hashName = null;
    }

    @Deprecated
    public void addImage(ImageInt seg, Calibration cali) {
        addImage(seg, 0, cali);
    }

    /**
     * @param plus
     */
    @Deprecated
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
     * @return
     */
    public Object3D getMask() {
        return mask;
    }

    /**
     * @param mask
     */
    public void setMask(Object3D mask) {
        this.mask = mask;
        mask.init();
    }

    /**
     * @param i
     * @return
     */
    public Object3D getObject(int i) {
        return objects.get(i);
    }

    public void setObject(int i, Object3D obj) {
        if (getNbObjects() == 0) {
            scaleXY = obj.resXY;
            scaleZ = obj.resZ;
            unit = obj.getUnits();
        }
        // check if calibration consistent
        else {
            if ((scaleXY != obj.resXY) || (scaleZ != obj.resZ)) {
                if (log != null)
                    log.log("Calibration not consistent between population and object : (" + scaleXY + "," + scaleZ + ") (" + obj.resXY + "," + obj.resZ + ")");
                obj.setCalibration(scaleXY, scaleZ, unit);
            }
        }
        objects.set(i, obj);
        // update kdtree if available
        kdtree = null;
        // need to update hash tables
        hashValue = null;
        hashName = null;
    }

    public Object3D getObjectByValue(int val) {
        if (hashValue == null) {
            updateNamesAndValues();
        }
        Integer idxI = hashValue.get(val);
        if (idxI == null) {
            return null;
        }
        return objects.get(idxI);
    }

    public Object3D getObjectByName(String name) {
        if (hashName == null) {
            updateNamesAndValues();
        }
        Integer nb = hashName.get(name);
        if (nb == null) {
            return null;
        }
        return objects.get(nb);
    }

    public int getIndexFromName(String name) {
        if (hashName == null) {
            updateNamesAndValues();
        }
        return hashName.get(name);
    }

    public int getIndexFromValue(int val) {
        if (hashValue == null) {
            updateNamesAndValues();
        }
        return hashValue.get(val);
    }

    public int getIndexOf(Object3D ob) {
        return objects.indexOf(ob);
    }

    public ArrayUtil getAllIndices(){
        ArrayUtil arrayUtil = new ArrayUtil(getNbObjects());
        for(int i=0;i<getNbObjects();i++){
            arrayUtil.putValue(i,getObject(i).getValue());
        }

        return arrayUtil;
    }

    public Object3D[] getObjectsArray() {
        return (Object3D[]) objects.toArray();
    }

    public ArrayList<Object3D> getObjectsList() {
        return objects;
    }

    /**
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

    public ImageInt drawPopulation(){
        int[] sizes=this.getMaxSizeAllObjects();
        ImageInt drawImage=new ImageShort("population", sizes[0],sizes[1],sizes[2]);
        for(Object3D object3DVoxels:getObjectsList()){
            object3DVoxels.draw(drawImage);
        }
        return drawImage;
    }


    /**
     * @return
     */
    public Point3D getRandomPointInMask() {
        int xmin = mask.getXmin();
        int xmax = mask.getXmax();
        int ymin = mask.getYmin();
        int ymax = mask.getYmax();
        int zmin = mask.getZmin();
        int zmax = mask.getZmax();

        // FIXME NO CALIBRATION   !!! ???
        double x = (Math.random() * (xmax - xmin) + xmin);
        double y = (Math.random() * (ymax - ymin) + ymin);
        double z = (Math.random() * (zmax - zmin) + zmin);

        // add counter
        int count = 0;
        int maxCount = 1000;
        while ((!mask.inside(x, y, z)) && (count < maxCount)) {
            x = Math.random() * (xmax - xmin) + xmin;
            y = Math.random() * (ymax - ymin) + ymin;
            z = Math.random() * (zmax - zmin) + zmin;
            count++;
        }
        if (count == maxCount) return null;

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
            obj1 = objects.get(i);
            res[i][i] = 0;
            for (int j = i + 1; j < s; j++) {
                obj2 = objects.get(j);
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
            obj1 = objects.get(i);
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
            obj1 = objects.get(i);
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
     * @return
     */
    public ArrayUtil distancesAllBorder() {
        double[] distances = new double[getNbObjects() * (getNbObjects() - 1) / 2];
        int nb = this.getNbObjects();
        Object3D ob1, ob2;
        int count = 0;
        for (int i = 0; i < nb; i++) {
            ob1 = objects.get(i);
            for (int j = i + 1; j < nb; j++) {
                ob2 = objects.get(j);
                distances[count] = ob1.distBorderUnit(ob2);
                count++;
            }
        }
        return new ArrayUtil(distances);
    }


    /**
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

    public ArrayUtil distancesAllClosestBorder() {
        int nb = this.getNbObjects();
        ArrayUtil tab = new ArrayUtil(nb);
        Object3D cl;
        for (int i = 0; i < nb; i++) {
            cl = closestBorder(this.getObject(i));
            if (cl != null) {
                double d = cl.distBorderUnit(this.getObject(i));
                tab.putValue(i, d);
            }
        }
        return tab;
    }


    public ArrayList<double[]> getMeasuresGeometrical() {
        // geometrical measure volume (pix and unit) and surface (pix and unit)
        ArrayList<double[]> al = new ArrayList<double[]>();
        for (Object3D ob : objects) {
            double[] mes = {ob.getValue(), ob.getVolumePixels(), ob.getVolumeUnit(), ob.getAreaPixels(), ob.getAreaUnit()};
            al.add(mes);
        }

        return al;
    }

    public ArrayList<double[]> getMeasuresStats(ImageHandler raw) {
        // geometrical measure volume (pix and unit) and surface (pix and unit)
        ArrayList<double[]> al = new ArrayList<double[]>();
        for (Object3D ob : objects) {
            double[] mes = {ob.getValue(), ob.getPixMeanValue(raw), ob.getPixStdDevValue(raw), ob.getPixMinValue(raw), ob.getPixMaxValue(raw), ob.getIntegratedDensity(raw)};
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
            obj1 = objects.get(i);
            res[i][i] = 0;
            for (int j = i + 1; j < s; j++) {
                obj2 = objects.get(j);
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
        //int s = objects.size();
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
        // int s = objects.size();
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

        for (Object3D object : objects) {
            tmp = object;
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
     * @return
     */
    public Object3D closestCenter(Object3D ob, ArrayList<Object3D> exclude) {
        Object3D res = null;
        double dmin = Double.MAX_VALUE;
        Object3D tmp;
        double d;
        for (Object3D object : objects) {
            tmp = object;
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
        double d;
        for (Object3D tmp : objects) {
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
     * @param x x coordinate in pixel
     * @param y y coordinate in pixel
     * @param z z coordinate in pixel
     * @return closest object (using calibrated distance)
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
        //Iterator<Object3D> it;
        double distanceMinimum = Double.MAX_VALUE;
        Object3D res = null;
        for (Object3D object3D : objects) {
            double d = O.distBorderUnit(object3D);
            if (d > dist) {
                if (d < distanceMinimum) {
                    distanceMinimum = d;
                    res = object3D;
                }
            }
        }

        return res;
    }

    public Object3D closestBorder(Object3D O, int[] allowed, double dist) {
        double distanceMinimum = Double.MAX_VALUE;
        Object3D res = null;
        if (allowed.length == 0) {
            return null;
        }
        for (int ob : allowed) {
            Object3D tmp = getObject(ob);
            double d = O.distBorderUnit(tmp);
            if (d > dist) {
                if (d < distanceMinimum) {
                    distanceMinimum = d;
                    res = tmp;
                }
            }
        }
        return res;
    }

    public Object3D closestCenter(Object3D O, int[] allowed, double dist) {
        double distanceMinimum = Double.MAX_VALUE;
        Object3D res = null;
        if (allowed.length == 0) {
            return null;
        }
        //for (it = objects.iterator(); it.hasNext();) {
        for (int ob : allowed) {
            Object3D tmp = getObject(ob);
            double d = O.distCenterUnit(tmp);
            if (d > dist) {
                if (d < distanceMinimum) {
                    distanceMinimum = d;
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
     * @return
     */
    public Object3D closestCenter(Object3D obj, boolean excludeInputObject) {
        return kClosestCenter(obj, 1, excludeInputObject);
    }

    public Object3D closestCenter(Object3D obj, int[] allowed, boolean excludeInputObject) {
        return closestCenter(obj, allowed, 0);
    }

    /**
     * Get the closest object in the population from given object Border to
     * border distance
     *
     * @param O
     * @return
     */
    public Object3D closestBorder(Object3D O) {
        return closestBorder(O, 0);
    }

    public Object3D closestBorder(Object3D O, int[] allowed) {
        return closestBorder(O, allowed, 0);
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
        // closest should be itself if in same population
        if (excludeInputObject) {
            k++;
        }
        Item clo = (kdtree.getNearestNeighbor(ob.getCenterAsArray(), k))[k - 1];
        return (Object3D) clo.obj;
    }

    // kth closest (k=1 to N), with exclude list, if object in same population,
    // the object should be in the exclude list
    public Object3D kClosestCenter(Object3D ob, int k, ArrayList<Object3D> exclude) {
        if (kdtree == null) {
            this.createKDTreeCenters();
        }

        int nbClosest = 0;
        Item kClosest = null;
        Item[] items = kdtree.getNearestNeighbor(ob.getCenterAsArray(), getNbObjects());
        for (Item item : items) {
            if (!exclude.contains(item)) {
                nbClosest++;
                kClosest = item;
            }
            if (nbClosest == k) break;
        }

        return (Object3D) kClosest.obj;
    }


    public ArrayList<Object3D> getObjectsWithinDistanceCenter(Object3D ob, double dist) {
        ArrayList<Object3D> list = new ArrayList<Object3D>();
        // first method test all distances
        // FIXME use kdtree
        for (Object3D object : objects) {
            double tmp = ob.distCenterUnit(object);
            if (tmp <= dist) {
                list.add(object);
            }
        }

        return list;
    }


    public ArrayList<Object3D> getObjectsWithinDistanceBorder(Object3D ob, double dist) {
        ArrayList<Object3D> list = new ArrayList<Object3D>();
        for (Object3D object : objects) {
            double tmp = ob.distBorderUnit(object);
            if (tmp <= dist) {
                list.add(object);
            }
        }

        return list;
    }

    public ArrayList<Object3D> getObjectsWithinVolume(double volumeMin, double volumeMax, boolean useUnit) {
        if (volumeMax < volumeMin) volumeMax = Double.POSITIVE_INFINITY;
        ArrayList<Object3D> list = new ArrayList<Object3D>();
        for (Object3D object : objects) {
            double vol = useUnit ? object.getVolumeUnit() : object.getVolumePixels();
            if ((vol >= volumeMin) && (vol <= volumeMax))
                list.add(object);
        }

        return list;
    }

    public Object3D kClosestBorder(Object3D ob, int k) {
        ArrayList<Object3D> exclude = new ArrayList<Object3D>();
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
     * @return
     */
    public Object3D secondClosestCenter(Object3D O, boolean ExcludeInputObject) {
        return kClosestCenter(O, 2, ExcludeInputObject);
    }

    // 2th closest, with exclude list, if object in same population,
    // the object should be in the exclude list
    public Object3D secondClosestCenter(Object3D ob, ArrayList<Object3D> exclude) {
        return kClosestCenter(ob, 2, exclude);
    }


    /**
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

        for (Object3D object : objects) {
            tmp = object;
            d = tmp.distPixelBorderUnit(x, y, z);
            if (d < dmin) {
                dmin = d;
                res = tmp;
            }
        }

        return res;
    }

    public ArrayList<Object3D> shuffle() {
        ArrayList<Object3D> shuObj = new ArrayList<Object3D>();
        Random ra = new Random();
        ImageInt maskImage = mask.getMaxLabelImage(1);
        Object3DVoxels maskVox = mask.getObject3DVoxels();

        // shuffle indices
        ArrayUtil shuffleIndex = new ArrayUtil(getNbObjects());
        shuffleIndex.fillRange(0, getNbObjects(), 1);
        shuffleIndex.shuffle();

        for (int i = 0; i < getNbObjects(); i++) {
            Object3DVoxels obj = (Object3DVoxels) getObject(shuffleIndex.getValueInt(i));
            Point3D center = obj.getCenterAsPoint();
            boolean ok = false;
            int it = 0;
            int maxIt = 1000000;
            while (!ok) {
                //log.log("Shuffling " + getObject3D(i).getValue());
                Voxel3D vox = maskVox.getRandomvoxel(ra);
                obj.setNewCenter(vox.getX(), vox.getY(), vox.getZ());
                ok = true;
                it++;
                obj.resetQuantifImage();
                if (maskVox.includesBox(obj)) {
                    if (obj.getPixMinValue(maskImage) < 1) {
                        ok = false;
                    }
                } else {
                    ok = false;
                }
                if (it >= maxIt) {
                    ok = true;
                }
            }
            if (it == maxIt) {
                if (log != null) log.log("Could not shuffle " + obj);
                obj.setNewCenter(center.x, center.y, center.z);
            }
            shuObj.add(obj);
            // update mask
            obj.draw(maskImage, 0);
        }
        return shuObj;
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

        for (Object3D object : objects) {
            tmp = object;
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
                tmp = objects.get(i);
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
                tmp = objects.get(i);
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

    @Deprecated
    public ArrayList<double[]> getMeasuresMesh() {
        // geometrical measure volume (pix and unit) and surface (pix and unit)
        ArrayList<double[]> al = new ArrayList<double[]>();
        for (Object3D ob : objects) {
            Object3DSurface surf = new Object3DSurface(ob.computeMeshSurface(true), ob.getValue());
            Object3D_IJUtils.setCalibration(surf, calibration);
            surf.setSmoothingFactor(0.1f);

            double[] mes = {surf.getValue(), surf.getSurfaceMesh(), surf.getSmoothSurfaceArea()};
            al.add(mes);
        }

        return al;
    }

    @Deprecated
    private void addImagePlus(ImagePlus plus) {
        addImage(plus);
    }

    public boolean saveObjects(String path) {
        int[] indexes = new int[getNbObjects()];
        for (int i = 0; i < indexes.length; i++) {
            indexes[i] = i;
        }
        return saveObjects(path, indexes);
    }

    public boolean saveObjects(String path, int[] indexes) {
        Object3D obj;
        String name;
        File f = new File(path);
        String dir = f.getParent();
        String fs = File.separator;

        for (int i : indexes) {
            obj = this.getObject(i);
            obj.saveObject(dir + fs);
        }
        byte[] buf = new byte[1024];
        ZipOutputStream zip;
        FileInputStream in;
        File file;
        int len;
        try {
            //  ZIP           
            zip = new ZipOutputStream(new FileOutputStream(path));
            for (int i : indexes) {
                name = this.getObject(i).getName();
                file = new File(dir + fs + name + ".3droi");
                in = new FileInputStream(file);
                zip.putNextEntry(new ZipEntry(name + ".3droi"));
                while ((len = in.read(buf)) > 0) {
                    zip.write(buf, 0, len);
                }
                zip.closeEntry();
                in.close();
                file.delete();
            }
            zip.close();

        } catch (IOException ex) {
            if (log != null) log.log("Pb saving population " + ex);
            return false;
        }

        return true;
    }

    public void sortPopulation() {
        // sort object3D based on field compare
        Collections.sort(objects);
        // update kdtree if available
        kdtree = null;
        // need to update hash tables
        hashValue = null;
        hashName = null;
    }

    public void loadObjects(String path) {
        //ImagePlus plus = this.getImage();
        ZipInputStream zipinputstream;
        ZipEntry zipentry;
        FileOutputStream fileoutputstream;
        int n;
        byte[] buf = new byte[1024];
        File file;
        Object3DVoxels obj;
        if (log != null) log.log("Loading objects from " + path);
        File f = new File(path);
        String dir = f.getParent();
        String fs = File.separator;
        try {
            zipinputstream = new ZipInputStream(new FileInputStream(path));
            zipentry = zipinputstream.getNextEntry();
            while (zipentry != null) {
                //for each entry to be extracted
                String entryName = zipentry.getName();
                //IJ.log("entryname=" + entryName);
                fileoutputstream = new FileOutputStream(dir + fs + entryName);
                file = new File(dir + fs + entryName);
                while ((n = zipinputstream.read(buf, 0, 1024)) > -1) {
                    fileoutputstream.write(buf, 0, n);
                }
                fileoutputstream.close();
                zipinputstream.closeEntry();
                zipentry = zipinputstream.getNextEntry();
                // create object
                obj = new Object3DVoxels();
                obj.setValue(1);
                obj.loadObject(dir + fs, entryName);
                obj.setName(entryName.substring(0, entryName.length() - 6));

                //Calibration cal = new Calibration();
                //cal.pixelWidth = obj.getResXY();
                //cal.pixelHeight = obj.getResXY();
                //cal.pixelDepth = obj.getResZ();
                //cal.setUnit(obj.getUnits());

                addObject(obj);

                file.delete();
            }
            zipinputstream.close();
        } catch (FileNotFoundException ex) {
            if (log != null) log.log("Pb loading " + ex);
        } catch (IOException e) {
            if (log != null) log.log("Pb loading " + e);
        }
    }

    //    public ImageInt getLabelImage() {
//        return labelImage;
//    }
//
//    public void setLabelImage(ImageInt labelImage) {
//        this.labelImage = labelImage;
//    }
}
