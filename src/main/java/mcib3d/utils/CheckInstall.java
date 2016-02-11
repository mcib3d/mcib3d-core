/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mcib3d.utils;

import ij.IJ;

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
public class CheckInstall {

    public static boolean installComplete() {
        IJ.log("Checking installation...");
        boolean complete = true;
        ClassLoader loader = ij.IJ.getClassLoader();
        try {
            loader.loadClass("mcib3d.image3d.ImageHandler");
        } catch (Exception e) {
            ij.IJ.log("mcib3d-core not installed. Please install from\nhttp://imagejdocu.tudor.lu/doku.php?id=plugin:stacks:3d_ij_suite:start");
            complete = false;
        }
//        try {
//            loader.loadClass("imageware.ImageWare");
//        } catch (Exception e) {
//            ij.IJ.log("ImageWare not installed");
//        }
//        try {
//            loader.loadClass("i5d.Image5D");
//        } catch (Exception e) {
//            ij.IJ.log("Image5D not installed: overlay view not available");
//        }
        try {
            loader.loadClass("javax.vecmath.Point3f");
        } catch (Exception e) {
            ij.IJ.log("Java3D not installed. ");
        }
        try {
            loader.loadClass("ij3d.Image3DUniverse");
        } catch (Exception e) {
            ij.IJ.log("ImageJ 3D Viewer not installed. Please install from http://3dviewer.neurofly.de/");
        }
        try {
            loader.loadClass("imagescience.image.Image");
        } catch (Exception e) {
            ij.IJ.log("imagescience not installed, please install from\nhttp://www.imagescience.org/meijering/software/featurej/");
            complete = false;
        }
        if (complete) {
            IJ.log("Installation OK");
        }
        return complete;
    }
}
