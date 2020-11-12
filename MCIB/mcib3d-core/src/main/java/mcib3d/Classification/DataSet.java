/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mcib3d.Classification;

import ij.IJ;
import weka.core.Instances;
import weka.core.converters.ArffSaver;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

/**
 * @author thomasb
 */
public class DataSet {

    Attributes attributes = null;
    Instances instances = null;
    int classIndex = -1;

    public DataSet(String name, Attributes atts) {
        instances = new Instances(name, atts.getArrayList(), atts.size());
        attributes = atts;
        instances.setClass(atts.getClassAttribute());
    }

    public void addData(Data data) {
        instances.add(data.getData());
    }

    public Instances getInstances() {
        return instances;
    }

    public int size() {
        return instances.size();
    }

    public void saveDatasetARFF(String fileName) {
        ArffSaver saver = new ArffSaver();
        saver.setInstances(instances);
        try {
            saver.setFile(new File(fileName));
            saver.writeBatch();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void loadDataARFF(String fileName) {
        Instances dataTmp = null;
        try {
            dataTmp = new Instances(new BufferedReader(new FileReader(fileName)));
        } catch (IOException e) {
            e.printStackTrace();
        }
        dataTmp.setClassIndex(attributes.getClassIndex());
        if (dataTmp.numAttributes() == attributes.size())
            instances = dataTmp;
        else IJ.log("Pb readind arff, number of attributes different");
    }


}
