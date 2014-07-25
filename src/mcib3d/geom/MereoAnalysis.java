/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mcib3d.geom;

import ij.measure.ResultsTable;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import mcib3d.image3d.ImageHandler;
import mcib3d.image3d.ImageShort;

/**
 *
 * @author thomas
 */
public class MereoAnalysis {

    Objects3DPopulation popA;
    Objects3DPopulation popB;
    // for relationship
    private float RadX, RadY, RadZ;
    String[][] relationships;

    public MereoAnalysis(Objects3DPopulation popA, Objects3DPopulation popB) {
        this.popA = popA;
        this.popB = popB;
        initRelationships();
        RadX = 1;
        RadY = 1;
        RadZ = 1;
    }

    private void initRelationships() {
        relationships = new String[popA.getNbObjects()][popB.getNbObjects()];
        for (int ia = 0; ia < popA.getNbObjects(); ia++) {
            for (int ib = 0; ib < popB.getNbObjects(); ib++) {
                relationships[ia][ib] = MereoObject3D.DC;
            }
        }
    }

    public Objects3DPopulation getPopA() {
        return popA;
    }

    public void setPopA(Objects3DPopulation popA) {
        this.popA = popA;
        initRelationships();
    }

    public Objects3DPopulation getPopB() {
        return popB;
    }

    public void setPopB(Objects3DPopulation popB) {
        this.popB = popB;
        initRelationships();
    }

    public float getRadX() {
        return RadX;
    }

    public void setRadX(float RadX) {
        this.RadX = RadX;
    }

    public float getRadY() {
        return RadY;
    }

    public void setRadY(float RadY) {
        this.RadY = RadY;
    }

    public float getRadZ() {
        return RadZ;
    }

    public void setRadZ(float RadZ) {
        this.RadZ = RadZ;
    }

    public String[][] getRelationships() {
        return relationships;
    }

    public void computeSlowRelationships() {
        // mereo relationship object to object
        for (int ia = 0; ia < popA.getNbObjects(); ia++) {
            for (int ib = 0; ib < popB.getNbObjects(); ib++) {
                MereoObject3D mereo = new MereoObject3D(popA.getObject(ia), popB.getObject(ib), RadX, RadY, RadZ);
                relationships[ia][ib] = mereo.getRCC8Relationship();
            }
        }
    }

    public void computeFastRelationships() {
        int[] size = popB.getMaxSizeAllObjects();
        ImageHandler segB = new ImageShort("popB", size[0] + 1, size[1] + 1, size[2] + 1);
        popB.draw(segB); //segB.show();
        boolean[] checkedObject = new boolean[popB.getNbObjects()];
        int nbA = popA.getNbObjects();
        for (int a = 0; a < nbA; a++) {
            //IJ.log("Processing object A" + a);
            for (int o = 0; o < popB.getNbObjects(); o++) {
                checkedObject[o] = false;
                relationships[a][o] = MereoObject3D.DC;
            }
            Object3D A = popA.getObject(a);
            Object3D Adil = A.getDilatedObject(RadX, RadY, RadZ, false);
            ArrayList<Voxel3D> al = Adil.listVoxels(segB);
            for (Voxel3D vox : al) {
                int pix = (int) vox.getValue();
                if (pix != 0) {
                    int idx = popB.getIndexFromValue(pix);
                    if (!checkedObject[idx]) {
                        checkedObject[idx] = true;
                        Object3D B = popB.getObject(idx);
                        MereoObject3D mereo = new MereoObject3D(A, B, RadX, RadY, RadZ);
                        relationships[a][idx] = mereo.getRCC8Relationship();
                    }
                }
            }
        }
    }

    public ArrayList<Object3D> getObjectsRelation(int obj, int pop, String rel) {
        ArrayList<Object3D> res = new ArrayList<Object3D>();

        if (pop == 0) {
            for (int b = 0; b < popB.getNbObjects(); b++) {
                if (relationships[obj][b].compareToIgnoreCase(rel) == 0) {
                    res.add(popB.getObject(b));
                }
            }
        } else {
            for (int a = 0; a < popA.getNbObjects(); a++) {
                if (relationships[a][obj].compareToIgnoreCase(rel) == 0) {
                    res.add(popA.getObject(a));
                }
            }
        }

        return res;
    }

