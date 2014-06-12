package tango.dataStructure;
import mcib3d.image3d.ImageHandler;
import mcib3d.image3d.ImageShort;
import mcib3d.image3d.ImageFloat;
import mcib3d.image3d.ImageInt;
import mcib3d.geom.Object3DFactory;
import mcib3d.geom.Object3DFuzzy;
import java.io.File;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.mongodb.BasicDBList;
import mcib3d.utils.exceptionPrinter;
import java.util.*;
import mcib3d.geom.*;
import mcib3d.image3d.*;
import ij.*;
import ij.gui.ImageWindow;
import ij.io.FileInfo;
import java.awt.image.IndexColorModel;
import tango.gui.Core;
import tango.gui.parameterPanel.StructurePanel;
import tango.mongo.MongoConnector;
import tango.plugin.filter.PostFilterSequence;
import tango.plugin.filter.PreFilterSequence;
import tango.plugin.segmenter.SpotSegmenterRunner;
import tango.util.IJ3dViewerParameters;
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

public class Structure extends AbstractStructure {
    public Structure (String title, int idx, Cell cell) {
        super(title, idx, cell);
        
    }
    
    public boolean shiftObjectIndexes(boolean saveIfChange) {
        Object3D[] objects = cell.segImages.getObjects(idx);
        if (objects==null || objects.length==0) return false;
        boolean change=false;
        ImageStack c = cell.segImages.getImage(idx).getImageStack();
        for (int i = 1; i<=objects.length; i++) {
            Object3D o = objects[i-1];
            if (o.getValue()!=i) {
                change=true;
                o.setValue(i);
                o.draw(c, i);
            }
        }
        if (saveIfChange && change) this.saveOutput();
        return change;
    }
    
    @Override
    public void createObjects() {
        ImageInt S = cell.segImages.getImage(idx);
        //System.out.println("create objects:"+this.name+ " S==null?"+(S==null));
        if (S!=null) {
            try {
                ImageFloat pm = cell.segImages.getProbabilityMap(idx);
                if (pm!=null) cell.segImages.setObjects(S.getObjects3D(pm, 0.5f), idx); 
                else cell.segImages.setObjects(S.getObjects3D(), idx); 
                return;
            } catch (Exception e) {
                exceptionPrinter.print(e, "", Core.GUIMode);
            }
        }
        cell.segImages.setObjects(new Object3DVoxels[0], idx);
    }

    public ImageHandler openRaw() {
        return cell.openInputImage(idxRaw);
    }
    
    @Override
    public ImageInt openSegmented()  {
        try {
            ImageHandler ih = cell.mc.getNucImage(cell.id, idx, MongoConnector.S);
            if (ih !=null) {
                ImageInt S=(ImageInt)ih;
                S.set332RGBLut();
                xp.setCalibration(S);
                return S;
            }
        } catch (Exception e) {
            exceptionPrinter.print(e, "", Core.GUIMode);
        }
        return null;
    }
    
    public void setSegmented(ImageInt labelledImage) {
        this.cell.segImages.setSegmentedImage(labelledImage, idx);
    }
    
    public ImageFloat openProbabilityMap() {
        try {
            ImageHandler ih=cell.mc.getNucImage(cell.id, idx, MongoConnector.SP);
            if (ih!=null) {
                ImageFloat SP = (ImageFloat)ih;
                xp.setCalibration(SP);
                return SP;
            }
        } catch (Exception e) {
            exceptionPrinter.print(e, "", Core.GUIMode);
        }
        return null;
    }

