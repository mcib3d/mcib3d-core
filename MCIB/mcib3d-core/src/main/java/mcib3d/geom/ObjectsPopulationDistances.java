package mcib3d.geom;

import java.util.ArrayList;
import java.util.TreeSet;

public class ObjectsPopulationDistances {
    Objects3DPopulation population;
    TreeSet<ObjectDistBB> borderDistances;
    Object3D currentObject;

    public ObjectsPopulationDistances(Objects3DPopulation population) {
        this.population = population;
    }

    public Objects3DPopulation getPopulation() {
        return population;
    }

    public void setPopulation(Objects3DPopulation population) {
        this.population = population;
        borderDistances = null;
    }


    private void computeTreeSetBorder() {
        borderDistances = new TreeSet(new ComparatorBorderDistance());
        for (int i = 0; i < population.getNbObjects(); i++) {
            Object3D object3D = population.getObject(i);
            ObjectDistBB distBB = new ObjectDistBB(object3D, currentObject.distBorderUnit(object3D));
            borderDistances.add(distBB);
        }
    }

    public ArrayList<ObjectDistBB> closestsBorderK() {
        if (borderDistances == null) computeTreeSetBorder();
        ArrayList<ObjectDistBB> distBBS = new ArrayList<>();
        for (ObjectDistBB objectDistBB : borderDistances) {
            distBBS.add(objectDistBB);
        }

        return distBBS;
    }

    public ArrayList<ObjectDistBB> closestsBorderK(int k) {
        if (borderDistances == null) computeTreeSetBorder();
        ArrayList<ObjectDistBB> distBBS = new ArrayList<>();
        int i = 0;
        for (ObjectDistBB distBB : borderDistances) {
            if (i < k) {
                distBBS.add(distBB);
                i++;
            } else break;
        }

        return distBBS;
    }

    public Object3D kClosestBorder(int k) {
        ArrayList<ObjectDistBB> distBBS = closestsBorderK();

        return distBBS.get(k - 1).getObject3D();


        /*
        ArrayList<Object3D> exclude = new ArrayList<Object3D>();
        exclude.add(ob);
        if (k == 1) {
            return this.closestBorder(ob, exclude);
        }
        int kk = 1;
        Object3D clo = this.closestBorder(ob, exclude);
        exclude.add(clo);
        while ((clo != null) && (kk < k)) {
            clo = this.closestBorder(ob, exclude);
            if (clo != null) {
                exclude.add(clo);
            }
            kk++;
        }
        return clo
        */
    }

    public Object3D getCurrentObject() {
        return currentObject;
    }

    public void setCurrentObject(Object3D O) {
        if (!O.equals(currentObject)) {
            this.currentObject = O;
            borderDistances = null;
        }
    }
}
