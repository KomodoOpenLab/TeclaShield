/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.komodo.desktop.client;

import com.komodo.socket.tecla.TeclaSocket;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * ConnectionSearcher is a Runnable class that will search and try
 * to connect to a TeclaShield.
 * In case of failure,it will attempt to reconnect periodically.
 * @author Akhil
 */
public class ConnectionSearcher extends Thread implements Runnable{
    
    public static final int ATTEMPT_DELAY  = 2; //Period for reconnecting attempt in minutes
    
    TeclaSocket sock;
    
    public ConnectionSearcher(TeclaSocket sock_){
        sock=sock_;
    }
    @Override
    public void run(){
        while(!sock.isConnected())//Check if connection is established.
        {
            sock.scan_devices();  //search and connect if found.
            GlobalVar.client_window_global.setStatus("Looking for a TeclaShield in range...");
            try {
                Thread.sleep(ATTEMPT_DELAY * 60000); //Wait for delay period
            } catch (InterruptedException ex) {
                System.out.println(ex.getMessage()); //Can be interrupted by calling interrupt function
            }
        }
    }
}
