
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
public class DeleteSlicesOptionPane extends JOptionPane {
    public static int[] showInputDialog(final int nSlices) {
        class GetData extends JDialog implements ActionListener {
            DeleteSlicePanel panel;
            int[] res=null;
            public GetData() {
                setModal(true);
                res=null;
                panel = new DeleteSlicePanel(nSlices);
                //getContentPane().setLayout(new BorderLayout());
                getContentPane().add(panel);
                setDefaultCloseOperation(DISPOSE_ON_CLOSE);
                //setLocation(400,300);
                
                panel.buttonOk.addActionListener(this);
                panel.buttonCancel.addActionListener(this);
                pack();
                setVisible(true);
                //setPreferredSize(new Dimension(700, 500));
                //setMinimumSize(new Dimension(700, 500));
            }
            @Override
            public void actionPerformed(ActionEvent ae) {
                if(ae.getSource() == panel.buttonOk) {
                    res=new int[2];
                    res[0]=panel.sliderStart.getValue();
                    res[1]=panel.sliderStop.getValue();
                    dispose();
                } else dispose();
            }
            
            public int[] getData(){
                return res;
            }

    }
    return  new GetData().getData();
  }
  
}
