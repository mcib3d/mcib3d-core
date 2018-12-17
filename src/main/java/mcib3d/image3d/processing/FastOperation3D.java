package mcib3d.image3d.processing;

import ij.IJ;
import mcib3d.image3d.ImageFloat;
import mcib3d.image3d.ImageHandler;
import mcib3d.image3d.ImageInt;
import mcib3d.utils.Chrono;
import mcib3d.utils.Logger.AbstractLog;
import mcib3d.utils.Logger.NoLog;
import mcib3d.utils.ThreadUtil;

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
public class FastOperation3D {
    static public final int INVERT = 1; // out = par1 - pix
    static public final int POW = 4; // out = pow(pix,par1)
    static public final int ADD = 5; // out = pix + par1
    static public final int MULT = 6; // out = pix*par1
    static public final int FILL = 7; // out = par1
    static public final int THRESHOLDINC = 8; // out = (par1<=pix<=par2)?255:0
    static public final int THRESHOLDEXC = 9; // out = (par1<pix<par2)?255:0
    static public final int REPLACE = 10; // out = (pix==par1)?par2:pix


    public static ImageInt operationIntImage(ImageInt stackorig, int operation, final float par1, final float par2, int nbcpus, boolean showstatus, final AbstractLog log) {
        int nbToProcess = stackorig.sizeZ;
        // Timer
        final Chrono time = new Chrono(nbToProcess);
        time.start();
        final AbstractLog show = log;
        //
        IJ.log("Replacing " + par1 + " " + par2 + " " + operation);

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
                        ima.operationGeneric(out, dec * k, dec * (k + 1), fi, par1, par2, time, log);
                    }
                }
            };
        }
        ThreadUtil.startAndJoin(threads);
        show.log("Finished");

        return res;
    }

    public static ImageFloat operationFloatImage(ImageFloat stackorig, int operation, final float par1, final float par2, int nbcpus, boolean showstatus, final AbstractLog log) {
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
            threads[ithread] = new Thread(() -> {
                for (int k = ai.getAndIncrement(); k < n_cpus; k = ai.getAndIncrement()) {
                    ima.operationGeneric(out, dec * k, dec * (k + 1), fi, par1, par2, time, log);
                }
            });
        }
        ThreadUtil.startAndJoin(threads);
        show.log("Finished");

        return out;
    }

    public static ImageHandler operationImage(ImageHandler stackorig, int operation, final float par1, final float par2) {
        ImageHandler res = operationImage(stackorig, operation, par1, par2, 0, false, new NoLog());
        res.show();
        return res;
    }

    public static ImageHandler operationImage(ImageHandler stackorig, int operation, final float par1, final float par2, int nbcpus, boolean showstatus, final AbstractLog log) {
        if (stackorig instanceof ImageInt)
            return operationIntImage((ImageInt) stackorig, operation, par1, par2, nbcpus, showstatus, log);
        if (stackorig instanceof ImageFloat)
            return operationFloatImage((ImageFloat) stackorig, operation, par1, par2, nbcpus, showstatus, log);

        return null;
    }


}
