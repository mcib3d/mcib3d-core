/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package tango.dataStructure;

import mcib3d.image3d.ImageHandler;
import mcib3d.image3d.ImageInt;

/**
 *
 * @author jean
 */
public interface StructureContainer {
    public int getFileRank(int channelIdx);
    public ImageHandler openInputImage(int channelImageIdx);
    public ImageInt getMask();
    public ImageHandler preFilterStructure(ImageHandler image, int structureIdx);
    public ImageInt postFilterStructure(ImageInt image, int structureIdx);
    public void setVerbose(boolean verbose);
}