    public void process() {
        try {
            if (cell.verbose) cell.inputImages.getFilteredImage(idx).showDuplicate("pre Filtered Image");
            System.out.println(name+ " prefilter ok.");
            segment();
            System.out.println(name+ " segmentation ok.");
            if (cell.verbose && cell.segImages.getImage(idx)!=null) {
                cell.segImages.getImage(idx).set332RGBLut();
                cell.segImages.getImage(idx).showDuplicate("Segmented Image");
            }
            ImageInt pp = postFilter(cell.segImages.getImage(idx));
            cell.segImages.setSegmentedImage(pp, idx);
            System.out.println(name+ " postfilter ok.");
            if (cell.verbose && cell.segImages.getImage(idx)!=null) {
                cell.segImages.getImage(idx).set332RGBLut();
                cell.segImages.getImage(idx).showDuplicate(null);
            }
            if (!cell.verbose) this.shiftObjectIndexes(false);
        } catch (Exception e) {
            String n = (cell.field!=null) ? "field: "+cell.field.name+ " ": "";
            n+="cell: "+cell.name+ " channel: "+name;
            exceptionPrinter.print(e, "process: cell:"+n, Core.GUIMode);
        }
    }
    // for testing : stops the process to a substep of a step (0 = pre-processing / 1=segmentation / 2=post-processing)
    public void testProcess(int step, int subStep) {
        try {
            if (step == 0) { // stops within pre-process
                 ImageHandler in = cell.inputImages.getImage(idx);
                 PreFilterSequence pofs= xp.getPreFilterSequence(idx, cell.nbCPUs, false);
                 pofs.test(idx, in, cell.inputImages, subStep, false);
            } else if (step == 1) { // stops after segmentation
                cell.inputImages.getFilteredImage(idx).showDuplicate("pre Filtered Image");
                cell.setVerbose(true);
                segment();
                cell.segImages.getImage(idx).showDuplicate("Segmented Image");
                cell.segImages.setSegmentedImage(null, idx);
            } else if (step == 2) { // stops within post-process
                cell.setVerbose(false);
                segment();
                ImageInt in = cell.segImages.getImage(idx);
                PostFilterSequence pofs= xp.getPostFilterSequence(idx, cell.nbCPUs, false);
                pofs.test(idx, in, cell.inputImages, subStep, false);
                cell.segImages.setSegmentedImage(null, idx);
            }
        } catch (Exception e) {
            String n = (cell.field!=null) ? "field: "+cell.field.name+ " ": "";
            n+="cell: "+cell.name+ " channel: "+name;
            exceptionPrinter.print(e, "test: cell:"+n, Core.GUIMode);
        }
    }
    
    public ImageHandler preFilter(ImageHandler input) {
        PreFilterSequence prfs= xp.getPreFilterSequence(idx, cell.nbCPUs, cell.verbose);
        if (!prfs.isEmpty()) {
            input=prfs.run(idx, input, cell.inputImages);
            input.setTitle(name+ "::Filtered");
        }
        return input;
    }
    
    public ImageInt postFilter(ImageInt input) {
        PostFilterSequence pofs= xp.getPostFilterSequence(idx, cell.nbCPUs, cell.verbose);
        if (!pofs.isEmpty()) {
            ImageInt S=(ImageInt)pofs.run(idx, input, cell.inputImages);
            S.setTitle(name+ "::PostFiltered");
            return S;
        }
        return input;
    }
    
    public void segment() {
        SpotSegmenterRunner ssr = xp.getSpotSegmenterRunner(idx, cell.nbCPUs, cell.verbose);
        if (!ssr.isEmpty()) {
            ImageHandler in = cell.inputImages.getFilteredImage(idx);
            ImageInt S=ssr.run(idx, in, cell.inputImages);
            if (S==null) {
                cell.segImages.setSegmentedImage(null, idx);
                cell.segImages.setProbabilityMap(null, idx);
                return;
            }
            ImageFloat SP=ssr.getProbabilityMap();
            if (SP!=null) {
                SP.setTitle(name+ "::ProbabilityMap");
                SP.setScale(in);
                SP.setOffset(in);
            }
            S.setTitle(name+ "::Segmented");
            S.setScale(in);
            S.setOffset(in);
            cell.segImages.setSegmentedImage(S, idx);
            cell.segImages.setProbabilityMap(SP, idx);
        }
    }
    
    @Override
    public void saveOutput() {
        ImageInt S = cell.segImages.getImage(idx);
        cell.getConnector().saveNucleusImage(cell.id, idx, MongoConnector.S, S);
        ImageFloat SP = cell.segImages.getProbabilityMap(idx);
        cell.getConnector().saveNucleusImage(cell.id, idx, MongoConnector.SP, SP);
    }

    public ImageFloat getProbabilityMap() {
        return cell.segImages.getProbabilityMap(idx);
    }
    
    
}
