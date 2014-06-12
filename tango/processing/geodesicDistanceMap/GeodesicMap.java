package tango.processing.geodesicDistanceMap;

import ij.ImagePlus;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.TreeSet;
import mcib3d.geom.*;
import mcib3d.image3d.ImageFloat;
import mcib3d.image3d.ImageHandler;
import mcib3d.image3d.ImageInt;
import mcib3d.image3d.ImageShort;
import tango.dataStructure.InputImages;
import tango.parameter.*;
/**
 *
 **
 * /**
 * Copyright (C) 2012 Jean Ollion
 *
 *
 *
 * This file is part of tango
 *
 * tango is free software; you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 3 of the License, or (at your option) any later
 * version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 *
 * @author Jean Ollion
 */
public class GeodesicMap {
    ImageFloat distanceMap, templateDistanceMap;
    ImageShort labelMap, templateLabelMap;
    ImageFloat intensityMap;
    ImageInt mask;
    float resXY2, resZ2, diag, diagZ, diagZ2, dist, distZ;
    int sizeX, sizeY, sizeZ, limX, limY, limZ;
    TreeSet<Voxel> heap;
    boolean invert = true;
    boolean normalize=true;
    PreFilterSequenceParameter filters = new PreFilterSequenceParameter("Filters for intensity map:", "filters");
    BooleanParameter filtered = new BooleanParameter("Use filtered intensity map:", "filtered", false);
    BooleanParameter normalize_P = new BooleanParameter("Normalize intensity map:", "normalize", normalize);
    BooleanParameter invert_P = new BooleanParameter("Invert intensity map:", "invert", invert);
    StructureParameter intensity_P = new StructureParameter("Intensity map:", "intensity", -1, false);
    Parameter[] parameters = new Parameter[] {intensity_P, filtered, filters, normalize_P, invert_P};
    boolean firstRun=false;
    HashMap<Short, ArrayList<SimpleVoxel>> labels;
    boolean verbose;
    int nCPUs=1;
    public GeodesicMap() {
        intensity_P.setHelp("The raw channelFile from which intensity level will be used to define the geodesci distance", true);
        filtered.setHelp("Use image resulting from pre-filtering step of the processing sequence associated to intensity map structure (if existing)", true);
        filters.setHelp("Filters applied to (pre-filtered) intensity map", false);
        invert_P.setHelp("Choose invert for low distance cost associated with high intensities", true);
        
        //call init(ImagePlus[] raw, ImagePlus[] seg) after this constructor;
    }
    
    public StructureParameter getStructure() {
        return intensity_P;
    }
    
    public GeodesicMap(ImageInt mask, ImageFloat intensityMap, int nCPUs, boolean verbose) {
        this.mask=mask;
        if (intensityMap!=null) {
            this.intensityMap=intensityMap;
        } else {
            this.intensityMap = new ImageFloat("intensityMap", mask.sizeX, mask.sizeY, mask.sizeZ);
            this.intensityMap.fill(1);
        }
        this.verbose=verbose;
        this.nCPUs=nCPUs;
        init();
    }
    
    
    public void init(InputImages raw, int nCPUs, boolean verbose) {
        this.mask=raw.getMask();
        this.invert=invert_P.isSelected();
        this.normalize=normalize_P.isSelected();
        this.verbose=verbose;
        this.nCPUs=nCPUs;
        init();
        computeIntensityMap(raw);
    }
   
    
    public ImageShort getLabelMap() {
        //if (templateLabelMap!=null) return templateLabelMap;
        //else 
        return labelMap;
    }
    
    public ImageFloat getDistanceMap() {
        //if (templateDistanceMap!=null) return templateDistanceMap;
        //else 
        return distanceMap;
    }
    
    public ImageFloat getIntensityMap() {
        return intensityMap;
    }
    
