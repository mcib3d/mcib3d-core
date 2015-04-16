package mcib3d.align;
import ij.IJ;
import ij.ImageStack;
import ij.gui.Roi;
import ij.process.FloatProcessor;
import ij.process.ImageProcessor;
import mcib3d.geom.Voxel3D;
import mcib3d.image3d.legacy.FHTImage3D;
import mcib3d.utils.ArrayUtil;
import mcib3d.utils.Chrono;

/**
 *  Description of the Class
 *
 * @author     cedric
 * @created    24 mai 2005
 */
public class Align2D {
	/**
	 *  Description of the Field
	 */
	private Align2DData[] serie;
	/**
	 *  Description of the Field
	 */
	protected boolean show;
	/**
	 *  Description of the Field
	 */
	protected int sizex, sizey, sizez;
	/**
	 *  Description of the Field
	 */
	protected Roi selection;

	/**
	 *  Description of the Field
	 */
	public static int FILL_NONE = 1;
	/**
	 *  Description of the Field
	 */
	public static int FILL_AVG = 2;
	/**
	 *  Description of the Field
	 */
	public static int FILL_NOISE = 3;


	/**
	 *  Constructor for the Align2D object
	 *
	 * @param  stack  the stack to register
	 */
	public Align2D(ImageStack stack) {
		setStack(stack);
		show = true;
		selection = null;
	}


	/**
	 *  Constructor for the Align2D object
	 *
	 * @param  datas  the images to align
	 */
	private Align2D(Align2DData[] datas) {
		serie = datas;
		sizex = serie[0].getSizex();
		sizey = serie[0].getSizey();
		sizez = serie.length;
		selection = null;
	}


	/**
	 *  Constructor for the Align2D object
	 *
	 * @param  original  the object to copy beware the images are the same for the 2
	 *      objects there is no duplication
	 */
	public Align2D(Align2D original) {
		serie = original.serie;
		sizex = original.sizex;
		sizey = original.sizey;
		sizez = original.sizez;
		selection = original.selection;

	}


	/**
	 *  shows progress of computing in the status bar of ImageJ
	 *
	 * @param  value  true for showing (by default in constructor) false otherwise
	 */
	public void showInIJ(boolean value) {
		show = value;
	}


	/**
	 *  Sets the images to work with
	 *
	 * @param  pile  The new ImageStack on which perform alignment
	 */
	public void setStack(ImageStack pile) {
		sizez = pile.getSize();
		sizex = pile.getProcessor(1).getWidth();
		sizey = pile.getProcessor(1).getHeight();
		serie = new Align2DData[sizez];
		for (int i = 0; i < sizez; i++) {
			serie[i] = new Align2DData(pile.getProcessor(i + 1));
		}
	}


	/**
	 *  changes the ImageStack on which alignment is performed
	 *
	 * @param  pile  the new ImageStack on which the alignment is computed beware
	 *      the previous alignment is not deleted
	 */
	public void changeStack(ImageStack pile) {
		if (sizez != pile.getSize()) {
			setStack(pile);
			return;
		}
		sizex = pile.getProcessor(1).getWidth();
		sizey = pile.getProcessor(1).getHeight();
		for (int i = 0; i < sizez; i++) {
			serie[i].setImage(pile.getProcessor(i + 1), sizex, sizey);
		}

	}


	/**
	 *  gets the images on which the alignment is performed<BR>
	 *  the images could be with or without alignement beware there is always a
	 *  duplication of the images (even if not applying alignment)
	 *
	 * @param  applytransform  true for registered version
	 * @param  fill            Description of the Parameter
	 * @return                 the images aligned or not.
	 */
	public ImageStack getStack(boolean applytransform, int fill) {
		ImageStack stack = new ImageStack(sizex, sizey);
		for (int k = 0; k < sizez; k++) {
			stack.addSlice("", serie[k].getImage(applytransform, fill));
		}
		return stack;
	}


	/**
	 *  gets the images on which the alignment is performed in the given stack<BR>
	 *  the images could be with or without alignement beware there is always a
	 *  duplication of the images (even if not applying alignment)
	 *
	 * @param  stack           stack in which inserting the aligned images
	 * @param  applytransform  true for registered version
	 * @param  fill            Description of the Parameter
	 * @return                 the images aligned or not.
	 */
	public ImageStack getInStack(ImageStack stack, boolean applytransform, int fill) {
		//new ImagePlus("avant de mettre le bordel", stack).show();

		for (int i = 0; i < stack.getSize() - sizez; i++) {
			stack.deleteLastSlice();
		}
		for (int k = 0; k < sizez; k++) {
			stack.setPixels(serie[k].getImage(applytransform, fill).getPixels(), k + 1);
		}
		//new ImagePlus("apres", stack).show();
		return stack;
	}


	/**
	 *  Gets the image attribute of the Align2D object
	 *
	 * @param  img             Description of the Parameter
	 * @param  applytransform  Description of the Parameter
	 * @return                 The image value
	 */
	public ImageProcessor getImage(int img, boolean applytransform) {
		return serie[img].getImage(applytransform);
	}


	/**
	 *  Gets the image attribute of the Align2D object
	 *
	 * @param  img             Description of the Parameter
	 * @param  applytransform  Description of the Parameter
	 * @param  fill            Description of the Parameter
	 * @return                 The image value
	 */
	public ImageProcessor getImage(int img, boolean applytransform, int fill) {
		return serie[img].getImage(applytransform, fill);
	}


	/**
	 *  Gets the image attribute of the Align2D object
	 *
	 * @param  img  Description of the Parameter
	 * @return      The image value
	 */
	public ImageProcessor getImage(int img) {
		return serie[img].getImage();
	}



	/**
	 *  Description of the Method
	 *
	 * @param  img  Description of the Parameter
	 * @param  ip   Description of the Parameter
	 */
	public void changeImage(int img, ImageProcessor ip) {
		serie[img].setImage(ip);
	}


	/**
	 *  Gets the pixelValue attribute of the Align2D object
	 *
	 * @param  img  Description of the Parameter
	 * @param  x    Description of the Parameter
	 * @param  y    Description of the Parameter
	 * @return      The pixelValue value
	 */
	public float getPixelValue(int img, int x, int y) {
		return serie[img].getPixelValue(x, y);
	}


	/**
	 *  Gets the part attribute of the Align2D object
	 *
	 * @param  indexstart  Description of the Parameter
	 * @param  nbimages    Description of the Parameter
	 * @param  increment   Description of the Parameter
	 * @return             The part value
	 */
	public Align2D getPart(int indexstart, int increment, int nbimages) {
		Align2DData[] part = new Align2DData[nbimages];
		for (int i = 0; i < nbimages; i++) {
			part[i] = serie[indexstart + i * increment];
		}
		return new Align2D(part);
	}


	/**
	 *  Sets the roi attribute of the Align2D object
	 *
	 * @param  sel  The new roi value
	 */
	public void setRoi(Roi sel) {
		selection = sel;
	}


	/**
	 *  Gets the roi attribute of the Align2D object
	 *
	 * @return    The roi value
	 */
	public Roi getRoi() {
		return selection;
	}


