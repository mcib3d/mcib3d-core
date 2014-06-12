package tango.plugin.filter;

import filters.Bandpass3D;
import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.WindowManager;
import ij.measure.Calibration;
import ij.plugin.Duplicator;
import ij.process.ByteProcessor;
import ij.process.ImageConverter;
import ij.process.ImageProcessor;
import ij.process.ShortProcessor;
import imageware.Builder;
import imageware.ImageWare;
import java.util.HashMap;
import mcib3d.image3d.ImageFloat;
import mcib3d.image3d.ImageHandler;
import mcib_plugins.processing.LoG3D;
import tango.dataStructure.InputImages;
import tango.parameter.*;

/**
 *
 **
 * /**
 * Copyright (C) 2008- 2012 Thomas Boudier and others
 *
 *
 *
 * This file is part of mcib3d
 *
 * mcib3d is free software; you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 3 of the License, or (at your option) any later
 * version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 *
 * @author thomas
 */
public class Misc_3DFilters implements PreFilter {

    static final int GAUSSIAN = 0;
    static final int LOG = 1;
    static final int DENOISE = 2;
    static final int BANDPASS = 3;
    boolean debug = true;
    boolean thread = false;
    int nbCPUs = 1;
    double voisx = 2;
    double voisz = 1;
    int cs = 5;
    int mins = 2;
    int maxs = 10;
    int filter = 0;
    String[] filters = {"Gaussian 3D (IJ)", "LoG 3D (BIG)", "PureDenoise (BIG)", "BandPass (Droplet)"};
    ChoiceParameter filter_P = new ChoiceParameter("Choose Filter: ", "filter", filters, null);
    DoubleParameter voisXY_P = new DoubleParameter("VoisXY: ", "voisXY", (double) voisx, Parameter.nfDEC1);
    DoubleParameter voisZ_P = new DoubleParameter("VoisZ: ", "voisZ", (double) voisx, Parameter.nfDEC1);
    SliderParameter iteration_P = new SliderParameter("Nb Iterations (Denoise):", "iterations", 1, 10, cs);
    IntParameter mins_P = new IntParameter("Min size (BandPass):", "minsize", mins);
    IntParameter maxs_P = new IntParameter("Max size (BandPass):", "maxsize", maxs);
    HashMap<Object, Parameter[]> map = new HashMap<Object, Parameter[]>() {

        {
            put(filters[GAUSSIAN], new Parameter[]{voisXY_P, voisZ_P});
            put(filters[LOG], new Parameter[]{voisXY_P, voisZ_P});
            put(filters[DENOISE], new Parameter[]{voisXY_P, voisZ_P, iteration_P});
            put(filters[BANDPASS], new Parameter[]{mins_P, maxs_P});
        }
    };
    ConditionalParameter cond = new ConditionalParameter(filter_P, map);
    Parameter[] parameters = new Parameter[]{cond};
    static String gauss3DHelp = "<ul><li><strong>Gaussian 3D</strong> taken from ImageJ Process/Filters.</li></ul>";
    static String log3DHelp = "<ul><li><strong>LoG</strong>, laplacian of Gaussian, <br>taken from BIG http://bigwww.epfl.ch/sage/soft/LoG3D/ <br>. <br>When using this plugin, please cite : <br><br>D. Sage, F.R. Neumann, F. Hediger, S.M. Gasser, M. Unser, \"Automatic Tracking of Individual Fluorescence Particles: Application to the Study of Chromosome Dynamics,\" IEEE Transactions on Image Processing, vol. 14, no. 9, pp. 1372-1383, September 2005.<br> </li></ul>";
    static String denoiseHelp = "<ul><li><strong>PureDenoise</strong>, fluorescence denoising, <br>taken from BIG http://bigwww.epfl.ch/algorithms/denoise/ <br>Please make sure you have installed this plugin first</li></ul>";
    static String BPHelp = "<ul><li><strong>BandPass</strong>, filter pixels based on object size, <br>taken from Droplet Finder http://imagejdocu.tudor.lu/doku.php?id=plugin:analysis:droplet_counter:start <br>Please make sure you have installed this plugin first</li></ul>";
    // contructor for Tango

    public Misc_3DFilters() {
        filter_P.setHelp("Availabe filters are : " + gauss3DHelp + log3DHelp + denoiseHelp + BPHelp, true);
        voisXY_P.setHelp("The radius in <em>X</em> and <em>Y</em> direction", true);
        voisZ_P.setHelp("The radius in <em>Z</em> direction", true);
        iteration_P.setHelp("Number of iterations for PureDenoise", true);
        mins_P.setHelp("Minimum size to filter for BandPass", true);
        maxs_P.setHelp("Maximum size to filter for BandPass", true);
    }

