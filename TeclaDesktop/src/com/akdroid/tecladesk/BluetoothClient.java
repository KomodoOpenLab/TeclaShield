/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.akdroid.tecladesk;

import com.akdroid.interfaces.ShieldEvent;
import com.akdroid.interfaces.ShieldEventListener;
import com.akdroid.teclasocket.BluetoothEventListener;
import com.akdroid.teclasocket.TeclaSocket;
import java.io.DataInputStream;
import java.io.IOException;
import javax.swing.event.EventListenerList;

/**
 * BluetoothClient handles all aspects of bluetooth communication
 * for this desktop client for TeclaShield
 * It can be initialized using the uuid value of the service to be connected
 * BluetoothClient is responsible for starting the ConnectionSearcher,
 * PingManager and Receiver threads for non-blocking consistent UI.
 * It is also responsible for generating ShieldEvents defined in ShieldEvent.
 *  * @author Akhil
 */
public class BluetoothClient extends Thread implements Runnable{
    
    TeclaSocket sock;                //Socket for connection
    
    EventListenerList event_list;    //eventlist for generatioon of ShieldEvents
    
    PingManager pinger;              //A ping manager to manage connections
    Receiver rec;                    //Receiver for listening for input continously
    ConnectionSearcher searcher;     //Reconnection thread
    
    ShieldEvent prevclick,prevevent; //events used for generating ShieldEvent of specific type
    
    byte previousstate;              //previous state of the Shield
    
    Counter timer;                   //Counter for deciding click or double click 
                                     //to be fired as Shield Event.
    public BluetoothClient(){
        sock=null;
    }
    
    public BluetoothClient(String uuidname){ 
        
        /*
         * Initialize all contents.
         * Socket is initialized as client only for initializing.
         * scanning for devices occurs in separate thread 
         * so as the main thread doesn't block.
         * previoustate=0x3F i.e all switches released.
         */
        sock=new TeclaSocket(uuidname);   
        prevclick=new ShieldEvent(this,-1,ShieldEvent.EVENT_LONGPRESS);
        prevevent=new ShieldEvent(this,-1,ShieldEvent.EVENT_RELEASED);
        timer=new Counter();
        event_list=new EventListenerList();
        previousstate=0x3f;
    }
    