	/**
	 *  Description of the Method
	 */
	public void reset() {
		for (int i = 0; i < sizez; i++) {
			serie[i].resetTransform();
		}
	}


	/**
	 *  Description of the Method
	 *
	 * @param  image  Description of the Parameter
	 */
	public void deleteImage(int image) {
		sizez--;
		Align2DData[] tmp = new Align2DData[sizez];
		for (int i = 0; i < image; i++) {
			tmp[i] = serie[i];
		}
		for (int i = image; i < sizez; i++) {
			tmp[i] = serie[i + 1];
		}
		serie = tmp;
	}


	/**
	 *  Gets the translation on X Axis for an image
	 *
	 * @param  img  image number in the serie ... be careful start at 0 not 1
	 * @return      the Translations on X
	 */
	public int getTx(int img) {
		return serie[img].getTx();
	}


	/**
	 *  Gets the translation on Y Axis for an image
	 *
	 * @param  img  image number in the serie ... be careful start at 0 not 1
	 * @return      the Translations on Y
	 */
	public int getTy(int img) {
		return serie[img].getTy();
	}


	/**
	 *  Gets the rotation attribute of the Align2D object
	 *
	 * @param  img  Description of the Parameter
	 * @return      The rotation value
	 */
	public double getRotation(int img) {
		return serie[img].getRotation();
	}


	/**
	 *  Gets the translation on X Axis for an image
	 *
	 * @param  img    image number in the serie ... be careful start at 0 not 1
	 * @param  value  The new tx value
	 */
	public void setTx(int img, int value) {
		serie[img].setTx(value);
	}


	/**
	 *  Gets the translation on Y Axis for an image
	 *
	 * @param  img    image number in the serie ... be careful start at 0 not 1
	 * @param  value  The new ty value
	 */
	public void setTy(int img, int value) {
		serie[img].setTy(value);
	}


	/**
	 *  Sets the translation attribute of the Align2D object
	 *
	 * @param  img  The new translation value
	 * @param  tx   The new translation value
	 * @param  ty   The new translation value
	 */
	public void setTranslation(int img, int tx, int ty) {
		serie[img].setTranslation(tx, ty);
	}


	/**
	 *  Adds a feature to the Translation attribute of the Align2D object
	 *
	 * @param  img  The feature to be added to the Translation attribute
	 * @param  tx   The feature to be added to the Translation attribute
	 * @param  ty   The feature to be added to the Translation attribute
	 */
	public void addTranslation(int img, int tx, int ty) {
		serie[img].addTranslation(tx, ty);
	}


	/**
	 *  Sets the rotation attribute of the Align2D object
	 *
	 * @param  img       The new rotation value
	 * @param  angleDeg  The new rotation value
	 */
	public void setRotation(int img, double angleDeg) {
		serie[img].setRotation(angleDeg);
	}


	/**
	 *  Adds a feature to the Rotation attribute of the Align2D object
	 *
	 * @param  img       The feature to be added to the Rotation attribute
	 * @param  angleDeg  The feature to be added to the Rotation attribute
	 */
	public void addRotation(int img, double angleDeg) {
		serie[img].setRotation(serie[img].getRotation() + angleDeg);
	}


	/**
	 *  Description of the Method
	 *
	 * @param  points  Description of the Parameter
	 * @return         Description of the Return Value
	 */
	public ArrayUtil[] XCorr(ArrayUtil[] points) {
		int nbpoints = points.length;
		int nbimages = serie.length;
		for (int i = 0; i < nbpoints; i++) {
			for (int j = 0; j < nbimages; j++) {
				points[i].putValue(j * 2, points[i].getValue(j * 2) - serie[j].getTx());
				points[i].putValue(j * 2 + 1, points[i].getValue(j * 2 + 1) - serie[j].getTy());
			}
		}
		XCorr();
		for (int i = 0; i < nbpoints; i++) {
			for (int j = 0; j < nbimages; j++) {
				points[i].putValue(j * 2, points[i].getValue(j * 2) + serie[j].getTx());
				points[i].putValue(j * 2 + 1, points[i].getValue(j * 2 + 1) + serie[j].getTy());
			}
		}

		return points;
	}


	/**
	 *  crosscorrelation of all images
	 */
	public void XCorr() {
		int rayon = sizex < sizey ? sizex / 2 : sizey / 2;

		//ImageProcessor tmp = insert2(serie[0].getImage(true), rayon, true);
		int oldtx = serie[0].getTx();
		int oldty = serie[0].getTy();
		ImageProcessor ip = serie[0].getImage(true);
		ip.setRoi(selection);
		FHTImage3D fht1 = new FHTImage3D(ip.crop());
		FHTImage3D fht2;
		int totx = 0;
		int toty = 0;
		System.out.println("Xcorr...");
		Chrono time = new Chrono(sizez - 1);
		time.start();
		for (int i = 1; i < sizez; i++) {
			//if (show) {
			//	IJ.showStatus("" + i + "/" + (sizez - 1));
			//}
			//tmp = insert2(serie[i].getImage(true), rayon, true);
			ip = serie[i].getImage(true);
			ip.setRoi(selection);
			fht2 = new FHTImage3D(ip.crop());
			Voxel3D max = FHTImage3D.getMaxCorrelation(fht1, fht2);
			fht1 = fht2;
			int tmpx = oldtx;
			int tmpy = oldty;
			oldtx = serie[i].getTx();
			oldty = serie[i].getTy();
			serie[i].addTranslation(serie[i - 1].getTx() + max.getX() - tmpx, serie[i - 1].getTy() + max.getY() - tmpy);
			totx += serie[i].getTx();
			toty += serie[i].getTy();
			time.stop();
			String strtime = "Xcorr : " + 100 * (i + 1) / sizez + "% remaining " + time.remainString(i);
			System.out.print("\r                                                                 \r" + strtime + " total :" + time.totalTimeEstimateString(i) + "           ");
			if (show) {
				IJ.showStatus(strtime);
			}
			//System.out.print("\r" + 100 * (i + 1) / sizez + "% \t" + time.delayString() + "\t (" + time.remainString(i) + ")                ");
		}

		totx /= sizez;
		toty /= sizez;
		for (int i = 0; i < sizez; i++) {
			serie[i].addTranslation(-totx, -toty);
			IJ.write(" final image " + i + " : tx ="
					 + serie[i].getTx() + ", ty="
					 + serie[i].getTy());
		}
		System.out.print("\n");
		IJ.freeMemory();
	}


