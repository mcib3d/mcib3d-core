/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mcib3d.geom;

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
 *
*/
import mcib3d.image3d.legacy.Image3D;
import mcib3d.image3d.*;

/**
 *
 * @author thomas
 */
public class GradientPoint extends Gradient {

    double xc, yc, zc;
    double R0, R1;
    double gmin, gmax;
    boolean positive = true;

    /**
     *
     * @param x
     * @param y
     * @param z
     * @param r0
     * @param r1
     * @param g0
     * @param g1
     * @param po
     */
    public GradientPoint(double x, double y, double z, double r0, double r1, double g0, double g1, boolean po) {
        xc = x;
        yc = y;
        zc = z;

        R0 = r0;
        R1 = r1;

        gmin = g0;
        gmax = g1;

        positive = po;
    }

    /**
     *
     * @param x
     * @param y
     * @param z
     */
    public void setPoint(double x, double y, double z) {
        xc = x;
        yc = y;
        zc = z;
    }

    /**
     *
     * @param x
     * @param y
     * @param z
     * @return
     */
    public double getGradient(double x, double y, double z) {
        double res = 0;

        double R = getRadius(x, y, z);

        //TODO Evaluate R using nice functions

        if (positive) {
            if (R < R0) {
                res = gmax;
            } else if (R > R1) {
                res = gmin;
            } else {
                ///////////// TODO nice function !
                res = gmax - (gmax - gmin) * ((R - R0) / (R1 - R0));
            }
        } else {
            if (R < R0) {
                res = gmin;
            } else if (R > R1) {
                res = gmax;
            } else {
                ///////////// TODO nice function !
                res = gmin + (gmax - gmin) * ((R - R0) / (R1 - R0));
            }
        }

        return res;
    }

    private double getRadius(double x, double y, double z) {
        double R;

        R = Math.sqrt(getRadius2(x,y,z));
        return R;
    }

    private double getRadius2(double x, double y, double z) {
        double R;

        R = (xc - x) * (xc - x) + (yc - y) * (yc - y) + (zc - z) * (zc - x);
        return R;
    }

    /**
     *
     * @param ima
     */
    public void drawGradient(Image3D ima) {
        int sx = ima.getSizex();
        int sy = ima.getSizex();
        int sz = ima.getSizex();

        for (int z = 0; z < sz; z++) {
            for (int y = 0; y < sy; y++) {
                for (int x = 0; x < sx; x++) {
                    ima.setPix(x, y, z, getGradient(x, y, z));
                }
            }
        }
    }
    
   


}
