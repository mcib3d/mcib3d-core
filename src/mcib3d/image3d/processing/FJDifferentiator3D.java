/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package mcib3d.image3d.processing;
import mcib3d.utils.ThreadRunner;
import imagescience.image.*;
/**
 *
 * @author jean
 */
public class FJDifferentiator3D {


    public Image run(final Image image, final double scale, final int xorder, final int yorder, final int zorder, int nCPUs) {


		// Initialize:

		final Dimensions dims = image.dimensions();

		final Aspects asps = image.aspects();
		if (asps.x <= 0) throw new IllegalStateException("Aspect-ratio value in x-dimension less than or equal to 0");
		if (asps.y <= 0) throw new IllegalStateException("Aspect-ratio value in y-dimension less than or equal to 0");
		if (asps.z <= 0) throw new IllegalStateException("Aspect-ratio value in z-dimension less than or equal to 0");

		final Image deriv = new FloatImage(image);

		// Differentiation in x-dimension:
		if (dims.x == 1) {
			if (xorder != 0) deriv.set(0);
		} else {
			final double xscale = scale/asps.x;
			final double[] kernel = kernel(xscale,xorder,dims.x);
			final int klenm1 = kernel.length - 1;
			deriv.axes(Axes.X);
                        final ThreadRunner tr = new ThreadRunner(0, dims.z, nCPUs);
                        for (int i  =0 ; i<tr.threads.length; i++) {
                            tr.threads[i]= new Thread(
                                new Runnable(){
                                    public void run() {
                                        final Coordinates coords = new Coordinates();
                                        final double[] ain = new double[dims.x + 2*klenm1];
                                        final double[] aout = new double[dims.x];
                                        for (coords.z=tr.ai.getAndIncrement(); coords.z<dims.z; coords.z=tr.ai.getAndIncrement()) {
                                            
                                            for (coords.y=0; coords.y<dims.y; ++coords.y) {
                                                    coords.x = -klenm1; deriv.get(coords,ain);
                                                    convolve(ain,aout,kernel, Axes.X, coords);
                                                    coords.x = 0; deriv.set(coords,aout);
                                            }
                                        }
                                    }
                                }
                            );
                        }
                        tr.startAndJoin();                     
		}

		// Differentiation in y-dimension:
		if (dims.y == 1) {
			if (yorder != 0) deriv.set(0);
		} else {
			final double yscale = scale/asps.y;
			final double[] kernel = kernel(yscale,yorder,dims.y);
			final int klenm1 = kernel.length - 1;
			deriv.axes(Axes.Y);
                        final ThreadRunner tr = new ThreadRunner(0, dims.z, nCPUs);
                        for (int i  =0 ; i<tr.threads.length; i++) {
                            tr.threads[i]= new Thread(
                                new Runnable(){
                                    public void run() {
                                        final Coordinates coords = new Coordinates();
                                        final double[] ain = new double[dims.y + 2*klenm1];
                                        final double[] aout = new double[dims.y];
                                        for (coords.z=tr.ai.getAndIncrement(); coords.z<dims.z; coords.z=tr.ai.getAndIncrement()) {
                                            
                                            for (coords.x=0; coords.x<dims.x; ++coords.x) {
                                                    coords.y = -klenm1; deriv.get(coords,ain);
                                                    convolve(ain,aout,kernel, Axes.Y, coords);
                                                    coords.y = 0; deriv.set(coords,aout);
                                            }
                                        }
                                    }
                                }
                            );
                        }
                        tr.startAndJoin();
		}

		// Differentiation in z-dimension:
		if (dims.z == 1) {
			if (zorder != 0) deriv.set(0);
		} else {
			final double zscale = scale/asps.z;
			final double[] kernel = kernel(zscale,zorder,dims.z);
			final int klenm1 = kernel.length - 1;
			deriv.axes(Axes.Z);
                        final ThreadRunner tr = new ThreadRunner(0, dims.y, nCPUs);
                        for (int i  =0 ; i<tr.threads.length; i++) {
                            tr.threads[i]= new Thread(
                                new Runnable(){
                                    public void run() {
                                        final Coordinates coords = new Coordinates();
                                        final double[] ain = new double[dims.z + 2*klenm1];
                                        final double[] aout = new double[dims.z];
                                        for (coords.y=tr.ai.getAndIncrement(); coords.y<dims.y; coords.y=tr.ai.getAndIncrement()) {
                                            
                                            for (coords.x=0; coords.x<dims.x; ++coords.x) {
                                                    coords.z = -klenm1; deriv.get(coords,ain);
                                                    convolve(ain,aout,kernel, Axes.Z, coords);
                                                    coords.z = 0; deriv.set(coords,aout);
                                            }
                                        }
                                    }
                                }
                            );
                        }
                        tr.startAndJoin();
		}
		deriv.name(image.name()+" dx"+xorder+" dy"+yorder+" dz"+zorder);

		return deriv;
	}

