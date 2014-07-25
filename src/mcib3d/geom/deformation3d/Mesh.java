package mcib3d.geom.deformation3d;


import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import javax.vecmath.Point3f;
import mcib3d.geom.Object3DSurface;
import mcib3d.geom.Voxel3D;
import quickhull3d.Point3d;
import quickhull3d.QuickHull3D;
//import com.sun.j3d.*;


/*
 * Classe Mesh:
 */
public class Mesh {

    //Liste de sommets (vertices)
    protected List<Point3f> vertices = null;
    protected ArrayList<Point3f> unique_vertices = null;
    //protected List<Point3d> triangles = null;
    protected List<Point3f> facesNormals = null;
    protected List<Point3f> verticesNormals = null;
    protected ArrayList<Triangle> triangles = null;
    protected ArrayList<Vertex> vertexList = null;

    //Constructor
    public Mesh(List<Point3f> v) {
        this.vertices = v;
    }

    public void restart(List<Point3f> v){
        if (!vertices.isEmpty())
            vertices.clear();
        if (!unique_vertices.isEmpty())
            unique_vertices.clear();
        this.vertices=v;
    }
    
    public void computeTriangleList(ArrayList<Integer> indices) {
        triangles = new ArrayList<Triangle>();
        int a, b, c;
        for (int i = 0; i < indices.size(); i += 3) {
            a = indices.get(i);
            b = indices.get(i + 1);
            c = indices.get(i + 2);
            Triangle t = new Triangle(a, b, c);
            triangles.add(t);
        }
    }

    //Needs computeUniqueVertices() 
    public void computeVertexList(ArrayList<Integer> indices) {
        vertexList = new ArrayList<Vertex>();
        for (int i = 0; i < unique_vertices.size(); i++) {
            Vertex v = new Vertex(unique_vertices.get(i), i);
            ArrayList<Integer> t = new ArrayList<Integer>();
            for (int j = 0; j < indices.size(); j++) {
                if (indices.get(j) == i) {
                    t.add((int) Math.floor(j / 3));
                }
            }
            v.setAdjTriangles(t);
            vertexList.add(v);
        }
    }

    //Getters
    public ArrayList<Point3f> getUniqueVertices() {
        return unique_vertices;
    }

    public List<Point3f> getVerticesNormals() {
        return verticesNormals;
    }

    public ArrayList<Triangle> getTriangleList() {
        return triangles;
    }

    public ArrayList<Vertex> getVertexList() {
        return vertexList;
    }

    //FROM Object3DSurface.java (modified version)
    //Return: Index of unique_vertices in vertices
    public List<Integer> computeUniqueVertices() {
        unique_vertices = new ArrayList<Point3f>();
        ArrayList<Integer> indices = new ArrayList<Integer>();
        Iterator<Point3f> it = vertices.iterator();
        Point3f P;
        int cpt = 0;
        while (it.hasNext()) {
            P = (Point3f) it.next();
            if (!unique_vertices.contains(P)) {
                unique_vertices.add(P);
                indices.add(cpt);
                cpt++;
            } else {
                indices.add(unique_vertices.indexOf(P));
            }
        }
        return indices;
    }

    //Compute face normals from full vertices list 
    private void getNormals() {
        facesNormals = new ArrayList<Point3f>();
        Point3f v1, v2, normal;
        float n1, n2, n3, norm;
        for (int i = 0; i < vertices.size(); i += 3) {
            //2 vectors
            v1 = new Point3f(vertices.get(i + 1).x - vertices.get(i).x, vertices.get(i + 1).y - vertices.get(i).y, vertices.get(i + 1).z - vertices.get(i).z);
            v2 = new Point3f(vertices.get(i + 2).x - vertices.get(i + 1).x, vertices.get(i + 2).y - vertices.get(i + 1).y, vertices.get(i + 2).z - vertices.get(i + 1).z);
            //normal 
            n1 = (v1.y * v2.z) - (v1.z * v2.y);
            n2 = (v1.z * v2.x) - (v1.x * v2.z);
            n3 = (v1.x * v2.y) - (v1.y * v2.x);

            //normalization
            norm = (float) Math.sqrt((n1 * n1) + (n2 * n2) + (n3 * n3));
            n1 = n1 / (float) norm;
            n2 = n2 / (float) norm;
            n3 = n3 / (float) norm;

            normal = new Point3f(n1, n2, n3);
            //System.out.println(normal.toString());
            facesNormals.add(normal);
        }
        //System.out.println("triangles.size: " + facesNormals.size());
    }
    
