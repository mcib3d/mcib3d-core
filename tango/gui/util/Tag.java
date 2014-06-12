package tango.gui.util;

import java.awt.Color;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
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
public class Tag {
    // TODO keys should be string ?? and add possibilty to retrieve tag names or values

    public static Map<Integer, Color> colors = Collections.unmodifiableMap(new HashMap<Integer, Color>() {

        {
            put(-1, Color.RED);
            put(0, Color.BLACK);
            put(1, new Color(0, 26, 229));
            put(2, new Color(0, 51, 204));
            put(3, new Color(0, 77, 178));
            put(4, new Color(0, 102, 153));
            put(5, new Color(0, 128, 127));
            put(6, new Color(0, 153, 102));
            put(7, new Color(0, 178, 76));
            put(8, new Color(0, 204, 51));
            put(9, new Color(0, 220, 26));
            put(10, Color.GREEN);
        }
    });
    public static Map<Integer, Color> oppositeColors = Collections.unmodifiableMap(new HashMap<Integer, Color>() {

        {
            put(-1, Color.BLACK);
            put(0, Color.WHITE);
            put(1, Color.BLACK);
            put(2, Color.BLACK);
            put(3, Color.BLACK);
            put(4, Color.BLACK);
            put(5, Color.BLACK);
            put(6, Color.BLACK);
            put(7, Color.BLACK);
            put(8, Color.BLACK);
            put(9, Color.BLACK);
            put(10, Color.BLACK);
        }
    });
    int tag;

    public Tag(int tag) {
        this.tag = tag;
    }

    public int getTag() {
        return tag;
    }

    public void setTag(int tag) {
        this.tag = tag;
    }

    public static int getNbTag() {
        return colors.size();
    }
}
