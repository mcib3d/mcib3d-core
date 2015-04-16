package mcib3d.ext;
import ij.*;
import java.io.*;
import java.io.BufferedReader;
import java.io.InputStream;
import java.text.NumberFormat;
import java.util.Locale;
import mcib3d.utils.*;

/**
 *  Interface pour imod <br>
 *
 * @author     thomas
 * @created    21 juillet 2003
 */
public class Imod {

	private final static String SHELL = "csh";
	ImodConf conf;
	Shell shell;
	String RepertoireTmp;
	String RepertoireScripts;
	boolean debug = false;


	/**
	 *  Constructor for the Imod object
	 */
	public Imod() {
		conf = new ImodConf();
		shell = new Shell();
		// Init system, check if the startup file is present
		File imodstartup = new File("");
		if (IJ.isLinux()) {
			imodstartup = new File("/usr/local/IMOD/IMOD-startup." + SHELL);
		} else if (IJ.isWindows() || IJ.isVista()) {
			imodstartup = new File("/usr/local/IMOD/IMOD-startup." + SHELL);
		} else if (IJ.isMacOSX()) {
			imodstartup = new File("/Applications/IMOD/IMOD-startup." + SHELL);
		} else {
			IJ.log("Unknown System for IMOD");
		}
		if (!imodstartup.exists()) {
			String imod_system = "";
			String osname = System.getProperty("os.name");
			IJ.log("The startup file is not present");
			IJ.log("Your system is " + osname);
			if (osname.startsWith("Windows")) {
				imod_system = "IMOD-cygwin." + SHELL;
			} else if (osname.startsWith("Mac")) {
				imod_system = "IMOD-mac." + SHELL;
			} else if (osname.startsWith("Linux")) {
				imod_system = "IMOD-linux." + SHELL;
			} else if (osname.startsWith("Irix")) {
				imod_system = "IMOD-sgi." + SHELL;
			} else {
				System.out.println("Unknown System for IMOD");
			}
			if (imod_system.length() > 0) {
				IJ.log("Copy the file " + imod_system + " to " + imodstartup);
			}
		}
	}


	/**
	 *  Sets the conf attribute of the Imod object
	 *
	 * @param  c  The new conf value
	 */
	public void setConf(ImodConf c) {
		conf = c;
	}


	/**
	 *  Sets the repertoireTmp attribute of the ImodConf object
	 *
	 * @param  rep  The new repertoireTmp value
	 */
	public void setRepertoireTmp(String rep) {
		RepertoireTmp = rep;
	}


	/**
	 *  Gets the repertoireTmp attribute of the Imod object
	 *
	 * @return    The repertoireTmp value
	 */
	public String getRepertoireTmp() {
		return RepertoireTmp;
	}


	/**
	 *  Gets the conf attribute of the Imod object
	 *
	 * @return    The conf value
	 */
	public ImodConf getConf() {
		return conf;
	}


	/**
	 *  Sets the repertoireScripts attribute of the ImodConf object
	 *
	 * @param  rep  The new repertoireScripts value
	 */
	public void setRepertoireScripts(String rep) {
		RepertoireScripts = rep;
	}



	/**
	 *  Création du fichier de param�res pour copytomocoms
	 *
	 * @param  Tmp  Description of the Parameter
	 */
	public void EcritFichierParams(String Tmp) {
		try {
			File fichier = new File(Tmp + "params.txt");
			FileWriter fw = new FileWriter(fichier);
			BufferedWriter bw = new BufferedWriter(fw);
			String e = conf.getEraser() ? "y" : "n";
			bw.write("1\nc\nn\n" + e + "\nn\n" + conf.getNomSerie() + "\n\n\n" + conf.getBille() + "\n" + conf.getTiltAngle() + "\nn\n1\n" + conf.getAngleDebut() + "," + conf.getAngleIncrement() + "\n" + conf.getExclure() + "\n\n");
			bw.close();
			fw.close();
		} catch (IOException e) {
			;
		}
	}


