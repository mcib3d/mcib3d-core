/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package tango.plugin.filter;

import ij.IJ;
import java.util.HashMap;
import mcib3d.geom.Object3D;
import mcib3d.geom.Voxel3D;
import mcib3d.image3d.ImageInt;
import tango.dataStructure.InputCellImages;
import tango.dataStructure.InputImages;
import tango.parameter.BooleanParameter;
import tango.parameter.ConditionalParameter;
import tango.parameter.IntParameter;
import tango.parameter.Parameter;
import tango.parameter.SliderParameter;

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
public class SizeFilter implements PostFilter {

    boolean debug;
    int nbCPUs = 1;
    int min, max, edgeXY, edgeZ, limX, limY, limZ;
    IntParameter minVox = new IntParameter("Min Voxels:", "minVox", 1);
    IntParameter maxVox = new IntParameter("Max Voxels:", "maxVox", null);
    BooleanParameter max_P = new BooleanParameter("Use constraint on maximum size", "useMaw", false);
    BooleanParameter edge_PXY = new BooleanParameter("Remove Objects touching edges XY", "edgeXY", false);
    BooleanParameter edge_PZ = new BooleanParameter("Remove Objects touching edges Z", "edgeZ", false);
    IntParameter edgeSurf_PXY = new IntParameter("Min nb of Voxels touching XY edges:", "edgeSurfXY", 1);
    IntParameter edgeSurf_PZ = new IntParameter("Min nb of Voxels touching Z edges:", "edgeSurfZ", 1);
   // BooleanParameter outside = new BooleanParameter("Delete outside structures", "outside", false);
    //SliderParameter minPc = new SliderParameter("Min % coloc to remove", "coloc", 1, 100, 50);
    HashMap<Object, Parameter[]> mapXY = new HashMap<Object, Parameter[]>() {
        {
            put(true, new Parameter[]{edgeSurf_PXY});
        }
    };
    HashMap<Object, Parameter[]> mapZ = new HashMap<Object, Parameter[]>() {
        {
            put(true, new Parameter[]{edgeSurf_PZ});
        }
    };
    ConditionalParameter edgeCondXY = new ConditionalParameter(edge_PXY, mapXY);
    ConditionalParameter edgeCondZ = new ConditionalParameter(edge_PZ, mapZ);
    HashMap<Object, Parameter[]> map2 = new HashMap<Object, Parameter[]>() {
        {
            put(true, new Parameter[]{maxVox});
        }
    };
//    HashMap<Object, Parameter[]> mapOut = new HashMap<Object, Parameter[]>() {
//        {
//            put(true, new Parameter[]{minPc});
//        }
//    };
    ConditionalParameter maxCond = new ConditionalParameter(max_P, map2);
   //ConditionalParameter outParam = new ConditionalParameter(outside, mapOut);
    Parameter[] parameters = new Parameter[]{minVox, maxCond, edgeCondXY, edgeCondZ};

    public SizeFilter() {
        minVox.setHelp("if an objects has less voxel than this value, it is erased", false);
        maxVox.setHelp("if an objects has more voxel than this value, it is erased. \nleave blank or 0 for no maximum value", false);
        maxVox.setCompulsary(false);
        edge_PXY.setHelp("Pixels touching the border of the image (XY border) will be erased", false);
        edge_PXY.setHelp("Pixels touching the border of the image (Z border) will be erased", false);
        edgeSurf_PXY.setHelp("Minimum number of edge-touching voxel per objects: if the objects too few voxels touching the edges, it won't be erased", false);
        edgeSurf_PZ.setHelp("Minimum number of edge-touching voxel per objects: if the objects too few voxels touching the edges, it won't be erased", false);
        //outside.setHelp("Delete objects falling outside containing structure (nucleus)", false);
        //minPc.setHelp("Minimum % of structure colocalisation outside nucleus to remove it", false);
    }

