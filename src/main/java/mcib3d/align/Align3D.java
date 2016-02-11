package mcib3d.align;
import mcib3d.image3d.legacy.RealImage3D;
import mcib3d.geom.GeomTransform3D;
import ij.*;
import mcib3d.image3d.*;
import mcib3d.utils.*;
import java.io.*;

/**
 *  Description of the Class
 *
 *@author     cedric
 *@created    23/01/06
 */
public class Align3D {
	/**
	 *  Description of the Field
	 */
	protected final static int RA = 0;
	/**
	 *  Description of the Field
	 */
	protected final static int RFA = 1;
	/**
	 *  Description of the Field
	 */
	protected final static int PSPC = 2;

	private static boolean debug = false;
	private FileWriter outputfile;
	private boolean show = false;
	private boolean showmem = false;
	//private int[] rmaxx;
	//private int[] rmaxy;
	//private int[] rmaxz;
	//private RealImage3D[] images;
	//private ArrayUtil[] transfos;
	//private ArrayUtil[] transfaligndep;
	//private GeomTransform3D[] geos;
	private RealImage3D moyenne1;
	private RealImage3D moyenne2;
	private int nb_ima;
	private static float incmin = 0.1F;
	private int tmaxX;
	private int tmaxY;
	private int tmaxZ;
	private Message msg;
	private Align3DData[] imgs;


	/**
	 *  Constructor for the Align3D object
	 *
	 *@param  images      Description of the Parameter
	 *@param  transforms  Description of the Parameter
	 *@param  filename    Description of the Parameter
	 */
	public Align3D(RealImage3D[] images, ArrayUtil[] transforms, String filename) {
		if (filename != null) {
			try {
				outputfile = new FileWriter(filename);
			} catch (IOException ioe) {
				System.out.println(ioe.toString());
			}
		} else {
			outputfile = null;
		}
		nb_ima = images.length;
		moyenne1 = null;
		moyenne2 = null;
		imgs = new Align3DData[nb_ima];
		for (int i = 0; i < nb_ima; i++) {
			imgs[i] = new Align3DData(images[i]);
			if (transforms != null && transforms[i] != null) {
				imgs[i].setTransform(transforms[i]);
			}
		}
		show = false;
		tmaxX = images[0].getSizex() / 2;
		tmaxY = images[0].getSizey() / 2;
		tmaxZ = images[0].getSizez() / 2;
	}


	/**
	 *  Description of the Method
	 */
	public void activateAverageAll() {
		for (int i = 0; i < nb_ima; i++) {
			getTransform(i).putValue(6, 1.0F);
		}
	}


	/**
	 *  Description of the Method
	 *
	 *@param  img  Description of the Parameter
	 *@return      Description of the Return Value
	 */
	public boolean activateAverage(int img) {
		if (img >= nb_ima) {
			return false;
		}
		getTransform(img).putValue(6, 1.0F);
		return true;
	}


	/**
	 *  Description of the Method
	 *
	 *@param  img  Description of the Parameter
	 *@return      Description of the Return Value
	 */
	public boolean desactivateAverage(int img) {
		if (img >= nb_ima) {
			return false;
		}
		getTransform(img).putValue(6, -1.0F);
		return true;
	}


	/**
	 *  Description of the Method
	 */
	public void desactivateAverageAll() {
		for (int i = 0; i < nb_ima; i++) {
			getTransform(i).putValue(6, -1.0F);
		}
	}


	/**
	 *  Description of the Method
	 *
	 *@return    Description of the Return Value
	 */
	public RealImage3D average() {
		if (moyenne1 != null) {
			moyenne1.resetImage();
		}
		int nb = 0;
		for (int i = 0; i < nb_ima; i++) {
			if (imgs[i].getTransform().getValue(6) > 0.0F) {
				if (moyenne1 == null) {
					moyenne1 = new RealImage3D(imgs[i].getSizex(), imgs[i].getSizey(), imgs[i].getSizez());
				}
				moyenne1.add(imgs[i].getImage3D(), imgs[i].getTransform());
				nb++;
			}
		}
		if (moyenne1 != null) {
			moyenne1.divideBy((float) nb);
		}

		return moyenne1;
	}


	/**
	 *  Description of the Method
	 *
	 *@param  mode       Description of the Parameter
	 *@param  direction  Description of the Parameter
	 *@param  range      Description of the Parameter
	 *@param  inc        Description of the Parameter
	 */
	public void alignAllWithProjection(int mode, int direction, float range, float inc) {
		if (mode == RA) {
			for (int i = 1; i < nb_ima; i++) {
				alignWithProjection(0, i, direction, range, inc);
			}
		} else if (mode == RFA) {
			desactivateAverageAll();
			activateAverage(0);
			average();
			for (int i = 1; i < nb_ima; i++) {
				alignWithProjection(-1, i, direction, range, inc);
				activateAverage(i);
				average();
			}
		} else if (mode == PSPC) {
			average();
			for (int i = 1; i < nb_ima; i++) {
				alignWithProjection(-1, i, direction, range, inc);
			}
		}
	}


