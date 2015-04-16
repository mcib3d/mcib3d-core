package mcib3d.image3d.legacy;

import ij.*;
import ij.process.*;

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

import java.awt.*;
/**
 *  IntImage3D implemente la classe Image3D pour une utilisation avec des images
 *  contenant de valeurs entieres
 *
 * @author     Thomas BOUDIER & Cedric MESSAOUDI
 * @created    28 avril 2003
 */
public class ColorImage3D {
	/**
	 *  Description of the Field
	 */
	public final static String tabcolor[] = {"R", "G", "B", "C", "M", "Y", "W"};
	/**
	 *  Description of the Field
	 */
	protected final double RED[] = {1, 0, 0, 0, 1, 1, 1};
	/**
	 *  Description of the Field
	 */
	protected final double GREEN[] = {0, 1, 0, 1, 0, 1, 1};
	/**
	 *  Description of the Field
	 */
	protected final double BLUE[] = {0, 0, 1, 1, 1, 0, 1};

	/**
	 *  Description of the Field
	 */
	protected int nbdecouleur;
	/**
	 *  Description of the Field
	 */
	protected IntImage3D[] couleur;
	/**
	 *  Description of the Field
	 */
	protected int sizex = -1;
	/**
	 *  Description of the Field
	 */
	protected int sizey = -1;
	/**
	 *  Description of the Field
	 */
	protected int sizez = -1;
	private boolean debug = false;


	/**
	 *  constructeur d'un volume vide attention rien dedans maximum de couleur
	 */
	public ColorImage3D() {
		nbdecouleur = 0;
		couleur = new IntImage3D[tabcolor.length];
	}


	/**
	 *  constructeur d'un volume vide attention rien dedans nombre de couleur
	 *  fourni
	 *
	 * @param  nbcolor  nombre de couleur
	 */
	public ColorImage3D(int nbcolor) {
		nbdecouleur = nbcolor;
		if (nbdecouleur > tabcolor.length) {
			nbdecouleur = tabcolor.length;
		}
		couleur = new IntImage3D[nbdecouleur];
	}


	/**
	 *  constructeur d'un volume vide (noir)
	 *
	 * @param  sizex  taille du volume en x
	 * @param  sizey  taille du volume en y
	 * @param  sizez  taille du volume en z
	 */
	public ColorImage3D(int sizex, int sizey, int sizez) {
		nbdecouleur = tabcolor.length;
		couleur = new IntImage3D[nbdecouleur];
		this.sizex = sizex;
		this.sizey = sizey;
		this.sizez = sizez;
		for (int i = 0; i < nbdecouleur; i++) {
			couleur[i] = new IntImage3D(sizex, sizey, sizez);
		}
	}


	/**
	 *  constructeur d'un volume vide (noir)
	 *
	 * @param  sizex    taille du volume en x
	 * @param  sizey    taille du volume en y
	 * @param  sizez    taille du volume en z
	 * @param  nbcolor  nombre de couleur
	 */
	public ColorImage3D(int sizex, int sizey, int sizez, int nbcolor) {
		nbdecouleur = nbcolor;
		if (nbdecouleur > tabcolor.length) {
			nbdecouleur = tabcolor.length;
		}
		couleur = new IntImage3D[nbdecouleur];
		this.sizex = sizex;
		this.sizey = sizey;
		this.sizez = sizez;
		for (int i = 0; i < nbdecouleur; i++) {
			couleur[i] = new IntImage3D(sizex, sizey, sizez);
		}
	}


	/**
	 *  Gets the sizex attribute of the ColorImage3D object
	 *
	 * @return    The sizex value
	 */
	public int getSizex() {
		return sizex;
	}


	/**
	 *  Gets the sizey attribute of the ColorImage3D object
	 *
	 * @return    The sizey value
	 */
	public int getSizey() {
		return sizey;
	}


	/**
	 *  Gets the sizez attribute of the ColorImage3D object
	 *
	 * @return    The sizez value
	 */
	public int getSizez() {
		return sizez;
	}


	/**
	 *  Gets the nbColor attribute of the ColorImage3D object
	 *
	 * @return    The nbColor value
	 */
	public int getNbColor() {
		return nbdecouleur;
	}


	/**
	 *  ajoute une couleur a l'image3D si c'est possible
	 *
	 * @param  img  IntImage3D contenant les valeurs des pixels pour la couleur
	 * @return      Description of the Return Value
	 */
	public boolean addColor(IntImage3D img) {
		if (nbdecouleur >= tabcolor.length) {
			return false;
		}
		nbdecouleur++;
		if (this.sizex == -1 || this.sizey == -1 || this.sizez == -1) {
			this.sizex = img.getSizex();
			this.sizey = img.getSizey();
			this.sizez = img.getSizez();
		} else if ((this.sizex != img.getSizex()) || (this.sizey != img.getSizey()) || (this.sizez != img.getSizez())) {
			return false;
		}
		couleur[nbdecouleur - 1] = img;
		return true;
	}


