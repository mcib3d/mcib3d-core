package tango.gui;

import ij.ImagePlus;
import ij.WindowManager;
import ij.gui.Roi;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.util.ArrayList;
import java.util.HashMap;
import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import tango.gui.util.LCRenderer;
import tango.gui.util.LCRendererROI;
import tango.helper.HelpManager;
import tango.helper.ID;
import tango.helper.RetrieveHelp;
import tango.util.ImageUtils;
import tango.util.RoiInterpolator;

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
public class RoiManager3D extends javax.swing.JPanel implements ListSelectionListener, AdjustmentListener, MouseWheelListener {

    DefaultListModel listModel;
    ListSelectionModel listSelectionModel;
    int nbSlices;
    boolean populatingObjects;
    ImagePlus currentImage;
    NucleusManager nucleusManager;
    public RoiManager3D(NucleusManager nucleusManager) {
        initComponents();
        this.nucleusManager=nucleusManager;
        this.listModel = new DefaultListModel();
        this.list.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        list.setModel(listModel);
        listSelectionModel = list.getSelectionModel();
        listSelectionModel.addListSelectionListener(this);
        list.setPreferredSize(null);
    }
    
    public void toggleIsRunning(boolean isRunning) {
        this.add.setEnabled(!isRunning);
        this.drawROIs.setEnabled(!isRunning);
        this.getROIs.setEnabled(!isRunning);
        this.interpolate.setEnabled(!isRunning);
        this.list.setEnabled(!isRunning);
        this.newObject.setEnabled(!isRunning);
        this.remove.setEnabled(!isRunning);
        this.show.setEnabled(!isRunning);
    }
    
    public void populateRois(int nbSlices) {
        System.out.println("populate Rois:"+nbSlices);
        this.nbSlices=nbSlices;
        this.populatingObjects = true;
        listModel.removeAllElements();
        for (int i = 0; i<nbSlices; i++) listModel.addElement(null);
        this.populatingObjects = false;
        this.list.repaint();
        this.list.revalidate();
        
    }
    
    public void registerComponents(HelpManager hm) {
        hm.objectIDs.put(this.add, new ID(RetrieveHelp.manualNucPage, "Add_or_Update_ROI"));
        hm.objectIDs.put(this.remove, new ID(RetrieveHelp.manualNucPage, "Remove"));
        hm.objectIDs.put(this.interpolate, new ID(RetrieveHelp.manualNucPage, "Interpolate"));
        hm.objectIDs.put(this.show, new ID(RetrieveHelp.manualNucPage, "Show"));
        hm.objectIDs.put(this.newObject, new ID(RetrieveHelp.manualNucPage, "New_Object"));
        hm.objectIDs.put(this.drawROIs, new ID(RetrieveHelp.manualNucPage, "ROIs_to_Mask"));
        hm.objectIDs.put(this.getROIs, new ID(RetrieveHelp.manualNucPage, "Mask_to_ROIs"));
    }
    
    protected void registerActiveImage() {
        ImagePlus activeImage = WindowManager.getCurrentImage();
        if (activeImage != null && activeImage.getProcessor() != null && activeImage.getImageStackSize() > 1) {
            if (currentImage != null && currentImage.getWindow() != null && currentImage != activeImage) {
                //System.out.println("remove listener:"+currentImage.getTitle());
                ImageUtils.removeScrollListener(currentImage, this, this);
                currentImage.killRoi();
                currentImage.updateAndDraw();
                currentImage = null;
            }
            if (currentImage != activeImage) {
                //System.out.println("add listener:"+activeImage.getTitle());
                ImageUtils.addScrollListener(activeImage, this, this);
                this.currentImage = activeImage;
            }
        }
    }
    
    protected void hideRois() {
        if (currentImage==null) return;
        currentImage.killRoi();
        if (currentImage.isVisible()) {
            currentImage.updateAndDraw();
            ImageUtils.removeScrollListener(currentImage, this, this);
        }
        currentImage = null;
    }
    
