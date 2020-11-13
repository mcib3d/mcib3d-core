package mcib3d.utils.Logger;

import ij.IJ;

/**
 * Created by thomasb on 15/11/16.
 */
public class IJStatus extends AbstractLog {

    @Override
    public void log(String S) {
         IJ.showStatus(S);
    }
}
