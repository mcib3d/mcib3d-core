package tango.util;
import mcib3d.image3d.ImageFloat;
import java.util.*;
//import ij.*;
@SuppressWarnings (value="unchecked")
public class Sphere3D {
    public float radius, radiusZ;
    public boolean frontZ, frontY, sort, is2D;
    public float[] sphere, front; // %4 : 0:x 1:y 2:z 3:distance
    public int length, length_f;

    public Sphere3D(float radius, float radiusZ){
        this.radius=radius;
        this.radiusZ=radiusZ;
        this.sort=true;
        this.frontY=false;
        this.frontZ=false;
        this.is2D=false;
    }

    public void computeSphere() {
        if (frontZ || frontY) this.computeSphere_front(frontZ);
        else if (is2D) this.computeSphere_temp2D();
        else this.computeSphere_temp();
    }

    public void drawSphere() {
        int size = ((int)(this.radius+0.5))*2+1;
        int sizeZ = ((int)(this.radiusZ+0.5))*2+1;
        int sizeXY=size*size;
        ImageFloat ih = new ImageFloat("sphere 3D", size, size, sizeZ);
        if (frontZ || frontY) {
            for (int idx=0; idx <(front.length-3); idx+=4) {
                int pX=(int)(size/2+front[idx]);
                int pY=(int)(size/2+front[idx+1]);
                int pZ=(int)(sizeZ/2+front[idx+2]);
                ih.pixels[pZ][pX+pY*size] = -front[idx+3]-1000;
            }
        }
        for (int idx=0; idx <(sphere.length-3); idx+=4) {
            int pX=(int)(size/2+sphere[idx]);
            int pY=(int)(size/2+sphere[idx+1]);
            int pZ=(int)(sizeZ/2+sphere[idx+2]);
            ih.pixels[pZ][pX+size*pY] = sphere[idx+3]+1000;
        }
        ih.show();
    }

    private void computeSphere_front(boolean frontZ) { //si !fronZ > frontY
        float rad2 = this.radius*this.radius;
        float r=(float)this.radius/this.radiusZ;
        float rad_min = Math.min(this.radius, this.radiusZ) -1 ;
        int upper_rad=(int)(this.radius+0.5);
        int upper_radz=(int)(this.radiusZ+0.5); // -1 pour eviter les pixels singuliers
        int rad_max = Math.max(upper_rad, upper_radz);
        float[][] temp = new float[(2*upper_rad+1)*(2*upper_rad+1)*(2*upper_radz+1)][4];
        float[][] temp_f = new float[(2*upper_rad+1)*(2*upper_radz+1)][4];
        int count=0;
        int count_f=0;
        for (int z=-upper_radz;z<=upper_radz;z++) {
            for (int y=-upper_rad;y<=upper_rad;y++) {
                for (int x=-upper_rad;x<=upper_rad;x++) {
                    float d2=z*r*z*r+y*y+x*x;
                    if (d2<rad2) {
                        int h = frontZ?(int)((Math.sqrt(rad2-x*x-y*y)/r)-0.5):(int)(Math.sqrt(rad2-x*x-z*z*r*r)-0.5);
                        boolean isFront  = frontZ?z>=h:y>=h;
                        //correction pour les pixels singuliers:
                        //if (frontZ && x==0 && y==0 && z>=(h-1)) isFront = true;
                        //else if (!frontZ && x==0 && z==0 && y>=(h-1)) isFront = true;
                        int lifetime = frontZ?z+h:y+h;
                        //if (frontZ && x==0 && y==0 && z==upper_radz) lifetime--;
                        if (!isFront) {
                            temp[count][0]=x;
                            temp[count][1]=y;
                            temp[count][2]=z;
                            temp[count][3]=lifetime+1;
                            count++;
                        } else {
                            temp_f[count_f][0]=x;
                            temp_f[count_f][1]=y;
                            temp_f[count_f][2]=z;
                            temp_f[count_f][3]=lifetime+1;
                            count_f++;
                        }
                    }
                }
            }
        }
        this.length=count;
        this.length_f=count_f;
        Arrays.sort(temp_f, new SphereComparatorInv());
        Arrays.sort(temp, new SphereComparatorInv());
        this.sphere =new float[count*4];
        this.sphere[0]=0;
        this.sphere[1]=0;
        this.sphere[2]=0;
        this.sphere[3]=frontZ?this.radiusZ:this.radius;
        int idx=1;
        int idx2=0;
        //IJ.log("count:"+count +" count f:"+count_f+" temp length:"+temp.length);
        while (idx<count) {
            if (temp[idx2][0]!=0 || temp[idx2][1]!=0 || temp[idx2][2]!=0) {
                this.sphere[4*idx]=temp[idx2][0];
                this.sphere[4*idx+1]=temp[idx2][1];
                this.sphere[4*idx+2]=temp[idx2][2];
                this.sphere[4*idx+3]=temp[idx2][3];
                idx++;
            }
            idx2++;
        }
        //IJ.log("idx:"+idx+" idx2:"+idx2);
        this.front =new float[count_f*4];
        for (int i = 0; i<count_f; i++) {
            this.front[4*i]=temp_f[i][0];
            this.front[4*i+1]=temp_f[i][1];
            this.front[4*i+2]=temp_f[i][2];
            this.front[4*i+3]=temp_f[i][3];
        }
    }


