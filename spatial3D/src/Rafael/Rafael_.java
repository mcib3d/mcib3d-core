/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package Rafael;

import ij.IJ;
import ij.WindowManager;
import ij.measure.ResultsTable;
import java.util.ArrayList;
import java.util.Collections;
import mcib3d.geom.Object3D;
import mcib3d.geom.Objects3DPopulation;
import mcib3d.image3d.ImageHandler;
import mcib3d.image3d.ImageInt;
import mcib3d.utils.ArrayUtil;

/**
 *
 * @author thomasb
 */

public class Rafael_ implements ij.plugin.PlugIn {

    private final int UNLABELED = 1;
    private final int ALPHA = 2;
    private final int BETA = 3;
    private final int DELTA = 4;

    public void run(String string) {
        int[] seg;
        int[] typeWat;
        ImageInt imgWatOrig = ImageInt.wrap(WindowManager.getImage("DAPI-seg-wat.tif"));
        ImageInt imgWat = imgWatOrig.duplicate();
        imgWat.replacePixelsValue(1, 0);//delete borders in wat
        ImageInt imgSeg = ImageInt.wrap(WindowManager.getImage("DAPI-seg.tif"));
        ImageInt imgAlpha = ImageInt.wrap(WindowManager.getImage("alpha-bin.tif"));
        if (imgAlpha.getMax() == 255) {
            imgAlpha.divideByValue(255);// 0-1
        }
        ImageInt imgBeta = ImageInt.wrap(WindowManager.getImage("beta-bin.tif"));
        if (imgBeta.getMax() == 255) {
            imgBeta.divideByValue(255);// 0-1
        }
        ImageInt imgDelta = ImageInt.wrap(WindowManager.getImage("delta-bin.tif"));
        if (imgDelta.getMax() == 255) {
            imgDelta.divideByValue(255);// 0-1
        }        // draw
        ImageHandler draw = imgWat.createSameDimensions();
        Objects3DPopulation popWat = new Objects3DPopulation(imgWat);
        seg = new int[popWat.getNbObjects()];
        typeWat = new int[popWat.getNbObjects()];
        Objects3DPopulation popSeg = new Objects3DPopulation(imgSeg);
        for (int i = 0; i < popSeg.getNbObjects(); i++) {
            IJ.log("pop " + i + " " + popSeg.getObject(i) + " " + popSeg.getObject(i).getValue());
        }
        for (int i = 0; i < popWat.getNbObjects(); i++) {
            int col = UNLABELED;
            Object3D obj = popWat.getObject(i);
            double alpha_intden = obj.getIntegratedDensity(imgAlpha);
            double beta_intden = obj.getIntegratedDensity(imgBeta);
            double delta_intden = obj.getIntegratedDensity(imgDelta);
            // alpha max
            if ((alpha_intden > beta_intden) && (alpha_intden > delta_intden)) {
                if (alpha_intden > 10) {
                    obj.setComment("ALPHA");
                    col = ALPHA;
                }
            } // beta max
            else if ((beta_intden > alpha_intden) && (beta_intden > delta_intden)) {
                if (beta_intden > 10) {
                    obj.setComment("BETA");
                    col = BETA;
                }
            } // delta max
            else if ((delta_intden > alpha_intden) && (delta_intden > beta_intden)) {
                if (delta_intden > 10) {
                    obj.setComment("DELTA");
                    col = DELTA;
                }
            } else {
                obj.setComment("UNKNOWN");
            }

            // seg Values
            int segV = (int) obj.getModePixValueNonZero(imgSeg);
            seg[i] = segV;
            typeWat[i] = col;
            obj.setType(col);

            // TEST 9
            if (obj.getValue() == 9) {
                IJ.log(" object 9 " + i);
            }
        }
        // TEST RANDOMISE TYPE
        //type = randomizeType(type);
        typeWat = randomizeType(typeWat);

        int[] typeSeg = new int[(int) (imgSeg.getMax() + 1)];
        // DRAW
        for (int i = 0; i < popWat.getNbObjects(); i++) {
            Object3D obj = popSeg.getObjectByValue(seg[i]);
            int idx = popSeg.getIndexFromValue(seg[i]);
            obj.setType(typeWat[i]);
            obj.draw(draw, typeWat[i]);
            typeSeg[idx] = typeWat[i];
        }

        draw.show("DRAW");

        // ASSO IN DONE ON VALUES
        // TYPE AND SEG IS DONE ON OBJECTS
        // OBJECT = VALUE - 2
        ArrayList<Integer>[] nei = computeAsso(imgWatOrig);

        // TEST neig2
        //nei = computeAsso2(nei);
        // histogram
        ArrayUtil tab = new ArrayUtil(nei.length);
        tab.putValue(0, 0);
        tab.putValue(1, 0);
        for (int i = 2; i < tab.getSize(); i++) {
            tab.putValue(i, nei[i].size() - 2);// remove border and itself
        }

        tab = tab.getIntegerHistogram();

        tab.getPlot().show();

        int le = nei.length;
        int[] aa = new int[le];
        int[] ab = new int[le];
        int[] ad = new int[le];
        int[] bb = new int[le];
        int[] ba = new int[le];
        int[] bd = new int[le];
        int[] dd = new int[le];
        int[] da = new int[le];
        int[] db = new int[le];
        int[] au = new int[le];
        int[] bu = new int[le];
        int[] du = new int[le];

        int na = 0, nb = 0, nd = 0;

        for (int i = 2; i < nei.length; i++) {
            if (typeWat[i - 2] == ALPHA) {
                na++;
                for (int j = 0; j < nei[i].size(); j++) {
                    if ((nei[i].get(j) > 1) && (nei[i].get(j) != i)) {
                        int nt = typeWat[nei[i].get(j) - 2];
                        if (nt == ALPHA) {
                            aa[i]++;
                        } else if (nt == BETA) {
                            ab[i]++;
                        } else if (nt == DELTA) {
                            ad[i]++;
                        } else {
                            au[i]++;
                        }
                    }
                }
            } else if (typeWat[i - 2] == BETA) {
                nb++;
                for (int j = 0; j < nei[i].size(); j++) {
                    if ((nei[i].get(j) > 1) && (nei[i].get(j) != i)) {
                        int nt = typeWat[nei[i].get(j) - 2];
                        if (nt == BETA) {
                            bb[i]++;
                        } else if (nt == DELTA) {
                            bd[i]++;
                        } else if (nt == ALPHA) {
                            ba[i]++;
                        } else {
                            bu[i]++;
                        }
                    }
                }
            } else if (typeWat[i - 2] == DELTA) {
                nd++;
                for (int j = 0; j < nei[i].size(); j++) {
                    if ((nei[i].get(j) > 1) && (nei[i].get(j) != i)) {
                        int nt = typeWat[nei[i].get(j) - 2];
                        if (nt == DELTA) {
                            dd[i]++;
                        } else if (nt == ALPHA) {
                            da[i]++;
                        } else if (nt == BETA) {
                            db[i]++;
                        } else {
                            du[i]++;
                        }
                    }
                }
            }
        }
        int test = 503;
        IJ.log(na + " " + nb + " " + nd + " : " + aa[test] + " " + ab[test] + " " + ad[test] + " " + bb[test] + " " + ba[test] + " " + bd[test] + " " + dd[test] + " " + da[test] + " " + db[test]);

        // stats
        int minaa = 1000;
        int maxaa = 0;
        int sumaa = 0;
        int minab = 1000;
        int maxab = 0;
        int sumab = 0;
        int minad = 1000;
        int maxad = 0;
        int sumad = 0;
        int minau = 1000;
        int maxau = 0;
        int sumau = 0;
        for (int i = 2; i < le; i++) {
            if (typeWat[i - 2] == ALPHA) {
                if (aa[i] < minaa) {
                    minaa = aa[i];
                }
                if (aa[i] > maxaa) {
                    maxaa = aa[i];
                }
                sumaa += aa[i];
                if (ab[i] < minab) {
                    minab = ab[i];
                }
                if (ab[i] > maxab) {
                    maxab = ab[i];
                }
                sumab += ab[i];
                if (ad[i] < minad) {
                    minad = ad[i];
                }
                if (ad[i] > maxad) {
                    maxad = ad[i];
                }
                sumad += ad[i];
                if (au[i] < minau) {
                    minau = au[i];
                }
                if (au[i] > maxau) {
                    maxau = au[i];
                }
                sumau += au[i];
            }
        }
        IJ.log("AA : " + minaa + " " + ((double) sumaa / (double) na) + " " + maxaa);
        IJ.log("AB : " + minab + " " + ((double) sumab / (double) na) + " " + maxab);
        IJ.log("AD : " + minad + " " + ((double) sumad / (double) na) + " " + maxad);
        IJ.log("AU : " + minau + " " + ((double) sumau / (double) na) + " " + maxau);

        int minbb = 1000;
        int maxbb = 0;
        int sumbb = 0;
        int minbd = 1000;
        int maxbd = 0;
        int sumbd = 0;
        int minba = 1000;
        int maxba = 0;
        int sumba = 0;
        int minbu = 1000;
        int maxbu = 0;
        int sumbu = 0;

        for (int i = 2; i < le; i++) {
            if (typeWat[i - 2] == BETA) {
                if (bb[i] < minbb) {
                    minbb = bb[i];
                }
                if (bb[i] > maxbb) {
                    maxbb = bb[i];
                }
                sumbb += bb[i];
                if (bd[i] < minbd) {
                    minbd = bd[i];
                }
                if (bd[i] > maxbd) {
                    maxbd = bd[i];
                }
                sumbd += bd[i];
                if (ba[i] < minba) {
                    minba = ba[i];
                }
                if (ba[i] > maxba) {
                    maxba = ba[i];
                }
                sumba += ba[i];
                if (bu[i] < minbu) {
                    minbu = bu[i];
                }
                if (bu[i] > maxbu) {
                    maxbu = bu[i];
                }
                sumbu += bu[i];
            }
        }
        IJ.log("BB : " + minbb + " " + ((double) sumbb / (double) nb) + " " + maxbb);
        IJ.log("BD : " + minbd + " " + ((double) sumbd / (double) nb) + " " + maxbd);
        IJ.log("BA : " + minba + " " + ((double) sumba / (double) nb) + " " + maxba);
        IJ.log("BU : " + minbu + " " + ((double) sumbu / (double) nb) + " " + maxbu);

        int mindd = 1000;
        int maxdd = 0;
        int sumdd = 0;
        int mindb = 1000;
        int maxdb = 0;
        int sumdb = 0;
        int minda = 1000;
        int maxda = 0;
        int sumda = 0;
        int mindu = 1000;
        int maxdu = 0;
        int sumdu = 0;

        for (int i = 2; i < le; i++) {
            if (typeWat[i - 2] == DELTA) {
                if (dd[i] < mindd) {
                    mindd = dd[i];
                }
                if (dd[i] > maxdd) {
                    maxdd = dd[i];
                }
                sumdd += dd[i];
                if (da[i] < minda) {
                    minda = da[i];
                }
                if (da[i] > maxda) {
                    maxda = da[i];
                }
                sumda += da[i];
                if (db[i] < mindb) {
                    mindb = db[i];
                }
                if (db[i] > maxdb) {
                    maxdb = db[i];
                }
                sumdb += db[i];
                if (du[i] < mindu) {
                    mindu = du[i];
                }
                if (du[i] > maxdu) {
                    maxdu = du[i];
                }
                sumdu += du[i];
            }
        }
        IJ.log("DD : " + mindd + " " + ((double) sumdd / (double) nd) + " " + maxdd);
        IJ.log("DA : " + minda + " " + ((double) sumda / (double) nd) + " " + maxda);
        IJ.log("DB : " + mindb + " " + ((double) sumdb / (double) nd) + " " + maxdb);
        IJ.log("DU : " + mindu + " " + ((double) sumdu / (double) nd) + " " + maxdu);

        // RESULTS
        // convert asso in seg reference
        le = typeSeg.length;
        int[] aaSeg = new int[le];
        int[] abSeg = new int[le];
        int[] adSeg = new int[le];
        int[] bbSeg = new int[le];
        int[] baSeg = new int[le];
        int[] bdSeg = new int[le];
        int[] ddSeg = new int[le];
        int[] daSeg = new int[le];
        int[] dbSeg = new int[le];
        int[] auSeg = new int[le];
        int[] buSeg = new int[le];
        int[] duSeg = new int[le];
        for (int i = 0; i < popWat.getNbObjects(); i++) {
            int idx = popSeg.getIndexFromValue(seg[i]);
            aaSeg[idx] = aa[i + 2];
            abSeg[idx] = ab[i + 2];
            adSeg[idx] = ad[i + 2];
            auSeg[idx] = au[i + 2];
            baSeg[idx] = ba[i + 2];
            bbSeg[idx] = bb[i + 2];
            bdSeg[idx] = bd[i + 2];
            buSeg[idx] = bu[i + 2];
            daSeg[idx] = da[i + 2];
            dbSeg[idx] = db[i + 2];
            ddSeg[idx] = dd[i + 2];
            duSeg[idx] = du[i + 2];
        }
        writeResults(popSeg, aaSeg, abSeg, adSeg, auSeg, typeSeg, ALPHA, "A", "alpha");
        writeResults(popSeg, baSeg, bbSeg, bdSeg, buSeg, typeSeg, BETA, "B", "beta");
        writeResults(popSeg, daSeg, dbSeg, ddSeg, duSeg, typeSeg, DELTA, "D", "delta");

        //ArrayList<Integer>[] nei2 = computeAsso2(nei);
        //IJ.log("asso1 " + (int) (nei.length / 2) + " " + nei[(int) (nei.length / 2)]);
        //IJ.log("asso2 " + (int) (nei2.length / 2) + " " + nei2[(int) (nei2.length / 2)]);
    }

