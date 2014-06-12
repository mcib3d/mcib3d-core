package tango.gui.parameterPanel;

import tango.dataStructure.VirtualStructure;
import tango.dataStructure.VirtualStructureColoc;
import tango.parameter.*;
import tango.util.IJ3dViewerParameters;
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
public class VirtualStructurePanel extends ParameterPanel {

    TextParameter name = new TextParameter("Virtual Structure Name: ", "name", "");
    public ChoiceParameter type;
    public ConditionalParameter cond;
    public ChoiceParameter color;
    public DoubleParameter pcColoc;
    public ChoiceParameter outputType;
    public StructureParameter outputStructure;
    public MultiParameter structures;
    public IntParameter objectNumber;
    public StructureParameter inputStructure;
    public StructureParameter inputStructureCluster;
    public DoubleParameter clusterThreshold;
    IJ3dViewerParameters ij3d;
    public VirtualStructurePanel() {
        super();
        
    }

    @Override
    public void getParameters() {
        type = new ChoiceParameter("Type", "type", VirtualStructure.methods, VirtualStructure.methods[0]);
        cond = new ConditionalParameter(type);
        // coloc
        name.setHelp("The name of the virtual structure", true);
        name.text.getDocument().addDocumentListener(this);
        color = new ChoiceParameter("Color: ", "color", tango.gui.util.Colors.colorNames, tango.gui.util.Colors.colorNames[this.idx + 1]);
        color.setHelp("The color to display the virtual spots", true);
        pcColoc = new DoubleParameter("min % coloc:", "pcColoc", 0.5d, Parameter.nfDEC5);
        pcColoc.setHelp("The percentage of colocalization to create virtual spots", true);
        outputType = new ChoiceParameter("Output Type:", "outputType", VirtualStructureColoc.outputMethods, VirtualStructureColoc.outputMethods[2]);
        outputType.setHelp("How is computed the virtual spot", true);
        outputStructure = new StructureParameter("Output Structure:", "outputStructure", -1, false);
        outputStructure.setHelp("If copy spots then the corresponding real structure", true);
        StructureParameter SP = new StructureParameter("Structure:", "structure", -1, false);
        SP.setHelp("The real structure to compute the virtual spots", true);
        BooleanParameter BP = new BooleanParameter("Keep segmented objects", "keep", true);
        BP.setHelp("Do we keep or erase the corrresponfing spots in real structure", true);
        Parameter[] defaultParams = new Parameter[]{SP, BP};
        structures = new MultiParameter("Structures:", "structures", defaultParams, 2, 3, 2);
        structures.setHelp("The different real structure to compute colocalised spots", true);
        ij3d = new IJ3dViewerParameters(false);
        Parameter[] colocParameters = new Parameter[]{outputType, outputStructure, pcColoc, structures};
        cond.setCondition(VirtualStructure.methods[1], colocParameters);
        
        // constant number
        objectNumber = new IntParameter("Object Number", "objectNumber", 46);
        inputStructure = new StructureParameter("Input Structure:", "inputStructure", -1, false);
        cond.setCondition(VirtualStructure.methods[0], new Parameter[]{inputStructure, objectNumber});
        
        // cluster
        clusterThreshold = new DoubleParameter("Contact Thresdhold:", "contactThld", 0.11d, Parameter.nfDEC5);
        inputStructureCluster = new StructureParameter("Mediating Structure:", "mediatingStructure", -1, false);
        cond.setCondition(VirtualStructure.methods[2], new Parameter[]{inputStructure, inputStructureCluster, clusterThreshold});
        
        parameters = new Parameter[]{name, color, cond, ij3d.getParameter()};
    }

    @Override
    public String getHelp() {
        return "Virtual Structures are Structures generated from other segmented";
    }
    
    

    @Override
    public String getMPPLabel() {
        return name.getText();
    }
    
    public String getName() {
        return name.getText();
    }
    
    @Override
    public boolean checkValidity() {
        return name.isValidOrNotCompulsary() && pcColoc.isValidOrNotCompulsary() && this.outputStructure.isValidOrNotCompulsary() && this.outputType.isValidOrNotCompulsary() && this.structures.isValidOrNotCompulsary();
    }
}
