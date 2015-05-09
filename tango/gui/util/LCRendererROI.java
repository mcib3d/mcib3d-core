package tango.gui.util;

import ij.gui.Roi;
import java.awt.*;
import javax.swing.*;
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
public class LCRendererROI extends DefaultListCellRenderer {

    public LCRendererROI() {
        setOpaque(true);
    }
    // TODO : faire une classe par element affiché, ou alors une interface pour les objets affichables dans une liste...
    @Override
    public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
        JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
        if (value instanceof Roi) label.setText(((Roi)value).getPosition()+"");
        else label.setText("-");
        label.setBackground(isSelected ? Color.BLACK : Color.white);
        label.setForeground(isSelected ? Color.white : Color.BLACK);
        return label;
    }
}