    protected void init() {
        resXY2=(float)mask.getScaleXY();
        dist=resXY2/2f;
        resXY2=resXY2*resXY2;
        resZ2=(float)mask.getScaleZ();
        distZ=resZ2/2f;
        resZ2=resZ2*resZ2;
        diag=(float)Math.sqrt(2*resXY2)/8f;
        diagZ=(float)Math.sqrt(resXY2+resZ2)/8f;
        diagZ2=(float)Math.sqrt(resXY2*2+resZ2)/16f;
        sizeX=mask.sizeX;
        sizeY=mask.sizeY;
        sizeZ=mask.sizeZ;
        limX=sizeX-1;
        limY=sizeY-1;
        limZ=sizeZ-1;
    }
    
    public Parameter[] getParameters() {
        return parameters;
    }
    
    protected ImageHandler getIntensity(InputImages raw) {
        return intensity_P.getImage(raw, filtered.isSelected());
    }
    
    protected void computeIntensityMap(InputImages raw) {
        ImageHandler intensity = getIntensity(raw);
        intensity = filters.runPreFilterSequence(intensity_P.getIndex(), intensity, raw, nCPUs, false);
        intensityMap=new ImageFloat(intensity);
        if (normalize) intensityMap=intensityMap.normalize(mask, 0);
        if (invert) intensityMap.invert(mask);
    }
    
    protected void initDistanceMap() {
        if (distanceMap==null) distanceMap=new ImageFloat("gdm", sizeX, sizeY, sizeZ);
        for (int xy = 0; xy<distanceMap.sizeXY; xy++) {
            distanceMap.pixels[0][xy]=Float.MAX_VALUE;
        }
        for (int z=1; z<distanceMap.sizeZ; z++) System.arraycopy(distanceMap.pixels[0], 0, distanceMap.pixels[z], 0, distanceMap.sizeXY);
    }
    
    protected void plantSeed(int x, float dx, int y, float dy, int z, float dz, short label) {
        if (dx==0 && dy==0 && dz==0) heap.add(new Voxel(x+y*sizeX, z,  0f, label));
        else {
            double dx2=dx*dx*resXY2;
            double dy2=dy*dy*resXY2;
            double dz2=dz*dz*resZ2;
            double dxx2=(1-dx)*(1-dx)*resXY2;
            double dyy2=(1-dy)*(1-dy)*resXY2;
            double dzz2=(1-dz)*(1-dz)*resXY2;
            int xy = x+y*sizeX;
            float value = intensityMap.getPixel(x+dx, y+dy, z+dz, mask);
            double dist;
            dist =  Math.sqrt(dx2+dy2+dz2);
            Voxel v = new Voxel(xy, z, (float)dist*(value+intensityMap.pixels[z][xy])/2, label);
            heap.add(v);
            distanceMap.pixels[v.z][v.xy]=v.value;
            labelMap.pixels[v.z][v.xy]=label;
            if (dx>0) {
                dist =  Math.sqrt(dxx2+dy2+dz2);
                v=new Voxel(xy+1, z, (float)dist*(value+intensityMap.pixels[z][xy+1])/2, label);
                heap.add(v);
                distanceMap.pixels[v.z][v.xy]=v.value;
                labelMap.pixels[v.z][v.xy]=label;
                if (dy>0) {
                    dist =  Math.sqrt(dxx2+dyy2+dz2);
                    v=new Voxel(xy+1+sizeX, z, (float)dist*(value+intensityMap.pixels[z][xy+1+sizeX])/2, label);
                    heap.add(v);
                    distanceMap.pixels[v.z][v.xy]=v.value;
                    labelMap.pixels[v.z][v.xy]=label;
                }
            }
            if (dy>0) {
                dist =  Math.sqrt(dx2+dyy2+dz2);
                v=new Voxel(xy+sizeX, z, (float)dist*(value+intensityMap.pixels[z][xy+sizeX])/2, label);
                heap.add(v);
                distanceMap.pixels[v.z][v.xy]=v.value;
                labelMap.pixels[v.z][v.xy]=label;
            }
            if (dz>0) {
                dist =  Math.sqrt(dx2+dy2+dzz2);
                v=new Voxel(xy, z+1, (float)dist*(value+intensityMap.pixels[z+1][xy])/2, label);
                heap.add(v);
                distanceMap.pixels[v.z][v.xy]=v.value;
                labelMap.pixels[v.z][v.xy]=label;
                if (dx>0) {
                    dist =  Math.sqrt(dxx2+dy2+dzz2);
                    v=new Voxel(xy+1, z+1, (float)dist*(value+intensityMap.pixels[z+1][xy+1])/2, label);
                    heap.add(v);
                    distanceMap.pixels[v.z][v.xy]=v.value;
                    labelMap.pixels[v.z][v.xy]=label;    
                    if (dy>0) {
                        dist =  Math.sqrt(dxx2+dyy2+dzz2);
                        v=new Voxel(xy+1+sizeX, z+1, (float)dist*(value+intensityMap.pixels[z+1][xy+1+sizeX])/2, label);
                        heap.add(v);
                        distanceMap.pixels[v.z][v.xy]=v.value;
                        labelMap.pixels[v.z][v.xy]=label;
                    }
                }
                if (dy>0) {
                    dist =  Math.sqrt(dx2+dyy2+dzz2);
                    v=new Voxel(xy+sizeX, z+1, (float)dist*(value+intensityMap.pixels[z+1][xy+sizeX])/2, label);
                    heap.add(v);
                    distanceMap.pixels[v.z][v.xy]=v.value;
                    labelMap.pixels[v.z][v.xy]=label;
                }
            }
        }
    }
    
