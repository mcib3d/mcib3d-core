package mcib3d.image3d.comparator;
import java.util.*;
import mcib3d.image3d.ImageByte;

public class ComparatorByte implements Comparator<Integer>{
    ImageByte values;

    public ComparatorByte(ImageByte values) {
        this.values=values;
    }

    public int compare(Integer c1, Integer c2){
        int val1 = values.getPixelInt((c1));
        int val2 = values.getPixelInt((c2));
        if(val1 > val2) return 1;
        else if(val1 < val2) return -1;
        else return 0;
    }
}