	/**
	 *  ajoute une couleur a l'image3D si c'est possible
	 *
	 * @param  img  ImageStack contenant les valeurs des pixels pour la couleur
	 * @return      Description of the Return Value
	 */
	public boolean addColor(ImageStack img) {
		if (nbdecouleur >= tabcolor.length) {
			return false;
		}
		nbdecouleur++;
		couleur[nbdecouleur - 1] = new IntImage3D(img);
		if (this.sizex == -1 || this.sizey == -1 || this.sizez == -1) {
			this.sizex = couleur[nbdecouleur - 1].getSizex();
			this.sizey = couleur[nbdecouleur - 1].getSizey();
			this.sizez = couleur[nbdecouleur - 1].getSizez();
		} else if ((this.sizex != couleur[nbdecouleur - 1].getSizex()) || (this.sizey != couleur[nbdecouleur - 1].getSizey()) || (this.sizez != couleur[nbdecouleur - 1].getSizez())) {
			nbdecouleur--;
			return false;
		}
		return true;
	}


	/**
	 *  met une couleur donnee a l'image3D
	 *
	 * @param  img  IntImage3D contenant les valeurs des pixels pour la couleur
	 * @param  col  couleur que l'on veut assigner (les couleurs connues sont dans
	 *      ColorImage3D.tabcolor)
	 */
	public void setColor(IntImage3D img, String col) {
		for (int i = 0; i < nbdecouleur; i++) {
			if (col.compareTo(tabcolor[i]) == 0) {
				couleur[i] = img;
				if ((this.sizex != -1) && (this.sizey != -1) && (this.sizez != -1) && ((this.sizex != img.getSizex()) || (this.sizey != img.getSizey()) || (this.sizez != img.getSizez()))) {
					IJ.showMessage("Attention les images n'ont pas la meme taille");
				}
				/*
				 *  this.sizex = img.getSizex()<this.sizex ? img.getSizex() : this.sizex;
				 *  this.sizey = img.getSizey()<this.sizey ? img.getSizey() : this.sizey;
				 *  this.sizez = img.getSizez()<this.sizez ? img.getSizez() : this.sizez;
				 */
				this.sizex = couleur[i].getSizex();
				this.sizey = couleur[i].getSizey();
				this.sizez = couleur[i].getSizez();
			}
		}
	}


	/**
	 *  met une couleur donnee a l'image3D
	 *
	 * @param  img  ImageStack contenant les valeurs des pixels pour la couleur
	 * @param  col  couleur que l'on veut assigner (les couleurs connues sont dans
	 *      ColorImage3D.tabcolor)
	 */
	public void setColor(ImageStack img, String col) {
		for (int i = 0; i < nbdecouleur; i++) {
			if (col.compareTo(tabcolor[i]) == 0) {
				couleur[i] = new IntImage3D(img);
				this.sizex = couleur[i].getSizex();
				this.sizey = couleur[i].getSizey();
				this.sizez = couleur[i].getSizez();
			}
		}
	}


	/**
	 *  recupere une couleur donnee a l'image3D
	 *
	 * @param  col  couleur que l'on veut assigner (les couleurs connues sont dans
	 *      ColorImage3D.tabcolor)
	 * @return      IntImage3D contenant les valeurs des pixels de la couleur
	 */
	public IntImage3D getColor(String col) {
		for (int i = 0; i < nbdecouleur; i++) {
			if (col.compareTo(tabcolor[i]) == 0) {
				return couleur[i];
			}
		}
		return new IntImage3D(sizex, sizey, sizez);
	}


	/**
	 *  recupere l'Image3D comme une ImageStack
	 *
	 * @return    ImageStack contenant les valeurs des pixels de l'Image3D
	 */
	public ImageStack getStack() {
		ColorProcessor res = new ColorProcessor(sizex, sizey);
		ImageStack stack = new ImageStack(sizex, sizey, res.getColorModel());
		int[] col = new int[3];
		ImageProcessor ipres;

		for (int k = 0; k < sizez; k++) {
			ipres = res.createProcessor(sizex, sizey);
			for (int j = 0; j < sizey; j++) {
				for (int i = 0; i < sizex; i++) {
					for (int a = 0; a < nbdecouleur; a++) {
						if (couleur[a] != null) {
							col[0] += (int) (couleur[a].getPixel(i, j, k) * RED[a]);
							col[1] += (int) (couleur[a].getPixel(i, j, k) * GREEN[a]);
							col[2] += (int) (couleur[a].getPixel(i, j, k) * BLUE[a]);
							if (col[0] > 255) {
								col[0] = 255;
							}
							if (col[1] > 255) {
								col[1] = 255;
							}
							if (col[2] > 255) {
								col[2] = 255;
							}
						}
					}
					ipres.setColor(new Color(col[0], col[1], col[2]));
					ipres.drawPixel(i, j);
					col[0] = 0;
					col[1] = 0;
					col[2] = 0;
				}
			}
			stack.addSlice("" + (k + 1), ipres);
		}
		return stack;
	}


