package tango.plugin;

import tango.plugin.thresholder.Thresholder;
import tango.plugin.filter.PostFilter;
import tango.plugin.segmenter.NucleusSegmenter;
import tango.plugin.segmenter.SpotSegmenter;
import tango.plugin.filter.PreFilter;
import tango.plugin.sampler.Sampler;
import tango.plugin.measurement.MeasurementObject;
import tango.plugin.measurement.Measurement;
import tango.plugin.measurement.MeasurementStructure;
import ij.IJ;
import java.util.*;
import tango.gui.Core;
import tango.plugin.measurement.MeasurementObject2Object;
import tango.plugin.thresholder.ThresholderHistogram;
import tango.spatialStatistics.spatialDescriptors.SpatialDescriptor;

/**
 *
 **
 * /**
 * Copyright (C) 2012 Jean Ollion
 *
 *
 *
 * This file is part of tango
 *
 * tango is free software; you can redistribute it and/or modify it under the
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
 * @author Jean Ollion
 */
public class PluginFactory {

    static TreeMap<String, Class> nucleiSegmenters;
    static TreeMap<String, Class> spotSegmenters;
    static TreeMap<String, Class> mesurements_O;
    static TreeMap<String, Class> mesurements_S;
    static TreeMap<String, Class> mesurements_O2O;
    static TreeMap<String, Class> thresholders;
    static TreeMap<String, Class> thresholdersHisto;
    static TreeMap<String, Class> preFilters;
    static TreeMap<String, Class> postFilters;
    static TreeMap<String, Class> samplers;
    static TreeMap<String, Class> spatialDescriptors;
    static ArrayList<String> testing = new ArrayList<String>() {
        {
            add("Spotiness");
            add("EraseRegions");
            add("ShellProfile");
            add("Histogram");
            add("Granulometry");
            add("GeodesicDistanceMap");
            add("HistogramFit");
            add("LocalSegmentation");
            add("SignalNoiseQuantification");
            add("ObjectPatternAnalysis");
            add("MediatedContact");
            add("CellCycleMeasurements");
            add("VolumeFractionSpatialDistribution");
            add("ExtendedVolumeFraction");
            add("ObjectRelativeSpatialDistribution");
            add("SummarizeObject2Object");
        }
    };
    // renamed plugins -> retro-compatibility
    public static Map<String, String> correspondances = Collections.unmodifiableMap(new HashMap<String, String>() {{
        put("Volume Fraction Spatial Distribution", "Object Orientation");
    }});
    
    
    public static boolean ij5d = false;
    public static boolean ij3d = false;
    public static boolean java3d = false;

    public static boolean installComplete() {
        IJ.log("Checking installation");
        boolean complete = true;
        ClassLoader loader = ij.IJ.getClassLoader();
        try {
            loader.loadClass("mcib3d.image3d.ImageHandler");
        } catch (Exception e) {
            ij.IJ.log("mcib3d-core not installed");
            complete = false;
        }
        try {
            loader.loadClass("imageware.ImageWare");
        } catch (Exception e) {
            ij.IJ.log("ImageWare not installed");
        }
        ij5d = true;
        try {
            loader.loadClass("i5d.Image5D");
        } catch (Exception e) {
            ij.IJ.log("Image5D not installed: overlay view not available");
            ij5d = false;
        }
        java3d = true;
        try {
            loader.loadClass("javax.vecmath.Point3f");
        } catch (Exception e) {
            ij.IJ.log("Java3D not installed");
            java3d = false;
            ij3d = false;
        }
        ij3d = true;
        if (java3d) {
            try {
                loader.loadClass("ij3d.Image3DUniverse");
            } catch (Exception e) {
                ij.IJ.log("ij 3D Viewer not installed: 3D view not available");
                ij3d = false;
            }
        }
        try {
            loader.loadClass("imagescience.image.Image");
        } catch (Exception e) {
            ij.IJ.log("imagescience not installed");
            complete = false;
        }
        return complete;
    }