    public float getDistance(Point3D p) {
        return distanceMap.getPixel((float)p.getX(), (float)p.getY(), (float)p.getZ(), mask);
    }
    
    public int getLabel(Point3D p) {
        return labelMap.getPixelInt(p);
    }
    
    public float getMinDistance(Object3D o, boolean onlyContours) {
        if (o instanceof Object3DPoint) return distanceMap.getPixelInterpolated(o.getCenterAsPoint());
        float min = Float.MAX_VALUE;
        if (onlyContours) {
            for (Voxel3D v : o.getContours()) {
                float temp = distanceMap.getPixel(v);
                if (temp<min) min = temp;
            }
        } else {
            for (Voxel3D v : o.getVoxels()) {
                float temp = distanceMap.getPixel(v);
                if (temp<min) min = temp;
            }
        }
        
        return min;
    }
    
    public void run(Object3D[] seeds, boolean centerOfObjects, boolean contourOfObjects) {
        initDistanceMap();
        if (labelMap!=null) labelMap.erase();
        else labelMap=new ImageShort("gdm_labels", sizeX, sizeY, sizeZ);
        firstRun=true;
        heap=new TreeSet<Voxel>();
        plantSeeds(seeds, centerOfObjects, contourOfObjects);
        //getDistanceMap().showDuplicate("distanceMap init");
        //getLabelMap().showDuplicate("labelMap init");
        run();
        plantSeeds(seeds, centerOfObjects, contourOfObjects); //to ensure every seed is in the map
    }
    
    protected void plantSeeds(Object3D[] seeds, boolean centerOfObjects, boolean contourOfObjects) {
        for (int i = 0; i<seeds.length; i++) {
            Object3D seed=seeds[i];
            short label = (short)seeds[i].getValue();
            if (seed instanceof Object3DPoint || centerOfObjects) {
                double x = seed.getCenterX();
                double y = seed.getCenterY();
                double z = seed.getCenterZ();
                if (x>limX) x=limX;
                if (y>limY) y=limY;
                if (z>limZ) z=limZ;
                plantSeed((int)x, (float)(x-(int)x), (int)y, (float)(y-(int)y), (int)z, (float)(z-(int)z), label);
            } else {
                ArrayList<Voxel3D> voxels = (contourOfObjects)?seed.getContours():seed.getVoxels();
                for (Voxel3D v : voxels) {
                    Voxel vv = new Voxel(v.getRoundX()+v.getRoundY()*sizeX, v.getRoundZ(), 0f, label);
                    heap.add(vv);
                    labelMap.pixels[vv.z][vv.xy]=vv.label;
                    distanceMap.pixels[vv.z][vv.xy]=0;
                }
            }
        }
    }
    
