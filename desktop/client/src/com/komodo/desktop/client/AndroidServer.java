/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.komodo.desktop.client;

import com.komodo.desktop.interfaces.ShieldEvent;
import com.komodo.desktop.interfaces.ShieldEventListener;
import com.komodo.socket.tecla.WiFiEventListener;
import com.komodo.socket.tecla.WiFiSocket;
import java.awt.AWTException;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.ClipboardOwner;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.lang.Thread.State;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.event.EventListenerList;
import org.w3c.dom.Element;

/**
 *
 * @author akhil
 */
public class AndroidServer implements Runnable,ClipboardOwner {
    
    String Password;
    WiFiSocket socket;
    byte previousstate;
    EventListenerList event_list;
    ShieldEvent prevevent,prevclick;
    Counter timer;
    public static final byte NEXT_WINDOW=(byte)0x81;
    public static final byte NEXT_FIELD=(byte) 0x82;
    WiFiReceiver rec;
    public static ShieldEvent disevent,dictevent;
    int count=0;
    boolean waitflag=false;
    
    public AndroidServer(String passcode) throws IOException{
        Password=passcode;
        socket=new WiFiSocket(passcode);
        previousstate=0x3f;
        prevclick=new ShieldEvent(this,-1,ShieldEvent.EVENT_LONGPRESS);
        prevevent=new ShieldEvent(this,-1,ShieldEvent.EVENT_RELEASED);
        timer=new Counter();
        event_list=new EventListenerList();
        
        socket.addWiFiEventListener(new WiFiEventListener(){

            @Override
            public void onClientFound() {
                
                //throw new UnsupportedOperationException("Not supported yet.");
            }

            @Override
            public void onClientConnected() {
                try {
                    socket.read();
                    socket.read();
                    GlobalVar.client_window_global.setStatus("Connected to android");
                    System.out.println("Connected");
                    rec=new WiFiReceiver(socket);
                    new Thread(rec).start();
                    //throw new UnsupportedOperationException("Not supported yet.");
                } catch (IOException ex) {
                    Logger.getLogger(AndroidServer.class.getName()).log(Level.SEVERE, null, ex);
                }
            }

            @Override
            public void onClientDisconnected() {
                
                GlobalVar.client_window_global.setStatus("Disconnected from android");
                runnewthread();
               // throw new UnsupportedOperationException("Not supported yet.");
            }

            @Override
            public void onNoClientFound() {
                
                GlobalVar.client_window_global.setStatus("No Android Clients were found");
                try {
                    Thread.sleep(600*2);
                } catch (InterruptedException ex) {
                    Logger.getLogger(AndroidServer.class.getName()).log(Level.SEVERE, null, ex);
                }
                runnewthread();
            }

            @Override
            public void onReceived(String data) {
               System.out.println("received data:"+data);
               resolve(data);
            }

            @Override
            public void onSent() {
                
            }
        });
    }
    public void setPassword(String psswd){
        socket.setPasscode(psswd);
        Element root=(Element)GlobalVar.handler.get_doc().getFirstChild();
        root.setAttribute("password",psswd);
        GlobalVar.handler.commitchanges(GlobalVar.handler.get_doc());
    }
    public String getPassword(){
        return socket.getPasscode();
    }
    @Override
    public void run() {
        if(waitflag){
            waitflag=false;
            try {
            Thread.sleep(2*60000);
            
        } catch (InterruptedException ex) {
            Logger.getLogger(AndroidServer.class.getName()).log(Level.SEVERE, null, ex);
        }
        }
        if(GlobalVar.client_window_global!=null)
          GlobalVar.client_window_global.setStatus("Looking for android clients");  
        while(!socket.connection_status)
            socket.search_connections();
        //   throw new UnsupportedOperationException("Not supported yet.");
    }
    public void runnewthread(){
               
        new Thread(this).start();
    }
    public void runnewthreadwithwait(){
        waitflag=true;
        new Thread(this).start();
    }
    
