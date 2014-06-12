
package tango.gui.util;

import java.awt.Dimension;
import java.util.ArrayList;
import javax.swing.BorderFactory;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import tango.dataStructure.Cell;
import tango.gui.CellManager;
import tango.gui.Core;
import tango.gui.PanelDisplayer;
import tango.helper.HelpManager;
import tango.helper.ID;
import tango.helper.RetrieveHelp;
import tango.util.utils;
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

public class CellManagerLayout extends javax.swing.JPanel {
    public Dimension controlPanelDimension;
    CellManager cellManager;
    boolean populateKeys;
    public CellManagerLayout(CellManager cellManager) {
        this.cellManager=cellManager;
        initComponents();
        
        // taille des JCB
        // FIXME fonctionnne pas pour les thumbnails???
        this.thumbnailsCB.setMaximumSize(new Dimension(viewPanel.getPreferredSize().width, thumbnailsCB.getPreferredSize().height));
        utils.addHorizontalScrollBar(thumbnailsCB);
        this.sortByCB.setMaximumSize(new Dimension(sortPanel.getPreferredSize().width, sortByCB.getPreferredSize().height));
        utils.addHorizontalScrollBar(sortByCB);
        
    }
    
    public void toggleIsRunning(boolean isRunning) {
        this.run.setEnabled(!isRunning);
        this.deleteCells.setEnabled(!isRunning);
        this.tagChoice.setEnabled(!isRunning);
        this.test.setEnabled(!isRunning);
        this.view3D.setEnabled(!isRunning);
        this.viewObjects.setEnabled(!isRunning);
        this.viewOverlay.setEnabled(!isRunning);
        this.viewStructures.setEnabled(!isRunning);
        this.selectAll.setEnabled(!isRunning);
        this.selectNone.setEnabled(!isRunning);
        this.ascendingOrder.setEnabled(!isRunning);
        this.sortByCB.setEnabled(!isRunning);
        
    }
    
    public JComboBox getTagChoice() {
        return this.tagChoice;
    }
    
    public void setSelectionLength(int length) {
        this.selectionLabel.setText("Selection: "+length);
    }
    
    public void registerComponents(HelpManager hm) {
        hm.objectIDs.put(this.selectAll, new ID(RetrieveHelp.cellPage, "Select_All"));
        hm.objectIDs.put(this.selectNone, new ID(RetrieveHelp.cellPage, "Select_None"));
        hm.objectIDs.put(this.deleteCells, new ID(RetrieveHelp.cellPage, "Delete_Cells"));
        hm.objectIDs.put(this.tagLabel, new ID(RetrieveHelp.cellPage, "Tags"));
        hm.objectIDs.put(this.viewOverlay, new ID(RetrieveHelp.cellPage, "Overlay"));
        hm.objectIDs.put(this.viewStructures, new ID(RetrieveHelp.cellPage, "Open_Structures"));
        hm.objectIDs.put(this.view3D, new ID(RetrieveHelp.cellPage, "View_3D"));
        hm.objectIDs.put(this.viewObjects, new ID(RetrieveHelp.cellPage, "Objects"));
        hm.objectIDs.put(this.process, new ID(RetrieveHelp.cellPage, "Process_Structures"));
        hm.objectIDs.put(this.measurement, new ID(RetrieveHelp.cellPage, "Quantifications"));
        hm.objectIDs.put(this.override, new ID(RetrieveHelp.cellPage, "Override_Quantifications"));
        hm.objectIDs.put(this.run, new ID(RetrieveHelp.cellPage, "Run"));
        hm.objectIDs.put(this.test, new ID(RetrieveHelp.cellPage, "Test"));
    }
    
    public void setCellScrollUp() {
        if (this.cellListScroll.getVerticalScrollBar()!=null) this.cellListScroll.getVerticalScrollBar().setValue(0);
    }
    
    public String getSortKey() {
        return utils.getSelectedString(sortByCB);
    }
    
    public boolean getAscendingOrder() {
        return this.ascendingOrder.isSelected();
    }
    