    public void removeSeedAndRun(int seedIdx) {
        // FIXME verifier le comportement de firstRun
        if (templateDistanceMap==null) templateDistanceMap=distanceMap.duplicate();
        else if (firstRun) distanceMap.copy(templateDistanceMap);
        else templateDistanceMap.copy(distanceMap);
        if (templateLabelMap==null) templateLabelMap=labelMap.duplicate();
        else if (firstRun) labelMap.copy(templateLabelMap);
        else templateLabelMap.copy(labelMap);
        heap=new TreeSet<Voxel>();
        short label = (short)(seedIdx+1);
        if (firstRun) {
            firstRun=false;
            labels=new HashMap<Short, ArrayList<SimpleVoxel>>();
            for (int z = 0; z<sizeZ; z++) {
                for (int y=0; y<sizeY; y++) {
                    for (int x= 0; x<sizeX; x++) {
                        ArrayList<SimpleVoxel> al = labels.get(labelMap.pixels[z][x+y*sizeX]);
                        if (al==null) {
                            al=new ArrayList<SimpleVoxel>();
                            labels.put(labelMap.pixels[z][x+y*sizeX], al);
                        }
                        al.add(new SimpleVoxel(x, y, z));
                    }
                }
            }
        }
        if (labels.get(label)==null) {
            System.out.println("label not found:"+label);
            labelMap.show("labelMap");
        }
        for (SimpleVoxel v : labels.get(label)) { // FIXME bug lorque le label n'existe pas!!
            int xy= v.x+v.y*sizeX;
            distanceMap.pixels[v.z][xy]=Float.MAX_VALUE;
            addToHeap(v.x, v.y, v.z, xy, label);
        }
        for (Voxel v : heap) labelMap.pixels[v.z][v.xy]=v.label;
        run();
    }
    
    protected void addToHeap(int x, int y, int z, int xy, short label) {
        Voxel min=null;
        for (int zz=(z>0?-1:0); zz<=(z<limZ?1:0); zz++) {
            for (int yy=(y>0?-1:0); yy<=(y<limY?1:0); yy++) {
                for (int xx=(x>0?-1:0); xx<=(x<limX?1:0);xx++) {
                    if (zz==0 && yy==0 && xx==0) continue;
                    int xy2 = xy+xx+yy*sizeX;
                    if (mask.getPixel(xy2, z+zz)!=0 && labelMap.pixels[z+zz][xy2]!=label) {
                        float distance=distanceMap.pixels[z][xy2];
                        if (zz==0 && (xx==0 || yy==0)) distance+=getCostLine(xy, xy2, z);
                        else if (zz==0) distance+=getCostDiag(xy, z, xx, yy*sizeX);
                        else if (xx==0 && yy==0) distance+=getCostLineZ(xy, z, z+zz);
                        else if (xx==0 || yy==0) distance+=getCostDiagZ(xy, z, z+zz, xx+yy*sizeX);
                        else distance+=getCostDiagZ2(xy, z, z+zz, xx, yy*sizeX);
                        if (min==null) min=new Voxel(xy, z, distance, labelMap.pixels[z+zz][xy2]);
                        else if (min.value>distance) {
                            min.value=distance;
                            min.label=labelMap.pixels[z+zz][xy2];
                        }
                    }
                }
            }
        }
        if (min!=null) {
            heap.add(min);
            distanceMap.pixels[min.z][min.xy]=min.value;
            //labelMap.pixels[z][xy]=min.label;
        }
    }
    