	/**
	 *  Description of the Method
	 *
	 * @param  G      Description of the Parameter
	 * @param  rayon  Description of the Parameter
	 * @param  up     Description of the Parameter
	 * @return        Description of the Return Value
	 */
	public ImageProcessor insert2(ImageProcessor G, int rayon, boolean up) {
		int x;
		int y;
		int larg = G.getWidth();
		int haut = G.getHeight();
		int r0 = rayon + 10;
		int x0;
		int y0;
		int X0;
		int Y0;
		float pix;
		double coeff;
		double val;
		double rapport;

		double kl = Math.log(larg) / Math.log(2);
		double kh = Math.log(haut) / Math.log(2);
		int kkl = (int) (Math.pow(2.0, (int) (kl + 0.99)));
		int kkh = (int) (Math.pow(2.0, (int) (kh + 0.99)));

		int size = Math.max(kkl, kkh);
		if (!up) {
			size /= 2;
		}
		FloatProcessor res = new FloatProcessor(size, size);
		int centre = size / 2;
		double r = 0.0;

		float moy = Moyenne(G, r0);

		for (x = 0; x < size; x++) {
			for (y = 0; y < size; y++) {
				r = Math.sqrt((x - centre) * (x - centre) + (y - centre) * (y - centre));
				if (r >= r0) {
					res.putPixelValue(x, y, moy);
				} else if (r >= rayon) {
					x0 = centre + (int) (((float) (rayon) / (float) r) * (x - centre));
					y0 = centre + (int) (((float) (rayon) / (float) r) * (y - centre));
					X0 = x0 - (size - larg) / 2;
					if (X0 >= larg) {
						X0 = larg - 1;
					}
					Y0 = y0 - (size - haut) / 2;
					if (Y0 >= haut) {
						Y0 = haut - 1;
					}
					pix = G.getPixelValue(X0, Y0);

					rapport = (r - (rayon)) / (r0 - rayon);
					coeff = Math.cos(1.57 * rapport);
					val = coeff * pix + (1.0 - coeff) * moy;
					res.putPixelValue(x, y, (int) val);
				} else {
					res.putPixelValue(x, y, G.getPixelValue(x - (size - larg) / 2, y - (size - haut) / 2));
				}
			}
		}
		return res;
	}


	/**
	 *  computes the mean value of the image given
	 *
	 * @param  G      image on which compute the mean
	 * @param  rayon  radius around the center to take into account
	 * @return        mean value in a radius
	 */
	private float Moyenne(ImageProcessor G, double rayon) {
		double sum = 0.0;
		int nb = 0;
		int l = G.getWidth();
		int h = G.getHeight();
		double dist;
		for (int x = (int) (l / 2 - rayon); x <= (int) (l / 2 + rayon); x++) {
			for (int y = (int) (h / 2 - rayon); y <= (int) (h / 2 + rayon); y++) {
				dist = Math.sqrt(Math.pow((x - l / 2), 2) + Math.pow((y - h / 2), 2));
				if (dist < rayon) {
					sum += G.getPixelValue(x, y);
					nb++;
				}
			}
		}
		return (float) (sum / nb);
	}


	/**
	 *  Description of the Method
	 *
	 * @param  points  Description of the Parameter
	 * @return         Description of the Return Value
	 */
	public ArrayUtil[] meanSquare(ArrayUtil[] points) {
		int nbpoints = points.length;
		int nbimages = serie.length;
		int nbrot = serie.length;
		int nbtr = serie.length;
		if (nbpoints < 3) {
			IJ.write("not enough points for mean square method ! call basic translation computing method instead.");
			return basicTranslation(points);
		}

		double totx = 0;
		double toty = 0;
		double totrz = 0;
		double[][] result = new double[nbimages][3];
		for (int j = 0; j < nbimages - 1; j++) {
			int jj = j * 2;
			double moyx = 0.0F;
			double moyy = 0.0F;
			double moytx = 0.0F;
			double moyty = 0.0F;
			int nb = 0;
			for (int i = 0; i < nbpoints; i++) {
				if (!Double.isNaN(points[i].getValue(jj + 0) + points[i].getValue(jj + 2))) {
					moyx += points[i].getValue(jj + 0);
					moyy += points[i].getValue(jj + 1);
					moytx += points[i].getValue(jj + 2);
					moyty += points[i].getValue(jj + 3);
					nb++;
				}
			}
			if (nb == 0) {
				nb = 1;
			}
			moyx /= (float) nb;
			moyy /= (float) nb;
			moytx /= (float) nb;
			moyty /= (float) nb;
			float k11 = 0.0F;
			float k12 = 0.0F;
			float k21 = 0.0F;
			float k22 = 0.0F;
			for (int i = 0; i < nbpoints; i++) {
				if (!Double.isNaN(points[i].getValue(jj + 0) + points[i].getValue(jj + 2))) {
					k11 += (points[i].getValue(jj + 0) - moyx) * (points[i].getValue(jj + 2)
							 - moytx);
					k12 += (points[i].getValue(jj + 1) - moyy) * (points[i].getValue(jj + 3)
							 - moyty);
					k21 += (points[i].getValue(jj + 2) - moytx) * (points[i].getValue(jj + 1)
							 - moyy);
					k22 += (points[i].getValue(jj + 0) - moyx) * (points[i].getValue(jj + 3)
							 - moyty);
				}
			}
			double k1 = k11 + k12;
			double k2 = k21 - k22;
			double cosa = k1 / (float) Math.sqrt((double) (k1 * k1 + k2 * k2));
			double sina = -k2 / (float) Math.sqrt((double) (k1 * k1 + k2 * k2));
			double a = Math.toDegrees(Math.acos((double) cosa));
			double a2 =  Math.toDegrees(Math.asin((double) sina));
			double dx = moytx - (moyx * cosa - moyy * sina);
			double dy = moyty - (moyx * sina + moyy * cosa);
			IJ.log("fonction \u00b2 image " + (j + 1) + " dx=" + dx + ", dy=" + dy + ", r(depuis cos)="
					 + a + " ou (depuis sin)" + a2);
			if (Double.isNaN(dx)) {
				dx = 0;
			}
			if (Double.isNaN(dy)) {
				dy = 0;
			}
			if (Double.isNaN(a2)) {
				a2 = 0;
			}

			result[j + 1][0] = result[j][0] - dx;
			result[j + 1][1] = result[j][1] - dy;
			result[j + 1][2] = result[j][2] - (int) a2;
			//tx[j + 1] -= (int) (dx + 0.5f);
			//ty[j + 1] -= (int) (dy + 0.5f);
			//rz[j + 1] = a2;
			totx += result[j + 1][0];
			toty += result[j + 1][1];
			totrz += result[j + 1][2];
			IJ.log("valeur rz =" + result[j + 1][2]);
		}

		totx /= nbimages;
		toty /= nbimages;
		totrz /= nbimages;
		//IJ.write("valeur totrz =" + totrz);
		for (int i = 0; i < sizez; i++) {
			result[i][0] -= totx;
			result[i][1] -= toty;
			result[i][2] -= totrz;
			//IJ.write("substract totrz valeur rz =" + result[i][2]);
			serie[i].addTranslation(Math.round(result[i][0]), Math.round(result[i][1]));
			serie[i].addRotation(result[i][2]);
		}
		double centerX = (sizex - 1) / 2.0;
		double centerY = (sizey - 1) / 2.0;
		for (int i = 0; i < nbpoints; i++) {
			for (int j = 0; j < nbimages; j++) {
				double angleRadians = Math.toRadians(result[j][2]);
				double ca = Math.cos(angleRadians);
				double sa = Math.sin(angleRadians);
				double x = points[i].getValue(j * 2) - centerX;
				double y = points[i].getValue(j * 2 + 1) - centerY;
				double xx = x * ca - y * sa + centerX + Math.round(result[j][0]);
				double yy = x * sa + y * ca + centerY + Math.round(result[j][1]);
				points[i].putValue(j * 2, (float) xx);
				points[i].putValue(j * 2 + 1, (float) yy);
				//IJ.write("fonction \u00b2 image " + j + " dx=" + result[j][0] + ", dy=" + result[j][1] + ", r(depuis sin)" + result[j][2]);

			}
		}
		return points;
	}


