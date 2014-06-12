package tango.plugin.filter;

import java.util.HashMap;
import mcib3d.image3d.ImageHandler;
import tango.dataStructure.InputImages;
import tango.parameter.ChoiceParameter;
import tango.parameter.ConditionalParameter;
import tango.parameter.Parameter;

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
public class HistogramTransformation implements PreFilter {
    boolean debug;
    int nbCPUs=1;
    static String[] transfo = new String[]{"Invert", "Normalize", "Scale"}; //"Size Normalization"
    ChoiceParameter transfo_P = new ChoiceParameter("Choose Transformation:", "transformation", transfo, transfo[1]);
    SizeNormalization sn = new SizeNormalization();
    Invert invert = new Invert();
    Normalize normalize =new Normalize();
    Scale scale =new Scale();
    HashMap<Object, Parameter[]> map = new HashMap<Object, Parameter[]>(){{
        put(transfo[0], invert.getParameters()); 
        put(transfo[1], normalize.getParameters());
        put(transfo[2], scale.getParameters());
        //put(transfo[3], sn.getParameters());
    }};
    ConditionalParameter cond = new ConditionalParameter(transfo_P, map);
    
    @Override
    public ImageHandler runPreFilter(int currentStructureIdx, ImageHandler input, InputImages images) {
        if (transfo_P.getSelectedItem().equals(transfo[0])) {
            invert.setVerbose(debug);
            invert.setMultithread(nbCPUs);
            return invert.runPreFilter(currentStructureIdx, input, images);
        } else if (transfo_P.getSelectedItem().equals(transfo[1])) {
            normalize.setVerbose(debug);
            normalize.setMultithread(nbCPUs);
            return normalize.runPreFilter(currentStructureIdx, input, images);
        } else if (transfo_P.getSelectedItem().equals(transfo[2])) {
            scale.setVerbose(debug);
            scale.setMultithread(nbCPUs);
            return scale.runPreFilter(currentStructureIdx, input, images);
        } else if (transfo_P.getSelectedItem().equals(transfo[3])) {
            sn.setVerbose(debug);
            sn.setMultithread(nbCPUs);
            return sn.runPreFilter(currentStructureIdx, input, images);
        } else return input;
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
        return new Parameter[]{cond};
    }

    @Override
    public String getHelp() {
        return "Histogram Transformations";
    }
    
}
