package tango.spatialStatistics.StochasticProcess;

import ij.ImagePlus;
import java.util.ArrayList;
import mcib3d.geom.Object3D;
import mcib3d.geom.Point3D;
import tango.spatialStatistics.constraints.Constraint;
import mcib3d.image3d.ImageHandler;
import mcib3d.image3d.ImageInt;
import tango.dataStructure.InputCellImages;
import tango.dataStructure.SegmentedCellImages;
import tango.spatialStatistics.constraints.Hardcore;
import tango.spatialStatistics.util.KDTreeC;
import tango.spatialStatistics.constraints.HomologDistance;
import tango.spatialStatistics.constraints.DistancesToStructure;
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
public class RPGConstraint extends RandomPoint3DGenerator {
    
    protected Constraint[] constraints;
    double resolutionLimit;
    
    public RPGConstraint(ImageInt mask, int maxSize , Constraint[] constraints, double resolutionLimit, InputCellImages raw, SegmentedCellImages seg, int nCPUs, boolean verbose) {
        super(mask, maxSize, nCPUs, verbose);
        this.constraints=constraints;
        this.resolutionLimit=resolutionLimit;
        for (Constraint c : constraints) c.initialize(this, raw, seg);
    }
    
    @Override
    public void resetPoints() {
        if (hardcore) {
            this.kdTree=new KDTreeC(3, this.nbPoints);
            kdTree.setScaleSq(new double[] {resXY*resXY, resXY*resXY, resZ*resZ});
        }
        pointIndex=0;
        points=new Point3D[nbPoints];
        for (Constraint c : constraints) c.reset();
    }
    
    @Override
    protected Point3D drawPoint3D() {
        //System.out.println("draw Point3D: "+pointIndex);
        int firstConstraint=0;
        ArrayList<Integer> candidates=constraints[0].getVoxelIndexes();
        while (candidates==null && firstConstraint<constraints.length) {
            firstConstraint+=1;
            candidates=constraints[firstConstraint].getVoxelIndexes();
        }
        if (candidates!=null) {
            for (int i = 0; i<constraints.length; i++) {
                if (i!=firstConstraint) {
                    int idx =0;
                    while (idx<candidates.size()) {
                        int cand = candidates.get(idx);
                        if (constraints[i].eval(this.maskCoordsXY[cand]%mask.sizeX, this.maskCoordsXY[cand]/mask.sizeX, this.maskCoordsZ[cand], resXY)) idx++;
                        else candidates.remove(idx);
                    }
                }
            }
            //System.out.println("nb vox restant apres contraintes: "+candidates.size());
            while (!candidates.isEmpty()) {
                Point3D p = getSubVoxel(candidates.remove(randomGenerator.nextInt(candidates.size())));
                if (p!=null) {
                    return p;
                }
            } 
            return null;
        } else return null;
    }
    
    protected Point3D getSubVoxel(int voxelIndex) {
        Node curNode=new Node(maskCoordsXY[voxelIndex]%mask.sizeX, maskCoordsXY[voxelIndex]/mask.sizeX, maskCoordsZ[voxelIndex], resXY, resZ);
        int count = 0;
        //Node bestNode = curNode;
        while (curNode!=null && curNode.sizeXY>resolutionLimit) {
            count++;
            Node child = curNode.getOneChild();
            if (child == null) curNode=curNode.getParentAndCommitSuicide();
            else {
                curNode=child;
                //if (bestNode.sizeXY>curNode.sizeXY) bestNode=curNode;
            }
            //if (count>3) return curNode.point;
        }
        //if (curNode!=null) System.out.println("exploration:"+count+ " cur node:"+curNode.point+ " resolution:"+curNode.sizeXY);
        if (curNode!=null && curNode.sizeXY<resolutionLimit) return curNode.point;
        else return null;
    }
    
    @Override
    public boolean isValid() {
        boolean res = true;
        for (Constraint c : this.constraints) res=res&c.isValid();
        return res;
    }
    
    
    
    
    private class Node {
        Point3D point;
        Node parent;
        ArrayList<Node> children;
        double sizeXY,sizeZ;
        
        private Node(double x, double y, double z, double sizeXY, double sizeZ) {
            this.point=new Point3D(x, y , z);
            this.sizeXY=sizeXY;
            this.sizeZ=sizeZ;
        } 
        
        private Node(Point3D point, Node parent) {
            this.point=point;
            this.parent=parent;
            this.sizeXY=parent.sizeXY/2;
            this.sizeZ=parent.sizeZ/2;
        } 
        
        private void setChildren() {
            this.children=new ArrayList<Node>(8);
            double x, y, z;
            x=point.getX()-sizeXY/2;
            y=point.getY()-sizeXY/2;
            z=point.getZ()-sizeZ/2;
            eval(x, y, z); // ---
            z+=sizeZ;
            eval(x, y, z); // --+
            z-=sizeZ;
            y+=sizeXY;
            eval(x, y, z); // -+-
            z+=sizeZ;
            eval(x, y, z); // -++
            z-=sizeZ;
            y-=sizeZ;
            x+=sizeXY;
            eval(x, y, z); // +--
            z+=sizeZ;
            eval(x, y, z); // +-+
            z-=sizeZ;
            y+=sizeXY;
            eval(x, y, z); // ++-
            z+=sizeZ;
            eval(x, y, z); // +++
            
        }
        
        private Node getOneChild() {
            if (children==null) setChildren();
            if (children.isEmpty()) return null;
            return children.get(randomGenerator.nextInt(children.size()));
        }
        
        private void eval(double x, double y, double z) {
            int constIdx=0;
            while (constIdx<constraints.length && constraints[constIdx].eval(x, y, z, sizeXY)) constIdx++;
            if (constIdx==constraints.length) children.add(new Node(new Point3D(x, y, z), this));
        }
        
        private Node getParentAndCommitSuicide() {
            if (parent!=null) parent.children.remove(this);
            return parent;
        }
    }
    
    
}

    
    