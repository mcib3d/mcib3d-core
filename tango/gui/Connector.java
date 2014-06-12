package tango.gui;

import com.mongodb.BasicDBObject;
import ij.IJ;
import ij.Prefs;
import java.io.File;
import java.util.ArrayList;
import javax.swing.JOptionPane;
import mcib3d.utils.exceptionPrinter;
import tango.helper.HelpManager;
import tango.helper.Helper;
import tango.helper.ID;
import tango.helper.RetrieveHelp;
import tango.mongo.MongoConnector;
import tango.parameter.*;
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
public class Connector extends javax.swing.JPanel {

    Core core;
    boolean connecting;
    String currentUser;
    public DoubleParameter magnitude = new DoubleParameter("Zoom Magnitude:", "zoom", 2d, Parameter.nfDEC1);
    public static SliderParameter maxThreads = new SliderParameter("Max Threads:", "maxThreads", 1, Runtime.getRuntime().availableProcessors(), Runtime.getRuntime().availableProcessors());
    public static SliderParameter maxCellsProcess = new SliderParameter("Max Cells (process):", "maxCellsProcess", 1, Runtime.getRuntime().availableProcessors(), Runtime.getRuntime().availableProcessors());
    public static SliderParameter maxCellsMeasure = new SliderParameter("Max Cells (measure):", "maxCellsMeasure", 1, Runtime.getRuntime().availableProcessors(), Runtime.getRuntime().availableProcessors());
    public static BooleanParameter recordWindowsPosition = new BooleanParameter("Record Cell Image Windows Position:", "windowPos", true);

    //public BooleanParameter multithreadProcess = new BooleanParameter("Multithread Cell Process", "multithreadProcess", true);
    Parameter[] parameters = new Parameter[]{magnitude, maxThreads, maxCellsProcess, maxCellsMeasure, recordWindowsPosition};
    public GroupParameter options = new GroupParameter("Options", "options", parameters);

    public Connector(Core core) {
        this.core = core;
        initComponents();
        options.setHelp("General options. Saved For the current user", true);
        magnitude.setHelp("Zoom magnitude factor when opening cell images (buttons overlay/ open structures)", true);
        maxThreads.setHelp("Limits the maximium number of threads used dureing process & measurements", true);
        maxCellsProcess.setHelp("During cell process: one cell is processed by one thread. If this causes out of memory errors, lower this value", true);
        maxCellsMeasure.setHelp("During cell quantifications: one cell is processed by one thread. If this causes out of memory errors, lower this value", true);
        recordWindowsPosition.setHelp("Record cell image windows position when browsing cells in the data tab.", true);
        toggleEnableButtons(false, false);
    }
    
    private String trim(String s, int size) {
        System.out.println("trim:"+s+ " size:"+size);
        if (s.length()>size) return "..."+s.substring(size-3);
        else return s;
    }
    
    public void registerComponents(HelpManager hm) {
        hm.objectIDs.put(host, new ID(RetrieveHelp.connectPage, "Host"));
        hm.objectIDs.put(hostLabel, new ID(RetrieveHelp.connectPage, "Host"));
        hm.objectIDs.put(connect, new ID(RetrieveHelp.connectPage, "Connect_2"));
        hm.objectIDs.put(helpButton, new ID(RetrieveHelp.connectPage, "Help"));
        hm.objectIDs.put(userLabel, new ID(RetrieveHelp.connectPage, "User"));
        hm.objectIDs.put(usernames, new ID(RetrieveHelp.connectPage, "User"));
        hm.objectIDs.put(newUser, new ID(RetrieveHelp.connectPage, "New_User"));
        hm.objectIDs.put(deleteUsr, new ID(RetrieveHelp.connectPage, "Delete_User"));
        hm.objectIDs.put(importData, new ID(RetrieveHelp.connectPage, "Import_Data"));
        hm.objectIDs.put(exportData, new ID(RetrieveHelp.connectPage, "Export_Data"));
        hm.objectIDs.put(exportInput, new ID(RetrieveHelp.connectPage, "Export_Input_Images"));
        hm.objectIDs.put(exportOutput, new ID(RetrieveHelp.connectPage, "Export_Ouput_Images"));
        hm.objectIDs.put(importSettings, new ID(RetrieveHelp.connectPage, "Import_Processing_Chains_Templates"));
        hm.objectIDs.put(exportSettings, new ID(RetrieveHelp.connectPage, "Export_Processing_Chains_Templates"));
    }

    private void getUsers() {
        this.usernames.removeAllItems();
        // login user
        for (String key : Core.mongoConnector.getUsers()) {
            usernames.addItem(key);
        }
    }

