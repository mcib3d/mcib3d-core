package tango.plugin.measurement;

//import fish.FishImage3D;
//import fish.FishObject;
import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.WindowManager;
import ij.gui.GenericDialog;
import ij.plugin.PlugIn;
import mcib3d.image3d.processing.ImageColocalizer;
import java.util.HashMap;
import mcib3d.image3d.ImageHandler;
import tango.dataStructure.InputCellImages;
import tango.dataStructure.SegmentedCellImages;
import tango.dataStructure.StructureQuantifications;
import tango.parameter.*;
import tango.plugin.measurement.MeasurementStructure;

/**
 * Description of the Class
 *
 * @author thomas
 * @created 16 avril 2006
 */
public class Jacop_ implements PlugIn, MeasurementStructure {
    // seeds

    ImagePlus APlus;
    ImageStack AStack;
    // spots
    ImagePlus BPlus;
    ImageStack BStack;
    // res
    double resXY = 0.1328;
    double resZ = 0.2;
    int seeds_threshold = 7000;
    int voxels_threshold = 0;
    //boolean segMaxLocal = true;    
    String[] methods = {"Pearson", "MM", "Li ICA", "Overlap"};
    StructureParameter structure1 = new StructureParameter("Structure 1:", "structure1", -1, true);
    BooleanParameter preFilter1 = new BooleanParameter("Use filtered image", "filtered1", true);
    PreFilterSequenceParameter preFilters1 = new PreFilterSequenceParameter("Pre-Filters", "preFilters1");
    StructureParameter structure2 = new StructureParameter("Structure 2:", "structure2", -1, false);
    BooleanParameter preFilter2 = new BooleanParameter("Use filtered image", "filtered2", true);
    PreFilterSequenceParameter preFilters2 = new PreFilterSequenceParameter("Pre-Filters", "preFilters2");
    ChoiceParameter jacop_method = new ChoiceParameter("Choose method : ", "jacopmethod", methods, methods[0]);
    ThresholdParameter threshold1 = new ThresholdParameter("Threshold channel 1:", "threshold1", "Value");
    ThresholdParameter threshold2 = new ThresholdParameter("Threshold channel 2:", "threshold", "Value");
    // conditional parameter
    HashMap<Object, Parameter[]> action = new HashMap<Object, Parameter[]>() {
        {
            put(methods[0], new Parameter[]{});
            put(methods[1], new Parameter[]{threshold1, threshold2});
            put(methods[2], new Parameter[]{});
            put(methods[3], new Parameter[]{threshold1, threshold2});
        }
    };
    ConditionalParameter cond = new ConditionalParameter(jacop_method, action);
    Parameter[] parameters = new Parameter[]{structure1, preFilter1, preFilters1, structure2, preFilter2, preFilters2, cond};
    KeyParameterStructureNumber K_pearson = new KeyParameterStructureNumber("Pearson", "pearson");
    KeyParameterStructureNumber K_M1 = new KeyParameterStructureNumber("M1", "m1");
    KeyParameterStructureNumber K_M2 = new KeyParameterStructureNumber("M2", "m2");
    KeyParameterStructureNumber K_M1Thr = new KeyParameterStructureNumber("M1Thr", "m1Thr");
    KeyParameterStructureNumber K_M2Thr = new KeyParameterStructureNumber("M2Thr", "m2Thr");
    KeyParameterStructureNumber K_ICA = new KeyParameterStructureNumber("ICA", "ica");
    KeyParameterStructureNumber K_Overlap = new KeyParameterStructureNumber("Overlap", "overlap");
    KeyParameterStructureNumber K_OverlapDen1 = new KeyParameterStructureNumber("OverlapDen1", "overlapDen1");
    KeyParameterStructureNumber K_OverlapDen2 = new KeyParameterStructureNumber("OverlapDen2", "overlapDen2");
    KeyParameterStructureNumber K_OverlapThr = new KeyParameterStructureNumber("OverlapThr", "overlapThr");
    KeyParameterStructureNumber K_OverlapDen1Thr = new KeyParameterStructureNumber("OverlapDen1Thr", "overlapDen1Thr");
    KeyParameterStructureNumber K_OverlapDen2Thr = new KeyParameterStructureNumber("OverlapDen2Thr", "overlapDen2Thr");
    KeyParameterStructureNumber[][] keys = new KeyParameterStructureNumber[][]{{K_pearson}, {K_M1, K_M2, K_M1Thr, K_M2Thr}, {K_ICA}, {K_Overlap, K_OverlapDen1, K_OverlapDen2, K_OverlapThr, K_OverlapDen1Thr, K_OverlapDen2Thr}};
    boolean verbose = true;
    int nbCPUs = 1;

    public Jacop_() {
        jacop_method.setHelp("Choose the co-localization method", false);
        threshold1.setHelp("Threshold for structure 1 to be considered as signal", false);
        threshold2.setHelp("Threshold for structure 2 to be considered as signal", false);
        jacop_method.setFireChangeOnAction();
    }

    public void run(String arg) {
        int nbima = WindowManager.getImageCount();
        String[] namesRaw = new String[nbima];
        String[] namesSeeds = new String[nbima];
        for (int i = 0; i < nbima; i++) {
            namesRaw[i] = WindowManager.getImage(i + 1).getShortTitle();
            namesSeeds[i] = WindowManager.getImage(i + 1).getShortTitle();
        }


        int Aidx = 0;
        int Bidx = 1;

        GenericDialog dia = new GenericDialog("Seeds spots");
        dia.addChoice("Image_A", namesRaw, namesRaw[Aidx]);
        dia.addChoice("Image_B", namesSeeds, namesSeeds[Bidx]);


        dia.showDialog();
        if (dia.wasOKed()) {
            Aidx = dia.getNextChoiceIndex();
            Bidx = dia.getNextChoiceIndex();


            BPlus = WindowManager.getImage(Aidx + 1);
            BStack = BPlus.getImageStack();

            APlus = WindowManager.getImage(Bidx + 1);
            AStack = APlus.getImageStack();

            this.jacop_pearson();
        }
    }