    private void computeSphere_temp() {
        float r=(float)this.radius/this.radiusZ;
        int upper_rad=(int)(this.radius+0.5);
        int upper_radz=(int)(this.radiusZ+0.5);
        float[][] temp = new float[upper_rad*upper_rad*upper_radz*8][4];
        int count=0;
        for (int z=-upper_radz;z<=upper_radz;z++) {
            for (int y=-upper_rad;y<=upper_rad;y++) {
                for (int x=-upper_rad;x<=upper_rad;x++) {
                    float d=(float)Math.sqrt(z*r*z*r+y*y+x*x);
                    if (d<=this.radius) {
                        temp[count][0]=x;
                        temp[count][1]=y;
                        temp[count][2]=z;
                        temp[count][3]=d;
                        count++;
                    }
                }
            }
        }
        this.length=count;
        if (sort) Arrays.sort(temp, new SphereComparator());
        this.sphere =new float[count*4];
        this.sphere[0]=0;
        this.sphere[1]=0;
        this.sphere[2]=0;
        this.sphere[3]=0;
        int idx=1;
        int idx2=0;
        while (idx<count) {
            if (temp[idx2][0]!=0 || temp[idx2][1]!=0 || temp[idx2][2]!=0) {
                this.sphere[4*idx]=temp[idx2][0];
                this.sphere[4*idx+1]=temp[idx2][1];
                this.sphere[4*idx+2]=temp[idx2][2];
                this.sphere[4*idx+3]=temp[idx2][3];
                idx++;
            }
            idx2++;
        }
    }

    private void computeSphere_temp2D() {
        int upper_rad=(int)(this.radius+0.5);
        float[][] temp = new float[upper_rad*upper_rad*4][4];
        int count=0;
        for (int y=-upper_rad;y<=upper_rad;y++) {
            for (int x=-upper_rad;x<=upper_rad;x++) {
                float d=(float)Math.sqrt(y*y+x*x);
                if (d<=this.radius) {
                    temp[count][0]=x;
                    temp[count][1]=y;
                    temp[count][2]=0;
                    temp[count][3]=d;
                    count++;
                }
            }
        }
        this.length=count;
        if (sort) Arrays.sort(temp, new SphereComparator());
        this.sphere =new float[count*4];
        this.sphere[0]=0;
        this.sphere[1]=0;
        this.sphere[2]=0;
        this.sphere[3]=0;
        int idx=1;
        int idx2=0;
        while (idx<count) {
            if (temp[idx2][0]!=0 || temp[idx2][1]!=0 || temp[idx2][2]!=0) {
                this.sphere[4*idx]=temp[idx2][0];
                this.sphere[4*idx+1]=temp[idx2][1];
                this.sphere[4*idx+2]=temp[idx2][2];
                this.sphere[4*idx+3]=temp[idx2][3];
                idx++;
            }
            idx2++;
        }
    }
    
    public class SphereComparator implements Comparator{
        public int compare(Object array1, Object array2){
            float val1 = ((float[])array1)[3];
            float val2  = ((float[])array2)[3];
            if(val1 > val2)
                return 1;
            else if(val1 < val2)
                return -1;
            else
                return 0;
        }
    }
    public class SphereComparatorInv implements Comparator{
        public int compare(Object array1, Object array2){
            float val1 = ((float[])array1)[3];
            float val2  = ((float[])array2)[3];
            if(val1 > val2)
                return -1;
            else if(val1 < val2)
                return 1;
            else
                return 0;
        }
    }
}