    protected void updateRoi() {
        if (currentImage==null || !currentImage.isVisible() || currentImage.getNSlices()!=nbSlices || populatingObjects) return;
        Object o = listModel.get(currentImage.getSlice()-1);
        if (o!=null) {
            currentImage.setRoi((Roi)o);
        } else {
            currentImage.killRoi();
        }
        currentImage.updateAndDraw();
    }
    
    public void addROIs(Roi[] rois, boolean ignoreNULL) {
        this.populatingObjects=true;
        for (int i = 0; i<rois.length; i++) {
            Roi r = rois[i];
            if (ignoreNULL && r==null) continue;
            if (ignoreNULL) {
                if (r==null) continue;
                int idx = r.getPosition()-1;
                if (idx>=0) {
                    listModel.remove(idx);
                    listModel.add(idx, r);
                }
            } else {
                int idx = r==null?i:r.getPosition()-1;
                if (r!=null || rois.length==nbSlices) {
                    listModel.remove(idx);
                    listModel.add(idx, r);
                }
            }
            
        }
        this.populatingObjects=false;
    }
    
    public Roi[] getROIs() {
        ArrayList<Roi> rois=new ArrayList<Roi>(this.nbSlices);
        for (int i = 0; i<nbSlices; i++) if (listModel.get(i)!=null) rois.add((Roi)listModel.get(i));
        Roi[] roisArr = new Roi[rois.size()];
        return rois.toArray(roisArr);
    }
    
    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        add = new javax.swing.JButton();
        remove = new javax.swing.JButton();
        interpolate = new javax.swing.JButton();
        show = new javax.swing.JToggleButton();
        listScrollPane = new javax.swing.JScrollPane();
        list = new javax.swing.JList();
        newObject = new javax.swing.JButton();
        getROIs = new javax.swing.JButton();
        drawROIs = new javax.swing.JButton();

        setMaximumSize(new java.awt.Dimension(150, 588));
        setMinimumSize(new java.awt.Dimension(150, 588));
        setPreferredSize(new java.awt.Dimension(150, 588));

