package tango.plugin;

import tango.parameter.Parameter;


public interface TangoPlugin {
    public Parameter[] getParameters();
    public String getHelp();
    public void setVerbose(boolean verbose);
    public void setMultithread(int nCPUs);
    
}
