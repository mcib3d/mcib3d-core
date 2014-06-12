package tango.plugin.filter;

import java.util.HashMap;
import mcib3d.image3d.ImageHandler;
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
public class ImageFeatures implements PreFilter {
    boolean debug;
    int nbCPUs;
    static String[] features = new String[]{"Gradient Magnitude", "Hessian", "Structure"}; //, "Euclidean Distance Map", "Local Thickness"
    ChoiceParameter feature_P = new ChoiceParameter("Choose Feature:", "feature", features, features[0]);
    Structure structure= new Structure();
    GradientMagnitude gradient = new GradientMagnitude();
    Hessian hessian = new Hessian();
    //EuclideanDistanceMap edm = new EuclideanDistanceMap();
    //LocalThickness lt = new LocalThickness();
    HashMap<Object, Parameter[]> map = new HashMap<Object, Parameter[]>(){{
        put(features[0], gradient.getParameters()); 
        put(features[1], hessian.getParameters());
        put(features[2], structure.getParameters());
        //put(features[3], edm.getParameters());
        //put(features[4], lt.getParameters());
    }};
    ConditionalParameter cond = new ConditionalParameter(feature_P, map);
    BooleanParameter usePreFilter = new BooleanParameter("Perform Pre-Filter", "usePreFilter", false);
    PreFilterParameter preFilter = new PreFilterParameter("Pre-Filter:", "preFilter", "Fast_Filters3D", new Parameter[]{new ChoiceParameter("Choose Filter: ", "filter", new String[]{"Median"}, "Median"), new DoubleParameter("VoisXY: ", "voisXY", 2d, Parameter.nfDEC1), new DoubleParameter("VoisZ: ", "voisZ", 1d, Parameter.nfDEC1)});
    ConditionalParameter condPreFilter = new ConditionalParameter(usePreFilter);
    BooleanParameter usePostFilter = new BooleanParameter("Perform Post-Filter", "usePostFilter", false);
    PreFilterParameter postFilter = new PreFilterParameter("Post-Filter:", "postFilter", "Fast_Filters3D", new Parameter[]{new ChoiceParameter("Choose Filter: ", "filter", new String[]{"Median"}, "Median"), new DoubleParameter("VoisXY: ", "voisXY", 2d, Parameter.nfDEC1), new DoubleParameter("VoisZ: ", "voisZ", 1d, Parameter.nfDEC1)});
    ConditionalParameter condPostFilter = new ConditionalParameter(usePostFilter);
    
    public ImageFeatures() {
        condPreFilter.setCondition(true, new Parameter[]{preFilter});
        condPostFilter.setCondition(true, new Parameter[]{postFilter});
    }
    
    @Override
    public ImageHandler runPreFilter(int currentStructureIdx, ImageHandler input, InputImages images) {
        if (usePreFilter.isSelected()) {
            input = preFilter.preFilter(currentStructureIdx, input, images, nbCPUs, debug);
        }
        if (feature_P.getSelectedItem().equals(features[0])) {
            gradient.setVerbose(debug);
            gradient.setMultithread(nbCPUs);
            input= gradient.runPreFilter(currentStructureIdx, input, images);
        } else if (feature_P.getSelectedItem().equals(features[1])) {
            hessian.setVerbose(debug);
            hessian.setMultithread(nbCPUs);
            input = hessian.runPreFilter(currentStructureIdx, input, images);
        } else if (feature_P.getSelectedItem().equals(features[2])) {
            structure.setVerbose(debug);
            structure.setMultithread(nbCPUs);
            input = structure.runPreFilter(currentStructureIdx, input, images);
        } /*else if (feature_P.getSelectedItem().equals(features[3])) {
            edm.setVerbose(debug);
            edm.setMultithread(nbCPUs);
            return edm.runPreFilter(currentStructureIdx, input, images);
        } else if (feature_P.getSelectedItem().equals(features[4])) {
            lt.setVerbose(debug);
            lt.setMultithread(nbCPUs);
            return lt.runPreFilter(currentStructureIdx, input, images);
        } 
        * 
        */
        if (usePostFilter.isSelected()) {
            input = postFilter.preFilter(currentStructureIdx, input, images, nbCPUs, debug);
        }
        return input;
    }

    @Override
    public void setVerbose(boolean debug) {
        this.debug=debug;
    }

    @Override
    public void setMultithread(int nbCPUs) {
        this.nbCPUs=nbCPUs;
    }

    @Override
    public Parameter[] getParameters() {
        return new Parameter[]{cond, condPreFilter, condPostFilter};
    }

    @Override
    public String getHelp() {
        return "Compute Features from Image";
    }
    
}
