package tango.plugin.thresholder;

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
public class ThresholderOperation implements Thresholder {
    
    ThresholdParameter threshold1 = new ThresholdParameter("Threshold method 1:", "thld1", null);
    ThresholdParameter threshold2 = new ThresholdParameter("Threshold method 2:", "thld2", null);
    DoubleParameter coeff = new DoubleParameter("Coefficient:", "coeff", 0.5d, Parameter.nfDEC3);
    static String[] methods = new String[]{"Max", "Min", "Mean"};
    ChoiceParameter operation = new ChoiceParameter("Operation:", "operation", methods, methods[0]); 
    HashMap<Object, Parameter[]> map = new HashMap<Object, Parameter[]>(){{
        put(methods[0], new Parameter[]{}); 
        put(methods[1], new Parameter[]{});
        put(methods[2], new Parameter[]{coeff});
    }};
    ConditionalParameter cond= new ConditionalParameter(operation, map);
    Parameter[] parameters= new Parameter[]{threshold1, threshold2, cond};
    
    public ThresholderOperation() {
        operation.setHelp("Choose operation performed on computed thresholds", true);
        coeff.setHelp("returned threshold is a ponderated mean value of thld1 & thld2: thld1 * (1-coeff) + thld2*coeff", true);
    }
    
    boolean debug;
    int nbCPUs=1;
    @Override
    public void setMultithread(int nCPUs) {
        this.nbCPUs=nCPUs;
    }

    @Override
    public void setVerbose(boolean debug) {
        this.debug=debug;
    }

    @Override
    public double runThresholder(ImageHandler input, InputImages images) {
        double thld1=threshold1.getThreshold(input, images, nbCPUs, debug);
        double thld2=threshold2.getThreshold(input, images, nbCPUs, debug);
        if (debug) ij.IJ.log("thld1:"+thld1+ " thld2:"+thld2);
        if (operation.getSelectedItem().equals(methods[0])) {
            return Math.max(thld2, thld1);
        } else if (operation.getSelectedItem().equals(methods[1])) {
            return Math.min(thld2, thld1);
        } else if (operation.getSelectedItem().equals(methods[1])) {
            double coeffd=coeff.getDoubleValue(0.5d);
            return thld1*(1-coeffd)+thld2*coeffd;
        } else return thld1;
    }

    @Override
    public Parameter[] getParameters() {
        return parameters;
    }

    @Override
    public String getHelp() {
        return "Performs and operation on values computed by selected threshold methods";
    }
    
}
