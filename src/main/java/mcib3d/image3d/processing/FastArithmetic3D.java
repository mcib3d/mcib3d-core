package mcib3d.image3d.processing;

import ij.ImageStack;
import mcib3d.geom.Object3D;
import mcib3d.geom.Object3DVoxels;
import mcib3d.geom.Voxel3DComparable;
import mcib3d.image3d.*;
import mcib3d.utils.Chrono;
import mcib3d.utils.Logger.AbstractLog;
import mcib3d.utils.Logger.IJStatus;
import mcib3d.utils.ThreadUtil;

import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicInteger;

/*
 * To change this template, choose Tools | Templates and open the template in
 * the editor.
 */

/**
 * *
 * /**
 * Copyright (C) 2008- 2012 Thomas Boudier and others
 * <p>
 * <p>
 * <p>
 * This file is part of mcib3d
 * <p>
 * mcib3d is free software; you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 3 of the License, or (at your option) any later
 * version.
 * <p>
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * <p>
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 *
 * @author thomas
 */
public class FastArithmetic3D {

    static public final int ADD = 1;
    static public final int MULT = 2;
    static public final int MAX = 3;
    static public final int MIN = 4;
    static public final int DIFF = 5;


    public static ImageInt mathIntImage(ImageInt stackorig, final ImageInt stackother, int operation, final float par1, final float par2, int nbcpus, boolean showstatus, final AbstractLog log) {
        int nbToProcess = stackorig.sizeZ;
        // Timer
        final Chrono time = new Chrono(nbToProcess);
        time.start();
        final AbstractLog show = log;

        // get stack info
        final ImageInt ima = stackorig;
        final ImageInt res = (ImageInt) ima.createSameDimensions();
        final ImageInt out = res;
        final AtomicInteger ai = new AtomicInteger(0);
        final int n_cpus = nbcpus == 0 ? ThreadUtil.getNbCpus() : nbcpus;

        final int fi = operation;
        final int dec = (int) Math.ceil((double) ima.sizeZ / (double) n_cpus);
        Thread[] threads = ThreadUtil.createThreadArray(n_cpus);
        show.log("Starting");
        for (int ithread = 0; ithread < threads.length; ithread++) {
            threads[ithread] = new Thread() {
                @Override
                public void run() {
                    for (int k = ai.getAndIncrement(); k < n_cpus; k = ai.getAndIncrement()) {
                        ima.mathGeneric(stackother, res, dec * k, dec * (k + 1), fi, par1, par2, time, log);
                    }
                }
            };
        }
        ThreadUtil.startAndJoin(threads);
        show.log("Finished");

        return res;
    }

    public static ImageFloat mathIntImage(ImageFloat stackorig, final ImageFloat stackother, int operation, final float par1, final float par2, int nbcpus, boolean showstatus, final AbstractLog log) {
        int nbToProcess = stackorig.sizeZ;
        // Timer
        final Chrono time = new Chrono(nbToProcess);
        time.start();
        final AbstractLog show = log;

        // get stack info
        final ImageFloat ima = stackorig;
        final ImageFloat res = (ImageFloat) ima.createSameDimensions();
        final ImageFloat out = res;
        final AtomicInteger ai = new AtomicInteger(0);
        final int n_cpus = nbcpus == 0 ? ThreadUtil.getNbCpus() : nbcpus;

        final int fi = operation;
        final int dec = (int) Math.ceil((double) ima.sizeZ / (double) n_cpus);
        Thread[] threads = ThreadUtil.createThreadArray(n_cpus);
        show.log("Starting");
        for (int ithread = 0; ithread < threads.length; ithread++) {
            threads[ithread] = new Thread() {
                @Override
                public void run() {
                    for (int k = ai.getAndIncrement(); k < n_cpus; k = ai.getAndIncrement()) {
                        ima.mathGeneric(stackother, res, dec * k, dec * (k + 1), fi, par1, par2, time, log);
                    }
                }
            };
        }
        ThreadUtil.startAndJoin(threads);
        show.log("Finished");

        return res;
    }







}
