package mcib3d.tracking_dev;

import ij.IJ;
import ij.measure.ResultsTable;
import mcib3d.geom.Object3D;
import mcib3d.geom.Objects3DPopulation;
import mcib3d.geom.Objects3DPopulationColocalisation;
import mcib3d.image3d.ImageHandler;

import java.util.LinkedList;
import java.util.List;
import java.util.TreeSet;

public class TrackingAssociation {
    List<AssociationPair> finalAssociations = null; // TODO
    List<Object3D> finalOrphan1; // TODO
    List<Object3D> finalOrphan2; // TODO
    List<Mitosis> finalMitosis; // TODO

    private ImageHandler img1;
    private ImageHandler img2;
    private ImageHandler path = null;
    private ImageHandler tracked = null;
    private ImageHandler pathed = null;

    private boolean merge = false;
    private boolean mitosis = false;

    public TrackingAssociation(ImageHandler img1, ImageHandler img2) {
        this.img1 = img1;
        this.img2 = img2;
    }

    public void setPathImage(ImageHandler path) {
        this.path = path;
    }

    public ImageHandler getTrackedImage() {
        if (finalAssociations == null) computeTracking();
        if (this.tracked == null) drawAssociation();

        return this.tracked;
    }

    public ImageHandler getPathedImage() {
        if (finalAssociations == null) computeTracking();
        if (this.path == null) return null;
        if (this.pathed == null) drawAssociation();

        return this.pathed;
    }

    public void setImage1(ImageHandler img1) {
        this.finalAssociations = null;
        this.img1 = img1;
        this.tracked = null;
    }

    public void setImage2(ImageHandler img2) {
        this.finalAssociations = null;
        this.img2 = img2;
        this.tracked = null;
    }

    public void setMerge(boolean merge) {
        this.finalAssociations = null;
        this.merge = merge;
        this.tracked = null;
    }

    public void setMitosis(boolean mitosis) {
        this.finalAssociations = null;
        this.mitosis = mitosis;
        this.tracked = null;
    }

    private void computeTracking() {
        Objects3DPopulation population1 = new Objects3DPopulation(this.img1);
        Objects3DPopulation population2 = new Objects3DPopulation(this.img2);

        Association association = new Association(population1, population2, new CostColocalisation(new Objects3DPopulationColocalisation(population1, population2)));
        association.verbose = false;

        association.computeAssociation();

        MitosisDetector mitosisDetector = new MitosisDetector(this.img1, this.img2, association);

        // compute merging
        if (this.merge) {
            this.img2 = mitosisDetector.detectAndMergeSplit();

            population2 = new Objects3DPopulation(this.img2);
            association = new Association(population1, population2, new CostColocalisation(new Objects3DPopulationColocalisation(population1, population2)));
            association.computeAssociation();
            mitosisDetector = new MitosisDetector(this.img1, this.img2, association);
        }

        // final associations
        finalAssociations = association.getAssociationPairs();
        finalOrphan1 = association.getOrphan1Population().getObjectsList();
        finalOrphan2 = association.getOrphan2Population().getObjectsList();
        finalMitosis = new LinkedList<>();

        // MITOSIS DETECTION
        if (mitosis) {
            TreeSet<Mitosis> treeSet = mitosisDetector.detectMitosis();
            List<Object3D> mito = new LinkedList<>();
            for (Mitosis mitosis : treeSet) {
                Object3D d1 = mitosis.getDaughter1();
                Object3D d2 = mitosis.getDaughter2();
                Object3D mo = mitosis.getMother();
                double coloc = mitosis.getColocMitosis();
                int valPath = (int) mo.getPixMeanValue(this.path);
                if (!mito.contains(d1) && !mito.contains(d2) && coloc > mitosisDetector.getMinColocMitosis()) {
                    IJ.log("MITOSIS : " + d1.getValue() + " " + d2.getValue() + " " + mo.getValue() + " " + coloc + " " + valPath);
                    // FIXME
                    finalMitosis.add(mitosis);
                    mito.add(d1);
                    mito.add(d2);
                    if (this.path != null) {
                        d1.draw(this.pathed, valPath);
                        d2.draw(this.pathed, valPath);
                    }
                }
            }
        }

        // draw associations
    }

    private void drawAssociation() {
        if (finalAssociations == null) computeTracking();
        // create results
        this.tracked = this.img1.createSameDimensions();
        if (this.path != null) this.pathed = this.img1.createSameDimensions();
        // draw results
        int max = 0;
        for (AssociationPair pair : finalAssociations) {
            int val1 = pair.getObject3D1().getValue();
            //object3D2.setValue(val1);
            pair.getObject3D2().draw(this.tracked, val1);
            if (val1 > max) max = val1;
        }
        // orphan2
        for (Object3D object3D : finalOrphan2) {
            max++;
            object3D.draw(this.tracked, max);
        }

        // mitosis
        if ((mitosis) && (this.path != null)) {
            for (Mitosis mitosis : finalMitosis) {
                Object3D d1 = mitosis.getDaughter1();
                Object3D d2 = mitosis.getDaughter2();
                Object3D mo = mitosis.getMother();
                int valPath = (int) mo.getPixMeanValue(this.path);
                d1.draw(this.pathed, valPath);
                d2.draw(this.pathed, valPath);
            }
        }
    }

    public ResultsTable getResultsTableAssociation() {
        if (finalAssociations == null) computeTracking();
        // create results table
        ResultsTable table = new ResultsTable();
        // list associations
        int row = table.getCounter();
        for (AssociationPair pair : finalAssociations) {
            int val1 = pair.getObject3D1().getValue();
            int val2 = pair.getObject3D2().getValue();
            double cost = pair.getAsso();
            // table
            table.setValue("Label1", row, val1);
            table.setValue("Label2", row, val2);
            table.setValue("Cost", row, cost);
            row++;
        }
        // orphan1
        row = table.getCounter();
        for (Object3D object3D : finalOrphan1) {
            table.setValue("Label1", row, object3D.getValue());
            table.setValue("Label2", row, 0);
            table.setValue("CostAsso", row, 0);
            row++;
        }
        // orphan2
        row = table.getCounter();
        for (Object3D object3D : finalOrphan2) {
            table.setValue("Label1", row, 0);
            table.setValue("Label2", row, object3D.getValue());
            table.setValue("CostAsso", row, 0);
            row++;
        }

        return table;
    }

    public ResultsTable getResultsTableMitosis() {
        if (finalAssociations == null) computeTracking();
        // create results table
        ResultsTable table = new ResultsTable();
        // list associations
        int row = table.getCounter();
        for (Mitosis mitosis : finalMitosis) {
            // table
            table.setValue("Mother", row, mitosis.getMother().getValue());
            table.setValue("Daughter1", row, mitosis.getDaughter1().getValue());
            table.setValue("Daughter2", row, mitosis.getDaughter2().getValue());
            table.setValue("Colocalisation", row, mitosis.getColocMitosis());
            row++;
        }

        return table;
    }


}