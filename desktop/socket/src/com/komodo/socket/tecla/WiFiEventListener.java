/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.komodo.socket.tecla;

import java.util.EventListener;

/**
 * A Listener interface to listen to WiFiEvents
 * @author akhil
 */
public interface WiFiEventListener extends EventListener {
    
    public void onClientFound();         //executes when a client is found
    public void onClientConnected();     //executes when connection to a client is successful with authentication
    public void onClientDisconnected();  //executes when the connection to a client is disconnected
    public void onNoClientFound();       //executes when no client is found
    public void onReceived(String data); //executes when data is received 
    public void onSent();                //executes when data is sent successfully.   
    
}
