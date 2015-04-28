package tango.util;
import mcib3d.image3d.ImageHandler;
import mcib3d.image3d.ImageByte;
import tango.dataStructure.Cell;
import tango.dataStructure.Nucleus;
import tango.dataStructure.AbstractStructure;
import tango.dataStructure.Structure;
import ij3d.*;
import ij.gui.GUI;
import javax.vecmath.Color3f;
import javax.vecmath.Point3d;
import java.util.*;
import com.sun.j3d.utils.universe.SimpleUniverse;
import customnode.CustomMesh;
import customnode.CustomMeshNode;
import customnode.CustomMultiMesh;
import customnode.CustomTriangleMesh;
import ij.*;
import isosurface.MeshEditor;
import isosurface.MeshGroup;
import java.awt.Color;
import javax.media.j3d.Background;
import voltex.VoltexGroup;
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
public class Cell3DViewer {
    public static Map<String, Color3f> colors3f = Collections.unmodifiableMap(new HashMap<String, Color3f>() {{
        put("BLUE", new Color3f(0,    0, 1f));
        put("GREEN", new Color3f(0,    1f, 0));
        put("YELLOW", new Color3f(1f, 1f, 0));
        put("RED", new Color3f(1f,    0, 0));
        put("CYAN", new Color3f(0, 1f, 1f));
        put("MAGENTA", new Color3f(1f,    0, 1f));
        put("WHITE", new Color3f(1f, 1f, 1f));
    }});
    
    
    public static Color3f bcg=new Color3f(1, 1, 1);
    Image3DUniverse univ;
    
    int type;
    ArrayList<ImageByte> images;
			
    public Cell3DViewer() {
        univ= new Image3DUniverse();
        
        //univ.show();
        //GUI.center(univ.getWindow());
        //verifier l'ouverture des cells?
    }

    public void addContent(Cell cell, boolean show) throws Exception {
        reset();
        if (univ==null || univ.getWindow()==null || univ.getCanvas()==null) {
            univ= new Image3DUniverse();
            if (show) show();
            //univ.sync(true);
            //return;
        }
        images=new ArrayList<ImageByte>();
        // TODO MULTITHREAD ??
        for (int i = 0; i<cell.getNbStructures(true); i++) { 
            AbstractStructure s=cell.getStructure(i);
            addContent(s);
        }
        //adjust();
    }
    
    public void resetAndAddNucleus(Cell c) {
        reset();
        images=new ArrayList<ImageByte>();
        addContent(c.getNucleus());
        adjust();
    }
    
    public void adjust() {
        if (images==null || images.isEmpty() || images.get(0)==null) return;
        ImageHandler im = images.get(0);
        //System.out.println("univ set size: x:"+im.sizeX+ " y:"+im.sizeY);
        univ.setSize(im.sizeX, im.sizeY);
        univ.centerAt(new Point3d(im.sizeX/2, im.sizeY/2, im.sizeZ/2));
        univ.setSize(512, 512);
    }
    
    public void addContent(AbstractStructure s) {
        IJ3dViewerParameters params=s.getIJ3DViwerParameter();
        if (!params.getDisplay()) return;
        if (s.getSegmented()!=null) {
            ImageByte im=new ImageByte(s.getSegmented(), false);
            addContent(im, s.getChannelName(), colors3f.get(s.getColorName()), params);
        }
    }
    
    public void addContent(ImageByte image, String name, Color3f color, IJ3dViewerParameters params) {
        if (image!=null && params.getDisplay()) {
            images.add(image);
            if (color!=null) {
                //IJ.log("title:"+s.getName());
                //Content c = ContentCreator.createContent(s.getName(), im.img, type, (i==0)?2:1,0, color, 0,new boolean[3]);
                
                //Content c = univ.addContent(image.getImagePlus(), color, name, 0, new boolean[] {true, true, true}, params.getResamplingFactor(), 2);
                Content c = ContentCreator.createContent(name, image.getImagePlus(), 2, params.getResamplingFactor(), 0, color, 0, new boolean[] {true, true, true});
                univ.addContentLater(c);
                //Content c = univ.addContent(im.img, 2, 1);
                //c.setThreshold(0);
                //c.setColor(color);
                //System.out.println("Smooth: "+s.getName()+ " iterations:"+params[i].getSmooth());
                c.setTransparency((float)params.getTransparancy());
                c.setLocked(true);
                c.setShaded(params.getShade());
                //univ.addContent(c);
                c.setName(name);
                smoothMesh(c, params.getSmooth());
            }
        }
    }
    
    public static void smoothMesh(Content c, int iterations) {
        if (c==null) return; 
        if(c.getType() == Content.SURFACE || c.getType() == Content.CUSTOM) {
            ContentNode cn = c.getContent();
            if(cn instanceof MeshGroup) {
                MeshEditor.smooth2(((MeshGroup)cn).getMesh(), iterations);
            }
            if(cn instanceof CustomMultiMesh) {
                    CustomMultiMesh multi = (CustomMultiMesh)cn;
                    for(int i=0; i<multi.size(); i++) {
                            CustomMesh m = multi.getMesh(i);
                            if(m instanceof CustomTriangleMesh)
                                    MeshEditor.smooth2((CustomTriangleMesh)m, iterations);
                    }
            } else if(cn instanceof CustomMeshNode) {
                CustomMesh mesh = ((CustomMeshNode)cn).getMesh();
                    if(mesh instanceof CustomTriangleMesh)
                            MeshEditor.smooth2((CustomTriangleMesh)mesh, iterations);
            }
        }
               
    }
    
    

    public void show() {
        //((ImageCanvas3D)univ.getCanvas()).setBackground(bcg);
        univ.show();
        ((ImageCanvas3D)univ.getCanvas()).getBG().setColor(bcg);
        univ.getWindow().getStatusLabel().setBackground(bcg.get());
        ((ImageCanvas3D)univ.getCanvas()).render();
        
        GUI.center(univ.getWindow());
        //this.type = -1;
    }
    
    public void reset() {
        if (images==null) return;
        univ.removeAllContents();
        for (ImageHandler ih : images) ih.closeImagePlus();
        this.images=null;
    }
    

    public void close() {
        univ.close();
        for (ImageHandler ih : images) ih.closeImagePlus();
    }
}
