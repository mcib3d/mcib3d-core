package mcib3d.geom;

import ij.measure.ResultsTable;
import mcib3d.image3d.ImageInt;

public class Objects3DPopulationColocalisation {
    // populations
    private Objects3DPopulation population1;
    private Objects3DPopulation population2;
    // stored coloc information
    private int[][] colocs = null;
    private boolean needToComputeColoc=false;

    public Objects3DPopulationColocalisation(Objects3DPopulation population1, Objects3DPopulation population2) {
        this.population1 = population1;
        this.population2 = population2;
        colocs = new int[population1.getNbObjects()][population2.getNbObjects()];
        needToComputeColoc=true;
    }

    // using label images to compute coloc, basically double histogram
    private void computeColocalisationImage(ImageInt image1,ImageInt image2){
        initColocalisation();
        int Xmax=Math.min(image1.sizeX,image2.sizeX);
        int Ymax=Math.min(image1.sizeY,image2.sizeY);
        int Zmax=Math.min(image1.sizeZ,image2.sizeZ);

        for(int z=0;z<Zmax;z++){
            for(int x=0;x<Xmax;x++){
                for(int y=0;y<Ymax;y++){
                    colocs[image1.getPixelInt(x,y,z)][image2.getPixelInt(x,y,z)]++;
                }
            }
        }
        needToComputeColoc=false;
    }

    private void initColocalisation() {
        for (int ia = 0; ia < population1.getNbObjects(); ia++) {
            for (int ib = 0; ib < population2.getNbObjects(); ib++) {
                colocs[ia][ib] = 0;
            }
        }
    }

    public ResultsTable getResultsTable(boolean useValueObject) {
        if(needToComputeColoc) computeColocalisation();
        ResultsTable rt = new ResultsTable();
        for (int ia = 0; ia < population1.getNbObjects(); ia++) {
            rt.incrementCounter();
            if (!useValueObject) {
                rt.setLabel("A" + ia, ia);
            } else {
                rt.setLabel("A" + population1.getObject(ia).getValue(), ia);
            }
            for (int ib = 0; ib < population2.getNbObjects(); ib++) {
                if (!useValueObject) {
                    rt.setValue("B" + ib, ia, colocs[ia][ib]);
                } else {
                    rt.setValue("B" + population2.getObject(ib).getValue(), ia, colocs[ia][ib]);
                }
            }
        }
        return rt;
    }

    private void computeColocalisation(){
        if(needToComputeColoc) computeColocalisation();
        ImageInt image1=population1.drawPopulation();
        ImageInt image2=population2.drawPopulation();

        computeColocalisationImage(image1,image2);
    }

    public int getColoc(int i1, int i2){
        if(needToComputeColoc) computeColocalisation();
        return colocs[i1][i2];
    }
}