        add.setText("Add/Update ROI");
        add.setMaximumSize(new java.awt.Dimension(150, 25));
        add.setMinimumSize(new java.awt.Dimension(150, 25));
        add.setPreferredSize(new java.awt.Dimension(150, 25));
        add.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addActionPerformed(evt);
            }
        });

        remove.setText("Remove");
        remove.setMaximumSize(new java.awt.Dimension(150, 25));
        remove.setMinimumSize(new java.awt.Dimension(150, 25));
        remove.setPreferredSize(new java.awt.Dimension(150, 25));
        remove.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                removeActionPerformed(evt);
            }
        });

        interpolate.setText("Interpolate");
        interpolate.setMaximumSize(new java.awt.Dimension(150, 25));
        interpolate.setMinimumSize(new java.awt.Dimension(150, 25));
        interpolate.setPreferredSize(new java.awt.Dimension(150, 25));
        interpolate.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                interpolateActionPerformed(evt);
            }
        });

        show.setText("Show");
        show.setMaximumSize(new java.awt.Dimension(150, 25));
        show.setMinimumSize(new java.awt.Dimension(150, 25));
        show.setPreferredSize(new java.awt.Dimension(150, 25));
        show.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                showActionPerformed(evt);
            }
        });

        listScrollPane.setMaximumSize(new java.awt.Dimension(150, 32767));
        listScrollPane.setMinimumSize(new java.awt.Dimension(150, 250));
        listScrollPane.setPreferredSize(new java.awt.Dimension(150, 250));

        list.setCellRenderer(new LCRendererROI());
        list.setMaximumSize(new java.awt.Dimension(150, 32767));
        list.setMinimumSize(new java.awt.Dimension(150, 250));
        list.setPreferredSize(new java.awt.Dimension(150, 600));
        listScrollPane.setViewportView(list);

        newObject.setText("New Object");
        newObject.setMaximumSize(new java.awt.Dimension(150, 25));
        newObject.setMinimumSize(new java.awt.Dimension(150, 25));
        newObject.setPreferredSize(new java.awt.Dimension(150, 25));
        newObject.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                newObjectActionPerformed(evt);
            }
        });

        getROIs.setText("Mask -> ROIs");
        getROIs.setMaximumSize(new java.awt.Dimension(150, 25));
        getROIs.setMinimumSize(new java.awt.Dimension(150, 25));
        getROIs.setPreferredSize(new java.awt.Dimension(150, 25));
        getROIs.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                getROIsActionPerformed(evt);
            }
        });

        drawROIs.setText("ROIs -> Mask");
        drawROIs.setMaximumSize(new java.awt.Dimension(150, 25));
        drawROIs.setMinimumSize(new java.awt.Dimension(150, 25));
        drawROIs.setPreferredSize(new java.awt.Dimension(150, 25));
        drawROIs.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                drawROIsActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(listScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(add, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(remove, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(interpolate, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(show, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(newObject, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(getROIs, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(drawROIs, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addComponent(add, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(remove, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(interpolate, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(show, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(newObject, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(getROIs, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(drawROIs, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(listScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 359, Short.MAX_VALUE)
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

    private void showActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_showActionPerformed

        if (show.isSelected()) {
            if (nucleusManager.showObjects.isSelected()) {
                nucleusManager.showObjects.setSelected(false);
                nucleusManager.hideRois();
            }
            registerActiveImage();
            updateRoi();
        } else {
            hideRois();
        }
    }//GEN-LAST:event_showActionPerformed

    private void addActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addActionPerformed
        ImagePlus activeImage = WindowManager.getCurrentImage();
        if (activeImage==null) return;
        Roi current = activeImage.getRoi();
        if (current==null) return;
        current = (Roi)current.clone();
        int slice = activeImage.getSlice();
        current.setPosition(slice);
        this.populatingObjects=true;
        listModel.remove(slice-1);
        listModel.add(slice-1, current);
        list.setSelectedIndex(slice-1);
        this.populatingObjects=false;
    }//GEN-LAST:event_addActionPerformed

    private void removeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_removeActionPerformed
        int[] sel = list.getSelectedIndices();
        this.populatingObjects=true;
        for (int i : sel) {
            listModel.remove(i);
            listModel.add(i, null);
        }
        this.populatingObjects=false;
    }//GEN-LAST:event_removeActionPerformed

    private void interpolateActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_interpolateActionPerformed
        Roi[] newRois=RoiInterpolator.run(getROIs());
        this.addROIs(newRois, true);
    }//GEN-LAST:event_interpolateActionPerformed

    private void newObjectActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_newObjectActionPerformed
        if (nucleusManager.maskChange && JOptionPane.showConfirmDialog(this, "Save current Changes on Mask?", "TANGO", JOptionPane.OK_CANCEL_OPTION) == 0) nucleusManager.saveMask();
        nucleusManager.newObject();
    }//GEN-LAST:event_newObjectActionPerformed

    private void getROIsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_getROIsActionPerformed

        this.addROIs(nucleusManager.getCurrentLabelRois(), false);
    }//GEN-LAST:event_getROIsActionPerformed

    private void drawROIsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_drawROIsActionPerformed
        nucleusManager.addRoisToMask();
    }//GEN-LAST:event_drawROIsActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton add;
    private javax.swing.JButton drawROIs;
    private javax.swing.JButton getROIs;
    private javax.swing.JButton interpolate;
    private javax.swing.JList list;
    private javax.swing.JScrollPane listScrollPane;
    private javax.swing.JButton newObject;
    private javax.swing.JButton remove;
    protected javax.swing.JToggleButton show;
    // End of variables declaration//GEN-END:variables

    @Override
    public void valueChanged(ListSelectionEvent lse) {
        if (populatingObjects || list.getSelectedIndex()==-1) return;
        ImagePlus activeImage = WindowManager.getCurrentImage();
        if (activeImage!=null) {
            activeImage.setSlice(list.getSelectedIndex()+1);
            if (list.getSelectedValue()!=null) activeImage.setRoi((Roi)list.getSelectedValue(), true);
            else {
                activeImage.killRoi();
                activeImage.updateAndDraw();
            }
        }
    }

    @Override
    public void adjustmentValueChanged(AdjustmentEvent ae) {
        updateRoi();
    }

    @Override
    public void mouseWheelMoved(MouseWheelEvent mwe) {
        updateRoi();
    }
}
