package mcib3d.image3d.IterativeThresholding;

import java.util.Comparator;

public class ComparatorObjectTrack implements Comparator<ObjectTrack> {


    @Override
    public int compare(ObjectTrack o1, ObjectTrack o2) {
        if (o1.valueCriteria < o2.valueCriteria) return -1;
        else if (o1.valueCriteria > o2.valueCriteria) return +1;
        else {
            if (o1.volume < o2.volume) return -1;
            else if (o1.volume > o2.volume) return +1;
            else {
                if (o1.id < o2.id) return -1;
                else if (o1.id > o2.id) return +1;
                else return 0;
            }
        }
    }
}
