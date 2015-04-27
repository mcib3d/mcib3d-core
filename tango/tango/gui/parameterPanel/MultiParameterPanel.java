package tango.gui.parameterPanel;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import mcib3d.utils.exceptionPrinter;
import ij.IJ;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import javax.swing.*;
import tango.gui.Core;
import tango.gui.PanelDisplayer;
import tango.gui.util.Displayer;
import tango.gui.util.PanelElementAbstract;
import tango.gui.util.PanelElementMono;
import tango.helper.Helper;
import tango.plugin.filter.PostFilter;
import tango.plugin.filter.PreFilter;
import tango.plugin.segmenter.NucleusSegmenter;
import tango.plugin.segmenter.SpotSegmenter;
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
public class MultiParameterPanel<T extends ParameterPanelAbstract> implements ActionListener, Displayer  {
    ArrayList<PanelElementAbstract> panelElements;
    private JPanel listPanel;
    private JButton add;
    private PanelDisplayer panelDisplayer;
    private DBObject data;
    private Class<T> clazz;
    private int maxNb, minNb;
    protected Helper ml;
    GridLayout listLayout;
    final int minNbRows;
    MultiParameterPanel<T> template;
    boolean enableTest;
    Core core;
    public MultiParameterPanel(Core core, DBObject data, int minNb, int maxNb, Dimension minimumSize, PanelDisplayer panelDisplayer, boolean enableTest, Class<T> clazz) {
        this.core=core;
        this.enableTest=enableTest;
        this.minNbRows = maxNb==1?1:Math.max(2, minimumSize.height/40);
        this.maxNb=maxNb;
        this.minNb=minNb;
        this.panelDisplayer=panelDisplayer;
        this.clazz=clazz;
        this.data=data;
        this.panelElements=new ArrayList<PanelElementAbstract>();
        listLayout = new GridLayout(0, 1, 5, 0);
        this.listPanel=new JPanel(listLayout);
        this.listPanel.setMinimumSize(minimumSize);
        this.add=new JButton("Add");
        if (maxNb>1) {
            Box addJP = Box.createHorizontalBox();
            addJP.add(Box.createHorizontalStrut(5));
            addJP.add(add);
            addJP.add(Box.createHorizontalGlue());
            listPanel.add(addJP);
            add.addActionListener(this);
        }
        if (this.data!=null && maxNb>1) {
            BasicDBList list = (BasicDBList)data;
            for (int i = 0; i<list.size(); i++) {
                addElement((BasicDBObject)list.get(i), i);
            }
        } else if (maxNb==1) {
            if (this.data!=null) addElement((BasicDBObject)data, 0);
            else addElement(null, 0);
        } else if (minNb>0) {
            for (int i = 0; i<minNb; i++) {
                addElement(null, i);
            }
        }
    }
    
    public void setTemplate(MultiParameterPanel<T> mpp) {
        this.template=mpp;
        for (int i = 0; i<this.panelElements.size(); i++) {
            if (panelElements.get(i) instanceof PanelElementPlugin) {
                if (mpp!=null) {
                    if (i<mpp.panelElements.size()) {
                        if (mpp.panelElements.get(i) instanceof PanelElementPlugin) {
                            ((PanelElementPlugin)panelElements.get(i)).setTemplate((PanelElementPlugin)mpp.panelElements.get(i));
                        }
                    } else ((PanelElementPlugin)panelElements.get(i)).setTemplate(null);
                } else ((PanelElementPlugin)panelElements.get(i)).removeTemplate();
            }
        }
        setAddButtonColor();
    }
    
    private void setAddButtonColor() {
        if (template!=null && this.panelElements.size()!=template.panelElements.size()) this.add.setForeground(Color.blue);
        else this.add.setForeground(Color.black);
    }
    
    public JPanel getPanel () {
        return this.listPanel;
    }
    
    public DBObject save() {
        if (maxNb==1) return saveFirst();
        else return saveAll();
    }
    
    public BasicDBList saveAll() {
        BasicDBList list = new BasicDBList();
        for (PanelElementAbstract p : panelElements) {
            if (p instanceof PanelElementPlugin) {
                if (((PanelElementPlugin)p).getParameterPanel().getMethod()==null)  ((PanelElementPlugin)p).setCurrentMethod();
            } else {
                ((ParameterPanel)p.getParameterPanel()).checkValidity();
            }
            list.add(p.getParameterPanel().save());
        }
        return list;
    }
    
    public BasicDBObject saveFirst () {
        if (panelElements.size()>0) return panelElements.get(0).getParameterPanel().save();
        else return null;
    }
    
