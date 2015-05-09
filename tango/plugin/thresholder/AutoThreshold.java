package tango.plugin.thresholder;

import tango.parameter.ChoiceParameter;
import tango.parameter.Parameter;
import ij.ImagePlus;
import ij.process.AutoThresholder;
import mcib3d.image3d.ImageHandler;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import mcib3d.image3d.ImageInt;
import mcib3d.image3d.ImageStats;
import tango.dataStructure.InputCellImages;
import tango.dataStructure.InputImages;
import tango.plugin.thresholder.Thresholder;

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
public class AutoThreshold implements Thresholder, ThresholderHistogram {
    boolean debug;
    int nCPUs=1;
    public static Map<String, AutoThresholder.Method> methods = Collections.unmodifiableMap(new HashMap<String, AutoThresholder.Method>() {{
        put("ISODATA", AutoThresholder.Method.IsoData);
        put("OTSU", AutoThresholder.Method.Otsu);
        put("MINIMUM", AutoThresholder.Method.Minimum);
        put("MINERROR", AutoThresholder.Method.MinError);
        put("MOMENTS", AutoThresholder.Method.Moments);
        put("INTERMODES", AutoThresholder.Method.Intermodes);
        put("YEN", AutoThresholder.Method.Yen);
        put("TRIANGLE", AutoThresholder.Method.Triangle);
        put("MEAN", AutoThresholder.Method.Mean);
        put("RENYIENTROPY", AutoThresholder.Method.RenyiEntropy);
        put("SHANBHAG", AutoThresholder.Method.Shanbhag);
        put("TRIANGLE", AutoThresholder.Method.Triangle);
        put("YEN", AutoThresholder.Method.Yen);
        put("HUANG", AutoThresholder.Method.Huang);
        put("PERCENTILE", AutoThresholder.Method.Percentile);
        put("MAXENTROPY", AutoThresholder.Method.MaxEntropy);
        put("IJ_ISODATA", AutoThresholder.Method.IJ_IsoData);
    }});
    
    private Parameter[] parameters=new Parameter[] {
        new ChoiceParameter("Method: ", "AutoThresholdMethod", methods.keySet(), "OTSU")
    };

    @Override
    public Parameter[] getParameters() {
        return parameters;
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
    public double runThresholder(ImageHandler input, InputImages images) {
        AutoThresholder.Method method = methods.get(((ChoiceParameter)parameters[0]).getSelectedItem());
        return run(input, (images!=null)?images.getMask():null, method);
    }
    
    public static double run(ImageHandler in, ImageInt mask, AutoThresholder.Method method ) {
        ImageStats s = in.getImageStats(mask);
        return run(s.getHisto256(), s.getHisto256BinSize(), s.getMin(), method);
    }
    
    public static double run(int[] histo, double binSize, double min,  AutoThresholder.Method method) {
        AutoThresholder at = new AutoThresholder();
        double thld = at.getThreshold(method, histo);
        return thld*binSize+min;
    }

    @Override
    public String getHelp() {
        return "ImageJ's autoThreshold plugin";
    }

    @Override
    public double getThreshold(int[] histogram, double binSize, double min) {
        AutoThresholder.Method method = methods.get(((ChoiceParameter)parameters[0]).getSelectedItem());
        return run(histogram, binSize, min, method);
    }

}
