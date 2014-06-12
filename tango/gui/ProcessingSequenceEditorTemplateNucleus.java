package tango.gui;

import tango.gui.parameterPanel.MultiParameterPanel;
import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import tango.gui.parameterPanel.NucleiSegmenterPanel;
import tango.gui.parameterPanel.PostFilterPanel;
import tango.gui.parameterPanel.PreFilterPanel;
import mcib3d.utils.exceptionPrinter;
import ij.IJ;
import ij.gui.GenericDialog;
import mcib3d.image3d.ImageByte;
import mcib3d.image3d.ImageHandler;
import mcib3d.image3d.ImageInt;
import java.awt.Choice;
import java.awt.Dimension;
import tango.dataStructure.InputFieldImages;
import tango.mongo.MongoConnector;
import tango.plugin.segmenter.NucleusSegmenterRunner;
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

public class ProcessingSequenceEditorTemplateNucleus extends ProcessingSequenceTemplateEditor {
    protected String currentTestImage;
    public ProcessingSequenceEditorTemplateNucleus(Core main) {
        super(main);
    }
    @Override
    protected String getTitle() {
        return "Nucleus Template";
    }
    
    @Override
    protected void get() {
        populatingProcessingSequences=true;
        processingSequences.removeAllItems();
        processingSequences.addItem(" ");
        for (String item : Core.mongoConnector.getNucSettings()) {
            processingSequences.addItem(item);
        }
        populatingProcessingSequences=false;
    }
    
    @Override
    protected void create(String name) {
        Core.mongoConnector.createNucSettings(name);
    }
    @Override
    protected BasicDBObject get(String name){
        return Core.mongoConnector.getNucSettings(name);
    }
    @Override
    protected void record() {
        if (data==null) create((String)processingSequences.getSelectedItem());
        else {
            Core.mongoConnector.saveNucSettings(data);
        }
        //ij.IJ.log("record:"+Core.mongoConnector.getNucSettings(data.getString("name")));
    }
    @Override
    protected void createMultiPanels() {
        this.preFilterPanel=new MultiParameterPanel<PreFilterPanel> (core, getPreFilters(), 0, 10, layout.postFilterPanel.getMinimumSize(), layout, false, PreFilterPanel.class);
        this.segmenterPanel=new MultiParameterPanel<NucleiSegmenterPanel> (core, getSegmentation(), 1, 1, layout.segmentationPanel.getMinimumSize(), layout, false, NucleiSegmenterPanel.class);
        this.postFilterPanel=new MultiParameterPanel<PostFilterPanel> (core, getPostFilters(), 0,10, layout.postFilterPanel.getMinimumSize(), layout, false, PostFilterPanel.class);
    }

    @Override
    protected void rename(String oldName, String newName) {
        Core.mongoConnector.renameNucSettings(oldName, newName);
    }

    @Override
    protected void duplicate(String name, String newName) {
        Core.mongoConnector.duplicateNucSettings(name, newName);
    }

    @Override
    protected void remove(String name) {
        Core.mongoConnector.removeNucSettings(name);
    }

    @Override
    protected void test() {
        try{
            Core.debug=true;
            int[] ids = ij.WindowManager.getIDList();
            String[] names = new String[ids.length];

            for (int i = 0; i<ids.length; i++) {
                names[i]=ij.WindowManager.getImage(ids[i]).getTitle();
            }

            GenericDialog gd = new GenericDialog("Select Window For Test:", core);
            gd.addChoice("Image: ", names, (currentTestImage!=null)?currentTestImage:names[0]);
            gd.addCheckbox("preFilters", true);
            gd.addCheckbox("segmentation", true);
            gd.addCheckbox("postFilter", true);
            gd.showDialog();
            if (gd.wasOKed()) {
                int imageIdx = ((Choice)gd.getChoices().get(0)).getSelectedIndex();
                currentTestImage=((Choice)gd.getChoices().get(0)).getSelectedItem();
                boolean preFilters = gd.getNextBoolean();
                boolean segmentation = gd.getNextBoolean();
                boolean postFilters = gd.getNextBoolean();
                save(false);
                ImageHandler curIm = ImageHandler.wrap(ij.WindowManager.getImage(ids[imageIdx]));
                ij.WindowManager.toFront(ij.WindowManager.getFrame(names[imageIdx]));
                InputFieldImages images = new InputFieldImages(curIm);
                if (preFilters) {
                    IJ.log("preFilter chain test..");
                    PreFilterSequence pfr = new PreFilterSequence(data, Core.getMaxCPUs(), false);
                    if (!pfr.isEmpty()) {
                        IJ.log("run preFilters..");
                        curIm=pfr.run(0, curIm, images);
                        images.setFilteredImage(curIm, 0);
                        curIm.showDuplicate(names[imageIdx]+"::preProcessed");
                    } else {
                        IJ.log("no prefilters..");
                    }
                }
                if (segmentation) {
                    NucleusSegmenterRunner spr = new NucleusSegmenterRunner(data, Core.getMaxCPUs(), true);
                    if (!spr.isEmpty()) {
                        // FIXME inconsitancy
                        curIm = spr.run(0, curIm, images);
                        curIm.showDuplicate("Masks");
                    }
                }
                if (postFilters) {
                    PostFilterSequence pfr = new PostFilterSequence(data, Core.getMaxCPUs(), true);
                    if (!pfr.isEmpty()) {
                        curIm=pfr.run(0, (ImageInt)curIm, images);
                        curIm.show("Masks::postProcessed");
                    }
                }
            }
        } catch (Exception e) {
            exceptionPrinter.print(e, "", Core.GUIMode);
        }
        Core.debug=false;
    }
}
