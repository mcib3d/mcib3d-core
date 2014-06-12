package tango.parameter;

import ij.IJ;
import java.util.ArrayList;
import java.util.Arrays;
import mcib3d.image3d.ImageByte;
import mcib3d.image3d.ImageFloat;
import mcib3d.image3d.ImageHandler;
import mcib3d.image3d.ImageInt;
import mcib3d.image3d.distanceMap3d.EDT;
import tango.dataStructure.InputCellImages;
import tango.dataStructure.ObjectQuantifications;
import tango.dataStructure.SegmentedCellImages;
import tango.gui.Core;
import tango.plugin.measurement.radialAnalysis.NormalizeDistanceMap;

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

public class DistanceMapParameter extends GroupParameter {
    MultiParameter referenceStructures = new MultiParameter("Reference Structures:", "refStructure", new Parameter[]{new StructureParameter("Structure:", "structure", 0, true)}, 1, 10, 1);
    BooleanParameter negativeInsideInternal = new BooleanParameter("Negative distances inside internal structures", "center", false);
    BooleanParameter onlyInsideInternal = new BooleanParameter("Only Inside Structures", "outside", false);
    BooleanParameter normalize = new BooleanParameter("Normalize distances [0:1]", "normalize", true);
    BooleanParameter erode = new BooleanParameter("Erode nuclear edges", "erode", false);
    
    ConditionalParameter normCond = new ConditionalParameter(normalize);
    FilteredStructureParameter normStruct = new FilteredStructureParameter("Normalization map (optional):", "normMap");
    
    ConditionalParameter erodeCond = new ConditionalParameter(erode);
    DoubleParameter erodeDist = new DoubleParameter("Erosion from periphery (unit):", "hardcoreStruct",0d, Parameter.nfDEC5);
    
    public DistanceMapParameter(String label, String id) {
        super(label, id);
        normStruct.setCompulsary(false);
        normCond.setCondition(true, new Parameter[]{normStruct});
        setParameters(new Parameter[]{referenceStructures, onlyInsideInternal, negativeInsideInternal, erode ,normCond});
    }
    
    public DistanceMapParameter(String label, String id, boolean alwaysNormalized, boolean allowErode) {
        super(label, id);
        Parameter normParam = null;
        if (alwaysNormalized) {
            this.normalize.setSelected(true);
            normStruct.setCompulsary(false);
            normParam = normStruct;
        } else {
            normStruct.setCompulsary(false);
            normCond.setCondition(true, new Parameter[]{normStruct});
            normParam = normCond;
        }
        if (allowErode) setParameters(new Parameter[]{referenceStructures, onlyInsideInternal, negativeInsideInternal, erode ,normParam});
        else {
            erode.setSelected(false);
            setParameters(new Parameter[]{referenceStructures, onlyInsideInternal, negativeInsideInternal ,normParam});
        }
    }
    
    public DistanceMapParameter(String label, String id, String dummy) { // no normalization, erode with constant value
        super(label, id);
        normStruct.setCompulsary(false);
        normalize.setSelected(false);
        erodeCond.setCondition(true, new Parameter[]{erodeDist});
        setParameters(new Parameter[]{referenceStructures, onlyInsideInternal, negativeInsideInternal, erodeCond});
    }
    
    private int[] getMaskStructures(SegmentedCellImages seg) {
        int nb = this.referenceStructures.getNbParameters();
        ArrayList<Integer> s = new ArrayList<Integer>(nb);
        Parameter[] ps = referenceStructures.getParameters();
        for (int i = 0; i<nb; i++) {
            int idx = ((StructureParameter)ps[i]).getIndex();
            if (seg.getImage(idx)!=null) {
                if (!s.contains(idx)) s.add(idx);
            }
            else System.out.println("Error: distanceMap parameter structure not segmented:"+idx);
        }
        int[] res = new int[s.size()];
        for (int i = 0;i<res.length;i++) res[i]=s.get(i);
        Arrays.sort(res);
        return res;
    }
    
    private static void erodeMaskAndDM(ImageInt mask, ImageFloat dm, float dist) { 
        for (int z = 0; z<mask.sizeZ; z++) {
            for (int xy = 0; xy<mask.sizeXY; xy++) {
                if (dm.pixels[z][xy]<dist) {
                    dm.pixels[z][xy]=0;
                    mask.setPixel(xy, z, 0);
                } else dm.pixels[z][xy]-=dist;
            }
        }
    }
    
    private static void erodeMask(ImageInt mask, ImageFloat dm, float dist) {
        for (int z = 0; z<mask.sizeZ; z++) {
            for (int xy = 0; xy<mask.sizeXY; xy++) {
                if (dm.pixels[z][xy]<dist) mask.setPixel(xy, z, 0);
            }
        }
    }
    
    public boolean isErodeNucleus() {
        return this.erode.isSelected();
    }
    
    public float getErodeDistance() {
        return erodeDist.getFloatValue(0);
    }
    
