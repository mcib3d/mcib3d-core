package tango.parameter;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import ij.gui.GenericDialog;
import java.awt.Color;
import java.awt.GridLayout;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.border.BevelBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import tango.gui.util.CollapsiblePanel;
import tango.gui.util.Refreshable;
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

public class MultiParameter extends Parameter implements ChangeListener, NestedParameter {
    Parameter[] defaultParameters;
    SpinnerParameter nb;
    ArrayList<Parameter[]> parameters;
    int curNb;
    Box mainBox;
    public MultiParameter(String label, String id, Parameter[] defaultParameters, int minNb, int maxNb, int defaultNb) {
        super(id);
        this.defaultParameters=defaultParameters;
        mainBox = Box.createVerticalBox();
        mainBox.setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED));
        nb=new SpinnerParameter("nb of "+label, "nb", minNb, maxNb, defaultNb);
        this.label=this.nb.label;
        curNb=nb.getValue();
        nb.spinner.addChangeListener(this);
        nb.addToContainer(mainBox);
        nb.setParent(this);
        box=new CollapsiblePanel(this.label);
        box.add(mainBox);
        this.majPanel();
        setColor();
    }
    
    public SpinnerParameter getSpinner() {
        return nb;
    }
    
    @Override
    public Parameter duplicate(String newLabel, String newId) {
        Parameter[] dup = new Parameter[defaultParameters.length];
        for (int i = 0; i<dup.length; i ++) dup[i]=defaultParameters[i].duplicate(defaultParameters[i].getLabel(), defaultParameters[i].getId());
        return new MultiParameter(newLabel, newId, dup, nb.minValue, nb.maxValue, nb.getValue());
    }
    
    @Override
    public void toggleVisibility(boolean visible) {
        ((CollapsiblePanel)box).toggleVisibility(visible);
    }
    
    @Override
    public void dbPut(DBObject dbo) {
        BasicDBObject subDBO= new BasicDBObject();
        nb.dbPut(subDBO);
        
        if (parameters!=null) {
            BasicDBList list = new BasicDBList();
            for (int i = 0; i< parameters.size(); i++) {
                Parameter[] ps=parameters.get(i);
                BasicDBObject subSubDBO=new BasicDBObject();
                for (Parameter p : ps) p.dbPut(subSubDBO);
                list.add(i, subSubDBO);
            }
            subDBO.append("parameters", list);
        }
        dbo.put(id, subDBO);
    }

    @Override
    public void dbGet(BasicDBObject dbo) {
        BasicDBObject subDBO= (BasicDBObject)dbo.get(id);
        if (subDBO==null) return;
        nb.dbGet(subDBO);
        majParameters(nb.getValue());
        Object o = subDBO.get("parameters");
        if (o!=null) {
            BasicDBList list = (BasicDBList)o;
            for (int i = 0; i< parameters.size(); i++) {
                Parameter[] ps=parameters.get(i);
                BasicDBObject subSubDBO=(BasicDBObject)list.get(i);
                for (Parameter p : ps) p.dbGet(subSubDBO);
            }
        }
        majPanel();
    }
    
    protected void majPanel() {
        if (parameters!=null) {
            for (Parameter[] ps  : parameters) if (ps!=null) for (Parameter p  : ps) if (p!=null) p.removeFromContainer(mainBox);
            if (ml!=null) unRegister();
        }
        majParameters(nb.getValue());
        if (parameters!=null) {
            for (Parameter[] ps  : parameters) if (ps!=null) for (Parameter p  : ps) if (p!=null) {p.addToContainer(mainBox);}
            if (ml!=null) register();
        }
        mainBox.revalidate();
        mainBox.repaint();
        box.repaint();
        setColor();
    }
    
    protected void majParameters(int nb) {
        if (parameters==null) {
            parameters=new ArrayList<Parameter[]>(nb);
            for (int i = 0; i<nb; i++) {
                parameters.add(createParameters(i));
            }
        } else if (parameters.size()<nb) {
            int size = parameters.size();
            for (int i = size; i<nb; i++) parameters.add(createParameters(i));
        } else if (parameters.size()>nb) {
            int size = parameters.size();
            for (int i = size-1; i>=nb; i--) parameters.remove(i);
        }
    }
    
    protected Parameter[] createParameters(int idx) {
        Parameter[] res= new Parameter[defaultParameters.length];
        Parameter[] templateArray=null;
        if (template instanceof MultiParameter) {
            MultiParameter mt=(MultiParameter)template;
            if (mt.getParametersArrayList().size()>idx) {
                templateArray=mt.getParametersArrayList().get(idx);
                if (templateArray.length!=res.length) templateArray=null;
            }
        }
        for (int i = 0; i<res.length; i++) {
            res[i]=defaultParameters[i].duplicate("#"+(idx+1)+ ": "+defaultParameters[i].getLabel(), defaultParameters[i].id);
            res[i].setParent(this);
            if (templateArray!=null) res[i].setTemplate(templateArray[i]);
        }
        return res;
    }
    
    public ArrayList<Parameter[]> getParametersArrayList() {
        return parameters;
    }
    
    public int getNbParameters() {
        return nb.getValue();
    }


    /*@Override
    public void refresh() {
        majPanel();
        //if (parameters!=null) for (ArrayList<Parameter> alp  : parameters) if (alp!=null) for (Parameter p  : alp) if (p!=null && p instanceof Refreshable) ((Refreshable)p).refresh();
    }
    * 
    */

    @Override
    public void stateChanged(ChangeEvent ce) {
        if (curNb==0 || curNb!= nb.getValue()) {
            curNb= nb.getValue();
            majPanel();
            //if (displayer!=null) displayer.refreshDisplay();
        }
    }

    @Override
    public void addToGenericDialog(GenericDialog gd) {
    }
    
    @Override 
    protected void register() {
        label.addMouseListener(this);
        box.addMouseListener(this);
        if (parameters!=null) for (Parameter[] ps  : parameters) if (ps!=null) for (Parameter p  : ps) if (p!=null) p.register(ml);
    }
    
    @Override 
    protected void unRegister() {
        label.removeMouseListener(this);
        box.removeMouseListener(this);
        if (parameters!=null) for (Parameter[] ps  : parameters) if (ps!=null) for (Parameter p  : ps) if (p!=null) p.unRegister(ml);
    }

    @Override
    public Parameter[] getParameters() {
        if (parameters==null) return null;
        Parameter[] p= new Parameter[this.parameters.size()*this.defaultParameters.length];
        int off=0;
        for (Parameter[] ps  : parameters) {
            System.arraycopy(ps, 0, p, off, ps.length);
            off+=ps.length;
        }
        return p;
    }
    
    @Override
    public void refresh() {
        if (parameters==null) return;
        for (Parameter[] p : parameters) if (p!=null) for (Parameter pp:p) if (pp instanceof Refreshable) ((Refreshable)pp).refresh();
        setColor();
    }

    @Override
    public boolean sameContent(Parameter p) {
        if (p instanceof MultiParameter) {
            MultiParameter mp = (MultiParameter)p;
            if (mp.getNbParameters()==getNbParameters()) {
                ArrayList<Parameter[]> al1=getParametersArrayList();
                ArrayList<Parameter[]> al2=mp.getParametersArrayList();
                for (int i = 0; i<getNbParameters(); i++) {
                    if (!sameValue(al1.get(i), al2.get(i))) return false;
                }
                return true;
            } else return false;
        } else return false;
    }
    
    @Override
    public void setContent(Parameter p) {
        if (p instanceof MultiParameter) {
            MultiParameter mp = (MultiParameter)p;
            majParameters(mp.getNbParameters());
            ArrayList<Parameter[]> al1=getParametersArrayList();
            ArrayList<Parameter[]> al2=mp.getParametersArrayList();
            for (int i = 0; i<getNbParameters(); i++) {
                setContent(al1.get(i), al2.get(i));
            }
        } 
    }
    
    @Override
    public void setTemplate(Parameter template) {
        if (template instanceof MultiParameter) {
            super.setTemplate(template);
            MultiParameter mt = (MultiParameter) template;
            this.nb.setTemplate(mt.nb);
            for (int i = 0; i<Math.min(mt.getParametersArrayList().size(), getParametersArrayList().size()); i++) {
                Parameter[] ppt = mt.getParametersArrayList().get(i);
                Parameter[] pp = getParametersArrayList().get(i);
                if (pp!=null && ppt!=null && pp.length==ppt.length) {
                    for (int j = 0;j<pp.length;j++) pp[j].setTemplate(ppt[j]);
                }
            }
        }
        else if (parameters!=null && template==null) {
            for (int i = 0; i<getParametersArrayList().size(); i++) {
                Parameter[] pp = getParametersArrayList().get(i);
                if (pp!=null) {
                    for (int j = 0;j<pp.length;j++) pp[j].setTemplate(null);
                }
            }
        }
        this.template=template;
        setColor();
    }
    
    @Override
    public boolean isValid() {
        if (!this.compulsary) return true;
        if (!nb.isValidOrNotCompulsary()) return false;
        for (Parameter[] pp : getParametersArrayList()) if (pp!=null) for (Parameter p : pp) if (!p.isValidOrNotCompulsary()) return false;
        return true;
    }
    
}
