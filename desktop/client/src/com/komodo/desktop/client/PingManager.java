/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.komodo.desktop.client;

import com.komodo.socket.tecla.TeclaSocket;

/**
 * Sends Ping Byte Continously and disconnects 
 * if an echo is not received after a certain number of bytes.
 * @author Akhil
 */
public class PingManager extends Thread implements Runnable {
    
    
    public static final byte PING_BYTE = 127;  //Ping Byte that is sent 0x7F
    public static final int MAX_PING_COUNTER = 5;  //Max value of count  
    public static final int DEBOUNCE_TIME =10; //Debouncing time 
    public static final int PING_DELAY =  1000;//Delay between two consecutive Pings in ms  
    
    TeclaSocket sock;   //socket for communication
    
    int count = 0;      //count to maintain the connection 
    
    boolean runflag;    //Interrupt switch
    
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
        if(count==MAX_PING_COUNTER){    
            //if count reaches MAX_PING_COUNTER ,disconnect..
            sock.disconnect();
            return;
        }   
        sock.send(PING_BYTE);
        //Send ping byte and increment
        //count is reset when the same byte i.e PING_BYTE is received
        increment();
            try {
                Thread.sleep(PING_DELAY);   //Wait for PING_DELAY
            } catch (InterruptedException ex) {
                System.out.println(ex.getMessage());
            }
            
       }
    }
    public void increment(){ //Inrements count
        count ++;
    }
    public void resetcount(){ //Resets the value of count
        count =0;
    }
    public void end(){  //Interrupt pinging
        runflag=false;
    }
    public int getcount(){ //Gives the value of count.
        return count;
    }
}
