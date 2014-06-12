package tango.dataStructure;
import mcib3d.image3d.ImageHandler;
import mcib3d.image3d.ImageFloat;
import mcib3d.image3d.ImageInt;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import mcib3d.utils.exceptionPrinter;
import org.bson.types.ObjectId;
import java.io.*;
import mcib3d.geom.*;
import mcib3d.image3d.*;
import ij.*;
import ij.gui.ImageWindow;
import java.util.ArrayList;
import tango.gui.Core;
import tango.mongo.MongoConnector;
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

public class Nucleus extends AbstractStructure {
    public double cX, cY, cZ;
    
    public Nucleus (String title, Cell cell) {
        super(title, 0, cell);
    }
    
    @Override
    public ImageInt openSegmented()  {
        try {
            ImageHandler ih = cell.mc.getNucImage(cell.id, idx, MongoConnector.S);
            if (ih !=null) {
                ImageInt S=(ImageInt)ih;
                xp.setCalibration(S);
                return S;
            }
        } catch (Exception e) {
            exceptionPrinter.print(e, "", Core.GUIMode);
        }
        return null;
    }
    
    @Override
    public void saveOutput(){
        ImageInt mask = cell.segImages.getImage(idx);
        cell.getConnector().saveNucleusImage(cell.id, idx, MongoConnector.S, mask);
    }

    @Override
    public void createObjects() {
        try {
            ImageInt S = cell.segImages.getImage(idx);
            Object3DVoxels nucleus = new Object3DVoxels(S.getImagePlus(), (int)S.getMax(null));
            nucleus.setValue(1);
            cell.segImages.setObjects(new Object3DVoxels[]{nucleus}, idx); 
        } catch (Exception e) {
            exceptionPrinter.print(e, "", Core.GUIMode);
        }
    }
    
}