	/**
	 *  Description of the Method
	 */
	public void EcritFichierParams() {
		EcritFichierParams("");
	}


	/**
	 *  Description of the Method
	 *
	 * @param  nbimage  Description of the Parameter
	 */
	public void EcritFichierAngles(int nbimage) {
		EcritFichierAngles(nbimage, "");
	}


	/**
	 *  Création du fichier d'angles
	 *
	 * @param  nbimage  Description of the Parameter
	 * @param  tmp      Description of the Parameter
	 */
	public void EcritFichierAngles(int nbimage, String tmp) {
		try {
			File fichier = new File(tmp + conf.getNomSerie() + ".tlt");
			FileWriter fw = new FileWriter(fichier);
			BufferedWriter bw = new BufferedWriter(fw);
			float a = conf.getAngleDebut() + conf.getAddAngles();
			for (int i = 0; i < nbimage - 1; i++) {
				bw.write(a + "\n");
				a += conf.getAngleIncrement();
			}
			bw.write("" + a);
			bw.close();
			fw.close();
		} catch (IOException e) {
			;
		}
	}


	/**
	 *  Création du fichier d'angles
	 *
	 * @param  nbimage  Description of the Parameter
	 * @param  tmp      Description of the Parameter
	 * @param  xtilt    Description of the Parameter
	 */
	public void EcritFichierXTilt(int nbimage, double xtilt, String tmp) {
		try {
			File fichier = new File(tmp + conf.getNomSerie() + ".xtilt");
			FileWriter fw = new FileWriter(fichier);
			BufferedWriter bw = new BufferedWriter(fw);
			for (int i = 0; i < nbimage - 1; i++) {
				bw.write(xtilt + "\n");
			}
			bw.write("" + xtilt);
			bw.close();
			fw.close();
		} catch (IOException e) {
			;
		}
	}


	/**
	 *  Description of the Method
	 *
	 * @param  nbimage  Description of the Parameter
	 * @return          Description of the Return Value
	 */
	public boolean TestFichierAngles(int nbimage) {
		return TestFichierAngles(nbimage, "");
	}


	/**
	 *  Description of the Method
	 *
	 * @param  nbimage  Description of the Parameter
	 * @param  tmp      Description of the Parameter
	 * @return          Description of the Return Value
	 */
	public boolean TestFichierAngles(int nbimage, String tmp) {
		float a[] = new float[nbimage];
		Float A;
		String data;
		int i;
		int index = 0;
		boolean croissant;

		try {
			File fichier = new File(tmp + conf.getNomSerie() + ".tlt");
			FileReader fw = new FileReader(fichier);
			BufferedReader bw = new BufferedReader(fw);
			data = bw.readLine();
			while ((index < nbimage) && (data.length() >= 0)) {
				A = new Float(data);
				a[index] = A.floatValue();
				if (debug) {
					IJ.write("data angle=" + data + " index=" + index + "/" + nbimage + " a=" + a[index]);
				}
				data = bw.readLine();
				index++;
			}
		} catch (IOException e) {
			;
		}

		// test si les nombres sont dans l'ordre
		i = 0;
		croissant = (a[i] < a[i + 1]);
		while (((i < index - 1) && (a[i] < a[i + 1]) && (croissant)) || ((i < index - 1) && (a[i] > a[i + 1]) && (!croissant))) {
			if (debug) {
				IJ.write("a[" + i + "]=" + a[i] + " a[" + (i + 1) + "]=" + a[i + 1] + " /" + index);
			}
			i++;
		}

		if (i < index - 1) {
			IJ.write("Test angle");
			IJ.write("a[i]=" + a[i] + " a[i+1]=" + a[i + 1]);
			return false;
		} else {
			return true;
		}
	}


