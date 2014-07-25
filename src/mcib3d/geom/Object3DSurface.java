/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mcib3d.geom;

import ij.IJ;
import ij.ImageStack;
import ij.gui.PolygonRoi;
import ij.gui.Roi;
import ij.process.ByteProcessor;
import ij.process.ImageProcessor;
import ij3d.Content;
import ij3d.Image3DUniverse;
import java.awt.Color;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import javax.vecmath.Color3f;
import javax.vecmath.Point3f;
import javax.vecmath.Vector3f;
import mcib3d.image3d.ImageFloat;
import mcib3d.image3d.ImageHandler;
import mcib3d.utils.ArrayUtil;
import mcib3d.utils.KDTreeC;
import mcib3d.utils.ThreadUtil;
import quickhull3d.Point3d;
import quickhull3d.QuickHull3D;

/**
 *
 **
 * /**
 * Copyright (C) 2008- 2011 Thomas Boudier
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
public class Object3DSurface extends Object3D {
    // vertices coordiantes are in real calibrated values (not pixels)
    // calibration hence is the original image calibration
    // they are normally as triangle mesh

    protected List<Point3f> faces = null;
    //protected List<Integer> vertices_unique_index = null;
    protected List<Point3f> smooth_faces = null;
    protected List<Point3f> vertices = null;
    protected List<List<Integer>> vertices_faces_index = null;
    protected List<Integer> faces_vertices_index = null;
    protected ArrayList<Vector3D> verticesNormals;
    ArrayList<Voxel3D> voxels = null;
    //double surface_area = Double.NaN;
    double surfaceMesh = Double.NaN;
    double surfaceMeshUnit = Double.NaN;
    double smooth_surface_area = Double.NaN;
    double smooth_surface_area_unit = Double.NaN;
    private float smoothing_factor = 0.1f;
    public static final int SMOOTH_LAPLACE = 1;
    public static final int SMOOTH_IJ3D = 2;
    public static final int SMOOTH_BLENDER = 3;
    private int smooth_method = SMOOTH_IJ3D;

    /**
     *
     * @param l
     */
    public Object3DSurface(List<Point3f> l) {
        faces = l;
        smooth_faces = null;
        voxels = null;

        //surface_area = Double.NaN;
        smooth_surface_area = Double.NaN;
        smoothing_factor = 0.1f;

        value = 1;

        computeUniqueVertices();

        init();
    }

    /**
     *
     * @param l
     * @param val
     */
    public Object3DSurface(List<Point3f> l, int val) {
        faces = l;
        smooth_faces = null;
        voxels = null;

        //surface_area = Double.NaN;
        smooth_surface_area = Double.NaN;
        smoothing_factor = 0.1f;

        value = val;

        computeUniqueVertices();

        init();
    }

    // go back to pixels coordinates
    public void deCalibrateObject() {
        deCalibratePoints();

        resXY = 1;
        resZ = 1;
        units = "pix";

        smooth_faces = null;
        voxels = null;

        //surface_area = Double.NaN;
        smooth_surface_area = Double.NaN;

        //computeUniqueVertices();
        init();
    }

    private void deCalibratePoints() {
        for (Point3f P : faces) {
            P.set(P.x / (float) resXY, P.y / (float) resXY, P.z / (float) resZ);
        }

        for (Point3f P : vertices) {
            P.set(P.x / (float) resXY, P.y / (float) resXY, P.z / (float) resZ);
        }

        this.computeCenter();
        this.computeBounding();

    }

    // go from pixels coordinates to normal calibrated coordinates
    public void reCalibrateObject() {
        reCalibratePoints();

        smooth_faces = null;
        voxels = null;

        //surface_area = Double.NaN;
        smooth_surface_area = Double.NaN;

        //computeUniqueVertices();
        init();
    }

    public void reCalibratePoints() {
        for (Point3f P : faces) {
            P.set(P.x * (float) resXY, P.y * (float) resXY, P.z * (float) resZ);
        }

        for (Point3f P : vertices) {
            P.set(P.x * (float) resXY, P.y * (float) resXY, P.z * (float) resZ);
        }

        this.computeCenter();
        this.computeBounding();
    }

    /**
     *
     * @return
     */
    public double getSmoothSurfaceArea() {
        if (Double.isNaN(smooth_surface_area)) {
            computeSmoothSurfaceArea();
        }
        return smooth_surface_area;
    }

    /**
     *
     * @return
     */
    public double getSmoothSurfaceAreaUnit() {
        if (Double.isNaN(smooth_surface_area)) {
            computeSmoothSurfaceArea();
        }
        return smooth_surface_area_unit;
    }

    /**
     *
     * @return
     */
    public double getSurfaceMesh() {
        if (Double.isNaN(surfaceMesh)) {
            computeSurfaceAreas();
        }
        return surfaceMesh;
    }

    /**
     *
     * @return
     */
    public double getSurfaceMeshUnit() {
        if (Double.isNaN(surfaceMeshUnit)) {
            computeSurfaceAreas();
        }
        return surfaceMeshUnit;
    }

    /**
     *
     * @return
     */
    public List<Point3f> getSmoothSurface() {
        if (smooth_faces == null) {
            computeSmoothSurfaceArea();
        }
        return smooth_faces;
    }

    /**
     *
     * @param smooth
     * @return
     */
    public List<Point3f> getSurfaceTrianglesPixels(boolean smooth) {
        if ((smooth) && (smooth_faces == null)) {
            computeSmoothSurfaceArea();
        }
        List<Point3f> l;
        if (smooth) {
            l = smooth_faces;
        } else {
            l = faces;
        }
        // calibration
        ArrayList<Point3f> unit_vertices = new ArrayList();
        Iterator it = l.iterator();
        Point3f P, PP;
        float rxy = (float) resXY;
        float rz = (float) resZ;

        // coordinates are normally calibrated
        while (it.hasNext()) {
            P = (Point3f) it.next();
            PP = new Point3f(P.x / rxy, P.y / rxy, P.z / rz);
            unit_vertices.add(PP);
        }

        return unit_vertices;
    }

    public List<Point3f> getSurfaceTrianglesUnit(boolean smooth) {
        if ((smooth) && (smooth_faces == null)) {
            computeSmoothSurfaceArea();
        }
        List<Point3f> l;
        if (smooth) {
            l = smooth_faces;
        } else {
            l = faces;
        }
        // calibration
        ArrayList<Point3f> unit_vertices = new ArrayList();
        Iterator it = l.iterator();
        Point3f P, PP;

        // coordinates are normally calibrated
        while (it.hasNext()) {
            P = (Point3f) it.next();
            PP = new Point3f(P.x, P.y, P.z);
            unit_vertices.add(PP);
        }

        return unit_vertices;
    }

    private double computeSurfaceMeshArea(boolean smooth, double rxy, double rz) {
        double surfarea = 0;
        List l;
        if (!smooth) {
            l = faces;
        } else {
            l = smooth_faces;
        }
        for (int i = 0; i < l.size(); i += 3) {
            Vector3f ab = new Vector3f((Point3f) l.get(i));
            ab.sub((Point3f) l.get(i + 1));
            Vector3f ac = new Vector3f((Point3f) l.get(i));
            ac.sub((Point3f) l.get(i + 2));
            Vector3f abc = new Vector3f();
            // calibration
            ab.set((float) (ab.x * rxy), (float) (ab.y * rxy), (float) (ab.z * rz));
            ac.set((float) (ac.x * rxy), (float) (ac.y * rxy), (float) (ac.z * rz));
            abc.cross(ab, ac);
            float area_tmp = abc.length() * .5f;
            surfarea += area_tmp;
        }
        return surfarea;
    }

    // CONVEX HULL 3D USING QUICKHULL3D
    public ArrayList<Point3f> computeConvexHull3D() {
        QuickHull3D hull = new QuickHull3D();
        ArrayList<Voxel3D> pointsList = this.getContours();
        Point3d[] points = new Point3d[pointsList.size()];
        for (int ve = 0; ve < points.length; ve++) {
            points[ve] = new Point3d(pointsList.get(ve).getX(), pointsList.get(ve).getY(), pointsList.get(ve).getZ());
        }

        hull.build(points);
        hull.triangulate();

        ArrayList<Point3f> convex = new ArrayList<Point3f>();
        int[][] faceIndices = hull.getFaces();
        Point3d[] verticesHull = hull.getVertices();
        for (int ve = 0; ve < verticesHull.length; ve++) {
            for (int k = 0; k < faceIndices[ve].length; k++) {
                Point3d point = verticesHull[faceIndices[ve][k]];
                convex.add(new Point3f((float) point.x, (float) point.y, (float) point.z));
            }
        }

        for (int[] faceIndice : faceIndices) {
            for (int ve = 0; ve < 3; ve++) {
                Point3d point = verticesHull[faceIndice[ve]];
                convex.add(new Point3f((float) point.x, (float) point.y, (float) point.z));
            }
        }

        return convex;
    }

    private void computeSurfaceAreas() {
        // FIXME aresas ad surfaces meshes !!
        areaNbVoxels = computeSurfaceMeshArea(false, 1, 1);
        areaContactVoxels = areaNbVoxels;
        areaContactUnit = computeSurfaceMeshArea(false, resXY, resZ);
        surfaceMesh = areaNbVoxels;
        surfaceMeshUnit = areaContactUnit;
    }

    private void computeSmoothSurfaceArea() {
        if (smooth_faces == null) {
            switch (smooth_method) {
                case SMOOTH_LAPLACE:
                    IJ.showStatus("Smoothing Laplace");
                    this.computeSmoothSurface_Laplace();
                    break;
                case SMOOTH_IJ3D:
                    IJ.showStatus("Smoothing IJ3D");
                    this.computeSmoothSurface_IJ3D();
                    break;
                case SMOOTH_BLENDER:
                    IJ.showStatus("Smoothing blender");
                    this.computeSmoothSurface_BLENDER();
                    break;
                default:
                    break;
            }
        }
        smooth_surface_area = computeSurfaceMeshArea(true, 1, 1);
        smooth_surface_area_unit = computeSurfaceMeshArea(true, resXY, resZ);
    }

    // compute neighborhood vertices on all vertices (not unique)
    private HashMap computeNeighorhood() {
        HashMap voisins = new HashMap();
        for (int j = 0; j < faces.size(); j += 3) {
            computeNeighborhoodSurface(voisins, j, 0);
            computeNeighborhoodSurface(voisins, j, 1);
            computeNeighborhoodSurface(voisins, j, 2);
        }

        return voisins;
    }

    private void computeNeighborhoodSurface(HashMap neighbors, int base, int offset) {
        HashSet set;
        if (neighbors.containsKey(faces.get(base + offset))) {
            set = (HashSet) neighbors.get(faces.get(base + offset));
        } else {
            set = new HashSet();
            neighbors.put(faces.get(base + offset % 3), set);
        }

        if (!set.contains(faces.get(base + (offset + 1) % 3))) {
            set.add(faces.get(base + (offset + 1) % 3));
        }

        if (!set.contains(faces.get(base + (offset + 2) % 3))) {
            set.add(faces.get(base + (offset + 2) % 3));
        }
    }

    private void computeSmoothSurface_IJ3D() {
        smooth_faces = MeshEditor.smooth(faces, smoothing_factor);
    }

    private void computeSmoothSurface_BLENDER() {
        smooth_faces = MeshEditor.smooth2(faces, (int) (smoothing_factor * 10));
    }

    private void computeSmoothSurface_Laplace() {

        HashMap voisins = computeNeighorhood();
//        for (int j = 0; j < vertices.size(); j += 3) {
//            computeNeighborhoodSurface(voisins, j, 0);
//            computeNeighborhoodSurface(voisins, j, 1);
//            computeNeighborhoodSurface(voisins, j, 2);
//        }
        HashMap<Point3f, Point3f> newPoints = new HashMap();

        //Collection e = voisins.keySet();
        Iterator it = faces.iterator();
        int c = 0;
        while (it.hasNext()) {
            Point3f p = (Point3f) it.next();
            Point3f tmp = new Point3f(0, 0, 0);
            Object[] e2 = ((HashSet) voisins.get(p)).toArray();
            for (Object e21 : e2) {
//                if (c == 10) {
//                }
                tmp.add((Point3f) e21);
            }
            tmp.scale(1.0f / e2.length);
            tmp.interpolate(p, 1 - smoothing_factor);
            newPoints.put(p, tmp);

            c++;
        }

        smooth_faces = new ArrayList<Point3f>();
        for (int j = 0; j < faces.size(); j++) {
            smooth_faces.add(newPoints.get(faces.get(j)));
        }
    }

    //dans mesh.java
    public static List<Point3f> invertNormals(List<Point3f> li) {
        ArrayList<Point3f> li2 = new ArrayList();

        for (int i = 0; i < li.size(); i += 3) {
            li2.add(li.get(i));
            li2.add(li.get(i + 2));
            li2.add(li.get(i + 1));
        }

        return li2;
    }

    /**
     *
     * @return
     */
    public float getSmoothingFactor() {
        return smoothing_factor;
    }

    /**
     *
     * @param method
     */
    public void setSmooth_method(int method) {
        if (method != smooth_method) {
            smooth_method = method;
            smooth_faces = null;
            smooth_surface_area = Double.NaN;
        }
    }

    /**
     *
     * @param fac
     */
    public void setSmoothingFactor(float fac) {
        if (fac != smoothing_factor) {
            smoothing_factor = fac;
            smooth_faces = null;
            smooth_surface_area = Double.NaN;
        }
    }

