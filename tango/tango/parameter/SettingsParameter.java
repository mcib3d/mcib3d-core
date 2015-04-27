package tango.parameter;

import java.awt.*;
import java.util.*;
import java.io.*;
import javax.swing.*;
import java.text.*;
import com.mongodb.DBObject;
import com.mongodb.BasicDBObject;
import ij.ImagePlus;
import ij.gui.GenericDialog;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import mcib3d.image3d.ImageHandler;
import mcib3d.image3d.ImageInt;
import tango.dataStructure.Experiment;
import tango.dataStructure.InputImages;
import tango.gui.Core;
import tango.gui.util.Refreshable;
import tango.mongo.MongoConnector;
import tango.plugin.filter.PostFilterSequence;
import tango.plugin.filter.PreFilterSequence;
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
public class SettingsParameter extends Parameter implements Refreshable {

    JComboBox choice;
    boolean isNucleus;
    static String[] nucleusSettings, structureSettings;

    public SettingsParameter(String label, String id, boolean isNucleus) {
        super(label, id);
        this.isNucleus = isNucleus;
        this.choice = new JComboBox();
        addItems();
        choice.addItemListener(
            new ItemListener() {
                @Override
                public void itemStateChanged(ItemEvent itemEvent) {
                    if (itemEvent.getStateChange()==ItemEvent.DESELECTED) return;
                    setColor();
                }
            }
        );
    }

    protected void addItems() {
        choice.addItem("");
        if (isNucleus && nucleusSettings != null) {
            for (String s : nucleusSettings) {
                choice.addItem(s);
            }
            box.add(choice);
        }
        if (!isNucleus && structureSettings != null) {
            for (String s : structureSettings) {
                choice.addItem(s);
            }
            box.add(choice);
        }
    }

    @Override
    public Parameter duplicate(String newLabel, String newId) {
        return new SettingsParameter(newLabel, newId, isNucleus);
    }

    public JComboBox getChoice() {
        return choice;
    }

    public static void setSettings() {
        ArrayList<String> n = Core.mongoConnector.getNucSettings();
        nucleusSettings = new String[n.size()];
        nucleusSettings = n.toArray(nucleusSettings);
        ArrayList<String> s = Core.mongoConnector.getChannelSettings();
        structureSettings = new String[s.size()];
        structureSettings = s.toArray(structureSettings);
    }

    @Override
    public void refresh() {
        String type = utils.getSelectedString(choice);
        choice.removeAllItems();
        addItems();
        if (type != null) {
            choice.setSelectedItem(type);
        }
        setColor();
    }

    @Override
    public void dbPut(DBObject DBO) {
        if (choice.getSelectedIndex() >= 0) {
            DBO.put(id, choice.getSelectedItem());
        }
    }

    @Override
    public void dbGet(BasicDBObject DBO) {
        if (DBO.containsField(id)) {
            String settings = DBO.getString(id);
            if (utils.contains(choice, settings, true)) {
                choice.setSelectedItem(settings);
            }
        }
        setColor();
    }
    
    @Override
    public void setContent(Parameter p) {
        if (p instanceof SettingsParameter) {
            SettingsParameter s = (SettingsParameter)p;
            String ss = s.getSettings();
            if (utils.contains(choice, s, true)) this.choice.setSelectedItem(ss);
        }
    }

    public String getSettings() {
        return utils.getSelectedString(choice);
    }

    public ImageHandler preFilter(ImageHandler in, InputImages images, int nbCPUs, boolean verbose) {
        int structureIdx=choice.getSelectedIndex();
        PreFilterSequence pfs = Core.getExperiment().getPreFilterSequence(structureIdx, nbCPUs, verbose);
        return pfs.run(structureIdx, in, images);
    }

    public ImageInt postFilter(ImageInt in, InputImages images, int nbCPUs, boolean verbose) {
        int structureIdx=choice.getSelectedIndex();
        PostFilterSequence pfs = Core.getExperiment().getPostFilterSequence(structureIdx, nbCPUs, verbose);
        return pfs.run(structureIdx, in, images);
    }

    @Override
    public void addToGenericDialog(GenericDialog gd) {
    }

    @Override
    public boolean isValid() {
        if (!this.compulsary) return true;
        if (this.choice.getSelectedIndex()<=0) return false;
        else return true;
    }
    
    @Override
    public boolean sameContent(Parameter p) {
        return p instanceof SettingsParameter && ((SettingsParameter)p).getSettings().equals(getSettings());
    }

}