	/**
	 *  Lit le fichier align.log pour extraire l'axe de tilt calcul� *
	 *
	 * @param  nbimage  Description of the Parameter
	 * @param  tmp      Description of the Parameter
	 */
	public void GetAngleFromAlign(int nbimage, String tmp) {
		String data;
		int list = -1;
		float angle = 0.0f;
		String extra;
		double sum = 0.0;
		int nbview = 0;
		try {
			File fichier = new File(tmp + "align.log");
			FileReader fw = new FileReader(fichier);
			BufferedReader bw = new BufferedReader(fw);
			data = bw.readLine();
			while (data.length() >= 0) {
				if (data.indexOf("deltilt") > 0) {

					list = 0;
				}
				data = bw.readLine();
				if ((list >= 0) && (list < nbimage)) {
					if (data.indexOf("0.0000      0.00") > 0) {
						extra = data.substring(8, 16).trim();
						Double D = new Double(extra);
						sum += D.doubleValue();
						nbview++;
					}
					list++;
				}
			}
			bw.close();
			fw.close();
		} catch (IOException e) {
			;
		} catch (java.lang.NullPointerException e) {
			;
		}
		conf.setTiltAngle((float) (sum / nbview));
	}


	/**
	 *  Description of the Method
	 *
	 * @param  nbimage  Description of the Parameter
	 */
	public void GetAngleFromAlign(int nbimage) {
		GetAngleFromAlign(nbimage, "");
	}


	/**
	 *  Description of the Method
	 */
	public void Tomopitch() {
		shell.runCommand(SHELL + " " + RepertoireScripts + "tomopitch.sh " + RepertoireTmp);
		TomopitchInfo(RepertoireTmp);
	}


	/**
	 *  Description of the Method
	 */
	public void TomopitchInfo() {
		TomopitchInfo("");
	}


	/**
	 *  Description of the Method
	 *
	 * @param  tmp  Description of the Parameter
	 */
	public void TomopitchInfo(String tmp) {
		String data;
		int list = -1;
		String extra;
		Float f;
		Integer i;
		try {
			File fichier = new File(tmp + "tomopitch.log");
			FileReader fw = new FileReader(fichier);
			BufferedReader bw = new BufferedReader(fw);
			data = bw.readLine();
			while (data.length() >= 0) {
				if (data.indexOf("itch between samples") > 0) {
					list = 0;
				}
				data = bw.readLine();
				if ((list == 0)) {
					if (data.indexOf("to total angle offset") > 0) {
						extra = data.substring(33, 40).trim();
						f = new Float(extra);
						conf.setAddAngles(conf.getAddAngles() + f.floatValue());
					}
					if (data.indexOf("Z shift of") > 0) {
						extra = data.substring(40, 46).trim();
						f = new Float(extra);
						conf.setShiftY(conf.getShiftY() + f.floatValue());
					}
					if (data.indexOf("thickness") > 0) {
						extra = data.substring(75, 79).trim();
						f = new Float(extra);
						IJ.write("Computed thickness : " + f);
						// on ne tient pas compte de  cette épaisseur
					}
				}
			}
			bw.close();
			fw.close();
		} catch (IOException e) {
			;
		} catch (java.lang.NullPointerException e) {
			;
		}
	}


	/**
	 *  Description of the Method
	 */
	public void Newst2() {
		Newst2("");
	}


	/**
	 *  Création d'un fichier newst2.com (correction du bug ,10)
	 *
	 * @param  tmp  Description of the Parameter
	 */
	public void Newst2(String tmp) {
		String data;
		String replace;
		try {
			File fichier = new File(tmp + "newst.com");
			FileReader fr = new FileReader(fichier);
			BufferedReader br = new BufferedReader(fr);
			File fichier2 = new File(tmp + "newst2.com");
			FileWriter fw = new FileWriter(fichier2);
			BufferedWriter bw = new BufferedWriter(fw);
			data = br.readLine();
			while (data.length() >= 0) {
				if (data.indexOf("$newst") >= 0) {
					int pos = data.indexOf(",10");
					replace = data.substring(0, pos).concat(",,");
					replace = replace.concat(data.substring(pos + 3, data.length()));
				} else {
					replace = new String(data);
				}
				bw.write(replace + "\n");
				bw.flush();
				data = br.readLine();
			}
			bw.close();
			fw.close();
			br.close();
			fr.close();
		} catch (IOException e) {
			System.out.println("Pb io " + e);
		} catch (java.lang.NullPointerException e) {
			;
		}
	}


