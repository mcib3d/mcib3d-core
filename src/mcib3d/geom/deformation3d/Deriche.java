package mcib3d.geom.deformation3d;

import ij.IJ;
import ij.ImageStack;
import java.util.ArrayList;
import javax.vecmath.Point3f;
import javax.vecmath.Vector3d;
import mcib3d.geom.Voxel3D;


/*
 * Classe Gradient: Permet le calcul du gradient d'une suite (ligne) de
 * pixels/voxels
 */
public class Deriche {

    protected ImageStack stack;
    protected double[] line;

    //Constructor
    public Deriche(ImageStack stack) {
        this.stack = stack;
        this.line = null;
    }

    //Constructor
    public Deriche(ImageStack stack, double[] line) {
        this.stack = stack;
        this.line = line;
    }

    //Setters & Getters
    public void setStack(ImageStack stack) {
        this.stack = stack;
    }

    public ImageStack getStack() {
        return this.stack;
    }

    public void setLine(double[] line) {
        this.line = line;
    }

    public double[] getLine() {
        return this.line;
    }

    //Draw line
    public void drawLine(int x0, int y0, int z0, int x1, int y1, int z1, double val) {

        int dx = (x1 - x0);
        int dy = (y1 - y0);
        int dz = (z1 - z0);
        int dist = Math.abs(dx) + Math.abs(dy) + Math.abs(dz);

        //double [] list = new double[dist];

        float xStep = dx / (float) dist;
        float yStep = dy / (float) dist;
        float zStep = dz / (float) dist;

        //System.out.println("Steps: " + xStep + ", " + yStep + ", " + zStep);

        int posX, posY, posZ;

        for (int i = 0; i < dist; i++) {
            posX = Math.round(x0 + (i * xStep));
            posY = Math.round(y0 + (i * yStep));
            posZ = Math.round(z0 + (i * zStep));

            stack.setVoxel(posX, posY, posZ, val);

        }

    }

    public void drawLine(Point3f p1, Point3f p2, double val) {

        float x0, y0, z0, x1, y1, z1;

        x0 = p1.x;
        y0 = p1.y;
        z0 = p1.z;
        x1 = p2.x;
        y1 = p2.y;
        z1 = p2.z;

        int dx = Math.round(x1 - x0);
        int dy = Math.round(y1 - y0);
        int dz = Math.round(z1 - z0);
        int dist = Math.abs(dx) + Math.abs(dy) + Math.abs(dz);

        //double [] list = new double[dist];

        float xStep = dx / (float) dist;
        float yStep = dy / (float) dist;
        float zStep = dz / (float) dist;

        //System.out.println("Steps: " + xStep + ", " + yStep + ", " + zStep);

        int posX, posY, posZ;

        for (int i = 0; i < dist; i++) {
            posX = Math.round(x0 + (i * xStep));
            posY = Math.round(y0 + (i * yStep));
            posZ = Math.round(z0 + (i * zStep));

            stack.setVoxel(posX, posY, posZ, val);

        }

    }

    public double[] extractLine(int x0, int y0, int z0, int x1, int y1, int z1) {

        //if(stack.)

        int dx = (x1 - x0);
        int dy = (y1 - y0);
        int dz = (z1 - z0);
        int dist = Math.abs(dx) + Math.abs(dy) + Math.abs(dz);

        double[] list = new double[dist];

        float xStep = dx / (float) dist;
        float yStep = dy / (float) dist;
        float zStep = dz / (float) dist;

        //System.out.println("Steps: " + xStep + ", " + yStep + ", " + zStep);

        int posX, posY, posZ;

        if (!inStack(x1, y1, z1) || !inStack(x0, y0, z0)) {

            //boolean in = true;
            //double val=0;

            for (int i = 0; i < dist; i++) {
                posX = Math.round(x0 + (i * xStep));
                posY = Math.round(y0 + (i * yStep));
                posZ = Math.round(z0 + (i * zStep));

                if (!inStack(posX, posY, posZ)) {
                    if (i == 0) {
                        list[i] = 0;
                    } else {
                        list[i] = list[i - 1];
                    }
                } else {

                    list[i] = stack.getVoxel(posX, posY, posZ);
                }

                //stack.setVoxel(posX, posY, posZ, 0);

            }

        } else {
            for (int i = 0; i < dist; i++) {
                posX = Math.round(x0 + (i * xStep));
                posY = Math.round(y0 + (i * yStep));
                posZ = Math.round(z0 + (i * zStep));

                list[i] = stack.getVoxel(posX, posY, posZ);

                //stack.setVoxel(posX, posY, posZ, 0);

            }
        }
        return list;
    }

    private boolean inStack(int x, int y, int z) {
        boolean b = x < stack.getWidth() && x >= 0 && y < stack.getHeight() && y >= 0 && z < stack.getSize() && z >= 0;

        return b;
    }