	/**
	 *  Description of the Method
	 *
	 * @param  points  Description of the Parameter
	 * @return         Description of the Return Value
	 */
	public ArrayUtil[] basicTranslation(ArrayUtil[] points) {
		int nbpoints = points.length;
		int nbimages = serie.length;

		double totx = 0;
		double toty = 0;
		double[][] result = new double[nbimages][3];
		for (int j = 0; j < nbimages - 1; j++) {
			int jj = j * 2;
			double moyx = 0.0F;
			double moyy = 0.0F;
			int nb = 0;
			for (int i = 0; i < nbpoints; i++) {
				if (!Double.isNaN(points[i].getValue(jj + 0) + points[i].getValue(jj + 2))) {
					moyx += points[i].getValue(jj + 2) - points[i].getValue(jj + 0);
					moyy += points[i].getValue(jj + 3) - points[i].getValue(jj + 1);
					nb++;
				}
			}
			if (nb == 0) {
				nb = 1;
			}
			moyx /= (float) nb;
			moyy /= (float) nb;
			IJ.log("simple translation : dx=" + moyx + ", dy=" + moyy);
			result[j + 1][0] = result[j][0] - moyx;
			result[j + 1][1] = result[j][1] - moyy;
			result[j + 1][2] = 0;
			totx += result[j + 1][0];
			toty += result[j + 1][1];
		}
		totx /= nbimages;
		toty /= nbimages;
		for (int i = 0; i < sizez; i++) {
			result[i][0] -= totx;
			result[i][1] -= toty;
			serie[i].addTranslation(Math.round(result[i][0]), Math.round(result[i][1]));
		}

		for (int i = 0; i < nbpoints; i++) {
			for (int j = 0; j < nbimages; j++) {
				points[i].putValue(j * 2, points[i].getValue(j * 2) + Math.round(result[i][0]));
				points[i].putValue(j * 2 + 1, points[i].getValue(j * 2 + 1) + Math.round(result[i][1]));
			}
		}
		return points;
	}


	/**
	 *  Description of the Method
	 *
	 * @param  points  Description of the Parameter
	 * @return         Description of the Return Value
	 */
	public ArrayUtil[] centerFeatures(ArrayUtil[] points) {
		int nbpoints = points.length;
		int nbimages = serie.length;
		int centerx = sizex / 2;
		int centery = sizey / 2;
		for (int j = 0; j < nbimages; j++) {
			int jj = j * 2;
			double moyx = 0.0F;
			double moyy = 0.0F;
			int nb = 0;
			//compute the barycentre
			for (int i = 0; i < nbpoints; i++) {
				if (!Double.isNaN(points[i].getValue(jj + 0))) {
					moyx += points[i].getValue(jj + 0);
					moyy += points[i].getValue(jj + 1);
					nb++;
				}
			}
			//if there is a barycentre moves it to the center of image
			if (nb != 0) {
				moyx /= (double) nb;
				moyy /= (double) nb;
				double tx = centerx - moyx;
				double ty = centery - moyy;
				IJ.log("center feature on image #" + j + " (" + tx + ", " + ty + ")");
				serie[j].addTranslation(Math.round(tx), Math.round(ty));
				for (int i = 0; i < nbpoints; i++) {
					if (!Double.isNaN(points[i].getValue(jj + 0))) {
						points[i].addValue(jj + 0, Math.round(tx));
						points[i].addValue(jj + 1, Math.round(ty));

					}
				}
			}
		}

		return points;
	}


	/**
	 *  Description of the Method
	 *
	 * @param  nbimg  Description of the Parameter
	 * @return        Description of the Return Value
	 */
	public double average(int nbimg) {
		double sum = 0;
		for (int j = 0; j < sizey; j++) {
			for (int i = 0; i < sizex; i++) {
				sum += serie[nbimg].getPixelValue(i, j);
			}
		}
		return sum / (sizex * sizey);
	}


	/**
	 *  Description of the Method
	 *
	 * @param  avg  Description of the Parameter
	 * @param  img  Description of the Parameter
	 * @return      Description of the Return Value
	 */
	public double sigma(int img, double avg) {
		double total = 0.0;
		for (int j = 0; j < sizey; j++) {
			for (int i = 0; i < sizex; i++) {
				double val = (double) (serie[img].getPixelValue(i, j) - avg);
				total += val * val;
			}
		}

		total /= (double) (sizex * sizey);
		double sigma = Math.sqrt(total);
		return sigma;
	}


	/**
	 *  computes the crosscorrelation between 2 images using FFT
	 *
	 * @param  img1  first image (reference)
	 * @param  img2  second image
	 * @return       Voxel3D containing the shifts in X,Y and Z(should be zero!) and
	 *      the pixel value is the correlation value
	 */
	public static Voxel3D crossCorrelation(ImageProcessor img1, ImageProcessor img2) {
		return FHTImage3D.getMaxCorrelation(img1, img2);
	}


	/**
	 *  compute the crosscorrelation between 2 images with a maximum translation of
	 *  (maxX,maxY)
	 *
	 * @param  img1  reference image
	 * @param  img2  image to translate
	 * @param  maxX  maximum translation on X axis
	 * @param  maxY  maximum translation on Y axis
	 * @return       translation obtaining best correlation score
	 */
	public static Voxel3D crossCorrelation(ImageProcessor img1, ImageProcessor img2, int maxX, int maxY) {
		return FHTImage3D.getMaxCorrelation(img1, img2, maxX, maxY);
	}


	/**
	 *  compute the rotation between 2 images using the Fourier Transform.<BR>
	 *  use the power spectrum to be free of translation<BR>
	 *  then convert to polar coordinate to compute rotation with cross-correlation
	 *
	 * @param  image1          reference image
	 * @param  image2          image to rotate
	 * @param  range           the rotation computed will be between [-range,+range[
	 * @param  angleprecision  the precision wanted (1 for degrees precision, 0.1
	 *      for decidegrees precision)
	 * @return                 angle in degrees to rotate the second image for it to
	 *      coincide with the first
	 */
	public static double computeRotation2ImagesFHT(ImageProcessor image1, ImageProcessor image2, double range, double angleprecision) {
		//System.out.println("FHT " + image1.getWidth());
		ImageProcessor img1 = toPolar(new FHTImage3D(image1).center().getPowerSpectrum(true).getProcessor(1), range, angleprecision);
		ImageProcessor img2 = toPolar(new FHTImage3D(image2).center().getPowerSpectrum(true).getProcessor(1), range, angleprecision);
		Voxel3D corr = FHTImage3D.getMaxCorrelation(img1, img2);
		//IJ.write("max (" + corr.getX() + ", " + corr.getY() + ", " + corr.getZ() + ") =" + corr.getValue());
		return -corr.getX() * angleprecision;
		//ImageProcessor img1 = new FHTImage3D(image1).center().getPowerSpectrum(true).getProcessor(1);
		//ImageProcessor img2 = new FHTImage3D(image2).center().getPowerSpectrum(true).getProcessor(1);
		//return computeRotation2Images(img1, img2, range, angleprecision);
	}


