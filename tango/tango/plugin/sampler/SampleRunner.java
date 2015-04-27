
package tango.plugin.sampler;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import java.util.ArrayList;
import mcib3d.geom.Object3D;
import mcib3d.geom.Point3D;
import tango.spatialStatistics.StochasticProcess.RandomPoint3DGenerator;
import tango.spatialStatistics.StochasticProcess.RandomPoint3DGeneratorProbaMap;
import tango.spatialStatistics.StochasticProcess.RandomPoint3DGeneratorUniform;
import mcib3d.image3d.ImageHandler;
import mcib3d.image3d.ImageShort;
import mcib3d.utils.ThreadRunner;
import mcib3d.utils.exceptionPrinter;
import tango.dataStructure.Cell;
import tango.dataStructure.Structure;
import tango.dataStructure.Experiment;
import tango.gui.Core;
import tango.gui.util.CellFactory;
import tango.mongo.MongoConnector;
import tango.parameter.Parameter;
import tango.plugin.PluginFactory;
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
public class SampleRunner {
    
    public static void test() {
        try {
        Cell c= CellFactory.getOneCell(Core.getExperiment());
        if (c!=null) {
            Object3D[][][] o = runCell(c, 1);
            ImageShort[] im = new ImageShort[o.length];
            ImageHandler ref = c.getMask();
            for (int i = 0; i<o.length; i++) {
                im[i]=new ImageShort(c.getName()+ "_sample:"+i, ref.sizeX, ref.sizeY, ref.sizeZ);
                //TODO mettre le nom du field Attention il est null avec la methode getOneCell
                ImageStack st = im[i].getImageStack();
                for (int j = 0; j<o[i][0].length; j++) o[i][0][j].draw(st, j+1);
                im[i].show();
            }
            c.close();
        } else {
            if (Core.GUIMode) IJ.log("Sample Runner error: no cell found!!");
        }
        } catch (Exception e) {
            exceptionPrinter.print(e, "", Core.GUIMode);
        }
    }
    
    private static Object3D[][][] runCell(Cell cell, int nbSamples) {
        BasicDBList sampleChannels = cell.getExperiment().getSampleChannels();
        int nbSampleChannels = sampleChannels.size();
        Object3D[][][] objects = new Object3D[nbSampleChannels][nbSamples][];
        for (int i = 0; i< nbSampleChannels; i++) {
            BasicDBObject sampleSettings = (BasicDBObject)sampleChannels.get(i);
            Sampler s = PluginFactory.getSampler(sampleSettings.getString("method"));
            for (Parameter p : s.getParameters()) p.dbGet(sampleSettings);
            s.initSampler(cell.getRawImages(), cell.getSegmentedImages());
            for (int j = 0; j<nbSamples; j++) objects[i][j]=s.getSample();
        }
        return objects;
    }
    
    /*private static void measure(Cell c, Object3D[][][] objects) {
        MeasurementSampleSequence mss=new MeasurementSampleSequence(c.getExperiment(), c.getExperiment().getSampleMeasurements());
        if (mss!=null && !mss.isEmpty()) mss.run(c, objects);
    }
    * 
    */
    
}