    protected void propagate(Voxel v) {
        int x=v.xy%sizeX;
        int y = v.xy/sizeX;
        for (int zz=(v.z>0?-1:0); zz<=(v.z<limZ?1:0); zz++) {
            for (int yy=(y>0?-1:0); yy<=(y<limY?1:0); yy++) {
                for (int xx=(x>0?-1:0); xx<=(x<limX?1:0);xx++) {
                    if (zz==0 && yy==0 && xx==0) continue;
                    int xy2 = v.xy+xx+yy*sizeX;
                    int z2=v.z+zz;
                    if (mask.getPixel(xy2, z2) != 0) {
                        float distance=v.value;
                        if (zz==0 && (xx==0 || yy==0)) distance+=getCostLine(v.xy, xy2, v.z);
                        else if (zz==0) distance+=getCostDiag(v.xy, v.z, xx, yy*sizeX);
                        else if (xx==0 && yy==0) distance+=getCostLineZ(v.xy, v.z, z2);
                        else if (xx==0 || yy==0) distance+=getCostDiagZ(v.xy, v.z, z2, xx+yy*sizeX);
                        else distance+=getCostDiagZ2(v.xy, v.z, z2, xx, yy*sizeX);
                        if (distance<distanceMap.pixels[z2][xy2]) {
                            distanceMap.pixels[z2][xy2]=distance;
                            labelMap.pixels[z2][xy2]=v.label;
                            heap.add(new Voxel(xy2, z2, distance, v.label));
                        }
                    }
                }
            }
        }
    }
    
    protected void run() {
        while (!heap.isEmpty()) {
            Voxel v = heap.pollFirst();
            if (distanceMap.pixels[v.z][v.xy]==v.value) propagate(v);
        }
    }
    
    //same line & same plane
    protected float getCostLine(int xy1, int xy2, int z) {
        return ((intensityMap.pixels[z][xy1]+intensityMap.pixels[z][xy2])*dist);
    }
    
    //same line & != plane
    protected float getCostLineZ(int xy, int z1, int z2) {
        return ((intensityMap.pixels[z1][xy]+intensityMap.pixels[z2][xy])*distZ);
    }
    
    //same plane & diag
    protected float getCostDiag(int xy, int z, int incX, int incY) {
        return ((3*intensityMap.pixels[z][xy]+3*intensityMap.pixels[z][xy+incX+incY]+intensityMap.pixels[z][xy+incX]+intensityMap.pixels[z][xy+incY])*diag);
    }
    
    //!= plane & diag
    protected float getCostDiagZ(int xy, int z, int z2, int incXY) {
        return ((3*intensityMap.pixels[z][xy]+3*intensityMap.pixels[z2][xy+incXY]+intensityMap.pixels[z][xy+incXY]+intensityMap.pixels[z2][xy])*diagZ);
    }
    
    //!= plane & diag
    protected float getCostDiagZ2(int xy, int z, int z2, int incX, int incY) {
        return (float)((5*intensityMap.pixels[z][xy]+5*intensityMap.pixels[z2][xy+incX+incY]+intensityMap.pixels[z][xy+incX]+intensityMap.pixels[z][xy+incY]+intensityMap.pixels[z2][xy+incX]+intensityMap.pixels[z2][xy+incY]+intensityMap.pixels[z2][xy]+intensityMap.pixels[z][xy+incY+incX])*diagZ2);
    }
    
    
    protected class Voxel implements java.lang.Comparable<Voxel> {
        public int xy, z;
        public short label;
        public float value;

        public Voxel(int xy, int z, float value, short label) {
            this.xy=xy;
            this.z=z;
            this.value=value;
            this.label=label;
        }
        
        @Override
        public int compareTo(Voxel v) {
            if(value > v.value) return 1;
            else if(value < v.value) return -1;
            else if (xy==v.xy && z==v.z) return 0;
            else if (z<v.z) return -1;
            else if (z==v.z && xy<v.xy) return -1;
            else return 1;
        }
        
        @Override
        public int hashCode() {
            int hash = 5;
            hash = 41 * hash + this.xy;
            hash = 41 * hash + this.z;
            return hash;
        }
        @Override
        public boolean equals(Object o) {
            if (o instanceof Voxel) {
                return xy==((Voxel)o).xy && z==((Voxel)o).z; // FIXME : changed for VOXEL instead of DMVOXEL
            } else return false;
        }
        
        public float getIntensity() {
            return intensityMap.pixels[z][xy];
        }
        public short getLabel() {
            return labelMap.pixels[z][xy];
        }
    }
    
    protected class SimpleVoxel {
        public int x, y, z;

        public SimpleVoxel(int x, int y, int z) {
            this.x=x;
            this.y=y;
            this.z=z;
        }
    }
}
