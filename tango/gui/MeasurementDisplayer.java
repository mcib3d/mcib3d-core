package tango.gui;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import java.util.*;
import java.util.Map.Entry;
import javax.swing.text.Document;
import javax.swing.text.StyledDocument;
import mcib3d.utils.exceptionPrinter;
import org.bson.types.ObjectId;
import tango.dataStructure.Experiment;
import tango.dataStructure.Object3DGui;
import tango.gui.Core;
import tango.gui.util.ContextMenuMouseListener;
import tango.plugin.measurement.MeasurementKey;
import tango.plugin.measurement.MeasurementObject;
import tango.plugin.measurement.MeasurementStructure;
/**
 *
 **
 * /**
 * Copyright (C) 2012 Jean Ollion
 *
 *
 *
 * This file is part of tango
 *
 * tango is free software; you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 3 of the License, or (at your option) any later
 * version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 *
 * @author Jean Ollion
 */
public class MeasurementDisplayer extends javax.swing.JPanel {

    int[] currentStructures;
    ObjectId nucId;
    HashMap<Integer, Integer> count;
    BasicDBObject[] currentDBOs;
    HashMap<MeasurementKey, BasicDBObject> o2o;
    public MeasurementDisplayer() {
        initComponents();
        //this.jEditorPane1.setPreferredSize(null);
        text.setLineWrap(false);
        text.setEditable(false);
        ContextMenuMouseListener cml = new ContextMenuMouseListener();
        text.addMouseListener(cml);
        label.setText("");
        o2o=new HashMap<MeasurementKey, BasicDBObject>();
        count=new HashMap<Integer, Integer>();
    }
    
    public void setStructures(ObjectId nucId, int[] selectedStructures) {
        this.nucId=nucId;
        currentStructures=selectedStructures;
        count.clear();
        o2o.clear();
        eraseText();
        if (currentStructures!=null && currentStructures.length>0) {
            currentDBOs=Core.getExperiment().getConnector().getMeasurementStructure(nucId, currentStructures, null, false);
            //for (BasicDBObject dbo : currentDBOs) ij.IJ.log(dbo+"");
        } else currentDBOs=null;
        
    }
    
    
    public void setObjects(Object[] selectedObjects) {
        eraseText();
        if (currentStructures==null) {
            refreshText();
            return;
        }
        if (selectedObjects==null || selectedObjects.length==0) { //display misc & number
            if (currentDBOs!=null) for (BasicDBObject o : currentDBOs) append(o);
            label.setText("structures");
        } else if (selectedObjects.length==1) { //display o
            Object3DGui o = (Object3DGui)selectedObjects[0];
            append(Core.getExperiment().getConnector().getObject(nucId, o.getChannel().getIdx(), o.getLabel(), null));
            label.setText("object");
        } else if (selectedObjects.length==2) { //display o2o
            
            BasicDBObject disp = new BasicDBObject();
            Object3DGui o1 = (Object3DGui)selectedObjects[0];
            Object3DGui o2 = (Object3DGui)selectedObjects[1];
            int[] selStruct = new int[]{o1.getChannel().getIdx(), o2.getChannel().getIdx()};
            MeasurementKey key=new MeasurementKey(selStruct, MeasurementStructure.ArrayO2O); //currentStructures
            int size;
            int idx1=0;
            int idx2=0;
            for (int i = 0; i<currentStructures.length; i++) {
                if (currentStructures[i]==o1.getChannel().getIdx())idx1=i;
                if (currentStructures[i]==o2.getChannel().getIdx())idx2=i;
            }
            int idx, idxInv;
            if (idx1!=idx2) {
                idx=(o1.getLabel()-1)*getCount(o2.getChannel().getIdx())+o2.getLabel()-1;
                idxInv=(o2.getLabel()-1)*getCount(o1.getChannel().getIdx())+o1.getLabel()-1;
                size=getCount(o1.getChannel().getIdx())*getCount(o2.getChannel().getIdx());
            } else {
                idx = (o1.getLabel()-1)*getCount(o1.getChannel().getIdx())-((o1.getLabel()-1)*o1.getLabel())/2+o2.getLabel()-o1.getLabel()-1;
                idxInv=idx;
                size=getCount(o1.getChannel().getIdx());
                size=size*(size-1)/2;
            }
            //System.out.println("idx"+idx+ " idx1:"+o1.getLabel()+ " idx2:"+o2.getLabel()+ " count1:"+getCount(o1.getChannel().getIdx())+ " count2:"+getCount(o2.getChannel().getIdx()));
            for (Map.Entry<MeasurementKey, ArrayList<String>> entry : Core.getExperiment().getKeys().entrySet()){
                if (entry.getKey().includeO2O(key))  {
                    BasicDBObject dbo=o2o.get(entry.getKey());
                    if (dbo==null) {
                        dbo= Core.getExperiment().getConnector().getMeasurementStructure(nucId, entry.getKey().getStructures(), entry.getValue(), true)[0];
                        o2o.put(entry.getKey(), dbo);
                    }
                    for (String k : entry.getValue()) {
                        Object o = dbo.get(k);
                        if (o!=null) {
                            if (o instanceof BasicDBList) {
                                BasicDBList list = (BasicDBList)o;
                                if (list.size()==size) {
                                    if (entry.getKey().invertedOrder(o1.getChannel().getIdx(), o2.getChannel().getIdx())) disp.append(k, list.get(idxInv));
                                    else disp.append(k, list.get(idx));
                                }
                            } else if (size==1 && o instanceof Number) {
                                disp.append(k, o);
                            }
                            //else disp.append(k, "Wrong Array Size");
                        } else disp.append(k, null);
                    }
                }
            }
            append(disp);
            label.setText("object to object");
        } else label.setText("");
        refreshText();
    }
    
