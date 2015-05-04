/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tango.util;

import ij.IJ;
import ij.ImageStack;
import ij.process.ImageProcessor;
import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import loci.common.services.DependencyException;
import loci.common.services.ServiceException;
import loci.common.services.ServiceFactory;
import loci.formats.ChannelSeparator;
import loci.formats.FormatException;
import loci.formats.meta.IMetadata;
import loci.formats.services.OMEXMLService;
import loci.plugins.util.ImageProcessorReader;
import loci.plugins.util.LociPrefs;
import mcib3d.image3d.ImageHandler;
import ome.units.quantity.Length;

/**
 *
 * @author jollion
 */
public class ImageOpener {
    
    public static ImageHandler OpenChannel(File file, int channel, int seriesNumber, int timePoint) {
            ImageHandler res = null;
            ImageProcessorReader r = new ImageProcessorReader(new ChannelSeparator(LociPrefs.makeImageReader()));
            
            ServiceFactory factory;
            IMetadata meta=null;
            try {
                factory = new ServiceFactory();
                OMEXMLService service = factory.getInstance(OMEXMLService.class);
                try {
                    meta = service.createOMEXMLMetadata();
                    r.setMetadataStore(meta);
                } catch (ServiceException ex) {
                    Logger.getLogger(ImageOpener.class.getName()).log(Level.SEVERE, null, ex);
                }
            } catch (DependencyException ex) {
                Logger.getLogger(ImageOpener.class.getName()).log(Level.SEVERE, null, ex);
            }
            
            try {
                //IJ.log("Examining file " + file.getName());
                r.setId(file.getAbsolutePath());
                
                r.setSeries(seriesNumber);
                int width = r.getSizeX();
                int height = r.getSizeY();
                int sizeZ = r.getSizeZ();
                ImageStack stack = new ImageStack(width, height);
                for (int z=0; z<sizeZ; z++) {
                    ImageProcessor ip = r.openProcessors(r.getIndex(z, channel, timePoint))[0];
                    stack.addSlice("" + (z + 1), ip);
                }
                res = ImageHandler.wrap(stack);
                res.setGraysLut();
                
                //MetadataRetrieve meta=(MetadataRetrieve)r.getMetadataStore();
                if (meta!=null) {
                    Length xy=meta.getPixelsPhysicalSizeX(0);
                    Length z=meta.getPixelsPhysicalSizeZ(0);
                    
                    if (xy!=null && z!=null) {
                        //ij.IJ.log("calibration: xy"+ xy.value()+" z:"+z.value()+ "  units:"+xy.unit().getSymbol());
                        res.setScale((Double)xy.value(), (Double)z.value(), xy.unit().getSymbol());
                    } else ij.IJ.log("no calibration found");
                }
                r.close();
                
                
                
            }
            catch (FormatException exc) {
                IJ.log("An error occurred while opering image: "+file.getName()+ " channel:"+channel+" t:"+timePoint+" s:"+seriesNumber + exc.getMessage());
            }
            catch (IOException exc) {
                IJ.log("An error occurred while opering of image: "+file.getName()+ " channel:"+channel+" t:"+timePoint+" s:"+seriesNumber + exc.getMessage());
            }
        return res;
    }
    
