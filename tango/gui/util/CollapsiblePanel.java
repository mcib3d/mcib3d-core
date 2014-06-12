package tango.gui.util;

    import java.awt.BorderLayout;  
    import java.awt.Color;
    import java.awt.Component;  
    import java.awt.Dimension;
    import java.awt.event.ComponentAdapter;  
    import java.awt.event.ComponentEvent;  
    import java.awt.event.ComponentListener;  
    import java.awt.event.MouseAdapter;  
    import java.awt.event.MouseEvent;  
    import java.awt.event.MouseListener;  
    import java.beans.PropertyChangeEvent;
    import java.beans.PropertyChangeListener;
    import javax.swing.BorderFactory;  
    import javax.swing.JLabel;
    import javax.swing.JPanel;  
    import javax.swing.border.TitledBorder;  
    import tango.gui.PanelDisplayer;
      
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
 * @author rgd from http://www.coderanch.com/t/341737/GUI/java/Expand-Collapse-Panels
 * @author Jean Ollion (adaptation of the original class)
 */

public class CollapsiblePanel extends JPanel {  
    String title = "";
    TitledBorder border;  
    JLabel label;
    public CollapsiblePanel(JLabel label_) {  
        this.title=label_.getText();
        this.label=label_;
        border = BorderFactory.createTitledBorder(title);  
        this.label.addPropertyChangeListener(
            new PropertyChangeListener() {
                @Override
                public void propertyChange(PropertyChangeEvent evt) {
                    border.setTitleColor(label.getForeground());
                }
        });
        setBorder(border);  
        BorderLayout borderLayout = new BorderLayout();  
        setLayout(borderLayout);  
        addMouseListener(mouseListener);
    }  

    MouseListener mouseListener = new MouseAdapter() {  
        @Override  
        public void mouseClicked(MouseEvent e) {  
            toggleVisibility();  
        }  
    };  

    ComponentListener contentComponentListener = new ComponentAdapter() {  
        @Override  
        public void componentShown(ComponentEvent e) {  
            updateBorderTitle();  
        }  
        @Override  
        public void componentHidden(ComponentEvent e) {  
            updateBorderTitle();  
        }  
    };  

    public String getTitle() {  
        return title;  
    }  

    public void setTitle(String title) {  
        firePropertyChange("title", this.title, this.title = title);  
    }
    

    @Override  
    public Component add(Component comp) {  
        comp.addComponentListener(contentComponentListener);  
        Component r = super.add(comp);  
        updateBorderTitle();  
        return r;  
    }  

    @Override  
    public Component add(String name, Component comp) {  
        comp.addComponentListener(contentComponentListener);  
        Component r = super.add(name, comp);  
        updateBorderTitle();  
        return r;  
    }  

    @Override  
    public Component add(Component comp, int index) {  
        comp.addComponentListener(contentComponentListener);  
        Component r = super.add(comp, index);  
        updateBorderTitle();  
        return r;  
    }  

    @Override  
    public void add(Component comp, Object constraints) {  
        comp.addComponentListener(contentComponentListener);  
        super.add(comp, constraints);  
        updateBorderTitle();  
    }  

    @Override  
    public void add(Component comp, Object constraints, int index) {  
        comp.addComponentListener(contentComponentListener);  
        super.add(comp, constraints, index);  
        updateBorderTitle();  
    }  

    @Override  
    public void remove(int index) {  
        Component comp = getComponent(index);  
        comp.removeComponentListener(contentComponentListener);  
        super.remove(index);  
    }  

    @Override  
    public void remove(Component comp) {  
        comp.removeComponentListener(contentComponentListener);  
        super.remove(comp);  
    }  

    @Override  
    public void removeAll() {  
        for (Component c : getComponents()) {  
            c.removeComponentListener(contentComponentListener);  
        }  
        super.removeAll();  
    }  

    protected void toggleVisibility() {  
        toggleVisibility(hasInvisibleComponent());  
    }  

    public void toggleVisibility(boolean visible) {  
        for (Component c : getComponents()) {  
            c.setVisible(visible);  
        }  
        updateBorderTitle();  
    }  

    protected void updateBorderTitle() {  
        String arrow = "";  
        if (getComponentCount() > 0) {  
            boolean b=hasInvisibleComponent();
            arrow = (b?"+":"~");  
            //arrow = (hasInvisibleComponent()?"▽":"△");  
            if (b) setPreferredSize(new Dimension(this.getSize().width, 20));
            else setPreferredSize(null);
            
        }  
        border.setTitle(arrow +" "+ title+" ");
        
        
        repaint();  
    }  

    protected final boolean hasInvisibleComponent() {  
        for (Component c : getComponents()) {  
            if (!c.isVisible()) {  
                return true;  
            }  
        }  
        return false;  
    }  

}  