//    private ArrayList<Vector3D> computeSurfaceNormalsFaces() {
//        ArrayList<Vector3D> surf_normals = new ArrayList<Vector3D>();
//        for (int i = 0; i < faces.size(); i += 3) {
//            Vector3D V1 = new Vector3D(faces.get(i), faces.get(i + 1));
//            Vector3D V2 = new Vector3D(faces.get(i + 1), faces.get(i + 2));
//            Vector3D N = V1.crossProduct(V2);
//            N.normalize();
//            surf_normals.add(N);
//        }
//
//        return surf_normals;
//    }
    public Point3f getNormalFace(int idx) {
        if (vertices_faces_index == null) {
            computeUniqueVertices();
        }

        Vector3D N;
        int ba = (idx / 3) * 3;
        Point3f P1 = faces.get(ba);
        Point3f P2 = faces.get(ba + 1);
        Point3f P3 = faces.get(ba + 2);

        Vector3D P1P2 = new Vector3D(P1, P2);
        Vector3D P1P3 = new Vector3D(P1, P3);

        N = P1P2.crossProduct(P1P3);
        N.normalize();

        return new Point3f((float) N.x, (float) N.y, (float) N.z);
    }

    public Point3f getCenterFace(int idx) {
        if (vertices_faces_index == null) {
            computeUniqueVertices();
        }

        int ba = (idx / 3) * 3;
        Point3f C = new Point3f(faces.get(ba));
        C.add(faces.get(ba + 1));
        C.add(faces.get(ba + 2));

        C.scale(0.333f);

        return C;
    }

    public Point3f getNormalVertex(int idx) {
        if (vertices_faces_index == null) {
            computeUniqueVertices();
        }
        List<Integer> list = vertices_faces_index.get(idx);

        Point3f N = new Point3f();

        for (int i : list) {
            N.add(getNormalFace(i));
        }
        float le = N.distance(new Point3f());
        N.scale(1.0f / le);

        return N;
    }

    public Point3f getNormalVertexWeighted(int idx) {
        if (vertices_faces_index == null) {
            computeUniqueVertices();
        }
        List<Integer> list = vertices_faces_index.get(idx);

        Vector3D N = new Vector3D();
        Point3f P = vertices.get(idx);
        for (int i : list) {
            Point3f C = getCenterFace(i);
            Vector3D V = new Vector3D(P, C);
            Vector3D Nf = new Vector3D(getNormalFace(i));
            Nf.multiplyMe(1.0 / V.getLength());
            N.addMe(Nf);
            // TEST
            if (idx == 100) {
                IJ.log("normal " + Nf + " " + " " + N);
            }
        }
        N.normalize();

        return N.getPoint3f();
    }

    public void computeVerticesNormals() {
        verticesNormals = new ArrayList<Vector3D>();
        for (int i = 0; i < vertices.size(); i++) {
            verticesNormals.add(new Vector3D(getNormalVertex(i)));
        }

//        ArrayList<Vector3D> facesNormals = computeSurfaceNormalsFaces();
//        List<Integer> indices = this.getUniqueVerticesIndexes();
//        
//        for (int i = 0; i < unique_vertices.size(); i++) {
//            float x, y, z;
//            int triangleIndex;
//            int cpt = 0;
//            Vector3D P;
//            x = y = z = 0;
//            // FIXME hashmap ? see computeUniqueVertices
//            for (int j = 0; j < indices.size(); j++) {
//                if (indices.get(j) == i) {
//                    triangleIndex = (int) ((j) / 3);
//                    x += facesNormals.get(triangleIndex).x;
//                    y += facesNormals.get(triangleIndex).y;
//                    z += facesNormals.get(triangleIndex).z;
//                    cpt++;
//                }
//            }
//            P = new Vector3D(x / (float) cpt, y / (float) cpt, z / (float) cpt);
//            P.normalize();
//            verticesNormals.add(P);
//        }
    }

    public void computeVerticesNormalsWeighted() {
        verticesNormals = new ArrayList<Vector3D>();
        for (int i = 0; i < vertices.size(); i++) {
            verticesNormals.add(new Vector3D(getNormalVertexWeighted(i)));
        }
    }

    public Point3f getTangentVector(int v0, int v1) {
        Vector3D PPi = new Vector3D(vertices.get(v0), vertices.get(v1));
        double ppiN = PPi.dotProduct(new Vector3D(getNormalVertex(v0)));
        Vector3D proj = PPi.multiply(ppiN);
        Vector3D res = new Vector3D(proj, PPi);
        res.normalize();

        return res.getPoint3f();
    }

    public double getCurvatureTangent(int v0, int v1) {
        Vector3D PPi = new Vector3D(vertices.get(v0), vertices.get(v1));
        Vector3D NNi = new Vector3D(getNormalVertexWeighted(v0), getNormalVertexWeighted(v1));

        double a = PPi.dotProduct(NNi);
        double b = PPi.dotProduct(PPi);

        return -(a / b);
    }

    private int getMaxCurvatureTangentIndex(int v0) {
        ArrayList<Integer> li = getNeighborVertices(v0);
        double kmax = Double.NEGATIVE_INFINITY;
        int kid = -1;

        for (int i = 0; i < li.size(); i++) {
            double k = getCurvatureTangent(v0, li.get(i));
            if (k > kmax) {
                kmax = k;
                kid = li.get(i);
            }
        }
        // ERROR
        if (kid == -1) {
            IJ.log("curf  tan idx : " + v0 + " " + li + " " + kmax);
        }
        return kid;
    }

    private double angleTangent(int v0, int v1, Point3f P) {
        Vector3D PP = new Vector3D(P);
        Vector3D P0P1 = new Vector3D(vertices.get(v0), vertices.get(v1));

        return P0P1.angle(PP);
    }

    private ArrayList<Double> getAnglesVertice(int ve) {

        ArrayList<Double> ang = new ArrayList<Double>();
        List<Integer> vfaces = vertices_faces_index.get(ve);
        for (Integer vface : vfaces) {
            int base = (vface / 3) * 3;
            int v1 = faces_vertices_index.get(base);
            int v2 = faces_vertices_index.get(base + 1);
            int v3 = faces_vertices_index.get(base + 2);
            double angle = 0;
            // vertex is first angle 12,13
            if (v1 == ve) {
                Vector3D P1P2 = new Vector3D(vertices.get(v1), vertices.get(v2));
                Vector3D P1P3 = new Vector3D(vertices.get(v1), vertices.get(v3));
                angle = P1P2.angle(P1P3);
            } // vertex is second angle 23,21
            else if (v2 == ve) {
                Vector3D P2P3 = new Vector3D(vertices.get(v2), vertices.get(v2));
                Vector3D P2P1 = new Vector3D(vertices.get(v2), vertices.get(v1));
                angle = P2P3.angle(P2P1);
            } // vertex is second angle 31,32
            else if (v3 == ve) {
                Vector3D P3P1 = new Vector3D(vertices.get(v3), vertices.get(v1));
                Vector3D P3P2 = new Vector3D(vertices.get(v3), vertices.get(v2));
                angle = P3P1.angle(P3P2);
            } else {
                IJ.log("PB angle vertex " + ve + " " + v1 + " " + v2 + " " + v3);
            }
            ang.add(angle);
        }

        return ang;
    }

    private ArrayList<Double> getAreasVertice(int ve) {

        ArrayList<Double> areas = new ArrayList<Double>();
        List<Integer> vfaces = vertices_faces_index.get(ve);
        for (Integer vface : vfaces) {
            int base = (vface / 3) * 3;
            int v1 = faces_vertices_index.get(base);
            int v2 = faces_vertices_index.get(base + 1);
            int v3 = faces_vertices_index.get(base + 2);
            double area;
            Vector3D P1P2 = new Vector3D(vertices.get(v1), vertices.get(v2));
            Vector3D P1P3 = new Vector3D(vertices.get(v1), vertices.get(v3));
            area = 0.5 * P1P2.crossProduct(P1P3).getLength();
            areas.add(area);
        }

        return areas;
    }

    public double getCurvatureGaussBonnet(int ve) {
        double sumAng = 0;
        double sumAreas = 0;

        ArrayList<Double> angles = getAnglesVertice(ve);
        ArrayList<Double> areas = getAreasVertice(ve);
        for (double d : angles) {
            sumAng += d;
            //if(d<0) IJ.log("neg ang "+ve+" "+d);
        }
        for (double d : areas) {
            sumAreas += d;
        }

        return (2 * Math.PI - sumAng) / (0.3333 * sumAreas);
    }

    public double[] getCurvatureFaces(double[] curvatures) {
        int nbFace = faces.size();

        double[] cur = new double[nbFace];

        for (int f = 0; f < nbFace; f += 3) {
            int v0 = faces_vertices_index.get(f);
            int v1 = faces_vertices_index.get(f + 1);
            int v2 = faces_vertices_index.get(f + 2);
            double cu = 0.3333 * (curvatures[v0] + curvatures[v1] + curvatures[v2]);
            cur[f] = cu;
            cur[f + 1] = cu;
            cur[f + 2] = cu;
        }

        return cur;
    }

    public double[] getMeanCurvatureVertexFaces(double[] facesCurvatures) {
        double[] curs = new double[vertices.size()];

        for (int i = 0; i < vertices.size(); i++) {
            List<Integer> vfaces = vertices_faces_index.get(i);
            double cur = 0;
            for (int f : vfaces) {
                cur += facesCurvatures[f];
            }
            curs[i] = cur / (double) vfaces.size();
        }
        return curs;
    }

    public double[] getCurvaturesGaussBonnet() {
        double[] curs = new double[vertices.size()];

        for (int ve = 0; ve < vertices.size(); ve++) {
            curs[ve] = getCurvatureGaussBonnet(ve);
        }

        return curs;
    }

    // Dong 2005 Curvatures estimation on triangular mesh
    public double[] getCurvatures(int v) {
        if (v == 100) {
            IJ.log("Curvature 0 ");

        }
        int imax = getMaxCurvatureTangentIndex(v);
        Vector3D MaxTang = new Vector3D(vertices.get(v), vertices.get(imax));
        MaxTang.normalize();
        ArrayList<Integer> nei = getNeighborVertices(v);
        int si = nei.size();

        double angles[] = new double[si];
        double curvs[] = new double[si];

        for (int i = 0; i < si; i++) {
            angles[i] = angleTangent(v, nei.get(i), MaxTang.getPoint3f());
            curvs[i] = getCurvatureTangent(v, nei.get(i));
        }

        double a11 = 0, a12 = 0, a22 = 0, a13 = 0, a23 = 0;
        double a = getCurvatureTangent(v, imax);

        if (v == 100) {
            IJ.log("Curvature 1 " + a + " " + imax);
        }

        for (int i = 0; i < si; i++) {
            a11 += Math.pow(Math.cos(angles[i]), 2) * Math.pow(Math.sin(angles[i]), 2);
            a12 += Math.pow(Math.cos(angles[i]), 1) * Math.pow(Math.sin(angles[i]), 3);
            a22 += Math.pow(Math.cos(angles[i]), 0) * Math.pow(Math.sin(angles[i]), 4);

            a13 += (curvs[i] - a * Math.pow(Math.cos(angles[i]), 2)) * Math.pow(Math.cos(angles[i]), 1) * Math.pow(Math.sin(angles[i]), 1);
            a23 += (curvs[i] - a * Math.pow(Math.cos(angles[i]), 2)) * Math.pow(Math.cos(angles[i]), 0) * Math.pow(Math.sin(angles[i]), 2);
        }

        if (v == 100) {
            IJ.log("Curvature 2 " + a11 + " " + a12 + " " + a22 + " " + a13 + " " + a23);
        }

        double tmp = a11 * a22 - a12 * a12;
        double b, c;
        if (tmp != 0) {
            b = (a13 * a22 - a23 * a12) / (tmp);
            c = (a11 * a23 - a12 * a13) / (tmp);
        } else {
            b = 0;
            c = 0;
        }

        double G = a * c - b * b / 4.0;
        double H = (a + c) / 2.0;
        double k1 = H - Math.sqrt(H * H - G);
        double k2 = H + Math.sqrt(H * H - G);

        if (v == 100) {
            IJ.log("Curvature 3 " + k1 + " " + k2 + " " + H + " " + G);
        }

        return new double[]{k1, k2, H, G};
    }

    public void drawCurvature(ImageFloat draw, double[] curvatures) {
        deCalibratePoints();
        for (int i = 0; i < vertices.size(); i++) {
            draw.setPixel(new Point3D(vertices.get(i)), (float) curvatures[i]);
        }
        reCalibratePoints();
    }

    public ArrayList<Integer> getNeighborVertices(int v) {
        if (vertices_faces_index == null) {
            computeUniqueVertices();
        }
        List<Integer> li = vertices_faces_index.get(v);
        ArrayList<Integer> res = new ArrayList<Integer>();
        for (int i : li) {
            int idx = (i / 3) * 3;
            for (int j = 0; j < 3; j++) {
                int ii = idx + j;
                if (ii != i) {
                    int vt = faces_vertices_index.get(ii);
                    if (!res.contains(vt)) {
                        res.add(vt);
                    }
                }
            }
        }

        return res;
    }

    @Override
    protected void computeCenter() {
        bx = 0;
        by = 0;
        bz = 0;
        Point3f P;
        Iterator<Point3f> it = vertices.iterator();

        while (it.hasNext()) {
            P = it.next();
            bx += P.x;
            by += P.y;
            bz += P.z;
        }
        int nb = vertices.size();
        bx /= nb;
        by /= nb;
        bz /= nb;

        // Need to compute voxels for volume
        volume = -1;
    }

    @Override
    protected void computeMassCenter(ImageHandler ima) {
        // using voxels

        if (ima != null) {
            cx = 0;
            cy = 0;
            cz = 0;
            double sum = 0;
            double sum2 = 0;
            double pix;
            double pmin = Double.MAX_VALUE;
            double pmax = -Double.MAX_VALUE;

            double i, j, k;
            Voxel3D vox;
            Iterator it = getVoxels().iterator();
            while (it.hasNext()) {
                vox = (Voxel3D) it.next();
                i = vox.getX();
                j = vox.getY();
                k = vox.getZ();

                pix = ima.getPixel(vox);
                cx += i * pix;
                cy += j * pix;
                cz += k * pix;
                sum += pix;
                sum2 += pix * pix;
                if (pix > pmax) {
                    pmax = pix;
                }
                if (pix < pmin) {
                    pmin = pix;
                }
            }
            cx /= sum;
            cy /= sum;
            cz /= sum;

            integratedDensity = sum;

            pixmin = pmin;
            pixmax = pmax;

            // standard dev
            int vol = getVolumePixels();
            sigma = Math.sqrt((sum2 - ((sum * sum) / vol)) / (vol - 1));
        }
    }

    @Override
    protected void computeBounding() {
        xmin = Integer.MAX_VALUE;
        xmax = 0;
        ymin = Integer.MAX_VALUE;
        ymax = 0;
        zmin = Integer.MAX_VALUE;
        zmax = 0;

        Point3f vox;
        Iterator it = vertices.iterator();
        while (it.hasNext()) {
            vox = (Point3f) it.next();
            if (vox.x < xmin) {
                xmin = (int) vox.x;
            }
            if (vox.x > xmax) {
                xmax = (int) vox.x;
            }
            if (vox.y < ymin) {
                ymin = (int) vox.y;
            }
            if (vox.y > ymax) {
                ymax = (int) vox.y;
            }
            if (vox.z < zmin) {
                zmin = (int) vox.z;
            }
            if (vox.z > zmax) {
                zmax = (int) vox.z;
            }
        }
    }

    private void computeUniqueVertices() {
        vertices_faces_index = new ArrayList();
        faces_vertices_index = new ArrayList();
        vertices = new ArrayList();
        IJ.showStatus("Unique Vertices : " + faces.size());

        ArrayList[] vefa = new ArrayList[faces.size()];
        for (int i = 0; i < faces.size(); i++) {
            Point3f P = faces.get(i);
            if (!vertices.contains(P)) {
                vertices.add(P);
                int p = vertices.indexOf(P);
                if (p == 50) {
                    IJ.log(P + " new " + p + " " + i);
                }
                ArrayList list = new ArrayList();
                list.add(i);
                vefa[p] = list;
                faces_vertices_index.add(i, p);
            } else {

                int p = vertices.indexOf(P);
                if (p == 50) {
                    IJ.log(P + " already " + p + " " + i);
                }
                vefa[p].add(i);
                faces_vertices_index.add(i, p);
            }
        }
        for (int i = 0; i < vertices.size(); i++) {
            vertices_faces_index.add(i, vefa[i]);
        }
    }

