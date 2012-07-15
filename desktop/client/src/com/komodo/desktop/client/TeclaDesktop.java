/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.komodo.desktop.client;

import com.komodo.desktop.interfaces.ClientMain;
import com.komodo.desktop.interfaces.ErrorDialog;
import com.komodo.desktop.interfaces.ShieldEvent;
import com.komodo.desktop.interfaces.ShieldEventListener;


/**
 * TeclaDesktop v0.9
 * @author Akhil
 */
public class TeclaDesktop {
    public static final String uuidstring = "0000110100001000800000805F9B34FB";
    public static final String location = "Preferences";
    public static final String errormessage="<html><div width=\"100\">Bluetooth is either turned off,Turn On and restart the application,The application will now quit</div></html>";
    //public static final String configlocation = "Preferences/config.xml";
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
            
            final PreferencesHandler prefs=new PreferencesHandler(location);
            BluetoothClient btclient=new BluetoothClient(uuidstring);  
            ErrorDialog err;
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
                   if(current!=null)
                   eventgen.interpret(current.eventlist[ShieldEvent.EVENT_PRESSED]);
                   System.out.println("Event of press "+EventConstant.Shieldbuttons[e.getbutton()]);
                   // throw new UnsupportedOperationException("Not supported yet.");
                }

                @Override
                public void onButtonReleased(ShieldEvent e) {
                    ShieldButton current=prefs.getShieldButton(e.getbutton());
                   if(current!=null)
                   eventgen.interpret(current.eventlist[ShieldEvent.EVENT_RELEASED]);
                    //System.out.println("Released");
                   System.out.println("Event of release "+EventConstant.Shieldbuttons[e.getbutton()]);
                    // throw new UnsupportedOperationException("Not supported yet.");
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
        }else{
                 err= new ErrorDialog(errormessage);
            }
              
    }
}
