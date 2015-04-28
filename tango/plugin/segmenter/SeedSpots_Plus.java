package tango.plugin.segmenter;

import ij.ImagePlus;
import ij.ImageStack;
import mcib3d.image3d.ImageFloat;
import mcib3d.image3d.ImageHandler;
import mcib3d.image3d.ImageInt;
import mcib3d.image3d.Segment3DSpots;
import mcib3d.image3d.processing.FastFilters3D;
//import mcib_plugins.tools.RoiManager3D_;
import tango.dataStructure.InputImages;
import tango.parameter.*;
import tango.plugin.filter.PreFilter;

/**
 * Description of the Class
 *
 * @author thomas
 * @created 16 avril 2006
 */
public class SeedSpots_Plus implements SpotSegmenter {
    // seeds

    ImagePlus seedPlus;
    ImageHandler seed3DImage;
    ImageHandler filteredSeed;
    // spots
    ImagePlus spotPlus;
    ImageStack spotStack;
    ImageHandler spot3DImage;
    // Segmentation algo
    Segment3DSpots seg;
    // segResulrs
    ImageHandler fishImage;
    ImagePlus segPlus = null;
    ImageStack segStack;
    // res
    double resXY = 0.1328;
    double resZ = 0.2;
    double radiusFixed = 0;
    double weight = 0.5;
    int local_method = 0;
    int spot_method = 0;
    int global_background = 15;
    int local_background = 65;
    // local mean
    float rad0 = 2;
    float rad1 = 4;
    float rad2 = 6;
    double we = 0.5;
    // gauss_fit
    int radmax = 10;
    double sdpc = 1.0;
    private boolean watershed = true;
    private int radiusSeeds = 2;
    // volumes (pix)
    //int volumeMin = 1;
    //int volumeMax = 1000000;
    String[] local_methods = {"Constant", "Local Mean", "Gaussian fit"};
    String[] spot_methods = {"Classical", "Maximum", "Block"};
    String[] outputs = {"Label Image", "Roi Manager 3D", "Both"};
    // DB
    PreFilterParameter algoSeeds = new PreFilterParameter("Seeds Filter", "pluginseeds", "");
    IntParameter DB_radseeds = new IntParameter("Radius for seeds (pix)", "radSeeds", radiusSeeds);
    ThresholdParameter DB_global_background = new ThresholdParameter("Global seeds threshold:", "thldHigh", "Value");
    // test with gaussian method
    ChoiceParameter DB_algos = new ChoiceParameter("Choose algo : ", "algo", spot_methods, null);
    ChoiceParameter DB_lcth = new ChoiceParameter("Choose local threshold : ", "lc", local_methods, null);
    ConditionalParameter cond = new ConditionalParameter(DB_lcth);
    ThresholdParameter DB_local_background = new ThresholdParameter("Local threshold:", "thlc", "Percentage Of Bright Pixels");
    LabelParameter DB_Label_Gaussian = new LabelParameter("Gaussian Fit");
    IntParameter DB_radmax = new IntParameter("Radius for gaussian (pix)", "radGaussianFit", radmax);
    DoubleParameter DB_sdpc = new DoubleParameter("Sigma cutoff", "sigmaGaussianFit", new Double(sdpc), Parameter.nfDEC2);
    LabelParameter DB_Label_Mean = new LabelParameter("Local Means");
    SpinnerParameter DB_rad0 = new SpinnerParameter("Radius 0", "rad0", 0, 10, 2);
    SpinnerParameter DB_rad1 = new SpinnerParameter("Radius 1", "rad1", 0, 10, 4);
    SpinnerParameter DB_rad2 = new SpinnerParameter("Radius 2", "rad1", 0, 10, 6);
    //IntParameter DB_volumeMin = new IntParameter("Volume minimum (pix)", "volMin", volumeMin);
    //IntParameter DB_volumeMax = new IntParameter("Volume maximum (pix)", "volMax", volumeMax);
    Parameter[] parameters = new Parameter[]{algoSeeds, DB_radseeds, DB_global_background, DB_algos, cond};
    private boolean debug = true;
    private int nbCPUs = 1;

    public SeedSpots_Plus() {
        //DB_algos.setRefreshOnAction();
        algoSeeds.setCompulsary(false);  
        cond.setCondition(local_methods[0], new Parameter[]{DB_local_background});
        cond.setCondition(local_methods[1], new Parameter[]{DB_rad0, DB_rad1, DB_rad2});
        cond.setCondition(local_methods[2], new Parameter[]{DB_radmax, DB_sdpc});
    }