    public ArrayList<Point3f> getFacesNormals(){
        return (ArrayList)facesNormals;
    }

    //Compute vertices normals by interpolation of faces normals 
    //Need
    public void computeVerticesNormals(List<Integer> indices) {
        
        getNormals();
        
        verticesNormals = new ArrayList<Point3f>();
        for (int i = 0; i < unique_vertices.size(); i++) {
            float x, y, z;
            int triangleIndex;
            int cpt = 0;
            Point3f P;
            x = y = z = 0.f;
            for (int j = 0; j < indices.size(); j++) {
                if (indices.get(j) == i) {
                    triangleIndex = (int) ((j) / 3);
                    if (!Float.isNaN(facesNormals.get(triangleIndex).x)){
                        
                    x += facesNormals.get(triangleIndex).x;
                    y += facesNormals.get(triangleIndex).y;
                    z += facesNormals.get(triangleIndex).z;
                    //System.out.println("tI: "+triangleIndex);
                   
                    cpt++;
                    }
                }
            }

            P = new Point3f(x / (float) cpt, y / (float) cpt, z / (float) cpt);
            verticesNormals.add(P);
        }

    }

    //Create .OFF
    public void createOFF(List<Integer> indices) {
        System.out.println("OFF");
        System.out.println(unique_vertices.size() + " " + facesNormals.size() + " 0");
        for (int i = 0; i < unique_vertices.size(); i++) {
            System.out.println(unique_vertices.get(i).x + " " + unique_vertices.get(i).y + " " + unique_vertices.get(i).z);
        }

        for (int i = 0; i < indices.size(); i += 3) {
            System.out.println("3 " + indices.get(i) + " " + indices.get(i + 1) + " " + indices.get(i + 2));
        }

    }

    public float distance(Point3f p1, Point3f p2) {
        float d = (float) (((p1.x - p2.x) * (p1.x - p2.x)) + ((p1.y - p2.y) * (p1.y - p2.y)) + ((p1.z - p2.z) * (p1.z - p2.z)));
        return (float) Math.sqrt(d);
    }

    public Point3f computeCenter(List<Point3f> uV) {

        int n = uV.size();
        float x, y, z;
        x = y = z = 0.f;

        for (int i = 0; i < n; i++) {
            x += uV.get(i).x;
            y += uV.get(i).y;
            z += uV.get(i).z;
        }

        Point3f center = new Point3f(x / n, y / n, z / n);

        return center;
    }
    
    public Point3f computeMedian(List<Point3f> uV) {

        int n = uV.size();
        List<Float> x = new ArrayList<Float>();
        List<Float> y = new ArrayList<Float>();
        List<Float> z = new ArrayList<Float>();

        for (int i = 0; i < n; i++) {
            x.add(uV.get(i).x);
            y.add(uV.get(i).y);
            z.add(uV.get(i).z);
        }

        Collections.sort(x);
        Collections.sort(y);
        Collections.sort(z);
        
        int n2 = (int) Math.floor(n/2);
        
        Point3f center = new Point3f(x.get(n2), y.get(n2), z.get(n2));
          
        return center;
    }

    public ArrayList<Point3f> computeBBox(ArrayList<Point3f> uV) {
        ArrayList<Point3f> bBox = new ArrayList<Point3f>(2);

        float xm, ym, zm, xM, yM, zM;
        xm = xM = uV.get(0).x;
        ym = yM = uV.get(0).y;
        zm = zM = uV.get(0).z;

        Point3f p;

        for (int i = 1; i < uV.size(); i++) {
            p = uV.get(i);
            if (xm > p.x) {
                xm = p.x;
            }
            if (ym > p.y) {
                ym = p.y;
            }
            if (zm > p.z) {
                zm = p.z;
            }
            if (xM < p.x) {
                xM = p.x;
            }
            if (yM < p.y) {
                yM = p.y;
            }
            if (zM < p.z) {
                zM = p.z;
            }

        }

        p = new Point3f(xm - 1.0f, ym - 1.0f, zm - 1.0f);
        bBox.add(p);
        p = new Point3f(xM + 1.0f, yM + 1.0f, zM + 1.0f);
        bBox.add(p);

        return bBox;
    }