//    private void computeUniqueVerticesIndexes() {
//        unique_vertices = new ArrayList<Point3f>();
//        vertices_unique_index = new ArrayList<Integer>();
//        Iterator<Point3f> it = faces.iterator();
//        Point3f P;
//        int cpt = 0;
//        while (it.hasNext()) {
//            P = (Point3f) it.next();
//            if (!unique_vertices.contains(P)) {
//                unique_vertices.add(P);
//                vertices_unique_index.add(cpt);
//                cpt++;
//            } else {
//                vertices_unique_index.add(unique_vertices.indexOf(P));
//            }
//        }
//    }
    public List<List<Integer>> getUniqueVerticesIndexes() {
        if (vertices_faces_index == null) {
            computeUniqueVertices();
        }
        return vertices_faces_index;
    }

    @Override
    public void computeContours() {
        // Contours pixels are same as surface vertices
        kdtreeContours = new KDTreeC(3);
        kdtreeContours.setScale3(this.resXY, this.resXY, this.resZ);
        contours = new ArrayList();
        Point3f P;
        // Value ?
        double val = 1;
        Iterator it = vertices.iterator();
        while (it.hasNext()) {
            P = (Point3f) it.next();
            Voxel3D vox = new Voxel3D(P, val);
            contours.add(vox);
            kdtreeContours.add(vox.getArray(), vox);
        }
        // needs to compute area since contours are used to compute areas in other classes
        this.computeSurfaceAreas();
    }