    public void setKeys(ArrayList<String> keys) {
        this.populateKeys=true;
        this.ascendingOrder.setEnabled(true);
        this.sortByCB.setEnabled(true);
        this.sortByCB.removeAllItems();
        Dimension dim = new Dimension(sortPanel.getPreferredSize().width, sortByCB.getPreferredSize().height);
        this.sortByCB.addItem("idx");
        this.sortByCB.addItem("tag");
        for (int i = 1; i<Core.getExperiment().getNBStructures(true); i++) sortByCB.addItem("objectNumber_"+i);
        if (keys!=null) {
            for (String key : keys) sortByCB.addItem(key);
        }
        this.sortByCB.setSelectedIndex(0);
        this.sortByCB.setMaximumSize(dim);
        this.populateKeys=false;
    }
    
    public void setStructures(String[] structures) {
        this.thumbnailsCB.removeAllItems();
        Dimension dim = new Dimension(viewPanel.getPreferredSize().width, thumbnailsCB.getPreferredSize().height);
        for (String s: structures) this.thumbnailsCB.addItem(s);
        if (structures.length>0) this.thumbnailsCB.setSelectedIndex(0);
        this.thumbnailsCB.setMaximumSize(dim);
    }
    
    public String getThumbnailStructure() {
        return utils.getSelectedString(thumbnailsCB);
    }
    
    public void setStructure(String structure) {
        if (utils.contains(thumbnailsCB, structure, true)) thumbnailsCB.setSelectedItem(structure);
    }
    
    
    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        selection = new javax.swing.JPanel();
        selectAll = new javax.swing.JButton();
        selectNone = new javax.swing.JButton();
        deleteCells = new javax.swing.JButton();
        tagLabel = new javax.swing.JLabel();
        tagChoice = new javax.swing.JComboBox();
        selectionLabel = new javax.swing.JLabel();
        viewPanel = new javax.swing.JPanel();
        viewOverlay = new javax.swing.JButton();
        viewStructures = new javax.swing.JButton();
        view3D = new javax.swing.JButton();
        viewObjects = new javax.swing.JToggleButton();
        thumbnailsCB = new javax.swing.JComboBox();
        showROIs = new javax.swing.JToggleButton();
        processPanel = new javax.swing.JPanel();
        test = new javax.swing.JButton();
        process = new javax.swing.JCheckBox();
        measurement = new javax.swing.JCheckBox();
        override = new javax.swing.JCheckBox();
        run = new javax.swing.JButton();
        cellListScroll = new javax.swing.JScrollPane();
        cellList = new javax.swing.JList();
        structureListScroll = new javax.swing.JScrollPane();
        structureList = new javax.swing.JList();
        sortPanel = new javax.swing.JPanel();
        sortByCB = new javax.swing.JComboBox();
        ascendingOrder = new javax.swing.JCheckBox();

        setBorder(javax.swing.BorderFactory.createTitledBorder("Cells"));
        setMinimumSize(new java.awt.Dimension(463, 670));
        setPreferredSize(new java.awt.Dimension(463, 670));

        selection.setBorder(javax.swing.BorderFactory.createTitledBorder("Selection"));

