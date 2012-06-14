/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.akdroid.tecladesk;

import com.akdroid.interfaces.ClientMain;
import com.akdroid.interfaces.ShieldEvent;
import com.akdroid.interfaces.ShieldEventListener;
import java.util.Properties;

/**
 *
 * @author Akhil
 */
public class TeclaDesktop {
    public static final String uuidstring = "0000110100001000800000805F9B34FB";
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        ClientMain client=new ClientMain();
        client.setVisible(true);
        BluetoothClient btclient=new BluetoothClient(uuidstring);
        btclient.start();
        btclient.addShieldEventListener(new ShieldEventListener(){

            @Override
            public void onButtonPressed(ShieldEvent e) {
               // throw new UnsupportedOperationException("Not supported yet.");
            }

            @Override
            public void onButtonReleased(ShieldEvent e) {
               // throw new UnsupportedOperationException("Not supported yet.");
            }

            @Override
            public void onButtonClick(ShieldEvent e) {
               // throw new UnsupportedOperationException("Not supported yet.");
            }

            @Override
            public void onButtonDblClick(ShieldEvent e) {
              //  throw new UnsupportedOperationException("Not supported yet.");
            }

            @Override
            public void onLongPress(ShieldEvent e) {
               // throw new UnsupportedOperationException("Not supported yet.");
            }
        
        });
    }
}