	/**
	 *  Description of the Method
	 *
	 *@param  index1     Description of the Parameter
	 *@param  index2     Description of the Parameter
	 *@param  direction  Description of the Parameter
	 *@param  range      Description of the Parameter
	 *@param  inc        Description of the Parameter
	 */
	public void alignWithProjection(int index1, int index2, int direction, float range, float inc) {
		RealImage3D proj1 = getImage(index1).project(direction);
		RealImage3D proj2 = getImage(index2).project(direction);
		float maxcorr = -1;
		float varmaxcorr = -1;
		for (float i = -range; i <= range; i += inc) {
			RealImage3D tmp = proj2.applyTransform(new GeomTransform3D(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, i));
			float corr = 0;
			//correlation(proj1, tmp, imgs[index1].getRmaxx(), imgs[index1].getRmaxy(), imgs[index1].getRmaxz());
			if (maxcorr < corr) {
				maxcorr = corr;
				varmaxcorr = i;
			}
		}
		if (direction == RealImage3D.XAXIS) {
			getTransform(index2).addValue(3, varmaxcorr);
		} else if (direction == RealImage3D.YAXIS) {
			getTransform(index2).addValue(4, varmaxcorr);
		} else if (direction == RealImage3D.ZAXIS) {
			getTransform(index2).addValue(5, varmaxcorr);
		}
	}


	/**
	 *  Gets the image attribute of the Align3D object
	 *
	 *@param  index  Description of the Parameter
	 *@return        The image value
	 */
	public RealImage3D getImage(int index) {
		if (index >= 0 && index < nb_ima) {
			return imgs[index].getImage3D();
		}
		if (index == -1) {
			return moyenne1;
		}
		if (index == -2) {
			return moyenne2;
		}
		return null;
	}


	/**
	 *  Gets the transform attribute of the Align3D object
	 *
	 *@param  index  Description of the Parameter
	 *@return        The transform value
	 */
	public ArrayUtil getTransform(int index) {
		if (index >= 0 && index < nb_ima) {
			return imgs[index].getTransform();
		}
		return null;
	}

}

/**
 *  Description of the Class
 *
 *@author     cedric
 *@created    23 janvier 2006
 */
class Align3DData {


	RealImage3D img;
	ArrayUtil transfo;
	//GeomTransform3D geo;
	int rmaxx, rmaxy, rmaxz;
	int sizex, sizey, sizez;


	/**
	 *  Constructor for the Align3DData object
	 *
	 *@param  image  Description of the Parameter
	 */
	public Align3DData(RealImage3D image) {
		img = image;
		transfo = new ArrayUtil(7);
		//geo = new GeomTransform();
		sizex = img.getSizex();
		sizey = img.getSizey();
		sizez = img.getSizez();

	}


	/**
	 *  Sets the image3D attribute of the Align3DData object
	 *
	 *@param  image  The new image3D value
	 */
	public void setImage3D(RealImage3D image) {
		img = image;
		sizex = img.getSizex();
		sizey = img.getSizey();
		sizez = img.getSizez();
	}


	/**
	 *  Gets the image3D attribute of the Align3DData object
	 *
	 *@return    The image3D value
	 */
	public RealImage3D getImage3D() {
		return img;
	}


	/**
	 *  Sets the transform attribute of the Align3DData object
	 *
	 *@param  transf  The new transform value
	 */
	public void setTransform(ArrayUtil transf) {
		transfo = transf;
	}


	/**
	 *  Gets the transform attribute of the Align3DData object
	 *
	 *@return    The transform value
	 */
	public ArrayUtil getTransform() {
		return transfo;
	}


	/**
	 *  Constructor for the getGeomTransform object
	 *
	 *@return    The geomTransform value
	 */
	public GeomTransform3D getGeomTransform() {
		return new GeomTransform3D(transfo);
	}


	/**
	 *  Gets the rmaxx attribute of the Align3DData object
	 *
	 *@return    The rmaxx value
	 */
	public int getRmaxx() {
		rmaxx = img.getSizex() / 2;
		rmaxx -= Math.abs(transfo.getValue(0));
		return rmaxx;
	}


	/**
	 *  Gets the rmaxy attribute of the Align3DData object
	 *
	 *@return    The rmaxy value
	 */
	public int getRmaxy() {
		rmaxy = img.getSizey() / 2;
		rmaxy -= Math.abs(transfo.getValue(1));
		return rmaxy;
	}


	/**
	 *  Gets the rmaxz attribute of the Align3DData object
	 *
	 *@return    The rmaxz value
	 */
	public int getRmaxz() {
		rmaxz = img.getSizez() / 2;
		rmaxz -= Math.abs(transfo.getValue(2));
		return rmaxz;
	}


	/**
	 *  Gets the sizex attribute of the Align3DData object
	 *
	 *@return    The sizex value
	 */
	public int getSizex() {
		return sizex;
	}


	/**
	 *  Gets the sizey attribute of the Align3DData object
	 *
	 *@return    The sizey value
	 */
	public int getSizey() {
		return sizey;
	}


	/**
	 *  Gets the sizez attribute of the Align3DData object
	 *
	 *@return    The sizez value
	 */
	public int getSizez() {
		return sizez;
	}

}

