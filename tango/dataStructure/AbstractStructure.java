
package tango.dataStructure;
import mcib3d.image3d.ImageHandler;
import mcib3d.image3d.ImageInt;
import com.mongodb.BasicDBObject;
import java.awt.Color;
import java.util.ArrayList;
import javax.swing.ImageIcon;
import mcib3d.geom.Object3D;
import org.bson.types.ObjectId;
import tango.mongo.MongoConnector;
import tango.util.IJ3dViewerParameters;
/**
 *
 **
 * /**
 * Copyright (C) 2012 Jean Ollion
 *
 *
 *
 * This file is part of tango
 *
 * tango is free software; you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 3 of the License, or (at your option) any later
 * version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 *
 * @author Jean Ollion
 */
public abstract class AbstractStructure implements ObjectStructure {
    Cell cell;
    protected int idx, idxRaw;
    protected Experiment xp;
    protected String name;
    ArrayList<Integer> selectedIndicies;
    public AbstractStructure(String title, int idx, Cell cell) {
        this.idx=idx;
        this.cell=cell;
        this.xp=cell.xp;
        this.name=title;
        if (idx<cell.getNbStructures(false)) this.idxRaw=cell.getExperiment().getChannelFileIndexes()[idx];
        else idxRaw=-1;
    }
    
    public void setSelectedIndicies(ArrayList<Integer> selection) {
        //if (selection!=null) System.out.println("number of selected objects for sutrcture:"+name+ ": "+selection.size()+" "+MeasurementKey.arrayToString(selection));        
        selectedIndicies=selection;
    }
    
    public ArrayList<Integer> getSelectedIndicies() {
        return selectedIndicies;
    }
    
    public ImageIcon getThumbnail() {
        return cell.getThumbnail(idx);
    }
    @Override
    public int getIdx() {
        return idx;
    }
    
    public int getIdxRaw() {
        return idxRaw;
    }
    //abstract void close();
    @Override
    public abstract void saveOutput();
    @Override
    public abstract void createObjects();
    @Override
    public Object3D[] getObjects() {
        return cell.segImages.getObjects(idx);
    }
    public Cell getCell() {
        return cell;
    }

    public abstract ImageInt openSegmented();
    public Color getColor() {
        return tango.gui.util.Colors.colors.get(xp.getChannelSettings(idx).getString("color"));
    }
    public String getColorName() {
        return xp.getChannelSettings(idx).getString("color");
    }
    @Override
    public String getChannelName() {
        return xp.getChannelSettings(idx).getString("name");
    }
    
    public String getName() {
        return this.name;
    }
    
    @Override
    public ImageInt getSegmented() {
        return cell.segImages.getImage(idx);
    }

    public ImageHandler getRaw() {
        return cell.inputImages.getChannelFile(idxRaw);
    }
    
    public ImageHandler getFiltered() {
        return cell.inputImages.getFilteredImage(idx);
    }
    
    @Override
    public MongoConnector getConnector() {
        return cell.getConnector();
    }
    
    @Override
    public ObjectId getId() {
        return cell.getId();
    }
    
    public IJ3dViewerParameters getIJ3DViwerParameter() {
        BasicDBObject settings = this.cell.xp.getChannelSettings(idx);
        IJ3dViewerParameters res = new IJ3dViewerParameters(idx==0);
        res.getParameter().dbGet(settings);
        return res;
    }
}

