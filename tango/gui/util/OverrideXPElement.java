package tango.gui.util;

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

public class OverrideXPElement {
    public String project, xp;
    public boolean channelImages, structures, virtualStructures, measurements, samplers;
    public OverrideXPElement(String project, String xp, boolean channelFiles, boolean structures, boolean virtualStructures, boolean measurements, boolean samplers) {
        this.project=project;
        this.xp=xp;
        this.channelImages=channelFiles;
        this.structures=structures;
        this.virtualStructures=virtualStructures;
        this.measurements=measurements;
        this.samplers=samplers;
    }
    
    @Override
    public String toString() {
        return "Project:"+project+ " XP:"+xp+" ChannelImages:"+channelImages+ " Structures:"+structures+ " VirtualStructures:"+virtualStructures+ " Measurements:"+measurements+ " Samplers:"+samplers;
    }
}
