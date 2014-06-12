package tango.plugin.filter;

import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.measure.Calibration;
import mcib3d.image3d.ImageHandler;
import mcib3d.image3d.ImageInt;
import mcib3d.image3d.processing.FastFilters3D;
import tango.dataStructure.InputImages;
import tango.parameter.ChoiceParameter;
import tango.parameter.DoubleParameter;
import tango.parameter.Parameter;

/**
 * 3D filtering
 *
 * @author Thomas BOUDIER @created feb 2008
 */
@SuppressWarnings("empty-statement")
public class Fast_Filters3D implements PreFilter, PostFilter {

    int nbcpus=1;
    String filters[] = {"Mean", "Median", "Minimum", "Maximum", "MaximumLocal", "TopHat", "OpenGray","CloseGray","Variance", "Sobel", "Adaptive"};
    int filter;
    float voisx = 2;
    float voisy = 2;
    float voisz = 2;
    boolean xy = true;
    Calibration calibration;
    boolean debug = false;
    ChoiceParameter filter_P = new ChoiceParameter("Choose Filter: ", "filter", filters, null);
    DoubleParameter voisXY_P = new DoubleParameter("VoisXY: ", "voisXY", (double) voisx, Parameter.nfDEC1);
    DoubleParameter voisZ_P = new DoubleParameter("VoisZ: ", "voisZ", (double) voisx, Parameter.nfDEC1);
    Parameter[] parameters = new Parameter[]{filter_P, voisXY_P, voisZ_P};

    // contructor for Tango
    public Fast_Filters3D() {
        filter_P.setHelp("Select the filter you want", true);
        filter_P.setHelp("Availabe filters are : <ul><li><strong>Mean</strong>, the average value in the neighborhood.</li></ul>"
                + "<ul><li><strong>Median</strong>, the median value in the neighborhood.</li></ul>"
                + "<ul><li><strong>Minimum</strong>, the minimum value in the neighborhood, can be used as a erosion for morphological filtering with white objects.</li></ul>"
                + "<ul><li><strong>Maximum</strong>, the maximum value in the neighborhood, can be used as a dilatation for morphological filtering (with white objects).</li></ul>"
                + "<ul><li><strong>Local maxima</strong>, detects the pixels that are maxima in the neighborhood, can be used as seeds for segmentation.</li></ul>"
                + "<ul><li><strong>TopHat</strong>, performs a tophat filtering, TH=image-opened(image), permits to detect bright spots by suppressing local background.</li></ul>"
                + "<ul><li><strong>OpenGray</strong>, performs a gray opening filtering, minimum followed by maximum.</li></ul>"
                + "<ul><li><strong>CloseGray</strong>, performs a gray closing filtering, maximum followed by minimum.</li></ul>"
                + "<ul><li><strong>Variance</strong>, the variance value in the neighborhood, can be used as edge detection.</li></ul>"
                + "<ul><li><strong>Sobel</strong>, a edge detection filter.</li></ul>"
                + "<ul><li><strong>Adaptive</strong> filtering, a 3D version of Nagao filter. 6 areas are defined (left, right, up, down, front, back), takes the mean of the area that has the smallest variation.</li></ul>", false);
        voisXY_P.setHelp("The radius in <em>X</em> and <em>Y</em> direction (in pixels).", true);
        voisZ_P.setHelp("The radius in <em>Z</em> direction (in pixels).", true);

    }

    @Override
    public Parameter[] getParameters() {
        return parameters;
    }

    @Override
    public void setVerbose(boolean debug) {
        this.debug = debug;
    }

    @Override
    public ImageInt runPostFilter(int currentStructureIdx, ImageInt input, InputImages images) {
        return (ImageInt) runFilter(input);
    }

    private ImageHandler runFilter(ImageHandler input) {
        if (nbcpus == 0) {
            nbcpus = 1;
        }
        filter = filter_P.getSelectedIndex();
        voisx = voisXY_P.getFloatValue(voisx);
        voisy = voisx;
        voisz = voisZ_P.getFloatValue(voisz);
        return FastFilters3D.filterImage(input, filter, voisx, voisy, voisz, nbcpus, false);
    }

    @Override
    public void setMultithread(int nbCPUs) {
        this.nbcpus=nbCPUs;
    }

    @Override
    public ImageHandler runPreFilter(int currentStructureIdx, ImageHandler input, InputImages images) {
        return runFilter(input);
    }

    @Override
    public String getHelp() {
        return "Fast filters 3D allows you to perform various 3D filtering";
    }
}