    private double[] kernel(final double s, final int d, final int m) {

		// Initialize:
		double r = 5;
		if (d == 0) r = 3;
		else if (d <= 2) r = 4;
		int h = (int)(s*r) + 1;
		if (h > m) h = m;
		final double[] kernel = new double[h];
		kernel[0] = (d == 0) ? 1 : 0;

		// Compute kernel:
		if (h > 1) {
			final double is2 = 1/(s*s);
			final double is4 = is2*is2;
			final double is6 = is4*is2;
			final double is8 = is6*is2;
			final double is10 = is8*is2;
			final double mis2 = -0.5*is2;
			final double sq2pi = Math.sqrt(2*Math.PI);
			switch (d) {
				case 0: {
					double integral = 0;
					for (int k=0; k<h; ++k) {
						kernel[k] = Math.exp(k*k*mis2);
						integral += kernel[k];
					}
					integral *= 2.0;
					integral -= kernel[0];
					for (int k=0; k<h; ++k)
						kernel[k] /= integral;
					break;
				}
				case 1: {
					final double c = -is2/(sq2pi*s);
					for (int k=1; k<h; ++k) {
						final double k2 = k*k;
						kernel[k] = c*k*Math.exp(k2*mis2);
					}
					break;
				}
				case 2: {
					final double c = is2/(sq2pi*s);
					for (int k=0; k<h; ++k) {
						final double k2 = k*k;
						kernel[k] = c*(k2*is2 - 1)*Math.exp(k2*mis2);
					}
					break;
				}
				case 3: {
					final double c = -is4/(sq2pi*s);
					for (int k=1; k<h; ++k) {
						final double k2 = k*k;
						kernel[k] = c*k*(k2*is2 - 3)*Math.exp(k2*mis2);
					}
					break;
				}
				case 4: {
					final double c = is4/(sq2pi*s);
					for (int k=0; k<h; ++k) {
						final double k2 = k*k;
						kernel[k] = c*(k2*k2*is4 - 6*k2*is2 + 3)*Math.exp(k2*mis2);
					}
					break;
				}
				case 5: {
					final double c = -is6/(sq2pi*s);
					for (int k=1; k<h; ++k) {
						final double k2 = k*k;
						kernel[k] = c*k*(k2*k2*is4 - 10*k2*is2 + 15)*Math.exp(k2*mis2);
					}
					break;
				}
				case 6: {
					final double c = is6/(sq2pi*s);
					for (int k=0; k<h; ++k) {
						final double k2 = k*k;
						final double k4 = k2*k2;
						kernel[k] = c*(k4*k2*is6 - 15*k4*is4 + 45*k2*is2 - 15)*Math.exp(k2*mis2);
					}
					break;
				}
				case 7: {
					final double c = -is8/(sq2pi*s);
					for (int k=1; k<h; ++k) {
						final double k2 = k*k;
						final double k4 = k2*k2;
						kernel[k] = c*k*(k4*k2*is6 - 21*k4*is4 + 105*k2*is2 - 105)*Math.exp(k2*mis2);
					}
					break;
				}
				case 8: {
					final double c = is8/(sq2pi*s);
					for (int k=0; k<h; ++k) {
						final double k2 = k*k;
						final double k4 = k2*k2;
						kernel[k] = c*(k4*k4*is8 - 28*k4*k2*is6 + 210*k4*is4 - 420*k2*is2 + 105)*Math.exp(k2*mis2);
					}
					break;
				}
				case 9: {
					final double c = -is10/(sq2pi*s);
					for (int k=1; k<h; ++k) {
						final double k2 = k*k;
						final double k4 = k2*k2;
						kernel[k] = c*k*(k4*k4*is8 - 36*k4*k2*is6 + 378*k4*is4 - 1260*k2*is2 + 945)*Math.exp(k2*mis2);
					}
					break;
				}
				case 10: {
					final double c = is10/(sq2pi*s);
					for (int k=0; k<h; ++k) {
						final double k2 = k*k;
						final double k4 = k2*k2;
						final double k6 = k4*k2;
						kernel[k] = c*(k6*k4*is10 - 45*k4*k4*is8 + 630*k6*is6 - 3150*k4*is4 + 4725*k2*is2 - 945)*Math.exp(k2*mis2);
					}
					break;
				}
			}
		}

		return kernel;
	}

    private void convolve(final double[] ain, final double[] aout, final double[] kernel, final int axe, final Coordinates coords) {

		// Mirror borders in input array:
		final int khlenm1 = kernel.length - 1;
		final int aolenm1 = aout.length - 1;
		for (int k=0, lm=khlenm1, lp=khlenm1, hm=khlenm1+aolenm1, hp=khlenm1+aolenm1; k<khlenm1; ++k) {
			ain[--lm] = ain[++lp];
			ain[++hp] = ain[--hm];
		}

		// Convolve with kernel:
		final double sign = (kernel[0] == 0) ? -1 : 1;
		for (int io=0, ii=khlenm1; io<=aolenm1; ++io, ++ii) {
			if (axe==Axes.X) coords.x=io;
                        else if (axe==Axes.Y) coords.y=io;
                        else coords.z=io;
                        double convres = ain[ii]*kernel[0];
                        for (int k=1, iimk=ii, iipk=ii; k<=khlenm1; ++k) convres += (ain[--iimk] + sign*ain[++iipk])*kernel[k];
                        aout[io] = convres;
		}
	}
}
