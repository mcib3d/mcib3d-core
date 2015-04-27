
package tango.spatialStatistics.SDIEvaluator.filter1d;

import tango.parameter.DoubleParameter;
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
public class BandPass2 extends Filter1d {
    double xc=1;
    double q = 0.5;
    DoubleParameter cutoff = new DoubleParameter("Cuttoff Distance: (unit)", "cutoff", xc, Parameter.nfDEC5);
    DoubleParameter qFactor = new DoubleParameter("Quality Factor:", "qFactor", q, Parameter.nfDEC5);
    public BandPass2() {
        
    }
    @Override
    public double getValue(double x) {
        if (x==0) return 0;
        double r = x/xc;
        r=r*r;
        return 1/Math.sqrt(1+q*Math.pow(r-1/r, 2));
    }

    @Override
    public Parameter[] getParameters() {
        return new Parameter[]{cutoff, qFactor};
    }

    @Override
    public String getHelp() {
        return "Second Order Low-Pass Filter";
    }

    @Override
    public void initFilter() {
        xc=cutoff.getDoubleValue(xc);
        q=qFactor.getDoubleValue(q);
        q=q*q;
    }
    
}