	/**
	 *  Creation d'un fichier track2.com (correction du bug pour les version 3)
	 *
	 * @param  tmp  Description of the Parameter
	 */
	public void Track2(String tmp) {
		String data;
		String replace;
		try {
			File fichier = new File(tmp + "track.com");
			FileReader fr = new FileReader(fichier);
			BufferedReader br = new BufferedReader(fr);
			File fichier2 = new File(tmp + "track2.com");
			FileWriter fw = new FileWriter(fichier2);
			BufferedWriter bw = new BufferedWriter(fw);
			data = br.readLine();
			while (data.length() >= 0) {
				if (data.indexOf("radius of beads") >= 0) {
					replace = "" + conf.getBille();
					replace = replace.concat(data.substring(1, data.length()));
				} else {
					replace = new String(data);
				}
				bw.write(replace + "\n");
				bw.flush();
				data = br.readLine();
			}
			bw.close();
			fw.close();
			br.close();
			fr.close();
		} catch (IOException e) {
			System.out.println("Pb io " + e);
		} catch (java.lang.NullPointerException e) {
			;
		}
	}


	/**
	 *  Description of the Method
	 */
	public void Tilt2() {
		Tilt2("");
	}


	/**
	 *  Création d'un fichier tilt2.com (mode parallel et type 2)
	 *
	 * @param  tmp  Description of the Parameter
	 */
	public void Tilt2(String tmp) {
		String data;
		String replace;
		try {
			File fichier = new File(tmp + "tilt.com");
			FileReader fr = new FileReader(fichier);
			BufferedReader br = new BufferedReader(fr);
			File fichier2 = new File(tmp + "tilt2.com");
			FileWriter fw = new FileWriter(fichier2);
			BufferedWriter bw = new BufferedWriter(fw);
			data = br.readLine();
			while (data.length() >= 0) {
				if (data.indexOf("PERPENDICULAR") >= 0) {
					replace = "PARALLEL";
				} else if (data.indexOf("MODE") >= 0) {
					replace = "MODE 2";
				} else if (data.indexOf("THICKNESS") >= 0) {
					replace = "THICKNESS " + conf.getThickness();
				} else if (data.indexOf("RADIAL .35") >= 0) {
					replace = "RADIAL .5 .05 ";
				} else if (data.indexOf("XAXISTILT 0.") >= 0) {
					replace = "XAXISTILT " + conf.getTiltAngle();
				} else {
					replace = data;
				}
				bw.write(replace + "\n");
				bw.flush();
				data = br.readLine();
			}
			bw.close();
			fw.close();
			br.close();
			fr.close();
		} catch (IOException e) {
			System.out.println("Pb io " + e);
		} catch (java.lang.NullPointerException e) {
			;
		}
	}


	/**
	 *  Description of the Method
	 */
	public void Align2() {
		Align2("");
	}


