package mcib3d.utils;

import java.util.Date;

/**
 * Copyright (C) Thomas Boudier
 * <p>
 * License:
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 * <p>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

/**
 * Class that defines a chronometer <P>
 *
 * @author <a href="mailto:Olivier.Sigaud@lip6.fr"> Olivier Sigaud</a> and <a
 *         href="mailto:gerpy@free.fr"> Pierre Gérard</a> .
 * @created 24 janvier 2005 modified by thomas
 */

public final class Chrono {
    /**
     * Description of the Field
     */
    private Date D1Start;
    private Date D1Temp;
    /**
     * Description of the Field
     */
    private Date D2;
    /**
     * Description of the Field
     */
    private int nbTasksTotal;
    private int nbTasksCompleted;

    private long minDelayToShow = 500; // ms


    /**
     * Constructor for the Chrono object
     */
    public Chrono() {
        D1Start = new Date();
        D1Temp = new Date();
        D2 = new Date();
        nbTasksTotal = -1;
        nbTasksCompleted = -1;
    }


    /**
     * Constructor for the Chrono object
     *
     * @param nbt Description of the Parameter
     */
    public Chrono(int nbt) {
        D1Start = new Date();
        D1Temp = new Date();
        D2 = new Date();
        nbTasksTotal = nbt;
        nbTasksCompleted = 0;
    }


    /**
     * Description of the Method
     */
    public void start() {
        D1Start = new Date();
        D2 = new Date();
    }


    /**
     * Description of the Method
     */
    public void stop() {
        D2 = new Date();
    }


    /**
     * Description of the Method
     *
     * @return Description of the Return Value
     */
    public long delay() {
        return (D2.getTime() - D1Start.getTime());
    }


    /**
     * Description of the Method
     *
     * @return Description of the Return Value
     */
    public String delayString() {
        return timeString(delay());
    }


    /**
     * Description of the Method
     *
     * @param nb Description of the Parameter
     * @return Description of the Return Value
     */
    public long remain(int nb) {
        long delay = D2.getTime() - D1Start.getTime();
        long remain = (long) (delay * ((float) nbTasksTotal / (float) nb - 1.0f));
        return remain;
    }


    /**
     * Description of the Method
     *
     * @param nb Description of the Parameter
     * @return Description of the Return Value
     */
    public String remainString(int nb) {
        return timeString(remain(nb));
    }


    /**
     * Description of the Method
     *
     * @param nb Description of the Parameter
     * @return Description of the Return Value
     */
    public long totalTimeEstimate(int nb) {
        long delay = delay();
        long total = (long) (delay * ((float) nbTasksTotal / (float) nb));
        return total;
    }

    public String remainingTimeEstimate(int nb) {
        long delay = delay();
        long total = (long) (delay * ((float) (nbTasksTotal - nb) / (float) nb));
        return timeString(total);
    }


    /**
     * Description of the Method
     *
     * @param nb Description of the Parameter
     * @return Description of the Return Value
     */
    public String totalTimeEstimateString(int nb) {
        return timeString(totalTimeEstimate(nb));
    }

    synchronized public String getFullInfo(int nbTasks) {
        D2 = new Date();
        nbTasksCompleted += nbTasks;
        if ((D2.getTime() - D1Temp.getTime()) > minDelayToShow) {
            String res = new String("" + nbTasksCompleted + "/" + nbTasksTotal + " (" + remainingTimeEstimate(nbTasksCompleted) + " ETC)");
            D1Temp = new Date();
            return res;
        }

        return null;
    }

    /**
     * Description of the Method
     *
     * @param delay Description of the Parameter
     * @return Description of the Return Value
     */
    private String timeString(long delay) {
        String res;
        if (delay < 1000) {
            res = new String(delay + " ms");
        } else {
            long d1 = delay / 1000;
            long ms = (delay - d1 * 1000);
            if (d1 < 60) {
                res = new String(d1 + " s " + ms + " ms");
            } else {
                long d2 = d1 / 60;
                long reste = d1 - d2 * 60;
                if (d2 < 60) {
                    res = new String(d2 + " min " + reste + " s");
                } else {
                    long d3 = d2 / 60;
                    long r2 = d2 - d3 * 60;
                    if (d3 < 24) {
                        res = new String(d3 + " h " + r2 + " min " + reste + " s");
                    } else {
                        long d4 = d3 / 24;
                        res = new String(d4 + " d " + d3 + " h " + r2 + " min " + reste + " s");
                    }
                }
            }
        }
        return res;
    }

}