	/**
	 *  convert image to the polar coordinate system <BR>
	 *  all angles are not taken, only those between +/- range
	 *
	 * @param  img             image to convert (not modified)
	 * @param  range           the rotation taken for new image will be between
	 *      [-range,+range[
	 * @param  angleprecision  the precision wanted (1 for degrees precision, 0.1
	 *      for decidegrees precision)
	 * @return                 converted image
	 */
	public static ImageProcessor toPolar(ImageProcessor img, double range, double angleprecision) {
		int width = (int) (2 * range / angleprecision);
		int height = Math.min(img.getWidth(), img.getHeight()) / 2;
		int centerx = img.getWidth() / 2;
		int centery = img.getHeight() / 2;
		int x;
		int y;
		ImageProcessor result = img.createProcessor(width, height);
		for (int i = 0; i < width; i++) {
			for (int j = 0; j < height; j++) {
				x = (int) (j * Math.cos(Math.toRadians(i - range)) + centerx);
				y = (int) (j * Math.sin(Math.toRadians(i - range)) + centery);
				result.putPixelValue(i, j, img.getPixelValue(x, y));
			}
		}
		//new ImagePlus("polar representation", result).show();
		return result;
	}


	/**
	 *  compute the rotation between 2 images in space domain<BR>
	 *  the second image is rotated in the allowed range and the rotation obtaining
	 *  the best correlation coefficient is returned
	 *
	 * @param  img1            Description of the Parameter
	 * @param  img2            Description of the Parameter
	 * @param  range           the rotation computed will be between [-range,+range[
	 * @param  angleprecision  the precision wanted (1 for degrees precision, 0.1
	 *      for decidegrees precision)
	 * @return                 angle in degrees to rotate the second image for it to
	 *      coincide with the first
	 */
	public static double computeRotation2Images(ImageProcessor img1, ImageProcessor img2, double range, double angleprecision) {
		double bestangle = 0;
		double bestscore = -Double.MAX_VALUE;
		double rxmin = (img1.getWidth() - 1.0) / 2;
		double rymin = (img2.getHeight() - 1.0) / 2;
		double nbcalc = 2 * range / angleprecision;
		double compt = 0;
		for (double angle = -range; angle <= range; angle += angleprecision) {
			ImageProcessor tmp = img2.duplicate();
			tmp.rotate(-angle);
			double score = correlation(img1, tmp, rxmin, rymin);
			if (score > bestscore) {
				bestscore = score;
				bestangle = angle;
			}
			compt++;
			IJ.showProgress(compt / nbcalc);
		}
		return bestangle;
	}


	/**
	 *  compute the rotation for the images. the rotations will be computed between
	 *  2 consecutive images in Fourier Space
	 *
	 * @param  range           the rotations computed will be between
	 *      [-range,+range[
	 * @param  angleprecision  the precision wanted (1 for degrees precision, 0.1
	 *      for decidegrees precision)
	 */
	public void computeRotation(double range, double angleprecision) {
		computeRotation(true, range, angleprecision);
	}


	/**
	 *  compute the rotation for the images. the rotations will be computed between
	 *  2 consecutive images
	 *
	 * @param  polar           true if computation in fourier space false for
	 *      computation in real space
	 * @param  range           the rotations computed will be between
	 *      [-range,+range[
	 * @param  angleprecision  the precision wanted (1 for degrees precision, 0.1
	 *      for decidegrees precision)
	 */
	public void computeRotation(boolean polar, double range, double angleprecision) {
		ImageProcessor img1 = serie[0].getImage(true);
		img1.setRoi(selection);
		img1 = img1.crop();
		//ImageProcessor img1 = new FHTImage3D(serie[0].getImage(true)).center().getPowerSpectrum(true).getProcessor(1);
		//ImageProcessor img1 = null;
		ImageProcessor img2 = null;
		double totangles = 0;
		System.out.println("computing rotation with new method..." + polar);
		Chrono time = new Chrono(sizez - 1);
		time.start();
		for (int i = 1; i < sizez; i++) {
			img2 = serie[i].getImage(true);
			img2.setRoi(selection);
			img2 = img2.crop();
			double bestangle = 0;
			//img2 = new FHTImage3D(serie[i].getImage(true)).center().getPowerSpectrum(true).getProcessor(1);
			if (polar) {
				bestangle = computeRotation2ImagesFHT(img1, img2, range, angleprecision);
			} else {
				bestangle = computeRotation2Images(img1, img2, range, angleprecision);
			}
			totangles += bestangle;
			serie[i].addRotation(totangles);
			IJ.write("adding to " + i + " a rotation of " + totangles + " rotation to precedent image " + bestangle);
			img1 = img2;
			time.stop();
			String strtime = "rotation : " + 100 * i / (sizez - 1) + "% remaining " + time.remainString(i);
			System.out.print("\r                                                                 \r" + strtime + " total :" + time.totalTimeEstimateString(i) + "           ");
			IJ.showStatus(strtime);

		}
		totangles /= sizez - 1;
		IJ.write("adding to all a rotation of " + (-totangles));
		for (int i = 0; i < sizez; i++) {
			serie[i].addRotation(-totangles);
		}
	}


	/**
	 *  Description of the Method
	 *
	 * @param  range           Description of the Parameter
	 * @param  angleprecision  Description of the Parameter
	 */
	public void computeRotationOld(double range, double angleprecision) {
		ImageProcessor img1 = serie[0].getImageAdding(0, 0, 0);
		ImageProcessor img2 = null;
		double rx1 = (serie[0].getSizex() - 1.0) / 2 - Math.abs(serie[0].getTx());
		double ry1 = (serie[0].getSizey() - 1.0) / 2 - Math.abs(serie[0].getTy());
		double totangles = 0;
		System.out.println("computing rotation...");
		int nbcalc = (int) ((sizez - 1) * (range * 2 + 1) * angleprecision);
		int cc = 0;
		Chrono time = new Chrono(nbcalc);
		time.start();
		for (int i = 1; i < sizez; i++) {
			double bestangle = 0;
			double bestscore = -Double.MAX_VALUE;
			double rx2 = (serie[i].getSizex() - 1.0) / 2 - Math.abs(serie[i].getTx());
			double ry2 = (serie[i].getSizey() - 1.0) / 2 - Math.abs(serie[i].getTy());
			double rxmin = (rx1 < rx2) ? rx1 : rx2;
			double rymin = (ry1 < ry2) ? ry1 : ry2;
			for (double angle = -range; angle <= range; angle += angleprecision) {
				img2 = serie[i].getImageAdding(0, 0, angle);
				//double score = score(img1, img2);
				double score = correlation(img1, img2, rxmin, rymin);
				if (score > bestscore) {
					bestscore = score;
					bestangle = angle;
				}
				cc++;
				time.stop();
				String strtime = "rotation : " + 100 * cc / nbcalc + "% remaining " + time.remainString(cc);
				System.out.print("\r                                                                 \r" + strtime + " total :" + time.totalTimeEstimateString(cc) + "           ");

			}
			serie[i].addRotation(bestangle);
			IJ.write("adding to " + i + " a rotation of " + bestangle);
			totangles += bestangle;
			img1 = serie[i].getImage(true);
			rx1 = rx2;
			ry1 = ry2;
		}
		totangles /= sizez - 1;
		IJ.write("adding to all a rotation of " + (-totangles));
		for (int i = 0; i < sizez; i++) {
			serie[i].addRotation(-totangles);
		}
	}


