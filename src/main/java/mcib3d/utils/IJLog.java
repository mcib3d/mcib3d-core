package mcib3d.utils;

import ij.IJ;

/**
 * Created by thomasb on 15/11/16.
 */
public class IJLog extends AbstractLog {

    @Override
    public void log(String S) {
        IJ.log(S);
    }
}
