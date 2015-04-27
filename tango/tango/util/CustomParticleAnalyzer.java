package tango.util;
import ij.plugin.filter.*;
import ij.process.*;
import ij.measure.*;
import ij.gui.*;
import java.util.*;
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
public class CustomParticleAnalyzer extends ParticleAnalyzer {
    public ArrayList<Roi> rois;

    public CustomParticleAnalyzer(int options, int measurements, ResultsTable rt, double minSize, double maxSize, double cmin, double cmax){
         super(options, measurements, rt, minSize, maxSize , cmin, cmax);
         this.rois=new ArrayList<Roi>();
    }

    @Override
    protected void saveResults(ImageStatistics stats, Roi roi) {
        //Roi r = (PolygonRoi)roi.clone();
        Roi r = new PolygonRoi(roi.getConvexHull(), 4);
        rois.add(r);
    }
}
