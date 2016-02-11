/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mcib3d.geom.deformation3d;

import ij.IJ;
import java.util.ArrayList;
import java.util.List;
import javax.vecmath.Point3f;
import mcib3d.geom.GeomTransform3D;
import mcib3d.geom.Object3DSurface;
import mcib3d.geom.Point3D;
import mcib3d.geom.Vector3D;
import mcib3d.image3d.ImageHandler;
import mcib3d.image3d.processing.CannyDeriche1D;
import mcib3d.utils.ArrayUtil;

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
public class DeformableMesh extends Object3DSurface {

    GeomTransform3D transformation = null;
    ArrayList<Vector3D> forces = null;
    double minForce, maxForce, avgForce;
    ImageHandler image = null;
    // parameters forces
    double alpha;
    double thresholdMax;
    double thresholdMin;
    int maxDistance;

    public DeformableMesh(List<Point3f> l, int val) {
        super(l, val);
    }

    public DeformableMesh(List<Point3f> l) {
        super(l);
    }

    public ImageHandler getImage() {
        return image;
    }

    public void setImage(ImageHandler image) {
        this.image = image;
    }

    public double getAlpha() {
        return alpha;
    }

    public void setAlpha(double alpha) {
        this.alpha = alpha;
    }

    public int getMaxDistance() {
        return maxDistance;
    }

    public void setMaxDistance(int maxDistance) {
        this.maxDistance = maxDistance;
    }

    public double getThresholdMax() {
        return thresholdMax;
    }

    public void setThresholdMax(double thresholdMax) {
        this.thresholdMax = thresholdMax;
    }

    public double getThresholdMin() {
        return thresholdMin;
    }

    public void setThresholdMin(double thresholdMin) {
        this.thresholdMin = thresholdMin;
    }

    public void copyParameters(DeformableMesh src) {
        setAlpha(src.getAlpha());
        setThresholdMax(src.getThresholdMax());
        setThresholdMin(src.getThresholdMin());
        setMaxDistance(src.getMaxDistance());
    }

    private double[] computeLine(int i, int dist, int border, boolean forward) {
        int dir = forward ? 1 : -1;
        // compute normals (normalized)
        this.computeVerticesNormals();
        // first point and last point
        Point3D P0 = new Point3D(vertices.get(i));
        P0.translate(verticesNormals.get(i).multiply(-dir * border));
        Point3D P1 = new Point3D(vertices.get(i));
        P1.translate(verticesNormals.get(i).multiply(dir * (dist + border)));
        double[] line = image.extractLine(P0.getRoundX(), P0.getRoundY(), P0.getRoundZ(), P1.getRoundX(), P1.getRoundY(), P1.getRoundZ(), false);
        return line;
    }

    private double[] computeEdgeDeriche(int i, int border, boolean forward) {
        CannyDeriche1D der = new CannyDeriche1D(computeLine(i, maxDistance, border, forward), alpha);
        return der.getCannyDeriche();
    }

    private Point3D getClosestEdge(int i, boolean forward) {
        int border = 1;
        ArrayUtil der = new ArrayUtil(computeEdgeDeriche(i, border, forward));
        int pos = der.getFirstLocalExtrema(thresholdMax, thresholdMin);
        if (pos < border) {
            return null;
        }
        double ratio = (pos - border) / (der.getSize() - 2.0 * border);
        Point3D P = new Point3D(vertices.get(i));
        int dir = forward ? 1 : -1;
        P.translate(verticesNormals.get(i).multiply(dir * ratio * maxDistance));

        return P;
    }

    private Vector3D getBestDisplacement(int i) {
        Point3D P = new Point3D(getUniqueVertex(i));
        Point3D closestPlus = getClosestEdge(i, true);
        Point3D closestMinus = getClosestEdge(i, false);
        double distPlus, distMinus;
        Vector3D displ;
        if (closestPlus != null) {
            distPlus = closestPlus.distance(P);
        } else {
            distPlus = Double.MAX_VALUE;
        }
        if (closestMinus != null) {
            distMinus = closestMinus.distance(P);
        } else {
            distMinus = Double.MAX_VALUE;
        }
        if ((distPlus < distMinus) && (closestPlus != null)) {
            displ = new Vector3D(P, closestPlus);
        } else if ((distPlus > distMinus) && (closestMinus != null)) {
            displ = new Vector3D(P, closestMinus);
        } else {
            displ = null;
        }
        return displ;
    }