    public static void findPlugins() {
        try {
            if (Core.GUIMode) {
                IJ.showStatus("TANGO: initializing plugins...");
            }
            if (!Core.SPATIALSTATS) {
                testing.add("SpatialAnalysis");
            }
            nucleiSegmenters = new TreeMap<String, Class>();
            spotSegmenters = new TreeMap<String, Class>();
            mesurements_O = new TreeMap<String, Class>();
            mesurements_S = new TreeMap<String, Class>();
            mesurements_O2O = new TreeMap<String, Class>();
            thresholders = new TreeMap<String, Class>();
            thresholdersHisto = new TreeMap<String, Class>();
            preFilters = new TreeMap<String, Class>();
            postFilters = new TreeMap<String, Class>();
            samplers = new TreeMap<String, Class>();
            spatialDescriptors = new TreeMap<String, Class>();
            Hashtable<String, String> table = ij.Menus.getCommands();
            ClassLoader loader = ij.IJ.getClassLoader();
            //ClassLoader loader = new ij.io.PluginClassLoader(ij.IJ.getDirectory("plugins"), true);
            Enumeration ks = table.keys();
            while (ks.hasMoreElements()) {
                String command = (String) ks.nextElement();
                String className = table.get(command);
                testClass(command, className, loader);
            }
            //for (String className : table.values()) testClass(className, loader) ;

            // version
            

            //IJ.log("prefilters:");
            //for (String s : preFilters.keySet()) IJ.log(s);


        } catch (Exception e) {
            mcib3d.utils.exceptionPrinter.print(e, "getClasses exception...", Core.GUIMode);
        }
    }

    private static void testClass(String command, String className, ClassLoader loader) {
        if (!className.startsWith("ij.")) {
            if (className.endsWith("\")")) {
                int argStart = className.lastIndexOf("(\"");
                className = className.substring(0, argStart);
            }
            try {
                Class c = loader.loadClass(className);
                if (TangoPlugin.class.isAssignableFrom(c)) {
                    //String simpleName = c.getSimpleName();
                    String simpleName = command;
                    if (!Core.TESTING && testing.contains(c.getSimpleName())) {
                        return;
                    }
                    if (NucleusSegmenter.class.isAssignableFrom(c)) {
                        nucleiSegmenters.put(simpleName, c);
                    }
                    if (SpotSegmenter.class.isAssignableFrom(c)) {
                        spotSegmenters.put(simpleName, c);
                    }
                    if (MeasurementObject.class.isAssignableFrom(c)) {
                        mesurements_O.put(simpleName, c);
                    }
                    if (MeasurementStructure.class.isAssignableFrom(c)) {
                        mesurements_S.put(simpleName, c);
                    }
                    if (MeasurementObject2Object.class.isAssignableFrom(c)) {
                        mesurements_O2O.put(simpleName, c);
                    }
                    if (Thresholder.class.isAssignableFrom(c)) {
                        thresholders.put(simpleName, c);
                    }
                    if (ThresholderHistogram.class.isAssignableFrom(c)) {
                        thresholdersHisto.put(simpleName, c);
                    }
                    if (PreFilter.class.isAssignableFrom(c)) {
                        preFilters.put(simpleName, c);
                    }
                    if (PostFilter.class.isAssignableFrom(c)) {
                        postFilters.put(simpleName, c);
                    }
                    if (Sampler.class.isAssignableFrom(c)) {
                        samplers.put(simpleName, c);
                    }
                    if (SpatialDescriptor.class.isAssignableFrom(c)) {
                        spatialDescriptors.put(simpleName, c);
                    }
                }
            } catch (ClassNotFoundException e) {
                if (Core.GUIMode) {
                    ij.IJ.log("plugin not found: " + className);
                }
                //mcib3d.utils.exceptionPrinter.print(e, "getClass exception...", Core.GUIMode);
            } catch (NoClassDefFoundError e) {
                int dotIndex = className.indexOf('.');
                if (dotIndex >= 0) {
                    testClass(command, className.substring(dotIndex + 1), loader);
                }
            }
        }
    }

