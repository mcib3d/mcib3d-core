/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package tango.gui.util;

import java.awt.*;
import java.awt.geom.AffineTransform;
import javax.swing.*;

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
 * @author Santhosh Kumar http://www.jroller.com/santhosh/date/20050617#adobe_like_tabbedpane_in_swing
 */


public class VerticalTextIcon implements Icon, SwingConstants{ 
    private Font font = UIManager.getFont("Label.font"); 
    private FontMetrics fm = Toolkit.getDefaultToolkit().getFontMetrics(font); 
 
    private String text; 
    private int width, height; 
    private boolean clockwize; 
 
    public VerticalTextIcon(String text, boolean clockwize){ 
        this.text = text; 
        width = SwingUtilities.computeStringWidth(fm, text); 
        height = fm.getHeight(); 
        this.clockwize = clockwize; 
    } 
 
    @Override
    public void paintIcon(Component c, Graphics g, int x, int y){ 
        Graphics2D g2 = (Graphics2D)g; 
        Font oldFont = g.getFont(); 
        Color oldColor = g.getColor(); 
        AffineTransform oldTransform = g2.getTransform(); 
 
        g.setFont(font); 
        g.setColor(Color.black); 
        if(clockwize){ 
            g2.translate(x+getIconWidth(), y); 
            g2.rotate(Math.PI/2); 
        }else{ 
            g2.translate(x, y+getIconHeight()); 
            g2.rotate(-Math.PI/2); 
        } 
        g.drawString(text, 0, fm.getLeading()+fm.getAscent()); 
 
        g.setFont(oldFont); 
        g.setColor(oldColor); 
        g2.setTransform(oldTransform); 
    } 
    @Override
    public int getIconWidth(){ 
        return height; 
    } 
    @Override
    public int getIconHeight(){ 
        return width; 
    }
    
    public static void addTab(JTabbedPane tabPane, String text, Component comp){ 
        int tabPlacement = tabPane.getTabPlacement(); 
        switch(tabPlacement){ 
            case JTabbedPane.LEFT: 
            case JTabbedPane.RIGHT: 
                tabPane.addTab(null, new VerticalTextIcon(text, tabPlacement==JTabbedPane.RIGHT), comp); 
                return; 
            default: 
                tabPane.addTab(text, null, comp); 
        } 
    }
    
    public static JTabbedPane createTabbedPane(int tabPlacement){ 
        switch(tabPlacement){ 
            case JTabbedPane.LEFT: 
            case JTabbedPane.RIGHT: 
                Object textIconGap = UIManager.get("TabbedPane.textIconGap"); 
                Insets tabInsets = UIManager.getInsets("TabbedPane.tabInsets"); 
                UIManager.put("TabbedPane.textIconGap", new Integer(1)); 
                UIManager.put("TabbedPane.tabInsets", new Insets(tabInsets.left, tabInsets.top, tabInsets.right, tabInsets.bottom)); 
                JTabbedPane tabPane = new JTabbedPane(tabPlacement); 
                UIManager.put("TabbedPane.textIconGap", textIconGap); 
                UIManager.put("TabbedPane.tabInsets", tabInsets); 
                return tabPane; 
            default: 
                return new JTabbedPane(tabPlacement); 
        } 
    } 
}