	/**
	 *  Description of the Method
	 *
	 * @param  tmp  Description of the Parameter
	 */
	public void Align2(String tmp) {
		String data;
		String replace;
		try {
			File fichier = new File(tmp + "align.com");
			FileReader fr = new FileReader(fichier);
			BufferedReader br = new BufferedReader(fr);
			File fichier2 = new File(tmp + "align2.com");
			FileWriter fw = new FileWriter(fichier2);
			BufferedWriter bw = new BufferedWriter(fw);
			data = br.readLine();
			while (data.length() >= 0) {
				if (data.indexOf("AMOUNT TO MOVE TILT AXIS IN Z") >= 0) {
					replace = conf.getShiftY() + "     AMOUNT TO MOVE TILT AXIS IN Z, OR 1000 to move midpoint ";

				} else if (data.indexOf("AMOUNT TO ADD TO ALL ANGLES") >= 0) {
					replace = conf.getAddAngles() + "     AMOUNT TO ADD TO ALL ANGLES (DEGREES)";

				} else {
					replace = data;
				}
				bw.write(replace + "\n");
				bw.flush();
				data = br.readLine();
			}
			bw.close();
			fw.close();
			br.close();
			fr.close();
		} catch (IOException e) {
			System.out.println("Pb io " + e);
		} catch (java.lang.NullPointerException e) {
			;
		}
	}


	/**
	 *  Description of the Method
	 */
	public void Sample() {
		shell.runCommand("cp " + RepertoireTmp + "tiltshift.com " + RepertoireTmp + "tilt.com");
		shell.runCommand(SHELL + " " + RepertoireScripts + "sample.sh " + RepertoireTmp);
	}


	/**
	 *  Description of the Method
	 */
	public void RecShift() {
		shell.runCommand(SHELL + " " + RepertoireScripts + "tiltshift.sh " + RepertoireTmp);
	}


	/**
	 *  Description of the Method
	 */
	public void TiltShift() {
		TiltShift("");
	}


	/**
	 *  Création du fichier tilt pour le calcul du shift
	 *
	 * @param  tmp  Description of the Parameter
	 */
	public void TiltShift(String tmp) {
		String data;
		String replace;
		boolean shift = false;
		try {
			File fichier = new File(tmp + "tilt.com");
			FileReader fr = new FileReader(fichier);
			BufferedReader br = new BufferedReader(fr);
			File fichier2 = new File(tmp + "tiltshift.com");
			FileWriter fw = new FileWriter(fichier2);
			BufferedWriter bw = new BufferedWriter(fw);
			data = br.readLine();
			while (data.length() >= 0) {
				if (data.indexOf("SHIFT") >= 0) {
					replace = "SHIFT 0 " + conf.getShiftY();
					shift = true;
				} else if ((data.indexOf("DONE") >= 0) && (!shift)) {
					replace = "SHIFT 0 " + conf.getShiftY() + "\nMODE 1";
				} else if (data.indexOf("THICKNESS") >= 0) {
					replace = "THICKNESS " + conf.getThickness();
				} else {
					replace = data;
				}
				bw.write(replace + "\n");
				bw.flush();
				data = br.readLine();
			}
			bw.close();
			fw.close();
			br.close();
			fr.close();
		} catch (IOException e) {
			System.out.println("Pb io " + e);
		} catch (java.lang.NullPointerException e) {
			;
		}
	}


