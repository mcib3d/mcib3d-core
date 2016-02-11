/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mcib3d.utils;
import java.util.concurrent.atomic.AtomicInteger;
import javax.swing.SwingUtilities;
/**
Copyright (C) Thomas Boudier

License:
This program is free software; you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation; either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

public class ThreadRunner {

    /** Start all given threads and wait on each of them until all are done.
     * From Stephan Preibisch's Multithreading.java class. See:
     * http://repo.or.cz/w/trakem2.git?a=blob;f=mpi/fruitfly/general/MultiThreading.java;hb=HEAD
     */

    /* To initiate Threads:
    final ThreadRunner tr = new ThreadRunner(0, sizeZ, multithread?0:1);
    for (int i = 0; i<tr.threads.length; i++) {
        tr.threads[i] = new Thread(
            new Runnable() {
                public void run() {
                    for (int idx = tr.ai.getAndIncrement(); idx<tr.end; idx = tr.ai.getAndIncrement()) {

                    }
                }
            }
        );
    }

     * 
     */
    public final int start, end;
    public final Thread[] threads;
    public final AtomicInteger ai;

    public ThreadRunner(int start, int end, int cpulimit) {
        this.start=start;
        this.end=end;
        this.ai= new AtomicInteger(this.start);
        int nb = getNbCpus();
        if (cpulimit>0 && nb>cpulimit) nb=cpulimit;
        this.threads = new Thread[nb];
        
    }

    public void startAndJoin() {
       startAndJoin(threads);
    }
    
    
    protected static void startAndJoin(Thread[] threads) {
        for (int ithread = 0; ithread < threads.length; ++ithread) {
            threads[ithread].setPriority(Thread.NORM_PRIORITY);
            threads[ithread].start();
            //SwingUtilities.invokeLater(threads[ithread]);
            
        }

        try {
            for (int ithread = 0; ithread < threads.length; ++ithread) {
                threads[ithread].join();
            }
        } catch (InterruptedException ie) {
            throw new RuntimeException(ie);
        }
    }
    

    public void resetAi(){
        ai.set(start);
    }
    
    private int getNbCpus() {
        return Math.max(1, Math.min(Runtime.getRuntime().availableProcessors(), end-start));
    }

}
