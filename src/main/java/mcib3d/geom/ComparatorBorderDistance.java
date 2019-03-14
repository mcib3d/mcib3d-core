package mcib3d.geom;

import java.util.Comparator;

public class ComparatorBorderDistance implements Comparator<ObjectDistBB> {

    @Override
    public int compare(ObjectDistBB o1, ObjectDistBB o2) {
        double bb1 = o1.getDistBB();
        double bb2 = o2.getDistBB();

        if (bb1 < bb2) return -1;
        else return 1;
    }
}
