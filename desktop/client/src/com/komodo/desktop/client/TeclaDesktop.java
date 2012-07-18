/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.komodo.desktop.client;

import com.komodo.desktop.interfaces.ClientMain;
import com.komodo.desktop.interfaces.ErrorDialog;
import com.komodo.desktop.interfaces.ShieldEvent;
import com.komodo.desktop.interfaces.ShieldEventListener;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.bluetooth.BluetoothStateException;


/**
 * TeclaDesktop v0.9
 * @author Akhil
 */
public class TeclaDesktop {
    public static final String uuidstring = "0000110100001000800000805F9B34FB";
    public static final String location = "Preferences";
    public static final String errormessage="Bluetooth is either turned off or you have not installed a bluetooth device,"
            + "Turn On and restart the application,The application will now quit";
    //public static final String configlocation = "Preferences/config.xml";
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        try {
            final PreferencesHandler prefs=new PreferencesHandler(location);
            BluetoothClient btclient=new BluetoothClient(uuidstring);
            
            final Thread[] threads=new Thread[6];
            
            if(btclient.get_btstate()){
            ClientMain client=new ClientMain(prefs,btclient);
            //Make client and btclient globally available.
            GlobalVar.setMainWindow(client);
            GlobalVar.setbluetoothclient(btclient);
            //show the GUI
            client.setVisible(true);
            
            
            
            final EventGenerator eventgen =new EventGenerator();
            
            btclient.start();
            
            btclient.addShieldEventListener(new ShieldEventListener(){

                @Override
                public void onButtonPressed(ShieldEvent e) {
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
                    ShieldButton current=prefs.getShieldButton(e.getbutton());
                   if(current!=null)
                   eventgen.interpret(current.eventlist[ShieldEvent.EVENT_CLICK]);
                   System.out.println("Event of click "+EventConstant.Shieldbuttons[e.getbutton()]);
                   // throw new UnsupportedOperationException("Not supported yet.");
                }

                @Override
                public void onButtonDblClick(ShieldEvent e) {
                   ShieldButton current=prefs.getShieldButton(e.getbutton());
                   if(current!=null)
                   eventgen.interpret(current.eventlist[ShieldEvent.EVENT_DOUBLECLICK]);
                   System.out.println("Event of double click "+EventConstant.Shieldbuttons[e.getbutton()]);
                  //  throw new UnsupportedOperationException("Not supported yet.");
                }

                @Override
                public void onLongPress(ShieldEvent e) {
                    ShieldButton current=prefs.getShieldButton(e.getbutton());
                   if(current!=null)
                   eventgen.interpret(current.eventlist[ShieldEvent.EVENT_LONGPRESS]);
                   System.out.println("Event of long press "+EventConstant.Shieldbuttons[e.getbutton()]);
                   // throw new UnsupportedOperationException("Not supported yet.");
                }
            
            });
            }
        } catch (BluetoothStateException ex) {
            ErrorDialog err=new ErrorDialog(errormessage);
            err.setVisible(true);
            Logger.getLogger(TeclaDesktop.class.getName()).log(Level.SEVERE, null, ex);
        }
              
    }
}