    public void computeAllForces(boolean stats) {
        forces = new ArrayList<Vector3D>();
        minForce = Double.MAX_VALUE;
        maxForce = 0;
        avgForce = 0;
        int cpt = 0;
        for (int i = 0; i < getNbUniqueVertices(); i++) {
            Vector3D force = getBestDisplacement(i);
            forces.add(force);
            // compute Statistics
            if ((stats) && (force != null)) {
                double le = force.getLength();
                if (le > maxForce) {
                    maxForce = le;
                }
                if (le < minForce) {
                    minForce = le;
                }
                avgForce += le;
                cpt++;
            }
        }
        avgForce /= cpt;
    }

    public double getSumAbsForces() {
        double sum = 0;
        for (int i = 0; i < getNbUniqueVertices(); i++) {
            Vector3D force = forces.get(i);
            if (force != null) {
                sum += forces.get(i).getLengthSquare();
            }
        }
        return sum;
    }

    public double[] getStatsForces() {
        double[] res = {minForce, maxForce, avgForce};
        return res;
    }

    public Vector3D getTranslation() {
        Vector3D moy = new Vector3D();
        int cpt = 0;
        for (int i = 0; i < getNbUniqueVertices(); i++) {
            Vector3D displ = forces.get(i);
            if (displ != null) {
                moy.addMe(displ);
                cpt++;
            }
        }
        if (cpt > 0) {
            moy = moy.multiply(1.0 / (double) (cpt));
        }

        return moy;
    }

    public Vector3D iterateTranslation(double error, int nIte) {
        Vector3D transT = new Vector3D();
        Vector3D trans;
        boolean translate = true;
        int count = 1;
        while ((translate) && (count < nIte)) {
            count++;
            this.computeAllForces(false);
            trans = this.getTranslation();
            this.translate(trans);
            transT = transT.add(trans);
            //IJ.log("Translation " + trans);
            if (trans.getLength() < error) {
                translate = false;
            }
        }
        return transT;
    }

    public double getGlobalScaling() {
        Point3D center = this.getCenterAsVector();
        double ratio = 0;
        double r;
        int cpt = 0;
        for (int i = 0; i < forces.size(); i++) {
            Point3D P0 = new Vector3D(this.getUniqueVertex(i));
            Point3D P1 = new Vector3D(this.getUniqueVertex(i));
            Vector3D force = forces.get(i);
            //IJ.log("Force " + i + " " + force);
            if (force != null) {
                P1.translate(force);
                Vector3D V0 = new Vector3D(center, P0);
                Vector3D V1 = new Vector3D(center, P1);
                r = V1.getLength() / V0.getLength();
                ratio += r;
                cpt++;
                //IJ.log("scale " + i + " " + r + " " + V0 + " " + V1);
            }
        }
        if (cpt > 1) {
            ratio /= (double) cpt;
        } else {
            ratio = 1;
        }
        return ratio;
    }

    public double iterateGlobalScaling(double error, int nIte) {
        boolean scale = true;
        double scalingT = 1;
        double scaling;
        int count = 1;
        while ((scale) && (count < nIte)) {
            count++;
            this.computeAllForces(false);
            scaling = this.getGlobalScaling();
            this.scale(scaling);
            scalingT *= scaling;
            //IJ.log("Scaling " + scaling);
            if (Math.abs(scaling - 1.0) < error) {
                scale = false;
            }
        }
        return scalingT;
    }

    public double getOrientedScaling(Vector3D dir) {
        Point3D center = this.getCenterAsVector();
        Vector3D dirN = dir.getNormalizedVector();
        double ratio = 0;
        double r;
        double cpt = 0;
        for (int i = 0; i < forces.size(); i++) {
            Point3D P0 = new Vector3D(this.getUniqueVertex(i));
            Point3D P1 = new Vector3D(this.getUniqueVertex(i));
            Vector3D force = forces.get(i);
            //IJ.log("Force " + i + " " + force);
            if (force != null) {
                double orient = Math.abs(force.getNormalizedVector().dotProduct(dirN));
                double rat = orient * orient;
                P1.translate(force);
                Vector3D V0 = new Vector3D(center, P0);
                Vector3D V1 = new Vector3D(center, P1);
                r = V1.getLength() / V0.getLength();
                ratio += rat * r;
                cpt += rat;
                if (r > 100) {
                    IJ.log("scale " + i + " " + r + " " + orient + " " + force.getLength() + " " + V0.getLength() + " " + V1.getLength() + " " + center + " " + P0 + " " + P1);
                }
            }
        }
        if (cpt > 0) {
            ratio /= cpt;
        } else {
            ratio = 1;
        }
        return ratio;
    }

