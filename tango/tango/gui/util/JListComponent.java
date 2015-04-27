
package tango.gui.util;

import javax.swing.Icon;
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
public class JListComponent extends java.awt.Component{
    private String label;
    private Tag tag;
    private Icon thumbnail;
    
    public JListComponent(String label, Tag tag, Icon thumbnail) {
        this.label=label;
        this.tag=tag;
        this.thumbnail=thumbnail;
    }
    
    public void setTag(int tag) {
        this.tag.setTag(tag);
    }
    
    public int getTag() {
        return tag.getTag();
    }
    
    public String getLabel() {
        return label;
    }
    
    public Icon getThumbnail() {
        return thumbnail;
    }
    
    @Override
    public String toString() {
        return label;
    }
}