    public static byte[] openThumbnail(File file, int channel, int seriesNumber, int timePoint, int sizeX, int sizeY) {
            ImageProcessorReader r = new ImageProcessorReader(new ChannelSeparator(LociPrefs.makeImageReader()));
            IJ.log("Creating thumbnail for file:" +file.getName());
            try {
                //IJ.log("Examining file " + file.getName());
                r.setId(file.getAbsolutePath());
                r.setSeries(seriesNumber);
                //ImageProcessor[] ips = r.openThumbProcessors(r.getIndex(0, channel, timePoint));
                ImageHandler tmb=null;
                //boolean create = ips.length==0;
                //if (file.getName().endsWith(".zvi")) create = true;
                //if (!create) {
                //    tmb = ImageHandler.wrap(new ImagePlus("", ips[0]));
                //    if (tmb.getMax()==0) create = true;
                //}
                boolean create = true;
                if (create) { // open a few planes and getTumbnail
                    //IJ.log("no thumbnail found for file: "+file.getName());
                    int width = r.getSizeX();
                    int height = r.getSizeY();
                    int sizeZ = r.getSizeZ();
                    ImageStack stack = new ImageStack(width, height);
                    int step;
                    if (sizeZ>50) step = sizeZ/10;
                    else if (sizeZ>20) step = sizeZ/5;
                    else if (sizeZ>6) step = sizeZ/3;
                    else step = 1;
                    for (int z=0; z<sizeZ; z+=step) {
                        //IJ.log("open slice:"+z);
                        ImageProcessor ip = r.openProcessors(r.getIndex(z, channel, timePoint))[0];
                        stack.addSlice("" + (z + 1), ip);
                    }
                    tmb = ImageHandler.wrap(stack);
                }
                r.close();
                tmb.setGraysLut();
                return tmb.getThumbNail(sizeX, sizeY);
            }
            catch (FormatException exc) {
                IJ.log("An error occurred during import of image: "+file.getName()+ " channel:"+channel+" t:"+timePoint+" s:"+seriesNumber + exc.getMessage());
            }
            catch (IOException exc) {
                IJ.log("An error occurred during import of image: "+file.getName()+ " channel:"+channel+" t:"+timePoint+" s:"+seriesNumber + exc.getMessage());
            }
        return null;
    }

    public static byte[][] openThumbnails(File file, int seriesNumber, int timePoint, int sizeX, int sizeY) {
            ImageProcessorReader r = new ImageProcessorReader(new ChannelSeparator(LociPrefs.makeImageReader()));
            IJ.log("Creating thumbnails for file:" +file.getName());
            try {
                r.setId(file.getAbsolutePath());
                r.setSeries(seriesNumber);
                ImageHandler tmb=null;
                int cn = r.getSizeC();
                byte[][] tmbs = new byte[cn][];
                int width = r.getSizeX();
                int height = r.getSizeY();
                int sizeZ = r.getSizeZ();
                int step;
                if (sizeZ>50) step = sizeZ/10;
                else if (sizeZ>20) step = sizeZ/5;
                else if (sizeZ>6) step = sizeZ/3;
                else step = 1;
                for (int i = 0;i<cn; i++) {// open a few planes and getTumbnail
                    //IJ.log("no thumbnail found for file: "+file.getName());
                    ImageStack stack = new ImageStack(width, height);
                    for (int z=0; z<sizeZ; z+=step) {
                        //IJ.log("open slice:"+z);
                        ImageProcessor ip = r.openProcessors(r.getIndex(z, i, timePoint))[0];
                        stack.addSlice("" + (z + 1), ip);
                    }
                    tmb = ImageHandler.wrap(stack);
                    tmb.setGraysLut();
                    tmbs[i]=tmb.getThumbNail(sizeX, sizeY);
                }
                r.close();
                return tmbs;
            }
            catch (FormatException exc) {
                IJ.log("An error occurred during creation of thumbnails for image: "+file.getName()+" t:"+timePoint+" s:"+seriesNumber + exc.getMessage());
            }
            catch (IOException exc) {
                IJ.log("An error occurred during creation of thumbnails for image: "+file.getName()+" t:"+timePoint+" s:"+seriesNumber + exc.getMessage());
            }
        return null;
    }
    
    
        
    public static int[] getSTCNumbers(File file) {
        ImageProcessorReader r = new ImageProcessorReader(new ChannelSeparator(LociPrefs.makeImageReader()));
        try {
            //IJ.showStatus("Examining file " + name);
            r.setId(file.getAbsolutePath());
            
            int[] res = new int[3];
            res[0] = r.getSeriesCount();
            res[1] = r.getSizeT();
            res[2] = r.getSizeC();
            return res;
        }
        catch (FormatException exc) {
            IJ.log("Sorry, an error occurred: " + exc.getMessage());
        }
        catch (IOException exc) {
            IJ.log("Sorry, an error occurred: " + exc.getMessage());
        }
        return null;
    }
}
