package mcib3d.utils.Logger;

import ij.IJ;

/**
 * Created by thomasb on 15/11/16.
 */
public class IJLog extends AbstractLog {
    private boolean update = false;

    @Override
    public void log(String S) {
        if (S != null)
            if (update)
                IJ.log("\\Update:" + S);
            else IJ.log(S);
    }

    public boolean isUpdate() {
        return update;
    }

    public void setUpdate(boolean update) {
        this.update = update;
    }
}
