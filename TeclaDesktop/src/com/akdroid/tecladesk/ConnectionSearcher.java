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
public class ConnectionSearcher extends Thread implements Runnable{
    
    public static final int ATTEMPT_DELAY  = 5;
    
    TeclaSocket sock;
    
    public ConnectionSearcher(TeclaSocket sock_){
        sock=sock_;
    }
    @Override
    public void run(){
        while(!sock.isConnected())
        {
            sock.scan_devices();
            try {
                Thread.sleep(ATTEMPT_DELAY * 60000);
            } catch (InterruptedException ex) {
                Logger.getLogger(ConnectionSearcher.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
}
