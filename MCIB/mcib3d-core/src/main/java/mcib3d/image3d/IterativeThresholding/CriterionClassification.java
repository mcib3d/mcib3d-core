package mcib3d.image3d.IterativeThresholding;

import ij.IJ;
import mcib3d.Classification.Attributes;
import mcib3d.Classification.Classifier;
import mcib3d.Classification.Data;
import mcib3d.Classification.DataSet;
import mcib3d.geom.Object3D;

public class CriterionClassification implements Criterion {
    DataSet dataSet;
    Classifier classifier;

    public CriterionClassification(String fileARFF) {
        // attributes
        Attributes attributes = new Attributes(5);
        attributes.addAttribute("volume");
        attributes.addAttribute("elong");
        attributes.addAttribute("ratioEll");
        attributes.addAttribute("mom1");
        attributes.addAttribute("mom2");
        attributes.addAttribute("mom3");
        attributes.addAttribute("mom4");
        attributes.addAttribute("mom5");
        dataSet = new DataSet("training", attributes);
        dataSet.loadDataARFF(fileARFF);
        // classifier
        classifier = new Classifier();
        classifier.trainClassifier(dataSet);
        IJ.log("classifier error= " + classifier.error());
    }

    @Override
    public double computeCriterion(Object3D object3D) {
        Data data = new Data(dataSet);
        data.setValue("volume", object3D.getVolumePixels());
        data.setValue("elong", object3D.getMainElongation());
        data.setValue("ratioEll", object3D.getRatioEllipsoid());
        data.setValue("mom1", object3D.getMoments3D()[0]);
        data.setValue("mom2", object3D.getMoments3D()[1]);
        data.setValue("mom3", object3D.getMoments3D()[2]);
        data.setValue("mom4", object3D.getMoments3D()[3]);
        data.setValue("mom5", object3D.getMoments3D()[4]);
        data.setClass(0);
        return (classifier.getClassesProbability(data)[(int) classifier.classify(data)]);
    }
}
