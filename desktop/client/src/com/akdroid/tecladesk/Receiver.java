/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.akdroid.tecladesk;
import com.akdroid.teclasocket.TeclaSocket;
import java.util.logging.Level;
import java.util.logging.Logger;
/**
 * Makes the socket listening on a separate thread for data inputs 
 * over the DataInputStream of the socket.
 * @author Akhil
 */
public class Receiver extends Thread implements Runnable {
    
    TeclaSocket sock;       //socket for communication 
    boolean runf;           //Interrupt switch
    
    public Receiver(TeclaSocket t){
        sock=t;
        runf=true;
    }
    
    @Override
    public void run() {
        while(runf){
            sock.receive();  //Start listening on the socket for any received byte.
            
        }
    }
   
    public void end(){  //Interrupt the thread.
        runf=false;
    }
}
