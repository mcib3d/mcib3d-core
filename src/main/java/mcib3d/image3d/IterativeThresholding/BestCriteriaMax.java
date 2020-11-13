package mcib3d.image3d.IterativeThresholding;

import mcib3d.utils.ArrayUtil;

import java.util.ArrayList;

/**
 * Created by thomasb on 20/4/16.
 */
public class BestCriteriaMax implements BestCriterion {
    @Override
    public int computeBestCriterion(ArrayUtil list) {
        return list.getMaximumIndex();
    }
}
