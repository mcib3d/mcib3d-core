/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mcib3d.Classification;

import ij.IJ;
import weka.classifiers.trees.RandomForest;

/**
 *
 * @author thomasb
 */
public class Classifier {

    RandomForest classifier;

    public Classifier() {
        classifier = new RandomForest();
        classifier.setNumTrees(200);

    }

    public void trainClassifier(DataSet data) {
        try {
            classifier.buildClassifier(data.getInstances());
        } catch (Exception ex) {
            IJ.log("Pb train classifier " + ex);
        }
    }

    public double classify(Data data) {
        double cl = -1;
        if ((data == null) || (data.getData() == null)) {
            return cl;
        }
        if (classifier == null) {
            return cl;
        }
        try {
            cl = (int) classifier.classifyInstance(data.getData());
        } catch (Exception ex) {
            // IJ.log("Pb classifying " + ex+" "+classifier);
        }
        return cl;
    }

    public double[] getClassesProbability(Data data) {
        double[] res = null;
        try {
            res = classifier.distributionForInstance(data.getData());
        } catch (Exception ex) {
            IJ.log("Pb classify " + ex);
        }
        return res;
    }

    public double error() {
        return classifier.measureOutOfBagError();
    }
}