    @Override
    public ImageInt runPostFilter(int currentStructureIdx, ImageInt in, InputImages images) {
        Object3D[] objects = in.getObjects3D();
        edgeXY = edgeSurf_PXY.getIntValue(1);
        if (edgeXY < 1) {
            edgeXY = 1;
        }
        edgeZ = edgeSurf_PZ.getIntValue(1);
        if (edgeZ < 1) {
            edgeZ = 1;
        }

        min = minVox.getIntValue(1);
        max = maxVox.getIntValue(0);
        if (max <= min) {
            max = 0;
        }
        limX = in.sizeX - 1;
        limY = in.sizeY - 1;
        limZ = in.sizeZ - 1;
        boolean useMax = max_P.getValue();
        if (max <= min) {
            useMax = false;
        }
        // NUCLEI
        if (images instanceof InputCellImages) {
            // IJ.log("Input Cell images");
            ImageInt mask = ((InputCellImages) images).getMask();
            for (Object3D o : objects) {
                if (o.getVolumePixels() < min || (max > 0 && o.getVolumePixels() > max)) {
                    in.draw(o, 0);
                } else if (edge_PXY.isSelected()) {
                    int count = 0;
                    for (Voxel3D v : o.getContours()) {
                        if (isOutsideMaskXY(v, mask)) {
                            count++;
                        }
                        if (count >= edgeXY) {
                            break;
                        }
                    }
                    if (count >= edgeXY) {
                        // IJ.log("Touch XY");
                        in.draw(o, 0);
                    }
                } else if (edge_PZ.isSelected()) {
                    int count = 0;
                    for (Voxel3D v : o.getContours()) {
                        if (isOutsideMaskZ(v, mask)) {
                            count++;
                        }
                        if (count >= edgeZ) {
                            break;
                        }
                    }
                    if (count >= edgeZ) {
                        in.draw(o, 0);
                        //  IJ.log("Touch Z");
                    }
                }
            }
            // OBJECTS
        } else {
            for (Object3D o : objects) {
                if (o.getVolumePixels() < min || (useMax && o.getVolumePixels() > max)) {
                    in.draw(o, 0);
                    //EDGE XY
                } else if (edge_PXY.isSelected() && (o.getXmax() == limX || o.getXmin() == 0 || o.getYmax() == limY || o.getYmin() == 0)) {
                    if (edgeXY > 1) {
                        //count touching voxels
                        int count = 0;
                        for (Voxel3D v : o.getContours()) {
                            if (v.getRoundX() == 0 || v.getRoundX() == limX || v.getRoundY() == 0 || v.getRoundY() == limY) {
                                count++;
                            }
                            if (count >= edgeXY) {
                                break;
                            }
                        }
                        if (count >= edgeXY) {
                            in.draw(o, 0);
                        }
                    } else {
                        in.draw(o, 0);
                    }
                    // EDGEZ
                } else if (edge_PZ.isSelected() && (o.getZmax() == limZ || o.getZmin() == 0)) {
                    if (edgeZ > 1) {
                        //count touching voxels
                        int count = 0;
                        for (Voxel3D v : o.getContours()) {
                            if (v.getRoundZ() == 0 || v.getRoundZ() == limZ) {
                                count++;
                            }
                            if (count >= edgeZ) {
                                break;
                            }
                        }
                        if (count >= edgeZ) {
                            in.draw(o, 0);
                        }
                    } else {
                        in.draw(o, 0);
                    }
                }
//                // OUTSIDE
//                else if(outside.isSelected()){
//                    
//                }
            }
        }
        return in;
    }

    private boolean isOutsideMask(Voxel3D vox, ImageInt mask) {
        if (vox.getRoundX() < limX) {
            if (mask.getPixelInt(vox.getRoundX() + 1, vox.getRoundY(), vox.getRoundZ()) == 0) {
                return true;
            }
        } else {
            return true;
        }
        if (vox.getRoundX() > 0) {
            if (mask.getPixelInt(vox.getRoundX() - 1, vox.getRoundY(), vox.getRoundZ()) == 0) {
                return true;
            }
        } else {
            return true;
        }
        if (vox.getRoundY() < limY) {
            if (mask.getPixelInt(vox.getRoundX(), vox.getRoundY() + 1, vox.getRoundZ()) == 0) {
                return true;
            }
        } else {
            return true;
        }
        if (vox.getRoundY() > 0) {
            if (mask.getPixelInt(vox.getRoundX(), vox.getRoundY() - 1, vox.getRoundZ()) == 0) {
                return true;
            }
        } else {
            return true;
        }
        if (vox.getRoundZ() < limZ) {
            if (mask.getPixelInt(vox.getRoundX(), vox.getRoundY(), vox.getRoundZ() + 1) == 0) {
                return true;
            }
        } else {
            return true;
        }
        if (vox.getRoundZ() > 0) {
            if (mask.getPixelInt(vox.getRoundX(), vox.getRoundY(), vox.getRoundZ() - 1) == 0) {
                return true;
            }
        } else {
            return true;
        }
        return false;
    }

    private boolean isOutsideMaskXY(Voxel3D vox, ImageInt mask) {
        if (vox.getRoundX() < limX) {
            if (mask.getPixelInt(vox.getRoundX() + 1, vox.getRoundY(), vox.getRoundZ()) == 0) {
                return true;
            }
        } else {
            return true;
        }
        if (vox.getRoundX() > 0) {
            if (mask.getPixelInt(vox.getRoundX() - 1, vox.getRoundY(), vox.getRoundZ()) == 0) {
                return true;
            }
        } else {
            return true;
        }
        if (vox.getRoundY() < limY) {
            if (mask.getPixelInt(vox.getRoundX(), vox.getRoundY() + 1, vox.getRoundZ()) == 0) {
                return true;
            }
        } else {
            return true;
        }
        if (vox.getRoundY() > 0) {
            if (mask.getPixelInt(vox.getRoundX(), vox.getRoundY() - 1, vox.getRoundZ()) == 0) {
                return true;
            }
        } else {
            return true;
        }
        return false;
    }

    private boolean isOutsideMaskZ(Voxel3D vox, ImageInt mask) {
        if (vox.getRoundZ() < limZ) {
            if (mask.getPixelInt(vox.getRoundX(), vox.getRoundY(), vox.getRoundZ() + 1) == 0) {
                return true;
            }
        } else {
            return true;
        }
        if (vox.getRoundZ() > 0) {
            if (mask.getPixelInt(vox.getRoundX(), vox.getRoundY(), vox.getRoundZ() - 1) == 0) {
                return true;
            }
        } else {
            return true;
        }
        return false;
    }

    @Override
    public void setVerbose(boolean debug) {
        this.debug = debug;
    }

    @Override
    public void setMultithread(int nbCPUs) {
        this.nbCPUs = nbCPUs;
    }

    @Override
    public Parameter[] getParameters() {
        return parameters;
    }

    @Override
    public String getHelp() {
        return "Erase Objects according to their sizes (in voxels)";
    }
}
