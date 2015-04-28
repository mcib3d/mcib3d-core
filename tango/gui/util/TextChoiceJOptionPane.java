
package tango.gui.util;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextArea;
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
public class TextChoiceJOptionPane extends JOptionPane
{
  public static String[] showInputDialog(final String message, final ArrayList<String> choice)
  {
    String[] data = null;
    class GetData extends JDialog implements ActionListener
    {
      JTextArea ta = new JTextArea(1,12);
      JComboBox cb = new JComboBox(choice.toArray());
      JButton btnOK = new JButton("   OK   ");
      JButton btnCancel = new JButton("Cancel");
      String[] res = null;
      public GetData()
      {
        setModal(true);
        getContentPane().setLayout(new BorderLayout());
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setLocation(400,300);
        getContentPane().add(new JLabel(message),BorderLayout.NORTH);
        getContentPane().add(ta,BorderLayout.WEST);
        getContentPane().add(cb,BorderLayout.EAST);
        JPanel jp = new JPanel();
        btnOK.addActionListener(this);
        btnCancel.addActionListener(this);
        
        jp.add(btnOK);
        jp.add(btnCancel);
        getContentPane().add(jp,BorderLayout.SOUTH);
        pack();
        setVisible(true);
      }
      public void actionPerformed(ActionEvent ae)
      {
        if(ae.getSource() == btnOK) {
            res=new String[2];
            res[0] = ta.getText();
            res[1] = (String)cb.getSelectedItem();
        }
        dispose();
      }
      public String[] getData(){return res;}
    }
    data = new GetData().getData();
    return data;
  }
}
