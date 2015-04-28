package tango.spatialStatistics.util;

import java.util.ArrayList;
import java.util.Random;
import mcib3d.geom.Point3D;
import mcib3d.image3d.ImageFloat;
import mcib3d.image3d.ImageInt;

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

public class CellPopulation {
    ArrayList<SubCell> cells;
    ImageInt mask;
    ImageFloat dm; 
    double resXY;
    Random randomGenerator;
    Distance[] distArray;
    public CellPopulation(int cellSize, ImageInt mask, ImageFloat dm, double resXY, Random randomGenerator) {
        int curXMin=0,curXMax=0,curYMin=0,curYMax=0,curZMin=0,curZMax=0;
        this.mask=mask;
        this.dm=dm;
        this.resXY=resXY;
        this.randomGenerator=randomGenerator;
        cells=new ArrayList<SubCell>();
        for (int z = 0; z<=mask.sizeZ/cellSize; z++) {
            curZMin=z*cellSize;
            if ((z+1)*cellSize<mask.sizeZ) curZMax=(z+1)*cellSize;
            else curZMax=mask.sizeZ;
            for (int y =0; y<=mask.sizeY/cellSize; y++) {
                curYMin=y*cellSize;
                if ((y+1)*cellSize<mask.sizeY) curYMax=(y+1)*cellSize;
                else curYMax=mask.sizeY;
                for (int x =0; x<=mask.sizeX/cellSize; x++) {
                    curXMin=x*cellSize;
                    if ((x+1)*cellSize<mask.sizeX) curXMax=(x+1)*cellSize;
                    else curXMax=mask.sizeX;
                    SubCell sc = new SubCell(curXMin, curXMax, curYMin, curYMax, curZMin, curZMax, this);
                    //ij.IJ.log(""+sc);
                    if (sc.dmin!=Double.MAX_VALUE) cells.add(sc);
                }
            }
        }
        distArray=new Distance[cellSize*cellSize*cellSize];
    }
    
    public Point3D drawPoint(float distance) {
        int cellIdx = randomGenerator.nextInt(cells.size());
        while (!cells.get(cellIdx).isValidCandidate(distance)) cellIdx = randomGenerator.nextInt(cells.size());
        return cells.get(cellIdx).draw(distance, distArray);
    }
    
}
