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
public class PingManager extends Thread implements Runnable {
    
    
    public static final byte PING_BYTE = 127;
    public static final int PING_COUNTER = 5;
    public static final int DEBOUNCE_TIME =10;
    public static final int PING_DELAY =  1000;
    TeclaSocket sock;
    int count = 0;
    boolean runflag;
    public PingManager(TeclaSocket sock_){
        sock=sock_;
        runflag=true;
    }
    
    @Override
    public void run(){
        ping();
    }
    public void ping(){
       while(runflag) {
        if(count==PING_COUNTER){
            sock.disconnect();
            return;
        }   
        sock.send(PING_BYTE);
       // System.out.println("ping byte sent");
        increment();
            try {
                Thread.sleep(PING_DELAY);
            } catch (InterruptedException ex) {
                System.out.println(ex.getMessage());
            }
            
       }
    }
    public void increment(){
        count ++;
    }
    public void resetcount(){
        count =0;
    }
    public void end(){
        runflag=false;
    }
    public int getcount(){
        return count;
    }
}
