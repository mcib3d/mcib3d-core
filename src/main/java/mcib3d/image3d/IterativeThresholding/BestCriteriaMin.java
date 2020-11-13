package mcib3d.image3d.IterativeThresholding;

import mcib3d.utils.ArrayUtil;

/**
 * Created by thomasb on 20/4/16.
 */
public class BestCriteriaMin implements BestCriterion {
    @Override
    public int computeBestCriterion(ArrayUtil list) {
        return list.getMinimumIndex();
    }
}
