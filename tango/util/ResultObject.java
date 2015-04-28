/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tango.util;

import ij.IJ;
import java.util.ArrayList;
import java.util.Iterator;

/**
 *
 * @author julien
 */
class ResultObject {
    boolean result;
    ArrayList<String> output;
    ArrayList<String> error;
    
    ResultObject(boolean result){
        this.result = result;
        this.output = new ArrayList<String>();
        this.error = new ArrayList<String>();
    }
    
    synchronized void logResult(){
        if(result) IJ.log("Command executed succesfully");
        else IJ.log("Command failed");
    }

    synchronized void logOutput() {
        Iterator<String> it = output.iterator();
        while(it.hasNext()){
            IJ.log(it.next());
        }
    }

    synchronized void logError() {
        Iterator<String> it = error.iterator();
        while(it.hasNext()){
            IJ.log(it.next());
        }
    }

    synchronized void addError(String e) {
        error.add(e);
    }

    synchronized void setResult(boolean r) {
        result = r;
    }

   synchronized  void addOutput(String o) {
        output.add(o);
    }
   
   synchronized boolean getResult(){
       return result;
   }
}
