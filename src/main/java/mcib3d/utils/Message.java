package mcib3d.utils;

import ij.*;
import ij.process.*;
import java.io.*;
import java.io.BufferedReader;
import java.io.InputStream;
import java.lang.Math;
import java.text.NumberFormat;
import java.util.Hashtable;
import java.util.Locale;
import java.util.Enumeration;
import java.util.jar.JarFile;
import java.util.jar.JarEntry;

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
 *  classe permettant : <BR>
 *  le stockage d'un tableau de float les calculs statistiques sur ce tableau le
 *  tri du tableau
 *
 *@author     Cedric MESSAOUDI & Thomas BOUDIER
 *@created    17 septembre 2003
 */
public class Message {

	Hashtable Messages;
	private boolean debug = false;


	/**
	 *  Constructor for the Messages object
	 */
	public Message() {
		Messages = new Hashtable();
	}


	/**
	 *  Description of the Method
	 *
	 *@param  cle  Description of the Parameter
	 *@return      Description of the Return Value
	 */
	public String get(String cle) {
		return (String) Messages.get(cle);
	}


	/**
	 *  Description of the Method
	 *
	 *@param  FichierMessages  Description of the Parameter
	 */
	public void LireMessages(String FichierMessages) {
		// test de locale
		Locale locale = Locale.getDefault();
		LireMessages(FichierMessages, locale.getLanguage().trim());
	}


	/**
	 *  Description of the Method
	 *
	 *@param  in  Description of the Parameter
	 */
	public void LireMessages(BufferedReader in) {
		Locale locale = Locale.getDefault();
		LireMessages(in, locale.getLanguage().trim());
	}


	/**
	 *  Description of the Method
	 *
	 *@param  in      Description of the Parameter
	 *@param  langue  Description of the Parameter
	 */
	public void LireMessages(BufferedReader in, String langue) {
		try {
			int lang = 0;
			String data;
			String cle;
			data = in.readLine();
			int nl = Integer.parseInt(data);
			for (int i = 0; i < nl; i++) {
				data = in.readLine();
				if (data.indexOf(langue) >= 0) {
					lang = i;
				}
			}
			data = in.readLine();
			while (data.length() > 0) {
				while (data.startsWith("//")) {
					data = in.readLine();
				}
				// cle ref
				cle = new String(data);
				for (int i = 0; i < nl; i++) {
					data = in.readLine();
					if (i == lang) {
						//IJ.write("cle : " + cle + " , data : " + data);
						Messages.put(cle, data);
					}
				}
				data = in.readLine();
			}
		} catch (IOException e) {
			IJ.write("Pb fichier message");
		} catch (java.lang.NullPointerException e) {
			;
		}
	}


	/**
	 *  Description of the Method
	 *
	 *@param  FichierMessages  Description of the Parameter
	 *@param  langue           Description of the Parameter
	 */
	public void LireMessages(String FichierMessages, String langue) {
		try {
			File fichier = new File(FichierMessages);
			FileReader fw = new FileReader(fichier);
			BufferedReader bw = new BufferedReader(fw);
			LireMessages(bw, langue);
			bw.close();
			fw.close();
		} catch (IOException e) {
			IJ.write("Pb fichier message:" + FichierMessages);
		} catch (java.lang.NullPointerException e) {
			;
		}
	}


	/**
	 *  Description of the Method
	 *
	 *@param  JarFileName      Description of the Parameter
	 *@param  FichierMessages  Description of the Parameter
	 *@param  langue           Description of the Parameter
	 */
	public void LireMessagesFromJar(String JarFileName, String FichierMessages, String langue) {
		InputStream in = null;
		try {
			JarFile jar = new JarFile(JarFileName);
			for (Enumeration e = jar.entries(); e.hasMoreElements(); ) {
				JarEntry je = (JarEntry) e.nextElement();
				if (je.getName().compareTo(FichierMessages) == 0) {
					in = jar.getInputStream(je);
					break;
				}
			}
			//FileReader fw = new FileReader(fichier);
			BufferedReader bw = new BufferedReader(new InputStreamReader(in));
			LireMessages(bw, langue);
			bw.close();
			in.close();
		} catch (IOException e) {
			IJ.write("Pb fichier message:" + FichierMessages);
		} catch (java.lang.NullPointerException e) {
			;
		}
	}

}

