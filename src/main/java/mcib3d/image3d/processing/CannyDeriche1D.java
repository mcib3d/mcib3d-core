/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mcib3d.image3d.processing;

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
public class CannyDeriche1D {

    private double[] line;
    private double alpha;

    public CannyDeriche1D(double[] li, double a) {
        line = li;
        alpha = a;
    }

    //Filtre de Deriche 1D
    private double[] computeCannyderiche() {

        int n = line.length;
        double[] der = new double[n];
        double[] der1 = new double[n];
        double[] der2 = new double[n];

        if (n > 2) {
            //Constantes		
            double e = Math.exp(-alpha);
            double e2 = Math.exp(-2 * alpha);
            //float c = 1-(float)Math.pow((1-e),2)/e;
            double a0, a1, a2, a3, b1, b2;
            a0 = 0;
            a1 = 1;
            a2 = -1;
            a3 = 0;
            b1 = 2 * e;
            b2 = -e2;

            //Gestion de bords
            der1[0] = line[0] * (a0 + a1) / (1 - b1 - b2);
            der1[1] = line[1] * a0 + a1 * line[0] + der1[0] * (b1 + b2);
            der2[n - 1] = line[n - 1] * (a2 + a3) / (1 - b1 - b2);
            der2[n - 2] = line[n - 1] * a2 + a3 * line[n - 1] + der2[n - 1] * (b1 + b2);

            //Calcul de der1
            for (int i = 2; i < n; i++) {
                //der1[i]=(c*e*(float)line[i-1])+(2*e*der1[i-1])-(e2*der1[i-2]);
                //der1[i]=((float)line[i-1])+(2*e*der1[i-1])-(e2*der1[i-2]);
                der1[i] = (a0 * (line[i])) + (a1 * (line[i - 1])) + (b1 * der1[i - 1]) + (b2 * der1[i - 2]);
            }

            //Calcul de der2
            for (int i = n - 3; i >= 0; i--) {
                //der2[i]=(c*e*(float)line[i+1])+(2*e*der2[i+1])-(e2*der2[i+2]);
                //der2[i]=(-1*(float)line[i+1])+(2*e*der2[i+1])-(e2*der2[i+2]);
                der2[i] = (a2 * (line[i + 1])) + (a3 * (line[i + 2])) + (b1 * der2[i + 1]) + (b2 * der2[i + 2]);
            }

            //Calcul de der		
            for (int i = 0; i < n; i++) {
                der[i] = (b2) * (der1[i] + der2[i]);
            }
        } else {
            for (int i = 0; i < n; i++) {
                der[i] = 0;
            }
        }

        return der;
    }

    public double[] getCannyDeriche() {
        return computeCannyderiche();
    }

    public double getAlpha() {
        return alpha;
    }

    public void setAlpha(double alpha) {
        this.alpha = alpha;
    }

    public double[] getLine() {
        return line;
    }

    public void setLine(double[] line) {
        this.line = line;
    }
}