    public static NucleusSegmenter getNucleiSegmenter(String s) {
        if (s == null) {
            return null;
        }
        try {
            Object res = null;
            if (nucleiSegmenters.containsKey(s)) {
                res = nucleiSegmenters.get(s).newInstance();
            }
            if (res != null && res instanceof NucleusSegmenter) {
                return ((NucleusSegmenter) res);
            }
        } catch (Throwable e) {
            mcib3d.utils.exceptionPrinter.print(e, "getClass nucleiSegmenter exception...", Core.GUIMode);
        }
        return null;
    }

    public static Measurement getMeasurement(String s) {
        MeasurementObject m = PluginFactory.getMesurement_Object(s);
        if (m != null) {
            return m;
        } else {
            MeasurementStructure m3 = PluginFactory.getMesurement_Structure(s);
            return m3;
        }
    }

    public static MeasurementObject getMesurement_Object(String s) {
        if (s == null) {
            return null;
        }
        try {
            Object res = null;
            if (mesurements_O.containsKey(s)) {
                res = mesurements_O.get(s).newInstance();
            } 
            if (res != null && res instanceof MeasurementObject) {
                return ((MeasurementObject) res);
            }
        } catch (Throwable e) {
            mcib3d.utils.exceptionPrinter.print(e, "getClass Mesurement exception...", Core.GUIMode);
        }
        
        
        
        
        return null;
    }

    public static MeasurementStructure getMesurement_Structure(String s) {
        if (s == null) {
            return null;
        }
        try {
            Object res = null;
            if (mesurements_S.containsKey(s)) {
                res = mesurements_S.get(s).newInstance();
            }
            if (res != null && res instanceof MeasurementStructure) {
                return ((MeasurementStructure) res);
            }
        } catch (Throwable e) {
            mcib3d.utils.exceptionPrinter.print(e, "getClass Mesurement exception...", Core.GUIMode);
        }
        return null;
    }
    
    public static MeasurementObject2Object getMesurement_O2O(String s) {
        if (s == null) {
            return null;
        }
        try {
            Object res = null;
            if (mesurements_O2O.containsKey(s)) {
                res = mesurements_O2O.get(s).newInstance();
            }
            if (res != null && res instanceof MeasurementObject2Object) {
                return ((MeasurementObject2Object) res);
            }
        } catch (Throwable e) {
            mcib3d.utils.exceptionPrinter.print(e, "getClass Mesurement exception...", Core.GUIMode);
        }
        return null;
    }

    public static SpotSegmenter getSpotSegmenter(String s) {
        if (s == null) {
            return null;
        }
        try {
            Object res = null;
            if (spotSegmenters.containsKey(s)) {
                res = spotSegmenters.get(s).newInstance();
            }
            if (res != null && res instanceof SpotSegmenter) {
                return ((SpotSegmenter) res);
            }
        } catch (Throwable e) {
            mcib3d.utils.exceptionPrinter.print(e, "getClass SpotSegmenter exception...", Core.GUIMode);
        }
        return null;
    }
    
    public static NucleusSegmenter getNucleusSegmenter(String s) {
        if (s == null) {
            return null;
        }
        try {
            Object res = null;
            if (nucleiSegmenters.containsKey(s)) {
                res = nucleiSegmenters.get(s).newInstance();
            }
            if (res != null && res instanceof NucleusSegmenter) {
                return ((NucleusSegmenter) res);
            }
        } catch (Throwable e) {
            mcib3d.utils.exceptionPrinter.print(e, "getClass SpotSegmenter exception...", Core.GUIMode);
        }
        return null;
    }
    
    

    public static Thresholder getThresholder(String s) {
        if (s == null) {
            return null;
        }
        try {
            Object res = null;
            if (thresholders.containsKey(s)) {
                res = thresholders.get(s).newInstance();
            }
            if (res != null && res instanceof Thresholder) {
                return ((Thresholder) res);
            }
        } catch (Throwable e) {
            mcib3d.utils.exceptionPrinter.print(e, "getClass SpotSegmenter exception...", Core.GUIMode);
        }
        return null;
    }

