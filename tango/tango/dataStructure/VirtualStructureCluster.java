package tango.dataStructure;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import mcib3d.geom.Object3DVoxels;
import mcib3d.image3d.ImageFloat;
import mcib3d.image3d.ImageHandler;
import mcib3d.image3d.ImageInt;
import mcib3d.image3d.ImageShort;
import mcib3d.utils.exceptionPrinter;
import tango.gui.Core;
import tango.gui.parameterPanel.VirtualStructurePanel;
import tango.plugin.measurement.distance.Distance;
import tango.plugin.measurement.distance.EuclideanDistance;
import tango.plugin.segmenter.DivideObjects;

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

public class VirtualStructureCluster extends VirtualStructure {
    double contactThreshold;
    int inputStructureCentro, inputStructureMediator;
    
    VirtualStructureCluster(String title, int idx, Cell cell) {
        super(title, idx, cell);
        
    }
    
    
    protected void init() {
        VirtualStructurePanel vsp = getPanel(cell, idx);
        contactThreshold = vsp.clusterThreshold.getDoubleValue(0.11);
        inputStructureCentro = vsp.inputStructure.getIndex();
        inputStructureMediator=vsp.inputStructureCluster.getIndex();
        
    }

    @Override
    public void process() {
        init();
        if (inputStructureCentro<=0 || inputStructureMediator<=0) return;
        //((Structure)cell.getStructure(inputStructure)).shiftObjectIndexes(!cell.verbose);
        Object3DVoxels[] centro = cell.segImages.getObjects(inputStructureCentro);
        Object3DVoxels[] PHC = cell.segImages.getObjects(inputStructureMediator); 
        if (centro==null || centro.length==0) return;
        
        // get distances 
        Distance d = new EuclideanDistance(1, 2); // distance Bord-Bord inclusion<0
        int[] indexes = new int[centro.length];
        double[] minDist = d.getNearestNeighborDistances(centro, PHC, indexes);
        Set<Integer> centroAlone = new HashSet<Integer>();
        Set<Integer> PHCCluster = new HashSet<Integer>();
        for (int i = 0;i<minDist.length; i++) {
            if (minDist[i]<=this.contactThreshold) PHCCluster.add(indexes[i]);
            else centroAlone.add(i);
            //TODO: ajouter les centro dans le cluster...
        }
        System.out.println("VS:cluster: nb PHC:"+PHCCluster.size());
        System.out.println("VS:cluster: nb centro:"+centroAlone.size());
        //create new objects (clones with new  ones at the end with new values)
        Object3DVoxels[] res = new Object3DVoxels[centroAlone.size()+PHCCluster.size()];
        int count = 0;
        for (int i : PHCCluster) res[count++] = PHC[i].copyObject(count); //cont++ evaluated before copyObject!
        for (int i : centroAlone) res[count++] = centro[i].copyObject(count); //cont++ evaluated before copyObject!
        ImageInt mask = cell.getMask();
        ImageInt seg = new ImageShort("VirtualStructureCluster:"+cell.getName(), mask.sizeX, mask.sizeY, mask.sizeZ);
        seg.setOffset(mask);
        seg.setScale(mask);
        for (Object3DVoxels v: res) v.draw(seg);
        cell.segImages.setSegmentedImage(seg, idx);
        cell.segImages.setObjects(res, idx);
    }

}
