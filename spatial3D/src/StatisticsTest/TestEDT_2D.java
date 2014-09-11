package StatisticsTest;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;
import ij.IJ;
import ij.ImagePlus;
import ij.plugin.PlugIn;
import ij.plugin.filter.EDM;
import ij.process.FloatProcessor;
import ij.process.ImageProcessor;
//import ij.process.ShortProcessor;


public class TestEDT_2D implements PlugIn 
{
    boolean inverse = false;
    int threshold = 1;
    public static final double CLOSE_REGION = 0.1;
    public static final double MAX_REGION = 0.7;
    //boolean flag[];
    public static Random RANDOM = new Random(System.nanoTime());
    
    public static final double random(final double pMin, final double pMax) {
	    return pMin + RANDOM.nextDouble() * (pMax - pMin);
	}
    public static final int random(final int pMin, final int pMax) 
    {
	    return pMin + RANDOM.nextInt(pMax - pMin);
    }   
    @Override
    public void run(String arg) 
    {
        ImagePlus impCurrent = IJ.getImage();
        //String title = impCurrent.getTitle();
        ImageProcessor ip = impCurrent.getProcessor();
        EDM e = new EDM();
        e.setup("edm", impCurrent);
        FloatProcessor ip2 = e.makeFloatEDM(ip, (byte)0, false);
        ImagePlus imp2 = new ImagePlus();
        FloatProcessor ip3 = ip2;
        ip3.threshold(threshold);
        normalizeDistanceMap(ip2, ip3);
        FloatProcessor ip4 = getCloseDistanceZone(ip2);
        
        
        //int count = 0;
    	
        
    	/*while(count < 20)
    	{	
			float tmp = (float)getPoissonDistribution(5) / 10;
			ArrayList<Point2D> listPoint = new ArrayList<Point2D>();
			IJ.log("value of tmp: " + tmp);
			
			for(int x=0; x<ip2.getWidth(); x++)
            {
            	for(int y=0; y<ip2.getHeight(); y++)
            	{
            		if(ip2.getf(x, y) <= tmp && ip2.getf(x, y) > (tmp - 0.05))
            		{
            			//IJ.log("x: " + x + " y: " + y + " value: " + ip2.getf(x, y) +" tmp: "+ tmp);
            			Point2D p = new Point2D(ip2.getf(x, y), x, y);
            			listPoint.add(p);
            			
            		}
            	}	
            }
			if(!listPoint.isEmpty())
			{
				//verify position of Point2D with others
				int ranIndex = random(0, (listPoint.size() - 1));
				IJ.log("random value: stt: " + count + " x:" + listPoint.get(ranIndex).x + " y: " + listPoint.get(ranIndex).y + 
						" dis: " + listPoint.get(ranIndex).distance);
				
				ip2.setf(listPoint.get(ranIndex).x, listPoint.get(ranIndex).y, 100 + count * 5);
				count++;
			}
			listPoint.clear();
			
    	}*/
	imp2.setProcessor(ip4);
        imp2.setTitle("EDM Result Found");
        imp2.show();
        
    }  
    public FloatProcessor getCloseDistanceZone(FloatProcessor ip)
    {
    	FloatProcessor tmp = new FloatProcessor(ip.getWidth(), ip.getHeight());
    	for(int x=0; x<ip.getWidth(); x++)
        {
        	for(int y=0; y<ip.getHeight(); y++)
        	{
        		if(ip.getf(x, y) < CLOSE_REGION)
        		{
        			tmp.setf(x, y, 100 + ip.getf(x, y) * 50);
        		}
                        else
                        {
                                tmp.setf(x, y, 0);
                        }
        	}	
        }
    	return tmp;
    	
    }
    public double getRandomExponential(double p) {
		return -(Math.log(RANDOM.nextDouble()) / p);
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
    public void normalizeDistanceMap(ImageProcessor imgDistanceMap, ImageProcessor imgMaskPro) 
    {	
        int count = 0;
        //IJ.log("mask volume: " + countMaskVolume(imgMaskPro));
        Point2D[] idx = new Point2D[countMaskVolume(imgMaskPro)];
        double volume = idx.length;
        for (int x = 0; x < imgDistanceMap.getWidth(); x++) {
            for (int y = 0; y < imgDistanceMap.getHeight(); y++) {
                if (imgMaskPro.getPixel(x, y) != 0) 
                {  
                    idx[count] = new Point2D(imgDistanceMap.getf(x, y), x, y);
                    count++;
                    //IJ.log("dis: " + imgDistanceMap.getf(x,y));  
                }
            }
        }
        
        //IJ.log("size of arr: " + idx.length);
        
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
        
        for (int i = 0; i < idx.length; i++) 
        {	
        	imgDistanceMap.setf(idx[i].x, idx[i].y, (float) (idx[i].index / volume));
        }
       
    }
    protected class Point2D implements Comparable<Point2D>
    {
    	float distance;
        double index;
        int x, y;
        public Point2D(int x, int y) {
            this.x = x;
            this.y = y;
        }
        public Point2D(float distance, int x, int y) {
            this.distance = distance;
            this.x = x;
            this.y = y;
        }

        public Point2D(float distance, double index, int x, int y) {
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
