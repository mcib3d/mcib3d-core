package tango.dataStructure;

import com.mongodb.BasicDBObject;
import ij.IJ;
import ij.measure.Calibration;
import java.util.ArrayList;
import mcib3d.image3d.ImageHandler;
import mcib3d.image3d.ImageInt;
import mcib3d.utils.exceptionPrinter;
import tango.gui.Core;
import tango.gui.parameterPanel.VirtualStructurePanel;
import tango.gui.util.ColocFactory;
import tango.parameter.BooleanParameter;
import tango.parameter.Parameter;
import tango.parameter.StructureParameter;
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

public class VirtualStructureColoc extends VirtualStructure {

    public final static String[] outputMethods = new String[]{"Union", "Intersection", "Spot", "None"};
    int outputMethod;
    double pcColoc;
    int outputStructure;
    int[] structures;
    boolean[] keepObjects;

    public VirtualStructureColoc(String title, int idx, Cell cell) {
        super(title, idx, cell);
        init();
    }
    
    protected void init() {
        VirtualStructurePanel vsp = getPanel(cell, idx);
        pcColoc = vsp.pcColoc.getDoubleValue(50);
        outputStructure = vsp.outputStructure.getIndex();
        outputMethod = vsp.outputType.getSelectedIndex();
        ArrayList<Parameter[]> params = vsp.structures.getParametersArrayList();
        structures = new int[params.size()];
        keepObjects = new boolean[params.size()];
        for (int i = 0; i < structures.length; i++) {
            Parameter[] alp = params.get(i);
            structures[i] = ((StructureParameter) alp[0]).getIndex();
            keepObjects[i] = ((BooleanParameter) alp[1]).isSelected();
        }
    }

    @Override
    public void process() {
        try {
        int nbs = structures.length;
        if (Core.GUIMode) IJ.log("virtual nb=" + nbs);

        ImageInt[] imas = new ImageInt[nbs];
        for (int i = 0; i < nbs; i++) {
            imas[i]=cell.segImages.getImage(structures[i]);
        }
        Calibration cal = imas[0].getImagePlus().getCalibration();
        
        ImageInt S=null;
        if (nbs == 2) {
            S = (ImageInt) ImageHandler.wrap(ColocFactory.createColoc2Images(imas, cal, keepObjects, pcColoc, outputMethod, outputStructure));
        }
        else if(nbs==3){
             S = (ImageInt) ImageHandler.wrap(ColocFactory.createColoc3Images(imas, cal, keepObjects, pcColoc, outputMethod, outputStructure));
            
        }
        if (S!=null) {
            S.setTitle(this.getChannelName());
            cell.segImages.setSegmentedImage(S, idx);
        }
        } catch (Exception e) {
            String n = (cell.field!=null) ? "field: "+cell.field.name+ " ": "";
            n+="cell: "+cell.name+ " channel: "+name;
            exceptionPrinter.print(e, "process virtual: "+n, Core.GUIMode);
        }
    }
    
}
