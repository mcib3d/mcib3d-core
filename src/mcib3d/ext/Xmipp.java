package mcib3d.ext;
import ij.*;
import java.io.*;
import java.io.BufferedReader;
import java.io.InputStream;
import java.text.NumberFormat;
import java.util.Locale;

/**
 *  Interface pour Xmipp
 *
 * @author     thomas
 * @created    6 octobre 2004
 */
public class Xmipp {

	final static int WBP = 0;
	final static int ART = 1;
	final static int SIRT = 2;

	final static String pathbin = "c:\\cygwin\\home\\cedric\\NewXmipp\\bin\\";
	final static String pathbinlinux = "/home/cedric/NewXmipp/bin/";
	final static String xmipp_prefix = "xmipp_";

	XmippConf conf;


	/**
	 *  Sets the conf attribute of the Xmipp object
	 *
	 * @param  cf  The new conf value
	 */
	public void setConf(XmippConf cf) {
		conf = cf;
	}


	/**
	 *  Constructor for the EcritInfoTomo object
	 *
	 * @param  name  Description of the Parameter
	 */
	public void EcritInfoTomo(String name) {
		String nomimage = conf.getNomSerie();
		float angdeb = conf.getAngleDebut();
		float anginc = conf.getAngleIncrement();
		float angfin = conf.getAngleFin();
		float tiltaxis = conf.getTiltAxis();
		NumberFormat nf = NumberFormat.getIntegerInstance(Locale.ENGLISH);
		nf.setMinimumIntegerDigits(3);
		NumberFormat nf2 = NumberFormat.getInstance(Locale.ENGLISH);
		nf2.setMinimumFractionDigits(5);
		float i;
		int j;
		try {
			File fichier = new File(name);
			FileWriter fw = new FileWriter(fichier);
			BufferedWriter bw = new BufferedWriter(fw);
			j = 1;
			bw.write("; Headerinfo columns: rot (1), tilt (2), psi (3), Xoff (4), Yoff (5)\n");
			for (i = angdeb; i <= angfin; i += anginc) {
				bw.write(" ; " + nomimage + nf.format(j) + ".spi\n");
				bw.write(" " + j + " 5 " + nf2.format(tiltaxis) + " " + nf2.format(i) + " 0.00000 0.00000 0.00000\n");
				j++;
			}

			bw.close();
			fw.close();
		} catch (IOException e) {
			System.out.println("Pb io : " + name + " (" + e + ")");
		}
	}


	/**
	 *  Description of the Method
	 *
	 * @param  name           Description of the Parameter
	 * @param  pathdata       Description of the Parameter
	 * @param  pathdatalinux  Description of the Parameter
	 */
	public void EcritScriptReconstruction(String pathdata, String pathdatalinux, String name) {
		String nomimage = conf.getNomSerie();
		int tailleX = conf.getTailleX();
		int tailleY = conf.getTailleY();
		int taille = (int) (Math.sqrt(tailleX * tailleX + tailleY * tailleY));
		int rec = conf.getRec();
		int ite = conf.getIteration();
		try {
			File fichier = new File(pathdata + "/" + name);
			FileWriter fw = new FileWriter(fichier);
			BufferedWriter bw = new BufferedWriter(fw);

			bw.write("cd " + pathdatalinux + " \n");
			bw.write(pathbinlinux + xmipp_prefix + "do_selfile \"" + nomimage + "???.spi\" > " + nomimage + ".sel \n");

			bw.write("echo Assigner angles tilt \n");
			bw.write(pathbinlinux + xmipp_prefix + "headerinfo -assign -i data -o " + nomimage + ".sel \n");

			bw.write("echo Reconstruction : ");
			if (rec == WBP) {
				bw.write("WBP \n");
				bw.write(pathbinlinux + xmipp_prefix + "WBP -i " + nomimage + ".sel -o " + nomimage + "WBP.vol -radius " + taille + " -use_each_image\n");
			} else if (rec == ART) {
				bw.write("ART \n");
				bw.write(pathbinlinux + xmipp_prefix + "art -i " + nomimage + ".sel -l 0.1 -n " + ite + " -o " + nomimage + "ART \n");
			} else if (rec == SIRT) {
				bw.write("SIRT \n");
				bw.write(pathbinlinux + xmipp_prefix + "art -i " + nomimage + ".sel -SIRT -o " + nomimage + "SIRT \n");
			}
			bw.close();
			fw.close();
		} catch (IOException e) {
			System.out.println("Pb io : " + name + " (" + e + ")");
		}

	}


	/**
	 *  Description of the Method
	 *
	 * @param  name      Description of the Parameter
	 * @param  pathdata  Description of the Parameter
	 */
	public void EcritScriptAlign(String pathdata, String name) {
		NumberFormat nf = NumberFormat.getIntegerInstance(Locale.ENGLISH);
		nf.setMinimumIntegerDigits(3);
		try {
			int i = 1;
			String nomimage = conf.getNomSerie();
			File fichier = new File(name);
			FileWriter fw = new FileWriter(fichier);
			BufferedWriter bw = new BufferedWriter(fw);

			bw.write("cd " + pathdata + " \n");

			for (float a = conf.getAngleDebut(); a < conf.getAngleFin(); a += conf.getAngleIncrement()) {
				bw.write("cp " + nomimage + nf.format(i) + ".spi ref.spi \n");
				bw.write(pathbin + xmipp_prefix + "mask -i ref.spi -o ref.mask.spi -mask gaussian -32 \n");
				bw.write("cp " + nomimage + nf.format(i + 1) + ".spi img.spi \n");
				bw.write(pathbin + xmipp_prefix + "mask -i img.spi -o img.mask.spi -mask gaussian -32 \n");
				bw.write(pathbin + xmipp_prefix + "do_selfile \"img.mask.spi\" > img.sel \n");
				bw.write(pathbin + xmipp_prefix + "align2d -i img.sel -ref ref.mask.spi -only_trans -oext align.spi -doc aligndoc" + (i + 1) + ".txt \n");
				i++;
			}

			bw.close();
			fw.close();
		} catch (IOException e) {
			System.out.println("Pb io : " + name + " (" + e + ")");
		}
	}
}