    public void addToClipboard(String dictatedtext){
        /*
         * should be called when a String with dictated text is received from android
         * copies the text to the system clipboard
         */
        StringSelection selectiontext=new StringSelection(dictatedtext);
        Clipboard clipboard =Toolkit.getDefaultToolkit().getSystemClipboard();
        clipboard.setContents(selectiontext,this);
    }

    @Override
    public void lostOwnership(Clipboard clpbrd, Transferable t) {
        Clipboard clipboard =Toolkit.getDefaultToolkit().getSystemClipboard();
        //throw new UnsupportedOperationException("Not supported yet.");
    }
    
    public void resolve(String receiveddata){
        /*
         * prefix for dictation text  ----- dictate:
         * prefix for shield commands ----- command:
         * 
         */
        if(receiveddata.equals("ping"))
            try {
            socket.write("ping");
            System.out.println("Sent ping");
            return;
        } catch (IOException ex) {
            Logger.getLogger(AndroidServer.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        String action_type=receiveddata.substring(0, receiveddata.indexOf(':'));
        String argument=receiveddata.substring(receiveddata.indexOf(':')+1);
        if(!receiveddata.equals("command:129"))
            {
            System.out.println("count=0"+ argument);
            count=0;
        }
        if(action_type.contains("dictate")){
            addToClipboard(argument);
            try {
                Robot rob=new Robot();
                synchronized(this){
                rob.keyPress(KeyEvent.VK_CONTROL);
                rob.keyPress(KeyEvent.VK_V);
                rob.keyRelease(KeyEvent.VK_V);
                rob.keyRelease(KeyEvent.VK_CONTROL);
                }
            } catch (AWTException ex) {
                Logger.getLogger(AndroidServer.class.getName()).log(Level.SEVERE, null, ex);
            }
        }else if(action_type.contains("command")){
            byte x=(byte)Integer.parseInt(argument);
            resolve(x);
        }else if(action_type.contains("disevent")){
            setdisevent(Integer.parseInt(argument));
        }else if(action_type.contains("dictevent")){
            setdictevent(Integer.parseInt(argument));
        }
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
    
    public void resolve (byte b){
        if(b==NEXT_WINDOW){    
            count++;
            if(count>10)count=1;
            System.out.println("count="+count);
            try {
                Robot rob=new Robot();
                synchronized(this){
                rob.keyPress(KeyEvent.VK_ALT);
                for(int x=0;x<count;x++){
                rob.keyPress(KeyEvent.VK_TAB);
                rob.keyRelease(KeyEvent.VK_TAB);
                }
                rob.keyRelease(KeyEvent.VK_ALT);
                }
            } catch (AWTException ex) {
                Logger.getLogger(AndroidServer.class.getName()).log(Level.SEVERE, null, ex);
            } 
        }
        else if(b==NEXT_FIELD){
            try {
                Robot rob=new Robot();
                synchronized(this){
                rob.keyPress(KeyEvent.VK_TAB);
                rob.keyRelease(KeyEvent.VK_TAB);
                }
            } catch (AWTException ex) {
                Logger.getLogger(AndroidServer.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
         else{
               // System.out.println("resolving " +b);
                int button =getButton(b);           //Get which button event occured
              //  System.out.println("button changed " +button);
                if(button>-1){                      //If button number is valid 
                previousstate=b;                    //Update previous shield state
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
                 * and release is greater than Long Delay then event is a 
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

    public void close() {
        try {
            socket.disconnect();
        } catch (IOException ex) {
            Logger.getLogger(AndroidServer.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void closeserver() {
        socket.closeserver();
    }
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
    
    public void setdisevent(int num){
        
        int button=num/10-1;
        int event=num%10-1;
        disevent=new ShieldEvent(this,button,event);
        System.out.println("setting disevent");
        
    }
    public void setdictevent(int num){
        System.out.println("setting dictevent");
        int button=num/10-1;
        int event=num%10-1;
        dictevent=new ShieldEvent(this,button,event);
    }
    public void request_dictation(){
        try {
            socket.write("dictation");
        } catch (IOException ex) {
            Logger.getLogger(AndroidServer.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
}
