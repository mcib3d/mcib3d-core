package mcib3d.utils.Logger;

/**
 * Created by thomasb on 6/12/16.
 */
public class TextLog extends AbstractLog {
    String log = "";

    @Override
    public void log(String S) {
        log =S;
    }

    public String getLog() {
        return log;
    }
}
