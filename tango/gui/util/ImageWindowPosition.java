package tango.gui.util;

import java.awt.Point;
import tango.dataStructure.AbstractStructure;
import tango.dataStructure.Cell;
import tango.dataStructure.VirtualStructure;

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

public class ImageWindowPosition {
    Point[] rawPosition;
    Point[] segPosition;
    public ImageWindowPosition(int structureNb, int allStructureNb) {
        rawPosition=new Point[structureNb];
        segPosition=new Point[allStructureNb];
    }
    
    public void recordWindowPosition(Cell c) {
        for (int i = 0; i<rawPosition.length; i++) {
            Point p = c.getRawImages().getWindowPosition(i);
            if (p!=null) {
                rawPosition[i]=p;
            } 
        }
        for (int i = 0; i<segPosition.length; i++) {
            Point p = c.getSegmentedImages().getWindowPosition(i);
            if (p!=null) segPosition[i] = p;
        }
    }
    
    public void setWindowPosition(AbstractStructure ass) {
        Cell c = ass.getCell();
        int iRaw= ass.getIdxRaw();
        int i = ass.getIdx();
        if (iRaw>=0 && iRaw<rawPosition.length) c.getRawImages().setWindowPosition(iRaw, rawPosition[iRaw]);
        c.getSegmentedImages().setWindowPosition(i, segPosition[i]);
    }
}
