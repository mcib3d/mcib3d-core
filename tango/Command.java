package tango;

import tango.dataStructure.Experiment;

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

public class Command {
    String project, experiment;
    boolean processNuc, crop, processStructures, measurements, overrideMeasurements;
    public Command(String project, String experiment, boolean processNuc, boolean crop, boolean processStructures, boolean measurements, boolean overrideMeasurements){
        this.project=project;
        this.experiment=experiment;
        this.processNuc=processNuc;
        this.crop=crop;
        this.processStructures=processStructures;
        this.measurements=measurements;
        this.overrideMeasurements=overrideMeasurements;
    }
    
    @Override
    public String toString(){
        String res = "Project: "+project + " Experiment:"+experiment;
        if (processNuc) res= res+" process nuclei/";
        if (crop) res= res+" crop/";
        if (processStructures) res= res+" process structures/";
        if (measurements) {
            if (overrideMeasurements) res = res + " override measurements/";
            else res = res + " measurements/";
        }
        return res;
    }
}
