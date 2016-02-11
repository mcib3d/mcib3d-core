/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mcib3d.geom.deformation3d;

import java.util.ArrayList;


/**
 *
 * @author lautaro
 */
public class Triangle {
    private ArrayList<Integer> vertices;
    
    public Triangle(int a, int b, int c){
        ArrayList<Integer> v = new ArrayList<Integer>(3);
        v.add(a);
        v.add(b);
        v.add(c);
        this.vertices =v;
    }
    
    public void setVertices(Integer a, Integer b, Integer c){
        ArrayList<Integer> v = new ArrayList<Integer>(3);
        v.add(a);
        v.add(b);
        v.add(c);
        this.vertices = v;
    } 
    
    public ArrayList<Integer> getVertices(){
        return vertices;
    }
            
}
