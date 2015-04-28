package tango.plugin.measurement.radialAnalysis;

import java.util.HashMap;
import mcib3d.geom.Object3D;
import mcib3d.geom.Object3DVoxels;
import mcib3d.image3d.ImageByte;
import mcib3d.image3d.ImageFloat;
import mcib3d.image3d.ImageHandler;
import tango.dataStructure.InputCellImages;
import tango.dataStructure.ObjectQuantifications;
import tango.dataStructure.SegmentedCellImages;
import tango.parameter.*;
import tango.plugin.measurement.MeasurementObject;

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

public class ExtendedVolumeFraction implements MeasurementObject {

    StructureParameter structure = new StructureParameter("Distance From structure:", "structure", 0, true);
    DoubleParameter distExtension = new DoubleParameter("Extension (µm)", "extension", 0.5d, Parameter.nfDEC2);
    Parameter[] parameters = new Parameter[]{structure, distExtension};
    KeyParameterObjectNumber extendeVolumeFraction = new KeyParameterObjectNumber("Extended Volume Fraction", "extendedVolumeFraction", "extendedVolumeFraction", true);
    KeyParameterObjectNumber extendeSignalFraction = new KeyParameterObjectNumber("Extended Signal Fraction", "extendedSignalFraction", "extendedSignalFraction", true);
    GroupKeyParameter group = new GroupKeyParameter("", "evfGroup", "", true, new KeyParameter[]{extendeVolumeFraction, extendeSignalFraction}, false);
    Parameter[] keys = new Parameter[]{group};
    int nCPUs=1;
    boolean verbose;
    
    public ExtendedVolumeFraction() {
        extendeVolumeFraction.setHelp("Volume of the extended region (dilated for a nuclear structure, or (1-eroded volume fraction) for the nucleus) divided by the nuclear volume", true);
        extendeSignalFraction.setHelp("Nuclear staining signal of the extended region (dilated for a nuclear structure, or (1-eroded volume fraction) for the nucleus) divided by the total nuclear signal", true);
    }
    
    @Override
    public int getStructure() {
        return 0;
    }

    @Override
    public void getMeasure(InputCellImages rawImages, SegmentedCellImages segmentedImages, ObjectQuantifications quantifications) {
        ImageFloat distanceMap = segmentedImages.getDistanceMap(structure.getIndex(), nCPUs);
        ImageHandler nuc = rawImages.getImage(0);
        ImageHandler nucMask = segmentedImages.getImage(0);
        double thld = this.distExtension.getDoubleValue(0.5);
        double volume = 0;
        double signal = 0;
        double extendedVolume = 0;
        double extendedSignal = 0;
        boolean doS = extendeSignalFraction.isSelected();
        ImageByte mask=null;
        if (verbose) mask = new ImageByte("Extended Structure", distanceMap.sizeX, distanceMap.sizeY, distanceMap.sizeZ);
        if (extendeVolumeFraction.isSelected() || doS) {
            double s=0;
            for (int z = 0; z<distanceMap.sizeZ; z++) {
                for (int xy = 0; xy<distanceMap.sizeXY; xy++) {
                    if (nucMask.getPixel(xy, z)!=0) {
                        volume++;
                        if (doS) {
                            s=nuc.getPixel(xy, z);
                            signal+=s;
                        }
                        if (distanceMap.pixels[z][xy]<=thld) {
                            extendedVolume++;
                            if (doS) extendedSignal+=s;
                            if (verbose) mask.pixels[z][xy]=(byte)255;
                        }
                    }
                }
            }
            if (verbose) mask.show();
            if (extendeVolumeFraction.isSelected()) {
                quantifications.setQuantificationObjectNumber(extendeVolumeFraction, new double[]{extendedVolume/volume});
            }
            if (doS) {
                quantifications.setQuantificationObjectNumber(extendeSignalFraction, new double[]{extendedSignal/signal});
            }
        }
    }

    @Override
    public Parameter[] getKeys() {
        return keys;
    }

    @Override
    public Parameter[] getParameters() {
        return parameters;
    }

    @Override
    public String getHelp() {
        return "Extended Volume/Signal Fraction of a structure. If the selected structure is the nucleus, then the peripheral layer of the nucleus is computed, otherwise the nuclear structure is dilated. This measurement computes the volume fraction or the nuclear signal fraction.";
    }

    @Override
    public void setVerbose(boolean verbose) {
        this.verbose= verbose;
    }

    @Override
    public void setMultithread(int nCPUs) {
        this.nCPUs=nCPUs;
    }

}
