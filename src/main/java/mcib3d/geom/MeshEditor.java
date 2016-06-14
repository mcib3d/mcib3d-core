/** Albert Cardona 2007
 *  Released under the terms of the latest edition of the General Public License.
 */
package mcib3d.geom;

import java.util.*;
import org.scijava.vecmath.Point3f;

/**
 *
 **
 * /**
Copyright (C) 2008- 2011 Thomas Boudier
 *

 *
This file is part of mcib3d

mcib3d is free software; you can redistribute it and/or modify
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
public class MeshEditor {

    /**
     * If the Content instance wraps a mesh, smooth it by the
     * fraction K (0, 1).
     * @param orig
     * @param K
     * @return  
     */
    static public List smooth(final List orig, final float K) {
        //final List triangles = c.getMesh();
        List triangles=new ArrayList();
        triangles.addAll(orig);
        if (0 != triangles.size() % 3) {
            System.out.println("MeshEditor.smooth: need a list of points multiple of 3.");
            return null;
        }
        // for each unique point, find which other points are linked by one edge to it.
        // In the triangles List, there are only points, but each sequence of 3 points makes a triangle.
        final HashMap ht = new HashMap();
        for (int i = 0; i < triangles.size(); i += 3) {
            // process one triangle at a time
            Point3f p1 = (Point3f) triangles.get(i);
            Point3f p2 = (Point3f) triangles.get(i + 1);
            Point3f p3 = (Point3f) triangles.get(i + 2);
            build(p1, p2, p3, ht);
            build(p2, p3, p1, ht);
            build(p3, p1, p2, ht);
        }
        /*  // shrinkage correction works, but generates undesirably unsmooth edges
        for (Iterator it = ht.values().iterator(); it.hasNext(); ) {
        PointGroup pg = (PointGroup)it.next();
        pg.computeVector(K);
        }
        for (Iterator it = ht.values().iterator(); it.hasNext(); ) {
        PointGroup pg = (PointGroup)it.next();
        pg.applyVector(ht);
        }
         */
        for (Iterator it = ht.values().iterator(); it.hasNext();) {
            PointGroup pg = (PointGroup) it.next();
            pg.smoothMembers(K);
        }

        //c.update();
        // done!

        return triangles;
    }

    /** Represents one point in 3D space that appears in multiple instances within the triangles list. */
    static private class PointGroup {

        Point3f first;
        HashSet edges = new HashSet();
        ArrayList members = new ArrayList(); // can't be a HashSet, because points will compare as equal
        float vx, vy, vz;

        PointGroup(Point3f first) {
            this.first = first;
            members.add(first);
        }

        void addMember(Point3f p) {
            members.add(p);
        }

        void addEdge(Point3f p) {
            edges.add(p);
        }

        void smoothMembers(float K) {
            // Compute a vector composed of all vectors to other points with which it shares an edge, and add some fraction of that vector to each member point.
            vx = 0;
            vy = 0;
            vz = 0;
            for (Iterator it = edges.iterator(); it.hasNext();) {
                Point3f po = (Point3f) it.next();
                vx += po.x;
                vy += po.y;
                vz += po.z;
            }
            int size = edges.size();
            vx = (vx / size - first.x) * K;
            vy = (vy / size - first.y) * K;
            vz = (vz / size - first.z) * K;
            for (Iterator it = members.iterator(); it.hasNext();) {
                Point3f m = (Point3f) it.next();
                m.x += vx;
                m.y += vy;
                m.z += vz;
            }
        }

        void computeVector(float K) {
            // Compute a vector composed of all vectors to other points with which it shares an edge, and add some fraction of that vector to each member point.
            vx = 0;
            vy = 0;
            vz = 0;
            for (Iterator it = edges.iterator(); it.hasNext();) {
                Point3f po = (Point3f) it.next();
                vx += po.x;
                vy += po.y;
                vz += po.z;
            }
            int size = edges.size();
            vx = (vx / size - first.x) * K;
            vy = (vy / size - first.y) * K;
            vz = (vz / size - first.z) * K;
        }

        void applyVector(HashMap ht) {
            // compute average displacement vector for all neighbors, i.e. edge points
            float ax = 0, ay = 0, az = 0;
            int count = 0;
            for (Iterator it = edges.iterator(); it.hasNext();) {
                PointGroup pg = (PointGroup) ht.get(it.next());
                if (null == pg) {
                    continue;
                }
                count++;
                ax += pg.vx;
                ay += pg.vy;
                az += pg.vz;
            }
            ax += vx;
            ay += vy;
            az += vz;
            count++; // so count can never be zero
            ax /= count;
            ay /= count;
            az /= count;
            // apply to each member the smoothing vector minus average neighborhood smoothing vector to avoid shrinking
            for (Iterator it = members.iterator(); it.hasNext();) {
                Point3f m = (Point3f) it.next();
                m.x += vx;// - ax;
                m.y += vy;// - ay;
                m.z += vz;// - az;
            }
        }
    }

    /** Build a list of points that are just one edge away from p1, and store them in the Hashtable.  */
    static private void build(Point3f p1, Point3f p2, Point3f p3, HashMap ht) {
        PointGroup pg = (PointGroup) ht.get(p1);
        if (null != pg) {
            pg.addMember(p1);
        } else {
            pg = new PointGroup(p1);
            ht.put(p1, pg);
        }
        pg.addEdge(p2);
        pg.addEdge(p3);
    }

    static private final class Vertex {
        // The original

        final private Point3f p;
        // The collection of vertices from the mesh that equal this one
        final private ArrayList<Point3f> copies = new ArrayList<Point3f>();
        // The averaged and later on the smoothed result
        final private Point3f tmp;
        // The number of times it's been averaged
        private int n;

        Vertex(final Point3f p) {
            this.p = p;
            this.tmp = new Point3f(0, 0, 0);
            this.copies.add(p);
        }

        private void reset() {
            this.tmp.set(0, 0, 0);
            this.n = 0;
        }

        @Override
        public final boolean equals(final Object ob) {
            final Vertex v = (Vertex) ob;
            return v.p == this.p || (v.p.x == this.p.x && v.p.y == this.p.y && v.p.z == this.p.z);
        }

        private void average(final Vertex v) {
            // Increment counter for both
            ++this.n;
            ++v.n;
            // compute average of the original coordinates
            final Point3f a = new Point3f((this.p.x + v.p.x) / 2,
                    (this.p.y + v.p.y) / 2,
                    (this.p.z + v.p.z) / 2);
            // Add average to each vertices' tmp
            this.tmp.add(a);
            v.tmp.add(a);
        }

        private void smooth() {
            // Compute smoothed coordinates
            final float f = 0.5f / n;
            tmp.set(0.5f * p.x + f * tmp.x,
                    0.5f * p.y + f * tmp.y,
                    0.5f * p.z + f * tmp.z);
            // Apply them to all copies
            // It doesn't matter if the copies are not unique.
            for (final Point3f p : copies) {
                p.set(tmp);
            }
        }
    }

    static private final class Edge {

        private Vertex v1, v2;

        Edge(final Vertex v1, final Vertex v2) {
            this.v1 = v1;
            this.v2 = v2;
        }

        @Override
        public final boolean equals(final Object ob) {
            final Edge e = (Edge) ob;
            return e == this || (e.v1 == this.v1 && e.v2 == this.v2) || (e.v1 == this.v2 && e.v2 == this.v1);
        }

        private void averageVertices() {
            this.v1.average(this.v2);
        }
    }

    static private Vertex uniqueVertex(final Point3f p, final HashMap<Point3f, Vertex> verts) {
        Vertex v = verts.get(p);
        if (null == v) {
            v = new Vertex(p);
            verts.put(p, v);
        } else {
            v.copies.add(p);
        }
        return v;
    }

    /** Implemented Blender-style vertex smoothing.
     * See Blender's file editmesh_mods.c, at function
     * "static int smooth_vertex(bContext *C, wmOperator *op)"
     *
     * What it does:
     *
     *  1. For each unique edge, compute the average of both vertices
     *     and store it in a Point3f. Also increment a counter for each
     *     vertex indicating that it has been part of an averaging operation.
     *     If the vertex is again part of an averaging operation, just add
     *     the new average to the existing one.
     *
     *  2. For each unique vertex, computer a factor as 0.5/count, where
     *     count is the number of times that the vertex has been part
     *     of an averaging operation. Then set the value of the vertex
     *     to 0.5 times the original coordinates, plus the factor times
     *     the cumulative average coordinates.
     *
     *  The result is beautifully smoothed meshes that don't shrink noticeably.
     *
     *  All kudos to Blender's authors. Thanks for sharing with GPL license.
     * 
     * Taken from ImageJ 3D Viewer
     * @param orig 
     * @param iterations 
     * @return 
     */
    static public List smooth2(final List<Point3f> orig, final int iterations) {
        final HashMap<Point3f, Vertex> verts = new HashMap<Point3f, Vertex>();
        final HashSet<Edge> edges = new HashSet<Edge>();
        List<Point3f> triangles = new ArrayList();
        triangles.addAll(orig);

        // Find unique edges made of unique vertices
        for (int i = 0; i < triangles.size(); i += 3) {
            // process one triangle at a time
            final Vertex v1 = uniqueVertex(triangles.get(i), verts),
                    v2 = uniqueVertex(triangles.get(i + 1), verts),
                    v3 = uniqueVertex(triangles.get(i + 2), verts);

            // Add unique edges only
            edges.add(new Edge(v1, v2));
            edges.add(new Edge(v2, v3));
            edges.add(new Edge(v1, v3));

            if (0 == i % 300 && Thread.currentThread().isInterrupted()) {
                return null;
            }
        }

        for (int i = 0; i < iterations; ++i) {

            if (Thread.currentThread().isInterrupted()) {
                return null;
            }

            // First pass: accumulate averages
            for (final Edge e : edges) {
                e.averageVertices();
            }

            // Second pass: compute the smoothed coordinates and apply them
            for (final Vertex v : verts.values()) {
                v.smooth();
            }

            // Prepare for next iteration
            if (i + 1 < iterations) {
                for (final Vertex v : verts.values()) {
                    v.reset();
                }
            }
        }
        return triangles;
    }
}