	/**
	 *  compute score between 2 images as (image1-image2)^2
	 *
	 * @param  img1  first image
	 * @param  img2  second image
	 * @return       score
	 */
	private double score(ImageProcessor img1, ImageProcessor img2) {
		double score = 0;
		double diff;
		for (int i = 0; i < img1.getWidth(); i++) {
			for (int j = 0; j < img1.getHeight(); j++) {
				diff = img1.getPixelValue(i, j) - img2.getPixelValue(i, j);
				score += diff * diff;
			}
		}
		return score;
	}


	/**
	 *  compute the correlation between 2 images
	 *
	 * @param  img1       first image
	 * @param  img2       second image
	 * @param  rayonmaxx  maximum radius on X axis to take pixels into account
	 * @param  rayonmaxy  maximum radius on Y axis to take pixels into account
	 * @return            correlation score
	 */
	public static double correlation(ImageProcessor img1, ImageProcessor img2, double rayonmaxx, double rayonmaxy) {
		double sum = 0.0;
		double sum2nd = 0.0;
		double avg1rst = 0.0;
		double avg2nd = 0.0;
		double sum1rst = 0.0;
		int sizex = img1.getWidth();
		int sizey = img1.getHeight();
		//int sizez = first.getSizez();
		double centerx = (sizex - 1) / 2.0;
		double centery = (sizey - 1) / 2.0;
		//float centerz = first.getCenterZ();
		int nb = 0;
		//for (int k = 0; k < sizex; k++) {
		//float dz = (float) k - centerz;
		for (int j = 0; j < sizey; j++) {
			double dy = j - centery;
			for (int i = 0; i < sizex; i++) {
				double dx = i - centerx;
				double d
						 = (dx * dx / (rayonmaxx * rayonmaxx)
						 + dy * dy / (rayonmaxy * rayonmaxy)
						);
				d = Math.sqrt(d);
				if (d <= 1.0) {
					avg1rst += img1.getPixelValue(i, j);
					avg2nd += img2.getPixelValue(i, j);
					nb++;
				}
			}
		}
		//}
		avg1rst /= nb;
		avg2nd /= nb;
		//for (int k = 0; k < sizex; k++) {
		//float dz = (float) k - centerz;
		for (int j = 0; j < sizey; j++) {
			double dy = j - centery;
			for (int i = 0; i < sizex; i++) {
				double dx = i - centerx;
				double d
						 = (dx * dx / (rayonmaxx * rayonmaxx)
						 + dy * dy / (rayonmaxy * rayonmaxy)
						);
				d = Math.sqrt(d);
				if (d <= 1.0) {
					double p1 = img1.getPixelValue(i, j) - avg1rst;
					double p2 = img2.getPixelValue(i, j) - avg2nd;
					sum2nd += p2 * p2;
					sum1rst += p1 * p1;
					sum += p2 * p1;
				}
			}
		}
		//}
		return (float) (sum / Math.sqrt(sum1rst * sum2nd));
	}



	/**
	 *  average images using a selection
	 *
	 * @param  using  array containing true for all images that are added to the
	 *      average
	 * @return        average image
	 */
	public ImageProcessor average(boolean[] using) {
		ImageProcessor result = serie[0].getImage().createProcessor(sizex, sizey);
		int nb = 0;
		for (int n = 0; n < serie.length; n++) {
			if (using[n]) {
				for (int i = 0; i < sizex; i++) {
					for (int j = 0; j < sizey; j++) {
						result.putPixelValue(i, j, result.getPixelValue(i, j) + serie[0].getImage().getPixelValue(i, j));
					}
				}

				nb++;
			}
		}
		for (int i = 0; i < sizex; i++) {
			for (int j = 0; j < sizey; j++) {
				result.putPixelValue(i, j, result.getPixelValue(i, j) / nb);
			}
		}
		return result;
	}


	/**
	 *  align all images using RFA procedure (Penczek 1992)
	 *
	 * @param  rangeangle      Description of the Parameter
	 * @param  incrementangle  Description of the Parameter
	 * @return                 Description of the Return Value
	 */
	public ImageProcessor doRFA(double rangeangle, double incrementangle) {
		boolean[] using = new boolean[serie.length];
		for (int i = 0; i < serie.length; i++) {
			using[i] = false;
		}
		using[0] = true;
		for (int i = 1; i < serie.length; i++) {
			ImageProcessor avg = average(using);
			align2D(avg, i, rangeangle, incrementangle);
			using[i] = true;
		}
		return average(using);
	}


	/**
	 *  Description of the Method
	 *
	 * @param  img1            Description of the Parameter
	 * @param  index2          Description of the Parameter
	 * @param  rangeangle      Description of the Parameter
	 * @param  incrementangle  Description of the Parameter
	 */
	public void align2D(ImageProcessor img1, int index2, double rangeangle, double incrementangle) {
		double score = correlation(img1, getImage(index2, true), sizex, sizey);
		Voxel3D max = FHTImage3D.getMaxCorrelation(img1, getImage(index2, true));
		serie[index2].addTranslation(max.getX(), max.getY());

	}

}

/**
 *  Description of the Class
 *
 * @author     cedric
 * @created    24 mai 2005
 */
class Align2DData {


	/**
	 *  Description of the Field
	 */
	public static int FILL_NONE = 1;
	/**
	 *  Description of the Field
	 */
	public static int FILL_AVG = 2;
	/**
	 *  Description of the Field
	 */
	public static int FILL_NOISE = 3;
	int tx;
	int ty;
	double rz;
	ImageProcessor img;
	int sizex;
	int sizey;
	//double cosa;
	//double sina;
	double average = Double.NaN;
	double sigma = Double.NaN;



	/**
	 *  Constructor for the Align2DData object
	 *
	 * @param  image  Description of the Parameter
	 */
	public Align2DData(ImageProcessor image) {
		setImage(image);
		resetTransform();
	}


	/**
	 *  Sets the image attribute of the Align2DData object
	 *
	 * @param  image  The new image value
	 */
	public void setImage(ImageProcessor image) {
		setImage(image, image.getWidth(), image.getHeight());
		average = Double.NaN;
		sigma = Double.NaN;
	}


