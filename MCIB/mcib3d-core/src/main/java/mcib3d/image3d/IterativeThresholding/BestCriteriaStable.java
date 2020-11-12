package mcib3d.image3d.IterativeThresholding;

import mcib3d.utils.ArrayUtil;

/**
 * Created by thomasb on 20/4/16.
 */
public class BestCriteriaStable implements BestCriterion {
    private int step = 1;

    public void setStep(int step) {
        this.step = step;
    }

    @Override
    public int computeBestCriterion(ArrayUtil list) {
        // minimum of |(a+s)-(a-s)|/(a)
        if (list.size() < 2 * step + 1) return (list.size() / 2);
        double minimum = Double.MAX_VALUE;
        int index = -1;
        for (int i = step; i < list.size() - step; i++) {
            double value = Math.abs(list.getValue(i + step) - list.getValue(i - step)) / list.getValue(i);
            if (value < minimum) {
                minimum = value;
                index = i;
            }
        }

        return index;
    }
}