    public ArrayList<Point3f> triangleBBox(Triangle t) {
        ArrayList<Point3f> bBox = new ArrayList<Point3f>(2);

        ArrayList<Integer> vts = t.getVertices();

        float xm, ym, zm, xM, yM, zM;
        xm = xM = vertexList.get(vts.get(0)).getPosition().x;
        ym = yM = vertexList.get(vts.get(0)).getPosition().y;
        zm = zM = vertexList.get(vts.get(0)).getPosition().z;

        Point3f p;

        for (int i = 1; i < vts.size(); i++) {
            p = vertexList.get(vts.get(i)).getPosition();
            if (xm > p.x) {
                xm = p.x;
            }
            if (ym > p.y) {
                ym = p.y;
            }
            if (zm > p.z) {
                zm = p.z;
            }
            if (xM < p.x) {
                xM = p.x;
            }
            if (yM < p.y) {
                yM = p.y;
            }
            if (zM < p.z) {
                zM = p.z;
            }

        }

        p = new Point3f(xm - 0.1f, ym - 0.1f, zm - 0.1f);
        bBox.add(p);
        p = new Point3f(xM + 0.1f, yM + 0.1f, zM + 0.1f);
        bBox.add(p);


        return bBox;
    }

    public boolean inBBox(ArrayList<Point3f> bBox, Point3f p) {

        boolean b = false;

        if (p.x <= bBox.get(1).x && p.x >= bBox.get(0).x) {
            if (p.y <= bBox.get(1).y && p.y >= bBox.get(0).y) {
                if (p.z <= bBox.get(1).z && p.z >= bBox.get(0).z) {
                    b = true;
                }
            }

        }

        return b;

    }

    //marche que avec des ellipsoides avec direction principales X,Y,Z
    public float volume(ArrayList<Point3f> bBox) {
        float volume;
        float rx, ry, rz;

        rx = (float) (bBox.get(1).x - bBox.get(0).x - 0.2) / 2;
        ry = (float) (bBox.get(1).y - bBox.get(0).y - 0.2) / 2;
        rz = (float) (bBox.get(1).z - bBox.get(0).z - 0.2) / 2;

        volume = (float) ((4 * Math.PI) * rx * ry * rz) / 3;

        return volume;
    }

    public ArrayList<Integer> getAdjVertices(Vertex v) {
        v.setMarker(true);
        ArrayList<Integer> vts = new ArrayList<Integer>();
        ArrayList<Integer> adjTrs = v.getAdjTriangles();
        ArrayList<Integer> temp;
        Vertex tmp;

        for (int i = 0; i < adjTrs.size(); i++) {
            //System.out.println("DEBUG: "+triangles.get(adjTrs.get(i)).getVertices().toString());
            temp = triangles.get(adjTrs.get(i)).getVertices();
            tmp = vertexList.get(temp.get(0));
            if (!tmp.isMarked()) {
                vts.add(temp.get(0));
                tmp.setMarker(true);
            }
            tmp = vertexList.get(temp.get(1));
            if (!tmp.isMarked()) {
                vts.add(temp.get(1));
                tmp.setMarker(true);
            }
            tmp = vertexList.get(temp.get(2));
            if (!tmp.isMarked()) {
                vts.add(temp.get(2));
                tmp.setMarker(true);
            }
        }

        for (int i = 0; i < vts.size(); i++) {
            vertexList.get(vts.get(i)).setMarker(false);
        }
        v.setMarker(false);

        return vts;
    }

    public float dotProduct(Point3f a, Point3f b) {
        float dp;

        dp = (a.x * b.x) + (a.y * b.y) + (a.z * b.z);

        return dp;
    }

    public Point3f crossProduct(Point3f v1, Point3f v2) {

        float n1, n2, n3;

        n1 = (v1.y * v2.z) - (v1.z * v2.y);
        n2 = (v1.z * v2.x) - (v1.x * v2.z);
        n3 = (v1.x * v2.y) - (v1.y * v2.x);

        return new Point3f(n1, n2, n3);
    }

    public Point3f normalization(Point3f v) {
        Point3f nV;

        float norme = (float) Math.sqrt((v.x * v.x) + (v.y * v.y) + (v.z * v.z));

        nV = new Point3f(v.x / norme, v.y / norme, v.z / norme);

        return nV;
    }
    
