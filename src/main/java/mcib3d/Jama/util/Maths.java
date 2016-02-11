package mcib3d.Jama.util;

/**
 *  Description of the Class
 *
 * @author     
 * @created    18 décembre 2007
 */
public class Maths {

	/**
	 * sqrt(a^2 + b^2) without under/overflow. *
	 *
	 * @param  a  Description of the Parameter
	 * @param  b  Description of the Parameter
	 * @return    Description of the Return Value
	 */

	public static double hypot(double a, double b) {
		double r;
		if (Math.abs(a) > Math.abs(b)) {
			r = b / a;
			r = Math.abs(a) * Math.sqrt(1 + r * r);
		} else if (b != 0) {
			r = a / b;
			r = Math.abs(b) * Math.sqrt(1 + r * r);
		} else {
			r = 0.0;
		}
		return r;
	}
}

