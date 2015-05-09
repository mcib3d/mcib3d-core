package tango.plugin.sampler;

import ij.IJ;
import ij.ImagePlus;
import java.util.ArrayList;
import mcib3d.geom.Object3D;
import mcib3d.geom.Object3DVoxels;
import mcib3d.geom.Objects3DPopulation;
import mcib3d.image3d.ImageHandler;
import mcib3d.image3d.ImageInt;
import tango.dataStructure.InputCellImages;
import tango.dataStructure.SegmentedCellImages;
import tango.parameter.Parameter;
import tango.parameter.StructureParameter;

/*
 * To change this template, choose Tools | Templates and open the template in
 * the editor.
 */
/**
 *
 **
 * /**
 * Copyright (C) 2008- 2012 Thomas Boudier and others
 *
 *
 *
 * This file is part of mcib3d
 *
 * mcib3d is free software; you can redistribute it and/or modify it under the
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
 * @author thomas
 */
public class Shuffle_3D implements Sampler {

    ImagePlus plus;
    private boolean debug;
    double ang = 0;
    String[] axes = {"X-axis", "Y-axis", "Z-axis"};
    int axe = 0;

    // parameters
    StructureParameter channelMask = new StructureParameter("Channel for mask:", "shuffle_mask", 0, true);
    StructureParameter channelObjects = new StructureParameter("Channel for objects:", "shuffle_objects", 1, true);
    //BooleanParameter rotate = new BooleanParameter("Rotation:", "shuffle_rotate", false);
    //DoubleParameter angle = new DoubleParameter("Angle:", "shuffle_angle", ang, Parameter.nfDEC2);
    //ChoiceParameter axechoice = new ChoiceParameter("Axe:", "shuffle_axe", axes, axes[axe]);
    //TextParameter testtext = new TextParameter("Test:", "shuffle_test", "test de texte");
    //SpinnerParameter slid = new SpinnerParameter("test slider:", "shuffle_test2", 0, 10, 5);
    Parameter[] parameters = {channelMask, channelObjects};
    Objects3DPopulation population;
    ImageHandler draw = null;
    
    ArrayList<Object3D> shu;

    boolean verbose;
    int nbCPUs = 1;

    @Override
    public void setMultithread(int nbCPUs) {
        this.nbCPUs = nbCPUs;
    }

    @Override
    public void setVerbose(boolean verbose) {
        this.verbose = verbose;
    }

//    public int setup(String arg, ImagePlus imp) {
//        plus = imp;
//
//        return PlugInFilter.DOES_16 + PlugInFilter.DOES_8G;
//    }
//
//    public void run(ImageProcessor ip) {
//        debug = true;
//        GenericDialog gd = Parameter.buildDialog("Shuffle", parameters);
//        gd.showDialog();
//        ang = gd.getNextNumber();
//        axe = gd.getNextChoiceIndex();
//        // test shuffle
//        ImageShort shu = new ImageShort("shuffle", plus.getWidth(), plus.getHeight(), plus.getStackSize());
//        shu.addValue(1);
//
//        if (debug) {
//            shuffle(plus, shu.getImagePlus()).show();
//        }
//    }

//    private ImagePlus shuffle(ImagePlus spotsplus, ImagePlus maskplus) {
//        Objects3DPopulation pop = new Objects3DPopulation();
//        pop.addImage(spotsplus);
//        //IntImage3D draw = new IntImage3D(plus.getWidth(), plus.getHeight(), plus.getStackSize());
//        draw = new ImageShort("draw", plus.getWidth(), plus.getHeight(), plus.getStackSize());
//        Object3D mask = new Object3DVoxels(maskplus, 1);
//        pop.setMask(mask);
//        Vector3D axis = new Vector3D();
//        if (axe == 0) {
//            axis.setCoord(1, 0, 0);
//        } else if (axe == 1) {
//            axis.setCoord(0, 1, 0);
//        } else if (axe == 2) {
//            axis.setCoord(0, 0, 1);
//        }
//        if (pop.shuffle(ang, axis)) {
//            pop.draw(draw);
//            //draw.show("test shuffle");
//            //draw = (ImageInt) FastFilters3D.filterIntImage(draw, FastFilters3D.CLOSEGRAY, 1.5f, 1.5f, 1.5f, Prefs.getThreads(), false);
//            //draw.filterGeneric(draw2, 1.5f, 1.5f, 1.5f, 0, draw.getSizez(), IntImage3D.FILTER_MAX);
//            //draw2.filterGeneric(draw, 1.5f, 1.5f, 1.5f, 0, draw.getSizez(), IntImage3D.FILTER_MIN);
//            return new ImagePlus("shuffle", draw.getImageStack());
//        } else {
//            return null;
//        }
//    }

    @Override
    public Parameter[] getParameters() {
        return parameters;
    }

    @Override
    public void initSampler(InputCellImages raw, SegmentedCellImages seg) {
        ImageInt spotPlus = seg.getImage(channelObjects.getIndex());//channelObjects.getImagePlus(seg, false);
        ImageInt maskPlus = seg.getImage(channelMask.getIndex());//channelMask.getImagePlus(seg, false);
        IJ.log("Shuffling objects");
        population = new Objects3DPopulation(spotPlus);
        Object3D mask = new Object3DVoxels(maskPlus, 1);
        population.setMask(mask);
        shu=population.shuffle();
        draw = spotPlus.createSameDimensions();
        for(Object3D O:shu){
            O.draw(draw);
        }
    }

    @Override
    public Object3D[] getSample() {
        return population.getObjectsArray();
    }

    @Override
    public String getHelp() {
        return "Shuffle the objects positions.";
    }

    @Override
    public void displaySample() {
        draw.show("Shuffle");
    }

}
