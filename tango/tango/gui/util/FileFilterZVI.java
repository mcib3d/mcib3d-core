package tango.gui.util;

import java.io.File;
import javax.swing.filechooser.FileFilter;
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
public class FileFilterZVI extends FileFilter implements java.io.FileFilter {

    @Override
    public boolean accept(File file) {
        String name = file.getName().toLowerCase();
        return  (file.isDirectory() || (name.endsWith(".zvi") && !file.isHidden()));
        }
     

    @Override
    public String getDescription() {
        return ".ZVI files";
    }
    
}
