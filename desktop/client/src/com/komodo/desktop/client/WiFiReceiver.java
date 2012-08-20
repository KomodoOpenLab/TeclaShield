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
 * This class represents the receiver which continously listens to the android client for inputs
 * The listening for inputs from client occurs on a new thread.
 * @author akhil
 */
public class WiFiReceiver implements Runnable {
    
    /*
     * should be used as new Thread(new WiFiReceiver(wifi_socket)).start();
     */
    
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
        /*
         * Stop the receiver
         */
        runf=false;
    }
    
}
