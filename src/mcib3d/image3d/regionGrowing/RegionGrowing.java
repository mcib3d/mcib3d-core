/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mcib3d.image3d.regionGrowing;

import ij.ImageStack;
import java.util.ArrayList;
import mcib3d.geom.Voxel3D;
import mcib3d.image3d.ImageHandler;
import mcib3d.utils.ArrayUtil;

/**
 *
 **
 * /**
 * Copyright (C) 2008- 2012 Thomas Boudier and others
 *
 *
 *
 * This file is part of mcib3d
 *
 * mcib3d is free software; you can redistribute it and/or modify it under the
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
 * @author thomas
 */
public class RegionGrowing extends Watershed3D {

    public RegionGrowing(ImageHandler image, ImageHandler seeds, int noi, int seth) {
        super(image, seeds, noi, seth);
    }

    public RegionGrowing(ImageStack stack, ImageStack seeds, int noi, int seth) {
        super(stack, seeds, noi, seth);
    }

    public void computeAssociations(boolean ca) {
        computeAssociation = ca;
        if (!ca) {
            associations = null;
            assoRegions = null;
        }
        if ((ca) && (associations == null)) {
            associations = new ArrayList<String>();
            assoRegions = new AllRegionsAssociation();
        }
    }

    public void computeUpdatedLabels(boolean ca) {
        computeUpdatedLabels = ca;
        if (!ca) {
            updatedLabels = null;
        }
        if ((ca) && (updatedLabels == null)) {
            updatedLabels = new ArrayList<String>();
        }
    }

    public void computeVolumes(boolean b) {
        computeVolumes = b;
        if (!b) {
            volumeLabels = null;
        }
        if ((b) && (volumeLabels == null)) {
            volumeLabels = new ArrayList<Double>();
        }
    }

    public ArrayList<String> getAssociationsAsString() {
        return associations;
    }

    public ArrayList<String> getUpdatedLabelsAsString() {
        return updatedLabels;
    }

    public ArrayList<int[]> getAssociationsAsIntegers() {
        if ((associations == null) || (associations.isEmpty())) {
            return null;
        }
        ArrayList<int[]> res = new ArrayList<int[]>(associations.size());
        for (String S : associations) {
            String[] sp = S.split("_");
            int[] tab = new int[sp.length];
            for (int i = 0; i < sp.length; i++) {
                tab[i] = Integer.parseInt(sp[i]);
            }
            res.add(tab);
        }
        return res;
    }

    public ArrayList<AssociationRegion> getAssociationAsList() {
        return assoRegions.getListAssociation();
    }

    public AllRegionsAssociation getAllRegionsAssociations() {
        return assoRegions;
    }

    public ArrayList<Double> getVolumeLabels() {
        return volumeLabels;
    }

    public int mergeLabels(ArrayList<Integer> oldLabels, ArrayList<Voxel3D> borders) {
        int[] a = new int[oldLabels.size()];
        for (int i = 0; i < a.length; i++) {
            a[i] = oldLabels.get(i);
        }

        return mergeLabels(a, borders);
    }

    public double getVolumeMergeLabels(int[] oldLabels, ArrayList<Voxel3D> borders) {
        if (!computeVolumes) {
            return 0;
        }
        double newVol = borders.size();
        for (int i : oldLabels) {
            newVol += volumeLabels.get(i);
        }
        return newVol;
    }

    public double getVolumeMergeLabels(ArrayList<Integer> oldLabels, ArrayList<Voxel3D> borders) {
        int[] a = new int[oldLabels.size()];
        for (int i = 0; i < a.length; i++) {
            a[i] = oldLabels.get(i);
        }
        return getVolumeMergeLabels(a, borders);
    }

    public ArrayList<Voxel3D> getBordersMerge(ArrayList<Integer> oldLabels, boolean outsideBorders) {
        int[] a = new int[oldLabels.size()];
        for (int i = 0; i < a.length; i++) {
            a[i] = oldLabels.get(i);
        }

        return getBordersMerge(a, outsideBorders);
    }

