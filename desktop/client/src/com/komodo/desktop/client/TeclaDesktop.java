/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.komodo.desktop.client;

import com.komodo.desktop.interfaces.ClientMain;
import com.komodo.desktop.interfaces.ErrorDialog;
import com.komodo.desktop.interfaces.ShieldEvent;
import com.komodo.desktop.interfaces.ShieldEventListener;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.bluetooth.BluetoothStateException;
import org.w3c.dom.Element;


/**
 * TeclaClient v1.0
 * @author Akhil
 */
public class TeclaDesktop implements ShieldEventListener{
    public static final String uuidstring = "0000110100001000800000805F9B34FB";
    public static String location = "Preferences";
    public static final String errormessage="Bluetooth is either turned off or you have not installed a bluetooth device,"
            + "Turn On and restart the application,The application will now quit";
    //public static final String configlocation = "Preferences/config.xml";
    /**
     * @param args the command line arguments
     * Tentative main file.Needs many changes.but the structure remains the same
     */
    
    BluetoothClient btclient;
    AndroidServer server;
    PreferencesHandler prefs;
    EventGenerator eventgen;
    Thread[] threads;
    ClientMain mains;
    public static void main(String[] args) {
        TeclaDesktop tdesk=new TeclaDesktop();
             
    }

    private TeclaDesktop(BluetoothClient btclient, AndroidServer server, PreferencesHandler prefs,Thread[] threads) {
        this.btclient=btclient;
        this.server=server;
        this.prefs=prefs;
        eventgen=new EventGenerator();
        this.threads=threads;
    }
    
    private TeclaDesktop(){
        location="Preferences";
        prefs=new PreferencesHandler(location);
        mains=new ClientMain(prefs,this);
        GlobalVar.setMainWindow(mains);
        mains.setVisible(true);
        threads=new Thread[6];
        eventgen=new EventGenerator();
        GlobalVar.setPreferences(prefs);
        
       
        
    }
    
                @Override
                public void onButtonPressed(ShieldEvent e) {
                   if(prefs.getChoice()==EventConstant.CONNECT_TO_ANDROID&&
                           server!=null&&AndroidServer.dictevent!=null&&e.equals(AndroidServer.dictevent)){
                       server.request_dictation();
                       return;
                   }else if(prefs.getChoice()==EventConstant.CONNECT_TO_ANDROID&&
                           server!=null&&AndroidServer.disevent!=null&&e.equals(AndroidServer.disevent)){
                       server.close();
                       return;
                   }
                   ShieldButton current=prefs.getShieldButton(e.getbutton());
                   
                   if(current!=null){
                   System.out.println("Event of press "+EventConstant.Shieldbuttons[e.getbutton()]);
                   if(current.RTR_flag){
                       threads[e.getbutton()]=new Thread(eventgen);
                       eventgen.setEvent(current.eventlist[ShieldEvent.EVENT_PRESSED]);
                       eventgen.setDelay(current.RTR_delay);
                       threads[e.getbutton()].start();
                       }            
                   eventgen.interpret(current.eventlist[ShieldEvent.EVENT_PRESSED]);
                   }
                   // throw new UnsupportedOperationException("Not supported yet.");
                }

                @Override
                public void onButtonReleased(ShieldEvent e) {
                    if(prefs.getChoice()==EventConstant.CONNECT_TO_ANDROID&&
                           server!=null&&AndroidServer.dictevent!=null&&e.equals(AndroidServer.dictevent)){
                       server.request_dictation();
                       return;
                   }else if(prefs.getChoice()==EventConstant.CONNECT_TO_ANDROID&&
                           server!=null&&AndroidServer.disevent!=null&&e.equals(AndroidServer.disevent)){
                       server.close();
                       return;
                   }
                    ShieldButton current=prefs.getShieldButton(e.getbutton());
                   if(current!=null){
                   if(current.RTR_flag && threads[e.getbutton()]!=null&&threads[e.getbutton()].isAlive())
                       eventgen.stoptrigger();
                   eventgen.interpret(current.eventlist[ShieldEvent.EVENT_RELEASED]);
                   System.out.println("Event of release "+EventConstant.Shieldbuttons[e.getbutton()]);
                   }// throw new UnsupportedOperationException("Not supported yet.");
                }