    public void toggleIsRunning(boolean isRunning) {
        this.connect.setEnabled(!isRunning);
        toggleEnableButtons(!isRunning, !isRunning);
    }

    private void toggleEnableButtons(boolean connected, boolean userSet) {
        if (!connected) {
            this.newUser.setEnabled(false);
            this.usernames.setEnabled(false);
            this.helpButton.setEnabled(false);
        }
        if (connected) {
            this.newUser.setEnabled(true);
            this.helpButton.setEnabled(true);
            this.usernames.setEnabled(true);
        }
        if (!connected || (connected && !userSet)) {
            this.deleteUsr.setEnabled(false);
            exportData.setEnabled(false);
            importData.setEnabled(false);
            exportSettings.setEnabled(false);
            importSettings.setEnabled(false);
        }
        if (connected && userSet) {
            this.deleteUsr.setEnabled(true);
            exportData.setEnabled(true);
            importData.setEnabled(true);
            exportSettings.setEnabled(true);
            importSettings.setEnabled(true);
        }
    }

    private void connect() {
        connecting = true;
        try {
            Core.mongoConnector = new MongoConnector(host.getText());
            if (!Core.mongoConnector.isConnected()) {
                toggleEnableButtons(false, false);
                return;
            }
            toggleEnableButtons(true, false);
            Prefs.set(MongoConnector.getPrefix() + "_host.String", host.getText());
            getUsers();
            if (usernames.getItemCount() > 0) {
                String user = Prefs.get(MongoConnector.getPrefix() + "_username.String", "");
                if (user.length() == 0 || !utils.contains(usernames, user, true)) {
                    user = (String) usernames.getItemAt(0);
                }
                setUser(user);
            }
        } catch (Exception e) {
            exceptionPrinter.print(e, "", Core.GUIMode);
        }
        connecting = false;
    }

    public String getHost() {
        String h = this.host.getText();
        if (h == null || h.equals("")) {
            h = "localhost";
        }
        return h;
    }

    private void setUser(String usr) {
        if (utils.contains(usernames, usr, true)) {
            usernames.setSelectedItem(usr);
        }
        BasicDBObject user = Core.mongoConnector.setUser(usr, false);
        if (user != null) {
            currentUser = usr;
            Object userHost = user.get("options_" + this.getHost());
            if (userHost == null) {
                userHost = new BasicDBObject();
                user.append("options_" + this.getHost(), userHost);
            }
            options.dbGet((BasicDBObject) userHost);
            Prefs.set(MongoConnector.getPrefix() + "_username.String", usr);
            core.connect();
            toggleEnableButtons(true, true);
        } else {
            currentUser = null;
            core.disableTabs();
            toggleEnableButtons(true, false);
        }
    }