//    private void computeVoxelsFill() {
//        voxels = new ArrayList<Voxel3D>();
//        // start at 1 1 1 to let 0 0 0 as background for fill holes
//        int w = getXmax() - getXmin() + 2;
//        int h = getYmax() - getYmin() + 2;
//        int d = getZmax() - getZmin() + 2;
//        ImageByte mask = new ImageByte("mask",w, h, d);
//
//        Point3f vox;
//        int x, y, z;
//        int val = 1;
//
//        Iterator it = unique_vertices.iterator();
//        while (it.hasNext()) {
//            vox = (Point3f) it.next();
//            x = (int) (vox.getX() - getXmin() + 1);
//            y = (int) (vox.getY() - getYmin() + 1);
//            z = (int) (vox.getZ() - getZmin() + 1);
//            mask.putPixel(x, y, z, val);
//        }
//        // fill holes 3D
//        mask.fillHoles3D(0, val);
//
//        // create voxels lists
//        double xx, yy, zz;
//        for (z = 0; z < mask.getSizez(); z++) {
//            for (y = 0; y < mask.getSizey(); y++) {
//                for (x = 0; x < mask.getSizex(); x++) {
//                    if (mask.getPixel(x, y, z) == val) {
//                        xx = x + getXmin() - 1;
//                        yy = y + getYmin() - 1;
//                        zz = z + getZmin() - 1;
//                        voxels.add(new Voxel3D(xx, yy, zz, val));
//                    }
//                }
//            }
//        }
//
//    }
    // Voxellisation ; coordinates should be backed to pixels coordinates
    private ArrayList<Voxel3D> computeVoxelsMultithread() {
        final int zminv = (int) Math.floor(getZmin()) - 1;
        final int yminv = (int) Math.floor(getYmin()) - 1;
        final int xminv = (int) Math.floor(getXmin()) - 1;
        final int zmaxv = (int) Math.ceil(getZmax()) + 1;
        final int ymaxv = (int) Math.ceil(getYmax()) + 1;
        final int xmaxv = (int) Math.ceil(getXmax()) + 1;
        final int n_cpus = ThreadUtil.getNbCpus();
        final int val = this.getValue();
        final ArrayList[] voxelss = new ArrayList[n_cpus];
        for (int i = 0; i < voxelss.length; i++) {
            voxelss[i] = new ArrayList();
        }
        final Vector3D dir0 = new Vector3D(1, 0, 0);
        final Vector3D dir1 = new Vector3D(-1, 0, 0);
        final int dec = (int) Math.ceil((double) (zmaxv - zminv + 1) / (double) n_cpus);
        //IJ.log("dec " + dec + " " + zminv + " " + zmaxv + " " + n_cpus);
        final AtomicInteger ai = new AtomicInteger(0);
        Thread[] threads = ThreadUtil.createThreadArray(n_cpus);
        for (int ithread = 0; ithread < threads.length; ithread++) {
            threads[ithread] = new Thread() {
                @Override
                public void run() {
                    boolean in;
                    Point3D origin;

                    for (int k = ai.getAndIncrement(); k < n_cpus; k = ai.getAndIncrement()) {
                        int zmaxx = Math.min(zminv + (k + 1) * dec, zmaxv);
                        for (float zi = zminv + dec * k; zi < zmaxx; zi += 1) {
                            IJ.showStatus(k + " : Voxellisation " + zi + "/" + (zmaxx - 1));
                            for (float yi = yminv; yi <= ymaxv; yi += 1) {
                                in = false;
                                float xi = xminv;
                                while (xi <= xmaxv) {
                                    if (!in) {
                                        origin = new Point3D(xi, yi + 0.5, zi + 0.5);
                                        double distMin = minDistSquareTriangles(origin, dir0, 1.0);
                                        if (distMin <= 1) {
                                            in = true;
                                        } else if ((distMin >= 9.0) && (distMin < Double.MAX_VALUE)) {
                                            //IJ.log(" " + distMin + " " + (Math.floor(Math.sqrt(distMin)) - 1));
                                            xi += Math.floor(Math.sqrt(distMin)) - 1;
                                        } else if (distMin == Double.MAX_VALUE) {
                                            xi += xmaxv;
                                        }
                                    } else {
                                        // voxels are in pixels not calibrated units
                                        voxelss[k].add(new Voxel3D(xi, yi, zi, val));
                                        origin = new Point3D(xi, yi + 0.5, zi + 0.5);
                                        double distMin = minDistSquareTriangles(origin, dir1, 1);
                                        if (distMin <= 1) {
                                            in = false;
                                        } else if ((distMin >= 9.0) && (distMin < Double.MAX_VALUE)) {
                                            //IJ.log(" " + distMin + " " + (Math.floor(Math.sqrt(distMin)) - 1));
                                            int nb = (int) Math.floor(Math.sqrt(distMin));
                                            if (xi + nb < xmaxv) {
                                                addLineXVoxels(voxelss[k], val, xi + 1, yi, zi, nb);
                                                xi += nb;
                                                in = false;
                                            }
                                        }
                                    }
                                    xi += 1;
                                }
                            }
                        }
                    }
                }
            };
        }
        ThreadUtil.startAndJoin(threads);

        // put all arrays in one
        ArrayList<Voxel3D> newVox = new ArrayList<Voxel3D>();
        for (ArrayList voxels1 : voxelss) {
            newVox.addAll(voxels1);
        }
        return newVox;
    }

    private ArrayList<Voxel3D> computeVoxels() {
        Point3D origin;
        boolean in;
        Vector3D dir0 = new Vector3D(1, 0, 0);
        Vector3D dir1 = new Vector3D(-1, 0, 0);

        int val = this.getValue();

        ArrayList<Voxel3D> newVox = new ArrayList<Voxel3D>();

        final float zminv = getZmin() - (float) 1;
        final float yminv = getYmin() - (float) 1;
        final float xminv = getXmin() - (float) 1;
        final float zmaxv = getZmax() + (float) 1;
        final float ymaxv = getYmax() + (float) 1;
        final float xmaxv = getXmax() + (float) 1;
        for (float zi = zminv; zi <= zmaxv; zi += 1) {
            IJ.showStatus("Voxellisation " + zi + "/" + zmaxv);
            for (float yi = yminv; yi <= ymaxv; yi += 1) {
                in = false;
                float xi = xminv;
                while (xi <= xmaxv) {
                    if (!in) {
                        //origin = new Point3D(xi, yi + 0.5 * resXY, zi + 0.5 * resZ);
                        origin = new Point3D(xi, yi + 0.5, zi + 0.5);
                        double distMin = minDistSquareTriangles(origin, dir0, 1);
                        if (distMin <= 1) {
                            in = true;
                        } else if ((distMin >= 9.0) && (distMin < Double.MAX_VALUE)) {
                            xi += Math.floor(Math.sqrt(distMin)) - 1;
                        }
                    } else {
                        // voxels are in pixels not calibrated units
                        newVox.add(new Voxel3D(xi, yi, zi, val));
                        origin = new Point3D(xi, yi + 0.5, zi + 0.5);
                        double distMin = minDistSquareTriangles(origin, dir1, 1);
                        if (distMin <= 1) {
                            in = false;
                        } else if ((distMin >= 9.0) && (distMin < Double.MAX_VALUE)) {
                            int nb = (int) Math.floor(Math.sqrt(distMin));
                            if (xi + nb < xmaxv) {
                                addLineXVoxels(newVox, val, xi + (float) 1, yi, zi, nb);
                                xi += nb;
                                in = false;
                            }
                        }
                    }
                    xi += 1;
                }
            }
        }
        return newVox;
    }

    private void addLineXVoxels(ArrayList<Voxel3D> list, int val, float x0, float y0, float z0, int nb) {
        for (float i = 0; i < nb; i += 1) {
            list.add(new Voxel3D((x0 + i), y0, z0, val));
        }
    }

    private double minDistSquareTriangles(Point3D origin, Vector3D dir, double inf) {
        double inf2 = inf * inf;
        double distMin = Double.MAX_VALUE;
        for (int i = 0; i < faces.size(); i += 3) {
            double dist = distanceSquareIntersectTriangle(faces.get(i), faces.get(i + 1), faces.get(i + 2), dir, origin);
            if (dist <= distMin) {
                distMin = dist;
                if (distMin <= inf2) {
                    return distMin;
                }
            }
        }

        return distMin;
    }

    private boolean intersectTriangle2(Point3f A, Point3f B, Point3f C, Vector3D dir, Point3D origin) {
        //Calcul des vecteurs u et v qui engendrent le plan du triangle
        //Point3f A, B, C;
        Vector3D u, v;
        //ArrayList<Triangle> triangles = computeTriangleList(this.getUniqueVerticesIndexes());
        //Triangle t = triangles.get(ti);

        //A = vertices.get(t.getVertices().get(0));
        //B = vertices.get(t.getVertices().get(1));
        //C = vertices.get(t.getVertices().get(2));
        u = new Vector3D(A, B);
        v = new Vector3D(A, C);

        Vector3D n = u.crossProduct(v);
        Point3D intersectionPoint;
        boolean inter = false;
        //Calcul de la normale du triangle t de maillage de l'objet k

        double prodNormDir = n.dotProduct(dir); //               dotProduct(n, dir);

        // Si le plan du triangle n est pas parallele au rayon, donc visible depuis la camera:
        if (prodNormDir <= 0.0) // cos inferieur a 0 => la normal et la direction forment un angle superieur à 90 degres
        {
            //Calcul de w, vecteur du point d'origine du rayon au point A
            //Point3f w = new Point3f(origin.x - A.x, origin.y - A.y, origin.z - A.z);
            Vector3D w = new Vector3D(new Point3D(A), origin);

            //Point3f w = new Point3f(A.x - origin.x,A.y - origin.y,A.z - origin.z);  
            //Les coordonées baricentriques du point d'intersection I sont donc, par rapport aux vecteurs u et v qui engendrent le triangle:
            //float Ix = dotProduct(crossProduct(w, v), dir) / prodNormDir;
            double Ix = (w.crossProduct(v)).dotProduct(dir) / prodNormDir;
            //float Iy = dotProduct(crossProduct(u, w), dir) / prodNormDir;
            double Iy = (u.crossProduct(w)).dotProduct(dir) / prodNormDir;
            // distance du triangle a l origine.
            //float Ir = -dotProduct(n, w) / prodNormDir;
            double Ir = -n.dotProduct(w) / prodNormDir;

            //Test: Si le rayon traverse le triangle: la somme des coordonnees suivant u et v ne doivent pas depaser 1, et les deux doivent etre positifs (puisque dans la direction de u et v, "vers" le triangle)
            if ((Ix + Iy <= 1) && ((Ix >= 0) && (Iy >= 0)) && (Ir >= 0)) {
                //baricentriques = Vec3Df(Ix, Iy, Iz);
                intersectionPoint = new Point3D(A.x + (u.x * Ix) + (v.x * Iy), A.y + (u.y * Ix) + (v.y * Iy), A.z + (u.z * Ix) + (v.z * Iy));
                //if (distance(intersectionPoint, origin) <= 1) {
                if (origin.distance(intersectionPoint) <= 1) {
                    inter = true;
                }
            }
        }
        return inter;
    }

    private boolean intersectTriangle(Point3f A, Point3f B, Point3f C, Vector3D dir, Point3D origin) {
        double dist = distanceSquareIntersectTriangle(A, B, C, dir, origin);
        return dist <= 1;
    }

    private double distanceSquareIntersectTriangle(Point3f A, Point3f B, Point3f C, Vector3D dir, Point3D origin) {
        //Calcul des vecteurs u et v qui engendrent le plan du triangle
        //Point3f A, B, C;
        Vector3D u, v;
        //ArrayList<Triangle> triangles = computeTriangleList(this.getUniqueVerticesIndexes());
        //Triangle t = triangles.get(ti);

        //A = vertices.get(t.getVertices().get(0));
        //B = vertices.get(t.getVertices().get(1));
        //C = vertices.get(t.getVertices().get(2));
        u = new Vector3D(A, B);
        v = new Vector3D(A, C);

        Vector3D n = u.crossProduct(v);
        Point3D intersectionPoint;
        //Calcul de la normale du triangle t de maillage de l'objet k

        double prodNormDir = n.dotProduct(dir); //               dotProduct(n, dir);

        // Si le plan du triangle n est pas parallele au rayon, donc visible depuis la camera:
        if (prodNormDir <= 0.0) // cos inferieur a 0 => la normal et la direction forment un angle superieur à 90 degres
        {
            //Calcul de w, vecteur du point d'origine du rayon au point A
            //Point3f w = new Point3f(origin.x - A.x, origin.y - A.y, origin.z - A.z);
            Vector3D w = new Vector3D(new Point3D(A), origin);

            //Point3f w = new Point3f(A.x - origin.x,A.y - origin.y,A.z - origin.z);  
            //Les coordonées baricentriques du point d'intersection I sont donc, par rapport aux vecteurs u et v qui engendrent le triangle:
            //float Ix = dotProduct(crossProduct(w, v), dir) / prodNormDir;
            double Ix = (w.crossProduct(v)).dotProduct(dir) / prodNormDir;
            //float Iy = dotProduct(crossProduct(u, w), dir) / prodNormDir;
            double Iy = (u.crossProduct(w)).dotProduct(dir) / prodNormDir;
            // distance du triangle a l origine.
            //float Ir = -dotProduct(n, w) / prodNormDir;
            double Ir = -n.dotProduct(w) / prodNormDir;

            //Test: Si le rayon traverse le triangle: la somme des coordonnees suivant u et v ne doivent pas depaser 1, et les deux doivent etre positifs (puisque dans la direction de u et v, "vers" le triangle)
            if ((Ix + Iy <= 1) && ((Ix >= 0) && (Iy >= 0)) && (Ir >= 0)) {
                //baricentriques = Vec3Df(Ix, Iy, Iz);
                intersectionPoint = new Point3D(A.x + (u.x * Ix) + (v.x * Iy), A.y + (u.y * Ix) + (v.y * Iy), A.z + (u.z * Ix) + (v.z * Iy));
                //if (distance(intersectionPoint, origin) <= 1) {
                return origin.distanceSquare(intersectionPoint);
            }
        }
        return Double.MAX_VALUE;
    }

    /**
     *
     * @param transform
     * @param meridians
     * @param parallels
     * @return
     */
    public static List<Point3f> createSphere(GeomTransform3D transform, final int meridians, final int parallels) {
        final double[][][] globe = generateGlobe(meridians, parallels);
        Vector3D zero_vector = new Vector3D(0, 0, 0);
        for (int j = 0; j < globe.length; j++) {
            for (int k = 0; k < globe[0].length; k++) {
                Vector3D point = new Vector3D(globe[j][k][0], globe[j][k][1], globe[j][k][2]);
                Vector3D res = transform.getVectorTransformed(point, zero_vector);
                globe[j][k][0] = res.getX();
                globe[j][k][1] = res.getY();
                globe[j][k][2] = res.getZ();
            }
        }
        // create triangular faces and add them to the list
        final ArrayList<Point3f> list = new ArrayList<Point3f>();
        for (int j = 0; j < globe.length - 1; j++) { // the parallels
            for (int k = 0; k < globe[0].length - 1; k++) { // meridian points
                if (j != globe.length - 2) {
                    // half quadrant (a triangle)
                    list.add(new Point3f((float) globe[j + 1][k + 1][0], (float) globe[j + 1][k + 1][1], (float) globe[j + 1][k + 1][2]));
                    list.add(new Point3f((float) globe[j][k][0], (float) globe[j][k][1], (float) globe[j][k][2]));
                    list.add(new Point3f((float) globe[j + 1][k][0], (float) globe[j + 1][k][1], (float) globe[j + 1][k][2]));
                }
                if (j != 0) {
                    // the other half quadrant
                    list.add(new Point3f((float) globe[j][k][0], (float) globe[j][k][1], (float) globe[j][k][2]));
                    list.add(new Point3f((float) globe[j + 1][k + 1][0], (float) globe[j + 1][k + 1][1], (float) globe[j + 1][k + 1][2]));
                    list.add(new Point3f((float) globe[j][k + 1][0], (float) globe[j][k + 1][1], (float) globe[j][k + 1][2]));
                }
            }
        }
        return list;
    }

    /**
     * Generate a globe of radius 1.0 that can be used for any Ball. First
     * dimension is Z, then comes a double array x,y. Minimal accepted meridians
     * and parallels is 3.
     */
    private static double[][][] generateGlobe(int meridians, int parallels) {
        if (meridians < 3) {
            meridians = 3;
        }
        if (parallels < 3) {
            parallels = 3;
        }
        /*
         * to do: 2 loops: -first loop makes horizontal circle using meridian
         * points. -second loop scales it appropriately and makes parallels.
         * Both loops are common for all balls and so should be done just once.
         * Then this globe can be properly translocated and resized for each
         * ball.
         */
        // a circle of radius 1
        double angle_increase = 2 * Math.PI / meridians;
        double temp_angle = 0;
        final double[][] xy_points = new double[meridians + 1][2];    //plus 1 to repeat last point
        xy_points[0][0] = 1;     // first point
        xy_points[0][1] = 0;
        for (int m = 1; m < meridians; m++) {
            temp_angle = angle_increase * m;
            xy_points[m][0] = Math.cos(temp_angle);
            xy_points[m][1] = Math.sin(temp_angle);
        }
        xy_points[xy_points.length - 1][0] = 1; // last point
        xy_points[xy_points.length - 1][1] = 0;

        // Build parallels from circle
        angle_increase = Math.PI / parallels;   // = 180 / parallels in radians
        final double[][][] xyz = new double[parallels + 1][xy_points.length][3];
        for (int p = 1; p < xyz.length - 1; p++) {
            double radius = Math.sin(angle_increase * p);
            double Z = Math.cos(angle_increase * p);
            for (int mm = 0; mm < xyz[0].length - 1; mm++) {
                //scaling circle to appropriate radius, and positioning the Z
                xyz[p][mm][0] = xy_points[mm][0] * radius;
                xyz[p][mm][1] = xy_points[mm][1] * radius;
                xyz[p][mm][2] = Z;
            }
            xyz[p][xyz[0].length - 1][0] = xyz[p][0][0];  //last one equals first one
            xyz[p][xyz[0].length - 1][1] = xyz[p][0][1];
            xyz[p][xyz[0].length - 1][2] = xyz[p][0][2];
        }

        // south and north poles
        for (int ns = 0; ns < xyz[0].length; ns++) {
            xyz[0][ns][0] = 0;	//south pole
            xyz[0][ns][1] = 0;
            xyz[0][ns][2] = 1;
            xyz[xyz.length - 1][ns][0] = 0;    //north pole
            xyz[xyz.length - 1][ns][1] = 0;
            xyz[xyz.length - 1][ns][2] = -1;
        }

        return xyz;
    }

    @Override
    protected void computeMoments2(boolean normalize) {
        s200 = 0;
        s110 = 0;
        s101 = 0;
        s020 = 0;
        s011 = 0;
        s002 = 0;

        double i, j, k;

//        while (it.hasNext()) {
//            P = (Point3f) it.next();
//            i = P.x;
//            j = P.y;
//            k = P.z;
//            sxx += (i - bx) * (i - bx);
//            syy += (j - by) * (j - by);
//            szz += (k - bz) * (k - bz);
//            sxy += (i - bx) * (j - by);
//            sxz += (i - bx) * (k - bz);
//            syz += (j - by) * (k - bz);
//        }
        // resolution already set
        // normalize by nb of points
//        double nb = unique_vertices.size();
//        sxx /= nb;
//        syy /= nb;
//        szz /= nb;
//        sxy /= nb;
//        sxz /= nb;
//        syz /= nb;
        // TEST VOXELLISATION
        ArrayList<Voxel3D> voxlist = this.getVoxels();
        Iterator<Voxel3D> it2 = voxlist.iterator();
        Voxel3D v;
        while (it2.hasNext()) {
            v = it2.next();
            i = v.x;
            j = v.y;
            k = v.z;
            s200 += (i - bx) * (i - bx);
            s020 += (j - by) * (j - by);
            s002 += (k - bz) * (k - bz);
            s110 += (i - bx) * (j - by);
            s101 += (i - bx) * (k - bz);
            s011 += (j - by) * (k - bz);
        }

        // resolution already set
        // normalize by nb of points
        double nb = voxlist.size();
        if (normalize) {
            s200 /= nb;
            s020 /= nb;
            s002 /= nb;
            s110 /= nb;
            s101 /= nb;
            s011 /= nb;
        }

        eigen = null;
    }

    @Override
    protected void computeMoments3() {
        s300 = s030 = s003 = 0;
        s210 = s201 = s120 = s021 = s102 = s012 = s111 = 0;

        double i, j, k;

//        while (it.hasNext()) {
//            P = (Point3f) it.next();
//            i = P.x;
//            j = P.y;
//            k = P.z;
//            sxx += (i - bx) * (i - bx);
//            syy += (j - by) * (j - by);
//            szz += (k - bz) * (k - bz);
//            sxy += (i - bx) * (j - by);
//            sxz += (i - bx) * (k - bz);
//            syz += (j - by) * (k - bz);
//        }
        // resolution already set
        // normalize by nb of points
//        double nb = unique_vertices.size();
//        sxx /= nb;
//        syy /= nb;
//        szz /= nb;
//        sxy /= nb;
//        sxz /= nb;
//        syz /= nb;
        //  VOXELLISATION
        ArrayList<Voxel3D> voxlist = this.getVoxels();
        Iterator<Voxel3D> it2 = voxlist.iterator();
        Voxel3D v;
        while (it2.hasNext()) {
            v = it2.next();
            i = v.x;
            j = v.y;
            k = v.z;
            double xx = (i - bx);
            double yy = (j - by);
            double zz = (k - bz);
            s300 += xx * xx * xx;
            s030 += yy * yy * yy;
            s003 += zz * zz * zz;
            s210 += xx * xx * yy;
            s201 += xx * xx * zz;
            s120 += yy * yy * xx;
            s021 += yy * yy * zz;
            s102 += zz * zz * xx;
            s012 += zz * zz * yy;
            s111 += xx * yy * zz;
        }
        // decalibrate in voxels adjust resolution
        s300 *= resXY * resXY * resXY;
        s030 *= resXY * resXY * resXY;
        s003 *= resZ * resZ * resZ;
        s210 *= resXY * resXY * resXY;
        s201 *= resXY * resXY * resZ;
        s120 *= resXY * resXY * resXY;
        s021 *= resXY * resXY * resZ;
        s102 *= resZ * resZ * resXY;
        s012 *= resZ * resZ * resXY;
        s111 *= resXY * resXY * resZ;

        // normalize by nb of points
        double vol = voxlist.size();
        // normalize by volume
        s300 /= vol;
        s030 /= vol;
        s003 /= vol;
        s210 /= vol;
        s201 /= vol;
        s120 /= vol;
        s021 /= vol;
        s102 /= vol;
        s012 /= vol;
        s111 /= vol;
    }

    public void computeMoments4() {
        s400 = s040 = s040 = s220 = s202 = s022 = s121 = s112 = s211 = 0;
        s103 = s301 = s130 = s310 = s013 = s031 = 0;

        //  VOXELLISATION
        ArrayList<Voxel3D> voxlist = this.getVoxels();
        Iterator<Voxel3D> it = voxlist.iterator();
        Voxel3D vox;
        double i, j, k;
        while (it.hasNext()) {
            vox = (Voxel3D) it.next();
            i = vox.getX();
            j = vox.getY();
            k = vox.getZ();
            double xx = (i - bx);
            double yy = (j - by);
            double zz = (k - bz);
            s400 += xx * xx * xx * xx;
            s040 += yy * yy * yy * yy;
            s004 += zz * zz * zz * zz;
            s220 += xx * xx * yy * yy;
            s202 += xx * xx * zz * zz;
            s022 += yy * yy * zz * zz;
            s121 += xx * yy * yy * zz;
            s112 += xx * yy * zz * zz;
            s211 += xx * xx * yy * zz;
            s103 += xx * zz * zz * zz;
            s301 += xx * xx * xx * zz;
            s130 += xx * yy * yy * yy;
            s310 += xx * xx * xx * yy;
            s013 += yy * zz * zz * zz;
            s031 += yy * yy * yy * zz;
        }
        // calibration
        s400 *= resXY * resXY * resXY * resXY;
        s040 *= resXY * resXY * resXY * resXY;
        s004 *= resZ * resZ * resZ * resZ;
        s220 *= resXY * resXY * resXY * resXY;
        s202 *= resXY * resXY * resZ * resZ;
        s022 *= resXY * resXY * resZ * resZ;
        s121 *= resXY * resXY * resXY * resZ;
        s112 *= resXY * resXY * resZ * resZ;
        s211 *= resXY * resXY * resXY * resZ;
        s103 *= resXY * resZ * resZ * resZ;
        s301 *= resXY * resXY * resXY * resZ;
        s130 *= resXY * resXY * resXY * resXY;
        s310 *= resXY * resXY * resXY * resXY;
        s013 *= resXY * resZ * resZ * resZ;
        s031 *= resXY * resXY * resXY * resZ;
    }

    @Override
    public Voxel3D getPixelMax(ImageHandler ima) {
        Voxel3D res = null;
        float pix;
        float max = -Float.MAX_VALUE;
        Iterator it = getVoxels().iterator();
        Voxel3D vox;

        while (it.hasNext()) {
            vox = (Voxel3D) it.next();
            pix = ima.getPixel(vox);
            if (pix > max) {
                max = pix;
                res = new Voxel3D(vox);
            }
        }

        return res;
    }

    @Override
    public ArrayList listVoxels(ImageHandler ima, double threshold) {
        ArrayList vector = new ArrayList();
        Voxel3D pixel;
        float pixvalue;

        Iterator it = getVoxels().iterator();

        while (it.hasNext()) {
            pixel = new Voxel3D((Voxel3D) it.next());
            pixvalue = ima.getPixel(pixel);
            if (pixvalue > threshold) {
                pixel.setValue(pixvalue);
                vector.add(pixel);
            }
        }

        return vector;
    }

    public ArrayList<Voxel3D> getVoxels() {
        //IJ.log("voxels  begin " + voxels+" "+this.resXY+" "+this.resZ);
        // check if already computed
        if (voxels != null) {
            return voxels;
        }
        //compute voxellisation in pixels coordinates (safer ;-)) 
        //IJ.log("before decalibrate " + vertices + " " + unique_vertices);
        //IJ.log("before decalibrate " + unique_vertices.size() + " " + unique_vertices.get(0));
        if ((resXY != 1) || (resZ != 1)) {
            deCalibratePoints();
        }
        //IJ.log("after decalibrate " + unique_vertices.size() + " " + unique_vertices.get(0));
        if (multiThread) {
            voxels = computeVoxelsMultithread();
            //IJ.log("voxelisation multi " + voxels.size() + " " + voxels.get(0));
        } else {
            voxels = computeVoxels();
        }
        //IJ.log("before recalibrate " + unique_vertices.size() + " " + unique_vertices.get(0));
        if ((resXY != 1) || (resZ != 1)) {
            reCalibratePoints();
        }
        //IJ.log("after recalibrate " + unique_vertices.size() + " " + unique_vertices.get(0));

        return voxels;
    }

    protected Object3DVoxels buildObject3DVoxels() {
        Object3DVoxels obj = new Object3DVoxels(this.getVoxels());
        obj.setCalibration(this.getCalibration());

        return obj;
    }

    public Point3f getVertex(int i) {
        return faces.get(i);
    }

    public Point3f getUniqueVertex(int i) {
        return vertices.get(i);
    }

    public int getNbUniqueVertices() {
        return vertices.size();
    }

    /**
     *
     * @param calibrated
     * @return
     */
    public List<Point3f> getSurfaceTriangles(boolean calibrated) {
        if (calibrated) {
            return faces;
        } else {
            return getSurfaceTrianglesPixels(false);
        }
    }

    @Override
    public List computeMeshSurface(boolean calibrated) {
        return getSurfaceTriangles(calibrated);
    }

