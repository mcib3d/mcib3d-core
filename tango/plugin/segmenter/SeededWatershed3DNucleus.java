package tango.plugin.segmenter;

import ij.IJ;
import java.util.*;
import mcib3d.geom.Object3DVoxels;
import mcib3d.geom.Voxel3D;
import mcib3d.image3d.*;
import mcib3d.utils.ThreadRunner;
import mcib3d.utils.exceptionPrinter;
import tango.dataStructure.InputCellImages;
import tango.dataStructure.InputImages;
import tango.gui.Core;
import tango.parameter.*;
import tango.plugin.filter.GaussianFit;
import tango.plugin.filter.Structure;
import tango.plugin.segmenter.SpotSegmenter;
import tango.util.ImageUtils;

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
public class SeededWatershed3DNucleus implements NucleusSegmenter  {
    //int volumeMin=10;
    //float fusionCoeff=0.5f;
    double gradientScale = 1;
    int volumeDyn=5;
    int volumeDyn2=5;
    double dynamics=0.01;
    PreFilterParameter watershedMap_P = new PreFilterParameter("Watershed Map:", "watershedMap", "Image Features", new Parameter[]{new ChoiceParameter("", "", new String[]{"Gradient Magnitude"}, "Gradient Magnitude")}); 
    //DoubleParameter gradientScale_P = new DoubleParameter("Graident Scale:", "gradientScale", gradientScale, Parameter.nfDEC2);
    //static String[] dyn = new String[]{"no dynamics", "volume dynamics", "dynamics", "volume dynamics & dynamics", "Dynamics (volume constraint)", "volume dynamics & dynamics (volume constraint)"};
    BooleanParameter useDynamic = new BooleanParameter("Use Dynamics", "useDyn", false);
    BooleanParameter useVolumeDynamic = new BooleanParameter("Volume Dynamic", "useVolDyn", false);
    BooleanParameter useHeightDynamic = new BooleanParameter("Dynamics", "useHeightDyn", false);
    BooleanParameter useVolumeConstraint = new BooleanParameter("Volume constraint:", "useVolumeConstraint", true);
    //ChoiceParameter dynCond = new ChoiceParameter("Regional Minima Dynamics:", "dynamicsCond", dyn, dyn[0]);
    IntParameter volumeDyn_P = new IntParameter("Volume Dynamics: ", "volumeDyn", volumeDyn);
    IntParameter volumeConstraint_P = new IntParameter("Volume constraint: ", "volumeConstraint", volumeDyn2);
    SliderDoubleParameter dynamics_P = new SliderDoubleParameter("Dynamics:", "dynamics", 0, 0.2,  (double)dynamics, 3);
    ConditionalParameter volumDynCond = new ConditionalParameter(useVolumeDynamic);
    ConditionalParameter dynCond = new ConditionalParameter(useDynamic);
    ConditionalParameter heightDynCond = new ConditionalParameter(useHeightDynamic);
    ConditionalParameter volumeConstraintCond = new ConditionalParameter(useVolumeConstraint);
    
    Parameter[] parameters;

    boolean debug;
    int nCPUs=1;
    int sign=1;
    boolean dyn, heightDyn, volDyn, volumeDynConst;
    double maxDynamics, maxDynamicsGrad;

    WatershedTransform3D wsTransform;
    
    public SeededWatershed3DNucleus() {
        //gradientScale_P.setHelp("gradient scale, in pixels", true);
        dynCond.setCondition(true, new Parameter[]{volumDynCond, heightDynCond});
        volumDynCond.setCondition(true, new Parameter[]{volumeDyn_P});
        heightDynCond.setCondition(true, new Parameter[]{dynamics_P, volumeConstraintCond});
        volumeConstraintCond.setCondition(true, new Parameter[]{volumeConstraint_P});
        dynamics_P.setHelp("The dynamics of a regional minima is the minimum height (normalized) a pixel has to climb in a walk to reach another regional minima with a lower value", true);
        volumeDyn_P.setHelp("The volume-dynamics is the minimum volume a catchment basin has to raise to reach another regional minima", true);
        volumeConstraint_P.setHelp("Maximum volume of a catchment basin to reach another regional minima. This constraint limits the dynamics constraint", true);
        //useHessianCond.setCondition(true, new Parameter[]{hessianScale, hessianThld_P});
        //hessianScale.setHelp("Integration scale for Hessian transform", true);
        //hessianThld_P.setHelp("Constraint applied after watershed transform: The 0.05-quantile of hessian value of each spot's voxels has to be inferior to this threshold", true);
        //intensityThld_P.setHelp("Constraint applied after watershed transform: The 0.9-quantile of intensity value of each spot's voxels has to be superior to this threshold", debug);
        //volumDynCond.toggleVisibility(false);
    }
    
    @Override
    public String getHelp() {
        return"<ul><strong>Seeded Watershed 3D</strong>"
                + "<li>Uses a user-defined propagation map (default is gradient magnitude). </li>"
                + "<li>Seeds regional minima of the propagation map. </li>"
                + "<li>Runs until the background threshold. </li>"
                +"<li>Split the whole image (within mask space) into regions. Some regions will have to be erased during post-processing</li>"
                +"<li>May produce over-segmentation: to overcome this problem: use pre-filter to reduce noise, increase gradient scale, or use a merging algorithm as post-filter</li>";
    }
    
    protected void setDynamicsParameters() {
        this.volumeDyn=volumeDyn_P.getIntValue(volumeDyn);
        this.volumeDyn2=volumeConstraint_P.getIntValue(volumeDyn2);
        this.dynamics=dynamics_P.getValue();
        this.dyn=useDynamic.getValue();
        this.heightDyn=this.useHeightDynamic.getValue();
        this.volDyn=this.useVolumeDynamic.getValue();
        this.volumeDynConst=this.useVolumeConstraint.getValue();
        wsTransform.setDynamics(dyn, volDyn, volumeDyn, heightDyn, dynamics, volumeDynConst, volumeDyn2);
    }
    
    @Override
    public void setVerbose(boolean debug) {
        this.debug=debug;
    }

    @Override
    public void setMultithread(int nCPUs) {
        this.nCPUs=nCPUs;
    }


    @Override
    public Parameter[] getParameters() {
        return new Parameter[]{this.watershedMap_P}; //dynCond
    }

    @Override
    public ImageInt runNucleus(int currentStructureIdx, ImageHandler input, InputImages rawImages) {
        wsTransform = new WatershedTransform3D(nCPUs, debug);
        setDynamicsParameters();
        ImageHandler wsmap = watershedMap_P.preFilter(0, input, rawImages, nCPUs, debug);
        if (debug) wsmap.showDuplicate("Watershed Map");
        return wsTransform.runWatershed(input, wsmap, rawImages.getMask());
    }

//    @Override
//    public int[] getTags() {
//        return null;
//    }

}