                @Override
                public void onButtonClick(ShieldEvent e) {
                    if(prefs.getChoice()==EventConstant.CONNECT_TO_ANDROID&&
                           server!=null&&AndroidServer.dictevent!=null&&e.equals(AndroidServer.dictevent)){
                       server.request_dictation();
                       return;
                   }else if(prefs.getChoice()==EventConstant.CONNECT_TO_ANDROID&&
                           server!=null&&AndroidServer.disevent!=null&&e.equals(AndroidServer.disevent)){
                       server.close();
                       return;
                   }
                    ShieldButton current=prefs.getShieldButton(e.getbutton());
                   if(current!=null)
                   eventgen.interpret(current.eventlist[ShieldEvent.EVENT_CLICK]);
                   System.out.println("Event of click "+EventConstant.Shieldbuttons[e.getbutton()]);
                   // throw new UnsupportedOperationException("Not supported yet.");
                }

                @Override
                public void onButtonDblClick(ShieldEvent e) {
                    if(prefs.getChoice()==EventConstant.CONNECT_TO_ANDROID&&
                           server!=null&&AndroidServer.dictevent!=null&&e.equals(AndroidServer.dictevent)){
                       server.request_dictation();
                       return;
                   }else if(prefs.getChoice()==EventConstant.CONNECT_TO_ANDROID&&
                           server!=null&&AndroidServer.disevent!=null&&e.equals(AndroidServer.disevent)){
                       server.close();
                       return;
                   }
                   ShieldButton current=prefs.getShieldButton(e.getbutton());
                   if(current!=null)
                   eventgen.interpret(current.eventlist[ShieldEvent.EVENT_DOUBLECLICK]);
                   System.out.println("Event of double click "+EventConstant.Shieldbuttons[e.getbutton()]);
                  //  throw new UnsupportedOperationException("Not supported yet.");
                }

               @Override
                public void onLongPress(ShieldEvent e) {
                   if(prefs.getChoice()==EventConstant.CONNECT_TO_ANDROID&&
                           server!=null&&AndroidServer.dictevent!=null&&e.equals(AndroidServer.dictevent)){
                       server.request_dictation();
                       return;
                   }else if(prefs.getChoice()==EventConstant.CONNECT_TO_ANDROID&&
                           server!=null&&AndroidServer.disevent!=null&&e.equals(AndroidServer.disevent)){
                       server.close();
                       return;
                   }
                    ShieldButton current=prefs.getShieldButton(e.getbutton());
                   if(current!=null)
                   eventgen.interpret(current.eventlist[ShieldEvent.EVENT_LONGPRESS]);
                   System.out.println("Event of long press "+EventConstant.Shieldbuttons[e.getbutton()]);
                   // throw new UnsupportedOperationException("Not supported yet.");
                }
      
   public void startandroid(){
             if(btclient!=null)
                 btclient.close();
             String password=prefs.getPassword();
        try {
            server=new AndroidServer(password);
            GlobalVar.setAndroidServer(server);
            server.addShieldEventListener(this);
            server.runnewthread();
        } catch (IOException ex) {
            Logger.getLogger(TeclaDesktop.class.getName()).log(Level.SEVERE, null, ex);
        }
         }
         
         
   public void startbluetoothserver(){
             if(server != null){
                server.close();
                server.closeserver();
             }
        try {
            if(server==null)
            server=new AndroidServer(prefs.getPassword());
            GlobalVar.setAndroidServer(server);
        } catch (IOException ex) {
            Logger.getLogger(TeclaDesktop.class.getName()).log(Level.SEVERE, null, ex);
        }
        try {
            btclient=new BluetoothClient(uuidstring);
            GlobalVar.setbluetoothclient(btclient);
            if(btclient.get_btstate()){
                btclient.start();
                btclient.addShieldEventListener(this);
            }
        } catch (BluetoothStateException ex) {
            ErrorDialog err=new ErrorDialog(errormessage);
            err.setVisible(true);
            mains.setandroid(); //prevents deadlock
            mains.setVisible(false);
            Logger.getLogger(TeclaDesktop.class.getName()).log(Level.SEVERE, null, ex);
        }
             
             
             
     }
                

}