    private void writeResults(Objects3DPopulation popSeg, int[] na, int[] nb, int[] nd, int[] nu, int[] types, int type, String typeName, String name) {
        ResultsTable rt = new ResultsTable();
        int c = 0;
        Objects3DPopulation[] pops = getTypePopulations(popSeg, types);
        Objects3DPopulation popD = pops[2];
        popD.createKDTreeCenters();
        for (int i = 0; i < popSeg.getNbObjects(); i++) {
            if (types[i] == type) {
                rt.incrementCounter();
                Object3D obj = popSeg.getObject(i);
                rt.setValue("val", c, obj.getValue());
                rt.setValue("vol", c, obj.getVolumeUnit());
                rt.setValue("diam", c, obj.getDistCenterMean());
                rt.setValue("elon", c, obj.getMainElongation());
                rt.setValue(typeName + "_A", c, na[i]);
                rt.setValue(typeName + "_B", c, nb[i]);
                rt.setValue(typeName + "_D", c, nd[i]);
                rt.setValue(typeName + "_U", c, nu[i]);
                // closest D 
                Object3D cloD = popD.closestCenter(obj, 0);
                if (cloD == null) {
                    rt.setValue("cloD", c, -1);
                    rt.setValue("cloDi", c, -1);
                } else {
                    rt.setValue("cloD", c, obj.distCenterUnit(cloD));
                    rt.setValue("cloDi", c, cloD.getValue());
                }
                c++;
            }
        }
        rt.show(name);
    }