//    @Override
//    public double pcColoc(Object3D obj) {
//        double pourc;
////        ArrayList<Voxel3D> al1 = this.getVoxels();
////        ArrayList<Voxel3D> al2 = ((Object3DSurface) obj).getVoxels();
////        double cpt = 0;
////        double dist = 0.25;// normally int values (0.25=0.5²)
////        Voxel3D v1, v2;
////        for (Iterator it1 = al1.iterator(); it1.hasNext();) {
////            v1 = (Voxel3D) it1.next();
////            for (Iterator it2 = al2.iterator(); it2.hasNext();) {
////                v2 = (Voxel3D) it2.next();
////                if (v1.distanceSquare(v2) < dist) {
////                    cpt++;
////                }
////            }
////        }
////        pourc = (cpt / al1.size()) * 100;
//        Object3DVoxels obj1 = this.getObject3DVoxels();
//        Object3DVoxels obj2 = obj.getObject3DVoxels();
//        
//        return obj1.pcColoc(obj);
//    }
    public void drawMesh(ObjectCreator3D obj, int col) {
        // No volume, only contours
        Point3f vox;
        Iterator it = vertices.iterator();
        while (it.hasNext()) {
            vox = (Point3f) it.next();
            obj.createPixel((int) vox.x, (int) vox.y, (int) vox.z, col);
        }
    }

    @Override
    public void draw(ObjectCreator3D obj, int col) {
        for (Voxel3D vox : this.getVoxels()) {
            obj.createPixel((int) (Math.round(vox.getX())), (int) (Math.round(vox.getY())), (int) (Math.round(vox.getY())), col);
        }
    }

    @Override
    public void draw(ByteProcessor mask, int z, int col) {
        for (Voxel3D vox : this.getVoxels()) {
            if (Math.abs(z - vox.getZ()) < 0.5) {
                mask.putPixel((int) (Math.round(vox.getX())), (int) (Math.round(vox.getY())), col);
            }
        }
    }

    @Override
    public void draw(ImageStack mask, int col) {
        for (Voxel3D vox : this.getVoxels()) {
            mask.setVoxel((int) (Math.round(vox.getX())), (int) (Math.round(vox.getY())), (int) (Math.round(vox.getY())), col);
        }
    }

    @Override
    public void draw(ImageHandler mask, int col) {
        for (Voxel3D vox : this.getVoxels()) {
            mask.setPixel((int) (Math.round(vox.getX())), (int) (Math.round(vox.getY())), (int) (Math.round(vox.getY())), col);
        }
    }

    public Content drawContent(Image3DUniverse univ, Color3f co, String name) {
        return univ.addTriangleMesh(faces, co, name);
    }

    public Content drawNormal(int i, double le, Image3DUniverse univ, Color3f co, String name, boolean useCalibration) {
        List<Point3f> line = new ArrayList();
        Point3D P0 = new Point3D(vertices.get(i));
        if (!useCalibration) {
            P0.scale(1.0 / resXY, 1.0 / resXY, 1.0 / resZ);
        }
        line.add(P0.getPoint3f());
        Point3D P1 = new Point3D(vertices.get(i));
        P1.translate(verticesNormals.get(i).multiply(le));
        if (!useCalibration) {
            P1.scale(1.0 / resXY, 1.0 / resXY, 1.0 / resZ);
        }
        line.add(P1.getPoint3f());
        return univ.addLineMesh(line, co, name, true);
    }

    public Content drawFacesVertex(int i, Image3DUniverse univ, Color3f co, String name, boolean useCalibration) {
        List<Point3f> face = new ArrayList();

        List<Integer> fas = vertices_faces_index.get(i);
        //IJ.log(" " + i + " " + fas);
        for (int fa : fas) {
            int base = (fa / 3) * 3;
            Point3D P1 = new Point3D(faces.get(base));
            if (!useCalibration) {
                P1.scale(1.0 / resXY, 1.0 / resXY, 1.0 / resZ);
            }
            face.add(P1.getPoint3f());
            P1 = new Point3D(faces.get(base + 1));
            if (!useCalibration) {
                P1.scale(1.0 / resXY, 1.0 / resXY, 1.0 / resZ);
            }
            face.add(P1.getPoint3f());
            P1 = new Point3D(faces.get(base + 2));
            if (!useCalibration) {
                P1.scale(1.0 / resXY, 1.0 / resXY, 1.0 / resZ);
            }
            face.add(P1.getPoint3f());
        }
        return univ.addTriangleMesh(face, co, name);
    }

    public Content drawFacesCurvature(Image3DUniverse univ, double[] cur, String name, boolean useCalibration) {
        List<Point3f> face = new ArrayList();
        double min = cur[0];
        double max = cur[0];
        for (double d : cur) {
            if (d < min) {
                min = d;
            }
            if (d > max) {
                max = d;
            }
        }

        for (int i = 0; i < faces.size(); i += 3) {
            Point3D P1 = new Point3D(faces.get(i));
            if (!useCalibration) {
                P1.scale(1.0 / resXY, 1.0 / resXY, 1.0 / resZ);
            }
            face.add(P1.getPoint3f());
            P1 = new Point3D(faces.get(i + 1));
            if (!useCalibration) {
                P1.scale(1.0 / resXY, 1.0 / resXY, 1.0 / resZ);
            }
            face.add(P1.getPoint3f());
            P1 = new Point3D(faces.get(i + 2));
            if (!useCalibration) {
                P1.scale(1.0 / resXY, 1.0 / resXY, 1.0 / resZ);
            }
            face.add(P1.getPoint3f());

            Color3f col = new Color3f((float) ((cur[i] - min) / (max - min)), (float) ((cur[i] - min) / (max - min)), 0);
            univ.addTriangleMesh(face, col, name + "_" + i);

        }
        return univ.getContent(name);
    }

    public Content drawNeighbors(int v, Image3DUniverse univ, Color3f co, String name, boolean useCalibration) {
        List<Point3f> line = new ArrayList();

        ArrayList<Integer> nei = getNeighborVertices(v);

        for (int i : nei) {
            Point3D P = new Point3D(vertices.get(i));
            if (!useCalibration) {
                P.scale(1.0 / resXY, 1.0 / resXY, 1.0 / resZ);
            }
            line.add(P.getPoint3f());
        }

        return univ.addLineMesh(line, co, name, true);
    }

    @Override
    public void draw(ImageStack mask, int r, int g, int b) {
        Voxel3D vox;
        ImageProcessor tmp;
        Color col = new Color(r, g, b);
        Iterator it = this.getVoxels().iterator();
        while (it.hasNext()) {
            vox = (Voxel3D) it.next();
            tmp = mask.getProcessor((int) (vox.getZ() + 1));
            tmp.setColor(col);
            tmp.drawPixel((int) vox.getX(), (int) vox.getY());
        }
    }

    @Override
    public Roi createRoi(int z) {
        // FIXME coordinates may not be ordered
        float xcoor[] = new float[faces.size()];
        float ycoor[] = new float[faces.size()];

        int i = 0;
        Point3f vox;
        Iterator it = vertices.iterator();
        while (it.hasNext()) {
            vox = (Point3f) it.next();
            if (Math.abs(z - vox.z) < 0.5) {
                xcoor[i] = vox.x;
                ycoor[i] = vox.y;
            }
        }
        PolygonRoi roi = new PolygonRoi(xcoor, ycoor, xcoor.length, Roi.POINT);
        return roi;
    }

    /**
     *
     * @param path
     */
    @Override
    public void writeVoxels(String path) {
        // TODO write voxels
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void translate(double x, double y, double z) {
        Point3f T = new Point3f((float) x, (float) y, (float) z);
        for (Iterator<Point3f> it = faces.iterator(); it.hasNext();) {
            Point3f P = it.next();
            P.add(T);
        }
        init();
        //this.computeUniqueVertices();
    }

    public static List translateTool(List l, float tx, float ty, float tz) {
        List lt = new ArrayList(l.size());
        for (Iterator<Point3f> it = l.iterator(); it.hasNext();) {
            Point3f P = it.next();
            Point3f Pt = new Point3f(P.x + tx, P.y + ty, P.z + tz);
            lt.add(Pt);
        }
        return lt;
    }

    public void scale(double scale) {
        Point3D center = this.getCenterAsVector();
        for (int i = 0; i < faces.size(); i++) {
            Point3f P = this.getVertex(i);
            Point3D P0 = new Vector3D(P);
            Vector3D V0 = new Vector3D(center, P0);
            Vector3D V1 = V0.multiply(scale);
            Point3D P1 = new Vector3D(center);
            P1.translate(V1);
            P.set(P1.getPoint3f());
        }
        init();
        //this.computeUniqueVertices();
    }

    public void scale(double scale, Vector3D dir) {
        Vector3D dirN = dir.getNormalizedVector();
        Point3D center = this.getCenterAsVector();
        for (int i = 0; i < faces.size(); i++) {
            Point3f P = this.getVertex(i);
            Point3D P0 = new Vector3D(P);
            Vector3D V0 = new Vector3D(center, P0);
            double orient = Math.abs((V0.getNormalizedVector()).dotProduct(dirN));
            double sca = 1 + orient * orient * (scale - 1);
            Vector3D V1 = V0.multiply(sca);
            Point3D P1 = new Vector3D(center);
            P1.translate(V1);
            //IJ.log("Scale dir " + i + " " + P + " " + P1+" "+orient+" "+scale+" "+V1);
            P.set(P1.getPoint3f());
        }
        init();
        //this.computeUniqueVertices();
    }

    public void rotate(Vector3D Axis, double angle) {
        GeomTransform3D trans = new GeomTransform3D();
        trans.setRotation(Axis, angle);
        Vector3D center = this.getCenterAsVector();

        for (Point3f v : faces) {
            Vector3D tv = trans.getVectorTransformed(new Vector3D(v), center);
            v.set((float) tv.getX(), (float) tv.getY(), (float) tv.getZ());
        }

        init();
    }

    public ArrayList<Point3f> getRotated(Vector3D Axis, double angle) {
        GeomTransform3D trans = new GeomTransform3D();
        trans.setRotation(Axis, angle);
        Vector3D center = this.getCenterAsVector();
        ArrayList<Point3f> res = new ArrayList(faces.size());
        for (Point3f v : faces) {
            Vector3D tv = trans.getVectorTransformed(new Vector3D(v), center);
            res.add(new Point3f((float) tv.getX(), (float) tv.getY(), (float) tv.getZ()));
        }
        return res;
    }

    @Override
    public int getColoc(Object3D obj) {
        Object3DVoxels obj1 = this.getObject3DVoxels();
        Object3DVoxels obj2 = obj.getObject3DVoxels();

        return obj1.getColoc(obj2);
    }

    @Override
    public boolean hasOneVoxelColoc(Object3D obj) {
        Object3DVoxels obj1 = this.getObject3DVoxels();
        Object3DVoxels obj2 = obj.getObject3DVoxels();

        return obj1.hasOneVoxelColoc(obj2);
    }

    @Override
    public ArrayUtil listValues(ImageHandler ima) {
        ArrayUtil vector = new ArrayUtil(this.getVolumePixels());
        float pixvalue;
        int idx = 0;

        for (Voxel3D pixel : voxels) {
            if (ima.contains(pixel)) {
                pixvalue = ima.getPixel(pixel);
                vector.addValue(idx, pixvalue);
                idx++;
            }
        }
        vector.setSize(idx);
        return vector;
    }
}
