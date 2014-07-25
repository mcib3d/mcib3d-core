package mcib3d.utils;

import ij.*;
import ij.gui.*;
import ij.io.*;
import ij.process.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import java.io.*;

/**
Copyright (C) Thomas Boudier

License:
This program is free software; you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation; either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

/**
 *  Opens a folder of images as a stack.
 *
 * @author     thomas
 * @created    17 septembre 2003
 */
public class StackFolderOpener {

	private static boolean grayscale;
	private static boolean halfSize;
	private int n, start, increment;
	private String filter;


	/**
	 *  Description of the Method
	 *
	 * @param  directory  Description of the Parameter
	 * @return            Description of the Return Value
	 */
	public ImageStack open(String directory) {
		return open(directory, true);
	}


	/**
	 *  ouvre les images d'un repertoire sous la forme d'un stack
	 *
	 * @param  directory  repertoire ou il faut chercher les images
	 * @param  ijshow     Description of the Parameter
	 * @return            ImageStack avec toutes les images du repertoire
	 */
	public ImageStack open(String directory, boolean ijshow) {

		String[] list = new File(directory).list();
		if (list == null) {
			return null;
		}

		ij.util.StringSorter.sort(list);

		int width = 0;

		int height = 0;

		int type = 0;
		ImageStack stack = null;

		try {
			for (int i = 0; i < list.length; i++) {
				if (list[i].endsWith(".txt")) {
					continue;
				}
				ImagePlus imp = new Opener().openImage(directory, list[i]);
				if (imp != null) {
					width = imp.getWidth();
					height = imp.getHeight();
					type = imp.getType();
					break;
				}
			}
			increment = 1;
			n = list.length;
			start = 1;

			int count = 0;
			int counter = 0;
			for (int i = start - 1; i < list.length; i++) {
				//if ((list[i].endsWith(".txt")) {
				if (!(list[i].endsWith(".tif"))) {
					continue;
				}
				if ((counter++ % increment) != 0) {
					continue;
				}
				ImagePlus imp = new Opener().openImage(directory, list[i]);
				if (imp != null && stack == null) {
					width = imp.getWidth();
					height = imp.getHeight();
					type = imp.getType();
					ColorModel cm = imp.getProcessor().getColorModel();
					stack = new ImageStack(width, height, cm);
				}
				if (imp == null) {
					if (ijshow) {
						IJ.log(list[i] + ": unable to open");
					}
				} else if (imp.getWidth() != width || imp.getHeight() != height) {
					if (ijshow) {
						IJ.log(list[i] + ": wrong dimensions");
					}
				} else if (imp.getType() != type) {
					if (ijshow) {
						IJ.log(list[i] + ": wrong type");
					}
				} else {
					count = stack.getSize() + 1;
					if (ijshow) {
						IJ.showStatus(count + "/" + n);
					}
					if (ijshow) {
						IJ.showProgress((double) count / n);
					}
					ImageProcessor ip = imp.getProcessor();
					if (grayscale) {
						if (nonStandardLut(ip)) {
							ip = new ColorProcessor(imp.getImage());
						}
						ip = ip.convertToByte(true);
					}
					stack.addSlice(imp.getTitle(), ip);
				}
				if (count >= n) {
					break;
				}
				//System.gc();
			}
		} catch (OutOfMemoryError e) {
			if (ijshow) {
				IJ.outOfMemory("FolderOpener");
			}
			stack.trim();
		}

		if (ijshow) {
			IJ.showProgress(1.0);
		}
		return stack;
	}


	/**
	 *  pour les images couleurs verifie qu'elles sont standard
	 *
	 * @param  ip  image
	 * @return     vrai si image couleur standard, faux sinon
	 */
	boolean nonStandardLut(ImageProcessor ip) {
		ColorModel cm = ip.getColorModel();
		if (!(cm instanceof IndexColorModel)) {
			return false;
		}
		IndexColorModel icm = (IndexColorModel) cm;
		int mapSize = icm.getMapSize();
		if (mapSize != 256) {
			return true;
		}
		byte[] reds = new byte[256];
		byte[] greens = new byte[256];
		byte[] blues = new byte[256];
		icm.getReds(reds);
		icm.getGreens(greens);
		icm.getBlues(blues);
		boolean isStandard = true;
		int inc = (reds[1] & 255) - (reds[0] & 255);
		for (int i = 0; i < 256; i++) {
			if ((reds[i] != greens[i]) || (greens[i] != blues[i])) {
				isStandard = false;
				break;
			}
			if (i > 0 && ((reds[i] & 255) - (reds[i - 1] & 255)) != inc) {
				isStandard = false;
				break;
			}
		}
		return !isStandard;
	}

}

