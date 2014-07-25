package mcib3d.ext;
import ij.*;
/**
 *  Parametres de configuration pour Imod
 *
 * @author     thomas
 * @created    30 septembre 2003
 */
public class ImodConf {

	String NomSerie;
	float angle_start, anginc, tiltangle;
	int beadsize;
	float shiftY;
	float addangles;
	int thickness;
	String Exclure;
	boolean eraser;


	/**
	 *  Constructor for the ImodConf object
	 */
	public ImodConf() {
		NomSerie = Prefs.get("imod_conf_nomserie.string", "serie");
		angle_start = (float) Prefs.get("imod_conf_angle_start.double", -50.0);
		anginc = (float) Prefs.get("imod_conf_anginc.double", 2.0);
		tiltangle = (float) Prefs.get("imod_conf_tiltangle.double", 0.0);
		beadsize = (int) Prefs.get("imod_conf_beadsize.int", 10);
		thickness = (int) Prefs.get("imod_conf_thickness.int", 100);
		Exclure = Prefs.get("imod_conf_exclude.string", "");
		shiftY = 0.0f;
		addangles = 0.0f;
		eraser = false;
	}


	/**
	 *  Constructor for the ImodConf object
	 *
	 * @param  nom    Nom de la serie
	 * @param  ad     angle de début
	 * @param  ai     incrément pour l'angle
	 * @param  tilta  angle pour l'axe de tilt
	 * @param  be     taille des billes
	 * @param  ex     images à exclure
	 * @param  er     Description of the Parameter
	 */
	public ImodConf(String nom, float ad, float ai, float tilta, int be, String ex, boolean er) {
		NomSerie = nom;
		angle_start = ad;
		anginc = ai;
		tiltangle = tilta;
		beadsize = be;
		Exclure = ex;
		thickness = 100;
		eraser = er;
	}


	/**
	 *  Constructor for the updatePrefs object
	 */
	public void updatePrefs() {
		Prefs.set("imod_conf_nomserie.string", NomSerie);
		Prefs.set("imod_conf_angle_start.double", angle_start);
		Prefs.set("imod_conf_anginc.double", anginc);
		Prefs.set("imod_conf_tiltangle.double", tiltangle);
		Prefs.set("imod_conf_beadsize.int", beadsize);
		Prefs.set("imod_conf_thickness.int", thickness);
		Prefs.set("imod_conf_exclude.string", Exclure);
	}


	/**
	 *  Gets the shiftY attribute of the ImodConf object
	 *
	 * @return    The shiftY value
	 */
	public float getShiftY() {
		return shiftY;
	}


	/**
	 *  Gets the addAngles attribute of the ImodConf object
	 *
	 * @return    The addAngles value
	 */
	public float getAddAngles() {
		return addangles;
	}


	/**
	 *  Sets the addAngles attribute of the ImodConf object
	 *
	 * @param  f  The new addAngles value
	 */
	public void setAddAngles(float f) {
		addangles = f;
	}



	/**
	 *  Gets the thickness attribute of the ImodConf object
	 *
	 * @return    The thickness value
	 */
	public int getThickness() {
		return thickness;
	}


	/**
	 *  Gets the bille attribute of the ImodConf object
	 *
	 * @return    The bille value
	 */
	public int getBille() {
		return beadsize;
	}



	/**
	 *  Sets the thickness attribute of the ImodConf object
	 *
	 * @param  i  The new thickness value
	 */
	public void setThickness(int i) {
		thickness = i;
	}


	/**
	 *  Sets the bille attribute of the ImodConf object
	 *
	 * @param  i  The new bille value
	 */
	public void setBille(int i) {
		beadsize = i;
	}


	/**
	 *  Sets the shiftY attribute of the ImodConf object
	 *
	 * @param  s  The new shiftY value
	 */
	public void setShiftY(float s) {
		shiftY = s;
	}


	/**
	 *  Sets the tiltAngle attribute of the ImodConf object
	 *
	 * @param  t  The new tiltAngle value
	 */
	public void setTiltAngle(float t) {
		tiltangle = t;
	}


	/**
	 *  Gets the tiltAngle attribute of the ImodConf object
	 *
	 * @return    The tiltAngle value
	 */
	public float getTiltAngle() {
		return tiltangle;
	}


	/**
	 *  Sets the nomSerie attribute of the ImodConf object
	 *
	 * @param  s  The new nomSerie value
	 */
	public void setNomSerie(String s) {
		NomSerie = s;
	}


	/**
	 *  Gets the nomSerie attribute of the ImodConf object
	 *
	 * @return    The nomSerie value
	 */
	public String getNomSerie() {
		return NomSerie;
	}


	/**
	 *  Sets the exclure attribute of the ImodConf object
	 *
	 * @param  s  The new exclure value
	 */
	public void setExclure(String s) {
		Exclure = s;
	}


	/**
	 *  Gets the exclure attribute of the ImodConf object
	 *
	 * @return    The exclure value
	 */
	public String getExclure() {
		return Exclure;
	}


	/**
	 *  Sets the angleDebut attribute of the ImodConf object
	 *
	 * @param  t  The new angleDebut value
	 */
	public void setAngleDebut(float t) {
		angle_start = t;
	}


	/**
	 *  Gets the angleDebut attribute of the ImodConf object
	 *
	 * @return    The angleDebut value
	 */
	public float getAngleDebut() {
		return angle_start;
	}


	/**
	 *  Sets the angleIncrement attribute of the ImodConf object
	 *
	 * @param  t  The new angleIncrement value
	 */
	public void setAngleIncrement(float t) {
		anginc = t;
	}


	/**
	 *  Gets the angleIncrement attribute of the ImodConf object
	 *
	 * @return    The angleIncrement value
	 */
	public float getAngleIncrement() {
		return anginc;
	}


	/**
	 *  Gets the eraser attribute of the ImodConf object
	 *
	 * @return    The eraser value
	 */
	public boolean getEraser() {
		return eraser;
	}


	/**
	 *  Sets the eraser attribute of the ImodConf object
	 *
	 * @param  er  The new eraser value
	 */
	public void setEraser(boolean er) {
		eraser = er;
	}

}