    public double iterateOrientedScaling(Vector3D dir, double error, int nIte) {
        double scalingT = 1;
        boolean scale = true;
        int count = 1;
        while ((scale) && (count < nIte)) {
            count++;
            this.computeAllForces(false);
            double scaling = this.getOrientedScaling(dir);
            scalingT *= scaling;
            this.scale(scaling, dir);
            this.computeVerticesNormals();
            if (Math.abs(scaling - 1.0) < error) {
                scale = false;
            }
        }
        return scalingT;
    }

    public Vector3D getRotationAxis() {
        Vector3D moy = new Vector3D();
        Vector3D elong = this.getMainAxis().getNormalizedVector();
        double cpt = 0;
        for (int i = 0; i < getNbUniqueVertices(); i++) {
            Vector3D displ = forces.get(i);
            if (displ != null) {
                //Vector3D displN=displ.getNormalizedVector();
                //double dot=displN.dotProduct(elong);
                if (displ.getX() < 0) {
                    displ = displ.multiply(-1);
                }
                moy.addMe(displ.crossProduct(elong));
                cpt++;
            }
        }
        if (cpt > 0) {
            moy = moy.multiply(1.0 / (double) (cpt));
        }

        return moy.getNormalizedVector();
    }

    public double iterateRotation(Vector3D axe, double rotStep, double maxAngle) {
        /// ROTATION
        //this.computeAllForces();
        // rot plus
        ArrayList listPlus = this.getRotated(axe, Math.toRadians(rotStep));
        DeformableMesh defPlus = new DeformableMesh(listPlus);
        defPlus.setImage(image);
        defPlus.setCalibration(1, 1, "pix");
        defPlus.copyParameters(this);
        defPlus.computeAllForces(true);
        double sumPlus = defPlus.getSumAbsForces();
        //IJ.log("Sum Plus " + sumPlus);
        // rot minus
        ArrayList listMinus = this.getRotated(axe, Math.toRadians(-rotStep));
        DeformableMesh defMinus = new DeformableMesh(listMinus);
        defMinus.setImage(image);
        defMinus.setCalibration(1, 1, "pix");
        defMinus.copyParameters(this);
        defMinus.computeAllForces(true);
        double sumMinus = defMinus.getSumAbsForces();
        //IJ.log("Sum Minus " + sumMinus);
        // check best rotation
        double rotangle = sumPlus < sumMinus ? rotStep : -rotStep;
        double sum = sumPlus < sumMinus ? sumPlus : sumMinus;
        this.rotate(axe, Math.toRadians(rotangle));
        this.computeVerticesNormals();
        // iterate rotation while sumForces decreases
        boolean iteRot = true;
        double angleT = rotangle;
        double angRotationDegree = rotangle / 2.0;
        double angRotationRadian = Math.toRadians(angRotationDegree);
        while ((iteRot) && (angleT < maxAngle)) {
            listPlus = this.getRotated(axe, angRotationRadian);
            defPlus = new DeformableMesh(listPlus);
            defPlus.setImage(image);
            defPlus.setCalibration(1, 1, "pix");
            defPlus.copyParameters(this);
            defPlus.computeAllForces(true);
            sumPlus = defPlus.getSumAbsForces();
            if (sumPlus < sum) {
                //IJ.log("Sum rotate " + sumPlus+" "+angleT);
                this.rotate(axe, angRotationRadian);
                this.computeVerticesNormals();
                angleT += angRotationDegree;
                sum = sumPlus;
            } else {
                iteRot = false;
            }
        }
        return angleT;
    }

    public void roughDisplacement(double maxDispl) {
        this.computeAllForces(false);
        for (Vector3D force : forces) {
            if ((force != null) && (force.getLength() > maxDispl)) {
                Vector3D dirForce = force.getNormalizedVector();
                force = dirForce.multiply(maxDispl);
            }
            // loop on vertices 
            if (force != null) {
                for (Point3f vertice : vertices) {
                    vertice.add(force.getPoint3f());
                }
            }
        }
        this.computeVerticesNormals();
    }

    public void iterateRoughDisplacement(double maxDispl, int maxIte) {
        double sum = Double.MAX_VALUE;
        boolean iterate = true;
        int ite = 0;
        while ((iterate) && (ite < maxIte)) {
            this.roughDisplacement(maxDispl);
            double sumtmp = this.getSumAbsForces();
            IJ.log("ite " + ite + " " + sum + " " + sumtmp);
            if (sumtmp < sum) {
                sum = sumtmp;
            } else {
                iterate = false;
            }
            ite++;
        }
    }
}
