/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mcib3d.Classification;

import java.util.ArrayList;
import java.util.HashMap;
import weka.core.Attribute;

/**
 *
 * @author thomasb
 */
public class Attributes {

    ArrayList<Attribute> atts;
    HashMap<String, Attribute> attributes;

    public Attributes(int nbClasses) {
        attributes = new HashMap<String, Attribute>(10);
        atts = new ArrayList<Attribute>(10);
        ArrayList<String> classes = new ArrayList<String>(nbClasses);
        for (int i = 1; i <= nbClasses; i++) {
            classes.add("class_" + i);
        }
        Attribute classAtt = new Attribute("_class_", classes);
        atts.add(classAtt);
        attributes.put(classAtt.name(), classAtt);

    }

    public void addAttribute(String name) {
        Attribute att = new Attribute(name);
        atts.add(att);
        attributes.put(name, att);
    }

    public int size() {
        return atts.size();
    }

    public ArrayList<Attribute> getArrayList() {
        return atts;
    }

    public Attribute getAttribute(String name) {
        return attributes.get(name);
    }

    public int getClassIndex() {
        return 0;
    }

    public Attribute getClassAttribute() {
        return attributes.get("_class_");
    }

}
