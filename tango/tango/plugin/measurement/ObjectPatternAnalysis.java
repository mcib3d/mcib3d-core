package tango.plugin.measurement;

import ij.gui.Plot;
import java.util.HashMap;
import mcib3d.geom.Object3DSurface;
import mcib3d.geom.Object3DVoxels;
import mcib3d.image3d.ImageByte;
import mcib3d.image3d.ImageHandler;
import mcib3d.image3d.ImageInt;
import tango.dataStructure.InputCellImages;
import tango.dataStructure.SegmentedCellImages;
import tango.dataStructure.StructureQuantifications;
import tango.gui.Core;
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
public class ObjectPatternAnalysis implements MeasurementStructure {

    boolean verbose;
    int nbCPUs = 1;
    StructureParameter structure = new StructureParameter("Structure:", "structure", -1, true);
    Parameter[] parameters = new Parameter[]{structure};
    KeyParameterStructureNumber convexHullVolume = new KeyParameterStructureNumber("Convex Hull Volume:", "convexHullVolume", "convexHullVolume", true);
    KeyParameterStructureNumber convexHullVolumeRatio = new KeyParameterStructureNumber("Convex Hull Volume Ratio:", "convexHullVolumeRatio", "convexHullVolumeRatio", true);
    KeyParameterStructureNumber massCenterDistance = new KeyParameterStructureNumber("Mass Center Distance to Nucleus centrer:", "massCenterDistance", "massCenterDistance", true);
    KeyParameterStructureNumber dispersion = new KeyParameterStructureNumber("Dispersion:", "dispersion", "dispersion", true);
    GroupKeyParameter key = new GroupKeyParameter("", "objectPatternDescriptors", "", true, new KeyParameter[]{massCenterDistance, dispersion, convexHullVolume, convexHullVolumeRatio}, false);

    @Override
    public int[] getStructures() {
        return new int[]{structure.getIndex()};
    }

    @Override
    public void getMeasure(InputCellImages rawImages, SegmentedCellImages segmentedImages, StructureQuantifications quantifs) {
        Object3DVoxels[] objects = segmentedImages.getObjects(structure.getIndex());
        Object3DVoxels nuc = segmentedImages.getObjects(0)[0];
        double[] centerNuc = nuc.getCenterAsArray();
        double cx = 0, cy = 0, cz = 0, sx = 0, sy = 0, sz = 0;
        for (Object3DVoxels o : objects) {
            double[] center = o.getCenterAsArray();
            double dx = (center[0] - centerNuc[0]);
            cx += dx;
            sx += dx * dx;
            double dy = (center[1] - centerNuc[1]);
            cy += dy;
            sy += dy * dy;
            double dz = (center[2] - centerNuc[2]);
            cz += dz;
            sz += dz * dz;
        }
        if (objects.length > 0) {
            cx /= (double) objects.length;
            cy /= (double) objects.length;
            cz /= (double) objects.length;
            sx = sx / (double) objects.length - cx * cx;
            sy = sy / (double) objects.length - cy * cy;
            sz = sz / (double) objects.length - cz * cz;
            double s = Math.sqrt(sx + sy + sz);
            double cDist = Math.sqrt(cx * cx + cy * cy + cz * cz);
            quantifs.setQuantificationStructureNumber(dispersion, s);
            quantifs.setQuantificationStructureNumber(massCenterDistance, cDist);
        }

        if (convexHullVolume.isSelected()) {

            ImageByte im = new ImageByte("convexhull mask", rawImages.getMask().sizeX, rawImages.getMask().sizeY, rawImages.getMask().sizeZ);
            for (Object3DVoxels o : objects) {
                o.draw(im, 255);
            }
            im.setScale(nuc.getResXY(), nuc.getResZ(), nuc.getUnits());
            Object3DVoxels obj = (im.getObjects3D()[0]).getConvexObject(nbCPUs > 1);
//            Object3DVoxels obj = im.getObjects3D()[0];
//            Object3DSurface surf = new Object3DSurface(obj.computeMeshSurface(false));
//            surf.setCalibration(nuc.getCalibration());
//            surf = surf.getConvexObject();
//            surf.multiThread = false;
//            obj = new Object3DVoxels(surf.getVoxels());
//            obj.setCalibration(nuc.getCalibration());
            if (verbose) {
                im.showDuplicate("objects");
            }
            obj.draw(im, 255);
            if (verbose) {
                im.show();
            }
            if (convexHullVolume.isSelected()) {
                quantifs.setQuantificationStructureNumber(convexHullVolume, obj.getVolumeUnit());
            }
            if (convexHullVolumeRatio.isSelected()) {
                quantifs.setQuantificationStructureNumber(convexHullVolumeRatio, obj.getVolumeUnit() / nuc.getVolumeUnit());
            }
        }

    }

    @Override
    public Parameter[] getKeys() {
        return new Parameter[]{key};
    }

    @Override
    public Parameter[] getParameters() {
        return parameters;
    }

    @Override
    public void setMultithread(int nbCPUs) {
        this.nbCPUs = nbCPUs;
    }

    @Override
    public void setVerbose(boolean verbose) {
        this.verbose = verbose;
    }

    @Override
    public String getHelp() {
        return "3D Signal Autocorrelation coefficient, normalized by mean and variance of signal. See http://en.wikipedia.org/wiki/Autocorrelation";
    }
}
