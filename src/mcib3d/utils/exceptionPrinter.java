package mcib3d.utils;
import ij.*;
public class exceptionPrinter {
    public static void print(Throwable e, String label, boolean log) {
        if (log) {
            IJ.log(label+" "+e.toString());
            StackTraceElement[] st = e.getStackTrace();
            for (StackTraceElement ste : st ) if (!ste.toString().startsWith("java")) IJ.log(ste.toString());
        } else {
            System.out.println(label+" "+e.toString());
            StackTraceElement[] st = e.getStackTrace();
            for (StackTraceElement ste : st ) if (!ste.toString().startsWith("java")) System.out.println(ste.toString());
        }
    }
    
}