    public boolean checkObjectsRelation(int obj, int pop, String rel) {
        if (pop == 0) {
            for (int b = 0; b < popB.getNbObjects(); b++) {
                if (relationships[obj][b].compareToIgnoreCase(rel) == 0) {
                    return true;
                }
            }
        } else {
            for (int a = 0; a < popA.getNbObjects(); a++) {
                if (relationships[a][obj].compareToIgnoreCase(rel) == 0) {
                    return true;
                }
            }
        }
        return false;
    }

    public String getResult(int a, int b) {
        return relationships[a][b];
    }

    public String getResults(boolean excludeDC) {
        String res = "";
        for (int ia = 0; ia < popA.getNbObjects(); ia++) {
            for (int ib = 0; ib < popB.getNbObjects(); ib++) {
                if (excludeDC) {
                    if (!relationships[ia][ib].equalsIgnoreCase(MereoObject3D.DC)) {
                        res = res.concat(popA.getObject(ia) + " with " + popB.getObject(ib) + " : " + relationships[ia][ib] + "\n");
                    }
                } else {
                    res = res.concat(popA.getObject(ia) + " with " + popB.getObject(ib) + " : " + relationships[ia][ib] + "\n");
                }
            }
        }
        return res;

    }

    public ResultsTable getResultsTable(boolean excludeDC, boolean useValueObject) {
        ResultsTable rt = new ResultsTable();
        for (int ia = 0; ia < popA.getNbObjects(); ia++) {
            rt.incrementCounter();
            if (!useValueObject) {
                rt.setLabel("A" + ia, ia);
            } else {
                rt.setLabel("A" + popA.getObject(ia).getValue(), ia);
            }
            for (int ib = 0; ib < popB.getNbObjects(); ib++) {
                if (excludeDC) {
                    if (!relationships[ia][ib].equalsIgnoreCase(MereoObject3D.DC)) {
                        if (!useValueObject) {

                            rt.setValue("B" + ib, ia, relationships[ia][ib]);
                        } else {
                            rt.setValue("B" + popB.getObject(ib).getValue(), ia, relationships[ia][ib]);
                        }
                    } else {
                        if (!useValueObject) {
                            rt.setValue("B" + ib, ia, ".");
                        } else {
                            rt.setValue("B" + popB.getObject(ib).getValue(), ia, ".");
                        }
                    }
                } else {
                    if (!useValueObject) {
                        rt.setValue("B" + ib, ia, relationships[ia][ib]);
                    } else {
                        rt.setValue("B" + popB.getObject(ib).getValue(), ia, relationships[ia][ib]);
                    }
                }
            }
        }
        return rt;
    }

    public void writePrologFacts(String filename, String prefixA, String prefixB, boolean excludeDC, boolean useValue) {
        try {
            BufferedWriter out = new BufferedWriter(new FileWriter(filename));
            for (int ia = 0; ia < popA.getNbObjects(); ia++) {
                for (int ib = 0; ib < popB.getNbObjects(); ib++) {
                    if (excludeDC) {
                        if (!relationships[ia][ib].equalsIgnoreCase(MereoObject3D.DC)) {
                            if (!useValue) {
                                out.write(relationships[ia][ib].toLowerCase() + "(" + prefixA + ia + "," + prefixB + ib + ").\n");
                            } else {
                                out.write(relationships[ia][ib].toLowerCase() + "(" + prefixA + popA.getObject(ia).getValue() + "," + prefixB + popB.getObject(ib).getValue() + ").\n");
                            }
                        }
                    } else {
                        if (!useValue) {
                            out.write(relationships[ia][ib].toLowerCase() + "(" + prefixA + ia + "," + prefixB + ib + ").\n");
                        } else {
                            out.write(relationships[ia][ib].toLowerCase() + "(" + prefixA + popA.getObject(ia).getValue() + "," + prefixB + popB.getObject(ib).getValue() + ").\n");
                        }
                    }
                }
            }

        } catch (IOException ex) {
            Logger.getLogger(MereoAnalysis.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