        selectAll.setText("Select All");
        selectAll.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                selectAllActionPerformed(evt);
            }
        });

        selectNone.setText("Select None");
        selectNone.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                selectNoneActionPerformed(evt);
            }
        });

        deleteCells.setText("Delete Cells");
        deleteCells.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                deleteCellsActionPerformed(evt);
            }
        });

        tagLabel.setText("Tag:");

        tagChoice.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                tagChoiceActionPerformed(evt);
            }
        });

        selectionLabel.setText("Selection: 0");

        javax.swing.GroupLayout selectionLayout = new javax.swing.GroupLayout(selection);
        selection.setLayout(selectionLayout);
        selectionLayout.setHorizontalGroup(
            selectionLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(selectAll, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(selectNone, javax.swing.GroupLayout.DEFAULT_SIZE, 166, Short.MAX_VALUE)
            .addComponent(deleteCells, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(selectionLayout.createSequentialGroup()
                .addComponent(tagLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(tagChoice, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            .addComponent(selectionLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        selectionLayout.setVerticalGroup(
            selectionLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(selectionLayout.createSequentialGroup()
                .addComponent(selectAll)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(selectNone)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(deleteCells)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(selectionLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(tagLabel)
                    .addComponent(tagChoice, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(selectionLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        viewPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("View"));

        viewOverlay.setText("Overlay");
        viewOverlay.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                viewOverlayActionPerformed(evt);
            }
        });

        viewStructures.setText("Open Structures");
        viewStructures.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                viewStructuresActionPerformed(evt);
            }
        });

        view3D.setText("3D");
        view3D.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                view3DActionPerformed(evt);
            }
        });

        viewObjects.setText(">Objects>");
        viewObjects.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                viewObjectsActionPerformed(evt);
            }
        });

        thumbnailsCB.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "nucleus" }));
        thumbnailsCB.setMaximumSize(new java.awt.Dimension(176, 24));
        thumbnailsCB.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                thumbnailsCBActionPerformed(evt);
            }
        });

        showROIs.setText("ROIs");
        showROIs.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                showROIsActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout viewPanelLayout = new javax.swing.GroupLayout(viewPanel);
        viewPanel.setLayout(viewPanelLayout);
        viewPanelLayout.setHorizontalGroup(
            viewPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(viewOverlay, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(viewStructures, javax.swing.GroupLayout.DEFAULT_SIZE, 162, Short.MAX_VALUE)
            .addComponent(view3D, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(viewObjects, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(thumbnailsCB, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(showROIs, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        viewPanelLayout.setVerticalGroup(
            viewPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(viewPanelLayout.createSequentialGroup()
                .addComponent(viewOverlay)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(viewStructures)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(view3D)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(showROIs)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 7, Short.MAX_VALUE)
                .addComponent(thumbnailsCB, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(viewObjects))
        );

        processPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Process"));

        test.setText("Test");
        test.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                testActionPerformed(evt);
            }
        });

        process.setText("Process Structures");

        measurement.setText("Measurements");

        override.setText("Override Meas.");

        run.setText("Run");
        run.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                runActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout processPanelLayout = new javax.swing.GroupLayout(processPanel);
        processPanel.setLayout(processPanelLayout);
        processPanelLayout.setHorizontalGroup(
            processPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(test, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(run, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(processPanelLayout.createSequentialGroup()
                .addGroup(processPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(process)
                    .addComponent(measurement)
                    .addComponent(override))
                .addGap(0, 15, Short.MAX_VALUE))
        );
        processPanelLayout.setVerticalGroup(
            processPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, processPanelLayout.createSequentialGroup()
                .addComponent(process)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(measurement)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(override)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(test)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(run))
        );

        cellList.setModel(new javax.swing.AbstractListModel() {
            String[] strings = { "" };
            public int getSize() { return strings.length; }
            public Object getElementAt(int i) { return strings[i]; }
        });
        cellListScroll.setViewportView(cellList);

        structureList.setModel(new javax.swing.AbstractListModel() {
            String[] strings = { "" };
            public int getSize() { return strings.length; }
            public Object getElementAt(int i) { return strings[i]; }
        });
        structureListScroll.setViewportView(structureList);

        sortPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Sort By Value"));

        sortByCB.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "idx" }));
        sortByCB.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                sortByCBActionPerformed(evt);
            }
        });

        ascendingOrder.setText("ascending order");
        ascendingOrder.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ascendingOrderActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout sortPanelLayout = new javax.swing.GroupLayout(sortPanel);
        sortPanel.setLayout(sortPanelLayout);
        sortPanelLayout.setHorizontalGroup(
            sortPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(sortByCB, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(ascendingOrder, javax.swing.GroupLayout.DEFAULT_SIZE, 166, Short.MAX_VALUE)
        );
        sortPanelLayout.setVerticalGroup(
            sortPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(sortPanelLayout.createSequentialGroup()
                .addComponent(sortByCB, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(ascendingOrder))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(selection, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(viewPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(processPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(sortPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(cellListScroll, javax.swing.GroupLayout.DEFAULT_SIZE, 271, Short.MAX_VALUE)
                    .addComponent(structureListScroll)))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(selection, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(viewPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(processPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(sortPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
            .addGroup(layout.createSequentialGroup()
                .addComponent(cellListScroll, javax.swing.GroupLayout.PREFERRED_SIZE, 418, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(structureListScroll)
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

    private void viewOverlayActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_viewOverlayActionPerformed
        cellManager.viewOverlay();
    }//GEN-LAST:event_viewOverlayActionPerformed

    private void viewStructuresActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_viewStructuresActionPerformed
        cellManager.openStructures();
    }//GEN-LAST:event_viewStructuresActionPerformed

    private void view3DActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_view3DActionPerformed
        cellManager.show3DCell();
    }//GEN-LAST:event_view3DActionPerformed

    private void viewObjectsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_viewObjectsActionPerformed
        cellManager.toggleShowObjects();
    }//GEN-LAST:event_viewObjectsActionPerformed

    private void testActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_testActionPerformed
        cellManager.test(this.process.isSelected(), this.measurement.isSelected(), this.override.isSelected(), -1);
    }//GEN-LAST:event_testActionPerformed

    private void runActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_runActionPerformed
        cellManager.run(this.process.isSelected(), this.measurement.isSelected(), this.override.isSelected());
    }//GEN-LAST:event_runActionPerformed

    private void ascendingOrderActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_ascendingOrderActionPerformed
        Cell.setAscendingOrger(this.ascendingOrder.isSelected());
        this.cellManager.populateCells();
    }//GEN-LAST:event_ascendingOrderActionPerformed

    private void sortByCBActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_sortByCBActionPerformed
        if (this.populateKeys) return;
        this.cellManager.populateCells();
    }//GEN-LAST:event_sortByCBActionPerformed

    private void thumbnailsCBActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_thumbnailsCBActionPerformed
        Cell.structureThumbnail=this.thumbnailsCB.getSelectedIndex();
        this.cellList.updateUI();
    }//GEN-LAST:event_thumbnailsCBActionPerformed

    private void tagChoiceActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_tagChoiceActionPerformed

        cellManager.tagAction();
    }//GEN-LAST:event_tagChoiceActionPerformed

    private void deleteCellsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_deleteCellsActionPerformed
        cellManager.deleteCells();
    }//GEN-LAST:event_deleteCellsActionPerformed

    private void selectNoneActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_selectNoneActionPerformed
        cellManager.selectNone();
    }//GEN-LAST:event_selectNoneActionPerformed

    private void selectAllActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_selectAllActionPerformed
        cellManager.selectAll();
    }//GEN-LAST:event_selectAllActionPerformed

    private void showROIsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_showROIsActionPerformed
        this.cellManager.toggleShowROIs(showROIs.isSelected());
    }//GEN-LAST:event_showROIsActionPerformed

    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JCheckBox ascendingOrder;
    public javax.swing.JList cellList;
    private javax.swing.JScrollPane cellListScroll;
    private javax.swing.JButton deleteCells;
    private javax.swing.JCheckBox measurement;
    private javax.swing.JCheckBox override;
    private javax.swing.JCheckBox process;
    private javax.swing.JPanel processPanel;
    public javax.swing.JButton run;
    private javax.swing.JButton selectAll;
    private javax.swing.JButton selectNone;
    private javax.swing.JPanel selection;
    private javax.swing.JLabel selectionLabel;
    public javax.swing.JToggleButton showROIs;
    private javax.swing.JComboBox sortByCB;
    private javax.swing.JPanel sortPanel;
    public javax.swing.JList structureList;
    private javax.swing.JScrollPane structureListScroll;
    public javax.swing.JComboBox tagChoice;
    private javax.swing.JLabel tagLabel;
    private javax.swing.JButton test;
    private javax.swing.JComboBox thumbnailsCB;
    private javax.swing.JButton view3D;
    public javax.swing.JToggleButton viewObjects;
    private javax.swing.JButton viewOverlay;
    private javax.swing.JPanel viewPanel;
    private javax.swing.JButton viewStructures;
    // End of variables declaration//GEN-END:variables


    
}
