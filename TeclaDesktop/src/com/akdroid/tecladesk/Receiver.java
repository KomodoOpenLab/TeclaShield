/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.akdroid.tecladesk;
import com.akdroid.teclasocket.TeclaSocket;
import java.util.logging.Level;
import java.util.logging.Logger;
/**
 *
 * @author Akhil
 */
public class Receiver extends Thread implements Runnable {
    TeclaSocket sock;
    boolean runf;
    public Receiver(TeclaSocket t){
        sock=t;
        runf=true;
    }
    
    @Override
    public void run() {
        while(runf){
            sock.receive();
            try {
                Thread.sleep(PingManager.DEBOUNCE_TIME);
            } catch (InterruptedException ex) {
                Logger.getLogger(Receiver.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
       // throw new UnsupportedOperationException("Not supported yet.");
    }
   
    public void end(){
        runf=false;
    }
}
