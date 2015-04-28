package tango.parameter;

import java.awt.event.ActionEvent;
import javax.swing.*;
import com.mongodb.DBObject;
import com.mongodb.BasicDBObject;
import ij.gui.GenericDialog;
import java.awt.event.ActionListener;
import java.io.File;

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
public class FileParameter extends Parameter implements ActionListener {

    JButton button;
    File defaultDir, curDir;

    public FileParameter(String label, String id, File defautlDir) {
        super(label, id);
        this.button = new JButton("Browse");
        button.addActionListener(this);
        box.add(button);
    }

    @Override
    public Parameter duplicate(String newLabel, String newId) {
        return new FileParameter(newLabel, newId, defaultDir);
    }

    public JButton getButton() {
        return button;
    }

    public File getDir() {
        return curDir;
    }

    @Override
    public void dbPut(DBObject DBO) {
        if (curDir != null) {
            DBO.put(id, curDir.getAbsolutePath().replace(" ", "\\ "));
        }
    }

    @Override
    public void dbGet(BasicDBObject DBO) {
        if (DBO.containsField(id)) {
            curDir = new File(DBO.getString(id));
        }
    }

    public void fireAction() {
        final JFileChooser fc = new JFileChooser(label.getText());
        fc.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
        if (curDir != null) {
            fc.setCurrentDirectory(curDir.getParentFile());
        }
        int returnval = fc.showOpenDialog(box);
        File file = null;
        if (returnval == JFileChooser.APPROVE_OPTION) {
            file = fc.getSelectedFile();
            //if (!file.isDirectory()) file=file.getParentFile();
        }
        this.curDir = file;
    }

    @Override
    public void actionPerformed(ActionEvent ae) {
        fireAction();
        setColor();
    }

    @Override
    public void addToGenericDialog(GenericDialog gd) {
        // TODO should create a text and a browse button ;-) 
    }

    @Override
    public boolean sameContent(Parameter p) {
        return (p instanceof FileParameter && ((FileParameter) p).curDir == curDir);
    }

    @Override
    public void setContent(Parameter p) {
        // TODO
    }
}
