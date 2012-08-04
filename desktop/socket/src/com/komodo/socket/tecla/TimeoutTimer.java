/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.komodo.socket.tecla;

import java.io.IOException;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author akhil
 */
public class TimeoutTimer extends Thread {
    
    int timeout_duration;
    int timeout_step;
    public boolean timeout;
    int elapsed=0;
    
    TimeoutTimer(int timeout,int step){
        timeout_duration=timeout;
        timeout_step=step;
        elapsed =0;
    }
    
    public void setTimeout(int value){
        timeout_duration=value;
    }
    public void setTimeoutStep(int value){
        timeout_step=value;
    }
    
    @Override
    public void run(){
        timeout=false;
        while(true){
            try {
                Thread.sleep(timeout_step);
            } catch (InterruptedException ex) {
                continue;
            }
            synchronized(this){
            elapsed=elapsed+timeout_step;
            
            if(elapsed>timeout_duration){
                timeout=true;
                System.exit(1);
            }
            }
        }
        
        
    }
    
    public synchronized void reset(){
        elapsed=0;
        timeout=false;
    }

    
    
}
