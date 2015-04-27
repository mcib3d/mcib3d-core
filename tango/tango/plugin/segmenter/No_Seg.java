package tango.plugin.segmenter;

import ij.IJ;
import ij.ImagePlus;
import java.text.NumberFormat;
import mcib3d.image3d.ImageFloat;
import mcib3d.image3d.ImageHandler;
import mcib3d.image3d.ImageInt;
import mcib3d.image3d.ImageLabeller;
import tango.dataStructure.InputImages;
import tango.parameter.BooleanParameter;
import tango.parameter.ConditionalParameter;
import tango.parameter.DoubleParameter;
import tango.parameter.IntParameter;
import tango.parameter.Parameter;
import tango.parameter.ThresholdParameter;

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
public class No_Seg implements NucleusSegmenter, SpotSegmenter {

    ImagePlus myPlus;
    // DB
    boolean debug = false;
    int nbCPUs = 1;   
    Parameter[] par = {};

    @Override
    public Parameter[] getParameters() {       
        return par;
    }

    @Override
    public void setVerbose(boolean debug) {
        this.debug = debug;
    }

    // nucleiSegmenter
    @Override
    public ImageInt runNucleus(int currentStructureIdx, ImageHandler input, InputImages images) {      
        return (ImageInt) input;
    }

//    @Override
//    public int[] getTags() {
//        return null;
//    }
    @Override
    public void setMultithread(int nbCPUs) {
        this.nbCPUs = nbCPUs;
    }

    // spotSegmenter
    @Override
    public ImageInt runSpot(int currentStructureIdx, ImageHandler input, InputImages images) {
        return (ImageInt) input;
    }

   

    @Override
    public ImageFloat getProbabilityMap() {
        return null;
    }

    @Override
    public String getHelp() {
        return "Does nothing, when input image is already labelled.";
    }
}
