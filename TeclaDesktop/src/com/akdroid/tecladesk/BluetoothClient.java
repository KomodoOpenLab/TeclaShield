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
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.event.EventListenerList;

/**
 *
 * @author Akhil
 */
public class BluetoothClient extends Thread implements Runnable{
    
    TeclaSocket sock;
    EventListenerList event_list;
    PingManager pinger;
    Receiver rec;
    ConnectionSearcher searcher;
    ShieldEvent prevclick,prevevent;
    byte previousstate;
    Counter timer;
    public BluetoothClient(){
        sock=null;
    }
    
    public BluetoothClient(String uuidname){
        sock=new TeclaSocket(uuidname);
        prevclick=new ShieldEvent(this,-1,ShieldEvent.EVENT_LONGPRESS);
        prevevent=new ShieldEvent(this,-1,ShieldEvent.EVENT_RELEASED);
        timer=new Counter();
    }
    
    @Override
    public void run() {
        if(sock == null)return;
        searcher=new ConnectionSearcher(sock);
        searcher.start();
        sock.addBluetoothEventListener(new BluetoothEventListener(){

            @Override
            public void onConnect() {
                pinger=new PingManager(sock);
                rec=new Receiver(sock);
                pinger.start();
                rec.start();
                //throw new UnsupportedOperationException("Not supported yet.");
            }

            @Override
            public void onDisconnect() {
                rec.end();
                pinger.end();
                searcher=new ConnectionSearcher(sock);
                searcher.start();
                //throw new UnsupportedOperationException("Not supported yet.");
            }

            @Override
            public void onReceive(DataInputStream datain) {
                try {
                    Byte b=datain.readByte();
                    resolve(b);
                } catch (IOException ex) {
                    Logger.getLogger(BluetoothClient.class.getName()).log(Level.SEVERE, null, ex);
                }
                        
            }

            @Override
            public void onSent() {
                //throw new UnsupportedOperationException("Not supported yet.");
            }
            
        });
       // throw new UnsupportedOperationException("Not supported yet.");
    }
    public void resolve(byte b){
         if(b==PingManager.PING_BYTE)
             pinger.resetcount();
         int button =getButton(b);
         if(button>-1){
             setevent(b,button);
         }
      
    }
    public int getButton(byte b){
       int button = -1;
       byte mask=0x01;
       byte change= (byte) (b ^ previousstate);
       for(int i=0;i<6;i++){
           if(mask<<i==change){
               button=i;
               break;
           }
       }    
       return button;
    }
    public int setevent(byte b,int button){
        int event=-1;
        byte mask=0x01;
        if((b&mask<<button)!=0){
            prevevent=new ShieldEvent(this,button,ShieldEvent.EVENT_RELEASED);
            fireevent(prevevent);
        }
        else{
            long prev_time=prevevent.gettimestamp();
            prevevent=new ShieldEvent(this,button,ShieldEvent.EVENT_PRESSED);
            fireevent(prevevent);
            if(prevevent.gettimestamp()-prev_time>ShieldEvent.LONG_DELAY)
            {
                prevclick=new ShieldEvent(this,button,ShieldEvent.EVENT_LONGPRESS);
                fireevent(prevclick);
                event=prevclick.getevent();
            }   
            else if(timer.isAlive()){
                timer.end();
                ShieldEvent ev=new ShieldEvent(this,button,ShieldEvent.EVENT_CLICK);
                if(ev.equals(prevclick)){
                  prevclick=new ShieldEvent(this,button,ShieldEvent.EVENT_DOUBLECLICK);
                  fireevent(prevclick);
                }
                else {
                    if(prevclick.getevent()==ShieldEvent.EVENT_CLICK)
                        fireevent(prevclick);
                    timer=new Counter();
                    prevclick=new ShieldEvent(this,button,ShieldEvent.EVENT_CLICK);
                    timer.start();
                }                
            }
            else {
                timer=new Counter();
                prevclick=new ShieldEvent(this,button,ShieldEvent.EVENT_CLICK);
                timer.start();
            }
        }
        
        return event;
    }
    public void addShieldEventListener(ShieldEventListener l){
        event_list.add(ShieldEventListener.class,l);
    }
    public void removeShieldEventListener(ShieldEventListener l){
        event_list.remove(ShieldEventListener.class,l);
    }
    
    private void fireevent(ShieldEvent e){
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
        int delay=0;
        boolean runflag;
        BluetoothClient bcl;
        public Counter(){
            delay=0;
            runflag=true;
        }
        @Override
        public void run() {
            while(delay<ShieldEvent.DOUBLECLICK_DELAY&&runflag)
            {
                delay++;
                try {
                    Thread.sleep(1);
                } catch (InterruptedException ex) {
                    Logger.getLogger(BluetoothClient.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            if(runflag)
                fireevent(prevclick);
            //throw new UnsupportedOperationException("Not supported yet.");
        }
        public int getdelay(){
            return delay;
        }
        public void end(){
            runflag=false;
        }
    }
}