    //Calcul du gradient de "line" 
    public float[] calculGradient() {

        int n = line.length;
        float[] gr = new float[n];

        //Gestion des bords -> "padding"
        gr[0] = (float) (line[1] - line[0]);
        gr[n - 1] = (float) (line[n - 1] - line[n - 2]);

        for (int i = 1; i < n - 1; i++) {
            gr[i] = (float) (line[i + 1] - line[i - 1]);
        }

        return gr;
    }

    //Filtre de Deriche 1D
    public float[] calculeDeriche(float alpha) {

        int n = line.length;
        float[] der = new float[n];
        float[] der1 = new float[n];
        float[] der2 = new float[n];

        if (n > 2) {


            //Constantes		
            float e = (float) Math.exp(-alpha);
            float e2 = (float) Math.exp(-2 * alpha);
            //float c = 1-(float)Math.pow((1-e),2)/e;
            float a0, a1, a2, a3, b1, b2;
            a0 = 0;
            a1 = 1;
            a2 = -1;
            a3 = 0;
            b1 = 2 * e;
            b2 = -e2;

            //Gestion de bords
            der1[0] = (float) line[0] * (a0 + a1) / (1 - b1 - b2);
            der1[1] = (float) line[1] * a0 + a1 * (float) line[0] + der1[0] * (b1 + b2);
            der2[n - 1] = (float) line[n - 1] * (a2 + a3) / (1 - b1 - b2);
            der2[n - 2] = (float) line[n - 1] * a2 + a3 * (float) line[n - 1] + der2[n - 1] * (b1 + b2);

            //Calcul de der1
            for (int i = 2; i < n; i++) {
                der1[i] = (a0 * ((float) line[i])) + (a1 * ((float) line[i - 1])) + (b1 * der1[i - 1]) + (b2 * der1[i - 2]);
            }

            //Calcul de der2
            for (int i = n - 3; i >= 0; i--) {
                der2[i] = (a2 * ((float) line[i + 1])) + (a3 * ((float) line[i + 2])) + (b1 * der2[i + 1]) + (b2 * der2[i + 2]);
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

    //Calcul d'un point (position) à partir d'un point initial, un vecteur 
    //de direction et un entier n 
    public Vector3d newPosition(Point3f position, Point3f direction, float n) {

        double posX, posY, posZ;
        posX = Math.round(position.x + (direction.x * n));
        posY = Math.round(position.y + (direction.y * n));
        posZ = Math.round(position.z + (direction.z * n));

        Vector3d vec = new Vector3d(posX, posY, posZ);

        return vec;

    }

    //Calcul d'un point (position) à partir d'un point initial, un vecteur 
    //de direction et un entier n 
    public Point3f newPositionF(Point3f position, Point3f direction, float n) {

        float posX, posY, posZ;
        posX = (position.x + (direction.x * n));
        posY = (position.y + (direction.y * n));
        posZ = (position.z + (direction.z * n));

        Point3f vec = new Point3f(posX, posY, posZ);

        return vec;

    }

    //Normalisation d'un vecteur
    public float[] normalize(float[] vec, float norm) {
        int n = vec.length;
        float[] out = vec;
        if (norm != 0) {

            for (int i = 0; i < n; i++) {
                out[i] = vec[i] / norm;
            }
        }
        return out;

    }

    //Min & max of a float array
    public float[] minMax(float[] vec) {

        float[] out = new float[2];
        int n = vec.length;
        float min, max;

        if (n != 0) {

            min = max = vec[0];

            for (int i = 1; i < n; i++) {
                if (vec[i] < min) {
                    min = vec[i];
                }
                if (vec[i] > max) {
                    max = vec[i];
                }
            }

            out[0] = min;
            out[1] = max;
        } else {
            out[0] = 0;
            out[1] = 0;
        }


        return out;

    }

    //b = false si fond noir, objet blanc
    public int localMinMax(float[] vec, int seuil, boolean b) {
        int j = 0;
        float p0, p1, p2;

        for (int i = 1; i < vec.length - 1; i++) {
            p0 = vec[i - 1];
            p1 = vec[i];
            p2 = vec[i + 1];

            if (Math.abs(p1) > seuil) {
                //IJ.log("maxlocal " + p0 + " " + p1 + " " + p2+" "+b);
                if ((b && (p1 < 0)) || (!b && (p1 > 0))) {
                    if ((Math.abs(p1) >= Math.abs(p0)) && (Math.abs(p1) >= Math.abs(p2))) {
                        j = i;
                        //IJ.log("MaxLocal OK " + j);
                    }
                }
            }
        }

        return j;
    }

    //Pourcentage de voxels noirs/blancs dans une surface
    public float voxelsCounter(ArrayList<Voxel3D> voxels, ImageStack s, boolean b) {

        float percent = 0.f;
        int c1, c2;
        double val;

        for (int i = 0; i < voxels.size(); i++) {
            val = s.getVoxel(voxels.get(i).getRoundX(), voxels.get(i).getRoundY(), voxels.get(i).getRoundZ());

        }

        return percent;
    }
}