	/**
	 *  filtre le volume grace a un filtre median 3D
	 *
	 * @param  voisx  rayon du voisinage en x
	 * @param  voisy  rayon du voisinage en y
	 * @param  voisz  rayon du voisinage en z
	 * @return        Image3D contenant le volume filtre par un filtre median
	 */
	public ColorImage3D medianFilter(int voisx, int voisy, int voisz) {
		ColorImage3D res = new ColorImage3D(nbdecouleur);
		for (int i = 0; i < nbdecouleur; i++) {
			res.setColor((IntImage3D) couleur[i].medianFilter(voisx, voisy, voisz), tabcolor[i]);
		}
		return res;
	}


	/**
	 *  Description of the Method
	 *
	 * @param  voisx  Description of the Parameter
	 * @param  voisy  Description of the Parameter
	 * @param  voisz  Description of the Parameter
	 * @return        Description of the Return Value
	 */
	public ColorImage3D tophatFilter(int voisx, int voisy, int voisz) {
		ColorImage3D res = new ColorImage3D(nbdecouleur);
		for (int i = 0; i < nbdecouleur; i++) {
			res.setColor((IntImage3D) couleur[i].tophatFilter(voisx, voisy, voisz), tabcolor[i]);
		}
		return res;
	}



	/**
	 *  Description of the Method
	 *
	 * @param  R  Description of the Parameter
	 * @param  H  Description of the Parameter
	 * @return    Description of the Return Value
	 */
//	public ColorImage3D RH_Maxima(int R, int H) {
//		ColorImage3D res = new ColorImage3D(nbdecouleur);
//		for (int i = 0; i < nbdecouleur; i++) {
//			if (debug) {
//				IJ.write("RH_Maxima : " + tabcolor[i]);
//			}
//			res.setColor((IntImage3D) couleur[i].RH_Maxima(R, H), tabcolor[i]);
//		}
//		return res;
//	}


	/**
	 *  Description of the Method
	 *
	 * @param  seuil1  Description of the Parameter
	 * @param  seuil2  Description of the Parameter
	 * @return         Description of the Return Value
	 */
	public ColorImage3D binarisation(double seuil1, double seuil2) {
		ColorImage3D res = new ColorImage3D(nbdecouleur);
		for (int i = 0; i < nbdecouleur; i++) {
			res.setColor((IntImage3D) couleur[i].binarisation((int) seuil1, (int) seuil2), tabcolor[i]);
		}
		return res;
	}


	/**
	 *  Ouverture binaire 3D, normalement les objets sont noir sur fond blanc
	 *
	 * @param  inverse  Convention inversee, objets blancs sur fond noir
	 * @param  ite      Taille de l'ouverture
	 * @param  vx       Description of the Parameter
	 * @param  vy       Description of the Parameter
	 * @param  vz       Description of the Parameter
	 * @return          image modifiee
	 */
	public ColorImage3D ouverture3D(int vx, int vy, int vz, int ite, boolean inverse) {
		ColorImage3D res = new ColorImage3D(nbdecouleur);
		for (int i = 0; i < nbdecouleur; i++) {
			res.setColor((IntImage3D) couleur[i].opening3D(vx, vy, vz, ite, inverse), tabcolor[i]);
		}
		return res;
	}


	/**
	 *  Fermeture binaire 3D, normalement les objets sont noir sur fond blanc
	 *
	 * @param  inverse  Convention inversee, objets blancs sur fond noir
	 * @param  ite      Taille de la fermeture
	 * @param  vx       Description of the Parameter
	 * @param  vy       Description of the Parameter
	 * @param  vz       Description of the Parameter
	 * @return          image modifiee
	 */
	public ColorImage3D fermeture3D(int vx, int vy, int vz, int ite, boolean inverse) {
		ColorImage3D res = new ColorImage3D(nbdecouleur);
		for (int i = 0; i < nbdecouleur; i++) {
			res.setColor((IntImage3D) couleur[i].closing3D(vx, vy, vz, ite, inverse), tabcolor[i]);
		}
		return res;
	}


	/**
	 *  segmente le volume sur chaque couleur a partir d'un seuil
	 */
	/*
	 *  public ColorImage3D segmentation(int seuil, int voisx, int voisy, int voisz) {
	 *  ColorImage3D res = new ColorImage3D(nbdecouleur);
	 *  IJ.showProgress(0);
	 *  for (int i = 0; i < nbdecouleur; i++) {
	 *  res.setColor((IntImage3D) couleur[i].segmentation(seuil, voisx, voisy, voisz), tabcolor[i]);
	 *  IJ.showProgress((double) (i + 1) / (double) nbdecouleur);
	 *  }
	 *  return res;
	 *  }
	 */
	/**
	 *  extension de l'histogramme sur chaque couleur
	 */
	public void extendHisto() {
		for (int i = 0; i < nbdecouleur; i++) {
			couleur[i].extendHisto();
		}
	}


	/**
	 *  Description of the Method
	 *
	 * @return    Description of the Return Value
	 */
	public int[] snr3D() {
		int snr[] = new int[nbdecouleur];
		int min;
		int max;
		for (int i = 0; i < nbdecouleur; i++) {
			snr[i] = ((IntImage3D) couleur[i]).snr3D();
		}
		return snr;
	}

}

