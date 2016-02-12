package mcib3d.image3d.comparator;
import java.util.*;
import mcib3d.image3d.ImageFloat;

public class ComparatorFloat implements Comparator<Integer>{
    ImageFloat values;

    public ComparatorFloat(ImageFloat values) {
        this.values=values;
    }

    public int compare(Integer c1, Integer c2){
        float val1 = values.getPixel(c1);
        float val2  = values.getPixel(c2);
        if(val1 > val2) return 1;
        else if(val1 < val2) return -1;
        else return 0;
    }
}
