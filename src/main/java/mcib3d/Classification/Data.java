/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mcib3d.Classification;

import weka.core.DenseInstance;
import weka.core.Instance;

/**
 *
 * @author thomasb
 */
public class Data {

    Instance instance;
    Attributes attributes;

    public Data(DataSet set) {
        instance = new DenseInstance(set.attributes.size());
        attributes = set.attributes;
        instance.setDataset(set.instances);
    }

    public void setValue(String name, double value) {
        //IJ.log(name + " " + attributes.getAttribute(name));
        instance.setValue(attributes.getAttribute(name), value);
    }

    public Instance getData() {
        return instance;
    }

    public void setClass(int cl) {
        // IJ.log("class "+attributes.getClassAttribute());
        instance.setValue(attributes.getClassAttribute(), attributes.getClassAttribute().indexOfValue("class_" + cl));
    }

}