    public void saveOptions() {
        if (currentUser != null) {
            BasicDBObject user = Core.mongoConnector.getUser();
            BasicDBObject userHost = new BasicDBObject();
            options.dbPut(userHost);
            user.append("options_" + this.getHost(), userHost);
            Core.mongoConnector.saveUser(user);
        }
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        connectionPanel = new javax.swing.JPanel();
        hostLabel = new javax.swing.JLabel();
        host = new javax.swing.JTextField();
        connect = new javax.swing.JButton();
        usernames = new javax.swing.JComboBox();
        userLabel = new javax.swing.JLabel();
        newUser = new javax.swing.JButton();
        helpButton = new javax.swing.JButton();
        deleteUsr = new javax.swing.JButton();
        ExportImportPanel = new javax.swing.JPanel();
        exportInput = new javax.swing.JCheckBox();
        exportOutput = new javax.swing.JCheckBox();
        exportData = new javax.swing.JButton();
        importData = new javax.swing.JButton();
        jPanel1 = new javax.swing.JPanel();
        exportSettings = new javax.swing.JButton();
        importSettings = new javax.swing.JButton();

        setMaximumSize(new java.awt.Dimension(1024, 600));
        setMinimumSize(new java.awt.Dimension(1024, 600));
        setPreferredSize(new java.awt.Dimension(1024, 600));

        connectionPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Database Connection"));

        hostLabel.setText("Host:");

        host.setText((String) Prefs.get(tango.mongo.MongoConnector.getPrefix() + "_host.String", tango.mongo.MongoConnector.defaultHost_DB));
        host.setMaximumSize(new java.awt.Dimension(152, 25));
        host.setMinimumSize(new java.awt.Dimension(152, 25));
        host.setPreferredSize(new java.awt.Dimension(152, 25));

        connect.setText("Connect");
        connect.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                connectActionPerformed(evt);
            }
        });

        usernames.setMaximumSize(new java.awt.Dimension(152, 25));
        usernames.setMinimumSize(new java.awt.Dimension(152, 25));
        usernames.setPreferredSize(new java.awt.Dimension(152, 25));
        usernames.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                usernamesItemStateChanged(evt);
            }
        });

        userLabel.setText("User:");

        newUser.setText("Add user");
        newUser.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                newUserActionPerformed(evt);
            }
        });

        helpButton.setText("Help!");
        helpButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                helpButtonActionPerformed(evt);
            }
        });

        deleteUsr.setText("Delete User");
        deleteUsr.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                deleteUsrActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout connectionPanelLayout = new javax.swing.GroupLayout(connectionPanel);
        connectionPanel.setLayout(connectionPanelLayout);
        connectionPanelLayout.setHorizontalGroup(
            connectionPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(connectionPanelLayout.createSequentialGroup()
                .addGap(20, 20, 20)
                .addGroup(connectionPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(connect, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(newUser, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(deleteUsr, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(helpButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(connectionPanelLayout.createSequentialGroup()
                        .addGroup(connectionPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addGroup(connectionPanelLayout.createSequentialGroup()
                                .addComponent(hostLabel)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(host, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                            .addGroup(connectionPanelLayout.createSequentialGroup()
                                .addComponent(userLabel)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(usernames, 0, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addGap(0, 33, Short.MAX_VALUE)))
                .addContainerGap())
        );
        connectionPanelLayout.setVerticalGroup(
            connectionPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(connectionPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(connectionPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(hostLabel)
                    .addComponent(host, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(connect)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(connectionPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(usernames, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(userLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(newUser)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(deleteUsr)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(helpButton)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        ExportImportPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Import/Export Data"));

        exportInput.setSelected(true);
        exportInput.setText("Export Input Images");

        exportOutput.setSelected(true);
        exportOutput.setText("Export Ouput Images");

        exportData.setText("Export Data");
        exportData.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                exportDataActionPerformed(evt);
            }
        });

        importData.setText("Import Data");
        importData.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                importDataActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout ExportImportPanelLayout = new javax.swing.GroupLayout(ExportImportPanel);
        ExportImportPanel.setLayout(ExportImportPanelLayout);
        ExportImportPanelLayout.setHorizontalGroup(
            ExportImportPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(ExportImportPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(ExportImportPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(exportData, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(ExportImportPanelLayout.createSequentialGroup()
                        .addGroup(ExportImportPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(exportOutput)
                            .addComponent(exportInput))
                        .addGap(0, 78, Short.MAX_VALUE))
                    .addComponent(importData, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        ExportImportPanelLayout.setVerticalGroup(
            ExportImportPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(ExportImportPanelLayout.createSequentialGroup()
                .addComponent(exportInput)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(exportOutput)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(exportData)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(importData)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder("Import/Export Processing Chains"));

        exportSettings.setText("Export Processing Chains");
        exportSettings.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                exportSettingsActionPerformed(evt);
            }
        });

        importSettings.setText("Import Processing Chains");
        importSettings.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                importSettingsActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(exportSettings, javax.swing.GroupLayout.DEFAULT_SIZE, 251, Short.MAX_VALUE)
                    .addComponent(importSettings, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addComponent(exportSettings)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(importSettings)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(connectionPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(ExportImportPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(438, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(connectionPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(ExportImportPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(356, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void connectActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_connectActionPerformed
        connect();
    }//GEN-LAST:event_connectActionPerformed

    private void newUserActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_newUserActionPerformed
        String name = JOptionPane.showInputDialog("Username (no special char):");
        if (name==null) return;
        if (!utils.isValid(name, false)) {
            IJ.error("Name should not contain any special character");
            return;
        }
        if (utils.contains(usernames, name, false)) {
            IJ.error("Name should not contain any special character");
            return;
        }
        if (name.length()>15) {
            IJ.error("Name should be shorter than 15 characters");
            return;
        }
        Core.mongoConnector.setUser(name, true);
        getUsers();
        setUser(name);
    }//GEN-LAST:event_newUserActionPerformed

    private void exportDataActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_exportDataActionPerformed
        if (Core.mongoConnector == null || !Core.mongoConnector.isConnected()) {
            IJ.error("connect first");
            return;
        }
        ArrayList<String> folders = Core.mongoConnector.getProjects();
        Object[] choice = new Object[folders.size() + 1];
        choice[0] = "";
        for (int i = 0; i < folders.size(); i++) {
            choice[i + 1] = folders.get(i);
        }
        Object selectedFolder = JOptionPane.showInputDialog(
                this,
                "Choose Project to export :",
                "Tango",
                JOptionPane.QUESTION_MESSAGE,
                null,
                choice,
                "");
        String folder = (String) selectedFolder;
        if (folder != null && folder.length() > 0) {
            File dir = utils.chooseDir("Select Export directory", null);
            if (dir != null) {
                Core.mongoConnector.mongoDumpProject(folder, dir.getAbsolutePath(), this.exportInput.isSelected(), exportOutput.isSelected());
            }
        }
    }//GEN-LAST:event_exportDataActionPerformed

    private void importDataActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_importDataActionPerformed
        if (Core.mongoConnector == null || !Core.mongoConnector.isConnected()) {
            IJ.error("connect first");
            return;
        }
        File dir = utils.chooseDir("Select Import directory containing .bson files", null);
        if (dir != null) {
            String name = JOptionPane.showInputDialog("Project name (no special char):");
            if (name==null) return;
            if (!utils.isValid(name, false)) IJ.error("Invalid name");
            if (name.length()>30) IJ.error("Name length must be <30");
            if (!Core.mongoConnector.getProjects().contains(name)) {
                try {
                    Core.mongoConnector.mongoRestoreProject(dir.getAbsolutePath(), name);
                    connect();
                } catch (Exception e) {
                    exceptionPrinter.print(e, "", Core.GUIMode);
                }

            } else {
                IJ.error("Project already exists...");
            }
        }
    }//GEN-LAST:event_importDataActionPerformed

    private void exportSettingsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_exportSettingsActionPerformed
        if (Core.mongoConnector == null || !Core.mongoConnector.isConnected()) {
            IJ.error("connect first");
            return;
        }
        File dir = utils.chooseDir("Select Export directory", null);
        if (dir != null) {
            Core.mongoConnector.mongoDumpSettings(dir.getAbsolutePath());
        }
    }//GEN-LAST:event_exportSettingsActionPerformed

    private void importSettingsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_importSettingsActionPerformed
        if (Core.mongoConnector == null || !Core.mongoConnector.isConnected()) {
            IJ.error("connect first");
            return;
        }
        File dir = utils.chooseDir("Select Import directory containing .bson files", null);
        if (dir != null) {
            try {
                Core.mongoConnector.mongoRestoreSettings(dir.getAbsolutePath());
                connect();
            } catch (Exception e) {
                exceptionPrinter.print(e, "", Core.GUIMode);
            }
        }
    }//GEN-LAST:event_importSettingsActionPerformed

    private void usernamesItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_usernamesItemStateChanged
        if (connecting) {
            return;
        }
        if (evt.getStateChange() == 1) {
            String name = (String) usernames.getSelectedItem();
            if (name != null && name.length() > 0) {
                setUser(name);
            }
        }
    }//GEN-LAST:event_usernamesItemStateChanged
    
    public void setMongoBinDir(File binDir) {
        Prefs.set(MongoConnector.getPrefix() + "_mongoBinPath.String", binDir.getAbsolutePath());
    }
    
    private void helpButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_helpButtonActionPerformed
        if (Core.helper != null) {
            Core.helper.close();
        }
        Core.helper = new Helper(core);
    }//GEN-LAST:event_helpButtonActionPerformed

    private void deleteUsrActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_deleteUsrActionPerformed
        if (this.currentUser == null) {
            ij.IJ.error("select user first");
            return;
        }
        if (JOptionPane.showConfirmDialog(this, "Remove User:" + this.currentUser + " and Projects associated (all data will be lost)?", "TANGO", JOptionPane.OK_CANCEL_OPTION) == 0) {
            Core.mongoConnector.removeUser();
            this.usernames.removeItem(this.currentUser);
            this.setUser(null);
        }
    }//GEN-LAST:event_deleteUsrActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel ExportImportPanel;
    private javax.swing.JButton connect;
    private javax.swing.JPanel connectionPanel;
    private javax.swing.JButton deleteUsr;
    private javax.swing.JButton exportData;
    private javax.swing.JCheckBox exportInput;
    private javax.swing.JCheckBox exportOutput;
    private javax.swing.JButton exportSettings;
    private javax.swing.JButton helpButton;
    private javax.swing.JTextField host;
    private javax.swing.JLabel hostLabel;
    private javax.swing.JButton importData;
    private javax.swing.JButton importSettings;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JButton newUser;
    private javax.swing.JLabel userLabel;
    private javax.swing.JComboBox usernames;
    // End of variables declaration//GEN-END:variables
}
