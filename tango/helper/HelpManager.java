package tango.helper;

import java.awt.event.MouseListener;
import java.net.URL;
import java.util.HashMap;
import tango.gui.*;
import tango.parameter.Parameter;

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
public class HelpManager {
    public HashMap<java.awt.Component, ID> objectIDs ;
    Core core;
    public HelpManager(Core core) {
        this.core=core;
        this.objectIDs=new HashMap<java.awt.Component, ID>();
        registerComponents();
    }
    
    public void register(Helper ml) {
        for (java.awt.Component c : objectIDs.keySet()) {
            c.addMouseListener(ml);
        }
    }
    
    public void unRegister(Helper ml) {
        for (java.awt.Component c : objectIDs.keySet()) c.removeMouseListener(ml);
    }
    
    public void registerComponents() {
        Connector c = core.getConnector();
        c.registerComponents(this);
        XPEditor xpe = core.getXPEditor();
        if (xpe!=null) xpe.registerComponents(this);
        ProcessingSequenceEditor ps = core.getProcessingSequenceEditor();
        if (ps!=null) ps.registerComponents(this);
        ProcessingSequenceEditorTemplateNucleus psn = core.getProcessingSequenceEditorNucleus();
        if (psn!=null) psn.registerComponents(this);
        ProcessingSequenceEditorTemplateStructure pss = core.getProcessingSequenceEditorStructure();
        if (pss!=null) pss.registerComponents(this);
        FieldManager fm = core.getFieldManager();
        if (fm!=null) fm.registerComponents(this);
        CellManager cm = core.getCellManager();
        if (cm!=null) cm.registerComponents(this);
    }
    
    public String getHelp(Object o) {
        ID id = objectIDs.get(o);
        //System.out.println("mouse over:"+id);
        if (id!=null) {
            return Core.mongoConnector.getHelp(id);
        } else return "";
    }
    
    public void retrieveHelpFromWeb() {
        RetrieveHelp rh = new RetrieveHelp();
        rh.retrieveFromWeb();
        for (ID id : this.objectIDs.values()) {
            String help = rh.getHelp(id);
            if (help!=null) Core.mongoConnector.setHelp(id, help);
        }
    }
    
}