    // faire la methode statique qui n'a pas besoin des parameters
    // returns [ImageInt, ImageFloat]
    public ImageHandler[] getMaskAndDistanceMap(InputCellImages raw, SegmentedCellImages seg, float erodeDist, boolean verbose, int nCPUs) {
        if (verbose && isErodeNucleus()) System.out.println("DistanceMap parameter erode:"+erodeDist);
        if (isErodeNucleus() && erodeDist==0 && this.erodeDist.getFloatValue(0)>0)   erodeDist=this.erodeDist.getFloatValue(0);
        int[] structures = getMaskStructures(seg);
        ImageHandler normImage=null;
        if (normStruct.getIndex()>=0) normImage = normStruct.getImage(raw, verbose, nCPUs);
        return getMaskAndDistanceMap(raw, seg, erodeDist, structures,normalize.isSelected(), negativeInsideInternal.isSelected(), this.onlyInsideInternal.isSelected(), normImage, verbose, nCPUs);
    }
    
    
    public static ImageHandler[] getMaskAndDistanceMap(InputCellImages raw, SegmentedCellImages seg, float erodeDist, int[] structures,boolean normalizeDM, boolean negativeInside, boolean onlyInside, ImageHandler normImage, boolean verbose, int nCPUs) {
        // mask
        ImageInt mask;
        if (structures.length==0) {
            if (Core.GUIMode) IJ.log("distanceMap parameter error: no structures selected");
            else System.out.println("distanceMap parameter: no structures selected");
            return null;
        }
        ImageFloat distanceMap;
        boolean newDM = false;
        if (structures[0]==0) { 
            if (structures.length==1) {
                mask=raw.getMask();
                distanceMap = seg.getDistanceMap(0, nCPUs);
                if (erodeDist>0) {
                    mask=mask.duplicate();
                    distanceMap=distanceMap.duplicate();
                    erodeMaskAndDM(mask, distanceMap, erodeDist);
                    newDM=true;
                }
            }
            else if (!negativeInside) { // inside nucleus and outside other structutres
                ImageByte m =  raw.getMask().toMask();
                if (erodeDist>0) erodeMask(m, seg.getDistanceMap(0, nCPUs), erodeDist);
                for (int i = 1; i<structures.length; i++) m.substractMask(seg.getImage(structures[i]));
                mask=m;
                distanceMap=EDT.run(mask, 0, false, nCPUs);
                newDM=true;
            } else { // inside nucleus and outside other structutres + negativ inside internal structures
                mask = raw.getMask().toMask();
                if (erodeDist>0) erodeMask(mask, seg.getDistanceMap(0, nCPUs), erodeDist);
                ImageByte m =  (ImageByte)mask.duplicate();
                ImageInt mInternal;
                if (structures.length>2) {
                    ImageByte m2 = seg.getImage(structures[1]).toMask();
                    for (int i = 2; i<structures.length; i++) m2.addMask(seg.getImage(structures[i])); 
                    mInternal=m2;
                } else mInternal = seg.getImage(structures[1]);
                m.substractMask(mInternal);
                distanceMap=EDT.run(m, 0, false, nCPUs);
                ImageFloat distanceMap2=EDT.run(mInternal, 0, false, nCPUs);
                distanceMap.subtract(distanceMap2);
                newDM=true;
            }
        } else {
            if (structures.length==1) {
                if (onlyInside) {
                    mask = seg.getImage(structures[0]);
                    distanceMap=EDT.run(mask, 0, false, nCPUs);
                    newDM=true;
                } else if (!negativeInside) { 
                    distanceMap = seg.getDistanceMap(structures[0], nCPUs);
                    ImageByte m  =  raw.getMask().toMask();
                    m.substractMask(seg.getImage(structures[0]));
                    mask=m;
                } else { 
                    mask = seg.getImage(structures[0]);
                    distanceMap=EDT.run(mask, 0, true, nCPUs);
                    ImageFloat distanceMap2=EDT.run(mask, 0, false, nCPUs);
                    distanceMap.subtract(distanceMap2);
                    newDM=true;
                    mask = raw.getMask();
                }
            }
            else if (!negativeInside || onlyInside) {
                ImageByte m =  seg.getImage(structures[0]).toMask();
                for (int i = 1; i<structures.length; i++) m.addMask(seg.getImage(structures[i]));
                mask=m;
                distanceMap=EDT.run(mask, 0, !onlyInside, nCPUs);
                newDM=true;
                if (!onlyInside) { 
                    ImageByte mask2 = raw.getMask().toMask();
                    mask2.substractMask(m);
                    mask=mask2;
                }
            } else { 
                ImageByte m =  seg.getImage(structures[0]).toMask();
                for (int i = 1; i<structures.length; i++) m.addMask(seg.getImage(structures[i]));
                distanceMap=EDT.run(m, 0, true, nCPUs);
                ImageFloat distanceMap2=EDT.run(m, 0, false, nCPUs);
                distanceMap.subtract(distanceMap2);
                newDM=true;
                mask=raw.getMask();
            }
        }
        if (verbose) {
            mask.show("mask");
            distanceMap.showDuplicate("distanceMap");
        }

        // normalize distanceMap
        if (normalizeDM) {
            NormalizeDistanceMap nm= new NormalizeDistanceMap();
            if (!newDM) distanceMap = distanceMap.duplicate();
            if (normImage!=null) {
                nm.normalizeDistanceMap(distanceMap, mask, normImage);
            } else nm.normalizeDistanceMap(distanceMap, mask);
            if (verbose) distanceMap.show("normalized distanceMap");
        }
        
        return new ImageHandler[]{mask, distanceMap};
    }
    
}
