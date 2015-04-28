package tango.gui;

import tango.gui.parameterPanel.MultiParameterPanel;
import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import tango.gui.parameterPanel.ChannelSegmenterPanel;
import tango.gui.parameterPanel.NucleiSegmenterPanel;
import tango.gui.parameterPanel.PostFilterPanel;
import tango.gui.parameterPanel.PreFilterPanel;
import ij.gui.GenericDialog;
import mcib3d.image3d.ImageHandler;
import java.awt.Choice;
import java.awt.Dimension;
import mcib3d.image3d.ImageInt;
import mcib3d.utils.exceptionPrinter;
import tango.dataStructure.InputCellImages;
import tango.mongo.MongoConnector;
import tango.plugin.filter.PostFilterSequence;
import tango.plugin.filter.PreFilterSequence;
import tango.plugin.segmenter.SpotSegmenterRunner;
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

public class ProcessingSequenceEditorTemplateStructure extends ProcessingSequenceTemplateEditor {
    protected String currentTestImage, currentTestMask;
    
    public ProcessingSequenceEditorTemplateStructure(Core main) {
        super(main);
    }
    
    @Override
    protected String getTitle() {
        return "Structure Template";
    }
    
    @Override
    protected void get() {
        populatingProcessingSequences=true;
        processingSequences.removeAllItems();
        for (String item : Core.mongoConnector.getChannelSettings()) processingSequences.addItem(item);
        populatingProcessingSequences=false;
    }
    @Override
    protected void create(String name) {
        Core.mongoConnector.createStructureProcessingChain(name);
    }
    
    @Override
    protected BasicDBObject get(String name){
        return Core.mongoConnector.getChannelSettings(name);
    }
    @Override
    protected void record() {
        if (data==null) create((String)processingSequences.getSelectedItem());
        else {
            Core.mongoConnector.saveStructureProcessingChain(data);
        }
    }
    @Override
    protected void createMultiPanels() {
        //ij.IJ.log("prefilters data:"+(data.get("preFilters")));
        try {
            this.preFilterPanel=new MultiParameterPanel<PreFilterPanel> (core, getPreFilters(), 0, 10, layout.preFilterPanel.getMinimumSize(), layout, false, PreFilterPanel.class);
            this.segmenterPanel=new MultiParameterPanel<ChannelSegmenterPanel> (core, getSegmentation(), 1, 1, layout.segmentationPanel.getMinimumSize(), layout, false, ChannelSegmenterPanel.class);
            this.postFilterPanel=new MultiParameterPanel<PostFilterPanel> (core, getPostFilters(), 0,10, layout.postFilterPanel.getMinimumSize(), layout, false, PostFilterPanel.class);
        } catch (Exception e) {
            exceptionPrinter.print(e, "", Core.GUIMode);
        }
    }

    @Override
    protected void rename(String oldName, String newName) {
        Core.mongoConnector.renameChannelSettings(oldName, newName);
    }

    @Override
    protected void duplicate(String name, String newName) {
        Core.mongoConnector.duplicateChannelSettings(name, newName);
    }

    @Override
    protected void remove(String name) {
        Core.mongoConnector.removeChannelSettings(name);
    }

    @Override
    protected void test() {
        try {
            Core.debug=true;
            int[] ids = ij.WindowManager.getIDList();
            String[] names = new String[ids.length];
            String[] namesNone = new String[ids.length+1];
            namesNone[0]="*NONE*";
            for (int i = 0; i<ids.length; i++) {
                names[i]=ij.WindowManager.getImage(ids[i]).getTitle();
                namesNone[i+1]=names[i];
            }

            GenericDialog gd = new GenericDialog("Select Window For Test:", core);
            gd.addChoice("Image: ", names, (currentTestImage!=null)?currentTestImage:names[0]);
            gd.addChoice("Nucleus Mask: ", namesNone, (currentTestMask!=null)?currentTestMask:namesNone[0]);
            gd.addCheckbox("preFilters", true);
            gd.addCheckbox("segmentation", true);
            gd.addCheckbox("postFilter", true);
            gd.showDialog();
            if (gd.wasOKed()) {
                int imageIdx = ((Choice)gd.getChoices().get(0)).getSelectedIndex();
                currentTestImage=((Choice)gd.getChoices().get(0)).getSelectedItem();
                int maskIdx = ((Choice)gd.getChoices().get(1)).getSelectedIndex()-1;
                currentTestMask=((Choice)gd.getChoices().get(1)).getSelectedItem();
                boolean preFilters = gd.getNextBoolean();
                boolean segmentation = gd.getNextBoolean();
                boolean postFilters = gd.getNextBoolean();
                save(false);
                ImageHandler curIm = ImageHandler.wrap(ij.WindowManager.getImage(ids[imageIdx]));
                ImageHandler m = (maskIdx>=0)? ImageHandler.wrap(ij.WindowManager.getImage(ids[maskIdx])):null;
                if (m!=null && !(m instanceof ImageInt)) ij.IJ.error("TANGO Processing sequence Test", "Mask is not short or byte image");
                InputCellImages images = (maskIdx>=0)?new InputCellImages((ImageInt)m) : null;
                ij.WindowManager.toFront(ij.WindowManager.getFrame(names[imageIdx]));
                if (preFilters) {
                    PreFilterSequence pfr = new PreFilterSequence(data, Core.getMaxCPUs(), true);
                    if (!pfr.isEmpty()) {
                        // FIXME inconsitancy
                        curIm=pfr.run(0, curIm, images);
                        curIm.showDuplicate(names[imageIdx]+"::preProcessed");
                    }
                }
                if (segmentation) {
                    SpotSegmenterRunner spr = new SpotSegmenterRunner(data, Core.getMaxCPUs(), true);
                    if (!spr.isEmpty()) {
                        curIm = spr.run(0, curIm, images);
                        curIm.set332RGBLut();
                        curIm.showDuplicate(names[imageIdx]+"::segmented");
                    }
                }
                if (postFilters) {
                    PostFilterSequence pfr = new PostFilterSequence(data, Core.getMaxCPUs(), true);
                    if (!pfr.isEmpty()) {
                        curIm=pfr.run(0, (ImageInt)curIm, images);
                        curIm.set332RGBLut();
                        curIm.showDuplicate(names[imageIdx]+"::postProcessed");
                    }
                }
            }
        } catch (Exception e) {
            exceptionPrinter.print(e, "", Core.GUIMode);
        }
        Core.debug=false;
    }   
}
