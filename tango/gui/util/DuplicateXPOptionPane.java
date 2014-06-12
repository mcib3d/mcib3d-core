
package tango.gui.util;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;
import javax.swing.*;
import tango.gui.Core;
import tango.util.utils;
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
public class DuplicateXPOptionPane extends JOptionPane {
    public static DuplicateXP showInputDialog(final String message, final ArrayList<String> set, final String name) {
        class GetData extends JDialog implements ActionListener, ItemListener {
            JTextArea ta = new JTextArea(name, 1,12);
            JComboBox sets = new JComboBox(set.toArray());
            JButton btnNew = new JButton("  New XP  ");
            JButton btnCancel = new JButton("Cancel");
            DuplicateXP res = null;
            
            public GetData() {
                setModal(true);
                getContentPane().setLayout(new BorderLayout());
                setDefaultCloseOperation(DISPOSE_ON_CLOSE);
                setLocation(400,300);
                
                getContentPane().add(new JLabel(message),BorderLayout.NORTH);
                JPanel jpcb = new JPanel();
                jpcb.setLayout(new BoxLayout(jpcb, BoxLayout.Y_AXIS));
                
                JPanel jpChoice = new JPanel();
                jpChoice.setLayout(new BoxLayout(jpChoice, BoxLayout.Y_AXIS));
                jpChoice.add(new JLabel("Destination Folder:"));
                jpChoice.add(sets);
                jpChoice.add(new JLabel("type new Experiment name:"));
                jpChoice.add(ta);
                
                JPanel jpall = new JPanel();
                jpall.add(jpChoice);
                jpall.add(jpcb);
                getContentPane().add(jpall,BorderLayout.CENTER);
                
                JPanel jp = new JPanel();
                btnNew.addActionListener(this);
                btnCancel.addActionListener(this);

                jp.add(btnNew);
                jp.add(btnCancel);
                getContentPane().add(jp,BorderLayout.SOUTH);
                pack();
                setVisible(true);
                setPreferredSize(new Dimension(700, 500));
                setMinimumSize(new Dimension(700, 500));
            }
            public void actionPerformed(ActionEvent ae) {
                if(ae.getSource() == btnNew) {
                    res=new DuplicateXP();
                    res.xp = ta.getText();
                    res.set = utils.getSelectedString(sets);
                    dispose();
                } else dispose();
            }
            public DuplicateXP getData(){
                return res;
            }

            @Override
            public void itemStateChanged(ItemEvent ie) {
                if (ie.getStateChange()==ItemEvent.DESELECTED) return;
                
            }
            
    }
    return  new GetData().getData();
  }
  
  public static class DuplicateXP {
      public String set;
      public String xp;
  }
}
