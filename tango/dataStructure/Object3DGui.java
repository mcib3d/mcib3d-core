package tango.dataStructure;

import com.mongodb.BasicDBObject;
import java.awt.Color;
import java.text.DecimalFormat;
import java.util.ArrayList;
import mcib3d.geom.Object3D;
import mcib3d.geom.Object3DVoxels;
import mcib3d.image3d.Segment3DSpots;
import org.bson.types.ObjectId;
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


public class Object3DGui implements Comparable<Object3DGui>{
    static int ascendingOrder = 1;
    public final static DecimalFormat df = new DecimalFormat("0.###");
    ObjectId id;
    ObjectStructure channel;
    int label;
    double value;
    Color color;
    String name; 
    mcib3d.geom.Object3D o;
    boolean isNucleus;
    boolean isInSelection;
    
    public static void setAscendingOrger(boolean ascending) {
        if (ascending) ascendingOrder=1;
        else ascendingOrder=-1;
    }
    
    public Object3DGui(Object3D o, ObjectStructure as) {
        if (as instanceof Nucleus) isNucleus=true;
        this.channel=as;
        this.o=o;
        this.label=(isNucleus)?1:o.getValue();
        value = label;
        if (as instanceof AbstractStructure) {
            AbstractStructure ass = (AbstractStructure)as;
            this.color=ass.getColor();
            if (ass.getSelectedIndicies()!= null && ass.getSelectedIndicies().contains(label)) isInSelection=true;
        }
        else this.color=new Color(0, 0, 0);
        setName();
    }
    
    public boolean isInSelection(){
        return isInSelection;
    }
    
    protected void setName() {
        this.name=channel.getChannelName()+"::"+label;
    }

    public ObjectStructure getChannel() {
        return channel;
    }
    
    public Object3D getObject3D() {
        return o;
    }
    
    public void setObject3D(Object3D o) {
        int l = this.label;
        if (this.o!=null) changeLabel(0);
        this.o=o;
        changeLabel(l);
    }
    
    
    public String getName() {
        return name;
    }
    
    public Color getColor() {
        return color;
    }
    
    public void delete(boolean changeLabel) {
        if (isNucleus) return;
        channel.getConnector().removeObject(channel.getId(), channel.getIdx(), label);
        if (changeLabel) changeLabel(0);
    }
    
    public void changeLabel(int label) {
        if (isNucleus) return;
        if (value == this.label) value = label;
        this.label=label;
        setName();
        o.setValue(label);
        o.draw(channel.getSegmented().getImageStack(), label);
    }
    
    public int getLabel() {
        return label;
    }
    
    public void merge(Object3DGui other) {
        if (isNucleus || other.isNucleus || other.channel!=channel) return;
        if (other.label>label) {
            other.changeLabel(label);
            o.getVoxels().addAll(other.o.getVoxels());
            o.init();
            other.delete(false);
        }
        else if (label>other.label) {
            changeLabel(other.label);
            o.getVoxels().addAll(other.o.getVoxels());
            o.init();
            delete(false);
        }
    }
    
    public void adjustThreshold(float thld) {
        // TODO adjust threshold...
    }
    
    public Object3DGui[] split(float rad, float dist) {
        Object3D[] splitted = Segment3DSpots.splitSpotWatershed(o, rad, dist);
        if (splitted!=null && splitted.length>1 && splitted[0]!=null && splitted[0].getVolumePixels()>0 && splitted[1]!=null && splitted[1].getVolumePixels()>0) {
            this.setObject3D(splitted[0]);
            int length = 1;
            for (int i = 2; i<splitted.length; i++) {
                if (splitted[i]!=null && splitted[i].getVolumePixels()>0) length++;
            }
            Object3DGui[] res = new Object3DGui[length];
            for (int i = 0; i<length; i++) {
                if (splitted[i+1]==null || splitted[i+1].getVolumePixels()==0) continue;
                res[i]=new Object3DGui(splitted[i+1], channel);
            }
            return res;
        } return new Object3DGui[0];
    }
    
    public ObjectId getId() {
        if (id!=null) return id;
        else {
            BasicDBObject dbo = channel.getConnector().getObject(channel.getId(), channel.getIdx(), label);
            id=(ObjectId)dbo.get("_id");
            return id;
        }
    }
    
    public void setValue(double value) {
        this.value=value;
        this.setName();
        this.name+=" - value="+df.format(value);
    }

    @Override
    public int compareTo(Object3DGui t) {
        if (value<t.value) return -ascendingOrder;
        else if (value>t.value) return ascendingOrder;
        else return 0;
    }
}
