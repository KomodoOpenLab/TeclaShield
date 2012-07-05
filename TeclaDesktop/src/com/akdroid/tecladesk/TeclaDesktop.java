/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.akdroid.tecladesk;

import com.akdroid.interfaces.ClientMain;
import com.akdroid.interfaces.ShieldEvent;
import com.akdroid.interfaces.ShieldEventListener;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.xml.sax.SAXException;

/**
 *
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
                   System.out.println("Event of press"+e.getbutton());
                   // throw new UnsupportedOperationException("Not supported yet.");
                }

                @Override
                public void onButtonReleased(ShieldEvent e) {
                    ShieldButton current=prefs.getShieldButton(e.getbutton());
                   if(current!=null)
                   eventgen.interpret(current.eventlist[ShieldEvent.EVENT_RELEASED]);
                    //System.out.println("Released");
                    // throw new UnsupportedOperationException("Not supported yet.");
                }

                @Override
                public void onButtonClick(ShieldEvent e) {
                    ShieldButton current=prefs.getShieldButton(e.getbutton());
                   if(current!=null)
                   eventgen.interpret(current.eventlist[ShieldEvent.EVENT_CLICK]);
                   // throw new UnsupportedOperationException("Not supported yet.");
                }

                @Override
                public void onButtonDblClick(ShieldEvent e) {
                   ShieldButton current=prefs.getShieldButton(e.getbutton());
                   if(current!=null)
                   eventgen.interpret(current.eventlist[ShieldEvent.EVENT_DOUBLECLICK]);
                  //  throw new UnsupportedOperationException("Not supported yet.");
                }

                @Override
                public void onLongPress(ShieldEvent e) {
                    ShieldButton current=prefs.getShieldButton(e.getbutton());
                   if(current!=null)
                   eventgen.interpret(current.eventlist[ShieldEvent.EVENT_LONGPRESS]);
                   // throw new UnsupportedOperationException("Not supported yet.");
                }
            
            });

    }
}
