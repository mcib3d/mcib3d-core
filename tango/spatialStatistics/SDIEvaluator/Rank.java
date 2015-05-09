package tango.spatialStatistics.SDIEvaluator;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import mcib3d.utils.ArrayUtil;
import mcib3d.utils.CDFTools;
import tango.dataStructure.StructureQuantifications;
import tango.parameter.*;
import tango.plugin.measurement.Measurement;
import tango.plugin.measurement.MeasurementStructure;
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
public class Rank implements SDIEvaluator {
    KeyParameterStructureNumber sdi_P = new KeyParameterStructureNumber("SDI:", "sdi", "sdi", true);
    KeyParameter[] keys = new KeyParameter[] {sdi_P};
    int nCPUs=1;
    boolean verbose;
    @Override
    public void setMultithread(int nbCPUs) {
        this.nCPUs=nbCPUs;
    }
    
    @Override
    public void setVerbose(boolean verbose) {
        this.verbose=verbose;
    }
    @Override
    public Parameter[] getParameters() {
        return new Parameter[]{};
    }

    @Override
    public KeyParameter[] getKeyParameters() {
        return keys;
    }

    @Override
    public void eval(double[] observedDescriptor, double[][] sampledDescriptor, StructureQuantifications quantifs) {
         if (!sdi_P.isSelected() || observedDescriptor==null || sampledDescriptor==null || observedDescriptor.length==0 || sampledDescriptor.length==0) return;
        //sdi
        
        //Order[] values = new Order[descriptor.length];
        double[] values = new double[sampledDescriptor.length+1];
        //for (int i = 0; i<values.length; i++) values[i]=new Order(descriptor[i][0]);
        //Order observed = values[0];
        for (int i = 0; i<sampledDescriptor.length; i++) values[i]=sampledDescriptor[i][0];
        values[sampledDescriptor.length]=observedDescriptor[0];
        Arrays.sort(values);
        int i = 0;
        double observed = observedDescriptor[0];
        while(i<values.length && values[i]<observed) i++;
        int j=i+1;
        while(j<values.length && values[j]==observed) j++;
        j--;
        //res.put(sdi_P.getKey(), (double)Arrays.binarySearch(values, observed)/(double)descriptor.length);
        double sdi = 1.0 - ((double)(i+j)/2)/(double) values.length; // 1.0 - ??
        if (verbose) {
            ij.IJ.log("Spatial Statistics: rank of:"+observedDescriptor[0]+" within:"+sampledDescriptor.length+" values is between:"+i + " and "+j + " sdi:"+sdi);
        }
        quantifs.setQuantificationStructureNumber(sdi_P, sdi);
    }

    @Override
    public String getHelp() {
        return "";
    }
    
    private class Order implements java.lang.Comparable<Order>{
        double value;
        private Order(double value) {
            this.value=value;
        }

        @Override
        public int compareTo(Order t) {
            if (value<t.value) return -1;
            else if (value>t.value) return 1;
            else return 0;
        }
    }
    
}
