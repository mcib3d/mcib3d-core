package tango.parameter;

import java.awt.*;
import java.util.*;
import java.io.*;
import javax.swing.*;
import java.text.*;
import com.mongodb.DBObject;
import com.mongodb.BasicDBObject;
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
public class ChannelFileParameter extends StructureParameter  {
    protected static String[] channelFiles;
    
    public ChannelFileParameter(String label, String id, int defaultValue) {
        super (label, id, defaultValue);
        choice.setRenderer(new DefaultListCellRenderer());
    }
    
    @Override
    protected void addItems(int idx) {
        choice.addItem("");
        if (channelFiles!=null) {
            for (String s : channelFiles) choice.addItem(s);
            if (idx>=-1 && (idx+1)<choice.getItemCount()) choice.setSelectedIndex(idx+1);
        }
    }
    
    @Override
    public void refresh() {
        int idx = getIndex();
        choice.removeAllItems();
        addItems(idx);
        setColor();
    }
    
    @Override
    public Parameter duplicate(String newLabel, String newId) {
        return new ChannelFileParameter(newLabel, newId, choice.getSelectedIndex());
    }
    
    
    public static void setChannels(String[] channelFiles) {
        ChannelFileParameter.channelFiles=channelFiles;
    }
}