	/**
	 *  Sets the image attribute of the Align2DData object
	 *
	 * @param  image  The new image value
	 * @param  sx     The new image value
	 * @param  sy     The new image value
	 */
	public void setImage(ImageProcessor image, int sx, int sy) {
		img = image;
		sizex = sx;
		sizey = sy;
		average = Double.NaN;
		sigma = Double.NaN;
	}


	/**
	 *  Gets the image attribute of the Align2DData object
	 *
	 * @return    The image value
	 */
	public ImageProcessor getImage() {
		return img;
	}


	/**
	 *  Gets the image attribute of the Align2DData object
	 *
	 * @param  applyTransform  Description of the Parameter
	 * @param  fill            Description of the Parameter
	 * @return                 The image value
	 */
	public ImageProcessor getImage(boolean applyTransform, int fill) {
		if (!applyTransform) {
			return img.duplicate();
		}
		return getImage(fill, tx, ty, rz);
	}


	/**
	 *  Gets the image attribute of the Align2DData object
	 *
	 * @param  applyTransform  Description of the Parameter
	 * @return                 The image value
	 */
	public ImageProcessor getImage(boolean applyTransform) {
		return getImage(applyTransform, FILL_AVG);
	}


	/**
	 *  Gets the imageAdding attribute of the Align2DData object
	 *
	 * @param  dx        Description of the Parameter
	 * @param  dy        Description of the Parameter
	 * @param  rotation  Description of the Parameter
	 * @return           The imageAdding value
	 */
	public ImageProcessor getImageAdding(double dx, double dy, double rotation) {
		return getImageAdding(FILL_AVG, dx, dy, rotation);
	}


	/**
	 *  Gets the imageAdding attribute of the Align2DData object
	 *
	 * @param  fill      Description of the Parameter
	 * @param  dx        Description of the Parameter
	 * @param  dy        Description of the Parameter
	 * @param  rotation  Description of the Parameter
	 * @return           The imageAdding value
	 */
	public ImageProcessor getImageAdding(int fill, double dx, double dy, double rotation) {
		return getImage(fill, tx + dx, ty + dy, rz + rotation);
	}


	/**
	 *  Gets the image with the given transform
	 *
	 * @param  dx    translation on X axis
	 * @param  dy    translation on Y axis
	 * @param  rot   rotation in degrees in the plane
	 * @param  fill  what to do with non defined pixels actually does not work
	 * @return       The image aligned
	 */
	// public ImageProcessor getImage(int mode, int fill, double dx, double dy, double rot) {
	// fill = FILL_AVG;
	// ImageProcessor result = img.createProcessor(sizex, sizey);
	// double cx = (sizex - 1) / 2.0;
	// double cy = (sizey - 1) / 2.0;
	// double aux = Math.PI / Math.sqrt(dx * dx + dy * dy);
//
	// double cos = Math.cos(Math.toRadians(rot));
	// double sin = Math.sin(Math.toRadians(rot));
	// for (int i = 0; i < sizex; i++) {
	// for (int j = 0; j < sizey; j++) {
//
	// double x = i - cx;
	// double y = j - cy;
	// double xx = x;
	// double yy = y;
	// if (mode == Align2D.TRANSLATION || mode == Align2D.TRANSLROT) {
	// x -= dx;
	// y -= dy;
	// }
	// if (mode == Align2D.TRANSLROT || mode == Align2D.ROTTRANSL || mode == Align2D.ROTATION) {
	// xx = cos * x - sin * y;
	// yy = sin * x + cos * y;
	// }
	// if (mode == Align2D.ROTTRANSL) {
	// xx -= dx;
	// yy -= dy;
	// }
	// xx += cx;
	// yy += cy;
	// if (xx >= 0 && xx < sizex && yy >= 0 && yy < sizey) {
	// result.putPixelValue(i, j, img.getInterpolatedPixel(xx, yy));
	// } else if (fill == FILL_AVG) {
	// result.putPixelValue(i, j, average());
	// } else if (fill == FILL_COS) {
	// double xxx = xx;
	// double yyy = yy;
	// if (xx < 0) {
	// xxx = 0;
	// }
	// if (xx >= sizex) {
	// xxx = sizex - 1;
	// }
	// if (yy < 0) {
	// yyy = 0;
	// }
	// if (yy >= sizey) {
	// yyy = sizey - 1;
	// }
	// double dist = Math.sqrt((xx - xxx) * (xx - xxx) + (yy - yyy) * (yy - yyy));
	// double tmp = Math.cos(dist * aux);
	// result.putPixelValue(i, j, img.getInterpolatedPixel(xxx, yyy) * tmp);
	// }
	// }
	// }
	// return result;
	// }


	/**
	 *  Gets the image with the given transform
	 *
	 * @param  dx    translation on X axis
	 * @param  dy    translation on Y axis
	 * @param  rot   rotation in degrees in the plane
	 * @param  fill  what to do with non defined pixels actually does not work
	 * @return       The image aligned
	 */
	public ImageProcessor getImage(int fill, double dx, double dy, double rot) {
		//fill = FILL_AVG;
		if (dx == 0 && dy == 0 && rot == 0) {
			return img.duplicate();
		}
		ImageProcessor result = img.createProcessor(sizex, sizey);
		double cx = 0;
		double cy = 0;
		double aux = 0;
		double cos = 0;
		double sin = 0;
		if (rot != 0) {
			cx = (sizex - 1) / 2.0;
			cy = (sizey - 1) / 2.0;
			aux = Math.PI / Math.sqrt(dx * dx + dy * dy);
			cos = Math.cos(Math.toRadians(rot));
			sin = Math.sin(Math.toRadians(rot));
		}
		double x;
		double y;
		double xx;
		double yy;
		double range = 1.96 * average() / Math.sqrt(sizex * sizey);
		//double range = sigma();
		//System.out.println("random poisson " + average() + "+/-" + range + "vs random " + average() + "+/-" + sigma());
		for (int i = 0; i < sizex; i++) {
			for (int j = 0; j < sizey; j++) {
				xx = i;
				yy = j;
				if (rot != 0) {
					x = i - cx;
					y = j - cy;
					xx = cos * x - sin * y;
					yy = sin * x + cos * y;
				}
				if (dx != 0 || dy != 0) {
					xx -= dx;
					yy -= dy;
				}
				if (rot != 0) {
					xx += cx;
					yy += cy;
				}
				double pix = 0;
				int ix0 = (int) xx;
				int iy0 = (int) yy;
				double dx0 = xx - ix0;
				double dy0 = yy - iy0;
				if (ix0 >= 0 && ix0 < sizex && iy0 >= 0 && iy0 < sizey) {
					//en bas a gauche
					if (ix0 == sizex - 1 || iy0 == sizey - 1) {
						pix = img.getPixelValue(ix0, iy0);
					} else {

						pix += img.getPixelValue(ix0, iy0) * (1 - dx0 - dy0 + dx0 * dy0);

						//en bas a droite
						if (ix0 + 1 < sizex && iy0 >= 0) {
							pix += img.getPixelValue(ix0 + 1, iy0) * (dx0 - dx0 * dy0);
						}
						//en haut a gauche
						if (ix0 >= 0 && iy0 + 1 < sizey) {
							pix += img.getPixelValue(ix0, iy0 + 1) * (dy0 - dx0 * dy0);
						}
						//en haut a droite
						if (ix0 + 1 < sizex && iy0 + 1 < sizey) {
							pix += img.getPixelValue(ix0 + 1, iy0 + 1) * (dx0 * dy0);
						}
					}
					result.putPixelValue(i, j, pix);

					//if (xx >= 0 && xx < sizex && yy >= 0 && yy < sizey) {
					//result.putPixelValue(i, j, img.getInterpolatedPixel(xx, yy));
				} else if (fill == FILL_AVG) {
					result.putPixelValue(i, j, average());
				} else if (fill == FILL_NOISE) {
					pix = average() + Math.random() * (2 * range) - range;
					//double pix = average() + (Math.random() * (2 * sigma()) - sigma());
					result.putPixelValue(i, j, pix);
				}
			}
		}
		average = Double.NaN;
		sigma = Double.NaN;
		return result;
	}


