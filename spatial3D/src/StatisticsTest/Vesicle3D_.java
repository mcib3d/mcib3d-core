package StatisticsTest;

import ij.*;
import ij.process.*;
import ij.gui.*;

import java.awt.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Random;

import mcib3d.geom.Object3D;
import mcib3d.geom.Object3DVoxels;
import mcib3d.geom.ObjectCreator3D;
import mcib3d.geom.Voxel3D;
//import mcib3d.image3d.ImageByte;
import mcib3d.image3d.ImageHandler;
import mcib3d.utils.ArrayUtil;
//import ij.measure.*;
import ij.plugin.PlugIn;
//import ij.plugin.Selection;
import ij.plugin.TextReader;

/** This plugin implements the File/Import/XY Coordinates command. It reads a
    two column text file, such as those created by File/Save As/XY Coordinates,
    as a polygon ROI. The ROI is displayed in the current image or, if the image
    is too small, in a new blank image.
*/
public class Vesicle3D_ implements PlugIn 
{
	public ImagePlus imgPlus, imgModify;
	public ImageHandler imgOrigine;
	public ArrayList<Voxel3D> objects;
	private Roi[] arrayRois;
    public void run(String arg) 
    {
    	readFileV2();
    	convexHull(imgModify);
    	toBoundingBox(imgModify);
    	createMask(imgModify);
    	
        /*convexHull(imgPlus);
        imgPlus.show();
        IJ.log("img initial");
        //createMask(imgPlus);
        toBoundingBox(imgPlus);
        createMask(imgPlus);
        */
    }
    
    public void readFileV2()
    {
    	int tx = 64;
        int ty = 64;
        int tz = 64;
        VesicleInCell ves = new VesicleInCell(tx, ty, tz);
        ArrayList<Object3DVoxels> listVesiclesResult = ves.createListVesicle();
        int sizeVes = listVesiclesResult.size();
        float[] x = new float[sizeVes];
        float[] y = new float[sizeVes];
        boolean allIntegers = true;
        double length = 0.0;
        for (int i=0; i<sizeVes; i++) {
            x[i] = (float)listVesiclesResult.get(i).getCenterX();
            y[i] = (float)listVesiclesResult.get(i).getCenterY();
            if ((int)x[i]!=x[i] || (int)y[i]!=y[i])
                allIntegers = false;
            if (i>0) {
                double dx = x[i] - x[i-1];
                double dy = y[i] - y[i-1];
                length += Math.sqrt(dx*dx+dy*dy);
            }
        }
        
        Roi roi = null;
        int type = length/x.length>10?Roi.POLYGON:Roi.FREEROI;
        if (allIntegers)
            roi = new PolygonRoi(Roi.toIntR(x), Roi.toIntR(y), sizeVes, type);
        else
            roi = new PolygonRoi(x, y, sizeVes, type);
        Rectangle r = roi.getBoundingRect();
        //ImageHandler img01_contours = new ImageByte("Contours_1", tx, ty, tz);
        imgPlus = WindowManager.getCurrentImage();
        if (imgPlus==null || imgPlus.getWidth()<r.x+r.width || imgPlus.getHeight()<r.y+r.height) {
            new ImagePlus("Vesicle3D", new ByteProcessor(Math.abs(r.x)+r.width+10, Math.abs(r.y)+r.height+10)).show();
            imgPlus = WindowManager.getCurrentImage();
        }
        IJ.log(" size of img plus: "+imgPlus.getWidth() + " height: " + imgPlus.getHeight());
        ImageStack imgStack = new ImageStack(imgPlus.getWidth(), imgPlus.getHeight());
        ShortProcessor pro = new ShortProcessor(imgPlus.getWidth(), imgPlus.getHeight());
        imgStack.addSlice("", pro);
        
        if (imgPlus!=null)
        {
        	
        	objects = new ArrayList<Voxel3D>();
        	for (int i=0; i<sizeVes; i++) 
        	{
        		Voxel3D o = new Voxel3D(x[i], y[i], 0, 255);
        		objects.add(o);
        		imgStack.setVoxel(o.getRoundX(), o.getRoundY(), o.getRoundZ(), 255);
        		IJ.log(" parameters: x: "+x[i]+" y: "+y[i]);
        			
        	}
        	imgPlus.setStack("image rebuild", imgStack);
        	imgPlus.show();
        	imgModify = imgPlus;
        	imgModify.setRoi(roi);
        	imgModify.show();
        }
    }
    
    private void transferProperties(Roi roi1, Roi roi2) {
        if (roi1==null || roi2==null)
            return;
        roi2.setStrokeColor(roi1.getStrokeColor());
        if (roi1.getStroke()!=null)
            roi2.setStroke(roi1.getStroke());
        roi2.setDrawOffset(roi1.getDrawOffset());
    }
    public void convexHull(ImagePlus imp) {
        Roi roi = imp.getRoi();
        int type = roi!=null?roi.getType():-1;
        if (!(type==Roi.FREEROI||type==Roi.TRACED_ROI||type==Roi.POLYGON||type==Roi.POINT))
            {IJ.error("Convex Hull", "Polygonal or point selection required"); return;}
        if (roi instanceof EllipseRoi)
            return;
        //if (roi.subPixelResolution() && roi instanceof PolygonRoi) {
        //  FloatPolygon p = ((PolygonRoi)roi).getFloatConvexHull();
        //  if (p!=null)
        //      imp.setRoi(new PolygonRoi(p.xpoints, p.ypoints, p.npoints, roi.POLYGON));
        //} else {
        Polygon p = roi.getConvexHull();
        if (p!=null) {
            Undo.setup(Undo.ROI, imp);
            Roi roi2 = new PolygonRoi(p.xpoints, p.ypoints, p.npoints, roi.POLYGON);
            transferProperties(roi, roi2);
            imp.setRoi(roi2);
        }
    }
    public void toBoundingBox(ImagePlus imp) {
        Roi roi = imp.getRoi();
        if (roi==null) {
        	IJ.error("To Bounding Box", "This command requires a selection");
            return;
        }
        Undo.setup(Undo.ROI, imp);
        Rectangle r = roi.getBounds();
        imp.deleteRoi();
        Roi roi2 = new Roi(r.x-1, r.y-1, r.width+3, r.height+3);
        transferProperties(roi, roi2);
        imp.setRoi(roi2);
    }
    private void createMask(ImagePlus plus) {
        ByteProcessor mask = new ByteProcessor(plus.getWidth(), plus.getHeight());
        Roi roi = plus.getRoi();
        if (roi == null) {
            return;
        }
        for (int x = 0; x < plus.getWidth(); x++) {
            for (int y = 0; y < plus.getHeight(); y++) {
                if (roi.contains(x, y)) {
                    mask.putPixel(x, y, 255);
                }
            }
        }
        ImagePlus plusMask = new ImagePlus("mask", mask);
        /*if (plus.getCalibration() != null) {
            plusMask.setCalibration(plus.getCalibration());
        }*/
        
        plusMask.show();
        IJ.log("Mask created");
    }
    
    
}
