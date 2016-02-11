package mcib3d.ext;
import ij.*;
/**
 *  Parametres de configuration pour Xmipp
 *
 * @author     thomas
 * @created    6 octobre 2004
 */
public class XmippConf {

	String NomSerie;
	float angdeb, anginc, angfin, tiltaxis;
	int tailleX, tailleY;
	int rec;
	int ite;


	/**
	 *  Constructor for the XmippConf object
	 */
	public XmippConf() {
		NomSerie = Prefs.get("xmipp_conf_nomserie.string", "serie");
		angdeb = (float) Prefs.get("xmipp_conf_angdeb.double", -90.0);
		anginc = (float) Prefs.get("xmipp_conf_anginc.double", 2.0);
		angfin = (float) Prefs.get("xmipp_conf_angfin.double", 90.0);
		tiltaxis = (float) Prefs.get("xmipp_conf_tiltaxis.double", 0.0);
		rec = (int) Prefs.get("xmipp_conf_rec.int", 0);
		ite = (int) Prefs.get("xmipp_conf_ite.int", 0);
	}


	/**
	 *  Constructor for the XmippConf object
	 *
	 * @param  nom  Nom de la serie
	 * @param  ad   angle de début
	 * @param  ai   incrément pour l'angle
	 * @param  af   Description of the Parameter
	 * @param  r    Description of the Parameter
	 */
	public XmippConf(String nom, float ad, float ai, float af, int r) {
		NomSerie = nom;
		angdeb = ad;
		anginc = ai;
		angfin = af;
		rec = r;
	}


	/**
	 *  Constructor for the updatePrefs object
	 */
	public void updatePrefs() {
		Prefs.set("xmipp_conf_nomserie.string", NomSerie);
		Prefs.set("xmipp_conf_angdeb.double", angdeb);
		Prefs.set("xmipp_conf_anginc.double", anginc);
		Prefs.set("xmipp_conf_angfin.double", angfin);
		Prefs.set("xmipp_conf_tiltaxis.double", tiltaxis);
		Prefs.set("xmipp_conf_rec.int", rec);
		Prefs.set("xmipp_conf_ite.int", ite);
	}


	/**
	 *  Sets the nomSerie attribute of the XmippConf object
	 *
	 * @param  s  The new nomSerie value
	 */
	public void setNomSerie(String s) {
		NomSerie = s;
	}


	/**
	 *  Gets the nomSerie attribute of the XmippConf object
	 *
	 * @return    The nomSerie value
	 */
	public String getNomSerie() {
		return NomSerie;
	}


	/**
	 *  Sets the angleDebut attribute of the XmippConf object
	 *
	 * @param  t  The new angleDebut value
	 */
	public void setAngleDebut(float t) {
		angdeb = t;
	}


	/**
	 *  Sets the angleFin attribute of the XmippConf object
	 *
	 * @param  t  The new angleFin value
	 */
	public void setAngleFin(float t) {
		angfin = t;
	}


	/**
	 *  Sets the tiltAxis attribute of the XmippConf object
	 *
	 * @param  t  The new tiltAxis value
	 */
	public void setTiltAxis(float t) {
		tiltaxis = t;
	}


	/**
	 *  Gets the angleDebut attribute of the XmippConf object
	 *
	 * @return    The angleDebut value
	 */
	public float getAngleDebut() {
		return angdeb;
	}


	/**
	 *  Gets the angleFin attribute of the XmippConf object
	 *
	 * @return    The angleFin value
	 */
	public float getAngleFin() {
		return angfin;
	}


	/**
	 *  Gets the tiltAxis attribute of the XmippConf object
	 *
	 * @return    The tiltAxis value
	 */
	public float getTiltAxis() {
		return tiltaxis;
	}


	/**
	 *  Sets the angleIncrement attribute of the XmippConf object
	 *
	 * @param  t  The new angleIncrement value
	 */
	public void setAngleIncrement(float t) {
		anginc = t;
	}


	/**
	 *  Gets the angleIncrement attribute of the XmippConf object
	 *
	 * @return    The angleIncrement value
	 */
	public float getAngleIncrement() {
		return anginc;
	}


	/**
	 *  Gets the tailleX attribute of the XmippConf object
	 *
	 * @return    The tailleX value
	 */
	public int getTailleX() {
		return tailleX;
	}


	/**
	 *  Sets the tailleX attribute of the XmippConf object
	 *
	 * @param  t  The new tailleX value
	 */
	public void setTailleX(int t) {
		tailleX = t;
	}


	/**
	 *  Sets the tailleY attribute of the XmippConf object
	 *
	 * @param  t  The new tailleY value
	 */
	public void setTailleY(int t) {
		tailleY = t;
	}


	/**
	 *  Gets the tailleY attribute of the XmippConf object
	 *
	 * @return    The tailleY value
	 */
	public int getTailleY() {
		return tailleY;
	}


	/**
	 *  Sets the rec attribute of the XmippConf object
	 *
	 * @param  r  The new rec value
	 */
	public void setRec(int r) {
		rec = r;
	}


	/**
	 *  Gets the rec attribute of the XmippConf object
	 *
	 * @return    The rec value
	 */
	public int getRec() {
		return rec;
	}


	/**
	 *  Sets the iteration attribute of the XmippConf object
	 *
	 * @param  iterations  The new iteration value
	 */
	public void setIteration(int iterations) {
		ite = iterations;
	}


	/**
	 *  Gets the iteration attribute of the XmippConf object
	 *
	 * @return    The iteration value
	 */
	public int getIteration() {
		return ite;
	}

}

