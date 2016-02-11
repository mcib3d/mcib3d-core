package mcib3d.utils;

import java.io.*;
import java.util.*;

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

/**
 *  Class that defines a chronometer <P>
 *
 * @author     <a href="mailto:Olivier.Sigaud@lip6.fr"> Olivier Sigaud</a> and <a
 *      href="mailto:gerpy@free.fr"> Pierre Gérard</a> .
 * @created    24 janvier 2005 modified by thomas
 */

public final class Chrono {
	/**
	 *  Description of the Field
	 */
	protected Date D1;
	/**
	 *  Description of the Field
	 */
	protected Date D2;
	/**
	 *  Description of the Field
	 */
	protected int nb_tasks;


	/**
	 *  Constructor for the Chrono object
	 */
	public Chrono() {
		D1 = new Date();
		D2 = new Date();
		nb_tasks = -1;
	}


	/**
	 *  Constructor for the Chrono object
	 *
	 * @param  nbt  Description of the Parameter
	 */
	public Chrono(int nbt) {
		D1 = new Date();
		D2 = new Date();
		nb_tasks = nbt;
	}


	/**
	 *  Description of the Method
	 */
	public void start() {
		D1 = new Date();
		D2 = new Date();
	}


	/**
	 *  Description of the Method
	 */
	public void stop() {
		D2 = new Date();
	}


	/**
	 *  Description of the Method
	 *
	 * @return    Description of the Return Value
	 */
	public long delay() {
		return (D2.getTime() - D1.getTime());
	}


	/**
	 *  Description of the Method
	 *
	 * @return    Description of the Return Value
	 */
	public String delayString() {
		return timeString(delay());
	}


	/**
	 *  Description of the Method
	 *
	 * @param  nb  Description of the Parameter
	 * @return     Description of the Return Value
	 */
	public long remain(int nb) {
		long delay = D2.getTime() - D1.getTime();
		long remain = (long) (delay * ((float) nb_tasks / (float) nb - 1.0f));
		return remain;
	}


	/**
	 *  Description of the Method
	 *
	 * @param  nb  Description of the Parameter
	 * @return     Description of the Return Value
	 */
	public String remainString(int nb) {
		return timeString(remain(nb));
	}


	/**
	 *  Description of the Method
	 *
	 * @param  nb  Description of the Parameter
	 * @return     Description of the Return Value
	 */
	public long totalTimeEstimate(int nb) {
		long delay = delay();
		long total = (long) (delay * ((float) nb_tasks / (float) nb));
		return total;
	}


	/**
	 *  Description of the Method
	 *
	 * @param  nb  Description of the Parameter
	 * @return     Description of the Return Value
	 */
	public String totalTimeEstimateString(int nb) {
		return timeString(totalTimeEstimate(nb));
	}


	/**
	 *  Description of the Method
	 *
	 * @param  delay  Description of the Parameter
	 * @return        Description of the Return Value
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
						res = new String(d4 + " j " + d3 + " h " + r2 + " min " + reste + " s");
					}
				}
			}
		}
		return res;
	}

}