    private ArrayList<Integer>[] computeAsso2(ArrayList<Integer>[] ne) {
        ArrayList<Integer>[] nei2 = new ArrayList[ne.length];

        for (int n = 0; n < ne.length; n++) {
            ArrayList<Integer> nei = ne[n];
            nei2[n] = new ArrayList();
            for (int i : nei) {
                ArrayList<Integer> nej = ne[i];
                if ((i != 1) && (i != n)) {
                    for (int j : nej) {
                        if (!nei2[n].contains(j) && (j != 1)) {
                            nei2[n].add(j);
                            if (n == 388) {
                                IJ.log("388 " + i + " " + j + " " + nej);
                            }
                        }
                    }
                }
            }
        }

        return nei2;
    }

    private ArrayList<Integer>[] computeAsso(ImageHandler img) {
        //ImagePlus imp = WindowManager.getCurrentImage();
        //ImageHandler img = ImageHandler.wrap(imp);
        int max = (int) img.getMax();

        ArrayList<Integer>[] nei = new ArrayList[max + 1];
        for (int i = 0; i < nei.length; i++) {
            nei[i] = new ArrayList();
        }

        for (int x = 0; x < img.sizeX; x++) {
            for (int y = 0; y < img.sizeY; y++) {
                for (int z = 0; z < img.sizeZ; z++) {
                    if (img.getPixel(x, y, z) == 1) {
                        ArrayUtil tab = img.getNeighborhood3x3x3(x, y, z);
                        tab = tab.distinctValues();
                        for (int j = 0; j < tab.getSize(); j++) {
                            int val = (int) tab.getValue(j);
                            if (!tab.hasOnlyValuesInt(nei[val])) {
                                for (int i = 0; i < tab.getSize(); i++) {
                                    if (!nei[val].contains((int) tab.getValue(i))) {
                                        nei[val].add((int) tab.getValue(i));
                                        if (val == 9) {
                                            IJ.log(" " + tab);
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        IJ.log("Asso 9 : ");
        for (int v : nei[9]) {
            IJ.log(" " + v);
        }
        return nei;
    }

    private Objects3DPopulation[] getTypePopulations(Objects3DPopulation pop, int[] types) {
        Objects3DPopulation popA = new Objects3DPopulation();
        popA.setCalibration(pop.getCalibration());
        Objects3DPopulation popB = new Objects3DPopulation();
        popB.setCalibration(pop.getCalibration());
        Objects3DPopulation popD = new Objects3DPopulation();
        popD.setCalibration(pop.getCalibration());

        for (int i = 0; i < pop.getNbObjects(); i++) {
            Object3D obj = pop.getObject(i);
            IJ.log("type " + types[i] + " " + obj + " " + obj.getValue() + " " + obj.getType());
            switch (obj.getType()) {
                case ALPHA:
                    popA.addObject(obj);
                    break;
                case BETA:
                    popB.addObject(obj);
                    break;
                case DELTA:
                    popD.addObject(obj);
                    break;
                default:
                    break;
            }
        }

        IJ.log("pops " + popA.getNbObjects() + " " + popB.getNbObjects() + " " + popD.getNbObjects());
        return new Objects3DPopulation[]{popA, popB, popD};
    }

    private int[] randomizeType(int[] type) {
        ArrayList<Integer> okcells = new ArrayList();
        int[] rtype = new int[type.length];
        for (int i = 0; i < type.length; i++) {
            rtype[i] = type[i];
            if (type[i] != UNLABELED) {
                okcells.add(i);
            }
        }
        ArrayList<Integer> okcellsorig = new ArrayList();
        okcellsorig.addAll(okcells);
        Collections.shuffle(okcells);

        for (int i = 0; i < okcells.size(); i++) {
            rtype[okcells.get(i)] = type[okcellsorig.get(i)];
        }

        return rtype;
    }
}