	/**
	 *  Constructor for the CreateXf object
	 *
	 * @param  angles  Description of the Parameter
	 * @param  name    Description of the Parameter
	 * @param  shiftX  Description of the Parameter
	 * @param  shiftY  Description of the Parameter
	 */
	public void CreateXf(double[] angles, double[] shiftX, double[] shiftY, String name) {
		NumberFormat nf = NumberFormat.getInstance(Locale.ENGLISH);
		nf.setMaximumFractionDigits(7);
		NumberFormat nf2 = NumberFormat.getInstance(Locale.ENGLISH);
		nf2.setMaximumFractionDigits(3);
		StringBuffer buff;
		String s;
		int i;
		int j;
		try {
			File fichier = new File(name);
			FileWriter fw = new FileWriter(fichier);
			BufferedWriter bw = new BufferedWriter(fw);
			double ang;
			for (i = 0; i < angles.length; i++) {
				ang = Math.toRadians(angles[i]);
				// cos
				buff = new StringBuffer(12);
				for (j = 0; j < 12; j++) {
					buff.append(" ");
				}
				s = nf.format(Math.cos(ang));
				buff.insert(12 - s.length(), s);
				bw.write(new String(buff));
				// sin
				buff = new StringBuffer(12);
				for (j = 0; j < 12; j++) {
					buff.append(" ");
				}
				s = nf.format(Math.sin(ang));
				buff.insert(12 - s.length(), s);
				bw.write(new String(buff));
				// -sin
				buff = new StringBuffer(12);
				for (j = 0; j < 12; j++) {
					buff.append(" ");
				}
				s = nf.format(Math.sin(-ang));
				buff.insert(12 - s.length(), s);
				bw.write(new String(buff));
				// cos
				buff = new StringBuffer(12);
				for (j = 0; j < 12; j++) {
					buff.append(" ");
				}
				s = nf.format(Math.cos(ang));
				buff.insert(12 - s.length(), s);
				bw.write(new String(buff));
				// shift X
				buff = new StringBuffer(12);
				for (j = 0; j < 12; j++) {
					buff.append(" ");
				}
				s = nf2.format(shiftX[i]);
				buff.insert(12 - s.length(), s);
				bw.write(new String(buff));
				// shift Y
				buff = new StringBuffer(12);
				for (j = 0; j < 12; j++) {
					buff.append(" ");
				}
				s = nf2.format(shiftY[i]);
				buff.insert(12 - s.length(), s);
				bw.write(new String(buff));
				// newline
				bw.write("\n");
			}
			bw.close();
			fw.close();
		} catch (IOException e) {
			System.out.println("Pb io " + e);
		}
	}


	/**
	 *  Create a Xf file for a given angle of rotation and shifts
	 *
	 * @param  name   filename
	 * @param  angle  angle of rotation
	 * @param  nbima  number of image
	 * @param  shift  Description of the Parameter
	 */
	public void CreateXf(double angle, double[][] shift, int nbima, String name) {
		double angles[] = new double[nbima];
		double sX[] = new double[nbima];
		double sY[] = new double[nbima];
		for (int i = 0; i < nbima; i++) {
			angles[i] = angle;
			sX[i] = shift[0][i];
			sY[i] = shift[1][i];
		}
		CreateXf(angles, sX, sY, name);
	}


	/**
	 *  Description of the Method
	 *
	 * @param  angle  Description of the Parameter
	 * @param  nbima  Description of the Parameter
	 * @param  name   Description of the Parameter
	 */
	public void CreateXf(double angle, int nbima, String name) {
		double angles[] = new double[nbima];
		double sX[] = new double[nbima];
		double sY[] = new double[nbima];
		for (int i = 0; i < nbima; i++) {
			angles[i] = angle;
			sX[i] = 0.0;
			sY[i] = 0.0;
		}
		CreateXf(angles, sX, sY, name);
	}


	/**
	 *  Description of the Method
	 *
	 * @param  tmp    Description of the Parameter
	 * @param  serie  Description of the Parameter
	 * @param  nbima  Description of the Parameter
	 * @return        Description of the Return Value
	 */
	public double[][] ExtractShiftsFromXG(String tmp, String serie, int nbima) {
		String data;
		double tab[][] = new double[2][nbima];
		String extra;
		Double D;
		try {
			File fichier = new File(tmp + serie + ".prexg");
			FileReader fw = new FileReader(fichier);
			BufferedReader bw = new BufferedReader(fw);
			for (int i = 0; i < nbima; i++) {
				// shift X
				data = bw.readLine();
				extra = data.substring(50, 60).trim();
				D = new Double(extra);
				tab[0][i] = D.doubleValue();
				// shift Y
				extra = data.substring(62, 72).trim();
				D = new Double(extra);
				tab[1][i] = D.doubleValue();
			}
			bw.close();
			fw.close();
		} catch (IOException e) {
			;
		} catch (java.lang.NullPointerException e) {
			;
		}
		return tab;
	}


