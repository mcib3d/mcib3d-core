package tango.helper;

import ij.plugin.frame.PlugInFrame;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import javax.swing.*;
import mcib3d.utils.exceptionPrinter;
import tango.gui.*;
import tango.gui.util.ContextMenuMouseListener;
import tango.gui.parameterPanel.ParameterPanelAbstract;
import tango.gui.parameterPanel.ParameterPanelPlugin;
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
public class Helper extends PlugInFrame implements MouseListener, ActionListener  {
    Core core;
    HelpManager helpManager;
    JToggleButton level;
    JButton retrieveHelp;
    Object currentObject;
    JEditorPane text;
    JScrollPane scrollPane;
    final static int x=200;
    final static int y=200;
    public Helper(Core core) {
        super("Tango Helper");
        this.core=core;
        this.helpManager=new HelpManager(core);
        level=new JToggleButton("Advanced Help Level", false);
        level.addActionListener(this);
        retrieveHelp = new JButton("Update Help");
        retrieveHelp.addActionListener(this);
        text = new JEditorPane();
        text.setEditable(false);
        ContextMenuMouseListener cml = new ContextMenuMouseListener();
        text.addMouseListener(cml);
        text.setMaximumSize(new Dimension(x, 30000));
        text.setContentType("text/html");
        scrollPane = new JScrollPane(text);
        scrollPane.setPreferredSize(new Dimension(x+20, y+20));
        scrollPane.setMinimumSize(new Dimension(x+20, y+20));
        scrollPane.setVerticalScrollBarPolicy( JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        JPanel buttonPanel = new JPanel();
        buttonPanel.add(level);
        buttonPanel.add(retrieveHelp);
        setLayout(new BorderLayout());
        add(buttonPanel,BorderLayout.SOUTH);
        add(scrollPane, BorderLayout.CENTER);
        pack();
        ij.gui.GUI.center(this);
        setVisible(true);
        register();
    }
    
    public void registerComponents() {
        helpManager.registerComponents();
    }
    
    public HelpManager getHelpManager() {
        return helpManager;
    }
    
    private void refreshText() {
        if (currentObject!=null) {
            if (currentObject instanceof Parameter) {
                text.setText(((Parameter) currentObject).getHelp(!level.isSelected()));
            } else if (currentObject instanceof ParameterPanelAbstract) {
                text.setText(((ParameterPanelAbstract) currentObject).getHelp());
            }
            else {
                try {
                    String s = this.helpManager.getHelp(currentObject);
                    //System.out.println(s);
                    if (s!=null) text.setText(s);
                } catch (Exception e) {
                    exceptionPrinter.print(e, "", Core.GUIMode);
                }
            }
            refreshDisplay();
        }
    }
    
    private void refreshDisplay()  {
        text.revalidate();
        text.repaint();
        scrollPane.revalidate();
        scrollPane.repaint();
    }
    
    public void register() {
        helpManager.register(this);
        core.getConnector().options.register(this);
        XPEditor xpe=core.getXPEditor();
        if (xpe!=null) xpe.register(this);
        ProcessingSequenceTemplateEditor psn = core.getProcessingSequenceEditorNucleus();
        if (psn!=null) psn.register(this);
        ProcessingSequenceTemplateEditor pss = core.getProcessingSequenceEditorStructure();
        if (pss!=null) pss.register(this);
        ProcessingSequenceEditor ps = core.getProcessingSequenceEditor();
        if (ps!=null) ps.register(this);
    }
    
    private void unRegister() {
        helpManager.unRegister(this);
        core.getConnector().options.unRegister(this);
        if (core==null) return;
        XPEditor xpe=core.getXPEditor();
        if (xpe!=null) xpe.unRegister(this);
        ProcessingSequenceTemplateEditor psn = core.getProcessingSequenceEditorNucleus();
        if (psn!=null) psn.unRegister(this);
        ProcessingSequenceTemplateEditor pss = core.getProcessingSequenceEditorStructure();
        if (pss!=null) pss.unRegister(this);
        ProcessingSequenceEditor ps = core.getProcessingSequenceEditor();
        if (ps!=null) ps.unRegister(this);
    }
    
    
    @Override
    public void close() {
        unRegister();
        Core.helper=null;
        dispose();
    }

    @Override
    public void mouseClicked(MouseEvent me) {
        
    }

    @Override
    public void mousePressed(MouseEvent me) {
        
    }

    @Override
    public void mouseReleased(MouseEvent me) {
        
    }

    @Override
    public void mouseEntered(MouseEvent me) {
        Object source = me.getSource();
        if (source!=currentObject) {
            currentObject=source;
            refreshText();
        }
    }

    @Override
    public void mouseExited(MouseEvent me) {
        
    }

    @Override
    public void actionPerformed(ActionEvent ae) {
        Object source = ae.getSource();
        if (source == this.level) {
            refreshText();
        } else if (source==this.retrieveHelp) {
            helpManager.retrieveHelpFromWeb();
            refreshText();
        }
    }
}
