/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package tango.gui.util;

import ij.IJ;
import ij.ImagePlus;
import ij.measure.Calibration;
import java.util.ArrayList;
import mcib3d.geom.Object3D;
import mcib3d.geom.Object3DVoxels;
import mcib3d.geom.Objects3DPopulation;
import mcib3d.image3d.ImageHandler;
import mcib3d.image3d.ImageInt;

/**
 *
 **
 * /**
 * Copyright (C) 2008- 2012 Thomas Boudier and others
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
 * @author thomas
 */
public class ColocFactory {

    static final int COLOC_UNION = 1;
    static final int COLOC_INTERSECTION = 2;
    static final int COLOC_INDIVIDUAL = 3;
    static final int COLOC_NONE = 4;

    /**
     * Create a new ImagePlus from two input ImagePlus where objects are
     * collocalized (multifish experiment)
     *
     * @param plus1 The first segmented channel with labelled objects
     * @param plus2 The second segmented channel with labelled objects
     * @param pc The percentage (o-100) of colocalization between objects
     * @return a new colo image (input images are also modified)
     */
    public static ImagePlus createColoc2Images(ImageInt[] imasegs, Calibration cal, boolean[] keeps, double pc, int mode, int ind) {
        // works for two images only !!
        //System.out.println("coloc factory " + cal);
        Objects3DPopulation pop1 = new Objects3DPopulation();
        pop1.addImage(imasegs[0], cal);
        Objects3DPopulation pop2 = new Objects3DPopulation();
        pop2.addImage(imasegs[1], cal);
        //
        Objects3DPopulation pop3 = new Objects3DPopulation();
        pop3.setCalibration(cal);
        Object3D ob1, ob2;
        Object3DVoxels ob3;
        int nbob = 1;
        for (int i1 = 0; i1 < pop1.getNbObjects(); i1++) {
            ob1 = pop1.getObject(i1);
            for (int i2 = 0; i2 < pop2.getNbObjects(); i2++) {
                ob2 = pop2.getObject(i2);
                double p1 = ob1.pcColoc(ob2);
                double p2 = ob2.pcColoc(ob1);
                //System.out.println("testing " + i1 + " " + i2);
                if ((p1 > pc) && (p2 > pc)) {
                    System.out.println(" " + ob1.getValue() + " " + ob2.getValue() + " " + p1 + " " + p2);
                    if (mode != COLOC_NONE) {
                        ob3 = new Object3DVoxels();
                        if (mode == COLOC_INTERSECTION) {
                            ob3.addVoxelsIntersection(ob1, ob2);
                        } else if (mode == COLOC_UNION) {
                            ob3.addVoxelsUnion(ob1, ob2);
                        } else if (mode == COLOC_INDIVIDUAL) {
                            ArrayList al = ind == 1 ? ob1.getVoxels() : ob2.getVoxels();
                            ob3.addVoxels(al);
                        }
                        ob3.setValue(nbob);
                        nbob++;
                        pop3.addObject((Object3DVoxels) ob3);
                    }
                    if (!keeps[0]) {
                        ob1.draw(imasegs[0].getImageStack(), 0);
                    }
                    if (!keeps[1]) {
                        ob2.draw(imasegs[1].getImageStack(), 0);
                    }
                }
            }
        }
        // coloc = new ImageInt(imasegs[0].getSizex(), imasegs[0].getSizey(), imasegs[0].getSizez());
        ImageHandler coloc = imasegs[0].createSameDimensions();
        
        pop3.draw(coloc.getImageStack());
        ImagePlus coplus = new ImagePlus("coloc", coloc.getImageStack());
        coplus.setCalibration(cal);
        return coplus;
    }

    /**
     * Create a new ImagePlus from two input ImagePlus where objects are
     * collocalized (multifish experiment)
     *
     * @param plus1 The first segmented channel with labelled objects
     * @param plus2 The second segmented channel with labelled objects
     * @param pc The percentage (o-100) of colocalization between objects
     * @return a new colo image (input images are also modified)
     */
    public static ImagePlus createColoc3Images(ImageInt[] plus, Calibration cal, boolean[] keeps, double pc, int mode, int ind) {
        Objects3DPopulation pop1 = new Objects3DPopulation();
        pop1.addImage(plus[0], cal);

        Objects3DPopulation pop2 = new Objects3DPopulation();
        pop2.addImage(plus[1], cal);
        Objects3DPopulation pop3 = new Objects3DPopulation();
        pop2.addImage(plus[2], cal);

        // coloc compute
        Objects3DPopulation popColoc = new Objects3DPopulation();
        popColoc.setCalibration(cal);
        Object3DVoxels ob1, ob2, ob3;
        Object3DVoxels obColoc;
        for (int i1 = 0; i1 < pop1.getNbObjects(); i1++) {
            ob1 = (Object3DVoxels) pop1.getObject(i1);
            for (int i2 = 0; i2 < pop2.getNbObjects(); i2++) {
                ob2 = (Object3DVoxels) pop2.getObject(i2);
                for (int i3 = 0; i3 < pop3.getNbObjects(); i3++) {
                    ob3 = (Object3DVoxels) pop3.getObject(i3);
                    ArrayList al = new ArrayList();
                    al.add(ob2);
                    al.add(ob3);
                    //IJ.log("pc2 "+ob1.pcColoc2(ob2)+" "+ob2.pcColoc2(ob1));
                    double pc123 = ob1.pcColoc2(al);
                    //IJ.log("coloc "+i1+" "+i2+" "+i3+" : "+pc123);
                    if (pc123 > 0) {
                        // test all coloc between 2
                        al.add(ob1);
                        if (colocAll(al, pc)) {
                            obColoc = new Object3DVoxels();
                            if (mode == COLOC_INTERSECTION) {
                                obColoc.addVoxelsIntersection(al);
                            } else if (mode == COLOC_UNION) {
                                obColoc.addVoxelsUnion(al);
                            } else if (mode == COLOC_INDIVIDUAL) {
                                if (ind == 1) {
                                    obColoc.addVoxels(ob1.getVoxels());
                                } else if (ind == 2) {
                                    obColoc.addVoxels(ob2.getVoxels());
                                } else if (ind == 3) {
                                    obColoc.addVoxels(ob3.getVoxels());
                                }
                            }
                            if (mode != COLOC_NONE) {
                                popColoc.addObject((Object3DVoxels) obColoc);
                            }
                            // remove from original images
                            if (!keeps[0]) {
                                ob1.draw(plus[0].getImageStack(), 0);
                            }
                            if (!keeps[1]) {
                                ob2.draw(plus[1].getImageStack(), 0);
                            }
                            if (!keeps[2]) {
                                ob3.draw(plus[2].getImageStack(), 0);
                            }
                        }
                    }
                }
            }
        }
        //IntImage3D coloc = new IntImage3D(plus[0].getSizex(), plus[0].getSizey(), plus[0].getSizez());
        ImageHandler coloc=plus[0].createSameDimensions();
        popColoc.draw(coloc.getImageStack(), 255);
        return new ImagePlus("coloc", coloc.getImageStack());
    }

    private static boolean colocAll(ArrayList<Object3DVoxels> al, double pc) {
        for (int i = 0; i < al.size(); i++) {
            for (int j = i + 1; j < al.size(); j++) {
                double p1 = al.get(i).pcColoc(al.get(j));
                double p2 = al.get(j).pcColoc(al.get(i));
                //IJ.log("" + i + " " + j + " : " + p1 + " " + p2);
                if ((p1 < pc) || (p2 < pc)) {
                    return false;
                }
            }
        }

        return true;
    }
}
