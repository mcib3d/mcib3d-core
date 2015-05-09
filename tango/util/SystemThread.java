/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tango.util;

import java.lang.reflect.InvocationTargetException;
import javax.swing.SwingUtilities;

/**
 *
 * @author julien
 */
public class SystemThread extends Thread{
    public SystemTask st;
    SystemThread(SystemTask st){
        this.st = st;
    }
    @Override
    public void run() {
        try {
            SwingUtilities.invokeAndWait(st);
        }
        catch (InterruptedException e) {
            
        } catch (InvocationTargetException e) {
            
        }
    }
}