    private ImagePlus process(ImagePlus imp) {
        if (filter == GAUSSIAN) {
            ImagePlus img2 = new Duplicator().run(imp);
            //img2.show();
            ij.plugin.GaussianBlur3D.blur(img2, voisx, voisx, voisz);
            //IJ.run("Gaussian Blur 3D...", "x=" + voisx + " y=" + voisx + " z=" + voisz);
            if (this.debug) {
                IJ.log("finished");
            }
            img2.setTitle(imp.getTitle() + "::Gauss3D");
            return img2;
        } else if (filter == LOG) {
            ImageWare in = Builder.create(imp, 3);
            LoG3D localLoG3D = new LoG3D(false);
            ImageWare res;
            if (imp.getStackSize() > 1) {
                res = localLoG3D.doLoG(in, voisx, voisx, voisz);
            } else {
                res = localLoG3D.doLoG(in, voisx, voisx);
            }
            res.invert();
            return new ImagePlus(imp.getTitle() + "::loG3D", res.buildImageStack());

            /*
             * int nb = WindowManager.getImageCount(); IJ.run("LoG 3D",
             * "sigmax=" + voisx + " sigmay=" + voisx + " sigmaz=" + voisz + "
             * displaykernel=0 volume=1"); while (WindowManager.getWindowCount()
             * == nb) { IJ.wait(100); } IJ.log("finished");
             *
             */
        } else if (filter == DENOISE) {
            if(!imp.isVisible())imp.show();
            int nb = WindowManager.getImageCount();
            IJ.run("PureDenoise ...", "parameters='1 " + cs + "' estimation='Auto Global'");            
            while (WindowManager.getWindowCount() == nb) {
                IJ.wait(100);
            }
            if (this.debug) {
                IJ.log("finished");
            }
            ImagePlus res = IJ.getImage();
            //res.hide();
            return res;
        } else if (filter == BANDPASS) {
            //ImageFloat iflo = new ImageFloat(ImageHandler.wrap(imp));
            //iflo.showDuplicate("converted float image");
            
            new ImageConverter(imp).convertToGray32();
            imp.updateAndRepaintWindow();
            imp.updateImage();
            ImageStack stack = imp.getStack();
            ImageStack convert = imp.createEmptyStack();
            ImageProcessor tmp;
            ImageProcessor tmpf;
            for (int i = 1; i <= imp.getStackSize(); i++) {
                tmp = stack.getProcessor(i);
                if (tmp instanceof ByteProcessor) {
                    tmp.setMinAndMax(0, 255);
                }
                if (tmp instanceof ShortProcessor) {
                    tmp.setMinAndMax(0, 65535);
                }
                tmpf = tmp.convertToFloat();
                convert.addSlice(tmpf);
            }
            imp.setStack(convert);
            Calibration cal = imp.getCalibration();
            double ratio = cal.pixelDepth / cal.pixelWidth;
            //IJ.log("depth=" + imp.getBitDepth() + " " + (imp.getStack().getProcessor(1) instanceof FloatProcessor));
            //IJ.run(imp, "DF_Bandpass", "maximal_feature_size=" + maxs + " minimal_feature_size=" + mins + " z/x_aspect_ratio=" + ratio);
            
            
            Bandpass3D bp3d = new Bandpass3D();
            bp3d.in_hprad = maxs;
            bp3d.in_lprad = mins;
            bp3d.in_xz_ratio = ratio;
            bp3d.in_image = convert;
            bp3d.filterit();
            ImagePlus impOut = new ImagePlus("BP_", bp3d.out_result);
            if (this.debug) {
                IJ.log("finished");
            }
            return impOut;
        }
        return null;
    }

    @Override
    public ImageHandler runPreFilter(int currentStructureIdx, ImageHandler input, InputImages images) {
        filter = filter_P.getSelectedIndex();
        voisx = voisXY_P.getDoubleValue(voisx);
        voisz = voisZ_P.getDoubleValue(voisz);
        cs = iteration_P.getValue();
        mins = mins_P.getIntValue(mins);
        maxs = maxs_P.getIntValue(maxs);

        return ImageHandler.wrap(process(input.getImagePlus()));
    }

    @Override
    public void setVerbose(boolean debug) {
        this.debug = debug;
    }

    @Override
    public void setMultithread(int nbCPUs) {
        this.nbCPUs = nbCPUs;
    }

    @Override
    public Parameter[] getParameters() {
        return parameters;
    }

    @Override
    public String getHelp() {
        switch (this.filter_P.getSelectedIndex()) {
            case 0:
                return gauss3DHelp;
            case 1:
                return log3DHelp;
            case 2:
                return denoiseHelp;
            case 3:
                return BPHelp;
        }
        return "Misc 3D Filters";
    }
}
