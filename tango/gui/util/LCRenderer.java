package tango.gui.util;

import tango.dataStructure.AbstractStructure;
import tango.dataStructure.Cell;
import tango.dataStructure.Field;
import tango.dataStructure.Object3DGui;
import ij.IJ;
import java.awt.*;
import javax.swing.*;
import java.util.*;
import tango.dataStructure.*;
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
public class LCRenderer extends DefaultListCellRenderer {

    public LCRenderer() {
        setOpaque(true);
    }
    // TODO : faire une classe par element affiché, ou alors une interface pour les objets affichables dans une liste...
    @Override
    public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
        JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
        if (value instanceof Field) {
            Field f = (Field) value;
            Icon icon = f.getThumbnail();
            if (icon != null) {
                label.setIcon(icon);
            }
            label.setText(f.getName());
            label.setBackground(isSelected ? Color.BLACK : Color.white);
            label.setForeground(isSelected ? Color.white : Color.BLACK);
        } else if (value instanceof Cell) {
            Cell c = (Cell) value;
            Icon icon = c.getThumbnail();
            if (icon != null) {
                label.setIcon(icon);
            }
            int tag = c.getTag().getTag();
            if (tag > Tag.getNbTag()) {
                tag = 0;
            }
            Color col = Tag.colors.get(tag);
            label.setText(c.getField().getName() + "::" + c.getName());
            Color bck = c.isInSelection() ? Color.lightGray : Color.white;
            label.setBackground(isSelected ? col : bck);
            label.setForeground(isSelected ? Tag.oppositeColors.get(tag) : col);
        } else if (value instanceof AbstractStructure) {
            AbstractStructure ass = (AbstractStructure) value;
            Icon icon = ass.getThumbnail();
            if (icon != null) {
                label.setIcon(icon);
            }
            label.setText(ass.getChannelName());
            Color col = ass.getColor();
            Color opposite = new Color(255 - col.getRed(), 255 - col.getGreen(), 255 - col.getBlue());
            label.setBackground(isSelected ? col : Color.white);
            label.setForeground(isSelected ? opposite : col);
        } else if (value instanceof Object3DGui) {
            Object3DGui o = (Object3DGui) value;
            label.setText(o.getName());
            Color col = o.getColor();
            //Color opposite = new Color(255 - col.getRed(), 255 - col.getGreen(), 255 - col.getBlue());
            Color bck = o.isInSelection() ? Color.lightGray : Color.white;
            Color selectedFore = (col.getBlue() == 0 && col.getRed()== 0 && col.getGreen()==0) ? Color.white : col ;
            label.setBackground(isSelected ? Color.black : bck);
            //label.setForeground(isSelected ? opposite : col);
            label.setForeground(isSelected ? selectedFore : col);
        } else if (value instanceof Selection) {
            Selection o = (Selection) value;
            label.setText(o.getName());
            label.setBackground(isSelected ? Color.black : Color.white);
            label.setForeground(isSelected ? Color.white : Color.black);
        }
        return label;
    }
}