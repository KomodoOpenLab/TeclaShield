/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.komodo.desktop.client;

import com.komodo.socket.tecla.WiFiSocket;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author akhil
 */
public class WiFiReceiver implements Runnable {
    
    WiFiSocket sock;
    boolean runf;
    public WiFiReceiver(WiFiSocket sock){
        this.sock=sock;
        runf=true;
    }

    @Override
    public void run() {
        while(runf)
            try {
            sock.read();
        } catch (IOException ex) {
            stop();
            try {
                sock.disconnect();
            } catch (IOException ex1) {
                Logger.getLogger(WiFiReceiver.class.getName()).log(Level.SEVERE, null, ex1);
            }
            Logger.getLogger(WiFiReceiver.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public void stop(){
        runf=false;
    }
    
}
