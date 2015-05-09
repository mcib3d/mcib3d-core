package tango.plugin.filter;

import mcib3d.image3d.ImageHandler;
import mcib3d.image3d.ImageInt;
import tango.dataStructure.InputImages;
import tango.parameter.Parameter;
import tango.processing.geodesicDistanceMap.GeodesicMap;
import tango.processing.geodesicDistanceMap.GrayscaleGeodesicMap;

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
public class GeodesicDistanceMap implements PreFilter {
    GrayscaleGeodesicMap map = new GrayscaleGeodesicMap();
    boolean debug;
    int nbCPUs=1;
    @Override
    public ImageHandler runPreFilter(int currentStructureIdx, ImageHandler input, InputImages images) {
        map.setIntensity(input);
        map.init(images, nbCPUs, debug);
        //ImageInt mask = images.getMask();
        //map.run(mask.getObjects3D(null, 0), false, true);
        map.runGrayscale();
        return map.getDistanceMap();
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
        return map.getParametersPreFilter();
    }

    @Override
    public String getHelp() {
        return "Geodesic Distance Map";
    }
}
