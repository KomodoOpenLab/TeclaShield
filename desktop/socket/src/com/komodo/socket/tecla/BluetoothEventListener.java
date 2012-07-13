/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.komodo.socket.tecla;

import java.io.DataInputStream;
import java.util.EventListener;

/**
 * The interface that is used for listening to bluetooth events
 * The functions which are going to use this implementation will have to 
 * implement this interface.
 * @author Akhil
 */
public interface BluetoothEventListener extends EventListener {
    
    public void onConnect();
    public void onDisconnect();
    public void onReceive(DataInputStream datain);
    public void onSent();
}