    protected int getCount(int structure) {
        Integer c = count.get(structure);
        if (c==null) {
            c=Core.getExperiment().getConnector().getObjectCount(nucId, structure);
            count.put(structure, c);
        }
        System.out.println("structure:"+structure+ " count:"+count);
        return c;
    }
    
    
    protected void refreshText() {
        this.text.repaint();
        this.text.revalidate();
    }
    
    
    
    protected void append(BasicDBObject o) {
        if (o==null) return;
        Document doc = text.getDocument();
        try {
            doc.insertString(doc.getLength(), toString(o), null);
        } catch(Exception e) {
            exceptionPrinter.print(e, "", Core.GUIMode);
        }
    }
    
    protected String toString(BasicDBObject dbo) {
        StringBuilder buffer = new StringBuilder();
        TreeSet<Entry<String, Object>> entries = new TreeSet<Entry<String, Object>>(new EntryComparator());
        entries.addAll(dbo.entrySet());
        Entry<String, Object> id=null;
        Entry<String, Object> id2=null;
        Entry<String, Object> structure=null;
        for (Entry<String, Object> e : entries) {
            if (e.getKey().equals("_id")) id=e;
            else if (e.getKey().equals("nucleus_id")) id2=e;
            else if (e.getKey().equals("structure") || e.getKey().equals("structures") || e.getKey().equals("channelIdx")) structure=e;
        }
        if (id!=null) appendEntry(buffer, id);
        if (id2!=null) appendEntry(buffer, id2);
        if (structure!=null) appendEntry(buffer, structure);
        for (Entry<String, Object> e : entries) {
            if (e.getKey().equals("_id") || e.getKey().equals("nucleus_id") || e.getKey().equals("structures") || e.getKey().equals("structure") || e.getKey().equals("channelIdx")) continue;
            else appendEntry(buffer, e);
        }
        buffer.append("\n");
        return buffer.toString();
    }
    
    protected void appendEntry(StringBuilder buffer, Entry<String, Object> e) {
        buffer.append(e.getKey());
        buffer.append(" : ");
        if (e.getValue()!=null) buffer.append(e.getValue().toString());
        buffer.append("\n");
    }
    
    public void eraseText() {
        this.text.setText("");
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        label = new javax.swing.JLabel();
        mesLabel = new javax.swing.JLabel();
        scrollPane = new javax.swing.JScrollPane();
        text = new javax.swing.JTextArea();

        setMaximumSize(new java.awt.Dimension(300, 600));
        setMinimumSize(new java.awt.Dimension(300, 600));
        setPreferredSize(new java.awt.Dimension(300, 600));

        label.setText("label");

        mesLabel.setText("Measurements: ");

        text.setColumns(20);
        text.setFont(new java.awt.Font("TlwgTypewriter", 0, 14)); // NOI18N
        text.setRows(5);
        scrollPane.setViewportView(text);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(mesLabel)
                .addGap(50, 50, 50)
                .addComponent(label)
                .addContainerGap(92, Short.MAX_VALUE))
            .addComponent(scrollPane)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(mesLabel)
                    .addComponent(label))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(scrollPane, javax.swing.GroupLayout.PREFERRED_SIZE, 513, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(51, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel label;
    private javax.swing.JLabel mesLabel;
    private javax.swing.JScrollPane scrollPane;
    private javax.swing.JTextArea text;
    // End of variables declaration//GEN-END:variables

    private class EntryComparator implements Comparator<Entry<String, Object>> {
        @Override
        public int compare(Entry<String, Object> t, Entry<String, Object> t1) {
            return t.getKey().compareTo(t1.getKey());
        }
        
    }

}
