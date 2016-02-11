/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mcib3d.geom.deformation3d;

import java.util.ArrayList;
import javax.vecmath.Point3f;

/**
 *
 * @author lautaro
 */
public class Vertex {
    private Point3f position;
    private ArrayList<Integer> adjTriangles;
    private boolean marker;
    private int Id;
    
    public Vertex(Point3f p){
        this.position = p;
        this.adjTriangles = null;
        this.marker = false;
    }
    
    public Vertex(Point3f p, int id){
        this.position = p;
        this.adjTriangles = null;
        this.marker = false;
        this.Id=id;
    }
    
    public Vertex(Point3f p, ArrayList<Integer> t){
        this.position = p;
        this.adjTriangles = t;
    }
    
    public void setPosition(Point3f p){
        this.position = p;
    }
    
    public void setAdjTriangles(ArrayList<Integer> t){
        this.adjTriangles = t;
    }
    
    public Point3f getPosition(){
        return this.position;
    }
    
    public ArrayList<Integer> getAdjTriangles(){
        return this.adjTriangles;
    }
    
    public void setMarker(boolean b){
        this.marker = b;
    }
    
    public boolean isMarked(){
        return this.marker;
    }
    
    public int getId(){
        return this.Id;
    }
    
    public void setId(int id){
        this.Id=id;
    }
}
