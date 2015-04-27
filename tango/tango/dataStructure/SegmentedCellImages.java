package tango.dataStructure;

import java.awt.Point;
import mcib3d.geom.Object3D;
import mcib3d.geom.Object3DVoxels;
import mcib3d.image3d.ImageFloat;
import mcib3d.image3d.ImageHandler;
import mcib3d.image3d.ImageInt;
import mcib3d.image3d.distanceMap3d.EDT;
import tango.dataStructure.Cell;

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

public class SegmentedCellImages extends StructureImages {
    Cell cell;
    private ImageInt[] images;
    private ImageFloat[] probabilityMap;
    private ImageFloat[] distanceMap;
    private Object3DVoxels[][] objects;
    public SegmentedCellImages(Cell cell) {
        this.cell=cell;
        images= new ImageInt[cell.getNbStructures(true)];
        probabilityMap= new ImageFloat[cell.getNbStructures(false)];
        distanceMap= new ImageFloat[cell.getNbStructures(true)];
        objects = new Object3DVoxels[images.length][];
    }
    
    public boolean isOpened(int structureIdx) {
       return (images[structureIdx]!=null);
    }
    
    public boolean isVisible(int structureIdx) {
        return (isOpened(structureIdx) && images[structureIdx].isVisible());
    }
    
        public Point getWindowPosition(int structureIdx) {
        if (isVisible(structureIdx)) return images[structureIdx].getImagePlus().getWindow().getLocation();
        else return null;
    }
    
    public void setWindowPosition(int structureIdx, Point p) {
        if (p!=null && isOpened(structureIdx)) {
            if (images[structureIdx].getImagePlus()!=null && images[structureIdx].getImagePlus().getWindow()!=null) {
                images[structureIdx].getImagePlus().getWindow().setLocation(p);
            }
        }
    }
    
    
    @Override
    public synchronized ImageInt getImage(int structureIdx) {
        if (structureIdx<0 || structureIdx>=images.length) return null;
        if (images[structureIdx]==null  || !images[structureIdx].isOpened()) images[structureIdx]=cell.getStructure(structureIdx).openSegmented();
        return images[structureIdx];
    }
    
    public synchronized Object3DVoxels[] getObjects(int structureIdx) {
        if (structureIdx<0 || structureIdx>=objects.length) return null;
        if (objects[structureIdx]==null) cell.getStructure(structureIdx).createObjects();
        return objects[structureIdx];
    }
    
    protected synchronized void setObjects(Object3DVoxels[] o, int structureIdx) {
        if (structureIdx>=0 && structureIdx<objects.length) objects[structureIdx]=o;
    }
    
    public synchronized ImageFloat getProbabilityMap(int structureIdx) {
        if (structureIdx<=0 || structureIdx>=probabilityMap.length) return null;
        if (probabilityMap[structureIdx]==null  || !probabilityMap[structureIdx].isOpened()) probabilityMap[structureIdx]=((Structure)cell.getStructure(structureIdx)).openProbabilityMap();
        return probabilityMap[structureIdx];
    }
    
    public synchronized ImageFloat getDistanceMap(int structureIdx, int nbCPUs) {
        if (distanceMap[structureIdx]==null) {
            // 0 distanceMap inside nucleus else distanceMap outside structure, within nucleus
            ImageInt image = getImage(structureIdx);
            ImageInt maskImage = getImage(0);
            if (image!=null) distanceMap[structureIdx]=EDT.run(image, 0f, (float)maskImage.getScaleXY(), (float)maskImage.getScaleZ(), structureIdx!=0, nbCPUs);
        }
        return distanceMap[structureIdx];
    }

    @Override
    protected void closeAll() {
        for (int i = 0; i<images.length; i++) {
            if (images[i]!=null) {
                images[i].flush();
                images[i]=null;
            }
            objects[i]=null;
        }
        for (int i = 0; i<probabilityMap.length; i++) {
            if (probabilityMap[i]!=null) {
                probabilityMap[i].flush();
                probabilityMap[i]=null;
            }
        }
        for (int i = 0; i<distanceMap.length; i++) {
            if (distanceMap[i]!=null) {
                distanceMap[i].flush();
                distanceMap[i]=null;
            }
        }
    }
    @Override
    protected void hideAll() {
        for (int i = 0; i<images.length; i++) {
            if (images[i]!=null) {
                images[i].hide();
            }
        }
        for (int i = 0; i<probabilityMap.length; i++) {
            if (probabilityMap[i]!=null) {
                probabilityMap[i].hide();
            }
        }
        for (int i = 0; i<distanceMap.length; i++) {
            if (distanceMap[i]!=null) {
                distanceMap[i].hide();
            }
        }
    }
    public void setSegmentedImage(ImageInt image, int structureIdx) {
        images[structureIdx]=image;
        objects[structureIdx]=null;
    }
    public void setProbabilityMap(ImageFloat probaMap, int structureIdx) {
        this.probabilityMap[structureIdx]=probaMap;
    }

    @Override
    protected boolean hasOpenedImages() {
        for (int i = 0; i<images.length; i++) if (images[i]!=null) return true;
        for (int i = 0; i<probabilityMap.length; i++) if (probabilityMap[i]!=null) return true;
        for (int i = 0; i<distanceMap.length; i++) if (distanceMap[i]!=null) return true;
        return false;
    }
    
    
}