    public boolean intersectTriangle(int ti, Point3f dir, Point3f origin) {
        //Calcul des vecteurs u et v qui engendrent le plan du triangle
        Point3f A, B, C, u, v;
        Triangle t = triangles.get(ti);

        A = vertexList.get(t.getVertices().get(0)).getPosition();
        B = vertexList.get(t.getVertices().get(1)).getPosition();
        C = vertexList.get(t.getVertices().get(2)).getPosition();
        u = new Point3f(B.x - A.x, B.y - A.y, B.z - A.z);
        v = new Point3f(C.x - A.x, C.y - A.y, C.z - A.z);

        Point3f n = crossProduct(u, v);
        Point3f intersectionPoint;
        boolean inter = false;
        //Calcul de la normale du triangle t de maillage de l'objet k

        float prodNormDir = dotProduct(n, dir);
       

        // Si le plan du triangle n est pas parallele au rayon, donc visible depuis la camera:
        if ((prodNormDir) < 0.0f) // cos inferieur a 1 => la normal et la direction forment un angle inferieur à 90 degres
        {
            //Calcul de w, vecteur du point d'origine du rayon au point A
            Point3f w = new Point3f(origin.x - A.x, origin.y - A.y, origin.z - A.z);
            //Point3f w = new Point3f(A.x - origin.x,A.y - origin.y,A.z - origin.z);  
            //Les coordonées baricentriques du point d'intersection I sont donc, par rapport aux vecteurs u et v qui engendrent le triangle:
            float Ix = dotProduct(crossProduct(w, v), dir) / prodNormDir;
            float Iy = dotProduct(crossProduct(u, w), dir) / prodNormDir;
            // distance du triangle a l origine.
            float Ir = -dotProduct(n, w) / prodNormDir;


            //Test: Si le rayon traverse le triangle: la somme des coordonnees suivant u et v ne doivent pas depaser 1, et les deux doivent etre positifs (puisque dans la direction de u et v, "vers" le triangle)
            if ((Ix + Iy <= 1) && ((Ix >= 0) && (Iy >= 0)) && Ir >= 0) {

                //baricentriques = Vec3Df(Ix, Iy, Iz);
                intersectionPoint = new Point3f(A.x + (u.x * Ix) + (v.x * Iy), A.y + (u.y * Ix) + (v.y * Iy), A.z + (u.z * Ix) + (v.z * Iy));
                if (distance(intersectionPoint, origin) <= 1) {
                    inter = true;
                     
                }
            }
        }
        return inter;
    }
    
    public ArrayList<Point3f> getMainAxis(){
        
        ArrayList<Point3f> mA = new ArrayList<Point3f>();
        Point3f p1, p2;
        float dist = 0.f;
        int i1, i2;
        i1=i2=0;
        
        for (int i = 0; i< unique_vertices.size(); i++){
            for (int j = i; j<unique_vertices.size(); j++){
                p1 = unique_vertices.get(i);
                p2 = unique_vertices.get(j);
                
                if (distance(p1, p2) > dist)
                {
                    dist = distance(p1, p2);
                    i1 = i;
                    i2 = j;
                }
            }
        }
        
        p1 = unique_vertices.get(i1);
        p2 = unique_vertices.get(i2);
        
        mA.add(p1);
        mA.add(p2);
        
        return mA;
        
    }
    
    public ArrayList<Point3f> computeConvexHull3D() {
        QuickHull3D hull = new QuickHull3D();
        
        System.out.println("Computing 3d convex hull...");
        
        Object3DSurface o = new Object3DSurface(vertices);
        
        ArrayList<Voxel3D> pointsList = o.getContours();
        Point3d[] points = new Point3d[pointsList.size()];
        for (int ve = 0; ve < points.length; ve++) {
            points[ve] = new Point3d(pointsList.get(ve).getX(), pointsList.get(ve).getY(), pointsList.get(ve).getZ());
        }
        
        System.out.println("done 1st for");

        hull.build(points);
        hull.triangulate();

        ArrayList<Point3f> convex = new ArrayList<Point3f>();
        int[][] faceIndices = hull.getFaces();
        Point3d[] verticesHull = hull.getVertices();

        for (int k = 0; k < faceIndices.length; k++) {
            for (int ve = 0; ve < 3; ve++) {
                Point3d point = verticesHull[faceIndices[k][ve]];
                convex.add(new Point3f((float) point.x, (float) point.y, (float) point.z));
            }
        }

        return convex;
    }
    
    //dans mesh.java
    public ArrayList<Point3f> invertNormals(ArrayList<Point3f> v){
        
        ArrayList<Point3f> v2 = new ArrayList<Point3f>();
        
        for (int i = 0 ; i < v.size() ; i+=3){
            v2.add(new Point3f(v.get(i)));
            v2.add(new Point3f(v.get(i+2)));
            v2.add(new Point3f(v.get(i+1)));
        }
        
        return v2;        
    }
  
  
}
