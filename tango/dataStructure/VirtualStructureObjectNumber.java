package tango.dataStructure;

import mcib3d.geom.Object3DVoxels;
import mcib3d.image3d.ImageFloat;
import mcib3d.image3d.ImageHandler;
import mcib3d.image3d.ImageInt;
import mcib3d.utils.exceptionPrinter;
import tango.gui.Core;
import tango.gui.parameterPanel.VirtualStructurePanel;
import tango.plugin.segmenter.DivideObjects;

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

public class VirtualStructureObjectNumber extends VirtualStructure {
    int objectNumber;
    int inputStructure;
    double[] objectNumbers, objectNumbersVirtual, objectIdx;
    double[] effectiveValues, effectiveValuesVirtual;
    VirtualStructureObjectNumber(String title, int idx, Cell cell) {
        super(title, idx, cell);
        
    }
    
    public int getInputStructure() {
        return inputStructure;
    }
    
    public double[] getObjectNumbers(boolean virtual) {
        return virtual?objectNumbersVirtual:objectNumbers;
    }
    
    public double[] getObjectIdx() {
        return objectIdx;
    }
    
    public double[] getEffectiveValues(boolean virtual) {
        return virtual?effectiveValuesVirtual:effectiveValues;
    }
    
    protected void init() {
        VirtualStructurePanel vsp = getPanel(cell, idx);
        objectNumber = vsp.objectNumber.getIntValue(46);
        inputStructure = vsp.inputStructure.getIndex();
    }

    @Override
    public void process() {
        init();
        if (inputStructure<=0) return;
        ((Structure)cell.getStructure(inputStructure)).shiftObjectIndexes(!cell.verbose);
        Object3DVoxels[] o = cell.segImages.getObjects(inputStructure); 
        if (o==null || o.length==0) return;
        ImageHandler intensity = cell.inputImages.getImage(inputStructure);
        // get values
        double[] values = new double[o.length];
        for (int i = 0; i<values.length; i++) values[i]=o[i].getIntegratedDensity(intensity);
        
        DivideObjects divO = new DivideObjects(o, values);
        divO.divideObjects(objectNumber);
        objectNumbers = divO.getNumbers();
        effectiveValues = divO.getEffectiveValues();
        // plot if testing
        if (cell.verbose) divO.plot("Cell: "+this.cell.getName()+ " number of objects in structure:"+o.length);
        
        //create new objects (clones with new  ones at the end with new values)
        Object3DVoxels[] res;
        if (o.length>=objectNumber) {
            res = o;
            effectiveValuesVirtual=effectiveValues;
            objectNumbersVirtual = objectNumbers;
            objectIdx= new double[res.length];
            for (int i = 0; i<objectIdx.length; i++) objectIdx[i]=i+1;
        } else {
            res = new Object3DVoxels[objectNumber];
            objectIdx= new double[objectNumber];
            for (int i = 0; i<o.length; i++) objectIdx[i]=i+1;
            effectiveValuesVirtual = new double[objectNumber];
            objectNumbersVirtual = new double[objectNumber];
            System.arraycopy(o, 0, res, 0, o.length);
            System.arraycopy(effectiveValues, 0, effectiveValuesVirtual, 0, effectiveValues.length);
            System.arraycopy(objectNumbers, 0, objectNumbersVirtual, 0, objectNumbers.length);
            int curIdx = o.length;
            objectNumbers = divO.getNumbers();
            for (int i = 0; i<o.length; i++) {
                for (int j = 1; j<objectNumbers[i]; j++) {
                    res[curIdx] = o[i].copyObject(curIdx+1);
                    objectNumbersVirtual[curIdx]=objectNumbers[i];
                    effectiveValuesVirtual[curIdx]=effectiveValues[i];
                    objectIdx[curIdx]=i+1;
                    curIdx++;
                }
            }
        }
        cell.segImages.setSegmentedImage(cell.segImages.getImage(inputStructure), idx);
        cell.segImages.setObjects(res, idx);
    }
    
    @Override
    public void createObjects() {
        process();
    }

}