    protected void addElement(BasicDBObject DBO, int idx) {
        try{
            PanelElementAbstract b = createPanelElement(DBO, idx);
            if (ml!=null) b.register(ml);
            panelElements.add(b);
            if (template!=null) {
                if (template.panelElements.size()>idx) {
                    if (b instanceof PanelElementPlugin) ((PanelElementPlugin)b).setTemplate((PanelElementPlugin)template.panelElements.get(idx));
                }
            }
            listPanel.add(b.getPanel());
            listLayout.setRows(Math.max(panelElements.size()+1, minNbRows));
            listPanel.revalidate();
            //scrollPane.getViewport().revalidate();
            panelDisplayer.refreshDisplay();
            setAddButtonColor();
        } catch(Exception e) {
            exceptionPrinter.print(e, "", Core.GUIMode);
        }
    }
    
    protected PanelElementAbstract createPanelElement(BasicDBObject DBO, int idx) {
        T t;
        try{
            t = clazz.newInstance();
            t.setIdx(idx);
            t.setDisplayer(panelDisplayer);
            t.setData(DBO);
            t.initPanel();
            PanelElementAbstract b;
            if (t instanceof ParameterPanel) {
                b=new PanelElementMono((ParameterPanel)t, this, maxNb>1 && idx>=minNb, idx);
            } else {
                b = new PanelElementPlugin((ParameterPanelPlugin)t, this, maxNb>1 && idx>=minNb, enableTest);
                if (template!=null && idx<template.panelElements.size()) ((PanelElementPlugin)b).setTemplate((PanelElementPlugin)template.panelElements.get(idx));
            }
            //System.out.println("panelElement null:"+(b==null)+ " idx:"+idx+ " "+DBO);
            return b;
        } catch(Exception e) {
            exceptionPrinter.print(e, "", Core.GUIMode);
        } return null;
    }
    
    public void removeElement(PanelElementAbstract b) {
        int idx = panelElements.indexOf(b);
        if (idx>=0) {
            panelElements.remove(idx);
            listPanel.remove(b.getPanel());
            listLayout.setRows(Math.max(panelElements.size()+1, minNbRows));
            listPanel.revalidate();
            listPanel.repaint();
            setAddButtonColor();
            if (ml!=null) b.unRegister(ml);
            for (int i = idx; i<panelElements.size(); i++) panelElements.get(i).setIdx(idx);
        }
        
    }
    
    public void showPanel(PanelElementAbstract b) {
        for (PanelElementAbstract b2 : panelElements) b2.off();
        b.getParameterPanel().refreshDisplay();
        panelDisplayer.showPanel(b.getParameterPanel().getPanel());
        b.on();
    }
    
    public void test(PanelElementPlugin e) {
        if (clazz.equals(PreFilterPanel.class)) {
            int subStep = this.panelElements.indexOf(e);
            core.getProcessingSequenceEditor().test(0, subStep);
        } else if (clazz.equals(PostFilterPanel.class)) {
            int subStep = this.panelElements.indexOf(e);
            core.getProcessingSequenceEditor().test(2, subStep);
        } else if (clazz.equals(NucleiSegmenterPanel.class) || clazz.equals(ChannelSegmenterPanel.class)) {
            core.getProcessingSequenceEditor().test(1, 0);
        } else if (clazz.equals(MeasurementPanel.class)) {
            int subStep = this.panelElements.indexOf(e);
            core.getCellManager().testMeasure(subStep);
        } else if (clazz.equals(SamplerPanel.class)) {
            core.getCellManager().testSampler(((SamplerPanel)e.getParameterPanel()).getSampler());
        }
    }
    
    public void hidePanel() {
        panelDisplayer.hidePanel();
    }
    
    public void allOff() {
        for (PanelElementAbstract b2 : panelElements) b2.off();
    }

    @Override
    public void actionPerformed(ActionEvent ae) {
        String command = ae.getActionCommand();
        if (command==null) return;
        else if (command.equals("Add")) {
            addElement(null, this.panelElements.size());
        }
    }
    
    public void refreshScrollPane() { //???
        //this.scrollPane.getViewport().
        //this.scrollPane.revalidate();
        //this.scrollPane.repaint();
        //this.listPanel.setPreferredSize();
        //this.listPanel.setPreferredSize(listPanel.getSize());
        this.listPanel.revalidate();
        //this.listPanel.repaint();
        panelDisplayer.refreshDisplay();
    }

    @Override
    public void refreshDisplay() {
        if (this.panelElements!=null) for (PanelElementAbstract p : panelElements) p.refreshDisplay();
    }
    
    public void refreshParameters() {
        if (this.panelElements!=null) for (PanelElementAbstract p : panelElements) if (p.getParameterPanel()!=null) p.getParameterPanel().refreshParameters();
    }
    
    public void register(Helper ml) {
        this.ml=ml;
        for (PanelElementAbstract p : panelElements) p.register(ml);
    }
    
    public void unRegister(Helper ml) {
        for (PanelElementAbstract p : panelElements) p.unRegister(ml);
    }
    
    public ParameterPanelAbstract[] getParameterPanels() {
        ParameterPanelAbstract[] res = new ParameterPanelAbstract[panelElements.size()];
        for (int i = 0; i<res.length; i++) res[i]=panelElements.get(i).getParameterPanel();
        return res;
    }
}
