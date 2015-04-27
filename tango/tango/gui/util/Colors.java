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
public class Colors {
    public static Map<String, Color> colors = Collections.unmodifiableMap(new HashMap<String, Color>() {{
        put("NONE", Color.black);
        put("BLUE", new Color(0,    0, 1f));
        put("GREEN", new Color(0,    1f, 0));
        put("YELLOW", new Color(0.75f, 0.75f, 0));
        put("RED", new Color(1f,    0, 0));
        put("CYAN", new Color(0, 1f, 1f));
        put("MAGENTA", new Color(1f,    0, 1f));
        put("WHITE", Color.white);
        
    }});
    
    public final static String[] colorNames = {"NONE", "BLUE", "GREEN", "YELLOW", "RED", "CYAN", "MAGENTA", "WHITE"};
}
