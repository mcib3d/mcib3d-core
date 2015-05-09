package tango.gui.parameterPanel;

import tango.helper.HelpManager;
import tango.helper.ID;
import tango.helper.RetrieveHelp;
import tango.parameter.IntParameter;
import tango.parameter.Parameter;
import tango.parameter.PreFilterSequenceParameter;
import tango.parameter.TextParameter;
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
public class ChannelImagePanel extends ParameterPanel {

    TextParameter keyword;
    PreFilterSequenceParameter preFilter;

    public ChannelImagePanel() {
        super();
    }

    @Override
    public void getParameters() {
        keyword = new TextParameter("Keyword:", "keyword", "" + (idx + 1));
        keyword.setHelp("keword present in the file name to identify it. \nCase sensitive. \nUsed only with the keyword import file method", true);
        keyword.text.getDocument().addDocumentListener(this);
        preFilter=new PreFilterSequenceParameter("Pre-Filters:", "preFilters");
        preFilter.setHelp("Sequence of pre-filters that will be applied on the whole field image before cropping nuclei", true);
        parameters = new Parameter[]{keyword, preFilter};
    }

    @Override
    public String getHelp() {
        return "Define the channel images of your experiment";
    }

    public void registerComponents(HelpManager hm) {
        // folders
        hm.objectIDs.put(this.getPanel(), new ID(RetrieveHelp.editXPPage, "Channels"));
    }

    @Override
    public String getMPPLabel() {
        return keyword.getText();
    }
    
    public String getName() {
        return keyword.getText();
    }

    @Override
    public boolean checkValidity() {
        return keyword.isValidOrNotCompulsary() && preFilter.isValidOrNotCompulsary();
    }
}