    private double jacop_pearson() {
        if (verbose) {
            IJ.log("Computing pearson");
        }
        ImageColocalizer jac = new ImageColocalizer(APlus, BPlus, verbose);
        double p = jac.getPearson();
        if (verbose) {
            IJ.log("pearson=" + p);
        }

        return p;
    }

    private double[] jacop_MM(int t1, int t2) {
        if (verbose) {
            IJ.log("Computing MM");
        }
        ImageColocalizer jac = new ImageColocalizer(APlus, BPlus, verbose);
        double[] p = jac.MM(t1, t2);
        if (verbose) {
            for (int i = 0; i < p.length; i++) {
                IJ.log("M" + i + " = " + p[i]);
            }
        }

        return p;
    }

    private double[] jacop_Overlap(int t1, int t2) {
        if (verbose) {
            IJ.log("Computing Overlap");
        }
        ImageColocalizer jac = new ImageColocalizer(APlus, BPlus, verbose);
        double[] p = jac.Overlap(t1, t2);
        if (verbose) {
            for (int i = 0; i < p.length; i++) {
                IJ.log("Overlap " + i + " = " + p[i]);
            }
        }

        return p;
    }

    private double jacop_ICQ() {
        if (verbose) {
            IJ.log("Computing ICQ");
        }
        ImageColocalizer jac = new ImageColocalizer(APlus, BPlus, verbose);
        double p = jac.getICQ();
        if (verbose) {
            IJ.log("ICQ=" + p);
        }

        return p;
    }

    @Override
    public int[] getStructures() {
        return new int[]{Math.min(structure1.getIndex(), structure2.getIndex()), Math.max(structure1.getIndex(), structure2.getIndex())};
    }

    @Override
    public Parameter[] getParameters() {
        return parameters;
    }

    @Override
    public void setMultithread(int nbCPUs) {
        this.nbCPUs = nbCPUs;
    }

    @Override
    public void setVerbose(boolean verbose) {
        this.verbose = verbose;
    }

    @Override
    public KeyParameter[] getKeys() {
        if (jacop_method.getSelectedItem().equalsIgnoreCase("pearson")) {
            return keys[0];
        } else if (jacop_method.getSelectedItem().equalsIgnoreCase("MM")) {
            return keys[1];
        } else if (jacop_method.getSelectedItem().equalsIgnoreCase("Li ICA")) {
            return keys[2];
        } else if (jacop_method.getSelectedItem().equalsIgnoreCase("Overlap")) {
            return keys[3];
        } else {
            return keys[0];
        }
    }

    @Override
    public String getHelp() {
        return "IJ's Jacop plugin for colocalization, written by F. Cordelières and S. Bolte.<br>See http://imagejdocu.tudor.lu/doku.php?id=plugin:analysis:jacop_2.0:just_another_colocalization_plugin:start";
    }

    @Override
    public void getMeasure(InputCellImages rawImages, SegmentedCellImages segmentedImages, StructureQuantifications quantif) {
        ImageHandler A = preFilter1.isSelected() ? rawImages.getFilteredImage(structure1.getIndex()) : rawImages.getImage(structure1.getIndex());
        A = preFilters1.runPreFilterSequence(structure1.getIndex(), A, rawImages, this.nbCPUs, verbose);
        ImageHandler B = preFilter2.isSelected() ? rawImages.getFilteredImage(structure2.getIndex()) : rawImages.getImage(structure2.getIndex());
        B = preFilters2.runPreFilterSequence(structure2.getIndex(), B, rawImages, this.nbCPUs, verbose);
        APlus = A.getImagePlus();
        BPlus = B.getImagePlus();

        //HashMap<String, Object> res = new HashMap();

        if (jacop_method.getSelectedItem().equalsIgnoreCase("pearson")) {
            if (keys[0][0].isSelected()) {
                quantif.setQuantificationStructureNumber(keys[0][0], jacop_pearson());
                //res.put(keys[0][0].getKey(), jacop_pearson());
            }
        } else if (jacop_method.getSelectedItem().equalsIgnoreCase("MM")) {
            double[] m = this.jacop_MM(threshold1.getThreshold(A, rawImages, nbCPUs, verbose).intValue(), threshold2.getThreshold(B, rawImages, nbCPUs, verbose).intValue());
            for (int i = 0; i < keys.length; i++) {
                if (keys[1][i].isSelected()) {
                    quantif.setQuantificationStructureNumber(keys[1][i], m[i]);
                    //res.put(keys[1][i].getKey(), m[i]);
                }
            }
        } else if (jacop_method.getSelectedItem().equalsIgnoreCase("Li ICA")) {
            if (keys[2][0].isSelected()) {
                quantif.setQuantificationStructureNumber(keys[2][0], jacop_ICQ());
                //res.put(keys[2][0].getKey(), this.jacop_ICQ());
            }
        } else if (jacop_method.getSelectedItem().equalsIgnoreCase("Overlap")) {
            double[] m = this.jacop_Overlap(threshold1.getThreshold(A, rawImages, nbCPUs, verbose).intValue(), threshold2.getThreshold(B, rawImages, nbCPUs, verbose).intValue());
            for (int i = 0; i < keys.length; i++) {
                if (keys[3][i].isSelected()) {
                    quantif.setQuantificationStructureNumber(keys[3][i], m[i]);
                    //res.put(keys[3][i].getKey(), m[i]);
                }
            }
        }
    }
}
