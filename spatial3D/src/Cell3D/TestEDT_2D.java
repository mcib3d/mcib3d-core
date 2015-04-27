package Cell3D;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

import mcib3d.image3d.ImageFloat;
import mcib3d.image3d.ImageInt;
import ij.IJ;
import ij.ImagePlus;
import ij.plugin.PlugIn;
import ij.plugin.filter.EDM;
import ij.plugin.filter.PlugInFilter;
import ij.process.FloatProcessor;
import ij.process.ImageProcessor;
import ij.process.ShortProcessor;


public class TestEDT_2D implements PlugIn {

    boolean inverse = false;
    int threshold = 1;
    boolean flag[];
    public static Random RANDOM = new Random(System.nanoTime());
    ArrayList<Point2D> listPoint = new ArrayList<Point2D>();
    public static final double random(final double pMin, final double pMax) {
	    return pMin + RANDOM.nextDouble() * (pMax - pMin);
	}
    @Override
    public void run(String arg) 
    {
        ImagePlus impCurrent = IJ.getImage();
        String title = impCurrent.getTitle();
        ImageProcessor ip = impCurrent.getProcessor();
        EDM e = new EDM();
        e.setup("edm", impCurrent);
        //IJ.log("output type: " + e.getOutputType());
        //e.setOutputType(2);
        //IJ.log("set output type: " + e.getOutputType());
        FloatProcessor ip2 = e.makeFloatEDM(ip, (byte)0, false);
        ImagePlus imp2 = new ImagePlus();
        
        
        /*ShortProcessor ip3 = ip2;
        ip3.threshold(threshold);
        normalizeDistanceMap(ip2, ip3);
        imp2.setProcessor(ip2);
        */
       /* for(int x=0; x<ip2.getWidth(); x++)
        {
        	for(int y=0; y<ip2.getHeight(); y++)
        	{
        		if(ip2.getf(x, y) < 6){
        			//ip2.setf(x, y, 100);
        			IJ.log("x: " + x + " y: " + y + " value: " + ip2.getf(x, y));
        		}
        	}	
        }*/	
        
    	int count = 0;
    	
    	while(count < 10)
    	{	
			int tmp = getPoissonDistribution(6);
			count++;
			
			for(int x=0; x<ip2.getWidth(); x++)
            {
            	for(int y=0; y<ip2.getHeight(); y++)
            	{
            		if(ip2.getf(x, y) <= (float)tmp && ip2.getf(x, y) > ((float)tmp - 1)){
            			IJ.log("x: " + x + " y: " + y + " value: " + ip2.getf(x, y) +" dis: "+ tmp);
            			//ip2.setf(x, y, 100);
            			Point2D p = new Point2D(x, y);
            			listPoint.add(p);
            			//flag[count] = false;
            			//ip2.setf(x, y, 100);
            			
            		}
            	}	
            }
			
			double ranIndex = random(0, listPoint.size()-1);
			IJ.log("random value: " + listPoint.get((int)ranIndex).x + " y: " + listPoint.get((int)ranIndex).y);
			ip2.setf(listPoint.get((int)ranIndex).x, listPoint.get((int)ranIndex).y, 100 + count * 5);
    	}	
    	/*IJ.log("verify values");
    	for(int x=0; x<ip2.getWidth(); x++)
        {
        	for(int y=0; y<ip2.getHeight(); y++)
        	{
        		if(ip2.getf(x, y) == 100.0)
        		{
        			IJ.log("x: " + x + " y: " + y + " value: " + ip2.getf(x, y));
        		}
        	}	
        }*/
		
		imp2.setProcessor(ip2);
        imp2.setTitle("EDM Result");
        imp2.show();
    }  
    public static int getPoissonDistribution(double lambda) {
    	  double L = Math.exp(-lambda);
    	  double p = 1.0;
    	  int k = 0;

    	  do {
    	    k++;
    	    p *= Math.random();
    	  } while (p > L);

    	  return k - 1;
    }
    public int countMaskVolume(ImageProcessor img) {
        int count = 0;
        for (int x = 0; x<img.getWidth(); x++) {
            for (int y = 0; y<img.getHeight(); y++) {
                if (img.getPixel(x,y)!=0) count++;
            }
        }
        return count;
    }
    public void normalizeDistanceMap(ShortProcessor imgDistanceMap, ImageProcessor imgMaskPro) 
    {
    	
    	//ImageProcessor imgMaskPro = mask.getProcessor();
    	//ImageProcessor imgDistanceMap = distanceMap.getProcessor();
        int count = 0;
        Point2D[] idx = new Point2D[countMaskVolume(imgMaskPro)];
        double volume = idx.length;
        for (int x = 0; x < imgDistanceMap.getWidth(); x++) {
            for (int y = 0; y < imgDistanceMap.getHeight(); y++) {
                if (imgMaskPro.getPixel(x, y) != 0) {
                    idx[count] = new Point2D(imgDistanceMap.getPixel(x, y), x, y);
                    count++;
                }
            }
        }
        Arrays.sort(idx);
        for (int i = 0; i < idx.length - 1; i++) {
            // gestion des repetitions
            if (idx[i + 1].distance == idx[i].distance) {
                int j = i + 1;
                while (j < (idx.length - 1) && idx[i].distance == idx[j].distance) {
                    j++;
                }
                double median = (i + j) / 2d;
                for (int k = i; k <= j; k++) {
                    idx[k].index = median;
                }
                i = j;
            } else {
                idx[i].index = i;
            }
        }
        if (idx[idx.length - 1].index == 0) {
            idx[idx.length - 1].index = idx.length - 1;
        }
        for (int i = 0; i < idx.length; i++) {
        	if(idx[i].index / volume < 0.5){
        		imgDistanceMap.putPixel(idx[i].x, idx[i].y, (int) (idx[i].index / volume));
        	}
            
        }
    }
    protected class Point2D implements Comparable<Point2D>
    {
    	int distance;
        double index;
        int x, y;
        public Point2D(int x, int y) {
            this.x = x;
            this.y = y;
        }
        public Point2D(int distance, int x, int y) {
            this.distance = distance;
            this.x = x;
            this.y = y;
        }

        public Point2D(int distance, double index, int x, int y) {
            this.distance = distance;
            this.index = index;
            this.x = x;
            this.y = y;
        }

        public int compareTo(Point2D v) {
            if (distance > v.distance) {
                return 1;
            } else if (distance < v.distance) {
                return -1;
            } else {
                return 0;
            }
        }
    }
}