    private void computeSeeds(int currentStructureIdx, ImageHandler input, InputImages images) {
        PreFilter filter = algoSeeds.getPlugin(nbCPUs, debug);
        if (filter != null) {
            filteredSeed = filter.runPreFilter(currentStructureIdx, input, images);
        } else {
            filteredSeed = input;
        }
        seed3DImage = FastFilters3D.filterImage(filteredSeed, FastFilters3D.MAXLOCAL, (float) radiusSeeds, (float) radiusSeeds, (float) radiusSeeds, 0, false);
    }

    private void Segmentation() {
        seg = new Segment3DSpots(this.spot3DImage, this.seed3DImage);
        seg.show = debug;
        // set parameter
        seg.setSeedsThreshold(this.global_background);
        seg.setLocalThreshold(local_background);
        seg.setWatershed(watershed);
        //seg.setVolumeMin(volumeMin);
        //seg.setVolumeMax(volumeMax);
        switch (local_method) {
            case 0:
                seg.setMethodLocal(Segment3DSpots.LOCAL_CONSTANT);
                break;
            case 1:
                seg.setMethodLocal(Segment3DSpots.LOCAL_MEAN);
                seg.setRadiusLocalMean(rad0, rad1, rad2, we);
                break;
            case 2:
                seg.setMethodLocal(Segment3DSpots.LOCAL_GAUSS);
                seg.setGaussPc(sdpc);
                seg.setGaussMaxr(radmax);
                break;
        }
        switch (spot_method) {
            case 0:
                seg.setMethodSeg(Segment3DSpots.SEG_CLASSICAL);
                break;
            case 1:
                seg.setMethodSeg(Segment3DSpots.SEG_MAX);
                break;
            case 2:
                seg.setMethodSeg(Segment3DSpots.SEG_BLOCK);
                break;
        }
        seg.segmentAll();
        // output 
        segPlus = new ImagePlus("seg", seg.getLabelImage().getImageStack());
    }

    @Override
    public Parameter[] getParameters() {
        algoSeeds.setHelp("Prefilter to fin the seeds, in the case no prefilter is selected, only local maxima will be used.", true);
        algoSeeds.setHelp("Prefilter to fin the seeds, in the case no prefilter is selected, only local maxima will be used. A robust spot detector is the Image Features Hessian", false);
        DB_radseeds.setHelp("Radius to compute max local for seeds", true);
        DB_global_background.setHelp("Threshold for seeds", true);
        //DB_volumeMax.setCompulsary(false);
        //DB_volumeMin.setHelp("Minimum volume for a spot", true);
        //DB_volumeMax.setHelp("Maximum volume for a spot (or infinity if nothing)", true);
        DB_radmax.setHelp("Radius max to compute values for gaussian fitting", true);
        DB_sdpc.setHelp("The multiplication factor with sigma value to compute the threshold based on gaussian fitting", true);
        DB_algos.setHelp("Algorithms for spot segmentation", true);
        DB_algos.setHelp("Three segmentation algorithms:\nClassical, segment all voxels with value greater than local threshold"
                + "\nMaximum, stop segmentation if voxel value greater than seed"
                + "\nBlock, stop segmentation process if one voxel grreater that seed", false);
        return parameters;
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
    public ImageInt runSpot(int currentStructureIdx, ImageHandler input, InputImages images) {
        spot3DImage = input;


        local_background = 0;
        radmax = DB_radmax.getIntValue(radmax);
        sdpc = DB_sdpc.getDoubleValue(sdpc);

        // seeds
        radiusSeeds = DB_radseeds.getIntValue(1);
        computeSeeds(currentStructureIdx, input, images);
        global_background = (DB_global_background.getThreshold(filteredSeed, images, nbCPUs, debug)).intValue();
        if (debug) {
            filteredSeed.show("Seeds");
        }

        watershed = true;
        local_method = 2;
        spot_method = DB_algos.getSelectedIndex();
        Segmentation();

        return (ImageInt) ImageHandler.wrap(segPlus);
    }

    @Override
    public ImageFloat getProbabilityMap() {
        return null;
    }

    @Override
    public String getHelp() {
        return "3D Spot segmentation";
    }
}
