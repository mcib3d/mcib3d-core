/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package tango.rGraphicDevice;

import java.awt.Component;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

import javax.swing.JFrame;

import org.rosuda.javaGD.GDInterface;
import org.rosuda.javaGD.JGDBufferedPanel;

/**
 *
 * @author julien
 */

public class rTangoJavaGD extends GDInterface implements WindowListener {

    JFrame f;
   
    public void gdOpen(double w, double h) {
        if (f!=null) gdClose();
        f = new JFrame("Tango Graphic Device");
        f.addWindowListener(this);      
        c = new JGDBufferedPanel(w, h);
        f.getContentPane().add((Component) c);
        f.pack();
        f.setVisible(true);
    }
   
    public void gdClose() {
        super.gdClose();
        if (f!=null) {
            c=null;
            f.removeAll();
            f.dispose();
            f=null;
        }
    }
   
    /** listener response to "Close" - effectively invokes <code>dev.off()</code> on the device */
    public void windowClosing(WindowEvent e) {
        if (c!=null) executeDevOff();
    }
    public void windowClosed(WindowEvent e) {}
    public void windowOpened(WindowEvent e) {}
    public void windowIconified(WindowEvent e) {}
    public void windowDeiconified(WindowEvent e) {}
    public void windowActivated(WindowEvent e) {}
    public void windowDeactivated(WindowEvent e) {}
}