	/**
	 *  Gets the pixelValue attribute of the Align2DData object
	 *
	 * @param  x  Description of the Parameter
	 * @param  y  Description of the Parameter
	 * @return    The pixelValue value
	 */
	public float getPixelValue(int x, int y) {
		return img.getPixelValue(x, y);
	}



	/**
	 *  Sets the translation attribute of the Align2DData object
	 *
	 * @param  dx  The new translation value
	 * @param  dy  The new translation value
	 */
	public void setTranslation(int dx, int dy) {
		tx = dx;
		ty = dy;
	}


	/**
	 *  Adds a feature to the Translation attribute of the Align2DData object
	 *
	 * @param  dx  The feature to be added to the Translation attribute
	 * @param  dy  The feature to be added to the Translation attribute
	 */
	public void addTranslation(double dx, double dy) {
		tx += dx;
		ty += dy;
	}



	/**
	 *  Gets the tx attribute of the Align2DData object
	 *
	 * @return    The tx value
	 */
	public int getTx() {
		return tx;
	}


	/**
	 *  Gets the ty attribute of the Align2DData object
	 *
	 * @return    The ty value
	 */
	public int getTy() {
		return ty;
	}


	/**
	 *  Gets the translation on X Axis for an image
	 *
	 * @param  value  The new tx value
	 */
	public void setTx(int value) {
		tx = value;
	}


	/**
	 *  Gets the translation on Y Axis for an image
	 *
	 * @param  value  The new ty value
	 */
	public void setTy(int value) {
		ty = value;
	}


	/**
	 *  Sets the rotation attribute of the Align2DData object
	 *
	 * @param  angleDeg  The new rotation value
	 */
	public void setRotation(double angleDeg) {
		rz = angleDeg;
		//cosa = Math.cos(Math.toRadians(rz));
		//sina = Math.sin(Math.toRadians(rz));
	}


	/**
	 *  Adds a feature to the Rotation attribute of the Align2DData object
	 *
	 * @param  angleDeg  The feature to be added to the Rotation attribute
	 */
	public void addRotation(double angleDeg) {
		double radrz = Math.toRadians(rz);
		double radna = Math.toRadians(angleDeg);
		double sinna = Math.sin(radna);
		double cosna = Math.cos(radna);
		rz = Math.toDegrees(Math.asin(Math.cos(radrz) * sinna + Math.sin(radrz) * cosna));
		tx = (int) Math.round(tx * cosna - ty * sinna);
		ty = (int) Math.round(tx * sinna + ty * cosna);
		//rz += angleDeg;
	}


	/**
	 *  Gets the rotation attribute of the Align2DData object
	 *
	 * @return    The rotation value
	 */
	public double getRotation() {
		return rz;
	}


	/**
	 *  Description of the Method
	 */
	public void resetTransform() {
		tx = 0;
		ty = 0;
		setRotation(0);
	}



	/**
	 *  Description of the Method
	 *
	 * @return    Description of the Return Value
	 */
	public double average() {
		if (!Double.isNaN(average)) {
			return average;
		}
		double sum = 0;
		for (int j = 0; j < sizey; j++) {
			for (int i = 0; i < sizex; i++) {
				sum += img.getPixelValue(i, j);
			}
		}
		average = sum / (sizex * sizey);
		return average;
	}


	/**
	 *  Description of the Method
	 *
	 * @return    Description of the Return Value
	 */
	public double sigma() {
		if (!Double.isNaN(sigma)) {
			return sigma;
		}
		this.average();
		double sum = 0;
		for (int j = 0; j < sizey; j++) {
			for (int i = 0; i < sizex; i++) {
				double pix = img.getPixelValue(i, j) - average;
				sum += pix * pix;
			}
		}
		sigma = Math.sqrt(sum / (sizex * sizey));
		return sigma;
	}


	/**
	 *  Description of the Method
	 *
	 * @param  angle         Description of the Parameter
	 * @param  defaultvalue  Description of the Parameter
	 * @param  dx            Description of the Parameter
	 * @param  dy            Description of the Parameter
	 * @return               Description of the Return Value
	 */
	public ImageProcessor transform(int dx, int dy, double angle, double defaultvalue) {
		//System.out.println("call for Align2DData transform");
		ImageProcessor result = img.createProcessor(sizex, sizey);
		double centerX = (sizex - 1) / 2.0 - tx;
		double centerY = (sizey - 1) / 2.0 - ty;

		double angleRadians = Math.toRadians(-rz);
		double ca = Math.cos(angleRadians);
		double sa = Math.sin(angleRadians);

		double tmp1 = centerY * sa - centerX * ca;
		double tmp2 = -centerX * sa - centerY * ca;
		double tmp3;
		double tmp4;
		double xs;
		double ys;

		double xlimit = sizex - 1.0;
		double xlimit2 = sizex - 1.001;
		double ylimit = sizey - 1.0;
		double ylimit2 = sizey - 1.001;

		for (int y = 0; y < sizey; y++) {
			tmp3 = tmp1 - y * sa + centerX;
			tmp4 = tmp2 + y * ca + centerY;
			for (int x = 0; x < sizex; x++) {
				xs = x * ca + tmp3;
				ys = x * sa + tmp4;
				if ((xs >= -0.01) && (xs < sizex) && (ys >= -0.01) && (ys < sizey)) {
					if (xs < 0.0) {
						xs = 0.0;
					}
					if (xs >= xlimit) {
						xs = xlimit2;
					}
					if (ys < 0.0) {
						ys = 0.0;
					}
					if (ys >= ylimit) {
						ys = ylimit2;
					}
					result.putPixelValue(x, y, img.getInterpolatedPixel(xs, ys));

				} else {
					result.putPixelValue(x, y, defaultvalue);
				}
			}

		}
		return result;
	}


	/**
	 *  Gets the sizex attribute of the Align2DData object
	 *
	 * @return    The sizex value
	 */
	public int getSizex() {
		return sizex;
	}


	/**
	 *  Gets the sizey attribute of the Align2DData object
	 *
	 * @return    The sizey value
	 */
	public int getSizey() {
		return sizey;
	}
}

