package tango.util;

import ij.IJ;
import java.io.File;

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

public class SearchFile {
    public static File searchOnDrive(File refDrive, String fileName) {
        File drive = refDrive;
        while (drive.getParentFile()!=null) drive=drive.getParentFile();
        System.out.println("refDrive:"+refDrive+ " refDrivename:"+refDrive.getName()+ " drive:"+drive);
        return getFile(drive, fileName);
    }
    public static File searchAllDrives(String fileName) {
        File[] paths = File.listRoots();
        for (File f:paths) {
            File res = getFile(f, fileName);
            if (res!=null) return res;
        }
        return null;
    }
    
    public static File getFile(File directory, String fileName) {
        System.out.println("Searching:"+fileName+" in "+directory.getAbsolutePath());
        File[] fList = directory.listFiles();
        if(fList!=null){
            for (File file : fList) {
                if (file.isFile()) {
                    if (file.getName().equals(fileName)) return file;
                } else if (file.isDirectory()) {
                    getFile(file, fileName);
                }
            }
        }
        return null;
    }
}
