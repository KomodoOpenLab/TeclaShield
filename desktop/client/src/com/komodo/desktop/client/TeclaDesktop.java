/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.komodo.desktop.client;

import com.komodo.desktop.interfaces.ClientMain;
import com.komodo.desktop.interfaces.ShieldEvent;
import com.komodo.desktop.interfaces.ShieldEventListener;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.xml.sax.SAXException;

/**
 * TeclaDesktop v0.9
 * @author Akhil
 */
public class TeclaDesktop {
    public static final String uuidstring = "0000110100001000800000805F9B34FB";
    public static final String location = "Preferences";
    //public static final String configlocation = "Preferences/config.xml";
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
            final PreferencesHandler prefs=new PreferencesHandler(location);
            BluetoothClient btclient=new BluetoothClient(uuidstring);            
            ClientMain client=new ClientMain(prefs,btclient);
            client.setVisible(true);
            final EventGenerator eventgen =new EventGenerator();
            
            btclient.start();
            //while(btclient == null);
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

    }
}
