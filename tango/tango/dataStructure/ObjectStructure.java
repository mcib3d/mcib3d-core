package tango.dataStructure;

import mcib3d.geom.Object3D;
import mcib3d.image3d.ImageInt;
import org.bson.types.ObjectId;
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

public interface ObjectStructure {
    public Object3D[] getObjects();
    public void createObjects();
    public ImageInt getSegmented();
    public void saveOutput();
    public MongoConnector getConnector();
    public ObjectId getId();
    public String getChannelName();
    public int getIdx();
}