    public ArrayList<Voxel3D> getBordersMerge(int[] oldLabels, boolean outsideBorders) {
        ArrayList<Voxel3D> drawBorders = new ArrayList();
        int nb = oldLabels.length;
        ArrayList allowedValues = new ArrayList();
        allowedValues.add(BORDER);
        if (outsideBorders) {
            allowedValues.add(NO_LABEL);
        }
        for (int o : oldLabels) {
            allowedValues.add(o);
        }

        int maxLabel = 0;
        for (int z = 0; z < watershedImage.sizeZ; z++) {
            for (int y = 0; y < watershedImage.sizeY; y++) {
                for (int x = 0; x < watershedImage.sizeX; x++) {
                    int pix = watershedImage.getPixelInt(x, y, z);
                    if (pix > maxLabel) {
                        maxLabel = pix;
                    }
                    if (pix == BORDER) {
                        ArrayUtil neigh = watershedImage.getNeighborhood3x3x3(x, y, z);
                        //IJ.log("" + neigh + " " + neigh.hasOnlyValuesInt(allowedValues));
                        if (neigh.hasOnlyValuesInt(allowedValues)) {
                            drawBorders.add(new Voxel3D(x, y, z, pix));
                            //IJ.log("border " + x + " " + y + " " + z);
                        }
                    }
                }
            }
        }

        return drawBorders;
    }

    public int mergeLabels(int[] oldLabels, ArrayList<Voxel3D> borders) {
        // get borders between these labels
        //ArrayList<Voxel3D> borders = this.getBordersMerge(oldLabels, outsideBorders);

        watershedImage.resetStats(null);
        int newLabel = (int) watershedImage.getMax() + 1;
        for (Voxel3D V : borders) {
            watershedImage.setPixel(V, newLabel);
        }

        // update image
        watershedImage.replacePixelsValue(oldLabels, newLabel);

        // update associations
        if (computeAssociation) {
            ArrayList<String> toRemove = new ArrayList();
            ArrayList<String> toAdd = new ArrayList();
            for (String S : associations) {
                boolean reprep = false;
                String[] asso = S.split("_");
                ArrayUtil assoTab = new ArrayUtil(asso.length);
                for (int i = 0; i < assoTab.getSize(); i++) {
                    int val = Integer.parseInt(asso[i]);
                    boolean rep = false;
                    for (int old : oldLabels) {
                        if (val == old) {
                            rep = true;
                            reprep = true;
                            break;
                        }
                    }
                    if (rep) {
                        assoTab.putValue(i, newLabel);
                    } else {
                        assoTab.putValue(i, val);
                    }
                }
                if (reprep) {
                    assoTab = assoTab.distinctValues();
                    assoTab.reverse(); // array from max to min with unique values
                    if (assoTab.getSize() == 1) {
                        toRemove.add(S);
                    } else {
                        String assoRep = "" + (int) assoTab.getValue(0);
                        for (int i = 1; i < assoTab.getSize(); i++) {
                            assoRep = assoRep.concat("_" + (int) assoTab.getValue(i));
                        }
                        toRemove.add(S);
                        toAdd.add(assoRep);
                    }
                }
            }
            if (!toRemove.isEmpty()) {
                associations.removeAll(toRemove);
            }
            if (!toAdd.isEmpty()) {
                for (String A : toAdd) {
                    // test if association exists already
                    boolean ok = true;
                    for (String S : associations) {
                        if ((S.compareTo(A)) == 0) {
                            ok = false;
                            break;
                        }
                    }
                    if (ok) {
                        associations.add(A);
                    }
                }
            }
            // With allRegions
            assoRegions.replaceRegion(oldLabels, newLabel);
        }

        // update used labels
        // ??
        if (computeUpdatedLabels) {
        }

        // update volumes
        if (computeVolumes) {
            double newVol = 0;
            for (int i : oldLabels) {
                newVol += volumeLabels.get(i);
                //volumeLabels.get(i)[1] = 0;
                volumeLabels.set(i, 0.0);
            }
            newVol += borders.size();
            volumeLabels.add(newVol);
        }

        return newLabel;
    }
}