	/**
	 *  Description of the Method
	 */
	// A verifier pour tmp
	public void Eraser2() {
		String data;
		String replace;
		try {
			File fichier = new File("eraser.com");
			FileReader fr = new FileReader(fichier);
			BufferedReader br = new BufferedReader(fr);
			File fichier2 = new File("eraser2.com");
			FileWriter fw = new FileWriter(fichier2);
			BufferedWriter bw = new BufferedWriter(fw);
			data = br.readLine();
			while (data.length() >= 0) {
				if (data.indexOf("List of objects to replace on all sections") >= 0) {
					replace = "/     List of objects to replace on all sections";
				} else if (data.indexOf("border to use to fit to") >= 0) {
					replace = "10     border to use to fit to";
				} else {
					replace = data;
				}
				bw.write(replace + "\n");
				bw.flush();
				data = br.readLine();
			}
			bw.close();
			fw.close();
			br.close();
			fr.close();
		} catch (IOException e) {
			System.out.println("Pb io " + e);
		} catch (java.lang.NullPointerException e) {
			;
		}
	}


	/**
	 *  Description of the Method
	 *
	 * @param  nomRaw   Description of the Parameter
	 * @param  nomMRC   Description of the Parameter
	 * @param  largeur  Description of the Parameter
	 * @param  hauteur  Description of the Parameter
	 * @param  nbimage  Description of the Parameter
	 * @param  stype    Description of the Parameter
	 */
	public void Raw2MRC(String nomRaw, String nomMRC, int largeur, int hauteur, int nbimage, String stype) {
		// lancement direct de raw2mrc ne marche pas
		//shell.LanceCommande("source /etc/profile.d/IMOD-linux.sh; raw2mrc -x 256 -y 256 -z 38 -t byte -s IMODJ/tmp/test.raw IMODJ/tmp/test.st");
		shell.runCommand(SHELL + " " + RepertoireScripts + "raw2mrc.sh " + largeur + " " + hauteur + " " + nbimage + " " + stype + " " + RepertoireTmp + " " + nomRaw + " " + nomMRC);
	}


	/**
	 *  Description of the Method
	 */
	public void CopyTomocoms() {
		EcritFichierParams(RepertoireTmp);
		shell.runCommand(SHELL + " " + RepertoireScripts + "copytest.sh " + RepertoireTmp, false);
	}


	/**
	 *  Description of the Method
	 */
	public void Xcorr() {
		shell.runCommand(SHELL + " " + RepertoireScripts + "xcorr.sh " + RepertoireTmp);
	}


	/**
	 *  Description of the Method
	 */
	public void Prenewst() {
		shell.runCommand(SHELL + " " + RepertoireScripts + "prenewst.sh " + RepertoireTmp);
	}


	/**
	 *  Description of the Method
	 */
	public void Track() {
		shell.runCommand(SHELL + " " + RepertoireScripts + "track.sh " + RepertoireTmp);
	}


	/**
	 *  Description of the Method
	 */
	public void Align() {
		shell.runCommand(SHELL + " " + RepertoireScripts + "align.sh " + RepertoireTmp);
	}


	/**
	 *  Description of the Method
	 *
	 * @param  nom    Description of the Parameter
	 * @param  model  Description of the Parameter
	 */
	public void visu(String nom, String model) {
		visu(nom, model, "", "");
	}


	/**
	 *  Description of the Method
	 *
	 * @param  a  Description of the Parameter
	 * @param  b  Description of the Parameter
	 * @param  c  Description of the Parameter
	 * @param  d  Description of the Parameter
	 */
	public void visu(String a, String b, String c, String d) {
		shell.runCommand(SHELL + " " + RepertoireScripts + "imodv.sh " + RepertoireTmp + " " + a + " " + b + " " + c + " " + d);
	}


	/**
	 *  Description of the Method
	 *
	 * @param  nom  Description of the Parameter
	 */
	public void visu(String nom) {
		visu(nom, "", "", "");
	}

}

