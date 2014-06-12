package tango.parameter;

import java.awt.*;
import java.util.*;
import java.io.*;
import javax.swing.*;
import java.text.*;
import com.mongodb.DBObject;
import com.mongodb.BasicDBObject;
import ij.ImagePlus;
import ij.WindowManager;
import ij.gui.GenericDialog;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import mcib3d.geom.Object3D;
import mcib3d.image3d.ImageHandler;
import tango.dataStructure.InputImages;
import tango.dataStructure.SegmentedCellImages;
import tango.dataStructure.StructureImages;
import tango.gui.util.Refreshable;
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
public class StructureParameter extends Parameter implements Refreshable {

    JComboBox choice;
    private static String[] structures, virtualStructures;
    private boolean allowVirtual;

    public StructureParameter(String label, String id, int defaultValue, boolean allowVirtual) {
        super(label, id);
        this.allowVirtual = allowVirtual;
        this.choice = new JComboBox();
        box.add(choice);
        addItems(defaultValue);
        setColor();
        choice.setRenderer(new StructureParameterCellRenderer());
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

    protected StructureParameter(String label, String id, int defaultValue) {
        super(label, id);
        this.choice = new JComboBox();
        box.add(choice);
        addItems(defaultValue);
        setColor();
        choice.setRenderer(new StructureParameterCellRenderer());
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

    protected void addItems(int idx) {
        choice.addItem(" ");
        if (structures != null) {
            for (String s : structures) {
                choice.addItem(s);
            }
            if (allowVirtual && virtualStructures != null) {
                for (String s : virtualStructures) {
                    choice.addItem(s);
                }
            }
            
            if (idx >= -1 && (idx + 1) < choice.getItemCount()) {
                choice.setSelectedIndex(idx + 1);
            }
        }
    }

    @Override
    public Parameter duplicate(String newLabel, String newId) {
        return new StructureParameter(newLabel, newId, choice.getSelectedIndex() - 1, allowVirtual);
    }

    public JComboBox getChoice() {
        return choice;
    }

    public static void setStructures(String[] structures, String[] virtualChannels) {
        StructureParameter.structures = structures;
        StructureParameter.virtualStructures = virtualChannels;
    }
    
    public static String[] getStructures() {
        return StructureParameter.structures;
    }

    @Override
    public void refresh() {
        int idx = getNewIndex(); //FIXME besoin de ça??
        if (idx==-1) idx=getIndex();
        //ij.IJ.log("get new index: item:"+getStructureName()+ " oldIdx"+getIndex()+ " new Idx:"+idx);
        choice.removeAllItems();
        addItems(idx);
        setColor();
    }

    protected int getNewIndex() {
        String currentChannel = this.getStructureName();
        if (currentChannel == null || currentChannel.length() == 0) {
            return -1;
        }
        int idx = Arrays.asList(structures).indexOf(currentChannel);
        if (idx >= 0) {
            return idx;
        }
        if (allowVirtual && virtualStructures != null) {
            idx = Arrays.asList(virtualStructures).indexOf(currentChannel);
            if (idx >= 0) {
                return structures.length + idx;
            }
        }
        return -1;
    }

    @Override
    public void dbPut(DBObject DBO) {
        if (choice.getSelectedIndex() >= 0) {
            DBO.put(id, choice.getSelectedIndex() - 1);
        }
    }

    @Override
    public void dbGet(BasicDBObject DBO) {
        if (DBO.containsField(id)) {
            int idx = DBO.getInt(id) + 1;
            if (choice.getItemCount() > idx) {
                choice.setSelectedIndex(idx);
            }
        }
        setColor();
    }
    
    @Override
    public void setContent(Parameter p) {
        if (p instanceof StructureParameter) {
            int idx = ((StructureParameter)p).getIndex();
            if (choice.getItemCount() > idx) {
                choice.setSelectedIndex(idx);
            }
        }
        setColor();
    }

    public int getIndex() {
        return choice.getSelectedIndex() - 1;
    }
    
    public void setValue(String value) {
        if (value==null) return;
        this.choice.setSelectedItem(value);
        setColor();
    }
    
    public void setIndex(int idx) {
        if (choice.getItemCount() > (idx+1)) {
            choice.setSelectedIndex(idx+1);
        }
        setColor();
    }

    public ImagePlus getImagePlus(StructureImages images, boolean filtered) {
        ImageHandler im = getImage(images, filtered);
        if (im != null) {
            return im.getImagePlus();
        } else {
            return null;
        }
    }

    public ImageHandler getImage(StructureImages images, boolean filtered) {
        if (getIndex() < 0) {
            return null;
        }
        if (filtered && images instanceof InputImages) {
            return ((InputImages) images).getFilteredImage(getIndex());
        } else {
            return images.getImage(getIndex());
        }
    }

    public Object3D[] getObjects(SegmentedCellImages images) {
        if (getIndex() < 0) {
            return null;
        }
        return images.getObjects(getIndex());
    }

    public String getStructureName() {
        return (String) choice.getSelectedItem();
    }

    public boolean isVirtual() {
        return getIndex() >= structures.length;
    }

    public boolean isAllowVirtual() {
        return allowVirtual;
    }

    public static Color getColor(int idx) {
        if (idx < structures.length) {
            return Color.BLACK;
        } else {
            return Color.RED;
        }
    }

    @Override
    public void addToGenericDialog(GenericDialog gd) {
        int nbima = WindowManager.getImageCount();
        String[] imas = new String[WindowManager.getImageCount()];
        for (int i = 0; i < nbima; i++) {
            imas[i] = WindowManager.getImage(i + 1).getTitle();
        }
        int idd = this.getIndex() == -1 ? 0 : this.getIndex();
        gd.addChoice(this.getLabel(), imas, imas[idd]);
    }
    
    @Override
    public boolean isValid() {
        if (!this.compulsary) return true;
        if (this.choice.getSelectedIndex()<=0) return false;
        else return true;
    }
    
    @Override
    public boolean sameContent(Parameter p) {
        return p instanceof StructureParameter && ((StructureParameter)p).getIndex()==getIndex();
    }

    private class StructureParameterCellRenderer extends DefaultListCellRenderer {

        public StructureParameterCellRenderer() {
            setOpaque(true);
        }

        @Override
        public Component getListCellRendererComponent(
                JList list,
                Object value,
                int index,
                boolean isSelected,
                boolean cellHasFocus) {
            JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            label.setText(value.toString());
            label.setBackground(isSelected ? StructureParameter.getColor(index - 1) : Color.white);
            label.setForeground(isSelected ? Color.white : StructureParameter.getColor(index - 1));
            return label;
        }
    }
}