    public static Thresholder getThresholderHisto(String s) {
        if (s == null) {
            return null;
        }
        try {
            Object res = null;
            if (thresholdersHisto.containsKey(s)) {
                res = thresholdersHisto.get(s).newInstance();
            }
            if (res != null && res instanceof Thresholder) {
                return ((Thresholder) res);
            }
        } catch (Throwable e) {
            mcib3d.utils.exceptionPrinter.print(e, "getClass SpotSegmenter exception...", Core.GUIMode);
        }
        return null;
    }

    public static PreFilter getPreFilter(String s) {
        if (s == null) {
            return null;
        }
        try {
            Object res = null;
            if (preFilters.containsKey(s)) {
                res = preFilters.get(s).newInstance();
            }
            if (res != null && res instanceof PreFilter) {
                return ((PreFilter) res);
            }
        } catch (Throwable e) {
            mcib3d.utils.exceptionPrinter.print(e, "getClass PreFilter exception...", Core.GUIMode);
        }
        return null;
    }

    public static PostFilter getPostFilter(String s) {
        if (s == null) {
            return null;
        }
        try {
            Object res = null;
            if (postFilters.containsKey(s)) {
                res = postFilters.get(s).newInstance();
            }
            if (res != null && res instanceof PostFilter) {
                return ((PostFilter) res);
            }
        } catch (Throwable e) {
            mcib3d.utils.exceptionPrinter.print(e, "getClass PreFilter exception...", Core.GUIMode);
        }
        return null;
    }

    public static Sampler getSampler(String s) {
        if (s == null) {
            return null;
        }
        try {
            Object res = null;
            if (samplers.containsKey(s)) {
                res = samplers.get(s).newInstance();
            }
            if (res != null && res instanceof Sampler) {
                return ((Sampler) res);
            }
        } catch (Throwable e) {
            mcib3d.utils.exceptionPrinter.print(e, "getClass PreFilter exception...", Core.GUIMode);
        }
        return null;
    }

    public static SpatialDescriptor getSpatialDescriptor(String s) {
        if (s == null) {
            return null;
        }
        try {
            Object res = null;
            if (spatialDescriptors.containsKey(s)) {
                res = spatialDescriptors.get(s).newInstance();
            }
            if (res != null && res instanceof SpatialDescriptor) {
                return ((SpatialDescriptor) res);
            }
        } catch (Throwable e) {
            mcib3d.utils.exceptionPrinter.print(e, "getClass PreFilter exception...", Core.GUIMode);
        }
        return null;
    }

    public static Set<String> getSpatialDescriptorList() {
        return spatialDescriptors.keySet();
    }

    public static Set<String> getSamplerList() {
        return samplers.keySet();
    }

    public static Set<String> getPreFilterList() {
        return preFilters.keySet();
    }

    public static Set<String> getPostFilterList() {
        return postFilters.keySet();
    }

    public static Set<String> getNucleiSegmenterList() {
        return nucleiSegmenters.keySet();
    }

    public static Set<String> getSpotSegmenterList() {
        return spotSegmenters.keySet();
    }

    public static Set<String> getMeasurementList() {
        Set<String> res = new TreeSet<String>();
        res.addAll(mesurements_O.keySet());
        res.addAll(mesurements_S.keySet());
        return res;
    }

    public static Set<String> getMeasurementStructureList() {
        return mesurements_S.keySet();
    }
    
    public static Set<String> getMeasurementO2OList() {
        return mesurements_O2O.keySet();
    }

    public static Set<String> getThresholdersList() {
        if (thresholders == null) {
            return null;
        }
        return thresholders.keySet();
    }

    public static Set<String> getThresholderHistoList() {
        if (thresholdersHisto == null) {
            return null;
        }
        return thresholdersHisto.keySet();
    }
    
    public static String  getCorrespondance(String s) {
        if (correspondances.containsKey(s)) return correspondances.get(s);
        else return s;
    }
}
