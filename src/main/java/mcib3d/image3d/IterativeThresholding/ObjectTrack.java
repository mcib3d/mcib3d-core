/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mcib3d.image3d.IterativeThresholding;

import mcib3d.geom.Object3D;
import mcib3d.geom.Voxel3D;
import mcib3d.image3d.ImageHandler;

import java.util.ArrayList;

/**
 * *
 * /**
 * Copyright (C) 2008- 2012 Thomas Boudier and others
 * <p>
 * <p>
 * <p>
 * This file is part of mcib3d
 * <p>
 * mcib3d is free software; you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 3 of the License, or (at your option) any later
 * version.
 * <p>
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * <p>
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 *
 * @author thomas
 */
public class ObjectTrack {


    public static int STATE_UNKNOWN = 0;
    public static int STATE_DIE = 1;
    public static int STATE_DIVIDE = 2;
    public static int STATE_MOVE = 3;
    // measurements
    public double valueCriteria;
    //public double sphericity;
    //public double elongation;
    //public double DCavg;
    //public double DCsd;
    public double volume;
    // first point for segment + threshold
    public Voxel3D seed;
    public int threshold;
    public ImageHandler rawImage;
    public boolean VALID = true;
    public int id;
    private Object3D object = null;
    //private double time = 0;
    private int frame = 0;
    private ArrayList<ObjectTrack> children = null;
    private ObjectTrack parent = null;
    private int state = STATE_UNKNOWN;

    public ObjectTrack() {
    }

    public ObjectTrack getParent() {
        return parent;
    }

    public void setParent(ObjectTrack parent) {
        this.parent = parent;
        state = STATE_UNKNOWN;
    }

    public int getFrame() {
        return frame;
    }

    public void setFrame(int frame) {
        this.frame = frame;
    }

    public int getState() {
        if (state == STATE_UNKNOWN) {
            computeState();
        }
        return state;
    }

    public void setState(int state) {
        this.state = state;
    }

    public boolean isNew() {
        return parent == null;
    }

    public boolean isRoot() {
        return parent == null;
    }

    public boolean isEnd() {
        return (getNbChildren() == 0);
    }


    public int getNbChildren() {
        if (children == null) {
            return 0;
        } else {
            return children.size();
        }
    }

    public ArrayList<ObjectTrack> getChildren() {
        return children;
    }

    public void addChild(ObjectTrack child) {
        if (children == null) {
            children = new ArrayList<ObjectTrack>();
        }
        children.add(child);
        state = STATE_UNKNOWN;
    }

    public void removeAllChildren() {
        children = null;
        state = STATE_UNKNOWN;
    }

    public void removeChild(ObjectTrack child) {
        children.remove(child);
        if (children.isEmpty()) {
            state = STATE_UNKNOWN;
        }
    }

    private void computeState() {
        if (children == null) {
            state = STATE_DIE;
        } else {
            if (children.size() == 1) {
                state = STATE_MOVE;
            } else if (children.size() > 1) {
                state = STATE_DIVIDE;
            } else { // 0 children
                state = STATE_DIE;
            }
        }
    }

    public boolean hasBrothers() {
        if (parent == null) {
            return false;
        }
        return parent.getNbChildren() > 1;
    }

    public ObjectTrack getAncestor() {
        ObjectTrack par = this;
        while (!par.hasBrothers()) {
            if (par.getParent() == null) {
                return par;
            } else {
                par = par.getParent();
            }
        }
        return par;
    }

    public ObjectTrack getRoot() {
        ObjectTrack par = this;
        while (par.getParent() != null) {
            par = par.getParent();
        }

        return par;
    }


    public ArrayList<ObjectTrack> getLineageTo(ObjectTrack anc) {
        ArrayList<ObjectTrack> list = new ArrayList<ObjectTrack>();
        ObjectTrack par = this;
        list.add(par);
        while (!par.equals(anc)) {
            if (par.getParent() == null) {
                return null;
            } else {
                par = par.getParent();
                list.add(par);
            }
        }

        return list;
    }



    public ArrayList<ObjectTrack> getAllDescendantsToEnd() {
        ArrayList<ObjectTrack> list = new ArrayList<ObjectTrack>();
        ObjectTrack par = this;
        list.add(par);
        if (par.getNbChildren() == 0) return list;
        for (ObjectTrack child : children) {
            list.addAll(child.getAllDescendantsToEnd());
        }
        return list;
    }

    public boolean isValid() {
        return VALID;
    }

    public Object3D getObject3D() {
        return object;
    }

    public void setObject3D(Object3D object3) {
        this.object = object3;
    }

    public void computeCriterion(Criterion criterion) {
        valueCriteria = criterion.computeCriterion(getObject3D());
    }

    @Override
    public String toString() {
        return id + "(" + seed+") "+threshold;
    }
}