    @Override
    public void run() {
        /* 
         * The code to run in separate thread.
         * Invoke searcher to search and connect to new devices
         * Handles Bluetooth Events using a BluetoothEventListener
         * 
         */
        if(sock == null)return;
        searcher=new ConnectionSearcher(sock);                          //Search for a TeclaShield
        searcher.start();                                               // till a shield is found
        sock.addBluetoothEventListener(new BluetoothEventListener(){

            @Override
            public void onConnect() {
                /*
                 * Executes on Connected
                 */
                pinger=new PingManager(sock);          // Start
                rec=new Receiver(sock);                // Pinging
                rec.start();                           // and  
                pinger.start();                        // Receiving 
                
            }

            @Override
            public void onDisconnect() {
                /*
                 * Executes on Disconnect
                 */
                if(rec!=null)                   
                rec.end();                              //Stop receiving
                if(pinger!=null)
                pinger.end();                           //Stop pinging
                System.out.println("Disconnected"); 
                searcher=new ConnectionSearcher(sock);  // Repeatedly keep searching 
                searcher.start();                       // for TeclaShields periodically.
                
            }

            @Override
            public void onReceive(DataInputStream datain) {
                /*
                 * Executed when data is received
                 */
                try {
                    Byte b=datain.readByte();   //Read the byte received
                    resolve(b);                 //Resolve the input to their respective events
                } catch (IOException ex) {
                   System.out.println(ex.getMessage()); 
                }
                        
            }

            @Override
            public void onSent() {
                /*
                 * Executed when sending data is succcessful
                 */
            }
            
        });
    }
    public void resolve(byte b){
         /*
          * Resolves the received byte  and decides what action to take 
          * depending on the preferences 
          */
         if(b==PingManager.PING_BYTE)               // if byte received is ping
                pinger.resetcount();                // byte,reset ping counts
         else{
                int button =getButton(b);           //Get which button event occured
                if(button>-1){                      //If button number is valid 
                previousstate=b;                    //Update previous button
                setevent(b,button);                 //Fire a particular ShieldEvent
                }
         }
         
    }
    public int getButton(byte b){
        /*
         * returns the button number whose state has changed
         * if no button state is changed it will return -1
         * 
         * Only one button change can be 
         * identified using this function
         */
        
       int button = -1;                             //initialize
       byte mask=0x01;
       byte change= (byte) (b ^ previousstate);     //change from previous state
       for(int i=0;i<6;i++){                        
           if(mask<<i==change){                     //change will be equal to mask
               button=i;                            //shifted left by the button number
               break;
           }
       }    
       if(button==5)System.out.println("5");
       return button;
    }
    public int setevent(byte b,int button){
        /*
         * fires appropriate ShieldEvent for byte b depending on
         * previous state and previous special Shieldevent fired.
         * prevclick is used to hold previous special shieldevent 
         * like click and doubleclick.
         * prevevent is used to hold previous button press and button release.
         */
        
        int event=-1;
        byte mask=0x01;
        
        //Check the state of the button
        
        if((b&mask<<button)==0){ 
            //if b has a zero bit at position button ,button is in pressed state
            //fire ShieldButton Press Event
            
            prevevent=new ShieldEvent(this,button,ShieldEvent.EVENT_PRESSED);
            fireevent(prevevent);
            
        }
        else{
            //if b has a one bit at position button ,button is in released state
            long prev_time=prevevent.gettimestamp();
            int prev_button=prevevent.getbutton();
            //fire ShieldButton Release Event
            prevevent=new ShieldEvent(this,button,ShieldEvent.EVENT_RELEASED);
            fireevent(prevevent);
            
            //Firing Special Shield Events
            
            if(prevevent.gettimestamp()-prev_time>ShieldEvent.LONG_DELAY && prev_button==prevevent.getbutton())
            {
                /*
                 * If the time difference between previous event i.e a press
                 * and release is greater than Long Delay then event is a d
                 * Long Press of a Shield Button.
                 * Fire LongPress ShieldEvent
                 */
                prevclick=new ShieldEvent(this,button,ShieldEvent.EVENT_LONGPRESS);
                fireevent(prevclick);
                event=prevclick.getevent();
            }   
            else if(timer.isAlive()&&timer.getState()!=State.NEW){
                /*
                 * if Timer is running ,stop the timer 
                 * if button clicked is same as prevclick fire double-click else 
                 * fire prevclick and start a new timer.
                 */
                System.out.println("timer alive");
                timer.end();
                ShieldEvent ev=new ShieldEvent(this,button,ShieldEvent.EVENT_CLICK);
                if(ev.equals(prevclick)){
                    //fire double click
                  prevclick=new ShieldEvent(this,button,ShieldEvent.EVENT_DOUBLECLICK);
                  fireevent(prevclick);
                }
                else {
                    //fire previous click and start the timer for the current.
                    if(prevclick.getevent()==ShieldEvent.EVENT_CLICK)
                        fireevent(prevclick);
                    timer=new Counter();
                    prevclick=new ShieldEvent(this,button,ShieldEvent.EVENT_CLICK);
                    timer.start();
                }                
            }
            else {
                /*
                 * Previous timer has stopped and hence new timer should start.
                 * Timer waits for a delay equal to the maximum difference between
                 * two clicks and if it is not interrupted by end fires a click event.
                 * after counting finishes(time is elapsed uninterrupted)
                 */
                timer=new Counter();
                prevclick=new ShieldEvent(this,button,ShieldEvent.EVENT_CLICK);
                timer.start();
            }
        }
        
        return event;
    }
    public void addShieldEventListener(ShieldEventListener l){
        /*
         * Register an event listener for
         * ShieldEvents
         */
        event_list.add(ShieldEventListener.class,l);
    }
    public void removeShieldEventListener(ShieldEventListener l){
        /*
         * Unregister an event listener for
         * ShieldEvents
         */
        event_list.remove(ShieldEventListener.class,l);
    }
    
    private void fireevent(ShieldEvent e){
        /*
         * Fires a typical ShieldEvent.
         */
        Object[] listeners =event_list.getListenerList();
        // Each listener occupies two elements - the first is the listener class
        // and the second is the listener instance
        for (int i=0; i<listeners.length; i+=2) {
            if (listeners[i]==ShieldEventListener.class) {
                switch(e.getevent())
                {
                    case ShieldEvent.EVENT_PRESSED:
                        ((ShieldEventListener)listeners[i+1]).onButtonPressed(e);
                        break;
                    case ShieldEvent.EVENT_RELEASED:
                        ((ShieldEventListener)listeners[i+1]).onButtonReleased(e);
                        break;
                    case ShieldEvent.EVENT_CLICK:
                        ((ShieldEventListener)listeners[i+1]).onButtonClick(e);
                        break;
                    case ShieldEvent.EVENT_DOUBLECLICK:
                        ((ShieldEventListener)listeners[i+1]).onButtonDblClick(e);
                        break;
                    case ShieldEvent.EVENT_LONGPRESS:
                        ((ShieldEventListener)listeners[i+1]).onLongPress(e);
                        break;    
                }
            }
        }
    }
    
    
    //used for firing single click and double click events
    private class Counter extends Thread implements Runnable{
        
        int delay=0;  //Used to count delay
        boolean runflag; //Switch to run and stop
        
        public Counter(){
            delay=0;
            runflag=true;  //
        }
        @Override
        public void run() {
            while(delay<ShieldEvent.DOUBLECLICK_DELAY&&runflag)
            {
                delay++; 
                try {
                    Thread.sleep(1);        //Introduce a delay of 1 ms.
                } catch (InterruptedException ex) {
                    System.out.println(ex.getMessage());
                }
            }
            if(runflag)                 //if run wasn't interrupted  
                fireevent(prevclick);   //fire a ShieldButton Clicked Event
            
        }
        public int getdelay(){
            return delay;
        }
        public void end(){      //Interrupt the wait.
            runflag=false;
        }
    }
}
